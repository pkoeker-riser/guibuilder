package de.pkjs.pl;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.w3c.dom.Node;

import de.jdataset.JDataColumn;
import de.jdataset.JDataRow;
import de.jdataset.JDataTable;
import de.jdataset.NVPair;
import de.jdataset.ParameterList;
import de.pkjs.util.Convert;
import electric.xml.Comment;
import electric.xml.DocType;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;

/**
 * @author ikunin
 */
public final class Database implements DatabaseMBean {
   // Database Software
   public static final int UNKNOWN = 0;
   public static final int JDBC_ODBC = 1;
   public static final int MYSQL = 2;
   public static final int FIREBIRD = 3;
   public static final int SQL_SERVER = 4;
   public static final int MCKOI = 5;
   public static final int MAX_DB = 6;
   public static final int SYBASE = 7;
   public static final int ORACLE = 8;
   public static final int CACHE = 9;
   public static final int DB2 = 10;
   public static final int AXION = 11;
   public static final int HSQLDB = 12;
   public static final int POSTGRES = 13;
   public static final int HANA = 14;
   public static final int MARIA = 15;

   private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Database.class);

   // Default Formats
   private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
   private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
   private SimpleDateFormat timestampFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
   private DecimalFormat decimalFormat = new DecimalFormat("#0.00");

   private String datasetElementName = "Dataset";
   // Attributes
   public static final LinkedHashMap<String, Integer> databaseTypes = new LinkedHashMap<String, Integer>();

   private Element elDatabase;
   private Elements elNamedSequences;
   private Elements elViews;
   private Elements elStatements;
   private Elements elBatches;

   private int dbType = UNKNOWN; // DataBaseType
   private boolean isDebug;
   private boolean jmx;
   private String encoding = "UFT-8";
   private String databaseName;
   private String jdbcDriver;
   private String databaseURL;
   private String catalog;
   private String schema;

   String getSchema() {
      return schema;
   }

   private String username;
   private String password;
   private LinkedHashMap<String, JDataTable> tables = new LinkedHashMap<String, JDataTable>();
   private int transactionIsolationLevel = Connection.TRANSACTION_READ_COMMITTED; // Default
   /**
    * SessionID der Datenbank ermitteln (bei hoch belasteten Datenbanken besser ausschalten)
    */
   private boolean detectDatabaseSessionID;

   public boolean isDetectDatabaseSessionID() {
      return detectDatabaseSessionID;
   }

   public void setDetectDatabaseSessionID(boolean b) {
      this.detectDatabaseSessionID = b;
   }

   private String getSeq;
   private String setSeq;
   private String validationQuery;
   private String initSQL;
   private String[] tbl_types = {"TABLE", "VIEW"}; // Eingelesene Metadaten
   // "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY",
   // "ALIAS", "SYNONYM"
   // String[] tbl_types = {"TABLE"};

   // Options
   private boolean readMetaDataReferences = true;
   private String optimisticField;
   private String createUserField;
   private String updateUserField;
   private String updateJournalDirectory;
   private DatabaseJournal dbJournal;
   private Logger slowQueryLogger;
   private int defaultMaxExcutionTime = 5000;
   private int blockingQueueCapacity = 10;

   public int getBlockingQueueCapacity() {
      return this.blockingQueueCapacity;
   }

   private String datasetDefinitionFileName;
   private LinkedHashMap<String, Request> requests = new LinkedHashMap<String, Request>();
   private LinkedHashMap<String, NamedStatement> statements = new LinkedHashMap<String, NamedStatement>();
   private LinkedHashMap<String, BatchStatement> batches;
   private LinkedHashMap<String, NamedSequence> sequences;

   private final java.util.Date createdTimeStamp = new java.util.Date();
   private java.util.Date resetTimeStamp;

   private static JDataTable metaDataTable;
   private PoolProperties pp = new PoolProperties();
   private org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();

   org.apache.tomcat.jdbc.pool.DataSource getDataSource() {
      return dataSource;
   }

   private LinkedHashMap<Long, TransactionInfo> transactions = new LinkedHashMap<Long, TransactionInfo>();

   void putTransactionInfo(TransactionInfo tInfo) {
      synchronized(transactions) {
         this.transactions.put(tInfo.getId(), tInfo);
      }
      if(transactions.size() > this.getMaxActive()) {
         logger.error("MaxActive Transaction exceeded! " + transactions.size());
      }
   }

   TransactionInfo removeTransactionInfo(long id) {
      synchronized(transactions) {
         return this.transactions.remove(id);
      }
   }

   TransactionInfo getTransactionInfo(long id) {
      return this.transactions.get(id);
   }

   public Collection<TransactionInfo> getTransactionInfos() {
      synchronized(transactions) {
         Collection<TransactionInfo> col = new ArrayList<TransactionInfo>(this.transactions.values());
         return col;
      }
   }

   // Constructor ===================================
   Database(Element root, String username, String password) throws PLException {
      // Aktivierte DB rausfischen
      Elements dbEles = root.getElements("Database");
      while(dbEles.hasMoreElements()) {
         Element dbEle = dbEles.next();
         String sEn = dbEle.getAttribute("enabled");
         if(sEn == null || sEn.equalsIgnoreCase("true")) {
            elDatabase = dbEle;
         }
      }
      if(elDatabase == null) {
         throw new IllegalStateException("No enabled Database defined!");
      }

      // DatabaseName
      this.databaseName = elDatabase.getAttribute("name");
      { // Options; Muß zuerst erfolgen wegen debug!
         Element optEle = root.getElement("Options");
         if(optEle != null) {
            // Debug
            Element debugEle = optEle.getElement("Debug");
            if(debugEle != null) {
               String sDebug = debugEle.getAttribute("value");
               if(sDebug != null && sDebug.equals("true")) {
                  this.isDebug = true;
               }
            }
            // JMX
            Element jmxEle = optEle.getElement("JMX");
            if(jmxEle != null) {
               String s = jmxEle.getAttribute("value");
               if(s != null && s.equals("true")) {
                  this.jmx = true;
               }
            }
            // Jounal
            Element updDirEle = optEle.getElement("UpdateJournalDirectory");
            if(updDirEle != null) {
               String sDir = updDirEle.getTextString();
               if(sDir.length() > 0) {
                  this.setUpdateJournalDirectory(sDir);
               }
            }
            // SlowQuery
            Element slowEle = optEle.getElement("SlowQueryLogger");
            if(slowEle != null) {
               String loggerName = slowEle.getAttribute("loggerName");
               slowQueryLogger = Logger.getLogger(loggerName);
               String s = slowEle.getAttribute("defaultMaxExecutionTime");
               if(s != null) {
                  defaultMaxExcutionTime = Convert.toInt(s);
               }
            }
            // BlockingQueueCapacity
            Element capaEle = optEle.getElement("BlockingQueueCapacity");
            if(capaEle != null) {
               String s = capaEle.getAttribute("value");
               if(s != null) {
                  blockingQueueCapacity = Convert.toInt(s);
               }
            }
         }
      }

      try {
         Element defiEle = root.getElement("DatasetDefinitionFile");
         if(defiEle != null && istLeer(defiEle.getTextString()) == false) {
            this.datasetDefinitionFileName = defiEle.getTextString();
            Document viewDoc = null;
            /*
             * KKN 30.04.2004 Laden der Dataset-Definitionen aus einem JAR-File (via
             * ResourceClass). Im PLConfig.xml kann im Element
             * "DatasetDefinitionFile" z.B. folgendes angegeben werden
             * <DatasetDefinitionFile
             * >CLASS:psi.smul.invekos.client.resources.data.DataResource
             * :InVeKoSConfig.xml </DatasetDefinitionFile> Dabei dient "CLASS:" als
             * Kennzeichen dafür, das die Dataset-Definition via Resourceloader
             * geladen werden soll. Als zweites Attribut wird der komplette Name der
             * Resource-Klasse erwartet. Als drittes Attribut wird der Name des
             * Dataset-Definition-XML erwartet.
             */
            if(this.datasetDefinitionFileName.startsWith("CLASS:")) {
               StringTokenizer st = new StringTokenizer(this.datasetDefinitionFileName, ":");
               // String sDummy = st.nextToken();
               st.nextToken();
               if(!st.hasMoreTokens()) {
                  logger.error("invalid datasetdefinition " + datasetDefinitionFileName);
                  throw new Exception("invalid datasetdefinition " + datasetDefinitionFileName);
               }
               String classToLoad = st.nextToken();
               if(!st.hasMoreTokens()) {
                  logger.error("invalid datasetdefinition " + datasetDefinitionFileName);
                  throw new Exception("invalid datasetdefinition " + datasetDefinitionFileName);
               }
               String fileName = st.nextToken();
               Class<?> resClass = null;
               try {
                  resClass = Class.forName(classToLoad);
               }
               catch(Exception ex) {
                  logger.error("Unable to load Resourceclass " + classToLoad);
                  throw ex;
               }
               try {
                  viewDoc = new Document(resClass.getResourceAsStream(fileName));
               }
               catch(Exception ex) {
                  logger.error("Unable to load resourcefile " + fileName);
                  throw ex;
               }
            }
            else {
               viewDoc = new Document(new File(this.datasetDefinitionFileName));
            }
            Element rootViewEle = viewDoc.getRoot();
            elViews = rootViewEle.getElements("View");
            elStatements = rootViewEle.getElements("Statement");
            elBatches = rootViewEle.getElements("Batch");
         }
         else {
            elViews = root.getElements("View");
            elStatements = root.getElements("Statement");
            elBatches = root.getElements("Batch");
         }
      }
      catch(Exception ex) {
         String msg = "PL [ " + this.getLayerName() + " ] " + "Unable to read dataset definition file: " + ex.getMessage();
         System.err.println(msg);
         logger.error(msg, ex);
         throw new PLException(msg);
      }

      // DatabaseType
      String _dbType = elDatabase.getAttribute("type");
      if(_dbType != null) {
         this.dbType = getSupportedDatabaseType(_dbType);
         if(this.dbType == UNKNOWN)
            logger.error("PL [ " + databaseName + " ] " + "new Database: Unknown DatabaseType: '" + _dbType + "'");
      }

      Element driverEle = elDatabase.getElement("JDBC-Driver");
      if(driverEle != null) {
         jdbcDriver = driverEle.getTextString();
         pp.setDriverClassName(jdbcDriver);
      }
      Element urlEle = elDatabase.getElement("URL");
      if(urlEle != null) {
         databaseURL = urlEle.getTextString();
         pp.setUrl(databaseURL);
         logger.info("DatabaseURL: " + databaseURL);
      }
      //    Element dataSourceEle = elDatabase.getElement("DataSource");
      //    if (dataSourceEle != null) {
      //      dataSource = dataSourceEle.getTextString();
      //      logger.info("DataSouce: "+dataSource);
      //    }
      this.setDatabaseType(databaseURL);
      // Alles raus, was nicht zu dieser DB gehört.
      this.removeOtherDatabaseElements(elDatabase, this.getDatabaseType());
      this.removeOtherDatabaseElements(elViews, this.getDatabaseType());
      elViews.first();
      this.removeOtherDatabaseElements(elStatements, this.getDatabaseType());
      elStatements.first();

      Element catEle = elDatabase.getElement("Catalog");
      if(catEle != null) {
         catalog = catEle.getTextString();
         pp.setDefaultCatalog(catalog);
      }

      Element schEle = elDatabase.getElement("Schema");
      if(schEle != null) {
         schema = schEle.getTextString();
      }
      if(username != null && password != null) {
         this.username = username;
         this.password = password;
      }
      else { // Username + Password nicht als Argument übergeben
         Element userEle = elDatabase.getElement("Username");
         if(userEle != null) {
            this.username = userEle.getTextString();
         }
         Element passwordEle = elDatabase.getElement("Password");
         if(passwordEle != null) {
            this.password = passwordEle.getTextString();
         }
      }
      pp.setUsername(this.username);
      pp.setPassword(this.password);
      // TableTypes
      Element ttEle = elDatabase.getElement("MetadataTableTypes");
      if(ttEle != null) {
         String stt = ttEle.getTextString();
         StringTokenizer toks = new StringTokenizer(stt, "|");
         tbl_types = new String[toks.countTokens()];
         int i = 0;
         while(toks.hasMoreTokens()) {
            tbl_types[i] = toks.nextToken();
            i++;
         }
      }

      // TransactionIsolation
      Element isoEle = elDatabase.getElement("TransactionIsolationLevel");
      if(isoEle != null) {
         String sTransactionLevel = isoEle.getAttribute("value");
         this.transactionIsolationLevel = getTransactionIsolationLevel(sTransactionLevel);
         // siehe initConnection
      }
      pp.setDefaultTransactionIsolation(this.getTransactionIsolationLevel());
      // detect Session
      Element detEle = elDatabase.getElement("DetectDatabaseSessionID");
      if(detEle != null) {
         String sDet = detEle.getAttribute("value");
         this.setDetectDatabaseSessionID(Convert.toBoolean(sDet));
      }

      // Config
      // MaxActiveConnections
      Element elMaxActiveConnections = elDatabase.getElement("MaxActiveConnections");
      if(elMaxActiveConnections != null) {
         String sMaxActiveConnections = elMaxActiveConnections.getAttribute("value");
         if(sMaxActiveConnections != null) {
            pp.setMaxActive(Integer.parseInt(sMaxActiveConnections));
         }
      }

      // MaxIdleConnections
      Element elMaxIdleConnections = elDatabase.getElement("MaxIdleConnections");
      if(elMaxIdleConnections != null) {
         String sMaxIdleConnections = elMaxIdleConnections.getAttribute("value");
         if(sMaxIdleConnections != null) {
            pp.setMaxIdle(Integer.parseInt(sMaxIdleConnections));
         }
      }

      // MinIdleConnections
      Element elMinIdleConnections = elDatabase.getElement("MinIdleConnections");
      if(elMinIdleConnections != null) {
         String sMinIdleConnections = elMinIdleConnections.getAttribute("value");
         if(sMinIdleConnections != null) {
            pp.setMinIdle(Integer.parseInt(sMinIdleConnections));
         }
      }
      // Neu 14.4.2014: Abandoned
      {
         Element elRemoveAbandoned = elDatabase.getElement("removeAbandoned"); // default = false
         if(elRemoveAbandoned != null) {
            boolean removeAbandoned = Convert.toBoolean(elRemoveAbandoned.getAttribute("value"));
            pp.setRemoveAbandoned(removeAbandoned);
         }
      }
      {
         Element elRemoveAbandonedTimeout = elDatabase.getElement("removeAbandonedTimeout"); // default = 60 Seconds
         if(elRemoveAbandonedTimeout != null) {
            int removeAbandonedTimeout = Convert.toInt(elRemoveAbandonedTimeout.getAttribute("value"));
            pp.setRemoveAbandonedTimeout(removeAbandonedTimeout);
         }
      }
      {
         Element elLogAbandoned = elDatabase.getElement("logAbandoned");
         if(elLogAbandoned != null) {
            boolean logAbandoned = Convert.toBoolean(elLogAbandoned.getAttribute("value"));
            pp.setLogAbandoned(logAbandoned);
         }
      }
      
      Element elConnectionTimeOut = elDatabase.getElement("ConnectionTimeOut");
      if(elConnectionTimeOut != null) {
         String value = elConnectionTimeOut.getAttribute("value");
         if(value != null) {
            /*
             * TODO: Was soll ConnectionTimeout eigentlich bewirken? maxWait, ist
             * jedenfalls die Zeit, die ein borrowObject dauern darf, bis eine
             * Exception geworfen wird (falls der Pool erschöpft ist)
             */
            pp.setMaxWait(Integer.parseInt(value));
         }
      }
      // testOnBorrow: default = true 
      boolean testOnBorrow = true;
      Element elTestOnBorrow = elDatabase.getElement("TestOnBorrow");
      {
         if(elTestOnBorrow != null) {
            testOnBorrow = Convert.toBoolean(elTestOnBorrow.getAttribute("value"));
         }
      }
      pp.setTestOnBorrow(testOnBorrow);
      // testOnReturn
      Element elTestOnReturn = elDatabase.getElement("TestOnReturn");
      {
         if(elTestOnReturn != null) {
            boolean b = Convert.toBoolean(elTestOnReturn.getAttribute("value"));
            pp.setTestOnReturn(b);
         }
      }
      // Validation
      Element elValidQuery = elDatabase.getElement("ValidationQuery");
      if(elValidQuery != null) {
         this.validationQuery = elValidQuery.getTextString();
      }
      pp.setValidationQuery(this.getValidationQuery()); // lädt u.U. den default
      // ValidInter
      Element elValidInter = elDatabase.getElement("ValidationInterval");
      {
         if(elValidInter != null) {
            int i = Convert.toInt(elValidInter.getAttribute("value"));
            pp.setValidationInterval(i);
         }
      }
      Element elInitScript = elDatabase.getElement("InitializationScript");
      if(elInitScript != null) {
         this.initSQL = elInitScript.getTextString();
         pp.setInitSQL(initSQL);
      }

      // MetaData
      Element readMetaEle = elDatabase.getElement("ReadMetaDataReferences");
      if(readMetaEle != null) {
         String sMeta = readMetaEle.getAttribute("value");
         if(sMeta != null && sMeta.equals("false")) {
            readMetaDataReferences = false;
         }
      }
      // Optimistic Locking
      Element optimistic = elDatabase.getElement("OptimisticLockingField");
      if(optimistic != null) {
         optimisticField = optimistic.getAttribute("value");
      }
      // User
      Element createUserEle = elDatabase.getElement("CreateUserField");
      if(createUserEle != null) {
         createUserField = createUserEle.getAttribute("value");
      }
      Element updateUserEle = elDatabase.getElement("UpdateUserField");
      if(updateUserEle != null) {
         updateUserField = updateUserEle.getAttribute("value");
      }
      // Default-Sequence
      try {
         Element seqElement = elDatabase.getElement("Sequence");
         Element getEle = seqElement.getElement("get");
         Element setEle = seqElement.getElement("set");
         getSeq = getEle.getTextString();
         if(setEle != null) {
            setSeq = setEle.getTextString();
         }
      }
      catch(Exception ex) {
         logger.error("PL [ " + databaseName + " ] " + "Warning: Missing <Sequence> Definition in PLConfig: " + ex.getMessage(), ex);
      }

      // Beliebig viele benannte Sequences
      this.elNamedSequences = elDatabase.getElements("Sequence");

      // Dataset
      Element dataset = root.getElement("Dataset");
      if(dataset != null) {
         Element encodingEle = dataset.getElement("Encoding");
         if(encodingEle != null) {
            encoding = encodingEle.getAttribute("value");
         }
         Element dsNameEle = dataset.getElement("ElementName");
         if(dsNameEle != null) {
            datasetElementName = dsNameEle.getAttribute("value");
         }
      }

      // Format
      Element form = root.getElement("Format");
      if(form != null) {
         try {
            dateFormat = new SimpleDateFormat(form.getElement("DateFormat").getAttribute("value"));
         }
         catch(Exception ex) {
            System.err.println("PL [ " + this.getLayerName() + " ] " + "PLConfig.xml: Missing DateFormat");
         }
         try {
            timeFormat = new SimpleDateFormat(form.getElement("TimeFormat").getAttribute("value"));
         }
         catch(Exception ex) {
            System.err.println("PL [ " + this.getLayerName() + " ] " + "PLConfig.xml: Missing TimeFormat");
         }
         try {
            timestampFormat = new SimpleDateFormat(form.getElement("TimestampFormat").getAttribute("value"));
         }
         catch(Exception ex) {
            System.err.println("PL [ " + this.getLayerName() + " ] " + "PLConfig.xml: Missing TimestampFormat");
         }
         try {
            decimalFormat = new DecimalFormat(form.getElement("DecimalFormat").getAttribute("value"));
         }
         catch(Exception ex) {
            System.err.println("PL [ " + this.getLayerName() + " ] " + "PLConfig.xml: Missing DecimalFormat");
         }
      }
   }

   /**
    * Löscht alle Elemente aus dem XML-Teilbaum, die für einen anderen
    * Datenbank-Typ vorgesehen sind.
    * 
    * @param ele
    * @param databaseType
    */
   private void removeOtherDatabaseElements(Element ele, int databaseType) {
      String s = ele.getAttribute("databaseType");
      if(s != null) {
         int type = this.getSupportedDatabaseType(s);
         if(type != databaseType) {
            @SuppressWarnings("unused")
            boolean removed = ele.remove(); // funktioniert irgendwie nicht!
            return;
         }
      }
      Elements eles = ele.getElements();
      this.removeOtherDatabaseElements(eles, databaseType);
   }

   private void removeOtherDatabaseElements(Elements eles, int databaseType) {
      if(eles == null)
         return;
      while(eles.hasMoreElements()) {
         Element child = eles.next();
         this.removeOtherDatabaseElements(child, databaseType);
      }
   }

   /**
    * Diese Methode muss gleich nach dem Erzeugen der Klasse aufgerufen werden
    * 
    * @param dbConnection
    */
   private void initDatabase(DatabaseConnection dbConnection) throws PLException {
      // Metadaten der Datenbank einlesen
      this.initMetaData(dbConnection);
      // Alle Requests vordefinieren
      this.initRequests(dbConnection, elViews);
      // Alle Named Statements vordefinieren
      this.initStatements(elStatements);
      // Alle Named batches vordefinieren
      this.initBatches(elBatches);
      // Unbenutzte Tabellen im Logger ausgeben
      Set<String> ual = this.getUnusedTablenames();
      // Named Sequences
      this.initSequences(elNamedSequences);
      // Caches
      this.initCaches();
   }

   /**
    * Alle Requests vordefinieren
    * 
    * @param dbConnection
    * @param elViews
    * @throws PLException
    */
   private void initRequests(DatabaseConnection dbConnection, Elements _elViews) throws PLException {
      this.requests = new LinkedHashMap<String, Request>();
      while(_elViews.hasMoreElements()) {
         Element viewEle = _elViews.next();
         String s = viewEle.getAttribute("databaseType");
         if(s != null) {
            int type = this.getSupportedDatabaseType(s);
            if(type != this.dbType) {
               continue;
            }
         }
         String database = viewEle.getAttribute("database");
         if(database == null || database.equalsIgnoreCase(this.getDatabaseName())) {
            Request req = null;
            try {
               req = new Request(this, dbConnection, viewEle);
            }
            catch(PLException ex) {
               logger.error("Error creating Request: [" + ex.getMessage() + "]" + '\n' + viewEle, ex);
               throw ex;
            }
            catch(Exception ex) {
               logger.error("Error creating Request: [" + ex.getMessage() + "]" + '\n' + viewEle, ex);
               throw new PLException(ex);
            }
            this.addRequest(req);
            logger.debug("init Request: " + req.getDatasetName());
         }
         else {
            // Other database
         }
      }
      logger.info("PL [ " + this.getLayerName() + " ] " + "View definitions processed.");
   }

   /**
    * Fügt dem Persistenz Layer einen neuen Request hinzu.
    * <p>
    * Der Name des Dataset muß eindeutig sein.
    * 
    * @param req
    */
   void addRequest(Request req) {
      String datasetname = req.getDatasetName();
      if(this.requests.get(datasetname.toLowerCase()) != null) {
         throw new IllegalArgumentException("PL [ " + this.getLayerName() + " ] " + "Duplicate Dataset Name: " + datasetname);
      }
      else {
         this.requests.put(datasetname.toLowerCase(), req);
      }
   }

   /**
    * Erzeugt einen leeren Request mit dem angegebenen Dataset-Namen.
    * <p>
    * Dieser Name muß je PL eindeutig sein.
    * 
    * @param datasetname
    * @return
    */
   public Request createRequest(String datasetname) {
      Request req = new Request(this, datasetname);
      this.addRequest(req);
      return req;
   }

   /**
    * Liefert den Request zu dem angegebenen Namen.
    * <p>
    * Wirft eine IllegalArgumentException, wenn kein Request unter diesem Namen.
    * 
    * @param datasetName
    * @return
    */
   public Request getRequest(String datasetName) {
      if(datasetName == null || datasetName.length() == 0) {
         throw new IllegalArgumentException("PL [ " + this.getLayerName() + " ] " + "PL#getRequest: Dataset name is null or empty!");
      }

      Request req = this.requests.get(datasetName.toLowerCase());
      if(req == null) {
         throw new IllegalArgumentException("PL [ " + this.getLayerName() + " ] " + "PL#getRequest: Missing Dataset name: '" + datasetName + "'");
      }
      return req;
   }

   /**
    * Löscht einen Request
    * 
    * @param datasetName
    * @return 'true' wenn erfolgreich gelöscht wurde
    */
   boolean removeRequest(String datasetName) {
      Request req = this.requests.remove(datasetName.toLowerCase());
      if(req == null) {
         return false;
      }
      else {
         return true;
      }
   }

   private void initStatements(Elements eles) {
      if(statements == null) {
         statements = new LinkedHashMap<String, NamedStatement>();
      }
      while(eles.hasMoreElements()) {
         Element ele = eles.next();
         String s = ele.getAttribute("databaseType");
         if(s != null) {
            int type = this.getSupportedDatabaseType(s);
            if(type != this.dbType) {
               continue;
            }
         }
         NamedStatement nst = new NamedStatement(ele);
         if(nst.getMaxExecutionTime() == Integer.MAX_VALUE) {
            nst.setMaxExecutionTime(this.getDefaultMaxExecutionTime());
         }
         this.statements.put(nst.getName(), nst);
         logger.debug("init Statement: " + nst.getName());
      }
   }

   public NamedStatement getStatement(String name) {
      NamedStatement nst = this.statements.get(name);
      return nst;
   }

   private void initBatches(Elements eles) {
      if(batches == null) {
         batches = new LinkedHashMap<String, BatchStatement>();
      }
      while(eles.hasMoreElements()) {
         Element ele = eles.next();
         String s = ele.getAttribute("databaseType");
         if(s != null) {
            int type = this.getSupportedDatabaseType(s);
            if(type != this.dbType) {
               continue;
            }
         }
         BatchStatement bst = new BatchStatement(ele);
         this.batches.put(bst.getName(), bst);
         logger.debug("init Batch: " + bst.getName());
      }
   }

   public BatchStatement getBatchStatement(String name) {
      BatchStatement bst = this.batches.get(name);
      return bst;
   }

   private void initSequences(Elements elNamedSequences) {
      if(elNamedSequences == null)
         return;
      int sCnt = 0;
      elNamedSequences.first(); // Wichtig!
      while(elNamedSequences.hasMoreElements()) {
         Element sEle = elNamedSequences.next();
         sCnt++;
         if(sCnt > 1) {
            String seqName = sEle.getAttribute("name");
            String sql = sEle.getElement("get").getTextString();
            NamedSequence ns = new NamedSequence(seqName, sql);
            this.addSequence(ns);
         }
      }
   }

   private void addSequence(NamedSequence ns) {
      if(sequences == null) {
         sequences = new LinkedHashMap<String, NamedSequence>();
      }
      Object o = sequences.put(ns.getName(), ns);
      if(o != null) {
         throw new IllegalArgumentException("PL [ " + this.getLayerName() + " ] Duplicate Named Sequence: " + ns.getName());
      }
   }

   NamedSequence getSequence(String name) throws PLException {
      if(this.sequences == null) {
         throw new PLException("No sequences defined");
      }
      NamedSequence seq = sequences.get(name);
      return seq;
   }

   /**
    * - Alle Caches ermitteln (Request mit cacheEnabled)
    * - Alle View-Definitionen durchlaufen.
    * - Unter remove alle CachedViews eintragen, die eine betroffene Tabelle enthalten.
    * - Zusätzlich alle Caches von NamedStatements, die eine der betroffenen Tabellen enthalten.
    */
   private void initCaches() {
      // 1. Alle definierten caches ermitteln
      ArrayList<Request> caches = new ArrayList<Request>();
      for(Request req : requests.values()) {
         if(req.hasCache()) {
            caches.add(req);
         }
      }
      // 2. Schleife Views
      for(Request req : requests.values()) {
         ArrayList<String> names = req.getAllChildDataTableNames();
         String remove = "";
         // 2.1 Schleife Caches
         for(Request creq : caches) {
            ArrayList<String> cnames = creq.getAllChildDataTableNames();
            for(String tablename : names) {
               if(cnames.contains(tablename)) {
                  remove += creq.getDatasetName() + ",";
                  break;
               }
            }
         }
         // 2.2 Schleife NamedStatements
         for(NamedStatement nst : this.statements.values()) {
            if(nst.cacheConfig != null) {
               ArrayList<String> cnames = nst.cacheConfig.getTablenames();
               for(String tablename : names) {
                  if(cnames.contains(tablename)) {
                     remove += "NamedStatement:" + nst.datasetName + ",";
                     break;
                  }
               }
            }
         }

         // 2.3 Zu löschende Caches im View eintragen 
         if(remove.length() > 0) {
            if(req.getCacheConfig() == null) {
               RequestCacheConfig rcc = new RequestCacheConfig(req.getDatasetName(), null);
               req.setCacheConfig(rcc);
            }
            req.getCacheConfig().remove = remove;
         }
      }
   }

   /**
    * Liefert die Zugriffsdefinitionen aus DatabaseConfig.xml
    * 
    * @return
    */
   public Document getDatabaseConfig() {
      Document doc = new Document();
      doc.setEncoding("UFT-8");
      DocType dt = new DocType("Server");
      dt.setSystemId("PLConfig.dtd");
      doc.addChild(dt);
      Element reqs = new Element("Server");
      doc.setRoot(reqs);

      for(Request req : requests.values()) {
         reqs.addElement(req.getElement());
      }
      return doc;
   }

   /**
    * Der Cache mit den Datenbankabfragen wird gelöscht.
    * 
    * @throws PLException
    */
   public void reset(DatabaseConnection dbConnection) throws PLException {
      this.initDatabase(dbConnection);
      this.resetTimeStamp = new Date();
   }

   /**
    * Liefert eine Liste mit den Namen der definierten Datenbank-Zugriffe.
    * 
    * @return
    */
   public ArrayList<String> getDatasetNames() {
      ArrayList<String> ret = new ArrayList<String>();
      for(String s : requests.keySet()) {
         ret.add(s);
      }
      return ret;
   }

   static int getTransactionIsolationLevel(String sTransactionLevel) {
      int level = -1;
      if(sTransactionLevel != null) {
         if(sTransactionLevel.equalsIgnoreCase("TRANSACTION_NONE")) {
            level = Connection.TRANSACTION_NONE;
         }
         else if(sTransactionLevel.equalsIgnoreCase("TRANSACTION_READ_UNCOMMITTED")) {
            level = Connection.TRANSACTION_READ_UNCOMMITTED;
         }
         else if(sTransactionLevel.equalsIgnoreCase("TRANSACTION_READ_COMMITTED")) {
            level = Connection.TRANSACTION_READ_COMMITTED;
         }
         else if(sTransactionLevel.equalsIgnoreCase("TRANSACTION_REPEATABLE_READ")) {
            level = Connection.TRANSACTION_REPEATABLE_READ;
         }
         else if(sTransactionLevel.equalsIgnoreCase("TRANSACTION_SERIALIZABLE")) {
            level = Connection.TRANSACTION_SERIALIZABLE;
         }
         else {
            logger.error("Illegal TransactionIsolationLevel: " + sTransactionLevel);
            level = -1;
         }
      }
      return level;
   }

   /**
    * Ändert den TransactionIsolationLevel Wirft eine IllegalArgumentEx, wenn
    * ungültiger Level
    * 
    * @see Connection
    */
   public void setTransactionIsolationLevel(int level) {
      if(level != Connection.TRANSACTION_NONE && level != Connection.TRANSACTION_READ_UNCOMMITTED && level != Connection.TRANSACTION_READ_COMMITTED && level != Connection.TRANSACTION_REPEATABLE_READ && level != Connection.TRANSACTION_SERIALIZABLE)
         throw new IllegalArgumentException("Illegal TransactionIsolationLevel");
      this.transactionIsolationLevel = level;
   }

   // ############# METADATA ####################################
   private void initMetaData(DatabaseConnection dbConnection) throws PLException {
      logger.info("PL [ " + databaseName + " ] " + "Retrieving database metadata... ");
      this.tables = new LinkedHashMap<String, JDataTable>();
      try {
         String[] schemata = new String[] {null};
         if(schema != null && schema.length() > 0) {
            schemata = schema.split(",");
         }
         for(int i = 0; i < schemata.length; i++) {
            String curSchema = schemata[i];
            if(this.getDatabaseType() == ORACLE) {
               curSchema = curSchema.toUpperCase(); // Schema bei Oracle immer upcase?
            }
            // 1. DataTables =======================================
            DatabaseMetaData md = dbConnection.getConnection().getMetaData();
            ResultSet rs = md.getTables(catalog, curSchema, null, tbl_types);
            while(rs.next()) {
               final String tblCat = rs.getString("TABLE_CAT");
               final String tblSchema = rs.getString("TABLE_SCHEM");
               final String tblname = rs.getString("TABLE_NAME");
               final String tblType = rs.getString("TABLE_TYPE");
               final String comment = rs.getString("REMARKS");
               JDataTable tbl = new JDataTable(tblname);
               logger.debug("TableName: " + tblname + " Catalog: " + tblCat + " Schema: " + tblSchema);
               tbl.setDatabaseName(this.getDatabaseName());
               tbl.setCatalog(tblCat);
               tbl.setSchema(tblSchema);
               tbl.setType(tblType);
               tbl.setComment(comment);
               try {
                  ResultSet colrs = md.getColumns(catalog, curSchema, tblname, null);
                  tables.put(tbl.getTablename().toLowerCase(), tbl);
                  if(tblSchema != null) {
                     tables.put(tbl.getFullTablename().toLowerCase(), tbl);
                  }
                  logger.debug("initMetaData Table: " + tbl.getTablename());
                  // 1.1 Columns ===========================================
                  while(colrs.next()) {
                     /*
                      * Achtung! Die Reihenfolge der Abarbeitung der Felder ist
                      * wichtig! Da z.B. Oracle sonst einen Fehler ORA-17027 meldet!
                      * Reihenfolge im ResultSet: TABLE_CAT TABLE_SCHEM TABLE_NAME
                      * COLUMN_NAME DATA_TYPE TYPE_NAME COLUMN_SIZE BUFFER_LENGTH
                      * DECIMAL_DIGITS NUM_PREC_RADIX NULLABLE REMARKS COLUMN_DEF
                      * SQL_DATA_TYPE SQL_DATETIME_SUB CHAR_OCTET_LENGTH
                      * ORDINAL_POSITION IS_NULLABLE
                      */
                     // String colTblName = colrs.getString("TABLE_NAME");
                     String colName = colrs.getString("COLUMN_NAME");
                     int type = colrs.getInt("DATA_TYPE");
                     // Size
                     int size = -1;
                     try {
                        size = colrs.getInt("COLUMN_SIZE");
                     }
                     catch(Exception ex) {
                     }
                     // Decimal Digits
                     int digits = 0;
                     try {
                        digits = colrs.getInt("DECIMAL_DIGITS");
                     }
                     catch(Exception ex) {
                     }
                     // Default Value
                     String dv = null;
                     try {
                        dv = colrs.getString("COLUMN_DEF");
                        if(this.dbType == FIREBIRD && dv != null) {
                           if(dv.startsWith("default ")) {
                              dv = dv.substring(8); // Firebird :-(((
                           }
                        }
                        if(dv != null && (dv.startsWith(" ") || dv.endsWith(" "))) {
                           // Für Maxdb neu 17.5.2004 PKÖ
                           dv = dv.trim();
                        }
                     }
                     catch(Exception ex) {
                        logger.warn("PL [ " + databaseName + " ] " + "Warning: Can't get COLUMN_DEF '" + colName + "' (" + ex.getMessage() + ")", ex);
                     }
                     String isNull = colrs.getString("IS_NULLABLE");
                     boolean nullable = false;
                     if(isNull.equals("YES")) {
                        nullable = true;
                     }
                     // Remarks
                     String remarks = colrs.getString("REMARKS");
                     // Create Column
                     JDataColumn col = new JDataColumn(tbl, colName, type, nullable, false);
                     if(size != -1) {
                        col.setSize(size);
                     }
                     // Set Digits
                     col.setDecimalDigits(digits);
                     // Set Default value
                     if(dv != null) {
                        col.setDefaultValue(dv);
                     }
                     // Set Remarks
                     col.setComment(remarks);
                     // Add Column to table
                     try {
                        tbl.addColumn(col);
                     }
                     catch(Exception ex) {
                        logger.error("Error adding Column: " + tbl.getTablename() + "." + col.getColumnName(), ex);
                     }
                  }
                  colrs.close();
               }
               catch(Exception ex) {
                  logger.error(ex.getMessage(), ex);
                  logger.debug("Unable to getColumns: " + tblname);
                  continue;
               }
               // 1.2 PK =============================================
               try {
                  ResultSet prs = md.getPrimaryKeys(catalog, curSchema, tblname);
                  int keySeq = 0;
                  while(prs.next()) {
                     String pkColName = prs.getString("COLUMN_NAME");
                     // logger.debug("PK: "+pk);
                     JDataColumn pkCol = tbl.getDataColumn(pkColName);
                     if(pkCol != null) {
                        pkCol.setPrimaryKey(true);
                        keySeq++; // 1-relativ
                        pkCol.setKeySeq(keySeq);
                     }
                     else {
                        logger.warn("PL [ " + databaseName + " ] " + "Missing Primary Key Column: " + pkColName);
                     }
                  }
                  prs.close();
               }
               catch(SQLException ex) {
                  logger.error("PL [ " + databaseName + " ] " + "Database Metadata getPrimaryKeys: " + ex.getMessage(), ex);
               }
               tbl.setLocked();
            }
            rs.close();
            // Foreign Key References
            if(readMetaDataReferences == true) {
               readMetaDataReferences(md, curSchema);
            }
         } // next Schema
         logger.info("PL [ " + databaseName + " ] " + "Retrieving database metadata completed.");
      }
      catch(Exception ex) {
         String msg = "PL [ " + databaseName + " ] " + "Error retrieving database metadata: " + ex.getMessage();
         logger.error(msg, ex);
         throw new PLException(msg, ex);
      }
   }

   /**
    * Zweite Durchlauf: Childs und Parents
    * 
    * @param md
    * @param tbl_types
    * @throws SQLException
    */
   private void readMetaDataReferences(DatabaseMetaData md, String curSchema) throws SQLException {
      String[] _tbl_types = {"TABLE"};
      // 2. References
      if(isDebug()) {
         logger.debug("MetaDataRefrerences (Foreign Keys)...");
      }
      ResultSet rs = md.getTables(catalog, curSchema, null, _tbl_types);
      while(rs.next()) {
         String tblname = rs.getString("TABLE_NAME");
         if(this.isDebug()) {
            logger.debug(tblname);
         }
         JDataTable tbl = null;
         try {
            tbl = this.getDataTable(tblname);
         }
         catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            logger.debug("Missing DataTable: " + tblname);
            continue;
         }
         // Import: Parent
         {
            try {
               // 2.1 Imported keys ===============================
               ResultSet irs = md.getImportedKeys(catalog, curSchema, tblname);
               // FK-Columns bilden (oh weh!)
               StringBuffer fkColumns = new StringBuffer();
               int fkcnt = 0; // Zähler für pk-Columns
               while(irs.next()) {
                  // String pk = irs.getString("PKCOLUMN_NAME");
                  String pkTable = irs.getString("PKTABLE_NAME");
                  String fk = irs.getString("FKCOLUMN_NAME");
                  fkcnt++;
                  fkColumns.append(fk); // add
                  fkColumns.append(","); // add
                  // String fkTable = irs.getString("FKTABLE_NAME");
                  // int seq = irs.getInt("KEY_SEQ"); // Wird je Table neu
                  // gezählt

                  // int update_rule = irs.getInt("UPDATE_RULE");
                  // int delete_rule = irs.getInt("DELETE_RULE");
                  // logger.debug("Parent:
                  // "+pkTable+"."+pk+"/"+fkTable+"."+fk);
                  // logger.debug(seq);
                  // logger.debug(" "+delete_rule);

                  JDataTable parent = this.getDataTable(pkTable).cloneTable(false); // new:
                  // cloneTable(false)!
                  // /
                  // 8.5.2004 PKÖ
                  int pkcnt = parent.getPKColumnsCount();
                  // so, wenn wir so viele FK-Columns angesammelt
                  // haben, wie unsere Parent Table
                  // Primär-Schlüssel-Felder hat
                  // dann sollten wir eine Referenz erzeugen
                  if(pkcnt == fkcnt) {
                     fkColumns.deleteCharAt(fkColumns.length() - 1); // letztes
                     // Komma
                     // wech
                     String refname = parent.getTablename() + "_" + fkColumns.toString();
                     parent.setRefname(refname); // Das geht schief wenn
                     // nicht ge-clone-t
                     // wird! / 8.5.2004 PKÖ
                     try {
                        tbl.addParentTable(parent, fkColumns.toString());
                     }
                     catch(Exception ex) {
                        logger.error("PL [ " + databaseName + " ] " + "addParentTable: " + ex.getMessage(), ex);
                     }
                     // Alles wieder von vorn...
                     fkColumns = new StringBuffer();
                     fkcnt = 0; // Zähler zurücksetzen
                  }
               }
               irs.close();
            }
            catch(Exception ex) {
               logger.info("PL [ " + databaseName + " ] " + "Database Metadata getImportedKeys: " + ex.getMessage(), ex);
            }
         }
         // 2.2 Exported Keys: Child ==================================
         {
            try {
               ResultSet ers = md.getExportedKeys(catalog, curSchema, tblname);
               // FK-Columns bilden (oh weh!)
               StringBuffer fkColumns = new StringBuffer();
               int fkcnt = 0; // Zähler für pk-Columns
               int pkcnt = tbl.getPKColumnsCount();
               while(ers.next()) {
                  // String pk = ers.getString("PKCOLUMN_NAME");
                  String fkTable = ers.getString("FKTABLE_NAME");
                  String fk = ers.getString("FKCOLUMN_NAME");
                  fkcnt++;
                  fkColumns.append(fk); // add
                  fkColumns.append(","); // add
                  // int seq = ers.getInt("KEY_SEQ");
                  // int update_rule = ers.getInt("UPDATE_RULE");
                  // int delete_rule = ers.getInt("DELETE_RULE");
                  // logger.debug("Child: "+pk+"/"+fkTable+"."+fk);
                  // //##
                  // logger.debug(seq);
                  // logger.debug(" "+rule);
                  JDataTable child = this.getDataTable(fkTable);
                  // so, wenn wir so viele FK-Columns angesammenlt
                  // haben, wie unsere Child Table Primär-Schlüssel-Felder
                  // hat
                  // dann sollten wir eine Referenz erzeugen
                  if(pkcnt == fkcnt) {
                     fkColumns.deleteCharAt(fkColumns.length() - 1); // letztes
                     // Komma
                     // wech
                     String refname = child.getTablename() + "_" + fkColumns.toString();
                     child.setRefname(refname); // ??
                     try {
                        tbl.addChildTable(child, fkColumns.toString());
                     }
                     catch(Exception ex) {
                        logger.error("PL [ " + databaseName + " ] " + "tbl.addChildTable: " + ex.getMessage(), ex);
                     }
                     // Alles wieder von vorn...
                     fkColumns = new StringBuffer();
                     fkcnt = 0; // Zähler zurücksetzen
                  }
               }
               ers.close();
            }
            catch(Exception ex) {
               logger.info("PL [ " + databaseName + " ] " + "Database Metadata getExportedKeys: " + ex.getMessage(), ex);
            }
         }
      }
      rs.close();
   }

   public Element getElement() {
      Element ele = new Element("Database");
      ele.setAttribute("name", this.databaseName);
      // JDBC-Driver
      Element driverEle = ele.addElement("JDBC-Driver");
      driverEle.setText(this.jdbcDriver);
      // URL
      Element urlEle = ele.addElement("URL");
      urlEle.setText(this.databaseURL);
      // Catalog
      if(istLeer(this.catalog) == false) {
         Element catEle = ele.addElement("Catalog");
         catEle.setText(this.catalog);
      }
      // Schema
      if(istLeer(this.schema) == false) {
         Element schemaEle = ele.addElement("Schema");
         schemaEle.setText(this.schema);
      }
      // Username
      Element userEle = ele.addElement("Username");
      userEle.setText(this.username);
      // Password ???
      Element passEle = ele.addElement("Password");
      passEle.setText(this.password);
      // Isolation Level
      if(this.transactionIsolationLevel != -1) {
         Element traEle = ele.addElement("TransactionIsolationLevel");
         traEle.setAttribute("value", Integer.toString(transactionIsolationLevel));
      }
      // Default Sequence
      {
         Element seqEle = ele.addElement("Sequence");
         Element getEle = seqEle.addElement("get");
         getEle.setText(this.getSeq);
         Element setEle = seqEle.addElement("set");
         setEle.setText(this.setSeq);
      }
      // Named Sequences // 11.6.2004 / PKÖ
      if(this.elNamedSequences != null) {
         while(elNamedSequences.hasMoreElements()) {
            Element elNamedSequence = elNamedSequences.next();
            Element seqEle = ele.addElement("Sequence");
            seqEle.setAttribute("name", elNamedSequence.getAttribute("name"));
            Element getEle = seqEle.addElement("get");
            getEle.setText(elNamedSequence.getElement("get").getText());
         }
      }
      // ReadMeta
      if(this.readMetaDataReferences == false) {
         Element rmEle = ele.addElement("ReadMetaDataReferences");
         rmEle.setAttribute("value", "false");
      }
      // Optimistic
      if(istLeer(this.optimisticField) == false) {
         Element optEle = ele.addElement("OptimisticLockingField");
         optEle.setAttribute("value", this.optimisticField);
      }
      // CreateUser
      if(istLeer(this.createUserField) == false) {
         Element cuEle = ele.addElement("CreateUserField");
         cuEle.setAttribute("value", this.createUserField);
      }
      // UpdateUser
      if(istLeer(this.updateUserField) == false) {
         Element upEle = ele.addElement("UpdateUserField");
         upEle.setAttribute("value", this.updateUserField);
      }

      Element elRequests = new Element("Server");
      if(istLeer(getDatasetDefinitionFileName())) {
         for(Request req : requests.values()) {
            elRequests.addElement(req.getElement());
         }
      }

      return ele;
   }

   /**
    * Liefert einen Iterator über alle Tabellen und Views der Datenbank.
    * 
    * @return
    * @see de.pkjs.dataset.JDataTable
    */
   public Iterator<JDataTable> getDataTables() {
      return this.tables.values().iterator(); // tables ist nie null
   }

   /**
    * Liefert eine ArrayList von Strings mit allen Tabellennamen.
    * 
    * @return
    */
   public ArrayList<String> getDataTableNames() {
      ArrayList<String> al = new ArrayList<String>(tables.size());
      Iterator<JDataTable> it = this.getDataTables();
      while(it.hasNext()) {
         JDataTable tbl = it.next();
         al.add(tbl.getTablename());
      }
      return al;
   }

   /**
    * Liefert die Tabellendefinition mit dem angegebenen Tabellennamen.
    * <p>
    * Diese Daten werden per JDBC aus den MetaDaten der Datenbank ermittelt.
    * 
    * @param name
    *           Der Name der gewünschten Tabelle
    * @return Exception, wenn Tabelle fehlt
    */
   public JDataTable getDataTable(String name) {
      if(name == null) {
         throw new IllegalArgumentException("PL [ " + databaseName + " ] getDataTable: name ist null!");
      }
      String lowname = name.toLowerCase();
      if(tables.containsKey(lowname)) {
         return tables.get(lowname);
      }
      else {
         throw new IllegalArgumentException("PL [ " + databaseName + " ] " + "Database '" + this.databaseName + "' missing DataTable '" + name + "'");
      }
   }

   // void addTable(JDataTable tbl) {
   // tables.put(tbl.getTablename().toLowerCase(), tbl);
   // }

   public String getEncoding() {
      return this.encoding;
   }

   /**
    * Liefert die Metadaten zu dieser Datenbank als XML-Document.
    * 
    * @return
    */
   public Document getDatabaseMetaDataDoc() {
      Document doc = new Document();
      doc.setEncoding(this.getEncoding());
      Element root = this.getElement();
      doc.setRoot(root);
      Element tablesEle = root.addElement("DataTables");
      for(Map.Entry<String, JDataTable> entry : tables.entrySet()) { 
         String key = entry.getKey();
         JDataTable tbl = entry.getValue();
         if (key.indexOf(".") == -1) { // Die kommen hier u.U. doppelt an: "MyTable" sowie "MySchema.MyTable"
            tablesEle.addElement(tbl.getElement(false));
         }
      }
      return doc;
   }

   public boolean isDebug() {
      return this.isDebug;
   }

   public boolean isJMX() {
      return this.jmx;
   }

   public String getDatasetDefinitionFileName() {
      return this.datasetDefinitionFileName;
   }

   /**
    * Liefert den Namen dieser Datenbank.
    * 
    * @return
    */
   public String getDatabaseName() {
      return databaseName;
   }

   /**
    * Liefert den Namen des Feldes, über das das optimistische Locking
    * abgewickelt wird.
    * <p>
    * Wenn eine Tabelle keine Column dieses Namens enthält, dann findet kein optimistisches Locking mit dieser Tabelle
    * statt.
    * 
    * @return
    */
   public String getOptimisticField() {
      return optimisticField;
   }

   /**
    * Liefert den Namen des Feldes, in das der Benutzername beim Insert
    * eingetragen werden soll.
    * 
    * @return
    */
   public String getCreateUserField() {
      return createUserField;
   }

   /**
    * Liefert den Namen des Feldes, in das der Benutzername beim Update
    * eingetragen werden soll.
    * 
    * @return
    */
   public String getUpdateUserField() {
      return updateUserField;
   }

   private void setDatabaseType(String url) {
      if(url == null)
         return;
      String s = url.toLowerCase();
      if(s.startsWith("jdbc:mysql")) {
         this.dbType = MYSQL;
      }
      else if(s.startsWith("jdbc:mckoi")) {
         this.dbType = MCKOI;
      }
      else if(s.startsWith("jdbc:odbc")) {
         this.dbType = JDBC_ODBC;
      }
      else if(s.startsWith("jdbc:microsoft:sqlserver")) {
         this.dbType = SQL_SERVER;
      }
      else if(s.startsWith("jdbc:jtds:sqlserver")) {
         this.dbType = SQL_SERVER;
      }
      else if(s.startsWith("jdbc:firebirdsql")) {
         this.dbType = FIREBIRD;
      }
      else if(s.startsWith("jdbc:sapdb")) {
         this.dbType = MAX_DB;
      }
      else if(s.startsWith("jdbc:sybase")) {
         this.dbType = SYBASE;
      }
      else if(s.startsWith("jdbc:oracle")) {
         this.dbType = ORACLE;
      }
      else if(s.startsWith("jdbc:cache")) {
         this.dbType = CACHE;
      }
      else if(s.startsWith("jdbc:db2")) {
         this.dbType = DB2;
      }
      else if(s.startsWith("jdbc:hsqldb")) {
         this.dbType = HSQLDB;
      }
      else if(s.startsWith("jdbc:postgresql")) {
         this.dbType = POSTGRES;
      }
      else if (s.startsWith("jdbc:sap:")) {
      	this.dbType = HANA;
      }
      else if (s.startsWith("jdbc:mariadb:")) {
      	this.dbType = MARIA;
      }
      else {
         logger.warn("PL [ " + databaseName + " ] " + "Database#setDatabaseType Warning: Unknown Database Type: " + url);
      }
   }

   public int getDatabaseType() {
      return this.dbType;
   }

   // Meta
   /**
    * Liefert die DataTable mit den Feldnamen dieser Klasse.
    * 
    * @return
    */
   public static JDataTable getMetaDataTable() {
      if(metaDataTable == null) {
         metaDataTable = new JDataTable("MetaDatabase");
         metaDataTable.setDatabaseName(JDataTable.META_DATABASE_NAME);
         metaDataTable.setTransient(true);
         JDataColumn cName = metaDataTable.addColumn("DatabaseName", Types.VARCHAR);
         cName.setNullable(false);
         cName.setPrimaryKey(true);
         cName.setKeySeq(1);
         metaDataTable.addColumn("DatabaseType", Types.INTEGER);
         metaDataTable.addColumn("JDBC_Driver", Types.VARCHAR);
         metaDataTable.addColumn("URL", Types.VARCHAR);
         metaDataTable.addColumn("Catalog", Types.VARCHAR);
         metaDataTable.addColumn("Schema", Types.VARCHAR);
         metaDataTable.addColumn("Username", Types.VARCHAR);
         metaDataTable.addColumn("Password", Types.VARCHAR);
         metaDataTable.addColumn("IsolationLevel", Types.VARCHAR);
         metaDataTable.addColumn("getSequence", Types.VARCHAR);
         metaDataTable.addColumn("setSequence", Types.VARCHAR);
         JDataColumn cReadRef = metaDataTable.addColumn("ReadMetaDataRef", Types.TINYINT); // Boolean
         cReadRef.setDefaultValue("0");
         JDataColumn cBoolInt = metaDataTable.addColumn("BooleanToInt", Types.TINYINT); // Boolean
         cBoolInt.setDefaultValue("0");
         metaDataTable.addColumn("OptimisticLocking", Types.VARCHAR);
         metaDataTable.addColumn("CreateUser", Types.VARCHAR);
         metaDataTable.addColumn("UpdateUser", Types.VARCHAR);
         // FK
         JDataColumn cFK = metaDataTable.addColumn("DatasetDefinitionFile", Types.VARCHAR);
         cFK.setForeignKey(true);
         // Enabled
         JDataColumn cEn = metaDataTable.addColumn("enabled", Types.TINYINT);
         cEn.setDefaultValue("0");
         // Connection Pool
         metaDataTable.addColumn("MaxActiveConnections", Types.INTEGER);
         metaDataTable.addColumn("MaxIdleConnections", Types.INTEGER);
         metaDataTable.addColumn("MinIdleConnections", Types.INTEGER);
         metaDataTable.addColumn("ConnectionTimeOut", Types.BIGINT);
         // Named Sequences
         {
            JDataTable nsTbl = new JDataTable("NamedSequence");
            nsTbl.setTransient(true);
            JDataColumn colName = nsTbl.addColumn("Name", Types.VARCHAR);
            colName.setPrimaryKey(true);
            colName.setNullable(false);
            nsTbl.addColumn("SQL", Types.VARCHAR);
            nsTbl.addColumn("DatabaseName", Types.VARCHAR);

            metaDataTable.addChildTable(nsTbl, "DatabaseName");
         }
         // DataTables
         {
            JDataTable dTbl = JDataTable.getMetaDataTable();
            dTbl.setTransient(true);
            metaDataTable.addChildTable(dTbl, "FK_DatabaseName");
            // DataColumns
            JDataTable cTbl = JDataColumn.getMetaDataTable();
            cTbl.setTransient(true);
            dTbl.addChildTable(cTbl, "TableName");
         }
      }
      return metaDataTable;
   }

   /**
    * Liefert die Parameter dieser Datenbank als DataRow
    * 
    * @return
    */
   public JDataRow getMetaDataRow() {
      JDataRow row = getMetaDataTable().createNewRow();
      row.setValue("DatabaseName", this.databaseName);
      row.setValue("DatabaseType", this.dbType);
      row.setValue("JDBC_Driver", this.jdbcDriver);
      row.setValue("URL", this.databaseURL);
      row.setValue("Catalog", this.catalog);
      row.setValue("Schema", this.schema);
      row.setValue("Username", this.username);
      row.setValue("Password", this.password);
      row.setValue("IsolationLevel", Integer.toString(this.transactionIsolationLevel));
      row.setValue("getSequence", this.getSeq);
      row.setValue("setSequence", this.setSeq);
      row.setValue("ReadMetaDataRef", this.readMetaDataReferences);
      row.setValue("OptimisticLocking", this.optimisticField);
      row.setValue("CreateUser", this.createUserField);
      row.setValue("UpdateUser", this.updateUserField);
      row.setValue("DatasetDefinitionFile", this.getDatasetDefinitionFileName());
      // Connection Pool
      row.setValue("MaxActiveConnections", this.getMaxActive());
      row.setValue("MaxIdleConnections", this.getMaxIdle());
      row.setValue("MinIdleConnections", this.getMinIdle());
      row.setValue("ConnectionTimeOut", this.getConnectionTimeOut());

      // Named Sequences
      {
         if(this.elNamedSequences != null) {
            while(elNamedSequences.hasMoreElements()) {
               Element elNamedSequence = elNamedSequences.next();
               JDataTable nsTable = getMetaDataTable().getChildTable("NamedSequence");
               JDataRow nsRow = nsTable.createNewRow();
               row.addChildRow(nsRow);
               nsRow.setValue("Name", elNamedSequence.getAttribute("name"));
               nsRow.setValue("SQL", elNamedSequence.getElement("get").getTextString());
            }
         }
      }
      // DataTables
      {
         JDataTable xTbl = getMetaDataTable();
         JDataTable tblTbl = xTbl.getChildTable("MetaDataTable");
         for(Iterator<JDataTable> it = this.getDataTables(); it.hasNext();) {
            JDataTable tbl = it.next();
            JDataRow tblRow = tblTbl.createNewRow();
            tbl.getMetaDataRow(tblRow);
            row.addChildRow(tblRow);
            // Columns
            Iterator<JDataColumn> itCol = tbl.getDataColumns();
            if(itCol != null) {
               while(itCol.hasNext()) {
                  JDataColumn col = itCol.next();
                  JDataRow colRow = col.getMetaDataRow();
                  tblRow.addChildRow(colRow);
               }
            }
            else {
               // keine Columns?
            }
         }
      }
      return row;
   }

   private int getSupportedDatabaseType(String _dbType) {
      _dbType = _dbType.toUpperCase();
      if(databaseTypes.containsKey(_dbType)) {
         Integer i = databaseTypes.get(_dbType);
         return i.intValue();
      }
      else
         return UNKNOWN;
   }

   private static boolean istLeer(String s) {
      if(s == null || s.length() == 0) {
         return true;
      }
      else {
         return false;
      }
   }

   /**
    * Liefert die URL über die der JDBC-Trieber auf die Datenbank zugreift.
    * <p>
    * Beispiel: "jdbc:sapdb://myServerName/myDatabaseName"
    * 
    * @return
    */
   public String getDatabaseURL() {
      return databaseURL;
   }

   /**
    * Liefert die Klassennamen des JDBC-Treibers.
    * <p>
    * Beispiel: "com.sap.dbtech.jdbc.DriverSapDB"
    * 
    * @return
    */
   public String getJDBCDriver() {
      return this.jdbcDriver;
   }

   /**
    * @return Returns the layerName.
    */
   public String getLayerName() {
      return this.databaseName;
   }

   /**
    * @return Returns the password.
    */
   public String getPassword() {
      return this.password;
   }

   /**
    * @return Returns the transactionLevel.
    */
   public int getTransactionIsolationLevel() {
      return this.transactionIsolationLevel;
   }

   /**
    * Liefert das Statement, welches zum anpingen der Datenbank verwendet werden
    * soll; z.B.: SELECT 1 FROM DUAL (Oracle, MAXDB); SELECT 1 AS Test (Postgres)
    * ValidationQuery
    * 
    * @return
    */
   public String getValidationQuery() {
      if(this.validationQuery == null) {
         switch(this.dbType) {
            default: // MAXDB, Oracle OK
               validationQuery = "SELECT 1 FROM DUAL";
               break;
            case POSTGRES:
               validationQuery = "SELECT 1 AS Test";
               break;
            case MYSQL:
            case SQL_SERVER:
               validationQuery = "SELECT 1";
               break;
         }
      }
      return this.validationQuery;
   }

   /**
    * Dieses Statement wird beim Erzeugen ienr Connection zuerst ausgeführt
    * Siehe Tomcat-Property initSQL
    */
   public String getInitSQL() {
      return this.initSQL;
   }

   public String getUsername() {
      return this.username;
   }

   public boolean isAutocommit() {
      return false;
   }

   public PoolProperties getPoolConfig() {
      return this.pp;
   }

   public int getMaxActive() {
      return pp.getMaxActive();
   }

   public int getMinIdle() {
      return pp.getMinIdle();
   }

   public int getMaxIdle() {
      return pp.getMaxIdle();
   }

   public long getConnectionTimeOut() {
      return this.pp.getMaxWait();
   }

   public String getGetSequence() {
      return this.getSeq;
   }

   public String getSetSequence() {
      return this.setSeq;
   }

   public Elements getSequences() {
      return this.elNamedSequences;
   }

   /**
    * Element-Name für Root-Element des Ergebnis-Dokuments. Aus PLConfig.xml
    * (Dataset/ElementName).
    * 
    * @return
    */
   public String getDatasetElementName() {
      return this.datasetElementName;
   }

   public SimpleDateFormat getDateFormat() {
      return this.dateFormat;
   }

   public SimpleDateFormat getTimeFormat() {
      return this.timeFormat;
   }

   public SimpleDateFormat getTimestampFormat() {
      return this.timestampFormat;
   }

   /**
    * Liefert das heutige Datum im gewählten Format (dd.MM.yyyy).
    * 
    * @see #getDateFormat
    */
   public String getTodayString() {
      String s = getDateFormat().format(new java.util.Date());
      return s;
   }

   /**
    * Liefert die aktuelle Uhrzeit im gewähltenFormat (HH:mm).
    * 
    * @see #getTimeFormat
    */
   public String getNowString() {
      String s = getTimeFormat().format(new java.util.Date());
      return s;
   }

   public String getTodayNowString() {
      String s = getTimestampFormat().format(new java.util.Date());
      return s;
   }

   public void setDebug(boolean state) {
      this.isDebug = state;
   }

   public DecimalFormat getDecimalFormat() {
      return this.decimalFormat;
   }

   // MBeans #######################################################
   public boolean hasDefaultGetOidStatement() {
      if(this.getSeq == null) {
         return false;
      }
      else {
         return true;
      }
   }

   public boolean hasDefaultSetOidStatement() {
      if(this.setSeq == null) {
         return false;
      }
      else {
         return true;
      }
   }

   public int getNumberOfNamedSequences() {
      if(this.elNamedSequences == null) {
         return 0;
      }
      else {
         return this.elNamedSequences.size();
      }
   }

   public int getNumberOfNamedStatements() {
      return this.statements.size();
   }

   public int getNumberOfRequests() {
      return requests.size();
   }

   public int getNumberOfTables() {
      return tables.size();
   }

   public Date getCreatedTimeStamp() {
      return this.createdTimeStamp;
   }

   public Date getResetTimeStamp() {
      return this.resetTimeStamp;
   }

   public String getUpdateJournalDirectory() {
      return updateJournalDirectory;
   }

   void setUpdateJournalDirectory(String updateJournalDirectory) {
      this.updateJournalDirectory = updateJournalDirectory;
      this.dbJournal = new DatabaseJournal(this);
   }

   boolean hasJournal() {
      return getDatabaseJournal() != null;
   }

   DatabaseJournal getDatabaseJournal() {
      return dbJournal;
   }

   /**
    * @return null, wenn kein Logger definiert
    */
   Logger getSlowQueryLogger() {
      return this.slowQueryLogger;
   }

   int getDefaultMaxExecutionTime() {
      return defaultMaxExcutionTime;
   }

   /**
    * true, wenn die Datenbank über die Syntax "... LIMIT #" verfügt
    * 
    * @return
    */
   boolean hasLimit() {
      switch(this.dbType) {
         case MAX_DB:
         case POSTGRES:
         case MYSQL:
            return true;
      }
      return false;
   }

   public Set<String> getTablenames() {
      Set<String> al = new HashSet<String>();
      for(Request req : requests.values()) {
         req.getTablenames(al);
      }
      return al;
   }

   public Set<String> getUnusedTablenames() {
      Set<String> all = new HashSet<String>(this.tables.size());
      for(JDataTable tbl : tables.values()) {
         all.add(tbl.getTablename().toLowerCase());
      }
      Set<String> used = this.getTablenames();
      for(String tname : used) {
         all.remove(tname.toLowerCase());
      }
      for(NamedStatement st : statements.values()) {
         String tname = st.getTableName();
         if(tname != null) {
            all.remove(tname.toLowerCase());
         }
         else {
            // tname null??
            logger.warn("Tablename null? " + st);
         }
      }
      for(String tname : all) {
         logger.warn("Unused DataTable: " + tname);
      }
      return all;
   }

   static {
      databaseTypes.put("JDBC_ODBC", new Integer(JDBC_ODBC));
      databaseTypes.put("MYSQL", new Integer(MYSQL));
      databaseTypes.put("FIREBIRD", new Integer(FIREBIRD));
      databaseTypes.put("SQL_SERVER", new Integer(SQL_SERVER));
      databaseTypes.put("MCKOI", new Integer(MCKOI));
      databaseTypes.put("MAXDB", new Integer(MAX_DB));
      databaseTypes.put("SYBASE", new Integer(SYBASE));
      databaseTypes.put("ORACLE", new Integer(ORACLE));
      databaseTypes.put("CACHE", new Integer(CACHE));
      databaseTypes.put("DB2", new Integer(DB2));
      databaseTypes.put("AXION", new Integer(AXION));
      databaseTypes.put("HSQLDB", new Integer(HSQLDB));
      databaseTypes.put("POSTGRES", new Integer(POSTGRES));
      databaseTypes.put("HANA", new Integer(HANA));
      databaseTypes.put("MARIADB", new Integer(MARIA));
   }

   public static final class NamedStatement {
      private String name;
      private String datasetName;
      private String tableName;
      private int maxRows = Integer.MAX_VALUE; // default
      private int queryTimeout = Integer.MAX_VALUE; // default
      private int isolationLevel = -1;
      RequestCacheConfig cacheConfig;

      RequestCacheConfig getCacheConfig() {
         return cacheConfig;
      }

      boolean hasCache() {
         return cacheConfig != null && cacheConfig.isEnabled() == true;
      }

      private String sql;
      /**
       * Wenn die Ausführungszeit länger dauert, dann Warning im Logfile
       */
      private int maxExecutionTime = Integer.MAX_VALUE;
      private ArrayList<String> parameterNames;

      NamedStatement(Element ele) {
         this.name = ele.getAttribute("name");
         String dName = ele.getAttribute("datasetname");
         if(dName != null) {
            this.datasetName = dName;
         }
         else {
            this.datasetName = name;
         }
         String tName = ele.getAttribute("tablename");
         if(tName != null) {
            this.tableName = tName;
         }
         else {
            this.tableName = name;
         }
         String sMax = ele.getAttribute("maxRows");
         if(sMax != null) {
            this.maxRows = Convert.toInt(sMax);
         }
         String sTimeout = ele.getAttribute("queryTimeout");
         if(sTimeout != null) {
            this.queryTimeout = Convert.toInt(sTimeout);
         }
         String sMaxTime = ele.getAttribute("maxExecutionTime");
         if(sMaxTime != null) {
            this.maxExecutionTime = Convert.toInt(sMaxTime);
         }
         else {
            // TODO
         }
         this.sql = ele.getTextString();
         Node cnode = ele.getFirstChild();
         if(cnode instanceof Comment) {
            this.sql = ((Comment)cnode).getString();
         }
         if(sql == null) {
            logger.warn("Statement is null: " + name);
         } else {
         	this.createParameterNames(sql);
         }
         // TransactionIsolationLevel
         Element isoEle = ele.getElement("isolationLevel");
         if(isoEle != null) {
            String sTransactionLevel = isoEle.getAttribute("value");
            this.isolationLevel = Database.getTransactionIsolationLevel(sTransactionLevel);
         }
         // Cache
         Element cele = ele.getElement("Cache");
         if(cele != null) {
            cacheConfig = new RequestCacheConfig("NamedStatement:" + this.name, cele);
         }
      }

      NamedStatement(String name, String sql) {
         this.name = name;
         this.datasetName = name;
         this.tableName = name;
         this.sql = sql;
         this.createParameterNames(sql);
      }

      public String getName() {
         return this.name;
      }

      public String getSql() {
         return this.sql;
      }

      //    public String getSql(ParameterList parameters) {
      //       //Wenn Parameter vom Typ List, dann ? expandieren zu ?,?,...[size]
      //       StringBuilder buff = new StringBuilder();
      //       StringTokenizer toks = new StringTokenizer(sql, "?", true);
      //       int cnt = 0;
      //       Iterator<NVPair> it = parameters.iterator();
      //       while (toks.hasMoreTokens()) {
      //          String tok = toks.nextToken();
      //          buff.append(tok);
      //          if ("?".equals(tok)) {             
      //             NVPair nv = it.next();
      //             if (nv.getValue() instanceof List<?>) {
      //                List<?> l = (List<?>)nv.getValue();
      //                if (l.size() > 1) {
      //                   for (int i = 1; i < l.size(); i++) {
      //                      buff.append(",?");
      //                   }
      //                }
      //             }
      //             cnt++;
      //          }
      //       }
      //       
      //      return buff.toString();
      //    }

      /**
       * Durchsucht das Statement nach Parametern in der Form: "... WHERE id =
       * $MyId AND ..."
       * 
       * @param sql
       */
      private void createParameterNames(String _sql) {
         StringTokenizer toks = new StringTokenizer(_sql, " (),=\n\t\r");
         while(toks.hasMoreTokens()) {
            String tok = toks.nextToken();
            if(tok.startsWith("$") && tok.length() > 1) {
               if(this.parameterNames == null) {
                  this.parameterNames = new ArrayList<String>();
               }
               this.parameterNames.add(tok.substring(1));
            }
         }
      }

      /**
       * Liefert eine ArrayList mit dem in diesem Statement verwendeten Parametern
       * in der Form: "... WHERE id = $MyId AND ..." oder null, wenn keine
       * Parameter definiert.
       * 
       * @return
       */
      public ArrayList<String> getParameterNames() {
         return this.parameterNames;
      }

      /**
       * Entnimmt aus der übergebenen ParameterList all die benannten Parameter,
       * die in diesem Statement definiert sind.
       * 
       * @param list
       *           Die ParameterList, aus der die Parameter entnommen werden
       *           sollen.
       * @return Null, wenn dieses Statement keine Parameter hat. Wirft eine
       *         IllegalArgumentException, wenn in der übergebenen Liste Parameter
       *         fehlen, die für dieses Statement benötigt werden.
       */
      public ParameterList extractParameters(ParameterList list) {
         if(this.parameterNames == null || this.parameterNames.size() == 0) {
            return null;
         }
         ParameterList ret = new ParameterList();
         int size = this.parameterNames.size();
         for(int i = 0; i < size; i++) {
            String paraName = this.parameterNames.get(i);
            NVPair pair = list.getParameter(paraName);
            ret.addParameter(paraName, pair.getValue());
         }
         return ret;
      }

      public String toString() {
         return "Name: " + this.name + "\n" + "Statement: " + this.sql;
      }

      /**
       * @return Returns the maxRows.
       */
      public int getMaxRows() {
         return maxRows;
      }

      /**
       * @param maxRows
       *           The maxRows to set.
       */
      public void setMaxRows(int maxRows) {
         this.maxRows = maxRows;
      }

      public int getMaxExecutionTime() {
         return maxExecutionTime;
      }

      /**
       * Wenn die Ausführungszeit länger dauert, dann Warning im Logfile
       */
      public void setMaxExecutionTime(int millies) {
         this.maxExecutionTime = millies;
      }

      /**
       * @return Returns the datasetName.
       */
      public String getDatasetName() {
         return datasetName;
      }

      /**
       * Der Name des Dataset wird auf diesen Wert gesetzt.
       * 
       * @param datasetName
       */
      public void setDatasetName(String datasetName) {
         this.datasetName = datasetName;
      }

      /**
       * @return Returns the tableName.
       */
      public String getTableName() {
         return tableName;
      }

      /**
       * @param tableName
       */
      public void setTableName(String tableName) {
         this.tableName = tableName;
      }

      public int getQueryTimeout() {
         return queryTimeout;
      }

      public void setQueryTimeout(int queryTimeout) {
         this.queryTimeout = queryTimeout;
      }

      int getIsolationLevel() {
         return isolationLevel;
      }

      void setIsolationLevel(int isolationLevel) {
         this.isolationLevel = isolationLevel;
      }
   }

   static final class BatchStatement {
      private String name;
      private String datasetName;
      private ArrayList<NamedStatement> statements = new ArrayList<NamedStatement>();

      BatchStatement(Element ele) {
         this.name = ele.getAttribute("name");
         String dName = ele.getAttribute("datasetname");
         if(dName != null) {
            this.datasetName = dName;
         }
         else {
            this.datasetName = name;
         }
         Elements eles = ele.getElements("Statement");
         while(eles.hasMoreElements()) {
            Element stmtEle = eles.next();
            NamedStatement stmt = new NamedStatement(stmtEle);
            this.addStatement(stmt);
         }
      }

      BatchStatement(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public void addStatement(NamedStatement stmt) {
         statements.add(stmt);
      }

      public ArrayList<NamedStatement> getStatements() {
         return this.statements;
      }

      @Override
      public String toString() {
         StringBuilder buff = new StringBuilder();
         buff.append("Name: " + this.name + "\n" + "Statements: \n");
         int size = statements.size();
         for(int i = 0; i < size; i++) {
            NamedStatement stmt = statements.get(i);
            buff.append(stmt.toString());
         }
         return buff.toString();
      }

      /**
       * @return Returns the datasetName.
       */
      public String getDatasetName() {
         return datasetName;
      }

      /**
       * Der Name des Dataset wird auf diesen Wert gesetzt.
       * <p>
       * 
       * @param datasetName
       *           The datasetName to set.
       */
      public void setDatasetName(String datasetName) {
         this.datasetName = datasetName;
      }
   }
   /**
    * Inner Class f&uuml;r NamedSequences.
    * 
    * @author pkoeker / 11.6.2004
    */
   class NamedSequence {
      private String name;
      private String sql;

      /**
       * Erzeugt eine neue Sequence unter dem angegebenen Namen und mit dem
       * angegebenen SQL-Statement.
       * 
       * @param name
       * @param sql
       * @throws SQLException
       * @throws PLException
       */
      NamedSequence(String name, String sql) {
         this.name = name;
         this.sql = sql;
      }

      String getName() {
         return this.name;
      }

      /**
       * Liefert eine Oid zu der benannten Sequence.
       * <p>
       * Named Sequences werden in PLConfig.xml definiert.
       * 
       * @return oid
       */
      long getNextSequence(Connection con) throws PLException {
         try {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            long oid = rs.getLong(1);
            rs.close();
            return oid;
         }
         catch(SQLException ex) {
            logger.error("PL [ " + getLayerName() + " ] " + "Unable to init sequence: " + this.name, ex);
            throw new PLException("PL [ " + getLayerName() + " ] " + "Unable to init sequence: " + this.name, ex);
         }
      }
   }

}
