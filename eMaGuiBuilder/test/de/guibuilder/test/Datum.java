/*
 * Created on 08.11.2003
 */
package de.guibuilder.test;

import java.util.Date;

import de.guibuilder.framework.GuiDate;

/**
 * @author peter
 */
public class Datum extends GuiDate {
	/**
	 * Verwandert "h" oder "H" in ein heutiges Datum.
	 * Wird bei lostFocus aufgerufen.
	 */
	public void postProc() {
		if (this.getText().equalsIgnoreCase("h")) {
			Date d = new Date();
			this.setValue(d);
		}
	}
}
