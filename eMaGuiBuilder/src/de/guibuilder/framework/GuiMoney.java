package de.guibuilder.framework;

import java.awt.Dimension;

import java.math.*;

import java.text.*;

import javax.swing.SwingConstants;

import de.pkjs.util.*;

/**
 * Implementierung einer Betragseingabe mit überprüfung des eingegebenen Betrages auf
 * numerische Werte mit zwei Nachkommastellen.<p>
 * Die Eingabe ist rechtsbündig; der
 * Dezimal-Trenner wird aus den Ländereinstllungen übernommen.
 */
public class GuiMoney extends GuiText {
	// Attributes
	/**
	 * DecimalFormat("#,##0.00;-#,##0.00")
	 */
	private static DecimalFormat defaultFormat = new DecimalFormat("#,##0.00;-#,##0.00");
	private DecimalFormat format;

	// Constructor
	/**
	 * Columns wird auf 8 gesetzt. Die maximale Eingabelänge auf 18. Die Ausrichtung auf
	 * Rechtsbündig. Default-Name ist "money".
	 */
	public GuiMoney() {
		this("money");
	}

	public GuiMoney(String label) {
		super(label);
		this.guiInit();
	}

	// Methods
	private void guiInit() {
		this.component.setColumns(8);
		this.setMaxlen(18); // Veranlaßt Registrierung des Input-Verifiers!
		this.setMinimumSize(new Dimension(70, this.getMinimumSize().height));
		this.setMaximumSize(new Dimension(150, this.getMaximumSize().height));
		this.component.setHorizontalAlignment(SwingConstants.RIGHT);
		this.format = defaultFormat;
	}

	public final String getTag() {
		return "Money";
	}

	// From GuiComponent --> GuiText
	public final int getDataType() {
		return NUMBER;
	}

	/**
	 * Liefert das interne Währungsformat.
	 */
	public DecimalFormat getFormat() {
		return this.format;
	}

	/**
	 * Setzt die Formatangabe für die Zahl; siehe java.text.DecimalFormat
	 */
	public void setFormat(String pattern) {
		this.format = new DecimalFormat(pattern);
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

		ParsePosition pp = new ParsePosition(0);
		Number n = format.parse(txt, pp);
		if (pp.getIndex() < txt.length()) {
			throw new ParseException("Illegal Number Format", pp.getIndex() + 1);
		}
		double value = n.doubleValue();
		return format.format(value);
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
			this.setValue((String)null);
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

	// From GuiText
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