package de.jdataset;

import java.util.*;
import java.sql.*;
import java.io.Serializable;

import electric.xml.*;

import de.pkjs.util.*;
/**
 * Eine DataTable repäsentiert die Definition einer Tabelle in der Datenbank,
 * ihre Spalten sowie ihre über- und untergeordneten Tabellen.<p>
 * Tabellen können direkt dem DataSet zugewiesen werden (Root Table)
 * oder sie werden als Child Table oder Parent Table einer
 * anderen Tabelle zugeordnet.<br>
 * Es ist nicht erlaubt, einer Parent Table Child Tables hinzuzufügen.<p>
 * Das Geheimnis mit den Child und Parent Reference Names:<br>
 * Bei ER-Modellen ist es erlaubt (wenn auch nicht übermäßig häufig),
 * daß ein und die selbe Tabelle mit einer anderen Tabelle mehrfach
 * verknüpft ist.<br>
 * Beispiel: Die Tabelle Buchung hat die beiden Foreign Keys
 * KontoID und GegenkontoID die beide auf die übergeordnete Parent
 * Table "Konto" zeigen. In unserer Zugriffs-Definition sähe dann so aus:
 * <pre><code>
 * &lt;View ...&gt;
  &lt;RootTable name="Buchung" ...&gt;
    &lt;Column name="betrag"/&gt;
    &lt;Column name="kontoID"/&gt;
    &lt;Column name="gegenkontoID"/&gt;
    &lt;Parent tablename="Konto" fk="kontoID"&gt;
      &lt;Column name="Kontobezeichnung"/&gt;
    &lt;/Parent&gt;
    &lt;Parent tablename="Konto" fk="gegenkontoID"&gt;
      &lt;Column name="Kontobezeichnung"/&gt;
    &lt;/Parent&gt;
</code></pre>
 * Um jetzt im Dataset von der Tabelle Buchung aus zu den beiden
 * Konto-Bezeichnungen zu navigieren brauchts also mehr
 * Informationen als den Namen der Tabelle.<p>
 * Natürlich muß die Kombination aus Tabellennamen und Foreign Key auch
 * eindeutig sein, aber diese Art der Notation ist doch
 * gar zu umständlich, wenn sie nur in wenigen Füllen wirklich
 * benötigt wird.<p>
 * Statt dessen wird der Child- und der Table Referenz jeweils
 * ein Name gegeben (wie das im ER-Modell auch üblich ist):<p>
 * <pre><code>
 * &lt;View ...&gt;
  &lt;RootTable name="Buchung" ...&gt;
    &lt;Column name="betrag"/&gt;
    &lt;Column name="kontoID"/&gt;
    &lt;Column name="gegenkontoID"/&gt;
    &lt;Parent tablename="Konto" fk="kontoID"&gt;
      &lt;Column name="Kontobezeichnung"/&gt;
    &lt;/Parent&gt;
    &lt;Parent tablename="Konto" <b>refname="Gegenkonto"</b> fk="gegenkontoID"&gt;
      &lt;Column name="Kontobezeichnung"/&gt;
    &lt;/Parent&gt;
</code></pre>
 * Einfache Regel:<br>
 * Wenn nichts weiter angegeben wird der Name der Tabelle als
 * Name der Referenz verwendet; das geht meistens gut.
 * Ansonsten muss im Aufnahmefall der Name der Referenz
 * gesondert angegeben werden.
 */
public final class JDataTable implements Serializable {
	// Attributes
	public static final int ROOT_TABLE = 1;
	public static final int CHILD_TABLE = 2;
	public static final int PARENT_TABLE = 3;
	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JDataTable.class);
	static final long serialVersionUID = -3211853369671609490L;

	private String tablename;
	private String alias;
	// new 23.4.2005 wenn ROOT_TABLE ansonsten null
	//private JDataSet myDataset;
	// new 23.4.2005 wenn CHILD oder PARENT_TABLE ansonsten null
	private JDataTable myParentTable;
	private String beanClassName; // new 5.11.2004
	/**
	 * @deprecated @see Relation
	 */
	private String refname; // Für Child- oder Parent-Reference
	private String catalog; // TABLE_CAT
	private String schema; // Database-Schema or null; from TABLE_SCHEM
	private String type; // From TABLE_TYPE
	private int tableType; // ROOT, CHILD, PARENT
	private transient String comment; // REMARKS // New 2.7.2004 / PKÖ
	private String changeProtocol;
	private transient boolean locked; // 12.3.2014: Wenn gesperrt, dann nicht (mehr) ändern; ist nach Serialisierung/Cloning aber wieder erlaubt
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked() {
		this.locked = true;
	}
  
  public String getChangeProtocol () {
    return this.changeProtocol;
  }
  
  public boolean hasChangeProtocol() {
    return getChangeProtocol() != null;
  }
  /**
   * Änderungsprotokoll für diese Root-Tabelle definieren
   * Das muß die Definition eines Views in PLConfig.xml sein.
   * @param s
   */
  public void setChangeProtocol(String s) {
    this.changeProtocol = s;
  }

	private LinkedHashMap<String, JDataColumn> dataColumns;
	private ArrayList<JDataTable> parentRefs;

	private ArrayList<Relation> childRelas; 	// New 14.12.2003
	private ArrayList<Relation> parentRelas; 	// New 14.12.2003
	// TODO Die folgenden drei (oder vier?) Attribute gehören in die Klasse Relation!
	private String join; // LEFT, INNER, RIGHT, ...
	private boolean selfReference; // selfReference
	private String databaseName;
	/**
	 * Diese Angabe wird für Parent Tables benötigt.
	 * Hier werden die Columns der anderen Tabelle aufgeführt,
	 * deren FK auf diese Tabelle hier zeigt.
	 * TODO eigentlich wird hier eine ParentRelation benötigt
	 */
	private String parentFK;
	private boolean readonly;
	private boolean isTransient; // Wird nie aus der Datenbank gelesen oder in sie geschrieben.
	private String virtualChild; // Wird als DataRow in *einem* Feld (nämlich diesem hier) der Parenttabelle gespeichert.

	// Constructors
	/**
	 * @deprecated
	 * For serialization purpose only.
	 */
	public JDataTable() {}
	/**
	 * @deprecated
	 * @param s
	 */
	public void setTablename(String s) {
		this.tablename = s;
	}
	/**
	 * Erzeugt eine Tabelle mit dem angegebenen Namen.
	 */
	public JDataTable(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Tablename is NULL");
		}
		this.tablename = name;
	}
	/**
	 * Erzeugt eine Tabelle vom angegebenen Typ (ROOT, CHILD, PARENT).
	 * @param name
	 * @param tableType
	 */
	public JDataTable(String name, int tableType) {
		this(name);
		this.tableType = tableType;
	}
	/**
	 * Erzeugt eine Tabelle mit dem angegebenen Namen
	 * und einem abweichenden Alias-Namen.
	 */
	public JDataTable(String name, String alias) {
		this(name);
		this.alias = alias;
	}
	public JDataTable(String name, String alias, int tableType) {
		this(name, alias);
		this.tableType = tableType;
	}
	JDataTable(Element ele) {
		if (ele.getAttribute("tablename") != null) {
			this.tablename = ele.getAttribute("tablename");
		} else {
			this.tablename = ele.getName();
		}
		this.databaseName = ele.getAttribute("DatabaseName");
		this.alias = ele.getAttribute("alias");
		this.beanClassName = ele.getAttribute("beanclass");
		this.refname = ele.getAttribute("refname");
		this.schema = ele.getAttribute("schema");
		this.type = ele.getAttribute("type");
		// inline
		this.join = ele.getAttribute("join");

		// self
		this.selfReference = Convert.toBoolean(ele.getAttribute("selfReference"));
		this.readonly = Convert.toBoolean(ele.getAttribute("readonly"));
		this.isTransient = Convert.toBoolean(ele.getAttribute("transient"));
		this.virtualChild = ele.getAttribute("virtual");
		this.changeProtocol = ele.getAttribute("changeProtocol");
		// TODO Comment
		// Columns
		Elements eles = ele.getElements("Column");
		while (eles.hasMoreElements()) {
			Element colEle = eles.next();
			JDataColumn col = new JDataColumn(this, colEle);
			this.addColumn(col, true);
		}
		// Childs
		Elements childEles = ele.getElements(new XPath("[@tabletype='child']"));
		while (childEles.hasMoreElements()) {
			Element childEle = childEles.next();
			String fk = childEle.getAttribute("fk");
			JDataTable childTable = new JDataTable(childEle);
			Relation rela = this.addChildTable(childTable, fk);
			// Update Rule
			String updateRule = childEle.getAttribute("OnUpdate");
            if (updateRule != null && updateRule.length() > 0) { // NoAction ist default
               if ("Cascade".equalsIgnoreCase(updateRule)) {
                  rela.setUpdateRule(DatabaseMetaData.importedKeyCascade);
               }
            }
			// Delete Rule
			String deleteRule = childEle.getAttribute("OnDelete");
			if (deleteRule != null && deleteRule.length() > 0) { // Cascade ist default
      			if ("Restrict".equalsIgnoreCase(deleteRule)) {
      				rela.setUpdateRule(DatabaseMetaData.importedKeyRestrict);
      			} else if ("SetNull".equalsIgnoreCase(deleteRule)) {
                   rela.setUpdateRule(DatabaseMetaData.importedKeySetNull);
      			} else if ("SetDefault".equalsIgnoreCase(deleteRule)) {
                   rela.setUpdateRule(DatabaseMetaData.importedKeySetDefault);
                } else if ("NoAction".equalsIgnoreCase(deleteRule)) {
                   rela.setUpdateRule(DatabaseMetaData.importedKeyNoAction);
                }
			}
		}
		// Parents
		Elements parentEles = ele.getElements(new XPath("[@tabletype='parent']"));
		while (parentEles.hasMoreElements()) {
			Element parentEle = parentEles.next();
			String fk = parentEle.getAttribute("fk");
			JDataTable parentTable = new JDataTable(parentEle);
			this.addParentTable(parentTable, fk);
		}
	}
	/**
	 * Erzeugt einen Clone von der angegebenen Tabelle
	 * @param origTbl
	 */
	JDataTable (JDataSet parent, JDataTable tbl) {
	   //##this.myDataset = parent;
	   this.alias = tbl.alias;
	   this.beanClassName = tbl.beanClassName;
	   this.catalog = tbl.catalog;
	   this.changeProtocol = tbl.changeProtocol;
	   this.comment = tbl.comment;
	   this.databaseName = tbl.databaseName;
	   this.isTransient = tbl.isTransient;
	   this.join = tbl.join;
	   this.readonly = tbl.readonly;
	   this.refname = tbl.refname;
	   this.schema = tbl.schema;
	   this.selfReference = tbl.selfReference;
	   this.tablename = tbl.tablename;
	   this.tableType = tbl.tableType;
	   this.type = tbl.type;
	   this.virtualChild = tbl.virtualChild;
	   // dataColumns
	   this.dataColumns = new LinkedHashMap<String, JDataColumn>();
	   Iterator<JDataColumn> itcol = tbl.getDataColumns();
	   if (itcol != null) {
	      while(itcol.hasNext()) {
	         JDataColumn col = itcol.next();
	         JDataColumn ccol = new JDataColumn(this, col.getColumnName(), col.getDataType());
	         ccol.setAlias(col.getAlias());
	         ccol.setAutoid(col.isAutoid());
	         ccol.setComment(col.getComment());
	         ccol.setDecimalDigits(col.getDecimalDigits());
	         ccol.setDefaultValue(col.getDefaultValue());
	         ccol.setForeignKey(col.isForeignKey());
	         ccol.setKeySeq(col.getKeySeq());
	         ccol.setNullable(col.isNullable());
	         ccol.setPrimaryKey(col.isPrimaryKey());
	         ccol.setReadonly(col.isReadonly());
	         ccol.setSize(col.getSize());
	         ccol.setTransient(col.isTransient());
	         this.addColumn(ccol, true);
	      }
	   }
	 // childRelas
     if (tbl.childRelas != null && tbl.tableType != PARENT_TABLE) {
       for (Relation rela: tbl.childRelas) {
         JDataTable ctbl = rela.getChildTable();
         boolean skip = false;
         if (tbl == this) { // Endlos-Vermeidung
           skip = true;
         }
         // Endlos-Schleife bei self
         if (tbl.isSelfReference() && this.getTablename().equals(tbl.getTablename())) {
             skip = true;
         }
         if (skip == false) {
           JDataTable cctbl = new JDataTable(parent, ctbl);
           Relation newRela = this.addChildTable(rela.getRefName(), cctbl, rela.getFK());
         } // End If skip
       }
     }
	 // parentRelas
     if (tbl.parentRefs != null) {
        for (JDataTable ptbl: tbl.parentRefs) {
            JDataTable pptbl = new JDataTable(parent, ptbl);
            this.addParentTable(pptbl, ptbl.getParentFK());
        }
      }
	}
	// Methods ####################################################
	/**
	 * Macht eine Kopie dieser DataTable.
	 * @param deep wenn true, dann auch Child- und Parent Tables mit kopieren.
	 * @see JDataRow#cloneRow(JDataTable)
	 */
	public JDataTable cloneTable(boolean deep) {
		JDataTable tbl = new JDataTable(this.getElement(deep));
//		if (this.myDataset != null) { //##
//			tbl.setMyDataset(this.myDataset);
//		}
		return tbl;
	}
	/**
	 * Liefert die MetaDaten zu dieser Tabelle als
	 * XML-Element.<p>
	 * Dieses Element kann wieder als Constructor verwendet werden.
	 * @param deep wenn 'true', werden auch Child und Parent Tables
	 * mit verarbeitet.
	 */
	public Element getElement(boolean deep) {
		Element ele = null;
		// Table
		if (alias != null) {
			ele = new Element(alias);
		} else {
			ele = new Element(tablename);
		}
		ele.setAttribute("tablename", tablename);
		if (this.alias != null) {
			ele.setAttribute("alias", alias);
		}
		switch (tableType) {
			case ROOT_TABLE :
					ele.setAttribute("tabletype", "root");
				break;
			case CHILD_TABLE :
					ele.setAttribute("tabletype", "child");
				break;
			case PARENT_TABLE :
					ele.setAttribute("tabletype", "parent");
				break;
		}
		if (this.beanClassName != null) {
			ele.setAttribute("beanclass", this.beanClassName);
		}
		if (this.databaseName != null) {
			ele.setAttribute("DatabaseName", this.databaseName);
		}
		if (this.schema != null) {
			ele.setAttribute("schema", schema);
		}
		if (this.type != null) {
			ele.setAttribute("type", type);
		}
		// Attributes
		if (this.getJoin() != null) {
			ele.setAttribute("join", this.getJoin());
		}
		if (this.isSelfReference() == true) {
			ele.setAttribute("selfReference", "true");
		}
		if (this.isReadonly() == true) {
			ele.setAttribute("readonly", "true");
		}
		if (this.isTransient() == true) {
			ele.setAttribute("transient", "true");
		}
		if (this.isVirtualChild() == true) {
			ele.setAttribute("virtual", this.getVirtualChild());
		}
		if (this.hasChangeProtocol() == true) {
			ele.setAttribute("changeProtocol", this.getChangeProtocol());
		}
		// Comment
		if (this.getComment() != null) {
			ele.addComment(this.getComment());
		}
		// Columns
		if (this.dataColumns != null) {
			for (JDataColumn col: dataColumns.values()) {
				Element colEle = col.getElement();
				ele.addElement(colEle);
			}
		}
		// End Atts JDataTable
		// abhängige Daten
		if (deep) {
			// ChildTables
			if (this.childRelas != null && this.tableType != PARENT_TABLE) {
				for (Relation rela: childRelas) {
					JDataTable tbl = rela.getChildTable();
					boolean skip = false;
					if (tbl == this) { // Endlos-Vermeidung
						skip = true;
					}
					// Endlos-Schleife bei self
					if (this.isSelfReference()) {
						if (this.getTablename().equals(tbl.getTablename())) {
							skip = true;
						}
					}
					if (skip == false) {
						Element tblEle = tbl.getElement(deep);
						if (tbl.getRefname().equalsIgnoreCase(tbl.getTablename()) == false) {
							tblEle.setAttribute("refname", rela.getRefName());
						}
						// FK kann besonders bei readOnly und transienten Tabellen fehlen.
						String s = rela.getFK();
						if (s != null && s.length() > 0) {
						   tblEle.setAttribute("fk", s);
						}
						if (rela.getUpdateRule() == DatabaseMetaData.importedKeyCascade) {
							tblEle.setAttribute("OnUpdate", "Cascade");
						}						
						switch (rela.getDeleteRule()) {
//							case DatabaseMetaData.importedKeyCascade: // default
//								tblEle.setAttribute("OnDelete", "Cascade");
//							break;
							case DatabaseMetaData.importedKeySetDefault:
								tblEle.setAttribute("OnDelete", "SetDefault");
							break;
							case DatabaseMetaData.importedKeySetNull:
								tblEle.setAttribute("OnDelete", "SetNull");
							break;
							case DatabaseMetaData.importedKeyRestrict:
								tblEle.setAttribute("OnDelete", "Restrict");
							break;
							case DatabaseMetaData.importedKeyNoAction:
								tblEle.setAttribute("OnDelete", "NoAction");
							break;
						}
						ele.addElement(tblEle);
					} // End If skip
				}
			}
			// ParentTables
			if (this.parentRefs != null) {
				for (JDataTable tbl: parentRefs) {
					Element tblEle = tbl.getElement(deep);
					if (tbl.getRefname().equalsIgnoreCase(tbl.getTablename()) == false) {
						tblEle.setAttribute("refname", tbl.getRefname());
					}
					// FK kann besonders bei readOnly und transienten Tabellen fehlen.
					String s = tbl.getParentFK();
					if (s != null && s.length() > 0) {
					   tblEle.setAttribute("fk", s);
					}
					ele.addElement(tblEle);
				}
			}
		} else { // !deep
			// ChildTables
			if (this.childRelas != null) {
				for (Relation rela: childRelas) {
					JDataTable tbl = rela.getChildTable();
					Element tblEle = new Element("ChildTable");
					tblEle.setAttribute("name", tbl.getTablename());
					if (tbl.getRefname().equalsIgnoreCase(tbl.getTablename()) == false) {
						tblEle.setAttribute("refname", rela.getRefName());
					}
					// FK kann besonders bei readOnly und transienten Tabellen fehlen.
					String s = rela.getFK();
					if (s != null && s.length() > 0) {
					   tblEle.setAttribute("fk", s);
					}
					ele.addElement(tblEle);
				}
			}
			// ParentTables
			if (this.parentRefs != null) {
				for (JDataTable tbl: parentRefs) {
					Element tblEle = new Element("ParentTable");
					tblEle.setAttribute("name", tbl.getTablename());
					if (tbl.getRefname().equalsIgnoreCase(tbl.getTablename()) == false) {
						tblEle.setAttribute("refname", tbl.getRefname());
					}
					// FK kann besonders bei readOnly und transienten Tabellen fehlen.
					String s = tbl.getParentFK();
					if (s != null && s.length() > 0) {
					   tblEle.setAttribute("fk", s);
					}
					ele.addElement(tblEle);
				}
			}
		}
		return ele;
	}
	/**
	 * Liefert die Definition dieser Tabelle mit ihren Columns als DataSet;
	 * Child und Parent Tables werden mitgeliefert.
	 * @return
	 */
	public JDataSet getMetaDataSet() {
		JDataSet ds = new JDataSet(this.getTablename());
		// Root Table
		JDataTable tblTbl = getMetaDataTable();
		ds.addRootTable(tblTbl);
		// Root Row
		JDataRow rootRow = tblTbl.createNewRow();
		ds.addChildRow(rootRow);
		rootRow.setValue("Tablename", this.getTablename());
		rootRow.setValue("Alias", this.getAlias());
		rootRow.setValue("Schema", this.getSchema());
		// Columns
		JDataTable tblColumn = JDataColumn.getMetaDataTable();
		tblColumn.setTableType(JDataTable.CHILD_TABLE);
		tblTbl.addChildTable(tblColumn, "TableName");
		if (this.dataColumns != null) {
			for (JDataColumn col: dataColumns.values()) {
				JDataRow rowCol = col.getMetaDataRow();
				rootRow.addChildRow(rowCol);
			}
		}
		return ds;
	}
	// Methods
	/**
	 * Fügt der Tabelle eine weitere Spalte hinzu.<p>
	 * Achtung:<br>
	 * Ggf. im DataSet bereits vorhandene DataRows bleiben
	 * unberücksichtigt!
	 */
	public JDataColumn addColumn(String name, int datatype) {
		  if (locked) {
			  String msg = "JDataTable#addColumn: DataTable locked [" + this.tablename + "/" + name + "]";
			  logger.warn(msg);
			  //throw new IllegalArgumentException(msg);
		  }
		if (name == null) {
			throw new IllegalArgumentException("JDataTable#addColumn: Column Name is NULL!");
		}
		this.checkDuplicateColumn(name); // throws IllegalArgumentException wenn duplicate!
		JDataColumn col = new JDataColumn(this, name, datatype);
		if (this.dataColumns == null) {
			dataColumns = new LinkedHashMap<String, JDataColumn>();
		}
		dataColumns.put(col.getColumnName().toLowerCase(), col);
      // Alle die Rows des Dataset um einen DataValue erweitern, die auf diese DataTable zeigen
//      JDataSet myds = this.getMyDataset(); //##
//      if (myds == null) return col;
//      myds.addDataColumn(this, col);
		return col;
	}
	/**
	 * Fügt der Tabelle eine weitere Spalte hinzu.<p>
	 * Achtung:<br>
	 * Ggf. im DataSet bereits vorhandene DataRows bleiben
	 * unberücksichtigt!
	 */
	public void addColumn(JDataColumn col) {
	  this.addColumn(col, false);
	}
	/**
	 * 
	 * @param col
	 * @param quick wenn true, wird nix geprüft.
	 */
  void addColumn(JDataColumn col, boolean quick) {
	  if (col == null) {
		  throw new IllegalArgumentException("JDataTable#addColumn: DataColumn is NULL");
	  }
	  if (locked) {
		  String msg = "JDataTable#addColumn: DataTable locked [" + this.tablename + "/" + col.getColumnName() + "]";
		  logger.warn(msg);
		  //throw new IllegalArgumentException(msg);
	  }
    if (!quick) {
      this.checkDuplicateColumn(col.getColumnName());
    }
    if (this.dataColumns == null) {
      dataColumns = new LinkedHashMap<String, JDataColumn>();
    }
    dataColumns.put(col.getColumnName().toLowerCase(), col);
    if (quick) return;
    // Alle die Rows des Dataset um einen DataValue erweitern, die auf diese DataTable zeigen
//    JDataSet myds = this.getMyDataset(); //##
//    if (myds == null) return;
//    myds.addDataColumn(this, col);
  }
	private void checkDuplicateColumn(String colName) {
		JDataColumn tmpCol = null;
		try {
			tmpCol = this.getDataColumn(colName.toLowerCase());
		} catch (Exception ex) {
			// eine Column mit diesem Namen darf es noch nicht geben!
		}
		if (tmpCol != null) {
			String msg = "JDataTable#checkDuplicateColumn: Duplicate Column Name: " + this.getTablename()+"/"+colName;
			// Muß Aufrufende Methode machen, sonst müllt LogFile voll
			throw new IllegalArgumentException(msg);
		}
	}
	/**
	 * Fügt der Tabelle eine Child-Table hinzu
	 * @param refName
	 * @param tbl
	 * @param keys
	 * @return Die Relation zwischen den beiden Tabellen.
	 */
	public Relation addChildTable(String refName, JDataTable tbl, String keys) {
		  if (locked) {
			  String msg = "JDataTable#addChildTable: DataTable locked [" + this.tablename + "/" + refName + "]";
			  logger.warn(msg);
			  //throw new IllegalArgumentException(msg);
		  }
		if (this.tableType == PARENT_TABLE) {
			logger.warn("JDataTable#addChildTable: Warning! Add Child Table to Parent Table: " + this.tablename+"/"+tbl.getTablename());
		}
		if (tbl == this) {
			logger.warn("JDataTable#addChildTable: Warning! Self Referenced Table: " + this.getTablename());
		}
		Relation rela = new Relation(refName, this, tbl, keys);
		tbl.setTableType(CHILD_TABLE);
		tbl.setMyParentTable(this);
		// Self Reference
		if (tbl.isSelfReference()) {
			tbl.addChildTable(this, keys);
		}
		return rela;
	}
	/**
	 * Fügt eine ChildTable hinzu.<p>
	 * Dieses ist bei Parent Tables nicht erlaubt.
	 * Child Tables sind abhängige Daten;
	 * also z.B. die Ansprechpartner einer Firma,
	 * die Buchungen auf einem Konto.<p>
	 * Die Table Reference muß eindeutig sein.
	 * @param tbl
	 * @param foreignKeys der Foreign Key in der Child-Table der auf diese
	 * Parent Table zeigt. Wenn der Foreign Key aus mehreren Feldern
	 * besteht, dann mit Komma getrennt. Es wird eine Exception geworfen, wenn
	 * die angegebenen Spalten in der Child Table nicht vorhanden sind.
	 * @return Die Relation zwischen den beiden Tabellen.
	 */
	public Relation addChildTable(JDataTable tbl, String foreignKeys) {
		Relation rela = this.addChildTable(tbl.getRefname(), tbl, foreignKeys);
		return rela;
	}

	void addChildRelation (Relation rela) {
	  if (locked) {
		  String msg = "JDataTable#addChildRelation: DataTable locked [" + this.tablename + "]";
		  logger.warn(msg);
		  //throw new IllegalArgumentException(msg);
	  }
		if (this.childRelas == null) {
			this.childRelas = new ArrayList<Relation>();
		}
		for (Iterator<Relation> i = childRelas.iterator(); i.hasNext();) {
			Relation xrela = i.next();
			if (xrela.getRefName().equalsIgnoreCase(rela.getRefName())) {
				String msg = "JDataTable#addChildRealtion: '"+ this.getTablename()+"' Duplicate Child Table Relation Name '"+rela.getRefName()+"'";
				logger.error(msg);
				throw new IllegalArgumentException(msg);
			}
		}
		this.childRelas.add(rela);
	}
	/**
	 * Liefert die Menge der abhängigen Relationen.
	 * @return
	 */
	public ArrayList<Relation> getChildRelations() {
		return this.childRelas;
	}
	public Relation getChildRelation(String refName) {
		if (this.childRelas == null) {
			String msg = "JDataTable#getChildRelation; No Child Relations defined: "+refName;
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		} else {
			for (Relation xrela: childRelas) {
				if (xrela.getRefName().equalsIgnoreCase(refName)) {
					return xrela;
				}
			}
		}
		String msg = "JDataTable#getChildRelation; Missing Child Relation: "+refName;
		logger.error(msg);
		throw new IllegalArgumentException(msg);
	}
	void addParentRelation (Relation rela) {
		if (this.parentRelas == null) {
			this.parentRelas = new ArrayList<Relation>();
		}
		for (Relation xrela: parentRelas) {
			if (xrela.getRefName().equalsIgnoreCase(rela.getRefName())) {
				logger.warn("JDataTable: '" + tablename + "' Duplicate Parent Relation Name: '"+rela.getRefName()+ "'");
				return; // Fehlte! PKÖ 13.4.2008
			}
		}
		this.parentRelas.add((rela));
	}
	/**
	 * Liefert die Menge der übergeordneten Relationen
	 * @return
	 */
	public ArrayList<Relation> getParentRelations() {
		return this.parentRelas;
	}
	public Relation getParentRelation(String refName) {
		if (this.parentRelas == null) {
			String msg = "JDataTable#getParentRelation; No Parent Relations defined: "+refName;
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		} else {
			for (Relation xrela: parentRelas) {
				if (xrela.getRefName().equalsIgnoreCase(refName)) {
					return xrela;
				}
			}
		}
		String msg = "JDataTable#getParentRelation; Missing Parent Relation: "+refName;
		logger.error(msg);
		throw new IllegalArgumentException(msg);
	}
	/**
	 * Fügt eine Parent Table hinzu.<p>
	 * Eine Parent Table ist eine übergeordnete Information;
	 * also z.B. die Funktion einer Person,
	 * der Artikelstamm zu einer Bestellung.
	 * @param tbl die Parent Table
	 * @param keys der Foreign Key in dieser Table der auf die
	 * Parent Table zeigt. Wenn der Foreign Key aus mehreren Feldern
	 * besteht, dann mit Komma getrennt. Es wird eine Exception geworfen, wenn
	 * die angegebenen Spalten in dieser Table nicht vorhanden sind.
	 */
	public void addParentTable(JDataTable tbl, String keys) {
		 if (locked) {
			  String msg = "JDataTable#addParentTable: DataTable locked [" + this.tablename + "/" + tbl.getRefname() + "]";
			  logger.warn(msg);
			  //throw new IllegalArgumentException(msg);
		 }
		tbl.setParentFK(keys); // Provisorisch
		String _refname = tbl.getRefname();
		if (parentRefs == null) {
			parentRefs = new ArrayList<JDataTable>();
		} else {
			for (JDataTable xtbl: parentRefs) {
				if (xtbl.getRefname().equalsIgnoreCase(_refname)) {
					String msg = "JDataTable#addParentTable: Duplicate Parent Table Reference Name '" + _refname + "'";
					logger.error(msg);
					throw new IllegalArgumentException(msg);
				}
			}
		}
		// FK hier kann noch eine Exception geworfen werden!
		// wenn z.B. die FK-Column fehlt!
		if (keys == null) {
			logger.warn("JDataTable#addParentTable: Missing Foreign Key(s)! '"+tbl.getTablename()+"'");
		} else {
			StringTokenizer toks = new StringTokenizer(keys, ",");
			while (toks.hasMoreTokens()) {
				String tok = toks.nextToken();
				if (tok.startsWith(" ")) {
					tok = tok.trim();
				}
				this.getDataColumn(tok); // Prüfen, ob Column vorhanden
				// col.setFK(true)?
			}
		}
		// Add
		parentRefs.add(tbl);
		tbl.setTableType(PARENT_TABLE);
		tbl.setMyParentTable(this);
	}
	/**
	 * Liefert die Child Table mit dem angegebenen (Referenz-)Namen.<p>
	 * Wirft eine IllegalState- oder ArgumentException,
	 * wenn keine Childs vorhanden, oder angegebenen Tabelle nicht
	 * als Child definiert ist.<p>
	 * @param _refname Der Name der Referenz auf die Tabelle
	 * @return
	 */
	public JDataTable getChildTable(String _refname) {
		if (childRelas == null) {
            String msg = "JDataTable#getChildTable: '"
                + this.getTablename()
                + "' Missing Child Table Reference: '"+_refname+"'"
                + " (No Child Tables defined).";
            // 8.3.2008 PKÖ: Fehler hier nicht loggen sondern nur Exception werfen!
			//logger.error(msg);
			throw new IllegalStateException(msg);
		} else {
			for (Relation rela: childRelas) {
				if (rela.getRefName().equalsIgnoreCase(_refname)) {
					JDataTable tbl = rela.getChildTable();
					return tbl;
				}
			}
		}
		String msg = "JDataTable#getChildTable: '"+this.getTablename()+"' Missing Child Table Reference: '"+_refname+"'";
		// logger.error(msg);
		throw new IllegalArgumentException(msg);
	}
	/**
	 * Liefert die Parent Table mit dem angegebenen Name.<p>
	 * Wirft eine IllegalState- oder ArgumentException,
	 * wenn keine Parents vorhanden, oder angegebenen Tabelle nicht
	 * als Parent definiert ist.<p>
	 * @param _refname Der Name der Referenz auf die Tabelle
	 * @return
	 */
	public JDataTable getParentTable(String _refname) {
		if (parentRefs == null) {
			String msg = "JDataTable#getParentTable: '"+this.getTablename()+"' No Parent Tables defined";
			//logger.error(msg);
			throw new IllegalStateException(msg);
		} else {
			for (JDataTable tbl: parentRefs) {
				if (tbl.getRefname().equalsIgnoreCase(_refname)) {
					return tbl;
				}
			}
		}
		String msg = "JDataTable#getParentTable: '"+this.getTablename()+"' Missing Parent Table Reference: '"+_refname+"'";
		//logger.error(msg);
		throw new IllegalArgumentException(msg);
	}
	public boolean hasParentTable(String _refname) {
		if (parentRefs == null) {
			return false;
		}
		for (JDataTable tbl: parentRefs) {
			if (tbl.getRefname().equalsIgnoreCase(_refname)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Erzeugt eine neue leere Zeile.<p>
	 * Diese DataRow hat die Eigenschaft 'inserted'.
	 * Die Spalten werden mit den default-Werten gefüllt.<p>
	 * Anschließend muss diese Zeile an geeigneter Stelle angefügt werden:
	 * @see JDataSet#addRow
	 * @see JDataRow#addChildRow
	 * @see JDataRow#addParentRow
	 * @return
	 */
	public JDataRow createNewRow() {
		JDataRow row = new JDataRow(this);
		row.setInserted(true);
		return row;
	}
	/**
	 * Liefert eine DataColumns dieser Tabelle unter Angabe ihres Namens.<p>
	 * Wirft eine Exception, wenn Column fehlt.<p>
     * Funktioniert hier auch,
     * wenn dem Column.Namen der Name dieser Tabelle mit einem "." vorangestellt wird.<br>
     * "MyTable.MyColumn"
	 * @param name
	 * @return
	 */
	public JDataColumn getDataColumn(String name) {
		JDataColumn col = null;
		name = name.toLowerCase();
		// 1. Mit Namen so-wie-er-kommt probieren
		col = dataColumns.get(name);
		if (col != null)
			return col;
		// 2. @ am Anfang
		if (name.startsWith("@")) { // HACK
			name = name.substring(1);
		}
		// 3. Wenn der Name einer Spalte mit "[Tabellenname]." anfängt,
		// dann bis zum "." abschneiden!
		// PKÖ 4.7.2005
		int poi = name.indexOf('.');
		if (poi != -1) {
			String pref = name.substring(0, poi);
			if (pref.equalsIgnoreCase(this.getTablename())) {
				name = name.substring(poi + 1);
			}
		}
		if (this.dataColumns == null) {
			String msg = "JDataTable#getDataColumn: '" + this.getTablename()
			      + "' No DataColumns defined!";
			// ##logger.error(msg);
			throw new IllegalStateException(msg);
		}
		col = dataColumns.get(name);
		if (col == null) {
			String msg = "JDataTable#getDataColumn: '" + this.getTablename()
			      + "' Missing DataColumn: '" + name + "'";
			// ##logger.error(msg);
			throw new IllegalArgumentException(msg);
		}
		return col;
	}
	/**
	 * Prüft, ob eine DataColumn unter dem angegebenen Namen existiert.
	 * @param name
	 * @return
	 */
	public boolean hasDataColumn (String name) {
	   try {
	      this.getDataColumn(name);
	      return true;
	   } catch (Exception ex) {
	      return false;
	   }
	}
	/**
	 * Der Aliasname der Tabelle oder null, wenn kein Alias
	 * @return
	 */
	public String getAlias() {
		return alias;
	}
	/**
	 * Liefert den Tabellennamen.
	 * @return
	 */
	public String getTablename() {
		return tablename;
	}
	/**
	 * Liefert den voll-qualifizierten Tabellennamen:
	 * MySchema.MyTablename
	 * @return
	 */
	public String getFullTablename() {
	  if (this.schema == null) return getTablename();
	  return this.schema + "." + getTablename();
	}
	/**
	 * Liefert den Aliasname wenn vorhanden; ansonsten den Tabellennamen
	 * @return
	 */
	public String getAliasOrName() {
		if (alias != null) {
			return alias;
		} else {
			return tablename;
		}
	}
	/**
	 * Liefert "myTable AS myAlias"
	 * oder nur "myTable"
	 * @return
	 */
	public String getSelect() {
		if (alias != null) {
			return tablename + " AS " + alias;
		} else {
			return tablename;
		}
	}
	/**
	 * From Database MetaData TABLE_SCHEM
	 * @return
	 */
	public String getSchema() {
		return this.schema;
	}
	/**
	 * Set Database Schema
	 * @param schema
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}
	/**
	 * From DatabaseMetaData TABLE_TYPE ('TABLE', 'VIEW', ...)
	 * @return
	 */
	public String getType() {
		return this.type;
	}
	/**
	 * Set Table Type.<br>
	 * See DatabaseMetaData TABLE_TYPE
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * Liefert den Iterator für Child Tables.<p>
	 * Wenn keine ChildTables vorhanden, wird ein leerer Iterator geliefert.
	 * @return
	 */
	public Iterator<JDataTable> getChildTables() {
		ArrayList<JDataTable> al = this.getChildTableList();
		if( al != null) {
			return al.iterator();
		} else {
			return Collections.EMPTY_LIST.iterator();
		}
	}
	ArrayList<JDataTable> getChildTableList() {
		if (childRelas == null) {
			return null;
		} else {
			ArrayList<JDataTable> al = new ArrayList<JDataTable>();
			for (Relation rela: childRelas) {
				JDataTable child = rela.getChildTable();
				al.add(child);
			}
			return al;
		}
	}
	public boolean hasChildTable(String tablename) {
		if (childRelas == null) {
			return false;
		} 
		for (Relation rela: childRelas) {
			JDataTable child = rela.getChildTable();
			if (child.getRefname().equalsIgnoreCase(tablename)) { 
				return true;
			}
		}
		return false;		
	}
	/**
	 * Prüft, ob die angegebene Tabelle ein abhängige Tabelle ist.
	 * @param tbl
	 * @return Auch false, wenn keine childTables vorhanden
	 */
	public boolean isChildTable(JDataTable tbl) {
		if (childRelas == null) {
			return false;
		} else {
			for (Relation rela: childRelas) {
				JDataTable child = rela.getChildTable();
				if (child.getTablename().equalsIgnoreCase(tbl.getTablename())) { // Nur Namen vergleichen!
					return true;
				}
			}
			return false;
		}
	}
	/**
	 * Prüft, ob die angegebene Tabelle ein abhängige Tabelle ist.
	 * Im Unterscheid zu isParentTable wird hier auf Gleichheit
	 * der Objekte geprüft, und nicht auf den Namen der Tabelle.
	 * Außerdem wird rekursiv die ganze Tiefes des Tabellenbaums weiter durchsucht.
	 * @param tbl
	 * @return Auch false, wenn keine childTables vorhanden
	 */
	public boolean isChildTableObject(JDataTable tbl) {
		if (childRelas == null) {
			return false;
		} else {
			for (Relation rela: childRelas) {
				JDataTable child = rela.getChildTable();
				if (child == tbl) {
					return true;
				} else {
				   if (child.isChildTableObject(tbl) == true) {
				       return true;
				   }
				}
			}
			return false;
		}
	}
	/**
	 * Prüft, ob die angegebene Tabelle mit dieser Tabelle in irgendeiner Form
	 * verknüpft ist (entweder Parent oder Child, Grandparent,...).
	 * @param tbl
	 * @return
	 */
	public boolean isTableObject(JDataTable tbl) {
		if (this.isParentTableObject(tbl)== true) {
			return true;
		} else {
			return this.isChildTableObject(tbl);
		}
	}
	/**
	 * Prüft, ob die angegebene Tabelle ein übergeordnete Tabelle ist.
	 * @param tbl
	 * @return Auch false, wenn keine ParentTables vorhanden.
	 */
	public boolean isParentTable(JDataTable tbl) {
		if (parentRefs == null) {
			return false;
		} else {
			for (JDataTable parent: parentRefs) {
			  if (parent.getJoin() == null) {
  				// Vergleich über Tablename hier richtig?
  				if (parent.getTablename().equalsIgnoreCase(tbl.getTablename())) {
  					return true;
  				}
			  } else {
			    Iterator<JDataTable> pts = parent.getParentTables();
			    if (pts != null) {
			      while(pts.hasNext()) {
			        JDataTable ptbl = pts.next();
		          if (ptbl.getTablename().equalsIgnoreCase(tbl.getTablename())) {
		            return true;
		          }
			      }
			    }
			  }
			}
			return false;
		}
	}
	/**
	 * Prüft, ob die angegebene Tabelle ein übergeordnete Tabelle ist.
	 * Im Unterscheid zu isParentTable wird hier auf Gleichheit
	 * der Objecte geprüft, und nicht auf den Namen der Tabelle.
	 * Außerdem wird rekursiv die ganze Tiefes des Tabellenbaums weiter durchsucht.
	 * @param tbl
	 * @return Auch false, wenn keine ParentTables vorhanden.
	 */
	public boolean isParentTableObject(JDataTable tbl) {
		if (parentRefs == null) {
			return false;
		} else {
			for (JDataTable parent: parentRefs) {
				if (parent == tbl) {
					return true;
				} else {
				   if (parent.isParentTableObject(tbl) == true) {
				      return true;
				   }
				}
			}
			return false;
		}
	}

	/**
	 * Liefert den Iterator für Parent Tables.<p>
	 * Es wird ein leerer Iterator geliefert, wenn
	 * keine ParentTables vorhanden.
	 * @return
	 */
	public Iterator<JDataTable> getParentTables() {
		ArrayList<JDataTable> al = this.getParentTableList();
		if(al != null) {
			return al.iterator();
		} else {
			return Collections.EMPTY_LIST.iterator();
		}
	}
	private ArrayList<JDataTable> getParentTableList() {
		if (parentRefs == null) {
			return null;
		} else {
			ArrayList<JDataTable> al = new ArrayList<JDataTable>();
			for (JDataTable tbl: parentRefs) {
				al.add(tbl);
			}
			return al;
		}
	}

	/**
	 * Liefert den TableType dieser Tabelle:
	 * ROOT, CHILD, PARENT
	 * @return
	 */
	public int getTableType() {
		return this.tableType;
	}

	/**
	 * @param i
	 */
	void setTableType(int i) {
		this.tableType = i;
	}
	/**
	 * Liefert die Namen der Columns
	 * @return
	 */
	public ArrayList<String> getDataColumnNames() {
		ArrayList<String> al = new ArrayList<String>(dataColumns.size());
		Iterator<JDataColumn> it = this.getDataColumns();
		if (it != null) {
			while (it.hasNext()) {
				JDataColumn col = it.next();
				al.add(col.getColumnName());
			}
		}
		return al;
	}
	/**
	 * Liefert den Iterator von DataColumns.
	 * @return null, wenn keine Columns vorhanden.
	 */
	public Iterator<JDataColumn> getDataColumns() {
		if (dataColumns == null) {
			return null;
		} else {
			return dataColumns.values().iterator();
		}
	}
	/**
	 * Liefert die Anzahl der Spalten in dieser Tabelle
	 * @return
	 */
	public int getDataColumnCount() {
	  if (this.dataColumns == null ) return 0;
	  return this.dataColumns.size();
	}
	/**
	 * Liefert die Primary Key Column;
	 * wirft eine Exception, wenn der Primary Key nicht
	 * *genau* eine Spalte hat.
	 * @return
	 */
	public JDataColumn getPKColumn() {
		if (this.dataColumns == null) {
			String msg = "JDataTable#getPKColumn: '"+this.getTablename()+"' No DataColumns defined";
			logger.error(msg);
			throw new IllegalStateException(msg);
		}
		for (JDataColumn col: dataColumns.values()) {
			if (col.isPrimaryKey()) {
				return col; // TODO: wie prüfen, wenn noch mehr PK-Columns?
			}
		}
		String msg = "JDataTable#getPKColumn: '"+this.getTablename()+"' No Primary Key Columns defined";
		logger.error(msg);
		throw new IllegalStateException(msg);
	}
	/**
	 * Liefert einen Iterator über die DataColumns, die den Primary Key bilden.<p>
	 * Wenn kein PK definiert, wird ein leerer Iterator geliefert.
	 * @return
	 */
	public Iterator<JDataColumn> getPKColumns() {
		ArrayList<JDataColumn> al = new ArrayList<JDataColumn>();
		if (this.dataColumns == null) {
			String msg = "JDataTable#getPKColumns '"+this.getTablename()+"' No DataColumns defined";
			logger.error(msg);
			return al.iterator();
		}
		for (JDataColumn col: dataColumns.values()) {
			if (col.isPrimaryKey()) {
				al.add(col);
			}
		}
		if (al.size() == 0) {
			String msg = "JDataTable#getPKColumns: '"+this.getTablename()+"' No Primary Key Columns defined";
			logger.error(msg);
		}
		return al.iterator();
	}
	/**
	 * Liefert die Feldnamen der Primary Key Columns als String mit Komma getrennt.
	 * TODO: [PKÖ] Stimmt denn hier die Reihenfolge der Columns immer?
	 * @return
	 */
	public String getPKs() {
		StringBuilder sb = new StringBuilder();
		for (JDataColumn col: dataColumns.values()) {
			if (col.isPrimaryKey()) {
				sb.append(col.getColumnName());
				sb.append(",");
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
	/**
	 * Liefert die Zahl der Columns die den Primary Key bilden.
	 * @return Wenn 0, dann kein PK definiert
	 */
	public int getPKColumnsCount() {
		int cnt = 0;
		if (this.dataColumns == null) {
			String msg = "JDataTable#getPKColumnsCount: '"+this.getTablename()+"' No DataColumns defined";
			logger.error(msg);
			return 0;
		}
		for (JDataColumn col: dataColumns.values()) {
			if (col.isPrimaryKey()) {
				cnt++;
			}
		}
		return cnt;
	}
	/**
	 * @deprecated
	 * Liefert die Foreign Key Columns für die angegebene Relation
	 */
	public String getFK(String refName) {
		Relation rela = this.getParentRelation(refName);
		return rela.getFK();
	}
	/**
	 * Wird aufgerufen von addParentTable
	 * @param s
	 */
	void setParentFK(String s) {
		this.parentFK = s;
	}
	/**
	 * Bei Parent Tables liefert dieses den Foreign Key, der von
	 * der ChildTable auf Tabelle zeigt.
	 * Wenn mehrere Felder, dann durch Komma getrennt.<p>
	 * TODO : Diese Methode liefert falsche Ergebnisse,
	 * wenn ein Tabellen-Objekt mehrfach als Parent-Table eingesetzt wird.
	 * @return
	 */
	public String getParentFK() {
		return this.parentFK;
	}
	/**
	 * Wenn != null, dann wird diese Parent Table mit JOIN eingelesen.
	 * eingelesen.
	 * @return
	 */
	public String getJoin() {
		return join;
	}

	/**
	 * Die Spalten dieser Parent Table werden als [s] JOIN mit
	 * eingelesen.
	 * @param s
	 */
	public void setJoin(String s) {
		if ("true".equalsIgnoreCase(s)) {
			s = "LEFT OUTER";
		}
		/*
		if (b && this.tableType != PARENT_TABLE) {
			String msg = "JDataTable#setJoin: '"+this.getTablename()+"' Only Parent Tables can set to JOIN Tables";
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}
		*/
		this.join = s;
	}

	/**
	 * Liefert den Referenznamen dieser Tabelle.<p>
	 * Wenn keine Referenz angegeben wird der Tabellenname geliefert.
	 * Siehe Attribut 'refname'.
	 * @return
	 */
	public String getRefname() {
		if (refname == null) {
			return tablename;
		} else {
			return refname;
		}
	}
	/**
	 * @param string
	 */
	public void setRefname(String string) {
		refname = string;
	}
	public static final String META_DATABASE_NAME = "MetaData";
	/**
	 * Liefert eine DataTable mit den Attributen einer DataTable (Uff!).
	 * @return
	 */
	public static JDataTable getMetaDataTable() {
		JDataTable tblTbl = new JDataTable("MetaDataTable");
		tblTbl.setDatabaseName(META_DATABASE_NAME);
		tblTbl.setTransient(true);
		JDataColumn colTablename = tblTbl.addColumn("Tablename", Types.VARCHAR);
		colTablename.setPrimaryKey(true);
		colTablename.setKeySeq(1);
		colTablename.setNullable(false);
		@SuppressWarnings("unused")
		JDataColumn colAlias = tblTbl.addColumn("Alias", Types.VARCHAR);
		JDataColumn colReadOnly = tblTbl.addColumn("ReadOnly", Types.BOOLEAN);
		colReadOnly.setDefaultValue("0");
		JDataColumn colTransient = tblTbl.addColumn("Transient", Types.BOOLEAN);
		colTransient.setDefaultValue("0");
		@SuppressWarnings("unused")
		JDataColumn colVirtual = tblTbl.addColumn("Virtual", Types.VARCHAR);
		@SuppressWarnings("unused")
		JDataColumn colBean = tblTbl.addColumn("Beanclass", Types.VARCHAR);
		@SuppressWarnings("unused")
		JDataColumn colCat = tblTbl.addColumn("Catalog", Types.VARCHAR);
		@SuppressWarnings("unused")
		JDataColumn colSchema = tblTbl.addColumn("Schema", Types.VARCHAR);
		@SuppressWarnings("unused")
		JDataColumn colDb = tblTbl.addColumn("FK_DatabaseName", Types.VARCHAR);
		JDataColumn colSelf = tblTbl.addColumn("selfReference", Types.TINYINT);
		@SuppressWarnings("unused")
		JDataColumn colComment = tblTbl.addColumn("Remarks", Types.VARCHAR);
		colSelf.setDefaultValue("0");
		return tblTbl;
	}
	/**
	 * Liefert eine DataRow mit den Attributen eines JDataTable-Objektes
	 * @return
	 */
	public JDataRow getMetaDataRow() {
		JDataRow row = getMetaDataTable().createNewRow();
		return this.getMetaDataRow(row);
	}
	public JDataRow getMetaDataRow(JDataRow row) {
		row.setValue("Tablename", this.getTablename());
		row.setValue("Alias", this.getAlias());
		row.setValue("ReadOnly", this.isReadonly());
		row.setValue("transient", this.isTransient());
		row.setValue("virtual", this.getVirtualChild());
		row.setValue("Beanclass", this.getBeanClassName());
		row.setValue("Catalog", this.getCatalog());
		row.setValue("Schema", this.getSchema());
		row.setValue("FK_DatabaseName", this.getDatabaseName());
		row.setValue("selfReference", this.isSelfReference());
		row.setValue("Remarks", this.getComment());
		//row.commitChanges();
		return row;
	}
	/**
	 * @return Returns the databaseName.
	 */
	public String getDatabaseName() {
		return databaseName;
	}
	/**
	 * Da TABLE_SCHEM nicht zuverlässig funktioniert,
	 * wird hier der Name der Datenbank eingetragen.
	 * @param databaseName The databaseName to set.
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	/**
	 * @return Returns the selfReference.
	 */
	public boolean isSelfReference() {
		return selfReference;
	}
	/**
	 * Selbst-Referenzierende Tabelle
	 * @param
	 */
	public void setSelfReference(boolean self) {
		this.selfReference = self;
	}
	/**
	 * From Database MetaData TABLE_CAT
	 * @return Returns the TABLE_CAT
	 */
	public String getCatalog() {
		return catalog;
	}
	/**
	 * @param catalog The TABLE_CAT to set.
	 */
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	/**
	 * @return Returns the readonly.
	 */
	public boolean isReadonly() {
		return readonly;
	}
	/**
	 * Die Daten dieser Tabelle werden nur gelesen, aber nie
	 * in die Datenbank zurückgeschrieben.
	 * @param readonly The readonly to set.
	 */
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
	/**
	 * Definiert diese Tabelle als "transient" wenn 'true',
	 * d.h. sie wird vom Persistenz-Layer nie verwendet
	 * also weder aus der Datenbank gelesen, noch in sie
	 * geschrieben.<p>
	 * Alle Spalten einer transiente Tabelle sind gleichfalls
	 * transient.
	 * @param b
	 */
	public void setTransient(boolean b) {
	   this.isTransient = b;
	}
	/**
	 * Liefert die Eigenschaft "transient".
	 * @return
	 */
	public boolean isTransient() {
	   return this.isTransient;
	}
	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}
	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
//	// ModificationObserver
//	private transient ModificationObserver modificationObserver;
//	private transient boolean wasModified = false;
//	public void setModificationObserver(ModificationObserver obs) {
//	  this.modificationObserver = obs;
//	  this.wasModified = false;
//	}
//	public void notifyChanged() {
//    if (this.wasModified == false && modificationObserver != null) {
//      modificationObserver.modificationNotified();
//	    this.wasModified = true;
//	    if (myDataset != null) {
//	      this.myDataset.notifyChanged();
//	    }
//	  }
//	}
	/**
	 * Liefert die DataTable als XML-Element
	 */
	public String toString() {
		return this.getElement(false).toString();
	}
	public static String generateClazz(JDataTable tbl, String pack) {
		StringBuffer buff = new StringBuffer();
		// Package
		if (pack != null) {
			buff.append("package ");
			buff.append(pack);
			buff.append(";\n\n");
		}
		// Comment
		String tblComment = tbl.getComment();
		if (tblComment != null) {
			writeComment(buff, tblComment, "");
		}
		// Class
		buff.append("public class ");
		buff.append(tbl.getTablename());
		buff.append(" {\n");
		// Attributes
		Iterator<JDataColumn> it = tbl.getDataColumns();
		if (it == null) {
			throw new IllegalStateException("Now DataColumns defined");
		}
		while (it.hasNext()) {
			JDataColumn col = it.next();
      String aName = col.getColumnName().toLowerCase();
			String comment = col.getComment();
			if (comment != null) {
				writeComment(buff, comment, "\t");
			}
			buff.append("\tprivate ");
			String type = "String";
			switch (col.getDataType()) {
				case Types.BIT:
				case Types.BOOLEAN:
					type = "boolean";
				break;
				case Types.BIGINT:
					type = "long";
					break;
				case Types.TINYINT: // bool?
				case Types.SMALLINT:
				case Types.INTEGER:
					type = "int";
					break;
				case Types.CHAR:
				case Types.LONGVARCHAR:
				case Types.VARCHAR:
					type = "String";
					break;
				case Types.DATE:
					type = "java.util.Date";
					break;
				case Types.DECIMAL:
				case Types.NUMERIC:
					type = "java.math.BigDecimal";
					break;
				case Types.REAL: // float
				case Types.FLOAT:
				case Types.DOUBLE:
					type = "double";
					break;
				case Types.TIME:
					type = "java.sql.Time";
					break;
				case Types.TIMESTAMP:
					type = "java.sql.Timestamp";
					break;
				case Types.BINARY:
				case Types.VARBINARY:
				case Types.LONGVARBINARY:
					type = "Object";
					break;
			}
			buff.append(type);
			buff.append(" ");
			buff.append(aName);
			buff.append(";\n");
			// Getter
			String mName = aName.substring(0,1).toUpperCase() + aName.substring(1);
			buff.append("\tpublic ");
			buff.append(type);
			if (type.equals("boolean")) {
        buff.append(" is");        
			} else {
			  buff.append(" get");			  
			}
			buff.append(mName);
			buff.append("() {\n");
			buff.append("\t\treturn ");
			buff.append(aName);
			buff.append(";\n\t}\n");
			// Setter
      buff.append("\tpublic void set");
      buff.append(mName);
      buff.append("(");
      buff.append(type);
      buff.append(" " + aName);
      buff.append(") {\n");
      buff.append("\t\tthis.");
      buff.append(aName);
      buff.append(" = " + aName + ";\n\t}\n\n");
		}
		buff.append("}");
		return buff.toString();
	}
	private static void writeComment(StringBuffer buff, String comment, String indent) {
		if (comment == null) return;
		buff.append(indent);
		buff.append("/**\n");
		StringTokenizer toks = new StringTokenizer(comment, "\n");
		while (toks.hasMoreTokens()) {
			String tok = toks.nextToken();
			buff.append(indent);
			buff.append(" * ");
			buff.append(tok);
			buff.append("\n");
		}
		buff.append(indent);
		buff.append(" */");
		buff.append("\n");
	}
	public String getBeanClassName() {
		return beanClassName;
	}
	public void setBeanClassName(String beanClassName) {
		this.beanClassName = beanClassName;
	}
    /**
     * Virtual Child Tables werden nicht in einer wirklichen Datenbank-Tabelle gespeichert, sondern
     * in *einem* Feld der Parent-Tabelle.<p>
     * Dem Dataset ist nicht anzusehen,
     * ob die Child-Tabelle in der Datenbank wirklich existiert oder nicht.
     * @return Returns the virtualChild.
     */
    public boolean isVirtualChild() {
        boolean b = this.virtualChild != null;
        return b;
    }
    /**
     * @param virtualChild The virtualChild to set.
     */
    public void setVirtualChild(String virtualChild) {
        this.virtualChild = virtualChild;
    }
    public String getVirtualChild() {
        return this.virtualChild;
    }
	/**
	 * Liefert den Dataset, zu dem diese Tabelle gehört
	 * oder null, wenn außerhalb eines Dataset.
	 * TODO: Ist das wirklich sichergestellt, daß hier die gewünsche DataSet referenziert wird?
	 * @return Returns the myDataset.
	 */
//	public JDataSet getMyDataset() {
//		if (this.myDataset != null) return this.myDataset;
//		JDataTable tbl = this.getMyParentTable();
//		while (tbl != null) {
//			if (tbl.getMyDataset() != null) {
//				return tbl.getMyDataset();
//			}
//			tbl = tbl.getMyParentTable();
//		}
//		return null;
//	}
//	/**
//	 * @param myDataset The myDataset to set.
//	 */
//	void setMyDataset(JDataSet myDataset) {
//		this.myDataset = myDataset;
//	}
	/**
	 * Liefert die dieser Tabelle übergeordnete Tabelle im Dataset.
	 * Ist bei einer ROOT_TABLE also null.
	 * @see #getMyDataset()
	 * @return Returns the myParentTable.
	 */
	public JDataTable getMyParentTable() {
		return this.myParentTable;
	}
	/**
	 * @param myParentTable The myParentTable to set.
	 */
	void setMyParentTable(JDataTable myParentTable) {
		this.myParentTable = myParentTable;
	}
}
