/*
 * Created on 04.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.pkjs.pl;

/**
 * @author PKOEKER
 */
public interface PLTransactionContextMBean {
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkjs.pl.IPL#getCurrentDatabaseName()
	 */public String getCurrentDatabaseName();

	public boolean isDebug();

	// public synchronized void abortTransaction() throws PLException {
	public String getDatasetDefinitionFileName();

	// public String getDatabaseMetaData();

	public String pingDatabase() throws PLException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkjs.pl.IPLContext#getTodayString()
	 */public String getTodayString();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkjs.pl.IPLContext#getNowString()
	 */public String getNowString();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkjs.pl.IPLContext#getTodayNowString()
	 */public String getTodayNowString();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkjs.pl.IPLContext#setDebug(boolean)
	 */public void setDebug(boolean state);

	public int getNumActive();

	public int getNumIdle();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkjs.pl.IPLContext#getMaxActive()
	 */public int getMaxActive();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkjs.pl.IPLContext#getMaxIdle()
	 */public int getMaxIdle();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkjs.pl.IPLContext#getMinIdle()
	 */public int getMinIdle();

	/**
	 * @return Returns the abortedTransactions.
	 */
	public long getAbortedTransactions();

	/**
	 * @return Returns the commitedTransactions.
	 */
	public long getCommitedTransactions();

	/**
	 * @return Returns the rollbackedTransaktions.
	 */
	public long getRollbackedTransaktions();

	/**
	 * @return Returns the startedTransactions.
	 */
	public long getStartedTransactions();

	/**
	 * @return Returns the createdTimeStamp.
	 */
	public java.util.Date getCreatedTimeStamp();

	/**
	 * @return Returns the resetTimeStamp.
	 */
	public java.util.Date getResetTimeStamp();
}