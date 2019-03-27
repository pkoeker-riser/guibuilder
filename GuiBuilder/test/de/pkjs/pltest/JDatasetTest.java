/*
 * Created on 14.05.2003
 */
package de.pkjs.pltest;

import de.jdataset.*;

import java.sql.*;
import java.util.*;

import junit.framework.TestCase;
/**
 * @author peter
 */
public class JDatasetTest extends TestCase {
	public void test() {
		System.out.println("--- BEGIN JDatasetTest ---");
		try {
			JDataSet ds = new JDataSet("Dataset1");
			JDataTable tbl = new JDataTable("Table1");
			ds.addRootTable(tbl);
			{
				JDataTable tmp = ds.getDataTable();
				assertEquals("Root Table nicht wiedergefunden!", tmp.getTablename(), "Table1");
				assertEquals("Root Table Eigenschaft nicht gesetzt", 
						ds.isRootTable(tbl), true);
			}
			JDataColumn colID = tbl.addColumn("id", Types.INTEGER);
			colID.setPrimaryKey(true);
			tbl.addColumn("name",Types.VARCHAR);
			JDataRow row = ds.createChildRow();
			row.setValue("id", 4711);
			row.setValue("name", "Rudi");
			{
				int x = ds.getRowCount("Table1");
				assertEquals("Das muß jetzt eine Row sein!",x,1);
			}
			{
				int x0 = ds.getRowCount("Table1xxx");
				assertEquals("Diese Rows darf es nicht geben",x0,0);
			}
			{
				int anz = 0;
				Iterator i = ds.getChildRows("Table1");
				assertNotNull("Der Iterator ist null!!??", i);
				while (i.hasNext()) {
					i.next();
					anz++;
				}
				assertEquals("Anzahl der Zeilen falsch", anz, 1);
			}
			// Rollback
			ds.rollbackChanges();
			{
				int x = ds.getRowCount("Table1");
				assertEquals("Nach Rollback darf es keine Rows mehr geben!" ,x,0);
			}
			assertEquals("Nach Rollback darf kein hasChanges mehr 'true' liefern", ds.hasChanges(), false);
			// Neue Zeile
			try {
				ds.createChildRow("xxxx");
				fail("Hier hätte eine Exception geworfen werden müssen");
			} catch (Exception ex) {} // Diese Exception muß sein
			{
				JDataRow newRow = ds.createChildRow("Table1");
				assertEquals("Nach addRow sollte hasChanges 'true' liefern", ds.hasChanges(), true);
				newRow.setValue("id", 4712);
				newRow.setValue("name", "Rudi");
				try {
					newRow.setValue("xxx", "yyy");
					fail("Diese Spalte darf es in der Row garnicht geben!");
				} catch (Exception ex) {} // Erwarteter Fehler
				ds.commitChanges();
				assertEquals("Nach Commit darf kein hasChanges mehr 'true' liefern", ds.hasChanges(), false);
			}
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
		System.out.println("--- END JDatasetTest ---");
	}
}
