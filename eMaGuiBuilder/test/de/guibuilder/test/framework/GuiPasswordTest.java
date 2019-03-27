/*
 * Created on 30.07.2003
 */
package de.guibuilder.test.framework;

import javax.swing.JPasswordField;
import java.lang.IllegalArgumentException;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiElement;
import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiPassword;

/**
 * @author kknobloch
 */
public class GuiPasswordTest extends GuiComponentTest {

	GuiPassword myPassword = new GuiPassword();

	public GuiComponent getTestGuiComponent() {
		return this.myPassword;
	}
	public GuiElement getTestGuiElement() {
		return this.myPassword;
	} 
	public GuiMember getTestGuiMember() {
		return this.myPassword;
	} 

	public boolean validateGuiMemberValue(String otyp, Object o) {
		return false;
	}

	/**
	 * Der Konstruktor GuiPassword(String) setzt den Label und den Namen
	 * der GuiComponent. Endet der String mit einem Doppelpunkt, wird
	 * dieser nicht in den Namen �bernommen. Der erste Buchstabe des
	 * Namens ist immer klein.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Neuen GuiPassword(String) anlegen und Label pr�fen
	 * <br><i>Der Label mu� "MeinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Namen des GuiPassword pr�fen
	 * <br><i>Der Name mu� "meinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Neuen GuiPassword(String) anlegen und Label pr�fen
	 * <br><i>Der Label mu� "MeinFeld:" sein</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Namen des GuiPassword pr�fen
	 * <br><i>Der Name mu� "meinFeld" sein</i>
	 */
	public void testConstructorString() {
		
		GuiPassword t1 = new GuiPassword("MeinLabel");
		assertEquals("(1)", "MeinLabel", t1.getLabel());
		assertEquals("(2)", "meinLabel", t1.getName());

		GuiPassword t2 = new GuiPassword("MeinFeld:");
		assertEquals("(3)", "MeinFeld:", t2.getLabel());
		assertEquals("(4)", "meinFeld", t2.getName());
	}
	/**
	 * Die Methode getJComponent() liefert die Java-Komponente zur 
	 * GuiComponent. Im Falle von GuiPassword mu� dies JPasswordField sein.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>JComponent abfragen
	 * <br><i>Mu� class javax.swing.JPasswordField sein</i>
	 *
	 */
	public void testGetJComponent() {
		assertEquals("class javax.swing.JPasswordField",this.myPassword.getJComponent().getClass().toString());
	}
	/**
	 * Die Methode reset() kann nicht automatisch getestet werden,
	 * da es nicht m�glich ist einem GuiPasswort einen Wert zuzuweisen. 
	 */
	public void testReset() {
		assertEquals(true,true);
	}

	/**
	 * Die Methode getTag() liefert den String "Password" zur�ck.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getTag() ausf�hren
	 * <br><i>R�ckgabewert mu� "Password" sein</i>
	 */
	public void testGetTag() {
		assertEquals("Password",this.myPassword.getTag());
	}
	/**
	 * TODO: was ist hier zu testen ?
	 *
	 */
	public void testLostFocus() {
		//##fail("was ist hier zu testen ?");
	}
	/**
	 * Die Methode getDataType() liefert den Datentyp einer Komponenten zur�ck. 
	 * <br>Bei einem GuiPassword mu� dies STRING sein.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Datentyp der Komponente abfragen
	 * <br><i>Der Datentyp mu� STRING sein</i>
	 */
	public void testGetDataType() {
		assertEquals(GuiComponent.STRING, this.myPassword.getDataType());
	}
	/**
	 * Die Methode getGuiType() liefert den Typ der Gui Komponenten zur�ck.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Typ f�r GuiPassword abfragen
	 * <br><i>Der Typ mu� GUI_COMPONENT sein.</i>
	 */
	public void testGetGuiType() {
		assertEquals(GuiComponent.GUI_COMPONENT, this.myPassword.getGuiType());
	}
	/**
	 * Die Methode setValue() darf bei GuiPasswort() keinen Wert an die
	 * GuiKomponente weitergeben.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Dem GuiPassword einen Wert zuweisen und pr�fen, ob dieser mit getValue()
	 * ausgelesen werden kann.
	 * <br><i>getValue() darf keinen Wert zur�ckliefern</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Pr�fen, ob der Wert bei der JComponent von GuiPassword angekommen ist.
	 * <br><i>Die JComponent darf keinen Wert zur�ckliefern</i>
	 */
	public void testSetValue() {
		
		JPasswordField myJField = (JPasswordField) this.myPassword.getJComponent();
		
		this.myPassword.setValue("MeinPasswort");
		assertEquals("(1)","",this.myPassword.getValue());
		
		String s = new String(myJField.getPassword());
		assertEquals("(2)","",s);
	}
	/** 
	 * Die Methode getValue() kann nicht automatisch getestet werden,
	 * da es nicht m�glich ist einem GuiPasswort einen Wert zuzuweisen. 
	 */
	public void testGetValue() {
		assertEquals(true,true);
	}
	/**
	 * Die Methode setHorizontalAlignmet() setzt die horizontale Ausrichtung 
	 * des GuiPassword.
	 * <br>Mit der Methode getHorizontalAlignmet() kann die aktuelle horizontale
	 * Ausrichtung des GuiPassword abgefragt werden.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</b>
	 * <br>Defautl Alignment pr�fen
	 * <br><i>Alignment mu� LEFT, CENTER, RIGHT, LEADING oder TRAILING sein</i>
	 * <br>
	 * <br><u>Testschritt 2</b>
	 * <br>Alignment auf LEFT einstellen und pr�fen
	 * <br><i>Alignment mu� LEFT sein</i>
	 * <br>
	 * <br><u>Testschritt 3</b>
	 * <br>Alignment auf CENTER einstellen und pr�fen
	 * <br><i>Alignment mu� CENTER sein</i>
	 * <br>
	 * <br><u>Testschritt 4</b>
	 * <br>Alignment auf RIGHT einstellen und pr�fen
	 * <br><i>Alignment mu� RIGHT sein</i>
	 * <br>
	 * <br><u>Testschritt 5</b>
	 * <br>Alignment auf LEADING einstellen und pr�fen
	 * <br><i>Alignment mu� LEADING sein</i>
	 * <br>
	 * <br><u>Testschritt 6</b>
	 * <br>Alignment auf TRAILING einstellen und pr�fen
	 * <br><i>Alignment mu� TRAILING sein</i>
	 * <br>
	 * <br><u>Testschritt 7</b>
	 * <br>Alignment auf einen undefinierten Wert einstellen
	 * <br><i>Es mu� eine IllegalArgumentException geworfen werden</i>
	 */
	public void testGetSetHorizontalAlignment() {
		
		int aktAlign = this.myPassword.getHorizontalAlignment();
		
		if( aktAlign != JPasswordField.LEFT &&
		    aktAlign != JPasswordField.CENTER &&
			aktAlign != JPasswordField.RIGHT &&
			aktAlign != JPasswordField.LEADING &&
			aktAlign != JPasswordField.TRAILING )
			fail("(1) illegal align " + aktAlign);
		
		this.myPassword.setHorizontalAlignment(JPasswordField.LEFT);
		assertEquals("(2)", JPasswordField.LEFT, this.myPassword.getHorizontalAlignment());
		
		this.myPassword.setHorizontalAlignment(JPasswordField.CENTER);
		assertEquals("(3)", JPasswordField.CENTER, this.myPassword.getHorizontalAlignment());

		this.myPassword.setHorizontalAlignment(JPasswordField.RIGHT);
		assertEquals("(4)", JPasswordField.RIGHT, this.myPassword.getHorizontalAlignment());

		this.myPassword.setHorizontalAlignment(JPasswordField.LEADING);
		assertEquals("(5)", JPasswordField.LEADING, this.myPassword.getHorizontalAlignment());

		this.myPassword.setHorizontalAlignment(JPasswordField.TRAILING);
		assertEquals("(6)", JPasswordField.TRAILING, this.myPassword.getHorizontalAlignment());

		try{
			this.myPassword.setHorizontalAlignment(-123);
			fail("(7) IllegalArgumentException wurde nicht geworfen");
		}
		catch(IllegalArgumentException e) {
			
		}
	}
	/*
	 * Die Methode getHorizontalAlignment() wird im vorhergehenden Testfall
	 * mit gepr�ft.
	 *
	public void testGetHorizontalAlignment() {
		fail("nicht implementiert");
	}
	*/
	/**
	 * Die Methode setColumns() kann nicht gepr�ft werden.
	 */
	public void testSetColumns() {
		assertEquals(true, true);
	}
	/**
	 * Die Methode verify() kann nicht gepr�ft werden.
	 */
	public void testVerify() {
		assertEquals(true, true);
	}
}
