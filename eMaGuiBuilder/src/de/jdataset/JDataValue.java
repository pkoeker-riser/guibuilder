/*
 * Created on 23.04.2003
 * 01.09.2005 IKU: support für BLOBs erweitert. Serialisierung und Deserialisierung
 * von BLOBs als Base64 umgesetzt.
 */
package de.jdataset;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import de.pkjs.util.Base64Coder;
import de.pkjs.util.Convert;
import electric.xml.Element;

/**
 * Repräsentiert den Wert einer Spalte in einer Zeile.
 * <p>
 * Der Wert wird intern immer als String gehalten, und zwar in einer Human
 * Readable Form.<br>
 * Die Formate für Date, Time und Timestamp werden aus der Klasse Convert
 * übernommen.
 * 
 * @see JDataColumn
 * @see JDataRow
 */
public final class JDataValue implements Serializable {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(JDataValue.class);

	private static final String BLOB_TEXT = "binary data";
	private JDataColumn column; // Darf nie null sein.
	private String value;
	private byte[] objValue;
	private boolean modified;
	private String oldValue;
	private int errorCode;
	private boolean binary;

	public static final int NO_ERROR = 0;
	public static final int NOTNULL = 1;
	public static final int MAX_LENGTH_EXEEDED = 2;
	public static final int ILLEGAL_DATE_FORMAT = 3;
	public static final int ILLEGAL_TIME_FORMAT = 4;
	public static final int ILLEGAL_DATETIME_FORMAT = 5;
	public static final int ILLEGAL_NUMBER_FORMAT = 6;
	public static final int ILLEGAL_BOOLEAN_VALUE = 7;
	public static final int USER_DEFINED_ERROR = 8;

	static final long serialVersionUID = -6488037242550442912L;

	// Constructor
	/**
	 * @deprecated For serialization purpose only.
	 */
	public JDataValue() {
	}

	/**
	 * Erzeugt einen neuen Wert unter Angabe der Spalte und des Werts.
	 */
	public JDataValue(JDataColumn column, String value) {
		this.column = column;
		this.value = value;
	}

	/**
	 * Für Erstellung eines DataSet aus XML
	 * <p>
	 * Wird von JDataRow verwendet.
	 * 
	 * @param column
	 * @param ele
	 */
	JDataValue(JDataColumn column, Element ele) {
		this.column = column;
		this.modified = Convert.toBoolean(ele.getAttribute("modified"));
		this.binary = Convert.toBoolean(ele.getAttribute("binary"));
		if (binary) {
			String base64 = ele.getTextString();
			if (base64 != null) {
				try {
				   base64 = base64.replaceAll("\r", "");
                   base64 = base64.replaceAll("\n", "");
					this.objValue = Base64Coder.decode(base64);
					this.value = "binary data";
				} catch (Exception ex) {
					logger.error("Error decoding base64 value for column ["
							+ column.getColumnName() + "] " + ex.getMessage());
				}
			}
		} else {
			this.value = ele.getTextString();			
			if (ele.hasAttribute("oldValue")) { // nur wenn modified + PK
			   this.oldValue = ele.getAttribute("oldValue");
			}
		}
	}
	/**
	 * Clont einen DataValue
	 * @param column Referent auf die gleichfalls geclonte Column
	 * @param origVal
	 */
	JDataValue(JDataColumn column, JDataValue origVal) {
	   this.column = column;
	   this.binary = origVal.binary;
	   this.errorCode = origVal.errorCode;
	   this.modified = origVal.modified;
	   this.objValue = origVal.objValue;
	   this.oldValue = origVal.oldValue;
	   this.value = origVal.value;
	}

	// Methods
	/**
	 * Liefert den Spaltenname.
	 * 
	 * @see JDataColumn#getColumnName()
	 */
	public String getColumnName() {
		return this.column.getColumnName();
	}

	/**
	 * Liefert den Sql-Datentyp aus der Spalte siehe java.sql.Types
	 * 
	 * @see JDataColumn#getDataType()
	 */
	public int getDataType() {
		return this.column.getDataType();
	}

	/**
	 * Liefert den Wert dieser Spalte.
	 * 
	 * @return
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Liefert den Wert dieser Spalte oder den übergebenen Default-Wert wenn null.
	 * 
	 * @return
	 */
	public String getValue(String defaultValue) {
		if (this.value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}

	/**
	 * Liefert den Wert dieser Spalte als Datum.
	 * 
	 * @return
	 */
	public java.util.Date getValueDate() {
		return Convert.toDate(this.value);
	}

	/**
	 * Liefert den Inhalt der Spalte als Timestamp. Wirft eine
	 * IllegalStateException, wenn die Spalte nicht vom Typ Types.TIMESTAMP ist.
	 * 
	 * @return
	 */
	public Timestamp getValueTimestamp() {
		if (this.column.getDataType() != Types.TIMESTAMP) {
			throw new IllegalStateException("Not a Timestamp Column: "
					+ column.getColumnName());
		}
		return Convert.toDateTime(this.value);
	}

	/**
	 * Liefert den Wert dieser Spalte als Datum oder den übergebenen Default-Wert
	 * wenn null.
	 * 
	 * @param defaultValue
	 * @return
	 */
	public java.util.Date getValueDate(java.util.Date defaultValue) {
		if (this.value != null) {
			return Convert.toDate(this.value);
		} else {
			return defaultValue;
		}
	}

	/**
	 * Liefert den Wert dieser Spalte als int.
	 * 
	 * @see Convert
	 * @return
	 */
	public int getValueInt() {
		return Convert.toInt(this.value);
	}

	/**
	 * Liefert den Wert dieser Spalte als int oder den übergebenen Default-Wert
	 * wenn null.
	 * 
	 * @return
	 */
	public int getValueInt(int defaultValue) {
		if (this.value != null) {
			return Convert.toInt(this.value);
		} else {
			return defaultValue;
		}
	}

	/**
	 * Liefert den Wert dieser Spalte als long.
	 * 
	 * @return
	 */
	public long getValueLong() {
		return Convert.toLong(this.value);
	}

	/**
	 * Liefert den Wert dieser Spalte als long oder den übergebenen Default-Wert
	 * wenn null.
	 * 
	 * @return
	 */
	public long getValueLong(long defaultValue) {
		if (this.value != null) {
			return Convert.toLong(this.value);
		} else {
			return defaultValue;
		}
	}

	/**
	 * Liefert den Wert als double
	 * 
	 * @return
	 */
	public double getValueDouble() {
		return Convert.toDouble(this.value);
	}

	/**
	 * Liefert den Wert als boolean.
	 * 
	 * @return
	 */
	public boolean getValueBool() {
		return Convert.toBoolean(this.value);
	}

	/**
	 * @see Convert#toObject(String, int)
	 * @return
	 */
	public Object getValueObject() {
		return Convert.toObject(this.value, this.getDataType());
	}

	/**
	 * @see setObjectValue
	 * @return
	 */
	public Object getObjectValue() {
		return this.objValue;
	}

	/**
	 * Liefert den Wert dieser Spalte vor der letzten Änderung.
	 * 
	 * @return
	 */
	public String getOldValue() {
		return this.oldValue;
	}

	/**
	 * Setzt den Wert dieser Spalte.
	 * <p>
	 * Wenn der Wert abweichend zu dem bisherigen Inhalt ist, wird modified auf
	 * 'true' gesetzt.
	 * <p>
	 * Achtung!<br>
	 * Auch wenn die dazugehörige DataColumn readonly ist, können hier Werte
	 * übergeben werden; diese finden nur nie den Weg in die Datenbank.
	 * <p>
	 * Falls dieser Wert Teil eines Primärschlüssels ist, wird bei ggf.
	 * vorhandenen abhängigen Daten die Update-Rule für den Foreign Key
	 * berücksichtigt.
	 * 
	 * @see #isModified
	 * @see JDataColumn#isReadonly
	 * @param s
	 *          Der neue Inhalt der Spalte
	 * @return Der übergebene Wert
	 */
	public String setValue(final String s) {
      boolean wasChanged = false;
      if(this.value == null && s == null) { // Neu 25.8.2003
         return null;
      }
      else if(this.value == null && s.length() == 0) {
         oldValue = value;
         // Wenn vorher null und jetzt Leerstring, dann nicht modified setzen!
      }
      else if(this.value == null && s != null) {
         if(modified == false) {
            this.modified = true;
            wasChanged = true;
            oldValue = value;
         }
      }
      else if(this.value != null && s == null) {
         if(modified == false) {
            this.modified = true;
            wasChanged = true;
            oldValue = value;
         }
      }
      else if(Convert.isModified(value, s, this.getDataType()) && this.modified == false) {
            this.modified = true;
            wasChanged = true;
            oldValue = value;
      }
		this.value = s;
		// Update Rule Primary Key --> Foreign Key
		try { //##
			if (this.getColumn().isPrimaryKey() && wasChanged == true) {
				ArrayList<Relation> al = this.getColumn().getDataTable().getChildRelations();
				if (al != null) {
					Iterator<Relation> it = al.iterator();
					while (it.hasNext()) {
						Relation rela = it.next();
						int rule = rela.getUpdateRule();
						if (rule != DatabaseMetaData.importedKeyNoAction) {
							/* Jetzt brauche ich "meine" Row, damit ich deren Child Rows abarbeiten kann!
							   TODO: Prüfen: Ist hier sichergestelt, daß das wirklich die gemeinte Row ist?
							   Nachtrag 17.3.2014:
							   Das funktioniert ganz und gar nicht :-(
							   Hier nur Warnung ausgeben							
							 */
						   logger.warn("Update Primary Key Column: " + column.getDataTable().getTablename()+"/"+column.getColumnName() + " Relation: " + rela.getRefName() + "; UpdateRule != importedKeyNoAction does not work");
					   
						   
//							JDataRow myRow = this.getDataRow(); 
//							// Nur bei UPDATE
//							if (myRow.isInserted() == false && myRow.isDeleted() == false) {
//								// Jetzt brauchen wir den Foreign Key,
//								// der zu dieser PK-Column paßt! (KEY_SEQ)
//								int keySeq = this.getColumn().getKeySeq();
//								JDataColumn fkColumn = rela.getChildColumn(keySeq);
//								String fkName = fkColumn.getColumnName();
//								Iterator<JDataRow> itc = myRow.getChildRows(rela.getRefName());
//								if (itc != null) {
//									while (itc.hasNext()) {
//										JDataRow crow = itc.next();
//										switch (rule) {
//										case DatabaseMetaData.importedKeyCascade:
//											crow.setValue(fkName, this.value);
//											break;
//										case DatabaseMetaData.importedKeyRestrict:
//											throw new IllegalStateException("Update restricted! "
//													+ rela.getRefName());										
//										case DatabaseMetaData.importedKeySetDefault:
//											String dval = crow.getDataColumn(fkName).getComment();
//											crow.setValue(fkName, dval);
//											break;
//										case DatabaseMetaData.importedKeySetNull:
//											crow.setValue(fkName, (String) null);
//											break;
//										}
//									}
//								}
//							}
						}
					}
				}
			}
		} catch (Exception ex) {
			// Erstmal nix machen, wenn Update Rule schief ging
		}
		return this.value;
	}

	/**
	 * Setzt den Wert der Spalte als long; wird in String verwandelt.
	 * 
	 * @param l
	 */
	public String setValue(long l) {
		String s = Long.toString(l);
		return this.setValue(s);
	}

	/**
	 * Setzt den Wert der Spalte als int; wird in String verwandelt.
	 * 
	 * @param i
	 */
	public String setValue(int i) {
		String s = Integer.toString(i);
		return this.setValue(s);
	}

	/**
	 * Setzt den Wert der Spalte und liefert entsprechend 'true' oder 'false'.
	 * 
	 * @param b
	 * @return ein boolean als String konvertiert.
	 */
	public String setValue(boolean b) {
		String s = Convert.toString(b);
		return this.setValue(s);
	}

	/**
	 * Setzt den Wert der Spalte als Datum; wird in String verwandelt.<br>
	 * Nimmt auch Objekte der Klasse Timestamp.
	 * 
	 * @param date
	 * @return Das übergebene Datum als String
	 */
	public String setValue(java.util.Date date) {
		String s = null;
		switch (this.getDataType()) {
		case Types.TIMESTAMP:
			s = Convert.toStringTimestamp(date);
			break;
		case Types.TIME:
			s = Convert.toStringTime(date);
			break;
		default:
			s = Convert.toString(date);
			break;
		}
		this.setValue(s);
		return s;
	}

	/**
	 * Setzt den Wert als double.
	 * 
	 * @param d
	 * @return Der übergebene Wert als String
	 */
	public String setValue(double d) {
		String s = Convert.toString(d);
		this.setValue(s);
		return s;
	}

	/**
	 * Setzt den Wert als BigDecimal.
	 * 
	 * @param big
	 * @return
	 */
	public String setValue(BigDecimal big) {
		String s = Convert.toString(big);
		this.setValue(s);
		return s;
	}

	/**
	 * Liefert den Wert als BigDecimal
	 * 
	 * @return
	 */
	public BigDecimal getValueBigDecimal() {
		BigDecimal ret = Convert.toBigDecimal(this.getValue());
		return ret;
	}

	/**
	 * Die Konvertierung nach String hängt vom Datentyp ab.
	 * 
	 * @see #getDataType
	 * @see Convert
	 * @param o
	 * @return nach String konvertiertes Objekt
	 */
	public String setValue(Object o) {
		String s = Convert.toString(o, this.getDataType());
		return this.setValue(s);
	}

	/**
	 * Setzt den Object-Value des Datavalues
	 * 
	 * @param o
	 */
	public void setObjectValue(Object o) {
		if (o != null && !(o instanceof byte[])) {
			throw new IllegalArgumentException(
					"Only arguments of type byte[] are supported!");
		}

		this.objValue = (byte[]) o;
		this.modified = true;
		if (o != null) {
			this.binary = true;
			this.value = BLOB_TEXT;
		} else {
			this.binary = false;
			this.value = null;
		}
	}

	/**
	 * Falls BLOB durch die Methode <code>setObjectValue</code> gesetzt wurde,
	 * wird true geliefert
	 * 
	 * @return
	 */
	public boolean isBinary() {
		return binary;
	}

	/**
	 * Nur ein Wert der die Eigenschaft 'modified' hat, wird beim UPDATE in die
	 * Datenbank geschrieben.
	 * 
	 * @return
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Liefert die Eigenschaft modified nur als true, wenn nicht transient und
	 * nicht readonly.
	 * <p>
	 * Wird vom Persistenz-Layer für UPDATE benötigt.
	 * 
	 * @see JDataColumn#isTransient()
	 * @see JDataColumn#isReadonly()
	 * @return
	 */
	public boolean isModifiedPersistent() {
		if (this.getColumn().isTransient() || this.getColumn().isReadonly()) {
			return false;
		} else {
			return isModified();
		}
	}

	/**
	 * Ein Wert der die Eigenschaft 'modified' hat, wird in die Datenbank
	 * geschrieben: UPDATE.
	 * <p>
	 * Wenn modified auf false gesetzt wird, dann wird oldValue auf null gesetzt.
	 */
	public void setModified(boolean b) {
		this.modified = b;
		if (!modified) {
			oldValue = null; // New 15.5.2004 / Dieser Wert wird jetzt nicht mehr
												// gebraucht / PKÖ
		}
	}

	Element getElement() {
		Element ele = new Element(this.column.getColumnName());
		if (this.modified) {
			ele.setAttribute("modified", "true");
	        if (this.getOldValue() != null) { // 13.6.2018: für ChangeLog 
	           ele.setAttribute("oldValue", this.getOldValue());
	        }
		}
		if (this.binary || objValue != null) {
			ele.setAttribute("binary", "true");
			if (this.objValue != null) {
				String base64 = Base64Coder.encodeLines(objValue); // CRLF alle 76 Bytes!
				ele.setText(base64);
			}
		} else {
			if (this.value != null) {
				ele.setText(value);
			}
		}
		return ele;
	}

	/**
	 * Liefert die Column-Definition zu diesem Value.
	 * 
	 * @return
	 */
	public JDataColumn getColumn() {
		return column;
	}

	/**
	 * Setzt den Wert auf den vorigen Wert zurück falls Inhalt verändert wurde und
	 * setzt die Eigenschaft 'modified' wieder auf 'false'.
	 */
	public void rollbackChanges() {
		if (this.modified) {
			this.modified = false;
			this.value = this.oldValue;
		}
	}

	/**
	 * @see #getValue
	 */
	public String toString() {
		String s = this.getColumnName() + ": " + this.value;
		return s;
	}

	/**
	 * Liefert den Fehlercode von verify
	 * 
	 * @see #verify
	 * @return
	 */
	public int getErrorCode() {
		return this.errorCode;
	}

	/**
	 * Setzt den ErrorCode auf einen angegebenen Wert.
	 * 
	 * @param code
	 */
	public void setErrorCode(int code) {
		if (code < NO_ERROR || code > USER_DEFINED_ERROR) {
			String msg = "Error Code out of Range: " + code;
			throw new IllegalArgumentException(msg);
		}
		this.errorCode = code;
	}

	/**
	 * Überprüft den Wert auf Gültigkeit; wenn hier 'false' geliefert wird, kann
	 * man sich mit getErrorCode den Fehler anzeigen lassen.
	 * <ul>
	 * <li>Not Null
	 * <li>Max Size exeeded
	 * <li>Illegal Boolean Value
	 * <li>Illegal Date Value
	 * <li>Illegal Time Value
	 * <li>Illegal DateTime Value
	 * <li>Illegal Number Value
	 * </ul>
	 * 
	 * @see #getErrorCode()
	 * @return
	 */
	public boolean verify(boolean inserted) {
		// TODO bei notModified und readonly nicht prüfen???
		String s = this.getValue();
		// Trim
		if (s != null) {
			if (s.startsWith(" ") || s.endsWith(" ")) {
				s = s.trim(); // Trim, aber nicht modified!
				this.value = s;
			}
		}
		// Not Null
		if (this.getColumn().isNullable() == false && (s == null || s.length() == 0)) {
			/*
			 * KKN 26.05.2004 Primärschlüssel dürfen NULL sein, auch wenn dran steht
			 * das sie es nicht sein dürfen
			 */
				if (inserted == true) { // Aber nur, wenn neue Zeile!
					if (this.getColumn().isPrimaryKey() == false
					// Auch Foreign Keys dürfen null sein,
							// weil sie vom PL mit Werten des PK versorgt werden.
							&& this.getColumn().isForeignKey() == false) {
						this.errorCode = NOTNULL;
						return false;
					}
				} else { // nicht inserted
					this.errorCode = NOTNULL;
					return false;
				}
		} else {
			/*
			 * KKN 26.05.2004: Die Prüfung auf ein gültigtes Format darf nur dann
			 * erfolgen, wenn im JDataValue auch etwas drin steht !
			 */
			switch (this.getColumn().getDataType()) {
			// Boolean
			case Types.BIT:
			case Types.BOOLEAN:
			case Types.TINYINT: // MySql hat kein Boolean
			{
				@SuppressWarnings("unused")
        boolean b = Convert.toBoolean(s);
				if (Convert.getLastException() != null) {
					this.errorCode = ILLEGAL_BOOLEAN_VALUE;
					return false;
				}
			}
				break;

			case Types.INTEGER:
			case Types.SMALLINT: {
				@SuppressWarnings("unused")
        int i = Convert.toInt(s);
				if (Convert.getLastException() != null) {
					this.errorCode = ILLEGAL_NUMBER_FORMAT;
					return false;
				}
			}
				break;

			case Types.BIGINT: {
				@SuppressWarnings("unused")
        long l = Convert.toLong(s);
				if (Convert.getLastException() != null) {
					this.errorCode = ILLEGAL_NUMBER_FORMAT;
					return false;
				}
			}
				break;

			case Types.CHAR:
			case Types.LONGVARCHAR:
			case Types.VARCHAR: {
				if (s != null && this.getColumn().getSize() > 0 && s.length() > this.getColumn().getSize()) {
					this.errorCode = MAX_LENGTH_EXEEDED;
					return false;
				}
			}
				break;
			case Types.DATE: {
			  if (s != null && s.length() > 0) {
  				@SuppressWarnings("unused")
          Date d = Convert.toSqlDate(s);
  				if (Convert.getLastException() != null) {
  					this.errorCode = ILLEGAL_DATE_FORMAT;
  					return false;
  				}
			  }
			}
				break;
			case Types.DECIMAL:
			case Types.NUMERIC: {
				@SuppressWarnings("unused")
        BigDecimal big = Convert.toBigDecimal(s);
				if (Convert.getLastException() != null) {
					this.errorCode = ILLEGAL_NUMBER_FORMAT;
					return false;
				}
			}
				break;
			case Types.REAL:
			case Types.FLOAT:
			case Types.DOUBLE: {
				@SuppressWarnings("unused")
        double d = Convert.toDouble(s);
				if (Convert.getLastException() != null) {
					this.errorCode = ILLEGAL_NUMBER_FORMAT;
					return false;
				}
			}
				break;
			case Types.TIME: {
				@SuppressWarnings("unused")
        java.sql.Time d = Convert.toTime(s);
				if (Convert.getLastException() != null) {
					this.errorCode = ILLEGAL_TIME_FORMAT;
					return false;
				}
			}
				break;
			case Types.TIMESTAMP: {
				@SuppressWarnings("unused")
        Timestamp dt = Convert.toDateTime(s);
				if (Convert.getLastException() != null) {
					this.errorCode = ILLEGAL_DATETIME_FORMAT;
					return false;
				}
			}
			default: {
				// ??
			}
			} // End Switch Types
		}
		if (this.errorCode == USER_DEFINED_ERROR)
			return false;

		this.errorCode = NO_ERROR;
		return true;
	}

	/**
	 * Ermittel die DataRow, zu der dieser Value gehört. Liefert null, wenn dieses
	 * nicht ermittelbar.
	 * TODO: prüfen, ob das die gewünschte Row liefert! (
	 * @return
	 */
//	JDataRow getDataRow() { //##
//		JDataSet ds = this.column.getDataTable().getMyDataset();
//		Iterator<JDataRow> itr = ds.getChildRows();
//		if (itr != null) {
//			JDataRow row = itr.next();
//			if (this.isParentRow(row))
//				return row;
//		}
//
//		return null;
//	}

	/**
	 * Rekursive Suche nach der DataRow dieses DataValues
	 * 
	 * @param row
	 * @return
	 */
	private boolean isParentRow(JDataRow row) {
		// DataValues
		Iterator<JDataValue> itd = row.getDataValues();
		if (itd != null) {
			while (itd.hasNext()) {
				JDataValue val = itd.next();
				if (val == this)
					return true;
			}
		}
		// Child Rows
		Iterator<JDataRow> itc = row.getChildRows();
		if (itc != null) {
			while (itc.hasNext()) {
				JDataRow crow = itc.next();
				if (isParentRow(crow))
					return true;
			}
		}
		// Parent Rows
		Iterator<JDataRow> itp = row.getParentRows();
		if (itp != null) {
			while (itp.hasNext()) {
				JDataRow prow = itp.next();
				if (isParentRow(prow))
					return true;
			}
		}
		return false;
	}
}
