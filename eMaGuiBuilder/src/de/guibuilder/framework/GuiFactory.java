package de.guibuilder.framework;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.l2fprod.common.swing.JTaskPaneGroup;

import de.guibuilder.adapter.ComboBoxAdapterIF;
import de.jdataset.JDataSet;
import de.pkjs.util.Convert;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;

/**
 * Implementierung einer Factory zur Generierung von Java-Oberflächen.
 * Siehe besonders die Methode "createWindow()".
 * Es werden weitere Methoden zur Unterstützung der Generierung zur
 * Verfügung gestellt.
 */
 /*
 Vorgehen bei der Erweiterung des Sprachumfangs:
 Es ist zu unterscheiden zwischen Elementen und Attributen.
 Elemente bezeichnen eine Oberflächen-Komponente
 Attribute bezeichnen die Eigenschaften einer Komponente
 1. Eine Konstante definieren:
   private static final int keyCALENDAR = 299;
 1.1 Die Konstante in die Map des Sprachumfangs eintragen:
  hash.put("Calendar", new Integer(keyCALENDAR));
 2. Neue Klasse in das Framework aufnehmen.
 Zuerst ist festzulegen, in welcher Stelle der Vererbunghierarchie
 die neue Komponente eingebaut werden soll.
 Zentrale abstrakte Klassen sind
 - GuiComponent für alle die Oberflächenelemente die Daten halten
 - GuiAction für alle Oberflächenkomponenten die eine Benutzeraktion auslösen
 - GuiContainer für Behälter von Komponenten.
 - GuiTable und GuiTree erben zwar auch von GuiComponent, sind aber sehr komplex
 aufgebaut.
 In unserem Beispiel-Kalender erben wir also von GuiComponent.

  package de.guibuilder.framework;

  public class GuiCalendar extends GuiComponent {
    ...
  }
 Es müssen die abstrakten Methoden implementiert werden.

 3. Eine Methode schreiben, die die Komponente erzeugt und in die
 Oberfläche aufnimmt.
 Diese Methoden heißen alle perf...
  private static void perfCalendar(CurrentKeyword curKey, CurContext c) {
    ...
  }

 Den Aufruf dieser Methode in der Factory organisieren.
        case keyCALENDAR:
          perfCalendar(curKey, ctx);
        break;

 4. GDL.DTD anpassen
 5. KeywordAttributes.properties anpassen
 6. Dokumentation anpassen
 */

 /*
 * <pre>
 * To Do
 * - Umstieg auf XML-Schema mit Validierung (über Xerces?)
 * - Default-Values für Attribute ins Schema
 * - User defined Widgets (mit / ohne Vererbung)
 * - Beautyfier für GridBagConstraints
 * - TableLayout
 * - Map bei ComboBox unschön
 * - defaultValue für reset
 * - XML direkt verarbeiten (GDL vorher in XML umwandeln)
 * - Plugin für jEdit / geht nicht, weil dtd nicht gefunden wird :-(
 *
 * - Relative Anordnung von Komponenten: bei || x; sonst y (Siehe REL und REM)
 * - Defaults für it= und ib= übernehmen, wenn selbe Zeile
 * - Navigator bei GuiMain
 * - Bean-Framework; bei rechter Maustaste im Editor Eingabe der Attribute
 * - Attribut "Font" (font=[fontname,style,size])
 * - Der Editor kann noch verbessert werden...
*/

/*
Done
* - ButtonGroup für RadioButton:
* <ButtonGroup name="myName" OnChange="changed">
*    <Option ... />
 * - RootPane für Menus anders organisieren:
 * RootPane immer vom ParentMenu holen; zuletzt von Menubar
 * - minLen/maxLen (Text) minValue/maxValue (bei Text abstract)
 * - Verify
- Events spezifizieren zur Weiterleitung an Controller (OnLostFocus=ActionCommand)
- Messageboxen fehlen (siehe GuiAPI)
- Bei XML-Parser-Fehler Cursor in die Zeile mit dem Fehler setzen.

- überschreibbare default-Einstellungen der Attribute aller Klassen erstellen. (OK)
Anforderungen:
- Die bisher fest codierten Default-Einstellungen der Keywords werden in
einer Textdatei abgelegt (XML? GDL?). Siehe Repository.txt (OK)
- Diese Liste kann vom Anwender beliebigt editiert werden. (OK)
- Es können auch abgleitete Klassen erstellt werden. (OK)

- Bei einer Spezifikation können die Attribute überladen werden. (OK)
- InsertRow() und DeleteRow() geht nicht richtig wenn mehr als eine Tabelle
- DTD für XML
- Code-Generator (in Java? VB?)
- Text-Editor (erste Version)
- Tabellen sind zu hoch; Mindestgröße korrigieren? (Mit JDK 1.2 erledigt)
- Attribut "Package" (pack=) zu Form und Dialog für Code-Generierung?(nicht nötig!)
- Zugriff auf Datenbank
- HelpTopics zu Objekten und Helpfile im Browser anzeigen.
- Packagestruktur definieren; jar-File
- Dialoge auch model und nicht größenänderbar setzen
- file= für Combo- und ListBox
- Schnittstelle zu Rose
- Attribut "Reference" (ref=) als Verweis auf Attribute oder Methoden der Serverklassen.
- Attribute "Name" (name=) für Generierung der Namen von Klassen und Attributen
- Attribut "Color" (sb= sf=)
- "überladen" der Attribute bei Use geht nicht (w,h,wx,wy) (nur bei Child Container)
- FileDialog
- Split Panel
- Tabsets können auch geschachtelt werden (max 8)
- Tabellenspalten auch do=y (OK)
- Table mit Date, Time, Money, Number, Check, Combo
- GuiDate, GuiMoney, GuiTime, GuiNumber
- Dialoge (OK; Buttons OK und Abbrechen fehlen; OK)
- BEGIN CASE/ END CASE
- Images gehen nicht im Browser (OK, wenn Classes gezipt mit Archive)
- Parser verbessern: Keywords in HashMap (OK!)
- Es geht nur eine ComboBox je Tabelle (OK)
- Breite der Spalten der Tabelle fehlt (w=) (OK!)
- Statuszeile mit Statuszeilentext (OK!)
- Dialogablauf (Menü, Button, Tool) (OK!)
- Toolbar fehlt (OK!)
- CheckBoxMenuItem (OK!)
- PopupMenu bei rechter Maustaste (Tree, Table) (OK!)
- Alle Objekte mit Icon wo sinnvoll (Menü (OK!), Combobox, Button(OK!), Tool (OK!), ..)
  Tree hat bei Swing keine Icons!
- Mnemonics setzen wo's geht (Tool (OK!), Menü (OK!), Button (OK!), Label (geht nicht!))
- Toolbar nur anlegen, wenn erstes Tool definiert wird (OK!)
- Ausrichtung von Spalten der Tabelle setzen; al=L|R|C  (OK; ohne CellEditor)
</pre>
*/

public final class GuiFactory  {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GuiFactory.class);
	/**
	 * Singleton
	 */
  private static volatile GuiFactory me;
  // MBean Start
  private final Date dateCreated = new Date();
  private int numberOfWindowsCreated;
  private long totalTimeUsed; // Number of millies
  // MBean End
  /**
   * Um diesen Wert werden die automatisch erzeugten Labels horizontal
   * verbreitert (ipadX).
   */
  private static final int LPX = 8;
  /**
   * Hack: Der zuletzt verwendete Filename wenn XML für Pnuts-Compiler
   */
  private String currentFilename;
  /**
   * Kennzeichen, ob das mit den Komponenten automatisch erstellte Label
   * rechts- oder linksbündig ist.<p>
   * Siehe GuiBuilder.properties "Label_Anchor"
   */
  private static boolean labelAnchorEast = true;
  private static HashMap<String, Integer> anchorMap;
  private static HashMap<String, Integer> alignMap;
  private static HashMap<String, Integer> fillMap;
  private static HashMap<String, Integer> keyMap;
  private static HashMap<String, PluginKeyword> pluginsMap;
  // static Keywords
  // Offset additional Keywords is 1000!
  private static final int keyBEGIN_APPLET = 1;
  private static final int keyEND_APPLET = 2;
  private static final int keyBEGIN_FORM = 3;
  private static final int keyEND_FORM = 4;
  private static final int keyBEGIN_DIALOG = 5;
  private static final int keyEND_DIALOG = 6;
  private static final int keyBEGIN_PANEL = 7;
  private static final int keyEND_PANEL = 8;
  private static final int keyBEGIN_GROUP = 9;
  private static final int keyEND_GROUP = 10;
  private static final int keyBEGIN_TABSET = 11;
  private static final int keyEND_TABSET = 12;
  private static final int keyBEGIN_TAB = 13;
  private static final int keyEND_TAB = 14;
  private static final int keyBEGIN_TABLE = 15;
  private static final int keyEND_TABLE = 16;
  private static final int keyBEGIN_TOOLBAR = 17;
  private static final int keyEND_TOOLBAR = 18;
  private static final int keyBEGIN_MENUBAR = 19;
  private static final int keyEND_MENUBAR = 20;
  private static final int keyBEGIN_MENU = 21;
  private static final int keyEND_MENU = 22;
  private static final int keyBEGIN_POPUP = 23;
  private static final int keyEND_POPUP = 24;
  private static final int keyBEGIN_TREE = 25;
  private static final int keyEND_TREE = 26;
  private static final int keyBEGIN_FOLDER = 27;
  private static final int keyEND_FOLDER = 28;
  private static final int keyBEGIN_FRAME = 29;
  private static final int keyEND_FRAME = 30;
  private static final int keyBEGIN_SPLIT = 31;
  private static final int keyEND_SPLIT = 32;
  private static final int keyBEGIN_BOX = 33;
  private static final int keyEND_BOX = 34;
  private static final int keyBEGIN_ELEMENT = 35;
  private static final int keyEND_ELEMENT = 36;
  private static final int keyBEGIN_OPTION_GROUP = 37;
  private static final int keyEND_OPTION_GROUP = 38;

  private static final int keyBEGIN_BUTTONBAR = 39;
  private static final int keyEND_BUTTONBAR = 40;
  private static final int keyBEGIN_BUTTONBAR_BUTTON = 41;
  private static final int keyEND_BUTTONBAR_BUTTON = 42;

  private static final int keyBEGIN_OUTLOOKBAR = 43;
  private static final int keyEND_OUTLOOKBAR = 44;
  private static final int keyBEGIN_OUTLOOKBAR_TAB = 45;
  private static final int keyEND_OUTLOOKBAR_TAB = 46;
  private static final int keyBEGIN_OUTLOOKBAR_BUTTON = 47;
  private static final int keyEND_OUTLOOKBAR_BUTTON = 48;

  private static final int keyBEGIN_TASKPANE = 49;
  private static final int keyEND_TASKPANE = 50;
  private static final int keyBEGIN_TASKPANE_TAB = 51;
  private static final int keyEND_TASKPANE_TAB = 52;
  private static final int keyBEGIN_TASKPANE_BUTTON = 53;
  private static final int keyEND_TASKPANE_BUTTON = 54;
  
  private static final int keyBEGIN_CHART = 55;
  private static final int keyEND_CHART = 56;
  private static final int keyBEGIN_BROWSER = 57;
  private static final int keyEND_BROWSER = 58;
  private static final int keyBEGIN_JFX = 59;
  private static final int keyEND_JFX = 60;
  
  // Components
  private static final int keyTEXT = 100;
  private static final int keyDATE = 101;
  private static final int keyTIME = 102;
  private static final int keyMONEY = 103;
  private static final int keyNUMBER = 104;
  private static final int keyPASSWORD = 105;
  private static final int keyMEMO = 106;
  private static final int keyCOMBO = 107;
  private static final int keyLIST  = 108;
  private static final int keyCHECK = 109;
  private static final int keyOPTION = 110;
  private static final int keyLABEL = 111;
  private static final int keyBUTTON = 112;
  private static final int keyTBUTTON = 113;
  private static final int keySCROLLBAR = 114;
  private static final int keySLIDER = 115;
  private static final int keyXFILLER = 116;
  private static final int keyYFILLER = 117;
  private static final int keyEDITOR = 118;
  private static final int keyCONTENT = 119;
  private static final int keyDOCUMENT = 120;
  private static final int keySPIN = 121;
  private static final int keyTITLE = 122;
  private static final int keyCALENDAR = 123;
  private static final int keyCALENDAR_POPUP = 124;
  // special components
  private static final int keyITEM = 200;
  private static final int keyITEM_CHECK = 201;
  private static final int keyITEM_OPTION = 202;
  private static final int keyTOOL = 203;
  private static final int keySEPARATOR = 204;
  private static final int keyNODE = 205;
  private static final int keyROW  = 206;
  private static final int keyCOLUMN  = 207;
  private static final int keyHIDDEN = 208;
  private static final int keyBORDER = 209;
  private static final int keySTATE = 210;
  private static final int keyTIMER = 211;
  // special statements
  private static final int keyUSE  = 300;
//  private static final int keyBEGIN_CASE = 301;
//  private static final int keyEND_CASE = 302;
  // Scripting
  static final int keySCRIPT = 310;
  static final int attLANGUAGE = 311;
  static final int attSRC = 312;
  // Documentation
  static final int keyDESCRIPTION = 320;
  // Attributes
  private static final int attX = 501;  // GridBagConstraints
  private static final int attY = 502;
  private static final int attH = 503;
  private static final int attW = 504;
  private static final int attIR = 505;
  private static final int attIL = 506;
  private static final int attIT = 507;
  private static final int attIB = 508;
  private static final int attWX = 509;
  private static final int attWY = 510;
  private static final int attPX = 512;
  private static final int attPY = 513;
  private static final int attAN = 514;
  private static final int attFILL = 515;
  private static final int attCOLS = 516; // Columns (Text Components)
  static final int attITEMS = 517;  // Item List
  private static final int attDO = 518;		// DisplayOnly
  private static final int attNN = 519;		// NotNull
  static final int attTT = 520;		// ToolTipText
  static final int attST = 521;		// StatusText
  private static final int attVAL = 522;	// Initial Value
  public static final int attFILE = 523;	// File / Method --> GuiDoc
  private static final int attCMD = 524;	// ActionCommand
  private static final int attIMG = 525;	// ImageIcon
  private static final int attSICON = 526;	// ImageIcon
  private static final int attMN = 527;		// Mnemonic Char
  private static final int attAL = 528;		// Alignment
  private static final int attTOPIC = 529;	// HelpTopic
  private static final int attSB = 530;		// SetBackgroundColor
  private static final int attSF = 531;		// SetForegroundColor
  private static final int attLSB = 532;	// SetBackgroundColor/Label
  private static final int attLSF = 533;	// SetForegroundColor/Label
  private static final int attPOINT = 534; // FontSize
  private static final int attSTYLE = 535; // FontStyle
  private static final int attFONT = 536;	// FontName
  private static final int attACC = 537; 	// Menu Accelerator
  private static final int attTYPE = 538; 	// Border Type of Group
  private static final int attMIN = 539; 	// ColMinWidth
  private static final int attMAX = 540; 	// ColMaxWidth
  private static final int attNAME = 541; // Name of component
  private static final int attREF = 542; 	// Reference to Business Logic
  private static final int attSIZE = 543; // Preferred Size
  private static final int attMINSIZE = 544; 	// Minimum Size
  private static final int attMAXSIZE = 545; 	// Maximum Size
  private static final int attTABSTOP = 546; 	// true|false
  private static final int attVISIBLE = 547; 	// true|false
  private static final int attLINKCOL = 548; 	// Table-Link-Component
  private static final int attLINKTABLE = 549; 	// Container
  private static final int attFORMAT = 550; 	// Text
  private static final int attCLOSED_ICON = 551;	// Tree
  private static final int attOPEN_ICON = 552;  // Tree
  private static final int attLEAF_ICON = 553;  // Tree
  private static final int attGRID0 = 554;  // Panel, Group
  private static final int attMINLEN = 555; // Min Input Length (Text)
  private static final int attMAXLEN = 556; // Max Input Length (Text)
  private static final int attSEARCH = 557; // Search Criteria
  private static final int attREGEXP = 558; // RE
  private static final int attINVERT = 559; // Invert CheckBox
  private static final int attMAP = 560;    // Map ComboBox
  private static final int attMINVAL = 561; // Slider, ScrollBar
  private static final int attMAXVAL = 562; // Slider, ScrollBar
  private static final int attLAYOUT = 563; // LayoutManager
  static final int attRB = 564; // ResourceBundle
  private static final int attNODE_TITLE = 565; // Text --> TreeNode.setTitle
  private static final int attPACK = 566; // Package
  private static final int attCOLSPEC = 567; // Form-Layout
  private static final int attROWSPEC = 568; // Form-Layout
  private static final int attROWHEIGHT = 569; // JTable#rowHeight
  private static final int attCLOSEABLE = 570;
  private static final int attICONABLE = 571;
  private static final int attMAXABLE = 572;
  private static final int attDRAG = 573;
  private static final int attAUTOSIZE = 574;
  private static final int attRESTORE = 575;
  private static final int attHELPID = 576;
  private static final int attUI = 577;
  private static final int attBORDER = 578;
  private static final int attOG_NAME = 579;
  private static final int attDELAY = 580;
  private static final int attENABLED_WHEN = 581;
  
  private static final int attENABLED = 400;

  // Bindings
  private static final int attCONTROLLER = 581; // Controler-Class für Form, Dialog
  private static final int attROOT_ELEMENT = 582;	// Window
  private static final int attELEMENT = 583;	// Element
  private static final int attDATASET = 584;
  private static final int attDISPLAY_MEMBER = 585; // Combo
  private static final int attVALUE_MEMBER = 586; 	// Combo
  private static final int attRO = 587;     // ReadOnly; für getDatasetValues()

  private static final int attHTP = 590; // HorizontalTextPosition (Label und Checkbox)
  private static final int attLOC = 591; // Location (Split#dividerLocation)

  // Messages *******************************************
  static final int msgCREATE = 600; // see CurrentKeyword
  private static final int msgOPEN = 601;   // Window
  private static final int msgCLOSE = 602;  // Window
  private static final int msgACTIVE = 603; // Window, Tabset
  private static final int msgCLICK = 604; // Label
  private static final int msgDBLCLICK = 605;
  private static final int msgMOUSEOVER = 606;
  private static final int msgMOUSEMOVE = 607;
  //private static final int msgGOTFOCUS = 608;
  private static final int msgLOSTFOCUS = 609;
  private static final int msgCHANGE = 610;
  private static final int msgCOLHEADERCLICK = 611;
  //private static final int msgCOLHEADERDBLCLICK = 612;
  private static final int msgROWCLICK = 613;
  private static final int msgNODECLICK = 614;
  // DragDrop
  private static final int msgDRAG_ENTER = 615;
  private static final int msgDRAG_OVER = 616;
  private static final int msgDRAG_EXIT = 617;
  private static final int msgDROP = 618;
  private static final int msgFILEDROP = 619;

  private static final int msgKEYTYPED = 620; // Text
  //private static final int msgKEYINSERT = 621;
  //private static final int msgKEYDELETE = 622;

  private static final int msgPRE_SORT = 625;
  private static final int msgPOST_SORT = 626;
  private static final int msgPOPUP = 630;
  /**
   * Diese HashMap umfaßt die Schlüsselworte zum Sprachumfang.
   */
  private static HashMap<String, Integer> hash;
  private long lastTime = 0; // Zeit messen
  private long totalTime = 0;
  // Zähler
  private static int splitCounter;

  /**
   * Resource-Loader, für den Fall das die Dialogbeschreibungen
   * nicht vom Filesystem geladen werden sollen, kann hier
   * eine Loader-Klasse angegeben werden.
   */
  private static Class<?> resLoader = GuiFactory.class;
  // Constructor
  /**
   * Private Constructor.
   */
  private GuiFactory() {
    this.initPlugins();
  }
  /**
   * Singleton
   */
  public static GuiFactory getInstance() {
    if (me == null) {
    	synchronized(GuiFactory.class) {
    		if (me == null) {
    		    me = new GuiFactory();
    		}
    	}
    }
    return me;
  }
  
  public static void setResourceLoader(Class<?> r) {
  	resLoader = r;
  }

  public static Class<?> getResourceLoader() {
  	return resLoader;
  }
  /**
   * Liest die Default-Einstellungen der Komponenten ein.
   */
  void fillDefKeys(String filename) {
    final HashMap<String, CurrentKeyword> _hash = new HashMap<String, CurrentKeyword>();
    CurrentKeyword curKey;
    if (filename == null || filename.equals("NONE") || filename.length() < 3) {
      return;
    }
    final String xs = GuiUtil.fileToString(filename);
    if (xs == null) {
      return;
    }
    ArrayList<CurrentKeyword> keys = null;
    try {
      keys = makeKeywordList(xs);
      for (Iterator<CurrentKeyword> k = keys.iterator(); k.hasNext();) {
        curKey = k.next();
        _hash.put(curKey.sKeyword, curKey);
      }
      CurrentKeyword.setDefKeys(_hash);
    } catch (GDLParseException ex) {
      GuiUtil.showEx(ex);
    }
  }
  /**
   * Erzeugt einen Vector für Combo- Listboxen unter Angabe eines Dateinamens.
   * <BR>
   * Es wird erwartet, daß die Einträge in einer Textdatei zeilenweise
   * untereinander stehen.
   */
  static String[] getItemsFile(String fileName) {

    String s = GuiUtil.fileToString(fileName);
    if (s == null) {
      logger.warn("Missing file: " + fileName);
      return new String[0];
    } else {
   	 String[] items = s.split("\n");
   	 return items;
    }
  }

  /**
   *  Liefert einen Vektor von Einträgen, die hinter dem Attribut "Items=" stehen,
   *  in "" eingeschlossen und durch | getrennt sind.
   */
  private static String[] getItems(String s) {
	  if (s == null) {
		  return new String[0];
	  } 
	  String[] items = s.split("\\|");
	  return items;	  
  } // End of method getItems


  /**
   * übersetzt die Angaben des Attribut an= in GridBagKonstante.
   */
  public static int getAnchor(String val) {
    int ret = GridBagConstraints.NORTHWEST;
    Integer retI = anchorMap.get(val);
    if (retI != null) {
      ret = retI.intValue();
    }
    return ret;
  } // End of method getAnchor
  /**
   * Errechnet die Position eines Fensters anhand der "Himmelsrichtung".
   * Die Größe des Bildschirms wird dabei berücksichtigt.
   * @param val Himmelsrichtung wie bei Anchor (N, NE, NW...)
   * @param w Breite des Fensters
   * @param h Höhe des Fensters
   * @return X und Y-Position für die Location des Fensters.
   */
  private static Point getLocation(String val, int w, int h) {
    final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    // Default: Center
    Point ret = new Point((screen.width-w)/2, (screen.height-h)/2);
    //Integer anchor = (Integer)anchorMap.get(val);
    //switch (anchor.intValue()) {
    if (val.equals("NE")) { ret = new Point(screen.width-w, 0); }
    else if (val.equals("N")) {ret = new Point((screen.width-w)/2 , 0);}
    else if (val.equals("NW")) {ret = new Point(0, 0);}
    else if (val.equals("W")) {ret = new Point(0, (screen.height-h)/2);}
    else if (val.equals("SW")) {ret = new Point(0, screen.height-h);}
    else if (val.equals("S")) {ret = new Point((screen.width-w)/2, screen.height-h);}
    else if (val.equals("SE")) {ret = new Point(screen.width-w, screen.height-h);}
    else if (val.equals("E")) {ret = new Point(screen.width-w, (screen.height-h)/2);}
    //else if (val.equals("C")) {;}
    return ret;
  }
  /**
   * Rechnet den Anchor einer Komponente in den dazugehörigen
   * Anchor für das entsprechende Label um.
   */
  private static int makeLabelAnchor(int an) {
    int ret=GridBagConstraints.NORTHEAST; // default

    switch (an) {
      case GridBagConstraints.NORTHEAST:
        ret = labelAnchorEast ? GridBagConstraints.NORTHEAST : GridBagConstraints.NORTHWEST;
        break;
      case GridBagConstraints.NORTH:
        ret = labelAnchorEast ? GridBagConstraints.NORTHEAST : GridBagConstraints.NORTHWEST;
        break;
      case GridBagConstraints.NORTHWEST:
        ret = labelAnchorEast ? GridBagConstraints.NORTHEAST : GridBagConstraints.NORTHWEST;
        break;
      case GridBagConstraints.WEST:
        ret = labelAnchorEast ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        break;
      case GridBagConstraints.SOUTHWEST:
        ret = labelAnchorEast ? GridBagConstraints.SOUTHEAST : GridBagConstraints.SOUTHWEST;
        break;
      case GridBagConstraints.SOUTH:
        ret = labelAnchorEast ? GridBagConstraints.SOUTHEAST : GridBagConstraints.SOUTHWEST;
        break;
      case GridBagConstraints.SOUTHEAST:
        ret = labelAnchorEast ? GridBagConstraints.SOUTHEAST : GridBagConstraints.SOUTHWEST;
        break;
      case GridBagConstraints.EAST:
        ret = labelAnchorEast ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        break;
      case GridBagConstraints.CENTER:
        ret = labelAnchorEast ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        break;
    }
    return ret;
  }

  /**
   * Errechnet aus dem Attribut fill= die GridBagKonstante
   */
  public static int getFill(String val) {
    int ret = GridBagConstraints.NONE;
    Integer retI = fillMap.get(val);
    if (retI != null) {
      ret = retI.intValue();
    }
    return ret;
  }

  /**
   * Errechnet aus dem Attribut al= die SwingKonstante
   */
  public static int getAlign(String val) {
    int ret = SwingConstants.LEFT; // default
    Integer retI = alignMap.get(val);
    if (retI != null) {
      ret = retI.intValue();
    }
    return ret;
  }

  /**
   * Liefert ein Color-Object unter Angabe eines Farbnamens oder im RBG-Format.
   */
  public static Color getColor(String val) {
  	return Convert.toColor(val);
  }
  
  public static Insets getInsets(String s) {
     String[] si = s.split(",");
     if(si.length < 4) {
        return null;
     }
     Insets i = new Insets(Convert.toInt(si[0]), Convert.toInt(si[1]), Convert.toInt(si[2]), Convert.toInt(si[3]));
     return i;
  }

  /**
   * Converts a String to Java KeyStroke
   * See Menu Item Accelerator
   */
  public static KeyStroke getKey(String val) {
    KeyStroke ret = null;
    int p;
    String sKey;
    String sModi;
    int iKey = -1;
    int iModi = 0; // default

    p=val.indexOf("+");
    if (p > 1) {
      sModi=val.substring(0,p);
      sKey=val.substring(p+1);
      if (sModi.equalsIgnoreCase("Alt")) {iModi=ActionEvent.ALT_MASK;}
      else if (sModi.equalsIgnoreCase("Ctrl")) {iModi=ActionEvent.CTRL_MASK;}
      else if (sModi.equalsIgnoreCase("Shift")) {iModi=ActionEvent.SHIFT_MASK;}
      else if (sModi.equalsIgnoreCase("Meta")) {iModi=ActionEvent.META_MASK;}
    }
    else {
      sKey=val;
    }
    Integer retI = keyMap.get(sKey);
    if (retI != null) {
      iKey = retI.intValue();
    }

    ret = KeyStroke.getKeyStroke(iKey, iModi);
    return ret;
  }

  public static int getStyle(String val) {
    int ret = Font.PLAIN;
    if (val.equals("BOLD")) ret=Font.BOLD;
    else if (val.equals("ITALIC")) ret=Font.ITALIC;
    else if (val.equals("BOLD_ITALIC")) ret=Font.BOLD + Font.ITALIC;
    return ret;
  }
  public static String getStyle(int val) {
    String ret = "PLAIN";
    if (val == Font.BOLD + Font.ITALIC) ret = "BOLD_ITALIC";
    else if (val == Font.BOLD) ret = "BOLD";
    else if (val == Font.ITALIC) ret = "ITALIC";
    return ret;
  }
  /**
   * Wandelt einen String in der Notation size="123,456" in eine Dimension um.
   * Im Fehlerfall wird "40,21" geliefert.
   */
  private static Dimension getSize(String val) {
    Dimension ret = null;
    int w = 40;
    int h = 21;

    int p = val.indexOf(",");
    if (p != -1) {
      String temp = val.substring(0, p);
      w = Convert.toInt(temp);
      temp = val.substring(p+1);
      h = Convert.toInt(temp);
    }
    ret = new Dimension(w, h);
    return ret;
  }
  private static int getLayout(String val) {
    int ret = GuiContainer.GRIDBAG;
    String v = val.toUpperCase();
    if (v.equals("NULL")) {
      ret = GuiContainer.NULL;
    } else if (v.equals("GRID")) {
      ret = GuiContainer.GRID;
    } else if (v.equals("FLOW")) {
      ret = GuiContainer.FLOW;
    } else if (v.equals("BORDER")) {
      ret = GuiContainer.BORDER;
    } else if (v.equals("FORM")) {
    	ret = GuiContainer.FORM;
		} else if (v.equals("SPRING")) {
			ret = GuiContainer.SPRING;
    } else if (v.equals("TABLE")) {
      ret = GuiContainer.TABLE;
    }
    return ret;
  }
  /**
   * Erzeugt einen Dialog mit dem angegebenen Owner
   * @param filename
   * @param owner
   * @return
   */
  public GuiDialog createDialog(String filename, GuiWindow owner) throws GDLParseException {
    GuiWindow win = createWindow(filename, owner);
    return (GuiDialog)win;
  }
  /**
   * Erzeugt ein Formular oder einen Dialog auf Basis eines Dateinamens (.xml).
   * <BR>
   * Das erzeugte Fenster kann mit "show()" angezeigt werden.
   * @param filename Dateiname eine Spezifikaton
   * @return Das erzeugte Fenster oder null, wenn der Dateiname ungültig war.
   */
  public GuiWindow createWindow(String filename) throws GDLParseException {
    return this.createWindow(filename, null);
  }
  private GuiWindow createWindow(String filename, GuiWindow owner) throws GDLParseException {
    Document doc = GuiUtil.getDocument(filename);
    if (doc == null) {
      throw new GDLParseException("GuiFactory#createWindow; Missing file: "+ filename);
    }
    if (owner != null) {
       Element root = doc.getRoot();
       Element form = root.getElement("Form");
       if (form != null) {
          Element dia = form.setName("Dialog");
       }
    }
    logger.debug("createWindow [filename]: " + filename);
    GuiWindow window = this.createWindow(doc, owner);
    return window;
  }

  public GuiWindow createWindow(Document doc) throws GDLParseException {
    return this.createWindow(doc, null);
  }
  public GuiWindow createWindow(Document doc, GuiWindow owner) throws GDLParseException {
    if (lastTime == 0) {
      lastTime = System.currentTimeMillis(); // Zeit messen
      totalTime = lastTime;
    }
    final ArrayList<CurrentKeyword> keys = XmlReader.makeKeywordList(doc);
    if (keys != null) {
      CurContext ctx = this.makeForm(keys, owner, null);
      GuiWindow window = ctx.form;
      window.setCreatedBy(doc);
      return window;
    }
    return null;

  }
  /**
   * Erzeugt ein Fenster auf Basis eines String, der ein XML-Document enthält.
   * @param s Ein String, der eine Spezifikation als XML-Dokument enthält.
   * @param filename Nachrichtlich der Dateiname der Spezifikation
   * (falls kompilierte Scripte eingesetzt werden sollen).
   */
  public GuiWindow createWindowXml(String s, String filename)
  	throws GDLParseException {
    if (filename == null || filename.length() == 0) {
      currentFilename = null;
    } else {
      currentFilename = filename;
    }
    return createWindowXml(s);
  }
  /**
   * Erzeugt ein Fenster auf Basis eines String, der ein XML-Document enthält.
   * @param s Ein String, der eine Spezifikation als XML-Dokument enthält.
   * @see #createWindow
   * @see #createWindowGdl
   */
  public GuiWindow createWindowXml(String source) throws GDLParseException {
	  Document doc = XmlReader.createDocument(source);
	  GuiWindow window = this.createWindow(doc);
	  return window;
  }

  /**
   * Erzeugt ein Panel unter Angabe eines Dateinamens.<BR>
   * Endet der Datename auf ".xml" wird XML-Syntax vermutet.<BR>
   * Diese Spezifikation darf als Hauptcontainer nur ein Panel enthalten.<BR>
   * Wird für den Navigator benötigt.<BR>
   * @param filename Dateiname der Spezifikation eines Panels
   * @param parentForm ParentWindow des Panels
   * @see GuiTree#valueChanged
   */
  public final GuiPanel createPanel(String filename, GuiWindow parentForm)
  throws GDLParseException {
    if (lastTime == 0) {
      lastTime = System.currentTimeMillis(); // Zeit messen
      totalTime = lastTime;
    }

    final ArrayList<CurrentKeyword> keys = this.getKeywords(filename);
    CurContext ctx = this.makeForm(keys, null, parentForm);

    return ctx.retPanel;
  }

  /**
   * Erzeugt ein Panel auf Basis eines String, der ein XML-Document enthält.
   * @param s
   * @return
   * @throws GDLParseException
   */
  public GuiPanel createPanelXml(String s)
		throws GDLParseException {
		if (lastTime == 0) {
		  lastTime = System.currentTimeMillis(); // Zeit messen
		  totalTime = lastTime;
		}
		final ArrayList<CurrentKeyword> keys = XmlReader.makeKeywordListFromString(s);
		//System.out.println(System.currentTimeMillis() - totalTime);
		if (keys != null) {
		  CurContext ctx = this.makeForm(keys, null, null);
		  return ctx.retPanel;
		}
		return null;
  }

  public GuiPanel createPanelXml(String s, GuiWindow parentForm)
		throws GDLParseException {
		if (lastTime == 0) {
		  lastTime = System.currentTimeMillis(); // Zeit messen
		  totalTime = lastTime;
		}
		final ArrayList<CurrentKeyword> keys = XmlReader.makeKeywordListFromString(s);
		//System.out.println(System.currentTimeMillis() - totalTime);
		if (keys != null) {
		  CurContext ctx = this.makeForm(keys, null, parentForm);
		  return ctx.retPanel;
		}
		return null;
  }

  /**
   * Erzeugt den Vector von Keywords aus einem Datennamen (.xml).
   */
  private ArrayList<CurrentKeyword> getKeywords(String filename) throws GDLParseException {
    ArrayList<CurrentKeyword> keys = null;
    String xs = null;
    try {
    	xs = GuiUtil.fileToString(filename); 
      keys = XmlReader.makeKeywordListFromString(xs);
      return keys;
    } catch (Exception ex) {
    	logger.error(ex.getMessage());
    	throw new GDLParseException(ex.getMessage());
    }
  }


  /**
   * Wandel den Namen eines Keywords oder eines Attributes in eine
   * interne Konstante um.<BR>
   * Liefert "null" wenn ungültiges Keyword.
   */
  static Integer getKeywordInt(String name) {
    return hash.get(name);
  }

  /**
   * Liefert das erste Keyword einer Spezifikationsdatei (XML).<BR>
   * Wird für die Referenzierung von Klassen bei "Use" benötigt (Generator).
   */
  public CurrentKeyword getFirstKeyword(String filename)
  throws GDLParseException {
    CurrentKeyword ret = null;

    final ArrayList<CurrentKeyword> keys = this.getKeywords(filename);
    if (keys == null) {
      return null;
    }
    ret = keys.get(0);
    return ret;
  }
  /**
   * "Eigentliche" Factory
   * @param keys Diese ArrayList mit Objekten vom Typ CurrentKeyword enthält die Spezifikation
   * @param owner null oder GuiWindow
   * @param form HauptFenster (wenn createPanel) oder null (wenn createWindow)
   */
  private CurContext makeForm(ArrayList<CurrentKeyword> keys, GuiWindow owner, GuiWindow form) {
    if (keys == null || keys.size() == 0) {
      throw new IllegalArgumentException("No Keywords specified!");
    }
    // Count Windows
    numberOfWindowsCreated++;
    splitCounter = 0;
    // Label_Anchor EAST|WEST
    {
      try {
        final String prop = GuiUtil.getConfig().getValuePath("@LabelAnchor", "EAST");
        if (prop != null && prop.equals("WEST"))  {
          labelAnchorEast = false;
        }
      } catch (Exception ex) {
        System.out.println("Warning: Missing "+GuiUtil.GUIBUILDER_CONFIG);
      }
    }
    // CurContext
    final CurContext ctx = new CurContext(); // Return Value
    ctx.owner = owner;
    if (form != null) { // createPanel
      ctx.form = form;
    } else { // createWindow; DummyDialog erzeugen, wenn kein Window?
      CurrentKeyword firstKeyword = keys.get(0);
      String word = firstKeyword.sKeyword;
      if (word.equals("Begin Applet") == false
        && word.equals("Begin Form") == false
        && word.equals("Begin Dialog") == false ) {
        ctx.createDummyForm();
      }
    }
    // Schleife Keywords
    CurrentKeyword curKey;
    int iKey; // Zähler der Keywords im Vector; siehe USE!!!
    for (iKey = 0; iKey < keys.size(); iKey++) {
      curKey = keys.get(iKey);
      // Case Version
      if (curKey.version != null) {
        if (curKey.version.indexOf(GuiUtil.getVersion()) == -1) {
          System.out.println("Skipping: " + curKey.sKeyword+" "+curKey.title+" "+curKey.vAttrib.toString()); // Debug mode: display line
          continue; // Dieses Keyword weglassen weil andere Version
        }
      } // EndIf Version

      if (GuiUtil.getDebug() == true) {
        System.out.println(System.currentTimeMillis()-lastTime);
        lastTime = System.currentTimeMillis();
        System.out.println(curKey.sKeyword+" "+curKey.title+" "+curKey.vAttrib.toString()); // Debug mode: display line
      }
      switch (curKey.iKeyword.intValue()) { // je nach Keyword

        case keyBEGIN_APPLET:
          perfBeginApplet(curKey, ctx);
        break;

        case keyEND_APPLET:
          perfEndWindow(curKey, ctx);
        break;

        case keyBEGIN_FORM:
          perfBeginForm(curKey, ctx);
        break;

        case keyEND_FORM:
          perfEndWindow(curKey, ctx);
        break;

        case keyBEGIN_DIALOG:
          perfBeginDialog(curKey, ctx);
        break;

        case keyEND_DIALOG:
          perfEndWindow(curKey, ctx);
        break;

        case keyBEGIN_FOLDER:
          perfBeginFolder(curKey, ctx);
        break;

        case keyEND_FOLDER:
          perfEndFolder(curKey, ctx);
        break;

        case keyBEGIN_FRAME:
          perfBeginFrame(curKey, ctx);
        break;

        case keyEND_FRAME:
          perfEndFrame(curKey, ctx);
        break;

        case keyBEGIN_SPLIT:
          perfBeginSplit(curKey, ctx);
        break;

        case keyEND_SPLIT:
          perfEndSplit(curKey, ctx);
        break;

        case keyBEGIN_GROUP:
          perfBeginGroup(curKey, ctx);
        break;

        case keyEND_GROUP:
        case keyEND_PANEL:
        case keyEND_BUTTONBAR_BUTTON:
        case keyEND_OUTLOOKBAR_BUTTON:
        case keyEND_TAB:
        case keyEND_CHART:
        case keyEND_BROWSER:           
        case keyEND_JFX:
          perfEndPanel(curKey, ctx);
          // grid0
          if (ctx.grid0 != 0) {
            ctx.grid0 = ctx.grid0 + ctx.gridOffs;
          }
        break;

        case keyBEGIN_MENUBAR:
          perfBeginMenubar(curKey, ctx);
        break;

        case keyEND_MENUBAR:
          perfEndMenubar(curKey, ctx);
        break;

        case keyBEGIN_MENU:
          perfBeginMenu(curKey, ctx);
        break;

        case keyBEGIN_PANEL:
          perfBeginPanel(curKey, ctx);
        break;

        case keyBEGIN_POPUP:
          perfBeginPopup(curKey, ctx);
        break;

        case keyBEGIN_TAB:
          perfBeginTab(curKey, ctx);
        break;

        case keyBEGIN_TABLE:
          perfBeginTable(curKey, ctx);
        break;

        case keyEND_TABLE:
          perfEndTable(curKey, ctx);
        break;

        case keyBEGIN_TABSET:
          perfBeginTabset(curKey, ctx);
        break;

        case keyBEGIN_TOOLBAR:
          perfBeginToolbar(curKey, ctx);
        break;

        case keyBEGIN_BUTTONBAR:
            perfBeginButtonBar(curKey, ctx);
        break;

        case keyBEGIN_BUTTONBAR_BUTTON:
        		perfBeginButtonBarButton(curKey, ctx);
        break;

        case keyBEGIN_OUTLOOKBAR:
         	perfBeginOutlookBar(curKey, ctx);
         	break;

        case keyBEGIN_OUTLOOKBAR_TAB:
     		perfBeginOutlookBarTab(curKey, ctx);
     break;

     case keyBEGIN_OUTLOOKBAR_BUTTON:
     		perfBeginOutlookBarButton(curKey, ctx);
     break;

     case keyBEGIN_TASKPANE:
     	perfBeginTaskPane(curKey, ctx);
     	break;

     case keyBEGIN_TASKPANE_TAB:
  		perfBeginTaskPaneTab(curKey, ctx);
  		break;

     case keyBEGIN_TASKPANE_BUTTON:
  		perfBeginTaskPaneButton(curKey, ctx);
  		break;

     case keyBEGIN_CHART:
        perfBeginChart(curKey, ctx);
        break;
        
     case keyBEGIN_BROWSER:
        perfBeginBrowser(curKey, ctx);
        break;

     case keyBEGIN_JFX:
        perfBeginJFX(curKey, ctx);
        break;
        
        case keyBEGIN_TREE:
          perfBeginTree(curKey, ctx);
        break;

        case keyBEGIN_ELEMENT:
          perfBeginElement(curKey, ctx);
        break;

        case keyEND_ELEMENT:
          ctx.currentTree.addElement(ctx.treeElement);
        break;

        case keyCONTENT:
          perfTreeContent(curKey, ctx);
        break;

        case keyBUTTON:
          perfButton(curKey, ctx);
        break;

        case keyTBUTTON:
          perfTButton(curKey, ctx);
        break;

        case keyCHECK:
          perfCheck(curKey, ctx);
        break;

        case keyCOMBO:
          perfCombo(curKey, ctx);
        break;

        case keyDOCUMENT:
          perfDocument(curKey, ctx);
        break;

        case keyEND_MENU:
          perfEndMenu(curKey, ctx);
        break;

        case keyEND_POPUP:
          perfEndPopup(curKey, ctx);
        break;

        case keyEND_TABSET:
          perfEndTabset(curKey, ctx);
        break;

        case keyEND_TOOLBAR:
          ctx.toolBar = null; // Toolbar null setzen
        break;

        case keyEND_TREE:
          perfEndTree(curKey, ctx);
        break;

        case keyITEM:
          perfItem(curKey, ctx);
        break;

        case keyITEM_CHECK:
          perfItemCheck(curKey, ctx);
        break;

        case keyITEM_OPTION:
          perfItemOption(curKey, ctx);
        break;

        case keyLABEL:
          perfLabel(curKey, ctx);
        break;

        case keyTITLE:
            perfTitle(curKey, ctx);
          break;

        case keyBORDER:
          perfBorder(curKey, ctx);
        break;

        case keyHIDDEN:
          perfHidden(curKey, ctx);
        break;

        case keyLIST:
          perfList(curKey, ctx);
        break;

        case keyMEMO:
          perfMemo(curKey, ctx);
        break;

        case keyEDITOR:
          perfEditor(curKey, ctx);
        break;

        case keyNODE:
          perfTreeNode(curKey, ctx);
        break;

        case keyBEGIN_OPTION_GROUP:
          perfBeginOptionGroup(curKey, ctx);
        break;

        case keyEND_OPTION_GROUP:
          perfEndOptionGroup(curKey, ctx);
        break;

        case keyOPTION:
          perfOption(curKey, ctx);
        break;

        case keyROW:
          perfTableRow(curKey, ctx);
        break;

        case keyCOLUMN:
          perfTableColumn(curKey, ctx);
        break;

        case keySCROLLBAR:
          perfScrollBar(curKey, ctx);
        break;

        case keySLIDER:
          perfSlider(curKey, ctx);
        break;

        case keyTEXT:
          perfText(curKey, ctx);
        break;

        case keyDATE:
          perfDate(curKey, ctx);
        break;

        case keyCALENDAR:
            perfCalendar(curKey, ctx);
        break;
        case keyCALENDAR_POPUP:
         perfCalendarPopup(curKey, ctx);
        break;

        case keyTIME:
          perfTime(curKey, ctx);
        break;

        case keyMONEY:
          perfMoney(curKey, ctx);
        break;

        case keyNUMBER:
          perfNumber(curKey, ctx);
        break;

        case keySPIN:
          perfSpin(curKey, ctx);
        break;

        case keyPASSWORD:
          perfPassword(curKey, ctx);
        break;

        case keyTOOL:
          perfTool(curKey, ctx);
        break;

        case keySEPARATOR:
          perfSeparator(curKey, ctx);
        break;

        case keyUSE:
          perfUse(iKey, keys, curKey, ctx);
        break;

//        case keyBEGIN_CASE: {
//          // Nüscht! Siehe oben!
//        }
//        break;
//
//        case keyEND_CASE:
//          // Nüscht
//        break;

        case keyXFILLER:
          perfXFiller(curKey, ctx);
        break;

        case keyYFILLER:
          perfYFiller(curKey, ctx);
        break;

        case keySCRIPT:
          perfScript(curKey, ctx, this.currentFilename);
        break;

        case keySTATE:
           perfState(curKey, ctx);
        break;
        
        case keyTIMER:
           perfTimer(curKey, ctx);
        break;

        case keyDESCRIPTION:
        	perfDesciption(curKey, ctx);
        break;

        default: {
        	// Plugins =================================================
          if (GuiUtil.getDebug() == true) {
            System.out.println("Additional Keyword: "+curKey.sKeyword);
          } // end if
          PluginKeyword pkw = getPluginKeyword(curKey.sKeyword);
          if (pkw != null) {
          	try {
          		Object o = pkw.getClazz().newInstance();
          		// Hier müssen wir je nach erweiterter Klasse
          		// die entsprechende Methode der Factory aufrufen
          		switch (pkw.superclass) {
          			case keyTEXT: {
          				GuiText txt = (GuiText)o;
          				perfTextAtt(curKey, ctx, pkw.lc, txt);
          			}
          			break;
          			case keyDATE: {
          				GuiText txt = (GuiText)o;
          				perfTextAtt(curKey, ctx, pkw.lc.cloneLC(), txt);
          			}
          			break;
          			case keyMEMO: {
          				GuiMemo txtA = (GuiMemo)o;
						perfMemoAtt(curKey, ctx, pkw.lc.cloneLC(), txtA);
          			}
          			break;
					case keyBEGIN_TABLE: {
						GuiTable tbl = (GuiTable)o;
						perfTableAtt(curKey, ctx, pkw.lc.cloneLC(), tbl);
					}
					break;

          		} // End Switch
          		for (Iterator<CurrentAttrib> a = curKey.vAttrib.iterator() ; a.hasNext() ;) {
          			CurrentAttrib	curAtt = a.next();
							  PluginAttrib pa = pkw.getAttrib(curAtt.sKeyword);
							  if (pa != null) {
								  Object args[] = null;
								  switch (pa.argtype) {
								  	case PluginAttrib.BOOL:
								  		args = new Object[] { Boolean.valueOf(curAtt.bValue) };
								  		pa.method.invoke(o, args);
								  		break;
										case PluginAttrib.INT:
											args = new Object[] { new Integer(curAtt.iValue) };
											pa.method.invoke(o, args);
											break;
										case PluginAttrib.FLOAT:
											args = new Object[] { new Float(curAtt.fValue) };
											pa.method.invoke(o, args);
											break;
										case PluginAttrib.CHAR:
											args = new Object[] { new Character(curAtt.cValue) };
											pa.method.invoke(o, args);
											break;
										case PluginAttrib.DOUBLE:
											args = new Object[] { new Double(curAtt.dValue) };
											pa.method.invoke(o, args);
											break;
										case PluginAttrib.STRING:
											args = new Object[] { curAtt.sValue };
											pa.method.invoke(o, args);
											break;
										case PluginAttrib.COLOR:
											args = new Object[] { getColor(curAtt.sValue) };
											pa.method.invoke(o, args);
											break;
										case PluginAttrib.POINT:
											args = new Object[] { getSize(curAtt.sValue) };
											pa.method.invoke(o, args);
											break;
										default:
											System.err.println("Plugin; Unknown Argument Type: " + pa.argtype);
									}
							  }
          		}
          	} catch (Exception ex) {
          		System.err.println("Plugin-Attribute: "+ex.getMessage());
          	}
          }
        } // end of default
      } //  end of switch
      // NACHBRENNER =================================
      // OnCreate -----------------------
      if (curKey.onCreate == true) {
      	// ArrayList!!
      	for (Iterator<CurrentAttrib> it = curKey.vAttrib.iterator(); it.hasNext();) {
      		CurrentAttrib att = it.next();
      		if (att.iKeyword.intValue() == msgCREATE) {
						if (ctx.curComponent instanceof GuiComponent) {
							GuiRootPane rootPane = ctx.form.getRootPane();
							if (rootPane != null) {
								rootPane.obj_Create((GuiComponent)ctx.curComponent, att.sValue);
							}
						}
						break;
      		}
      	}
      }
      // Context -------------------------
      // End of line (eol) verarbeiten
      if (curKey.sKeyword.startsWith("End ") || curKey.sKeyword.startsWith("Use ") || curKey.eol == false || curKey.sKeyword.startsWith("Row") || curKey.sKeyword.startsWith("Desc")) {
        // Zeile nicht weiterzählen, x nicht setzen
      }
      else {
        ctx.incY(ctx.yoff);  // Zeile weiterzählen
        ctx.cpPar.ly = ctx.y;
        ctx.yoff = 0;
        ctx.x = 0; // wieder in Spalte 0 beginnen
      } // End if
    } // End For Schleife Keywords
    long timeUsed = System.currentTimeMillis() - totalTime;
    totalTimeUsed += timeUsed;
    System.out.println("Time used: "+timeUsed+" / Total time: "+totalTimeUsed);
    lastTime = 0;
    return ctx;
  } // end of method makeForm

  // ************** Performing Components ****************************
  private void perfBeginApplet(CurrentKeyword curKey, CurContext c) {
    int w=0, h=0;
    if (GuiUtil.isApplet() == true) {
      c.form = new GuiApplet(curKey.title);
    } else {
      c.form = new GuiForm(curKey.title);
    }
    c.pPar = c.form.getMainPanel();
    c.cpPar = new CurrentPanel(c.form.getMainPanel(), 0, 0); // Current Parent Panel
    c.cpStack[0]=c.cpPar;
    c.parentContainer.push(c.form.getMainPanel());
    c.curContainer = c.form.getMainPanel();
    c.form.setDefaultTitle(curKey.title);
    w = GuiUtil.getConfig().getValueIntPath(".Form@width", 500);
    h = GuiUtil.getConfig().getValueIntPath(".Form@height", 400);
    c.x = -1; c.y = 0;
    Point location = null;

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attIMG: c.form.setIconImage(GuiUtil.makeAwtImage(curAtt.sValue)); break; // nicht so einfach!
        case attSB: c.form.getMainPanel().setBackground(getColor(curAtt.sValue)); break;
        case attSF: c.form.getMainPanel().setForeground(getColor(curAtt.sValue)); break;
        case attTOPIC: c.form.getRootPane().setHelpTopic(curAtt.sValue); break;
        case attNAME: c.form.setName(curAtt.sValue); break;
        case attFILE: if (!GuiUtil.isAPI()) {c.defaultValues = GuiUtil.fileToXml(curAtt.sValue);} break;
        case attREF: c.form.getRootPane().setRef(curAtt.sValue); break;
        case attSIZE: final Dimension size = getSize(curAtt.sValue);
                      w = size.width;
                      h = size.height;
                      break;
        case attAN: location = getLocation(curAtt.sValue, w, h); break;
        case msgOPEN: c.form.setMsgOpen(curAtt.sValue); break;
        case msgCLOSE: c.form.setMsgClose(curAtt.sValue); break;
        case msgACTIVE: c.form.setMsgActive(curAtt.sValue); break;
        case attLAYOUT: c.form.getMainPanel().setLayoutManager(getLayout(curAtt.sValue)); break;
        case attCOLSPEC: c.form.getMainPanel().setColSpec(curAtt.sValue); break;
        case attROWSPEC: c.form.getMainPanel().setRowSpec(curAtt.sValue); break;
        case attCOLS: c.form.getMainPanel().setGridColumns(curAtt.iValue); break;
        case attCONTROLLER: c.form.setController(curAtt.sValue); break;
        case attAUTOSIZE: c.form.setAutoSize(curAtt.bValue); break;
        case attRESTORE: c.form.setRestoreWindow(curAtt.sValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(c.form, curAtt.sValue); break;
      }
    } // next i
    c.form.getComponent().setSize(w, h);
    if (c.x >=0) {
      c.form.getComponent().setLocation(c.x, c.y);
    } else if (location != null) {
      c.form.getComponent().setLocation(location);
    } else {
      // Default-Location Center
      final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      c.form.getComponent().setLocation((screen.width-w)/2, (screen.height-h)/2);
    }
    c.x=0; c.y=0;
  }

  private static void perfEndWindow(CurrentKeyword curKey, CurContext c) {
    // DefaultValues?
    if (c.defaultValues != null) {
      c.form.setAllValuesXml(c.defaultValues);
      c.defaultValues = null;
    }
  }
  private void perfBeginForm(CurrentKeyword curKey, CurContext c) {
    int w=0, h=0;
    c.form = new GuiForm(curKey.title);
    c.pPar = c.form.getMainPanel();
    c.cpPar = new CurrentPanel(c.form.getMainPanel(), 0, 0); // Current Parent Panel
    c.cpStack[0]=c.cpPar;
    c.parentContainer.push(c.form.getMainPanel());
    c.curContainer = c.form.getMainPanel();

    c.form.setDefaultTitle(curKey.title);
    w = GuiUtil.getConfig().getValueIntPath(".Form@width", 500);
    h = GuiUtil.getConfig().getValueIntPath(".Form@height", 400);
    c.x = -1; c.y = 0;
    Point location = null;

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attIMG: c.form.setIconImage(GuiUtil.makeAwtImage(curAtt.sValue)); break; // nicht so einfach!
        case attSB: c.form.getMainPanel().setBackground(getColor(curAtt.sValue)); break;
        case attSF: c.form.getMainPanel().setForeground(getColor(curAtt.sValue)); break;
        case attTOPIC: c.form.getRootPane().setHelpTopic(curAtt.sValue); break;
        case attNAME: c.form.setName(curAtt.sValue); break;
        case attFILE: if (!GuiUtil.isAPI()) {c.defaultValues = GuiUtil.fileToXml(curAtt.sValue);} break;
        case attREF: c.form.getRootPane().setRef(curAtt.sValue); break;
        case attSIZE: final Dimension size = getSize(curAtt.sValue);
                      w = size.width;
                      h = size.height;
                      break;
        case attAN: location = getLocation(curAtt.sValue, w, h); break;
        case attTYPE:
          if (curAtt.sValue.equals("SYSTEM") && c.form instanceof GuiForm) {
            ((GuiForm)c.form).setSystemForm(true);
          }
          break;
        case msgOPEN: c.form.setMsgOpen(curAtt.sValue); break;
        case msgCLOSE: c.form.setMsgClose(curAtt.sValue); break;
        case msgACTIVE: c.form.setMsgActive(curAtt.sValue); break;
        case attLAYOUT: c.form.getMainPanel().setLayoutManager(getLayout(curAtt.sValue)); break;
        case attCOLSPEC: c.form.getMainPanel().setColSpec(curAtt.sValue); break;
        case attROWSPEC: c.form.getMainPanel().setRowSpec(curAtt.sValue); break;
        case attCOLS: c.form.getMainPanel().setGridColumns(curAtt.iValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(c.form, curAtt.sValue); break;
        // Bindings
        case attDATASET: c.form.setDatasetName(curAtt.sValue); break;
        case attROOT_ELEMENT: c.form.setRootElementName(curAtt.sValue); break;
        case attCONTROLLER: c.form.setController(curAtt.sValue); break;
        case attAUTOSIZE: c.form.setAutoSize(curAtt.bValue); break;
        case attRESTORE: c.form.setRestoreWindow(curAtt.sValue); break;
      }
    } // next i
    c.form.getComponent().setSize(w, h);
    if (c.x >=0) {
      c.form.getComponent().setLocation(c.x, c.y);
    } else if (location != null) {
      c.form.getComponent().setLocation(location);
    } else {
      // Default-Location Center
      final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      c.form.getComponent().setLocation((screen.width-w)/2, (screen.height-h)/2);
    }
    c.x=0; c.y=0;
  }

  private void perfBeginDialog(CurrentKeyword curKey, CurContext c) {
    int w=0, h=0;
    GuiWindow parent = GuiSession.getInstance().getCurrentWindow();
    if (parent instanceof GuiForm) {
      c.form = new GuiDialog(parent, curKey.title);
    } else {
      c.form = new GuiDialog(c.owner, curKey.title);
    }
    c.pPar = c.form.getMainPanel();
    c.cpPar = new CurrentPanel(c.form.getMainPanel(), 0, 0); // Current Parent Panel
    c.cpStack[0]=c.cpPar;
    c.parentContainer.push(c.form.getMainPanel());
    c.curContainer = c.form.getMainPanel();

    c.form.setName(GuiUtil.labelToName(curKey.title));
    // Use?
    if (c.uVal != null) {
      c.form.setTitle(c.uVal);
      c.form.setName(GuiUtil.labelToName(c.uVal));
      c.uVal = null;
    }
		w = GuiUtil.getConfig().getValueIntPath(".Dialog@width", 500);
		h = GuiUtil.getConfig().getValueIntPath(".Dialog@height", 400);
    // Use
    if (c.uw>=0) {w=c.uw; c.uw=-1;};
    if (c.uh>=0) {h=c.uh; c.uh=-1;};
    //?if (uwx>=0) {wx=uwx; uwx=-1;};
    //?if (uwy>=0) {wy=uwy; uwy=-1;};

    c.x = -1 ; c.y = 0;
    Point location = null;

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        // case attIMG:  break; // gips leider nicht!!
        case attSB: c.form.getMainPanel().setBackground(getColor(curAtt.sValue)); break;
        case attSF: c.form.getMainPanel().setForeground(getColor(curAtt.sValue)); break;
        case attTOPIC: c.form.getRootPane().setHelpTopic(curAtt.sValue); break;
        case attTYPE: ((GuiDialog)c.form).setDialogType(curAtt.sValue); break;
        case attNAME: c.form.setName(curAtt.sValue); break;
        case attFILE: if (!GuiUtil.isAPI()) {c.defaultValues = GuiUtil.fileToXml(curAtt.sValue);} break;
        case attREF: c.form.getRootPane().setRef(curAtt.sValue); break;
        case attSIZE: final Dimension size = getSize(curAtt.sValue);
                    w = size.width;
                    h = size.height;
                    break;
        case attAN: location = getLocation(curAtt.sValue, w, h); break;
        case msgOPEN: c.form.setMsgOpen(curAtt.sValue); break;
        case msgCLOSE: c.form.setMsgClose(curAtt.sValue); break;
        case msgACTIVE: c.form.setMsgActive(curAtt.sValue); break;
        case attLAYOUT: c.form.getMainPanel().setLayoutManager(getLayout(curAtt.sValue)); break;
        case attCOLSPEC: c.form.getMainPanel().setColSpec(curAtt.sValue); break;
        case attROWSPEC: c.form.getMainPanel().setRowSpec(curAtt.sValue); break;
        case attCOLS: c.form.getMainPanel().setGridColumns(curAtt.iValue); break;
        // Bindings
        case attDATASET: c.form.setDatasetName(curAtt.sValue); break;
        case attROOT_ELEMENT: c.form.setRootElementName(curAtt.sValue); break;
        case attCONTROLLER: c.form.setController(curAtt.sValue); break;
        case attAUTOSIZE: c.form.setAutoSize(curAtt.bValue); break;
        case attRESTORE: c.form.setRestoreWindow(curAtt.sValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(c.form, curAtt.sValue); break;
      }
    }
    c.form.getComponent().setSize(w, h);
    if (c.x >=0) {
      c.form.getComponent().setLocation(c.x, c.y);
    } else if (location != null) {
      c.form.getComponent().setLocation(location);
    } else {
      // Default-Location Center
      final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      c.form.getComponent().setLocation((screen.width-w)/2, (screen.height-h)/2);
    }
    c.x=0; c.y=0;
  }
  // Macht sowohl Panel als auch Group
  private void perfBeginPanel(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=1, wy=1;
    // Use?
    if (c.uw>=0) {w=c.uw; c.uw=-1;};
    if (c.uh>=0) {h=c.uh; c.uh=-1;};
    if (c.uwx>=0) {wx=c.uwx; c.uwx=-1;};
    if (c.uwy>=0) {wy=c.uwy; c.uwy=-1;};
    int it=0, ib=0, ir=0, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.CENTER;
    int fill=GridBagConstraints.BOTH;
    boolean scroll = false;
    final GuiPanel pPanel = new GuiPanel(curKey.title);
    pPanel.setGuiParent(c.curContainer); // neu 23.3.2002
    if (c.retPanel == null) c.retPanel = pPanel;
    boolean isLinkTable = false;
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attSB: pPanel.setBackground(getColor(curAtt.sValue)); break;
        case attSF: pPanel.setForeground(getColor(curAtt.sValue)); break;
        case attNAME: pPanel.setName(curAtt.sValue); break;
        case attREF: pPanel.setRef(curAtt.sValue);
                if (c.curContainer != null) {
                  c.curContainer.addMember(pPanel);
                  c.parentContainer.push(pPanel);
                }
                c.curContainer = pPanel;
                break;
        case attPOINT: pPanel.setFont(pPanel.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: pPanel.setFont(pPanel.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: pPanel.setFont(new Font(curAtt.sValue, pPanel.getFont().getStyle(), pPanel.getFont().getSize())); break;
        case attSIZE: pPanel.setPreferredSize(getSize(curAtt.sValue)); break;
        case attTYPE: if (curAtt.sValue.equals("SCROLL")) {
                        scroll = true;
                      }
                break;
        case attVISIBLE: pPanel.setVisible(curAtt.bValue); break;
        case attGRID0: c.grid0 = curAtt.iValue; break;
        case attLAYOUT: pPanel.setLayoutManager(getLayout(curAtt.sValue)); break;
			case attCOLSPEC: pPanel.setColSpec(curAtt.sValue); break;
			case attROWSPEC: pPanel.setRowSpec(curAtt.sValue); break;
        case attCOLS: pPanel.setGridColumns(curAtt.iValue); break;
        case attLINKTABLE: isLinkTable = curAtt.bValue; break;
        case attHELPID: GuiSession.getInstance().setHelpId(pPanel, curAtt.sValue); break;
        // DragDrop
				case msgDROP: pPanel.setMsgDrop(curAtt.sValue); break;
				case msgDRAG_ENTER: pPanel.setMsgDragEnter(curAtt.sValue); break;
				case msgDRAG_OVER: pPanel.setMsgDragOver(curAtt.sValue); break;
				case msgDRAG_EXIT: pPanel.setMsgDragExit(curAtt.sValue); break;
        // Bindings --> Ref?
        case attELEMENT: pPanel.setElementName(curAtt.sValue);
        if (c.curContainer != pPanel && c.curContainer != null) {
          c.curContainer.addMember(pPanel);
          c.parentContainer.push(pPanel);
          c.curContainer = pPanel;
        }
        break;
        // Events
        case msgCREATE: pPanel.setMsgCreate(curAtt.sValue); break;
      }
    } // next i
    // LinkTable?
    if (isLinkTable == true && c.curContainer != null) {
      c.curContainer.setLinkTable(c.cTbl);
    }
    if (c.checkFirst(pPanel)==false) {
      // Scrollbox?
      if (scroll == false) {
        c.pPar.add(pPanel, new GridBagConstraints(c.x, c.y, w, h, wx, wy
                 ,anchor, fill, new Insets(it,il,ib,ir), px, py));
      } else {
        c.pPar.add(new GuiScrollBox(pPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED ,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
          new GridBagConstraints(c.x, c.y, w, h, wx, wy,
         anchor, fill, new Insets(it,il,ib,ir), px, py));
      }
      c.incX(w);
      if (curKey.eol == true) {
        c.incY(h);
      }
      c.cpPar.lx=c.x;
      c.cpPar.ly=c.y;
      c.cpStack[c.poi]=c.cpPar;
      c.poi++;
      c.pPar = pPanel;

      c.resetXY();
      c.cpPar = new CurrentPanel(pPanel, c.x, c.y);
      c.cpStack[c.poi]=c.cpPar;
    }
  }
  private static void perfEndPanel(CurrentKeyword curKey, CurContext c) {
    if (c.poi >0 ) {
      if (c.parentContainer.peek() == c.pPar) {
        c.parentContainer.pop();
        c.curContainer = c.parentContainer.peek();
      }
      c.poi--; // Stackpointer für Panels runterzählen
      c.cpPar=c.cpStack[c.poi]; // CurrentPanel setzen
      c.pPar=c.cpPar.p;
      c.x=c.cpPar.lx;
      c.y=c.cpPar.ly;
      if (curKey.eol == true){
        c.x = 0;
      }
      c.curOptionGroup = null; // Das geht bei geschachtelten Panels schief!
    }
  }
  private void perfBeginGroup(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=1, wy=1;
    // Use?
    if (c.uw>=0) {w=c.uw; c.uw=-1;};
    if (c.uh>=0) {h=c.uh; c.uh=-1;};
    if (c.uwx>=0) {wx=c.uwx; c.uwx=-1;};
    if (c.uwy>=0) {wy=c.uwy; c.uwy=-1;};
    int it=0, ib=0, ir=0, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.CENTER;
    int fill=GridBagConstraints.BOTH;

    String grpTitle = null;
    if (c.uVal != null) {
      grpTitle = c.uVal;
      c.uVal = null;
    }
    else {
      grpTitle = curKey.title;
    }
    final GuiGroup pPanel = new GuiGroup(grpTitle);
    pPanel.setGuiParent(c.curContainer); // neu 23.3.2002
    if (c.retPanel == null) c.retPanel = pPanel;
    boolean isLinkTable = false;
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attSB: pPanel.setBackground(getColor(curAtt.sValue)); break;
        case attSF: pPanel.setForeground(getColor(curAtt.sValue)); break;
        // is nich so einfach!
        case attPOINT: {
          final Font tf = pPanel.getBorderFont();
          if (tf != null) {
        	  pPanel.setBorderFont(tf.deriveFont(curAtt.fValue));
          }
          }
          break;
        case attSTYLE: {
         final Font tf = pPanel.getBorderFont();
         if (tf != null) {
        	 pPanel.setBorderFont(tf.deriveFont(getStyle(curAtt.sValue))); 
         }
         }
          break;
        case attFONT: {
          final Font tf = pPanel.getBorderFont();
          if (tf != null) {
          pPanel.setBorderFont(
            new Font(curAtt.sValue,
            tf.getStyle(),
            tf.getSize())); }
        }
          break;
        case attTYPE: pPanel.setBorder(curAtt.sValue); break;
        case attNAME: pPanel.setName(curAtt.sValue); break;
        case attREF: pPanel.setRef(curAtt.sValue);
                if (c.curContainer != null) {
                  c.curContainer.addMember(pPanel);
                  c.parentContainer.push(pPanel);
                }
                c.curContainer = pPanel;
                break;
        case attSIZE: pPanel.setPreferredSize(getSize(curAtt.sValue)); break;
        case attVISIBLE: pPanel.setVisible(curAtt.bValue); break; // geht nicht?
        case attGRID0: c.grid0 = curAtt.iValue; break;
        case attLAYOUT: pPanel.setLayoutManager(getLayout(curAtt.sValue)); break;
        case attCOLSPEC: pPanel.setColSpec(curAtt.sValue); break;
        case attROWSPEC: pPanel.setRowSpec(curAtt.sValue); break;
        case attCOLS: pPanel.setGridColumns(curAtt.iValue); break;
        case attLINKTABLE: isLinkTable = curAtt.bValue; break;
        case attHELPID: GuiSession.getInstance().setHelpId(pPanel, curAtt.sValue); break;
        // DragDrop
				case msgDROP: pPanel.setMsgDrop(curAtt.sValue); break;
				case msgDRAG_ENTER: pPanel.setMsgDragEnter(curAtt.sValue); break;
				case msgDRAG_OVER: pPanel.setMsgDragOver(curAtt.sValue); break;
				case msgDRAG_EXIT: pPanel.setMsgDragExit(curAtt.sValue); break;
        // Bindings
        case attELEMENT: pPanel.setElementName(curAtt.sValue); break;
        // Events
        case msgCREATE: pPanel.setMsgCreate(curAtt.sValue); break;
      }
    } // next i
    c.curComponent = pPanel;
    // LinkTable?
    if (isLinkTable == true && c.curContainer != null) {
      c.curContainer.setLinkTable(c.cTbl);
    }
    if (c.checkFirst(pPanel)==false) {
      c.pPar.add(pPanel, new GridBagConstraints(c.x, c.y, w, h, wx, wy
                 ,anchor, fill, new Insets(it,il,ib,ir), px, py));

      c.incX(w);
      if (curKey.eol == true) {
        c.incY(h);
      }
      c.cpPar.lx=c.x;
      c.cpPar.ly=c.y;
      c.cpStack[c.poi]=c.cpPar;

      c.poi++;
      c.pPar = pPanel;

      c.resetXY();
      c.yoff=0;
      c.cpPar = new CurrentPanel(pPanel, c.x, c.y);
      c.cpStack[c.poi]=c.cpPar;
    }
    // grid0
    if (c.grid0 != 0) {
      try { // Bei Nimbus NPE bei getBorderInserts
    	  c.gridOffs = pPanel.getJComponent().getBorder().getBorderInsets(null).left;
      } catch (Exception ex) {}
      c.grid0 = c.grid0 - c.gridOffs;
    }
  }

  private static void perfBeginTabset(CurrentKeyword curKey, CurContext c) {
    int w=4, h=1;
    float wx=1, wy=1;
    // Use?
    if (c.uw>=0) {w=c.uw; c.uw=-1;};
    if (c.uh>=0) {h=c.uh; c.uh=-1;};
    if (c.uwx>=0) {wx=c.uwx; c.uwx=-1;};
    if (c.uwy>=0) {wy=c.uwy; c.uwy=-1;};
    int it=5, ib=0, ir=0, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.CENTER;
    int fill=GridBagConstraints.BOTH;
    final GuiTabset tPanel = new GuiTabset(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attNAME: tPanel.setName(curAtt.sValue); break;
        // ## PENDING: wozu? case attREF: tPanel.setRef(curAtt.sValue); break;
        case attSIZE: tPanel.setPreferredSize(getSize(curAtt.sValue)); break;
        case attVISIBLE: tPanel.setVisible(curAtt.bValue); break;
        case attTYPE: tPanel.setTabPlacement(curAtt.sValue); break;
        case attLAYOUT: tPanel.setTabLayoutPolicy(curAtt.sValue); break;
        case attSB: tPanel.setBackground(getColor(curAtt.sValue)); break;
        case attSF: tPanel.setForeground(getColor(curAtt.sValue)); break;
        case msgACTIVE: tPanel.setMsgActive(curAtt.sValue); break;
      }
    } // next i
    c.pPar.add(tPanel, new GridBagConstraints(c.x, c.y, w, h, wx, wy
               ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    c.incX(w); // sinnlos?
    c.yoff=h;
    // Stack of Tabsets
    c.cTabset = tPanel; // current Tabset setzen
    c.tabsetPoi++;
    c.tabsetStack[c.tabsetPoi]=c.cTabset;
  }

  private static void perfEndTabset(CurrentKeyword curKey, CurContext c) {
    c.tabsetPoi--;
    c.cTabset = c.tabsetStack[c.tabsetPoi];
    c.x=0;
  }

  private void perfBeginTab(CurrentKeyword curKey, CurContext c) {
    c.cpPar.lx=c.x;
    c.cpPar.ly=c.y;
    c.cpStack[c.poi]=c.cpPar;

    boolean bDO = false;
    String tabTitle = "";
    if (c.uVal != null) {
      tabTitle = c.uVal;
      c.uVal = null;
    }
    else {
      tabTitle = curKey.title;
    }
    // Tabset hinzufügen, wenn fehlt.
    if (c.cTabset == null) {
      System.out.println("Warning: Missing <Tabset>");
      c.cTabset = new GuiTabset("dummy");
      c.pPar.add(c.cTabset, new GridBagConstraints(0, 0, 1, 1, 1, 1
               ,GridBagConstraints.CENTER
               ,GridBagConstraints.BOTH , new Insets(0,0,0,0), 0, 0));
    }
    GuiTab pTab = new GuiTab(c.cTabset, tabTitle);
    if (c.retPanel == null) c.retPanel = pTab;
    boolean isLinkTable = false;
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attIMG: pTab.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
        case attTT: pTab.setToolTipText(curAtt.sValue); break;
        case attSB: pTab.setBackground(getColor(curAtt.sValue)); break;
        case attSF: pTab.setForeground(getColor(curAtt.sValue)); break;
        case attDO: bDO = !curAtt.bValue; break;
        case attNAME: pTab.setName(curAtt.sValue); break;
        case attREF: pTab.setRef(curAtt.sValue); break;
        case attVISIBLE: pTab.setVisible(curAtt.bValue); break;
        case attLAYOUT: pTab.setLayoutManager(getLayout(curAtt.sValue)); break;
        case attCOLSPEC: pTab.setColSpec(curAtt.sValue); break;
        case attROWSPEC: pTab.setRowSpec(curAtt.sValue); break;
        case attCOLS: pTab.setGridColumns(curAtt.iValue); break;
        case attGRID0: c.grid0 = curAtt.iValue; break;
        case attLINKTABLE: isLinkTable = curAtt.bValue; break;
        case attHELPID: GuiSession.getInstance().setHelpId(pTab, curAtt.sValue); break;
        // DragDrop
				case msgDROP: pTab.setMsgDrop(curAtt.sValue); break;
				case msgDRAG_ENTER: pTab.setMsgDragEnter(curAtt.sValue); break;
				case msgDRAG_OVER: pTab.setMsgDragOver(curAtt.sValue); break;
				case msgDRAG_EXIT: pTab.setMsgDragExit(curAtt.sValue); break;
        // Bindings
        case attELEMENT: pTab.setElementName(curAtt.sValue); break;
        case attROOT_ELEMENT: pTab.setElementName("ROOT:"+curAtt.sValue); break;
        // Events
        case msgCREATE: pTab.setMsgCreate(curAtt.sValue); break;
        case msgACTIVE: pTab.setMsgActive(curAtt.sValue); break;
      }
    } // next i
    if (bDO == true) {
      c.cTabset.setEnabledAt(pTab.getTabIndex(), false);
    }

    if (c.curContainer != null && pTab.getRef().equals("-") == false) {
      c.curContainer.addMember(pTab);
      c.parentContainer.push(pTab);
      c.curContainer = pTab;
    }
    // LinkTable?
    if (isLinkTable == true && c.curContainer != null) {
      c.curContainer.setLinkTable(c.cTbl);
    }
    c.pPar = pTab;
    c.resetXY();
    // Panel-Stack versorgen
    c.poi++;
    c.cpPar = new CurrentPanel(pTab, c.x, c.y);
    c.cpStack[c.poi]=c.cpPar;
  }

  private void perfBeginTable(CurrentKeyword curKey, CurContext c) {
		LayoutConstraints lc = new LayoutConstraints();
    lc.w=1; lc.h=2;
    lc.wx=1; lc.wy=1;
    // Use?
    if (c.uw>=0) {lc.w=c.uw; c.uw=-1;};
    if (c.uh>=0) {lc.h=c.uh; c.uh=-1;};
    if (c.uwx>=0) {lc.wx=c.uwx; c.uwx=-1;};
    if (c.uwy>=0) {lc.wy=c.uwy; c.uwy=-1;};
    lc.it=0; lc.ib=0; lc.ir=0; lc.il=0;
    lc.px=0; lc.py=0;
    lc.anchor = GridBagConstraints.CENTER;
    lc.fill = GridBagConstraints.BOTH;
    final GuiTable tbl = new GuiTable(curKey.title);
    this.perfTableAtt(curKey, c, lc, tbl);
  }
	private void perfTableAtt(CurrentKeyword curKey, CurContext c, LayoutConstraints lc, final GuiTable tbl) {
		c.isTable = true;
    c.tableData = null;
    c.tblScroll = new GuiScrollBox(tbl);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attW: lc.w=curAtt.iValue; break;
        case attH: lc.h=curAtt.iValue; break;
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attWX: lc.wx=curAtt.fValue; break;
        case attWY: lc.wy=curAtt.fValue; break;
        case attIT: lc.it=curAtt.iValue; break;
        case attIL: lc.il=curAtt.iValue; break;
        case attIB: lc.ib=curAtt.iValue; break;
        case attIR: lc.ir=curAtt.iValue; break;
        case attPX: lc.px=curAtt.iValue; break;
        case attPY: lc.py=curAtt.iValue; break;
        case attAN: lc.anchor=getAnchor(curAtt.sValue); break;
        case attFILL: lc.fill=getFill(curAtt.sValue); break;
        case attDO: tbl.setEnabled(!curAtt.bValue); break; // dann geht selectieren nicht!
        case attRO: tbl.setReadonly(curAtt.bValue); break; // für getDatasetValues
        case attNAME: tbl.setName(curAtt.sValue); break;
        case attREF: tbl.setRef(curAtt.sValue); break;
        case attFILE: if (!GuiUtil.isAPI()) { tbl.setRowEditor(curAtt.sValue);} break;
        case attVAL: if (!GuiUtil.isAPI()) { c.tableData = GuiUtil.fileToTableData(curAtt.sValue); }
            break;
        case attMIN: tbl.getMinimumSize().width = curAtt.iValue; c.tblScroll.setMinimumSize(tbl.getMinimumSize()); break;
        case attMAX: tbl.getMaximumSize().width = curAtt.iValue; c.tblScroll.setMaximumSize(tbl.getMaximumSize()); break;
        case attMINSIZE: tbl.setMinimumSize(getSize(curAtt.sValue)); 
           c.tblScroll.setMinimumSize(tbl.getMinimumSize()); break;
        case attMAXSIZE: tbl.setMaximumSize(getSize(curAtt.sValue)); break;
        case attSIZE: tbl.setPreferredSize(getSize(curAtt.sValue)); break;
        case attTABSTOP: tbl.setTabstop(curAtt.bValue); break;
        case attVISIBLE: tbl.setVisible(curAtt.bValue); break;
		case attROWHEIGHT: tbl.setRowHeight(curAtt.iValue); break;
		case attDRAG: tbl.setDrag(curAtt.bValue); break;
		case attAUTOSIZE: tbl.setAutoSize(curAtt.bValue); break;
		case attHELPID: GuiSession.getInstance().setHelpId(tbl, curAtt.sValue); break;
        // Messages
        case msgDBLCLICK: tbl.setMsgDblClick(curAtt.sValue); break;
        case msgCOLHEADERCLICK: tbl.setMsgColHeaderClick(curAtt.sValue); break;
        case msgROWCLICK: tbl.setMsgRowClick(curAtt.sValue); break;
        case msgCREATE: tbl.setMsgCreate(curAtt.sValue); break;
        case msgPOPUP: tbl.setMsgPopup(curAtt.sValue); break;
        // DragDrop
        case msgDROP: tbl.setMsgDrop(curAtt.sValue); break;
        case msgFILEDROP: tbl.setMsgFileDrop(curAtt.sValue); break;
        case msgDRAG_ENTER: tbl.setMsgDragEnter(curAtt.sValue); break;
        case msgDRAG_OVER: tbl.setMsgDragOver(curAtt.sValue); break;
        case msgDRAG_EXIT: tbl.setMsgDragExit(curAtt.sValue); break;
        // Mouse
        case msgMOUSEOVER: tbl.setMsgMouseOver(curAtt.sValue); break;
        case msgMOUSEMOVE: tbl.setMsgMouseMoved(curAtt.sValue); break;
        // pre/postSort
        case msgPRE_SORT: tbl.setMsgPreSort(curAtt.sValue); break;
        case msgPOST_SORT: tbl.setMsgPostSort(curAtt.sValue); break;
        // Bindings
        case attELEMENT: tbl.setElementName(curAtt.sValue); break;
        case attROOT_ELEMENT: tbl.setElementName("ROOT:"+curAtt.sValue); break;
      }
    } // next i
    c.cTbl = tbl; // Current table

    c.tblGrid = new GridBagConstraints(c.x, c.y, lc.w, lc.h, lc.wx, lc.wy
          ,lc.anchor, lc.fill, new Insets(lc.it,lc.il,lc.ib,lc.ir), lc.px, lc.py);
    // für PopupMenu der Scrollbox
    c.tblScroll.setName(tbl.getName()+"_ScrollBox");
    c.tblScroll.getViewport().setOpaque(true);
    c.tblScroll.setBackground(tbl.getBackground());
    c.curComponent = tbl;
    if (c.curContainer != null) {
      c.curContainer.addMember(tbl);
    }
    c.incX(lc.w);
    //c.yoff=lc.h;
    if (curKey.eol == true) {
      c.incY(lc.h);
    }
  }

  private static void perfEndTable(CurrentKeyword curKey, CurContext c) {
    c.isTable = false;
    c.cTbl.tableReady();
    if (c.tableData != null) {
      c.cTbl.setValue(c.tableData);
      c.tableData = null;
    }
    c.pPar.add(c.tblScroll, c.tblGrid);
    // Set current Table (die erste, die hier vorbeikommt).
    GuiRootPane root = c.cTbl.getRootPane();
    if (root != null) {
       if (root.getCurrentTable() == null) {
          root.setCurrentTable(c.cTbl);
       }
    }
  }

  private static void perfTableRow(CurrentKeyword curKey, CurContext c) {
    if (GuiUtil.isAPI()) return;
    GuiTableRow row = new GuiTableRow(c.cTbl, c.cTbl.getColCount());
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attFORMAT: {
          row.setFormat(curAtt.sValue);
        } break;
        case attDO: {
          row.setEditable(!curAtt.bValue);
        } break;
        case attITEMS: {
            if (curAtt.iValue > 0 && curAtt.sValue.length() < 4) {
              c.cTbl.initRows(curAtt.iValue-1);
            } else {
              final String[] items=getItems(curAtt.sValue);
              row.setData(items);
            }
          break;
        } // end Items
      } // end switch
    } // next i
    // Wenn die Rows keine ModelElementNummer kriegen, geht bei GetAllValuesXml schief!
    row.setModelElementNumber(c.cTbl.getRowCount()); // HACK 24.1.2005 / PKÖ
    c.cTbl.addRow(row);
  }

  private static void perfTableColumn(CurrentKeyword curKey, CurContext c) {
    String[] items = null;
    int col = 0;
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attVAL: {
          if (!GuiUtil.isAPI()) {
            col = curAtt.iValue;
          }
        }
        break;
        case attITEMS: {
          if (!GuiUtil.isAPI()) {
            items=getItems(curAtt.sValue);
          }
        }
        break;
      }
    }
    c.cTbl.setColValues(col, items);
  }

  private static void perfBeginToolbar(CurrentKeyword curKey, CurContext c) {
    c.resetXY();
    c.toolBar = new GuiToolbar(curKey.title);
    if (c.pPar == null
    		|| c.pPar.getName().equals("mainPanel")) {
      c.form.getRootPane().addToolBar(c.toolBar);
    } else if (c.currentFrame != null ) {
    	c.currentFrame.getRootPane().addToolBar(c.toolBar);
    } else {
      c.pPar.addToolbar(c.toolBar);
    }
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attTABSTOP: c.toolBar.setTabstop(curAtt.bValue); break;
        case attNAME: c.toolBar.setName(curAtt.sValue); break;
        case attTYPE: if (curAtt.sValue.indexOf("FLOAT") != -1) {
                    c.toolBar.setFloatable(true);
                  }
        if (curAtt.sValue.indexOf("VERTICAL") != -1) {
          c.toolBar.setOrientation(SwingConstants.VERTICAL);
        }
        if (curAtt.sValue.indexOf("ROLLOVER") != -1) {
          c.toolBar.setRollover(true);
        }
                  break;
        case attVISIBLE: c.toolBar.setVisible(curAtt.bValue); break;
      }
    }
  }

  	private static void perfBeginButtonBar(CurrentKeyword curKey, CurContext c) {
      int w=1, h=1;
      float wx=1, wy=1;
      int it=0, ib=0, ir=0, il=0;
      int px=0, py=0;
      int anchor=GridBagConstraints.CENTER;
      int fill=GridBagConstraints.BOTH;

      c.resetXY();
      GuiButtonBar bar = new GuiButtonBar();
      c.buttonBar = bar;
      for (CurrentAttrib curAtt: curKey.vAttrib) {
      	switch (curAtt.iKeyword.intValue()) {
      		// Orientation?
      		case attSIZE: bar.setPreferredSize(getSize(curAtt.sValue)); break;
      		case attUI: bar.setUI(curAtt.sValue); break;
      		case msgACTIVE: bar.setMsgActive(curAtt.sValue); break;
      	}
      }
      c.pPar.add(bar.getMainPanel(), new GridBagConstraints(c.x, c.y, w, h, wx, wy
            ,anchor, fill, new Insets(it,il,ib,ir), px, py));
   }

  	private static void perfBeginButtonBarButton(CurrentKeyword curKey, CurContext c) {
      String imgUrl = null;
      String name = null;
      for (CurrentAttrib curAtt: curKey.vAttrib) {
      	switch (curAtt.iKeyword.intValue()) {
      		case attIMG: imgUrl = curAtt.sValue; break;
      		case attNAME: name = curAtt.sValue; break;
      		case msgACTIVE: break;
      	}
      }
      GuiPanel panel = c.buttonBar.addNamedButton(curKey.title, name, imgUrl);
      if (name != null) {
          panel.setName(name);
      }
      if (c.curContainer != null) {
          c.curContainer.addMember(panel);
          c.parentContainer.push(panel);
          c.curContainer = panel;
        }

      c.poi++;
      c.pPar = panel;

      c.resetXY();
      c.cpPar = new CurrentPanel(panel, c.x, c.y);
      c.cpStack[c.poi]=c.cpPar;
   }

  	private static void perfBeginOutlookBar(CurrentKeyword curKey, CurContext c) {
      int w=1, h=1;
      float wx=1, wy=1;
      int it=0, ib=0, ir=0, il=0;
      int px=0, py=0;
      int anchor=GridBagConstraints.CENTER;
      int fill=GridBagConstraints.BOTH;

      c.resetXY();
      GuiOutlookBar bar = new GuiOutlookBar();
      c.outlookBar = bar;
      for (CurrentAttrib curAtt: curKey.vAttrib) {
      	switch (curAtt.iKeyword.intValue()) {
      		// Orientation?
         	case attSIZE: bar.setPreferredSize(getSize(curAtt.sValue)); break;
      		case msgACTIVE: bar.setMsgActive(curAtt.sValue); break;
      	}
      }
      c.pPar.add(bar.getMainPanel(), new GridBagConstraints(c.x, c.y, w, h, wx, wy
            ,anchor, fill, new Insets(it,il,ib,ir), px, py));
   }

  	private static void perfBeginOutlookBarTab(CurrentKeyword curKey, CurContext c) {
  		String name = null;
      boolean visible = true;
      String msgActive = null;
      for (CurrentAttrib curAtt: curKey.vAttrib) {
          switch (curAtt.iKeyword.intValue()) {
          case attNAME :
        		name = curAtt.sValue;
        		break;
          case attVISIBLE:
            visible = curAtt.bValue;
            break;
          case msgACTIVE:
            msgActive = curAtt.sValue;
            break;
          }
      }
  		GuiTab tab = c.outlookBar.addTab(curKey.title, name);
  		if (msgActive != null) {
  		  tab.setMsgActive(msgActive);
  		}
  		tab.setGuiParent(c.curContainer); // für Controller
  	}

  	private static void perfBeginOutlookBarButton(CurrentKeyword curKey, CurContext c) {
      String imgUrl = null;
      String name = null;
      for (CurrentAttrib curAtt: curKey.vAttrib) {
      	switch (curAtt.iKeyword.intValue()) {
      		case attIMG: imgUrl = curAtt.sValue; break;
      		case attNAME: name = curAtt.sValue; break;
      	}
      }
      GuiPanel panel = c.outlookBar.addButton(curKey.title, name, imgUrl);
      if (c.curContainer != null) {
          c.curContainer.addMember(panel);
          c.parentContainer.push(panel);
          c.curContainer = panel;
      }
      for (CurrentAttrib curAtt: curKey.vAttrib) {
        switch (curAtt.iKeyword.intValue()) {
          case attELEMENT: panel.setElementName(curAtt.sValue); break;
        }
      }
      c.poi++;
      c.pPar = panel;

      c.resetXY();
      c.cpPar = new CurrentPanel(panel, c.x, c.y);
      c.cpStack[c.poi]=c.cpPar;
   }

  	private static void perfBeginTaskPane(CurrentKeyword curKey, CurContext c) {
  		//GuiTaskPane.setUI("com.l2fprod.common.swing.plaf.windows.WindowsLookAndFeelAddons");
        int size = curKey.vAttrib.size();
        for (int i = 0; i < size; i++) {
        	CurrentAttrib curAtt = curKey.vAttrib.get(i);
        	switch (curAtt.iKeyword.intValue()) {
        		case attUI: GuiTaskPane.setUI(curAtt.sValue); break;
        	}
        }

        int w=1, h=1;
        float wx=1, wy=1;
        int it=0, ib=0, ir=0, il=0;
        int px=0, py=0;
        int anchor=GridBagConstraints.CENTER;
        int fill=GridBagConstraints.BOTH;

        c.resetXY();
        GuiTaskPane task = new GuiTaskPane();
        c.taskPane = task;
        for (CurrentAttrib curAtt: curKey.vAttrib) {
        	switch (curAtt.iKeyword.intValue()) {
        		// Orientation?
           		case attSIZE: task.setPreferredSize(getSize(curAtt.sValue)); break;
        		case msgACTIVE: task.setMsgActive(curAtt.sValue); break;
        	}
        }
        c.pPar.add(task.getMainPanel(), new GridBagConstraints(c.x, c.y, w, h, wx, wy
              ,anchor, fill, new Insets(it,il,ib,ir), px, py));
     }

  	private static void perfBeginTaskPaneTab(CurrentKeyword curKey, CurContext c) {
    	JTaskPaneGroup group = c.taskPane.addTab(curKey.title);
      for (CurrentAttrib curAtt: curKey.vAttrib) {
        	switch (curAtt.iKeyword.intValue()) {
		    	case attTT: group.setToolTipText(curAtt.sValue);break;
		    	case attIMG: group.setIcon(GuiUtil.makeIcon(curAtt.sValue));break;

        	}
        }
    }

    private static void perfBeginTaskPaneButton(CurrentKeyword curKey, CurContext c) {
        String imgUrl = null;
        String name = null;
        for (CurrentAttrib curAtt: curKey.vAttrib) {
        	switch (curAtt.iKeyword.intValue()) {
        		case attIMG: imgUrl = curAtt.sValue; break;
        		case attNAME: name = curAtt.sValue; break;
        	}
        }
        GuiPanel panel = c.taskPane.addButton(curKey.title, imgUrl);
        if (name != null) {
            panel.setName(name);
        }
        if (c.curContainer != null) {
            c.curContainer.addMember(panel);
            c.parentContainer.push(panel);
            c.curContainer = panel;
        }
        c.poi++;
        c.pPar = panel;

        c.resetXY();
        c.cpPar = new CurrentPanel(panel, c.x, c.y);
        c.cpStack[c.poi]=c.cpPar;
     }
    private void perfBeginChart(final CurrentKeyword curKey, CurContext c) {
       Platform.setImplicitExit(false); // Wichtig!
       final GuiContainer cont = c.pPar;
       String[] labels = curKey.title.split("\\|"); 
       final GuiChartPanel panel = new GuiChartPanel(labels); // new JFXPanel
         
         LayoutConstraints lc = new LayoutConstraints();
         lc.w = 1;
         lc.h = 1;
         lc.wx = 1;
         lc.wy = 1;
         lc.it = 0;
         lc.ib = 0;
         lc.ir = 0;
         lc.il = 0;
         lc.px = 0;
         lc.py = 0;
         lc.anchor = GridBagConstraints.CENTER;
         lc.fill = GridBagConstraints.BOTH;
         String type = "Line"; // default ChartType
         String name = null;
         for(CurrentAttrib curAtt : curKey.vAttrib) {
            switch(curAtt.iKeyword.intValue()) {
               case attW:
                   lc.w = curAtt.iValue;
                  break;
               case attH:
                   lc.h = curAtt.iValue;
                  break;
               case attTYPE:
                  type = curAtt.sValue;
                  break;
               case attNAME:
                  name = curAtt.sValue;
                  break;
               case attSIZE:
                   Dimension d = getSize(curAtt.sValue);
                   panel.setPreferredSize(d);
                   break;
            }
         }
         if(name != null) {
            panel.setName(name);
         }
         panel.setType(type);
         cont.add(panel, new GridBagConstraints(c.x, c.y, lc.w, lc.h, lc.wx, lc.wy, lc.anchor, lc.fill, new Insets(lc.it, lc.il, lc.ib, lc.ir), lc.px, lc.py));
         if(c.curContainer != null) {
            c.curContainer.addMember(panel);
         }
         
         c.incX(lc.w);
         if(curKey.eol == true) {
            c.incY(lc.h);
         }
         c.cpPar.lx = c.x;
         c.cpPar.ly = c.y;
         c.cpStack[c.poi] = c.cpPar;
         c.poi++;
         c.pPar = panel;
         c.resetXY();
         //c.yoff = 0;
         c.cpPar = new CurrentPanel(panel, c.x, c.y);
         c.cpStack[c.poi] = c.cpPar;
         
         // JFX *************************
         this.createChart(panel);
      }

      private void createChart(final GuiChartPanel panel) {
         Platform.runLater(new Runnable() {
            @Override
            public void run() {
               Scene scene = null;
               String[] labels = panel.getLabels();
               String type = panel.getType();
               if (type.equals("Line")) {
                   final NumberAxis xAxis = new NumberAxis();
                   if (labels.length > 1) {
                       xAxis.setLabel(labels[1]);
                   }
                   final NumberAxis yAxis = new NumberAxis();
                   LineChart<Number, Number> chart = new LineChart<Number, Number>(xAxis,yAxis);
                   if (labels.length > 2) {
                       yAxis.setLabel(labels[2]);
                   }
                   chart.setTitle(labels[0]);
                   scene = new Scene(chart);
               } else if (type.equals("Bar")) {
               
               } else if (type.equals("StackedBar")) {

               } else if (type.equals("Pie")) {
                   PieChart chart = new PieChart();
                   chart.setTitle(labels[0]);
                   scene = new Scene(chart);
               } else if (type.equals("Scatter")) {
                   
               }
               panel.getJFXPanel().setScene(scene);
            }  
         });
      }    
      
      private void perfBeginBrowser(final CurrentKeyword curKey, CurContext c) {
         Platform.setImplicitExit(false); // Wichtig!
         final GuiContainer cont = c.pPar;
         final GuiBrowserPanel panel = new GuiBrowserPanel(curKey.title); // new JFXPanel
           
           LayoutConstraints lc = new LayoutConstraints();
           lc.w = 1;
           lc.h = 1;
           lc.wx = 1;
           lc.wy = 1;
           lc.it = 0;
           lc.ib = 0;
           lc.ir = 0;
           lc.il = 0;
           lc.px = 0;
           lc.py = 0;
           lc.anchor = GridBagConstraints.CENTER;
           lc.fill = GridBagConstraints.BOTH;
           String name = null;
           for(CurrentAttrib curAtt : curKey.vAttrib) {
              switch(curAtt.iKeyword.intValue()) {
                 case attW:
                     lc.w = curAtt.iValue;
                    break;
                 case attH:
                     lc.h = curAtt.iValue;
                    break;
                 case attNAME:
                    name = curAtt.sValue;
                    break;
                 case attSIZE:
                     Dimension d = getSize(curAtt.sValue);
                     panel.setPreferredSize(d);
                     break;
              }
           }
           if(name != null) {
              panel.setName(name);
           }
           cont.add(panel, new GridBagConstraints(c.x, c.y, lc.w, lc.h, lc.wx, lc.wy, lc.anchor, lc.fill, new Insets(lc.it, lc.il, lc.ib, lc.ir), lc.px, lc.py));
           if(c.curContainer != null) {
              c.curContainer.addMember(panel);
           }
           
           c.incX(lc.w);
           if(curKey.eol == true) {
              c.incY(lc.h);
           }
           c.cpPar.lx = c.x;
           c.cpPar.ly = c.y;
           c.cpStack[c.poi] = c.cpPar;
           c.poi++;
           c.pPar = panel;
           c.resetXY();
           //c.yoff = 0;
           c.cpPar = new CurrentPanel(panel, c.x, c.y);
           c.cpStack[c.poi] = c.cpPar;
           
           // JFX *************************
           //this.createChart(panel);
        }
      private void perfBeginJFX(final CurrentKeyword curKey, CurContext c) {
         Platform.setImplicitExit(false); // Wichtig!
         final GuiContainer cont = c.pPar;
         final GuiJFXPanel panel = new GuiJFXPanel(); // new JFXPanel
           
           LayoutConstraints lc = new LayoutConstraints();
           lc.w = 1;
           lc.h = 1;
           lc.wx = 1;
           lc.wy = 1;
           lc.it = 0;
           lc.ib = 0;
           lc.ir = 0;
           lc.il = 0;
           lc.px = 0;
           lc.py = 0;
           lc.anchor = GridBagConstraints.CENTER;
           lc.fill = GridBagConstraints.BOTH;
           String name = null;
           for(CurrentAttrib curAtt : curKey.vAttrib) {
              switch(curAtt.iKeyword.intValue()) {
                 case attX: c.x=curAtt.iValue; break;
                 case attY: c.y=curAtt.iValue; break;
                 case attWX: lc.wx=curAtt.fValue; break;
                 case attWY: lc.wy=curAtt.fValue; break;
                 case attIT: lc.it=curAtt.iValue; break;
                 case attIL: lc.il=curAtt.iValue; break;
                 case attIB: lc.ib=curAtt.iValue; break;
                 case attIR: lc.ir=curAtt.iValue; break;
                 case attPX: lc.px=curAtt.iValue; break;
                 case attPY: lc.py=curAtt.iValue; break;
                 case attAN: lc.anchor=getAnchor(curAtt.sValue); break;
                 case attFILL: lc.fill=getFill(curAtt.sValue); break;

                 case attW:
                     lc.w = curAtt.iValue;
                    break;
                 case attH:
                     lc.h = curAtt.iValue;
                    break;
                 case attNAME:
                    name = curAtt.sValue;
                    break;
                 case attSIZE:
                     Dimension d = getSize(curAtt.sValue);
                     panel.setPreferredSize(d);
                     break;
                 case attSRC: // Verweis auf *.fxml
                     Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                           try {
                              Parent root = FXMLLoader.load(getClass().getClassLoader().getResource(curAtt.sValue));
                              Scene scene = new Scene(root);
                              panel.getJFXPanel().setScene(scene);
                           }
                           catch(Exception e) {
                              System.err.println(e.getMessage());
                              e.printStackTrace();
                           }
                        }
                     });
                    break;
              }
           }
           if(name != null) {
              panel.setName(name);
           }
           cont.add(panel, new GridBagConstraints(c.x, c.y, lc.w, lc.h, lc.wx, lc.wy, lc.anchor, lc.fill, new Insets(lc.it, lc.il, lc.ib, lc.ir), lc.px, lc.py));
           if(c.curContainer != null) {
              c.curContainer.addMember(panel);
           }
           
           c.incX(lc.w);
           if(curKey.eol == true) {
              c.incY(lc.h);
           }
           c.cpPar.lx = c.x;
           c.cpPar.ly = c.y;
           c.cpStack[c.poi] = c.cpPar;
           c.poi++;
           c.pPar = panel;
           c.resetXY();
           //c.yoff = 0;
           c.cpPar = new CurrentPanel(panel, c.x, c.y);
           c.cpStack[c.poi] = c.cpPar;
           
           // JFX *************************
        }

      
  	private static void perfBeginMenubar(CurrentKeyword curKey, CurContext c) {
    c.menuBar = c.getForm().getGuiMenuBar();
    c.curMenu = c.menuBar;
    c.menuStack.push(c.menuBar);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attVISIBLE: c.menuBar.setVisible(curAtt.bValue); break;
      }
    }
  }

  private static void perfEndMenubar(CurrentKeyword curKey, CurContext c) {
    c.curMenu = null;
    if (c.menuStack.empty() == false) {
      c.menuStack.pop();
    } else {
      System.out.println("Warning! MenuStack is empty!");
    }
  }

  private static void perfBeginMenu(CurrentKeyword curKey, CurContext c) {
    final GuiMenu menu = new GuiMenu(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attIMG: menu.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
        case attMN: menu.setMnemonic(curAtt.cValue); break;
        case attNAME: menu.setName(curAtt.sValue); break;
        case attPOINT: menu.setFont(menu.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: menu.setFont(menu.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: menu.setFont(new Font(curAtt.sValue, menu.getFont().getStyle(), menu.getFont().getSize())); break;
        case attVISIBLE: menu.setVisible(curAtt.bValue); break;
      }
    }
    // für Kompatibilität mit alten Spezifikationen, die keinen Container "Menubar" enthalten.
    // 4.12.2004/PKÖ: Zweite Bedingung hinzugefügt; sonst funktionieren in PopupMenus keine UnterMenüs
    if (c.menuBar == null && c.curMenu == null) {
      System.out.println("Warning: Missing <Menubar>");
      c.menuBar = c.getForm().getGuiMenuBar();
      c.curMenu = c.menuBar;
      c.menuStack.push(c.menuBar);
    }
    c.curMenu.add(menu);
    // Set new current Menu
    c.curMenu = menu;
    c.menuStack.push(menu);
    c.yoff=0;
  }

  private static void perfEndMenu(CurrentKeyword curKey, CurContext c) {
    if (c.menuStack.empty() == false) {
      c.menuStack.pop();
      if (c.menuStack.empty() == false) {
        c.curMenu = c.menuStack.peek();
      }
    }
  }

  private static void perfBeginPopup(CurrentKeyword curKey, CurContext c) {
    final GuiPopupMenu menu = new GuiPopupMenu(curKey.title);

    if (c.curComponent != null) {
      c.curComponent.setPopupMenu(menu);
    }

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attNAME: menu.setName(curAtt.sValue); break;
      }
    }
    c.curMenu = menu;
    c.menuStack.push(menu);
    c.yoff=0;
  }

  private static void perfEndPopup(CurrentKeyword curKey, CurContext c) {
    c.curMenu = null;
    if (c.menuStack.empty() == false) {
      c.menuStack.pop();
    } else {
      System.out.println("Warning! MenuStack is empty!");
    }
  }

  private static void perfBeginTree(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=1, wy=1;
    int it=0, ib=0, ir=0, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.CENTER;
    int fill=GridBagConstraints.BOTH;
    final GuiTreeNode top = new GuiTreeNode(curKey.title);
    final GuiTree tree = new GuiTree(top);
    tree.setLabel(curKey.title);
    tree.setName(GuiUtil.labelToName(curKey.title));
    c.currentTree = tree;
    if (c.form != null) {
      tree.setSplit(c.form.getRootPane().getSplit());
      c.form.getRootPane().setCurrentTree(tree);
    }

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        //case attDO: tree.setEnabled(!curAtt.bValue); break;
        case attDO: tree.getTree().setEditable(!curAtt.bValue); break;
        case attFILE: if (!GuiUtil.isAPI() || curAtt.sValue.endsWith(")") == true) top.setFileName(curAtt.sValue); break;
        case attITEMS: top.setFiles(getItems(curAtt.sValue)); break;
        case attTYPE: top.setName(curAtt.sValue); break;
        case attSB: tree.setBackground(getColor(curAtt.sValue)); break;
        case attSF: tree.setForeground(getColor(curAtt.sValue)); break;
        case attPOINT: tree.setFont(tree.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: tree.setFont(tree.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: tree.setFont(new Font(curAtt.sValue, tree.getFont().getStyle(), tree.getFont().getSize())); break;
        case attNAME: tree.setName(curAtt.sValue); break;
        case attREF: tree.setRef(curAtt.sValue); break;
        case attSIZE:
          tree.setPreferredSize(getSize(curAtt.sValue));
          break;
        case attMINSIZE: tree.setMinimumSize(getSize(curAtt.sValue)); break;
        case attMAXSIZE: tree.setMaximumSize(getSize(curAtt.sValue)); break;
        case attVISIBLE: tree.setVisible(curAtt.bValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(tree, curAtt.sValue); break;
        case attIMG: top.setIconName(curAtt.sValue); break;
        case attCLOSED_ICON:
          ((DefaultTreeCellRenderer)tree.getCellRenderer()).setClosedIcon(GuiUtil.makeIcon(curAtt.sValue));
          break;
        case attOPEN_ICON:
          ((DefaultTreeCellRenderer)tree.getCellRenderer()).setOpenIcon(GuiUtil.makeIcon(curAtt.sValue));
          break;
        case attLEAF_ICON:
          ((DefaultTreeCellRenderer)tree.getCellRenderer()).setLeafIcon(GuiUtil.makeIcon(curAtt.sValue));
          break;
				// Messages
        case msgNODECLICK: tree.setMsgNodeClick(curAtt.sValue); break;
			case msgDROP: tree.setMsgDrop(curAtt.sValue); break;
			case msgDRAG_ENTER: tree.setMsgDragEnter(curAtt.sValue); break;
			case msgDRAG_OVER: tree.setMsgDragOver(curAtt.sValue); break;
			case msgDRAG_EXIT: tree.setMsgDragExit(curAtt.sValue); break;
        case msgCREATE: tree.setMsgCreate(curAtt.sValue); break;
        case msgPOPUP: tree.setMsgPopup(curAtt.sValue); break;
				// Bindings
				case attELEMENT: tree.setElementName(curAtt.sValue); break;
				case attROOT_ELEMENT: tree.setElementName("ROOT:"+curAtt.sValue); break;
      }
    } // next i
    c.curComponent = tree; // Set Current Component for Popup
    final GuiScrollBox box = new GuiScrollBox(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    /* Hier ist das Problem mit der LeftComponent
     * eines Split Panels, wenn dort ein Tree eingefügt wird.
     * Diese add-Methode führt dazu, daß die Scroll-Box
     * als left Component gesetzt wird, aber die Info über
     * GuiMember verloren geht.
     */
    c.pPar.add(box, new GridBagConstraints(c.x, c.y, w, h, wx, wy
                      ,anchor, fill, new Insets(it,il,ib,ir), px, py));

    if (c.curContainer != null) {
      c.curContainer.addMember(tree);
    }
    c.yoff=h;
    c.incX(w);
    c.tpoi++;
    c.treeStack[c.tpoi]=top;
    c.curNode=top;
  }

  private static void perfEndTree(CurrentKeyword curKey, CurContext c) {
    // Wurzelknoten expandieren
    c.currentTree.getTree().expandRow(0);
    c.x=0;
  }

  private static void perfBeginFolder(CurrentKeyword curKey, CurContext c) {
    final GuiTreeNode node = new GuiTreeNode(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attNAME: node.setName(curAtt.sValue); break;
        case attTYPE: node.setGuiTreeElementName(curAtt.sValue); break;
        case attFILE: if (!GuiUtil.isAPI() || curAtt.sValue.endsWith(")") == true) node.setFileName(curAtt.sValue); break;
        case attITEMS: node.setFiles(getItems(curAtt.sValue)); break;
        case attIMG: node.setIconName(curAtt.sValue); break;
        case msgNODECLICK: node.setMsgNodeClick(curAtt.sValue); break;
				case attELEMENT: node.setElementName(curAtt.sValue); break;
      }
    }
    c.curNode.getMyTree().getGuiTreeModel().insertNodeInto(node, c.curNode, c.curNode.getChildCount());
    c.tpoi++; // Stackpointer erhöhen
    c.treeStack[c.tpoi]=node;
    c.curNode=node; // current Node merken
    c.yoff=0;
    c.curComponent = node; // Current Component for Popup

  }
  private static void perfEndFolder(CurrentKeyword curKey, CurContext c) {
    c.tpoi--;
    c.curNode=c.treeStack[c.tpoi];
  }

  private static void perfTreeNode(CurrentKeyword curKey, CurContext c) {
    GuiTreeNode node;
    node = new GuiTreeNode(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attNAME: node.setName(curAtt.sValue); break;
        case attTYPE: node.setGuiTreeElementName(curAtt.sValue); break;
        case attFILE: if (!GuiUtil.isAPI() || curAtt.sValue.endsWith(")") == true) node.setFileName(curAtt.sValue); break;
        case attITEMS: node.setFiles(getItems(curAtt.sValue)); break;
        case attIMG: node.setIconName(curAtt.sValue); break;
        case msgNODECLICK: node.setMsgNodeClick(curAtt.sValue); break;
        case msgPOPUP: node.setMsgPopup(curAtt.sValue); break;
        case attELEMENT: node.setElementName(curAtt.sValue); break;
        case attRO: node.setReadonly(curAtt.bValue); break;
      }
    } // next i
    c.curNode.getMyTree().getGuiTreeModel().insertNodeInto(node, c.curNode, c.curNode.getChildCount());
    c.curComponent = node; // Current Component for Popup
    //c.curContainer = node; // geht nicht!
  }

  private static void perfTreeContent(CurrentKeyword curKey, CurContext c) {
    final GuiTreeContent content = new GuiTreeContent(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attNN: content.setMandatory(curAtt.bValue); break;
        case attTYPE: content.setMulti(curAtt.bValue); break;
      }
    }
    c.treeElement.addContent(content);
  }

  private void perfBeginFrame(CurrentKeyword curKey, CurContext c) {
    int w=300, h=300;
    c.x=0; c.y=0;
    final GuiInternalFrame pPanel = new GuiInternalFrame(curKey.title);
    pPanel.setDefaultTitle(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attX: c.frameLocation.x=curAtt.iValue; break;
        case attY: c.frameLocation.y=curAtt.iValue; break;
        case attIMG: pPanel.setIconImage(GuiUtil.makeAwtImage(curAtt.sValue)); break; // nicht so einfach!
        case attNAME: pPanel.setName(curAtt.sValue); break;
        case attREF: pPanel.getRootPane().setRef(curAtt.sValue); break;
        case attLAYOUT: pPanel.getMainPanel().setLayoutManager(getLayout(curAtt.sValue)); break;
				case attCOLSPEC: pPanel.getMainPanel().setColSpec(curAtt.sValue); break;
				case attROWSPEC: pPanel.getMainPanel().setRowSpec(curAtt.sValue); break;
        case attCOLS: pPanel.getMainPanel().setGridColumns(curAtt.iValue); break;
        case attCONTROLLER: c.form.setController(curAtt.sValue); break;
        case attSIZE: final Dimension size = getSize(curAtt.sValue);
					  w = size.width;
					  h = size.height;
					  break;
				case attELEMENT: pPanel.getRootPane().getMainPanel().setElementName(curAtt.sValue); break;
				case attCLOSEABLE: pPanel.getInternalFrame().setClosable(curAtt.bValue); break;
				case attICONABLE: pPanel.getInternalFrame().setIconifiable(curAtt.bValue); break;
				case attMAXABLE: pPanel.getInternalFrame().setMaximizable(curAtt.bValue); break;
				case attAUTOSIZE: pPanel.setAutoSize(curAtt.bValue); break;
      }
    }
    pPanel.getComponent().setSize(w, h);

    // Die mainPanels der Frames müssen verschieden heißen für XmlSave!
    pPanel.getMainPanel().setName("frame_"+pPanel.getName());
    // Desktop
    if (c.pPar instanceof GuiPanel) {
      final GuiPanel tmpPanel = (GuiPanel)c.pPar;
      GuiDesktop desktop = tmpPanel.getDesktop();
      if (desktop == null) {
        desktop = new GuiDesktop();
        tmpPanel.setDesktop(desktop);
      }
      desktop.add(pPanel.getComponent(), JLayeredPane.PALETTE_LAYER);
      //desktop.setSelectedFrame(pPanel);
      desktop.getDesktopManager().activateFrame(pPanel.getInternalFrame());
      pPanel.getComponent().setLocation(c.frameLocation);
      c.frameLocation.setLocation(c.frameLocation.getX()+20, c.frameLocation.getY()+20);
      // Frame immer als ChildContainer?
      if (c.curContainer != null) {
        c.curContainer.addMember(pPanel.getMainPanel());
        c.parentContainer.push(pPanel.getMainPanel());
        c.curContainer = pPanel.getMainPanel();
      }
    } else {
      // nicht erlaubt!
    }
    c.currentFrame = pPanel;
    c.cpPar.lx=c.x;
    c.cpPar.ly=c.y;
    c.cpStack[c.poi]=c.cpPar;

    c.poi++;
    c.resetXY();
    c.pPar = pPanel.getMainPanel();
    c.cpPar = new CurrentPanel(c.pPar, c.x, c.y);
    c.cpStack[c.poi]=c.cpPar;
  }

  private static void perfEndFrame(CurrentKeyword curKey, CurContext c) {
    if (c.poi > 0 ) {
    	// Show am Ende wegen pack()
    	if (c.currentFrame != null) {
    	  	c.currentFrame.show();
    	}
      // Internal Frame immer als ChildContainer
      c.parentContainer.pop();
      c.curContainer = c.parentContainer.peek();
      c.poi--; // Stackpointer für Panels runterzählen
      c.cpPar=c.cpStack[c.poi]; // CurrentPanel setzen
      c.pPar=c.cpPar.p;
      c.x=c.cpPar.lx;
      c.y=c.cpPar.ly;
      c.x=0;
    }
  }
  private void perfBeginSplit(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=1, wy=1;
    int it=0, ib=0, ir=0, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.CENTER;
    int fill=GridBagConstraints.BOTH;
    final GuiSplit split = new GuiSplit();
    if (curKey.title == null || curKey.title.length() == 0) {
      splitCounter++;
      curKey.title = "split" + splitCounter;
    }
    split.setName(GuiUtil.labelToName(curKey.title));
    c.form.getRootPane().setSplit(split);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attNAME: split.setName(curAtt.sValue); break;
        case attREF: split.setRef(curAtt.sValue); break;
        case attTYPE: split.setOrientation(curAtt.sValue); break;
        case attLOC: split.setDividerLocation(curAtt.iValue, true); break;
        // todo : element
      }
    } // next i
    c.pPar.add(split, new GridBagConstraints(c.x, c.y, w, h, wx, wy
             ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    split.setGuiParent(c.pPar); // New 10.12.2003 PKÖ
    c.incX(w);
    c.incY(h);
    c.cpPar.lx=c.x;
    c.cpPar.ly=c.y;
    c.cpStack[c.poi]=c.cpPar;

    c.poi++;
    c.pPar = split;

    c.resetXY();
    c.cpPar = new CurrentPanel(c.pPar, c.x, c.y);
    c.cpStack[c.poi]=c.cpPar;
  }
  private static void perfEndSplit(CurrentKeyword curKey, CurContext c) {
     
     if (c.poi > 0 ) {
      if (c.parentContainer.peek() == c.pPar) {
        c.parentContainer.pop();
        c.curContainer = c.parentContainer.peek();
      }
      c.poi--; // Stackpointer für Panels runterzählen
      c.cpPar=c.cpStack[c.poi]; // CurrentPanel setzen
      c.pPar=c.cpPar.p;
      c.x=c.cpPar.lx;
      c.y=c.cpPar.ly;
      c.x=0;
//      for (CurrentAttrib curAtt: curKey.vAttrib) {
//         switch (curAtt.iKeyword.intValue()) {
//            case attLOC: ((GuiSplit)c.pPar).setDividerLocation(curAtt.iValue); 
//         }
//      }
    }
  }
  /*
  private static void perfBeginBox(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    wx=1; wy=1;
    // Use?
    if (uw>=0) {w=uw; uw=-1;};
    if (uh>=0) {h=uh; uh=-1;};
    if (uwx>=0) {wx=uwx; uwx=-1;};
    if (uwy>=0) {wy=uwy; uwy=-1;};
    it=0; ib=0; ir=0; il=0;
    int px=0, py=0;
    anchor = GridBagConstraints.CENTER;
    fill = GridBagConstraints.BOTH;
    GuiBox pPanel;
    if (curKey.title.equals("X")) {
      pPanel = new GuiBox(BoxLayout.X_AXIS);
    }
    else {
      pPanel = new GuiBox(BoxLayout.Y_AXIS);
    }
    pPanel.setName(GuiUtil.labelToName(curKey.title));
    pPanel.setRootPane(form.getGuiRootPane());
    for (a = curKey.vAttrib.iterator() ; a.hasNext() ;) {
      curAtt=(CurrentAttrib)a.next();
      switch (curAtt.iKeyword.intValue()) {
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attX: x=curAtt.iValue; break;
        case attY: y=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attSB: pPanel.setBackground(getColor(curAtt.sValue)); break;
        case attSF: pPanel.setForeground(getColor(curAtt.sValue)); break;
        case attNAME: pPanel.setName(curAtt.sValue); break;
        case attREF: childRef = curAtt.sValue;
                curContainer.addMember(pPanel);
                parentContainer.push(pPanel);
                curContainer = pPanel;
                break;
      }
    } // next i
    pPar.add(pPanel, new GridBagConstraints(x, y, w, h, wx, wy
             ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    x=x+w;
    y=y+h;
    cpPar.lx=x;
    cpPar.ly=y;
    cpStack[poi]=cpPar;

    poi++;
    pPar = pPanel;

    x=0;
    y=0;
    yoff=0;
    cpPar = new CurrentPanel(pPanel, x, y);
    cpStack[poi]=cpPar;


  }
  private static void perfEndBox(CurrentKeyword curKey, CurContext c) {
  }
  */

  private static void perfBeginElement(CurrentKeyword curKey, CurContext c) {
  	String name = curKey.title;
    String fileName = null;
    String iconName = null;
    String msgNodeClick = null;
    String elementName = null;
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
      	case attNAME: // Nur wenn Label fehlt
      		if (name == null || name.length() == 0) {
      			name = curAtt.sValue;
      		}
      		break;
        case attFILE: if (!GuiUtil.isAPI() || curAtt.sValue.endsWith(")") == true) fileName = curAtt.sValue; break;
        case attIMG: iconName = curAtt.sValue; break;
        case msgNODECLICK: msgNodeClick = curAtt.sValue; break; // New 2.1.2004 PKÖ
				// Bindings
				case attELEMENT: elementName = curAtt.sValue; break;
      }
    }
    c.treeElement = new GuiTreeElement(name, fileName);
    c.treeElement.setIconName(iconName);
    c.treeElement.setMsgNodeClick(msgNodeClick);
    c.treeElement.setElementName(elementName);

    c.curComponent = c.treeElement; // für Popup
  }
//  private static void perfEndElement(CurrentKeyword curKey, CurContext c) {
//  	// nüscht
//  }

  // Components ****************************************************************
  private static void perfButton(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=0, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor = GridBagConstraints.NORTHWEST;
    int fill = GridBagConstraints.NONE;
    String fileName = "";
    final GuiButton pb = new GuiButton(curKey.title);
    String cmd = null;
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attTT: pb.setToolTipText(curAtt.sValue); break;
        case attST: pb.setHint(curAtt.sValue); break;
        case attDO: pb.setEnabled(!curAtt.bValue); break;
        case attAL: pb.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
        case attIMG: pb.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
        case attFILE: fileName = curAtt.sValue; // Eingebaute Methode
        if (!GuiUtil.isAPI() || curAtt.sValue.endsWith(")") == true) pb.setFileName(fileName);
            if (c.cTbl != null) {
              pb.setTable(c.cTbl);
            }
           break;
        case attACC: pb.setAccelerator(getKey(curAtt.sValue)); break;
        case attMN: pb.setMnemonic(curAtt.cValue); break;
        case attCMD: cmd = curAtt.sValue; pb.setActionCommand(curAtt.sValue); break;
        case attSB: pb.setBackground(getColor(curAtt.sValue)); break;
        case attSF: pb.setForeground(getColor(curAtt.sValue)); break;
        case attPOINT: pb.setFont(pb.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: pb.setFont(pb.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: pb.setFont(new Font(curAtt.sValue, pb.getFont().getStyle(), pb.getFont().getSize())); break;
        case attMIN: pb.getMinimumSize().width = curAtt.iValue; break;
        case attMAX: pb.getMaximumSize().width = curAtt.iValue; break;
        case attNAME: pb.setName(curAtt.sValue); break;
        case attREF: pb.setRef(curAtt.sValue); break; // merken für Suchdialog
        case attSIZE: pb.setPreferredSize(getSize(curAtt.sValue)); break;
        case attTABSTOP: pb.setTabstop(curAtt.bValue); break;
        case attVISIBLE: pb.setVisible(curAtt.bValue); break;
        case attTYPE: pb.setType(curAtt.sValue); break;
  		  case attHELPID: GuiSession.getInstance().setHelpId(pb, curAtt.sValue); break;
  		  case attBORDER: pb.getJComponent().setBorder(null); break;
        // Messages
        case msgMOUSEOVER: pb.setMsgMouseOver(curAtt.sValue); break;
        case msgPOPUP: pb.setMsgPopup(curAtt.sValue); break;
      }
    } // next i
    if (c.toolBar != null) {
      c.toolBar.addGuiTool(pb);
    } else {
      c.pPar.add(pb, new GridBagConstraints(c.x, c.y, w, h, wx, wy
            ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    }
    GuiRootPane root = pb.getRootPane();
    // Default Button setzen?
    if (pb.getText().equals("OK")
          || pb.getActionCommand().equalsIgnoreCase("OK")
          || pb.getName().equalsIgnoreCase("OK")
          || pb.getType() == GuiAction.OK) {
      // Wenn Panels einzeln erzeugt werden, gibts (noch) keine RootPane!
      if (root != null) {
        root.setDefaultButton(pb.getButton());
      }
      if (fileName.length() == 0 && GuiUtil.isAPI() == false && (cmd == null || cmd.equals("ok") )) { // schließen
        pb.setFileName("Close()");
      }
    } else if(pb.getText().equals("Abbrechen")
          || pb.getText().equals("Cancel")
          || pb.getType() == GuiAction.CANCEL) {
      // Bei Cancel-Buttons keine Input-Validierung vornehmen.
      pb.getJComponent().setVerifyInputWhenFocusTarget(false);
      if (fileName.length() == 0 && GuiUtil.isAPI() == false && (cmd == null || cmd.equals("canel"))) { // schließen
        pb.setFileName("Cancel()");
      }
    }
    c.curContainer.addAction(pb);
    c.curComponent = pb; // Current Component for Popup
    c.yoff=h;
    c.incX(w);
  }

  private static void perfTButton(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=0, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor = GridBagConstraints.NORTHWEST;
    int fill = GridBagConstraints.NONE;
    final GuiTButton pb = new GuiTButton(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attTT: pb.setToolTipText(curAtt.sValue); break;
        case attST: pb.setHint(curAtt.sValue); break;
        case attDO: pb.setEnabled(!curAtt.bValue); break;
        case attAL: pb.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
        case attIMG: pb.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
        case attSICON: pb.setSelectedIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
        case attVAL: if (!GuiUtil.isAPI()) { pb.setSelected(curAtt.bValue); }
          break;
        /*
        case attFILE: fileName = curAtt.sValue;
            pb.setFileName(fileName);
           break;
           */
        case attMN: pb.setMnemonic(curAtt.cValue); break;
        case attSB: pb.setBackground(getColor(curAtt.sValue)); break;
        case attSF: pb.setForeground(getColor(curAtt.sValue)); break;
        case attPOINT: pb.setFont(pb.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: pb.setFont(pb.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: pb.setFont(new Font(curAtt.sValue, pb.getFont().getStyle(), pb.getFont().getSize())); break;
        case attMIN: pb.getMinimumSize().width = curAtt.iValue; break;
        case attMAX: pb.getMaximumSize().width = curAtt.iValue; break;
        case attNAME: pb.setName(curAtt.sValue); break;
        case attREF: pb.setRef(curAtt.sValue); break; // merken für Suchdialog
        case attSIZE: pb.setPreferredSize(getSize(curAtt.sValue)); break;
        case attTABSTOP: pb.setTabstop(curAtt.bValue); break;
        case attVISIBLE: pb.setVisible(curAtt.bValue); break;
        case attINVERT: pb.setInvert(curAtt.bValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(pb, curAtt.sValue); break;
        case attRESTORE: pb.setRestore(curAtt.bValue); break;
        // Messages
        case msgCHANGE: pb.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: pb.setMsgMouseOver(curAtt.sValue); break;
      }
    } // next i
    if (c.toolBar != null) {
      c.toolBar.addGuiTool(pb);
    } else {
      c.pPar.add(pb, new GridBagConstraints(c.x, c.y, w, h, wx, wy
            ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    }
    if (c.curContainer != null) {
      c.curContainer.addMember(pb);
    }
    c.yoff=h;
    c.incX(w);
  }

  private static void perfDocument(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=0, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor = GridBagConstraints.NORTHWEST;
    int fill = GridBagConstraints.NONE;
    final GuiDocument pb = new GuiDocument(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attTT: pb.setToolTipText(curAtt.sValue); break;
        case attST: pb.setHint(curAtt.sValue); break;
        case attDO: pb.setEnabled(!curAtt.bValue); break;
        case attAL: pb.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
        case attIMG: pb.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
        //case attVAL: pb.setTemplate(curAtt.sValue); break;
        case attTYPE: pb.setTemplate(curAtt.sValue); break;
        case attFILE: if (!GuiUtil.isAPI() || curAtt.sValue.endsWith(")") == true) pb.setValue(curAtt.sValue); break; // Eingebaute Methode
        case attMN: pb.setMnemonic(curAtt.cValue); break;
        case attSB: pb.setBackground(getColor(curAtt.sValue)); break;
        case attSF: pb.setForeground(getColor(curAtt.sValue)); break;
        case attPOINT: pb.setFont(pb.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: pb.setFont(pb.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: pb.setFont(new Font(curAtt.sValue, pb.getFont().getStyle(), pb.getFont().getSize())); break;
        case attMIN: pb.getMinimumSize().width = curAtt.iValue; break;
        case attMAX: pb.getMaximumSize().width = curAtt.iValue; break;
        case attNAME: pb.setName(curAtt.sValue); break;
        case attREF: pb.setRef(curAtt.sValue); break; // merken für Suchdialog
        case attSIZE: pb.setPreferredSize(getSize(curAtt.sValue)); break;
        case attTABSTOP: pb.setTabstop(curAtt.bValue); break;
        case attVISIBLE: pb.setVisible(curAtt.bValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(pb, curAtt.sValue); break;
        // Messages
        case msgCHANGE: pb.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: pb.setMsgMouseOver(curAtt.sValue); break;
        case msgPOPUP: pb.setMsgPopup(curAtt.sValue); break;
      }
    } // next i
    if (c.toolBar != null) {
      c.toolBar.addGuiTool(pb);
    } else {
      c.pPar.add(pb, new GridBagConstraints(c.x, c.y, w, h, wx, wy
            ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    }
    if (c.curContainer != null) {
      c.curContainer.addMember(pb);
    }
    c.curComponent = pb; // Current Component for Popup
    c.yoff=h;
    c.incX(w);
  }


  private static void perfCheck(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    if (c.isTable==true) w=50;
    //int min=40; int max=60;}
    float wx=0, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.NORTHWEST;
    int fill=GridBagConstraints.NONE;
    final GuiCheck chk = new GuiCheck(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attTT: chk.setToolTipText(curAtt.sValue); break;
        case attST: chk.setHint(curAtt.sValue); break;
        case attDO: chk.setEnabled(!curAtt.bValue); break;
        case attAL: chk.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
        case attIMG: chk.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
        case attMN: chk.setMnemonic(curAtt.cValue); break;
        case attVAL: chk.setSelected(curAtt.bValue); break; // Default-Value
        case attSB: chk.setBackground(getColor(curAtt.sValue)); break;
        case attSF: chk.setForeground(getColor(curAtt.sValue)); break;
        case attPOINT: chk.setFont(chk.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: chk.setFont(chk.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: chk.setFont(new Font(curAtt.sValue, chk.getFont().getStyle(), chk.getFont().getSize())); break;
        case attMIN: int min=curAtt.iValue;
                chk.setMinimumSize(new Dimension(min, chk.getMinimumSize().height));
        break;
        case attMAX: int max=curAtt.iValue;
                chk.setMaximumSize(new Dimension(max, chk.getMaximumSize().height));
        break;
        case attNAME: chk.setName(curAtt.sValue); break;
        case attREF: chk.setRef(curAtt.sValue); break;
        case attSIZE: chk.setPreferredSize(getSize(curAtt.sValue)); break;
        case attTABSTOP: chk.setTabstop(curAtt.bValue); break;
        case attVISIBLE: chk.setVisible(curAtt.bValue); break;
        case attLINKCOL: chk.setLinkColumn(c, curAtt.sValue); break;
        case attINVERT: chk.setInvert(curAtt.bValue); break;
        case attMAP: chk.setMap(getItems(curAtt.sValue)); break;
        case attHELPID: GuiSession.getInstance().setHelpId(chk, curAtt.sValue); break;
        case attRESTORE: chk.setRestore(curAtt.bValue); break;
        case attHTP: chk.setHorizontalTextPosition(getAlign(curAtt.sValue)); break;
        // Messages
        case msgCHANGE: chk.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: chk.setMsgMouseOver(curAtt.sValue); break;
        // Bindings
        case attELEMENT: chk.setElementName(curAtt.sValue); break;
      }
    } // next i
    if (c.isTable == false) {
      c.pPar.add(chk, new GridBagConstraints(c.x, c.y, w, h, wx, wy
        ,anchor, fill, new Insets(it,il,ib,ir), px, py));
      if (c.curContainer != null) {
        c.curContainer.addMember(chk);
      }
      c.yoff=h;
      c.incX(w);
    }
    else { // isTable
      c.cTbl.addColumn(chk, curKey.title, w);
      curKey.eol = false; // Wenn Tabellenspalte, dann Layout nicht weiterzählen
    }
  }

  private static void perfCombo(CurrentKeyword curKey, CurContext c) {
    int w=3, h=1;
    int min=50, max=120;
    if (c.isTable==true) {w=50;}
    float wx=1, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.NORTHWEST;
    int fill=GridBagConstraints.HORIZONTAL;
    Color lblSB = null;
    Color lblSF = null;
    final GuiCombo cmb = new GuiCombo();
    cmb.setLabel(curKey.title);
    cmb.setName(GuiUtil.labelToName(curKey.title));
    int selectedIndex = -1; // default
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attDO: cmb.setEnabled(!curAtt.bValue); break;
        case attNN: cmb.setNotnull(true); break;
        case attTT: cmb.setToolTipText(curAtt.sValue); break;
        case attST: cmb.setHint(curAtt.sValue); break;
        case attITEMS: if (!GuiUtil.isAPI()) { cmb.setItems(getItems(curAtt.sValue));} break;
        case attMAP: if (!GuiUtil.isAPI()) {cmb.setMap(getItems(curAtt.sValue));} break;
        case attFILE: if (!GuiUtil.isAPI()) {cmb.setItems(getItemsFile(curAtt.sValue));} break;
        case attVAL: if (!GuiUtil.isAPI()) { selectedIndex = curAtt.iValue;}
          break;
        case attSB: cmb.setBackground(getColor(curAtt.sValue)); break;
        case attSF: cmb.setForeground(getColor(curAtt.sValue)); break;
        case attLSB: lblSB = getColor(curAtt.sValue); break;
        case attLSF: lblSF = getColor(curAtt.sValue); break;
        case attPOINT: cmb.setFont(cmb.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: cmb.setFont(cmb.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: cmb.setFont(new Font(curAtt.sValue, cmb.getFont().getStyle(), cmb.getFont().getSize())); break;
        case attMIN: min=curAtt.iValue;
            cmb.setMinimumSize(new Dimension(min, cmb.getMinimumSize().height));
            break;
        case attMAX: max=curAtt.iValue;
            cmb.setMaximumSize(new Dimension(max, cmb.getMaximumSize().height));
            break;
        case attNAME: cmb.setName(curAtt.sValue); break;
        case attREF: cmb.setRef(curAtt.sValue); break;
        case attTYPE: if (curAtt.sValue.equals("EDIT")) {
                    cmb.setEditable(true);
                  }
            break;
        case attSIZE: cmb.setPreferredSize(getSize(curAtt.sValue)); break;
        case attMINSIZE: cmb.setMinimumSize(getSize(curAtt.sValue)); break;
        case attMAXSIZE: cmb.setMaximumSize(getSize(curAtt.sValue)); break;
        case attTABSTOP: cmb.setTabstop(curAtt.bValue); break;
        case attVISIBLE: cmb.setVisible(curAtt.bValue); break;
        case attLINKCOL: cmb.setLinkColumn(c, curAtt.sValue); break;
        case attMINLEN: cmb.setMinlen(curAtt.iValue); break;
        case attMAXLEN: cmb.setMaxlen(curAtt.iValue); break;
        case attSEARCH: cmb.setSearch(curAtt.bValue); break;
        case attREGEXP: cmb.setRegexp(curAtt.sValue); break;
        case attNODE_TITLE: cmb.setNodeTitle(curAtt.iValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(cmb, curAtt.sValue); break;
        case attRESTORE: cmb.setRestore(curAtt.bValue); break;
        // Messages
        case msgLOSTFOCUS: cmb.setMsgLostFocus(curAtt.sValue); break;
        case msgCHANGE: cmb.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: cmb.setMsgMouseOver(curAtt.sValue); break;
          // Bindings
          case attELEMENT: cmb.setElementName(curAtt.sValue); break;
          case attDATASET: cmb.setDatasetName(curAtt.sValue); break;
          case attDISPLAY_MEMBER: cmb.setDisplayMember(curAtt.sValue); break;
          case attVALUE_MEMBER: cmb.setValueMember(curAtt.sValue); break;
      }
    } // next i
    // Fill Combo Variante 1
    {
	    if (cmb.getDatasetName() != null && cmb.getDisplayMember() != null) {
	    	try {
	    		cmb.pullData();
	    	} catch (Exception ex) {
	    		System.err.println(ex.getMessage());
	    	}
	    }
    }
    // Fill Combo Variant 2
    {
    	String namedStatement = cmb.getRef();
    	ComboBoxAdapterIF ada = GuiSession.getInstance().getComboBoxAdapter();
	    if (namedStatement != null && ada != null) {
	    	JDataSet ds = ada.findNamedDataSet(namedStatement);
	    	cmb.setItems(ds);
	    }
    }
    // val= wird erst hier gesetzt, wegen der Reihenfolge der Argumente.
    // Sonst gibts eine Fehlermeldung, wenn Items erst später gesetzt werden.
    if (selectedIndex != -1) {
       try {
           cmb.setSelectedIndex(selectedIndex);
       } catch (Exception ex) {
           System.err.println("GuiFactory#perfCombo; Error setting selectedIndex ["+selectedIndex+"]: "+cmb.getName());
       }
    }
    if (curKey.title.length() > 0 && c.isTable == false
        && (c.pPar.getLayoutManager() == GuiContainer.GRIDBAG
        || c.pPar.getLayoutManager() == GuiContainer.FORM
        || c.pPar.getLayoutManager() == GuiContainer.TABLE) ) {
      // final GuiLabel lbl = new GuiLabel(cmb, curKey.title);
      final GuiLabel lbl = new GuiLabel(cmb, cmb.getLabel());
      if (lblSB != null) {
        lbl.getJComponent().setOpaque(true);
        lbl.setBackground(lblSB);
      }
      if (lblSF != null) {
        lbl.setForeground(lblSF);
      }
      lbl.setVisible(cmb.isVisible());
      // grid0
      if (c.grid0 != 0 && c.x == 0) {
        lbl.setPreferredSize(new Dimension(c.grid0, 17));
      }
      c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, 1, 1, 0, 0,
                       makeLabelAnchor(anchor), GridBagConstraints.NONE, new Insets(it, il, ib, 5), LPX, 0));
      c.x++;
      il=0;
    }
    if (c.isTable == false) { // Panel
      c.pPar.add(cmb, new GridBagConstraints(c.x, c.y, w, h, wx, wy
                    ,anchor, fill, new Insets(it,il,ib,ir), px, py));
      if (c.curContainer != null) {
        c.curContainer.addMember(cmb);
      }
      c.yoff=h;
      c.incX(w);
    } else {  // isTable
      c.cTbl.addColumn(cmb, curKey.title, w);
      curKey.eol = false; // Wenn Tabellenspalte, dann Layout nicht weiterzählen
    }
  }

  private static void perfList(CurrentKeyword curKey, CurContext c) {
    int w=3, h=1;
    float wx=1, wy=1;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.NORTHWEST;
    int fill=GridBagConstraints.BOTH;
    Color lblSB = null;
    Color lblSF = null;

    final GuiList lst = new GuiList();
    if (curKey.title.length() > 0) {
      lst.setLabel(curKey.title);
      lst.setName(GuiUtil.labelToName(curKey.title)); // Default-Name
    }

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attTT: lst.setToolTipText(curAtt.sValue); break;
        case attST: lst.setHint(curAtt.sValue); break;
        case attDO:  lst.setEnabled(!curAtt.bValue); break;
        case attNN: lst.setNotnull(true); break;
        case attITEMS: if (!GuiUtil.isAPI()) {lst.setItems(getItems(curAtt.sValue));} break;
        case attMAP: if (!GuiUtil.isAPI()) {lst.setMap(getItems(curAtt.sValue));} break;
        case attFILE: if (!GuiUtil.isAPI()) {lst.setItems(getItemsFile(curAtt.sValue));} break;
        case attVAL: if (!GuiUtil.isAPI()) { lst.setSelectedIndex(curAtt.iValue);}
          break;
        case attSB: lst.setBackground(getColor(curAtt.sValue)); break;
        case attSF: lst.setForeground(getColor(curAtt.sValue)); break;
        case attLSB: lblSB = getColor(curAtt.sValue); break;
        case attLSF: lblSF = getColor(curAtt.sValue); break;
        case attPOINT: lst.setFont(lst.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: lst.setFont(lst.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: lst.setFont(new Font(curAtt.sValue, lst.getFont().getStyle(), lst.getFont().getSize())); break;
        case attNAME: lst.setName(curAtt.sValue); break;
        case attMIN:
            int min=curAtt.iValue;
                lst.setMinimumSize(new Dimension(min, lst.getMinimumSize().height));
          break;
        case attMAX:
          int max=curAtt.iValue;
          lst.setMaximumSize(new Dimension(max, lst.getMaximumSize().height));
          break;
        case attREF: lst.setRef(curAtt.sValue); break;
        case attSIZE: lst.setPreferredSize(getSize(curAtt.sValue)); break;
        case attMINSIZE: lst.setMinimumSize(getSize(curAtt.sValue)); break;
        case attMAXSIZE: lst.setMaximumSize(getSize(curAtt.sValue)); break;
        case attTABSTOP: lst.setTabstop(curAtt.bValue); break;
        case attTYPE: lst.setListboxType(curAtt.sValue); break;
        case attVISIBLE: lst.setVisible(curAtt.bValue); break;
        case attLINKCOL: lst.setLinkColumn(c, curAtt.sValue); break;
        case attLAYOUT: lst.setLayout(curAtt.sValue); break;
        case attDRAG: lst.setDrag(curAtt.bValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(lst, curAtt.sValue); break;
        case attRESTORE: lst.setRestore(curAtt.bValue); break;
        // Messages
        case msgLOSTFOCUS: lst.setMsgLostFocus(curAtt.sValue); break;
        case msgDBLCLICK: lst.setMsgDblClick(curAtt.sValue); break;
        case msgCHANGE: lst.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: lst.setMsgMouseOver(curAtt.sValue); break;
        case msgPOPUP: lst.setMsgPopup(curAtt.sValue); break;
        //case msgFILEDROP: lst.setMsgFileDrop(curAtt.sValue); break;
        // Bindings
        case attELEMENT: lst.setElementName(curAtt.sValue); break;
        case attDISPLAY_MEMBER: lst.setDisplayMember(curAtt.sValue); break;
        case attVALUE_MEMBER: lst.setValueMember(curAtt.sValue); break;
      }
    } // next i

    if (curKey.title.length() > 0) {
      // final GuiLabel lbl = new GuiLabel(lst, curKey.title);
      final GuiLabel lbl = new GuiLabel(lst, lst.getLabel());
      lbl.setVisible(lst.isVisible());
      if (lblSB != null) {
        lbl.getJComponent().setOpaque(true);
        lbl.setBackground(lblSB);
      }
      if (lblSF != null) {
        lbl.setForeground(lblSF);
      }
      // grid0
      if (c.grid0 != 0 && c.x == 0) {
        lbl.setPreferredSize(new Dimension(c.grid0, 17));
      }
      c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, 1, 1, 0, 0
                       ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(it, il, ib, 5), LPX, 0));
      c.x++;
      il=0;
    }
    //final JScrollPane scrollPane = new JScrollPane(lst.getJComponent());
    GuiScrollBox scrollPane = new GuiScrollBox(lst);
    c.pPar.add(scrollPane , new GridBagConstraints(c.x, c.y, w, h, wx, wy
                    ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    if (c.curContainer != null) {
      c.curContainer.addMember(lst);
    }
    c.curComponent = lst;
    c.yoff=h;
    c.incX(w);
  }

  private static void perfItem(CurrentKeyword curKey, CurContext c) {
    if (curKey.title.equals("-")) {
      c.curMenu.addSeparator();
    } else {
      final GuiMenuItem item = new GuiMenuItem(curKey.title, c.curMenu);
      for (CurrentAttrib curAtt: curKey.vAttrib) {
        switch (curAtt.iKeyword.intValue()) {
          case attIMG: item.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
          case attMN: item.setMnemonic(curAtt.cValue); break;
          case attCMD: item.setActionCommand(curAtt.sValue); break;
          case attDO: item.setEnabled(!curAtt.bValue); break;
          case attFILE: if (!GuiUtil.isAPI() || curAtt.sValue.endsWith(")") == true) item.setFileName(curAtt.sValue) ;break; // Eingebaute Methode
          case attACC: item.setAccelerator(getKey(curAtt.sValue)); break;
          case attNAME: item.setName(curAtt.sValue); break;
          case attPOINT: item.setFont(item.getFont().deriveFont(curAtt.fValue)); break;
          case attSTYLE: item.setFont(item.getFont().deriveFont(getStyle(curAtt.sValue))); break;
          case attFONT: item.setFont(new Font(curAtt.sValue, item.getFont().getStyle(), item.getFont().getSize())); break;
          //case attVISIBLE: mItem.setVisible(curAtt.bValue); break;
          case attTYPE: item.setType(curAtt.sValue); break;
          //##case attCOLS: item.setEnabledColumns(curAtt.sValue);
    		case attHELPID: GuiSession.getInstance().setHelpId(item, curAtt.sValue); break;
    		case attENABLED_WHEN: item.setEnabledWhen(curAtt.cValue); break;
    		case attTT: item.setToolTipText(curAtt.sValue); break;
        }
      } // next i
      c.curContainer.addAction(item);
      c.curComponent = item;
//      GuiRootPane root = item.getRootPane();
//      if(item.getType() == GuiAction.HELP && root != null && root.getHelpId() != null) {
//      	HelpBroker helpBroker = GuiSession.getInstance().getHelpBroker();
//      	helpBroker.enableHelpKey(root, root.getHelpId(), null);
//      }

    } // End no Separator
  }

  private static void perfItemCheck(CurrentKeyword curKey, CurContext c) {
    final GuiMenuItemCheckBox item = new GuiMenuItemCheckBox(curKey.title, c.curMenu);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attIMG: item.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
        case attMN: item.setMnemonic(curAtt.cValue); break;
        case attCMD: item.setActionCommand(curAtt.sValue); break;
        case attDO: item.setEnabled(!curAtt.bValue); break;
        case attFILE: if (!GuiUtil.isAPI() || curAtt.sValue.endsWith(")") == true) item.setFileName(curAtt.sValue) ;break;
        case attACC: item.setAccelerator(getKey(curAtt.sValue)); break;
        case attNAME: item.setName(curAtt.sValue); break;
        case attVAL: item.getButton().setState(curAtt.bValue); break; // Default Value
        case attPOINT: item.setFont(item.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: item.setFont(item.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: item.setFont(new Font(curAtt.sValue, item.getFont().getStyle(), item.getFont().getSize())); break;
        //case attVISIBLE: mItem.setVisible(curAtt.bValue); break;
        case attINVERT: item.setInvert(curAtt.bValue); break;
        case attMAP: item.setMap(getItems(curAtt.sValue)); break;
        case attRESTORE: item.setRestore(curAtt.bValue); break;
        case attENABLED_WHEN: item.setEnabledWhen(curAtt.cValue); break;
        // Messages
        case msgCHANGE: item.setMsgChange(curAtt.sValue); break;
        // Bindings
        case attELEMENT: item.setElementName(curAtt.sValue); break;
        case attTT: item.setToolTipText(curAtt.sValue); break;
      }
    } // next i
    if (c.curContainer != null) {
      c.curContainer.addMember(item);
    }
  }

  private static void perfItemOption(CurrentKeyword curKey, CurContext c) {
    final GuiMenuItemOption item = new GuiMenuItemOption(curKey.title, c.curMenu);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attIMG: item.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
        case attMN: item.setMnemonic(curAtt.cValue); break;
        case attCMD: item.setActionCommand(curAtt.sValue); break;
        case attDO: item.setEnabled(!curAtt.bValue); break;
        case attFILE: if (!GuiUtil.isAPI() || curAtt.sValue.endsWith(")") == true) item.setFileName(curAtt.sValue) ;break;
        case attACC: item.setAccelerator(getKey(curAtt.sValue)); break;
        case attNAME: item.setName(curAtt.sValue); break;
        case attVAL: item.setSelected(curAtt.bValue); break; // Default value
        case attPOINT: item.setFont(item.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: item.setFont(item.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: item.setFont(new Font(curAtt.sValue, item.getFont().getStyle(), item.getFont().getSize())); break;
        //case attVISIBLE: item.setVisible(curAtt.bValue); break;
        case attRESTORE: item.setRestore(curAtt.bValue); break;
        case attENABLED_WHEN: item.setEnabledWhen(curAtt.cValue); break;
        case msgCHANGE: item.setMsgChange(curAtt.sValue); break;
        case attTT: item.setToolTipText(curAtt.sValue); break;
      }
    } // next i
    // OptionGroup
    GuiOptionGroup og = c.curMenu.getOptionGroup();
    c.curMenu.addOption(item); // Erzeugt og wenn noch nicht vorhanden

    if (og == null) {
        og = c.curMenu.getOptionGroup();
        // Zweite Durchlauf: Alles was mit OptionGroup zu tun hat.
        for (CurrentAttrib curAtt: curKey.vAttrib) {
            switch (curAtt.iKeyword.intValue()) {
            	case attELEMENT: og.setElementName(curAtt.sValue); break;
            }
        }
    }
  }

  private static void perfHidden(CurrentKeyword curKey, CurContext c) {
    final HiddenField hid = new HiddenField(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attNAME: hid.setName(curAtt.sValue); break;
        case attVAL: hid.setValue(curAtt.sValue); break; // default
        case attREF: hid.setRef(curAtt.sValue); break;
        // Bindings
        case attELEMENT: hid.setElementName(curAtt.sValue); break;
      }
    } // next i
    if (c.isTable == false) {
      if (c.curContainer != null) {
        c.curContainer.addMember(hid);
        //##hid.setParent((Container)curContainer);
      }
    } else {
      c.cTbl.addColumn(hid, curKey.title, 0);
      curKey.eol = false; // Wenn Tabellenspalte, dann Layout nicht weiterzählen
    }
  }

  private static void perfLabel(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=0, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.NORTHWEST;
    int fill=GridBagConstraints.NONE;
    final GuiLabel lbl = new GuiLabel(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attAL: lbl.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
        case attIMG: lbl.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
        case attMN: lbl.setMnemonic(curAtt.cValue); break;
        case attSB: lbl.getJComponent().setOpaque(true);
            lbl.setBackground(getColor(curAtt.sValue)); break; // Geht nicht??
        case attSF: lbl.setForeground(getColor(curAtt.sValue)); break;
        case attPOINT: lbl.setFont(lbl.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: lbl.setFont(lbl.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: lbl.setFont(new Font(curAtt.sValue, lbl.getFont().getStyle(), lbl.getFont().getSize())); break;
        case attMIN:
            int min=curAtt.iValue;
                lbl.setMinimumSize(new Dimension(min, lbl.getMinimumSize().height));
          break;
        case attMAX:
          int max=curAtt.iValue;
          lbl.setMaximumSize(new Dimension(max, lbl.getMaximumSize().height));
          break;
        case attTYPE:
            if (curAtt.sValue.toLowerCase().equals("statusbar")) {
            	c.form.getRootPane().setStatusBar(lbl);
            }
        break;
        case attNAME: lbl.setName(curAtt.sValue); break;
        case attSIZE: lbl.setPreferredSize(getSize(curAtt.sValue)); break;
        case attVISIBLE: lbl.setVisible(curAtt.bValue); break;
        case attFILE: {
        	lbl.setText(GuiUtil.fileToString(curAtt.sValue));
        }
        break;
        case attHELPID: GuiSession.getInstance().setHelpId(lbl, curAtt.sValue); break;
        case attHTP: lbl.setHorizontalTextPosition(getAlign(curAtt.sValue)); break;

        // Messages
        case msgMOUSEOVER: lbl.setMsgMouseOver(curAtt.sValue); break;
        case msgCLICK: lbl.setMsgClick(curAtt.sValue);  break;
        // DragDrop
        case attDRAG: lbl.setDrag(curAtt.bValue); break;
			case msgDROP: lbl.setMsgDrop(curAtt.sValue); break;
			case msgDRAG_ENTER: lbl.setMsgDragEnter(curAtt.sValue); break;
			case msgDRAG_OVER: lbl.setMsgDragOver(curAtt.sValue); break;
			case msgDRAG_EXIT: lbl.setMsgDragExit(curAtt.sValue); break;
         // Bindings
         case attELEMENT: lbl.setElementName(curAtt.sValue); break;
      }
    } // next i
    if (c.isTable == false) {
      // grid0
      if (c.grid0 != 0 && c.x == 0) {
        lbl.setPreferredSize(new Dimension(c.grid0, 17));
      }
      c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, w, h, wx, wy
        ,anchor, fill, new Insets(it,il,ib,ir), px, py));
      c.yoff=h;
      c.incX(w);
      c.curComponent = lbl; // Current Component for Popup
    } else {
      c.cTbl.addColumn(lbl, curKey.title, w);
      curKey.eol = false; // Wenn Tabellenspalte, dann Layout nicht weiterzählen
    }
  }
  private static void perfTitle(CurrentKeyword curKey, CurContext c) {
      int w=4, h=1;
      float wx=1, wy=0;
      int it=4, ib=4, ir=5, il=5;
      int px=0, py=0;
      int anchor=GridBagConstraints.NORTHWEST;
      int fill=GridBagConstraints.HORIZONTAL;
      final GuiLabel lbl = new GuiLabel(curKey.title);
      lbl.setHorizontalAlignment(SwingConstants.LEFT);
      lbl.getJComponent().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
      lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
      lbl.getJComponent().setOpaque(true); // sonst klappt background nicht!
      lbl.setBackground(c.pPar.getBackground().brighter());
      for (CurrentAttrib curAtt: curKey.vAttrib) {
        switch (curAtt.iKeyword.intValue()) {
          case attX: c.x=curAtt.iValue; break;
          case attY: c.y=curAtt.iValue; break;
          case attW: w=curAtt.iValue; break;
          case attH: h=curAtt.iValue; break;
          case attWX: wx=curAtt.fValue; break;
          case attWY: wy=curAtt.fValue; break;
          case attIT: it=curAtt.iValue; break;
          case attIL: il=curAtt.iValue; break;
          case attIB: ib=curAtt.iValue; break;
          case attIR: ir=curAtt.iValue; break;
          case attPX: px=curAtt.iValue; break;
          case attPY: py=curAtt.iValue; break;
          case attAN: anchor=getAnchor(curAtt.sValue); break;
          case attFILL: fill=getFill(curAtt.sValue); break;
          case attAL: lbl.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
          case attIMG: lbl.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
          case attMN: lbl.setMnemonic(curAtt.cValue); break;
          case attSB: 
              lbl.setBackground(getColor(curAtt.sValue));              
              break; 
          case attSF: lbl.setForeground(getColor(curAtt.sValue)); break;
          case attPOINT: lbl.setFont(lbl.getFont().deriveFont(curAtt.fValue)); break;
          case attSTYLE: lbl.setFont(lbl.getFont().deriveFont(getStyle(curAtt.sValue))); break;
          case attFONT: lbl.setFont(new Font(curAtt.sValue, lbl.getFont().getStyle(), lbl.getFont().getSize())); break;
          case attMIN:
              int min=curAtt.iValue;
                  lbl.setMinimumSize(new Dimension(min, lbl.getMinimumSize().height));
            break;
          case attMAX:
            int max=curAtt.iValue;
            lbl.setMaximumSize(new Dimension(max, lbl.getMaximumSize().height));
            break;
          case attTYPE:
              if (curAtt.sValue.toLowerCase().equals("statusbar")) {
              	c.form.getRootPane().setStatusBar(lbl);
              }
          break;
          case attNAME: lbl.setName(curAtt.sValue); break;
          case attSIZE: lbl.setPreferredSize(getSize(curAtt.sValue)); break;
          case attVISIBLE: lbl.setVisible(curAtt.bValue); break;
          case attFILE: {
          	lbl.setText(GuiUtil.fileToString(curAtt.sValue));
          }
          break;
          case attHELPID: GuiSession.getInstance().setHelpId(lbl, curAtt.sValue); break;
          // Messages
          case msgMOUSEOVER: lbl.setMsgMouseOver(curAtt.sValue); break;
          case msgCLICK: lbl.setMsgClick(curAtt.sValue);  break;
          // DragDrop
          case attDRAG: lbl.setDrag(curAtt.bValue); break;
  				case msgDROP: lbl.setMsgDrop(curAtt.sValue); break;
  				case msgDRAG_ENTER: lbl.setMsgDragEnter(curAtt.sValue); break;
  				case msgDRAG_OVER: lbl.setMsgDragOver(curAtt.sValue); break;
  				case msgDRAG_EXIT: lbl.setMsgDragExit(curAtt.sValue); break;
            // Bindings
            case attELEMENT: lbl.setElementName(curAtt.sValue); break;
        }
      } // next i
	    c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, w, h, wx, wy
	      ,anchor, fill, new Insets(it,il,ib,ir), px, py));
	    c.yoff=h;
	    c.incX(w);

	    c.curComponent = lbl; // Current Component for Popup
    }

  private static void perfBorder(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=0, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.NORTHWEST;
    int fill=GridBagConstraints.NONE;
    final GuiBorder lbl = new GuiBorder(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attIMG: lbl.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
        case attSB: lbl.getJComponent().setOpaque(true);
            lbl.setBackground(getColor(curAtt.sValue)); break; // Geht nicht??
        case attSF: lbl.setForeground(getColor(curAtt.sValue)); break;
        case attPOINT: lbl.setFont(lbl.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: lbl.setFont(lbl.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: lbl.setFont(new Font(curAtt.sValue, lbl.getFont().getStyle(), lbl.getFont().getSize())); break;
        case attTYPE: lbl.setBorder(curAtt.sValue); break;
        case attNAME: lbl.setName(curAtt.sValue); break;
        case attSIZE: lbl.setPreferredSize(getSize(curAtt.sValue)); break;
        case msgMOUSEOVER: lbl.setMsgMouseOver(curAtt.sValue); break;
      }
    } // next i
    c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, w, h, wx, wy
      ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    c.yoff=h;
    c.incX(w);
    c.curComponent = lbl; // Current Component for Popup
  }

  private void perfMemo(CurrentKeyword curKey, CurContext c) {
		LayoutConstraints lc = new LayoutConstraints();
    lc.w=3; lc.h=1;
    lc.wx=3; lc.wy=1;
    lc.it=5; lc.ib=0; lc.ir=5; lc.il=0;
		lc.px=0; lc.py=0;
		lc.anchor=GridBagConstraints.NORTHWEST;
		lc.fill=GridBagConstraints.BOTH;
    final GuiMemo txtA = new GuiMemo();
    this.perfMemoAtt(curKey, c, lc, txtA);
  }
	private void perfMemoAtt(CurrentKeyword curKey, CurContext c, LayoutConstraints lc, final GuiMemo txtA) {
		Color lblSB = null;
		Color lblSF = null;
    if (curKey.title.length() > 0) {
      txtA.setLabel(curKey.title);
      txtA.setName(GuiUtil.labelToName(curKey.title));
    }
    c.curComponent = txtA; // Current Component for Popup

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: lc.w=curAtt.iValue; break;
        case attH: lc.h=curAtt.iValue; break;
        case attWX: lc.wx=curAtt.fValue; break;
        case attWY: lc.wy=curAtt.fValue; break;
        case attIT: lc.it=curAtt.iValue; break;
        case attIL: lc.il=curAtt.iValue; break;
        case attIB: lc.ib=curAtt.iValue; break;
        case attIR: lc.ir=curAtt.iValue; break;
        case attPX: lc.px=curAtt.iValue; break;
        case attPY: lc.py=curAtt.iValue; break;
        case attAN: lc.anchor=getAnchor(curAtt.sValue); break;
        case attFILL: lc.fill=getFill(curAtt.sValue); break;
        case attTT: txtA.setToolTipText(curAtt.sValue); break;
        case attST: txtA.setHint(curAtt.sValue); break;
        case attVAL:
          if (!GuiUtil.isAPI()) {
            txtA.setText(curAtt.sValue.replace('|', '\n'));
          }
          break;
        case attFILE: {
            if (!GuiUtil.isAPI()) {
              txtA.setText(GuiUtil.fileToString(curAtt.sValue));
            }
        	}
        	break;
        case attCOLS: txtA.setColumns(curAtt.iValue); lc.wx=0; break;
        case attDO: txtA.setEnabled(!curAtt.bValue); break;
        case attNN: txtA.setNotnull(true); break;
        case attSB: txtA.setBackground(getColor(curAtt.sValue)); break;
        case attSF: txtA.setForeground(getColor(curAtt.sValue)); break;
        case attLSB: lblSB = getColor(curAtt.sValue); break;
        case attLSF: lblSF = getColor(curAtt.sValue); break;
        case attPOINT: txtA.setFont(txtA.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: txtA.setFont(txtA.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: txtA.setFont(new Font(curAtt.sValue, txtA.getFont().getStyle(), txtA.getFont().getSize())); break;
        case attMIN:
            int min=curAtt.iValue;
                txtA.setMinimumSize(new Dimension(min, txtA.getMinimumSize().height));
          break;
        case attMAX:
          int max=curAtt.iValue;
          txtA.setMaximumSize(new Dimension(max, txtA.getMaximumSize().height));
          break;
        case attNAME: txtA.setName(curAtt.sValue); break;
        case attREF: txtA.setRef(curAtt.sValue); break;
        case attMINLEN: txtA.setMinlen(curAtt.iValue); break;
        case attMAXLEN: txtA.setMaxlen(curAtt.iValue); break;
        case attSIZE: txtA.setPreferredSize(getSize(curAtt.sValue)); break;
        case attMINSIZE: txtA.setMinimumSize(getSize(curAtt.sValue)); break;
        case attMAXSIZE: txtA.setMaximumSize(getSize(curAtt.sValue)); break;
        case attTABSTOP: txtA.setTabstop(curAtt.bValue); break;
        case attVISIBLE: txtA.setVisible(curAtt.bValue); break;
        case attSEARCH: txtA.setSearch(curAtt.bValue); break;
        case attLINKCOL: txtA.setLinkColumn(c, curAtt.sValue); break;
				case attDRAG: txtA.setDrag(curAtt.bValue); break;
				case attHELPID: GuiSession.getInstance().setHelpId(txtA, curAtt.sValue); break;
        // Messages
        case msgLOSTFOCUS: txtA.setMsgLostFocus(curAtt.sValue); break;
        case msgDBLCLICK: txtA.setMsgDblClick(curAtt.sValue); break;
        case msgCHANGE: txtA.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: txtA.setMsgMouseOver(curAtt.sValue); break;
        case msgPOPUP: txtA.setMsgPopup(curAtt.sValue); break;
        // DragDrop
				case msgDROP: txtA.setMsgDrop(curAtt.sValue); break;
				case msgDRAG_ENTER: txtA.setMsgDragEnter(curAtt.sValue); break;
				case msgDRAG_OVER: txtA.setMsgDragOver(curAtt.sValue); break;
				case msgDRAG_EXIT: txtA.setMsgDragExit(curAtt.sValue); break;
          // Bindings
          case attELEMENT: txtA.setElementName(curAtt.sValue); break;
      }
    } // next i
    if (curKey.title.length() >0) {
    	// final GuiLabel lbl = new GuiLabel(txtA, curKey.title);
      final GuiLabel lbl = new GuiLabel(txtA, txtA.getLabel());
      lbl.setVisible(txtA.isVisible());
      if (lblSB != null) {
        lbl.setBackground(lblSB);
      }
      if (lblSF != null) {
        lbl.setForeground(lblSF);
      }
      // grid0
      if (c.grid0 != 0 && c.x == 0) {
        lbl.setPreferredSize(new Dimension(c.grid0, 17));
      }
      c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, 1, 1, 0, 0
            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(lc.it,lc.il,lc.ib,5), LPX, 0));
      c.x++;
      lc.il=0;
    }
    GuiScrollBox scrollPane = new GuiScrollBox(txtA);
    c.pPar.add(scrollPane, new GridBagConstraints(c.x, c.y, lc.w, lc.h, lc.wx, lc.wy
           ,lc.anchor, lc.fill, new Insets(lc.it,lc.il,lc.ib,lc.ir), lc.px, lc.py));
    if (c.curContainer != null) {
      c.curContainer.addMember(txtA);
    }
    c.yoff=lc.h;
    c.incX(lc.w);
  }

  private static void perfEditor(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=1, wy=1;
    int it=0, ib=0, ir=0, il=0;
    int px=0, py=0;
    int anchor = GridBagConstraints.CENTER;
    int fill = GridBagConstraints.BOTH;
    final GuiEditor txtA = new GuiEditor();
    c.curComponent = txtA; // Current Component for Popup

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attLINKCOL: txtA.setLinkColumn(c, curAtt.sValue); break;
        //case attTT: txtA.setToolTipText(curAtt.sValue); break;
        //case attST: txtA.setHint(curAtt.sValue); break;
        case attVAL:
          if (!GuiUtil.isAPI()) {
            txtA.setText(curAtt.sValue.replace('|', '\n'));
          }
          break;
        case attFILE:
          if (!GuiUtil.isAPI()) {
            try {
              if (curAtt.sValue.startsWith("http://")) {
                txtA.setPage(new URL(curAtt.sValue));
              }
              else {
                txtA.setPage(new URL(GuiUtil.getDocumentBase(), curAtt.sValue));
              }
            }
            catch (Exception exe) {
              GuiUtil.showEx(exe);
            }
          }
          break;
        //case attCOLS: txtA.setColumns(curAtt.iValue); wx=0; break;
        case attDO: txtA.setEnabled(!curAtt.bValue);
            txtA.setBackground(Color.lightGray);
            break;
        //case attNN: txtA.setNotNull(true); break;
        case attSB: txtA.setBackground(getColor(curAtt.sValue)); break;
        case attSF: txtA.setForeground(getColor(curAtt.sValue)); break;
        case attPOINT: txtA.setFont(txtA.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: txtA.setFont(txtA.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attNAME: txtA.setName(curAtt.sValue); break;
        case attREF: txtA.setRef(curAtt.sValue); break;
        case attTYPE: txtA.setContentType(curAtt.sValue); break;
        case attSIZE: txtA.setPreferredSize(getSize(curAtt.sValue)); break;
        case attMINSIZE: txtA.setMinimumSize(getSize(curAtt.sValue)); break;
        case attMAXSIZE: txtA.setMaximumSize(getSize(curAtt.sValue)); break;
			case msgDROP: txtA.setMsgDrop(curAtt.sValue); break;
			case msgDRAG_ENTER: txtA.setMsgDragEnter(curAtt.sValue); break;
			case msgDRAG_OVER: txtA.setMsgDragOver(curAtt.sValue); break;
			case msgDRAG_EXIT: txtA.setMsgDragExit(curAtt.sValue); break;
	      case msgPOPUP: txtA.setMsgPopup(curAtt.sValue); break;
          // Bindings
          case attELEMENT: txtA.setElementName(curAtt.sValue); break;
      }
    } // next i
    //final JScrollPane scrollPane = new JScrollPane(txtA.getJComponent());
    GuiScrollBox scrollPane = new GuiScrollBox(txtA);
    c.pPar.add(scrollPane, new GridBagConstraints(c.x, c.y, w, h, wx, wy
           ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    if (c.curContainer != null) {
      c.curContainer.addMember(txtA);
    }
    c.yoff=h;
    c.incX(w);
  }

  private static void perfBeginOptionGroup(CurrentKeyword curKey, CurContext c) {
    final GuiOptionGroup opt = new GuiOptionGroup(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attDO: opt.setEnabled(!curAtt.bValue); break;
        case attNAME: opt.setName(curAtt.sValue); break;
        case attVAL: opt.setValue(curAtt.sValue); break; // geht das, wenn es die Buttons noch nicht gibt?
        case attREF: opt.setRef(curAtt.sValue); break;
        case attLINKCOL: opt.setLinkColumn(c, curAtt.sValue); break;
        case msgCHANGE: opt.setMsgChange(curAtt.sValue); break;
        case attRESTORE: opt.setRestore(curAtt.bValue); break;
        // Bindings
        case attELEMENT: opt.setElementName(curAtt.sValue); break;
      }
    } // next i
    c.curOptionGroup = opt;
    // Member <-> Parent
    if (c.curContainer != null) {
      c.curContainer.addMember(opt);
    }
  }

  private static void perfEndOptionGroup(CurrentKeyword curKey, CurContext c) {
    c.curOptionGroup = null;
  }

  private static void perfOption(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=0, wy=0;
    int it=-5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.NORTHWEST;
    int fill=GridBagConstraints.NONE;
    final GuiOption opt = new GuiOption(curKey.title);
    // Das ist hier ein absurdes Henne-Ei-Problem für den ersten Radiobutton eines Panels
    // Erste muß der Name des Buttons ermittelt werden;
    // dann dem Panel hinzufügen (und dabei eine OptionGroup erstellen);
    // und erst danach können die Attribute val=, linkcol= und element= verarbeiter werden,
    // weil diese eine OprionGroup voraussetzen.
    // Erste Durchlauf: Alles was nicht die OptionGroup betrifft.
    // Wenn bis jetzt keine OptionGroup definiert wurde,
    // holen wir das jetzt nach.
    // Wegen Kompatibilität mit alten Scripten.
    GuiOptionGroup og = c.curOptionGroup; // default

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attTT: opt.setToolTipText(curAtt.sValue); break;
        case attST: opt.setHint(curAtt.sValue); break;
        // Gif müßte eigentlich einen Toggle Button setzen!
        case attIMG: opt.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
        case attMN: opt.setMnemonic(curAtt.cValue); break;
        case attDO: opt.setEnabled(!curAtt.bValue); break;
        case attAL: opt.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
        case attCMD: opt.setActionCommand(curAtt.sValue); break;
        case attSB: opt.setBackground(getColor(curAtt.sValue)); break;
        case attSF: opt.setForeground(getColor(curAtt.sValue)); break;
        case attPOINT: opt.setFont(opt.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: opt.setFont(opt.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: opt.setFont(new Font(curAtt.sValue, opt.getFont().getStyle(), opt.getFont().getSize())); break;
        case attNAME: opt.setName(curAtt.sValue); break;
        case attOG_NAME: og = c.getNOG(curAtt.sValue); break;
        case attREF: opt.setRef(curAtt.sValue); break;
        case attSIZE: opt.setPreferredSize(getSize(curAtt.sValue)); break;
        case attTABSTOP: opt.setTabstop(curAtt.bValue); break;
        case attVISIBLE: opt.setVisible(curAtt.bValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(opt, curAtt.sValue); break;
        case attRESTORE: opt.setRestore(curAtt.bValue); break;
        // Messages
        case msgCHANGE: opt.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: opt.setMsgMouseOver(curAtt.sValue); break;
      }
    } // next i
    if (og == null) {
      og = new GuiOptionGroup(opt.getName());
      c.curOptionGroup = og;
      og.setMsgChange(opt.getMsgChange());
      // Member <-> Parent
      if (c.curContainer != null) {
        c.curContainer.addMember(og);
      }
    }
    og.add(opt);
    // Zweite Durchlauf: Alles was mit OptionGroup zu tun hat.
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        // Geht nur, wenns bereits eine OptionGroup gibt!
        case attVAL: opt.setSelected(curAtt.bValue); break;
        // Geht nur, wenns bereits eine OptionGroup gibt!
        case attLINKCOL: og.setLinkColumn(c, curAtt.sValue); break;
        case attELEMENT: og.setElementName(curAtt.sValue); break;
      }
    }
    // Display-Parent
    c.pPar.add(opt, new GridBagConstraints(c.x, c.y, w, h, wx, wy
      ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    c.yoff=h;
    c.incX(w);
  }

  private static void perfScrollBar(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=0, wy=1;
    int it=0, ib=0, ir=0, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.NORTHEAST;
    int fill=GridBagConstraints.VERTICAL;
    final GuiScrollBar scr = new GuiScrollBar(curKey.title);
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attTT: scr.setToolTipText(curAtt.sValue); break;
        //case attST: scr.setHint(curAtt.sValue); break;
        //case attDO: sld.setEnabled(!curAtt.bValue); break;
        case attTYPE:
          if (curAtt.sValue.equals("VERTICAL")) {
            scr.getBar().setOrientation(SwingConstants.VERTICAL);
          }
          else if (curAtt.sValue.equals("HORIZONTAL")) {
            scr.getBar().setOrientation(SwingConstants.HORIZONTAL);
            scr.setPreferredSize(new Dimension(48,22));
            fill = GridBagConstraints.HORIZONTAL;
            w = 3; wx = 1; wy = 0;
          }
          break;
        case attMINVAL: scr.setMinimum(curAtt.iValue); break;
        case attMAXVAL: scr.setMaximum(curAtt.iValue); break;
        case attSIZE: scr.setPreferredSize(getSize(curAtt.sValue)); break;
        case attNAME: scr.setName(curAtt.sValue); break;
        case attREF: scr.setRef(curAtt.sValue); break;
        case msgCHANGE: scr.setMsgChange(curAtt.sValue); break;
        // todo element
      }
    } // next i
    c.pPar.add(scr, new GridBagConstraints(c.x, c.y, w, h, wx, wy
           ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    if (c.curContainer != null) {
      c.curContainer.addMember(scr);
    }
    c.yoff=h;
    c.incX(w);
  }

  private static void perfSlider(CurrentKeyword curKey, CurContext c) {
    int w=3, h=1;
    float wx=3, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor = GridBagConstraints.NORTHWEST;
    int fill = GridBagConstraints.HORIZONTAL;
    Color lblSB = null;
    Color lblSF = null;

    final GuiSlider sld = new GuiSlider(curKey.title);

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attDO: sld.setEnabled(!curAtt.bValue); break;
        case attTT: sld.setToolTipText(curAtt.sValue); break;
        case attST: sld.setHint(curAtt.sValue); break;
        case attLSB: lblSB = getColor(curAtt.sValue); break;
        case attLSF: lblSF = getColor(curAtt.sValue); break;
        case attTYPE:
          if (curAtt.sValue.equals("VERTICAL")) {
            sld.getSlider().setOrientation(SwingConstants.VERTICAL);
            sld.getSlider().setInverted(true);
            fill = GridBagConstraints.VERTICAL;
            w = 1; wx=0; wy = 1;
          }
          else if (curAtt.sValue.equals("HORIZONTAL")) {
            sld.getSlider().setOrientation(SwingConstants.HORIZONTAL);
          }
          break;
        case attMINVAL: sld.setMinimum(curAtt.iValue); break;
        case attMAXVAL: sld.setMaximum(curAtt.iValue);
                if (curAtt.iValue%10 == 0) {
                  sld.getSlider().setMajorTickSpacing(curAtt.iValue/5);
                  sld.getSlider().setMinorTickSpacing(curAtt.iValue/10);
                  sld.getSlider().createStandardLabels(curAtt.iValue/10);
                }
                sld.getSlider().setPaintLabels(true); break;
        case attNAME: sld.setName(curAtt.sValue); break;
        case attREF: sld.setRef(curAtt.sValue); break;
        case attTABSTOP: sld.setTabstop(curAtt.bValue); break;
        case attVISIBLE: sld.setVisible(curAtt.bValue); break;
        case attLINKCOL: sld.setLinkColumn(c, curAtt.sValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(sld, curAtt.sValue); break;
        case attRESTORE: sld.setRestore(curAtt.bValue); break;
        // Messages
        case msgCHANGE: sld.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: sld.setMsgMouseOver(curAtt.sValue); break;
        // Bindings
        case attELEMENT: sld.setElementName(curAtt.sValue); break;
      }
    } // next i

    if (curKey.title.length() >0) {
      final GuiLabel lbl = new GuiLabel(sld, curKey.title);
      lbl.setVisible(sld.isVisible());
      if (lblSB != null) {
        lbl.setBackground(lblSB);
      }
      if (lblSF != null) {
        lbl.setForeground(lblSF);
      }
      // grid0
      if (c.grid0 != 0 && c.x == 0) {
        lbl.setPreferredSize(new Dimension(c.grid0, 17));
      }
      c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, 1, 1, 0, 0,
                       makeLabelAnchor(anchor), GridBagConstraints.NONE, new Insets(it,il,ib,5), LPX, 0));
      c.x++;
      il = 0;
    }
    c.pPar.add(sld, new GridBagConstraints(c.x, c.y, w, h, wx, wy
           ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    if (c.curContainer != null) {
      c.curContainer.addMember(sld);
    }
    c.yoff=h;
    c.incX(w);
  }

  private void perfText(CurrentKeyword curKey, CurContext c) {
     LayoutConstraints lc = new LayoutConstraints();
     lc.w=3; lc.h=1;
     if (c.isTable == true) lc.w=0;

     lc.wx=3; lc.wy=0;
     lc.it=5; lc.ib=0; lc.ir=5; lc.il=0;
     lc.anchor = GridBagConstraints.NORTHWEST;
     lc.fill = GridBagConstraints.HORIZONTAL;
     // Create Component
     GuiText txt = new GuiText(curKey.title);
     this.perfTextAtt(curKey, c, lc, txt);
  }
  private void perfTextAtt(CurrentKeyword curKey, CurContext c, LayoutConstraints lc, GuiText txt) {
    Color lblSB = null;
    Color lblSF = null;

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: lc.w=curAtt.iValue; break;
        case attH: lc.h=curAtt.iValue; break;
        case attWX: lc.wx=curAtt.fValue; break;
        case attWY: lc.wy=curAtt.fValue; break;
        case attIT: lc.it=curAtt.iValue; break;
        case attIL: lc.il=curAtt.iValue; break;
        case attIB: lc.ib=curAtt.iValue; break;
        case attIR: lc.ir=curAtt.iValue; break;
        case attPX: lc.px=curAtt.iValue; break;
        case attPY: lc.py=curAtt.iValue; break;
        case attAN: lc.anchor=getAnchor(curAtt.sValue); break;
        case attFILL: lc.fill=getFill(curAtt.sValue); break;
        case attTT: txt.setToolTipText(curAtt.sValue); break;
        case attST: txt.setHint(curAtt.sValue); break;
        case attVAL: if (!GuiUtil.isAPI()) {txt.setValue(curAtt.sValue);} break;
        case attCOLS: txt.setColumns(curAtt.iValue); lc.wx=0; lc.w=1; break;
        case attDO: txt.setEnabled(!curAtt.bValue); break;
        case attNN: txt.setNotnull(curAtt.bValue); break;
        case attAL: txt.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
        case attSB: txt.setBackground(getColor(curAtt.sValue)); break;
        case attSF: txt.setForeground(getColor(curAtt.sValue)); break;
        case attLSB: lblSB = getColor(curAtt.sValue); break;
        case attLSF: lblSF = getColor(curAtt.sValue); break;
        case attPOINT: txt.setFont(txt.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: txt.setFont(txt.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: txt.setFont(new Font(curAtt.sValue, txt.getFont().getStyle(), txt.getFont().getSize())); break;
        case attMIN:
               txt.setMinimumSize(new Dimension(curAtt.iValue, txt.getMinimumSize().height));
         break;
        case attMAX:
               txt.setMaximumSize(new Dimension(curAtt.iValue, txt.getMaximumSize().height));
         break;
        case attNAME: txt.setName(curAtt.sValue); break;
        case attREF: txt.setRef(curAtt.sValue); break;
        case attSIZE: txt.setPreferredSize(getSize(curAtt.sValue)); break;
        case attMINSIZE: txt.setMinimumSize(getSize(curAtt.sValue)); break;
        case attMAXSIZE: txt.setMaximumSize(getSize(curAtt.sValue)); break;
        case attTABSTOP: txt.setTabstop(curAtt.bValue); break;
        case attVISIBLE: txt.setVisible(curAtt.bValue); break;
        case attLINKCOL: txt.setLinkColumn(c, curAtt.sValue); break;
        case attMINLEN: txt.setMinlen(curAtt.iValue); break;
        case attMAXLEN: txt.setMaxlen(curAtt.iValue); break;
        case attSEARCH: txt.setSearch(curAtt.bValue); break;
        case attREGEXP: txt.setRegexp(curAtt.sValue); break;
        case attNODE_TITLE: txt.setNodeTitle(curAtt.iValue); break;
        case attDRAG: txt.setDrag(curAtt.bValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(txt, curAtt.sValue); break;
        case attRESTORE: txt.setRestore(curAtt.bValue); break;
        // Messages
        case msgLOSTFOCUS: txt.setMsgLostFocus(curAtt.sValue); break;
        case msgDBLCLICK: txt.setMsgDblClick(curAtt.sValue); break;
        case msgCHANGE: txt.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: txt.setMsgMouseOver(curAtt.sValue); break;
        case msgKEYTYPED: txt.setMsgKeyTyped(curAtt.sValue); break;
        case msgDROP: txt.setMsgDrop(curAtt.sValue); break;
        case msgDRAG_ENTER: txt.setMsgDragEnter(curAtt.sValue); break;
        case msgDRAG_OVER: txt.setMsgDragOver(curAtt.sValue); break;
        case msgDRAG_EXIT: txt.setMsgDragExit(curAtt.sValue); break;

        // Bindings
        case attELEMENT: txt.setElementName(curAtt.sValue); break;
      }
    } // next i
    if (curKey.title.length() > 0 && c.isTable == false
        && (c.pPar.getLayoutManager() == GuiContainer.GRIDBAG
				|| c.pPar.getLayoutManager() == GuiContainer.FORM || c.pPar.getLayoutManager() == GuiContainer.TABLE)	) { // Oh Weh!
    	// Ganz wichtig! Hier muss txt.getLabel() verwendet werden
    	// damit bei Pflichtfeldern ggf. das Zeichen zur Erweiterung 
    	// des Labels gesetzt werden kann
      final GuiLabel lbl = new GuiLabel(txt, txt.getLabel());
      lbl.setVisible(txt.isVisible());
      if (lblSB != null) {
        lbl.getJComponent().setOpaque(true);
        lbl.setBackground(lblSB);
      }
      if (lblSF != null) {
        lbl.setForeground(lblSF);
      }
      // grid0
      if (c.grid0 != 0 && c.x == 0) {
        lbl.setPreferredSize(new Dimension(c.grid0, 17));
      }
      c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, 1, 1, 0, 0,
      		makeLabelAnchor(lc.anchor), GridBagConstraints.NONE, new Insets(lc.it, lc.il, lc.ib, 5), LPX, 0));
      c.x++;
      lc.il = 0;
    }
    if (c.isTable == false) {
      c.pPar.add(txt, new GridBagConstraints(c.x, c.y, lc.w, lc.h, lc.wx, 0
                       ,lc.anchor, lc.fill, new Insets(lc.it, lc.il, lc.ib, lc.ir), lc.px, lc.py));
      if (c.curContainer != null) {
        c.curContainer.addMember(txt);
      }
      c.yoff=lc.h;
      c.incX(lc.w);
      c.curComponent = txt; // Current Component for Popup
    } else { // isTable == true
      c.cTbl.addColumn(txt, curKey.title, lc.w);
      curKey.eol = false; // Wenn Tabellenspalte, dann Layout nicht weiterzählen
    }
  }

  private void perfDate(CurrentKeyword curKey, CurContext c) {
  	LayoutConstraints lc = new LayoutConstraints();
    lc.w=1; lc.h=1;
    if (c.isTable==true) {lc.w=70;}
    lc.it=5; lc.ib=0; lc.ir=5; lc.il=0;
    lc.anchor = GridBagConstraints.NORTHWEST;
    lc.fill = GridBagConstraints.NONE;
    lc.wx = 0; lc.wy = 0;

    final GuiDate txt = new GuiDate(curKey.title);
    perfDateAtt(curKey, c, lc, txt);
    c.currentGuiDate = txt;
  }
  private void perfCalendar(CurrentKeyword curKey, CurContext c) {
		LayoutConstraints lc = new LayoutConstraints();
		lc.w=2; lc.h=1;
		if (c.isTable==true) {lc.w=70;}
		lc.it=0; lc.ib=0; lc.ir=0; lc.il=0;
		lc.anchor = GridBagConstraints.WEST;
		lc.fill = GridBagConstraints.NONE;
		lc.wx = 1; lc.wy = 0;

		final GuiCalendar cal = new GuiCalendar(curKey.title);
		perfDateAtt(curKey, c, lc, cal);
  }
  private void perfCalendarPopup(CurrentKeyword curKey, CurContext c) {
		LayoutConstraints lc = new LayoutConstraints();
		lc.w=1; lc.h=1;
		if (c.isTable == true) {lc.w=70;}
		lc.it=0; lc.ib=0; lc.ir=5; lc.il=0;
		lc.anchor = GridBagConstraints.SOUTHWEST;
		lc.fill = GridBagConstraints.NONE;
		lc.wx = 1; lc.wy = 0;
		lc.px = -25; lc.py = 6;
//		JFrame frame = null;
//		try {
//			frame = ((GuiForm)c.form).getForm();
//		} catch (Exception ex) {
//			// nix wenn Dialog
//		}
		final GuiCalendarPopup txt = new GuiCalendarPopup(curKey.title, c.form);
		perfDateAtt(curKey, c, lc, txt);
		if (c.currentGuiDate != null) {
			txt.setGuiDate(c.currentGuiDate);
			c.currentGuiDate = null;
		}
	}
  private void perfDateAtt(CurrentKeyword curKey, CurContext c, LayoutConstraints lc, GuiDate txt) {
  	Color lblSB = null;
  	Color lblSF = null;

    for (CurrentAttrib curAtt: curKey.vAttrib) {
     switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: lc.w=curAtt.iValue; break;
        case attH: lc.h=curAtt.iValue; break;
        case attWX: lc.wx=curAtt.fValue; break;
        case attWY: lc.wy=curAtt.fValue; break;
        case attIT: lc.it=curAtt.iValue; break;
        case attIL: lc.il=curAtt.iValue; break;
        case attIB: lc.ib=curAtt.iValue; break;
        case attIR: lc.ir=curAtt.iValue; break;
        case attPX: lc.px=curAtt.iValue; break;
        case attPY: lc.py=curAtt.iValue; break;
        case attAN: lc.anchor=getAnchor(curAtt.sValue); break;
        case attFILL: lc.fill=getFill(curAtt.sValue); break;
        case attTT: txt.setToolTipText(curAtt.sValue); break;
        case attST: txt.setHint(curAtt.sValue); break;
        case attVAL: if (!GuiUtil.isAPI()) {txt.setValue(curAtt.sValue);} break;
        case attCOLS: txt.setColumns(curAtt.iValue); lc.wx=0; lc.w=1; break;
        case attDO: txt.setEnabled(!curAtt.bValue); break;
        case attNN: txt.setNotnull(curAtt.bValue); break;
        case attAL: txt.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
        case attSB: txt.setBackground(getColor(curAtt.sValue)); break;
        case attSF: txt.setForeground(getColor(curAtt.sValue)); break;
        case attLSB: lblSB = getColor(curAtt.sValue); break;
        case attLSF: lblSF = getColor(curAtt.sValue); break;
        case attPOINT: txt.setFont(txt.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: txt.setFont(txt.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: txt.setFont(new Font(curAtt.sValue, txt.getFont().getStyle(), txt.getFont().getSize())); break;
        case attMIN:
               txt.setMinimumSize(new Dimension(curAtt.iValue, txt.getMinimumSize().height));
         break;
        case attMAX:
               txt.setMaximumSize(new Dimension(curAtt.iValue, txt.getMaximumSize().height));
         break;
        case attNAME: txt.setName(curAtt.sValue); break;
        case attREF: txt.setRef(curAtt.sValue); break;
        case attSIZE:
            txt.setPreferredSize(getSize(curAtt.sValue));
            //txt.setColumns(0); // ??
            break;
        case attTABSTOP: txt.setTabstop(curAtt.bValue); break;
        case attVISIBLE: txt.setVisible(curAtt.bValue); break;
        case attLINKCOL: txt.setLinkColumn(c, curAtt.sValue); break;
        case attMINLEN: txt.setMinlen(curAtt.iValue); break;
        case attMAXLEN: txt.setMaxlen(curAtt.iValue); break;
        case attFORMAT: txt.setFormat(curAtt.sValue); break;
        case attSEARCH: txt.setSearch(curAtt.bValue); break;
        case attREGEXP: txt.setRegexp(curAtt.sValue); break;
        case attNODE_TITLE: txt.setNodeTitle(curAtt.iValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(txt, curAtt.sValue); break;
        case attRESTORE: txt.setRestore(curAtt.bValue); break;
        // Messages
        case msgLOSTFOCUS: txt.setMsgLostFocus(curAtt.sValue); break;
        case msgDBLCLICK: txt.setMsgDblClick(curAtt.sValue); break;
        case msgCHANGE: txt.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: txt.setMsgMouseOver(curAtt.sValue); break;
				case msgDROP: txt.setMsgDrop(curAtt.sValue); break;
				case msgDRAG_ENTER: txt.setMsgDragEnter(curAtt.sValue); break;
				case msgDRAG_OVER: txt.setMsgDragOver(curAtt.sValue); break;
				case msgDRAG_EXIT: txt.setMsgDragExit(curAtt.sValue); break;
        // Bindings
        case attELEMENT: txt.setElementName(curAtt.sValue); break;
      }
    } // next i
    // Label zu Date/Calendar
    if (curKey.title.length() > 0 && c.isTable == false) {
      // final GuiLabel lbl = new GuiLabel(txt, curKey.title);
    	final GuiLabel lbl = new GuiLabel(txt, txt.getLabel());
      lbl.setVisible(txt.isVisible());
      if (lblSB != null) {
        lbl.getJComponent().setOpaque(true);
        lbl.setBackground(lblSB);
      }
      if (lblSF != null) {
        lbl.setForeground(lblSF);
      }
      // grid0
      if (c.grid0 != 0 && c.x == 0) {
        lbl.setPreferredSize(new Dimension(c.grid0, 17));
      }
      c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, 1, 1, 0, 0,
                    makeLabelAnchor(lc.anchor), GridBagConstraints.NONE, new Insets(lc.it,lc.il,lc.ib,5), LPX, 0));
      c.x++;
      lc.il=0;
    } // End Label
    if (c.isTable==false) {
      c.pPar.add(txt, new GridBagConstraints(c.x, c.y, lc.w, lc.h, lc.wx, lc.wy
                       ,lc.anchor, lc.fill, new Insets(lc.it,lc.il,lc.ib,lc.ir), lc.px, lc.py));
      if (c.curContainer != null) {
        c.curContainer.addMember(txt);
      }
      c.yoff=lc.h;
      c.incX(lc.w);
      c.curComponent = txt; // Current Component for Popup
    }
    else {
      c.cTbl.addColumn(txt, curKey.title, lc.w);
      curKey.eol = false; // Wenn Tabellenspalte, dann Layout nicht weiterzählen
    }
  }

  private static void perfTime(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    if (c.isTable==true) {w=50;}
    float wx=0, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.NORTHWEST;
    int fill=GridBagConstraints.NONE;
    Color lblSB = null;
    Color lblSF = null;

    final GuiTime txt = new GuiTime(curKey.title);

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attTT: txt.setToolTipText(curAtt.sValue); break;
        case attST: txt.setHint(curAtt.sValue); break;
        case attVAL: if (!GuiUtil.isAPI()) {txt.setValue(curAtt.sValue);} break;
        case attCOLS: txt.setColumns(curAtt.iValue); wx=0; w=1; break;
        case attDO: txt.setEnabled(!curAtt.bValue); break;
        case attNN: txt.setNotnull(curAtt.bValue); break;
        case attAL: txt.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
        case attSB: txt.setBackground(getColor(curAtt.sValue)); break;
        case attSF: txt.setForeground(getColor(curAtt.sValue)); break;
        case attLSB: lblSB = getColor(curAtt.sValue); break;
        case attLSF: lblSF = getColor(curAtt.sValue); break;
        case attPOINT: txt.setFont(txt.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: txt.setFont(txt.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: txt.setFont(new Font(curAtt.sValue, txt.getFont().getStyle(), txt.getFont().getSize())); break;
        case attMIN:
               txt.setMinimumSize(new Dimension(curAtt.iValue, txt.getMinimumSize().height));
         break;
        case attMAX:
               txt.setMaximumSize(new Dimension(curAtt.iValue, txt.getMaximumSize().height));
         break;
        case attNAME: txt.setName(curAtt.sValue); break;
        case attREF: txt.setRef(curAtt.sValue); break;
        case attSIZE:
            txt.setPreferredSize(getSize(curAtt.sValue));
            txt.setColumns(0);
            break;
        case attTABSTOP: txt.setTabstop(curAtt.bValue); break;
        case attVISIBLE: txt.setVisible(curAtt.bValue); break;
        case attLINKCOL: txt.setLinkColumn(c, curAtt.sValue); break;
        case attMINLEN: txt.setMinlen(curAtt.iValue); break;
        case attMAXLEN: txt.setMaxlen(curAtt.iValue); break;
        case attFORMAT: txt.setFormat(curAtt.sValue); break;
        case attSEARCH: txt.setSearch(curAtt.bValue); break;
        case attREGEXP: txt.setRegexp(curAtt.sValue); break;
        case attNODE_TITLE: txt.setNodeTitle(curAtt.iValue); break;
        case attDRAG: txt.setDrag(curAtt.bValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(txt, curAtt.sValue); break;
        case attRESTORE: txt.setRestore(curAtt.bValue); break;
        // Messages
        case msgLOSTFOCUS: txt.setMsgLostFocus(curAtt.sValue); break;
        case msgDBLCLICK: txt.setMsgDblClick(curAtt.sValue); break;
        case msgCHANGE: txt.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: txt.setMsgMouseOver(curAtt.sValue); break;
        case msgDROP: txt.setMsgDrop(curAtt.sValue); break;
        case msgDRAG_ENTER: txt.setMsgDragEnter(curAtt.sValue); break;
        case msgDRAG_OVER: txt.setMsgDragOver(curAtt.sValue); break;
        case msgDRAG_EXIT: txt.setMsgDragExit(curAtt.sValue); break;
          // Bindings
          case attELEMENT: txt.setElementName(curAtt.sValue); break;
      }
    } // next i
    if (curKey.title.length() > 0 && c.isTable == false) {
    	// final GuiLabel lbl = new GuiLabel(txt, curKey.title);
      final GuiLabel lbl = new GuiLabel(txt, txt.getLabel());
      lbl.setVisible(txt.isVisible());
      if (lblSB != null) {
        lbl.getJComponent().setOpaque(true);
        lbl.setBackground(lblSB);
      }
      if (lblSF != null) {
        lbl.setForeground(lblSF);
      }
      // grid0
      if (c.grid0 != 0 && c.x == 0) {
        lbl.setPreferredSize(new Dimension(c.grid0, 17));
      }
      c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, 1, 1, 0, 0,
          makeLabelAnchor(anchor), GridBagConstraints.NONE, new Insets(it,il,ib,5), LPX, 0));
      c.x++;
      il=0;
    }
    if (c.isTable == false) {
      c.pPar.add(txt, new GridBagConstraints(c.x, c.y, w, h, wx, wy
        ,anchor, fill, new Insets(it,il,ib,ir), px, py));
      if (c.curContainer != null) {
        c.curContainer.addMember(txt);
      }
      c.yoff=h;
      c.incX(w);
      c.curComponent = txt; // Current Component for Popup
    } else {
      c.cTbl.addColumn(txt, curKey.title, w);
      curKey.eol = false; // Wenn Tabellenspalte, dann Layout nicht weiterzählen
    }
  }

  private static void perfMoney(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    if (c.isTable==true) {w=120;}
    float wx=0, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.NORTHWEST;
    int fill=GridBagConstraints.NONE;
    Color lblSB = null;
    Color lblSF = null;

    final GuiMoney txt = new GuiMoney(curKey.title);

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attTT: txt.setToolTipText(curAtt.sValue); break;
        case attST: txt.setHint(curAtt.sValue); break;
        case attVAL: if (!GuiUtil.isAPI()) {txt.setValue(txt.getFormat().format(curAtt.dValue));} break;
        case attCOLS: txt.setColumns(curAtt.iValue); wx=0; w=1; break;
        case attDO: txt.setEnabled(!curAtt.bValue); break;
        case attNN: txt.setNotnull(curAtt.bValue); break;
        case attAL: txt.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
        case attSB: txt.setBackground(getColor(curAtt.sValue)); break;
        case attSF: txt.setForeground(getColor(curAtt.sValue)); break;
        case attLSB: lblSB = getColor(curAtt.sValue); break;
        case attLSF: lblSF = getColor(curAtt.sValue); break;
        case attPOINT: txt.setFont(txt.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: txt.setFont(txt.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: txt.setFont(new Font(curAtt.sValue, txt.getFont().getStyle(), txt.getFont().getSize())); break;
        case attMIN:
               txt.setMinimumSize(new Dimension(curAtt.iValue, txt.getMinimumSize().height));
         break;
        case attMAX:
               txt.setMaximumSize(new Dimension(curAtt.iValue, txt.getMaximumSize().height));
         break;
        case attNAME: txt.setName(curAtt.sValue); break;
        case attREF: txt.setRef(curAtt.sValue); break;
        case attSIZE:
            txt.setPreferredSize(getSize(curAtt.sValue));
            txt.setColumns(0);
            break;
        case attTABSTOP: txt.setTabstop(curAtt.bValue); break;
        case attVISIBLE: txt.setVisible(curAtt.bValue); break;
        case attLINKCOL: txt.setLinkColumn(c, curAtt.sValue); break;
        case attFORMAT: txt.setFormat(curAtt.sValue); break;
        case attSEARCH: txt.setSearch(curAtt.bValue); break;
        case attREGEXP: txt.setRegexp(curAtt.sValue); break;
        case attNODE_TITLE: txt.setNodeTitle(curAtt.iValue); break;
        case attDRAG: txt.setDrag(curAtt.bValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(txt, curAtt.sValue); break;
        case attRESTORE: txt.setRestore(curAtt.bValue); break;
        // Messages
        case msgLOSTFOCUS: txt.setMsgLostFocus(curAtt.sValue); break;
        case msgDBLCLICK: txt.setMsgDblClick(curAtt.sValue); break;
        case msgCHANGE: txt.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: txt.setMsgMouseOver(curAtt.sValue); break;
				case msgDROP: txt.setMsgDrop(curAtt.sValue); break;
				case msgDRAG_ENTER: txt.setMsgDragEnter(curAtt.sValue); break;
				case msgDRAG_OVER: txt.setMsgDragOver(curAtt.sValue); break;
				case msgDRAG_EXIT: txt.setMsgDragExit(curAtt.sValue); break;
          // Bindings
          case attELEMENT: txt.setElementName(curAtt.sValue); break;
      }
    } // next i

    if (curKey.title.length() > 0 && c.isTable == false) {
      // final GuiLabel lbl = new GuiLabel(txt, curKey.title);
    	final GuiLabel lbl = new GuiLabel(txt, txt.getLabel());
      lbl.setVisible(txt.isVisible());
      if (lblSB != null) {
        lbl.getJComponent().setOpaque(true);
        lbl.setBackground(lblSB);
      }
      if (lblSF != null) {
        lbl.setForeground(lblSF);
      }
      // grid0
      if (c.grid0 != 0 && c.x == 0) {
        lbl.setPreferredSize(new Dimension(c.grid0, 17));
      }
      c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, 1, 1, 0, 0,
              makeLabelAnchor(anchor), GridBagConstraints.NONE, new Insets(it,il,ib,5), LPX, 0));
      c.x++;
      il=0;
    }
    if (c.isTable == false) {
      c.pPar.add(txt, new GridBagConstraints(c.x, c.y, w, h, wx, wy
                       ,anchor, fill, new Insets(it,il,ib,ir), px, py));
      if (c.curContainer != null) {
        c.curContainer.addMember(txt);
      }
      c.yoff=h;
      c.incX(w);
      c.curComponent = txt; // Current Component for Popup
    } else {
      c.cTbl.addColumn(txt, curKey.title, w);
      curKey.eol = false; // Wenn Tabellenspalte, dann Layout nicht weiterzählen
    }
  }

  private static void perfNumber(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    if (c.isTable==true) {w=80;}
    float wx=0, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.NORTHWEST;
    int fill=GridBagConstraints.NONE;
    Color lblSB = null;
    Color lblSF = null;

    final GuiNumber txt = new GuiNumber(curKey.title);

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attTT: txt.setToolTipText(curAtt.sValue); break;
        case attST: txt.setHint(curAtt.sValue); break;
        case attVAL: if (!GuiUtil.isAPI()) {txt.setValue(txt.getFormat().format(curAtt.dValue));} break;
        case attCOLS: txt.setColumns(curAtt.iValue); wx=0; w=1; break;
        case attDO: txt.setEnabled(!curAtt.bValue); break;
        case attNN: txt.setNotnull(curAtt.bValue); break;
        case attAL: txt.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
        case attSB: txt.setBackground(getColor(curAtt.sValue)); break;
        case attSF: txt.setForeground(getColor(curAtt.sValue)); break;
        case attLSB: lblSB = getColor(curAtt.sValue); break;
        case attLSF: lblSF = getColor(curAtt.sValue); break;
        case attPOINT: txt.setFont(txt.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: txt.setFont(txt.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: txt.setFont(new Font(curAtt.sValue, txt.getFont().getStyle(), txt.getFont().getSize())); break;
        case attMIN:
               txt.setMinimumSize(new Dimension(curAtt.iValue, txt.getMinimumSize().height));
         break;
        case attMAX:
               txt.setMaximumSize(new Dimension(curAtt.iValue, txt.getMaximumSize().height));
         break;
        case attNAME: txt.setName(curAtt.sValue); break;
        case attREF: txt.setRef(curAtt.sValue); break;
        case attSIZE:
            txt.setPreferredSize(getSize(curAtt.sValue));
            txt.setColumns(0);
            break;
        case attTABSTOP: txt.setTabstop(curAtt.bValue); break;
        case attVISIBLE: txt.setVisible(curAtt.bValue); break;
        case attLINKCOL: txt.setLinkColumn(c, curAtt.sValue); break;
        case attFORMAT: txt.setFormat(curAtt.sValue); break;
        case attSEARCH: txt.setSearch(curAtt.bValue); break;
        case attREGEXP: txt.setRegexp(curAtt.sValue); break;
        case attNODE_TITLE: txt.setNodeTitle(curAtt.iValue); break;
        case attDRAG: txt.setDrag(curAtt.bValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(txt, curAtt.sValue); break;
        case attRESTORE: txt.setRestore(curAtt.bValue); break;
        // Messages
        case msgLOSTFOCUS: txt.setMsgLostFocus(curAtt.sValue); break;
        case msgDBLCLICK: txt.setMsgDblClick(curAtt.sValue); break;
        case msgCHANGE: txt.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: txt.setMsgMouseOver(curAtt.sValue); break;
				case msgDROP: txt.setMsgDrop(curAtt.sValue); break;
				case msgDRAG_ENTER: txt.setMsgDragEnter(curAtt.sValue); break;
				case msgDRAG_OVER: txt.setMsgDragOver(curAtt.sValue); break;
				case msgDRAG_EXIT: txt.setMsgDragExit(curAtt.sValue); break;
          // Bindings
          case attELEMENT: txt.setElementName(curAtt.sValue); break;
      }
    } // next i
    if (curKey.title.length() > 0 && c.isTable == false) {
      // final GuiLabel lbl = new GuiLabel(txt, curKey.title);
    	final GuiLabel lbl = new GuiLabel(txt, txt.getLabel());
      lbl.setVisible(txt.isVisible());
      if (lblSB != null) {
        lbl.getJComponent().setOpaque(true);
        lbl.setBackground(lblSB);
      }
      if (lblSF != null) {
        lbl.setForeground(lblSF);
      }
      // grid0
      if (c.grid0 != 0 && c.x == 0) {
        lbl.setPreferredSize(new Dimension(c.grid0, 17));
      }
      c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, 1, 1, 0, 0,
          makeLabelAnchor(anchor), GridBagConstraints.NONE, new Insets(it,il,ib,5), LPX, 0));
      c.x++;
      il=0;
    }
    if (c.isTable == false) {
      c.pPar.add(txt, new GridBagConstraints(c.x, c.y, w, h, wx, wy
                   ,anchor, fill, new Insets(it,il,ib,ir), px, py));
      if (c.curContainer != null) {
        c.curContainer.addMember(txt);
      }
      c.yoff=h;
      c.incX(w);
      c.curComponent = txt; // Current Component for Popup
    } else { // isTable
      c.cTbl.addColumn(txt, curKey.title, w);
      curKey.eol = false; // Wenn Tabellespalte, dann Layout nicht weiterzählen
    }
  }

  private static void perfSpin(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=0, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor = GridBagConstraints.NORTHWEST;
    int fill = GridBagConstraints.NONE;
    Color lblSB = null;
    Color lblSF = null;

    GuiSpin spin = new GuiSpin(curKey.title);

    for (CurrentAttrib curAtt: curKey.vAttrib) {
	    switch (curAtt.iKeyword.intValue()) {
	      case attX: c.x=curAtt.iValue; break;
	      case attY: c.y=curAtt.iValue; break;
	      case attW: w=curAtt.iValue; break;
	      case attH: h=curAtt.iValue; break;
	      case attWX: wx=curAtt.fValue; break;
	      case attWY: wy=curAtt.fValue; break;
	      case attIT: it=curAtt.iValue; break;
	      case attIL: il=curAtt.iValue; break;
	      case attIB: ib=curAtt.iValue; break;
	      case attIR: ir=curAtt.iValue; break;
	      case attPX: px=curAtt.iValue; break;
	      case attPY: py=curAtt.iValue; break;
	      case attAN: anchor=getAnchor(curAtt.sValue); break;
	      case attFILL: fill=getFill(curAtt.sValue); break;
	      case attDO: spin.setEnabled(!curAtt.bValue); break;
	      case attTT: spin.setToolTipText(curAtt.sValue); break;
	      case attST: spin.setHint(curAtt.sValue); break;
	      case attLSB: lblSB = getColor(curAtt.sValue); break;
	      case attLSF: lblSF = getColor(curAtt.sValue); break;
	      case attPOINT: spin.setFont(spin.getFont().deriveFont(curAtt.fValue)); break;
	      case attSTYLE: spin.setFont(spin.getFont().deriveFont(getStyle(curAtt.sValue))); break;
	      case attFONT: spin.setFont(new Font(curAtt.sValue, spin.getFont().getStyle(), spin.getFont().getSize())); break;
	      case attMIN: spin.setMinimum(curAtt.iValue);
	                   spin.setValue(curAtt.sValue);
	                   break;
	      case attMAX: spin.setMaximum(curAtt.iValue); break;
	      case attVAL: if (!GuiUtil.isAPI()) { spin.setValue(curAtt.sValue);} break;
	      //case attCOLS: spin.setColumns(curAtt.iValue); wx=0; w=1; break;
	      case attNAME: spin.setName(curAtt.sValue); break;
	      case attREF: spin.setRef(curAtt.sValue); break;
	      case attSIZE: spin.setPreferredSize(getSize(curAtt.sValue)); break;
	      case attMINSIZE: spin.setMinimumSize(getSize(curAtt.sValue)); break;
	      case attMAXSIZE: spin.setMaximumSize(getSize(curAtt.sValue)); break;
	      case attTABSTOP: spin.setTabstop(curAtt.bValue); break;
	      case attVISIBLE: spin.setVisible(curAtt.bValue); break;
	      case attLINKCOL: spin.setLinkColumn(c, curAtt.sValue); break;
	      case attSEARCH: spin.setSearch(curAtt.bValue); break;
			  case attHELPID: GuiSession.getInstance().setHelpId(spin, curAtt.sValue); break;
        case attRESTORE: spin.setRestore(curAtt.bValue); break;
	      // Messages
	      case msgCHANGE: spin.setMsgChange(curAtt.sValue); break;
	      case msgMOUSEOVER: spin.setMsgMouseOver(curAtt.sValue); break;
	      // Bindings
	      case attELEMENT: spin.setElementName(curAtt.sValue); break;
	    }
	  } // next i

	  if (curKey.title.length() >0) {
	    GuiLabel lbl = new GuiLabel(spin, curKey.title);
	    lbl.setVisible(spin.isVisible());
	    if (lblSB != null) {
	      lbl.setBackground(lblSB);
	    }
	    if (lblSF != null) {
	      lbl.setForeground(lblSF);
	    }
	    // grid0
	    if (c.grid0 != 0 && c.x == 0) {
	      lbl.setPreferredSize(new Dimension(c.grid0, 17));
	    }
	    c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, 1, 1, 0, 0,
	                     makeLabelAnchor(anchor), GridBagConstraints.NONE, new Insets(it,il,ib,5), LPX, 0));
	    c.x++;
	    il = 0;
	  }
	  c.pPar.add(spin, new GridBagConstraints(c.x, c.y, w, h, wx, wy
	         ,anchor, fill, new Insets(it,il,ib,ir), px, py));
	  if (c.curContainer != null) {
	    c.curContainer.addMember(spin);
	  }
	  c.yoff=h;
	  c.x=c.x+w;
	}


  private static void perfPassword(CurrentKeyword curKey, CurContext c) {
    int w=3, h=1;
    float wx=3, wy=0;
    int it=5, ib=0, ir=5, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.NORTHWEST;
    int fill=GridBagConstraints.HORIZONTAL;
    Color lblSB = null;
    Color lblSF = null;

    GuiPassword txt = new GuiPassword(curKey.title);

    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attAN: anchor=getAnchor(curAtt.sValue); break;
        case attFILL: fill=getFill(curAtt.sValue); break;
        case attTT: txt.setToolTipText(curAtt.sValue); break;
        case attST: txt.setHint(curAtt.sValue); break;
        //case attVAL: txt.setText(curAtt.sValue); break;
        case attCOLS: txt.setColumns(curAtt.iValue); wx=0; w=1; break;
        //case attDO: txt.setDisplayOnly(curAtt.bValue); break;
        case attNN: txt.setNotnull(curAtt.bValue); break;
        case attAL: txt.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
        case attSB: txt.setBackground(getColor(curAtt.sValue)); break;
        case attSF: txt.setForeground(getColor(curAtt.sValue)); break;
        case attLSB: lblSB = getColor(curAtt.sValue); break;
        case attLSF: lblSF = getColor(curAtt.sValue); break;
        case attPOINT: txt.setFont(txt.getFont().deriveFont(curAtt.fValue)); break;
        case attSTYLE: txt.setFont(txt.getFont().deriveFont(getStyle(curAtt.sValue))); break;
        case attFONT: txt.setFont(new Font(curAtt.sValue, txt.getFont().getStyle(), txt.getFont().getSize())); break;
        case attMIN:
            txt.setMinimumSize(new Dimension(curAtt.iValue, txt.getMinimumSize().height));
          break;
        case attMAX:
            txt.setMaximumSize(new Dimension(curAtt.iValue, txt.getMaximumSize().height));
          break;
        case attNAME: txt.setName(curAtt.sValue); break;
        case attREF: txt.setRef(curAtt.sValue); break;
        case attSIZE: txt.setPreferredSize(getSize(curAtt.sValue)); break;
        case attMINLEN: txt.setMinlen(curAtt.iValue); break;
        case attMAXLEN: txt.setMaxlen(curAtt.iValue); break;
        case attHELPID: GuiSession.getInstance().setHelpId(txt, curAtt.sValue); break;
        //case attTABSTOP: txt.setTabstop(curAtt.bValue); break;
        //case attVISIBLE: txt.setVisible(curAtt.bValue); break;
        //case attLINKCOL: txt.setLinkColumn(cTbl, curAtt.iValue); break;
        // Messages
        case msgLOSTFOCUS: txt.setMsgLostFocus(curAtt.sValue); break;
        case msgCHANGE: txt.setMsgChange(curAtt.sValue); break;
        case msgMOUSEOVER: txt.setMsgMouseOver(curAtt.sValue); break;
      }
    } // next i
    if (curKey.title.length() > 0) {
        // final GuiLabel lbl = new GuiLabel(txt, curKey.title);    	
    	final GuiLabel lbl = new GuiLabel(txt, txt.getLabel());
      lbl.setVisible(txt.isVisible());
      if (lblSB != null) {
        lbl.getJComponent().setOpaque(true);
        lbl.setBackground(lblSB);
      }
      if (lblSF != null) {
        lbl.setForeground(lblSF);
      }
      // grid0
      if (c.grid0 != 0 && c.x == 0) {
        lbl.setPreferredSize(new Dimension(c.grid0, 17));
      }
      c.pPar.add(lbl, new GridBagConstraints(c.x, c.y, 1, 1, 0, 0,
              makeLabelAnchor(anchor), GridBagConstraints.NONE, new Insets(it,il,ib,5), LPX, 0));
      c.x++;
      il = 0;
    }
    c.pPar.add(txt, new GridBagConstraints(c.x, c.y, w, h, wx, wy
                     ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    if (c.curContainer != null) {
      c.curContainer.addMember(txt);
    }
    c.yoff=h;
    c.incX(w);
  }

  private static void perfTool(CurrentKeyword curKey, CurContext c) {
    if (curKey.title.equals("-")) { // Separator
      c.toolBar.addSeparator();
    }
    else { // Tool; keine Separator
      final GuiButton pb = new GuiButton(curKey.title);
      for (CurrentAttrib curAtt: curKey.vAttrib) {
        switch (curAtt.iKeyword.intValue()) {
          case attTT: pb.setToolTipText(curAtt.sValue); break;
          case attST: pb.setHint(curAtt.sValue); break;
          case attDO: pb.setEnabled(!curAtt.bValue); break;
          case attAL: pb.setHorizontalAlignment(getAlign(curAtt.sValue)); break;
          case attIMG: pb.setIcon(GuiUtil.makeIcon(curAtt.sValue)); break;
          case attFILE: if (!GuiUtil.isAPI() || curAtt.sValue.endsWith(")") == true) pb.setFileName(curAtt.sValue); break; // Eingebaute Methode
          case attMN: pb.setMnemonic(curAtt.cValue); break;
          case attCMD: pb.setActionCommand(curAtt.sValue); break;
          case attSB: pb.setBackground(getColor(curAtt.sValue)); break;
          case attSF: pb.setForeground(getColor(curAtt.sValue)); break;
          case attPOINT: pb.setFont(pb.getFont().deriveFont(curAtt.fValue)); break;
          case attSTYLE: pb.setFont(pb.getFont().deriveFont(getStyle(curAtt.sValue))); break;
          case attFONT: pb.setFont(new Font(curAtt.sValue, pb.getFont().getStyle(), pb.getFont().getSize())); break;
          /*
          geht nicht richtig
              case attMIN: min=curAtt.iValue;
                  pb.setMinimumSize(new Dimension(min, pb.getMinimumSize().height));
          break;
              case attMAX: max=curAtt.iValue;
                  pb.setMaximumSize(new Dimension(max, pb.getMaximumSize().height));
          break;
          */
          case attNAME: pb.setName(curAtt.sValue); break;
          case attREF: pb.setRef(curAtt.sValue); break; // merken für Suchdialog
          //case attSIZE: pb.setPreferredSize(getSize(curAtt.sValue)); break;
          case attTABSTOP: pb.setTabstop(curAtt.bValue); break;
          case attVISIBLE: pb.setVisible(curAtt.bValue); break;
          case attTYPE: pb.setType(curAtt.sValue); break;
  		  case attHELPID: GuiSession.getInstance().setHelpId(pb, curAtt.sValue); break;
          // Messages
          case msgMOUSEOVER: pb.setMsgMouseOver(curAtt.sValue); break;

        } // end switch
      } // next i
      c.toolBar.addGuiTool(pb);
      c.curContainer.addAction(pb);
//      GuiRootPane root = pb.getRootPane();
//      if(pb.getType() == GuiAction.HELP && root != null && root.getHelpId() != null) {
//      	HelpBroker helpBroker = GuiSession.getInstance().getHelpBroker();
//      	helpBroker.enableHelpKey(root, root.getHelpId(), null);
//      }
    } // Separator or Tool
  }

  private static void perfSeparator(CurrentKeyword curKey, CurContext c) {
    // 1. Toolbar
    if (c.toolBar != null) {
      Dimension size = null;
      for (CurrentAttrib curAtt : curKey.vAttrib) {
        switch (curAtt.iKeyword.intValue()) {
          case attSIZE :
            size = getSize(curAtt.sValue);
            break;
        }
      } // next i
      if (size == null) {
        c.toolBar.addSeparator();
      } else {
        c.toolBar.addSeparator(size);
      }
      return;
    }
    // 2. Menu
    if (c.curMenu != null) {
      c.curMenu.addSeparator();
      return;
    }
    // 3. Separator auf Panel
      int w = 1, h = 1;
      float wx = 1, wy = 0;
      int it = 0, ib = 0, ir = 0, il = 0;
      int px = 0, py = 0;
      int anchor = GridBagConstraints.CENTER;
      int fill = GridBagConstraints.HORIZONTAL;
      final JSeparator sep = new JSeparator();
      for (CurrentAttrib curAtt : curKey.vAttrib) {
        switch (curAtt.iKeyword.intValue()) {
          case attX :
            c.x = curAtt.iValue;
            break;
          case attY :
            c.y = curAtt.iValue;
            break;
          case attW :
            w = curAtt.iValue;
            break;
          case attH :
            h = curAtt.iValue;
            break;
          case attWX :
            wx = curAtt.fValue;
            break;
          case attWY :
            wy = curAtt.fValue;
            break;
          case attIT :
            it = curAtt.iValue;
            break;
          case attIL :
            il = curAtt.iValue;
            break;
          case attIB :
            ib = curAtt.iValue;
            break;
          case attIR :
            ir = curAtt.iValue;
            break;
          case attPX :
            px = curAtt.iValue;
            break;
          case attPY :
            py = curAtt.iValue;
            break;
          case attFILL :
            fill = getFill(curAtt.sValue);
            break;
          case attSIZE :
            break;
          case attTYPE : // TODO: Reihenfolge problematisch
            if (curAtt.sValue.equalsIgnoreCase("VERTICAL")) {
              sep.setOrientation(SwingConstants.VERTICAL);
              fill = GridBagConstraints.VERTICAL;
              wx = 0;
              wy = 1;
            } else if (curAtt.sValue.equalsIgnoreCase("HORIZONTAL")) {
              sep.setOrientation(SwingConstants.HORIZONTAL);
              fill = GridBagConstraints.HORIZONTAL;
              wx = 1;
              wy = 0;
            }
            break;
        }
      } // next i
      c.pPar.add(sep, new GridBagConstraints(c.x, c.y, w, h, wx, wy, anchor, fill, new Insets(it,
        il, ib, ir), px, py));
      c.yoff = h;
      c.incX(w);

  }

  private void perfUse(int iKey, ArrayList<CurrentKeyword> keys, CurrentKeyword curKey, CurContext c) {
    String useName = curKey.title;
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        // Default Values for next Container
        case attW: c.uw=curAtt.iValue; break;
        case attH: c.uh=curAtt.iValue; break;
        case attWX: c.uwx=curAtt.fValue; break;
        case attWY: c.uwy=curAtt.fValue; break;
        case attFILE: useName = curAtt.sValue; break;
        case attVAL: c.uVal = curAtt.sValue; break;
      } // end switch
    } // next i
    // Insert Script File
    ArrayList<CurrentKeyword> useKeys = null;
    try {
      logger.debug("perfUse: " + useName);
      // XML
     Document doc = GuiUtil.getDocument(useName);
     useKeys = XmlReader.makeKeywordList(doc);
    } catch (Exception ex) {
      GuiUtil.showEx(ex);
      return;
    }
    if (useKeys != null) {
      /*
      Ermittelte Keyword-List einfügen.
      */
      int lfdKey = 1;
      for (CurrentKeyword useCurKey: useKeys) {
        keys.add(iKey + lfdKey, useCurKey);
        lfdKey++;
      }
    } // End If null
  }
  private static void perfState(CurrentKeyword curKey, CurContext c) {
      String state = null;
      for (CurrentAttrib curAtt: curKey.vAttrib) {
        String aName = curAtt.sKeyword;
        String aValue = curAtt.sValue;
        if (aName.equals("name=")) {
            state = aValue;
        } else {
            if (c.curComponent != null && state != null) {
                if (c.curComponent instanceof GuiMember) {
                    GuiMember member = (GuiMember)c.curComponent;
                    member.addStateAttribute(state, aName.substring(0, aName.length()-1), aValue);
                }
            }
        }
      }
  }

   private static void perfTimer(CurrentKeyword curKey, CurContext c) {
      String name = "<dummy>";
      String cmd = "";
      int delay = 3000;
      boolean enabled = true;
      String msg = curKey.title;
      for(CurrentAttrib curAtt : curKey.vAttrib) {
         switch(curAtt.iKeyword.intValue()) {
            case attNAME:
               name = curAtt.sValue;
               break;
            case attCMD:
               cmd = curAtt.sValue;
               break;
            case attDELAY:
               delay = curAtt.iValue;
               break;
            case attENABLED:
               enabled = curAtt.bValue;
               break;
         }
      }
      GuiTimer t = new GuiTimer(c.form, msg, delay, cmd);
      t.setEnabled(enabled);
      c.form.getRootPane().getMainPanel().addTimer(t);
   }

  private static void perfXFiller(CurrentKeyword curKey, CurContext c) {
    int w=1, h=1;
    float wx=1, wy=0;
    int it=5, ib=0, ir=0, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.CENTER;
    int fill=GridBagConstraints.HORIZONTAL;
    final JLabel xFill = new JLabel(" ");
		//final JSeparator xFill = new JSeparator();
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        //case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attFILL: fill=getFill(curAtt.sValue); break;
      }
    } // next i
    c.pPar.add(xFill, new GridBagConstraints(c.x, c.y, w, h, wx, wy
          ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    c.yoff=h;
    c.incX(w);
  }

  private static void perfYFiller(CurrentKeyword curKey, CurContext c) {
    int w=4, h=1;
    float wx=1, wy=1;
    int it=5, ib=0, ir=0, il=0;
    int px=0, py=0;
    int anchor=GridBagConstraints.CENTER;
    int fill=GridBagConstraints.BOTH;
    final JLabel yFill = new JLabel();
    for (CurrentAttrib curAtt: curKey.vAttrib) {
      switch (curAtt.iKeyword.intValue()) {
        case attX: c.x=curAtt.iValue; break;
        case attY: c.y=curAtt.iValue; break;
        case attW: w=curAtt.iValue; break;
        case attH: h=curAtt.iValue; break;
        case attWX: wx=curAtt.fValue; break;
        case attWY: wy=curAtt.fValue; break;
        case attIT: it=curAtt.iValue; break;
        case attIL: il=curAtt.iValue; break;
        case attIB: ib=curAtt.iValue; break;
        case attIR: ir=curAtt.iValue; break;
        case attPX: px=curAtt.iValue; break;
        case attPY: py=curAtt.iValue; break;
        case attFILL: fill=getFill(curAtt.sValue); break;
      }
    } // next i
    c.pPar.add(yFill, new GridBagConstraints(c.x, c.y, w, h, wx, wy
           ,anchor, fill, new Insets(it,il,ib,ir), px, py));
    c.yoff=h;
    c.incX(w);
  }

  private static void perfScript(CurrentKeyword curKey, CurContext c, String curFilename) {
    if (GuiUtil.hasScripting() == true) {
      GuiFactoryHelper.perfScript(curKey, c, curFilename);
    } else {
      System.out.println("Warning: Scripting not installed!");
    }
  }

  private static void perfDesciption(CurrentKeyword curKey, CurContext c) {
	  // Noting to do;
  }
  //**************************
  /*
  public void getObjectSize() {
    final java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    try {
      new java.io.ObjectOutputStream( baos ).writeObject(this);
      System.out.println(baos.size());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  */
  final class CurContext {
    int x;
    int y;
    int yoff;
    int grid0, gridOffs;
    int uw=-1, uh=-1; // für Use
    float uwx=-1, uwy=-1; // Use
    String uVal; // Letzter Wert val= bei <Use val= />
    GuiWindow owner;
    GuiWindow form;
    GuiInternalFrame currentFrame;
    GuiPanel retPanel; // RückgabeWert für createPanel
    Document defaultValues;
    GuiContainer pPar;
    GuiOptionGroup curOptionGroup;

    LinkedHashMap<String, GuiOptionGroup> namedOptionGroups = new LinkedHashMap<String, GuiOptionGroup>();
    GuiOptionGroup getNOG(String name) {
    	GuiOptionGroup og = namedOptionGroups.get(name);
    	if (og == null) {
    		og = new GuiOptionGroup(name);
    		namedOptionGroups.put(name, og);
        if (curContainer != null) {
          curContainer.addMember(og);
        }
    	}
    	return og;
    }
    GuiDate currentGuiDate; // linkPopup
    // Table
    boolean isTable;
    GuiTable cTbl;
    LinkedHashMap<String,GuiTable> tables = new LinkedHashMap<String,GuiTable>();
    Vector tableData; // see <Table> Attibute val=[filename]
    GuiScrollBox tblScroll;
    GridBagConstraints tblGrid;
    // Tree
    GuiTree currentTree;
    GuiTreeNode curNode;
    GuiTreeNode treeStack[]= new GuiTreeNode[8]; // Stack of Nodes in a Tree
    int tpoi=0; // Stackpointer Treenodes
    GuiTreeElement treeElement = new GuiTreeElement("", null); // Ein Element im Tree
    // Container
    GuiContainer curContainer;
    CurrentPanel cpPar;
    int poi;
    CurrentPanel cpStack[] = new CurrentPanel[16]; // Stack of Panels
    Stack<GuiContainer> parentContainer = new Stack<GuiContainer>();
    // Toolbar
    GuiToolbar toolBar;
    // Menu
    GuiMenuBar menuBar;
    MenuAble curMenu; // Current Menu
    final Stack<MenuAble> menuStack = new Stack<MenuAble>();
    MemberPopupAble curComponent;  // current Component for PopupMenu
    // Current ButtonBar
    GuiButtonBar buttonBar;
    GuiOutlookBar outlookBar;
    GuiTaskPane taskPane;
    // Tabset
    GuiTabset cTabset = null;  // Current Tabset
    final GuiTabset tabsetStack[] = new GuiTabset[5]; // Stack of Tabsets
    int tabsetPoi = 0; // Pointer Tabset
    // Frame
    Point frameLocation = new Point(0,0); // Anfangswert für Internal Frames

    void incX(int off) {
      x += off;
    }
    void incY(int off) {
      y += off;
    }
    void resetXY() {
      x = 0;
      y = 0;
      yoff = 0;
    }
    /*
     * TODO Hack!? für CreatePanel?
     */
    boolean checkFirst(GuiPanel cont) {
      if (pPar == null) {
        pPar = cont;
        cpPar = new CurrentPanel(cont, 0, 0); // Current Parent Panel
        cpStack[0]=cpPar;
        parentContainer.push(cont);
        curContainer = cont;
        retPanel = cont;
        return true;
      } else {
        return false;
      }
    }
    /**
     * Erzeugt ein Dummy-Form, wenn Spezifikation kein Window enthält
     * (Früher wurde hier ein Dummy-Dialog erzeugt, der aber wegen
     * Bug#1453806 nicht mit Calendar-Popup funktioniert. Deshalb wird
     * jetzt als Workaround ein Form erzeugt, siehe Bug#1628711.)
     *
     */
    void createDummyForm() {
      if (form == null) {
        form = new GuiForm(GuiWindow.DUMMY_TITLE);
        // Kennzeichen setzen, dass es ein DummyDialog ist (wird in GuiDoc
        // verwendet.
        form.setDummyDialog(true);
        // Weil es schöner ist, wenn ein Panel richtig zu sehen ist. Geht aber
        // nur, weil hier als Workaround für Bug#1628771 ein Form erzeugt wird.
        form.setAutoSize(true);
        pPar = form.getMainPanel();
        cpPar = new CurrentPanel(form.getMainPanel(), 0, 0); // Current Parent Panel
        cpStack[0] = cpPar;
        parentContainer.push(form.getMainPanel());
        curContainer = form.getMainPanel();
      }
    }
    GuiWindow getForm() {
      return form;
    }
  }
  // Plugins ==========================================================
  static boolean hasPlugins() {
  	if (pluginsMap == null) return false;
  	else return true;
  }
  private void initPlugins() {
  	try {
  		File pluginDir = new File("plugins");
  		if (pluginDir.canRead()) {
				String dirs[] = pluginDir.list();
				for (int i = 0; i < dirs.length; i++) {
					String dirp = dirs[i];
					try {
						String path = "plugins" + System.getProperty("file.separator") + dirp + System.getProperty("file.separator");
						File pluginXml = new File (path+"plugin.xml");
						if (pluginXml.canRead()) {
							Document doc = new Document(pluginXml);
							PluginProvider vendor = new PluginProvider(path, doc);
							Vector<PluginKeyword> v = vendor.getPluginKeywords();
							for (PluginKeyword pkw: v) {
								addPluginKeyword(pkw);
							}
						}
					} catch (Exception ex) {
						System.err.println("GuiFactory#initPlugins; Error Initializing Plugins: "+ex.getMessage());
					}
				}
  		}
		} catch (Exception ex) {
			System.err.println("GuiFactory#initPlugins; Error Initializing Plugins: " +ex.getMessage());
		}
  }
  private void addPluginKeyword(PluginKeyword pkw) {
  	if (pluginsMap == null) {
  		pluginsMap = new HashMap<String, PluginKeyword>();
  	}
  	pluginsMap.put(pkw.keyword, pkw);
  	if (pkw.container == false) {
			hash.put(pkw.keyword, new Integer(9999));
			pluginsMap.put(pkw.keyword, pkw);
  	} else {
			hash.put("Begin "+pkw.keyword, new Integer(9999));
			Integer iEnd = hash.get("End "+pkw.extendsKeyword);
			hash.put("End "+pkw.keyword, iEnd);
			pluginsMap.put("Begin "+pkw.keyword, pkw);
			pluginsMap.put("End "+pkw.keyword, pkw);
		}
  }
  static PluginKeyword getPluginKeyword(String name) {
      PluginKeyword pkw = null;
      if (pluginsMap != null) {
           pkw = pluginsMap.get(name);
      }
      return pkw;
  }

  /**
   * Jeweils gültiger Container für GuiFactory
   */
  static final class CurrentPanel {
    /**
     * Der GuiContainer, dem Komponenten hinzugefügt werden sollen.
     */
    GuiContainer p;
    /**
    current X-Position im Grid
    */
    int lx;
    /**
    current Y-Position im Grid
    */
    int ly;
    // Constructor
    CurrentPanel(GuiContainer panel, int x, int y) {
      this.p = panel;
      this.lx = x;
      this.ly = y;
     }
  } // End of class CurrentPanel
  final class PluginProvider {
  	//String name;
  	//String version;
  	String libname;
  	private URLClassLoader ucl;
  	private Vector<PluginKeyword> keywords = new Vector<PluginKeyword>();
		// Constructor
		PluginProvider(String path, Document doc) {
			Element root = doc.getRoot();
			//this.name = root.getAttribute("name");
			//this.version = root.getAttribute("version");
			this.libname = root.getAttribute("library");
			try {
				String libPath = path + libname;
				File libFile = new File(libPath);
				if (libFile.canRead()) {
					URL url = libFile.toURI().toURL();
					ucl = new URLClassLoader(new URL[] { url });
					// Keywords
					Elements clsEles = root.getElements("class");
					while (clsEles.hasMoreElements()) {
						Element ele = clsEles.next();
						PluginKeyword pkw = new PluginKeyword(this, ele);
						keywords.add(pkw);
					}
				}
			} catch (Exception ex) {
				System.err.println("new PluginProvider: "+ex.getMessage());
				ex.printStackTrace();
			}
		}
		Vector<PluginKeyword> getPluginKeywords() {
			return keywords;
		}
		URLClassLoader getClassLoader() {
			return ucl;
	  }
  }
  final class LayoutConstraints {
		// GridBag
		int w=3, h=1;
		float wx=0, wy=0;
		int it=5, ib=0, ir=5, il=0;
		int px = 0;
		int py = 0;
		int columns = -1;
		int alignment = SwingConstants.LEFT;
		int anchor = GridBagConstraints.NORTHWEST;
		int fill = GridBagConstraints.HORIZONTAL;
		LayoutConstraints cloneLC() {
			LayoutConstraints ret = new LayoutConstraints();
			ret.w = this.w;
			ret.h = this.h;
			ret.wx = this.wx;
			ret.wy = this.wy;
			ret.it = this.it;
			ret.ib = this.ib;
			ret.ir = this.ir;
			ret.il = this.il;
			ret.px = this.px;
			ret.py = this.py;
			ret.columns = this.columns;
			ret.alignment = this.alignment;
			ret.anchor = this.anchor;
			ret.fill = this.fill;
			return ret;
		}
  }
  final class PluginKeyword {
  	//PluginProvider provider;
  	// Superclass
  	int superclass = keyTEXT;
  	String extendsKeyword;
  	String keyword;
  	boolean container = false;
  	private Class clazz;
  	// Layout
  	LayoutConstraints lc = new LayoutConstraints();
  	// Attributes
  	private HashMap<String, PluginAttrib> atts = new HashMap<String, PluginAttrib>();
  	private void addAttrib(PluginAttrib a) {
  		atts.put(a.name, a);
  	}
  	PluginAttrib getAttrib(String name) {
  		PluginAttrib a = atts.get(name);
  		return a;
  	}
  	// Constructor
  	PluginKeyword(PluginProvider provider, Element ele) {
  		//this.provider = provider;
  		String className = ele.getAttribute("name");
  		this.keyword = ele.getAttribute("keyword");
  		// Container?
  		String sContainer = ele.getAttribute("container");
  		if (sContainer != null && sContainer.equalsIgnoreCase("true")) {
  			this.container = true;
				XmlReader.addContainerTag(this.keyword);
  		}
			// Extends
			extendsKeyword = ele.getAttribute("extends");
			Integer ikw = null;
			if (this.container == false) {
				ikw = getKeywordInt(extendsKeyword);
			} else {
				ikw = getKeywordInt("Begin "+extendsKeyword);
			}
			if (ikw != null) {
				this.superclass = ikw.intValue();
			}
  		// GridBad
  		Element defaultEle = ele.getElement("DefaultLayoutConstraints");
  		if (defaultEle != null) {
  			this.lc.w = Convert.toInt(defaultEle.getAttribute("width"));
  			this.lc.h = Convert.toInt(defaultEle.getAttribute("height"));
  			this.lc.wx = Convert.toFloat(defaultEle.getAttribute("weightx"));
  			this.lc.wy = Convert.toFloat(defaultEle.getAttribute("weighty"));
  			this.lc.it = Convert.toInt(defaultEle.getAttribute("Insets.top"));
				this.lc.ir = Convert.toInt(defaultEle.getAttribute("Insets.right"));
				this.lc.il = Convert.toInt(defaultEle.getAttribute("Insets.left"));
				this.lc.ib = Convert.toInt(defaultEle.getAttribute("Insets.bottom"));
				this.lc.anchor = getAnchor(defaultEle.getAttribute("anchor"));
				this.lc.fill = getFill(defaultEle.getAttribute("fill"));
				{
					String s = defaultEle.getAttribute("columns");
					if (s != null) {
						lc.columns = Convert.toInt(s);
					}
				}
				{
					String s = defaultEle.getAttribute("horizontalAlignment");
					if (s != null) {
						lc.alignment = getAlign(s);
					}
				}
  		}
  		try {
	  		this.clazz = provider.getClassLoader().loadClass(className);
	  		// Attributes
	  		Elements eles = ele.getElements("Attribute");
	  		while (eles.hasMoreElements()) {
	  			Element a = eles.next();
	  			PluginAttrib pa = new PluginAttrib(this, a);
	  			this.addAttrib(pa);
	  		}
  		} catch (Exception ex) {
  			System.err.println("PluginKeyword: "+ex.getMessage());
  		}
  	}
  	Class getClazz() {
  		return this.clazz;
  	}
  }
  static final class PluginAttrib {
  	// ArgumentTypes
  	static final int BOOL = 1;
		static final int INT = 2;
		static final int FLOAT = 3;
		static final int STRING = 4;
		static final int CHAR = 5;
		static final int DOUBLE = 6;
		static final int COLOR = 7;
		static final int POINT = 8;
  	String name;
  	Method method;
  	int argtype = 4;
  	//PluginKeyword myKeyword;
  	// Constructor
  	PluginAttrib(PluginKeyword pkw, Element ele) {
  		//myKeyword = pkw;
  		this.name = ele.getAttribute("name");
  		String methodName = ele.getAttribute("method");
  		Method ms[] = pkw.getClazz().getMethods();
  		for (int i = 0; i < ms.length; i++) {
  			Method m = ms[i];
  			if (m.getName().equals(methodName)) {
  				this.method = m;
  			}
  		}
  		String at = ele.getAttribute("argumentType");
  		if (at != null) {
  			if (at.equalsIgnoreCase("bool")) {
  				this.argtype = BOOL;
  			} else if (at.equalsIgnoreCase("int")) {
  				this.argtype = INT;
				} else if (at.equalsIgnoreCase("float")) {
					this.argtype = FLOAT;
				} else if (at.equalsIgnoreCase("string")) {
					this.argtype = STRING;
				} else if (at.equalsIgnoreCase("char")) {
					this.argtype = CHAR;
				} else if (at.equalsIgnoreCase("double")) {
					this.argtype = DOUBLE;
				} else if (at.equalsIgnoreCase("color")) {
					this.argtype = COLOR;
				} else if (at.equalsIgnoreCase("point")) {
					this.argtype = POINT;
  			}
  		}
  		hash.put(this.name, new Integer(9998));
  	}
  }
  /**
   * HashMaps für übersetzungstabellen mit Werten füllen.
   */
  static {
    // Anchors
    anchorMap = new HashMap<String, Integer>();
    anchorMap.put("N", new Integer(GridBagConstraints.NORTH));
    anchorMap.put("NE", new Integer(GridBagConstraints.NORTHEAST));
    anchorMap.put("E", new Integer(GridBagConstraints.EAST));
    anchorMap.put("SE", new Integer(GridBagConstraints.SOUTHEAST));
    anchorMap.put("S", new Integer(GridBagConstraints.SOUTH));
    anchorMap.put("SW", new Integer(GridBagConstraints.SOUTHWEST));
    anchorMap.put("W", new Integer(GridBagConstraints.WEST));
    anchorMap.put("NW", new Integer(GridBagConstraints.NORTHWEST));
    anchorMap.put("C", new Integer(GridBagConstraints.CENTER));
    // Align
    alignMap = new HashMap<String, Integer>();
    alignMap.put("L", new Integer(SwingConstants.LEFT));
    alignMap.put("C", new Integer(SwingConstants.CENTER));
    alignMap.put("R", new Integer(SwingConstants.RIGHT));
    // Fill
    fillMap = new HashMap<String, Integer>();
    fillMap.put("N", new Integer(GridBagConstraints.NONE));
    fillMap.put("H", new Integer(GridBagConstraints.HORIZONTAL));
    fillMap.put("V", new Integer(GridBagConstraints.VERTICAL));
    fillMap.put("B", new Integer(GridBagConstraints.BOTH));
    // KeyStroke
    keyMap = new HashMap<String, Integer>(100);
    keyMap.put("0", new Integer(KeyEvent.VK_0));
    keyMap.put("1", new Integer(KeyEvent.VK_1));
    keyMap.put("2", new Integer(KeyEvent.VK_2));
    keyMap.put("3", new Integer(KeyEvent.VK_3));
    keyMap.put("4", new Integer(KeyEvent.VK_4));
    keyMap.put("5", new Integer(KeyEvent.VK_5));
    keyMap.put("6", new Integer(KeyEvent.VK_6));
    keyMap.put("7", new Integer(KeyEvent.VK_7));
    keyMap.put("8", new Integer(KeyEvent.VK_8));
    keyMap.put("9", new Integer(KeyEvent.VK_9));
    keyMap.put("F1", new Integer(KeyEvent.VK_F1));
    keyMap.put("F2", new Integer(KeyEvent.VK_F2));
    keyMap.put("F3", new Integer(KeyEvent.VK_F3));
    keyMap.put("F4", new Integer(KeyEvent.VK_F4));
    keyMap.put("F5", new Integer(KeyEvent.VK_F5));
    keyMap.put("F6", new Integer(KeyEvent.VK_F6));
    keyMap.put("F7", new Integer(KeyEvent.VK_F7));
    keyMap.put("F8", new Integer(KeyEvent.VK_F8));
    keyMap.put("F9", new Integer(KeyEvent.VK_F9));
    keyMap.put("F10", new Integer(KeyEvent.VK_F10));
    keyMap.put("F11", new Integer(KeyEvent.VK_F11));
    keyMap.put("F12", new Integer(KeyEvent.VK_F12));
    keyMap.put("a", new Integer(KeyEvent.VK_A));
    keyMap.put("A", new Integer(KeyEvent.VK_A));
    keyMap.put("b", new Integer(KeyEvent.VK_B));
    keyMap.put("B", new Integer(KeyEvent.VK_B));
    keyMap.put("c", new Integer(KeyEvent.VK_C));
    keyMap.put("C", new Integer(KeyEvent.VK_C));
    keyMap.put("d", new Integer(KeyEvent.VK_D));
    keyMap.put("e", new Integer(KeyEvent.VK_E));
    keyMap.put("f", new Integer(KeyEvent.VK_F));
    keyMap.put("g", new Integer(KeyEvent.VK_G));
    keyMap.put("h", new Integer(KeyEvent.VK_H));
    keyMap.put("i", new Integer(KeyEvent.VK_I));
    keyMap.put("j", new Integer(KeyEvent.VK_J));
    keyMap.put("k", new Integer(KeyEvent.VK_K));
    keyMap.put("l", new Integer(KeyEvent.VK_L));
    keyMap.put("m", new Integer(KeyEvent.VK_M));
    keyMap.put("n", new Integer(KeyEvent.VK_N));
    keyMap.put("N", new Integer(KeyEvent.VK_N));
    keyMap.put("o", new Integer(KeyEvent.VK_O));
    keyMap.put("O", new Integer(KeyEvent.VK_O));
    keyMap.put("p", new Integer(KeyEvent.VK_P));
    keyMap.put("P", new Integer(KeyEvent.VK_P));
    keyMap.put("q", new Integer(KeyEvent.VK_Q));
    keyMap.put("r", new Integer(KeyEvent.VK_R));
    keyMap.put("s", new Integer(KeyEvent.VK_S));
    keyMap.put("S", new Integer(KeyEvent.VK_S));
    keyMap.put("t", new Integer(KeyEvent.VK_T));
    keyMap.put("u", new Integer(KeyEvent.VK_U));
    keyMap.put("v", new Integer(KeyEvent.VK_V));
    keyMap.put("V", new Integer(KeyEvent.VK_V));
    keyMap.put("w", new Integer(KeyEvent.VK_W));
    keyMap.put("x", new Integer(KeyEvent.VK_X));
    keyMap.put("X", new Integer(KeyEvent.VK_X));
    keyMap.put("y", new Integer(KeyEvent.VK_Y));
    keyMap.put("z", new Integer(KeyEvent.VK_Z));

    keyMap.put("*", new Integer(KeyEvent.VK_ASTERISK));
    //keyMap.put("@", new Integer(KeyEvent.VK_AT));

    keyMap.put("Esc", new Integer(KeyEvent.VK_ESCAPE));
    keyMap.put("Druck", new Integer(KeyEvent.VK_PRINTSCREEN));
    keyMap.put("Untbr", new Integer(KeyEvent.VK_CANCEL));
    keyMap.put("Pause", new Integer(KeyEvent.VK_PAUSE));
    keyMap.put("Entf", new Integer(KeyEvent.VK_DELETE));
    keyMap.put("Ende", new Integer(KeyEvent.VK_END));
    keyMap.put("Pos1", new Integer(KeyEvent.VK_HOME));
    keyMap.put("Einfg", new Integer(KeyEvent.VK_INSERT));
    keyMap.put("Tab", new Integer(KeyEvent.VK_TAB));
    keyMap.put("Rollen", new Integer(KeyEvent.VK_SCROLL_LOCK));

    keyMap.put("Oben", new Integer(KeyEvent.VK_UP));
    keyMap.put("Unten", new Integer(KeyEvent.VK_DOWN));
    keyMap.put("Links", new Integer(KeyEvent.VK_LEFT));
    keyMap.put("Rechts", new Integer(KeyEvent.VK_RIGHT));
    keyMap.put("Bild auf", new Integer(KeyEvent.VK_PAGE_UP));
    keyMap.put("Bild ab", new Integer(KeyEvent.VK_PAGE_DOWN));
    // Keywords
    /*
     * Füllt die interne Hashtable mit dem Sprachumfang.
     * Lädt zusätzliche Keywords und Attribute aus Keyword.properties
     */
    hash = new HashMap<String, Integer>(250);
    hash.put("Begin Applet", new Integer(keyBEGIN_APPLET));
    hash.put("End Applet", new Integer(keyEND_APPLET));
    hash.put("Begin Form", new Integer(keyBEGIN_FORM));
    hash.put("End Form", new Integer(keyEND_FORM));
    hash.put("Begin Dialog", new Integer(keyBEGIN_DIALOG));
    hash.put("End Dialog", new Integer(keyEND_DIALOG));
    hash.put("Begin Panel", new Integer(keyBEGIN_PANEL));
    hash.put("End Panel", new Integer(keyEND_PANEL));
    hash.put("Begin Group", new Integer(keyBEGIN_GROUP));
    hash.put("End Group", new Integer(keyEND_GROUP));
    hash.put("Begin Tabset", new Integer(keyBEGIN_TABSET));
    hash.put("End Tabset", new Integer(keyEND_TABSET));
    hash.put("Begin Tab", new Integer(keyBEGIN_TAB));
    hash.put("End Tab", new Integer(keyEND_TAB));
    hash.put("Begin Table", new Integer(keyBEGIN_TABLE));
    hash.put("End Table", new Integer(keyEND_TABLE));
    hash.put("Begin Toolbar", new Integer(keyBEGIN_TOOLBAR));
    hash.put("End Toolbar", new Integer(keyEND_TOOLBAR));
    hash.put("Begin Menubar", new Integer(keyBEGIN_MENUBAR));
    hash.put("End Menubar", new Integer(keyEND_MENUBAR));
    hash.put("Begin Menu", new Integer(keyBEGIN_MENU));
    hash.put("End Menu", new Integer(keyEND_MENU));
    hash.put("Begin Popup", new Integer(keyBEGIN_POPUP));
    hash.put("End Popup", new Integer(keyEND_POPUP));
    hash.put("Begin Tree", new Integer(keyBEGIN_TREE));
    hash.put("End Tree", new Integer(keyEND_TREE));
    hash.put("Begin Folder", new Integer(keyBEGIN_FOLDER));
    hash.put("End Folder", new Integer(keyEND_FOLDER));
    hash.put("Begin Frame", new Integer(keyBEGIN_FRAME));
    hash.put("End Frame", new Integer(keyEND_FRAME));
    hash.put("Begin Split", new Integer(keyBEGIN_SPLIT));
    hash.put("End Split", new Integer(keyEND_SPLIT));
    hash.put("Begin Box", new Integer(keyBEGIN_BOX));
    hash.put("End Box", new Integer(keyEND_BOX));
    hash.put("Begin Element", new Integer(keyBEGIN_ELEMENT));
    hash.put("End Element", new Integer(keyEND_ELEMENT));
    hash.put("Begin OptionGroup", new Integer(keyBEGIN_OPTION_GROUP));
    hash.put("End OptionGroup", new Integer(keyEND_OPTION_GROUP));
    hash.put("Begin ButtonBar", new Integer(keyBEGIN_BUTTONBAR));
    hash.put("End ButtonBar", new Integer(keyEND_BUTTONBAR));
    hash.put("Begin ButtonBarButton", new Integer(keyBEGIN_BUTTONBAR_BUTTON));
    hash.put("End ButtonBarButton", new Integer(keyEND_BUTTONBAR_BUTTON));
    hash.put("Begin OutlookBar", new Integer(keyBEGIN_OUTLOOKBAR));
    hash.put("End OutlookBar", new Integer(keyEND_OUTLOOKBAR));
    hash.put("Begin OutlookBarTab", new Integer(keyBEGIN_OUTLOOKBAR_TAB));
    hash.put("End OutlookBarTab", new Integer(keyEND_OUTLOOKBAR_TAB));
    hash.put("Begin OutlookBarButton", new Integer(keyBEGIN_OUTLOOKBAR_BUTTON));
    hash.put("End OutlookBarButton", new Integer(keyEND_OUTLOOKBAR_BUTTON));

    hash.put("Begin TaskPane", new Integer(keyBEGIN_TASKPANE));
    hash.put("End TaskPane", new Integer(keyEND_TASKPANE));
    hash.put("Begin TaskPaneTab", new Integer(keyBEGIN_TASKPANE_TAB));
    hash.put("End TaskPaneTab", new Integer(keyEND_TASKPANE_TAB));
    hash.put("Begin TaskPaneButton", new Integer(keyBEGIN_TASKPANE_BUTTON));
    hash.put("End TaskPaneButton", new Integer(keyEND_TASKPANE_BUTTON));
    hash.put("Begin Chart", new Integer(keyBEGIN_CHART));
    hash.put("End Chart", new Integer(keyEND_CHART));
    hash.put("Begin Browser", new Integer(keyBEGIN_BROWSER));
    hash.put("End Browser", new Integer(keyEND_BROWSER));
    hash.put("Begin JFX", new Integer(keyBEGIN_JFX));
    hash.put("End JFX", new Integer(keyEND_JFX));
  
// Components
    hash.put("Text", new Integer(keyTEXT));
    hash.put("Date", new Integer(keyDATE));
    hash.put("Time", new Integer(keyTIME));
    hash.put("Money", new Integer(keyMONEY));
    hash.put("Number", new Integer(keyNUMBER));
    hash.put("Password", new Integer(keyPASSWORD));
    hash.put("Memo", new Integer(keyMEMO));
    hash.put("Combo", new Integer(keyCOMBO));
    hash.put("List", new Integer(keyLIST));
    hash.put("Check", new Integer(keyCHECK));
    hash.put("Option", new Integer(keyOPTION));
    hash.put("Label", new Integer(keyLABEL));
    hash.put("Button", new Integer(keyBUTTON));
    hash.put("TButton", new Integer(keyTBUTTON));
    hash.put("Scrollbar", new Integer(keySCROLLBAR));
    hash.put("Slider", new Integer(keySLIDER));
    hash.put("xFiller", new Integer(keyXFILLER));
    hash.put("yFiller", new Integer(keyYFILLER));
    hash.put("Editor", new Integer(keyEDITOR));
    hash.put("Content", new Integer(keyCONTENT));
    hash.put("Document", new Integer(keyDOCUMENT));
    hash.put("Spin", new Integer(keySPIN));
    hash.put("Title", new Integer(keyTITLE));
    hash.put("Calendar", new Integer(keyCALENDAR));
    hash.put("CalendarPopup", new Integer(keyCALENDAR_POPUP));
    // special components
    hash.put("Item", new Integer(keyITEM));
    hash.put("ItemCheck", new Integer(keyITEM_CHECK));
    hash.put("ItemOption", new Integer(keyITEM_OPTION));
    hash.put("Tool", new Integer(keyTOOL));
    hash.put("Separator", new Integer(keySEPARATOR));
    hash.put("Node", new Integer(keyNODE));
    hash.put("Row", new Integer(keyROW));
    hash.put("Column", new Integer(keyCOLUMN));
    hash.put("Hidden", new Integer(keyHIDDEN));
    hash.put("Border", new Integer(keyBORDER));
    hash.put("State", new Integer(keySTATE));
    hash.put("Timer", new Integer(keyTIMER));
    // special statements
    hash.put("Use", new Integer(keyUSE));
//    hash.put("Begin Case", new Integer(keyBEGIN_CASE));
//    hash.put("End Case", new Integer(keyEND_CASE));
    // Scripting
    hash.put("Script", new Integer(keySCRIPT));
    hash.put("language=", new Integer(attLANGUAGE));
    hash.put("src=", new Integer(attSRC));
    // Description
    hash.put("Desc", new Integer(keyDESCRIPTION));
    // Attributes
    hash.put("x=", new Integer(attX));
    hash.put("y=", new Integer(attY));
    hash.put("h=", new Integer(attH));
    hash.put("w=", new Integer(attW));
    hash.put("ir=", new Integer(attIR));
    hash.put("il=", new Integer(attIL));
    hash.put("it=", new Integer(attIT));
    hash.put("ib=", new Integer(attIB));
    hash.put("wx=", new Integer(attWX));
    hash.put("wy=", new Integer(attWY));
    hash.put("px=", new Integer(attPX));
    hash.put("py=", new Integer(attPY));
    hash.put("an=", new Integer(attAN));
    hash.put("fill=", new Integer(attFILL));
    hash.put("cols=", new Integer(attCOLS));
    hash.put("Items=", new Integer(attITEMS)); // Deprecated see items
    hash.put("items=", new Integer(attITEMS));
    hash.put("do=", new Integer(attDO));
    hash.put("nn=", new Integer(attNN));
    hash.put("tt=", new Integer(attTT));
    hash.put("st=", new Integer(attST));
    hash.put("val=", new Integer(attVAL));
    hash.put("file=", new Integer(attFILE));
    hash.put("cmd=", new Integer(attCMD));
    hash.put("gif=", new Integer(attIMG)); // deprecated see img
    hash.put("img=", new Integer(attIMG));
    hash.put("sicon=", new Integer(attSICON));
    hash.put("mn=", new Integer(attMN));
    hash.put("al=", new Integer(attAL));
    hash.put("topic=", new Integer(attTOPIC));
    hash.put("sb=", new Integer(attSB));
    hash.put("sf=", new Integer(attSF));
    hash.put("lsb=", new Integer(attLSB));
    hash.put("lsf=", new Integer(attLSF));
    hash.put("point=", new Integer(attPOINT));
    hash.put("style=", new Integer(attSTYLE));
    hash.put("font=", new Integer(attFONT));
    hash.put("acc=", new Integer(attACC));
    hash.put("typ=", new Integer(attTYPE)); // deprecated see type
	 hash.put("type=", new Integer(attTYPE));
    hash.put("min=", new Integer(attMIN));
    hash.put("max=", new Integer(attMAX));
    hash.put("name=", new Integer(attNAME));
    hash.put("ref=", new Integer(attREF));
    hash.put("size=", new Integer(attSIZE));
    hash.put("minSize=", new Integer(attMINSIZE));
    hash.put("maxSize=", new Integer(attMAXSIZE));
    hash.put("tabstop=", new Integer(attTABSTOP));
    hash.put("visible=", new Integer(attVISIBLE));
    hash.put("linkCol=", new Integer(attLINKCOL));
    hash.put("linkTable=", new Integer(attLINKTABLE));
    hash.put("format=", new Integer(attFORMAT));
    hash.put("closedIcon=", new Integer(attCLOSED_ICON));
    hash.put("openIcon=", new Integer(attOPEN_ICON));
    hash.put("leafIcon=", new Integer(attLEAF_ICON));
    hash.put("grid0=", new Integer(attGRID0));
    hash.put("minLen=", new Integer(attMINLEN));
    hash.put("maxLen=", new Integer(attMAXLEN));
    hash.put("search=", new Integer(attSEARCH));
    hash.put("regexp=", new Integer(attREGEXP));
    hash.put("invert=", new Integer(attINVERT));
    hash.put("map=", new Integer(attMAP));
    hash.put("minVal=", new Integer(attMINVAL));
    hash.put("maxVal=", new Integer(attMAXVAL));
    hash.put("layout=", new Integer(attLAYOUT));
    hash.put("rb=", new Integer(attRB));
    hash.put("nodeTitle=", new Integer(attNODE_TITLE));
    hash.put("pack=", new Integer(attPACK));
	hash.put("colSpec=", new Integer(attCOLSPEC));
	hash.put("rowSpec=", new Integer(attROWSPEC));
	hash.put("rowHeight=", new Integer(attROWHEIGHT));
	hash.put("closeAble=", new Integer(attCLOSEABLE));
	hash.put("iconAble=", new Integer(attICONABLE));
	hash.put("maxAble=", new Integer(attMAXABLE));
	hash.put("drag=", new Integer(attDRAG));
	hash.put("autosize=", new Integer(attAUTOSIZE));
	hash.put("restore=", new Integer(attRESTORE));
	hash.put("helpID=", new Integer(attHELPID));
	hash.put("UI=", new Integer(attUI));
	hash.put("border=", new Integer(attBORDER));
	hash.put("ogname=", new Integer(attOG_NAME));
	hash.put("htp=", new Integer(attHTP)); // HorizontalTextPosition
	hash.put("location=", new Integer(attLOC)); // Location
	hash.put("delay=", new Integer(attDELAY)); // delay Timer
    hash.put("enabled=", new Integer(attENABLED)); // delay Timer
    hash.put("enabledWhen=", new Integer(attENABLED_WHEN)); // MenuItems

    // Bindings
    hash.put("controler=", new Integer(attCONTROLLER)); // deprecated
    hash.put("controller=", new Integer(attCONTROLLER));
    hash.put("element=", new Integer(attELEMENT));
    hash.put("root-element=", new Integer(attROOT_ELEMENT));
    hash.put("dataset=", new Integer(attDATASET));
    hash.put("displayMember=", new Integer(attDISPLAY_MEMBER));
    hash.put("valueMember=", new Integer(attVALUE_MEMBER));
    hash.put("ro=", new Integer(attRO));

    // Messages *****************************************
    hash.put("OnCreate=", new Integer(msgCREATE));
    hash.put("OnOpen=", new Integer(msgOPEN));
    hash.put("OnClose=", new Integer(msgCLOSE));
    hash.put("OnActive=", new Integer(msgACTIVE));
    hash.put("OnClick=", new Integer(msgCLICK));
    hash.put("OnDblClick=", new Integer(msgDBLCLICK));
    hash.put("OnMouseOver=", new Integer(msgMOUSEOVER));
    hash.put("OnMouseMove=", new Integer(msgMOUSEMOVE));
    //hash.put("OnGotFocus=", new Integer(msgGOTFOCUS));
    hash.put("OnLostFocus=", new Integer(msgLOSTFOCUS));
    hash.put("OnChange=", new Integer(msgCHANGE));
    hash.put("OnColHeaderClick=", new Integer(msgCOLHEADERCLICK));
    //hash.put("OnColHeaderDblClick=", new Integer(msgCOLHEADERDBLCLICK));
    hash.put("OnRowClick=", new Integer(msgROWCLICK));
    hash.put("OnNodeClick=", new Integer(msgNODECLICK));
    hash.put("OnDragEnter=", new Integer(msgDRAG_ENTER));
    hash.put("OnDragOver=", new Integer(msgDRAG_OVER));
    hash.put("OnDragExit=", new Integer(msgDRAG_EXIT));
    hash.put("OnDrop=", new Integer(msgDROP));
    hash.put("OnFileDrop=", new Integer(msgFILEDROP));
    hash.put("OnKeyTyped=", new Integer(msgKEYTYPED));
    //hash.put("OnKeyInsert=", new Integer(msgKEYINSERT));
    //hash.put("OnKeyDelete=", new Integer(msgKEYDELETE));
    hash.put("OnPreSort=", new Integer(msgPRE_SORT));
    hash.put("OnPostSort=", new Integer(msgPOST_SORT));
    hash.put("OnPopup=", new Integer(msgPOPUP));
    // Properties ****************************************

    // Load additional Keywords and Attributes
    try {
      final Properties prop = GuiUtil.loadProperties("Keyword.properties");
      int i = 1000; // Offset additional Keywords
      String key;
      Integer val;
      for (Enumeration<?> e = prop.propertyNames() ; e.hasMoreElements() ;) {
        key = (String)e.nextElement();
        val = hash.get(prop.getProperty(key));
        if (val == null) { // Vorhandene Einträge nicht überschreiben!
          hash.put(prop.getProperty(key), new Integer(i));
          i++;
        } else {
          hash.put(key, val);
        }
      }
    } catch (Exception ex) {
      // nix machen, wenn fehlt
    }
    // System Properties
    // 16.10.2004 PKÖ Unterdrückt das Swing-Drop-Verhalten,
    // für GuiBuilder Drag-Drop
    try { // try .. catch wegen Applet-Security
    	System.setProperty("suppressSwingDropSupport", "true");
    } catch (Exception ex) {
   	 try {
   		 logger.error(ex.getMessage(), ex);
   	 } catch (Exception ex2) {
   		 System.err.println(ex.getMessage());
   	 }
    }
  } // End static
	public int getNumberOfWindowsCreated() {
		return numberOfWindowsCreated;
	}
	public long getTotalTimeUsed() {
		return totalTimeUsed;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	  /**
	   * @deprecated Wird nurnoch für Repository.txt benötigt
	  * Diese Methode verwandelt eine als String vorliegende Spezifikation
	  * in eine ArrayList, der alle Keywords mit ihren Attributen enthält.
	  * Anschließend kann dieser für das Erzeugen von Oberfläche
	  * oder zur Code-Generierung eingesetzt werden.
	  * @param s Eine Spezifikation in "alter" Notation.
	  * @return ArrayList von Keywords
	  * @see CurrentKeyword
	  */
	  public final ArrayList<CurrentKeyword> makeKeywordList(String s) throws GDLParseException {
	    if (s == null) {
	      return null;
	    }
	    final ArrayList<CurrentKeyword> ret = new ArrayList<CurrentKeyword>();
	    CurrentKeyword curKey = null;
	    int lineCnt = 0;
	    String ll;
	    int p1;
	    boolean endOfLine = true;

	    String xl = ""; // temporary
	    StringBuffer comment = new StringBuffer(); // Kommentar
	    boolean skip = false;
	    boolean skipVersion = false;
	    boolean isComment = false;
	    StringBuffer conti = null;
	    // ************* Schleife Zeilen im String **************
	    final StringTokenizer lines = new StringTokenizer(s, "\n\r");
	    while (lines.hasMoreTokens()) {
	      ll = lines.nextToken().trim();
	      lineCnt++; // Zeilenzähler
	      //System.out.println(ll);
	      // Kommentare überlesen, andere Version überlesen,
	      // Fortsetzungszeilen einsammeln.
	      if (ll.startsWith("/*")) {
	        skip = true;
	        isComment = true;
	        comment = comment.append(ll.substring(2)).append("\n");
	        // einzeiliger Kommentar mit /* ... */
	        if (ll.endsWith("*/")) {
	          if (skipVersion == false) {
	            skip = false;
	          }
	          comment.delete(comment.length()-3, comment.length()-1);
	          isComment = false;
	        }
	      } else if (ll.endsWith("*/")) {
	        if (skipVersion == false) {
	          skip = false;
	        }
	        comment = comment.append(ll.substring(0, ll.length()-2));
	        isComment = false;
	      } else if (ll.startsWith("End Case") && isComment == false) {
	        skip = false;
	        skipVersion = false;
	      } else if (ll.startsWith("Begin Case") && isComment == false) {
	        if (ll.indexOf(GuiUtil.getVersion()) == -1) {
	          skip = true;
	          skipVersion = true;
	        }
	      } else if (ll.startsWith("/")) {
	        // Single comment line = do nothing
	        comment = comment.append(ll).append("\n");
	        //skip = true;
	      } else if (ll.length() == 0) {
	        // empty line = do nothing
	        if (isComment == true) {
	          comment = comment.append("\n");
	        }
	      } else if (skip == true) {
	        // skip interpreting lines
	        if (isComment == true) {
	          comment = comment.append(ll).append("\n");
	        }
	      } else if (ll.endsWith(" -") && skip == false) {
	        // Fortsetzungszeile
	        if (conti == null) {
	          conti = new StringBuffer();
	        }
	        conti = conti.append(ll.substring(0, ll.length()-1));
	        //skip = true;
	      } else {
	        if (conti != null) { // Fortsetzungszeile
	          conti.append(ll);
	          ll = conti.toString();
	          conti = null;
	        }
	        // ********* Loop the Components in the line ************
	        do {
	          p1 = ll.indexOf(" || "); // Component-Concatenator
	          if (p1 > 0) { // Es gibt noch Folgekomponenten in der selben Zeile
	            xl = ll.substring(0, p1).trim();
	            ll = ll.substring(p1 + 4, ll.length());
	            endOfLine = false;
	          } else {
	            xl = ll;
	            endOfLine = true;
	          }
	          // *********************
	          curKey = makeOneKeyword(xl, endOfLine, comment.toString(), lineCnt);
	          if (curKey == null) {
	            //##errLine = lineCnt;
	            return null;
	          }
	          ret.add(curKey);
	          comment = new StringBuffer();
	        } // End of Do
	        while (p1 > 0); // Schleife selbe Zeile mit ||
	      }
	    } // End Of hasMore Tokens
	    return ret;
	  }

	  /**
	   * @deprecated Wird nurnoch für Repository.txt benötigt
	   * Wandelt eine Komponente in "alter" Spezifikation in ein
	   * Keyword um.
	   * @param line Eine Zeile mit der kompletten Spezifikation für ein Keyword mit allen Attributen
	   * @param eol Kennzeichen, ob letzte Komponente in der Zeile oder nicht
	   * @param comment Ggf. Kommentar im Text davor.
	   * @param lineCnt Ldf. Nr. der Zeile im SourceCode.
	   * @return Das aus der Zeile fertig ermittelte Keyword
	   * @see #makeKeywordList
	   */
	  private final CurrentKeyword makeOneKeyword(String line, boolean eol, String comment, int lineCnt)
	  throws GDLParseException {
	    String title = "";
	    int p;
	    boolean bOff = false;
	    boolean bTitle = false;
	    boolean bValue = false;
	    String sTok;
	    String sxOff = "";
	    String sKeyword = "";
	    String sValue = "";

	    final CurrentKeyword curKey = new CurrentKeyword();
	    curKey.eol = eol;
	    if (comment.length() > 0) {
	      curKey.comment = comment.trim();
	    }
	    CurrentAttrib curAtt = new CurrentAttrib();

	    // ***************** Attribute des Keywords ermitteln ******************
	    final StringTokenizer tokens = new StringTokenizer(line); // Blanc als Separator!
	    while (tokens.hasMoreTokens()) {
	      sTok = tokens.nextToken();
	      if (sTok.equals("End")) { // Keyword Container Ende
	        bOff=true;
	        sxOff="End ";
	      } else if (sTok.equals("Begin")) { // Keyword Container Anfang
	        bOff=true;
	        sxOff="Begin ";
	      } else if (sTok.equals("\"")) { // einzelnes Gänsebein: Gehört zum Label
	        if (bTitle == true) {
	          title = title + " ";
	          bTitle = false;
	          curKey.title = title;
	        } else {
	          bTitle=true;
	          title="";
	        }
	      } else if (sTok.startsWith("\"")) { // Fängt an mit Gänsebein: Label
	        if (sTok.endsWith("\"")) { // Hört auch auf mit Gänsebein
	          title=sTok.substring(1, sTok.length()-1);
	          curKey.title=title; // Label ist fertig
	        } else { // Hört nicht auf mit Gänsebein: Wir warten auf weitere Tokens zum Label
	          title=sTok.substring(1);
	          bTitle=true;
	        }
	      } else if (bTitle==true) { // Weiterer Token zum Titel
	        title=title+" "+sTok;
	        if (sTok.endsWith("\"")) { // Hört auf mit Gänsebein: Label ist fertig
	          bTitle=false;
	          curKey.title=title.substring(0, title.length()-1);
	        }
	      }
	      else if (bValue==true) { // Weitere Tokens zum Attributwert
	        sValue=sValue+" "+sTok;
	        if (sTok.endsWith("\"")) { // Attributwert ist fertig
	          bValue=false;
	          curAtt.setValue(sValue);
	          curKey.add(curAtt);
	          curAtt = new CurrentAttrib();
	        }
	      }
	      else {
	        if (bOff==true) { // Container-Keyword fertig
	          bOff=false;
	          sKeyword = sxOff+sTok;
	          curKey.setKeyword(sKeyword);
	        }
	        if (sTok.indexOf("=") > 0) { // Attribut?
	          p=sTok.indexOf("=");
	          sKeyword=sTok.substring(0, p+1);
	          curAtt.sKeyword=sKeyword;
	          sValue=sTok.substring(p+1, sTok.length());
	          if (sValue.startsWith("\"")) {
	            if (sValue.endsWith("\"") == false) {
	              bValue=true;
	            }
	          }
	          if (bValue == false) {
	            curAtt.setValue(sValue);
	            curKey.add(curAtt);
	            curAtt = new CurrentAttrib();
	          }
	        }
	        else { // Einfaches Keyword (kein Container).
	          sKeyword = sTok;
	          if (curKey.sKeyword == null || curKey.sKeyword.length() == 0) {
	            curKey.setKeyword(sKeyword);
	          }
	        }
	      }
	    } // End while hasMoreTokens
	    curKey.iKeyword = getKeywordInt(curKey.sKeyword); // Schlüsselwort
	    if (curKey.iKeyword == null) {
	    	String msg = "Illegal Keyword: "+curKey.sKeyword;
	    	logger.error(msg + " in line " + lineCnt + ".");
	    	throw new GDLParseException(msg, lineCnt);
	    }
	    // Attribute formatieren
	    for (ListIterator<CurrentAttrib> i = curKey.vAttrib.listIterator() ; i.hasNext() ;) {
	      curAtt = i.next();
	      curAtt.iKeyword = getKeywordInt(curAtt.sKeyword);
	      if (curAtt.iKeyword == null) {
	    	  String msg = "Illegal Attribute (ignored): "+curAtt.sKeyword + "in line " + lineCnt +".";
	    	  logger.warn(msg);
	    	  System.out.println(msg);
	    	  i.remove();
	      }
	    } // next i; Ende Schleife Attribute formatieren
	    return curKey;
	  } // End Of Method makeOneKeyword

} // end of Class GuiFactory
