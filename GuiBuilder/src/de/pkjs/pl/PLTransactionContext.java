package de.pkjs.pl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.jdataset.JDataColumn;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.jdataset.JDataValue;
import de.jdataset.NVPair;
import de.jdataset.ParameterList;
import de.pkjs.pl.Database.BatchStatement;
import de.pkjs.pl.Database.NamedStatement;
import de.pkjs.util.Convert;
import electric.xml.DocType;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;

/**
 * @author ikunin
 */
public final class PLTransactionContext implements IPLContext, PLTransactionContextMBean {
   private static final Logger logger = Logger.getLogger(PLTransactionContext.class);

   private DatabaseConnectionPool dbConnectionPool;
   private DatabaseConnection dbConnection; // null, wenn autocommit!
   
   private String transName;
   private static long transCounter = 1000;
   private Database database;
   private boolean isDisabled;
   private String layerName;
   private boolean isAutocommit;

   // Meta
   private static JDataTable metaDataTable;
   // Statistic
   private final java.util.Date createdTimeStamp = new java.util.Date();
   private java.util.Date resetTimeStamp;
   private long startedTransactions;
   private long commitedTransactions;
   private long rollbackedTransaktions;
   private long abortedTransactions;
   /**
    * Constructor für remote Transactionen
    * @param parentPL
    */
   public PLTransactionContext(PL parentPL) {
      this(parentPL.getDatabase(), parentPL.getDatabaseConnectionPool(), null, false);
   }
   /**
    * Transaction context for auto commit mode
    * @param database
    * @param dbConnectionPool
    * @throws PLException
    */
   PLTransactionContext(Database database, DatabaseConnectionPool dbConnectionPool) {
      this(database, dbConnectionPool, null, true);
   }

   /**
    * Transaction mode for custom or auto commit mode. The custom transaction
    * will be automaticaly started.
    * @param database
    * @param dbConnectionPool
    * @param transName
    *           Name of the custom transaction
    * @param isAutocommit
    * @throws PLException
    */
   PLTransactionContext(Database database, DatabaseConnectionPool dbConnectionPool,
         String transName, boolean isAutocommit) {
      if (transName == null && isAutocommit == false) {
         transCounter++;
         transName = "remoteTransaction" + transCounter;
      }
      this.database = database;
      this.layerName = database.getLayerName();
      this.dbConnectionPool = dbConnectionPool;
      this.isDisabled = false;
      this.isAutocommit = isAutocommit;
      if(!isAutocommit && (transName == null || transName.length() == 0)) {
         throw new IllegalArgumentException(
               "PL [ "
                     + layerName
                     + " ] Error creating transaction context for custom transaction mode because transaction name is null or empty!");
      }
      this.transName = transName;
   }
   

   private Database getDatabase() {
      return database;
   }

   public Database getCurrentDatabase() {
      return database;
   }

   public String getCurrentDatabaseName() {
      return database.getDatabaseName();
   }

   public ArrayList<String> getDatasetNames() {
      return database.getDatasetNames();
   }

   public boolean isDebug() {
      return database.isDebug();
   }

   public Request getRequest(String datasetname) {
      return getDatabase().getRequest(datasetname);
   }

   private PLException handleException(String methodName, String transactionName,
         DatabaseConnection con, Exception e) throws PLException {
     //e.printStackTrace();
      String msg = "PL [ " + layerName + " ] Error executing " + methodName + ": " + e.getMessage()
            + " (" + con.getLabel() + ")";
      try {
         boolean rollbacked = this._rollbackTransaction(transactionName, con);
         if (!rollbacked) {
           this._abortTransaction(transactionName, con);
         }
      } catch(Exception ex) {
         msg += "\nError rollback transaction: " + transactionName + " : " + ex.getMessage()
               + "\nAborting all transactions!";
         this._abortTransaction(transactionName, con);
         try {
         	this.invalidateConnection(con);
            msg += "\nConnection to database invalidated.";
         }
         catch(PLException exx) {
            msg += "\nError invalidating connection to database: " + exx.getMessage();
         }
      }
      logger.error(msg, e);
      if (e instanceof PLException) { // PLEx nicht weiter verpacken
        return new PLException(msg, (PLException)e);
      }
      return new PLException(msg, e);
   }

   private void setReadOnly(DatabaseConnection db, boolean state) throws PLException {
     /* 12.8.2007 PKÖ:
      * Bei Postgres darf der Status einer laufenden Transaktion nicht geändert werden.
      * Deshalb bei !autocommit immer !readonly setzen
      */
   	switch (database.getDatabaseType()) {   	   	
   		case Database.POSTGRES: 
   		case Database.MYSQL: // MYSql auch nicht
   		case Database.MARIA: // wie Mysql
   		case Database.HANA: // HANA auch nicht
   			return;   		
   	}
     if (!isAutocommit && state) {
       state = false;
     }
     try {
         if(db.getConnection().isReadOnly() != state) {
            db.getConnection().setReadOnly(state);
         }
      } catch(SQLException e) {
      	String msg = "PL [ " + layerName + " ] Error setting connection readonly: " + e.getMessage();
        logger.error(msg, e);
        throw new PLException(msg, e);
      }
   }

   // #### getDataset ##############################################
   public JDataSet getDataset(String datasetname, long oid) throws PLException {
       // Request
       Request req = this.getRequest(datasetname);
       if(req.isTransient()) {
          throw new PLException("Transient Request: " + datasetname);
       }
       // Cache read
       RequestCacheConfig cacheConfig = req.getCacheConfig();
       if (cacheConfig != null && cacheConfig.isEnabled()) {
         JDataSet datasetFromCache = cacheConfig.get(oid);
         if (datasetFromCache != null) {
           return datasetFromCache;
         }
       }
       DatabaseConnection con = this.getConnection(datasetname);
       try {
         // ReadOnly setzen
         this.setReadOnly(con, true);
         // isolationLevel abweichend?
         if (req.getIsolationLevel() != -1) {
           con.setIsolationLevel(req.getIsolationLevel());
         }
         // Request ausführen
         this._startTransaction(TransactionManager.TRANS_INTERNAL_GETDATASET, con);

         JDataSet dataset = req.getDataset(con, datasetname, oid);
         if(isDebug()) {
            logger.debug(dataset.getXml());
         }
         this._commitTransaction(TransactionManager.TRANS_INTERNAL_GETDATASET, con);
         // Cache put
         if (cacheConfig != null && cacheConfig.isEnabled() && dataset != null && dataset.getRowCount() > 0) {
           cacheConfig.put(oid, dataset);
         }
         return dataset; 
         //##return new JDataSet(dataset.getXml());
      }
      catch(Exception ex) {
         throw handleException("getDataset(String datasetname, long oid)",
               TransactionManager.TRANS_INTERNAL_GETDATASET, con, ex);
      }
 }

 public JDataSet getDataset(String datasetname, long[] oids) throws PLException {
   DatabaseConnection con = this.getConnection(datasetname);

    // Request
    Request req = this.getRequest(datasetname);
    if(req.isTransient()) {
       throw new PLException("Transient Request: " + datasetname);
    }
     try {
        // ReadOnly setzen
        this.setReadOnly(con, true);
        // isolationLevel abweichend?
        if (req.getIsolationLevel() != -1) {
          con.setIsolationLevel(req.getIsolationLevel());
        }
        // Request ausführen
        this._startTransaction(TransactionManager.TRANS_INTERNAL_GETDATASET, con);

        JDataSet dataset = req.getDataset(con, datasetname, oids);
        if(isDebug()) {
           logger.debug(dataset.getXml());
        }
        this._commitTransaction(TransactionManager.TRANS_INTERNAL_GETDATASET, con);
        // Cache put
        RequestCacheConfig cacheConfig = req.getCacheConfig(); 
        if (cacheConfig != null && cacheConfig.isEnabled() && dataset != null && dataset.getRowCount() > 1) {
          Vector<JDataSet> vds = dataset.splitMulti2Single();
          Iterator<JDataSet> it = vds.iterator();
          while(it.hasNext()) {
            JDataSet dsCache = it.next();
            JDataRow row = dsCache.getRow();
            JDataColumn pkcol = row.getDataTable().getPKColumn();
            long oid = -1;
            boolean boid = false;
            switch (pkcol.getDataType()) {
              case Types.TINYINT:
              case Types.SMALLINT:
              case Types.BIGINT:
              case Types.INTEGER:
              oid = row.getValueLong(pkcol.getColumnName());
              boid = true;
            }
            if (boid) {
              cacheConfig.put(oid, dsCache);
            }
          }
        }
        return dataset;
        //##return new JDataSet(dataset.getXml());

     }
     catch(Exception ex) {
        throw handleException("getDataset(String datasetname, long oid)",
              TransactionManager.TRANS_INTERNAL_GETDATASET, con, ex);
     }
  }

   
   public JDataSet getDataset(String datasetname, String key) throws PLException {
      DatabaseConnection con = this.getConnection(datasetname);
       // Request
       Request req = this.getRequest(datasetname);
       if(req.isTransient()) {
          throw new PLException("Transient Request: " + datasetname);
       }
        try {
           // ReadOnly setzen
           this.setReadOnly(con, true);
           // Request ausführen
           this._startTransaction(TransactionManager.TRANS_INTERNAL_GETDATASET, con);

           JDataSet dataset = req.getDataset(con, datasetname, key);
           if(isDebug()) {
              logger.debug(dataset.getXml());
           }
           this._commitTransaction(TransactionManager.TRANS_INTERNAL_GETDATASET, con);
           return dataset;
           //##return new JDataSet(dataset.getXml());

        }
        catch(Exception ex) {
           throw handleException("getDataset(String datasetname, long oid)",
                 TransactionManager.TRANS_INTERNAL_GETDATASET, con, ex);
        }
   }

   public String getDatasetString(String datasetname, long oid) throws PLException {
      JDataSet ds = this.getDataset(datasetname, oid);
      return ds.getXml().toString();
   }

   public JDataSet getDataset(String datasetname, ParameterList parameters) throws PLException {
      // Request
      DatabaseConnection con = getConnection(datasetname);
       Request req = this.getRequest(datasetname);
       if(req.isTransient()) {
          throw new PLException("Transient Request: " + datasetname);
       }
        try {
           // ReadOnly setzen
           setReadOnly(con, true);
           // IsolationLevel abweichend?
           if (parameters != null && parameters.getIsolationLevel() != -1) {
             con.setIsolationLevel(parameters.getIsolationLevel());
           }
           else if (req.getIsolationLevel() != -1) {
             con.setIsolationLevel(req.getIsolationLevel());
           }               
           _startTransaction(TransactionManager.TRANS_INTERNAL_GETDATASET, con);
           JDataSet ds = req.getDataset(con, datasetname, parameters);
           _commitTransaction(TransactionManager.TRANS_INTERNAL_GETDATASET, con);
           return ds;
           //##return new JDataSet(ds.getXml());
        }
        catch(Exception ex) {
           throw handleException(
                 "getDataset(String datasetname, ParameterList parameters) for dataset [ "
                       + datasetname + " ]", TransactionManager.TRANS_INTERNAL_GETDATASET, con,
                 ex);
        }
   }

   /**
    * Liefert einen Dataset mit der angegebenen WHERE-Bedingung.
    * @deprecated
    * @param datasetname
    * @param where
    *           Bedingung ohne das Wörtchen WHERE
    * @return Dataset
    * @throws PLException
    */
   public JDataSet getDatasetWhere(String datasetname, String where) throws PLException {
      // Request
      DatabaseConnection con = getConnection(datasetname);
       Request req = this.getRequest(datasetname);
       if(req.isTransient()) {
          throw new PLException("Transient Request: " + datasetname);
       }
        try {
           // ReadOnly setzen
           setReadOnly(con, true);
           // Request ausführen

           _startTransaction(TransactionManager.TRANS_INTERNAL_GETDATASET, con);

           JDataSet dataset = req.getDatasetWhere(con, datasetname, where);
           if(this.isDebug()) {
              logger.debug(dataset.getXml());
           }
           _commitTransaction(TransactionManager.TRANS_INTERNAL_GETDATASET, con);
           return dataset;
           //##return new JDataSet(dataset.getXml());
        }
        catch(Exception ex) {
           throw handleException("getDataset(String datasetname, String where) for dataset [ "
                 + datasetname + " ]", TransactionManager.TRANS_INTERNAL_GETDATASET, con, ex);
        }
   }

   public Database.NamedStatement getNamedStatement(String name) {
      NamedStatement nst = this.database.getStatement(name);
      return nst;
   }
   
   public JDataSet getDatasetStatement(NamedStatement nst, ParameterList list) throws PLException {
      // Cache-get
      if (nst.hasCache()) {
         String key = list.toString();
         JDataSet datasetFromCache = nst.cacheConfig.get(key);
        if (datasetFromCache != null) {
          logger.debug("Dataset from cache: " + nst.cacheConfig.isEnabled() + "/" + key);
          return datasetFromCache;
        }
      }
      int limit = nst.getMaxRows();
      // maxExe 1. default aus Statement
      long maxExecutionTime = nst.getMaxExecutionTime(); 
      if (list != null) {
         // Limit aus ParameterList, wenn dort angegeben
        if (list.getMaxRows() != Integer.MAX_VALUE) {
           limit = list.getMaxRows();
        }
        // maxExe 2. aus ParameterList, falls angegeben
        if (list.getMaxExecutionTime() != Integer.MAX_VALUE) {
           maxExecutionTime = list.getMaxExecutionTime();
        }
      }
      // maxExe 3. default aus Database wenn weder 1 noch 2
      if (maxExecutionTime  <= 0 || maxExecutionTime == Integer.MAX_VALUE) {
         maxExecutionTime = database.getDefaultMaxExecutionTime();
      }
      if (limit <=0) {
         limit = Integer.MAX_VALUE;
      }
      String sql = nst.getSql();
      String datasetname = nst.getDatasetName(); 
      final int queryTimeout = nst.getQueryTimeout();
      final DatabaseConnection con = this.getConnection(sql);
         try {
           long _startTime = System.currentTimeMillis();
            JDataSet ds = new JDataSet(datasetname);
            // ReadOnly setzen
            setReadOnly(con, true); // Postgres mag das nicht!

            // Ersetzen von Platzhaltern: ... WHERE ort LIKE $pattern AND plz IN ( $plz ) ...
            if (list != null && list.size() > 0) {
                sql = this.replaceToken(sql, list);
            }         
            // IsolationLevel abweichend?
            if (list != null && list.getIsolationLevel() != -1) {
                con.setIsolationLevel(list.getIsolationLevel());
            } else if (nst.getIsolationLevel() != -1) {
                con.setIsolationLevel(nst.getIsolationLevel());
            }                        
            if (limit != Integer.MAX_VALUE && this.database.hasLimit()) {
              sql += " LIMIT " + limit;
            }
            // Start Trans
            _startTransaction(TransactionManager.TRANS_INTERNAL_GETDATASET_STATEMENT, con);
            final PreparedStatement ps = con.getConnection().prepareStatement(sql);
            setParameter(ps, list);
            // Query Timneout
            Thread th = null;
            if (queryTimeout < 1 || queryTimeout == Integer.MAX_VALUE // nix machen;
                  || this.getDatabase().getDatabaseType() == Database.HSQLDB) { // HSQLDB kann kein QueryTimeout
            } else {
               ps.setQueryTimeout(queryTimeout); // MaxDB kennt QueryTimeout, kümmert sich aber nicht
               if (this.getDatabase().getDatabaseType() == Database.MAX_DB) {
                  th = new Thread(new Runnable() {
                     
                     @Override
                     public void run() {
                        try {
                           Thread.sleep(queryTimeout * 1000);
                           // http://stackoverflow.com/questions/295920/how-can-i-abort-a-running-jdbc-transaction
                           ps.cancel();
                        }
                        catch(InterruptedException e) {
                           logger.info(e.getMessage());
                        }
                        catch(SQLException e) {
                           logger.warn(e.getMessage());
                        } 
                     }
                  });
                  th.start();
               }
            }           
            ResultSet rs = ps.executeQuery(); // Statement ausführen; das kann dauern!
            if (th != null) { // ggf. vorhandenen Timeout-Thread abbrechen
               th.interrupt();
            }
            JDataTable tbl = new JDataTable(datasetname);
            tbl.setDatabaseName(con.getDatabaseName());
            ds.addRootTable(tbl);
            rs2Dataset(ds, tbl, rs, limit);
            ps.close();
            _commitTransaction(TransactionManager.TRANS_INTERNAL_GETDATASET_STATEMENT, con);
            // Slow Query
            long _dura = System.currentTimeMillis() - _startTime;
            Logger slowLog = database.getSlowQueryLogger();
            if (_dura > maxExecutionTime &&  slowLog != null) {
              String msg = "Statement name/SQL: " + nst.getName() + "/" + sql
              + " Parameter: " + list
              + " maxExecutionTime exceeded: " + _dura + ">" + maxExecutionTime;
              slowLog.warn(msg);
            }
            // Cache-put
            if (nst.hasCache() && ds != null /* && ds.getRowCount() > 0 */) {
              nst.cacheConfig.put(list.toString(), ds);
            }
            return ds;
         }
         catch(Exception ex) {
            throw handleException(
                  "getDatasetStatement(NamedStatement nst, ParameterList list) for dataset [ "
                        + datasetname + " ]", TransactionManager.TRANS_INTERNAL_GETDATASET_STATEMENT, con, ex);
         }
   }

   public JDataSet getDatasetStatement(String name) throws PLException {
     JDataSet ds = this.getDatasetStatement(name, null);
     return ds;
   }

   public JDataSet getDatasetStatement(String name, ParameterList list) throws PLException {
      NamedStatement nst = this.database.getStatement(name);
      return this.getDatasetStatement(nst, list);
   }

   public JDataSet getEmptyDataset(String datasetname) {
      Request req = this.getRequest(datasetname);
      try {
         JDataSet dataset = req.getEmptyDataset(datasetname);
         return dataset;
         //##return new JDataSet(dataset.getXml());
      }
      catch(Exception ex) {
         throw new IllegalStateException(ex.getMessage());
      }
   }

   /**
    * @throws PLException
    * @deprecated
    */
   public JDataSet getDatasetSql(String tablename, String columns, String from) throws PLException {
      DatabaseConnection con = this.getConnection("getDatasetSql: " + tablename);
         Request req = new Request(getDatabase(), tablename);
         try {
            // ReadOnly setzen
            setReadOnly(con, true);

            _startTransaction(TransactionManager.TRANS_INTERNAL_GETDATASETSQL, con);
            TableRequest treq = req.createRootTableRequest(con, tablename, columns);
            treq.createSelectStatement(from);
            JDataSet ds = req.getDataset(con, tablename);
            _commitTransaction(TransactionManager.TRANS_INTERNAL_GETDATASETSQL, con);
            return ds;
         }
         catch(Exception ex) {
            throw handleException(
                  "getDatasetSql(String tablename, String columns, String from) for table [ "
                        + tablename + " ]", TransactionManager.TRANS_INTERNAL_GETDATASETSQL, con,
                  ex);
         }
   }

   public JDataSet getDatasetSql(String datasetname, String sql) throws PLException {
      return this.getDatasetSql(datasetname, sql, null, Integer.MAX_VALUE, Integer.MAX_VALUE);
   }

   public JDataSet getDatasetSql(String datasetname, String sql, int limit) throws PLException {
     JDataSet ds = this.getDatasetSql(datasetname, sql, null, limit, Integer.MAX_VALUE);
     return ds;
   }

   public JDataSet getDatasetSql(String datasetname, String sql, ParameterList list)
         throws PLException {
      return this.getDatasetSql(datasetname, sql, list, Integer.MAX_VALUE, list.getQueryTimeout());
   }

   // TODO - funktioniert nur wenn "SELECT bla FROM blub WHERE bli = ? AND ..."
   public JDataSet getDatasetSql(String datasetname, String sql, ParameterList list, int limit, int queryTimeout)
         throws PLException {     
     NamedStatement nst = new NamedStatement(datasetname, sql);
     nst.setMaxRows(limit);
     nst.setQueryTimeout(queryTimeout);
     JDataSet ds = this.getDatasetStatement(nst, list);
     return ds;
   }
   
   /**
    * Achtung!
    * Das Expandieren eines ? zu einer Listen von ?,?,... ist gefährlich weil das darf nur genau einmal erfolgen!
    * @see AbstractStatement#setParameter
    * @param sql
    * @param list
    * @return
    */
   private String replaceToken(String sql, ParameterList list) {
   	/* Phase 1: ? expandieren, aber nicht '?'!
   	 * Spezialfall: Wenn in dem Statement ein '?' als konstantes Ergebnis (aber nicht als Parameter) enthalten ist!
   	 * SELECT '?' as unbekannt FROM MyTable WHERE id = ?; 
   	 * Das Teil hat nur einen Parameter! */
     StringBuilder buff = new StringBuilder();
     
     StringTokenizer toks1 = new StringTokenizer(sql, "?", true);
     int cnt = 0;
     Iterator<NVPair> it = list.iterator();
     String prevTok = " ";
     while (toks1.hasMoreTokens()) {
        String tok = toks1.nextToken();
        buff.append(tok);
        if (!prevTok.endsWith("'") && "?".equals(tok) && it.hasNext()) {            
           NVPair nv = it.next(); // Parameter weiterzählen
           if (nv.getValue() instanceof List<?>) {
              List<?> l = (List<?>)nv.getValue();
              if (l.size() > 1) {
                 for (int i = 1; i < l.size(); i++) {
                    buff.append(",?");
                 }
              }
           } else if (nv.getValue() instanceof long[]) {
              long[] l = (long[])nv.getValue();
              if (l.length > 1) {
                 for (int i = 1; i < l.length; i++) {
                    buff.append(",?");
                 }
              }
           }
           cnt++;
        }
        prevTok = tok;
     }
     
     sql = buff.toString();
     buff = new StringBuilder();
     
     if (sql.indexOf("$") == -1 ) return sql; // Keine Platzhalter drin (u.U. aber bereits ? mit der (hoffentlich!) richtigen Anzahl)    
     // Phase 2: Named Parameter
     StringTokenizer toks2 = new StringTokenizer(sql, " (),=\n\t\r", true);
     while (toks2.hasMoreTokens()) {
       String tok = toks2.nextToken();
       if (tok.startsWith("$")) {
          buff.append("?");
          String paraName = tok.substring(1);         
          NVPair nv = list.getParameter(paraName);
          Object paraValue = nv.getValue();
          if (paraValue instanceof List<?>) {
            List<?> paraList = (List<?>)paraValue;
            for(int i = 1; i < paraList.size(); i++) {
              buff.append(",?");
            }
          } else if (nv.getValue() instanceof long[]) {
             long[] l = (long[])nv.getValue();
             if (l.length > 1) {
                for (int i = 1; i < l.length; i++) {
                   buff.append(",?");
                }
             }
          }
       } else {
         buff.append(tok); // kein $
       }
     }
     return buff.toString();
   }

   public DataRowIterator getDataRowIteratorStatement(String statementName, ParameterList list)
         throws PLException {
      NamedStatement nst = this.database.getStatement(statementName);
      String sql = nst.getSql();
      return this.getDataRowIterator(nst.getDatasetName(), nst.getTableName(), sql, list);
   }

   public DataRowIterator getDataRowIterator(String datasetname, String tablename, String sql,
         ParameterList list) throws PLException {
      if (list != null && list.getMaxExecutionTime() == Integer.MAX_VALUE) {
         list.setMaxExecutionTime(database.getDefaultMaxExecutionTime());
      }
      DatabaseConnection con = this.getConnection(sql);
         try {
            JDataSet ds = new JDataSet(datasetname);
            // ReadOnly setzen
            setReadOnly(con, true);
            //##_startTransaction(TransactionManager.TRANS_INTERNAL_GETDATAROWITERATOR, con);
            PreparedStatement ps = con.getConnection().prepareStatement(sql);
            setParameter(ps, list);
            // TODO: maxExecutionTime
            ResultSet rs = ps.executeQuery();
            JDataTable tbl = new JDataTable(tablename);
            tbl.setDatabaseName(con.getDatabaseName());
            ds.addRootTable(tbl);
            // MetaDaten einlesen
            ResultSetMetaData meta = rs.getMetaData();
            // DataTable aus MetaDaten aufbauen
            rs2meta(meta, tbl, database.getDatabaseType());

            DataRowIterator dri = new DataRowIterator(ds, ps, rs, list);
            //##_commitTransaction(TransactionManager.TRANS_INTERNAL_GETDATAROWITERATOR, con);
            return dri;
         }
         catch(Exception ex) {
            throw handleException(
                  "getDataRowIterator(String datasetName, String sql, ParameterList list) for dataset [ "
                        + datasetname + " ]", TransactionManager.TRANS_INTERNAL_GETDATAROWITERATOR,
                  con, ex);
         }
   }

   public String pingDatabase() throws PLException {
      String pingStatement = this.database.getValidationQuery();
      if(pingStatement == null) {
         return "No PingStatement defined (see PLConfig.xml <ValidationQuery/>)";
      }
      @SuppressWarnings("unused")
      JDataSet ds = this.getDatasetSql("Ping", pingStatement);
      return "pong";
   }

   private void rs2Dataset(JDataSet dataset, JDataTable tbl, ResultSet rs, int limit)
         throws SQLException {
      try {
         // MetaDaten einlesen
         ResultSetMetaData meta = rs.getMetaData();
         // DataTable aus MetaDaten aufbauen
         rs2meta(meta, tbl, database.getDatabaseType());
         int cnt = 0;
         while(rs.next() && cnt < limit) {
            // Eine Row aus dem ResultSet wird in eine
            // ArrayList von JDataValues verwandelt.
            ArrayList<JDataValue> al = rs2ArrayList(rs, meta, tbl);
            // Row erzeugen
            JDataRow row = new JDataRow(tbl, al);
            // Neue Row an DataSet hängen
            dataset.addChildRow(row);
            cnt++;
         } // wend rs.next()
         rs.close();
      }
      finally {
         if(rs != null) {
            try {
               rs.close();
               rs = null;
            }
            catch(SQLException e) {
               logger.error("PL [ " + layerName
                     + " ] Method rs2Dataset: Error closing result set: " + e.getMessage());
            }
         }
      }
   }
   /**
    * DataTable aus MetaDaten aufbauen
    * @param meta
    * @param tbl
    * @throws SQLException
    */
   private static void rs2meta(ResultSetMetaData meta, JDataTable tbl, int databaseType) throws SQLException {
      for(int i = 1; i <= meta.getColumnCount(); i++) {
         String alias = meta.getColumnLabel(i);
//         String colName = meta.getColumnName(i);
//         if (databaseType == Database.POSTGRES) {            
//           String tmpColName = ((org.postgresql.PGResultSetMetaData)meta).getBaseColumnName(i);
//           if (tmpColName.length() > 0) {
//             colName = tmpColName;
//           }
//         }
         // Column erzeugen
         final int type = meta.getColumnType(i);
         // TODO: weitere Attribute aus Metadaten übernehmen (in Abhängigkeit vom Spalten-Typ)
         //int scale = meta.getScale(i);
         //int precis = meta.getPrecision(i);
         int size = meta.getColumnDisplaySize(i);
         try {
            /* TODO : Hier kann es passieren, daß ein ColumnName mehrfach vorkommt!
             * Z.B. dadurch, daß leichtsinning zwei Tabellen mit gleichen Spaltennamen ge-join-ed werden.
             * Ein Design-Fehler ist z.B. wenn man dem Foreign Key den selben Namen gibt wie dem Primary Key;
             * dann sind bei einem Join diese Spalten doppelt:
             * SELECT * FROM Parent P, Child C where P.id = C.id
             * Anderer Fall: doppelt vergebener Alias bei einem Statement: select name1 as name, name2 as name ...
             */
//            JDataColumn col = tbl.addColumn(colName, type);
//            if (!colName.equalsIgnoreCase(alias)) {
//               col.setAlias(alias);
//            }
            JDataColumn col = tbl.addColumn(alias, type);
            col.setSize(size);
            //col.setDecimalDigits(precis);
         }
         catch(IllegalArgumentException ex) {
           logger.warn(ex); // Doppelter Spalten-Name
         }
      }
   }

   /**
    * @see DataRowIterator
    * @param rs
    * @param meta
    * @param tbl
    * @return
    * @throws SQLException
    */
   public static ArrayList<JDataValue> rs2ArrayList(ResultSet rs, ResultSetMetaData meta, JDataTable tbl)
         throws SQLException {
      ArrayList<JDataValue> al = new ArrayList<JDataValue>(meta.getColumnCount());
      for(int i = 1; i <= meta.getColumnCount(); i++) {
         //final String colName = meta.getColumnName(i);
         final String colName = meta.getColumnLabel(i);
         try {
           JDataColumn col = tbl.getDataColumn(colName); // HSQLDB: Bei call fangen die Namen mit "@" an 
           int type = meta.getColumnType(i); // Aus Type Column?

           Object value;
           /*
            * Dieses switch ist notwendig, da Oracle-spezifische Klassen herausgegeben werden
            * Bei NUMBER(x,y) macht Oracle daraus immer ein BigDecimal
            */
           switch (type) {
             case Types.DATE:
               value = rs.getDate(i);
             break;
             case Types.TIME:
               value = rs.getTime(i);
               break;
             case Types.TIMESTAMP:
               value = rs.getTimestamp(i);
               break;
             case Types.BLOB:
            	value = rs.getBlob(i);
            	break;
             case Types.CLOB:
               value = rs.getClob(i); 
               break;
             case Types.LONGVARBINARY:
            	 value = rs.getBytes(i);
            	 break;
               default:
                value = rs.getObject(i); // kann auch null sein!
           }
           // Column
           String s = Convert.toString(value, col.getDataType());
           JDataValue val = new JDataValue(col, s);
           /*
            * KKN 28.04.2004 Wenn der Datentyp auf Binärdaten hinweist, dann
            * zusätzlich das Value-Object im DataValue ablegen.
            */
           if (JDataColumn.isBinaryType(col.getDataType())) {
             val.setObjectValue(value);
           }
           al.add(val);
           
         } catch (IllegalArgumentException ex) {
           System.err.println(ex.getMessage());
           logger.error(ex.getMessage(), ex);
         }
      }
      return al;
   }
   /**
   * Für CLOB, wenn vor Oracle 10g oder SetBigStringTryClob nicht gesetzt.
   * Sonst max 32k Zeichen
   * @param rs
   * @param columnIndex
   * @return
   * @throws SQLException
   */
  private static String getCLOB(ResultSet rs, int columnIndex, boolean onlyFirstChunk) throws SQLException {
    try {
      Clob lob = rs.getClob(columnIndex);
      if (lob != null) {
        BufferedReader lobReader = new BufferedReader(lob.getCharacterStream());
        StringBuilder buff = new StringBuilder();
        char[] cData = new char[1024];
        int bytesread = 0;
        if (onlyFirstChunk) {
          if ((bytesread = lobReader.read(cData,0,cData.length)) != -1 ) {
            buff.append(cData, 0, bytesread);
          }
        } else {
          while ((bytesread = lobReader.read(cData,0,cData.length)) != -1 ) {
            buff.append(cData, 0, bytesread);
          }
        }
        lobReader.close();
        return buff.toString();
      }
    } catch (SQLException e) {
      throw e;
    }
    catch (IOException ie) {
      // TODO
      throw new SQLException(ie);
    }
    return null;
  }
  private static byte[] getBLOB(ResultSet rs, int columnIndex, boolean onlyFirstChunk) throws SQLException{
    byte[] blobArray = null;
    try {
      Blob lob = rs.getBlob(columnIndex);
      if (lob != null) {
        InputStream inputStream = lob.getBinaryStream();
        BufferedInputStream lobReader = new BufferedInputStream(inputStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        byte[] binaryData = new byte[1024];
        int bytesread = 0;
        if (onlyFirstChunk) {
          if ((bytesread = lobReader.read(binaryData,0,binaryData.length)) != -1) {
            baos.write(binaryData, 0, bytesread);
          }
        } else {
          while ((bytesread = lobReader.read(binaryData,0,binaryData.length)) != -1) {
            baos.write(binaryData,0,bytesread);
          }
        }
        blobArray = baos.toByteArray();
        lobReader.close();
      }
    } catch (SQLException e) {
      throw e;
    } catch (IOException e) {
      // TODO
      throw new SQLException(e);
    }
    return blobArray;
  }

   /*
    * TODO Haben wir irgendeine Chance, daß wir hier mehr Parameter übergeben
    * und das Statement sich nur die Werte rauspickt, die es wirklich benötigt??
    * @param stmt @param list @throws SQLException
    */
   private static void setParameter(PreparedStatement stmt, ParameterList list) throws SQLException {
      if(list == null)
         return; // Wenn nix zu tun, dann auch nix machen

      Iterator<NVPair> i = list.iterator();
      int cnt = 1;
      while(i.hasNext()) {
         NVPair nv = i.next();
         // TODO Prüfen/Testen: Geht das für alle Datentypen gut?
         switch(nv.getDataType()) {
            case Types.DATE: {
               java.sql.Date d = null;
               Object o = nv.getValue();
               if(o instanceof java.util.Date) {
                  java.util.Date ud = (java.util.Date)o;
                  d = new Date(ud.getTime());
               }
               else {
                  d = Convert.toSqlDate(nv.getValue().toString());
                  if(Convert.getLastException() != null) {
                     logger.error("Cant convert from '" + nv.getValue() + "' to java.sql.Date",
                           Convert.getLastException());
                     throw new SQLException("Cant convert from '" + nv.getValue()
                           + "' to java.sql.Date\n" + Convert.getLastException().getMessage());
                  }
               }
               stmt.setDate(cnt, d);
            }
               break;
            case Types.BIGINT: {
              long l;
              Object paraValue = nv.getValue();
              if (paraValue instanceof List<?>) {
                List<?> paraList = (List<?>)paraValue;
                if (paraList.size() == 0) {
                   stmt.setNull(cnt, Types.BIGINT);
                   cnt++;
                } else {
                   for (int p = 0; p < paraList.size(); p++) {
                     Object paraListValue = paraList.get(p);
                     if (paraListValue instanceof Long) {
                       l = ((Long)paraListValue).longValue();
                     } else {
                        l = Convert.toLong(paraListValue);
                     }
                     stmt.setLong(cnt, l);
                     cnt++; 
                   }
                }
                cnt--; // HACK; wird unten wieder hochgezählt!
              } else { // keine List<?>
                if (nv.getValue() instanceof Long) {
                  l = ((Long)paraValue).longValue();
               } else {
                  l = Convert.toLong(paraValue);
               }
               stmt.setLong(cnt, l);
              }
            }
               break;
            case  Types.BLOB:
               stmt.setObject(cnt, nv.getValue()); // Postgres mag hier kein Types.BLOB sondern nur ein byte[]
            	break;
               
            default:
              // Value von NPPair ist vom Typ List<?>
              Object paraValue = nv.getValue();
              if (paraValue instanceof List<?>) {
                List<?> paraList = (List<?>)paraValue;
                if (paraList.size() == 0) {
                   stmt.setNull(cnt, nv.getDataType());
                   cnt++;
                } else {
                   for (int p = 0; p < paraList.size(); p++) {
                     Object paraListValue = paraList.get(p);
                     Object o = Convert.toObject(paraListValue.toString(), nv.getDataType());
                     stmt.setObject(cnt, o, nv.getDataType());
                     cnt++; 
                   }
                }
                cnt--; // HACK; wird unten wieder hochgezählt!
              } else {
                 if (nv.getValue() == null) {
                    stmt.setNull(cnt,  nv.getDataType());                    
                 } else {
                    stmt.setObject(cnt, nv.getValue(), nv.getDataType());
                 }
              }
         }
         cnt++;
      }
   }

   // ============ ENDE SQL =======================
   public int setDataset(List<JDataSet> datasets) throws PLException {
      if (datasets.size() == 0)
         return 0;
      int anz = 0;
      String transname = TransactionManager.TRANS_INTERNAL_SETDATASET_LIST;// TODO eindeutiger Name!
      DatabaseConnection con = this.getConnection(transname);
      try {
         // ReadOnly false setzen
         setReadOnly(con, false);
         _startTransaction(transname, con);
         for(JDataSet dataset: datasets) { // Schleife Datasets
            String datasetname = dataset.getDatasetName();
            Request req = this.getRequest(datasetname);
          int cnt = req.setDataset(con, dataset);
          anz += cnt;
         }
         _commitTransaction(transname, con);
      } catch(Exception ex) {
         throw handleException("setDataset(List<JDataSet> datasets)", transname, con, ex);
      }
      // Update-Protokoll?
      for(JDataSet dataset: datasets) {
         if(this.database.hasJournal()) {
            this.database.getDatabaseJournal().journalDataset(dataset);
         }
         // Änderungsprotocol
         if(dataset.hasChangeProtocol()) {
            DatabaseConnection conp = null;
            transname = TransactionManager.TRANS_INTERNAL_PROTOCOL;
            try {
               String datasetname = dataset.getDatasetName();
               Request req = this.getRequest(datasetname);
               conp = this.getConnection(datasetname);
               _startTransaction(transname, conp);
               int anzp = req.createChangeProtocol(this, conp, dataset);
               _commitTransaction(transname, conp);
               logger.debug(dataset.getDatabaseName() + " Protocol: " + anzp);
            }
            catch(Exception ex) {
               // Nix machen, wenn Changeprotocol fehlt
               if (conp != null) {
                  try {
                     conp.rollbackTransaction(transname);
                  } catch (Exception eex) {
                     logger.debug(eex.getMessage(), eex);
                  }
               }
               logger.debug(ex.getMessage(), ex);
            }
         }
      }
      return anz;
   }
   
   public int setDataset(JDataSet dataset) throws PLException {

     String datasetname = dataset.getDatasetName();
     DatabaseConnection con = this.getConnection(datasetname);
      Request req = this.getRequest(datasetname);
      String transname = TransactionManager.TRANS_INTERNAL_SETDATASET;
      try {
         // ReadOnly false setzen
         //## setReadOnly(con, false); // Postgres mag das nicht
         // IsolationLevel abweichend?               
         if (req.getIsolationLevel() != -1) {
           con.setIsolationLevel(req.getIsolationLevel());
         }               

         _startTransaction(transname, con);
         int cnt = req.setDataset(con, dataset);
         _commitTransaction(transname, con);
         // Update-Protokoll?
         if (this.database.hasJournal()) {
           this.database.getDatabaseJournal().journalDataset(dataset);
         }
         // Änderungsprotocol
         if (dataset.hasChangeProtocol()) {
         	DatabaseConnection conp = null;
           try {            
             conp = this.getConnection(datasetname);
             transname = TransactionManager.TRANS_INTERNAL_PROTOCOL;
             _startTransaction(transname, conp);
             int anzp = req.createChangeProtocol(this, conp, dataset);
             _commitTransaction(transname, conp);
             logger.debug(dataset.getDatabaseName() + " Protocol: " + anzp);
           }
           catch (Exception ex) {
             // Nix machen, wenn Changeprotocol fehlt
             logger.debug(ex.getMessage());
             if (conp != null) {
            	 conp.rollbackTransaction(transname);
             }
           }
         }
         return cnt;
      }
      catch(Exception ex) {
         throw handleException("setDataset(JDataSet dataset) for dataset [ "
               + dataset.getDatasetName() + " ]", transname, con, ex);
      }
   }
   /**
    * Nur bei PL verfügbar
    * @param dataset
    * @throws PLException
    */
   public void setDatasetAsync(JDataSet dataset) throws PLException {
     throw new IllegalStateException("Async Dataset Writer not available!");
    }

   public int setDataset(String dataset) throws PLException {
      try {
         Document doc = new Document(dataset);
         JDataSet ds = new JDataSet(doc);
         return this.setDataset(ds);
      }
      catch(PLException ex) {
         throw ex;
      }
      catch(Exception ex) {
      	String msg = "PL [ " + layerName + " ] Error parsing dataset xml document: " + ex.getMessage();
      	logger.error(msg, ex);
        throw new PLException(msg, ex);
      }
   }
   
//   int importJournal(File file) throws PLException {
//     int anz = 0;
//     try {
//       FileReader fr = new FileReader(file);
//       char[] buff = new char[(int)file.length()];
//       int size = fr.read(buff);
//       fr.close();
//       String s = new String(buff);
//       String filename = file.getName();
//       if (filename.endsWith(".dataset")) {
//         anz = this.setDataset(s);
//       } else if (filename.endsWith(".sql")) {
//         try {
//          Document doc = new Document(s);
//          Element eRoot = doc.getRoot();
//          Element eSql = eRoot.getElement("Statement");
//          String sql = eSql.getTextString();
//          Element eParams = eRoot.getElement("Parameter");
//          ParameterList parameters = null;
//          if (eParams != null) {
//            parameters = new ParameterList();
//            Elements eParas = eParams.getElements();
//            while (eParas.hasMoreElements()) {
//              Element eParam = eParas.next();
//              String name = eParam.getAttribute("name");
//              String value = eParam.getAttribute("value");
//              int type = Convert.toInt(eParam.getAttribute("type"));
//              parameters.addParameter(name, value, type);
//            }
//          }
//          this.executeSql(sql, parameters);
//        } catch (ParseException ex) {
//          throw new PLException(ex.getMessage());
//        }
//       } else if (filename.endsWith(".transaction")) {
//         try {
//           Document doc = new Document(s);
//           Element eRoot = doc.getRoot();
//           Element eSql = eRoot.getElement("Transaction");
//           String sql = eSql.getTextString();
//           if (sql.equalsIgnoreCase(PL.START_TRANSACTION)) {
//             this.executeSql("SET AUTOCOMMIT FALSE", null);
//           } else if (sql.equalsIgnoreCase(PL.COMMIT)) {
//             this.executeSql("COMMIT", null);
//           } else if (sql.equalsIgnoreCase(PL.ROLLBACK)) {
//             this.executeSql("ROLLBACK", null);
//           }
//         } catch (ParseException ex) {
//           throw new PLException(ex.getMessage());
//         }
//       }
//     } catch (IOException ex) {
//       throw new PLException(ex.getMessage());
//     }
//     return anz;
//   }

   public int importJournal(List<Document> docs) throws PLException {
     int anz = 0;
     try {
       for (Document doc: docs) {
         Element root = doc.getRoot();
         String sRoot = root.getName();
         if (sRoot.equalsIgnoreCase("JDataSet")) {
           JDataSet _ds = new JDataSet(doc);
           /* TODO: Wenn hier ein Lockkonflikt auftritt, dann Versionszähler um eins erhöhen
            * damit ggf. nachfolgende Dataset zur selben Tabelle auch einen Lockkonflikt erhalten.
            * Es kann aber auch sein, daß der Datensatz gelöscht wurde (dann bleibt jedes Update sowieso folgenlos).
            */
           try {
             anz += this.setDataset(_ds);
           } catch (PLException pex) {
             if (pex.isOptimistic()) {
               try {
                 JDataRow row = pex.getRow();
                 if (row != null) {
                 String colname = database.getOptimisticField();
                 JDataTable tbl = row.getDataTable();
                 if (tbl.hasDataColumn(colname)) {
                   int oldVal = row.getValueInt(colname);
                   //int newVal = oldVal++;
                   String pkName = tbl.getPKs();
                   // TODO: Mehrere Felder als PK?
                   String pkVal = row.getValue(pkName);
                   String sql = "UPDATE "  + tbl.getTablename() + " SET " + colname + " = " + colname + " + 1"
                   + " WHERE " + pkName + " = ? AND " + colname + " = ?";
                   ParameterList list = new ParameterList();
                   //list.addParameter(colname, newVal);
                   list.addParameter(pkName, pkVal);
                   list.addParameter(colname, oldVal);
                   logger.debug(sql + '\n' + list);
                   PLTransactionContext ctx = new PLTransactionContext(database, dbConnectionPool, "Inc Optimistic Lock Counter", true);
                   int anzu = ctx.executeSql(sql, list);
                   logger.debug(anzu);
                   throw pex;
                 }
                 }
               } catch (Exception ex) {
                 logger.error(ex.getMessage(), ex);
                 throw new PLException(ex);
               }
             }
           }
         } else if (sRoot.equalsIgnoreCase("Statement")) {
           Element eSql = root.getElement("SQL");
           String sql = eSql.getTextString();
           Element eParams = root.getElement("Parameter");
           ParameterList parameters = null;
           if (eParams != null) {
             parameters = new ParameterList();
             Elements eParas = eParams.getElements();
             while (eParas.hasMoreElements()) {
               Element eParam = eParas.next();
               String name = eParam.getAttribute("name");
               String value = eParam.getAttribute("value");
               int type = Convert.toInt(eParam.getAttribute("type"));
               parameters.addParameter(name, value, type);
             }
           }
           anz += this.executeSql(sql, parameters);
           
         } else if (sRoot.equalsIgnoreCase("Transaction")) {         
           String val = root.getTextString();
           String transname = "**importJournal**";
           String t = root.getAttribute("name");
           if (t != null && t.length() > 0) transname = t;
           if (val.equalsIgnoreCase(de.pkjs.pl.PL.START_TRANSACTION)) { 
             this.startTransaction(transname);
           } else if (val.equalsIgnoreCase(de.pkjs.pl.PL.COMMIT)) {
             boolean committed = this.commitTransaction(transname);
             // ist nie committed, weil ist schon innerhalb einer Transaktion!
//             if (!committed) {
//               logger.warn("Transaction '" + transname + "' not commited!?");
//             }
           } else if (val.equalsIgnoreCase(de.pkjs.pl.PL.ROLLBACK)) {
             boolean rollbacked = this.rollbackTransaction(transname);
           }
         }
       } // end for
       return anz;
     } catch (PLException ex) {
       logger.error(ex.getMessage(), ex);
       throw ex;
     }
   }

   public void clearCache(String datasetname) throws PLException {
     try {
       Request req = this.getRequest(datasetname);
       if (req == null) {
         logger.warn("Non-existing datasetname: " + datasetname);
         return;
       }
//       req.getCacheConfig().cache.removeAll();
//       logger.info("Cache cleared: " + datasetname);
     } catch (Exception ex) {
       logger.error(ex.getMessage(), ex);
       throw new PLException(ex);
     }
   }
   
   public JDataSet getAll(String datasetname) throws PLException {
      DatabaseConnection con = this.getConnection(datasetname);
      // Request
      Request req = this.getRequest(datasetname);
      try {
         // ReadOnly setzen
         setReadOnly(con, true);
         // IsolationLevel abweichend?
         if (req.getIsolationLevel() != -1) {
           con.setIsolationLevel(req.getIsolationLevel());
         }               
         this._startTransaction(TransactionManager.TRANS_INTERNAL_ALL, con);
         JDataSet dataset = req.getDataset(con, datasetname);
         this._commitTransaction(TransactionManager.TRANS_INTERNAL_ALL, con);
         // Cache put
         RequestCacheConfig cacheConfig = req.getCacheConfig(); 
         if (cacheConfig != null && cacheConfig.isEnabled() && dataset.getRowCount() > 1) {
           Vector<JDataSet> vds = dataset.splitMulti2Single();
           Iterator<JDataSet> it = vds.iterator();
           while(it.hasNext()) {
             JDataSet dsCache = it.next();
             JDataRow row = dsCache.getRow();
             JDataColumn pkcol = row.getDataTable().getPKColumn();
             long oid = -1;
             boolean boid = false;
             switch (pkcol.getDataType()) {
               case Types.TINYINT:
               case Types.SMALLINT:
               case Types.BIGINT:
               case Types.INTEGER:
               oid = row.getValueLong(pkcol.getColumnName());
               boid = true;
             }
             if (boid) {
               cacheConfig.put(oid, dsCache);
             }
           }
         }
         return dataset;
      }
      catch(Exception ex) {
         throw handleException("getAll(String datasetname) for dataset [ " + datasetname
               + " ]", TransactionManager.TRANS_INTERNAL_ALL, con, ex);
      }
   }

   public long getOID() throws PLException {
      String _transName = "PLTrans#getOID";
      DatabaseConnection con = this.getConnection(_transName);
      try {
     	 	switch(this.database.getDatabaseType()) {
     	 		case Database.POSTGRES: // ab Postgres 9.0 können Sequences nicht mehr mit einer readonly Connection verwendet werden
     	 		case Database.MYSQL: // 24.1.2012 Mysql auch nicht (wegen UPDATE)
     	 		case Database.MARIA: // 27.2.2018 vorsichtshalber wie mysql 
     	 			this.setReadOnly(con, false);         
     	 			break;
     	 		default:
     	 			this.setReadOnly(con, true);        	 
     	 	}
     	 	this._startTransaction(_transName, con);
         long newID = con.getOID(true);
         this._commitTransaction(_transName, con);
         return newID;
      }
      catch(PLException ex) {
         throw handleException("getOID()", _transName, con, ex);
      }
   }

   public long getOID(String sequenceName) throws PLException {
      String _transName = "getOID:" + sequenceName;
      DatabaseConnection con = this.getConnection(_transName);
         try {
            this._startTransaction(_transName, con);
            long newID = con.getNextSequence(sequenceName);
            this._commitTransaction(_transName, con);
            return newID;
         }
         catch(Exception ex) {
            throw handleException("getOID(String sequenceName)", _transName, con, ex);
         }
   }

   /**
    * Führt ein beliebiges (NonQuery) SQL-Statement aus, welches kein Ergebnis
    * liefern sollte.
    * @param sqlCommand
    * @return Anzahl geänderter Datensätze oder 0
    * @throws PLException
    */
   public int executeSql(String sqlCommand) throws PLException {
      return executeSql(sqlCommand, null);
   }

   public int executeSql(String sqlCommand, ParameterList parameters) throws PLException {
      PreparedStatement ps = null;
      DatabaseConnection con = this.getConnection(sqlCommand);
       try {
          long _startTime = System.currentTimeMillis();
          this.setReadOnly(con, false);
          if (parameters != null && parameters.size() > 0) {
             sqlCommand = this.replaceToken(sqlCommand, parameters);
         }         

          this._startTransaction(TransactionManager.TRANS_INTERNAL_EXECUTE_SQL, con);
          ps = con.getConnection().prepareStatement(sqlCommand);
          long maxExecutionTime = database.getDefaultMaxExecutionTime();
          if(parameters != null) {
             setParameter(ps, parameters);
             if (parameters.getMaxExecutionTime() != Integer.MAX_VALUE) {
               maxExecutionTime = parameters.getMaxExecutionTime();
             }
             // IsolationLevel abweichend?
             if (parameters.getIsolationLevel() != -1) {
               con.setIsolationLevel(parameters.getIsolationLevel());
             }
          }
          int count = ps.executeUpdate();
          ps.close();
          this._commitTransaction(TransactionManager.TRANS_INTERNAL_EXECUTE_SQL, con);
          // Slow Query Logger
          long _dura = System.currentTimeMillis() - _startTime;
          if (_dura > maxExecutionTime) {
             Logger slowLog = database.getSlowQueryLogger();
             if (slowLog != null) {
               String msg = "Statement: " + sqlCommand
               + "\n Paramter: " + parameters
               + "\n maxExecutionTime exceeded: " + _dura + ">" + maxExecutionTime;
               slowLog.warn(msg);
             }
          }
          // TODO
          if (database.hasJournal()) {
            this.database.getDatabaseJournal().journalStatement(sqlCommand, parameters);
          }
          return count;
       }
       catch(Exception ex) {
          throw handleException(
                "executeSql(String sqlCommand, ParameterList parameters) for sql command [ "
                      + sqlCommand + " ]", TransactionManager.TRANS_INTERNAL_EXECUTE_SQL, con, ex);
       }
       finally {
          if(ps != null) {
             try {
                ps.close();
             }
             catch(SQLException e) {
                logger.error("PL [ " + layerName
                      + " ] Method executeSql: Error closing prepared statement: "
                      + e.getMessage(), e);
             }
          }
       }
   }

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
    * @param sqlCommand
    * @param parameter
    * @return Anzahl geänderter Datensatze oder 0
    * @throws PLException
    */
   public int executeSqlPara(String sqlCommand, Vector<Object> parameter) throws PLException {
      PreparedStatement stmt = null;
      DatabaseConnection con = this.getConnection(sqlCommand);
         try {
            setReadOnly(con, false);
            _startTransaction(TransactionManager.TRANS_INTERNAL_EXECUTE_SQL_PARA, con);
            stmt = con.getConnection().prepareStatement(sqlCommand);
            Iterator<Object> it = parameter.iterator();
            int i = 0;
            while(it.hasNext()) {
               Object o = it.next();
               i++;
               if(o instanceof java.sql.Date) {
                  stmt.setDate(i, (java.sql.Date)o);
               }
               else if(o instanceof java.util.Date) {
                  java.util.Date ud = (java.util.Date)o;
                  java.sql.Date sd = Convert.toSqlDate(ud);
                  stmt.setDate(i, sd);
               }
               else {
                  stmt.setObject(i, o);
               }
            }
            long _startTime = System.currentTimeMillis();
            int cnt = stmt.executeUpdate();
            _commitTransaction(TransactionManager.TRANS_INTERNAL_EXECUTE_SQL_PARA, con);
            long _dura = System.currentTimeMillis() - _startTime;
            long maxExecutionTime = database.getDefaultMaxExecutionTime();
            Logger slowLog = database.getSlowQueryLogger();
            if (_dura > maxExecutionTime &&  slowLog != null) {
              String msg = "Statement: " + sqlCommand + " maxExecutionTime exceeded: " + _dura + ">" + maxExecutionTime;
              slowLog.warn(msg);
            }
            return cnt;
         }
         catch(Exception ex) {
            throw handleException(
                  "executeSqlPara(String sqlCommand, Vector parameter) for sql command [ "
                        + sqlCommand + " ]", TransactionManager.TRANS_INTERNAL_EXECUTE_SQL_PARA,
                  con, ex);
         }
         finally {
            if(stmt != null) {
               try {
                  stmt.close();
               }
               catch(Exception ex) {

               }
            }
         }
   }

   public int executeStatement(String name) throws PLException {
      NamedStatement nst = this.database.getStatement(name);
      int cnt = this.executeSql(nst.getSql());
      return cnt;
   }

   public int executeStatement(String name, ParameterList parameters) throws PLException {
      NamedStatement nst = this.database.getStatement(name);
      //##String sql = nst.getSql(parameters);
      String sql = nst.getSql();
      int cnt = this.executeSql(sql, parameters);
      return cnt;
   }

   public int executeBatchStatement(String name) throws PLException {
      int cnt = this.executeBatchStatement(name, null);
      return cnt;
   }

   public int executeBatchStatement(String name, ParameterList list) throws PLException {
      int ret = 0;
      BatchStatement bst = this.database.getBatchStatement(name);
      ArrayList<NamedStatement> al = bst.getStatements();
      int size = al.size();
      for(int i = 0; i < size; i++) {
         NamedStatement nst = al.get(i);
         int cnt = 0;
         if(list != null) {
            ParameterList stmtList = nst.extractParameters(list);
            // TODO: addBatch
            cnt = this.executeSql(nst.getSql(), stmtList);
         }
         else {
            cnt = this.executeSql(nst.getSql());
         }
         ret = ret + cnt;
      }
      return ret;
   }
   
   private void _startTransaction(String _transName, DatabaseConnection con) throws PLException {
      con.startTransaction(_transName);
      this.startedTransactions++;
      TransactionInfo tInfo = new TransactionInfo(con);
      this.database.putTransactionInfo(tInfo);
   }

   private boolean _testCommit(DatabaseConnection con) {
         return con.testCommit();
   }

   private boolean _commitTransaction(String _transName, DatabaseConnection con) throws PLException {
         try {
            con.commitTransaction(_transName);
            this.commitedTransactions++;
            return con.isCommited();
         }
         finally {
            if(con.isCommited() || con.isRollback()) { // wozu isRollback?
               // IKU die Connection muss freigegeben werden
               releaseConnection(con);
               if(!this.isAutocommit) {
                  this.isDisabled = true;
               }
            }
            this.database.removeTransactionInfo(con.getId());
         }
   }

   private boolean _rollbackTransaction(String _transName, DatabaseConnection con) throws PLException {
     if (con == null) {
       logger.warn("Cannot rollback Transaction ["+ _transName + "]; Connection is null (allready invalidated?)");
       return false;
     }
    	boolean rollbacked = false;
      try {
          rollbacked = con.rollbackTransaction(_transName);
      }
      finally {
          this.rollbackedTransaktions++;
          if(con.isCommited() || con.isRollback()) {
             // IKU die Connection muss freigegeben werden
             releaseConnection(con);
             if(!isAutocommit) {
                this.isDisabled = true;
             }
          }
          this.database.removeTransactionInfo(con.getId());
       }
       return rollbacked;
   }

   private void _abortTransaction(String _transName, DatabaseConnection con) throws PLException {
     if (con == null) {
       logger.warn("Cannot abort Transaction ["+ _transName + "]; Connection is null (allready invalidated?)");
       return;
     }
       con.abortTransaction(_transName);
       this.abortedTransactions++;
       //       IKU die Connection muss freigegeben werden
       invalidateConnection(con);
       if(!this.isAutocommit) {
          this.isDisabled = true;
       }
   }

   public  void startTransaction(String _transName) throws PLException {
      if(this.isAutocommit) {
         throw new PLException(
               "PL [ "
                     + layerName
                     + " ] custom transactions are not supported in auto commit mode!"
                     + "\nUse pl.createNewTransaction() to get a neu transaction context for custom transaction mode.");
      }
      DatabaseConnection con = this.getConnection("Start Transaction: " + _transName);
      this._startTransaction(_transName, con);
   }

   public boolean testCommit() throws PLException {
      if(isAutocommit) {
         throw new PLException(
               "PL [ "
                     + layerName
                     + " ] custom transactions are not supported in auto commit mode!"
                     + "\nUse pl.createNewTransaction() to get a neu transaction context for custom transaction mode.");
      }
      return _testCommit(this.dbConnection); // ??
   }

   public boolean commitTransaction(String _transName) throws PLException {
      if(isAutocommit) {
         throw new PLException(
               "PL [ "
                     + layerName
                     + " ] custom transactions are not supported in auto commit mode!"
                     + "\nUse pl.createNewTransaction() to get a neu transaction context for custom transaction mode.");
      }
      if (_transName == null) {
         _transName = this.transName;
      }
      return this._commitTransaction(_transName, this.dbConnection);
   }

   public boolean rollbackTransaction(String _transName) throws PLException {
      if(isAutocommit) {
         throw new PLException(
               "PL [ "
                     + layerName
                     + " ] custom transactions are not supported in auto commit mode!"
                     + "\nUse pl.createNewTransaction() to get a neu transaction context for custom transaction mode.");
      }
      if (_transName == null) {
         _transName = this.transName;
      }
      boolean  rollbacked = this._rollbackTransaction(_transName, this.dbConnection); 
      return rollbacked;
   }
   public IPLContext startNewTransaction(String _transName) throws PLException {
     PLTransactionContext plc = new PLTransactionContext(this.database, this.dbConnectionPool, _transName, false);
     plc.startTransaction(_transName);
     return plc;
   }

   public String getDatasetDefinitionFileName() {
      return getDatabase().getDatasetDefinitionFileName();
   }

   /**
    * @return
    */
   public SimpleDateFormat getDateFormat() {
      return getDatabase().getDateFormat();
   }

   /**
    * @return
    */
   public SimpleDateFormat getTimeFormat() {
      return getDatabase().getTimeFormat();
   }

   /**
    * @return
    */
   public SimpleDateFormat getTimestampFormat() {
      return getDatabase().getTimestampFormat();
   }

   public static JDataTable getMetaDataTable() {
      if(metaDataTable == null) {
         metaDataTable = new JDataTable("MetaPL");
         metaDataTable.setDatabaseName(JDataTable.META_DATABASE_NAME);
         metaDataTable.addColumn("LayerName", Types.VARCHAR);
         JDataColumn cName = metaDataTable.addColumn("DatasetDefinitionFile", Types.VARCHAR);
         cName.setNullable(false);
         cName.setPrimaryKey(true);
         cName.setKeySeq(1);
         // Format
         metaDataTable.addColumn("DateFormat", Types.VARCHAR);
         metaDataTable.addColumn("TimeFormat", Types.VARCHAR);
         metaDataTable.addColumn("TimestampFormat", Types.VARCHAR);
         metaDataTable.addColumn("DecimalFormat", Types.VARCHAR);
         // Dataset
         metaDataTable.addColumn("Encoding", Types.VARCHAR);
         metaDataTable.addColumn("ElementName", Types.VARCHAR);
         // Options
         metaDataTable.addColumn("Debug", Types.VARCHAR);
         // WebService
         metaDataTable.addColumn("WebServiceURL", Types.VARCHAR);
         metaDataTable.addColumn("WebServiceName", Types.VARCHAR);
         // RMI
         JDataColumn cPort = metaDataTable.addColumn("RmiPort", Types.INTEGER);
         cPort.setDefaultValue("1099");
         metaDataTable.addColumn("RmiServiceName", Types.VARCHAR);
      }
      return metaDataTable;
   }

   /**
    * Liefert eine DataRow mit den Eigenschaften des PL.
    * @return
    */
   public JDataRow getMetaDataRow() {
      JDataRow row = getMetaDataTable().createNewRow();
      row.setValue("LayerName", getDatabase().getLayerName());
      row.setValue("DatasetDefinitionFile", getDatabase().getDatasetDefinitionFileName());
      row.setValue("DateFormat", getDatabase().getDateFormat().toPattern());
      row.setValue("TimeFormat", getDatabase().getTimeFormat().toPattern());
      row.setValue("TimestampFormat", getDatabase().getTimestampFormat().toPattern());
      row.setValue("DecimalFormat", getDatabase().getDecimalFormat().toPattern());
      row.setValue("Encoding", getDatabase().getEncoding());
      row.setValue("ElementName", getDatabase().getDatasetElementName());
      row.setValue("Debug", getDatabase().isDebug());
      //		row.setValue("WebServiceURL", this.url );
      //		row.setValue("WebServiceName", this.service );
      //		row.setValue("RmiPort", this.rmiPort );
      //		row.setValue("RmiServiceName", this.rmiService );
      //row.commitChanges();
      return row;
   }

   /**
    * Liefert einen Dataset, der alle definierten MetaDaten des Persistenz
    * Layers umfaßt:
    * <ul>
    * <li>Attribute der Klasse PL
    * <li>Attribute der Klasse Database (als Child von PL)
    * <ul>
    * <li>Named Sequences
    * <li>DataTables
    * </ul>
    * <li>Attribute der Klasse Request (als Child von PL)
    * <li>Attribute der Klasse TableRequest (als Child von Request und mit
    * Selbst-Referenzierung)
    * <li>Attribute der Klasse JDataColumn (als Child von TableRequest)
    * </ul>
    * @return
    */
   public JDataSet getMetaDataSet() {
      JDataSet ds = new JDataSet("PersistenceLayer");
      // Schema ##########################
      // PL
      JDataTable rootTbl = PLTransactionContext.getMetaDataTable();
      ds.addRootTable(rootTbl);
      // Database
      JDataTable dbTbl = Database.getMetaDataTable();
      // db aber ok
      rootTbl.addChildTable(dbTbl, dbTbl.getPKs());
      // Request
      JDataTable reqTbl = Request.getMetaDataTable();
      rootTbl.addChildTable(reqTbl, reqTbl.getPKs());
      // TableRequest
      JDataTable tblReq = TableRequest.getMetaDataTable();
      reqTbl.addChildTable("RootTables", tblReq, "DatasetName");
      // Child and Parent Request
      tblReq.addChildTable("ChildTables", tblReq.cloneTable(true), "FK_TableRequestId");
      tblReq.addChildTable("ParentTables", tblReq.cloneTable(true), "FK_TableRequestId");
      // Column ist schon bei TableRequest
      // Data ##############################
      // PL
      JDataRow rootRow = this.getMetaDataRow();
      ds.addChildRow(rootRow);
      // DataBases
      {
         JDataRow dbRow = database.getMetaDataRow();
         rootRow.addChildRow(dbRow);
      }
      // Requests
      {
         ArrayList<String> al = this.getDatasetNames();
         for(Iterator<String> i = al.iterator(); i.hasNext();) {
            String reqName = i.next();
            Request req = this.getRequest(reqName);
            JDataRow reqRow = req.getMetaDataRow();
            rootRow.addChildRow(reqRow);
            // Root TableRequest
            TableRequest rootReq = req.getRootTableRequest();
            JDataRow rootReqRow = rootReq.getMetaDataRow();
            reqRow.addChildRow(rootReqRow);
            // Child and Parent Request; Rekursiv!!
            this.createTableRequests(rootReq, rootReqRow);
         }
      }
      return ds;
   }

   //	private void setMetaDataRow(JDataRow row) {
   //		this.layerName = row.getValue("LayerName");
   //		this.datasetDefinitionFileName = row.getValue("DatasetDefinitionFile");
   //		// Formats
   //		try {
   //			dateFormat =
   //			new SimpleDateFormat(row.getValue("DateFormat"));
   //		} catch (Exception ex) {
   //			System.err.println("PL#setMetaDataRow: Missing DateFormat");
   //		}
   //		try {
   //			timeFormat =
   //			new SimpleDateFormat(row.getValue("TimeFormat"));
   //		} catch (Exception ex) {
   //			System.err.println("PL#setMetaDataRow: Missing TimeFormat");
   //		}
   //		try {
   //			timestampFormat =
   //			new SimpleDateFormat(row.getValue("TimestampFormat"));
   //		} catch (Exception ex) {
   //			System.err.println("PL#setMetaDataRow: Missing TimestampFormat");
   //		}
   //		try {
   //			decimalFormat =
   //			new DecimalFormat(row.getValue("DecimalFormat"));
   //		} catch (Exception ex) {
   //			System.err.println("PL#setMetaDataRow: Missing DecimalFormat");
   //		}
   //		this.encoding = row.getValue("Encoding");
   //		this.datasetElementName = row.getValue("ElementName");
   //		debug = row.getValueBool("debug");
   //		this.url = row.getValue("WebServiceUrl");
   //		this.service = row.getValue("WebServiceName");
   //		this.rmiPort = row.getValueInt("RmiPort");
   //		this.rmiService = row.getValue("RmiServiceName");
   //	}

   private void createTableRequests(TableRequest parentReq, JDataRow parentRow) {
      Iterator<TableRequest> ic = parentReq.getChildRequests();
      if(ic != null) {
         while(ic.hasNext()) {
            TableRequest childReq = ic.next();
            JDataRow childRow = childReq.getMetaDataRow();
            parentRow.addChildRow(childRow);
            this.createTableRequests(childReq, childRow);
         }
      }
      Iterator<TableRequest> ip = parentReq.getParentRequests();
      if(ip != null) {
         while(ip.hasNext()) {
            TableRequest pReq = ip.next();
            JDataRow pRow = parentReq.getMetaDataRow();
            parentRow.addChildRow(pRow);
            this.createTableRequests(pReq, pRow);
         }
      }
   }

   /**
    * Liefert die Metadaten der Current Database als String
    */
   public String getDatabaseMetaData() {
      return this.getDatabaseMetaDataDoc().toString();
   }

   /**
    * Liefert die Metadaten der Current Database als XML-Document
    */
   public Document getDatabaseMetaDataDoc() {
      return database.getDatabaseMetaDataDoc();
   }

   /**
    * Erzeugt einen Request aus Metadaten
    * @see Request#getMetaDataRow()
    * @param metadataRow
    * @return
    */
   public synchronized Request addRequest(JDataRow metadataRow) throws PLException {
      DatabaseConnection dbConReq = null;
      try {
         dbConReq = this.dbConnectionPool.getConnection("PLTransactionContext#setRequest");
         dbConReq.startTransaction("addRequest");
         Request req = new Request(this.getCurrentDatabase(), dbConReq, metadataRow);
         // TODO : unschön
         this.getCurrentDatabase().removeRequest(req.getDatasetName());
         this.getCurrentDatabase().addRequest(req);
         dbConReq.commitTransaction("addRequest");
         return req;
      }
      catch(Exception ex) {
         if(dbConReq != null) {
            dbConReq.rollbackTransaction("addRequest");
         }
         String msg = "PL [ " + layerName + " ] " + "Error adding Request: " + ex.getMessage();
         logger.error(msg, ex);
         throw new PLException(msg, ex);
      }
   }

   /*
    * (non-Javadoc)
    * @see de.pkjs.pl.IPLContext#shutdown()
    */
   public void shutdown() throws PLException {
   }

   /*
    * (non-Javadoc)
    * @see de.pkjs.pl.IPLContext#getPLMetaData()
    */
   public String getPLMetaData() {
      return null;
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
     Document doc = new Document();
     doc.setEncoding("UTF-8");
     DocType dt = new DocType("Server");
     dt.setSystemId("PLConfig.dtd");
     doc.addChild(dt);
     doc.setRoot(this.getElement());
     return doc;
   }
   private Element getElement() {
     Element rootEle = new Element("Server");
     // Defi
     Element defiEle = rootEle.addElement("DatasetDefinitionFile");
     defiEle.setText(this.database.getDatasetDefinitionFileName());
     // Format
     Element formatEle = rootEle.addElement("Format");
     Element dateEle = formatEle.addElement("DateFormat");
     dateEle.setAttribute("value", this.database.getDateFormat().toPattern());
     Element timeEle = formatEle.addElement("TimeFormat");
     timeEle.setAttribute("value", this.database.getTimeFormat().toPattern());
     Element timestampEle = formatEle.addElement("TimestampFormat");
     timestampEle.setAttribute("value", this.database.getTimestampFormat().toPattern());
     Element decEle = formatEle.addElement("DecimalFormat");
     decEle.setAttribute("value", this.database.getDecimalFormat().toPattern());
     // Dataset
     Element datasetEle = rootEle.addElement("Dataset");
     Element encEle = datasetEle.addElement("Encoding");
     encEle.setAttribute("value", this.database.getEncoding());
     Element nameEle = datasetEle.addElement("ElementName");
     nameEle.setAttribute("value", this.database.getDatasetElementName());
     // Options
     Element optEle = rootEle.addElement("Options");
     Element debugEle = optEle.addElement("Debug");
     debugEle.setAttribute("value", Convert.toString(this.database.isDebug()));
     rootEle.addElement(dbConnectionPool.getElement());

     // Databases
     rootEle.addElement(database.getElement());

     return rootEle;
   }


   /*
    * (non-Javadoc)
    * @see de.pkjs.pl.IPLContext#getTodayString()
    */
   public String getTodayString() {
      return getDatabase().getTodayString();
   }

   /*
    * (non-Javadoc)
    * @see de.pkjs.pl.IPLContext#getNowString()
    */
   public String getNowString() {
      return getDatabase().getNowString();
   }

   /*
    * (non-Javadoc)
    * @see de.pkjs.pl.IPLContext#getTodayNowString()
    */
   public String getTodayNowString() {
      return getDatabase().getTodayNowString();
   }

   /*
    * (non-Javadoc)
    * @see de.pkjs.pl.IPLContext#setDebug(boolean)
    */
   public void setDebug(boolean state) {
      getDatabase().setDebug(state);
   }

   /*
    * (non-Javadoc)
    * @see de.pkjs.pl.IPLContext#reset()
    */
   public synchronized void reset() throws PLException {
      // die Definition der Datenbank neu einlesen
      DatabaseConnection dbConReset = null;
      try {
         dbConReset = dbConnectionPool.getConnection("PLTransactionContext#reset InitDatabaseClass");
         dbConReset.startTransaction("InitDatabaseClass");
         this.getDatabase().reset(dbConReset);
         dbConReset.commitTransaction("InitDatabaseClass");
         // Init Statistics
         this.resetTimeStamp = new java.util.Date();
         this.startedTransactions = 0;
         this.commitedTransactions = 0;
         this.rollbackedTransaktions = 0;
         this.abortedTransactions = 0;
      }
      catch(Exception ex) {
         if(dbConReset != null) {
            dbConReset.rollbackTransaction("InitDatabaseClass");
         }
         String msg = "PL [ " + layerName + " ] " + "Error resetting database: " + ex.getMessage();
         logger.error(msg, ex);
         throw new PLException(msg, ex);
      } finally {
         if (dbConReset != null) {
         	dbConReset.close("PL#reset");
          }
      }
   }

   /**
    * @param label
    * @return
    * @throws PLException
    */
  private DatabaseConnection getConnection(String label) throws PLException {
    // 1. Wenn autocommit (= Context des PL), dann neue Connection aus dem Pool
    // holen
    if (this.isAutocommit) {
      this.dbConnection = null;
      logger.debug("get new autocommit connection: " + label);
      DatabaseConnection dconn = this.dbConnectionPool.getConnection(label);
      return dconn;
    }
    // 2. kein autocommit
    // 2.1 wenn disabled: Connection ist "verbraucht" --> Exception
    if (this.isDisabled) {
      String msg = "PL [ " + layerName
          + " ] Transaction context has been released that's why it can't be used any more!";
      logger.error(msg);
      throw new PLException(msg);
    }
    // 2.2 wenn null: Connection aus Pool erzeugen
    if (this.dbConnection == null) {
      logger.debug("get new non-autocommit connection: " + label);
      this.dbConnection = this.dbConnectionPool.getConnection(label);
      // TODO: synchronize?
      // TODO: return dbConnection?
    }
    // 2.3 Thread prüfen
    synchronized (this.dbConnection) {
      if (dbConnection.getOwner() != Thread.currentThread()) {
        String msg = "Not owner of connection: " + this.dbConnection.getOwner().getName() + " <--> "
            + Thread.currentThread().getName();
        logger.error(msg);
        throw new PLException("PL [ " + layerName + " ] Not owner of connection: "
            + dbConnection.getOwner().getName() + " <--> " + Thread.currentThread().getName() + "\n");
      }
      return this.dbConnection;
    }
  }

   /**
    * Wenn Fehler aufgetreten.
    * @see #handleException(String, String, IDatabaseConnection, Exception)
    * @throws PLException
    */
   private void invalidateConnection(DatabaseConnection _dbConnection) throws PLException {
      if(_dbConnection != null) {
         _dbConnection.close("PLTansactionContext#invalidateConnection");
         this.dbConnection = null; // Zur Sicherheit
      }
   }

   /**
    * Wenn Die Connection nicht mehr gebraucht wird
    * @see #handleException(String, String, IDatabaseConnection, Exception)
    * @throws PLException
    */
   private void releaseConnection(DatabaseConnection _dbConnection) throws PLException {
      if(_dbConnection != null) {
         synchronized(_dbConnection) {
            _dbConnection.close("PLTansactionContext#releaseConnection");
            this.dbConnection = null;
         }
      }
   }

   public int getNumActive() {
      return this.dbConnectionPool.getNumActive();
   }

   public int getNumIdle() {
      return this.dbConnectionPool.getNumIdle();
   }

   /*
    * (non-Javadoc)
    * @see de.pkjs.pl.IPLContext#getMaxActive()
    */
   public int getMaxActive() {
      return database.getMaxActive();
   }

   /*
    * (non-Javadoc)
    * @see de.pkjs.pl.IPLContext#getMaxIdle()
    */
   public int getMaxIdle() {
      return database.getMaxIdle();
   }

   /**
    * @see de.pkjs.pl.IPLContext#getMinIdle()
    */
   public int getMinIdle() {
      return database.getMinIdle();
   }
   public long getConnectionTimeOut() {
  	 return database.getConnectionTimeOut();
   }
   public Collection<TransactionInfo> getTransactionInfos() {
     Collection<TransactionInfo> col = database.getTransactionInfos();
     return col;
   }
   /**
    * @return Returns the abortedTransactions.
    */
   public long getAbortedTransactions() {
      return abortedTransactions;
   }

   /**
    * @return Returns the commitedTransactions.
    */
   public long getCommitedTransactions() {
      return commitedTransactions;
   }

   /**
    * @return Returns the rollbackedTransaktions.
    */
   public long getRollbackedTransaktions() {
      return rollbackedTransaktions;
   }

   /**
    * @return Returns the startedTransactions.
    */
   public long getStartedTransactions() {
      return startedTransactions;
   }

   /**
    * @return Returns the createdTimeStamp.
    */
   public java.util.Date getCreatedTimeStamp() {
      return createdTimeStamp;
   }

   /**
    * @return Returns the resetTimeStamp.
    */
   public java.util.Date getResetTimeStamp() {
      return resetTimeStamp;
   }
   
   public String getLayerName() {
     return layerName;
   }
   
   public String getTransactionName() {
     return this.transName;
   }
   
   public void finalize() {
   	if (this.dbConnection != null) {
   		try {
   			if (!dbConnection.isClosed()) {
   				this.dbConnection.close("finalize");
   			}
         } catch (PLException e) {
         	logger.warn(e.getMessage(), e);
         }
   	}   	
   }
}