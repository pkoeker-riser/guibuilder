package de.guibuilder.framework;

import java.awt.Component;
import java.awt.IllegalComponentStateException;
import java.util.Vector;

import de.jdataset.JDataSet;

/**
 * Beschreibt die Struktur eines Knotens in einer Tree-Komponente. Entspricht dem Keyword
 * "Element".
 * 
 * @see GuiTreeContent
 * @see GuiTree
 */
public final class GuiTreeElement implements java.io.Serializable, IDatasetComponent,
		MemberPopupAble {
	// Attributes
	/**
	 * Name (Typ) des Elements
	 */
	private String name;
	/**
	 * Verweist auf den Dateinamen einer Oberflächenstazifikation.
	 */
	private String fileName;
	private String iconName;
	private String msgNodeClick;
	private String elementName;
	private GuiPopupMenu popup;
	private String msgPopup;
	/**
	 * Menge der erlaubten Unterelemente.
	 * 
	 * @see #addContent
	 */
	private Vector<GuiTreeContent> content = new Vector<GuiTreeContent>();

	// Constructor
	/**
	 * Wird von der Factory bei dem Keyword "Begin Element" erzeugt
	 * 
	 * @param name
	 * @param fileName
	 */
	public GuiTreeElement(String name, String fileName) {
		this.name = name;
		this.fileName = fileName;
	}

	// Methods
	public String getName() {
		return name;
	}

	/**
	 * @see GuiTree#createNode
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFilename(String fileName) {
		this.fileName = fileName;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	// IDatasetComponent
	public void setElementName(String name) {
		elementName = name;
	}

	public String getElementName() {
		return elementName;
	}

	public String getElementPath(String path) {
		// TODO

		return path;
	}

	public void getDatasetValues(JDataSet ds) {
		this.getDatasetValues(ds, "");
	}

	public void getDatasetValues(JDataSet ds, String current) {
		// TODO
	}

	public void setDatasetValues(JDataSet ds) {
		// TODO
	}

	public void commitChanges() {
		// TODO
	}

	// End Binding
	/**
	 * Setzt das ActionCommand, welches ausgelöst wird, wenn sich der Inhalt der Komponente
	 * geändert hat.
	 */
	/*
	 * public final void setMsgChange(String cmd) { actionChange = cmd; }
	 */
	/**
	 * Liefert das ActionCommand, welches ausgelöst wird, wenn sich der Inhalt der
	 * Komponente geändert hat.
	 */
	/*
	 * public final String getMsgChange() { return actionChange; }
	 */
	/**
	 * Liefert den Vector der möglichen Einträge zu diesem Element. Siehe Standard-Methode
	 * "NewNode()".
	 */
	public Vector<GuiTreeContent> getContent() {
		return content;
	}

	/**
	 * Liefert einen GuiTreeContent unter Angabe seines Namens. Wirft eine
	 * IllegalArgumentException wenn unter dem Namen kein Content vorhanden.
	 * 
	 * @see GuiTree#createNode
	 */
	public GuiTreeContent getContentByName(String n) {
		if (n == null) {
			throw new IllegalArgumentException("GuiTree#getContentByName: Argument is null");
		}
		GuiTreeContent tmp = null;
		// TODO : contains
		for (int i = 0; i < content.size(); i++) {
			tmp = content.elementAt(i);
			if (tmp.getName().equals(n)) {
				return tmp;
			}
		}
		throw new IllegalArgumentException("GuiTree#getContentByName: Missing Content '"
				+ n + "'");
	}

	/**
	 * Liefert einen Vector mit den Namen aus GuiTreeContent. <br>
	 * Liefert null, wenn nix vorhanden.
	 * 
	 * @see GuiTreeContent
	 * @see GuiTree#createNode
	 */
	public Vector<String> getContentNames() {
		Vector<String> v = null;
		if (content.size() != 0) {
			v = new Vector<String>(content.size());
			for (int i = 0; i < content.size(); i++) {
				v.addElement(content.elementAt(i).getName());
			}
		}
		return v;
	}

	/**
	 * Setzt den Vector der möglichen Inhalte neu.
	 * 
	 * @see GuiTreeContent
	 */
	public void setContent(Vector<GuiTreeContent> v) {
		this.content = v;
	}

	/**
	 * Fügt dem Vector der möglichen Inhalte einen Eintrag hinzu. Siehe Keyword "Content"
	 * bei der Factory.
	 */
	public void addContent(GuiTreeContent _content) {
		this.content.add(_content);
	}

	// From MemberPopupAble
	public void setPopupMenu(GuiPopupMenu menu) {
		this.popup = menu;
	}

	// From MemberPopupAble
	public GuiPopupMenu getPopupMenu() {
		return this.popup;
	}

	// From MemberPopupAble
	public void showPopupMenu(int x, int y) {
		// TODO Exception
	}

	// From MemberPopupAble	
	public String getMsgPopup() {
		return this.msgPopup;
	}

	// From MemberPopupAble
	public void setMsgPopup(String msgPopup) {
		this.msgPopup = msgPopup;
	}

	// From MemberAble
	public String getTag() {
		return "Element";
	}

	/**
	 * @return Returns the msgNodeClick.
	 */
	public String getMsgNodeClick() {
		return msgNodeClick;
	}

	/**
	 * @param msgNodeClick
	 *           The msgNodeClick to set.
	 */
	public void setMsgNodeClick(String msgNodeClick) {
		this.msgNodeClick = msgNodeClick;
	}

	/**
	 * Gibt an, dass dieses GuiTreeElement kein rootElement ist. Nur GuiTree componenten
	 * k&ouml;nnen ihr eigene JDataSet verwalten. <br>
	 * <i>Indicates that this GuiTreeElement does NOT administers its own JDataSet. Only
	 * and GuiTree components can administer its own JDataSet. </i>
	 * 
	 * @see de.guibuilder.framework.IDatasetComponent#isRootElement()
	 */
	public boolean isRootElement() {
		return false;
	}

	/**
	 * Not Implemented
	 */
	public Component getAwtComponent() {
		throw new IllegalComponentStateException("Not Implemented");
	}

}