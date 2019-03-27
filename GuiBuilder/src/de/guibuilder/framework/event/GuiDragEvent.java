/*
 * Created on 09.10.2004
 */
package de.guibuilder.framework.event;

import java.awt.dnd.DropTargetEvent;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiWindow;
/**
 * Implementierung eines Drag-Ereignisses.
 * Anhand des Event-Types kann festgestellt werden,
 * ob es sich um eine DRAG_ENTER, OVER, oder EXIT Ereignis handelt.
 * @author peter
 */
public class GuiDragEvent extends GuiUserEvent {
	public DropTargetEvent event;
	private int eventType = DRAG_ENTER;

	// Constructor
	public GuiDragEvent(GuiWindow win, GuiMember member, DropTargetEvent event, int type) {
		super(win, member);
		this.event = event;
		this.eventType = type;
	}
	
	public int getEventType() {
		return eventType;
	}
}



