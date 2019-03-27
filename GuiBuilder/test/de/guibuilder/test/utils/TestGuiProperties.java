/*
 * Created on 21.07.2003
 *
 */
package de.guibuilder.test.utils;

import java.io.File;

import de.guibuilder.framework.GuiUtil;

import electric.xml.Document;
import electric.xml.Element;
/**
 * Zusammenfassung von Hilfsklassen, die mit der Behandlung der 
 * GuiBuilder-Properties zu tun haben.
 * @author kknobloch
 *
 */
public class TestGuiProperties {
	/**
	 * Setzen eines Properties in GuiBuilderConfig.xml
	 * Ist das Property nicht in GuiBuilderConfig.xml vorhanden, 
	 * wird es eingefügt.
	 * 
	 * @param propName, Name des zu setzenden Property
	 * @param propValue, Wert der gesetzt werden soll
	 * @return true, wenn Property gesetzt werden konnte. Sonst false.
	 */
	public boolean setGuiProperty(String propName, String propValue) {

		try {
			File f = new File(GuiUtil.GUIBUILDER_CONFIG);
			Document myDoc = new Document(f);
			Element rootElem = myDoc.getRoot();
			
			if( rootElem == null ) {
				System.out.println("ERR: rootElem is null");
				return false;
			}
			Element dataElem = rootElem.getElement("Data");
			if( dataElem == null ) {
				System.out.println("ERR: dataElem is null");
				return false;
			}
			Element propElem = dataElem.getElement("Properties");
			if( propElem == null ) {
				System.out.println("ERR: propElem is null");
				return false;
			}
			
			Element toSet = propElem.getElement(propName);
			if( toSet == null ) {
				
				toSet = propElem.addElement(propName);
				toSet.setString(propValue);
			}
			else
				toSet.setString(propValue);
				
			myDoc.write(f);

			GuiUtil.loadGuiPropXml();
			
			return true;			
			
		} catch (Exception ex) {
			System.out.println(ex);
			return false;
		}		
	}

}
