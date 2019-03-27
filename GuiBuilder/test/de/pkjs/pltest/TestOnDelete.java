/*
 * Created on 27.01.2005
 */
package de.pkjs.pltest;

import de.jdataset.JDataColumn;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.pkjs.pl.PL;
import junit.framework.TestCase;

/**
 * Testet die Definition OnDelete="..." bei ChildTables
 * @author PKOEKER
 */
public class TestOnDelete extends TestCase {
	public void testSetNull() {
		try {
			PL pl = AllTests.getPL();
			JDataSet ds = pl.getEmptyDataset("AdresseDelete");
			JDataRow rowAdrs = ds.createChildRow();
			JDataRow row = rowAdrs.createChildRow("notiz");
			row.setValue("fk_adrsid", "x");
			ds.commitChanges();
			ds.setDeleted(true);
			String val = row.getValue("fk_adrsid");
			assertEquals("String muﬂ null(leer) sein", val, null);
			assertEquals("Feld muﬂ 'modified' sein", true, row.isModified("fk_adrsid"));
			ds.commitChanges();
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
	public void testCascade() {
		try {
			PL pl = AllTests.getPL();
			JDataSet ds = pl.getEmptyDataset("AdresseDelete");
			JDataRow rowAdrs = ds.createChildRow();
			JDataRow row = rowAdrs.createChildRow("termin");
			ds.commitChanges();
			ds.setDeleted(true);
			assertEquals("Abh‰ngige Zeile wird gelˆscht", row.isDeleted(), true);
			ds.commitChanges();
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
	public void testRestrict() {
		try {
			PL pl = AllTests.getPL();
			JDataSet ds = pl.getEmptyDataset("AdresseDelete");
			JDataRow rowAdrs = ds.createChildRow();
			rowAdrs.createChildRow("adrsslgw");
			try {
				ds.setDeleted(true);
				fail("Delete Restrict wirft keine Exception!");
			} catch (Exception ex) {
				// erwarteted Fehler
			}
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
	public void testSetDefault() {
		try {
			PL pl = AllTests.getPL();
			JDataSet ds = pl.getEmptyDataset("AdresseDelete");
			JDataRow rowAdrs = ds.createChildRow();
			JDataRow row = rowAdrs.createChildRow("person");
			JDataColumn col = row.getDataColumn("fk_adrsid");
			col.setDefaultValue("abc");
			row.setValue("fk_adrsid", "x");
			ds.commitChanges();
			ds.setDeleted(true);
			String val = row.getValue("fk_adrsid");
			assertEquals("Default-Wert gesetzt", val, "abc");
			assertEquals("Feld muﬂ 'modified' sein", true, row.isModified("fk_adrsid"));
			ds.commitChanges();
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
}
