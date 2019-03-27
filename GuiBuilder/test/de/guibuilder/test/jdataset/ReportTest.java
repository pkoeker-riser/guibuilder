/*
 * Created on 23.10.2004
 */
package de.guibuilder.test.jdataset;

import java.sql.Types;

import net.sf.jasperreports.engine.*;

import de.jdataset.*;
import de.jdataset.jasper.JRDataSetDataSource;
import de.jdataset.jasper.JRDataSetParameters;

/**
 * @author Peter Köker
 */
public class ReportTest {
	public static void main(String[] args) {
		// 1. Create DataSet on the fly
		JDataSet ds = new JDataSet("ExampleReport");
		JDataTable tbl = new JDataTable("Address");
		ds.addRootTable(tbl);
		tbl.addColumn("Nachname", Types.VARCHAR);
		tbl.addColumn("Vorname", Types.VARCHAR);
		tbl.addColumn("Geburtsdatum", Types.DATE);
		tbl.addColumn("Einkommen", Types.DECIMAL);
		// 1.1 Add Data to Dataset
		JDataRow row1 = tbl.createNewRow();
		ds.addChildRow(row1);
		row1.setValue("Nachname", "Müller");
		row1.setValue("Vorname", "Heinz");
		row1.setValue("Geburtsdatum", "11.12.1977");
		row1.setValue("Einkommen", 3000);
		
		JDataRow row2 = tbl.createNewRow();
		ds.addChildRow(row2);
		row2.setValue("Nachname", "Schulze");
		row2.setValue("Vorname", "Maria");
		row2.setValue("Geburtsdatum", "1.5.1987");
		row2.setValue("Einkommen", 3500);
		
		JDataRow row3 = tbl.createNewRow();
		ds.addChildRow(row3);
		row3.setValue("Nachname", "Lehmann");
		row3.setValue("Vorname", "Karl");
		row3.setValue("Geburtsdatum", "7.9.1967");
		row3.setValue("Einkommen", 2345);
		// 1.2 Create Jasper DataSource from DataSet
		JRDataSetDataSource sr = new JRDataSetDataSource(ds);
		// 1.3 Create Jasper Parameter
		JDataSet dsp = new JDataSet("Paramter");
		JDataTable tblp = new JDataTable("tblParamter");
		dsp.addRootTable(tblp);
		tblp.addColumn("ReportTitle", Types.VARCHAR);
		JDataRow rowp = tblp.createNewRow();
		rowp.setValue("ReportTitle", "ReportTitle als Parameter");
		dsp.addChildRow(rowp);
		JRDataSetParameters pm = new JRDataSetParameters(dsp);
		// 2. Create the Report
		try {
			// 2.1 Compile the Report
			JasperCompileManager.compileReportToFile("reports/testreport1.jrxml");
			// 2.2 Fill compiled Report with Data
			JasperFillManager.fillReportToFile("reports/test_report1.jasper", pm, sr);
			// 2.3 Export Report to PDF
			JasperExportManager.exportReportToPdfFile("reports/test_report1.jrprint");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
