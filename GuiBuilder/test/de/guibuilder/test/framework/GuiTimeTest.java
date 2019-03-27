/*
 * Created on 30.07.2003
 */
package de.guibuilder.test.framework;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.lang.NullPointerException;
import java.lang.IllegalArgumentException;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiElement;
import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiTime;
/**
 * @author kknobloch
 */
public class GuiTimeTest extends GuiComponentTest {

	GuiTime myTime = new GuiTime();
	public GuiComponent getTestGuiComponent() {
		return this.myTime;
	}
	public GuiElement getTestGuiElement() {
		return this.myTime;
	} 
	public GuiMember getTestGuiMember() {
		return this.myTime;
	} 

	public boolean validateGuiMemberValue(String otyp, Object o) {
		return false;
	}

	/**
	 * Der Konstruktor GuiTime(String) setzt den Label und den Namen
	 * der GuiComponent. Endet der String mit einem Doppelpunkt, wird
	 * dieser nicht in den Namen übernommen. Der erste Buchstabe des
	 * Namens ist immer klein.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Neuen GuiTime(String) anlegen und Label prüfen
	 * <br><i>Der Label muß "MeinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Namen des GuiTime prüfen
	 * <br><i>Der Name muß "meinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Neuen GuiTime(String) anlegen und Label prüfen
	 * <br><i>Der Label muß "MeinFeld:" sein</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Namen des GuiTime prüfen
	 * <br><i>Der Name muß "meinFeld" sein</i>
	 */
	public void testConstructorString() {
		
		GuiTime t1 = new GuiTime("MeinLabel");
		assertEquals("(1)", "MeinLabel", t1.getLabel());
		assertEquals("(2)", "meinLabel", t1.getName());

		GuiTime t2 = new GuiTime("MeinFeld:");
		assertEquals("(3)", "MeinFeld:", t2.getLabel());
		assertEquals("(4)", "meinFeld", t2.getName());
	}
	/**
	 * Die Methode getTag() liefert den String "Time" zurück.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getTag() ausführen
	 * <br><i>Rückgabewert muß "Time" sein</i>
	 */
	public void testGetTag() {
		assertEquals("Time",this.myTime.getTag());
	}
	/**
	 * Die Methode getDataType() liefert den Datentyp einer Komponenten zurück. 
	 * <br>Bei einem GuiTime muß dies TIME sein.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Datentyp der Komponente abfragen
	 * <br><i>Der Datentyp muß TIME sein</i>
	 */
	public void testGetDataType() {
		assertEquals(GuiComponent.TIME,this.myTime.getDataType());
	}
	/**
	 * Die Methode getGuiType() liefert den Typ der Gui Komponenten zurück.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Typ für GuiTime abfragen
	 * <br><i>Der Typ muß GUI_COMPONENT sein.</i>
	 */
	public void testGetGuiType() {
		assertEquals(GuiComponent.GUI_COMPONENT,this.myTime.getGuiType());
	}
	/**
	 * Die Methode setText() wird von GuiTime überschrieben, da durch 
	 * setText("NOW") die aktuelle Uhrzeit eingestellt werden kann.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</b>
	 * <br>Dem GuiTime den Text "NOW" zuweisen
	 * <br><i>Der Value von GuiTime muß der aktuellen Uhrzeit entsprechen</i>
	 * <br>
	 * <br><u>Testschritt 2</b>
	 * <br>Dem GuiTime den Text "now" zuweisen
	 * <br><i>Der Value von GuiTime muß "now" sein</i>
	 */
	public void testSetTextNow() {
		Date date = new Date();
		this.myTime.setText("NOW");
		
		SimpleDateFormat format = new SimpleDateFormat();
		String sd = format.format(date);
		/*
		 * Na ja...
		 * In sd steht sowas "01.01.1999 14:00".
		 * split(" ") macht daraus sd[0]="01.01.1999" und sd[1]="14:00". 
		 * sd.split(" ")[1] ist dann die Uhrzeit !
		 */
		assertEquals("(1)",sd.split(" ")[1], this.myTime.getText());
		
		this.myTime.setText("now");
		assertEquals("(2)","now", this.myTime.getText());
		
	}
	/**
	 * Die Methode getValueClass() liefert die Date-Klasse der
	 * Komponente zurück
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getValueClass() ausführen
	 * <br><i>Rückgabewert muß eine Date-Class sein</i>
	 */
	public void testGetValueClass() {
		assertEquals("class java.util.Date", this.myTime.getValueClass().toString());
	}
	/**
	 * Die Methode getValue() liefert den Inhalt der Komponente zurück. 
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Inhalt auslesen
	 * <br><i>Initial ist der Inhalt leer</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Inhalt zuweisen und auslesen
	 * <br><i>Es wird der korrekte Inhalt zurückgegeben</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Einen leeren String als Inhalt zuweisen
	 * <br><i>Es wird der korrekte Inhalt zurückgegeben</i>
	 * <br>
	 * <br><u>Schritt 4</u>
	 * <br>Null als Inhalt zuweisen
	 * <br><i>Es wird der korrekte Inhalt zurückgegeben</i>
	 */
	public void testGetValue() {
		
		assertNull("(1)", this.myTime.getValue());
		
		this.myTime.setValue("myComp new Value");
		assertNull("(2)", this.myTime.getValue());
		
		this.myTime.setValue("");
				assertNull("(3)", this.myTime.getValue());
		
		this.myTime.setValue(null);
		assertNull("(4)", this.myTime.getValue());
	}
	/**
	 * Die Methode getFormat() liefert das Format des GuiTime zurück.
	 * <br>Die Methode setFormat() setzt das Format der Komponente.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getFormat() ausführen, um das Standard-Format zu prüfen.
	 * <br><i>Rückgabewert muß "HH:mm" sein</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Format umsetzen und prüfen ob es korrekt gesetzt wurde.
	 * <br><i>Rückgabewert muß "HH:mm:ss" sein</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Format umsetzen - falls im vorgehenden Test das Standardformat erwischt wurde - und prüfen ob es korrekt gesetzt wurde.
	 * <br><i>Rückgabewert muß "mm:ss" sein</i>
	 * <br>
	 * <br><u>Schritt 4</u>
	 * <br>null als Format übergeben.
	 * <br><i>Es muß eine NullPointerException geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 5</u>
	 * <br>Beliebigen String als Format übergeben
	 * <br><i>Es muß eine IllegalArgumentException geworfen werden</i>
	 */
	public void testGetSetFormat() {
		
		SimpleDateFormat fmt = this.myTime.getFormat();
		assertEquals("(1)", "HH:mm", fmt.toPattern());

		this.myTime.setFormat("HH:mm:ss");
		fmt = this.myTime.getFormat();
		assertEquals("(2)", "HH:mm:ss", fmt.toPattern());
		
		this.myTime.setFormat("mm:ss");
		fmt = this.myTime.getFormat();
		assertEquals("(3)", "mm:ss", fmt.toPattern());
		
		try {
			this.myTime.setFormat(null);
			fail("(4) Es wird keine NullPointerException geworfen");
		}
		catch(NullPointerException e) {
			
		}
		try {
			this.myTime.setFormat("Heiner");
			fail("(5) Es wird keine IllegalArgumentException geworfen");
		}
		catch(IllegalArgumentException e) {
			
		}
	}
	/*
	 * Die Methode setFormat() wird im vorhergehenden Testfall
	 * mit gepürft.
	 *
	public void testSetFormat() {
		assertEquals("nicht implementiert", false, true);
	}
	*/
	/**
	 * Die Methode getDefaultFormat() liefert das Standard-Format des GuiTime zurück.
	 * <br>Die Methode setDefaultFormat() setzt das Standard-Format der Komponente.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getDefaultFormat() ausführen, um das Standard-Format zu prüfen.
	 * <br><i>Rückgabewert muß "HH:mm" sein</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Format umsetzen und prüfen ob es korrekt gesetzt wurde.
	 * <br><i>Rückgabewert muß "HH:mm:ss" sein</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>null als Format übergeben.
	 * <br><i>Es muß eine NullPointerException geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 4</u>
	 * <br>Beliebigen String als Format übergeben
	 * <br><i>Es muß eine IllegalArgumentException geworfen werden</i>
	 */
	public void testGetSetDefaultFormat() {
		
		SimpleDateFormat fmt = GuiTime.getDefaultFormat();
		assertEquals("(1)", "HH:mm", fmt.toPattern());

		GuiTime.setDefaultFormat("HH:mm:ss");
		fmt = GuiTime.getDefaultFormat();
		assertEquals("(2)", "HH:mm:ss", fmt.toPattern());
		
		try {
			GuiTime.setDefaultFormat(null);
			fail("(3) Es wird keine NullPointerException geworfen");
		}
		catch(NullPointerException e) {
			
		}
		try {
			GuiTime.setDefaultFormat("Heiner");
			fail("(4) Es wird keine IllegalArgumentException geworfen");
		}
		catch(IllegalArgumentException e) {
			
		}
	}
	/*
	 * Die Methode getDefaultFormat() wird im vorhergehenden Testfall
	 * mit geprüft.
	 *
	public void testGetDefaultFormat() {
		assertEquals("nicht implementiert", false, true);
	}
	*/
	/**
	 * Die Methode getValueTime() liefert den Inhalt von GuiTime als
	 * java.sql.Time-Objekt zurück.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</b>
	 * <br>GuiTime mit einem gültigen Wert versorgen und prüfen
	 * <br><i>getValueTime() liefert den korrekt Wert
	 * <br>
	 * <br><u>Testschritt 2</b>
	 * <br>GuiTime entleeren und den Wert prüfen
	 * <br><i>getValueTime() liefert null</i>
	 */
	public void testGetValueTime() {
		/*
		 * Format von GuiTime mit java.sql.Time.toString() abgleichen
		 */
		this.myTime.setFormat("HH:mm:ss");
		
		Date date = new Date();
		this.myTime.setText("NOW");
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		String sd = format.format(date);

		java.sql.Time sqlTime = this.myTime.getValueTime();
		assertEquals("(1)",sd, sqlTime.toString());
		
		this.myTime.setValue(null);
		sqlTime = this.myTime.getValueTime();
		assertEquals("(2)",null, sqlTime);
				
	}
	/**
	 * Die Methode verify() führ den InputVerifier der GuiComponent aus.
	 * <br>Im Fehlerfall wird eine IllegalStateException geworfen. 
	 * <br>GuiTime wandelt ziemlich viel in eine Uhrzeit um. So wird z.B.
	 * 24:00 in 00:00 umgesetzt und 01:60 in 02:00. Da hier der Verifyer
	 * geprüft wird, werden hier auch diese Umwandlungen geprüft.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Prüfen, ob an der Komponente ein InputVerifyer vorhanden ist
	 * <br><i>GuiTime muß einen InputVerifyer besitzen.</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Wert NOW zuweisen und verify ausführen.
	 * <br><i>Es darf keine Exception geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Wert "12:00" zuweisen und verify ausführen.
	 * <br><i>Es darf keine Exception geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 4</u>
	 * <br>Wert "00:00" zuweisen und verify ausführen.
	 * <br><i>Es darf keine Exception geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 5</u>
	 * <br>Wert "24:00" zuweisen und verify ausführen.
	 * <br><i>Es darf keine Exception geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 6</u>
	 * <br>Wert "05:60" zuweisen und verify ausführen.
	 * <br><i>Es darf keine Exception geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 7</u>
	 * <br>Wert "05:100" zuweisen und verify ausführen.
	 * <br><i>Es muß eine Exception geworfen werden</i>
	 */
	public void testVerify() {
		
		assertNotNull("(1)",this.myTime.getJComponent().getInputVerifier());
		
		this.myTime.setText("NOW");
		try{
			this.myTime.verify();
		}
		catch( IllegalStateException e) {
			fail("(2) FEHLER: IllegalStateException wurde geworfen.");
		}
		
		this.myTime.setText("12:00");
		try{
			this.myTime.verify();
		}
		catch( IllegalStateException e) {
			fail("(3) FEHLER: IllegalStateException wurde geworfen.");
		}

		this.myTime.setText("00:00");
		try{
			this.myTime.verify();
		}
		catch( IllegalStateException e) {
			fail("(4) FEHLER: IllegalStateException wurde geworfen.");
		}

		this.myTime.setText("24:00");
		try{
			this.myTime.verify();
		}
		catch( IllegalStateException e) {
			fail("(5) FEHLER: IllegalStateException wurde geworfen.");
		}
		
		this.myTime.setText("05:60");
		try{
			this.myTime.verify();
		}
		catch( IllegalStateException e) {
			fail("(6) FEHLER: IllegalStateException wurde geworfen.");
		}

		this.myTime.setText("05:100");
		try{
			this.myTime.verify();
			fail("(7) FEHLER: IllegalStateException wurde nicht geworfen.");
		}
		catch( IllegalStateException e) {
		}
	}
	/**
	 * Die Methode reset() setzt den Value des GuiTime auf null und das 
	 * Modified-Kennzeichen auf false
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Modified-Kennzeichen auf true setzen und dem GuiTime einen Wert zuweise.
	 * <br><i>Modified-Kennzeichen steht auf true<br>Der zugewiesene Wert ist gesetzt.</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Ausführen der Methode reset()
	 * <br><i>Modified-Kennzeichen steht auf false<br>Der Value des GuiTime ist ein leerer String.</i>
	 */
	public void testReset() {
		/*
		 * Achtung: Reihenfolge ist wichtig !!!
		 * Erst den neuen Value und dann das modified-Kennzeichen auf 
		 * true setzen, da setValue() das modified-Kennzeichen auf false 
		 * einstellt.
		 */
		Date date = new Date();
		this.myTime.setText("NOW");
		
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		String sd = format.format(date);

		this.myTime.setText("NOW");
		this.myTime.setModified(true);
		
		assertEquals("(1)", true, this.myTime.isModified());
		assertEquals("(2)", sd, this.myTime.getText());
		
		this.myTime.reset();
		
		assertEquals("(3)", false, this.myTime.isModified());
		assertNull("(4)", this.myTime.getValue());
	}
}
