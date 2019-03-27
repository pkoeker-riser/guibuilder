/*
 * Created on 14.07.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.guibuilder.test.framework;
import de.guibuilder.framework.GuiElement;

/**
 * @author kknobloch
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class GuiElementTest extends GuiMemberTest {

	private GuiElement myElem;

	public abstract GuiElement getTestGuiElement(); 
	
	public void testElemInit() {
		/*
		 * zu testende Klasse holen
		 */
		this.myElem = this.getTestGuiElement();
		if( this.myElem == null )
			fail("GuiElement ist null");		
	}
	/**
	 * Die Methode requestFocus() kann nicht mit einem Unit-Test 
	 * geprüft werden.
	 */
	public void testrequestFocus() {
		assertEquals(true,true);
	}
	/**
	 * Die Methode isEnabled() gibt das enabled-Flag des GuiElement zurück.
	 * <br>Die Methode setEnabled() setzt das enabled-Flag am GuiElement.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Prüfen der Default-Einstellung des enabled-Flag
	 * <br><i>Das Flag muß auf true stehen</i>
	 * <br>
 	 * <br><u>Schritt 2</u>
	 * <br>enabled-Flag auf false einstellen und prüfen
	 * <br><i>Das Flag muß auf false stehen</i>
	 * <br>
 	 * <br><u>Schritt 3</u>
	 * <br>enabled-Flag auf true einstellen und prüfen
	 * <br><i>Das Flag muß auf true stehen</i>
	 */
	public void testIsSetEnabled() {
		/*
		 * zu testende Klasse holen
		 */
		this.myElem = this.getTestGuiElement();
		/*
		 * Default für ein Element ist enabled == true
		 */
		assertEquals("(1)",true,this.myElem.isEnabled());

		this.myElem.setEnabled(false);
		assertEquals("(2)",false,this.myElem.isEnabled());
				
		this.myElem.setEnabled(true);
		assertEquals("(3)",true,this.myElem.isEnabled());
	}
	/*
	 * Die Methode setEnabled() wird im vorhergehenden Testfall
	 * mit geprüft.
	 *
	public void testSetEnabled() {
	}
	*/
	/**
	 * Die Methode setHint() setzte den Statuszeilentext eines GuiElement.
	 * <br>Die Methode getHint() liefert den Statuszeilentext eines GuiElement.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Dem GuiElement einen neuen Statuszeilentext zuweisen
	 * <br><i>Der Statuszeilentext wurde korrket gesetzt</i>
	 * <br>
 	 * <br><u>Schritt 2</u>
	 * <br>Dem GuiElement einen leeren String als Statuszeilentext zweisen
	 * <br><i>Der Statuszeilentext ist leer</i>
	 */
	public void testGetSetHint() {
		/*
		 * zu testende Klasse holen
		 */
		this.myElem = this.getTestGuiElement();

		this.myElem.setHint("myElement Statuszeilentext");
		assertEquals("(1)","myElement Statuszeilentext",this.myElem.getHint());		

		this.myElem.setHint("");
		assertEquals("(2)","",this.myElem.getHint());		
	}
	/*
	 * Die Methode setHint() wird im vorhergehende Testfall mit geprüft.
	 *
	public void testSetHint() {
	}
	*/
	/**
	 * Die Methode getFilename() kann hier nicht getestet werden.
	 */
	public void testGetFileName() {
		assertEquals(true,true);		
	}
	/**
	 * Die Methode setFilename() kann hier nicht getestet werden.
	 */
	public void testSetFileName() {
		assertEquals(true,true);		
	}

}
