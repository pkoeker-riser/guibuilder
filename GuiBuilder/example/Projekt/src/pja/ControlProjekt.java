/*
 * Created on 18.10.2003
 */
package de.pkjs.pja;

import java.math.BigDecimal;
import java.util.Iterator;

import de.guibuilder.framework.*;
import de.guibuilder.framework.event.*;

import de.jdataset.*;

import de.pkjs.util.*;

/**
 * @author peter
 */
public class ControlProjekt {
	private static final int NEU = 0;
	private static final int ALT = 1;
	private static final int LEER = 2;
	private static final double ARBEITS_STUNDEN = 7.8;
	private int status = LEER;
	
	private PjaService srv = PjaService.getInstance();
	private GuiWindow projektWindow;
	/**
	 * Controller für Projekt.xml
	 *
	 */
	public ControlProjekt() {
		try {
			projektWindow = GuiFactory.getInstance().createWindow("example/Projekt/Projekt.xml");
			projektWindow.setController(this);
			// Comboboxen in Tabellen füllen: Rolle, Mitarbeiter, Status
			this.comboStatus();
			this.comboMitarbeiter();
			this.comboRollen();
			this.comboKostenarten();
			
			this.setStatus(LEER);
		} catch (Exception ex) {
			GuiUtil.showEx(ex);
		}
	}
	void show() {
		projektWindow.show();
	}
	// ComboBoxen aus Stammdaten füllen
	private void comboStatus() {
		// ComboBox Status füllen
		JDataSet dsStatus = srv.getProjektStatus();
		GuiCombo cmbStatus = (GuiCombo)projektWindow.getGuiComponent("status");
		cmbStatus.setItems(dsStatus);
	}
	private void comboRollen() {
		GuiTable tblPlanung = (GuiTable)projektWindow.getGuiComponent("tabPlanung.tblPlanung");
		tblPlanung.setItems("rolle", srv.getRollen());
		
		GuiTable tblMA = (GuiTable)projektWindow.getGuiComponent("tabMitarbeiter.tblMitarbeiter");
		tblMA.setItems("rolle", srv.getRollen());
	}
	void comboMitarbeiter() {		
		GuiTable tblMA = (GuiTable)projektWindow.getGuiComponent("tabMitarbeiter.tblMitarbeiter");
		tblMA.setItems("mitarbeiter", srv.getMitarbeiter());
	}
	private void comboKostenarten() {
		// Kostenarten
		GuiTable tblFremdkosten = (GuiTable)projektWindow.getGuiComponent("tabSachkosten.tblSachkosten");
		tblFremdkosten.setItems("kostenart", srv.getKostenarten());
	}
	// USER ACTIONS ======================
	/**
	 * Toolbar/Menu "Neu"
	 */
	public void projektNew(GuiUserEvent event) {
		projektWindow.reset();
		JDataSet ds = srv.getLeeresProjekt();
		projektWindow.setDatasetValues(ds);
		this.setStatus(NEU);
	}
	/**
	 * Toolbar/Menu "Öffnen"
	 * @param event
	 */
	public void projektOpen(GuiUserEvent event) {
		try {
			GuiDialog dia = (GuiDialog)GuiFactory.getInstance().createWindow("example/Projekt/ProjektAuswahl.xml");
			JDataSet dsProjekte = srv.getProjektAuswahl();
			dia.reset();
			//System.out.println(dsProjekte.getXml());
			dia.setDatasetValues(dsProjekte);
			if (dia.showDialog()) {
				GuiTable tbl = dia.getRootPane().getCurrentTable();
				long id = Convert.toLong( tbl.getCellValue(0) );
				JDataSet ds = srv.getProjekt(id);
				
				projektWindow.reset();
				projektWindow.setDatasetValues(ds);
				this.calcIst(ds);
				this.calcFremd(null);
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
	public void projektSave(GuiUserEvent event) {
		JDataSet ds = projektWindow.getDatasetValues();
		if (ds.hasChanges()) {
			JDataSet dsChanges = ds.getChanges();
			//System.out.println(dsChanges.getXml());
			try {
				projektWindow.verify();
				srv.setProjekt(dsChanges);
				ds.commitChanges();
				projektWindow.commitChanges();
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
	public void projektRemove(GuiUserEvent event) {
		if (GuiUtil.yesNoMessage(this.projektWindow, "Projekt Löschen", "Soll das Projekt gelöscht werden?")) {
			JDataSet ds = this.projektWindow.getDatasetValues();
			ds.setDeleted(true);
			srv.setProjekt(ds);
			this.projektWindow.reset();
			this.setStatus(LEER);
		}			
	}	
	/**
	 * Budget berechnen
	 * @param event
	 */
	public void calcBudget(GuiUserEvent event) {
		GuiTable tbl = event.window.getRootPane().getMainPanel().getGuiTable("tabPlanung.tblPlanung");
		GuiChangeEvent e = (GuiChangeEvent)event;
		{
			int index = e.row;
			index = tbl.getSelectedRow();
			GuiTableRow row = tbl.getRow(index);
	
			BigDecimal k = Convert.toBigDecimal(row.getValue("kapazitaeten"));
			if (k == null) {
				k = new BigDecimal(0);
			}
			BigDecimal b = Convert.toBigDecimal(row.getValue("budget"));
			if (b == null) {
				b = new BigDecimal(0);
			}
			BigDecimal a = Convert.toBigDecimal(row.getValue("aufwand"));
			if (a == null) {
				a = new BigDecimal(0);
			}
			BigDecimal t = Convert.toBigDecimal(row.getValue("tagessatz"));
			if (t == null) {
				t = new BigDecimal(0);
			}
			if (b.doubleValue() != 0 && a.doubleValue() == 0 && t.doubleValue() != 0) {
				a = b.divide(t, BigDecimal.ROUND_DOWN);
				row.setValue("aufwand", a);
			} else if (b.doubleValue() != 0 && a.doubleValue() != 0 && t.doubleValue() == 0) {
				t = b.divide(a, BigDecimal.ROUND_DOWN);
				row.setValue("tagessatz", t);
			} else if (b.doubleValue() == 0 && a.doubleValue() != 0 && t.doubleValue() != 0) {
				b = a.multiply(t);
				row.setValue("budget", b);
			} else if (b.doubleValue() != 0 && a.doubleValue() != 0 && t.doubleValue() != 0) {
				// Bis jetzt haben wir NULL-Werte neu berechnet; jetzt errechnen wir den Tagesatz
				// oder den Aufwand neu
				String name = event.member.getName();
				if (name.equals("budget")) {
					t = b.divide(a, BigDecimal.ROUND_DOWN);
					row.setValue("tagessatz", t);
				} else if (name.equals("aufwand")) {
					b = a.multiply(t);
					row.setValue("budget", b);
				} else if (name.equals("tagessatz")) {
					b = a.multiply(t);
					row.setValue("budget", b);
				}
			}
		}
		// Summieren
		{
			BigDecimal sb = new BigDecimal(0);
			BigDecimal sa = new BigDecimal(0);
			BigDecimal st = new BigDecimal(0);
			for (int i = 0; i < tbl.getRowCount(); i++) {
				GuiTableRow row = tbl.getRow(i);
				BigDecimal k = Convert.toBigDecimal(row.getValue("kapazitaeten"));
				BigDecimal b = Convert.toBigDecimal(row.getValue("budget"));
				if (b != null) {
					sb = sb.add(b);
				}
				BigDecimal a = Convert.toBigDecimal(row.getValue("aufwand"));
				if (a != null) {
					sa = sa.add(a);
				}
			}
			GuiWindow win = event.window;
			GuiNumber numBP = (GuiNumber)win.getGuiComponent("budgetPlan");
			numBP.setValue(sb);
			GuiNumber numAP = (GuiNumber)win.getGuiComponent("aufwandPlan");
			numAP.setValue(sa);
			st = sb.divide(sa, BigDecimal.ROUND_DOWN);
			GuiNumber numTP = (GuiNumber)win.getGuiComponent("tagessatzPlan");
			numTP.setValue(st);
			this.calcOffen();
		}
	}
	public void calcFremd(GuiUserEvent event) {
		GuiTable tbl = projektWindow.getRootPane().getMainPanel().getGuiTable("tabSachkosten.tblSachkosten");
		// Summieren
		{
			BigDecimal sPlan = new BigDecimal(0);
			BigDecimal sIst = new BigDecimal(0);
			for (int i = 0; i < tbl.getRowCount(); i++) {
				GuiTableRow row = tbl.getRow(i);
				BigDecimal plan = Convert.toBigDecimal(row.getValue("plan"));
				if (plan != null) {
					sPlan = sPlan.add(plan);
				}
				BigDecimal ist = Convert.toBigDecimal(row.getValue("ist"));
				if (ist != null) {
					sIst = sIst.add(ist);
				}
			}
			GuiWindow win = projektWindow;
			GuiMoney numPlan = (GuiMoney)win.getGuiComponent("fremdPlan");
			numPlan.setValue(sPlan);
			GuiMoney numIst = (GuiMoney)win.getGuiComponent("fremdIst");
			numIst.setValue(sIst);
			GuiMoney numOffen = (GuiMoney)win.getGuiComponent("fremdOffen");
			BigDecimal sOffen = new BigDecimal(sPlan.doubleValue());
			sOffen = sOffen.subtract(sIst);
			numOffen.setValue(sOffen);
		}
		
	}
	public void menuMitarbeiter(GuiUserEvent event) {
		try {
			ControlMitarbeiter cm = new ControlMitarbeiter();
			cm.show();
		} catch (Exception ex) {
			GuiUtil.showEx(ex);
		}
	}
	public void menuRollen(GuiUserEvent event) {
		try {
			GuiDialog dia = (GuiDialog)GuiFactory.getInstance().createWindow("example/Projekt/Rollen.xml");
			JDataSet dsRead = srv.getRollen();
			dia.setDatasetValues(dsRead);
			if (dia.showDialog()) {
				JDataSet dsWrite = dia.getDatasetValues();
				if (dsWrite.hasChanges()) {
					JDataSet dsChanges = dsWrite.getChanges();
					srv.setRollen(dsChanges);
					this.comboRollen();
				}
			}
		} catch (Exception ex) {
			GuiUtil.showEx(ex);
		}
	}
	public void menuStatus(GuiUserEvent event) {
		try {
			GuiDialog dia = (GuiDialog)GuiFactory.getInstance().createWindow("example/Projekt/Status.xml");
			JDataSet dsRead = srv.getProjektStatus();
			dia.setDatasetValues(dsRead);
			if (dia.showDialog()) {
				JDataSet dsWrite = dia.getDatasetValues();
				if (dsWrite.hasChanges()) {
					JDataSet dsChanges = dsWrite.getChanges();
					srv.setProjektStatus(dsChanges);
					this.comboStatus();
				}
			}
		} catch (Exception ex) {
			GuiUtil.showEx(ex);
		}
	}
	public void menuKostenart(GuiUserEvent event) {
		try {
			GuiDialog dia = (GuiDialog)GuiFactory.getInstance().createWindow("example/Projekt/Kostenart.xml");
			JDataSet dsRead = srv.getKostenarten();
			dia.setDatasetValues(dsRead);
			if (dia.showDialog()) {
				JDataSet dsWrite = dia.getDatasetValues();
				if (dsWrite.hasChanges()) {
					JDataSet dsChanges = dsWrite.getChanges();
					srv.setKostenarten(dsChanges);
					this.comboKostenarten();
				}
			}
		} catch (Exception ex) {
			GuiUtil.showEx(ex);
		}
	}
	// ============ private ===================================
	private void setStatus(int status) {
		this.status = status;
		switch (status) {
			case NEU:
				projektWindow.getAction("iSave").setEnabled(true);
				projektWindow.getAction("iDelete").setEnabled(false);
				projektWindow.getAction("tSave").setEnabled(true);
				break;
			case ALT:
				projektWindow.getAction("iSave").setEnabled(true);
				projektWindow.getAction("iDelete").setEnabled(true);
				projektWindow.getAction("tSave").setEnabled(true);
				break;
			case LEER:
				projektWindow.getAction("iSave").setEnabled(false);
				projektWindow.getAction("iDelete").setEnabled(false);
				projektWindow.getAction("tSave").setEnabled(false);
				break;
		}
	}
	private void calcIst(JDataSet ds) {
		Iterator it = ds.getRow().getChildRows("ProjektMitarbeiter");
		BigDecimal istBudget = new BigDecimal(0);
		BigDecimal istAufwand = new BigDecimal(0);
		if (it != null) {
			while (it.hasNext()) {
				JDataRow pm = (JDataRow)it.next();
				JDataValue val = pm.getDataValue("Tagessatz");
				double summeStunden = 0;
				double tagessatz = val.getValueDouble();
				Iterator ib = pm.getChildRows("Buchung");
				if (ib != null) {
					while (ib.hasNext()) {
						JDataRow buch = (JDataRow)ib.next();
						JDataValue vStunden = buch.getDataValue("AufwandStunden");
						double std = vStunden.getValueDouble();
						summeStunden = summeStunden + std;
					}
				}
				BigDecimal umsatz = new BigDecimal(summeStunden);
				umsatz = umsatz.setScale(2);
				umsatz = umsatz.divide(new BigDecimal(ARBEITS_STUNDEN), BigDecimal.ROUND_HALF_DOWN);
				istAufwand = istAufwand.add(umsatz);
				umsatz = umsatz.multiply(new BigDecimal(tagessatz));
				istBudget = istBudget.add(umsatz);
			}
		}
		GuiNumber numB = (GuiNumber)projektWindow.getGuiComponent("istBudget");
		numB.setValue(istBudget);
		GuiNumber numA = (GuiNumber)projektWindow.getGuiComponent("istAufwand");
		numA.setValue(istAufwand);
		BigDecimal istSatz = new BigDecimal(0);
		try {
			istSatz = istBudget.divide(istAufwand, BigDecimal.ROUND_HALF_DOWN);
		} catch (Exception ex) {}
		GuiNumber numT = (GuiNumber)projektWindow.getGuiComponent("istTagessatz");
		numT.setValue(istSatz);
		this.calcOffen();
	}
	private void calcOffen() {
		double bp  = Convert.toDouble(projektWindow.getValue("budgetPlan").toString());
		double ap  = Convert.toDouble(projektWindow.getValue("aufwandPlan").toString());
		double bi  = Convert.toDouble(projektWindow.getValue("istBudget").toString());
		double ai  = Convert.toDouble(projektWindow.getValue("istAufwand").toString());
		double ob = bp - bi;
		double oa = ap - ai;
		GuiNumber numB = (GuiNumber)projektWindow.getGuiComponent("offenBudget");
		numB.setValue(ob);
		GuiNumber numA = (GuiNumber)projektWindow.getGuiComponent("offenAufwand");
		numA.setValue(oa);
		double satz = 0;
		if (oa != 0)
			satz = ob / oa;
		GuiNumber numT = (GuiNumber)projektWindow.getGuiComponent("offenTagessatz");
		numT.setValue(satz);
		
	}
}
