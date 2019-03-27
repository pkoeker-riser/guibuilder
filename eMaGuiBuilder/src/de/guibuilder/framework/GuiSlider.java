package de.guibuilder.framework;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.pkjs.util.*;

/**
Implementierung eines Schiebereglers.
*/
public final class GuiSlider extends GuiMinMax {
  // Attributes
  private JSlider component;
  // Constructors
  /**
  Erzeugt einen horizontalen Schiebregler mit einem angegebenen Namen.
  */
  public GuiSlider(String title) {
    super(title);
    this.guiInit();
  }
  // Methods
  private void guiInit() {
    if (this.getName().length() == 0) {
      this.setName("slider");
    }
    this.addFocusListener(new GuiFocusListener(this));
    component.addChangeListener(new ChangeListener() {
      // Nested Change Listener
      public void stateChanged(ChangeEvent e) {
      	postProc();
        setModified(true);
        // Inhalt an verknüpfte Spalte weiterreichen?
        if (linkTable != null) {
          linkTable.setCellValue(linkColumn, getValue());
        }
        // Externen Controller informieren
        if (actionChange != null) {
          getRootPane().obj_ItemChanged(GuiSlider.this, actionChange, getValue());
        }
      }
    });
  }
  public final String getTag() {
    return "Slider";
  }
  public JComponent getJComponent() {
    if (component == null) {
    	component = new JSlider();
      component.setPaintTicks(true);
    }
    return component;
  }
  /**
  Setzt den Schieberegler; es darf ein String mit Ziffern oder ein Integer übergeben werden.
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
    this.setModified(false);
  }
  
  public Object getUnformatedValue() {
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
  /**
   * Liefert die Swing-Componente
   * @return
   */
  public JSlider getSlider() {
    return component;
  }
}