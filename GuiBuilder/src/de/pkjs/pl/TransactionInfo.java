package de.pkjs.pl;

import java.io.Serializable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import de.jdataset.JDataTable;

public class TransactionInfo implements Serializable {
  private static final Logger logger = Logger.getLogger(TransactionInfo.class);
  private static final long serialVersionUID = 1L;
  
  private String layerName;
  private String ownerName;
  private String label;
  private Date created;
  private Date lastused;
  private long id;
  private String sessionId;
  private boolean commited;
  private boolean rollbacked;
  private ArrayList<StatementInfo> statements = new ArrayList<StatementInfo>();
  private static enum Status {NEW, STARTED, COMMIT, ROLLBACK};
  private Status status = Status.NEW;

  TransactionInfo(DatabaseConnection conn) {
    this.layerName = conn.getDatabase().getLayerName();
    this.ownerName = conn.getOwnerName();
    this.label = conn.getLabel();
    this.created = conn.getCreatedTimestamp();
    this.lastused = conn.getLastusedTimestamp();
    this.id = conn.getId();
    this.sessionId = conn.getDatabaseSessionID();
    this.commited = conn.isCommited();
    this.rollbacked = conn.isRollback();
  }
  
  StatementInfo addStatement(DatabaseConnection conn, AbstractStatement stmt) {
    StatementInfo sInfo = new StatementInfo(conn, stmt);
    statements.add(sInfo);
    return sInfo;
  }
  
  public long getId() {
    return this.id;
  }
  
  public String toString() {
    String s = "Label: " +label 
       + "\nSession/id: " + sessionId + "/" +id + " Status: " + status + " Created: " + created + " LastUsed: " + lastused;
    if (statements != null) {
      try {
        for(StatementInfo sInfo:statements) {
          s += "  " + sInfo.toString() + "\n";
        }
      } catch (Exception ex) {
        logger.warn(ex.getMessage()); // CurrentModificationException
      } 
    }
    return s;
  }
  
//  JDataTable getTransInfoTable() {
//     JDataTable tbl = new JDataTable("TransactionInfo");
//     tbl.addColumn("layerName", Types.VARCHAR);
//     tbl.addColumn("ownerName", Types.VARCHAR);
//     tbl.addColumn("label", Types.VARCHAR);
//     tbl.addColumn("created", Types.TIMESTAMP);
//     tbl.addColumn("lastused", Types.TIMESTAMP);
//     tbl.addColumn("id", Types.INTEGER);
//
//     return tbl;
//  }
  
}
