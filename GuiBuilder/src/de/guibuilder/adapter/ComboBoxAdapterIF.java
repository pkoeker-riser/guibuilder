package de.guibuilder.adapter;

import de.jdataset.JDataSet;
/**
 * Eine Implementierung dieses Adapters soll unter der Angabe eines benannten Statements
 * die Daten zum Initialisieren einer ComboBox liefern.
 * Folglich müssen in dem DataSet displayMember und valueMember enthalten sein.
 * @author peter
 *
 */
public interface ComboBoxAdapterIF {
	public JDataSet findNamedDataSet(String namedStatement);
}
