package de.guibuilder.framework;

import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.guibuilder.framework.event.GuiTabSelectionEvent;

/**
 * Implementierung eines Containers für Registerkarten.
 * <P>
 * Die Karten müssen in der gewünschten Reihenfolge mit der Methode
 * <code>addTab(GuiTab)</code> nacheinander hinzugefügt werden. <BR>
 * Beispielcode: <BR>
 * <PRE>
 * GuiTabset tabset = new GuiTabset(); // Erzeugen einer Karte mit Beschriftung
 * GuiTab karteAdresse = new GuiTab("Adresse"); tabset.addTab(karteAdresse); //
 * Erzeugen einer Karte mit nachträglichem Setzen der Beschriftung GuiTab
 * karteBank = new GuiTab(); karteBank.setTitle("Bankverbindung");
 * tabset.addTab(karteBank);
 * </PRE>
 * @see GuiTab
 */
public class GuiTabset extends JTabbedPane implements MemberAble {
   private static final long serialVersionUID = 1L;

   // Attributes
   /**
    * Menge der Registerkarten zu diesem Tabset; Als Key wird der Name des Panel
    * verwendet. Wird für den ChangeListener benötigt, sowie für die Methode
    * getTab(String name);
    */
   private LinkedHashMap<String, GuiTab> myTabs = new LinkedHashMap<String, GuiTab>();

   private String label;

   private String msgActive;

   private transient GuiTab currentTab;

   // Constructors
   /**
    * Erzeugt einen Registerkartensatz mit dem Defaultnamen "tabset".
    */
   public GuiTabset() {
      super();
      this.setName("tabset");
      this.guiInit();
   }

   /**
    * Erzeugt einen Registerkartensatz mit einem definierten Name. <BR># Es
    * wird ein ChangeListener eingerichtet, der das Wechseln der selektierten
    * Registerkarte an RootPane und den Controller weiter leitet.
    * 
    * @see GuiRootPane#obj_TabOpen
    */
   public GuiTabset(String title) {
      super();
      label = title;
      if (title.length() == 0) {
         this.setName("tabset");
      }
      this.guiInit();
   }

   // Methods
   /**
    * Initialisierung durch den Constructor. <BR>
    * Create nested ChangeListener.
    */
   private void guiInit() {
      this.addChangeListener(new ChangeListener() {
         /**
          * ChangeListener
          * <p>
          * <ul>
          * <li>Setzt currentTabset
          * <li>Setzt currentTab
          * <li>Löst TabSelectionEvent aus
          * </ul>
          * 
          * @see GuiRootPane#getCurrentTabset
          * @see GuiTabset#getCurrentTab
          * @see de.guibuilder.framework.event.GuiTabSelectionEvent
          */
         public void stateChanged(ChangeEvent e) {
            JPanel panel = (JPanel) getSelectedComponent();
            if (panel != null) {
               GuiTab tab = myTabs.get(panel.getName());
               GuiRootPane pane = (GuiRootPane) getRootPane();
               if (pane != null) {
                  pane.setCurrentTabset(GuiTabset.this);
                  setCurrentTab(tab);
                  // Externen Controller informieren
                  // 1. Tab selbst
                  if (tab.getMsgActive() != null) {
                    // Event
                    GuiTabSelectionEvent event = new GuiTabSelectionEvent(
                          getGuiWindow(), tab, tab.getMsgActive(), tab.getTabIndex());
                    pane.obj_TabOpen(tab, tab.getMsgActive(), event);
                  }
                  // 2. Tabset
                  if (msgActive != null) {
                     // Event
                     GuiTabSelectionEvent event = new GuiTabSelectionEvent(
                           getGuiWindow(), tab, msgActive, tab.getTabIndex());
                     pane.obj_TabOpen(tab, msgActive, event);
                  }
               } else { // RootPane null; passiert nur beim ersten Anzeigen
                  System.out
                        .println("GuiTabset ChangeListener.stateChanged: Missing GuiRootPane!");
               }
            }
         }
      });
   }

   public final String getTag() {
      return "Tabset";
   }

   public final String getLabel() {
      return label;
   }

   public final void setLabel(String title) {
      this.label = title;
   }

   /**
    * Setzt das ActionCommand, welches beim TabSelectionEvent geliefert wird.
    * 
    * @see de.guibuilder.framework.event.GuiTabSelectionEvent
    */
   public final void setMsgActive(String s) {
      msgActive = s;
   }

   public final String getMsgActive() {
      return msgActive;
   }

   /**
    * Fügt dem Tabset eine Registerkarte hinzu.
    * <p>
    * Die erste Registerkarte wird selektiert. <BR>
    * Die Registerkarte muß zuvor mit einem entsprechenden Constructor erzeugt
    * worden sein. <BR>
    * Die Karten sollten einen eindeutigen Namen haben; ansonsten wird eine
    * Warnung ausgegeben. <br>
    * Die Registerkarte wird gleichzeitig mit diesem Registerkartensatz
    * verknüpft.
    * <p>
    * TODO : Geht schief, wenn Factory.createPanel verwendet wird.
    * 
    * @param tab
    *           Eine Registerkarte
    */
   public final void addTab(GuiTab tab) {
      // 1. Verknüpfung Tabset <--> Tab (muß zuerst erfolgen wegen stateChanged)
      GuiTab prev = myTabs.put(tab.getName(), tab);
      if (prev != null) {
         System.err.println("Warning: Duplicate TabName: " + tab.getName());
      }
      tab.setGuiTabset(this, this.getTabCount());
      // 2. Als zweites bei JTabbedPane einhängen
      if (tab.getIcon() == null) {
         this.addTab(tab.getLabel(), tab.getJComponent());
      } else {
         this.addTab(tab.getLabel(), tab.getIcon(), tab.getJComponent());
      }
      // 3. ToolTipText umschaufeln
      if (tab.getToolTipText() != null) {
         this.setToolTipTextAt(tab.getTabIndex(), tab.getToolTipText());
         tab.setToolTipText(null);
      }
      // 4. Erste Karte selektieren
      if (this.getTabCount() == 1) {
         this.setSelectedIndex(0);
      }
   }

   /**
    * Liefert die Registerkarte mit dem angegebenen Namen. Enthält der übergebene Name Großbuchstaben,
    * werden diese in Kleinbuchstaben umgewandelt. Wenn eine leere Zeichenkette
    * übergeben wird, wird das CurrentTab zurück gegeben.
    * @see #currentTab
    * 
    * @throws IllegalArgumentException
    *            wenn Karte nicht existiert.
    */
   public final GuiTab getTab(String name) {
      GuiTab ret = null;
      
      if (name.length() != 0) {
	      ret = myTabs.get(name);
      } else {
    	  ret = getCurrentTab();
      }
      
      if (ret == null) {
	         throw new IllegalArgumentException("Tabset#getTab: Missing Tab: "
	               + name);
	  }      
      return ret;
   }
   /**
    * Wenn sich der Name einer Registerkarte ändert,
    * muß auch der Eintrag in der LinkedHashMap geändert werden.
    * @see de.guibuilder.framework.GuiTab#setName
    * @param oldName
    * @param newName
    */
   final void renameTab(String oldName, String newName) {
      GuiTab tab = this.myTabs.remove(oldName);
      if (tab == null) {
         throw new IllegalArgumentException("GuiTabset#renameTab; Missing GuiTab: " + oldName);
      }
      GuiTab prev = myTabs.put(newName, tab);
      if (prev != null) {
         System.err.println("Warning: Duplicate TabName: " + tab.getName());
      }
   }
   /**
    * Wird vom Change-Listner bei State-Changed gesetzt.<p>
    * Wenn auf dieser Karte eine Tabelle liegt, wird currentTable gesetzt.
    * @param tab
    */
   private void setCurrentTab(GuiTab tab) {
      this.currentTab = tab;
      if (currentTab.getMembers() != null) {
	      for (GuiMember mem: currentTab.getMembers().values()) {
	      	if (mem instanceof GuiTable) {
            if (getRootPane() != null) {
              GuiRootPane pane = (GuiRootPane) getRootPane();
              pane.setCurrentTable((GuiTable)mem);
            }
	      	}
	      }
      }
   }

   /**
    * Liefert die zuletzt aktivierte Registerkarte.
    */
   public GuiTab getCurrentTab() {
      return currentTab;
   }

   /**
    * Setzt die Anordnung der Registerkarten; erlaubte Werte sind:
    * <UL>
    * <LI>TOP
    * <LI>BOTTOM
    * <LI>LEFT
    * <LI>RIGHT
    * </UL>
    */
   public final void setTabPlacement(String s) {
      if (s.equals("TOP")) {
         this.setTabPlacement(SwingConstants.TOP);
      } else if (s.equals("BOTTOM")) {
         this.setTabPlacement(SwingConstants.BOTTOM);
      } else if (s.equals("LEFT")) {
         this.setTabPlacement(SwingConstants.LEFT);
      } else if (s.equals("RIGHT")) {
         this.setTabPlacement(SwingConstants.RIGHT);
      } else {
         throw new IllegalArgumentException("TabPlacement not supported: " + s);
      }
   }

   /**
    * Setzt TabLayoutPolicy.
    * <p>
    * Neu: JDK 1.4
    * 
    * @param s
    *           Erlaubte Werte sind SCROLL oder WRAP.
    */
   public final void setTabLayoutPolicy(String s) {
      if (s.equals("SCROLL")) {
         this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
      } else if (s.equals("WRAP")) {
         this.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
      }
   }

   /**
    * für StateChangeListener
    */
   private GuiWindow getGuiWindow() {
      GuiWindow win = null;
      if (getRootPane() instanceof GuiRootPane) {
         win = ((GuiRootPane) getRootPane()).getParentWindow();
      }
      return win;
   }
   public Component getAwtComponent() {
 	  return this;
   }

   /** 
    * Liefert die Registerkarte mit dem angegebenen Tabindex zurück
    * @param index
    * 	Tabindex der Registerkarte die zurück gegeben werden soll
    * @return 
    * 	Registerkarte mit dem entsprechenden TabIndex oder null,
    *   wenn keine Registerkarte mit dem übergebenen Index gefunden wurde
    * @author thomas
    */
   public GuiTab getTab(int index) {
	   GuiTab tab = null;
		if (!myTabs.isEmpty()) {
			Iterator<GuiTab> tabit= myTabs.values().iterator(); 
			while (tabit.hasNext()) {
				tab = tabit.next();
				if (tab.getTabIndex() == index) {
					return tab;
				}
			}
		}
		
		return null;
	}
}