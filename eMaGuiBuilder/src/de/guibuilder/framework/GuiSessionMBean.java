package de.guibuilder.framework;

import java.util.Date;

public interface GuiSessionMBean {

	/**
	 * Liefert den Benutzernamen des angemeldeten Benutzers, <br>
	 * oder "applet", wenn der Benutzername nicht zu ermitteln ist.
	 */
	public abstract String getUsername();

	// Creation Date
	/**
	 * Liefert das Datum, an dem diese Session erzeugt wurde.
	 */
	public abstract Date getDateCreated();

	/**
	 * Liefert eine Eigenschaft unter dem angegebenen Namen.
	 * 
	 * @param key
	 * @return
	 */
	public abstract Object getProperty(String key);

}