
package de.jdataset;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;

/**
 * Eine Liste von Name-Value-Pairs für parametrisierte Abfragen.<p>
 * Bei der Definition von Zugriffen in der Konfigurationsdatei
 * kann das Attribut "where" mit einer Bedingung für die
 * Datenbankselektion je Tabelle versehen werden.<br>
 * Neben konstanten Werten ("WHERE Status = 1") dürfen auch
 * Named Parameter definiert werden, die erst zur Laufzeit
 * mit Inhalt gefüllt werden: "WHERE Status = $Status".<p>
 * Auf dieser Art wird eine variable "Status" definiert,
 * deren Wert im Zusammenhang mit einer Abfrage gesetzt weden muß.
 * <code><pre>
 * ...
 * RootTable tablename="MyTable" pk="oid" where="Name1 LIKE $name AND Vorname = $vorname"
 * ...
 * PL pl = new PL();
 * ...
 * ParameterList lst = new ParameterList();
 * lst.add(new NVPair("Name", "Müller%"));
 * lst.add(new NVPair("Vorname", "Rudi"));
 * JDataSet ds = pl.getDataset("MyRequestName", lst);
 * </pre>
 * </code>
 */
public final class ParameterList implements Serializable {
	// Attributes
  private int maxRows = Integer.MAX_VALUE; // default
  private int queryTimeout = Integer.MAX_VALUE; // default
  private int maxExecutionTime = Integer.MAX_VALUE; // default
  private int isolationLevel = -1;
  private boolean batchUpdate;

  public boolean isBatchUpdate() {
	  return batchUpdate;
  }
  /**
   * Hiermit wird beim PL ein batch-Update freigeschaltet; 
   * es wird davon ausgegangen, daß bei allen Zeilen einer Tabelle 
   * ein identisches Update-Statement verwendet werden (nämlich das des ersten Datensatzes).  
   * @param batchUpdate
   */
  public void setBatchUpdate(boolean batchUpdate) {
	  this.batchUpdate = batchUpdate;
  }

private ArrayList<NVPair> list = new ArrayList<NVPair>();
	
	static final long serialVersionUID = 583598472846340400L;
	// Methods
	/**
	 * Die Anzahl Parameter in der Liste
	 */
	public int size() {
	   return list.size();
	}
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.<p>
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(NVPair nv) {
		if (list.contains(nv)) {
			throw new IllegalArgumentException("ParameterList#addParameter Duplicate Parameter Name: "+nv.getName());
		}
		this.list.add(nv);
	}
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, String value) {
		NVPair nv = new NVPair(name, value, Types.VARCHAR);
		this.addParameter(nv);
	}
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, String value, int dataType) {
		NVPair nv = new NVPair(name, value, dataType);
		this.addParameter(nv);
	}
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, Object value) {
		NVPair nv = new NVPair(name, value, getSQLDataType(value));
		this.addParameter(nv);
	}
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.<br>
	 * Achtung!<br>
	 * Es gibt datenbankspezifische Grenzen für die Größe der List! (MaxDB ca. 1000)
	 * @param name
	 * @param value Hier eine Liste von Werten für Statements wie ... WHERE ID IN (?) ...
	 */
  public void addParameter(String name,  List<?> value) {
    int type = Types.BIGINT; // DEFAULT
    if (value.size() > 0) {
       Object firstval = value.get(0);
       type = getSQLDataType(firstval);
    }
    NVPair nv = new NVPair(name, value, type);
    this.addParameter(nv);
  }
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, boolean value) {
		NVPair nv = new NVPair(name, Boolean.valueOf(value), Types.BOOLEAN);
		this.addParameter(nv);
	}
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, byte value) {
		NVPair nv = new NVPair(name, new Byte(value), Types.TINYINT);
		this.addParameter(nv);
	}
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, short value) {
		NVPair nv = new NVPair(name, new Short(value), Types.SMALLINT);
		this.addParameter(nv);
	}
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, int value) {
		NVPair nv = new NVPair(name, new Integer(value), Types.INTEGER);
		this.addParameter(nv);
	}
    public void addParameter(String name, int[] value) {
       NVPair nv = new NVPair(name, value, Types.INTEGER);
       this.addParameter(nv);
   }
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, float value) {
		NVPair nv = new NVPair(name, new Float(value), Types.FLOAT);
		this.addParameter(nv);
	}
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, double value) {
		NVPair nv = new NVPair(name, new Double(value), Types.DOUBLE);
		this.addParameter(nv);
	}
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, long value) {
		NVPair nv = new NVPair(name, new Long(value), Types.BIGINT);
		this.addParameter(nv);
	}
    public void addParameter(String name, long[] value) {
       NVPair nv = new NVPair(name, value, Types.BIGINT);
       this.addParameter(nv);
   }
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, char value) {
		NVPair nv = new NVPair(name, new Character(value), Types.CHAR);
		this.addParameter(nv);
	}
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, BigInteger value) {
		NVPair nv = new NVPair(name, value, Types.DECIMAL);
		this.addParameter(nv);
	}
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, BigDecimal value) {
		NVPair nv = new NVPair(name, value, Types.DECIMAL);
		this.addParameter(nv);
	}
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, Date value) {
		NVPair nv = new NVPair(name, value, Types.DATE);
		this.addParameter(nv);
	}
	
	/**
	 * Fügt ein neues Name-Value-Pair der ParameterListe hinzu.
	 * Wirft eine IllegalArgumentException, wenn der Name doppelt vergeben wird.
	 */
	public void addParameter(String name, Timestamp value) {
		NVPair nv = new NVPair(name, value, Types.TIMESTAMP);
		this.addParameter(nv);
	}
	
	/**
	 * Setzt den Value eines Name-Value-Pairs neu.
	 * @param name Ein der Liste bereits zugewiesener Name
	 * @param value Der neue Value für das Pair
	 */
	public void setParameter(String name, String value) {
		NVPair nv = this.getParameter(name);
		nv.setValue(value);
	}
	/**
	 * Liefert den Iterator über die NVPairs
	 * @return
	 */
	public Iterator<NVPair> iterator() {
		return list.iterator();
	}
	/**
	 * Liefert das NVPair unter dem angegebenen Name.
	 * Wirft eine IllegalArgumentException, 
	 * wenn es unter dem Namen keinen Parameter gibt.
	 * @param name
	 * @return
	 */
	public NVPair getParameter(String name) {
		Iterator<NVPair> i = this.iterator();
		while (i.hasNext()) {
			NVPair nv = i.next();
			if (nv.getName().equalsIgnoreCase(name)) {
				return nv;
			}
		}
		throw new IllegalArgumentException("ParameterList#getParameter Missing Parameter: "+name);
	}
	
	public int getSQLDataType(Object value)
	{ // TODO: value ist List<Type>
//	   if (value instanceof List) {
//	      List list = (List)value;
//	      value = list.get(0);
//	   }
		if(value instanceof String)
			return Types.VARCHAR;
		else if(value instanceof Character)
			return Types.CHAR;
		else if(value instanceof Boolean)
			return Types.BOOLEAN;
		else if(value instanceof Byte)
			return Types.TINYINT;
		else if(value instanceof Short)
			return Types.SMALLINT;
		else if(value instanceof Integer)
			return Types.INTEGER;
		else if(value instanceof Long)
			return Types.BIGINT;
		else if(value instanceof Float)
			return Types.FLOAT;
		else if(value instanceof Double)
			return Types.DOUBLE;
		else if(value instanceof BigInteger)
			return Types.DECIMAL;
		else if(value instanceof BigDecimal)
			return Types.DECIMAL;
		else if(value instanceof Date)
			return Types.DATE;
		else if(value instanceof Time)
			return Types.TIME;
		else if(value instanceof Timestamp)
			return Types.TIMESTAMP;
		else if(value instanceof byte[])
			return Types.BLOB;
		else
			throw new IllegalArgumentException("getSQLDataType: No correspondig mapping to SQL Type for datatype: " + value.getClass().getName());		
	}
   public int getMaxRows() {
      return maxRows;
   }

   public void setMaxRows(int maxRows) {
      this.maxRows = maxRows;
   }

   public int getQueryTimeout() {
      return queryTimeout;
   }
   /**
    * JDBC Query Timeout in Sekunden (harte Grenze; es wird bei Überschreitung eine Exception geworfen)
    * HSQLDB kann kein Query Timeout
    * MaxDB kennt zwar die Eigenschaft, kümmert sich aber nicht drum :-(
    * https://bugs.eclipse.org/bugs/show_bug.cgi?id=326503
    * @param queryTimeout
    */
   public void setQueryTimeout(int queryTimeout) {
      this.queryTimeout = queryTimeout;
   }

   public int getMaxExecutionTime() {
     return maxExecutionTime;
  }
  
  /**
   * Weiche Grenze; es wird bei Überschreitung ein Eintrag in das Slow Query Log geschrieben
   * @param maxExecutionTime
   */
  public void setMaxExecutionTime(int maxExecutionTime) {
     this.maxExecutionTime = maxExecutionTime;
  }
  public int getIsolationLevel() {
    return isolationLevel;
  }
  /**
   * Es wird für diese Transaction ein vom Default abweichender TransactionIsolationLevel gesetzt
   * @param isolationLevel
   */
  public void setIsolationLevel(int isolationLevel) {
    this.isolationLevel = isolationLevel;
  }

   public String toString() {
     StringBuilder buff = new StringBuilder();
     for (NVPair p:list) {
       buff.append(p.toString());
       buff.append(' ');
     }
     return buff.toString();
   }

}
