/*
 * Created on 02.04.2003
 */
package de.pkjs.pl;

import java.io.Serializable;
import java.sql.Types;

import de.pkjs.util.Convert;

/**
 * @author peter
 */
final class SqlCondition implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String AND = " AND ";
	// public static final String OR = " OR ";
	public static final String EQUALS = " = ";
	public static final String NOTEQUALS = " != ";
	// public static final String LIKE = " LIKE ";
	public static final String IN = " IN (";
	public static final String IS_NULL = " IS NULL "; // 27.8.2004 PKÖ
	// public static final String ISNOT_NULL = " IS NOT NULL "; // 27.8.2004 PKÖ

	private String name;
	private int dataType = Types.VARCHAR;
	private String compare;
	private String value;
	private long[] oids;
	private boolean isSimple;
	private boolean join; // Für ... JOIN ...

	// Constructor
	/**
	 * Konstante Bedingung 1=0
	 */
	SqlCondition(String name) {
		this.name = name;
		this.isSimple = true;
	}

	/**
	 * AND x = ?
	 */
	SqlCondition(String name, int dataType, boolean join) {
		this.name = name;
		this.dataType = dataType;
		this.join = join;
		this.compare = EQUALS;
	}

	/**
	 * ... AND x = ? ... ... AND y IS ? ...
	 */
	SqlCondition(String name, String value, int dataType) {
		this.name = name;
		if (value == null) {
			this.compare = IS_NULL; // 27.8.2004 / PKÖ
		} else {
			this.compare = EQUALS;
		}
		this.value = value;
		this.dataType = dataType;
	}

	SqlCondition(String name, long[] oids) {
		this.name = name;
		this.compare = IN;
		this.oids = oids;
		this.dataType = Types.BIGINT;
	}

	// Methods
	/**
	 * @deprecated unused Stellt fest, ob das die gleiche Condition ist; mit
	 *             Ausnahme des Vergleichswerts!
	 */
	boolean equals(SqlCondition oc) {
		if (oc.isSimple()) {
			if (this.isSimple() && oc.getName().equals(this.getName())) {
				return true;
			} else {
				return false;
			}
		} else { // nix simple
			if (oc.getName().equals(this.getName()) && oc.getDataType() == this.getDataType() && oc.getCompare().equals(this.getCompare())) {
				return true;
			} else {
				return false;
			}
		}
	}

	String getSql(boolean firstTime) {
		if (isSimple) {
			return " WHERE " + name;
		}
		StringBuilder buff = new StringBuilder();
		if (firstTime) {
			buff.append(" WHERE ");
		} else {
			buff.append(AND);
		}
		buff.append(this.name);
		buff.append(this.compare);
		if (this.compare.equals(IS_NULL) == false) { // 27.8.2004 / PKÖ
			buff.append('?'); // Wenn "IS_NULL", dann keinen Parameter angeben
		} 
		if (this.compare.equals(IN)) {
			for (int i = 1; i < oids.length; i++) { // Schleife ab 1 statt 0: ein ? ist schon dran 
				buff.append(",?");
			}
			buff.append(')');
		}
		return buff.toString();
	}

	int getDataType() {
		return this.dataType;
	}

	Object getValue() {
		if (oids != null) {
			return oids;
		} else {
			Object oVal = Convert.toObject(value, dataType);
			return oVal;
		}
	}

	void setValue(String val) {
		this.value = val;
	}

	/**
	 * @return
	 */
	String getCompare() {
		return compare;
	}

	/**
	 * @return
	 */
	boolean isSimple() {
		return isSimple;
	}

	/**
	 * @return
	 */
	String getName() {
		return name;
	}

	boolean isJoin() {
		return this.join;
	}

	public String toString() {
		String s = this.getSql(false);
		return s;
	}
}
