/*
 * Created on 02.09.2003
 */
package de.guibuilder.test.framework;

import javax.swing.JLabel;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiElement;
import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiLabel;

import de.guibuilder.test.utils.TestGuiComponents;
/**
 * @author kknobloch
 */
public class GuiLabelTest extends GuiComponentTest {

	private GuiLabel myComp = new GuiLabel("TestLabel");
	
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
		
		return false;
	}

	/**
	 * Die Methode getGuiType() liefert den Typ der Gui Komponenten zurück.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Typ für GuiLabel abfragen
	 * <br><i>Der Typ muß GUI_COMPONENT sein.</i>
	 */
	public void testGetGuiType() {
		assertEquals(GuiComponent.GUI_COMPONENT, this.myComp.getGuiType());
	}
	
	/**
	 * Die Methode verify() führ den InputVerifier der GuiComponent aus.
	 * <br>Ein Label ist statisch, daher ist ein solcher Test hier nicht 
	 * durchführbar.
	 */
	public void testVerify() {
	}

	/**
	 * Der Konstruktor GuiLabel(String) setzt den Label und den Namen
	 * der GuiComponent. Endet der String mit einem Doppelpunkt, wird
	 * dieser nicht in den Namen übernommen. 
	 * <br>Bei einem GuiLabel beginnt der Name immer mit "guiLabel_".
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Neuen GuiLabel(String) anlegen und Label prüfen
	 * <br><i>Der Label muß "MeinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Namen des GuiLabel prüfen
	 * <br><i>Der Name muß "guiLabel_MeinLabel" sein</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Neuen GuiLabel(String) anlegen und Label prüfen
	 * <br><i>Der Label muß "MeinFeld:" sein</i>
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Namen des GuiLabel prüfen
	 * <br><i>Der Name muß "guiLabel_MeinFeld" sein</i>
	 */
	public void testConstructorString() {
		
		GuiLabel t1 = new GuiLabel("MeinLabel");
		assertEquals("(1)", "MeinLabel", t1.getLabel());
		assertEquals("(2)", "guiLabel_meinLabel", t1.getName());

		GuiLabel t2 = new GuiLabel("MeinFeld:");
		assertEquals("(3)", "MeinFeld:", t2.getLabel());
		assertEquals("(4)", "guiLabel_meinFeld", t2.getName());
	}

	/**
	 * Über den Konstruktor GuiLabel(GuiComponent, String) wird
	 * eine GuiLabel mit dem Text <i>String</i> angelegt und der Komponente
	 * <i>GuiComponent</i> als Label zugewiesen.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>GuiLabel() mit jeder möglichen GuiComponent instanzieren und
	 * <br>+ den Text des GuiLabel prüfen
	 * <br>+ den Namen des GuiLabel prüfen
	 * <br>+ den Label der GuiComponent prüfen 
	 * <br><i>Der Text und der Name des Labels sind korrekt.
	 * <br>Der Label der GuiComponent ist korrekt.</i>
	 */
	public void testConstructorGuiComponentString() {

		GuiLabel label;
		TestGuiComponents tc = new TestGuiComponents();
		GuiComponent comp = tc.getFirstComponent();
		
		while( comp != null ) {
			
			label = new GuiLabel(comp, "Testlabel");
			assertEquals("(1 - " + tc.getComponentName() + ")", "Testlabel", label.getLabel());
			assertEquals("(2 - " + tc.getComponentName() + ")", "guiLabel_testlabel", label.getName());
			//assertEquals("(3 - " + tc.getComponentName() + ")", "text", comp.getLabel());

			comp = tc.getNextComponent();
		}
		

	}

	/**
	 * Die Methode getJComponent() liefert die Java-Komponente zum 
	 * GuiLabel zurück. Wenn die Java-Komponente null ist, wird eine 
	 * neue angelegt.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getJComponent() ausführen
	 * <br><i>Rückgabewert darf nicht null sein</i>
	 */
	public void testGetJComponent() {
		
		assertEquals("class javax.swing.JLabel",this.myComp.getJComponent().getClass().toString());
		
	}

	/**
	 * Die Methode reset() setzt den Value des GuiComponent auf null und das 
	 * Modified-Kennzeichen auf false.
	 * Da ein Label statisch ist, macht diese Prüfung hier keinen Sinn.
	 */
	public void testReset() {
	}

	/**
	 * Die Methode getTag() liefert den String "Label" zurück.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Methode getTag() ausführen
	 * <br><i>Rückgabewert muß "Label" sein</i>
	 */
	public void testGetTag() {
		
		assertEquals("Label",this.myComp.getTag() );
		
	}

	/**
	 * Die Methode getDataType() liefert den Datentyp einer Komponenten zurück. 
	 * <br>Bei einem GuiLabel muß dies STRING sein.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Datentyp der Komponente abfragen
	 * <br><i>Der Datentyp muß STRING sein</i>
	 */
	public void testGetDataType() {
		
		assertEquals(GuiComponent.STRING,this.myComp.getDataType());
				
	}

	/**
	 * Die Methode setValue() setzt den Label neu.
	 * <br>Die Methode getValue() liefert den aktuellen Text des Labels.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Dem Label einen Value zuweisen und abfragen
	 * <br><i>Es wird der korrekte Wert zurückgeliefert</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Dem Label einen leeren String als Value zuweisen und abfragen
	 * <br><i>Es wird der korrekte Wert zurückgeliefert</i>
	 * <br>
	 * <br><u>Schritt 3</u>
	 * <br>Dem Label null als Value zuweisen und abfragen
	 * <br><i>Es wird der korrekte Wert zurückgeliefert</i>
	 */
	public void testSetGetValue() {

		this.myComp.setValue("Dies ist ein Label");
		assertEquals("(1)", "Dies ist ein Label", this.myComp.getValue());
		
		this.myComp.setValue("");
		assertEquals("(2)", "", this.myComp.getValue());
		
		try{
			this.myComp.setValue(null);
			assertEquals("(3)", "", this.myComp.getValue());
		}
		catch(Exception e) {
			fail("(3) " + e.getMessage());
		}
	}
	/**
	 * Die Methode getValue() wird im Testfall testSetGetValue() geprüft.
	 */
	public void testGetValue() {
	}
	/**
	 * Die Methode setText() setzt die Beschriftung eines GuiLabel.
	 * <br>Die Methode getText() liefert die aktuelle Beschrifung eines GuiLabel.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Dem Label einen Text zuweisen und abfragen
	 * <br><i>Der Text wurde korrekt gesetzt</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Dem Label einen leeren Text zuweisen und abfragen
	 * <br><i>Der Text wurde korrekt gesetzt</i>
	 */
	public void testSetGetText() {
		
		this.myComp.setText("testSetGetText Label");
		assertEquals("(1)", "testSetGetText Label", this.myComp.getText());
		
		this.myComp.setText("");
		assertEquals("(2)", "", this.myComp.getText());

	}
	/*
	 * Die Methode getText() wird im vorhergehenden Testfall
	 * mit geprüft.
	 *
	public void testGetText() {
		fail("nicht implementiert");
	}
	*/
	/**
	 * Die Methode setHorizontalAlignment() setzt die Textausrichtung
	 * des GuiLabel.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Textausrichtung auf LEFT setzen
	 * <br><i>Die Textausrichtung der Java-Komponente ist LEFT</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Textausrichtung auf RIGHT setzen
	 * <br><i>Die Textausrichtung der Java-Komponente ist RIGHT</i>
	 */
	public void testSetHorizontalAlignment() {
		
		JLabel label = (JLabel) this.myComp.getJComponent();

		this.myComp.setHorizontalAlignment(JLabel.LEFT);
		assertEquals("(1)", JLabel.LEFT, label.getHorizontalAlignment());
		
		this.myComp.setHorizontalAlignment(JLabel.RIGHT);
		assertEquals("(2)", JLabel.RIGHT, label.getHorizontalAlignment());
	}
	/**
	 * Die Methode getHorizontalAlignment() ist nicht public und 
	 * kann deshalb nicht geprüft werden.
	 */
	public void testGetHorizontalAlignment() {
	}
	/**
	 * Die Methode setMnemonic() setzt den Mnemonic für den GuiLabel.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Mnemonic 'C' setzen
	 * <br><i>Das DisplayedMnemonic der Java-Komp. ist 'C'</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Mnemonic 'F' setzen
	 * <br><i>Das DisplayedMnemonic der Java-Komp. ist 'F'</i>
	 */
	public void testSetMnemonic() {
		
		JLabel label = (JLabel) this.myComp.getJComponent();

		this.myComp.setMnemonic('C');
		assertEquals("(1)", 'C', label.getDisplayedMnemonic());
		
		this.myComp.setMnemonic('F');
		assertEquals("(2)", 'F', label.getDisplayedMnemonic());
	}
	/**
	 * Die Methode setIcon() setzt ein Bild am GuiLabel.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Icon am GuiLabel setzen
	 * <br><i>Das Icon der Java-Komp. identisch mit dem zuvor gesetztem</i>
	 */
	public void testSetIcon() {

		JLabel label = (JLabel) this.myComp.getJComponent();

		Icon myIcon = new ImageIcon("TestIcon.gif");
		this.myComp.setIcon(myIcon);
		
		assertEquals(myIcon,label.getIcon());
		
	}

	/**
	 * Die Methode getIcon() ist nicht public und 
	 * kann deshalb nicht geprüft werden.
	 */
	public void testGetIcon() {
	}

	public void testGetValueClass() {
		assertEquals("class java.lang.String",this.myComp.getValueClass().toString());
	}

}
