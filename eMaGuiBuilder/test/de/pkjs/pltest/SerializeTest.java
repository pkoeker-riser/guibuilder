package de.pkjs.pltest;


import de.jdataset.JDataSet;
import de.pkjs.pl.PL;
import de.pkjs.util.Convert;
import junit.framework.TestCase;

public class SerializeTest extends TestCase {
	private PL pl = AllTests.getPL();
	public void test1() {
		try {
			JDataSet ds = pl.getDataset("AdresseKurz", 1);
			long start = System.currentTimeMillis();
			int anz = 1000;
			byte[] b = null;
			for (int i = 0; i < anz; i++) {
				b = Convert.serialize(ds, false);
				Convert.deserialize(b, false);				
			}
			long end = System.currentTimeMillis();
			long dur = end - start;
			System.out.println("Uncompressed: " + b.length + " "+dur);
		} catch (Exception ex) {
			fail(ex.getLocalizedMessage());
		}
	}
	public void test2() {
		try {
			JDataSet ds = pl.getDataset("AdresseKurz", 1);
			long start = System.currentTimeMillis();
			int anz = 1000;
			byte[] b = null;
			for (int i = 0; i < anz; i++) {
				b = Convert.serialize(ds, true);
				Convert.deserialize(b, true);				
			}
			long end = System.currentTimeMillis();
			long dur = end - start;
			System.out.println("Compressed: "+b.length +" "+dur);
		} catch (Exception ex) {
			fail(ex.getLocalizedMessage());
		}
	}
}
