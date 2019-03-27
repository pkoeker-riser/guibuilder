package de.guibuilder.framework;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Mouse-Adapter für MouseMotionEvents.
 * <UL>
 * <li>MouseMove
 * <li>Drag
 * </ul>
 * @see GuiMember#addMouseMotionListener
 */
final class GuiMouseMotionListener implements MouseMotionListener {
  // Attributes
  private GuiMember member;
  // Constructor
  /**
   * MouseListener für Komponenten
   */
  GuiMouseMotionListener(GuiMember m) {
    this.member = m;
  }
  // Methods
  public void mouseMoved(MouseEvent e) {
    member.mouseMoved(e, false);
  }
  public void mouseDragged(MouseEvent e) {
    member.mouseMoved(e, true);
  }
}