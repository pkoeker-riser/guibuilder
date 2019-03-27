package de.guibuilder.test.framework;

import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import de.guibuilder.framework.GDLParseException;
import de.guibuilder.framework.GuiChartPanel;
import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiWindow;
import de.guibuilder.framework.event.GuiUserEvent;
import de.jdataset.JDataColumn;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import junit.framework.TestCase;

public class GuiChartTest extends TestCase {

	public void test1() {
		try {
			GuiWindow win = GuiFactory.getInstance().createWindow("tutorial/chart.xml");
			win.setController(this);
			win.show();
		} catch (GDLParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadDataLine(GuiUserEvent event) {
		JDataSet ds = new JDataSet("TestChart");
		JDataTable tbl = new JDataTable("TestChart");
		ds.addRootTable(tbl);
		JDataColumn colX = tbl.addColumn("X", Types.DOUBLE);
		JDataColumn colY = tbl.addColumn("Y", Types.DOUBLE);
		for (int i = 0; i < 30; i++) {
			JDataRow row = tbl.createNewRow();
			ds.addChildRow(row);
			row.setValue("X", i);
			Random r = new Random();
			Double dd = r.nextDouble();
			double d = Math.pow(i,dd) -10;
			//double d = i^2;
			row.setValue("Y", d);
		}
		GuiChartPanel p = (GuiChartPanel)event.window.getGuiContainer("line.chartLine");
		//p.addLineChart(ds);
		p.setMultiLineChart(ds);
	}
	public void loadDataLineDate(GuiUserEvent event) {
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

		
		JDataSet ds = new JDataSet("TestChart");
		JDataTable tbl = new JDataTable("TestChart");
		ds.addRootTable(tbl);
		//JDataColumn colX = tbl.addColumn("Datum", Types.VARCHAR);
		JDataColumn colX = tbl.addColumn("Datum", Types.DATE);
		JDataColumn colY1 = tbl.addColumn("Y1", Types.DOUBLE);
		JDataColumn colY2 = tbl.addColumn("Y2", Types.DOUBLE);
		JDataColumn colY3 = tbl.addColumn("Y3", Types.DOUBLE);
		JDataColumn colY4 = tbl.addColumn("Y4", Types.DOUBLE);
		for (int i = 0; i < 30; i++) {
			JDataRow row = tbl.createNewRow();
			ds.addChildRow(row);
			String sd = (i+1) + ".08.2017";
			try {
				Date dt = format.parse(sd);
//				String s = format.format(dt);
				row.setValue("Datum", dt);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			{
				Random r = new Random();
				Double dd = r.nextDouble();
				double d = Math.pow(i,dd) -10;
				row.setValue("Y1", d);
			}
			{
				Random r = new Random();
				Double dd = r.nextDouble();
				double d = Math.pow(i,dd) -10;
				row.setValue("Y2", d);
			}
			{
				Random r = new Random();
				Double dd = r.nextDouble();
				double d = Math.pow(i,dd) -10;
				row.setValue("Y3", d);
			}
			{
				Random r = new Random();
				Double dd = r.nextDouble();
				double d = Math.pow(i,dd) -10;
				row.setValue("Y4", d);
			}
		}
		GuiChartPanel p = (GuiChartPanel)event.window.getGuiContainer("line.chartLine");
		p.setMultiLineChart(ds);
	}
	public void clearDataLine(GuiUserEvent event) {
		GuiChartPanel p = (GuiChartPanel)event.window.getGuiContainer("line.chartLine");
		p.reset();
	}
	public void loadDataPie(GuiUserEvent event) {
		JDataSet ds = new JDataSet("TestChart");
		JDataTable tbl = new JDataTable("TestChart");
		ds.addRootTable(tbl);
		JDataColumn colN = tbl.addColumn("name", Types.VARCHAR);
		JDataColumn colV = tbl.addColumn("value", Types.DOUBLE);
		Random r = new Random();
		Double dd = r.nextDouble();
		JDataRow row = tbl.createNewRow();
		ds.addChildRow(row);
		row.setValue("name", "automatisch");
		row.setValue("value", 50);
		
		row = tbl.createNewRow();
		ds.addChildRow(row);
		row.setValue("name", "manuell");
		row.setValue("value", 10);
		
		row = tbl.createNewRow();
		ds.addChildRow(row);
		row.setValue("name", "nicht gefunden");
		row.setValue("value", 30);

		GuiChartPanel p = (GuiChartPanel)event.window.getGuiContainer("pie.chartPie");
		p.setPieChart(ds);
	}
	public void clearDataPie(GuiUserEvent event) {
		GuiChartPanel p = (GuiChartPanel)event.window.getGuiContainer("pie.chartPie");
		p.reset();
	}
	
	public void loadDataBar(GuiUserEvent event) {
		JDataSet ds = new JDataSet("TestChart");
		JDataTable tbl = new JDataTable("Verarbeitungart");
		ds.addRootTable(tbl);
		JDataColumn colA = tbl.addColumn("city", Types.VARCHAR);
		JDataColumn col1 = tbl.addColumn("automatisch", Types.DOUBLE);
		JDataColumn col2 = tbl.addColumn("manuell", Types.DOUBLE);
		JDataColumn col3 = tbl.addColumn("nicht-gefunden", Types.DOUBLE);
		{
			JDataRow row = tbl.createNewRow();
			ds.addChildRow(row);
			row.setValue("city", "Berlin");
			row.setValue("automatisch", 65);
			row.setValue("manuell", 3);
			row.setValue("nicht-gefunden", 60);
		}
		{

			JDataRow row = tbl.createNewRow();
			ds.addChildRow(row);
			row.setValue("city", "Hamburg");
			row.setValue("automatisch", 55);
			row.setValue("manuell", 12);
			row.setValue("nicht-gefunden", 20);

		}
		{
			JDataRow row = tbl.createNewRow();
			ds.addChildRow(row);
			row.setValue("city", "München");
			row.setValue("automatisch", 60);
			row.setValue("manuell", 8);
			row.setValue("nicht-gefunden", 18);
		}
		{
			JDataRow row = tbl.createNewRow();
			ds.addChildRow(row);
			row.setValue("city", "Leipzig");
			row.setValue("automatisch", 57);
			row.setValue("manuell", 13);
			row.setValue("nicht-gefunden", 24);

		}
		GuiChartPanel p = (GuiChartPanel)event.window.getGuiContainer("bar.chartBar");
		p.setMultiBarChart(ds);
	}
	
	public void loadMultiDataBar(GuiUserEvent event) {
		this.loadBarData(event, "bar.chartBar");	
	}

	public void loadMultiDataStackedBar(GuiUserEvent event) {
		this.loadBarData(event, "stackBar.chartStackBar");
	}
	
	private void loadBarData(GuiUserEvent event, String chart) {
		JDataSet ds = new JDataSet("Verarbeitungsart");
		JDataTable tbl = new JDataTable("Verarbeitungsart");
		ds.addRootTable(tbl);
		JDataColumn colN = tbl.addColumn("name", Types.VARCHAR);
		JDataColumn colV1 = tbl.addColumn("auto", Types.DOUBLE);
		JDataColumn colV2 = tbl.addColumn("manu", Types.DOUBLE);
		JDataColumn colV3 = tbl.addColumn("nix", Types.DOUBLE);

		JDataRow row = tbl.createNewRow();
		ds.addChildRow(row);
		row.setValue("name", "Berlin");
		row.setValue("auto", 65);
		row.setValue("manu", 0);
		row.setValue("nix", 20);
		
		row = tbl.createNewRow();
		ds.addChildRow(row);
		row.setValue("name", "Hamburg");
		row.setValue("auto", 55);
		row.setValue("manu", 12);
		row.setValue("nix", 10);
		
		row = tbl.createNewRow();
		ds.addChildRow(row);
		row.setValue("name", "München");
		row.setValue("auto", 60);
		row.setValue("manu", 8);
		row.setValue("nix", 13);

		row = tbl.createNewRow();
		ds.addChildRow(row);
		row.setValue("name", "Leipzig");
		row.setValue("auto", 57);
		row.setValue("manu", 10);
		row.setValue("nix", 20);

		GuiChartPanel p = (GuiChartPanel)event.window.getGuiContainer(chart);
		if (chart.startsWith("bar")) {
			p.setMultiBarChart(ds);
		} else {			
			p.setMultiStackedBarChart(ds);
		}
	}
	
	public void clearDataBar(GuiUserEvent event) {
		GuiChartPanel p = (GuiChartPanel)event.window.getGuiContainer("bar.chartBar");
		p.reset();
	}
	public void loadDataStackBar(GuiUserEvent event, String chart) {
		JDataSet ds = new JDataSet("Verarbeitungsart");
		JDataTable tbl = new JDataTable("Verarbeitungsart");
		ds.addRootTable(tbl);
		JDataColumn colN = tbl.addColumn("name", Types.VARCHAR);
		JDataColumn colV1 = tbl.addColumn("auto", Types.DOUBLE);
		JDataColumn colV2 = tbl.addColumn("manu", Types.DOUBLE);
		JDataColumn colV3 = tbl.addColumn("nix", Types.DOUBLE);

		JDataRow row = tbl.createNewRow();
		ds.addChildRow(row);
		row.setValue("name", "Berlin");
		row.setValue("auto", 65);
		row.setValue("manu", 0);
		row.setValue("nix", 20);
		
		row = tbl.createNewRow();
		ds.addChildRow(row);
		row.setValue("name", "Hamburg");
		row.setValue("auto", 55);
		row.setValue("manu", 12);
		row.setValue("nix", 10);
		
		row = tbl.createNewRow();
		ds.addChildRow(row);
		row.setValue("name", "München");
		row.setValue("auto", 60);
		row.setValue("manu", 8);
		row.setValue("nix", 13);

		row = tbl.createNewRow();
		ds.addChildRow(row);
		row.setValue("name", "Leipzig");
		row.setValue("auto", 57);
		row.setValue("manu", 10);
		row.setValue("nix", 20);

		GuiChartPanel p = (GuiChartPanel)event.window.getGuiContainer(chart);
		p.setMultiStackedBarChart(ds);
	}
	
	public void clearDataStackBar(GuiUserEvent event) {
		GuiChartPanel p = (GuiChartPanel)event.window.getGuiContainer("stackBar.chartStackBar");
		p.reset();
	}
	
	public static void main (String[] args) {
		GuiChartTest me = new GuiChartTest();
		me.test1();
	}
}
