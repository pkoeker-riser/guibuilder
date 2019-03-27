/*
 * Created on 12.05.2003
 */
package de.pkjs.pltest;
import de.jdataset.*;
import de.pkjs.pl.IPLContext;

import java.util.*;

import org.apache.log4j.xml.DOMConfigurator;

import junit.framework.TestCase;
/**
 * @author peter
 */
public class AdresseTest extends TestCase {
	private IPLContext pl = AllTests.getPL();
	
    public AdresseTest() {
        DOMConfigurator.configure("Log4JTestConnection.xml");
    }
    
	public void test() {
		System.out.println("*** BEGIN AdresseTest ***");
		String datasetName = "AdresseKurz";
		String rootTableName = "adresse";
		String persTableName = "person";
		int pers1 = 0;
		try {
			// Lesen
			{
				JDataSet ds = pl.getDataset(datasetName, 1); // ## T1
				//System.out.println(ds.getXml());
				assertEquals("Dataset hat falschen DatasetNamen", ds.getDatasetName(), datasetName);

				JDataTable tblAdresse = ds.getDataTable();
				assertEquals("RootTableName mismatch: "+ tblAdresse.getTablename(), tblAdresse.getTablename(), rootTableName);

				// Adresse ändern
				JDataRow rowAdresse = ds.getRow();
				String ort1 = rowAdresse.getValue("ort");
				String ort2 = rowAdresse.setValue("ort", "Berlin-Mitte");
				// Personen
				pers1 = rowAdresse.getChildRowCount(persTableName);
				JDataRow rowPerson =  rowAdresse.createChildRow(persTableName);
				int persAdd = rowAdresse.getChildRowCount(persTableName);
				assertEquals("Anzahl Person Counter stimmt nicht nach add", pers1 +1, persAdd);

				rowPerson.setValue("name", "Rudi Müller");
				rowPerson.setValue("fk_funkid", "2");
				JDataSet dsChanges = ds.getChanges();
				// Schreiben
				int x1 = pl.setDataset(dsChanges); // ## T2
				int xx = 2;
				if (ort1.equals(ort2)) xx--;
				assertEquals("Anzahl geänderter Datensätze falsch", x1, xx);
				ds.commitChanges();
			}
			// wieder lesen
			{
				JDataSet ds2 = pl.getDataset(datasetName, 1); // ## T3
				JDataRow rowAdresse = ds2.getRow();
				assertEquals("UPDATE 'ort' wird nicht aus Datenbank wieder eingelesen!?", rowAdresse.getValue("ort"), "Berlin-Mitte");
				int pers2 = rowAdresse.getChildRowCount(persTableName);
				assertEquals("Anzahl Personen stimmt nicht nach Insert", pers1 + 1, pers2);
				// Person wieder raus
				for (Iterator ip = rowAdresse.getChildRows(persTableName); ip.hasNext();) {
					JDataRow rowP = (JDataRow)ip.next();
					if (rowP.getValue("name").equals("Rudi Müller")) {
						rowP.setDeleted(true);
					}
				}
				JDataSet dsChanges2 = ds2.getChanges();
				assertNotNull("Keine Änderung am DataSet??", dsChanges2);
				int x2 = pl.setDataset(dsChanges2); // ## T4
				assertEquals("AdresseTest: Person kann nicht gelöscht werden", x2, 1);
				
				ds2.commitChanges();				
			}
			// Schlagworte
			{
				JDataSet ds3 = pl.getDataset(datasetName, 1); // ## T5
				JDataRow rowAdresse = ds3.getRow();
				JDataRow slgwRow = rowAdresse.createChildRow("adrsslgw");
				slgwRow.setValue("fk_slgwid", 5);
				JDataSet dsChanges3 = null;
				try {
					dsChanges3 = ds3.getChanges();
					assertNotNull("Keine Änderung am DataSet??", dsChanges3);
				} catch (Exception ex) {}
				try {
					pl.setDataset(dsChanges3); // ## T6
					ds3.commitChanges();
				} catch (Exception ex) {
					fail(ex.getMessage());
				}
				slgwRow.setDeleted(true); // das geht schief?
				JDataSet dsChanges4 = ds3.getChanges();
				try {
					pl.setDataset(dsChanges4); // ## T7
				} catch (Exception ex) {
					fail(ex.getMessage());
				}
				ds3.commitChanges();	
			}
			// Termine
			{
				JDataSet ds4 = pl.getDataset(datasetName, 1); // ## T8
				JDataRow rowAdresse = ds4.getRow();
				JDataRow terminRow = rowAdresse.createChildRow("termin");
				terminRow.setValue("datum","1.5.2003");
				terminRow.setValue("von", "3:59");
				terminRow.setValue("bis", "14:4");
				terminRow.setValue("bemerkung", "Hallo!");
				JDataSet dsChanges = ds4.getChanges();
				//System.out.println(dsChanges.getXml());
				int x4 = pl.setDataset(dsChanges); // ## T9
				assertEquals("Es muß *ein* Termin hinzugefügt werden!", x4, 1);
				ds4.commitChanges();
				// Hm, das kann als WebService nicht funktioniern,
				// da der Primärschlüssel dann nicht bekannt ist!
				terminRow.setDeleted(true); 
				JDataSet dsChanges2 = ds4.getChanges();
				int x5 = pl.setDataset(dsChanges2); // ## T10
				assertEquals("Es muß *ein* Termin gelöscht werden: "+x5, x5, 1);
				ds4.commitChanges();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
		System.out.println("*** END AdresseTest ***");
	}

}
