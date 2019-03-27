package de.guibuilder.framework;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
/**
 * Nur im Null-Layout verwenden!
 */
public final class GuiBorder extends GuiLabel {

  public GuiBorder(String label) {
    super();
    getJComponent().setBorder(BorderFactory.createTitledBorder(label));

  }
  /**
   * Setzt den Rahmen auf erhaben oder versenkt.
   * Siehe auch das Attribut "type=".
   * @param type Es sind die Werte "LOW" und "RAISE" erlaubt.
   * @throws IllegalArgumentException wenn weder LOW noch RAISE
   */
  public final void setBorder(String type) {
    if (type.equals("LOW")) {
      component.setBorder(new TitledBorder(BorderFactory.createLoweredBevelBorder(), getLabel()));
    } else if (type.equals("RAISE")) {
      component.setBorder(new TitledBorder(BorderFactory.createRaisedBevelBorder(), getLabel()));
    } else {
      throw new IllegalArgumentException("GuiBorder.setBorder: Argument must be LOW or RAISE");
    }
  }
}