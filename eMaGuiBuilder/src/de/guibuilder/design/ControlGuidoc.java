/*
 * Created on 12.03.2005
 */
package de.guibuilder.design;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import de.guibuilder.framework.CurrentKeyword;
import de.guibuilder.framework.GuiDialog;
import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiList;
import de.guibuilder.framework.GuiUtil;
import de.guibuilder.framework.GuiWindow;
import de.guibuilder.framework.XmlReader;

/**
 * Controller für die Generierung von GuiDoc
 * 
 * @author peter
 */
public class ControlGuidoc {
	private String filename;

	public ControlGuidoc(String filename) {
		this.filename = filename;
		this.showDialog();
	}

	private void showDialog() {
		try {
			GuiDialog dia = (GuiDialog) GuiFactory.getInstance().createWindow("GuiDoc.xml");
			dia.setValue("filename", filename);
			GuiList list = (GuiList) dia.getRootPane().getMainPanel().getGuiComponent(
					"format");
			String[] formats = ImageIO.getWriterFormatNames();
			list.setItems(formats);
			list.setSelectedItem("png");
			if (dia.showDialog()) {
				this.generate(dia);
			}
		} catch (Exception ex) {
			GuiUtil.showEx(ex);
		}
	}

	private void generate(GuiDialog dia) {
		String breite = dia.getValue("width").toString();
		String format = dia.getValue("format").toString();
		format = "." + format;
		if (filename.toLowerCase().endsWith(".make")) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(GuiUtil.getCurrentDir()
						+ System.getProperty("file.separator") + filename));
				String source;
				try {
					source = reader.readLine();
					while (source != null) {
						source = source.trim();
						if (source.length() > 0 && source.startsWith("#") == false) {
							this.makeGuiDoc(source, breite, format);
						}
						source = reader.readLine();
					}
				} catch (Exception ex) {
					GuiUtil.showEx(ex);
				}
			} catch (Exception ex) {
				GuiUtil.showEx(ex);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Exception ex) {
					}
					;
				}
			}
		} else {
			makeGuiDoc(filename, breite, format);
		}

	}

	private void makeGuiDoc(String fileName, String breite, String format) {
		ArrayList<CurrentKeyword> keys = null;
		System.out.println(fileName);
		String source = GuiUtil.fileToString(fileName);
		try {
			keys = XmlReader.makeKeywordListFromString(source);
			// Generate HTML
			GuiDoc.generate(keys, fileName, breite, format);
			// Hardcopy
			GuiWindow tmpWindow = GuiFactory.getInstance().createWindow(fileName);
			if (tmpWindow.isModal()) {
				tmpWindow.setModal(false);
			}
			tmpWindow.show();
			tmpWindow.getComponent().setCursor(new Cursor(Cursor.WAIT_CURSOR));
			GuiDoc.createHardcopy(tmpWindow, fileName, format);
			tmpWindow.getComponent().setCursor(Cursor.getDefaultCursor());
			tmpWindow.dispose();
		} catch (Exception ex) {
			GuiUtil.showEx(ex);
		}

	}
}