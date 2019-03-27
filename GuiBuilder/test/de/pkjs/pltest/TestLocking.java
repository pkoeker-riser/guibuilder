/*
 * Created on 19.05.2003
 */
package de.pkjs.pltest;
import junit.framework.TestCase;
import de.jdataset.*;
import de.pkjs.pl.IPLContext;
/**
 * @author peter
 */
public class TestLocking extends TestCase {
	private IPLContext pl = AllTests.getPL();
	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TestLocking.class);
	
	public void test() {
		System.out.println("--- BEGIN TestLocking ---");
		String datasetName = "AdresseKurz";
		//String rootTableName = "adresse";
		// Lesen
		try {
			JDataSet ds1 = pl.getDataset(datasetName, 1); // ## T1
			String bem = ds1.getValuePath("@bemerkung");
			bem = bem + "+";
			ds1.setValuePath("@bemerkung", bem);
			
			JDataSet ds2 = pl.getDataset(datasetName, 1); // ## T2
			String bem2 = ds2.getValuePath("@bemerkung");
			bem2 = bem2 + "+";
			ds2.setValuePath("@bemerkung", bem2);
			// zurückschreiben
			pl.setDataset(ds1);
			//JDataSet dsx = pl.getDataset(datasetName, 1); // ## TX
			//System.out.println(dsx.getXml());
			String msg = "Jetzt kommt eine erwartete Fehlermeldung!";
			System.err.println(msg);
			logger.info(msg);
			pl.setDataset(ds2);
			fail("TestLocking: optimistisches Locking ging schief");
		}	catch (Exception ex) { // erwarteter Fehler
			logger.info("Ende der erwarteten Fehlermeldung");
		} 
		System.out.println("--- END TestLocking ---");
	}
}
