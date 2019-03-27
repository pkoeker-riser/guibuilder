/*
 * Created on 21.06.2003
 */
package de.pkjs.pl;

import java.io.Serializable;

/**
 * Ein Named Parameter in einem SQL-Statement
 * ... WHERE oid = $kundeId ...
 */
final class NamedParameter implements Serializable {
	private static final long serialVersionUID = 1L;
	// Attributes
	private String name;
	private int index;
	private int dataType;
	// Constructor
	NamedParameter(String name, int index, int dataType) {
		this.name = name;
		this.index = index;
		this.dataType = dataType;
	}
	// Methods
	String getName() {
		return this.name;
	}
	int getIndex() {
		return this.index;
	}
	int getDataType() {
		return this.dataType;
	}
}
