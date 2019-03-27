/*
 * Created on 29.07.2003
 *
 */
package de.guibuilder.test.framework;
import java.text.DecimalFormat;
import java.math.BigDecimal;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiElement;
import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiMoney;
/**
 * @author kknobloch
 *
 */
public class GuiMoneyTest extends GuiComponentTest {

	GuiMoney myMoney = new GuiMoney();
	
	public GuiComponent getTestGuiComponent() {
		return this.myMoney;
	}
	public GuiElement getTestGuiElement() {
		return this.myMoney;
	} 
	public GuiMember getTestGuiMember() {
		return this.myMoney;
	} 

	public boolean validateGuiMemberValue(String otyp, Object o) {
		return false;
	}
	
	/**
	 * Der Konstruktor GuiMoney(String) setzt den Label und den Namen
	 * der GuiComponent. Endet der String mit einem Doppelpunkt, wird
	 * dieser nicht in den Namen �bernommen. Der erste Buchstabe des
	 * Namens ist immer klein.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Neuen GuiMoney(String) anlegen und Label pr�fen
	 * <br><i>Der Label mu� "MeinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Namen des GuiMoney pr�fen
	 * <br><i>Der Name mu� "meinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Neuen GuiMoney(String) anlegen und Label pr�fen
	 * <br><i>Der Label mu� "MeinFeld:" sein</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Namen des GuiMoney pr�fen
	 * <br><i>Der Name mu� "meinFeld" sein</i>
	 */
	public void testConstructorString() {
		
		GuiMoney t1 = new GuiMoney("MeinLabel");
		assertEquals("(1)", "MeinLabel", t1.getLabel());
		assertEquals("(2)", "meinLabel", t1.getName());

		GuiMoney t2 = new GuiMoney("MeinFeld:");
		assertEquals("(3)", "MeinFeld:", t2.getLabel());
		assertEquals("(4)", "meinFeld", t2.getName());
	}
	/**
	 * Die Methode getTag() liefert den String "Money" zur�ck.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getTag() ausf�hren
	 * <br><i>R�ckgabewert mu� "Money" sein</i>
	 */
	public void testGetTag() {
		assertEquals("Money",this.myMoney.getTag());
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
		assertEquals(GuiComponent.GUI_COMPONENT,this.myMoney.getGuiType());
	}
	/**
	 * Die Methode getDataType() liefert den Datentyp einer Komponenten zur�ck. 
	 * <br>Bei einem GuiMoney mu� dies NUMBER sein.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Datentyp der Komponente abfragen
	 * <br><i>Der Datentyp mu� NUMBER sein</i>
	 */
	public void testGetDataType() {
		assertEquals(GuiMoney.NUMBER,this.myMoney.getDataType());;
	}
	/**
	 * Die Methode getValueClass() liefert die Number-Klasse der
	 * Komponente zur�ck
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getValueClass() ausf�hren
	 * <br><i>R�ckgabewert mu� eine Number-Class sein</i>
	 */
	public void testGetValueClass() {
		assertEquals("class java.lang.Number",this.myMoney.getValueClass().toString());
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
	 * <br>
	 * <br><u>Schritt 4</u>
	 * <br>Null als Inhalt zuweisen
	 * <br><i>Es wird der korrekte Inhalt zur�ckgegeben</i>
	 */
	public void testGetValue() {
		
		assertEquals("(1)","", this.myMoney.getValue());
		
		this.myMoney.setValue("myComp new Value");
		assertNull("(2)", this.myMoney.getValue());
		
		this.myMoney.setValue("");
		assertNull("(3)", this.myMoney.getValue());
		
		this.myMoney.setValue((String)null);
		assertNull("(4)", this.myMoney.getValue());
	}

	/**
	 * Die Methode getFormat() liefert das Format des GuiMoney zur�ck.
	 * <br>Die Methode setFormat() setzt das Format der Komponente.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getFormat() ausf�hren, um das Standard-Format zu pr�fen.
	 * <br><i>R�ckgabewert mu� "#,###.00" sein</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Format umsetzen und pr�fen ob es korrekt gesetzt wurde.
	 * <br><i>R�ckgabewert mu� "#,###.000" sein</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Format umsetzen - falls im vorgehenden Test das Standardformat erwischt wurde - und pr�fen ob es korrekt gesetzt wurde.
	 * <br><i>R�ckgabewert mu� "#.00" sein</i>
	 * <br>
	 * <br><b>Hinweis</b>
	 * <br>Ein Format-Pattern mu� eine bestimmten Notation entsprechen.
	 * <br>Bei numerischen Pattern ist darauf zu achten, da� links von
	 * einem Trennzeichen (Komma oder Punkt) nur ein Formatzeichen 
	 * (Null oder Raute) stehen darf. Das Pattern wird intern entsprechend
	 * umgewandelt.
	 * <br>Bsp.: aus ###,###.00 wird #,###.00
	 */
	public void testGetSetFormat() {
	  
		DecimalFormat myFmt = (DecimalFormat) this.myMoney.getFormat();
		assertEquals("(1) ist Format:" + myFmt.toPattern(),"#,##0.00",myFmt.toPattern());
	  
		this.myMoney.setFormat("#,###.000");
		myFmt = (DecimalFormat) this.myMoney.getFormat();
		assertEquals("(2) ist Format:" + myFmt.toPattern(),"#,###.000",myFmt.toPattern());
	  	
		this.myMoney.setFormat("#.00");
		myFmt = (DecimalFormat) this.myMoney.getFormat();
		assertEquals("(3) ist Format:" + myFmt.toPattern(),"#.00",myFmt.toPattern());
	  
	}
	/*
	 * Die Methode setFormat() wird im vorhergehenden Testfall
	 * mit gepr�ft.
	 *
	public void testSetFormat() {
		assertEquals("nicht implementiert",true,false);
	}
	*/
	/**
	 * �ber die Methode setValue(double) kann dem GuiMoney ein double-Wert
	 * zugewiesen werden. Druch das Standardformat wird kaufm�nnisch gerundet.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Wert 12.5 zuweisen
	 * <br><i>Text der Komponente mu� "12,50" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Wert 10.555 zuweisen
	 * <br><i>Text der Komponente mu� "10,56" sein</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Wert 4.554 zuweisen
	 * <br><i>Text der Komponente mu� "4,55" sein</i>
	 */
	public void testSetValuedouble() {
		double dbVal = 12.5;		
		
		this.myMoney.setValue(dbVal);
		assertEquals("(1)","12,50",this.myMoney.getText());

		dbVal = 10.555;
		this.myMoney.setValue(dbVal);
		assertEquals("(2)","10,56",this.myMoney.getText());
				
		dbVal = 4.554;
		this.myMoney.setValue(dbVal);
		assertEquals("(3)","4,55",this.myMoney.getText());
	}
	/**
	 * �ber die Methode getValueInt() kann der aktuelle Wert des GuiMoney
	 * als Ganzzahl ausgelesen werden.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Der Komponente den Wert 12480 zuweisen. Dieser wird wg. des
	 * Formates als 12.480,00 dargestellt
	 * <br><i>getValueInt() mu� 12480 zur�ckliefern</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Der Komponente den Wert 0 zuweisen.
	 * <br><i>getValueInt() mu� 0 zur�ckliefern</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Der Komponente den Wert 12.5 zuweisen. Dieser wird wg. des
	 * Formates als 12,50 dargestellt
	 * <br><i>getValueInt() mu� 12 zur�ckliefern</i>
	 */
	public void testGetValueInt() {
		
		this.myMoney.setValue(12480);
		assertEquals("(1)",12480,this.myMoney.getValueInt());

		this.myMoney.setValue(0);
		assertEquals("(2)",0,this.myMoney.getValueInt());

		this.myMoney.setValue(12.5);
		assertEquals("(3)",12,this.myMoney.getValueInt());
		
	}
	/**
	 * �ber die Methode getValueLong() kann der aktuelle Wert des GuiMoney
	 * als Ganzzahl ausgelesen werden.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Der Komponente den Wert 12480 zuweisen. Dieser wird wg. des
	 * Formates als 12.480,00 dargestellt
	 * <br><i>getValueLong() mu� 12480 zur�ckliefern</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Der Komponente den Wert 0 zuweisen.
	 * <br><i>getValueLong() mu� 0 zur�ckliefern</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Der Komponente den Wert 12.5 zuweisen. Dieser wird wg. des
	 * Formates als 12,50 dargestellt
	 * <br><i>getValueLong() mu� 12 zur�ckliefern</i>
	 */
	public void testGetValueLong() {
		
		this.myMoney.setValue(12480);
		assertEquals("(1)",12480,this.myMoney.getValueLong());

		this.myMoney.setValue(0);
		assertEquals("(2)",0,this.myMoney.getValueLong());

		this.myMoney.setValue(12.5);
		assertEquals("(3)",12,this.myMoney.getValueLong());
	}
	/**
	 * �ber die Methode getValueDecimal() kann der aktuelle Wert des GuiMoney
	 * als BigDecimal-Objekt ausgelesen werden.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Der Komponente den Wert 1.47 zuweisen. Dieser wird wg. des
	 * Formates als 1,47 dargestellt
	 * <br><i>getValueDecimal() mu� 1.47 zur�ckliefern</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Der Komponente den Wert 2.516 zuweisen.
	 * <br><i>getValueDecimal() mu� 2.52 zur�ckliefern</i>
	 */
	public void testGetValueDecimal() {
		
		this.myMoney.setValue(1.4744);
		BigDecimal dv = this.myMoney.getValueDecimal();
		assertEquals("(1)","1.47",dv.toString());

		this.myMoney.setValue(2.516);
		dv = this.myMoney.getValueDecimal();
		assertEquals("(2)","2.52",dv.toString());
	}
	/**
	 * Die Methode verify() f�hr den InputVerifier der GuiComponent aus.
	 * <br>Im Fehlerfall wird eine IllegalStateException geworfen. 
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 0</u>
	 * <br>Pr�fen, ob an der Komponente ein InputVerifyer vorhanden ist
	 * <br><i>GuiMoney mu� einen InputVerifyer besitzen.</i>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Wert 12.5 zuweisen und verify ausf�hren.
	 * <br><i>Es darf keine Exception geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Wert "12,5" als Text zuweisen und verify ausf�hren.
	 * <br><i>Es darf keine Exception geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Wert "1.000" als Text zuweisen und verify ausf�hren.
	 * <br><i>Es darf keine Exception geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 4</u>
	 * <br>Wert "1.000," als Text zuweisen und verify ausf�hren.
	 * <br><i>Es darf keine Exception geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 5</u>
	 * <br>Wert "Heiner" als Text zuweisen und verify ausf�hren.
	 * <br><i>Es mu� eine Exception geworfen werden</i>
	 */
	public void testVerify() {
		
		assertNotNull("(0)",this.myMoney.getJComponent().getInputVerifier());
		
		this.myMoney.setValue(12.5);
		try{
			this.myMoney.verify();
		}
		catch( IllegalStateException e) {
			fail("(1) FEHLER: IllegalStateException wurde geworfen.");
		}
		this.myMoney.setValue("12,5");
		try{
			this.myMoney.verify();
		}
		catch( IllegalStateException e) {
			fail("(2) FEHLER: IllegalStateException wurde geworfen.");
		}
		this.myMoney.setValue("1.000");
		try{
			this.myMoney.verify();
		}
		catch( IllegalStateException e) {
			fail("(3) FEHLER: IllegalStateException wurde geworfen.");
		}
		this.myMoney.setValue("1.000,");
		try{
			this.myMoney.verify();
		}
		catch( IllegalStateException e) {
			fail("(4) FEHLER: IllegalStateException wurde geworfen.");
		}

		this.myMoney.setValue("Heiner");
		try{
			this.myMoney.verify();
			fail("(5) FEHLER: IllegalStateException wurde nicht geworfen.");
		}
		catch( IllegalStateException e) {
			
		}
		
	}
	/**
	 * Die Methode reset() setzt den Value des GuiMoney auf null und das 
	 * Modified-Kennzeichen auf false
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Modified-Kennzeichen auf true setzen und dem GuiMoney einen Wert zuweise.
	 * <br><i>Modified-Kennzeichen steht auf true<br>Der zugewiesene Wert ist gesetzt.</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Ausf�hren der Methode reset()
	 * <br><i>Modified-Kennzeichen steht auf false<br>Der Value des GuiMoney ist ein leerer String.</i>
	 */
	public void testReset() {
		/*
		 * Achtung: Reihenfolge ist wichtig !!!
		 * Erst den neuen Value und dann das modified-Kennzeichen auf 
		 * true setzen, da setValue() das modified-Kennzeichen auf false 
		 * einstellt.
		 */
		this.myMoney.setValue("125");
		this.myMoney.setModified(true);
		
		assertEquals("(1)", true, this.myMoney.isModified());
		assertEquals("(2)", "125,00", this.myMoney.getValue());
		
		this.myMoney.reset();
		
		assertEquals("(3)", false, this.myMoney.isModified());
		assertEquals("(4)", "", this.myMoney.getValue());
	}
}
