/*
 * Created on 07.08.2003
 */
package de.guibuilder.test.utils;

import de.guibuilder.framework.GuiWindow;
import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.event.GuiUserEvent;

import de.guibuilder.adapter.GuiAPI;
/**
 * @author kknobloch
 */
public class TestGuiAnzeige {

	/**
	 * GUI-Builder API Instanz des Fensters
	 */
	protected GuiAPI api = GuiAPI.getInstance();
	protected boolean weiter = false;
	protected GuiWindow win = null;
	
	public TestGuiAnzeige() {

		api.createWindow("testAnzeige", "testAnzeige", "./de/guibuilder/test/TestAnzeige.xml");
		// Ich registriere mich beim Fenster als Controller.
		api.setController("testAnzeige", this);
		this.win = api.getWindow("testAnzeige");
		win.hide();
	}
	
	public void showMessage(String m) {
		
		GuiComponent myComp = this.win.getGuiComponent("dfAnzeige");
		myComp.setValue(m);
		this.weiter = false;
		
	}
	
	public boolean isReady() {
		return this.weiter;
	}

	public void pbOKClick(GuiUserEvent event) {
		
		this.weiter = true;
		this.win.hide();
		
	}

}
