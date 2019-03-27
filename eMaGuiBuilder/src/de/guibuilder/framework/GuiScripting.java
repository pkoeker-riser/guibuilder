package de.guibuilder.framework;

import de.guibuilder.framework.event.GuiUserEvent;
/**
 * Abstracte Klasse für installierte Scriptsprachen.<p>
 * Das könnte hier eigentlich auch ein Interface sein.<p>
 * Scripte werden von der Factory eingelesen und
 * @see GuiContainer#setContext
 */
abstract class GuiScripting {

	/**
	 * Methode des Scripts aufrufen.
	 * @param cmd ActionCommand = aufzurufende Methode
	 * @param event UserEvent
	 * @return boolean true, wenn keine Fehler
	 */
	abstract GuiInvokationResult invokeScripting(String cmd, GuiUserEvent event);
	
}  
