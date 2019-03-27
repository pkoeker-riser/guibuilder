package de.guibuilder.framework.event;

import de.guibuilder.framework.GuiTable;

public abstract class GuiTablePasteCallbackAdapter {
	protected GuiTable tbl;
	
	public GuiTablePasteCallbackAdapter() {
		//this.tbl = tbl;
	}
		
	public abstract void callback();
		
	
}
