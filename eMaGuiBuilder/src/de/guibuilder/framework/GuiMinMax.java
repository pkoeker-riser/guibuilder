package de.guibuilder.framework;

/**
 * Abstrakte Basisklasse für ScrollBar, Slider und Spin.<br>
 * Es kann ein Minimal- und ein Maximalwert eingestellt werden,
 * den diese Komponente repräsentiert.<br>
 * Der Maximalwert muß größer als der Minimalwert sein.
 * @see GuiSlider
 * @see GuiScrollBar
 * @see GuiSpin
 */
public abstract class GuiMinMax extends GuiComponent {
  public GuiMinMax(String label) {
    super(label);
  }
  public final int getDataType() {
    return INTEGER;
  }
  /**
   * Liefert den Minimalwert.
   */
  public abstract int getMinimum();
  /**
   * Setzt den Minimalwert.
   */
  public abstract void setMinimum(int i);
  /**
   * Liefert den Maximalwert.
   */
  public abstract int getMaximum();
  /**
   * Setzt den Maximalwert.
   */
  public abstract void setMaximum(int i);
}