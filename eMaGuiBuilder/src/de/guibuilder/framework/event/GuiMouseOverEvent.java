package de.guibuilder.framework.event;

import java.awt.event.MouseEvent;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiWindow;

/**
 * Wird ausgelöst, wenn die Maus über einem Objekt erscheint (isOver = true)
 * oder es wieder verläßt (isOver = false).
 * @author Peter Köker
 * @since 0.9.3f
*/
public final class GuiMouseOverEvent extends GuiUserEvent {
  public boolean isOver;
  public MouseEvent mouseEvent;
  // Constructor
  public GuiMouseOverEvent(GuiWindow win, GuiMember comp, MouseEvent me, boolean isOver) {
    super(win, comp);
    this.member = comp;
    this.mouseEvent = me;
    this.isOver = isOver;
  }
  public final int getEventType() {
    return MOUSE_OVER;
  }
  public boolean isOver() {
     return this.isOver;
  }
}