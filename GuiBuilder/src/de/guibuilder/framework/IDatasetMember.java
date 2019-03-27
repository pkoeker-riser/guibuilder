/*
 * Created on 27.07.2003
 */
package de.guibuilder.framework;

/**
 * @author peter
 */
public interface IDatasetMember {
	/**
	 * Liefert den ElementNamen dieser Komponente im JDataSet.
	 * Da hieraus Path-Ausdrücke gebildet werden muß dieser Name
	 * mit einem ".", "#" oder "@" beginnen.
	 * @see de.jdataset.JDataSet
	 */
	public String getElementName();
	/**
	 * Setzt den ElementNamen dieser Komponente im JDataSet.
	 * Da hieraus Path-Ausdrücke gebildet werden muß dieser Name
	 * mit einem ".", "#" oder "@" beginnen.
	 * Wenn "*" angegeben wird, wird der Name der Komponente verwendet.
	 * @see de.jdataset.JDataSet
	 */
	public void setElementName(String elementName);
	/**
	 * Liefert rekursiv den Path zum Element des Models.
	 */
	public String getElementPath(String current);
	/**
	 * Gibt an, ob ein IDatasetMeber ein eigenes JDataSet verwaltet.
	 * Es wird dann von &uuml;bergeordneten IDatasetMembers ignoriert.<br/>
	 * <i>Indicates that the IDatasetComponent manages its own JDataSet.
	 * It's values are ignored from a parent JDataSetComponent.</i>
	 */
	public boolean isRootElement();

}
