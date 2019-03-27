/*
 * Created on 01.08.2003
 */
package de.guibuilder.test.utils;
import java.util.Date;
import java.util.Calendar;

import de.jdataset.*;
/**
 * @author kknobloch
 */
public class TestJDataSetSupport {

	JDataSet dsAdressen;
	JDataSet dsAdresse;
	JDataSet dsCmbFunktionen;
	
	public TestJDataSetSupport() {
		
		this.initAdressen();
		this.initEineAdresse();
		this.initCmbFunktionen();
		
	}

	public JDataSet getAdressen() {
		
		return this.dsAdressen;
		
	}

	public JDataSet getEineAdresse() {
		
		return this.dsAdresse;
		
	}
	
	public JDataSet getCmbFunktionen() {
		
		return this.dsCmbFunktionen;
	}

	private void initEineAdresse() {
		this.dsAdresse = new JDataSet("TestAdresse");
		
		JDataTable tbl = new JDataTable("Adressen");
		this.dsAdresse.addRootTable(tbl);
		
		tbl.addColumn("oid", java.sql.Types.INTEGER);
		tbl.addColumn("name", java.sql.Types.VARCHAR);
		tbl.addColumn("strasse", java.sql.Types.VARCHAR);
		tbl.addColumn("plz", java.sql.Types.INTEGER);
		tbl.addColumn("ort", java.sql.Types.VARCHAR);
		tbl.addColumn("gueltig", java.sql.Types.BOOLEAN);
		tbl.addColumn("ungueltigSeit", java.sql.Types.DATE);
		tbl.addColumn("gebuehren", java.sql.Types.DOUBLE);
		tbl.addColumn("uhrzeit", java.sql.Types.DATE);
		tbl.addColumn("inhaltsangabe", java.sql.Types.VARCHAR);
		tbl.addColumn("editor", java.sql.Types.VARCHAR);
		tbl.addColumn("passwort", java.sql.Types.VARCHAR);
		tbl.addColumn("art", java.sql.Types.VARCHAR);
		tbl.addColumn("optvalue", java.sql.Types.INTEGER);
		tbl.addColumn("fk_funkid", java.sql.Types.INTEGER);

		JDataRow row = tbl.createNewRow();
		
		row.setValue("oid", 4711);
		row.setValue("name", "Heiner von M¸ller");
		row.setValue("strasse", "Dorfstraﬂe 17");
		row.setValue("plz", 15831);
		row.setValue("ort", "Groﬂ Kienitz");
		row.setValue("gueltig", true);

		Calendar c = Calendar.getInstance();
		c.set(2003,1-1, 1, 0, 0, 0);
		Date d = c.getTime();
		row.setValue("ungueltigSeit", d);

		Double dbVal = new Double(12.5);
		row.setValue("gebuehren", dbVal);
		
		c.set(2003, 1, 1, 14, 45, 30);
		d = c.getTime();
		//row.setValue("uhrzeit", d);
		row.setValue("uhrzeit", "14:45");

		row.setValue("inhaltsangabe", this.getLangenText());
		row.setValue("editor", this.getLangenText());
		row.setValue("passwort", "wasserfall");
		row.setValue("art","artR");
		row.setValue("optvalue",1);
		row.setValue("fk_funkid",3);

		this.dsAdresse.addChildRow(row);
		this.dsAdresse.commitChanges();

		System.out.println(this.dsAdresse.getXml());
		
	}

	private void initAdressen() {
		this.dsAdressen = new JDataSet("TestAdressen");
		
		JDataTable tbl = new JDataTable("Adressen");
		this.dsAdressen.addRootTable(tbl);
		
		tbl.addColumn("oid", java.sql.Types.INTEGER);
		tbl.addColumn("name", java.sql.Types.VARCHAR);
		tbl.addColumn("strasse", java.sql.Types.VARCHAR);
		tbl.addColumn("plz", java.sql.Types.INTEGER);
		tbl.addColumn("ort", java.sql.Types.VARCHAR);
		
		JDataRow row = tbl.createNewRow();
		
		row.setValue("oid", 4711);
		row.setValue("name", "Heiner von M¸ller");
		row.setValue("strasse", "Dorfstraﬂe 17");
		row.setValue("plz", 15831);
		row.setValue("ort", "Groﬂ Kienitz");
		
		this.dsAdressen.addChildRow(row);
		JDataRow row2 = tbl.createNewRow();
		
		row2.setValue("oid", 4712);
		row2.setValue("name", "Erwin Schulze");
		row2.setValue("strasse", "Hagelberger Straﬂe 10");
		row2.setValue("plz", 10965);
		row2.setValue("ort", "Berlin");
		
		this.dsAdressen.addChildRow(row2);
		JDataRow row3 = tbl.createNewRow();
		
		row3.setValue("oid", 4713);
		row3.setValue("name", "Ute Mayer");
		row3.setValue("strasse", "Groﬂbeerenstraﬂe 120b");
		row3.setValue("plz", 10965);
		row3.setValue("ort", "Berlin");
		
		this.dsAdressen.addChildRow(row3);
		this.dsAdressen.commitChanges();
	}
	
	private void initCmbFunktionen() {
		
		this.dsCmbFunktionen = new JDataSet("Funktionen");
		JDataTable tbl = new JDataTable("Funktion");
		tbl.addColumn("funkid", java.sql.Types.INTEGER);
		tbl.addColumn("bezeichnung", java.sql.Types.VARCHAR);
		this.dsCmbFunktionen.addRootTable(tbl);
		
		JDataRow row = tbl.createNewRow();
		row.setValue("funkid", 1);
		row.setValue("bezeichnung","Gesch‰ftsf¸hrer");
		this.dsCmbFunktionen.addChildRow(row);
		
		JDataRow row2 = tbl.createNewRow();
		row2.setValue("funkid", 2);
		row2.setValue("bezeichnung","Abteilungsleiter");
		this.dsCmbFunktionen.addChildRow(row2);

		JDataRow row3 = tbl.createNewRow();
		row3.setValue("funkid", 3);
		row3.setValue("bezeichnung","Angestellter");
		this.dsCmbFunktionen.addChildRow(row3);
		this.dsCmbFunktionen.commitChanges();				
		
	}
	
	private String getLangenText() {
		
		return "Dies ist ein Text mit 6 Zeilen\n" + 
		        "Zeile 2\n" +				"Zeile 3\n" +
				"Zeile 4\n" +
				"Zeile 5\n" +
				"Zeile 6";
		        
		
	}
	
}
