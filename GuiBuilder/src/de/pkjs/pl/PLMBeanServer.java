package de.pkjs.pl;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;


/**
 * Registriert MBeans für den Persistenz Layer
 * @author peter
 */
public class PLMBeanServer {
  private final static Logger logger = Logger.getLogger(PLMBeanServer.class);

  private static MBeanServer server;
  
  static MBeanServer getServer() {
    return server;
  }
  
  public PLMBeanServer(PL pl) {
    this(pl, "de.pkjs.pl." + pl.getLayerName());
  }
  
	public PLMBeanServer(PL pl, String url) {
		try {
			server = ManagementFactory.getPlatformMBeanServer();
			ObjectName name1 = new ObjectName(url + ":type=PL");
			try {
				server.unregisterMBean(name1);
			} catch (Exception ex) {
				logger.debug("Cannot unregister MBean: " + name1);
			}
			server.registerMBean(pl.getMBeanPL(), name1);

			ObjectName name2 = new ObjectName(url + ":type=DB");
			try {
				server.unregisterMBean(name2);
			} catch (Exception ex) {
				logger.debug("Cannot unregister MBean: " + name1);
			}
			server.registerMBean(pl.getMBeanDB(), name2);

			logger.debug("MBean-Server registered: " + url);
		} catch (Exception ex) {
			logger.error(url, ex);
		}
	}
}
