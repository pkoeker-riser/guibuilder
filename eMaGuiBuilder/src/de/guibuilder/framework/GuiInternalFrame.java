package de.guibuilder.framework;

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;

/**
 * Implementierung eines internen verschiebbaren Fensters ähnlich wie MS MDI.
 */
public class GuiInternalFrame extends GuiWindow {
  // Attributes
  private GuiInternalFrameImpl component;
  // Constructors
  public GuiInternalFrame () {
    super("internalFrame");
    component = new GuiInternalFrameImpl("");
    this.guiInit();
  }
  public GuiInternalFrame (String title) {
    super(title);
    component = new GuiInternalFrameImpl(title);
    this.setTitle(title);
    this.guiInit();
  }
  // Methods
  private void guiInit() {
    component.setGuiRootPane(new GuiRootPane(this));
    component.setSize(300,300); // default-Size; sonst unsichtbar!
  }
  public final int getGuiType() {
    return INTERNAL;
  }
  public final String getTag() {
    return "Frame";
  }
  public final Container getComponent() {
    return component;
  }
  public Window getWindow() {
  	return null;
  }

  public GuiInternalFrameImpl getInternalFrame() {
    return component;
  }
  public Component getAwtComponent() {
	  return this.getComponent();
  }
  /**
   * Schiebt dem Fenster ein anderes RootPane unter.
   */
  public final void setRootPane(GuiRootPane root) {
    component.setGuiRootPane(root);
  }
  /**
   * Liefert GuiRootPane
   */
  public final GuiRootPane getRootPane() {
    return (GuiRootPane)component.getRootPane();
  }
  // dummy
  public final void setIconImage(Image img) {
  	if (component != null) component.setFrameIcon(new ImageIcon(img));
  }
  // geht das gut??
  public final Image getIconImage() {
    return (Image)component.getFrameIcon();
  }
  /**
   * Liefert hier null.
   */
  public final Window getOwner() {
    return null;
  }
  /**
   * Liefert false.
   */
  public final boolean isSystemForm() {
    return false;
  }
  /**
   * Liefert false.
   */
  public final boolean isModal() {
    return false;
  }
  public final void setModal(boolean b) {
    // nix
  }
  public final void setTitle(String s) {
    if (component != null)  component.setTitle(s);   
  }
  public final String getTitle() {
    return component.getTitle();
  }
  public final void dispose() {
    component.dispose();
  }
  public final void hide() {
    this.saveSizeLocation();
    this.setVisible(false);
  }
  public final void pack() {
  	if (component != null) component.pack();
  }
  public void setVisible(boolean b) {
  	if (component != null) component.setVisible(b);
  }
  public void setAutoSize(boolean b) {
  	if (component != null) component.setAutoSize(b);
  }
  public JLayeredPane getLayeredPane() {
    if (this.getInternalFrame() != null) 
      return this.getInternalFrame().getLayeredPane();
    else
      return null;
  }
  
  /**
   * JDialog / JFrame
   * @return
   */
  public Component getGlassPane() {
    if (this.getInternalFrame() != null) 
      return this.getInternalFrame().getGlassPane();
    else
      return null;
  }
  public void setGlassPane(Component c) {
    if (this.getInternalFrame() != null)
      this.getInternalFrame().setGlassPane(c);
  }

}