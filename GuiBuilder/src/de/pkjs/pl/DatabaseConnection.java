package de.pkjs.pl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DisposableConnectionFacade;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import org.apache.tomcat.jdbc.pool.ProxyConnection;
import org.apache.tomcat.jdbc.pool.TrapException;

import de.pkjs.pl.Database.NamedSequence;

/**
 * Wrapper-Klasser um eine JDBC-Connection zur Datenbank.
 */
final class DatabaseConnection {
  private static org.apache.log4j.Logger       logger = org.apache.log4j.Logger
                                                        .getLogger(DatabaseConnection.class);
  private String                               label;
  private Connection                           connection;
  private Database                             config;
  private String                               getSeq;
  private String                               setSeq;
  private String                               layerName;
  private TransactionManager                   tm;
  // Debug-Info
  private Date                                 created;
  private Date                                 lastUsed;
  private Thread                               owner;
  private String                               sessionID;
  // ID
  private long        id;
  private static long idCounter;
  private boolean     closed; // als Ersatz für isClosed, weil das geht übers Netz an die Datenbank!
  /**
   * Erzeugt ein neues DatabaseConnection Objekt <br>
   * <i>Creates a new DatabaseConnection object </i>
   * @param config
   *           Einstellungen f&uuml;r diese neue DatabaseConnection. <br>
   *           <i>Settings für this new DatabaseConnection. </i>
   * @param conn Eine JDBC-Connection aus dem ConnectionPool.
   * @throws PLException
   *            wenn die neue DatabaseConnection nicht initialisiert werden
   *            kannn. <br>
   *            <i>If the new DatabaseConnection could not be initalized. </i>
   */
  DatabaseConnection(Database database, Connection conn) {
    this.config = database;
    this.layerName = config.getLayerName();
    this.getSeq = config.getGetSequence();
    this.setSeq = config.getSetSequence();
    this.created = new Date();
    this.lastUsed = new Date();
    this.connection = conn;
    // Beliebig viele benannte Sequences
    synchronized (DatabaseConnection.class) {
      this.id = idCounter++;             
    }
    // Transaction Manager erzeugen
    this.tm = new TransactionManager(this);
  }
  
  Database getDatabase() {
    return this.config;
  }

  void close(String reason) throws PLException {
    try {
      synchronized (connection) {
        if (!closed) {
        //if (!connection.isClosed()) { // Das dauert zu lange! Extra Netzwerkzugriff!
          logger.debug("Closing a connection: " + this.toString() + " Reason: " + reason);
          this.connection.close();
          closed = true;
        } else {
          logger.warn("Closing a connection which is already closed: " + this.label + " Reason: " + reason);
        }
      }
    } catch (SQLException ex) {
      logger.error(ex.getMessage(), ex);
      throw new PLException(ex);
    }
  }

  /**
   * Schlie&szlig;t die DatabaseConnection, falls JDBC-Connection noch
   * erreichbar ist (con != null). <br>
   * <i>Closes the DatabaseConnection object if the JDBC connection is
   * reachable (con != null). </i>
   */
  protected void finalize() throws Throwable {
      try {
         if(this.connection != null) {
            synchronized(connection) {
               if(!closed) {
                  logger.warn("PL [ " + layerName + "/" + this.label + " ]  Owner: [ " + this.getOwnerName() + " ] closing DatabaseConnection " + this.toString());
                  this.close("finalize");
               }
            }
         }
      }
      finally {
         super.finalize();
      }
  }

  void startTransaction(String transName) {
    this.tm.startTransaction(transName);
  }

  boolean testCommit() {
    return this.tm.testCommit();
  }

  boolean rollbackTransaction(String transName) throws PLException {
    boolean rollback = this.tm.rollbackTransaction(transName);
    updateLastUsed();
    return rollback;
  }

  boolean commitTransaction(String transName) throws PLException {
    boolean committed = tm.commitTransaction(transName);
    updateLastUsed();
    return committed;
  }

  boolean abortTransaction(String transName) throws PLException {
    boolean aborted = this.tm.abortTransaction(transName);
    return aborted;
  }

  boolean hasOpenTransaction() {
    return tm.hasOpenTransaction();
  }

  String getOpenTransactions() {
    return tm.getOpenTransactions();
  }

  /**
   * Liefert die Connection zur Datenbank
   * @return Die JDBC-Connection
   * @throws PLException
   */
  Connection getConnection() throws PLException {
     if (closed) {
       String msg = "Getting Connection which is already been closed! " + this.label;
       logger.error(msg);
       throw new PLException(msg);
     }
    this.updateLastUsed();
    return connection;
  }

  int getDatabaseType() {
    return config.getDatabaseType();
  }

  /**
   * @return Returns the layerName.
   */
  String getLayerName() {
    return layerName;
  }

  /**
   * Liefert eine neue ObjectId unter der angegebenen Named Sequence.
   * <p>
   * Wirft eine IllegalStateException, wenn garkeine Sequences definiert oder
   * eine IllegalArgumentException, wenn unter dem angegebenen Namen keine
   * Sequence existiert.
   * @param seqName
   * @return
   * @throws PLException
   */
  long getNextSequence(String seqName) throws PLException {
     NamedSequence seq = config.getSequence(seqName);
      if (seq == null) {
        throw new PLException("Missing named sequence: " + seqName);
      }
    long oid = seq.getNextSequence(this.connection);
    return oid;
  }

  /**
   * Wenn innerhalb von Transaktionen oids ben&ouml;tigt werden, dann hier
   * "false" &uuml;bergeben; wenn der Client eine einzelne oid, dann "true".
   * <p>
   * Gilt natürlich nur für Datenbank, die keine Sequences kennen.
   * <P>
   * Es wird eine PLException geworfen, wenn keine default-Sequence für das
   * Erzeugen von Primary Keys definiert wurde.
   * @param commit
   * @return
   */
  synchronized long getOID(boolean commit) throws PLException {
    if (this.getSeq == null) {
      throw new PLException(
        "Cannot create Primary Key Value: No default Primary Key Sequence defined!");
    }
    long oid = -1;
    try {
      PreparedStatement getStmt = this.getConnection().prepareStatement(this.getSeq);
      long _startTime = System.currentTimeMillis();
      ResultSet rs = getStmt.executeQuery(); // Timeout??
      rs.next();
      oid = rs.getLong(1);
      rs.close();
      getStmt.close(); // NEW ORACLE
      long _dura = System.currentTimeMillis() - _startTime;
      if (_dura > this.config.getDefaultMaxExecutionTime()) {
        Logger slowLog = config.getSlowQueryLogger();
        if (slowLog != null) {
          String msg = "Statement: " + this.getSeq
          + " maxExecutionTime exceeded: " + _dura + ">" + this.config.getDefaultMaxExecutionTime();
          slowLog.warn(msg);
        }
      }
      if (this.setSeq != null) {
        oid++;
        long _startTime2 = System.currentTimeMillis();
        PreparedStatement setStmt = this.getConnection().prepareStatement(this.setSeq);
        if (this.getDatabaseType() == Database.JDBC_ODBC) { // weia!
          setStmt.setInt(1, (int) oid);
        } else {
          setStmt.setLong(1, oid);
        }
        setStmt.executeUpdate();
        if (commit) {
          connection.commit();
        }
        long _dura2 = System.currentTimeMillis() - _startTime2;
        if (_dura2 > this.config.getDefaultMaxExecutionTime()) {
          Logger slowLog = config.getSlowQueryLogger();
          if (slowLog != null) {
            String msg = "Statement: " + this.getSeq
            + " maxExecutionTime exceeded: " + _dura2 + ">" + this.config.getDefaultMaxExecutionTime();
            slowLog.warn(msg);
          }
        }
      }
      return oid;
    } catch (SQLException ex) {
      String msg = "PL [ " + layerName + " ] " + "Unable to get/set Sequence: " + ex.getMessage();
      logger.error(msg, ex);
      logger.info("Connection Created: " + this.created);
      throw new PLException(msg, ex);
    }
  }

  TransactionManager getTransactionManager() {
    return this.tm;
  }

  void shutdown() {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException ex) {
      logger.error(
        "PL [ " + layerName + " ] " + "Unable to shutdown Database.\n" + ex.getMessage(), ex);
    }
  }


  boolean isDebug() {
    return this.config.isDebug();
  }
  
  long getId() {
    return this.id;
  }
  
  Date getCreatedTimestamp() {
    return this.created;
  }


  boolean isCommited() {
    return tm.isCommited();
  }

  boolean isRollback() {
    return tm.isRollback();
  }
  
  boolean isClosed() {
	  return closed;
  }

  String getOptimisticField() {
    return this.config.getOptimisticField();
  }

  /**
   * @return Returns the connectionTimeOut. After this time the connection
   *         should be checked
   */
  long getConnectionTimeOut() {
    return config.getConnectionTimeOut();
  }

  /**
   * @return Returns the transactionLevel.
   */
  int getTransactionLevel() {
    return config.getTransactionIsolationLevel();
  }

  /**
   * @return Returns the owner.
   */
  Thread getOwner() {
    return owner;
  }

  /**
   * @param owner
   *           The owner to set.
   */
  void setOwner(Thread owner) {
    this.owner = owner;
  }

  /**
   * Liefert den Namen des Owner-Thread oder null, wenn keine Owner.
   * @return
   */
  String getOwnerName() {
    if (owner != null) {
      return owner.getName();
    }
    return null;
  }

  Date getLastusedTimestamp() {
    return lastUsed;
  }

  private void updateLastUsed() {
    this.lastUsed = new Date();
  }

  String getCreateUserField() {
    return this.config.getCreateUserField();
  }

  String getUpdateUserField() {
    return this.config.getUpdateUserField();
  }

  /**
   * Antwortet mit dem Datenbanknamen. <br>
   * <i>Returns the databaseName. </i>
   */
  String getDatabaseName() {
    return this.config.getDatabaseName();
  }
  
  String getUpdateJournalDirectory() {
    return this.config.getUpdateJournalDirectory();
  }

  /**
   * @return Returns the label.
   */
  String getLabel() {
    return label;
  }

  /**
   * @param label
   *           The label to set.
   */
  void setLabel(String label) {
    this.label = label;
    //this.setClientInfo("label", label);
  }
  
   String getDatabaseSessionID() {
      if(this.sessionID != null) {
         return sessionID;
      }
      if (!config.isDetectDatabaseSessionID()) {
      	return "-2";
      }
      sessionID = "-1"; // default
      switch(config.getDatabaseType()) {
        case Database.POSTGRES: {
           try { // je nach JDBC-Driver?
	           Object oconn1 = getMember(connection.getClass(), connection, "h");
	           if (oconn1 != null) {
	             Object oconn2 = getMember(oconn1.getClass(), oconn1, "next");
	             if (oconn2 != null) {
	                Object oconn3 = getMember(oconn2.getClass(), oconn2, "connection");
	                if (oconn3 != null) {
	 	              Object opc = getMember(oconn3.getClass(), oconn3, "protoConnection");
	 	              if (opc != null) {
	 	                Object pid = getMember(opc.getClass(), opc, "cancelPid");
	 	                sessionID = pid.toString();
	 	              }
	 	            }
	             }
	           }
           } catch (Exception ex) {
         	  logger.debug(ex.getMessage());
           }
         }
          break;
        case Database.MAX_DB:
            try {
              Object o = getMember(connection.getClass(), connection, "h"); 
              if(o instanceof ProxyConnection) { // tomcat
                ProxyConnection prcon = (ProxyConnection)o;
                PooledConnection poolcon = prcon.getConnection();
                Connection conn = poolcon.getConnection();
                 if (conn instanceof com.sap.dbtech.jdbc.trace.Connection) {
                   com.sap.dbtech.jdbc.trace.Connection sapConn = (com.sap.dbtech.jdbc.trace.Connection)conn;
                   com.sap.dbtech.jdbc.ConnectionSapDBFinalize sapConnf = (com.sap.dbtech.jdbc.ConnectionSapDBFinalize)sapConn.getInner();
                   //Object oi = getMember(sapConn.getClass(), sapConn, "_inner");
//                   com.sap.dbtech.jdbc.ConnectionSapDBFinalize sapConnf = 
//                     (com.sap.dbtech.jdbc.ConnectionSapDBFinalize)oi;
                   int sessid = sapConnf.getSessionID();
                   sessionID = Integer.toString(sessid);
                 } else { 
                   Object os = getMember(conn.getClass(), conn, "sessionID");
                   if (os != null) {
                     sessionID = os.toString();
                   }
                 }
               } else if (o instanceof TrapException) {
                 // Das passiert, wenn man die Property setJdbcInterceptors setzt!
                 TrapException te = (TrapException)o;
                 logger.warn("TrapException?");
                 sessionID = "TrapException";
               } else if (o instanceof DisposableConnectionFacade) {
                  DisposableConnectionFacade df = (DisposableConnectionFacade)o;
                  ProxyConnection prcon = (ProxyConnection)df.getNext();
                  PooledConnection poolcon = prcon.getConnection();
                  Connection conn = poolcon.getConnection();
                  if (conn instanceof com.sap.dbtech.jdbc.trace.Connection) {
                     com.sap.dbtech.jdbc.trace.Connection sapConn = (com.sap.dbtech.jdbc.trace.Connection)conn;
                     com.sap.dbtech.jdbc.ConnectionSapDBFinalize sapConnf = (com.sap.dbtech.jdbc.ConnectionSapDBFinalize)sapConn.getInner();
                     int sessid = sapConnf.getSessionID();
                     sessionID = Integer.toString(sessid);
                  }
               }
            }
            catch (Exception ex) {
              logger.debug(ex.getMessage(), ex);
            }
            break;
      }
      return sessionID;
   }
   
  private Object getMember(Class<?> clazz, Object op, String name) {
    try {
      Field fp = clazz.getDeclaredField(name);
      fp.setAccessible(true);
      Object o = fp.get(op);
      return o;
    } catch (SecurityException ex) {
      ex.printStackTrace();
    } catch (NoSuchFieldException ex) {
      if (clazz.getSuperclass() != null) {
        Class<?> sc = clazz.getSuperclass();
        return getMember(sc, op, name);
      } else {
        logger.debug(ex.getMessage(), ex);
      }
    } catch (IllegalArgumentException ex) {
      logger.debug(ex.getMessage(), ex);
    } catch (IllegalAccessException ex) {
      logger.debug(ex.getMessage(), ex);
    }
    return null;
  }
   
  void setIsolationLevel(int level) {
    if (connection != null) {
      try {
        int l = connection.getTransactionIsolation();
        if (l != level) {
          connection.setTransactionIsolation(level);          
        }
      } catch (SQLException ex) {
        logger.error("Can't set TransactionIsolationLevel: " + level, ex);
      }
    }
  }      
   
  public String toString() {
    if (label != null && label.length() != 0 && sessionID != null) {
      return label + "/" + sessionID;
    }
    return super.toString();
  }
}