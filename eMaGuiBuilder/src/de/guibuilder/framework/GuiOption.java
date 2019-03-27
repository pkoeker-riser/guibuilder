package de.guibuilder.framework;

import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

/**
 * Implementierung eines RadioButtons.<p>
 * Die Beschriftung des Buttons wird gleichzeitig als sein Name verwendet,
 * sowie als ActionCommand.<br>
 * Der Name des ersten RadioButtons wird als Membername verwendet.
 * Unter diesem Namen wird mit getValue() das ActionCommand des selektierten
 * Buttons ausgelesen, bzw. kann mit setValue() der Button mit dem
 * entsprechenden ActionCommand selektiert werden.
 * @see GuiOptionGroup
 */
public final class GuiOption extends GuiComponent implements OptionAble	{
  // Attributes
  /**
   * JRadioButton
   */
  private JRadioButton component;
  /**
   * Verweis auf die OptionGroup zum dem dieser RadioButton gehört.
   */
  private GuiOptionGroup optionGroup;
  // Constructors
  /**
   * Erzeugt einen RadioButton mit der angegebenen Beschriftung.<p>
   * Enthält die Beschriftung das Zeichen "%" wird der folgende Buchstabe
   * als Mnemonic Char verwendet.
   */
  public GuiOption(String title) {
    super(title);
    this.setText(title);
    // Mnemo
    char mnemo;
    int p = title.indexOf("%");
    if (p != -1 && p+1 < title.length()) {
      mnemo = title.charAt(p+1);
      title = title.substring(0,p) + title.substring(p+1);
      this.setText(title);
      if (mnemo != '%') {
        this.setMnemonic(mnemo);
        this.component.setDisplayedMnemonicIndex(p);
      }
    }
    this.addFocusListener(new GuiFocusListener(this));
    component.addActionListener(new GuiChangeListener(this));
  }
  /**
  * Erstellt einen Radiobutton und verknüft ihn mit eine OptionGroup.
  */
  public GuiOption(GuiOptionGroup og, String title) {
    this(title);
    this.setOptionGroup(og);
  }
  // Methods
  /**
   * Liefert "Option"
   */
  public final String getTag() {
    return "Option";
  }
  /**
   * Liefert JRadioButton
   */
  public JComponent getJComponent() {
    if (component == null) component = new JRadioButton();
    return component;
  }
  /**
   * Liefert STRING
   */
  public final int getDataType() {
    return STRING;
  }
  /**
   * Setzt die OptionGroup für diesen RadioButton.
   */
  public void setOptionGroup(GuiOptionGroup grp) {
    optionGroup = grp;
  }
  /**
   * Wird an OptionGroup delegiert
   * @see GuiOptionGroup#updateValue
   */
  void obj_ItemChanged(ActionEvent e) {
    setModified(true);
    // Inhalt weiterreichen an OptionGroup
    if (component.isSelected() == true && optionGroup != null) {
      optionGroup.updateValue(this, this.getActionCommand());
    }
  }
  /**
   * Selektiert den Radiobutton, wenn sein ActionCommand übergeben wird;
   * mit null wird der RadioButoon deselektiert.
   */
  public void setValue(Object val) {
    if (val == null) {	// neu
      this.setSelected(false);
    } else if (val.toString().equals(this.getActionCommand())) {
      this.setSelected(true);
    }
    this.setModified(false);
  }
  public final void setValue(boolean b) {
		component.setSelected(b);
		this.setModified(false);
  }
  
  public Object getUnformatedValue()
  {
	return getValue();	
  }
  
  /**
   * Liefert den Wert, den diese Option repräsentiert, wenn sie selektiert ist;
   * ansonsten null.
   * Der ReturnWert wird mit setActionCommand gesetzt.
   * @see #setActionCommand
   */
  public Object getValue() {
    if (component.isSelected() == true) {
      return this.getActionCommand();
    }
    else {
      return null;
    }
  }
  /**
   * setSelected(false); setModified(false)
   */
  public void reset() {
    this.setSelected(false);
    this.setModified(false);
  }
  public void setHorizontalAlignment(int i) {
    component.setHorizontalAlignment(i);
  }
  public void setMnemonic(char c) {
    component.setMnemonic(c);
  }
  /**
   * (De-)Selektiert den Radiobutton.
   */
  public void setSelected(boolean b) {
    // TODO assert optionGroup != null;
    component.setSelected(b);
    // Inhalt weiterreichen an OptionGroup
    if (component.isSelected() == true && optionGroup != null) {
      optionGroup.updateValue(this, this.getActionCommand());
    }
  }
  public void setIcon(Icon icon) {
    component.setIcon(icon);
  }
  /**
   * Setzt die Beschriftung des RadioButtons.
   */
  public void setText(String s) {
    component.setText(s);
  }
  /**
   * Liefert die Beschriftung des RadioButtons.
   */
  public String getText() {
    return component.getText();
  }
  public String getActionCommand() {
    return component.getActionCommand();
  }
  /**
   * Setzt das ActionCommand; dieses ist der Wert der von getValue geliefert wird,
   * wenn der RadioButton selektiert ist.
   * @see #getValue
   */
  public void setActionCommand(String s) {
    component.setActionCommand(s);
  }
  /**
   * Liefert die SwingKomponente (Delegation).
   */
  public AbstractButton getButton() {
    return component;
  }
}