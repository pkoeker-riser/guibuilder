package de.guibuilder.framework;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
/**
 * Focus-Adapter für Focus-Events.
 * @see GuiElement
 */
final class GuiFocusListener implements FocusListener {
  // Attributes
  private GuiElement element;
  // Constructor
  GuiFocusListener(GuiElement ele) {
    this.element = ele;
  }
  // Methods
  /**
   * @see GuiElement#gotFocus
   */
  public void focusGained(FocusEvent e) {
    element.gotFocus(e);
  }
  /**
   * @see GuiElement#lostFocus
   */
  public void focusLost(FocusEvent e) {
  	element.lostFocus(e);
  }
}