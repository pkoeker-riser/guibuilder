/*
 * Created on 14.12.2003
 */
package de.jdataset;

import java.io.Serializable;

import java.sql.DatabaseMetaData;
import java.util.*;

/**
 * Diese Klasse stellt eine Relation zwischen zwei Tabellen dar; wie im
 * ER-Modell. Die Namen der Relationen einer Tabelle müssen eindeutig sein.
 */
public final class Relation implements Serializable {
	private String refName;

	private JDataTable parentTable;

	private JDataTable childTable;

	private ArrayList<JDataColumn> childColumns = new ArrayList<JDataColumn>();

	// Nur für Child Requests
	private int update_rule = DatabaseMetaData.importedKeyNoAction; 
	private int delete_rule = DatabaseMetaData.importedKeyCascade;
	private String fk; // Für den Fall, daß es die Columns nicht gibt :-(

	static final long serialVersionUID = 4776473895806227190L;

	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(Relation.class);

	// Constructor
	/**
	 * @deprecated For serialization purpose only.
	 */
	public Relation() {}

	Relation(String name, JDataTable parent, JDataTable child, String fk) {
	   if (name == null || name.length() == 0 || parent == null || child == null) {
			throw new IllegalArgumentException("DataRelation: Null Arguments");
		}
	   if (child.isVirtualChild()== false) {
			if (fk == null || fk.length() == 0) {
				throw new IllegalArgumentException("DataRelation '"+name+"': Missing Foreign Key");
			}
		}
		this.refName = name;
		this.parentTable = parent;
		this.childTable = child;
		this.fk = fk;
		StringTokenizer toks = new StringTokenizer(fk, ",");
        boolean foreignKeyOK = true;
		while (toks.hasMoreTokens()) {
			String tok = toks.nextToken();
			try {
				JDataColumn col = child.getDataColumn(tok);
				childColumns.add(col);
				col.setForeignKey(true);
			} catch (Exception ex) {
                foreignKeyOK = false;
				logger.warn("Warning! DataRelation '"+name+"': Missing Foreign Key Column: "
								+ childTable.getTablename() + "." + tok
								+ " (Parent Table: " + parent.getTablename() +")");
			}
		}
		if (foreignKeyOK == true && child.isVirtualChild() == false && parent.getPKColumnsCount() != childColumns.size()) {
			logger.warn("Warning! DataRelation '"+name+"': Number of Primary Key Columns ("
							+ parent.getTablename()
							+ ") != Number of Foreign Key Columns ("
							+ child.getTablename() + ")");
		}
		boolean childAdded = false;
		try {
			this.childTable.addParentRelation(this);
			childAdded = true;
			this.parentTable.addChildRelation(this);
		} catch (IllegalArgumentException ex) {
			if (childAdded == true) {
				// TODO : remove child
				logger.error("Error addRelation", ex);
			}
			throw ex;
		}
	}

	/**
	 * @return Returns the childTable.
	 */
	public JDataTable getChildTable() {
		return childTable;
	}

	/**
	 * Unused
	 * 
	 * @return Returns the parentTable.
	 */
	public JDataTable getParentTable() {
		return parentTable;
	}

	/**
	 * @return Returns the refName.
	 */
	public String getRefName() {
		return refName;
	}

	/**
	 * Liefert einen Iterator über die ChildColumns.
	 * 
	 * @return
	 */
	Iterator<JDataColumn> getChildColumns() {
		return childColumns.iterator();
	}
	/**
	 * Liefert die DataColumn des Foreign Key mit der angegebenen
	 * laufenden Nummer (1-relativ).
	 * @param index
	 * @return
	 */
	JDataColumn getChildColumn(int index) {
		return childColumns.get(index-1);
	}
	/**
	 * Liefert den oder die Foreign Keys mit Komma getrennt.
	 * 
	 * @return
	 */
	public String getFK() {
	   return fk;
//		StringBuilder buff = new StringBuilder();
//		for (Iterator<JDataColumn> i = this.getChildColumns(); i.hasNext();) {
//			JDataColumn col = i.next();
//			buff.append(col.getColumnName());
//			buff.append(",");
//		}
//		if (buff.length() > 0) {
//			buff.deleteCharAt(buff.length() - 1);
//		}
//		return buff.toString();
	}
	/**
	 * @return
	 */
	public int getDeleteRule() {
		return delete_rule;
	}

	/**
	 * @param delete_rule
	 */
	public void setDeleteRule(int delete_rule) {
		this.delete_rule = delete_rule;
	}

	/**
	 * @return
	 */
	public int getUpdateRule() {
		return update_rule;
	}

	/**
	 * @param update_rule
	 */
	public void setUpdateRule(int update_rule) {
		this.update_rule = update_rule;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Reference Name: ");
		sb.append(this.refName);
		sb.append('\n');
		sb.append("Parent Table : ");
		sb.append(this.parentTable.getTablename());
		sb.append('\n');
		sb.append("Child Table  : ");
		sb.append(this.childTable.getTablename());
		sb.append('\n');
		sb.append("Foreign Key  : ");
		sb.append(this.childColumns);
		return sb.toString();
	}
}