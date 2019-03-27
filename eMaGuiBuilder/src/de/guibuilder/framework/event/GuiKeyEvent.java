package de.guibuilder.framework.event;

import java.awt.event.KeyEvent;

import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiWindow;

/**
 * Es ist ein Key-Typed-Ereignis aufgetreten.
 * @author Peter Köker
 * @since 0.9.3g
 */

public final class GuiKeyEvent extends GuiUserEvent {
  /**
   * Die Komponente, die das Ereignis ausgelöst hat..
   */
  public transient GuiComponent component;
  /**
   * Der Inhalt der Komponente.
   * @see de.guibuilder.framework.GuiComponent#getDataType
   */
  public Object value;
  /**
   * awt.KeyEvent
   */
  public KeyEvent keyEvent;

  public GuiKeyEvent(GuiWindow win, GuiComponent comp, Object val, KeyEvent e) {
    super(win, comp);
    component = comp;
    value = val;
    keyEvent = e;
  }
  public final int getEventType() {
    return KEY;
  }
  public GuiComponent getComponent() {
     return this.component;
  }
}