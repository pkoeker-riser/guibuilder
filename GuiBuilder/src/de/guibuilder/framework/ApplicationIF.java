package de.guibuilder.framework;

import de.jdataset.*;
/**
 * Dieses Interface ist von einem Application-Adapter zu implementiern.
 * Z.Z. wird dieses Interface verwendet, damit ComboBoxen mit
 * einem Wertevorrat gefüllt werden können.
 * @author Peter Köker
 * http://www.guibuilder.de
 * 11.11.2002 16:52:31
 * @see GuiSession#setAdapter
 */
public interface ApplicationIF {
	/**
	 * Liefert ein JDataSet unter dem angegebenen Namen.	 * @param modelname	 * @return JDataSet	 */
	public JDataSet getDataset(String datasetname) throws Exception;
}
