/*
 * Created on 08.08.2003
 */
package de.guibuilder.test.utils;
import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiWindow;
import electric.xml.Document;

/**
 * @author kknobloch
 */
public class TestDialogSupport {

	private TestXMLSupport t = new TestXMLSupport();
	//private TestJDataSetSupport ds = new TestJDataSetSupport();
	private GuiWindow win;

	public TestDialogSupport(String guixml) {
		
		Document xDialog = t.loadFromFileSystem(guixml);
		
		try {
			this.win = GuiFactory.getInstance().createWindow(xDialog);
		}
		catch(Exception e) {
			System.out.println(e);
		}
		
		
	}
	
	public GuiWindow getWin() {
		return this.win;
	}
}
