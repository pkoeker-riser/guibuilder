/*
 * Created on 23.04.2003
 */
package de.jdataset;

import java.io.Serializable;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedHashMap;

import de.pkjs.util.*;

import electric.xml.*;
/**
 * Definition einer Spalte in einer Tabelle.<p>
 * Eine DataColumn sollte nur über eine DataTable erzeugt werden.
 * @see JDataTable#addColumn
 */
public final class JDataColumn implements Serializable {
	// Attributes
	private JDataTable myTable; // Darf nie null sein!
	private String colName;
	private String alias;
	/**
	 * Wird redundant aus colName und alias gespeichert wegen sonst wiederholter String-Addition
	 */
	private String select;
	private int dataType = Types.OTHER;
	private boolean isNullable = true;
	private boolean isReadonly; // Wird nicht in die Datenbank geschrieben
	private boolean isTransient; // Wird nie aus der Datenbank gelesen oder in sie geschrieben.
	private boolean isPrimaryKey;
	private int key_seq; // Wenn Primary Key
	private boolean isForeignKey;
	private boolean isAutoid; // Unused
	private int size;
	private int decimalDigits;
	private String defaultValue;
	private transient String comment;
	private static JDataTable metaDataTable;
	
	private static LinkedHashMap<String, Integer> mapTypes = new LinkedHashMap<String, Integer>(30);
	
	static final long serialVersionUID = 2844179384188178955L;

	// Constructor
	/**
	 * @deprecated
	 * For serialization purpose only.
	 */
	public JDataColumn() {}	
	/**
	 * Erzeugt eine DataColumn in der angegebenen Tabelle
	 * mit dem angegebenen Namen vom angegebenen Datentyp 
	 * (Konstanten auch aus java.sql.Types).
	 * @param tbl
	 * @param name
	 * @param type
	 */
	public JDataColumn (JDataTable tbl, String name, int type) {
		if (tbl == null || name == null) {
			throw new IllegalArgumentException("DataTable or name is null");
		}
		this.myTable = tbl;
		this.colName = name.trim();
		this.dataType = type;
		this.makeSelectString();
	}
	/**
	 * Erzeugt eine Tabellenspalte mit einem Alias-Namen
	 * @param tbl
	 * @param name
	 * @param alias
	 * @param type
	 */
	public JDataColumn (JDataTable tbl, String name, String alias, int type) {
		this(tbl, name, type);
		if (alias != null) {
		  this.alias = alias.trim();
		}
		this.makeSelectString();
	}
	/**
	 * @param tbl
	 * @param name
	 * @param type
	 * @param nullable
	 * @param readonly
	 */
	public JDataColumn (JDataTable tbl, String name, int type, boolean nullable, boolean readonly) {
		this(tbl, name, type);
		this.isNullable = nullable;
		this.isReadonly = readonly;
		this.makeSelectString();
	}
	/**
	 * Wenn hier das Attribut 'type' fehlt, wird VARCHAR angenommen.
	 * @param ele
	 */
	public JDataColumn(JDataTable tbl, Element ele) {
		if (tbl == null) {
			throw new IllegalArgumentException("DataTable is null");
		}
		this.myTable = tbl;
		this.colName = ele.getAttribute("name");
		this.alias = ele.getAttribute("alias");
		this.makeSelectString();
		String pk = ele.getAttribute("pk");
		if (pk != null) {
			this.isPrimaryKey = Convert.toBoolean(pk);
			if (this.isPrimaryKey()) {
				String keySeq = ele.getAttribute("keySeq");
				this.key_seq = Convert.toInt(keySeq);
			}
		}
		// type
		{
			String s = ele.getAttribute("type");
			if (s != null) {
			   int i = Convert.toInt(s);
			   if (i == 0) { // String?
			      i = getType(s);
			   }
				this.setDataType(i); 
			} else {
			   this.setDataType(Types.VARCHAR);
			}
		}
		String fk = ele.getAttribute("fk");
		if (fk != null) {
			this.setForeignKey(Convert.toBoolean(fk));
		}
		// not null
		{
			String s = ele.getAttribute("notnull");
			if (s != null) {
				isNullable = !Convert.toBoolean(s);
			}
		}
		// autoid
		{
			String s = ele.getAttribute("autoid");
			if (s != null) {
				isAutoid = Convert.toBoolean(s);
			}
		}
		// ReadOnly
		{
			String s = ele.getAttribute("readonly");
			if (s != null) {
				this.isReadonly = Convert.toBoolean(s);
			}
		}
		// Transient
		{
			String s = ele.getAttribute("transient");
			if (s != null) {
				this.isTransient = Convert.toBoolean(s);
			}
		}
		this.defaultValue = ele.getAttribute("defaultValue");
		// size
		{
			String s = ele.getAttribute("size");
			if (s != null) {
				this.size = Convert.toInt(s);
			}
		}
		// dec
		{
			String s = ele.getAttribute("decimalDigits");
			if (s != null) {
				this.decimalDigits = Convert.toInt(s);
			}
		}
	}
	/**
	 * Erzeugt eine DataColumn aus einer geeigneten DataRow
	 * @param row
	 */
	public JDataColumn(JDataTable tbl, JDataRow row) {
		if (tbl == null) {
			throw new IllegalArgumentException("DataTable is null");
		}
		this.myTable = tbl;
		this.colName = row.getValue("ColumnName");
		this.alias = row.getValue("Alias");
		this.makeSelectString();
		this.setDataType(row.getValueInt("DataType"));
		this.setPrimaryKey(row.getValueBool("PrimaryKey"));
		this.setKeySeq(row.getValueInt("KeySeq"));
		this.setForeignKey(row.getValueBool("ForeignKey"));
		this.setNullable(!row.getValueBool("NotNull"));
		this.setAutoid(row.getValueBool("AutoId"));
		this.isReadonly = row.getValueBool("ReadOnly");
		this.isTransient = row.getValueBool("Transient");
		this.setSize(row.getValueInt("Size"));
		this.setDecimalDigits(row.getValueInt("DecimalDigits"));
		this.setDefaultValue(row.getValue("DefaultValue"));
	}
	// Methods
	/**
	 * Diese Spalte ist Teil eines Foreign Key.
	 * @param b
	 */
	public void setForeignKey(boolean b) {
		this.isForeignKey = b;
	}
	/**
	 * Zwecks Dokumentation wird ein XML-Element erstellt.
	 * @return Element
	 */
	public Element getElement() {
		Element ele = new Element("Column");
		ele.setAttribute("name", colName);
		if (isEmpty(this.alias) == false) {
			ele.setAttribute("alias", alias);
		}
		if (this.dataType != Types.OTHER) {
			ele.setAttribute("type", Integer.toString(dataType));
		}
		if (this.isPrimaryKey) {
			ele.setAttribute("pk", "true");
			ele.setAttribute("keySeq", Integer.toString(this.key_seq));
		}
		if (this.isNullable == false) {
			ele.setAttribute("notnull", "true");
		}
		if (this.isAutoid() == true) {
			ele.setAttribute("autoid", "true");
		}
		if (this.isReadonly == true) {
			ele.setAttribute("readonly", "true");
		}
		if (this.isTransient == true) {
				ele.setAttribute("transient", "true");
			}
		if (this.isForeignKey) {
			ele.setAttribute("fk", "true");
		}
		if (this.size > 0) {
			ele.setAttribute("size", Integer.toString(size));
		}
		if (this.decimalDigits > 0) {
			ele.setAttribute("decimalDigits", Integer.toString(decimalDigits));
		}
		if (isEmpty(this.defaultValue) == false ) {
			ele.setAttribute("defaultValue", defaultValue);
		}
		return ele;
	}
	/**
	 * Liefert den SQL-Type der Spalte (siehe java.sql.Types)
	 * @return
	 */
	public int getDataType() {
		return this.dataType;
	}
	/**
	 * Setzt den Datentyp dieser Spalte.
	 * @param type Konstante aus java.sql.Types
	 */
	public void setDataType(int type) {
		this.dataType = type;
	}
	/**
	 * Setzt den Datentyp dieser Spalte.
	 * @param type Konstante aus java.sql.Types
	 */
	public void setDataType(String type) {
	   int i = getType(type);
		this.dataType = i;
	}
	/**
	 * Liefert den Spaltenname.
	 * @return
	 */
	public String getColumnName() {
		return this.colName;
	}
	/**
	 * Setzt den Spaltenname.
	 * @param s String, neuer Name der Spalte
	 */
	public void setColumnName(String s) {
		if( s != null ) {
			this.colName = s;
			this.makeSelectString();
		}
	}
	/**
	 * Liefert den Index, unter dem diese Column in der DataTable
	 * geführt wird (0-relativ).<p>
	 * Dieser Index entspricht dem der DataValues in der entsprechenden
	 * DataRow.
	 * @return
	 */
	public int getColumIndex() {
		Iterator<JDataColumn> it = this.myTable.getDataColumns();
		int cnt = 0;
		while (it.hasNext()) {
			JDataColumn col = it.next();
			if (col == this) {
				return cnt;
			}
			cnt++;
		}
		throw new IllegalStateException("DataColumn not fount in DataTable");
	}
	/**
	 * Liefert den Default-Wert dieser Spalte;
	 * dieser Wert wird verwendet, wenn eine Zeile neu
	 * erzeugt wird.<p>
	 * Workaround:<br>
	 * Als DefaultValue wird null geliefert, wenn die Spalte
	 * Primary Key ist. 
	 * MySql setzt leider den DefaultValue von notnull
	 * Integer Keys immer auf "0".
	 * @see JDataTable#createNewRow()
	 * @return
	 */
	public String getDefaultValue() {
		if (isPrimaryKey) {
			return null;
		} else {
			return defaultValue;
		}
	}
	/**
	 * Der Default-Wert dieser Spalte wird gesetzt.
	 * dieser Wert wird verwendet, wenn eine Zeile neu
	 * erzeugt wird.
	 * @param s
	 */
	public void setDefaultValue(String s) {
		this.defaultValue = s;
	}
	/**
	 * Liefert true, wenn diese Spalte zu einem Foreign Key gehört.
	 * Achtung!<br>
	 * Es kann sein, daß diese Spalte zu mehreren Foreign Keys gehört!
	 * @return
	 */
	public boolean isForeignKey() {
		return isForeignKey;
	}
	/**
	 * Liefert true, wenn die Spalte zum Primary Key gehört.
	 * @return
	 */
	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}
	/**
	 * Liefert true, wenn diese Spalte nur aus der Datenbank gelesen,
	 * aber niemals geschrieben wird (INSERT, UPDATE).
	 * @return
	 */
	public boolean isReadonly() {
		return this.isReadonly;
	}
	/**
	 * Wenn hier true übergeben wird, wird diese Spalte nie
	 * in die Datenbank geschrieben.
	 * @param b
	 */
	public void setReadonly(boolean b) {
		this.isReadonly = b;
	}
	/**
	 * Definiert diese Spalte als "transient" wenn 'true', 
	 * d.h. sie wird vom Persistenz-Layer nie verwendet
	 * also weder aus der Datenbank gelesen, noch in sie
	 * geschrieben.
	 * @param b
	 */
	public void setTransient(boolean b) {
	   this.isTransient = b;
	}
	/**
	 * Liefert die Eigenschaft "transient".<p>
	 * Eine Spalte ist auch dann transient, wenn ihre
	 * Tabelle transient ist.
	 * @see JDataTable#setTransient(boolean)
	 * @return
	 */
	public boolean isTransient() {
	   if (this.myTable.isTransient()) 
	     return true;
	   return this.isTransient;
	}
	/**
	 *  
	 * @return true, wenn Datentyp zeichenorientiert
	 */
	public boolean isString() {
	   switch (this.dataType) {
	      case Types.CHAR:
	      case Types.NCHAR:
	      case Types.VARCHAR:
          case Types.NVARCHAR:
	      case Types.LONGVARCHAR:
	      case Types.LONGNVARCHAR:
	      case Types.CLOB:
          case Types.NCLOB:
	         return true;
	      default:
	         return false;
	   }
	}
	/**
	 * Liefert die maximale Länge dieser Column; 
	 * nur für Strings wirklich von Interesse.
	 * @return
	 */
	public int getSize() {
		return size;
	}
	/**
	 * Setzt die maximale Länge dieser Column.
	 * Nur bei Strings wirklich hilfreich.
	 * @param i
	 */
	public void setSize(int i) {
		size = i;
	}
	/**
	 * Ich bin Primary Key meiner Tabelle (oder ein Teil davon).<p>
	 * @param b
	 */
	public void setPrimaryKey(boolean b) {
		isPrimaryKey = b;
		if (b && this.key_seq == 0) this.key_seq = 1; // default
	}
	/**
	 * Liefert den Alias-Name dieser Column oder null, wenn kein Alias
	 * @return
	 */
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String alias) {
	  this.alias = alias;
	}
	
	/**
	 * Liefert die Datatable zur Column,
	 * @return myTable JDataTable
	 */
	public JDataTable getDataTable() {
		return this.myTable;
	}
	
	/**
	 * Liefert den Anteil des SELECT-Statements für diese Column:<p>
	 * SELECT name,<br>
	 * SELECT name AS alias,
	 * @return
	 */
	public String getSelect() {
		return this.select;
	}
	private void makeSelectString() {
		if (this.isEmpty(this.alias)) {
			this.select = colName + ",";
		} else {
			this.select = colName + " AS " + alias + ","; 
		}
	}
	/**
	 * Teil des SELECT-STatements incl. Tabellenname:<br>
	 * SELECT ... myTableName.myColumnName,...<br>
	 * SELECT ... myTableName.myColumnName AS aliasName,...<br>
	 * @return
	 */
	public String getSelectJoin() {
		if (this.isEmpty(this.alias)) {
			return this.myTable.getAliasOrName()+"."+ colName + ",";
		} else {
			return this.myTable.getAliasOrName()+"."+ colName + " AS " + alias + ","; 
		}
	}
	/**
	 * !notnull
	 * @return
	 */
	public boolean isNullable() {
		return isNullable;
	}
	/**
	 * Definiert, ob NULL-Values in dieser Spalte erlaubt sind.
	 * @param b
	 */
	public void setNullable(boolean b) {
		this.isNullable = b;
	}
	/**
	 * @return
	 */
	public boolean isAutoid() {
		return isAutoid;
	}

	/**
	 * Wenn true, werden die Primary Keys von der Datenbank erzeugt.
	 * 
	 * @param b
	 */
	public void setAutoid(boolean b) {
		isAutoid = b;
	}
	/**
	 * Liefert den SQL-Datentyp-Namen zu der angegebenen Konstante aus
	 * java.sql.Types.
	 * @param i
	 * @return
	 */
	public static String getTypeName(int i) {
		switch (i) {
			case Types.ARRAY :
				return "ARRAY";
			case Types.BIGINT :
				return "BIGINT";
			case Types.BINARY :
				return "BINARY";
			case Types.BIT :
				return "BIT";
			case Types.BLOB :
				return "BLOB";
			case 16: // Types.BOOLEAN : // Wegen JDK 1.3
				return "BOOLEAN";
			case Types.CHAR :
				return "CHAR";
			case Types.CLOB :
				return "CLOB";
			case 70: // Types.DATALINK : // Wegen JDK 1.3
				return "DATALINK";
			case Types.DATE :
				return "DATE";
			case Types.DECIMAL :
				return "DECIMAL";
			case Types.DISTINCT :
				return "DISTINCT";
			case Types.DOUBLE :
				return "DOUBLE";
			case Types.FLOAT :
				return "FLOAT";
			case Types.INTEGER :
				return "INTEGER";
			case Types.JAVA_OBJECT :
				return "JAVA_OBJECT";
			case Types.LONGVARBINARY :
				return "LONGVARBINARY";
			case Types.LONGVARCHAR :
				return "LONGVARCHAR";
			case Types.NUMERIC :
				return "NUMERIC";
			case Types.OTHER :
				return "OTHER";
			case Types.REAL :
				return "REAL";
			case Types.REF :
				return "REF";
			case Types.SMALLINT :
				return "SMALLINT";
			case Types.STRUCT :
				return "STRUCT";
			case Types.TIME :
				return "TIME";
			case Types.TIMESTAMP :
				return "TIMESTAMP";
			case Types.TINYINT :
				return "TINYINT";
			case Types.VARBINARY :
				return "VARBINARY";
			case Types.VARCHAR :
				return "VARCHAR";
			default :
				throw new IllegalArgumentException("Unknown java.sql.Types: " +i);
		}
	}
	/**
	 * Liefert zu dem angegebenen Datenbank-Feldtypen den entsprechenden Wert
	 * aus java.sql.Types<p>
	 * "INTEGER" --> Types.INTEGER<p>
	 * Wenn fehlt, dann VARCHAR
	 * @param typeName
	 * @return
	 */
	public static int getType(String typeName) {
		int type = Types.VARCHAR;
		Integer i = mapTypes.get(typeName.toUpperCase());
		if (i != null) {
			type = i.intValue();
		}
		return type;
	}
	/**
	 * Liefert die DataTable mit den Feldnamen dieser Klasse.
	 * @return
	 */
	public static JDataTable getMetaDataTable() {
		if (metaDataTable == null) {
			metaDataTable = new JDataTable("TableColumn");
			metaDataTable.setDatabaseName(JDataTable.META_DATABASE_NAME);
			metaDataTable.setTransient(true);
			JDataColumn cName = metaDataTable.addColumn("ColumnName", Types.VARCHAR);
			cName.setPrimaryKey(true);
         cName.setKeySeq(1);
			cName.setNullable(false);
			JDataColumn cTable = metaDataTable.addColumn("TableName", Types.VARCHAR); // Foreign Key
			cTable.setForeignKey(true);
			@SuppressWarnings("unused")
      JDataColumn cAlias = metaDataTable.addColumn("Alias", Types.VARCHAR);
			@SuppressWarnings("unused")
      JDataColumn cType = metaDataTable.addColumn("DataType", Types.INTEGER); // TODO : type?
			@SuppressWarnings("unused")
      JDataColumn cTypeName = metaDataTable.addColumn("DataTypeName", Types.VARCHAR);
			JDataColumn cPK = metaDataTable.addColumn("PrimaryKey", Types.BOOLEAN); 
			cPK.setDefaultValue("0");
			@SuppressWarnings("unused")
      JDataColumn cKeySeq = metaDataTable.addColumn("KeySeq", Types.SMALLINT);
			JDataColumn cFK = metaDataTable.addColumn("ForeignKey", Types.BOOLEAN); 
			cFK.setDefaultValue("0");
			JDataColumn cNN = metaDataTable.addColumn("NotNull", Types.BOOLEAN); 
			cNN.setDefaultValue("0");
			JDataColumn cAuto = metaDataTable.addColumn("AutoId", Types.BOOLEAN); 
			cAuto.setDefaultValue("0");
			JDataColumn cRo = metaDataTable.addColumn("ReadOnly", Types.BOOLEAN); 
			cRo.setDefaultValue("0");
			JDataColumn cTrans = metaDataTable.addColumn("Transient", Types.BOOLEAN);
			cTrans.setDefaultValue("0");
			@SuppressWarnings("unused")
      JDataColumn cSize = metaDataTable.addColumn("Size", Types.INTEGER);
			@SuppressWarnings("unused")
      JDataColumn cDeci = metaDataTable.addColumn("DecimalDigits", Types.INTEGER);
			@SuppressWarnings("unused")
      JDataColumn cDefault = metaDataTable.addColumn("DefaultValue", Types.VARCHAR);
		}
		return metaDataTable;
	}
	/**
	 * Liefert die Metadaten ein DataColumn als DataRow
	 * @return
	 */
	public JDataRow getMetaDataRow() {
		JDataRow row = getMetaDataTable().createNewRow();
		row.setValue("ColumnName", this.colName);
		row.setValue("TableName", this.myTable.getTablename());
		row.setValue("Alias", this.alias);
		row.setValue("DataType", this.getDataType());
		row.setValue("DataTypeName", getTypeName(this.getDataType()));
		row.setValue("PrimaryKey", this.isPrimaryKey());
		row.setValue("ForeignKey", this.isForeignKey());
		row.setValue("NotNull", !this.isNullable());
		row.setValue("AutoId", this.isAutoid());
		row.setValue("Size", this.getSize());
		row.setValue("DecimalDigits", this.getDecimalDigits());
		row.setValue("DefaultValue", this.getDefaultValue());
		row.setValue("ReadOnly", this.isReadonly());
		row.setValue("Transient", this.isTransient());
		//row.commitChanges();
		return row;
	}
	private boolean isEmpty(String s) {
		if (s == null || s.length() == 0) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * @return
	 */
	public int getDecimalDigits() {
		return decimalDigits;
	}

	/**
	 * @param i
	 */
	public void setDecimalDigits(int i) {
		decimalDigits = i;
	}
	/**
	 * Liefert die DataColumn als XML-Element
	 */
	public String toString() {
		return this.getElement().toString();
	}
	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}
	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	/**
	 * Liefert 'true' wenn der angegebene Datentyp von diesem
	 * Framework unterstützt wird.
	 * @param type @see java.sql.Types
	 * @return
	 */
	public static boolean isSupportedColumnType(int type) {
	   switch (type) {
			case Types.BIGINT:
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			case Types.BIT:
			case Types.BOOLEAN: 
			case Types.CHAR:
			case Types.LONGVARCHAR:
			case Types.VARCHAR:
			case Types.DATE:
			case Types.DECIMAL:
			case Types.NUMERIC:
			case Types.REAL: // float
			case Types.FLOAT:
			case Types.DOUBLE:
			case Types.TIME:
			case Types.TIMESTAMP:
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			case Types.BLOB:
			case Types.CLOB:
			   return true;
	   }
	   return false;
	}
	static {
		mapTypes.put("ARRAY", new Integer(Types.ARRAY));
		mapTypes.put("BIGINT", new Integer(Types.BIGINT));
		mapTypes.put("BINARY", new Integer(Types.BINARY));
		mapTypes.put("BIT", new Integer(Types.BIT));
		mapTypes.put("BLOB", new Integer(Types.BLOB));
		mapTypes.put("BOOLEAN", new Integer(Types.BOOLEAN));
		mapTypes.put("CHAR", new Integer(Types.CHAR));
		mapTypes.put("CLOB", new Integer(Types.CLOB));
		mapTypes.put("DATE", new Integer(Types.DATE));
		mapTypes.put("DATALINK", new Integer(Types.DATALINK));
		mapTypes.put("DECIMAL", new Integer(Types.DECIMAL));
		mapTypes.put("DISTINCT", new Integer(Types.DISTINCT));
		mapTypes.put("DOUBLE", new Integer(Types.DOUBLE));
		mapTypes.put("FLOAT", new Integer(Types.FLOAT));
		mapTypes.put("INTEGER", new Integer(Types.INTEGER));
		mapTypes.put("JAVA_OBJECT", new Integer(Types.JAVA_OBJECT));
		mapTypes.put("LONGVARBINARY", new Integer(Types.LONGVARBINARY));
		mapTypes.put("LONGVARCHAR", new Integer(Types.LONGVARCHAR));
		mapTypes.put("NUMERIC", new Integer(Types.NUMERIC));
		mapTypes.put("OTHER", new Integer(Types.OTHER));
		mapTypes.put("REAL", new Integer(Types.REAL));
		mapTypes.put("REF", new Integer(Types.REF));
		mapTypes.put("SMALLINT", new Integer(Types.SMALLINT));
		mapTypes.put("STRUCT", new Integer(Types.STRUCT));
		mapTypes.put("TIME", new Integer(Types.TIME));
		mapTypes.put("TIMESTAMP", new Integer(Types.TIMESTAMP));
		mapTypes.put("TINYINT", new Integer(Types.TINYINT));
		mapTypes.put("VARBINARY", new Integer(Types.VARBINARY));
		mapTypes.put("VARCHAR", new Integer(Types.VARCHAR));
	}
	/**
	 * Liefert die Position dieser Column im Primary Key falls Primary Key
	 * (1-relativ)
	 * @return Returns the key_seq.
	 */
	public int getKeySeq() {
		return this.key_seq;
	}
	/**
	 * @param key_seq The key_seq to set.
	 */
	public void setKeySeq(int key_seq) {
		this.key_seq = key_seq;
	}
	/**
	 * Prüft, ob eine Datentyp (java.sql.Types) zu den binären Datentypen gehört.
	 * @param t int, zur prüfender Datentyp @see java.sql.Types.
	 * @return 'true', wenn der Datentyp binär ist, false sonst.
	 */
	public static boolean isBinaryType(int t) {

		switch(t) {
			case Types.BINARY:
			case Types.BLOB:
			case Types.LONGVARBINARY:
			case Types.VARBINARY:
				return true;
		}
		
		return false;
	}
	
	public static boolean isBooleanType(int t) {

		switch(t) {
			case Types.BIT:
			case Types.BOOLEAN:
				return true;
		}
		
		return false;
		
	}

}
