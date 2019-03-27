/*
 * Created on 13.09.2003
 */
package de.pkjs.pltest;

import junit.framework.TestCase;
import de.jdataset.*;
import de.pkjs.pl.IPLContext;

/**
 * @author peter
 */
public class ExecuteSqlTest extends TestCase {
	private IPLContext pl = AllTests.getPL();
	
	public void test() {
		try {
			String sql = "SELECT * FROM termin T, person P, funktionen F "
					+ " WHERE T.fk_persid = P.persid AND P.fk_funkid = F.funkid";
			JDataSet ds = pl.getDatasetSql("test", sql);		
			System.out.println(ds.getXml());
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
}
