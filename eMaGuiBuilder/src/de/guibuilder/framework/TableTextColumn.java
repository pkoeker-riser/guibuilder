/*
 * Created on 06.01.2004
 */
package de.guibuilder.framework;

import java.awt.event.MouseEvent;
import java.text.ParseException;

/**
 * Interface für eine Tabellenspalte als TextBox
 */
public interface TableTextColumn extends TableColumnAble {
	public int getHorizontalAlignment();
	public void postProc();
	public boolean hasTabstop();
	public void d_click(MouseEvent e);
	public String makeFormat(String value) throws ParseException;
   public String makeFormat(Number value) throws ParseException;
}
