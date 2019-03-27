package de.guibuilder.framework;

import java.awt.Component;
import java.awt.IllegalComponentStateException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import de.guibuilder.framework.event.GuiPopupEvent;
import de.jdataset.JDataSet;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.XPath;

/**
 * Implementierung eines Knotens in einer Baum-Komponente.
 * <p>
 * Dem Knoten kann für das Navigator-Pattern eine relative URL auf eine gültige
 * Gui-Spezifikations zugewiesen werden. <br>
 * {@link #setFileName}<br>
 * Dort darf nur ein Child Container spezifiziert werden (also zumeist ein Panel). <br>
 * {@link GuiFactory#createPanel}<br>
 * Die Tree-Komponente hält Referenzen auf alle Panels der Knoten. <br>
 * Der Knoten selbst hält die jeweiligen Werte zu seinem Panel. <br>
 * {@link #getAllValuesXml}
 * <p>
 * Wechselt der Benutzer von einem Knoten zum anderen, so werden diese Werte dem Panel
 * jeweils zugewiesen. {@link GuiTree#valueChanged}
 * <p>
 * Die vom Knoten gehaltenen Werte können mit {@link #getValue}und {@link #setValue}
 * ausgelesen und geändert werden.
 */
public final class GuiTreeNode extends DefaultMutableTreeNode implements MemberPopupAble,
		IDatasetComponent, Transferable {
   private static final long serialVersionUID = 1L;
	// Attributes
	/**
	 * Name des Knotens; default ist sein Titel.
	 */
	private String name;
	private boolean inserted = false;
	/**
	 * Verweist auf das laufende Element im DataSet
	 */
	private int modelElementNumber = -1;
	/**
	 * Verweis auf den Typ des Knotens --> Name von GuiTreeElement
	 */
	private String guiTreeElementName;
	/**
	 * Dateiname der Gui-Spezifikation zu diesem Knoten.
	 */
	private String fileName;
	private Document guiDoc = null;
	/**
	 * Daten des Panels.
	 */
	private Document xmlValues;
	/**
	 * Menge der Gui-Spezifikationen, die für diesen Folder erlaubt sind. Diese Menge wird
	 * bei der Erstellung eines neuen Knotens zur Auswahl angeboten. <br>
	 * Wird von der Factory versorgt; siehe KeyWord "Content". <br>
	 * Siehe auch die Standard-Methode "NewNode()".
	 */
	private Vector<Object> files;
	/**
	 * Verweis auf die Tree-Komponete dieses Knotens.
	 */
	private transient GuiTree myTree;
	/**
	 * Dateiname des individuellen Icons für diesen Node.
	 */
	private String iconName;
	private long oid = -1;
	private Object userObject2;
	private Object controller;
	private String msgNodeClick;
	// Binding
	private String elementName;
	// POPUP
	private GuiPopupMenu popupMenu;
	private String msgPopup;
	//private boolean hasMouseListener;

	public final static DataFlavor GUI_TREENODE_FLAVOR = new DataFlavor(
			DefaultMutableTreeNode.class, "Default Mutable Tree Node");

	private static DataFlavor flavors[] = { GUI_TREENODE_FLAVOR };
//	private final static int TREE = 0;
//	private final static int STRING = 1;
//	private final static int PLAIN_TEXT = 2;

	private boolean readonly; // getDataSetValue wird so abgeschaltet (ro="y")
	public void setReadonly(boolean b) {
	   this.readonly = b;
	}
	public boolean isReadonly() {
	   return readonly;
	}
	// Constructor
	/**
	 * Erzeugt einen neuen Ast für einen Baum.
	 * 
	 * @param title
	 *           Beschriftung des Knotens; wird gleichzeitig als Name und ElementName
	 *           verwendet.
	 */
	public GuiTreeNode(String title) {
		super(title);
		this.setName(title);
		this.setGuiTreeElementName(title);
	}

	/**
	 * Erzeugt einen neuen Knoten mit einer Beschriftung und einem davon abweichenden
	 * Namen.
	 * 
	 * @param title
	 *           Beschriftung des Knotens
	 * @param name
	 *           Name des Knotens; wird gleichzeitig als Element verwendet.
	 */
	public GuiTreeNode(String title, String name) {
		super(title);
		this.setName(name);
		this.setGuiTreeElementName(name);
	}

	/**
	 * Erzeugt einen neuen Knoten mit einer Beschriftung, einem davon abweichenden Namen
	 * und definiert den KnotenTyp (Name eine GuiTreeElements).
	 * 
	 * @param title
	 *           Beschriftung des Knotens
	 * @param name
	 *           Name des Knotens
	 * @param element
	 *           Name des GuiTreeElements
	 */
	public GuiTreeNode(String title, String name, String guiTreeElementName) {
		super(title);
		this.setName(name);
		this.setGuiTreeElementName(guiTreeElementName);
	}

	// Methods
	/**
	 * Fügt einen GuiTreeNode als Child Node an letzer Position hinzu. Im Unterschied zu
	 * add von DefaultMutableTreeNode wird bei dieser Method der neue Node auch gleich
	 * angezeigt.
	 */
	public void add(GuiTreeNode node) { // New 10.8.2003 PKÖ
		if (this.getMyTree() == null) {
			super.add(node);
		} else {
			this.getMyTree().getGuiTreeModel().insertNodeInto(node, this,
					this.getChildCount());
		}
	}

	/**
	 * Löscht den angegebenen ChildNode
	 * 
	 * @param child
	 */
	public void remove(GuiTreeNode child) {
		if (this.getMyTree() == null) {
			super.remove(child);
		} else {
			this.getMyTree().getGuiTreeModel().removeNodeFromParent(child);
		}
	}

	public final String getTag() {
		return "Folder";
	}

	public final String getGuiTreeElementName() {
		return guiTreeElementName;
	}

	public final GuiTreeElement getGuiTreeElement() {
		GuiTreeElement ele = getMyTree().getGuiTreeElement(getGuiTreeElementName());
		return ele;
	}

	/**
	 * Setzt den Namen des TreeElements
	 * 
	 * @see GuiTreeElement
	 */
	public final void setGuiTreeElementName(String name) {
		guiTreeElementName = name;
	}

	// Binding
	public void setElementName(String name) {
		this.elementName = name;
	}

	public String getElementName() {
		return this.elementName;
	}

	/**
	 * Liefert rekursiv den Path zum Element des DataSet
	 */
	public String getElementPath(String current) {
		// Element
		if (this.elementName != null) {
			current = elementName + current;
		}
		// Parents abarbeiten
		GuiTreeNode parentNode = this.getGuiParentNode();
		if (parentNode != null) {
			current = parentNode.getElementPath(current);
		} else {
			current = this.getMyTree().getElementPath(current);
		}
		System.out.println(current);
		return current;
	}

	public final void getDatasetValues(JDataSet ds) {
		for (int i = 0; i < this.getChildCount(); i++) {
		   GuiTreeNode node = (GuiTreeNode) this.getChildAt(i);
		   node.getDatasetValues(ds);
		}
		// Daten aus Panel
		if (this.isReadonly()) { // do="y"
		   // nix machen
		} else {
           GuiPanel panel = this.getPanel(); 
           if (panel != null) { 
              panel.getDatasetValues(ds);
              this.xmlValues = panel.getAllValuesXml(); // frisch zuweisen
           }  
		}
	}

	/**
	 * Setzt den Inhalt der Komponente auf den Inhalt des Models der über getElementPath
	 * erreichbar ist. Wird von GuiTable und GuiTree überschrieben. Voraussetzung ist, daß
	 * ein Attribut "element" spezifiziert ist.
	 * Billiglösung: Erstmal nur einen statischen Baum versorgen (je Node ein Panel, welches mit file=[filename.xml] definiert ist)
	 */
	public final void setDatasetValues(JDataSet ds) {		
	   // Rekursion Child Nodes
		for (int i = 0; i < this.getChildCount(); i++) {
		   GuiTreeNode node = (GuiTreeNode) this.getChildAt(i);
		   node.setDatasetValues(ds);
		}
		// Panel versorgen
		GuiPanel panel = this.getPanel(); 
		if (panel != null) { 
		   panel.setDatasetValues(ds);
		   this.xmlValues = panel.getAllValuesXml(); // frisch zuweisen
		}  
	}

	public void commitChanges() {
		GuiTreeNode node;
		for (int i = 0; i < this.getChildCount(); i++) {
			node = (GuiTreeNode) this.getChildAt(i);
			node.commitChanges();
		}
        // Panel versorgen
        GuiPanel panel = this.getPanel(); 
        if (panel != null) { 
           panel.commitChanges();
        }  
	}

	// End Binding
	public String getIconName() {
		return iconName;
	}

	public void setIconName(String name) {
		this.iconName = name;
	}

	public final void setController(Object o) {
		controller = o;
	}

	/**
	 * Rekursive Suche nach einem Controller
	 */
	public final Object getController() {
		if (controller != null) {
			return controller;
		} else {
			GuiTreeNode parentNode = this.getGuiParentNode();
			if (parentNode != null) {
				return parentNode.getController();
			} else { // wenn RootNode...
				return this.getMyTree().getController(); // ... dann Controller vom Tree
			}
		}
	}

	/**
	 * Setzt das ActionCommand, daß bei der Selection eines anderen Knotens geliefert wird.
	 */
	public final void setMsgNodeClick(String s) {
		msgNodeClick = s;
	}

	/**
	 * Rekursive Suche nach einem ActionCommand
	 */
	public final String getMsgNodeClick() {
		if (msgNodeClick != null) {
			return msgNodeClick;
		} else {
			GuiTreeNode parentNode = this.getGuiParentNode();
			if (parentNode != null) {
				return parentNode.getMsgNodeClick();
			} else { // wenn RootNode...
				return this.getMyTree().getMsgNodeClick(); // ... dann vom Tree
			}
		}
	}

	/**
	 * Erzeugt die zwingenden ChildNodes.
	 * <p>
	 * Voraussetzungen: <br>
	 * <ul>
	 * <li>Diesem Node ist eine GuiTreeElement zu geordnet.
	 * <li>Zu diesem Element wurde eine Liste von Sub-Elementen zugeordnet (Content)
	 * <li>Der GuiTreeContent hat die Eigenschaft mandatory (nn='true')
	 * </ul>
	 */
	public final void createMandatoryChildNodes() {
		GuiTreeElement ele = this.getGuiTreeElement();
		if (ele != null) {
			Vector<GuiTreeContent> eles = ele.getContent();
			if (eles != null) {
				for (int i = 0; i < eles.size(); i++) {
					GuiTreeContent cont = eles.elementAt(i);
					if (cont.isMandatory()) {
						GuiTreeNode mchild = new GuiTreeNode(cont.getName());
						GuiTreeElement mele = (GuiTreeElement) getMyTree().getElements().get(
								cont.getName());
						mchild.setFileName(mele.getFileName());
						mchild.setIconName(mele.getIconName());
						this.add(mchild);
					}
				}
			}
		}
	}

	/**
	 * Liefert einen Vector von erzeugbaren ChildNodes.
	 * <p>
	 * Es werden die ElementNamen weggelassen, die die Eigenschaft typ='n' haben (multi)
	 * und zu denen bereits ein ChildNode existiert.
	 */
	public Vector<Object> getPossibleChildNodeNames() {
		Vector<Object> ret = new Vector<Object>();
		GuiTreeElement ele = this.getGuiTreeElement();
		if (ele != null) {
			Vector<GuiTreeContent> eles = ele.getContent();
			if (eles != null) {
				for (int i = 0; i < eles.size(); i++) {
					GuiTreeContent cont = eles.elementAt(i);
					if (cont.isMulti()) {
						ret.add(cont.getName());
					} else {
						Enumeration enu = this.children();
						boolean hasChild = false;
						while (enu.hasMoreElements()) {
							GuiTreeNode child = (GuiTreeNode) enu.nextElement();
							if (child.getGuiTreeElementName().equals(cont.getName())) {
								hasChild = true;
								break;
							}
						} // Wend
						if (hasChild == false) {
							ret.add(cont.getName());
						}
					}
				}
			}
		} else { // No Element; Files?
			ret = this.getFiles();
		}
		if (ret.size() == 0) {
			return null;
		} else {
			return ret;
		}
	}

	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	/*
	 * Hier mit TreePath arbeiten!
	 */
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,
			IOException {
		Object returnObject;
		returnObject = this.getMyTree().getTree().getSelectionPath();

		return returnObject;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		boolean returnValue = false;
		for (int i = 0, n = flavors.length; i < n; i++) {
			if (flavor.equals(flavors[i])) {
				returnValue = true;
				break;
			}
		}
		return returnValue;
	}

	/**
	 * Liefert die Beschriftung des Knotens in der Oberfläche.
	 */
	public String getTitle() {
		return (String) super.getUserObject();
	}

	/**
	 * Setzt die Beschriftung des Knotens neu.
	 */
	public void setTitle(String title) {
		// Vermutlich wird userObject.toString verwendet.
		super.setUserObject(title);
		if (getMyTree().getJComponent().isVisible() == true) {
			getMyTree().getGuiTreeModel().nodeChanged(this);
		}
	}

	/**
	 * Setzt die Beschriftung des Nodes an dem angegebenen Index neu.
	 */
	public void setTitle(String title, int index) {
		StringBuffer old = new StringBuffer(this.getTitle());
		// ggf. fehlende Trenner anhängen
		int poi = 0;
		for (int i = 0; i < index; i++) {
			int p = old.indexOf(" ° ", poi);
			if (p == -1) {
				old.append(" ° ");
				poi += 3;
			} else {
				poi = p + 3;
			}
		}
		// Ersetzen
		int p1 = 0;
		int p2 = old.length();
		for (int i = 0; i <= index; i++) {
			int p = old.indexOf(" ° ", p1);
			if (i < index) {
				p1 = p + 3;
			} else if (p != -1) {
				p2 = p;
			}
		}
		//System.out.println(p1);
		//System.out.println(p2);
		old.replace(p1, p2, title);
		setTitle(old.toString());
	}

	/**
	 * Setzt ein beliebiges UserObject. Die von DefaultMutableTreeNode geerbte Methode
	 * setUserObject setzt das Label des Nodes.
	 * 
	 * @param o
	 */
	public void setUserObject2(Object o) {
		this.userObject2 = o;
	}

	/**
	 * Nicht zu verwechseln mit DefaultMutableTreeNode.getUserObject
	 * 
	 * @return
	 */
	public Object getUserObject2() {
		return this.userObject2;
	}

	/**
	 * Liefert den Dateiname der zu diesem Knoten gehörenden GUI-Spezifikation.
	 */
	public String getFileName() {
		if (this.fileName == null) {
			if (this.guiDoc != null)
				return this.name;
		}
		return this.fileName;
	}

	/**
	 * Setzt den Dateiname der zu diesem Knoten gehörenden GUI-Spezifikation. <br>
	 * In der Spezifikation sollte nur ein Panel definiert werden
	 */
	public void setFileName(String f) {
		this.fileName = f;
	}

	/**
	 * Setzt das XML-Dokument der zu diesem Knoten gehörenden GUI-Spezifikation. <br>
	 * In der Spezifikation sollte nur ein Panel definiert werden
	 */
	public void setGuiDocumnet(Document doc) {
		this.guiDoc = doc;
	}

	/**
	 * Setzt die Menge der Gui-Spezifikationen, die für diesen Folder erlaubt sind. Aus dem
	 * Schlüsselwort Items= bei Folder und Node. <BR>
	 * Siehe auch die eingebaute Methode "NewNode()".
	 * <p>
	 * Ein besser Lösung ist mit den GuiTreeElement und GuiTreeContent zu arbeiten.
	 */
	public void setFiles(Vector<Object> v) {
		this.files = v;
	}

	/**
	 * Setzt die Menge der Gui-Spezifikationen, die für diesen Folder erlaubt sind. Aus dem
	 * Schlüsselwort Items= bei Folder und Node. <BR>
	 * Siehe auch die eingebaute Methode "NewNode()". Ein besser Lösung ist mit den
	 * GuiTreeElement und GuiTreeContent zu arbeiten.
	 */
	public void setFiles(String[] s) {
		if (s != null) {
			files = new Vector<Object>();
			for (int i = 0; i < s.length; i++) {
				files.add(s[i]);
			}
		} else {
			files = null;
		}
	}

	public Vector<Object> getFiles() {
		return files;
	}

	/**
	 * Liefert die diesem Knoten zugeordnete Oberflächenkomponente.
	 * <p>
	 * Ist dem Knoten eine Gui-Spezifikation zugeordnet (setFileName), wird das Panel
	 * erzeugt (falls noch nicht geschehen). Panels gleichen Namens werden wiederverwendet.
	 * 
	 * @see #setFileName
	 */
	public GuiPanel getPanel() {
		GuiPanel panel = null;
		if ((this.fileName != null || this.guiDoc != null) && this.getMyTree() != null) { // Dateiname
																													 // der
																													 // Spezifikation
																													 // muß
																													 // sein
			// Im Baum nachschauen, ob es dort das Panel schon gibt
			if (this.fileName != null)
				panel = this.getMyTree().getPanel(this.fileName);
			else
				panel = this.getMyTree().getPanel(this.name);

			// Panel vorhanden? ...
			if (panel == null) { // ... nein, das Panel gibt es noch nicht
				// Panel von Factory erzeugen lassen
				try {
					if (this.fileName != null) {
						panel = GuiFactory.getInstance().createPanel(fileName,
								getMyTree().getRootPane().getParentWindow());
					} else {
						panel = GuiFactory.getInstance().createPanelXml(this.guiDoc.toString(),
								getMyTree().getRootPane().getParentWindow());
					}
					// addMember (hier lazyLoad)
					getMyTree().getRootPane().getMainPanel().addMember(panel);

				} catch (GDLParseException ex) {
					GuiUtil.showEx(ex);
				}
				if (panel != null) { // Hätte auch schief gehen können (FileName ungültig)
					if (xmlValues == null) {
						xmlValues = panel.getAllValuesXml(); // Default-Werte aus Spezifikation
																		 // einlesen
					}
					// Panel im Tree registrieren
					if (this.fileName != null)
						getMyTree().addPanel(fileName, panel);
					else
						getMyTree().addPanel(this.name, panel);
				}
			}
			if (xmlValues == null) { // Wenn keine XML-Daten da...
				panel.reset();
				xmlValues = panel.getAllValuesXml(); // ... dann Default-Werte aus Panel
																 // setzen
			}
		}

		return panel;
	}

	/**
	 * Liefert die vom Knoten gehaltenen Werte aus den Benutzereigaben, die zwischen dem
	 * Knoten und seinem Panel hin und hergeschaufelt werden.
	 */
	public Document getAllValuesXml() {
		return xmlValues;
	}

	public void setAllValuesXml(Document doc) {
		xmlValues = doc;
	}

	/**
	 * Das Element wird geclont
	 */
	public void setAllValuesXml(Element ele) {
		xmlValues = new Document();
		xmlValues.setEncoding("UTF-8");
		xmlValues.setRoot((Element) ele.clone());
	}

	/**
	 * Liefert die Tree-Komponete, zu dem dieser Knoten gehört.
	 * <p>
	 * Ist der Baum des Wurzelknotens.
	 */
	public GuiTree getMyTree() {
		// TODO: isRoot liefert auch true, wenn Node frisch erzeugt,
		// aber dem Baum noch nicht zugewiesen wurde.
		if (this.isRoot() == true) {
			return this.myTree;
		} else {
			return ((GuiTreeNode) getRoot()).getMyTree();
		}
	}

	/**
	 * Setzt die Tree-Komponete, zu dem dieser Knoten gehört.
	 * <p>
	 * Wird nur beim Wurzelknoten wirklich benötigt.
	 */
	void setMyTree(GuiTree tree) {
		this.myTree = tree;
	}

	/**
	 * Clont diesen TreeNode z.B. für copy-paste.
	 */
	public GuiTreeNode guiClone() {
		GuiTreeNode node = (GuiTreeNode) this.clone();
		if (fileName != null) {
			node.setFileName(this.fileName);
			if (xmlValues != null) {
				Document doc = (Document) xmlValues.clone();
				node.setAllValuesXml(doc);
			}
		}
		for (int i = 0; i < this.getChildCount(); i++) {
			node.add(((GuiTreeNode) this.getChildAt(i)).guiClone());
		}
		return node;
	}

	/**
	 * Liefert den Pfad dieses Knotens in Punkt-Notation: "root.myFolder.myNode" <br>
	 * Es wird der Name des Knotens verwendet;
	 * 
	 * @see #getName
	 * @see #getChildByName
	 */
	public String getGuiPath() {
		StringBuffer b = new StringBuffer();
		GuiTreeNode node = null;

		TreeNode[] nodes = this.getPath();
		for (int i = 0; i < nodes.length; i++) {
			node = (GuiTreeNode) nodes[i];
			b.append(node.getName());
			if (i < nodes.length - 1) {
				b.append(".");
			}
		}
		return b.toString();
	}

	/**
	 * Liefert einen Iterator über die ChildNodes. Die Elemente können auf GuiTreeNode
	 * gecastet werden.
	 * 
	 * @return Null, wenn keine ChildNodes vorhanden
	 */
	public Iterator getChildNodes() {
		if (this.children == null) {
			return null;
		} else {
			return this.children.iterator();
		}
	}

	/**
	 * Liefert einen Tochterknoten (sowie Enkel usw.) unter Angabe seines Namens oder null
	 * wenn nix zu finden.
	 * 
	 * @see #setName
	 */
	public GuiTreeNode getChildByName(String _name) {
		GuiTreeNode node;
		for (int i = 0; i < this.getChildCount(); i++) {
			node = (GuiTreeNode) this.getChildAt(i);
			if (node.getName().equals(_name)) {
				return node;
			} else {
				GuiTreeNode cnode = node.getChildByName(_name);
				if (cnode != null) {
					return cnode;
				}
			}
		}
		return null;
	}

	/**
	 * @see #setName
	 */
	public String getName() {
		return name;
	}

	/**
	 * Die Namen der Schwester-Nodes müssen eindeutig sein, wenn später auf sie gezielt
	 * zugegriffen werden soll.
	 * 
	 * @see #getChildByName
	 * @see GuiTree#setSelectedNode
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Setzt die beliebige Oid.
	 */
	public final void setOid(long id) {
		oid = id;
	}

	/**
	 * Liefert die Oid. Wenn -1, dann ist keine Oid gesetzt.
	 */
	public final long getOid() {
		return oid;
	}

	/**
	 * DefaultMutableTreeNode mit cast
	 */
	public final GuiTreeNode getGuiParentNode() {
		return (GuiTreeNode) this.getParent();
	}

	/**
	 * Liefert den Wert einer Komponente unter Angabe ihres Namens (auch mit
	 * Punkt-Notation).
	 * <P>
	 * Der Rückgabewert ist zumeist vom Typ "String"; bei Tabellen ein Vector von Vectoren,
	 * bei CheckBoxen ein "Boolean". <br>
	 * TODO : Liefert immer "String"; ChildContainer (Table, Tree) funktionieren nicht!
	 * 
	 * @param xpath
	 *           XPath mit dem Namen der Komponente.
	 */
	public final String getValue(String xpath) {
		Element ele = this.getValueElement(xpath);
		return ele.getTextString();
	}

	/**
	 * Setzt den Wert einer Komponente unter Angabe ihres Namens (auch mit Punkt-Notation).
	 * TODO : ChildContainer (Table, Tree) funktionieren nicht!
	 * 
	 * @param xpath
	 *           Name der Komponente in Punktnotation
	 * @param value
	 *           In der Regel kann ein String verwendet werden; Tabellen erwarten einen
	 *           Vector von Vectoren.
	 */
	public final void setValue(String xpath, Object value) {
		Element ele = this.getValueElement(xpath);
		ele.setText(value.toString());
	}

	/**
	 * Liefert das Element aus dem vom Node gehaltenen Werten:
	 * 
	 * @param xpath
	 *           Pfad zu der Komponete über Punktnotation der Namen ("myTab.myTextBox").
	 */
	private Element getValueElement(String xpath) {
		if (xmlValues == null) {
			throw new IllegalStateException("Node: '" + this.getName()
					+ "' contains no values!");
		}
		StringBuffer buf = new StringBuffer("/Panel");
		StringTokenizer toks = new StringTokenizer(xpath, ".");
		while (toks.hasMoreTokens()) {
			String tok = toks.nextToken();
			buf.append("/[@name='");
			buf.append(tok);
			buf.append("']");
		}
		String xxpath = buf.toString();
		Element ele = xmlValues.getRoot().getElement(new XPath(xxpath));
		if (ele == null) {
			throw new IllegalArgumentException("Missing Element: " + xpath);
		}
		return ele;
	}

	/**
	 * @return boolean
	 */
	public boolean isInserted() {
		return inserted;
	}

	/**
	 * Sets the inserted.
	 * 
	 * @param inserted
	 *           The inserted to set
	 */
	public void setInserted(boolean inserted) {
		this.inserted = inserted;
	}

	final int getModelElementNumber() {
		return modelElementNumber;
	}

	final void setModelElementNumber(int n) {
		modelElementNumber = n;
	}

	// ====== POPUP ========================================
	/**
	 * Setzt das PopupMenu der Komponente.
	 */
	public final void setPopupMenu(GuiPopupMenu menu) {
		this.popupMenu = menu;
		if (this.getMyTree() != null && this.getMyTree().hasMouseListener == false) {
			getMyTree().addMouseListener(new GuiMouseListener(getMyTree()));
		}

	}

	/**
	 * Liefert das PopupMenu zu diesem Member oder null.
	 * 
	 * @see GuiMouseListener
	 */
	public final GuiPopupMenu getPopupMenu() {
		return popupMenu;
	}

	/**
	 * Zeigt das PopupMenu an.
	 * 
	 * @see GuiMouseListener
	 */
	public final void showPopupMenu(int x, int y) {
		if (popupMenu != null) {
			// PopupEvent OnPopup
		   if (this.msgPopup != null) {
				GuiWindow win = this.getMyTree().getRootPane().getParentWindow();
				GuiPopupEvent event = new GuiPopupEvent(win, this.getMyTree(), popupMenu);
				this.getMyTree().getRootPane().obj_PopupShow(this.getMyTree(), this.msgPopup ,event);
		   }
			popupMenu.setGuiInvoker(this.getMyTree());
			popupMenu.show(this.getMyTree().getJComponent(), x, y);
		}
	}

	/**
	 * Gibt an, dass dieses GuiTreeNode kein rootElement ist. Nur GuiTree Komponenten
	 * k&ouml;nnen ihr eigene JDataSet verwalten. <br>
	 * <i>Indicates that this GuiTreeNode does NOT administers its own JDataSet. Only and
	 * GuiTree components can administer its own JDataSet. </i>
	 * 
	 * @see de.guibuilder.framework.IDatasetComponent#isRootElement()
	 */
	public boolean isRootElement() {
		return false;
	}

	/**
	 * Not Implemented
	 */
	public Component getAwtComponent() {
		throw new IllegalComponentStateException("Not Implemented");
	}
	/**
	 * @return Returns the msgPopup.
	 */
	public String getMsgPopup() {
		return this.msgPopup;
	}
	/**
	 * @param msgPopup The msgPopup to set.
	 */
	public void setMsgPopup(String msgPopup) {
		this.msgPopup = msgPopup;
	}
	
	public GuiComponent getGuiComponent(String name) {
	   GuiPanel panel = this.getPanel();
	   if (panel != null) {
	      GuiComponent comp = panel.getGuiComponent(name);
	      return comp;
	   } else {
	      return null;
	   }
	}
}