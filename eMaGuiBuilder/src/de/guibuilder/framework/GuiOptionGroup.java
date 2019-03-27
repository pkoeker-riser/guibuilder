package de.guibuilder.framework;

import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;

/**
 * Ein Container für RadioButtons.<p>
 * GuiContainer und GuiMenu können genau einen Satz von RadioButton halten.
 * Dieser Satz repräsentiert dann genau den Wert, der dem ActionCommand
 * des selektierten RadioButtons entspricht.<p>
 * Diese OptionGroup wird erst dann wirklich erzeugt, wenn dem
 * Container die erste Option zugewiesen wird.
 * @see GuiOption
 * @see GuiMenu#addOption
*/
public final class GuiOptionGroup extends GuiComponent  {
  // Attributes
  private ButtonGroup component = new ButtonGroup();
  /**
   * für Attribut name= da ButtonGroup leider keinen Namen hat :-(
   */
  private String name;
  private Object value;
  // Constructor
  /**
   * Erzeugt eine OptionGroup unter Angabe ihres Namens.<p>
   * Die Factory verwendet den Namen des ersten RadioButtons.
   */
  public GuiOptionGroup(String name) {
    super(name); 
    this.setName(name);
  }
  // Methods
  /**
   * Liefert "Option"
   */
  public final String getTag() {
    return "Option";
  }
  /**
   * Liefert hier immer null, da ButtonGroup von Object abgeleitet ist.
   */
  public JComponent getJComponent() {
    return null;
  }
  /**
   * Liefert GUI_COMPONENT
   */
  public int getGuiType() {
    return GUI_COMPONENT;
  }
  /**
   * Liefert STRING
   */
  public final int getDataType() {
    return STRING;
  }
  /**
   * Von GuiMember überschrieben.
   */
  public void setName(String name) {
    this.name = name;
  }
  /**
   * Von GuiMember überschrieben.
   */
  public String getName() {
    return name;
  }
  /**
   * RootPane wird vom GuiParent übernommen.
   */
  public GuiRootPane getRootPane() {
    return this.getGuiParent().getRootPane();
  }
  /**
   * Fügt einen RadioButton hinzu.
   * @see GuiOption
   * @see GuiMenuItemOption
   */
  //public void add(AbstractButton opt) {
  public void add(OptionAble opt) {
    component.add(opt.getButton());
    opt.setOptionGroup(this);
    opt.setGuiParent(this.getGuiParent());
    if (opt.getActionCommand().equals(this.value)) {
      opt.setSelected(true);
    }
  }
  /**
   * Wird vom jeweiligen RadioButton aufgerufen, wenn er gesetzt wird.
   */
  void updateValue(GuiComponent comp, String val) {
    setModified(true);
    this.value = val;
    // Ref weiterreichen
    if (comp.getRef() != null) {
      this.setRef(comp.getRef() );
    }
    // Inhalt an verknüpfte Spalte weiterreichen?
    if (linkTable != null) {
      linkTable.setCellValue(linkColumn, this.getValue());
    }
    if (actionChange != null) {
      this.getRootPane().obj_ItemChanged(this, actionChange, this.value);
    }
  }
  /**
   * Es wird der RadioButten selektiert, der den übergebenen Wert als ActionCommand hält.
   */
  public void setValue(Object val) {
    this.value = val;
    // Radiobutton ermitteln, der diesen Wert hat.
    for (Enumeration<AbstractButton> e = this.component.getElements(); e.hasMoreElements();) {
      AbstractButton opt = e.nextElement();
      if (opt.getActionCommand().equals(value)) {
        opt.setSelected(true);
        this.setModified(false);
      } else {
        opt.setSelected(false);
      }
    } // End For Elements
  }
  
  public Object getUnformatedValue()
  {
	return value;	
  }
  
  /**
   * Liefert das ActionCommand des selektierten Buttons (String).
   */
  public Object getValue() {
    return value;
  }
  /**
   * Es werden alle RadioButtons deselektiert.
   * TODO :  Funzt nicht mehr!
   */
  public void reset() {
    // Das geht leider nicht mehr!  This Bug was a Feature!
    component.setSelected(null, true); // führt dazu, daß kein RadioButton selected ist
    // Geht alles nicht!
    //component.setSelected(component.getSelection(), true);
    //component.getSelection().setSelected(false);
    /*
    for (Enumeration e = this.component.getElements(); e.hasMoreElements();) {
      AbstractButton ab = (AbstractButton)e.nextElement();
      ab.setSelected(false);
    }
    */
    this.setModified(false);
  }
  /**
   * (De-)Aktiviert alle RadioButtons dieser ButtonGroup.
   * überschriebn von GuiElement
   * from awt.Component
   */
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    for (Enumeration<AbstractButton> e = this.component.getElements(); e.hasMoreElements();) {
      AbstractButton ab = e.nextElement();
      ab.setEnabled(b);
    }
  }
}