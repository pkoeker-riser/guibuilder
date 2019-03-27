package de.pkjs.pl;

import java.util.ArrayList;
import java.util.StringTokenizer;

import net.sf.ehcache.Cache;
import net.sf.ehcache.config.CacheConfiguration;
import de.jdataset.JDataSet;
import de.pkjs.util.Convert;
import electric.xml.Element;

class RequestCacheConfig {
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger
      .getLogger(RequestCacheConfig.class);

  private String cacheName;
  private boolean enabled;

  boolean isEnabled() {
    return enabled;
  }

  /**
   * Dieser Cache wird gelöscht, wenn eine der hier aufgeführten Tabellen
   * verändert werden.
   */
  String remove;
  /**
   * Bei NamedStatement: Die im Statement verwendeten Tabellen.<br>
   * Wenn ein DataSet zurückgeschrieben wird, der eine dieser Tables enthält,
   * ist diese Cache zu löschen.
   */
  String tables;
  ArrayList<String> getTablenames() {
    ArrayList<String> al = new ArrayList<String>();
    if (tables != null) {
      StringTokenizer toks = new StringTokenizer(tables, ",;");
      while(toks.hasMoreTokens()) {
        al.add(toks.nextToken().toLowerCase());
      }
    }
    return al;
  }
  /**
   * Es werden nur die Datasets im Cache vorgehalten, wie weniger Rows
   * enthalten.
   */
  private int maxDatasetRows = 1000;
  private Cache cache;
  

  RequestCacheConfig(String name, Element ele) {
    this.cacheName = name;
    if (ele != null) {
      String sEnabled = ele.getAttribute("enabled");
      enabled = Convert.toBoolean(sEnabled);
      remove = ele.getAttribute("remove");
      String s = ele.getAttribute("maxDatasetRows");
      if (s != null) {
        maxDatasetRows = Integer.parseInt(s);
      }
      tables = ele.getAttribute("tables");
      try {
        // CacheConfig
        CacheConfiguration cfg = new CacheConfiguration();
        cfg.setName(cacheName);
        // Properties
        long maxElesMem = 10000;
        String maxElementsInMemory = ele.getAttribute("maxElementsInMemory");
        if (maxElementsInMemory != null) {
          maxElesMem = Convert.toLong(maxElementsInMemory);
        }
        cfg.setMaxEntriesLocalHeap(maxElesMem);
        String maxElementsOnDisk = ele.getAttribute("maxElementsOnDisk");
        if (maxElementsOnDisk != null) {
          cfg.setMaxEntriesLocalDisk(Convert.toLong(maxElementsOnDisk));
        }
        long time2idl = 120;
        String timeToIdleSeconds = ele.getAttribute("timeToIdleSeconds");
        if (timeToIdleSeconds != null) {
          time2idl = Convert.toLong(timeToIdleSeconds);
        }
        cfg.setTimeToIdleSeconds(time2idl);
        long time2live = 120;
        String timeToLiveSeconds = ele.getAttribute("timeToLiveSeconds");
        if (timeToLiveSeconds != null) {
          time2live = Convert.toLong(timeToLiveSeconds);
        }
        cfg.setTimeToLiveSeconds(time2live);
        // Create Cache
        cache = new Cache(cfg);
        // add Cache
        PL.getCacheManager().addCache(cache);
      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      }
    }
  }

  void put(String key, JDataSet ds) {
    if (key == null || ds == null)
      return;
    if (ds.getRowCount() > maxDatasetRows)
      return;
    net.sf.ehcache.Element cele = new net.sf.ehcache.Element(key, ds);
    cache.put(cele);
  }

  void put(long oid, JDataSet ds) {
    if (ds == null)
      return;
    if (ds.getRowCount() > maxDatasetRows)
      return;
    net.sf.ehcache.Element ele = new net.sf.ehcache.Element(oid, ds);
    cache.put(ele);
  }

  JDataSet get(String key) {
    net.sf.ehcache.Element cele = cache.get(key);
    return this.get(cele);
  }

  JDataSet get(long oid) {
    net.sf.ehcache.Element cele = cache.get(oid);
    return this.get(cele);
  }

  private JDataSet get(net.sf.ehcache.Element cele) {
    if (cele != null) {
      Object val = cele.getObjectValue();
      JDataSet datasetFromCache = (JDataSet) val;
      JDataSet clonedDs = datasetFromCache.getClone();
      return clonedDs;
    } else {
      return null;
    }
  }

  void removeAll() {
    cache.removeAll();
  }

  public String toString() {
    return cacheName;
  }
}
