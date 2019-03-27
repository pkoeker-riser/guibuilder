package de.guibuilder.framework.event;

import java.awt.event.FocusEvent;

import de.guibuilder.framework.GuiElement;
import de.guibuilder.framework.GuiWindow;

/**
 * Es ist ein Lost-Focus-Ereignis aufgetreten.
 * @since 0.9.3f
 */

public final class GuiLostFocusEvent extends GuiUserEvent {
  /**
   * Die Komponente, die das Ereignis ausgelöst hat..
   */
  public transient GuiElement component;
  /**
   * Der Inhalt der Komponente.
   * @see de.guibuilder.framework.GuiComponent#getDataType
   */
  public Object value;
  public boolean bValue;
  public FocusEvent focusEvent;

  public GuiLostFocusEvent(GuiWindow win, GuiElement comp, Object val, FocusEvent e) {
    super(win, comp);
    component = comp;
    value = val;
    if (value != null) {
      bValue = Boolean.valueOf(val.toString()).booleanValue();
    }
    focusEvent = e;
  }
  public final int getEventType() {
    return LOST_FOCUS;
  }
  public GuiElement getComponent() {
     return this.component;
  }
  public Object getValue() {
     return this.value;
  }
}