package de.guibuilder.framework;

import java.util.Date;
import java.util.Hashtable;

import de.guibuilder.adapter.ComboBoxAdapterIF;
import de.guibuilder.adapter.UserAccessChecker;

/**
 * Defines a Client-Session; singleton.<p>
 * Es wird zwar das Interface HelpManagerIF implementiert,
 * aber alle Aufrufe werden delegiert.<p>
 * Per Default wird der JavaHelpManager verwendet.
 * @see de.guibuilder.help.JavaHelpManager 
 * Ermittelt den Benutzername.<br>
 * Mit set- und getProperty können beliebige Informationen in einer Hashtable verwaltet
 * werden.
 */
public final class GuiSession implements HelpManagerIF, GuiSessionMBean {
	// Attributes
	private static GuiSession me = new GuiSession();
	private ApplicationIF adapter;
	private ComboBoxAdapterIF comboBoxAdapter;
	/*
	 * Benutzerrechte
	 */
	private static UserAccessChecker checker;
	public static UserAccessChecker getChecker() {
      return checker;
   }

   public static void setChecker(UserAccessChecker c) {
      checker = c;
   }

   /**
	 * Der Username wird versucht aus den System Properties zu ermitteln (geht nicht, wenn
	 * dem Applet der Zugriff verwehrt ist).
	 */
	private String username = "<unknown>";
	//private String computername = "<unknown>";
	private GuiWindow currentWindow;
	private Date dateCreated = new Date();
	private Hashtable<String, Object> properties = new Hashtable<String, Object>();
	private HelpManagerIF helpManager;

	// private Constructor
	private GuiSession() {
		System.out.println("Session created: " + dateCreated.toString());
		try {
			this.username = System.getProperty("user.name");
			System.out.println("User Name: " + username);
		} catch (Exception ex) {
			//System.out.println("Warning: No User Name set; may be Applet");
		}
		
		try {
			Class.forName("javax.help.SearchHit");
		} catch (Throwable ex) {
			System.err.println("JavaHelp: Search not enabled " + ex.getMessage());
		}
		try {
			Class.forName("javax.help.HelpBroker");
		} catch (Throwable ex) {
			System.err.println("JavaHelp: Missing HelpBroker " + ex.getMessage());
		}
		// Default Help Manager
		String className = "de.guibuilder.help.JavaHelpManager";
		try {
			className = GuiUtil.getConfig().getRow().getValue("HelpManager");
		} catch (Throwable ex) {
			System.err.println(ex.getMessage());
		}
		try {
			Class hmc = Class.forName(className);
			Object o = hmc.newInstance();
			this.helpManager = (HelpManagerIF)o;
			System.out.println("HelpManager loaded: "+className);
		} catch (Throwable ex) {
			System.err.println("GuiBuilderHelp: Missing HelpManager " +ex.getMessage());
		}
	}

	// Methods
	/**
	 * Liefert eine Instanz der Client Session
	 */
	public static GuiSession getInstance() {
		return me;
	}

	// Adapter
	/**
	 * Setzt den Application Adapter für den Zugriff auf einen Application Server see
	 * ApplicationAdpater in guibuilder.properties
	 */
	public ApplicationIF getAdapter() {
		return adapter;
	}

	/**
	 * Liefert den Apdater zum Application Server oder null wenn keiner gesetzt. see
	 * ApplicationAdpater in guibuilder.properties
	 */
	public void setAdapter(ApplicationIF ada) {
		this.adapter = ada;
	}

	/* (non-Javadoc)
	 * @see de.guibuilder.framework.GuiSessionMBean#getUsername()
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Setzt den Benutzernamen für diese Client Session
	 * 
	 * @param name
	 */
	public void setUsername(String name) {
		this.username = name;
	}

	// Creation Date
	/* (non-Javadoc)
	 * @see de.guibuilder.framework.GuiSessionMBean#getDateCreated()
	 */
	public Date getDateCreated() {
		return dateCreated; // findbugs: EI
	}

	/* (non-Javadoc)
	 * @see de.guibuilder.framework.GuiSessionMBean#getProperty(java.lang.String)
	 */
	public Object getProperty(String key) {
		return properties.get(key);
	}

	/**
	 * Setzt eine Eigenschaft unter dem angegebenen Namen.
	 * 
	 * @param key
	 * @param value
	 *           Den vorigen Wert dieser Eigenschaft oder null, wenn neue Eigeschaft.
	 * @return
	 */
	public Object setProperty(String key, Object value) {
		return properties.put(key, value);
	}

	/**
	 * Liefert das aktuell aktivierte Fenster.
	 * 
	 * @see GuiWindowListener#windowActivated
	 * @see GuiWindow#show
	 */
	public GuiWindow getCurrentWindow() {
		return currentWindow;
	}

	/**
	 * Setzt das aktuell aktivierte Fenster.
	 * 
	 * @see GuiWindowListener#windowActivated
	 * @see GuiWindow#show
	 */
	public void setCurrentWindow(GuiWindow win) {
		currentWindow = win;
	}

	/**
	 * @return Returns the helpManager.
	 */
	public HelpManagerIF getHelpManager() {
		return this.helpManager;
	}

	/**
	 * @param helpManager
	 *           The helpManager to set.
	 */
	public void setHelpManager(HelpManagerIF helpManager) {
		this.helpManager = helpManager;
	}
	public void setHelpId(MemberAble member, String helpId) {
		if (this.helpManager != null) {
			this.helpManager.setHelpId(member, helpId);
		}
	}
	public String getHelpId(MemberAble member) {
		if (this.helpManager != null) {
			return this.helpManager.getHelpId(member);
		} else {
			return null;
		}
	}
	public void showHelp(MemberAble member) {
		if (this.helpManager != null) {
			this.helpManager.showHelp(member);
		}
	}
	public void showHelp(String helpId) {
		if (this.helpManager != null) {
			this.helpManager.showHelp(helpId);
		}		
	}
	public void enableContextHelp(GuiAction action) {
		if (this.helpManager != null) {
			this.helpManager.enableContextHelp(action);
		}	
	}

	public ComboBoxAdapterIF getComboBoxAdapter() {
		return comboBoxAdapter;
	}

	public void setComboBoxAdapter(ComboBoxAdapterIF comboBoxAdapter) {
		this.comboBoxAdapter = comboBoxAdapter;
	}
	
}