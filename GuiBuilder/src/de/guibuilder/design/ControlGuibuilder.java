package de.guibuilder.design;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JRadioButtonMenuItem;

import de.guibuilder.framework.GDLParseException;
import de.guibuilder.framework.GuiApplet;
import de.guibuilder.framework.GuiAuthenticator;
import de.guibuilder.framework.GuiDialog;
import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiList;
import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiMemo;
import de.guibuilder.framework.GuiMenu;
import de.guibuilder.framework.GuiMenuBar;
import de.guibuilder.framework.GuiMenuItemOption;
import de.guibuilder.framework.GuiUtil;
import de.guibuilder.framework.GuiWindow;
import de.guibuilder.framework.event.GuiActionEvent;
import de.guibuilder.framework.event.GuiChangeEvent;
import de.guibuilder.framework.event.GuiKeyEvent;
import de.guibuilder.framework.event.GuiUserEvent;
import de.pkjs.util.Convert;
import electric.xml.Attribute;
import electric.xml.Attributes;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import electric.xml.XPath;

/**
 * Diese Klasse bildet den Controller für die Design-Umgebung des GuiBuilder selbst.
 */
public final class ControlGuibuilder {
  // Attributes
  private transient GuiWindow guibuilder; // Das Fenster des GuiBuilders
  private transient GuiWindow lastWindow; // Last Window
  private transient GuiDialog fontChooser;
  private String filename = ""; // Inhalt des DatenamensBox
  private String fileDialogDir; // Direktory merken für FileDialoge
  private String editorDefault = ""; // Defaultwert für Editor
  private GuiMemo editor; // ObjektReferenz auf den Editor

  private String search; // Letzter Suchbegriff
  private transient GuiDialog propEdit;
  private transient GuiDialog searchDialog;
  // Constructor
  ControlGuibuilder(String startWindow) {
    started(startWindow);
  }
  // Methods
  GuiDialog getPropEditor() {
    if (propEdit == null) {
      try {
        propEdit = (GuiDialog)GuiFactory.getInstance().createWindow(GuiUtil.getCodeBase()+"PropEditor.xml");
        propEdit.getRootPane().setDesignMode(false);
      } catch (GDLParseException ex) {
        GuiUtil.showEx(ex);
      }
    }
    return propEdit;
  }
  /**
   * Fügt einen Eintrag dem UnterMenü mit der Historie der Files hinzu.
   */
  private void addFileHistory(String directory, String fileName) {
    // History der geöffneten Files fortschreiben
    GuiMenuBar menubar = guibuilder.getRootPane().getGuiMenuBar();
    GuiMenu menu = menubar.getGuiMenu("recent");
    boolean isListed = false;
    int posi = 0;
    for (int i = 0; i < menu.getItemCount(); i++) {
      JRadioButtonMenuItem tmpItem = (JRadioButtonMenuItem)menu.getItem(i);
      int cmp = tmpItem.getText().compareToIgnoreCase(fileName);
      if (cmp < 0) {
        posi = i+1;
      }
      else if (cmp == 0) {
        isListed = true;
        break;
      }
      else {
      }
    }
    if (isListed == false) {
      GuiMenuItemOption newItem = new GuiMenuItemOption(fileName, menu);
      newItem.setName("history");
      newItem.setActionCommand(fileName);
      newItem.setMsgChange("changed_actionPerformed");
      newItem.setRef(directory);
      menu.insert((JRadioButtonMenuItem)newItem.getJComponent(), posi);
      menu.addOption(newItem);
      newItem.setSelected(true);
      if (menu.getItemCount() > 20) {
        menu.remove(20);
      }
    }
  }

  public void show_actionPerformed(GuiUserEvent event) {
    if (lastWindow != null) {
      lastWindow.dispose();
      lastWindow = null;
    }
    String source = guibuilder.getValue("editor").toString();
    try {
      lastWindow = GuiFactory.getInstance().createWindowXml(source, filename);
      lastWindow.show();
    } catch (GDLParseException ex) {
      GuiUtil.showEx(ex);
      editor.setLine(ex.getErrorLine());
      return;
    } catch (Exception ex) {
      GuiUtil.showEx(ex);
    }
//    System.out.print("Used Memory : ");
//    System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
//    System.out.print("Total Memory: ");
//    System.out.println(Runtime.getRuntime().totalMemory());
  }
  /*
  public void designModeMouseMoved(GuiUserEvent e) {
    GuiMouseMovedEvent event = (GuiMouseMovedEvent)e;
    GuiMember member = event.member;
    //System.out.println(member.getName());
    JComponent comp = member.getJComponent();
    int x = event.mouseEvent.getX();
    int y = event.mouseEvent.getY();
    int w = (int)comp.getSize().getWidth();
    int h = (int)comp.getSize().getHeight();
    Graphics g = comp.getGraphics();
    Color oldColor = g.getColor();
    comp.update(g);
    if (x <= 3) { // links
      comp.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
      g.setColor(Color.green);
      g.fillRect(0, 0, 2, h);
    } else if (x >= w -3) { // rechts
      comp.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
      g.setColor(Color.green);
      g.fillRect(w-2, 0, w, h);
    } else if (y <= 3) { // oben
      comp.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
      g.setColor(Color.green);
      g.fillRect(0, 0, w, 2);
    } else if (y >= h -3) { // unten
      comp.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
      g.setColor(Color.green);
      g.fillRect(0, h-2, w, h);
    } else {
      comp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
  }
  */
  public void new_actionPerformed(GuiUserEvent event) {
    if (editor.isModified()) {
      if (GuiUtil.okCancelMessage(guibuilder, "GuiBuilder", "Discard changes?") == false) {
        return;
      }
    }
    guibuilder.setValue("editor", editorDefault);
    guibuilder.setValue("filename", "");
    guibuilder.getAction("readButton").setEnabled(false);
    guibuilder.getAction("saveButton").setEnabled(false);
    guibuilder.getAction("saveItem").setEnabled(false);
    filename = "";
    editor.setModified(false);
    guibuilder.setTitle("GuiBuilder");
    // deprecated GuiUtil.clearWindowCache();
    if (lastWindow != null) {
      lastWindow.hide();
      lastWindow.dispose();
      lastWindow = null;
    }
    System.gc();
    System.out.print("Used Memory : ");
    System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    System.out.print("Total Memory: ");
    System.out.println(Runtime.getRuntime().totalMemory());
  }
  public void read_actionPerformed(GuiUserEvent event) {
    if (filename.length() == 0) {
      GuiUtil.showMessage(guibuilder, "Error", "Error", "Missing Filename!");
      return;
    }
    if (editor.isModified()) {
      if (GuiUtil.okCancelMessage(guibuilder, "GuiBuilder", "Discard changes?") == false) {
        return;
      }
    }
    String docBase;
    if (filename.equals("/")) {
      GuiUtil.resetDocumentBase();
      return;
    }
//    int posi = filename.lastIndexOf("/");
//    if (posi != -1) {
//      docBase = filename.substring(0, posi+1);
//      filename = filename.substring(posi+1);
//      GuiUtil.setDocumentBase(docBase);
//      System.out.println(docBase);
//      System.out.println(filename);
//      System.out.println(GuiUtil.getDocumentBase());
//    }
    try {
    	String s = GuiUtil.fileToString(filename);
      if (s != null) {
        guibuilder.setValue("filename", filename);
        guibuilder.setValue("editor", s);
        editor.getMemo().setCaretPosition(0);
        addFileHistory(null, filename);
        editor.setModified(false);
        guibuilder.setTitle("GuiBuilder - "+filename);
      }
    } catch (Exception ex) {
    	GuiUtil.showEx(ex);
    }
  }
  public void open_actionPerformed(GuiUserEvent event) {
    String ret[] = GuiUtil.fileOpenDialog(guibuilder, "Open File",
          fileDialogDir,
          GuiUtil.getConfig().getValuePath("@FileDialogDefaultExtension", "*.xml"));
    if (ret != null) {
      fileDialogDir = ret[1];
      filename = ret[0];
      try {
        GuiUtil.setDocumentBase(new URL("file", null, 0, fileDialogDir));
      }
      catch (Exception ex) {
        GuiUtil.showEx(ex);
      }
      if (fileDialogDir.startsWith(GuiUtil.getCurrentDir()+System.getProperty("file.separator")) == true
          && GuiUtil.getProtocol().equals("file"))
          {
        filename = filename.substring(GuiUtil.getCurrentDir().length()+1);
      }
      //System.out.println(filename);
      // File in Editor laden
      try {
      	String s = GuiUtil.fileToString(filename);
	      if (s != null) {
					addFileHistory(fileDialogDir, filename);
	        guibuilder.setValue("editor", s);
	        editor.getMemo().setCaretPosition(0);
	        guibuilder.setValue("filename", filename);
	        guibuilder.getAction("readButton").setEnabled(true);
	        guibuilder.getAction("saveButton").setEnabled(true);
	        guibuilder.getAction("saveItem").setEnabled(true);
	        editor.setModified(false);
	        guibuilder.setTitle("GuiBuilder - "+filename);
	      }
      } catch (Exception ex) {
      	GuiUtil.showEx(ex);
      }
    }
  }
  /**
   * Action "save"
   */
  public void save_actionPerformed(GuiUserEvent event) {
    String source = guibuilder.getValue("editor").toString();
    this.save(source);
  }
  /**
   * Save, Save As, Save XML (filename)
   * @param source Source to be saved
   */
  private void save(String source) {
    if (GuiUtil.getDocumentBase().getProtocol().equals("http")
        && GuiUtil.getHttpUsage() == GuiUtil.PUT
        && filename.indexOf(":") != 1)  {
    	PrintWriter out = null;
      try {
        Authenticator.setDefault(new GuiAuthenticator());
        URL url = new URL(GuiUtil.getDocumentBase(), filename);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setAllowUserInteraction(true);
        con.setDoOutput(true);
        con.setRequestMethod("PUT");

        out = new PrintWriter(con.getOutputStream());
        out.println(source);
        out.flush();
        out.close();
        System.out.println(con.getResponseCode()+" "+con.getResponseMessage());
        editor.setModified(false);
        guibuilder.setTitle("GuiBuilder - "+filename);
      }
      catch (Exception exc) {
        GuiUtil.showEx(exc);
      }
      finally {
      	if (out != null) {
      		try {
      			out.close();
      		} catch (Exception ex) {}
      	}
      }
    }
    else { // Protocoll: *** file ****************************************
      File f;
      if (filename.indexOf(":") == 1  || (filename.charAt(0)=='/')) { // absolute Pfadangabe ) 
        f = new File (filename);
      }
      else { // relative Pfadangabe
        f = new File (GuiUtil.getCurrentDir(), filename);
      }
      try {
        if (f.canWrite() == false && f.createNewFile() == false) {
          GuiUtil.showMessage(guibuilder, "Error!", "Error", "File is write protected!");
          return;
        }
        FileWriter fw = new FileWriter(f);
        fw.write(source);
        fw.flush();
        fw.close();
        editor.setModified(false);
        guibuilder.setTitle("GuiBuilder - "+filename);
      }
      catch (Exception exc) {
        GuiUtil.showEx(exc);
      }
    }
  }

  /**
   * Menu "Save As".
   */
  public void saveAs_actionPerformed(GuiUserEvent event) {
    String ret[] = GuiUtil.fileSaveDialog(guibuilder, "Save File As",
          GuiUtil.getCurrentDir(),
          this.filename);
    if (ret != null) {
      String tmpFileName = ret[0];
      if (ret[1].startsWith(GuiUtil.getCurrentDir()+System.getProperty("file.separator"))==true
            && GuiUtil.getProtocol().equals("file")) {
        this.filename = tmpFileName.substring(GuiUtil.getCurrentDir().length()+1);
      } else {
        this.filename = tmpFileName;
      }
      guibuilder.setValue("filename", this.filename);
      String source = guibuilder.getValue("editor").toString();
      this.save(source);
		  guibuilder.getAction("saveButton").setEnabled(true);
		  guibuilder.getAction("saveItem").setEnabled(true);
    }
  }
  public void formatXML_actionPerformed(GuiUserEvent event) {
    String source = guibuilder.getValue("editor").toString();
    try {
      boolean modi = editor.isModified();
      Document doc = new Document(source);
      source = doc.toString();
      guibuilder.setValue("editor", source);
      editor.setModified(modi);
    } catch (ParseException ex) {
      GuiUtil.showEx(ex);
    }
  }
  /**
   * CTRL-F
   */
  public void search_actionPerformed(GuiUserEvent event) {
    search(false);
  }
  /**
   * F3
   */
  public void repeat_actionPerformed(GuiUserEvent event) {
    search(true);
  }
  /**
   * Menu "Search".
   */
  private void search(boolean repeat) {
    if (searchDialog == null) {
      this.createSearchDialog();
    }
    if (repeat) {
      search(search);
    } else {
      searchDialog.setValue("find", search);
      searchDialog.getGuiComponent("find").requestFocus();
      if (searchDialog.showDialog() ) {
        String s = (String)searchDialog.getValue("find");
        guibuilder.getAction("repeat").setEnabled(search(s));
      }
    }
  }
  private boolean search(String s) {
    boolean ret = false;
    if (s != null) {
      search = s;
      int offs = editor.getMemo().getCaretPosition();
      int p = editor.getText().toLowerCase().substring(offs).indexOf(s.toLowerCase());
      if (p != -1) {
        editor.getMemo().setSelectionStart(p+offs);
        editor.getMemo().setSelectionEnd(p+offs+s.length());
        ret = true;
      }
    }
    return ret;
  }
  private void createSearchDialog() {
    try {
      searchDialog = (GuiDialog)GuiFactory.getInstance().createWindowXml(
        "<?xml version='1.0' encoding='UTF-8'?>"
        +"<GDL>"
        +"<Dialog rb='GuiBundle' label='InputKey' w='300' h='150' type='MODAL_NORESIZE'>"
          +"<Label label='FindKey' il='10'/>"
          +"<Text name='find' il='10' ir='10' ib='10' w='2'/>"
          +"<Button label='OK' eol='false' name='ok' cmd='ok' px='30' il='10' an='E'/>"
          +"<Button label='CancelKey' name='cancel' an='W'/>"
        +"</Dialog>"
        +"</GDL>");
    } catch (GDLParseException ex) {
      GuiUtil.showEx(ex);
    }
  }
  /**
   * Generiert eine HTML-Dokumentation des Dialoges
   */
  public void guiDoc_actionPerformed(GuiUserEvent event) {
  		if (editor.isModified()) {
  			save_actionPerformed(event);
  		}
  		//ControlGuidoc ctrl = 
  		new ControlGuidoc(this.filename);
  }
  
  /**
   * Ruft einen Dialog zur Eingabe der internen Eigenschaften auf.
   */
  /*public void proper_actionPerformed(GuiUserEvent event) {
    try {
    	String name = GuiUtil.getCodeBase() + "GuiBuilderConfigEditor.xml";
      GuiWindow dia = GuiFactory.getInstance().createWindow(name);
      //dia.getAction("load").click();
      dia.show();
      GuiUtil.loadGuiPropXml(); // geänderte Einträge neu laden
    } catch (Exception ex) {
      GuiUtil.showEx(ex);
    }
  }*/
	public void proper_actionPerformed(GuiUserEvent event) {
		ControlEditProperties propedit = new ControlEditProperties();
		propedit.formLoad(null);
	}  
  /*
  private void generateDtd_actionPerformed() {
    this.show_actionPerformed();
    if (window == null) return;
    // File erzeugen
    File f = new File("xxx.dtd");
    try {
      FileOutputStream out = new FileOutputStream(f);
      makeContainer(window.getMainPanel(), out);
      out.flush();
      out.close();
    } catch (Exception ex) {
      GuiUtil.showEx(ex);
    }
  }

  private void makeContainer(GuiContainer cont, OutputStream out) {
    ArrayList memberNames = cont.getMemberNames();
    HashMap members = cont.getMembers();
    GuiMember member = null;
    String name = null;
    String fullName = null;
    StringBuffer buf = new StringBuffer("<!ENTITY ");
    //buf.append(cont.getName());
    buf.append(cont.getFullName());
    buf.append(" (");
    // 1. ENTITY of Container
    for (int i = 1; i<=memberNames.size(); i++) {
      name = (String)memberNames.get(i-1);
      member = (GuiMember)members.get(name);
      buf.append(member.getFullName());
      if (i < memberNames.size()) {
        buf.append(", ");
      }
    }
    buf.append(")>\n");
    try {
      out.write(buf.toString().getBytes());
      // 2. ENTITY of Members
      for (int i = 0; i<memberNames.size(); i++) {
        name = (String)memberNames.get(i);
        member = (GuiMember)members.get(name);
        fullName = member.getFullName();
        if (member.getGuiType() == GuiMember.GUI_COMPONENT) {
          buf = new StringBuffer("<!ENTITY ");
          buf.append(fullName);
          buf.append(" #PCDATA>\n");
          out.write(buf.toString().getBytes());
        } else if (member.getGuiType() == GuiMember.GUI_CONTAINER) {
          makeContainer((GuiContainer)member, out);
        } else if (member.getGuiType() == GuiMember.GUI_TABLE) {
          buf = new StringBuffer("<!ENTITY ");
          buf.append(fullName);
          buf.append(" (");
          buf.append(fullName);
          buf.append("_Row*");
          buf.append(")>\n");
          out.write(buf.toString().getBytes());
          // Table Row
          buf = new StringBuffer("<!ENTITY ");
          buf.append(fullName);
          buf.append("_Row");
          buf.append(" (");
          // Table-Columns
          GuiTable tbl = (GuiTable)member;
          Vector cols = tbl.getColumnIdentifiers();
          String col = null;
          for (int j = 1; j<=cols.size(); j++) {
            col = (String)cols.get(j-1);
            buf.append(fullName);
            buf.append(".");
            buf.append(col);
            if (j < cols.size()) {
              buf.append(", ");
            }
          }
          buf.append(")>\n");
          out.write(buf.toString().getBytes());
          // Col-Entitiey
          for (int j = 0; j<cols.size(); j++) {
            col = (String)cols.get(j);
            buf = new StringBuffer("<!ENTITY ");
            buf.append(fullName);
            buf.append(".");
            buf.append(col);
            buf.append(" #PCDATA>\n");
            out.write(buf.toString().getBytes());
          }
        }
      }
    } catch (Exception ex) {
      GuiUtil.showEx(ex);
    }
  }
  */
  /**
   * Aufruf Property-Editor auf Feldebene.
   */
  public void fieldProp_actionPerformed(GuiUserEvent event) {
    getPropEditor();
    final int curpos = editor.getMemo().getCaretPosition();
    final String val = editor.getText();
    final int anf = val.lastIndexOf('<', curpos);
    final int end1 = val.indexOf('>', anf);
    final int end2 = val.indexOf("/>", anf);
    int end = Math.min(end1, end2);
    if (end2 == -1) end = end1;
    final String item = val.substring(anf, end).trim();
    //System.out.println(item);
    final StringBuffer buf = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?>");
    buf.append("<Panel name='mainPanel'>");
    buf.append(item);
    buf.append("/>");
    buf.append("</Panel>");
    try {
      //final String tmp = buf.toString();
      final Document doc = new Document(buf.toString());
      final Element root = doc.getRoot();
      final Element xitem = root.getElementAt(1);
      root.removeElementAt(1);
      Element ele = root.addElement("Component");
      ele.setAttribute("name", "class");
      final String keyword = xitem.getName();
      ele.setText(keyword);
      final Attributes map = xitem.getAttributes(new XPath("@*"));
      while (map.hasMoreElements()) {
        Attribute att = map.next();
        ele = root.addElement("Component");
        ele.setAttribute("name", att.getName());
        ele.setText(att.getValue());
      }
      //System.out.println(doc);
      propEdit.reset();
      final HashSet<String> components = GuiUtil.getKeywordAttributes(keyword);
      // Nur die Enablen, die in KeywordAttributes.properties aufgeführt sind
      propEdit.getMainPanel().setEnabled(components, true);
      // Werte aus Editor zuweisen
      propEdit.setAllValuesXml(doc);
      if (propEdit.showDialog() ) {
        // Eingegebene Daten
        final Document ret = propEdit.getAllValuesXml();
        //System.out.println(ret);
        final Element panel = ret.getRoot();
        final Elements comps = panel.getElements();
        StringBuffer repl = new StringBuffer();
        repl.append("<");
        while (comps.hasMoreElements() ) {
          Element rele = comps.next();
          String att = rele.getAttributeValue("name");
          String rval = rele.getTextString();
          // Widget
          if (att.equals("class")) {
            repl.append(rval);
            repl.append(" ");
          } else {
            // Default-Werte nicht setzen
            if (att.equals("tabstop") && rval.equals("true")) {
              continue;
            } else if (att.equals("visible") && rval.equals("true")) {
              continue;
            } else if (att.equals("eol") && rval.equals("true")) {
              continue;
            } else if (att.equals("do") && rval.equals("false")) {
              continue;
            } else if (att.equals("nn") && rval.equals("false")) {
              continue;
            } else if (att.equals("search") && rval.equals("false")) {
              continue;
            } else if (att.equals("invert") && rval.equals("false")) {
              continue;
            } else if (att.equals("nodeTitle") && rval.equals("false")) {
              continue;
            }
            repl.append(att);
            repl.append("='");
            repl.append(rval);
            repl.append("' ");
          }
        }
        repl = repl.deleteCharAt(repl.length()-1);
        //System.out.println(repl);
        StringBuffer old = new StringBuffer(val);
        StringBuffer erg = old.replace(anf, end, repl.toString());
        editor.setValue(erg.toString());
        editor.getMemo().setCaretPosition(curpos);
      }
    } catch (Exception ex) {
      GuiUtil.showEx(ex);
    }
  }

  /**
   * Menü "Help"; Zeigt die GDL-Sprachdefinition "GDL_Defi.html".
   */
  public void help_actionPerformed(GuiUserEvent event) {
    try {
      GuiUtil.showHelp(new URL(GuiUtil.getCodeBase(), "../IndexFrame.html"), "GuiBuilder - Help");
    }
    catch (Exception ex) {
      GuiUtil.showEx(ex);
    }
  }
  /**
   * Zeigt die About-Box.
   */
  public void about_actionPerformed(GuiUserEvent event) {
    guibuilder.cursorWait();
    URL base = GuiUtil.getDocumentBase();
    GuiUtil.setDocumentBase(GuiUtil.getCodeBase());
    try {
    	GuiWindow aboutWin = GuiFactory.getInstance().createWindow("GuiBuilderAbout.xml");
    	aboutWin.show();
    } catch (Exception ex) {
    	GuiUtil.showEx(ex);
    }
    GuiUtil.setDocumentBase(base);
    guibuilder.cursorDefault();
  }

  public void example_actionPerformed(GuiUserEvent event) {
    if (editor.isModified()) {
      if (GuiUtil.okCancelMessage(guibuilder, "GuiBuilder", "Discard changes?") == false) {
        return;
      }
    }
    guibuilder.cursorWait();
    GuiUtil.setDocumentBase("tutorial/");
    guibuilder.setValue("filename", "BeispielFormLayout.xml");
    filename = "BeispielFormLayout.xml";
    read_actionPerformed(event);
    show_actionPerformed(event);
    guibuilder.cursorDefault();
  }

  /**
   * Menü "Exit" wenn GuiBuilder als Application läuft bzw. Applet beenden.
   */
  public void exit_actionPerformed(GuiUserEvent event) {
    if (editor.isModified()) {
      if (GuiUtil.okCancelMessage(guibuilder, "GuiBuilder", "Discard changes?") == false) {
        return;
      }
    }
    GuiUtil.showImageCacheHits();
    if (GuiUtil.isApplet() == false) {
      System.exit(0);
    } else {
      // wie nur?
      GuiUtil.getApplet().dispose();
    }
  }
  public void verify_actionPerformed(GuiUserEvent event) {
    XmlVerifier veri = XmlVerifier.getInstance();
    guibuilder.cursorWait();
    int errLine = veri.verifyFromString(guibuilder.getValue("editor").toString());
    if (errLine > -1) {
      editor.setLine(errLine);
    } else {
      System.out.println("Verified");
    }
    guibuilder.cursorDefault();
  }

	/**
	 * Initialisierung des GuiDesigner-Fensters.
	 * Es werden die Ressourcen f&uuml;r die jeweilige Standardsprache geladen,
	 * sowie der Controller f&uuml;r die Ereignisbehandlung gesetzt.<br>
	 * <i>Initializes the GuiDesigner window.
	 * Loads the ressources for the current default local
	 * and sets the controller for the event handling.</i>
	 * 
	 * @param startWindow Name der GDL-Definitionsdatei f&uuml;r das GuiDesigner-Fenster.
	 * <i>Name of the GDL definition file for the GuiDesign window.</i>
	 */
	private void started(String startWindow) {
	// Das ist hier irgendwie Mist:
	// Der Guibuilder selbst sollte sich aus dem guibuilder.jar
	// bedienen wenn irgend möglich.
		try {
			guibuilder = GuiFactory.getInstance().createWindow(startWindow);
		    	guibuilder.cursorWait();
		} catch (Exception ex) {
			try {
				URL url = this.getClass().getClassLoader().getResource(startWindow);
				if (url != null && startWindow.toLowerCase().endsWith(".xml")) {
					Document doc = new Document(url.openStream());
					guibuilder = GuiFactory.getInstance().createWindow(doc);
				}
				else {
					throw ex;
				}
		    	} catch (Exception uex) {
		    		uex.printStackTrace();
		    		if (ex != uex) {
		    			System.err.print("\nroot cause: ");
		    			ex.printStackTrace();
		    		}
		    		System.exit(1);
		    	}
		}
		guibuilder.setDefaultCloseOperation(GuiWindow.NOTHING);
		guibuilder.setController(this);
		guibuilder.getRootPane().setDesignMode(false);
		this.fillResources();
		GuiUtil.setUiManager(guibuilder.getValue("ui").toString());
		// prüfen, ob GuiBuilder als Applet läuft
		// wozu das??? GuiBuilder erzeugt sich selbst!
		// Das Ergebnis hiervon (GuiBuilder als Applet) ist ziemlicher Mist:
		// Wir haben jetzt zwei Fenster, die auf das selbe RootPane zeigen!
		if (GuiUtil.getApplet() instanceof de.guibuilder.framework.GuiApplet) {
			GuiApplet applet = GuiUtil.getApplet();
			applet.setRootPane(guibuilder.getRootPane());
		} else {
			guibuilder.show();
		}
		editorDefault = GuiUtil.getConfig().getValuePath("@EditorDefault");
		guibuilder.setValue("editor", editorDefault);
		// UI-Manager auf den ersten Eintrag der Combobox stellen
		fileDialogDir = GuiUtil.getCurrentDir();
		editor = (GuiMemo)guibuilder.getGuiComponent("editor");
		guibuilder.cursorDefault();
		//********************
		System.out.print("Used Memory : ");
		System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		System.out.print("Total Memory: ");
		System.out.println(Runtime.getRuntime().totalMemory());
	}
  //************** Font
  public void fontAction(GuiUserEvent event) {
    if (fontChooser == null) {
      try {
        fontChooser = (GuiDialog)GuiFactory.getInstance().createWindow(GuiUtil.getCodeBase()+"FontChooser.xml");
      } catch (Exception ex) {
        GuiUtil.showEx(ex);
        return;
      }
      fontChooser.setController(this);
      Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
      Vector<String> set = new Vector<String>();
      GuiList list = (GuiList)fontChooser.getRootPane().getMainPanel().getMember("font");
      for (int i = 0; i< fonts.length; i++) {
        StringTokenizer toks = new StringTokenizer(fonts[i].getFontName(), " .");
        String tok = toks.nextToken();
        if (fonts[i].canDisplay('�') == false) continue;
        if (fonts[i].canDisplay('<') == false) continue;
        set.add(tok);
        list.addItem(fonts[i].getFontName());
      }
    }
    Font oldFont = editor.getFont();
    fontChooser.setValue("font", oldFont.getFontName());
    fontChooser.setValue("style", GuiFactory.getStyle(oldFont.getStyle()));
    fontChooser.setValue("size", Integer.toString(oldFont.getSize()));
    if ((fontChooser).showDialog()) {
      GuiUtil.getConfig().setValuePath(".Font@name", editor.getFont().getFontName());
      GuiUtil.getConfig().setValuePath(".Font@style", GuiFactory.getStyle(editor.getFont().getStyle()));
      GuiUtil.getConfig().setValuePath(".Font@size", editor.getFont().getSize());
    } else {
      editor.setFont(oldFont);
    }
  }
  public void fontChanged(GuiUserEvent event) {
    GuiChangeEvent e = (GuiChangeEvent)event;
    editor.setFont(new Font((String)e.value, editor.getFont().getStyle(), editor.getFont().getSize()));
  }
  public void styleChanged(GuiUserEvent event) {
    GuiChangeEvent e = (GuiChangeEvent)event;
    editor.setFont(new Font(editor.getFont().getFontName(),
      GuiFactory.getStyle(e.value.toString()),
      editor.getFont().getSize()));
  }
  public void sizeChanged(GuiUserEvent event) {
    GuiChangeEvent e = (GuiChangeEvent)event;
    editor.setFont(new Font(editor.getFont().getFontName(), editor.getFont().getStyle(), Convert.toInt(e.value.toString())));
  }
  public void fontSelected(GuiUserEvent event) {
    GuiActionEvent e = (GuiActionEvent)event;
    String fontName = e.action.getText();
    editor.setFont(new Font(fontName, editor.getFont().getStyle(), editor.getFont().getSize()));
  }
  // ********** End Font
  private void fillResources() {
    GuiMenu main = guibuilder.getRootPane().getGuiMenuBar().getGuiMenu("resources");
    if (main == null) return;
    {
      GuiMenu res = main.getGuiMenu("defaultResource");
      String s = GuiUtil.fileToString("Resources.lst");
      if (s == null) {
        return;
      }
      StringTokenizer tokens = new StringTokenizer(s,"\n");
      while (tokens.hasMoreTokens()) {
        String sTok=tokens.nextToken().trim();
        try  {
          if (sTok.length() > 0 && sTok.startsWith("#") == false) {
            GuiMenuItemOption item = new GuiMenuItemOption(sTok, res);
            item.setName("resource");
            item.setMsgChange("changed_actionPerformed");
            res.addOption(item);
          }
        } catch (Exception ex) {
          // nix machen, wenn in GuiBuilder.xml das Menü fehlt
        }
      }
    }
    // Locale
    {
      GuiMenu loc = main.getGuiMenu("currentLocale");
      String s = GuiUtil.fileToString("Locales.lst");
      if (s == null) {
        return;
      }
      StringTokenizer tokens = new StringTokenizer(s,"\n");
      while (tokens.hasMoreTokens()) {
        String sTok=tokens.nextToken().trim();
        try  {
          if (sTok.length() > 0) {
            GuiMenuItemOption item = new GuiMenuItemOption(sTok, loc);
            item.setName("locale");
            item.setMsgChange("changed_actionPerformed");
            loc.addOption(item);
          }
        } catch (Exception ex) {
          // nix machen, wenn in GuiBuilder.xml das Menü fehlt
        }
      }
    }
  }
  public void fileNameTyped(GuiUserEvent e) {
    GuiKeyEvent event = (GuiKeyEvent)e;
    String value = event.component.getValue().toString();
    //System.out.println(event.ke.getKeyChar());
    //System.out.println(Character.isDefined(event.ke.getKeyChar()));
    if (value.length() == 0) {
      guibuilder.getAction("readButton").setEnabled(false);
      guibuilder.getAction("saveButton").setEnabled(false);
      guibuilder.getAction("saveItem").setEnabled(false);
    } else {
      guibuilder.getAction("readButton").setEnabled(true);
      guibuilder.getAction("saveButton").setEnabled(true);
      guibuilder.getAction("saveItem").setEnabled(true);
    }
  }
  /**
   * Der Benutzer hat den Inhalt einer Komponente verändert.
   * Dieses sind alle Komponenten, die Daten halten können,
   * also TextField, ComboBox, ListBox, RadioButton, Scrollbar, usw.<br>
   * Diese Nachricht wird nur ausgelöst, wenn sich der Inhalt
   * der Komponente tatsächlich geändert hat;
   * bei dem TextField nach Anderung der Eingabe beim FocusVerlust,
   * bei der ComboBox, CheckBox usw. nach dem Anklicken eines anderen Wertes.
   */
  public void changed_actionPerformed(GuiUserEvent e) {
    GuiChangeEvent event = (GuiChangeEvent)e;
    String name = event.component.getName();
    String value = event.component.getValue().toString();

    // Debug.Modus setzen
    if (name.equals("debug")) {
      GuiUtil.setDebug(value);
      if (lastWindow != null) {
        // Method Testing
        if (GuiUtil.getDebug() == true) {
          lastWindow.setController(new Object());
        } else {
          lastWindow.setController(null);
        }
      }
    }
    // Dateiname geändert?
    else if (name.equals("filename")) {
      filename = value;
    }
    // DesignMode?
//    else if (name.equals("designMode")) {
//      GuiPanelImpl.setShowGrid(event.bValue);
//      if (event.bValue) {
//        GuiPanelImpl.memberPropController = new MemberPropController(this.getPropEditor());
//      } else {
//        GuiPanelImpl.memberPropController = null;
//      }
//    }
    // UI-Manager
    else if (name.equals("ui")) {
      GuiUtil.setUiManager(value);
    }
    // Version
    else if (name.equals("version")) {
      GuiUtil.setVersion(value);
    }
    // Menu "Recent"
    else if (name.equals("history")) {
      if (editor.isModified()) {
      if (GuiUtil.okCancelMessage(guibuilder, "GuiBuilder", "Discard changes?") == false) {
          return;
        }
      }
      GuiMember mem = guibuilder.getGuiMember("history");
      if (mem.getRef() != null) {
        try {
          GuiUtil.setDocumentBase(new URL("file", null, 0, mem.getRef()) );
        } catch (MalformedURLException ex) {
          GuiUtil.showEx(ex);
        }
      }
      guibuilder.setValue("filename", value);
      filename = value;
      read_actionPerformed(event);
    }
    // Menu "Pattern"
    else if (name.equals("pattern")) {
      guibuilder.setValue("filename", value);
      filename = value;
      read_actionPerformed(event);
    }
    else if (name.equals("resource")) {
      if (value.equals("null")) {
        GuiUtil.setDefaultResourceBundle(null);
      } else {
        GuiUtil.setDefaultResourceBundle(value);
      }
    }
    else if (name.equals("locale")) {
      GuiUtil.setLocale(value);
    }
  }
  public void checkNamesChanged(GuiUserEvent e) {
    GuiChangeEvent event = (GuiChangeEvent)e;
    GuiUtil.setAPI(event.bValue);
  }
  public void apiChanged(GuiUserEvent e) {
    GuiChangeEvent event = (GuiChangeEvent)e;
    GuiUtil.setAPI(event.bValue);
  }
}
