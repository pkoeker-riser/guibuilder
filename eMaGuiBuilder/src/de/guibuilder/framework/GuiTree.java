package de.guibuilder.framework;

import java.awt.Component;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.jdataset.JDataSet;
import de.pkjs.util.Convert;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
/**
 * Implementierung einer Baum-Komponente.
 * @see GuiTreeNode
 * @see GuiTreeElement
 * @see GuiTreeContent
 */
public final class GuiTree extends GuiContainer /*GuiComponent*/ {

  // Attributes
  public static final int NODE_UP = 0;
  public static final int NODE_DOWN = 1;
  private JTree component = new JTree();
  /**
   * Das TreeModel dieses Baums.
   */
  private DefaultTreeModel treeModel;
  /**
   * Verweis auf SplitPanel für Navigator.
   */
  private transient GuiSplit split;
  /**
   * Derzeit selektierter Knoten.
   * Der Konstuktor setzt den Wurzelknoten auf selektiert.
   */
  private transient GuiTreeNode clickNode;
  /**
   * Der mit copy oder cut erzeugte Knoten für paste.
   */
  private transient GuiTreeNode copyNode;
  /**
   * Der Knoten, der vor einer Änderung den Focus hatte.
   */
  private transient GuiTreeNode lastNode;
  /**
   * Das der Node, bei dem zuletzt updateCurrentNode
   * aufgerufen wurde.
   */
  private transient GuiTreeNode lastUpdatedNode;
  /**
   * Eine HashMap mit der Liste der Elemente zu diesem Baum mit ihrem Inhalt als
   * Vector (Content).
   * @see GuiTreeElement
   */
  private LinkedHashMap<String, GuiTreeElement> elements;
  /**
   * Menge der Oberflächenpanels für Navigator; die Knoten selbst
   * halten die Daten für diese Panels.
   */
  private LinkedHashMap<String, GuiPanel> panels;
  /**
   * Der SelectionListener zu diesem Baum.
   */
  private transient TreeSelectionListener sl;
  private String msgNodeClick;
  private String msgCreate;
  // Drag-Drop
  private final static DragSourceListener dragSourceListener = new MyDragSourceListener();
  private DragSource dragSource = DragSource.getDefaultDragSource();
  //private DropTarget dropTarget;

  // Constructors
  /**
   * Erzeugt einen Tree mit einem initialen Wurzelknoten.<BR>
   * Der Wurzelknoten wird dabei auch mit dieser Tree-Komponente assoziiert,
   * um von einem Knoten wieder auf den Tree zugreifen zu können.
   * @see GuiTreeNode#getMyTree
   */
  public GuiTree(GuiTreeNode root) {
    super();
    treeModel = new DefaultTreeModel(root);
    component.setModel(treeModel);
    root.setMyTree(this);
    clickNode = root;
    lastNode = root;
    this.guiInit();
  }
  // Methods
  /**
   * Initialisiert die Komponente und den SelectionListener für die Nodes.
   */
  private void guiInit() {
    this.setName("tree");
    component.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    component.setEditable(true);
    component.setInvokesStopCellEditing(true); // Beenden des Editierens auch ohne Return
    component.setLargeModel(true); // Alle Knoten gleich hoch
    // Selection Listener
    sl = new GuiTreeSelectionAdapter(this);
    // Focus-Listener
    component.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        // nix
      }
      public void focusLost(FocusEvent e) {
        updateCurrentNode();
      }
    });
    component.addTreeSelectionListener(sl);
    component.putClientProperty("JTree.lineStyle", "Angled");
    component.setCellRenderer(new GuiTreeRenderer());
  }
  int getRowForLocation(int x, int y) {
  	return component.getClosestRowForLocation(x, y);
  }
  TreePath getPathForLocation(int x, int y) {
  	return component.getClosestPathForLocation(x, y);
  }
  //################
  @Override
	public final String getTag() {
    return "Tree";
  }
  /**
   * Liefert JTree
   */
  @Override
	public final JComponent getJComponent() {
    return component;
  }
  /**
   * Public wegen Code Generator
   */
  public final JTree getTree() {
    return component;
  }
  /**
   * Liefert GUI_TREE
   */
  @Override
	public final int getGuiType() {
    return GUI_TREE;
  }
  /**
   * Liefert TREE
   */
  public int getDataType() {
     return GuiComponent.TREE;
  }
  /**
   * Setzt das ActionCommand, daß bei der Selection eines anderen Knotens
   * geliefert wird.
   * @see de.guibuilder.framework.event.GuiTreeNodeSelectionEvent
   */
  public final void setMsgNodeClick(String s) {
    msgNodeClick = s;
  }
  /**
   * Liefert das ActionCommand, daß bei der Selection eines anderen Knotens
   * geliefert wird.
   * @see de.guibuilder.framework.event.GuiTreeNodeSelectionEvent
   */
  public final String getMsgNodeClick() {
    return msgNodeClick;
  }
  /**
   * @return Returns the msgCreate.
   */
  String getMsgCreate() {
	  return msgCreate;
  }
	
  /**
   * @param msgCreate The msgCreate to set.
   */
  void setMsgCreate(String msgCreate) {
	  this.msgCreate = msgCreate;
  }
  
  public GuiComponent getGuiComponent(String name) {
     GuiComponent comp = null;
     int p = name.indexOf('.');
     if (p != -1) {
        String nodename = name.substring(0,p-1);
        GuiTreeNode node = this.getGuiTreeNode(nodename);
        comp = node.getGuiComponent(name);
     } else {
        GuiTreeNode root = this.getRoot();
        if (root != null) {
           comp = root.getGuiComponent(name);
        }
     }
     return comp;
  }

	/**
	 * Setzt den Inhalt der Komponente auf den Inhalt des Models
	 * der über getElementPath erreichbar ist.
	 * Wird von GuiTable und GuiTree überschrieben.
	 * Voraussetzung ist, daß ein Attribut "element" spezifiziert ist.
	 */
	public final void setDatasetValues(JDataSet ds) {
		this.reset();
		//this.setModified(false);
		// Node und Folder, die fest definiert sind füllen.
		GuiTreeNode root = this.getRoot();
		root.setDatasetValues(ds);
	}

	public final void getDatasetValues(JDataSet ds) {
		GuiTreeNode root = this.getRoot();
		root.getDatasetValues(ds);
	}

	public final void commitChanges() {
		super.commitChanges();
		GuiTreeNode root = this.getRoot();
		root.commitChanges();
	}

  // DragDrop auskommentien?

  public void dragGestureRecognized(DragGestureEvent event) {
    TreePath path = component.getSelectionPath();
    if (path == null) {
      // Nothing selected, nothing to drag
      System.err.println ("Nothing selected - beep");
      this.component.getToolkit().beep();
    } else {
      GuiTreeNode selection =(GuiTreeNode)path.getLastPathComponent();
      //TransferableTreeNode node = new TransferableTreeNode(selection);
      dragSource.startDrag(
        event,
        DragSource.DefaultCopyDrop,
        selection,
        dragSourceListener);
    }
  }
  
  /**
   * Liefert den Wurzelknoten des Baums.
   */
  public GuiTreeNode getRoot() {
  	
	GuiTreeNode rootNode = null;
  	if(component.getModel() != null) {
  		rootNode = (GuiTreeNode)component.getModel().getRoot();
  	}
  	return rootNode;
  }
  /**
   * Fügt dem Baum einen Knoten hinzu.<br>
   * Der Knoten wird unterhalb des zuletzt selektierten Knotens eingefügt.
   * @see #setSelectedNode
   */
  public void addGuiNode(GuiTreeNode node) {
    GuiTreeNode parent = null;
    if (clickNode != null) {
      parent = clickNode;
    } else {
      parent = this.getRoot();
    }
    this.treeModel.insertNodeInto(node, parent, parent.getChildCount());
  }
  /**
   * Fügt einen Knoten hinzu, dessen Beschriftung auch seinem Namen entspricht.
   */
  public GuiTreeNode addGuiNode(String title) {
    GuiTreeNode node = new GuiTreeNode(title);
    this.addGuiNode(node);
    return node;
  }
  /**
   * Fügt einen Knoten hinzu, der zu seiner Beschriftung einen abweichenden Namen trägt.
   */
  public GuiTreeNode addGuiNode(String title, String name) {
    GuiTreeNode node = new GuiTreeNode(title, name);
    this.addGuiNode(node);
    return node;
  }
  /**
   * Fügt dem Navigator eine neues Oberflächenpanel hinzu.
   * @see GuiTreeNode#getPanel
   */
  void addPanel(String fileName, GuiPanel panel) {
    // Das Problem ist, daß getRootPane bei nicht sichtbaren
    // Panels null liefert...
    if (panels == null) {
      panels = new LinkedHashMap<String, GuiPanel>();
    }
    panels.put(fileName, panel);
    panel.setMyTree(this);
  }
  /**
   * Liefert das Panel unter dem angegebenen Namen oder null, wenn keines
   * gefunden wurde.
   */
  public GuiPanel getPanel(String fileName) {
    if (panels == null) {
      return null;
    } else {
      return (GuiPanel)panels.get(fileName);
    }
  }
  /**
   * Setzt den selektierten Node neu.
   * @param name Angabe in Punktnotation "root.myFolder.myNode" mit den
   * Namen der jeweiligen (Zwischen-)Knoten.
   * Wenn kein "." enthalten, dann wird unterhalb des
   * selectedNode nach Unterknoten gesucht.
   */
  public void setSelectedNode(String name) {
    int cnt = 0;
    String tok;
    // Relativ suchen=
    if (clickNode != null && name.indexOf(".") == -1) {
      GuiTreeNode child = clickNode.getChildByName(name);
      if (child != null) {
        this.setSelectedNode(child);
        return;
      }
    }
    GuiTreeNode currentNode = this.getRoot();

    StringTokenizer st = new StringTokenizer(name, ".");
    GuiTreeNode[] nodes = new GuiTreeNode[st.countTokens()];
    while (st.hasMoreTokens()) {
      tok = st.nextToken();
      //System.out.println(tok);
      if (cnt == 0) {
        // nix
      } else {
        currentNode = currentNode.getChildByName(tok);
      }
      nodes[cnt] = currentNode;
      cnt++;
    }
    TreePath path = new TreePath(nodes);
    component.setSelectionPath(path);
  }
  public void setSelectedNode(GuiTreeNode node) {
    TreeNode[] nodes = node.getPath();
    TreePath path = new TreePath(nodes);
    component.setSelectionPath(path);
  }
  /**
   * Liefert den vom Benutzer selektierten Knoten.
   */
  public GuiTreeNode getSelectedNode() {
    GuiTreeNode ret = null;
    TreePath path = component.getSelectionPath();
    if (path != null) {
      ret =(GuiTreeNode)path.getLastPathComponent();
    } else { // Root liefern, wenn nix selektiert?
      ret = this.getRoot();
    }
    return ret;
  }
  /**
   * Bei LostFocus müssen die XML-Daten des Nodes fortgeschrieben werden.
   */
  private boolean updateCurrentNode() {
  	boolean ret = true;
    GuiPanel lastPanel = lastNode.getPanel();
    if (lastPanel != null && this.getRootPane() != null) {
    	// GuiTable#stopCellEditing muß leider mit Gewalt erzwungen werden.
    	GuiTable tbl = this.getRootPane().getCurrentTable();
    	if (tbl != null ) {
    		tbl.stopCellEditing(); 
    	}
    	// Daten aus Panel nach Node
    	lastNode.setAllValuesXml(lastPanel.getAllValuesXml());
    	try {
    		lastPanel.verify();
				lastUpdatedNode = lastNode;
    	} catch (Exception ex) {
    		ret = false; 
    		this.setSelectedNode(lastNode);
    	}
    }
    return ret;
  }
  /**
   * Liefert das Model zum Tree.
   */
  public DefaultTreeModel getGuiTreeModel() {
    return this.treeModel;
  }
  /**
   * Liefert den TreeNode vor(!) einem Fokus-Wechsel.<p>
   */
  public GuiTreeNode getLastNode() {
    return lastUpdatedNode;
  }
  /**
   * Setzt den selektierten Knoten neu.
   * Wird von GuiTreeSelectionAdapter aufgerufen.<p>
   * für das Panel, welches den Fokus hat, wird verify aufgerrufen.
   * Wenn dieses schief geht (NotNull, Datum, ...) behält
   * das alte Panel den Fokus.
   * @see GuiTreeSelectionAdapter
   */
  public void valueChanged(TreePath path) {
    // Reihenfolge der Events:
    // 1 Tree: ValueChanged
  	// 2 TreeNode: Mouse Pressed
    // 3 Element (from Panel): LostFocus
    // 4 Changed
    // Alte Knoten: Daten retten
   	if (this.updateCurrentNode()) {
	   	// Neuer Knoten
	    if (path != null) { // Kann das jemals sein??
	      clickNode = (GuiTreeNode)path.getLastPathComponent();
	      if (clickNode.getFileName() != null && split != null) {
	        // hier wird das Panel erzeugt, falls noch nicht vorhanden
	        GuiPanel panel = clickNode.getPanel();
	        if (panel != null) {
	          // 23.2.2002
	          // Reihenfolge geändert:
	          // Erst Panel in Split einhängen,
	          // dann reset und setAllValues aufrufen
	          // sonst kennt das Panel in der Zwischenzeit kein RootPane!
	          // 14.4.2002: Wenn gleiches Panel, dann nicht mehr verstecken/setzen.
	          if (lastNode == null || lastNode.getFileName() == null
	              || lastNode.getFileName().equals(clickNode.getFileName()) == false) {
	            panel.setVisible(false); // neu: verstecken
	            split.setRightComponent(panel); // hier richtig!
	          }
	          panel.reset(); // muß sein, da getAllValues nur die ausgefüllten Felder liefert!
	          Document docVals = clickNode.getAllValuesXml();
	          panel.setAllValuesXml(docVals);
	          panel.setVisible(true); // neu: anzeigen
	          lastNode = clickNode;
	          //System.out.println("Set lastNode: "+lastNode.getTitle());		//##debug
	        }
	      }
	      // Meldung an Controller
	      if (clickNode.getMsgNodeClick() != null && getRootPane() != null) {
	        getRootPane().obj_TreeNodeSelected(this, clickNode.getMsgNodeClick(), clickNode);
	      }
	    }
   	}
  }
  /**
   * Setzt das SplitPanel für den Einsatz dieses Trees im Navigator.
   * @see GuiFactory#perfBeginTree
   */
  void setSplit(GuiSplit split) {
    this.split = split;
  }
  /**
   * Liefert das ggf. vorhandene Split Panel zu diesem Tree
   * oder null, wenn keins vorhanden.<p>
   * für Navigator Pattern.
   * @return
   */
  public GuiSplit getSplit() {
  	return split;
  }
  /**
   * Setzt das TreeModel.
   * @param root Wurzelknoten des Baums
   */
  public void setModel(GuiTreeNode root) {
    component.removeTreeSelectionListener(sl);
    treeModel = new DefaultTreeModel(root);
    component.setModel(treeModel);
    root.setMyTree(this);
    component.expandPath(new TreePath(root.getPath()));
    component.addTreeSelectionListener(sl);
  }
  /**
   * Liefert die HashMap der GuiTreeElemente zum Tree.
   * @see #addElement
   * @see GuiTreeElement
   */
  public LinkedHashMap<String, GuiTreeElement> getElements() {
    return elements;
  }
  /**
   * Liefert ein TreeElement unter Angabe seines Namens.
   */
  public GuiTreeElement getGuiTreeElement(String name) {
    return this.getElements().get(name);
  }
  /**
   * Setzt die HashMap der GuiTreeElemente zum Tree.
   * @see GuiTreeElement
   */
  public void setElements(LinkedHashMap<String, GuiTreeElement> hash) {
    this.elements = hash;
  }
  /**
   * Wird von der Factory bei "<Element ..." aufgerufen.
   */
  public void addElement(GuiTreeElement ele) {
    if (elements == null) elements = new LinkedHashMap<String, GuiTreeElement>();
    this.elements.put(ele.getName(), ele);
    GuiTreeNode root = this.getRoot();
    if (root.getName().equals(ele.getName())) {
      root.setFileName(ele.getFileName());
    }
  }
  /**
   * Erzeugt einen neuen Knoten im Baum unterhalb des aktivierten Knotens.
   * Es wird ein Dialog aufgerufen, in dem der gewünschte Knotentyp ausgewählt
   * werden kann (wenn mit den Keywords "Element" und "Content" definiert).
   * @return Der neu angelegte Node oder null.
   */
  public GuiTreeNode createNode(GuiTreeNode node) {
    GuiTreeNode newNode = null;
    Vector<Object> v = node.getPossibleChildNodeNames();
    // Nur wenn Elemente definiert sind, dann auch Auswahl anbieten!
    if (v != null) {
      String xs =
        "<?xml version='1.0' encoding='UTF-8' ?>" +
        "<!DOCTYPE GDL SYSTEM 'gdl.dtd'>" +
        "<GDL>" +
          "<Dialog label='Knoten einfügen' type='MODAL' w='400' h='400' >" +
          "<Group type='LOW' w='3'>" +
            "<List label='Knotentyp:' />" +
            "<Text label='Bezeichnung:' it='10' ib='5' />" +
          "</Group>" +
          "<xFiller eol='false'/>" +
          "<Button label='OK' it='10' ib='10' px='40' file='Close()' eol='false'/>" +
          "<Button label='Abbrechen' it='10' ib='10' file='Cancel()'/>" +
        "</Dialog>" +
        "</GDL>";
      GuiDialog dia = null;
      try {
        dia = (GuiDialog)GuiFactory.getInstance().createWindowXml(xs);
      } catch (GDLParseException ex) {
        GuiUtil.showEx(ex);
        return null;
      }
      // Bezeichnung initialisieren
      dia.setValue("bezeichnung", "<neu>");
      // Combobox füllen
      GuiList cmb = null;
      cmb = (GuiList)dia.getGuiComponent("knotentyp");
      cmb.setItems(v);
      cmb.setSelectedIndex(0);
      // Dialog anzeigen
      if (dia.showDialog()) { // Nur, wenn OK-Button betötigt...
        String bez = (String)dia.getValue("bezeichnung");
        String cmbValue = (String)dia.getValue("knotentyp");
        if (bez.equals("<neu>") && cmbValue != null) {
          bez = cmbValue;
        }
        newNode = new GuiTreeNode(bez);
        if (cmbValue != null) {
          newNode.setGuiTreeElementName(cmbValue);
	        GuiTreeElement newEle = elements.get(cmbValue);
	        if (newEle == null) {
	          JOptionPane.showMessageDialog(this.component, "Content: \"" + cmbValue + "\" missing Element!",
	            "Tree Element Definition Error", JOptionPane.ERROR_MESSAGE);
	        }
	        else {
	          newNode.setFileName(newEle.getFileName());
	          newNode.setIconName(newEle.getIconName());
	          newNode.setElementName(newEle.getElementName());
	          newNode.setPopupMenu(newEle.getPopupMenu());
	          newNode.setMsgNodeClick(newEle.getMsgNodeClick());
	        }
        }
        //##lastNode = newNode; // OK? (sonst funktioniert valueChanged nicht bei neuen Knoten!?
        treeModel.insertNodeInto(newNode, node, node.getChildCount());
        // Erzwingt XML-Daten-Initialisierung
        newNode.getPanel();
        //System.out.println(newNode.getAllValuesXml());
        // Create mandatory Child Nodes
        newNode.createMandatoryChildNodes();
        int row = component.getMinSelectionRow();
        if (row != -1) {
          component.expandRow(row);
        }
      } // End if Dia
    }
    return newNode;
  }
  /**
   * Erzeugt einen Node unterhalb des selektierten Nodes.<p>
   * Eingebaute Methode NewNode()
   */
  public GuiTreeNode createNode() {
    return createNode(this.clickNode);
  }
  /**
   * Erzeugt einen Enkel-Node unterhalb des Nodes, dessen
   * Name unterhalb des selektierten Nodes angegeben ist.
   */
  public GuiTreeNode createNode(String nodeName) {
    if (nodeName == null || nodeName.length() == 0) {
      return createNode();
    } else {
      return createNode(clickNode.getChildByName(nodeName));
    }
  }
  /**
   * Kopiert den selektierten Knoten.
   * @see #pasteNode
   */
  public void copyNode() {
    copyNode = clickNode.guiClone();
  }
  /**
   * Schneidet den selektierten Knoten aus.
   * @see #pasteNode
   */
  public void cutNode() {
    if (clickNode.isRoot() == false) {
      // Alte Daten retten
      GuiPanel lastPanel = lastNode.getPanel();
      if (lastPanel != null) {
        lastNode.setAllValuesXml(lastPanel.getAllValuesXml());
      }
      copyNode = clickNode;
      treeModel.removeNodeFromParent(clickNode);
    }
  }
  /**
   * Schiebt den selektierten Node um eins nach oben.<p>
   * Macht nichts, wenn der selektierte Knoten die Wurzel oder der erste Knoten ist.
   */
  public void nodeUp() {
    if (clickNode.isRoot() == false && clickNode.getGuiParentNode().getChildBefore(clickNode) != null) {
      TreePath path = component.getSelectionPath();
      int row = component.getRowForPath(path);
      this.cutNode();
      component.setSelectionRow(row - 1);
      this.pasteNode(NODE_UP);
      component.setSelectionRow(row - 1);
    }
  }
  /**
   * Schiebt den selektierten Node um eins nach unten.
   * Macht nichts, wenn der selektierte Knoten die Wurzel oder der letzte Knoten ist.
   */
  public void nodeDown() {
    if (clickNode.isRoot() == false && clickNode.getGuiParentNode().getChildAfter(clickNode) != null) {
      TreePath path = component.getSelectionPath();
      int row = component.getRowForPath(path);
      this.cutNode();
      component.setSelectionRow(row);
      this.pasteNode(NODE_DOWN);
      component.setSelectionRow(row + 1);
    }
  }
  /**
   * Verschiebt den selektierten Knoten eine Ebene höher.<p>
   * Es ist dabei nicht möglich, den Knoten zum Wurzelknoten zu machen.
   */
  public void nodeLeft() {
    if (clickNode.getGuiParentNode().isRoot() == false && clickNode.getGuiParentNode().isRoot() == false) {
      GuiTreeNode newParent = clickNode.getGuiParentNode().getGuiParentNode();
      this.cutNode();
      this.setSelectedNode(newParent);
      this.pasteNode();
    }
  }
  /**
   * Verschiebt den selektierten Knoten eine Ebene tiefer;
   * d.h. der Knoten oberhalb wird sein neuer Parent.
   */
  public void nodeRight() {
    if (clickNode.getPreviousSibling() != null) {
      GuiTreeNode newParent = (GuiTreeNode)clickNode.getPreviousSibling();
      this.cutNode();
      this.setSelectedNode(newParent);
      this.pasteNode();
    }
  }
  /**
   * Fügt einen kopierten oder ausgeschnittenen Knoten als ChildNode des selektierten Knoten ein.
   * @see #copyNode
   * @see #cutNode
   */
  public void pasteNode() {
    if (copyNode != null) {
      treeModel.insertNodeInto(copyNode.guiClone(), clickNode, clickNode.getChildCount());
    }
  }
  /**
   * Fügt einen kopierten oder ausgeschnittenen Knoten oberhalb oder unterhalb des selektierten Knoten ein.
   * @param offset NODE_UP oder NODE_DOWN
   */
  public void pasteNode(int offset) {
    if (copyNode != null && clickNode != null && clickNode.getGuiParentNode() != null) {
      treeModel.insertNodeInto(copyNode, clickNode.getGuiParentNode(), clickNode.getParent().getIndex(clickNode) + offset);
    }
  }
  public final GuiContainer getContainer(String name) {
     GuiTreeNode node = this.getGuiTreeNode(name);
     if (node == null) 
        return null;     
     GuiPanel panel = node.getPanel();
     return panel;
  }
  /**
   * Liefert den Member eines zu einem Knoten gehörenden Panels.<P>
   * root#myFolder#myNode#myTab.myName
   */
  GuiMember getGuiMember(String name) {
    String memberName = null;
    GuiTreeNode currentNode = null;
    int cnt = 0;

    StringTokenizer tokens = new StringTokenizer(name, "#");
    int anz = tokens.countTokens(); // Anzahl Tokens
    // Nodes abarbeiten
    String tok;
    while (tokens.hasMoreTokens()) {
      tok = tokens.nextToken();
      cnt++;
      // Erste Knoten: Wurzel
      if (cnt == 1) {
        currentNode = this.getRoot();
      // Zwischenknoten
      } else if (cnt < anz) {
        // Rekursiv Container-Schachtelung abarbeiten.
        currentNode = currentNode.getChildByName(tok);
      // Letzter Knoten (ist der mit dem Panel!)
      } else {
        memberName = tok;
      }
    } // End While
    GuiContainer cont = currentNode.getPanel();
    GuiMember member = cont.getMember(memberName);
    return member;
  }
  /**
   * Liefert den Knoten unter Angabe seines Pfades<P>
   * root.myFolder.myNode
   */
  public GuiTreeNode getGuiTreeNode(String path) {
    GuiTreeNode currentNode = this.getRoot();

    StringTokenizer tokens = new StringTokenizer(path, ".");
    // Nodes abarbeiten
    while (tokens.hasMoreTokens()) {
      String tok = tokens.nextToken();
     // Rekursiv Container-Schachtelung abarbeiten.
     currentNode = currentNode.getChildByName(tok);
    } // End While
    return currentNode;
  }
  /**
   * TODO : Not Implemented!
   */  
	public void setValue(Object value) {
	   //this.setModified(false);
	}
  

	public Object getUnformatedValue() {
	   return getValue();
	}
  
  /**
   * TODO : Not Implemented!
   */
 
	public Object getValue() {
    return null;
  }
  /**
   * TODO : Not Implemented!<br>
   * Die Frage besteht darin, ob alle? Konten des Baums bei "reset" gelöscht werden sollen.
   */
  @Override
	public void reset() {
     //this.setModified(false);
     }
  /**
   * Speichert einen Navigator-Baum in einem XML-Dokument.
   */
  public Document getAllValuesXml() {
    // ggf. geänderte Daten des selektierten Knoten retten
    GuiPanel lastPanel = lastNode.getPanel();
    if (lastPanel != null) {
      lastNode.setAllValuesXml(lastPanel.getAllValuesXml());
    }
    // XML
    Document doc = new Document();
    Element tree = doc.setRoot("Tree");
    tree.setAttribute("name", this.getName());
    tree.setAttribute("type", GuiComponent.getDataTypeName(this.getDataType()));
    if (this.getOid() != -1) {
      tree.setAttribute(OID, Long.toString(this.getOid()));
    }
    GuiTreeNode treeNode = this.getRoot();
    this.xmlLoopNode(tree, treeNode);
    return doc;
  }
  /**
   * Füllt den Baum mit Knoten aus einem XML-Document.
   * @throws IllegalArgumentException wenn NodeName != "Tree"
   */
  public void setAllValuesXml(Element root) {
    if (root.getName().equals("Tree") == false) {
      throw new IllegalArgumentException("NodeName != Tree: "+root.getName());
    }
    // oid
    final String oid = root.getAttributeValue(OID);
    if ( oid != null) {
      this.setOid(Convert.toLong(oid));
    }
    importNodes(root, null);
    treeModel.reload(); // jetzt funktioniert auch add!!!
  }
  /**
   * Importiert eine Menge von Nodes aus einem Xml-Dokument
   * unterhalb des angegebenen Parents.
   */
  public void importNodesFromXml(Document doc, GuiTreeNode parent) {
    Element root = doc.getRoot();
    importNodes(root, parent);
    treeModel.reload(); // wichtig wegen add
  }
  /**
   * @see #importNodesFromXml
   * @see #setAllValuesXml
   */
  private void importNodes(Element root, GuiTreeNode parent) {
    Elements nl = root.getElements();
    Element ele;
    GuiTreeNode guiTreeNode;
    String title = null;
    String name = null;
    String _elementName = null;
    String typ = null;

    while (nl.hasMoreElements()) {
      ele = nl.next();
      if (ele.getName().equals("Node")) {
        title = ele.getAttributeValue("title");
        name = ele.getAttributeValue("name");
        _elementName = ele.getAttributeValue("element");
        typ = ele.getAttributeValue("typ");

        guiTreeNode = new GuiTreeNode(title, name);
        // oid
        final String oid = ele.getAttributeValue(OID);
        if ( oid != null) {
          guiTreeNode.setOid(Convert.toLong(oid));
        }
        if (_elementName != null) {
          guiTreeNode.setGuiTreeElementName(_elementName);
        }
        // Zuerst prüfen, ob ElementName vorhanden
        if (_elementName != null) {
          GuiTreeElement tele = this.getGuiTreeElement(_elementName);
          if (tele != null) {
            guiTreeNode.setFileName(tele.getFileName());
          }
        } else if (typ != null) { // erst jetzt nach Typ fragen
          guiTreeNode.setFileName(typ);
        }

        if (parent == null) {
          this.setModel(guiTreeNode);
          parent = guiTreeNode;
        } else {
          parent.add(guiTreeNode); // funktioniert mit anschließendem reload() !!
          GuiTreeElement treeEle = guiTreeNode.getGuiTreeElement();
          if (treeEle != null) {
            guiTreeNode.setIconName(treeEle.getIconName());
          }
        }
        importNodes(ele, guiTreeNode);
      }
      else if (ele.getName().equals("Panel")) {
        parent.setAllValuesXml(ele);
      }
    }
  }
  /**
   * Recursive Routine zur XML-Speicherung eines Knotens im Baum.
   * Der XML-Tag wird aus dem Attribute xmlTag der Klasse GuiNode gebildet.
   * Die Bezeichnung eines Knotens wird im Attribut "title=" abgelegt.
   * Ist einem Konten ein Panel zugeordnet, werden die Attributen "name=[Name des Panels]" und
   * "type=[Spezifikationsdatei für Oberfläche]" versorgt.
   */
  private void xmlLoopNode(Element parent, GuiTreeNode treeNode) {
    Element ele = parent.addElement("Node");
    ele.setAttribute("title", treeNode.getTitle());
    ele.setAttribute("name", treeNode.getName());
    ele.setAttribute("element", treeNode.getGuiTreeElementName());
    // oid
    if (treeNode.getOid() != -1) {
      ele.setAttribute(OID, Long.toString(treeNode.getOid()));
    }
    if (treeNode.getPanel() != null) {
      GuiPanel panel = treeNode.getPanel();
      if (panel.getMembers() != null) {
        //ele.setAttribute("name", panel.getName());
        ele.setAttribute("typ", treeNode.getFileName());
        Document childDoc = treeNode.getAllValuesXml();
        if (childDoc != null) {
          // Wurzel umhängen
          Element childRoot = (Element)childDoc.getRoot().clone();
          if (childRoot != null) {
            //doc.changeNodeOwner(childRoot);
            ele.addElement(childRoot);
          }
        }
      }
    }
    // Unterknoten abarbeiten.
    for (Enumeration e = treeNode.children() ; e.hasMoreElements() ;) {
      GuiTreeNode child = (GuiTreeNode)e.nextElement();
      this.xmlLoopNode(ele, child);
    }
  }
  /**
   * CellRenderer
   */
  TreeCellRenderer getCellRenderer() {
    return component.getCellRenderer();
  }

  //** Inner Classes *******************************************************
  private static class MyDragSourceListener implements DragSourceListener {
    public void dragDropEnd(DragSourceDropEvent event) {
      //System.out.println("dragDropEnd");
    }
    public void dragEnter(DragSourceDragEvent event) {
      //System.out.println("dragEnter");
    }
    public void dragExit(DragSourceEvent event) {
      //System.out.println("dragExit");
    }
    public void dragOver(DragSourceDragEvent event) {
      //System.out.println("dragOver");
    }
    public void dropActionChanged(DragSourceDragEvent event) {
      //System.out.println("dropActionChanged");
      //System.out.println(event.getDropAction());
    }
  }
  //****** Inner Classes *******************************************************
  /**
   * Ein SelectionAdapter für die Tree-Komponente.
   * Horcht auf Änderungen an dem selektierten Knoten.
   * @see GuiTree#valueChanged
   */
  private static final class GuiTreeSelectionAdapter implements TreeSelectionListener {
    // Attributes
    private GuiTree myTree;
    // Constructor
    /**
     * Der abzuhörende Baum ist zu übergeben.
     */
    GuiTreeSelectionAdapter(GuiTree tree) {
      this.myTree = tree;
    }
    /**
     * Wird aufgerufen, wenn ein anderer Knoten selektiert wurde (Maus oder Tastatur).
     */
    public void valueChanged(TreeSelectionEvent e) {
      TreePath path = e.getNewLeadSelectionPath();
      //##System.out.println(path); // Ist auch null, wenn Ctrl gedrückt!
      this.myTree.valueChanged(path);
    }
  } // End Of GuiTreeSelectionAdapter
  /*
  TreeCellRenderer getCellRenderer() {
    return component.getCellRenderer();
  }
  */
  //**********************************************************************
  /**
   * Renderer für Nodes in Tree.
   */
  private final class GuiTreeRenderer extends DefaultTreeCellRenderer {
    /**
     * HashMap für alle verwendeten Icons.
     */
    private LinkedHashMap<String, Icon> icons = new LinkedHashMap<String, Icon>();
    // Method
    /**
     * Es wird eine HashMap mit Icons gepflegt.
     * @see GuiUtil#makeIcon
     * @see GuiTreeNode#setIconName
     */
    @Override
		public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean _hasFocus) {
      super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        _hasFocus);
      GuiTreeNode node = (GuiTreeNode)value;
      String iconName = node.getIconName();
      if (iconName != null) {
        Icon icon = icons.get(iconName);
        if (icon == null) {
          icon = GuiUtil.makeIcon(iconName);
          icons.put(iconName, icon);
        }
        setIcon(icon);
      } // End if null
      return this;
    } // End getTreeCellRendererComponent
  }
@Override
public int getLayoutManager() {
   // TODO Auto-generated method stub
   return 0;
}
@Override
public void setLayoutManager(int lm) {
   // TODO Auto-generated method stub
   
}
@Override
void removeAll() {
   // TODO Auto-generated method stub
   
}
} // End Of GuiTree
