/*
 * Created on 01.08.2003
 *
 */
package de.guibuilder.test.framework;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import de.guibuilder.framework.GuiAction;
import de.guibuilder.framework.GuiMember;

/**
 * @author kknobloch
 *
 */
public abstract class  GuiActionTest extends GuiElementTest {

	
	public abstract GuiAction getTestGuiAction();

	/**
	 * Die Methode getGuiType() gibt den Typ der GuiComponent zurück.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Typ der GuiComponent abfragen
	 * <br><i>Der GuiType muß GUI_ACTION sein</i>
	 * 
	 */
	public void testGetGuiType() {
		
		GuiAction myAction = this.getTestGuiAction();
		assertEquals("(1)",GuiMember.GUI_ACTION, myAction.getGuiType());
		
	}
	/**
	 * Die Methode verify() ist in GuiAction als leere Methode implementiert.
	 * Daher kann hier nichts getestet werden.
	 */
	public void testVerify() {
	}
	/**
	 * Die Methode setActionCommand() setzt das ActionCommand des
	 * GuiAction.
	 * <br>Die Methode getActionCommand() liefert das aktuelle ActionCommand
	 * des GuiAction zrück.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>ActionCommand des GuiAction setzten und prüfen.
	 * <br><i>Das ActionCommand wurde korrekt gesetzt</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>ActionCommand des GuiAction auf "" setzten und prüfen.
	 * <br><i>Das ActionCommand ist ""</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>ActionCommand des GuiAction erneut setzten und prüfen.
	 * <br><i>Das ActionCommand wurde korrekt gesetzt</i>
	 */
	public void testGetSetActionCommand() {
		GuiAction myAction = this.getTestGuiAction();
		
		myAction.setActionCommand("Action1");
		assertEquals("(1)", "Action1", myAction.getActionCommand());
		
		myAction.setActionCommand("");
		assertEquals("(1)", "", myAction.getActionCommand());

		myAction.setActionCommand("Action2");
		assertEquals("(1)", "Action2", myAction.getActionCommand());

	}
	/*
	 * Die Methode getActionCommand() wird im vorhergehenden Testfall
	 * mit geprüft.
	 *
	public void testGetActionCommand() {
	}
	*/
	/**
	 * Die Methode setHorizontalAlignmet() setzt die horizontale Ausrichtung 
	 * des GuiAction.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</b>
	 * <br>Defautl Alignment prüfen
	 * <br><i>Alignment muß LEFT, CENTER, RIGHT, LEADING oder TRAILING sein</i>
	 * <br>
	 * <br><u>Testschritt 2</b>
	 * <br>Alignment auf LEFT einstellen und prüfen
	 * <br><i>Alignment muß LEFT sein</i>
	 * <br>
	 * <br><u>Testschritt 3</b>
	 * <br>Alignment auf CENTER einstellen und prüfen
	 * <br><i>Alignment muß CENTER sein</i>
	 * <br>
	 * <br><u>Testschritt 4</b>
	 * <br>Alignment auf RIGHT einstellen und prüfen
	 * <br><i>Alignment muß RIGHT sein</i>
	 * <br>
	 * <br><u>Testschritt 5</b>
	 * <br>Alignment auf LEADING einstellen und prüfen
	 * <br><i>Alignment muß LEADING sein</i>
	 * <br>
	 * <br><u>Testschritt 6</b>
	 * <br>Alignment auf TRAILING einstellen und prüfen
	 * <br><i>Alignment muß TRAILING sein</i>
	 * <br>
	 * <br><u>Testschritt 7</b>
	 * <br>Alignment auf einen undefinierten Wert einstellen
	 * <br><i>Es muß eine IllegalArgumentException geworfen werden</i>
	 */
	public void testSetHorizontalAlignment() {
		
		GuiAction myAction = this.getTestGuiAction();
		AbstractButton jButton = (AbstractButton) myAction.getJComponent();
		
		int aktAlign = jButton.getHorizontalAlignment();
		
		if( aktAlign != SwingConstants.LEFT &&
			aktAlign != SwingConstants.CENTER &&
			aktAlign != SwingConstants.RIGHT &&
			aktAlign != SwingConstants.LEADING &&
			aktAlign != SwingConstants.TRAILING )
			fail("(1) illegal align " + aktAlign);
		
		myAction.setHorizontalAlignment(SwingConstants.LEFT);
		assertEquals("(2)", SwingConstants.LEFT, jButton.getHorizontalAlignment());
		
		myAction.setHorizontalAlignment(SwingConstants.CENTER);
		assertEquals("(3)", SwingConstants.CENTER, jButton.getHorizontalAlignment());

		myAction.setHorizontalAlignment(SwingConstants.RIGHT);
		assertEquals("(4)", SwingConstants.RIGHT, jButton.getHorizontalAlignment());

		myAction.setHorizontalAlignment(SwingConstants.LEADING);
		assertEquals("(5)", SwingConstants.LEADING, jButton.getHorizontalAlignment());

		myAction.setHorizontalAlignment(SwingConstants.TRAILING);
		assertEquals("(6)", SwingConstants.TRAILING, jButton.getHorizontalAlignment());

		try{
			myAction.setHorizontalAlignment(-123);
			fail("(7) IllegalArgumentException wurde nicht geworfen");
		}
		catch(IllegalArgumentException e) {
			
		}
	}
	/**
	 * Die Methode setIcon() setzt das Icon des GuiAction.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</b>
	 * <br>GuiAction mit einem Icon versehen.
	 * <br><i>Das Icon der JComponent muß dem zuvor instanzierem entsprechen</i>
	 */
	public void testSetIcon() {
		
		Icon myIcon = new ImageIcon("TestIcon.gif");
		GuiAction myAction = this.getTestGuiAction();
		AbstractButton jButton = (AbstractButton) myAction.getJComponent();

		myAction.setIcon(myIcon);
		assertEquals(myIcon, jButton.getIcon());		
		
	}
	/**
	 * Die Methode setMnemonic() setzt den HotKey für den GuiAction.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</b>
	 * <br>Mnemonic 'A' am GuiAction setzen.
	 * <br><i>Mnemonic der JComponent ist 'A'</i>
	 * <br>
	 * <br><u>Testschritt 2</b>
	 * <br>Mnemonic 'Z' am GuiAction setzen.
	 * <br><i>Mnemonic der JComponent ist 'Z'</i>
	 * <br>
	 * <br><u>Testschritt 3</b>
	 * <br>Mnemonic ' ' am GuiAction setzen.
	 * <br><i>Mnemonic der JComponent ist ' '</i>
	 */
	public void testSetMnemonic() {

		GuiAction myAction = this.getTestGuiAction();
		AbstractButton jButton = (AbstractButton) myAction.getJComponent();

		myAction.setMnemonic('A');
		assertEquals("(1)",'A',jButton.getMnemonic());

		myAction.setMnemonic('Z');
		assertEquals("(2)",'Z',jButton.getMnemonic());

		myAction.setMnemonic(' ');
		assertEquals("(3)",' ',jButton.getMnemonic());
	}
	/**
	 * Die Methode getText() liefert die aktuelle Beschriftung 
	 * des GuiAction.
	 * <br>Die Methode setText() setzt die aktuelle Beschriftung
	 * des GuiAction.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</b>
	 * <br>GuiAction mit Text "Label1" versehen
	 * <br><i>+ getText() muß "Label1" zurückgeben
	 * <br>+ getLabel() muß "Label1" zurückgeben</i>
	 * <br>
	 * <br><u>Testschritt 2</b>
	 * <br>GuiAction mit Text "Label2" versehen
	 * <br><i>+ getText() muß "Label2" zurückgeben
	 * <br>+ getLabel() muß "Label2" zurückgeben</i>
	 * <br>
	 * <br><u>Testschritt 3</b>
	 * <br>GuiAction null als Text zuweisen
	 * <br><i>+ getText() muß null zurückgeben
	 * <br>+ getLabel() muß null zurückgeben</i>
	 */
	public void testGetSetText() {
		GuiAction myAction = this.getTestGuiAction();
		
		myAction.setText("Label1");
		assertEquals("(1)","Label1",myAction.getText());
		assertEquals("(2)","Label1",myAction.getLabel());
		
		myAction.setText("Label2");
		assertEquals("(3)","Label2",myAction.getText());
		assertEquals("(4)","Label2",myAction.getLabel());

		myAction.setText(null);
		assertEquals("(5)",null,myAction.getText());
		assertEquals("(6)",null,myAction.getLabel());
	}
	/*
	 * Die Methode setText() wird im vorhergehenden Testfall 
	 * mit geprüft.
	 *
	public void testSetText() {
	}
	*/

}
