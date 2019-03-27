/*
 * Created on 01.08.2003
 *
 */
package de.guibuilder.test.framework;

import java.awt.Dimension;
import javax.swing.JTextArea;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiElement;
import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiMemo;

/**
 * @author kknobloch
 *
 */
public class GuiMemoTest extends GuiComponentTest {

	GuiMemo myMemo = new GuiMemo();

	public GuiComponent getTestGuiComponent() {
		return this.myMemo;
	}
	public GuiElement getTestGuiElement() {
		return this.myMemo;
	} 
	public GuiMember getTestGuiMember() {
		return this.myMemo;
	} 

	public boolean validateGuiMemberValue(String otyp, Object o) {
		return false;
	}


	/**
	 * Der Konstruktor GuiMemo(String) setzt den Label und den Namen
	 * der GuiComponent. Endet der String mit einem Doppelpunkt, wird
	 * dieser nicht in den Namen übernommen. Der erste Buchstabe des
	 * Namens ist immer klein.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Neuen GuiMemo(String) anlegen und Label prüfen
	 * <br><i>Der Label muß "MeinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Namen des GuiMemo prüfen
	 * <br><i>Der Name muß "meinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Neuen GuiMemo(String) anlegen und Label prüfen
	 * <br><i>Der Label muß "MeinFeld:" sein</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Namen des GuiMemo prüfen
	 * <br><i>Der Name muß "meinFeld" sein</i>
	 */
	public void testConstructorString() {
		/**
		 * TODO: warum hat GuiMemo keinen STRING Konstruktor ?
		 */
		/*
		GuiMemo t1 = new GuiMemo("MeinLabel");
		assertEquals("(1)", "MeinLabel", t1.getLabel());
		assertEquals("(2)", "meinLabel", t1.getName());

		GuiMemo t2 = new GuiMemo("MeinFeld:");
		assertEquals("(3)", "MeinFeld:", t2.getLabel());
		assertEquals("(4)", "meinFeld", t2.getName());
		*/
	}
	/**
	 * Die Methode getJComponent() liefert die Java-Komponente zur 
	 * GuiComponent. Im Falle von GuiMemo muß dies JTextArea sein.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>JComponent abfragen
	 * <br><i>Muß class javax.swing.JTextArea sein</i>
	 *
	 */
	public void testGetJComponent() {
		assertEquals("class javax.swing.JTextArea",this.myMemo.getJComponent().getClass().toString());
	}
	/**
	 * Die Methode reset() setzt den Value des GuiMemo auf null und das 
	 * Modified-Kennzeichen auf false
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Modified-Kennzeichen auf true setzen und dem GuiMemo einen Wert zuweise.
	 * <br><i>Modified-Kennzeichen steht auf true
	 * <br>Der zugewiesene Wert ist gesetzt.</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Ausführen der Methode reset()
	 * <br><i>Modified-Kennzeichen steht auf false
	 * <br>Der Value des GuiMemo ist ein leerer String.</i>
	 */
	public void testReset() {
		/*
		 * Achtung: Reihenfolge ist wichtig !!!
		 * Erst den neuen Value und dann das modified-Kennzeichen auf 
		 * true setzen, da setValue() das modified-Kennzeichen auf false 
		 * einstellt.
		 */
		this.myMemo.setValue("Dies ist der neue Wert");
		this.myMemo.setModified(true);
		
		assertEquals("(1)", true, this.myMemo.isModified());
		assertEquals("(2)", "Dies ist der neue Wert", this.myMemo.getValue());
		
		this.myMemo.reset();
		
		assertEquals("(3)", false, this.myMemo.isModified());
		assertEquals("(4)", "", this.myMemo.getValue());
	}
	/**
	 * TODO: wie kann dies getestet werden ?
	 *
	 */
	public void testD_click() {
		//##fail("Wie kann dies getestet werden?");
	}

	/**
	 * Die Methode getTag() liefert den String "Memo" zurück.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getTag() ausführen
	 * <br><i>Rückgabewert muß "Memo" sein</i>
	 */
	public void testGetTag() {
		
		assertEquals("Memo",this.myMemo.getTag() );
		
	}

	/**
	 * Die Methode getDataType() liefert den Datentyp einer Komponenten zurück. 
	 * <br>Bei einem GuiMemo muß dies STRING sein.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Datentyp der Komponente abfragen
	 * <br><i>Der Datentyp muß MULTILINE sein</i>
	 */
	public void testGetDataType() {
		
		assertEquals(GuiComponent.MULTILINE,this.myMemo.getDataType());
				
	}

	/**
	 * Die Methode getGuiType() liefert den Typ der Gui Komponenten zurück.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Typ für GuiMemo abfragen
	 * <br><i>Der Typ muß GUI_COMPONENT sein.</i>
	 */
	public void testGetGuiType() {
		
		assertEquals(GuiComponent.GUI_COMPONENT, this.myMemo.getGuiType());
		
	}
	/**
	 * Die Methode setEnabled() von GuiMemo setzt neben dem Enabled-Kennzeichen
	 * der JComponent (wird in GuiMemberTest geprüft) auch das Editable-Kennzeichen.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Defaulteinstellungen von GuiMemo prüfen
	 * <br><i>+ GuiMemo muß enabled sein
	 * <br>+ JComponent muß editable sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>GuiMemo auf disabled setzen
	 * <br><i>JComponent darf nicht editable sein</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>GuiMemo auf enabled setzen
	 * <br><i>JComponent muß editable sein</i>
	 */
	public void testSetEnabled() {
		
		JTextArea jMemo = (JTextArea) this.myMemo.getJComponent();
		
		assertEquals("(1)", true, this.myMemo.isEnabled());
		assertEquals("(2)", true, jMemo.isEditable());
		
		this.myMemo.setEnabled(false);
		assertEquals("(3)", false, jMemo.isEditable());

		this.myMemo.setEnabled(true);
		assertEquals("(4)", true, jMemo.isEditable());
	}
	/**
	 * TODO: wie kann dies getestet werden ?
	 *
	 */
	public void testLostFocus() {
		//##fail("wie kann dies getestet werden ?");
	}
	/*
	public void testSetValue() {
	}
	*/
	/**
	 * Die Methode getValue() liefert den Inhalt der Komponente zurück. 
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Inhalt auslesen
	 * <br><i>Initial ist der Inhalt leer</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Inhalt zuweisen und auslesen
	 * <br><i>Es wird der korrekte Inhalt zurückgegeben</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Einen leeren String als Inhalt zuweisen
	 * <br><i>Es wird der korrekte Inhalt zurückgegeben</i>
	 * <br>
	 * <br><u>Testschritt 4</u>
	 * <br>Null als Inhalt zuweisen
	 * <br><i>Es wird der korrekte Inhalt zurückgegeben</i>
	 */
	public void testGetValue() {
		
		assertEquals("(1)","", this.myMemo.getValue());
		
		this.myMemo.setValue("myComp new Value");
		assertEquals("(2)","myComp new Value", this.myMemo.getValue());
		
		this.myMemo.setValue("");
		assertEquals("(3)","", this.myMemo.getValue());
		
		this.myMemo.setValue(null);
		assertEquals("(4)","", this.myMemo.getValue());
	}
	/**
	 * Die Methode getText() liefert den Inhalt von GuiMemo zurück.
	 * <br>Die Methode setText() setzt den Inhalt von GuiMemo und 
	 * setzt die CaretPostion der JComponent auf 0.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Dem GuiMemo einen Wertzuweisen und prüfen
	 * <br><i>+ Der korrekte Wert wird von getText() zurückgegeben
	 * <br>+ Die CaretPosition der JComponent ist 0</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>CaretPos. der JComponent setzen und dem GuiMemo einen Wertzuweisen und prüfen
	 * <br><i>+ Der korrekte Wert wird von getText() zurückgegeben
	 * <br>+ Die CaretPosition der JComponent ist 0</i>
	 */
	public void testGetSetText() {
		
		JTextArea jMemo = (JTextArea) this.myMemo.getJComponent();
		
		this.myMemo.setText("Dies ist ein Test");
		assertEquals("(1)", "Dies ist ein Test",this.myMemo.getText());
		assertEquals("(2)", 0, jMemo.getCaretPosition());
	
		jMemo.setCaretPosition(4);
		this.myMemo.setText("Dies ist noch ein Test");
		assertEquals("(2)", "Dies ist noch ein Test",this.myMemo.getText());
		assertEquals("(3)", 0, jMemo.getCaretPosition());
	
	}
	/*
	 * Die Methode setText wird im vorhergehenden Testfall
	 * mit geprüft.
	 *
	public void testSetText() {
	}
	*/
	/**
	 * Die Methode setLine(int) setzt den Cursor an der angegebenen Zeile.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>GuiMemo mit einem mehrzeiligen Text versorgen
	 * <br><i>Die CaretPosition der JComponent ist 0</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>setLine() für Zeile 2 ausführen
	 * <br><i>Die CaretPosition der JComponent ist 15</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>setLine() für Zeile 1 ausführen
	 * <br><i>Die CaretPosition der JComponent ist 7</i>
	 * <br>
	 * <br><u>Testschritt 4</u>
	 * <br>setLine() für Zeile 3 ausführen
	 * <br><i>Die CaretPosition der JComponent ist 23</i>
	 */
	public void testSetLine() {
		JTextArea jMemo = (JTextArea) this.myMemo.getJComponent();
		
		this.myMemo.setText("Zeile 1\nZeile 2\nZeile 3\n Zeile 4");
		assertEquals("(1)", 0, jMemo.getCaretPosition());
		
		this.myMemo.setLine(2);
		assertEquals("(2)", 15, jMemo.getCaretPosition());
		
		this.myMemo.setLine(1);
		assertEquals("(3)", 7, jMemo.getCaretPosition());
		
		this.myMemo.setLine(3);
		assertEquals("(4)", 23, jMemo.getCaretPosition());
	}
	/**
	 * Die Methode getMemo() liefert die JComponent von GuiMemo zurück.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Prüfen ob getMemo() und getJComponent() das gleiche Objekt liefern.
	 *
	 */
	public void testGetMemo() {
		
		assertEquals(this.myMemo.getJComponent(), this.myMemo.getMemo());
		
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

		if( this.myMemo.getJComponent().getInputVerifier() != null ) {
			this.myMemo.setMaxlen(5);
		}
		this.myMemo.setValue("12345");
		try{
			this.myMemo.verify();
			}
		catch( IllegalStateException e) {
			fail("FEHLER: IllegalStateException wurde geworfen.");
		}
		this.myMemo.setValue("123456");
		try{
			this.myMemo.verify();
			fail("FEHLER: IllegalStateException wurde nicht geworfen.");
		}
		catch( IllegalStateException e) {
			
		}
	}
	/**
	 * Die Methode testSetGetPreferredSize() aus GuiMemberTest wird hier überschrieben,
	 * da in GuiMemberTest mit einem JTextField gearbeitet wird und im Falle von
	 * GuiMemo eine JTextArea als JComponent verwendet wird.
	 * 
	 */
	public void testSetGetPreferredSize() {
		Dimension d = new Dimension();
		Dimension aktDim;
		Dimension newDim;
		JTextArea jtext;
		int aktCol;
		/*
		 * Für den Fall, daß die Columns-Anzahl eines 
		 * JTextField gesetzt ist, wird beim Abfragen
		 * der prefferd size eine Berechnung durchgeführt. 
		 * Dies soll hier vermieden werden und darum wird
		 * die Anzahl Columns auf 0 gesetzt.
		 */
		jtext = (JTextArea) this.myMemo.getJComponent();
		aktCol = jtext.getColumns();
		jtext.setColumns(0); 
		/*
		 * aktuelle preferred Size besorgen
		 */		
		aktDim = this.myMemo.getPreferredSize();
		/*
		 * neue preferred Size berechnen
		 */
		d.height = aktDim.height + 55;
		d.width = aktDim.width + 555;
		/*
		 * neue preferred Size setzen
		 */
		this.myMemo.setPreferredSize(d);
		/*
		 * prüfen, ob die Size korrekt gesetzt wurde
		 */
		newDim = this.myMemo.getPreferredSize();

		assertEquals("(1)",aktDim.height  + 55,newDim.height);
		assertEquals("(2)",aktDim.width + 555,newDim.width);
		
		jtext.setColumns(aktCol);
	}
	
}
