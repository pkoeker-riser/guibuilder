/*
 * Created on 05.04.2003
 */
package de.pkjs.pl;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;

import de.jdataset.JDataRow;
import de.jdataset.JDataValue;
import de.pkjs.util.Convert;

/**
 * UPDATE myTable SET myField = ?, SET ... WHERE myVal = ? AND ...
 * @author peter
 */
class UpdateStatement extends AbstractStatement {
	// Attribute
	private ArrayList<JDataValue> setValues; // JDataValue
	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(UpdateStatement.class);
	private boolean optimistic; // true, wenn Statement mit Optimistischen Locking
	// Constructor
	UpdateStatement(TableRequest req, String tablename, boolean debug) {
		super(req, tablename);
		if (debug) {
			logger.debug("New UpdateStatement created: "+req.getViewname());
		}
	}
	// Methods
  /**
   * @return Anzahl der geänderten Spalten
   */
  int setValues(JDataRow row, boolean modifiedOnly) {
    this.setValues = new ArrayList<JDataValue>();
    int colCount = 0;
    boolean toAdd = true;
    Iterator<JDataValue> i = row.getDataValues();
    while (i.hasNext()) {
      JDataValue val = i.next();
      if (val.getColumn().isReadonly() || val.getColumn().isTransient()) {
        continue;
      }
      if (modifiedOnly == true) {
        toAdd = val.isModified();
      }
      if (toAdd) {
        colCount++;
        this.addSetValue(val); 
      }
    }
    return colCount;
  }
  /**
   * Wird eigentlich nur für das InsertStatement benötigt
   * @param dbConnection
   * @param row
   * @param modifiedOnly
   * @return
   * @throws PLException
   */
  int addBatch(DatabaseConnection dbConnection, JDataRow row, boolean modifiedOnly) throws PLException {
    int cnt = this.setValues(row, modifiedOnly);
    if (cnt == 0) return 0;
    PreparedStatement ps = getStatement(dbConnection);
    try {
      ps.addBatch();
    } catch (SQLException sex) {
      logger.error(sex);
      throw new PLException(sex);
    }
    return cnt;
  }
    
	private void addSetValue(JDataValue val) {
		this.getSetValues().add(val);
	}

	final ArrayList<JDataValue> getSetValues() {
		return setValues;
	}

	String getSql() {
		StringBuilder buff = new StringBuilder();
		buff.append("UPDATE ");
		buff.append(this.getTablename());
		buff.append(this.getSetColumns());
		buff.append(this.getWhere());
		return buff.toString();
	}
	/**
	 * Liefert den Teil des SQL-Statements für SET bla = ?, blu = ? ...
	 * @return
	 */
	String getSetColumns() {
		StringBuilder buff = new StringBuilder();
		buff.append(" SET ");
		for (int i = 0; i < this.getSetValues().size(); i++) {
			JDataValue val = getSetValues().get(i);
			buff.append(val.getColumnName());
			buff.append(" = ?,");
		}
		if (buff.length() > 1) {
			buff.deleteCharAt(buff.length() -1 );
		}
		return buff.toString();
	}
	PreparedStatement getStatement(DatabaseConnection dbConnection) throws PLException {
		PreparedStatement stmt = super.getStatement(dbConnection);
    this.fillValues(stmt);
		return stmt;
	}
	private void fillValues(java.sql.PreparedStatement ps) throws PLException {
		try {
			int i = 0;
			for (JDataValue col: this.getSetValues()) {
	       i++; // 1-relativ
			  Object o = null;
				// Leerstring zu NULL??
				String sVal = col.getValue();
				String defVal = col.getColumn().getDefaultValue();
				// Default-Value zuweisen, wenn null und default nicht null
				if (sVal == null && defVal != null) {
					sVal = defVal;
				}
				//System.out.print(sVal+":");
				//System.out.println(col.getDataType());
				if (sVal != null && sVal.length() > 0) {
					o = Convert.toObject(sVal, col.getDataType());
					// Wenn bei der Konvertierung etwas schief geht,
					// dann Exception werfen.
					if (Convert.getLastException() != null) {
						throw Convert.getLastException();
					}
				}
				// a. NULL
				if (o == null) {
					ps.setNull(i, col.getDataType());
				// b. BigDecimal (nicht null)
				} else {
					if (o instanceof BigDecimal) {
						ps.setBigDecimal(i, (BigDecimal)o);
					} else {
				    // c. andere Typen (nicht null)				    
					  switch (col.getDataType()) {
            case Types.BIGINT: {
              long l;
              if (o instanceof Long) {
                 l = ((Long)o).longValue();
              } else {
                 l = Convert.toLong(o);
              }
              ps.setLong(i, l);
            }
            break;
            case Types.TIMESTAMP: { // neu PKÖ 30.3.2011
               Timestamp ts = Convert.toDateTime(sVal); 
               ps.setTimestamp(i, ts);
            }
            break;
            case Types.BINARY:
            case Types.BLOB:
            case Types.LONGVARBINARY:
            case Types.VARBINARY: { 
							byte[] binData = (byte[]) col.getObjectValue();
							int len = binData.length;
							ByteArrayInputStream is = new ByteArrayInputStream(binData);
							ps.setBinaryStream(i, is, len);
            }
            break;
            case Types.CLOB: {
							/*
							 * KKN 30.04.2004:
							 * JDTS V8.0 kann CLOB lesen aber nicht speichern.
							 * Darum manchen wir hier einen - langen - String daraus. 
							 */
							ps.setObject(i, o.toString() );
            }
						break;						
            default: {
							// TODO : hier kann eine SqlException auftreten,
							// Wenn der JDBC-Driver den Datentyp nicht kann (z.B. BOOL)
							// Siehe java.sql.Types
							ps.setObject(i, o, col.getDataType());
						}
					} // end switch					
				}
			} // End Set
			} // End For
			int jCnt = 0; // 27.8.2004 PKÖ
			for (int j = 0; j < this.getConditions().size(); j++) {
				SqlCondition cond = this.getConditions().get(j);
				Object val = cond.getValue();
				// 27.8.2004 PKÖ:
				// Wenn der Wert einer Condition null ist, 
				// dann wird ein SQL-Statement mit IS NULL erzeugt, das "?" also weggelassen.
				// Folglich darf ein solcher Wert hier nicht eingetragen werden.
				if (val != null) {
					ps.setObject(i+jCnt+1, val, cond.getDataType()); // 27.8.2004 PKÖ
					jCnt++; // 27.8.2004 PKÖ
				}
			}
		} catch (Exception ex) {
			String msg = "Fatal: UpdateStatement#fillValues "+ex.getMessage();
			logger.error(msg, ex);
			throw new PLException(msg, ex);
		}
	}
	/**
	 * True, wenn Statement optimistisches Locking beinhaltet (... AND Version = 4711 ...)
	 * @return
	 */
  boolean isOptimistic() {
    return optimistic;
  }
  void setOptimistic(boolean optimistic) {
    this.optimistic = optimistic;
  }
}
