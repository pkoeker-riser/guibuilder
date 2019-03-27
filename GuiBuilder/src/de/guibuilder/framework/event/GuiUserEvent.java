package de.guibuilder.framework.event;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiWindow;

/**
 * Abstrakte Basis-Klasse für alle vom Benutzer ausgelösten Ereignisse.
 * @since 0.9.3d
 */
public abstract class GuiUserEvent {
  public static final int ACTION = 0;
  public static final int CHANGE = 1;
  public static final int CLICK = 20;
  public static final int DBL_CLICK = 2;
  public static final int KEY = 3;
  public static final int LOST_FOCUS = 4;
  public static final int MESSAGE_BOX = 5;
  public static final int MOUSE_MOVED = 6;
  public static final int MOUSE_OVER = 7;
  public static final int TABLE = 8;
  public static final int TAB = 9;
  public static final int TREE = 10;
  public static final int NODE_CHANGE = 11;
  public static final int WINDOW = 12;
  public static final int CREATE = 13;
  public static final int DROP = 14;
  public static final int DRAG_ENTER = 15;
  public static final int DRAG_OVER = 16;
  public static final int DRAG_EXIT = 17;
  public static final int POPUP_SHOW = 18;
  
  public static final int FILEDROP = 24;
  
  public static final int TIMER = 30;
  /**
   * Verweis auf das Fenster, aus welchem heraus das Ereignis ausgelöst wurde.
   */
  public transient GuiWindow window;
  /**
   * Verweis auf den auslösenden Member; dieser ist null
   * bei WindowEvent und MessageBoxEvent
   */
  public transient GuiMember member;
  /**
   * Teilt dem Framework mit, die weitere Verarbeitung abzubrechen
   * (z.B. das Fenster nicht zu schließen)
   */
  public boolean cancel;
  /**
   * @see de.guibuilder.framework.GuiRootPane
   */
  public GuiUserEvent(GuiWindow win, GuiMember mem) {
    this.window = win;
    this.member = mem;
  }
  /**
   * Liefert den jeweiligen Event-Typ;
   * dieses soll Ketten von <code>if (... instanceof ...) else if ...</code> vermeiden.<p>
   * <code><br>
   * <pre>
   * public final void userActionPerformed(GuiUserEvent event) {
    *    switch (event.getEventType() ) {
      *    case GuiUserEvent.ACTION:
      *       GuiActionEvent action = (GuiActionEvent)event;
      *       ...
      *       break;
      *    case GuiUserEvent.CHANGE:
      *       GuiChangeEvent change = (GuiChangeEvent)event;
      *       ...
      *       break;
      *    ...
      *    }
      * }
      * </pre>
      * </code>
    *
   */
  public abstract int getEventType();
  
  public GuiWindow getWindow() {
     return this.window;
  }
  public GuiMember getMember() {
     return this.member;
  }
}