import electric.registry.Registry;
import electric.server.http.HTTP;
import java.util.*;

public class Main implements IService {
  private String service = "http://localhost:8004/serv";
  public Main() {
    try {
      HTTP.startup( service );
      Registry.publish( "urn:service", this );
    } catch (Exception ex) {
      ex.printStackTrace();
      // restart?
    }
  }
  public static void main(String[] args) {
    new Main();
  }
  public void restart() {
    try {
      HTTP.shutdown(service);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    this.showDocument("startserver.bat");
    System.exit(0);
  }
  public String sayHello() {
    return "Hello World!";
  }
  public String getDateTime() {
    return new Date().toString();
  }
  public void endlos() {
    while (true) {}
  }
  private void showDocument(String fileName) {
    if (System.getProperty("os.name").startsWith("Windows")) { // Windows
      try {
        Process p = Runtime.getRuntime().exec("rundll32 "+"url.dll,FileProtocolHandler "+fileName);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
