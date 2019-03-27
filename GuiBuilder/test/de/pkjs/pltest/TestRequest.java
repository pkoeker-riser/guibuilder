/*
 * Created on 26.05.2003
 */
package de.pkjs.pltest;

import junit.framework.TestCase;
import de.pkjs.pl.Database;
import de.pkjs.pl.IPLContext;

/**
 * @author peter
 */
public class TestRequest extends TestCase {
	private IPLContext pl = AllTests.getPL();
	public void test() {
		System.out.println("--- BEGIN TestRequest ---");
		Database db = pl.getCurrentDatabase();
			try {
				db.createRequest("test");
//				// Table-Request erzeugen
//				TableRequest tr = req.createRootTableRequest("adresse", "*");
//				// Primary Key festlegen.
//				tr.setPK("adrsid");
//				// Daten lesen
//				JDataSet ds = pl.getDataset("test", 1);
//				System.out.println(ds.getXml());
			} catch(Exception ex) {
				fail(ex.getMessage());
			}
		System.out.println("--- END TestRequest ---");
	}
}
