package de.guibuilder.framework;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.sql.Types;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import de.jdataset.DataView;
import de.jdataset.JDataColumn;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.jdataset.NVPair;

/**
 * Implementierung eines SplitPanels.
 * <P>
 * Es können mit der Methode add nacheinander (von links nach rechts) zwei
 * Components (z.B. Tree, Panel) zugewiesen werden. <br>
 * Die Breite des linken Teilfensters (bzw. die Höher des oberen) wird dabei
 * nach der PreferredSize der linken Komponente gesetzt (Breite bzw. Höhe).
 * 
 * @see #add
 */
public final class GuiSplit extends GuiContainer {
   // Attributes
   private GuiSplitImpl component;
   private int compCount;
   private GuiMember leftComponent;
   private GuiMember rightComponent;
   private int dividerLocation;
   private boolean dividerFixed;

   // Constructor
   /**
    * Erzeugt ein SplitPanel mit senkrechten Divider.
    */
   public GuiSplit() {
      super();
      component = new GuiSplitImpl(JSplitPane.HORIZONTAL_SPLIT);
      //##this.setRightComponent(new GuiLabel(" "));
   }

   // Methods
   public final String getTag() {
      return "Split";
   }

   public int getLayoutManager() {
      return SPLIT;
   }

   public void setLayoutManager(int lm) {
      throw new IllegalArgumentException("No LayoutManager to set!");
   }

   // From GuiMember
   /**
    * Liefert ein JSplitPane
    */
   public JComponent getJComponent() {
      return component;
   }

   /**
    * Fügt eine Komponente hinzu. Ab dem zweiten Aufruf wird immer
    * RightComponent gesetzt.
    * <p>
    * Die Divider Location wird auf die PreferredSize der ersten Komponente
    * gesetzt.
    */
   public void add(GuiMember member, GridBagConstraints dummy) {
      if (compCount == 0) { // New 29.1.2005 / PKÖ
         this.leftComponent = member;
      } else {
         this.rightComponent = member;
      }
      if (member.getGuiParent() == null) { 
         member.setGuiParent(this.getGuiParent());
      } // End New 29.1.2005
      this.add(member.getJComponent(), dummy);
   }

   /**
    * Wird von der Factory direkt aufgerufen, wenn Komponente in einer ScrollBox
    * steckt (wie z.B. bei Tree).
    * Dann wird left und rightComponent aus dem Inhalt der ScrollBox gesetzt
     */
   public void add(Component panel, GridBagConstraints dummy) {
      // In dummy sind die GridBagConstraints der Factory
      // Erste Komponente
      if (compCount == 0) {
         compCount++;
         component.setLeftComponent(panel);
         if (panel instanceof GuiScrollBox) { // Wenn das GuiMember in einer Scrollbox steckt, dann auspacken
            this.leftComponent = ((GuiScrollBox)panel).getGuiMember();
         }
      }
      // zweite Komponente
      else {
         component.setRightComponent(panel);
         if (panel instanceof GuiScrollBox) { // Wenn das GuiMember in einer Scrollbox steckt, dann auspacken
            this.rightComponent = ((GuiScrollBox)panel).getGuiMember();
         }
      }
   }

   /**
    * Delegation to JSplitPane
    */
   public void setLeftComponent(GuiMember comp) {
      leftComponent = comp;
      component.setLeftComponent(comp.getJComponent());
      if (comp.getGuiParent() == null) { // New 10.12.2003 PKÖ
         comp.setGuiParent(this.getGuiParent());
      }
   }

   public GuiMember getLeftComponent() {
      return leftComponent;
   }

   /**
    * Delegation to JSplitPane
    */
   public void setRightComponent(GuiMember comp) {
      rightComponent = comp;
      //##int location = component.getDividerLocation();
      component.setRightComponent(comp.getJComponent());
      //##this.setDividerLocation(location, false);
      if (comp.getGuiParent() == null) { // New 10.12.2003 PKÖ
         comp.setGuiParent(this.getGuiParent());
      }
   }

   public GuiMember getRightComponent() {
      return rightComponent;
   }

   /**
    * Setzt die Ausrichtung des Split Panels; erlaubte Werte sind HOTIZONTAL und
    * VERTICAL. <br>
    * Wird für die Factory benötigt; siehe Attribut type="VERTICAL".
    */
   public void setOrientation(String s) {
      if (s.equals("HORIZONTAL")) {
         component.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
      } else if (s.equals("VERTICAL")) {
         component.setOrientation(JSplitPane.VERTICAL_SPLIT);
      } else {
         throw new IllegalArgumentException("Valid arguments: HORIZONTAL | VERTICAL");
      }
   }
   
   /**
    * Delegation to JSplitPane
    * @param location
    * @param fixed Wenn true, dann nicht aus Preferences wiederherstellen
    */
   public void setDividerLocation(final int location, final boolean fixed) {
      if (location <= 0) return;
      SwingUtilities.invokeLater(new Runnable() {

         @Override
         public void run() {
            if (fixed) {
               dividerFixed = true;
               dividerLocation = location;
               component.setResizeWeight(0.0);
            }
            if (component != null) {
//         System.out.println(this.getName() + ":" + component.getDividerLocation() + "/" + location + " " + fixed 
//               + " (" + component.getMinimumDividerLocation() + ")");
               if (fixed) {
                  component.setFixedDividerLocation(location);
               } else {
                  component.setDividerLocationImpl(location);
               }
            }
         }
     });
   }
   
   public final GuiComponent getGuiComponentsFromComponents(String name) {      
      {
         Object o = this.leftComponent;
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
      {
         Object o = this.rightComponent;
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
      return null;
  }

   /**
    * From awt.Container
    */
   final void removeAll() {
      leftComponent = null;
      rightComponent = null;
      component.removeAll();
   }
   
   public void getPreferences(JDataSet ds) {
     if (ds == null) return;
     // Meta Table
     JDataTable tblT = null;
     try {
       tblT = ds.getDataTable("GuiSplit");
     } catch (Exception ex) {
       tblT = new JDataTable("GuiSplit");
       ds.addRootTable(tblT);
       JDataColumn colName = tblT.addColumn("name", Types.VARCHAR);
       colName.setPrimaryKey(true);
     }
     if (!tblT.hasDataColumn("dividerLocation")) {
        tblT.addColumn("dividerLocation", Types.INTEGER);
     }
     // Prefs Table
     JDataRow rowT;
     Collection<JDataRow> trows = null;
     try {
       trows = ds.getChildRows(new DataView("GuiSplit", new NVPair("name", this.getName(), Types.VARCHAR)));
     } catch (Exception ex) {}
     if (trows != null && trows.size() >= 1) {
       rowT = trows.iterator().next();
     } else {
       rowT = ds.createChildRow("GuiSplit");
     }
     rowT.setValue("name", this.getName());
     rowT.setValue("dividerLocation", this.component.getDividerLocation());
     if (this.getLeftComponent() != null) {
       this.getLeftComponent().getPreferences(ds);
     }
     if (this.getRightComponent() != null) {
       this.getRightComponent().getPreferences(ds);
     }
   }
   
   public void setPreferences(JDataSet ds) {
     if (ds == null) return;
     Collection<JDataRow> coll = ds.getChildRows(new DataView("GuiSplit", new NVPair("name", this.getName(), Types.VARCHAR)));
     if (coll.size() != 1) return;
     JDataRow row = coll.iterator().next();
     if (dividerFixed) {
        this.setDividerLocation(dividerLocation, true);
     } else {
        this.setDividerLocation(row.getValueInt("dividerLocation"), false);
     }
     if (this.getLeftComponent() != null) {
       this.getLeftComponent().setPreferences(ds);
     }
     if (this.getRightComponent() != null) {
       this.getRightComponent().setPreferences(ds);
     }
   }

}