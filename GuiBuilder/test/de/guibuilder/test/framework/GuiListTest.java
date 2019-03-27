/*
 * Created on 13.03.2005
 */
package de.guibuilder.test.framework;

import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiElement;
import de.guibuilder.framework.GuiList;
import de.guibuilder.framework.GuiMember;

/**
 * @author peter
 */
public class GuiListTest extends GuiComponentTest {
	private GuiList myComp = new GuiList();

	public GuiMember getTestGuiMember() {
		return this.myComp;
	} 
	public GuiElement getTestGuiElement() {
		return this.myComp;
	} 
	public GuiComponent getTestGuiComponent() {
		return this.myComp;
	} 
	public void testConstructorString() {
	}

	public void testGetDataType() {
		assertEquals(GuiComponent.STRING,this.myComp.getDataType());
	}
	public void testGetGuiType() {
		assertEquals(GuiComponent.GUI_COMPONENT, this.myComp.getGuiType());
	}
	public void testReset() {
		this.myComp.addItem("Bla");
		this.myComp.addItem("Blub");
		this.myComp.reset();
		assertEquals(this.myComp.getSelectedIndex(), -1);
	}
	public boolean validateGuiMemberValue(String otyp, Object o) {
		
		if( otyp.equalsIgnoreCase("GuiType") ) {
			
			if(Integer.parseInt((String) o) == GuiComponent.GUI_COMPONENT )
				return true;
			
		}
		
		return false;
	}

	public void testGetValue() {
		// TODO
	}
	public void testVerify() {
		// Nix
	}
	public void testMap() {
		this.myComp.setItems(new String[] {"Frau", "Herrn", "Firma"});
		this.myComp.setMap(new String[] {"1", "2", "3"});
		this.myComp.setValue("2");
		assertEquals(myComp.getSelectedItem(),"Herrn");
		assertEquals(myComp.getSelectedIndex(), 1);
	}
	
}
