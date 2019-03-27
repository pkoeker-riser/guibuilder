package de.guibuilder.framework;

import javax.swing.JDialog;

/**
 * Diese Ableiterei dient nur einem Zweck:
 * setRootPane ist in JDialog protected!
 * @see GuiDialog
 */
public final class GuiDialogImpl extends JDialog {
   private static final long serialVersionUID = 1L;
// Constructors
  public GuiDialogImpl() {
    super();
    guiInit();
  }
  public GuiDialogImpl(String title) {
    super();
    this.setTitle(title);
    guiInit();
  }
  /**
   * Erzeugt einen Dialog mit einem Owner-Window.
   */
  GuiDialogImpl(GuiFormImpl owner) {
    super(owner);
    guiInit();
  }
  /**
   * Erzeugt einen Dialog mit einem Owner-Window und einem Titel
   */
  GuiDialogImpl(GuiFormImpl owner, String title) {
    super(owner, title);
    guiInit();
  }
  /**
   * Erzeugt einen Dialog mit einem Owner-Window.
   */
  GuiDialogImpl(GuiDialogImpl owner) {
    super(owner);
    guiInit();
  }
  /**
   * Erzeugt einen Dialog mit einem Owner-Window und einem Titel
   */
  GuiDialogImpl(GuiDialogImpl owner, String title) {
    super(owner, title);
    guiInit();
  }
  /**
   * Hide on Close
   */
  private void guiInit() {
    this.setDefaultCloseOperation(HIDE_ON_CLOSE);
  }
  /**
   * Schiebt dem Fenster ein anderes RootPane unter.
   */
  final void setGuiRootPane(GuiRootPane root) {
    this.setRootPane(root);
  }
}