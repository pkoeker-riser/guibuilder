package de.guibuilder.framework;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
/**
 * Dieser Button kann mit einer - plattformspezifischen - Datei verknüpft werden.
 * Außerdem kann eine Dokumentvorlage definiert werden.
 */
public final class GuiDocument extends GuiComponent {
  // Attributes
  protected JButton component = new JButton();
  private String template;
  private Object value;
  // Constructor
  public GuiDocument(String label) {
    super(label);
    char mnemo;
    if (label != null) {
      component.setText(label);
      int p = label.indexOf("%");
      if (p != -1 && p + 1 < label.length()) {
         mnemo = label.charAt(p + 1);
         label = label.substring(0, p) + label.substring(p + 1);
         component.setText(label);
         if (mnemo != '%') {
            this.setMnemonic(mnemo);
            this.component.setDisplayedMnemonicIndex(p);
         }
      }
    }
    guiInit(label);
  }
  // Methods
  /**
   * Es wird der ActionListener gesetzt, und setDefaultCapable(false).
   */
  private void guiInit(String label) {
    component.setName(GuiUtil.labelToName(label));
    component.setDefaultCapable(false);
    component.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (value == null) {
          try {
            value = GuiUtil.createDocument(template, component.getText());
          } catch (Exception ex) {
            GuiUtil.showEx(ex);
            return;
          }
        }
        GuiUtil.showDocument(value.toString());
      }
    });
    // Endlich verstanden!
    component.setDefaultCapable(false);
  }
  public JComponent getJComponent() {
    if (component == null) component = new JButton();
    return component;
  }
  /**
   * Liefert STRING; wird von Date,Time.Money,Number überschrieben.
   * From GuiComponent
   */
  public int getDataType() {
    return STRING;
  }
  public String getTag() {
    return "Document";
  }
  
  public Object getUnformatedValue()
  {
	return value;	
  }
  
  public Object getValue() {
    return value;
  }
  
  public void setValue(Object s) {
    this.value = s;
  }
  public void reset() {
    value = null;
  }
  /**
   * Setzt den Dateinamen der DokumentVorlage.
   * Siehe Attribut type=
   */
  public void setTemplate(String t) {
    this.template = t;
  }
  public String getTemplate() {
    return template;
  }
  /**
   * From swing.AbstractButton
   */
  public final void setHorizontalAlignment(int i) {
    component.setHorizontalAlignment(i);
  }
  /**
   * From swing.AbstractButton
   */
  public final void setIcon(Icon icon) {
    component.setIcon(icon);
  }
  /**
   * From swing.AbstractButton
   */
  public final void setMnemonic(char c) {
    component.setMnemonic(c);
  }
}