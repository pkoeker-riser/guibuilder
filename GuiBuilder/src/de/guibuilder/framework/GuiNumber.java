package de.guibuilder.framework;

import java.awt.Dimension;

import java.math.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import javax.swing.SwingConstants;

import de.pkjs.util.*;

/**
 * Implementierung einer numerischen Eingabe.<p> 
 * Die Eingabe erfolgt standardmäßig rechtsbündig für Ganzzahlen. 
 * Das Format der Eingabe kann mit <code>setFormat</code>
 * geändert werden.
 */
public class GuiNumber extends GuiText {
	// Attributes
	private NumberFormat format;

	// Constructor
	/**
	 * Columns wird auf 5 gesetzt; maximale Eingabelänge ist per Default 20. <br>
	 * MinimumSize = 30; MaximumSize = 100 <br>
	 * Ausrichtung Rechtsbündig.
	 */
	public GuiNumber() {
		this("number");
	}

	public GuiNumber(String label) {
		super(label);
		this.guiInit();
	}

	// Methods
	private void guiInit() {
		component.setColumns(5);
		this.setMaxlen(20); // Veranlaßt Registrierung des Input-Verifiers!
		this.setMinimumSize(new Dimension(30, this.getMinimumSize().height));
		this.setMaximumSize(new Dimension(100, this.getMaximumSize().height));
		component.setHorizontalAlignment(SwingConstants.RIGHT);
		this.format = NumberFormat.getInstance();
	}

	public final String getTag() {
		return "Number";
	}

	/**
	 * Liefert NUMBER From GuiComponent --> GuiText
	 */
	public final int getDataType() {
		return NUMBER;
	}

	/**
	 * Liefert das interne Zahlenformat.
	 */
	public NumberFormat getFormat() {
		return format;
	}

	/**
	 * Setzt die Formatangabe für die Eingabe; siehe java.text.DecimalFormat
	 */
	public void setFormat(String pattern) {
		format = new DecimalFormat(pattern);
	}

	/**
	 * Formatiert den übergebenen String unter Verwendung des internen Formats.
	 * 
	 * @param txt
	 *           Ein unformatierter String mit gültigen Werten
	 * @return Formatierter String
	 * @see #setFormat
	 * @see GuiTable.GuiTableFormatRenderer
	 */
	public String makeFormat(String txt) throws ParseException {
		if (txt == null || txt.length() == 0) {
			if (this.isNotnull()) {
				throw new ParseException("", 0);
			} else {
				return "";
			}
		}
		if (txt.indexOf(" ") != -1) { // Blancs raus
		   txt = txt.replaceAll(" ", "");		 
		}
		ParsePosition pp = new ParsePosition(0);
		Number n = format.parse(txt, pp);
		if (pp.getIndex() < txt.length()) {
			throw new ParseException("Illegal Number Format", pp.getIndex() + 1);
		}
		double value = n.doubleValue();
		String ret = format.format(value); 
		return ret;
	}
    public String makeFormat(Number value) throws ParseException {
        if (value == null) return null;
        String s = format.format(value);
        return s;
    }

	/**
	 * Setzt den Inhalt der Komponente auf den übergebenen Wert.
	 */
	public void setValue(double value) {
		this.setText(format.format(value));
	}

	public void setValue(BigDecimal value) {
		if (value != null) {
			this.setValue(value.doubleValue());
		} else {
			this.setText(null);
		}
	}
	public void setValue(Integer value) {
		if (value != null) {
			this.setValue(value.intValue());
		} else {
			this.setText(null);
		}
	}
	/**
	 * @see Convert#toDouble(String)
	 * @param value
	 */
	public void setValue(String value) {
		if (value == null || value.length() == 0) {
			this.setText(null);
		} else {
			double d = Convert.toDouble(value);
			if (Convert.getLastException() != null) {
				this.setText(value);
			} else {
				this.setValue(d);
			}
		}
	}
	public Class getValueClass() {
		return Number.class;
	}

	/**
	 * @see Convert#toInt(String)
	 * @return
	 */
	public int getValueInt() {
		String txt = component.getText();
		int i = Convert.toInt(txt);
		return i;
	}

	/**
	 * @see Convert#toLong(String)
	 * @return
	 */
	public long getValueLong() {
		String txt = component.getText();
		long l = Convert.toLong(txt);
		return l;
	}

	/**
	 * @see Convert#toBigDecimal(String)
	 * @return
	 */
	public BigDecimal getValueDecimal() {
		String txt = component.getText();
		BigDecimal big = Convert.toBigDecimal(txt);
		return big;
	}
}