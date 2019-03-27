package de.pkjs.pltest;

import junit.framework.TestCase;
import de.jdataset.JDataSet;
import de.jdataset.ParameterList;
import de.pkjs.pl.PL;
import de.pkjs.pl.PLException;

public class TestStatement extends TestCase {
	private PL pl = AllTests.getPL();

	public void testSt1() {
		try {
			JDataSet ds = pl.getDatasetStatement("Adresse");
			assertNotNull(ds);
		} catch (PLException ex) {
			fail(ex.getLocalizedMessage());
		}
	}
	public void testSt2() {
		try {
			ParameterList list = new ParameterList();
			list.addParameter("Kennung", "RUDI%");
			JDataSet ds = pl.getDatasetStatement("AdresseKennung", list);
			assertNotNull(ds);
		} catch (PLException ex) {
			fail(ex.getLocalizedMessage());
		}
	}
	public void testSt3() {
		try {
			JDataSet ds = pl.getDatasetSql("Adresse", "Select * FROM Adresse");
			assertNotNull(ds);
		} catch (PLException ex) {
			fail(ex.getLocalizedMessage());
		}
	}
	public void testSt4() {
		try {
			ParameterList list = new ParameterList();
			list.addParameter("Kennung", "RUDI%");
			String sql = "Select * FROM Adresse WHERE Kennung LIKE ?";
			JDataSet ds = pl.getDatasetSql("Adresse", sql, list);
			assertNotNull(ds);
		} catch (PLException ex) {
			fail(ex.getLocalizedMessage());
		}
	}
	public void testSt5() {
		try {
			JDataSet ds = pl.getDatasetSql("Adresse", "Select * FROM Adresse", 3);
			assertNotNull(ds);
			assertEquals(ds.getRowCount(), 3);
		} catch (PLException ex) {
			fail(ex.getLocalizedMessage());
		}
	}
	public void testSt6() {
//		try {
//			//NamedStatement ns = pl.getCurrentDatabase().getStatement("Adresse");
//			
//		} catch (PLException ex) {
//			fail(ex.getLocalizedMessage());
//		}
	}
}
