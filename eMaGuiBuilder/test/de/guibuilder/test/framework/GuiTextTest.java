/*
 * Created on 14.07.2003
 *
 */
package de.guibuilder.test.framework;

import java.awt.Color;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiElement;
import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiText;
/**
 * @author kknobloch
 *
 */
public class GuiTextTest extends GuiComponentTest {

	private GuiText myComp = new GuiText();
	
	public GuiMember getTestGuiMember() {
		return this.myComp;
	} 
	public GuiElement getTestGuiElement() {
		return this.myComp;
	} 
	public GuiComponent getTestGuiComponent() {
		return this.myComp;
	} 

	public boolean validateGuiMemberValue(String otyp, Object o) {
		
		if( otyp.equalsIgnoreCase("GuiType") ) {
			
			if(Integer.parseInt((String) o) == GuiComponent.GUI_COMPONENT )
				return true;
			
		}
		
		return false;
	}
	
	public void testInitSubs() {
		this.myMember = this.myComp;		
	}
	/**
	 * Der Konstruktor GuiText(String) setzt den Label und den Namen
	 * der GuiComponent. Endet der String mit einem Doppelpunkt, wird
	 * dieser nicht in den Namen �bernommen. Der erste Buchstabe des
	 * Namens ist immer klein.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Neuen GuiText(String) anlegen und Label pr�fen
	 * <br><i>Der Label mu� "MeinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Namen des GuiText pr�fen
	 * <br><i>Der Name mu� "meinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Neuen GuiText(String) anlegen und Label pr�fen
	 * <br><i>Der Label mu� "MeinFeld:" sein</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Namen des GuiText pr�fen
	 * <br><i>Der Name mu� "meinFeld" sein</i>
	 */
	public void testConstructorString() {
		
		GuiText t1 = new GuiText("MeinLabel");
		assertEquals("(1)", "MeinLabel", t1.getLabel());
		assertEquals("(2)", "meinLabel", t1.getName());

		GuiText t2 = new GuiText("MeinFeld:");
		assertEquals("(3)", "MeinFeld:", t2.getLabel());
		assertEquals("(4)", "meinFeld", t2.getName());
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
		
		assertEquals(GuiComponent.GUI_COMPONENT, this.myComp.getGuiType());
		
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
		this.myComp.setValue("Dies ist der neue Wert");
		this.myComp.setModified(true);
		
		assertEquals("(1)", true, this.myComp.isModified());
		assertEquals("(2)", "Dies ist der neue Wert", this.myComp.getValue());
		
		this.myComp.reset();
		
		assertEquals("(3)", false, this.myComp.isModified());
		assertEquals("(4)", "", this.myComp.getValue());
		
		this.myComp.setValue(null);
		assertEquals("(5)", "", this.myComp.getValue());
		
	}
	/**
	 * Die Methode getDataType() liefert den Datentyp einer Komponenten zur�ck. 
	 * <br>Bei einem GuiText mu� dies STRING sein.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Datentyp der Komponente abfragen
	 * <br><i>Der Datentyp mu� STRING sein</i>
	 */
	public void testGetDataType() {
		
		assertEquals(GuiComponent.STRING,this.myComp.getDataType());
				
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
		
		assertEquals("(1)","", this.myComp.getValue());
		
		this.myComp.setValue("myComp new Value");
		assertEquals("(2)","myComp new Value", this.myComp.getValue());
		
		this.myComp.setValue("");
		assertEquals("(3)","", this.myComp.getValue());
		
		this.myComp.setValue(null);
		assertEquals("(4)","", this.myComp.getValue());
	}
	/**
	 * Die Methode getTag() liefert den String "Text" zur�ck.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Methode getTag() ausf�hren
	 * <br><i>R�ckgabewert mu� "Text" sein</i>
	 */
	public void testGetTag() {
		
		assertEquals("Text",this.myComp.getTag() );
		
	}
	/**
	 * Die Methode getJComponent() liefert die Java-Komponente zum 
	 * GuiText zur�ck. Wenn die Java-Komponente null ist, wird eine 
	 * neue angelegt.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Methode getJComponent() ausf�hren
	 * <br><i>R�ckgabewert darf nicht null sein</i>
	 */
	public void testGetJComponent() {
		
		assertEquals("class javax.swing.JTextField",this.myComp.getJComponent().getClass().toString());
		
	}
	/**
	 * Die Methode getTextField() liefert die Java-Komponente zum 
	 * GuiText zur�ck.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getTextField() ausf�hren
	 * <br><i>R�ckgabewert darf nicht null sein</i>
	 */
	public void testGetTextField() {
		
		assertNotNull(this.myComp.getTextField());
		
	}
	/**
	 * Die Methode isEnabled() gibt das Enabled-Kennzeichen der Komponenten zur�ck.
	 * <br>Die Methode setEnabled() 
	 * <br>+ setzt das Enabled-Kennzeichen der Kompoente um
	 * <br>+ setzt die Hintergrundfarbe auf grau bzw. wei� um
	 * <br>+ setzt das TabStop-Kennzeichen der Komponente um
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Enabled-Kennzeichen auf ture setzen
	 * <br><i>+ Enabled-Kennzeichen mu� gesetzt sein
	 * <br>+ Hintergrundfarbe mu� wei� sein
	 * <br>+ TabStop-Kennzeichen mu� gesetzt sein</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Enabled-Kennzeichen auf false setzen
	 * <br><i>+ Enabled-Kennzeichen darf nicht gesetzt sein
	 * <br>+ Hintergrundfarbe mu� grau sein
	 * <br>+ TabStop-Kennzeichen darf nicht gesetzt sein</i>
	 */
	public void testTextIsSetEnabled() {
		
		Color aktColor;
		
		this.myComp.setEnabled(true);
		
		assertEquals("(1)",true,this.myComp.isEnabled());
		
		aktColor = this.myComp.getBackground();
		if( aktColor.getBlue() != 255 ||
		    aktColor.getGreen() != 255 ||
		    aktColor.getRed() != 255)
			assertEquals("(2)","wei�","nicht wei�");
		
		// assertEquals("(2)",true,this.myComp.hasTabStop());
		
		this.myComp.setEnabled(false);
		
		assertEquals("(3)",false,this.myComp.isEnabled());
		
		aktColor = this.myComp.getBackground();
		if( aktColor.getBlue() != 230 ||
			aktColor.getGreen() != 230 ||
			aktColor.getRed() != 230)
			assertEquals("(4)","grau","nicht grau");
		
		// assertEquals("(2)",true,this.myComp.hasTabStop());
		
	}
	/*
	 * Die Methode setEnabled() wird im vorhergehenden Testfall 
	 * mit gepr�ft.
	 *
	public void testSetEnabled() {
		
	}
	*/
	/**
	 * Die Methode verify() f�hr den InputVerifier der GuiComponent aus.
	 * <br>Im Fehlerfall wird eine IllegalStateException geworfen. 
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Der Componenten eine Wert zuweisen und verify ausf�hren.
	 * <br><i>Es ist kein Verifier gesetzt, es darf keine Exception geworfen werden</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Max. Eingabel�nge auf 5 setzen und Wert "123456" zuweisen
	 * <br><i>Es mu� eine Exception geworfen werden</i>
	 */
	public void testVerify() {

		if (this.myComp.getJComponent().getInputVerifier() != null) {
			this.myComp.setMaxlen(5);
		}
		this.myComp.setValue("12345");
		try {
			this.myComp.verify();
		} catch (IllegalStateException e) {
			fail("FEHLER: IllegalStateException wurde geworfen.");
		}
		this.myComp.setValue("123456");
		try {
			this.myComp.verify();
			fail("FEHLER: IllegalStateException wurde nicht geworfen.");
		} catch (IllegalStateException e) {

		}
	}
}
