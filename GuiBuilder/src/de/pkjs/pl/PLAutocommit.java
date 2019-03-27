/*
 * Created on 14.12.2004
 */
package de.pkjs.pl;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import java.util.*;

import de.jdataset.*;
import electric.xml.Document;

/**
 * Persistenz-Layer für Autocommit-Modus.
 * Alle Methoden werden an ein Objekt der Klasse PL delegiert.
 * In diesem Modus sind explizite Transaktionen nicht erlaubt.
 * Die Methoden startTransaktion usw. werfen eine IllegalStateException.<p>
 * Für explizite Transaktionen:
 * @see de.pkjs.pl.PL#startNewTransaction
 */
public final class PLAutocommit implements IPL {
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PLAutocommit.class);
	// Attributes
	private PL pl;
	// Constructors
	/**
	 * Erzeugt einen neuen Persistenz-Layer.<p>
	 * Die Einstellungen werden aus "PLConfig.xml" einglesen.<br>
	 * <strong>Achtung!</strong>Wenn diese Datei im aktuellen Verzeichnis nicht gefunden wird,
	 * wird auch im übergeordneten Verzeichnis danach gesucht: "../PLConfig.xml"
	 */
	public PLAutocommit() {
		try {
			this.pl = new PL();
		} catch (Exception ex) {
			throw new IllegalStateException(ex.getMessage());
		}
	}
	/**
	 * Erzeugt einen PersistenzLayer aus der angegebenen Configuationsdatei.
	 * @param configFileName
	 * @throws Exception
	 */
	public PLAutocommit(String configFileName) throws PLException {
		try {
			this.pl = new PL(configFileName);
		} catch (Exception ex) {
			throw new PLException(ex.getMessage(), ex);
		}
	}
	/**
	 * Erzeugt einen Persistenzlayer mit der angegebenen
	 * Konfigurationsdatei und der Datenbank-Authorisierung.<p>
	 * Die hier übergebenen Angaben bezüglich Username und Password
	 * sind vorrangig gegen über denen in der Konfigurationsdatei.<p>
	 * Der Sinn dieses Konstrukturs besteht darin,
	 * mit verschiedenen Usern einen Persistenzdienst zu erhalten,
	 * die auch mit der selben Konfiguration arbeiten.
	 * @param configFileName is null then "PLConfig.xml"
	 * @param username must be != null
	 * @param password must be != null
	 */
	public PLAutocommit(String configFileName, String username, String password) throws Exception {
		this.pl = new PL(configFileName, username, password);
	}
	/**
	 * Erzeugt einen Persistenz-Layer aus einem XML-Dokument
	 * im Format von PLConfig.dtd.
	 * @param doc
	 * @throws Exception
	 */
	public PLAutocommit(Document doc) throws Exception {
		this.pl = new PL(doc);
	}
	/**
	 * Liefert die Delegation an PL
	 * @return
	 */
	public IPLContext getIPLContext() {
	   return pl;
	}
	
	public Database getCurrentDatabase() {
		return pl.getCurrentDatabase();		
	}
	
	public String getCurrentDatabaseName()  {
		return pl.getCurrentDatabaseName();
	}
	
	public JDataSet getDataset(String datasetname, long oid) throws PLException {
		try {
			return pl.getDataset(datasetname, oid);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public JDataSet getDataset(String datasetname, String key) throws PLException {
		try {
			return pl.getDataset(datasetname, key);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public String getDatasetString(String datasetname, long oid) throws PLException {
		try {
			return pl.getDatasetString(datasetname, oid);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public JDataSet getDataset(String datasetname, ParameterList parameters) throws PLException {
		try {
			return pl.getDataset(datasetname, parameters);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}	
	public int setDataset(JDataSet dataset) throws PLException {
	   int cnt = pl.setDataset(dataset);
		return cnt;
	}	
	public int setDataset(String dataset) throws PLException {
		int cnt = pl.setDataset(dataset);
		return cnt;
	}
	public JDataSet getAll(String datasetname) throws PLException {
		try {
			return pl.getAll(datasetname);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}

	public JDataSet getDatasetSql(String datasetname, String sql) throws PLException  {
		try {
			return pl.getDatasetSql(datasetname, sql);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public JDataSet getDatasetSql(String datasetname, String sql, int limit) throws PLException  {
		try {
			return pl.getDatasetSql(datasetname, sql, limit);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public JDataSet getDatasetSql(String datasetname, String sql, ParameterList list) throws PLException  {
		try {
			return pl.getDatasetSql(datasetname, sql, list);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
   public JDataSet getDatasetStatement(String name) throws PLException {
      return pl.getDatasetStatement(name);
   }
   public JDataSet getDatasetStatement(String name, ParameterList list) throws PLException {
      return pl.getDatasetStatement(name, list);
   }

   public DataRowIterator getDataRowIteratorStatement(String statementName, ParameterList list) throws PLException {
      try {
           return pl.getDataRowIteratorStatement(statementName, list);
 		} catch (Exception ex) {
 			logger.error("PLAutocommit", ex);
 			throw new PLException(ex.getMessage(), ex);
 		}
   }
	public DataRowIterator getDataRowIterator(String datasetName, String tablename, String sql, ParameterList list) throws PLException {
      try {
           return pl.getDataRowIterator(datasetName, tablename, sql, list);
 		} catch (Exception ex) {
 			logger.error("PLAutocommit", ex);
 			throw new PLException(ex.getMessage(), ex);
 		}
	}
	public JDataSet getEmptyDataset(String datasetname) {
		try {
			return pl.getEmptyDataset(datasetname);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new IllegalStateException(ex.getMessage());
		}
	}
	public long getOID() throws PLException  {
		try {
			return pl.getOID();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public long getOID(String seqName) throws PLException  {
		try {
			return pl.getOID(seqName);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public int executeSql(String sqlCommand) throws PLException {
		try {
			return pl.executeSql(sqlCommand);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	
	public int executeSql(String sqlCommand, ParameterList parameters) throws PLException {
		try {
			return pl.executeSql(sqlCommand, parameters);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	
	public int executeSqlPara(String sqlCommand, Vector parameter) throws PLException  {
		try {
			return pl.executeSqlPara(sqlCommand, parameter);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public int executeStatement(String name) throws PLException {
		try {
			return pl.executeStatement(name);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public int executeStatement(String name, ParameterList parameters) throws PLException {
		try {
			return pl.executeStatement(name, parameters);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
   /**
    * Führt ein Batch-Statement aus.<p>
    * Batch-Statements sind eine Menge von NamedStatements, die nacheinander als
    * Batch ausgeführt werden.
    * @param name
    * @return
    * @throws PLException
    * @throws PLException
    */
   public int executeBatchStatement(String name) throws PLException {
 		try {
 			return pl.executeBatchStatement(name);
 		} catch (Exception ex) {
 			logger.error("PLAutocommit", ex);
 			throw new PLException(ex.getMessage(), ex);
 		}
   }
   /**
    * Führt ein Batch-Statement aus.<p>
    * Batch-Statements sind eine Menge von NamedStatements, die nacheinander als
    * Batch ausgeführt werden.
    * @param name
    * @param list Eine Liste von Parametern für die Menge aller Parameter, die
    * für die verschiedenen Statements insgesamt benötigt werden.
    * @return
    * @throws PLException
    * @throws PLException
    */
   public int executeBatchStatement(String name, ParameterList list) throws PLException {
 		try {
 			return pl.executeBatchStatement(name, list);
 		} catch (Exception ex) {
 			logger.error("PLAutocommit", ex);
 			throw new PLException(ex.getMessage(), ex);
 		}
   }
   
	public void shutdown() throws PLException  {
		try {
			pl.shutdown();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	
	public Request getRequest(String datasetName) {
		return pl.getRequest(datasetName);
	}

	public String getDatabaseMetaData() throws PLException  {
		try {
			return pl.getDatabaseMetaData();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public String getPLMetaData() throws PLException  {
		try {
			return pl.getPLMetaData();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}

	public ArrayList getDatasetNames() throws PLException {
		try {
			return pl.getDatasetNames();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	/**
	 * Führt das "Ping"-Statement der Datenbank aus, wie in PLConfig definiert.
	 * @return Liefert "pong" wenn alles OK, "No PingStatement defined" wenn
	 * in PLConfig keine PingStatement definiert ist.
	 * @throws PLException
	 * @throws PLException
	 */
	public String pingDatabase() throws PLException {
		try {
			return pl.pingDatabase();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}

	public SimpleDateFormat getDateFormat() throws PLException {
		try {
			return pl.getDateFormat();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public SimpleDateFormat getTimeFormat() throws PLException {
		try {
			return pl.getTimeFormat();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public SimpleDateFormat getTimestampFormat() throws PLException {
		try {
			return pl.getTimestampFormat();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public String getTodayString() throws PLException {
		try {
			return pl.getTodayString();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public String getNowString() throws PLException {
		try {
			return pl.getNowString();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public String getTodayNowString() throws PLException {
		try {
			return pl.getTodayNowString();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public void setDebug(boolean b) throws PLException {
		try {
			pl.setDebug(b);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public boolean isDebug() throws PLException  {
		try {
			return pl.isDebug();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public void reset() throws PLException  {
		try {
			pl.reset();
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
	public IPLContext startNewTransaction(String transName) throws PLException {
	   try {
	      return pl.startNewTransaction(transName);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}
   public PLTransactionContextMBean getMBeanPL() {
       return this.pl.getMBeanPL();
   }
   public DatabaseMBean getMBeanDB() {
       return this.pl.getCurrentDatabase();
   }
	// ### NOT SUPPORTED ###################################
	private static final String NOT_SUPPORTED  = "PLAutocommit: Transactions not supported";
	/**
	 * @deprecated
	 */
	public void startTransaction(String transName) {
		logger.error(NOT_SUPPORTED);
		throw new IllegalStateException(NOT_SUPPORTED);
	}
	/**
	 * @deprecated
	 */
	public void commitTransaction(String transName) throws SQLException {
		logger.error(NOT_SUPPORTED);
		throw new IllegalStateException(NOT_SUPPORTED);
	}
	/**
	 * @deprecated
	 */
	public boolean testCommit() {
		logger.error(NOT_SUPPORTED);
		throw new IllegalStateException(NOT_SUPPORTED);
	}
	/**
	 * @deprecated
	 */
	public void rollbackTransaction(String transName) throws SQLException {
		logger.error(NOT_SUPPORTED);
		throw new IllegalStateException(NOT_SUPPORTED);
	}
	/**
	 * @deprecated
	 */
	public void abortTransaction() {
		logger.error(NOT_SUPPORTED);
		throw new IllegalStateException(NOT_SUPPORTED);
	}
	
	/**
	 * @throws PLException
	 * @deprecated
	 */
	public JDataSet getDatasetSql(String tablename, String columns, String from) throws PLException {
		try {
			return pl.getDatasetSql(tablename, columns, from);
		} catch (Exception ex) {
			logger.error("PLAutocommit", ex);
			throw new PLException(ex.getMessage(), ex);
		}
	}

}
	
