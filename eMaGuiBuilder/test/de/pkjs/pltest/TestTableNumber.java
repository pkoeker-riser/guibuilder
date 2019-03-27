package de.pkjs.pltest;

import de.guibuilder.framework.GDLParseException;
import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiTable;
import de.guibuilder.framework.GuiTableRow;
import de.guibuilder.framework.GuiWindow;
import junit.framework.TestCase;

public class TestTableNumber extends TestCase {
   public void test1() {
      try {
         GuiWindow win = GuiFactory.getInstance().createWindow("tutorial/BeispielTableNumber.xml");
         win.setSystemForm(true);
         win.show();
         GuiTable tbl = win.getRootPane().getCurrentTable();
         GuiTableRow trow = tbl.insertRow(); 
         trow.setValue("number1", 11.44 * 100d);
         trow.setValue("number2", 11.123);
         trow = tbl.insertRow(); 
         trow.setValue("number1", 111.44 * 100.123);
         trow.setValue("number2", 111 );
         trow = tbl.insertRow(); 
         trow.setValue("number1", 1.44 * 106d);
         trow.setValue("number2", 1 );
         trow = tbl.insertRow(); 
         trow.setValue("number1", 22.33 * 105d);
         trow.setValue("number2", 22.229);
         trow = tbl.insertRow(); 
         trow.setValue("number1", 66.4 * 104d);
         trow.setValue("number2", 22.223);
         trow = tbl.insertRow(); 
         trow.setValue("number1", 11.44 * 102d);
         trow.setValue("number2", 112 );
         while(true) {
            try {
               Thread.sleep(3000);
            }
            catch(InterruptedException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }
      catch(GDLParseException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
   }

}
