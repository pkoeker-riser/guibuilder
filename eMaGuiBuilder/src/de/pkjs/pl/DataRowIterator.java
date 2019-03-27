/*
 * Created on 09.08.2004
 */
package de.pkjs.pl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.ArrayList;
import java.util.Iterator;


import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataValue;
import de.jdataset.ParameterList;

/**
 * Über die Methode PL#getDataRowIterator wird ein Objekt dieser Klasse geliefert.
 * Dieser Iterator liest die Ergebnismenge zeilenweise durch. 
 * Dadurch können auch große Datenmengen analysiert werden.
 * <code>
 * IPL pl = ...
 * DataRowIterator dri = pl.getDataRowIterator("MyData", "SELECT * FROM MyData", null);
 * while (dri.hasNext()) {
 *    JDataRow = dri.nextRow();
 *    ...
 * }
 * </code>
 * @author PKOEKER
 */
public class DataRowIterator implements Iterator<JDataRow> {
	private JDataSet dataset;
	private PreparedStatement ps;
	private ResultSet rs;
	private boolean hasNext;
	private int cnt = 0;
	
	DataRowIterator(JDataSet ds, PreparedStatement ps, ResultSet rs, ParameterList list) {
		this.dataset = ds;
		this.ps = ps;
		this.rs = rs;
		this.hasNext = true;
		this.cnt = 0;
	}
	/**
	 * Achtung!<br>
	 * Dieser Dataset enthält nur die Metadaten, aber keine einzige DataRow!
	 * @return
	 */
	public JDataSet getDataset() {
		return this.dataset;
	}
	/**
	 * Not implemented
	 */
	public void remove() {
		throw new IllegalStateException("Illegal access");
	}

	/**
	 * Liefert true, solange noch Rows verfügbar sind.
	 */
	public boolean hasNext() {
	   try {
	      this.hasNext = rs.next();
	      if (this.hasNext == false) {
					rs.close();
					ps.close();
	      }
	      return this.hasNext;
	   } catch (SQLException ex) {
	       return false;
	   }
	}

	/**
	 * Liefert eine JDataRow, solange der Vorrat reicht.
	 */
	public JDataRow next() {
		JDataRow row = null;
		try {
			if (this.hasNext) {
					// Eine Row aus dem ResultSet wird in eine
					// ArrayList von JDataValues verwandelt.
					ArrayList<JDataValue> al = PLTransactionContext.rs2ArrayList(rs, rs.getMetaData(), dataset.getDataTable());
					// Row erzeugen
					row = new JDataRow(dataset.getDataTable(), al);
					cnt++;
					return row;
			} else {
				return null;
			}
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage());
		}
	}
	/**
	 * @see #next
	 * @return
	 */
	public JDataRow nextRow() {
		JDataRow row = this.next();
		return row;
	}
	/**
	 * Liefert die Anzahl der bisher mit next() gelesenen DataRows.
	 * @return
	 */
	public int getRowCounter() {
		return cnt;
	}
	
	public void close() throws SQLException {
    if (rs != null) 
      rs.close();
    if (ps != null) 
      ps.close();
	}
	
	@Override
	protected void finalize() throws Throwable {
	  super.finalize();
	  this.close();
	}
}
