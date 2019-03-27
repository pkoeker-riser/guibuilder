/*
 * Created on 30.06.2005
 */
package de.guibuilder.test.jdataset;

import java.io.File;
import java.sql.Types;

import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import electric.xml.Document;
import junit.framework.TestCase;

/**
 */
public class TestDataset extends TestCase {
    public void test1() {
        try {
            JDataSet ds = new JDataSet("Example");
            JDataTable tbl = new JDataTable("Root");
            ds.addRootTable(tbl);
            tbl.addColumn("id", Types.BIGINT);

            tbl.addColumn("menu_yesno", Types.BOOLEAN);
            tbl.addColumn("menu_color", Types.VARCHAR);

            tbl.addColumn("Text", Types.VARCHAR);
            tbl.addColumn("Number", Types.BIGINT);
            tbl.addColumn("Decimal", Types.DECIMAL);
            tbl.addColumn("Money", Types.DECIMAL);
            tbl.addColumn("yesno", Types.BOOLEAN);
            tbl.addColumn("Color", Types.VARCHAR);
            tbl.addColumn("Choice1", Types.INTEGER);
            tbl.addColumn("Choice2", Types.VARCHAR);

            JDataTable tblC1 = new JDataTable("Child1");
            tblC1.addColumn("id", Types.BIGINT);
            tbl.addChildTable(tblC1, "id");
            tblC1.addColumn("Text", Types.VARCHAR);
            tblC1.addColumn("Number", Types.BIGINT);
            tblC1.addColumn("Decimal", Types.DECIMAL);
            tblC1.addColumn("Money", Types.DECIMAL);
            tblC1.addColumn("yesno", Types.BOOLEAN);

            JDataTable tblC2 = new JDataTable("Child2");
            tblC2.addColumn("id", Types.BIGINT);
            tbl.addChildTable(tblC2, "id");
            tblC2.addColumn("Text", Types.VARCHAR);
            tblC2.addColumn("Number", Types.BIGINT);
            tblC2.addColumn("Decimal", Types.DECIMAL);
            tblC2.addColumn("Money", Types.DECIMAL);
            tblC2.addColumn("yesno", Types.BOOLEAN);

            JDataTable tblP1 = new JDataTable("Parent1");
            tblP1.addColumn("id", Types.BIGINT);
            tbl.addParentTable(tblP1, "id");
            tblP1.addColumn("text", Types.VARCHAR);

            JDataTable tblP2 = new JDataTable("Parent2");
            tblP2.addColumn("id", Types.BIGINT);
            tbl.addParentTable(tblP2, "id");
            tblP2.addColumn("text", Types.VARCHAR);
            // Rows
            JDataRow rowRoot = tbl.createNewRow();
            ds.addChildRow(rowRoot);
            
            JDataRow rowP1 = tblP1.createNewRow();
            JDataRow rowP2 = tblP2.createNewRow();
            rowRoot.addParentRow(rowP1);
            rowRoot.addParentRow(rowP2);
                       
            ds.commitChanges();
            File f = new File("tutorial/example_dataset.xml");
            Document doc = ds.getXml();
            doc.write(f);
            //GuiUtil.fileOpenDialog();
            
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}