/*
 * Created on 04.05.2005
 */
package de.guibuilder.framework;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.l2fprod.common.swing.JButtonBar;
import com.l2fprod.common.swing.plaf.ButtonBarUI;
import de.guibuilder.framework.event.GuiTabSelectionEvent;

/**
 * @author PKOEKER
 */
public class GuiButtonBar extends JButtonBar implements MemberAble {	
   private static final long serialVersionUID = 1L;
	private JPanel mainPanel = new JPanel();
	private ButtonGroup group = new ButtonGroup();

	private GuiPanel currentPanel;
	private String msgActive;

	public GuiButtonBar() {
		super(VERTICAL);
		// TODO: Verschiedene UIs
		//this.setUI(new BlueishButtonBarUI());
		//this.setUI(new IconPackagerButtonBarUI());
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add("West", this);
	}

	JPanel getMainPanel() {
		return this.mainPanel;
	}

	public final String getTag() {
		return "ButtonBar";
	}

	public Component getAwtComponent() {
		return this;
	}

	/** 
	 * Fügt eine Schaltfläche unter Verwendung des Schaltflächentitels
	 * und ihrer iconURL hinzu. Der Name wird nicht gesetzt, bleibt also
	 * auf dem initialen null stehen.
	 * 
	 * @deprecated
	 * Sollte nicht mehr verwendet werden, weil auch der Name der Komponente
	 * gesetzt werden soll. Stattdessen sollte addNamesButton() verwendet werden.
	 * 
	 * @see #addNamedButton(String, String, String)
	 * 
	 * @param title 
	 * Sichtbarer Schaltflächentitel
	 * 
	 * @param iconURL
	 * 
	 * @return
	 * 
	 * @author thomas
	 * 
	 */
	GuiPanel addButton(String title, String iconUrl) {
		return this.addNamedButton(title, null, iconUrl);
	}
	
	/**
	 * Fügt eine Schaltfläche unter Verwendung des Schaltflächentitels,
	 * ihres Namens und ihrer iconURL hinzu.
	 * 
	 * Sollte statt der alten Methode addButton() verwendet werden, weil auch der 
	 * Name der Komponente gesetzt werden soll.
	 * 
	 * @see #addButton(String, String)
	 * 
	 * @param title 
	 * Sichtbarer Schaltflächentitel (entspricht im XML der label-Eigenschaft
	 * des Tags ButtonBarButton)
	 * @param name
	 * unsichtbarer Name der Schaltfläche (entspricht im XML der name-Eigenschaft
	 * des Tags ButtonBarButton) 	  
	 * @param iconURL
	 *  
	 * @return
	 * 
	 * @author thomas
	 * 
	 */
	GuiPanel addNamedButton(String title, String name, String iconUrl) {
		final GuiPanel panel = new GuiPanel(title);
		Icon icon = null;
		Action action = null;
		try {
			icon = GuiUtil.makeIcon(iconUrl);
		} catch (Exception ex) {
			System.err.println("Missing IconURL für ButtonBarButton results in Exception: " + ex.getMessage());
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
		button.setName(name);
		this.add(button);

		group.add(button);

		if (group.getSelection() == null) {
			button.setSelected(true);
			show(panel);
		}
		return panel;
	}

	
	/**
	 * Liefert den Button zu dem angegebenen Label.
	 * @param label
	 * Sichtbarer Schaltflächentitel (entspricht der label-Eigenschaft des
	 * ButtonBarButton)
	 * @return null, wenn kein Button mit diesem Label vorhanden
	 * @author thomas
	 */
	public AbstractButton getButton(String label) {
		Enumeration<AbstractButton> bts = group.getElements();
		while (bts.hasMoreElements()) {
			AbstractButton bt = bts.nextElement();
			if (bt.getActionCommand().equalsIgnoreCase(label)) {
				return bt;
			}
		}
		System.err.println("Missing ButtonBarButton! (No Button with label ' "+ label + "' found.)");
		return null;
	}

	/**
	 * Liefert den Button an der angegebenen Position.
	 * @param index
	 *  Position des zurückzugebenden Button in der Buttonbar
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
		System.err.println("Error: Missing ButtonBarButton! (In GuiButtonBar#getButton(int index) no Button at position' "+ index + "' found.)");
		return null;
	}
	
	/**
	 * Liefert den Button mit dem angegebenen Name.
	 * @param name
	 * Name des Buttons (entspricht der label-Eigenschaft des
	 * ButtonBarButton)
	 * @return null, wenn kein Button mit diesem Label vorhanden
	 * @author thomas
	 */
	public AbstractButton getButtonByName(String name) {
		Enumeration<AbstractButton> bts = group.getElements();
		while (bts.hasMoreElements()) {
			AbstractButton bt = bts.nextElement();
			if (bt.getName().equalsIgnoreCase(name)) {
				return bt;
			}
		}
		System.err.println("Missing ButtonBarButton! (No Button with name ' "+ name + "' found.)");
		return null;
	}	
	
	/** 
	 * Liefert das zum aktuell angeklickten Button gehörige Panel auf
	 * der rechten Seite des Fenster zurück
	 * @return Aktuell sichtbares Panel auf der rechten Fensterseite.
	 * @author thomas
	 */
	public GuiPanel getCurrentRightPanel() {
		return this.currentPanel;
	}

	
	private void show(GuiPanel panel) {
		if (currentPanel != null) {
			this.mainPanel.remove(currentPanel.getJComponent());
		}
		this.mainPanel.add("Center", panel.getJComponent());
		this.mainPanel.revalidate();
		this.mainPanel.repaint();
		currentPanel = panel;
      // Externen Controller informieren
      if (msgActive != null) {
         // Event
         GuiRootPane pane = (GuiRootPane) getRootPane();
         GuiTabSelectionEvent event = new GuiTabSelectionEvent ( 
         		pane.getParentWindow(), panel, msgActive, -2); // TODO Tabindex
         pane.obj_TabOpen(panel, msgActive, event);
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
   public void setUI(String ui_class) {
   	try {
   		Class<?> clazz = Class.forName(ui_class);
   		Object o = clazz.newInstance();
   		this.setUI((ButtonBarUI)o);
   	} catch (Exception ex) {
   		System.err.println("Method GuiButtonBar.setUI('"+ ui_class.toString() +"') results in Exception: " + ex.getMessage());
   	}
   }

	public int getButtonIndex(String name) {
		int i = 0;
	
		Enumeration<AbstractButton> bts = group.getElements();
		while (bts.hasMoreElements()) {
			AbstractButton bt = bts.nextElement();
			if (bt.getActionCommand().equalsIgnoreCase(name)) {
				return i;
			}
			i++;
		}
		System.err.println("Missing Button: "+ name +". No ButtonIndex to return.");
		return -1;
	}


}