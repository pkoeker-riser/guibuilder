/*
 * Created on 22.02.2004
 * TODO : Alles auf UnmodifiableCollections umstellen.
 */
package de.jdataset;

import java.util.*;
/**
 * Gemeinsame Methoden von JDataSet und JDataRow
 */
public interface RowContainer {
	/**
	 * Fügt eine ChildRow hinzu.<p>
	 * Es dürfen nur Rows von den Tabellen hinzugefügt werden,
	 * die auch als Child-Table aufgeführt sind.
	 * Ansonsten wird eine IllegalArgumentException geworfen.
	 * @param row
	 */
	public void addChildRow(JDataRow row);
	/**
	 * Fügt eine ChildRow an dem entsprechenden Index ein.<p>
	 * Wirft eine IllegalArgumentException, wenn der Index außerhalb
	 * des definierten Bereichs ist.
	 */
	public void addChildRow(JDataRow row, int index);
	/**
	 * Fügt eine neue leere Child Row hinzu.<p>
	 * Wirft eine IllegalStateException, wenn nicht genau eine
	 * ChildTable definiert ist.
	 */
	public JDataRow createChildRow();
	public JDataRow createChildRow(String childRefName);
	/**
	 * @param childRefName
	 * @param index
	 * @return
	 */
	public JDataRow getChildRow(String childRefName, int index);
	public Iterator<JDataRow> getChildRows();
	public Iterator<JDataRow> getChildRows(String childRefName);
	/**
	 * Liefert eine unmodifiable Collection von DataRows,
	 * die durch den DataView entsprechend gefiltert und/oder sortiert sind.
	 * @param view
	 * @return
	 */
	public Collection getChildRows(DataView view);
	public boolean hasChanges();
	public void commitChanges();
	public JDataTable getDataTable();
}
