package de.guibuilder.framework;

import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * Wird für scrollbare Komponenten wie List, Tree, Memo, Table benötigt.
 */
@SuppressWarnings("serial")
public final class GuiScrollBox extends JScrollPane implements MemberAble {
  // Attributes
  /**
   * Verweis auf die ggf. in der ScrollBox befindliche GuiTable, GuiTree, Panel,....<br>
   * für weiterleiten von MouseEvent an die Tabelle.
   */
  private GuiMember myComponent;
  // Constructors
  /**
   * Erzeugt eine ScrollBox für die zu übergebende Komponente.<BR>
   * Handelt es sich hierbei um eine Tabelle, wird diese intern vermerkt.
   * @see #getGuiTable
   */
  public GuiScrollBox(GuiMember comp) {
    super(comp.getJComponent());
    // Gründe?
    //this.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
    this.guiInit(comp);
  }
  /**
   * für Panels
   */
  public GuiScrollBox(GuiMember comp, int vp, int hp) {
		// @see GuiFactory#perfBeginPanel
    super(comp.getJComponent(), vp, hp);
    this.guiInit(comp);
  }
  private void guiInit(GuiMember comp) {
    this.setName("scrollBox_"+comp.getName());
    myComponent = comp;
    this.setMinimumSize(comp.getMinimumSize()); // Neu: 18.12.2014
//    this.setPreferredSize(comp.getPreferredSize());
//    this.setMaximumSize(comp.getMaximumSize());
    if (comp instanceof GuiTable) {
      this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      this.addMouseListener(new GuiMouseListener(comp)); // Oder so?
    }
  }
  // Methods
  public GuiMember getGuiMember() {
     return myComponent;
  }
  /**
   * Liefert die Tabelle, wenn die ScrollBox eine enthält.
   */
  public GuiTable getGuiTable() {
    if (myComponent instanceof GuiTable) {
      return (GuiTable)myComponent;
    }
    return null;
  }
  public Component getAwtComponent() {
	 return this;
  }
  public String getTag() {
     return myComponent.getTag();
  }
}