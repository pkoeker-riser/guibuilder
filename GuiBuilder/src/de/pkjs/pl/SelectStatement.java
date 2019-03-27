package de.pkjs.pl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import de.jdataset.JDataColumn;
import de.jdataset.JDataRow;
import de.jdataset.JDataTable;
import de.jdataset.NVPair;
import de.jdataset.ParameterList;
import de.pkjs.util.Convert;

/**
 * SELECT * FROM MyTable WHERE MyKey = ?
 * 
 * @author peter
 */
final class SelectStatement extends AbstractStatement {
   // Attribute
   private boolean distinct;
   private String orderBy;
   private String groupBy;
   private PreparedStatement ps;

   /**
    * @deprecated
    *             Für handgeschnitzte Statements
    */
   private String from;
   private ArrayList<TableRequest> joinRequests; // SELECT ... JOIN ... JOIN ...

   private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SelectStatement.class);

   // Constructor
   SelectStatement(TableRequest req, String tablename) {
      super(req, tablename);
      this.distinct = req.isDistinct();
      if(req.isDebug()) {
         logger.debug("New SelectStatement created: " + req.getViewname());
      }
   }

   /**
    * @deprecated
    * @param req
    * @param tablename
    * @param from
    */
   SelectStatement(TableRequest req, String tablename, String from) {
      this(req, tablename);
      this.from = from;
   }

   // Methods
   /**
    * Fügt dem Select-Statement eine JOIN-Condition hinzu.
    */
   void addJoinRequest(TableRequest parentRequest) {
      if(this.joinRequests == null) {
         this.joinRequests = new ArrayList<TableRequest>();
      }
      this.joinRequests.add(parentRequest);
   }

   void setOrderBy(String s) {
      if(s != null && s.length() == 0) {
         s = null;
      }
      this.orderBy = s;
   }

   void setGroupBy(String s) {
      if(s != null && s.length() == 0) {
         s = null;
      }
      this.groupBy = s;
   }

   ResultSet executeQuery(DatabaseConnection dbConnection) throws PLException {
      return this.executeQuery(dbConnection, null, null);
   }

   // Parameters darf auch null sein	
   ResultSet executeQuery(DatabaseConnection dbConnection, ParameterList parameters, JDataRow parentRow)
         throws PLException {
      ps = this.getStatement(dbConnection);
      this.fillValues(ps, parameters, parentRow);
      ResultSet rs;
      try {
         // StatementInfo
         TransactionInfo tInfo = dbConnection.getDatabase().getTransactionInfo(dbConnection.getId());
         if(tInfo != null) {
            tInfo.addStatement(dbConnection, this);
         }
         if(myRequest.isDebug()) {
            String sql = this.toString();
            logger.debug(sql);
         }
         // Statement ausführen
         long _startTime = System.currentTimeMillis();
         rs = ps.executeQuery();
         // Zeit messen
         long _dura = System.currentTimeMillis() - _startTime;
         // 1. default aus Datenbank
         long maxExecutionTime = dbConnection.getDatabase().getDefaultMaxExecutionTime();
         // 2. aus Statement
         if(this.getMaxExecutionTime() != Integer.MAX_VALUE) {
            maxExecutionTime = this.getMaxExecutionTime();
         }
         // 3. aus Parameter
         if(parameters != null && parameters.getMaxExecutionTime() != Integer.MAX_VALUE) {
            maxExecutionTime = parameters.getMaxExecutionTime();
         }
         if(_dura > maxExecutionTime) {
            Logger slowLog = dbConnection.getDatabase().getSlowQueryLogger();
            if(slowLog != null) {
               String msg = "Statement: " + this.getSql() + "\n Paramter: " + parameters
                     + "\n maxExecutionTime exceeded: " + _dura + ">" + maxExecutionTime;
               slowLog.warn(msg);
            }
         }
         return rs;
      }
      catch(SQLException ex) {
         String msg = "PL [ " + myRequest.getLayerName() + " ] Error executing query statement: " + ex.getMessage();
         logger.error(msg, ex);
         throw new PLException(msg, ex);
      }
   }
   
   void close() {
   	if (ps != null) {
   		try {
	         ps.close();
         } catch (SQLException e) {
         	logger.warn(e.getMessage(), e);
         }
   	}
   }

   // überschrieben von AbstractStatement
   String getSql() {
      StringBuilder buff = new StringBuilder();
      buff.append("SELECT ");
      if(this.distinct == true) {
         buff.append("DISTINCT ");
      }
      buff.append(this.getSelectColumns());
      if(from != null) {
         buff.append(" ");
         buff.append(this.from);
      }
      else {
         buff.append(this.getFrom());
         buff.append(this.getWhere());
      }
      if(orderBy != null) {
         buff.append(" ORDER BY ");
         buff.append(orderBy);
      }
      if(groupBy != null) {
         buff.append(" GROUP BY ");
         buff.append(groupBy);
      }
      // LIMIT
      if(this.maxRows != Integer.MAX_VALUE && this.getDatabase().hasLimit()) {
         buff.append(" LIMIT ");
         buff.append(this.maxRows);
      }
      // ... UNION ...
      ArrayList<UnionStatement> unions = myRequest.getUnions();
      if(unions != null) {
         for(UnionStatement us : unions) {
            String uSql = us.getSql();
            buff.append(' ');
            buff.append(uSql);
         }
      }
      //		if (this.getMyRequest().isDebug()) {
      //			logger.debug(buff.toString());
      //		}
      return buff.toString();
   }

   String getFrom() {
      StringBuilder buff = new StringBuilder();
      buff.append(" FROM ");
      // Schema mit ausgeben falls vorhanden?
      //		if (schema != null) {
      //		   buff.append(schema);
      //		   buff.append(".");
      //		}
      buff.append(this.getTablename());
      // JoinTables
      if(this.joinRequests != null) {
         for(TableRequest parentRequest : joinRequests) {
            String fj = this.createFromJoin(this.getTablename(), parentRequest);
            buff.append(fj);
         }
      }
      return buff.toString();
   }

   /**
    * @param tablename
    * @param parentRequest
    * @return
    */
   private String createFromJoin(String tablename, TableRequest request) {
      StringBuilder buff = new StringBuilder();
      JDataTable dataTable = request.getDataTable();
      // 1. JOIN ...
      buff.append(" ");
      buff.append(request.getDataTable().getJoin());
      buff.append(" JOIN ");
      // 2. Parent Tablename
      buff.append(dataTable.getSelect());
      // 3. ON
      buff.append(" ON ");
      String pks = null;
      String fks = null;
      if(dataTable.getTableType() == JDataTable.PARENT_TABLE) {
         pks = request.getPK();
         fks = request.getFK();
      }
      else if(dataTable.getTableType() == JDataTable.CHILD_TABLE) {
         pks = request.getFK();
         fks = this.myRequest.getPK();
      }
      StringTokenizer toks_pk = new StringTokenizer(pks, ",");
      StringTokenizer toks_fk = new StringTokenizer(fks, ",");
      int anzTokens = toks_pk.countTokens();
      int cntTokens = 0;
      try {
         switch(anzTokens) {
            case 0:
               // Error
               throw new IllegalArgumentException("SelectStatement#getFrom: Missing Foreign/Primary Key");
               //break;
            case 1: {
               // 3.1 fk=pk
               buff.append(tablename);
               buff.append(".");
               buff.append(fks);
               buff.append("=");
               buff.append(request.getDataTable().getAliasOrName());
               buff.append(".");
               buff.append(pks);
            }
               break;
            default: // > 1
            {
               // 3.2 fk1=pk1 AND fk2=pk2 AND... 
               while(toks_pk.hasMoreTokens()) {
                  String pk = toks_pk.nextToken();
                  String fk = toks_fk.nextToken();
                  buff.append(tablename);
                  buff.append(".");
                  buff.append(fk);
                  buff.append("=");
                  buff.append(request.getDataTable().getAliasOrName());
                  buff.append(".");
                  buff.append(pk);
                  cntTokens++;
                  if(cntTokens < anzTokens) {
                     buff.append(" AND ");
                  }
               }
            }
               break;
         }

      }
      catch(Exception ex) {
         logger.error("Tablename: " + tablename, ex);
         System.err.println(ex.getMessage());
      }
      // ParentParents TODO : rekusiv? Childs?
      Iterator<TableRequest> it = request.getParentRequests();
      if(it != null) {
         while(it.hasNext()) {
            TableRequest tr = it.next();
            if(tr.getDataTable().getJoin() != null) {
               String fj = this.createFromJoin(request.getDataTable().getTablename(), tr);
               buff.append(fj);
            }
         }
      }

      return buff.toString();
   }

   private void fillValues(PreparedStatement ps, ParameterList parameters, JDataRow parentRow) {
      try {
         if(parameters != null) {
            int queryTimeout = parameters.getQueryTimeout();
            if(queryTimeout < 1 || queryTimeout == Integer.MAX_VALUE
                  || this.getDatabase().getDatabaseType() == Database.HSQLDB) {
               // nix machen; HSQLDB kann kein QueryTimeout
            }
            else {
               ps.setQueryTimeout(queryTimeout);
            }
         }
         int cnt = 0;
         Iterator<NamedParameter> ii = this.getNamedParas();
         if(ii != null) {
            while(ii.hasNext()) {
               NamedParameter np = ii.next();
               NVPair nvp = parameters.getParameter(np.getName());
               // Zuvor konvertieren
               Object paraValue = nvp.getValue();
               // Value von NPPair ist vom Typ List<?>
               if(paraValue instanceof List<?>) {
                  List<?> paraList = (List<?>)paraValue;
                  for(int p = 0; p < paraList.size(); p++) {
                     Object paraListValue = paraList.get(p);
                     Object o = Convert.toObject(paraListValue.toString(), np.getDataType());
                     cnt++;
                     ps.setObject(cnt, o, np.getDataType());
                  }
               } else if(paraValue instanceof long[]) {
                  long[] paraList = (long[])paraValue;
                  for(int p = 0; p < paraList.length; p++) {
                     long paraListValue = paraList[p];             
                     cnt++;
                     ps.setObject(cnt, paraListValue, np.getDataType());
                  }                  
               }
               else {
                  cnt++;
                  if (paraValue == null) {
                     ps.setNull(cnt, np.getDataType());
                  } else {
                     Object o = Convert.toObject(paraValue.toString(), np.getDataType());
                     ps.setObject(cnt, o, np.getDataType());
                  }
               }
            }
         }
         for(int j = 0; j < this.getConditions().size(); j++) {
            SqlCondition cond = this.getConditions().get(j);
            if(cond.isSimple() == false) {
               Object val = cond.getValue();
               if(val instanceof long[]) {
                  long[] oids = (long[])val;
                  for(int jj = 0; jj < oids.length; jj++) {
                     cnt++;
                     ps.setObject(cnt, oids[jj], cond.getDataType());
                  }
               }
               else {
                  cnt++;
                  ps.setObject(cnt, val, cond.getDataType());
               }
            }
         }
         // Union am Schluß??
         ArrayList<UnionStatement> unions = this.myRequest.getUnions();
         if(unions != null) {
            for(UnionStatement us : unions) {
               ParameterList ulist = us.getParameter(parentRow);
               Iterator<NVPair> it = ulist.iterator();
               if(it != null) {
                  while(it.hasNext()) {
                     NVPair nvp = it.next();
                     cnt++;
                     Object o = nvp.getValue();
                     ps.setObject(cnt, o, nvp.getDataType());
                  }
               }
            }
         }
      }
      catch(Exception ex) {
         logger.error("fillValues", ex);
         System.err.println("SelectStatement#fillValues; Fatal: " + ex.getMessage());
      }
   }

   /**
    * Liefert einen String mit den Columns für ein SELECT-Statement.
    * ... Name,Strasse,PLZ AS ZIP, ...
    * <p>
    * Wenn garkeine Column angegeben, wird 'empty' geliefert.
    * 
    * @see JDataColumn#getSelect()
    * @return
    */
   private String getSelectColumns() {
      StringBuilder buff = new StringBuilder();
      for(int i = 0; i < getColumns().size(); i++) {
         JDataColumn col = getColumns().get(i);
         if(col.isTransient() == false) {
            buff.append(col.getSelect());
         }
      }
      // Join	
      if(this.joinRequests != null) {
         for(TableRequest parentRequest : joinRequests) {
            //if (parentRequest.getDataTable().getJoin() != null) {
            String jc = this.getSelectColumnsJoin(parentRequest);
            buff.append(jc);
            //}
         }
      }
      // Das letzte Komma abschneiden
      if(buff.length() > 1) {
         buff.deleteCharAt(buff.length() - 1);
         return buff.toString();
      }
      else {
         //logger.warn("No columns for SELECT-Statement??");
         return "'empty'";
      }
   }

   /**
    * Rekursion
    * 
    * @param parentRequest
    * @return
    */
   private String getSelectColumnsJoin(TableRequest parentRequest) {
      StringBuilder buff = new StringBuilder();
      for(Iterator<JDataColumn> ic = parentRequest.getColumns(); ic.hasNext();) {
         JDataColumn col = ic.next();
         if(col.isTransient() == false) {
            buff.append(col.getSelectJoin());
         }
      }
      Iterator<TableRequest> it = parentRequest.getParentRequests();
      if(it != null) {
         while(it.hasNext()) {
            TableRequest tr = it.next();
            if(tr.getDataTable().getJoin() != null) {
               String jc = this.getSelectColumnsJoin(tr);
               buff.append(jc);
            }
         }
      }
      return buff.toString();
   }

   private Database getDatabase() {
      return myRequest.getRequest().getDatabase();
   }
   
   public void finalize() {
   	this.close();
   }
}
