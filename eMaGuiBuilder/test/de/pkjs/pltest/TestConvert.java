/*
 * Created on 26.06.2004
 */
package de.pkjs.pltest;

import java.awt.Color;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import de.pkjs.util.Convert;

/**
 * @author peter
 */
public class TestConvert extends TestCase {
	protected void setUp() {
		Convert.setLenient(false);
		Convert.setDateFormat(new SimpleDateFormat("dd.MM.yyyy"));
		Convert.setDateTimeFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"));
		Convert.setTimeFormat(new SimpleDateFormat("HH:mm"));
		Convert.setSqlTimeFormat( new SimpleDateFormat("HH:mm:ss"));
	}
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for de.jdataset / de.pkjs.pl");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestConvert.class);
		return suite;
	}
	public void testToBigDecimal() {
		BigDecimal big = Convert.toBigDecimal("123,45");
		assertEquals(big.doubleValue(), 123.45, 0.0);

		big = Convert.toBigDecimal("123.45");
		assertEquals(big.doubleValue(), 123.45, 0.0);

		big = Convert.toBigDecimal("11.123,45");
		assertEquals(big.doubleValue(), 11123.45, 0.0);

		big = Convert.toBigDecimal("false");
		assertEquals(big.doubleValue(), 0.0, 0.0);

		big = Convert.toBigDecimal("true");
		assertEquals(big.doubleValue(), 1.0, 0.0);
		
		assertEquals("123,45", Convert.toString(new BigDecimal("123.45")));
		big = null;
		assertNull(Convert.toString(big));
	}
	public void testToBoolean() {
		assertEquals(false, Convert.toBoolean(0));
		assertEquals(true, Convert.toBoolean(1));
		assertEquals(false, Convert.toBoolean(1234567));
		assertEquals(false, Convert.toBoolean(0l));
		assertEquals(true, Convert.toBoolean(1l));
		assertEquals(false, Convert.toBoolean("false"));
		assertEquals(true, Convert.toBoolean("true"));
		assertEquals(false, Convert.toBoolean("no"));
		assertEquals(true, Convert.toBoolean("yes"));
		assertEquals(false, Convert.toBoolean("n"));
		assertEquals(true, Convert.toBoolean("y"));
		assertEquals(false, Convert.toBoolean("off"));
		assertEquals(true, Convert.toBoolean("on"));
		assertEquals(true, Convert.toBoolean("ok"));
		assertEquals(false, Convert.toBoolean("0"));
		assertEquals(false, Convert.toBoolean("(0)"));
		assertEquals(true, Convert.toBoolean("1"));

		assertEquals(false, Convert.toBoolean("False"));
		assertEquals(true, Convert.toBoolean("True"));
		assertEquals(false, Convert.toBoolean("No"));
		assertEquals(true, Convert.toBoolean("Yes"));
		assertEquals(false, Convert.toBoolean("N"));
		assertEquals(true, Convert.toBoolean("Y"));
		assertEquals(false, Convert.toBoolean("Off"));
		assertEquals(true, Convert.toBoolean("On"));
		assertEquals(true, Convert.toBoolean("oK"));
		assertEquals(false, Convert.toBoolean("BlaBlub"));
		assertNotNull(Convert.getLastException());

	}
	public void testToFloat() {
		assertEquals(0f, Convert.toFloat("false"),0.0f);
		assertEquals(1f, Convert.toFloat("true"),0.0f);
		assertEquals(123f, Convert.toFloat("123"),0.0f);
		assertEquals(12.3f, Convert.toFloat(" 12.3" ),0.0f);
		//assertEquals(12.3f, Convert.toFloat("12,3"),0.0f);
		assertEquals(12.9f, Convert.toFloat("12.9"),0.0f);
		//assertEquals(12.9f, Convert.toFloat("12,9"),0.0f);
		assertEquals(Float.MIN_VALUE, Convert.toFloat("MIN_VALUE"),0.0f);
		assertEquals(Float.MAX_VALUE, Convert.toFloat("MAX_VALUE"),0.0f);
	}
	public void testToInt() {
		assertEquals(0, Convert.toInt(false));
		assertEquals(1, Convert.toInt(true));
		assertEquals(0, Convert.toInt("false"));
		assertEquals(1, Convert.toInt("true"));
		assertEquals(123, Convert.toInt("123"));
		assertEquals(4321, Convert.toInt(new Integer(4321)));
		assertEquals(54321, Convert.toInt((long)54321));
		assertEquals(12, Convert.toInt(" 12.3 "));
		assertEquals(12, Convert.toInt("12,3"));
		assertEquals(12, Convert.toInt("12.9"));
		assertEquals(12, Convert.toInt("12,9"));
		assertEquals(Integer.MIN_VALUE, Convert.toInt("MIN_VALUE"));
		assertEquals(Integer.MAX_VALUE, Convert.toInt("MAX_VALUE"));
		
		assertEquals(true, Convert.isInt("123456"));
		assertEquals(false, Convert.isInt("BlaBlub"));
		assertEquals(false, Convert.isInt("123456789o"));
		assertEquals(true, Convert.isInt("-123"));
		assertEquals(false, Convert.isInt(""));
		assertEquals(false, Convert.isInt(null));
	}
	public void testToLong() {
		assertEquals(0, Convert.toLong(false));
		assertEquals(1, Convert.toLong(true));
		assertEquals(0, Convert.toLong("false"));
		assertEquals(1, Convert.toLong("true"));
		assertEquals(123, Convert.toLong("123"));
		assertEquals(3210, Convert.toLong(3210));
		assertEquals(3210000, Convert.toLong(new Long(3210000)));
		assertEquals(12, Convert.toLong(" 12.3" ));
		assertEquals(12, Convert.toLong("12,3"));
		assertEquals(12, Convert.toLong("12.9"));
		assertEquals(12, Convert.toLong("12,9"));
		assertEquals(Long.MIN_VALUE, Convert.toLong("MIN_VALUE"));
		assertEquals(Long.MAX_VALUE, Convert.toLong("MAX_VALUE"));
	}
	public void testDouble() {
		assertEquals(543.21, Convert.toDouble("543,21"), 0.0);
	}
	public void testToString() {
		assertEquals("false", Convert.toString(false));
		assertEquals("true", Convert.toString(true));

		assertEquals("123", Convert.toString(123));
		assertEquals("-123", Convert.toString(-123));
		assertEquals("2345", Convert.toString(new Integer(2345)));
		// Long
		assertEquals("123", Convert.toString(123L));
		assertEquals("1234567", Convert.toString(new Long(1234567)));
		// Double
		assertEquals("123.456", Convert.toString(123.456D));
		assertEquals("23.4567", Convert.toString(new Double(23.4567D)));
		
		Calendar cal = new GregorianCalendar(2001,0,1); // Monate 0-Relativ!
		assertEquals("01.01.2001", Convert.toString(cal.getTime()));
		assertEquals("12:13", Convert.toString(new Time(11*3600*1000 + 13*60*1000)));
		assertEquals("12:13:30", Convert.toString(new Time(11*3600*1000 + 13*60*1000 + 30*1000), Convert.getSqlTimeFormat()));
		
		cal = new GregorianCalendar(2001,0,1,12,13);
		Timestamp ts = new Timestamp(cal.getTime().getTime());
		assertEquals("01.01.2001 12:13:00", Convert.toString(ts));
		
		// Float
		assertEquals("1.23", Convert.toString(1.23F));
		assertEquals("2.34", Convert.toString(new Float(2.34F)));
		// Short
		assertEquals("321", Convert.toString((short)321));
		assertEquals("432", Convert.toString(new Short((short)432)));
		// Boolean
		assertEquals("true", Convert.toString(true));
		assertEquals("false", Convert.toString(false));
		assertEquals("true", Convert.toString(Boolean.TRUE));
		assertEquals("false", Convert.toString(Boolean.FALSE));
	}

	public void testToDate() {
		Calendar cal = new GregorianCalendar(2001,0,1); // Monate 0-Relativ!
		assertEquals(cal.getTime(), Convert.toDate("1.1.2001"));
		cal = new GregorianCalendar(2001,11,1);
		assertEquals(cal.getTime(), Convert.toDate("01.12.2001"));

		cal = new GregorianCalendar(1970,0,1, 12,13); // 1.1.1970 wenn ohne Datum
		assertEquals(cal.getTime(), Convert.toDate("12:13"));
        // "23.02.2006 11:47:56"
		Convert.toDateTime("23.02.2006 11:47:56.000000");
        assertNull(Convert.getLastException());        
	}
	public void testIsDate() {
		assertEquals(true, Convert.isDate("1.1.2001"));
		assertEquals(true, Convert.isDate("29.2.2004"));
		assertEquals(false, Convert.isDate("29.2.2005"));
		assertEquals(false, Convert.isDate("0.13.2001"));
		assertEquals(false, Convert.isDate("BlaBlub"));
		assertEquals(false, Convert.isDate(""));
		assertEquals(false, Convert.isDate(null));
	}
	public void testBirthdate() {
		assertEquals(Convert.toDate("11.12.1955"), Convert.toBirthdate("11.12.55"));
		assertEquals(Convert.toDate("01.04.2004"), Convert.toBirthdate("1.4.4"));
		assertNull(Convert.toBirthdate(null));
		assertNull(Convert.toBirthdate(""));
		assertNull(Convert.toBirthdate("BlaBlub"));
		assertNotNull(Convert.getLastException());
	}
	public void testColor() {
		assertNull(Convert.toString((Color)null));
		assertEquals("255,255,255", Convert.toString(Color.WHITE));
		assertEquals(Color.WHITE, Convert.toColor("255,255,255"));
	}
	public void testByte() {
		assertEquals("123", Convert.toString((byte)123));
		assertEquals("124", Convert.toString(new Byte((byte)124)));
	}
	public void testDimension() {
		assertEquals("123.0,456.0", Convert.toString(new Dimension(123,456)));
		assertEquals(new Dimension(123,321), Convert.toDimension("123,321"));
		assertEquals(new Dimension(123,321), Convert.toDimension("123.321"));
		assertNull(Convert.toDimension(null));
	}
	public void testGet() {
		Convert.getDateFormat();
		Convert.getDateTimeFormat();
		Convert.getTimeFormat();
		Convert.getSqlTimeFormat();
		assertEquals(false, Convert.isLenient());
	}
	public void testType() {
		assertNull(Convert.toString(null, Types.OTHER));

		assertEquals("123,45", Convert.toString(new BigDecimal("123.45"), Types.BIGINT)); // TODO INT mit Nachkomma??
		assertEquals("12345678", Convert.toString(new BigInteger("12345678"), Types.BIGINT)); 
		assertEquals("1234567", Convert.toString(new Long(1234567), Types.BIGINT)); 
		assertEquals("12345", Convert.toString(new Integer(12345), Types.BIGINT)); 
		assertEquals("12345", Convert.toString(new Short((short)12345), Types.BIGINT)); 
		assertEquals("123", Convert.toString(new Byte((byte)123), Types.BIGINT)); 
		// Oh weh! "," <--> "."
		assertEquals("123,45", Convert.toString(new BigDecimal("123.45"), Types.DECIMAL)); 
		assertEquals("123.45", Convert.toString(new Double(123.45), Types.DECIMAL)); 
		assertEquals("123.45", Convert.toString(new Float(123.45), Types.DECIMAL)); 
	}
}