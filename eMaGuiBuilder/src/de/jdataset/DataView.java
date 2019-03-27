/*
 * Created on 22.02.2004
 */
package de.jdataset;
import java.util.*;
/**
 * Objekte dieser Klasse dienen dem Filtern und Sortieren
 * von DataRows
 * @see JDataSet#getChildRows(DataView)
 * @see JDataRow#getChildRows(DataView)
 */
public class DataView {
   /**
    * Filtert die Zeilen, bei denen isInserted() auf 'true' steht.
    */
	public static final int INSERTED = 1;
   /**
    * Filtert die Zeilen, bei denen isDeleted() auf 'true' steht.
    */
	public static final int DELETED = 2;
   /**
    * Filtert die Zeilen, bei denen isModified) auf 'true' steht.
    */
	public static final int MODIFIED = 4;
	/**
	 * Filtert die Zeilen, die weder inserted, deleted oder modified sind.
	 */
	public static final int UNCHANGED = 8;
	private String refName;
	private NVPair filter;
	private String columnToSort;
	private boolean ascending = true;
	private int rowState;
	// Constructor
	/**
	 * Erzeugt einen DataView für die Zeilen, die einen
	 * bestimmten Zustand haben (INSERTED, MODIFIED, ...).<p>
	 * Diese Zustände können auch mit OR verknüpft werden
	 * (3 = INSERTED OR DELETED).
	 * @see JDataSet#getChildRows(DataView)
	 * @see JDataRow#getChildRows(DataView)
	 */
	public DataView(int rowState) {
		this.setRowState(rowState);
	}
	/**
	 * Erzeugt einen DataView, bei nach der in NVPair
	 * übergebenen Spalte und deren Wert gefiltert sind.
	 * @param refname
	 * @param filter
	 */
	public DataView(String refname, NVPair filter) {
		this.refName = refname;
		this.filter = filter;
	}
	/**
	 * Erzeugt einen DataView, der nach der angegenenen
	 * Spalte sortiert ist.
	 * @param refname
	 * @param columnName
	 */
	public DataView(String refname, String columnName) {
		this.refName = refname;
		this.columnToSort = columnName;
	}
	/**
	 * @return Returns the ascending.
	 */
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * 'true' = Sort ascending (= default);
	 * 'false' = Sort descending.
	 * @param ascending The ascending to set.
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	/**
	 * @return Returns the columnToSort.
	 */
	public String getColumnToSort() {
		return columnToSort;
	}

	/**
	 * @param columnToSort The columnToSort to set.
	 */
	public void setColumnToSort(String columnToSort) {
		this.columnToSort = columnToSort;
	}

	/**
	 * @return Returns the filter.
	 */
	public NVPair getFilter() {
		return filter;
	}

	/**
	 * @param filter The filter to set.
	 */
	public void setFilter(NVPair filter) {
		this.filter = filter;
	}

	/**
	 * @return Returns the rowState.
	 */
	public int getRowState() {
		return rowState;
	}

	/**
	 * Erlaubte Werte sind 1 bis 8 oder 0 für alle Rows.
	 * @param rowState The rowState to set.
	 */
	public void setRowState(int rowState) {
		if (rowState < 0 || rowState > 8) {
			throw new IllegalArgumentException("RowState out of Range");
		}
		this.rowState = rowState;
	}

	/**
	 * @return Returns the refName.
	 */
	public String getRefName() {
		return refName;
	}

	/**
	 * @param refName The refName to set.
	 */
	public void setRefName(String refName) {
		this.refName = refName;
	}
	ArrayList<JDataRow> getChildRowList(RowContainer cont) {
		ArrayList<JDataRow> al = new ArrayList<JDataRow>();
		Iterator<JDataRow> it = null;
		if (refName == null) {
			it = cont.getChildRows();
		} else {
			it = cont.getChildRows(refName);
		}
		if (it == null) {
			return null;
		}
		// Filter
		while (it.hasNext()) {
			JDataRow row = it.next();
			boolean toAdd = false;
			if (filter != null) {
				String value = row.getValue(filter.getName());
				if (value != null && value.equals(filter.getValue())) {
					toAdd = true;
				} else {
					toAdd = false;
				}
			} else {
				toAdd = true;
			}
			if (toAdd == true && rowState != 0) {
				int state = rowState & row.getRowState();
				if (state == 0) {
					toAdd = false;
				}
			}
			if (toAdd) {
				al.add(row);
			}
		}
		// Sorter
		if (this.getColumnToSort() != null) {
			JDataTable childTbl = null;
			if (cont instanceof JDataSet) {
				if (refName != null) {
					childTbl = ((JDataSet)cont).getDataTable(refName);
				} else {
					childTbl = cont.getDataTable();
				}
			} else { // JDataRow
				if (refName != null) {
					childTbl = cont.getDataTable().getChildTable(refName);
				} else {
					throw new IllegalArgumentException("Column to Sort: Missing Child Reference Name");
				}
			}
			if (childTbl != null) {
				JDataColumn col = childTbl.getDataColumn(this.getColumnToSort());
				RowSorter rs = new RowSorter(col.getColumIndex(), this.isAscending());
				Collections.sort(al, rs);
			}
		}
		return al;
	}
}
