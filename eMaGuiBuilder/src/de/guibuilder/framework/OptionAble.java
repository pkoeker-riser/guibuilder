package de.guibuilder.framework;

import javax.swing.AbstractButton;
/**
* Vereinheitlich die Eigenschaften von Option und ItemOption.
* @see GuiOptionGroup
*/
public interface OptionAble {
  public void setOptionGroup(GuiOptionGroup og);
  public void setGuiParent(GuiContainer cont);
  public void setSelected(boolean b);
  public AbstractButton getButton();
  public GuiRootPane getRootPane();
  public String getName();
  public String getMsgChange();
  public String getActionCommand();
}