/*
 * Created on 27.02.2005
 */
package de.guibuilder.help;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.net.URL;

import javax.help.CSH;
import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;

import de.guibuilder.framework.GuiAction;
import de.guibuilder.framework.GuiSession;
import de.guibuilder.framework.GuiWindow;
import de.guibuilder.framework.HelpManagerIF;
import de.guibuilder.framework.MemberAble;

/**
 * Implementierung eines HelpManagers für JavaHelp
 * 
 * @author peter
 */
public class JavaHelpManager implements HelpManagerIF {
	private HelpBroker helpBroker;
	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(JavaHelpManager.class);
	private final static String DEFAULT_CUSTOM_HELPSET_NAME = "helpset.hs";
	private final static String DEFAULT_GUIBUILDER_HELPSET_NAME = "Help.hs";

	/**
	 * Der Helpmanager versucht zuerst ein Helpset mit dem Namen "helpset.hs" zu laden.
	 * Schl�gt dies fehl, versucht er "help.hs" zu laden. Wenn eine von beiden gefunden wird,
	 * wird der javax.help.DefaultHelpBroker damit initialisiert. Es wird dabei sowohl 
	 * im Classpath, als auch im Filesystem nach dem Helpset gesucht.
	 * 
	 * Sollen eigene Helpsets mit dem GuiBuilder verwendet werden, m�ssen diese "helpset.hs" 
	 * hei�en. Andernfalls besteht die Gefahr, dass der GuiBuilder, das in seiner eigenen 
	 * Auslieferung enthaltene "Help.hs" zuerst findet und l�d.
	 * 
	 * @author peter, thomas
	 */
	public JavaHelpManager() {

		/*
		 * Zuerst wird versucht, das Helpset mit dem Namen "helpset.hs" zu
		 * laden. Nur wenn dies nicht gelingt, wird mit "Help.hs" weiter
		 * versucht. Um also eigene Helpsets verwenden zu k�nnen, m�ssen diese
		 * als "helpset.hs" im Classpath liegen.
		 */
		URL url = getHelpsetUrl(DEFAULT_CUSTOM_HELPSET_NAME);
		if (url != null) {
			// jetzt sollte ein Helpset gefunden sein, dann wird es
			// initialisiert
			this.helpBroker = getHelpBroker(url);
		}
		
		if (url == null) {
			// wenn noch kein Helpset gefunden wurde, dann wird
			// das Standard-Guibuilder Helpset geladen
			url = getHelpsetUrl(DEFAULT_GUIBUILDER_HELPSET_NAME);
			this.helpBroker = getHelpBroker(url);
		}		
	}
	
	private DefaultHelpBroker getHelpBroker(URL hsUrl) {
		HelpSet hs = null;
		DefaultHelpBroker ret = null;
		if (hsUrl != null) {
			// jetzt sollte ein Helpset gefunden sein, dann wird es
			// initialisiert
			try {
				hs = new HelpSet(this.getClass().getClassLoader(), hsUrl);
				ret = new DefaultHelpBroker(hs);
				logger.info("JavaHelpManager: Successfully initialized HelpBroker.");
				System.out.println("JavaHelpManager: Successfully initialized HelpBroker.");
			} catch (Exception ex) {
				logger.error("JavaHelpManager: Failed to initialize HelpBroker.");
	      logger.error(ex.getMessage(), ex);
				System.err.println("JavaHelpManager: Failed to initialize HelpBroker.");
				System.err.println(ex.getMessage());
			}
		}
		return ret;
	}
	
	/**
	 * Versucht ein Helpset mit dem übergebenen Namen zu laden. (Dabei wird
	 * sowohl über den Classpath, als auch über das Dateisystem versucht.
	 * 
	 * @param hs
	 *            Name des zu landenden Helpsets
	 * @return URL des gefundenen Helpsets oder null, wenn es nicht gefunden
	 *         wurde
	 */
	private URL getHelpsetUrl(String hs) {
		URL url = null;
		String logtext = null;
		// hier wird versucht das helpset aus dem classpath zu laden
		try {
			logtext = "load helpset.hs from classpath";
			url = Thread.currentThread().getContextClassLoader()
					.getResource(hs);
		} catch (Exception ex3) {
			logger.error("JavaHelpManager: Faild to load " + hs
					+ " from classpath.");
			logger.error(ex3);
		}

		// Und noch ein paar kreativere Methoden zum Laden, damit es auch in
		// komplexeren
		// Umgebungen, wie JavaWebStart klappt
		if (url == null) {
			logtext = "found " + hs + " via ClassLoader  from classpath";
			url = HelpSet.findHelpSet(this.getClass().getClassLoader(),
					hs);
		}

		if (url == null) {
			logtext = "found " + hs + " via SystemClassloader from classpath";
			url = HelpSet.findHelpSet(ClassLoader.getSystemClassLoader(), hs);
		}

		if (url == null) {
			logtext = "loaded " + hs + " as resource";
			url = this.getClass().getResource(hs);
		}

		if (url == null) {
			logtext = "loaded /" + hs + " as resource";
			url = this.getClass().getResource("/" + hs);
		}

		if (url == null) {
			logtext = "loaded " + hs + " as SystemResource";
			url = ClassLoader.getSystemResource(hs);
		}

		if (url == null) {
			logtext = "loaded /" + hs + " as SystemResource";
			url = ClassLoader.getSystemResource("/" + hs);
		}

		// das ganze auch nochmal über das Dateisystem
		if (url == null) {
			logtext = "loaded " + hs + " from filesystem";
			try {
				File f = new File(hs);
				if (f.exists()) {
					url = f.toURL();
				}
			} catch (Exception ex1) {
				logger.error("JavaHelpManager: Failed to load " + hs
						+ " from filesystem.");
				logger.error(ex1);
			}
		}

		if (url != null) {
			logger.info("JavaHelpManager: Successfull " + logtext + " at URL "
					+ url.toExternalForm() + ".");
		} else {
			// wenn noch immer kein Helpset gefunden wurde, Fehler loggen.
			logger
					.info("JavaHelpManager: Unable to load " + hs + " from filesystem.");
			logger.warn("JavaHelpManager: No Helpset loaded.");
		}

		return url;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.guibuilder.framework.HelpManagerIF#setHelpId(de.guibuilder.framework
	 * .MemberAble, java.lang.String)
	 */
	public void setHelpId(MemberAble member, String helpId) {
		Component comp = member.getAwtComponent();
		if (comp != null) {
			CSH.setHelpIDString(comp, helpId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.guibuilder.framework.HelpManagerIF#getHelpId(de.guibuilder.framework
	 * .MemberAble)
	 */
	public String getHelpId(MemberAble member) {
		Component comp = member.getAwtComponent();
		if (comp != null) {
			String helpId = CSH.getHelpIDString(comp);
			return helpId;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.guibuilder.framework.HelpManagerIF#showHelp(de.guibuilder.framework
	 * .MemberAble)
	 */
	public void showHelp(MemberAble member) {
		if (this.helpBroker != null) {
			GuiWindow parent = GuiSession.getInstance().getCurrentWindow();
			Window win = parent.getWindow();
			if (win != null) {
				((DefaultHelpBroker) helpBroker).setActivationWindow(parent
						.getWindow());
			}
			String helpId = this.getHelpId(member);
			this.showHelp(helpId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.guibuilder.framework.HelpManagerIF#showHelp(java.lang.String)
	 */
	public void showHelp(String helpId) {
		if (helpId != null) {
			try {
				this.helpBroker.setCurrentID(helpId);
			} catch (Exception ex) {
				System.out.println("JavaHelpMenager#showHelp: "
						+ ex.getMessage());
	      logger.error(ex.getMessage(), ex);
			}
			this.helpBroker.setDisplayed(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.guibuilder.framework.HelpManagerIF#enableContextHelp(de.guibuilder
	 * .framework.GuiAction)
	 */
	public void enableContextHelp(GuiAction action) {
		try {
			action.getAbstractButton().addActionListener(
					new CSH.DisplayHelpAfterTracking(this.helpBroker));
		} catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
			System.err.println("JavaHelpManager: Cannot enable Help");
		}
	}
}