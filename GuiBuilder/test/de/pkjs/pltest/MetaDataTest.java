/*
 * Created on 23.01.2005
 */
package de.pkjs.pltest;

import junit.framework.TestCase;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.pkjs.pl.PL;

/**
 * @author peter
 */
public class MetaDataTest extends TestCase {
	private PL pl = AllTests.getPL();
	String datasetName = "AdresseKurz";
	String rootTableName = "adresse";
	String persTableName = "person";

	public void testMetaDatatable() {
	   try {
	      JDataSet ds = pl.getDataset(datasetName,1);
	      JDataTable tblAdresse = ds.getDataTable();
	      tblAdresse.getMetaDataSet();
	      //System.out.println(dsMeta);
	   } catch (Exception ex) {
	      fail(ex.getMessage());
	   }
	}
	public void testDatabaseMetadata() {
	   JDataSet ds = pl.getMetaDataSet();
	   System.out.println(ds);
	}
}
