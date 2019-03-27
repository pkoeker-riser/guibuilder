/*
 * Created on 08.08.2003
 */
package de.guibuilder.test;

import de.guibuilder.framework.GuiUtil;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author kknobloch
 * Die TestSuite AllTestGuiBuilder f�hrt den kompletten Test des GuiBuilder durch.
 * Dazu werden hier die TestSuiten der GuiBuilder-Packages (f�r jedes Package gibt es 
 * eine TestSuite) geladen und nacheinander ausgef�hrt.
 * <br>
 * <br>Die Reihenfolge der Ausf�hrung ist:
 * <br>de.guibuilder.framework
 * <br>de.jdataset
 */
public class AllTestsGuiBuilder {
	
	public static Test suite() {
		GuiUtil.setCheckNN(true);
		TestSuite suite = new TestSuite("Test for de.guibuilder.test");
		//$JUnit-BEGIN$
		suite.addTest(de.guibuilder.test.framework.AllTests.suite());
		//$JUnit-END$
		return suite;
	}
}
