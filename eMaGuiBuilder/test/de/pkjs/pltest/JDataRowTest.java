/*
 * Created on 14.05.2003
 */
package de.pkjs.pltest;
import de.jdataset.*;
import electric.xml.*;

import java.math.BigDecimal;
import java.sql.*;

import junit.framework.TestCase;
/**
 * @author peter
 */
public class JDataRowTest extends TestCase {
	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JDataRowTest.class);

	public void test() {
		System.out.println("--- BEGIN JDataRowTest ---");
		try {
			JDataSet ds = new JDataSet("Dataset");
			JDataTable tblRoot = new JDataTable("WurzelTabelle");
			{
				JDataColumn colpk = tblRoot.addColumn("id", Types.INTEGER);
				colpk.setPrimaryKey(true);
				colpk.setKeySeq(1);
				tblRoot.addColumn("fk1", Types.INTEGER);
				tblRoot.addColumn("fk2", Types.INTEGER);
				tblRoot.addColumn("fk3", Types.INTEGER);
			}
			ds.addRootTable(tblRoot);
			assertEquals("Die Rolle der Table muß ROOT sein", tblRoot.getTableType(), JDataTable.ROOT_TABLE);
			// Child Tables
			JDataTable tblChild1 = new JDataTable("Child1");
			{
				JDataColumn colpk = tblChild1.addColumn("id1", Types.INTEGER);
				colpk.setPrimaryKey(true);
				tblChild1.addColumn("fk1", Types.INTEGER);
			}
			JDataTable tblChild2 = new JDataTable("Child2");
			{
				JDataColumn colpk = tblChild2.addColumn("id2", Types.INTEGER);
				colpk.setPrimaryKey(true);
			}
			tblChild2.addColumn("fk2", Types.INTEGER);
			
			JDataTable tblChild3 = new JDataTable("Child3");
			{
				JDataColumn colpk = tblChild3.addColumn("id3", Types.INTEGER);
				colpk.setPrimaryKey(true);
				tblChild3.addColumn("fk3", Types.INTEGER);
			}
			tblRoot.addChildTable(tblChild1, "fk1");
			assertEquals("Die Rolle der Table muß CHILD sein", tblChild1.getTableType(), JDataTable.CHILD_TABLE);
			tblRoot.addChildTable(tblChild2, "fk2");
			assertEquals("JDataTable#isChildTable fehlerhaft",tblRoot.isChildTable(tblChild2), true);
			try {
				logger.info("Jetzt kommt eine erwartete Fehlermessage");
				System.err.println("Jetzt kommt eine erwartete Fehlermessage");
				tblRoot.addChildTable(tblChild1, "fk1");
				fail("Erneute Zuweisung einer Child Table");
			} catch (Exception ex) {} // Erwarteter Fehler
			logger.info("Ende der erwarteten Fehlermessage");
			{
				tblRoot.getChildTable("Child1");
				try {
					tblRoot.getChildTable("xxx");
					fail("Hier fehlt eine Exception");
				} catch (Exception ex) {} // Erwarteter Fehler
			}
			// Zugriff auch Child Table über fk
			{
				try {
					tblRoot.getChildTable("Child1");
				} catch (Exception ex) {
					fail("getChildTable über fk: Child Table nicht gefunden");
				}
			}
			{
				try {
					tblRoot.getChildTable("xxx");
					fail("getChildTable mit falschem refnamen: Hier fehlt eine Exception");
				} catch (Exception ex) {} // Erwarteter Fehler
			}
			// Parent tables
			JDataTable tblParent1 = new JDataTable("Parent1");
			JDataColumn pk1 = tblParent1.addColumn("pk", Types.INTEGER);
			pk1.setPrimaryKey(true);
			
			JDataTable tblParent2 = new JDataTable("Parent2");
			JDataColumn pk2 = tblParent2.addColumn("pk", Types.INTEGER);
			pk2.setPrimaryKey(true);
			
			JDataTable tblParent31 = new JDataTable("Parent3");
			JDataColumn pk31 = tblParent31.addColumn("pk", Types.INTEGER);
			pk31.setPrimaryKey(true);
			tblParent31.setRefname("Parent3Ref1");
			
			JDataTable tblParent32 = new JDataTable("Parent3");
			JDataColumn pk32 = tblParent32.addColumn("pk", Types.INTEGER);
			pk32.setPrimaryKey(true);
			tblParent32.setRefname("Parent3Ref2");
			{
				try {
					tblRoot.addParentTable(tblParent31, "xxx");
					fail("addParentTable trotz falscher fk-Angabe!");
				} catch (Exception ex) {} // Erwarteter Fehler
				tblRoot.addParentTable(tblParent1, "fk1");
				assertEquals("Die Rolle der Table muß PARENT sein",tblParent1.getTableType(), JDataTable.PARENT_TABLE);
				assertEquals("JDataTable#isParentTable fehlerhaft", tblRoot.isParentTable(tblParent1), true);
				tblRoot.addParentTable(tblParent2,"fk2");
				try {
					tblRoot.addParentTable(tblParent2,"fk2");
					fail("Erneute Zuweisung einer Parent Table");
				} catch (Exception ex) {} // Erwarteter Fehler
			}
			try {
				tblRoot.addParentTable(tblParent31, "fk3");				
				tblRoot.addParentTable(tblParent32, "fk3");				
			} catch (Exception ex) {
				 fail(ex.getMessage());
			}
			// Zugriff auf Parent Table über refnamen
			{
				try {
					tblRoot.getParentTable("Parent1");
				} catch (Exception ex) {
					fail("getParentTable über Refname: Parent Table nicht gefunden");
				}
				try {
					tblRoot.getParentTable("Parent3Ref1");
				} catch (Exception ex) {
					fail("getParentTable über Refname: Parent Table nicht gefunden");
				}
			}
			{
				try {
					tblRoot.getParentTable("xxx");
					fail("getParentTable mit falschem Refnamen: Hier fehlt eine Exception");
				} catch (Exception ex) {} // Erwarteter Fehler
			}
			// DataRows
			// Root
			JDataRow rootRow = ds.createChildRow();
			// Child 1 (zwei rows)
			JDataRow child1Row1 = tblChild1.createNewRow();
			rootRow.addChildRow(child1Row1);
			JDataRow child1Row2 = tblChild1.createNewRow();
			rootRow.addChildRow(child1Row2);
			// Parent1
			JDataRow parent1Row = tblParent1.createNewRow();
			rootRow.addParentRow(parent1Row);
			// Doppelt?? funktioniert (leider)
			//##JDataRow parent1Rowx = tblParent1.createNewRow();
			//##rootRow.addParentRow(parent1Rowx);
			// Parent2
			JDataRow parent2Row = tblParent2.createNewRow();
			rootRow.addParentRow(parent2Row);
			// Parant3
			JDataRow parent31Row = tblParent31.createNewRow();
			rootRow.addParentRow(parent31Row);
			JDataRow parent32Row = tblParent32.createNewRow();
			rootRow.addParentRow(parent32Row);
			// XML-test
			{
				Document doc = ds.getXml();
				String s1 = doc.toString();
				JDataSet dsd = new JDataSet(doc);
				Document doc2 = dsd.getXml();
				String s2 = doc2.toString();
				assertEquals("Die getXML --> XML-Constructor liefert abweichendes Ergebnis", s1, s2);
			}
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
		System.out.println("--- END JDataRowTest ---");
	}
	public void testChildRows() {
		try {
			JDataSet ds = new JDataSet("Dataset");
			JDataTable tblRoot = new JDataTable("RootTable");
			{
				JDataColumn colpk = tblRoot.addColumn("id", Types.INTEGER);
				colpk.setPrimaryKey(true);
				colpk.setKeySeq(1);
				tblRoot.addColumn("col1", Types.VARCHAR);
				tblRoot.addColumn("col2", Types.BIGINT);
				tblRoot.addColumn("date", Types.DATE);
				
			}
			ds.addRootTable(tblRoot);
			// Child Table
			JDataTable tblChild = new JDataTable("ChildTable");
			{
				JDataColumn colpk = tblChild.addColumn("id", Types.INTEGER);
				colpk.setPrimaryKey(true);
				colpk.setKeySeq(1);
				tblChild.addColumn("col1", Types.VARCHAR);
				tblChild.addColumn("fk1", Types.BIGINT);
				tblChild.addColumn("date", Types.DATE);
				tblChild.addColumn("amount", Types.DECIMAL);
				
			}
			tblRoot.addChildTable(tblChild, "fk1");
			JDataRow rowRoot1 = ds.createChildRow();
			ds.createChildRow("RootTable");
			JDataRow rowRoot3 = tblRoot.createNewRow();
			ds.addChildRow(rowRoot3, 1);
			
			JDataRow rowChild1 = rowRoot1.createChildRow();
			rowChild1.setValue("date", new java.util.Date());
			rowChild1.getValueDate("date");
			rowRoot1.createChildRow("ChildTable");
			
			rowChild1.setValue("amount", new BigDecimal("1.23"));
			BigDecimal big = rowChild1.getValueBigDecimal("amount");
			assertEquals(big, new BigDecimal("1.23"));
			
			rowChild1.getValue(4); // Amount
			
			JDataRow rowClone = rowChild1.cloneRow(rowChild1.getDataTable());
			String s1 = rowChild1.toString();
			String s2 = rowClone.toString();
			assertEquals(s1,s2);
			
			rowChild1.getParentRow();
			boolean b = rowChild1.verify();
			assertTrue(b);
			boolean modi = rowChild1.isModified();
			assertTrue(modi);
			modi = rowChild1.isModified("amount");
			assertTrue(modi);
			rowChild1.commitChanges();
			modi = rowChild1.isModified();
			assertFalse(modi);
			
			rowChild1.getDataColumn("amount");
			
			rowChild1.resetErrorState();
			
			JDataRow rowChild3 = tblChild.createNewRow();
			rowRoot1.addChildRow(rowChild3, 0);
		} catch (Exception ex) {
			fail(ex.getMessage());
		}

	}
	public void testBean() {
		try {
			JDataSet ds = new JDataSet("Dataset");
			JDataTable tblRoot = new JDataTable("RootTable");
			{
				JDataColumn colpk = tblRoot.addColumn("id", Types.INTEGER);
				colpk.setPrimaryKey(true);
				colpk.setKeySeq(1);
				tblRoot.addColumn("col1", Types.VARCHAR);
				tblRoot.addColumn("fk1", Types.BIGINT);
				tblRoot.addColumn("date", Types.DATE);
				tblRoot.addColumn("amount", Types.DECIMAL);
				tblRoot.addColumn("yesno", Types.BOOLEAN);
				tblRoot.addColumn("chr", Types.CHAR);
				tblRoot.addColumn("flt", Types.REAL);
				tblRoot.addColumn("dbl", Types.REAL);
				tblRoot.addColumn("integer", Types.INTEGER);
				tblRoot.addColumn("shrt", Types.INTEGER);
				tblRoot.addColumn("byt", Types.VARCHAR);
				tblRoot.addColumn("tm", Types.TIME);
				tblRoot.addColumn("stamp", Types.TIMESTAMP);
			}
			ds.addRootTable(tblRoot);
			JDataRow row = ds.createChildRow();
			RowBean rb1  = new RowBean();
			rb1.setAmount(new BigDecimal("12.34"));
			rb1.setCol1("Hallo!");
			rb1.setDate(new java.util.Date());
			rb1.setFk1(123);
			rb1.setInteger(321);
			rb1.setYesno(true);
			row.setBean(rb1);
			row.verify();
			row.toString();
			
			long l = row.getValueLong("fk1");
			assertEquals(123, l);
			
			int i = row.getValueInt("integer");			
			assertEquals(321, i);
			
			boolean b = row.getValueBool("yesno");
			assertEquals(true, b);
			
			row.getValueDate("date");
			
			JDataValue jval = row.getDataValue("stamp");
			jval.getValueTimestamp();
			jval.toString();
			
			RowBean rb2 = new RowBean();
			row.getBean(rb2);
		} catch (Exception ex) {
			fail(ex.getMessage());
		}	
	}
	public class RowBean {
		private BigDecimal amount;
		private long fk1;
		private String col1;
		private java.util.Date date;
		
		private boolean yesno;
		private char chr;
		private float flt;
		private double dbl;
		private int integer;
		private byte byt;
		private short shrt;
		private Time tm;
		private Timestamp stamp;
		
		public BigDecimal getAmount() {
			return amount;
		}
		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}
		public String getCol1() {
			return col1;
		}
		public void setCol1(String col1) {
			this.col1 = col1;
		}
		public java.util.Date getDate() {
			return date;
		}
		public void setDate(java.util.Date date) {
			this.date = date;
		}
		public long getFk1() {
			return fk1;
		}
		public void setFk1(long fk1) {
			this.fk1 = fk1;
		}
		public byte getByt() {
			return byt;
		}
		public void setByt(byte byt) {
			this.byt = byt;
		}
		public char getChr() {
			return chr;
		}
		public void setChr(char chr) {
			this.chr = chr;
		}
		public double getDbl() {
			return dbl;
		}
		public void setDbl(double dbl) {
			this.dbl = dbl;
		}
		public float getFlt() {
			return flt;
		}
		public void setFlt(float flt) {
			this.flt = flt;
		}
		public int getInteger() {
			return integer;
		}
		public void setInteger(int integer) {
			this.integer = integer;
		}
		public short getShrt() {
			return shrt;
		}
		public void setShrt(short shrt) {
			this.shrt = shrt;
		}
		public Timestamp getStamp() {
			return stamp;
		}
		public void setStamp(Timestamp stamp) {
			this.stamp = stamp;
		}
		public Time getTm() {
			return tm;
		}
		public void setTm(Time tm) {
			this.tm = tm;
		}
		public boolean isYesno() {
			return yesno;
		}
		public void setYesno(boolean yesno) {
			this.yesno = yesno;
		}
	}
}
