package de.guibuilder.framework.event;

import de.guibuilder.framework.GuiTable;
import de.guibuilder.framework.GuiTableRow;
import de.guibuilder.framework.GuiWindow;

/**
 * Der Benutzer hat ein Ereignis in Zusammenhang mit einer Tabelle ausgelöst.<p>
 * Anhand von eventType ist ersichtlich, welches Ereignis eingetreten ist:
 * HEADER_CLICK, DBL_CLICK oder ROW_CLICK
 */
public final class GuiTableEvent extends GuiUserEvent {
  public enum EventType {HEADER_CLICK, DBL_CLICK, ROW_CLICK};
  public EventType eventSubType;
  public transient GuiTable table;
  /**
   * Wenn -2, dann kein Index; ansonsten der angeklickte Zeilen- oder Spalten-Nummer.
   */
  public int index = -2;
  /**
   * Die angeklickte Tabellen-Zeile bei DBL_CLICK und ROW_CLICK
   */
  public GuiTableRow value;

  public GuiTableEvent(GuiWindow win, GuiTable tbl, EventType type) {
    super(win, tbl);
    eventSubType = type;
    table = tbl;
  }
  public final int getEventType() {
    return TABLE;
  }
  public GuiTable getTable() {
     return this.table;
  }
}