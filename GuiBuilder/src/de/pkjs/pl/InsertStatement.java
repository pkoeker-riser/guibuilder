/*
 * TODO Nur die Spalten aufführen, die nicht NULL sind?
 * Created on 08.04.2003
 */
package de.pkjs.pl;

import java.util.Iterator;

import de.jdataset.*;
/**
 * INSERT INTO (col1, col2, ...) VALUES (?, ?, ...)
 * @author peter
 */
final class InsertStatement extends UpdateStatement {
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(InsertStatement.class);
	// Constructor
	InsertStatement(TableRequest req, String tablename) {
		super(req, tablename, false);
		if (this.getMyRequest().isDebug()) {
			logger.debug("New InsertStatement created: "+req.getViewname());
		}
	}
	// Methods
	String getSql() {
		StringBuilder buff = new StringBuilder();
		buff.append("INSERT INTO ");
		buff.append(this.getTablename());
		buff.append(this.getSetColumns());
		buff.append(this.getWhere());
		return buff.toString();
	}
	String getSetColumns() {
		StringBuilder buffCol = new StringBuilder();
		StringBuilder buffVal = new StringBuilder();
		buffCol.append(" (");
		buffVal.append(") VALUES (");
		for (Iterator<JDataValue> it = this.getSetValues().iterator(); it.hasNext();) {
			JDataValue val = it.next();
			buffCol.append(val.getColumnName());
			buffCol.append(",");
			// Values
			buffVal.append("?,");
		}
		if (buffCol.length() > 1) {
			buffCol.deleteCharAt(buffCol.length() -1 );
		}
		if (buffVal.length() > 1) {
			buffVal.deleteCharAt(buffVal.length() -1 );
		}
		buffVal.append(") ");
		return buffCol.toString() + buffVal.toString();
	}
}
