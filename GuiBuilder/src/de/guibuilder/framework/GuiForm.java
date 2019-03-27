package de.guibuilder.framework;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Window;

import javax.swing.JLayeredPane;
import javax.swing.WindowConstants;

/**
 * Implementierung eine Formulars.<br>
 * Die meisten Methoden werden an GuiRootPane delegiert.
 * @see GuiRootPane
 */
public class GuiForm extends GuiWindow {
  private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GuiForm.class);
  // Attributes
  private GuiFormImpl component;
  // Constructor
  /**
   * Erzeugt ein neues Formular.
   */
  public GuiForm() {
    super("form");
    guiInit();
  }
  public GuiForm(String title) {
    super(title);
    guiInit();
  }
  private void guiInit() {
    component.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    //component.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    component.setBackground(Color.lightGray);
    component.addWindowListener(new GuiWindowListener(this));
    component.setSize(300,200);
    this.setRootPane(new GuiRootPane(this));
  }
  // Methods
  // From GuiWindow
  public final String getTag() {
    return "Form";
  }

  public final Container getComponent() {
    if (component == null) {
       component = new GuiFormImpl();
    }
    return component;
  }
  public Component getAwtComponent() {
	  return this.getComponent();
  }
  public Window getWindow() {
  	return component;
  }
  public final GuiFormImpl getForm() {
    return (GuiFormImpl)getComponent();
  }
  /**
   * Liefert GuiWindow.FORM
   */
  public final int getGuiType() {
    return FORM;
  }
  /**
   * Schiebt dem Fenster ein anderes RootPane unter.
   */
  public final void setRootPane(GuiRootPane root) {
    if (component != null) component.setGuiRootPane(root);
  }
  /*
   * abstract in GuiWindow
   */
  public final GuiRootPane getRootPane() {
  	if (component != null && component.getRootPane() instanceof GuiRootPane) {
  	  return (GuiRootPane)component.getRootPane();  	  
  	}
  	// Geht schief beim Schließen des Fensters
    //System.err.println("GuiForm#getRootPane(): Missing JFrame.\nThis Form is possibly disposed.");
    return null;
  }
  /**
   * Liefert immer false.
   * @see GuiDialog
   */
  public final boolean isModal()  {
    return false;
  }
  /**
   * Not implemented
   * @see GuiDialog
   */
  public final void setModal(boolean b) {
    //nix
  }
  /**
   * Setzt den Titel des Fensters neu.
   */
  public final void setTitle(String s) {
    getForm().setTitle(s);
  }
  /**
   * Liefert den Titel des Fensters.
   */
  public final String getTitle() {
    return component.getTitle();
  }
  /**
   * Setzt den Icon in der oberen linken Ecke des Fensters (statt Kaffeetasse).
   */
  public final void setIconImage(Image icon) {
    if (component != null) component.setIconImage(icon);
  }
  public final Image getIconImage() {
    if (component != null) {
      return component.getIconImage();
    } else {
      return null;
    }
  }
  /**
   * Trägt das Formular aus dem WindowManager aus.
   * JFrame.dispose
   */
  public final void dispose() {
  	if (component == null) return;
    try {
    	this.hide();
    	if (this.getRootPane() != null) {
    	  this.getRootPane().dispose();    	  
    	  component.setGuiRootPane(null);
    	}
      if (component != null) component.dispose();
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      //System.err.println("GuiForm#dispose(): " + ex.getMessage());
    } finally {
    	component = null;
    }
  }
  /**
   * JFrame.hide
   */
  public final void hide() {
    if (component != null) {
      this.saveSizeLocation();
      this.setVisible(false);
    }
  }
  public final void pack() {
  	component.pack();
  }
  public void setVisible(boolean b) {
  	if (component != null) {
  	  component.setVisible(b);
      if (b) {
        GuiWindowManager.getInstance().addWindow(this);
      } else {
        GuiWindowManager.getInstance().removeWindow(this);
      }
  	}
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