/*
 * Created on 07.08.2003
 */
package de.guibuilder.test.jdataset;

import junit.framework.TestCase;
import de.guibuilder.framework.*;
import de.jdataset.*;
import de.guibuilder.test.utils.TestDialogSupport;
import de.guibuilder.test.utils.TestJDataSetSupport;
import de.pkjs.util.Convert;
/**
 * @author kknobloch
 */
public class JDataSetSimpleTest extends TestCase {
	
	public TestDialogSupport testDialog = new TestDialogSupport("test/de/guibuilder/test/TestDialog.xml");
	public TestJDataSetSupport ds = new TestJDataSetSupport();
	/**
	 * Prüfung ob die Werte eines JDataSet korrekt in ein GuiWindow übernommen werden.
	 * <br>
	 * <br><b>Vorbereitung</b>
	 * <br>Über die GuiFactory wird eine Dialogbeschreibung geladen und damit ein 
	 * GuiWindow instanziert.
	 * <br>Die Dialogbeschreibung enthält für jede Klasse des GuiBuilder Framework eine
	 * Komponente.
	 * <br>In der Dialogbeschreibung sind alls notwendigen Angaben zur Verwendung 
	 * eines JDataSet enthalten 
	 * <br>Über eine Hilfsklasse wird ein, für die Dialogbeschreibung passendes, JDataSet
	 * angelegt. Dieses JDataSet wird an den Dialog gebunden.
	 * <br>
	 * <br>Hinweis: Diese Vorbereitung gilt für alle nachfolgende Testfälle.
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Über die GuiFactory die HiddenField-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Über die GuiFactory die GuiText-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 * <br>
	 * <br><u>Testschritt 3</u>
	 * <br>Über die GuiFactory die GuiNumber-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 * <br>
	 * <br><u>Testschritt 4</u>
	 * <br>Über die GuiFactory die GuiMoney-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 * <br>
	 * <br><u>Testschritt 5</u>
	 * <br>Über die GuiFactory die GuiMemo-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 * <br>
	 * <br><u>Testschritt 6</u>
	 * <br>Über die GuiFactory die GuiEditor-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 * <br>
	 * <br><u>Testschritt 7</u>
	 * <br>Über die GuiFactory die GuiDate-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 * <br>
	 * <br><u>Testschritt 8</u>
	 * <br>Über die GuiFactory die GuiTime-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 * <br>
	 * <br><u>Testschritt 9</u>
	 * <br>Über die GuiFactory die GuiPassword-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 * <br>
	 * <br><u>Testschritt 10</u>
	 * <br>Über die GuiFactory die GuiCheck-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 * <br>
	 * <br><u>Testschritt 11</u>
	 * <br>Über die GuiFactory die GuiOption-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 * <br>
	 * <br><u>Testschritt 12</u>
	 * <br>Über die GuiFactory die GuiOptionGroup-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 * <br>
	 * <br><u>Testschritt 13</u>
	 * <br>Über die GuiFactory die GuiCombo-Komponente ermitteln und aktuellen Wert prüfen.
	 * <br><i>Der Wert der Komponente entspricht dem Wert des JDataSet</i> 
	 */
	public void testDialog() {
		
		GuiComponent myComp;
		JDataSet myDataSet;
		GuiWindow win = testDialog.getWin();		
		myDataSet = ds.getEineAdresse();
		JDataSet myFktSet = ds.getCmbFunktionen();
			
		GuiCombo myCmb = (GuiCombo) win.getGuiComponent("cmbFunktion");
		myCmb.setItems(myFktSet);
		win.setDatasetValues(myDataSet);
		
		//System.out.println(win.getAllValuesXml());
		/*
		 * Prüfen, ob alle Werte korrekt in den Komponenten angekommen sind
		 */
		myComp = win.getGuiComponent("dfOid");
		assertEquals("(1)",myDataSet.getValuePath("Adressen@oid"),myComp.getValue());
			
		myComp = win.getGuiComponent("dfName");
		assertEquals("(2)",myDataSet.getValuePath("Adressen@name"),myComp.getValue());
			
		myComp = win.getGuiComponent("dfPLZ");
		assertEquals("(3)",myDataSet.getValuePath("Adressen@plz"),myComp.getValue());
			
		myComp = win.getGuiComponent("dfGebuehren");
		//assertEquals("(4)",myDataSet.getValuePath("Adressen@gebuehren"),myComp.getValue());
		assertEquals("(4)","12,50",myComp.getValue());
		
		myComp = win.getGuiComponent("dfInhaltsangabe");
		assertEquals("(5)",myDataSet.getValuePath("Adressen@inhaltsangabe"),myComp.getValue());
		
		myComp = win.getGuiComponent("dfEditor");
		assertEquals("(6)",myDataSet.getValuePath("Adressen@editor"),myComp.getValue());

		myComp = win.getGuiComponent("dfUngueltigDatum");
		assertEquals("(7)",myDataSet.getValuePath("Adressen@ungueltigSeit"),myComp.getValue());

		myComp = win.getGuiComponent("dfUhrzeit");
		assertEquals("(8)",myDataSet.getValuePath("Adressen@uhrzeit"),myComp.getValue());

		myComp = win.getGuiComponent("dfPasswort");
		//assertEquals("(9)",myDataSet.getValuePath("Adressen@passwort"),myComp.getValue());
		assertEquals("(9)","",myComp.getValue());

		myComp = win.getGuiComponent("cbGueltig");
		assertEquals("(10)",myDataSet.getValuePath("Adressen@gueltig"),Convert.toString((Boolean)myComp.getValue()));

		myComp = win.getGuiComponent("optGroup");
		assertEquals("(11)",myDataSet.getValuePath("Adressen@optvalue"),myComp.getValue());

		myComp = win.getGuiComponent("cmbFunktion");
		assertEquals("(12)",myDataSet.getValuePath("Adressen@fk_funkid"),myComp.getValue());

		myComp = win.getGuiComponent("optArtR");
		assertEquals("(13)",myDataSet.getValuePath("Adressen@art"),myComp.getValue());
		
	}
	/**
	 * Spezielle Prüfung der GuiText-Komponente.
	 * <br>
	 * <br><b>Vorbereitung</b>
	 * <br>siehe Testfall <i>testDialog</i>
	 * <br>
	 * <br><b>Testschritte</b>
	 * <br>
	 * <br><u>Testschritt 1</u>
	 * <br>Dem GuiText wird ein neuer Wert zugewiesen.
	 * <br><i>+ Der neue Wert des GuiText ist im JDataSet vorhanden.
	 * <br>+getChanges() liefert ein Objekt</i>
	 * <br>
	 * <br><u>Testschritt 2</u>
	 * <br>Die Änderungen mit JDataSet.rollbackChanges() rückgängig machen
	 * <br><i>+ im JDataSet steht nicht mehr der neue Wert
	 * <br>+ im GuiText steht der gleiche Wert im JDataSet</i>
	 */
	public void testGuiText() {
		GuiText myComp;
		JDataSet myDataSet;
		GuiWindow win = testDialog.getWin();		
		JDataSet myFktSet = ds.getCmbFunktionen();
			
		GuiCombo myCmb = (GuiCombo) win.getGuiComponent("cmbFunktion");
		myCmb.setItems(myFktSet);
		myDataSet = ds.getEineAdresse();
		win.setDatasetValues(myDataSet);

		// dem GuiText einen Wert zuweisen
		myComp = (GuiText) win.getGuiComponent("dfName");
		myComp.setValue("Heiko Schmidt");
		// dem GuiWindow mitteilen, daß es das JDataSet aktualisieren soll
		win.getDatasetValues();
		// prüfen				
		assertEquals("(1.1)","Heiko Schmidt", myDataSet.getValuePath("Adressen@name"));
		assertNotNull("(1.2)", myDataSet.getChanges());
		// Änderung rückgängig machen 	
		myDataSet.rollbackChanges();
		//
		// 
		assertEquals("(2.1)",false,myDataSet.getValuePath("Adressen@name" ).equalsIgnoreCase("Heiko Schmidt"));
		//assertEquals("(2.2)",myDataSet.getValuePath("Adressen@name" ),myComp.getValue());
	}

	public void testHiddenField() {
		//##assertEquals(true,false);
	}

	public void testGuiNumber() {
		GuiComponent myComp;
		JDataSet myDataSet;
		GuiWindow win = testDialog.getWin();		
		myDataSet = ds.getEineAdresse();
			
		win.setDatasetValues(myDataSet);

		myComp = win.getGuiComponent("dfPLZ");
		myComp.setValue("10965");
		win.getDatasetValues();
						
		assertEquals("(1)","10965", myDataSet.getValuePath("Adressen@plz"));
		assertNotNull("(2)", myDataSet.getChanges());
			
		myDataSet.commitChanges();
		assertNull("(3)", myDataSet.getChanges());
	}
	
	public void testGuiMoney() {
		GuiComponent myComp;
		JDataSet myDataSet;
		GuiWindow win = testDialog.getWin();		
		myDataSet = ds.getEineAdresse();
			
		win.setDatasetValues(myDataSet);

		myComp = win.getGuiComponent("dfGebuehren");
		myComp.setValue("12");
		win.getDatasetValues();
						
		assertEquals("(1)","12,00", myDataSet.getValuePath("Adressen@gebuehren"));
		assertNotNull("(2)", myDataSet.getChanges());
			
		myDataSet.commitChanges();
		assertNull("(3)", myDataSet.getChanges());
	}

	public void testGuiMemo() {
		//##assertEquals(true,false);
	}

	public void testGuiEditor() {
		//##assertEquals(true,false);
	}

	public void testGuiDate() {
		//##assertEquals(true,false);
	}

	public void testGuiTime() {
		//##assertEquals(true,false);
	}

	public void testGuiPassword() {
		//##assertEquals(true,false);
	}
	
	public void testGuiCheck() {
		//##assertEquals(true,false);
	}
	
	public void testGuiOption() {
		//##assertEquals(true,false);
	}
	
	public void testGuiOptionGroup() {
		//##assertEquals(true,false);
	}
	
	public void testGuiCombo() {
		//##assertEquals(true,false);
	}
}
