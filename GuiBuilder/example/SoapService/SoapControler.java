import electric.registry.Registry;
import de.guibuilder.framework.*;
import de.guibuilder.framework.event.*;

public class SoapControler {
  private static SoapControler me = new SoapControler();
  private IService srv;
  private SoapControler() {}
  public static SoapControler getInstance() {
    return me;
  }
  public void bind(GuiUserEvent event) {
    try {
      srv = (IService)Registry.bind("http://localhost:8004/serv/urn:service.wsdl", IService.class);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  public void restart(GuiUserEvent event) {
    srv.restart();
  }
  public void endlos(GuiUserEvent event) {
    srv.endlos();
  }
  public void sayHello(GuiUserEvent event) {
    String h = srv.sayHello();
    GuiWindow win = event.window;
    win.getRootPane().getMainPanel().setValue("ausgabe", h);
  }
  public void getDateTime(GuiUserEvent event) {
    String d = srv.getDateTime();
    GuiWindow win = event.window;
    win.getRootPane().getMainPanel().setValue("ausgabe", d);
  }
}
