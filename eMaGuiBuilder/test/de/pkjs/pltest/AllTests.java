/*
 * Created on 24.05.2004
 */
package de.pkjs.pltest;

import org.apache.log4j.xml.DOMConfigurator;

import de.guibuilder.test.jdataset.JDataColumnTest;
import de.guibuilder.test.jdataset.JDataSetSimpleTest;
import de.guibuilder.test.jdataset.TestBean;
import de.guibuilder.test.jdataset.TestDataset;
import de.pkjs.pl.PL;
import junit.framework.*;
/**
 * @author pkoeker
 */
public class AllTests {
	private static PL pl;
	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AllTests.class);

	public static void main(String[] args) {
		try {
			//junit.textui.TestRunner.run(AllTests.class);
			(new junit.swingui.TestRunner()).start(
			      new String[] { "-noloading", "de.pkjs.pltest.AllTests" } );
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
	public static PL getPL() {
		try {
			if (pl == null) {
			   synchronized(AllTests.class) {
			      pl = new PL("TestPLConfig.xml");
			   }
			}
			return pl;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	public static Test suite() {
		DOMConfigurator.configure("Log4JTestConnection.xml");
		logger.info("TestSuite started #################");
		TestSuite suite = new TestSuite("Test for de.jdataset / de.pkjs.pl");
		//$JUnit-BEGIN$
		//##suite.addTestSuite(InitDatabase.class);
		
		//suite.addTestSuite(ConnectionPoolTest.class);
		
		//##suite.addTestSuite(TestOnDelete.class);
		//##suite.addTestSuite(TestTransient.class);
//		suite.addTestSuite(TestInsert.class);
		//suite.addTestSuite(TestJoin.class);
		suite.addTestSuite(TestCascade.class);
		suite.addTestSuite(TestPL.class);
		suite.addTestSuite(DataViewTest.class);
		suite.addTestSuite(SimpleDatasetTest.class);
		suite.addTestSuite(JDataRowTest.class);
		suite.addTestSuite(TestPath.class);
		suite.addTestSuite(MetaDataTest.class);
	
		suite.addTestSuite(TestDelete.class);
		suite.addTestSuite(XmlTest.class);
		suite.addTestSuite(JDatasetTest.class);
		suite.addTestSuite(JDataTableTest.class);
		suite.addTestSuite(TestLocking.class);
		suite.addTestSuite(AdresseTest.class);
		suite.addTestSuite(TestRequest.class);
		suite.addTestSuite(TestParameter.class);
		//suite.addTestSuite(BuchungsSatzTest.class);
		suite.addTestSuite(ExecuteSqlTest.class);
		suite.addTestSuite(TransactionTest.class);
		suite.addTestSuite(TestConvert.class);
		suite.addTestSuite(TestMetadata.class);
		suite.addTestSuite(TestSeq.class);
		suite.addTestSuite(TestStatement.class);
		// DS
		suite.addTestSuite(JDataSetSimpleTest.class);
		suite.addTestSuite(JDataColumnTest.class);
		suite.addTestSuite(TestBean.class);
		suite.addTestSuite(TestDataset.class);

		suite.addTestSuite(LastTest.class);
		//$JUnit-END$
		return suite;
	}

}
