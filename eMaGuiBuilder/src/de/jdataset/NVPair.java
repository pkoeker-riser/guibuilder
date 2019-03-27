/*
 * Created on 21.06.2003
 */
package de.jdataset;

import java.io.Serializable;
import java.util.List;

/**
 * Ein Name-Value-Pair für parametrisierte Abfragen.
 * @see ParameterList
 */
public final class NVPair implements Serializable {
	// Attributes
	private String name;
	private Object value;
	private int dataType = -9999;
	
  static final long serialVersionUID = 7921919178523824194L;
	// Constructor
	/**
	 * @deprecated
	 * For serialization purpose only.
	 */
	public NVPair() {}
	/**
	 * Erzeugt ein NVPair unter Angabe von Namen, Wert und Datentyp.
	 * Ein Datentyp muß bei Date, Time usw immer mit angegeben werden,
	 * sonst wird eine entsprechende Abfrage nicht das gewünschte Ergebnis liefern.
	 * @param name
	 * @param value
	 * @param dataType see java.sql.Types
	 */
	public NVPair(String name, Object value, int dataType) {
		this.name = name;
		this.value = value;
		this.dataType = dataType;
	}
	/**
	 * Aus IN(?) wird IN(?,?,...) je nach Anzahl der Elemente in der List
	 * @param name
	 * @param value
	 * @param dataType
	 */
  public NVPair(String name, List<?> value, int dataType) {
    this.name = name;
    this.value = value;
    this.dataType = dataType;
  }
	public String getName() {
		return name;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException("NVPair: Null Value not allowed");
		}
		this.value = value;
	}
	public int getDataType() {
		return dataType;
	}
	/**
	 * java.sql.Types
	 * @param i
	 */
	public void setDataType(int i) {
		dataType = i;
	}
	public String toString() {
      if (value == null) return name + "=NULL";
      else return name + "=" + value.toString();
	}
}
