package de.guibuilder.framework;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JComponent;

/**
 * Dieses Interface wird von den Komponenten implementiert, die als
 * Spalte einer Tabelle verwendet werden können.
 * <ul>
 * <li>GuiText (und abgeleitete Klassen wie Number, Date, Time, Money)
 * <li>GuiCheck
 * <li>GuiCombo
 * <li>GuiLabel
 * <li>HiddenField
 * </ul>
 * Die Methoden sind (fast) alle von GuiComponent.
 * @see GuiTable#addColumn
 */
public interface TableColumnAble {
  String getName();
  int getDataType();
  String getToolTipText();
  void setToolTipText(String text);
  Dimension getMinimumSize();
  Dimension getMaximumSize();
  boolean isVisible();
  void setEnabled(boolean b);
  boolean isEnabled();
  Color getBackground();
  Color getForeground();
  Font getFont();
  void setFont(Font f);
  JComponent getJComponent();
  GuiComponent getGuiComponent();
  String getTag();
  /**
   * Liefert die Tabelle, mit der die Spalte verknüpft ist.
   */
  GuiTable getParentTable();
  /**
   * Verknüpft die Tabellenspalte mit der Tabelle.<p>
   * Diese Methode ist nur aus implementierungstechnischen Gründen public
   * (weil Interface); also Vorsicht!.
   * @see GuiTable#addColumn
   */
  void setParentTable(GuiTable tbl);
  /**
   * Liefert die Klasse, die dem Wert der Komponente entspricht.
   * String, Number, Date, Boolean.<p>
   * unused!
   */
  Class getValueClass();
  // Bindings
  // Element-Name
  public String getElementName();
  void setGuiParent(GuiContainer c);
}