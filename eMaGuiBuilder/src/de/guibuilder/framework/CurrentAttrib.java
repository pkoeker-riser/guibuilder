package de.guibuilder.framework;

import java.awt.GridBagConstraints;
import java.text.NumberFormat;
/**
 * Ein Objekt dieser Klasse kann ein Attribut eines Keywords halten.
 * @see CurrentKeyword#vAttrib
 * @see de.guibuilder.design.Generator
 * @see de.guibuilder.design.GuiDoc
 */
public final class CurrentAttrib {
  // Attributes
  /**
   * AttributName
   */
  public String sKeyword;
  /**
   *  AttributId für Factory.
   */
  public Integer iKeyword;
  /**
   *  String-Wert des Attributes.
   */
  public String sValue;
  /**
   *  Integer-Wert des Attributes.
   */
  int iValue;
  /**
   * Float-Wert des Attributes; für wx und wy
   */
  float fValue;
  /**
   * Double-Wert des Attributes; für val=
   */
  double dValue;
  /**
   *  Char-Wert des Attributes. Default ist Blanc.
   */
  char cValue = ' ';
  /**
   *  Boolean-Wert des Attributes. Default ist false.
   */
  boolean bValue = false;

  // Methods
  /**
   *  Setzt den Wert des Attributes; dabei wird versucht festzustellen, ob der Inhalt
   *  int, char, float oder boolean ist.
   */
  public void setValue(String val) {
    sValue = val;
    // Error
    if (val == null || val.length() == 0) {
      return;
    }
    // Character?
    if (val.length() == 1) {
      cValue = val.charAt(0);
    }
    // Gänsebeine? --> String!
    if (sValue.startsWith("\"") && sValue.endsWith("\"")) {
      sValue = sValue.substring(1, sValue.length()-1);
      cValue = sValue.charAt(0);
    }
    // Character?
    else if (sValue.startsWith("'") && sValue.endsWith("'")) {
      cValue = sValue.charAt(1);
    }
    // nummerisch?
    else if (Character.isDigit(sValue.charAt(0)) || sValue.charAt(0) == '-') {
      try {
        dValue=NumberFormat.getInstance().parse(sValue).doubleValue();
        fValue = (float)dValue;
        iValue = (int)dValue;
      }
      catch (Exception e) {
        // nix
      }
    }
    else { // keine Zahl; Schlüsselwort?
      if (sValue.equals("REL")) {
        iValue=GridBagConstraints.RELATIVE;
      }
      else if (sValue.equals("REM")) {
        iValue=GridBagConstraints.REMAINDER;
      }
    }
    // boolean?
    if (sValue.equalsIgnoreCase("true") || sValue.equalsIgnoreCase("y") || sValue.equals("1")) {
      bValue=true;
    }
  }
  /**
   * Liefert den Namen des Attributes und seinen Wert.<BR>
   * für den Debug-Modus.
   */
  public String toString() {
    return sKeyword + "'" + sValue+"'";
  }
}