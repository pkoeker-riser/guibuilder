package de.guibuilder.framework.event;

import de.guibuilder.framework.GuiWindow;

/**
 * Von einer MessageBox ausgelöster Event.<br>
 * Der Controller wird von dem Fenster übernommen, welches der Parent der
 * MessageBox ist.
 * @since 0.9.3e
 * @see de.guibuilder.framework.GuiRootPane#obj_MessageBoxEvent
 */
public final class GuiMessageBoxEvent extends GuiUserEvent {
  /**
   * Name der MessageBox
   */
  public String name;
  /**
   * Das ActionCommand des vom User betötigten Buttons aus der MessageBox.
   */
  public String actionCommand;
  /**
   * Erzeugt einen MessageBoxEvent
   * @param win Das ParentWindow der MessageBox
   * @param msgName Name der MessageBox
   * @param cmd ActionCommand des auslösenden Buttons.
   */
  public GuiMessageBoxEvent(GuiWindow win, String msgName, String cmd) {
    super(win, null);
    this.name = msgName;
    this.actionCommand = cmd;
  }
  public final int getEventType() {
    return MESSAGE_BOX;
  }
}