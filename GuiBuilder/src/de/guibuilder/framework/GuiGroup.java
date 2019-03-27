package de.guibuilder.framework;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
/**
 * Von GuiPanel abgeleitete Klasse, die einen
 * grafisch sichtbaren Container implementiert.
 * Der (beschriftete) Rahmen kann erhaben oder versenkt sein.
 */
public class GuiGroup extends GuiPanel {
  // Constructor
  /**
   * Erzeugt einen oben links beschrifteten Rahmen.
   * @param title Die Beschriftung des Rahmens.
   */
  public GuiGroup (String title) {
    super(title);
    component.setBorder(BorderFactory.createTitledBorder(title));
  }
  // Methodes
  public final String getTag() {
    return "Group";
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
      throw new IllegalArgumentException("GuiGroup.setBorder: Argument must be LOW or RAISE");
    }
  }
  /**
   * Setzt den Font des Rahmens
   */
  public final void setBorderFont(Font f) {
    final TitledBorder tB = (TitledBorder)component.getBorder();
    tB.setTitleFont(f);
  }
  /**
   * Liefert den Font des Rahmens
   * Bug in Java7: getTitledFont kann null liefern!
   */
  public final Font getBorderFont() {
    final TitledBorder tB = (TitledBorder)component.getBorder();
    return tB.getTitleFont();
  }
  /**
   * Setzt das Label der Border neu
   */
  public void setLabel(String label) {
    super.setLabel(label);
    if (component == null) return;
    Border b = component.getBorder();
    if (b instanceof TitledBorder) {
      TitledBorder tb = (TitledBorder)b;
      tb.setTitle(label);
      component.repaint();
    } 
  }
}