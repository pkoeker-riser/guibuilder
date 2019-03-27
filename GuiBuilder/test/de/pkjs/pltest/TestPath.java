/*
 * Created on 17.05.2003
 */
package de.pkjs.pltest;
import java.sql.Types;

import junit.framework.TestCase;

import de.jdataset.JDataColumn;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.jdataset.JDataValue;
/**
 * @author peter
 */
public class TestPath extends TestCase {

	public void test() {
		System.out.println("--- BEGIN TestPath ---");
		try {
			JDataSet ds = new JDataSet("Dataset");
			JDataTable tblRoot = new JDataTable("WurzelTabelle");
			{
				JDataColumn colId = tblRoot.addColumn("id", Types.INTEGER);
				colId.setPrimaryKey(true);
			}
			ds.addRootTable(tblRoot);
			// Child tables
			JDataTable tblChild1 = new JDataTable("Child1");
			{
				JDataColumn colChildId = tblChild1.addColumn("id1", Types.INTEGER);
				colChildId.setPrimaryKey(true);
				tblChild1.addColumn("fk1", Types.INTEGER);
			}
			tblRoot.addChildTable(tblChild1, "fk1");
			
			JDataTable tblChild2 = new JDataTable("Child2");
			{
				JDataColumn colChildId = tblChild2.addColumn("id2", Types.INTEGER);
				colChildId.setPrimaryKey(true);
				tblChild2.addColumn("fk2", Types.INTEGER);
				tblChild2.addColumn("fkp1", Types.INTEGER);
			}
			tblChild1.addChildTable(tblChild2, "fk2");
			
			JDataTable tblParent1 = new JDataTable("Parent1");
			{
				tblParent1.addColumn("pid1", Types.INTEGER);
				tblParent1.addColumn("name1", Types.INTEGER);
				tblParent1.addColumn("fkp2", Types.INTEGER);
			}
			JDataTable tblParent2 = new JDataTable("Parent2");
			{
				tblParent2.addColumn("pid2", Types.INTEGER);
				tblParent2.addColumn("name2", Types.INTEGER);
			}
			tblChild2.addParentTable(tblParent1, "fkp1");
			tblParent1.addParentTable(tblParent2, "fkp2");
			// Schema ausgeben
			//System.out.println(ds.getXml());
			// Zugriffe auf Column
			String path = "WurzelTabelle[1].Child1[2].Child2[3]#Parent1#Parent2@name2";
			JDataTable tbl = ds.getDataTablePath(path);
			assertEquals("Tabelle nicht gefunden","Parent2", tbl.getTablename());
			JDataColumn col = ds.getDataColumnPath(path);
			assertEquals("Column nicht gefunden","name2", col.getColumnName());
			path = "WurzelTabelle.Child1.Child2#Parent1#Parent2@name2";
			
			// Daten
			JDataRow rootRow = ds.createChildRow();
			JDataRow child1Row = rootRow.createChildRow("Child1");
			JDataRow child2Row = child1Row.createChildRow("Child2");
			JDataRow child2Row2 = child1Row.createChildRow("Child2");
			child2Row2.setValue("id2", 4711);
			JDataRow parent1Row = tblParent1.createNewRow();
			child2Row.addParentRow(parent1Row);
			JDataRow parent2Row = tblParent2.createNewRow();
			parent1Row.addParentRow(parent2Row);
			parent2Row.setValue("name2", "Rudi Müller");
			//System.out.println(ds.getXml());
			JDataRow row = ds.getDataRowPath(path);
			assertEquals("Fehler mit Path", "Parent2", row.getDataTable().getTablename());
			JDataValue val = ds.getDataValuePath(path);
			assertEquals("Fehler mit Path", "Rudi Müller", val.getValue());
			//System.out.println(val.getValue());
			// []
			path = "WurzelTabelle[0].Child1[0].Child2[1]@id2";
			JDataValue val2 = ds.getDataValuePath(path);
			System.out.println(val2.getValue());
			ds.setValuePath(path, 4712);
			//System.out.println(ds.getValuePath(path));
			assertEquals("Falscher Wert", "4712", ds.getValuePath(path));
			// @
			ds.setValuePath("@id", 42);
			//System.out.println(ds.getValueIntPath("@id"));
			assertEquals("Falscher Wert", 42, ds.getValueIntPath("@id"));

			path = ".Child1[0].Child2[1]@id2";
			JDataValue val3 = ds.getDataValuePath(path);
			//System.out.println(val3.getValue());
			assertEquals("Falscher Wert","4712", val3.getValue());
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
		
		System.out.println("--- ENDE TestPath ---");
	}
}
