package de.guibuilder.framework;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Types;
import java.util.Iterator;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import de.guibuilder.framework.event.GuiWindowEvent;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.pkjs.util.Convert;
import electric.xml.Document;
import electric.xml.Element;

/**
 * Diese abstakte Klasse ist für Top Level Fenster; also GuiForm, GuiDialog,
 * GuiApplet, GuiInternalFrame.
 * <p>
 * Weitere Methoden stehen über den Zugriff auf GuiRootPane und dessen
 * HauptPanel zur Verfügung.
 * <p>
 * Ein Fenster verfügt <strong>immer</strong> über ein solches RootPane-Objekt
 * und RootPane hat <strong>immer</strong> ein HauptPanel. <br>
 * Der folgende Code funktioniert daher zuverlässig: <br>
 * <code>myForm.getRootPane().getMainPanel()</code>
 *
 * @see #getRootPane
 * @see GuiRootPane#getMainPanel
 */
public abstract class GuiWindow implements MemberAble, Printable {
   private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GuiWindow.class);

   public static final int FORM = 0;
   public static final int DIALOG = 1;
   public static final int APPLET = 2;
   public static final int INTERNAL = 3;
   public static final int NOTHING = 10;
   public static final int DISPOSE = 11;
   public static final int HIDE = 12;
   public static final int RESTORE_NOTHING = 0;
   public static final int RESTORE_SIZE = 20;
   public static final int RESTORE_LOCATION = 21;
   public static final int RESTORE_SIZE_LOCATION = 22;
   // Data-Status
   public static final int STATE_EMPTY = 100;
   public static final int STATE_NEW = 101;
   public static final int STATE_OLD = 102;
   public static final int STATE_SAVED = 103;

   private int state = STATE_EMPTY;

   /** 
    * Default-Titel, der in der GuiFactory gesetzt wird. Ein DummyDialog
    * wird immer dann erzeugt, wenn in der Spezifikation kein Form oder
    * Dialog-Tag enthalten ist. (Das ist z.B. immer dann der Fall, wenn 
    * ein wiederverwendetes Panels per Use in andere Dialoge/Form eingebunden 
    * werden soll.)
    */
   public static final String DUMMY_TITLE = "<Dummy Dialog>";
   private boolean dummyDialog = false;

   /**
    * Kennzeichen, daß es sich um ein Systemfenster handelt. Wenn "true", dann
    * wird beim Schließen des Fensters "System.exit()" aufgerufen (gilt nur für
    * GuiForm).
    */
   private boolean systemForm = false;

   /**
    * Diese Id dient der eindeutigen Identifizierung des Fensters.
    */
   private String windowId = "00";

   /**
    * für zusammengesetzte Window-Titel
    */
   private String defaultTitle;
   protected String name;

   private String msgClose;
   private String msgOpen;
   private String msgActive;
   private boolean autoSize; // pack() wenn true
   private int restoreWindow; // see const
   //   private String restoreWidth; // Alte Werte aus .properties
   //   private String restoreHeight;
   //   private String restoreX;
   //   private String restoreY;

   /**
    * Ein beliebiges Objekt für Benutzer-eigene Zwecke.
    */
   private Object userObject;
   private Document createdBy;

   // Bindings
   private String datasetName;

   private String rootElementName;

   private JDataSet dataset;

   private JDataSet dsPreferences;
   /**
    * Wenn true, werden beim Schließen des Fensters die Preferences gelöscht
    */
   private boolean removePreferencesWhenClosed;
   
   public void setRemovePreferencesWhenClosed(boolean b) {
   	removePreferencesWhenClosed = b;
   }
   
   public boolean isRemovePreferencesWhenClosed() {
   	return removePreferencesWhenClosed;
   }

   // Constructor
   /**
    * Erzeugt ein Fenster mit einem Titel; der Titel wird gleichzeitig als Name
    * verwendet.
    */
   public GuiWindow(String title) {
      this.setTitle(title);
      this.setDefaultTitle(title);
      this.setName(GuiUtil.labelToName(title));
   }

   // Methods **************************************************
   /**
    * Delegation an jeweilige Component
    */
   public final void setName(String name) {
      this.name = name;
      if(getComponent() != null)
         getComponent().setName(name);
   }

   /**
    * Delegation an jeweilige Component
    */
   public final String getName() {
      return getComponent().getName();
   }

   public abstract String getTag();

   // Bindings ################
   void setDatasetName(String s) {
      datasetName = s;
   }

   String getDatasetName() {
      return datasetName;
   }

   /**
    * Setzt das "root-element=" Attribut dieses Fensters für den DataSet
    * 
    * @param s
    */
   public void setRootElementName(String s) {
      rootElementName = s;
      this.getRootPane().getMainPanel().setElementName(s); // Oops?
   }

   /**
    * Liefert das Attribut "root-element=" für DataSet
    * 
    * @return
    */
   public String getRootElementName() {
      return rootElementName;
   }

   private void setDataset(JDataSet ds) { // private statt public PKÖ 8.11.2003
      this.dataset = ds;
   }

   public JDataSet getDataset() {
      return this.dataset;
   }

   public void setDatasetValues(JDataSet ds) {
      this.setDataset(ds);
      this.getRootPane().getMainPanel().setDatasetValues(ds);
   }

   public JDataSet getDatasetValues() {
      // Childs abarbeiten;
      this.getRootPane().getMainPanel().getDatasetValues(this.getDataset());
      return this.getDataset();
   }

   /**
    * Teilt allen Datenbankkomponenten des Fensters und ggf. dem DataSet mit,
    * dass die &Auml;nderungen der Maske korrekt bearbeitet wurden.
    */
   public void commitChanges() {
      if(dataset != null) {
         dataset.commitChanges(); // TODO: ist das hier schlau?
      }
      this.getRootPane().getMainPanel().commitChanges();
   }

   /**
    * Zeigt das Fenster an, wenn Form oder Dialog.
    * <p>
    * Setzt in der Session das CurrentWindow
    * 
    * @see GuiSession#setCurrentWindow
    */
   public final void show() {
      // Restore
      if (this.restoreWindow != RESTORE_NOTHING) {
         this.restoreSizeLocation();
      }
      // show
      if(getComponent() instanceof Window) {
         GuiSession.getInstance().setCurrentWindow(this);
      }
      this.setVisible(true);
      // Autosize
      if(this.isAutoSize()) {
         this.pack();
      }
      // Timer starten
      this.getRootPane().getMainPanel().startTimers();
      
//      // Restore Async
//      if(this.restoreWindow != RESTORE_NOTHING) {
//         restoreSizeLocation();
//      }
          // Geht auch nicht!
//       SwingUtilities.invokeLater(new Runnable() {
//         
//                  @Override
//                  public void run() {
//                     try {
//                     Thread.sleep(1000);
//                     restoreSizeLocation();
//                     Thread.sleep(1000);
//                     restoreSizeLocation();
//                     Thread.sleep(1000);
//                     restoreSizeLocation();
//                     } catch(InterruptedException e) {
//                   }
//                  }
//       });
      // Synchron (geht nicht!)
//         try {
//            Thread.sleep(1000);
//            restoreSizeLocation();
//            Thread.sleep(1000);
//            restoreSizeLocation();
//            Thread.sleep(1000);
//            restoreSizeLocation();
//         }
//         catch(InterruptedException e) {
//         }
   }

   /**
    * Öffnet den Online-Help-Dialog.<p>
    * Voraussetzung:<ul>
    * <li>Hilfe muß allgemein eingerichtet sein.
    * <li>Es muß ein Help-Topic zu diesem Fenster definiert sein <Form ... helpID="MyHelpTopic" ...
    * </ul>
    */
   public void showHelp() {
      GuiSession.getInstance().showHelp(this);
   }

   /**
    * Liefert die Swing-Komponente zu diesem Oberflächen-Objekt. <br>
    * Delegation; es ist möglich, auf die jeweilige swing-Klasse (JApplet,
    * JDialog, JFrame, ...) zu casten; die Swing-Methoden können so genutzt
    * werden.
    * 
    * @return awt.Container als kleinstes gemeinsames Vielfaches
    */
   public abstract Container getComponent();

   public abstract Window getWindow();

   /**
    * Gilt nur für GuiDialog
    * 
    * @see GuiDialog
    */
   public abstract boolean isModal();

   /**
    * Gilt nur für GuiDialog
    * 
    * @see GuiDialog
    */
   public abstract void setModal(boolean b);

   /**
    * Liefert den Typ des Fensters je nach Unterklasse: FORM, DIALOG, APPLET
    * oder INTERNAL
    */
   public abstract int getGuiType();

   /**
    * Liefert den Fenstertitel; gilt nicht für Applet.
    */
   public abstract String getTitle();

   /**
    * Setzt den Fenstertitel; gilt nicht für Applet.
    */
   public abstract void setTitle(String title);

   /**
    * Liefert den Icon in der linken oberen Ecke des Fensters; nur bei Form
    */
   public abstract Image getIconImage();

   /**
    * Setzt den Icon in der linken oberen Ecke des Fensters; nur bei Form
    */
   public abstract void setIconImage(Image icon);

   /**
    * Vernichtet das Fenster und entfernt alle abhängigen Objekte; das hilft dem gc.
    * <p>
    * <strong>Achtung! </strong> <br>
    * Im Anschluß an diese Methodenaufruf dürfen keine weiteren Manipulation an
    * diesem Objekt mehr vorgenommen werden; vielmehr ist die Objektreferenz auf
    * null zu setzen.
    */
   public abstract void dispose();

   /**
    * "Versteckt" das Fenster; es kann anschließend mit "show()" wieder in alter
    * Schönheit angezeigt werden.
    */
   public abstract void hide();

   public abstract void setVisible(boolean b);

   /**
    * Es kann definiert werden, was passiert, wenn der Benutzer die ControlBox
    * des Fensters betätigt: DISPOSE, HIDE, NOTHING.
    * <p>
    * Geht naturgemäß nicht bei einem Applet.
    */
   public final void setDefaultCloseOperation(int type) {
      int oper = WindowConstants.DO_NOTHING_ON_CLOSE;
      switch(type) {
         case DISPOSE:
            oper = WindowConstants.DISPOSE_ON_CLOSE;
            break;
         case HIDE:
            oper = WindowConstants.HIDE_ON_CLOSE;
            break;
      }
      switch(getGuiType()) {
         case APPLET:
            break;
         case DIALOG:
            ((GuiDialogImpl)getComponent()).setDefaultCloseOperation(oper);
            break;
         case FORM:
            ((GuiFormImpl)getComponent()).setDefaultCloseOperation(oper);
            break;
         case INTERNAL:
            ((GuiInternalFrameImpl)getComponent()).setDefaultCloseOperation(oper);
            break;
      }
   }

   /**
    * Diese Methode ersetzt den vorhandenen Inhalt eines Fensters komplett durch
    * eine neue Spezifikation aus dem übergebenen Filenamen. <br>
    * Dabei wird auch das Menü und die Toolbar übernommen. Siehe die
    * Standard-Methode replace([filename]).
    * <p>
    * PENDING Diese Methode hat ein Memory leak!
    * 
    * @deprecated
    */
   public final void replaceRootPane(String filename) {
      /*
       * GuiRootPane root = new GuiRootPane(this); this.setRootPane(root);
       * GuiPanel panel = GuiUtil.getFactory().createPanel(filename,
       * this.getMainPanel()); this.setMainPanel(panel);
       */
      GuiWindow dummy = null;
      try {
         dummy = GuiFactory.getInstance().createWindow(filename);
      }
      catch(GDLParseException ex) {
         ex.printStackTrace();
      }
      if(dummy == null)
         return;
      this.setRootPane(dummy.getRootPane());
      this.getRootPane().setParentWindow(this);
      this.setTitle(dummy.getTitle());
      this.setModal(dummy.isModal());
      this.setIconImage(dummy.getIconImage());
      this.getComponent().validate();
      this.getComponent().repaint();
      this.getRootPane().getContentPane().repaint();
      dummy.setRootPane(null);
      dummy.dispose();

   }

   /**
    * Schiebt dem Fenster ein anderes RootPane unter.
    */
   public abstract void setRootPane(GuiRootPane root);

   /**
    * Liefert GuiRootPane.
    * <p>
    * Wenn der Rückgabewert hier null sein sollte, liegt ein schwerer interner
    * Fehler vor; entweder trat bereits ein Fehler bei der Initialisierung des
    * Fensters auf, oder es wurde zuvor dispose() aufgerufen.
    */
   public abstract GuiRootPane getRootPane();

   /**
    * Liefert das Kennzeichen, ob das Formular ein Systemfenster ist. <br>
    * Nur bei FORM kann true geliefert werden.
    */
   public boolean isSystemForm() {
      return systemForm;
   }

   /**
    * Kennzeichnet das Formmular als ein Systemfenster. Beim Benötigen der
    * Controlbox wird die Anwendung beendet (System.exit). <br>
    * Funktioniert nur bei FORM.
    */
   public final void setSystemForm(boolean b) {
      systemForm = b;
   }

   /**
    * Liefert das Kennzeichen, ob das Fenster ein Dummy-Dialog ist, d.h. von
    * der GuiFactory erzeugt wurde ohne dass in der Spezifikation ein Form der
    * Dialog-Tag enthalten ist.
    * @see GuiFactory#createDummyForm()
    * @see #dummyDialog
    * @see #setDummyDialog(boolean)
    * 
    * @return Kennzeichen, ob es sich um einen DummyDialog handelt
    * 
    * @author thomas
    * 
    */

   public boolean isDummyDialog() {
      return this.dummyDialog;
   }

   /**
    * Kennzeichnet ein Fenster als Dummy-Dialog. DummyDialog wird benutzt, wenn
    * in der Spezifikation kein Form- oder Dialog-Tag enthalten ist.
    * @see GuiFactory#createDummyForm()
    * @see #dummyDialog
    * 
    * @param b Wert für Kennzeichen, ob Dummy Dialog oder nicht
    * 
    * @author thomas
    */

   public final void setDummyDialog(boolean b) {
      this.dummyDialog = b;
   }

   /**
    * Wird von der Factory versorgt für zusammengesetzte Fenstertitel; enthält
    * also den Text aus der GuiSpezifikation.
    */
   public final String getDefaultTitle() {
      return defaultTitle;
   }

   public final void setDefaultTitle(String s) {
      defaultTitle = s;
   }

   public final void setMsgOpen(String s) {
      msgOpen = s;
   }

   public final String getMsgOpen() {
      return msgOpen;
   }

   public final void setMsgClose(String s) {
      msgClose = s;
   }

   public final String getMsgClose() {
      return msgClose;
   }

   public final void setMsgActive(String s) {
      msgActive = s;
   }

   public final String getMsgActive() {
      return msgActive;
   }

   /**
    * Wird beim Öffenen, Schließen oder Aktivieren des Fensters aufgerufen.
    * 
    * @see GuiWindowListener
    * @see de.guibuilder.framework.event.GuiWindowEvent
    */
   final void obj_windowEvent(WindowEvent e) {
      GuiWindowEvent event = null;
      Object controller = this.getController();
      switch(e.getID()) {
         case WindowEvent.WINDOW_ACTIVATED:
            //System.out.println("Activated");
            if(msgActive != null) {
               // Event
               event = new GuiWindowEvent(this, GuiWindowEvent.ACTIVE, e);
               @SuppressWarnings("unused")
               GuiInvokationResult result = this.getRootPane().obj_WindowEvent(msgActive, event, controller);
            }
            try {
               this.getRootPane().getMainPanel().setTimersTempDisabled(false);
            } catch (Exception ex) {
               logger.warn(ex.getMessage(), ex); // gelegentlich NPE?
            }
            break;
         case WindowEvent.WINDOW_CLOSING:
            GuiRootPane rp = this.getRootPane();
            if(rp != null) {
               GuiTable tbl = rp.getCurrentTable();
               if(tbl != null) {
                  tbl.stopCellEditing();
               }
            }
            if(msgClose != null) {
               // Event
               event = new GuiWindowEvent(this, GuiWindowEvent.CLOSE, e);
               @SuppressWarnings("unused")
               GuiInvokationResult result = this.getRootPane().obj_WindowEvent(msgClose, event, controller);
               // bei cancel=true Window nicht schließen
               if(event.cancel)
                  return;
            }
            // hasChanged?

            // Restore geht nur, solange noch kein dispose erfolgt ist!
//            if(this.restoreWindow != RESTORE_NOTHING) {
//               this.saveSizeLocation();
//            }
            // dispose
            int dco = WindowConstants.DO_NOTHING_ON_CLOSE;
            switch(getGuiType()) {
               case APPLET:
                  break;
               case DIALOG:
                  dco = ((GuiDialogImpl)getComponent()).getDefaultCloseOperation();
                  break;
               case FORM:
                  dco = ((GuiFormImpl)getComponent()).getDefaultCloseOperation();
                  break;
               case INTERNAL:
                  dco = ((GuiInternalFrameImpl)getComponent()).getDefaultCloseOperation();
                  break;
            }
            switch (dco) {
               case WindowConstants.DO_NOTHING_ON_CLOSE:
                   break;
               case WindowConstants.DISPOSE_ON_CLOSE:
                   this.dispose();
                   break;
               case WindowConstants.HIDE_ON_CLOSE:
                   this.hide();
                   break;
            }
           break;
        case WindowEvent.WINDOW_CLOSED:
           // Hier ist das Fenster schon zu und hat die Gr��e 0,0 an der Position 0,0
           break;
        case WindowEvent.WINDOW_OPENED:
           if(msgOpen != null) {
              // Event
              event = new GuiWindowEvent(this, GuiWindowEvent.OPEN, e);
              @SuppressWarnings("unused")
              GuiInvokationResult result = this.getRootPane().obj_WindowEvent(msgOpen, event, controller);
           }
           break;
        case WindowEvent.WINDOW_DEACTIVATED:
           //System.out.println("deactivated");
           break;
        case WindowEvent.WINDOW_ICONIFIED:
           //System.out.println("iconified");
           this.getRootPane().getMainPanel().setTimersTempDisabled(true);
           break;
        case WindowEvent.WINDOW_DEICONIFIED: // Passiert nie!?
           //System.out.println("deiconified");
           this.getRootPane().getMainPanel().setTimersTempDisabled(false);
           break;
          default:
             //System.out.println(e.getID());
             break;
      }
   }

   /**
    * @see GuiRootPane#getMenuBar
    */
   public final GuiMenuBar getGuiMenuBar() {
      return this.getRootPane().getGuiMenuBar();
   }

   /**
    * @see GuiRootPane#getMainPanel
    */
   public final GuiPanel getMainPanel() {
      if(this.getRootPane() == null)
         return null;
      else
         return this.getRootPane().getMainPanel();
   }

   /**
    * Liefert den OutlookBar oder null, wenn keiner vorhanden.
    * @return
    */
   public GuiOutlookBar getOutlookBar() {
      JPanel panel1 = (JPanel)this.getMainPanel().getJComponent();
      if(panel1.getComponentCount() > 0) {
         Component comp = panel1.getComponent(0);
         if(comp instanceof JPanel) {
            JPanel panel2 = (JPanel)panel1.getComponent(0);
            LayoutManager lay = panel2.getLayout();
            if(lay instanceof BorderLayout) {
               BorderLayout blay = (BorderLayout)lay;
               Component comp2 = blay.getLayoutComponent(BorderLayout.WEST);
               if(comp2 instanceof GuiOutlookBar) {
                  return (GuiOutlookBar)comp2;
               }
            }
         }
      }
      return null;
   }

   /**
    * @see GuiContainer#getAction
    */
   public final GuiAction getAction(String _name) {
      return this.getRootPane().getMainPanel().getAction(_name);
   }

   /**
    * Führt die Aktion mit dem angegebenen Namen aus. So, als wäre das Menü
    * betötigt oder der Button gedrückt worden.
    * 
    * @see GuiContainer#doAction
    * @param name
    */
   public final void doAction(String _name) {
      this.getRootPane().getMainPanel().doAction(_name);
   }

   /**
    * @see GuiContainer#getMember
    */
   public final GuiMember getGuiMember(String _name) {
      return this.getRootPane().getMainPanel().getMember(_name);
   }

   /**
    * @see GuiContainer#getGuiComponent
    */
   public final GuiComponent getGuiComponent(String _name) {
      return this.getRootPane().getMainPanel().getGuiComponent(_name);
   }

   public final GuiContainer getGuiContainer(String _name) {
      return this.getRootPane().getMainPanel().getContainer(_name);
   }

   public final GuiComponent getGuiComponentFromComponents(String _name) {
      return this.getRootPane().getMainPanel().getGuiComponentsFromComponents(_name);
   }

   /**
    * Liefert ein XML-Document mit allen enthaltenen GuiMembers.
    * @see GuiRootPane#getMemberDocument()
    * @return
    */
   public final Document getMemberDocument() {
      return this.getRootPane().getMemberDocument();
   }

   /**
    * Es kann über den Namen einer Komponente (siehe Attribut name=) ihr Wert
    * ausgelesen werden.
    * 
    * @see GuiContainer#getValue
    */
   public final Object getValue(String _name) {
      return this.getRootPane().getMainPanel().getValue(_name);
   }

   /**
    * Es kann über den Namen einer Komponente (siehe Attribut name=) ihr Wert
    * gesetzt werden.
    * 
    * @see GuiContainer#setValue
    */
   public final void setValue(String name, Object value) {
      this.getRootPane().getMainPanel().setValue(name, value);
   }

   public final Document getAllValuesXml() {
      return this.getRootPane().getMainPanel().getAllValuesXml();
   }

   public final void setAllValuesXml(Document doc) {
      this.getRootPane().getMainPanel().setAllValuesXml(doc);
   }

   public final void setAllValuesXml(Element node) {
      this.getRootPane().getMainPanel().setAllValuesXml(node);
   }

   /**
    * Inhalt aller Felder zurücksetzen; siehe eingebaute Methode new()
    */
   public final void reset() {
      this.setTitle(getDefaultTitle());
      this.getRootPane().reset();
   }

   /**
    * Fenster prüfen (auch notNull)
    * @see GuiContainer#verify
    */
   public final void verify() throws IllegalStateException {
      // Notnull auch prüfen!
      this.getMainPanel().verify(true); // Hier fliegt u.U. eine Exception!
   }

   /**
    * Sanduhr einschalten.
    */
   public void cursorWait() {
      this.getComponent().setCursor(new Cursor(Cursor.WAIT_CURSOR));
   }

   /**
    * Sanduhr ausschalten.
    */
   public void cursorDefault() {
      this.getComponent().setCursor(Cursor.getDefaultCursor());
   }

   /**
    * Liefert die Window-Id des Fensters
    */
   public final String getWindowId() {
      return this.windowId;
   }

   /**
    * Setzt die Window-Id des Fensters
    */
   public final void setWindowId(String id) {
      this.windowId = id;
   }

   public abstract void pack();

   /**
    * @deprecated
    * @see #setController(Object)
    * @param o
    */
   public final void setControler(Object o) {
      this.setController(o);
   }

   /**
    * Controller für Reflection setzen.
    */
   public final void setController(Object c) {
      this.getMainPanel().setController(c);
   }

   /**
    * Controller setzen;
    * 
    * @param s
    *           Ein String, der einen Klassennamen (muß im Classpath enthalten
    *           sein) oder eine Script-File (*.pnut, *.bsh oder *.groovy).
    */
   public final void setController(String s) {
      try {
         if(s.toLowerCase().endsWith(".pnut")) {
            String script = GuiUtil.fileToString(s);
            GuiScripting context = new GuiScriptingPnuts(script);
            this.getRootPane().setContext(context);
            return;
         }
         else if(s.toLowerCase().endsWith(".bsh")) {
            GuiScripting context = new GuiScriptingBeanShell(null, s);
            this.getRootPane().setContext(context);
         }
//         else if(s.toLowerCase().endsWith(".groovy")) {
//            GuiScripting context = new GuiScriptingGroovy(null, s);
//            this.getRootPane().setContext(context);
//         }
         else {
            Class<?> c = Class.forName(s);
            try {
               Method gi = GuiUtil.getMethod(c, "getInstance", null);
               Object o = gi.invoke((Object)null, (Object)null);
               this.setController(o);
               return;
            }
            catch(NoSuchMethodException ex) {
               // na denn eben nich
            }
            Constructor<?> con = c.getConstructor((Class<?>)null);
            Object o = con.newInstance((Object)null);
            this.setController(o);
         }
      }
      catch(Exception ex) {
         GuiUtil.showEx(ex);
      }
   }

   /**
    * @deprecated
    * @see #getController()
    * @return
    */
   public final Object getControler() {
      return this.getController();
   }

   public final Object getController() {
      if(this.getMainPanel() != null) {
         return this.getMainPanel().getController();
      }
      else {
         return null;
      }
   }

   /**
    * @return Returns the userObject.
    */
   public Object getUserObject() {
      return userObject;
   }

   /**
    * Dem Fenster kann ein beliebiges Objekt zugewiesen werden, welches
    * von diesem Farmework niemals manipuliert wird.
    * @param userObject The userObject to set.
    */
   public void setUserObject(Object userObject) {
      this.userObject = userObject;
   }

   /**
    * Liefert die Größe des Objektes
    * 
    * @deprecated
    */
   public final int getObjectSize() {
      final java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
      try {
         new java.io.ObjectOutputStream(baos).writeObject(this);
         return baos.size();
      }
      catch(Exception ex) {
         ex.printStackTrace();
      }
      return -1;
   }

   /**
    * @return Returns the autoSize Property.
    */
   public boolean isAutoSize() {
      return autoSize;
   }

   /**
    * Wenn true, wird die Größe des Fensters automatisch ermittelt:
    * pack()
    * @param autoSize
    */
   public void setAutoSize(boolean autoSize) {
      this.autoSize = autoSize;
   }

   /**
    * Legt fest, ob die Größe und/oder Positions des
    * Fensters persistent abgelegt wird.<p>
    * Erlaubte Werte sind SIZE, LOCATION, SIZE_LOCATION
    * @param restore
    */
   public void setRestoreWindow(String restore) {
      int i = RESTORE_NOTHING;
      if(restore.equalsIgnoreCase("SIZE")) {
         i = RESTORE_SIZE;
      }
      else if(restore.equalsIgnoreCase("LOCATION")) {
         i = RESTORE_LOCATION;
      }
      else if(restore.equalsIgnoreCase("SIZE_LOCATION")) {
         i = RESTORE_SIZE_LOCATION;
      }
      this.setRestoreWindow(i);
   }

   public void setRestoreWindow(int restore) {
      this.restoreWindow = restore;
   }

   public int getRestoreWindow() {
      return this.restoreWindow;
   }

   /**
    * Speichert die Größe und/oder Position des Fensters
    * im Home-Verzeichnis des Benutzers: 
    * ~/.guibuilder/[FensterName].dataset<p>
    * Wenn die Eigenschaft RemovePreferencesWhenClosed gesetzt ist, wird statt dessen versucht dieses File zu löschen.
    */
   public void saveSizeLocation() {
		try {
			String dir = GuiUtil.getLocalDir();
			if (dir == null) {
				return; // java WebStart
			}
			if (this.isRemovePreferencesWhenClosed()) {
				File fds = new File(dir, this.getName() + ".dataset");
				boolean deleted = fds.delete();
				if (deleted) {
					logger.info("Preferences deleted: " + fds.getName());
				}
				return;
			}
			if (this.getRestoreWindow() == RESTORE_NOTHING)
				return; // Nix machen
			if (this.restoreWindow == RESTORE_NOTHING) {
				return;
			}
			// Preferences
			JDataSet ds = this.getPreferences();
			if (ds.hasChanges()) {
				ds.commitChanges();
				File f = new File(dir, this.getName() + ".dataset");
				Document doc = ds.getXml();
				doc.write(f);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

   /**
    * Stellt die Position und/oder die Größe des Fensters
    * seit dem letzten Schließen wieder her.
    * @see #setRestoreWindow(String)
    */
   public void restoreSizeLocation() {
		try {
			String dir = GuiUtil.getLocalDir();
			if (dir == null) {
				return; // java WebStart
			}
			if (this.getRestoreWindow() == RESTORE_NOTHING)
				return; // Nix machen
			// Preferences
			File fds = new File(dir, this.getName() + ".dataset");
			Document doc = new Document(fds);
			final JDataSet ds = new JDataSet(doc);
            SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
               protected Object doInBackground() throws Exception {
                  setPreferences(ds);
//                  Thread.sleep(50);
//                  setPreferences(ds);
//                  Thread.sleep(150);
//                  setPreferences(ds);
                  return null;
               }

               protected void done() {
               }
            };
            worker.execute();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
   }

   /**
    * From awt.Component
    * 
    * @param s_width
    * @param s_height
    */
   public void setSize(String s_width, String s_height) {
      int width = Convert.toInt(s_width);
      int height = Convert.toInt(s_height);
      if(width > 0 && height > 0) {
         this.getComponent().setSize(width, height);
      }
   }

   /**
    * Form awt.Component
    * 
    * @param s_x
    * @param s_y
    */
   public void setLocation(String s_x, String s_y) {
      int x = Convert.toInt(s_x);
      int y = Convert.toInt(s_y);
      this.getComponent().setLocation(x, y); // dürfen auch negativ sein
   }

   /**
    * Setzt den Anzeige-Status STATE_EMPTY | NEW | OLD | SAVED.<p
    * Aktiviert bzw. deaktiviert alle Actions vom Type
    * NEW | SAVE usw.
    * @param state
    */
   public void setActionState(int state) {
      this.state = state;
      Iterator<GuiMember> it = this.getRootPane().getMainPanel().getActions();
      while(it.hasNext()) {
         GuiAction action = (GuiAction)it.next();
         action.setEnabled(state);
      }
   }

   public int getActionState() {
      return this.state;
   }

   /**
    * @see GuiContainer#setStateAttributes(String)
    * @param state
    */
   public void setStateAttributes(String state) {
      this.getRootPane().getMainPanel().setStateAttributes(state);
   }

   /**
    * Liefert das XML-Document aus dem die Facrory dieses Fenster erzeugt hat
    * oder null, wenn das Fenster ohne die Factory erzeugt wurde. 
    * @return
    */
   public Document getCreatedBy() {
      return createdBy;
   }

   void setCreatedBy(Document createdBy) {
      this.createdBy = createdBy;
   }

   public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
      if(pageIndex == 0) {
         Shape s = graphics.getClip();
         Graphics2D g2d = (Graphics2D)graphics;
         g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
         Dimension d = this.getComponent().getSize();
         double compWidth = d.width;
         double compHeight = d.height;
         double pageWidth = pageFormat.getImageableWidth();
         double pageHeight = pageFormat.getImageableHeight();
         double scaleFactor = pageWidth / compWidth;
         scaleFactor = Math.min(scaleFactor, pageHeight / compHeight);
         if(scaleFactor < 1) {
            g2d.scale(scaleFactor, scaleFactor);
         }
         this.getComponent().printAll(graphics);
         return PAGE_EXISTS;
      }
      else {
         return NO_SUCH_PAGE;
      }
   }

   /**
    * JDialog / JFrame
    * @return
    */
   public abstract JLayeredPane getLayeredPane();

   /**
    * JDialog / JFrame
    * @return
    */
   public abstract Component getGlassPane();

   public abstract void setGlassPane(Component c);

   public JDataSet getPreferences() {
      JDataRow row = null;
      if(dsPreferences == null) {
         dsPreferences = new JDataSet("Preferences");
         JDataTable tbl = new JDataTable("WindowPreferences");
         dsPreferences.addRootTable(tbl);
         tbl.addColumn("height", Types.INTEGER);
         tbl.addColumn("width", Types.INTEGER);
         tbl.addColumn("x", Types.INTEGER);
         tbl.addColumn("y", Types.INTEGER);
         tbl.addColumn("maximized", Types.BOOLEAN);
      }
      if(dsPreferences.getChildRows("WindowPreferences") == null) {
         row = dsPreferences.createChildRow();
      }
      else {
         row = dsPreferences.getChildRow("WindowPreferences", 0);
      }
      if(this.getWindow() != null) {
         row.setValue("height", this.getWindow().getHeight());
         row.setValue("width", this.getWindow().getWidth());
         row.setValue("x", this.getWindow().getX());
         row.setValue("y", this.getWindow().getY());
      }
      // Components
      GuiRootPane rp = this.getRootPane();
      if(rp != null) {
         rp.getMainPanel().getPreferences(dsPreferences);
      }
      return dsPreferences;
   }

   public void setPreferences(JDataSet ds) {
      this.dsPreferences = ds;
      if(this.getWindow() == null)
         return;
      if (this.restoreWindow == RESTORE_NOTHING)
         return;
      try {
         JDataRow row = dsPreferences.getChildRow("WindowPreferences", 0);
         switch(restoreWindow) {
            case RESTORE_SIZE: {
               getWindow().setSize(row.getValueInt("width"), row.getValueInt("height"));
            }
               break;
            case RESTORE_LOCATION: {
               getWindow().setLocation(row.getValueInt("x"), row.getValueInt("y"));
            }
               break;
            case RESTORE_SIZE_LOCATION: {
               getWindow().setSize(row.getValueInt("width"), row.getValueInt("height"));
               getWindow().setLocation(row.getValueInt("x"), row.getValueInt("y"));
            }
         }
         // Components
         getRootPane().getMainPanel().setPreferences(dsPreferences);
      }
      finally {
      }

   }
   private void removePreferences() {
   	
   }
}

