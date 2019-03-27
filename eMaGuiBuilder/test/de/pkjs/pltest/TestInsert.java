/*
 * Created on 27.02.2005
 */
package de.pkjs.pltest;

import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.pkjs.pl.PL;
import junit.framework.TestCase;

/**
 * @author peter
 */
public class TestInsert extends TestCase {
	private PL pl = AllTests.getPL();
	public void testInsertReadOnly() {
		// Read-only Columns dürfen nicht INSERTed werden.
		try {
			JDataSet ds = pl.getEmptyDataset("TestInsertReadOnly");
			JDataRow row = ds.createChildRow();
			row.setValue("adrsid", 9999);
			row.setValue("Kennung", "TestInsert");
			row.setValue("Name1", "TestInsert");
			pl.setDataset(ds);
			ds.commitChanges();
			row.setDeleted(true);
			pl.setDataset(ds);
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
}
