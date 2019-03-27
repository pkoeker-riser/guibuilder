package de.guibuilder.framework;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Window;

import javax.swing.JLayeredPane;

/**
 * Implementierung eines Applet.<p>
 * Diese Klasse erweitert zwar GuiWindow, aber einige Methoden
 * bewirken hier naturgemäß nichts.
 * @see GuiRootPane
 */
public class GuiApplet extends GuiWindow {
  // Attributes
  private GuiAppletImpl component;
  private String title;
  // Constructors
  /**
   * Erzeugt ein neues Applet.
   */
  public GuiApplet() {
    super("applet");
    this.guiInit();
  }
  public GuiApplet(String title) {
    super(title); // GuiWindow ruft setTitle auf
    this.guiInit();
  }
  // Methods
  /**
   * Wird beim Starten vom Browser aufgerufen.<p>
   * Kann überschrieben werden.
   */
  public void init() {
  }
  /**
  Initialisierung des Fensters.
  <UL>
  <LI>Dispose on Close
  <LI>WindowListener
  <LI>RootPane
  </UL>
  @see GuiRootPane
  */
  private void guiInit() {
    component.setBackground(Color.lightGray);
    this.setRootPane(new GuiRootPane(this));
  }
  public final String getTag() {
    return "Applet";
  }
  public final Container getComponent() {
    if (component == null) component = new GuiAppletImpl();
    return component;
  }
  public Component getAwtComponent() {
	  return this.getComponent();
  }
  public Window getWindow() {
  	return null;
  }

  public final int getGuiType() {
    return APPLET;
  }
  /**
   * Schiebt dem Fenster ein anderes RootPane unter.
   */
  public final void setRootPane(GuiRootPane root) {
    component.setGuiRootPane(root);
  }
  public final GuiRootPane getRootPane() {
    return (GuiRootPane)component.getRootPane();
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
  public final void pack() {}
  /*
  public final void stop() {
    this.setVisible(false);
  }
  */
  public final GuiAppletImpl getApplet() {
    return component;
  }
  /**
   * Wird von design.GuiMain verwendet.
   */
  public final void setApplet(GuiAppletImpl applet) {
    component = applet;
  }
  public final String getParameter(String p) {
    return component.getParameter(p);
  }
  /**
   * Setzt den - hier unsichtbaren - Titel.
   */
  public final void setTitle(String s) {
    title = s;
  }
  /**
   * Liefert den - hier unsichtbaren - Titel
   */
  public final String getTitle() {
    return title;
  }
  /**
   * Tut nix; Dummy aus Interface GuiWindow
   */
  public final void setIconImage(Image icon) {
  }
  /**
   * Liefert null
   */
  public final Image getIconImage() {
    return null;
  }
  /**
   * Geht bei Applet nicht; daher setVisible(false)
   */
  public final void dispose() {
    component.setVisible(false);
  }
  public final void hide() {
    this.setVisible(false);
  }
  public void setVisible(boolean b) {
  	if (component != null) component.setVisible(b);
  }
  public JLayeredPane getLayeredPane() {
    if (component != null) 
      return component.getLayeredPane();
    else
      return null;
  }
  
  /**
   * JDialog / JFrame
   * @return
   */
  public Component getGlassPane() {
    if (component != null) 
      return component.getGlassPane();
    else
      return null;
  }
  public void setGlassPane(Component c) {
    if (component != null)
      component.setGlassPane(c);
  }

}