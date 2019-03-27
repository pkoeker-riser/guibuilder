package de.pkjs.pl;


import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import de.jdataset.JDataColumn;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.jdataset.JDataValue;
import de.jdataset.ParameterList;
import de.jdataset.Relation;
import de.pkjs.util.Convert;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;

/**
 * Der Zugriff auf eine einzelne Tabelle im Zusammenhang mit einem Request.
 * <p>
 * Ein TableRequest kann gemäß RequestDefinition Child- und ParentRequests
 * haben.<p>
 * Achtung! Diese Klasse ist nicht Thread-Save!
 */
public final class TableRequest {
  // Attributes
  public static final int ROOT_TABLE = 1;
  public static final int CHILD_TABLE = 2;
  public static final int PARENT_TABLE = 3;
  private int requestType;
  private Request myRequest;
  /**
   * Das ist die DataTable, die am Ende mit dem Dataset ausgeliefert wird! Aus
   * Config und meta; nur die in DatabaseConfig.xml definierten Columns (ggf
   * zusätzliche auch Ausdrücke)
   */
  private JDataTable myDataTable;
  private String refname; // Referenc Name statt Tablename
  private String viewname;
  /**
   * Aus Datenbank-Definition; alle Columns.<br>
   * Dieses Teil ist null, wenn die Tabelle transient oder virtual ist
   */
  private JDataTable dbTable;
  private boolean distinct;
  private String pk;
  /**
   * Sequence für PKs; aus Attribut seq=
   */
  private String sequence;
  private String fk;
  private String where;
  private String orderBy;
  private String groupBy;
  private static JDataTable metaDataTable;
  // StatementAttributes
  private int fetchSize = -1;
  private int maxFieldSize = -1;
  private int maxRows = Integer.MAX_VALUE;
  private int queryTimeout = Integer.MAX_VALUE;
  private int maxExecutionTime = Integer.MAX_VALUE;
  
  private ArrayList<TableRequest> childRequests;
  private ArrayList<TableRequest> parentRequests;
  private ArrayList<UnionStatement> unions;
  // Das sind die Columns aus der Request-Definition!
  // Hier kann eine Column also durchaus den Namen "*" haben!
  // Nicht zu verwechseln mit den Columns, die aus
  // den Metadaten in myDatatable eingelesen werden.
  private ArrayList<JDataColumn> columns = new ArrayList<JDataColumn>(); // JDataColumn
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TableRequest.class);
  // Nur für Child Requests
  private int update_rule = DatabaseMetaData.importedKeyNoAction;
  private int delete_rule = DatabaseMetaData.importedKeyCascade;
  
  private Element element;

  // Constructor
  /**
   * @deprecated Erzeugt einen leeren Table Request
   */
  TableRequest(DatabaseConnection dbConnection, Request req, String tablename,
      String columns, int tableType) {
    this.myRequest = req;
    this.requestType = tableType;
    this.dbTable = myRequest.getDatabase().getDataTable(tablename);
    myDataTable = new JDataTable(tablename);
    myDataTable.setSchema(req.getDatabase().getSchema());
    // Columns
    StringTokenizer toks = new StringTokenizer(columns, ",");
    while (toks.hasMoreTokens()) {
      String tok = toks.nextToken();
      if (tok.startsWith(" ")) {
        tok = tok.trim();
      }
      this.addColumn(tok);
    }
    // JDataTable und JDataColumns aus Metadaten einlesen.
    try {
      this.createMetaData(dbConnection, false);
    } catch (Exception ex) {
      logger.error("CreateMetaData", ex);
      throw new IllegalStateException(ex.getMessage());
    }
  }

  TableRequest(DatabaseConnection dbConnection, Request req, Element ele, int tableType) {
    this.myRequest = req;
    this.element = ele;
    this.requestType = tableType;

    // Table
    String tablename = ele.getAttribute("tablename");
    viewname = ele.getAttribute("viewname");
    this.refname = ele.getAttribute("refname");
    // PK, FK
    this.pk = ele.getAttribute("pk");
    this.fk = ele.getAttribute("fk");
    // seq
    this.sequence = ele.getAttribute("seq");
    // Alias
    String alias = ele.getAttribute("alias");
    // Create myDataTable
    this.myDataTable = new JDataTable(tablename, alias, tableType);
    // transient
    boolean isTransient = Convert.toBoolean(ele.getAttribute("transient"));
    this.myDataTable.setTransient(isTransient);
    // virtual
    String virtualChild = ele.getAttribute("virtual");
    this.myDataTable.setVirtualChild(virtualChild);
    // JDataTable
    if (this.isTransient() == false && myDataTable.isVirtualChild() == false) {
      this.dbTable = req.getDatabase().getDataTable(tablename);
    }
    this.myDataTable.setRefname(this.refname);
    this.orderBy = ele.getAttribute("orderby");
    this.groupBy = ele.getAttribute("groupby");
    this.where = ele.getAttribute("where");
    // distinct
    distinct = Convert.toBoolean(ele.getAttribute("distinct"));
    // join
    String join = ele.getAttribute("join");
    this.myDataTable.setJoin(join);
    // readonly
    boolean readonly = Convert.toBoolean(ele.getAttribute("readonly"));
    this.myDataTable.setReadonly(readonly);
    // self
    boolean self = Convert.toBoolean(ele.getAttribute("selfReference"));
    this.myDataTable.setSelfReference(self);
    String changeProtocol = ele.getAttribute("changeProtocol");
    this.myDataTable.setChangeProtocol(changeProtocol);
    // StatementAttributes
    {
      String s = ele.getAttribute("fetchSize");
      if (s != null) {
        this.fetchSize = Convert.toInt(s);
      }
      s = ele.getAttribute("maxRows");
      if (s != null) {
        this.maxRows = Convert.toInt(s);
      }
      s = ele.getAttribute("maxFieldSize");
      if (s != null) {
        this.maxFieldSize = Convert.toInt(s);
      }
      s = ele.getAttribute("queryTimeout");
      if (s != null) {
        this.queryTimeout = Convert.toInt(s);
      }
      s = ele.getAttribute("maxExecutionTime");
      if (s != null) {
        this.maxExecutionTime = Convert.toInt(s);
      } else {
         this.maxExecutionTime = myRequest.getDatabase().getDefaultMaxExecutionTime();
      }
    }
    // Rules
    String s_updateRule = ele.getAttribute("OnUpdate");
    if (s_updateRule != null) {
      this.setUpdateRule(TableRequest.getRule(s_updateRule));
    }
    String s_deleteRule = ele.getAttribute("OnDelete");
    if (s_deleteRule != null) {
      this.setDeleteRule(TableRequest.getRule(s_deleteRule));
    }
    // Columns
    Elements columnEles = ele.getElements("Column");
    this.addColumns(columnEles);
    // Create Childs and Parents
    Elements eles = ele.getElements();
    // Persistent ----------------
    if (this.isTransient() == false && myDataTable.isVirtualChild() == false) {
      // JDataTable und JDataColumns aus Metadaten einlesen.
      try {
        this.createMetaData(dbConnection, false);
        this.createTables(dbConnection, eles);
        // TODO : Das muß hier doch nochmal vorgenommen werden!
        // wegen der anderen beiden Befehle!?
        // createMetaData wird hier doppelt aufgerufen!?
        if (this.hasParentRequests() && this.getDataTable().getJoin() == null) {
          this.createMetaData(dbConnection, true);
        }
        // Lock
        if (!isTransient) {
       	 myDataTable.setLocked();
        }
      } catch (Exception ex) {
        logger.error("createMetaData", ex);
        throw new IllegalStateException(ex.getMessage());
      }
    } else { // Transient ---------------------
      // Tables und Columns aus XML erzeugen
      this.createColumns(ele);
      this.createTables(dbConnection, eles);
    }
  }

  TableRequest(DatabaseConnection dbConnection, Request req, JDataRow row) {
    this.myRequest = req;
    // Table
    String tableName = row.getValue("TableName");
    this.requestType = row.getValueInt("RequestType");
    this.refname = row.getValue("RefName");
    String s = row.getValue("ViewName");
    if (s != null && s.length() > 0 && s.equals(tableName) == false) {
      this.viewname = s;
    }
    String alias = row.getValue("Alias");
    this.distinct = row.getValueBool("x_distinct");
    this.pk = row.getValue("PK");
    this.fk = row.getValue("FK");
    this.sequence = row.getValue("seq");
    // MyDataTable
    this.myDataTable = new JDataTable(tableName, alias, this.requestType);
    this.myDataTable.setRefname(this.refname);
    // transient
    boolean isTransient = row.getValueBool("transient");
    this.myDataTable.setTransient(isTransient);
    // virtual
    String virtualChild = row.getValue("virtual");
    this.myDataTable.setVirtualChild(virtualChild);
    // JDataTable
    if (myDataTable.isTransient() == false && myDataTable.isVirtualChild() == false) {
      this.dbTable = myRequest.getDatabase().getDataTable(tableName);
    }
    this.myDataTable.setSelfReference(row.getValueBool("selfReference"));
    // Other
    this.where = row.getValue("x_Where");
    this.orderBy = row.getValue("x_Orderby");
    this.groupBy = row.getValue("x_GroupBy");
    this.myDataTable.setJoin(row.getValue("x_Join"));
    this.fetchSize = row.getValueInt("FetchSize");
    this.maxFieldSize = row.getValueInt("MaxFieldSize");
    this.maxRows = row.getValueInt("MaxRows");
    this.queryTimeout = row.getValueInt("QueryTimeout");
    this.maxExecutionTime = row.getValueInt("maxExecutionTime");
    this.update_rule = row.getValueInt("x_OnUpdate");
    this.delete_rule = row.getValueInt("x_OnDelete");
    // Columns
    this.addColumns(row);
    // Persistent ----------------
    if (this.isTransient() == false && myDataTable.isVirtualChild() == false) {
      // JDataTable und JDataColumns aus Metadaten einlesen.
      try {
        this.createMetaData(dbConnection, false);
        Iterator<JDataRow> itc = row.getChildRows("ChildTables");
        this.createTables(dbConnection, itc);
        Iterator<JDataRow> itp = row.getChildRows("ParentTables");
        this.createTables(dbConnection, itp);
        // TODO : Das muß hier doch nochmal vorgenommen werden!
        // wegen der anderen beiden Befehle!?
        if (this.hasParentRequests() && this.getDataTable().getJoin() == null) {
          this.createMetaData(dbConnection, true);
        }
      } catch (Exception ex) {
        logger.error("CreateMetaData", ex);
        throw new IllegalStateException(ex.getMessage());
      }
    } else { // Transient -------------
      // Tables und Columns aus XML erzeugen
      this.createColumns(row);
      // this.createTables(dbConnection, eles);
    }
  }

  /**
   * @return
   */
  int getDeleteRule() {
    return delete_rule;
  }

  /**
   * @param delete_rule
   */
  void setDeleteRule(int delete_rule) {
    this.delete_rule = delete_rule;
  }

  /**
   * @return
   */
  int getUpdateRule() {
    return update_rule;
  }

  /**
   * @param update_rule
   */
  void setUpdateRule(int update_rule) {
    this.update_rule = update_rule;
  }

  /**
   * Wandelt den Text einer Update oder Delete-Rule in die Konstante aus
   * DatabaseMateData um. Default ist Cascade
   * 
   * @param s
   * @return
   */
  public static int getRule(String s) {
    int ret = DatabaseMetaData.importedKeyCascade;
    if (s == null) {
      return ret;
    } else if (s.equals("Cascade")) {
      return DatabaseMetaData.importedKeyCascade;
    } else if (s.equals("SetNull")) {
      return DatabaseMetaData.importedKeySetNull;
    } else if (s.equals("Restrict")) {
      return DatabaseMetaData.importedKeyRestrict;
    } else if (s.equals("NoAction")) {
      return DatabaseMetaData.importedKeyNoAction;
    } else if (s.equals("SetDefault")) {
      return DatabaseMetaData.importedKeySetDefault;
    }
    return ret;
  }

  /**
   * Wandelt eine Update- oder Delete-Rule in den entsprechenden Text um.
   * 
   * @param rule
   * @return
   */
  public static String getRule(int rule) {
    switch (rule) {
    case DatabaseMetaData.importedKeyCascade:
      return "Cascade";
    case DatabaseMetaData.importedKeySetNull:
      return "SetNull";
    case DatabaseMetaData.importedKeyRestrict:
      return "Restrict";
    case DatabaseMetaData.importedKeyNoAction:
      return "NoAction";
    case DatabaseMetaData.importedKeySetDefault:
      return "SetDefault";
    }
    return "NoAction";
  }

  // Methods
  /*
   * public wegen test
   */
  public Element getElement() {
    Element tblEle = null;
    switch (this.requestType) {
    case ROOT_TABLE:
      tblEle = new Element("RootTable");
      break;
    case CHILD_TABLE:
      tblEle = new Element("Child");
      break;
    case PARENT_TABLE:
      tblEle = new Element("Parent");
      break;
    default: {
      String msg = "TableRequest#getElement: Illegal Table Request Type: "
          + this.getRefname();
      logger.error(msg);
      throw new IllegalStateException(msg);
    }
    }
    // Attributes
    tblEle.setAttribute("tablename", this.getTablename());
    if (isEmpty(this.viewname) == false) {
      tblEle.setAttribute("viewname", viewname);
    }
    if (isEmpty(this.myDataTable.getAlias()) == false) {
      tblEle.setAttribute("alias", myDataTable.getAlias());
    }
    if (this.getTablename().equals(this.getRefname()) == false) {
      tblEle.setAttribute("refname", this.getRefname());
    }
    if (this.distinct == true) {
      tblEle.setAttribute("x_distinct", "true");
    }
    if (isEmpty(this.pk) == false) {
      tblEle.setAttribute("pk", pk);
    }
    if (isEmpty(this.fk) == false) {
      tblEle.setAttribute("fk", fk);
    }
    if (isEmpty(this.sequence) == false) {
      tblEle.setAttribute("seq", sequence);
    }
    if (this.myDataTable.isSelfReference()) {
      tblEle.setAttribute("selfReference", "true");
    }
    /*
     * if (istLeer(this.myDataTable.getParentElementName()) == false) {
     * tblEle.setAttribute("Element", this.myDataTable.getParentElementName()); }
     */
    if (isEmpty(this.orderBy) == false) {
      tblEle.setAttribute("orderby", this.orderBy);
    }
    if (isEmpty(this.groupBy) == false) {
      tblEle.setAttribute("groupby", this.groupBy);
    }
    if (isEmpty(this.where) == false) {
      tblEle.setAttribute("where", this.where);
    }
    if (this.myDataTable.getJoin() != null) {
      tblEle.setAttribute("join", this.myDataTable.getJoin());
    }
    // Other JDBC
    if (this.fetchSize != -1) {
      tblEle.setAttribute("fetchSize", Convert.toString(this.fetchSize));
    }
    if (this.maxFieldSize != -1) {
      tblEle.setAttribute("maxFieldSize", Convert.toString(this.maxFieldSize));
    }
    if (this.maxRows != Integer.MAX_VALUE) {
      tblEle.setAttribute("maxRows", Convert.toString(this.maxRows));
    }
    if (this.queryTimeout != Integer.MAX_VALUE) {
      tblEle.setAttribute("queryTimeout", Convert.toString(this.queryTimeout));
    }
    if (this.maxExecutionTime != Integer.MAX_VALUE) {
      tblEle.setAttribute("maxExecutionTime", Integer.toString(this.maxExecutionTime));
    }
    // Columns
    for (JDataColumn col : columns) {
      Element colEle = col.getElement();
      tblEle.addElement(colEle);
    }
    if (this.update_rule != DatabaseMetaData.importedKeyNoAction) {
      tblEle.setAttribute("OnUpdate", getRule(this.update_rule));
    }
    if (this.delete_rule != DatabaseMetaData.importedKeyCascade) {
      tblEle.setAttribute("OnDelete", getRule(this.delete_rule));
    }
    // Childs
    if (this.childRequests != null) {
      for (TableRequest child : this.childRequests) {
        Element childEle = child.getElement();
        tblEle.addElement(childEle);
      }
    }
    // Parents
    if (this.parentRequests != null) {
      for (TableRequest parent : this.parentRequests) {
        Element parentEle = parent.getElement();
        tblEle.addElement(parentEle);
      }
    }
    return tblEle;
  }

  /**
   * Erzeugt die Child- und Parent Requests und -Tables.
   * <p>
   * Wird vom Constructor aufgerufen
   */
  private void createTables(DatabaseConnection dbConnection, Elements eles) {
    eles.first();
    while (eles.hasMoreElements()) {
      Element ele = eles.next();
      if (myRequest.isDebug()) {
        System.out.println(ele.toString());
      }
      if (ele.getName().equals("Child")) {
        // 1.1 Child Request
        TableRequest childRequest = new TableRequest(dbConnection, this.myRequest, ele, CHILD_TABLE);
        this.addChildRequest(childRequest);
        // 1.2 Child Table
        String sfk = ele.getAttribute("fk");
        {
          String[] fkColumns = sfk.split(",");
          int pkCols = this.myDataTable.getPKColumnsCount();
          int fkCols = fkColumns.length;
          if (pkCols != fkCols) {
            String msg = "TableRequest: '" + this.getRefname() + "'"
                + " Number of primary key columns '" + pkCols
                + "' != number of foreign key columns '" + fkCols + "'";
            logger.warn(msg);
          }
        }
        Relation rela = this.myDataTable.addChildTable(childRequest.getDataTable(), sfk);
        rela.setDeleteRule(childRequest.getDeleteRule());
        rela.setUpdateRule(childRequest.getUpdateRule());
      } else if (ele.getName().equals("Parent")) {
        // 2.1 Parent Request
        TableRequest parentRequest = new TableRequest(dbConnection, this.myRequest, ele, PARENT_TABLE);
        this.addParentRequest(parentRequest);
        // 2.2 Parent Table
        String sfk = ele.getAttribute("fk");
        this.myDataTable.addParentTable(parentRequest.getDataTable(), sfk);
      } else if (ele.getName().equals("Union")) {
        String s = ele.getTextString();
        UnionStatement us = new UnionStatement(s);
        this.addUnion(us);
      }
    }
  }

  // Will einen Iterator von DataRows
  private void createTables(DatabaseConnection dbConnection, Iterator<JDataRow> it) {
    if (it != null) {
      while (it.hasNext()) {
        JDataRow row = it.next();
        TableRequest req = new TableRequest(dbConnection, this.myRequest, row);
        if (req.getRequestType() == TableRequest.CHILD_TABLE) {
          String sfk = req.getFK();
          this.addChildRequest(req);
          this.myDataTable.addChildTable(req.getDataTable(), sfk);
        } else if (req.getRequestType() == TableRequest.PARENT_TABLE) {
          String sfk = req.getFK();
          this.addParentRequest(req);
          this.myDataTable.addParentTable(req.getDataTable(), sfk);
        } else {
          String msg = "Illegal TabelRequest Type: " + req.getRequestType() + " "
              + this.getRefname();
          logger.error(msg);
          throw new IllegalStateException(msg);
        }
      }
    }
  }
  
  private void addUnion(UnionStatement us) {
    if (this.unions == null) {
      unions = new ArrayList<UnionStatement>();
    }
    unions.add(us);
  }
  
  ArrayList<UnionStatement> getUnions() {
    return unions;
  }

  // Table and Columns
  /**
   * @deprecated Fügt eine DataColumn hinzu vom sql-Typ OTHER
   * @see #addColumn(JDataColumn)
   */
  public JDataColumn addColumn(String name) {
    JDataColumn col = new JDataColumn(this.myDataTable, name, Types.OTHER);
    this.columns.add(col);
    return col;
  }

  /**
   * @deprecated
   * @param name
   * @param alias
   * @return
   * @see #addColumn(JDataColumn)
   */
  public JDataColumn addColumn(String name, String alias) {
    JDataColumn col = new JDataColumn(this.myDataTable, name, alias, Types.OTHER);
    this.columns.add(col);
    return col;
  }

  /**
   * Fügt der Abfrage eine weitere Spalte hinzu.
   * 
   * @param col
   */
  public void addColumn(JDataColumn col) {
    this.columns.add(col);
  }

  void addColumns(Elements eles) {
    eles.first();
    while (eles.hasMoreElements()) {
      Element ele = eles.next();
      JDataColumn col = new JDataColumn(this.myDataTable, ele);
      this.addColumn(col);
    }
  }

  private void addColumns(JDataRow parentRow) {
    Iterator<JDataRow> it = parentRow.getChildRows("TableColumn");
    if (it != null) {
      while (it.hasNext()) {
        JDataRow colRow = it.next();
        JDataColumn col = new JDataColumn(this.myDataTable, colRow);
        this.addColumn(col);
      }
    }
  }

  /**
   * Liefert einen Iterator über die JDataColumns dieser Abfrage.
   * 
   * @return
   * @see JDataColumn
   */
  public Iterator<JDataColumn> getColumns() {
    return columns.iterator();
  }

  void fillDataset(DatabaseConnection dbConnection, JDataSet dataset) throws PLException {
    if (this.isTransient())
      return;
    this.fillDataset(dbConnection, dataset, (SqlCondition) null);
  }

  /**
   * Wird von Request aufgerufen
   * 
   * @param ds
   * @param oid
   */
  void fillDataset(DatabaseConnection dbConnection, JDataSet dataset, long oid)
      throws PLException {
    if (this.isTransient())
      return;
    SqlCondition cond = new SqlCondition(this.pk, Long.toString(oid), Types.BIGINT);
    this.fillDataset(dbConnection, dataset, cond);
  }
  void fillDataset(DatabaseConnection dbConnection, JDataSet dataset, long[] oids)
      throws PLException {
    if (this.isTransient())
      return;
    SqlCondition cond = new SqlCondition(this.pk, oids);
    this.fillDataset(dbConnection, dataset, cond);
  }

  void fillDataset(DatabaseConnection dbConnection, JDataSet dataset, String key)
      throws PLException {
    if (this.isTransient())
      return;
    SqlCondition cond = new SqlCondition(this.pk, key, Types.VARCHAR);
    this.fillDataset(dbConnection, dataset, cond);
  }

  /**
   * RootTable Request
   * 
   * @param dataset
   * @param cond
   * @throws SQLException
   */
  void fillDataset(DatabaseConnection dbConnection, JDataSet dataset, SqlCondition cond)
      throws PLException {
    if (this.isTransient())
      return;
    // Statement bilden (das dauert nicht lange!)
    dataset.startProfiler("executeQuery");
    // Get ResultSet
    SelectStatement selectStatement = this.createSelectStatement(cond);
    // ResultSet bilden
    ResultSet rs = selectStatement.executeQuery(dbConnection);
    dataset.endProfiler();
    // ResultSet to DataRows
    this.rs2DataRows(dataset, rs);
    selectStatement.close(); // NEW ORACLE
    // über alle Zeilen des ResultSet
    Iterator<JDataRow> it = dataset.getChildRows();
    if (it != null) {
      while (it.hasNext()) {
        JDataRow childRow = it.next();
        // Childs Requests
        if (childRequests != null) {
          for (TableRequest childReq : this.childRequests) {
            if (childReq.isVirtualChild()) {
              String colName = childReq.getVirtualChild();
              String childRef = childReq.getRefname();
              try {
                int vCnt = childRow.getVirtualChilds(colName, childRef); // TODO:
                // childRef
                childRow.commitChanges(); // Damit "inserted" usw
                // durchgeschrieben
                // werden.
              } catch (ParseException pex) {
                String msg = "Error getting virtual child rows: " + pex.getMessage();
                logger.error(msg, pex);
                throw new PLException(msg, pex);
              }
            } else { // Nix virtual
              dataset.startProfiler("childRequest");
              childReq.getRequestChild(dbConnection, childRow, null);
              dataset.endProfiler();
            }
          }
        }
        // Parents Requests
        if (parentRequests != null) {
          for (TableRequest parentRequest : this.parentRequests) {
            // DataTable of Parent Request
            JDataTable parentTable = parentRequest.getDataTable();
            if (parentTable.getJoin() == null) { // kein Join: normal lesen
              // Perfom Parent Request
              dataset.startProfiler("parentRequest");
              parentRequest.getRequestParent(dbConnection, childRow, null);
              dataset.endProfiler();
            } else { // Wenn Join, dann Großvater; TODO: Rekursion fehlt!
              Iterator<TableRequest> ptrs = parentRequest.getParentRequests();
              if (ptrs != null) {
                while (ptrs.hasNext()) {
                  TableRequest ptr = ptrs.next();
                  // DataTable of Parent Request
                  JDataTable pparentTable = ptr.getDataTable();
                  if (pparentTable.getJoin() == null) {
                    // Perfom Parent Request
                    ptr.getRequestParent(dbConnection, childRow, null);
                  } else {
                    
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Root Table Request
   * 
   * @param dataset
   * @param parameters
   * @throws SQLException
   *           TODO: Methode ist (fast) identisch
   *           #getRequest(DatabaseConnection, JDataSet, SqlCondition)
   */
  void fillDataset(DatabaseConnection dbConnection, JDataSet dataset,
      ParameterList parameters) throws PLException {
    // Statement
    dataset.startProfiler("executeQuery");
    SelectStatement selectStatement = this.createSelectStatement(parameters);
    // ResultSet bilden
    ResultSet rs = selectStatement.executeQuery(dbConnection, parameters, null);
    dataset.endProfiler();
    this.rs2DataRows(dataset, rs);
    selectStatement.close(); // Neu 15.9.2018
    // über alle Zeilen des ResultSet
    if (dataset.getChildRows() != null) {
      for (Iterator<JDataRow> it = dataset.getChildRows(); it.hasNext();) {
        JDataRow childRow = it.next();
        // Childs
        if (childRequests != null) {
          for (TableRequest childReq : this.childRequests) {
            if (childReq.isVirtualChild()) {
              String colName = childReq.getVirtualChild();
              String childRef = childReq.getRefname();
              try {
                int vCnt = childRow.getVirtualChilds(colName, childRef); // TODO:
                // childRef
              } catch (ParseException pex) {
                String msg = "Error getting virtual child rows: " + pex.getMessage();
                logger.error(msg, pex);
                throw new PLException(msg, pex);
              }
            } else { // Nix virtual
              dataset.startProfiler("childRequest");
              childReq.getRequestChild(dbConnection, childRow, parameters);
              dataset.endProfiler();
            }
          }
        }
        // Parents
        if (parentRequests != null) {
          for (TableRequest parent : this.parentRequests) {
            JDataTable parentTable = parent.getDataTable();
            if (parentTable.getJoin() == null) {
              dataset.startProfiler("parentRequest");
              parent.getRequestParent(dbConnection, childRow, parameters);
              dataset.endProfiler();
            }
          }
        }
      }
    }
  }

  /**
   * Child Requests (rekursiv)
   * 
   * @param parentRow
   */
  void getRequestChild(DatabaseConnection dbConnection, JDataRow parentRow,
      ParameterList parameters) throws PLException {
    if (this.isTransient())
      return;
    ArrayList<JDataRow> rows = null;
    // pkValues
    ArrayList<JDataValue> pkValues = parentRow.getPrimaryKeyValues();
    // Statement
    SelectStatement selectStatement = null;
    if (parameters != null) {
      selectStatement = this.createSelectStatement(parameters);
    } else {
      selectStatement = this.createSelectStatement((SqlCondition) null);
    }
    selectStatement.setMaxExecutionTime(this.maxExecutionTime);
    // JOIN; Conditions
    /*
     * ### Die Anzahl der Felder bei Child darf auch geringer als die bei Parent sein; nicht aber größer
     */
    
    if (pkValues.size() < selectStatement.getJoinCount()) {
      String msg = "Number of FK-Fields " 
         + selectStatement.getJoinCount()
         + " > # PK-Values "
         + pkValues.size() + " "
         + this.getRefname();
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }
    selectStatement.setJoinValue(pkValues);
    
//    for (int i = 0; i < this.joinConditions.size(); i++) {
//      SqlCondition cond = this.joinConditions.get(i);
//      JDataValue val = pkValues.get(i);
//      cond.setValue(val.getValue());
//    }
    // Get ResultSet
    ResultSet rs = selectStatement.executeQuery(dbConnection, parameters, parentRow);
    // Bei Parent darf das nicht mehr als einer sein!!!
    rows = this.rs2DataRows(rs);
    selectStatement.close(); // NEW ORACLE
    if (rows != null) {
      for (int r = 0; r < rows.size(); r++) {
        JDataRow childRow = rows.get(r);
        JDataRow childParentRow = null;
        if (this.myDataTable.getJoin() != null) {
          // Columns from child Table to parent Table
          // TODO Das geht nur gut, wenn nur eine Root Row
          // und wenn alle Felder ausgefüllt.
          // Geht schief, wenn viele Root Rows vorhanden sind,
          // weil dann immer wieder den ParentTable die
          // Columns hinzugefügt werden;
          // Führt dann zu vielen Fehlern im Protokoll.
          // Es muß am Anfang sichergestellt werden,
          // daß die ParentTable alle Columns enthält
          // (auch die aus dem JOINED Child Tables).
          // Das parentTable#addColumn muß hier raus
          // und zu createMetaData
          if (r == 0) { // zuerst die Spalten umtüten
            JDataTable childTable = childRow.getDataTable();
            JDataTable parentTable = parentRow.getDataTable();
            Iterator<JDataColumn> iCols = childTable.getDataColumns();
            if (iCols != null) {
              JDataColumn col =  iCols.next();
              try {
                parentTable.addColumn(col);
              } catch (Exception ex) {
                logger.error("Cant add DataColumn: " + col.getColumnName()
                    + " to DataTable: " + parentTable.getTablename());
              }
            } else {
              logger.warn("Missing DataColumns in joined child table: "
                  + childTable.getTablename());
            }
          }
          // Values
          Iterator<JDataValue> iVals = childRow.getDataValues();
          if (iVals != null) {
            // TODO Iterator??
            JDataValue val = iVals.next();
            parentRow.addDataValue(val);
          }
          childParentRow = parentRow;
        } else { // Nix Join
          parentRow.addChildRow(childRow);
          childParentRow = childRow;
        }
        // Childs
        if (childRequests != null) {
          for (TableRequest childReq : this.childRequests) {
            if (childReq.isVirtualChild()) {
              String colName = childReq.getVirtualChild();
              String childRef = childReq.getRefname();
              try {
                int vCnt = childRow.getVirtualChilds(colName, childRef); // TODO:
                // childRef
                childRow.commitChanges(); // Damit "inserted" usw
                // durchgeschrieben
                // werden.
              } catch (ParseException pex) {
                String msg = "Error getting virtual child rows: " + pex.getMessage();
                logger.error(msg, pex);
                throw new PLException(msg, pex);
              }
            } else { // Nix virtual
              childReq.getRequestChild(dbConnection, childParentRow, parameters);
            }
          }
        }
        // Parents
        if (parentRequests != null) {
          for (TableRequest parent : this.parentRequests) {
            JDataTable parentTable = parent.getDataTable();
            if (parentTable.getJoin() == null) {
              parent.getRequestParent(dbConnection, childParentRow, parameters);
            }
          }
        }
      }
    }
  }

  /**
   * @param parentRow
   * @param parameters
   */
  void getRequestParent(DatabaseConnection dbConnection, JDataRow parentRow,
      ParameterList parameters) throws PLException {
    if (this.isTransient())
      return;
    if (this.myDataTable.getJoin() != null) { // TODO: geht das gut? was passiert mit tiefer gestaffelten Parents die nicht join sind?
       return;
    }
    ArrayList<JDataValue> fkValues = parentRow.getFKValues(this.fk);
    // Statement
    SelectStatement selectStatement = null;
    if (parameters != null) {
      selectStatement = this.createSelectStatement(parameters);
    } else {
      selectStatement = this.createSelectStatement((SqlCondition) null);
    }

    // JOIN; Conditions
    if (fkValues.size() != selectStatement.getJoinCount()) {
      String msg = "Number of FK-Fields "  
      + selectStatement.getJoinCount()
      + " > # FK-Values "
      + fkValues.size() + " "
      + this.getRefname();
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }    
    // Join Conditions mit Werten versorgen
    selectStatement.setJoinValue(fkValues);
    
//    for (int i = 0; i < this.joinConditions.size(); i++) {
//      SqlCondition cond = this.joinConditions.get(i);
//      JDataValue val = fkValues.get(i);
//      cond.setValue(val.getValue());
//    }
    // ResultSet bilden
    ResultSet rs = selectStatement.executeQuery(dbConnection, parameters, parentRow);
    // ResultSet to DataRows
    ArrayList<JDataRow> rows = this.rs2DataRows(rs);
    selectStatement.close(); // NEW ORACLE
    if (rows != null) {
      // Bei Parent darf das nicht mehr als eine sein!!!
      for (int r = 0; r < rows.size(); r++) {
        JDataRow row = rows.get(r);
        parentRow.addParentRow(row);
        // Parents
        if (parentRequests != null) {
          for (int i = 0; i < this.parentRequests.size(); i++) {
            TableRequest parent = parentRequests.get(i);
            parent.getRequestParent(dbConnection, row, parameters);
          }
        }
      }
    }
  }

  /**
   * Für RootTable Request
   * 
   * @param dataset
   * @param rs
   */
  private void rs2DataRows(JDataSet dataset, ResultSet rs) throws PLException {
     int rows = 0;
    try {
      dataset.startProfiler("getResutSet data");
      ResultSetMetaData meta = rs.getMetaData();
      // Prüfen auf maxRows, wenn von JDBC-Driver nicht unterstützt
      while (rs.next()) { // in seltenen Fällen gibts hier einen Fehler, weil der ResultSet inzwischen Closed ist
        rows++;
        // Eine Row aus dem ResultSet wird in eine
        // ArrayList von JDataValues verwandelt.
        ArrayList<JDataValue> al = rs2ArrayList(this.myDataTable, rs, meta);
        // Row erzeugen
        JDataRow row = new JDataRow(this.myDataTable, al); // TODO: DataTable aus DataSet entnehmen!
        // Neue Row an DataSet hängen
        dataset.addChildRow(row);
        if (rows == maxRows) {
          break;
        }
      } // wend rs.next()
    } catch (SQLException e) {
      String msg = "PL [" + getLayerName() + "] rs2DataRows: Error retrieving ResultSet[" + rows +"]: " + e.getMessage();
      logger.error(msg, e);
      throw new PLException(msg, e);
    } finally {
      try {
        rs.close();
        //##rs.getStatement().close(); // NEW ORACLE
        dataset.endProfiler();
      } catch (SQLException e) {
        logger.warn("PL [" + getLayerName() + "] rs2DataRows: Error closing result set! " + e.getMessage());
      }
    }
  }

  /**
   * Für Child und Parent
   * 
   * @param rs
   */
  private ArrayList<JDataRow> rs2DataRows(ResultSet rs) throws PLException {
    try {
      ArrayList<JDataRow> ret = new ArrayList<JDataRow>();
      ResultSetMetaData meta = rs.getMetaData();
      // Prüfen auf maxRows, wenn von JDBC-Driver nicht unterstützt
      int rows = 0;
      while (rs.next()) {
        rows++;
        // Eine Row aus dem ResultSet wird in eine
        // ArrayList von JDataValues verwandelt.
        ArrayList<JDataValue> al = rs2ArrayList(this.myDataTable, rs, meta);
        JDataRow row = new JDataRow(this.myDataTable, al);
        ret.add(row);
        if (rows == maxRows) {
          break;
        }
      } // wend rs.next()
      return ret;
    } catch (SQLException e) {
      String msg = "PL [" + getLayerName()
          + " ] rs2DataRows: Error retrieving metadata: " + e.getMessage();
      logger.error(msg, e);
      throw new PLException(msg, e);
    } finally {
      try {
        rs.close();
      } catch (SQLException e) {
        logger.warn("PL [" + getLayerName() + " ] rs2DataRows: Error closing result set!");
      }
    }
  }

  private ArrayList<JDataValue> rs2ArrayList(JDataTable tbl, ResultSet rs, ResultSetMetaData meta) throws SQLException {
    int colCount = meta.getColumnCount();
    ArrayList<JDataValue> al = new ArrayList<JDataValue>(colCount);
    for (int i = 1; i <= colCount; i++) {
      final String colName = meta.getColumnName(i);
      try {
        // TODO: Statt Object besser in Abhängigkeit vom Type spezifische Objekte einlesen und erst dann toString (wegen Oracle)
        JDataColumn col = tbl.getDataColumn(colName);
        Object value = null;
        /*
         * Dieses switch ist notwendig, da Oracle-spezifische Klassen herausgegeben werden
         * Bei NUMBER(x,y) macht Oracle daraus immer ein BigDecimal
         */
        switch (col.getDataType()) {
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
      } catch (SQLException ex) {
         String colInfo = tbl.getTablename() + " ["+i+"/"+colCount+"] " + colName;
         if (rs.isClosed()) {
            logger.warn("ResultSet is closed:" + colInfo);
         }
         logger.error("DataTable: " + colInfo, ex);
         throw ex;
      }
    }
    // Transiente Spalten?
    Iterator<JDataColumn> it = tbl.getDataColumns();
    if (it != null) {
      while (it.hasNext()) {
        JDataColumn col = it.next();
        if (col.isTransient()) {
          JDataValue val = new JDataValue(col, col.getDefaultValue());
          al.add(val);
        }
      }
    }

    return al;
  }
  /**
   * Vorbereitung das DataSet:
   * PKs vergeben, FKs verknüpfen
   * @param dbConnection
   * @param dataset
   * @param row
   * @return
   */
  int prepareSetDataset(DatabaseConnection dbConnection, JDataSet dataset, JDataRow row) throws PLException {
    int cnt = 0;
    // 0. Check DataTable
    JDataTable tbl = row.getDataTable();
    if (tbl.getTablename().equalsIgnoreCase(this.myDataTable.getTablename()) == false) {
      String msg = "JDataTable mismatch; expected: '" + this.myDataTable.getTablename()
          + "' got: '" + tbl.getTablename() + "'";
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }
    // 0. Vorbereitung; hier für virtual='true'
    @SuppressWarnings("unused")
    int preChilds = this.preProcessRow(row);
    // 1. Insert, Update
    if (row.isInserted() && row.isDeleted()) { // new 4.8.2004
      // 1.1 Nix machen, wenn sowohl inserted als auch deleted
    } else if (row.isInserted()) {
      // 1.2 Insert
      int tmp = this.prepareInsertRow(dbConnection, dataset, row);
      cnt += tmp;
    }
    // 2. Childs, Parents(?)
    int childs = this.prepareSetDataset2(dbConnection, dataset, row);
    cnt += childs;
    return cnt;
  }
  /**
   * UPDATE, DELETE, INSERT eine Row (und deren Kinder veranlassen)
   * @param rows
   * @return
   */
  int setDataset(DatabaseConnection dbConnection, JDataSet dataset, JDataRow row)
      throws PLException {
    // 0. Check DataTable
//    JDataTable tbl = row.getDataTable();
//    if (tbl.getTablename().equalsIgnoreCase(this.myDataTable.getTablename()) == false) {
//      String msg = "JDataTable mismatch; expected: '" + this.myDataTable.getTablename()
//          + "' got: '" + tbl.getTablename() + "'";
//      logger.error(msg);
//      throw new IllegalArgumentException(msg);
//    }
//    // 0. Vorbereitung; hier für virtual='true'
//    int preChilds = this.preProcessRow(row);
    // 1. Insert, Update
    int cnt = 0;
    if (row.isInserted() && row.isDeleted()) { // new 4.8.2004
      // 1.1 Nix machen, wenn sowohl inserted als auch deleted
    } else if (row.isInserted()) {
      // 1.2 Insert Jetzt mit addBatch!
      //## int tmp = this.insertRow(dbConnection, dataset, row); 
      //##cnt += tmp;
    } else if (row.isModifiedPersistent()) {
      // 1.3 Update
      int tmp = this.updateRow(dbConnection, dataset, row);
      cnt += tmp;
    }
    // 2. Childs, Parents(?)
    int childs = this.setDataset2(dbConnection, dataset, row);
    cnt += childs;
    // 3. Delete erst nach den Childs!
    if (row.isInserted() && row.isDeleted()) { // new 4.8.2004
      // 3.1 Nix machen, wenn sowohl inserted als auch deleted
    } else if (row.isDeleted()) {
      // 3.2
      cnt += this.deleteRow(dbConnection, row);
    }
    return cnt;
  }

   /**
   * Diese Methode wird aufgerufen *bevor* die übergebene Row in die Datenbank
   * geschrieben wird.
   *
   * @param row
   * @return Die Anzahl der umgetüteten Zeilen
   */
  private int preProcessRow(JDataRow row) {
    int cnt = 0;
    Iterator<JDataRow> i = row.getChildRows();
    if (i != null) {
      while (i.hasNext()) {
        JDataRow childRow = i.next();
        String childRefname = childRow.getDataTable().getRefname();
        TableRequest childReq = this.getChildRequest(childRefname);
        // TODO: childReq kann hier null werden?
        if (childReq != null && childReq.isVirtualChild()) {
          String colName = childReq.getVirtualChild();
          cnt += row.setVirtualChilds(colName, childRefname);
        } else {
          // nix
        }
      }
    }
    return cnt;
  }
  private int prepareSetDataset2(DatabaseConnection dbConnection, JDataSet dataset, JDataRow row)
    throws PLException {
      int cnt = 0;
      { // 1. Childs
        Iterator<JDataRow> i = row.getChildRows();
        if (i != null) {
          while (i.hasNext()) {
            JDataRow childRow = i.next();
            String childRefname = childRow.getDataTable().getRefname();
            TableRequest childReq = this.getChildRequest(childRefname);
            if (childReq != null) {
              if (childReq.isVirtualChild()) {
                // Nix machen; siehe #preProcessChildRows
              } else {
                int tmp = childReq.prepareSetDataset(dbConnection, dataset, childRow);
                cnt += tmp;
              }
            }
          }
        }
      }
      { // 2. Parents
        // TODO : Muß das nicht raus? Auf Parents keine Update!?
        Iterator<JDataRow> i = row.getParentRows();
        if (i != null) {
          while (i.hasNext()) {
            JDataRow parentRow = i.next();
            String parentRefname = parentRow.getDataTable().getRefname();
            TableRequest parentReq = this.getParentRequest(parentRefname);
            int tmp = parentReq.prepareSetDataset(dbConnection, dataset, parentRow);
            cnt += tmp;
          }
        }
      }
      return cnt;
  }
  /**
   * Alle ChildRows verarbeiten ParentRows immer weglassen, weil immer
   * readonly?!
   * 
   * @param row
   * @return
   */
  private int setDataset2(DatabaseConnection dbConnection, JDataSet dataset, JDataRow row)
      throws PLException {
    int cnt = 0;
    { // 1. Childs
      Iterator<JDataRow> i = row.getChildRows();
      if (i != null) {
        while (i.hasNext()) {
          JDataRow childRow = i.next();
          String childRefname = childRow.getDataTable().getRefname();
          TableRequest childReq = this.getChildRequest(childRefname);
          if (childReq != null) {
            if (childReq.isVirtualChild()) {
              // Nix machen; siehe #preProcessChildRows
            } else {
              int tmp = childReq.setDataset(dbConnection, dataset, childRow);
              cnt += tmp;
            }
          }
        }
      }
    }
    { // 2. Parents
      // TODO : Muß das nicht raus? Auf Parents keine Update!?
//      24.1.2012: Richtig! Insert weglassen, aber Update ausführen ist großer Mist!
//      Iterator<JDataRow> i = row.getParentRows();
//      if (i != null) {
//        while (i.hasNext()) {
//          JDataRow parentRow = i.next();
//          String parentRefname = parentRow.getDataTable().getRefname();
//          TableRequest parentReq = this.getParentRequest(parentRefname);
//          int tmp = parentReq.setDataset(dbConnection, dataset, parentRow);
//          cnt += tmp;
//        }
//      }
    }
    return cnt;
  }
  
  int insertBatchRows(DatabaseConnection dbConnection, JDataSet dataset) throws PLException {
    if (isVirtualChild()) return 0;
    Iterator<JDataRow> rows = dataset.getChildRows();
    if (rows == null) return 0;
    InsertStatement stmt = null;
    // 1. My Rows
    while (rows.hasNext()) {
      JDataRow row = rows.next();
      if (row.isInserted() && !row.isDeleted()) {
        if (stmt == null) {
          stmt = new InsertStatement(this, this.getTablename()); 
          stmt.setMaxExecutionTime(this.maxExecutionTime);
        }            
        stmt.addBatch(dbConnection, row, false);
      }      
    }
    int cnt = 0;
    if (stmt != null) {
      // Statement ausführen
      long _startTime = System.currentTimeMillis();
      cnt += stmt.executeBatch();
      // Zeit messen
      long _dura = System.currentTimeMillis() - _startTime;
      if (_dura > stmt.getMaxExecutionTime()) {
        Logger slowLog = myRequest.getDatabase().getSlowQueryLogger();
        if (slowLog != null) {
          String msg = "MaxExecutionTime exceeded: " + _dura + ">" + maxExecutionTime + " Statement: " + stmt.getSql();
          slowLog.warn(msg);
        }
      }
    }
    // 2. Childs
    if (this.childRequests != null) {
      for(TableRequest req:this.childRequests) {
        cnt += req.insertBatchChildRows(dbConnection, dataset.getChildRows());
      }
    }
    // 3. Parents??
    if (this.parentRequests != null) {
      for (TableRequest req: this.parentRequests) {
        cnt += req.insertBatchChildRows(dbConnection, dataset.getChildRows());
      }
    }
    return cnt;
  }
  
  int insertBatchChildRows(DatabaseConnection dbConnection, Iterator<JDataRow> parentRows) throws PLException {
    if (parentRows == null) return 0;
    if (isVirtualChild()) return 0;
    InsertStatement stmt = null;
    ArrayList<JDataRow> alParent = new ArrayList<JDataRow>();
  
    while (parentRows.hasNext()) {
      JDataRow row = parentRows.next();
      Iterator<JDataRow> childRows = row.getChildRows(this.getRefname());
      if (childRows != null) {
        while(childRows.hasNext()) {
          JDataRow childRow = childRows.next();
          alParent.add(childRow);
          if (childRow.isInserted() && !childRow.isDeleted()) {
            if (stmt == null) {
              stmt = new InsertStatement(this, this.getTablename()); 
              stmt.setMaxExecutionTime(this.maxExecutionTime);
            }            
            stmt.addBatch(dbConnection, childRow, false);
          }      
        }
      }
    }
    int cnt = 0;
    if (stmt != null) {
      // StatementInfo
      TransactionInfo tInfo = dbConnection.getDatabase().getTransactionInfo(dbConnection.getId());
      if (tInfo != null) {
        tInfo.addStatement(dbConnection, stmt);
      }
      // Statement ausführen
      long _startTime = System.currentTimeMillis();
      cnt += stmt.executeBatch();
      // Zeit messen
      long _dura = System.currentTimeMillis() - _startTime;
      if (_dura > stmt.getMaxExecutionTime()) {
        Logger slowLog = myRequest.getDatabase().getSlowQueryLogger();
        if (slowLog != null) {
          String msg = "MaxExecutionTime exceeded: " + _dura + ">" + maxExecutionTime + " Statement: " + stmt.getSql();
          slowLog.warn(msg);
        }        
      }
    }
    // Childs
    if (this.childRequests != null) {
      for(TableRequest req:this.childRequests) {
        cnt += req.insertBatchChildRows(dbConnection, alParent.iterator());
      }
    }
    // Parents?
    if (this.parentRequests != null) {
      for (TableRequest req: this.parentRequests) {
        cnt += req.insertBatchChildRows(dbConnection, alParent.iterator());
      }
    }
    return cnt;
  }
  
  private int updateRow(DatabaseConnection dbConnection, JDataSet dataset, JDataRow row)
      throws PLException {
    int cnt = 0;
    UpdateStatement stmt = this.getUpdateStatement(dbConnection, dataset, row);
    if (stmt == null) {
      String msg = "TableRequest#updateRow: '" + this.getTablename()
          + "' Missing Update Statement";
      logger.error(msg);
      throw new IllegalStateException(msg);
    }
    long _startTime = System.currentTimeMillis();
    cnt = stmt.executeUpdate(dbConnection);
    long _dura = System.currentTimeMillis() - _startTime;
    if (_dura > maxExecutionTime) {
      Logger slowLog = myRequest.getDatabase().getSlowQueryLogger();
      if (slowLog != null) {
        String msg = "MaxExecutionTime exceeded: " + _dura + ">" + maxExecutionTime + " Statement: " + stmt.getSql();
        slowLog.warn(msg);
      }        
    }
    if (cnt != 1) {
      // Führt zum Rollback
      // Wenn es sich um optimistisches Locking handelt, hier rausfummeln
      // und gesonderte Exception werfen
      // TODO: Wenn man jetzt noch ermittelt, wer, wann den Datensatz geändert hat...
      logger.error(stmt.toString());
      logger.debug(row);
      if (cnt == 0 && stmt.isOptimistic()) {
         // TODO: UpdateStatement: Update optional ohne OL wiederholen
        // Betroffene Zeile in die Exception eintragen
        throw new PLException("Update auf Tabelle '"+ this.getTablename() + "' mißglückt; Datensatz zwischenzeitlich geändert/gelöscht?", true, row);
      } else {
        throw new IllegalStateException("TableRequest#updateRow: '" + this.getTablename()
            + "' Number of updated Rows '" + cnt + "' != 1");
      }
    }
    return cnt;
  }

  private int prepareInsertRow(DatabaseConnection dbConnection, JDataSet dataset, JDataRow row)
      throws PLException {
    int cnt = 0;
    // Foreign Key versorgen; Vom Parent holen (das machen wir nur, wenn das eine Child Row ist!)
    if (row.getDataTable().getTableType() == JDataTable.CHILD_TABLE) {
      JDataRow parentRow = row.getParentRow();
      if (parentRow == null) {
        String msg = "#getInsertStatement getParentRow: Missing Parent Row! Request: "
            + this.getRefname();
        logger.error(msg);
        throw new IllegalStateException(msg);
      }
      ArrayList<JDataValue> pal = parentRow.getPrimaryKeyValues(); // Primary Key Values
      ArrayList<JDataValue> fal = row.getFKValues(this.fk); // Foreign Key values
      if (pal.size() != fal.size()) {
        // Darf nicht sein!
        String msg = "Number of primary key columns != number of foreign key columns; Request: "
            + this.getRefname();
        logger.error(msg);
        throw new IllegalStateException(msg);
      }
      // Copy from Parent Row into Child Row:
      // Master Primary Key Values into Detail Foreign Key Values
      // Das Ganze geht nur dann gut, wenn die Spalten bei pk= und fk=
      // in der passenden Reihenfolge angegeben wurden!
      Iterator<JDataValue> fi = fal.iterator();
      for (Iterator<JDataValue>  pi = pal.iterator(); pi.hasNext();) {
        JDataValue pval = pi.next();
        JDataValue fval = fi.next();
        fval.setValue(pval.getValue());
      }
    }
    // Primary Key mit frischen IDs versorgen wenn diese noch fehlen; muss nach
    // FK
    // erfolgen! (Wieso danach??)
    // Der Client erfährt von diesen neuen PKs erstmal nix; er muß im Prinzip
    // den
    // DataSet neu lesen.
    /*
     * 16.8.2004 / PKÖ Es muß möglich sein, auch Daten einzufügen, die keinen
     * PrimaryKey haben. Leider wirft JDataRow#getPrimaryKeyValues eine
     * Exception, wenn kein PK definiert ist.
     */
    int pkCnt = row.getDataTable().getPKColumnsCount();
    if (pkCnt != 0) {
      ArrayList<JDataValue>  pal = row.getPrimaryKeyValues();
      for (Iterator<JDataValue>  i = pal.iterator(); i.hasNext();) {
        JDataValue pval = i.next();
        String s = pval.getValue();
        if ((s == null || s.trim().length() == 0) && pval.getColumn().isAutoid() == false) {
          long oid;
          try {
            if (this.sequence != null) {
              oid = dbConnection.getNextSequence(sequence);
            } else {
              oid = dbConnection.getOID(false);
            }
            pval.setValue(oid);
          } catch (PLException e) {
            String msg = "PL [ " + dbConnection.getLayerName()
                + " ] Error creating insert statement " + "for dataset [ "
                + dataset.getDatasetName() + " ] " + e.getMessage();
            logger.error(msg, e);
            throw new PLException(msg, e);
          }
        }
      }
    }
    // Insert: Optimistic / Version="1"
    String optimisticField = dbConnection.getOptimisticField();
    if (optimisticField != null) {
      try {
        JDataValue optiVal = row.getDataValue(optimisticField);
        if (optiVal.getValue() == null) {
          optiVal.setValue("1");
        } else {
          // TODO : Wenn nicht im DataSet enthalten, dann Version hier anhängen
          // oder sich auf Datenbank verlassen (default = 0)
        }
      } catch (Exception ex) {
      } // Nix machen, wenn die Column fehlt
    }
    // CreateUser
    String createUserField = dbConnection.getCreateUserField();
    if (createUserField != null) {
      try {
        JDataValue createVal = row.getDataValue(createUserField);
        if (createVal.getValue() == null) {
          String fld = this.getTimestamp() + " " + dataset.getUsername();
          // Sicherstellen, daß der Wert in das Feld paßt.
          int size = createVal.getColumn().getSize();
          if (fld.length() > size) {
            fld = fld.substring(0, size);
          }
          createVal.setValue(fld);
        }
      } catch (Exception ex) {
      }
    }

    return cnt;
  }
//  /**
//   * @deprecated INSERT ohne BATCH (veraltet)
//   * @param dbConnection
//   * @param dataset
//   * @param row
//   * @return
//   * @throws PLException
//   */
//  private int insertRow(DatabaseConnection dbConnection, JDataSet dataset, JDataRow row)
//      throws PLException {
//    int cnt = 0;
//    InsertStatement stmt = this.getInsertStatement(row);
//    if (stmt == null) {
//      logger.error("Insert-Statement without columns: " + row.toString());
//      return 0;
//    }
//    if (isDebug()) {
//      System.out.println(stmt.toString());
//    }
//    cnt = stmt.executeUpdate(dbConnection);
//    if (cnt != 1) {
//      logger.error(stmt.toString());
//      throw new IllegalStateException(
//          "TableRequest#insertRow Insert failure: Number of inserted Rows != 1: " + cnt);
//    }
//    return cnt;
//  }

  private int deleteRow(DatabaseConnection dbConnection, JDataRow row)
      throws PLException {
    int cnt = 0;
    DeleteStatement stmt = this.getDeleteStatement(dbConnection, row);
    // Aber wir müssen den Primärschlüssel setzen.
    stmt.fillPKValues(dbConnection, row);
    if (isDebug()) {
      logger.debug(stmt.toString());
    }
    long _startTime = System.currentTimeMillis();
    cnt = stmt.executeUpdate(dbConnection);
    long _dura = System.currentTimeMillis() - _startTime;
    if (_dura > maxExecutionTime) {
      Logger slowLog = myRequest.getDatabase().getSlowQueryLogger();
      if (slowLog != null) {
        String msg = "MaxExecutionTime exceeded: " + _dura + ">" + maxExecutionTime + " Statement: " + stmt.getSql();
        slowLog.warn(msg);
      }        
    }
    if (cnt != 1) {
      logger.error(stmt.toString());
      logger.debug(row);
      if (cnt == 0 && stmt.isOptimistic()) {
         // Betroffene Zeile in die Exception eintragen
         throw new PLException("Delete in Tabelle '"+ this.getTablename() + "' mißglückt; Datensatz zwischenzeitlich geändert/gelöscht?", true, row);
       } else {
          throw new IllegalStateException("Delete failure: Number of deleted Rows != 1: " + cnt);
       }
    }
    return cnt;
  }

  // Statements ################################
  private SelectStatement createSelectStatement(SqlCondition cond) throws PLException {
    SelectStatement selectStatement = new SelectStatement(this, this.getViewname());
    selectStatement.setColumns(this.columns);
    selectStatement.setOrderBy(this.orderBy);
    selectStatement.setGroupBy(this.groupBy);
    selectStatement.setMaxRows(this.maxRows);

    if (this.where != null) {
      selectStatement.addCondition(new SqlCondition(this.where));
    }
    if (cond != null) {
      selectStatement.addCondition(cond);
    }
    // Join
    switch (this.requestType) {
    case CHILD_TABLE:
      this.createJoin(this.fk, selectStatement);
      break;
    case PARENT_TABLE:
      this.createJoin(this.pk, selectStatement);
      break;
    }
    this.perfParentTables(selectStatement);

    return selectStatement;
  }

  private SelectStatement createSelectStatement(ParameterList parameters)
      throws PLException {
    SelectStatement selectStatement = new SelectStatement(this, this.getViewname());
    selectStatement.setColumns(this.columns);
    selectStatement.setOrderBy(this.orderBy);
    selectStatement.setGroupBy(this.groupBy);
    selectStatement.setMaxExecutionTime(this.maxExecutionTime); // default
    selectStatement.setMaxRows(this.maxRows); // default
    if (parameters != null) { // überladen, wenn angegeben
      selectStatement.setMaxExecutionTime(this.maxExecutionTime, parameters.getMaxExecutionTime());
      if (parameters.getMaxRows() != Integer.MAX_VALUE) {
         selectStatement.setMaxRows(parameters.getMaxRows());
      }
    }
    // Parameters
    if (this.where != null) {
      selectStatement.setParameter(where, parameters);
    }
    // Join
    switch (this.requestType) {
    case CHILD_TABLE:
      // pk, fk
      this.createJoin(this.fk, selectStatement);
      break;
    case PARENT_TABLE:
      // prüfen pk, fk
      this.createJoin(this.pk, selectStatement);
      break;
    default:
      // Nix machen
      break;
    }
    this.perfParentTables(selectStatement);
    return selectStatement;
  }

  /**
   * @deprecated Erzeugt ein spezielles Select-Statement, bei dem FROM und WHERE
   *             hier in einem String übergeben werden. Für handgeschnitzte
   *             Datenbankzugriffe.
   * @param from
   */
  SelectStatement createSelectStatement(String from) {
    SelectStatement selectStatement = new SelectStatement(this, this.getViewname(), from);
    selectStatement.setColumns(this.columns);
    selectStatement.setOrderBy(this.orderBy);
    selectStatement.setGroupBy(this.groupBy);
    return selectStatement;
  }

  /**
   * Versorgt ein Prepared Statement mit den Attributen aus DatabaseConfig.xml
   * 
   * @param stmt
   */
  void setStatementAttributes(PreparedStatement stmt) {
    try {
      if (this.fetchSize != -1) {
        stmt.setFetchSize(fetchSize);
      }
      if (this.maxRows != Integer.MAX_VALUE) {
        stmt.setMaxRows(maxRows);
      }
      if (this.maxFieldSize != -1) {
        stmt.setMaxFieldSize(maxFieldSize);
      }
      if (this.queryTimeout != Integer.MAX_VALUE && this.getRequest().getDatabase().getDatabaseType() != Database.HSQLDB) {
        stmt.setQueryTimeout(queryTimeout);
      }
    } catch (SQLException ex) {
      logger.error("TableRequest#setStatementAttributes", ex);
      System.err.println("TableRequest#setStatementAttributes: " + ex.getMessage());
    }
  }

  /**
   * Erzeugt das prepared SelectStatement für das Einlesen der MetaDaten. Wird
   * vom Constructor aufgerufen.
   */
  private void createMetaData(DatabaseConnection dbConnection, boolean repeat)
      throws PLException {
    ResultSet rs = null;
    try {
      // Statement
      SelectStatement metaSelectStatement = new SelectStatement(this, this.getViewname());
      // TODO : JOINED Tables mit berücksichtigen
      // System.out.println("createMeta "+this.getViewname());
      metaSelectStatement.setColumns(this.columns);
      // this.metaSelectStatement.setOrderBy(this.orderBy);
      metaSelectStatement.setGroupBy(this.groupBy); // neu 17.2.2004 PKÖ
      this.perfParentTables(metaSelectStatement);
      // Cond      
      SqlCondition cond = new SqlCondition("1=0");
      metaSelectStatement.addCondition(cond);
      logger.debug("Creating TableRequest: [" + this.myRequest.getDatasetName() + "] " + metaSelectStatement);
      rs = metaSelectStatement.executeQuery(dbConnection);
      ResultSetMetaData meta = rs.getMetaData();
      ArrayList<JDataColumn> _columns = this.createColumns(meta);
      if (_columns != null) {
        for (JDataColumn col : _columns) {
          try {
            this.myDataTable.addColumn(col);
          } catch (Exception ex) {
            // Wenn Join mit Parent Tables gibts hier doppelte Columns
            if (repeat == false) {
              logger.warn("Duplicate Columns " + this.getRefname(), ex);
            }
          }
        }
      } else {
        // System.out.println("Missing Columns: " + this.getTablename());
        logger.error("Missing Columns: " + this.getTablename());
      }
      // Transiente Columns hinzufügen (aber nur beim ersten Durchlauf!)
      if (repeat == false) {
        for (JDataColumn col: this.columns) {
          if (col.isTransient()) {
            this.myDataTable.addColumn(col);
          }
        }
      }
      // PK
      if (this.pk != null) {
        StringTokenizer toks = new StringTokenizer(pk, ",");
        int keySeq = 0;
        while (toks.hasMoreTokens()) {
          String tok = toks.nextToken();
          if (tok.startsWith(" ")) {
            tok = tok.trim();
          }
          try {
            // TODO : Offenbar gibts auch Request ohne PK??
            JDataColumn col = myDataTable.getDataColumn(tok);
            col.setPrimaryKey(true);
            keySeq++; // 1-relativ
            col.setKeySeq(keySeq);
          } catch (Exception ex) {
            // Diese Exception bedeutet,
            // daß bei einer Tabelle ein pk definiert wurde, der nicht mit
            // eingelesen wird, also bei den Columns nicht definiert ist.
            if (myDataTable.isReadonly() == false && myDataTable.getJoin() == null) {
              logger.warn("Missing Primary Key Column: '" + tok + "' Request: '"
                  + this.myRequest.getDatasetName() + "' TableReference: '"
                  + this.getRefname() + "' (please set readonly='true')", ex);
            }
          }
        }
      } else { // Wenn kein pk angegeben, dann aus Metadaten aufbauen
        String s = dbTable.getPKs();
        this.pk = s;
      }
    } catch (SQLException e) {
      String msg = "PL [" + getLayerName()
          + " ] createMetaData: Error retrieving metadata: " + e.getMessage();
      logger.error(msg, e);
      throw new PLException(msg, e);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
      } catch (SQLException e) {
        logger.warn("PL [" + getLayerName()
            + " ] createMetaData: Error closing result set!");
      }
    }
  }
  
  /**
   * Wandelt eine Zeile des ResultSet in eine ArrayList von JDataColumns um
   * 
   * @param meta
   * @return ArrayList
   * @see de.pkjs.dataset.JDataColumn
   */
  private ArrayList<JDataColumn> createColumns(ResultSetMetaData meta)
      throws SQLException {
    ArrayList<JDataColumn> al = new ArrayList<JDataColumn>(meta.getColumnCount());
    for (int i = 1; i <= meta.getColumnCount(); i++) {
      String colName = meta.getColumnName(i);
      // Neu 18.3.2011 PKÖ: Alias from MetaData + Postgres-Bug
      // 18.3.2014 PKÖ: Kein Alias-Bug mehr in Postgres!?
//      if (myRequest.getDatabase().getDatabaseType() == Database.POSTGRES) {            
//        String colName_pg = ((org.postgresql.PGResultSetMetaData)meta).getBaseColumnName(i);
//      }
      final String alias = meta.getColumnLabel(i); 
      int type = meta.getColumnType(i);
      boolean nullable = true;
      boolean readonly = false;
      if (meta.isNullable(i) == ResultSetMetaData.columnNoNulls) {
        nullable = false;
      }
      // default-Value; readOnly aus dbTable
      JDataColumn col = null;
      try {
        col = new JDataColumn(myDataTable, colName, type, nullable, readonly);
        if (alias != null && alias.length() > 0 && !colName.equalsIgnoreCase(alias)) {
          col.setAlias(alias);
        }
        // Default-Value aus Datenbank
        JDataColumn dbCol = this.dbTable.getDataColumn(colName);
        col.setDefaultValue(dbCol.getDefaultValue());
        col.setDecimalDigits(dbCol.getDecimalDigits());
        col.setSize(dbCol.getSize());
        // Primary Key aus dbTable übernehmen!
        col.setPrimaryKey(dbCol.isPrimaryKey()); // New: 27.3.2005 PKÖ
        col.setKeySeq(dbCol.getKeySeq()); // New 23.4.2005 PKÖ
      } catch (Exception ex) {
        // nix gefunden: Ausdruck wie 'Menge * Preis AS Wert'
        readonly = true;
        col = new JDataColumn(myDataTable, colName, type, nullable, readonly);
      }
      al.add(col);
    }
    return al;
  }

  // Für transiente Requests werden die Columns aus
  // dem XML-Dokument der Zugriffsdefinition aufgebaut.
  private void createColumns(Element ele) {
    Elements eles = ele.getElements("Column");
    while (eles.hasMoreElements()) {
      Element colEle = eles.next();
      JDataColumn newCol = new JDataColumn(this.myDataTable, colEle);
      myDataTable.addColumn(newCol);
    }
    this.createPKColumns();
  }

  // Für transiente Requests werden die Columns aus
  // den ChildRows aufgebaut.
  private void createColumns(JDataRow parentRow) {
    Iterator<JDataRow> it = parentRow.getChildRows("TableColumn");
    if (it != null) {
      while (it.hasNext()) {
        JDataRow columnRow = it.next();
        JDataColumn newCol = new JDataColumn(this.myDataTable, columnRow);
        myDataTable.addColumn(newCol);
      }
    }
    this.createPKColumns();
  }

  // Für transiente Requests werden die PK-Columns aus den
  // Angaben der Request-Definition pk='id' aufgebaut.
  private void createPKColumns() {
    // PKs aus Request-Definition
    if (isEmpty(this.pk) == false) {
      StringTokenizer toks = new StringTokenizer(this.pk, ",");
      int keySeq = 0;
      while (toks.hasMoreElements()) {
        String tok = toks.nextToken();
        try {
          JDataColumn col = myDataTable.getDataColumn(tok);
          col.setPrimaryKey(true);
          keySeq++; // 1-relativ
          col.setKeySeq(keySeq);
        } catch (Exception ex) {
          String msg = "Missing primary key column: " + this.myRequest.getDatasetName() + "/" + this.getTablename() + "/"+ tok;
          logger.error(msg);
        }
      }
    }
  }

  /**
   * Erzeugt die JOIN-Bedingungen aus primary und Foreign Key Bei Child FK
   * übergeben ... AND fk_oid = ? --> parent.pkValue Bei Parent PK übergeben ...
   * AND oid = ? --> parent.fkValue Versorgt gleichzeitig die JoinConditions für
   * die spätere Zuweisung der Werte (pkValue bzw. fkValue)
   * TODO: Wenn diese Klasse Thread-save werden soll,
   * dann darf diese Method nicht bei jedem Statement aufgerufen werden.
   * 
   * @param key
   *          Wenn mehrere Felder, dann mit Komma getrennt.
   * @param stmt
   *          Das Statement, welches mit der Join-Condition versorgt werden
   *          soll.
   */
  private void createJoin(String key, SelectStatement stmt) throws PLException {
    // TODO : Das muß anders werden! Join aus myDataTable aufbauen (pk, fk)
    // Delete-Statement OK!
    // Key mit mehreren Feldern; Komma getrennt.
    //##this.joinConditions = null; // Reset JoinConditions
    StringTokenizer toks = new StringTokenizer(key, ",");
    int anzTokens = toks.countTokens();
    switch (anzTokens) {
    case 0: {
      // Error
      String msg = "createJoin: Missing Key(s) " + this.getRefname();
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }
    case 1: {
      String colName = key;
      JDataColumn dc = this.dbTable.getDataColumn(colName);
      if (dc == null) {
        String msg = "Missing Key-Column '" + colName + "' in DataTable: "
            + dbTable.getTablename();
        logger.error(msg);
        throw new IllegalStateException(msg);
      }
      int pktype = dc.getDataType();
      SqlCondition cond = new SqlCondition(colName, pktype, true);
      stmt.addCondition(cond); // Das ist OK!
      //##this.addJoinCondition(cond); // TODO: Das ist nicht OK! (Deshalb ist diese Klasse nicht Thread-save)
    }
      break;
    default: { // > 1
      while (toks.hasMoreTokens()) {
        final String tok = toks.nextToken();
        String colName = tok;
        if (colName.startsWith(" ")) {
          colName = colName.trim();
        }
        JDataColumn dc = this.dbTable.getDataColumn(colName);
        if (dc == null) {
          String msg = "Missing Key-Column '" + colName + "' in DataTable: "
              + dbTable.getTablename();
          logger.error(msg);
          throw new IllegalStateException(msg);
        }
        int pktype = dc.getDataType();
        SqlCondition cond = new SqlCondition(colName, pktype, true);
        stmt.addCondition(cond);
        //##this.addJoinCondition(cond); // TODO: Das ist nicht OK! (Deshalb ist diese Klasse nicht Thread-save)
      }
    }
      break;
    } // End Switch
  }

  /**
   * Arbeitet die (Parent-)Tables ab, um ggf. JOIN-Conditions hinzuzufügen: <br>
   * (SELECT ...) INNER JOIN [TableName] ON (...)
   * 
   * @param stmt
   */
  private void perfParentTables(SelectStatement stmt) {
    // ParentTables
    {
      Iterator<TableRequest> it = this.getParentRequests();
      if (it != null) {
        while (it.hasNext()) {
          TableRequest tr = it.next();
          JDataTable parentTable = tr.getDataTable();
          if (parentTable.getJoin() != null) {
            stmt.addJoinRequest(tr);
          }
        }
      }
    }
    // Child Tables
    {
      Iterator<TableRequest> it = this.getChildRequests();
      if (it != null) {
        while (it.hasNext()) {
          TableRequest tr = it.next();
          JDataTable childTable = tr.getDataTable();
          if (childTable.getJoin() != null) {
            stmt.addJoinRequest(tr);
          }
        }
      }
    }
  }

  /*
   * Wird nur von getDeleteStatement verwendet. Funzt bei Access nicht, da keine
   * Information über PK! PK aus Request?
   */
  private void createPKJoin(AbstractStatement stmt) {
    int cnt = 0;
    for (Iterator<JDataColumn> i = dbTable.getDataColumns(); i.hasNext();) {
      JDataColumn col = i.next();
      if (col.isPrimaryKey()) {
        SqlCondition cond = new SqlCondition(col.getColumnName(), col.getDataType(), false);
        stmt.addCondition(cond);
        cnt++;
      }
    }
    // Wenn in dbTable keine Primary Key definiert,
    // dann aus PK??
    if (cnt == 0 && this.pk != null) {
      StringTokenizer toks = new StringTokenizer(this.pk, ",");
      int keySeq = 0;
      while (toks.hasMoreTokens()) {
        String tok = toks.nextToken();
        if (tok.startsWith(" ")) {
          tok = tok.trim();
        }
        JDataColumn col = dbTable.getDataColumn(tok);
        col.setPrimaryKey(true);
        keySeq++; // 1-relativ
        col.setKeySeq(keySeq);
        SqlCondition cond = new SqlCondition(col.getColumnName(), col.getDataType(), false);
        stmt.addCondition(cond);
      }
    }
  }

  /**
   * Wird nur von UpdateRow verwendet. Wenn ein Feld, welches Bestandteil des
   * Primary Keys ist geändert wird, muß hier der alte, aus der DB eingelesene
   * Wert für die WHERE-Bedingung verwendet werden.
   * 
   * @param row
   * @param stmt
   */
  private void createPKWhere(JDataRow row, AbstractStatement stmt) {
    Iterator<JDataValue> i = row.getDataValues();
    while (i.hasNext()) {
      JDataValue val = i.next();
      if (val.getColumn().isPrimaryKey()) {
        String colName = val.getColumnName();
        String colValue = null;
        if (val.isModified() == true) {
          // Das ist gefährlich, wenn hier NULL drin steht!
          colValue = val.getOldValue();
        } else {
          colValue = val.getValue();
        }
        int pktype = val.getColumn().getDataType();
        SqlCondition cond = new SqlCondition(colName, colValue, pktype);
        stmt.addCondition(cond);
      }
    }
  }

//  private void addJoinCondition(SqlCondition cond) {
//    if (joinConditions == null) {
//      joinConditions = new ArrayList<SqlCondition>();
//    }
//    joinConditions.add(cond);
//  }

  private SimpleDateFormat getTimestampFormat() {
    return this.myRequest.getTimestampFormat();
  }

  private String getTimestamp() {
    String ret = this.getTimestampFormat().format(new Date());
    return ret;
  }

  void addChildRequest(TableRequest request) {
    if (childRequests == null) {
      childRequests = new ArrayList<TableRequest>();
    }
    childRequests.add(request);
  }

  void addParentRequest(TableRequest request) {
    if (parentRequests == null) {
      parentRequests = new ArrayList<TableRequest>();
    }
    parentRequests.add(request);
    // Join
    // JDataTable parentTable = request.getDataTable();
    /*
     * if (parentTable.isJoin()) { Iterator it = request.getColumns(); if (it !=
     * null) { while (it.hasNext()) { JDataColumn col = (JDataColumn)it.next();
     * this.addColumn(col); } } }
     */
  }

  public Iterator<TableRequest> getChildRequests() {
    if (childRequests == null) {
      return null;
    } else {
      return childRequests.iterator();
    }
  }

  /**
   * @param child
   *          refname
   * @return
   */
  private TableRequest getChildRequest(String _refname) {
    if (childRequests != null) {
      for (TableRequest req : childRequests) {
        if (req.getRefname().equalsIgnoreCase(_refname)) {
          return req;
        }
      }
    }
    return null;
  }

  /**
   * @param Parent
   *          refname
   * @return
   */
  private TableRequest getParentRequest(String _refname) {
    if (parentRequests != null) {
      for (TableRequest req : parentRequests) {
        if (req.getRefname().equalsIgnoreCase(_refname)) {
          return req;
        }
      }
    }
    return null;
  }
  
  void getAllChildDataTableNames(ArrayList<String> al) {
    if (this.myDataTable != null) {
      String tname = myDataTable.getTablename().toLowerCase(); 
      if (!al.contains(tname)) {
        al.add(tname);
      }
    }
    if (childRequests != null) {
      for (TableRequest req : childRequests) {
        req.getAllChildDataTableNames(al);      
      }
    }
  }

  private DeleteStatement getDeleteStatement(DatabaseConnection dbConnection, JDataRow row) {
    // Delete-Statement nicht mehr cachen! 28.7.2004 / PKÖ
    DeleteStatement deleteStatement = new DeleteStatement(this, this.getTablename());
    deleteStatement.setMaxExecutionTime(this.maxExecutionTime);
    String optimisticField = dbConnection.getOptimisticField();
    if (optimisticField != null) {
      try {
        JDataColumn optiCol = myDataTable.getDataColumn(optimisticField);
        if (optiCol.isReadonly() == false) { // weglassen wegen join="true"!
          // 14.12.2004 PKÖ
          JDataValue optiValue = row.getDataValue(optimisticField);
          if (optiValue != null) {
            String oldOptiValue = optiValue.getValue();
            SqlCondition cond = new SqlCondition(optimisticField, oldOptiValue, Types.INTEGER); // ##
            deleteStatement.addCondition(cond); // ##
            deleteStatement.setOptimistic(true);
          } else {
            // TODO : Wenn nicht im DataSet enthalten, dann hier anhängen?
          }
        }
      } catch (Exception ex) {
        // Nix machen, wenn diese Tabelle kein Feld für optimistic Locking hat
      }
    } // End Optimistic

    this.createPKJoin(deleteStatement);
    return deleteStatement;
  }
  
//  /**
//   * @deprecated unused; see addBatch
//   * @param row
//   * @return
//   * @throws PLException
//   */
//  private InsertStatement getInsertStatement(JDataRow row) throws PLException {
//    InsertStatement stmt = new InsertStatement(this, this.getTablename());
//    // SET ...
//    int colCount = stmt.setValues(row, false);
//    if (colCount == 0) { // Keine Columns geändert!
//      return null;
//    }
//    return stmt;
//  }

  public Iterator<TableRequest> getParentRequests() {
    if (parentRequests == null) {
      return null;
    } else {
      return parentRequests.iterator();
    }
  }

  private UpdateStatement getUpdateStatement(DatabaseConnection dbConnection,
      JDataSet dataset, JDataRow row) {
    UpdateStatement updateStatement = new UpdateStatement(this, this.getTablename(), this.isDebug());
    updateStatement.setMaxExecutionTime(this.maxExecutionTime);
    // Optimistic
    // TODO - Geht nur gut, wenn in der Datenbank der default auf 0 steht
    String optimisticField = dbConnection.getOptimisticField();
    if (optimisticField != null) {
      try {
        JDataColumn optiCol = myDataTable.getDataColumn(optimisticField);
        if (optiCol.isReadonly() == false) { // weglassen wegen join="true"!
          // 14.12.2004 PKÖ
          JDataValue optiValue = row.getDataValue(optimisticField);
          if (optiValue != null) {
            String oldOptiValue = optiValue.getValue();
            long l = Convert.toLong(oldOptiValue);
            l++;
            String newOptiValue = Long.toString(l);
            optiValue.setValue(newOptiValue);
            optiValue.setModified(true);
            SqlCondition cond = new SqlCondition(optimisticField, // ##
                oldOptiValue, Types.INTEGER); // ##
            updateStatement.addCondition(cond); // ##
            updateStatement.setOptimistic(true);
          } else {
            // TODO : Wenn nicht im DataSet enthalten, dann hier anhängen?
          }
        }
      } catch (Exception ex) {
        // Nix machen, wenn diese Tabelle kein Feld für optimistic Locking hat
      }
    } // End Optimistic
    // UpdateUser
    String updateUserField = dbConnection.getUpdateUserField();
    if (updateUserField != null) {
      try {
        JDataColumn updUserCol = myDataTable.getDataColumn(updateUserField);
        if (updUserCol.isReadonly() == false) { // weglassen wegen join="true"!
          // 14.12.2004 PKÖ
          JDataValue updateVal = row.getDataValue(updateUserField);
          String fld = this.getTimestamp() + " " + dataset.getUsername();
          // Sicherstellen, daß der Wert in das Feld paßt.
          int size = updateVal.getColumn().getSize();
          if (fld.length() > size) {
            fld = fld.substring(0, size);
          }
          updateVal.setValue(fld);
          updateVal.setModified(true);
        }
      } catch (Exception ex) {
      }
    }
    // SET ...
    int colCount = updateStatement.setValues(row, true);
    if (colCount == 0) { // Keine Columns geändert!
      return null;
    }
    // WHERE (nur PK)
    this.createPKWhere(row, updateStatement);

    return updateStatement;
  }

  /**
   * Liefert die Feldnamen der Primary Keys für diese Tabelle; wenn mehrere
   * Felder, dann mit Komma getrennt.
   * 
   * @return
   */
  public String getPK() {
    return pk;
  }

  /**
   * Setzt den oder die Feldnamen, die den Primary Key bilden sollen; wenn
   * mehrere Spalten, dann mit Komma getrennt.
   * 
   * @param s
   */
  public void setPK(String s) {
    this.pk = s;
  }

  /**
   * Liefert den/die Feldnamen der/des Foreign Key(s) für diese Tabelle; wenn
   * mehrere Felder, dann mit Komma getrennt.
   * 
   * @return
   */

  public String getFK() {
    return fk;
  }

  /**
   * Liefert die Feldnamen, nach denen diese Tabelle sortiert werden soll; wenn
   * mehrere Felder, dann mit Komma getrennt. Entspricht also genau der
   * SQL-Syntax von ORDER BY.
   * 
   * @return
   */

  public String getOrderBy() {
    return orderBy;
  }

  /**
   * @see #getOrderBy()
   * @return
   */
  public String getGroupBy() {
    return this.groupBy;
  }

  /**
   * @see #setWhere(String)
   * @return
   */
  public String getWhere() {
    return this.where;
  }

  /**
   * Setzt die WHERE Bedingung für diesen Request neu.
   * <p>
   * Es können so benannte Parameter gesetzt werden: <br>
   * #setWhere("name = $name");
   * 
   * @param where
   */
  public void setWhere(String where) {
    this.where = where;
  }

  /**
   * Je nach Art dieses TableRequest wird ROOT, CHILD, oder PARENT geliefert.
   * 
   * @return
   */

  public int getRequestType() {
    return requestType;
  }

  // Neu
  public JDataTable getDataTable() {
    return myDataTable;
  }

  public String getTablename() {
    return myDataTable.getTablename();
  }

  /**
   * Name der Child- oder Parent Reference.
   * 
   * @return
   */
  public String getRefname() {
    if (refname == null) {
      return this.getTablename();
    } else {
      return this.refname;
    }
  }

  /**
   * Liefert den Zugriffsnamen oder den Tabellennamen.
   * 
   * @return
   */
  public String getViewname() {
    if (viewname != null) {
      return this.viewname;
    } else {
      return this.getTablename();
    }
  }

  public boolean isDebug() {
    return myRequest.isDebug();
  }

  public boolean hasParentRequests() {
    if (this.parentRequests == null) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Liefert die DataTable mit den Feldnamen dieser Klasse.
   * 
   * @return
   */
  @SuppressWarnings("unused")
  public static JDataTable getMetaDataTable() {
    if (metaDataTable == null) {
      metaDataTable = new JDataTable("TableRequest");
      metaDataTable.setDatabaseName(JDataTable.META_DATABASE_NAME);
      metaDataTable.setTransient(true);
      JDataColumn cDB = metaDataTable.addColumn("DatabaseName", Types.VARCHAR);
      cDB.setNullable(false);
      JDataColumn cName = metaDataTable.addColumn("TableName", Types.VARCHAR);
      cName.setNullable(false);
      JDataColumn cRef = metaDataTable.addColumn("RefName", Types.VARCHAR);
      JDataColumn cView = metaDataTable.addColumn("ViewName", Types.VARCHAR);
      JDataColumn cDist = metaDataTable.addColumn("x_Distinct", Types.BOOLEAN);
      cDist.setDefaultValue("0");
      JDataColumn cPK = metaDataTable.addColumn("PK", Types.VARCHAR);
      JDataColumn cFK = metaDataTable.addColumn("FK", Types.VARCHAR);
      JDataColumn cSeq = metaDataTable.addColumn("SEQ", Types.VARCHAR);
      JDataColumn cSelf = metaDataTable.addColumn("selfReference", Types.TINYINT);
      cSelf.setDefaultValue("0");
      JDataColumn cWhere = metaDataTable.addColumn("x_Where", Types.VARCHAR);
      JDataColumn cOrderBy = metaDataTable.addColumn("x_OrderBy", Types.VARCHAR);
      JDataColumn cGroupBy = metaDataTable.addColumn("x_GroupBy", Types.VARCHAR);
      JDataColumn cJoin = metaDataTable.addColumn("x_Join", Types.VARCHAR);
      JDataColumn cRO = metaDataTable.addColumn("readonly", Types.BOOLEAN);
      cRO.setDefaultValue("0");
      JDataColumn cTr = metaDataTable.addColumn("transient", Types.BOOLEAN);
      cTr.setDefaultValue("0");
      JDataColumn cVC = metaDataTable.addColumn("virtual", Types.VARCHAR);
      JDataColumn cSupp = metaDataTable.addColumn("Suppress", Types.BOOLEAN);
      cSupp.setDefaultValue("0");
      JDataColumn cFetch = metaDataTable.addColumn("FetchSize", Types.INTEGER);
      cFetch.setDefaultValue("-1");
      JDataColumn cMaxField = metaDataTable.addColumn("MaxFieldSize", Types.INTEGER);
      cMaxField.setDefaultValue("-1");
      JDataColumn cMaxRow = metaDataTable.addColumn("MaxRows", Types.INTEGER);
      cMaxRow.setDefaultValue("-1");
      JDataColumn cQT = metaDataTable.addColumn("queryTimeout", Types.INTEGER);
      cQT.setDefaultValue("-1");
      JDataColumn cMex = metaDataTable.addColumn("maxExecutionTime", Types.INTEGER);
      cMex.setDefaultValue("-1");
      JDataColumn cDataSet = metaDataTable.addColumn("DatasetName", Types.VARCHAR);
      JDataColumn cType = metaDataTable.addColumn("RequestType", Types.INTEGER);
      cType.setNullable(false);
      JDataColumn cAlias = metaDataTable.addColumn("Alias", Types.VARCHAR);
      JDataColumn cReqId = metaDataTable.addColumn("TableRequestId", Types.INTEGER);
      cReqId.setPrimaryKey(true);
      cReqId.setKeySeq(1);
      cReqId.setNullable(false);
      JDataColumn cUp = metaDataTable.addColumn("x_OnUpdate", Types.INTEGER);
      JDataColumn cDel = metaDataTable.addColumn("x_OnDelete", Types.INTEGER);
      JDataColumn cParentReqId = metaDataTable.addColumn("FK_TableRequestId",
          Types.INTEGER);
      // Columns
      JDataTable tblCol = new JDataTable("TableColumn");
      {
        tblCol.setDatabaseName(JDataTable.META_DATABASE_NAME);
        JDataColumn cColName = tblCol.addColumn("ColumnName", Types.VARCHAR);
        cColName.setNullable(false);
        JDataColumn cColAlias = tblCol.addColumn("Alias", Types.VARCHAR);
        JDataColumn cColNN = tblCol.addColumn("ReadOnly", Types.BOOLEAN);
        cColNN.setDefaultValue("0");
        JDataColumn cFkReqId = tblCol.addColumn("FK_TableRequestId", Types.INTEGER);
        cFkReqId.setForeignKey(true);
        JDataColumn cColId = tblCol.addColumn("ColumnId", Types.INTEGER);
        cColId.setPrimaryKey(true);
        cColId.setNullable(false);
      }
      metaDataTable.addChildTable(tblCol, "FK_TableRequestId");
    }
    return metaDataTable;
  }

  /**
   * Liefert eine DataRow mit den Attributen dieser Klasse.
   * 
   * @return
   */
  public JDataRow getMetaDataRow() {
    JDataRow row = getMetaDataTable().createNewRow();
    row.setValue("DatabaseName", myDataTable.getDatabaseName());
    row.setValue("TableName", this.getTablename());
    row.setValue("RequestType", this.requestType);
    row.setValue("RefName", this.getRefname());
    row.setValue("ViewName", this.getViewname());
    if (this.getTablename().equals(this.myDataTable.getAlias()) == false) {
      row.setValue("Alias", this.myDataTable.getAlias());
    }
    row.setValue("PK", this.getPK());
    row.setValue("FK", this.getFK());
    row.setValue("selfReference", this.myDataTable.isSelfReference());
    row.setValue("x_Where", this.getWhere());
    row.setValue("x_Orderby", this.getOrderBy());
    row.setValue("x_GroupBy", this.getGroupBy());
    row.setValue("x_Join", this.myDataTable.getJoin());
    row.setValue("readonly", this.isReadonly());
    row.setValue("transient", this.isTransient());
    row.setValue("virtual", this.myDataTable.getVirtualChild());
    row.setValue("FetchSize", this.fetchSize);
    row.setValue("MaxFieldSize", this.maxFieldSize);
    row.setValue("MaxRows", this.maxRows);
    row.setValue("QueryTimeout", this.queryTimeout);
    row.setValue("maxExecutionTime", this.maxExecutionTime);
    row.setValue("DatasetName", this.myRequest.getDatasetName());
    row.setValue("x_OnUpdate", this.getUpdateRule());
    row.setValue("x_OnDelete", this.getDeleteRule());
    // Columns
    JDataTable colTbl = metaDataTable.getChildTable("TableColumn");
    for (Iterator<JDataColumn> i = columns.iterator(); i.hasNext();) {
      JDataColumn col = i.next();
      JDataRow rowCol = colTbl.createNewRow();
      rowCol.setValue("ColumnName", col.getColumnName());
      rowCol.setValue("Alias", col.getAlias());
      rowCol.setValue("ReadOnly", !col.isNullable());

      row.addChildRow(rowCol);
    }
    // row.commitChanges();
    return row;
  }

  private boolean isEmpty(String s) {
    if (s == null || s.length() == 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * @return
   */
  public boolean isDistinct() {
    return distinct;
  }

  /**
   * @param b
   */
  public void setDistinct(boolean b) {
    distinct = b;
  }

  /**
   * @see JDataTable#isReadonly()
   * @return Returns the readonly.
   */
  public boolean isReadonly() {
    return myDataTable.isReadonly();
  }

  /**
   * @see Request#isTransient()
   * @see JDataTable#isTransient()
   * @return
   */
  public boolean isTransient() {
    if (this.myRequest.isTransient()) {
      return true;
    } else {
      return myDataTable.isTransient();
    }
  }

  public boolean isVirtualChild() {
    return myDataTable.isVirtualChild();
  }

  public String getVirtualChild() {
    return myDataTable.getVirtualChild();
  }

  /**
   * @param readonly
   *          The readonly to set.
   */
  void setReadonly(boolean readonly) {
    this.myDataTable.setReadonly(readonly);
  }

  public Request getRequest() {
    return this.myRequest;
  }

  public String getLayerName() {
    return getRequest().getDatabase().getLayerName();
  }
  
  int createChangeProtocol(PLTransactionContext pl, DatabaseConnection con, JDataSet ds) throws PLException {
    Iterator<JDataRow> rows = ds.getChildRows();
    if (rows == null) return 0;
    InsertStatement stmt = null;
    String cp = this.getDataTable().getChangeProtocol();
    if (cp != null) {
      JDataSet dsProt = pl.getEmptyDataset(cp);
      int anz = 0;
      // 1. My Rows
      while (rows.hasNext()) {
        JDataRow row = rows.next();
        if (row.isModifiedChild()) {
          anz++;
          if (anz == 1) {
            stmt = new InsertStatement(this, dsProt.getDataTable().getTablename() ); 
          } 
          JDataRow rowProt = dsProt.getDataTable().createNewRow();
          dsProt.addChildRow(rowProt);
          this.fillProtocol(pl, ds, row, rowProt);
          stmt.addBatch(con, rowProt, false);
        }      
      }
    }
    int cnt = 0;
    if (stmt != null) {
      cnt = stmt.executeBatch();
    }
    // 2. Childs
    if (this.childRequests != null) {
      for(TableRequest req:this.childRequests) {
        cnt += req.createChangeProtocol(pl, con, ds, ds.getChildRows());
      }
    }    
    return cnt;
  }
  
  int createChangeProtocol(PLTransactionContext pl, DatabaseConnection con, JDataSet ds, Iterator<JDataRow> parentRows) throws PLException {
    if (parentRows == null) return 0;
    String cp = this.getDataTable().getChangeProtocol();
    if (cp != null) {
      JDataSet dsProt = pl.getEmptyDataset(cp);

      InsertStatement stmt = null;
      ArrayList<JDataRow> alParent = new ArrayList<JDataRow>();
      int anz = 0;
      while (parentRows.hasNext()) {
        JDataRow row = parentRows.next();
        Iterator<JDataRow> childRows = row.getChildRows(this.getRefname());
        if (childRows != null) {
          while(childRows.hasNext()) {
            JDataRow childRow = childRows.next();
            alParent.add(childRow);
            if (childRow.isModifiedChild()) {
              anz++;
              if (anz == 1) {
                stmt = new InsertStatement(this, dsProt.getDataTable().getTablename()); 
              }            
              JDataRow rowProt = dsProt.getDataTable().createNewRow();
              this.fillProtocol(pl, ds, row, rowProt);
              dsProt.addChildRow(rowProt);
              stmt.addBatch(con, rowProt, false);
            }      
          }
        }
      }
      int cnt = 0;
      if (stmt != null) {
        cnt = stmt.executeBatch();
      }
      // Childs
      if (this.childRequests != null) {
        for(TableRequest req:this.childRequests) {
          cnt += req.createChangeProtocol(pl, con, ds, alParent.iterator());
        }
      }
      return cnt;
    } else {
      return 0;
    }
  }
  
  private void fillProtocol(PLTransactionContext pl, JDataSet ds, JDataRow row, JDataRow rowProt) throws PLException {
    rowProt.setValue("id", pl.getOID());
    rowProt.setValue("datasetname", ds.getDatasetName());
    rowProt.setValue("tablename", row.getDataTable().getTablename().toLowerCase());
    rowProt.setValue("username", ds.getUsername());
    rowProt.setValue("currenttime", new Date());
    ArrayList<JDataValue> pks = row.getPrimaryKeyValues();          
    rowProt.setValue("oid", pks.get(0).getValue());
    rowProt.setValue("dataset", row.getChangesPersistent().toString());    
    rowProt.setValue("actiontype", row.getActionType());
  }
  
  void getTablenames(Set<String> al) {
     String tname = this.getTablename();
     if (tname != null) {
        al.add(tname.toLowerCase());        
     }
     if (this.childRequests != null) {
        for(TableRequest req:this.childRequests) {
           req.getTablenames(al);
        }
     }
     if (this.parentRequests != null) {
        for(TableRequest req:this.parentRequests) {
           req.getTablenames(al);
        }
     }
  }
  
  public String toString() {
    return element == null ? super.toString() : element.toString();
  }

}