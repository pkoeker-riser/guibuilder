/*
 * Created on 28.10.2004
 */
package de.guibuilder.test.jdataset;

import java.util.Iterator;

import de.jdataset.JDataColumn;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import junit.framework.TestCase;

/**
 * @author PKOEKER
 */
public class JDataColumnTest extends TestCase {

	public void testCSV2Dataset() {
		String file = 
			"Vorname;Nachname;Geburtsdatum;Geschlecht;Kinder;Einkommen;OK\n"
			+"varchar;varchar;date;varchar;integer;decimal;boolean\n"
			+"Hans;Müller;12.3.1956;m;3;1234,56;true\n"
			+"Maria;Lehmann;1.9.1965;w;;1234,57;false\n"
			+"Karl;Meier;1.7.1969;m;1;1234,58;false\n";
		try {
			JDataSet ds = JDataSet.CSV2Dataset("Test", file);
			Iterator<JDataRow> it = ds.getChildRows();
			int cnt = 0;
			JDataTable tbl = ds.getDataTable();
			JDataColumn colOK = tbl.getDataColumn("OK");
			colOK.setTransient(true);
			assertEquals(colOK.isTransient(), true);
			colOK.setReadonly(true);
			assertEquals(colOK.isReadonly(), true);
			colOK.setAutoid(false);
			assertEquals(colOK.isAutoid(), false);

			colOK.toString();
			assertEquals(JDataColumn.isBooleanType(colOK.getDataType()), true);
			
			assertEquals(JDataColumn.isSupportedColumnType(java.sql.Types.BIGINT), true);
			assertEquals(JDataColumn.isSupportedColumnType(java.sql.Types.VARBINARY), true);
			colOK.setColumnName("ok");
			colOK.getComment();
			colOK.setDataType("BOOLEAN");
			while (it.hasNext()) {
				it.next();
				cnt++;
			}
			assertEquals("Anzahl Zeilen falsch", 3, ds.getRowCount());
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
}
