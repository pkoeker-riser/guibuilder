package de.guibuilder.framework;

import javax.swing.JFrame;

/**
 * Diese Ableiterei dient nur einem Zweck:
 * setRootPane ist in JFrame protected!
 * @see GuiForm
 */
public final class GuiFormImpl extends JFrame {
  /**
   * Schiebt dem Fenster ein anderes RootPane unter.
   */
  final void setGuiRootPane(GuiRootPane root) {
    this.setRootPane(root);
  }
}