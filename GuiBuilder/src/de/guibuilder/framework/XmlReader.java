package de.guibuilder.framework;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;

import org.w3c.dom.Node;

import electric.xml.Attribute;
import electric.xml.Attributes;
import electric.xml.Child;
import electric.xml.Comment;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import electric.xml.XPath;

import de.pkjs.util.*;

/**
 * Diese Klasse liest ein Xml-Dokument ein, und wandelt es in die
 * interne Struktur um.
 * @see #makeKeywordListFromXmlFile
 * @see #makeKeywordListFromString
 * @see GuiFactory#createWindowXml
 */
public final class XmlReader {
  private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XmlReader.class);
  private static String codebase = GuiUtil.getCurrentDir();
  private static ResourceBundle bundle;
  /**
   * Set of Container Tags.
   */
  private static HashSet<String> contTags = new HashSet<String>(20);

  /**
   * Erzeugt ein XML-Document aus dem angegebenen String;
   * wirf eine Exception, wenn das kein gültiges XML-Document
   * @param source
   * @return
   * @throws GDLParseException
   */
  public static Document createDocument(String source) throws GDLParseException {
    if (source == null) {
      System.out.println("XmlReader#createDocument Warning! Null Source!"); // TODO: logger
      return null;
    }
    try {
      Document doc = new Document(source);
      return doc;
    }
    catch (ParseException es) {
      String msg = es.getMessage();
      int p1 = msg.indexOf("line ");
      int p2 = msg.indexOf(", char", p1);
      if (p1 != 0 && p2 != 0 && p1<p2) {
        int errLine = Convert.toInt(msg.substring(p1+5,p2));
        throw new GDLParseException(msg, errLine);
      }
      throw new GDLParseException(msg);
    }
  }
  /**
   * Liefert eine ArrayList mit allen Komponenten auf Grundlage
   * eines String, der eine XML-Spezifikation enthält.
   * @param source String mit zu analysierdenden XML-Sepzifikation
   * @return ArrayList von Keywords
   */
  public static ArrayList<CurrentKeyword> makeKeywordListFromString(String source)
  	throws GDLParseException {
    Document doc = createDocument(source);
    ArrayList<CurrentKeyword> ret = makeKeywordList(doc);
    return ret;
  }
  /**
   * Liefert eine ArrayList mit allen Komponenten auf Grundlage
   * einer XML-Datei.
   * @param fileName Name der zu analysierdenden XML-Datei
   * @return ArrayList von Keywords
   */
  public static ArrayList<CurrentKeyword> makeKeywordListFromXmlFile(String fileName)
  throws GDLParseException {
    ArrayList<CurrentKeyword> ret = null;
    Document doc = null;
    File f = null;

    if (fileName.indexOf(":") == 1) { // Laufwerksbuchstabe?
      f = new File (fileName);
    }
    else { // relative Pfadangabe
      f = new File (codebase, fileName);
    }

    try {
      doc = new Document(f);
    }
    catch (ParseException es) {
      String msg = es.getMessage();
      int p1 = msg.indexOf("line ");
      int p2 = msg.indexOf(", char", p1);
      if (p1 != 0 && p2 != 0 && p1<p2) {
        int errLine = Convert.toInt(msg.substring(p1+5,p2));
        throw new GDLParseException(msg, errLine);
      }
      GuiUtil.showEx(es);
      return null;
    }
    ret = makeKeywordList(doc);
    return ret;
  }
  /**
    * Liefert eine ArrayList mit allen Komponenten auf Grundlage
    * eines XML-Documents, der eine XML-Spezifikation enthält.
    * @see #makeKeywordListFromXmlFile
    * @see #makeKeywordListFromString
   */
  static ArrayList<CurrentKeyword> makeKeywordList(Document doc) {
    if (doc == null) return null;
    bundle = GuiUtil.getDefaultResourceBundle();
    ArrayList<CurrentKeyword> ret = new ArrayList<CurrentKeyword>(100);
    Element root = doc.getRoot();
    makeEle (root, ret);
    return ret;
  }

  private static void makeEle(Element root, ArrayList<CurrentKeyword> ret) {
    Elements nl = root.getElements();
    Element ele;
    Child node;
    //Comment comment = null;
    String nn;
    String bnn;
    boolean container;
    CurrentKeyword keyword;
    CurrentAttrib att;
    Attributes map;
    Attribute xmlAtt;
    String an;
    String val;
    while (nl.hasMoreElements()) {
      ele = nl.next();
      nn = ele.getName();
      if (contTags.contains(nn)) {
        container = true;
      } else {
        container = false;
      }
      // New Keyword
      bnn = nn;
      if (container == true) {
        bnn = "Begin "+nn;
      }
      keyword = new CurrentKeyword(bnn);
      //System.out.println("New Keyword: "+bnn);
      keyword.iKeyword = GuiFactory.getKeywordInt(bnn);
      if (keyword.iKeyword == null) {
    	  logger.error("ParseException: Illegal Element: " + bnn);
    	  if (GuiSession.getInstance().getCurrentWindow() != null) {
	        if (GuiUtil.okCancelMessage(null, "ParseException", "Illegal Element: "+bnn) == false) {
	          ret = null;
	          return;
	        }
    	  }
        continue;
      }
      // Plugin?
      if (GuiFactory.hasPlugins()) {
      	if (GuiFactory.getPluginKeyword(nn) != null) {
      		keyword.isPlugin = true;
      	}
      }
      keyword.eol = true;
      // Kommentar?
      // geht schief, wenn Kommentar ein NextSiblingNode ist,
      // aber kein previous (andere Ebene im Baum).
      node = ele.getPreviousSiblingChild();
      if (node instanceof Comment) {
        //System.out.println(((Comment)node).getString());
        //System.out.println(ele.toString());
        keyword.comment = ((Comment)node).getString();
      }
      // Add to ArrayList
      ret.add(keyword);
      // Attributes
      map = ele.getAttributes(new XPath("@*"));
      while (map.hasMoreElements()) {
        xmlAtt = map.next();
        an = xmlAtt.getName();
        val = xmlAtt.getValue();
        // ResourceBundle
        if (an.equals("label")) { // label -------------------------------------------
          keyword.title = val;
          if (val.length() > 0 && bundle != null) {
            try {
              String xval = bundle.getString(val);
              if (xval != null) {
                keyword.title = xval;
              }
            } catch (Exception ex) {
              //System.out.println("Warning! Missing Resource Key: "+val);
            }
          }
        }
        else if (an.equals("eol")) { // eol -------------------------------------
        	keyword.eol = Convert.toBoolean(val);
        }
        else if (an.equals("case")) { // case -------------------------------------
          keyword.version = val;
        }
        else { // weder label, eol, noch case -------------------------------------
          att = new CurrentAttrib();
          att.sKeyword=an+"=";
          att.iKeyword=GuiFactory.getKeywordInt(att.sKeyword);
          if (att.iKeyword == null) {
        	  String msg = "ParseException: Illegal Attribute: '"+att.sKeyword + "' in Element '" + ele.getName() + "'.";
        	  logger.error(msg);
        	  GuiWindow win = GuiSession.getInstance().getCurrentWindow();
        	  if (win != null) {
	            if (GuiUtil.okCancelMessage(win, "ParseException", msg) == false ) {
	              ret = null;
	              return;
	            }
        	  }
            continue;
          }
          att.setValue(val);
          switch (att.iKeyword.intValue()) {
            case GuiFactory.attRB: // rb -------------------------------------------
            		// ResourceBundle in Form definiert ?
            		try {
            			bundle = ResourceBundle.getBundle(val);
            		} catch (Exception e) {
            			System.err.print ("Warning! ");
            			e.printStackTrace();
            		}
            		break;
            case GuiFactory.attST: // st -------------------------------------------
              if (val.length() > 0 && bundle != null) {
                try {
                  att.setValue(bundle.getString(val));
                } catch (Exception ex) {
                  //System.out.println("Warning! Missing Resource Key: "+val);
                }
              }
              break;
            case GuiFactory.attTT: // tt -------------------------------------------
              if (val.length() > 0 && bundle != null) {
                try {
                  att.setValue(bundle.getString(val));
                } catch (Exception ex) {
                  //System.out.println("Warning! Missing Resource Key: "+val);
                }
              }
              break;
          }
          keyword.add(att);
        }
      } // End While (Attributes)
      // Row --> Items
      if (nn.equals("Row") || nn.equals("Column") || nn.equals("Script")) {
        String txt = ele.getTextString();
        // Es ist auch erlaubt, das Script in einem XML-Kommentar unterzubringen.
        if (nn.equals("Script")) {
          Node cnode = ele.getFirstChild();
          if (cnode instanceof Comment) {
            txt = ((Comment)cnode).getString();
          }
        }
        if (txt != null) {
          att = new CurrentAttrib();
          att.sKeyword = "Items=";
          att.iKeyword = GuiFactory.getKeywordInt(att.sKeyword);
          att.setValue(txt);
          keyword.add(att);
        }
      }
      makeEle(ele, ret);
      if (container == true) { // KeyWord End...
        CurrentKeyword endKeyword = new CurrentKeyword("End "+nn);
        endKeyword.iKeyword = GuiFactory.getKeywordInt("End "+nn);
        endKeyword.eol = keyword.eol;
        endKeyword.version = keyword.version;
        // Add to ArrayList
        ret.add(endKeyword);
      }
    } // Wend Elements
  }
  /**
   * Fügt ein weiteres Schlüsselwort für einen Container hinzu:
   * Begin Panel
   * ...
   * End Panel
   */
  static void addContainerTag(String name) {
  	if (contTags == null) {
  		contTags = new HashSet<String>(20);
  	}
  	contTags.add(name);
  }
  static {
    // Begin ... End Container
    contTags.add("Form");
    contTags.add("Dialog");
    contTags.add("Applet");
    contTags.add("Panel");
    contTags.add("Group");
    contTags.add("Table");
    contTags.add("Frame");
    contTags.add("Toolbar");
    contTags.add("Menubar");
    contTags.add("Menu");
    contTags.add("Split");
    contTags.add("Tabset");
    contTags.add("Tab");
    contTags.add("Tree");
    contTags.add("Folder"); // kann eigentlich raus
    //contTags.add("Node"); // statt Folder nur noch Node
    contTags.add("Popup");
    contTags.add("Element");
    contTags.add("OptionGroup");
    contTags.add("ButtonBar");
    contTags.add("ButtonBarButton");
    contTags.add("OutlookBar");
    contTags.add("OutlookBarTab");
    contTags.add("OutlookBarButton");
    contTags.add("TaskPane");
    contTags.add("TaskPaneTab");
    contTags.add("TaskPaneButton");
    contTags.add("Chart");
    contTags.add("Browser");
    contTags.add("JFX");
  }
}