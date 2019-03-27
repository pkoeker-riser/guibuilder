package de.guibuilder.framework;

import java.util.Vector;
/**
 * hält Referenzen auf alle von der Factory erzeigte Fenster und Dialoge.
 * Die Formulare selbst müssen sich hier wieder austragen.
 * @author peter
 */
public class GuiWindowManager {
  private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GuiWindowManager.class);

  private static GuiWindowManager me;

  private Vector<GuiWindow> windows;
  
  private GuiWindowManager()  {
    windows = new Vector<GuiWindow>();
  }
  
  public static GuiWindowManager getInstance() {
    if (me == null) {
      synchronized (GuiWindowManager.class) {
        if (me == null) {
            me = new GuiWindowManager();
        }
      }
    }
    return me;
  }
  
  public boolean addWindow(GuiWindow win) {
    if (win == null) return false;
    synchronized (windows) { 
      if (windows.contains(win)) {
        logger.warn("Duplicate Object added: " + win.getTitle());
        return false;
      } else {
        logger.debug("Window created: " + win.getTitle());
        return windows.add(win);
      }
    }
  }
  
  public boolean removeWindow(GuiWindow win) {
    if (win == null) return false;
    synchronized (windows) { 
      boolean removed = windows.remove(win);
      if (removed) {
        logger.debug("Window removed: " + win.getTitle());
      } else {
        logger.warn("Window remove (missing): " + win.getTitle());
      }
      return removed;
    }
  }
  
  public int getSize() {
    synchronized (windows) { 
      return windows.size();
    }
  }
  
  public boolean isEmpty() {
    return getSize() == 0;
  }
  
  public boolean hasChanges() {
    synchronized (windows) { 
      for (GuiWindow win:windows) {
        if (win.getMainPanel().isModified()) return true;
      }      
      return false;
    }
  }
}
