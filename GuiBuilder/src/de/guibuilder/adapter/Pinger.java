package de.guibuilder.adapter;

/**
 * Dieser Thread ruft nach einer einstellbaren Zeit die Methode "ping()" auf.
 * Dieser Thread läuft mit niedrigster Priorität.<br>
 * Die Klasse ist singleton.<br>
 * über getInstance(Pingable) kann dieser Thread
 * erzeugt und anschließend gestartet werden.<br>
 * Es muß ein Objekt übergeben werden, welches das Interface "Pingable" implementiert.<p>
 * Dieser Pinger wird von ThinClientAdapter verwendet, um den Server "anzupingen".<br>
 * Dieser hat nun die Chance, auf dieses Ereignis zu reagieren (etwa indem der Benutzer
 * über eingegangene Mails informiert wird, oder hierüber festzustellen,
 * ob der Client noch lebt).<p>
 * Per Default wird einmal pro Minute ein "ping" ausgelöst;
 * dieses kann mit setPingSleep(anzahlSekunden) auch geändert werden.
 * @see Pingable
 * @see ThinClientAdapter
 */
public final class Pinger extends Thread {
  // Attributes
  private static volatile Pinger me;
  /**
   * 60 000ms = 1Min
   */
  private static int pingSleep = 60000;
  private volatile Pingable controller;
  private static boolean brun = true;
  // Constructor
  private Pinger(Pingable p) {
    controller = p;
  }
  // Methods
  /**
   * Liefert den Pinger und startet ihn, falls noch nicht geschehen.
   */
  public static Pinger getInstance(Pingable p) {
    if (me == null) {
    	synchronized(Pinger.class) {
    	   if (me == null) {
    		me = new Pinger(p);
    		me.setPriority(MIN_PRIORITY);
    	   }
    	}
    }
    brun = true;
    return me;
  }
  /**
   * Vorsicht!<br>
   * Liefert u.U. null, wenn der Pinger nicht instanziiert wurde!
   */
  public static Pinger getInstance() {
    return me;
  }
  /**
   * @param sec Ping-Sleep in Sekunden.
   * @throws IllegalArgumentException, wenn < 0.
   */
  public static void setPingSleep(int sec) {
    if (sec < 1) {
      throw new IllegalArgumentException("PingSleep must be greater then 0!");
    }
    pingSleep = sec * 1000;
  }
  /**
   * Liefert Ping-Sleep in Sekunden
   */
  public static int getPingSleep() {
    return pingSleep / 1000;
  }
  /*
  public void setPingable(Pingable p) {
    controller = p;
  }
  public Pingable getPingable() {
    return controller;
  }
  */
  public void run() {
    // erzwingt schlafen...ping...schlafen...
    try {
      sleep(pingSleep);
    } catch (InterruptedException e) {
      // nix
    }
    while (brun) {
      controller.ping();
      try {
        sleep(pingSleep);
      } catch (InterruptedException e) {
        // nix
      }
    }
  }
  /**
   * hält den Pinger an.
   */
  public void stopRun() {
    brun = false;
  }
}