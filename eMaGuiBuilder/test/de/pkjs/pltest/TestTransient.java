/*
 * Created on 05.02.2005
 */
package de.pkjs.pltest;

import de.jdataset.JDataRow;
import de.jdataset.JDataSet;
import de.jdataset.JDataTable;
import de.pkjs.pl.PL;
import junit.framework.TestCase;

/**
 * @author peter
 */
public class TestTransient extends TestCase {
   public void test1() {
      try {
         PL pl = AllTests.getPL();
         JDataSet ds = pl.getEmptyDataset("Transient1");
         JDataTable tblArtikel = ds.getDataTable();
         JDataRow rowArtikel = tblArtikel.createNewRow();
         ds.addChildRow(rowArtikel);
         JDataTable tblKunde = tblArtikel.getChildTable("Kunde");
         JDataRow rowKunde = tblKunde.createNewRow();
         rowArtikel.addChildRow(rowKunde);
         rowKunde.setValue("KundenName", "Müller");
         System.out.println(ds);
      } catch (Exception ex) {
         fail(ex.getMessage());
      }
   }
}
