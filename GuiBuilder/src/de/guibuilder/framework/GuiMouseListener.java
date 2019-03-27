package de.guibuilder.framework;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Mouse-Adapter für MouseEvents.
 * <UL>
 * <li>Click
 * <li>Double Click
 * <li>Mouse Over
 * <li>Popup Trigger
 * </ul>
 * @see GuiMember#addMouseListener
 */
final class GuiMouseListener implements MouseListener {
  // Attributes
  private GuiMember member;
  // Constructor
  /**
   * MouseListener für Komponenten
   */
  GuiMouseListener(GuiMember m) {
    this.member = m;
  }
  // Methods
  /**
   * Es wird die Methode click oder d_click der Komponente aufgerufen.
   * @see GuiMember#click
   * @see GuiMember#d_click
   */
  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() == 1) {
      member.click(e);
    }
    else if (e.getClickCount() == 2) {
      member.d_click(e);
    }
  }
  /**
   * Wird an GuiMember weiter gereicht.
   * @see GuiMember#mouseOver
   */
  public void mouseEntered(MouseEvent e) {
    member.mouseOver(e, true);
  }
  /**
   * Wird an GuiMember weiter gereicht.
   * @see GuiMember#mouseOver
   */
  public void mouseExited(MouseEvent e) {
    member.mouseOver(e, false);
  }
  public void mousePressed(MouseEvent e) {
    showPopup(e);
  }

  public void mouseReleased(MouseEvent e) {
    showPopup(e);
  }
  /**
   * Zeigt das PopupMenu.
   * @see GuiMember#showPopupMenu
   */
  private void showPopup(MouseEvent e) {
    if (e.isPopupTrigger()) {
      int modifiers = e.getModifiersEx();
      if ((modifiers & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
        member.showPopupMenu(InputEvent.CTRL_DOWN_MASK, e.getX(), e.getY());
      } else {
        member.showPopupMenu(e.getX(), e.getY());
      }
    }
  }
}