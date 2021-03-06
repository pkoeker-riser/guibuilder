/*
 * Created on 17.03.2004
 * TODO: Fehlermeldungen für alle Datenbanken normalisieren: Duplicate, Constraint-Verletzung, Not Found, ...
 */
package de.pkjs.pl;

import java.sql.SQLException;

import de.jdataset.JDataRow;

/**
 * Serializable Exception des PL für den Client 
 * @author ikunin
 */
public class PLException extends Exception {
  private static final long serialVersionUID = -3420418324812691983L;
  
  private String additionalMessage;
  private String sqlState;
  private int vendorCode;
  private boolean optimistic;
  private JDataRow row;

  /**
   * 
   */
  public PLException() {
    super();
  }

  /**
   * @param message
   */
  public PLException(String message) {
    super(message);
  }
  /**
   * 
   * @param message
   * @param optimistic Wenn true, dann wurde Exception (vermutlich) durch OL verursacht.
   * @param row Der Datensatz, dessen Update schief gegangen ist
   */
  PLException(String message, boolean optimistic, JDataRow row) {
    this(message);
    this.optimistic = optimistic;
    this.row = row;
  }

  /**
   * @param cause
   */
  public PLException(Throwable cause) {
    // Änderung 22.12.2007 PKÖ: 
    // Exception-Objecte des Servers nicht an den Client weiterreichen!
    super(cause.getMessage());
    this.analyseCause(cause);
  }
  
  /**
   * Wenn schon PLEx: dann nicht weiter verpacken
   * @param cause
   */
  PLException(PLException cause) {
    super(cause);
    this.additionalMessage = cause.getAdditionalMessage();
    this.sqlState = cause.getSQLState();
    this.optimistic = cause.isOptimistic();
    this.row = cause.getRow();
  }
  /**
   * Wenn schon PLEx: dann nicht weiter verpacken
   * @param message
   * @param cause
   */
  public PLException(String message, PLException cause) {
    super(message);
    this.additionalMessage = cause.getAdditionalMessage();
    this.sqlState = cause.getSQLState();
    this.optimistic = cause.isOptimistic();
    this.row = cause.getRow();
    this.vendorCode = cause.getErrorCode();
  }

  /**
   * @param message
   * @param cause
   */
  public PLException(String message, Throwable cause) {
     super(message);
     if (cause instanceof PLException) {
        PLException pex = (PLException)cause;
        this.additionalMessage = pex.getAdditionalMessage();
        this.sqlState = pex.getSQLState();
        this.optimistic = pex.isOptimistic();
        this.row = pex.getRow();
        this.vendorCode = pex.getErrorCode();
     } else {
    // Änderung PKÖ 27.8.2006: 
    // Exception-Objecte des Servers nicht an den Client weiterreichen!
    this.analyseCause(cause);
  }
  }

  private void analyseCause(Throwable cause) {
    if (cause instanceof SQLException) {
      SQLException sex = (SQLException)cause;
      this.sqlState = sex.getSQLState();
      this.vendorCode = sex.getErrorCode();
      while (sex.getNextException() != null ) { // && sex != sex.getNextException()
        additionalMessage += '\n' + sex.getNextException().getLocalizedMessage(); 
        sex = sex.getNextException();
      }
    }
  }
  
  /**
   * Hier überschrieben für Messages von SQL-Exceptions
   */
  public String getLocalizedMessage() {
    String msg = super.getMessage();
    if (additionalMessage != null) {
      msg +=  '\n' + "NextException: " + '\n' + additionalMessage;
      return msg;
    }
    if (this.sqlState != null) {
      msg += '\n' + "SQLStatus: [" + this.sqlState +"]";
    }
    return msg;
  }
  /**
   * Von {@link SQLException#getSQLState()}
   * @return
   */
  public String getSQLState() {
    return sqlState;
  }
  /**
   * Von {@link SQLException#getErrorCode()}
   * @return
   */
  public int getErrorCode() {
    return vendorCode;
  }
  public String getAdditionalMessage() {
    return this.additionalMessage;
  }
  /**
   * true, wenn dieser Fehler durch Optimistisches Locking verursacht wurde
   * @return
   */
  public boolean isOptimistic() {
    return optimistic;
  }

  void setOptimistic(boolean optimistic) {
    this.optimistic = optimistic;
  }
  /**
   * Liefert die Zeile mit dem Lockkonflikt
   * @return
   */
  public JDataRow getRow() {
    return row;
  }
  /**
   * Versucht anhand des sqlState zu ermitteln, ob ein Unique Constraint verletzt ist.
   * @return
   */
  public boolean isDuplicate() {
   // TODO: Wie den Datenbank-Typ rausfinden?
    if ("23505".equals(sqlState) // Postgres 
        || "23000".equals(sqlState) // MySql+Oracle
        || "9".equals(sqlState)) // Hsqldb
      return true;
    else 
      return false;
  }
}
