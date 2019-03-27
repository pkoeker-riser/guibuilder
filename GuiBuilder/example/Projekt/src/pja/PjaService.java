/*
 * Created on 18.10.2003
 */
package de.pkjs.pja;

import de.pkjs.pl.*;
import de.pkjs.util.*;
import de.jdataset.*;

import java.util.*;

/**
 * Dienste für PJA
 * @author peter
 */
public class PjaService {
	private static PjaService me;
	private IPL pl;
	/**
	 * private Constructor
	 */
	private PjaService() {
		try {
			pl = new PL();
			System.out.println(pl.getDatabaseMetaData());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public static PjaService getInstance() {
		if (me == null) {
			me = new PjaService();
		}
		return me;
	}
	// Dienste **********************************
	public JDataSet getRollen() {
		JDataSet ds = pl.getAll("Rolle");
		return ds;
	}
	public int setRollen(JDataSet ds) throws Exception {
		int cnt = pl.setDataset(ds);
		return cnt;
	}
	public JDataSet getProjektStatus() {
		JDataSet ds = pl.getAll("ProjektStatus");
		return ds;
	}
	public int setProjektStatus(JDataSet ds) throws Exception {
		int cnt = pl.setDataset(ds);
		return cnt;
	}
	public JDataSet getKostenarten() {
		JDataSet ds = pl.getAll("Kostenart");
		return ds;
	}
	public int setKostenarten(JDataSet ds) throws Exception {
		int cnt = pl.setDataset(ds);
		return cnt;
	}
	public JDataSet getMitarbeiter() {
		JDataSet ds = pl.getAll("Mitarbeiter");
		return ds;
	}
	public JDataSet getProjektAuswahl() {
		JDataSet ds = pl.getAll("ProjektAuswahl");
		return ds;
	}
	public JDataSet getProjekte() {
		JDataSet ds = pl.getAll("Projekte");
		return ds;
	}
	public long getNewId() {
		return pl.getOID();
	}
	/**
	 * Liefert ein leeres Projekt
	 * @return
	 */
	public JDataSet getLeeresProjekt() {
		JDataSet ds = pl.getEmptyDataset("Projekt"); 
		JDataRow row = ds.addRow();
		// PK vergeben
		long id = pl.getOID();
		row.setValue("Projekt_Id", id);
		return ds;
	}
	/**
	 * Ein Projekt einlesen
	 * @param projektId
	 * @return
	 */
	public JDataSet getProjekt(long projektId) {
		JDataSet ds = pl.getDataset("Projekt", projektId);
		return ds;
	}
	/**
	 * Schreibt ein Projekt wieder zurück in die Datenbank
	 * @param ds
	 */
	public void setProjekt(JDataSet ds) {
		try {
			pl.setDataset(ds);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	// =========== Mitarbeiter ========================
	/**
	 * Liefert einen leeren Mitarbeiter
	 * @return
	 */
	public JDataSet getLeerenMitarbeiter() {
		JDataSet ds = pl.getEmptyDataset("MitarbeiterPflege"); 
		JDataRow row = ds.addRow();
		// PK vergeben
		long id = pl.getOID();
		row.setValue("Mitarbeiter_Id", id);
		return ds;
	}
	/**
	 * Einen Mitarbeiter einlesen
	 * @param maId
	 * @return
	 */
	public JDataSet getMitarbeiter(long maId) {
		JDataSet ds = pl.getDataset("MitarbeiterPflege", maId);
		return ds;
	}
	/**
	 * Schreibt einen Mitarbeiter wieder zurück in die Datenbank
	 * @param ds
	 */
	public void setMitarbeiter(JDataSet ds) {
		try {
			pl.setDataset(ds);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Liefert alle Mitarbeiter
	 * @return
	 */
	public JDataSet getMitarbeiterAuswahl() {
		JDataSet ds = pl.getAll("MitarbeiterAuswahl");
		System.out.println(ds.getXml());
		return ds;
	}
	/**
	 * Liefert die Buchungen eines Mitarbeiters zu einem Projekt innerhalb
	 * eines bestimmten Datum-Intervals.
	 * @param mitarbeiter_id
	 * @param projekt_id
	 * @param vonDatum
	 * @param bisDatum
	 * @return
	 */
	public JDataSet getMitarbeiterBuchungen(long projekt_mitarbeiter_id, Date vonDatum, Date bisDatum) {
		ParameterList list = new ParameterList();
		list.addParameter("projekt_mitarbeiter_id", Convert.toString(projekt_mitarbeiter_id));
		list.addParameter("von_datum", Convert.toString(vonDatum));
		list.addParameter("bis_datum", Convert.toString(bisDatum));
		JDataSet ds = pl.getDataset("MitarbeiterBuchungen", list);
		return ds;
	}
}
