/*
 * Created on 26.04.2005
 */
package de.guibuilder.framework.event;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiPopupMenu;
import de.guibuilder.framework.GuiWindow;

/**
 * Dieses Ereignis wird ausgelöst, wenn der Benutzer mit der
 * rechten Maustaste ein KontextMenü aufgerufen hat.
 * @author peter
 */
public class GuiPopupEvent extends GuiUserEvent {
	public GuiPopupMenu popupMenu;
	public GuiPopupEvent(GuiWindow window, GuiMember member, GuiPopupMenu popupMenu) {
		super(window, member);
		this.popupMenu = popupMenu;
	}
	public int getEventType() {
		return POPUP_SHOW;
	}
}
