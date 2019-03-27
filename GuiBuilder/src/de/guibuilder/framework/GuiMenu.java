package de.guibuilder.framework;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JRootPane;

/**
 * Implementierung eines Menüs.
 */
public class GuiMenu extends JMenu implements MenuAble {
   private static final long serialVersionUID = 1L;
// Attributes
  /**
   * Button Group für GuiMenuItemOption
   */
  private transient GuiOptionGroup optionGroup;
  /**
   * Verweis auf das ParentMenu (wenn UnterMenü) oder Menubar.
   */
  private MenuAble parentMenu;
  // Constructor
  /**
   * Erzeugt ein Menü mit einer definierten Beschriftung.
   */
  public GuiMenu (String title) {
    super(title);
    // Mnemo
    char mnemo;
    final int p = title.indexOf("%");
    if (p != -1 && p+1 < title.length()) {
      mnemo = title.charAt(p+1);
      title = title.substring(0,p) + title.substring(p+1);
      this.setText(title);
      if (mnemo != '%') {
        this.setMnemonic(mnemo);
        this.setDisplayedMnemonicIndex(p);
      }
    }
    this.setName(GuiUtil.labelToName(title));
    this.setVerifyInputWhenFocusTarget(false);
  }
  // Methods
  public final String getTag() {
    return "Menu";
  }
  /**
   * Fügt dem Menü einen Menüeintrag hinzu.<p>
   * Setzt bei dem Menüeintrag auch den Parent.
   */
  public final void add(MenuItemAble item) {
    super.add(item.getJComponent());
    item.setGuiMenu(this);
  }
  /**
   * Fügt dem Menü ein UnterMenü hinzu.
   */
  public final void add(GuiMenu menu) {
    super.add(menu);
    menu.setParentMenu(this);
  }
  /**
   * Fügt dem Menü ein UnterMenü mit dem angegebenen Text hinzu
   * und liefert eine Referenz auf das so erzeugte Menü.
   */
  public final GuiMenu addMenu(String label) {
    GuiMenu menu = new GuiMenu(label);
    this.add(menu);
    return menu;
  }
  /**
   * Fügt dem Menü einen normalen Menüeintrag hinzu.
   */
  public final GuiMenuItem addItem(String label) {
    GuiMenuItem item = null;
    if (label.equals("-")) {
      this.addSeparator();
    } else {
      item = new GuiMenuItem(label, this);
    }
    return item;
  }
  /**
   * Fügt dem Menü einen Radiobutton hinzu.<BR>
   * Der erste hinzugefügte Radiobutton wird - nicht! - selektiert.<BR>
   * Je Menü kann nur eine Gruppe von RadioButtons definiert werden.<p>
   * Beim ersten RadioButton wird die OptionGroup als Member beim
   * MainPanel registriert.<p>
   * OptionGroup und RadioButton werden wechselseitig miteinander verknüpft.
   */
  public final void addOption(OptionAble opt) {
    if (optionGroup == null) {
      optionGroup = new GuiOptionGroup(opt.getName());
      optionGroup.setMsgChange(opt.getMsgChange());
      opt.setSelected(true);
      this.getGuiRootPane().getMainPanel().addMember(optionGroup);
    }
    optionGroup.add(opt);
  }
  /**
   * Liefert die OptionGroup zu diesem Menu oder null, 
   * wenn dieses Menu keine Optionen hält.
   * @see GuiMenuItemOption
   * @return
   */
  public final GuiOptionGroup getOptionGroup() {
      return this.optionGroup;
  }
  /**
   * Fügt dem Menü einen neuen Eintrag mit einem Label und einem ActionCommand
   * hinzu.
   * @param label Beschriftung des Menüeintrags.
   * @param name Name des Menüeintrags; wenn null wird das Label als Name verwendet.
   * @param cmd ActionCommand, welches beim Betätigen dieses Menüeintrags
   * übergeben wird; wenn null, wird das Label als ActionCommand verwendet.
   * @param type Typ des Menüeintrags: NORMAL, OPTION, CHECK.
   */
  public final void addGuiMenuItem(String label, String name, String cmd, String type) {
    if (label == null) {
      throw new IllegalArgumentException("Label can't be null");
    }
    MenuItemAble mItem = null;
    if (type.equals("OPTION")) {
      mItem = new GuiMenuItemOption(label, this);
    } else if (type.equals("CHECK")) {
      mItem = new GuiMenuItemCheckBox(label, this);
    } else {
      mItem = new GuiMenuItem(label, this);
    }
    if (cmd != null) {
      mItem.setActionCommand(cmd);
    }
    if (name != null) {
      mItem.setName(name);
    }
    // Phase 2: Nach setActionCommand
    if (type.equals("OPTION")) {
      this.addOption((OptionAble)mItem);
    } else if (type.equals("CHECK")) {
      getGuiRootPane().getMainPanel().addMember((GuiMenuItemCheckBox)mItem);
    } else {
      getGuiRootPane().getMainPanel().addAction((GuiMenuItem)mItem);
    }
  }
  /**
   * Liefert das Menü mit dem angegebenen Namen.<br>
   * Es wird nur nach den Menüs gesucht, die UnterMenüs zu
   * diesem Menü sind.
   * @throws IllegalArgumentException Wenn es ein derartiges Menu nicht gibt.
   */
  public final GuiMenu getGuiMenu(String name) {
    if (name == null) return null;
    for (int i=0; i<this.getItemCount(); i++) {
      Component comp = this.getMenuComponent(i);
      if (name.equals(comp.getName()) && comp instanceof GuiMenu) {
        return (GuiMenu)comp;
      }
    }
    throw new IllegalArgumentException("Missing Menu: "+name);
  }
  /**
   * Wird wegen Bug in Swing hier für UnterMenüs überschrieben.
   */
  public JRootPane getRootPane() {
    return parentMenu.getRootPane();
  }
  public Component getAwtComponent() {
	  return this;
  }
  GuiRootPane getGuiRootPane() {
    return (GuiRootPane)getRootPane();
  }
  void setParentMenu(MenuAble m) {
    parentMenu = m;
  }
  MenuAble getParentMenu() {
    return parentMenu;
  }
}