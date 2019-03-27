/*
 * Created on 14.07.2003
 *
 */
package de.guibuilder.test;

import junit.swingui.TestRunner; 

/**
 * TestStarter ist das JUnit-Main, welches benötigt wird um den automatischen
 * Test auch ausserhalb der Entwicklungsumgebung starten zu können.
 * @author kknobloch
 *
 */
public class TestStarter {

	private TestRunner m_testRunner = null; 

	public TestStarter(  ) { 


		String[] tests = new String[2]; 
		tests[0]="de.guibuilder.test.framework.AllTests"; 
		tests[1]="de.guibuilder.test.jdataset.AllTests"; 
		m_testRunner = new TestRunner(); 
		m_testRunner.start(tests);

	} 


	public static void main(String[] args) {
		
		new TestStarter(); 
		
	}
	
}
