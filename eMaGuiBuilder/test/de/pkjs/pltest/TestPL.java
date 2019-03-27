/*
 * Created on 24.05.2004
 */
package de.pkjs.pltest;

import java.util.ArrayList;
import java.util.Iterator;

import de.jdataset.JDataSet;
import de.pkjs.pl.PL;
import electric.xml.Document;

import junit.framework.TestCase;

/**
 * @author pkoeker
 */
public class TestPL extends TestCase {
	private PL pl = AllTests.getPL();

	/*
	 * @see TestCase#setUp()
	 */
	public void test1() throws Exception {
		try {
			pl.setDebug(true);
			JDataSet ds = pl.getDataset("TestJoin",1);
			pl.setDebug(false);
			System.out.println(ds.getXml());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


//	public void testGetEncoding() {
//		try	{
//			String s = pl.getEncoding();
//			assertNotNull("Encoding nicht gesetzt!!!",s);
//		}
//		catch(Exception e)
//		{
//			fail("testGetEncoding");
//		}
//		
//	}

	public void testGetCurrentDB() {
		try	{
			String s = pl.getCurrentDatabaseName();
			assertNotNull("Current Database nicht gesetzt!!!",s);
		}
		catch(Exception e)
		{
			fail("testGetCurrentDB");
		}
	}
	public void test2() {
		System.out.println("--- START TestPL");
		try {
			{
				System.out.print("Database: ");
				String dbName = pl.getCurrentDatabaseName();
				System.out.println(dbName);
			}
			{
				System.out.println("Requests:");
				ArrayList dsn = pl.getDatasetNames();
				for (Iterator i = dsn.iterator(); i.hasNext();) {
					String dsName = (String)i.next();
					System.out.println(dsName);
				}
			}
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
		System.out.print("--- END TestPL");
	}
//	public void testMetaDataset() {
//		JDataSet ds = pl.getMetaDataSet();
//		System.out.println(ds.getXml());
//	}
	public void testMetaDoc() {
		Document doc = pl.getDatabaseMetaDataDoc();
		System.out.println(doc);
	}
}
