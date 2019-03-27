package de.pkjs.pltest;

import java.text.SimpleDateFormat;

import junit.framework.TestCase;
import de.pkjs.pl.Database;
import de.pkjs.pl.PL;
import de.pkjs.pl.PLException;
import electric.xml.Document;

public class TestMetadata extends TestCase {
	private PL pl = AllTests.getPL();
	public void test1() {
		String s = pl.getCurrentDatabaseName();
		assertNotNull(s);
	}
	public void test2() {
		String s = pl.getDatabaseMetaData();
		assertNotNull(s);
		String sPL = pl.getPLMetaData();
		assertNotNull(sPL);
	}
	public void test3() {
		Document doc = pl.getDatabaseMetaDataDoc();
		assertNotNull(doc);
		Document docPL = pl.getPLMetaDataDoc();
		assertNotNull(docPL);
	}
	public void test4() {
		SimpleDateFormat format = pl.getDateFormat();
		assertNotNull(format);
		SimpleDateFormat t = pl.getTimeFormat();
		assertNotNull(t);
		SimpleDateFormat ts = pl.getTimestampFormat();
		assertNotNull(ts);
		
		String now = pl.getNowString();
		assertNotNull(now);

		String tnow = pl.getTodayNowString();
		assertNotNull(tnow);
	}
	public void test5() {
		pl.getMaxIdle();
		pl.getMaxActive();
		pl.getMinIdle();
		pl.getNumActive();
		pl.getNumIdle();
		pl.getConnectionTimeOut();
	}
	public void testDatabase() {
		Database db = pl.getCurrentDatabase();
		db.getCreatedTimeStamp();
		db.getResetTimeStamp();
		db.getNumberOfNamedSequences();
		db.getNumberOfNamedStatements();
		db.getNumberOfRequests();
		db.getNumberOfTables();
		db.hasDefaultGetOidStatement();
		db.hasDefaultSetOidStatement();
		db.isAutocommit();
		
		db.getDataTableNames();
		db.getDatabaseConfig();
	}
	public void testPing() {
		try {
			String s = pl.pingDatabase();
			assertEquals("pong", s);
		} catch (PLException ex) {
			fail(ex.getMessage());
		}
	}
}
