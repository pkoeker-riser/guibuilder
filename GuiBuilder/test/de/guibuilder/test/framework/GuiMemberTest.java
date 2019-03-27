/*
 * Created on 14.07.2003
 *
 */
package de.guibuilder.test.framework;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JTextField;

import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiWindow;
import de.guibuilder.framework.GuiContainer;
import de.guibuilder.framework.GDLParseException;
import electric.xml.Document;
import electric.xml.Element;

import junit.framework.TestCase;

/**
 * @author kknobloch
 * TestCase f�r die Komponente GuiMember.
 */
public abstract class GuiMemberTest extends TestCase {
	/**
	 * Klassenvariable mit der alle Tests vorgenommen werden
	 */
	protected GuiMember myMember;
	/**
	 * Methode mit welcher vom nicht abstrakten TestCase den
	 * GuiMember abgeholt werden kann.
	 * 
	 * @return GuiMember, zu testende Klasse 
	 */
	public abstract GuiMember getTestGuiMember();
	/**
	 * Methode mit welcher im nicht abstrakten TestCase Pr�fungen
	 * durchgef�hrt werden.
	 * 
	 * @param otyp, Bezeichnung des zu pr�fenden Wertes
	 * @param o, der zu pr�fende Wert
	 * @return true, wenn Pr�fung erfolgreicht. false, sonst
	 */
	public abstract boolean validateGuiMemberValue(String otyp, Object o);
	/**
	 * Pr�fen, ob die Methode getGuiMember() tats�chlich eine
	 * zu pr�fende Klasse zur�ck liefert.
	 */
	public void testInit() {
			
		this.myMember = this.getTestGuiMember();
		if(this.myMember == null )
			fail("GuiMember ist NULL!");
		
	}
	/**
	 * Der Konstruktor GuiMember(String) setzt den Label und den Namen
	 * des GuiMember. Da GuiMember aber nicht selber instanziert werden
	 * kann, mu� diese Pr�fung in den konkreten Klassen erfolgen.
	 */
	public abstract void testConstructorString();
	
	/**
	 * Die Methode getGuiParent() liefert den GuiContainer zur�ck, auf 
	 * dem sich ein GuiMember befindet.
	 * <br>Die Methode setGuiParent() setzt den Container, auf dem
	 * sich der GuiMember befinden soll.
	 * <br>
	 * <br><u><b>Testschritte:</b></u>
	 * <br>
	 * <br><u>Schritt 1</u>
	 * <br>�ber GuiFactory einen Dialog mit zwei Containern instanzieren und
	 * den aktuellen Container des GuiMember pr�fen.
	 * <br><i>Der Name des aktuellen GuiParent ist "Tab1"</i>
	 * <br>
	 * <br><u>Schritt 2</u>
	 * <br>Als Parent die zweite Registerkarte setzen und anschlie�en den
	 * Parent des GuiMember pr�fen.
	 * <br><i>Der Name des aktuellen GuiParent ist "Tab2"</i>
	 */
	public void testGetSetGuiParent() {
		
		Document myDoc = new Document();
		Element myElem = myDoc.addElement("GDL");
		Element myFrm = myElem.addElement("Form");
		myFrm.setAttribute("name","TestForm");
	
		Element myTabSet = myFrm.addElement("Tabset");
		Element myTab = myTabSet.addElement("Tab");
		myTab.setAttribute("name","Tab1");
		Element myFld = myTab.addElement("Text");
		myFld.setAttribute("name","dfText1");
		myTab = myTabSet.addElement("Tab");
		myTab.setAttribute("name","Tab2");
		myFld = myTab.addElement("Text");
		myFld.setAttribute("name","dfText2");
		
		GuiFactory myFact = GuiFactory.getInstance();
		
		try{
			GuiWindow myWin = myFact.createWindow(myDoc);
			this.myMember = myWin.getGuiMember("Tab1.dfText1");
			
			assertEquals("(1)","Tab1",this.myMember.getGuiParent().getName());

			GuiMember myGTab = myWin.getGuiMember("Tab2");
			this.myMember.setGuiParent((GuiContainer) myGTab);
										
			assertEquals("(1)","Tab2",this.myMember.getGuiParent().getName());
			
		}
		catch(GDLParseException e) {
			fail("GDLParseException");
		}
	}
	/**
	 * Die Methode getName() liefert den Namen des GuiMember zur�ck.
	 * <br>
	 * <br><u><b>Testschritte:</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
 	 * <br>GuiMember mit neuem Namen versehen
	 * <br><i>der Name wurde korrekt gesetzt</i>
	 */
	public void testGetName() {
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();
		/*
		 * der Klasse einen anderen Namen verpassen
		 */
		this.myMember.setName("myMemberName");
		/*
		 * pr�fen, ob der Name korrekt ist
		 */
		assertEquals("myMemberName", this.myMember.getName());
		
	}
	/**
	 * Die Methode setName() setzt den Name des GuiMember neu und,
	 * f�r den Fall das der Label leer ist, auch diesen
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Label des Member auf null setzen und neuen Namen vergeben
	 * <br><i>Name und Label sind identisch</i>
	 * <br>
 	 * <br><u>Schritt 2</u>
	 * <br>Den Namen erneut umsetzen
	 * <br><i>Label wie vorher<br>Name entspricht dem neu gesetztem</i>
	 */
	public void testSetName() {
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();
		/*
		 * Label des GuiMember auf null setzen
		 */		
		this.myMember.setLabel(null);
		/*
		 * neuen Namen setzen, Name und Label m�ssen identisch sein
		 */
		this.myMember.setName("myMemberName2");
		assertEquals("(1)","myMemberName2", this.myMember.getName());
		assertEquals("(2)","myMemberName2", this.myMember.getLabel());
		/*
		 * den Namen erneut umsetzen, Label mu� der alte bleiben
		 */
		this.myMember.setName("myMemberName3");
		assertEquals("(3)","myMemberName3", this.myMember.getName());
		assertEquals("(4)","myMemberName2", this.myMember.getLabel());
	}
	/**
	 * Die Methode setLabel() setzt den Label des GuiMember neu.
	 * <br>Die Methode getLabel() gibt den Label des GuiMember zur�ck.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Lable des GuiMember setzen
	 * <br><i>Der zur�ckgelieferte Label entspricht dem zuvor gesetztem</i>
	 */
	public void testGetSetLabel() {
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();
		/*
		 * dem Member einen neuen Label verpassen
		 */
		this.myMember.setLabel("myMemberLabel");
		/*
		 * pr�fen, ob der Label korrekt ist
		 */
		assertEquals("myMemberLabel",this.myMember.getLabel());
	}
	/*
	 * die Methode setLabel() wird im vorhergehende Testfall
	 * mit gepr�ft.
	 *  
	public void testSetLabel() {
	}
	*/
	/**
	 * Die Methoden getElementName() und setElementName() k�nnen hier
	 * nicht getestet werden. 
	 */
	public void testGetElementName() {
		assertEquals(true,true);
	}
	/**
	 * Die Methode isVisible() gibt das visible-Flag der JComponent zur�ck.
	 * <br>Die Methode setVisible() setzt das visible-Flag der JComponent.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Sichtbarkeit des GuiMember abfragen
	 * <br><i>Standardm��ig sind GuiMember sichtbar => Flag also true</i>
	 * <br>
 	 * <br><u>Schritt 2</u>
	 * <br>GuiMember unsichtbar setzen
	 * <br><i>Das visible-Flag steht auf false</i>
	 * <br>
 	 * <br><u>Schritt 3</u>
	 * <br>GuiMember sichtbar setzen
	 * <br>Das visible-Flag steht wieder auf true</i>
	 */
	public void testIsSetVisible() {
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();
		/*
		 * Standardm��ig sollte die Komponente sichtbar sein
		 */
		assertEquals("(1)", true, this.myMember.isVisible());
		/*
		 * jetzt unsichtbar setzen
		 */
		this.myMember.setVisible(false);
		assertEquals("(2)", false, this.myMember.isVisible());
		/*
		 * jetzt wieder sichtbar setzen
		 */
		this.myMember.setVisible(true);
		assertEquals("(3)", true, this.myMember.isVisible());
	}
	/*
	 * Die Methode setVisible() wird im vorhergehenden Test 
	 * mit gepr�ft.
	 *
	public void testSetVisible() {
		assertEquals(false,true);
	}
	*/

	/**
	 * @deprecated
	 * Die Methode getFullName() gibt den kompletten Namen (mit Pfad)
	 * des GuiMember zur�ck.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>�ber die GuiFacotry einen Dialog instanzieren und den Namen
	 * des GuiMember abfragen.
	 * <br><i>Die Methode liefert das korrekte Ergebnis</i>
	 */	
	public void testGetFullName() {
		
		Document myDoc = new Document();
		Element myElem = myDoc.addElement("GDL");
		Element myFrm = myElem.addElement("Form");
		myFrm.setAttribute("name","TestForm");
	
		Element myTabSet = myFrm.addElement("Tabset");
		Element myTab = myTabSet.addElement("Tab");
		myTab.setAttribute("name","Tab1");
		Element myFld = myTab.addElement("Text");
		myFld.setAttribute("name","dfText1");
		
		GuiFactory myFact = GuiFactory.getInstance();
		
		try{
			GuiWindow myWin = myFact.createWindow(myDoc);
			this.myMember = myWin.getGuiMember("Tab1.dfText1");
			
			assertEquals("Tab1.dfText1",this.myMember.getFullName());			
			
		}
		catch(GDLParseException e) {
			fail("GDLParseException");
		}
		
	}
	/**
	 * Die Methode getGuiType() ist abstrakt und mu� in der konkreten
	 * Klasse gepr�ft werden.
	 */
	public abstract void testGetGuiType();
	
	/**
	 * Die Methode getGuiTypeName() die Bezeichnung eines GuiType zur�ck.
	 * <br>Diese Methode ist static, kann also ohne konkrete Klasse 
	 * gepr�ft werden.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Bezeichnung zu 0 abfragen
	 * <br><i>Bezeichnung ist "Member"</i>
	 * <br>
 	 * <br><u>Schritt 2</u>
	 * <br>Bezeichnung zu 1 abfragen
	 * <br><i>Bezeichnung ist "Element"</i>
	 * <br>
 	 * <br><u>Schritt 3</u>
	 * <br>Bezeichnung zu GUI_ACTION abfragen
	 * <br><i>Bezeichnung ist "Action"</i>
	 * <br>
 	 * <br><u>Schritt 4</u>
	 * <br>Bezeichnung zu GUI_COMPONENT abfragen
	 * <br><i>Bezeichnung ist "Component"</i>
	 * <br>
 	 * <br><u>Schritt 5</u>
	 * <br>Bezeichnung zu GUI_CONTAINER abfragen
	 * <br><i>Bezeichnung ist "Container"</i>
	 * <br>
 	 * <br><u>Schritt 6</u>
	 * <br>Bezeichnung zu GUI_TABLE abfragen
	 * <br><i>Bezeichnung ist "Table"</i>
	 * <br>
 	 * <br><u>Schritt 7</u>
	 * <br>Bezeichnung zu GUI_TREE abfragen
	 * <br><i>Bezeichnung ist "Tree"</i>
	 * <br>
 	 * <br><u>Schritt 8</u>
	 * <br>Die L�nge von GUI_TYPE_NAMES pr�fen
	 * <br><i>wenn keine Typen dazugekommen sind, ist die L�nge 5</i>
	 */
	public void testGetGuiTypeName() {
		assertEquals("(1)","Member", GuiMember.getGuiTypeName(0));
		assertEquals("(2)","Element", GuiMember.getGuiTypeName(1));
		assertEquals("(3)","Action", GuiMember.getGuiTypeName(GuiMember.GUI_ACTION));
		assertEquals("(4)","Component", GuiMember.getGuiTypeName(GuiMember.GUI_COMPONENT));
		assertEquals("(5)","Container", GuiMember.getGuiTypeName(GuiMember.GUI_CONTAINER));
		assertEquals("(6)","Table", GuiMember.getGuiTypeName(GuiMember.GUI_TABLE));
		assertEquals("(7)","Tree", GuiMember.getGuiTypeName(GuiMember.GUI_TREE));
		
		assertEquals("(8)",7,GuiMember.GUI_TYPE_NAMES.length);
		
	}
	/**
	 * Die Methode reset() ist abstrakt und mu� in der konkreten
	 * Klasse gepr�ft werden.
	 */
	public abstract void testReset();
	/**
	 * Die Methode verify() ist abstrakt und mu� in der konkreten
	 * Klasse gepr�ft werden.
	 */
	public abstract void testVerify();
	/**
	 * Die Methode setMinimumSize() setzt die mind. Gr��e eines GuiMember.
	 * <br>Die Methode getMinimumSize() liefert die mind. Gr��e des GuiMember.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>MinimumSize des Members um 33 bzw. 333 erh�hen.
	 * <br><i>Die neue MinimumSize ist gesetzt.</i>
	 */
	public void testSetGetMinimumSize() {
		Dimension d = new Dimension();
		Dimension aktDim;
		Dimension newDim;
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();
		/*
		 * aktuelle MinimumSize besorgen
		 */		
		aktDim = this.myMember.getMinimumSize();
		/*
		 * neue MinimumSize berechnen
		 */
		d.height = aktDim.height + 33;
		d.width = aktDim.width + 333;
		/*
		 * neue MinimumSize setzen
		 */
		this.myMember.setMinimumSize(d);
		/*
		 * pr�fen, ob die Size korrekt gesetzt wurde
		 */
		newDim = this.myMember.getMinimumSize();

		assertEquals("(1)",aktDim.height  + 33,newDim.height);
		assertEquals("(2)",aktDim.width + 333,newDim.width);
	}
	/*
	 * Die Methode getMinimumSize wird im vorhergehenden Testfall mit gepr�ft.
	 * 
	public void testGetMinimumSize() {
		assertEquals(false,true);
	}
	*/
	/**
	 * Die Methode setMaximumSize() setzt die max. Gr��e eines GuiMember.
	 * <br>Die Methode getMaximumSize() liefert die max. Gr��e des GuiMember.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>MaximumSize des Members um 44 bzw. 444 erh�hen.
	 * <br><i>Die neue MaximumSize ist gesetzt.</i>
	 */
	public void testSetGetMaximumSize() {
		
		Dimension d = new Dimension();
		Dimension aktDim;
		Dimension newDim;
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();
		/*
		 * aktuelle MaximumSize besorgen
		 */		
		aktDim = this.myMember.getMaximumSize();
		/*
		 * neue MaximumSize berechnen
		 */
		d.height = aktDim.height + 44;
		d.width = aktDim.width + 444;
		/*
		 * neue MaximumSize setzen
		 */
		this.myMember.setMaximumSize(d);
		/*
		 * pr�fen, ob die Size korrekt gesetzt wurde
		 */
		newDim = this.myMember.getMaximumSize();

		assertEquals("(1)",aktDim.height  + 44,newDim.height);
		assertEquals("(2)",aktDim.width + 444,newDim.width);
	}
	/*
	 * Die Methode getMaximumSize() wird im vorhergehenden Testfall
	 * mit gepr�ft
	 *
	public void testGetMaximumSize() {
		assertEquals(false,true);
	}
	*/
	/**
	 * Die Methode setPreferredSize() setzt die bevorzugte Gr��e eines GuiMember.
	 * <br>Die Methode getPreferredSize() liefert die bevorzugte Gr��e des GuiMember.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>PreferredSize des Members um 55 bzw. 555 erh�hen.
	 * <br><i>Die neue PreferredSize ist gesetzt.</i>
	 */
	public void testSetGetPreferredSize() {
		
		Dimension d = new Dimension();
		Dimension aktDim;
		Dimension newDim;
		JTextField jtext = new JTextField();
		int aktCol = 0;
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();
		/*
		 * F�r den Fall, da� die Columns-Anzahl eines 
		 * JTextField gesetzt ist, wird beim Abfragen
		 * der prefferd size eine Berechnung durchgef�hrt. 
		 * Dies soll hier vermieden werden und darum wird
		 * die Anzahl Columns auf 0 gesetzt.
		 */
		String s =  this.myMember.getJComponent().getClass().toString();
		if( s.equalsIgnoreCase("class javax.swing.JTextField") ) {
			jtext = (JTextField) this.myMember.getJComponent();
			aktCol = jtext.getColumns();
			jtext.setColumns(0);
		} 
		/*
		 * aktuelle preferred Size besorgen
		 */		
		aktDim = this.myMember.getPreferredSize();
		/*
		 * neue preferred Size berechnen
		 */
		d.height = aktDim.height + 55;
		d.width = aktDim.width + 555;
		/*
		 * neue preferred Size setzen
		 */
		this.myMember.setPreferredSize(d);
		/*
		 * pr�fen, ob die Size korrekt gesetzt wurde
		 */
		newDim = this.myMember.getPreferredSize();

		assertEquals("(1)",aktDim.height  + 55,newDim.height);
		assertEquals("(2)",aktDim.width + 555,newDim.width);
		
		if( jtext != null )
			jtext.setColumns(aktCol);
	}
	/*
	 * Die Methode getPreferredSize() wird im vorhergehenden Testfall
	 * mit gepr�ft.
	 *
	public void testGetPreferredSize() {
		assertEquals(false,true);
	}
	*/
	/**
	 * Die Methode setFont() setzt die Schriftart und -gr��e des GuiMember
	 * <br>Die Methode getFont() liefert die Schriftart des GuiMember.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Einen neuen Font definieren und diesem dem GuiMember zuweisen
	 * <br><i>Der neue Font wurde gesetzt</i>
	 */
	public void testSetGetFont() {
		Font aktFont;
		Font newFont;
		Font f = new Font("Arial", Font.PLAIN, 12);
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();
		/*
		 * aktuellen Font des GuiMember holen
		 */
		aktFont = this.myMember.getFont();	
		/*
		 * neuen Font setzen
		 */
		this.myMember.setFont(f);
		/*
		 * aktuellen Font holen und vergleichen
		 */
		newFont = this.myMember.getFont();
		assertEquals("(1)",f.isBold(),newFont.isBold());
		assertEquals("(2)",f.isItalic(),newFont.isItalic());
		assertEquals("(3)",f.isPlain(),newFont.isPlain());
		assertEquals("(4)",f.getFontName(),newFont.getFontName());
		/*
		 * den alte Font wieder setzen
		 */
		this.myMember.setFont(aktFont);
		
	}
	/*
	 * Die Methode setFont() wird im vorhergehenden Testfall
	 * mit gepr�ft
	 *
	public void testGetFont() {
		assertEquals(false,true);
	}
	*/
	/**
	 * Die Methode getToolTipText() liefert den Tooltiptext des 
	 * GuiMember zur�ck.
	 * <br>Die Methode setToolTipText() setzt den Tooltiptest des GuiMember.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Dem GuiMember einen neuen Tooltiptext zuweisen
	 * <br><i>Der Tooltiptext wurde korrekt �bernommen</i>
	 */
	public void testGetToolTipText() {
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();
		/*
		 * neuen Tooltip-Text setzen
		 */
		this.myMember.setToolTipText("myMemberToolTipText");		
		/*
		 * Tooltip pr�fen
		 */
		assertEquals("myMemberToolTipText",this.myMember.getToolTipText());
	}
	/*
	 * Die Methode setToolTipText() wird im vorhergehenden Testfall
	 * mit gepr�ft.
	 *
	public void testSetToolTipText() {
		assertEquals(false,true);
	}
	*/
	/**
	 * Die Methode setBackground() setzt die Hintergrundfarbe des GuiMember.
	 * <br>Die Methode getBackground() liefert die Hintergrundfarbe des GuiMember.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Dem GuiMember eine neue Hintergrundfarbe zuweisen
	 * <br><i>Die Hintergrundfarbe wurde korrekt gesetzt</i>
	 */
	public void testGetSetBackground() {
		Color aktColor;
		Color newColor;
		Color c = Color.getHSBColor(111,222,333);
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();
		/*
		 * aktuelle Hintergrundfarbe einlesen
		 */
		aktColor = this.myMember.getBackground();
		/*
		 * neue Hintergrundfarbe setzen
		 */
		this.myMember.setBackground(c);
		/*
		 * Hintergrundfarbe einlesen und pr�fen
		 */
		newColor = this.myMember.getBackground();
		
		assertEquals("(1)",c.getBlue(),newColor.getBlue());
		assertEquals("(2)",c.getRed(),newColor.getRed());
		assertEquals("(3)",c.getGreen(),newColor.getGreen());
		
		this.myMember.setBackground(aktColor);
		
	}
	/*
	 * Die Methode setBackground() wird im vorhergehenden Testfall 
	 * mit gepr�ft.
	 *
	public void testSetBackground() {
		assertEquals(false,true);
	}
	*/
	/**
	 * Die Methode setForeground() setzt die Vordergrundfarbe des GuiMember.
	 * <br>Die Methode getForeground() liefert die Vordergrundfarbe des GuiMember.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Dem GuiMember eine neue Vordergrundfarbe zuweisen
	 * <br><i>Die Vordergrundfarbe wurde korrekt gesetzt</i>
	 */
	public void testGetForeground() {
		Color aktColor;
		Color newColor;
		Color c = Color.getHSBColor(111,222,333);
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();
		/*
		 * aktuelle Vordergrundfarbe einlesen
		 */
		aktColor = this.myMember.getForeground();
		/*
		 * neue Vordergrundfarbe setzen
		 */
		this.myMember.setForeground(c);
		/*
		 * Vordergrundfarbe einlesen und pr�fen
		 */
		newColor = this.myMember.getForeground();
		
		assertEquals("(1)",c.getBlue(),newColor.getBlue());
		assertEquals("(2)",c.getRed(),newColor.getRed());
		assertEquals("(3)",c.getGreen(),newColor.getGreen());
		
		this.myMember.setForeground(aktColor);
	}
	/*
	 * Die Methode setForground() wird im vorhergehenden Testfall
	 * mit gepr�ft.
	 *
	public void testSetForeground() {
		assertEquals(false,true);
	}
	*/
	/**
	 * Die Methode setRef() setzt die Reference des GuiMember.
	 * <br>Die Methode getRef() liefert die aktuelle Reference des GuiMember.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Dem GuiMember eine neue Reference zuweisen
	 * <br><i>Die Reference wurde korrekt gesetzt</i>
	 * <br>
 	 * <br><u>Schritt 2</u>
	 * <br>Dem GuiMember eine leere Reference zuweisen
	 * <br><i>Die Reference des GuiMember ist leer</i>
	 * <br>
 	 * <br><u>Schritt 3</u>
	 * <br>Dem GuiMember null als Reference zuweisen
	 * <br><i>Die Reference des GuiMember ist null</i>
	 */
	public void testSetGetRef() {
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();

		this.myMember.setRef("myMemberReference");
		assertEquals("(1)","myMemberReference",this.myMember.getRef());

		this.myMember.setRef("");
		assertEquals("(2)","",this.myMember.getRef());

		this.myMember.setRef(null);
		assertEquals("(3)",null,this.myMember.getRef());
	}	
	/*
	 * Die Methode getRef() wird im vorhergehenden Testfall
	 * mit gepr�ft.
	 *
	public void testGetRef() {
		assertEquals(false,true);
	}
	*/
	/**
	 * Die Methode setOid() setzt die Objekt-ID des GuiMember.
	 * <br>Die Methode getOid() liefert die aktuelle Objekt-ID des GuiMember.
	 * <br>
	 * <br><u><b>Testschritte</b></u>
	 * <br>
 	 * <br><u>Schritt 1</u>
	 * <br>Dem GuiMember eine neue Objekt-ID zuweisen
	 * <br><i>Die Objekt-ID wurde korrekt gesetzt</i>
	 * <br>
 	 * <br><u>Schritt 2</u>
	 * <br>Dem GuiMember 0 als Objekt-ID zuweisen
	 * <br><i>Die Objekt-ID des GuiMember ist 0</i>
	 * <br>
 	 * <br><u>Schritt 3</u>
	 * <br>Dem GuiMember -1 als Objekt-ID zuweisen
	 * <br><i>Die Objekt-ID des GuiMember ist -1</i>
	 *
	 */
	public void testSetGetOid() {
		/*
		 * zu pr�fende Klasse besorgen
		 */
		this.myMember = this.getTestGuiMember();

		long aktOID = this.myMember.getOid();
		
		this.myMember.setOid(4711);
		assertEquals("(1)",4711,this.myMember.getOid());

		this.myMember.setOid(0);
		assertEquals("(2)",0,this.myMember.getOid());

		this.myMember.setOid(-1);
		assertEquals("(3)",-1,this.myMember.getOid());

		this.myMember.setOid(aktOID);
		
	}
	/*
	 * Die Methode getOid() wird im vorhergehenden Testfall 
	 * mit gepr�ft.
	 *
	public void testGetOid() {
		assertEquals(false,true);
	}
	*/
}
