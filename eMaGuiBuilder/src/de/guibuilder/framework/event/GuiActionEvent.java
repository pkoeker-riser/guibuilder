package de.guibuilder.framework.event;

import java.awt.event.ActionEvent;

import de.guibuilder.framework.GuiAction;
import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiMenuItem;
import de.guibuilder.framework.GuiPopupMenu;
import de.guibuilder.framework.GuiWindow;

/**
 * Von einem MenuItem oder einem Button ausgelöster ActionEvent.
 * @since 0.9.3d
 */
public final class GuiActionEvent extends GuiUserEvent {
  /**
   * Das MenuItem oder der Button, der dieses Ereignis augelöst hat.
   */
  public transient GuiAction action;
  /**
  * Das awt.event
  */
  public ActionEvent actionEvent;
  /**
   * Wenn PopupMenu, dann steht hier der Invoker, sonst null.
   * Der Invoker ist die Komponente, dem das PopupMenu zugeordnet ist
   * (z.B. Table oder Tree).
   */
  public GuiMember invoker;

  public GuiActionEvent(GuiWindow win, GuiAction a, ActionEvent e) {
    super(win, a);
    action = a;
    actionEvent = e;
    // Popup? --> Invoker versorgen
    if (action instanceof GuiMenuItem) {
      GuiMenuItem item = (GuiMenuItem)action;
      if (item.getGuiMenu() instanceof GuiPopupMenu) {
        GuiPopupMenu pop = (GuiPopupMenu)item.getGuiMenu();
        this.invoker = pop.getGuiInvoker();
      }
    }
  }
  // From GuiUserEvent
  public final int getEventType() {
    return ACTION;
  }
  public GuiAction getAction() {
     return this.action;
  }
}