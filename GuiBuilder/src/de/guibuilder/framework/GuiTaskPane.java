/*
 * Created on 05.05.2005
 */
package de.guibuilder.framework;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPanel;

import com.l2fprod.common.swing.JTaskPaneGroup;
import com.l2fprod.common.swing.PercentLayout;
import com.l2fprod.common.swing.plaf.LookAndFeelAddons;

import de.guibuilder.framework.event.GuiTabSelectionEvent;

/**
 * @author peter
 */
public class GuiTaskPane extends com.l2fprod.common.swing.JTaskPane
		implements MemberAble {	
   private static final long serialVersionUID = 1L;

   private JPanel mainPanel = new JPanel();

	private GuiPanel currentPanel;

	private JTaskPaneGroup currentGroup;

	private String msgActive;

	public GuiTaskPane() {
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add("West", this);
	}

	JPanel getMainPanel() {
		return this.mainPanel;
	}

	public final String getTag() {
		return "TaskPane";
	}

	public Component getAwtComponent() {
		return this;
	}

	JTaskPaneGroup addTab(String label) {
		return this.addTab(label, null);
	}
	JTaskPaneGroup addTab(String label, String iconUrl) {
		JTaskPaneGroup group = new JTaskPaneGroup();
		group.setText(label);
		if (this.currentGroup == null) {
			group.setSpecial(true);
		}
		if (iconUrl != null) {
			try {
				Icon icon = GuiUtil.makeIcon(iconUrl);
			    group.setIcon(icon);
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
			}		
		}
		JPanel panel = new JPanel();
		panel.setLayout(new PercentLayout(PercentLayout.VERTICAL, 0));
		panel.setOpaque(false);

		this.add(group);

		this.currentGroup = group;
		return group;
	}

	GuiPanel addButton(String title, String iconUrl) {
		final GuiPanel panel = new GuiPanel(title);
		Icon icon = null;
		Action action = null;
		try {
			icon = GuiUtil.makeIcon(iconUrl);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
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
		this.currentGroup.add(action);

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
			GuiTabSelectionEvent event = new GuiTabSelectionEvent(pane
					.getParentWindow(), panel, msgActive, -2); // TODO:
																// tabIndex
			pane.obj_TabOpen(panel, msgActive, event);
		}
	}
	public static void setUI(String ui_class) {
	   	try {
	   		Class clazz = Class.forName(ui_class);
	   		LookAndFeelAddons.setAddon(clazz);
	   	} catch (Exception ex) {
	   		System.err.println(ex.getMessage());
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
}
