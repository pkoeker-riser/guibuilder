package de.guibuilder.framework;

/**
 * <p>Title: GuiBuilder</p>
 * <p>Description: Diese Exception wird geworfen, wenn das GDL-Script einen Syntax-Fehler hat</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Peter Köker
 * @version 1.0
 */
public class GDLParseException extends Exception {
   private static final long serialVersionUID = 1L;
   private int errLine = -1;
  // Constructor
  public GDLParseException(String message) {
  	super(message);
  }
  public GDLParseException(String message, int errorLine) {
    super(message + "\nLine: " + Integer.toString(errorLine));
    this.errLine = errorLine;
  }
  /**
   * Liefert die fehlerhafte Zeile
   */
  public int getErrorLine() {
    return errLine;
  }
}