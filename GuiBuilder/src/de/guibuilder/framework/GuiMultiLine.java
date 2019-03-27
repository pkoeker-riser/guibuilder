package de.guibuilder.framework;

/**
 * Abstracte Basisklasse für mehrzeilige Eingaben.
 */
abstract class GuiMultiLine extends GuiComponent {
  // Attributes
  // Methods
  /**
   * Liefert MULTILINE
   */
  public final int getDataType() {
    return MULTILINE;
  }
  /**
   * Delegation
   */
  public abstract String getText();
  /**
   * Delegation
   */
  public abstract void setText(String s);
}