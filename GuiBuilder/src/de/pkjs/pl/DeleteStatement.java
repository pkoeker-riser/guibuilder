/*
 * Created on 07.04.2003
 */
package de.pkjs.pl;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import de.jdataset.JDataRow;
import de.jdataset.JDataValue;
import de.pkjs.util.Convert;

/**
 * DELETE FROM myTable WHERE myKey = ?
 * @author peter
 */
final class DeleteStatement extends AbstractStatement {
    private static final Logger logger = Logger.getLogger(DeleteStatement.class);
    private boolean optimistic; // true, wenn Statement mit Optimistischen Locking

	// Constructor
	DeleteStatement(TableRequest req, String tablename) {
		super(req, tablename);
		if (this.getMyRequest().isDebug()) {
			logger.debug("New DeleteStatement created: "+req.getViewname());
		}
	}
	String getSql() {
		StringBuilder buff = new StringBuilder();
		buff.append("DELETE FROM ");
		buff.append(this.getTablename());
		buff.append(this.getWhere());
		return buff.toString();
	}
	/**
	 * Für Delete WHERE pk1 = ? AND pk2 = ?...
	 * @param row
	 * @param stmt
	 */
	void fillPKValues(DatabaseConnection dbConnection, JDataRow row) throws PLException {
	   int cnt = 1; // 1-Relativ
	   // Condition ggf aus optimistic Locking
      for (int j = 0; j < this.getConditions().size(); j++) {
         SqlCondition cond = this.getConditions().get(j);
         Object val = cond.getValue();
         // Wenn der Wert einer Condition null ist, 
         // dann wird ein SQL-Statement mit IS NULL erzeugt, das "?" also weggelassen.
         // Folglich darf ein solcher Wert hier nicht eingetragen werden.
         if (val != null) {
            try {
               this.getStatement(dbConnection).setObject(cnt, val, cond.getDataType()); 
               cnt++; // 27.8.2004 PKÖ
            } catch(SQLException e) {
               String msg = "PL [ " + myRequest.getLayerName() + " ] Error executing fillPKValues : " + e.getMessage();
               logger.error(msg, e);
               throw new PLException(msg, e);                
            }
         }
      }
		// Wenn das mehrere Felder sind, stimmt dann hier die Reihenfolge?
		// TODO Leider nein! (z.B. bei adrsslgw)
		// Das geht schief, wenn in der Definition die Felder in der
		// falschen Reihenfolge angegeben werden
		ArrayList<JDataValue> pal = row.getPrimaryKeyValues();
		for (Iterator<JDataValue> i = pal.iterator(); i.hasNext();) {
			JDataValue val = i.next();
			int pktype = val.getDataType();
			Object o = Convert.toObject(val.getValue(), pktype);
			//System.out.println(val.getColumnName());
			try {
				if (o == null) {
					this.getStatement(dbConnection).setNull(cnt, pktype);
				} else {
					this.getStatement(dbConnection).setObject(cnt, o, pktype);					
				}
				cnt++;
			}
			catch(SQLException e) {
			   String msg = "PL [ " + myRequest.getLayerName() + " ] Error executing fillPKValues : " + e.getMessage();
	         logger.error(msg, e);
	         throw new PLException(msg, e);				    
			}
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
