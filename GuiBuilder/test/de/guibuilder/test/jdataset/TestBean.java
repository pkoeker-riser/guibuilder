/*
 * Created on 04.11.2004
 */
package de.guibuilder.test.jdataset;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import junit.framework.TestCase;

/**
 * JDataSet <--> JavaBean-Transformation
 * @author PKOEKER
 */
public class TestBean extends TestCase {
	public void testBean() {
		JDataSet ds = new JDataSet("Bean");
		JDataTable tbl = new JDataTable("Bean");
		ds.addRootTable(tbl);
		tbl.addColumn("Name1", Types.VARCHAR);
		tbl.addColumn("Name2", Types.VARCHAR);
		tbl.addColumn("Name3", Types.VARCHAR);
		tbl.addColumn("Name4", Types.VARCHAR);
		tbl.addColumn("Geschlecht", Types.CHAR);
		tbl.addColumn("Anzahl", Types.INTEGER);
		tbl.addColumn("JaNein", Types.BOOLEAN);
		
		tbl.addColumn("Lang", Types.DECIMAL);
		tbl.addColumn("einfach", Types.DECIMAL);
		tbl.addColumn("Doppelt", Types.DECIMAL);
		tbl.addColumn("Preis", Types.DECIMAL);
		tbl.addColumn("Datum", Types.DATE);
		tbl.addColumn("Zeit", Types.TIME);
		tbl.addColumn("Stempel", Types.TIMESTAMP);

		JDataRow row = tbl.createNewRow();
		ds.addChildRow(row);
		// get
		row.setValue("Name1", "Name1--");
		row.setValue("Name2", "Name2++");
		row.setValue("Name3", "Name3##");
		row.setValue("Anzahl", 4711);
		
		MyTestBeanMethod b = new MyTestBeanMethod();
		row.getBean(b);
		// set
		b.setName1("Name1-set");
		b.setAnzahl(13);
		b.setJaNein(true);
		b.setLang(12345L);
		b.setEinfach(123.12F);
		b.setPreis(new BigDecimal("12.34"));
		b.setDatum(new Date());
		b.setZeit(new Time(new Date().getTime()));
		b.setStempel(new Timestamp(new Date().getTime()));
		
		row.commitChanges();
		row.setBean(b);
	}
	private MyTestBeanMethod getBeanMethod() {
		MyTestBeanMethod b = new MyTestBeanMethod();
		// set
		b.setName1("Name1-set");
		b.setGeschlecht('f');
		b.setAnzahl(13);
		b.setJaNein(true);
		b.setLang(12345L);
		b.setEinfach(123.12F);
		b.setDoppelt(87654321);
		b.setPreis(new BigDecimal("12.34"));
		b.setDatum(new Date());
		b.setZeit(new Time(new Date().getTime()));
		b.setStempel(new Timestamp(new Date().getTime()));
		return b;
	}
	private MyTestBeanField getBeanField() {
		MyTestBeanField b = new MyTestBeanField();
		// set
		b.name1 = "Name1-set";
//		b.geschlecht = 'm';
//		b.anzahl = 13;
//		b.jaNein = true;
//		b.lang = 12345L;
//		b.einfach = 123.12F;
//		b.doppelt = 87654321;
		b.geschlecht = new Character('m');
		b.anzahl = new Integer(13);
		b.jaNein = Boolean.TRUE;
		b.lang = new Long(12345L);
		b.einfach = new Float(123.12F);
		b.doppelt = new Double(87654321);
		b.preis = new BigDecimal("12.34");
		b.datum = new Date();
		b.zeit = new Time(new Date().getTime());
		b.stempel = new Timestamp(new Date().getTime());
		return b;
	}
	
	public void testDatasetMethod() {
		MyTestBeanMethod b = this.getBeanMethod();
		JDataSet ds = JDataSet.toDataset(b);
		JDataRow row = ds.getRow();
		row.setValue("doppelt", 1234567);
		row.setValue("Name2", "Name-2" );
		row.setValue("Name3", "Name-3" );
		row.setValue("Name4", "Name-4" );
		row.setValue("Geschlecht", "w");
		ds.toObject(b);
		//assertEquals(b.getDoppelt(), 1234567.0);
		assertEquals(b.getName2(), "Name-2");
		assertEquals(b.getName3(), "Name-3");
		assertEquals(b.getGeschlecht(), 'w');
	}
	
	public void testDatasetField() {
		MyTestBeanField b = this.getBeanField();
		JDataSet ds = JDataSet.toDataset(b);
		JDataRow row = ds.getRow();
		row.setValue("doppelt", 1234567);
		row.setValue("Name2", "Name-2" );
		row.setValue("Name3", "Name-3" );
		row.setValue("Name4", "Name-4" );
		row.setValue("Geschlecht", "w");
		ds.toObject(b);
		assertEquals(new Double(1234567), b.doppelt );
		assertEquals(b.name2, "Name-2");
		assertEquals(b.name3, "Name-3");
		assertEquals(b.geschlecht, new Character('w'));
	}
	
	
	public static class MyTestBeanMethod {
		private String name1;
		private String name2;
		private String name3;
		private static String name4;
		private int anzahl;
		private boolean jaNein;
		private char geschlecht;
		
		private long lang;
		private double doppelt;
		private float einfach;
		private BigDecimal preis;
		private Date datum;
		private Time zeit;
		private Timestamp stempel;
		
		public int getAnzahl() {
			return anzahl;
		}
		public void setAnzahl(int anzahl) {
			this.anzahl = anzahl;
		}
		public boolean isJaNein() {
			return jaNein;
		}
		public void setJaNein(boolean jaNein) {
			this.jaNein = jaNein;
		}
		public String getName1() {
			return name1;
		}
		public void setName1(String name1) {
			this.name1 = name1;
		}
		public String getName2() {
			return name2;
		}
		public void setName2(String name2) {
			this.name2 = name2;
		}
		public String getName3() {
			return name3;
		}
		public void setName3(String name3) {
			this.name3 = name3;
		}
		public String getName4() {
			return name4;
		}
		public void setName4(String n) {
			name4 = n;
		}
		/**
		 * @return Returns the datum.
		 */
		public Date getDatum() {
			return this.datum;
		}
		/**
		 * @param datum The datum to set.
		 */
		public void setDatum(Date datum) {
			this.datum = datum;
		}
		/**
		 * @return Returns the doppelt.
		 */
		public double getDoppelt() {
			return this.doppelt;
		}
		/**
		 * @param doppelt The doppelt to set.
		 */
		public void setDoppelt(double doppelt) {
			this.doppelt = doppelt;
		}
		/**
		 * @return Returns the einfach.
		 */
		public float getEinfach() {
			return this.einfach;
		}
		/**
		 * @param einfach The einfach to set.
		 */
		public void setEinfach(float einfach) {
			this.einfach = einfach;
		}
		/**
		 * @return Returns the lang.
		 */
		public long getLang() {
			return this.lang;
		}
		/**
		 * @param lang The lang to set.
		 */
		public void setLang(long lang) {
			this.lang = lang;
		}
		/**
		 * @return Returns the preis.
		 */
		public BigDecimal getPreis() {
			return this.preis;
		}
		/**
		 * @param preis The preis to set.
		 */
		public void setPreis(BigDecimal preis) {
			this.preis = preis;
		}
		/**
		 * @return Returns the stempel.
		 */
		public Timestamp getStempel() {
			return this.stempel;
		}
		/**
		 * @param stempel The stempel to set.
		 */
		public void setStempel(Timestamp stempel) {
			this.stempel = stempel;
		}
		/**
		 * @return Returns the zeit.
		 */
		public Time getZeit() {
			return this.zeit;
		}
		/**
		 * @param zeit The zeit to set.
		 */
		public void setZeit(Time zeit) {
			this.zeit = zeit;
		}
		/**
		 * @return Returns the geschlecht.
		 */
		public char getGeschlecht() {
			return this.geschlecht;
		}
		/**
		 * @param geschlecht The geschlecht to set.
		 */
		public void setGeschlecht(char geschlecht) {
			this.geschlecht = geschlecht;
		}
	}
	public static class MyTestBeanField {
		public String name1;
		public String name2;
		public String name3;
		public static String name4;
		public Integer anzahl;
		public Boolean jaNein;
		public Character geschlecht;
		
		public Long lang;
		public Double doppelt;
		public Float einfach;
		public BigDecimal preis;
		public Date datum;
		public Time zeit;
		public Timestamp stempel;
	}
}
