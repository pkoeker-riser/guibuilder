package de.guibuilder.framework;

import javax.swing.JComponent;

import javafx.embed.swing.JFXPanel;

/**
 * Panel für Integration Swing --> JFX
 * @author pkoeker
 */
public class GuiJFXPanel extends GuiPanel {
   private String name;
   protected JFXPanel jfxPanel = new JFXPanel();

   public JFXPanel getJFXPanel() {
      return jfxPanel;
   }

   public void setJFXPanel(JFXPanel panel) {
      this.jfxPanel = panel;
   }

   public JComponent getJComponent() {
      return jfxPanel;
   }

   protected void guiInit() {
      //jfxPanel = new JFXPanel();
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

}
