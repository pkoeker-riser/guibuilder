/*
 * Created on 01.08.2003
 */
package de.guibuilder.test.utils;
import java.io.File;

import electric.xml.Document;
/**
 * @author kknobloch
 */
public class TestXMLSupport {


	public Document loadFromFileSystem(String path) {
		
		Document myDoc;
		
		try {
			File f = new File(path);
			myDoc = new Document(f);
		} catch (Exception ex) {
			System.out.println(ex);
			myDoc = new Document();
		}
		return myDoc;
		
	}

}
