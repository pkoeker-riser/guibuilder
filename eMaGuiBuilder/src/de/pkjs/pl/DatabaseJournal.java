package de.pkjs.pl;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import de.jdataset.JDataSet;
import de.jdataset.NVPair;
import de.jdataset.ParameterList;
import de.pkjs.util.Convert;
import electric.xml.Document;
import electric.xml.Element;

class DatabaseJournal {
  private Database database;
  private int updateJournaleCounter;
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DatabaseJournal.class);

  DatabaseJournal(Database database) {
    this.database = database;
  }
  
  private synchronized int getUpdateJournalCounter() {
    return ++this.updateJournaleCounter;
  }

  /**
   * Speichert einen DataSet im Journal 
   * @param dataset
   */
  void journalDataset(JDataSet dataset) {
    // Wegen dem Optimistic Locking muß hier wieder auf den OriginalWert von Version
    // zurückgesetzt werden.
    dataset.rollbackVersion(database.getOptimisticField());
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss-SSS");
    String filename = database.getUpdateJournalDirectory() 
      + "/" + dateFormat.format(new java.util.Date())
      + "_" + this.getUpdateJournalCounter() 
      + ".dataset";
    try {              
      Document doc = dataset.getXml();
      File f = new File(filename);
      doc.write(f);
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }
  
  void journalStatement(String sql, ParameterList parameters) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss-SSS");
    String filename = database.getUpdateJournalDirectory() 
      + "/" + dateFormat.format(new java.util.Date())
      + "_" + this.getUpdateJournalCounter() 
      + ".sql";
    try {
      File f = new File(filename);
      FileWriter fw = new FileWriter(f);
      Document doc = new Document();
      doc.setEncoding("UTF8");
      Element eRoot = doc.setRoot("Statement");
      Element eSql = eRoot.addElement("SQL");
      eSql.setText(sql);
      if (parameters != null) {
        Element eParams = eRoot.addElement("Parameter");
        Iterator<NVPair> it = parameters.iterator();
        while (it.hasNext()) {
          NVPair pair = it.next();
          Element ePara = eParams.addElement("Param");
          ePara.setAttribute("name", pair.getName());
          ePara.setAttribute("value", Convert.toString(pair.getValue(), pair.getDataType()));
          ePara.setAttribute("type", "" + pair.getDataType());
        }
      }
      String sDoc = doc.toString();        
      fw.write(sDoc);
      fw.close();
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  void startTransaction(String name) {
    this.execTransaction(PL.START_TRANSACTION, name);
  }
  
  void rollbackTransaction(String name) {
    this.execTransaction(PL.ROLLBACK, name);
  }
  
  void commitTransaction(String name) {
    this.execTransaction(PL.COMMIT, name);
  }
  
  private void execTransaction(String type, String name) {
    if (name.startsWith("**")) return; // interne Transaktionen
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss-SSS");
    String filename = database.getUpdateJournalDirectory() 
    + "/" + dateFormat.format(new java.util.Date())
    + "_" + this.getUpdateJournalCounter() 
    + ".transaction";
    try {
      File f = new File(filename);
      FileWriter fw = new FileWriter(f);
      Document doc = new Document();
      doc.setEncoding("UTF8");
      Element eRoot = doc.setRoot("Transaction");
      eRoot.setText(type);
      eRoot.setAttribute("name", name);
      String sDoc = doc.toString();        
      fw.write(sDoc);
      fw.close();
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }        
  }
}
