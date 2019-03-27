/*
 * Created on 25.01.2004
 */
package de.pkjs.pltest;

import junit.framework.TestCase;
import de.jdataset.*;
import de.pkjs.pl.PL;
import de.pkjs.pl.PLException;

import electric.xml.*;

/**
 * @author peter
 */
public class XmlTest extends TestCase {

	public void test() {
		PL pl = AllTests.getPL();
		System.out.println("Start XML Test");
		// 1. DataSet einlesen, --> XML --> DataSet --> XML
		{
			JDataSet ds1;
			try {
				ds1 = pl.getDataset("AdresseKurz", 1);
				Document doc1 = ds1.getXml();
				String s1 = doc1.toString();
				JDataSet ds2 = new JDataSet(doc1);
				String s2 = ds2.getXml().toString();
				assertEquals("DataSet1 != DataSet2", s1, s2);
			} catch (PLException e) {
				e.printStackTrace();
			}

		}
		// 2.
		{

		}
		System.out.println("End XML Test");

	}
}