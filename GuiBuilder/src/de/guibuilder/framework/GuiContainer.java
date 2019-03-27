package de.guibuilder.framework;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import de.jdataset.JDataSet;
import de.pkjs.util.Convert;
import electric.xml.CData;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.Text;

/**
 * Diese Klasse ist von allen Komponenten zu erweitern, die als
 * Container für Daten haltende Komponenten fungieren sollen:
 * Panel, Group, Tab, Split.
 * <p>
 * Zusammen mit GuiMember und GuiElement bildet diese Klasse ein Compositum-Pattern.
 * <p>
 * Die Hauptfenster (Form, Dialog, Applet, InternalFrame) haben alle eine Referenz auf ein Objekt der Klasse
 * GuiRootPane.<br>
 * GuiRootPane hat genau ein WurzelPanel, dem alle weiteren Komponenten zugeordnet sind.
 * <P>
 * Typischer Code:<br>
 * <code>GuiPanel root = myForm.getRootPane().getMainPanel();</code><br>
 * Die Container halten zwei Mengen von Komponenten, die durch die Methode addMember bzw. addAction aufgebaut werden.
 * Daher müssen die Namen der Komponenten je Container eindeutig sein.<br>
 * Zusätzlich wird eine ArrayList mit den Namen der Komponenten in der Reihenfolge geführt, in der sie dem Container
 * hinzugefügt wurden (siehe getMemberNames).
 * <P>
 * Wichtig zum Verständnis ist, daß ein Container zwei Rollen einnehmen kann:<br>
 * 1. Layout-Container<br>
 * 2. Parent-Container<br>
 * Welche Rolle ein Container einnimmt liefert die Methode isParentContainer.<br>
 * Layout-Container dienen nur der Gliederung im (GridBag)Layout; sie haben keine Members!<br>
 * Parent-Container haben neben ihrer Layout-Funktion zusätzlich eine Liste von Members.
 * <p>
 * Die Schachtelung der Parent-Container wird von der Factory nach folgenden Regeln aufgebaut:
 * <UL>
 * <LI>Alle Registerkarten (GuiTab) werden implizit immer als Parent-Container geführt.
 * <LI>Registerkarten mit dem Attribut ref="-" werden nicht als Parent-Container verwendet.
 * <LI>Panels werden dann als Parent-Container verwendet, wenn sie ein Attribut ref="*" haben. Dieses Konvention soll
 * verhindern, daß Panels, die lediglich aus Gründen des Layouts eingeführt werden, den Zugriff auf die in ihnen
 * enthaltenen Komponenten erschwert.
 * </UL>
 * <p>
 * Mit den Methoden "getValue" und "setValue" kann der Inhalt einer Komponente gelesen oder gesetzt werden.<br>
 * Der Name der Komponente kann hierbei in Punkt-Notation erfolgen, bei der die Schachtelung der Container abgebildet
 * wird:<br>
 * <code>strasse = frm.getValue("tabAdresse.panelRechnung.strasse");</code>
 * <P>
 * Die Komponenten selbst implementieren die Methoden "getValue" und "setValue" um auf ihren Inhalt zuzugreifen.
 * <p>
 * Mit der Methode reset() wird der Wert aller Daten haltenden Widgets des Containers zurückgesetzt; dies gilt auch für
 * seine Child Container.
 * <p>
 * Per default ist den Container das GridBagLayout zugeordnet. Mit dem Attribut layout= kann auch ein anderer
 * LayoutManager gesetzt werden.
 * 
 * @see GuiMember
 * @see GuiElement
 * @see GuiPanel
 * @see GuiRootPane#getMainPanel
 */
public abstract class GuiContainer extends GuiMember implements IDatasetComponent {
   // Attributes
   /**
    * Menge der GuiMembers
    * 
    * @see GuiMember
    */
   private HashVector members;
   /**
    * Menge der Componenten, die von GuiAction abgeleitet sind:
    * Buttons, MenuItems.
    * 
    * @see GuiAction
    */
   private HashVector actions;
   private JPanel toolbarPanel;
   /**
    * Menge der awt-Componenten, die in diesem Container enthalten sind.
    * Achtung! Die sind vom Typ her bunt gemischt!
    * Siehe die Methode add(awt.Component ...)
    */
   private ArrayList<Object> components = new ArrayList<Object>();

   /**
    * Liste der Timer des Windows
    * TODO: Timer anhalten, wenn Fenster nicht sichtbar?
    */
   private ArrayList<GuiTimer> timers;
   /**
    * Pnuts oder BeanShell Context wenn Scripting oder null.
    */
   private GuiScripting context;
   /**
    * Mit diesem Container verknüpfte Tabelle.
    */
   private GuiTable linkTable;
   // Layout-Manager
   /**
    * Null Layout
    */
   public static final int NULL = 0;
   /**
    * GridBagLayout
    */
   public static final int GRIDBAG = 1;
   /**
    * GridLayout
    */
   public static final int GRID = 2;
   /**
    * FlowLayout
    */
   public static final int FLOW = 3;
   /**
    * BorderLayout
    */
   public static final int BORDER = 4;
   /**
    * SpringLayout (Swing)
    */
   public static final int SPRING = 5;
   /**
    * FormLayout; see http://jgoodies.com
    */
   public static final int FORM = 6;
   /**
    * @see GuiSplit
    */
   public static final int SPLIT = 7;
   /**
    * TableLayout
    */
   public static final int TABLE = 8;
   static final String[] LAYOUT_MANAGER_NAMES = {"NULL", "GRIDBAG", "GRID", "FLOW", "BORDER", "SPRING", "FORM",
                                                 "SPLIT", "TABLE"};

   /**
    * Liefert die Bezeichnung des Layout-Managers.
    */
   public static String getLayoutManagerName(int i) {
      return LAYOUT_MANAGER_NAMES[i];
   }

   // End Layout Manager
   // Form-Layout
   private String colSpec;
   private String rowSpec;
   private AbstractLayoutHelper layoutHelper;

   private String msgCreate; // OnCreate="...
   private JDataSet dataset;

   // Constructor
   GuiContainer() {
      super();
   }

   /**
    * Erzeugt einen Container mit einem bestimmten Label.
    * 
    * @see GuiMember#setLabel
    */
   GuiContainer(String label) {
      super(label);
   }

   // Methods
   /**
    * Liefert GUI_CONTAINER
    */
   @Override
   public int getGuiType() {
      return GUI_CONTAINER;
   }

   /**
    * @see #NULL
    */
   public abstract int getLayoutManager();

   /**
    * @see #NULL
    */
   public abstract void setLayoutManager(int lm);

   /**
    * From awt.Container
    * 
    * @see GuiPanel#setDesktop
    */
   public final void add(Component comp) {
      getJComponent().add(comp);
   }

   /**
    * Wird von der Factory aufgerufen, wenn die Komponente in einer
    * ScrollBox steckt (wie z.B. bei Table, Tree, Memo, List).
    * <p>
    * From awt.Container
    */
   public void add(Component comp, GridBagConstraints constraints) {
      this.addComponent(comp, constraints);
      // components
      components.add(comp);
   }

   private void addComponent(Component comp, GridBagConstraints c) {
      switch(getLayoutManager()) {
         case NULL:
            int x = c.gridx;
            int y = c.gridy;
            int w = c.gridwidth;
            int h = c.gridheight;
            if(w < 10 && h < 10) { // Hack! auch Attribut size=
               w = comp.getPreferredSize().width;
               h = comp.getPreferredSize().height;
            }
            comp.setBounds(x, y, w, h);
            getJComponent().add(comp);
            break;
         case GRIDBAG:
            getJComponent().add(comp, c);
            break;
         case GRID:
            getJComponent().add(comp);
            break;
         case FLOW:
            getJComponent().add(comp);
            break;
         case BORDER: {
            String bc = BorderLayout.CENTER;
            switch(c.anchor) {
               case GridBagConstraints.NORTH:
                  bc = BorderLayout.NORTH;
                  break;
               case GridBagConstraints.EAST:
                  bc = BorderLayout.EAST;
                  break;
               case GridBagConstraints.CENTER:
                  bc = BorderLayout.CENTER;
                  break;
               case GridBagConstraints.WEST:
                  bc = BorderLayout.WEST;
                  break;
               case GridBagConstraints.SOUTH:
                  bc = BorderLayout.SOUTH;
                  break;
            }
            getJComponent().add(comp, bc);
         }
            break;
         case FORM: {
            if(layoutHelper == null) { // HACK
               layoutHelper = new FormLayoutHelper(getColSpec(), getRowSpec());
               LayoutManager frmLay = layoutHelper.getLayoutManager();
               getJComponent().setLayout(frmLay);
            }
            Object constraints = layoutHelper.addAbsolut(c);
            getJComponent().add(comp, constraints); // OK?
         }
            break;
         case SPRING: {
            //      	// TODO Spring
            //      	SpringLayout layout = (SpringLayout)getJComponent().getLayout();
            //      	// Anchor
            //      	String anchor = SpringLayout.WEST;
            //      	switch (c.anchor) {
            //      	case GridBagConstraints.NORTH:
            //      		anchor = SpringLayout.NORTH;
            //      		break;
            //      	case GridBagConstraints.EAST:
            //      		anchor = SpringLayout.EAST;
            //      		break;
            //      	case GridBagConstraints.WEST:
            //      		anchor = SpringLayout.WEST;
            //      		break;
            //      	case GridBagConstraints.SOUTH:
            //      		anchor = SpringLayout.SOUTH;
            //      		break;
            //      	}
            //      	//layout.putConstraint(anchor, comp, 5, getJComponent());
            break;
         }
         case TABLE: {
            // GridBagConstraints --> TableLayoutConstraints
            String constr = gridBag2Table(c);
            getJComponent().add(comp, constr);
            break;
         }
      }
   }

   /**
    * Erzeugt aus den GridBagConstraints TableLayoutConstraints
    * 
    * @param c
    * @return
    */
   private String gridBag2Table(GridBagConstraints c) {
      c.gridx = c.gridx * 2 + 1;
      c.gridy = c.gridy * 2 + 1;
      if(c.gridwidth == 0)
         c.gridwidth = 1;
      if(c.gridheight == 0)
         c.gridheight = 1;
      c.gridwidth = c.gridwidth * 2 - 2;
      c.gridheight = c.gridheight * 2 - 2;

      StringBuffer buff = new StringBuffer(12);
      buff.append(c.gridx);
      buff.append(',');
      buff.append(c.gridy);
      buff.append(',');
      buff.append(c.gridx + c.gridwidth);
      buff.append(',');
      buff.append(c.gridy + c.gridheight);
      buff.append(',');
      // hori
      switch(c.fill) {
         case GridBagConstraints.HORIZONTAL:
            buff.append("F,");
            break;
         case GridBagConstraints.BOTH:
            buff.append("F,");
            break;
         case GridBagConstraints.NONE:
            switch(c.anchor) {
               case GridBagConstraints.WEST:
               case GridBagConstraints.NORTHWEST:
               case GridBagConstraints.SOUTHWEST:
                  buff.append("L,");
                  break;
               case GridBagConstraints.EAST:
               case GridBagConstraints.NORTHEAST:
               case GridBagConstraints.SOUTHEAST:
                  buff.append("R,");
                  break;
               case GridBagConstraints.CENTER:
                  buff.append("C,");
                  break;
               default:
                  buff.append("L");
                  break;
            }
            break;
         default:
            buff.append("L");
      }
      // verti
      switch(c.fill) {
         case GridBagConstraints.VERTICAL:
            buff.append("F");
            break;
         case GridBagConstraints.BOTH:
            buff.append("F");
            break;
         case GridBagConstraints.NONE:
            switch(c.anchor) {
               case GridBagConstraints.NORTH:
               case GridBagConstraints.NORTHWEST:
               case GridBagConstraints.NORTHEAST:
                  buff.append("T");
                  break;
               case GridBagConstraints.SOUTH:
               case GridBagConstraints.SOUTHWEST:
               case GridBagConstraints.SOUTHEAST:
                  buff.append("B");
                  break;
               case GridBagConstraints.CENTER:
                  buff.append("C");
                  break;
               default:
                  buff.append("C");
                  break;
            }
            break;
         default:
            buff.append("C");
      }
      return buff.toString();
   }

   /**
    * Wird von GuiSplit überschrieben.
    * <p>
    */
   public void add(GuiMember member, GridBagConstraints constraints) {
      this.addComponent(member.getJComponent(), constraints);
      // components
      components.add(member);
   }

   /**
    * Z.B. für Border-Layout
    * 
    * @param member
    * @param constraints
    */
   public void add(GuiMember member, String constraints) {
      this.getJComponent().add(member.getJComponent(), constraints);
      // components
      components.add(member);
   }

   public void addToolbar(GuiToolbar tb) {
      if(this.toolbarPanel == null) {
         // ToolbarPanel
         FlowLayout flow = new FlowLayout(FlowLayout.LEFT);
         flow.setHgap(0);
         flow.setVgap(0);
         this.toolbarPanel = new JPanel(flow);
         this.add(toolbarPanel);
      }
      this.toolbarPanel.add(tb);
      //			if (toolBar == null)
      //				toolBar = tb;

   }

   /**
    * Fügt dem Container eine neue Daten haltende Komponente hinzu.
    * <p>
    * Diese Methode unterscheidet zwischen der Spezifikations- und der Laufzeit-Umgebung {@link GuiUtil#isAPI}.<br>
    * Wenn Laufzeitumgebung gesetzt ist, dürfen die Namen der Komponente nicht leer sein und es dürfen auch keine
    * doppelten Namen vergeben werden.
    * 
    * @throws IllegalArgumentException wenn Name leer oder doppelt.
    */
   public final void addMember(GuiMember comp) {
      if(members == null) {
         members = new HashVector(this.getName());
      }
      members.addMember(comp);
      comp.setGuiParent(this);
   }

   /**
    * Entfernt eine Komponente aus dem Container.
    * <p>
    * Wird auch beim Umbenennen von Members eingesetzt.
    * 
    * @return true, wenn erfolgreich gelöscht
    * @see GuiMember#setName
    */
   public final boolean removeMember(GuiMember member) {
      if(members != null) {
         return members.removeMember(member);
      }
      return false;
   }

   /**
    * Liefert einen Member unter dem angegebenen Namen.
    * Wirft eine IllegalArgumentException, wenn Member fehlt.
    * 
    * @see #getAction(String)
    * @see #getContainer(String)
    * @see #getGuiComponent(String)
    */
   public final GuiMember getMember(String name) {
      if(members != null) {
         return members.getMember(name, this, false);
      }
      return null;
   }

   /**
    * Liefert eine Komponente unter dem angegebenen Namen.
    * Wirft eine IllegalArgumentException, wenn Member fehlt
    * oder wenn Member keine Instanz von GuiComponent (weil Container).
    * 
    * @see #getAction(String)
    * @see #getContainer(String)
    */
   public GuiComponent getGuiComponent(String name) {
      if(members != null) {
         GuiMember comp = members.getMember(name, this, false);
         if(comp instanceof GuiComponent) {
            return (GuiComponent)comp;
         }
         throw new IllegalArgumentException("Member '" + name + "' not instanceof GuiComponent");
      }
      if(this.getGuiParent() != null)
         return this.getGuiParent().getGuiComponent(name); // 23.3.2002 Trick 17
      return null;
   }

   /**
    * Liefert eine Tabelle unter Angabe ihres Namens.
    * <p>
    * Wirft eine IllegalArgumentException, wenn Member nicht GuiTable.
    */
   public final GuiTable getGuiTable(String name) {
      final GuiComponent comp = getGuiComponent(name);
      if(comp.getGuiType() == GuiMember.GUI_TABLE) {
         return (GuiTable)comp;
      }
      throw new IllegalArgumentException("Member \"" + name + "\" not instanceof GuiTable");
   }

   /**
    * Liefert einen ChildContainer unter Angabe seines Namens
    * (auch mit Punkt-Notation). Wenn der übergebene Name leer ist,
    * wird der erste gefundene Container zurück gegeben.
    * Wirft eine IllegalArgumentException, wenn Member nicht GuiContainer.
    * 
    * @see #getAction(String)
    * @see #getGuiComponent(String)
    */
   public GuiContainer getContainer(String name) {
      if(this.components != null) {
         for(int i = 0; i < this.components.size(); i++) {
            Object o = this.components.get(i);
            if (o instanceof GuiSplit) {
               GuiSplit split = (GuiSplit)o;
               GuiMember member = split.getLeftComponent();
               if (member instanceof GuiTree) {
                  GuiTree tree = (GuiTree)member;
                  GuiContainer cont = tree.getContainer(name);
                  if (cont != null) {
                     return cont;
                  }
               }
            } else if(o instanceof GuiContainer) {
               GuiContainer myMem = (GuiContainer)o;
               if(name.length() > 0) {
                  if(myMem.getName().equalsIgnoreCase(name)) {
                     return myMem;
                  }
               }
               else {
                  return myMem;
               }
            }
         }
      }

      final GuiMember member = this.getMember(name);

      if(member.getGuiType() == GuiMember.GUI_CONTAINER) {
         return (GuiContainer)member;
      }
      throw new IllegalArgumentException("Member '" + name + "' not instanceof GuiContainer");
   }

   /**
    * Liefert die GuiChildContainer des per XML-Tag angegebenen
    * Typ an der angegebenen Position zurück. Beipiele:
    * tag = "Panel" und index = 2 sucht nach dem 3. Panel und gibt
    * es zurück, sofern vorhanden
    * tag = "Group" und index = 0 sucht nach der 1. Group und gibt
    * sie zurück, sofern vorhanden
    * 
    * @param tag
    *           XML-Tag des ChildContainers, der zurück gegeben werden soll
    * @param index
    *           Index des ChildContainers, der zurück gegeben werden soll
    * @return
    *         ChildContainer mit dem entsprechenden Index oder null,
    *         wenn kein Container mit dem übergebenen Index gefunden wurde
    * @author thomas
    */
   public GuiContainer getContainer(String tag, int index) {
      // TODO im Gegensatz zu getContainer werden hier die members
      // nicht durchsucht, evtl muss das noch nachgeholt werden
      int pos = -1;
      //String myContTag = null;
      GuiContainer myCont = null;
      if(this.components != null) {
         if(index < this.components.size()) {
            for(int i = 0; i < this.components.size(); i++) {
               Object o = this.components.get(i);
               if(o instanceof GuiContainer) {
                  myCont = (GuiContainer)o;
                  //myContTag = myCont.getTag();
                  if(myCont.getTag().equalsIgnoreCase(tag)) {
                     pos++;
                     if(pos == index) {
                        return myCont;
                     }
                  }

               }
            }
         }
      }
      return null;
   }

   /**
    * Ermittelt eine GuiComponente aus dem Components-Array, da dort
    * auch Labels enthalten sind.
    * 
    * @param name
    * @return
    */
   public GuiComponent getGuiComponentsFromComponents(String name) {
      if(this.components != null) {
         for(int i = 0; i < this.components.size(); i++) {
            Object o = this.components.get(i);
            if(o instanceof GuiComponent) {
               GuiComponent myMem = (GuiComponent)o;
               if(myMem.getName().equalsIgnoreCase(name)) {
                  return myMem;
               }
            }
            else if(o instanceof GuiContainer) {
               GuiContainer myCont = (GuiContainer)o;
               GuiComponent myComp = myCont.getGuiComponentsFromComponents(name);
               if(myComp != null) {
                  return myComp;
               }
            }
         }
      }
      return null;
   }

   public final GuiContainer getGuiContainerFromComponents(String name) {
      if(components != null) {
         for(int i = 0; i < components.size(); i++) {
            Object o = components.get(i);
            if(o instanceof GuiContainer) {
               GuiContainer myCont = (GuiContainer)o;
               if(myCont.getName().equalsIgnoreCase(name))
                  return myCont;
               GuiContainer subCont = myCont.getGuiContainerFromComponents(name);
               if(subCont != null)
                  return subCont;
            }
         }

      }
      return null;
   }

   /**
    * Ermittelt GuiTabsets aus dem Components-Array. Diese Methode wird benötigt,
    * weil GuiTabset weder GuiContainer noch GuiComponent ist, also mit den Methoden
    * 
    * @see GuiContainer#getGuiComponentsFromComponents(String)
    * @see GuiContainer#getContainer(String)
    *      nicht zugegriffen werden kann.
    * @param name, wenn name leer, dann wird das erste zurück gegeben
    * @return GuiTabset
    * @author thomas
    */
   public final GuiTabset getGuiTabsetFromComponents(String name) {
      if(this.components != null) {
         for(int i = 0; i < this.components.size(); i++) {
            Object o = this.components.get(i);
            if(o instanceof GuiTabset) {
               GuiTabset myMem = (GuiTabset)o;
               if(name.length() == 0) {
                  return myMem;
               }
               else if(myMem.getName().equalsIgnoreCase(name)) {
                  return myMem;
               }
            }

         }
      }
      return null;

   }

   public final GuiTabset getGuiTabsetFromComponents(int index) {
      if(this.components != null) {
         int pos = -1;
         for(int i = 0; i < this.components.size(); i++) {
            Object o = this.components.get(i);
            if(o instanceof GuiTabset) {
               pos++;
               GuiTabset myMem = (GuiTabset)o;
               if(pos == index) {
                  return myMem;
               }
            }

         }
      }
      return null;

   }

   /**
    * Ermittelt GuiMember mit dem angegebenen Tag aus dem Components-Array:
    * 
    * @param tag XML-Tag der Komponente, die zurück gegeben werden soll
    * @return GuiMember, das dem XML-Tag entspricht und sich im Components-Array
    *         an der entsprechenden index-Position aller GuiMember mit gleichem(!) XML-Tag
    *         befindet.
    * @see #getContainer(String, int)
    * @author thomas
    */
   public final GuiMember getGuiMemberFromComponents(String tag, int index) {
      if(this.components != null) {
         int pos = -1;
         GuiMember myMem = null;
         for(int i = 0; i < this.components.size(); i++) {
            Object o = this.components.get(i);
            if(o instanceof GuiMember) {
               myMem = (GuiMember)o;
               if(myMem.getTag() == tag) {
                  pos++;
                  if(pos == index) {
                     return myMem;
                  }
               }
            }
         }
      }
      return null;
   }

   /**
    * Liefert den OutlookBar oder null, wenn keiner vorhanden. Wenn
    * mehr als ein OutlookBar vorhanden ist, wird der erste geliefert.
    * Analoge Implementierung zu GuiWindow.getOutlookBar()
    * 
    * @see GuiWindow#getOutlookBar()
    * @see #getButtonBarFromComponents()
    *      Der ButtonBar wird über eine eigene Methode, die analog
    *      implementiert ist, geliefert.
    * @return GuiOutlookBar
    *         Outlookbar des Containers oder null, wenn keiner existiert
    * @author thomas
    */
   public GuiOutlookBar getOutlookBarFromComponents() {
      if(this.components != null) {
         Object comp = this.components.get(0);
         if(comp instanceof JPanel) {
            Object comp2 = ((JPanel)comp).getComponent(0);
            if(comp2 instanceof GuiOutlookBar) {
               return (GuiOutlookBar)comp2;
            }
         }
      }
      return null;
   }

   /**
    * Liefert den ButtonBar oder null, wenn keiner vorhanden. Wenn
    * mehr als ein ButtonBar vorhanden ist, wird der erste geliefert.
    * Analoge Implementierung zu GuiWindow.getOutlookBar()
    * 
    * @see GuiWindow#getOutlookBar()
    * @see #getOutlookBarFromComponents()
    *      Der Outlookbar wird über eine eigene Methode, die analog implementiert ist,
    *      geliefert.
    * @return GuiButtonBar
    *         ButtonBar des Containers oder null, wenn keiner existiert
    * @author thomas
    */
   public GuiButtonBar getButtonBarFromComponents() {
      if(this.components != null) {
         Object comp = this.components.get(0);
         if(comp instanceof JPanel) {
            Object comp2 = ((JPanel)comp).getComponent(0);
            if(comp2 instanceof GuiButtonBar) {
               return (GuiButtonBar)comp2;
            }
         }
      }
      return null;
   }

   /**
    * Liefert einen Baum unter Angabe seines Namens
    * (auch mit Punkt-Notation).
    * Wirft eine IllegalArgumentException, wenn Member nicht GuiTree.
    */
   public final GuiTree getGuiTree(String name) {
//      final GuiComponent comp = getGuiComponent(name);
//      if(comp.getGuiType() == GuiMember.GUI_TREE) {
//         return (GuiTree)comp;
//      }
//      throw new IllegalArgumentException("Member \"" + name + "\" not instanceof GuiTree");
      final GuiContainer cont = getGuiContainerFromComponents(name);
      if(cont.getGuiType() == GuiMember.GUI_TREE) {
         return (GuiTree)cont;
      }
      throw new IllegalArgumentException("Member \"" + name + "\" not instanceof GuiTree");
   }

   /**
    * Liefert die LinkedHashMap mit allen Komponenten dieses Panel.
    * <P>
    * Die HashMap kann Komponenten halten, die vom Typ GuiComponent, GuiTable oder GuiPanel sind.
    */
   public final LinkedHashMap<String, GuiMember> getMembers() {
      return members != null ? members.getMembers() : null;
   }

   public final Element getMemberElement() {
      Element ele = super.getMemberElement();
      Iterator<GuiMember> it = members.members();
      while(it.hasNext()) {
         GuiMember member = it.next();
         Element memEle = member.getMemberElement();
         ele.addElement(memEle);
      }
      return ele;
   }

   /**
    * Liefert true, wenn eine der Componenten dieses Container die Eigenschaft "modified" hat.
    * GuiComponents sowie ChildContainer
    * 
    * @see GuiComponent#isModified()
    * @return
    */
   public boolean isModified() {
      boolean ret = false;
      for(GuiMember member : getMembers().values()) {
         if(member instanceof GuiComponent) {
            GuiComponent comp = (GuiComponent)member;
            if(comp.isModified())
               return true;
         }
         else if(member instanceof GuiContainer) {
            GuiContainer cont = (GuiContainer)member;
            if(cont.isModified())
               return true;
         }
      }
      return ret;
   }

   /**
    * Liefert die Namen der Komponenten dieses Container in der Reihenfolge
    * ihrer Anordnung.
    */
   /*
    * public final ArrayList getMemberNames() {
    * return members != null ? members.getMemberNames(): null;
    * }
    */
   /**
    * Liefert das erste Objekt der Klasse GuiComponent
    * oder null, wenn keines vorhanden.
    * <p>
    * Nach einem reset() erhält dieses Widget den Focus.
    * 
    * @return
    */
   public GuiComponent getFirstComponent() {
      GuiComponent comp = null;
      if(members == null || members.size() == 0) {
         return null;
      }
      Iterator<GuiMember> it = members.members();
      while(it.hasNext()) {
         GuiMember member = it.next();
         if(member.getGuiType() == GuiMember.GUI_COMPONENT) {
            comp = (GuiComponent)member;
            if(comp.getJComponent() != null) { // Hidden überlesen! / 20.1.2004 / PKÖ
               return comp;
            }
         }
      }
      return null;
   }

   /**
    * Fügt eine anklickbare Komponente hinzu (MenuItem, Tool, Button).<BR>
    * Die Komponente muß je Container einen eindeutigen Namen haben.
    * 
    * @see #getAction
    */
   public final void addAction(GuiAction comp) {
      if(actions == null) {
         actions = new HashVector(this.getName());
      }
      comp.checkEmptyName();
      actions.addMember(comp);
      comp.setGuiParent(this);
   }

   /**
    * Liefert eine Aktion unter dem angegebenen Namen.
    * Wirft eine IllegalArgumentException, wenn Item oder Button fehlt.
    * 
    * @see #getGuiComponent(String)
    * @see #getAction(String)
    */
   public final GuiAction getAction(String name) {
      // Punktnotation wenn der Parent keine Actions hat!
      if(actions != null) {
         return (GuiAction)actions.getMember(name, this, true);
      }
      else if(members != null && name.indexOf(".") != -1) {
         GuiMember mem = members.getMember(name, this, true);
         if(mem instanceof GuiAction) {
            return (GuiAction)mem;
         }
      }
      return null;
   }

   /**
    * Liefert einen Iterator über alle definierten Actions.
    * <p>
    * Der Iterator kann leer sein, ist aber niemals null.
    * 
    * @return
    */
   Iterator<GuiMember> getActions() {
      if(actions == null) {
         return Collections.EMPTY_LIST.iterator();
      }
      return actions.members();
   }

   /**
    * Führt die Aktion mit dem angegebenen Namen aus.
    * So, als wäre das Menü betötigt oder der Button gedrückt worden.
    * Wirft eine IllegalArgumentException, wenn die Action mit dem Namen fehlt.
    * 
    * @param name
    */
   public final void doAction(String name) {
      GuiAction action = this.getAction(name);
      if(action == null) {
         throw new IllegalArgumentException("Missing action: " + name);
      }
      action.click();
   }

   /**
    * En- oder disabled alle Menüitems und Buttons
    */
   public final void enableAllActions(boolean b) {
      if(actions != null) {
         for(Iterator<GuiMember> i = actions.members(); i.hasNext();) {
            GuiAction action = (GuiAction)i.next();
            action.setEnabled(b);
         }
      }
   }
   
   public boolean removeAction(GuiAction action) {
      boolean b = actions.removeMember(action);
      return b;
   }

   /**
    * Liefert eine ArrayList von GuiElements die enabled sind.
    * 
    * @return
    */
   public ArrayList<GuiElement> getEnabledComponents() {
      ArrayList<GuiElement> al = null;
      for(Iterator<Map.Entry<String, GuiMember>> i = members.getMembers().entrySet().iterator(); i.hasNext();) {
         Map.Entry<String, GuiMember> entry = i.next();
         GuiMember member = entry.getValue();
         if(member instanceof GuiElement) {
            GuiElement comp = (GuiElement)member;
            if(comp.isEnabled()) {
               if(al == null) {
                  al = new ArrayList<GuiElement>();
               }
               al.add(comp);
            }
         }
      }
      return al;
   }

   /**
    * Aktiviert oder deaktiviert die übergebene Menge von GuiElements.
    * 
    * @param comps Eine ArrayList von GuiElements.
    * @param b Enable oder disable
    */
   public final void setEnabled(ArrayList<GuiElement> comps, boolean b) {
      if(comps == null) {
         return;
         //throw new IllegalArgumentException("GuiContainer#setEnabled: Argument null");
      }
      for(GuiElement element : comps) {
         element.setEnabled(b);
      }
   }

   /**
    * Aktiviert oder deaktiviert die übergebene Menge von Members
    * (GuiElement).
    * 
    * @param comps Ein HashSet von GuiElements oder null, wenn alle.
    * @param b Enable oder disable
    */
   public final void setEnabled(HashSet<String> comps, boolean b) {
      for(Iterator<Map.Entry<String, GuiMember>> i = members.getMembers().entrySet().iterator(); i.hasNext();) {
         Map.Entry<String, GuiMember> entry = i.next();
         GuiMember member = entry.getValue();
         if(member instanceof GuiElement) {
            GuiElement element = (GuiElement)member;
            if(comps != null && comps.contains(member.getName())) {
               element.setEnabled(b);
            }
            else {
               element.setEnabled(!b);
            }
         }
      }
   }

   /**
    * Liefert alle Komponenten, die in der Oberfläche als Suchfelder
    * gekennzeichnet sind (search="y"), und die vom Anwender auch
    * ausgefüllt wurden. Dient vor allem dazu, eine Datenbankabfrage
    * zu formulieren.
    * <p>
    * Der Vector enthält nur Komponenten, die bei denen isSearch() true ergibt.
    * 
    * @param ret Ein Vector, an den die Eintrage angehängt werden oder null.
    * @return Einen Vector von GuiMembers
    * @see GuiComponent#isSearch()
    */
   public final Vector<GuiComponent> getSearchables(Vector<GuiComponent> ret) {
      if(ret == null) {
         ret = new Vector<GuiComponent>();
      }
      if(members.size() == 0) {
         return null;
      }
      Iterator<GuiMember> it = members.members();
      while(it.hasNext()) {
         GuiMember member = it.next();
         if(member.getGuiType() == GuiMember.GUI_COMPONENT) {
            GuiComponent comp = (GuiComponent)member;
            if(comp.isSearch()) {
               if(comp.getValue() != null && comp.getValue().toString().length() > 0) {
                  ret.addElement(comp);
               }
            }
         }
         else if(member.getGuiType() == GuiMember.GUI_CONTAINER) {
            ret = ((GuiContainer)member).getSearchables(ret);
         }// else if(member.getGuiType() == GuiMember.GUI_TABLE) {
          // ret = ((GuiTable)member).getSearchables(ret); getSearchables für GuiTable tut nicht
          // }
      } // End For
      return ret;
   }

   /**
    * Ruft den InputVerifier der Componenten von diesem Container auf.
    * ChildContainer werden mit überprüft.
    * 
    * @throws IllegalStateException, wenn Eingaben unzulässig.
    */
   public final void verify(boolean checkNN) throws IllegalStateException {
      if(members != null) {
         for(Iterator<GuiMember> i = members.members(); i.hasNext();) {
            GuiMember member = i.next();
            try {
               member.verify(checkNN);
            }
            catch(IllegalStateException ex) {
               member.getJComponent().requestFocus();
               throw ex;
            }
         }
      }
   }

   /**
    * Setzt den Feldinhalt aller Komponenten dieses Panels zurück (auf leer).
    * <p>
    * Wird von GuiTab überschrieben; daher nicht final.
    * <p>
    * 
    * @see GuiMember#reset
    */
   @Override
   public void reset() {
      if(members != null) {
         for(Iterator<GuiMember> i = members.members(); i.hasNext();) {
            GuiMember member = i.next();
            member.reset();
         }
      }
      enableAllActions(true);
   }

   /**
    * Liefert den Wert eine Komponente unter Angabe ihres Namens (auch mit Punkt-Notation).
    * <P>
    * Der Rückgabewert ist zumeist vom Typ "String"; bei Tabellen ein Vector von Vectoren, bei CheckBoxen ein "Boolean".
    * 
    * @param componentName Name der Komponente
    */
   public final Object getValue(String name) {
      return this.getGuiComponent(name).getValue();
   }

   /**
    * Setzt den Wert eine Komponente unter Angabe ihres Namens (auch mit Punkt-Notation).
    * 
    * @param name Name der Komponente
    * @param value In der Regel kann ein String verwendet werden; Tabellen
    *           erwarten einen Vector von Vectoren.
    */
   public final void setValue(String name, Object value) {
      this.getGuiComponent(name).setValue(value);
   }

   /**
    * Liefert eine HashMap mit den Namen und den Inhalten aller
    * Komponenten, die vom Benutzer modifiziert wurden. <br>
    * ChildPanels werden mit geliefert.
    * 
    * @return Eine HashMap oder null, wenn keine Komponenten geändert
    *         wurde.
    * @see #setAllValues
    * @see GuiComponent#isModified
    */
   public final LinkedHashMap<String, Object> getAllModifiedValues() {
      return this.getAllValues(true);
   }

   /**
    * Liefert eine HashMap mit den Namen und den Inhalten aller
    * Komponenten. <br>
    * ChildPanels werden rekursiv mit geliefert.
    * 
    * @return Eine HashMap oder null, wenn keine Komponenten vorhanden.
    * @see #setAllValues
    */
   public final LinkedHashMap<String, Object> getAllValues() {
      return this.getAllValues(false);
   }

   /**
    * Liefert die Werte des Containers und seiner Kinder.
    * TODO : GuiTree liefert nix!
    * 
    * @param modified Wenn "true", werden nur die geänderten Komponenten geliefert;
    *           ansonsten alle.
    */
   public final LinkedHashMap<String, Object> getAllValues(boolean modified) {
      if(this.members.size() == 0) {
         return null;
      }
      final LinkedHashMap<String, Object> ret = new LinkedHashMap<String, Object>(this.members.size());
      for(Iterator<GuiMember> it = members.members(); it.hasNext();) {
         GuiMember member = it.next();
         if(member.getGuiType() == GuiMember.GUI_COMPONENT) {
            GuiComponent comp = (GuiComponent)member;
            if(modified == false || comp.isModified() == true) {
               if(comp.getValue() != null) {
                  ret.put(member.getName(), comp.getValue());
               }
            }
         }
         else if(member.getGuiType() == GuiMember.GUI_TABLE) {
            Vector<GuiTableRow> tableValues = ((GuiTable)member).getAllValues(modified);
            if(tableValues != null) {
               ret.put(member.getName(), tableValues);
            }
         }
         else if(member.getGuiType() == GuiMember.GUI_CONTAINER) {
            LinkedHashMap<String, Object> hp = ((GuiContainer)member).getAllValues(modified);
            if(hp != null) {
               ret.put(member.getName(), hp);
            }
         }
         else if(member.getGuiType() == GuiMember.GUI_TREE) {
            // wie nur?
         }
      }
      if(ret.size() == 0) {
         return null;
      }
      return ret;
   }

   /**
    * Es wird eine HashMap übergeben, die die Namen von Komponenten
    * und deren zu setzenden Inhalt enthält.
    * 
    * @see #getAllValues
    */
   public final void setAllValues(HashMap<String, Object> hash) {
      if(hash == null) {
         return;
      }
      Object value = null;
      for(Iterator<Map.Entry<String, Object>> i = hash.entrySet().iterator(); i.hasNext();) {
         Map.Entry<String, Object> entry = i.next();
         String name = entry.getKey();
         value = entry.getValue();
         if(value instanceof HashMap<?, ?>) {
            HashMap<String, Object> childHash = (HashMap<String, Object>)value;
            GuiContainer childPanel = getContainer(name);
            childPanel.setAllValues(childHash);
         }
         else {
            setValue(name, value);
         }
      }
   }

   /**
    * Setzt mehrere Komponenten dieses Panels auf den Werte der (public) Attribute
    * des übergebenen Objects. <BR>
    * Auch die Klasse des Objektes muß public sein. <BR>
    * Hierbei müssen die Namen der Attributes der Klassen den Namen der
    * Oberflächen-Komponenten entsprechen. Auf Groß- und Kleinschreibung ist zu
    * achten. <BR>
    * Auf alle Attribute wird die Methode toString() angewendet.
    * Voraussetzung ist, daß die angegebenen Komponenten einen String als
    * Wert akzeptieren; also z.B. keine Tabellen.
    * 
    * @param obj Ein Object dessen Attributnamen mit den Namen der
    *           Komponenten übereinstimmen.
    */
   public final void setFieldValues(Object obj) {
      Class<?> cls = obj.getClass();
      Field[] fs = cls.getFields();
      for(int i = 0; i < fs.length; i++) {
         Field f = fs[i];
         try {
            String val = null;
            if(f.get(obj) != null) {
               val = f.get(obj).toString();
            }
            this.setValue(f.getName(), val);
         }
         catch(Exception ex) {
            GuiUtil.showEx(ex);
         }
      }
   }

   /**
    * Liefert den Inhalt dieses Containers als XML-Dokument.
    * Ggf. vorhandene ChildContainer werden mitgeliefert.
    * Es werden nur die Feldinhalte geliefert der nicht null sind.<br>
    * Das XML-Dokument entspricht "guivalues.dtd".
    * 
    * @return Ein XML-Document oder null, wenn kein ParentContainer
    */
   public Document getAllValuesXml() {
      GuiMember member;
      // Null?
      if(members == null || members.size() == 0)
         return null;

      // Create Document
      Document doc = new Document();
      doc.setEncoding("UTF-8");
      // Create Root Element
      Element rootNode = doc.setRoot("Panel");
      rootNode.setAttribute("name", getName());
      if(this.getOid() != -1) {
         rootNode.setAttribute(OID, Long.toString(this.getOid()));
      }
      // Loop members
      for(Iterator<GuiMember> i = members.members(); i.hasNext();) {
         member = i.next();
         // Je nach Typ
         switch(member.getGuiType()) {
            case GuiMember.GUI_COMPONENT: {
               GuiComponent comp = (GuiComponent)member;
               if(comp.getValue() != null) {
                  Element elementNode = new Element("Component");
                  elementNode.setAttribute("name", member.getName());
                  elementNode.setAttribute("type", GuiComponent.getDataTypeName(comp.getDataType()));
                  if(comp.getOid() != -1) {
                     elementNode.setAttribute(OID, Long.toString(comp.getOid()));
                  }
                  if(comp.isModified()) {
                     elementNode.setAttribute("modified", "true");
                  }
                  Text text = null;
                  if(comp instanceof GuiMultiLine) {
                     text = new CData(comp.getValue().toString());
                  }
                  else {
                     text = new Text(comp.getValue().toString());
                  }
                  if(text.getString().length() > 0) {
                     rootNode.addElement(elementNode);
                     elementNode.addText(text);
                  }
               } // End If
            } // End Case
               break;

            case GuiMember.GUI_CONTAINER: {
               GuiContainer childPanel = (GuiContainer)member;
               Document childDoc = childPanel.getAllValuesXml();
               if(childDoc != null) {
                  // Wurzel umhängen
                  Element childRoot = childDoc.getRoot();
                  rootNode.addElement((Element)childRoot.clone());
               }
            }
               break;

            case GuiMember.GUI_TABLE: {
               GuiTable tbl = (GuiTable)member;
               Document childDoc = tbl.getAllValuesXml();
               // Wurzel umhängen
               Element childRoot = childDoc.getRoot();
               rootNode.addElement((Element)childRoot.clone());
            }
               break;

            case GuiMember.GUI_TREE: {
               GuiTree tree = (GuiTree)member;
               Document childDoc = tree.getAllValuesXml();
               // Wurzel umhängen
               Element childRoot = childDoc.getRoot();
               rootNode.addElement((Element)childRoot.clone());
            }
               break;
         } // End Switch
      } // end for
      return doc;
   }

   /**
    * Setzt den Inhalt dieses Container mit einem XmlDocument.
    * Ggf. im Dokument enthaltene ChildContainer werden mit versorgt.
    * <p>
    * Das XML-Dokument muß "guivalues.dtd" entsprechen.
    */
   public final void setAllValuesXml(Document doc) {
      if(doc != null) {
         Element root = doc.getRoot();
         if(root != null) {
            setAllValuesXml(root);
         }
      }
   }

   /**
    * Setzt den Inhalte der Komponenten dieses Panel mit den Werten
    * des übergebenen XML-Knotens.
    * 
    * @throws IllegalArgumentException wenn ElementName != "Panel"
    */
   public void setAllValuesXml(Element node) {
      if(node.getName().equals("Panel") == false) {
         throw new IllegalArgumentException("NodeName != Panel: " + node.getName());
      }
      // oid
      final String oid = node.getAttributeValue(OID);
      if(oid != null) {
         this.setOid(Convert.toLong(oid));
      }
      Elements list = node.getElements();
      String compName = null;
      Element child;
      while(list.hasMoreElements()) {
         child = list.next();
         String nodeName = child.getName();
         compName = child.getAttributeValue("name");
         if(nodeName.equals("Component")) {
            // oid
            final String coid = child.getAttributeValue(OID);
            if(coid != null) {
               this.getMember(compName).setOid(Convert.toLong(coid));
            }
            String value = child.getTextString();
            setValue(compName, value);
         }
         else if(nodeName.equals("Panel")) {
            GuiContainer panel = getContainer(compName);
            panel.setAllValuesXml(child);
         }
         else if(nodeName.equals("Table")) {
            GuiTable tbl = getGuiTable(compName);
            tbl.setAllValuesXml(child);
         }
         else if(nodeName.equals("Tree")) {
            GuiTree tree = getGuiTree(compName);
            tree.setAllValuesXml(child);
         }
         else {
            System.out.println("Illegal NodeName: " + nodeName);
         }
      }
   }

   /**
    * @deprecated
    *             Es werden nur die Felder upgedated, wenn sie leer sind.
    */
   public final void updateIfNull(Element node) {
      if(node.getName().equals("Panel") == false) {
         throw new IllegalArgumentException("NodeName != Panel: " + node.getName());
      }
      Elements list = node.getElements();
      String compName = null;
      Element child;
      while(list.hasMoreElements()) {
         child = list.next();
         String nodeName = child.getName();
         compName = child.getAttributeValue("name");
         if(nodeName.equals("Component")) {
            try {
               GuiComponent comp = this.getGuiComponent(compName);
               Object val = comp.getValue();
               if(val != null) {
                  if(val.toString().length() == 0)
                     val = null;
               }
               if(val == null) {
                  // oid
                  final String coid = child.getAttributeValue(OID);
                  if(coid != null) {
                     comp.setOid(Convert.toLong(coid));
                  }
                  String value = child.getTextString();
                  comp.setValue(value);
               }
            }
            catch(Exception ex) {
               // nix; missing component
            }
         }
         else if(nodeName.equals("Panel")) {
            GuiContainer panel = getContainer(compName);
            panel.updateIfNull(child);
         }
         else if(nodeName.equals("Table")) {
            //GuiTable tbl = getGuiTable(compName);
            //tbl.setAllValuesXml(child);
         }
         else if(nodeName.equals("Tree")) {
            //GuiTree tree = getGuiTree(compName);
            //tree.setAllValuesXml(child);
         }
         else {
            System.out.println("Illegal NodeName: " + nodeName);
         }
      }
   }

   public void setDatasetValues(JDataSet ds) {
      this.dataset = ds; // merken für getDatasetValues
      if(members == null)
         return;
      for(Iterator<Map.Entry<String, GuiMember>> i = members.getMembers().entrySet().iterator(); i.hasNext();) {
         Map.Entry<String, GuiMember> entry = i.next();
         GuiMember member = entry.getValue();
         if(member instanceof IDatasetComponent) {
            IDatasetComponent comp = (IDatasetComponent)member;
            if(!comp.isRootElement()) // bubi: root-elemente werden nicht bestückt
            {
               comp.setDatasetValues(ds);
            }
         }
      }
   }

   public JDataSet getDatasetValues() {
      this.getDatasetValues(this.dataset);
      return this.dataset;
   }

   public void getDatasetValues(JDataSet ds) {
      if(members == null)
         return;
      for(Iterator<Map.Entry<String, GuiMember>> i = members.getMembers().entrySet().iterator(); i.hasNext();) {
         Map.Entry<String, GuiMember> entry = i.next();
         GuiMember member = entry.getValue();
         if(member instanceof IDatasetComponent) {
            IDatasetComponent comp = (IDatasetComponent)member;
            if(!comp.isRootElement()) // bubi: wenn Komponente NICHT eigenes JDataSet verwaltet
            {
               comp.getDatasetValues(ds);
            }
         }
      }
   }

   public void commitChanges() {
      if(members == null)
         return;
      for(Iterator<Map.Entry<String, GuiMember>> i = members.getMembers().entrySet().iterator(); i.hasNext();) {
         Map.Entry<String, GuiMember> entry = i.next();
         GuiMember member = entry.getValue();
         if(member instanceof IDatasetComponent) {
            IDatasetComponent comp = (IDatasetComponent)member;
            comp.commitChanges();
         }
      }
   }

   /**
    * Liefert "true" wenn dieses Panel zur Schachtelung der
    * Componenten verwendet wird bzw. "false", wenn es allein dem Layout dient.
    * <p>
    * Achtung!<br>
    * Diese Methode liefert immer dann true, wenn dem Container ein Member zugewiesen wurde.<br>
    * Sie liefert false, wenn addMember nie aufgerufen wurde. Dieses ist folglich auch bei neuen Panels der Fall, denen
    * später noch Members zugewiesen werden sollen!
    * 
    * @see #addMember
    */
   public final boolean isParentContainer() {
      if(members == null) {
         return false;
      }
      else {
         return true;
      }
   }

   /**
    * Fügt dem Panel einen Radiobutton hinzu.<BR>
    * Je Panel kann nur eine Gruppe von RadioButtons definiert werden.
    * <p>
    * Beim ersten RadioButton wird die OptionGroup als Member bei diesem Container registriert.
    * <p>
    * OptionGroup und RadioButton werden wechselseitig miteinander verknüpft.
    * 
    * @return true, wenn eine OptionGroup erstellt wurde
    *         (also beim ersten RadioButton für diesem Container).
    * @see GuiOptionGroup
    */
   /*
    * public final boolean addOption(GuiOption opt) {
    * boolean ret = false;
    * if (optionGroup == null) {
    * optionGroup = new GuiOptionGroup(opt.getName());
    * optionGroup.setMsgChange(opt.getMsgChange());
    * ret = true;
    * //opt.setSelected(true); // wegen EWW
    * //this.addMember(optionGroup); // falsch! muß der ParentContainer sein!
    * }
    * optionGroup.add(opt);
    * //opt.setOptionGroup(optionGroup);
    * //opt.setGuiParent(this);
    * return ret;
    * }
    */
   final void dispose() {
      this.stopTimers();
      if(members != null) {
         for(Iterator<GuiMember> i = members.members(); i.hasNext();) {
            GuiMember member = i.next();
            member.setUserObject(null);
            switch(member.getGuiType()) {
               case GUI_CONTAINER:
                  ((GuiContainer)member).dispose();
                  break;
               case GUI_COMPONENT:
                  if(member.getPopupMenu() != null) {
                     member.setPopupMenu(null);
                  }
                  break;
               case GUI_TABLE:
                  GuiTable tbl = (GuiTable)member;
                  tbl.dispose();
                  break;
            }
         }
      }
      if(actions != null) {
         for(Iterator<GuiMember> i = actions.members(); i.hasNext();) {
            GuiAction action = (GuiAction)i.next();
            action.dispose();
         }
      }
      this.setController(null);
      this.context = null;
      this.actions = null;
      this.members = null;
      this.setLayoutManager(NULL);
      this.removeAll();
   }

   abstract void removeAll();

   /**
    * Setzt den Pnuts Context
    * 
    * @see GuiFactory#perfScript
    */
   final void setContext(GuiScripting c) {
      context = c;
   }

   final GuiScripting getContext() {
      if(this.context != null) {
         return this.context;
      }
      else {
         if(this.getGuiParent() != null) {
            return this.getGuiParent().getContext();
         }
      }
      return null;
   }

   final void setLinkTable(GuiTable tbl) {
      linkTable = tbl;
      if(tbl != null) {
         tbl.setLinkPanel(this);
      }
   }

   final GuiTable getLinkTable() {
      return linkTable;
   }

   /**
    * @return
    */
   public String getColSpec() {
      return colSpec;
   }

   /**
    * @return
    */
   public String getRowSpec() {
      return rowSpec;
   }

   /**
    * @param string
    */
   void setColSpec(String string) {
      colSpec = string;
      if(this.getLayoutManager() == GRID) {
         int hGap = Convert.toInt(colSpec);
         GridLayout grid = (GridLayout)this.getJComponent().getLayout();
         grid.setHgap(hGap);
      }
      else if(this.getLayoutManager() == FLOW) {
         int hGap = Convert.toInt(colSpec);
         FlowLayout flow = (FlowLayout)this.getJComponent().getLayout();
         flow.setHgap(hGap);
      }
   }

   /**
    * @param string
    */
   void setRowSpec(String string) {
      rowSpec = string;
      if(this.getLayoutManager() == GRID) {
         int vGap = Convert.toInt(rowSpec);
         GridLayout grid = (GridLayout)this.getJComponent().getLayout();
         grid.setVgap(vGap);
      }
      else if(this.getLayoutManager() == FLOW) {
         int vGap = Convert.toInt(rowSpec);
         FlowLayout flow = (FlowLayout)this.getJComponent().getLayout();
         flow.setVgap(vGap);
      }
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

   //	 /**
   //		* Gibt an, ob dieser Container sein eigenes JDataSet verwaltet.
   //		* &Uuml;berschreibt GuiComponent.isRootElement(). <br>
   //		* <i>Indicates that this Container administers its own JDataSet. </i>
   //		*
   //		* @see de.guibuilder.framework.IDatasetMember#isRootElement()
   //		*/
   //	public boolean isRootElement() {
   //		if (this.elementName != null && this.elementName.startsWith("ROOT:")) {
   //				return true;
   //		} else {
   //			return false;
   //		}
   //	}

   /**
    * überschrieben von GuiMember; setzt auch den Status
    * aller Kind-Elemente dieses Containers.
    * 
    * @see GuiMember#setStateAttributes(String)
    */
   public void setStateAttributes(String state) {
      super.setStateAttributes(state);
      if(actions != null) {
         for(Iterator<GuiMember> i = actions.members(); i.hasNext();) {
            GuiAction action = (GuiAction)i.next();
            action.setStateAttributes(state);
         }
      }
      if(members != null) {
         for(Iterator<GuiMember> i = members.members(); i.hasNext();) {
            GuiMember member = i.next();
            member.setStateAttributes(state);
         }
      }
   }

   public void addTimer(GuiTimer t) {
      if(timers == null) {
         timers = new ArrayList<GuiTimer>();
      }
      timers.add(t);
   }

   public ArrayList<GuiTimer> getTimers() {
      return timers;
   }

   public GuiTimer getTimer(String actionCommand) {
      if(timers == null || actionCommand == null) {
         return null;
      }
      ArrayList<GuiTimer> al = getTimers();
      for(GuiTimer t : al) {
         if(actionCommand.equalsIgnoreCase(t.getActionCommand())) {
            return t; // Der Erste hat Recht
         }
      }
      return null;
   }
   /**
    * @formatter:off
    * Startet die Timer (wenn enabled)
    * @formatter:on
    */
   public void startTimers() {
      ArrayList<GuiTimer> al = getTimers();
      if(al == null)
         return;
      for(GuiTimer t : al) {
         t.setActive(true);
      }
   }

   public void stopTimers() {
      ArrayList<GuiTimer> al = getTimers();
      if(al == null)
         return;
      for(GuiTimer t : al) {
         t.setActive(false);
      }
   }
   /**
    * @formatter:off
    * @param b
    * @formatter:on
    */
   public void setTimersActive(boolean b) {
      if(b) {
         this.startTimers();
      }
      else {
         this.stopTimers();
      }
   }
   public void setTimersEnabled(boolean b) {
      ArrayList<GuiTimer> al = getTimers();
      if(al == null)
         return;
      for(GuiTimer t : al) {
         t.setEnabled(b);
      }
   }

   /**
    * @formatter:off
    *                Window (de)iconified
    * @param b
    * @formatter:on
    */
   void setTimersTempDisabled(boolean b) {
      ArrayList<GuiTimer> al = getTimers();
      if(al == null)
         return;
      for(GuiTimer t : al) {
         t.setTempDisabled(b);
      }
   }

   public void getPreferences(JDataSet ds) {
      // Components
      LinkedHashMap<String, GuiMember> comps = this.getMembers();
      if(comps != null) {
         for(GuiMember mem : comps.values()) {
            mem.getPreferences(ds);
         }
      }
      for(Object comp : components) {
         if(comp instanceof GuiSplit) {
            GuiSplit split = (GuiSplit)comp;
            split.getPreferences(ds);
         }
      }
      if(this instanceof GuiPanel) {
         GuiPanel panel = (GuiPanel)this;
         GuiDesktop desk = panel.getDesktop();
         if(desk != null) {
            JInternalFrame[] frames = desk.getAllFrames();
            for(int i = 0; i < frames.length; i++) {
               GuiInternalFrameImpl fimpl = (GuiInternalFrameImpl)frames[i];
               fimpl.getPreferences(ds);
            }
         }
      }
   }

   public void setPreferences(JDataSet ds) {
      // Components
      LinkedHashMap<String, GuiMember> comps = this.getMembers();
      if(comps != null) {
         for(GuiMember mem : comps.values()) {
            mem.setPreferences(ds);
         }
      }
      for(Object comp : components) {
         if(comp instanceof GuiSplit) {
            GuiSplit split = (GuiSplit)comp;
            split.setPreferences(ds);
         }
      }
      if(this instanceof GuiPanel) {
         GuiPanel panel = (GuiPanel)this;
         GuiDesktop desk = panel.getDesktop();
         if(desk != null) {
            JInternalFrame[] frames = desk.getAllFrames();
            for(int i = 0; i < frames.length; i++) {
               GuiInternalFrameImpl fimpl = (GuiInternalFrameImpl)frames[i];
               fimpl.setPreferences(ds);
            }
         }
      }
   }
}