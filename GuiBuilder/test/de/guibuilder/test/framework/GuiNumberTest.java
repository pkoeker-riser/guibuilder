/*
 * Created on 21.07.2003
 *
 */
package de.guibuilder.test.framework;

import java.text.DecimalFormat;
import java.math.BigDecimal;

import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiElement;
import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiNumber;

/**
 * @author kknobloch
 *
 */
public class GuiNumberTest extends GuiComponentTest {

	GuiNumber myNumber = new GuiNumber();

	/* (non-Javadoc)
	 * @see de.guibuilder.test.framework.GuiComponentTest#getTestGuiComponent()
	 */
	public GuiComponent getTestGuiComponent() {
		return this.myNumber;
	}
	/* (non-Javadoc)
	 * @see de.guibuilder.test.framework.GuiElementTest#getTestGuiElement()
	 */
	public GuiElement getTestGuiElement() {
		return this.myNumber;
	}
	/* (non-Javadoc)
	 * @see de.guibuilder.test.framework.GuiMemberTest#getTestGuiMember()
	 */
	public GuiMember getTestGuiMember() {
		return this.myNumber;
	}
	/* (non-Javadoc)
	 * @see de.guibuilder.test.framework.GuiMemberTest#validateGuiMemberValue(java.lang.String, java.lang.Object)
	 */
	public boolean validateGuiMemberValue(String otyp, Object o) {
		return false;
	}

	/**
	 * Der Konstruktor GuiNumber(String) setzt den Label und den Namen
	 * der GuiComponent. Endet der String mit einem Doppelpunkt, wird
	 * dieser nicht in den Namen übernommen. Der erste Buchstabe des
	 * Namens ist immer klein.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Neuen GuiNumber(String) anlegen und Label prüfen
	 * <br><i>Der Label muß "MeinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Namen des GuiNumber prüfen
	 * <br><i>Der Name muß "meinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Neuen GuiNumber(String) anlegen und Label prüfen
	 * <br><i>Der Label muß "MeinFeld:" sein</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Namen des GuiNumber prüfen
	 * <br><i>Der Name muß "meinFeld" sein</i>
	 */
	public void testConstructorString() {
		
		GuiNumber t1 = new GuiNumber("MeinLabel");
		assertEquals("(1)", "MeinLabel", t1.getLabel());
		assertEquals("(2)", "meinLabel", t1.getName());

		GuiNumber t2 = new GuiNumber("MeinFeld:");
		assertEquals("(3)", "MeinFeld:", t2.getLabel());
		assertEquals("(4)", "meinFeld", t2.getName());
	}


	/**
	 * Die Methode getDataType() liefert den Datentyp einer Komponenten zurück. 
	 * <br>Bei einem GuiText muß dies STRING sein.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Datentyp der Komponente abfragen
	 * <br><i>Der Datentyp muß NUMBER sein</i>
	 */
	public void testGetDataType() {

		assertEquals(GuiNumber.NUMBER,this.myNumber.getDataType());

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
		
		assertEquals("(1)","", this.myNumber.getValue());
		
		this.myNumber.setValue("myComp new Value");
		assertNull("(2)",this.myNumber.getValue());
		
		this.myNumber.setValue("");
		assertEquals("(3)","", this.myNumber.getValue());
		
		this.myNumber.setValue((Integer)null);
		assertEquals("(4)","", this.myNumber.getValue());
	}
	/**
	 * Die Methode getGuiType() liefert den Typ der Gui Komponenten zurück.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Typ für GuiText abfragen
	 * <br><i>Der Typ muß GUI_COMPONENT sein.</i>
	 */
	public void testGetGuiType() {
		
		assertEquals(GuiNumber.GUI_COMPONENT,this.myNumber.getGuiType());

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
	 * <br>Ausführen der Methode reset()
	 * <br><i>Modified-Kennzeichen steht auf false<br>Der Value des GuiText ist ein leerer String.</i>
	 */
	public void testReset() {
		/*
		 * Achtung: Reihenfolge ist wichtig !!!
		 * Erst den neuen Value und dann das modified-Kennzeichen auf 
		 * true setzen, da setValue() das modified-Kennzeichen auf false 
		 * einstellt.
		 */
		this.myNumber.setValue("125");
		this.myNumber.setModified(true);
		
		assertEquals("(1)", true, this.myNumber.isModified());
		assertEquals("(2)", "125", this.myNumber.getValue());
		
		this.myNumber.reset();
		
		assertEquals("(3)", false, this.myNumber.isModified());
		assertEquals("(4)", "", this.myNumber.getValue());
		
	}
	/**
	 * Die Methode getTag() liefert den String "Number" zurück.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getTag() ausführen
	 * <br><i>Rückgabewert muß "Number" sein</i>
	 */
	public void testGetTag() {
		
		assertEquals("Number",this.myNumber.getTag() );
		
	}
	/**
	 * Die Methode getFormat() liefert das Format des GuiNumber zurück.
	 * <br>Die Methode setFormat() setzt das Format der Komponente.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getFormat() ausführen, um das Standard-Format zu prüfen.
	 * <br><i>Rückgabewert muß java.text.DecimalFormat sein</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Format umsetzen und prüfen ob es korrekt gesetzt wurde.
	 * <br><i>Rückgabewert muß "#,###.00" sein</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Format umsetzen - falls im vorgehenden Test das Standardformat erwischt wurde - und prüfen ob es korrekt gesetzt wurde.
	 * <br><i>Rückgabewert muß "#.00" sein</i>
	 * <br>
	 * <br><b>Hinweis</b>
	 * <br>Ein Format-Pattern muß eine bestimmten Notation entsprechen.
	 * <br>Bei numerischen Pattern ist darauf zu achten, daß links von
	 * einem Trennzeichen (Komma oder Punkt) nur ein Formatzeichen 
	 * (Null oder Raute) stehen darf. Das Pattern wird intern entsprechend
	 * umgewandelt.
	 * <br>Bsp.: aus ###,###.00 wird #,###.00
	 */
	public void testGetSetFormat() {
	  
	  DecimalFormat myFmt = (DecimalFormat) this.myNumber.getFormat();
	  assertEquals("(1) ist Format:" + myFmt.toPattern(),"#,##0.###",myFmt.toPattern());
	  
	  this.myNumber.setFormat("##,###.000");
	  myFmt = (DecimalFormat) this.myNumber.getFormat();
	  assertEquals("(2) ist Format:" + myFmt.toPattern(),"##,###.000",myFmt.toPattern());
	  	
	  this.myNumber.setFormat("#.00");
	  myFmt = (DecimalFormat) this.myNumber.getFormat();
	  assertEquals("(3) ist Format:" + myFmt.toPattern(),"#.00",myFmt.toPattern());
        
	}
	/**
	 * Die Methode getValueClass() liefert die Number-Klasse der
	 * Komponente zurück
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getValueClass() ausführen
	 * <br><i>Rückgabewert muß eine Number-Class sein</i>
	 */
	public void testGetValueClass() {
	  
	  assertEquals("class java.lang.Number",this.myNumber.getValueClass().toString());
	  
	}
	/**
	 * Die Methode getValueInt() liefert den Inhalt der Komponente
	 * als Integer.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Wert auf 1 setzen und prüfen.
	 * <br><i>Rückgabewert muß 1 sein</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Wert auf 10.35 setzen und prüfen.
	 * <br><i>Rückgabewert muß 10 sein</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Wert auf "Hallo" setzen und prüfen.
	 * <br><i>Rückgabewert muß null sein</i>
	 *
	 */
	public void testGetValueInt() {

		this.myNumber.setValue("1");
		assertEquals("(1)",1,this.myNumber.getValueInt());

		this.myNumber.setValue("10,35");
		assertEquals("(2)",10,this.myNumber.getValueInt());

		this.myNumber.setValue("Hallo");
		assertEquals("(3)",0,this.myNumber.getValueInt());
	}
	/**
	 * Die Methode getValueLong() liefert den Inhalt der Komponente
	 * als Long.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Wert auf 1 setzen und prüfen.
	 * <br><i>Rückgabewert muß 1 sein</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Wert auf 10.35 setzen und prüfen.
	 * <br><i>Rückgabewert muß 10 sein</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Wert auf "Hallo" setzen und prüfen.
	 * <br><i>Rückgabewert muß null sein</i>
	 *
	 */
	public void testGetValueLong() {

		this.myNumber.setValue("1");
		assertEquals("(1)",1,this.myNumber.getValueLong());

		this.myNumber.setValue("10,35");
		assertEquals("(2)",10,this.myNumber.getValueLong());

		this.myNumber.setValue("Hallo");
		assertEquals("(3)",0,this.myNumber.getValueLong());
	}
	/**
	 * Die Methode getValueDecimal() liefert den Inhalt der Komponente
	 * als BigDecimal.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Wert auf 1 setzen und prüfen.
	 * <br><i>Rückgabewert muß 1 sein</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Wert auf 10.35 setzen und prüfen.
	 * <br><i>Rückgabewert muß 10.35 sein</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Wert auf "Hallo" setzen und prüfen.
	 * <br><i>Rückgabewert muß null sein</i>
	 *
	 */
	public void testGetValueDecimal() {
	
		BigDecimal big0 = new BigDecimal(0);
		BigDecimal big1 = new BigDecimal(1);
		BigDecimal big10 = new BigDecimal("10.35");

		this.myNumber.setValue("1");
		assertEquals("(1)",0,big1.compareTo(this.myNumber.getValueDecimal()));
	
		this.myNumber.setValue("10,35");
		assertEquals("(2)",0,big10.compareTo(this.myNumber.getValueDecimal()));

		this.myNumber.setValue("Hallo");
		assertEquals("(3)",0,big0.compareTo(this.myNumber.getValueDecimal()));
	}

	/**
	 * Die Methode verify() führ den InputVerifier der GuiComponent aus.
	 * <br>Im Fehlerfall wird eine IllegalStateException geworfen. 
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Der Componenten eine Wert zuweisen und verify ausführen.
	 * <br><i>Es ist kein Verifier gesetzt, es darf keine Exception geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Max. Eingabelänge auf 5 setzen und Wert "123456" zuweisen
	 * <br><i>Es muß eine Exception geworfen werden</i>
	 */
	public void testVerify() {

		this.myNumber.setValue("12345");
		try{
			this.myNumber.verify();
		}
		catch( IllegalStateException e) {
			fail("FEHLER: IllegalStateException wurde geworfen.");
		}
		
		this.myNumber.setMaxlen(5);
		this.myNumber.setValue("123456");
		try{
			this.myNumber.verify();
			fail("FEHLER: IllegalStateException wurde nicht geworfen.");
		}
		catch( IllegalStateException e) {
			
		}
	}
}
