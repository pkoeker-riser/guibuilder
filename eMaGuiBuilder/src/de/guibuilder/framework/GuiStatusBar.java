package de.guibuilder.framework;

import java.awt.Color;
import java.awt.Font;
/**
Vom GuiLabel abgeleitete Statuszeile zur Darstellung des Statuszeilentextes.
Es wird lediglich die Schriftart auf PLAIN statt BOLD gesetzt und die
Textfarbe auf Schwarz (für Metal-UI).
*/
final class GuiStatusBar extends GuiLabel {
  // Constructor
  GuiStatusBar() {
    this.setForeground(Color.black);
    this.setFont(new Font(this.getFont().getName(), Font.PLAIN, this.getFont().getSize()));
  }
}