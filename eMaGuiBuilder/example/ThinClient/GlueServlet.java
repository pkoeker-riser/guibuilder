import electric.xml.*;

import electric.util.log.Log;

import java.util.Vector;

import de.guibuilder.server.GuiUserEventIF;
import de.guibuilder.server.GuiXAPI;
import de.guibuilder.server.XmlDatabaseImpl;

public class GlueServlet implements GuiUserEventIF {
  // Atributes
  private boolean debug;
  private GuiXAPI api = new GuiXAPI();
  private String documentBase = ".";
  private XmlDatabaseImpl base = null;
  private String databaseFilename = "adrsbase.xml";
  private String currentTab = null;
  private String currentValue = null;
  private int adrsId = -1;
  private boolean istNeu = true;
  // Constructor
  /**
   * @param docBase Documentbase für Gui-Scripte (relative URL zur Codebase!)
   * @param database Database Filename
   */
  public GlueServlet(String docBase, String database) {
    this.documentBase = docBase;
    this.databaseFilename = database;
    guiInit();
  }
  // Methods
  private void guiInit() {
      base = new XmlDatabaseImpl(databaseFilename);
      System.err.println("Database initialized: " + databaseFilename);
  }
  public void setDebug(boolean b) {
    debug = b;
    api.setDebugMode(b);
  }
  // From Interface GuiUserEventIF ********************************************
  /**
  Teilt dem Server mit, daß ein neuer Client gestartet wurde.
  Üblicherweise erwartet der Client jetzt eine Initialisierung seiner Umgebung,
  sowie die Anweisung, welcher Dialog (z.B. Login) als erstes zu starten ist.
  */
  public String started() {
    System.out.println("Client Started");

    // Mit XAPI
    api.startTrans();
    api.start();
    api.setDocumentBase(documentBase);
    api.setUiManager("Windows");
    api.createWindow("AdressBeispiel", "1", "AdressBeispiel.xml");
    api.resetWindow("1");
    api.enable("1", "loeschen", false);

    Vector anr = new Vector();
    anr.addElement("Herr");
    anr.addElement("Frau");
    anr.addElement("Firma");
    anr.addElement("Familie");
    anr.addElement("Rechtsanwälte");
    api.setItems("1", "anrede", anr);
    api.setValue("1", "anrede", "Herr");
    api.showWindow("1");
    api.end();
    Document ret = api.endTrans();
    if (debug) System.out.println(ret.toString());
    return ret.toString();
  }
  /**
  Ein Fenster wurde erstmalig angezeigt.
  */
  public String windowOpened(String windowId, String windowName, String cmd) {
    if (debug) System.out.println("WindowOpened: "+windowId);
    Document ret = createDoc();

    return ret.toString();
  }
  /**
  Der Benutzer hat die Controlbox des Fensters angeklickt,
  d.h. er möchte das Fenster schließen.
  Die Dialogsteuerung sollte daraufhin des Fenster tatsächlich schließen,
  oder eine Fehlermeldung ausgeben, wenn die Voraussetungen für das Schließen
  des Fensters nicht gegeben sind.
  @see de.guibuilder.adapter.GuiAPI#closeWindow
  @see de.guibuilder.adapter.GuiAPI#hideWindow
  @see de.guibuilder.adapter.GuiAPI#showMessage
  */
  public String windowClosed(String windowId, String windowName, String cmd) {
    if (debug) System.out.println("WindowClosed: "+windowId);
    Document ret = createDoc();

    return ret.toString();
  }
  /**
  Der Benutzer hat ein anderes Fenster angeklickt;
  diese Nachricht wird implizit auch dann ausgelöst,
  wenn das Fenster auf Anweisung der Dialogsteuerung neu erstellt
  oder aktiviert wurde.
  <BR>
  Diese Nachricht soll die Dialogsteuerung lediglich über den
  vom Benutzer vorgenommenen Kontextwechsel informieren;
  sie kann also auch ignoriert werden.
  <P>
  <strong>Achtung!</strong><BR>
  Wenn ein Fenster neu erstellt wird, wird <i>zuerst</i> windowActivated und
  <i>danach</i> windowOpened gesendet!
  */
  public String windowActivated(String windowId, String windowName, String cmd) {
    if (debug) System.out.println("WindowActivated: "+windowId);
    Document ret = createDoc();
    return ret.toString();
  }
  /**
  Der Benutzer hat eine andere Registerkarte eines Windows angeklickt.
  Diese Nachricht wird auch dann ausgelöst, wenn die Registerkarte auf Anweisung
  der Dialogsteuerung aktiviert wurde.
  <BR>
  Es ist nicht vorgesehen, daß Registerkartensätze auch geschachtelt werden können.
  <BR>
  Diese Nachricht soll die Dialogsteuerung lediglich über den
  vom Benutzer vorgenommenen Kontextwechsel informieren;
  sie kann also auch ignoriert werden.
  */
  public String tabSelected(String windowId, String tabName, String cmd, int index) {
    if (debug) System.out.println("TabSelected: "+windowId+":"+tabName);
    currentTab = tabName;

    return null;
  }
  public String messageBoxEvent(String windowId, String name, String cmd) {
    Document ret = createDoc();
    if (windowId.equals("1") && name.equals("adresseLoeschen") && cmd.equals("JaAction")) {
      base.remove(adrsId);
      api.startTrans();
      api.resetWindow("1");
      ret = api.endTrans();
      istNeu = true;
    }
    return ret.toString();
  }
//  public String lostFocus(String windowId, String tabName, String name, String actionCommand, String value) {
//    if (debug) System.out.println("LostFocus: "+windowId+":"+tabName+":"+name+":"+actionCommand+"/"+value);
//    Document ret = createDoc();
//
//    return ret.toString();
//  }
  public String actionPerformed(String windowId, String name, String cmd, String allValues) {
    Document ret = createDoc();

    return ret.toString();
  }
  /**
   * Der Benutzer hat eine implizite Aktion ausgelöst (Button, Tool, MenuItem).
   */
  public String actionPerformed(String windowId, String name, String cmd) {
    if (debug) System.out.println("Action: "+windowId+":"+name+":"+cmd);
    Document ret = null;
    if (cmd.equals("Einzelbrief")) {
      api.startTrans();
      api.start();
      api.openWindow("DokumentVorlage", "2", "Einzelbrief.xml");
      api.end();
      ret = api.endTrans();
    }
    else if (windowId.equals("2") && cmd.equals("ok")) {
      api.startTrans();
      api.start();
      api.closeWindow("2");
      api.openWindow("TextEditor", "3", "TextEditor.xml");
      api.end();
      ret = api.endTrans();
    }
    else if (cmd.equals("cancel")) {
      api.startTrans();
      api.closeWindow(windowId);
      ret = api.endTrans();
    }
    else if (cmd.equals("open")) {
      api.startTrans();
      api.start();
      api.createWindow("AdressAuswahl", "10", "AdressAuswahl.xml");
      Document sel = base.select("adressen", "name1,strasse,plz,ort,telefon1");
      api.setAllValuesXml("10", sel);
      api.showWindow("10");
      api.end();
      ret = api.endTrans();
    }
    else if (cmd.equals("exit")) {
      if (windowId.equals("1")) {
        api.startTrans();
        api.exit();
        ret = api.endTrans();
        base.save();
      } else {
        api.startTrans();
        api.closeWindow(windowId);
        ret = api.endTrans();
      }
    }
    else if (cmd.equals("new")) {
      api.startTrans();
      api.resetWindow("1");
      api.enable("1", "loeschen", false);
      ret = api.endTrans();
      istNeu = true;
    }
    else if (cmd.equals("Abbrechen")) {
      if (windowId.equals("1")) {
        api.startTrans();
        api.closeWindow(windowId);
        api.end();
        ret = api.endTrans();
      }
    }
    else if (cmd.equals("save")) {
      api.startTrans();
      api.verifyWindow("1");
      api.getAllValues("1");
      ret = api.endTrans();
    }
    else if (cmd.equals("delete")) {
      api.startTrans();
      api.showMessage("1", "adresseLoeschen", "Daten löschen", "Ask", "Sollen die Daten gelöscht werden?" , new String[] {"Ja", "Nein", "Abbrechen"}, new String[] {"JaAction", "NeinAction", "AbbrechenAction"});
      ret = api.endTrans();
    }
    else if (cmd.equals("cut")) {
      api.startTrans();
      api.selectPanel("1", null, false);
      ret = api.endTrans();
    }
    else if (cmd.equals("copy")) {
      api.startTrans();
      api.selectPanel("1", null, true);
      ret = api.endTrans();
    }
    else if (cmd.equals("paste")) {
      api.startTrans();
      api.setFocus("1", "tabPersonen", "tblPersonen", 2, 1);
      api.setCellValue("1", "tabPersonen", "tblPersonen", "Beratung", 2, 1);
      ret = api.endTrans();
    }
    else if (windowId.equals("10") && cmd.equals("ok")) {
      api.startTrans();
      api.closeWindow(windowId);
      Document adrs = base.get(adrsId);
      api.resetWindow("1");
      api.setAllValuesXml("1", adrs);
      api.enable("1", "loeschen", true);
      ret = api.endTrans();
      istNeu = false;
    } else if (cmd.equals("personEinfuegen")) {
      api.startTrans();
      api.insertRow("1", "tabPersonen", "tblPersonen");
      ret = api.endTrans();
    }
    else if (cmd.equals("personLoeschen")) {
      api.startTrans();
      api.deleteRow("1", "tabPersonen", "tblPersonen");
      ret = api.endTrans();
    }
    else if (cmd.equals("notizEinfuegen")) {
      api.startTrans();
      api.insertRow("1", currentTab, "tblNotizen");
      ret = api.endTrans();
    }
    else if (cmd.equals("notizLoeschen")) {
      api.startTrans();
      api.deleteRow("1", currentTab, "tblNotizen");
      ret = api.endTrans();
    }

    else if (cmd.equals("terminEinfuegen")) {
      api.startTrans();
      api.insertRow("1", currentTab, "tblTermine");
      ret = api.endTrans();
    }
    else if (cmd.equals("terminLoeschen")) {
      api.startTrans();
      api.deleteRow("1", currentTab, "tblTermine");
      ret = api.endTrans();
    }
    else if (cmd.equals("select")) {
      api.startTrans();
      api.addItem("1", "tabSchlagworte", "auswahlSchlagworte", currentValue);
      ret = api.endTrans();
    }

    else if (cmd.equals("deselect")) {
      api.startTrans();
      api.removeItem("1", "tabSchlagworte", "auswahlSchlagworte", currentValue);
      ret = api.endTrans();
    }
    /*
    if (ret != null) {
      System.out.println(ret.toString());
    } else  {
      System.out.println("Null??");
    }
    */
    if (debug && ret != null) System.out.println(ret.toString());
    return ret != null ? ret.toString() : null;
  }
  public String changed(String windowId, String name, String actionCommand, Object value, int index) {
    if (debug) System.out.println("Changed: "+windowId+":"+name+":"+actionCommand+":"+value+":"+index);
    currentValue = value.toString();
    return null;
  }
  public String dblClick(String windowId, String name, String actionCommand, Object value, int index) {
    if (debug) System.out.println("DblClick");
    return null;
  }

  /**
  Tabelle auf einer Registerkarte.
  */
  public String tableRowClick(String windowId, String tableName, String cmd, Vector values, int row) {
    if (debug) System.out.println("TableRowSelected: "+windowId+":"+tableName+":"+row);
    adrsId = row;
    return null;
  }
  public String tableHeaderClick(String windowId, String tableName, String cmd, int col) {
    if (debug) System.out.println("TableHeaderClick: "+windowId+":"+tableName+":"+col);
    return null;
  }
  public String tableDblClick(String windowId, String tableName, String cmd, Vector values, int row) {
    if (debug) System.out.println("TableRowDblClick: "+windowId+":"+tableName+":"+row);
    adrsId = row;
    return null;
  }
  public String treeNodeSelected(String windowId, String treeName,String cmd,  String path) {
    if (debug) System.out.println("NodeSelected: "+windowId+":"+treeName+":"+path);
    Document ret = createDoc();

    return ret.toString();
  }
  public String ping() {
    System.out.println("Ping");
    Document ret = createDoc();

    return ret.toString();
  }
  public String replay(String xml) {
    if (debug) System.out.println("Replay");
    Document doc = null;
    try {
      doc = new Document(xml);
    } catch (ParseException ex) {
      ex.printStackTrace();
      return null;
    }
    Element root = doc.getRoot();
    Elements nl = root.getElements();
    String msg = null;
    String ret = null;
    String windowId = null;
    String name = null;
    while (nl.hasMoreElements()) {
      Element ele = nl.next();
      msg = ele.getName();
      windowId = ele.getAttributeValue("Id");
      name = ele.getAttributeValue("Name");
      // GetAll Values
      if (msg.equals("GetAllValues")) {
        if (windowId.equals("1")) {
          Document save = new Document();
          save.setEncoding( "ISO-8859-1" );
          Elements nle = ele.getElements();
          while (nle.hasMoreElements()) {
            Element panel = nle.next();
            save.setRoot((Element)panel.clone());
            if (istNeu) {
              base.insert(save);
              api.startTrans();
              api.enable("1", "loeschen", true);
              ret = api.endTrans().toString();
              istNeu = false;
            } else {
              base.update(adrsId, save);
            }
          } // wend
        } // end if "1"
      }
      else if (msg.equals("GetValues")) {
        windowId = ele.getAttributeValue("Id");
        // TO DO
      }
      else if (msg.equals("GetAllModifiedValues")) {
        windowId = ele.getAttributeValue("Id");
        // TO DO
      }
      else if (msg.equals("GetValue")) {
        windowId = ele.getAttributeValue("Id");
        // TO DO
      }
      else if (msg.equals("GetMemberNames")) {
        windowId = ele.getAttributeValue("Id");
        // TO DO
      }
      /*
      else if (msg.equals("MessageButton")) {
        if (windowId.equals("1") && ele.getTextString().equals("JaAction")) {
          base.remove(adrsId);
          api.startTrans();
          api.resetWindow("1");
          ret = api.endTrans().toString();
          istNeu = true;
        }
      }
      */
      else {
        System.out.println("Unknown Replay: "+msg);
      }
    } // Wend messages
    if (debug && ret != null) System.out.println(ret.toString());
    return ret;

  }
  private Document createDoc() {
    Document doc = new Document();
    doc.setEncoding( "ISO-8859-1" );
    // Create Root Element
    doc.setRoot("Do");
    return doc;
  }
}
