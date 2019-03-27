package de.guibuilder.design;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.guibuilder.framework.GDLParseException;
import de.guibuilder.framework.GuiAction;
import de.guibuilder.framework.GuiDialog;
import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiUtil;
import de.guibuilder.framework.GuiWindow;
import de.guibuilder.framework.event.GuiActionEvent;
import de.guibuilder.framework.event.GuiCreateEvent;
import de.guibuilder.framework.event.GuiUserEvent;
import de.jdataset.JDataSet;
import electric.xml.Document;

public class ControlEditProperties {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(ControlEditProperties.class);
	private static String filename = "GuiBuilderConfigEditor.xml";
	private static String filenameConfig = GuiUtil.GUIBUILDER_CONFIG;
	private GuiDialog dia;

	public ControlEditProperties() {
		this.showDialog();
	}

	private void showDialog() {
		String xmlSrc = null;
		try {
			// in der Codebase suchen
			String name = GuiUtil.getCodeBase() + filename;
			dia = (GuiDialog) GuiFactory.getInstance().createWindow(name);
		} catch (Exception ex) {
			// im Classpath suchen
			InputStream isXmlSrc = ClassLoader
					.getSystemResourceAsStream(filename);
			if (isXmlSrc != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						isXmlSrc));

				StringBuffer sb = new StringBuffer();
				try {
					String line = null;
					while ((line = br.readLine()) != null) {
						sb.append(line + "\n");
					}
				} catch (Exception ex1) {
					GuiUtil.showEx(ex1);
				} finally {
					try {
						br.close();
						isXmlSrc.close();
					} catch (Exception ex2) {
						GuiUtil.showEx(ex2);
					}
				}
				xmlSrc = sb.toString();
			}
			try {
				dia = (GuiDialog) GuiFactory.getInstance().createWindowXml(
						xmlSrc);
			} catch (GDLParseException pex) {
				GuiUtil.showEx(pex);
			}
		}
		dia.show();
		dia.setController(this);
		dia.getAction("load").click();
		GuiUtil.loadGuiPropXml(); // geänderte Einträge neu laden
	}

	public void formLoad(GuiUserEvent event) {
		String strFile = null;
		File file = null;
		JDataSet ds = null;

		try {
			strFile = GuiUtil.getLocalDir()
					+ System.getProperty("file.separator") + filenameConfig;
			// System.out.println("Try Loading " + filenameConfig + " from: " +
			// strFile + ".");
			logger.info("Try Loading " + filenameConfig + " from: " + strFile
					+ ".");
			file = new File(strFile);
			ds = new JDataSet(new Document(file));
			ds.commitChanges(); // Alte Änderungen committen.
			// System.out.println(ds.getXml());
			// event.window.setDatasetValues(ds);
			dia.setDatasetValues(ds);
			logger.info("Loaded " + filenameConfig + " from: " + strFile + ".");
			// System.out.println("Loaded " + filenameConfig + " from: " +
			// strFile + ".");
		} catch (Exception ex) {
			logger.info("Error Loading " + filenameConfig + " from: " + strFile
					+ ".");
			// System.out.println("Error Loading " + filenameConfig + " from: "
			// + strFile + ".");
			try {
				strFile = GuiUtil.getUserDir()
						+ System.getProperty("file.separator") + filenameConfig;
				file = new File(strFile);
				logger.info("Try Loading " + filenameConfig + " from: "
						+ file.toString() + ".");
				// System.out.println("Try Loading " + filenameConfig + " from: "
				// 		+ file.toString() + ".");
				ds = new JDataSet(new Document(file));
				// event.window.setDatasetValues(ds);
				dia.setDatasetValues(ds);
				ds.commitChanges(); // Alte Änderungen committen.
				// System.out.println(ds.getXml());
				logger.info("Loaded " + filenameConfig + " from: "
						+ file.toString() + ".");
				// System.out.println("Loaded " + filenameConfig + " from: "
				//		+ file.toString() + ".");
			} catch (Exception ex1) {
				logger.info("Error Loading " + filenameConfig
						+ " from: " + file.toString() + ".");
				// System.out.println("Error Loading " + filenameConfig
				//		+ " from: " + file.toString() + ".");
				try {
					// im Classpath suchen
					logger.info("Try Loading " + filenameConfig
							+ " from Classpath.");
					// System.out.println("Try Loading " + filenameConfig
					//		+ " from Classpath.");
					String xmlSrc = null;
					InputStream isXmlSrc = ClassLoader
							.getSystemResourceAsStream(filenameConfig);
					if (isXmlSrc != null) {
						BufferedReader br = new BufferedReader(
								new InputStreamReader(isXmlSrc));

						StringBuffer sb = new StringBuffer();
						try {
							String line = null;
							while ((line = br.readLine()) != null) {
								sb.append(line + "\n");
							}
						} catch (Exception ex2) {
							logger.error(ex2.getMessage(), ex2);
							GuiUtil.showEx(ex2);
						} finally {
							try {
								br.close();
								isXmlSrc.close();
							} catch (Exception ex3) {
								logger.error(ex3.getMessage(), ex3);
								GuiUtil.showEx(ex3);
							}
						}
						xmlSrc = sb.toString();
						if (xmlSrc != null) {
							ds = new JDataSet(new Document(xmlSrc));
							// System.out.println(ds.getXml());
							// event.window.setDatasetValues(ds);
							dia.setDatasetValues(ds);
							ds.commitChanges(); // Alte Änderungen committen.
							logger.info("Loaded " + filenameConfig
									+ " from Classpath.");
							// System.out.println("Loaded " + filenameConfig
							//		+ " from Classpath.");
						} else {
							logger.error("Error loading "
									+ filenameConfig + " - File could not be loaded.");
							System.err.println("Error loading "
									+ filenameConfig + " - File could not be loaded.");
						}
					}
				} catch (Exception ex2) {
					logger.error(ex2.getMessage(), ex2);
					GuiUtil.showEx(ex2);
				}
			}
		}
		GuiUtil.loadGuiPropXml();
	}

	public void close(GuiUserEvent event) {
		dia.hide();
		dia.dispose();
	}

	public void save(GuiUserEvent event) {
		JDataSet ds = event.window.getDatasetValues();
		String saveFile = GuiUtil.getLocalDir()
				+ System.getProperty("file.separator") + filenameConfig;

		// if (ds.hasChanges()) {
		logger.info("Saving properties to: " + saveFile);
		// System.out.println("Saving properties to: " + saveFile);
		// System.out.println(ds.getXml());
		Document doc = ds.getXml();

		File outFile = new File(saveFile);
		try {
			outFile.createNewFile();
			doc.write(outFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ds.commitChanges();
		GuiUtil.loadGuiPropXml();
		// }
	}
}
