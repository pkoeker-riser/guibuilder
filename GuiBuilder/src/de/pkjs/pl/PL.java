package de.pkjs.pl;


import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.jdataset.ParameterList;
import de.pkjs.pl.Database.NamedStatement;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.ParseException;

/*
 * Da man den ResultSet u.U. nur ein einziges mal lesen kann (auch die Columns
 * jeweils nur einmal!) muß dieser zuerst in eine interne Struktur übersetzt
 * werden und erst danach verarbeitet. Attribute: colName, colType, colValue,
 * isNullable, colIndex? Eine ArrayList gemäß meta.colCount Man muß aber auch
 * über den colNamen auf diese Struktur zugreifen können (pk, fk); also eine
 * ArrayList Das funktioniert schon mal... ... nächstes Problem: Suns jdbc:odbc
 * Driver kann nur ein Statement auf einmal ausführen! TODO : Two Phase Commit
 * wenn ein DataSet aus verschiedenen Datenbanken kommt. TODO : Wenn mehrere
 * Operationen auf der selben Tabelle, dann mit addbatch arbeiten.
 * Statement.addBatch geht nur mit statischem SQL!? TODO : HAVING, IN über IN
 * kann das Einlesen von GrandChild o.ä. beschleunigt werden: Wenn z.B. zu 10
 * Childs jeweils deren Childs eingelesen werden sollen, so wird jetzt mit 10
 * SQL-Statements gelesen. Hier kann man auch alle Foreign Keys sammeln, und
 * dann die GrandChilds über IN(alleMeineChilds) einlesen und danach auf die
 * Childs verteilen. Müßte auch für die Parents von Childs funktionieren. TODO :
 * JOIN für Parents (Prefix? Alias?) Eine Alternative zu JOIN ist, die
 * Primärschlüssel der Parents über die jeweilige Ergebnismenge einzusammeln,
 * sie gemeinsam einlesen IN(...,...) und dann die Parent Rows den
 * entsprechenden Rows zuzuordnen. Das sollte auch Speicher sparen, weil dann
 * die Parents auch "wiederverwendet" werden. TODO : (Grand)Childs en Bloc
 * einlesen und dann auf DataSet verteilen (Foreign Key mit IN(<eingesammelte
 * PKs der ParentTable>) TODO : Zwischen-Tabellen unterdrücken (suppress? hide?)
 * --------------------------------------------------- TODO : Nutzer und Rechte
 * Nutzer, Rolle, Rechte, RolleRecht, TableGrant (Insert, Update, select,
 * delete) ColumnGrant (select, insert, update) Dafür wird man ein
 * Session-Management brauchen (mit login und logout) TODO : Billing für User
 * --------------------------------------------------- TODO : BLOB lesen: Bytes
 * to HEX; Schreiben HEX to ? TODO : Überprüfung der View-Definitionen anhand
 * MetaData Das ist z.B. bei Sybase problemantisch, da das Einlesen der
 * Metadaten sehr lange dauert. TODO : Mapping von Klassen auf DataSet?
 * (automatisch per Reflection?) TODO : EmptyDataset am Anfang für alle Views
 * generieren und (in HashMap) vorhalten. DONE
 * ---------------------------------------------- DONE : Virtual Child Tables
 * DONE : Named Statements DONE : Database Type DONE : ConnectionPool
 * --------------------------------------------------- DONE : Vergabe der
 * Primärschlüssel je Tabelle anders regeln: - Es können beliebig viele
 * Sequences definiert werden; - eine davon ist die Default-Sequence. - Angabe
 * einer Sequence je Tabelle <... pksequence="ScheckNummer" - MAX++ : SELECT
 * MAX(MyId) FROM ... - Wenn keine Angabe, dann Default-Sequence
 * --------------------------------------------------- DONE : transiente Columns
 * über readonly? --> sowohl als auch! DONE : Rekursive Datenstrukturen kann man
 * mit der derzeitigen Syntax nicht definieren! DONE : CreateUserField/Update
 * protokollieren (woher den Namen des Benutzers?) DONE : Parametrisierte
 * Filter: WHERE jahr = $jahr AND Betrag > $MinBetrag Alles mit Prepared
 * Statements! Wegen Sonderzeichen und SQL-Injection maxlen; Datenbank-Struktur
 * einlesen in XML-Dokument; pk und fk Optimistic Locking über Versions-Zähler
 * (Feld-Name steht in Config) GROUP BY done ColumnTypes mit ausgeben aus
 * MetaData done : Datum (91), Uhrzeit (92) Format als Parameter? done :
 * readonly (z.B. bei Parents) - notnull mit ausgeben
 */
/**
 * Implementierung eines JDBC <b>P</b>ersistenz <b>L</b>ayers.
 */
public final class PL implements IPLContext {
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PL.class);
  private Database database;
  Database getDatabase() {
     return this.database;
  }
  private DatabaseConnectionPool pool;
  DatabaseConnectionPool getDatabaseConnectionPool() {
     return this.pool;
  }
  private PLTransactionContext pl;
  
  private ArrayBlockingQueue<JDataSet> asyncQueue;
  private Thread asyncWorkerThread;
  
  private static PLMBeanServer mBeanServer;
  private static CacheManager cacheManager;
  
  static CacheManager getCacheManager() {
    synchronized (CacheManager.class) {
      if (cacheManager == null) {
        try {
      	  cacheManager = CacheManager.getInstance();
        } catch (Throwable ex) {
          logger.error(ex.getMessage(), ex);
        }
      }
    }
    return cacheManager;
  }
    
  public static final String START_TRANSACTION = "START_TRANSACTION";
  public static final String ROLLBACK = "ROLLBACK";
  public static final String COMMIT = "COMMIT";

  // Constructor
  /**
   * Erzeugt einen neuen Persistenz-Layer.
   * <p>
   * Die Einstellungen werden aus "PLConfig.xml" eingelesen. <br>
   */
  public PL() throws Exception {
    this("PLConfig.xml");
  }

  /**
   * Erzeugt einen PersistenzLayer aus der angegebenen Konfigurationsdatei.
   * <strong>Achtung! </strong>
   * Die genannte Datei wird zuerst im ClassPath gesucht, dann im aktuellen Verzeichnis,
   * und zuletzt im übergeordneten Verzeichnis.
   * @param configFileName
   * @throws Exception
   */
  public PL(String configFileName) throws Exception {
    logger.debug("Try to initialize PL: " + configFileName);
    try {
      Document configDoc = loadFile(configFileName);
      this.init(configDoc, null, null);
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      System.err.println("Unable to read '" + configFileName + "'.\n" + ex.getMessage());
      throw ex;
    }
  }

  /**
   * Erzeugt einen Persistenzlayer mit der angegebenen Konfigurationsdatei und
   * der Datenbank-Authorisierung.
   * <p>
   * Die hier übergebenen Angaben bezüglich Username und Password sind vorrangig
   * gegen über denen in der Konfigurationsdatei.
   * <p>
   * Der Sinn dieses Konstruktors besteht darin, verschiedenen Usern einen
   * Persistenzdienst zu geben, die mit der selben Konfiguration arbeiten.
   * 
   * @param configFileName
   *          is null then "PLConfig.xml"
   * @param username
   *          must be != null
   * @param password
   *          must be != null
   */
  public PL(String configFileName, String username, String password) throws Exception {
    if (username == null || password == null) {
      throw new IllegalArgumentException("Missing username | password");
    }
    if (configFileName == null) {
      configFileName = "PLConfig.xml";
    }
    Document doc = this.loadFile(configFileName);
    this.init(doc, username, password);
  }

  /**
   * Erzeugt einen Persistenz-Layer aus einem XML-Dokument im Format von
   * PLConfig.dtd.
   * 
   * @param doc
   * @throws Exception
   */
  public PL(Document doc) throws Exception {
    this.init(doc, null, null);
  }

  private Document loadFile(String fileName) throws ParseException  {
    Document doc = null;
    //ClassLoader cl = this.getClass().getClassLoader();
    InputStream inp = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
    if (inp != null) {
      doc = new Document(inp);
      logger.debug("PLConfig File Loaded: " + fileName);
      return doc;
    } else {
      File f = new File(fileName);
      if (f.canRead() == false) {
        f = new File("../" + fileName);
      }
      doc = new Document(f);
      return doc;
    }
  }

  private void init(Document configDoc, String username, String password) throws Exception {
    try {
      // Database
      Element root = configDoc.getRoot();
      // Database-Objekt erzeugen
      database = new Database(root, username, password);
      this.reset();
      // BlockingQueue für asynchrones Schreiben
      int capa = database.getBlockingQueueCapacity();
      if (capa > 0) {
         this.asyncQueue = new ArrayBlockingQueue<JDataSet>(capa, true); // 10.1.2011 PKÖ: Parametrisiert
         asyncWorkerThread = new Thread() {
           public void run() {
             while (true) {
               try {
                 int size = asyncQueue.size();
                 if (size > 0) {
                   logger.debug("Async Dataset Writer Queue size: " + size);
                 }
                 JDataSet ds = asyncQueue.poll(1000, TimeUnit.MILLISECONDS);
                 if (ds != null) {
                   int cnt = setDataset(ds);
                   logger.debug("Async Dataset Writer Queue update: " + ds.getDatasetName() + " " 
                         + cnt + " Records updated");
                 }
               } catch (InterruptedException ex) {
                 // Thread interrupted
               } catch (PLException ex) {
                 logger.error(ex.getMessage(), ex);
               }
             }
           }
         };   
         asyncWorkerThread.start();
      }
      logger.info("PL [ " + this.getLayerName() + " ] " + "PL initialization successfully completed.");
      // Feddisch!
    } catch (Exception ex) {
      String msg = "PL [ " + this.getLayerName() + " ] " + "Error initializing persistence layer: "
          + ex.getMessage();
      logger.error(msg, ex);
      throw new PLException(msg, ex);
    }
  }

  /**
   * Liefert den Context für eine benutzerdefinierte Datenbanktransaktion.
   * <p>
   * <code><pre>
   * PL pl = new PL();
   * IPLContext context = pl.startNewTransaction(&quot;MyTransName&quot;);
   * try {
   *   context.setDataset(myDataSet1);
   *   context.setDataset(myDataSet2);
   *   [weitere Datenbankoperationen]
   *   context.commitTransaction(&quot;MyTransName&quot;);
   *   // Der Context ist jetzt &quot;verbraucht&quot;
   *   // und darf nicht mehr verwendet werden!
   * } catch (Exception ex) {
   *   context.rollbackTransaction(&quot;MyTransName&quot;);
   * }
   * </pre></code>
   * 
   * @param transName
   * @return
   * @throws PLException
   */
  public IPLContext startNewTransaction(String transName) throws PLException {
    PLTransactionContext plc = new PLTransactionContext(this.database, this.pool, transName, false);
    plc.startTransaction(transName);
    return plc;
  }

  /**
   * Führt den Persistenz-Dienst herunter; es wird der Connection-Pool
   * geschlossen (und damit alle Datenbankverbindungen).
   * <p>
   * Diese Methode kann z.B. beim Spring-Framework als destroy-Method
   * eingetragen werden.
   * 
   * @throws PLException
   */
  public void shutdown() throws PLException {
    try {
      logger.info("Shutdown invoked");
      this.close();
    } catch (PLException ex) {
      String msg = "PL [ " + getLayerName() + " ] shutdown error: " + ex.getMessage();
      logger.error(msg, ex);
      throw new PLException(msg, ex);
    }
  }
  
  private void close() throws PLException {
    // asyncQueue abarbeiten
    if (asyncQueue != null) {
      int cnt = 0;
      while(this.asyncQueue.size() > 0 && cnt < 100) { // nach 100 Sekunden geben wirs auf
        try {
          logger.debug("Waiting for Async Dataset Writer Queue: " + asyncQueue.size());
          Thread.sleep(1000);
          cnt++;
        } catch (InterruptedException ex) {
        }
      }
      asyncQueue.clear();
      asyncQueue = null;
    }
    // Pool leeren
    if (pool != null) pool.close();
    // Alle Caches löschen
    if (cacheManager != null) {
      cacheManager.removalAll();
      logger.debug("All Caches removed");
    }
    // MBeans löschen wie?
    mBeanServer = null;
  }
  
  public void finalize() {
    try {
      this.close();
    } catch (PLException ex) {
      logger.error(ex.getMessage(), ex);
    }
  }
  
  public Collection<TransactionInfo> getSessions() {
    return database.getTransactionInfos();
  }

  /**
   * Der Cache mit den Datenbankabfragen wird gelöscht.
   * 
   * @throws PLException
   */
  synchronized public void reset() throws PLException {
    this.close();
    
    // Connection pool anlegen
    pool = new DatabaseConnectionPool(database);
    logger.debug("New Connection Pool created");

    // Alle Caches löschen
    if (cacheManager != null) {
      cacheManager.removalAll();
      logger.debug("All Caches removed");
    }

    // Connection holen
    DatabaseConnection dbConnection = null;
    try {
      dbConnection = pool.getConnection("PL#reset InitDatabaseClass");
      dbConnection.startTransaction("InitDatabaseClass");
      database.reset(dbConnection);
      dbConnection.commitTransaction("InitDatabaseClass");
      this.pl = new PLTransactionContext(database, pool);
    } catch (PLException ex) {
   	 logger.warn(ex.getMessage(), ex);
   	 try {
   		 if (dbConnection != null) {
   			 dbConnection.rollbackTransaction("InitDatabaseClass");
   		 }
   	 } catch (PLException e) {
   		 logger.error(e.getMessage(), e);
   	 }
   	 String msg = "PL [ " + getLayerName() + " ] Error resetting PL: " + ex.getMessage();
   	 logger.error(msg, ex);
   	 throw new PLException(msg, ex);
    } finally {
      if (dbConnection != null) {
      	try {
      		dbConnection.close("PL#reset");
      	} catch (Exception ex) {}
      }
    }
    // MBean-Server
    if (database.isJMX()) {
       try {
         mBeanServer = new PLMBeanServer(this);
         if (cacheManager != null) {
           ManagementService.registerMBeans(cacheManager, PLMBeanServer.getServer(), false, false, false, true);
         }
       } catch (Exception ex) {
          logger.error(ex.getMessage(), ex);
       }
    }
}

  /**
   * @deprecated Erzeugt für jede Tabelle in der Datenbank einen Request mit dem
   *             Namen der Tabelle.
   * @return
   */
  public int initDefaultTableRequests() throws PLException {
    int cnt = 0;
    // Connection holen
    DatabaseConnection dbConnection = null;
    try {
      dbConnection = pool.getConnection("PL#initDefaultTableRequests");
      dbConnection.startTransaction("InitDefaultTableRequests");
      Database db = this.getCurrentDatabase();
      Iterator<JDataTable> it = db.getDataTables();
      if (it == null)
        return 0;
      while (it.hasNext()) {
        JDataTable tbl = it.next();
        String reqName = "Default_" + tbl.getTablename();
        try {// Doppelte Tabellennamen "public.rudi" und "rudi" 
           this.getRequest(reqName);
           logger.warn("Duplicate DatasetName: " + reqName);
        } catch (IllegalArgumentException ex) {
           Request req = db.createRequest(reqName);
           req.createRootTableRequest(dbConnection, tbl.getTablename(), "*");
           cnt++;           
        }
      }
      dbConnection.commitTransaction("InitDefaultTableRequests");
    } catch (PLException ex) {
      try {
        if (dbConnection != null) {
          dbConnection.rollbackTransaction("InitDefaultTableRequests");
        }
      } catch (PLException e) {
      }
      String msg = "PL [ " + getLayerName() + " ] Error initialising default Requests: " + ex.getMessage();
      logger.error(msg, ex);
      throw new PLException(msg, ex);
    } finally {
      if (dbConnection != null) {
        dbConnection.close("PL#initDefaultTableRequests");
      }
    }
    return cnt;
  }

  /**
   * Liefert den gesamten Persistenz-Layer mit all seinen Eigenschaften:
   * <ul>
   * <li>Optionen des PL selbst
   * <li>Die Datenbank mit ihren Tabellen (aus JDBC-Metadaten)
   * <li>Die definierten Zugriffe (aus DatabaseConfig.xml)
   * </ul>
   * 
   * @return
   */
  public Document getPLMetaDataDoc() {
    Document doc = pl.getPLMetaDataDoc();
    return doc;
  }

  /**
   * Wie PLMetaDataDoc nur als String
   * 
   * @return
   */
  public String getPLMetaData() {
    return this.getPLMetaDataDoc().toString();
  }


  /*
   * (non-Javadoc)
   * 
   * @see de.pkjs.pl.IPL#getRequest(java.lang.String)
   */
  public Request getRequest(String datasetName) {
    return database.getRequest(datasetName);
  }

  public String getLayerName() {
    if (this.database == null) {
      return "Not initialized";
    } 
    return this.database.getLayerName();
  }

  // ############## IPLContext
  public Database getCurrentDatabase() {
    return database;
  }

  public String getCurrentDatabaseName() {
    return database.getDatabaseName();
  }

  public JDataSet getDataset(String datasetname, long oid) throws PLException {
    return pl.getDataset(datasetname, oid);
  }

  public JDataSet getDataset(String datasetname, long[] oids) throws PLException {
    return pl.getDataset(datasetname, oids);
  }

  public JDataSet getDataset(String datasetname, String key) throws PLException {
    return pl.getDataset(datasetname, key);
  }

  public String getDatasetString(String datasetname, long oid) throws PLException {
    return pl.getDatasetString(datasetname, oid);
  }

  public JDataSet getDataset(String datasetname, ParameterList parameters) throws PLException {
    return pl.getDataset(datasetname, parameters);
  }

  public int setDataset(JDataSet dataset) throws PLException {
    return pl.setDataset(dataset); // TODO: Hier commitChanges ausführen?
  }
  /**
   * Die Daten werden asynchron in die Datenbank geschrieben;
   * deshalb gibts hier keinen Rückgabewert.<p>
   * Die Anzahl der Threads für die Queue wird mit <BlockingQueueCapacity value="##"/> in PLConfig definiert;
   * default = 10. Wenn =0, dann ist diese Funktion nicht mehr verfügbar.
   * @param dataset
   * @throws PLException
   */
  public void setDatasetAsync(JDataSet dataset) throws PLException {
    if (asyncQueue == null) {
      throw new IllegalStateException("<BlockingQueueCapacity /> not defined");
    }
    boolean ok = false;
      try {    
        ok = asyncQueue.offer(dataset, 1000, TimeUnit.MILLISECONDS);
      } catch (InterruptedException ex) {
        logger.error(ex.getMessage(), ex);
      }
      if (!ok) {
        this.setDataset(dataset);
      }
   }

  public int setDataset(String dataset) throws PLException {
    return pl.setDataset(dataset);
  }
  public int setDataset(List<JDataSet> datasets) throws PLException {
     return pl.setDataset(datasets);
  }
  
  public void clearCache(String datasetname) throws PLException {
    pl.clearCache(datasetname);
  }

  public JDataSet getAll(String datasetname) throws PLException {
    return pl.getAll(datasetname);
  }

  /**
   * @deprecated
   */
  public JDataSet getDatasetSql(String tablename, String columns, String from) throws PLException {
    return pl.getDatasetSql(tablename, columns, from);
  }

  public JDataSet getDatasetSql(String datasetname, String sql) throws PLException {
    return pl.getDatasetSql(datasetname, sql);
  }

  public JDataSet getDatasetSql(String datasetname, String sql, int limit) throws PLException {
    return pl.getDatasetSql(datasetname, sql, limit);
  }

  public JDataSet getDatasetSql(String datasetname, String sql, ParameterList list) throws PLException {
    return pl.getDatasetSql(datasetname, sql, list);
  }

  public Database.NamedStatement getNamedStatement(String name) {
     return pl.getNamedStatement(name);
  }
  public JDataSet getDatasetStatement(NamedStatement nst, ParameterList list) throws PLException {
     return pl.getDatasetStatement(nst, list);
  }
  public JDataSet getDatasetStatement(String name) throws PLException {
    return pl.getDatasetStatement(name, null);
  }

  public JDataSet getDatasetStatement(String name, ParameterList list) throws PLException {
    return pl.getDatasetStatement(name, list);
  }

  public DataRowIterator getDataRowIteratorStatement(String statementName, ParameterList list)
      throws PLException {
    return pl.getDataRowIteratorStatement(statementName, list);
  }

  public DataRowIterator getDataRowIterator(String datasetname, String tablename, String sql,
      ParameterList list) throws PLException {
    return pl.getDataRowIterator(datasetname, tablename, sql, list);
  }

  public JDataSet getEmptyDataset(String datasetname) {
    return pl.getEmptyDataset(datasetname);
  }

  public long getOID() throws PLException {
    return pl.getOID();
  }

  public long getOID(String sequenceName) throws PLException {
    return pl.getOID(sequenceName);
  }

  public int executeSql(String sqlCommand) throws PLException {
    return pl.executeSql(sqlCommand);
  }

  public int executeSql(String sqlCommand, ParameterList parameters) throws PLException {
    return pl.executeSql(sqlCommand, parameters);
  }

  public int executeSqlPara(String sqlCommand, Vector<Object> parameter) throws PLException {
    return pl.executeSqlPara(sqlCommand, parameter);
  }

  public int executeStatement(String name) throws PLException {
    return pl.executeStatement(name);
  }

  public int executeStatement(String name, ParameterList parameters) throws PLException {
    return pl.executeStatement(name, parameters);
  }

  /**
   * Führt ein Batch-Statement aus.
   * <p>
   * Batch-Statements sind eine Menge von NamedStatements, die nacheinander als
   * Batch ausgeführt werden.
   * 
   * @param name
   * @return
   * @throws PLException
   */
  public int executeBatchStatement(String name) throws PLException {
    return pl.executeBatchStatement(name);
  }

  /**
   * Führt ein Batch-Statement aus.
   * <p>
   * Batch-Statements sind eine Menge von NamedStatements, die nacheinander als
   * Batch ausgeführt werden.
   * 
   * @param name
   * @param list
   *          Eine Liste von Parametern für die Menge aller Parameter, die für
   *          die verschiedenen Statements insgesamt benötigt werden.
   * @return
   * @throws PLException
   */
  public int executeBatchStatement(String name, ParameterList list) throws PLException {
    return pl.executeBatchStatement(name, list);
  }

  public String getDatabaseMetaData() {
    return pl.getDatabaseMetaData();
  }

  public Document getDatabaseMetaDataDoc() {
    return pl.getDatabaseMetaDataDoc();
  }

  public JDataSet getMetaDataSet() {
    return pl.getMetaDataSet();
  }

  public Request addRequest(JDataRow metaDataRow) throws PLException {
    return pl.addRequest(metaDataRow);
  }

  public ArrayList<String> getDatasetNames() {
    return pl.getDatasetNames();
  }

  /**
   * Führt das "Ping"-Statement der Datenbank aus, wie in PLConfig definiert.
   * 
   * @return Liefert "pong" wenn alles OK, "No PingStatement defined" wenn in
   *         PLConfig keine PingStatement definiert ist.
   * @throws PLException
   */
  public String pingDatabase() throws PLException {
    return pl.pingDatabase();
  }
//  /**
//   * Importiert die Journal-Files aus dem angegebenen Verzeichnis
//   * @param dir
//   * @return
//   */
//  public int importJournal(String dir) throws PLException {
//    int ret = 0;
//    File f = new File(dir);
//    File[] fs = f.listFiles();
//    for (File file: fs) {
//      int anz = pl.importJournal(file);
//      ret += anz;
//    }    
//    return ret;
//  }
//  /**
//  * Importiert den angegebenen Journal-File
//  * @param f
//  * @return
//  * @throws PLException
//  */
//  public int importJournal(File file) throws PLException {
//    return pl.importJournal(file);
//  }

  public int importJournal(List<Document> transaction) throws PLException {
    if (transaction == null || transaction.size() == 0) {
      logger.warn("Empty Transaction!");
      return 0;
    }
    String transname = "**import**";
    if (transaction.size() >= 2) {
      Document docStart = transaction.get(0);
      Element eleStart = docStart.getRoot();
      Document docEnd = transaction.get(transaction.size()-1);
      Element eleEnd = docEnd.getRoot();
      if (eleStart.getName().equalsIgnoreCase("Transaction") && eleEnd.getName().equalsIgnoreCase("Transaction")) {
        transname = eleStart.getAttribute("name");        
        String type = eleEnd.getTextString();
        if (type.equalsIgnoreCase("ROLLBACK")) {
          logger.debug("Rollbacked transaction: " + transname);
          return 0;
        }
        if (transaction.size() == 2) {
          logger.debug("Empty transaction: " + transname);
          return 0;
        }
        transaction.remove(0);
        transaction.remove(transaction.size()-1);
      }
    }
    IPLContext ipl = this.startNewTransaction(transname);
    try {
      int anz = ipl.importJournal(transaction);
      ipl.commitTransaction(transname);
      return anz;
    } catch (PLException ex) { // TODO: Was tun bei Fehler ? Retry, Skip, Abort?
      ipl.rollbackTransaction(transname);
      throw ex;
    }
  }

  public SimpleDateFormat getDateFormat() {
    return database.getDateFormat();
  }

  public SimpleDateFormat getTimeFormat() {
    return database.getTimeFormat();
  }

  public SimpleDateFormat getTimestampFormat() {
    return database.getTimestampFormat();
  }

  public String getTodayString() {
    return database.getTodayString();
  }

  public String getNowString() {
    return pl.getNowString();
  }

  public String getTodayNowString() {
    return pl.getTodayNowString();
  }

  public void setDebug(boolean state) {
    database.setDebug(state);
  }

  public boolean isDebug() {
    return database.isDebug();
  }

  public void startTransaction(String transName) throws PLException {
    this.pl.startTransaction(transName);
  }

  public boolean commitTransaction(String transName) throws PLException {
    boolean ret = this.pl.commitTransaction(transName); 
    return ret;
  }

  public boolean testCommit() throws PLException {
    return this.pl.testCommit();
  }

  public boolean rollbackTransaction(String transName) throws PLException {
    boolean ret = this.pl.rollbackTransaction(transName);
    return ret;
  }

  public int getNumActive() {
    return this.pl.getNumActive();
  }

  public int getNumIdle() {
    return this.pl.getNumIdle();
  }

  public int getMaxActive() {
    return this.pl.getMaxActive();
  }

  public int getMaxIdle() {
    return this.pl.getMaxIdle();
  }

  public int getMinIdle() {
    return this.pl.getMinIdle();
  }

  public long getConnectionTimeOut() {
    return this.pl.getConnectionTimeOut();
  }

  public String getTransactionName() {
     if (pl == null) return null;
     return pl.getTransactionName();
  }

  public PLTransactionContextMBean getMBeanPL() {
    return this.pl;
  }

  public DatabaseMBean getMBeanDB() {
    return this.pl.getCurrentDatabase();
  }
  public Collection<TransactionInfo> getTransactionInfos() {
    Collection<TransactionInfo> col = database.getTransactionInfos();
    return col;
  }

  /**
   * Setzt das Directory, in das Offline Transaktionen geschrieben werden 
   * @param dir oder null zum Abschalten
   */
  public void setUpdateJournalDirectory(String dir) {
    this.database.setUpdateJournalDirectory(dir);
  }
  /**
   * Liefert das Directory, in das Offline Transaktionen geschrieben werden
   * @return oder null, wenn Funktionalität abgeschaltet
   */
  public String getUpdateJournalDirectory() {
    return this.database.getUpdateJournalDirectory();
  }
    
//  private static class WriteDatasetWorker implements Runnable {
//     private IPLContext ctx;
//     private JDataSet ds;
//     
//     WriteDatasetWorker(IPLContext ctx, JDataSet ds) {
//        this.ctx = ctx;
//        this.ds = ds;
//     }
//      
//      public void run() {
//         try {
//            logger.debug("Write Dataset: " + ds);
//            int cnt = ctx.setDataset(ds);
//            logger.debug("Dataset updated: " + cnt + " records");
//         } catch (PLException ex) {
//            logger.error(ex);
//         }
//      }     
//  }
  
}