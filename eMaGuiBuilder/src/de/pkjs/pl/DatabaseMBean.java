/*
 * Created on 04.05.2005
 */
package de.pkjs.pl;

import java.util.Date;

/**
 * @author PKOEKER
 */
public interface DatabaseMBean {
    public String getEncoding();

    public boolean isDebug();

    /**
     * Liefert den Namen dieser Datenbank.
     * 
     * @return
     */
    public String getDatabaseName();

    /**
     * Liefert den Namen des Feldes, über das das optimistische Locking
     * abgewickelt wird.
     * <p>
     * Wenn eine Tabelle keine Column dieses Namens enthält, dann findet kein
     * optimistisches Locking mit dieser Tabelle statt.
     * 
     * @return
     */
    public String getOptimisticField();

    /**
     * Liefert den Namen des Feldes, in das der Benutzername beim Insert
     * eingetragen werden soll.
     * 
     * @return
     */
    public String getCreateUserField();

    /**
     * Liefert den Namen des Feldes, in das der Benutzername beim Update
     * eingetragen werden soll.
     * 
     * @return
     */
    public String getUpdateUserField();

    public int getDatabaseType();


    /**
     * Liefert die URL über die der JDBC-Trieber auf die Datenbank zugreift.
     * <p>
     * Beispiel: "jdbc:sapdb://myServerName/myDatabaseName"
     * 
     * @return
     */
    public String getDatabaseURL();

    /**
     * Liefert die Klassennamen des JDBC-Treibers.
     * <p>
     * Beispiel: "com.sap.dbtech.jdbc.DriverSapDB"
     * 
     * @return
     */
    public String getJDBCDriver();

    /**
     * @return Returns the layerName.
     */
    public String getLayerName();

    /**
     * @return Returns the transactionLevel.
     */
    public int getTransactionIsolationLevel();

    public boolean isAutocommit();

    public int getMaxActive();

    public int getMinIdle();

    public String getGetSequence();

    public String getSetSequence();

    /**
     * Liefert das heutige Datum im gewählten Format (dd.MM.yyyy).
     * 
     * @see #getDateFormat
     */
    public String getTodayString();

    /**
     * Liefert die aktuelle Uhrzeit im gewähltenFormat (HH:mm).
     * 
     * @see #getTimeFormat
     */
    public String getNowString();

    public String getTodayNowString();

    public void setDebug(boolean state);

    // MBeans #######################################################
    public boolean hasDefaultGetOidStatement();

    public boolean hasDefaultSetOidStatement();

    public int getNumberOfNamedSequences();

    public int getNumberOfNamedStatements();

    public int getNumberOfRequests();

    public int getNumberOfTables();

    public Date getCreatedTimeStamp();

    public Date getResetTimeStamp();
    
    public String getUpdateJournalDirectory();
}