import electric.xml.*;
import electric.xml.io.*;

import electric.registry.Registry;
import electric.server.http.HTTP;
//import electric.console.Console;
import electric.util.log.Log;

import electric.webserver.WebServer;

import java.util.Vector;
import java.io.File;

import de.guibuilder.server.GuiUserEventIF;

public class GlueServer {
  // Atributes
  private String host = "http://127.0.0.1:8004/glue";
  private boolean debug;
  // Constructor
  public GlueServer() {
  }
  // Methods
  public static void main( String[] args ) throws Exception {
    Log.startLogging( "EXCEPTION" );
    Log.startLogging( "ERROR" );
    //Log.startLogging( "HTTP" );
    Mappings.readMappings("standard.map");

    GlueServer me = new GlueServer();
    me.guiInit(args);
  }
  private void guiInit(String[] args) throws Exception {
    // Config
    Document config = new Document(new File("config.xml"));
    System.out.println(config.toString());
    Element root = config.getRoot();
    Element webservice = root.getElement("Webservice");
    Element guibuilder = root.getElement("GuiBuilder");
    // Webservice Parameter
    String host = webservice.getElement("Host").getTextString();
    String service = webservice.getElement("Service").getTextString();
    // Webserver
    try {
      Element webserver = root.getElement("Webserver");
      String docbase = webserver.getElement("Docbase").getTextString();
      String welcome = webserver.getElement("Welcome").getTextString();
      // Start WebServer
      WebServer webServer = new WebServer( "http://localhost:80" );
      webServer.setDocbase( docbase ); // could be an absolute path
      webServer.addWelcomeFile( welcome );
      webServer.startup(); // start accepting HTTP messages
      System.out.println( "Webserver started on http://localhost:80" );
    } catch (Exception ex) {
      System.err.println(ex.getMessage());
      System.out.println("No Webserver startet");
    }
    // Guibuilder
    String guibuilderDocbase = guibuilder.getElement("Docbase").getTextString();
    String database = guibuilder.getElement("Database").getTextString();
    // Debug
    String sDebug = root.getElement("Debug").getTextString();
    debug = Boolean.valueOf(sDebug).booleanValue();
    HTTP.startup( host );
    GlueServlet servlet = new GlueServlet(guibuilderDocbase, database);
    servlet.setDebug( debug );
    // publish an instance
    Registry.publish( service, servlet, GuiUserEventIF.class );

  }
}
