package de.guibuilder.framework;

import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;

public class GuiChartPanel extends GuiJFXPanel {
	private String[] labels;
	private String type;
	
	public GuiChartPanel(String[] labels) {
		//super();
		this.setName("chart");
		this.labels = labels;
		this.guiInit();
	}
	
	
	public String[] getLabels() {
		return labels;
	}
	
	void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
		
	/**
	 * Viele Lines aus einem Dataset:
	 * Categorie[Datum; X-Achse], y1, y2, ... yn
	 * Beschriftung der Lines aus Spalten-Namen y1, y2...
	 * @param ds
	 */
	public void setMultiLineChart(final JDataSet ds) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				JDataTable tbl = ds.getDataTable();
				int cnt = tbl.getDataColumnCount(); 
				List<String> cols = tbl.getDataColumnNames();
				final CategoryAxis xAxis = new CategoryAxis();
				final NumberAxis yAxis = new NumberAxis();
				if (labels.length> 1) {
					xAxis.setLabel(labels[1]);
				}
				if (labels.length> 2) {
					yAxis.setLabel(labels[2]);
				}
				LineChart<String,Number> chart = new LineChart<String,Number>(xAxis, yAxis);
				chart.setTitle(labels[0]);

				Scene scene = new Scene(chart);
				for (int i = 1; i < cnt; i++) {
					String x = cols.get(0);
					String y = cols.get(i);
					XYChart.Series<String,Number> series = new XYChart.Series<String,Number>();
					series.setName(y);
					Iterator<JDataRow> it = ds.getChildRows();
					if (it == null) {
						return;
					}
					while (it.hasNext()) {
						JDataRow row = it.next();
						String s1 = row.getValue(x);
						double s2 = row.getValueDouble(y);
						series.getData().add(new XYChart.Data<String,Number>(s1, s2));
					}
					chart.getData().add(series);
				};
				jfxPanel.setScene(scene);
			}
		});
	}

	public void setPieChart(final JDataSet ds) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Scene scene = new Scene(new Group());
				JDataTable tbl = ds.getDataTable();
				ObservableList<PieChart.Data> list = FXCollections.observableArrayList();
				List<String> cols = tbl.getDataColumnNames();
				String name = cols.get(0);
				String val = cols.get(1);
				Iterator<JDataRow> it = ds.getChildRows();
				if (it == null) return;
				int cnt = 0;
				double sum = 0.0;
				while (it.hasNext()) {
					JDataRow row = it.next();
					if (cnt < 6) {
					   list.add(new PieChart.Data(row.getValue(name), row.getValueDouble(val)));
					} else {
					   sum += row.getValueDouble(val);
					}
					cnt++;
				}
				if (sum > 0.0) {
                   list.add(new PieChart.Data("(Rest)", sum));
				}
				final PieChart chart = new PieChart(list);
                chart.setTitle(labels[0]);

				//chart.setLabelsVisible(true);
				//chart.setLabelLineLength(10);
				//chart.setLegendSide(Side.RIGHT);
				//chart.setLegendVisible(false);
				chart.setStartAngle(90);
							
				jfxPanel.setScene(scene);
				((Group) scene.getRoot()).getChildren().add(chart);
			};
		});
	}
	/**
	 * @deprecated
	 * @param dss
	 */
	public void setBarChart(final JDataSet[] dss) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				final CategoryAxis xAxis = new CategoryAxis();
				if (labels.length> 1) {
					xAxis.setLabel(labels[1]);
				}
				final NumberAxis yAxis = new NumberAxis();
				if (labels.length> 2) {
					yAxis.setLabel(labels[2]);
				}
				final BarChart<String,Number> bc = new BarChart<String,Number>(xAxis,yAxis);
				bc.setTitle(labels[0]);
				XYChart.Series<String, Number>[] series = new XYChart.Series[dss.length];
				for (int i = 0; i < dss.length; i++) {
					JDataSet ds = dss[i];
					JDataTable tbl = ds.getDataTable();
					XYChart.Series<String, Number> serie = new XYChart.Series<String, Number>();
					series[i] = serie;
					serie.setName(tbl.getTablename());
					Iterator<JDataRow> it = ds.getChildRows();
					if (it == null) {
						break;
					}
					List<String> cols = tbl.getDataColumnNames();
					String name = cols.get(0);
					String val = cols.get(1);
					while (it.hasNext()) {
						JDataRow row = it.next();
						String n = row.getValue(name);
						double v = row.getValueDouble(val);
						serie.getData().add(new XYChart.Data<String,Number>(n, v));
					}
				
				}
				Scene scene = new Scene(bc);
				bc.getData().addAll(series);
				jfxPanel.setScene(scene);
			};
		});
	}

	public void setMultiBarChart(final JDataSet ds) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				JDataTable tbl = ds.getDataTable();
				int cnt = tbl.getDataColumnCount(); 
				List<String> cols = tbl.getDataColumnNames();

				final CategoryAxis xAxis = new CategoryAxis();
				if (labels.length> 1) {
					xAxis.setLabel(labels[1]);
				}
				final NumberAxis yAxis = new NumberAxis();
				if (labels.length> 2) {
					yAxis.setLabel(labels[2]);
				}
				final BarChart<String,Number> sbc = new BarChart<String,Number>(xAxis,yAxis);
				sbc.setTitle(labels[0]);
				sbc.setCategoryGap(50);
				
				XYChart.Series<String, Number>[] series = new XYChart.Series[cnt-1];
				String name = cols.get(0);
				for (int i = 1; i < cnt; i++) {
					String val = cols.get(i);
					XYChart.Series<String, Number> serie = new XYChart.Series<String, Number>();
					serie.setName(val);
					series[i-1] = serie;
					Iterator<JDataRow> it = ds.getChildRows();
					if (it == null) {
						break;
					}
					while (it.hasNext()) {
						JDataRow row = it.next();
						String n = row.getValue(name);
						double v = row.getValueDouble(val);
						serie.getData().add(new XYChart.Data<String,Number>(n, v));
					}
					
				}
				Scene scene = new Scene(sbc);
				sbc.getData().addAll(series);
				jfxPanel.setScene(scene);
			};
		});
	}

	/**
	 * @deprecated siehe setMultiStackedBarChart
	 * @param dss
	 */
	public void setStackedBarChart(final JDataSet[] dss) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				final CategoryAxis xAxis = new CategoryAxis();
				if (labels.length> 1) {
					xAxis.setLabel(labels[1]);
				}
				final NumberAxis yAxis = new NumberAxis();
				if (labels.length> 2) {
					yAxis.setLabel(labels[2]);
				}
				final StackedBarChart<String,Number> sbc = new StackedBarChart<String,Number>(xAxis,yAxis);
				sbc.setTitle(labels[0]);
				sbc.setCategoryGap(50);
				XYChart.Series<String, Number>[] series = new XYChart.Series[dss.length];
				for (int i = 0; i < dss.length; i++) {
					JDataSet ds = dss[i];
					JDataTable tbl = ds.getDataTable();
					XYChart.Series<String, Number> serie = new XYChart.Series<String, Number>();
					series[i] = serie;
					serie.setName(tbl.getTablename());
					Iterator<JDataRow> it = ds.getChildRows();
					if (it == null) {
						break;
					}
					List<String> cols = tbl.getDataColumnNames();
					String name = cols.get(0);
					String val = cols.get(1);
					while (it.hasNext()) {
						JDataRow row = it.next();
						String n = row.getValue(name);
						double v = row.getValueDouble(val);
						serie.getData().add(new XYChart.Data<String,Number>(n, v));
					}
				}
				Scene scene = new Scene(sbc);
				sbc.getData().addAll(series);
				jfxPanel.setScene(scene);
			};
		});
	}
	
	public void setMultiStackedBarChart(final JDataSet ds) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				JDataTable tbl = ds.getDataTable();
				int cnt = tbl.getDataColumnCount(); 
				List<String> cols = tbl.getDataColumnNames();

				final CategoryAxis xAxis = new CategoryAxis();
				if (labels.length> 1) {
					xAxis.setLabel(labels[1]);
				}
				final NumberAxis yAxis = new NumberAxis();
				if (labels.length> 2) {
					yAxis.setLabel(labels[2]);
				}
				final StackedBarChart<String,Number> sbc = new StackedBarChart<String,Number>(xAxis,yAxis);
				sbc.setTitle(labels[0]);
				sbc.setCategoryGap(50);
				
				XYChart.Series<String, Number>[] series = new XYChart.Series[cnt-1];
				String name = cols.get(0);
				for (int i = 1; i < cnt; i++) {
					String val = cols.get(i);
					XYChart.Series<String, Number> serie = new XYChart.Series<String, Number>();
					serie.setName(val);
					series[i-1] = serie;
					Iterator<JDataRow> it = ds.getChildRows();
					if (it == null) {
						break;
					}
					while (it.hasNext()) {
						JDataRow row = it.next();
						String n = row.getValue(name);
						double v = row.getValueDouble(val);
						serie.getData().add(new XYChart.Data<String,Number>(n, v));
					}
					
				}
				Scene scene = new Scene(sbc);
				sbc.getData().addAll(series);
				jfxPanel.setScene(scene);
			};
		});
	}

	
	public void reset() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Scene scene = jfxPanel.getScene();
				Parent chart = scene.getRoot();
				if (chart instanceof XYChart) {
					((XYChart)chart).getData().clear();
				} else {
					jfxPanel.setScene(new Scene(new Group()));
				}
			};
		});
		
	}
}
