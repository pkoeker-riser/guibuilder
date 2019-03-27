package de.guibuilder.adapter;

import de.guibuilder.framework.ApplicationIF;
import de.guibuilder.framework.GuiSession;

import de.pkjs.pl.IPLContext;
import de.pkjs.pl.PL;
import de.jdataset.*;

/**
 * @author Peter Köker
 * http://www.guibuilder.de
 * 11.11.2002 16:56:43
 *
 */
public class PersistenceAdapter implements ApplicationIF {
	// Attributes
	private static volatile PersistenceAdapter me;
	private IPLContext db;
	// private Constructor
	private PersistenceAdapter() {
		try {
			db = new PL();
			GuiSession.getInstance().setAdapter(this);
			System.out.println("Application Adapter instantiated: de.guibuilder.adapter.PersistenceAdapter");
		} catch (Exception ex) {
			System.err.println("Error installing PersistenceAdapter: " + ex.getMessage());
		}
	}
	// Methods
	public static PersistenceAdapter getInstance() {
		if (me == null) {
			synchronized(PersistenceAdapter.class) {
		      if(me == null) {
		          me = new PersistenceAdapter();
		      }
			}
		}
		return me;
	}
	public void setDatabase(IPLContext d) {
		this.db = d;
	}
	public IPLContext getDatabase() {
		return db;
	}
	public JDataSet getDataset(String datasetname) throws Exception {
		JDataSet ds = db.getAll(datasetname);
		return ds;
	}
	/**
	 * Wird von GuiUtil ausgelösten, wenn in GuiBuilderConfig.xml
	 * diese Klasse als ApplicationAdapter eingetragen ist.
	 * Class.forName
	 */
	static {
		getInstance(); // 
	}
}
