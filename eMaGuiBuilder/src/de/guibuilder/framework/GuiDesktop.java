package de.guibuilder.framework;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
/**
 * DesktopPane für Panels.<p>
 * Im Constructor werden nur einige Eigenschaften gesetzt.
 * @see GuiInternalFrame
 */
public final class GuiDesktop extends JDesktopPane {  
   private static final long serialVersionUID = 1L;
// Constuctor
  /**
   * <code>this.putClientProperty("JDesktopPane.dragMode", "outline");<br>
   * this.setOpaque(false);</code>
   */
  public GuiDesktop() {
    super();
    this.putClientProperty("JDesktopPane.dragMode", "outline");
    this.setOpaque(false);
  }
  void dispose() {
    JInternalFrame[] myFrames = this.getAllFrames();
    for (int i = 0; i < myFrames.length; i++) {
      GuiInternalFrameImpl frm = (GuiInternalFrameImpl)myFrames[i];
      frm.removeAll();
      //frm.setGuiRootPane(null); geht nicht
      frm.dispose();
    }
    this.removeAll();
  }
}