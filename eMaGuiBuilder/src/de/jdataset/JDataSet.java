package de.jdataset;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import de.pkjs.util.Convert;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
/**
 * Ein DataSet ist ein generisches Transfermodell für verteilte
 * Anwendungen.<p>
 * Üblicherweise wird ein DataSet von einem Persistenz-Layer
 * erzeugt und an den Client übertragen.<br>
 * Der Client nimmt Änderungen, Ergänzungen an den Daten vor und
 * schickt die so veränderte Struktur zurück an den Persistenz-Layer.<br>
 * Dort werden die Informationen gemäß den vorgenommenen Änderungen
 * in die Datenbank geschrieben, bzw. aus ihr gelöscht.<br>
 * Die Oberfläche des Client sollte ein Binding an dieses Modell
 * ermöglichen.<p>
 * Ein DataSet umfaßt sowohl Informationen über die Datenstruktur 
 * (Tabellen, Relationen, Columns) 
 * als auch die Daten selbst in Form von Zeilen und der Werte.<p>
 * <h3>Metadaten</h3>
 * Ein DataSet enthält genau ein Wurzel-Tabelle. 
 * [TODO: hier wär eine Erweiterung auf beliebig viele Wurzel-Tabllen wünschenswert].
 * Ein Tabelle (JDataTable) hält zur Information über ihre
 * Struktur eine Liste von JDataColums.
 * Jede Tabelle kann gemäß der Datenstruktur Child- und Parent-Tables halten.<p>
 * Gemäß dieser Datenstruktur nehmen die Tabellen in einem DataSet
 * verschiedene Rollen ein:
 * <ul>
 * <li>Root Table<br>
 * Sie ist dem DataSet direkt zugeordnet und kann ihrerseits
 * Child und Parent Tables halten.
 * <li>Child Table<br>
 * Ein Child Table kann auch Child und Parent Tables halten.
 * <li>Parent Table<br>
 * Eine Parent Table kann keine Child Tables halten, sondern 
 * ausschließlich weitere Parent Tables.
 * </ul>
 * <h3>Daten</h3>
 * Ähnlich der definierten Tabellenstruktur hält der DataSet eine
 * Liste von Tabellen-Zeilen (JDataRow). Diese Tabellenzeilen
 * können Child- und Parent Rows halten; gemäß der definierten
 * Tabellen-Struktur.<br>
 * Eine DataRows besteht wiederum für jede definierte Spalte
 * aus einer entsprechenden Menge von Werten (JDataValue).<p>
 * Gemäß der definierten DatenStruktur nimmt eine DataRow eine
 * die entsprechende Rolle ein:
 * <ul>
 * <li>Root Row<br>Sie ist dem DataSet direkt zugeordnet; sie kann 
 * Child und Parent Rows haben. Zu beachten ist, daß Child Rows
 * als Menge auftreten können, Parent Rows aber immer eine
 * einzelne Zeile darstellen (ansonsten wäre die Datenstruktur
 * fehlerhaft). 
 * <li>Child Row<br>Sie können Child und Parent Rows  halten.
 * <li>Parent Row<br>Natürlich können Parent Rows 
 * keine Child Rows halten, sondern nur weitere Parent Rows.
 * </ul>
 * Dieser DataSet kann als XML-Document serialisiert und wieder
 * erzeugt werden.<p>
 * Package Diagram<br>
 * <img src="PackageDiagram.jpg"><br>
 * Class Diagram<br>
 * <img src="DatasetClassDiagram.jpg">
 * <h3>Zugriff auf Daten über Pfad-Notation</h3>
 * Das "Druchhangeln" über die Child und Parent Tables zu dem gewünschten
 * Feld kann umständlich und unübersichtlich werden. Zu diesem
 * Zweck gibt es einige Methoden, die über eine Path-Notation
 * die gewünschten Informationen liefern.<p>
 * Hierbei wird der Pfad zu dem gewünschten Objekt über
 * die Child und Parent Referenzen gebildet, durch einen
 * Delimiter getrennt.<br>
 * Beispiel:<br>
 * <code>adresse.person#funktion@bezeichnung</code><br>
 * Es wird die Column "bezeichnung" aus der Tabelle "funktion"
 * geliefert, wobei "funktion" eine Parent Table von "person" ist
 * und "person eine Child Table von "adresse".<p>
 * Ein "." liefert also eine Child Table,<br>
 * ein "#" eine Parent Table<br>
 * und "@" am Ende den Feldnamen.<p>
 * "." und "#" dürfen dabei beliebig häufig vorkommen,
 * "@" naturgemäß nur einmal am Ende, wobei "@" auf weggelassen
 * werden kann, wenn auf eine Table oder Row zugegriffen werden 
 * soll.<p>
 * Die Notation unterscheidet sich, wenn auf die Struktur
 * des Dataset oder seine Daten zugegriffen wird:<br>
 * Child Rows können ihrer Natur nach mehrfach vorhanden sein;
 * um auf sie zuzugreifen muß ggf. ihr Index angegeben werden:<br>
 * <code>adresse[4].person[15]#funktion@bezeichnung</code><br>
 * Die Angaben "[n]" sind 0-relativ. Wird kein Index
 * angegeben, wird "0" angenommen.
 * <h3>Getter und Setter</h3>
 * Auch wenn intern alle Daten als String gespeichert werden,
 * gibt es eine Reihe von Methoden die den Umgang mit Objekten
 * und Primitiv-Daten-Typen erleichern.<p>
 * Beim Datentyp Object ist zu beachten, dass dieses dem Datentyp
 * der Column entsprechen muß.
 * @see #getDataTablePath
 * @see #getDataColumnPath
 * @see #getDataRowPath
 * @see #getValuePath
 * @see #setValuePath
 */
public final class JDataSet implements Serializable, RowContainer {
   private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JDataSet.class);
	// Attributes
	private String datasetName;
	private String databaseName;
	private String username;
	private String warning;
	private long oid;
	private boolean readonly;
	private ArrayList<JDataTable> dataTables;
	private ArrayList<JDataRow> dataRows;
	private boolean verified;
	private ArrayList<ProfileEntry> profiler;
	
	static final long serialVersionUID = -5502569999774420366L;

	// Constructors
	/**
	 * @deprecated
 	 * For serialization purpose only.
 	 */
	public JDataSet() {}
	/**
	 * Erzeugt einen leeren DataSet.
	 * @param Der (beliebige) Name des DataSet
	 */
	public JDataSet(String name) {
		this.datasetName = name;
	}
	/**
	 * Erzeugt einen DataSet aus einem XML-Document.<p>
	 * @see #getXml
	 * Auf diese Art kann ein DataSet komplett geklont werden:<br>
	 * JDataSet clone = new JDataSet(myDataSet.getXml());
	 * @param doc
	 */
	public JDataSet(Document doc) {
		this.create(doc);
	}
	// Methods
	/**
	 * @deprecated
 	 * For serialization purpose only.
 	 */
	public void create(String s) {
		try {
			Document doc = new Document(s);
			this.create(doc);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	/**
	 * @deprecated
 	 * For serialization purpose only.
 	 */
	public void create(Document doc) {
		this.dataTables = null;
		this.dataRows = null;
		this.verified = false;
		
		Element root = doc.getRoot();
		this.datasetName = root.getAttribute("name");
		this.databaseName = root.getAttribute("DatabaseName");
		this.readonly = Convert.toBoolean(root.getAttribute("readonly"));
		// Schema
		Element schemaEle = root.getElement("Schema");
		if (schemaEle != null) {
			Elements schemaEles = schemaEle.getElements();
			while (schemaEles.hasMoreElements()) {
				Element tblEle = schemaEles.next();
				JDataTable tbl = new JDataTable(tblEle);
				this.addRootTable(tbl);
			}
		}
		// Data
		Element dataEle = root.getElement("Data");
		if (dataEle != null ) {
			Elements dataEles = dataEle.getElements();
			while(dataEles.hasMoreElements()) {
				Element rowEle = dataEles.next();
				// Woher JDataTable??
				String tablename = rowEle.getName(); // Geht das gut?
				JDataTable tbl = this.getDataTable(tablename);
				JDataRow row = new JDataRow(tbl, rowEle);
				this.addChildRow(row);
			}
		}
		// Profiler
		Element profEle = root.getElement("Profiler");
		if (profEle != null) {
			this.profiler = new ArrayList<ProfileEntry>();
			Elements eles = profEle.getElements();
			while(eles.hasMoreElements()) {
				Element ele = eles.next();
				ProfileEntry entry = new ProfileEntry(ele);
				profiler.add(entry);
			}
		}
		// Others
		Element userEle = root.getElement("Username");
		if (userEle != null) {
			this.username = userEle.getAttribute("value");
		}
		Element oidEle = root.getElement("Oid");
		if (oidEle != null) {
			this.oid = Convert.toLong(oidEle.getAttribute("value"));
		}
		Element warnEle = root.getElement("Warning");
		if (warnEle != null) {
			this.warning = warnEle.getAttribute("value");
		}		
	}
	/**
	 * Liefert den DataSet als XML-Document.<br>
	 * Mit diesem Document kann dieser DataSet wieder neu erzeugt werden.<p>
	 */
	public Document getXml() {
		return this.getXml(true);
	}
  public Document getXml(String encoding) {
    return this.getXml(true, encoding);
  }
  public Document getXml(boolean bProfile) {
    return this.getXml(bProfile, "UTF-8"); // PKÖ 5.10.2012: statt ISO-8859-1
  }
	/**
	 * 
	 * @param bProfile false liefert keine Profiling-Informationen
	 * @return
	 */
	public Document getXml(boolean bProfile, String encoding) {
		Document doc = new Document();
		doc.setEncoding(encoding);
		Element root = doc.setRoot("JDataSet");
		root.setAttribute("name", this.datasetName);
		if (this.databaseName != null) {
			root.setAttribute("DatabaseName", this.databaseName);
		}
		if (this.isReadonly() == true) {
			root.setAttribute("readonly", "true");
		}
		if (this.dataTables != null) {
			Element schema = root.addElement("Schema");
			for (JDataTable tbl: dataTables) {
				Element tblEle = tbl.getElement(true);
				schema.addElement(tblEle);
			}
		}
		if (this.dataRows != null) {
			Element data = root.addElement("Data");
			for (JDataRow row: dataRows) {
				row.getElement(data, false);
			}
		}
		if (this.profiler != null && bProfile == true) {
			Element profEle = root.addElement("Profiler");
			for (int i = 0; i < profiler.size(); i++) {
				ProfileEntry entry = profiler.get(i);
				entry.getXml(profEle);
			}
		}
		if (this.username != null) {
			Element userEle = root.addElement("Username");
			userEle.setAttribute("value", this.username);
		}
		if (this.oid > 0) {
			Element userEle = root.addElement("Oid");
			userEle.setAttribute("value", new Long(oid).toString());
		}
		if (this.warning != null) {
			Element userEle = root.addElement("Warning");
			userEle.setAttribute("value", this.warning);
		}
		return doc;
	}
	/**
	 * Erzeugt einen Clone dieses DataSet ohne Profiling-Informationen
	 * @return
	 */
	public JDataSet getClone() {
	  JDataSet ds = new JDataSet(this.datasetName);
	  ds.databaseName = this.databaseName;
	  ds.oid = this.oid;
	  ds.readonly = this.readonly;
	  ds.username = this.username;
	  ds.verified = this.verified;
	  ds.warning = this.warning;
	  // Tables
	  if (this.dataTables != null) {
	    ds.dataTables = new ArrayList<JDataTable>(dataTables.size());
	    for (JDataTable tbl: this.dataTables) {
	      JDataTable ctbl = new JDataTable(ds, tbl);
	      ds.addRootTable(ctbl);
	    }
	  }
	  // Rows
	  if (this.dataRows != null) {
         ds.dataRows = new ArrayList<JDataRow>(dataRows.size());
         for (JDataRow row: this.dataRows) {
           JDataRow crow = new JDataRow(ds.getDataTable(row.getDataTable().getTablename()), row);
           ds.dataRows.add(crow);
         }
	  }	  
	  return ds;
	}
	/**
	 * Beim Einsatz eines Persistenz-Layers dient dieser
	 * Name der Identifikation des Dataset-Typs.
	 * @return
	 */
	public String getDatasetName() {
		return datasetName;
	}
	/**
	 * Teilt ein Dataset, welches mehrere RootTables enthält.
	 * Dabei wird für jede RootTable ein DataSet im Ergebnisvector abgelegt.
	 * Das "neue" Dataset bekommt den Namen der RootTable verpasst.
	 * @param ds
	 * @return Vector mit x DataSet
	 */
	public Vector<JDataSet> split() {		
		Vector<JDataSet> vDS = null;
		// Tables
		Iterator<JDataTable> it = this.getDataTables();
		if (it != null) {
			vDS = new Vector<JDataSet>();
			while (it.hasNext()) {
				JDataTable rootTbl = it.next();
				JDataSet newDS = new JDataSet(rootTbl.getTablename());
				newDS.addRootTable(rootTbl);
				Iterator<JDataRow> iRow = this.getChildRows(rootTbl.getTablename());
				while( iRow != null && iRow.hasNext() ) {
					newDS.addChildRow(iRow.next());
				}
				vDS.add(newDS);
			}
		}		
		return vDS;
	}
	/**
	 * Erzeugt aus einem Dataset mit genau einer DataTable aber vielen DataRows
	 * einen Ergebnisvector von DataSets mit genau einer Row.
	 * Wirft eine {@link IllegalStateException}, wenn der DataSet mehr als eine RootTable hat.
	 * @return
	 */
	public Vector<JDataSet> splitMulti2Single() {
    Vector<JDataSet> vDS = null;
    JDataTable rootTbl = this.getDataTable(); // Wird Exception, wenn nicht genau eine
    // Row
    Iterator<JDataRow> it = this.getChildRows();
    if (it != null) {
      vDS = new Vector<JDataSet>();
      while (it.hasNext()) {
        JDataRow rootRow = it.next();
        JDataSet newDS = new JDataSet(this.getDatasetName());
        newDS.addRootTable(rootTbl);
        newDS.addChildRow(rootRow.cloneRow(this.getDataTable()));
        vDS.add(newDS);
      }
    }   
    return vDS;
	}
	/**
	 * Mischt einen Dataset hinzu; dabei werden die RootTables
	 * und RootRows aus dem angegebenen Dataset übernommen.
	 * Es entsteht ein DataSet mit mehreren RootTables und RootRows.
	 * @param ds
	 */
	public void merge(JDataSet ds) {
		// Tables
		Iterator<JDataTable> it = ds.getDataTables();
		if (it != null) {
			while (it.hasNext()) {
				JDataTable rootTbl = it.next();
				this.addRootTable(rootTbl);
			}
		}
		// Rows
		Iterator<JDataRow> ir = ds.getChildRows();
		if (ir != null) {
			while (ir.hasNext()) {
				JDataRow rootRow = ir.next();
				this.addChildRow(rootRow);
			}
		}
	}
	/**
	 * Fügt dem DataSet eine neue Wurzeltabelle hinzu.
	 * Die Namen der Tabellen müssen eindeutig sein; sonst
	 * wird eine IllegalArgumentException geworfen.
	 * @param tbl
	 */
	public void addRootTable(JDataTable tbl) {
		if (dataTables == null) {
			dataTables = new ArrayList<JDataTable>();
		}
		try {
			this.getDataTable(tbl.getTablename());
			throw new IllegalArgumentException("JDataSet#addRootTable: Duplicate Root Table: "+tbl.getTablename());
		} catch (Exception ex) {
			// Diese Exception wurde erwartet
		}
		dataTables.add(tbl);
		tbl.setTableType(JDataTable.ROOT_TABLE);
		//##tbl.setMyDataset(this);
	}
	/**
	 * Liefert eine Wurzel-Tabelle aus dem Dataset unter
	 * Angabe ihres Namens.
	 * @param name
	 * @return Exception, wenn Table fehlt.
	 */
	public JDataTable getDataTable(String name) {
		if (dataTables == null) {
			throw new IllegalStateException("JDataSet#getDataTable: No Root Tables defined");
		} else {
			for (JDataTable tbl: dataTables) {
				if (tbl.getTablename().equalsIgnoreCase(name)) {
					return tbl;
				}
			}
		}
		throw new IllegalArgumentException("JDataSet#getDataTable: Missing Root Table: " + name);
	}
	/**
	 * Liefert die Wurzel-Tabelle.<p>
	 * Wirft eine IllegalStateException, wenn die Zahl der
	 * Wurzeltabellen != 1 ist.
	 * @return
	 */
	public JDataTable getDataTable() {
		if (dataTables == null) {
			throw new IllegalStateException("JDataSet#getDataTable: No Root DataTable defined");
		} else if (dataTables.size() != 1) {
			throw new IllegalStateException("JDataSet#getDataTable: No single Root DataTable DataSet");
		} else {
			return dataTables.get(0);
		}
	}
	/**
	 * Prüft, ob die angegebene Tabelle eine Wurzel-Tabelle des
	 * DataSet ist.
	 * @param tbl
	 * @return
	 */
	public boolean isRootTable(JDataTable tbl) {
		if (dataTables == null) {
			return false;
		} else {
			for (JDataTable xTbl: dataTables) {
				if (tbl.getRefname().equalsIgnoreCase(xTbl.getRefname())) {
					return true;
				}
			}
			return false;
		}
	}
	/**
	 * Es wird geprüft, ob das übergebene DataTable-Object
	 * in irgendeiner Form Bestandteil dieses Dataset ist.
	 * @param tbl
	 * @return
	 */
	public boolean isDatasetTableObject(JDataTable tbl) {
		if (dataTables == null) {
			return false;
		} else {
			for (JDataTable xTbl: dataTables) {
				if (xTbl == tbl) {
					return true;
				} else {
				   if (xTbl.isTableObject(tbl) == true) {
				      return true;
				   }
				}
			}
			return false;
		}
	}
	/**
	 * Liefert den Iterator auf die Root Tables.
	 * @return
	 */
	public Iterator<JDataTable> getDataTables() {
		if (dataTables == null) {
			return null;
		} else {
			return dataTables.iterator();
		}
	}
	/**
	 * Liefert die DataTable mit dem angegebenen Pfad.<p>
	 * Notation: root.child1[x].child2[y]#parent1#parent2
	 * @param path
	 * @return
	 */
	public JDataTable getDataTablePath(String path) {
		JDataTable currentTable = null;
		int poi = path.indexOf('@');
		if (poi != -1) {
			path = path.substring(0, poi);
		}
		boolean child = false;
		StringTokenizer toks = new StringTokenizer(path, ".#", true);
		while (toks.hasMoreTokens()) {
			String tok = toks.nextToken();
			if (tok.equals(".")) {
				child = true;
			} else if(tok.equals("#")) {
				child = false; 
			} else {
				// Prüfen, ob hier ein Index drin ist // PKÖ 23.4.2004
				if (tok.endsWith("]")) {
					int p = tok.indexOf("[");
					// wenn ja: rausnehmen, weil sonst finden wir die Table nicht!
					if (p != -1) {
						tok = tok.substring(0, p);
					}
				}
				// End Prüfen				
				if (currentTable == null) {
					currentTable = this.getDataTable(tok);
				} else {
					if (child) {
						currentTable = currentTable.getChildTable(tok);
					} else {
						currentTable = currentTable.getParentTable(tok);
					}
				}
			}
		}
		return currentTable;
	}
	/**
	 * Liefert die DataRow mit dem angegebenen Pfad.<p>
	 * Notation: root[x].child1[y].child2[Spalte=Wert]#parent1#parent2
	 * @param path
	 * @return
	 */
	public JDataRow getDataRowPath(String path) {
      JDataRow currentRow = null;
      int poi = path.indexOf('@');
      if(poi == 0) {
         return this.getRow();
      }
      if(poi != -1) {
         path = path.substring(0, poi);
      }
      if(path.startsWith(".[")) {
         path = this.getDataTable().getTablename() + path.substring(1);
      }
      else if(path.startsWith(".")) {
         path = this.getDataTable().getTablename() + path;
      }
      boolean child = false;
      StringTokenizer toks = new StringTokenizer(path, ".#", true);
      while(toks.hasMoreTokens()) {
         String tok = toks.nextToken();
         if(tok.equals(".")) {
            child = true;
         }
         else if(tok.equals("#")) {
            child = false;
         }
         else {
            String tablename = tok;
            int index = 0;
            String arg = null;
            if(tok.endsWith("]")) {
               int p = tok.indexOf("[");
               if(p == -1) {
                  throw new IllegalArgumentException("JDataSet#getDataRowPath: Syntax Error: Missing '['");
               }
               tablename = tok.substring(0, p);
               String sIndex = tok.substring(p + 1, tok.length() - 1);
               if(sIndex.indexOf("=") != -1)
                  arg = sIndex;
               else
                  index = Integer.parseInt(sIndex);
            }
            if(currentRow == null) {
               // KKN 28.04.2004
               // currentRow = this.getRow(index);
               if(arg != null) {
                  currentRow = this.getChildRow(tablename, arg);
               }
               else {
                  try {
                     currentRow = this.getChildRow(tablename, index);
                  }
                  catch(Exception ex) {
                     logger.warn(ex.getMessage());
                     currentRow = null;
                  }
               }
            }
            else {
               if(child) {
                  if(arg != null) {
                     currentRow = currentRow.getChildRow(tablename, arg);
                  }
                  else {
                     currentRow = currentRow.getChildRow(tablename, index);
                  }
               }
               else {
                  currentRow = currentRow.getParentRow(tablename);
               }
            }
         }
      }
    
//    while (toks.hasMoreTokens()) {
//      String tok = toks.nextToken();
//      if (tok.equals(".")) {
//        child = true;
//      } else if(tok.equals("#")) {
//        child = false; 
//      } else {
//        String tablename = tok;
//        int index = 0;
//        if (tok.endsWith("]")) {
//          int p = tok.indexOf("[");
//          if (p == -1) {
//            throw new IllegalArgumentException("JDataSet#getDataRowPath: Syntax Error: Missing '['");
//          }
//          tablename = tok.substring(0,p);
//          String sIndex = tok.substring(p+1,tok.length()-1);
//          index = Integer.parseInt(sIndex);
//        }
//        if (currentRow == null) {
//          // KKN 28.04.2004
//          // currentRow = this.getRow(index);
//          currentRow = this.getChildRow(tablename, index);
//        } else {
//          if (child) {
//            currentRow = currentRow.getChildRow(tablename, index);
//          } else {
//            currentRow = currentRow.getParentRow(tablename);
//          }
//        }
//      }
//    }

		if (currentRow == null) {
			throw new IllegalArgumentException("JDataSet#getDataRowPath: '"+this.datasetName+"' Missing Row Path '"+path+"'");
		} else {
			return currentRow;
		}
	}
	/**
	 * Liefert die Child Rows mit dem angegebenen Pfad. 
	 * @param path
	 * @return
	 */
	public Iterator<JDataRow> getDataRowsPath(String path) {
		//System.out.println(path);
       if (path.equals(".")) {
           return this.getChildRows();
       }
		Iterator<JDataRow> ret = null;
		int poi = path.lastIndexOf(".");
		if (poi == -1) {
			ret = this.getChildRows(path);
		} else {
			String xpath = path.substring(0, poi);
			String refName = path.substring(poi + 1);
			JDataRow parentRow = this.getDataRowPath(xpath);
			ret = parentRow.getChildRows(refName);
		}
		return ret;
	}
	/**
	 * Liefert die DataColumn mit dem angegebenen Pfad in der Notation
	 * root[x].child1[y].child2[z]#parent1#parent2@column1
	 * @param path
	 * @return
	 */
	public JDataColumn getDataColumnPath(String path) {
		JDataTable tbl = this.getDataTablePath(path);
		if (tbl == null) {
			System.err.println("Missing DataTable: '"+path+"'");
			return null;
		}
		int poi = path.indexOf("@");
		if (poi == -1) {
			throw new IllegalArgumentException("JDataSet#getDataColumnPath: Missing Field Separator '@'");
		}
		String field = path.substring(poi+1);
		return tbl.getDataColumn(field);
	}
	/**
	 * Liefert den DataValue mit dem angegebenen Pfad in der Notation
	 * root[x].child1[y].child2[z]#parent1#parent2@column1
	 * @param path
	 * @return
	 */
	public JDataValue getDataValuePath(String path) {
		int poi = path.indexOf("@");
		if (poi == -1) {
			throw new IllegalArgumentException("JDataSet#getDataValuePath: Missing Field Separator '@': " +path);
		}
		JDataRow row = this.getDataRowPath(path);
		String field = path.substring(poi+1);
		return row.getDataValue(field);
	}
	/**
	 * Liefert den Wert des angegebene Feldes;
	 * Notation: root[x].child1[y].child2[z]#parent1#parent2@column1
	 * @param path
	 * @return Den Inhalt des DataValue als String
	 */
	public String getValuePath(String path) {
		JDataValue val = this.getDataValuePath(path);
		return val.getValue();
	}
	/**
	 * Liefert den Wert des angegebene Feldes oder
	 * wenn diese null, den übergebenen DefaultValue
	 * @param path
	 * @param dValue
	 * @return
	 */
	public String getValuePath(String path, String defaultValue) {
		JDataValue val = this.getDataValuePath(path);
		return val.getValue(defaultValue);
	}
	public Date getValueDatePath(String path) {
		JDataValue val = this.getDataValuePath(path);
		return val.getValueDate();
	}
	public Date getValueDatePath(String path, Date defaultValue) {
		JDataValue val = this.getDataValuePath(path);
		return val.getValueDate(defaultValue);
	}
	public int getValueIntPath(String path) {
		JDataValue val = this.getDataValuePath(path);
		return val.getValueInt();
	}
	public int getValueIntPath(String path, int defaultValue) {
		JDataValue val = this.getDataValuePath(path);
		return val.getValueInt(defaultValue);
	}
	public long getValueLongPath(String path) {
		JDataValue val = this.getDataValuePath(path);
		return val.getValueLong();
	}
	public long getValueLongPath(String path, long defaultValue) {
		JDataValue val = this.getDataValuePath(path);
		return val.getValueLong(defaultValue);
	}
	/**
	 * Setzt den Wert des angegebene Feldes;
	 * Notation: root[x].child1[y].child2[z]#parent1#parent2@column1
	 * @param path
	 * @return identisch mit value
	 */
	public String setValuePath(String path, String value) {
		JDataValue val = this.getDataValuePath(path);
		return val.setValue(value);		
	}
	/**
	 * Setzt den Wert des angegebene Feldes;
	 * Notation: root[x].child1[y].child2[z]#parent1#parent2@column1
	 * @param path
	 * @param value Ein Datum
	 * @return Das übergebene Datum als String
	 */
	public String setValuePath(String path, java.util.Date value) {
		JDataValue val = this.getDataValuePath(path);
		return val.setValue(value);		
	}
	/**
	 * Die Konvertierung nach String hängt vom Sql-DatenTyp ab.
	 * @param path
	 * @param value
	 * @return
	 */
	public String setValuePath(String path, Object value) {
		JDataValue val = this.getDataValuePath(path);
		return val.setValue(value);		
	}
	/**
	 * Setzt den Wert des angegebene Feldes;
	 * Notation: root[x].child1[y].child2[z]#parent1#parent2@column1
	 * @param path
	 * @return Der neue Wert als String
	 */
	public String setValuePath(String path, long value) {
		JDataValue val = this.getDataValuePath(path);
		return val.setValue(value);		
	}
	/**
	 * Setzt den Wert des angegebene Feldes;
	 * Notation: root[x].child1[y].child2[z]#parent1#parent2@column1
	 * @param path
	 * @return Der neue Wert als String
	 */
	public String setValuePath(String path, int value) {
		JDataValue val = this.getDataValuePath(path);
		return val.setValue(value);		
	}
	/**
	 * Setzt den Wert des angegebene Feldes;
	 * Notation: root[x].child1[y].child2[z]#parent1#parent2@column1
	 * @param path
	 * @return Der neue Wert als String
	 */
	public String setValuePath(String path, boolean value) {
		JDataValue val = this.getDataValuePath(path);
		return val.setValue(value);		
	}
	/**
	 * @deprecated
	 * @see #getChildRows
	 * @return
	 */
	public Iterator<JDataRow> getRows() {
		return this.getChildRows();
	}
	/**
	 * Liefert den Iterator für die Root Rows oder null, wenn keine Rows vorhanden.
	 * @return
	 */
	public Iterator<JDataRow> getChildRows() {
		if (this.dataRows == null) {
			return null;
		} else {
			return dataRows.iterator();
		}
	}
	/**
	 * @deprecated
	 * @see #getChildRows(DataView)
	 * Liefert einen Iterator über die Root Rows,
	 * bei denen der Wert eines Feldes der angegebenen Bedingung entspricht.
	 * @param condition FieldName, FieldValue
	 * @return
	 */
	public Iterator<JDataRow> getRows(NVPair condition) {
		if (dataRows == null) {
			return null;
		}
		ArrayList<JDataRow> al = new ArrayList<JDataRow>();
		for (JDataRow row: dataRows) {
			String value = row.getValue(condition.getName());
			if (value.equals(condition.getValue())) {
				al.add(row);
			}
		}
		if (al.size() == 0) {
			return null;
		} else {
			return al.iterator();
		}
	}
	/**
	 * Liefert die WurzelZeile aus dem DataSet.<p>
	 * Wirft eine IllegalStateException, wenn der DataSet leer ist
	 * oder mehr als eine WurzelZeile vorhanden.
	 * @return
	 */
	public JDataRow getRow() { 
		if (this.dataRows == null) {
			return null;
		} else if (dataRows.size() != 1) {
			throw new IllegalStateException("JDataSet#getRow: No single DataRow DataSet");
		} else {
			return dataRows.get(0);
		}
	}
	/**
	 * @deprecated
	 * @see #getChildRow(int)
	 * @param index
	 * @return
	 */
	public JDataRow getRow(int index) {
		return this.getChildRow(index);
	}
	/**
	 * Liefert die WurzelZeile aus dem DataSet mit dem angegebenen Index
	 * TODO: Das klappt nur, wenn das ein Single Root-Table Dataset ist!
	 * (0-relativ).
	 * @param index
	 * @return
	 */
	public JDataRow getChildRow(int index) {
	   if (dataRows == null) return null;
	   if (dataTables.size() != 1) {
	      logger.warn("No Single Root Table Dataset: "+ this.getDatasetName());
	   }
	   return this.dataRows.get(index);
	}
	/**
	 * Liefert die Child Row zu der angegebenen Tabelle 
	 * mit dem angegegeben Index (0-relativ).<p>
	 * Wirft eine IllegalArgumentException, wenn Index out of Range.
	 * @param refname
	 * @param index
	 * @return
	 */
	public JDataRow getChildRow(String refname, int index) {
//		Iterator<JDataRow> i = this.getChildRows(refname);
//		if (i == null) {
//			return null;
//		}
//		int cnt = 0;
//		while (i.hasNext()) {
//			JDataRow row = i.next();
//			if (cnt == index) {
//				return row;
//			}
//			cnt++;
//		}
	   // 1. Abkürzung: Single Root Table.
	   if (dataRows == null)
	      return null;
	   if (this.dataTables.size() == 1) { 
	      if (index < 0 || index >= dataRows.size()) {
	         throw new IllegalArgumentException("JDataSet#getChildRow#1: '"+refname+"' Child Row Index out of Range: "+index);
	      }
	      JDataRow row = dataRows.get(index);
          if (row.getDataTable().getTablename().equalsIgnoreCase(refname)) {
             return row;
          } else {
             throw new IllegalArgumentException("JDataSet#getChildRow#2: '"+refname+"' Missing DataTable");
          }
	   }
	   // 2. Multi Root Tables
	   Iterator<JDataRow> itr = dataRows.iterator();
	   int cnt = 0;
	   while (itr.hasNext()) {
	      JDataRow row = itr.next();
	      if (row.getDataTable().getTablename().equalsIgnoreCase(refname)) {
	         if (index == cnt) {
	            return row;
	         }
	         cnt++;
	      }
	   }
	   throw new IllegalArgumentException("JDataSet#getChildRow#3: '"+refname+"' Child Row Index out of Range: "+index);
	}
  /**
   * Findet die ChildRow bei der die angegebene Spalte den angegebenen Wert hat
   * (im Format name=value)
   * @param refname
   * @param arg SpaltenName=SpaltenWert
   * @return
   */
  public JDataRow getChildRow(String refname, String arg) {
    Iterator<JDataRow> i = this.getChildRows(refname);
    if (i == null) {
      return null;
    }
    int p = arg.indexOf("=");
    if (p == -1) return null;
    String columnName = arg.substring(0,p);
    String columnValue = arg.substring(p+1);
    int cnt = 0;
    while (i.hasNext()) {
      JDataRow row = i.next();
      JDataValue val = row.getDataValue(columnName);
      if (columnValue.equals(val.getValue())) {
        return row;
      }
      cnt++;
    }
    throw new IllegalArgumentException("JDataSet#getChildRow: '"+refname+"' Undefined Child Row: "+arg);
  }
	/**
	 * @deprecated
	 * @see #getChildRows(String)
	 * @param refname
	 * @return
	 */
	public Iterator<JDataRow> getRows(String refname) {
		return this.getChildRows(refname);
	}
	/**
	 * Liefert einen Iterator über alle Root Rows zu der angegebenen
	 * Table.
	 * @param tablename
	 * @return
	 */
	public Iterator<JDataRow> getChildRows(String tablename) {
		if (dataRows == null) {
			return null;
		} else {
		   ArrayList<JDataRow> al = new ArrayList<JDataRow>(dataRows.size());
		   Iterator<JDataRow> itr = dataRows.iterator();
		   while (itr.hasNext()) {
		      JDataRow row = itr.next();
		      if (row.getDataTable().getTablename().equalsIgnoreCase(tablename)) {
		         al.add(row);
		      }
		   }
		   if (al.size() == 0) {
				return null;
		   } else {
				return al.iterator();
		   }
		}
	}
	// Kommentar geerbt
	public Collection<JDataRow> getChildRows(DataView view) {
		ArrayList<JDataRow> al = view.getChildRowList(this);
		return Collections.unmodifiableCollection(al);
	}
	/**
	 * @deprecated
	 * @see #addChildRow
	 * @param row
	 */
	public void addRow(JDataRow row) {
		this.addChildRow(row);
	}
	/**
	 * Fügt eine Root-Row hinzu.<br>
	 * Es dürfen nur Rows von den Tabellen hinzugefügt werden,
	 * die auch als Root-Table aufgeführt sind.<br>
	 * Ansonsten wird eine IllegalArgumentException geworfen.
	 * @param row
	 */
	public void addChildRow(JDataRow row) {
		if (this.isRootTable(row.getDataTable()) == false) {
			throw new IllegalArgumentException("JDataSet#addRow: DataTable '"+row.getDataTable().getTablename()+"' isnt a Root Table");
		}
		if (this.dataRows == null) {
			this.dataRows = new ArrayList<JDataRow>();
		}
		this.dataRows.add(row);
		//##this.importRow(row);
	}
	public void addChildRow(JDataRow row, int index) {
		if (this.isRootTable(row.getDataTable()) == false) {
			throw new IllegalArgumentException("JDataSet#addRow: DataTable '"+row.getDataTable().getTablename()+"' isnt a Root Table");
		}
		if (this.dataRows == null) {
			this.dataRows = new ArrayList<JDataRow>();
		}
		if (dataRows.size() < index || index < 0) {
			throw new IllegalArgumentException("Index out of Range: "+dataRows.size());
		}
		this.dataRows.add(index, row);
		//##this.importRow(row);
	}
	/**
	 * Versucht die DataTable von frisch hinzugefügten Rows mit den DataTables dieses Dataset zu verknüpfen.
	 * @param row
	 */
	private void importRow(JDataRow row) {
		if (dataTables == null) {
			logger.warn("No DataTables defined: " + datasetName);
			return;
		}
		JDataTable t = row.getDataTable();
		boolean merged = false;
		// myDataTable
		if (this.dataTables.contains(t)) {
           System.out.println("Merged: root");
			merged = true;
		}
        if (! merged) {
           for (JDataTable tbl:dataTables) {
              // ChildTables
              ArrayList<Relation> childRelas = tbl.getChildRelations();
              if (childRelas != null) {
                 for (Relation rel:childRelas) {
                    if (rel.getChildTable() == t || rel.getParentTable() == t) {
                       merged = true;
                       System.out.println("Merged1: " + rel);
                       break;
                    }
                 }
                 if (! merged) {
                    for (Relation rel:childRelas) {
                       if (rel.getChildTable().getTablename().equalsIgnoreCase(t.getTablename())) {
                          row.setDataTable(rel.getChildTable());
                          merged = true;
                          System.out.println("Merged2: " + rel);
                          break;
                       }
                       if (rel.getParentTable().getTablename().equalsIgnoreCase(t.getTablename())) {
                          row.setDataTable(rel.getParentTable());
                          merged = true;
                          System.out.println("Merged3: " + rel);
                          break;
                       }
                    }
                 }
              }
           }
        }
        if (! merged) {
           for (JDataTable tbl:dataTables) {           
              // ParentTables
              if (tbl.isParentTable(row.getDataTable())) {
                 merged = true;
                 System.out.println("Merged4: " + tbl.getTablename());
              } else {
                 if (tbl.isParentTable(row.getDataTable())) {
                    row.setDataTable(tbl);
                    merged = true;
                    System.out.println("Merged5: " + tbl.getTablename());
                 }
              }
           }
       }
		if (! merged) {
      		for (JDataTable tbl:dataTables) {
      			if (tbl.getTablename().equalsIgnoreCase(row.getDataTable().getTablename())) {
      				row.setDataTable(tbl);
      				//logger.warn("import DataTable by name: " + row.getDataTable().getTablename());
      				merged = true;
      				break;
      			}
      		}
		}
		if (! merged) {
	       logger.warn("Unable to find matching DataTable: " + row.getDataTable().getTablename());
		   return;
		}
        // ChildRows
        Iterator<JDataRow> itChilds = row.getChildRows();
        if (itChilds != null ) {
           while(itChilds.hasNext()) {
              JDataRow rowChild = itChilds.next();
              this.importRow(rowChild);
           }
        }
        // Parent Rows
        Iterator<JDataRow> itParents = row.getParentRows();
        if (itParents != null ) {
           while(itParents.hasNext()) {
              JDataRow rowParent = itParents.next();
              this.importRow(rowParent);
           }
        }               
	}
	/**
	 * @deprecated
	 * @see #createChildRow(String)
	 * @param refname
	 * @return
	 */
	public JDataRow addRow(String refname) {
		return this.createChildRow(refname);
	}
	/**
	 * Fügt der angegebenen Root Table dieses Dataset
	 * eine neue leere Zeile hinzu.
	 * @param refname
	 * @return Die neue Zeile
	 */
	public JDataRow createChildRow(String refname) {
		JDataTable rootTable = this.getDataTable(refname);
		JDataRow newRow = rootTable.createNewRow();
		this.addChildRow(newRow);
        //newRow.setMyDataSet(this);
		return newRow;
	}
	/**
	 * @deprecated
	 * @see #createChildRow
	 * @return
	 */
	public JDataRow addRow() {
		return this.createChildRow();
	}
	/**
	 * Fügt dem DataSet eine neue leere Zeile hinzu.<p>
	 * Achtung!<br>
	 * Diese funktioniert nur, wenn dem DataSet genau eine
	 * Root Table zugeordnet ist.
	 * @return
	 */
	public JDataRow createChildRow() {
		if (this.dataTables.size() != 1) {
			throw new IllegalStateException("JDataSet#addRow: This is no single Root Table DataSet");
		} else {
			JDataTable rootTable = dataTables.get(0);
			JDataRow newRow = rootTable.createNewRow();
			this.addChildRow(newRow);
			return newRow;
		}
	}
	/**
	 * Fügt eine Zeile am angegebenen Pfad ein.<p>
	 * Achtung!<br>
	 * Das geht schief, wenn die übergeordneten Rows
	 * nicht existieren!
	 * @param path
	 * @return Die neu eingefügte leere Zeile
	 */
	public JDataRow addChildRowPath(String path) {
		// TODO : prüfen; siehe GuiTable#getDatasetValues (wenn inserted)
		JDataTable tbl = this.getDataTablePath(path);
		if (tbl == null) return null;
		JDataRow row = tbl.createNewRow();
		// Parent Row
		int poi = path.lastIndexOf(".");
		if (poi != -1) {
			String xpath = path.substring(0, poi);
			//String refname = path.substring(poi+1);
			JDataRow parentRow = this.getDataRowPath(xpath);
			parentRow.addChildRow(row); 
		}
		else {
			this.addChildRow(row);
		}
		return row;
	}
	
	int getChildRowIndex(JDataRow childRow) {
	   int i = dataRows.indexOf(childRow);
	   return i;
	}

		
	/**
	 * Liefert die Anzahl der Rows zu der angegebenen Tabelle.
	 * @param tablename
	 * @return
	 */
	public int getRowCount(String tablename) {
		int cnt = 0;
		if (dataRows == null) {
			return 0;
		} else {
			Iterator<JDataRow> i = this.getChildRows(tablename);
			if (i == null) {
				return 0;
			} else {
				while(i.hasNext()) {
					i.next();
					cnt++;
				}
			}
		}
		return cnt;
	}
	/**
	 * Liefert die Anzahl der Rows der RootTable.
	 * @return
	 */
	public int getRowCount() {
		if (dataRows == null) {
			return 0;
		} else {
			return this.dataRows.size();
		}
	}
	/**
	 * Liefert true, wenn der DataSet keine DataRows enthält
	 * (wohl aber beliebige Metadaten)
	 * @return
	 */
	public boolean isEmpty() {
		if (this.dataRows == null || this.dataRows.size() == 0) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * Liefert einen DataSet der nur die DataRows enthält,
	 * die verändert wurden; also inserted, delete, modified.<p>
	 * Dieser DataSet ist also ein Subset des vorhanden; der
	 * Sinn dieser Methode besteht darin, dass nur modifizierte
	 * Daten an den Persistenz Layer übertragen werden, und so
	 * Bandbreite im Netz gespart wird.<p>
	 * Es werden allerdings auch die Rows mit übertragen, die für
	 * die Abbildung der Relationen nötig sind.<p>
	 * Wenn die Daten erfolgreich übertragen wurden, ist
	 * anschließend commitChanges aufzurufen.
	 * @return Ein DataSet oder null, wenn keine Änderungen vorhanden.
	 * @see #commitChanges
	 * @see #hasChanges()
	 */
	public JDataSet getChanges() {
		JDataSet ds = null;
		// Child Rows
		if (dataRows != null) {
			for (int i = 0, size = dataRows.size(); i < size; i++) {
				JDataRow childRow = dataRows.get(i);
				JDataRow modiRow = childRow.getChanges();
				if (modiRow != null) {
					if (ds == null) {
						ds = new JDataSet(this.getDatasetName());
						if (this.username != null) {
							ds.setUsername(this.username);
						}
					}
					JDataTable modiTable = modiRow.getDataTable();
					try {
						ds.getDataTable(modiTable.getTablename()); 
					} catch (Exception ex) {
						// Wenn diese Tabelle fehlt, dann dem DataSet zuweisen
						ds.addRootTable(modiTable);
					}
					ds.addChildRow(modiRow);
				}
			}
		}
		return ds;
	}
    public JDataSet getChangesPersistent() {
        JDataSet ds = null;
        // Child Rows
        if (dataRows != null) {
            for (int i = 0, size = dataRows.size(); i < size; i++) {
                JDataRow childRow = dataRows.get(i);
                JDataRow modiRow = childRow.getChangesPersistent();
                if (modiRow != null) {
                    if (ds == null) {
                        ds = new JDataSet(this.getDatasetName());
                        if (this.username != null) {
                            ds.setUsername(this.username);
                        }
                    }
                    JDataTable modiTable = modiRow.getDataTable();
                    try {
                        ds.getDataTable(modiTable.getTablename()); 
                    } catch (Exception ex) {
                        // Wenn diese Tabelle fehlt, dann dem DataSet zuweisen
                        ds.addRootTable(modiTable);
                    }
                    ds.addChildRow(modiRow);
                }
            }
        }
        return ds;
    }
	/** 
	 * Liefert true, wenn sich irgendeine DataRow des DataSet
	 * geändert hat (inserted, deleted, modified).
	 * @return
	 * @see #getChanges()
	 * @see #commitChanges()
	 */
	public boolean hasChanges() {
		if (dataRows != null) {
			for (int i = 0, size = dataRows.size(); i < size; i++) {
				JDataRow childRow = dataRows.get(i);
				if (childRow.hasChanges()) {
					return true;
				}
			}
		}
		return false;
	}

  public boolean hasChangesPersistent() {
    if (dataRows != null) {
      for (int i = 0, size = dataRows.size(); i < size; i++) {
        JDataRow childRow = dataRows.get(i);
        if (childRow.hasChangesPersistent()) {
          return true;
        }
      }
    }
    return false;
  }

	/**
	 * Alle Kennzeichnungen von inserted und modified
	 * werden zurückgesetzt.<br>
	 * Als deleted markierte Zeilen werden jetzt wirklich gelöscht.<p>
	 * Diese Methode wird üblicherweise aufgerufen
	 * <strong>nachdem</strong> die Daten in die Datenbank zurückgeschrieben
	 * wurden.
	 * @see JDataRow#setDeleted
	 * @see #hasChanges()
	 * @see #getChanges()
	 */
	public void commitChanges() {
		if (this.dataRows != null) {
			for (Iterator<JDataRow> i = dataRows.iterator(); i.hasNext();) {
				JDataRow row = i.next();
				if (row.isDeleted()) {
					i.remove();
				} else {
					row.commitChanges();
				}
			}
		}
	}
	/**
	 * Macht alle getätigten Änderungen seit dem letzten Commit 
	 * wieder rückgängig.<p>
	 * Geänderte Felder werden wieder auf den alten Wert gesetzt,
	 * die Markierung von gelöschten Zeilen wird zurückgenommen,
	 * eingefügte Zeilen werden wieder gelöscht.
	 * @see #commitChanges
	 */
	public void rollbackChanges() {
		if (this.dataRows != null) {
			for (Iterator<JDataRow> i = dataRows.iterator(); i.hasNext();) {
				JDataRow row = i.next();
				if (row.isInserted()) {
					i.remove();
				} else {
					row.rollbackChanges();
				}
			}
		}
	}
	/**
	 * Löscht alle vom DataSet gehaltenen Rows.
	 * Genau genommen werden alle Rows nur als gelöscht markiert.
	 * Damit die Daten wirklich aus der Datenbank gelöscht werden
	 * muß der Dataset zurückgeschrieben werden: IPL#setDataset(...)
	 * Erst nach IPL#commitChanges ist der Dataset wirklich leer.
	 */
	public void setDeleted(boolean b) {
		if (this.dataRows != null) {			
			for (Iterator<JDataRow> i = this.dataRows.iterator();i.hasNext();) {
				JDataRow row = i.next();
				row.setDeleted(b);
			}
		}
	}
  /**
   * Setzt alle vom DataSet gehaltenen Rows auf "inserted".<p>
   * Dabei werden alle PK-Felder auf null gesetzt.<br>
   * Wird der DataSet anschließend dem PL übergeben, werden diese Daten mit
   * frischen IDs eingefügt.<br>
   * Auf diese Art wird quasi ein "saveAs" für diesen DataSet ausgeführt.
   * @param b
   */
  public void setInserted(boolean b) {
    if (this.dataRows != null) {      
      for (Iterator<JDataRow> i = this.dataRows.iterator();i.hasNext();) {
        JDataRow row = i.next();
        row.setInserted(b);
        // Alle PKs auf null setzen!?
        int pkCnt = row.getDataTable().getPKColumnsCount();
        if (pkCnt != 0) {
          ArrayList<JDataValue> pal = row.getPrimaryKeyValues();
          for (Iterator<JDataValue> j = pal.iterator(); j.hasNext();) {
            JDataValue pval = j.next();
            pval.setValue((String)null); // Oder Leerstring als null??
          }
        }
      }
    }
  }
	// =========== Username, Warning, OID ===========================
	/**
	 * @see #setUsername
	 * @return
	 */
	public final String getUsername() {
		return username;
	}

	/**
	 * Setzt den Usernamen für diesen Dataset.<p>
	 * Da der Persistenz-Layer keine User-Sessions kennt,
	 * muß hier der Username übergeben werden, wenn dieser
	 * beim Insert und Update in der Datenbank vermerkt werden soll.
	 * @param username
	 */
	public final void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @see #setOid
	 * @return
	 */
	public final long getOid() {
		return oid;
	}
	/**
	 * Es kann ein beliebiger Wert gesetzt werden, 
	 * der von diesem Framework selbst nie benutzt wird.<p>
	 * Der Sinn besteht darin, daß serverseitige Dienste, die
	 * JDataSets erstellen, hier "Nachrichten" für den Client
	 * übergeben können.
	 * @param oid
	 */
	public final void setOid(long oid) {
		this.oid = oid;
	}
	/**
	 * @see #setWarning
	 * @return
	 */
	public final String getWarning() {
		return warning;
	}
	/**
	 * Es kann ein beliebiger Wert gesetzt werden, 
	 * der von diesem Framework selbst nie benutzt wird.<p>
	 * Der Sinn besteht darin, daß serverseitige Dienste, die
	 * JDataSets erstellen, hier "Nachrichten" für den Client
	 * übergeben können.
	 * @param warning
	 */
	public final void setWarning(String warning) {
		this.warning = warning;
	}
	/**
	 * Der Name des DataSet, wie in DatabaseConfig.xml spezifiziert.
	 * @param name
	 */
	public void setDatasetName(String name) {
		this.datasetName = name;
	}
	/**
	 * Liefert der DataSet als getXml().toString()
	 * @see #getXml()
	 */
	public String toString() {
		return this.getXml().toString();
	}
	public String toString(String encoding) {
	   return this.getXml(encoding).toString();
	}
	/**
	 * Liefert den Inhalt des Elements "Data" (kein Schema)
	 * @return "null" wenn leer
	 */
	public String toStringData() {
       Document doc = this.getXml(false);
       if (doc == null) return "null";
       Element root = doc.getRoot();
       if (root == null) return "null";
       Element eData = root.getElement("Data");
       if (eData == null) return "null";
       String s = eData.toString();
       return s;
	}
	/**
	 * Überprüft alle Rows dieses DataSet.<p>
	 * Das Ergebnis dieser Prüfung wird im DataSet vorgehalten.
	 * @see #isVerified()
	 * @see #getValidationError()
	 * @see JDataValue#verify
	 * @return
	 */	
	public boolean verify() {
		boolean ret = true;
		Iterator<JDataRow> it = this.getChildRows();		
		if (it != null) {
			while (it.hasNext()) {
				JDataRow row = it.next();
				if(row.isInserted() || row.isModified()) { 
					boolean veri = row.verify();
					if (veri == false) {
						ret = false;
					}
				}
			}
		}
		this.setVerified(ret);
		return ret;
	}	
	/**
	 * Liefert das Ergebnis von verify().
	 * @see #verify()
	 * @return Returns the verified.
	 */
	public boolean isVerified() {
		return verified;
	}
	/**
	 * @param verified The verified to set.
	 */
	public void setVerified(boolean verified) {
		this.verified = verified;
	}
	
	/**
	 * Liefert eine Auflistung von Fehlern zurück, die nach der Validierung festgestellt wurden.
	 * @return Fehlerliste
	 */
	public String getValidationError() {
	   StringBuffer sb = new StringBuffer();
		
	   Iterator<JDataRow> it = getChildRows();		
		if (it != null) {
			while (it.hasNext()) {
				JDataRow row = it.next();
				addError(row, sb);
			}
		}
		return sb.toString();
	}
	
	private String getPrimaryKey(JDataRow row) {
	    StringBuilder sb = new StringBuilder();
	    Iterator<JDataValue> it = row.getPrimaryKeyValues().iterator();	    
	    while(it.hasNext()) {
	        JDataValue val = it.next();
	        String columnName = val.getColumnName();
	        String value = val.getValue();
	        if(sb.length() != 0)
	            sb.append(", ");
	        
	        sb.append(columnName).append("=").append(value);
	    }
	    
	    if(sb.length() == 0) {
	        return "PrimaryKey [undefined]";
	    }
	    else {
	        return "PrimaryKey [ " + sb.toString() + " ]"; 
	    }
	}
	
	private void addError(JDataRow row, StringBuffer sb) {	    
		JDataTable tbl = row.getDataTable();
		String tableName = tbl.getTablename();
		
		if(row.isInserted() || row.isModified()) {
			Iterator<JDataValue> itVals = row.getDataValues();
			if (itVals != null) {
				while (itVals.hasNext()) {
					JDataValue val = itVals.next();
					String columnName = val.getColumnName();
					String value = val.getValue();
					if (value != null && value.length() > 50) {
					   value = value.substring(0,50) + "...";
					}
					
					String error = "DataSet validation error: table [ " + tableName + " ] row " + getPrimaryKey(row) + " column [ " + columnName + " ] value [ " + value + " ] ";
				   switch(val.getErrorCode()) {
				    	case JDataValue.NO_ERROR: 
				    	    continue;

				    	case JDataValue.NOTNULL:
  	            sb.append(error + "Not null!");
				    	    break;
				    	case JDataValue.MAX_LENGTH_EXEEDED:
				    	    sb.append(error + "Max [ " + val.getColumn().getSize() + " ] length exeeded!");
				    	    break;
				    	    
				    	case JDataValue.ILLEGAL_DATE_FORMAT:
				    	    sb.append(error + "Illegal date format!");
				    	    break;
				    	    
				    	case JDataValue.ILLEGAL_TIME_FORMAT:
				    	    sb.append(error + "Illegal time format!");
				    	    break;
				    	
				    	case JDataValue.ILLEGAL_DATETIME_FORMAT:
				    	    sb.append(error + "Illegal datetime format!");
				    	    break;
				    	    
				    	case JDataValue.ILLEGAL_NUMBER_FORMAT:
				    	    sb.append(error + "Illegal number format!");
				    	    break;
				    	    
				    	case JDataValue.ILLEGAL_BOOLEAN_VALUE:
				    	    sb.append(error + "Illegal boolean format!");
				    	    break;
				    	
				    	case JDataValue.USER_DEFINED_ERROR:
				    	    sb.append(error + "User defined error!");
				    	    break;
				   }
					sb.append("\n");
				}
			}
			
			// Child Rows
			if (row.getChildRows() != null) {
				for (Iterator<JDataRow> i = row.getChildRows(); i.hasNext();) {
					JDataRow childRow = i.next();
					addError(childRow, sb);
				}
			}
			// Parent Rows (eigentlich unnötig, da immer readonly)
			if (row.getParentRows() != null) {
				for (Iterator<JDataRow> i = row.getParentRows(); i.hasNext();) {
					JDataRow parentRow = i.next();
					addError(parentRow, sb);
				}
			}
		}
	}

	
	/**
	 * @see JDataRow#resetErrorState()
	 */	
	public void resetErrorState() {
		Iterator<JDataRow> it = this.getChildRows();
		if (it != null) {
			while (it.hasNext()) {
				JDataRow row = it.next();
				row.resetErrorState();
			}
		}
	}	
	/**
	 * Liefert die (ungefähre) Größe des Objects (ObjectOutputStream)
	 * @return
	 */
	public int getSize() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();
			return baos.size();
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}
	/**
	 * @return Returns the readonly.
	 */
	public boolean isReadonly() {
		return readonly;
	}
	/**
	 * @param readonly The readonly to set.
	 */
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
	/**
	 * @return Returns the databaseName.
	 */
	public String getDatabaseName() {
		return databaseName;
	}
	/**
	 * @param databaseName The databaseName to set.
	 */
	public void setDatabaseName(String databaseName) {
	   this.databaseName = databaseName;
	}
	/**
	 * Setzt den Zähler für das optimistische Locking wieder zurück
	 * (für JournalDirectory)
	 */
	public void rollbackVersion(String fieldName) {
	  if (fieldName == null || fieldName.length() == 0) return;
	  Iterator<JDataRow> it = this.getChildRows();
	  if (it == null) return;
	  while (it.hasNext()) {
	    JDataRow row = it.next();
	    row.rollbackVersion(fieldName);
	  }
	}
	/**
	 * Fügt bei den Rows eine Spalte hinzu, die auf die angegebene DataTable verweisen
	 * @param tbl
	 * @param col
	 */
	public void addDataColumn(JDataTable tbl, JDataColumn col) {
	   Iterator<JDataRow> it = this.getChildRows();
	   if (it == null) return;
	   while (it.hasNext()) {
	      JDataRow row = it.next();
	      row.addDataColumn(tbl, col);
	   }
	}
	
	// Profiling ###############################################
	public static class ProfileEntry implements Serializable {
      private static final long serialVersionUID = 1L;
		public String name;
		public long startTime;
		public long endTime;
		public ProfileEntry() {
			
		}
		public ProfileEntry(String name, long startTime) {
			this.name = name;
			this.startTime = startTime;
		}		
		public ProfileEntry(Element ele) {
			this.name = ele.getAttribute("name");
			this.startTime = Convert.toLong(ele.getAttribute("startTime"));
			this.endTime = Convert.toLong(ele.getAttribute("endTime"));
		}
		public String toString() {
			String s = this.name + ": " + (endTime-startTime);
			return s;
		}
		public Element getXml(Element parentElement) {
			Element ele = new Element("Entry");
			parentElement.addElement(ele);
			ele.setAttribute("name", this.name);
			ele.setAttribute("startTime", Convert.toString(startTime));
			ele.setAttribute("endTime", Convert.toString(endTime));
			return ele;
		}
	}
	/**
	 * Startet den Profiler unter Angabe eines eindeutigen Namens.
	 * @return Die Anzahl der Einträge im Profiler
	 */
	public int startProfiler(String name) {
		long time = System.currentTimeMillis();
		if (profiler == null) {
			profiler = new ArrayList<ProfileEntry>();
		}
		ProfileEntry entry = new ProfileEntry(name, time);
		profiler.add(entry);
		return profiler.size();		
	}
	/**
	 * Mißt die seit dem start vergangene Zeit.
	 * @see #startProfiler(String)
	 * @return Die seit dem Starten des Profilers vergangene Zeit.
	 */
	public long endProfiler() {
		if (profiler == null) {
			return 0;
		} else {
			int i = profiler.size();
			if (i == 0) {
				return 0;
			} else {
				long time = System.currentTimeMillis();
				ProfileEntry entry = profiler.get(i-1);
				entry.endTime = time;
				return time - entry.startTime;
			}
		}
	}
	/**
	 * Löscht die bisher aufgelaufenen Profiling-Daten.
	 *
	 */
	public void resetProfiler() {
		this.profiler = null;
	}
	/**
	 * Liefert die Profiling-Daten 
	 * @see ProfileEntry
	 * @return
	 */
	public ArrayList<ProfileEntry> getProfiler() {
		return this.profiler;
	}
	/**
	 * Gibt an, ob eine der Root-Tables des DataSet ein Änderungsprotokoll haben
	 */
	public boolean hasChangeProtocol() {
	  if (this.dataTables == null) {
	    return false;
	  }
	  for (JDataTable tbl:dataTables) {
	   if (tbl.hasChangeProtocol()) return true; 
	  }
	  return false;
	}
	/**
	 * Erzeugt aus einem Dataset-Namen und einem CSV-File
	 * in dem in der ersten Zeile die Feldnamen stehen 
	 * und in der zweiten Zeile die Feldtypen
	 * einen Dataset und eine DataTable mit dem angegebenen Namen
	 * und DataRows mit den folgenden Werten.<p>
	 * Die Zeilen sind mit \n getrennt
	 * und die Werte mit ";". <p>
	 * TODO: gehört diese Methode nicht in eine DatasetFactory?
	 * @param dsName
	 * @param csv
	 * @return
	 * @throws Exception
	 */
	public static JDataSet CSV2Dataset(String dsName, String csv) throws Exception {
	   JDataSet ds = CSV2Dataset(dsName, csv, true);
	   return ds;
	}
	/**
	 * @formatter:off
	 * TODO: Wenn keine Datentypen angegeben, dann anhand der Werte erraten?
	 * @param dsName
	 * @param csv
	 * @param datatypes true bedeutet, in der zweiten Zeile stehen die Datentypen
	 * @throws Exception
	 * @formatter:on
	 */
	public static JDataSet CSV2Dataset(String dsName, String csv, boolean datatypes) throws Exception {
	     // Create Dataset
       JDataSet ds = new JDataSet(dsName);
       JDataTable tbl = new JDataTable(dsName);
       ds.addRootTable(tbl);
       // Analyse cvs
       StringTokenizer toks = new StringTokenizer(csv, "\n");
       int lineCnt = 0;
       ArrayList<String> fieldNames = new ArrayList<String>();
       ArrayList<Integer> fieldTypes = new ArrayList<Integer>();
       while (toks.hasMoreTokens()) {
           lineCnt++; // 1-relativ
           String line = toks.nextToken(); // Nächste Zeile
           if (lineCnt == 1) { // Field-Names
               StringTokenizer sto = new StringTokenizer(line, ";" ,true); 
               // Zeile zersägen
               while (sto.hasMoreTokens()) {
                   String tok = getNextToken(sto);
                   fieldNames.add(tok);
                   tbl.addColumn(tok, Types.VARCHAR); // Default-Datentyp
               }
           } else if (lineCnt == 2 && datatypes) { // Field-Types
              int colCnt = 0; // 0-releativ
               StringTokenizer sto = new StringTokenizer(line, ";" ,true); 
               // Zeile zersägen
               while (sto.hasMoreTokens()) {
                   String tok = getNextToken(sto);
                   String colName = fieldNames.get(colCnt);
                   JDataColumn col = tbl.getDataColumn(colName);
                   int fieldType = JDataColumn.getType(tok);
                   col.setDataType(fieldType);
                   fieldTypes.add(fieldType);
                   colCnt++;
               }
           } else { // Data
               if (line.length() == 0) {
                   continue;
               }
               StringTokenizer sto = new StringTokenizer(line, ";", true); 
               JDataRow dataRow = tbl.createNewRow();
               ds.addChildRow(dataRow);
               // Zeile zersägen
               int fcnt = 0;  // 0-releativ
               while (sto.hasMoreTokens()) {
                   String tok = getNextToken(sto);
                   if (tok != null) {
                      JDataValue val = dataRow.getDataValue(fcnt);
                      if (datatypes) {
                         int type = fieldTypes.get(fcnt);
                         Object o = Convert.toObject(tok, type);
                         val.setValue(o);
                      } else {
                         val.setValue(tok);
                      }
                   }
                   fcnt++;
               }
           }
       }
       
       return ds;

	}
	
	private static String getNextToken(StringTokenizer sto) throws Exception {
		String tok = sto.nextToken();
		
		if ( tok.equalsIgnoreCase(";") ) {
			return null;
		}
		tok = tok.trim();
		/*
		 * Behandlung von ...;Feld1;"Feld2;Feld2";Feld3;...
		 * 
		 * Für den Fall, dass ein Eintrag welcher ein Semikolon enthält 
		 * in Anführungszeichen eingefasst ist, wird dieser einer 
		 * Sonderbehandlung unterzogen. Diese Einträge werden
		 * dadaruch erkannt, das der erste Token mit einem Anführungszeichen
		 * beginnt aber nicht mit einem Anführungszeichen endet.  
		 */
		if ( tok.startsWith("\"") && !tok.endsWith("\"")) {
			/*
			 * Es werden alle Tokens zu einem zusammengefasst,
			 * bis ein Token gefunden wird, welches mit einem
			 * Anführungszeichen endet.
			 */
			String nTok = "";
			while (sto.hasMoreTokens() && !nTok.endsWith("\"")) {				
				nTok += sto.nextToken();
				nTok = nTok.trim(); 				
			}
			/*
			 * Wenn das neue Token nicht mit einem Anführungszeichen endet,
			 * dann ist die Datei nicht ordentlich Formatiert.
			 */
			if ( !nTok.endsWith("\"")) {
				throw new IllegalArgumentException("Missung \"");
			}			
			tok += nTok;			
		} 
		/*
		 * Wenn eine Eintrag mir einem Anführungszeichen beginnt und 
		 * mit einem endet, dann diese entfernen.
		 */
		if ( tok.startsWith("\"") && tok.endsWith("\"") ) {			
			tok = tok.substring(1,tok.length()-1);			
		}
		
		if(sto.hasMoreTokens()) {
			@SuppressWarnings("unused") String dummy = sto.nextToken();
		}
		return tok;
	}
    /**
     * Erzeugt aus dem Dataset einen String im CSV-Format (;-getrennt)
     * Daten
     * @return
     */
    public String toCSV() {
       return this.toCSV(";");
    }
	public String toCSV(String sep) {
	   StringBuilder sb = new StringBuilder();
	   Iterator<JDataColumn> itCol = this.getDataTable().getDataColumns();
	   while(itCol.hasNext()) {
	      JDataColumn col = itCol.next();
	      sb.append(col.getColumnName());
	      sb.append(sep);	      
	   }
	   sb.append('\n');
	   Iterator<JDataRow> itRow = this.getChildRows();
	   while(itRow.hasNext()) {
	      JDataRow row = itRow.next();
	      Iterator<JDataValue> itVal = row.getDataValues();
	      while(itVal.hasNext()) {
	         JDataValue val = itVal.next();
	         String sval = val.getValue();
	         if (sval != null && sval.length() > 0) {
	            if (val.getColumn().isString()) {
	               sb.append("\"");
	            }
	            sb.append(sval);
                if (val.getColumn().isString()) {
	               sb.append("\"");
	            }
	         }
	         sb.append(sep);         
	      }	      
	      sb.append('\n');
	   }	   
	   return sb.toString();
	}
	/**
	 * Erzeugt aus dem übergebenen Object einen DataSet.<p>
	 * Es wird eine DataTable mit dem Klassennamen des Objekts angelegt.
	 * Dabei werden alle getter (inc is...) oder public Members in Spalten der
	 * Tabelle verwandelt.<p>
	 * Es wird eine DataRow mit den Werten dieser Attribute
	 * angelegt.
	 * @see #toObject(Object)
	 * @param o
	 * @return
	 */
	public static JDataSet toDataset(Object o) {
		JDataSet ds = new JDataSet(o.getClass().getName());
		JDataTable tbl = new JDataTable(ds.getDatasetName());
		ds.addRootTable(tbl);
		Class<?> clazz = o.getClass();
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		createDataset(ds, clazz, o, map);
		JDataRow row = tbl.createNewRow();
		ds.addChildRow(row);
		Iterator<JDataValue> it = row.getDataValues();
		while (it.hasNext()) {
			JDataValue dval = it.next();
			Object val = map.get(dval.getColumnName());
			dval.setValue(val);
		}
		return ds;
	}
	private static void createDataset(JDataSet ds, Class<?> clazz, Object o, LinkedHashMap<String, Object> map) {
		JDataTable tbl = ds.getDataTable();
		// 1. Methods
		Method[] ms = clazz.getMethods();
		for (int i = 0; i < ms.length; i++) {
			Method m = ms[i];
			String name = m.getName();
			Class<?> fc = m.getReturnType();
			String colName = name;
			String tp = fc.getName();
			boolean get = false;
			if (name.startsWith("get")) {
				get = true;
				colName = name.substring(3);
			} else if (name.startsWith("is")) {
				get = true;
				colName = name.substring(2);
			} else if (name.startsWith("set")) {
				colName = name.substring(3);
			}
			if (get) {
				int type = getType(tp);
				JDataColumn col = null;
				if (type != -1 && m.getParameterTypes().length == 0) {
					col = new JDataColumn(tbl, colName, type);
					//System.out.println(colName);
					try {
						Object val = m.invoke(o, new Object[]{});
						map.put(colName, val);
						tbl.addColumn(col);
					} catch (Exception ex) {
						System.err.println(ex.getMessage());
					}
				}
			}
		}
		// 2. Fields
		//Field[] fields = clazz.getFields();
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			f.setAccessible(true);
			String colName = f.getName();
			String tp = f.getType().getName();
			int type = getType(tp);
			if (type != -1) {
				try {
					JDataColumn col = new JDataColumn(tbl, colName, type);
					Object val = f.get(o);
					map.put(colName, val);
					tbl.addColumn(col);
				} catch (Exception ex) {
					System.err.println(ex.getMessage());
				}				
			}
		}
	}
	private static int getType(String tp) {
		int type = -1;
		if (tp.equals("boolean") || tp.endsWith(".Boolean")) {
			type = Types.BOOLEAN;
		} else if(tp.equals("int") || tp.endsWith(".Integer")) {
			type = Types.INTEGER;
		} else if (tp.equals("long") || tp.endsWith(".Long")) {
			type = Types.BIGINT;
		} else if (tp.equals("double") || tp.endsWith(".Double")) {
			type = Types.DOUBLE;
		} else if (tp.equals("float") || tp.endsWith(".Float")) {
			type = Types.REAL;
		} else if ( tp.endsWith(".BigDecimal")) {
			type = Types.DECIMAL;
		} else if (tp.endsWith(".String")) {
			type = Types.VARCHAR;
		} else if (tp.endsWith(".Date")) {
			type = Types.DATE;
		} else if (tp.endsWith(".Time")) {
			type = Types.TIME;
		} else if (tp.endsWith(".Timestamp")) {
			type = Types.TIMESTAMP;
		} else if (tp.equals("char") || tp.endsWith(".Character")) {
			type = Types.CHAR;
		} else if (tp.equals("byte") || tp.endsWith(".Byte")) {
			type = Types.TINYINT;
		} else if (tp.equals("short") || tp.endsWith(".Short")) {
			type = Types.SMALLINT;
		}
		return type;
	}
	/**
	 * Weist die Werte aus diesem DataSet dem übergebenen
	 * Object zu, wenn es über entsprechende setter oder public Member verfügt.
	 * @see #toDataset(Object)
	 * @param o
	 */
	public void toObject(Object o) {
		JDataRow row = this.getRow();
		row.getBean(o);
//		Iterator it = row.getDataValues();
//		while (it.hasNext()) {
//			JDataValue val = (JDataValue)it.next();
//			// Method?
//			Method m = this.getMethod(val, o.getClass());
//			if (m != null) {			
//				Class[] params = m.getParameterTypes();
//				if (params.length != 1) return;
//				Class param = params[0];
//				Object arg = Convert.toObject(val.getValue(), param);
//				Object[] args = new Object[] {arg};
//				try {
//					m.invoke(o, args);
//				} catch (Exception ex) {
//					System.err.println(ex.getMessage());
//				}
//				return;
//			}
//			// Field?
//			try {
//				Field f = o.getClass().getField(val.getColumnName());
//				Class type = f.getType();
//				Object arg = Convert.toObject(val.getValue(), type);
//				f.set(o, arg);
//			} catch (Exception ex) {
//				System.err.println(ex.getMessage());
//			}
//		}
	}
//	private Method getMethod(JDataValue val, Class clazz) {
//		Method ret = null;
//		String mName = "set" + val.getColumnName();
//		
//		Method[] ms = clazz.getMethods();
//		for (int i = 0; i < ms.length; i++) {
//			Method m = ms[i];
//			if (m.getName().equals(mName)) {
//				return m;
//			}
//		}
//		return ret;
//	}
}
