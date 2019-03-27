/*
 * Created on 14.07.2003
 *
 */
package de.guibuilder.test.framework;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * TestSuite für de.guibuilder.framework.
 * Führt alle TestCases des Packages aus.
 * @author kknobloch
 * 
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite =
			new TestSuite("Test for de.guibuilder.framework");
		//$JUnit-BEGIN$
		suite.addTestSuite(GuiTextTest.class);
		suite.addTestSuite(GuiNumberTest.class);
		suite.addTestSuite(GuiDateTest.class);
		suite.addTestSuite(GuiTimeTest.class);
		suite.addTestSuite(GuiMoneyTest.class);
		suite.addTestSuite(GuiPasswordTest.class);
		suite.addTestSuite(GuiMemoTest.class);
		suite.addTestSuite(GuiButtonTest.class);
		suite.addTestSuite(GuiLabelTest.class);
		suite.addTestSuite(GuiListTest.class);
		//$JUnit-END$
		return suite;
	}
}
