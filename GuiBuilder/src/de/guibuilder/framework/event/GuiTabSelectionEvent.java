package de.guibuilder.framework.event;

import de.guibuilder.framework.GuiPanel;
import de.guibuilder.framework.GuiWindow;
/**
 * Dieses Ereignis wird ausgelöst, wenn eine andere Registerkarte angeklickt wurde.
 * @see GuiTabset
 * @see de.guibuilder.framework.GuiButtonBar
 * @see de.guibuilder.framework.GuiOutlookBar 
 * @author PKOEKER
 */
public final class GuiTabSelectionEvent extends GuiUserEvent {
  public GuiPanel component;
  public String actionCommand;
  public int tabIndex;

  public GuiTabSelectionEvent(GuiWindow win, GuiPanel comp, String actionCommand, int index) {
    super(win, comp);
    component = comp;
    this.actionCommand = actionCommand;
    this.tabIndex = index;
  }
  public final int getEventType() {
    return TAB;
  }
  public GuiPanel getComponent() {
     return this.component;
  }
}