/*
 * Created on 10.05.2003
 */

package de.pkjs.pltest;

import de.jdataset.*;

import java.sql.*;
import java.util.*;

import junit.framework.TestCase;
/**
 * TestKlasse für JDataTable
 * @author peter
 */
public class JDataTableTest extends TestCase {
	
	public void test() {
		System.out.println("--- BEGIN JDataTableTest ---");
		try {
			// 1. ein DataTable-Object erzeugen
			JDataTable tbl1 = new JDataTable("RootTable");
			// 2. Columns anfügen
			tbl1.addColumn("name", Types.VARCHAR);
			JDataColumn colMenge = tbl1.addColumn("anzahl", Types.INTEGER);
			colMenge.setDefaultValue("4711");
			JDataColumn colBool = tbl1.addColumn("janein", Types.BOOLEAN);
			colBool.setDefaultValue("false");
			// 3. doppelte Column-Name
			try {
				tbl1.addColumn("janein", Types.BOOLEAN);
				fail("Doppelten Spaltennamen nicht erkannt!");
			} catch (Exception ex) {} // erwarteter Fehler
			// 4. Columns Zählen
			Iterator i = tbl1.getDataColumns();
			int cnt = 0;
			while (i.hasNext()) {
				i.next();
				cnt ++;
			}
			assertEquals("Anzahl Spalten muß drei sein!", cnt, 3);
			// 5. Zeile erzeugen
			JDataRow row = tbl1.createNewRow();
			assertEquals("Default-Wert wurde bei neuer Zeile nicht zugewiesen", row.getValue("anzahl"), "4711");
			assertEquals("Default-Wert wurde bei neuer Zeile nicht zugewiesen", row.getValue("janein"), "false");
			row.setValue("name", "Rudi");
			assertEquals("Row muß nach Änderung die Eigenschaft 'modified' haben", row.isModified(), true);
			row.rollbackChanges();
			assertEquals("Row muß nach Rollback die Eigenschaft 'modified=false' haben", row.isModified(), false);
			assertNull("Feld muß nach Rollback wieder leer sein", row.getValue("name"));
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}

}
