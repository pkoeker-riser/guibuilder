/*
 * Created on 23.04.2005
 */
package de.pkjs.pltest;

import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.pkjs.pl.PL;
import junit.framework.TestCase;

/**
 * @author peter
 */
public class TestCascade extends TestCase {
	public void testUpdateCascade() {
		try {
			PL pl = AllTests.getPL();
			JDataSet ds = pl.getEmptyDataset("Transient1");
			System.out.println(ds.getSize());
			JDataRow row = ds.getDataTable().createNewRow();
			ds.addChildRow(row);
			System.out.println(ds.getSize());
			row.setValue("ArtikelNummer", 1);
			row.setValue("Artikelname", "Tretroller");
			JDataTable ctbl = row.getDataTable().getChildTable("Kunde");
			JDataRow crow1 = ctbl.createNewRow();
			row.addChildRow(crow1);
			crow1.setValue("KundenNummer", 4711);
			crow1.setValue("KundenName", "Müller");
			crow1.setValue("Einzelpreis", 7.15);
			crow1.setValue("FK_ArtikelNummer", 1);
			System.out.println(ds.getSize());
			JDataRow crow2 = ctbl.createNewRow();
			row.addChildRow(crow2);
			crow2.setValue("KundenNummer", 4712);
			crow2.setValue("KundenName", "Schneider");
			crow2.setValue("Einzelpreis", 8.55);
			crow2.setValue("FK_ArtikelNummer", 1);
			System.out.println(ds.getSize());
			ds.commitChanges();
			System.out.println(ds.getSize());
			row.setValue("ArtikelNummer", 2);
			assertEquals("2", crow1.getValue("FK_ArtikelNummer"));
			assertEquals("2", crow2.getValue("FK_ArtikelNummer"));
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
}
