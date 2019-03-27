/*
 * Created on 21.06.2003
 */
package de.pkjs.pltest;

import java.sql.Types;
import java.util.Collection;

import junit.framework.TestCase;
import de.jdataset.DataView;
import de.jdataset.JDataSet;
import de.jdataset.NVPair;
import de.jdataset.ParameterList;
import de.pkjs.pl.IPLContext;
/**
 * @author peter
 */
public class TestParameter extends TestCase {
	private IPLContext pl = AllTests.getPL();
	public void test() {
		System.out.println("--- BEGIN TestParameter ---");
		{
			try {
				ParameterList lst = new ParameterList();
				lst.addParameter("AdrsName", "Rudi%");
				lst.addParameter("Strasse", "Milch%");
				lst.addParameter("PersName", "Joh%");
				// Lesen 1
				JDataSet ds = pl.getDataset("AdresseParameterBeide", lst); 
				String erg1 = ds.getXml(false).toString();
				//System.out.println(erg1);
				// Wiederholen des Lesens mit selbem Parameterm
				ds = pl.getDataset("AdresseParameterBeide", lst); 
				String erg2 = ds.getXml(false).toString();
				//System.out.println(erg2);
				assertEquals("Wiederholung des Lesens mit Parametern führt nicht zum selben Ergebnis!",erg1, erg2);
			} catch (Exception ex) {
				fail(ex.getMessage());
			}
		}
		{
			try {
				ParameterList lst = new ParameterList();
				lst.addParameter("AdrsName", "Rudi%");
				lst.addParameter("Strasse", "Milch%");
				// Lesen 1
				JDataSet ds = pl.getDataset("AdresseParameterAdrs", lst); 
				String erg1 = ds.getXml(false).toString();
				//System.out.println(erg1);
				// Wiederholen des Lesens mit selbem Parameterm
				ds = pl.getDataset("AdresseParameterAdrs", lst); 
				String erg2 = ds.getXml(false).toString();
				//System.out.println(erg2);
				assertEquals("Wiederholung des Lesens mit Parametern führt nicht zum selben Ergebnis!",erg1, erg2);
			} catch (Exception ex) {
				fail(ex.getMessage());
			}
		}
		{
			try {
				ParameterList lst = new ParameterList();
				lst.addParameter("PersName", "Joh%");
				// Lesen 1
				JDataSet ds = pl.getDataset("AdresseParameterPers", lst); 
				String erg1 = ds.getXml(false).toString();
				//System.out.println(erg1);
				// Wiederholen des Lesens mit selbem Parameterm
				ds = pl.getDataset("AdresseParameterPers", lst); 
				String erg2 = ds.getXml(false).toString();
				//System.out.println(erg2);
				assertEquals("Wiederholung des Lesens mit Parametern führt nicht zum selben Ergebnis!",erg1, erg2);
			} catch (Exception ex) {
				fail(ex.getMessage());
			}
		}
		{
			try {
				JDataSet ds = pl.getAll("Schlagworte");
				NVPair cond = new NVPair("schlagwort", "Balkon", Types.VARCHAR);
				DataView dv = new DataView(null, cond);
				Collection coll = ds.getChildRows(dv);
				if (coll.isEmpty()) {
					fail("Es fehlt das Schlagwort 'Balkon'");
				} else {
					//
				}
			} catch (Exception ex) {
				fail(ex.getMessage());
				ex.printStackTrace();
			}
		}
		System.out.println("--- END TestParameter ---");
	}
}
