package de.pkjs.pltest;

import de.guibuilder.design.GuiDoc;
import junit.framework.TestCase;

public class TestGuiDoc extends TestCase {
	public void test1() {
		GuiDoc.createHardcopyWin(
				"D:/eclipse_workspace/it-man-client/gui/", "it-management_leitstand.xml", "C:/temp");
	}
	public void test2() {
		GuiDoc.createHardcopyForComponent("D:/eclipse_workspace/it-man-client/gui/", "it-management_leitstand.xml", "C:/temp", "Rechnungen");		
	}
	public void test3() {
		GuiDoc.createHardcopyForComponent("D:\\eclipse_workspace\\it-man-client\\gui", "rechnung/itm_dlg_rechnung.xml", "C:/temp", "Zahlungsanordnung");
	}
	
}
