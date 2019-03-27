package de.guibuilder.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import de.jdataset.JDataRow;
import de.jdataset.JDataSet;

/**
 * Abstrakte Basisklasse für die Vereinheitlichung des Verhaltens von Combo-
 * und ListBox.
 */
public abstract class GuiSelect extends GuiComponent {
  // Binding
  private String datasetName;
  private String displayMember;
  private String valueMember;
  
	/**
	 * Optionaler Vector für die gelieferten Werte von getValue.
	 * @see #getValue
	 */
	protected Vector<Object> map;

/**
 * @return
 */
public final String getDatasetName() {
	return datasetName;
}

/**
 * @param datasetName
 */
public final void setDatasetName(String datasetName) {
	this.datasetName = datasetName;
}

/**
 * @return
 */
public final String getDisplayMember() {
	return displayMember;
}

/**
 * @param displayMember
 */
public final void setDisplayMember(String displayMember) {
	this.displayMember = displayMember;
}

/**
 * @return
 */
public final String getValueMember() {
	return valueMember;
}

/**
 * @param valueMember
 */
public final void setValueMember(String valueMember) {
	this.valueMember = valueMember;
}

  // Methods
  /**
   * Liefert den vom Benutzer ausgewählten Index (0-relativ)
   * oder -1 wenn kein Eintrag gewählt wurde.<p>
   * Achtung!<br>
   * Wenn List vom Typ MULTI ist, wird der erste Index geliefert.
   */
  public abstract int getSelectedIndex();
  public abstract void setSelectedIndex(int i);
  public abstract void setSelectedItem(Object o);
  /**
   * Liefert im Unterschied zu getValue "wirklich" ein Object.
   */
  public abstract Object getSelectedItem();
  public abstract Object[] getSelectedItems();
  /**
   * Liefert die "übersetzung" ValueMember --> DisplayMember.
   * <p>
   * Wenn keine Map vorhanden, dann wird der übergebene Wert zurückgeliefert. Wenn der
   * Eintrag in der Map fehlt, wird null geliefert.
   * 
   * @param val
   *           Ein Wert aus der Menge der ValueMembers
   * @return Object Dem ValueMember entsprechenden DisplayMember oder null, falls dieser
   *         nicht gefunden wurde.
   * @see #setMap
   */
  public Object getDisplayMemberValue(Object mapValue) {
    if (mapValue == null) return null;
    Object ret = null;
    if (map != null) {
      int index = map.indexOf(mapValue.toString()); // #toString() 5.4.2005 PKÖ
      if (index != -1) {
        Vector<Object> v = this.getItems();
        ret = v.elementAt(index);
      }
    } else {
      ret = mapValue;
    }
    return ret;
  }
  /**
   * Liefert die "übersetzung" DisplayMember --> ValueMember.
   * <p>
   * Wenn keine Map vorhanden, dann wird der übergebene Wert zurückgeliefert. Wenn der
   * Eintrag in der Map fehlt, wird null geliefert.
   * 
   * @param val
   *           Ein Wert aus der Menge der ValueMembers
   * @return Object Dem DisplayMember entsprechenden ValueMember oder null, falls dieser
   *         nicht gefunden wurde.
   * @see #setMap
   */
  public Object getValueMemberValue(Object disValue) {
    if (disValue == null) return null;
    Object ret = null;
    Vector<Object> v = this.getItems();
    int index = v.indexOf(disValue);
    if (index == -1) return null;    
    if (map != null) {
      ret = map.get(index);
    } else {
      ret = disValue;
    }
    return ret;
  }
  /**
   * Setzt alle Einträge der Box als Vector.
   */
  public abstract void setItems(Vector<Object> v);
	
  public void setItems(List<Object> al) {
		Vector<Object> v = new Vector<Object>(al);
		this.setItems(v);
	}

  /**
   * Setzt alle Einträge der Box als Array von Strings.
   */
  public abstract void setItems(String[] s);
  /**
   * Füllt die ComboBox mit den Werten aus einem DataSet.
   * <p>
   * Es werden die Angaben zu DisplayMember und ValueMember ausgewertet.
   * <p>
   * ValueMember darf auch null sein; dann wird kein Mapping DiplayMember --> ValueMember
   * vorgenommen.
   * <p>
   * Ist auch DispayMember null, wird die erste Spalte der DataRow verwendet.
   * 
   * @see #setDisplayMember
   * @see #setValueMember
   * @param ds
   */
  public void setItems(JDataSet ds) {
    if (ds == null) {
      return;
    }
    Iterator<JDataRow> it = ds.getChildRows();
    if (it != null) {
      this.setItems(it);
    } else {
      this.setItems((String[])null);
    }
  }

  /**
   * Füllt die ComboBox mit den Werten aus einer DataTable eines DataSet.
   * <p>
   * @see #setItems(JDataSet)
   * @param ds
   * @param rowPath
   */
  public void setItems(JDataSet ds, String rowPath) {
    Iterator<JDataRow> rows = ds.getDataRowsPath(rowPath);
    if (rows != null) {
      this.setItems(rows);
    } else {
      this.setItems((String[])null);
    }
  }

  private void setItems(Iterator<JDataRow> i) {
    Vector<Object> _map = new Vector<Object>();
    Vector<Object> items = new Vector<Object>();
    String vm = this.getValueMember();
    String dm = this.getDisplayMember();
    while (i.hasNext()) {
      JDataRow row = i.next();
      if (row.isDeleted())
        continue;
      if (vm != null) {
        _map.add(row.getValue(vm));
      }
      if (dm == null) { // Wenn kein DisplayMember angegeben, dann die letzte Spalte
        int columns = row.getColumnCount();
        items.add(row.getValue(columns -1));
      } else {
        items.add(row.getValue(dm));
      }
    }
    setItems(items);
    if (_map.size() > 0) {
      this.setMap(_map);
    }
  }

  /**
   * Fügt einen Eintrag - am Ende - der Box hinzu.
   */
  public abstract void addItem(Object item);
  public abstract void addItems(Object[] items);
  /**
   * Fügt einen Eintrag - an dem angegebenen Index - ein.
   */
  public abstract void addItem(Object item, int index);
  /**
   * Löscht den angegebenen Eintrag aus der Box.
   */
  public abstract void removeItem(Object item);
  /**
   * Löscht alle Einträge
   * @see GuiList#reset
   */
  public abstract void removeAllItems();
  /**
   * Liefert die Menge der Einträge.
   */
  public abstract Vector<Object> getItems();
  // MAP
	/**
	 * Setzt eine übersetzungstabelle (ValueMembers) - die genauso groß sein muß wie die
	 * Menge der Einträge - für die gelieferten Werte.
	 * 
	 * @see #getValue
	 */
	public void setMap(Vector<Object> v) {
		// TODO : Das sollte doch mit einem ComboBoxModel besser gehen?
		this.map = v;
	}

	/**
	 * Setzt die übersetzungstabelle.
	 */
	public void setMap(String[] s) {
		this.map = new Vector<Object>(s.length);
		for (int i = 0; i < s.length; i++) {
			map.add(s[i]);
		}
	}

	/**
	 * Setzt eine übersetzungstabelle (ValueMembers) - die genauso groß sein muß wie die
	 * Menge der Einträge - für die gelieferten Werte.
	 * 
	 * @see #getValue
	 */
	public void setMap(ArrayList<Object> al) {
		this.map = new Vector<Object>(al.size());
		for (Iterator<Object> i = al.iterator(); i.hasNext();) {
			map.add(i.next());
		}
	}
	/**
	 * Liefert die Menge der Value-Werte.
	 * @return
	 */
	public Vector<Object> getMap() {
		return map;
	}
  
}