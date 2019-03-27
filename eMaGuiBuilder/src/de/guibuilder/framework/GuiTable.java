package de.guibuilder.framework;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Types;
import java.text.Collator;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import de.guibuilder.framework.event.GuiTablePasteCallbackAdapter;
import de.jdataset.DataView;
import de.jdataset.JDataColumn;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.jdataset.JDataValue;
import de.jdataset.NVPair;
import de.pkjs.util.Convert;
import electric.xml.Attribute;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import sun.swing.table.DefaultTableCellHeaderRenderer;

/**
 * Implementierung einer Tabellen-Komponente.
 * <p>
 * Wichtige Methoden:
 * <ul>
 * <li>insertRow
 * <li>deleteRow
 * <li>getSelectedRow
 * <li>getDatasetValues
 * <li>setDatasetValues
 * <li>reset
 * </ul>
 * Mit Click auf den Spaltenkopf wird die Tabelle nach dieser Spalte sortiert. <br>
 * Mit Shift-Click absteigend.
 * <p>
 * Der Inhalt der Tabelle kann mit Excel ausgetauscht werden: <br>
 * Mit Ctrl-C wird der selektierte Bereich der Tabelle kopiert; <br>
 * mit Shift-Ctrl-C die gesamte Tabelle einschließlich der Spaltenüberschriften.
 * 
 * @see GuiTableRow
 */
public class GuiTable extends GuiComponent {
   private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GuiTable.class);

   // Attributes
   /**
    * Delegation zu JTable
    */
   private JTable component;

   /**
    * Action Command für ColumnHeaderClick
    */
   private String msgColHeaderClick;
   private String msgRowClick;
   private String msgPreSort;
   private String msgPostSort;
   /**
    * Not implemented
    */
   private String msgCreate;

   /**
    * Zähler für hinzugefügte Spalten. TODO: Redundant zu colVector.size()
    * 
    * @see #addColumn
    */
   private int colCount;

   /**
    * Kennzeichen, ob tableReady aufgerufen wurde.
    * 
    * @see #tableReady
    */
   private boolean ready;

   /**
    * @see #addRow
    * @see #tableReady
    */
   private Vector<GuiTableRow> rowVector = new Vector<GuiTableRow>();

   /**
    * hält die Spaltenüberschriften
    * 
    * @see #tableReady
    */
   private Vector<String> colVector = new Vector<String>();

   /**
    * Column Model
    */
   private GuiTableColumnModel colModel = new GuiTableColumnModel();

   /**
    * Referenz auf die Zeilen die mit cut oder copy zum späteren paste
    * 
    * @see #copyRow
    * @see #deleteRow
    * @see #pasteRow
    */
   private GuiTableRow pasteRow;

   /**
    * Gui-Spezifikation für eine Zeile (wird bei d_click aufgerufen auf Zeile).
    */
   private String rowEditor;

   /**
    * Verknüpfung mit einem Panel; siehe Attribut linkTable=
    */
   private GuiContainer linkPanel;
   /**
    * Hier wird die zuletzt selektierte Row vorgehalten.
    * <p>
    * Dieses ist wichtig, da ein SelectionChange-Ereignis auch dann ausgelöst wird, wenn sich an der Selektion nix
    * geändert hat.<br>
    * Auf diese Art sollen unnötige SelectionChange-Ereignisse unterdrückt werden.
    */
   private int lastSelectedRow = -2;

   private void setLastSelectedRow(int index) {
      this.lastSelectedRow = index;
   }

   public int getLastSelectedRow() {
      return this.lastSelectedRow;
   }

   /**
    * Breite der Spalten automatisch Vergeben
    */
   private boolean autoSize = true; // New 4.3.2004 PKÖ

   /**
    * Name der Spalte, nach der die Tabelle sortiert ist.
    */
   private String sortedColumnName;
   /**
    * Sortierreihenfolge
    */
   private boolean ascending = true;

   private CopyPasteAdapter excelAdapter;

   private GuiTablePasteCallbackAdapter pasteAdapter;

   public void setPasteCallbackAdapter(GuiTablePasteCallbackAdapter a) {
      this.pasteAdapter = a;
   }

   public boolean hasPasteCallbackAdapter() {
      return this.pasteAdapter != null;
   }

   public GuiTablePasteCallbackAdapter getPasteCallbackAdapter() {
      return this.pasteAdapter;
   }

   private boolean readonly; // getDataSetValue wird so abgeschaltet (ro="y")

   public void setReadonly(boolean b) {
      this.readonly = b;
   }

   public boolean isReadonly() {
      return readonly;
   }

   // ******** Constructors ***************************************************
   /**
    * Erzeugt eine Tabelle mit dem Namen "table".
    */
   public GuiTable() {
      this("table");
   }

   /**
    * Erzeugt eine Tabelle mit einem definierten Namen.
    */
   public GuiTable(String name) {
      super(name);
      if(name.length() == 0) {
         this.setName("table");
      }
      this.guiInit();
   }

   // ******** Methodes *******************************************************
   /**
    * @see #setSelectionListener
    */
   private void guiInit() {
      this.setSelectionListener();
      component.setRowHeight(21); // swing-default ist 16 // TODO: Höhe in Abhängigkeit vom Font-Size setzen
      // this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // geht nicht??

      component.setRowSelectionAllowed(true); // ###
      component.setColumnSelectionAllowed(true); // ###
      component.setAutoscrolls(true);

      component.setAutoCreateColumnsFromModel(false);
      component.setSurrendersFocusOnKeystroke(true);
      // New 29.12.2003 soll #stoppCellEditing() ersetzen, funzt aber nicht :-(
      component.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
      component.getTableHeader().setReorderingAllowed(false); // TODO: 23.12.07
                                                              // PKÖ: erst wieder
                                                              // freigeben, wenn
                                                              // Bezug zum DataSet paßt!
      component.getTableHeader().setFont(this.getFont());

      this.excelAdapter = new CopyPasteAdapter(this);

   }

   // From GuiMember
   public final String getTag() {
      return "Table";
   }

   /**
    * Liefert JTable
    */
   public final JComponent getJComponent() {
      if(component == null)
         component = new JTable();
      return component;
   }

   /**
    * Liefert GUI_TABLE
    */
   public final int getGuiType() {
      return GUI_TABLE;
   }

   // From GuiComponent
   public final int getDataType() {
      return TABLE;
   }

   /**
    * Teilt der Komponente mit, daß sie ein ColumnHeaderClick-Ereignis an den
    * Controller weiterreichen soll. Bei übergaben von null wird die
    * Weiterleitung abgeschaltet. Diese Methode muß vor der Methode tableReady()
    * aufgerufen werden.
    * <p>
    * Attribut-Syntax OnColHeaderClick="[ActionCommand]"
    * 
    * @param cmd
    *           ActionCommand dieser Message
    * @see #tableReady
    */
   public final void setMsgColHeaderClick(String cmd) {
      this.msgColHeaderClick = cmd;
   }

   /**
    * @formatter:off
    *                Diese Methode wird *vor* dem neu Sortieren der Tabelle aufgerufen
    * @param cmd
    * @formatter:on
    */
   public final void setMsgPreSort(String cmd) {
      this.msgPreSort = cmd;
   }

   public String getMsgPreSort() {
      return this.msgPreSort;
   }

   /**
    * @formatter:off
    *                Diese Methode wird *nach* dem neu Sortieren der Tabelle aufgerufen
    * @param cmd
    * @formatter:on
    */
   public final void setMsgPostSort(String cmd) {
      this.msgPostSort = cmd;
   }

   public String getMsgPostSort() {
      return this.msgPostSort;
   }

   /**
    * @see #setMsgColHeaderClick
    * @return ActionCommand
    */
   public final String getMsgColHeaderClick() {
      return msgColHeaderClick;
   }

   /**
    * Es wird das Ereignis OnRowClick="[ActionCommand]" definiert.
    * 
    * @param cmd
    *           Das ActionCommand für OnRowClick=
    */
   public final void setMsgRowClick(String cmd) {
      msgRowClick = cmd;
   }

   /**
    * @see #setMsgRowClick
    * @return ActionCommand
    */
   public final String getMsgRowClick() {
      return msgRowClick;
   }

   /**
    * @return Returns the msgCreate.
    */
   String getMsgCreate() {
      return msgCreate;
   }

   /**
    * @param msgCreate
    *           The msgCreate to set.
    */
   void setMsgCreate(String msgCreate) {
      this.msgCreate = msgCreate;
   }

   public void setMsgDrop(String cmd) {
      super.setMsgDrop(cmd);
      if(cmd != null) {
         Container comp = this.getJComponent().getParent();
         new DropTarget(comp, new DTListener());
      }
   }

   public void setMsgFileDrop(String cmd) {
      if(cmd != null) {
         Container cont = this.getJComponent().getParent();
         super.setMsgFileDrop(cmd, cont);
      }
      //    if (cmd != null) {
      //       Container comp = this.getJComponent().getParent();
      //       new DropTarget(comp, new DTListener());
      //    }
   }

   /**
    * See JTable#getRowHeight Default is 21
    * 
    * @return
    */
   public int getRowHeight() {
      return component.getRowHeight();
   }

   /**
    * See JTable#setRowHeight Default is 21
    * 
    * @param pixel
    */
   public void setRowHeight(int pixel) {
      component.setRowHeight(pixel);
   }

   public void setPopupMenu(GuiPopupMenu m) {
      super.setPopupMenu(m);
      for(int i = 0; i < this.colModel.getColumnCount(); i++) {
         GuiTableColumn col = colModel.getGuiColumn(i);
         GuiComponent comp = col.getGuiComponent();
         if(comp instanceof GuiCombo) {
            comp.setPopupMenu(m);
         }
      }
   }

   /**
    * Fügt der Tabelle eine neue Spalte hinzu. Die Spalten sind in der
    * gewünschten Reihenfolge zuzuweisen. Erlaubte Komponenten sind GuiText (und
    * davon abgeleitete Klassen wie Date, Time, Money, Number), GuiCombo,
    * GuiCheck, GuiLabel, Hidden.
    * <P>
    * Es wird die minimale und die maximale Breite der Komponente übernommen. <BR>
    * Bei unsichtbaren Komponenten wird die Breite auf Null gesetzt.
    * <P>
    * Achtung! <BR>
    * Diese Methode darf nur für neu erstellte Tabellen verwendet werden, die anfangs mit Spalten initialisiert werden
    * sollen. Am Ende die Initialisierung muß die Methode tableReady() aufgerufen werden!
    * 
    * @param comp
    *           Eine Komponente
    * @param title
    *           Spaltenüberschrift
    * @param width
    *           Preferred Width in Pixeln.
    * @throws IllegalArgumentException
    *            wenn unzulässige Komponente übergeben wird.
    * @see #tableReady
    */
   public final GuiTableColumn addColumn(TableColumnAble comp, String title, int width) {
      if(ready == true) {
         throw new IllegalStateException("Table ready");
      }
      comp.setParentTable(this);
      GuiTableColumn col = new GuiTableColumn(this, comp, title, width);
      comp.setFont(component.getFont());
      this.colVector.addElement(title);
      this.colModel.addGuiColumn(col);
      this.colCount++;
      return col;
   } // end of addColumn

   /**
    * Liefert die Anzahl der mit {@link #addColumn}hinzugefügten Columns.
    */
   public final int getColCount() {
      return colCount;
   }

   /**
    * Liefert die Anzahl der Zeilen in der Tabelle.
    */
   public final int getRowCount() {
      if(this.ready == true) {
         return component.getRowCount();
      }
      else {
         return rowVector.size();
      }
   }

   /**
    * Fügt eine Zeile der Tabelle hinzu.
    * <P>
    * Achtung! <BR>
    * Diese Methode darf nur für neu erstellte Tabellen verwendet werden, die anfangs mit Zeilen initialisiert werden
    * sollen. Am Ende die Initialisierung muß die Methode {@link #tableReady}aufgerufen werden!
    * <P>
    * Sollen zur Laufzeit später Zeilen hinzugefügt werden, ist {@link #insertRow}zu verwenden.
    * 
    * @param row
    *           Eine GuiTableRow
    */
   public final void addRow(GuiTableRow row) {
      if(ready == true) {
         throw new IllegalStateException("Table ready");
      }
      if(row != null) {
         rowVector.addElement(row);
         row.setParentTable(this);
      }
   }

   /**
    * Liefert die selektierte Zeile oder null, wenn keine Zeile selektiert oder
    * die Tabelle leer ist.
    * 
    * @return
    */
   public final GuiTableRow getRow() {
      if(this.getSelectedRow() < 0) {
         return null;
      }
      else {
         return this.getRow(this.getSelectedRow());
      }
   }

   /**
    * Liefert die Zeile mit der angegebenen Nummer (0-relativ).
    */
   public final GuiTableRow getRow(int index) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      GuiTableModel mdl = this.getGuiTableModel();
      if(mdl == null) {
         return null; // TODO: Passiert irgendwie beim LostFocus / Window Close
      }
      return mdl.getRow(index);
   }

   /**
    * Liefert einen Vector mit den Namen der Spalten.
    */
   public final Vector<String> getColumnIdentifiers() {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      Vector<String> ret = new Vector<String>();
      for(int i = 0; i < component.getColumnCount(); i++) {
         TableColumn col = component.getColumnModel().getColumn(i);
         ret.addElement((String)col.getIdentifier());
      }
      return ret;
   }

   /**
    * Liefert die TableColumn mit dem angegebenen Namen.
    * 
    * @param name
    * @return
    */
   public final GuiTableColumn getColumn(String columnName) {
      return this.colModel.getGuiColumn(columnName);
   }

   public boolean hasColumn(String columnName) {
      GuiTableColumn col = getColumn(columnName);
      return col == null ? false : true;
   }

   /**
    * Liefert die TableColumn unter dem angegebenen Index (0-relativ).
    * 
    * @param colIndex
    * @return
    */
   public final GuiTableColumn getColumn(int colIndex) {
      GuiTableColumn col = colModel.getGuiColumn(colIndex);
      return col;
   }

   GuiTableColumnModel getColumnModel() {
      return colModel;
   }

   /**
    * Liefert den Index der Spalte unter Angabe ihres Namens.
    * 
    * @param colName
    *           Name der Spalte; siehe Attribut name=
    * @return Den Index der Spalte im ColumnModel oder -1, wenn der Name der
    *         Spalte ungültig ist.
    */
   public int getColumnIndex(String colName) {
      return this.colModel.getGuiColumnIndex(colName);
   }

   /**
    * Verknüpft eine externe Componente als Editor für eine Spalte.
    */
   final void setLinkComponent(GuiComponent comp, int colIndex) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      GuiTableColumn col = colModel.getGuiColumn(colIndex);
      col.setLinkComponent(comp);
   }

   /**
    * Verknüft diese Tabelle mit einem Panel.
    * <p>
    * Die Eingaben in dem Panel werden dann mit den Zeilen dieser Tabelle synchronisiert. Siehe Attribut linkTable=
    */
   final void setLinkPanel(GuiContainer p) {
      linkPanel = p;
   }

   /**
    * @deprecated Setzt den Wert einer Spalte der selektierten Zeile neu.
    * @see #setCellValue(int, Object)
    */
   public final void setColumnValue(Object obj, int colIndex) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      if(getSelectedRow() != -1) {
         GuiTableRow row = getRow(getSelectedRow());
         row.setValueAt(colIndex, obj);
      }
   }

   /**
    * @deprecated Setzt den Wert einer Spalte der selektierten Zeile neu.
    * @param obj
    *           Der zu setzende Wert
    * @param name
    *           Name der Spalte
    * @see #setCellValue(String, Object)
    */
   public final void setColumnValue(Object obj, String colName) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      GuiTableColumn col = this.getColumn(colName);
      int colIndex = col.getModelIndex();
      if(getSelectedRow() != -1) {
         GuiTableRow row = getRow(getSelectedRow());
         row.setValueAt(colIndex, obj);
      }
   }

   /**
    * @deprecated Setzt den Wert einer Spalte der angegebenen Zeile neu.
    * @see #setCellValue(int, int, Object)
    */
   public final void setColumnValue(Object obj, int colIndex, int rowIndex) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      GuiTableRow row = getRow(rowIndex);
      row.setValueAt(colIndex, obj);
   }

   /**
    * @deprecated Setzt die Werte einer Tabellenzeile neu (Vector). <br>
    *             Die Spalten werden dabei von links nach rechts mit den Werten
    *             des übergebenen Vector aufgefüllt; dieser Vector darf auch
    *             kleiner oder länger als die Zahl der Spalten sein; überzählige
    *             Spalten werden abgeschnitten.
    * @throws IllegalArgumentException
    *            Wenn row negativ oder größer als die Zahl der Zeilen ist.
    */
   public final void setRowValues(int row, Vector values) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      if(row < 0 || row > component.getRowCount()) {
         throw new IllegalArgumentException("Row number out of range!");
      }
      else {
         for(int i = 0; i < component.getColumnCount() && i < values.size(); i++) {
            GuiTableRow trow = getRow(row);
            trow.setData(values);
            // component.setValueAt(values.get(i), row, i);
         }
      }
   }

   /**
    * Setzt den Inhalt einer Tabellenspalte neu. <br>
    * Die Spalten werden von oben nach unten mit den Werten gefüllt. Der
    * übergebene Vector darf auch kleiner oder größer als die Anzahl der Zeilen
    * sein.
    * <p>
    * Hinweis: <br>
    * Mit initRows kann eine Tabelle mit einer definierten Anzehl von leeren Zeilen initialisiert werden.
    * 
    * @throws IllegalArgumentException
    *            wenn col negativ oder größer als die Zahl der Spalten ist.
    */
   public final void setColValues(int col, Vector<Object> values) {
      if(col < 0 || col > colCount) {
         throw new IllegalArgumentException("Column Number '" + col + "'out of Range!");
      }
      else {
         if(ready) { // "ready"
            for(int i = 0; i < component.getRowCount(); i++) {
               GuiTableRow row = getRow(i);
               row.setValueAt(col, values.elementAt(i));
               // component.setValueAt(values.get(i), i, col);
            }
         }
         else { // not "ready"
            for(int i = 0; i < values.size(); i++) {
               if(i >= rowVector.size()) {
                  this.addRow(new GuiTableRow(this, colCount));
               }
               GuiTableRow row = rowVector.elementAt(i);
               row.setValueAt(col, values.elementAt(i), false);
            }
         }
      }
   }

   /**
    * Setzt den Inhalt einer Tabellenspalte neu (StringArray).
    */
   public final void setColValues(int col, String[] values) {
      Vector<Object> v = new Vector<Object>(values.length);
      for(int i = 0; i < values.length; i++) {
         v.add(values[i]);
      }
      setColValues(col, v);
   }

   /**
    * Liefert den Inhalt einer Spalte als einen Array von Strings.
    */
   public final String[] getColValues(int col) {
      String[] ret = new String[component.getRowCount()];
      for(int i = 0; i < component.getRowCount(); i++) {
         GuiTableRow row = getRow(i);
         ret[i] = (String)row.getValueAt(col);
      }
      return ret;
   }

   /**
    * Scrollt auch zu der selektierten Zeile
    */
   public void requestFocus() { // New PKÖ 17.5.2004
      super.requestFocus();
      int selRow = this.getSelectedRow();
      if(selRow != -1) {
         Rectangle rect = component.getCellRect(selRow, 0, false);
         component.scrollRectToVisible(rect);
      }
   }

   /**
    * Setzt den Focus auf die angegebene Zelle.
    * <p>
    * 
    * @param row
    *           zu selektierende Zeile
    * @param col
    *           zu selektierende Spalte; wenn -1 dann nur die Zeile selektieren.
    */
   public final void setFocus(int row, int col) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }

      ListSelectionModel lsm = component.getSelectionModel();
      lsm.setSelectionInterval(row, row);
      if(col != -1) {
         lsm = component.getColumnModel().getSelectionModel();
         lsm.setSelectionInterval(col, col);
      }
   }

   /**
    * Teilt der Tabelle mit, daß die Initialisierung der Tabelle mit Spalten und
    * Zeilen beendet ist.
    * <p>
    * Erst hier wird das TableModel und das ColumnsModel eingerichtet.
    * <p>
    * Außerdem wird ein MouseListener eingerichtet, der das Anklicken der Spaltenköpfe weiterleitet wenn dieses
    * gewünscht ist.
    * 
    * @see #addColumn
    * @see #addRow(GuiTableRow)
    * @see #setMsgColHeaderClick(String)
    * @see GuiRootPane#obj_TblHeaderClick(GuiTable, String, int)
    */
   public final void tableReady() {
      if(ready)
         return;
      ready = true;
      GuiTableModel sortModel = new GuiTableModel(this);
      sortModel.setDataVector2(rowVector, colVector);
      component.setModel(sortModel);
      component.setColumnModel(colModel); // muß nach setModel() erfolgen!!
      this.addMouseListenerToHeader(); // ADDED THIS
      if(rowVector.size() > 0) {
         this.setSelectedRow(0);
      }
      // Excel-Adapter
      int[] disabledCols = this.getDisabledColumns();
      if(this.excelAdapter != null) {
         excelAdapter.setDisabledColumnsForPasting(disabledCols);
      }
      // Table Header Events
      if(msgColHeaderClick != null) {
         JTableHeader th = component.getTableHeader();
         th.addMouseListener(new MouseAdapter() {
            // Inner Class
            public void mouseClicked(MouseEvent e) {
               TableColumnModel columnModel = component.getColumnModel();
               int viewColumn = columnModel.getColumnIndexAtX(e.getX());
               int column = component.convertColumnIndexToModel(viewColumn);
               if(e.getClickCount() == 1 && column != -1 && getRootPane() != null) {
                  // Weiterreichen an RootPane
                  getRootPane().obj_TblHeaderClick(GuiTable.this, msgColHeaderClick, column);
               }
            }
         }); // End addMouseListener
      }
   }

   /**
    * Setzt den SelectionListener der Tabelle (inner Class).
    */
   private final void setSelectionListener() {
      // Notify Selection
      ListSelectionModel rowSM = component.getSelectionModel();
      //rowSM.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      rowSM.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      rowSM.addListSelectionListener(new ListSelectionListener() {
         int lastSelectedRowCount;

         // Start inner Class -----------------------------
         public void valueChanged(ListSelectionEvent e) {
            GuiRootPane pane = getRootPane();
            if(pane == null) {
               // Darf nicht mehr passieren!
               // GuiTree valueChanges geändert!
            }
            else {
               pane.setCurrentTable(GuiTable.this);
               if(e.getValueIsAdjusting() == false) {
                  ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                  // Zweite Bedingung auch wichtig!
                  // Eine Zeile 0 kann merkwürdiger Weise auch dann
                  // selektiert sein, wenn die Tabelle leer ist !!
                  if(lsm.isSelectionEmpty() || component.getRowCount() == 0) {
                     // no rows are selected
                  }
                  else {
                     // Row is selected
                     int selectedRow = lsm.getMinSelectionIndex();
                     int selectedRowCount = component.getSelectedRows().length;
                     if(GuiUtil.getDebug()) {
                        System.out.println("valueChanged: " + lastSelectedRow + "/" + selectedRow);
                     }
                     // Nur wenn wirklich eine andere Zeile selektiert wurde!
                     if(selectedRow != lastSelectedRow || selectedRowCount != lastSelectedRowCount) {

                        { // Block Update LinkComponents
                           for(int i = 0; i < colModel.getColumnCount(); i++) {
                              GuiComponent comp = colModel.getGuiColumn(i).getLinkComponent();
                              if(comp != null) {
                                 Object val = component.getValueAt(selectedRow, i);
                                 comp.setValue(val);
                                 comp.setLinkRow(selectedRow); // New 20.2.2005 / PKÖ
                                 if(GuiUtil.getDebug()) {
                                    System.out.println("valueChanged: [" + lastSelectedRow + ":" + selectedRow + "/" + i + "] " + comp.getName() + "/" + val);
                                 }
                              }
                           } // End For
                        } // End Block LinkComponents

                        { // LinkPanel
                           if(linkPanel != null) {
                              for(int i = 0; i < colModel.getColumnCount(); i++) {
                                 GuiTableColumn col = colModel.getGuiColumn(i);
                                 Object val = component.getValueAt(selectedRow, i);
                                 linkPanel.setValue(col.getName(), val);
                                 if(GuiUtil.getDebug()) {
                                    System.out.println("valueChanged: [" + lastSelectedRow + ":" + selectedRow + "/" + i + "] " + col.getName() + "/" + val);
                                 }
                              } // End For
                           } // End if linkPanel
                        } // End of Block LinkPanel
                          // Weiterreichen an RootPane
                        if(msgRowClick != null) {
                           pane.obj_TblRowSelected(GuiTable.this, msgRowClick, selectedRow, getRowValues(selectedRow));
                        }
                     }
                     setLastSelectedRow(selectedRow); // Merken
                     this.lastSelectedRowCount = selectedRowCount;
                  } // End if isSelectionEmpty
               } // end if isAdjust
            }
         } // end of method valueChanged()
      }); // end of nested class ListSelectionListener
   }

   /**
    * Fügt zur Laufzeit eine leere Zeile der Tabelle am Ende hinzu.
    * 
    * @param select
    *           wenn true, wird diese Zeile als die selected row markiert.
    * @return die frisch eingefügte leere Tabellenzeile
    */
   private GuiTableRow insertRow(boolean select) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      GuiTableModel mdl = this.getGuiTableModel();
      GuiTableRow tableRow = new GuiTableRow(this, colCount);
      tableRow.setInserted(true);
      mdl.addRow(tableRow);
      if(select) {
         int row = mdl.getRowCount() - 1;
         this.setSelectedRow(row);
      }
      return tableRow;
   }

   /**
    * Fügt zur Laufzeit eine leere Zeile der Tabelle am Ende hinzu.
    * 
    * @return Die neue Zeile
    * @see #addRow
    */
   public final GuiTableRow insertRow() {
      return this.insertRow(true);
   }

   /**
    * Fügt zur Laufzeit eine leere Zeile an der angegebenen Zeile in die Tabelle
    * ein.
    * 
    * @param row
    *           Zeilennummer 0-relativ.
    */
   public final GuiTableRow insertRow(int row) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      if(row < 0) {
         return this.insertRow();
      }
      else {
         GuiTableRow tRow = new GuiTableRow(this, colCount);
         this.insertRow(row, tRow);
         return tRow;
      }
   }

   /**
    * Fügt zur Laufzeit eine Zeile der Tabelle am Ende hinzu.
    * 
    * @param row
    *           Eine GuiTableRow
    * @see #addRow
    */
   public final void insertRow(GuiTableRow tableRow) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      GuiTableModel mdl = this.getGuiTableModel();
      tableRow.setInserted(true);
      mdl.addRow(tableRow);
      tableRow.setParentTable(this);
      int selrow = mdl.getRowCount() - 1;
      this.setSelectedRow(selrow);
   }

   /**
    * Fügt zur Laufzeit eine Zeile in die Tabelle ein.
    * 
    * @param row
    *           Index der Tabelle, an der die Zeile eingefügt werden soll.
    * @param tableRow
    */
   public final void insertRow(int row, GuiTableRow tableRow) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      GuiTableModel mdl = this.getGuiTableModel();
      tableRow.setInserted(true);
      mdl.insertRow(row, tableRow);
      tableRow.setParentTable(this);
      int selectedRow = row;
      this.setSelectedRow(selectedRow);
   }

   /**
    * Dubliziert die selektierte Zeile
    * 
    * @see #getRow()
    * @see #insertRow()
    */
   public void duplicateRow() {
      GuiTableRow row = this.getRow();
      if(row != null) {
         this.insertRow(row.guiClone());
      }
   }

   /**
    * Löscht die selektierte Zeile aus der Tabelle. Tut nichts, wenn keine Zeile
    * selektiert ist.
    * 
    * @see #pasteRow
    */
   public final void deleteRow() {
      int selectedRow = this.getSelectedRow();
      if(selectedRow != -1)
         this.deleteRow(selectedRow);
   }

   /**
    * Löscht alle selektierten Zeilen.
    * 
    * @return Die Anzahl der gelöschten Zeilen oder 0, wenn nix selektiert war.
    */
   public final int deleteSelectedRows() {
      int[] rows = component.getSelectedRows();
      int cnt = rows.length;
      if(cnt != 0) {
         for(int i = rows.length - 1; i >= 0; i--) {
            this.deleteRow(rows[i]);
         }
      }
      return cnt;
   }

   /**
    * Löscht die angegebene Zeile aus der Tabelle. Wirft eine Exception bei
    * ungültiger Zeilennummer.
    * 
    * @see #pasteRow
    */
   public final void deleteRow(int row) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      GuiTableModel mdl = this.getGuiTableModel();
      if(row < 0 || row > mdl.getRowCount() - 1) {
         throw new IllegalArgumentException("Row Number out of Range");
      }
      else {
         if(row == this.getSelectedRow())
            this.setLastSelectedRow(-1);
         pasteRow = getRow(row);
         mdl.deleteRow(row);
         if(row >= mdl.getRowCount()) {
            setSelectedRow(mdl.getRowCount() - 1);
         }
         else {
            setSelectedRow(row);
         }
      }
   }

   /**
    * Kopiert die selektierte Zeile in eine interne Variable. Tut nichts, wenn
    * keine Zeile selektiert ist.
    * 
    * @see #pasteRow
    */
   public final void copyRow() {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      int selectedRow = getSelectedRow();
      if(selectedRow != -1) {
         // GuiTableModel mdl = this.getGuiTableModel();
         pasteRow = getRow(selectedRow).guiClone();
      }
   }

   /**
    * Fügt die gelöschte oder kopierte Zeile ein wenn sich nicht null ist. <br>
    * Wenn keine Zeile selektiert ist, wird sie am Ende der Tabelle angefügt.
    */
   public final void pasteRow() {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      if(pasteRow != null) {
         GuiTableModel mdl = this.getGuiTableModel();
         int selectedRow = getSelectedRow();
         if(selectedRow == -1) {
            mdl.addRow(pasteRow.guiClone());
         }
         else {
            mdl.insertRow(selectedRow, pasteRow.guiClone());
         }
      }
   }

   /**
    * Setzt den Inhalt der Tabelle neu; Vector wird geklont. <br>
    * Der Vector muß aus GuiTableRows bestehen. <br>
    * 
    * @param data
    *           Wenn null, wird der Inhalt der Tabelle gelöscht.
    * @see GuiTableRow
    */
   private void setDataVector(Vector<GuiTableRow> data) {
      GuiTableModel mdl = this.getGuiTableModel();
      if(data != null) {
         Vector clone = (Vector)data.clone();
         mdl.setDataVector(clone);
      }
      else {
         mdl.reset();
      }
      this.setModified(false);
      mdl.fireTableDataChanged();
      component.invalidate();
   }

   /**
    * Ersetzt den Inhalt der Tabelle durch einen neuen Vector von Vectoren. Jeder
    * innere Vector entspricht eine Zeile in der Tabelle. <BR>
    * Der Zeilen-Vector muß Strings enthalten bis auf die Checkboxen, die ein
    * Boolean erwarten. <BR>
    * Wird hier null übergeben, wird der Inhalt der Tabelle gelöscht.
    */
   public final void setValue(Object val) {
      Vector data = (Vector)val;
      this.setDataVector(data);
      this.setModified(false);
      if(data != null && data.size() > 0) {
         setSelectedRow(0);
      }
   }

   /**
    * Setzt den Wert einer Zelle neu
    * 
    * @param val
    *           String, Boolean
    * @param row
    *           Zeilennummer 0-relativ
    * @param col
    *           Spaltennummer 0-relativ
    */
   public final void setValueAt(Object val, int row, int col) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      component.getModel().setValueAt(val, row, col);
   }

   /**
    * Füllt die Combobox der angegebenen Spalte mit neuen Werten. Wirft eine
    * IllegalArgumentException, wenn die Spalte keine ComboBox hält.
    * 
    * @param colIndex
    *           Spalte der Tabelle (0-relativ)
    * @param items
    *           Vector von String
    */
   public final void setItems(int colIndex, Vector<Object> items) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      try {
         JComboBox combo = this.getCombo(colIndex);
         combo.setModel(new DefaultComboBoxModel(items));
      }
      catch(IllegalArgumentException ex) {
         GuiUtil.showEx(ex);
      }
   }

   /**
    * Füllt die Combobox der angegebenen Spalte mit neuen Werten. Wirft eine
    * IllegalArgumentException, wenn die Spalte keine ComboBox hält.
    * 
    * @param colIndex
    *           Spalte der Tabelle (0-relativ)
    * @param items
    *           Array von Strings
    */
   public final void setItems(int colIndex, String[] items) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      try {
         JComboBox combo = this.getCombo(colIndex);
         combo.setModel(new DefaultComboBoxModel(items));
      }
      catch(IllegalArgumentException ex) {
         GuiUtil.showEx(ex);
      }
   }

   /**
    * Füllt eine ComboBox-Spalte mit den angegebenen Werten.
    * 
    * @see GuiCombo#setItems
    * @param colName
    * @param ds
    */
   public final void setItems(String colName, JDataSet ds) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      GuiTable.GuiTableColumn col = this.getColumn(colName);
      GuiComponent comp = col.getGuiComponent();
      if(comp instanceof GuiCombo) {
         GuiCombo cmb = (GuiCombo)comp;
         cmb.setItems(ds);
         // Renderer
         GuiTableComboRenderer rend = (GuiTableComboRenderer)col.getCellRenderer();
         rend.getGuiCombo().setItems(ds);
      }
      else {
         throw new IllegalArgumentException("Column: " + colName + " is not instanceof GuiCombo!");
      }
   }

   public final void setItems(String colName, ArrayList<Object> al) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      GuiTable.GuiTableColumn col = this.getColumn(colName);
      GuiComponent comp = col.getGuiComponent();
      if(comp instanceof GuiCombo) {
         GuiCombo cmb = (GuiCombo)comp;
         cmb.setItems(al);
         // Renderer
         GuiTableComboRenderer rend = (GuiTableComboRenderer)col.getCellRenderer();
         rend.getGuiCombo().setItems(al);
      }
      else {
         throw new IllegalArgumentException("Column: " + colName + " is not instanceof GuiCombo!");
      }
   }

   public final void setItems(String colName, String[] items) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      GuiTable.GuiTableColumn col = this.getColumn(colName);
      GuiComponent comp = col.getGuiComponent();
      if(comp instanceof GuiCombo) {
         GuiCombo cmb = (GuiCombo)comp;
         cmb.setItems(items);
         // Renderer
         GuiTableComboRenderer rend = (GuiTableComboRenderer)col.getCellRenderer();
         rend.getGuiCombo().setItems(items);
      }
      else {
         throw new IllegalArgumentException("Column: " + colName + " is not instanceof GuiCombo!");
      }
   }

   /**
    * Liefert die ComboBox aus der angegebenen Spalte.
    */
   private JComboBox getCombo(int colIndex) throws IllegalArgumentException {
      GuiTableColumn col = colModel.getGuiColumn(colIndex);
      DefaultCellEditor editor = (DefaultCellEditor)col.getCellEditor();
      Component comp = editor.getComponent();
      if(comp instanceof JComboBox) {
         return (JComboBox)comp;
      }
      else {
         throw new IllegalArgumentException("CellEditor of Column " + colIndex + " not instanceof JComboBox");
      }
   }

   /**
    * Leert die Tabelle
    */
   public final void reset() {
      this.setDataVector(null);
      this.setModified(false);
      // Header
      if(sortedColumnName != null) {
         GuiTableColumn col = this.getColumn(sortedColumnName);
         //col.setHeaderRenderer(null);
         Object pval = col.getHeaderValue();
         if(pval instanceof JLabel) {
            JLabel lbl = (JLabel)pval;
            lbl.setIcon(null);
         }
         JTableHeader th = component.getTableHeader();
         th.repaint();
      }
   }

   /**
    * Initialisiert die Tabelle mit der angegeben Zahl von leeren Zeilen.
    */
   final void initRows(int rows) {
      try {
         this.reset();
      }
      catch(Exception ex) {
         // nix; noch kein TableModel
      }
      for(int i = 0; i < rows; i++) {
         GuiTableRow row = new GuiTableRow(this, colCount);
         if(ready) {
            this.insertRow(row);
         }
         else {
            this.addRow(row);
         }
      }
   }

   public Object getUnformatedValue() {
      return getValue();
   }

   /**
    * Liefert den Inhalt der Tabelle als einen Vector von Vectoren. <br>
    * Es wird ein Clone dieses Vectors zurückgegeben.
    */
   public final Object getValue() {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      GuiTableModel mdl = this.getGuiTableModel();
      Vector<GuiTableRow> data = (Vector<GuiTableRow>)mdl.getDataVector().clone();
      return data;
   }

   /**
    * Liefert das TableModel.
    * <p>
    * Im Unterschied zu getModel wird hier auf GuiTableModel ge-cast-ed.
    */
   public final GuiTableModel getGuiTableModel() {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      if(component == null) {
         return null; // TODO: Passiert irgendwie beim LostFocus / Window Close
      }
      return (GuiTableModel)component.getModel();
   }

   /**
    * Liefert die seit dem letzten reset gelöschten Zeilen. <br>
    * Seit dem letzten reset neu eingefügte und wieder gelöschte Zeilen werden
    * hier nicht aufgeführt.
    */
   public final Vector<GuiTableRow> getDeletedRows() {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      return getGuiTableModel().getDeletedRows();
   }

   public final Vector<GuiTableRow> getAllValues(boolean modified) {
      Vector<GuiTableRow> ret = new Vector<GuiTableRow>();
      Vector data = (Vector)this.getValue();
      if(modified == false) {
         return data;
      }
      if(data != null) {
         for(Enumeration e = data.elements(); e.hasMoreElements();) {
            GuiTableRow row = (GuiTableRow)e.nextElement();
            if(row.isModified()) {
               ret.addElement(row);
            }
         }
      }
      if(ret.size() == 0) {
         return null;
      }
      else {
         return ret;
      }
   }

   /**
    * Liefert den Inhalt der Tabelle als XML-Document.
    */
   public final Document getAllValuesXml() {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      Document doc = new Document();
      Element tblEle = doc.setRoot("Table");
      tblEle.setAttribute("name", this.getName());
      tblEle.setAttribute("type", GuiComponent.getDataTypeName(this.getDataType()));
      if(this.getOid() != -1) {
         tblEle.setAttribute(OID, Long.toString(this.getOid()));
      }
      // TableColums
      for(int i = 0; i < this.colModel.getColumnCount(); i++) {
         GuiTableColumn col = this.colModel.getGuiColumn(i);
         Element colNode = tblEle.addElement("Column");
         colNode.setAttribute("name", col.getName());
         colNode.setAttribute("type", GuiComponent.getDataTypeName(col.getDataType()));
      }
      // TableData
      int rowCnt = 0; // Counter Rows
      GuiTableModel mdl = this.getGuiTableModel();
      for(Iterator<Map.Entry<Integer, GuiTableRow>> it = mdl.getRowIterator(); it.hasNext();) {
         Map.Entry<Integer, GuiTableRow> val = it.next();
         GuiTableRow tableRow = (GuiTableRow)val.getValue();
         Element rowEle = tableRow.getElement();
         tblEle.addElement(rowEle);
         // Selected Row
         if(rowCnt == getSelectedRow()) {
            rowEle.setAttribute("selected", "true");
         }
         rowCnt++; // inc RowCounter
      }
      return doc;
   }

   /**
    * Füllt die Tabelle mit Werten aus einem XML-Document. Der Knoten "Table" muß
    * "Row" und "Row" muß "Component" enthalten.
    * 
    * @throws IllegalArgumentException
    *            wenn NodeName != "Table"
    */
   public final void setAllValuesXml(Element node) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      if(node.getName().equals("Table") == false) {
         throw new IllegalArgumentException("NodeName != Table: " + node.getName());
      }
      this.reset();
      // oid
      final String oid = node.getAttributeValue(OID);
      if(oid != null) {
         this.setOid(Convert.toLong(oid));
      }
      // Vector von GuiTableRows für Model
      Vector<GuiTableRow> dataVector = new Vector<GuiTableRow>();
      Vector<GuiTableRow> deletedRows = null;
      // Die Child-Nodes "Row" ergeben jeweils eine Zeile
      Elements list = node.getElements("Row");
      int rowCnt = 0; // Row Counter
      int rowToSelect = 0;
      while(list.hasMoreElements()) {
         Element rowEle = list.next();
         // new TableRow
         GuiTableRow row = new GuiTableRow(this, rowEle); // + Attributes
         // modi,ins,dele,...
         // selected Row
         {
            Attribute att = rowEle.getAttributeObject("selected");
            if(att != null && att.getValue().equals("true")) {
               rowToSelect = rowCnt;
            }
         }
         // Add to Container
         if(row.isDeleted()) {
            if(deletedRows == null) {
               deletedRows = new Vector<GuiTableRow>();
            }
            deletedRows.addElement(row);
         }
         else {
            dataVector.addElement(row);
         }
         // Cells
         Elements cells = rowEle.getElements();
         while(cells.hasMoreElements()) {
            Element cellNode = cells.next();
            String val = cellNode.getTextString();
            row.getData().addElement(val);
         } // end of columns
         rowCnt++; // inc Row Counter
      } // end of Rows
      this.setDataVector(dataVector);
      // Deleted Rows?
      if(deletedRows != null) {
         this.getGuiTableModel().setDeletedRows(deletedRows);
      }
      // Set selected Row
      this.setSelectedRow(rowToSelect);
   }

   public String getCurrentRowPath(String current) {
      String path = this.getElementPath(current);
      path = path + "[";
      GuiTableRow row = this.getRow();
      path = path + Convert.toString(row.getModelElementNumber()) + "]";
      return path;
   }

   /**
    * Setzt den Inhalt der Komponente auf den Inhalt des Models der über
    * getElementPath erreichbar ist. Wird von GuiTable und GuiTree überschrieben.
    * Voraussetzung ist, daß ein Attribut "element" spezifiziert ist.<br />
    * <i>Sets the content of the component to the content of the JDataset. The
    * elements must be reachable through getElementPath() method and must be
    * declared by an element attribute which identifies the maping from the
    * dataset into the GuiTable. </i>
    * 
    * @param ds
    *           Dataset mit den Werten, die gesetzt werden sollen.<br/>
    */
   public final void setDatasetValues(JDataSet ds) {
      if(this.getElementName() == null)
         return;
      String xpath = this.getElementPath("");
      Iterator<JDataRow> ir = ds.getDataRowsPath(xpath);
      /*
       * Löscht hier den Inhalt der Tabelle! Das ist gerade dann notwendig, wenn
       * keine neuen Daten im Dataset vorhanden sind, aber die Tabelle noch alte
       * Daten dargestellt. Deshalb muss das reset() *vor* das return.
       */
      this.reset(); // macht auch setModified(false)
      if(ir == null)
         return;
      int cnt = 0; // ModelElementNumber
      int tableRowCnt = 0; // Angezeigte Tabellenzeilen zählen
      while(ir.hasNext()) { // 1.0 über alle Zeilen
         JDataRow dsrow = ir.next();
         // Zeile schnitzen
         GuiTableRow row = this.insertRow(false); // 1.0.1 Neue Zeile in Tabelle
         row.setInserted(false);
         row.setModelElementNumber(cnt);
         cnt++; // 0-Relativ
         for(int i = 0; i < this.getColCount(); i++) { // 1.1 über alle Spalten
            GuiTableColumn col = this.getColumnModel().getGuiColumn(i);
            GuiComponent comp = col.getGuiComponent();
            String _elementName = col.getElementName();
            if(_elementName != null) { // 1.1.1 element="@..." gesetzt?
               try {
                  JDataValue dVal = dsrow.getDataValue(_elementName); // 1.1.2 Wert
                                                                      // aus DataSet
                                                                      // holen
                  String value = dVal.getValue();
                  if(cnt == 1) { // 1.1.3 Metadaten aus DataSet an Tabellen-Spalte
                                    // weiterreichen (macht nur bei der ersten Zeile
                                 // Sinn)
                     JDataColumn dscol = dVal.getColumn();
                     if(dscol.isNullable() == false) {
                        comp.setNotnull(true);
                     }
                     if(dscol.isReadonly()) {
                        comp.setEnabled(false);
                        col.setColumnEditable(false);
                     }
                  }
                  if(value != null) { // 1.1.4 Wert aus DataSet in Tabellen-Zelle
                                         // (Zeile nicht selektieren!)
                     if(comp instanceof GuiCheck) {
                        row.setValueAt(i, Convert.toBoolean(value), false);
                     }
                     else {
                        row.setValueAt(i, value, false);
                     }
                  }
               }
               catch(Exception ex) {
                  // DataValue kann fehlen
               }
            }
         }
         // Als gelöscht markierte Zeilen gleich wieder löschen
         if(dsrow.isDeleted()) {
            this.deleteRow(tableRowCnt);
         }
         else {
            tableRowCnt++; // andernfalls weiterzählen
         }
      } // wend
      this.setSelectedRow(-1); // clearSelection
      this.setSelectedRow(0); // 2.0 Erste Zeile selektieren
   }

   /**
    * überträgt den Inhalt der Tabelle in den angegebenen Dataset.
    * <p>
    * Vorsicht!<br>
    * Hierbei werden neu in die Tabelle eingefügte Zeilen dem Dataset hinzugefügt; dieser Vorgang ist nicht
    * wiederholbar!
    */
   public void getDatasetValues(JDataSet ds) {
      if(this.isReadonly())
         return;
      // TODO : ModelElementNumber setzen wenn fehlt?
      // Wie ermitteln?
      String xpath = this.getElementPath("");
      Vector<GuiTableRow> data = (Vector<GuiTableRow>)this.getValue();
      if(data == null)
         return;
      // 1. Inserted + Modified Rows
      for(Iterator<GuiTableRow> it = data.iterator(); it.hasNext();) {
         GuiTableRow tableRow = it.next();
         int modelElementNumber = tableRow.getModelElementNumber(); // wenn -1, dann auf inserted setzen?
         // Reihenfolge ist wichtig, da eine Row auch
         // sowohl modified als auch inserted sein kann
         if(tableRow.isDeleted()) {
            // 1.1 deleted Rows s.u.
         }
         else if(tableRow.isInserted()) {
            // 1.2 inserted Rows
            // TODO: Problem:
            // Es wird eine neue Row im Dataset hinzugefügt;
            // dieser Vorgang ist nicht wiederholbar!
            // Alternativ:
            // ModelElement-Nummer ermitteln und der TableRow zuweisen. Wie?
            JDataRow dataRow = ds.addChildRowPath(xpath); // TODO: Nicht hinten
                                                          // anhängen sondern
                                                          // einfügen.
            for(int i = 0; i < this.getColCount(); i++) {
               GuiTableColumn col = this.getColumnModel().getGuiColumn(i);
               GuiComponent comp = col.getGuiComponent();
               String _elementName = comp.getElementName();
               if(_elementName != null) {
                  Object oval = tableRow.getValueAt(i);
                  String value = null;
                  if(oval != null) {
                     value = oval.toString();
                  }
                  // Problem: Hier werden default-Values in der Row
                  // bei notnull mit null aus der GuiTable überbügelt!
                  JDataColumn dataCol = dataRow.getDataColumn(_elementName);
                  if(value == null && dataCol.isNullable() == false && dataCol.getDefaultValue() != null) {
                     // NotNull mit default Value: nix machen!
                  }
                  else {
                     dataRow.setValue(_elementName, value);
                  }
               } // End if elementName
            } // End if inserted
            tableRow.setInserted(false); // TODO d.h. daß #getDatasetValues() nicht
            tableRow.setModified(false); // wiederholbar ist!
            // End of inserted Rows
         }
         else if(tableRow.isModified()) {
            // 1.3 modified Rows
            for(int i = 0; i < this.getColCount(); i++) {
               GuiTableColumn col = this.getColumnModel().getGuiColumn(i);
               String _elementName = col.getElementName();
               if(_elementName != null) { // && col.isColumnEditable()) { //
                                             // Anderung 10.10.06: Nicht editierbare
                                          // Spalten nicht in DataSet zurück; warum?
                  String rowPath = xpath + "[" + Integer.toString(modelElementNumber) + "]" // TODO
                                                                                            // Das
                                                                                            // muß
                                                                                            // anders
                                                                                            // werden!
                        + _elementName;
                  Object oval = tableRow.getValueAt(i);
                  String value = null;
                  if(oval != null) {
                     value = oval.toString();
                  }
                  ds.setValuePath(rowPath, value);
               }
            }
            tableRow.setModified(false); // TODO d.h. daß #getDatasetValues()
            // nicht wiederholbar ist!
            // End of modified Rows
         }
      }
      // 2. Deleted Rows
      Vector<GuiTableRow> v = this.getDeletedRows();
      if(v != null) {
         for(GuiTableRow tableRow : v) {
            int modelElementNumber = tableRow.getModelElementNumber();
            if(modelElementNumber >= 0) { // Neu und danach wieder gelöscht: weglassen
               // TODO Das muß anders werden!   
               String rowPath = xpath + "[" + Integer.toString(modelElementNumber) + "]";
               JDataRow dsRow = ds.getDataRowPath(rowPath);
               dsRow.setDeleted(true);
            }
         }
      }
   }

   public final void commitChanges() {
      super.commitChanges();
      this.getGuiTableModel().commitChanges();
   }

   /**
    * Liefert den Inhalt einer Zelle
    */
   public final String getCellValue(int row, int col) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      Vector<Object> vect = getRowValues(row);
      if(col < 0 || col > vect.size()) {
         throw new IllegalArgumentException("Column Number out of Range: " + Integer.toString(col));
      }
      String ret = vect.elementAt(col).toString();
      return ret;
   }

   /**
    * Liefert den Inhalt der Zelle der angegebenen Zeile der angegebenen Spalte.
    * 
    * @param row
    *           0-relativ
    * @param columnName
    *           Name der Spalte
    * @return
    */
   public final String getCellValue(int row, String columnName) {
      if(row < 0 || row > this.getRowCount()) {
         throw new IllegalStateException("Row Number out of Range: " + row);
      }
      GuiTableRow tRow = this.getRow(row);
      Object val = tRow.getValue(columnName);
      if(val != null) {
         return val.toString();
      }
      else {
         return null;
      }
   }

   public final void setCellValue(int row, String columnName, Object value) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      if(row < 0 || row > this.getRowCount()) {
         throw new IllegalStateException("Row Number out of Range: " + row);
      }
      GuiTableRow tRow = this.getRow(row);
      tRow.setValue(columnName, value);
   }

   /**
    * Setzt den Wert der angegeben Spalte der selektierten Zeile.
    * 
    * @param columnName
    * @param value
    */
   public final void setCellValue(String columnName, Object value) {
      int row = this.getSelectedRow();
      this.setCellValue(row, columnName, value);
   }

   public final void setCellValue(int row, int column, Object value) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      if(row < 0 || row > this.getRowCount()) {
         throw new IllegalStateException("Row Number out of Range: " + row);
      }
      GuiTableRow tRow = this.getRow(row);
      tRow.setValueAt(column, value);
   }

   public final void setCellValue(int column, Object value) {
      int row = this.getSelectedRow();
      this.setCellValue(row, column, value);
   }

   /**
    * Liefert den Inhalt der angegebenen Spalte aus der selektierten Zeile
    */
   public final String getCellValue(int col) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      Vector vect = getRowValues();
      if(col < 0 || col > vect.size()) {
         throw new IllegalArgumentException("Column Number out of Range: " + Integer.toString(col));
      }
      if(vect.elementAt(col) == null) {
         return null;
      }
      else {
         return vect.elementAt(col).toString();
      }
   }

   /**
    * Liefert den Inhalt der Spalte mit dem angegebenen Spaltenname der
    * selektierten Zeile.
    * 
    * @see #getSelectedRow()
    * @see #getRow(int)
    * @see GuiTableRow#getValue(String)
    * @param columnName
    * @return
    */
   public final String getCellValue(String columnName) {
      return this.getCellValue(this.getSelectedRow(), columnName);
   }

   /**
    * Liefert den Inhalt der selektierten Zeile
    */
   public final Vector<Object> getRowValues() {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      return getRowValues(getSelectedRow());
   }

   /**
    * Liefert der Inhalt der Tabelle als Vector von TableRows
    * 
    * @see GuiTableRow
    */
   public final Vector<GuiTableRow> getTableRows() {
      GuiTableModel mdl = this.getGuiTableModel();
      return mdl.getDataVector();
   }

   /**
    * Liefert den Inhalt der angegebenen Tabellenzeile als Vector.
    * Wirft eine Exception, wenn rowNumber außerhalb des gültigen
    * Bereichs ist.
    * 
    * @param rowNumber
    *           Die Zeilennummer beginnend mit 0.
    */
   public final Vector<Object> getRowValues(int index) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }
      if(index < 0 || index >= component.getRowCount()) {
         throw new IllegalArgumentException("rowNumber out of Range: " + Integer.toString(index));
      }
      GuiTableRow row = getRow(index);
      return row.getData();
   }

   private ArrayList<String> getDistinctColumnValues(int column) {
      ArrayList<String> al = new ArrayList<String>();
      for(int i = 0; i < this.getRowCount(); i++) {
         String val = this.getCellValue(i, column);
         if(!al.contains(val)) {
            al.add(val);
         }
      }
      // Sort
      Collections.sort(al);
      return al;
   }

   /**
    * Es wird ein Spezifikations-Script gesetzt, welcher beim Doppelklick auf die
    * Tabelle aufgerufen wird.
    */
   public final void setRowEditor(String name) {
      this.rowEditor = name;
   }

   /**
    * Wenn die Tabelle angeklickt wird, wird CurrentTable bei Rootpane gesetzt
    * 
    * @see GuiRootPane#setCurrentTable(GuiTable)
    */
   public final void click(MouseEvent e) {
      this.getRootPane().setCurrentTable(this);
   }

   /**
    * Tabelle wird doppelt geklickt. Ein ggf. registrierter Editor für die
    * Tabellenzeile wird gestartet.
    */
   public final void d_click(MouseEvent e) {
      // Row-Editor
      if(rowEditor != null) {
         GuiWindow window = null;
         try {
            window = GuiFactory.getInstance().createWindow(rowEditor);
         }
         catch(GDLParseException ex) {
            GuiUtil.showEx(ex);
            return;
         }
         if(window != null) {
            window.show();
         }
      }
      // Weiterleitung DoubleClick
      GuiRootPane pane = this.getRootPane();
      Vector<Object> v = null;
      int selectedRow = getSelectedRow();
      if(selectedRow >= 0 && selectedRow < component.getRowCount()) {
         v = this.getRowValues(selectedRow);

         if(actionDblClick != null && pane != null) {
            pane.obj_TblDoubleClick(this, actionDblClick, selectedRow, v);
         }
      }
   }

   /**
    * Liefert die Nummer der selektierte Zeile (0-relativ) oder -1 wenn Tabelle
    * leer.
    * 
    * @return selectedRowNumber
    */
   public int getSelectedRow() {
      return component.getSelectedRow();
   }

   /**
    * Liefert ein Array, in welchem die Zeilennummern der selektierten Rows
    * enthalten sind.
    * 
    * @return
    */
   public final int[] getSelectedRows() {
      return component.getSelectedRows();
   }

   public final int getSelectedColumn() {
      return component.getSelectedColumn();
   }

   public final int[] getSelectedColumns() {
      return component.getSelectedColumns();
   }

   /**
    * Setzt die selektierte Zeile der Tabelle neu.
    * -1 = clearSelection
    * 
    * @param index
    */
   public final void setSelectedRow(int index) {
      if(index == -1) {
         this.clearSelection();
      }
      else {
         this.setSelectedRows(index, index);
      }
   }

   public void setSelectedRow(GuiTableRow trow) {
      if(trow == null) {
         return;
      }
      for(int i = 0; i < this.getRowCount(); i++) {
         GuiTableRow xrow = this.getRow(i);
         if(xrow == trow) {
            this.setSelectedRow(i);
            return;
         }
      }
   }

   /**
    * ggf. selektierte Zeilen werden deselektiert.
    */
   public void clearSelection() {
      this.component.clearSelection();
      this.lastSelectedRow = -1;
   }

   /**
    * @formatter:off
    *                Selektiert eine weitere Zelle;
    *                ggf. zuvor clearSelection aufrufen, um alle Selektionen wieder zu löschen
    * @param row
    * @param col
    * @formatter:on
    */
   public void addSelection(int row, int col) {
      if(!ready) {
         throw new IllegalStateException("Table not ready");
      }

      ListSelectionModel lsm = component.getSelectionModel();
      lsm.addSelectionInterval(row, row);
      if(col != -1) {
         ListSelectionModel lsmc = component.getColumnModel().getSelectionModel();
         lsmc.addSelectionInterval(col, col);
      }
   }

   /**
    * Markiert ein Interval von Zeilen.
    * <p>
    * Nicht erlaubte Parameter werden stillschweigend angepaßt.
    * 
    * @param fromIndex
    * @param toIndex
    */
   public final void setSelectedRows(int fromIndex, int toIndex) {
      if(this.getRowCount() == 0)
         return;
      if(fromIndex < 0)
         fromIndex = 0;
      if(toIndex < fromIndex)
         toIndex = fromIndex;
      if(fromIndex >= this.getRowCount()) {
         fromIndex = this.getRowCount() - 1;
      }
      if(toIndex >= this.getRowCount()) {
         toIndex = this.getRowCount() - 1;
      }
      ListSelectionModel lstModel = component.getSelectionModel();
      lstModel.setSelectionInterval(fromIndex, toIndex);
      component.scrollRectToVisible(component.getCellRect(component.getSelectedRow(), 0, false));
      this.setLastSelectedRow(fromIndex);
   }

   public void setSelectedRows(int[] index) {
      if(index == null)
         return;
      ListSelectionModel lstModel = component.getSelectionModel();
      lstModel.clearSelection();
      for(int i = 0; i < index.length; i++) {
         lstModel.addSelectionInterval(index[i], index[i]);
      }
      component.scrollRectToVisible(component.getCellRect(component.getSelectedRow(), 0, false));

   }

   /**
    * Sucht die (erste) Zeile mit dem angegebenen Wert in der angegebenen Spalte.
    * 
    * @param columnName
    * @param columnValue
    * @return -1 wenn nix gefunden
    */
   public int findRow(String columnName, Object columnValue) {
      for(int i = 0; i < component.getRowCount(); i++) {
         GuiTableRow row = getRow(i);
         Object val = row.getValue(columnName);
         if(columnValue == null) {
            if(val == null)
               return i;
         }
         else if(columnValue.equals(val)) {
            return i;
         }
      }
      return -1;
   }

   /**
    * Da JTable (seit Jahren!) massive Focus-Probleme hat, wird diese Methode
    * benötigt.
    * 
    * @see GuiElement#lostFocus(FocusEvent)
    */
   public final void stopCellEditing() {
      try {
         TableCellEditor editor = component.getCellEditor();
         if(editor != null) {
            editor.stopCellEditing();
         }
         else { // Neu 15.9.2005
            int row = this.getSelectedRow();
            int col = this.getSelectedColumn();
            if(row >= 0 && col >= 0) {
               editor = component.getCellEditor(row, col);
               if(editor != null) {
                  editor.stopCellEditing();
               }
            }
         }
      }
      catch(Exception ex) { // Wenn Cell nicht editierbar
         // ex.printStackTrace();
      }
   }

   /**
    * Sortiert die Tabelle nach dem Inhalt der angegebenen Spalte.
    * 
    * @param colIndex
    *           Das Sortierkriterium
    * @param ascending
    *           false = descending
    */
   public void sortRows(int colIndex, boolean ascending) {
      // selektierte Zeile merken; TODO: Mehrfachselektion auch unterstützen?
      GuiTableRow trow = this.getRow();
      // Sort
      GuiTableModel model = this.getGuiTableModel();
      Vector<GuiTableRow> data = model.getDataVector();
      Collections.sort(data, new ColumnSorter(this, colIndex, ascending));
      model.fireTableStructureChanged();
      this.ascending = ascending;
      GuiTableColumn col = this.getColumn(colIndex);
      Icon icon;
      if(ascending) {
         icon = GuiUtil.makeIcon("/icons/Down16.gif");
      }
      else {
         icon = GuiUtil.makeIcon("/icons/Up16.gif");
      }
      Object val = col.getHeaderValue();
      if(val instanceof JLabel) {
         JLabel lbl = (JLabel)val;
         lbl.setIcon(icon);
      } else if(val instanceof String) {
         // tt nicht überbügeln
         String tt = null;
         TableCellRenderer renderer = col.getHeaderRenderer();
         if (renderer == null) {
            renderer = new JComponentTableCellRenderer();
            col.setHeaderRenderer(renderer);
         } else if (renderer instanceof DefaultTableCellHeaderRenderer) {
            DefaultTableCellHeaderRenderer hRenderer = (DefaultTableCellHeaderRenderer)renderer;
            hRenderer.setIcon(icon); // funktioniert nicht?
            tt = hRenderer.getToolTipText();
            renderer = new JComponentTableCellRenderer();
            col.setHeaderRenderer(renderer);
         }
         JLabel lbl = new JLabel((String)val, icon, JLabel.LEFT);
         if (tt != null) {
            lbl.setToolTipText(tt);
         }
         col.setHeaderValue(lbl);
         Border headerBorder = UIManager.getBorder("TableHeader.cellBorder");
         lbl.setBorder(headerBorder);
      }
      if(sortedColumnName != null && !col.getName().equals(sortedColumnName)) {
         GuiTableColumn prevCol = this.getColumn(sortedColumnName);
         Object pval = prevCol.getHeaderValue();
         if(pval instanceof JLabel) {
            JLabel lbl = (JLabel)pval;
            lbl.setIcon(null);
         }
      }
      JTableHeader th = component.getTableHeader();
      th.repaint();
      this.sortedColumnName = this.getColumn(colIndex).getName();
      // Selektion wieder herstellen
      if(trow == null) {
         this.setSelectedRow(-1);
      }
      else {
         this.setSelectedRow(trow);
      }
   }

   // Add a mouse listener to the Table to trigger a table sort
   // when a column heading is clicked in the JTable.
   private void addMouseListenerToHeader() {
      component.setColumnSelectionAllowed(false);
      MouseAdapter listMouseListener = new MouseAdapter() {
         private boolean popup;

         public void mouseReleased(MouseEvent e) {
            if(e.isPopupTrigger()) // Windows
               popup = true;
         }

         public void mousePressed(MouseEvent e) {
            if(e.isPopupTrigger()) // Linux
               popup = true;
            // PKÖ 15.9.2005 muß hier bei mousePressed erfolgen statt bei
            // mouseClicked!
            stopCellEditing();
            // TODO: Das editieren von linkCols muß auch beendet werden!
         }

         public void mouseClicked(MouseEvent e) {
            TableColumnModel columnModel = getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = component.convertColumnIndexToModel(viewColumn);
            if(e.getClickCount() == 1 && column != -1) {
               if(!popup) {
                  int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
                  String colName = getColumn(column).getName();
                  if(shiftPressed == 0 && colName.equals(sortedColumnName)) {
                     ascending = !ascending;
                  }
                  else {
                     ascending = (shiftPressed == 0);
                  }
                  sortRows(column, ascending);
               }
            }
            popup = false; // wieder zurücksetzen
         }
      };
      JTableHeader th = component.getTableHeader();
      th.addMouseListener(listMouseListener);
   }

   /**
    * Liefert das Kennzeichen, ob die Spaltenbreite automatisch errechnet wird.
    * 
    * @return
    */
   public boolean isAutoSize() {
      return autoSize;
   }

   /**
    * @param b
    */
   void setAutoSize(boolean b) {
      if(b) {
         component.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      }
      else {
         component.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      }
      autoSize = b;
   }

   //	/**
   //	 * Kein paste wenn deaktiviert
   //	 */
   //	public void setEnabled(boolean b) {
   //		super.setEnabled(b);
   //		if(this.excelAdapter != null) {
   //			excelAdapter.setPasteActive(b);
   //		}
   //	}

   private int[] getDisabledColumns() {
      ArrayList<Integer> al = new ArrayList<Integer>();
      for(int i = 0; i < this.getColCount(); i++) {
         GuiTableColumn col = this.getColumn(i);
         if(!col.isColumnEditable()) {
            al.add(i);
         }
      }
      if(al.size() == 0) {
         return null;
      }
      int[] ret = new int[al.size()];
      for(int i = 0; i < al.size(); i++) {
         ret[i] = al.get(i);
      }
      return ret;
   }

   public void search(String pattern, boolean ignorecase, boolean regex) {
      if(pattern == null)
         return;
      this.clearSelection();
      if(ignorecase) {
         pattern = pattern.toLowerCase();
      }
      for(int i = 0; i < getRowCount(); i++) {
         GuiTableRow row = getRow(i);
         for(int j = 0; j < getColCount(); j++) {
            Object o = row.getValueAt(j);
            String val = Convert.toString(o);
            if(val == null)
               continue;
            if(ignorecase) {
               val = val.toLowerCase();
            }
            int p = val.indexOf(pattern); // TODO:  regex
            if(p > -1) {
               this.addSelection(i, j);
            }
         }
      }
   }

   public void getPreferences(JDataSet ds) {
      if(ds == null)
         return;
      // Meta Table
      JDataTable tblT = null;
      try {
         tblT = ds.getDataTable("GuiTable");
      }
      catch(Exception ex) {
         tblT = new JDataTable("GuiTable");
         ds.addRootTable(tblT);
         JDataColumn colName = tblT.addColumn("name", Types.VARCHAR);
         colName.setPrimaryKey(true);
         JDataColumn colAuto = tblT.addColumn("autosize", Types.BOOLEAN);
         colAuto.setDefaultValue("false");
         JDataColumn colVis = tblT.addColumn("visible", Types.BOOLEAN);
         colVis.setDefaultValue("true");
         JDataColumn colEn = tblT.addColumn("enabled", Types.BOOLEAN);
         colEn.setDefaultValue("true");
      }
      if(!tblT.hasDataColumn("reorderColumns")) {
         JDataColumn colOrder = tblT.addColumn("reorderColumns", Types.BOOLEAN);
         colOrder.setDefaultValue("false");
      }
      if(!tblT.hasDataColumn("sortedColumn")) {
         JDataColumn colSort = tblT.addColumn("sortedColumn", Types.VARCHAR);
      }
      if(!tblT.hasDataColumn("ascending")) {
         JDataColumn colAsc = tblT.addColumn("ascending", Types.BOOLEAN);
         colAsc.setDefaultValue("true");
      }
      // Prefs Table
      JDataRow rowT;
      Collection<JDataRow> trows = null;
      try {
         trows = ds.getChildRows(new DataView("GuiTable", new NVPair("name", this.getName(), Types.VARCHAR)));
      }
      catch(Exception ex) {
      }
      if(trows != null && trows.size() >= 1) {
         rowT = trows.iterator().next();
      }
      else {
         rowT = ds.createChildRow("GuiTable");
      }
      rowT.setValue("name", this.getName());
      rowT.setValue("reorderColumns", component.getTableHeader().getReorderingAllowed());
      rowT.setValue("autosize", this.isAutoSize());
      rowT.setValue("visible", this.isVisible());
      rowT.setValue("enabled", this.isEnabled());
      rowT.setValue("sortedColumn", this.sortedColumnName);
      rowT.setValue("ascending", this.ascending);
      // MetaColumn
      JDataTable tblC = null;
      try {
         tblC = tblT.getChildTable("GuiColumn");
      }
      catch(Exception ex) {
         tblC = new JDataTable("GuiColumn");
         JDataColumn colName = tblC.addColumn("name", Types.VARCHAR);
         colName.setPrimaryKey(true);
         tblC.addColumn("width", Types.INTEGER);
         tblC.addColumn("minWidth", Types.INTEGER);
         tblC.addColumn("preferredWidth", Types.INTEGER);
         tblC.addColumn("maxWidth", Types.INTEGER);
         JDataColumn colSort = tblC.addColumn("sorted", Types.VARCHAR);
         colSort.setDefaultValue("ASC");
         JDataColumn colVis = tblC.addColumn("visible", Types.BOOLEAN);
         colVis.setDefaultValue("true");
         JDataColumn colEn = tblC.addColumn("enabled", Types.BOOLEAN);
         colEn.setDefaultValue("true");
         tblT.addChildTable(tblC, "name");
      }
      // Prefs Columns
      for(int i = 0; i < colModel.getColumnCount(); i++) {
         GuiTableColumn col = colModel.getGuiColumn(i);
         JDataRow rowC;
         Collection<JDataRow> crows = null;
         try {
            crows = rowT.getChildRows(new DataView("GuiColumn", new NVPair("name", col.getName(), Types.VARCHAR)));
         }
         catch(Exception ex) {
         }
         if(crows != null && crows.size() >= 1) {
            rowC = crows.iterator().next();
         }
         else {
            rowC = rowT.createChildRow("GuiColumn");
         }
         rowC.setValue("name", col.getName());
         rowC.setValue("visible", col.isVisible());
         rowC.setValue("enabled", col.isColumnEditable());
         if(col.isVisible()) {
            rowC.setValue("width", col.getWidth());
            rowC.setValue("minWidth", col.getMinWidth());
            rowC.setValue("maxWidth", col.getMaxWidth());
            rowC.setValue("preferredWidth", col.getPreferredWidth());
            rowC.setValue("enabled", col.isColumnEditable());
         }
         else { // not visible
            rowC.setValue("width", col.getDefaultWidth());
            rowC.setValue("minWidth", col.getDefaultMinWidth());
            rowC.setValue("maxWidth", col.getDefaultMaxWidth());
            rowC.setValue("preferredWidth", col.getDefaultPreferredWidth());
            rowC.setValue("enabled", col.getDefaultColumnEditable());
         }
      }
   }

   public void setPreferences(JDataSet ds) {
      if(ds == null)
         return;
      Collection<JDataRow> coll = ds.getChildRows(new DataView("GuiTable", new NVPair("name", this.getName(), Types.VARCHAR)));
      if(coll.size() != 1)
         return;
      JDataRow row = coll.iterator().next();
      this.setAutoSize(row.getValueBool("autosize"));
      this.component.getTableHeader().setReorderingAllowed(row.getValueBool("reorderColumns"));
      this.setVisible(row.getValueBool("visible"));
      this.setEnabled(row.getValueBool("enabled"));
      Iterator<JDataRow> itc = row.getChildRows("GuiColumn");
      if(itc == null)
         return;
      while(itc.hasNext()) {
         JDataRow rowC = itc.next();
         GuiTableColumn col = this.getColumn(rowC.getValue("name"));
         if(col == null) {
            continue;
         }
         col.setVisible(rowC.getValueBool("visible"));
         if(col.isVisible()) {
            col.setMinWidth(rowC.getValueInt("minWidth"));
            col.setMaxWidth(rowC.getValueInt("maxWidth"));
            col.setPreferredWidth(rowC.getValueInt("preferredWidth"));
            col.setWidth(rowC.getValueInt("width"));
            col.setColumnEditable(rowC.getValueBool("enabled"));
         }
         else { // not visible
            col.setDefaultMinWidth(rowC.getValueInt("minWidth"));
            col.setDefaultMaxWidth(rowC.getValueInt("maxWidth"));
            col.setDefaultPreferredWidth(rowC.getValueInt("preferredWidth"));
            col.setDefaultWidth(rowC.getValueInt("width"));
            col.setDefaultColumnEditable(rowC.getValueBool("enabled"));
         }
      }
   }

   public void showPopupMenu(int modi, int x, int y) {
      switch(modi) {
         case InputEvent.CTRL_DOWN_MASK:
            ActionListener al = new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                  String cmd = e.getActionCommand();
                  if(cmd.equalsIgnoreCase("Autosize")) {
                     JCheckBoxMenuItem cb = (JCheckBoxMenuItem)e.getSource();
                     setAutoSize(cb.isSelected());
                  }
                  else if(cmd.equalsIgnoreCase("ReorderColumns")) {
                     JCheckBoxMenuItem cb = (JCheckBoxMenuItem)e.getSource();
                     component.getTableHeader().setReorderingAllowed(cb.isSelected());
                  }
                  else if(cmd.startsWith("visible")) {
                     JCheckBoxMenuItem cb = (JCheckBoxMenuItem)e.getSource();
                     String colName = cmd.substring(7);
                     GuiTableColumn col = getColumn(colName);
                     col.setVisible(cb.isSelected());
                  }
                  else if(cmd.startsWith("enabled")) {
                     JCheckBoxMenuItem cb = (JCheckBoxMenuItem)e.getSource();
                     String colName = cmd.substring(7);
                     GuiTableColumn col = getColumn(colName);
                     col.setColumnEditable(cb.isSelected());
                  }
                  else if(cmd.startsWith("edit")) {
                     //JMenuItem mi = (JMenuItem)e.getSource();
                     String colName = cmd.substring(4);
                     GuiTableColumn col = getColumn(colName);
                     String editDia = "<?xml version='1.0' encoding='UTF-8'?>" + "<!DOCTYPE GDL SYSTEM 'gdl.dtd'>" + "<GDL>" + "<Dialog label='Edit Column Preferences' size='200,220'>" + "<Number label='MinWidth:' fill='H' />" + "<Number label='MaxWidth:' fill='H'/>"
                           + "<Number label='Width:' fill='H'/>" + "<Number label='PreferredWidth:' fill='H'/>" + "<Check label='Visible' x='1'/>" + "<Check label='Editable' x='1' it='0'/>" + "<Panel wy='0' w='3' layout='FLOW'>" + "   <Button label='OK' />" + "   <Button label='Cancel' />"
                           + "</Panel>" + "</Dialog>" + "</GDL>";
                     Document doc;
                     try {
                        doc = new Document(editDia);
                        GuiDialog dia = (GuiDialog)GuiFactory.getInstance().createWindow(doc, (GuiWindow)null);
                        dia.setValue("minWidth", col.getMinWidth());
                        dia.setValue("maxWidth", col.getMaxWidth());
                        dia.setValue("width", col.getWidth());
                        dia.setValue("preferredWidth", col.getPreferredWidth());
                        dia.setValue("editable", col.isColumnEditable());
                        dia.setValue("visible", col.isVisible());
                        if(dia.showDialog()) {
                           int minWidth = Convert.toInt(dia.getValue("minWidth"));
                           int maxWidth = Convert.toInt(dia.getValue("maxWidth"));
                           int preferredWidth = Convert.toInt(dia.getValue("preferredWidth"));
                           if(minWidth > maxWidth) {
                              minWidth = maxWidth;
                           }
                           if(minWidth > preferredWidth) {
                              minWidth = preferredWidth;
                           }
                           if(maxWidth < preferredWidth) {
                              maxWidth = preferredWidth;
                           }
                           col.setMinWidth(minWidth);
                           col.setMaxWidth(maxWidth);
                           col.setWidth(Convert.toInt(dia.getValue("width")));
                           col.setPreferredWidth(preferredWidth);
                           col.setColumnEditable(Convert.toBoolean(dia.getValue("editable")));
                           col.setVisible(Convert.toBoolean(dia.getValue("visible")));
                        }
                     }
                     catch(Exception e1) {
                        logger.error(e1.getMessage(), e1);
                        GuiUtil.showEx(e1);
                     }
                  }
               }
            };

            GuiPopupMenu pop = new GuiPopupMenu("Preferences");
            GuiMenuItemCheckBox itemAuto = new GuiMenuItemCheckBox("Autosize", pop);
            itemAuto.getButton().addActionListener(al);
            itemAuto.setSelected(this.isAutoSize());
            itemAuto.setFont(component.getFont());

            GuiMenuItemCheckBox itemOrder = new GuiMenuItemCheckBox("Reorder Columns", pop);
            itemOrder.getButton().addActionListener(al);
            itemOrder.setSelected(this.component.getTableHeader().getReorderingAllowed());
            itemOrder.setFont(component.getFont());

            GuiMenu mVisible = new GuiMenu("Visible...");
            mVisible.setFont(component.getFont());
            pop.add(mVisible);
            for(int i = 0; i < colModel.getColumnCount(); i++) {
               GuiTableColumn col = colModel.getGuiColumn(i);
               GuiMenuItemCheckBox item = new GuiMenuItemCheckBox(col.getName(), mVisible);
               item.setActionCommand("visible" + col.getName());
               item.setSelected(col.isVisible());
               item.getButton().addActionListener(al);
               item.setFont(component.getFont());
            }
            GuiMenu mEnabled = new GuiMenu("Editable...");
            mEnabled.setFont(component.getFont());
            pop.add(mEnabled);
            for(int i = 0; i < colModel.getColumnCount(); i++) {
               GuiTableColumn col = colModel.getGuiColumn(i);
               GuiMenuItemCheckBox item = new GuiMenuItemCheckBox(col.getName(), mEnabled);
               item.setActionCommand("enabled" + col.getName());
               item.setSelected(col.isColumnEditable());
               item.getButton().addActionListener(al);
               item.setFont(component.getFont());
            }
            GuiMenu mEdit = new GuiMenu("Edit Prefs...");
            mEdit.setFont(component.getFont());
            pop.add(mEdit);
            for(int i = 0; i < colModel.getColumnCount(); i++) {
               GuiTableColumn col = colModel.getGuiColumn(i);
               GuiMenuItem item = new GuiMenuItem(col.getName(), mEdit);
               item.setActionCommand("edit" + col.getName());
               item.getMenuItem().addActionListener(al);
               item.setFont(component.getFont());
            }
            pop.show(this.component, x, y);
            break;
      }
   }

   final void dispose() {
      colModel = null;
      colVector = null;
      component = null;
      pasteRow = null;
      rowVector = null;
      this.setGuiParent(null);
   }

   // End Of GuiTable

   private static class JComponentTableCellRenderer implements TableCellRenderer {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
         return (JComponent)value;
      }
   }

   // Inner Classes
   // ****************************************************************
   // This comparator is used to sort vectors of data
   private static final class ColumnSorter implements Comparator {
      // Attributes
      private SimpleDateFormat timestampFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); // Format mit Uhrzeit
      private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy"); // Format nur Datum
      private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

      private NumberFormat numFormat = NumberFormat.getInstance();

      private int colIndex;

      private boolean ascending;

      private int datatype;

      // Constructor
      ColumnSorter(GuiTable tbl, int colIndex, boolean ascending) {
         this.colIndex = colIndex;
         this.ascending = ascending;
         this.datatype = tbl.getColumnModel().getGuiColumn(colIndex).getDataType();
      }

      // Methods
      /**
       * From Comparator
       */
      public int compare(Object a, Object b) {
         Vector v1 = ((GuiTableRow)a).getData();
         Vector v2 = ((GuiTableRow)b).getData();
         Object o1 = v1.get(colIndex);
         Object o2 = v2.get(colIndex);

         // Treat empty strains like nulls
         if(o1 instanceof String && ((String)o1).length() == 0) {
            o1 = null;
         }
         if(o2 instanceof String && ((String)o2).length() == 0) {
            o2 = null;
         }

         // Berücksichtigung von Null-Werten je nach Sortierrichtung
         // Datenbanken sortieren die Null-Werte u.U. andresherum!?
         if(o1 == null && o2 == null) {
            return 0; // Beides null
         }
         else if(o1 == null) { // erster Wert null
            if(ascending)
               return -1;
            else
               return 1;
         }
         else if(o2 == null) { // zweiter Wert null
            if(ascending)
               return 1;
            else
               return -1;
         }
         switch(datatype) {
            case GuiComponent.DATE:
               try {
                  o1 = timestampFormat.parse(o1.toString()); // Timestamp? (wirft bei Date eine Exception!)
               }
               catch(Exception ex) {
                  try {
                     o1 = dateFormat.parse(o1.toString()); // Date
                  }
                  catch(Exception tex) {
                     try {
                        o1 = timestampFormat.parse(removeHash(o1.toString())); // Hash
                     }
                     catch(Exception e) {
                     }
                  }
               }
               try {
                  o2 = timestampFormat.parse(o2.toString());
               }
               catch(Exception ex) {
                  try {
                     o2 = dateFormat.parse(o2.toString());
                  }
                  catch(Exception tex) {
                     try {
                        o2 = timestampFormat.parse(removeHash(o2.toString()));
                     }
                     catch(Exception e) {
                     }
                  }
               }
               break;
            case GuiComponent.TIME:
               try {
                  o1 = timeFormat.parse(o1.toString());
               }
               catch(Exception ex) {
                  try {
                     o1 = timeFormat.parse(removeHash(o1.toString()));
                  }
                  catch(Exception e) {
                  }
               }
               try {
                  o2 = timeFormat.parse(o2.toString());
               }
               catch(Exception ex) {
                  try {
                     o2 = timeFormat.parse(removeHash(o2.toString()));
                  }
                  catch(Exception e) {
                  }
               }
               break;
            // Achtung: Number ist nicht comparable!
            case GuiComponent.NUMBER: // auch Money
               try {
                  if(o1 instanceof Double) {
                     // OK
                  }
                  else {
                     o1 = numFormat.parse(o1.toString());
                     if(o1 instanceof Long) { // Wenn ganzzahlig dann Double draus machen!
                        o1 = new Double(((Long)o1).longValue());
                     }
                  }
               }
               catch(Exception ex) {
                  //System.out.println(ex.getMessage());
                  try {
                     o1 = numFormat.parseObject(removeHash(o1.toString()));
                  }
                  catch(ParseException e) {
                     //System.out.println(e.getMessage());
                  }
               }
               try {
                  if(o2 instanceof Double) {
                     // OK
                  }
                  else {
                     o2 = numFormat.parse(o2.toString());
                     if(o2 instanceof Long) {
                        o2 = new Double(((Long)o2).longValue());
                     }
                  }
               }
               catch(Exception ex) {
                  //System.out.println(ex.getMessage());
                  try {
                     o2 = numFormat.parseObject(removeHash(o2.toString()));
                  }
                  catch(ParseException e) {
                     //System.out.println(e.getMessage());
                  }
               }
               break;
         }
         int ret = 0;
         // Strings
         // mit Standard Sortierreihenfolge (Collator)
         // der jeweiligen Standard-Gebietseinstellung (Locale)
         if(o1 instanceof String && o2 instanceof String) {
            Collator c = Collator.getInstance();
            c.setStrength(Collator.PRIMARY);
            ret = c.compare(o1, o2);
         }
         // Date
         else if(o1 instanceof Date && o2 instanceof Date) {
            ret = ((Comparable)o1).compareTo(o2);
         } // Number
         else if(o1 instanceof Number && o2 instanceof Number) {
            double d1 = ((Number)o1).doubleValue();
            double d2 = ((Number)o2).doubleValue();
            //ret = ((Comparable)o1).compareTo(o2);
            ret = Double.compare(d1, d2);
            //System.out.println(d1 + ":" + d2 + " - " + ret);
            return ascending ? ret : -ret;
         }
         else { // Mischmasch: als String vergleichen
            ret = o1.toString().compareToIgnoreCase(o2.toString());
         }
         return ascending ? ret : -ret;
      }
   }

   /**
    * Ein # eingeschlossene Daten werden entfernt
    * 
    * @param s
    * @return
    */
   private static String removeHash(String s) {
      int p1 = s.indexOf("#");
      int p2 = s.indexOf("#", p1 + 1);
      if(p1 == -1 || p2 == -1)
         return s;
      do {
         s = s.substring(0, p1) + s.substring(p1 + p2 + 1);
         p1 = s.indexOf("#");
         p2 = s.indexOf("#", p1 + 1);
      }
      while(p1 != -1 && p2 != -1);
      return s;
   }

   /**
    * Dient der Darstellung von CheckBoxen in einer Tabelle.
    * <p>
    * Einige Methoden geklaut von swing.table.DefaultTableCellRenderer.
    * 
    * @see GuiTable#addColumn
    */
   private final class GuiTableCheckRenderer extends JCheckBox implements TableCellRenderer {
      private static final long serialVersionUID = 1L;

      // Constructor
      GuiTableCheckRenderer(boolean displayOnly) {
         super();
         this.setOpaque(true);
         this.setHorizontalAlignment(SwingConstants.CENTER);
         this.setEnabled(!displayOnly);
      }

      // Methods
      /**
       * From Interface TableCellRenderer
       */
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
         //Component cmp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

         setFont(table.getFont());

         if(isSelected) {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
         }
         else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
         }
         boolean bval = Convert.toBoolean(value); // TODO: map
         this.setSelected(bval);

         return this;
         //return cmp;
      }

      //		private Border getNoFocusBorder() {
      //			Border border = DefaultLookup.getBorder(this, ui, "Table.cellNoFocusBorder");
      //			if (System.getSecurityManager() != null) {
      //				 if (border != null) return border;
      //				 return SAFE_NO_FOCUS_BORDER;
      //			} else if (border != null) {
      //				 if (noFocusBorder == null || noFocusBorder == DEFAULT_NO_FOCUS_BORDER) {
      //					  return border;
      //				 }
      //			}
      //			return noFocusBorder;
      //	  }
      //
      //		/**
      //		 * Overridden for performance reasons.
      //		 */
      //		public void validate() {
      //		}
      //
      //		/**
      //		 * Overridden for performance reasons.
      //		 */
      //		public void revalidate() {
      //		}
      //
      //		/**
      //		 * Overridden for performance reasons.
      //		 */
      //		public void repaint(long tm, int x, int y, int width, int height) {
      //		}
      //
      //		/**
      //		 * Overridden for performance reasons.
      //		 */
      //		public void repaint(Rectangle r) {
      //		}

      /**
       * Overridden for performance reasons.
       */
      protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
         // Strings get interned...
         if(propertyName.equals("text")) {
            this.firePropertyChange(propertyName, oldValue, newValue);
         }
      }

      //		/**
      //		 * Overridden for performance reasons.
      //		 */
      //		public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
      //		}

   } // End Of GuiTableCheckRenderer

   /**
    * Dient der Darstellung von ComboBoxen in einer Tabelle.
    * <p>
    * Einige Methoden geklaut von swing.table.DefaultTableCellRenderer.
    * 
    * @see GuiTable#addColumn
    */
   public final class GuiTableComboRenderer extends DefaultTableCellRenderer {
      private static final long serialVersionUID = 1L;
      private GuiCombo comp;

      // Constructor
      GuiTableComboRenderer(GuiCombo c, boolean displayOnly) {
         super();
         this.comp = c;
         this.setOpaque(true);
         this.setEnabled(!displayOnly);
         this.setFont(component.getFont());
         this.setBackground(c.getBackground());
      }

      // Methods
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
         Component cmp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         if(isSelected) {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
         }
         else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
         }

         try {
            comp.setValue(value);
            if(value != null) { // Wenn null, dann ist kein Mapping möglich //
                                   // 25.4.2005 PKÖ
               Object oMap = comp.getDisplayMemberValue(value);
               String sMap = null;
               if(oMap != null) {
                  sMap = oMap.toString();
               }
               this.setText(sMap);
            }
            else {
               this.setText(null); // Neu 24.7.2009
            }
         }
         catch(Exception ex) {
            String msg = "GuiTableComboRenderer#getTableCellRendererComponent: " + ex.getMessage();
            System.err.println(msg);
            logger.error(msg, ex);
         }
         //return this;
         return cmp;
      }

      public GuiCombo getGuiCombo() {
         return this.comp;
      }

      //		public void validate() {
      //		}
      //
      //		public void revalidate() {
      //		}
      //
      //		public void repaint(long tm, int x, int y, int width, int height) {
      //		}
      //
      //		public void repaint(Rectangle r) {
      //		}
      //
      //		protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
      //		}
      //
      //		public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
      //		}

   } // End Of GuiTableComboRenderer

   private class GuiTableComboEditor extends DefaultCellEditor {
      private static final long serialVersionUID = 1L;
      // TODO : muß der seinen Renderer kennen?
      // Attributes
      private TableComboColumn comp;

      // Constructor
      GuiTableComboEditor(TableComboColumn c) {
         super(c.getCombo()); // Müll! Aber geht nicht anders!
         final JComboBox comboBox = c.getCombo();
         this.comp = c;
         // ########
         editorComponent = comboBox;
         comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
         delegate = new EditorDelegate() {
            public void setValue(Object value) {
               comp.setValue(value);
            }

            public Object getCellEditorValue() {
               return comp.getValue();
            }

            //				public boolean shouldSelectCell(EventObject anEvent) {
            ////					if(anEvent instanceof MouseEvent) {
            ////						MouseEvent e = (MouseEvent)anEvent;
            ////						return e.getID() != MouseEvent.MOUSE_DRAGGED;
            ////					}
            //					return true;
            //				}

            @Override
            public boolean stopCellEditing() {
               if(comboBox.isEditable()) {
                  // Commit edited value.
                  comboBox.actionPerformed(new ActionEvent(GuiTableComboEditor.this, 0, ""));
               }
               return super.stopCellEditing();
            }
         }; // End Of EditorDelegate
         comboBox.addActionListener(delegate);
      }

      // Methods
      @Override
      public Object getCellEditorValue() {
         return comp.getValue();
      }
   }

   @SuppressWarnings("serial")
   private final class GuiTextCellEditor extends DefaultCellEditor {
      private GuiText guiComponent;

      public GuiTextCellEditor(GuiText guiComponent) {
         super((JTextField)guiComponent.getJComponent());
         this.guiComponent = guiComponent;
         this.guiComponent.setFont(component.getFont());
      }

      // Override to invoke setValue on the formatted text field.
      @Override
      public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

         JTextField ftf = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
         this.guiComponent.setValue(value);
         return ftf;
      }

      @Override
      public Object getCellEditorValue() {
         Object o = this.guiComponent.getValue();
         return o;
      }

      // Override to check whether the edit is valid,
      // setting the value if it is and complaining if
      // it isn't. If it's OK for the editor to go
      // away, we need to invoke the superclass's version
      // of this method so that everything gets cleaned up.
      @Override
      public boolean stopCellEditing() {
         if(guiComponent.isValid()) { // OK
            return super.stopCellEditing();
         }
         Toolkit.getDefaultToolkit().beep(); // Fehler
         return false;
      }
   }

   /**
    * Renderer für Spalten vom Typ GuiText. <br>
    * Dient vor allem der Darstellung von numerischen Formaten in einer Tabelle.
    * <p>
    * 
    * @see GuiTable#addColumn
    */
   @SuppressWarnings("serial")
   private final class GuiTableFormatRenderer extends DefaultTableCellRenderer {
      /**
       * Verweis auf den TableCellEditor
       */
      private TableTextColumn editor;

      // Constructor
      /**
       * Es ist der TableCellEditor der Spalte zu übergeben; vor allem die von
       * GuiText abgeleiteten Klassen wie Date, Time, Money, Number.
       */
      GuiTableFormatRenderer(TableTextColumn editor) {
         super();
         this.editor = editor;
         this.setHorizontalAlignment(editor.getHorizontalAlignment());
         this.setBackground(editor.getBackground()); // geht nicht??
         this.setForeground(editor.getForeground());
      }

      // Methods

      /*
       * public Component getTableCellRendererComponent( JTable table, Object
       * color, boolean isSelected, boolean hasFocus, int row, int column) { if
       * (isBordered) { if (isSelected) {
       * //selectedBorder is a solid border in the color
       * //table.getSelectionBackground(). setBorder(selectedBorder); } else {
       * //unselectedBorder is a solid border in the color
       * //table.getBackground(). setBorder(super.) setBorder(unselectedBorder); } }
       * return this; }
       */

      /**
       * Overlay; Ruft editor.makeFormat auf
       * 
       * @see GuiText#makeFormat
       */
      protected void setValue(Object value) {
         try {
            editor.postProc();
            // setText((value == null) ? "" : editor.makeFormat(value.toString()));
            String s = "";
            if(value == null) {
               // nix
            }
            else if(value instanceof Number) {
               s = editor.makeFormat((Number)value);
            }
            else {
               s = editor.makeFormat(value.toString());
            }
            setText(s);
         }
         catch(Exception px) {
            setText((value == null) ? "" : value.toString());
         }
      }

      public final boolean isFocusable() {
         return editor.hasTabstop();
      }

      void click(MouseEvent e) {
      }

      /**
       * Wird an den Editor weiter gereicht.
       */
      void d_click(MouseEvent e) {
         editor.d_click(e);
      }

      /**
       * Fügt dem Renderer einen MouseListener hinzu.
       */
      void createMouseListener() {
         super.addMouseListener(new MouseListener() {
            // Methods
            /**
             * Es wird die Methode click oder d_click des Editors aufgerufen.
             */
            public void mouseClicked(MouseEvent e) {
               if(e.getClickCount() == 1) {
                  click(e);
               }
               else if(e.getClickCount() == 2) {
                  d_click(e);
               }
            }

            /**
             * Tut nix
             */
            public void mousePressed(MouseEvent e) {
            }

            /**
             * Tut nix
             */
            public void mouseReleased(MouseEvent e) {
            }

            /**
             * Tut nix
             */
            public void mouseEntered(MouseEvent e) {
               /**
                * Tut nix
                */
            }

            public void mouseExited(MouseEvent e) {
               /**
                * Tut nix
                */
            }
         }); // End Of MouseListener
      }

      // From DefaultTableCellRenderer
      /*
       * The following methods are overridden as a performance measure to to prune
       * code-paths are often called in the case of renders but which we know are
       * unnecessary. Great care should be taken when writing your own renderer to
       * weigh the benefits and drawbacks of overriding methods like these.
       */

      /**
       * Overridden for performance reasons.
       */
      public void validate() {
      }

      /**
       * Overridden for performance reasons.
       */
      public void revalidate() {
      }

      /**
       * Overridden for performance reasons.
       */
      public void repaint(long tm, int x, int y, int width, int height) {
      }

      /**
       * Overridden for performance reasons.
       */
      public void repaint(Rectangle r) {
      }

      /**
       * Overridden for performance reasons.
       */
      protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
         // Strings get interned...
         /*
          * if (propertyName=="text") { this.firePropertyChange(propertyName,
          * oldValue, newValue); }
          */
      }

      /**
       * Overridden for performance reasons.
       */
      public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
      }
   } // End Of GuiTableFormatRenderer

   /**
    * Mit dieser Tabellenspalte kann eine GuiKomponente verknüpft werden, die als
    * externer Editor für die selektierte Spalte fungiert.
    * 
    * @see GuiTable
    * @see GuiTable.GuiTableColumnModel
    * @see TableColumnAble
    */
   @SuppressWarnings("serial")
   public final class GuiTableColumn extends TableColumn {
      // Attributes
      private TableColumnAble myMember;
      /**
       * Kennzeichen, ob die ganze Spalte editierbar ist.
       * 
       * @see GuiTable.GuiTableModel#isCellEditable
       */
      private boolean editable = true;
      /**
       * Diese default-Werte dienen der Restauration der Werte wenn eine Spalte wieder sichtbar gemacht wird
       */
      private int defaultWidth = 120;
      private int defaultPreferredWidth = 120;
      private int defaultMinWidth;
      private int defaultMaxWidth = Integer.MAX_VALUE;
      private boolean defaultEditable;

      private LinesBorder lb;

      /**
       * Link-Komponente
       */
      private GuiComponent linkComp = null;

      // Constructor
      GuiTableColumn(GuiTable myTable, TableColumnAble comp, String title, int width) {
         super();
         myMember = comp;
         comp.setGuiParent(myTable.getGuiParent());
         this.setModelIndex(colCount);
         if(title.length() > 0) {
            this.setHeaderValue(title);
         }
         else {
            this.setHeaderValue(" ");
         }
         this.setIdentifier(comp.getName());
         if(myTable.isEnabled() == false) {
            comp.setEnabled(false);
         }
         // ToolTipText weiterreichen.
         String ttt = comp.getToolTipText();
         if(ttt != null) {
            // Wenn kein HeaderRenderer gesetzt, dann den DefaultRenderer
            if(this.getHeaderRenderer() == null) {
               DefaultTableCellHeaderRenderer renderer = new DefaultTableCellHeaderRenderer();
               this.setHeaderRenderer(renderer);
            }
            JComponent hRenderer = (JComponent)this.getHeaderRenderer();
            hRenderer.setToolTipText(ttt);
            comp.setToolTipText(null);
         }
         this.setMinWidth(comp.getMinimumSize().width);
         this.setMaxWidth(comp.getMaximumSize().width);
         if(width > 10) {
            if(width < this.getMaxWidth()) {
               this.setMaxWidth(width);
            }
            if(this.getMinWidth() > width) {
               this.setMinWidth(width);
            }
            this.setPreferredWidth(width);
         }
         // Editierbar?
         this.setColumnEditable(comp.isEnabled());
         // Unsichtbare Spalten
         if(comp.isVisible() == false) {
            this.setMinWidth(0);
            this.setMaxWidth(0);
            this.setColumnEditable(false);
         }
         // Text, Date, Number, ...
         if(comp instanceof GuiText) {
            if(comp.isEnabled()) {
               GuiText guiText = (GuiText)comp;
               // An dieser Stelle muss ein FormatedEditor benutzt werden
               // this.setCellEditor(new
               // DefaultCellEditor((JTextField)comp.getJComponent()));
               this.setCellEditor(new GuiTextCellEditor(guiText));
            }

            GuiTableFormatRenderer render = new GuiTableFormatRenderer((TableTextColumn)comp);
            // DblKlick auf Tabelle?
            if(myTable.getMsgDblClick() != null) {
               render.createMouseListener();
            }
            // Oh weh!
            // Wie machen wir das mit dem Change-Ereignis?
            this.setCellRenderer(render);
         }
         else if(comp instanceof GuiCombo) {
            TableComboColumn combo = (TableComboColumn)comp;
            // Oh weh! Hier wird eine neue ComboBox in die Welt gesetzt,
            // und wenn erst später die Map gesetzt wird,
            // kommt das hier nicht mehr an!
            // Nachtrag:
            // Dafür gibts jetzt die Methode GuiTable#setItems(String colName,
            // JDataSet ds)
            // Die versorgt auch den Renderer.
            GuiCombo rendCombo = new GuiCombo(combo.getItems());
            rendCombo.setMap(combo.getMap());
            rendCombo.setDisplayMember(combo.getDisplayMember());
            rendCombo.setValueMember(combo.getValueMember());
            rendCombo.setNotnull(((GuiCombo)comp).getNotnull());
            GuiTableComboRenderer render = new GuiTableComboRenderer(rendCombo, !comp.isEnabled());
            this.setCellRenderer(render);
            rendCombo.setEditor((GuiCombo)comp);

            DefaultCellEditor editor = new GuiTableComboEditor(combo);
            this.setCellEditor(editor);
         }
         else if(comp instanceof GuiCheck) {
            ((GuiCheck)comp).setText(null);
            ((GuiCheck)comp).setHorizontalAlignment(SwingConstants.CENTER);
            this.setCellEditor(new DefaultCellEditor((JCheckBox)comp.getJComponent()));
            GuiTableCheckRenderer render = new GuiTableCheckRenderer(!comp.isEnabled());
            this.setCellRenderer(render);
            // this.sizeWidthToFit(); // übertreibt!
            /*
             * if (this.getMaxWidth() < 90) { this.setMaxWidth(90); }
             */
            /*
             * if (this.getMinWidth() < 30) { this.setMinWidth(30); }
             */
            // Minimale Spaltenbreite aus Textlänge errechnen
            if(this.getMinWidth() < title.length() * 5 && title.length() > 10 && title.startsWith("<") == false) {
               this.setMinWidth(title.length() * 5);
            }
            /*
             * int w = this.columnHeaderWidth((JTable)myTable.getJComponent()); if
             * (w != -1) { this.setMinWidth(w); }
             */
         }
         else if(comp instanceof GuiLabel) {
            // Grafik???
            DefaultTableCellRenderer render = new DefaultTableCellRenderer();
            render.setHorizontalAlignment(((GuiLabel)comp).getHorizontalAlignment());
            render.setBackground(comp.getBackground());
            render.setForeground(comp.getForeground());
            render.setIcon(((GuiLabel)comp).getIcon()); // für alle Zeilen
            // gleich??
            this.setCellRenderer(render);
            this.setColumnEditable(false);
            // Minimale Spaltenbreite aus Textlänge errechnen
            if(this.getMinWidth() < title.length() * 5) {
               this.setMinWidth(title.length() * 5);
            }
         }
         else if(comp instanceof HiddenField) {
            this.setColumnEditable(false);
         }
         else {
            throw new IllegalArgumentException("Illegal Class: " + comp.getClass().getName());
         }
         // min sollte nicht kleiner als max sein
         if(this.getMinWidth() > this.getMaxWidth()) {
            this.setMaxWidth(this.getMinWidth());
         }
         // preferred sollte nicht kleiner als max sein
         if(this.getPreferredWidth() > this.getMaxWidth()) {
            this.setMaxWidth(this.getPreferredWidth());
         }
      }

      // Methods
      /**
       * @see TableColumnAble#getName
       */
      public String getName() {
         return myMember.getName();
      }

      /**
       * @see TableColumnAble#getDataType
       */
      public int getDataType() {
         return myMember.getDataType();
      }

      /**
       * @see TableColumnAble#getTag
       * @return
       */
      public String getTag() {
         return myMember.getTag();
      }

      /**
       * Setzt die Eigenschaft "editable"
       */
      void setColumnEditable(boolean b) {
         editable = b;
      }

      boolean getDefaultColumnEditable() {
         return defaultEditable;
      }

      void setDefaultColumnEditable(boolean b) {
         this.defaultEditable = b;
      }

      /**
       * Liefert die Eigenschaft "editable"
       * <p>
       * überlicherweise sind Spalten vom Typ "Label" oder "Hidden" nicht editierbar.
       */
      public boolean isColumnEditable() {
         return editable;
      }

      public void setVisible(boolean b) {
         if(b && !isVisible()) {
            this.setMinWidth(defaultMinWidth);
            this.setMaxWidth(defaultMaxWidth);
            this.setWidth(defaultWidth);
            this.setPreferredWidth(defaultPreferredWidth);
            this.setColumnEditable(defaultEditable);
            this.adjustVisibleWidth();
         }
         else if(!b && isVisible()) {
            this.defaultMinWidth = this.getMinWidth();
            this.defaultMaxWidth = this.getMaxWidth();
            this.defaultWidth = this.getWidth();
            this.defaultPreferredWidth = this.getPreferredWidth();
            this.defaultEditable = this.isColumnEditable();
            this.setColumnEditable(false);
            this.setMinWidth(0);
            this.setMaxWidth(0);
            this.setWidth(0);
            this.setPreferredWidth(0);
         }
      }

      public boolean isVisible() {
         if(this.getWidth() == 0 && this.getMinWidth() == 0 && this.getMaxWidth() == 0) {
            return false;
         }
         else {
            return true;
         }
      }

      int getDefaultMinWidth() {
         return defaultMinWidth;
      }

      int getDefaultMaxWidth() {
         return defaultMaxWidth;
      }

      int getDefaultWidth() {
         return defaultWidth;
      }

      int getDefaultPreferredWidth() {
         return defaultPreferredWidth;
      }

      void setDefaultMinWidth(int w) {
         this.defaultMinWidth = w;
      }

      void setDefaultMaxWidth(int w) {
         this.defaultMaxWidth = w;
      }

      void setDefaultWidth(int w) {
         this.defaultWidth = w;
      }

      void setDefaultPreferredWidth(int w) {
         this.defaultPreferredWidth = w;
      }

      void adjustVisibleWidth() {
         if(this.getMaxWidth() == 0 && this.getWidth() == 0 && this.getPreferredWidth() == 0) {
            this.setPreferredWidth(120);
            this.setMinWidth(40);
            this.setWidth(40);
            this.setMaxWidth(Integer.MAX_VALUE);
         }
      }

      /**
       * Setzt die Komponente, mit der dieser Spalte verknüpft wird. Attribut
       * linkCol=
       */
      void setLinkComponent(GuiComponent comp) {
         linkComp = comp;
      }

      /**
       * Liefert die mit dieser Spalte verknüpfte Komponente.
       */
      GuiComponent getLinkComponent() {
         return linkComp;
      }

      Class<?> getColumnClass() {
         return myMember.getValueClass();
      }

      // Bindings
      // Element-Name
      String getElementName() {
         return myMember.getElementName();
      }

      public GuiComponent getGuiComponent() {
         return myMember.getGuiComponent();
      }

      public void setWidth(int width) {
         super.setWidth(width);
      }

      public void setLinesBorder(Color c, Insets i) {
         this.lb = new LinesBorder(c, i);
      }

      LinesBorder getLinesBorder() {
         return lb;
      }
   } // End Of GuiTableColumn

   /**
    * Column Model für GuiTable
    */
   @SuppressWarnings("serial")
   private static final class GuiTableColumnModel extends DefaultTableColumnModel {
      // Attributes
      // Constructor
      GuiTableColumnModel() {
         super();
      }

      // Methods
      /**
       * Returns the <B>GuiTableColumn </B> object for the column at
       * <I>columnIndex </I>
       * 
       * @return the GuiTableColumn object for the column at <I>columnIndex </I>
       * @param columnIndex
       *           the index of the column desired
       */
      GuiTableColumn getGuiColumn(int columnIndex) {
         if(columnIndex < 0)
            return null;
         return (GuiTableColumn)tableColumns.elementAt(columnIndex);
      }

      /**
       * Liefert eine Spalte unter Angabe ihres Namens.
       * 
       * @param colName
       *           Name der Spalte; siehe Attribut name=
       * @return Eine TabellenSpalte oder null, wenn unter diesem Namen keine
       *         Spalte existiert.
       */
      GuiTableColumn getGuiColumn(String colName) {
         for(Enumeration<TableColumn> e = this.getColumns(); e.hasMoreElements();) {
            GuiTableColumn col = (GuiTableColumn)e.nextElement();
            if(col.getName().equals(colName)) {
               return col;
            }
         }
         return null;
      }

      /**
       * Liefert den Index der Spalte unter Angabe ihres Namens.
       * 
       * @param colName
       *           Name der Spalte; siehe Attribut name=
       * @return Den Index der Spalte im ColumnModel oder -1, wenn der Name der
       *         Spalte ungültig ist.
       */
      int getGuiColumnIndex(String colName) {
         GuiTableColumn col = getGuiColumn(colName);
         if(col != null) {
            return col.getModelIndex();
         }
         String msg = "Missing Column Name: " + colName;
         logger.warn(msg);
         System.err.println(msg);
         return -1; // TODO Besser Exception werfen?
      }

      boolean hasGuiColumn(String colName) {
         return getGuiColumn(colName) != null;
      }

      void addGuiColumn(GuiTableColumn col) {
         if(hasGuiColumn(col.getName())) {
            String msg = "Duplicate Column Name: " + col.getName();
            logger.warn(msg);
            System.err.println(msg);
         }
         this.addColumn(col);
      }

   } // End Of GuiTableColumnModel

   /**
    * TableModel für GuiTable.
    */
   @SuppressWarnings("serial")
   public final class GuiTableModel extends AbstractTableModel /*DefaultTableModel*/ {

      // Attributes
      protected Vector<GuiTableRow> dataVector;

      private Vector<String> columnIdentifiers;

      private GuiTable myTable;

      /**
       * Menge der Zeilen, die aus der Tabelle gelöscht wurde; wird bei reset
       * zurückgesetzt.
       */
      private Vector<GuiTableRow> deletedRows;

      // Constructors
      /**
       * Erzeugt ein TableModel.
       */
      GuiTableModel(GuiTable tbl) {
         super();
         myTable = tbl;
      }

      // Methodes
      /**
       * Liefert die Anzahl der aktiven Zeilen.
       */
      public int getRowCount() {
         return (dataVector == null) ? 0 : dataVector.size();
      }

      /**
       * Liefert die Anzahl der Spalten.
       */
      public int getColumnCount() {
         return (columnIdentifiers == null) ? 0 : columnIdentifiers.size();
      }

      /**
       * Liefert die Anzahl der gelöschten Zeilen.
       * 
       * @return
       */
      public int getDeletedRowCount() {
         return (deletedRows == null) ? 0 : deletedRows.size();
      }

      /**
       * Setzt den Inhalt der Tabelle neu.
       * 
       * @param data
       *           Ein Vector von GuiTableRows
       * @param cols
       *           Ein Vector von ColumnIdentifiers
       */
      public void setDataVector2(Vector<GuiTableRow> data, Vector<String> cols) {
         dataVector = data;
         columnIdentifiers = cols;
         deletedRows = null;
         this.fireTableDataChanged(); // neu 11.5.2002
      }

      public Vector<GuiTableRow> getDataVector() {
         return dataVector;
      }

      /**
       * Liefert einen Iterator über alle Zeilen der Tabelle in der Reihenfolge,
       * wie sie für den DataSet benötigt werden. Dabei werden auch gelöschte
       * Zeilen mitgeliefert.
       * <p>
       * Die Zeilen, die erst eingefügt und später gelöscht wurden werden weggelassen.
       * 
       * @return
       */
      public Iterator<Map.Entry<Integer, GuiTableRow>> getRowIterator() {
         TreeMap<Integer, GuiTableRow> tmap = new TreeMap<Integer, GuiTableRow>();
         int insertCount = this.getMaxModelElementNumber();
         for(Iterator<GuiTableRow> it = dataVector.iterator(); it.hasNext();) {
            GuiTableRow tableRow = it.next();
            Integer key = null;
            if(tableRow.isInserted()) {
               if(tableRow.getModelElementNumber() == -1) {
                  insertCount++;
                  tableRow.setModelElementNumber(insertCount);
               }
            }
            key = new Integer(tableRow.getModelElementNumber());
            tmap.put(key, tableRow);
         }
         if(this.deletedRows != null) {
            for(GuiTableRow tableRow : deletedRows) {
               Integer key = null;
               if(tableRow.isInserted()) {
                  // nix; Eingefügte und später wieder gelöschte Zeilen
                  // weglassen.
               }
               else {
                  key = new Integer(tableRow.getModelElementNumber());
                  tmap.put(key, tableRow);
               }
            }
         }
         return tmap.entrySet().iterator();
      }

      private int getMaxModelElementNumber() {
         int max = 0;
         for(Iterator<GuiTableRow> it = dataVector.iterator(); it.hasNext();) {
            GuiTableRow tableRow = it.next();
            int i = tableRow.getModelElementNumber();
            if(i > max)
               max = i;
         }
         if(this.deletedRows != null) {
            for(GuiTableRow tableRow : deletedRows) {
               int i = tableRow.getModelElementNumber();
               if(i > max)
                  max = i;
            }
         }
         return max;
      }

      /**
       * Die gelöschte Zeile wird bei den deletedRows eingetragen. Neu eingefügte
       * Zeilen werden nicht berücksichtigt.
       * 
       * @see #getDeletedRows
       * @see GuiTableRow#isInserted
       */
      public void deleteRow(int row) {
         GuiTableRow trow = getRow(row);
         if(trow.isInserted() == false) {
            if(deletedRows == null) { // Lazy instanciation
               deletedRows = new Vector<GuiTableRow>();
            }
            trow.setDeleted(true);
            deletedRows.addElement(trow);
         }
         dataVector.removeElementAt(row);
         fireTableRowsDeleted(row, row);
      }

      /**
       * Liefert die Zeilen, die vom Benutzer seit dem letzten reset gelöscht
       * wurden. Das Ergebnis kann auch "null" sein.
       */
      public Vector<GuiTableRow> getDeletedRows() {
         return deletedRows;
      }

      /*
       * public Class getColumnClass(int c) { return
       * myTable.getColumnModel().getGuiColumn(c).getColumnClass(); }
       */
      public Object getValueAt(int row, int col) {
         GuiTableRow trow = getRow(row);
         return trow.getValueAt(col);
      }

      public void setValueAt(Object value, int row, int col) {
         GuiTableRow trow = getRow(row);
         trow.setValueAt(col, value);
         trow.setModified(true);
      }

      public void addRow(GuiTableRow trow) {
         dataVector.addElement(trow);
         fireTableDataChanged();
      }

      public GuiTableRow getRow(int index) {
         return (GuiTableRow)dataVector.elementAt(index);
      }

      public void insertRow(int index, GuiTableRow tableRow) {
         if(this.myTable != tableRow.getParentTable()) {
            tableRow.setParentTable(this.myTable); // Notnagel
         }
         dataVector.insertElementAt(tableRow, index);
         fireTableRowsInserted(index, index);
      }

      void setDataVector(Vector<Vector<Object>> data) {
         Vector clone = new Vector(data.size());
         for(int i = 0; i < data.size(); i++) {
            Object o = data.elementAt(i);
            if(o instanceof Vector) {
               GuiTableRow row = new GuiTableRow(this.myTable, (Vector)o);
               clone.addElement(row);
            }
            else {
               clone.addElement(o);
            }
         }
         dataVector = clone;
         deletedRows = null;
         this.fireTableDataChanged(); // neu 11.5.2002
      }

      void setDeletedRows(Vector<GuiTableRow> rows) {
         this.deletedRows = rows;
      }

      /**
       * Leert das TableModel
       */
      public void reset() {
         dataVector = new Vector<GuiTableRow>();
         deletedRows = null;
      }

      /**
       * Setzt deletedRows auf null.
       * <p>
       * Setzt bei den GuiTableRows die Eigenschaften modified und inserted auf false.
       */
      public void commitChanges() {
         deletedRows = null;
         if(dataVector != null) {
            for(int i = 0; i < dataVector.size(); i++) {
               Object o = dataVector.elementAt(i);
               if(o instanceof GuiTableRow) {
                  GuiTableRow row = (GuiTableRow)o;
                  row.commitChanges();
               }
               else {
                  // das sollte man protokollieren, wenn das keine TableRow
                  logger.error("Row not instanceof GuiTableRow: " + o.getClass().getName());
               }
            }
         }
      }

      /**
       * Diese Methode wird hier überschrieben, damit einzelne Spalten/Zeilen
       * editierbar gemacht werden können.
       */
      public boolean isCellEditable(int row, int col) {
         GuiTableColumn column = GuiTable.this.colModel.getGuiColumn(col);
         GuiTableRow trow = getRow(row);
         if(column.isColumnEditable() == true && trow.isEditable() == true) {
            return true;
         }
         else {
            return false;
         }
      }
   } // End Of GuiTableModel

   // private final class GuiTableModelListener implements TableModelListener {
   // private GuiTableModel model;
   //
   // public GuiTableModelListener(GuiTableModel model) {
   // this.model = model;
   // }
   //
   // // By default forward all events to all the listeners.
   // public void tableChanged(TableModelEvent e) {
   // //model.tableChanged(e);
   // }
   // } // End Of GuiTableModel

} // End Of GuiTable
