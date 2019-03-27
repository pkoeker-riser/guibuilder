package de.guibuilder.framework;

import de.guibuilder.framework.event.GuiUserEvent;

/**
 * Dieses Interface ist von einem Controller zu implementieren, wenn alle
 * vom Benutzer ausgelösten Ereignisse an eine einzige Methode weitergereicht werden sollen.
 * @see de.guibuilder.adapter.ThinClientAdapter
 */
public interface UserActionIF {
  /**
   * Alle Benutzerereignisse rufen diese Methode auf.
   */
  public void userActionPerformed(GuiUserEvent event);
}