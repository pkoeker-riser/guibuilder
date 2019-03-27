package de.guibuilder.framework;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;

import de.guibuilder.framework.GuiTable.GuiTableColumn;
import de.guibuilder.framework.event.GuiPopupEvent;
import de.guibuilder.framework.event.GuiUserEvent;
import de.jdataset.JDataSet;
import de.pkjs.util.Convert;
import electric.xml.Attribute;
import electric.xml.Attributes;
import electric.xml.Element;

/**
 * Eine abstrakte Oberflächenkomponente als Wurzel für alle Komponenten.<br>
 * Der Zusammenhang dieser Klasse mit GuiElement und GuiContainer bildet ein
 * Kompositum Pattern.<p>
 * Einige Methoden sind abstact, müssen also jeweils implementiert werden.
 * Andere sind final um sicherzustellen, daß sie nicht überschrieben werden.
 * @since 0.9.1
 * @see GuiElement
 * @see GuiContainer
 */
public abstract class GuiMember implements MemberPopupAble, IDatasetMember {
  // Attributes
  /**
   * @see GuiAction
   * @see #getGuiType
   */
  public static final int GUI_ACTION = 2;
  /**
   * @see GuiComponent
   * @see #getGuiType
   */
  public static final int GUI_COMPONENT = 3;
  /**
   * @see GuiContainer
   * @see #getGuiType
   */
  public static final int GUI_CONTAINER = 4;
  /**
   * @see GuiTable
   * @see #getGuiType
   */
  public static final int GUI_TABLE = 5;
  /**
   * @see GuiTree
   * @see #getGuiType
   */
  public static final int GUI_TREE = 6;
  public static final String[] GUI_TYPE_NAMES = {"Member", "Element", "Action", "Component", "Container", "Table", "Tree"};
  /**
   * Der Container dieser Komponente oder null.<p>
   * Diese Eigenschaft ist auch bei GuiAction gesetzt, wenn diese z.B. auf einer
   * Registerkarte liegen.
   */
  private GuiContainer guiParent;
  /**
   * Label der Komponente; zumeist die Beschriftung des zugeordneten Labels.
   * Siehe Attribut label=
   */
  private String label;
  /**
   * Beliebige Zeichenfolge für Benutzerdefinierte Zwecke;
   * z.B. als Datenbankreferenz.<br>
   * Siehe Attribut ref=
   */
  private String ref;
  /**
   * Ein beliebiges Objekt für Benutzer-eigene Zwecke.
   */
  private Object userObject;
  /**
   * Eine beliebige Object-Id.<p>
   * Diese Object-Id wird auch bei übernahme von XML-Dokumenten
   * im Attribut "_oid=" gesetzt und wieder geliefert.<p>
   * Wenn -1, dann keine Oid gesetzt.
   * @see GuiContainer#getAllValuesXml
   * @see GuiContainer#setAllValuesXml
   */
  private long oid = -1;
  /**
   * Xml-Attribute für oid: "_oid"
   */
  public static final String OID = "_oid";
  /**
   * Controller für Reflection
   */
  private Object controller;
  /**
   * Der Name der Methode, die aufgerufen werden soll, wenn die Maus
   * über der Komponente ist.
   */
  private String msgMouseOver;
  /**
   * Der Name der Methode, die aufgerufen werden soll, wenn die Maus
   * über der Komponente bewegt wird.
   */
  private String msgMouseMoved;
  /**
   * Kennzeichen, ob bereits ein MouseListener eingerichtet ist.
   * Soll doppelte und fehlende MouseListener vermeiden helfen.
   */
  protected boolean hasMouseListener;
  /**
   * Das PopupMenu zu dieser Komponente oder null.
   */
  private GuiPopupMenu popupMenu;
  /**
   * Nachricht, daß das PopupMenu aktiviert wurde:
   * OnPopup=[msg]
   */
  private String msgPopup;
  /**
   * XPath zum Element oder null; dann vom Parent
   */
  protected String elementName;
  /**
   * TODO: Kann raus?
   */
  private DropTarget dropTarget;

  private String msgDrop;
  private String msgFileDrop;
  private String msgDragEnter;
  private String msgDragOver;
  private String msgDragExit;
  // State-Attributes
  private LinkedHashMap<String, LinkedHashMap<String, String>> stateAttributes;
  protected static LinkedHashMap<String, Method> setters;
  //protected static LinkedHashMap<String, Method> getters;
  // Constructor
  /**
   * Macht nüscht.
   */
  GuiMember() {}
  /**
   * Das interne Label wird gesetzt.
   * Dieses sollte dem Label in der Oberfläche entsprechen.<br>
   * Es wird versucht, aus dem Label auch den Namen zu setzten.
   * @see #setName
   */
  GuiMember(String label) {
    setLabel(label);
    setName(GuiUtil.labelToName(label));
  }
  // Methods
  /**
   * Liefert die Swing-Komponente zu diesem Oberflächen-Objekt (Delegation).<p>
   * Es ist möglich, auf die jeweilige Swing-Klasse
   * (JCombo, JTextField, JPanel, ...) zu casten;
   * die Swing-Methoden können so genutzt werden.<p>
   * Bei einem HiddenField und OptionGroup wird null geliefert!
   */
  public abstract JComponent getJComponent();
  // From MemberAble
  public final Component getAwtComponent() {
  		return getJComponent();
  }
  /**
   * Verknüpft ein Member mit einem Container.<p>
   * Ist hier Public wegen Interface OptionAble.
   * @see GuiContainer#addMember
   * @see GuiContainer#addAction
   */
  public final void setGuiParent(GuiContainer c) {
    guiParent = c;
  }
  /**
   * Liefert den Container zu dieser Komponente oder null, wenn nicht Teil
   * eines Containers.<p>
   * Bei Layout-Containern wird der Parent-Container geliefert.
   */
  public final GuiContainer getGuiParent() {
    return guiParent;
  }
  /**
   * From awt.Component.<p>
   * Wird von HiddenField und OptionGroup überschrieben.
   */
  public String getName() {
    return getJComponent().getName();
  }
  /**
   * Delegiert an awt.Component.<p>
   * Wenn das Label null ist, wird es auf den Namen gesetzt.<br>
   * Wird von HiddenField und OptionGroup überschrieben. Außerdem auch
   * von Tab überschrieben (wg. Abhängigkeit zum Tabset)
   */
  public void setName(String name) {
	  
	  boolean removed = false;
	  // Mit altem Namen austragen...
	  if (guiParent != null) {
	    // TODO
	    // Das ist ein übler Hack, der für das Umbenennen benötigt wird
	    // Hat vermutlich schlimme Seiteneffekte!
	   removed = guiParent.removeMember(this);
	  }
	  
	  // Vermutlich wird durch JComponent die Konvertierung zu LowerCase bereits
	  // vorgenommen. Hier ist es aber nochmal explizit realisiert, um auch sicherzugehen
	  // getJComponent().setName(name.toLowerCase());
	  
	  // ... besser doch, wie bisher.
	  getJComponent().setName(name);
	  if (label == null) setLabel(name);
	   // ...mit neuem Name wieder rein.
	  if (removed == true) {
	     // TODO
	     // Das ist ein übler Hack, der für das Umbenennen benötigt wird
	     // Hat vermutlich schlimme Seiteneffekte!
	     guiParent.addMember(this);
	  }
  }
  /**
   * üblicherweise der Text des dazugehörigen Labels der Komponente
   * oder null, wenn kein Label vorhanden. (Darf nicht final sein,
   * weil es in GuiComponent überschrieben wird.)
   */
  public String getLabel() {
    return label;
  }
  /**
   * üblicherweise der Text des dazugehörigen Labels der Komponente.<br>
   * Dieses Attribut ist rein informativ.
   * @param s Der Label-Text
   */
  public void setLabel(String s) {
    label = s;
  }
  /**
   * Liefert rekursiv den Path zum Element des DataSet
   */
  public String getElementPath(String current) {
    // Element
    if (this.elementName != null) {
    	if (this.isRootElement()) {
    		String ret = elementName;
        /*
         * Durch "ROOT:" wird gekennzeichnet, daß als 
         * Element ein absoluter Pfad angegeben wurde.
         * Damit kann eine Referenz auf eine zweite Root-Table
         * in einem Dataset gesetzt werden.
         */
        if( this.elementName.startsWith("ROOT:") ) {
        	ret = elementName.substring(5);
        }
        return ret+current;
    	}      	
      current = elementName + current;
    }
    // Parents abarbeiten
    // Geht bei Navigator-Panel schief, 
    // da dann der entsprechende Node gefunden werden müßte!
    GuiContainer cont = this.getGuiParent();
    if (cont != null) {
      current = cont.getElementPath(current);
    } else {
    	// Navigator-Panel?
    	if (this instanceof GuiPanel) {
    		GuiPanel panel = (GuiPanel)this;
    		GuiTree tree = panel.getMyTree();
    		// TODO : TreeNode finden!?
    		if (tree != null) {
    			current = tree.getElementPath(current);
    		}
    	}
    }
    return current;
  }
  // Element-Name
  /**
   * Liefert den ElementNamen dieser Komponente im JDataSet.
   * Da hieraus Path-Ausdrücke gebildet werden muß dieser Name
   * mit einem ".", "#" oder "@" beginnen.
   * @see de.jdataset.JDataSet
   */
  public final String getElementName() {
    return elementName;
  }
  /**
   * Setzt den ElementNamen dieser Komponente im JDataSet.
   * Da hieraus Path-Ausdrücke gebildet werden muß dieser Name
   * mit einem ".", "#" oder "@" beginnen.
   * Wenn "*" angegeben wird, wird der Name der Komponente verwendet.
   * @see de.jdataset.JDataSet
   */
  public final void setElementName(String s) {
    if (s != null && s.equals("*")) {
      s = getName();
    }
    this.elementName = s;
  }
	/**
	 * Ein Root-Element hält einen eigenen DataSet.<p>
	 * Die Eigenschaft wird hier als false festgelegt, und muß ggf. überschrieben werden.
	 */
	public boolean isRootElement() {
		if (this.elementName != null && this.elementName.startsWith("ROOT:")) {
			return true;
		} else {
			return false;
		}
	}

  // Value
  /**
   * From awt.Component
   */
  public final boolean isVisible() {
    return getJComponent() != null ? getJComponent().isVisible(): false;
  }
  /**
   * From awt.Component
   */
  public final void setVisible(boolean b) {
    if (getJComponent() != null) getJComponent().setVisible(b);
  }
  /**
   * @deprecated
   * Das geht schief, wenn die Registerkarte nur ein Layout-Container ist!<p>
   * Liefert die Registerkarte, auf der die Komponente liegt oder null,
   * wenn außerhalb eines Tabset.
   */
  public final GuiTab getParentTab() {
    GuiMember comp = getGuiParent();
    while (comp != null) {
      comp = comp.getGuiParent();
      //System.out.println(comp);
      if (comp instanceof GuiTab) {
        return (GuiTab)comp;
      }
    }
    return null;
  }
  /**
   * @deprecated
   * Liefert den oder die Namen der übergeordneten Container
   * einer Komponente.<p>
   * Wenn mehrere Container geschachtelt,
   * denn die Namen in Punkt-Notation: tabBestellung.panelLieferAdresse<br>
   * Wenn das Objekt direkt auf dem mainPanel liegt, wird null geliefert.
   * @see GuiPanel#isParentContainer
   */
  public final String getParentNames() {
    String ret = null;
    GuiMember comp = this;
    while (comp != null) {
      comp = comp.getGuiParent();
      if (comp instanceof GuiPanel) {
        if (((GuiPanel)comp).isParentContainer() && comp.getName().equals("mainPanel") == false) {
          if (ret == null) {
            ret = comp.getName();
          } else {
            ret = comp.getName()+"."+ret;
          }
        }
      }
    }
    return ret;
  }
  /**
   * @deprecated
   * Liefert den vollständigen Pfad-Namen dieser Komponenten;
   * etwaige übergeordnete Container werden in Punktnotation vorangestellt:<p>
   * tabBestellung.panelLieferAdresse.strasse<br>
   * Wenn das Objekt auf dem MainPanel liegt, wird getName() geliefert.
   * @see #getParentNames
   * @see #getName
   */
  public final String getFullName() {
    final String parent = getParentNames();
    if (parent != null) {
      return parent+"."+getName();
    } else {
      return getName();
    }
  }
  /**
   * Liefert den Typ der Komponente:
   * <UL>
   * <li>GUI_ACTION (Button, MenuItem)
   * <li>GUI_COMPONENT (Combo, List, Text, Check, usw)
   * <li>GUI_CONTAINER (Panel, Group, Tab)
   * <li>GUI_TABLE
   * <li>GUI_TREE
   * </UL>
   */
  public abstract int getGuiType();
  /**
   * Liefert die Bezeichnung dieses Typs.
   */
  public static String getGuiTypeName(int i) {
    return GUI_TYPE_NAMES[i];
  }
  /**
   * Setzt die Komponente in den Default-Zustand; Methode new()
   */
  public abstract void reset();
  /**
   * Alle Eingabeprüfungen (notNull, minLen, maxLen) testen.
   * @see GuiInputVerifier
   */
  public final void verify() throws IllegalStateException {
     this.verify(GuiUtil.isCheckNN());
  }
  
  public abstract void verify(boolean checkNN) throws IllegalStateException;
  /**
   * Liefert RootPane.<p>
   * Wird von MenuItem und GuiElement überschrieben.
   */
  public GuiRootPane getRootPane() {
    return (GuiRootPane)getJComponent().getRootPane();
    // Geklaut von Swing-Utilities
    /*
    System.out.println(getName()+" ***");
    Component c = getJComponent();
    for( ; c != null; c = c.getParent()) {
      System.out.println(c.getClass().getName());
      if (c instanceof GuiRootPane) {
          return (GuiRootPane)c;
      }
    }
    return null;
    */
  }
  /**
   * Setzt das ActionCommand, was bei MouseOver geliefert werden soll.
   * Dazu wird ein MouseListener eingerichtet (wenn noch nicht vorhanden).
   * Dieses Verhalten wird mit übergabe von null abgeschaltet.
   * @see #mouseOver(MouseEvent, boolean)
   */
  public final void setMsgMouseOver(String s) {
    msgMouseOver = s;
    if (s != null && hasMouseListener == false) {
      this.addMouseListener(new GuiMouseListener(this));
    }
  }
  /**
   * Setzt das ActionCommand, was bei MouseMoved geliefert werden soll.
   * Dazu wird ein MouseMotion eingerichtet.
   * Dieses Verhalten wird mit übergabe von null abgeschaltet.
   * @see #mouseMoved(MouseEvent, boolean)
   */
  public final void setMsgMouseMoved(String s) {
    msgMouseMoved = s;
    if (s != null) {
      this.addMouseMotionListener(new GuiMouseMotionListener(this));
    }
  }
  /**
   * Liefert das ActionCommand, was bei MouseOver geliefert wird.
   */
  public final String getMsgMouseOver() {
    return msgMouseOver;
  }
  /**
   * Wird aufgerufen, wenn die Maus über die Komponente bewegt wird (true)
   * oder sie wieder verläßt (false).<br>
   * Wird an RootPane weitergereicht.
   * @see #setMsgMouseOver
   * @see GuiMouseListener
   * @see GuiRootPane#obj_MouseOver
   */
  final void mouseOver(MouseEvent e, boolean isOver) {
    if (msgMouseOver != null) {
      getRootPane().obj_MouseOver(this, msgMouseOver, e, isOver);
    }
  }
  final void mouseMoved(MouseEvent e, boolean isDrag) {
    if (msgMouseMoved != null) {
      getRootPane().obj_MouseMoved(this, msgMouseMoved, e, isDrag);
    }
  }
  /**
   * Die Komponente wird angeklickt; ist hier leer implementiert und muß
   * bei Bedarf überschrieben werden.
   * @see GuiMouseListener
   */
  public void click(MouseEvent e) {}
  /**
   * Die Komponente wird doppelt angeklickt. Diese Methode ist hier leer implementiert
   * und muß folglich bei abgeleiteten Klassen überschrieben werden.
   * Ist dieses Objekt eine Tabellenspalte, wird diese Nachricht auch an die
   * Tabelle weitergeleitet.
   * @see GuiMouseListener
   * @see GuiRootPane#obj_DblClick(GuiComponent, String, Object, MouseEvent)
   */
  public void d_click(MouseEvent e) {}
  // ************* PopupMenu *****************************
  /**
   * Setzt das PopupMenu der Komponente.
   * Dazu wird ein MouseListener eingerichtet (wenn noch nicht vorhanden).
   * @see GuiMouseListener
   */
  public void setPopupMenu(GuiPopupMenu m) {
    popupMenu = m;
    if (m != null) {
      if (!hasMouseListener ) {
        this.addMouseListener(new GuiMouseListener(this));
      }
      {// CTRL-M= ContextMenü
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK, false);
        /*
         * ActionListener 
         */
        ActionListener actionListener = new ActionListener() {
          public final void actionPerformed(ActionEvent actionEvent) {
            //Point loc = MouseInfo.getPointerInfo().getLocation();
            showPopupMenu(100, 10);
            //showPopupMenu(loc.x, loc.y);
          }
        };
        this.getJComponent().registerKeyboardAction(actionListener, "popup", stroke, JComponent.WHEN_FOCUSED);
      }
      {// Windows-Context-Menü-Taste
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0, false );
        /*
         * ActionListener 
         */
        ActionListener actionListener = new ActionListener() {
          public final void actionPerformed(ActionEvent actionEvent) {
             showPopupMenu(100, 10);
          }
        };
        this.getJComponent().registerKeyboardAction(actionListener, "popup", stroke, JComponent.WHEN_FOCUSED);
      }
    }
  }
  /**
   * Liefert das PopupMenu zu diesem Member oder null.
   * @see GuiMouseListener
   */
  public final GuiPopupMenu getPopupMenu() {
    return popupMenu;
  }
  /**
   * Zeigt das PopupMenu an.
   * @see GuiMouseListener
   */
  public final void showPopupMenu(int x, int y) {
		if (popupMenu != null) {
		  // PopupEvent OnPopup
		  if (this.msgPopup != null) {
				GuiWindow win = this.getRootPane().getParentWindow();
				GuiPopupEvent event = new GuiPopupEvent(win, this, popupMenu);
				this.getRootPane().obj_PopupShow(this, this.msgPopup ,event);
		  }
			// currentTable / Tree setzen
			if (this.getGuiType() == GUI_TABLE) {
				// Zeile auch mit der rechten Maustaste selektieren
				GuiTable tbl = (GuiTable) this;
				this.getRootPane().setCurrentTable((GuiTable) this);
				JTable jtbl = (JTable) tbl.getJComponent();
				int row = jtbl.rowAtPoint(new Point(x, y));
				/*
				 * KKN 24.04.2004
				 * Zeile nur dann selektieren, wenn keine anderen Zeilen
				 * oder nur eine Zeile selektiert ist.
				 * Anderenfalls ist der Aufruf einer Pop-Funktion mit 
				 * mehreren selektierten Zeilen nicht möglich. 
				 */
				if (tbl.getSelectedRows() == null || tbl.getSelectedRows().length <= 1)
					tbl.setSelectedRow(row);
				// Column
				TableColumnModel colModel = jtbl.getColumnModel();
				int colIndex = colModel.getColumnIndexAtX(x);
				if (colIndex >= 0) {
				  GuiTableColumn column = tbl.getColumn(colIndex);
				  //System.out.println(column.getName());
				}
				// enable/disable
				int cnt = tbl.getSelectedRows().length;
				popupMenu.setEnabledWhen(cnt);
			} else if (this.getGuiType() == GUI_TREE) {
				GuiTree tree = (GuiTree) this;
				this.getRootPane().setCurrentTree(tree);
			}
			popupMenu.setGuiInvoker(this);
			popupMenu.show(getJComponent(), x, y);
			popupMenu.requestFocus(); // geht auch nicht
		}
		// Node-spezifischen PopupMenu?
		if (this.getGuiType() == GUI_TREE) {
			GuiTree tree = (GuiTree) this;
			TreePath selPath = tree.getTree().getPathForLocation(x, y);
			if (selPath != null) { // Wenn außerhalb geklickt / PKÖ 27.2.2005
				GuiTreeNode node = (GuiTreeNode) selPath.getLastPathComponent();
				if (node != null) { // Kann auch null sein! / PKÖ 1.10.2004
					GuiPopupMenu menu = node.getPopupMenu();
					if (menu != null) {
						tree.setSelectedNode(node);
						node.showPopupMenu(x, y);
					}
				}
			}
	  }
  }
  /**
   * Zeigt ein KontextMenü an Abhängigkeit eines Modifiers;
   * muß jeweils überschrieben werden.
   * @param modi InputEvent.CTRL_DOWN_MASK usw.
   * @param x
   * @param y
   */
  public void showPopupMenu(int modi, int x, int y) {
    
  }
  /**
   * From swing.JComponent
   */
  public final void setMinimumSize(Dimension d) {
    if (getJComponent() != null) getJComponent().setMinimumSize(d);
  }
  /**
   * From swing.JComponent
   */
  public final Dimension getMinimumSize() {
    return getJComponent() != null ? getJComponent().getMinimumSize(): new Dimension(0,0);
  }
  /**
   * From swing.JComponent
   */
  public final void setMaximumSize(Dimension d) {
    if (getJComponent() != null) getJComponent().setMaximumSize(d);
  }
  /**
   * From swing.JComponent
   */
  public final Dimension getMaximumSize() {
    return getJComponent() != null ? getJComponent().getMaximumSize() : new Dimension(0,0);
  }
  /**
   * From swing.JComponent
   */
  public final void setPreferredSize(Dimension d) {
    if (getJComponent() != null) getJComponent().setPreferredSize(d);
  }
  /**
   * From swing.JComponent
   */
  public final Dimension getPreferredSize() {
    return getJComponent() != null ? getJComponent().getPreferredSize() : new Dimension(0,0);
  }
  /**
   * From swing.JComponent
   */
  public final void setFont(Font f) {
    if (getJComponent() != null) getJComponent().setFont(f);
  }
  /**
   * From swing.JComponent
   */
  public final Font getFont() {
    return getJComponent() != null ? getJComponent().getFont() : null;
  }
  /**
   * From swing.JComponent
   */
  public final String getToolTipText() {
    return getJComponent() != null ? getJComponent().getToolTipText() : null;
  }
  /**
   * From swing.JComponent
   */
  public final void setToolTipText(String s) {
    if (getJComponent() != null) getJComponent().setToolTipText(s);
  }
  /**
   * From swing.JComponent
   */
  public final Color getBackground() {
    return getJComponent() != null ? getJComponent().getBackground() : null;
  }
  /**
   * From swing.JComponent
   */
  public final void setBackground(Color c) {
    if (getJComponent() != null) getJComponent().setBackground(c);
  }
  /**
   * From swing.JComponent
   */
  public final Color getForeground() {
    return getJComponent() != null ? getJComponent().getForeground() : null;
  }
  /**
   * From swing.JComponent
   * @see GuiFocusListener
   */
  public final void setForeground(Color c) {
    if (getJComponent() != null) getJComponent().setForeground(c);
  }
  /**
   * Wenn eine Komponente auf Maus-Ereignisse (Click, DblClick, RightMouseClick, ...)
   * entgegennehmen soll, muß ein MouseListener eingerichtet werden.<br>
   * Die Komponente, die das Ereignis verarbeiten soll, ist im Konstruktor
   * des GuiMouseListeners anzugeben; das ist üblicherweise dieselbe;
   * es kann aber auch sein, daß z.B. eine ScrollBox dieses Ereignis
   * an die enthaltene Tabelle weiterreicht.<br>
   * Es kann nur ein MouseListener eingerichtet werden.<p>
   * From swing.JComponent
   * @see GuiMouseListener
   */
  final void addMouseListener(GuiMouseListener l) {
    if (getJComponent() != null && hasMouseListener == false) {
      getJComponent().addMouseListener(l);
      hasMouseListener = true;
    }
  }
  final void addMouseMotionListener(GuiMouseMotionListener l) {
    if (getJComponent() != null) getJComponent().addMouseMotionListener(l);
  }
  /**
   * From swing.JComponent
   */
  final void addFocusListener(GuiFocusListener l) {
    if (getJComponent() != null) getJComponent().addFocusListener(l);
  }
  /**
   * Liefert die Größe des Objektes
   */
   /*
  public final int getObjectSize() {
    final java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    try {
      new java.io.ObjectOutputStream( baos ).writeObject(this);
      return baos.size();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return -1;
  }
  */
  /**
   * Setzt eine beliebige Zeichenfolge für Benutzer-definierte Zwecke;
   * z.B. als Datenbankreferenz.<br>
   * Siehe Attribut ref=
   */
  public final void setRef(String ref) {
    this.ref = ref;
  }
  /**
   * @see #setRef
   */
  public final String getRef() {
    return ref;
  }
  /**
   * Jeder Oberflächen-Komponente kann ein beliebiges Object zugewiesen
   * werden. Dieses wird vom Framework selbst nie verwendet.
   */
  public final void setUserObject(Object obj) {
    userObject = obj;
  }
  /**
   * Liefert das UserObject
   */
  public final Object getUserObject() {
    return userObject;
  }
  /**
   * Setzt die beliebige Oid.
   */
  public final void setOid(long id) {
    oid = id;
  }
  /**
   * Liefert die Oid.<br>
   * Wenn -1, dann ist keine Oid gesetzt.
   */
  public final long getOid() {
    return oid;
  }
  /**
   * Liefert die GridBagConstraints zu diesem Member, oder null,
   * wenn kein GridBagLayout oder wenn kein awt.Component.
   */
  public final GridBagConstraints getConstraints() {
    GridBagConstraints ret = null;
    Component comp = getJComponent();
    if (comp != null) {
      GridBagLayout grid = getGridBagLayout(comp);
      if (grid != null) {
        ret = grid.getConstraints(comp);
      }
    }
    return ret;
  }
  /**
   * Setzt die GridBagConstraints für diesen Member neu;
   * macht nichts, wenn Member kein awt.Component.
   */
  public final void setConstraints(GridBagConstraints constr) {
    Component comp = getJComponent();
    if (comp != null) {
      GridBagLayout grid = getGridBagLayout(comp);
      if (grid != null) {
        grid.setConstraints(comp, constr);
        grid.layoutContainer(comp.getParent());
      }
    }
  }
  static GridBagLayout getGridBagLayout(Component comp) {
    GridBagLayout ret = null;
    Container parent = comp.getParent();
    if (parent != null) {
      LayoutManager lay = parent.getLayout();
      if (lay instanceof GridBagLayout) {
        ret = (GridBagLayout)lay;
      }
    }
    return ret;
  }
  /**
   * Liefert den XML-Element-Tag für den Member.
   */
  public abstract String getTag();
  /**
   * Liefert ein Element
   * @return
   */
  public Element getMemberElement() {
     Element ele = new Element(this.getName());
     if (this.getLabel() != null && this.getLabel().length()>0) {
        ele.setAttribute("label", this.getLabel());
     }
     ele.setAttribute("type", this.getTag());
     return ele;
  }
  
  // *************** Controller *******************************
  /**
   * @deprecated
   * @see #setController(Object)
   */
  public final void setControler(Object o) {
    this.setController(o);
  }
  /**
   * Auf jeder Ebene kann ein Controller für Benutzerereignisse eingerichet werden.
   * @see GuiRootPane#invokeMethod(Object, String, GuiUserEvent)
   */
  public final void setController(Object o) {
      this.controller = o;
  }
  /**
   * @deprecated
   * @see #getController()
   * @return
   */
  public final Object getControler() {
     return this.getController();
  }
  /**
   * Liefert den eingerichteten Controller oder der seines Parents.
   */
  public final Object getController() {
      if (controller != null) {
        return controller;
      }
      if (getGuiParent() != null) {
        return getGuiParent().getController();
      } else {
        return null;
      }
    }
  /**
   * Hier leer implementiert
   * @param ds
   */
  public void getPreferences(JDataSet ds) {
    
  }
  /**
   * Hier leer implementiert
   * @param ds
   */
  public void setPreferences(JDataSet ds) {
    
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

	/**
	 * Liefert das ActionCommand für OnDrop=
	 * @return
	 */
	public String getMsgDrop() {
		return msgDrop;
	}
  public String getMsgFileDrop() {
    return msgFileDrop;
  }
	
	/**
	 * Setzt das ActionCommand, welches beim Drop auf dieses Widget ausgelöste wird.
	 * Gleizeitig wird dieses Widget als ein DropTarget eingerichtet.<br>
	 * Siehe das Attribut OnDrop="[ActionCommand]"<br>
	 * Drop funktioniert bei folgenden Komponeneten:
	 * <ul><li>Text (mit Date, Time, Number, Money)
	 * <li>Memo
	 * <li>Editor
	 * <li>Tree
	 * </ul>
	 * Bei übergabe von null oder Leerstring wird dieses DropTarget wieder entfernt.
	 * Mit {@link #getDropTarget} kann das hierbei eingerichtete DropTarget manipuliert werden.
	 * @see #getDropTarget
	 * @param cmd Ein ActionCommand; üblicherweise der Name einer Methode des Controllers.
	 */
	public void setMsgDrop(String cmd) {
		msgDrop = cmd;
		if (cmd == null || cmd.length() == 0) {
			this.dropTarget = null;
		} else {
			if (this.dropTarget == null) {
				// Oder DropTarget.setComponent
				this.dropTarget = new DropTarget(
  				this.getJComponent(),
  				// Der DropTarget-Listener ist optional
  				new DTListener()); 	
			}
		}
	}
	
  public void setMsgFileDrop(String cmd, Container cont) {
    msgFileDrop = cmd;
    new FileDrop(cont, new FileDrop.Listener() {
      
      public void filesDropped(File[] files) {
        getRootPane().obj_FileDrop(GuiMember.this, msgFileDrop, files);
      }
    });
  }
  
	/**
   * @deprecated unused?
	 * Liefert das DropTarget falls eines definiert wurde.
	 * @see #setMsgDrop
	 * @return
	 */
	public DropTarget getDropTarget() {
		return this.dropTarget;
	}
	/**
	 * Definiert diese Komponente als DragSource
	 * @param b
	 */
	public void setDrag(boolean b) {
		if (b) {
			DragSource dragSource = new DragSource();
			dragSource.createDefaultDragGestureRecognizer(
						this.getJComponent(), 
						DnDConstants.ACTION_COPY_OR_MOVE, 
						new DGListener());
		} else {
			// TODO: Wie DragSource abschalten?
		}
	}
	/**
	 * @return Returns the msgDragEnter.
	 */
	public String getMsgDragEnter() {
		return msgDragEnter;
	}
	/**
	 * @param msgDragEnter The msgDragEnter to set.
	 */
	public void setMsgDragEnter(String msgDragEnter) {
		this.msgDragEnter = msgDragEnter;
	}
	/**
	 * @return Returns the msgDragExit.
	 */
	public String getMsgDragExit() {
		return msgDragExit;
	}
	/**
	 * @param msgDragExit The msgDragExit to set.
	 */
	public void setMsgDragExit(String msgDragExit) {
		this.msgDragExit = msgDragExit;
	}
	/**
	 * @return Returns the msgDragOver.
	 */
	public String getMsgDragOver() {
		return msgDragOver;
	}
	/**
	 * @param msgDragOver The msgDragOver to set.
	 */
	public void setMsgDragOver(String msgDragOver) {
		this.msgDragOver = msgDragOver;
	}
    /**
     * Setzt die Liste der Attribute für die verschiedenen Zustände.
     * Notation:
     * <State name="StateName" myAtt1="myAtt1_Value" myAtt2="... />
     * @param ele
     */
    void addStateAttributes(Element ele) {
        String sName = ele.getAttribute("name");
        Attributes atts = ele.getAttributeObjects();
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String> ();
        while(atts.hasMoreElements()) {
            Attribute att = atts.next();
            String aName = att.getName();
            if (aName.equals("name") == false) {
                map.put(aName, att.getValue());
            }
        }
        if (this.stateAttributes == null) {
            this.stateAttributes = new LinkedHashMap<String, LinkedHashMap<String, String>>();
        }
        this.stateAttributes.put(sName, map);
    }
    void addStateAttribute(String state, String name, String value) {
        if (this.stateAttributes == null) {
            this.stateAttributes = new LinkedHashMap<String, LinkedHashMap<String, String>>();
        }
        LinkedHashMap<String, String> map = this.stateAttributes.get(state);
        if (map == null) {
            map = new LinkedHashMap<String, String>();
            this.stateAttributes.put(state, map);
        }
        map.put(name, value);    
    }
    public void setStateAttributes(String state) {
        if (this.stateAttributes == null) return;
        LinkedHashMap<String, String> map = this.stateAttributes.get(state);
        if (map == null) return;
        for (Iterator<Map.Entry<String,String>> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String,String> entry = it.next();
            String aName = entry.getKey();
            String aValue = entry.getValue();
            Method m = this.getSetAttribMethod(aName);
            if (m != null) {
                Class argClass = m.getParameterTypes()[0];
                Object o = Convert.toObject(aValue, argClass);
                Object[] args = {o};
                try {
                    m.invoke(this, args);
                } catch (Exception ex) {
                    // TODO
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
    Method getSetAttribMethod(String attName) {
        Method m = setters.get(attName);
        return m;
    }
    /**
     * Fügt der Komponente einen Setter hinzu.
     * @param attribName Name des Attributes aus gdl.dtd
     * @param m ein setter
     */
    public static void addSetter(String attribName, Method m) {
        if (setters == null) {
            setters = new LinkedHashMap<String, Method>();
        }
        setters.put(attribName, m);
    }
  // ################# DragDrop

  // Inner Classes #########################################
  private final class DGListener implements DragGestureListener {
     public void dragGestureRecognized(DragGestureEvent event) {
  		// check to see if action is OK ...
        try {
            // TODO: Funzt nicht! GuiMember wird nicht transferiert!
            Transferable transferable = new StringSelection(GuiMember.this.getLabel());
            GuiSession.getInstance().setProperty("LastDragSource", GuiMember.this);
            DSListener dsListener = new DSListener();
            //Image img = GuiUtil.makeAwtImage("/icons/Open24.gif"); 
            event.startDrag(
						/*
						Toolkit.getDefaultToolkit().createCustomCursor(
								img,
								new Point(0,0),
								"Test"
								),
								*/
						DragSource.DefaultCopyNoDrop,
						transferable, 
						dsListener);
        } catch( InvalidDnDOperationException ex ) {
		      System.err.println( ex );
        }
  	 }
  }
  /*
	private final class GuiDragGestureListener implements DragGestureListener {
		public void dragGestureRecognized(DragGestureEvent e) {
			try {
				//System.out.println("DragStart mit " + GuiComponent.this.getName());
				GuiSession.getInstance().setProperty("LastDragComponent", GuiMember.this);
				StringSelection sel = new StringSelection(GuiMember.this.getName());
				e.startDrag(null,sel);
			}
			catch(InvalidDnDOperationException ex) {
				
			}
		}
	}
	*/
  private final class DSListener implements DragSourceListener {
		public void dragEnter(DragSourceDragEvent event) {
		  //System.out.println("dragEnter");
		  event.getDragSourceContext().setCursor(null);
		}
		public void dragExit(DragSourceEvent event) {
		  //System.out.println("dragExit");
		}
		public void dragDropEnd(DragSourceDropEvent event) {
		  //System.out.println("dragDropEnd");
		}
		public void dragOver(DragSourceDragEvent event) {
		  //System.out.println(",");
		}
		public void drop(DragSourceDropEvent event) {
		  //System.out.println("dragDrop");
		  //getRootPane().obj_Drop(GuiMember.this, event, getMsgDrop());
		}
		public void dropActionChanged(DragSourceDragEvent event) {
		  //System.out.println("dropActionChanged");
		}
  }
	// Inner Class DropTarget Listener ====================================
	final class DTListener implements DropTargetListener {
		public void dragEnter(DropTargetDragEvent event) {
			if (getMsgDragEnter() != null) {
			  //System.out.println("dragEnter");
				//System.out.println(event.getSourceActions()); // 3=Copy_Or_Move
				//event.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
			  getRootPane().obj_Drag(GuiMember.this, event, getMsgDragEnter(), GuiUserEvent.DRAG_ENTER);
			}
		}
		public void dragExit(DropTargetEvent event) {
			if (getMsgDragExit() != null) {
			  //System.out.println("dragExit");
			  getRootPane().obj_Drag(GuiMember.this, event, getMsgDragExit(), GuiUserEvent.DRAG_EXIT);
			}
		}
		public void dragOver(DropTargetDragEvent event) {
			if (getMsgDragOver() != null) {
			  //System.out.println("dragOver");
			  getRootPane().obj_Drag(GuiMember.this, event, getMsgDragOver(), GuiUserEvent.DRAG_OVER);
			}
		}
		public void drop(DropTargetDropEvent event) {
			if (getMsgDrop() != null) {
			  //System.out.println("dragDrop");
				//int action = event.getSourceActions(); // 0=NONE, 1=COPY, 2=MOVE, 3=COPYORMOVE
				event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				//Transferable trans = event.getTransferable();
			  getRootPane().obj_Drop(GuiMember.this, event, getMsgDrop());
			} else {
				event.rejectDrop();
			}
		}
		public void dropActionChanged(DropTargetDragEvent event) {
		  //System.out.println("dropActionChanged");
		}
	}
}