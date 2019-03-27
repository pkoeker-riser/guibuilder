package de.guibuilder.framework;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Allgemeiner Actionlistener für Buttons, MenuItems.
 * Es wird obj_actionPerformed von GuiAction aufgerufen; letztere reicht
 * den Aufruf an GuiRootPane weiter.<br>
 * @see GuiAction#obj_actionPerformed
 * @see GuiRootPane#obj_actionPerformed
 */
final class GuiActionListener implements ActionListener {
  // Attributes
  /**
   * Handle auf GuiElement, welches die Aktion auslöst.<br>
   * Ist entweder von GuiAction oder von GuiComponent abgeleitet.
   */
  private GuiAction action;
  // Constructor
  /**
   * Konstruktor mit Angabe der Komponente, die die Aktion auslöst.
   */
  GuiActionListener(GuiAction a) {
    action = a;
  }
  // Methods
  /**
   * @see GuiAction#obj_actionPerformed
   * @see GuiRootPane#obj_actionPerformed
   */
  public void actionPerformed(ActionEvent e) {
    GuiInvokationResult result = action.obj_actionPerformed(e);
    if (result != null) {
      // TODO?
    }
  }
}