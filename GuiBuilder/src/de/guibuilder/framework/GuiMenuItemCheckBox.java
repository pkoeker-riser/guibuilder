package de.guibuilder.framework;

import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/** @todo Map wie bei Check */

/**
Implementierung eines Menüeintrags in Form einer CheckBox.<p>
Ein ActionListner sorgt dafür, daß das Anklicken der Checkbox
an das RootPane des Formulars weiter geleitet wird.<p>
Diese Komponente entspricht in ihrem Verhalten einer "normalen" CheckBox.
*/
public final class GuiMenuItemCheckBox extends GuiComponent implements MenuItemAble {
  // Attributes
  private JCheckBoxMenuItem component;
  private boolean invert;
  private String trueValue;
  private String falseValue;
  private transient MenuAble parentMenu;
	private char enabledWhen; // S,M,N,A
	public char getEnabledWhen() {
		return enabledWhen;
	}

	public void setEnabledWhen(char enabledWhen) {
		this.enabledWhen = enabledWhen;
	}
  // Constructor
  /**
   * Erzeugt eine CheckBox mit der angegebenen Beschriftung.
   */
  private GuiMenuItemCheckBox(String label) {
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
    this.setActionCommand(this.getName());
    component.addActionListener(new GuiChangeListener(this));
  }
  /**
   * Erzeugt einen Menüeintrag, der dem übergebenen Menü am Ende hinzugefügt wird.
   */
  public GuiMenuItemCheckBox(String label, MenuAble menu) {
    this(label);
    menu.add(this);
  }
  // Methods
  public final String getTag() {
    return "ItemCheck";
  }
  public JComponent getJComponent() {
    if (component == null) {
    	component = new JCheckBoxMenuItem();
    }
    return component;
  }
  /**
   * Liefert BOOLEAN oder STRING (wenn map)
   */
  public final int getDataType() {
    if (trueValue != null) {
      return STRING;
    }
    else {
      return BOOLEAN;
    }
  }
  /**
   * Wegen Bug in Swing wird JComponent.getRootPane hier überschrieben.
   */
  public GuiRootPane getRootPane() {
    return (GuiRootPane)parentMenu.getRootPane();
  }

  /**
   * Setzt den Inhalt der Komponente.
   * Es muß ein Boolean oder ein String (mit dem Inhalt "true" oder "false")
   * übergeben werden oder die Werte gemäß der Map.
   * @see #setMap
   */
  public final void setValue(Object val) {
    Boolean b = Boolean.valueOf(false);
    if (val instanceof Boolean) {
      b = (Boolean)val;
    } else if (val instanceof String) {
      String s = (String)val;
      if (s.equals("y")) {
        b = Boolean.valueOf(true);
      } else {
        b = Boolean.valueOf(s);
      }
      if (s.equals(trueValue)) {
        b = Boolean.valueOf(true);
      }
      if (s.equals(falseValue)) {
        b = Boolean.valueOf(false);
      }
    }
    if (invert == true) {
      component.setSelected(!b.booleanValue());
    } else {
      component.setSelected(b.booleanValue());
    }
    this.setModified(false);
  }  
  
  public Object getUnformatedValue() {
  		return getValue();	
  }
  
  /**
   * Liefert ein Boolean oder einen String, wenn gemapt.
   * @see #setMap
   */
  public final Object getValue() {
    Object val = null;
    boolean bVal = false;
    if (invert == true) {
      val = Boolean.valueOf(!component.isSelected());
      bVal = !component.isSelected();
    } else {
      val = Boolean.valueOf(component.isSelected());
      bVal = component.isSelected();
    }
    if (bVal == true && trueValue != null) {
      val = trueValue;
    }
    if (bVal == false && falseValue != null) {
      val = falseValue;
    }
    return val;
  }
  public final boolean isInvert() {
    return invert;
  }
  /**
   * Setzt die Checkbox als "verdreht":
   * Eine gesetztes Häkchen liefert false.
   */
  public final void setInvert(boolean b) {
    invert = b;
  }
  public void reset() {
    this.setSelected(false);
    this.setModified(false);
  }
  /**
   * From JCheckBoxMenuItem
   * @return
   */
  public String getText() {
    return component.getText();
  }
  /**
   * From JCheckBoxMenuItem
   * @param s
   */
  public void setText(String s) {
    component.setText(s);
  }
  /**
   * From JCheckBoxMenuItem
   */
  public void setActionCommand(String a) {
    component.setActionCommand(a);
  }
  /**
   * From JCheckBoxMenuItem
   * @return
   */
  public boolean isSelected() {
    return component.isSelected();
  }
  /**
   * From JCheckBoxMenuItem
   * @param b
   */
  public void setSelected(boolean b) {
    component.setSelected(b);
  }
  /**
   * From JCheckBoxMenuItem
   * @param icon
   */
  public void setIcon(Icon icon) {
    component.setIcon(icon);
  }
  /**
   * From JCheckBoxMenuItem
   * @param c
   */
  public void setMnemonic(char c) {
    component.setMnemonic(c);
  }
  /**
   * From JCheckBoxMenuItem
   * @param key
   */
  public void setAccelerator(KeyStroke key) {
    component.setAccelerator(key);
  }
  /**
   * Liefert die Swing-Komponente
   * @return
   */
  public JCheckBoxMenuItem getButton() {
    return component;
  }
  /**
   * Setzt den zu liefernden Rückgabewert der Checkbox.<p>
   * Es darf auch null übergeben werden.
   */
  public final void setMap(String p_trueValue, String p_falseValue) {
    this.trueValue = p_trueValue;
    this.falseValue = p_falseValue;
  }
  /**
   * Erwartet einen Vector von 2 Strings mit den Werten für true|false.
   */
  final void setMap(String[] vals) {
	  if (vals == null || vals.length == 0) return;
    this.trueValue = vals[0];
	  if (vals.length == 1) return;
    this.falseValue = vals[1];
  }
  // From MenuItemAble
  public MenuAble getGuiMenu() {
    return parentMenu;
  }
  public void setGuiMenu(MenuAble menu) {
    parentMenu = menu;
  }
}