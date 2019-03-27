package de.guibuilder.framework;

import javax.swing.JMenuItem;


public class GuiMenuItemImpl extends JMenuItem {
   private static final long serialVersionUID = 1L;

   private char enabledWhen; // S,M,N,A
	public char getEnabledWhen() {
		return enabledWhen;
	}

	public void setEnabledWhen(char enabledWhen) {
		this.enabledWhen = enabledWhen;
	}

}
