package de.guibuilder.framework.event;

import java.awt.event.WindowEvent;

import de.guibuilder.framework.GuiWindow;

/**
 * Title:        GuiBuilder
 * Description:
 * Copyright:    Copyright (c) 2001
 * @author Peter Köker
 * @since 0.9.3d
 */
/**
 * Fensterereignisse.
 */
public final class GuiWindowEvent extends GuiUserEvent {
  /**
   * OnClose=
   */
  public static final int CLOSE = 0;
  /**
   * OnOpen=
   */
  public static final int OPEN = 1;
  /**
   * OnActive=
   */
  public static final int ACTIVE = 2;
  /**
   * Entweder CLOSE, OPEN oder ACTIVE
   */
  public int eventSubType;
  /**
   * awt.WindowEvent
   */
  public WindowEvent windowEvent;
  /**
   * Erzeugt ein Window-Event
   * @param type CLOSE, OPEN oder ACTIVE
   * @param e Das awt.WindowEvent
   */
  public GuiWindowEvent(GuiWindow win, int type, WindowEvent e) {
    super(win, null);
    eventSubType = type;
    windowEvent = e;
  }
  public final int getEventType() {
    return WINDOW;
  }
}