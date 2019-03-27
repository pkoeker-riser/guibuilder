/*
 * Created on 24.05.2004
 */
package de.pkjs.pltest;

import java.util.Iterator;

import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.pkjs.pl.PL;

import junit.framework.TestCase;

/**
 * @author pkoeker
 */
public class SimpleDatasetTest extends TestCase {
	private PL pl = AllTests.getPL();
	/**
	 * Es wird mit getAll ein einfacher Dataset eingelesen,
	 * eine Zeile hinzugef�gt,
	 * dieses zur�ckgeschrieben,
	 * wieder eingelesen,
	 * und die neue Zeile wieder gel�scht.
	 */
	public void test() {
		System.out.println("--- BEGIN SimpleDatasetTest ---");
		String datasetName = "Funktionen";
		String tableName = "FUNKTIONEN"; //##
		try {
			JDataSet ds = pl.getAll(datasetName); // ## T1
			System.out.println(ds.getXml());
			assertEquals("Dataset hat falschen DatasetNamen", ds.getDatasetName(), datasetName);

			int rows1 = ds.getRowCount(tableName); // Z�hlen
			JDataRow row = ds.createChildRow(tableName);
			assertEquals("Neu erzeugte Zeilen m�ssen die Eigenschaft 'inserted' haben", row.isInserted(), true);
			int rows2 = ds.getRowCount(tableName);
			assertEquals("Anzahl der Rows stimmt nicht nach addRow", rows1+1, rows2);
			row.setValue("funktion", "SimpleDatasetTest");
			assertEquals("Nach einer �nderung mu� die Eigenschaft 'inserted' gesetzt sein.", row.hasChanges(), true );

			JDataSet dsChanges = ds.getChanges();
			assertNotNull("Nach einer �nderung ist der ge�nderte Dataset leer??", dsChanges);
			
			assertEquals("Dataset hat falschen DatasetNamen", dsChanges.getDatasetName(), datasetName);
			int rows3 = dsChanges.getRowCount(tableName);
			assertEquals("Anzahl Zeilen im ge�nderten Dataset mu� '1' sein", rows3, 1);
			// Zur�ckschreiben
			pl.setDataset(dsChanges); // ## T2
			ds.commitChanges();
			assertEquals("Nach commitChanges mu� hasChanges 'false' ergeben!" ,ds.hasChanges(), false);
			// Neu lesen ###################################
			JDataSet ds2 = pl.getAll(datasetName); // ## T3 
			int rows4 = ds2.getRowCount(tableName);
			assertEquals("Anzahl Zeilen aus der Datenbank gelesen nach Insert mu� um eins erh�ht sein", rows4, rows1+1);
			// Modify ############################
			JDataRow modiRow = null;
			Iterator i2 = ds2.getChildRows(tableName);
			int modiCnt = 0;
			while (i2.hasNext()) {
				JDataRow tmp = (JDataRow)i2.next();
				if (tmp.getValue("funktion").equals("SimpleDatasetTest")) {
					modiCnt++; // Anzahl Zeilen ge�ndert
					modiRow = tmp;
					tmp.setValue("funktion", "SimpleDatasetTest2");
					assertEquals("Row mu� Eigenschaft 'modified' haben", tmp.isModified(), true);
				}
			}
			assertNotNull("Ge�nderte Row nach dem Einlesen nicht wiedergefunden!!??", modiRow );
			JDataSet dsChanges2 = ds2.getChanges();
			assertNotNull("Nach einer �nderung ist der ge�nderte Dataset leer??", dsChanges2);
			int modiCnt2 = dsChanges2.getRowCount();
			assertEquals("SimpleDatasetTest: ge�nderte Rows != Rows in dsChanges",modiCnt, modiCnt2);
			pl.setDataset(dsChanges2); // ## T4
			ds2.commitChanges();
			// L�schen #######################
			JDataSet ds3 = pl.getAll(datasetName); 
			Iterator i3 = ds3.getChildRows(tableName);
			while (i3.hasNext()) {
				JDataRow tmp = (JDataRow)i3.next();
				if (tmp.getValue("funktion").equals("SimpleDatasetTest2")) {
					tmp.setDeleted(true);
					assertEquals("Row mu� Eigenschaft 'deleted' haben", tmp.isDeleted(), true);
				}
			}
			JDataSet dsChanges3 = ds3.getChanges();
			assertNotNull("Nach einer �nderung ist der ge�nderte Dataset leer??", dsChanges3);
			pl.setDataset(dsChanges3); // ## T5
			ds3.commitChanges();
			// Nochmal lesen: jetzt mu� alles wie zuvor sein
			JDataSet ds4 = pl.getAll(datasetName); // ## T6
			rows4 = ds4.getRowCount(tableName);
			assertEquals("Am Ende dieses Test m�ssen die Rows in der Datenbank wieder gleich sein!", rows4,rows1);
		} catch (Exception ex) {
			fail(ex.getMessage());
			ex.printStackTrace();
		}
		System.out.println("--- END SimpleDataset ---");
	}
}
