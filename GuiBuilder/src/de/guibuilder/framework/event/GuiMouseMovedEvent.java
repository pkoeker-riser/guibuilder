package de.guibuilder.framework.event;

import java.awt.event.MouseEvent;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiWindow;

/**
 * Wird ausgelöst, wenn die Maus über einem Objekt bewegt wird (isDrag = false)
 * oder DragDrop vorliegt (isDrag = true).
 * @author Peter Köker
 * @since 0.9.3g
*/
public final class GuiMouseMovedEvent extends GuiUserEvent {
  public boolean isDrag;
  public MouseEvent mouseEvent;

  public GuiMouseMovedEvent(GuiWindow win, GuiMember comp, MouseEvent me, boolean isDrag) {
    super(win, comp);
    this.member = comp;
    this.mouseEvent = me;
    this.isDrag = isDrag;
  }
  public final int getEventType() {
    return MOUSE_MOVED;
  }
  public boolean isDrag() {
     return this.isDrag;
  }
}