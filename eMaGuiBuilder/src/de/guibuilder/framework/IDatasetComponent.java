/*
 * Created on 27.07.2003
 */
package de.guibuilder.framework;

import de.jdataset.*;

/**
 * 
 * @author peter
 */
public interface IDatasetComponent extends IDatasetMember {
	/**
	 * F&uuml;llt das angegebene JDataSet mit den Inhalten der Komponente
	 * (Oberfl&auml;che).<br/>
	 * <i>Get the JDataSet from the component.</i>
	 */
	public void getDatasetValues(JDataSet ds);
	/**
	 * F&uuml;llt die Komponente (Oberfl&auml;che)
	 * mit den Daten aus dem angegebenen JDataSet.<br>
	 * Zuvor sollte mit reset() die Komponente (Oberfl&auml;che)
	 * zur&uuml;ckgesetzt werden.<br/>
	 * <i>Fill the Komponent with the given JDataSet.</i>
	 */
	public void setDatasetValues(JDataSet ds);
	/**
	 * Teilt der Komponente mit, dass die Benutzereingaben
	 * erfolgreich (vom Persistenz-Layer) verarbeitet wurden.<p>
	 * Zumeist wird die Eigenschaft "modified" nur auf "false"
	 * gesetzt.</p><br/>
	 * <i>Tell the component to commit all its changes 
	 * (i.g. only the field modified changes to false).</i>
	 */
	public void commitChanges();
}
