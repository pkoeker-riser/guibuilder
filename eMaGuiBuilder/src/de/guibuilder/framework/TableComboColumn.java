/*
 * Created on 08.01.2004
 */
package de.guibuilder.framework;

import java.util.Vector;

import javax.swing.JComboBox;

/**
 * Interface für eine Tabellenspalte als Combobox
 */
public interface TableComboColumn extends TableColumnAble {
	public JComboBox getCombo();
	public Object getValue();
	public void setValue(Object val);
	public Vector<Object> getItems();
	public Vector<Object> getMap();
	public String getDisplayMember();
	public String getValueMember();
}
