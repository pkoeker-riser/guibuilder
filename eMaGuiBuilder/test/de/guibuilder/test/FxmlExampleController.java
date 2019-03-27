package de.guibuilder.test;

import de.guibuilder.framework.GDLParseException;
import de.guibuilder.framework.GuiFactory;
import de.guibuilder.framework.GuiWindow;
import de.guibuilder.framework.event.GuiUserEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class FxmlExampleController {
   
   public static void main(String[] args) {
      GuiFactory fact = GuiFactory.getInstance();
      GuiWindow win;
      try {
         win = fact.createWindow("gui/JFXTest1.xml");
         FxmlExampleController me = new FxmlExampleController();
         win.setController(me);
         win.show();
      }
      catch(GDLParseException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      new FxmlExampleController();
   }
   
   public FxmlExampleController() {
      // WARNING!!! Hier nicht GuiFactory aufrufen!!
   }
   
   @FXML private Text actiontarget;
   
   @FXML protected void handleSubmitButtonAction(ActionEvent event) {
       actiontarget.setText("Sign in button pressed");
   }
   
   // Swing
   public void open(GuiUserEvent event) {
      System.out.println("Open");
   }

   public void neu(GuiUserEvent event) {
      System.out.println("Neu");
   }
}
