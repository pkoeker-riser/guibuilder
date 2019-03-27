package de.guibuilder.framework;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import de.jdataset.JDataSet;
import de.pkjs.util.Convert;
import electric.xml.Attribute;
import electric.xml.Element;
import electric.xml.Elements;

/**
 * Implementierung einer Zeile in einer Tabelle.
 * <p>
 * Wichtige Methoden:
 * <ul>
 * <li>getValue
 * <li>setValue
 * <li>isModified
 * <li>isInserted
 * <li>isDeleted
 * 
 * @see GuiTable
 */
public class GuiTableRow {
	/**
	 * My Parent Table
	 */
	private GuiTable myTable;

	/**
	 * Ausgabeformat für Zahlen dieser Zeile oder null, wenn Tabelle spaltenorientiert.
	 * TODO : nicht implementiert!
	 */
	private String format;

	/**
	 * Kennzeichen, ob die Zeile editierbar ist.
	 */
	private boolean editable = true;
	private boolean modified = false;
	private boolean inserted = false;
	private boolean deleted = false;
	private long oid = -1;
	
	/**
	 * Verweist auf das laufende Element im DataSet
	 */
	private int modelElementNumber = -1;
	private Vector<Object> data; // TODO: Spalten vertauschen wie?

	// Constructor
	GuiTableRow(GuiTable tbl) {
		this.myTable = tbl;
		data = new Vector<Object>();
	}

	/**
	 * Erzeugt eine Tabellenzeile mit der angegebenen Inhalt.
	 */
	public GuiTableRow(GuiTable tbl, Vector<Object> v) {
		this.myTable = tbl;
		this.data = v;
	}

	/**
	 * Erzeugt eine Tabellenzeile mit der angegebenen Anzahl von leeren Spalten.
	 */
	public GuiTableRow(GuiTable tbl, int columns) {
		this.myTable = tbl;
		this.data = new Vector<Object>(columns);
		for (int i = 0; i < columns; i++) {
		   // Dieses führt dazu, daß die Zeile mit den Default-Werten
		   // aus den Spalten (Combo, val="...") initialisiert wird.
//		   GuiTable.GuiTableColumn col = this.myTable.getColumn(i);
//		   Object o = col.getGuiComponent().getValue(); 
//		   data.addElement(o);
			data.addElement(null); // obiges geht leider nicht!
		}
	}

	// Methods
	void setParentTable(GuiTable tbl) {
		this.myTable = tbl;
	}

	GuiTable getParentTable() {
		return myTable;
	}

	/**
	 * Das hier angegebene XML-Element muß im Format von getElement() sein.
	 * 
	 * @see #getElement
	 * @param tbl
	 * @param rowEle
	 */
	public GuiTableRow(GuiTable tbl, Element rowEle) {
		this.myTable = tbl;
		data = new Vector<Object>();
		// Vector für jede Zeile
		// (die sollten "Component" heißen, was hier aber nicht geprüft wird)
		// Editable?
		{
			Attribute att = rowEle.getAttributeObject("editable");
			if (att != null) {
				this.setEditable(Boolean.valueOf(att.getValue()).booleanValue());
			}
		}
		// Format?
		{
			Attribute att = rowEle.getAttributeObject("format");
			if (att != null) {
				this.setFormat(att.getValue());
			}
		}
		// oid
		final String roid = rowEle.getAttributeValue(GuiMember.OID);
		if (roid != null) {
			this.setOid(Convert.toLong(roid));
		}
		// Inserted / Modified / Deleted? // New 26.12.2003 PKÖ
		{
			Attribute attIns = rowEle.getAttributeObject("inserted");
			if (attIns != null && attIns.getValue().equals("true")) {
				this.setInserted(true);
			}
		}
		{
			Attribute attMod = rowEle.getAttributeObject("modified");
			if (attMod != null && attMod.getValue().equals("true")) {
				this.setModified(true);
			}
		}
		// Deleted
		{
			Attribute attDel = rowEle.getAttributeObject("deleted");
			if (attDel != null && attDel.getValue().equals("true")) {
				this.setDeleted(true);
			}
		}
		// ModelElementNum
		{
			Attribute attNum = rowEle.getAttributeObject("modelElementNumber");
			if (attNum != null) {
				this.setModelElementNumber(Convert.toInt(attNum.getValue()));
			}
		}
		// Cells
		Elements cells = rowEle.getElements();
		while (cells.hasMoreElements()) {
			Element cellNode = cells.next();
			String val = cellNode.getTextString();
			this.getData().addElement(val);
		} // end of columns
	}

	/**
	 * Liefert "Row"
	 */
	public final String getTag() {
		return "Row";
	}

	/**
	 * Liefert die Daten dieser Zeile als Vector.
	 * 
	 * @return
	 */
	public Vector<Object> getData() {
		return data;
	}

	/**
	 * Setzt den Inhalt der Zeile komplett neu. Der Vector muß so breit sein, wie die
	 * Tabelle Spalten hat.
	 * 
	 * @param d
	 */
	public void setData(Vector<Object> d) {
		data = d;
	}
	/**
	 * Setzt den Inhalt der Zeile komplett neu. Das StringArray muß so groß sein, wie die
	 * Tabelle Spalten hat.
	 * 
	 * @param d
	 */
	public void setData(String[] d) {
		data = new Vector<Object>(d.length);
		for (int i = 0; i < d.length; i++) {
			data.add(d[i]);
		}
	}

	/**
	 * Liefert die Eigenschaften dieser Zeile als XML-Element. Dieses ist so aufgebaut, daß
	 * es für den entsprechenden Constructor dieser Klasse verwendet werden kann.
	 * 
	 * @see GuiTable#getAllValuesXml
	 * @return
	 */
	public Element getElement() {
		Element rowEle = new Element("Row");
		// editable, modified, inserted
		rowEle.setAttribute("editable", Convert.toString(this.isEditable()));
		if (this.isDeleted()) {
			rowEle.setAttribute("deleted", "true");
		}
		if (this.isInserted()) {
			rowEle.setAttribute("inserted", "true");
		} else if (this.isModified()) {
			rowEle.setAttribute("modified", "true");
		}
		if (this.getOid() != -1) {
			rowEle.setAttribute(GuiMember.OID, Long.toString(this.getOid()));
		}
		// Format?
		if (this.getFormat() != null) {
			rowEle.setAttribute("format", this.getFormat());
		}
		if (this.modelElementNumber != -1) {
			rowEle.setAttribute("modelElementNumber", Convert.toString(this
					.getModelElementNumber()));
		}
		// Values
		Vector<String> header = myTable.getColumnIdentifiers();
		for (int i = 0; i < this.getData().size() && i < header.size(); i++) {
			String val = "";
			if (this.getValueAt(i) == null) {
				val = "";
			} else {
				val = this.getValueAt(i).toString();
			}
			String name = header.elementAt(i); // TODO: String? Object?
			if (GuiUtil.getDebug() == true) {
				System.out.println("TableColumn:" + name);
			}
			Element ele = rowEle.addElement("Component");
			ele.setAttribute("name", name);
			// Element-Inhalt setzen
			ele.addText(val);
		} // End Loop row
		return rowEle;
	}

	/**
	 * Liefert den Inhalt der Spalte unter Angabe ihres Namens.
	 * 
	 * @param columnName
	 *           Name der Column
	 * @return üblicherweise ein String, Date, Boolean, Integer, Long, Double, Float
	 */
	public Object getValue(String columnName) {
		int index = this.myTable.getColumnIndex(columnName);
		return this.getValueAt(index);
	}

	/**
	 * Liefert den Inhalt der Spalte unter Angabe der Column-Number.
	 * 
	 * @param col
	 *           Der Index der Spalte (0-relativ).
	 * @return
	 */
	public Object getValueAt(int col) {
		if (data != null && data.size() > col) {
			Object o = data.elementAt(col);
			return o;
		} else {
			return null;
		}
	}

	/**
	 * Setzt den Inhalt der Column unter Angabe des Column Namens.
	 * 
	 * @param columnName
	 * @param value
	 */
	public void setValue(String columnName, Object value) {
		int index = this.myTable.getColumnIndex(columnName);
		this.setValueAt(index, value);
	}

	public void setValue(String columnName, boolean value) {
		int index = this.myTable.getColumnIndex(columnName);
		this.setValueAt(index, value);
	}

	public void setValue(String columnName, int value) {
		int index = this.myTable.getColumnIndex(columnName);
		this.setValueAt(index, value);
	}

	public void setValue(String columnName, long value) {
		int index = this.myTable.getColumnIndex(columnName);
		this.setValueAt(index, value);
	}

	public void setValue(String columnName, double value) {
		int index = this.myTable.getColumnIndex(columnName);
		this.setValueAt(index, value);
	}

	public void setValue(String columnName, float value) {
		int index = this.myTable.getColumnIndex(columnName);
		this.setValueAt(index, value);
	}
  public void setValue(String columnName, BigDecimal value) {
    int index = this.myTable.getColumnIndex(columnName);
    this.setValueAt(index, value);
  }
  public void setValue(String columnName, Date value) {
    int index = this.myTable.getColumnIndex(columnName);
    String sValue = Convert.toString(value);
    this.setValueAt(index, sValue);
  }
  public void setValue(String columnName, Timestamp value) {
     int index = this.myTable.getColumnIndex(columnName);
     String sValue = Convert.toString(value);
     this.setValueAt(index, sValue);
   }
  public void setValue(String columnName, Time value) {
    int index = this.myTable.getColumnIndex(columnName);
    String sValue = Convert.toString(value);
    this.setValueAt(index, sValue);
  }
	/**
	 * Setzt den Wert der angegebenen Spalte
	 * @param col
	 *           Der Index der Spalte (0-relativ).
	 * @param value
	 */
  public void setValueAt(int col, Object value) {
    setValueAt(col, value, true);
  }
	public void setValueAt(int col, Object value, boolean select) {
		if (data != null && data.size() > col) {
			data.setElementAt(value, col);
			if (!this.isModified()) {
			  this.setModified(true);
			}
			if (select) { // 19.5.2008: nur wenn gewünscht
  			// Neu damits auch sichtbar wird: PKÖ 19.10.2003
  			GuiTable.GuiTableModel mdl = myTable.getGuiTableModel();
  			// daß man hier die selektierte Zeile sich merken muß ...
  			int index = myTable.getSelectedRow();
  			mdl.fireTableDataChanged();
  			// ... und hier wieder zuweisen, das glaubt einem kein Mensch!
  			// Sonst ist nämlich die selektierte Zeile der Tabelle wieder -1!!
  			// KKN 20.03.2004:
  			// Durch das setzen der seletierten Zeile wird das OnRowClick-Event
  			// für jede Zeile der Tabelle ausgelöst.
  			// Das führt im Controller ggf. zu unnötigen Verarbeitungen, wenn
  			// dort auf das OnRowClick-Ereignis reagiert wird !!
  			// 30.4.2005 PKÖ:
  			// Die Tabelle merkt sich jetzt die zuletzt selektierte Zeile
  			// und läßt Ereignisse nur aus, wenn wirklich eine andere Zeile
  			// selektiert wurde.
  			myTable.setSelectedRow(index);
			}
		}
	}

	/**
	 * Setzt den Inhalt einer CheckBox in der TabellenZeile.
	 * 
	 * @param col
	 *           Der Index der Spalte (0-relativ).
	 * @param value
	 */
  public void setValueAt(int col, boolean value) {
    this.setValueAt(col, value, true);
  }
	public void setValueAt(int col, boolean value, boolean select) {
		Boolean b = Boolean.valueOf(value);
		this.setValueAt(col, b, select);
	}
	/**
	 * Wird als Integer gespeichert.
	 */
	public void setValueAt(int col, int value) {
		Integer i = new Integer(value);
		this.setValueAt(col, i);
	}
	/**
	 * Wird als Long gespeichert
	 * @param col
	 * @param value
	 */
	public void setValueAt(int col, long value) {
		Long l = new Long(value);
		this.setValueAt(col, l);
	}
	/**
	 * Wird als Double übernommen.
	 * @param col
	 * @param value
	 */
	public void setValueAt(int col, double value) {
		Double d = new Double(value);
		this.setValueAt(col, d);
	}
	/**
	 * Wird als Float übernommen.
	 * @param col
	 * @param value
	 */
	public void setValueAt(int col, float value) {
		Float f = new Float(value);
		this.setValueAt(col, f);
	}

	String getLabel() {
		return (String) data.elementAt(0);
	}

	void setLabel(String p_Label) {
		data.setElementAt(p_Label, 0);
	}

	void insertColumn(int col) {
		data.insertElementAt(null, col);
	}

	void deleteColumn(int col) {
		data.removeElementAt(col);
	}

	String getFormat() {
		return format;
	}

	void setFormat(String p_Format) {
		format = p_Format;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean p_Editable) {
		editable = p_Editable;
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean p_Modified) {
		modified = p_Modified;
	}

	/**
	 * Liefert "true", wenn die Zeile vom Benutzer hinzugefügt wurde.
	 * 
	 * @see GuiTable#insertRow
	 */
	public boolean isInserted() {
		return inserted;
	}

	public void setInserted(boolean p_Inserted) {
		inserted = p_Inserted;
	}

	public boolean isDeleted() {
		return deleted;
	}

	void setDeleted(boolean p_Deleted) {
		deleted = p_Deleted;
	}

	/**
	 * Setzt die beliebige Oid.
	 */
	public final void setOid(long id) {
		oid = id;
	}

	/**
	 * Liefert die Oid. Wenn -1, dann ist keine Oid gesetzt.
	 */
	public final long getOid() {
		return oid;
	}

	/**
	 * für die Position dieser Zeile im DataSet oder -1, wenn nicht Teil eines DataSet.
	 * <p>
	 * Diese Information wird benötigt, wenn die Tabelle umsortiert wird.
	 * 
	 * @see GuiTable#setDatasetValues(JDataSet)
	 * @return
	 */
	public final int getModelElementNumber() {
		return modelElementNumber;
	}

	/**
	 * Setzt die Position dieser Zeile im Dataset.
	 * 
	 * @see #getModelElementNumber()
	 * @param n
	 */
	public final void setModelElementNumber(int n) {
		modelElementNumber = n;
	}

	/**
	 * Erzeugt eine Kopie dieses Objekts.
	 * <p>
	 * Achtung! <br>
	 * Die Zeile bleibt mit ihrer ParentTable assoziiert!
	 * 
	 * @see #getElement
	 * @return
	 */
	public GuiTableRow guiClone() {
		GuiTableRow row = new GuiTableRow(this.myTable, this.getElement());
		return row;
	}

	/**
	 * Setzt die Eigenschaften modified, inserted und deleted auf "false".
	 *  
	 */
	public void commitChanges() {
		this.setModified(false);
		this.setInserted(false);
		this.setDeleted(false);
	}

	public String toString() {
		if (data == null) {
			return "[]";
		} else {
			return data.toString();
		}
	}
}