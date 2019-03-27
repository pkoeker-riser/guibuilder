/*
 * TODO : Ein Request muß mit seinen ChildRequests 
 * nach XML serialisierbar sein!
 * Genau genommen der gesamte PL mit all seinen Requests!
 * Dann haben wir mit DataSet nochmal eine ähnliche Struktur!
 */
package de.pkjs.pl;

import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import de.jdataset.JDataColumn;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.jdataset.ParameterList;
import de.pkjs.pl.Database.NamedStatement;
import de.pkjs.util.Convert;
import electric.xml.Document;
import electric.xml.Element;

/**
 * Diese Klasse repräsentiert einen Datenbankzugriff.
 * <p>
 * Diese Zugriffe werden vom Persistenz-Layer wiederverwendet. <br>
 * Ein Request wird im Constructor aus einer Request-Definition der Datei
 * DatabaseConfig.xml aufgebaut.
 * <p>
 * Ein Request hat (vorerst) nur <i>einen </i> RootTableRequest. <br>
 * Der erzeugte Request liefert über getRequest einen Dataset aus der Datenbank.
 * Mit setDataset wird er in die Datenbank zurückgeschrieben.
 * <p>
 * Class Diagram <br>
 * <img src="ClassDiagramRequest.jpg">
 */
public final class Request {
   private String datasetName;
   private String databaseName;
   private TableRequest rootTableRequest; // TODO: Das müssen auch mehrere sein können
   private static JDataTable metaDataTable;
   private boolean readonly;
   private boolean isTransient;
   private SimpleDateFormat dateFormat;
   private SimpleDateFormat timeFormat;
   private SimpleDateFormat timestampFormat;
   private boolean isDebug;
   private Database database;
   private int maxExecutionTime = Integer.MAX_VALUE;
   private int isolationLevel = -1;
   private RequestCacheConfig cacheConfig;

   RequestCacheConfig getCacheConfig() {
     return cacheConfig;
   }


  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Request.class);

   // Constructors
   /**
    * Erzeugt einen leeren Request.
    */
   Request(Database database, String datasetName) {
      this.database = database;
      this.datasetName = datasetName;
      this.dateFormat = database.getDateFormat();
      this.timeFormat = database.getTimeFormat();
      this.timestampFormat = database.getTimestampFormat();
      this.isDebug = database.isDebug();
   }

   Request(Database database, DatabaseConnection dbConnection, Element ele)
         throws PLException {
      this.database = database;
      // Attributes
      this.datasetName = ele.getAttribute("name");
      logger.debug("New request created: " + datasetName);
      this.readonly = Convert.toBoolean(ele.getAttribute("readonly"));
      this.isTransient = Convert.toBoolean(ele.getAttribute("transient"));
      
      // RootRequest
      // TODO : Das sollte auf beliebig viele erweitert werden
      Element rootEle = ele.getElement("RootTable");
      rootTableRequest = new TableRequest(dbConnection, this, rootEle,
            TableRequest.ROOT_TABLE);
      this.dateFormat = database.getDateFormat();
      this.timeFormat = database.getTimeFormat();
      this.timestampFormat = database.getTimestampFormat();
      this.isDebug = database.isDebug();
      String s = ele.getAttribute("maxExecutionTime");
      if (s != null) {
        this.maxExecutionTime = Convert.toInt(s);
      } else {
         this.maxExecutionTime = database.getDefaultMaxExecutionTime();
      }
      // TransactionIsolationLevel
      Element isoEle = ele.getElement("isolationLevel");
      if (isoEle != null) {
        String sTransactionLevel = isoEle.getAttribute("value");
        this.isolationLevel = Database.getTransactionIsolationLevel(sTransactionLevel);
      }
      // Cache
      Element cele = ele.getElement("Cache");
      if (cele != null) {
        cacheConfig = new RequestCacheConfig(this.getDatasetName(), cele);
      }
   }

   /**
    * Erzeugt einen Request aus einer entsprechend gestalteten DataRow
    * 
    * @see #getMetaDataRow
    * @param pl
    * @param row
    * @throws PLException
    */
   Request(Database database, DatabaseConnection dbConnection, JDataRow row)
         throws PLException {
      this.database = database;
      this.datasetName = row.getValue("DatasetName");
      this.readonly = row.getValueBool("readonly");
      this.isTransient = row.getValueBool("transient");
      this.databaseName = row.getValue("DatabaseName");
      String childTableName = "RootTables";
      int cnt = row.getChildRowCount(childTableName);
      if (cnt == 1) {
         JDataRow rootRow = row.getChildRow(childTableName, 0);
         this.rootTableRequest = new TableRequest(dbConnection, this, rootRow);
      }
      this.dateFormat = database.getDateFormat();
      this.timeFormat = database.getTimeFormat();
      this.timestampFormat = database.getTimestampFormat();
      this.isDebug = database.isDebug();
      this.maxExecutionTime = row.getValueInt("maxExecutionTime");
   }

   // public
   public void setIsDebug(boolean isDebug) {
      this.isDebug = isDebug;
   }

   public boolean isDebug() {
      return isDebug;
   }

//   public TableRequest createRootTableRequest(String tablename, String columns) throws PLException {
//      this.rootTableRequest = new TableRequest(dbConnection, this, tablename, columns,
//            TableRequest.ROOT_TABLE);
//      return rootTableRequest;
//   }
   /**
    * @deprecated
    * Erzeugt einen/den Root Table Request zu diesem Request. Z.Z. gibt es davon
    * nur genau einen.
    * 
    * @param tablename
    *           Name der Tabelle
    * @param columns
    *           Spalten-Name aus der Tabelle mit Komma getrennt.
    * @return
    * @throws PLException
    */
   public TableRequest createRootTableRequest(DatabaseConnection dbConnection,
         String tablename, String columns) throws PLException {
      this.rootTableRequest = new TableRequest(dbConnection, this, tablename, columns,
            TableRequest.ROOT_TABLE);
      return rootTableRequest;
   }

   /**
    * Liefert die Struktur dieses Request als XML-Document.
    */
   public Document getDocument() {
      Document doc = new Document();
      doc.setEncoding("UTF-8");
      doc.setRoot(this.getElement());

      return doc;
   }

   /**
    * Liefert die Struktur dieses Request als XML-Element.
    * 
    * @return ein XML-Element <View name="...
    */
   public Element getElement() {
      Element ele = new Element("View");
      ele.setAttribute("name", this.datasetName);
      if (istLeer(this.databaseName) == false) {
         ele.setAttribute("database", databaseName);
      }
      TableRequest rootTblReq = this.getRootTableRequest();
      if (rootTblReq != null) { // Request ist leer!!
         Element rootEle = rootTblReq.getElement();
         ele.addElement(rootEle);
      }
      if (maxExecutionTime != Integer.MAX_VALUE) {
         ele.setAttribute("maxExecutionTime", Integer.toString(maxExecutionTime));
      }
      return ele;
   }

   JDataSet getDataset(DatabaseConnection dbConnection, String datasetname, long oid)
         throws PLException {
     JDataSet dataset = this.createDataset(datasetname);
     this.fillDataset(dbConnection, dataset, oid);
     return dataset;
   }
   JDataSet getDataset(DatabaseConnection dbConnection, String datasetname, long[] oids)
       throws PLException {
     JDataSet dataset = this.createDataset(datasetname);
     this.fillDataset(dbConnection, dataset, oids);
     
     return dataset;
   }
   JDataSet getDataset(DatabaseConnection dbConnection, String datasetname, String key)
   	throws PLException {
   	JDataSet dataset = this.createDataset(datasetname);
   	this.fillDataset(dbConnection, dataset, key);

   	return dataset;
   }

   JDataSet getDataset(DatabaseConnection dbConnection, String datasetname)
         throws PLException {
      if (this.isTransient) {
         throw new PLException("Transient Request: "+datasetname);
      }
      JDataSet dataset = this.createDataset(datasetname);
      this.fillDataset(dbConnection, dataset);
      return dataset;
   }
   /**
    * @deprecated
    * @param dbConnection
    * @param datasetname
    * @param where
    * @return
    * @throws PLException
    */
   JDataSet getDatasetWhere(DatabaseConnection dbConnection, String datasetname, String where)
         throws PLException {
      if (this.isTransient) {
         throw new PLException("Transient Request: "+datasetname);
      }
      JDataSet dataset = this.createDataset(datasetname);
      this.fillDatasetWhere(dbConnection, dataset, where);
      return dataset;
   }

   JDataSet getDataset(DatabaseConnection dbConnection, String datasetname,
         ParameterList parameters) throws PLException {
      if (this.isTransient) {
         throw new PLException("Transient Request: "+datasetname);
      }
      JDataSet dataset = this.createDataset(datasetname);
      this.fillDataset(dbConnection, dataset, parameters);
      return dataset;
   }

   JDataSet getEmptyDataset(String datasetname) {
      JDataSet dataset = this.createDataset(datasetname);
      return dataset;
   }

   /**
    * Erzeugt einen leeren Dataset unter Angabe seines Namens. Die
    * Schema-Struktur des DataSet (JDataTable und JDataColumn) wird dabei
    * aufgebaut.
    * 
    * @param datasetname
    * @return
    */
   private JDataSet createDataset(String datasetname) {
      JDataSet dataset = new JDataSet(datasetname);
      dataset.setReadonly(this.isReadonly());
      dataset.setDatabaseName(this.getDatabaseName());
      JDataTable jRootTable = this.rootTableRequest.getDataTable(); // TODO: clone?
      dataset.addRootTable(jRootTable);
      return dataset;
   }

   /**
    * Füllt einen leeren DataSet mit Werten
    * 
    * @param dataset
    * @param oid
    */
   private void fillDataset(DatabaseConnection dbConnection, JDataSet dataset, long oid)
         throws PLException {
      long _startTime = System.currentTimeMillis();
      this.rootTableRequest.fillDataset(dbConnection, dataset, oid);
      this.maxExeLogger(_startTime, "getDataset: " + oid);
   }
   private void fillDataset(DatabaseConnection dbConnection, JDataSet dataset, long[] oids)
       throws PLException {
     long _startTime = System.currentTimeMillis();
     this.rootTableRequest.fillDataset(dbConnection, dataset, oids);
     this.maxExeLogger(_startTime, "getDataset: " + Arrays.toString(oids));
   }
   private void fillDataset(DatabaseConnection dbConnection, JDataSet dataset, String key)
   	    throws PLException {
      long _startTime = System.currentTimeMillis();
   	this.rootTableRequest.fillDataset(dbConnection, dataset, key);
      this.maxExeLogger(_startTime, "getDataset: " + key);
   }

   /**
    * @deprecated
    * Füllt einen leeren DataSet mit Werten
    * @param dbConnection 
    * @param dataset
    * @param where Bedingung
    */
   private void fillDatasetWhere(DatabaseConnection dbConnection, JDataSet dataset,
         String where) throws PLException {
      long _startTime = System.currentTimeMillis();
      SqlCondition cond = new SqlCondition(where);
      this.rootTableRequest.fillDataset(dbConnection, dataset, cond);
      this.maxExeLogger(_startTime, "getDataset: " + where);
   }

   /**
    * Füllt einen leeren DataSet mit Werten
    * @param dbConnection
    * @param dataset
    */
   private void fillDataset(DatabaseConnection dbConnection, JDataSet dataset)
         throws PLException {
      long _startTime = System.currentTimeMillis();
      this.rootTableRequest.fillDataset(dbConnection, dataset);
      this.maxExeLogger(_startTime, "getDatasetAll");
   }

   private void fillDataset(DatabaseConnection dbConnection, JDataSet dataset,
         ParameterList parameters) throws PLException {
      long _startTime = System.currentTimeMillis();
      this.rootTableRequest.fillDataset(dbConnection, dataset, parameters);
      this.maxExeLogger(_startTime, "getDataset");
   }

   /**
    * Schreibt einen DataSet in die Datenbank
    * @param dbConnection
    * @param dataset
    * @return
    */
   int setDataset(DatabaseConnection dbConnection, JDataSet dataset) throws PLException {
      long _startTime = System.currentTimeMillis();
      { // 1. Prepare PK, FK
        int anz = 0;
        Iterator<JDataRow> rows = dataset.getChildRows();
        if (rows == null) {
          return 0; // leerer DS
        }
        while (rows.hasNext()) {
          JDataRow row = rows.next();
          int tmp = this.rootTableRequest.prepareSetDataset(dbConnection, dataset, row);
          anz += tmp;
        }
      }
      // 2. Insert mit addBatch
      int cnt = this.rootTableRequest.insertBatchRows(dbConnection, dataset);
      { // 3. Execute Update
        Iterator<JDataRow> rows = dataset.getChildRows();
        if (rows == null) {
          return cnt;
        }
        while (rows.hasNext()) {
          JDataRow row = rows.next();
          int tmp = this.rootTableRequest.setDataset(dbConnection, dataset, row);
          cnt += tmp;
        }
      }
      // 3. Cache löschen bei jedem Insert, Update, Delete
      if (this.hasCache() && cacheConfig.remove != null) {
          StringTokenizer toks = new StringTokenizer(cacheConfig.remove, ",;");
          while(toks.hasMoreTokens()) {
            String tok = toks.nextToken();
            RequestCacheConfig rcacheConfig = null;
            if (tok.startsWith("NamedStatement:")) {
              NamedStatement nst = database.getStatement(tok.substring(15));
              if (nst != null) {
                rcacheConfig = nst.getCacheConfig();
              }
            } else {
              Request rreq = database.getRequest(tok);
              if (rreq != null) {
                rcacheConfig = rreq.getCacheConfig();
              }
            }
            if (rcacheConfig != null) {
               rcacheConfig.removeAll();
            }
        }
      }                
      this.maxExeLogger(_startTime, "setDataset");
      return cnt;
   }
   
   private void maxExeLogger(long _startTime, String label) {
      long _dura = System.currentTimeMillis() - _startTime;
      if (_dura > maxExecutionTime) {
        Logger slowLog = getDatabase().getSlowQueryLogger();
        if (slowLog != null) {
          String msg = "MaxExecutionTime exceeded: " + _dura + ">" + maxExecutionTime + " Dataset: " + getDatasetName() + " " + label;
          slowLog.warn(msg);
        }        
      }
   }

   // ########## ALT ####################################
   /**
    * @return Der Name des Dataset, für den dieser Request zuständig ist.
    */
   public String getDatasetName() {
      return datasetName;
   }

   /**
    * @return Den Root Table Request
    */
   public TableRequest getRootTableRequest() {
      return rootTableRequest;
   }

   public String getDatabaseName() {
      return this.databaseName;
   }

   private static boolean istLeer(String s) {
      if (s == null || s.length() == 0) {
         return true;
      } else {
         return false;
      }
   }

   /**
    * Liefert die DataTable mit den Feldnamen dieser Klasse.
    * 
    * @return
    */
   public static JDataTable getMetaDataTable() {
      if (metaDataTable == null) {
         metaDataTable = new JDataTable("MetaRequest");
         metaDataTable.setDatabaseName(JDataTable.META_DATABASE_NAME);
         metaDataTable.setTransient(true);
         // DatasetName
         JDataColumn cName = metaDataTable.addColumn("DatasetName", Types.VARCHAR);
         cName.setNullable(false);
         cName.setPrimaryKey(true);
         cName.setKeySeq(1);
         // Readonly
         @SuppressWarnings("unused")
        JDataColumn cRO = metaDataTable.addColumn("readonly", Types.BOOLEAN);
         // Transient
         @SuppressWarnings("unused")
        JDataColumn cTr = metaDataTable.addColumn("transient", Types.BOOLEAN);
         // DatabaseName
         @SuppressWarnings("unused")
        JDataColumn cDB = metaDataTable.addColumn("DatabaseName", Types.VARCHAR);
         @SuppressWarnings("unused")
        JDataColumn cMax = metaDataTable.addColumn("maxExecutionTime", Types.INTEGER);
         // FK
         JDataColumn cFK = metaDataTable
               .addColumn("DatasetDefinitionFile", Types.VARCHAR);
         cFK.setForeignKey(true);
      }
      return metaDataTable;
   }

   /**
    * Liefert die Metadaten eines Requests als DataRow
    * 
    * @return
    */
   public JDataRow getMetaDataRow() {
      JDataRow row = getMetaDataTable().createNewRow();
      row.setValue("DatasetName", this.datasetName);
      row.setValue("readonly", this.isReadonly());
      row.setValue("transient", this.isTransient());
      row.setValue("DatabaseName", this.databaseName);
      row.setValue("DatasetDefinitionFile", this.getDatabase()
            .getDatasetDefinitionFileName());
      if (maxExecutionTime != Integer.MAX_VALUE)
         row.setValue("maxExecutionTime", maxExecutionTime);
      //row.commitChanges();
      return row;
   }
   
   /**
    * Liefert die Liste aller ChildTabellen, die an diese Definition beteiligt sind
    * ParentTables werden jeweils weggelassen
    * @return
    */
   ArrayList<String> getAllChildDataTableNames() {
     ArrayList<String> al = new ArrayList<String>();
     if (this.getRootTableRequest() != null) {
       this.getRootTableRequest().getAllChildDataTableNames(al);
     }
     return al;
   }

   /**
    * @return Returns the readonly.
    */
   boolean isReadonly() {
      return readonly;
   }

   /**
    * @param readonly
    *           The readonly to set.
    */
   void setReadonly(boolean readonly) {
      this.readonly = readonly;
   }

   /**
    * @return
    */
   public SimpleDateFormat getDateFormat() {
      return dateFormat;
   }

   /**
    * @return
    */
   public SimpleDateFormat getTimeFormat() {
      return timeFormat;
   }

   /**
    * @return
    */
   public SimpleDateFormat getTimestampFormat() {
      return timestampFormat;
   }

   public Database getDatabase() {
      return database;
   }
   /**
    * @return Returns the isTransient.
    */
   public boolean isTransient() {
      return isTransient;
   }
   
   int getIsolationLevel() {
     return isolationLevel;
   }

   void setIsolationLevel(int isolationLevel) {
     this.isolationLevel = isolationLevel;
   }
   
   void setCacheConfig(RequestCacheConfig rcc) {
     this.cacheConfig = rcc;
   }
   
   boolean hasCache() {
     return cacheConfig != null && cacheConfig.isEnabled() == true;
   }
   
  int createChangeProtocol(PLTransactionContext pl, DatabaseConnection con, JDataSet ds) throws PLException {
     if (this.rootTableRequest == null) return 0;
     int cnt = rootTableRequest.createChangeProtocol(pl, con, ds);
     return cnt;
   }
  
  /**
   * @formatter:off
   * Liefert die Menge der hier verwendeten Tabellennamen
   * @return
   * @formatter:on
   */
  void getTablenames(Set<String> al) {
     this.rootTableRequest.getTablenames(al);     
  }
  
  public String toString() {
    String s = this.getElement().toString();
    return s;
  }
}