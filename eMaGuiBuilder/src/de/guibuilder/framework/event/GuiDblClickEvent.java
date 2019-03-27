package de.guibuilder.framework.event;

import java.awt.event.MouseEvent;

import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiWindow;

/**
 * Title:        GuiBuilder
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author Peter Köker
 * @version 0.9.3d
 */

public final class GuiDblClickEvent extends GuiChangeEvent {
  public MouseEvent mouseEvent;

  public GuiDblClickEvent(GuiWindow win, GuiComponent comp, Object val, MouseEvent me) {
    super(win, comp, val);
    this.mouseEvent = me;
  }
  public final int getEventType() {
    return DBL_CLICK;
  }
}