package de.guibuilder.framework;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

/**
 * Implementierung einer Password Eingabe.
 * @since 0.8.2a
 */
public final class GuiPassword extends GuiComponent {
  // Attributes
  private JPasswordField component;
  private char oldValue[];
  // Constructor
  public GuiPassword() {
    super();
    this.guiInit();
  }
  public GuiPassword(String label) {
    super(label);
    this.guiInit();
  }
  // Methods
  private void guiInit() {
    // Taste "Enter" für DefaultButton nicht mehr auffuttern!
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    Keymap map = getPassword().getKeymap();
    map.removeKeyStrokeBinding(enter);
    // FocusListener
    this.addFocusListener(new GuiFocusListener(this));
  }
  public final String getTag() {
    return "Password";
  }
  public JComponent getJComponent() {
    if (component == null) component = new JPasswordField();
    return component;
  }
  private JPasswordField getPassword() {
    return (JPasswordField)getJComponent();
  }
  public final int getDataType() {
    return STRING;
  }
  // From GuiComponent
  public void lostFocus(FocusEvent e) {
    super.lostFocus(e); // GuiComponent --> GuiElement
    if (e.isTemporary() == false) {
    	this.postProc();
    	if (Arrays.equals(this.getPassword().getPassword(), this.getOldValue())) {
        this.setModified(true);
        if (actionChange != null && getRootPane() != null) {
          getRootPane().obj_ItemChanged(this, actionChange, this.getValue());
        }
      }
      this.setOldValue(this.getPassword().getPassword());
    }
  }
  /**
   * Setzt den Inhalt der Componente NICHT!
   */
  public void setValue(Object val) {
    //this.setText((String)val);
    //oldValue = (String)val;
    //this.setModified(false);
  }
  
  public Object getUnformatedValue() {
    return new String(component.getPassword());
  }
  
  /**
   * Liefert den Inhalt der Componente als String.
   */
  public Object getValue() {
    return new String(component.getPassword());
  }
  public void reset() {
    component.setText(null);
    this.setModified(false);
  }
  public void setHorizontalAlignment(int i) {
    component.setHorizontalAlignment(i);
  }
  public int getHorizontalAlignment() {
    return component.getHorizontalAlignment();
  }
  public void setColumns(int i) {
    component.setColumns(i);
  }
  private char[] getOldValue() {
    return oldValue;
  }
  private void setOldValue(char[] v) {
    oldValue = v;
  }
}