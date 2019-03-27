package de.guibuilder.adapter;

import java.util.HashMap;
import java.util.Vector;

import de.jdataset.JDataSet;
import de.pkjs.util.Convert;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
/**
 * Diese Klasse analysiert die Anweisungen der Dialogsteuerung wenn diese als
 * XML-Dokumente vorliegen und steuert damit die Oberfl�che über das API.
 * @see GuiAPI
 * @see de.guibuilder.server.GuiXAPI
 */
public final class XmlAPI {
  // Attributes
  private static XmlAPI me;

  private GuiAPI guiAPI = GuiAPI.getInstance();

  private HashMap<String, Integer> hash = new HashMap<String, Integer>(60);

  private static final int ACTIVATE_TAB = 1;
  private static final int ACTIVATE_WINDOW = 2;
  private static final int ADD_ITEM = 3;
  private static final int ADD_MENUITEM = 4;
  private static final int ADD_TREENODE = 5;
  private static final int CLOSE_WINDOW = 6;
  private static final int CREATE_WINDOW = 7;
  private static final int CREATE_WINDOW_XML = 8;
  private static final int DELETE_ROW = 9;
  private static final int ENABLE = 10;
  private static final int ENABLE_COMP = 11;
  private static final int ENABLE_TAB = 12;
  private static final int END = 13;
  private static final int EXIT = 14;
  private static final int GET_ALL_MODIFIED_VALUES = 15;
  private static final int GET_ALL_VALUES = 16;
  private static final int GET_MEMBER_NAMES = 17;
  private static final int GET_VALUE = 18;
  private static final int GET_VALUES = 19;
  private static final int HIDE_WINDOW = 20;
  private static final int INSERT_ROW = 21;
  private static final int OPEN_WINDOW = 22;
  private static final int REMOVE_ALL = 23;
  private static final int REMOVE_ITEM = 24;
  private static final int REMOVE_TREENODE = 25;
  private static final int RESET_PANEL = 26;
  private static final int VERIFY_WINDOW = 27;
  private static final int RESET_WINDOW = 28;
  private static final int SET_ALL_VALUES = 30;
  private static final int SET_CELL_VALUE = 31;
  private static final int SET_CODEBASE = 32;
  private static final int SET_DEBUG = 33;
  private static final int SET_DOCUMENTBASE = 34;
  private static final int SET_FOCUS = 35;
  private static final int SET_ITEMS = 36;
  private static final int SET_MIN_MAX_VALUE = 37;
  private static final int SET_MODIFIED = 38;
  private static final int SET_SELECTED_NODE = 40;
  private static final int SET_NODE_VALUES = 41;
  private static final int SET_TABLE_VALUES = 42;
  private static final int SET_UIMANAGER = 43;
  private static final int SET_VALUE = 44;
  private static final int SET_VALUES = 45;
  private static final int SET_VERSION = 46;
  private static final int SET_WINDOW_TITLE = 47;
  private static final int SHOW_MESSAGE = 48;
  private static final int SHOW_WINDOW = 49;
  private static final int START = 50;
  private static final int SET_PING_INTERVAL = 51;
  private static final int REPLACE_TABSET = 52;
  // New 19.12.2004 PKÖ
  private static final int GET_DATASET_VALUES = 100;
  private static final int SET_DATASET_VALUES = 101;

  // Constructor
  /**
  Privater Constructor
  @see #getInstance
  */
  private XmlAPI() {
    guiInit();
  }
  // Methods
  private void guiInit() {
    hash.put("ActivateTab", new Integer(ACTIVATE_TAB));
    hash.put("ActivateWindow" , new Integer(ACTIVATE_WINDOW));
    hash.put("AddItem" , new Integer(ADD_ITEM));
    hash.put("AddMenuItem" , new Integer(ADD_MENUITEM));
    hash.put("AddTreeNode" , new Integer(ADD_TREENODE));
    hash.put("CloseWindow" , new Integer(CLOSE_WINDOW));
    hash.put("CreateWindow" , new Integer(CREATE_WINDOW));
    hash.put("CreateWindowXml" , new Integer(CREATE_WINDOW_XML));
    hash.put("DeleteRow" , new Integer(DELETE_ROW));
    hash.put("Enable" , new Integer(ENABLE));
    hash.put("EnableComp" , new Integer(ENABLE_COMP));
    hash.put("EnableTab" , new Integer(ENABLE_TAB));
    hash.put("End" , new Integer(END));
    hash.put("Exit" , new Integer(EXIT));
    hash.put("GetAllModifiedValues" , new Integer(GET_ALL_MODIFIED_VALUES));
    hash.put("GetAllValues" , new Integer(GET_ALL_VALUES));
    hash.put("GetMemberNames" , new Integer(GET_MEMBER_NAMES));
    hash.put("GetValue" , new Integer(GET_VALUE));
    hash.put("GetValues" , new Integer(GET_VALUES));
    hash.put("HideWindow" , new Integer(HIDE_WINDOW));
    hash.put("InsertRow" , new Integer(INSERT_ROW));
    hash.put("OpenWindow" , new Integer(OPEN_WINDOW));
    hash.put("RemoveAll" , new Integer(REMOVE_ALL));
    hash.put("RemoveItem" , new Integer(REMOVE_ITEM));
    hash.put("RemoveTreeNode" , new Integer(REMOVE_TREENODE));
    hash.put("ResetPanel" , new Integer(RESET_PANEL));
    hash.put("ResetWindow" , new Integer(RESET_WINDOW));
    hash.put("VerifyWindow" , new Integer(VERIFY_WINDOW));
    hash.put("SetAllValues" , new Integer(SET_ALL_VALUES));
    hash.put("SetCellValue" , new Integer(SET_CELL_VALUE));
    hash.put("SetCodeBase" , new Integer(SET_CODEBASE));
    hash.put("SetDebug" , new Integer(SET_DEBUG));
    hash.put("SetDocumentBase" , new Integer(SET_DOCUMENTBASE));
    hash.put("SetFocus" , new Integer(SET_FOCUS));
    hash.put("SetItems" , new Integer(SET_ITEMS));
    hash.put("SetMinMaxValue" , new Integer(SET_MIN_MAX_VALUE));
    hash.put("SetModified" , new Integer(SET_MODIFIED));
    hash.put("SetSelectedNode" , new Integer(SET_SELECTED_NODE));
    hash.put("SetNodeValues" , new Integer(SET_NODE_VALUES));
    hash.put("SetTableValues" , new Integer(SET_TABLE_VALUES));
    hash.put("SetUiManager" , new Integer( SET_UIMANAGER));
    hash.put("SetValue" , new Integer(SET_VALUE));
    hash.put("SetValues" , new Integer(SET_VALUES));
    hash.put("SetVersion" , new Integer(SET_VERSION));
    hash.put("SetWindowTitle" , new Integer(SET_WINDOW_TITLE));
    hash.put("ShowMessage" , new Integer(SHOW_MESSAGE));
    hash.put("ShowWindow" , new Integer(SHOW_WINDOW));
    hash.put("Start" , new Integer(START));
    hash.put("SetPingInterval" , new Integer(SET_PING_INTERVAL));
    hash.put("ReplaceTabset" , new Integer(REPLACE_TABSET));
    // Dataset
    hash.put("SetDatasetValues" , new Integer(SET_DATASET_VALUES));
    hash.put("GetDatasetValues" , new Integer(GET_DATASET_VALUES));
  }
  /**
   * Liefert ein Objekt dieser Klasse.
   */
  public static XmlAPI getInstance() {
    if (me == null) {
    	synchronized(XmlAPI.class) {
    		me = new XmlAPI();
    	}
    }
    return me;
  }
  /**
   * Verarbeitet ein Xml-Script von Anweisungen an die Oberfäche.
   * @return Null oder ein Xml-Document zur Weiterleitung an den Server
   */
  public synchronized Document execute(Document doc) throws Exception {
    //Node node;
    Element ele = null;
    //String msg = null; // Text des Client-Events
    String window = null;	// Attribut Window-Bezeichnung
    String windowId = null;	// Attribut Id
    String tab = null;		// Attribut Tab-Name
    String tbl = null;		// Attribut Tabellen-Name
    String name = null;	// Attribut Komponenten-Name
    //String index = null;	// Attribut Index
    String row = null;		// Attribut Zeile
    String col = null;		// Attribut Spalte
    String cmd = null; 	// ActionCommand
    // Replay-Document erstellen, falls nötig
    Document ret = new Document();
    //ret.setEncoding( "UTF-8" );
    // Hier wird die Lieferung an den Server angehängt.
    Element rootNode = ret.setRoot("Replay");

    Element root = doc.getRoot(); // "Do"
    Elements rootList = root.getElements(); // Liste aller Befehle
    // Liste aller Befehle abarbeiten.
    while (rootList.hasMoreElements()) {
      ele = rootList.next();
      String nodeName = ele.getName();
      System.out.println("XmlAPI: "+nodeName);
      Integer action = (Integer)hash.get(nodeName);
      if (action == null) {
        System.out.println("XmlAPI.execute unspecified action: "+nodeName);
        return null;
      }
      // Befehl
      switch (action.intValue()) {

        case ACTIVATE_TAB: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          guiAPI.activateTab(windowId, tab);
        } break;

        case ACTIVATE_WINDOW: {
          windowId = ele.getAttributeValue("Id");
          guiAPI.activateWindow(windowId);
        } break;

        case ADD_ITEM: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          name = ele.getAttributeValue("Name");
          cmd = ele.getTextString();
          if (tab == null) {
            guiAPI.addItem(windowId, name, cmd);
          }
          else {
            guiAPI.addItem(windowId, tab, name, cmd);
          }
        } break;

        case ADD_MENUITEM: {
          windowId = ele.getAttributeValue("Id");
          String menuName = ele.getAttributeValue("MenuName");
          String label = ele.getAttributeValue("Lbl");
          name = ele.getAttributeValue("Name");
          cmd = ele.getAttributeValue("Cmd");
          String type = ele.getAttributeValue("Type");
          guiAPI.addMenuItem(windowId, menuName, label, name, cmd, type);
        } break;

        case ADD_TREENODE: {
          windowId = ele.getAttributeValue("Id");
          name = ele.getAttributeValue("Name");
          String filename = ele.getAttributeValue("Filename");
          cmd = ele.getTextString();
          if (name == null) {
            guiAPI.addTreeNode(windowId, cmd, filename);
          } else {
            guiAPI.addTreeNode(windowId, cmd, name, filename);
          }
        } break;

        case CLOSE_WINDOW: {
          windowId = ele.getAttributeValue("Id");
          guiAPI.closeWindow(windowId);
        } break;

        case CREATE_WINDOW: {
          window = ele.getAttributeValue("Window");
          windowId = ele.getAttributeValue("Id");
          cmd = ele.getTextString();
          guiAPI.createWindow(window, windowId, cmd);
        } break;

        case CREATE_WINDOW_XML: {
          window = ele.getAttributeValue("Window");
          windowId = ele.getAttributeValue("Id");
          cmd = ele.getTextString();
          guiAPI.createWindowXml(window, windowId, cmd);
        } break;

        case DELETE_ROW: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          tbl = ele.getAttributeValue("Table");
          row = ele.getAttributeValue("Row");
          if (tab == null) {
            if (row  == null) {
              guiAPI.deleteRow(windowId, tbl);
            }
            else {
              guiAPI.deleteRow(windowId, tbl, Convert.toInt(row));
            }
          }
          else {
            if (row == null) {
              guiAPI.deleteRow(windowId, tab, tbl);
            }
            else {
              guiAPI.deleteRow(windowId, tab, tbl, Convert.toInt(row));
            }
          }
        } break;

        case ENABLE: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          name = ele.getAttributeValue("Name");
          cmd = ele.getTextString();
          boolean b = Boolean.valueOf(cmd).booleanValue();
          if (tab == null) {
            guiAPI.enable(windowId, name, b);
          }
          else {
            //guiAPI.enable(windowId, tab, name, b);
          }
        } break;

        case ENABLE_COMP: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          name = ele.getAttributeValue("Name");
          cmd = ele.getTextString();
          boolean b = Boolean.valueOf(cmd).booleanValue();
          if (tab == null) {
            guiAPI.enableComp(windowId, name, b);
          }
          else {
            guiAPI.enableComp(windowId, tab, name, b);
          }
        } break;

        case ENABLE_TAB: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          cmd = ele.getTextString();
          boolean b = Boolean.valueOf(cmd).booleanValue();
          guiAPI.enableTab(windowId, tab, b);
        } break;

        case END: {
          guiAPI.end();
        } break;

        case EXIT: {
          guiAPI.exit();
        } break;

        case GET_ALL_MODIFIED_VALUES: {
        	
        } break;

        case GET_ALL_VALUES: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          Document childDoc = null;
          if (tab == null) {
            childDoc = guiAPI.getAllValuesXml(windowId);
          } else {
            childDoc = guiAPI.getAllValuesXml(windowId, tab);
          }
          Element childRoot = childDoc.getRoot();
          Element tmp = rootNode.addElement("GetAllValues");
          tmp.setAttribute("Id", windowId);
          tmp.addElement((Element)childRoot.clone());
        } break;

        case GET_MEMBER_NAMES: {
        	
        } break;

        case GET_VALUE: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          name = ele.getAttributeValue("Name");
          String value = null;
          if (tab == null) {
            value = guiAPI.getValue(windowId, name);
          } else {
            value = guiAPI.getValue(windowId, tab, name);
          }
          Element get = rootNode.addElement("GetValue");
          get.setAttribute("Id", windowId);
          if (tab != null) {
            get.setAttribute("Tab", tab);
          }
          get.setAttribute("Name", name);
          get.addText(value);

        } break;

        case GET_VALUES: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          Element getValues = rootNode.addElement("GetValues");
          getValues.setAttribute("Id", windowId);
          if (tab != null) {
            getValues.setAttribute("Tab", tab);
          }
          Elements getList = ele.getElements(); // Liste aller Komponenten
          while(getList.hasMoreElements()) {
            Element eleGetNode = getList.next();
            name = eleGetNode.getAttributeValue("Name");
            String value = null;
            if (tab == null) {
              value = guiAPI.getValue(windowId, name);
            } else {
              value = guiAPI.getValue(windowId, tab, name);
            }
            Element get = getValues.addElement("GetValue");
            get.setAttribute("Name", name);
            get.addText(value);
          }
        } break;

        case HIDE_WINDOW: {
          windowId = ele.getAttributeValue("Id");
          guiAPI.hideWindow(windowId);
        } break;
        // 4 Sorten: mit/ohne "Tab", mit/ohne "Row"
        case INSERT_ROW: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          tbl = ele.getAttributeValue("Table");
          row = ele.getAttributeValue("Row");
          Vector v = getCols(ele);
          if (tab == null) {
            if (row == null) {
              guiAPI.insertRow(windowId, tbl, v);
            } else {
              guiAPI.insertRow(windowId, tbl, Convert.toInt(row), v);
            }
          }
          else {
            if (row == null) {
              guiAPI.insertRow(windowId, tab, tbl, v);
            } else {
              guiAPI.insertRow(windowId, tab, tbl, Convert.toInt(row), v);
            }
          }
        } break;

        case OPEN_WINDOW: {
          window = ele.getAttributeValue("Window");
          windowId = ele.getAttributeValue("Id");
          cmd = ele.getTextString();
          guiAPI.openWindow(window, windowId, cmd);
        } break;

        case REMOVE_ALL: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          name = ele.getAttributeValue("Name");
          if (tab == null) {
            guiAPI.removeAll(windowId, name);
          }
          else {
            guiAPI.removeAll(windowId, tab, name);
          }
        } break;

        case REMOVE_ITEM: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          name = ele.getAttributeValue("Name");
          cmd = ele.getTextString();
          if (tab == null) {
            guiAPI.removeItem(windowId, name, cmd);
          }
          else {
            guiAPI.removeItem(windowId, tab, name, cmd);
          }
        } break;

        case REMOVE_TREENODE: {
          windowId = ele.getAttributeValue("Id");
          guiAPI.removeTreeNode(windowId);
        } break;

        case RESET_PANEL: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Panel");
          guiAPI.resetPanel(windowId, tab);
        } break;

        case VERIFY_WINDOW: {
          windowId = ele.getAttributeValue("Id");
          guiAPI.verifyWindow(windowId);
        } break;

        case RESET_WINDOW: {
          windowId = ele.getAttributeValue("Id");
          guiAPI.resetWindow(windowId);
        } break;

        case SET_ALL_VALUES: {
          setValues(ele);
        } break;

        case SET_CELL_VALUE: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          tbl = ele.getAttributeValue("Table");
          cmd = ele.getTextString();
          row = ele.getAttributeValue("Row");
          col = ele.getAttributeValue("Col");
          if (tab == null) {
            guiAPI.setCellValue(windowId, tbl, cmd, Convert.toInt(row), Convert.toInt(col));
          }
          else {
            guiAPI.setCellValue(windowId, tab, tbl, cmd, Convert.toInt(row), Convert.toInt(col));
          }
        } break;

        case SET_CODEBASE: {
          cmd = ele.getTextString();
          guiAPI.setCodeBase(cmd);
        } break;

        case SET_DEBUG: {
          cmd = ele.getTextString();
          guiAPI.setDebug(Boolean.valueOf(cmd).booleanValue());
        } break;

        case SET_DOCUMENTBASE: {
          cmd = ele.getTextString();
          guiAPI.setDocumentBase(cmd);
        } break;

        case SET_FOCUS: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          tbl = ele.getAttributeValue("Table");
          name = ele.getAttributeValue("Name");
          cmd = ele.getTextString();
          row = ele.getAttributeValue("Row");
          col = ele.getAttributeValue("Col");
          // Ohne Tab
          if (tab == null) {
            if (row != null && col != null) {
              guiAPI.setFocus(windowId, tbl, Convert.toInt(row), Convert.toInt(col));
            } else {
              guiAPI.setFocus(windowId, name);
            }
          }
          // Mit Tab
          else {
            if (row != null && col != null) {
              guiAPI.setFocus(windowId, tab, tbl, Convert.toInt(row), Convert.toInt(col));
            } else {
              guiAPI.setFocus(windowId, tab, name);
            }
          }
        } break;

        case SET_ITEMS: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          tbl = ele.getAttributeValue("Table");
          name = ele.getAttributeValue("Name");
          col = ele.getAttributeValue("ColIndex");
          Vector v = getCols(ele);
          if (tab == null) {
            if (tbl == null) {
              guiAPI.setItems(windowId, name, v);
            }
            else {
              // Combobox in Tabelle ohne Registerkarte
              guiAPI.setItems(windowId, null, tbl, Convert.toInt(col), v);
            }
          }
          else {
            if (tbl == null) {
              guiAPI.setItems(windowId, tab, name, v);
            }
            else {
              // Combobox in Tabelle und Registerkarte
              guiAPI.setItems(windowId, tab, tbl, Convert.toInt(col), v);
            }
          }
        } break;

        case SET_MIN_MAX_VALUE: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          name = ele.getAttributeValue("Name");
          String min = ele.getAttributeValue("min");
          String max = ele.getAttributeValue("max");
          int iMin = Convert.toInt(min);
          int iMax = Convert.toInt(max);
          if (tab == null) {
            guiAPI.setMinMaxValue(windowId, name, iMin, iMax);
          }
          else {
            guiAPI.setMinMaxValue(windowId, tab, name, iMin, iMax);
          }
        } break;

        case SET_MODIFIED: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          name = ele.getAttributeValue("Name");
          cmd = ele.getTextString();
          boolean b = Boolean.valueOf(cmd).booleanValue();
          if (tab == null) {
            guiAPI.setModified(windowId, name, b);
          } else {
            guiAPI.setModified(windowId, tab, name, b);
          }
        } break;

        case SET_SELECTED_NODE: {
          windowId = ele.getAttributeValue("Id");
          cmd = ele.getTextString();
          guiAPI.setSelectedNode(windowId, cmd);
        } break;

        case SET_NODE_VALUES: {
          windowId = ele.getAttributeValue("Id");
          String path = ele.getAttributeValue("Path");
          Elements list = ele.getElements();
          Document docNode = new Document();
          Element sv = list.first();
          if (sv != null) {
            docNode.setRoot((Element)sv.clone());
          }
          guiAPI.setNodeValues(windowId, path, docNode);
        } break;

        case SET_TABLE_VALUES: {
          setTableValues(ele);
        } break;

        case SET_UIMANAGER: {
          cmd = ele.getTextString();
          guiAPI.setUiManager(cmd);
        } break;

        case SET_VALUE: {
          windowId = ele.getAttributeValue("Id");
          tab = ele.getAttributeValue("Tab");
          name = ele.getAttributeValue("Name");
          cmd = ele.getTextString();
          if (tab == null) {
            guiAPI.setValue(windowId, name, cmd);
          }
          else {
            guiAPI.setValue(windowId, tab, name, cmd);
          }
        } break;

        case SET_VALUES: {
          setValues(ele);
        } break;

        case SET_VERSION: {
          cmd = ele.getTextString();
          guiAPI.setVersion(cmd);
        } break;

        case SET_WINDOW_TITLE: {
          windowId = ele.getAttributeValue("Id");
          cmd = ele.getTextString();
          guiAPI.setWindowTitle(windowId, cmd);
        } break;

        // 2 Varianten: Einfach Nachricht oder mit Auswahl
        case SHOW_MESSAGE: {
          String msgName = ele.getAttributeValue("Name");
          String title = ele.getAttributeValue("Title");
          String type = ele.getAttributeValue("Type");
          cmd = ele.getTextString().trim();
          Vector<String> buttons = new Vector<String>();
          Vector<String> actionCommands = new Vector<String>();
          getMessageButtons(ele, buttons, actionCommands);
          if (buttons.size() == 0) {
            guiAPI.showMessage(title, type, cmd);
          } else {
            windowId = ele.getAttributeValue("Id");
            String[] sButtons = new String [buttons.size()];
            String[] sActionCommands = new String [buttons.size()];
            for (int j = 0; j < buttons.size(); j++) {
              sButtons[j] = (String)buttons.elementAt(j);
              sActionCommands[j] = (String)actionCommands.elementAt(j);
            }
            //String pressedButton =
              guiAPI.showMessage(windowId, msgName, title, type, cmd, sButtons, sActionCommands);
            /*
            Element retButton = rootNode.addElement("MessageButton");
            retButton.setAttribute("Id", windowId);
            retButton.setAttribute("Name", msgName);
            retButton.addText(pressedButton);
            */
          }
        } break;

        case SHOW_WINDOW: {
          windowId = ele.getAttributeValue("Id");
          guiAPI.showWindow(windowId);
        } break;

        case START: {
          guiAPI.start();
        } break;
        case SET_PING_INTERVAL: {
          String interval = ele.getAttributeValue("Seconds");
          guiAPI.setPingInterval(interval);
        }
        break;
        case GET_DATASET_VALUES: {
           windowId = ele.getAttributeValue("Id");
           JDataSet ds = guiAPI.getDatasetValues(windowId);
           Element tmp = rootNode.addElement("GetAllValues");
           tmp.setAttribute("Id", windowId);
           tmp.setText(ds.toString());
        } break;
        case SET_DATASET_VALUES: {
           windowId = ele.getAttributeValue("Id");
           String sds = ele.getTextString();
           JDataSet ds = new JDataSet(sds);
           guiAPI.setDatasetValues(windowId, ds);
        } break;
      } // End Switch
    } // End For ChildNodes of "Do"

    // prüfen, ob Replay vorlag
    if (rootNode.hasChildren() == false) {
      ret = null;
    }
    return ret;
  }
  /**
  Schleift die ChildNodes dieses Knotens durch und verwandelt den Inhalt
  ihrer Textknoten in einen Vector; die XML-Tags dieser ChildNodes werden dabei
  glatt ignoriert.
  <br>
  Wird besonders für Tabellen, Combo- und Listeboxen ben�tigt.
  <br>
  Dabei werden die Strings "true" und "false" in ein Boolean verwandelt.
  */
  private static Vector getCols(Element ele) {
    Element col;
    String val;

    Elements list = ele.getElements();
    if (list.size() == 0) {
      return null;
    }
    Vector<Object> v = new Vector<Object>(list.size());
    while (list.hasMoreElements()) {
      col = list.next();
      val = col.getTextString();
      // @changed: jens hauptmann
      if(val==null)val="";
      // end changed jh 20.02.02
      if (val.equals("true") || val.equals("false")) {
        v.addElement(Boolean.valueOf(val));
      } else {
        v.addElement(val);
      }
    }
    return v;
  }
  private void getMessageButtons(Element ele, Vector<String> buttons, Vector<String> actionCommands) {
    Element btns;
    Element eleBtn;
    //String val;

    Elements list = ele.getElements();
    while (list.hasMoreElements()) {
      btns = list.next();
      if (btns.getName().equals("Buttons")) {
        Elements buttonList = btns.getElements();
        while (buttonList.hasMoreElements()) {
          eleBtn = buttonList.next();
          if (eleBtn.getName().equals("Button")) {
            String label = eleBtn.getAttributeValue("Label");
            String actionCommand = eleBtn.getAttributeValue("ActionCommand");
            buttons.addElement(label);
            actionCommands.addElement(actionCommand);
          }
        }
      }
    }
  }
  private void setValues(Element ele) {
    String windowId = ele.getAttributeValue("Id");
    String type = ele.getAttributeValue("Type");
    String tab = ele.getAttributeValue("Tab");
    Elements list = ele.getElements();
    if (type.equals("NameValuePair")) {
      HashMap<String, String> _hash = new HashMap<String, String>();
      while (list.hasMoreElements()) {
        Element sv = list.next();
        String name = sv.getAttributeValue("Name");
        String value = sv.getTextString();
        _hash.put(name, value);
      } // end for
      if (tab != null) {
        guiAPI.setValues(windowId, tab, _hash);
      } else {
        guiAPI.setAllValues(windowId, _hash);
      }
    } else if (type.equals("List")) {
      Vector<String> v = new Vector<String>();
      while (list.hasMoreElements()) {
        Element sv = list.next();
        String value = sv.getTextString();
        v.addElement(value);

      } // end for
      if (tab != null) {
        guiAPI.setValues(windowId, tab, v);
      } else {
        guiAPI.setValues(windowId, null, v);
      }
    } else if (type.equals("XMLDocument")) {
      Document doc = new Document();
      doc.setEncoding( "UTF-8" );
      Element sv = list.first();
      doc.setRoot((Element)sv.clone());
      if (tab != null) {
        guiAPI.setAllValuesXml(windowId, tab, doc);
      } else {
        guiAPI.setAllValuesXml(windowId, doc);
      }
    }
  }
  private void setTableValues(Element ele) {
    String windowId = ele.getAttributeValue("Id");
    String tab = ele.getAttributeValue("Tab");
    String tbl = ele.getAttributeValue("Table");
    Vector<Vector> data = new Vector<Vector>();
    // Schleife Zeilen
    Elements rows = ele.getElements();
    while (rows.hasMoreElements()) {
      Element rowNode = rows.next();
      Vector<Object> rowVector = new Vector<Object>();
      data.addElement(rowVector);
      Elements cols = rowNode.getElements();
      while (cols.hasMoreElements()) {
        Element colNode = cols.next();
        String value = colNode.getTextString();
        if (value.equals("true") || value.equals("false")) {
          rowVector.addElement(Boolean.valueOf(value));
        } else {
          rowVector.addElement(value);
        }
      }
    }
    if (tab == null) {
      guiAPI.setTableValues(windowId, tbl, data);
    } else {
      guiAPI.setTableValues(windowId, tab, tbl, data);
    }
  }
}