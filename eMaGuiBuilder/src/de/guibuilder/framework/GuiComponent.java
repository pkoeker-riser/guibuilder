package de.guibuilder.framework;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.sql.Types;
import java.util.Collection;

import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import de.guibuilder.framework.GuiFactory.CurContext;
import de.jdataset.DataView;
import de.jdataset.JDataColumn;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.jdataset.NVPair;
import de.pkjs.util.Convert;

/**
 * Abstrakte Basisklasse für alle Oberflächenelemente die Daten halten können im
 * Unterschied zu denen, die Aktionen auslösen.
 * 
 * @since 0.9.1
 * @see GuiAction
 */
public abstract class GuiComponent extends GuiElement implements IDatasetComponent {
   private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GuiComponent.class);

	public static final int BOOLEAN = 0;
	public static final int STRING = 1;
	public static final int NUMBER = 2;
	public static final int DATE = 3;
	public static final int TIME = 4;
	public static final int ENUM = 5;
	public static final int MULTILINE = 6;
	public static final int INTEGER = 7;
	public static final int TABLE = 8;
	public static final int TREE = 9;

	public static final String[] DATA_TYPE_NAMES = { "Boolean", "String",
			"Number", "Date", "Time", "Enum", "Multiline", "Integer", "Table",
			"Tree" };

	// Attributes
	/**
	 * Kennzeichen, ob der Inhalt vom Benutzer verändert wurde.
	 */
	private boolean modified;

	/**
	 * Kennzeichen Pflichtfeld
	 */
	protected boolean notnull;

	/**
	 * Kennzeichen Suchbegriff für Datenbank-Abfrage.
	 * 
	 * @see GuiContainer#getSearchables
	 */
	private boolean search;

	/**
	 * Minimale Eingabelänge
	 */
	private int minlen = 0;

	/**
	 * Default für die Maximallänge von Werten (String) = 50.
	 */
	private int maxlen = 50;

	/**
	 * Mit dieser Komponente verknüpfte Tabelle.
	 */
	protected transient GuiTable linkTable;

	/**
	 * Mit dieser Komponente verknüpfte Spalte der verknüpften Tabelle.
	 */
	protected int linkColumn = -1;
	/**
	 * Leider werden beim hin- und her-klicken in Tabellen die Ereignisse nicht
	 * in der gewünschten Reihefolge ausgelöst: Wenn man Komponent geändert hat,
	 * und dann in der Tabelle eine andere Zeile angeklickt wird, wird *erst*
	 * die andere Zeile selektiert und *danach* verliert die Komponente ihren
	 * Focus. Das führt dazu, daß diese Komponente die selektierte Zeile der
	 * Tabelle nicht ermitteln kann; das muß also die Tabelle selbst erledigen.
	 */
	protected int linkRow = -1;
	/**
	 * Verknüpfung des Inhalts mit dem selektierten treeNode
	 */
	private int nodeTitle = -1;
	/**
	 * Der Inhalt des Feldes soll wiederhergestellt werden (Preferences)
	 */
	private boolean restore;

  // Constructors
	GuiComponent() {
		super();
	}

	GuiComponent(String label) {
		super(label);
	}

	// Methods
	/**
	 * Liefert GUI_COMPONENT
	 */
	public int getGuiType() {
		return GUI_COMPONENT;
	}

	/**
	 * Liefert den Datentyp der Componente.
	 * <p>
	 * Denkbare Werte sind:
	 * <ul>
	 * <li>BOOLEAN (Check)
	 * <li>DATE = String
	 * <li>ENUM (not implemented)
	 * <li>INTEGER (Slider, Scrollbar, Spinn)
	 * <li>MULTILINE = String (Memo, Editor)
	 * <li>NUMBER = String
	 * <li>STRING (Text, Combo, List, Label, Password, Hidden)
	 * <li>TIME = String
	 * <li>TABLE = Vector von Vectoren
	 * <li>TREE (not implemented; see GuiTree.getAllValuesXml)
	 * </ul>
	 */
	public abstract int getDataType();

	/**
	 * Liefert die Bezeichung zu dem Datentyp.
	 * 
	 * @param dataType
	 * @return
	 */
	public static String getDataTypeName(int dataType) {
		return DATA_TYPE_NAMES[dataType];
	}

	/**
	 * Setzt den Inhalt dieser Componente.
	 * <p>
	 * Diese Methode wird üblicherweise von den erbenden Komponenten
	 * überschrieben.
	 * <p>
	 * Die Eigenschaft "modified" der Komponente wird dabei auf "false" gesetzt.
	 * 
	 * @see #setModified
	 * @see #isModified
	 */
	public void setValue(Object o) {
		setModified(false);
	}

	/**
	 * Liefert den unformatierten Inhalt dieser Componente.
	 * 
	 */
	public abstract Object getUnformatedValue();

	/**
	 * Liefert den Inhalt dieser Componente.
	 * <p>
	 * Wird hier null zurückgegeben, ist die Componente zu ignorieren; es
	 * handelt sich dann um nicht selektierte RadioButtons.
	 */
	public abstract Object getValue();

	/**
	 * Setzt den Inhalt der Komponente auf den Inhalt des Models der über
	 * getElementPath erreichbar ist. Wird von GuiTable und GuiTree
	 * überschrieben.
	 */
	public void setDatasetValues(JDataSet ds) {
		// Components müssen ein Element haben!
		// Container nicht!
		if (this.getElementName() != null) {
			String xpath = this.getElementPath("");
			if (xpath != null) {
				try {
					String val = ds.getValuePath(xpath);
					this.setValue(val);
					// val= (String) getValue(); // bubi: (Bug 985755) Date
					// values are changed according the date format
					// ds.setValuePath(xpath,val);
					JDataColumn col = ds.getDataColumnPath(xpath);
					if (col == null) {
						System.err.println("GuiComponent#setDatasetValues Missing DataColumn: "
										+ this.getName() + " xpath: " + xpath);
					} else {
						if (col.isReadonly()) {
							this.setEnabled(false);
						}
						if (col.isNullable() == false
								&& this.isEnabled() == true) {
							this.setNotnull(true);
						}
					}
				} catch (Exception ex) {
					System.err.println("GuiComponent#setDatasetValues: '"
							+ this.getName() + "' " + ex.getMessage());
				}
			}
		}
	}

	public void getDatasetValues(JDataSet ds, String current) {
		// Components müssen ein Element haben!
		// Container nicht!
		if (this.getElementName() != null) {
			String xpath = this.getElementPath(current); // From GuiMember
			Object oval = this.getValue();
			String val = null;
			if (oval != null) {
				val = oval.toString();
			}
			try {
				ds.setValuePath(xpath, val);
			} catch (Exception ex) { // Unter xpath nix zu finden
			   System.err.println(ex.getMessage());
			}
		}
	}

	/**
	 * Der von der Komponente gehaltene Wert wird in den Dataset eingetragen.
	 * 
	 * @param ds
	 */
	public void getDatasetValues(JDataSet ds) {
		this.getDatasetValues(ds, "");
	}
	
	/**
	 * Teilt der Komponente mit, daß die Benutzereingaben erfolgreich
	 * verarbeitet wurden. Zumeist wird die Eigenschaft "modified" nur auf
	 * "false" gesetzt.
	 */
	public void commitChanges() {
		setModified(false);
	}

	/**
	 * Liefert das Kennzeichen, ob der Inhalt der Komponente vom Benutzer
	 * verändert wurde.
	 * <p>
	 * Wird der Inhalt per Programm mit setValue() gefüllt, wird modified auf
	 * false gesetzt.
	 */
	public final boolean isModified() {
		return modified;
	}

	/**
	 * Setzt das Änderungskennzeichen neu. Wenn true, wird dieses auch an
	 * RootPane weiter verpetzt.
	 * 
	 * @see #isModified
	 * @see #setValue
	 * @see GuiRootPane#setModified
	 */
	public final void setModified(boolean b) {
		modified = b;
		if (modified == true && getRootPane() != null) {
			getRootPane().setModified(modified);
		}
	}

	/**
	 * Liefert das Kennzeichen, ob der Inhalt dieser Komponente ein Suchbegriff
	 * z.B. für eine Datenbank-Operation sein soll. Siehe das Attribut search=
	 * 
	 * @see GuiContainer#getSearchables
	 */
	final boolean isSearch() {
		return search;
	}

	/**
	 * Setzt das Kennzeichen, ob der Inhalt dieser Komponente ein Suchbegriff
	 * z.B. für eine Datenbank-Operation sein soll. Siehe das Attribut search=
	 * 
	 * @see GuiContainer#getSearchables
	 */
	final void setSearch(boolean b) {
		search = b;
	}

	/**
	 * Liefert das Kennzeichen "Pflichtfeld".
	 * 
	 * @see GuiUtil#setCheckNN(boolean)
	 */
	public final boolean isNotnull() {
		if (notnull == true && GuiUtil.isCheckNN() == true) { // TODO: verify funktioniert nur, wenn isCheckNN gesetzt!
			return true;
		} else {
			return false;
		}
	}
	
	public final boolean getNotnull() {
	   return notnull;
	}

	/**
	 * Setzt das Kennzeichen "Pflichtfeld". In GuiBuilder.properties kann unter
	 * "notNullBackgroundColor" eine Farbe für den Hintergrund der Komponente
	 * abgelegt werden; z.B. 243,243,192
	 * <p>
	 * Es wird ein InputVerifier für NotNull gesetzt wenn true.
	 * 
	 * @see GuiInputVerifier
	 */
	public final void setNotnull(boolean b) {
		this.notnull = b;
		//final String nnchar = GuiUtil.getNNChar();
		final Color bg = GuiUtil.getNNColor();
		if (b) {
			if (bg != null && this instanceof GuiCheck == false) {
				this.setBackground(bg);
			}
			/*
			 * if (nnchar != null && this instanceof GuiOption == false && this
			 * instanceof GuiCheck == false) { String label = nnchar +
			 * this.getLabel(); this.setLabel(label); }
			 */
			// InputVerifier
			this.setInputVerifier();
		} else { // Not Null zurücksetzen
			// BackgroundColor rücksetzen wenn Property gesetzt
			if (bg != null && this instanceof GuiCheck == false) {
				this.setBackground(Color.WHITE);
			}
			/*
			 * if (nnchar != null && this instanceof GuiOption == false && this
			 * instanceof GuiCheck == false) { String curlbl = this.getLabel();
			 * if (curlbl.endsWith(nnchar)) { String newlbl =
			 * curlbl.substring(0, curlbl.length() - 1); this.setLabel(newlbl);
			 * } }
			 */
			// Den InputVerifier lassen wir erstmal drin,
			// da es viel Gründe gibt die Eingabe zu prüfen.
			// Wir dürfen den nur löschen, wenn es keine zu prüfende
			// Eigenschaft mehr gesetzt ist (maxlen, Date, ...).
		}
	}

	/**
	 * Liefert die maximale Eingabelänge z.B. von Textfeldern.
	 */
	public final int getMaxlen() {
		return this.maxlen;
	}

	/**
	 * Setzt die maximale Eingabelänge z.B. für Textfelder.
	 * <p>
	 * Es wird ein InputVerifier gesetzt.
	 * 
	 * @see GuiInputVerifier
	 */
	public final void setMaxlen(int len) {
		this.maxlen = len;
		// InputVerifier
		this.setInputVerifier();
	}

	/**
	 * Liefert die minimale Eingabelänge z.B. von Textfeldern.
	 */
	public final int getMinlen() {
		return this.minlen;
	}

	/**
	 * Setzt die minimale Eingabelänge z.B. für Textfelder.
	 * <p>
	 * Es wird ein InputVerifier gesetzt.
	 * 
	 * @see GuiInputVerifier
	 */
	public final void setMinlen(int len) {
		this.minlen = len;
		// InputVerifier
		this.setInputVerifier();
	}

	/**
	 * Setzt einen InputVerifier für alle Eingabe-Prüfungen
	 * 
	 * @see GuiInputVerifier
	 */
	protected void setInputVerifier() {
		JComponent jcomp = getJComponent();
		if (jcomp == null) return; // Wegen Hidden
		// Spezialbehandlung für Combo
		if (jcomp instanceof JComboBox) {
			JComboBox<?> box = (JComboBox<?>) jcomp;
			jcomp = (JTextField) box.getEditor().getEditorComponent();
		}
		InputVerifier iv = jcomp.getInputVerifier();
		// Wir setzen den InputVerifier nur, wenn bisher keiner da
		if (iv == null) {
			GuiInputVerifier very = new GuiInputVerifier(this);
			jcomp.setInputVerifier(very);
		}
	}

	/**
	 * Ruft den InputVerifier der Componente auf.
	 * 
	 * @throws IllegalStateException
	 *             , wenn Eingaben unzulässig.
	 * @see GuiContainer#verify
	 */
   public final void verify(boolean checkNN) throws IllegalStateException {
      JComponent jcomp = getJComponent();
      if(jcomp == null)
         return; // Wegen Hidden
      if(this.isEnabled() == false)
         return;
      // Spezialbehandlung für Combo
      if(jcomp instanceof JComboBox) {
         JComboBox<?> box = (JComboBox<?>)jcomp;
         jcomp = (JTextField)box.getEditor().getEditorComponent();
      }
      InputVerifier iv = jcomp.getInputVerifier();
      if(iv == null)
         return;
      if (iv instanceof GuiInputVerifier) {
         GuiInputVerifier giv = (GuiInputVerifier)iv;
         if (giv.verify(jcomp, checkNN) == false) {
            throw new IllegalStateException(((GuiInputVerifier)jcomp.getInputVerifier()).getErrorMessage());            
         }
      } else if(iv.verify(jcomp) == false) {
         throw new IllegalStateException(((GuiInputVerifier)jcomp.getInputVerifier()).getErrorMessage());
      }
   }

	/**
	 * Setzt die Verknüpfung dieser Komponente zu eine Tabellenspalte. Bei
	 * LostFocus wird diese Spalte der selektierten Zeile der Tabelle auf den
	 * Inhalt dieser Komponente gesetzt. Wenn die Tablle null ist, passiert
	 * nüscht.
	 * @param colName: {[tableName].}[columnName|columnIndex] 
	 * @see GuiTable#setLinkComponent
	 */
	final void setLinkColumn(CurContext c, String colName) {
		if (colName == null || colName.length() == 0) return;
		GuiTable tbl = c.cTbl;
		int p = colName.indexOf('.');
		if (p != -1) {
			String tblName = colName.substring(0,p);
			colName = colName.substring(p+1);
			tbl = c.tables.get(tblName);
		}
		if (tbl != null) {
			linkTable = tbl;
			int index = Convert.toInt(colName);
			if (index > 0 || "0".equals(colName)) {
				linkColumn = index;
			} else {
				int colIndex = tbl.getColumnIndex(colName);
				linkColumn = colIndex;				
			}
			if (linkColumn >= tbl.getColCount()) {
				throw new IllegalArgumentException(
						"GuiComponent#setLinkColumn [" + this.getName()
								+ "] Table [" + tbl.getName()
								+ "] Column Index [" + linkColumn
								+ "] out of Range [" + tbl.getColCount() + "]");
			} else {
				tbl.setLinkComponent(this, linkColumn);
			}
		}
	}

	final void setNodeTitle(int i) {
		nodeTitle = i;
	}

	final int getNodeTitle() {
		return nodeTitle;
	}

	/**
	 * überschrieben wegen msgLostFocus
	 * 
	 * @see GuiElement#setMsgLostFocus
	 * @see GuiRootPane#obj_LostFocus
	 */
	void lostFocus(FocusEvent e) {
		super.lostFocus(e); // GuiElement (Hint, stopCellEditing)
		if (e == null || e.isTemporary() == false) {
			updateLinkedColumn();
			if (getMsgLostFocus() != null && this.getRootPane() != null) {
				getRootPane().obj_LostFocus(this, getMsgLostFocus(), getValue(), e);
			}
			updateNodeTitle();
		}
	} // End Of lostFocus

	void updateLinkedColumn() {
      // Inhalt an verknüpfte Spalte weiterreichen?
      // 18.4.2013: Den Update auf die Tabelle weglassen, wenn sich die Sortierung der Zeilen geändert hat
      // (erkennbar an lastSelectedRow = -1)
      if(this.linkTable != null && linkTable.getLastSelectedRow() != -1 && this.isModified()) {
         int row = this.linkRow;
         if(row == -1) {
            row = linkTable.getSelectedRow(); // liefert die neue Zeile!!
         }
         if(GuiUtil.getDebug()) {
            System.out.println("updateLinkedColumn: " + +linkTable.getLastSelectedRow() + "/" + linkRow + "-" + row);
         }
         // Wenn Tabelle nicht leer...
         if(row != -1) {
            GuiTableRow tRow = linkTable.getRow(row);
            if (tRow != null) {
	            Object val = this.getValue();
	            tRow.setValueAt(linkColumn, val);
	            tRow.setModified(true);
            }
         }
      }
      // Link Table; Bitte nur, wenn Comp nicht selbst eine Spalte ist!
      GuiTable _linkTable = null; // Vermeidet Null-Pointer Exception.
      // 8.1.2004 PKÖ
      GuiContainer parent = this.getGuiParent();
      if(parent != null) {
         _linkTable = parent.getLinkTable();
      }
      if(this.getParentTable() == null && _linkTable != null) {
         _linkTable.setCellValue(getName(), getValue());
      }
	}

	/**
	 * Diese Methode wird aufgerufen, nachdem der Benutzer eine Änderung an dem
	 * Eingabefeld vorgenommen hat. Sie ist hier leer implementiert und kann
	 * (z.B. bei Plugins) überschrieben werden.
	 */
	public void postProc() {

	}

	private final void updateNodeTitle() {
		if (getNodeTitle() != -1) {
			GuiTree tree = getRootPane().getCurrentTree(); // geht schief, wenn
			// Panel im Tree
			if (tree != null) {
				GuiTreeNode node = tree.getLastNode();
				if (node != null) {
					node.setTitle(this.getValue().toString(), getNodeTitle());
					// node.setName(this.getValue().toString());
				}
			}
		}
	}

	/**
	 * Der Inhalt der Komponente hat sich (durch Benutzereingaben) geändert. Die
	 * Eigenschaft "modified" wird gesetzt.
	 * 
	 * @see GuiChangeListener
	 */
	void obj_ItemChanged(ActionEvent e) {
		this.postProc();
		this.setModified(true);
		// Inhalt an verknüpfte Spalte weiterreichen?
		if (linkTable != null) {
			linkTable.setCellValue(linkColumn, this.getValue());
		}
		// RootPane informieren
		GuiRootPane rootPane = this.getRootPane();
		if (rootPane != null && actionChange != null) {
			rootPane.obj_ItemChanged(this, actionChange, this.getValue());
		}
	}

	/**
	 * @see TableColumnAble
	 * @return GuiComponent
	 */
	public final GuiComponent getGuiComponent() {
		return this;
	}

	/**
	 * @param linkRow
	 *            The linkRow to set.
	 */
	void setLinkRow(int linkRow) {
		this.linkRow = linkRow;
	}
	
  public boolean isRestore() {
    return restore;
  }
  /**
   * Der Inhalt des Feldes soll wiederhergestellt werden (Preferences)
   * @param restore
   */
  public void setRestore(boolean restore) {
    this.restore = restore;
  }

	public String toString() {
		String s = this.getTag() + " [" + this.getLabel() + "] "
				+ this.getValue();
		return s;
	}

	/**
	 * Überschreibt die Methode der Oberklasse, um bei Pflichtfeldern eine
	 * Ergänzung des Labels vorzunehmen (sofern in der GuiBuilderConfig.xml 
	 * ein solches Zeichen definiert ist).
	 * 
	 *  @author thomas
	 */
	public String getLabel() {
		String label = super.getLabel();
		// hier kann nicht his.isNotnull() verwendet werden, weil es 
		// das Kennzeichen checkNN prüft
		if (this.notnull) {
			String nnchar = GuiUtil.getNNChar();
			// Prüfen, ob das Label des Pflichtfeldes ein Zeichen erweitert werden soll
			if (nnchar != null && this instanceof GuiOption == false
					&& this instanceof GuiCheck == false) {
				// Zeichen für die Erweiterung des Pflichtfeld-Labels anhängen
				label = label + nnchar;
			}
		}
		return label;
	}
	
  public void getPreferences(JDataSet ds) {
    if (ds == null || !this.isRestore()) return;
    // Meta Table
    JDataTable tblT = null;
    try {
      tblT = ds.getDataTable("GuiComponent");
    } catch (Exception ex) {
      tblT = new JDataTable("GuiComponent");
      ds.addRootTable(tblT);
      JDataColumn colName = tblT.addColumn("name", Types.VARCHAR);
      colName.setPrimaryKey(true);
      tblT.addColumn("value", Types.VARCHAR);
    }
    // Prefs Table
    JDataRow rowT;
    Collection<JDataRow> trows = null;
    try {
      trows = ds.getChildRows(new DataView("GuiComponent", new NVPair("name", this.getName(), Types.VARCHAR)));
    } catch (Exception ex) {}
    if (trows != null && trows.size() >= 1) {
      rowT = trows.iterator().next();
    } else {
      rowT = ds.createChildRow("GuiComponent");
    }
    rowT.setValue("name", this.getName());
    rowT.setValue("value", this.getValue());
  }
  
  public void setPreferences(JDataSet ds) {
    if (ds == null || !this.isRestore()) return;
    Collection<JDataRow> coll = ds.getChildRows(new DataView("GuiComponent", new NVPair("name", this.getName(), Types.VARCHAR)));
    if (coll.size() != 1) return;
    JDataRow row = coll.iterator().next();

    this.setValue(row.getValue("value"));
  }

}