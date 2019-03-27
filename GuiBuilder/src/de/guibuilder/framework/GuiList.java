package de.guibuilder.framework;

import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.jdataset.JDataSet;

// TODO : Mapping wie ComboBox
/**
 * Implementierung einer ListBox.
 * <p>
 * Es wird ein SelectionListener eingerichtet.
 * <p>
 * Das Verhalten der Listbox bei get- und setValue kann mit setListboxType gesteuert
 * werden:
 * <ul>
 * <li>NORMAL liefert und setzt den selektierten Wert; die Listbox benimmt sich wie eine
 * Combobox.
 * <li>MULTI liefert und setzt mehrere Werte; der Benutzer kann mehrere Einträge
 * selektieren.
 * <li>ALL liefert und setzt alle Werte.
 * </ul>
 * Achtung! <br>
 * Auch wenn die Listbox nicht auf NORMAL steht, wird beim Anklicken durch den Benutzer im
 * Ereignis "Changed" immer der jeweils angeklickte Wert geliefert.
 * <p>
 */
public final class GuiList extends GuiSelect {
	// Attributes
	public static final String VERTICAL = "VERTICAL";
	public static final String VERTICAL_WRAP = "VERTICAL_WRAP";
	public static final String HORIZONTAL_WRAP = "HORIZONTAL_WRAP";
	/**
	 * Delegate
	 */
	private JList component;
	//private Vector map;
	/**
	 * Bestimmt die Art des Verhaltens der Listbox bei get- und setValue.
	 * <ul>
	 * <li>NORMAL liefert und setzt den selectierten Wert
	 * <li>MULTI liefert und setzt mehrere Werte
	 * <li>ALL liefert und setzt alle Werte
	 * </ul>
	 */
	private int listboxType = NORMAL;
	/**
	 * Normale Listebox; es kann nur ein Eintrag gewählt werden.
	 */
	public static final int NORMAL = 0;
	/**
	 * Es können mehrere Einträge selektiert werden.
	 */
	public static final int MULTI = 1;
	/**
	 * Bei getValue werden immer alle Einträge geliefert.
	 */
	public static final int ALL = 2;

	// Constructors
	/**
	 * Erzeugt eine leere ListBox.
	 * <p>
	 * Weist ein DefaultListModel zu.
	 */
	public GuiList() {
		super();
		component = new JList();
		component.setModel(new DefaultListModel());
		guiInit();
	}
	/**
	 * Erzeugt eine ListBox mit Inhalt.
	 * 
	 * @param v
	 *           Vector von Einträgen (Strings) für die Listbox.
	 */
	public GuiList(Vector<Object> v) {
		super();
		component = new JList(v);
		guiInit();
	}

	/**
	 * Erzeugt eine ListBox mit Inhalt.
	 * 
	 * @param s
	 *           Array von Strings für den Inhalt der Listbox.
	 */
	public GuiList(String[] s) {
		super();
		component = new JList(s);
		guiInit();
	}

	// Methods
	private void guiInit() {
		this.setName("list");
		component.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.addFocusListener(new GuiFocusListener(this));
		component.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				postProc();
				setModified(true);
				// Inhalt an verknüpfte Spalte weiterreichen?
				if (linkTable != null) {
					linkTable.setCellValue(linkColumn, getValue());
				}
				// Externen Controller informieren
				if (e.getValueIsAdjusting() == false) {
					if (actionChange != null) {
						getRootPane().obj_ItemChanged(GuiList.this, actionChange,
								getValue(NORMAL), getSelectedIndex());
					}
				}
			}
		});

	}

	/**
	 * Liefert "List"
	 */
	public final String getTag() {
		return "List";
	}

	/**
	 * Liefert JList
	 */
	public JComponent getJComponent() {
		return component;
	}

	/**
	 * Liefert NORMAL wenn NORMAL, ansonsten ENUM.
	 */
	public final int getDataType() {
		if (getListboxType() == NORMAL) {
			return STRING;
		} else {
			return ENUM;
		}
	}

	int getListboxType() {
		return listboxType;
	}

	/**
	 * Es sind die Werte NORMAL, MULTI und ALL erlaubt; ansonsten wird eine
	 * IllegalArgumentException geworfen.
	 */
	public void setListboxType(String t) {
		if (t.equals("NORMAL")) {
			listboxType = NORMAL;
		} else if (t.equals("MULTI")) {
			listboxType = MULTI;
			component.getSelectionModel().setSelectionMode(
					ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else if (t.equals("ALL")) {
			listboxType = ALL;
		} else {
			throw new IllegalArgumentException("Illegal ListboxTyp: NORMAL, MULTI, ALL");
		}
	}

	/**
	 * Liefert ein StringArray von Einträgen die durch | getrennt sind.
	 */
	private String[] getItems(String s) {
		if (s == null) {
			return new String[0];
		}
		String[] items = s.split("\\|");
		return items;
	} 

	/**
	 * Fügt der ListBox einen Eintrag hinzu; vorzugsweise Strings.
	 * <P>
	 * Diese Methode fehlt in JList tatsächlich!!!
	 */
	public void addItem(Object s) {
		final DefaultListModel mdl = (DefaultListModel) component.getModel();
		mdl.addElement(s);
	}

	public void addItems(Object[] items) {
	  if (items == null) return;
    DefaultListModel mdl = (DefaultListModel) component.getModel();
	  for (int i = 0; i < items.length; i++) {
	    mdl.addElement(items[i]);
	  }
	}
	/**
	 * Füllt die Listbox mit dem übergebenen Vector neu.
	 * <p>
	 * Bei übergaben von null wird die Listbox geleert.
	 * <P>
	 * Die Swing-Methode <code>setListData</code> ruiniert leider das ListModel!
	 */
	public void setItems(Vector<Object> v) {
		((DefaultListModel) component.getModel()).clear();
		if (v != null) {
			for (int i = 0; i < v.size(); i++) {
				//if (component.getPr
				this.addItem(v.elementAt(i));
			}
		}
	}

	/**
	 * Füllt die Listbox mit dem übergebenen Array neu.
	 * <P>
	 * Bei übergaben von null wird die Listbox geleert.
	 * <P>
	 * Die Swing-Methode <code>setListData</code> ruiniert leider das ListModel!
	 */
	public void setItems(String[] s) {
		((DefaultListModel) component.getModel()).clear();
		if (s != null) {
			for (int i = 0; i < s.length; i++) {
				this.addItem(s[i]);
			}
		}
	}

	/**
	 * Löscht aus der ListBox einen Eintrag.
	 * <P>
	 * Diese Methode fehlt in JList tatsächlich!!!
	 */
	public void removeItem(Object s) {
		final DefaultListModel mdl = (DefaultListModel) component.getModel();
		mdl.removeElement(s);
	}

	/**
	 * Setzt den selektierten Wert auf einen bestimmten Eintrag.
	 */
	public void setValue(Object val) {
		switch (listboxType) {
		case NORMAL:
			int index = -1;
			if (map != null) { // Map
				index = map.indexOf(val); // liefert ggf auch -1
				this.setSelectedIndex(index);
			} else {// No Map
				this.setSelectedItem(val);
			}

			break;
		case MULTI: {
			if (val instanceof String) {
				final String[] items = getItems((String)val);
				final DefaultListModel mdl = (DefaultListModel) component.getModel();
				final int end = items.length;
				final int indexes[] = new int[end];
				for (int i = 0; i < end; i++) {
					indexes[i] = mdl.indexOf(items[i]);
				}
				component.setSelectedIndices(indexes);
			}
		}
			break;
		case ALL: {
			if (val instanceof String) {
				final String[] items = getItems((String)val);
				this.setItems(items);
			} // End If String
			else {
				if (val == null) {
					((DefaultListModel) component.getModel()).clear();
				}
			}
		}
			break;
		}
		this.setModified(false);
	}

	public Object getUnformatedValue() {
		return getValue();
	}

	/**
	 * Liefert den selektierten Eintrag oder den gesamten Inhalt der ListBox.
	 */
	public Object getValue() {
		return getValue(listboxType);
	}

	/**
	 * Liefert den Wert der Listbox je nach übergebnen Type:
	 * <ul>
	 * <li>NORMAL liefert den Selektierten Eintrag als String.
	 * <li>MULTI liefert alle selektierten Einträge mit | getrennt.
	 * <li>ALL liefert alle Einträge mit | getrennt.
	 * </ul>
	 */
	public Object getValue(int type) {
		String ret = null;
		switch (type) {
		case NORMAL:
			if (map == null) {
				ret = (String) this.getSelectedItem();
			} else {
				ret = ((String) map.elementAt(component.getSelectedIndex())).trim();
			}	
			break;
		case MULTI: {
			final Object vals[] = component.getSelectedValues();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < vals.length; i++) {
				sb = sb.append(vals[i].toString());
				sb.append("|");
			}
			if (sb.length() > 0) {
				sb = sb.deleteCharAt(sb.length() - 1);
			}
			ret = sb.toString();
		}
			break;
		case ALL:
			ret = this.getValues();
			break;
		}
		// Trim?
		if (ret != null) {
			ret = ret.trim();
		}
		return ret;
	}

	/**
	 * Bei den Typen NORMAL und MULTI wird slectedIndex zurückgesetzt; bei ALL werden alle
	 * Einträge aus der ListBox gelöscht.
	 */
	public void reset() {
		this.setModified(false);
		switch (listboxType) {
		case NORMAL:
			this.setSelectedIndex(-1);
			break;
		case MULTI:
			this.setSelectedIndex(-1);
			break;
		case ALL:
			this.removeAllItems();
			break;
		}
	}

	/**
	 * Liefert alle Einträge der Listbox mit "|" getrennt.
	 */
	public String getValues() {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < component.getModel().getSize(); i++) {
			sb = sb.append(component.getModel().getElementAt(i).toString());
			sb.append("|");
		}
		if (sb.length() > 0) {
			sb = sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	// From GuiSelect
	public Vector<Object> getItems() {
		final Vector<Object> ret = new Vector<Object>();
		for (int i = 0; i < component.getModel().getSize(); i++) {
			ret.add(component.getModel().getElementAt(i));
		}
		return ret;
	}

	// From GuiSelect
	public void addItem(Object val, int index) {
		((DefaultListModel) component.getModel()).insertElementAt(val, index);
	}

	// From GuiSelect
	public void removeAllItems() {
	  this.setMap((Vector<Object>)null);
		((DefaultListModel) component.getModel()).removeAllElements();
	}

	/**
	 * Komponente wurde doppelt geklickt.
	 * 
	 * @see GuiRootPane#obj_DblClick(GuiComponent, String, Object, MouseEvent)
	 */
	public void d_click(MouseEvent e) {
		if (actionDblClick != null) {
			getRootPane().obj_DblClick(this, actionDblClick, this.getValue(), e);
		}
	}

	/**
	 * Liefert den selektierten Index; bei MULTI wird der erste Index geliefert.
	 */
	public int getSelectedIndex() {
		return component.getMinSelectionIndex();
	}

	/**
	 * Delegation zu JList.setSelectedInterval
	 */
	public void setSelectedIndex(int i) {
		component.setSelectionInterval(i, i);
	}

	/**
	 * Liefert den selektierten Eintrag.
	 */
	public Object getSelectedItem() {
		return component.getSelectedValue();
	}
	
	public Object[] getSelectedItems() {
	  return component.getSelectedValues();
	}

	/**
	 * Setzt den zu selektierenden Eintrag.
	 */
	public void setSelectedItem(Object o) {
		component.setSelectedValue(o, true);
	}
	
	/**
	 * @see JList#clearSelection()
	 */
	public void clearSelection() {
	  if (component != null) component.clearSelection();
	}

	public void setLayout(String lay) {
		if (lay.equals(VERTICAL)) {
			component.setLayoutOrientation(JList.VERTICAL);
		} else if (lay.equals(VERTICAL_WRAP)) {
			component.setLayoutOrientation(JList.VERTICAL_WRAP);
		} else if (lay.equals(HORIZONTAL_WRAP)) {
			component.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		} else {
			throw new IllegalArgumentException("VERTICAL, VERTICAL_WRAP, HORIZONTAL_WRAP");
		}
	}
	
  public void setDatasetValues(JDataSet ds) {
    if (this.getElementName() == null)
      return;
    String xpath = this.getElementPath("");
    this.setItems(ds, xpath);
  }  
}