/*
 * Created on 16.03.2003
 *
 * Mapping JDBC / java
 * bit 	 java.lang.Boolean
 * tinyint 	java.lang.Byte
 * smallint 	java.lang.Short
 * integer 	java.lang.Integer
 * bigint 	java.lang.Long / java.math.BigInteger
 * float, double 	java.lang.Double
 * real 	java.lang.Float
 * numeric, decimal 	java.math.BigDecimal
 * char, varchar, longvarchar 	java.lang.String
 * date 	java.sql.Date
 * time 	java.sql.Time
 * timestamp 	java.sql.Timestamp
 * binary, varbinary, longvarbinary 	byte[]
 */
package de.pkjs.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Diese Klasse entspricht in etwa der Convert-Klasse
 * des .NET Framework.<p>
 * Sie enthält nur statische Methoden zur Typ-Konvertierung.<br>
 * Wenn der übergebene Wert null ist, dann wird auch null (oder 0) geliefert.<p>
 * Zusätzlich kann aus einem String und unter Angabe des
 * Sql-Datentyps eine gewünschte Formatierung nach einem
 * Object vorgenommen werden.<p>
 * Diese Klasse soll konvertieren was zu konvertieren ist,
 * aber nicht validieren.
 * Alle Exceptions werden abgefangen.
 * Es werden also NIE Exceptions geworfen.
 * Es gibt lediglich die Möglichkeit, sich den zuletzt
 * aufgetretenen Fehler anzeigen zu lassen.
 * @see #getLastException
 */
public final class Convert {
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Convert.class);

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    //private static SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private static SimpleDateFormat sqlTimeFormat = new SimpleDateFormat("HH:mm:ss");
	private static SimpleDateFormat dateFormatDefault = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

	private static boolean lenient = false;
	private static int yearOffset;

	private static final DecimalFormat CONVERT_DOUBLE = new DecimalFormat("#0.###############"); // Double kann max 15 Stellen
   private static final DecimalFormat CONVERT_FLOAT = new DecimalFormat("#0.######"); // Float kann max 7 Stellen
	private static final ThreadLocal<Exception> lastEx = new ThreadLocal<Exception>();
	private static HashMap<String, java.awt.Color> colorMap;
	// Getter
	public static SimpleDateFormat getDateFormat() {
		return (SimpleDateFormat)dateFormat.clone();
	}
	public static SimpleDateFormat getDateTimeFormat() {
		return (SimpleDateFormat)dateTimeFormat.clone();
	}
	public static SimpleDateFormat getTimeFormat() {
		return (SimpleDateFormat)timeFormat.clone();
	}
	public static SimpleDateFormat getSqlTimeFormat() {
		return (SimpleDateFormat)sqlTimeFormat.clone();
	}
	 /**
   * Liefert das DateFormat im Format von Date#toString():
   * "EEE MMM dd HH:mm:ss zzz yyyy" Locale.US
   * @return
   */
  public static SimpleDateFormat getDateFormatDefault() {
    return (SimpleDateFormat)dateFormatDefault.clone();
  }
	// Setter
	public static void setDateFormat(SimpleDateFormat format) {
		dateFormat = format;
		dateFormat.setLenient(lenient);
	}
	public static void setDateTimeFormat(SimpleDateFormat format) {
		dateTimeFormat = format;
		dateTimeFormat.setLenient(lenient);
	}
	public static void setTimeFormat(SimpleDateFormat format) {
		timeFormat = format;
		timeFormat.setLenient(lenient);
	}
	public static void setSqlTimeFormat(SimpleDateFormat format) {
		sqlTimeFormat = format;
		sqlTimeFormat.setLenient(lenient);
	}
	/*
	public static DecimalFormat getDecimalFormat() {
		return decimalFormat;
	}
	public static void setDecimalFormat(DecimalFormat format) {
		decimalFormat = format;
	}
	*/
	/**
	 * Liefert den zuletzt aufgetretenen Fehler oder null,
	 * wenn bei der letzten Konvertierung kein Fehler
	 * aufgetreten ist.
	 * @return Throwable
	 */
	public static Exception getLastException() {
		return lastEx.get();
	}
	// Methods ###################################
	/**
	 * Liefert true wenn der übergebene Wert 1 ist.
	 * Alles andere ergibt false
	 */
	public static boolean toBoolean(int val) {
		lastEx.set(null);
		switch (val) {
			case 0: return false;
			case 1: return true;
			default:
				lastEx.set(new IllegalArgumentException("Convert.toBoolean Illegal Argument: " + Integer.toString(val)));
			return false;
		}
	}
	/**
	 * Liefert true wenn der übergebene Wert 1 ist.
	 * Alles andere ergibt false
	 */
	public static boolean toBoolean(long val) {
		lastEx.set(null);
		if (val == 0) {
			return false;
		} else if (val == 1) {
			return true;
		} else {
			lastEx.set(new IllegalArgumentException("Convert.toBoolean Illegal Argument: " + Long.toString(val)));
			return false;
		}
	}
	/**
	 * Akzeptiert auch "true", "false", "ok", "yes", "no", "y", "n", "on", "off", "0", "(0)", "1"
	 * Ist nicht Case-sensitiv. "FaLsE" und "No" ist also auch "false".<br>
	 * Blancs werden vorn und hinten abgeschnitten.<br>
	 * Liefert auch false wenn Argument null.
	 * @param s
	 * @return boolean
	 */
	public static boolean toBoolean(String s) {
		lastEx.set(null);
		if (s == null) {
			return false;
		}
		if (s.startsWith(" ") || s.endsWith(" ")) {
		  s = s.trim();
		}
		String val = s.toLowerCase();
		if (val.equals("true")) {
			return true;
		} else if (val.equals("false")) {
			return false;
		} else if (val.equals("y")) {
			return true;
		} else if (val.equals("n")) {
			return false;
		} else if (val.equals("yes")) {
			return true;
		} else if (val.equals("no")) {
			return false;
		} else if (val.equals("1")) {
			return true;
		} else if (val.equals("0")) {
			return false;
		} else if (val.equals("(0)")) { // MS-SQL Server
			return false;
		} else if (val.equals("ok")) {
			return true;
		} else if (val.equals("on")) {
			return true;
		} else if (val.equals("off")) {
			return false;
      } else if (val.equals("ja")) {
         return true;
      } else if (val.equals("nein")) {
         return false;
		} else if (val.equals("b'0'")) { // MySql
			return false;
		} else if (val.equals("b'1'")) { // MySql
			return true;
		} else {
			lastEx.set(new IllegalArgumentException("Convert.toBoolean Illegal Argument: " + s));
			return false;
		}
	}
	/**
	 * Wenn Object null, dann false
	 * @param o
	 * @return
	 */
	public static boolean toBoolean(Object o) {
		lastEx.set(null);
		if (o == null) {
			return false;
		} else {
			return toBoolean(o.toString());
		}
	}
	/**
	 * Macht aus boolean 0 oder 1
	 * @param b
	 * @return int 1 if true; 0 if false
	 */
	public static int toInt(boolean b) {
		lastEx.set(null);
		if (b == true) {
			return 1;
		} else {
			return 0;
		}
	}
	/**
	 * Liefert 0, wenn kein cast auf int möglich
	 * @param l
	 * @return
	 */
	public static int toInt(long l) {
		lastEx.set(null);
		int i = 0;
		try {
			i = (int)l;
		} catch (Exception ex) {
			lastEx.set(ex);
		}
		return i;
	}
	/**
	 * Hierbei werden "true" und "false" zu "1" oder "0".<p>
	 * Bei "MIN_VALUE" und "MAX_VALUE"
	 * wird Integer.MIN_VALUE bzw. Integer.MAX_VALUE geliefert.
	 * @param s
	 * @return
	 */
	public static int toInt(String s) {
		lastEx.set(null);
		int i = 0;
		if (s != null) {
			if (s.startsWith(" ") || s.endsWith(" ")) {
			  s = s.trim();
			}
			if (s.length() == 0) return 0;
			try {
				if (s.indexOf(',')!= -1) { // 25.6.2004
					s = toStringNumber(s); //PKÖ 25.11.2003 wegen JDK 1.3
				}
				i = (int) Double.parseDouble(s);
			} catch (Exception ex) {
				if (s.equals("true")) {
					return 1;
				} else if (s.equals("false")) {
					return 0;
				} else if (s.equals("MIN_VALUE")) {
					return Integer.MIN_VALUE;
				} else if (s.equals("MAX_VALUE")) {
					return Integer.MAX_VALUE;
				}
				lastEx.set(ex);
			}
		}
		return i;
	}
	public static int toInt(Integer i) {
		lastEx.set(null);
		return i.intValue();
	}
	/**
	 * @see #toInt(String)
	 * @param o
	 * @return 0 if param IS NULL
	 */
	public static int toInt(Object o) {
		lastEx.set(null);
		if (o == null) {
			return 0;
		} else {
			return toInt(o.toString());
		}
	}
	public static long toLong(boolean b) {
		lastEx.set(null);
		if (b == true) {
			return 1;
		} else {
			return 0;
		}
	}
	public static long toLong(int i) {
		lastEx.set(null);
		long l = i;
		return l;
	}
	public static long toLong(String s) {
		lastEx.set(null);
		long l = 0;
		if (s != null) {
			if (s.startsWith(" ") || s.endsWith(" ")) {
			  s = s.trim();
			}
	     if (s.length() == 0) return 0;
			try {
				if (s.indexOf(',') != -1) { // 25.6.2004
					s = toStringNumber(s); // PKÖ 25.11.2003
				}
				l = (long) Double.parseDouble(s);
			} catch (Exception ex) {
				if (s.equals("true")) {
					return 1;
				} else if (s.equals("false")) {
					return 0;
				} else if (s.equals("MIN_VALUE")) {
					return Long.MIN_VALUE;
				} else if (s.equals("MAX_VALUE")) {
					return Long.MAX_VALUE;
				}
				lastEx.set(ex);
			}
		}
		return l;
	}
	public static long toLong(Long l) {
		return l.longValue();
	}
	/**
	 * Liefert 0 wenn Object null;
	 * ansonsten toLong(o.toString());
	 * @param o
	 * @return
	 */
	public static long toLong(Object o) {
		if (o == null) {
			return 0;
		} else {
			return toLong(o.toString());
		}
	}
	/**
	 * Converts from String to float.
	 * Return 0.0 if param is null or param is not a parsable String
	 * @param s
	 * @return
	 */
   public static float toFloat(String s) {
      lastEx.set(null);
      if (s == null) return 0.0f;
      if(s.startsWith(" ") || s.endsWith(" ")) {
         s = s.trim();
      }
      if(s.length() == 0) return 0.0f;
      if(s.indexOf(',') != -1) {
         s = toStringNumber(s); // 12.2.2010 PKÖ
      }
      try {
         float f = Float.parseFloat(s);
         return f;
      }
      catch(Exception ex) {
         if(s.equals("true")) {
            return 1.0f;
         }
         else if(s.equals("false")) {
            return 0.0f;
         }
         else if(s.equals("MIN_VALUE")) {
            return Float.MIN_VALUE;
         }
         else if(s.equals("MAX_VALUE")) {
            return Float.MAX_VALUE;
         }
         lastEx.set(ex);
         return 0.0f;
      }
   }

	/**
	 * Converts from String to double.
	 * Return 0.0 if param is null or param is not a parsable String
	 * @param s
	 * @return
	 */
	public static double toDouble(String s) {
		lastEx.set(null);
		double d = 0.0d;
		if (s != null) {
			if (s.startsWith(" ") || s.endsWith(" ")) {
			  s = s.trim();
			}
	    if (s.length() == 0) return 0.0d;
			if (s.indexOf(',') != -1) {
				s = toStringNumber(s); // 2.9.2004 / PKÖ
			}
			try {
				d = Double.parseDouble(s);
			} catch (Exception ex) {
				if (s.equals("true")) {
					return 1.0;
				} else if (s.equals("false")) {
					return 0.0;
				} else if (s.equals("MIN_VALUE")) {
					return Double.MIN_VALUE;
				} else if (s.equals("MAX_VALUE")) {
					return Double.MAX_VALUE;
				}
				lastEx.set(ex);
			}
		}
		return d;
	}
	// new 4.10.2006  PKÖ
	public static double toDouble(Object o) {
		lastEx.set(null);
		if (o == null) {
			return 0.0;
		} else {
			return toDouble(o.toString());
		}
	}

	public static Date toDate(String s) {
      lastEx.set(null);
      Date ret = null;
      // 0. leer
      if(s == null || s.length() == 0) {
         return null;
      }
      // 1. DateFormat
      try {
         synchronized(dateFormat) {
            ret = dateFormat.parse(s);
            return ret;
         }
      }
      catch(Exception ex) {}
      // 2. SQL-Date-Format	
      if(s.indexOf('-') != -1) {
         try {
            ret = java.sql.Date.valueOf(s);
            return ret;
         }
         catch(Exception ex) {}
      }
      // 3. TimeFormat
      try {
         synchronized(timeFormat) {
            ret = timeFormat.parse(s);
            return ret;
         }
      }
      catch(Exception ex) {}
      // 4. Falls Date#toString() verwendet wurde...
      try {
         synchronized(dateFormatDefault) {
            ret = dateFormatDefault.parse(s);
            return ret;
         }
      }
      // 99. jetzt gebens wirs auf!
      catch(Exception dex) {
         logger.warn(dex.getMessage(), dex);
         lastEx.set(dex);
         return null;
      }
	}
	/**
	 * Funktioniert nur, wenn String im Format
	 * yyyy-mm-dd oder dd.mm.yyyy
	 * @param s
	 * @return
	 */
	public static java.sql.Date toSqlDate(String s) {
		lastEx.set(null);
		java.sql.Date ret = null;
		if (s != null && s.length() != 0) {
			try { // erste Versuch: sql.Date
				ret = java.sql.Date.valueOf(s);
			} catch (Exception ex) {
				try { // zweite Versuch: util.Date
					java.util.Date d = toDate(s);
					if (d != null) {
						ret = new java.sql.Date(d.getTime());
					} else {
						lastEx.set(ex);
					}
				} catch (Exception exx) {
					lastEx.set(ex);
				}
			}
		}
		return ret;
	}
	/**
	 * Converts java.util.Date to java.sql.Date
	 * @param ud
	 * @return null if argument is null
	 */
	public static java.sql.Date toSqlDate(java.util.Date ud) {
		lastEx.set(null);
		if (ud == null) {
			return null;
		} else {
			java.sql.Date ret = new java.sql.Date(ud.getTime());
			return ret;
		}
	}
	/**
	 * Converts String to Timestamp.<p>
	 * Wenn das nicht funktioniert, wird hilfsweise auf Datum oder Uhrzeit geparst
	 * @param s
	 * @return null, wenn kein gültiger String
	 */
	public static java.sql.Timestamp toDateTime(String s) { // Timestamp statt Date! 8.4.2004 PKÖ
		lastEx.set(null);
		java.util.Date ret = null;
		if (s != null && s.length() != 0) {
			try {
        synchronized(dateTimeFormat) {
          ret = dateTimeFormat.parse(s); // Zuerst auf Timestamp-Format prüfen...
        }
			} catch (Exception ex) {
				try {
	        synchronized(dateFormat) {
	          ret = dateFormat.parse(s); // ... wenn das schief get, vielleich ein Datum?
	        }
				} catch (Exception dex) {
					try {
		        synchronized(timeFormat) {
		          ret = timeFormat.parse(s); // ... auch nicht, vielleicht eine Uhrzeit?
		        }
					} catch (Exception tex) {
						lastEx.set(tex); // Jetzt geben wir auf!
					}
				}
			}
		}
		if (ret != null) {
			java.sql.Timestamp sqlDateTime = new java.sql.Timestamp(ret.getTime());
			return sqlDateTime;
		} else {
			return null; // Neu 15.5.2004 / PKÖ
		}
	}
	/**
	 * Akzeptiert hh:mm, h:m, hh:mm:ss, h:m:s
	 * @param s
	 * @return
	 */
	public static java.sql.Time toTime(String s) {
		lastEx.set(null);
		java.sql.Time ret = null;
		if (s == null || s.length() == 0) {
			return null;
		} 
		// Der will hier hh:mm:ss haben
		try {
			ret = java.sql.Time.valueOf(s);
			return ret;
		} catch (Exception ex) {}
		try {
			Date d = null;
         synchronized(sqlTimeFormat) {
         	d = sqlTimeFormat.parse(s);
         }
			ret = new java.sql.Time(d.getTime());
			return ret;
		} catch (Exception dex) {}
		// HH:mm
		try {
			Date d = null;
			synchronized(timeFormat) {
				d = timeFormat.parse(s);
         }
			ret = new java.sql.Time(d.getTime());
			return ret;
		} catch (Exception fex) {
			lastEx.set(fex);
		}					
		return ret;
	}
	/**
	 * Besonderheit:<br>
	 * Wenn die Länge von s <=8 wird versucht,
	 * daraus ein gültiges Geburtsdatum zu machen,
	 * welches nicht in der Zukunft liegt.<p>
	 * Also 11.12.55 zu 11.12.1955<p>
	 * Liefert null, wenn der String null oder leer.
	 * @param s
	 * @return
	 */
	public static Date toBirthdate(String s) {
		lastEx.set(null);
	   if (s == null || s.length() == 0) {
	      return null;
	   }
		try {
		  Date value = null;
      synchronized(dateFormat) {
        value = dateFormat.parse(s);
      }
	    if (s.length() <= 8) {
          Calendar cal = Calendar.getInstance();
          cal.setTime(value);
          int y = cal.get(Calendar.YEAR);
          if (y < yearOffset) {
            y = y + 2000;
            cal.set(Calendar.YEAR, y);
            value = cal.getTime();
          } else if (y < 100) {
            y = y + 1900;
            cal.set(Calendar.YEAR, y);
            value = cal.getTime();
          }
        }
	      return value;
		} catch (Exception ex) {
		    lastEx.set(ex);
		    return null;
		}
	}
	/**
	 * Formatiert die Zeit in das eingestellte Format.
	 * @param time
	 * @return String im Format hh:mm o.ä.
	 * @see #getTimeFormat
	 */
	public static String toString(java.sql.Time time) {
		lastEx.set(null);
		synchronized(timeFormat) {
			return timeFormat.format(time);
		}
	}
	/**
	 * @see #toBigDecimal(String)
	 * @param o #toString()
	 * @return
	 */
	public static BigDecimal toBigDecimal(Object o) {
		lastEx.set(null);
		BigDecimal ret = new BigDecimal("0.00");
		if (o != null) {
			ret = toBigDecimal(o.toString());
		}
		return ret;
	}
	/**
	 * Konvertiert einen String nach BigDecimal;
	 * dabei wird "," zu ".".<p>
	 * Die Strings "true" und "false" werden zu '1' oder '0'.<p>
	 * Wenn das Argument "null" ist oder der String nicht numerisch,
	 * wird '0.00' geliefert.
	 * @param s
	 * @return
	 */
	public static BigDecimal toBigDecimal(String s) {
		lastEx.set(null);
		BigDecimal ret = new BigDecimal("0.00");
		if (s != null) {
			try {
				if (s.startsWith(" ") || s.endsWith(" ")) {
					s = s.trim();
				}
				if (s.length() == 0) return ret;
				if (s.indexOf(',') != -1) {
					s = toStringNumber(s); // 29.2.2004 PKÖ
				} else if (s.toLowerCase().equals("true")) {
					s = "1";
				} else if (s.toLowerCase().equals("false")) {
					s = "0";
				}
				return new BigDecimal(s);
			} catch (Exception ex) {
				lastEx.set(ex);
			}
		}
		return ret;
	}
	public static String toString(boolean b) {
		lastEx.set(null);
		if (b) return "true";
		else return "false";
	}
	public static String toString(Boolean b) {
		lastEx.set(null);
		if (b.booleanValue()) return "true";
		else return "false";
	}
	public static String toString(Byte b) {
		lastEx.set(null);
		return b.toString();
	}
	public static String toString(short i) {
		lastEx.set(null);
		return Short.toString(i);
	}
	public static String toString(Short i) {
		lastEx.set(null);
		return i.toString();
	}
	public static String toString(int i) {
		lastEx.set(null);
		return Integer.toString(i);
	}
	public static String toString(int i, DecimalFormat format) {
		lastEx.set(null);
		try {
			String s = format.format(i);
			return s;
		} catch (Exception ex) {
			lastEx.set(ex);
			return null;
		}
	}
	public static String toString(Integer i) {
		lastEx.set(null);
		return i.toString();
	}
	public static String toString(long l) {
		lastEx.set(null);
		return Long.toString(l);
	}
	public static String toString(Long l) {
		lastEx.set(null);
		return l.toString();
	}
	public static String toString(double d) {
		lastEx.set(null);
		if (d == 0.0d) return "0";
      String s = Double.toString(d);
      if (s.indexOf('E') == -1) { // Kein Exponent
         if (s.endsWith(".0")) {
            s = s.substring(0, s.length()-2);
         } else {            
//            DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.getDefault());
//            char c = dfs.getDecimalSeparator();
//            if (c == ',')
            s = s.replace('.', ',');
         }
         return s;
      } else { // Exponent
         synchronized(CONVERT_DOUBLE) {
            s = CONVERT_DOUBLE.format(d);
            return s;
         }
      }
	}
	public static String toString(Double d) {
		lastEx.set(null);
		if (d == null) return null;
		return toString(d.doubleValue());
	}
	public static String toString(Float f) {
		lastEx.set(null);
		if (f == null) return null;
		return toString(f.floatValue());
	}
	public static String toString(float f) {
		lastEx.set(null);
      if (f == 0.0f) return "0";
      String s = Float.toString(f);
      if (s.indexOf('E') == -1) { // Kein Exponent
         if (s.endsWith(".0")) {
            s = s.substring(0, s.length()-2);
         } else {
            s = s.replace('.', ',');
         }
         return s;
      } else { // Exponent
         synchronized(CONVERT_FLOAT) {
            s = CONVERT_FLOAT.format(f);
            return s;
         }
      }
	}
	public static String toString(Date d) {
		lastEx.set(null);
		if (d == null) return null;
		synchronized(dateFormat) {
		   return dateFormat.format(d);
		}
	}
	public static String toString(Date d, SimpleDateFormat format) {
		lastEx.set(null);
		if (d == null) return null;
		return format.format(d);
	}
  public static String toStringTimestamp(Date d) {
    lastEx.set(null);
    if (d == null) return null;
    synchronized (dateTimeFormat) {
      return dateTimeFormat.format(d);
    }
  }
  public static String toStringTime(Date d) {
    lastEx.set(null);
    if (d == null) return null;
    synchronized (timeFormat) {
      return timeFormat.format(d);
    }
  }
  public static String toStringTimeSql(Date d) {
    lastEx.set(null);
    if (d == null) return null;
    synchronized (sqlTimeFormat) {
      return sqlTimeFormat.format(d);
    }
  }
	public static String toString(java.sql.Timestamp ts) {
		lastEx.set(null);
		if (ts == null) return null;
    synchronized(dateTimeFormat) {
      return dateTimeFormat.format(ts);
    }
	}
	public static String toString(BigDecimal big) {
		lastEx.set(null);
		if (big == null) return null;
		String txt = big.toString();
		if (big.scale() > 0) {
		  txt = txt.replace('.',','); // TODO Number Format
		}
		return txt;		
	}
	public static String toString(BigInteger big) {
		lastEx.set(null);
		if (big == null) return null;
		String txt = big.toString();
		return txt;
	}
	/**
	 * Konvertiert eine String im Format "###.###.##0,000"
	 * in das Format "########0.000".
	 * Wird null übergeben wird auch null geliefert.
	 * Strings werden vorn und hinten abgeschnitten.
	 * Der Sinn besteht darin, ein deutsches NumberFormat für
	 * parseDouble und BigDecimal zu konvertieren.
	 * @param s
	 * @return
	 */
	public static String toStringNumber(String s) {
		lastEx.set(null);
		if (s == null) return null;
		if (s.startsWith(" ") || s.endsWith(" ")) {
			s = s.trim();
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case ',' : // Aus Komma einen Punkt machen.
					sb.append('.');
				break;
				case '.' :
					// Punkte bitte ignorieren
				break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
	/**
	 * Liefert einen String "red,green,blue" ( z.B. "255,255,255").
	 * @param c
	 * @return
	 */
	public static String toString(java.awt.Color c) {
		lastEx.set(null);
		if (c == null) return null;
		StringBuilder buff = new StringBuilder();
		buff.append(c.getRed());
		buff.append(",");
		buff.append(c.getGreen());
		buff.append(",");
		buff.append(c.getBlue());
		return buff.toString();
	}
	/**
	 * Liefert einen String "width,height".
	 * @param d
	 * @return
	 */
	public static String toString(java.awt.Dimension d) {
		lastEx.set(null);
		if (d == null) return null;
		StringBuilder buff = new StringBuilder();
		buff.append(d.getWidth());
		buff.append(",");
		buff.append(d.getHeight());
		return buff.toString();
	}
	/**
	 * Liefert eine java.awt.Color unter Angabe ihres Namens
	 * (red, gray,...) oder rgb mit Komma getrennt (255,255,255).
	 * Im Fehlerfall wird Pink geliefert.
	 * @param s
	 * @return
	 */
	public static java.awt.Color toColor(String s) {
		// TODO : Sollte auch im Hex-Format #aabbcc funktionieren
		lastEx.set(null);
		if (s == null) {
			return null;
		} 
		java.awt.Color ret = java.awt.Color.pink; // default
		final int komma = s.indexOf(",");
		if ( komma != -1) {
			try {
			  final String rs = s.substring(0, komma);
			  final int r = toInt(rs);
			  final int komma2 = s.indexOf(",", komma+1);
			  final String bs = s.substring(komma+1, komma2);
			  final int b = toInt(bs);
			  final int g = toInt(s.substring(komma2+1, s.length()));
			  ret = new java.awt.Color(r, b, g);
			} catch (Exception ex) {}
			return ret;
		}
		java.awt.Color retC = colorMap.get(s);
		if (retC != null) {
		  ret = retC;
		}
		return ret;		
	}
	/**
	 * Wandelt einen String in der Notation
	 * "123,456" oder "321.654" in eine Dimension um.<p>
	 * Wenn null übergeben wird, wird auch null geliefert.
	 * Im Fehlerfall wird 0,0 geliefert.
	 */
	public static java.awt.Dimension toDimension(String val) {
		lastEx.set(null);
		java.awt.Dimension ret = null;
		if (val == null) {
			return null;
		} else {
		  int w = 0;
		  int h = 0;

		  int p = val.indexOf(',');
		  if (p == -1) p = val.indexOf('.');
		  if (p != -1) {
				String temp = val.substring(0, p);
				w = Convert.toInt(temp);
				temp = val.substring(p+1);
				h = Convert.toInt(temp);
		  }
		  ret = new java.awt.Dimension(w, h);
		  return ret;
	  }
	}
	public static boolean isModified(String a, String b, int type) {
    if (a == null && b == null) {
      return false;
    } else if (a == null && b.length() == 0) {
      return false;
      // Wenn vorher null und jetzt Leerstring, dann nicht modified setzen!
    } else if (a == null && b != null) {
      return true;
    } else if (a != null && b == null) {
      return true;
    }

    switch (type) { // beide nicht null
    case Types.BIGINT:
      long l1 = Convert.toLong(a);
      long l2 = Convert.toLong(b);
      if (l1 == l2) return false;
      break;
    case Types.TINYINT:
    case Types.SMALLINT:
    case Types.INTEGER:
      int i1 = Convert.toInt(a);
      int i2 = Convert.toInt(b);
      if (i1 == i2) return false;
      break;
    case Types.BIT:
    case Types.BOOLEAN:
    {
      boolean b1 = toBoolean(a);
      boolean b2 = toBoolean(b);
      if (b1 == b2) return false;
    }
      break;
    case Types.DECIMAL:
    case Types.NUMERIC:
      BigDecimal b1 = toBigDecimal(a);
      BigDecimal b2 = toBigDecimal(b);
      if (b1.compareTo(b2) == 0) return false;
      break;
    case Types.REAL: // float
    case Types.FLOAT:
    case Types.DOUBLE:
      double d1 = toDouble(a);
      double d2 = toDouble(b);
      if (d1 == d2) return false;
      break;
      default:
        if (a.equals(b) ) return false;
    }
    return true;
	}
	/**
	 * Erzeugt aus einem String unter Angabe des SQL-Types
	 * ein Object der entsprechenden Java-Klasse.<p>
	 * Aus "10.11.2003" und Types.DATE wird also
	 * ein Date-Object erzeugt.
	 * Dieses Objekt kann jetzt einem PreparedStatement
	 * mit setObject zugewiesen werden.<br>
	 * Wenn null übergeben wird, wird auch null geliefert.
	 * @param s
	 * @param type Siehe java.sql.Types
	 * @return
	 */
	public static Object toObject(String s, int type) {
		lastEx.set(null);
		if (s == null) {
			return null; // New 29.1.2004 //PKÖ
		}
		Object ret = s; // Default
		switch (type) {
			case Types.BIGINT:
				long l = Convert.toLong(s);
				ret = new Long(l);
				break;
			case Types.TINYINT:
				// bool?
			case Types.SMALLINT:
			case Types.INTEGER:
				int i = Convert.toInt(s);
				ret = new Integer(i);
				break;
			case Types.BIT:
			case Types.BOOLEAN:
				boolean b = Convert.toBoolean(s);
				ret = Boolean.valueOf(b);
				break;
			case Types.CHAR:
			case Types.LONGVARCHAR:
			case Types.VARCHAR:
				ret = s;
				break;
			case Types.DATE:
				ret = Convert.toSqlDate(s);
				break;
			case Types.DECIMAL:
			case Types.NUMERIC:
				ret = Convert.toBigDecimal(s);
				break;
			case Types.REAL: // float
			case Types.FLOAT:
			case Types.DOUBLE:
				double d = Convert.toDouble(s);
				ret = new Double(d);
				break;
			case Types.TIME:
				ret = Convert.toTime(s);
				break;
			case Types.TIMESTAMP:
				ret = Convert.toDateTime(s); //##?? MS-SQL-Server
				break;
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
				// TODO
				break;
			default:
				ret = s; // Nicht behandelter Datentyp ??
				break;
		}
		return ret;
	}
	/**
	 * Konvertiert einen String in ein Object der angegebenen Klasse.<p>
	 * Als Klassen sind erlaubt:
	 * <ul>
	 * <li>boolean, Boolean
	 * <li>int, Integer
	 * <li>long, Long
	 * <li>double, Double
	 * <li>float, Float
	 * <li>char, Character
	 * <li>byte, Byte
	 * <li>short, Short
	 * <li>String
	 * <li>Date
	 * <li>Time
	 * <li>Timestamp
	 * <li>BigDecimal
	 * </ul>
	 * Wenn der übergebene String null ist,
	 * werden die primitiv-Datentypen auf 0 abgebildet,
	 * bzw. false, ' '
	 * @param s
	 * @param type
	 * @return
	 */
	public static Object toObject(String s, Class<?> type) {
		lastEx.set(null);
		String tp = type.getName();
		if (s == null || s.length() == 0) {
			if (tp.equals("boolean")) {
				return Boolean.valueOf(false);
			} else if(tp.equals("int")) {
				return new Integer(0);
			} else if (tp.equals("long")) {
				return new Long(0);
			} else if (tp.equals("double")) {
				return new Double(0);
			} else if (tp.equals("float")) {
				return new Float(0);
			} else if (tp.equals("char")) {
				return new Character(' ');
			} else if (tp.equals("byte")) {
				return new Byte("0");
			} else if (tp.equals("short")) {
				return new Short("0");
			}
		} // End If null
    if (s == null) return null;
		if (tp.equals("boolean") || tp.endsWith(".Boolean")) {
			return Boolean.valueOf(s);
		} else if(tp.equals("int") || tp.endsWith(".Integer")) {
			return new Integer(s);
		} else if (tp.equals("long") || tp.endsWith(".Long")) {
			return new Long(s);
		} else if (tp.equals("double") || tp.endsWith(".Double")) {
			return new Double(s);
		} else if (tp.equals("float") || tp.endsWith(".Float")) {
			return new Float(s);
		} else if (tp.equals("char") || tp.endsWith(".Character")) {
			return new Character(s.charAt(0));
		} else if (tp.equals("byte") || tp.endsWith(".Byte")) {
			return new Byte(s);
		} else if (tp.equals("short") || tp.endsWith(".Short")) {
			return new Short(s);
		} else if (tp.endsWith(".String")) {
			return s;
		} else if (tp.endsWith(".Date")) {
			return toDate(s);
		} else if (tp.endsWith(".Time")) {
			return toTime(s);
		} else if (tp.endsWith(".Timestamp")) {
			return toDateTime(s);
		} else if (tp.endsWith(".BigDecimal")) {
			return toBigDecimal(s);
		}
		// In der Klasse nach einem Constructor suchen?
		lastEx.set(new IllegalArgumentException("#toObject; cannot convert Class: "+tp));
		return s;
	}
	/**
	 * Convertiert ein Object je nach sql.Types zu einem String
	 * @param o Ein Object oder null
	 * @param type Konstante java.sql.Types
	 * @return wenn o null, dann auch null
	 */
	public static String toString(Object o, int type) {
		if (o == null) {
			return null;
		}
		String ret = null;
		try {
			switch (type) {
				case Types.BIGINT:
				case Types.INTEGER:
				case Types.SMALLINT:
				case Types.TINYINT:
					if (o instanceof Integer) {
						ret = toString((Integer)o);
					} else if (o instanceof BigDecimal) {
						ret = toString((BigDecimal)o); // Oops? Was ist mit den Nachkommastellen?
					} else if (o instanceof BigInteger) {
						ret = toString((BigInteger)o);
					} else if (o instanceof Long) {
						ret = toString((Long)o);
					} else if (o instanceof Short) {
						ret = toString((Short)o);
					} else if (o instanceof Byte) {
						ret = toString((Byte)o);
					} else {
						ret = o.toString(); // Notnagel
					}
					break;

				case Types.BIT:
				case Types.BOOLEAN:
					ret = Convert.toString((Boolean)o);
					break;
				case Types.BLOB: {
						Blob b = (Blob)o;
						byte[] bts = b.getBytes(0, (int)b.length());
						ret = new String(bts); // TODO: Encoding??
					}
					break;
				case Types.CLOB:
				  Clob c = (Clob)o;
				  ret = c.getSubString(1L, new Long(c.length()).intValue());            
				  break;
				case Types.CHAR:
				case Types.LONGVARCHAR:
				case Types.VARCHAR:
					ret = o.toString();
					break;
				case Types.DATE:
				    ret = toString((Date)o);
					break;

				case Types.DECIMAL:
				case Types.NUMERIC:
				case Types.REAL: // float
				case Types.FLOAT:
				case Types.DOUBLE:
					if (o instanceof BigDecimal) {
						ret = toString((BigDecimal)o);
					} else if (o instanceof Double) {
						ret = toString((Double)o);
					} else if (o instanceof Float) {
						ret = toString((Float)o);
					} else {
						ret = o.toString(); // Notnagel
					}
					break;
				case Types.TIME:
				    ret = toString((java.sql.Time)o);
					break;
				case Types.TIMESTAMP:
					ret = toString((Timestamp)o);            
					break;
				case Types.BINARY:
				case Types.VARBINARY:
				case Types.LONGVARBINARY: {
						byte[] bts = (byte[])o;
						ret = new String(bts); // TODO: Encoding??
					}
					break;
				default:
					ret = o.toString(); // Alle anderen Datentypen
					break;
			}
		} catch (Exception ex) {
			lastEx.set(ex);
			return null;
		}
		return ret;
	}
	/**
	 * TODO:
	 * @param o
	 * @return
	 */
	public static String toString(Object o) {
	  lastEx.set(null);
	  if (o == null) return null;
	  if (o instanceof String) return (String)o;
	  if (o instanceof Date) return toString((Date)o);
    if (o instanceof Time) return toString((Time)o);
    if (o instanceof Timestamp) return toString((Timestamp)o);
    if (o instanceof Long) return toString((Long)o);
    if (o instanceof Integer) return toString((Integer)o);
    if (o instanceof Double) return toString((Double)o);
    if (o instanceof BigDecimal) return toString((BigDecimal)o);
    if (o instanceof BigInteger) return toString((BigInteger)o);
    if (o instanceof Boolean) return toString((Boolean)o);
    if (o instanceof Float) return toString((Float)o);
    if (o instanceof Short) return toString((Short)o);
    if (o instanceof Byte) return toString((Byte)o);

    String ret = o.toString(); // Notnagel
	  return ret;
	}
	/**
	 * Liefert true, wenn der String ausschließlich aus Ziffern (0 bis 9) besteht.
	 * Liefert auch false, wenn der String null oder leer ist.
	 * @param s
	 * @return
	 */
	public static boolean isInt(String s) {
		if (s == null || s.length() == 0 ) {
			return false;
		} else {
			if (s.startsWith("-")) {
				s = s.substring(1);
			}
			for (int i = 0; i < s.length(); i++ ) {
				char c = s.charAt(i);
				if (Character.isDigit(c)) {
					// Ziffer: OK
				} else { // keine Ziffer: false
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * Liefert nur dann true, wenn das ein gültiges Datum
	 * im Format dd.MM.yyyy ist.
	 * Liefert auch false, wann Argument null oder leer.
	 * @param s
	 * @return
	 */
	public static boolean isDate(String s) {
		lastEx.set(null);
		if (s == null || s.length() == 0 ) {
			return false;
		} else {
			try {
				synchronized(dateFormat) {
				  @SuppressWarnings("unused")
				  java.util.Date d = dateFormat.parse(s);
				}
				return true;
			} catch (Exception ex) {
				lastEx.set(ex);
				return false;
			}
		}
	}
	/**
	 * Konvertiert das übergebene Objekt in das Zielformat unter Angabe
	 * einer Methode.
	 * @param methodname
	 * @param argument
	 * @return
	 */
	public static Object convert(String methodname, Object argument) {
		try {
			Method m = getMethod(methodname, argument.getClass());
			Object o = m.invoke(null, new Object[] {argument});
			return o;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	/**
	 * Liefert eine Konvertierungsmethode dieser Klasse unter Angabe
	 * des Zielformats und dem Argument-Type.<p>
	 * @param name z.B. "Date" ohne den Zusatz "to"
	 * @param argumentType z.B. String.class
	 * @return z.B. die Methode Date toDate(String s)
	 */
	public static Method getMethod(String name, Class<?> argumentType) {
		try {
			int p = name.lastIndexOf('.');
			if (p != -1) {
				name = name.substring(p+1);
			}
			char c = name.charAt(0);
			if (Character.isLowerCase(c)) {
				name = name.substring(0, 1).toUpperCase() + name.substring(1);
			}
			Method m = Convert.class.getMethod("to"+name, new Class[] {argumentType});
			return m;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	static {
		// TODO : Date-Formate einlesen von wo?
		// java.awt.Color
		colorMap = new HashMap<String, java.awt.Color>();
		colorMap.put("black", java.awt.Color.black);
		colorMap.put("blue", java.awt.Color.blue);
		colorMap.put("cyan", java.awt.Color.cyan);
		colorMap.put("darkGray", java.awt.Color.darkGray);
		colorMap.put("gray", java.awt.Color.gray);
		colorMap.put("green", java.awt.Color.green);
		colorMap.put("lightGray", java.awt.Color.lightGray);
		colorMap.put("magenta", java.awt.Color.magenta);
		colorMap.put("orange", java.awt.Color.orange);
		colorMap.put("pink", java.awt.Color.pink);
		colorMap.put("red", java.awt.Color.red);
		colorMap.put("white", java.awt.Color.white);
		colorMap.put("yellow", java.awt.Color.yellow);
		// Neu 1.6.2004 / PKÖ damit 99.99.1999 nicht ein legales Datum ist!
		dateFormat.setLenient(false);
		dateTimeFormat.setLenient(false);
		timeFormat.setLenient(false);
		sqlTimeFormat.setLenient(false);
		dateFormatDefault.setLenient(false);
		// CurrentYear
		Date d = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		yearOffset = cal.get(Calendar.YEAR);
		while (yearOffset > 100) {
		    yearOffset = yearOffset - 100;
		}
	}
	/**
	 * @return Returns the lenient.
	 */
	public static boolean isLenient() {
		return lenient;
	}
	/**
	 * see DateFormat#setLenient
	 * Default is false
	 * @param lenient The lenient to set.
	 */
	public static void setLenient(boolean _lenient) {
		lenient = _lenient;
	}
	//################# Serialisierung #######################
	/**
	 * Serialisiert ein Objekt in ein Byte-Array.
	 * @param compressed Komprimiert das Byte-Array wenn true
	 */
	public static byte[] serialize(Object obj, boolean compressed) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			if (compressed) {
				GZIPOutputStream zipout = new GZIPOutputStream(baos);
				oos = new ObjectOutputStream(zipout);
			} else {
				oos = new ObjectOutputStream(baos);
			}
			oos.writeObject(obj);
			oos.close();
			byte[] bytes = baos.toByteArray();
			return bytes;
		} catch (Exception ex) {
			throw new IllegalStateException(ex.getMessage());
		}
	}
	/**
	 * Serialisiert ein Objekt ohne Komprimierung
	 * @param obj
	 * @return
	 */
	public static byte[] serialize(Object obj) {
		return serialize(obj, false);
	}
	/**
	 * Restauriert ein (nicht komprimiertes) serialisiertes Objekt.
	 * @param data
	 * @return
	 */
	public static Object deserialize(byte[] data) {
		return deserialize(data, false);
	}
	/**
	 * Restauriert ein serialisiertes Objekt.
	 * @param data
	 * @param compressed
	 * @return
	 */
	public static Object deserialize(byte[] data, boolean compressed) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ObjectInputStream ois = null;
			if (compressed) {
				GZIPInputStream zipin = new GZIPInputStream(bais);
				ois = new ObjectInputStream(zipin);
			} else {
				ois = new ObjectInputStream(bais);
			}
			Object o = ois.readObject();
			ois.close();
			return o;
		} catch (Exception ex) {
			throw new IllegalStateException(ex.getMessage());
		}
	}
	/**
	 * Convertiert beide Objekte nach String und vergleicht dann mit equals
	 * @param o1
	 * @param o2
	 * @return true, wenn beide null; false wenn eines null; ansonsten {@link String#equals(Object)}
	 */
	public static boolean equals(Object o1, Object o2) {
	   lastEx.set(null);
	   if (o1 == null && o2 == null) return true;
	   if (o1 == null || o2 == null) return false;
	   String s1 = toString(o1);
	   String s2 = toString(o2);
	   return s1.equals(s2);	   
	}
	/**
	 * @formatter:off
	 * Versucht anhand des Inhalts den Datentyp zu raten:
	 * Integer, Decimal, Date, Time, Timestamp, Varchar, Boolean
	 * Wenn OTHER, dann nicht bestimmbar
	 * @param s
	 * @return
	 * @formatter:on
	 */
	public static int guessDataType(String s) {
	   if (s == null || s.length() == 0) {
	      return Types.OTHER;
	   }
	   if (s.startsWith(" ") || s.endsWith(" ")) {
	      s = s.trim();
	      if (s.length() == 0) {
	         return Types.OTHER;
	      }
	   }
	   switch (s.length()) {
	   	case 4:
	   		if (s.equalsIgnoreCase("true")) {
	   			return Types.BOOLEAN;
	   		}
	   		break;
	      case 5: // TIME?
	         if (s.equalsIgnoreCase("false")) {
	            return Types.BOOLEAN;
	         }
	         if (isTypeTime4(s)) {
	            return Types.TIME;
	         }
	         break;
	      case 8: // TIME?
	         if (isTypeTime6(s)) {
	            return Types.TIME;
	         }
	         break;
	      case 10: // Date?
             if (isTypeDate(s))
                return Types.DATE;
	         break;
	      case 16:
	      	if (isTypeDate(s.substring(0,10)) 
	      			&& isTypeTime4(s.substring(11))) {
	      		return Types.TIMESTAMP;
	      	}
	      	break;
	      case 19:
	      	if (isTypeDate(s.substring(0,10)) 
	      			&& isTypeTime6(s.substring(11))) {
	      		return Types.TIMESTAMP;
	      	}
	      	break;
	   }
	   boolean isInt = true;
	   boolean isDec = true;
	   int pc = s.indexOf(',');
	   if (pc == -1) {
	      isDec = false;
	   }
	   if (s.startsWith("-") || s.startsWith("+")) {
	      s = s.substring(1);
	   }
       for (int i = 0; i < s.length(); i++) { // Vorab Buchstaben
          char c = s.charAt(i);
          if (Character.isLetter(c)) {
             return Types.VARCHAR;
          } else if (!Character.isDigit(c)) {
             isInt = false;
          }          
       }
       if (isInt) {
          return Types.INTEGER;
       }
       if (isDec) {
          return Types.DECIMAL;
       }
	   return Types.VARCHAR;
	}	
	private static boolean isTypeTime4(String s) {
      if (Character.isDigit(s.charAt(0)) 
            && Character.isDigit(s.charAt(1))
            && s.charAt(2) == ':'
            && Character.isDigit(s.charAt(3))
            && Character.isDigit(s.charAt(4))) {
      	return true;
      }
      return false;
	}
	private static boolean isTypeTime6(String s) {
      if (Character.isDigit(s.charAt(0)) 
            && Character.isDigit(s.charAt(1))
            && s.charAt(2) == ':'
            && Character.isDigit(s.charAt(3))
            && Character.isDigit(s.charAt(4))
            && s.charAt(5) == ':'
            && Character.isDigit(s.charAt(6))
            && Character.isDigit(s.charAt(7))) {
      	return true;
      }
      return false;
	}
	private static boolean isTypeDate(String s) {
		if ((Character.isDigit(s.charAt(0)) 
            && Character.isDigit(s.charAt(1))
            && s.charAt(2) == '.'
            && Character.isDigit(s.charAt(3))
            && Character.isDigit(s.charAt(4)) 
            && s.charAt(5) == '.'
            && Character.isDigit(s.charAt(6))
            && Character.isDigit(s.charAt(7))
            && Character.isDigit(s.charAt(8))
            && Character.isDigit(s.charAt(9)))
            || 
            (Character.isDigit(s.charAt(0)) 
            && Character.isDigit(s.charAt(1))
            && Character.isDigit(s.charAt(2))
            && Character.isDigit(s.charAt(3)) 
            && s.charAt(4) == '-'
            && Character.isDigit(s.charAt(5))
            && Character.isDigit(s.charAt(6))
            && s.charAt(7) == '-'
            && Character.isDigit(s.charAt(8))
            && Character.isDigit(s.charAt(9)))) {
			return true;
		}
		return false;
	}
}
