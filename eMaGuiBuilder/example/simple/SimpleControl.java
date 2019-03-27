import de.guibuilder.framework.event.GuiUserEvent;
import de.guibuilder.framework.event.GuiChangeEvent;

import de.guibuilder.framework.*;

import electric.xml.Document;

/**
 * Ganz einfacher Beispiel-Controller für ein zur Laufzeit erzeugtes Fenster.
 */
public class SimpleControl {
  /**
   * Constructor
   */
  public SimpleControl() {
    // Fenster aus "Simple.xml" erzeugen.
    try {
	    GuiWindow win = GuiFactory.getInstance().createWindow("Simple.xml");
	    // Ich registriere mich beim Fenster als Controller.
	    win.setController(this);
			// Fenster anzeigen.
			win.show();
    } catch (GDLParseException ex) {
    	GuiUtil.showEx(ex);
    }
  }
  /**
   * MenuItem "new"
   */
  public void new_actionPerformed(GuiUserEvent event) {
    event.window.reset();
  }
  /**
   * MenuItem "save"
   */
  public void save_actionPerformed(GuiUserEvent event) {
    Document doc = event.window.getAllValuesXml();
    System.out.println(doc);
  }
  /**
   * MenuItem "exit"
   */
  public void exit_actionPerformed(GuiUserEvent event) {
    System.exit(0);
  }
  /**
   * Create the AboutBox on the fly.
   */
  public void about_actionPerformed(GuiUserEvent event) {
		try {
	    GuiWindow aboutBox = GuiFactory.getInstance().createWindowXml(
  	    "<?xml version='1.0' encoding='ISO-8859-1'?>"
    	  +"<!DOCTYPE GDL SYSTEM 'gdl.dtd'>"
	      +"<GDL>"
  	    +"<Dialog label='About Simple' w='300' h='190' type='MODAL_NORESIZE'>"
    	    +"<Label label='Java GuiBuilder' an='C' point='14' style='BOLD'/>"
      	  +"<Label label='Version 0.9.7' an='C' point='14' />"
        	+"<Label label='www.guibuilder.de' an='C' point='14' />"
	        +"<Button label='OK'  an='C' it='15' />"
  	    +"</Dialog>"
    	  +"</GDL>");
      	aboutBox.show();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
  }
  /**
   * ComboBox selection changed
   */
  public void selectionChanged(GuiUserEvent event) {
    // Das Casting ist erlaubt, wenn man weiß was man tut.
    GuiChangeEvent cEvent = (GuiChangeEvent)event;
    System.out.println("Selection changed! "+cEvent.component.getName()+" "+cEvent.value+" "+cEvent.index);
  }
}
