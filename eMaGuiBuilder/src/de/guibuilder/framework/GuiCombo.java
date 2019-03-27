package de.guibuilder.framework;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import de.jdataset.JDataSet;

/**
 * Implementierung einer ComboBox.
 * <p>
 */
public final class GuiCombo extends GuiSelect implements TableComboColumn {
	// Attributes
	private JComboBox component;
	 /**
   * Regulärer Ausdruck für Eingabe-Prüfung
   */
  private Pattern regexp;
  private GuiCombo editor;
  
  void setEditor(GuiCombo edit) {
      this.editor = edit;
  }
  
  GuiCombo getEditor() {
      return editor;
  }

	// Constructors **********************************************
	/**
	 * Erzeugt eine leere ComboBox.
	 */
	public GuiCombo() {
		super();
		component = new JComboBox();
		guiInit();
	}

	/**
	 * Erzeugt eine ComboBox mit einem Vector von Einträgen; zumeist Strings.
	 */
	public GuiCombo(Vector<Object> v) {
		super();
		component = new JComboBox(v);
		guiInit();
	}

	/**
	 * Erzeugt eine ComboBox mit einer ArrayList von Einträgen.
	 */
	public GuiCombo(ArrayList<Object> al) { // new 3.12.2003 // PKÖ
		super();
		component = new JComboBox(al.toArray());
		this.guiInit();
	}

	/**
	 * Erzeugt eine ComboBox mit einem Array von Strings.
	 */
	public GuiCombo(String[] s) {
		super();
		component = new JComboBox(s);
		guiInit();
	}

	/**
	 * Erzeugt eine ComboBox gefüllt mit den Einträgen aus dem Iterator.
	 * 
	 * @param it
	 */
	public GuiCombo(Iterator<Object> it) { // new 3.12.2003 // PKÖ
		super();
		component = new JComboBox();
		while (it.hasNext()) {
			this.addItem(it.next());
		}
		this.guiInit();
	}

	// Methods
	/**
	 * Initialisierung; Name wird auf "combo" gesetzt; addFocusListener, addItemListener
	 */
	private void guiInit() {
		this.setName("combo");
		// neu: 28.1.2001 damit am Anfang nix angezeigt wird; wie Zustand "neu"
		this.setSelectedIndex(-1); 
		// LocusListener an den Editor hängen
		ComboBoxEditor edt = component.getEditor();
		Component awt = edt.getEditorComponent();
		awt.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        gotFocus(e);    
      }
      public void focusLost(FocusEvent e) {
        lostFocus(e);        
      }		  
		});
		// ItemListener
		component.addItemListener(new ItemListener() {
			// Inner Class ItemListener
			// TODO : Dieser Listener schlägt leider auch dann auch zu,
			// wenn mit setValue() --> setSelectedItem per Programm ein neuer Wert gesetzt wird.
			// Umpf, geht offenbar nicht anders!?
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					postProc();
					setModified(true);
					// Inhalt an verknüpfte Spalte weiterreichen?
					if (linkTable != null) {
						int row = linkTable.getSelectedRow();
						if (row != -1) {
							linkTable.setCellValue(row, linkColumn, getValue()); // TODO: Was tun mit displayMember?
						}
					}
					// NodeTitle?
					if (getNodeTitle() != -1 && getRootPane() != null) {
						GuiTree tree = getRootPane().getCurrentTree();
						if (tree != null) {
							GuiTreeNode node = tree.getSelectedNode();
							if (node != null) {
								node.setTitle(getValue().toString(), getNodeTitle());
							}
						}
					}
					// RootPane informieren
					if (actionChange != null && getRootPane() != null) {
						getRootPane().obj_ItemChanged(GuiCombo.this, actionChange, getValue(),
								getSelectedIndex());
					}
				}
			}
			// End Inner Class
			});
	}

	public final String getTag() {
		return "Combo";
	}

	public JComponent getJComponent() {
		return component;
	}

	/**
	 * Liefert die Swing-Komponente
	 */
	public JComboBox getCombo() {
		return component;
	}

	public final int getDataType() {
		return STRING;
	}
  public void gotFocus(FocusEvent e) {
    super.gotFocus(e);
  }
  public void lostFocus(FocusEvent e) {
    super.lostFocus(e);
  }
	/**
	 * Setzt das Kennzeichen, ob die ComboBox editierbar ist.
	 */
	public void setEditable(boolean b) {
		component.setEditable(b);
	}

	/**
	 * Setzt die Komponente auch "nur anzeigen" wenn "true" übergeben wird:
	 * Hintergrundfarbe auf hellgrau setzen, nicht editierbar, kein TabStop.
	 */
	public final void setEnabled(boolean b) {
		component.setEnabled(b);
		this.setTabstop(b);
	}
	/**
	 * Liefert den angezeigten Text der ComboBox.
	 * @return
	 */
	public String getText() {
		String val = component.getSelectedItem().toString();
		//String val = component.getEditor().getItem().toString(); 
		return val;
	}

  /**
   * Setzt einen regulären Ausdruck zur Eingabeüberprüfung. Es wird eine
   * Fehlermeldung ausgegeben, wenn der Ausdruck keine korrekte Syntax hat.
   * <p>
   * Bei übergabe von null wird der regläre Ausdruck gelöscht.
   */
  public final void setRegexp(String s) {
    try {
      if (s == null) {
        this.regexp = null;
      } else {
        this.regexp = Pattern.compile(s);
        this.setInputVerifier();
      }
    } catch (PatternSyntaxException ex) {
      GuiUtil.showEx(ex);
    }
  }

  /**
   * Liefert den Regulären Ausdruck zur Eingabeprüfung oder null, wenn keine
   * Eingabeprüfung mit RE vorgenommen wird.
   * 
   * @see #setRegexp
   */
  public final Pattern getRegexp() {
    return regexp;
  }

	/**
	 * Setzt den Inhalt der ComboBox auf einen neuen Wert.<p>
	 * Verfügt die ComboBox über eine Mapping von ValueMember auf DisplayMember, 
	 * wird hier ValueMember erwartet.
	 */
	public void setValue(Object val) {
		this.setModified(false);
		int index = -1;
		// Editierbare ComboBox
		if (component.isEditable()) { // TODO: Was ist wenn editierbar und mit Map? (geht eigentlich nicht!?)
		   if (map == null) {
		      component.getEditor().setItem(val);
      		  if (((DefaultComboBoxModel) component.getModel()).getIndexOf(val) != -1) {
      		     this.setSelectedItem(val);
      		  }
		   } else {
              index = map.indexOf(val); // liefert ggf. auch -1
              if (index == -1 && val != null && val.toString().length() != 0) {
                 // Wenn Wert in Map nicht vorhanden, dann hinzufügen (würde sonst <null> werden)
                 DefaultComboBoxModel mdl = (DefaultComboBoxModel) component.getModel();
                 map.add(val);
                 mdl.addElement("{" + val +"}");
                 index = map.indexOf(val); 
              }
              this.setSelectedIndex(index);
		   }
		} else { // Not editable
			if (map != null) { // Map
				index = map.indexOf(val); // liefert ggf. auch -1
				if (index == -1 && val != null && val.toString().length() != 0) {
				   // Wenn Wert in Map nicht vorhanden, dann hinzufügen (würde sonst <null> werden)
				   DefaultComboBoxModel mdl = (DefaultComboBoxModel) component.getModel();
	               map.add(val);
	               mdl.addElement("{" + val +"}");
	               index = map.indexOf(val); 
	               // Editor wenn Renderer in Table
	               if (this.editor != null) {                  
	                  DefaultComboBoxModel<Object> emdl = (DefaultComboBoxModel<Object>) editor.getCombo().getModel();
	                  emdl.addElement("{" + val +"}");                    
	               }
				}
			} else {// No Map
				DefaultComboBoxModel mdl = (DefaultComboBoxModel) component.getModel();
				index = mdl.getIndexOf(val); // liefert ggf. auch -1
			}
			this.setSelectedIndex(index);
		}
	}

	/**
	 * Liefert den selektierten Eintrag aus der ComboBox oder einen Leerstring, wenn nichts
	 * selektiert, bzw. die Eingabe des Benutzers, wenn sie editierbar ist.
	 * <p>
	 * Wenn eine übersetzungstabelle gesetzt wurde, wird deren Eintrag geliefert.
	 * 
	 * @see #setMap
	 */
	public Object getValue() {
		if (component.isEditable()) {
		   Object oval = component.getEditor().getItem();
		   if (map == null) {		     
		      return oval.toString().trim();
		   } else {
		      if (this.getItems().contains(oval)) {
		         String val = ((String) map.elementAt(getItems().indexOf(oval))).trim();
		         return val;
		      } else {
	              return oval.toString().trim();
		      }
		   }
		} else { // not editable
			if (this.getSelectedIndex() != -1) {
				if (map == null) {
					String val = ((String) component.getSelectedItem()).trim(); 
					return val;
				} else {
					String val = ((String) map.elementAt(component.getSelectedIndex())).trim(); 
					return val;
				}
			} else {
				return "";
			}
		}
	}

	public Object getUnformatedValue() {
		return getValue();
	}


	// From GuiSelect
	public Vector<Object> getItems() {
		final Vector<Object> ret = new Vector<Object>();
		for (int i = 0; i < component.getModel().getSize(); i++) {
			ret.add(component.getModel().getElementAt(i));
		}
		return ret;
	}

	/**
	 * Setzt den selektierten Index zurück
	 */
	public final void reset() {
		this.setSelectedIndex(-1);
		this.setModified(false);
		if (component.isEditable()) {
			component.getEditor().setItem(null);
		}
	}

	// From GuiSelect
	public void setItems(Vector<Object> v) {
		component.setModel(new DefaultComboBoxModel(v));
		this.map = null;
	}

	public void setItems(List<Object> al) {
		Vector<Object> v = new Vector<Object>(al);
		this.setItems(v);
	}

	/**
	 * Füllt die Combobox mit den Werten aus der Datenbank.
	 * <p>
	 * Wird von GuiFactory aufgerufen; Voraussetzung ist, daß der GuiSessen ein Adapter
	 * zugewiesen ist: <br>
	 * GuiBuilderConfig.xml --> ApplicationAdapter.
	 */
	public void pullData() throws Exception {
		if (this.getDatasetName() == null || this.getDisplayMember() == null)
			return;
		ApplicationIF ada = GuiSession.getInstance().getAdapter();
		if (ada == null)
			return;
		JDataSet ds = ada.getDataset(this.getDatasetName());
		this.setItems(ds);
	}



	// From GuiSelect
	public void setItems(String[] s) {
		if (s != null) {
			component.setModel(new DefaultComboBoxModel(s));
		} else  {
			component.setModel(new DefaultComboBoxModel());
			this.map = null;
		}
	}

	// From GuiSelect
	public void addItem(Object val) {
		component.addItem(val);
	}

	// From GuiSelect
	public void addItem(Object val, int index) {
		component.insertItemAt(val, index);
	}
	public void addItems(Object[] items) {
	  if (items == null) return;
	  for (int i = 0; i < items.length; i++) {
	    this.addItem(items[i]);
	  }
	}
	// From GuiSelect
	public void removeItem(Object val) {
		component.removeItem(val);
	}

	// From GuiSelect
	public void removeAllItems() {
		component.removeAllItems();
	}

	// From GuiSelect
	public int getSelectedIndex() {
		return component.getSelectedIndex();
	}

	public Object[] getSelectedItems() {
	  return component.getSelectedObjects();
	}
	// From GuiSelect
	public void setSelectedIndex(int i) {
		component.setSelectedIndex(i);
	}

	// From GuiSelect
	public Object getSelectedItem() {
		return component.getSelectedItem();
	}

	// From GuiSelect
	public void setSelectedItem(Object o) {
		component.setSelectedItem(o);
	}

	public Class getValueClass() {
		return String.class;
	}
}