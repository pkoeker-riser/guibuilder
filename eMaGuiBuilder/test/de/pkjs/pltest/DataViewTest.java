/*
 * Created on 21.02.2004
 */
package de.pkjs.pltest;

import junit.framework.TestCase;

import de.jdataset.*;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
/**
 * @author peter
 */
public class DataViewTest extends TestCase {
	//##private PL pl = AllTests.getPL();
	// Methods
	public void test() {
		try {
			JDataSet ds = new JDataSet("Sort");
			JDataTable tbl = new JDataTable("Sort");
			ds.addRootTable(tbl);
			tbl.addColumn("Number", Types.BIGINT);
			tbl.addColumn("Date", Types.DATE);
			tbl.addColumn("Time", Types.TIME);
			tbl.addColumn("DateTime", Types.TIMESTAMP);
			tbl.addColumn("String", Types.VARCHAR);
			//
			this.addRows(ds, tbl);
			{
				DataView view = new DataView(null, "Number");
				Collection col = ds.getChildRows(view);
				Iterator it = col.iterator();
				int cnt = 0;
				while (it.hasNext()) {
					cnt++;
					JDataRow row = (JDataRow)it.next();
					int iValue = row.getValueInt("Number");
					assertEquals("Number muß aufsteigend sortiert sein", cnt, iValue);
				}
			}
			{
				DataView view = new DataView(null, "Date");
				Collection col = ds.getChildRows(view);
				Iterator it = col.iterator();
				int cnt = 0;
				while (it.hasNext()) {
					JDataRow row = (JDataRow)it.next();
					cnt++;
					String s = row.getValue("Date");
					switch (cnt) {
						case 1:
							assertEquals("Datum falsch sortiert", s, "10.10.2003");
							break;
						case 2:
							assertEquals("Datum falsch sortiert", s, "11.10.2003");
							break;
						case 3:
							assertEquals("Datum falsch sortiert", s, "10.11.2003");
							break;
						case 4:
							assertEquals("Datum falsch sortiert", s, "10.12.2004");
							break;
					}
				}
			}
			{
				DataView view = new DataView(null, "Time");
				Collection col = ds.getChildRows(view);
				Iterator it = col.iterator();
				int cnt = 0;
				while (it.hasNext()) {
					JDataRow row = (JDataRow)it.next();
					cnt++;
					String s = row.getValue("Time");
					switch (cnt) {
						case 1:
							assertEquals("Zeit falsch sortiert", s, "09:00");
							break;
						case 2:
							assertEquals("Zeit falsch sortiert", s, "10:00");
							break;
						case 3:
							assertEquals("Zeit falsch sortiert", s, "10:01");
							break;
						case 4:
							assertEquals("Zeit falsch sortiert", s, "11:00");
							break;
					}
				}
			}
			{
				DataView view = new DataView(null, "DateTime");
				Collection col = ds.getChildRows(view);
				Iterator it = col.iterator();
				int cnt = 0;
				while (it.hasNext()) {
					JDataRow row = (JDataRow)it.next();
					cnt++;
					String s = row.getValue("DateTime");
					switch (cnt) {
						case 1:
							assertEquals("DatumZeit falsch sortiert", s, "10.10.2003 10:00");
							break;
						case 2:
							assertEquals("DatumZeit falsch sortiert", s, "10.10.2003 10:10");
							break;
						case 3:
							assertEquals("DatumZeit falsch sortiert", s, "11.10.2003 10:10");
							break;
						case 4:
							assertEquals("DatumZeit falsch sortiert", s, "11.10.2003 10:20");
							break;
					}
				}
			}
			{
				DataView view = new DataView(null, "String");
				Collection col = ds.getChildRows(view);
				Iterator it = col.iterator();
				int cnt = 0;
				// TODO : Beim Sortieren von String wie Groß/klein-Schreibung berücksichtigen
				while (it.hasNext()) {
					JDataRow row = (JDataRow)it.next();
					cnt++;
					String s = row.getValue("String");
					switch (cnt) {
						case 1:
							assertEquals("String falsch sortiert", s, "Aaa");
							break;
						case 2:
							assertEquals("String falsch sortiert", s, "Baaa");
							break;
						case 3:
							assertEquals("String falsch sortiert", s, "Xxx");
							break;
						case 4:
							assertEquals("String falsch sortiert", s, "ZZZZ");
							break;
					}
				}
			}
			// StateFilter
			ds.commitChanges();
			ds.getChildRow(0); // nix
			
			JDataRow row1 = ds.getChildRow(1);
			row1.setInserted(true); // inserted
			row1.getDataValue("String").setModified(true); // modi
			
			JDataRow row2 = ds.getChildRow(2);
			row2.getDataValue("String").setModified(true); // modi
			
			JDataRow row3 = ds.getChildRow(3);
			row3.setDeleted(true); // deleted
			{	
				DataView view = new DataView(DataView.INSERTED);
				Collection col = ds.getChildRows(view);
				assertEquals("Anzahl der eingefügten Zeilen ist falsch", col.size(),1);
			}
			{	
				DataView view = new DataView(DataView.DELETED);
				Collection col = ds.getChildRows(view);
				assertEquals("Anzahl der gelöschten Zeilen ist falsch", col.size(),1);
			}
			{	
				DataView view = new DataView(DataView.MODIFIED);
				Collection col = ds.getChildRows(view);
				assertEquals("Anzahl der geänderten Zeilen ist falsch", col.size(),2);
			}
			{	
				DataView view = new DataView(DataView.INSERTED | DataView.DELETED);
				Collection col = ds.getChildRows(view);
				assertEquals("Anzahl der eingefügten+gelöschten Zeilen ist falsch", col.size(),2);
			}
			{	
				DataView view = new DataView(DataView.INSERTED | DataView.MODIFIED);
				Collection col = ds.getChildRows(view);
				assertEquals("Anzahl der eingefügten+geänderten Zeilen ist falsch", col.size(),2);
			}
			{	
				DataView view = new DataView(DataView.UNCHANGED);
				Collection col = ds.getChildRows(view);
				assertEquals("Anzahl der unveränderten Zeilen ist falsch", col.size(),1);
			}
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
	private void addRows(RowContainer parent, JDataTable tbl) {
		{
			JDataRow row = tbl.createNewRow();
			row.setValue("Number", 1); // 1
			row.setValue("Date","10.10.2003"); // 1
			row.setValue("Time", "10:00"); // 2
			row.setValue("DateTime", "10.10.2003 10:00"); // 1
			row.setValue("String", "Xxx"); // 3
			parent.addChildRow(row);
		}
		{
			JDataRow row = tbl.createNewRow();
			row.setValue("Number", 4);
			row.setValue("Date","10.11.2003"); // 3
			row.setValue("Time", "11:00"); // 4
			row.setValue("DateTime", "11.10.2003 10:20"); // 4
			row.setValue("String", "Aaa"); // 1
			parent.addChildRow(row);
		}
		{
			JDataRow row = tbl.createNewRow();
			row.setValue("Number", 3);
			row.setValue("Date","11.10.2003"); // 2
			row.setValue("Time", "10:01"); // 3
			row.setValue("DateTime", "11.10.2003 10:10"); // 3
			row.setValue("String", "Baaa"); // 2
			parent.addChildRow(row);
		}
		{
			JDataRow row = tbl.createNewRow();
			row.setValue("Number", 2);
			row.setValue("Date","10.12.2004"); // 4
			row.setValue("Time", "09:00"); // 1
			row.setValue("DateTime", "10.10.2003 10:10"); // 2
			row.setValue("String", "ZZZZ"); // 4
			parent.addChildRow(row);
		}
	}
}
