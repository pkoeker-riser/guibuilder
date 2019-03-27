package de.pkjs.pl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.ParameterList;
import de.pkjs.pl.Database.NamedStatement;
import electric.xml.Document;

/**
 * @author ikunin
 */
public interface IPLContext {
   /**
    * Liefert die Datenbank
    * @return
    */
	public Database getCurrentDatabase();

	/**
	 * Liefert den Namen der verwendeten Datenbank.
	 * 
	 * @return String
	 */
	public String getCurrentDatabaseName();

	/**
	 * Liefert einen Dataset mit dem angegebenen Namen und dem angegebenen
	 * Primärschlüssel der Wurzeltabelle.
	 * 
	 * @param datasetname
	 * @param oid
	 * @return
	 * @throws PLException
	 */
	public JDataSet getDataset(String datasetname, long oid) throws PLException;
  /**
   * Liefert einen Dataset mit den angegebenen Primärschlüsseln.<br>
   * TODO: Wenn kein ORDER BY definiert hängt die Reihenfolge der Rows von der Implmentierung der Datenbank ab.
   * @param datasetname
   * @param oids
   * @return
   * @throws PLException
   */
	public JDataSet getDataset(String datasetname, long[] oids) throws PLException;
	/**
	 * Liefert einen Dataset mit dem angegebenen Namen und dem angegebenen
	 * Primärschlüssel der Wurzeltabelle.
	 * @param datasetname
	 * @param key
	 * @return
	 * @throws PLException
	 */
	public JDataSet getDataset(String datasetname, String key) throws PLException;

	public String getDatasetString(String datasetname, long oid)
			throws PLException;

	/**
	 * Liefert einen Dataset mit dem angegebenen Namen sowie unter Angabe eine
	 * Liste von Parametern.
	 * 
	 * @param datasetname
	 * @param parameters
	 * @return
	 * @throws PLException
	 */
	public JDataSet getDataset(String datasetname, ParameterList parameters)
			throws PLException;

	/**
	 * Schreibt eine Liste von DataSets in einer Transaktion zurück
	 * @param datasets
	 * @return
	 * @throws PLException
	 */
	public int setDataset(List<JDataSet> datasets) throws PLException;
	/**
	 * Der vom Client geänderter Dataset wird in die Datenbank
	 * zurückgeschrieben.
	 * 
	 * @param values
	 *            Ein Dataset
	 */
	public int setDataset(JDataSet dataset) throws PLException;
  
	public void setDatasetAsync(JDataSet dataset) throws PLException;

	public int setDataset(String dataset) throws PLException;
	
	/**
	 * Der angegebene Cache wird geleert
	 * @param JDataSet
	 * @throws PLException
	 */
	public void clearCache(String datasetname) throws PLException;

	/**
	 * Liefert einen Dataset ohne Angabe eines Schlüssels; also vor allem eine
	 * Menge von Datensätzen - bis zum Inhalt einer kompletten Tabelle.
	 * 
	 * @param datasetname
	 * @return String
	 * @throws PLException
	 */
	public JDataSet getAll(String datasetname) throws PLException;

	/**
	 * Liefert einen flachen Dataset auf Basis eines beliebigen SQL-Statements.
	 * 
	 * @param tablename
	 *            Tabelle zu den Columns
	 * @param columns
	 *            Spaltennamen mit Komma getrennt
	 * @param from
	 *            beliebige Bedingung
	 * @return
	 * @throws PLException
	 */
	public JDataSet getDatasetSql(String tablename, String columns, String from)
			throws PLException;

	/**
	 * Liefert einen flachen Dataset auf Basis eines beliebigen SQL-Statements.
	 * <p>
	 * Achtung! <br>
	 * Bei Sybase müssen die angebenen Column-Namen eindeutig sein!
	 * 
	 * @param datasetname ( =
	 *            TableName)
	 * @param sql
	 *            "SELECT a,b FROM c,d WHERE ... ORDER BY ...
	 * @return
	 * @throws PLException
	 */
	public JDataSet getDatasetSql(String datasetname, String sql)
			throws PLException;

	/**
	 * 
	 * @param datasetname
	 * @param sql
	 * @param limit
	 *            Maximale Größe des ResultSet vordefinieren
	 * @return
	 * @throws PLException
	 */
	public JDataSet getDatasetSql(String datasetname, String sql, int limit)
			throws PLException;

	/**
	 * Liefert einen flachen Dataset auf Basis eines beliebigen SQL-Statements.
	 * <p>
	 * Achtung! <br>
	 * Bei Sybase müssen die angebenen Column-Namen eindeutig sein!
	 * 
	 * @param datasetname ( =
	 *            TableName)
	 * @param sql
	 * @param list
	 * @return
	 * @throws PLException
	 */
	public JDataSet getDatasetSql(String datasetname, String sql,
			ParameterList list) throws PLException;

	/**
	 * Liefert ein benanntes Statement aus PLConfix.xml
	 * @param name
	 * @return
	 */
   public Database.NamedStatement getNamedStatement(String name);
   /**
    * Liefert einen DataSet unter Angabe eines benannten Statements.<p>
    * Hinweis:<br>
    * Das NameStatement kann auch aus einem abweichenden PL stammen
    * @param nst
    * @param list
    * @return
    * @throws PLException
    */
   public JDataSet getDatasetStatement(NamedStatement nst, ParameterList list) throws PLException;
	/**
	 * Liefert einen DataSet unter Angabe des Namens eines benannten Statements. 
	 * @param name
	 * @return
	 * @throws PLException
	 */
   public JDataSet getDatasetStatement(String name) throws PLException;
   /**
    * Liefert einen DataSet unter Angabe des Namens eines benannten Statements.
    * @param name
    * @param list Argumentliste für dieses Statement
    * @return
    * @throws PLException
    */
   public JDataSet getDatasetStatement(String name, ParameterList list) throws PLException;
   /**
    * @see #getDataRowIterator(String, String, ParameterList)
    * @param statementName Der Name eines benannten Statements
    * @param list ParameterList oder null, wenn ohne Parameter
    * @return
    * @throws PLException
    */
   public DataRowIterator getDataRowIteratorStatement(String statementName,
           ParameterList list) throws PLException;
	/**
	 * Während 'normalerweise' ein DataSet vom Persistenz-Layer immer fertig
	 * aufbereitet und komplett mit allen Daten übergeben wird, ist dieses
	 * Vorgehen bei großen Datenmengen nicht angebracht, nicht nur, daß das zu
	 * lange dauert, sondern weil auch ein OutOfMemory droht.
	 * <p>
	 * Der RowIterator schafft dort Abhilfe. <br>
	 * Es wird ein DataRowIterator geliefert unter Angabe von:
	 * 
	 * @param datasetName
	 *            Namens des DataSet für den RowIterator
	 * @param tablename Name der RootTable des Dataset
	 * @param sql
	 *            SELECT-Statement
	 * @param list
	 *            ParameterListe (darf auch null sein)
	 * @return Den RowIterator mit den gewünschten Daten.
	 * @throws PLException
	 */
	public DataRowIterator getDataRowIterator(String datasetName, String tablename, String sql,
			ParameterList list) throws PLException;

	/**
	 * Liefert einen Dataset ohne Werte aber mit den Metadaten. Wird bei der
	 * Erstellung eines neuen Datensatzes benötigt.
	 * 
	 * @param datasetname
	 *            Der gewünschte Dataset
	 * @return JDataSet
	 * @throws PLException
	 */
	public JDataSet getEmptyDataset(String datasetname);

	/**
	 * Liefert einen neuen eindeutigen Schlüssel für den Client.
	 * 
	 * @return long
	 * @throws PLException
	 */
	public long getOID() throws PLException;

	/**
	 * Liefert einen neuen eindeutigen Key aus der angegebenen Sequence. Wirft
	 * eine Exception, wenn Sequence fehlt.
	 * 
	 * @param sequenceName
	 * @return
	 * @throws PLException
	 */
	public long getOID(String sequenceName) throws PLException;

	/**
	 * Führt ein beliebiges SQL-Command aus (Update, Insert, Delete). Um
	 * beliebige Daten einzulesen siehe:
	 * 
	 * @see #getDatasetSql
	 * @param sqlCommand
	 * @return Anzahl geänderter Datensätze oder 0
	 * @throws PLException
	 */
	public int executeSql(String sqlCommand) throws PLException;

	/**
	 * Führt ein beliebiges SQL-Command aus (Update, Insert, Delete). Um
	 * beliebige Daten einzulesen siehe:
	 * 
	 * @see #getDatasetSql
	 * @param sqlCommand
	 *            SQL-Befehl
	 * @param parameters
	 *            Parameter für PreparedStatement
	 * @return Anzahl geänderter Datensätze oder 0
	 * @throws PLException
	 */
	public int executeSql(String sqlCommand, ParameterList parameters)
			throws PLException;

	/**
	 * Führt ein beliebiges SQL-Statement aus, wobei die Parameter für dieses
	 * Statement in einem Vector übergeben werden.
	 * <p>
	 * Beispiel: <br>
	 * <code><pre>
	 * String sql = &quot;INSERT INTO MyTable (DateCreated) VALUES (?)&quot;;
	 * Vector v = new Vector();
	 * v.add(new Date());
	 * int cnt = pl.executeSqlPara(sql, v);
	 * </pre></code>
	 * 
	 * @param sqlCommand
	 * @param parameter
	 * @return Anzahl geänderter Datensätze oder 0
	 * @throws PLException
	 */
	public int executeSqlPara(String sqlCommand, Vector<Object> parameter)
			throws PLException;
   /**
    * Führt ein benanntes Statement (UPDATE, INSERT, DELETE) aus.
    * @param name Statement-Name
    * @return Anzahl geänderter Datensätze oder 0
    */
   public int executeStatement(String name) throws PLException;
   /**
    * Führt ein benanntes Statement (UPDATE, INSERT, DELETE) aus.
    * @param name name Statement-Name
    * @param parameters Paramter für das Statement
    * @return Anzahl geänderter Datensätze oder 0
    */
   public int executeStatement(String name, ParameterList parameters) throws PLException;
   /**
    * Führt ein Batch-Statement aus.<p>
    * Batch-Statements sind eine Menge von NamedStatements, die nacheinander als
    * Batch ausgeführt werden.
    * @param name
    * @return Anzahl geänderter Datensätze oder 0
    * @throws PLException
    */
   public int executeBatchStatement(String name) throws PLException;
   /**
    * Führt ein Batch-Statement aus.<p>
    * Batch-Statements sind eine Menge von NamedStatements, die nacheinander als
    * Batch ausgeführt werden.
    * @param name
    * @param list Eine Liste von Parametern für die Menge aller Parameter, die
    * für die verschiedenen Statements insgesamt benötigt werden.
    * @return Anzahl geänderter Datensätze oder 0
    * @throws PLException
    */
   public int executeBatchStatement(String name, ParameterList list) throws PLException;

	/**
	 * Die Connection zur Datenbank wird geschlossen.
	 * @throws PLException
	 * @deprecated
	 */
	public void shutdown() throws PLException;

	/**
	 * Liefert einen Request zu dem angegebenen Namen. Ist der Request noch
	 * nicht vorhanden, wird er erzeugt und in den Cache von Requests
	 * geschrieben.
	 * 
	 * @param datasetName
	 * @return
	 */
	public Request getRequest(String datasetName);

	/**
	 * Liefert die MetaDaten der CurrentDatabase.
	 * 
	 * @return
	 */
	public String getDatabaseMetaData();

	public Document getDatabaseMetaDataDoc();
	public JDataSet getMetaDataSet();
  public Document getPLMetaDataDoc();

	/**
	 * Liefert alle Informationen zum Persistenz-Layer als XML-Document.
	 * 
	 * @return ein XML-Dokument als String
	 */
	public String getPLMetaData();
   
	/**
    * Erzeugt einen Request aus Metadaten
    * @see Request#getMetaDataRow()
    * @param metadataRow
    * @return
    */	
   public Request addRequest(JDataRow metaDataRow) throws PLException;

	/**
	 * Liefert die Menge der definierten Datenbank-Zugriffe.
	 * 
	 * @return
	 */
	public ArrayList<String> getDatasetNames();

	/**
	 * Führt das "Ping"-Statement der Datenbank aus, 
	 * wie in PLConfig unter <ValidationQuery/> definiert.
	 * @return Liefert "pong" wenn alles OK, "No PingStatement defined" wenn
	 * in PLConfig keine PingStatement definiert ist.
	 * @throws PLException
	 */
	public String pingDatabase() throws PLException;

	/**
	 * Liefert das DateFormat aus PLConfig.xml
	 * 
	 * @return
	 */
	public SimpleDateFormat getDateFormat();

	public SimpleDateFormat getTimeFormat();

	public SimpleDateFormat getTimestampFormat();

	/**
	 * Liefert das heutige Datum im aus PLConfig.xml.
	 * <p>
	 * Achtung! <br>
	 * Da der PersistenzLayer üblicherweise auf einem Server läuft, kann ein
	 * Client auf diese Art aktuelle Angaben über Datum und Uhrzeit des Servers
	 * ermitteln. Dieses ist in der Regel besser als die lokale Zeit des Client
	 * zu verwenden.
	 * 
	 * @return
	 */
	public String getTodayString();

	public String getNowString();

	public String getTodayNowString();

	/**
	 * Setzt den Debug-Modus des Persistenzlayers.
	 * 
	 * @param b
	 */
	public void setDebug(boolean b);

	/**
	 * Liefert den Debug-Modus
	 * 
	 * @return
	 */
	public boolean isDebug();

	/**
	 * Setzt den Persistenzlayer zurück.
	 * <p>
	 * Es wird der Cache der PreparedStatements gelöscht.
	 * 
	 * @throws PLException
	 */
	public void reset() throws PLException;

	/**
	 * Startet eine Transaktion mit dem angegebenen Namen. Bei geschachtelten
	 * Transaktionen müssen die Namen eindeutig sein.
	 * 
	 * @param transName
	 * @throws PLException
	 */
	public void startTransaction(String transName) throws PLException;

	/**
	 * Beendet die Transaktion mit dem angegebenen Namen, die zuvor mit
	 * startTransaktion gestartet wurde.
	 * <p>
	 * Bei Angabe eines falschen Transaktionsnamens wird eine Exception
	 * geworfen. <br>
	 * Bei geschachtelten Transaktion wird dann eine Exception geworfen, wenn
	 * zuvor ein Rollback ausgeführt wurde.
	 * 
	 * @param transName
	 * @throws PLException
	 */
	public boolean commitTransaction(String transName) throws PLException;

	/**
	 * Überppft, ob eine Transaktion mit commitTransaktion(name) beendet werden
	 * kann. Voraussetzung ist:
	 * <ul>
	 * <li>Es ist noch mindestens eine geschachtelte Transaktion offen
	 * <li>Es wurde von den anderen geschachtelten Tranaksaktionen zuvor kein
	 * Rollback ausgelöst
	 * </ul>
	 * 
	 * @return
	 * @throws PLException
	 */
	public boolean testCommit() throws PLException;

	/**
	 * Rollback der zuvor gestarteten Transaktion.
	 * 
	 * @param transName
	 * @throws PLException
	 */
	public boolean rollbackTransaction(String transName) throws PLException;
  
	public IPLContext startNewTransaction(String transName) throws PLException;

  public int importJournal(List<Document> transaction) throws PLException;


	//    /**
	//     * Beendet alle offenen Transaktionen und führt ein Rollback aus.
	//     * @throws PLException
	//     */
	//    public void abortTransaction() throws PLException;

	public int getNumActive();

	public int getNumIdle();

	/**
	 * @return
	 */
	public int getMaxActive();

	/**
	 * @return
	 */
	public int getMaxIdle();

	/**
	 * @return
	 */
	public int getMinIdle();
	public long getConnectionTimeOut();
	/**
	 * @formatter:off
	 * @see #startNewTransaction(String)
	 * 
	 * @return bei autocommit null
	 * @formatter:on
	 */
	public String getTransactionName();

	public Collection<TransactionInfo> getTransactionInfos();
}