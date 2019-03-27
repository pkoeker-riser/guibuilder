package de.guibuilder.test.framework;

import de.guibuilder.framework.GDLParseException;
import de.guibuilder.framework.GuiAction;
import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiContainer;
import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiTable;
import de.guibuilder.framework.GuiTable.GuiTableColumn;
import de.guibuilder.framework.GuiTableRow;
import de.guibuilder.framework.GuiText;
import de.guibuilder.framework.GuiUtil;
import de.guibuilder.framework.GuiWindow;
import de.guibuilder.framework.event.GuiUserEvent;
import junit.framework.TestCase;

public class ContainerAccessTest extends TestCase {
	
	public static void main(String[] args) {
		ContainerAccessTest t = new ContainerAccessTest();
		t.test1();
	}
	
	public void test1() {
		try {
			GuiWindow win = GuiFactory.getInstance().createWindow("tutorial/Tut_2_container.xml");
			win.setController(this);
			win.setSystemForm(true);
			win.show();
			GuiAction mSave= win.getAction("mSave");
			mSave.setEnabled(false);
			GuiContainer cont1 = win.getGuiContainer("links");
			GuiComponent comp1 = cont1.getGuiComponent("name");
			GuiText txt1 = (GuiText)comp1;
			txt1.setValue("Max Mustermann");
			//cont1.setVisible(false);
			GuiContainer auftrag = win.getGuiContainer("auftrag");
			GuiTable tbl = (GuiTable)auftrag.getGuiComponent("artikel");
			GuiTableRow trow = tbl.insertRow();
			trow.setValue("artikel", "Holzroller");
			trow.setValue("datum", "11.11.2011");
			GuiTableColumn col = tbl.getColumn("datum");
			GuiContainer bank = win.getGuiContainer("bank");
			GuiText bankname = (GuiText)bank.getGuiComponent("bankname");
			bankname.setValue("Berliner Sparkasse");
			//GuiUtil.okCancelMessage(null, "Test", "Weiter");
//			while(true) {
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					System.exit(0);
//				}
//			}
		} catch (GDLParseException e) {
			e.printStackTrace();
		}
		
	}
	public void doOpen(GuiUserEvent event) {
		System.out.println("Open!");
		GuiAction mSave= event.window.getAction("mSave");
		mSave.setEnabled(true);
		
	}
	public void doSave(GuiUserEvent event) {
		System.out.println("Save!");
	}
	public void doExit(GuiUserEvent event) {
		System.out.println("Exit!");
		System.exit(0);
	}
}
