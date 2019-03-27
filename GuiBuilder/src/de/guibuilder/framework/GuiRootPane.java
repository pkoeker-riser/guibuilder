package de.guibuilder.framework;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import de.guibuilder.adapter.UserAccessChecker;
import de.guibuilder.framework.event.GuiActionEvent;
import de.guibuilder.framework.event.GuiChangeEvent;
import de.guibuilder.framework.event.GuiClickEvent;
import de.guibuilder.framework.event.GuiCreateEvent;
import de.guibuilder.framework.event.GuiDblClickEvent;
import de.guibuilder.framework.event.GuiDragEvent;
import de.guibuilder.framework.event.GuiDropEvent;
import de.guibuilder.framework.event.GuiFileDropEvent;
import de.guibuilder.framework.event.GuiKeyEvent;
import de.guibuilder.framework.event.GuiLostFocusEvent;
import de.guibuilder.framework.event.GuiMessageBoxEvent;
import de.guibuilder.framework.event.GuiMouseMovedEvent;
import de.guibuilder.framework.event.GuiMouseOverEvent;
import de.guibuilder.framework.event.GuiPopupEvent;
import de.guibuilder.framework.event.GuiTabSelectionEvent;
import de.guibuilder.framework.event.GuiTableEvent;
import de.guibuilder.framework.event.GuiTreeNodeSelectionEvent;
import de.guibuilder.framework.event.GuiUserEvent;
import de.guibuilder.framework.event.GuiWindowEvent;
import de.jdataset.JDataSet;
import electric.xml.Document;
import electric.xml.Element;

/**
 * Von JRootPane abgeleitete Klasse, die die Aufgaben eines Hauptfensters (Formular,
 * Dialog, Applet, InternalFrame) übernimmt.
 * <P>
 * Der Vorteil dieser Klasse besteht darin, daß sie von (fast!) allen Komponenten über die
 * Methode getRootPane() erreichbar ist.
 * <P>
 * Hier werden alle Fenster-bezogenen Aufgaben erledigt; alle Container-bezogenen Methoden
 * werden an das MainPanel delegiert.
 * <UL>
 * <li>GuiRootPane
 * <ul>
 * <li>JLayeredPane
 * <UL>
 * <li>GuiMenuBar
 * <li>JPanel (contentPane, BorderLayout)
 * <ul>
 * <li>GuiToolBar (NORTH)
 * <li>GuiPanel mainPanel (CENTER)
 * <UL>
 * <li>alle weiteren Komponenten...
 * </UL>
 * <li>GuiStatusBar (SOUTH)
 * </ul>
 * </UL>
 * </UL>
 * </UL>
 * 
 * @since 0.8a
 * @see GuiContainer
 * @see GuiPanel
 */
public final class GuiRootPane extends JRootPane {
  private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GuiRootPane.class);
	/**
	 * Verweis auf das Fenster (Formular oder Dialog) dieses Objektes.
	 * 
	 * @see #getParentWindow
	 */
	private GuiWindow parentWindow;
	/**
	 * Verweis auf die Toolbar des Fensters.
	 */
	private GuiToolbar toolBar;
	private JPanel toolBarPanel;
	/**
	 * Verweis auf das HauptPanel; hier werden alle Komponenten angehängt.
	 * 
	 * @see #getMainPanel
	 */
	private GuiPanel mainPanel = new GuiPanel("mainPanel");
	/**
	 * Verweis auf die Statuszeile zu diesem Objekt.
	 * 
	 * @see #getStatusBar
	 */
	private GuiLabel statusBar;
	/**
	 * Der HelpTopic des Fensters.
	 */
	private String helpTopic;
	/**
	 * Verweis auf die zuletzt aktivierte Tabelle.
	 * 
	 * @see #getCurrentTable
	 */
	private GuiTable currentTable;
	/**
	 * Verweis auf den zuletzt aktivierten Navigator.
	 */
	private GuiTree currentTree;
	/**
	 * Zuletzt aktivierter Tabset.
	 */
	private GuiTabset currentTabset;
	// File-Dialog-Attribute
	/**
	 * Verweis auf das zuletzt von einem FileDialog verwendete Directoy.
	 */
	private String currentDir;
	/**
	 * Verweis auf die zuletzt von einem FileDialog verwendete Datei.
	 */
	private String fileName;
	/**
	 * Verweis auf ein ggf. vorhandenes Splitpanel.
	 */
	private GuiSplit currentSplit;
	/**
	 * Kennzeichen, ob sich der Inhalt durch Benutzeraktionen verändert hat.
	 */
	private boolean modified;
	/**
	 * Wert des Attributes "ref=" aus Gui-Spezifikation bei Form, Dialog, Applet. für
	 * Datenbankanwendungen o.ä. kann hier ein beliebiger Eintrag abgelegt werden. <br>
	 * Default ist null.
	 */
	private String attRef;
	/**
	 * Die Object-Id des von der Oberfläche gehaltenen Datenbank-Objekts. Default ist null.
	 * Dieser Wert wird auch von der Methode <code>reset()</code> auf null gesetzt;
	 * Ansonsten können Datenbankanwendungen hier eine beliebige Objekt-Id ablegen.
	 */
	private String soid;
	private boolean designMode = true;

	// Constructor
	/**
	 * @param parent
	 *           Verweis auf das hierzu gehörige Applet, Formular, Dialog oder
	 *           InternalFrame
	 * @see GuiWindow#setRootPane
	 */
	GuiRootPane(GuiWindow parent) {
		super();
		this.parentWindow = parent;
		this.guiInit(parent.getName());
	}

	// Methods
	/**
	 * Diese Initialisierung wird von allen Constructoren aufgerufen. Es wird das
	 * Hauptpanel gesetzt.
	 */
	private void guiInit(String name) {
		this.getContentPane().setName(name + "_contentPane");
		this.getContentPane().add(mainPanel.getJComponent(), BorderLayout.CENTER);
		this.setName(name + "_rootpane");
	}

	/**
	 * Setzt den Pnuts Context
	 * 
	 * @see GuiFactory#perfScript
	 */
	final void setContext(GuiScripting c) {
		this.getMainPanel().setContext(c);
	}

	final GuiScripting getContext() {
		return this.getMainPanel().getContext();
	}

	public final boolean hasDesignMode() {
		return designMode;
	}

	public final void setDesignMode(boolean b) {
		this.designMode = b;
	}

	/**
	 * Liefert die MenuBar.
	 * <P>
	 * Wenn bisher kein MenuBar verwendet wurde, wird jetzt einer angelegt.
	 * <p>
	 * Delegation an das protected Attribut "menuBar" in JRootPane.
	 */
	public GuiMenuBar getGuiMenuBar() {
		if (menuBar == null) {
			menuBar = new GuiMenuBar();
			this.setJMenuBar(menuBar);
		}
		return (GuiMenuBar) menuBar;
	}

	/**
	 * Liefert "true", wenn sich der Inhalt des Fensters durch Benutzereingaben verändert
	 * hat.
	 * 
	 * @see #obj_ItemChanged
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Kennzeichnet das Fenster als durch Benutzereingaben geändert. <br>
	 * Modified wird von allen Komponenten hier auf true gesetzt, wenn sie sich geändert
	 * haben. Ein zurücksetzen erfolgt in der Regel nur durch reset.
	 * 
	 * @see GuiComponent#setModified
	 * @see #reset
	 */
	public void setModified(boolean b) {
		modified = b;
	}

	/**
	 * Liefert das Hauptfenster.
	 */
	public GuiWindow getParentWindow() {
		return parentWindow;
	}

	/**
	 * Setzt den Parent neu
	 * 
	 * @see GuiWindow#replaceRootPane
	 */
	void setParentWindow(GuiWindow w) {
		this.parentWindow = w;
	}

	/**
	 * Liefert die ToolBar.
	 * <P>
	 * Wenn bisher keine ToolBar verwendet wurde, wird jetzt eine angelegt.
	 */
	/*
	 * public GuiToolbar getToolBar() { if (toolBar == null) { toolBar = new
	 * GuiToolbar("toolbar"); this.getContentPane().add(toolBar, BorderLayout.NORTH); }
	 * return toolBar; }
	 */
	boolean hasToolBar() {
		if (toolBar == null)
			return false;
		else
			return true;
	}

	/**
	 * Fügt dem Fenster eine Toolbar hinzu.
	 */
	public void addToolBar(GuiToolbar tb) {
		if (toolBarPanel == null) {
			FlowLayout flow = new FlowLayout(FlowLayout.LEFT);
			flow.setHgap(0);
			flow.setVgap(0);
			toolBarPanel = new JPanel(flow);

			this.getContentPane().add(toolBarPanel, BorderLayout.NORTH);
		}
		toolBarPanel.add(tb);
		if (toolBar == null)
			toolBar = tb;
	}

	/**
	 * Liefert das HauptPanel als Wurzel für alle Komponenten außer Menü, Toolbar und
	 * StatusBar.
	 * <p>
	 * Wirft eine IllegalStateException, wenn mainPanel unzulässiger Weise null sein
	 * sollte; vermußlich wurde zuvor dispose() aufgerufen.
	 */
	public GuiPanel getMainPanel() {
		if (mainPanel == null) {
			throw new IllegalStateException("MainPanel is NULL!");
		}
		return mainPanel;
	}

	/**
	 * Liefert die StatusBar für die Anzeige von Statustexten.
	 * <P>
	 * Wenn bisher keine StatusBar verwendet wurde, wird jetzt eine angelegt.
	 */
	public GuiLabel getStatusBar() {
		if (statusBar == null) {
			statusBar = new GuiStatusBar();
			statusBar.setText(" ");
			this.getContentPane().add(statusBar.getJComponent(), BorderLayout.SOUTH);
		}
		return statusBar;
	}

	/**
	 * Setzt die Statuszeile auf das gewünschte Label.
	 */
	public void setStatusBar(GuiLabel s) {
		GuiText dummy = new GuiText();
		dummy.setEnabled(false);
		s.getJComponent().setBorder(dummy.getJComponent().getBorder());
		s.setPreferredSize(dummy.getPreferredSize());
		s.setBackground(new Color(230, 230, 230));
		s.setHorizontalAlignment(SwingConstants.LEFT);
		statusBar = s;
	}


	/**
	 * Zeigt den übergebenen Statustext an.
	 * <P>
	 * Wenn noch keine Statusbar vorhanden, wird hiermit eine angelegt.
	 */
	public void setHint(String text) {
		if (text != null) {
			if (text.length() > 1 || (text.equals(" ") && this.statusBar != null)) {
				this.getStatusBar().setText(text);
			}
		}
	}

	/**
	 * Liefert die zuletzt angeklickte Tabelle oder null, wenn keine vorhanden oder noch
	 * nicht angeklickt wurde.
	 */
	public GuiTable getCurrentTable() {
		return this.currentTable;
	}

	/**
	 * Setzt die zuletzt angeklickte Tabelle.
	 */
	void setCurrentTable(GuiTable tbl) {
		this.currentTable = tbl;
	}

	/**
	 * Liefert die aktuelle Tree-Komponete oder null, wenn keine vorhanden. <BR>
	 * für Navigator.
	 */
	public GuiTree getCurrentTree() {
		return this.currentTree;
	}

	/**
	 * Setzt die aktuelle Tree-Komponente. <BR>
	 * für Navigator.
	 */
	void setCurrentTree(GuiTree tree) {
		this.currentTree = tree;
	}

	public GuiTabset getCurrentTabset() {
		return currentTabset;
	}

	void setCurrentTabset(GuiTabset tabset) {
		currentTabset = tabset;
	}
	/**
	 * Liefert die Karte mit dem angegebenen Namen.<p>
	 * Vorsicht!<br>
	 * Wenn die Namen bei mehreren Tabsets doppelt vergeben wurden, 
	 * wird die erste geliefert!
	 * @param name
	 * @return
	 */
	public GuiTab getTabByName(String name) {
		Vector<GuiTabset> v = this.getTabsets();
		if (v == null) return null;
		for (GuiTabset tabset: v) {
			try {
				GuiTab ret = tabset.getTab(name);
				return ret;
			} catch (IllegalArgumentException ex) {
			}
		}
		return null;
	}

	/**
	 * Liefert einen Vector alle Sätze von Registerkarten
	 * 
	 * @see de.guibuilder.design.GuiDoc
	 */
	public Vector<GuiTabset> getTabsets() {
		Container _contentPane = this.getContentPane();
		Component[] comps = _contentPane.getComponents();
		return searchTabset(comps, null);
	}

	private Vector<GuiTabset> searchTabset(Component[] comps, Vector<GuiTabset> v) {
		for (int i = 0; i < comps.length; i++) {
			Component comp = comps[i];
			if (comp instanceof GuiTabset) {
				try {
					GuiTabset tabset = (GuiTabset) comp;
					if (v == null) {
						v = new Vector<GuiTabset>();
					}
					v.add(tabset);
				} catch (IllegalArgumentException ex) {
				}
			}
			if (comp instanceof Container) {
				v = searchTabset(((Container) comp).getComponents(), v);
			}
		}
		return v;
	}

	/**
	 * Liefert den Frame (als JInternalFrame) mit dem angegebenen Namen.<p>
	 * Vorsicht!<br>
	 * Wenn die Namen bei mehreren Frames doppelt vergeben wurden, 
	 * wird der erste geliefert!
	 * 
	 * @param name Name des Frames, wie mit dem Attribut mit name="frame1" angegeben.
	 * @return JInternalFrame mit dem Namen. JInternalFrame als Oberklasse von 
	 * GuiInternalFrameImpl. Die Oberklasse muss anstelle der GuiBuilder-Frameworkklasse 
	 * geliefert werden, weil JInternalFrame.JDesktopIcon (für den Fall eines minimierten
	 * internen Fensters) keine Repräsentation im GuiBuilder-Framework hat. 
	 * @author thomas
	 */
	public JInternalFrame getFrameByName(String name) {
		Vector<JInternalFrame> v = this.getFrames();
		JInternalFrame frame = null;
		if (v == null) return null;
		for (int i = 0; i < v.size(); i++ ) {
			frame = v.get(i);
			if (frame.getName().equalsIgnoreCase(name)) {
				return frame;
			}
		}
		return null;
	}	
	
	/**
	 * Liefert einen Vector aller Frames
	 * 
	 *@author thomas
	 */
	public Vector<JInternalFrame> getFrames() {
		Container _contentPane = this.getContentPane();
		Component[] comps = _contentPane.getComponents();
		return searchFrames(comps, null);
	}	
	
	/**
	 * Sucht rekursiv die enthaltenen Frames
	 * 
	 * TODO Optimierung möglich, weil Frames nur auf der ersten RootPane des Fenster vorkommen?
	 */
	private Vector<JInternalFrame> searchFrames(Component[] comps, Vector<JInternalFrame> v) {
		for (int i = 0; i < comps.length; i++) {
			Component comp = comps[i];
			// System.out.println("Gefundene Komponente der Klasse: "+ comp.getClass().toString());
			if (comp instanceof GuiInternalFrameImpl) {
				try {
					JInternalFrame frame = (JInternalFrame) comp;
					if (v == null) {
						v = new Vector<JInternalFrame>();
					}
					v.add(frame);
				} catch (IllegalArgumentException ex) {
				  String msg = "Warning! In GuiRootPane.searchFrames( " + comps.toString() +", " + v.toString() + ")" + ex.getMessage() + " raised.";
					System.err.println(msg);
					logger.warn(msg);
				}
			}
			if (comp instanceof JInternalFrame.JDesktopIcon) {
				try {
					JInternalFrame.JDesktopIcon dicon = (JInternalFrame.JDesktopIcon)comp; 
					JInternalFrame frame = dicon.getInternalFrame();
					if (v == null) {
						v = new Vector<JInternalFrame>();
					}
					v.add(frame);
				} catch (IllegalArgumentException ex) {
				  String msg = "Warning! In GuiRootPane.searchFrames( " + comps.toString() +", " + v.toString() + ")" + ex.getMessage() + " raised.";
					System.err.println(msg);
					logger.warn(msg);
				}
			}			
			if (comp instanceof Container) {
				v = searchFrames(((Container) comp).getComponents(), v);
			}
		}
		return v;
	}	
	/**
	 * Liefert ein ggf. vorhandenes SplitPanel.
	 */
	public GuiSplit getSplit() {
		return this.currentSplit;
	}

	/**
	 * Setzt das SplitPanel.
	 */
	void setSplit(GuiSplit split) {
		this.currentSplit = split;
	}

	/**
	 * Liefert ein XML-Document mit allen enthaltenen GuiMembers.
	 * 
	 * @see GuiMember#getMemberElement()
	 * @see GuiContainer#getMemberElement()
	 * @return
	 */
	public Document getMemberDocument() {
		Document doc = new Document();
		doc.setEncoding("UFT-8");
		Element mainEle = this.getMainPanel().getMemberElement();
		doc.setRoot(mainEle);
		return doc;
	}

	private String getCurrentDir() {
		if (this.currentDir == null) {
			this.currentDir = GuiUtil.getCurrentDir();
		}
		return this.currentDir;
	}

	/**
	 * Zeigt den gültigen HelpTopic an. Aus StandardFunktion "help()".
	 * 
	 * @see GuiUtil#showHelp
	 */
	public void guiHelp() {
		URL url = null;
		try {
			url = new URL(GuiUtil.getDocumentBase(), helpTopic);
			GuiUtil.showHelp(url, "Help");
			String msg = "Help Topic: " + url.toString();
			if (GuiUtil.getDebug()) {
				System.out.println(msg);
			}
			logger.debug(msg);
		} catch (MalformedURLException ex) {
			GuiUtil.showEx(ex);
		}
	}

	/**
	 * Weiterreichen der Benutzeraktion an einen (ggf. vorhandenen) Java-Controller. (im
	 * Unterschied zu dem BeanShell-Controller).
	 * <p>
	 * Es gibt zwei Sorten von Controller: <br>
	 * Wenn der Controller das Interface UserActionIF implementiert, wird immer dessen
	 * Methode "userActionPerformed" aufgerufen. Ansonsten wird der Methode mit den Name
	 * des ActionCommand per Reflection aufgerufen. Unter diesem Namen muß es im Controller
	 * eine void-Methode mit dem Argument GuiUserEvent geben.
	 * 
	 * @param controller
	 *           der Controller der Componente (GuiMember oder GuiWindow)
	 * @param cmd
	 *           ActionCommand wie im GuiScript spezifiziert (OnChange=, cmd=);
	 * @param event
	 *           GuiUserEvent oder davon abgeleitet.
	 * @return true wenn alles gut ging oder im Fehlerfall false (z.B. wenn die Methode
	 *         fehlt).
	 * @see GuiMember#setController
	 * @see UserActionIF
	 */
	private GuiInvokationResult invokeMethod(final String cmd, final GuiUserEvent event, final Object controller ) {
		GuiInvokationResult result = new GuiInvokationResult("RootPane#invokeMethod");
		if (controller instanceof UserActionIF) {
			((UserActionIF) controller).userActionPerformed(event);
			result.label = "UserActionIF#userActionPerformed";
			result.done = true;
			return result;
		}
		try {
			if (cmd != null) {
				// TODO : Hier kann auch die "richtige" Klasse anhand des Event-Types ermittelt
				// werden; Event-Type an diese Methode übergeben und mit switch {...} cls definieren
				final Class<?>[] cls = { GuiUserEvent.class };
				Object ret = null;
			   Method m = controller.getClass().getMethod(cmd, cls);
			   Object[] args = { event };
			   UserAccessChecker checker = GuiSession.getChecker();
			   if (checker != null) {
			      if (!checker.checkAccess(controller.getClass().getName(), m.getName())) {
			         String msg = "Sie haben leider keinen Zugriff auf " + controller.getClass().getName() + "#" + m.getName();
			         GuiUtil.showMessage(getParentWindow(), "Kein Zugriffsrecht", "Error", msg);
			         return result;
			      }
			   }
			   ret = m.invoke(controller, args);
				result.done = true;
				result.label = "Controller#" + cmd;
				result.returnValue = ret;
				return result;
			}
		} catch (NoSuchMethodException ex) {
		   String msg = ex.getMessage() + " " + controller.getClass().getName() + "#" + cmd;
			System.err.println(msg);
			logger.error(msg);
			result = new GuiInvokationResult(ex);
		} catch (java.lang.reflect.InvocationTargetException ex) {
		   String msg = ex.getTargetException().getMessage() + " "
		     + controller.getClass().getName() + "#" + cmd;
			System.err.println(msg);
			logger.error(msg, ex);
			result = new GuiInvokationResult(ex);
		} catch (IllegalAccessException ex) {
		   String msg = ex.getMessage() + " " + controller.getClass().getName() + "#" + cmd;
			System.err.println(msg);
			logger.error(msg, ex);
			result = new GuiInvokationResult(ex);
		}
		return result;
	}

	/**
	 * Ein Benutzerereignis an den Controller weiterreichen; zuerst wird nach einem
	 * BeanShell oder Pnuts-Script gesucht. Ansonsten wird der Controller aufgerufen.
	 * 
	 * @see GuiMember#getController.
	 */
	private GuiInvokationResult invoke(GuiMember member, String cmd, GuiUserEvent event) {
		Object controller = member.getController();
		return this.invoke(cmd, event, controller);
	}

	/**
	 * Scripting und Controller
	 * 
	 * @param cmd
	 * @param event
	 * @param controller
	 * @return done
	 */
	private GuiInvokationResult invoke(final String cmd, final GuiUserEvent event, final Object controller) {
						
      if(cmd.endsWith(".SW")) { // SwingWorker
         GuiInvokationResult result = new GuiInvokationResult(cmd);
         event.window.cursorWait();
         final String tcmd = cmd.substring(0, cmd.length() - 3);
         SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {

            protected Object doInBackground() throws Exception {
               GuiInvokationResult _result = invokeImpl(tcmd, event, controller);
               return _result;
            }

            protected void done() {
               event.window.cursorDefault();
            }

         };
         worker.execute();
         result.done = true;
         result.isSync = false; // Async
         return result;
      }
      else { // kein SwingWorker          
         GuiInvokationResult result = invokeImpl(cmd, event, controller);
         return result;
      }
   }
	
	private GuiInvokationResult invokeImpl(final String cmd, final GuiUserEvent event, final Object controller) {
      // 1. Scripting
      GuiInvokationResult result = invokeScripting(cmd, event);
      if(result.done)
         return result;
      // 2. Controller
      if(controller != null) {
         result = invokeMethod(cmd, event, controller);
         if(result.done)
            return result;
      }
      // 3. Weder Scripting noch Controller:
      // Eingebaute Methoden abarbeiten
      result = invokeBuildinFunctions(cmd, null);
      return result;
	}

	private GuiInvokationResult invokeScripting(String cmd, GuiUserEvent event) {
		GuiInvokationResult result = new GuiInvokationResult("RootPane#invokeScripting");
		result.done = false;
		if (GuiUtil.hasScripting() == true) {
			// Default-Context ist der aus dem Fenster.
			GuiScripting context = this.getMainPanel().getContext();
			// prüfen, ob ein Panel-spezifische Context vorhanden ist.
			if (event.member != null) {
				GuiContainer cont = event.member.getGuiParent();
				if (cont != null) {
					GuiScripting pcontext = cont.getContext();
					if (pcontext != null) {
						context = pcontext;
					}
				}
			}
			if (context != null) {
				int p = cmd.indexOf("(");
				if (p != -1) {
					cmd = cmd.substring(0, p);
				}
				return context.invokeScripting(cmd, event);
			}
		}
		return result;
	}

	GuiInvokationResult obj_WindowEvent(String cmd, GuiWindowEvent event, Object controller) {
		GuiInvokationResult result = this.invoke(cmd, event, controller);
		return result;
	}

	/**
	 * Diese Methode wird von allen Child-Komponenten beim Ereignis "LostFocus" aufgerufen.
	 * <BR>
	 * Ist ein externer Controller assoziiert, wird dieses Ereignis an ihn weiter geleitet.
	 */
	GuiInvokationResult obj_LostFocus(GuiElement comp, String cmd, Object value, FocusEvent fe) {
		// Event
		GuiLostFocusEvent event = new GuiLostFocusEvent(getParentWindow(), comp, value, fe);
		GuiInvokationResult result = this.invoke(comp, cmd, event);
		return result;
	}

	/**
	 * Weiterreichen der Nachricht über Zustandsänderungen von Komponenten (außer Combo-
	 * und Listboxen). <BR>
	 * Das Fenster wird gleichzeitig als "geändert" markiert. <br>
	 * Ist die Komponente eine Tabellenspalte, wird auch die Tabelle als geändert markiert.
	 * 
	 * @see #isModified
	 */
	GuiInvokationResult obj_ItemChanged(GuiComponent comp, String cmd, Object value) {
		setModified(true);
		// Event
		GuiChangeEvent event = new GuiChangeEvent(getParentWindow(), comp, value);
		// Table?
		GuiTable tbl = comp.getParentTable();
		if (tbl != null) {
			tbl.setModified(true);
			event.row = tbl.getSelectedRow();
		}
		GuiInvokationResult result = this.invoke(comp, cmd, event);
		return result;
	}

	/**
	 * Weiterreichen der Nachricht über Zustandsänderungen von Combo- und List-Komponenten.
	 * <BR>
	 * Das Fenster wird gleichzeitig als "geändert" markiert. <br>
	 * 
	 * @see #isModified
	 */
	GuiInvokationResult obj_ItemChanged(GuiSelect comp, String cmd, Object value, int index) {
		modified = true;
		// Event
		GuiChangeEvent event = new GuiChangeEvent(getParentWindow(), comp, value);
		event.index = index;
		// Table?
		if (comp instanceof TableColumnAble) {
			TableColumnAble col = (TableColumnAble) comp;
			GuiTable tbl = col.getParentTable();
			if (tbl != null) {
				tbl.setModified(true);
				event.row = tbl.getSelectedRow();
			}
		}
		GuiInvokationResult result = this.invoke(comp, cmd, event);
		return result;
	}

	/**
	 * Weiterreichen der Nachricht bei "Click" von Komponenten.
	 */
	GuiInvokationResult obj_Click(GuiComponent comp, String cmd, Object value, MouseEvent me) {
		// 1. OK und Cancel bei Modalen Dialogen
		if (this.getParentWindow() instanceof GuiDialog && this.getParentWindow().isModal()) {
			GuiDialog dialog = (GuiDialog) getParentWindow();
			GuiInvokationResult result = new GuiInvokationResult(cmd);
			if (cmd.equals("Close()")) {
				dialog.setReturnValue(true);
				dialog.hide();
				result.status = GuiInvokationResult.ReturnStatus.OK;
				return result;
			} else if (cmd.equals("Cancel()")) {
				dialog.setReturnValue(false);
				dialog.hide();
				result.status = GuiInvokationResult.ReturnStatus.CANCEL;
				return result;
			}
		}
		// 2. Event
		GuiClickEvent event = new GuiClickEvent(getParentWindow(), comp, value, me);
		if (comp instanceof GuiSelect) {
			event.index = ((GuiSelect) comp).getSelectedIndex();
		}
		// 3. invoke
		GuiInvokationResult result = this.invoke(comp, cmd, event);
		return result;
	}
	/**
	 * Weiterreichen der Nachricht bei Doppelklick von Komponenten.
	 */
	GuiInvokationResult obj_DblClick(GuiComponent comp, String cmd, Object value, MouseEvent me) {
		// OK und Cancel bei Modalen Dialogen
		if (this.getParentWindow() instanceof GuiDialog && this.getParentWindow().isModal()) {
			GuiDialog dialog = (GuiDialog) getParentWindow();
			GuiInvokationResult result = new GuiInvokationResult(cmd);
			if (cmd.equals("Close()")) {
				dialog.setReturnValue(true);
				dialog.hide();
				result.status = GuiInvokationResult.ReturnStatus.OK;
				return result;
			} else if (cmd.equals("Cancel()")) {
				dialog.setReturnValue(false);
				dialog.hide();
				result.status = GuiInvokationResult.ReturnStatus.CANCEL;
				return result;
			}
		}
		// Event
		GuiDblClickEvent event = new GuiDblClickEvent(getParentWindow(), comp, value, me);
		if (comp instanceof GuiSelect) {
			event.index = ((GuiSelect) comp).getSelectedIndex();
		}
		GuiInvokationResult result = this.invoke(comp, cmd, event);
		return result;
	}

	/**
	 * Wird von GuiMember aufgerufen, wenn dort ein entsprechendes ActionCommand gesetzt
	 * wurde.
	 */
	GuiInvokationResult obj_MouseOver(GuiMember comp, String cmd, MouseEvent me, boolean isOver) {
		// Event
		GuiMouseOverEvent event = new GuiMouseOverEvent(getParentWindow(), comp, me, isOver);
		GuiInvokationResult result = this.invoke(comp, cmd, event);
		return result;
	}

	/**
	 * Wird von GuiMember aufgerufen, wenn dort ein entsprechendes ActionCommand gesetzt
	 * wurde.
	 */
	GuiInvokationResult obj_MouseMoved(GuiMember comp, String cmd, MouseEvent me, boolean isDrag) {
		// Event
		GuiMouseMovedEvent event = new GuiMouseMovedEvent(getParentWindow(), comp, me, isDrag);
		GuiInvokationResult result = this.invoke(comp, cmd, event);
		return result;
	}
	/**
	 * Es wurde ein Popup-Show-Event ausgelöst.
	 * @param member
	 * @param cmd
	 * @param event
	 */
	GuiInvokationResult obj_PopupShow(GuiMember member, String cmd, GuiPopupEvent event) {
		GuiInvokationResult result = this.invoke(member, cmd, event);
		return result;
	}
	/**
	 * Weiterreichen der Nachricht über das öffnen von Registerkarten.
	 */
	GuiInvokationResult obj_TabOpen(GuiPanel tab, String cmd, GuiTabSelectionEvent event) {
		GuiInvokationResult result = this.invoke(tab, cmd, event);
		return result;
	}

	/**
	 * Weiterreichen der Nachricht, wenn eine andere Tabellenzeile angeklickt wurde.
	 */
	GuiInvokationResult obj_TblRowSelected(GuiTable tbl, String msgRowClick, int rowNumber, Vector<Object> values) {
		// Event
		GuiTableEvent event = new GuiTableEvent(this.getParentWindow(), tbl,
				GuiTableEvent.EventType.ROW_CLICK);
		event.index = rowNumber;
		event.value = tbl.getRow(rowNumber);
		GuiInvokationResult result = this.invoke(tbl, msgRowClick, event);
		return result;
	}

	/**
	 * Weiterreichen der Nachricht, wenn ein anderer Knoten im Baum selektiert wurde.
	 */
	GuiInvokationResult obj_TreeNodeSelected(GuiTree tree, String cmd, GuiTreeNode node) {
		// Event
		GuiTreeNodeSelectionEvent event = new GuiTreeNodeSelectionEvent(this
				.getParentWindow(), tree, node);
		GuiInvokationResult result = this.invoke(tree, cmd, event);
		return result;
	}

	/**
	 * Weiterreichen der Nachricht, wenn eine Tabelle doppelt angeklickt wurde.
	 */
	GuiInvokationResult obj_TblDoubleClick(GuiTable tbl, String msgDblClick, int rowNumber, Vector<Object> values) {
		// OK und Cancel bei Modalen Dialogen
		if (this.getParentWindow() instanceof GuiDialog && this.getParentWindow().isModal()) {
			GuiDialog dialog = (GuiDialog) getParentWindow();
			GuiInvokationResult result = new GuiInvokationResult(msgDblClick);
			if (msgDblClick.equals("Close()")) {
				dialog.setReturnValue(true);
				dialog.hide();
				result.done = true;
				result.status = GuiInvokationResult.ReturnStatus.OK;
				return result;
			} else if (msgDblClick.equals("Cancel()")) {
				dialog.setReturnValue(false);
				dialog.hide();
				result.done = true;
				result.status = GuiInvokationResult.ReturnStatus.CANCEL;
				return result;
			}
		}
		// Event
		GuiTableEvent event = new GuiTableEvent(this.getParentWindow(), tbl,
				GuiTableEvent.EventType.DBL_CLICK);
		event.index = rowNumber;
		event.value = tbl.getRow(rowNumber);
		GuiInvokationResult result = this.invoke(tbl, msgDblClick, event);
		return result;
	}

	/**
	 * Weiterreichen der Nachricht, wenn ein Spaltenkopf einer Tabelle angeklickt wurde.
	 */
	GuiInvokationResult obj_TblHeaderClick(GuiTable tbl, String msgColHeaderClick, int columnNumber) {
		// Event
		GuiTableEvent event = new GuiTableEvent(this.getParentWindow(), tbl,
				GuiTableEvent.EventType.HEADER_CLICK);
		event.index = columnNumber;
		GuiInvokationResult result = this.invoke(tbl, msgColHeaderClick, event);
		return result;
	}

	/**
	 * Bei einer MessageBox wurde ein Button gedrückt.
	 * 
	 * @see de.guibuilder.framework.event.GuiMessageBoxEvent
	 */
	public GuiInvokationResult obj_MessageBoxEvent(GuiWindow parent, String msgName, String cmd) {
		// Event
		GuiMessageBoxEvent event = new GuiMessageBoxEvent(parent, msgName, cmd);
		GuiInvokationResult result = this.invoke(cmd, event, parent.getController());
		return result;
	}

	/**
	 * In einer TextBox wurde eine Taste gedrückt.
	 */
	GuiInvokationResult obj_KeyEvent(GuiComponent comp, String cmd, Object value, KeyEvent e) {
		// Event
		GuiKeyEvent event = new GuiKeyEvent(getParentWindow(), comp, value, e);
		GuiInvokationResult result = this.invoke(comp, cmd, event);
		return result;
	}

	/**
	 * Weiterleiden der Nachricht, wenn eine Komponente erzeugt wurde. Siehe
	 * OnCreate="[Event]"
	 * 
	 * @param comp
	 *           Component
	 * @param cmd
	 *           Message
	 */
	GuiInvokationResult obj_Create(GuiComponent comp, String cmd) {
		GuiCreateEvent event = new GuiCreateEvent(getParentWindow(), comp);
		GuiInvokationResult result = this.invoke(comp, cmd, event);
		return result;
	}

	GuiInvokationResult obj_Drop(GuiMember comp, DropTargetDropEvent event, String cmd) {
		GuiDropEvent gde = new GuiDropEvent(this.getParentWindow(), comp, event);
		GuiInvokationResult result = this.invoke(comp, cmd, gde);
		return result;
	}
  GuiInvokationResult obj_FileDrop(GuiMember comp, String cmd, File[] files) {
    GuiFileDropEvent gde = new GuiFileDropEvent(this.getParentWindow(), comp, files);
    GuiInvokationResult result = this.invoke(comp, cmd, gde);
    return result;
  }

	GuiInvokationResult obj_Drag(GuiMember comp, DropTargetEvent event, String cmd, int type) {
		GuiDragEvent gde = new GuiDragEvent(this.getParentWindow(), comp, event, type);
		GuiInvokationResult result = this.invoke(comp, cmd, gde);
		return result;
}

	/**
	 * Führt eine Aktion aus.
	 * <P>
	 * Wenn ein externer Controller registriert wurde, wird dort "doAction" aufgerufen.
	 * <P>
	 * für interne Zwecke fest definiert sind:
	 * <UL>
	 * <LI>OpenFile()
	 * <LI>SaveFile()
	 * <LI>XmlSave()
	 * <LI>XmlOpen()
	 * <li>delete()
	 * <LI>Close()
	 * <LI>Cancel()
	 * <LI>Exit()
	 * <LI>InsertRow()
	 * <LI>DeleteRow()
	 * <LI>CopyRow()
	 * <LI>PasteRow()
   * <LI>DuplicateRow()
	 * <LI>NewNode()
	 * <LI>CopyNode()
	 * <LI>CutNode()
	 * <LI>PasteNode()
	 * <LI>new()
	 * <LI>help()
	 * </UL>
	 */
	GuiInvokationResult obj_ActionPerformed(GuiAction action, ActionEvent e) {
		GuiInvokationResult result = new GuiInvokationResult("RootPane#obj_ActionPerformed");
		// 0. Wenn durch ein Menü eine Aktion ausgelöst wird,
		// wird nicht der TableCellEditor beendet!
		// Das geschieht hier mit Gewalt!
		if (this.getCurrentTable() != null) { // New 29.12.2003 // PKÖ
			this.getCurrentTable().stopCellEditing();
		}
		
		// 1. OK und Cancel bei Modalen Dialogen
		GuiWindow _parentWindow = this.getParentWindow(); 
		if (_parentWindow instanceof GuiDialog && _parentWindow.isModal()
				&& action.getFileName() != null) {
			GuiDialog dialog = (GuiDialog) getParentWindow();
			if (action.getFileName().equals("Close()")) {
				dialog.setReturnValue(true);
				dialog.hide();
				result.done = true;
				result.status = GuiInvokationResult.ReturnStatus.OK; 
				return result; // 1.a
			} else if (action.getFileName().equals("Cancel()")) {
				dialog.setReturnValue(false);
				dialog.hide();
				result.done = true;
				result.status = GuiInvokationResult.ReturnStatus.CANCEL; 
				return result; // 1.b
			}
		}

		// Event erzeugen
		GuiActionEvent event = new GuiActionEvent(getParentWindow(), action, e);
		// 2. Scripting/Controller
		String cmd = action.getActionCommand();
		result = this.invoke(cmd, event, action.getController());
		if (result.done) {
			return result; // 2.a/b
		}
		// 3. Weder OK/Cancel von modalen Dialogen
		// noch Scripting noch Controller:
		// Eingebaute Methoden abarbeiten
		result = this.invokeBuildinFunctions(action.getFileName(), e);
		return result;
	}
	/**
	 * Eingebaute Methoden aufrufen
	 * @param actionCommand
	 * @param ae
	 * @return TODO: was bedeutet dieser return-Wert?
	 */
	private GuiInvokationResult invokeBuildinFunctions(String command, AWTEvent ae) {
		GuiInvokationResult result = new GuiInvokationResult("RootPane#invokeBuildinFuncions");
		//String command = action.getFileName();
		if (command == null)
			return result;
		// Schleife über alle Befehle mit ";" getrennt.
		StringTokenizer tokens = new StringTokenizer(command, ";");
		while (tokens.hasMoreTokens()) {
			String tok = tokens.nextToken().trim();
			if (GuiUtil.getDebug()) {
				System.out.println("Token: " + tok);
			}
			if (tok.equalsIgnoreCase("OpenFile()")) {
				String[] ret = GuiUtil.fileOpenDialog(this.getParentWindow(), "Datei öffnen",
						this.getCurrentDir(), null);
				if (ret != null) {
					this.currentDir = ret[1];
					this.fileName = ret[2];
				}
			} else if (tok.equals("SaveFile()")) {
				String[] ret = GuiUtil.fileOpenDialog(this.getParentWindow(),
						"Datei speichern", this.getCurrentDir(), null);
				if (ret != null) {
					this.currentDir = ret[1];
					this.fileName = ret[2];
				}
			} else if (tok.equalsIgnoreCase("XmlSave()")) {
				this.xmlSave(false);
			} else if (tok.equalsIgnoreCase("XmlSaveAs()")) {
				this.xmlSave(true);
			} else if (tok.equalsIgnoreCase("XmlOpen()")) {
				this.xmlOpen();
			} else if (tok.equalsIgnoreCase("XmlLoadModel()")) {
				this.xmlLoadModel();
			} else if (tok.equalsIgnoreCase("XmlSaveModel()")) {
				this.xmlSaveModel();
			} else if (tok.equalsIgnoreCase("Close()")) {
				if (this.getParentWindow() instanceof GuiDialog) {
					((GuiDialog) this.getParentWindow()).setReturnValue(true);
				}
				//this.getParentWindow().dispose();
				this.getParentWindow().hide();
			} else if (tok.equalsIgnoreCase("Cancel()")) {
				if (this.getParentWindow() instanceof GuiDialog) {
					((GuiDialog) this.getParentWindow()).setReturnValue(false);
				}
				//this.getParentWindow().dispose();
				this.getParentWindow().hide();
			} else if (tok.equalsIgnoreCase("Exit()")) {
				this.getParentWindow().hide(); // Kein Close-Ereignis???
				System.exit(0);
			}
			// Table
			else if (tok.equalsIgnoreCase("InsertRow()")) {
				if (this.currentTable != null) {
					this.currentTable.insertRow(currentTable.getSelectedRow() + 1);
				} else if (ae != null && ae.getSource() instanceof Component) {
					Component button = (Component) ae.getSource();
					Container panel = button.getParent();
					for (int ii = 0; ii < panel.getComponentCount(); ii++) {
						if (panel.getComponent(ii) instanceof GuiScrollBox) {
							GuiScrollBox sBox = (GuiScrollBox) panel.getComponent(ii);
							this.currentTable = sBox.getGuiTable();
							if (sBox.getGuiTable() != null) {
								this.currentTable = sBox.getGuiTable();
								this.currentTable.insertRow(currentTable.getSelectedRow() + 1);
								break;
							}
						}
					}
				}
			} else if (tok.equalsIgnoreCase("DeleteRow()")) {
				if (this.currentTable != null) {
					currentTable.deleteSelectedRows(); // 25.6.2004 // PKÖ
				}
			} else if (tok.equalsIgnoreCase("CopyRow()")) {
				if (this.currentTable != null) {
					this.currentTable.copyRow();
				}
			} else if (tok.equalsIgnoreCase("CutRow()")) {
				if (this.currentTable != null) {
					this.currentTable.deleteRow();
				}
			} else if (tok.equalsIgnoreCase("PasteRow()")) {
				if (this.currentTable != null) {
					this.currentTable.pasteRow();
				}
      } else if (tok.equalsIgnoreCase("DuplicateRow()")) {
        if (this.currentTable != null) {
          this.currentTable.duplicateRow();
        }
			} else if (tok.equalsIgnoreCase("EditRow()")) {
				if (this.currentTable != null) {
					this.currentTable.d_click(null);
				}
			}
			// Tree
			else if (tok.equalsIgnoreCase("NewNode()")) {
				if (this.currentTree != null) {
					@SuppressWarnings("unused")
					GuiTreeNode newNode = currentTree.createNode();
				}
			} else if (tok.equalsIgnoreCase("CopyNode()")) {
				if (this.currentTree != null) {
					currentTree.copyNode();
				}
			} else if (tok.equalsIgnoreCase("CutNode()")) {
				if (this.currentTree != null) {
					currentTree.cutNode();
				}
			} else if (tok.equalsIgnoreCase("PasteNode()")) {
				if (this.currentTree != null) {
					currentTree.pasteNode();
				}
			} else if (tok.equalsIgnoreCase("PasteNodeBelow()")) {
				if (this.currentTree != null) {
					currentTree.pasteNode(GuiTree.NODE_DOWN);
				}
			} else if (tok.equalsIgnoreCase("NodeUp()")) {
				if (this.currentTree != null) {
					currentTree.nodeUp();
				}
			} else if (tok.equalsIgnoreCase("NodeDown()")) {
				if (this.currentTree != null) {
					currentTree.nodeDown();
				}
			} else if (tok.equalsIgnoreCase("NodeLeft()")) {
				if (this.currentTree != null) {
					currentTree.nodeLeft();
				}
			} else if (tok.equalsIgnoreCase("NodeRight()")) {
				if (this.currentTree != null) {
					currentTree.nodeRight();
				}
			} else if (tok.equalsIgnoreCase("new()")) {
				this.reset();
			} else if (tok.equalsIgnoreCase("delete()")) {
				if (GuiUtil.yesNoMessage(this.getParentWindow(), "Löschen",
						"Soll der Datensatz gelöscht werden?")) {
					this.reset();
				}
			} else if (tok.equalsIgnoreCase("RemovePreferences()")) {
				this.getParentWindow().setRemovePreferencesWhenClosed(true);
			}
			/*
			 * else if (tok.equals("getXY()")) { this.getXY(); }
			 */
			else if (tok.startsWith("select(")) {
				int start = tok.indexOf("(");
				int end = tok.indexOf(")");
				if (start != -1 && end != -1) {
					String args = tok.substring(start + 1, end);
					StringTokenizer targs = new StringTokenizer(args, ",");
					int ii = 0;
					GuiList lFrom = null;
					GuiList lTo = null;
					while (targs.hasMoreTokens()) {
						ii++;
						String arg = targs.nextToken().trim();
						switch (ii) {
						case 1:
							lFrom = (GuiList) this.getMainPanel().getGuiComponent(arg);
							break;
						case 2:
							lTo = (GuiList) this.getMainPanel().getGuiComponent(arg);
							if (lFrom.getValue() != null) {
								lTo.addItem(lFrom.getValue());
							}
							break;
						}
					}
				}
			}

			else if (tok.startsWith("deselect(")) {
				int start = tok.indexOf("(");
				int end = tok.indexOf(")");
				if (start != -1 && end != -1) {
					String arg = tok.substring(start + 1, end);
					GuiList lst = (GuiList) this.getMainPanel().getGuiComponent(arg);
					if (lst.getSelectedItem() != null) {
						lst.removeItem(lst.getSelectedItem());
					}
				}
			}

			else if (tok.equalsIgnoreCase("help()")) {
				this.guiHelp();
			}
			/*
			 * ### deprecated else if (tok.startsWith("replace(")) { int start =
			 * tok.indexOf("("); int end = tok.indexOf(")"); if (start != -1 && end != -1) {
			 * String file = tok.substring(start+1, end);
			 * this.getParentWindow().replaceRootPane(file); } } else if
			 * (tok.equals("objectSize()")) { System.out.println("ObjectSize:
			 * "+this.getParentWindow().getObjectSize()); }
			 */
			else if (tok.equalsIgnoreCase("verify()")) {
				try {
					this.getParentWindow().verify();
				} catch (IllegalStateException ex) {
					GuiUtil.showMessage(this.getParentWindow(), "Eingabefehler", "Error", ex
							.getMessage());
					result = new GuiInvokationResult(ex);
					return result;
				}
			}
			// TODO : Wie navigieren zu GuiEditor?
			else if (tok.equalsIgnoreCase("back()")) {
				GuiEditor editor = findEditor();
				if (editor != null) {
					editor.back();
				}
			} else if (tok.equalsIgnoreCase("forward()")) {
				GuiEditor editor = findEditor();
				if (editor != null) {
					editor.forward();
				}
			} else if (tok.equalsIgnoreCase("home()")) {
				GuiEditor editor = findEditor();
				if (editor != null) {
					editor.home();
				}
			}
			// TODO : Was das?
			else if (tok.equalsIgnoreCase("DocumentChooser()")) {
				try {
					GuiDialog dia = (GuiDialog) GuiFactory.getInstance().createWindow(
							GuiUtil.getCodeBase() + "DocumentChooser.xml");
					dia.getRootPane().setDesignMode(false);
					if (dia.showDialog()) {

					}
				} catch (GDLParseException ex) {
					GuiUtil.showEx(ex);
				}
			}
			// Keine StandardFunktion: file=[filename]
			else {
				if (tok.indexOf(".") != -1 && tok.endsWith(".") == false) { // Versuch, einen
																								// Dateinamen zu
																								// erkennen!
					try {
						GuiWindow window = GuiFactory.getInstance().createWindow(tok);
						window.show();
					} catch (GDLParseException ex) {
						GuiUtil.showEx(ex);
						result = new GuiInvokationResult(ex);
						return result;
					}
				} else {
					result.returnValue = GuiInvokationResult.ReturnStatus.ERROR;
					return result;
				}
			}
		} // End Of Has More Tokens
		return result;
	} // End of obj_ActionPerformed

	/**
	 * Hack
	 */
	private GuiEditor findEditor() {
		GuiMember member = getMainPanel().getGuiComponent("editor");
		if (member instanceof GuiEditor) {
			return (GuiEditor) member;
		} else {
			return null;
		}
	}

	/**
	 * Setzt den HelpTopic zum Formular.
	 */
	public void setHelpTopic(String topic) {
		this.helpTopic = topic;
	}

	/**
	 * Liefert den jeweiligen HelpTopic.
	 */
	public String getHelpTopic() {
		return helpTopic;
	}

	/**
	 * Aktiviert die Registerkarte mit dem angegebenen Namen.
	 */
	public void activateTab(String name) {
		GuiTab tab = getTabByName(name);
		if (tab == null) {
			throw new IllegalArgumentException("Missing Tab: " + name);
		} else {
			if (tab.getGuiTabset() == null) {
				throw new IllegalArgumentException("Missing Tabset: " + name);
			} else {
				tab.getGuiTabset().setSelectedIndex(tab.getTabIndex());
			}
		}
	}

	/**
	 * En- oder disabled die Registerkarte mit dem angegebenen Namen.
	 */
	public void enableTab(String name, boolean b) {
		GuiTab tab = getTabByName(name);
		if (tab == null) {
			throw new IllegalArgumentException("Missing Tab: " + name);
		} else {
			if (tab.getGuiTabset() == null) {
				throw new IllegalArgumentException("Missing Tabset: " + name);
			} else {
				tab.getGuiTabset().setEnabledAt(tab.getTabIndex(), b);
			}
		}
	}

	/**
	 * Leert alle Componenten des Fensters. Dabei wird die Eigenschaft "modified" auf
	 * "false" gesetzt.
	 * <p>
	 * Das erste editierbare Widget erhält den Focus.
	 * 
	 * @see GuiContainer#reset
	 * @see #isModified
	 */
	public void reset() {
		this.mainPanel.reset();
		this.setModified(false);
		// this.setOid(null); // Auskommentiert PKÖ
		// Focus
		GuiComponent comp = this.mainPanel.getFirstComponent();
		if (comp != null) {
			comp.requestFocus();
		}
	}

	/**
	 * @deprecated Speichert in Inhalt der Maske in einem XML-Dokument. <BR>
	 *             Als Zeichensatz wird "UTF-8" und die DTD "guivalues.dtd" verwendet.
	 *             <br>
	 *             Als Action ist im Menü|Button "file=XmlSave()" einzutragen.
	 * @param as
	 *           true = SaveAs(); false = Save()
	 */
	private void xmlSave(boolean as) {
		Document doc = this.getMainPanel().getAllValuesXml();
		this.xmlSaveDoc(doc, as);
	}

	/**
	 * @deprecated Ruft einen Datei-speichern Dialog zur Speicherung des übergebenen
	 *             XML-Dokumentes auf.
	 * @param doc
	 *           Das zu speichernde XML-Dokument.
	 */
	private void xmlSaveDoc(Document doc, boolean as) {
		String dir = this.getCurrentDir();
		String file = this.fileName;
		String path = dir + file;
		if (dir == null || file == null) {
			as = true;
		} else {
			// ?
		}
		// Save As File Dialog?
		if (as) {
			String[] ret = GuiUtil.fileSaveDialog(this.getParentWindow(),
					"XML-Datei speichern", dir, file);
			if (ret != null) {
				this.currentDir = ret[1];
				this.fileName = ret[2];
				path = ret[0];
			} else {
				return;
			}
		}
		File f;
		f = new File(path);
		FileWriter fos = null;
		try {
			fos = new FileWriter(f);
			doc.write(fos);
			fos.close();
		} catch (Exception exc) {
			GuiUtil.showEx(exc);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	/**
	 * @deprecated Liefert ein XML-Dokument auf Basis eines File-Open Dialoges.
	 */
	private Document xmlOpenFile() {
		Document doc = null;
		String[] ret = GuiUtil.fileOpenDialog(this.getParentWindow(), "XML-Datei öffnen",
				this.getCurrentDir(), "*.xml");
		if (ret != null) {
			this.currentDir = ret[1];
			this.fileName = ret[2];
			File f = new File(ret[0]);
			try {
        doc = new Document(f);
			} catch (Exception ep) {
				GuiUtil.showEx(ep);
			}
		}
		return doc;
	}

	/**
	 * @deprecated öffnen eines XML-Datendokumentes mit dem Standard FileDialog und setzen
	 *             der Werte.
	 */
	private void xmlOpen() {
		Document doc = xmlOpenFile();
		if (doc != null) {
			Element node = doc.getRoot();
			this.getMainPanel().setAllValuesXml(node);
		} // doc != null
	}

	/**
	 * @deprecated
	 *  
	 */
	private void xmlLoadModel() {
		Document doc = xmlOpenFile();
		if (doc != null) {
			JDataSet ds = new JDataSet(doc);
			this.reset();
			this.getParentWindow().setDatasetValues(ds);
		}
	}

	/**
	 * @deprecated
	 *  
	 */
	private void xmlSaveModel() {
		JDataSet ds = this.getParentWindow().getDatasetValues();
		this.xmlSaveDoc(ds.getXml(), true);
	}

	// Attribut "ref="
	public String getRef() {
		return this.attRef;
	}

	public void setRef(String ref) {
		this.attRef = ref;
	}

	/**
	 * Liefert die Object-Id oder null, wenn kein Dabaseobject gehalten wird.
	 */
	public String getOid() {
		return this.soid;
	}

	/**
	 * Setzt die Object-Id; für Datenbanken; wird bei reset auf null gesetzt.
	 * 
	 * @see #reset
	 */
	public void setOid(String s) {
		this.soid = s;
	}

	final void dispose() {
		this.getMainPanel().dispose(); // Arbeitet alle Child-Components ab
		currentDir = null;
		currentSplit = null;
		currentTable = null;
		currentTree = null;
		fileName = null;
		helpTopic = null;
		mainPanel = null;
		if (menuBar != null) {
			getGuiMenuBar().dispose();
			menuBar = null;
			this.setJMenuBar(null);
		}
		parentWindow = null;
		statusBar = null;
		if (toolBar != null) {
			toolBar.removeAll();
			toolBar = null;
		}
		this.getContentPane().removeAll();
	}
}