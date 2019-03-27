package de.guibuilder.framework;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

/**
 * Implementierung einer CheckBox.<p>
 * Alle Methoden sind final.
 */
public final class GuiCheck extends GuiComponent implements TableColumnAble {
   static final long serialVersionUID = -5945711881127406750L;
  // Attributes
  private JCheckBox component;
  /**
   * Kennzeichnet das Verhalten von get und setValue als "verdreht":
   * Aus true wird false und aus false wird true.
   */
  private boolean invert;
  private String trueValue;
  private String falseValue;
  // Constructors
  /**
   * Erzeugt eine beschriftete CheckBox.<br>
   * Das Label wird gleichzeitig als Name der Komponente verwendet.
   * Ein Mnemonic Char kann definiert werden mit vorangestelltem "%".<br>
   * Es wir ein FocusListener und ein ActionListener gesetzt.
   */
  public GuiCheck(String label) {
    super(label);
    this.setText(label);
    char mnemo;
    int p = label.indexOf("%");
    if (p != -1 && p+1 < label.length()) {
      mnemo = label.charAt(p+1);
      label = label.substring(0,p) + label.substring(p+1);
      this.setText(label);
      if (mnemo != '%') {
        this.setMnemonic(mnemo);
        this.component.setDisplayedMnemonicIndex(p);
      }
    }
    this.guiInit();
  }
  /**
   * Erzeugt eine unbeschriftete CheckBox mit dem Namen "check".<BR>
   * @see GuiTable.GuiTableCheckRenderer
   */
  public GuiCheck() {
    super("check");
    this.guiInit();
  }
  // Methods
  /**
   * Initialisierung der Komponente (FocusListener, ActionListener).
   */
  private void guiInit() {
    this.addFocusListener(new GuiFocusListener(this));
    component.addActionListener(new GuiChangeListener(this));
  }
  public final String getTag() {
    return "Check";
  }
  public final JComponent getJComponent() {
    if (component == null) component = new JCheckBox();
    return component;
  }
  /**
   * Liefert die JCheckBox aus javax.swing
   * @return
   */
  public final JCheckBox getButton() {
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
      if (s.equals("y") || s.equals("1")) {
        b = Boolean.valueOf(true);
      } else {
        b = Boolean.valueOf(s);
      }
      if (s.equals(trueValue)) {
        b = Boolean.valueOf(true);
      }
      else if (s.equals(falseValue)) {
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
  public final void setValue(boolean b) {
		if (invert == true) {
		  component.setSelected(!b);
		} else {
		  component.setSelected(b);
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
   * Kennzeichnet das Verhalten von get und setValue als "verdreht":
   * Aus true wird false und aus false wird true.
   */
  public final void setInvert(boolean b) {
    invert = b;
  }
  public final void reset() {
    component.setSelected(false);
    this.setModified(false);
  }
  final void setHorizontalAlignment(int i) {
    component.setHorizontalAlignment(i);
  }
  public final void setMnemonic(char c) {
    component.setMnemonic(c);
  }
  public boolean isSelected() {
    return component.isSelected();
  }
  final void setSelected(boolean b) {
    component.setSelected(b);
  }
  final void setIcon(Icon icon) {
    component.setIcon(icon);
  }
  public final void setText(String s) {
    component.setText(s);
  }
  /**
   * LEFT | RIGHT
   * @param posi
   */
  public void setHorizontalTextPosition(int posi) {
    component.setHorizontalTextPosition(posi);
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
   * Erwartet einen String[] von 2 Einträgen mit den Werten für true|false.
   */
  final void setMap(String[] items) {
	  if (items.length == 0) return;
	  this.trueValue = items[0];
	  if (items.length == 1) return;
	  this.falseValue = items[1];
  }
  public Class getValueClass() {
    return Boolean.class;
  }
}