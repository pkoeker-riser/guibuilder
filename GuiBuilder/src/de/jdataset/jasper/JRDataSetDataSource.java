/*
 * Created on 16.06.2004
 */
package de.jdataset.jasper;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import de.jdataset.EncodingFilter;
import de.jdataset.EncodingFilter.Encoding;
import de.jdataset.JDataRow;
import de.jdataset.JDataSet;

/**
 * Mithilfe dieser Klasse ist es möglich den JDataSet als Datenquelle für die
 * Erstellung von Berichten mit JasperReports zu benutzen.
 * 
 * @author ikunin
 */
public class JRDataSetDataSource implements JRDataSource {
  private Iterator<JDataRow> itRows;
  private JDataRow row;
  private EncodingFilter filter;
  private Encoding encoding;
  
  /**
   * Erzeugt eine neue Datenquelle für ein Bericht. Die Daten werden in Form von
   * DataSet als Parameter mit übergeben.
   * <p>
   * Der DataSet darf nur eine RootTable haben.
   * 
   * @param dataset
   *          DataSet mit den für Bericht zu verwendenden Daten
   */
  public JRDataSetDataSource(JDataSet dataset) {
    this(dataset, null);
  }

  /**
   * Erzeugt eine neue Datenquelle für ein Bericht. Die Daten werden in Form von
   * DataSet als Parameter mit übergeben.
   * 
   * @param dataset
   *          DataSet mit den für Bericht zu verwendenden Daten
   * @param tablename
   *          Name der zu verwendeten Tabelle
   */
  public JRDataSetDataSource(JDataSet dataset, String tablename) {
    if (tablename != null) {
      itRows = dataset.getChildRows(tablename);
    } else {
      itRows = dataset.getChildRows();
    }
  }
  /**
   * Gibt an, ob es im DataSet noch weitere Datensätze gibt.
   * 
   * @return true, falls weitere Datensätze vorhanden.
   */
  public boolean next() throws JRException {
    boolean hasNext = false;
    if (itRows == null)
      return false;

    hasNext = itRows.hasNext();
    if (hasNext)
      row = itRows.next();
    return hasNext;
  }

  /**
   * Liefert einen Wert aus der aktuellen JDataRow aus aus dem DataSet für das
   * übergebene Feld. Dabei wird der Wert in das Zieldatentyp des Feldes
   * konvertiert.
   * 
   * @param field
   * @return Der Wert des Feldes
   * @exception JRException
   */
  public Object getFieldValue(JRField field) throws JRException {
    Object objValue = null;
    if (field != null && row != null) {
      if (row.getValue(field.getName()) == null)
        return null;

      Class<?> clazz = field.getValueClass();
      try {
        if (clazz.equals(java.lang.Object.class))
          objValue = row.getValue(field.getName());
        else if (clazz.equals(java.lang.Boolean.class))
          objValue = row.getValueBool(field.getName()) ? ((Object) (Boolean.TRUE))
              : ((Object) (Boolean.FALSE));
        else if (clazz.equals(java.lang.Byte.class)) {
          objValue = new Byte((byte) row.getValueInt(field.getName()));
        } else if (clazz.equals(java.util.Date.class)) {
          objValue = row.getValueDate(field.getName());
        } else if (clazz.equals(java.sql.Timestamp.class)) {
          Date d = row.getValueDate(field.getName());
          if (d != null) {
            objValue = new Timestamp(d.getTime());
          } else {
            objValue = null;
          }
        } else if (clazz.equals(java.sql.Time.class)) {
          Date d = row.getValueDate(field.getName());
          if (d != null) {
            objValue = new Time(d.getTime());
          } else {
            objValue = null;
          }
        } else if (clazz.equals(java.lang.Double.class)) {
          objValue = new Double(row.getValueDouble(field.getName()));
        } else if (clazz.equals(java.lang.Float.class)) {
          objValue = new Float(row.getValueDouble(field.getName()));
        } else if (clazz.equals(java.lang.Integer.class)) {
          objValue = new Integer(row.getValueInt(field.getName()));
        } else if (clazz.equals(java.io.InputStream.class)) {
          throw new UnsupportedOperationException(
              "JRDataSetDataSource: Not supported field type: java.io.InputStream!");
        } else if (clazz.equals(java.lang.Long.class)) {
          objValue = new Long(row.getValueLong(field.getName()));
        } else if (clazz.equals(java.lang.Short.class)) {
          objValue = new Short((short) row.getValueInt(field.getName()));
        } else if (clazz.equals(java.math.BigDecimal.class)) {
          objValue = row.getValueBigDecimal(field.getName());
        } else if (clazz.equals(java.lang.String.class)) {
           String sVal = row.getValue(field.getName());
           if (sVal != null && filter != null && encoding != null) {
              sVal = filter.encode(sVal, encoding);
           }
          objValue = sVal;
        }
      } catch (Exception e) {
        throw new JRException("JRDataSetDataSource: Unable to get value for field '" + field.getName()
            + "' of class '" + clazz.getName() + "'!", e);
      }
    }
    return objValue;
  }
  /**
   * @formatter:off
   * Es kann ein Filter für String-Values angegeben werden (z.B. für PDF-Reports)
   * @param filter
   * @param encoding
   * @formatter:on
   */
  public void setEncodingFilter(EncodingFilter filter, Encoding encoding) {
     this.filter = filter;
     this.encoding = encoding;
  }

}
