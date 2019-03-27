package de.guibuilder.framework;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
/**
 * Interface für die drei verschiedenen Menüeinträge.
 */
public interface MenuItemAble extends MemberAble {
  /**
   * Liefert die swing-Komponente zum Menü-Eintrag.
   */
  public JComponent getJComponent();
  /**
   * Liefert das Menü zu diesem Menü-Eintrag
   */
  public MenuAble getGuiMenu();
  /**
   * Setzt das Menü zu diesem Menü-Eintrag
   */
  public void setGuiMenu(MenuAble menu);
  /**
   * From swing.AbstractButton
   */
  public void setActionCommand(String cmd);
  /**
   * From awt.Component
   */
  public void setName(String name);
  public String getName();
  public void setAccelerator(KeyStroke key);
  public void setMnemonic(char c);
  public void setIcon(Icon icon);
  public String getText();
  public void setText(String s);
  public void setEnabledWhen(char c);
  public char getEnabledWhen();
  public boolean isEnabled();
  public void setEnabled(boolean b);

}