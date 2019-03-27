/*
 * Created on 23.11.2004
 */
package de.pkjs.pltest;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import de.pkjs.pl.IPLContext;
import de.pkjs.pl.PL;
import de.pkjs.pl.PLException;

/**
 * @author ikunin
 */
public class ConnectionPoolTest extends TestCase {
   /**
    * Logger for this class
    */
   private static final Logger logger = Logger.getLogger(ConnectionPoolTest.class);

   private int okCounter = 0;
   private Object okSema = "x"; // okCounter++ nicht atomar!!
   private int errorCounter = 0;
   private Object errSema = "y";
   private int fin;
   //private Object finSema = "z";
   private String error;

   private PL plAll;
   //private static final String TRANS_NAME = "TransactionTest";
   private long startTime;
   private long endTime;

   public ConnectionPoolTest() {
      DOMConfigurator.configure("Log4JTestConnection.xml");
      plAll = AllTests.getPL();
   }

   public void setUp() throws Exception {
      logger.info("setUp");
      this.startTime = System.currentTimeMillis();
   }

   public void tearDown() throws Exception {
      logger.info("tearDown");
      //p.shutdown();
      // p.reset();
   }

   private void addError(String errorText) {
      if (error == null) {
         error = errorText + "\n";
      } else {
         error += errorText + "\n";
      }
   }

   private void print(String label, int totalRunsOK, int totalRunsError, IPLContext pl) {
      /*
      System.out.println("\n**** Connection Pool Info ****");
      System.out.println("* Name: \t" + label);
      System.out.println("* OK Count: \t" + totalRunsOK);
      System.out.println("* Error Count: \t" + totalRunsError);
      System.out.println("* MaxActive: \t" + pl.getMaxActive());
      System.out.println("* NumActive: \t" + pl.getNumActive());
      System.out.println("* MaxIdle: \t" + pl.getMaxIdle());
      System.out.println("* MinIdle: \t" + pl.getMinIdle());
      System.out.println("* ----------------------------");
      System.out.println("* Active: \t" + pl.getNumActive());
      System.out.println("* Idle: \t" + pl.getNumIdle());
      System.out.println("******************************\n");
      */
   }

   public void testConnectionPool() {
      int maxThreads = 80;
      int sleepTime = 20; // unused
      int runCounts = 5;
      int totalRuns = runCounts * maxThreads * 2 ;

      okCounter = 0;
      errorCounter = 0;

      print("Start", totalRuns, 0, plAll);
      //int sum = 0;
      for (int i = 0; i < maxThreads; i++) {
         Worker w = new Worker(i*2, sleepTime, runCounts);
         new Thread(w).start();
//         int size = w.getSize();
//         sum = sum + size;
         try {
             Thread.sleep(10);
          } catch (InterruptedException e) {}
          WorkerAuto w1 = new WorkerAuto(i*2+1, sleepTime, runCounts);
          new Thread(w1).start();
//          int size1 = w1.getSize();
//          sum = sum + size1;
         try {
            Thread.sleep(10);
         } catch (InterruptedException e) {}
      }
      do {
         try {
            Thread.sleep(1000);
         } catch (InterruptedException e) {

         }
         System.err.println("--> RunCount: " + (okCounter + errorCounter) + " Must run: "
               + totalRuns);
      } while ((okCounter + errorCounter) < totalRuns);
      this.endTime = System.currentTimeMillis();

      if (errorCounter != 0) {
         System.err.println("Errors: "+errorCounter);
         logger.error(error);
         fail(error);
      }
      long duration = endTime - startTime;
      System.out.println("Dauer: " + duration/1000);
      System.out.println("Je Sekunde: " + totalRuns/(duration/1000));
   }

   class Worker implements Runnable {
      //private int sleepTime;
      private int runCount;
      //private int workerNR;
      private String name;
      private String nextAction;
      
      Worker(int workerNR, int sleepTime, int runCount) {
         //this.workerNR = workerNR;
         //this.sleepTime = sleepTime;
         this.runCount = runCount;
         name = "Worker " + workerNR;
      }
      
      public void run() {
         logger.debug("Worker started: "+name);
         long start = System.currentTimeMillis();
         for (int i = 0; i < runCount; i++) {
            IPLContext pl = null;
            String transname = this.name + Integer.toString(i);
            try {
               nextAction = "-1-startTrans";
               //logger.debug(nextAction);
               pl = plAll.startNewTransaction(transname);
               nextAction = "-2-get1";
               print(name+nextAction, okCounter, errorCounter, pl);
               //logger.debug(nextAction);
               pl.getDataset("Schlagworte", 1);
               nextAction = "-3-get2";
               print(name+nextAction, okCounter, errorCounter, pl);
               //logger.debug(nextAction);
               pl.getDataset("AdresseKurz", 2);
               nextAction = "-4-commitTrans";
               //  JDataSet dsAutoCommit = p.getDataset("AdresseKurz", 2);
               print(name+nextAction, okCounter, errorCounter, pl);
               //logger.debug(nextAction);
               pl.commitTransaction(transname);
               print(name+nextAction, okCounter, errorCounter, pl);
               synchronized(okSema) {
                   okCounter++;
               }
            } catch (Exception ex) {
               String rollbackError = null;
               synchronized(errSema) {
                   errorCounter++;
                   //System.exit(1);
               }
               System.err.println("Next Action: "+nextAction);
               ex.printStackTrace();
               try {
                  if (pl != null) {
                     pl.rollbackTransaction(transname);
                  }
               } catch (PLException e) {
                  rollbackError = e.getMessage();
               }

               String s = ex.getMessage();
               if (rollbackError != null) {
                  s += " " + "Error during rollback: " + rollbackError;
               }
               addError(s+nextAction);
            }
         } // End For
         long dauer = System.currentTimeMillis() - start;
         fin++;
         System.out.println(name+" "+Thread.currentThread()+" Dauer: "+dauer + " / finished: "+fin);
      }
	public int getSize() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();
			return baos.size();
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}

   }
   class WorkerAuto implements Runnable {
      //private int sleepTime;
      private int runCount;
      //private int workerNR;
      private String name;
      private String nextAction;

      WorkerAuto(int workerNR, int sleepTime, int runCount) {
         //this.workerNR = workerNR;
         //this.sleepTime = sleepTime;
         this.runCount = runCount;
         name = "WorkerAuto " + workerNR;
      }

      public void run() {
         logger.debug("Worker started: "+name);
         PL pl = AllTests.getPL();
         long start = System.currentTimeMillis();
         for (int i = 0; i < runCount; i++) {
            try {
               nextAction = "a-get1";
               print(name+nextAction, okCounter, errorCounter, pl);
               //logger.debug(nextAction);
               pl.getDataset("AdresseSuchen", 1);
               nextAction = "a-get2";
               print(name+nextAction, okCounter, errorCounter, pl);
               //logger.debug(nextAction);
               if (i == 2) {
                 pl.getDataset("AdresseError", 2000);
               } else {
                 pl.getDataset("AdresseKomplett", 2);
               }
               //  JDataSet dsAutoCommit = p.getDataset("AdresseKurz", 2);
               synchronized(okSema) {
                   okCounter++;
               }
            } catch (Exception ex) {
               synchronized(errSema) {
                   errorCounter++;
                   //System.exit(1);
               }
               System.err.println("Next Action: "+nextAction);
               ex.printStackTrace();
               logger.error(nextAction, ex);
               addError(nextAction);
            }
         } // End For
         long dauer = System.currentTimeMillis() - start;
         fin++;
         System.out.println(name+" "+Thread.currentThread()+" Dauer: "+dauer + " / finished: "+fin);
      }
	public int getSize() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();
			return baos.size();
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}

   }

   public static void main(String[] argv) {
      TestRunner.run(ConnectionPoolTest.class);
   }

}

