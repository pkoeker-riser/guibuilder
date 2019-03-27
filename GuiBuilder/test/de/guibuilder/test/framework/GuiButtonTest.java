/*
 * Created on 01.08.2003
 */
package de.guibuilder.test.framework;

import javax.swing.JButton;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiElement;
import de.guibuilder.framework.GuiAction;
import de.guibuilder.framework.GuiButton;
import de.guibuilder.framework.GuiTable;

/**
 * @author kknobloch
 */
public class GuiButtonTest extends GuiActionTest {

	GuiButton myButton = new GuiButton();

	public GuiAction getTestGuiAction() {
		return this.myButton;
	} 
	public GuiElement getTestGuiElement() {
		return this.myButton;
	} 
	public GuiMember getTestGuiMember() {
		return this.myButton;
	} 

	public boolean validateGuiMemberValue(String otyp, Object o) {
		return false;
	}

	/**
	 * Der Konstruktor GuiButton(String) setzt den Label und den Namen
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
		
		GuiButton t1 = new GuiButton("MeinLabel");
		assertEquals("(1)", "MeinLabel", t1.getLabel());
		assertEquals("(2)", "meinLabel", t1.getName());

		GuiButton t2 = new GuiButton("MeinFeld:");
		assertEquals("(3)", "MeinFeld:", t2.getLabel());
		assertEquals("(4)", "meinFeld", t2.getName());
		
	}
	/**
	 * Die Methode getJComponent() liefert die Java-Komponente zur 
	 * GuiComponent. Im Falle von GuiButton muß dies JButton sein.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>JComponent abfragen
	 * <br><i>Muß class javax.swing.JButton sein</i>
	 *
	 */
	public void testGetJComponent() {
		assertEquals("class javax.swing.JButton",this.myButton.getJComponent().getClass().toString());
	}
	/**
	 * Die Methode reset() ist bei GuiButton eine leere Methode.
	 * Hier kann also nichts getestet werden.
	 */
	public void testReset() {
	}

	/**
	 * Die Methode getTag() liefert den String "Button" zurück.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getTag() ausführen
	 * <br><i>Rückgabewert muß "Button" sein</i>
	 */
	public void testGetTag() {
		assertEquals("Button",this.myButton.getTag());
	}
	/**
	 * Die Methode setActionCommand() von GuiButton setzt, wenn als 
	 * Command "OK" übergeben wird zusätzlich das DefaultCapable des Buttons.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>ActionCommand <> "OK" an GuiButton setzen.
	 * <br><i>Das DefaultCapable-Kennzeichen des JButton ist nicht gesetzt.</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>ActionCommand == "OK" an GuiButton setzen.
	 * <br><i>Das DefaultCapable-Kennzeichen des JButton ist gesetzt.</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>ActionCommand <> "OK" an GuiButton setzen.
	 * <br><i>Das DefaultCapable-Kennzeichen des JButton bleibt gesetzt.</i>
	 */
	public void testSetActionCommandOK() {
		
		JButton jB = (JButton) this.myButton.getJComponent();
		jB.setDefaultCapable(false);
		
		this.myButton.setActionCommand("XYZ");
		assertEquals("(1)", false,jB.isDefaultCapable());
		
		this.myButton.setActionCommand("ok");
		assertEquals("(2)", true,jB.isDefaultCapable());
		
		this.myButton.setActionCommand("quit");
		assertEquals("(3)", true,jB.isDefaultCapable());
		
	}
	/**
	 * Die Methode getButton() liefert die JComponent des GuiButton.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Vergleich von getJComponent() und getButton()
	 * <br><i>Beide Methoden liefern das gleiche Objekt</i>
	 */
	public void testGetButton() {
		assertEquals(this.myButton.getJComponent(),this.myButton.getButton());
	}
	/**
	 * Die Methode setTable() setzt das GuiTable-Objekt zum Button.
	 * <br>Die Methode getTable() liefert das GuiTable-Objekt des Buttons.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Button mit neuem GuiTable-Objekt versorgen
	 * <br><i>Die Methode getTable() gibt das korrekte Objekt zurück</i>
	 */
	public void testGetSetTable() {
		GuiTable myTbl = new GuiTable();
		
		this.myButton.setTable(myTbl);
		assertEquals(myTbl,this.myButton.getTable());
		
	}
	/*
	 * Die Methode setTable() wird im vorhergehenden Testfall
	 * mit geprüft.
	 *
	public void testSetTable() {
	}
	*/
}
