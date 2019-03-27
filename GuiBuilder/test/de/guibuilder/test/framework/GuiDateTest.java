/*
 * Created on 24.07.2003
 *
 */
package de.guibuilder.test.framework;

import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiElement;
import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiDate;

/**
 * @author kknobloch
 *
 */
public class GuiDateTest extends GuiComponentTest {

	GuiDate myDate = new GuiDate();

	/* (non-Javadoc)
	 * @see de.guibuilder.test.framework.GuiComponentTest#getTestGuiComponent()
	 */
	public GuiComponent getTestGuiComponent() {
		return this.myDate;
	}
	/* (non-Javadoc)
	 * @see de.guibuilder.test.framework.GuiElementTest#getTestGuiElement()
	 */
	public GuiElement getTestGuiElement() {
		return this.myDate;
	}
	/* (non-Javadoc)
	 * @see de.guibuilder.test.framework.GuiMemberTest#getTestGuiMember()
	 */
	public GuiMember getTestGuiMember() {
		return this.myDate;
	}
	/* (non-Javadoc)
	 * @see de.guibuilder.test.framework.GuiMemberTest#validateGuiMemberValue(java.lang.String, java.lang.Object)
	 */
	public boolean validateGuiMemberValue(String otyp, Object o) {
		return false;
	}

	/**
	 * Der Konstruktor GuiDate(String) setzt den Label und den Namen
	 * der GuiComponent. Endet der String mit einem Doppelpunkt, wird
	 * dieser nicht in den Namen �bernommen. Der erste Buchstabe des
	 * Namens ist immer klein.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Neuen GuiDate(String) anlegen und Label pr�fen
	 * <br><i>Der Label mu� "MeinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Namen des GuiDate pr�fen
	 * <br><i>Der Name mu� "meinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Neuen GuiDate(String) anlegen und Label pr�fen
	 * <br><i>Der Label mu� "MeinFeld:" sein</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Namen des GuiDate pr�fen
	 * <br><i>Der Name mu� "meinFeld" sein</i>
	 */
	public void testConstructorString() {
		
		GuiDate t1 = new GuiDate("MeinLabel");
		assertEquals("(1)", "MeinLabel", t1.getLabel());
		assertEquals("(2)", "meinLabel", t1.getName());

		GuiDate t2 = new GuiDate("MeinFeld:");
		assertEquals("(3)", "MeinFeld:", t2.getLabel());
		assertEquals("(4)", "meinFeld", t2.getName());
	}
	/**
	 * Die Methode getValue() liefert den Inhalt der Komponente zur�ck. 
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Inhalt auslesen
	 * <br><i>Initial ist der Inhalt leer</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Inhalt zuweisen und auslesen
	 * <br><i>Es wird der korrekte Inhalt zur�ckgegeben</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Einen leeren String als Inhalt zuweisen
	 * <br><i>Es wird der korrekte Inhalt zur�ckgegeben</i>
	 */
	public void testGetValue() {
		assertEquals("(1)","", this.myDate.getValue());
		
		this.myDate.setValue("myComp new Value");
		assertNull("(2)", this.myDate.getValue());
		
		this.myDate.setValue("");
		assertEquals("(3)","", this.myDate.getValue());
	}

	/**
	 * Die Methode getTag() liefert den String "Date" zur�ck.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getTag() ausf�hren
	 * <br><i>R�ckgabewert mu� "Date" sein</i>
	 */
	public void testGetTag() {
		assertEquals("Date", this.myDate.getTag());
	}

	/**
	 * Die Methode getDataType() liefert den Datentyp einer Komponenten zur�ck. 
	 * <br>Bei einem GuiText mu� dies STRING sein.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Datentyp der Komponente abfragen
	 * <br><i>Der Datentyp mu� DATE sein</i>
	 */
	public void testGetDataType() {
		assertEquals(GuiDate.DATE,this.myDate.getDataType());
	}

	/**
	 * Die Methode getGuiType() liefert den Typ der Gui Komponenten zur�ck.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Typ f�r GuiText abfragen
	 * <br><i>Der Typ mu� GUI_COMPONENT sein.</i>
	 */
	public void testGetGuiType() {
		assertEquals(GuiDate.GUI_COMPONENT,this.myDate.getGuiType());
	}

	/**
	 * Mit der Methode setText() kann �ber den Parameter "TODAY" das
	 * GuiDate auf das Tagesdatum eingestellt werden. 
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Aktuellen Inhalt von GuiDate abfragen
	 * <br><i>Value mu� leer sein</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Aufruf von setText() mit Parameter "HEUTE"
	 * <br><i>Value mu� "HEUTE" sein</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Aufruf von setText() mit Parameter "TODAY"
	 * <br><i>Value mu� Tagesdatum sein</i>
	 * <br>
	 * <br><u>Schritt 4</u>
	 * <br>Aufruf von setText() mit Parameter "HEUTE"
	 * <br><i>Value mu� "HEUTE" sein</i>
	 */
	public void testSetText() {

		SimpleDateFormat defaultFormat = new SimpleDateFormat("dd.MM.yyyy");
		Date date = new Date();
		String sd = defaultFormat.format(date); 
		
		assertEquals("(1)","",this.myDate.getValue());
		
		this.myDate.setText("HEUTE");
		assertNull("(2)",this.myDate.getValue());
		
		this.myDate.setText("TODAY");
		assertEquals("(3)",sd,this.myDate.getValue());

		this.myDate.setText("HEUTE");
		assertNull("(4)",this.myDate.getValue());
		
	}

	/**
	 * Die Methode getValueClass() liefert die Date-Klasse der
	 * Komponente zur�ck
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getValueClass() ausf�hren
	 * <br><i>R�ckgabewert mu� eine Date-Class sein</i>
	 */
	public void testGetValueClass() {
		assertEquals("class java.util.Date",this.myDate.getValueClass().toString());
	}

	/**
	 * Die Methode getFormat() liefert das Format des GuiDate zur�ck.
	 * <br>Die Methode setFormat() setzt das Format der Komponente.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getFormat() ausf�hren, um das Standard-Format zu pr�fen.
	 * <br><i>+ R�ckgabewert mu� "dd.MM.yyyy" sein
	 * <br>+ Das Datum mu� "28.02.2003" lauten</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Format umsetzen und pr�fen ob es korrekt gesetzt wurde.
	 * <br><i>+ R�ckgabewert mu� "yyyyMMdd" sein
	 * <br>+ Das Datum mu� "20030228" lauten</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Format umsetzen - falls im vorgehenden Test das Standardformat erwischt wurde - und pr�fen ob es korrekt gesetzt wurde.
	 * <br><i>+ R�ckgabewert mu� "MMddyyyy" sein
	 * <br>+ Das Datum mu� "02282003" lauten</i>
	 * 
	 */
	public void testGetSetFormat() {
		Calendar c = Calendar.getInstance();
		/*
		 * Der Monat mu� 0-relativ angegeben werden.
		 * Alos Januar = 0,...,Dezember = 11
		 */
		c.set(2003,2-1,28);
		Date d = c.getTime();
		
		this.myDate.setValue(d);
		SimpleDateFormat myFmt = (SimpleDateFormat) this.myDate.getFormat();
		assertEquals("(1)","dd.MM.yyyy",myFmt.toPattern());
		assertEquals("(2)","28.02.2003",this.myDate.getValue());
			  
		this.myDate.setFormat("yyyyMMdd");
		/*
		 * Nach �nderung des Formates mu� der Value neu gesetzt werden.
		 */
		this.myDate.setValue(d);
		myFmt = (SimpleDateFormat) this.myDate.getFormat();
		assertEquals("(3)","yyyyMMdd",myFmt.toPattern());
		assertEquals("(4)","20030228",this.myDate.getValue());
	  	
		this.myDate.setFormat("MMddyyyy");
		this.myDate.setValue(d);
		myFmt = (SimpleDateFormat) this.myDate.getFormat();
		assertEquals("(5)","MMddyyyy",myFmt.toPattern());
		assertEquals("(6)","02282003",this.myDate.getValue());
	}
	/*
	 * Die Methode setFormat() wird im vorhergehenden Testfall
	 * mit gepr�ft.
	 *
	public void testSetFormat() {
		assertEquals("nicht implementiert",false,true);
	}
	*/
	/**
	 * Die Methode getValueDate() liefert den Text-Inhalt der Komponente
	 * als Date-Objekt zur�ck.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Komponente �ber setText() mit dem Wert "01.01.2003" belegen 
	 * und anschlie�end den DateValue abfragen.
	 * <br><i>Der DateValue mu� dem 01.01.2003 entsprechen</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Komponente �ber setText() mit dem Wert "14.02.1966" belegen 
	 * und anschlie�end den DateValue abfragen.
	 * <br><i>Der DateValue mu� dem 14.02.1966 entsprechen</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Komponente �ber setText() mit dem Wert "06.12.88" belegen 
	 * und anschlie�end den DateValue abfragen.
	 * <br><i>Der DateValue mu� dem 06.12.1988 entsprechen</i>
	 */
	public void testGetValueDate() {
		Calendar c = Calendar.getInstance();
		
		c.set(2003,1-1, 1, 0, 0, 0);
		Date d = c.getTime();
		
		this.myDate.setText("01.01.2003");
		Date cDate = this.myDate.getValueDate();
		
		assertEquals("(1)", d.toString(), cDate.toString());
				
		c.set(1966, 2-1, 14, 0, 0, 0);
		d = c.getTime();
		
		this.myDate.setText("14.02.1966");
		cDate = this.myDate.getValueDate();

		assertEquals("(2)", d.toString(), cDate.toString());

		c.set(1988, 12-1, 6,0,0,0);
		d = c.getTime();
		
		this.myDate.setText("06.12.1988");
		cDate = this.myDate.getValueDate();

		assertEquals("(3)", d.toString(), cDate.toString());
	}

	/**
	 * Die Methode getValueSqlDate() liefert den Text-Inhalt der Komponente
	 * als java.sql.Date-Objekt zur�ck.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Komponente �ber setText() mit dem Wert "01.01.2003" belegen 
	 * und anschlie�end den DateValue abfragen.
	 * <br><i>Der DateValue mu� dem 01.01.2003 entsprechen</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Komponente �ber setText() mit dem Wert "14.02.1966" belegen 
	 * und anschlie�end den DateValue abfragen.
	 * <br><i>Der DateValue mu� dem 14.02.1966 entsprechen</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Komponente �ber setText() mit dem Wert "06.12.88" belegen 
	 * und anschlie�end den DateValue abfragen.
	 * <br><i>Der DateValue mu� dem 06.12.1988 entsprechen</i>
	 */
	public void testGetValueSqlDate() {
		Calendar c = Calendar.getInstance();
		
		c.set(2003,1-1, 1,0,0,0);
		
		java.sql.Date sqlDate = new java.sql.Date(c.getTimeInMillis());
		this.myDate.setText("01.01.2003");
		java.sql.Date cDate = this.myDate.getValueSqlDate();
		
		assertEquals("(1)", sqlDate.toString(), cDate.toString());
				
		c.set(1966, 2-1, 14,0,0,0);
		sqlDate.setTime(c.getTimeInMillis());
		
		this.myDate.setText("14.02.1966");
		cDate = this.myDate.getValueSqlDate();

		assertEquals("(2)", sqlDate.toString(), cDate.toString());
		
		c.set(1988, 12-1, 6,0,0,0);
		sqlDate.setTime(c.getTimeInMillis());
		
		this.myDate.setText("06.12.1988");
		cDate = this.myDate.getValueSqlDate();

		assertEquals("(3)", sqlDate.toString(), cDate.toString());
	}

	/**
	 * Die Methode reset() setzt den Value des GuiText auf null und das 
	 * Modified-Kennzeichen auf false
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Modified-Kennzeichen auf true setzen und dem GuiText einen Wert zuweise.
	 * <br><i>Modified-Kennzeichen steht auf true<br>Der zugewiesene Wert ist gesetzt.</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Ausf�hren der Methode reset()
	 * <br><i>Modified-Kennzeichen steht auf false<br>Der Value des GuiText ist ein leerer String.</i>
	 */
	public void testReset() {
		/*
		 * Achtung: Reihenfolge ist wichtig !!!
		 * Erst den neuen Value und dann das modified-Kennzeichen auf 
		 * true setzen, da setValue() das modified-Kennzeichen auf false 
		 * einstellt.
		 */
		this.myDate.setValue("12.01.2003");
		this.myDate.setModified(true);
		
		assertEquals("(1)", true, this.myDate.isModified());
		assertEquals("(2)", "12.01.2003", this.myDate.getValue());
		
		this.myDate.reset();
		
		assertEquals("(3)", false, this.myDate.isModified());
		assertEquals("(4)", "", this.myDate.getValue());
	}
	/**
	 * Mit der Methode setValue(Date) kann der Wert der GuiDate-Komponente
	 * als Date-Objekt gesetzt werden.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Date-Objekt mit Value "10.05.2002" instazieren und an GuiDate �bergeben.
	 * <br><i>Die Komponente mu� "10.05.2002" als Text zur�ckgeben</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Date-Objekt mit Value "16.06.2003" instazieren und an GuiDate �bergeben.
	 * <br><i>Die Komponente mu� "16.06.2003" als Text zur�ckgeben</i>
	 */	
	public void testSetValueDate() {
		Calendar c = Calendar.getInstance();
		
		c.set(2002,5-1, 10);
		Date d = c.getTime();
		this.myDate.setValue(d);
		assertEquals("(1)","10.05.2002", this.myDate.getText());
		
		c.set(2003,6-1, 16);
		d = c.getTime();
		this.myDate.setValue(d);
		assertEquals("(2)","16.06.2003", this.myDate.getText());

	}

	/**
	 * Mit der Methode setValue(long) kann der Wert der GuiDate-Komponente
	 * als Anzahl der Millisekunden seit 01.01.1970 gesetzt werden.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Calendar-Objekt mit Value "10.05.2002" instazieren 
	 * und an GuiDate �bergeben.
	 * <br><i>Die Komponente mu� "10.05.2002" als Text zur�ckgeben</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Calendar-Objekt mit Value "16.06.2003" instazieren und 
	 * an GuiDate �bergeben.
	 * <br><i>Die Komponente mu� "16.06.2003" als Text zur�ckgeben</i>
	 */	
	public void testSetValuelong() {
		Calendar c = Calendar.getInstance();
		
		c.set(2002,5-1, 10);
		this.myDate.setValue(c.getTimeInMillis());
		assertEquals("(1)","10.05.2002", this.myDate.getText());
		
		c.set(2003,6-1, 16);
		this.myDate.setValue(c.getTimeInMillis());
		assertEquals("(2)","16.06.2003", this.myDate.getText());
	}

	/**
	 * Mit der Methode setValue(Long) kann der Wert der GuiDate-Komponente
	 * als Anzahl der Millisekunden seit 01.01.1970 in einem Long-Objekt gesetzt werden.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Calendar-Objekt mit Value "10.05.2002" instazieren 
	 * und an GuiDate �bergeben.
	 * <br><i>Die Komponente mu� "10.05.2002" als Text zur�ckgeben</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Calendar-Objekt mit Value "16.06.2003" instazieren und 
	 * an GuiDate �bergeben.
	 * <br><i>Die Komponente mu� "16.06.2003" als Text zur�ckgeben</i>
	 */	
	public void testSetValueLong() {
		Calendar c = Calendar.getInstance();
		
		c.set(2002,5-1, 10);
		Long lTime = new Long(c.getTimeInMillis());
		this.myDate.setValue(lTime);
		assertEquals("(1)","10.05.2002", this.myDate.getText());
		
		c.set(2003,6-1, 16);
		lTime = new Long(c.getTimeInMillis());
		this.myDate.setValue(lTime);
		assertEquals("(2)","16.06.2003", this.myDate.getText());
	}
	/**
	 * Mit der Method setDefaultFormat() kann das Standardformat vom
	 * GuiDate ge�ndert werden.
	 * <br>Mit der Methode getDefaultFormat() kann das Standardformat von
	 * GuiDate abgefragt werden.
	 *<br>
	 *<br><b>Testschritte</b>
	 *<br>
	 *<br><u>Testschritt 1</u>
	 *<br>Voreingestelltest Standardformat von GuiDate abfragen
	 *<br><i>Das Format mu� "dd.MM.yyyy" sein</i>
	 *<br>
	 *<br><u>Testschritt 2</u>
	 *<br>Standardformat von GuiDate �ndern und abfragen
	 *<br><i>Das Format mu� "yyyyMMdd" sein</i>
	 */
	public void testGetSetDefaultFormat() {
	
		SimpleDateFormat myFmt = (SimpleDateFormat) GuiDate.getDefaultFormat();
		assertEquals("(1)","dd.MM.yyyy",myFmt.toPattern());
		
		GuiDate.setDefaultFormat("yyyyMMdd");
		myFmt = (SimpleDateFormat) GuiDate.getDefaultFormat();
		assertEquals("(2)","yyyyMMdd",myFmt.toPattern());
	
		
	}
	/*
	 * Die Methode getDefaultFormat() wird im vorhergehenden Testfall
	 * mit gepr�ft.
	 *
	public void testGetDefaultFormat() {
		assertEquals("nicht implementiert",false,true);
	}
	*/
	/**
	 * Die Methode verify() pr�ft den Inhalt der Komponente anhand des
	 * InputVerifyer. Bei GuiDate sind nur Datumwerte erlaubt.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>G�litges Datum eintragen und Pr�fung ansto�en.
	 * <br><i>Es erfolgt keine Fehlermeldung</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Ung�litgen 29.02. eintragen und Pr�fung ansto�en.
	 * <br><i>Es erfolgt eine Fehlermeldung</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>G�litgen 29.02. eintragen und Pr�fung ansto�en.
	 * <br><i>Es erfolgt keine Fehlermeldung</i>
	 * <br>
	 * <br><u>Testschritt 4</u>
	 * <br>Einen beliebigen ung�ltigen Text eintragen und Pr�fung ansto�en.
	 * <br><i>Es erfolgt eine Fehlermeldung</i>
	 */
	public void testVerify() {

		this.myDate.setValue("01.01.2003");
		try{
			this.myDate.verify();
		}
		catch( IllegalStateException e) {
			fail("(1) FEHLER: IllegalStateException wurde geworfen.");
		}
		
		this.myDate.setValue("29.02.2003");
		try{
			this.myDate.verify();
			fail("(2) FEHLER: IllegalStateException wurde nicht geworfen.");
		}
		catch( IllegalStateException e) {
			
		}

		this.myDate.setValue("29.02.2004");
		try{
			this.myDate.verify();
		}
		catch( IllegalStateException e) {
			fail("(3) FEHLER: IllegalStateException wurde geworfen.");
		}

		this.myDate.setValue("Heiner was here...");
		try{
			this.myDate.verify();
			fail("(4) FEHLER: IllegalStateException wurde nicht geworfen.");
		}
		catch( IllegalStateException e) {
			
		}
		
	}
}
