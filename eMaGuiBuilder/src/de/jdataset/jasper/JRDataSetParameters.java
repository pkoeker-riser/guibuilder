package de.jdataset.jasper;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataValue;

/**
 * Mithilfe dieser Klasse ist es möglich den JDataSet als Datenquelle für die
 * Erstellung von Parametern für Berichte mit JasperReports zu benutzen.
 * <p>
 * Die hiermit erstellte Map darf nachträglich verändert werden (etwa mittels
 * #put). Diese Änderungen haben jedoch keine Auswirkungen auf den
 * zugrundeliegenden DataSet.
 * <p>
 * Achtung!<br>
 * Die Namen der Parameter sind - nicht mehr! - Case-sensitiv
 * 
 * @author ikunin
 */
public class JRDataSetParameters implements Map<String, Object> {
	private JDataRow row;
	private Map<String, Object> map;

	/**
	 * Erzeugt die Parameter aus einem DataSet.<br>
	 * Der DataSet muß genau eine DataTable und genau eine DataRow enthalten.
	 */
	public JRDataSetParameters(JDataSet dataset) {
		this(dataset, null);
	}

	/**
	 * Erzeugt die Parameter unter Angabe einer definierten RootTable aus dem
	 * DataSet. Zu dieser Tabelle muß genau eine DataRow für die
	 * Berichts-Parameter existieren.
	 * (Wenn mehr als eine Row wird nur die erste verwendet und der Rest ignoriert)
	 * Die Spaltennamen werden hier in kleinbuchstaben transformiert;
	 * im Report selbst müssen die Parameter folglich auch in Kleinbuchstaben angegeben werden.
	 * @param dataset
	 * @param tablename
	 */
	public JRDataSetParameters(JDataSet dataset, String tablename) {
		if (dataset == null)
			throw new IllegalArgumentException("DataSet is null!");
		Iterator<JDataRow> it = null;
		if (tablename != null) {
			it = dataset.getChildRows(tablename);
		} else {
			it = dataset.getChildRows();
		}

		if (it == null) {
			throw new IllegalArgumentException("Empty DataSet!");
		}

		this.row = it.next();

		map = new LinkedHashMap<String, Object>();
		Iterator<JDataValue> itDataValues = row.getDataValues();
		while (itDataValues.hasNext()) {
			JDataValue dw = itDataValues.next();
			String columnName = dw.getColumnName();
			Object value = dw.getValueObject();
			map.put(columnName.toLowerCase(), value);
		}
		// Default-Locale
		map.put("report_locale", Locale.getDefault());
	}
	/**
	 * überschreibt Local.getDefault()
	 * @param loc
	 */
	public void setReportLocale(Locale loc) {
		map.put("report_locale", loc);
	}
	/**
	 * IS_IGNORE_PAGINATION
	 * @param b
	 */
	public void setIgnorePagination(boolean b) {
		map.put("is_ignore_pagination", b);
	}
	/**
	 * @see java.util.Map#size()
	 */
	public int size() {
		return map.size();
	}

	/**
	 * Not implemented
	 */
	public void clear() {
		throw new IllegalStateException("Not implemented!");
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
    if (key instanceof String) {
      key = ((String)key).toLowerCase();
    }
		return map.containsKey(key);
	}

	/**
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	/**
	 * @see java.util.Map#values()
	 */
	public Collection<Object> values() {
		return map.values();
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
   public void putAll(Map<? extends String, ? extends Object> _map) {
		this.map.putAll(_map);
   }

	/**
	 * @see java.util.Map#entrySet()
	 */
	public Set<Map.Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	public Set<String> keySet() {
		return map.keySet();
	}

	/**
	 * Liefert den Wert des Parameters unter dem angegebnen Key
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
	  if (key instanceof String) {
	    key = ((String)key).toLowerCase();
	  }
		return map.get(key);
	}

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public Object remove(Object key) {
    if (key instanceof String) {
      key = ((String)key).toLowerCase();
    }
		return map.remove(key);
	}

	/**
	 * Manuelles setzen von Parametern
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public Object put(String key, Object value) {
    if (key instanceof String) {
      key = ((String)key).toLowerCase();
    }
		return map.put(key, value);
	}



}
