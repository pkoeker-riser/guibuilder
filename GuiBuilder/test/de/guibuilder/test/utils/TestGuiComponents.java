/*
 * Created on 02.09.2003
 */
package de.guibuilder.test.utils;

import de.guibuilder.framework.*;

/**
 * @author kknobloch
 * Bereitstellung aller GuiBuilder Komponenten.
 */
public class TestGuiComponents {
	/**
	 * Interner Zähler, der angibt welche GuiComponent
	 * gerade die aktuelle ist
	 */
	private int compCnt = 0;
	/**
	 * Liefert die erste GuiComponent zurück.
	 * @return GuiText
	 */
	public GuiComponent getFirstComponent() {
		
		this.compCnt = 1;
		GuiText comp = new GuiText();
		return comp;
	}
	/**
	 * Liefer die nächte GuiComponent aus der Liste
	 * @return
	 */
	public GuiComponent getNextComponent() {

		GuiComponent comp = null;
		
		switch( this.compCnt ){
			case 0:
				comp = new GuiText();
				break;
			case 1:
				comp = new GuiNumber();
				break;
			case 2:
				comp = new GuiMoney();
				break;
			case 3:
				comp = new GuiPassword();
				break;
			case 4:
				comp = new GuiDate();
				break;
			case 5:
				comp = new GuiTime();
				break;
			case 6:
				comp = new GuiMemo();
				break;
		}
		this.compCnt++;
		return comp;
	}
	/**
	 * Liefert zur aktuellen GuiComponent die Bezeichnung
	 * @return Bezeichnung der GuiComponent oder Leerstring
	 */
	public String getComponentName() {
		
		String s = "";
		switch( this.compCnt-1 ){
			case -1:
				s = "";
				break;
			case 0:
				s = "GuiText";
				break;
			case 1:
				s = "GuiNumber";
				break;
			case 2:
				s = "GuiMoney";
				break;
			case 3:
				s = "GuiPassword";
				break;
			case 4:
				s = "GuiDate";
				break;
			case 5:
				s = "GuiTime";
				break;
			case 6:
				s = "GuiMemo";
				break;
		}
		return s;		
	} 

}
