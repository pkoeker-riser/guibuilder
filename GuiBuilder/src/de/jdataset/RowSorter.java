/*
 * Created on 21.02.2004
 */
package de.jdataset;

import java.text.*;
import java.util.*;
import java.sql.Types;
/**
 * @author peter
 */
final class RowSorter implements Comparator  {
	// Attributes
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private SimpleDateFormat timestampFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private NumberFormat numFormat = NumberFormat.getInstance();
	private int colIndex;
	private boolean ascending;
	// Constructor
	RowSorter(int colIndex, boolean ascending) {
		this.colIndex = colIndex;
		this.ascending = ascending;
	}
	// Methods
	/**
	 * From Comparator
	 */
	public int compare(Object a, Object b) {
		JDataRow r1 = (JDataRow)a;
		JDataRow r2 = (JDataRow)b;
		JDataValue v1 = r1.getDataValue(colIndex);
		JDataValue v2 = r2.getDataValue(colIndex);
		int datatype = v1.getDataType();
		Object o1 = v1.getValue();
		Object o2 = v2.getValue();
		String s1 = v1.getValue();
		String s2 = v2.getValue();
		
		// Treat empty strains like nulls
		if (s1 != null && s1.length() == 0) {
			o1 = null;
		}
		if (s2 != null && s2.length() == 0) {
			o2 = null;
		}

		// Berücksichtigung von Null-Werten je nach Sortierrichtung
		// Datenbanken sortien die Null-Werte u.U. andresherum!?
		if (o1 == null && o2 == null) {
			return 0; // Beides null
		} else if (o1 == null) { // erster Wert null
			if (ascending)
				return -1;
				else
					return 1;
		} else if (o2 == null) { // zweiter Wert null
			if (ascending)
				return 1;
				else
					return -1;
		}
		switch(datatype) {
			case Types.DATE:
				try {
					o1 = dateFormat.parse(s1);
					o2 = dateFormat.parse(s2);
				} catch (Exception ex) {}
				break;
			case Types.TIME:
				try {
					o1 = timeFormat.parse(s1);
					o2 = timeFormat.parse(s2);
				} catch (Exception ex) {}
				break;
			case Types.TIMESTAMP:
				try {
					o1 = timestampFormat.parse(s1);
					o2 = timestampFormat.parse(s2);
				} catch (Exception ex) {}
				break;
			case Types.BIGINT:
			case Types.INTEGER:
			case Types.DECIMAL:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.NUMERIC:
			case Types.REAL:
			case Types.TINYINT:
				try {
					o1 = numFormat.parse(s1);
					o2 = numFormat.parse(s2);
				} catch (Exception ex) {}
				break;
		} // End Switch
		if (ascending && o1 instanceof Comparable) {
			return ((Comparable)o1).compareTo(o2);
		} else if (ascending == false && o2 instanceof Comparable) {
			return ((Comparable)o2).compareTo(o1);
		} else {
			if (ascending) {
				return o1.toString().compareTo(o2.toString());
			} else {
				return o2.toString().compareTo(o1.toString());
			}
		}
	}
}
	
