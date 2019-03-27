package de.guibuilder.framework;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

/**
 * Implementierung eines ToogleButtons<p>
 * Sieht aus wie ein Button, benimmt sich aber wie eine Checkbox.
 * Mit setSelectedIcon wird der Icon gesetzt der erscheint, wenn
 * det Button "gedrückt" ist.<br>
 * Alle Methoden sind final.
 */
public final class GuiTButton extends GuiComponent {
  // Attributes
  private JToggleButton component;
  /**
   * Kennzeichnet das Verhalten von get und setValue als "verdreht":
   * Aus true wird false und aus false wird true.
   */
  private boolean invert;
  // Constructors
  /**
   * Erzeugt einen beschrifteten Toggle-Button.
   */
  public GuiTButton(String label) {
    super(label);
    this.setText(label);
    if (label.length() == 0) {
      this.setName("NoNameTButton");
    } else {
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
    }
    this.guiInit();
  }
  /**
   * Erzeugt eine unbeschriftete CheckBox mit dem Namen "check".<BR>
   * @see GuiTable.GuiTableCheckRenderer
   */
  public GuiTButton() {
    super("tbutton");
    this.guiInit();
  }
  // Methods
  /**
  Initialisierung der Komponente (FocusListener, ActionListener).
  */
  private void guiInit() {
    this.addFocusListener(new GuiFocusListener(this));
    component.addActionListener(new GuiChangeListener(this));
  }
  public final String getTag() {
    return "TButton";
  }
  public final JComponent getJComponent() {
    if (component == null) component = new JToggleButton();
    return component;
  }
  public final int getDataType() {
    return BOOLEAN;
  }
  /**
  Setzt den Inhalt der Komponente.
  Es muß ein Boolean oder ein String (mit dem Inhalt "true" oder "false") übergeben werden.
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
  /**
  Setzt den Inhalt der Komponente.
  Es muß ein String mit dem Inhalt "true" oder "false" übergeben werden.
  */
  /*
  public final void setValue(String val) {
    Boolean b = new Boolean(val);
    if (invert == true) component.setSelected(!b.booleanValue());
    else component.setSelected(b.booleanValue());
    this.setModified(false);
  }
  */
  
  public Object getUnformatedValue()
  {
	return getValue();
  }
  
  /**
  Liefert ein Boolean.
  */
  public final Object getValue() {
    Boolean val;
    if (invert == true) val = Boolean.valueOf(!component.isSelected());
    else val = Boolean.valueOf(component.isSelected());
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
  final void setSelected(boolean b) {
    component.setSelected(b);
  }
  final void setIcon(Icon icon) {
    component.setIcon(icon);
  }
  final void setSelectedIcon(Icon icon) {
    component.setSelectedIcon(icon);
  }
  final void setText(String s) {
    component.setText(s);
  }
}