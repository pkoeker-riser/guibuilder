package de.guibuilder.framework.event;

import de.pkjs.util.Convert;
import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiWindow;

/**
 * Der Benutzer hat den Inhalt eine Komponente verändert; dieses geschieht bei
 * Check, Option, Combo und List unmittelbar durch das Anklicken;
 * bei Text, Memo und Editor erst beim LostFocus.<p>
 * Dieses Ereignis wird nur dann ausgelöst, wenn sich der Inhalt wirklich verändert hat.
 * @since 0.9.3d
 */
public class GuiChangeEvent extends GuiUserEvent {
  /**
   * Die Komponente, deren Inhalt vom Benutzer verändert wurde.
   */
  public transient final GuiComponent component;
  /**
   * Der neue Inhalt der Komponente.
   * @see de.guibuilder.framework.GuiComponent#getDataType
   */
  public final Object value;
  /**
   * Konvertierung des Inhalts nach boolean (wenn möglich)
   */
  public boolean bValue;
	/**
	 * Konvertierung des Inhalts nach int (wenn möglich)
	 */
  public int iValue;
	/**
	 * Konvertierung des Inhalts nach long (wenn möglich)
	 */
  public long lValue;
	/**
	 * Konvertierung des Inhalts nach double (wenn möglich)
	 */
  public double dValue;
  /**
   * -2 = Komponente ohne Index;
   * ansonsten der SelectedIndex aus Combo oder List
   * @see de.guibuilder.framework.GuiSelect#getSelectedIndex
   */
  public int index = -2;

  public int row = -2;
  
  public GuiChangeEvent(GuiWindow win, GuiComponent comp, Object val) {
    super(win, comp);
    component = comp;
    value = val;
    if (value != null) {
      bValue = Boolean.valueOf(val.toString()).booleanValue();
      try {
				iValue = Convert.toInt(val.toString());
      } catch (Exception ex) {}
			try {
				lValue = Convert.toLong(val.toString());
			} catch (Exception ex) {}
			try {
				dValue = Convert.toDouble(val.toString());
			} catch (Exception ex) {}
    }
  }
  public int getEventType() {
    return CHANGE;
  }
  public GuiComponent getComponent() {
     return this.component;
  }
  public Object getValue() {
     return this.value;
  }
}