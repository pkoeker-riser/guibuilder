package de.guibuilder.framework;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Allgemeiner ChangeListener für Check und Option, sowie MenuItemCheck und
 * MenuItemOption.<br>
 * Es wird obj_ItemChanged von GuiComponent anfgerufen; letztere reicht
 * den Aufruf an GuiRootPane weiter.<br>
 * @since 0.9.3d
 * @see GuiComponent#obj_ItemChanged
 * @see GuiRootPane#obj_ItemChanged
 */
final class GuiChangeListener implements ActionListener {
  // Attributes
  /**
   * Handle auf GuiComponent, welches die Aktion auslöst.
   */
  private GuiComponent comp;
  // Constructor
  /**
   * Konstruktor mit Angabe der Komponente, die die Aktion auslöst.
   */
  GuiChangeListener(GuiComponent c) {
    comp = c;
  }
  // Methods
  /**
   * @see GuiComponent#obj_ItemChanged
   * @see GuiRootPane#obj_ItemChanged
   */
  public void actionPerformed(ActionEvent e) {
    comp.obj_ItemChanged(e);
  }
}