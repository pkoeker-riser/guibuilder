package de.guibuilder.framework;

import javax.swing.JRootPane;
/**
 * Dieses Interface wird von GuiMenu, GuiMenuBar und GuiPopupMenu implementiert.
 */
public interface MenuAble extends MemberAble {
  /**
   * Fügt dem Menü einen MenuSeparator hinzu.
   */
  public void addSeparator();
  /**
   * Fügt dem Menü einen Menüeintrag hinzu.
   */
  public void add(MenuItemAble item);
  /**
   * Fügt dem Menü ein UnterMenü hinzu
   */
  public void add(GuiMenu menu);
  /**
   * Fügt dem Menü beim ersten Aufruf auch eine OptionGroup hinzu.
   * @see GuiOptionGroup
   */
  public void addOption(OptionAble opt);
  /**
   * Liefert die OptionGroup zu diesem Menu oder null, 
   * wenn dieses Menu keine Optionen hält.
   * @see GuiMenuItemOption
   * @return
   */
  public GuiOptionGroup getOptionGroup();
  /**
   * From JComponent
   */
  public JRootPane getRootPane();
}