package de.pkjs.pltest;

import de.pkjs.pl.PL;
import de.pkjs.pl.PLException;
import junit.framework.TestCase;

public class TestSeq extends TestCase {
	private PL pl = AllTests.getPL();
	public void test1() {
		try {
			pl.getOID();
			pl.getOID("AdresseSeq");
		} catch (PLException ex) {
			fail(ex.getLocalizedMessage());
		}
	}
}
