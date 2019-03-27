/*
 * Created on 04.02.2004
 */
package de.pkjs.pltest;

import junit.framework.TestCase;

import org.apache.log4j.xml.DOMConfigurator;

import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.pkjs.pl.IPLContext;
import de.pkjs.pl.PL;
import de.pkjs.pl.PLException;

/**
 * @author pkoeker
 */
public class TransactionTest extends TestCase {

    private PL p;
    private static final String TRANS_NAME = "TransactionTest";

    public TransactionTest() {
        DOMConfigurator.configure("Log4JTestConnection.xml");
        p = AllTests.getPL();
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        //p.reset();
    }

    public void test1() {
        try {
            IPLContext context = p.startNewTransaction(TRANS_NAME);
            context.getDataset("AdresseKurz", 1);
            context.commitTransaction(TRANS_NAME);
        } catch (Exception ex) {
      	  ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    public void test2() {
        try {
            IPLContext pl = p.startNewTransaction(TRANS_NAME);
            pl.rollbackTransaction("Falscher-Name");            
            fail("Rollback darf hier nicht funktionieren");
        } catch (Exception ex) {
            // erwarteter Fehler
        }    
    }
    /**
     * Geschachtelte Transaktion - commit
     *
     */
    public void test3() {
        try {
            IPLContext pl = p.startNewTransaction(TRANS_NAME);
            pl.startTransaction("Test1");
            pl.startTransaction("Test2");
            pl.commitTransaction("Test2");
            pl.commitTransaction("Test1");
            pl.commitTransaction(TRANS_NAME);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    /**
     * Geschachtelte Transaktion - rollback
     *
     */
    public void test4() {
        try {
            IPLContext pl = p.startNewTransaction(TRANS_NAME);
            pl.startTransaction("Test");
            pl.rollbackTransaction("Test");
            pl.rollbackTransaction(TRANS_NAME);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    public void test6() {
        try {
            IPLContext pl = p.startNewTransaction(TRANS_NAME);
            pl.startTransaction("Test1");
            pl.startTransaction("Test2");
            try {
                pl.commitTransaction("Test1");
                fail("Erwarterer Fehler nicht eingetreten");
            } catch (Exception ex) {
                // erwarteter Fehler
            }
            pl.rollbackTransaction("Test2");
            pl.rollbackTransaction("Test1");
            pl.rollbackTransaction(TRANS_NAME);
            try {
                pl.startTransaction("Test1");
                fail("Start Transaction auf einem ungültigen Trans. kontext");
            }
            catch(PLException ex) {
                // erwarteter Fehler
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        } 
    }
//    public void test7() { // Abort gibts nicht mehr!
//		try {
//		  IPLContext pl = p.startNewTransaction(TRANS_NAME);
//			pl.abortTransaction();
//			JDataSet ds = pl.getDataset("AdresseKurz", 1);
//			fail("Operation auf einem ungültigen Transactionskontext");
//		} catch (Exception ex) {
//			// erwarteter Fehler
//		}
//    }
    
    public void test8() {
        try {
           //String sql = "SET AUTOCOMMIT FALSE";
           //p.executeSql(sql);
            IPLContext pl = p.startNewTransaction(TRANS_NAME);
            JDataSet ds = pl.getDataset("AdresseKurz", 1);
            JDataRow row = ds.getRow();
            String oldPLZ = row.getValue("PLZ");
            row.setValue("PLZ", "12345");
            pl.setDataset(ds);
            if (pl.testCommit() == false) {
                fail("testCommit (vor rollback) geht schief");
            }
            pl.rollbackTransaction(TRANS_NAME);

            pl = p.startNewTransaction("A");
            JDataSet dsTest = pl.getDataset("AdresseKurz", 1);
            pl.commitTransaction("A");
            JDataRow rowTest = dsTest.getRow();
            String testPLZ = rowTest.getValue("PLZ");
            if (testPLZ.equals(oldPLZ) == false) {
                fail("TransactionTest; Rollback keine Wirkung");
            }          
        } catch (Exception ex) {
           ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
    
    public void test9() {
        // Trans Commit
        try {
            IPLContext pl = p.startNewTransaction(TRANS_NAME);
            JDataSet ds = pl.getDataset("AdresseKurz", 1);
            JDataRow row = ds.getRow();
            String oldPLZ = row.getValue("PLZ");
            row.setValue("PLZ", "12345");
            pl.setDataset(ds);
            pl.commitTransaction(TRANS_NAME);
            
            pl = p.startNewTransaction("test9");
            JDataSet dsTest = pl.getDataset("AdresseKurz", 1);
            JDataRow rowTest = dsTest.getRow();
            String testPLZ = rowTest.getValue("PLZ");
            if (testPLZ.equals("12345") == false) {
                fail("TransactionTest; Commit keine Wirkung");
            }
            rowTest.setValue("PLZ", oldPLZ);
            pl.setDataset(dsTest);
            pl.commitTransaction("test9");
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}