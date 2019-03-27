/*
 * Created on 27.02.2005
 */
package de.guibuilder.framework;


/**
 * Kapselt das HilfeSystem.<p>
 * Eine Implementierung dieses Interfaces ist der GuiSession
 * zu übergeben.
 * GuiSession selbst implementiert dieses Interface 
 * und delegiert alle Aufrufe entsprechend weiter.<p>
 * In GuiBuilderConfig.xml kann unter "HelpManager"
 * der Name eine Klasse angegeben werden, die dieses
 * Interface implementiert.<br>
 * Diese Klasse wird dann von der GuiSession instanziiert.<p>
 * Z.Z. gibt es nur eine Implementierung mit JavaHelp
 * @see de.guibuilder.framework.GuiSession
 * @author peter
 */
public interface HelpManagerIF {
	/**
	 * Setzt das Help-Label zu der angegebenen Gui-Komponente
	 * @param member
	 * @param helpId
	 */
	public void setHelpId(MemberAble member, String helpId);
	/**
	 * Liefert das Help-Label der angegebenen Gui-Komponente
	 * @param member
	 * @return
	 */
	public String getHelpId(MemberAble member);
	/**
	 * Zeigt die Hilfe zu der angegebenen Gui-Komponente an.
	 * @param member
	 */
	public void showHelp(MemberAble member);
	/**
	 * Zeigt die Hilfe zu dem angegebenen Help-Label
	 * @param helpId
	 */
	public void showHelp(String helpId);
	/**
	 * Die angegebene Action (Button oder MenuItem) soll
	 * die Feld-bezogene Context-Hilfe auslösen können.
	 * @param action
	 */
	public void enableContextHelp(GuiAction action);
}
