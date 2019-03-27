import electric.registry.Registry;
import electric.server.http.HTTP;
import electric.servlet.HTTPContext;
import java.util.*;

public class DateTimeServer {
  public static void main( String[] args ) throws Exception {
    DateTimeServer me = new DateTimeServer();
    me.init();
  }
	private void init() throws Exception {
    HTTPContext context = HTTP.startup( "http://localhost:8004/services" );
		context.setDocBase("/home/peter");
		System.out.println(context.getDocBase());
    Registry.publish( "urn:datetimeserver", this );
	}
  public String getDateTime() {
    return new Date().toString();
  }
}
