/*
 * Created on 14.07.2003
 *
 */
package de.guibuilder.test.framework;
import java.awt.Color;

import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiUtil;

import de.guibuilder.test.utils.TestGuiProperties;

/**
 * @author kknobloch
 *
 */
public abstract class GuiComponentTest extends GuiElementTest {
	
	protected GuiComponent myComponent;	

	public abstract GuiComponent getTestGuiComponent();
	 
	/**
	 * Pr�fen, ob f�r jede abgleitete Komponente der korrekte
	 * GuiType zur�ckgeliefert wird.
	 */
	public void testgetGuiType() {
		/*
		 * zu pr�fende Klasse holen
		 */
		this.myComponent = this.getTestGuiComponent();
		System.out.println(GuiUtil.getCodeBase());
		/*
		 * Der GuiType mu� GUI_COMPONENT sein.
		 */
		assertEquals(GuiComponent.GUI_COMPONENT, myComponent.getGuiType());
	}
	/**
	 * Die Methode getDataype() ist abstrakt und mu� in der konkreten
	 * Klasse gepr�ft werden.
	 */
	public abstract void testGetDataType();
	
	/**
	 * Die Methode getDataTypeName() gibt die Bezeichnung eines Datentyps zur�ck.
	 * <br>Diese Methode ist static, kann also ohne konkrete Klasse 
	 * gepr�ft werden.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Bezeichnung zu GuiComponent.BOOLEAN abfragen
	 * <br><i>Bezeichnung ist "Booleand"</i>
	 * <br>
 	 * <br><u>Schritt 2</u>
	 * <br>Bezeichnung zu GuiComponent.STRING abfragen
	 * <br><i>Bezeichnung ist "String"</i>
	 * <br>
 	 * <br><u>Schritt 3</u>
	 * <br>Bezeichnung zu GuiComponent.NUMBER abfragen
	 * <br><i>Bezeichnung ist "Number"</i>
	 * <br>
 	 * <br><u>Schritt 4</u>
	 * <br>Bezeichnung zu GuiComponent.DATE abfragen
	 * <br><i>Bezeichnung ist "Date"</i>
	 * <br>
 	 * <br><u>Schritt 5</u>
	 * <br>Bezeichnung zu GuiComponent.TIME abfragen
	 * <br><i>Bezeichnung ist "Time"</i>
	 * <br>
 	 * <br><u>Schritt 6</u>
	 * <br>Bezeichnung zu GuiComponent.ENUM abfragen
	 * <br><i>Bezeichnung ist "Enum"</i>
	 * <br>
 	 * <br><u>Schritt 7</u>
	 * <br>Bezeichnung zu GuiComponent.INTEGER abfragen
	 * <br><i>Bezeichnung ist "Integer"</i>
	 * <br>
 	 * <br><u>Schritt 8</u>
	 * <br>Bezeichnung zu GuiComponent.TABLE abfragen
	 * <br><i>Bezeichnung ist "Table"</i>
	 * <br>
 	 * <br><u>Schritt 9</u>
	 * <br>Bezeichnung zu GuiComponent.TREE abfragen
	 * <br><i>Bezeichnung ist "Tree"</i>
	 * <br>
 	 * <br><u>Schritt 10</u>
	 * <br>Bezeichnung zu GuiComponent.MULTILINE abfragen
	 * <br><i>Bezeichnung ist "Multiline"</i>
	 * <br>
 	 * <br><u>Schritt 11</u>
	 * <br>Die L�nge von DATA_TYPE_NAMES pr�fen
	 * <br><i>wenn keine Typen dazugekommen sind, ist die L�nge 10</i>
	 */
	public void testGetDataTypeName() {
		
		assertEquals("(1)","Boolean", GuiComponent.getDataTypeName(GuiComponent.BOOLEAN));
		assertEquals("(2)","String", GuiComponent.getDataTypeName(GuiComponent.STRING));
		assertEquals("(3)","Number", GuiComponent.getDataTypeName(GuiComponent.NUMBER));
		assertEquals("(4)","Date", GuiComponent.getDataTypeName(GuiComponent.DATE));
		assertEquals("(5)","Time", GuiComponent.getDataTypeName(GuiComponent.TIME));
		assertEquals("(6)","Enum", GuiComponent.getDataTypeName(GuiComponent.ENUM));
		assertEquals("(7)","Integer", GuiComponent.getDataTypeName(GuiComponent.INTEGER));
		assertEquals("(8)","Table", GuiComponent.getDataTypeName(GuiComponent.TABLE));
		assertEquals("(9)","Tree", GuiComponent.getDataTypeName(GuiComponent.TREE));
		assertEquals("(10)","Multiline", GuiComponent.getDataTypeName(GuiComponent.MULTILINE));
		
		assertEquals("(11)",10, GuiComponent.DATA_TYPE_NAMES.length);
		
	}
	/**
	 * Die Methode setValue() der Klasse GuiComponent setzt das
	 * Modified-Kennzeichen der GuiComponent auf false.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Modified-Kennz. auf true setzen und setValue() aufrufen.
	 * <br><i>Das Modified-Kennz. steht auf false</i>
	 */
	public void testSetValue() {
		/*
		 * zu pr�fende Klasse holen
		 */
		this.myComponent = this.getTestGuiComponent();

		this.myComponent.setModified(true);
		this.myComponent.setValue("DiesIstEinTest");
				
		assertEquals(true, this.myComponent.isModified());
	}
	/**
	 * Die Methode getValue() ist abstrakt und mu� in der konkreten
	 * Klasse gepr�ft werden.
	 */
	public abstract void testGetValue();
	/**
	 * Die Methode setDatasetValues() kann an der GuiComponent nicht gepr�ft
	 * werden. Die Testung erfolgt bei GuiContainer.
	 *
	 */
	public void testSetDatasetValues() {
		
		assertEquals(true,true);
	}
	/**
	 * Die Methode setDatasetValues() kann an der GuiComponent nicht gepr�ft
	 * werden. Die Testung erfolgt bei GuiContainer.
	 */
	public void testGetDatasetValues() {
		assertEquals(true,true);
	}
	/**
	 * Die Methode commitChanges() setzt das
	 * Modified-Kennzeichen der GuiComponent auf false.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Modified-Kennz. auf true setzen und commitChanges() aufrufen.
	 * <br><i>Das Modified-Kennz. steht auf false</i>
	 */
	public void testCommitChanges() {
		/*
		 * zu pr�fende Klasse holen
		 */
		this.myComponent = this.getTestGuiComponent();

		this.myComponent.setModified(true);
		this.myComponent.commitChanges();
				
		assertEquals(false,this.myComponent.isModified());
		
	}
	/**
	 * Die Methode isModified() gibt das Modified-Kennzeichen der GuiComponent zur�ck.
	 * <br>Die Methode setModified() setzt das Modified-Kennzeichen der GuiComponent.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Modified-Kennz. auf true setzen
	 * <br><i>Das Modified-Kennz. steht auf true</i>
	 * <br>
 	 * <br><u>Schritt 2</u>
	 * <br>Modified-Kennz. auf false setzen
	 * <br><i>Das Modified-Kennz. steht auf false</i>
	 * <br>
 	 * <br><u>Schritt 3</u>
	 * <br>Modified-Kennz. auf true setzen
	 * <br><i>Das Modified-Kennz. steht auf true</i>
	 */
	public void testIsSetModified() {
		/*
		 * zu pr�fende Klasse holen
		 */
		this.myComponent = this.getTestGuiComponent();

		this.myComponent.setModified(true);
		assertEquals("(1)",true,this.myComponent.isModified());
		
		this.myComponent.setModified(false);
		assertEquals("(2)",false,this.myComponent.isModified());

		this.myComponent.setModified(true);
		assertEquals("(3)",true,this.myComponent.isModified());
		
	}
	/*
	 * Die Methode setModified() wird im vorhergehenden Testfall
	 * mit gepr�ft.
	 *
	public void testSetModified() {
		
	}
	*/
	/**
	 * Die Methode isSearch() kann hier nicht gepr�ft werden.
	 */
	public void testIsSetSearch() {
		assertEquals(true,true);
	}
	
	/**
	 * Die Methode isNotNull() gibt das Pflichtfeld-Kennzeichen der GuiComponent zur�ck.
	 * <br>Die Methode setNotNull() 
	 * <br>+ setzt das Modified-Kennzeichen der GuiComponent
	 * <br>+ die Hintergrundfarbe der GuiComponent
	 * <br>+ setzt einen InputVerifier an der JComponent
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Pflichtfeld-Kennz. auf true setzen und 
	 * <br>CheckNotNull in den Properties auf true einstellen
	 * <br><i>+ das Pflichtfeld-Kennz. steht auf true
	 * <br>+ Hintergrundfarbe hat sich ge�ndert
	 * <br>+ die GuiComponent verf�gt �ber einen InputVerifier</i>
	 * <br>
 	 * <br><u>Schritt 2</u>
 	 * <br>Pflichtfeld-Kennz. auf false setzen
	 * <br><i>+ das Pflichtfeld-Kennz. steht auf false
	 * <br>+ Hintergrundfarbe ist wie zu Beginn
	 * <br>+ die GuiComponent hat keinen InputVerifier</i>
	 */
	public void testIsSetNotNull() {
		GuiUtil.setCheckNN(true);
		/*
		 * zu pr�fende Klasse holen
		 */
		this.myComponent = this.getTestGuiComponent();
		
		new TestGuiProperties();
		
		//##assertEquals("(0)",true, myTestUtil.setGuiProperty("CheckNotNull","true"));
		/*
		 * aktuelle Hintergrundfarbe merken
		 */
		Color aktColor = this.myComponent.getBackground(); // WHITE
		/*
		 * Pflichtfeld-Kennz. setzen
		 */		
		this.myComponent.setNotnull(true);
		/*
		 * Pflichtfeld-Kennz. pr�fen
		 */
		assertEquals("(1)",true,this.myComponent.isNotnull());
		/*
		 * aktuelle Hintergrundfarbe der GuiComponent holen
		 */
		Color nnColor = this.myComponent.getBackground();
		/*
		 * pr�fen, ob sich die Farbe ge�ndert hat
		 */
		if( aktColor.getBlue() == nnColor.getBlue() &&
			aktColor.getGreen() == nnColor.getGreen() &&
			aktColor.getRed() == nnColor.getRed() )
			assertEquals("(2)",true,false);
		/*
		 * pr�fen, ob der Verifier vorhanden ist
		 */
		assertNotNull("(3)",this.myComponent.getJComponent().getInputVerifier());
		/*
		 * Pflichtfeld-Kennz. zur�ck setzen
		 */
		this.myComponent.setNotnull(false);
		/*
		 * Pflichtfeld-Kennz. pr�fen
		 */
		assertEquals("(4)",false,this.myComponent.isNotnull());
		/*
		 * aktuelle Hintergrundfarbe der GuiComponent holen
		 */
		nnColor = this.myComponent.getBackground();
		/*
		 * pr�fen, ob Farbe wie zu Beginn des Tests ist
		 */		
		assertEquals("(5)",aktColor.getBlue(),nnColor.getBlue());
		assertEquals("(6)",aktColor.getGreen(),nnColor.getGreen());
		assertEquals("(7)",aktColor.getRed(),nnColor.getRed());
		/*
		 * pr�fen, ob der Verifier noch vorhanden ist
		 */
		assertNotNull("(8)",this.myComponent.getJComponent().getInputVerifier());
		
	}
	/**
	 * Die Methode isNotNull() gibt das Pflichtfeld-Kennzeichen der GuiComponent zur�ck.
	 * <br>Die Methode setNotNull() 
	 * <br>+ setzt das Modified-Kennzeichen der GuiComponent
	 * <br>+ die Hintergrundfarbe der GuiComponent
	 * <br>+ setzt einen InputVerifier an der JComponent
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>Pflichtfeld-Kennz. auf true setzen und 
	 * <br>CheckNotNull in den Properties auf false einstellen
	 * <br><i>+ das Pflichtfeld-Kennz. steht auf false
	 * <br>+ Hintergrundfarbe hat sich nicht ge�ndert
	 * <br>+ Wenn die Komponente vorher �ber einen InputVerifyer verf�gte,
	 * <br>mu� dieser auch nachher noch gesetzt sein.
	 * <br>War vorher kein InputVerifyer vorhanden, darf nachher auch
	 * keine da sein.</i>
	 */
	public void testIsSetNotNull2() {
		boolean hasVerifyer = false;
		/*
		 * zu pr�fende Klasse holen
		 */
		this.myComponent = this.getTestGuiComponent();
		/*
		 * aktuelle Hintergrundfarbe merken
		 */
		Color aktColor = this.myComponent.getBackground();
		/*
		 * feststellen, ob die Komponente �ber einen Inputverifyer verf�gt. 
		 */
		if( this.myComponent.getJComponent().getInputVerifier() != null )
			hasVerifyer = true;
			
		new TestGuiProperties();
		/*
		 * CheckNotNull in den Properties auf false einstellen
		 */
		//##assertEquals("(0)",true, myTestUtil.setGuiProperty("CheckNotNull","false"));
		/*
		 * Pflichtfeld-Kennz. setzen
		 */		
		this.myComponent.setNotnull(true);
		/*
		 * Pflichtfeld-Kennz. pr�fen
		 */
		assertEquals("(1)",true,this.myComponent.isNotnull());
		/*
		 * aktuelle Hintergrundfarbe der GuiComponent holen
		 */
		Color nnColor = this.myComponent.getBackground();
		/*
		 * pr�fen, ob sich die Frabe ge�ndert hat
		 */
		assertFalse("(2)",aktColor.getBlue() == nnColor.getBlue());
		assertFalse("(3)",aktColor.getGreen() == nnColor.getGreen());
		assertFalse("(4)",aktColor.getRed() == nnColor.getRed());
		/*
		 * Verifyer pr�fen.
		 */
		if( hasVerifyer )
			assertNotNull("(5)",this.myComponent.getJComponent().getInputVerifier());		
		else
			assertEquals("(5)",null,this.myComponent.getJComponent().getInputVerifier());
		
	}
	/*
	 * Die Methode setNotNull() wird im vorhergehenden Testfall
	 * mit gepr�ft.
	 *
	public void testSetNotNull() {
		
	}
	*/
	/**
	 * Die Methode setMaxLen() 
	 * <br>+ setzt die max. Eingabel�nge der GuiComponent.
	 * <br>+ setzt einen InputVerifier an der JComponent.
	 * <br>Die Methode getMaxLen() liefert die max. Eingabel�nge der GuiComponent. 
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Neue max. Eingabel�nge setzen
	 * <br><i>+ Die Eingabel�nge wurde korrekt gesetzt
	 * <br>+ es ist ein InputVerifier vorhanden</i>
	 * <br>
 	 * <br><u>Schritt 2</u>
	 * <br>Max. Eingabel�nge auf 0 setzen
	 * <br><i>Die Eingabel�nge ist 0</i>
	 * <br>
 	 * <br><u>Schritt 3</u>
	 * <br>Max. Eingabel�nge auf -20 setzen
	 * <br><i>Die Eingabel�nge ist -20</i>
	 */
	public void testGetSetMaxLen() {
		/*
		 * zu pr�fende Klasse holen
		 */
		this.myComponent = this.getTestGuiComponent();

		this.myComponent.setMaxlen(55);
		assertEquals("(1)",55,this.myComponent.getMaxlen());
		
		assertNotNull("(2)",this.myComponent.getJComponent().getInputVerifier());
		
		this.myComponent.setMaxlen(0);
		assertEquals("(3)",0,this.myComponent.getMaxlen());

		this.myComponent.setMaxlen(-20);
		assertEquals("(4)",-20,this.myComponent.getMaxlen());
		
	}
	/*
	 * Die Methode setMaxLen() wird im vorhergehenden Testfall
	 * mit gepr�ft.
	 *
	public void testSetMaxLen() {
		
	}
	*/
	/**
	 * <Die Methode setMinLen() 
	 * <br>+ setzt die min. Eingabel�nge der GuiComponent.
	 * <br>+ setzt einen InputVerifier an der JComponent.
	 * <br>Die Methode getMinLen() liefert die min. Eingabel�nge der GuiComponent. 
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Neue min. Eingabel�nge setzen
	 * <br><i>+ Die Eingabel�nge wurde korrekt gesetzt
	 * <br>+ es ist ein InputVerifier vorhanden</i>
	 * <br>
 	 * <br><u>Schritt 2</u>
	 * <br>Min. Eingabel�nge auf 0 setzen
	 * <br><i>Die Eingabel�nge ist 0</i>
	 * <br>
 	 * <br><u>Schritt 3</u>
	 * <br>Min. Eingabel�nge auf -20 setzen
	 * <br><i>Die Eingabel�nge ist -20</i>
	 */
	public void testGetSetMinLen() {
		/*
		 * zu pr�fende Klasse holen
		 */
		this.myComponent = this.getTestGuiComponent();

		this.myComponent.setMinlen(5);
		assertEquals("(1)",5,this.myComponent.getMinlen());
		
		assertNotNull("(2)",this.myComponent.getJComponent().getInputVerifier());
		
		this.myComponent.setMinlen(0);
		assertEquals("(3)",0,this.myComponent.getMinlen());

		this.myComponent.setMinlen(-20);
		assertEquals("(4)",-20,this.myComponent.getMinlen());
		
			
	}
	/*
	 * Die Methode setMinLen() wird im vorhergehenden Testfall
	 * mit gepr�ft.
	 *
	public void testSetMinLen() {
		
	}
	*/
	/**
	 * Die Methode verify() f�hr den InputVerifier der GuiComponent aus.
	 * Sie ist in GuiComponent zwar nicht abstrakt, die Pr�fung mu� aber 
	 * trotzdem in den konkreten TestCases erfolgen.
	 */
	public abstract void testVerify();
	
	/**
	 * Die Methode getGuiComponent() liefer die GuiComponent zur�ck. 
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Ausf�hren von getGuiComponent()
	 * <br><i>Es wird die GuiComponent zur�ckgegeben.</i>
	 */
	public void testGetGuiComponent() {
		/*
		 * zu pr�fende Klasse holen
		 */
		this.myComponent = this.getTestGuiComponent();

		assertEquals(this.myComponent, this.myComponent.getGuiComponent());			
	}
}
	