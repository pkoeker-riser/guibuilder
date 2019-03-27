package de.guibuilder.framework;

import javax.swing.JApplet;

/**
 * Diese Ableiterei dient nur einem Zweck:
 * setRootPane ist in JApplet protected!
 * @see GuiApplet
 */
public class GuiAppletImpl extends JApplet {  
   private static final long serialVersionUID = 1L;

/**
   * Schiebt dem Fenster ein anderes RootPane unter.
   */
  final void setGuiRootPane(GuiRootPane root) {
    this.setRootPane(root);
  }
}