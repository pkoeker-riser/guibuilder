/*
 * Created on 02.08.2003
 */
package de.pkjs.pltest;

import java.util.ArrayList;

import junit.framework.TestCase;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataSet.ProfileEntry;
import de.pkjs.pl.IPLContext;

/**
 * @author peter
 */
public class LastTest extends TestCase {
	// Attributes
	private IPLContext pl = AllTests.getPL();
	private final static int ANZ = 1000;
	// Constructor
	// Test
	public void test() {
		System.out.println("BEGIN LastTest");
		try {
		//JDataSet dsx = pl.getDatasetSql("adresse", "adrsid,name1,name2,name3", " FROM adresse where adrsid < 10");
		//System.out.println(dsx.getXml());
		// größer 1000 löschen
		pl.executeSql("Delete FROM adresse WHERE adrsid >= 1000");
		
		String datasetName = "AdresseKurz";
		long lastTime = System.currentTimeMillis();
		pl.setDebug(false);
		double d = 0;
		// 1. 1000 Adressen Schreiben
		
		for (int i = 0; i < ANZ; i++) {
			JDataSet ds = pl.getEmptyDataset(datasetName);
			JDataRow adrs = ds.createChildRow();
			adrs.setValue("adrsid", i+1000);
			adrs.setValue("kennung", "Kennung"+i);
			adrs.setValue("name1", "Name"+i);
			adrs.setValue("PLZ", i);
			pl.setDataset(ds);
		}
		d = (System.currentTimeMillis() - lastTime);
		System.out.println(datasetName + " insert "+ d+ " pro Sekunde: "+ANZ * 1000 / d); // 27/Sec // MSDE:144
		lastTime = System.currentTimeMillis();
		
		// 1.1 SELECT *
		{
			String sql = "Select * from Adresse";
			JDataSet dsSql = pl.getDatasetSql("Adrsse", sql); 
			ArrayList<ProfileEntry> al = dsSql.getProfiler();
			for (ProfileEntry e: al) {
				System.out.println(e.toString());
			}
		}
		// 2. Adressen lesen
		
		for (int i = 0; i < ANZ; i++) {
			pl.getDataset(datasetName, i+1000);
		}
		d = (System.currentTimeMillis() - lastTime);
		System.out.println(datasetName + " select "+ d+ " pro Sekunde: "+ANZ * 1000 / d); // 217/Sec // MSDE:52
		lastTime = System.currentTimeMillis();
		
		// 2.a Alle Daten lesen
		/*
		for (int i = 0; i < 10; i++) {
			JDataSet dsAlle = pl.getAll(datasetName);
			System.out.println(dsAlle.getRowCount());
		}
		
		d = (System.currentTimeMillis() - lastTime);
		System.out.println(datasetName + " getAll "+ d+ " pro Sekunde: "+10 * 1000 / d); // 217/Sec // MSDE:52
		lastTime = System.currentTimeMillis();
		*/
		// 3. Lesen + Update
		for (int i = 0; i < ANZ; i++) {
			JDataSet ds = pl.getDataset(datasetName, i+1000);
			JDataRow rowAdresse = ds.getRow();
			rowAdresse.setValue("ort", "Berlin-Kreuzberg");
			pl.setDataset(ds);
		}
		d = (System.currentTimeMillis() - lastTime);
		System.out.println(datasetName + " select/update(neu) "+ d+ " pro Sekunde: "+ANZ * 1000 / d); // 217/Sec // MSDE:52
		lastTime = System.currentTimeMillis();
		// 4. Adressen löschen
		for (int i = 0; i < ANZ; i++) {
			JDataSet ds = pl.getDataset(datasetName, i+1000);
			ds.setDeleted(true);
			pl.setDataset(ds);
		}
		d = (System.currentTimeMillis() - lastTime);
		System.out.println(datasetName + " select/delete "+ d+ " pro Sekunde: "+ANZ * 1000 / d); // 24/Sec // MSDE: 48
		lastTime = System.currentTimeMillis();
		
		// AdresseKurz
		/*
		for (int i = 0; i < 100; i++) {
			JDataSet ds = pl.getDataset(datasetName, 1);
			// Adresse ändern
			JDataRow rowAdresse = ds.getRow();
			String ort = rowAdresse.setValue("ort", "Berlin-Kreuzberg");
			
		}
		d = (System.currentTimeMillis() - lastTime);
		System.out.println(datasetName + " select "+ d+ " pro Sekunde: "+100 * 1000 / d); // 69/Sec
		*/
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
		System.out.println("END LastTest");
	}
}
