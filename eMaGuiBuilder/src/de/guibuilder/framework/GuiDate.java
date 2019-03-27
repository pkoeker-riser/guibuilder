package de.guibuilder.framework;

import java.awt.Dimension;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.SwingConstants;

import de.pkjs.util.*;

/**
 * Implementierung einer Datumseingabe mit Überprüfung des eingegebenen Datums.
 * <p>
 * Das Datumsformat wird per Default auf "dd.MM.yyyy" gesetzt. <br>
 * Ein abweichendes Standard-Format kann bei "GuiBuilderConfig.xml" unter "DateFormat"
 * gesetzt werden.
 */
public class GuiDate extends GuiText {
	// Attributes
	protected static SimpleDateFormat defaultFormat = new SimpleDateFormat("dd.MM.yyyy");
	/**
	 * Das bei der Eingabe verwendete Datums-Format.
	 */
	protected SimpleDateFormat format;
	private GuiCalendarPopup linkPopup;

	// Constructor
	/**
	 * Erzeugt ein Datumsfeld mit dem Namen "date", Columns wird auf 7 gesetzt; Eingabe
	 * wird zentriert.
	 */
	public GuiDate() {
		this("date");
	}

	/**
	 * Erzeugt ein Datumsfeld mit dem übergebenen Label. Columns wird auf 7 gesetzt;
	 * Eingabe wird zentriert.
	 */
	public GuiDate(String label) {
		super(label);
		this.guiInit();
	}

	// Methods
	protected void guiInit() {
		this.format = defaultFormat;
		this.component.setColumns(8);
		this.setMinimumSize(new Dimension(70, this.getMinimumSize().height));
		this.setMaximumSize(new Dimension(80, this.getMaximumSize().height));
		this.component.setHorizontalAlignment(SwingConstants.CENTER);
		this.format.setLenient(false);
		this.setMaxlen(10); // Veranlaßt Registrierung des Input-Verifiers!
	}

	public final String getTag() {
		return "Date";
	}

	// From GuiComponent
	public final int getDataType() {
		return DATE;
	}

	/**
	 * Liefert das interne Datumsformat.
	 */
	public SimpleDateFormat getFormat() {
		return format;
	}

	/**
	 * Setzt die Formatangabe; siehe java.text.SimpleDateFormat
	 */
	public void setFormat(String pattern) {
		format = new SimpleDateFormat(pattern);
	}

	/**
	 * Formatiert den übergebenen String gemäß des internen Datum-Formats.
	 * <p>
	 * Wenn bei einer Eigabe von weniger als 9 Zeichen das Jahr kleiner als 20 ist, wird
	 * 2000 angenommen; wenn das Jahr &lt; 100 ist, wird 1900 angenommen. Ist wirklich ein
	 * Jahr im ersten Jahrhundert gemeint, muß dieses 10-stellig eingegeben werden:
	 * 01.02.0033
	 * 
	 * @throws ParseException,
	 *            wenn ungültiges Format
	 */
	public String makeFormat(String txt) throws ParseException {
		if (txt == null || txt.length() == 0) {
			if (this.isNotnull())
				throw new ParseException("", 0);
			else
				return "";
		}

		Date value;
		if (txt.equals("TODAY")) {
			value = new Date();
		} else {
		  synchronized (format) {
		    value = format.parse(txt);
		  }
			if (txt.length() <= 8) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(value);
				int y = cal.get(Calendar.YEAR);
				if (y < 20) {
					y = y + 2000;
					cal.set(Calendar.YEAR, y);
					value = cal.getTime();
				} else if (y < 100) {
					y = y + 1900;
					cal.set(Calendar.YEAR, y);
					value = cal.getTime();
				}
			}
		}
		String ret = null;
    synchronized (format) {
      ret = format.format(value);
    }
		return ret;
	}

	/**
	 * Liefert den Inhalt als java.util.Date (ohne Uhrzeit)
	 * @return
	 */
	public Date getValueDate() {
		String txt = component.getText();
		Date d = Convert.toDate(txt);
		return d;
	}
    /**
     * Liefert den Inhalt als java.util.Date (mit Uhrzeit)
     * @return
     */
    public Date getValueDateTime() {
       String txt = component.getText();
       Date d = Convert.toDateTime(txt);
       return d;
   }

	/**
	 * Liefert den Inhalt als java.sql.Date  (ohne Uhrzeit)
	 * @return
	 */
	public java.sql.Date getValueSqlDate() {
		String txt = component.getText();
		java.sql.Date d = Convert.toSqlDate(txt);
		return d;
	}

	/**
	 * Overlay <BR>
	 * Wenn "TODAY" übergeben wird, wird das aktuelle Tagesdatum gesetzt.
	 */
	public void setText(String s) {
		if (s != null && s.equals("TODAY")) {
			Date date = new Date();
			String sd = null;
			synchronized(format) {
			  sd = format.format(date);
			}
			component.setText(sd);
		} else {
			/*
			 * KKN 30.04.2004: Das Datum formatiert anzeigen, wenn es sich formatieren lässt.
			 */
			try {
				component.setText(this.makeFormat(s));
			} catch (ParseException e) {
				component.setText(s);
			}
		}
	}
	
	public void setValue(Object val) {
	   if (val instanceof Date) {
	      this.setValue((Date)val);
	   } else if (val instanceof Long) {
	      this.setValue((Long)val);
	   } else {
	      super.setValue(val);
	   }
	}

	public void setValue(Date d) {
	  String txt = null;
	  synchronized(format) {
	     txt = format.format(d);
	  }
		component.setText(txt);
	}

	public void setValue(long d) {
	   synchronized(format) {
	      component.setText(format.format(new Date(d)));
	   }
	}

	public void setValue(Long d) {
	   synchronized(format) {
	      component.setText(format.format(new Date(d.longValue())));
	   }
	}

	public static void setDefaultFormat(String f) {
		defaultFormat = new SimpleDateFormat(f);
	}

	void setLinkPopup(GuiCalendarPopup pop) {
		this.linkPopup = pop;
	}

	GuiCalendarPopup getLinkPopup() {
		return this.linkPopup;
	}

	public static SimpleDateFormat getDefaultFormat() {
		return defaultFormat;
	}

	public Class getValueClass() {
		return Date.class;
	}

	static {
		try {
			String s = GuiUtil.getConfig().getValuePath(".Locale@DateFormat", "dd.MM.yyyy");
			defaultFormat = new SimpleDateFormat(s);
		} catch (Exception ex) {
			System.err.println("GuiDate: Error Initializing DateFormat: " + ex.getMessage());
		}
	}
}