package de.guibuilder.framework;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.pkjs.util.*;

/**
Diese Komponente kann auch wie ein Schieberegler eingesetzt werden.
@see GuiSlider
*/
public final class GuiScrollBar extends GuiMinMax	{
  // Attributes
  private JScrollBar component;
  /**
   * Die alte Position merken, um ein Changed Ereignis auszulösen.
   */
  private int oldValue = 0;
  // Constructor
  /**
  Erzeugt eine vertikale Scrollbar.
  */
  public GuiScrollBar(String title) {
    super(title);
    //this.setBlockIncrement(3); // ##
    if (this.getName().length() == 0) {
      this.setName("scrollbar");
    }
    component.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
    this.setPreferredSize(new Dimension(22,48));
    //this.setBackground(new Color(230, 230, 230)); // geht nicht

    // Change Listener einrichten
    component.getModel().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      	postProc();
        // Externen Controller informieren
        if (oldValue != component.getValue()) {
          setModified(true);
          oldValue = component.getValue();
          if (actionChange != null && getRootPane() != null) {
            getRootPane().obj_ItemChanged(GuiScrollBar.this, actionChange, getValue());
          }
        }
      }
    }); // End ChangeListener
  }
  // Methods
  public final String getTag() {
    return "ScrollBar";
  }
  public JComponent getJComponent() {
    if (component == null) component = new JScrollBar(SwingConstants.VERTICAL);
    return component;
  }
  /**
  Setzt die Scrollbar auf den übergebenen Wert;
  es darf ein String mit Ziffern oder ein Integer übergeben werden.
  <BR>
  Die übergabe von "null" wird als "0" gewertet.
  */
  public void setValue(Object val) {
    if (val == null) {
      val = "0";
    }
    if (val instanceof String) {
      component.setValue(Convert.toInt((String)val));
    }
    else {
      component.setValue(((Integer)val).intValue());
    }
    oldValue = component.getValue();
    this.setModified(false);
  }
  
  public Object getUnformatedValue()
  {
	return new Integer(component.getValue());
  }
  
  /**
  Liefert ein Integer.
  */
  public Object getValue() {
    return new Integer(component.getValue());
  }
  public void reset() {
    component.setValue(0);
    this.setModified(false);
  }
  public int getMinimum() {
    return component.getMinimum();
  }
  public void setMinimum(int i) {
    component.setMinimum(i);
  }
  public int getMaximum() {
    return component.getMaximum();
  }
  public void setMaximum(int i) {
    component.setMaximum(i);
  }
  public JScrollBar getBar() {
    return component;
  }
}