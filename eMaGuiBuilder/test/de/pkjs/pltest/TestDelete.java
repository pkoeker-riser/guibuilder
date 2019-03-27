/*
 * Created on 06.09.2003
 */
package de.pkjs.pltest;

import junit.framework.TestCase;
import de.jdataset.*;
import de.pkjs.pl.PL;

/**
 * @author peter
 */
public class TestDelete extends TestCase {
	public void test() {
		PL pl = AllTests.getPL();
		System.out.println("*** BEGIN DeleteTest ***");
		try {
			// Alles leer machen
			String sqlT = "DELETE FROM Termin WHERE terminid IN (4714,4715)";
			pl.executeSql(sqlT);
			String sqlP = "DELETE FROM Person WHERE persid IN (4712,4713)";
			pl.executeSql(sqlP);
			String sqlA = "DELETE FROM Adresse WHERE adrsid = 4711";
			pl.executeSql(sqlA);
			// 1. Dataset erzeugen
			JDataSet ds = pl.getEmptyDataset("AdresseKomplett");
			JDataRow adrs = ds.createChildRow();
			adrs.setValue("adrsId", 4711);
			adrs.setValue("Kennung", "Test4711");
			adrs.setValue("Name1", "Name 4711");
	
			JDataRow pers1 = adrs.createChildRow("Person");
			pers1.setValue("persId", 4712);
			pers1.setValue("Name", "Person4712");
	
			JDataRow pers2 = adrs.createChildRow("Person");
			pers2.setValue("persId", 4713);
			pers2.setValue("Name", "Person4713");
			
			JDataRow term1 = pers1.createChildRow("Termin");
			term1.setValue("TerminId", 4714);
			term1.setValue("Datum", pl.getTodayString());
			term1.setValue("Bemerkung", "Termin 4714");
			
			JDataRow term2 = pers1.createChildRow("Termin");
			term2.setValue("TerminId", 4715);
			term2.setValue("Datum", pl.getTodayString());
			term2.setValue("Bemerkung", "Termin 4715");
			pl.setDataset(ds);
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
		try {
			// 2. Dataset wieder lesen
			JDataSet ds2 = pl.getDataset("AdresseKomplett", 4711);
			//System.out.println(ds2.getXml());
			// 3. Löschen
			ds2.setDeleted(true);
			pl.setDataset(ds2);
			// 4. Jetzt kommt leerer dataset
			JDataSet ds3 = pl.getDataset("AdresseKomplett", 4711);
			assertEquals("TestDelete: Nach dem Löschen noch Rows vorhanden!", ds3.getRowCount(), 0);
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
		System.out.println("***  END DeleteTest  ***");
	}
}
