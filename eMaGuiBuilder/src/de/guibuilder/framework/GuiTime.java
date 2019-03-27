package de.guibuilder.framework;

import java.awt.Dimension;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingConstants;

import de.pkjs.util.*;

/**
 * Implementierung einer Uhrzeiteingabe mit Überprüfung der eingegebenen Uhrzeit.
 */
public class GuiTime extends GuiText {
  // Attributes
  /**
   * Das Uhrzeitformat wird per Default auf "HH:mm" gesetzt.<p>
   * Ein abweichendes Standard-Format kann bei "GuiBuilder.properties" unter "TimeFormat"
   * gesetzt werden.
   */
  private static SimpleDateFormat defaultFormat = new SimpleDateFormat("HH:mm");
  private SimpleDateFormat format;
   // Constructor
  public GuiTime() {
    this("time");
  }
  public GuiTime(String label) {
    super(label);
    this.guiInit();
  }
  // Methods
  private void guiInit() {
    this.format = defaultFormat;
    component.setColumns(4);
    this.setMaxlen(5); // Veranlaßt Registrierung des Input-Verifiers!
    this.setMinimumSize(new Dimension(50, this.getMinimumSize().height));
    this.setMaximumSize(new Dimension(50, this.getMaximumSize().height));
    component.setHorizontalAlignment(SwingConstants.CENTER);
  }
  public final String getTag() {
    return "Time";
  }
  public final int getDataType() {
    return TIME;
  }
  /**
   * Liefert das interne Datumsformat.
   */
  public SimpleDateFormat getFormat() {
    return format;
  }
  /**
   * Setzt die Formatangabe für dieses Feld; siehe java.text.SimpleDateFormat
   * @see #setDefaultFormat
   */
  public void setFormat(String pattern) {
    format = new SimpleDateFormat(pattern);
    if (this.getMaxlen() < pattern.length()) {
        this.setMaxlen(pattern.length());
        component.setColumns(6);
        component.setMinimumSize(new Dimension(60, component.getMinimumSize().height));
    }
  }
  // From GuiText
  public String makeFormat(String txt) throws ParseException {
    Date value;
    if (txt.equals("NOW")) {
      value = new Date();
    } else {
    	if (txt.lastIndexOf(":")+1 == txt.length()) {
    		txt = txt +"00";
    	} else {
    		if (txt.indexOf(":") == -1 && (txt.length() == 1 || txt.length() == 2)) {
    			txt = txt + ":00";
    		}    		
    	}
      value = format.parse(txt);
    }
    String ret = format.format(value);
    return ret;
  }
  /**
   * Overlay; wenn "NOW" übergeben wird, wird die aktuelle Uhrzeit gesetzt.
   */
  public final void setText(String s) {
    if (s != null && s.equals("NOW")) {
      Date date = new Date();
      String sd = format.format(date);
      super.setText(sd);
    }
    else {
      super.setText(s);
    }
  }
  /**
   * Setzt das Standard-Uhrzeit-Format.
   */
  public static void setDefaultFormat(String f) {
    defaultFormat = new SimpleDateFormat(f);
  }
  /**
   * Liefert das Default-Format
   */
  public static SimpleDateFormat getDefaultFormat() {
    return defaultFormat;
  }
  public Class getValueClass() {
    return Date.class;
  }
//  /**
//   * Liefert die Zeit als String.
//   * Wenn nichts eingegeben, wird null geliefert.
//   */
//  public Object getValue() {
//		String value = this.getText();
//		if (value.length() == 0) {
//			value = null;
//		}
//		return value;
//  }
  public java.sql.Time getValueTime() {
  	String txt = component.getText();
  	return Convert.toTime(txt);
  }
}