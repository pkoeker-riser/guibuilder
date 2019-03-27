package de.guibuilder.test.framework;

import de.guibuilder.framework.GDLParseException;
import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiWindow;
import de.guibuilder.framework.event.GuiUserEvent;
import junit.framework.TestCase;

public class GuiTableTest extends TestCase {
   public void test1() {
      try {
         GuiWindow win = GuiFactory.getInstance().createWindow("tutorial/BeispielTable.xml");
         win.setController(this);
         win.show();
         int xxx = 0; 
         xxx++;
      }
      catch(GDLParseException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
   }
   
   public void rowClicked(GuiUserEvent event) {
      System.out.println("SelectionChanged");
   }
}
