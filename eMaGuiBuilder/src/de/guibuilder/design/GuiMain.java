package de.guibuilder.design;

import java.io.File;
import java.net.URL;

import javax.swing.JOptionPane;

import org.apache.log4j.xml.DOMConfigurator;

import de.guibuilder.adapter.GuiAPI;
import de.guibuilder.framework.GuiApplet;
import de.guibuilder.framework.GuiAppletImpl;
import de.guibuilder.framework.GuiUtil;
import de.guibuilder.framework.GuiWindow;

/**
 * Beispielhafte Implementierung eines Controllers f&uuml; ein GuiBuilder UI.
 * mit &Uuml;bergabe der Messages an einen Adapter.<br>
 * <i>Example implementation of a controller for a GuiBuilder UI.</i>
 */
public final class GuiMain extends GuiAppletImpl {
	private static final long serialVersionUID = 1L;
	/**
	 * Logger
	 */
	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GuiMain.class);
	/**
	 * Assoziierter Adapter f&uuml;r Dialogsteuerung
	 */
	private static final String GUI_EDITOR = "GuiBuilder.xml";

	/**
	 * Erzeugt einen neues GuiMain, das ist ein GuiAppletImpl mit dem Namen
	 * <b>GuiBuilder</b>, sowie Funktionen, um XML GDL Beschreibungen zu
	 * gestalten und zu testen, und diese in Dateien zu speichern.<br>
	 * <i>Creates a new GuiMain that is a GuiAppletImple with the name
	 * <b>GuiBuilder</b> and added functionality to design and test XML GDL
	 * descriptions and store it into files.</i>
	 */
	public GuiMain() {
		super();
		this.setName("GuiBuilder");
	}

	/**
	 * <p>
	 * Haupt-Einstiegspunkt des GuiBuilders beim Aufruf als Applikation. Als
	 * erstes Programm-Argument kann eine GDL-Beschreibungsdatei angegeben
	 * werden, welche die Benutzerschnittstelle definiert. Folgende weitere
	 * Programm-Argumente werden beachtet, falls sie in der form
	 * &lt;key&gt;=&lt;value&gt; angegeben werden:
	 * <p>
	 * <table>
	 * <tr>
	 * <th>Key</th>
	 * <th>Value</th>
	 * </tr>
	 * <tr>
	 * <td>StartWindow</td>
	 * <td>Eine GDL-Beschreibungsdatei, welche die Benutzerschnittstelle
	 * defininert. Der Schl&uuml;ssel kann weg gelassen werden, wenn dieses
	 * Argument als erstes angegeben wird (s.o.).</td>
	 * </tr>
	 * <tr>
	 * <td>DocumentBase</td>
	 * <td>Ein Verzeichnis, in dem die alle Dateien gesucht werden, falls ein
	 * relativer Pfad angegeben wird.</td>
	 * </tr>
	 * <tr>
	 * <td>UIManager</td>
	 * <td>Symbolischer Name eines UIManagers. Folgende Symbole werden erkannt:
	 * windows, metal, motif, gtk, 3d, oyoaha, skin und kunststoff.</td>
	 * </tr>
	 * <tr>
	 * <td>Version</td>
	 * <td>Setzt die Version der GDL-Beschreibungen.</td>
	 * </tr>
	 * <tr>
	 * <td>ResourceBundle</td>
	 * <td>Setzt eine Datei als eigenes ResourceBundle.</td>
	 * </tr>
	 * <tr>
	 * <td>Debug</td>
	 * <td>Schaltet den Debug Modus ein.</td>
	 * </tr>
	 * </table>
	 * <i>
	 * <p>
	 * Main entry point when starting as an application. A GDL file for the GUI
	 * of this programm may be given as first program argument. More arguments
	 * can be given in the form &lt;key&gt;=&lt;value&gt;:
	 * <p>
	 * <table>
	 * <tr>
	 * <th>Key</th>
	 * <th>Value</th>
	 * </tr>
	 * <tr>
	 * <td>StartWindow</td>
	 * <td>A GDL file for this applikation. The key may be discarded if it is the
	 * first argument (see above).</td>
	 * </tr>
	 * <tr>
	 * <td>DocumentBase</td>
	 * <td>The base directory for all files given with relative file names</td>
	 * </tr>
	 * <tr>
	 * <td>UIManager</td>
	 * <td>Symbolic name of a UIManager. The following symbols are recognized:
	 * windows, metal, motif, gtk, 3d, oyoaha, skin und kunststoff.</td>
	 * </tr>
	 * <tr>
	 * <td>Version</td>
	 * <td>The version of GDL descriptions.</td>
	 * <tr>
	 * <tr>
	 * <td>ResourceBundle</td>
	 * <td>Set own ResourceBundle file.</td>
	 * <tr>
	 * <tr>
	 * <td>Debug</td>
	 * <td>Switch on debug mode.</td>
	 * </tr>
	 * </table>
	 * <i>
	 * 
	 * @see javax.swing.UIManager
	 * @see java.util.ResourceBundle
	 */
	public static void main(final String args[]) {
		String logConfig = "Log4J_GuiBuilder.xml";
		try {
			File f = new File(logConfig);
			if (f.exists()) {
				DOMConfigurator.configureAndWatch(logConfig);
				logger.info("Logger (Client/File) initialized #################");
			} else {
				URL url = GuiMain.class.getClassLoader().getResource(logConfig);
				if (url != null) {
					DOMConfigurator.configure(url);
					logger.info("Logger (Client/URL) initialized #################");
				} else {
					// 1.3 nix zu finden
				}
			}
			logger.info("GuiBuilder started #################");
		} catch (Exception ex) {
			System.out.println("Error config logger: " + ex.getMessage());
		}
		String java = System.getProperty("java.version");
		if (java.compareTo("1.6") == -1 && java.compareTo("1.7") == -1) {
			System.err.println("Fatal: JDK 1.6 required!");
			JOptionPane.showMessageDialog(null, "JDK 1.6 required!", "Fatal: Java Version", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		GuiMain applet = new GuiMain();
		String startWindow;
		if (args.length > 0 && args[0].indexOf("=") == -1) {
			startWindow = args[0];
		} else {
			startWindow = GUI_EDITOR;
		}
		for (int i = 0; i < args.length; i++) {
			String param = args[i];
			int j = param.indexOf("=");
			if (j > 5) {
				String key = param.substring(0, j).toLowerCase();
				String val = param.substring(j + 1, param.length());
				if (key.equalsIgnoreCase("startwindow")) {
					startWindow = val;
				} else if (key.equalsIgnoreCase("documentbase")) {
					GuiUtil.setDocumentBase(val);
				} else if (key.equalsIgnoreCase("uimanager")) {
					GuiUtil.setUiManager(val);
				} else if (key.equalsIgnoreCase("version")) {
					GuiUtil.setVersion(val);
				} else if (key.equalsIgnoreCase("resourcebundle")) {
					GuiUtil.setDefaultResourceBundle(val);
				} else if (key.equalsIgnoreCase("debug")) {
					GuiUtil.setDebug(true);
				}
			}
		}
		applet.guiInit(startWindow);
	}

	/**
	 * <p>
	 * Haupt-Einstiegspunkt beim Start als Applet. Es werden div.
	 * Applet-Parameter beachtet (siehe <b>main()</b>)
	 * </p>
	 * <i>
	 * <p>
	 * Main entry point when started as applet. Several applet parameters are
	 * processed (see <b>main()</b>
	 * </p>
	 * </i>
	 * 
	 * @see de.guibuilder.framework.GuiUtil#setApplet
	 */
	public void init() {
		GuiApplet apl = new GuiApplet();
		apl.setApplet(this);
		// Hier wird GuiUtil initialisiert, wenn Applet
		GuiUtil.setApplet(apl); // setzt auch Codebase
		GuiUtil.loadGuiPropXml(); // hier richtig??
		GuiUtil.setDocumentBase(GuiUtil.getCodeBase());

		String startWindow = getParameter("StartWindow");
		if (startWindow == null) {
			startWindow = GUI_EDITOR;
		}
		String docBase = getParameter("DocumentBase");
		if (docBase != null) {
			GuiUtil.setDocumentBase(docBase);
		}

		String uiManager = getParameter("UIManager");
		if (uiManager != null) {
			GuiUtil.setUiManager(uiManager);
		}
		String version = getParameter("Version");
		if (version != null) {
			GuiUtil.setVersion(version);
		}
		String debug = getParameter("Debug");
		if (debug != null && debug.equals("true")) {
			GuiUtil.setDebug(true);
		}
		String resourceBundle = getParameter("ResourceBundle");
		if (resourceBundle != null) {
			GuiUtil.setDefaultResourceBundle(resourceBundle);
		}

		this.guiInit(startWindow);
	}

	/**
	 * <p>
	 * Typische Initialisierung des Controllers.
	 * </p>
	 * <i>
	 * <p>
	 * Typical init of a Controller for the GUI
	 * </p>
	 * </i>
	 */
	private void guiInit(String startWindow) {
		// Hier wird GuiUtil initialisiert wenn Application
		System.out.println("CodeBase: " + GuiUtil.getCodeBase());
		System.out.println("DocumentBase: " + GuiUtil.getDocumentBase());
		System.out.println("Directory: " + GuiUtil.getCurrentDir());
		if (startWindow.equals(GUI_EDITOR)) {
			new ControlGuibuilder(startWindow);
		} else {
			System.out.println("StartWindow: " + startWindow);
			GuiAPI.getInstance().openWindow("Main", "1", startWindow);
			// Wenn das Startfenster geschlossen wird, dann auch Anwendung beendet.
			// Das würde bei einem Login-Dialog schief gehen!
			GuiWindow start = GuiAPI.getInstance().getWindow("1");
			start.setSystemForm(true);
		}
	}
}