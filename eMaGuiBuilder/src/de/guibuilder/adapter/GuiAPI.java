package de.guibuilder.adapter;

import java.awt.Cursor;
import java.awt.Window;
import java.lang.reflect.Field;
import java.net.URL;

import java.util.*;

import javax.swing.JOptionPane;

import de.pkjs.util.Convert;
import de.guibuilder.framework.GuiAction;
import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiContainer;
import de.guibuilder.framework.GuiDialog;
import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiForm;
import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiMenu;
import de.guibuilder.framework.GuiMenuBar;
import de.guibuilder.framework.GuiMinMax;
import de.guibuilder.framework.GuiPanel;
import de.guibuilder.framework.GuiSelect;
import de.guibuilder.framework.GuiTable;
import de.guibuilder.framework.GuiTableRow;
import de.guibuilder.framework.GuiTree;
import de.guibuilder.framework.GuiTreeNode;
import de.guibuilder.framework.GuiUtil;
import de.guibuilder.framework.GuiWindow;
import de.jdataset.JDataSet;
import electric.xml.Document;

/**
 * Diese Klasse kapselt nach au�en die ansprechbaren Methoden des GuiBuilder
 * wenn er als API verwendet werden soll.<BR>
 * Die API funktioniert nur dann, wenn anfangs die beiden Methoden
 * setCodeBase und setController aufgerufen werden.<br>
 * Optional kann die DocumentBase abweichend zur Codebase absolut oder relativ
 * festgelegt werden.<p>
 * Singleton Pattern
 * @see #setCodeBase
 * @see #setController
 */
public final class GuiAPI implements GuiAPIF {
  // Attributes
  /**
   * Das Objekt, welches die Benutzerereignisse entgegen nimmt (Default).
   * Ansonsten bei jedem Fenster bzw. Member gesondert setzen.
   */
  private Object defaultController;
  private GuiWindow currentWindow;
  private GuiWindow waitWindow;
  private GuiForm dummyForm;

  private LinkedHashMap<String, GuiWindow> windows = new LinkedHashMap<String, GuiWindow>();
  /**
   * Selbstreferenz; singleton.
   */
  private static GuiAPI me;
  // private Constructor
  private GuiAPI() {
  }
  /**
   * Liefert das Object dieser Klasse; singleton.
   */
  public static GuiAPI getInstance() {
    if (me == null) {
    	synchronized(GuiAPI.class) {
    		me = new GuiAPI();
    	}
    }
    return me;
  }
  // Methods
//  private GuiForm getDummyForm() {
//    if (dummyForm == null) {
//      dummyForm = new GuiForm();
//    }
//    return dummyForm;
//  }
  /**
   * Liefert eine Listenkomponente. Wirft eine Exception, wenn das
   * übergebene Objekt weder Combo- noch ListBox ist.
   * @throws IllegalArgumentException
   * @see de.guibuilder.framework.GuiSelect
   */
  private GuiSelect getSelect(GuiComponent comp) {
    if (comp instanceof GuiSelect) {
      return (GuiSelect)comp;
    }
    else {
      throw new IllegalArgumentException("Component not Instance of GuiListComponent: "+comp.getName());
    }
  }
  /**
   * Setzt den Default-Controller für alle Fenster.
   * @see de.guibuilder.framework.GuiWindow#setController
   */
  public void setController(Object o) {
    defaultController = o;
  }
  /**
   * Setzt den Controller für ein bestimmtes Fenster.
   */
  public void setController(String windowId, Object controller) {
    GuiWindow frm = getWindow(windowId);
    frm.setController(controller);
  }
  /**
  Setzt die Codebase für alle Klassen.
  <BR>
  Die DocumentBase wird gleichzeitig auf diesen Wert gesetzt, wenn sie nicht null ist.
  <BR>
  Als Protokoll ist "http" oder "file" erlaubt.
  @see #setDocumentBase
  */
  public void setCodeBase(String url) {
    try {
      GuiUtil.setCodeBase(new URL(url));
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  public void setCodeBase(URL url) {
    GuiUtil.setCodeBase(url);
  }
  public URL getCodeBase() {
    return GuiUtil.getCodeBase();
  }
  /**
  Setzt die DocumentBase relativ zur CodeBase.
  <BR>
  Es handelt sich hierbei um dasjenige Verzeichnis, aus dem die Factory ihre
  Sourcen (Spezifikation, Images) bezieht, wenn keine absolute Adresse angegeben.
  <BR>
  Ist üblicherweise ein Unterverzeichnis der CodeBase;
  die CodeBase muß zuvor gesetzt werden.
  @see GuiUtil#setDocumentBase
  @see #setCodeBase
  */
  public void setDocumentBase(String documentBase) {
    GuiUtil.setDocumentBase(documentBase);
  }
  public void setDocumentBase(URL documentBase) {
    GuiUtil.setDocumentBase(documentBase);
  }
  public URL getDocumentBase() {
    return GuiUtil.getDocumentBase();
  }
  /**
  Setzt den Debug-Modus für die Factory.
  @see GuiUtil#setDebug
  */
  public void setDebug(boolean b) {
    GuiUtil.setDebug(b);
  }
  /**
  Setzt die Version für die Factory.
  <BR>
  Ist das Argument null; wird die Version auf "default" gesetzt.
  @see GuiUtil#setVersion
  */
  public void setVersion(String version) {
    GuiUtil.setVersion(version);
  }
  /**
  Setzt den UI Manager.
  <BR>
  Erlaubte Werte sind "windows", "metal", "motif".
  <BR>
  Groß- und Kleinschreibung ist egal.
  <BR>
  Es darf auch null übergeben werden, dann passiert aber auch nüscht.
  */
  public void setUiManager(String ui) {
    GuiUtil.setUiManager(ui);
  }
  /**
  Sanduhr einschalten.
  */
  public void start() {
    if (currentWindow != null) {
      currentWindow.getComponent().setCursor(new Cursor(Cursor.WAIT_CURSOR));
      waitWindow = currentWindow;
    }
  }
  /**
  Sanduhr ausschalten.
  */
  public void end() {
    if (waitWindow != null) {
      waitWindow.getComponent().setCursor(Cursor.getDefaultCursor());
    }
  }
  /**
  Anwendung beenden.
  <p>
  Bei einer Application wird System.exit aufgerufen; bei einem Applet werden alle
  Fenster geschlossen und der Pinger beendet.
  */
  public void exit() {
    if (GuiUtil.isApplet() == false) {
      System.exit(0);
    } else { // dispose all
      for (Iterator<String> i = windows.keySet().iterator(); i.hasNext();) {
        String windowId = i.next();
        GuiWindow win = (GuiWindow)windows.get(windowId);
        win.dispose();
      }
      windows = new LinkedHashMap<String, GuiWindow>();
      if (Pinger.getInstance() != null) {
        Pinger.getInstance().stopRun();
      }
    }
  }
  /**
  Neues Fenster erstellen und anzeigen.
  <BR>
  Das Fenster erhält den angegebenen Namen und die Id.
  Die WindowId wird von der Dialogsteuerung vergeben und muß je Client-Session
  eindeutig sein;
  hierüber kann der Client auch veranlaßt werden,
  das selbe Fenster mehrfach zu öffnen
  (etwa wenn aus der Personenauskunft heraus die Auskunft
  über eine andere Person aufgerufen werden soll).
  <BR>
  Die WindowId dient sowohl dem GuiBuilder als auch der Dialogsteuerung zur
  Identifikation eines Fensters.
  Bei allen Benutzeraktionen wird die hier vergebene WindowId gemeldet.
  <p>
  Es wird nur angegeben, welche Spezifikationsdatei angefordert werden soll.
  Der eigentliche Transport der Spezifikationen muß über einen anderen Kanal
  (z.B. HTTP) erfolgen.
  <BR>
  Dieses ermöglicht es dem Client,
  die Gui-Spezifikationen - oder noch besser das ganze Fenster -
  in einem Cache für den wiederholten Gebrauch vorzuhalten.
  <BR>
  Die Dialogsteuerung selbst kann mit den Methoden hideWindow und showWindow ein Fenster
  verstecken bzw. ein verstecktes Fenster wieder anzeigen.
  Hierüber kann die Performanz der Anwendung - bei entsprechendem Speicherbedarf -
  gesteigert werden.
  @param windowName Ein beliebiger Name für das Fenster.
  @param windowId Eine eindeutige Id für dieses Fenster.
  @param fileName Dateiname eine Gui-Spezifikation relativ zur DocumentBase.
  @see #hideWindow
  @see #showWindow
  @see #createWindow
  @see #setDocumentBase
  */
  public void openWindow(String windowName, String windowId, String fileName) {
    GuiWindow frm = null;
    try {
      frm = GuiFactory.getInstance().createWindow(fileName);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    if (frm != null) {
      frm.setName(windowName);
      frm.setWindowId(windowId);
      windows.put(windowId, frm);
      if (defaultController != null) {
        frm.setController(defaultController);
      }
      frm.show();
      currentWindow = frm;
    }
  }
  /**
  Erzeugt das Fenster wie <code>openWindow</code>, aber zeigt es nicht an.
  Das Fenster kann anschließend mit <code>showWindow</code> sichtbar gemacht werden.
  <br>
  Es wird der Default.Controller gesetzt.
  @see #openWindow
  @see #showWindow
  */
  public void createWindow(String windowName, String windowId, String fileName) {
    GuiWindow frm = null;
    try {
      frm = GuiFactory.getInstance().createWindow(fileName);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    if (frm != null) {
      frm.setName(windowName);
      frm.setWindowId(windowId);
      windows.put(windowId, frm);
      if (defaultController != null) {
        frm.setController(defaultController);
      }
    }
  }
  /**
  Erzeugt ein Fenster wie createWindow.
  <BR>
  Statt eines Dateinamens wird hier ein String erwartet, der ein XML-Dokument
  enthält.
  Das Fenster kann anschließend mit <code>showWindow</code> sichtbar gemacht werden.
  @see #openWindow
  @see #showWindow
  */
  public void createWindowXml(String windowName, String windowId, String doc) {
    GuiWindow frm = null;
    try {
      frm = GuiFactory.getInstance().createWindowXml(doc);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    if (frm != null) {
      frm.setName(windowName);
      frm.setWindowId(windowId);
      windows.put(windowId, frm);
      if (defaultController != null) {
        frm.setController(defaultController);
      }
    }
  }
  /**
  Das Fenster mit der angegebenen WindowId ist zu schließen (dispose).
  <BR>
  Es wird eine Exception geworfen, wenn diese WindowId bisher nicht geöffnet wurde.
  <P>
  PENDING: Was passiert mit currentWindow? Wird auf null gesetzt!?
  */
  public void closeWindow(String windowId) {
    GuiWindow frm = getWindow(windowId);
    windows.remove(windowId);
    if (frm == currentWindow) currentWindow = null;
    frm.dispose();
  }
  /**
  Macht ein Fenster unsichtbar.
  Achtung! Der Fensterinhalt bleibt dabei erhalten!
  @see #showWindow
  @see #resetWindow
  */
  public void hideWindow(String windowId) {
    GuiWindow frm = getWindow(windowId);
    frm.getComponent().setVisible(false);
  }
  /**
   * Zeigt ein mit <code>createWindow</code> erzeugtes
   * oder mit <code>hideWindow</code> verstecktes Fenster an.
   * @see #hideWindow
   * @see #createWindow
   */
  public void showWindow(String windowId) {
    GuiWindow frm = getWindow(windowId);
    frm.getComponent().setVisible(true);
    currentWindow = frm;
  }
  /**
   * Zeigt einen modalen Dialog wie eine Funktion.
   * @return true, wenn der Benutzer den OK-Button bet�tigt hat
   * oder false, bei Cancel oder Schlie�en des Dialoges.
   */
  public boolean showModalDialog(String windowId) {
    GuiDialog dlg = (GuiDialog)getWindow(windowId);
    dlg.setModal(true);
    return dlg.showDialog();
  }
  /**
   * Es wird ein anderes Fenster aktiviert, wenn mehrere gleichzeitig offen sind;
   * bewirkt einen Focuswechsel zwischen verschiedenen Fenstern.<BR>
   * Es wird eine Exception geworfen, wenn diese WindowId bisher nicht ge�ffnet wurde.<BR>
   * Als Folge dieser Anweisung wird von dem Fenster die Nachricht "windowActivated"
   * gesendet.
   */
  public void activateWindow(String windowId) {
    GuiWindow frm = getWindow(windowId);
    if (frm.getComponent() instanceof Window) {
      ((Window)frm.getComponent()).toFront();
    }
    currentWindow = frm;
  }
  /**
   * Alle Felder der Fensters leeren, alle Registerkarten, Menüeinträge und Buttons
   * werden enabled.
   */
  public void resetWindow(String windowId) {
    GuiWindow frm = getWindow(windowId);
    frm.setTitle(frm.getDefaultTitle());
    frm.reset();
  }
  /**
   * Fensterinhalt auf gültige Eingaben prüfen.
   */
  public void verifyWindow(String windowId) throws IllegalStateException {
    GuiWindow frm = getWindow(windowId);
    frm.verify();
  }
  /**
   * Leert die Felder eines einzelnen Panels (zumeist eine Registerkarte).
   */
  public void resetPanel(String windowId, String name) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer panel = frm.getGuiContainer(name);
    panel.reset();
  }
  /**
   * Selektiert oder deselektiert alle TextComponents des Panels.<br>
   * Wenn name null, dann MainPanel.
   * @deprecated
   */
  public void selectPanel(String windowId, String name, boolean b) {
    GuiWindow frm = getWindow(windowId);
    GuiPanel panel = null;
    if (name == null || name.length() == 0) {
      panel = frm.getMainPanel();
    } else {
      panel = (GuiPanel)frm.getGuiContainer(name);
    }
    panel.selectTextMembers(b);
  }
  /**
   * Setzt die Titelzeile des Fensters neu.
   */
  public void setWindowTitle(String windowId, String value) {
    GuiWindow frm = getWindow(windowId);
    frm.setTitle(value);
  }
  /**
   * @deprecated
   * @see de.guibuilder.framework.GuiWindow#replaceRootPane
   */
  public void replaceRootPane(String windowId, String filename) {
    //##GuiWindow frm = getWindow(windowId);
    //##frm.replaceRootPane(filename);
  }
  /**
   * Es soll eine andere Registerkarte aktiviert werden.
   */
  public void activateTab(String windowId, String tabName) {
    GuiWindow frm = getWindow(windowId);
    frm.getRootPane().activateTab(tabName);
    currentWindow = frm;
  }
  /**
   * Die ganze Registerkarte wird für Benutzeraktionen gesperrt (false),
   * d.h. daß sie auch nicht mehr angeklickt werden kann; bzw wieder aktiviert (true).
   */
  public void enableTab(String windowId, String tabName, boolean b) {
    GuiWindow frm = getWindow(windowId);
    frm.getRootPane().enableTab(tabName, b);
  }
  /**
   * Setzt den Wert der angegebenen Komponente auf den angegebenen Wert.<P>
   * Diese Methode kann auch für geschachtelte Container verwendet werden,
   * wenn der Name der Componente in Punkt-Notation angegeben wird:<BR>
   * Beispiel: <br><code>
   * setValue("myWindowId", "myTab.myPanel.myComponent", "neuer Wert");</code>
   */
  public void setValue(String windowId, String name, String value) {
    GuiWindow frm = getWindow(windowId);
    frm.setValue(name, value);
  }
  /**
   * Setzt den Wert der angegebenen Komponente
   * - die auf der angegebenen Registerkarte liegt - auf den angegebenen Wert.
   */
  public void setValue(String windowId, String tabName, String name, String value) {
    GuiWindow frm = getWindow(windowId);
    if (tabName != null && tabName.length() != 0) {
      frm.setValue(tabName+"."+name, value);
    } else {
      frm.setValue(name, value);
    }
  }
  /**
   * Liefert den Wert der angegebenen Komponente.
   * Diese Methode kann auch für geschachtelte Container verwendet werden,
   * wenn der Name der Componente in Punkt-Notation angegeben wird:<BR>
   * Beispiel: <br><code>
   * getValue("myWindowId", "myTab.myPanel.myComponent");</code>
   * @param windowId des Fensters
   * @param name Name der Komponente.
   * @return Wert der Komponente oder null, wenn name null ist.
   */
  public String getValue(String windowId, String name) {
    GuiWindow frm = getWindow(windowId);
    if (frm.getValue(name) == null)
      return null;
    else
      return frm.getValue(name).toString();
  }
  /**
  Liefert den Wert der angegebenen Komponente,
  die auf der angegebenen Registerkarte liegt.
  @param windowId des Fensters
  @param tabName Name einer Registerkarte (Panel oder Group) dieses Fensters.
  @param name Name der Komponente auf der Registerkarte.
  @return Inhalt der Komponente.
  */
  public String getValue(String windowId, String tabName, String name) {
    GuiWindow frm = getWindow(windowId);
    if (tabName != null && tabName.length() != 0) {
      return frm.getValue(tabName+"."+name).toString();
    } else {
      return frm.getValue(name).toString();
    }
  }
  /**
  Setzt alle Komponenten des übergebenen Name-Value-Pairs.<br>
  Bei Angabe eines ungültigene Komponentennamens wird eine
  IllegalArgumentException geworfen.
  */
  public void setAllValues(String windowId, HashMap hash) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer panel = frm.getMainPanel();
    panel.setAllValues(hash);
  }
  /**
  Setzt mehrere Komponenten des angegebenen Panels auf den Werte der
  (public) Attribute des übergebenen Objects.
  <BR>
  Hierbei m�ssen die Namen der Attributes der Klassen den Namen der
  Oberfl�chen-Komponenten entsprechen. Auf Gro�- und Kleinschreibung ist zu
  achten.
  <BR>
  Auf alle Attribute wird die Methode toString() angewendet.
  Voraussetzung ist, daß die angegebenen Komponenten einen String als
  Wert akzeptieren; also z.B. keine Tabellen.
  @param windowId Id des Fensters
  @param panelName Name eines Panels dieses Fensters (darf auch Tab oder Group sein).
  @param obj Ein Object dessen Attributnamen mit den Namen der
  Komponenten übereinstimmt.
  */
  public void setValues(String windowId, String panelName, Object obj) {
    Class cls = obj.getClass();
    Field[] fs = cls.getFields();
    for (int i = 0; i<fs.length; i++) {
      Field f = fs[i];
      try {
        String val = null;
        if (f.get(obj) != null) {
          val = f.get(obj).toString();
        }
        setValue(windowId, panelName, f.getName(), val);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  /**
   * Liefert den Inhalt des Fensters als XmlDocument.
   */
  public Document getAllValuesXml(String windowId) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer main = frm.getMainPanel();
    Document doc = main.getAllValuesXml();
    return doc;
  }
  /**
   * Liefert den Inhalt eines Containers (Registerkarte)  als XmlDocument.
   */
  public Document getAllValuesXml(String windowId, String tabName) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    Document doc = tab.getAllValuesXml();
    return doc;
  }
  public JDataSet getDatasetValues(String windowId) {
     GuiWindow frm = getWindow(windowId);
     return frm.getDatasetValues();
  }
  public void setDatasetValues(String windowId, JDataSet ds) {
     GuiWindow frm = getWindow(windowId);
     frm.setDatasetValues(ds);
  }
  /**
   * Setzt die Werte des Fensters mit einem XmlDocument.
   */
  public void setAllValuesXml(String windowId, Document doc) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer main = frm.getMainPanel();
    main.setAllValuesXml(doc);
  }
  /**
   * Setzt die Werte einer Registerkarte mit einem XmlDocument.
   * @param windowId des Fensters
   * @param tabName Name einer Registerkarte (Panel oder Group) dieses Fensters.
   */
  public void setAllValuesXml(String windowId, String tabName, Document doc) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    tab.setAllValuesXml(doc);
  }
  /**
  Setzt das XmlDocument, welches von einem TreeNode gehalten wird, zeigt aber nix an,
  @see de.guibuilder.framework.GuiTreeNode#getGuiPath
  @param path Pfad zum Knoten in Punktnotation: root.myFolder.myNode
  */
  public void setNodeValues(String windowId, String path, Document doc) {
    GuiWindow frm = getWindow(windowId);
    GuiTree tree = frm.getRootPane().getCurrentTree();
    if (tree != null) {
      GuiTreeNode node = tree.getGuiTreeNode(path);
      if (node != null) {
        node.setAllValuesXml(doc);
      }
      else  {
        System.out.println("Missing TreeNode: "+path);
      }
    }
  }
  /**
   * Aktiviert oder deaktiviert Menüeintr�ge und Buttons (auch Toolbar).<P>
   * PENDING:<BR>
   * Der Name aller Buttons, Tools und Menüeintr�ge muß eindeutig sein!
   * Auch die Namen von Buttons auf Registerkarten!
   */
  public void enable(String windowId, String name, boolean b) {
    GuiWindow frm = getWindow(windowId);
    frm.getRootPane().getMainPanel().getAction(name).setEnabled(b);
  }
  /**
  Aktiviert oder deaktiviert Eingabekomponenten wie Text, Combo usw.(keine Buttons oder Menüeintrage)
  */
  public void enableComp(String windowId, String name, boolean b) {
    GuiWindow frm = getWindow(windowId);
    frm.getGuiComponent(name).setEnabled(b);
  }
  /**
  Aktiviert oder deaktiviert Eingabekomponenten wie Text, Combo usw.
  die auf einer Registerkarte liegt.
  (keine Buttons oder Menüeintrage)
  */
  public void enableComp(String windowId, String tabName, String name, boolean b) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    tab.getGuiComponent(name).setEnabled(b);
  }
  /**
   * @param Ein HashSet mit den Namen der gew�nschten Komponenten.
   * @see de.guibuilder.framework.GuiContainer#setEnabled
   */
  public void enableComp(String windowId, ArrayList components, boolean b) {
    GuiWindow frm = getWindow(windowId);
    frm.getMainPanel().setEnabled(components, b);
  }
  /**
  Setzt den Focus auf die angegebene Komponente.
  */
  public void setFocus(String windowId, String name) {
    GuiWindow frm = getWindow(windowId);
    GuiComponent comp = frm.getGuiComponent(name);
    comp.requestFocus();
  }
  /**
  Setzt den Focus auf die angegebene Komponente.
  */
  public void setFocus(String windowId, String tabName, String name) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiComponent comp = tab.getGuiComponent(name);
    comp.requestFocus();
  }
  /**
  Selektiert die angegebene Zeile und Spalte der Tabelle.
  */
  public void setFocus(String windowId, String tableName, int row, int col) {
    setFocus(windowId, null, tableName, row, col);
  }
  /**
  Selektiert die angegebene Zeile und Spalte der Tabelle.
  */
  public void setFocus(String windowId, String tabName, String tableName, int row, int col) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = null;
    if (tabName != null) {
      tab = frm.getGuiContainer(tabName);
    } else {
      tab = frm.getMainPanel();
    }
    GuiTable tbl = tab.getGuiTable(tableName);
    tbl.setFocus(row, col);
  }
  /**
  Setzt das Minimum und das Maximum bei Scrollbar und Slider neu.
  PENDING: Bei Scrollbar immer max um 10 gr��er angeben !!!??? (Bug in Swing?)
  @see GuiMinMax
  */
  public void setMinMaxValue(String windowId, String name, int min, int max) {
    GuiWindow frm = getWindow(windowId);
    GuiComponent comp = frm.getGuiComponent(name);
    if (comp instanceof GuiMinMax) {
      ((GuiMinMax)comp).setValue(Integer.toString(min));
      ((GuiMinMax)comp).setMinimum(min);
      ((GuiMinMax)comp).setMaximum(max);
    }
    else {
      throw new IllegalArgumentException("Class not GuiMinMax: "+name);
    }
  }
  /**
  Setzt das Minimum und das Maximum bei Scrollbar und Slider neu.
  @see GuiMinMax
  */
  public void setMinMaxValue(String windowId, String tabName, String name, int min, int max) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiComponent comp = tab.getGuiComponent(name);
    if (comp instanceof GuiMinMax) {
      ((GuiMinMax)comp).setMinimum(min);
      ((GuiMinMax)comp).setMaximum(max);
    }
    else {
      throw new IllegalArgumentException("Class not GuiMinMax: "+name);
    }
  }
  /**
  Setzt das �nderungskennzeichen einer Komponente.
  @see de.guibuilder.framework.GuiComponent#setModified
  */
  public void setModified(String windowId, String name, boolean b) {
    GuiWindow frm = getWindow(windowId);
    GuiComponent comp = frm.getGuiComponent(name);
    comp.setModified(b);
  }
  public void setModified(String windowId, String tabName, String name, boolean b) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiComponent comp = tab.getGuiComponent(name);
    comp.setModified(b);
  }
  /**
  F�llt eine List- oder Combobox neu mit Werten.
  */
  public void setItems(String windowId, String name, Vector items) {
    GuiWindow frm = getWindow(windowId);
    GuiComponent comp = frm.getGuiComponent(name);
    GuiSelect list = getSelect(comp);
    list.setItems(items);
  }
  /**
  F�llt eine List- oder Combobox neu mit Werten.
  */
  public void setItems(String windowId, String tabName, String name, Vector items) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiComponent comp = tab.getGuiComponent(name);
    GuiSelect list = getSelect(comp);
    list.setItems(items);
  }
  /**
  F�llt eine Combobox, die Spalte einer Tabelle ist, neu mit Werten.
  <br>
  tabName darf auch null sein.
  */
  public void setItems(String windowId, String tabName, String tblName, int colIndex, Vector items) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = null;
    if (tabName != null && tabName.length() != 0) {
      tab = frm.getGuiContainer(tabName);
    } else {
      tab = frm.getMainPanel();
    }
    GuiTable tbl = tab.getGuiTable(tblName);
    tbl.setItems(colIndex, items);
  }
  /**
  F�gt der List- oder Combobox einen Eintrag am Ende hinzu.
  */
  public void addItem(String windowId, String name, String item) {
    GuiWindow frm = getWindow(windowId);
    GuiComponent comp = frm.getGuiComponent(name);
    GuiSelect list = getSelect(comp);
    list.addItem(item);
  }
  /**
  F�gt der List- oder Combobox einen Eintrag am Ende hinzu.
  */
  public void addItem(String windowId, String tabName, String name, String item) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiComponent comp = tab.getGuiComponent(name);
    GuiSelect list = getSelect(comp);
    list.addItem(item);
  }
  /**
  F�gt einen Eintrag an einem bestimmten Index ein.
  */
  public void addItem(String windowId, String name, String item, int index) {
    GuiWindow frm = getWindow(windowId);
    GuiComponent comp = frm.getGuiComponent(name);
    GuiSelect list = getSelect(comp);
    list.addItem(item, index);
  }
  /**
  F�gt einen Eintrag an einem bestimmten Index ein.
  */
  public void addItem(String windowId, String tabName, String name, String item, int index) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiComponent comp = tab.getGuiComponent(name);
    GuiSelect list = getSelect(comp);
    list.addItem(item, index);
  }
  /**
  L�scht den angegebenen Eintrag aus der List- oder Combobox.
  */
  public void removeItem(String windowId, String name, String item) {
    GuiWindow frm = getWindow(windowId);
    GuiComponent comp = frm.getGuiComponent(name);
    GuiSelect list = getSelect(comp);
    list.removeItem(item);
  }
  /**
  L�scht den angegebenen Eintrag aus der List- oder Combobox.
  */
  public void removeItem(String windowId, String tabName, String name, String item) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiComponent comp = tab.getGuiComponent(name);
    GuiSelect list = getSelect(comp);
    list.removeItem(item);
  }
  /**
  L�scht alle Eintr�ge aus der List- oder Combobox.
  */
  public void removeAll(String windowId, String name) {
    GuiWindow frm = getWindow(windowId);
    GuiComponent comp = frm.getGuiComponent(name);
    GuiSelect list = getSelect(comp);
    list.removeAllItems();
  }
  /**
  L�scht alle Eintr�ge aus der List- oder Combobox.
  */
  public void removeAll(String windowId, String tabName, String name) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiComponent comp = tab.getGuiComponent(name);
    GuiSelect list = getSelect(comp);
    list.removeAllItems();
  }
  /**
  F�gt dem Menü einen neuen Eintrag mit einem Label und einem ActionCommand
  hinzu.
  @param windowId Eine eindeutige Id für dieses Fenster.
  @param menuName Der Name des Menüs, dem der Menüeintrag hinzuzuf�gen ist.
  @param label Beschriftung des Menüeintrags.
  @param name Name des Menüeintrags; wenn null wird das Label als Name verwendet.
  @param cmd ActionCommand, welches beim Bet�tigen dieses Menüeintrags
  übergeben wird; wenn null, wird das Label als ActionCommand verwendet.
  */
  public void addMenuItem(String windowId, String menuName, String label, String name, String cmd) {
    try {
      addMenuItem(windowId, menuName, label, name, cmd, "NORMAL");
    } catch (IllegalArgumentException ex) {
      throw ex;
    }
  }
  /**
  F�gt dem Menü einen neuen Eintrag mit einem Label und einem ActionCommand
  hinzu.
  @param windowId Eine eindeutige Id für dieses Fenster.
  @param menuName Der Name des Menüs, dem der Menüeintrag hinzuzuf�gen ist.
  @param label Beschriftung des Menüeintrags.
  @param name Name des Menüeintrags; wenn null wird das Label als Name verwendet.
  @param cmd ActionCommand, welches beim Bet�tigen dieses Menüeintrags
  übergeben wird; wenn null, wird das Label als ActionCommand verwendet.
  @param type Typ des Menüeintrags: NORMAL, OPTION, CHECK.
  */
  public void addMenuItem(String windowId, String menuName, String label, String name, String cmd, String type) {
    GuiWindow frm = getWindow(windowId);
    GuiMenuBar menuBar = frm.getGuiMenuBar();
    try {
      GuiMenu menu = menuBar.getGuiMenu(menuName);
      menu.addGuiMenuItem(label, name, cmd, type);
    }
    catch (IllegalArgumentException ex) {
      throw ex;
    }
  }
  /**
   * Liefert den Inhalt der Tabelle als einen Vector von TableRows
   * @see de.guibuilder.framework.GuiTableRow
   */
  public Vector getTableValues(String windowId, String tableName) {
    GuiWindow frm = getWindow(windowId);
    GuiTable tbl = frm.getRootPane().getMainPanel().getGuiTable(tableName);
    return tbl.getTableRows();
  }
  /**
  Liefert den Inhalt der angegebenen Spalte aus der selektierten Zeile.
  */
  public String getCellValue(String windowId, String tableName, int col) {
    return getCellValue(windowId, null, tableName, col);
  }
  /**
  Liefert den Inhalt der angegebenen Spalte aus der selektierten Zeile.
  */
  public String getCellValue(String windowId, String tabName, String tableName, int col) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = null;
    if (tabName != null) {
      tab = frm.getGuiContainer(tabName);
    } else {
      tab = frm.getMainPanel();
    }
    GuiTable tbl = tab.getGuiTable(tableName);
    return tbl.getCellValue(col);
  }
  /**
  Setzt den Wert eine Tabellenzelle neu.
  */
  public void setCellValue(String windowId, String tableName, String value, int row, int col) {
    setCellValue(windowId, null, tableName, value, row, col);
  }
  /**
  Setzt den Wert eine Tabellenzelle neu.
  */
  public void setCellValue(String windowId, String tabName, String tableName, String value, int row, int col) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = null;
    if (tabName != null) {
      tab = frm.getGuiContainer(tabName);
    } else {
      tab = frm.getMainPanel();
    }
    GuiTable tbl = tab.getGuiTable(tableName);
    tbl.setValueAt(value, row, col);
  }
  /**
   * @deprecated
   * überschreibt den Inhalt einer Tabellenzeile mit neuen Werten.
   */
  public void setRowValues(String windowId, String tableName, int row, Vector values) {
    GuiWindow frm = getWindow(windowId);
    GuiTable tbl = frm.getRootPane().getMainPanel().getGuiTable(tableName);
    tbl.setRowValues(row, values);
  }
  /**
   * @deprecated
   * überschreibt den Inhalt einer Tabellenzeile mit neuen Werten.
   */
  public void setRowValues(String windowId, String tabName, String tableName, int row, Vector values) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiTable tbl = tab.getGuiTable(tableName);
    tbl.setRowValues(row, values);
  }
  /**
   * F�gt eine leere Zeile in die Tabelle ein.
   */
  public void insertRow(String windowId, String tableName) {
    GuiWindow frm = getWindow(windowId);
    GuiTable tbl = frm.getRootPane().getMainPanel().getGuiTable(tableName);
    tbl.insertRow();
  }
  /**
   * Fügt eine Zeile der Tabelle am Ende hinzu.
   */
  public void insertRow(String windowId, String tableName, Vector values) {
    if (values == null) {
      insertRow(windowId, tableName);
    } else {
      GuiWindow frm = getWindow(windowId);
      GuiTable tbl = frm.getRootPane().getMainPanel().getGuiTable(tableName);
      tbl.insertRow(new GuiTableRow(tbl, values));
    }
  }
  /**
   * F�gt eine Zeile in die Tabelle ein.
    */
  public void insertRow(String windowId, String tableName, int row, Vector values) {
    GuiWindow frm = getWindow(windowId);
    GuiTable tbl = frm.getRootPane().getMainPanel().getGuiTable(tableName);
    if (values == null) {
      tbl.insertRow(row);
    } else {
      tbl.insertRow(row, new GuiTableRow(tbl, values));
    }
  }
  /**
  F�gt eine Zeile der Tabelle am Ende hinzu.
  */
  public void insertRow(String windowId, String tabName, String tableName, Vector values) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiTable tbl = tab.getGuiTable(tableName);
    if (values == null) {
      tbl.insertRow();
    } else {
      tbl.insertRow(new GuiTableRow(tbl, values));
    }
  }
  /**
  F�gt eine Zeile in die Tabelle ein.
  */
  public void insertRow(String windowId, String tabName, String tableName, int row, Vector values) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiTable tbl = tab.getGuiTable(tableName);
    if (values == null) {
      tbl.insertRow(row);
    } else {
      tbl.insertRow(row, new GuiTableRow(tbl, values));
    }
  }
  /**
  Löscht die selektierte Zeile aus der Tabelle.
  */
  public void deleteRow(String windowId, String tableName) {
    GuiWindow frm = getWindow(windowId);
    GuiTable tbl = frm.getRootPane().getMainPanel().getGuiTable(tableName);
    tbl.deleteRow();
  }
  /**
  Löscht die angegebene Zeile aus der Tabelle.
  */
  public void deleteRow(String windowId, String tableName, int row) {
    GuiWindow frm = getWindow(windowId);
    GuiTable tbl = frm.getRootPane().getMainPanel().getGuiTable(tableName);
    tbl.deleteRow(row);
  }
  /**
  L�scht die angegebene Zeile aus der Tabelle.
  */
  public void deleteRow(String windowId, String tabName, String tableName, int row) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiTable tbl = tab.getGuiTable(tableName);
    tbl.deleteRow(row);
  }
  /**
  L�scht die selektierte Zeile aus der Tabelle.
  */
  public void deleteRow(String windowId, String tabName, String tableName) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiTable tbl = tab.getGuiTable(tableName);
    tbl.deleteRow();
  }
  /**
   * Liefert die in dieser Sitzung gel�schten Zeilen, die vor der Sitzung
   * vorhanden waren; also nicht die neu eingef�gten und gleich wieder
   * gel�schten Zeilen.
   * @return Vector von GuiTableRows
   * @see de.guibuilder.framework.GuiTableRow
   */
  public Vector getDeletedRows(String windowId, String tableName) {
    GuiWindow frm = getWindow(windowId);
    GuiTable tbl = frm.getRootPane().getMainPanel().getGuiTable(tableName);
    return tbl.getDeletedRows();
  }
  /**
   * F�llt eine Tabelle mit neuen Daten (Vector von Vectoren).<br>
   * <B>Achtung!</B><BR>
   * Der übergebene Vector wird hier mit clone() an die Tabelle weitergereicht!
   */
  public void setTableValues(String windowId, String tableName, Vector values) {
    GuiWindow frm = getWindow(windowId);
    GuiTable tbl = frm.getRootPane().getMainPanel().getGuiTable(tableName);
    tbl.setValue(values);
  }
  /**
   * F�llt eine Tabelle mit neuen Daten (Vector von Vectoren).<br>
   * <B>Achtung!</B><BR>
   * Der übergebene Vector wird hier mit clone() an die Tabelle weitergereicht!
   */
  public void setTableValues(String windowId, String tabName, String tableName, Vector values) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    GuiTable tbl = tab.getGuiTable(tableName);
    tbl.setValue(values);
  }
  /**
   * Liefert die Tabellenzeile mit der angegebenen Nummer.
   */
  public GuiTableRow getTableRow(String windowId, String tableName, int row) {
    GuiWindow frm = getWindow(windowId);
    GuiTable tbl = frm.getRootPane().getMainPanel().getGuiTable(tableName);
    return tbl.getRow(row);
  }
  public String[] getTableColumnValues(String windowId, String tableName, int col) {
    GuiWindow frm = getWindow(windowId);
    GuiTable tbl = frm.getRootPane().getMainPanel().getGuiTable(tableName);
    return tbl.getColValues(col);
  }
  /**
   * Einfache Benachrichtigung an den Benutzer (nur Button OK).
   * @param title Titel der Nachricht
   * @param type Art der Nachricht: Error, Info, Warn, Ask
   * @param message Text der Nachricht.
   */
  public void showMessage(String title, String type, String message) {
    GuiUtil.showMessage(null, title, type, message);
  }
  /**
   * Wahl einer Option durch den Benutzer mit Ausl�sen einer entsprechenden Nachricht.<P>
   * Es wird eine MessageBox angezeigt, die die mit "buttons" definierten Optionen zur
   * Auswahl anbietet.<BR>
   * In "actionCommands" ist eine gleich gro�e Anzahl von eindeutigen Nachrichten zu hinterlegen,
   * die den buttons entsprechen.<BR>
   * Es wird ein Event GuiMessageBoxEvent generiert. Das ActionCommand dieser Nachricht entspricht
   * der vom Benutzer gew�hlten Aktion.<BR>
   * Wurde keine windowId angegeben (null), wird bei GuiMessageBoxEvent als Fenstername "MessageBox" und
   * als FensterId "-1" geliefert.<BR>
   * Wenn der Benutzer die ControlBox angeklickt, wird
   * als ActionCommand "CANCEL" geliefert.
   * @param windowId des Parent Windows oder null, wenn kein Parent.
   * @param msgName Name der MessageBox für GuiMessageBoxEvent.
   * @param title Titel der Nachricht.
   * @param type Art der Nachricht: Error, Info, Warn, Ask.
   * @param message Text der Nachricht.
   * @param buttons Menge der Wahlm�glichkeiten durch den Benutzer.
   * @param actionCommands ActionCommands für die Buttons.
   * @return actionCommand des vom Benutzer gedr�ckten Button oder CANCEL,
   * wenn ControlBox angeklickt wurde.
   * @see de.guibuilder.framework.event.GuiMessageBoxEvent
   */
  public String showMessage(String windowId, String msgName, String title,
      String type, String message, String[] buttons, String[] actionCommands) {
    GuiWindow parent = null;
    if (windowId == null) {
      if (dummyForm == null) {
        dummyForm = new GuiForm();
        dummyForm.setName("MessageBox");
        dummyForm.setWindowId("-1");
        if (defaultController != null) {
          dummyForm.setController(defaultController);
        }
      }
      parent = dummyForm;
    } else {
      parent = getWindow(windowId);
    }
    if (buttons.length != actionCommands.length) {
      throw new IllegalArgumentException("Buttons.length != Commands.length");
    }
    int msg_type = JOptionPane.PLAIN_MESSAGE;
    if (type.equals("Error")) {
      msg_type = JOptionPane.ERROR_MESSAGE;
    } else if (type.equals("Info")) {
      msg_type = JOptionPane.INFORMATION_MESSAGE;
    } else if (type.equals("Warn")) {
      msg_type = JOptionPane.WARNING_MESSAGE;
    } else if (type.equals("Ask")) {
      msg_type = JOptionPane.QUESTION_MESSAGE;
    }
    int ret = JOptionPane.showOptionDialog(parent.getComponent(), message, title, 0,
                              msg_type, null, buttons, null);
    String retS;
    if (ret == -1) {
      retS = "CANCEL";
    } else {
      retS = actionCommands[ret];
    }
    parent.getRootPane().obj_MessageBoxEvent(parent, msgName, retS);
    return retS;
  }
  /**
   * @see de.guibuilder.framework.GuiUtil#fileOpenDialog
   */
  public String[] fileOpenDialog(String parentWindowId, String dialogTitle,
      String directoryName, String fileName) {
    GuiWindow frm = (GuiWindow)windows.get(parentWindowId);
    return GuiUtil.fileOpenDialog(frm, dialogTitle, directoryName, fileName);
  }
  /**
   * @see de.guibuilder.framework.GuiUtil#fileSaveDialog
   */
  public String[] fileSaveDialog(String parentWindowId, String dialogTitle,
      String directoryName, String fileName) {
    GuiWindow frm = (GuiWindow)windows.get(parentWindowId);
    return GuiUtil.fileSaveDialog(frm, dialogTitle, directoryName, fileName);
  }
  /**
   * Liefert ein Window (Form oder Dialog).<BR>
   * Auf diese Art k�nnen die Methoden des Fensters aufgerufen werden.
   */
  public GuiWindow getWindow(String windowId) {
    GuiWindow frm = (GuiWindow)windows.get(windowId);
    if (frm == null) {
      throw new IllegalArgumentException("Missing WindowId: "+windowId);
    }
    return frm;
  }
  /**
   * Liefert eine Tabelle die auf einer Registerkarte liegt.
   */
  public GuiTable getTable(String windowId, String tabName, String tableName) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer tab = frm.getGuiContainer(tabName);
    return tab.getGuiTable(tableName);
  }
  /**
   * Liefert eine Tabelle, die direkt auf einem Fenster liegt.
   */
  public GuiTable getTable(String windowId, String tableName) {
    GuiWindow frm = getWindow(windowId);
    return frm.getRootPane().getMainPanel().getGuiTable(tableName);
  }
  /**
   * Liefert die Tree-Komponente eines Fensters, oder null, wenn keine
   * vorhanden.<BR>
   * Es wird davon ausgegangen, daß ein Fenster nur eine Tree-Komponente enthält!
   */
  public GuiTree getTree(String windowId) {
    GuiWindow frm = getWindow(windowId);
    return frm.getRootPane().getCurrentTree();
  }
  /**
   * F�gt dem Tree des Fensters einen Knoten hinzu. Der Knoten wird
   * dem zuletzt aktivierten Knoten hinzugef�gt.
   */
  public void addTreeNode(String windowId, String title, String filename) {
    GuiWindow frm = getWindow(windowId);
    GuiTreeNode node = frm.getRootPane().getCurrentTree().addGuiNode(title);
    if (filename != null) {
      node.setFileName(filename);
    }
  }
  /**
   * F�gt dem Tree des Fensters einen Knoten mit einem speziellen Namen hinzu. Der Knoten wird
   * dem zuletzt aktivierten Knoten hinzugef�gt.
   */
  public void addTreeNode(String windowId, String title, String name, String filename) {
    GuiWindow frm = getWindow(windowId);
    GuiTreeNode node = frm.getRootPane().getCurrentTree().addGuiNode(title, name);
    if (filename != null) {
      node.setFileName(filename);
    }
  }
  /**
   * Setzt den selektierten Konten im Baum.
   * @param path Pfad zum Knoten in Punkt-Notation: "root.myFolder.myNode".
   */
  public void setSelectedNode(String windowId, String path) {
    GuiWindow frm = getWindow(windowId);
    frm.getRootPane().getCurrentTree().setSelectedNode(path);
  }
  /**
   * L�scht den selektierten Knoten des Baums; es wird die Knoten selekiert, der auf den
   * gel�schten folgt.
   */
  public void removeTreeNode(String windowId) {
    GuiWindow frm = getWindow(windowId);
    //frm.getCurrentTree().removeNode(); // geht nicht!
    frm.getRootPane().getCurrentTree().cutNode();
  }
  /**
   * Liefert eine Objektreferenz auf die Komponente mit dem angegebenen Namen.
   */
  public GuiMember getMember(String windowId, String name) {
    GuiWindow frm = getWindow(windowId);
    GuiMember member = frm.getGuiMember(name);
    return member;
  }
  /**
   * Liefert eine Objektreferenz auf die Komponente mit dem angegebenen Namen.
   */
  public GuiMember getMember(String windowId, String tabName, String name) {
    GuiWindow frm = getWindow(windowId);
    GuiMember member = frm.getGuiMember(tabName+"."+name);
    return member;
  }
  /**
   * Liefert eine Objektreferenz auf die Action mit dem angegebenen Namen.
   */
  public GuiAction getAction(String windowId, String name) {
    GuiWindow frm = getWindow(windowId);
    GuiAction action = frm.getMainPanel().getAction(name);
    return action;
  }
  /**
   * Liefert eine Objektreferenz auf die Komponente mit dem angegebenen Namen.
   */
  public GuiComponent getComponent(String windowId, String name) {
    GuiWindow frm = getWindow(windowId);
    GuiComponent comp = frm.getGuiComponent(name);
    return comp;
  }
  /**
   * Liefert eine Objektreferenz auf die Komponente mit dem angegebenen Namen.
   */
  public GuiComponent getComponent(String windowId, String tabName, String name) {
    GuiWindow frm = getWindow(windowId);
    GuiComponent comp = frm.getGuiComponent(tabName+"."+name);
    return comp;
  }
  /**
   * Liefert eine Objektreferenz auf einen Conatainer mit dem angegebenen Namen.
   */
  public GuiContainer getContainer(String windowId, String name) {
    GuiWindow frm = getWindow(windowId);
    GuiContainer cont = frm.getGuiContainer(name);
    return cont;
  }
  /**
   * @param interval Ping-Interval in Sekunden
   */
  public void setPingInterval(String interval) {
    int i = Convert.toInt(interval);
    Pinger.setPingSleep(i);
  }
  public void setDefaultResourceBundle(String name) {
    GuiUtil.setDefaultResourceBundle(name);
  }

}