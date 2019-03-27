package de.guibuilder.framework;

import java.sql.Types;
import java.util.Collection;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import de.jdataset.DataView;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.jdataset.NVPair;

/**
 * @see GuiInternalFrame
 */
final class GuiInternalFrameImpl extends JInternalFrame {
	private boolean autosize;
	
	public GuiInternalFrameImpl (String title) {
		super(title, true, true, true, true);
		this.setName(GuiUtil.labelToName(title));
		this.guiInit();
	}
	// Methods
	/**
   * Fügt einen InternalFrameListener hinzu.
	 */
	private void guiInit() {
		// Der tut ja nüscht!?
		this.addInternalFrameListener(new InternalFrameListener() {
			public void internalFrameActivated(InternalFrameEvent e) {
				// activated an RootPane weiterleiten?
				// OnActive
			}
			public void internalFrameClosed(InternalFrameEvent e) {
				// OnClose
			}
			public void internalFrameClosing(InternalFrameEvent e) {
			}
			public void internalFrameDeactivated(InternalFrameEvent e) {
			}
			public void internalFrameDeiconified(InternalFrameEvent e) {
			}
			public void internalFrameIconified(InternalFrameEvent e) {
			}
			public void internalFrameOpened(InternalFrameEvent e) {
				// OnOpen
			}
		});
	}
	/**
   * Schiebt dem Fenster ein anderes RootPane unter.
	 */
	final void setGuiRootPane(GuiRootPane root) {
		this.setRootPane(root);
	}
	public void show() {
		if (this.isAutoSize()) {
			this.pack();
		}
		super.show();
	}
	/**
	 * @return Returns the autosize.
	 */
	boolean isAutoSize() {
		return this.autosize;
	}
	/**
	 * @param autosize The autosize to set.
	 */
	void setAutoSize(boolean autosize) {
		this.autosize = autosize;
	}
  public void getPreferences(JDataSet ds) {
    // Meta Table
    JDataTable tbl = null;
    try {
      tbl = ds.getDataTable("GuiInternalFrame");
    } catch (Exception ex) {
      tbl = new JDataTable("GuiInternalFrame");
      ds.addRootTable(tbl);
      tbl.addColumn("name", Types.VARCHAR);
      tbl.addColumn("height", Types.INTEGER);
      tbl.addColumn("width", Types.INTEGER);
      tbl.addColumn("x", Types.INTEGER);
      tbl.addColumn("y", Types.INTEGER);
      tbl.addColumn("maximized", Types.BOOLEAN);
    }
    // Prefs Table
    JDataRow row = null;
    Collection<JDataRow> trows = null;
    try {
      trows = ds.getChildRows(new DataView("GuiInternalFrame", new NVPair("name", this.getName(), Types.VARCHAR)));
    } catch (Exception ex) {}
    if (trows != null && trows.size() == 1) {
      row = trows.iterator().next();
    } else {
      row = ds.createChildRow("GuiInternalFrame");
    }
    row.setValue("name", this.getName());
    row.setValue("height", this.getHeight());
    row.setValue("width", this.getWidth());
    row.setValue("x", this.getX());
    row.setValue("y", this.getY());
    // Components
    ((GuiRootPane)this.getRootPane()).getMainPanel().getPreferences(ds);    
  }
  
  public void setPreferences(JDataSet ds) {
    JDataRow row = null;
    try {
      Collection<JDataRow> trows = ds.getChildRows(new DataView("GuiInternalFrame", new NVPair("name", this.getName(), Types.VARCHAR)));
      if (trows != null && trows.size() == 1) {
        row = trows.iterator().next();
        this.setSize(row.getValueInt("width"), row.getValueInt("height"));
        this.setLocation(row.getValueInt("x"), row.getValueInt("y"));
        // Components
        ((GuiRootPane)this.getRootPane()).getMainPanel().setPreferences(ds);
      }
    } catch (Exception ex) {}
  }

}