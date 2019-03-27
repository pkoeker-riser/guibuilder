package de.pkjs.pl;

import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

import de.jdataset.*;

/**
 * Ein SQL-Statement ist immer an eine Tabelle in einer Datenbank gebunden.
 * <p>
 * Das Statement kann SELECT, INSERT, UPDATE oder DELETE ausführen. Hierzu gibt
 * es genau ein PreparedStatement. Dieses Statement kann Spalten und Bedingungen
 * haben.
 */
abstract class AbstractStatement {
  private final static Logger logger = Logger.getLogger(AbstractStatement.class);
   // Attributes
   protected TableRequest myRequest;
   protected String schema;
   // Erstmal nur eine Table je Statement;
   // vielleicht später mal 'nen Join reinbauen?
   private String tablename;
   private ArrayList<JDataColumn> columns = new ArrayList<JDataColumn>(); // nur SELECT!?
   private ArrayList<SqlCondition> wheres = new ArrayList<SqlCondition>();
   private ArrayList<NamedParameter> namedParas; // $name --> 'Maria'
   private String paramString; // ... name = ? (bzw) name = 'Maria'
   private int maxExecutionTime = Integer.MAX_VALUE;
   protected int maxRows = Integer.MAX_VALUE;
   
   private PreparedStatement stmt;

   // Constructor
   AbstractStatement(TableRequest req, String tablename) {
      this.schema = req.getRequest().getDatabase().getSchema();
      this.myRequest = req;
      this.tablename = tablename;
   }

   String getTablename() {
      return tablename;
   }

   ArrayList<JDataColumn> getColumns() {
      return columns;
   }

   ArrayList<SqlCondition> getConditions() {
      return wheres;
   }

   void addCondition(SqlCondition cond) {
      if (cond != null) {
         wheres.add(cond);
      }
   }

   /**
    * Die Angabe "...name = $name..." wird hier zu "...name = ?..."<br> 
    * Die Angabe "...name = 'Maria'..." bleibt so erhalten.
    * 
    * @param where
    */
   void setParameter(String where, ParameterList parameters) {
      StringBuilder buff = new StringBuilder();
      ArrayList<String> al = new ArrayList<String>(); 
      int cnt = 0;
      StringTokenizer toks = new StringTokenizer(where, " (),=\n\r\t", true); 
      while (toks.hasMoreTokens()) {
         String tok = toks.nextToken();
         al.add(tok);
         if (tok.startsWith("$")) {
            buff.append("?");
            cnt++;
            int offset = 0;
            int dataType = Types.VARCHAR; // default
            boolean dataTypeOK = false;
            String paraName = tok.substring(1);
            NVPair para = null;
            if (parameters != null) {
              para = parameters.getParameter(paraName);
              if (para != null) {
                dataType = para.getDataType(); // DataType aus NVPair
                dataTypeOK = true;
                // List<?>
                Object paraValue = para.getValue();
                // Anzahl ? anhand Länge der List<?> wenn Parameter von diesem Typ
                if (paraValue instanceof List<?>) {
                  List<?> paraList = (List<?>)paraValue;
                  int anzPara = paraList.size();
                  if (anzPara > 1) {
                    for (int i = 1; i < anzPara; i++) {
                      buff.append(",?");
                      cnt++;
                      offset++;
                    }
                  }
                } else if (paraValue instanceof long[]) { // desgleichen mit Array
                    long[] paraList = (long[])paraValue;
                    int anzPara = paraList.length;
                    if (anzPara > 1) {
                       for (int i = 1; i < anzPara; i++) {
                         buff.append(",?");
                         cnt++;
                         offset++;
                      }
                   }                    
                }
              }
            }
            // Column
            if (!dataTypeOK) {
              try {
                 // Diese wilde Konstruktion dient einzig dem Zweck,
                 // den Datentyp das Parameters zu ermitteln!!
                 // Funktioniert bei: ... WHERE ColName = $ParaName... (bzw. != < > <= >=); nicht bei ... NOT IN($ParaName)
                 String colName = al.get(al.size() - 3); // TODO: Oops! Setzt sorgfältiges Setzen von Leerzeichen voraus!
                 if (colName.startsWith("(")) { // TODO: Noch ne Krücke!
                    colName = colName.substring(1);
                 }
                 JDataTable tbl = myRequest.getRequest().getDatabase().getDataTable(
                       this.getTablename());
                 JDataColumn col = tbl.getDataColumn(colName);
                 dataType = col.getDataType();
              } catch (Exception ex) {
                 logger.warn("Parsing of ColumnName failed in WHERE Clause: '"+where+"'", ex);
              }
            }
            NamedParameter np = new NamedParameter(paraName, cnt - offset, dataType);
            buff.append(' ');
            this.addParameter(np);
         } else {
            buff.append(tok);
            //buff.append(' '); // raus, wenn Parser ok
         }
      }
      this.paramString = buff.toString();
   }

   private void addParameter(NamedParameter para) {
      if (this.namedParas == null) {
         namedParas = new ArrayList<NamedParameter>();
      }
      namedParas.add(para);
   }

   private String getParaString() {
      return this.paramString;
   }

   protected Iterator<NamedParameter> getNamedParas() {
      if (namedParas == null) {
         return null;
      } else {
         return namedParas.iterator();
      }
   }

   /**
    * Wird von TableRequest versorgt.
    * 
    * @param cols
    */
   void setColumns(ArrayList<JDataColumn> cols) {
      this.columns = cols;
   }

   /**
    * Liefert jeweils einen String in SQL-Syntax für PreparedStatements (SELECT *
    * FROM myTable WHERE id = ?);
    * 
    * @return
    */
   abstract String getSql();

   /**
    * Erzeugt das PreparedStatement.
    * 
    * @return
    */
   PreparedStatement getStatement(DatabaseConnection dbConnection) throws PLException {
      String sql = null;
      Connection con = dbConnection.getConnection();
      try {
         if (stmt == null) {
            sql = getSql();
            this.stmt = con.prepareStatement(sql);
            myRequest.setStatementAttributes(stmt);
            return stmt;
         } else {
            return stmt; 
         }
      } catch (SQLException e) {
         String msg = "PL [ " + myRequest.getLayerName()
         	+ " ] Error creating prepared statement for SQL [ " + sql + " ] "
         	+ e.getMessage();
         logger.error(msg, e);
         throw new PLException(msg, e);
      }
   }

   /**
    * Wird aufgerufen von TableRequest#deleteRow, updateRow
    * Dort wird auch die Ausführungszeit gemessen
    * 
    * @return Anzahl der geänderten Datensätze
    * @throws SQLException
    */
   int executeUpdate(DatabaseConnection dbConnection) throws PLException {
      PreparedStatement ps = this.getStatement(dbConnection);

      if (myRequest.isDebug()) {
        String sql = this.toString();
        logger.debug(sql);
      }
      int ret;
      try {
        // StatementInfo
        TransactionInfo tInfo = dbConnection.getDatabase().getTransactionInfo(dbConnection.getId());
        if (tInfo != null) {
          tInfo.addStatement(dbConnection, this);
        }
        // Statement ausführen 
        ret = ps.executeUpdate();
      } catch (SQLException e) {
         String msg = "PL [ " + myRequest.getLayerName()
         	+ " ] Error executing statement: " + e.getMessage() + "\n" + getSql();
         logger.error(msg, e);
         throw new PLException(msg, e);
      }
      try {
         ps.close(); // PS Wird immer geschlossen
      } catch (SQLException e) {
         String msg = "PL [ " + myRequest.getLayerName()
         	+ " ] Error closing statement: " + e.getMessage();
         logger.error(msg, e);
         throw new PLException(msg, e);
      }
      return ret;
   }

   TableRequest getMyRequest() {
      return myRequest;
   }

   protected String getWhere() {
      StringBuilder buff = new StringBuilder();
      boolean firstTime = true;
      if (this.getParaString() != null) {
         buff.append(" WHERE ");
         buff.append(this.getParaString());
         firstTime = false;
      }
      for (SqlCondition cond: wheres) {
         buff.append(cond.getSql(firstTime));
         firstTime = false;
      }
      return buff.toString();
   }
   /**
    * Liefert die Anzahl der SqlConditionen die vom Typ JOIN sind
    * @return
    */
   int getJoinCount() {
     if (this.wheres == null) {
       return 0;
     } else {
       int cnt = 0;
       for (SqlCondition cond: wheres) {
         if (cond.isJoin()) {
           cnt ++;
         }
       }
       return cnt;
     }
   }
   /**
    * Versorgt die JOIN-SqlCondition mit den Werten der Keys
    * @param value
    */
   void setJoinValue(ArrayList<JDataValue> values) {
     int cnt = 0;
     for (SqlCondition cond: wheres) {
       if (cond.isJoin()) {
         JDataValue val = values.get(cnt);
         cond.setValue(val.getValue());
         cnt++;
       }
     }
   }

   /**
    * @return Anzahl geänderter Datensätze (bei Oracle geschätzt!)
    * @throws PLException
    */
   int executeBatch() throws PLException {
     try {
        if (myRequest.isDebug()) {
           String sql = this.toString();
           logger.debug(sql);
           //System.out.println(sql);
        }
       int anz = 0;
       int[] ret = stmt.executeBatch();
       stmt.close(); // NEW ORACLE
       for (int i = 0; i < ret.length; i++) {
         if (ret[i] == Statement.SUCCESS_NO_INFO) { // -2
           /* TODO
            * Oh weh!
            * -2 bedeutet: "processed successfully, but that the number of effected rows is unknown"
            * Das macht Oracle so :-(
            */
           anz += 1; // Schätzung
         } else {
           anz += ret[i];
         }
       }
       return anz;
     } catch (SQLException sex) {
       logger.error(sex.getMessage(), sex);
       throw new PLException(sex);
     }
   }

   /**
    * Liefert das SQL-Statement.
    */
   public String toString() {
      return getSql();
   }

  protected int getMaxExecutionTime() {
    return maxExecutionTime;
  }

  protected void setMaxExecutionTime(int maxExecutionTime) {
    this.maxExecutionTime = maxExecutionTime;
  }
  /**
   * Wenn beide kein default, dann den zu Laufzeit definierten Wert nehmen
   * @param maxXml
   * @param maxPara
   */
  protected void setMaxExecutionTime(int maxXml, int maxPara) {
    if (maxXml != Integer.MAX_VALUE && maxPara != Integer.MAX_VALUE) {
      this.maxExecutionTime = maxPara;
    } else if (maxXml != Integer.MAX_VALUE) {
      this.maxExecutionTime = maxXml;      
    } else if (maxPara != Integer.MAX_VALUE) {
      this.maxExecutionTime = maxPara;
    }
  }
  int getMaxRows() {
    return maxRows;
  }
  void setMaxRows(int maxRows) {
   this.maxRows = maxRows;
  }
  
  /**
   * Parses a query with named parameters.  The parameter-index mappings are put into the map, and the
   * parsed query is returned.  DO NOT CALL FROM CLIENT CODE.  This method is non-private so JUnit code can
   * test it.
   * @param query    query to parse
   * @param paramMap map to hold parameter-index mappings
   * @return the parsed query
   */
  protected static final String parse(String query, Map<String, List<Integer>> paramMap) {
      // I was originally using regular expressions, but they didn't work well for ignoring
      // parameter-like strings inside quotes.
      int length=query.length();
      StringBuilder parsedQuery=new StringBuilder(length);
      boolean inSingleQuote=false;
      boolean inDoubleQuote=false;
      int index=1;

      for(int i=0;i<length;i++) {
          char c=query.charAt(i);
          if(inSingleQuote) {
              if(c=='\'') {
                  inSingleQuote=false;
              }
          } else if(inDoubleQuote) {
              if(c=='"') {
                  inDoubleQuote=false;
              }
          } else {
              if(c=='\'') {
                  inSingleQuote=true;
              } else if(c=='"') {
                  inDoubleQuote=true;
              } else if(c=='$' && i+1<length &&
                      Character.isJavaIdentifierStart(query.charAt(i+1))) {
                  int j=i+2;
                  while(j<length && Character.isJavaIdentifierPart(query.charAt(j))) {
                      j++;
                  }
                  String name=query.substring(i+1,j);
                  c='?'; // replace the parameter with a question mark
                  i+=name.length(); // skip past the end if the parameter

                  List<Integer> indexList = paramMap.get(name);
                  if(indexList == null) {
                      indexList = new LinkedList<Integer>();
                      paramMap.put(name, indexList);
                  }
                  indexList.add(new Integer(index));

                  index++;
              }
          }
          parsedQuery.append(c);
      }
      // replace the lists of Integer objects with arrays of ints
//      for(Iterator<Map.Entry<String, List<Integer>>> itr = paramMap.entrySet().iterator(); itr.hasNext();) {
//          Map.Entry<String, List<Integer>> entry = itr.next();
//          List<Integer> list = entry.getValue();
//          int[] indexes=new int[list.size()];
//          int i=0;
//          for(Iterator<Integer> itr2=list.iterator(); itr2.hasNext();) {
//              Integer x = itr2.next();
//              indexes[i++]=x.intValue();
//          }
//          entry.setValue(indexes);
//      }

      return parsedQuery.toString();
  }
}