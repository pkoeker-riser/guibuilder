package de.pkjs.pl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class StatementInfo implements Serializable {
  private static final long serialVersionUID = 1L;
   
  private String userName;
  private String threadName;
  private String statement;
  private ArrayList<NamedParameter> parameter;
  private ArrayList<SqlCondition> wheres;
  private Date started = new Date();
  private String dbSessionId; // Datenbank-Session aus Connection
  
  StatementInfo(DatabaseConnection conn, AbstractStatement stmt) {
    this.statement = stmt.getSql();
    this.dbSessionId = conn.getDatabaseSessionID();
    // Named Paras
    Iterator<NamedParameter> itp = stmt.getNamedParas();
    if (itp != null) {
      parameter = new ArrayList<NamedParameter>();
      while (itp.hasNext()) {
        parameter.add(itp.next());
      }
    }
    // Wheres
    ArrayList<SqlCondition> sc = stmt.getConditions();
    if (sc != null && sc.size() > 0) {
      wheres = new ArrayList<SqlCondition>(sc.size());
      for (SqlCondition wh: sc) {
        wheres.add(wh);
      }
    }
  }
  
  public String toString() {
    String s = "UserName: " + this.userName +" Thread: " + this.threadName + " Date: " + this.started + " Session: " +this.dbSessionId 
       + '\n' + statement;
    return s;
  }
}
