package de.guibuilder.test.framework;

import junit.framework.TestCase;
import de.guibuilder.framework.GDLParseException;
import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiWindow;

public class GuiBrowserTest extends TestCase {
   public void test1() {
      try {
          GuiWindow win = GuiFactory.getInstance().createWindow("tutorial/Browser.xml");
          win.setController(this);
          win.show();
      } catch (GDLParseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
  }
   public static void main (String[] args) {
      GuiBrowserTest me = new GuiBrowserTest();
      me.test1();
  }

}
