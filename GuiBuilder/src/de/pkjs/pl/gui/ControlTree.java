/*
 * Created on 26.11.2003
 */
package de.pkjs.pl.gui;

import de.guibuilder.framework.GuiPanel;
import de.guibuilder.framework.GuiTree;
import de.guibuilder.framework.GuiTreeElement;
import de.guibuilder.framework.GuiTreeNode;
import de.guibuilder.framework.event.GuiChangeEvent;
import de.guibuilder.framework.event.GuiUserEvent;
import de.jdataset.JDataSet;
import de.pkjs.pl.PL;
import de.pkjs.pl.Request;
import de.pkjs.pl.TableRequest;
import electric.xml.Document;

/**
 * @author peter
 */
public class ControlTree {
	// Attributes
	private static ControlTree me;

	//private PL pl;

	//private String configFilename;

	//private GuiWindow win;

	private GuiTree tree;

	// private Constructor
	private ControlTree() {

	}

	// Singleton
	public static ControlTree getInstance() {
		if (me == null) {
			synchronized (ControlTree.class) {
		      if(me == null) {
		          me = new ControlTree();
		      }
			}
		}
		return me;
	}

	// Methods
	public void setPL(PL pl) {
		//this.pl = pl;
		try {
		} catch (Exception ex) {
			//ex.printStackTrace();
		}
	}

	//	public void show() {
	//		try {
	//			win = GuiFactory.getInstance().createWindow("PL_GUI.xml");
	//			win.setController(this);
	//			tree = win.getRootPane().getMainPanel().getGuiTree("tree");
	//			// 0. Root / Options
	//			{
	//				GuiTreeNode rootNode = tree.getRoot();
	//				rootNode.setFileName("PL_OptionPanel.xml");
	//				GuiSplit split = tree.getSplit();
	//				GuiPanel rootPanel = rootNode.getPanel();
	//				split.setRightComponent(rootPanel);
	//				// DataSet
	//				JDataSet ds = new JDataSet("PL");
	//				ds.addRootTable(PL.getMetaDataTable());
	//				ds.addChildRow(pl.getMetaDataRow());
	//				rootNode.setUserObject2(ds);
	//				// Xml-Node versorgen
	//				rootPanel.setDatasetValues(ds);
	//				Document rootDoc = rootPanel.getAllValuesXml();
	//				rootNode.setAllValuesXml(rootDoc);
	//			}
	//			// 1. Databases #########################################
	//			GuiTreeNode rootDbNode = new GuiTreeNode("Databases", "databases");
	//			tree.addGuiNode(rootDbNode);
	//			Iterator it = pl.getDatabases();
	//			while (it.hasNext()) {
	//				Database db = (Database)it.next();
	//				String dbName = db.getDatabaseName();
	//				// 1.1 Database
	//				GuiTreeNode dbNode = new GuiTreeNode("Database: "+dbName);
	//				GuiTreeElement trele = tree.getGuiTreeElement("Database");
	//				// TODO : Eigenschaften von TreeElement nach TreeNode schaufeln
	//				dbNode.setFileName("PL_DatabasePanel.xml");
	//				dbNode.setMsgNodeClick("databaseNodeClick");
	//				dbNode.setName(dbName);
	//				rootDbNode.add(dbNode);
	//				// DataSet für TreeNode
	//				{
	//					JDataSet ds = new JDataSet("Database");
	//					ds.addRootTable(Database.getMetaDataTable());
	//					ds.addChildRow(db.getMetaDataRow());
	//					dbNode.setUserObject2(ds);
	//					// Xml-Node versorgen
	//					GuiPanel dbPanel = dbNode.getPanel(); // Erzwingt auch createPanel
	//					dbPanel.setDatasetValues(ds);
	//					Document dbDoc = dbPanel.getAllValuesXml();
	//					dbNode.setAllValuesXml(dbDoc);
	//				}
	//				// Popup Menu
	//				{
	//					GuiTreeElement tele = tree.getGuiTreeElement("Database");
	//					if (tele != null) {
	//						dbNode.setPopupMenu(tele.getPopupMenu());
	//					}
	//				}
	//				// 2. DataTables ###########################################
	//				{
	//					Iterator ii = db.getDataTables();
	//					if (ii != null) {
	//						while ( ii.hasNext() ) {
	//							JDataTable dataTable = (JDataTable)ii.next();
	//							GuiTreeNode tblNode = new GuiTreeNode("DataTable:
	// "+dataTable.getTablename());
	//							tblNode.setFileName("PL_DataTablePanel.xml");
	//							dbNode.add(tblNode);
	//							tblNode.setMsgNodeClick("tableNodeClick");
	//							tblNode.setName(dataTable.getTablename());
	//							// Popup Menu
	//							{
	//								GuiTreeElement tele = tree.getGuiTreeElement("DataTable");
	//								if (tele != null) {
	//									tblNode.setPopupMenu(tele.getPopupMenu());
	//								}
	//							}
	//						}
	//					}
	//				}
	//			}
	//			// 3. DataViews ##############################################
	//			ArrayList requests = pl.getDatasetNames();
	//			GuiTreeNode rootViewNode = new GuiTreeNode("Requests", "requests");
	//			tree.addGuiNode(rootViewNode);
	//			tree.getTree().expandPath(new
	// TreePath(tree.getGuiTreeModel().getPathToRoot(tree.getRoot())));
	//			// Popup Menu
	//			{
	//				GuiTreeElement tele = tree.getGuiTreeElement("Requests");
	//				if (tele != null) {
	//					rootViewNode.setPopupMenu(tele.getPopupMenu());
	//				}
	//			}
	//			for (Iterator i = requests.iterator(); i.hasNext();) {
	//				String dataviewName = (String)i.next();
	//				Request req = pl.getRequest(dataviewName);
	//				GuiTreeNode viewNode = new GuiTreeNode("Request: " + dataviewName);
	//				viewNode.setName(dataviewName);
	//				viewNode.setFileName("PL_RequestPanel.xml");
	//				viewNode.setMsgNodeClick("requestNodeClick");
	//				rootViewNode.add(viewNode);
	//				// DataSet für TreeNode
	//				{
	//					JDataSet ds = new JDataSet("Request");
	//					ds.addRootTable(Request.getMetaDataTable());
	//					ds.addChildRow(req.getMetaDataRow());
	//					viewNode.setUserObject2(ds);
	//					GuiPanel panel = viewNode.getPanel(); // erzwingt Factory.CreatePanel
	//					// Combo Databases
	//					GuiCombo cmb = (GuiCombo)panel.getGuiComponent("databaseName");
	//					if (cmb.getItems().size() == 0) {
	//						cmb.setItems(pl.getDatabaseNames());
	//					}
	//					// DataSet
	//					panel.reset(); // Vorher alles leer machen
	//					ds.commitChanges();
	//					panel.setDatasetValues(ds);
	//					Document doc = panel.getAllValuesXml();
	//					viewNode.setAllValuesXml(doc);
	//				}
	//				// Popup Menu
	//				{
	//					GuiTreeElement tele = tree.getGuiTreeElement("Request");
	//					if (tele != null) {
	//						viewNode.setPopupMenu(tele.getPopupMenu());
	//					}
	//				}
	//				// 4. RootTable ############################################
	//				// TODO mehrere RootTables
	//				TableRequest rootReq = req.getRootTableRequest();
	//				GuiTreeNode rootTblNode = new GuiTreeNode("Root Table:
	// "+rootReq.getTablename());
	//				viewNode.add(rootTblNode);
	//				rootTblNode.setName(rootReq.getTablename());
	//				rootTblNode.setFileName("PL_RootTablePanel.xml");
	//				// ComboBox füllen
	//				GuiPanel panel = rootTblNode.getPanel(); // erzwingt Factory.CreatePanel
	//				GuiCombo cmb = (GuiCombo)panel.getGuiComponent("dataTable");
	//				if (cmb.getItems().size() == 0) {
	//					ArrayList al = rootReq.getDatabase().getDataTableNames();
	//					cmb.setItems(al);
	//				}
	//				rootTblNode.setMsgNodeClick("rootTblNodeClick");
	//				// Popup Menu
	//				{
	//					GuiTreeElement tele = tree.getGuiTreeElement("RootTable");
	//					if (tele != null) {
	//						rootTblNode.setPopupMenu(tele.getPopupMenu());
	//					}
	//				}
	//				JDataSet ds = new JDataSet("RootRequest");
	//				ds.addRootTable(TableRequest.getMetaDataTable());
	//				ds.addChildRow(rootReq.getMetaDataRow());
	//				rootTblNode.setUserObject2(ds);
	//				// DataSet
	//				ds.commitChanges();
	//				panel.reset();
	//				panel.setDatasetValues(ds);
	//				Document doc = panel.getAllValuesXml();
	//				rootTblNode.setAllValuesXml(doc);
	//				
	//				this.loopTables(rootTblNode, rootReq);
	//			}
	//			win.show();
	//		} catch (Exception ex) {
	//			GuiUtil.showEx(ex);
	//		}
	//	}
	//	private void loopTables(GuiTreeNode rootNode, TableRequest rootReq) {
	//		// 5. Child Tables ###########################################
	//		Iterator ic = rootReq.getChildRequests();
	//		if (ic != null) {
	//			while (ic.hasNext()) {
	//				TableRequest tblReq = (TableRequest)ic.next();
	//				GuiTreeNode childNode = new GuiTreeNode("Child Table: "+
	// tblReq.getTablename());
	//				rootNode.add(childNode);
	//				childNode.setName(tblReq.getRefname());
	//				childNode.setFileName("PL_ChildTablePanel.xml");
	//				// ComboBox füllen
	//				GuiPanel panel = childNode.getPanel(); // erzwingt Factory.CreatePanel
	//				GuiCombo cmb = (GuiCombo)panel.getGuiComponent("dataTable");
	//				if (cmb.getItems().size() == 0) {
	//				    
	//					ArrayList al = tblReq.getDatabaseConnection().getDataTableNames();
	//					cmb.setItems(al);
	//				}
	//				childNode.setMsgNodeClick("childTblNodeClick");
	//				JDataSet ds = new JDataSet("ChildRequest");
	//				ds.addRootTable(TableRequest.getMetaDataTable());
	//				ds.addChildRow(tblReq.getMetaDataRow());
	//				childNode.setUserObject2(ds);
	//				// DataSet
	//				ds.commitChanges();
	//				panel.reset();
	//				panel.setDatasetValues(ds);
	//				Document doc = panel.getAllValuesXml();
	//				childNode.setAllValuesXml(doc);
	//				// Popup Menu
	//				{
	//					GuiTreeElement tele = tree.getGuiTreeElement("ChildTable");
	//					if (tele != null) {
	//						childNode.setPopupMenu(tele.getPopupMenu());
	//					}
	//				}
	//				loopTables(childNode, tblReq);
	//			}
	//		}
	//		// 6. Parent Tables ######################################
	//		Iterator ip = rootReq.getParentRequests();
	//		if (ip != null) {
	//			while (ip.hasNext()) {
	//				TableRequest tblReq = (TableRequest)ip.next();
	//				GuiTreeNode parentNode = new GuiTreeNode("Parent Table: "+
	// tblReq.getTablename());
	//				rootNode.add(parentNode);
	//				parentNode.setName(tblReq.getRefname());
	//				parentNode.setFileName("PL_ParentTablePanel.xml");
	//				// ComboBox füllen
	//				GuiPanel panel = parentNode.getPanel(); // erzwingt Factory.CreatePanel
	//				GuiCombo cmb = (GuiCombo)panel.getGuiComponent("dataTable");
	//				if (cmb.getItems().size() == 0) {
	//					ArrayList al = tblReq.getDatabaseConnection().getDataTableNames();
	//					cmb.setItems(al);
	//				}
	//				parentNode.setMsgNodeClick("parentTblNodeClick");
	//				JDataSet ds = new JDataSet("ParentRequest");
	//				ds.addRootTable(TableRequest.getMetaDataTable());
	//				ds.addChildRow(tblReq.getMetaDataRow());
	//				parentNode.setUserObject2(ds);
	//				// DataSet
	//				panel.reset();
	//				panel.setDatasetValues(ds);
	//				Document doc = panel.getAllValuesXml();
	//				parentNode.setAllValuesXml(doc);
	//				// Popup Menu
	//				{
	//					GuiTreeElement tele = tree.getGuiTreeElement("ParentTable");
	//					if (tele != null) {
	//						parentNode.setPopupMenu(tele.getPopupMenu());
	//					}
	//				}
	//				loopTables(parentNode, tblReq);
	//			}
	//		}
	//	}
	// Events ======================================================
	public void databaseNodeClick(GuiUserEvent e) {
		// Selected Node ermitteln
		/*
		 * GuiTreeNodeSelectionEvent event = (GuiTreeNodeSelectionEvent)e;
		 * GuiTreeNode node = event.node; // DataSet aus Node holen JDataSet ds =
		 * (JDataSet)node.getUserObject2(); //System.out.println(ds.getXml()); //
		 * Panel aus Node holen GuiPanel panel = node.getPanel(); // Panel leer
		 * machen panel.reset(); // Panel Daten zuweisen
		 * panel.setDatasetValues(ds);
		 */
	}

	//	public void tableNodeClick(GuiUserEvent e) {
	//		GuiTreeNodeSelectionEvent event = (GuiTreeNodeSelectionEvent)e;
	//		GuiTreeNode node = event.node;
	//		GuiTreeNode parentNode = node.getGuiParentNode();
	//		String dbName = parentNode.getName();
	//		String tableName = node.getName();
	//		Database db = pl.getDatabase(dbName);
	//		JDataTable tbl = db.getDataTable(tableName);
	//		JDataSet dsTable = tbl.getMetaDataSet();
	//		//System.out.println(dsTable.getXml());
	//		GuiPanel panel = node.getPanel();
	//		panel.reset();
	//		panel.setDatasetValues(dsTable);
	//	}
	//	// In GuiTreeNode.getUserObject2 steckt jeweils ein DataSet
	//	public void requestNodeClick(GuiUserEvent e) {
	//		GuiTreeNodeSelectionEvent event = (GuiTreeNodeSelectionEvent)e;
	//		GuiTreeNode node = event.node;
	//		JDataSet ds = (JDataSet)node.getUserObject2();
	//		GuiPanel panel = node.getPanel();
	//		//##panel.commitChanges();
	//		//##panel.getDatasetValues(ds);
	//		//##System.out.println(ds.getXml());
	//	}
	//	public void rootTblNodeClick(GuiUserEvent e) {
	//		// Selektierter Node
	//		GuiTreeNodeSelectionEvent event = (GuiTreeNodeSelectionEvent)e;
	//		GuiTreeNode node = event.node;
	//		System.out.println("rootTblNodeClick");
	//		System.out.println(node.getAllValuesXml());
	//		// Das Panel zu diesem Node
	//		GuiPanel panel = node.getPanel();
	//		
	//		//##panel.commitChanges();
	//		// Der Dataset zu diesem Node
	//		JDataSet ds = (JDataSet)node.getUserObject2();
	//		// Daten aus dem Panel in den Dataset überführen
	//		//##panel.getDatasetValues(ds);
	//		//##System.out.println(ds.getXml());
	//		// ComboBox "Columns" von GuiTable mit Columns aus DataTable versorgen.
	//		GuiTable gtbl = panel.getGuiTable("tblDataTable");
	//		JDataRow row = ds.getRow();
	//		Database db = pl.getDatabase(row.getValue("DatabaseName"));
	//		try {
	//			String tableName = row.getValue("TableName");
	//			JDataTable dTbl = db.getDataTable(tableName);
	//			ArrayList colNames = dTbl.getDataColumnNames();
	//			colNames.add(0, "*");
	//			gtbl.setItems("columnName", colNames);
	//		} catch (Exception ex) {}
	//	}
	//	public void childTblNodeClick(GuiUserEvent e) {
	//		GuiTreeNodeSelectionEvent event = (GuiTreeNodeSelectionEvent)e;
	//		GuiTreeNode node = event.node;
	//		GuiPanel panel = node.getPanel();
	//		//##panel.commitChanges();
	//		JDataSet ds = (JDataSet)node.getUserObject2();
	//		//##panel.getDatasetValues(ds);
	//		//##System.out.println(ds.getXml());
	//		// Columns
	//		GuiTable gtbl = panel.getGuiTable("tblDataTable");
	//		JDataRow row = ds.getRow();
	//		Database db = pl.getDatabase(row.getValue("DatabaseName"));
	//		try {
	//			String tableName = row.getValue("TableName");
	//			JDataTable dTbl = db.getDataTable(tableName);
	//			ArrayList colNames = dTbl.getDataColumnNames();
	//			colNames.add(0, "*");
	//			gtbl.setItems("columnName", colNames);
	//		} catch (Exception ex) {}
	//	}
	//	public void parentTblNodeClick(GuiUserEvent e) {
	//		GuiTreeNodeSelectionEvent event = (GuiTreeNodeSelectionEvent)e;
	//		GuiTreeNode node = event.node;
	//		GuiPanel panel = node.getPanel();
	//		//##panel.commitChanges();
	//		JDataSet ds = (JDataSet)node.getUserObject2();
	//		//##panel.getDatasetValues(ds);
	//		//##System.out.println(ds.getXml());
	//		// Columns
	//		GuiTable gtbl = panel.getGuiTable("tblDataTable");
	//		JDataRow row = ds.getRow();
	//		Database db = pl.getDatabase(row.getValue("DatabaseName"));
	//		try {
	//			String tableName = row.getValue("TableName");
	//			JDataTable dTbl = db.getDataTable(tableName);
	//			ArrayList colNames = dTbl.getDataColumnNames();
	//			colNames.add(0, "*");
	//			gtbl.setItems("columnName", colNames);
	//		} catch (Exception ex) {}
	//	}
	//	// Menu
	//	public void openConfig(GuiUserEvent event) {
	//		String[] ret = GuiUtil.fileOpenDialog(this.win, "Open Persistence Layer
	// Configuration File", GuiUtil.getCurrentDir(), "*.xml");
	//		if (ret != null) {
	//			try {
	//				File f = new File(ret[0]);
	//				Document doc = new Document(f);
	//				pl = new PL(doc);
	//				win.dispose();
	//				this.configFilename = ret[0];
	//				this.show();
	//				win.setTitle("Persistence Layer: "+this.configFilename);
	//			} catch (Exception ex) {
	//				GuiUtil.showEx(ex);
	//			}
	//		}
	//	}
	//	public void saveConfigAs(GuiUserEvent event) {
	//		String fn = pl.getDatasetDefinitionFileName();
	//		String[] ret = GuiUtil.fileSaveDialog(this.win, "Save Persistence Layer
	// Configuration File",
	//				GuiUtil.getCurrentDir(), fn);
	//		if (ret != null) {
	//			try {
	//				File f = new File(ret[0]);
	//				this.saveConfig(ret[0]);
	//			} catch (Exception ex) {
	//				GuiUtil.showEx(ex);
	//			}
	//		}
	//	}
	//	public void saveConfig(GuiUserEvent event) {
	//		// Filename ermitteln
	//		String fileName = pl.getDatasetDefinitionFileName();
	//		this.saveConfig(fileName);
	//	}
	//	private void saveConfig(String fileName) {
	//		// Den Node mit dem Focus behandelen.
	//		// Wenn das Menü aufgerufen wird gibts keinen Focusverlust beim
	//		// selectierten Node.
	//		{
	//			GuiTreeNode selectedNode = tree.getSelectedNode();
	//			GuiPanel panel = selectedNode.getPanel();
	//			if (panel != null) {
	//				/*
	//				JDataSet dsAlt = (JDataSet)selectedNode.getUserObject2();
	//				System.out.println("saveConfig - selected Node - dsAlt");
	//				System.out.println(dsAlt.getXml());
	//				Document docalt = selectedNode.getAllValuesXml();
	//				System.out.println("saveConfig - selected Node - docAlt");
	//				System.out.println(docalt);
	//				*/
	//				Document doc = panel.getAllValuesXml();
	//				System.out.println("saveConfig - selected Node - doc");
	//				System.out.println(doc);
	//				selectedNode.setAllValuesXml(doc);
	//			}
	//		}
	//		
	//		// 0. DataSet: Schema
	//		JDataSet ds = new JDataSet("PersistenceLayer");
	//		GuiTreeNode rootNode = tree.getRoot();
	//		// 0.1 PL
	//		JDataTable plTbl = PL.getMetaDataTable().cloneTable(false); // Damits
	// wiederholbar bleibt
	//		JDataRow plRow = null;
	//		ds.addRootTable(plTbl);
	//		{
	//			//JDataRow plRow = pl.getMetaDataRow().cloneRow(plTbl);
	//			JDataSet plDs = (JDataSet)rootNode.getUserObject2();
	//			// Aus Node umtüten
	//			Document doc = rootNode.getAllValuesXml();
	//			final GuiPanel rootPanel = rootNode.getPanel();
	//			rootPanel.reset();
	//			rootPanel.setAllValuesXml(doc);
	//			rootPanel.getDatasetValues(plDs);
	//			// DataRow
	//			plRow = plDs.getRow().cloneRow(plTbl); // Database
	//			ds.addChildRow(plRow);
	//		}
	//		// 0.2 Databases
	//		JDataTable dbTbl =
	// PLDatabaseConnection.getMetaDataTable().cloneTable(false);
	//		plTbl.addChildTable(dbTbl, "DatasetDefinitionFile");
	//
	//		{
	//			GuiTreeNode parentDbNode = rootNode.getChildByName("databases");
	//			Iterator it = parentDbNode.getChildNodes();
	//			if (it != null) {
	//				while (it.hasNext()) {
	//					GuiTreeNode dbNode = (GuiTreeNode)it.next();
	//					JDataSet dbDs = (JDataSet)dbNode.getUserObject2();
	//					// Aus Node umtüten
	//					Document doc = dbNode.getAllValuesXml();
	//					final GuiPanel dbPanel = dbNode.getPanel();
	//					dbPanel.reset();
	//					dbPanel.setAllValuesXml(doc);
	//					dbPanel.getDatasetValues(dbDs);
	//					// DataRow
	//					JDataRow dbRow = dbDs.getRow().cloneRow(dbTbl); // Database
	//					plRow.addChildRow(dbRow);
	//				}
	//			}
	//		}
	//		// 0.3 RequestTable
	//		JDataTable reqTbl = Request.getMetaDataTable().cloneTable(false);
	//		plTbl.addChildTable(reqTbl, "DatasetDefinitionFile"); // Request
	//		// 0.4 TableRequest
	//		JDataTable rootTbl = TableRequest.getMetaDataTable().cloneTable(true);
	//		reqTbl.addChildTable(rootTbl, "DatasetName"); // Root Table
	//		// 0.5 Selbstreferenzierend TableRequest
	//		JDataTable selfTbl = rootTbl.cloneTable(true); // Clone (deep wegen
	// Columns)
	//		selfTbl.setSelfReference(true);
	//		rootTbl.addChildTable(selfTbl, "FK_TableRequestId");
	//		// 1. Alle Requests durchgehen
	//		{
	//			GuiTreeNode parentReqNode = rootNode.getChildByName("requests");
	//			Iterator it = parentReqNode.getChildNodes();
	//			if (it != null) {
	//				while (it.hasNext()) {
	//					GuiTreeNode reqNode = (GuiTreeNode)it.next();
	//					// 2. Je Request aus den TreeNodes eine DataRow erzeugen (mit childRow)
	//					JDataSet reqDs = (JDataSet)reqNode.getUserObject2();
	//					// Aus Node umtüten
	//					Document doc = reqNode.getAllValuesXml();
	//					final GuiPanel reqPanel = reqNode.getPanel();
	//					reqPanel.reset();
	//					reqPanel.setAllValuesXml(doc);
	//					reqPanel.getDatasetValues(reqDs);
	//					// DataRow
	//					JDataRow reqRow = reqDs.getRow().cloneRow(reqTbl); // Request
	//					plRow.addChildRow(reqRow);
	//					if (reqNode.getChildCount() > 0) {
	//						// Root Table
	//						GuiTreeNode rootTblNode = (GuiTreeNode)reqNode.getChildAt(0);
	//						// Aus Node umtüten
	//						// Aus Dem Node entnehmen ...
	//						Document rootTblDoc = rootTblNode.getAllValuesXml();
	//						//System.out.println("saveConfig - rootTblDoc");
	//						//System.out.println(rootTblDoc);
	//						GuiPanel rootTblPanel = rootTblNode.getPanel();
	//						// ... ins Panel übertragen ...
	//						rootTblPanel.reset();
	//						rootTblPanel.setAllValuesXml(rootTblDoc);
	//						// ... und von da in DataSet.
	//						JDataSet rootTblDs = (JDataSet)rootTblNode.getUserObject2();
	//						rootTblPanel.getDatasetValues(rootTblDs);
	//						//System.out.println("saveConfig - rootTblDs");
	//						//System.out.println(rootTblDs.getXml());
	//						// Rückweg
	//						rootTblDs.commitChanges(); // besser später!
	//						rootTblPanel.reset(); // Zuerst Panel leer machen
	//						// Aus dem DataSet zurück ins Panel...
	//						rootTblPanel.setDatasetValues(rootTblDs);
	//						// ... und vom Panel ...
	//						Document pDoc = rootTblPanel.getAllValuesXml();
	//						// ... wieder in den TreeNode.
	//						rootTblNode.setAllValuesXml(pDoc);
	//						JDataRow rootTblRow = rootTblDs.getRow().cloneRow(rootTbl);
	//						reqRow.addChildRow(rootTblRow); // Root Table
	//						this.loopSaveNodes(rootTblNode, rootTblRow, selfTbl);
	//					}
	//				}
	//			}
	//		}
	//		System.out.println("saveConfig - ds (complete)");
	//		System.out.println(ds.getXml());
	//		try {
	//			PL testPl = new PL(ds);
	//			Document doc = testPl.getPLConfig();
	//			//Document doc = testPl.getDatabaseConfig();
	//			//System.out.println(doc);
	//			File f = new File(fileName);
	//			FileWriter fos = new FileWriter(f);
	//			doc.write(fos);
	//			fos.close();
	//			this.commitChanges();
	//		} catch (Exception ex) {
	//			GuiUtil.showEx(ex);
	//		}
	//	}
	/*
	private void loopSaveNodes(GuiTreeNode parentNode, JDataRow parentRow,
			JDataTable myTbl) {
		JDataSet reqDs = (JDataSet) parentNode.getUserObject2();
		Iterator it = parentNode.getChildNodes();
		if (it == null) {
			return;
		}
		while (it.hasNext()) {
			GuiTreeNode childNode = (GuiTreeNode) it.next();
			JDataSet childTblDs = (JDataSet) childNode.getUserObject2();
			// Daten zwischen Panel, TreeNode und DataSet synchronisieren.
			// Daten aus dem TreeNode...
			Document doc = childNode.getAllValuesXml();
			GuiPanel panel = childNode.getPanel();
			panel.reset();
			// ...ins Panel...
			panel.setAllValuesXml(doc);
			//...und von dort in den Dataset.
			panel.getDatasetValues(childTblDs);
			// Rückweg
			childTblDs.commitChanges(); // besser später!
			panel.reset();
			// Aus dem DataSet zurück ins Panel...
			panel.setDatasetValues(childTblDs);
			// ... und vom Panel ...
			Document pDoc = panel.getAllValuesXml();
			// ... wieder in den TreeNode.
			childNode.setAllValuesXml(pDoc);
			JDataRow childRow = childTblDs.getRow().cloneRow(myTbl);
			parentRow.addChildRow(childRow);

			//##panel.commitChanges();
			//##Document tmpDoc = panel.getAllValuesXml();
			//##childNode.setAllValuesXml(tmpDoc);

			this.loopSaveNodes(childNode, childRow, myTbl);
		}
	}
*/
	/**
	 * Muß nach dem Speichern aufgerufen werden.
	 *  
	 */
	/*
	private void commitChanges() {
		// 1. Alle Requests durchgehen
		GuiTreeNode rootNode = tree.getRoot();
		GuiTreeNode parentReqNode = rootNode.getChildByName("requests");
		Iterator it = parentReqNode.getChildNodes();
		if (it == null) {
			return;
		}
		while (it.hasNext()) {
			GuiTreeNode reqNode = (GuiTreeNode) it.next();
			// 2. Je Request aus den TreeNodes eine DataRow erzeugen (mit
			// childRow)
			JDataSet reqDs = (JDataSet) reqNode.getUserObject2();
			reqDs.commitChanges();
			if (reqNode.getChildCount() > 0) {
				GuiTreeNode rootTblNode = (GuiTreeNode) reqNode.getChildAt(0);
				JDataSet rootTblDs = (JDataSet) rootTblNode.getUserObject2();
				rootTblDs.commitChanges();
				this.loopCommitNodes(rootTblNode);
			}
		}
	}
*
	private void loopCommitNodes(GuiTreeNode parentNode) {
		JDataSet reqDs = (JDataSet) parentNode.getUserObject2();
		Iterator it = parentNode.getChildNodes();
		if (it == null) {
			return;
		}
		while (it.hasNext()) {
			GuiTreeNode childNode = (GuiTreeNode) it.next();
			JDataSet childTblDs = (JDataSet) childNode.getUserObject2();
			childTblDs.commitChanges();
			this.loopCommitNodes(childNode);
		}
	}
*/
	// Requests ================================================
	public void addRequest(GuiUserEvent event) {
		GuiTreeNode rootViewNode = tree.getSelectedNode();
		GuiTreeNode viewNode = new GuiTreeNode("Request: ");
		viewNode.setFileName("PL_RequestPanel.xml");
		viewNode.setMsgNodeClick("requestNodeClick");
		// DataSet für TreeNode
		{
			JDataSet ds = new JDataSet("Request");
			ds.addRootTable(Request.getMetaDataTable());
			ds.addChildRow(ds.getDataTable().createNewRow());
			viewNode.setUserObject2(ds);
		}
		rootViewNode.add(viewNode);
		// Popup Menu
		{
			GuiTreeElement tele = tree.getGuiTreeElement("Request");
			if (tele != null) {
				viewNode.setPopupMenu(tele.getPopupMenu());
			}
		}
		// Default Database?
		String dbName = null;
		GuiTreeNode rootNode = tree.getRoot();
		try {
			GuiTreeNode dbNode = (GuiTreeNode) rootNode.getChildAt(0);
			dbName = dbNode.getValue("databaseName");
			GuiPanel viewPanel = viewNode.getPanel(); // Erzwingt createPanel
			viewPanel.setValue("databaseName", dbName);
			Document viewDoc = viewPanel.getAllValuesXml();
			viewNode.setAllValuesXml(viewDoc);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void addRootTable(GuiUserEvent event) {
		// Selected Node (= Request)
		GuiTreeNode viewNode = tree.getSelectedNode();
		// New Node "Root Table: xxx"
		GuiTreeNode rootTblNode = new GuiTreeNode("Root Table: ");
		// Add Node
		viewNode.add(rootTblNode);
		// Node Panel
		rootTblNode.setFileName("PL_RootTablePanel.xml");
		// Node Click Event
		rootTblNode.setMsgNodeClick("rootTblNodeClick");
		// Popup Menu
		{
			GuiTreeElement tele = tree.getGuiTreeElement("RootTable");
			if (tele != null) {
				rootTblNode.setPopupMenu(tele.getPopupMenu());
			}
		}
		// New DataSet
		JDataSet ds = new JDataSet("RootRequest");
		// DataTable
		ds.addRootTable(TableRequest.getMetaDataTable());
		// DataRow
		ds.addChildRow(ds.getDataTable().createNewRow());
		// Database von Request übernehmen (vorerst)
		String dbName = viewNode.getValue("databaseName");
		ds.getRow().setValue("DatabaseName", dbName);
		ds.getRow().setValue("RequestType", TableRequest.ROOT_TABLE);
		rootTblNode.setUserObject2(ds);
	}

	public void addChildTable(GuiUserEvent event) {
		GuiTreeNode viewNode = tree.getSelectedNode();
		GuiTreeNode childTblNode = new GuiTreeNode("Child Table: ");
		viewNode.add(childTblNode);
		childTblNode.setFileName("PL_ChildTablePanel.xml");
		childTblNode.setMsgNodeClick("childTblNodeClick");
		// Popup Menu
		{
			GuiTreeElement tele = tree.getGuiTreeElement("ChildTable");
			if (tele != null) {
				childTblNode.setPopupMenu(tele.getPopupMenu());
			}
		}
		JDataSet ds = new JDataSet("ChildRequest");
		ds.addRootTable(TableRequest.getMetaDataTable());
		ds.addChildRow(ds.getDataTable().createNewRow());
		// Database von Request übernehmen (vorerst)
		JDataSet dsParent = (JDataSet) viewNode.getUserObject2();
		String dbName = dsParent.getRow().getValue("DatabaseName");
		ds.getRow().setValue("DatabaseName", dbName);
		ds.getRow().setValue("RequestType", TableRequest.CHILD_TABLE);
		childTblNode.setUserObject2(ds);
	}

	public void addParentTable(GuiUserEvent event) {
		GuiTreeNode viewNode = tree.getSelectedNode();
		GuiTreeNode parentTblNode = new GuiTreeNode("Parent Table: ");
		viewNode.add(parentTblNode);
		parentTblNode.setFileName("PL_ParentTablePanel.xml");
		parentTblNode.setMsgNodeClick("parentTblNodeClick");
		// Popup Menu
		{
			GuiTreeElement tele = tree.getGuiTreeElement("ParentTable");
			if (tele != null) {
				parentTblNode.setPopupMenu(tele.getPopupMenu());
			}
		}
		JDataSet ds = new JDataSet("ParentRequest");
		ds.addRootTable(TableRequest.getMetaDataTable());
		ds.addChildRow(ds.getDataTable().createNewRow());
		// Database von Request übernehmen (vorerst)
		JDataSet dsParent = (JDataSet) viewNode.getUserObject2();
		String dbName = dsParent.getRow().getValue("DatabaseName");
		ds.getRow().setValue("DatabaseName", dbName);
		ds.getRow().setValue("RequestType", TableRequest.PARENT_TABLE);
		parentTblNode.setUserObject2(ds);
	}

	public void removeNode(GuiUserEvent event) {
		GuiTreeNode viewNode = tree.getSelectedNode();
		GuiTreeNode parentNode = viewNode.getGuiParentNode();
		parentNode.remove(viewNode);
	}

	// Changed =================================
	public void requestNameChanged(GuiUserEvent event) {
		GuiChangeEvent e = (GuiChangeEvent) event;
		GuiTreeNode viewNode = tree.getSelectedNode();
		viewNode.setTitle("Request: " + e.value);
		viewNode.setName(e.value.toString());
	}
	//	public void rootTableChanged(GuiUserEvent event) {
	//		GuiChangeEvent e = (GuiChangeEvent)event;
	//		GuiTreeNode viewNode = tree.getSelectedNode();
	//		viewNode.setTitle("Root Table: "+e.value);
	//		viewNode.setName(e.value.toString());
	//		// ComboBox Columns
	//		GuiPanel panel = viewNode.getPanel();
	//		JDataSet ds = (JDataSet)viewNode.getUserObject2();
	//		//System.out.println(ds.getXml());
	//		// Columns
	//		GuiTable gtbl = panel.getGuiTable("tblDataTable");
	//		JDataRow row = ds.getRow();
	//		Database db = pl.getDatabase(row.getValue("DatabaseName"));
	//		try {
	//			String tableName = panel.getValue("dataTable").toString();
	//			JDataTable dTbl = db.getDataTable(tableName);
	//			ArrayList colNames = dTbl.getDataColumnNames();
	//			colNames.add(0, "*");
	//			gtbl.setItems("columnName", colNames);
	//		} catch (Exception ex) {}
	//	}
	//	public void childTableChanged(GuiUserEvent event) {
	//		GuiChangeEvent e = (GuiChangeEvent)event;
	//		GuiTreeNode viewNode = tree.getSelectedNode();
	//		viewNode.setTitle("Child Table: "+e.value);
	//		viewNode.setName(e.value.toString());
	//		// ComboBox Columns
	//		GuiPanel panel = viewNode.getPanel();
	//		JDataSet ds = (JDataSet)viewNode.getUserObject2();
	//		// Columns
	//		GuiTable gtbl = panel.getGuiTable("tblDataTable");
	//		JDataRow row = ds.getRow();
	//		Database db = pl.getDatabase(row.getValue("DatabaseName"));
	//		try {
	//			String tableName = panel.getValue("dataTable").toString();
	//			JDataTable dTbl = db.getDataTable(tableName);
	//			ArrayList colNames = dTbl.getDataColumnNames();
	//			colNames.add(0, "*");
	//			gtbl.setItems("columnName", colNames);
	//		} catch (Exception ex) {}
	//	}
	//	public void parentTableChanged(GuiUserEvent event) {
	//		GuiChangeEvent e = (GuiChangeEvent)event;
	//		GuiTreeNode viewNode = tree.getSelectedNode();
	//		viewNode.setTitle("Parent Table: "+e.value);
	//		viewNode.setName(e.value.toString());
	//		// ComboBox Columns
	//		GuiPanel panel = viewNode.getPanel();
	//		JDataSet ds = (JDataSet)viewNode.getUserObject2();
	//		//##panel.getDatasetValues(ds);
	//		// Columns
	//		GuiTable gtbl = panel.getGuiTable("tblDataTable");
	//		JDataRow row = ds.getRow();
	//		Database db = pl.getDatabase(row.getValue("DatabaseName"));
	//		try {
	//			String tableName = panel.getValue("dataTable").toString();
	//			JDataTable dTbl = db.getDataTable(tableName);
	//			ArrayList colNames = dTbl.getDataColumnNames();
	//			colNames.add(0, "*");
	//			gtbl.setItems("columnName", colNames);
	//		} catch (Exception ex) {}
	//	}
}