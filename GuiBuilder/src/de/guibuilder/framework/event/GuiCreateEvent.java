/*
 * Created on 08.11.2003
 */
package de.guibuilder.framework.event;

import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiWindow;

/**
 * Dieses Ereignis wird ausgelöst, wenn die GuiFactory diese
 * Komponente erzeugt hat.
 * Keyword-Attribute OnCreate="[CreateEvent]"
 */
public final class GuiCreateEvent extends GuiUserEvent {
	/**
	 * Die erzeugte Komponente.
	 */
	public final GuiComponent component;
	// Constrcutor
	public GuiCreateEvent(GuiWindow win, GuiComponent comp) {
		super(win, comp);
		this.component = comp;
	}
	public int getEventType() {
	  return CREATE;
	}
}
