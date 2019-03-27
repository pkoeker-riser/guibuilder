/*
 * Created on 19.10.2003
 */
package de.pkjs.pja;

//import java.math.BigDecimal;
//import java.util.Iterator;

import de.guibuilder.framework.*;
import de.guibuilder.framework.event.*;

import de.jdataset.*;

import de.pkjs.util.*;

import java.util.*;


/**
 * @author peter
 */
public class ControlMitarbeiter {
	private static final int NEU = 0;
	private static final int ALT = 1;
	private static final int LEER = 2;
	private int status = LEER;
	
	private PjaService srv = PjaService.getInstance();
	private GuiWindow maWindow;
	private GuiDialog buWindow;
	
	public ControlMitarbeiter() {
		try {
			maWindow = GuiFactory.getInstance().createWindow("example/Projekt/Mitarbeiter.xml");
			maWindow.setController(this);
			// ComboBox Status füllen
			JDataSet dsRolle = srv.getRollen();
			GuiCombo cmbRolle = (GuiCombo)maWindow.getGuiComponent("rolle");
			cmbRolle.setItems(dsRolle);
			// Comboboxen in Tabellen füllen: Rolle
			GuiTable tblMA = (GuiTable)maWindow.getGuiComponent("tabMitarbeiter.tblMitarbeiter");
			tblMA.setItems("projekt", srv.getProjekte());
			tblMA.setItems("rolle", srv.getRollen());
			this.setStatus(LEER);
			// Test
			java.sql.Date vonDatum = Convert.toSqlDate("01.10.2003");
			java.sql.Date bisDatum = Convert.toSqlDate("18.10.2003");
			JDataSet xx = srv.getMitarbeiterBuchungen(1008, vonDatum, bisDatum);
			System.out.println(xx.getXml());
		} catch (Exception ex) {
			GuiUtil.showEx(ex);
		}
		
	}
	
	public void maNew(GuiUserEvent event) {
		maWindow.reset();
		JDataSet ds = srv.getLeerenMitarbeiter();
		maWindow.setDatasetValues(ds);
		this.setStatus(NEU);
	}
	/**
	 * Toolbar/Menu "Öffnen"
	 * @param event
	 */
	public void maOpen(GuiUserEvent event) {
		try {
			GuiDialog dia = (GuiDialog)GuiFactory.getInstance().createWindow("example/Projekt/MitarbeiterAuswahl.xml");
			JDataSet ds = srv.getMitarbeiterAuswahl();
			dia.reset();
			//System.out.println(ds.getXml());
			dia.setDatasetValues(ds);
			if (dia.showDialog()) {
				GuiTable tbl = dia.getRootPane().getCurrentTable();
				long id = Convert.toLong( tbl.getCellValue(0) );
				JDataSet dsAuswahl = srv.getMitarbeiter(id);
				
				maWindow.reset();
				maWindow.setDatasetValues(dsAuswahl);
				this.setStatus(ALT);
			}
		} catch (Exception ex) {
			GuiUtil.showEx(ex);
		}
	}
	/**
	 * Toolbar/Menu "Speichern"
	 * @param event
	 */
	public void maSave(GuiUserEvent event) {
		JDataSet ds = maWindow.getDatasetValues();
		if (ds.hasChanges()) {
			JDataSet dsChanges = ds.getChanges();
			//System.out.println(dsChanges.getXml());
			try {
				maWindow.verify();
				srv.setMitarbeiter(dsChanges);
				ds.commitChanges();
				maWindow.commitChanges();
				this.setStatus(ALT);
			} catch (Exception ex) {
				GuiUtil.showEx(ex);
			}
		}
	}
	/**
	 * Menu "Löschen"
	 * @param event
	 */
	public void maRemove(GuiUserEvent event) {
		if (GuiUtil.yesNoMessage(this.maWindow, "Mitarbeiter Löschen", "Soll der Mitarbeiter gelöscht werden?")) {
			JDataSet ds = this.maWindow.getDatasetValues();
			ds.setDeleted(true);
			srv.setMitarbeiter(ds);
			this.maWindow.reset();
			this.setStatus(LEER);
		}			
	}	
	public void maBuchungen(GuiUserEvent event) {
		String id = event.window.getRootPane().getCurrentTable().getCellValue(0);
		
		try {
			GuiDialog buWindow = (GuiDialog)GuiFactory.getInstance().createWindow("example/Projekt/MitarbeiterBuchung.xml");
			buWindow.setController(this);
			Calendar cal = Calendar.getInstance();
			int tag = cal.get(Calendar.DAY_OF_WEEK); // 1 = Sonntag!!!
			if (tag != Calendar.MONDAY) {
				cal.add(Calendar.DATE, Calendar.MONDAY-tag);
			}
			cal.add(Calendar.DATE, -7);
			int woche = cal.get(Calendar.WEEK_OF_YEAR);
			buWindow.setValue("woche", Convert.toString(woche));
			Date von = cal.getTime();
			cal.add(Calendar.DATE, 6);
			Date bis = cal.getTime();
			JDataSet ds = srv.getMitarbeiterBuchungen(Convert.toLong(id), von, bis );
			buWindow.setDatasetValues(ds);
			cal.add(Calendar.DATE, -6);
			for (int i = 1; i <= 7; i++) {
				String name = "t"+Convert.toString(i);
				GuiDate d = (GuiDate)buWindow.getGuiComponent(name);
				d.setValue(cal.getTime());
				cal.add(Calendar.DATE, 1);
			}
			if (buWindow.showDialog()) {
				
			}
		} catch (Exception ex) {
			GuiUtil.showEx(ex);
		}
	}
	void show() {
		maWindow.show();
	}
	// ============ private ===================================
	private void setStatus(int status) {
		this.status = status;
		switch (status) {
			case NEU:
				maWindow.getAction("iSave").setEnabled(true);
				maWindow.getAction("iDelete").setEnabled(false);
				maWindow.getAction("tSave").setEnabled(true);
				break;
			case ALT:
				maWindow.getAction("iSave").setEnabled(true);
				maWindow.getAction("iDelete").setEnabled(true);
				maWindow.getAction("tSave").setEnabled(true);
				break;
			case LEER:
				maWindow.getAction("iSave").setEnabled(false);
				maWindow.getAction("iDelete").setEnabled(false);
				maWindow.getAction("tSave").setEnabled(false);
				break;
		}
	}
	// ================== Buchungen =================================
	public void weekPrev(GuiUserEvent event) {
		int woche = Convert.toInt(buWindow.getValue("woche"));
		woche--;
		buWindow.setValue("woche", Convert.toString(woche));
	}
	public void weekNext(GuiUserEvent event) {
		int woche = Convert.toInt(buWindow.getValue("woche"));
		woche++;
		buWindow.setValue("woche", Convert.toString(woche));
	}
	public void weekModified(GuiUserEvent event) {
		int woche = Convert.toInt(buWindow.getValue("woche"));
		
	}
}
