/*
 * Created on 29.01.2005
 */
package de.pkjs.pltest;

import junit.framework.TestCase;
import de.jdataset.JDataSet;
import de.pkjs.pl.IPLContext;

/**
 * @author peter
 */
public class TestJoin extends TestCase {
   public void testJoin2() {
      try {
         IPLContext pl = AllTests.getPL();
         JDataSet ds = pl.getDataset("TestJoin2", 1);
         ds.getRow();
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }
   public void testJoinPers() {
      try {
         IPLContext pl = AllTests.getPL();
         JDataSet ds = pl.getDataset("TestJoinPers", 1);
         ds.getRowCount();
         //JDataRow row = ds.getRow();
      } catch (Exception ex) {
         fail(ex.getMessage());
      }
   }
}
