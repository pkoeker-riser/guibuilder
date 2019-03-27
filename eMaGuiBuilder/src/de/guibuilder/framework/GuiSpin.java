package de.guibuilder.framework;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.pkjs.util.Convert;
/**
 * Implementierung einer SpinBox.
 */
public final class GuiSpin extends GuiMinMax {
  // Attributes
  protected JSpinner component;
  protected static final int guiType = GUI_COMPONENT;
  protected SpinnerNumberModel model;
  // Constructors
  /**
   * Erzeugt eine SpinnBox mit einem angegebenen Namen.<p>
   * Der Wertebereich ist per default 0 - 100.<br>
   * Ist der eingestellte Wert gleich dem Minimum,
   * wird null geliefert (so, als währe das Feld leer).<br>
   * Ist das Feld mit nn="true" gekennzeichnet, wird im
   * Unterschied dazu in diesem Fall der minimale Wert geliefert.
   * @see #setMinimum
   * @see #setMaximum
   */
  public GuiSpin(String title) {
    super(title);
    this.guiInit();
  }
  // Methods
  private void guiInit() {
    component.setPreferredSize(new Dimension(40, component.getMinimumSize().height));
    //component.setHorizontalAlignment(SwingConstants.RIGHT);
    if (this.getName().length() == 0) {
      this.setName("spin");
    }
    model = (SpinnerNumberModel)component.getModel();
    this.setMinimum(0);
    this.setMaximum(100);
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
          if (getRootPane() != null) {
            if (actionChange != null) {
              getRootPane().obj_ItemChanged(GuiSpin.this, actionChange, getValue());
            }
            //pane.obj_ItemChanged(GuiSpin.this, getValue());
          }
        }
      }
    });
  }
  public JComponent getJComponent() {
    if (component == null) component = new JSpinner();
    return component;
  }
  public final String getTag() {
    return "Spin";
  }
  /**
   * Setzt den Spinner; es darf ein String mit Ziffern oder ein Integer übergeben werden.
   * Aus "null" wird Minimum.
   */
  public void setValue(Object val) {
    if (val == null) {
      val = model.getMinimum();
    } else {
       val = Convert.toInt(val);
    }
    model.setValue(val);
//    if (val instanceof String) {
//      model.setValue(new Integer((String)val));
//    }
//    else if (val instanceof Integer) {
//      model.setValue(val);
//    }
    this.setModified(false);
  }
  
  public Object getUnformatedValue()
  {
	return getValue();
  }
  
  /**
   * Liefert ein Integer oder null, wenn Minimum und nn='false'.
   */
  public Object getValue() {
    Integer val = (Integer)model.getValue();
    if (val.equals(model.getMinimum()) && isNotnull() == false) {
      val = null;
    }
    return val;
  }
  /**
   * Setzt den Wert auf Minimum zurück.
   * @see #getMinimum
   */
  public void reset() {
    model.setValue(model.getMinimum());
    //model.setValue(null);
    this.setModified(false);
  }
  public int getMinimum() {
    Integer ii = (Integer)model.getMinimum();
    return ii.intValue();
  }
  public void setMinimum(int i) {
    Integer ii = new Integer(i);
    model.setMinimum(ii);
  }
  public int getMaximum() {
    Integer ii = (Integer)model.getMaximum();
    return ii.intValue();
  }
  /**
   * Setzt den maximalen Wert; es wird gleichzeitig in Abhängigkeit davon
   * die Breite der Komponente berechnet.
   */
  public void setMaximum(int i) {
    Integer ii = new Integer(i);
    String s = ii.toString();
    component.setMinimumSize(new Dimension(25 + s.length() * 8, component.getMinimumSize().height));
    model.setMaximum(ii);
  }
  public JSpinner getSpinner() {
    return component;
  }
}