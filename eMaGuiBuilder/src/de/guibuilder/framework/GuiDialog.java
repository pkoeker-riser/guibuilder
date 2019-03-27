package de.guibuilder.framework;

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;

/**
 * Implementierung eines Dialoges.<p>
 * Besonders bei modalen Dialogen sollte ein Constructor
 * mit einem Parent Frame als Owner gewählt werden.<p>
 * Mit der Methode "zeige" kann ein modaler Dialog wie
 * eine Funktion eingesetzt werden.
 * Der Dialog kann auch mit der Taste Esc geschlossen werden.
 * @see GuiRootPane
 */
public class GuiDialog extends GuiWindow {
  private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GuiDialog.class);
  
  private GuiDialogImpl component;
  /**
   * Rückgabewert eines Modalen Dialoges.
   * @see #zeige
   */
  private boolean returnValue;
  // Constructors
  /**
   * Erzeugt einen Dialog ohne Owner.
   */
  public GuiDialog() {
    super("dialog");
    component = new GuiDialogImpl();
    this.guiInit();
  }
  /**
   * Erzeugt einen Dialog ohne Owner;
   */
  public GuiDialog(String title) {
    super(title);
    component = new GuiDialogImpl(title);
    this.guiInit();
  }
  /**
   * Erzeugt einen Dialog mit Owner;
   */
  public GuiDialog(GuiWindow p_owner) {
    this(p_owner, "dialog");
  }
  /**
   * Erzeugt einen Dialog mit Owner und einem Titel
   */
  public GuiDialog(GuiWindow p_owner, String title) {
    super(title);
    if (p_owner instanceof GuiDialog) {
      component = new GuiDialogImpl(((GuiDialog)p_owner).getDialog(), title);
    } else  if (p_owner instanceof GuiForm) {
      component = new GuiDialogImpl(((GuiForm)p_owner).getForm(), title);
    } else { // null
      component = new GuiDialogImpl(title);
    }
    this.guiInit();
  }
  // Methods
  /**
   * Initialisierung des Dialoges.<br>
   * Die Größe wird per default auf 400,400 gesetzt.<br>
   * Der Dialog wird mit Esc geschlossen.
   */
  private void guiInit() {
    component.setSize(400, 400);
    this.setRootPane(new GuiRootPane(this));
    {// ESC --> Schließen
      KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
      /*
       * ActionListener für die Esc-Taste zum Schließen des Dialoges
       */
      ActionListener actionListener = new ActionListener() {
        public final void actionPerformed(ActionEvent actionEvent) {
          returnValue = false;
        	hide();
        }
      };
      this.getRootPane().registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    {// F1 = Help
      KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
      /*
       * ActionListener für die F1-Taste zum Anzeigen der Hilfe
       */
      ActionListener actionListener = new ActionListener() {
        public final void actionPerformed(ActionEvent actionEvent) {
           showHelp();
        }
      };
      this.getRootPane().registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    this.component.addWindowListener(new GuiWindowListener(this));
  }
  /**
   * Liefert "Dialog"
   */
  public final String getTag() {
    return "Dialog";
  }
  /**
   * Liefert GuiDialogImpl
   */
  public final Container getComponent() {
    return component;
  }
  public final GuiDialogImpl getDialog() {
    return (GuiDialogImpl)getComponent();
  }
  public Component getAwtComponent() {
	  return this.getComponent();
  }
  public Window getWindow() {
  	return component;
  }
  /**
   * Liefert DIALOG
   */
  public final int getGuiType() {
    return DIALOG;
  }
  /**
   * Setzt RootPane für den Dialog neu.
   */
  public final void setRootPane(GuiRootPane root) {
    if (component != null) component.setGuiRootPane(root);
  }
  public final GuiRootPane getRootPane() {
    if (component != null && component.getRootPane() instanceof GuiRootPane) {
       return (GuiRootPane)component.getRootPane();
    }
    return null;
  }
  /**
   * Setzt den Rückgabewert der Methode "showDialog".
   * @see #showDialog
   */
  public final void setReturnValue(boolean b) {
    returnValue = b;
  }
  /**
   @deprecated
   @see #showDialog
   */
  public final boolean zeige() {
    return this.showDialog();
  }
  /**
   * überlädt die Methode show().<BR>
   * Es wird "true" zurückgegeben, wenn die Standard-Methode "Close()"
   * ausgeführt wird. Es wird "false" zurückgegeben,
   * wenn die Methode "Cancel()" ausgeführt oder die Esc-Taste
   * gedrückt wird.<BR>
   * Der modale Dialog kann dann wie eine Funktion eingesetzt werden.<br>
   * <CODE>
   * if (myDialog.showDialog()) {<br>
   *    doSomeThing()<br>
   * }<br>
   * </CODE>
   */
  public final boolean showDialog() {
     // Restore
     if (this.getRestoreWindow() != RESTORE_NOTHING) {
        this.restoreSizeLocation();
     }
     // Modal
  	  this.setModal(true);
  	  this.setReturnValue(false);
  	  this.setVisible(true); // Hier wird gewartet
     this.saveSizeLocation();
     return returnValue;
  }
  /**
   * Liefert false.
   */
  public final boolean isSystemForm() {
    return false;
  }
  /**
   * Setzt den Dialog als modalen Dialog.
   */
  public final void setModal(boolean b) {
    if (component != null)
      component.setModal(b);
  }
  /**
   * Liefert "true" wenn das ein modaler Dialog ist.
   */
  public final boolean isModal() {
    return component.isModal();
  }
  /**
   * für Attribut type=MODAL, NORESIZE, MODAL_NORESIZE
   */
  public final void setDialogType(String val) {
    if (val.equals("MODAL")) {component.setModal(true);}
    else if (val.equals("NORESIZE")) {component.setResizable(false);}
    else if (val.equals("MODAL_NORESIZE")) {component.setModal(true); component.setResizable(false);}
  }
  // From GuiWindow
  public final void setTitle(String s) {
    if (component != null) getDialog().setTitle(s);
  }
  // From GuiWindow
  public final String getTitle() {    
    if (getDialog() == null) {
      return null;
    } else {
      return getDialog().getTitle();
    }
  }
  /**
   * Tut nix; Dummy von GuiWindow<p>
   * Spaßiger Weise kann der Icon in der linken oberen Ecke bei Dialogen nicht
   * verändert werden - im Unterschied zu JFrame und InternalFrame.
   */
  public final void setIconImage(Image icon) {
    logger.warn("Not implemented");
  }
  /**
   * Liefert hier null.
   */
  public final Image getIconImage() {
    logger.warn("Not implemented");
    return null;
  }
  
  public final void dispose() {
    if (component == null) return;
    try {
      this.hide();
      if (this.getRootPane() != null) {
        this.getRootPane().dispose();       
        component.setGuiRootPane(null);
      }
      component.dispose();
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }
    
  // From GuiWindow
  public final void hide() {
    this.saveSizeLocation();
  	this.setVisible(false);
  }
  public final void pack() {
  	if (component != null) component.pack();
  }
  public void setVisible(boolean b) {
  	if (component != null) { 
      if (b) {
        GuiWindowManager.getInstance().addWindow(this);
      } else {
        GuiWindowManager.getInstance().removeWindow(this);
      }
      component.setVisible(b); // bei modalen Dialogen wird hier gewartet!
  	}
  }
  public JLayeredPane getLayeredPane() {
    if (this.getDialog() != null) 
      return this.getDialog().getLayeredPane();
    else
      return null;
  }
  
  /**
   * JDialog / JFrame
   * @return
   */
  public Component getGlassPane() {
    if (this.getDialog() != null) 
      return this.getDialog().getGlassPane();
    else
      return null;
  }
  public void setGlassPane(Component c) {
    if (this.getDialog() != null)
      this.getDialog().setGlassPane(c);
  }

}