package de.guibuilder.framework;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
/**
 * InputVerifier für Feldbezogene Eingabeüberprüfung.
 * Wird vorerst nur bei GuiText eingesetzt, sowie Date, Time, Number, Money.<p>
 * @see GuiComponent#setNotnull
 * @see GuiComponent#setMinlen
 * @see GuiComponent#setMaxlen
 */
final class GuiInputVerifier extends InputVerifier {
  // Attributes
  /**
   * Verweis auf die Componente, deren Inhalt überprüft werden soll.
   */
  private GuiComponent comp;
  /**
   * Text der Fehlermeldung
   */
  private String errorMessage;
  // Constructor
  GuiInputVerifier(GuiComponent comp) {
    this.comp = comp;
  }
  // Methods
  /**
   * Liefert die Fehlermeldung, wenn verify "false" zurückgibt.
   */
  String getErrorMessage() {
    return errorMessage;
  }
  public boolean shouldYieldFocus(final JComponent c) {
     boolean b = super.shouldYieldFocus(c);
     return b;
  }
  /**
   * Wird bei lostFocus aufgerufen
   */
  public boolean verify(final JComponent c) {
     boolean b = this.verify(c, GuiUtil.isCheckNN());
     return b;
  }
  /**
   * From javax.swing.InputVerifier
   * @param c
   * @param checkNN Definiten, ob auf NotNull geprüft werden soll (LostFocus: besser nicht; verify: besser ja)
   */
  public boolean verify(final JComponent c, boolean checkNN) {
    // ErrorMessage zurücksetzen
    errorMessage = null;
    // PostProc
    // Uff; das muß hier sein, damit ggf. erst konvertiert und erst danach geprüft wird.
    // TODO Aber wie verhinden, daß jetzt postProc zweimal aufgerufen wird?
    comp.postProc();
    // Vorbereitung
    String value = null;
    if (comp.getUnformatedValue() != null) {
      value = comp.getUnformatedValue().toString();
    }
    // NotNull
    if (comp.getNotnull() && checkNN) {
      if (this.checkNotnull(comp, value) == false) {
        comp.getRootPane().setHint(errorMessage);
        return false;
      }
    }
    // Minlen
    if (this.checkMinlen(comp, value, comp.getMinlen()) == false) {
    	GuiRootPane root = comp.getRootPane();
    	if (root != null) {
    		root.setHint(errorMessage);
    	}
      return false;
    }
    // Maxlen
    if (this.checkMaxlen(comp, value, comp.getMaxlen()) == false) {
    	GuiRootPane root = comp.getRootPane();
    	if (root != null) {
    		root.setHint(errorMessage);
    	}
      return false;
    }
    // RegExp
    if (comp instanceof GuiText) {
      GuiText text = (GuiText)comp;
      if (this.checkRegexp(value, text.getRegexp()) == false) {
				comp.getRootPane().setHint(errorMessage);
        return false;
      }
    }
    if (comp instanceof GuiCombo) {
      GuiCombo text = (GuiCombo)comp;
      if (this.checkRegexp(value, text.getRegexp()) == false) {
        comp.getRootPane().setHint(errorMessage);
        return false;
      }
    }
    // Date
    if (comp.getDataType() == GuiComponent.DATE) {
      GuiText text = (GuiText)comp;
      if (this.checkFormat(text, value) == false) {
      	GuiRootPane root = comp.getRootPane();
      	if (root != null) {
      		root.setHint(errorMessage);
      	}      	
        return false;
      }
    }
    // Time
    else if (comp.getDataType() == GuiComponent.TIME) {
      GuiText text = (GuiText)comp;
      if (this.checkFormat(text, value) == false) {
				comp.getRootPane().setHint(errorMessage);
        return false;
      }
    }
    // Number / Money
    else if (comp.getDataType() == GuiComponent.NUMBER) {
      GuiText text = (GuiText)comp;
      if (this.checkFormat(text, value) == false) {
      	GuiRootPane root = comp.getRootPane();
      	if (root != null) {
      		root.setHint(errorMessage);
      	}
        return false;
      }
    }

    // Alles OK!
    return true;
  }
  private boolean checkNotnull(GuiComponent cmp, String value) {
    if (value == null || value.length() <= 0) {
      errorMessage = "Es sind nicht alle Pflichtfelder ausgefüllt worden.\nBitte füllen Sie alle Pflichtfelder aus, bevor Sie fortfahren.";
      if (cmp.getLabel() != null) {
         String label = cmp.getLabel();
         if (label.endsWith(":")) {
            label = label.substring(0, label.length()-1);
         }
         if (label == null || label.length() == 0) {
            label = cmp.getName();
         }
      	errorMessage += "\n["+label+"]";
      }
      return false;
    } else {
      return true;
    }
  }
  /**
   * Prüfung der minimalen Eingabelänge; wenn NotNull nicht gesetzt ist,
   * darf das Feld auch leer sein.
   */
  private boolean checkMinlen(GuiComponent cmp, String value, int minlen) {
  	if (minlen < 1) return true;
    if ((value == null || value.length() == 0) && cmp.isNotnull() == false) {
      return true;
    }
    if (value == null || value.length() < minlen) {
      errorMessage = "Es müssen mindestens "+minlen+" Zeichen eingegeben werden!";
      return false;
    } else {
      return true;
    }
  }
  private boolean checkMaxlen(GuiComponent cmp, String value, int maxlen) {
    if (value != null && value.length() > maxlen) {
      errorMessage = "Maximale Eingabelänge "+maxlen+" überschritten!";
      if (cmp.getJComponent() instanceof JTextComponent) {
        JTextComponent jCmp = (JTextComponent)cmp.getJComponent();
        jCmp.setSelectionStart(maxlen);
        jCmp.setSelectionEnd(value.length());
      } else if (cmp.getJComponent() instanceof JComboBox) {
        // Spezialbehandlung für Combo
        JComboBox box = (JComboBox) cmp.getJComponent();
        JTextComponent jCmp  = (JTextField)box.getEditor().getEditorComponent();
        jCmp.setSelectionStart(maxlen);
        jCmp.setSelectionEnd(value.length());
      }
      return false;
    } else {
      return true;
    }
  }
  private boolean checkRegexp(String value, Pattern regexp) {
    // Regulären Ausdruck prüfen
    if (regexp != null && value.length() > 0) {
      // boolean ok = regexp.match(value);
      Matcher match = regexp.matcher(value);
      boolean ok = match.matches();
      if (ok == false) {
        errorMessage = "Ungültiges Eingabeformat ("+regexp+")";
        return false;
      }
    }
    return true;
  }
  /**
   * Prüft auf Datum, Uhrzeit, Number, Money
   */
  private boolean checkFormat(GuiText cmp, String value) {
    if (value == null || value.length() == 0) {
      return true;
    }
    try {
      final String format = cmp.makeFormat(value);
      cmp.setText(format);
      return true;
    } catch (ParseException ex) {
      String msg = null;
      switch (cmp.getDataType()) {
        case GuiComponent.DATE:
          msg = "Ungültiges Datumsformat!";
        break;
        case GuiComponent.TIME:
          msg = "Ungültiges Uhrzeitformat!";
        break;
        case GuiComponent.NUMBER:
          msg = "Ungültiges Zahlenformat!";
        break;
      }
      errorMessage = msg; // + " ("+ex.getMessage()+")";
      return false;
    }
  }
}