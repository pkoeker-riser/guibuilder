import javax.swing.UIManager;

import de.guibuilder.adapter.GuiAPI;

public class Main {

  /**
   * Die Anwendung konstruieren
   */
  public Main() {
    // Controller erzeugen
    SimpleControl ctrl = new SimpleControl();

  }
  /**
   * Main-Methode
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    new Main();
  }
}
