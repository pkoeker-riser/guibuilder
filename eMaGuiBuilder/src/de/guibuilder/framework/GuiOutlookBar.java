/*
 * Created on 05.05.2005
 */
package de.guibuilder.framework;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ButtonUI;

import com.l2fprod.common.swing.PercentLayout;

import de.guibuilder.framework.event.GuiTabSelectionEvent;

/**
 * @author peter
 */
public class GuiOutlookBar extends com.l2fprod.common.swing.JOutlookBar implements MemberAble {
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger
      .getLogger(GuiOutlookBar.class);

  private JPanel mainPanel = new JPanel();
  private ButtonGroup group = new ButtonGroup();
  private GuiPanel currentPanel;
  private GuiTab currentTab;
  private String msgActive;
  /**
   * für den direkten Zugriff auf die einzelnen OutlookBarTabs werden diese in
   * einer HashMap verwaltet
   */
  private LinkedHashMap<String, GuiTab> myTabs = new LinkedHashMap<String, GuiTab>();

  public GuiOutlookBar() {
    this.setTabPlacement(SwingConstants.LEFT);
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add("West", this);
    this.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        Component panel = getSelectedComponent();
        if (panel != null) {
           GuiTab tab = myTabs.get(panel.getName());
           GuiRootPane pane = (GuiRootPane) getRootPane();
           if (pane != null && tab != null) {
              // Externen Controller informieren
              // 1. Tab selbst
              if (tab.getMsgActive() != null) {
                // Event
                GuiTabSelectionEvent event = new GuiTabSelectionEvent(
                      getGuiWindow(), tab, tab.getMsgActive(), tab.getTabIndex());
                pane.obj_TabOpen(tab, tab.getMsgActive(), event);
              }
           } else { // RootPane oder Tab null; passiert nur beim ersten Anzeigen
//              System.out
//                    .println("GuiTabset ChangeListener.stateChanged: Missing GuiRootPane!");
           }
        }
     }
    });
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

  JPanel getMainPanel() {
    return this.mainPanel;
  }

  public final String getTag() {
    return "OutlookBar";
  }

  public Component getAwtComponent() {
    return this;
  }

  GuiTab addTab(String label, String name) {
    if (name == null) {
      name = label;
    }
    GuiTab tab = new GuiTab();
    tab.component.setLayout(new PercentLayout(PercentLayout.VERTICAL, 0));
    tab.component.setOpaque(false);
    tab.setName(name);

    JScrollPane scroll = this.makeScrollPane(tab.component);
    scroll.setName(name);
    this.addTab("", scroll);

    // this to test the UI gets notified of changes
    int index = this.indexOfComponent(scroll);
    this.setTitleAt(index, label);
    this.currentTab = tab;

    // das aktuell erzeugte Tab in der LinkedHashMap merken,
    // um den späteren gezielten Zugriff auf dieses Tab und
    // seine Buttons zu erleichtern
    GuiTab prev = myTabs.put(tab.getName(), tab);
    if (prev != null) {
      System.err.println("Warning: In GuiOutlookBar#addTab(String label='" + label
          + "') duplicating OutlookBarTab-Name: " + tab.getName());
    }
    return tab;
  }

  public JPanel getTab(int index) {
    int pos = -1;
    GuiTab tab = null;
    if (!myTabs.isEmpty()) {
      Iterator<GuiTab> tabit = myTabs.values().iterator();
      while (tabit.hasNext()) {
        tab = tabit.next();
        pos++;
        if (pos == index) {
          return tab.component;
        }
      }
    }

    return null;
  }

  public JPanel getTab(String name) {
    GuiTab tab = null;
    if (!myTabs.isEmpty()) {
      Iterator<GuiTab> tabit = myTabs.values().iterator();
      while (tabit.hasNext()) {
        tab = tabit.next();
        if (tab.getName().equalsIgnoreCase(name)) {
          return tab.component;
        }
      }
    }

    return null;
  }

  GuiPanel addButton(String title, String name, String iconUrl) {
    if (name == null)
      name = title;
    final GuiPanel panel = new GuiPanel(title);
    panel.setName(name);
    Icon icon = null;
    Action action = null;
    try {
      icon = GuiUtil.makeIcon(iconUrl);
    } catch (Exception ex) {
      logger.error("Error: GuiOutlookBar#addButton(String title='" + title + "', String name='" + name
          + "', String iconUrl='" + iconUrl + "' failed creating Icon with Exception:", ex);
    }
    if (icon == null) {
      action = new AbstractAction(title) {
        public void actionPerformed(ActionEvent e) {
          show(panel);
        }
      };
    } else {
      action = new AbstractAction(title, icon) {
        public void actionPerformed(ActionEvent e) {
          show(panel);
        }
      };
    }
    JToggleButton button = new JToggleButton(action);
    button.setActionCommand(name);
    button.setName(name);
    try {
      button.setUI((ButtonUI) Class.forName((String) UIManager.get("OutlookButtonUI")).newInstance());
    } catch (Exception e) {
      logger.error("Error: GuiOutlookBar#addButton(String title='" + title + "', String name='" + name
          + "', String iconUrl='" + iconUrl + "' failed creating ButtonUI", e);
    }
    this.currentTab.add(button);

    group.add(button);

    if (group.getSelection() == null) {
      button.setSelected(true);
      show(panel);
    }

    return panel;
  }

  private void show(GuiPanel panel) {
    if (currentPanel != null) {
      if (panel == currentPanel) {
        return;
      } else {
        this.mainPanel.remove(currentPanel.getJComponent());
      }
    }
    this.mainPanel.add("Center", panel.getJComponent());
    this.mainPanel.revalidate();
    this.mainPanel.repaint();
    currentPanel = panel;
    // Externen Controller informieren
    if (msgActive != null) {
      // Event
      GuiRootPane pane = (GuiRootPane) getRootPane();
      GuiTabSelectionEvent event = new GuiTabSelectionEvent(pane.getParentWindow(), panel, msgActive, -2); // TODO:
      // tabIndex
      pane.obj_TabOpen(panel, msgActive, event);
    }
  }

  /**
   * Liefert die ButtonGroup mit der Menge der Tabs.
   * 
   * @return
   */
  public ButtonGroup getButtonGroup() {
    return this.group;
  }

  /**
   * Liefert den Button zu dem angegebenen Label.
   * 
   * @param label
   *          (Titel der Schaltfläche, wie in XML per label-Eigenschaft
   *          zugewiesen)
   * @return null, wenn Label ungültig
   */
  public AbstractButton getButton(String label) {
    Enumeration<AbstractButton> bts = group.getElements();
    while (bts.hasMoreElements()) {
      AbstractButton bt = bts.nextElement();
      if (bt.getActionCommand().equalsIgnoreCase(label)) {
        return bt;
      }
    }
    logger.warn("Missing Button: " + label);
    return null;
  }

  /**
   * Liefert den Button zu dem angegebenen Namen.
   * 
   * @param name
   *          (Name der Schaltfläche, wie in XML per name-Eigenschaft
   *          zugewiesen)
   * @return null, wenn Label ungültig
   */
  public AbstractButton getButtonByName(String name) {
    Enumeration<AbstractButton> bts = group.getElements();
    while (bts.hasMoreElements()) {
      AbstractButton bt = bts.nextElement();
      String btname = bt.getName();
      if (btname != null) {
        if (btname.equalsIgnoreCase(name)) {
          return bt;
        }
      }
    }
    System.err.println("Missing OutlookBarButton! (No Button with name ' " + name + "' found.)");
    return null;
  }

  /**
   * Selektiert den angegebenen Tab.
   * 
   * @param title
   */
  public void setSelectedIndex(String title) {
    int index = this.indexOfTab(title);
    if (index != -1) {
      this.setSelectedIndex(index);
    }
  }

  /**
   * Selektiert den angegebenen Tab.
   * 
   * @param name
   *          Name des OutlookBarTab, wie in der XML-Datei per name-Eigenschaft
   *          spezifiziert
   */
  public void setSelectedIndexByName(String name) {
    GuiTab tab = null;
    int i = -1;
    if (!myTabs.isEmpty()) {
      Iterator<GuiTab> tabit = myTabs.values().iterator();
      while (tabit.hasNext()) {
        i++;
        tab = tabit.next();
        if (tab.getName().equalsIgnoreCase(name)) {
          this.setSelectedIndex(i);
          return;
        }
      }
    }
    System.err
        .println("Error: Can't find OutlookBarTab in GuiOutlookBar#setSelectedIndexByName(String namename='"
            + name.toString() + "').");
    return;
  }

  /**
   * Zeigt das Panel zu dem angegebenen Label.
   * 
   * @param title
   */
  public void show(String label) {
    AbstractButton bt = this.getButton(label);
    if (bt != null) {
      bt.doClick();
    }
  }

  /**
   * Setzt das ActionCommand, welches beim TabSelectionEvent geliefert wird.
   * 
   * @see de.guibuilder.framework.event.GuiTabSelectionEvent
   */
  public final void setMsgActive(String s) {
    this.msgActive = s;
  }

  public final String getMsgActive() {
    return this.msgActive;
  }

  /**
   * Liefert den Button an der angegebenen Position der gesamten Buttonbar
   * zurück.
   * 
   * @param index
   *          Position des zurückzugebenden Button in der gesamten Buttonbar
   * @return null, wenn kein Button mit diesem Label vorhanden
   * @author thomas
   */
  public AbstractButton getButton(int index) {
    Enumeration<AbstractButton> bts = group.getElements();
    int pos = -1;
    while (bts.hasMoreElements()) {
      AbstractButton bt = bts.nextElement();
      pos++;
      if (pos == index) {
        return bt;
      }
    }
    System.err
        .println("Error: Missing ButtonBarButton! (In GuiButtonBar#getButton(int index) no Button at position' "
            + index + "' found.)");
    return null;
  }

  /**
   * Liefert den Button an der angegebenen Position auf dem angegebenen Tab
   * zurück.
   * 
   * @param index
   *          Position des zurückzugebenden Buttons innerhalb des Tab (Zählung
   *          beginnend mit 0)
   * @param tab
   *          OutlookBarTab als JScrollPane auf dem der zurückzugebene Button
   *          liegt
   * @return null, wenn kein Button mit diesem index vorhanden
   * @author thomas
   */
  public AbstractButton getButton(JPanel tab, int index) {
    if (tab != null) {
      int pos = -1;
      if (tab.getComponentCount() > 0) {
        for (int i = 0; i < tab.getComponentCount(); i++) {
          Object o = tab.getComponent(i);
          if (o instanceof JToggleButton) {
            JToggleButton myPane = (JToggleButton) o;
            pos++;
            if (pos == index) {
              return myPane;
            }
          }
        }
      }
    }

    System.err
        .println("Missing OutlookBarButton! (No Button found at position name ' " + index + "' found.)");
    return null;
  }

  /**
   * Liefert das zum aktuell angeklickten Button gehörige Panel auf der rechten
   * Seite des Fenster zurück
   * 
   * @return Aktuell sichtbares Panel auf der rechten Fensterseite.
   * @author thomas
   */
  public GuiPanel getCurrentRightPanel() {
    return this.currentPanel;
  }
  
  /**
   * @deprecated
   * @see #getTab(int)
   * @param index
   * @return
   */
  public JScrollPane getOutlookBarTab(Integer index) {
    int pos = -1;
    if (this.countComponents() > 0) {
      for (int i = 0; i < this.countComponents(); i++) {
        Object o = this.getComponent(i);
        if (o instanceof JScrollPane) {
          JScrollPane myPane = (JScrollPane) o;
          pos++;
          if (pos == index) {
            return myPane;
          }
        }
      }
    }
    return null;
  }

  /**
   * @deprecated
   * @see #getTab(String)
   * @param name
   * @return
   */
  public JScrollPane getOutlookBarTab(String name) {
    if (this.countComponents() > 0) {
      for (int i = 0; i < this.countComponents(); i++) {
        Object o = this.getComponent(i);
        if (o instanceof JScrollPane) {
          JScrollPane myPane = (JScrollPane) o;
          if (name.length() > 0) {
            if (myPane.getName().equalsIgnoreCase(name)) {
              return myPane;
            }
          } else {
            return myPane;
          }
        }
      }
    }
    return null;
  }
}
