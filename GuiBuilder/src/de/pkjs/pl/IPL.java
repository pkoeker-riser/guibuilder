package de.pkjs.pl;

import de.jdataset.*;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Dieses Interface ist von einem Persistence Layer zu implementieren. Wenn es
 * mit electric GLUE publiziert wird, kann ein Client diese Methoden �ber SOAP
 * aufrufen.
 * <p>
 * F�r die Konfiguration des Persistenzlayers werden PLConfig.xml und
 * DatabaseConfig.xml eingesetzt.
 * 
 * @author Peter K�ker <br>
 *         http://www.pkjs.de
 */
public interface IPL {
 	/**
 	 * Liefert die Delegation an PL
 	 * @return
 	 */
 	public IPLContext getIPLContext();
 	/**
 	 * Liefert die aktuelle Database
 	 * @return
 	 */
 	public Database getCurrentDatabase();

    /**
     * Liefert den Namen zuletzt verwendeten Datenbank.
     * 
     * @return String
     */
    public String getCurrentDatabaseName();

    /**
     * Liefert einen Dataset mit dem angegebenen Namen und dem angegebenen
     * Prim�rschl�ssel der Wurzeltabelle.
     * 
     * @param datasetname
     * @param oid
     * @return
    * @throws PLException
     */
    public JDataSet getDataset(String datasetname, long oid) throws PLException;
    /**
     * Liefert einen Dataset mit dem angegebenen Namen und dem angegebenen
     * Prim�rschl�ssel der Wurzeltabelle.
     * @param datasetname
     * @param key
     * @return
    * @throws PLException
     */
    public JDataSet getDataset(String datasetname, String key) throws PLException;
//    /**
//     * Liefert einen Dataset mit dem angegebenen Namen und dem angegebenen
//     * Prim�rschl�ssel der Wurzeltabelle.
//     * @param datasetname
//     * @param key
//     * @return
//     */
//    public JDataSet getDataset(String datasetname, String key);
    

    public String getDatasetString(String datasetname, long oid) throws PLException;

    /**
     * Liefert einen Dataset mit dem angegebenen Namen sowie unter Angabe eine
     * Liste von Parametern.
     * 
     * @param datasetname
     * @param parameters
     * @return
    * @throws PLException
     */
    public JDataSet getDataset(String datasetname, ParameterList parameters) throws PLException;

    /**
     * Der vom Client ge�nderter Dataset wird in die Datenbank
     * zur�ckgeschrieben.
     * 
     * @param values
     *            Ein Dataset
     */
    public int setDataset(JDataSet dataset) throws PLException;

    public int setDataset(String dataset) throws PLException;

    /**
     * Liefert einen Dataset ohne Angabe eines Schl�ssels; also vor allem eine
     * Menge von Datens�tzen - bis zum Inhalt einer kompletten Tabelle.
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
    public JDataSet getDatasetSql(String tablename, String columns, String from) throws PLException;

    /**
     * Liefert einen flachen Dataset auf Basis eines beliebigen SQL-Statements.
     * <p>
     * Achtung! <br>
     * Bei Sybase m�ssen die angebenen Column-Namen eindeutig sein!
     * 
     * @param datasetname ( =
     *            TableName)
     * @param sql
     *            "SELECT a,b FROM c,d WHERE ... ORDER BY ...
     * @return
    * @throws PLException
     */
    public JDataSet getDatasetSql(String datasetname, String sql) throws PLException;

    /**
     * 
     * @param datasetname
     * @param sql
     * @param limit
     *            Maximale Gr��e des ResultSet vordefinieren
     * @return
    * @throws PLException
     */
    public JDataSet getDatasetSql(String datasetname, String sql, int limit) throws PLException;

    /**
     * Liefert einen flachen Dataset auf Basis eines beliebigen SQL-Statements.
     * <p>
     * Achtung! <br>
     * Bei Sybase m�ssen die angebenen Column-Namen eindeutig sein!
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
 	 * Liefert einen DataSet unter Angabe des Namens eines benannten Statements. 
 	 * @param name
 	 * @return
 	 * @throws PLException
 	 */
    public JDataSet getDatasetStatement(String name) throws PLException;
    /**
     * Liefert einen DataSet unter Angabe des Namens eines benannten Statements.
     * @param name
     * @param list Argumentliste f�r dieses Statement
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
     * @throws PLException
     */
    public DataRowIterator getDataRowIteratorStatement(String statementName,
            ParameterList list) throws PLException;

    /**
     * W�hrend 'normalerweise' ein DataSet vom Persistenz-Layer immer ferig
     * aufbereitet und komplett mit allen Daten �bergeben wird, ist dieses
     * Vorgehen bei gro�en Datenmengen nicht angebracht, nicht nur, da� das zu
     * lange dauert, sondern weil auch ein OutOfMemory droht.
     * <p>
     * Der RowIterator schafft dort Abhilfe. <br>
     * Es wird ein DataRowIterator geliefert unter Angabe von:
     * 
     * @param datasetName
     *            Namens des DataSet f�r den RowIterator
     * @param tablename Name der RootTable
     * @param sql
     *            SELECT-Statement
     * @param list
     *            ParameterListe (darf auch null sein)
     * @return Den RowIterator mit den gew�nschten Daten.
    * @throws PLException
     */
    public DataRowIterator getDataRowIterator(String datasetName, String tablename, String sql,
            ParameterList list) throws PLException;

    /**
     * Liefert einen Dataset ohne Werte aber mit den Metadaten. Wird bei der
     * Erstellung eines neuen Datensatzes ben�tigt.
     * 
     * @param datasetname
     *            Der gew�nschte Dataset
     * @return JDataSet
    * @throws PLException
     */
    public JDataSet getEmptyDataset(String datasetname);

    /**
     * Liefert einen neuen eindeutigen Schl�ssel f�r den Client.
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
     * F�hrt ein beliebiges SQL-Command aus (Update, Insert, Delete). Um
     * beliebige Daten einzulesen siehe:
     * 
     * @see #getDatasetSql
     * @param sqlCommand
     * @return Anzahl ge�nderter Datens�tze oder 0
    * @throws PLException
     */
    public int executeSql(String sqlCommand) throws PLException;

    /**
     * F�hrt ein beliebiges SQL-Command aus (Update, Insert, Delete). Um
     * beliebige Daten einzulesen siehe:
     * 
     * @see #getDatasetSql
     * @param sqlCommand
     *            SQL-Befehl
     * @param parameters
     *            Parameter f�r PreparedStatement
     * @return Anzahl ge�nderter Datens�tze oder 0
    * @throws PLException
     */
    public int executeSql(String sqlCommand, ParameterList parameters) throws PLException;
    /**
     * F�hrt ein benanntes Statement (UPDATE, INSERT, DELETE) aus.
     * @param name Statement-Name
     * @return Anzahl ge�nderter Datens�tze oder 0
    * @throws PLException
     */
    public int executeStatement(String name) throws PLException;
    /**
     * F�hrt ein benanntes Statement (UPDATE, INSERT, DELETE) aus.
     * @param name name Statement-Name
     * @param parameters Paramter f�r das Statement
     * @return Anzahl ge�nderter Datens�tze oder 0
    * @throws PLException
     */
    public int executeStatement(String name, ParameterList parameters) throws PLException;
    /**
     * F�hrt ein Batch-Statement aus.<p>
     * Batch-Statements sind eine Menge von NamedStatements, die nacheinander als
     * Batch ausgef�hrt werden.
     * @param name
     * @return Anzahl ge�nderter Datens�tze oder 0
    * @throws PLException
     * @throws PLException
     */
    public int executeBatchStatement(String name) throws PLException;
    /**
     * F�hrt ein Batch-Statement aus.<p>
     * Batch-Statements sind eine Menge von NamedStatements, die nacheinander als
     * Batch ausgef�hrt werden.
     * @param name
     * @param list Eine Liste von Parametern f�r die Menge aller Parameter, die
     * f�r die verschiedenen Statements insgesamt ben�tigt werden.
     * @return Anzahl ge�nderter Datens�tze oder 0
    * @throws PLException
     * @throws PLException
     */
    public int executeBatchStatement(String name, ParameterList list) throws PLException;
    /**
     * F�hrt ein beliebiges SQL-Statement aus, wobei die Parameter f�r dieses
     * Statement in einem Vector �bergeben werden.
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
     * @return Anzahl ge�nderter Datens�tze oder 0
    * @throws PLException
     */
    public int executeSqlPara(String sqlCommand, Vector parameter) throws PLException;

    /**
     * Die Connection zur Datenbank wird geschlossen.
     * @throws PLException  
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
    * @throws PLException
     */
    public String getDatabaseMetaData() throws PLException;

    /**
     * Liefert alle Informationen zum Persistenz-Layer als XML-Document.
     * 
     * @return ein XML-Dokument als String
    * @throws PLException
     */
    public String getPLMetaData() throws PLException;

    /**
     * Liefert die Menge der definierten Datenbank-Zugriffe.
     * 
     * @return
    * @throws PLException
     */
    public ArrayList getDatasetNames() throws PLException;

    /**
 	 * F�hrt das "Ping"-Statement der Datenbank aus, wie in PLConfig definiert.
 	 * @return Liefert "pong" wenn alles OK, "No PingStatement defined" wenn
 	 * in PLConfig keine PingStatement definiert ist.
 	 * @throws PLException
 	 */
 	public String pingDatabase() throws PLException;

    /**
     * Liefert das DateFormat aus PLConfig.xml
     * 
     * @return
    * @throws PLException
     */
    public SimpleDateFormat getDateFormat() throws PLException;

    public SimpleDateFormat getTimeFormat() throws PLException;

    public SimpleDateFormat getTimestampFormat() throws PLException;

    /**
     * Liefert das heutige Datum im aus PLConfig.xml.
     * <p>
     * Achtung! <br>
     * Da der PersistenzLayer �blicherweise auf einem Server l�uft, kann ein
     * Client auf diese Art aktuelle Angaben �ber Datum und Uhrzeit des Servers
     * ermitteln. Dieses ist in der Regel besser als die lokale Zeit des Client
     * zu verwenden.
     * 
     * @return
    * @throws PLException
     */
    public String getTodayString() throws PLException;

    public String getNowString() throws PLException;

    public String getTodayNowString() throws PLException;

    /**
     * Setzt den Debug-Modus des Persistenzlayers.
     * 
     * @param b
    * @throws PLException
     */
    public void setDebug(boolean b) throws PLException;

    /**
     * Liefert den Debug-Modus
     * 
     * @return
    * @throws PLException
     */
    public boolean isDebug() throws PLException;

    /**
     * Setzt den Persistenzlayer zur�ck.
     * <p>
     * Es wird der Cache der PreparedStatements gel�scht.
    * @throws PLException
     */
    public void reset() throws PLException;
    /**
     * Liefert die MBean zum Persistence Layer
     * @return
     */
    public PLTransactionContextMBean getMBeanPL();
    /**
     * Liefert die MBean zur Datenbank
     * @return
     */
    public DatabaseMBean getMBeanDB();

    /**
     * @deprecated
     * @see #startNewTransaction(String)
     * Startet eine Transaktion mit dem angegebenen Namen. Bei geschachtelten
     * Transaktionen m�ssen die Namen eindeutig sein.
     * @see #startNewTransaction
     * @param transName
     */
    public void startTransaction(String transName);
    /**
     * Started eine neue transaktion und liefert den TransaktionsContext dazu.
     * @param transName
     * @return
    * @throws PLException
     */
    public IPLContext startNewTransaction(String transName) throws PLException;
    /**
     * @deprecated
     * @see IPLContext#commitTransaction(String)
     * Beendet die Transaktion mit dem angegebenen Namen, die zuvor mit
     * startTransaktion gestartet wurde.
     * <p>
     * Bei Angabe eines falschen Transaktionsnamens wird eine Exception
     * geworfen. <br>
     * Bei geschachtelten Transaktion wird dann eine Exception geworfen, wenn
     * zuvor ein Rollback ausgef�hrt wurde.
     * 
     * @param transName
     */
    public void commitTransaction(String transName) throws SQLException;

    /**
     * @deprecated
     * �berp�ft, ob eine Transaktion mit commitTransaktion(name) beendet werden
     * kann. Voraussetzung ist:
     * <ul>
     * <li>Es ist noch mindestens eine geschachtelte Transaktion offen
     * <li>Es wurde von den anderen geschachtelten Tranaksaktionen zuvor kein
     * Rollback ausgel�st
     * </ul>
     * 
     * @return
     */
    public boolean testCommit();

    /**
     * @deprecated
     * @see IPLContext#rollbackTransaction(String)
     * Rollback der zuvor gestarteten Transaktion.
     * 
     * @param transName
     */
    public void rollbackTransaction(String transName) throws SQLException;

    /**
     * @deprecated
     * Beendet alle offenen Transaktionen und f�hrt ein Rollback aus.
     */
    public void abortTransaction();
    
}