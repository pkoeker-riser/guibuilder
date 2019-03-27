import java.awt.*;
import java.awt.event.*;
import java.applet.*;

import electric.registry.Registry;

public class Applet1 extends Applet {

  /**Das Applet konstruieren*/
  public Applet1() {
  }
  /**Das Applet initialisieren*/
  public void init() {
    try {
      DateTimeServerIF server = (DateTimeServerIF)Registry.bind("http://localhost:8004/glue/urn:datetimeserver.wsdl", DateTimeServerIF.class);
      System.out.println(server.getDateTime());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  /**Applet-Information holen*/
  public String getAppletInfo() {
    return "Applet-Information";
  }
  /**Parameter-Infos holen*/
  public String[][] getParameterInfo() {
    return null;
  }
}
