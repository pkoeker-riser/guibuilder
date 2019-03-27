package de.guibuilder.framework;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dieser Adapter wird von Formularen und Dialogen für das öffnen, Schließen und Aktivieren
 * des Fensters verwendet.
 * $Id: GuiWindowListener.java,v 1.2 2017/03/29 09:55:32 pkoeker Exp $
 * $Log: GuiWindowListener.java,v $
 * Revision 1.2  2017/03/29 09:55:32  pkoeker
 * *** empty log message ***
 *
 * Revision 1.6  2012/11/17 22:18:31  pkoeker
 * UTF-8
 *
 * Revision 1.5  2012/11/09 18:47:23  pkoeker
 * UTF-8
 *
 * Revision 1.4  2007/10/28 11:06:45  pkoeker
 * Alle Events weiterreichen
 *
 * @see GuiWindow
 */
final class GuiWindowListener extends WindowAdapter {
  // Attributes
  private GuiWindow adaptee;
  // Constructor
  GuiWindowListener(GuiWindow adaptee) {
    this.adaptee = adaptee;
  }
  // Methods
  /**
   * Delegate
   * @see GuiWindow#obj_windowEvent
   */
  public void windowOpened(WindowEvent e) {
    adaptee.obj_windowEvent(e);
  }
  /**
   * Delegate
   * @see GuiWindow#obj_windowEvent
   */
  public void windowClosing(WindowEvent e) {
    adaptee.obj_windowEvent(e);
  }
  /**
   * Delegate<p>
   * Setzt CurrentWindow in GuiSession
   * @see GuiWindow#obj_windowEvent
   * @see GuiSession#setCurrentWindow
   */
  public void windowActivated(WindowEvent e) {
    GuiSession.getInstance().setCurrentWindow(adaptee);
    adaptee.obj_windowEvent(e);
  }
  public void windowClosed(WindowEvent e) {
    adaptee.obj_windowEvent(e);
  }
  public void windowDeactivated(WindowEvent e) {
    adaptee.obj_windowEvent(e);
  }
  public void windowGainedFocus(WindowEvent e) {
    adaptee.obj_windowEvent(e);
  }
  public void windowIconified(WindowEvent e) {
    adaptee.obj_windowEvent(e);
  }
  public void windowStateChanged(WindowEvent e) {
    adaptee.obj_windowEvent(e);
  }
}