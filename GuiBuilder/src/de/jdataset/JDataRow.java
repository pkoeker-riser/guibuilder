package de.jdataset;

import de.pkjs.pl.TableRequest;
import de.pkjs.util.Convert;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Eine Datenzeile einer Tabelle die aus einer Liste von Werten besteht.
 * <p>
 * Gemäß der definierten Struktur des DataSet kann eine DataRow Child - und Parent Rows haben.
 * @see JDataValue
 */
public final class JDataRow implements Serializable, RowContainer {
   // Attributes
   private JDataTable myTable; 
   //private JDataSet myDataSet; // nur bei root Rows gesetzt!
   private boolean inserted;
   private boolean deleted;
   private ArrayList<JDataValue> dataValues;
   private ArrayList<JDataRow> childRows;
   private ArrayList<JDataRow> parentRows;
   private JDataRow myParentRow; // wenn root, dann null

   static final long serialVersionUID = -1972104073073595873L;

   // Constructor
   /**
    * @deprecated For serialization purpose only.
    */
   public JDataRow() {
   }

   /**
    * Erzeugt eine neue DataRow für die angegebene Tabelle mit den angegebenen Werten.
    * <p>
    * Es wird eine ArrayList von DataValues erwartet. Die DataTable darf nicht null sein.
    */
   public JDataRow(JDataTable tbl, ArrayList<JDataValue> dataValues) {
      if(tbl == null) {
         throw new IllegalArgumentException("DataTable is null");
      }
      this.myTable = tbl;
      this.dataValues = dataValues;
   }

   JDataRow(JDataTable tbl) {
      this.myTable = tbl;
      // Columns mit Default
      Iterator<JDataColumn> i = myTable.getDataColumns();
      if(i == null) {
         throw new IllegalArgumentException("DataTable '" + tbl.getTablename()
               + "'contains no Columns!");
      }
      while(i.hasNext()) {
         JDataColumn col = i.next();
         JDataValue val = new JDataValue(col, col.getDefaultValue());
         this.addDataValue(val);
      }
   }

   /**
    * Für Erstellung eines DataSet aus XML
    * @param tbl
    * @param ele
    */
   JDataRow(JDataTable tbl, Element ele) {
      this.myTable = tbl;
      // Wenn die Attribute fehlen liefert Convert "false"
      inserted = Convert.toBoolean(ele.getAttribute("inserted"));
      deleted = Convert.toBoolean(ele.getAttribute("deleted"));
      // Values
      Elements eles = ele.getElements();
      eles.first(); // Falls wiederholt gelesen wird
      while(eles.hasMoreElements()) {
         Element valEle = eles.next();
         String tabletype = valEle.getAttribute("rowtype");
         if(tabletype == null) { // Value
            String colname = valEle.getName();
            JDataColumn col = myTable.getDataColumn(colname);
            JDataValue value = new JDataValue(col, valEle);
            // Für Child und Parent Tables,
            // brauchen wir das Attribut "tabletype"
            this.addDataValue(value);
         }
         else {
            if(tabletype.equals("child")) {
               String childRefname = valEle.getName();
               String ref = valEle.getAttribute("refname");
               if(ref != null)
                  childRefname = ref;
               JDataTable childTable = this.myTable.getChildTable(childRefname);
               JDataRow childRow = new JDataRow(childTable, valEle);
               this.addChildRow(childRow);
            }
            else if(tabletype.equals("parent")) {
               String parentRefname = valEle.getName();
               String ref = valEle.getAttribute("refname");
               if(ref != null)
                  parentRefname = ref;
               JDataTable parentTable = this.myTable.getParentTable(parentRefname);
               JDataRow parentRow = new JDataRow(parentTable, valEle);
               this.addParentRow(parentRow);
            }
            else if(tabletype.equals("node")) {
               // TODO : parentElementName
            }
         }
      }
   }
   /**
    * Erzeugt einen Clone aus der angegebenen DataRow
    * @param tbl
    * @param origRow
    */
   JDataRow(JDataTable tbl, JDataRow origRow) {
      this.myTable = tbl;
      this.deleted = origRow.deleted;
      this.inserted = origRow.inserted;
      // DataValues
      Iterator<JDataValue> itv = origRow.getDataValues();
      if (itv != null) {
         while(itv.hasNext()) {
            JDataValue origVal = itv.next();
            JDataValue cloneVal = new JDataValue(tbl.getDataColumn(origVal.getColumnName()),origVal); 
            this.addDataValue(cloneVal); // Clone from DataValue
         }
      }
      // ChildRows
      Iterator<JDataRow> itc = origRow.getChildRows();
      if (itc != null) {
         while (itc.hasNext()) {
            JDataRow childRow = itc.next();
            JDataTable childTable = this.myTable.getChildTable(childRow.getDataTable().getRefname());
            JDataRow cloneRow = new JDataRow(childTable, childRow);
            this.addChildRow(cloneRow);
         }
      }
      // ParentRows
      Iterator<JDataRow> itp = origRow.getParentRows();
      if (itp != null) {
         while (itp.hasNext()) {
            JDataRow parentRow = itp.next();
            JDataTable parentTable = this.myTable.getParentTable(parentRow.getDataTable().getRefname());
            JDataRow cloneRow = new JDataRow(parentTable, parentRow);
            this.addParentRow(cloneRow);
         }
      }
   }
   
   void setDataTable(JDataTable tbl) {
   	this.myTable = tbl;
   }

   // Methods
   Element getElement(Element parentElement, boolean modifiedOnly) {
      Element ele = parentElement;
      // bei join oder suppress kein neues Element
      if(this.myTable.getJoin() == null) {
         ele = new Element(myTable.getTablename());
         parentElement.addElement(ele);
         if(inserted) {
            ele.setAttribute("inserted", "true");
         }
         if(deleted) {
            ele.setAttribute("deleted", "true");
         }
         String type = "***missing***";
         switch(this.myTable.getTableType()) {
            case JDataTable.ROOT_TABLE:
               type = "root";
               break;
            case JDataTable.CHILD_TABLE:
               type = "child";
               break;
            case JDataTable.PARENT_TABLE:
               type = "parent";
               break;
         }
         ele.setAttribute("rowtype", type);
         if(myTable.getTablename().equals(myTable.getRefname()) == false) {
            ele.setAttribute("refname", myTable.getRefname());
         }
      } // End If inline
      // Loop Values
      if(this.dataValues != null) {
         for(JDataValue value: dataValues) {
           boolean modified = value.isModified();
           if (modified || !modifiedOnly) {             
             ele.addElement(value.getElement());
           }
         }
      }
      // Child Rows
      if(childRows != null) {
         for(JDataRow childRow: childRows) {
            childRow.getElement(ele, modifiedOnly);
         }
      }
      // Parent Rows
      if(parentRows != null) {
         for(JDataRow parentRow: parentRows) {
            parentRow.getElement(ele, modifiedOnly);
         }
      }
      return ele;
   }

   /**
    * Wird von addChild und addParent verwendet.
    * @param parent
    */
   void setParentRow(JDataRow parent) {
      this.myParentRow = parent;
   }
   
   /**
    * Liefert true, wenn diese Row bezogen auf ihren Context im DataSet die Rolle einer ParentRow einnimmt.
    * Also nicht in die Datenbank zurückgeschrieben wird.
    * @return
    */
   boolean isParentRow() {
   	if (myParentRow != null && myParentRow.getDataTable().isParentTable(this.getDataTable())) {
   		return true;
   	}
   	return false;
   }

   public void addDataValue(JDataValue val) {
      if(this.dataValues == null) {
         this.dataValues = new ArrayList<JDataValue>();
      }
      this.dataValues.add(val);
   }

   /**
    * Die Zeile wird hier nicht wirklich gelöscht, sondern nur als gelöscht markiert.
    * <p>
    * Bewirkt einen DELETE in der Datenbank, wenn true.
    * <p>
    * Die Row wird erst wirklich gelöscht, wenn im DataSet commitChanges() aufgerufen wird.
    * <p>
    * TODO: Neue Zeilen, denen nach einem Insert in der Oberfläche weiterhin der Primärschlüssel
    * fehlt, dürfen so nicht gelöscht werden!
    * <p>
    * Es kann bei ChildTables eine delete-Rule definiert werden (OnDelete="..."):
    * <ul>
    * <li>Cascade: Lüscht die abhängigen Daten
    * <li>SetNull: Setzt die Felder des Fremdschlüssels auf NULL
    * <li>SetDefault: Setzt die Felder des Fremdschlüssels auf ihren Default-Wert zurück.
    * <li>Restrict: Wirft eine IllegalStateException, wenn abhängige Daten vorhanden sind
    * <li>NoAction: Tut nichts
    * </ul>
    * @param b
    * @see JDataSet#commitChanges
    */
   public void setDeleted(boolean b) {
      deleted = b;
      if(this.childRows != null) {
         for(JDataRow childRow: childRows) {
            Relation rela = this.getDataTable().getChildRelation(
                  childRow.getDataTable().getRefname());
            int onDelete = DatabaseMetaData.importedKeyCascade;
            if(rela != null) {
               onDelete = rela.getDeleteRule();
            }
            switch(onDelete) {
               case DatabaseMetaData.importedKeyCascade:
                  childRow.setDeleted(b);
                  break;
               case DatabaseMetaData.importedKeySetNull:
                  if(rela != null) {
                     Iterator<JDataColumn> it = rela.getChildColumns();
                     while(it.hasNext()) {
                        JDataColumn col = it.next();
                        JDataValue val = childRow.getDataValue(col.getColumnName());
                        if(b) {
                           val.setValue((String)null);
                        }
                        else {
                           val.rollbackChanges();
                        }
                     }
                  }
                  break;
               case DatabaseMetaData.importedKeySetDefault:
                  if(rela != null) {
                     Iterator<JDataColumn> it = rela.getChildColumns();
                     while(it.hasNext()) {
                        JDataColumn col = it.next();
                        JDataValue val = childRow.getDataValue(col.getColumnName());
                        if(b) {
                           val.setValue((String)null);
                        }
                        else {
                           val.rollbackChanges();
                        }
                     }
                  }
                  break;
               case DatabaseMetaData.importedKeyRestrict:
                  if(b) {
                     throw new IllegalStateException("Delete restricted! " + rela.getRefName());
                  }
                  else {
                     // ??
                  }
               case DatabaseMetaData.importedKeyNoAction:
                  // Nix machen :-)
                  break;
               default:
                  System.out.println("Warning: Unsupported delete rule " + onDelete);
                  break;
            }
         }
      }
   }

   /**
    * Liefert true, wenn diese Zeile als zu löschen markiert ist.
    * @return
    */
   public boolean isDeleted() {
      return deleted;
   }

   /**
    * Bewirkt einen INSERT in die Datenbank, wenn 'true'
    * @param b
    */
   public void setInserted(boolean b) {
      inserted = b;
   }

   /**
    * Liefert 'true', wenn diese Zeile als neu markiert ist.
    * @return
    */
   public boolean isInserted() {
      return inserted;
   }

   /**
    * Liefert den RowState aus den Zuständen INSERTED (1), DELETED (2), MODIFIED (4) bzw.
    * Kombinationen davon (3,5,6,7) oder UNCHANGED (8)
    * @see DataView
    * @return
    */
   public int getRowState() {
      int state = 0;
      if(this.isInserted()) {
         state = state | DataView.INSERTED;
      }
      if(this.isDeleted()) {
         state = state | DataView.DELETED;
      }
      if(this.isModified()) {
         state = state | DataView.MODIFIED;
      }
      if(state == 0) {
         state = DataView.UNCHANGED;
      }
      return state;
   }

   /**
    * Liefert die Spaltendefinition zu der angegebenen Column.
    * @param columnName
    * @return
    */
   public JDataColumn getDataColumn(String columnName) {
      return myTable.getDataColumn(columnName);
   }

   /**
    * Liefert true zurück, falls eine Spalte mit dem übergebenen Namen exisitiert
    * @param columnName Name der gesuchten Spalte
    * @return
    */
   public boolean hasColumn(String columnName) {
      if(columnName == null || columnName.length() == 0) {
         throw new IllegalArgumentException("Parameter columnName is null or empty!");
      }
      if(this.dataValues != null) {
         for(JDataValue val: this.dataValues) {
            if(val.getColumnName().equalsIgnoreCase(columnName)) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Liefert den DataValue zu dem angegebenen Spaltennamen. Auch in der Notation
    * #ParentReference#GrandParent@columnName
    * @param columnName
    * @return DataValue oder null, wenn columnName ungültig
    */
   public JDataValue getDataValue(String columnName) {
      // #Parent Tables aufknabbern
      if(columnName.startsWith("#")) { // Neu 26.8.2003 // Koorigiert 26.10.2003
         columnName = columnName.substring(1);
         int poi1 = columnName.indexOf("@");
         if(poi1 == -1)
            throw new IllegalArgumentException(
                  "JDataRow#getDataValue(String columnName): Missing Column Separator '@': "
                        + columnName);
         int poi2 = columnName.indexOf("#");
         int poi = poi1;
         if(poi2 != -1 && poi2 < poi1) {
            poi = poi2;
         }
         if(poi != -1) {
            final String parentRef = columnName.substring(0, poi);
            columnName = columnName.substring(poi);
            JDataRow parentRow = this.getParentRow(parentRef);
            return parentRow.getDataValue(columnName); // Knabbert ggf. weiter an Parent Tables
                                                         // rum
         }
      }
      else if(columnName.startsWith(".")) { // Neu 28.01.2004
         columnName = columnName.substring(1);
         int poi1 = columnName.indexOf("@");
         if(poi1 == -1)
            throw new IllegalArgumentException(
                  "JDataRow#getDataValue(String columnName): Missing Column Separator '@': "
                        + columnName);
         int poi2 = columnName.indexOf(".");
         int poi = poi1;
         if(poi2 != -1 && poi2 < poi1) {
            poi = poi2;
         }
         if(poi != -1) {
            final String childRef = columnName.substring(0, poi);
            columnName = columnName.substring(poi);
            JDataRow childRow = this.getChildRow(childRef, 0);
            return childRow.getDataValue(columnName); // Knabbert ggf. weiter an Child Tables rum
         }
      }
      // @Column / @p0 bei HSQLDB
      if(columnName.startsWith("@")) {
        for(JDataValue val: this.dataValues) {
          if(val.getColumnName().equalsIgnoreCase(columnName)) {
             return val;
          }
       }
       columnName = columnName.substring(1); // HACK
      }
      if(this.dataValues != null) {
         for(JDataValue val: this.dataValues) {
            if(val.getColumnName().equalsIgnoreCase(columnName)) {
               return val;
            }
         }
      }
      throw new IllegalArgumentException(
            "JDataRow#getDataValue(String columnName): Missing Column Name: '" + columnName
                  + "' (in DataTable: '" + this.getDataTable().getTablename() + "')");
   }

   /**
    * Liefert den DataValue der angegebenen Spalten-Nummer (0-relativ);
    * @param colIndex
    * @return
    */
   public JDataValue getDataValue(int colIndex) {
      if(colIndex > this.getColumnCount()) {
         throw new IllegalArgumentException(
               "JDataRow#getDataValue(int index) Column Index out of Range: " + colIndex);
      }
      JDataValue val = dataValues.get(colIndex);
      return val;
   }

   /**
    * Liefert den Wert der angegebenen Spalte.
    * <p>
    * Wirft eine IllegalArgumentException, wenn columnName ungültig ist.
    * @param columnName
    * @return Den Wert als String oder null
    */
   public String getValue(String columnName) {
      JDataValue val = this.getDataValue(columnName);
      if (!val.isBinary()) { // bei binary bitte kein trim!
      	String sVal = val.getValue();
      	if(sVal != null && sVal.trim().length() == 0) // Oh weh! Das kann auch verwirren!
      		return null; 
      }
      return val.getValue();
   }

   /**
    * Liefert den Inhalt der angegebenen Spalte als int.
    * @see JDataValue#getValueInt()
    * @param columnName
    * @return
    */
   public int getValueInt(String columnName) {
      JDataValue val = this.getDataValue(columnName);
      return val.getValueInt();
   }

   /**
    * @see JDataValue#getValueLong()
    * @param columnName
    * @return
    */
   public long getValueLong(String columnName) {
      JDataValue val = this.getDataValue(columnName);
      return val.getValueLong();
   }

   /**
    * @see JDataValue#getValueBool()
    * @param columnName
    * @return
    */
   public boolean getValueBool(String columnName) {
      JDataValue val = this.getDataValue(columnName);
      return val.getValueBool();
   }

   /**
    * @see JDataValue#getValueDouble()
    * @param columnName
    * @return
    */
   public double getValueDouble(String columnName) {
      JDataValue val = this.getDataValue(columnName);
      return val.getValueDouble();
   }

   /**
    * Liefert den Wert der Spalte als BigDecimal
    * @see JDataValue#getValueBigDecimal()
    * @param columnName
    * @return
    */
   public BigDecimal getValueBigDecimal(String columnName) {
      JDataValue val = this.getDataValue(columnName);
      return val.getValueBigDecimal();
   }

   /**
    * @see JDataValue#getValueDate()
    * @param columnName
    * @return
    */
   public Date getValueDate(String columnName) {
      JDataValue val = this.getDataValue(columnName);
      return val.getValueDate();
   }
   public Timestamp getValueTimestamp(String columnName) {
       JDataValue val = this.getDataValue(columnName);
       return val.getValueTimestamp();
    }
   /**
    * Setzt den Wert der angegebenen Spalte neu.
    * @param columnName
    * @param value
    * @return Der übergebene Wert
    */
   public String setValue(String columnName, String value) {
      JDataValue val = this.getDataValue(columnName);
      return val.setValue(value);
   }

   /**
    * Setzt den Wert der angegebenen Spalte neu.
    * @param columnName
    * @param value
    * @return
    */
   public String setValue(String columnName, long value) {
      JDataValue val = this.getDataValue(columnName);
      return val.setValue(value);
   }

   /**
    * Setzt den Wert der angegebenen Spalte neu.
    * @param columnName
    * @param value
    * @return
    */
   public String setValue(String columnName, int value) {
      JDataValue val = this.getDataValue(columnName);
      return val.setValue(value);
   }

   /**
    * Setzt den Wert der angegebenen Spalte neu.
    * @param columnName
    * @param value
    * @return
    */
   public String setValue(String columnName, boolean value) {
      JDataValue val = this.getDataValue(columnName);
      return val.setValue(value);
   }

   /**
    * Setzt den Wert der angegebenen Spalte neu.
    * @param columnName
    * @param value
    * @return
    */
   public String setValue(String columnName, double value) {
      JDataValue val = this.getDataValue(columnName);
      return val.setValue(value);
   }

   /**
    * Setzt den Wert der angegebenen Spalte neu.
    * @param columnName
    * @param value
    * @return
    */
   public String setValue(String columnName, BigDecimal value) {
      JDataValue val = this.getDataValue(columnName);
      return val.setValue(value);
   }

   /**
    * Setzt den Wert der angegebenen Spalte neu.
    * @param columnName
    * @param value
    * @return
    */
   public String setValue(String columnName, java.util.Date value) {
      JDataValue val = this.getDataValue(columnName);
      return val.setValue(value);
   }
   public String setValue(String columnName, Timestamp value) {
       JDataValue val = this.getDataValue(columnName);
       return val.setValue(value);
    }

   /**
    * Setzt den Wert der angegebenen Spalte neu.
    * @param columnName
    * @param value
    * @return
    */
   public String setValue(String columnName, Object value) {
      JDataValue val = this.getDataValue(columnName);
      return val.setValue(value);
   }

   /**
    * Liefert die Zahl der Spalten dieser Zeile.
    * @return -1, wenn keine Spalten definiert.
    */
   public int getColumnCount() {
      if(this.dataValues == null) {
         return -1;
      }
      else {
         return this.dataValues.size();
      }
   }

   /**
    * Liefert den Wert der angegebenen Spalten-Nummer (0-relativ);
    * @param colIndex
    * @return
    */
   public String getValue(int colIndex) {
      JDataValue val = this.getDataValue(colIndex);
      return val.getValue();
   }

   /**
    * Setzt den Wert der angegebenen Spaltennummer (0-relativ);
    * @param colIndex
    * @param value
    */
   public void setValue(int colIndex, String value) {
      JDataValue val = this.getDataValue(colIndex);
      val.setValue(value);
   }

   /**
    * Liefert 'true', wenn einer der Werte dieser Zeile geändert wurde.
    * @see JDataValue#isModified
    * @return
    */
   public boolean isModified() {
      if(this.dataValues != null) {
         for(JDataValue val: this.dataValues) {
            if(val.isModified()) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Liefert nur dann true, wenn sich eine persistente Spalte geändert hat.
    * @see JDataValue#isModifiedPersistent()
    * @return
    */
   public boolean isModifiedPersistent() {
      if(this.dataValues != null) {
        for(JDataValue val: this.dataValues) {
            if(val.isModifiedPersistent()) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Liefert 'true', wenn sich der Wert mit dem angegebenen Name geändert hat. Dieses würde zu
    * einem UPDATE in der Datenbank führen.
    * @param columnName
    * @return
    */
   public boolean isModified(String columnName) {
      JDataValue val = this.getDataValue(columnName);
      return val.isModified();
   }
   
   /**
    * Liefert true, wenn sich diese Row oder eine ihrer ChildRows geändert haben
    * @return
    */
   public boolean isModifiedChild() {
     if (this.isInserted() && !this.isDeleted()) return true;
     if (this.isDeleted()) return true;
     if (this.isModified()) return true;
     Iterator<JDataRow> it = this.getChildRows();
     if (it != null) {
       while(it.hasNext()) {
         JDataRow childRow = it.next();
         if (childRow.isModifiedChild()) return true;
       }
     }
     return false;
   }
   /**
    * Liefert nur dann true, wenn sich eine für die Persistenz relevante Änderung ergeben hat.
    * @return
    */
   public boolean isModifiedChildPersistent() {
     if (this.isInserted() && !this.isDeleted()) return true;
     if (this.isDeleted()) return true;
     if (this.isModifiedPersistent()) return true;
     Iterator<JDataRow> it = this.getChildRows();
     if (it != null) {
       while(it.hasNext()) {
         JDataRow childRow = it.next();
         if (childRow.isModifiedChildPersistent()) return true;
       }
     }
     return false;
   }

   public void addChildRow(JDataRow row) {
      if(this.myTable.isChildTable(row.getDataTable()) == false) {
         throw new IllegalArgumentException("JDataRow#addChildRow DataTable '"
               + row.getDataTable().getTablename() + "' isnt a Child Table");
      }
      if(this.childRows == null) {
         this.childRows = new ArrayList<JDataRow>();
      }
      else {
         if(childRows.contains(row)) {
            String msg = "ChildRow already added! " + row;
            throw new IllegalStateException(msg);
         }
      }
      this.childRows.add(row);
      row.setParentRow(this);
   }

   public void addChildRow(JDataRow row, int index) {
      if(this.myTable.isChildTable(row.getDataTable()) == false) {
         throw new IllegalArgumentException("JDataRow#addChildRow DataTable '"
               + row.getDataTable().getTablename() + "' isnt a Child Table");
      }
      if(this.childRows == null) {
         this.childRows = new ArrayList<JDataRow>();
      }
      if(childRows.size() < index || index < 0) {
         throw new IllegalArgumentException("Index out of Range: " + childRows.size());
      }
      this.childRows.add(index, row);
      row.setParentRow(this);
   }

   /*
    * public void addChildRows(Iterator it) { if (it == null) return; while (it.hasNext()) {
    * JDataRow row = (JDataRow)it.next(); this.addChildRow(row.cloneRow()); } }
    */
   /**
    * @deprecated
    * @see createChildRow
    */
   public JDataRow addNewChildRow(String refname) {
      return this.createChildRow(refname);
   }

   /**
    * Fügt einen neue leere Child Row hinzu unter Angabe des Child Reference Namens.
    * <p>
    * Um die Zuweisung der Werte von Foreign und Primary Keys braucht man sich hier nicht zu
    * kümmern; dieses erledigt der Persistenz-Layer automatisch. Naturgemäß sind diese Felder aber
    * erst dann gefüllt, wenn sie wieder neu aus der Datenbank eingelesen werden.
    * <p>
    * @param refname Der Name der Child Table Reference
    * @return Die leere Child Row
    */
   public JDataRow createChildRow(String refname) {
      JDataTable childTable = this.myTable.getChildTable(refname);
      JDataRow newRow = childTable.createNewRow();
      this.addChildRow(newRow);
      return newRow;
   }

   /**
    * Fügt eine neue leere Child Row hinzu.
    * <p>
    * Wirft eine IllegalStateException, wenn nicht genau eine ChildTable definiert ist.
    */
   public JDataRow createChildRow() {
      ArrayList<JDataTable> childTables = myTable.getChildTableList();
      if(childTables.size() != 1) {
         throw new IllegalStateException(
               "JDataRow#createChildRow: No single Child DataTable DataRow");
      }
      else {
         JDataTable tbl = childTables.get(0);
         JDataRow newRow = tbl.createNewRow();
         this.addChildRow(newRow);
         return newRow;
      }
   }

   /**
    * Liefert die Anzahl der Zeilen zu der angegebenen Tabelle Reference.
    * @param refname Der Name der Child Table Reference
    * @return
    */
   public int getChildRowCount(String refname) {
      int cnt = 0;
      if(this.childRows == null) {
         return 0;
      }
      else {
         JDataTable childTable = this.myTable.getChildTable(refname);
         for(JDataRow row: childRows) {
            if(row.getDataTable() == childTable) {
               cnt++;
            }
         }
      }
      return cnt;
   }

   /**
    * Fügt eine Parent Row hinzu.
    * <p>
    * Es dürfen nur Rows von den Tabellen hinzugefügt werden, die auch als Parent-Table aufgeführt
    * sind. Ansonsten wird eine IllegalArgumentException geworfen.
    * <p>
    * Je Parent Table Reference ist nur eine Parent Row zuzulassen.
    * @param row
    */
   public void addParentRow(JDataRow row) {
      // TODO: Sicherstellen, daß nur eine Row je Parent Table Ref!?
      if(this.myTable.isParentTable(row.getDataTable()) == false) {
         throw new IllegalArgumentException("JDataRow#addParentRow: DataTable '"
               + row.getDataTable().getTablename() + "' isnt a Parent Table");
      }
      if(this.parentRows == null) {
         this.parentRows = new ArrayList<JDataRow>();
      }
      this.parentRows.add(row);
      row.setParentRow(this); // eigentlich überflüssig; wird bei ParentRows nicht benutzt
   }

   /**
    * Liefert die DataTable zu dieser Row.
    * @return
    */
   public JDataTable getDataTable() {
      return myTable;
   }

   /**
    * Liefert den Iterator für die Child Rows.
    * <p>
    * Vorsicht!<br>
    * Es werden <b>alle</b> abhängigen Daten geliefert, egal aus welcher Tabelle.
    * @return null, wenn keine Child Rows vorhanden
    */
   public Iterator<JDataRow> getChildRows() {
      if(childRows == null) {
         return null;
      }
      else {
         return childRows.iterator();
      }
   }

   /**
    * Liefert einen Iterator über alle ChildRows zu der angegebenen Child Reference.
    * <p>
    * @param refname
    * @return Liefert null, wenn keine Childs vorhanden.
    */
   public Iterator<JDataRow> getChildRows(String refname) {
      if(childRows == null) {
         return null;
      }
      ArrayList<JDataRow> al = this.getChildRowList(refname);
      if(al.size() == 0) {
         return null;
      }
      else {
         return al.iterator();
      }
   }

   ArrayList<JDataRow> getChildRowList(String refname) {
      ArrayList<JDataRow> al = new ArrayList<JDataRow>();
      for(JDataRow row: childRows) {
         if(row.getDataTable().getRefname().equalsIgnoreCase(refname)) {
            al.add(row);
         }
      }
      return al;
   }

   public Collection<JDataRow> getChildRows(DataView view) {
      ArrayList<JDataRow> al = view.getChildRowList(this);
      return Collections.unmodifiableCollection(al);
   }

   /**
    * @deprecated Liefert einen Iterator über alle Child Rows der angebenen Child Reference, bei
    *             denen der Wert eines Feldes der angegebenen Bedingung entspricht.
    * @param refname
    * @param condition FieldName, FieldValue
    * @return Liefert null, wenn keine Childs vorhanden
    */
   public Iterator<JDataRow> getChildRows(String refname, NVPair condition) {
      if(childRows == null) {
         return null;
      }
      ArrayList<JDataRow> al = new ArrayList<JDataRow>();
      // Auskommentiert und auf Namensgleichheit geändert 25.12.2003 PKÖ
      // JDataTable childTable = this.myTable.getChildTable(refname);
      for(JDataRow row: childRows) {
         // if (row.getDataTable() == childTable) { // raus: 25.12.2003 PKÖ
         if(row.getDataTable().getRefname().equals(refname)) { // neu: 25.12.2003 PKÖ
            String value = row.getValue(condition.getName());
            if(value.equals(condition.getValue())) {
               al.add(row);
            }
         }
      }
      if(al.size() == 0) {
         return null;
      }
      else {
         return al.iterator();
      }
   }

   /**
    * Liefert die Child Row zu der angegebenen Tabelle mit dem angegegeben Index (0-relativ).
    * <p>
    * Wirft eine IllegalArgumentException, wenn Index out of Range.
    * @param refname
    * @param index
    * @return
    */
   public JDataRow getChildRow(String refname, int index) {
      Iterator<JDataRow> i = this.getChildRows(refname);
      if(i == null) {
         return null;
      }
      int cnt = 0;
      while(i.hasNext()) {
         JDataRow row = i.next();
         if(cnt == index) {
            return row;
         }
         cnt++;
      }
      throw new IllegalArgumentException("JDataRow#getChildRow: '" + refname
            + "' Child Row Index out of Range: " + index);
   }
   /**
    * Findet die ChildRow bei der die angegebene Spalte den angegebenen Wert hat
    * (im Format name=value)
    * @param refname
    * @param arg SpaltenName=SpaltenWert
    * @return null, wenn Zeile fehlt
    */
   public JDataRow getChildRow(String refname, String arg) {
     Iterator<JDataRow> it = this.getChildRows(refname);
     int p = arg.indexOf("=");
     if (p == -1) return null;
     String columnName = arg.substring(0,p);
     boolean insert = false;
     if (columnName.endsWith("+")) {
        columnName = columnName.substring(0, p-1);
        insert = true;
     }
     String columnValue = arg.substring(p+1);
     if (it != null) {
        while (it.hasNext()) {
          JDataRow row = it.next();
          JDataValue val = row.getDataValue(columnName);
          if (columnValue.equals(val.getValue())) {
            return row;
          }
        }
     }
     if (insert) {
        JDataTable tbl = this.getDataTable().getChildRelation(refname).getChildTable();
        JDataRow newRow = tbl.createNewRow();
        this.addChildRow(newRow);
        newRow.setValue(columnName, columnValue);
        return newRow;
     } else {
        return null;
     }
   }

   /**
    * Liefert den Iterator für alle Parent Rows
    * @return null, wenn keine Parent Rows vorhanden
    */
   public Iterator<JDataRow> getParentRows() {
      if(parentRows == null) {
         return null;
      }
      else {
         return parentRows.iterator();
      }
   }

   public Iterator<JDataRow> getParentRows(String refname) {
      if(parentRows == null) {
         return null;
      }
      ArrayList<JDataRow> al = this.getParentRowList(refname);
      if(al.size() == 0) {
         return null;
      }
      else {
         return al.iterator();
      }
   }

   ArrayList<JDataRow> getParentRowList(String refname) {
      ArrayList<JDataRow> al = new ArrayList<JDataRow>();
      for(JDataRow row: parentRows) {
         if(row.getDataTable().getRefname().equalsIgnoreCase(refname)) {
            al.add(row);
         }
      }
      return al;
   }

   /**
    * Liefert die Parent Row zu der angegebenen Tabelle.
    * <p>
    * Wirft eine IllegalState- oder ArgumentException, wenn keine Parents vorhanden, oder
    * angegebenen Tabelle nicht als Parent definiert ist.
    * @param refname Der Name der Parent Table Reference
    * @return
    */
   public JDataRow getParentRow(String refname) {
      if(parentRows == null) {
         throw new IllegalStateException("JDataRow#getParentRow: No Parent Rows available: '" + refname + "'");
      }
      else {
         JDataTable parentTable = this.myTable.getParentTable(refname);
         for(JDataRow row: parentRows) {
            if(row.getDataTable().getRefname().equalsIgnoreCase(parentTable.getRefname())) {
               return row;
            }
         }
         throw new IllegalArgumentException("JDataRow#getParentRow: Missing Parent Row: '"
               + refname + "'");
      }
   }
   /**
    * Liefert true, wenn die ParentRow zu der angegebenen Tabelle gibt.
    * Liefert auch dann false, wenn es die angegebene Tabelle nicht gibt.
    * @param refname
    * @return
    */
   public boolean hasParentRow(String refname) {
      if(parentRows == null) {
      	return false;
      } 
      for(JDataRow row: parentRows) {
         if(row.getDataTable().getRefname().equalsIgnoreCase(refname)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Liefert den Iterator für DataValues
    * @return null, wenn keine DataValues vorhanden
    */
   public Iterator<JDataValue> getDataValues() {
      if(dataValues == null) {
         return null;
      }
      else {
         return dataValues.iterator();
      }
   }

   /**
    * Setzt den Wert eine Spalte neu.
    * @param val Die Column muß Bestandteil der Tabelle sein.
    */
   public void setDataValue(JDataValue val) {
      this.setValue(val.getColumnName(), val.getValue());
   }

   /**
    * Alle Kennzeichnungen deleted, modified werden auf false gesetzt, inserted Rows werden
    * gelöscht.
    */
   public void rollbackChanges() {
      this.deleted = false;
      // Values
      if(this.dataValues != null) {
         for(JDataValue val: this.dataValues) {
            if(val.isModified()) {
               val.rollbackChanges();
            }
         }
      }
      // Child Rows
      if(childRows != null) {
         for(Iterator<JDataRow> i = childRows.iterator(); i.hasNext();) {
        	 JDataRow childRow = i.next();
            if(childRow.isInserted()) {
               i.remove();
            }
            else {
               childRow.rollbackChanges();
            }
         }
      }
      // Parent Rows
      if(parentRows != null) {
         for(Iterator<JDataRow> i = parentRows.iterator(); i.hasNext();) {
            JDataRow parentRow = i.next();
            if(parentRow.isInserted()) {
               i.remove();
            }
            else {
               parentRow.rollbackChanges(); // überflüssig??
            }
         }
      }
   }

   /**
    * Alle Kennzeichnungen inserted, deleted, modified werden auf false gesetzt.
    */
   public void commitChanges() {
      this.inserted = false;
      this.deleted = false;
      // Values
      if(this.dataValues != null) {
         for(JDataValue val: this.dataValues) {
            if(val.isModified()) {
               val.setModified(false);
            }
         }
      }
      // Child Rows
      if(childRows != null) {
         for(Iterator<JDataRow> i = childRows.iterator(); i.hasNext();) {
            JDataRow childRow = i.next();
            if(childRow.isDeleted()) {
               i.remove();
            }
            else {
               childRow.commitChanges();
            }
         }
      }
      // Parent Rows
      if(parentRows != null) {
         for(Iterator<JDataRow> i = parentRows.iterator(); i.hasNext();) {
            JDataRow parentRow = i.next();
            if(parentRow.isDeleted()) { // New 24.12.2003 PKÖ
               i.remove();
            }
            else {
               parentRow.commitChanges();
            }
         }
      }
   }

   /**
    * Liefert diese DataRow, wenn sie verändert wurde (inserted, deleted, modified).
    * <p>
    * Alle Child Rows werden mitgeliefert, wenn sie sich geändert haben; dabei können auch
    * "Zwischen"-Rows entstehen, die mitgeliefert werden, weil von ihnen abhängige Child Rows sich
    * geändert haben.
    * @return null, wenn keine Änderung
    */
   public JDataRow getChanges() {
      JDataRow row = null;
      if(this.hasChanges()) {
         // CloneRow??
         row = new JDataRow(this.myTable, this.dataValues);
         row.setDeleted(this.deleted);
         row.setInserted(this.inserted);
         // Child Rows
         if(childRows != null) {
            for(JDataRow childRow: childRows) {
               JDataRow modiRow = childRow.getChanges();
               if(modiRow != null) {
                  row.addChildRow(modiRow);
               }
            }
         }
      }
      return row;
   }

   public JDataRow getChangesPersistent() {
      JDataRow row = null;
      if(this.hasChangesPersistent()) {
         // CloneRow??
         row = new JDataRow(this.myTable, this.dataValues);
         row.setDeleted(this.deleted);
         row.setInserted(this.inserted);
         // Child Rows
         if(childRows != null) {
            for(JDataRow childRow: childRows) {
               JDataRow modiRow = childRow.getChangesPersistent();
               if(modiRow != null) {
                  row.addChildRow(modiRow);
               }
            }
         }
      }
      return row;
   }

   /**
    * Liefert 'true', wenn sich irgendeine DataRow (auch eine Child Row!) geändert hat (inserted,
    * deleted, modified).
    * @return
    */
   public boolean hasChanges() {
      if(this.isInserted() == true || this.isDeleted() == true || this.isModified() == true) {
         return true;
      }
      else {
         // Child Rows
         if(childRows != null) {
            for(JDataRow childRow: childRows) {
               if(childRow.hasChanges()) {
                  return true;
               }
            }
         }
         // Parent Rows (eigentlich unnötig, da immer readonly)
         if(parentRows != null) {
            for(JDataRow parentRow: parentRows) {
               if(parentRow.hasChanges()) {
                  return true;
               }
            }
         }
      }
      return false;
   }

   public boolean hasChangesPersistent() {
      if(this.isInserted() == true || this.isDeleted() == true
            || this.isModifiedPersistent() == true) {
         return true;
      }
      else {
         // Child Rows
         if(childRows != null) {
           for(JDataRow childRow: childRows) {
               if(childRow.hasChangesPersistent()) {
                  return true;
               }
            }
         }
         // Parent Rows (eigentlich unnötig, da immer readonly)
         if(parentRows != null) {
           for(JDataRow parentRow: parentRows) {
               if(parentRow.hasChangesPersistent()) {
                  return true;
               }
            }
         }
      }
      return false;
   }

   /**
    * Liefert eine ArrayList von DataValues, die den Foreign Key zu den angegebenen Spalten bilden.
    * @return
    */
   public ArrayList<JDataValue> getFKValues(String fk) {
      ArrayList<JDataValue> al = new ArrayList<JDataValue>();
      StringTokenizer toks = new StringTokenizer(fk, ",");
      int anzTokens = toks.countTokens();
      int cntTokens = 0;
      try {
         switch(anzTokens) {
            case 0:
               // Error
               throw new IllegalArgumentException("JDataRow#getFKValue: Missing Foreign Key");
            // break;
            case 1: {
               JDataValue val = this.getDataValue(fk);
               al.add(val);
            }
               break;
            default: // > 1
            {
               while(toks.hasMoreTokens()) {
                  String tok = toks.nextToken();
                  if(tok.startsWith(" ")) {
                     tok = tok.trim();
                  }
                  JDataValue val = this.getDataValue(tok);
                  cntTokens++;
                  al.add(val);
               }
            }
               break;
         }
      }
      catch(Exception ex) {
         ex.printStackTrace();
      }
      return al;
   }

   /**
    * Liefert die übergeordnete Row dieser Row oder null, wenn dieses eine Root Row ist.<p>
    * Wird letztendlich nur benötigt, um beim Insert von ChildRows den Foreign Key zu ermitteln.
    * @see TableRequest#prepareInsertRow
    * @return
    */
   public JDataRow getParentRow() {
      return myParentRow;
   }

   /**
    * Liefert eine ArrayList von JDataValues die den Primary Key bilden.
    * <p>
    * Es wird eine IllegalStateException geworden, wenn die Tabelle keine Primary Key Columns hat.
    * @see JDataColumn#isPrimaryKey
    * @return
    */
   public ArrayList<JDataValue> getPrimaryKeyValues() {
      ArrayList<JDataValue> al = new ArrayList<JDataValue>();
      if(this.dataValues != null) {
      	for (JDataValue val: dataValues) {
            if(val.getColumn().isPrimaryKey()) {
               al.add(val);
            }
        }
      }
      if(al.size() == 0) {
         throw new IllegalStateException("DataRow#getPrimarKeyValues: DataTable '"
               + this.myTable.getTablename() + "' No Primary Key Columns defined");
      }
      return al;
   }
   /**
    * Fügt bei den Rows eine Spalte hinzu, die auf die angegebene DataTable verweisen (rekursiv)
    * @param tbl
    * @param col
    */
   void addDataColumn(JDataTable tbl, JDataColumn col) {
      if (this.getDataTable() == tbl) {
         JDataValue val = new JDataValue(col, col.getDefaultValue());
         this.addDataValue(val);
      }
      Iterator<JDataRow> itc = this.getChildRows();
      if (itc == null) return;
      while (itc.hasNext()) {
         JDataRow row = itc.next();
         row.addDataColumn(tbl, col);
      }
   }

   /**
    * Verwandelt einen *flachen* DataValue in eine Unterstruktur von ChildRows.
    * <p>
    * Für virtual ChildRows
    * @param row
    * @param fk
    * @param childReference
    * @return
    */
   public int getVirtualChilds(String colName, String childReference) throws ParseException {
      int cnt = 0;
      JDataValue val = this.getDataValue(colName);
      String sVal = val.getValue();
      if(sVal == null || sVal.length() == 0)
         return 0;
      Document doc = new Document(sVal);
      Element root = doc.getRoot();
      JDataTable childTable = this.getDataTable().getChildTable(childReference);
      Elements eles = root.getElements();
      while(eles.hasMoreElements()) {
         Element childEle = eles.next();
         JDataRow childRow = new JDataRow(childTable, childEle);
         this.addChildRow(childRow);
         cnt++;
      }
      return cnt;
   }

   /**
    * Tütet eine Liste von ChildRows in eine DataValue der ParentRow um.
    * <p>
    * Für virtual ChildRows
    * @param fk
    * @param childReference
    * @return
    */
   public int setVirtualChilds(String colName, String childReference) {
      int cnt = 0;
      Iterator<JDataRow> it = this.getChildRows(childReference);
      Document doc = new Document();
      Element root = doc.setRoot("Data");
      if(it != null) {
         while(it.hasNext()) {
            JDataRow childRow = it.next();
            childRow.getElement(root, false);
            cnt++;
         }
      }
      JDataValue val = this.getDataValue(colName);
      val.setValue(doc.toString());
      return cnt;
   }
   
   public void rollbackVersion(String fieldName) {
     Iterator<JDataRow> it = this.getChildRows();
     if (it != null) {
       while (it.hasNext()) {
         JDataRow row = it.next();
         row.rollbackVersion(fieldName);
       }
     }
     if (this.hasColumn(fieldName)) {
       JDataValue val = this.getDataValue(fieldName);
       if (val.isModified()) {
         val.rollbackChanges();
         val.setModified(true);
       }       
     }
   }

   public String toString() {
      return this.getElement(new Element("Row"), false).toString();
   }
   public String toStringModifiedOnly() {
     return this.getElement(new Element("Row"), true).toString();
  }

   /**
    * Erzeugt einen Clone dieser Row.
    * <p>
    * Alle Child und Parent Rows werden mit geclont. Es ist eine DataTable anzugeben, deren Name mit
    * der Tabelle der zu clonenden Row identisch sein muß; ansonsten wird eine
    * IllegalArgumentException geworfen.
    * @param myTbl
    * @return
    * @see JDataTable#cloneTable(boolean)
    */
   public JDataRow cloneRow(JDataTable myTbl) {
      if(myTbl.getTablename().equalsIgnoreCase(this.myTable.getTablename()) == false) {
         throw new IllegalArgumentException("JDataRow#cloneRow: Target Table Name Mismatch: Got '"
               + myTbl.getTablename() + "' expecting '" + this.myTable.getTablename() + "'");
      }
      JDataRow row = new JDataRow(myTbl, this.getElement(new Element("Dummy"), false));
      return row;
   }

   public void copyValues(JDataRow srcRow) {
      
      if(srcRow == null) {
         return;
      }
      
      Iterator<JDataValue> itSrcVals = srcRow.getDataValues();
      while(itSrcVals != null && itSrcVals.hasNext()) {
         
         JDataValue srcVal = itSrcVals.next();
         try {
            JDataValue myVal = this.getDataValue(srcVal.getColumnName());
            if(myVal.getDataType() == srcVal.getDataType()) {
               myVal.setValue(srcVal.getValue());
            }
         } catch(IllegalArgumentException ex) {
            // Wenn es den DataValue hier nicht gibt dann macht dass nichts!
         }
         
      }
      
   }
      
   /**
    * Überprüft die Werte dieser Zeile sowie alle Child und Parent Rows
    * @see JDataValue#verify
    * @return
    */
   public boolean verify() {
      boolean ret = true;
      try {
         // Values
         if(this.dataValues != null) {
        	 	for(JDataValue val: dataValues) {
               boolean veri = val.verify(this.isInserted());
               if(veri == false) {
                  ret = false;
               }
            }
         }
         // Child Rows
         if(childRows != null) {
            for(JDataRow childRow: childRows) {
               boolean veri = childRow.verify();
               if(veri == false) {
                  ret = false;
               }
            }
         }
         // Parent Rows (eigentlich unnötig, da immer readonly)
         if(parentRows != null) {
            for(JDataRow parentRow: parentRows) {
               boolean veri = parentRow.verify();
               if(veri == false) {
                  ret = false;
               }
            }
         }
      }
      catch(Exception ex) {
         ex.printStackTrace();
         ret = false;
      }
      return ret;
   }

   /**
    * Setzt den Fehlercode der Werte dieser Zeile sowie alle Child und Parent Rows zurück
    * @see JDataValue#verify
    * @see JDataSet#resetErrorState()
    * @return
    */
   public void resetErrorState() {
      // Values
      if(this.dataValues != null) {
         for(JDataValue val: dataValues) {
            val.setErrorCode(JDataValue.NO_ERROR);
         }
      }
      // Child Rows
      if(childRows != null) {
         for(JDataRow childRow: childRows) {
            childRow.resetErrorState();
         }
      }
      // Parent Rows
      if(parentRows != null) {
         for(JDataRow parentRow: parentRows) {
            parentRow.resetErrorState();
         }
      }
   }

   // #### Beans
   /**
    * Überträgt alle Felder aus dieser DataRow in das übergebene Objekt, wenn das Objekt über
    * entsprechende setter oder public Members verfügt.
    */
   public void getBean(Object bean) {
  	 // 1. Methods
      Method[] methods = bean.getClass().getMethods();
      for(int i = 0; i < methods.length; i++) {
         Method m = methods[i];
         String mName = m.getName();
         if(mName.startsWith("set")) {
            Class<?>[] params = m.getParameterTypes();
            if(params.length != 1)
               continue;
            Class<?> param = params[0];
            String colName = mName.substring(3);
            try {
		          this.getDataColumn(colName); // Führt zum Error, wenn Columns fehlt
		          Object arg = Convert.toObject(this.getValue(colName), param);
		          Object[] args = new Object[] {arg};
							m.invoke(bean, args);
            } catch(Exception ex) {
               System.out.println(ex);
            }
         }
      }
      // 2. Fields
      Field[] fields = bean.getClass().getFields();
      for (int i = 0; i < fields.length; i++) {
      	Field f = fields[i];
      	String colName = f.getName();
      	try {
      		this.getDataColumn(colName); // Führt zum Error, wenn Columns fehlt
      		Class<?> type = f.getType();
      		Object value = Convert.toObject(this.getValue(colName), type);
      		f.set(bean, value);
        } catch(Exception ex) {
          System.out.println(ex);
        }      	
      }
   }

   /**
    * Überträgt alle Attribute aus der Bean in diesen DataRow, wenn die Bean über entsprechende
    * getter (auch is...) oder public Member verfügt.
    * <p>
    * Die von Object geerbte Methode getClass() wird dabei weggelassen.
    * @param bean
    */
   public void setBean(Object bean) {
  	 // 1. Methods
      Method[] methods = bean.getClass().getMethods();
      for(int i = 0; i < methods.length; i++) {
         Method m = methods[i];
         String mName = m.getName();
         String colName = null;
         if(mName.startsWith("get")) {
            String x = mName.substring(3);
            if(x.equals("Class") == false) { // getClass weglassen
               colName = x;
            }
         }
         else if(mName.startsWith("is")) {
            colName = mName.substring(2);
         }
         if(colName != null) {
            try {
               this.getDataColumn(colName); // Führt zum Error, wenn Columns fehlt
               Object ret = m.invoke(bean, (Object[])null);
               this.setValue(colName, ret);
            }
            catch(Exception ex) {
               System.out.println(ex);
            }
         }
      }
      // 2. Fields
      Field[] fields = bean.getClass().getFields();
      for (int i = 0; i < fields.length; i++) {
      	Field f = fields[i];
      	String colName = f.getName();
      	try {
      		this.getDataColumn(colName); // Führt zum Error, wenn Columns fehlt
      		Object value = f.get(bean);
      		this.setValue(colName, value);
        } catch(Exception ex) {
          System.out.println(ex);
        }      	
      }
   }
   /**
    * Liefert ja nach Zustand inserted, delete, modified, modified_child oder unmodified
    * @return
    */
   public String getActionType() {
     if (inserted && !deleted) {
       return "INSERTED";
     } else if (deleted) {
       return "DELETED";
     } else if (isModified()) {
       return "MODIFIED";
     }
     if (this.isModifiedChild()) {
       return "MODIFIED_CHILD";
     }
     return "UNMODIFIED";
   }
   
   public String toSQL() {
      StringBuilder buff = new StringBuilder(100);
      if (this.isInserted()) {
         buff.append("INSERT INTO ");
         buff.append(myTable.getTablename());
         buff.append(" (");
         for(JDataValue val:this.dataValues) {
            buff.append(val.getColumnName());
            buff.append(",");
         }
         buff.replace(buff.length()-1, buff.length(), ")");
         buff.append(" VALUES (");
         for(JDataValue val:this.dataValues) {
            buff.append(this.getSqlValue(val));
            buff.append(',');
         }
         buff.replace(buff.length()-1, buff.length(), ")");
      }
      else if (this.isModified()) {
         buff.append("UPDATE ");
         buff.append(myTable.getTablename());
         buff.append(' ');
         for(JDataValue val:this.dataValues) {
            if (val.isModified()) {
               buff.append("SET ");
               buff.append(val.getColumnName());
               buff.append(" = ");
               buff.append(this.getSqlValue(val));
               buff.append(',');
            }
         }
         buff.replace(buff.length()-1, buff.length(), " ");
         // Where
         buff.append(" WHERE ");
         String keys = this.getDataTable().getPKs();
         StringTokenizer toks = new StringTokenizer(keys,",");
         int cnt = 0;
         while(toks.hasMoreTokens()) {
            if (cnt > 0) {
               buff.append(" AND ");
            }
            String tok = toks.nextToken();
            JDataValue val = this.getDataValue(tok);
            buff.append(val.getColumnName());
            buff.append(" = ");
            buff.append(this.getSqlValue(val));
            cnt++;
         }
      }
      else if (this.isDeleted()) {
         buff.append("DELETE FROM ");
         buff.append(myTable.getTablename());
         buff.append(" WHERE ");
         String keys = this.getDataTable().getPKs();
         StringTokenizer toks = new StringTokenizer(keys,",");
         int cnt = 0;
         while(toks.hasMoreTokens()) {
            if (cnt > 0) {
               buff.append(" AND ");
            }
            String tok = toks.nextToken();
            JDataValue val = this.getDataValue(tok);
            buff.append(val.getColumnName());
            buff.append(" = ");
            buff.append(this.getSqlValue(val));
            cnt++;
         }
      }
      // TODO: EndOfStatement
      buff.append(";\n");
      
      return buff.toString();
   }
   
   private String getSqlValue(JDataValue val) {
      StringBuilder buff = new StringBuilder();
      switch(val.getDataType()) {
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
            buff.append('\'');
            buff.append(val.getValue());
            buff.append('\'');
            break;
         case Types.DATE:
            val.getValueDate();
            break;
         case Types.TIMESTAMP:
            break;
         case Types.DECIMAL:
            break;
         default:
            buff.append(val.getValue());
            
      }
      return buff.toString();
   }
   
   int getChildRowIndex(JDataRow childRow) {
   	if (childRows == null) {
   		return -1;
   	}
   	int cnt = 0;
      for (int i = 0; i < this.childRows.size(); i++) { 
         JDataRow row = childRows.get(i);
         if (row == childRow) {
            return cnt;
         }
         // DataTable berücksichtigen!
         if (childRow.getDataTable().getTablename().equalsIgnoreCase(row.getDataTable().getTablename())) {
         	cnt++;
         }
      }
      return -2; // Fehler!
   }
   
   int getChildRowIndex() {
      if (myParentRow == null) {
         return -1; // Root
      }
      if (this.isParentRow()) {
         return 0; // ParentRow
      }
      int i = myParentRow.getChildRowIndex(this);
      return i;
   }
//   public String getRowPath() {
//      return this.getRowPath("");
//   }
   
//   String getRowPath(String path) {
//      if (path == null) path = "";
//      if (this.myParentRow == null) { // root Row
//      	int rootIndex = myDataSet.getChildRowIndex(this); // Hier steckt der Wurm!! myTable.myDataset zeit immer auf den selben ds!!
//      	path = this.getDataTable().getTablename() + "["+rootIndex+"]" + path;
//      } else {
//      	if (this.isParentRow()) { // ParentRow
//      		path = "#" + this.getDataTable().getTablename() + path;
//      		path = myParentRow.getRowPath(path);
//      	} else { // ChildRow
//      		path = "." + this.getDataTable().getTablename( )+ "["+this.getChildRowIndex()+"]" + path;
//      		path = myParentRow.getRowPath(path);
//      	}
//      }
//      return path;
//   }
//   /**
//    * Bei RootRows
//    * @formatter:off
//    * @param ds
//    * @formatter:on
//    */
//   void setMyDataSet(JDataSet ds) {
//      this.myDataSet = ds;
//   }
}
