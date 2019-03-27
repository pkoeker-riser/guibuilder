package de.guibuilder.framework;

import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

/**
 * Implementierung eines Menüeintrags in Form eines RadioButtons.
 * Verhält sich wie ein normaler RadioButton.
 * @see GuiOption
 */
public final class GuiMenuItemOption extends GuiComponent implements MenuItemAble, OptionAble {
  // Attributes
  private JRadioButtonMenuItem component;
  private transient MenuAble parentMenu;
	private char enabledWhen; // S,M,N,A
	public char getEnabledWhen() {
		return enabledWhen;
	}

	public void setEnabledWhen(char enabledWhen) {
		this.enabledWhen = enabledWhen;
	}
  /**
   * Verweis auf die OptionGroup zum dem dieser RadioButton gehört.
   */
  private GuiOptionGroup optionGroup;
  // Constructor
  private GuiMenuItemOption(String label) {
    super(label);
    component.setText(label); // ist nie null wegen GuiMember.setName()

    char mnemo;
    int p = label.indexOf("%");
    if (p != -1 && p+1 < label.length()) {
      mnemo = label.charAt(p+1);
      label = label.substring(0,p) + label.substring(p+1);
      component.setText(label);
      if (mnemo != '%') {
        component.setMnemonic(mnemo);
        this.component.setDisplayedMnemonicIndex(p);
      }
    }
    component.setActionCommand(this.getName());
    component.addActionListener(new GuiChangeListener(this));
  }
  /**
   * Erzeugt einen Menüeintrag, der dem übergebenen Menü am Ende hinzugefügt wird.
   */
  public GuiMenuItemOption(String label, MenuAble menu) {
    this(label);
    menu.add(this);
  }
  // Methods
  public final String getTag() {
    return "ItemOption";
  }
  public JComponent getJComponent() {
    if (component == null) component = new JRadioButtonMenuItem();
    return component;
  }
  public final int getDataType() {
    return STRING;
  }
  /**
   * @see GuiMenu#addOption
   */
  public void setOptionGroup(GuiOptionGroup grp) {
    optionGroup = grp;
  }
  /**
   * Wegen Bug in Swing wird JComponent.getRootPane hier überschrieben.
   */
  public GuiRootPane getRootPane() {
    return (GuiRootPane)parentMenu.getRootPane();
  }
  /**
   * Wird an OptionGroup delegiert
   * @see GuiOptionGroup#updateValue
   */
  void obj_ItemChanged(ActionEvent e) {
    setModified(true);
    // Inhalt weiterreichen an OptionGroup
    if (component.isSelected() == true && optionGroup != null) {
      optionGroup.updateValue(this, component.getActionCommand());
    }
  }
  public void setValue(Object val) {
    if (val == null) {	// neu
      component.setSelected(false);
    }
    else if (val.toString().equals(component.getActionCommand())) {
      component.setSelected(true);
    }
    this.setModified(false);
  }
  /*
  public void setValue(String val) {
    if (val == null) {	// neu
      component.setSelected(false);
    }
    else if (val.equals(component.getActionCommand())) {
      component.setSelected(true);
    }
    this.setModified(false);
  }
  */
  
  public Object getUnformatedValue() {
	return getValue();	
  }
  
  /**
  Liefert den Wert, den diese Option repräsentiert, wenn sie selektiert ist;
  ansonsten Boolen.FALSE
  */
  public Object getValue() {
    if (component.isSelected() == true) {
      return component.getActionCommand();
    }
    else {
      return Boolean.FALSE;
      //return null;
    }
  }
  public void reset() {
    component.setSelected(false);
    this.setModified(false);
  }
  /**
   * From JRadioButtonMenuItem
   * @return
   */
  public String getText() {
    return component.getText();
  }
  public void setText(String s) {
  		component.setText(s);
  }
  
  public String getActionCommand() {
    return component.getActionCommand();
  }
  public void setActionCommand(String a) {
    component.setActionCommand(a);
  }
  public void setSelected(boolean b) {
    component.setSelected(b);
  }
  /**
   * From JRadioButtonMenuItem
   * @param icon
   */
  public void setIcon(Icon icon) {
    component.setIcon(icon);
  }
  /**
   * From JRadioButtonMenuItem
   * @param c
   */
  public void setMnemonic(char c) {
    component.setMnemonic(c);
  }
  /**
   * From JRadioButtonMenuItem
   * @param key
   */
  public void setAccelerator(KeyStroke key) {
    component.setAccelerator(key);
  }
  public AbstractButton getButton() {
    return component;
  }
  // From MenuItemAble
  public MenuAble getGuiMenu() {
    return parentMenu;
  }
  public void setGuiMenu(MenuAble menu) {
    parentMenu = menu;
  }
}