/*
 * Created on 19.05.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package de.pkjs.pltest;
import de.jdataset.*;
import de.pkjs.pl.PL;
import junit.framework.TestCase;

import java.util.*;
/**
 * @author peter
 */
public class BuchungsSatzTest extends TestCase {
	private PL pl = AllTests.getPL();
	
	public void test() {
		System.out.println("--- BEGIN BuchungsSatz ---");
		try {
			{
				JDataSet ds1 = pl.getDataset("BuchungEinzeln", 1);
				//System.out.println(ds1.getXml());
				String kontoName = ds1.getValuePath("buchung#konto@kontoName");
				System.out.println(kontoName);
				String gegenkontoName = ds1.getValuePath("buchung#gegenkonto@kontoName");
				System.out.println(gegenkontoName);
			}
			// Saldo???
			{			
				pl.getDataset("SaldoKonto", 1000);
				//System.out.println(ds.getXml());
			}
			{
				System.out.println("Konto --> Buchungen");
				JDataSet ds = pl.getDataset("KontoBuchung", 1000);
				//System.out.println(ds.getXml());
				System.out.println("Konto");
				
				Iterator kRows = ds.getDataRowsPath("buchung.buchung");
				while (kRows.hasNext()) {
					JDataRow row = (JDataRow)kRows.next();
					int kontonummer = row.getValueInt("konto");
					if (kontonummer != 1000) {
						fail("Falsche Kontonummer != 1000");
					}
				}
				System.out.println("GegenkontoKonto");
				Iterator gRows = ds.getDataRowsPath("buchung.gegenkonto");
				while (gRows.hasNext()) {
					JDataRow row = (JDataRow)gRows.next();
					int kontonummer = row.getValueInt("gegenkonto");
					if (kontonummer != 1000) {
						fail("Falsches Gegenkonto != 1000");
					}
				}
			}
			{
				JDataSet ds = pl.getDataset("KontoBuchung", 1000);
				String bt1 = ds.getValuePath("konto.buchung[1]@buchungsText");
				ds.setValuePath("konto.buchung[1]@buchungsText", bt1+"+");				
				String bt2 = ds.getValuePath("konto.gegenkonto[1]@buchungsText");
				ds.setValuePath("konto.gegenkonto[1]@buchungsText", bt2+"+");	
				if (ds.hasChanges() == false) {
					fail("BuchungsSatzTest: Keine Änderung am DS??");
				} else {
					JDataSet dsChanges = ds.getChanges();
					System.out.println(dsChanges.getXml());
					int anz = pl.setDataset(dsChanges);
					if (anz != 2) {
						fail("BuchungsSatzTest: Zwei Änderungen an Buchungen!");
					}
				}
			}
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
		
		System.out.println("--- ENDE BuchungsSatz ---");
	}

}
