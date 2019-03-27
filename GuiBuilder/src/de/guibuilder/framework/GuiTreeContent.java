package de.guibuilder.framework;

/**
Entspricht dem Keyword "Content".
@see GuiTree
@see GuiTreeElement
*/
public final class GuiTreeContent implements java.io.Serializable {
  // Attributes
  private String name;
  private boolean mandatory = false;
  private boolean multi = true;
  // Constructors
  public GuiTreeContent(String name) {
    this.name=name;
  }
  /*
  GuiTreeContent(String name, boolean nn, boolean multi) {
          this.name=name;
          this.mandatory=nn;
          this.multi=multi;
  }
  */
  // Methods
  public String getName() {
    return name;
  }
  public void setName(String s) {
    this.name = s;
  }
  public boolean isMandatory() {
    return mandatory;
  }
  public void setMandatory(boolean b) {
    this.mandatory = b;
  }
  public boolean isMulti() {
    return multi;
  }
  public void setMulti(boolean b) {
    this.multi = b;
  }
}