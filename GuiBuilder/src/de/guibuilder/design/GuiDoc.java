package de.guibuilder.design;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import de.guibuilder.framework.CurrentAttrib;
import de.guibuilder.framework.CurrentKeyword;
import de.guibuilder.framework.GDLParseException;
import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiOutlookBar;
import de.guibuilder.framework.GuiTab;
import de.guibuilder.framework.GuiTabset;
import de.guibuilder.framework.GuiUtil;
import de.guibuilder.framework.GuiWindow;
import de.guibuilder.framework.XmlReader;
import de.guibuilder.framework.GuiContainer;
import de.guibuilder.framework.GuiButtonBar;

// TODO :Reichlich! Mit XSL? Velocity?
/**
 * Erzeugt eine HTML-Dokumentation eines Formulars oder eines Dialoges.
 * Kommentare im Script werden wie bei JavaDoc behandelt: Sie m�ssen vor der zu
 * kommentierenden Komponente stehen.
 */
public final class GuiDoc {
	private static boolean hasTitle = false;

	private static String breite = "900";

	private static String defaultFormat = "png";

	private static final String HARDCOPY_CMDLINE_WIN = "SCREENSHOT_WIN";
	private static final String HARDCOPY_CMDLINE_COMP = "SCREENSHOT_COMP";
	
	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(GuiDoc.class);

	/**
	 * Aufruf der Dokumentation von der Kommandozeile. Erzeugt Screenshots 
	 * für Fenster und Komponenten.
	 *  
	 *  Usage: GuiDoc -cmd=" + HARDCOPY_CMDLINE_WIN + " -filename=<inputfile.xml> -srcdir=<inputdir> -targetdir=<outputdir> [-uimanager=windows|metal|motif|gtk|kunststoff]
	 *   oder
	 *  Usage: GuiDoc -cmd=" + HARDCOPY_CMDLINE_COMP +" -filename=<inputfile.xml> -srcdir=<inputdir> -targetdir=<outputdir> -componentToShow=<pathToComponent> [-uimanager=windows|metal|motif|gtk|kunststoff]
	 *    
	 * @param args Steuerungsparameter siehe Beschreibung
	 */
    public static void main(String[] args) {

        int i = 0;
        int j = 0;
        String param; // ein Parameter aus Argumenten
        String outdir = GuiUtil.getTempDir();
        String uimanager = null;
        String srcfile = null;
        String srcdir = GuiUtil.getCurrentDir();
        String comp = "GDL.@From.";
        String cmd = HARDCOPY_CMDLINE_WIN;
        String outfile = null;
        
        while (i < args.length && args[i].startsWith("-")) {
            param = args[i++];
            j = param.indexOf("=");
            if (j>0) {
              String key = param.substring(1,j).toLowerCase();
              String val = param.substring(j+1, param.length());
              if (key.equalsIgnoreCase("u") || key.equalsIgnoreCase("uimanager")) {
            	  uimanager = val;
              } else if (key.equalsIgnoreCase("f") || key.equalsIgnoreCase("filename")) {
            	  srcfile = val;
              } else if (key.equalsIgnoreCase("s") || key.equalsIgnoreCase("srcdir")) {
            	  srcdir = val;
              } else if (key.equalsIgnoreCase("t") || key.equalsIgnoreCase("targetdir")) {
            	  outdir = val;
              } else if (key.equalsIgnoreCase("p") || key.equalsIgnoreCase("componentToShow")) {
            	  comp = val;
	          } else if (key.equalsIgnoreCase("c") || key.equalsIgnoreCase("cmd")) {
	        	  if (val!=null) {
	        		  cmd = val;
	        	  }
	          }
            }
        }
            
    	if (cmd.equalsIgnoreCase(HARDCOPY_CMDLINE_WIN) && srcfile!=null) {
    		// Screenshot für Fenster (für Form und Dialog) erzeugen
    		System.out.println("Creating Screenshot of Window from '" + srcdir + File.separator + srcfile + "'.");
    		outfile = createHardcopyWin(srcdir, srcfile, outdir, uimanager);
    		System.out.println("Screenshot sucessfully created in '" + outdir + File.separator + outfile + "'.");
    	} else if (cmd.equalsIgnoreCase(HARDCOPY_CMDLINE_COMP) && srcfile!=null) {
    		// Screenshot für Komponente erzeugen
    		System.out.println("Creating Screenshot of Component '" + comp + "' from '" + srcdir + File.separator + srcfile + "'.");
    		outfile = createHardcopyForComponent(srcdir, srcfile, outdir, comp, uimanager);
    		System.out.println("Screenshot of Component '" + comp + "' sucessfully created in '" + outdir + File.separator + outfile + "'.");    		
    	} else {
    		// Fehler und Hilfe ausgeben
    		System.err.println("Failed creating screenshot from '" + srcdir + File.separator + srcfile + "' in '" + outdir + "'.");
      		System.out.println("Usage: GuiDoc -cmd=" + HARDCOPY_CMDLINE_WIN + " -filename=<inputfile.xml> -srcdir=<inputdir> -targetdir=<outputdir> [-uimanager=windows|metal|motif|gtk|kunststoff]");
      		System.out.println("   or");
      		System.out.println("Usage: GuiDoc -cmd=" + HARDCOPY_CMDLINE_COMP +" -filename=<inputfile.xml> -srcdir=<inputdir> -targetdir=<outputdir> -componentToShow=<pathToComponent> [-uimanager=windows|metal|motif|gtk|kunststoff]\n");
      		System.out.println("   Erzeugt einen Screenshot von der übergebenen GuiBuilder XML-Datei (inputfile.xml) im Quell-Verzeichnis (inputdir) im PNG-Format");
      		System.out.println("   und legt diesen im Zielverzeichnis (outputdir) ab. Soll nicht das initiale Fenster, sondern eine Komponente ausgegeben werden,");
      		System.out.println("   die beim öffnen zun�chst verdeckt ist (z.B. die zweite Regiserkarte), dann muss der Pfad zur Komponente angegeben werden, die");
      		System.out.println("   von Interesse ist (pathToComponent). Ein Java UI-Manager kann angegeben werden um das Aussehen der Screenshots");
      		System.out.println("   zu steuern.\n");
      		System.out.println("where options are:");
      		System.out.println("   -cmd=" + HARDCOPY_CMDLINE_WIN + "|" + HARDCOPY_CMDLINE_COMP + "\n" +
      				           "           Auswahl von welchem Element ein Screenshot erstellt werden soll.");
      		System.out.println("   -filename=<inputfile.xml>\n" +
      				  		   "           GuiBuilder XML-Datei von deren Inhalt ein Screenshot gemacht werden soll");
      		System.out.println("   -targetdir=<outputdir>\n" +
      	                       "           Verzeichnis, in dem die Bild-Datei des Screenshots abgelegt wird");
      		System.out.println("   -srcdir=<outputdir>\n" + 
      				           "           Quellverzeichnis, in dem die GuiBuilder XML-Datei liegt");
      		System.out.println("   -componentToShow=<pathToComponent>\n" + 
	                           "           Pfad zur Komponente, die angezeigt werden soll. Beispiel für Pfadangabe ist:\n" +
	                           "              GDL.@Form.frmName@Tabset.tbsRegisterkaren@Tab#1\n" + 
	                           "           und bedeutet, dass die zweite Registerkarte (Z�hlung beginnt bei 0) des Register-\n" +
	                           "           kartensatzes mit dem Namen 'tbsRegisterkartensatz' (also <tabset name='tbsregisterkartensatz'>)\n" +
	                           "           im Fenster mit dem Namen 'frmName' (also <Form name='frmName'>) ausgegeben wird.\n" +
	                           "           Der Pfad hat also das Format:\n" + 
	                           "              Tag.name@Tag.name@Tag.name\n" + 
	                           "           oder\n" + 
	                           "              Tag#Nummer@Tag#Nummer@Tag#Nummer\n" + 
	                           "           oder auch vermischt\n" + 
	                           "              Tag.Name@Tag#Nummer@Tag.Name\n" + 
	                           "           Beginnen muss der Tag immer mit GDL, wobei der GDL-Name ignoriert wird." );
      		System.out.println("   -uimanager=windows|metal|motif|gtk|kunststoff]\n" + 
      				           "           Auswahl des Java UI-Managers.");
       
      }
    }
	
	/**
	 * Generiert HTML-File
	 *
	 * @param keys
	 *            Der Vector mit den Komponenten eine Gui-Spezifikation.
	 * @param name
	 *            Dateiname der Spezifikation.
	 * @param b
	 *            Breite der Tabelle
	 * @param format
	 *            Grafisches Format der Bildschirm-Hardcopies (.png, .jpg, ...)
	 */
	static void generate(ArrayList<CurrentKeyword> keys, String name,
			String width, String format) throws IllegalArgumentException {
		// Check
		final int p = name.indexOf(".");
		if (p == -1) {
			throw new IllegalArgumentException("Illegal Filename: " +name);
		}
		if (keys == null) {
			throw new IllegalArgumentException("Missing Components: " +name);
		}
		// Tabellenbreite
		breite = width;
		// Filename
		String fileName = name.substring(0, p) + ".html";
		String gif = name.substring(0, p);

		File f = null;
		PrintStream out = null;
		if (fileName.indexOf(":") == 1) { // Laufwerksbuchstabe?
			f = new File(fileName);
		} else { // relative Pfadangabe
			f = new File(GuiUtil.getCurrentDir(), fileName);
		}
		try {
			FileOutputStream fos = new FileOutputStream(f);
			out = new PrintStream(fos);
		} catch (Exception exc) {
			GuiUtil.showEx(exc);
			return;
		}
		CurrentKeyword curKey;
		String sKey;
		String label;
		String icon;
		String mn; // Mnemonic
		String acc; // Accelerator
		String aName;
		String ref;
		String dsp;
		String nn;
		String file;
		String comment;
		boolean isTable = false;
		boolean isToolbar = false;
    String type = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\""
       + "\"http://www.w3.org/TR/html4/strict.dtd\">";
		out.println(type);
    out.println("<HTML>");
		out.println("<HEAD>");
		out.println("<Title>" + gif + "</Title>");
		out.println("</HEAD>");
		out.println("<BODY BGCOLOR=\"#ffffff\">");
		// Schleife Keywords
		for (int iKey = 0; iKey < keys.size(); iKey++) {
			// CurrentKeyword setzen
			curKey = keys.get(iKey);
			// Case Version
			if (curKey.version != null) {
				if (curKey.version.indexOf(GuiUtil.getVersion()) == -1) {
					continue; // Dieses Keyword weglassen weil andere Version
				}
			} // EndIf Version
			sKey = curKey.sKeyword;
			comment = curKey.comment;
			if (comment != null && (comment.startsWith("*"))) {
				comment = comment.substring(1);
			} else {
				comment = "&nbsp;";
			}
			if (curKey.title.length() > 0) {
				label = curKey.title;
				final int poi = label.indexOf("%");
				if (poi != -1 && poi + 1 < label.length()) {
					final char mnemo = label.charAt(poi + 1);
					label = label.substring(0, poi) + "<U>" + mnemo + "</U>"
							+ label.substring(poi + 2);
				}
			} else {
				label = "&nbsp;";
			}
			// System.out.println(sKey);
			// Attribute
			aName = "&nbsp;";
			ref = "&nbsp;";
			dsp = "&nbsp;";
			nn = "&nbsp;";
			mn = "";
			acc = "";
			file = "";
			icon = "&nbsp;";
			// Schleife Attribute
			for (CurrentAttrib curAtt: curKey.vAttrib) {
				//curAtt = (CurrentAttrib) e.next();
				if (curAtt.sKeyword.equals("name=")) {
					aName = curAtt.sValue;
				}
				// else if(curAtt.sKeyword.equals("ref=")) {
				else if (curAtt.sKeyword.equals("element=")) {
					ref = curAtt.sValue;
				} else if (curAtt.sKeyword.equals("cmd=")) {
					ref = curAtt.sValue;
				} else if (curAtt.sKeyword.equals("gif=")) { // deprecated
					icon = curAtt.sValue;
				} else if (curAtt.sKeyword.equals("img=")) {
					icon = curAtt.sValue;
				} else if (curAtt.sKeyword.equals("do=")) {
					dsp = curAtt.sValue;
				} else if (curAtt.sKeyword.equals("nn=")) {
					nn = curAtt.sValue;
				} else if (curAtt.sKeyword.equals("mn=")) {
					mn = curAtt.sValue.toLowerCase();
					String xLabel = label;
					int posi = xLabel.toLowerCase().indexOf(mn);
					if (posi != -1) {
						label = label.substring(0, posi) + "<U>"
								+ label.charAt(posi) + "</U>"
								+ label.substring(posi + 1);
					}
				} else if (curAtt.sKeyword.equals("acc=")) {
					acc = curAtt.sValue;
					label = label + " " + acc;
				} else if (curAtt.sKeyword.startsWith("On") && isTable == true) {
					comment = comment + curAtt.sKeyword + curAtt.sValue;
					// } else if (curAtt.sKeyword.equals("file=") && isTable ==
					// true) {
				} else if (curAtt.sKeyword.equals("file=")) {
					file = curAtt.sValue;
					StringTokenizer tokens = new StringTokenizer(file, ";");
					while (tokens.hasMoreTokens()) {
						String tok = tokens.nextToken().trim();
						// if (file.endsWith("()") == false) {
						if (file.endsWith(")") == false) {
							int posi = tok.indexOf(".");
							if (posi != -1) {
								String href = "<a href=\""
										+ tok.substring(0, posi) + ".html\">"
										+ tok + "</A>";
								comment = comment + href;
							}
						}
					} // end while
				}
			}
			// Wenn weder label noch name, dann Klasse als Namen verwenden
			if (aName.equals("&nbsp;") && curKey.title.length() > 1) {
				aName = GuiUtil.labelToName(curKey.title);
			}
			if (label.length() == 0 && aName.equals("&nbsp;")) {
				aName = curKey.sKeyword.toLowerCase();
			}
			// Ende Attribute
			if (sKey.equals("Begin Form")) {
				out.println("<H1>" + "Formular: " + label + "</H1>");
				out.println(comment);
				out.println("<BR><img src=\"" + gif + format + "\" alt=\""
						+ gif + format + "\">");
				hasTitle = true;
			} else if (sKey.equals("Begin Dialog")) {
				out.println("<H1>" + "Dialog: " + label + "</H1>");
				out.println(comment);
				out.println("<BR><img src=\"" + gif + format + "\" alt=\""
						+ gif + format + "\">");
				hasTitle = true;
			} else if (sKey.equals("Begin Menu")) {
				if (isTable == true) {
					endTable(out);
					isTable = false;
				}
				out.println("<H2>" + "Menü: " + label + "</H2>");
				out.println(comment);
				hasTitle = false;
			} else if (sKey.equals("Begin Popup")) {
				if (isTable == true) {
					endTable(out);
					isTable = false;
				}
				out.println("<H4>" + "Kontext Menü: " + label + "</H4>");
				out.println(comment);
			} else if (sKey.startsWith("Item") || sKey.equals("Tool")
					|| (sKey.equals("Button") && isToolbar == true)) {
				if (isTable == false) {
					startMenuTable(out);
					isTable = true;
				}
				if (curKey.title.equals("-") == false) {
					out.println(" <TR>");
					if (label.equals("&nbsp;") == false) {
						out.println("   <TD valign=\"top\">" + label + "</TD>");
					} else {
						out.println("   <TD valign=\"top\">" + icon + "</TD>");
					}
					out.println("   <TD valign=\"top\">" + aName + "</TD>");
					out.println("   <TD valign=\"top\">" + ref + "</TD>");
					out.println("   <TD valign=\"top\">" + comment + "</TD>");
					out.println(" </TR>");
				}
			} else if (sKey.equals("Begin Toolbar")) {
				if (isTable == true) {
					endTable(out);
					isTable = false;
				}
				out.println("<H2>" + "Toolbar" + "</H2>");
				out.println(comment);
				hasTitle = false;
				isToolbar = true;
			} else if (sKey.equals("End Toolbar")) {
				if (isTable == true) {
					endTable(out);
					isTable = false;
				}
				isToolbar = false;
			} else if (sKey.equals("End Menu")) {
				if (isTable == true) {
					endTable(out);
					isTable = false;
				}
			} else if (sKey.equals("Begin Tab")) {
				if (isTable == true) {
					endTable(out);
					isTable = false;
				}
				out.println("<H2>" + "Karte: " + label + "</H2>");
				out.println(comment);
				String karte = null;
				if (aName.equals("&nbsp;") == false) {
					karte = aName;
				} else {
					karte = GuiUtil.labelToName(curKey.title);
				}
				karte = karte.substring(0, 1).toUpperCase()
						+ karte.substring(1);

				out.println("<BR><img src=\"" + gif + karte + format
						+ "\" alt=\"" + gif + karte + format + "\">");
				hasTitle = true;
			} else if (sKey.equals("Begin Group")) {
				if (isTable == true) {
					endTable(out);
					isTable = false;
				}
				out.println("<H3>" + "Gruppe: " + label + "</H3>");
				out.println(comment);
				hasTitle = true;
			} else if (sKey.equals("End Group")) {
				if (isTable == true) {
					endTable(out);
					isTable = false;
				}
			} else if (sKey.equals("Begin Table")) {
				if (isTable == true) {
					endTable(out);
					isTable = false;
				}
				out.println("<H3>" + "Tabelle: " + label + "</H3>");
				out.println(comment);
				hasTitle = true;
			} else if (sKey.equals("End Table")) {
				if (isTable == true) {
					endTable(out);
					isTable = false;
				}
			} else if (sKey.equals("Begin Tree")) {
				if (isTable == true) {
					endTable(out);
					isTable = false;
				}
				out.println("<H3>" + "Explorer: " + label + "</H3>");
				out.println(comment);
				hasTitle = true;
			} else if (sKey.equals("End Tree")) {
				if (isTable == true) {
					endTable(out);
					isTable = false;
				}
			} else if (sKey.equals("Use")) {
				// Insert Specification File
				ArrayList<CurrentKeyword> useKeys = null;
				CurrentKeyword useCurKey;

				String useName = curKey.title; // default
				for (CurrentAttrib curAtt: curKey.vAttrib) {
					switch (curAtt.iKeyword.intValue()) {
					case GuiFactory.attFILE:
						useName = curAtt.sValue;
						break;
					}
				}
				String xs = GuiUtil.fileToString(useName);
				try {
					useKeys = XmlReader.makeKeywordListFromString(xs);
				} catch (GDLParseException ex) {
					logger.error(ex.getMessage(), ex);
					ex.printStackTrace();
				}
				if (useKeys != null) {
					/*
					 * Ermittelte Keyword-List einf�gen.
					 */
					int lfdKey = 1;
					for (Iterator<CurrentKeyword> u = useKeys.iterator(); u.hasNext();) {
						useCurKey = u.next();
						keys.add(iKey + lfdKey, useCurKey);
						lfdKey++;
					}
				}
			} else if (sKey.equals("Button")) {
				if (isTable == false) {
					startTable(out);
					isTable = true;
				}
				out.println(" <TR>");
				if (label.equals("&nbsp;") == false) {
					out.println("   <TD valign=\"top\">" + label + "</TD>");
				} else {
					out.println("   <TD valign=\"top\">" + icon + "</TD>");
				}
				out.println("   <TD valign=\"top\">" + sKey + "</TD>");
				out.println("   <TD valign=\"top\">" + aName + "</TD>");
				out.println("   <TD valign=\"top\">" + ref + "</TD>");
				out.println("   <TD colspan=\"3\" valign=\"top\">" + comment
						+ "</TD>");
				out.println(" </TR>");
			} else if (sKey.equals("Text") || sKey.equals("Date")
					|| sKey.equals("Time") || sKey.equals("Money")
					|| sKey.equals("Number") || sKey.equals("Check")
					|| sKey.equals("Option") || sKey.equals("Memo")
					|| sKey.equals("Combo") || sKey.equals("List")
					|| sKey.equals("Text") || sKey.equals("Password")
					|| sKey.equals("Slider") || sKey.equals("Editor")
					|| sKey.equals("Spin")) {
				if (isTable == false) {
					startTable(out);
					isTable = true;
				}
				// Component
				out.println(" <TR>");
				out.println("   <TD valign=\"top\">" + label + "</TD>");
				out.println("   <TD valign=\"top\">" + sKey + "</TD>");
				out.println("   <TD valign=\"top\">" + aName + "</TD>");
				out.println("   <TD valign=\"top\">" + ref + "</TD>");
				out.println("   <TD valign=\"top\">" + dsp + "</TD>");
				out.println("   <TD valign=\"top\">" + nn + "</TD>");
				out.println("   <TD valign=\"top\">" + comment + "</TD>");
				out.println(" </TR>");
			} else if (sKey.equals("Label")) {
				if (isTable == false) {
					startTable(out);
					isTable = true;
				}
				out.println(" <TR>");
				if (label.equals("&nbsp;") == false) {
					out.println("   <TD valign=\"top\">" + label + "</TD>");
				} else {
					out.println("   <TD valign=\"top\">" + icon + "</TD>");
				}
				out.println("   <TD valign=\"top\">" + sKey + "</TD>");
				out.println("   <TD valign=\"top\">" + aName + "</TD>");
				out.println("   <TD valign=\"top\">" + ref + "</TD>");
				out.println("   <TD colspan=\"3\" valign=\"top\">" + comment
						+ "</TD>");
				out.println(" </TR>");
			}
		}
		if (isTable == true) {
			endTable(out);
			isTable = false;
		}
		// SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy 'um:'
		// HH:mm", Locale.GERMANY);
		// Date date = new Date();
		// String sd = format.format(date);
		// out.println("<BR><HR><P ALIGN=Center>");
		// out.println("Generiert am: "+sd);
		out.println("</BODY></HTML>");
		out.flush();
		out.close();
		// System.out.println("HTML Finished");
	}

	private static void startTable(PrintStream out) {
		if (hasTitle == false) {
			out.println("<H2>Attribute</H2>");
		}
		out.println("<table border=1 cellpadding=2 cellspacing=0 width=\"" + breite
						+ "\" >");
		      //+ "\" CELLPADDING=\"1\">");
		out.println(" <tr>");
		out.println("  <th width=\"16%\">Label</th>");
		out.println("  <th width=\"9%\">Typ</th>");
		out.println("  <th width=\"12%\">Name</th>");
		out.println("  <th width=\"12%\">Referenz</th>");
		out.println("  <th width=\"5%\">Anz.</th>");
		out.println("  <th width=\"5%\">mu&szlig;</th>");
		out.println("  <th width=\"41%\">Besonderheit</th>");
		out.println(" </tr>");
	}

	private static void endTable(PrintStream out) {
		out.println("</table>");

	}

	private static void startMenuTable(PrintStream out) {
		out
				.println("<table border=1 cellpadding=2 cellspacing=0 width=\"" + breite
						+ "\" >");
//		  + "\" CELLPADDING=\"1\">");
		out.println(" <TR>");
		out.println("  <TH width=\"16%\">Label</TH>");
		out.println("  <TH width=\"16%\">Name</TH>");
		out.println("  <TH width=\"18%\">Referenz</TH>");
		out.println("  <TH width=\"50%\">Besonderheit</TH>");
		out.println(" </TR>");
	}

	/**
	 * Erzeugt eine Grafik von der Oberfl�che im Format PNG passend zum
	 * HTML-File. <br>
	 * Sind Registerkarten vorhanden, wird auch von allen Tabs eine Grafik
	 * erzeugt.
	 */
	static void createHardcopy(GuiWindow window, String name, String format) {
		int p = name.indexOf(".");
		String fileName = name.substring(0, p) + format;
		String win = name.substring(0, p);
		saveImage(window, fileName, format);
		// Tabsets
		Vector<GuiTabset> tabsets = window.getRootPane().getTabsets();
		if (tabsets != null) {
			for (GuiTabset tabset : tabsets) {
				for (int j = 0; j < tabset.getTabCount(); j++) {
					tabset.setSelectedIndex(j);
					String karteName = tabset.getComponentAt(j).getName();
					// TabViewPort und Buttons weglassen
					if (karteName != null) {
						karteName = karteName.substring(0, 1).toUpperCase()
								+ karteName.substring(1);
						saveImage(window, win + karteName + format, format);
					}
				}
			}
		}
		// Outlook
		GuiOutlookBar obar = window.getOutlookBar();
		if (obar != null) {
			ButtonGroup bgrp = obar.getButtonGroup();
			Enumeration<AbstractButton> benum = bgrp.getElements();
			int cnt = 0;
			while (benum.hasMoreElements()) {
				cnt++;
				AbstractButton bt = benum.nextElement();
				bt.doClick();
				String label = GuiUtil.labelToName(bt.getActionCommand());
				if (label != null && label.length() > 3) {
					label = label.substring(0, 1).toUpperCase()
							+ label.substring(1);
				} else {
					label = "Button" + cnt;
				}
				saveImage(window, win + label + format, format);
			}
		}
	}

	private static void saveImage(GuiWindow window, String name, String format) {
		BufferedImage image = new BufferedImage(window.getComponent()
				.getWidth(), window.getComponent().getHeight(),
				BufferedImage.TYPE_BYTE_INDEXED);
		Graphics pg = image.getGraphics();
		window.getComponent().printAll(pg);
		pg.dispose();
		try {
			File outputFile = new File(GuiUtil.getCurrentDir(), name);
			ImageIO.write(image, format.substring(1), outputFile);
		} catch (Exception je) {
			GuiUtil.showEx(je);
		}
	}
	/**
	 * Setzt erst den UI-Manager (@see GuiUtil#setUiManager(String)) und erzeugt
	 * anschlie�end einen Screenshot mit diesem Look and Feel (@see
	 * GuiDoc#createHardcopyWin(String, String, String, String).
	 *
	 * @param filename
	 *            Dateiname für GUI-Spec
	 * @param documentBase
	 *            Vereichnis für GuiSpec
	 * @param outputDirectory
	 *            Directory für die erzeugte Grafik
	 * @return Dateiname (ohne Pfad) für die erzeugte Grafik, weil das
	 *         Output-Verzeichnis dem Aufrufer der Methode bekannt
	 * @see GuiUtil#setUiManager(String)
	 * @see GuiDoc#createHardcopyWin(String, String, String)
	 */
	public static String createHardcopyWin(String documentBase,
			String filename, String outputDirectory, String uiManager) {
		String ret = null;
		try {
			GuiUtil.setUiManager(uiManager);
		} catch (Throwable ex) {
			// Hier k�nnen lustige Fehler auftreten, wenn das Ger�t auf dem der Screenshot
			// erzeugt werden soll, kein Display hat (z.B. ein Viruteller Server, vglb. mit
			// BugID6604044 - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6604044)
			// Dann spielt der UIManager bei seiner statischen Initialisierung verr�ckt.
			if (logger != null) {
				logger.error("Error: Creating hardcopy for Window in GuiDoc.createHardcopyWin(String documentBase = '"
						+ documentBase.toString()
						+ "', String filename='"
						+ filename.toString()
						+ "', String outputDirectory='"
						+ outputDirectory.toString()
						+ ", String uiManager='"
						+ uiManager.toString()
						+ "') results in Exception: ", ex);
			} else {
				System.err.println("Error: Creating hardcopy for Window in GuiDoc.createHardcopyWin(String documentBase = '"
						+ documentBase.toString()
						+ "', String filename='"
						+ filename.toString()
						+ "', String outputDirectory='"
						+ outputDirectory.toString()
						+ ", String uiManager='"
						+ uiManager.toString()
						+ "') results in Exception: " + ex.getMessage());
				ex.printStackTrace();
			}
		}
		ret = createHardcopyWin(documentBase, filename, outputDirectory);

		return ret;
	}

	/**
	 * Erzeugt eine Hardcopy von dem angegebenen Formular
	 *
	 * @param filename
	 *            Dateiname für GUI-Spec
	 * @param documentBase
	 *            Vereichnis für GuiSpec
	 * @param outputDirectory
	 *            Directory für die erzeugte Grafik
	 * @return Dateiname (ohne Pfad) für die erzeugte Grafik, weil das
	 *         Output-Verzeichnis dem Aufrufer der Methode bekannt
	 */
	public static String createHardcopyWin(String documentBase,
			String filename, String outputDirectory) {
		String ret = null;
		String fn = null;
		try {
			// GuiBuilder wird als Laufzeitumgebung verwendet und soll
			// Eindeutigkeit von Namen prüfen
			// TODO
			// Wird diese Methode aufgerufen, vergisst der GuiBuilder pl�tzlich
			// die in Keyword.properties zus�tzlich definierten Keywords. Keine
			// Ahnung, warum.
			// GuiUtil.setAPI(true);

			// Weil es sch�ner ist ...
			// TODO
			// Erfordert das entsprechende JAR im Classpath. JAR ist aber
			// wahrscheinlich GPL und
			// kann daher nicht standardm��ig mitgeliefert werden.
			// GuiUtil.setUiManager(new String("plastic"));

			// Zu fotographierendes Fenster erzeugen
			GuiWindow win = createWindow(documentBase, filename);

			// win kann null sein, wenn es probleme mit der Initialisierung des GuiBuilders
			// oder mit dem parsen des Dokumentes gab, deshalb hier prüfen, um im Folgenden 
			// NullPointerException zu verhindern
			if (win != null) {
				
				// Modale Fenster bleiben nach dem Erzeugen ge�ffnet und m�ssten
				// vom Benutzer manuell geschlossen werden. Deshalb werden alle
				// Fenster hier auf nicht-modal gesetzt.
				if (win.isModal()) {
					win.setModal(false);
				}
	
				// Fenster anzeigen
				win.show();
	
				// Dateinamen ermitteln
				// Standardfall ist, dass der Name des Fensters genommen wird,
				// aber nicht, wenn es ein DummyDialog eines wiederverwendeten
				// Fensters ist
				if (win.isDummyDialog()) {
					// TODO
					// Diese L�sung hier liefert insgesamt übersichtlichere
					// Ergebnisse, bei vielen Screenshot-Dateien in einem
					// Verzeichnis. Daher w�re es besser den Dateinamen
					// immer so zu erzeugen.
					int p = filename.lastIndexOf(".");
					if (p != -1) {
						fn = filename.substring(0, p);
					} else {
						fn = filename;
					}
				} else {
					fn = win.getName();
				}
				// Screenshot fotographieren
				ret = createImage(win, outputDirectory, fn);
	
				// Verzeichnistrennung normalisieren
				ret = GuiUtil.replaceFileSeparator(ret);
	
				// Als R�ckgabewert wird der Dateiname ohne Pfad zurück gegeben,
				// weil
				// dem Aurufer der Methode der Pfad des Output-Verzeichnisses
				// bekannt ist
				// (es wurde als Parameter übergeben).
				// An der Implementierung von createImage() sollte man aber besser
				// nichts
				// �ndern, deshalb wird hier aus dem asoluten Pfad, der von
				// createImage()
				// zurückgegeben wird, der Dateiname über String-Operationen
				// extrahiert.
				String file = ret.substring(ret.lastIndexOf("/") + 1);
				ret = file;
	
				// Das Wegwerfen des Fensters funktioniert implizit nicht
				// zuverl�ssig und wird daher hier explizt angesto�en.
				// Die Methode createImage hat den unsch�nen Seiteneffekt, dass sie
				// bereits Dispose aufruft, deshalb hier nochmals prüfen ob win nicht 
				// bereits null
				if (win != null) {
					win.dispose();
					win = null;
				}
			} 				

		} catch (Exception ex) {
			System.err
					.println("Error: Creating hardcopy for Window in GuiDoc.createHardcopyWin(String documentBase = '"
							+ documentBase.toString()
							+ "', String filename='"
							+ filename.toString()
							+ "', String outputDirectory='"
							+ outputDirectory.toString()
							+ "') results in Exception: " + ex.getMessage());
		}

		return ret;
	}

	/**
	 * Erzeugt einen Screenshot mit aktivierter Registerkarte. Es gibt eine
	 * neuere und universelle Methode, die nicht nur Registerkarten, sondern
	 * auch BarButtons verarbeiten kann.
	 *
	 * @see #createHardcopyForComponent(String, String, String, String)
	 * @deprecated
	 * @param documentBase
	 * @param filename
	 * @param outputDirectory
	 * @param componentToShow
	 * @return Dateinamen der Bildatei
	 */
	public static String createHardcopyTab(String documentBase,
			String filename, String outputDirectory, String componentToShow) {
		return createHardcopyForComponent(documentBase, filename,
				outputDirectory, componentToShow);
	}

	/**
	 * Setzt erst den UI-Manager (@see GuiUtil#setUiManager(String)) und erzeugt
	 * anschlie�end einen Screenshot mit diesem Look and Feel (@see
	 * GuiDoc#createHardcopyForComponent(String, String, String, String).
	 *
	 *
	 * @param documentBase
	 * @see #createHardcopyForComponent(String, String, String, String)
	 * @param filename
	 * @see #createHardcopyForComponent(String, String, String, String)
	 * @param outputDirectory
	 * @see #createHardcopyForComponent(String, String, String, String)
	 * @param componentToShow
	 * @see #createHardcopyForComponent(String, String, String, String)
	 * @param uiManager
	 * @see GuiUtil#setUiManager(String)
	 * @return
	 * @see #createHardcopyForComponent(String, String, String, String)
	 */
	public static String createHardcopyForComponent(String documentBase,
			String filename, String outputDirectory, String componentToShow,
			String uiManager) {
		String ret = null;
		
		try {
			GuiUtil.setUiManager(uiManager);
		} catch (Throwable ex) {
			// Hier k�nnen lustige Fehler auftreten, wenn das Ger�t auf dem der Screenshot
			// erzeugt werden soll, kein Display hat (z.B. ein Viruteller Server, vglb. mit
			// BugID6604044 - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6604044)
			// Dann spielt der UIManager bei seiner statischen Initialisierung verr�ckt.
			if (logger != null) {
				logger.error("Error: Creating hardcopy for Window in GuiDoc.createHardcopyForComponent(String documentBase = '"
						+ documentBase.toString()
						+ "', String filename='"
						+ filename.toString()
						+ "', String outputDirectory='"
						+ outputDirectory.toString()
						+ ", String componentToShow='"
						+ componentToShow.toString()
						+ "', String uiManager='"
						+ uiManager.toString()
						+ "') results in Exception: ", ex);
			} else {
				System.err.println("Error: Creating hardcopy for Window in GuiDoc.createHardcopyForComponent(String documentBase = '"
						+ documentBase.toString()
						+ "', String filename='"
						+ filename.toString()
						+ "', String outputDirectory='"
						+ outputDirectory.toString()
						+ ", String componentToShow='"
						+ componentToShow.toString()
						+ "', String uiManager='"
						+ uiManager.toString()
						+ "') results in Exception: " + ex.getMessage());
				ex.printStackTrace();
			}
		}
		
		ret = createHardcopyForComponent(documentBase, filename,
				outputDirectory, componentToShow);
		return ret;
	}

	/**
	 * Erzeugt eine Hardcopy von dem angegebenen Formular/Dialog mit aktivierter
	 * Registerkarte bzw. aktiviertem BarButton.<BR>
	 * <BR>
	 * Beispiele für die Pfadangabe zur Komponente im Parameter <CODE>String
	 * componentToShow</CODE>:
	 * <UL>
	 * <LI> <CODE>"GDL.@Form.frmTest@Panel#1@Tabset#1@Tab.tab_b6"</CODE> zeigt
	 * das Tab mit dem name="tab_b6" auf dem zweiten Tabset (<Code>#1</Code>)
	 * des zweiten Panels (<Code>#1</Code>) des Form mit dem name="frmTest"
	 * an. (Hinweis: Es wird bei 0 begonnen zu z�hlen.)
	 *
	 * <LI><CODE>"GDL.@Form.@Panel#1@Tabset#0@Tab#0"</CODE> zeigt die erste
	 * Registerkarte auf dem ersten Tabset des zweiten Panels im ersten
	 * gefundenen Form an.
	 *
	 * <LI><CODE>"GDL.@Form.frmTest@Panel.pnlOben@Tabset#1@Tab.tab_b6"</CODE>
	 * zeigt die Registerkarte mit dem name="tab_b6" des zweiten Tabset auf dem
	 * Panel mit dem name="pnlOben" im Dialog mit dem Namen "frmTest" an.
	 * </UL>
	 * <U>Hinweis:</U> Der Name des GDL und des Form spielen bei der momentanen
	 * Implementierung (noch) keine Rolle, da es jeweils nur einmal vorkommen
	 * kann. Es wird also unabh�ngig vom name oder einer Position/Index-Angabe
	 * immer das erste gefundene verwendet. <BR>
	 * für die Verwendung in einer XSLT ist folgender Aufrufcode notwendig,
	 * wobei die Variablen entsprechend vorher gesetzt werden m�ssen:<BR>
	 *
	 * <PRE><CODE> &lt;xsl:value-of
	 * select="guibuilder:createHardcopyWin($current-dir, $current-filename,
	 * $screenshot-target-dir)"
	 * xmlns:guibuilder="java:de.guibuilder.design.GuiDoc"/&gt; </CODE></PRE>
	 *
	 * Nat�rlich m�ssen beim Aufruf aus einer XSLT die GuiBuilder-JARs im
	 * Classpath eingebunden sein. für Saxon beispielsweise so:<BR>
	 *
	 * <PRE><CODE> java -cp
	 * lib\guibuilder.jar;lib\icons.zip;lib\saxon8.jar;lib\log4j-1.2.8.jar;lib\GLUE.jar;lib\jdataset.jar;
	 * lib\JCalendar.jar;lib\l2fprod-common-all.jar net.sf.saxon.Transform -o
	 * output-file.html input-file.xml stylesheet.xsl </CODE></PRE>
	 *
	 * Um den Parameter $full-path wird die Komponente identifiziert die im
	 * Formular/Dialog angezeigt werden soll, wenn der Screenshot gemacht wird
	 * (z.B. welche Registerkarte angeklickt ist) Aus einer XSLT kann diese
	 * Variabel wie folgt zusammengesetzt werden:<BR>
	 *
	 * <PRE><CODE> &lt;xsl:variable name="full-path"&gt; &lt;xsl:for-each
	 * select="ancestor::*"&gt; &lt;xsl:value-of select="local-name()" /&gt;
	 * &lt;xsl:if test="string-length(@name)&gt;0"&gt;
	 * &lt;xsl:text&gt;.&lt;/xsl:text&gt; &lt;xsl:value-of select="@name"/&gt;
	 * &lt;/xsl:if&gt; &lt;xsl:if test="string-length(@name)=0"&gt;
	 * &lt;xsl:text&gt;#&lt;/xsl:text&gt; &lt;xsl:value-of
	 * select="count(preceding-sibling::*[name(current()) = name()])"/&gt;
	 * &lt;/xsl:if&gt; &lt;xsl:text&gt;@&lt;/xsl:text&gt; &lt;/xsl:for-each&gt;
	 *
	 * &lt;xsl:value-of select="local-name(.)"/&gt;
	 *
	 * &lt;xsl:if test="string-length(@name)&gt;0"&gt;
	 * &lt;xsl:text&gt;.&lt;/xsl:text&gt; &lt;xsl:value-of select="@name"/&gt;
	 * &lt;/xsl:if&gt; &lt;xsl:if test="string-length(@name)=0"&gt;
	 * &lt;xsl:text&gt;#&lt;/xsl:text&gt; &lt;xsl:value-of
	 * select="count(preceding-sibling::*[name(current()) = name()])"/&gt;
	 * &lt;/xsl:if&gt; &lt;/xsl:variable&gt; </CODE></PRE>
	 *
	 * <BR>
	 * <BR>
	 *
	 * @param filename
	 *            Dateiname für GUI-Spec
	 * @param documentBase
	 *            Vereichnis für GuiSpec
	 * @param outputDirectory
	 *            Directory für die erzeugte Grafik
	 * @param componentToShow
	 *            Pfad zu der anzuzeigenden Komponente im Format:<BR>
	 *            GDL.@lt;Tag&gt;.&lt;name&gt;@lt;Tag&gt;.&lt;name&gt;@... oder<BR>
	 *            GDL.@lt;Tag&gt;#&lt;index&gt;.@lt;Tag&gt;#&lt;index&gt;@...
	 *            oder<BR>
	 *            GDL.@lt;Tag&gt;.&lt;name&gt;@lt;Tag&gt;#&lt;index&gt; oder
	 *            <BR>
	 *            null, wenn das Fenster abfotografiert werden soll.
	 * @return Dateiname (ohne Pfadangabe) für die erzeugte Grafik
	 *
	 */

	public static String createHardcopyForComponent(String documentBase,
			String filename, String outputDirectory, String componentToShow) {
		String ret = null;
		String imagefn = null;

		try {

			GuiWindow win = createWindow(documentBase, filename);

			// Dateinamen ermitteln
			// Standardfall ist, dass der Name des Fensters genommen wird,
			// aber nicht, wenn es ein DummyDialog eines wiederverwendeten
			// Fensters ist
			if (win.isDummyDialog()) {
				// TODO
				// Diese L�sung hier liefert insgesamt übersichtlichere
				// Ergebnisse, bei vielen Screenshot-Dateien in einem
				// Verzeichnis. Daher w�re es besser den Dateinamen
				// immer so zu erzeugen.
				int p = filename.lastIndexOf(".");
				if (p != -1) {
					imagefn = filename.substring(0, p);
				} else {
					imagefn = filename;
				}
			} else {
				imagefn = win.getName();
			}

			// Modale Fenster bleiben nach dem Erzeugen ge�ffnet und m�ssten
			// vom Benutzer manuell geschlossen werden. Deshalb werden alle
			// Fenster hier auf nicht-modal gesetzt.
			if (win.isModal()) {
				win.setModal(false);
			}

			// Anzeigen des Fensters
			win.show();

			// Aufbl�ttern und anklicken der zu zeigenden Komponente
			imagefn = showComponent(win, componentToShow, imagefn);

			// An dieser Stelle ist jetzt die richtige Registerkarte
			// aufgebl�ttert.
			// Es kann der Screenshot erzeugt werden.

			ret = createImage(win, outputDirectory, imagefn);

			// Verzeichnistrennung normalisieren
			ret = GuiUtil.replaceFileSeparator(ret);

			// Als R�ckgabewert wird der Dateiname ohne Pfad zurück gegeben,
			// weil dem Aurufer der Methode der Pfad des Output-Verzeichnisses
			// bekannt ist (es wurde als Parameter übergeben).
			// An der Implementierung von createImage() sollte man aber besser
			// nichts �ndern, deshalb wird hier aus dem asoluten Pfad, der von
			// createImage() zurückgegeben wird, der Dateiname über
			// String-Operationen
			// extrahiert.
			String file = ret.substring(ret.lastIndexOf("/") + 1);
			ret = file;

			// Das Wegwerfen des Fensters funktioniert implizit nicht
			// zuverl�ssig und wird daher hier explizt angesto�en.
			// Die Methode createImage hat den unsch�nen Seiteneffekt, dass sie
			// bereits Dispose aufruft.
			if (win != null) {
				win.dispose();
				win = null;
			}

		} catch (Exception ex) {
			System.err
					.println("Error: Creating hardcopy for Component in GuiDoc.createHardcopyForComponent(String documentBase = '"
							+ documentBase.toString()
							+ "', String filename='"
							+ filename.toString()
							+ "', String outputDirectory='"
							+ outputDirectory.toString()
							+ "', String componentToShow='"
							+ componentToShow.toString()
							+ "') results in Exception: " + ex.getMessage());

		}
		return ret;
	}

	/**
	 * Erzeugt einen Screenshot mit aktivierter Registerkarte. Es gibt eine
	 * neuere und universelle Methode, die nicht nur Registerkarten, sondern
	 * auch BarButtons verarbeiten kann.
	 *
	 * @see #createHardcopyForComponent(String, String, String, String)
	 * @deprecated
	 * @param documentBase
	 * @param filename
	 * @param outputDirectory
	 * @param componentToShow
	 * @return Dateinamen der Bildatei
	 */
	public static String createHardcopyOBB(String documentBase,
			String filename, String outputDirectory, String componentToShow) {
		return createHardcopyForComponent(documentBase, filename,
				outputDirectory, componentToShow);
		// Alter Code:
		/*
		 * String ret = null; try { GuiWindow win = createWindow(documentBase,
		 * filename); win.show(); GuiOutlookBar obar = win.getOutlookBar();
		 * ButtonGroup bgrp = obar.getButtonGroup(); Enumeration<AbstractButton>
		 * benum = bgrp.getElements(); int cnt = 0;
		 * while(benum.hasMoreElements()) { cnt++; AbstractButton bt =
		 * benum.nextElement(); if
		 * (bt.getActionCommand().equalsIgnoreCase(componentToShow)) {
		 * bt.doClick(); String fn = win.getName() + "_obb_" +
		 * bt.getActionCommand(); ret = createImage(win, outputDirectory, fn); } } }
		 * catch (Exception ex) { logger.error("Error creating hardcopy", ex); }
		 * return ret;
		 */
	}

	private static GuiWindow createWindow(String documentBase, String filename)
			throws Exception {
		GuiUtil.setDocumentBase(new URL("file", null, 0, documentBase));
		GuiWindow win = null;
		GuiFactory fac = null;
		try {
			fac = GuiFactory.getInstance();
			if (fac != null) {
				win = fac.createWindow(filename);
			}
		} catch (Exception ex) {
			logger.debug("GuiDoc#createWindow(String documentBase='" + documentBase.toString()
					+ "', String filename='" + filename.toString()
					+ "') falied with exception: ", ex);
			System.err.println("GuiDoc#createWindow(String documentBase='" + documentBase.toString()
					+ "', String filename='" + filename.toString()
					+ "') falied with exception: " + ex.getMessage());
		}
		return win;
	}

	/**
	 *
	 * @param win
	 * @param componentToShow
	 * @return Gibt den Dateinamen der Bilddatei zurück
	 */
	private static String showComponent(GuiWindow win, String componentToShow,
			String imagefn) {
		String currentSubComponentToShow = null;
		String name = null;
		String kind = null;
		Integer index = null;
		int p = componentToShow.indexOf("@");
		int p2;
		boolean showByName = false;
		currentSubComponentToShow = componentToShow;
		GuiContainer currentGuiContainer = null;
		GuiTabset tabset = null;
		GuiButtonBar bbr = null;
		GuiOutlookBar obr = null;
		JPanel obrt = null;
		while (p != -1) {
			// Aktuelle Komponente ermitteln
			currentSubComponentToShow = componentToShow.substring(0, p);

			if (currentSubComponentToShow.contains(".")) {
				p2 = currentSubComponentToShow.indexOf(".");
				name = currentSubComponentToShow.substring(p2 + 1);
				kind = currentSubComponentToShow.substring(0, p2);
				showByName = true;
			} else if (currentSubComponentToShow.contains("#")) {
				p2 = currentSubComponentToShow.indexOf("#");
				kind = currentSubComponentToShow.substring(0, p2);
				index = new Integer(currentSubComponentToShow.substring(p2 + 1));
				showByName = false;
			} else {
				System.out.println("Error: Resolving Path to component.");
				System.err
						.println("       Neither Kind and Name of Subcomponent '"
								+ currentSubComponentToShow
								+ "' are not seperated by Char '.'");
				System.err
						.println("       nor Kind and PositionNr are seperated by Char '#'!");
				return imagefn;
			}

			if (kind.equalsIgnoreCase("GDL")) {
				// Irgendwo muss man ja anfangen: Hier beim Main-Panel.
				currentGuiContainer = win.getRootPane().getMainPanel();

			} else if (kind.equalsIgnoreCase("Panel")) {
				// Ein Panel kann nur in einem GuiContainer liegen, deshalb
				// funktioniert der Aufruf hier
				if (showByName) {
					currentGuiContainer = currentGuiContainer
							.getContainer(name);
				} else {
					currentGuiContainer = currentGuiContainer.getContainer(
							kind, index);
					if (currentGuiContainer == null) {
						System.err
								.println("Error: Getting GuiContainer of Typ '"
										+ kind + "' at Position '"
										+ index.toString()
										+ "' failed in GuiDoc#showComponent()!");
						return null;
					}
				}

			} else if (kind.equalsIgnoreCase("Group")) {
				// Eine Group kann ebenfalls nur in einem Container liegen,
				// deshalb kann so aufgerufen werden
				if (showByName) {
					currentGuiContainer = currentGuiContainer
							.getContainer(name);
				} else {
					currentGuiContainer = currentGuiContainer.getContainer(
							kind, index);
					if (currentGuiContainer == null) {
						System.err
								.println("Error: Getting GuiContainer of Typ '"
										+ kind + "' at Position '"
										+ index.toString()
										+ "' failed in GuiDoc#showComponent()!");
						return null;
					}
				}

			} else if (kind.equalsIgnoreCase("ButtonBar")) {
				bbr = currentGuiContainer.getButtonBarFromComponents();

			} else if (kind.equalsIgnoreCase("ButtonBarButton")
					&& (bbr != null)) {
				AbstractButton bbrbtn = null;
				if (showByName) {
					bbrbtn = bbr.getButtonByName(name);
					// Noch den Dateinamen des Screenshots um das Tab erg�nzen
					// Wenn das Tab einen Namen hat, wird dieser verwendet
					imagefn = imagefn + "_" + bbrbtn.getName();
					// besser ist evtl.
					// imagefn = imagefn + "_" + bbrbtn.getActionCommand();
					if (bbrbtn == null) {
						System.out
								.println("Error: Resolving Path to component. (In GuiDoc#showComponent() no Button with '"
										+ name.toString()
										+ "' found in ButtonBar.");
						return null;
					}
				} else {
					bbrbtn = bbr.getButton(index);
					// Noch den Dateinamen des Screenshots um das Tab erg�nzen
					// wenn es keinen Namen hat, dann seine Position im
					// Registerkartensatz
					imagefn = imagefn + "_tab" + bbr.getButtonIndex(name);
					if (bbrbtn == null) {
						System.out
								.println("Error: Resolving Path to component. (In GuiDoc#showComponent() no Button at position'"
										+ index.toString()
										+ "' found in ButtonBar.");
						return null;
					}
				}

				// für den Fall, dass der Button standardm��ig deaktiviert ist
				// (do="true"
				// muss es hier zun�chst aktiviert werden. (Sicherheitshalber
				// wird
				// das für alle Tabs gemacht.)
				bbrbtn.setEnabled(true);
				// Button anlicken (ist einfacher als über setSelected() zu
				// gehen, weil dann
				// sonst auch noch das anzuzeigende Panels geladen werden m�sste
				// ...
				bbrbtn.doClick();
				// ... und mit dem zugeh�rigen Panel als Container weitermachen.
				// (K�nnte ja weitere
				// Unterregisterkarten enthalten.)
				// currentGuiContainer = (GuiContainer)tab;
				currentGuiContainer = bbr.getCurrentRightPanel();

			} else if (kind.equalsIgnoreCase("OutlookBar")) {
				// ganz einfach den OutlookBar besorgen und für die nachfolgende
				// Bearbeitung des Pfad merken
				obr = currentGuiContainer.getOutlookBarFromComponents();

			} else if (kind.equalsIgnoreCase("OutlookBarTab") && (obr != null)) {
				// prüfen, ob anhand des Namen oder eines Index das
				// OutlookBarTab ausgew�hlt
				// werden soll
				if (showByName) {
					// wenn anhand des Namen, dann das Tab über den Namen
					// besorgen
					obrt = obr.getTab(name);
					// den Dateinamen der auszugebenden Screenshot-Datei um den
					// Tab erg�nzen
					imagefn = imagefn + "_" + name.toString();
					// den Tab "anklicken", damit er uns seine Buttons im
					// Screenshot sichtbar sind
					obr.setSelectedIndexByName(name);
				} else {
					// wenn anhand der Position des Tab im XML (index), dann das
					// Tab über den index besorgen
					obrt = obr.getTab(index);
					// den Dateinamen der auszugebenden Screenshot-Datei um den
					// Tab erg�nzen, andernfalls w�rde sich Buttons mit gleichem
					// Inhalt aber in unterschiedlichen Tabs gegenseitig die
					// Screenshots
					// überschreiben
					imagefn = imagefn + "_obrt" + index.toString();
					// den Tab "anklicken", damit er uns seine Buttons im
					// Screenshot sichtbar sind
					obr.setSelectedIndex(index);
				}

			} else if (kind.equalsIgnoreCase("OutlookBarButton")
					&& (obrt != null)) {
				AbstractButton obbtn = null;
				if (showByName) {
					obbtn = obr.getButtonByName(name);
					imagefn = imagefn + "_" + name.toString();
					if (obbtn == null) {
						System.out
								.println("Error: Resolving Path to component. (In GuiDoc#showComponent() no Button with '"
										+ name.toString()
										+ "' found in ButtonBar.");
						return null;
					}
				} else {
					obbtn = obr.getButton(obrt, index);
					imagefn = imagefn + "_obrbtn" + index.toString();
					if (obbtn == null) {
						System.out
								.println("Error: Resolving Path to component. (In GuiDoc#showComponent() no Button at position'"
										+ index.toString()
										+ "' found in ButtonBar.");
						return null;
					}
				}

				// für den Fall, dass der Button standardm��ig deaktiviert ist
				// (do="true") muss es hier zun�chst aktiviert werden.
				// (Sicherheitshalber
				// wird das für alle Tabs gemacht.)
				obbtn.setEnabled(true);
				// Button anlicken (ist einfacher als über setSelected() zu
				// gehen, weil dann sonst auch noch das anzuzeigende Panels
				// geladen werden m�sste ...
				obbtn.doClick();
				// ... und mit dem zugeh�rigen Panel, das durch den Klick
				// zum currentRightPanel wurde, als Container weitermachen.
				// (K�nnte ja weitere Unterregisterkarten enthalten.)
				currentGuiContainer = obr.getCurrentRightPanel();
				// Noch den Dateinamen des Screenshots um das Tab erg�nzen
			} else if (kind.equalsIgnoreCase("Tabset")) {
				// Entgegen der Erwartung ist ein Tabset selbst kein
				// GuiContainer und auch
				// keine GuiComponent. Deshalb wird hier über die Methode
				// GuiContainer.getGuiTabsetFromComponents()
				// zugegriffen.
				if (showByName) {
					tabset = currentGuiContainer
							.getGuiTabsetFromComponents(name);
				} else {
					tabset = currentGuiContainer
							.getGuiTabsetFromComponents(index);
				}
			} else if (kind.equalsIgnoreCase("Tab") && tabset != null) {
				GuiTab tab = null;

				if (showByName) {
					// Tabset per Namen besorgen
					tab = tabset.getTab(name);
					// für den Fall, dass das Tab standardm��ig deaktiviert ist
					// (do="true"
					// muss es hier zun�chst aktiviert werden.
					// (Sicherheitshalber wird
					// das für alle Tabs gemacht.)
					tabset.setEnabledAt(tab.getTabIndex(), true);
					// Registerkarte aufbl�ttern...
					tab.setSelected();
					// Noch den Dateinamen des Screenshots um das Tab
					// erg�nzen...
					// ... hier um den Namen des Tab.
					imagefn = imagefn + "_" + tab.getName();
				} else {
					tab = tabset.getTab(index);
					tabset.setEnabledAt(index, true);
					tabset.setSelectedIndex(index);
					// Noch den Dateinamen des Screenshots um das Tab erg�nzen
					// wenn es keinen Namen hat, dann seine Position im
					// Registerkartensatz
					imagefn = imagefn + "_tab" + tab.getTabIndex();
				}
				// ... und mit dieser Karte als Container weitermachen. (K�nnte
				// ja weitere
				// Unterregisterkarten enthalten.)
				currentGuiContainer = tab;

			}

			// prüfen, ob am Ende des Pfad-Strings angekommen
			if ((p + 1) < componentToShow.length()) {
				componentToShow = componentToShow.substring(p + 1);
				p = componentToShow.indexOf("@");
				if (p != -1) {
					// Es ist ein @-Zeichen enthlaten. Deshalb wir nur der Teil
					// bis zum n�chten
					// @-Zeichen betrachtet
					currentSubComponentToShow = componentToShow.substring(0, p);
				} else {
					// Kein @-Zeichen enthalten bedeutet, dass der Pfad keine
					// weiteren Elemente mehr hat
					// und deshalb komplett verarbeitet werden kann
					currentSubComponentToShow = componentToShow;
					p = componentToShow.length();
				}
			} else {
				// Wenn am Ende angekommen, dann While-Schleife verlassen
				p = -1;
			}

		}
		return imagefn;
	}

	private static String createImage(GuiWindow window, String directory,
			String filename) {
		BufferedImage image = new BufferedImage(window.getComponent()
				.getWidth(), window.getComponent().getHeight(),
				BufferedImage.TYPE_BYTE_INDEXED);
		Graphics pg = image.getGraphics();
		window.getComponent().printAll(pg);
		pg.dispose();
		try {
			File outputFile = new File(directory, filename + "."
					+ defaultFormat);
			ImageIO.write(image, defaultFormat, outputFile);
			// TODO
			// Den unsch�nen Seiteneffekt, dass die Methode dispose() auruft
			// sollte man bei Gelegenheit überdenken, denn sie bekommt ja ein
			// Fenster
			// übergeben und darf das dann nicht einfach zerst�ren. Dadurch
			// werden
			// die Referenzen des Aufrufers ungültig.
			window.dispose();
			window = null;
			return outputFile.getAbsolutePath();
		} catch (Exception ex) {
			logger.error("Error creating grafic", ex);
			return null;
		}
	}

}