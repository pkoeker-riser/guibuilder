// %Package%
package test;
// %Import% /FINAL
import de.guibuilder.framework.GuiButton;
import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiElement;
import de.guibuilder.framework.GuiForm;
import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiMenu;
import de.guibuilder.framework.GuiMenuBar;
import de.guibuilder.framework.GuiMenuItem;
import de.guibuilder.framework.GuiPanel;
import de.guibuilder.framework.GuiPopupMenu;
import de.guibuilder.framework.GuiSplit;
import de.guibuilder.framework.GuiTab;
import de.guibuilder.framework.GuiTable;
import de.guibuilder.framework.GuiToolbar;
import de.guibuilder.framework.GuiTree;
import de.guibuilder.framework.GuiTreeContent;
import de.guibuilder.framework.GuiTreeElement;
import de.guibuilder.framework.GuiTreeNode;
import de.guibuilder.framework.GuiUtil;
import de.guibuilder.framework.GuiWindow;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
/**
 * Docu
 *
 * @version
 * @author
 */
// %Form_Class%
public class Buero extends GuiForm {
	// %ClassAttributes%
	private GuiMenuBar menuBar;
	private GuiMenu file;
	private GuiMenuItem v_new;
	private GuiMenuItem load;
	private GuiMenuItem save;
	private GuiMenuItem cut;
	private GuiMenuItem copy;
	private GuiMenuItem paste;
	private GuiMenuItem pasteBelow;
	private GuiMenuItem nodeUp;
	private GuiMenuItem nodeDown;
	private GuiMenuItem nodeLeft;
	private GuiMenuItem nodeRight;
	private GuiMenuItem exit;
	private GuiMenu menu2;
	private GuiMenuItem help;
	private GuiMenuItem aboutBuero;
	private GuiToolbar toolbar;
	private GuiButton tool;
	private GuiButton tool3;
	private GuiButton tool4;
	private GuiButton tool6;
	private GuiButton tool7;
	private GuiButton tool8;
	private GuiButton tool9;
	private GuiButton tool11;
	private GuiButton tool12;
	private GuiButton tool13;
	private GuiButton tool14;
	private GuiTree buero;
	private GuiTreeNode rootNode;
	private GuiPopupMenu tree;
	private GuiMenuItem v_new2;
	private GuiMenuItem load2;
	private GuiMenuItem save2;
	private GuiMenuItem cut2;
	private GuiMenuItem copy2;
	private GuiMenuItem pasteAsChild;
	private GuiMenuItem pasteBelow2;
	private GuiMenuItem nodeUp2;
	private GuiMenuItem nodeDown2;
	private GuiMenuItem nodeLeft2;
	private GuiMenuItem nodeRight2;
	// %Form_Constructor%
	public Buero() {
		super();
		guiInit();
	}
	/* Diese Methode baut initial die Oberfläche gemäß Spezifikation zusammen.
	Sie muß von allen Constructoren aufgerufen werden.*/
	private void guiInit() {
		/* Lokale Attribute der Methode guiInit, die direkt erzeugt werden */
		GuiUtil.setUiManager("Windows");
		// %guiInitAttributes%
		/* Lokale Attribute der Methode guiInit für den mehrfachen Gebrauch */
		// %guiInitLocal% /FINAL
		GuiSplit split;
		GuiTreeNode tmpNode;
		/* Abschließend der Bereich, in dem das eigentliche UI erzeugt wird*/
		// %guiInitAction%
		this.setTitle("Mein Büro");
		this.getComponent().setSize(650, 500);
		this.setName("meinBuero");
		GuiPanel mainPanel = this.getMainPanel();
		this.menuBar = this.getGuiMenuBar();
		// Menu %File
		file = new GuiMenu("%File");
		file.setName("file");
		menuBar.add(file);
		// MenuItem "%New"
		v_new = new GuiMenuItem("%New", file);
		v_new.setIcon(GuiUtil.makeIcon("../images/new.gif"));
		v_new.setActionCommand("newTreeNode");
		v_new.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		this.getMainPanel().addAction(v_new);
		// MenuItem "%Load"
		load = new GuiMenuItem("%Load", file);
		load.setIcon(GuiUtil.makeIcon("../images/open.gif"));
		load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		this.getMainPanel().addAction(load);
		// MenuItem "%Save"
		save = new GuiMenuItem("%Save", file);
		save.setIcon(GuiUtil.makeIcon("../images/save.gif"));
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		this.getMainPanel().addAction(save);
		// Separator
		file.addSeparator();
		// MenuItem "%Cut"
		cut = new GuiMenuItem("%Cut", file);
		cut.setIcon(GuiUtil.makeIcon("../images/cut.gif"));
		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		this.getMainPanel().addAction(cut);
		// MenuItem "C%opy"
		copy = new GuiMenuItem("C%opy", file);
		copy.setIcon(GuiUtil.makeIcon("../images/copy.gif"));
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		this.getMainPanel().addAction(copy);
		// MenuItem "%Paste"
		paste = new GuiMenuItem("%Paste", file);
		paste.setIcon(GuiUtil.makeIcon("../images/paste.gif"));
		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		this.getMainPanel().addAction(paste);
		// MenuItem "Paste %Below"
		pasteBelow = new GuiMenuItem("Paste %Below", file);
		pasteBelow.setIcon(GuiUtil.makeIcon("../images/paste.gif"));
		this.getMainPanel().addAction(pasteBelow);
		// Separator
		file.addSeparator();
		// MenuItem "Node %up"
		nodeUp = new GuiMenuItem("Node %up", file);
		nodeUp.setIcon(GuiUtil.makeIcon("../images/arrow_up.gif"));
		nodeUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
		this.getMainPanel().addAction(nodeUp);
		// MenuItem "Node %down"
		nodeDown = new GuiMenuItem("Node %down", file);
		nodeDown.setIcon(GuiUtil.makeIcon("../images/arrow_down.gif"));
		nodeDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		this.getMainPanel().addAction(nodeDown);
		// MenuItem "Node %left"
		nodeLeft = new GuiMenuItem("Node %left", file);
		nodeLeft.setIcon(GuiUtil.makeIcon("../images/arrow_left.gif"));
		nodeLeft.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		this.getMainPanel().addAction(nodeLeft);
		// MenuItem "Node %right"
		nodeRight = new GuiMenuItem("Node %right", file);
		nodeRight.setIcon(GuiUtil.makeIcon("../images/arrow_right.gif"));
		nodeRight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		this.getMainPanel().addAction(nodeRight);
		// Separator
		file.addSeparator();
		// MenuItem "%Exit"
		exit = new GuiMenuItem("%Exit", file);
		this.getMainPanel().addAction(exit);
		// Menu %?
		menu2 = new GuiMenu("%?");
		menu2.setName("menu2");
		menuBar.add(menu2);
		// MenuItem "Help"
		help = new GuiMenuItem("Help", menu2);
		help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		this.getMainPanel().addAction(help);
		// MenuItem "%About Büro"
		aboutBuero = new GuiMenuItem("%About Büro", menu2);
		this.getMainPanel().addAction(aboutBuero);
		// Start Toolbar
		toolbar = new GuiToolbar();
		this.getRootPane().addToolBar(toolbar);
		// Tool "newTreeNode"
		tool = new GuiButton();
		tool.setToolTipText("Create New Node");
		tool.setIcon(GuiUtil.makeIcon("../images/new.gif"));
		tool.setActionCommand("newTreeNode");
		toolbar.addGuiTool(tool);
		this.getMainPanel().addAction(tool);
		// Separator
		toolbar.addSeparator();
		// Tool "<cmd>"
		tool3 = new GuiButton();
		tool3.setToolTipText("Load Tree from Disk");
		tool3.setIcon(GuiUtil.makeIcon("../images/open.gif"));
		toolbar.addGuiTool(tool3);
		this.getMainPanel().addAction(tool3);
		// Tool "<cmd>"
		tool4 = new GuiButton();
		tool4.setToolTipText("Save Tree to Disk");
		tool4.setIcon(GuiUtil.makeIcon("../images/save.gif"));
		toolbar.addGuiTool(tool4);
		this.getMainPanel().addAction(tool4);
		// Separator
		toolbar.addSeparator();
		// Tool "<cmd>"
		tool6 = new GuiButton();
		tool6.setToolTipText("Cut Node");
		tool6.setIcon(GuiUtil.makeIcon("../images/cut.gif"));
		toolbar.addGuiTool(tool6);
		this.getMainPanel().addAction(tool6);
		// Tool "<cmd>"
		tool7 = new GuiButton();
		tool7.setToolTipText("Copy Node");
		tool7.setIcon(GuiUtil.makeIcon("../images/copy.gif"));
		toolbar.addGuiTool(tool7);
		this.getMainPanel().addAction(tool7);
		// Tool "<cmd>"
		tool8 = new GuiButton();
		tool8.setToolTipText("Paste Node as Child Node");
		tool8.setIcon(GuiUtil.makeIcon("../images/paste.gif"));
		toolbar.addGuiTool(tool8);
		this.getMainPanel().addAction(tool8);
		// Tool "<cmd>"
		tool9 = new GuiButton();
		tool9.setToolTipText("Paste Node below current Node");
		tool9.setIcon(GuiUtil.makeIcon("../images/paste.gif"));
		toolbar.addGuiTool(tool9);
		this.getMainPanel().addAction(tool9);
		// Separator
		toolbar.addSeparator();
		// Tool "<cmd>"
		tool11 = new GuiButton();
		tool11.setIcon(GuiUtil.makeIcon("../images/arrow_up.gif"));
		toolbar.addGuiTool(tool11);
		this.getMainPanel().addAction(tool11);
		// Tool "<cmd>"
		tool12 = new GuiButton();
		tool12.setIcon(GuiUtil.makeIcon("../images/arrow_down.gif"));
		toolbar.addGuiTool(tool12);
		this.getMainPanel().addAction(tool12);
		// Tool "<cmd>"
		tool13 = new GuiButton();
		tool13.setIcon(GuiUtil.makeIcon("../images/arrow_left.gif"));
		toolbar.addGuiTool(tool13);
		this.getMainPanel().addAction(tool13);
		// Tool "<cmd>"
		tool14 = new GuiButton();
		tool14.setIcon(GuiUtil.makeIcon("../images/arrow_right.gif"));
		toolbar.addGuiTool(tool14);
		this.getMainPanel().addAction(tool14);
		// Begin Split split
		split = new GuiSplit();
		split.setName("split");
		mainPanel.add(split, new GridBagConstraints(0, 0, 1, 1, 1, 1
			,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// Tree Büro
		rootNode = new GuiTreeNode("Büro");
		buero = new GuiTree(rootNode);
		buero.setName("buero");
		buero.getTree().setEditable(!true);
		rootNode.setName("root");
		buero.setPreferredSize(new Dimension(180,100));
		split.add(buero, new GridBagConstraints(0, 0, 1, 1, 1, 1
			,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.getMainPanel().addMember(buero);
		// Node Adressen
		tmpNode = new GuiTreeNode("Adressen");
		rootNode.getMyTree().getGuiTreeModel().insertNodeInto(tmpNode, rootNode, rootNode.getChildCount());
		// Node Termine
		tmpNode = new GuiTreeNode("Termine");
		rootNode.getMyTree().getGuiTreeModel().insertNodeInto(tmpNode, rootNode, rootNode.getChildCount());
		// Node ToDo
		tmpNode = new GuiTreeNode("ToDo");
		rootNode.getMyTree().getGuiTreeModel().insertNodeInto(tmpNode, rootNode, rootNode.getChildCount());
		// Node Dokumente
		tmpNode = new GuiTreeNode("Dokumente");
		rootNode.getMyTree().getGuiTreeModel().insertNodeInto(tmpNode, rootNode, rootNode.getChildCount());
		// TreeElement Büro
		GuiTreeElement root = new GuiTreeElement("Büro", null);
		buero.addElement(root);
		// TreeNodeContent Adressen
		{
			GuiTreeContent adressen2 = new GuiTreeContent("Adressen");
			root.addContent(adressen2);
		}
		// TreeNodeContent Termine
		{
			GuiTreeContent termine2 = new GuiTreeContent("Termine");
			root.addContent(termine2);
		}
		// TreeNodeContent ToDo
		{
			GuiTreeContent toDo2 = new GuiTreeContent("ToDo");
			root.addContent(toDo2);
		}
		// TreeNodeContent Dokumente
		{
			GuiTreeContent dokumente2 = new GuiTreeContent("Dokumente");
			root.addContent(dokumente2);
		}
		// TreeNodeContent Wertelisten
		{
			GuiTreeContent wertelisten = new GuiTreeContent("Wertelisten");
			root.addContent(wertelisten);
		}
		// TreeElement Adressen
		GuiTreeElement adressen3 = new GuiTreeElement("Adressen", null);
		buero.addElement(adressen3);
		// TreeNodeContent Adresse
		{
			GuiTreeContent adresse = new GuiTreeContent("Adresse");
			adressen3.addContent(adresse);
		}
		// TreeElement Adresse
		GuiTreeElement adresse2 = new GuiTreeElement("Adresse", "AdressePanel.xml");
		buero.addElement(adresse2);
		// TreeNodeContent Personen
		{
			GuiTreeContent personen = new GuiTreeContent("Personen");
			adresse2.addContent(personen);
		}
		// TreeNodeContent Notizen
		{
			GuiTreeContent notizen = new GuiTreeContent("Notizen");
			adresse2.addContent(notizen);
		}
		// TreeElement Personen
		GuiTreeElement personen2 = new GuiTreeElement("Personen", "PersonenÜbersicht.xml");
		buero.addElement(personen2);
		// TreeNodeContent Person
		{
			GuiTreeContent person = new GuiTreeContent("Person");
			personen2.addContent(person);
		}
		// TreeElement Person
		GuiTreeElement person2 = new GuiTreeElement("Person", "PersonPanel.xml");
		buero.addElement(person2);
		// TreeNodeContent Notizen
		{
			GuiTreeContent notizen2 = new GuiTreeContent("Notizen");
			person2.addContent(notizen2);
		}
		// TreeElement Notizen
		GuiTreeElement notizen3 = new GuiTreeElement("Notizen", "NotizÜbersicht.xml");
		buero.addElement(notizen3);
		// TreeNodeContent Notiz
		{
			GuiTreeContent notiz = new GuiTreeContent("Notiz");
			notizen3.addContent(notiz);
		}
		// TreeElement Notiz
		GuiTreeElement notiz2 = new GuiTreeElement("Notiz", "NotizPanel.xml");
		buero.addElement(notiz2);
		// TreeElement Termine
		GuiTreeElement termine3 = new GuiTreeElement("Termine", null);
		buero.addElement(termine3);
		// TreeNodeContent Termin
		{
			GuiTreeContent termin = new GuiTreeContent("Termin");
			termine3.addContent(termin);
		}
		// TreeElement Termin
		GuiTreeElement termin2 = new GuiTreeElement("Termin", "TerminPanel.xml");
		buero.addElement(termin2);
		// TreeElement ToDo
		GuiTreeElement toDo3 = new GuiTreeElement("ToDo", null);
		buero.addElement(toDo3);
		// TreeNodeContent Notiz
		{
			GuiTreeContent notiz3 = new GuiTreeContent("Notiz");
			toDo3.addContent(notiz3);
		}
		// TreeElement Dokumente
		GuiTreeElement dokumente3 = new GuiTreeElement("Dokumente", null);
		buero.addElement(dokumente3);
		// TreeNodeContent Dokument
		{
			GuiTreeContent dokument = new GuiTreeContent("Dokument");
			dokumente3.addContent(dokument);
		}
		// TreeElement Dokument
		GuiTreeElement dokument2 = new GuiTreeElement("Dokument", "Dokument.xml");
		buero.addElement(dokument2);
		// TreeElement Wertelisten
		GuiTreeElement wertelisten2 = new GuiTreeElement("Wertelisten", null);
		buero.addElement(wertelisten2);
		// TreeNodeContent Anreden
		{
			GuiTreeContent anreden = new GuiTreeContent("Anreden");
			wertelisten2.addContent(anreden);
		}
		// TreeNodeContent Funktionen
		{
			GuiTreeContent funktionen = new GuiTreeContent("Funktionen");
			wertelisten2.addContent(funktionen);
		}
		// TreeElement Anreden
		GuiTreeElement anreden2 = new GuiTreeElement("Anreden", null);
		buero.addElement(anreden2);
		// TreeElement Funktionen
		GuiTreeElement funktionen2 = new GuiTreeElement("Funktionen", null);
		buero.addElement(funktionen2);
		// PopupMenu Tree
		tree = new GuiPopupMenu("Tree");
		tree.setName("tree");
		buero.setPopupMenu(tree);
		// MenuItem "New"
		v_new2 = new GuiMenuItem("New", tree);
		v_new2.setIcon(GuiUtil.makeIcon("../images/new.gif"));
		v_new2.setActionCommand("newTreeNode");
		this.getMainPanel().addAction(v_new2);
		// Separator
		tree.addSeparator();
		// MenuItem "Load"
		load2 = new GuiMenuItem("Load", tree);
		load2.setIcon(GuiUtil.makeIcon("../images/open.gif"));
		this.getMainPanel().addAction(load2);
		// MenuItem "Save"
		save2 = new GuiMenuItem("Save", tree);
		save2.setIcon(GuiUtil.makeIcon("../images/save.gif"));
		this.getMainPanel().addAction(save2);
		// Separator
		tree.addSeparator();
		// MenuItem "Cut"
		cut2 = new GuiMenuItem("Cut", tree);
		cut2.setIcon(GuiUtil.makeIcon("../images/cut.gif"));
		this.getMainPanel().addAction(cut2);
		// MenuItem "Copy"
		copy2 = new GuiMenuItem("Copy", tree);
		copy2.setIcon(GuiUtil.makeIcon("../images/copy.gif"));
		this.getMainPanel().addAction(copy2);
		// MenuItem "Paste as Child"
		pasteAsChild = new GuiMenuItem("Paste as Child", tree);
		pasteAsChild.setIcon(GuiUtil.makeIcon("../images/paste.gif"));
		this.getMainPanel().addAction(pasteAsChild);
		// MenuItem "Paste Below"
		pasteBelow2 = new GuiMenuItem("Paste Below", tree);
		pasteBelow2.setIcon(GuiUtil.makeIcon("../images/paste.gif"));
		this.getMainPanel().addAction(pasteBelow2);
		// Separator
		tree.addSeparator();
		// MenuItem "Node up"
		nodeUp2 = new GuiMenuItem("Node up", tree);
		nodeUp2.setIcon(GuiUtil.makeIcon("../images/arrow_up.gif"));
		this.getMainPanel().addAction(nodeUp2);
		// MenuItem "Node down"
		nodeDown2 = new GuiMenuItem("Node down", tree);
		nodeDown2.setIcon(GuiUtil.makeIcon("../images/arrow_down.gif"));
		this.getMainPanel().addAction(nodeDown2);
		// MenuItem "Node left"
		nodeLeft2 = new GuiMenuItem("Node left", tree);
		nodeLeft2.setIcon(GuiUtil.makeIcon("../images/arrow_left.gif"));
		this.getMainPanel().addAction(nodeLeft2);
		// MenuItem "Node right"
		nodeRight2 = new GuiMenuItem("Node right", tree);
		nodeRight2.setIcon(GuiUtil.makeIcon("../images/arrow_right.gif"));
		this.getMainPanel().addAction(nodeRight2);
	}
	/* Definition von Zugriffmethoden für die Daten haltenden Oberflächenkomponenten*/
	// %GetSet%
}
