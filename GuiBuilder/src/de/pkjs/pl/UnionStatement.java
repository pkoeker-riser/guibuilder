package de.pkjs.pl;

import java.sql.Types;
import java.util.Iterator;
import java.util.StringTokenizer;

import de.jdataset.JDataRow;
import de.jdataset.JDataValue;
import de.jdataset.NVPair;
import de.jdataset.ParameterList;

class UnionStatement {
  String sql;
  ParameterList list = new ParameterList();

  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(UnionStatement.class);

  UnionStatement(String sql) {
    this.sql = sql;
    StringBuilder buff = new StringBuilder();
    if (sql.indexOf("$") != -1 ) {
      list = new ParameterList();
      StringTokenizer toks = new StringTokenizer(sql, " (),=\n\r\t", true);
      while (toks.hasMoreTokens()) {
        String tok = toks.nextToken();
        if (tok.startsWith("$")) {
           buff.append("?");   
           String paraName = tok.substring(1);
           int dataType = Types.VARCHAR;
           NVPair nv = new NVPair(paraName, null, dataType);
           list.addParameter(nv);
        } else {
          buff.append(tok);
          //buff.append(' ');
        }
      }
      this.sql =  buff.toString();
    }
  }
  
  /**
   * Ein UNION-Teil eines SQL-Statement ... UNION ... SELECT ... WHERE ...
   * @return
   */
  String getSql() {
    return sql;
  }
  
  
  /**
   * Die Parameter für den Union-Teil des Statements
   * @return
   */
  ParameterList getParameter(JDataRow parentRow) {
    Iterator<NVPair> it = list.iterator();
    if(it!= null) {
      while (it.hasNext()) {
        NVPair nv = it.next();
        try {
          JDataValue val = parentRow.getDataValue(nv.getName());
          nv.setDataType(val.getDataType());
          nv.setValue(val.getValue());
        } catch (Exception ex) {
          logger.error(ex.getMessage(), ex);
        }
      }
    }
    return list;
  }
  
}
