package de.guibuilder.framework;

import java.awt.Component;

/**
 * Wurzel Interface für GuiMember und alle sonstigen Komponenten.
 */
public interface MemberAble {
	/**
	 * Name der Komponente.
	 * @return
	 */
  public String getName();
  /**
   * Setzt den Namen der Komponente.<p>
   * Es ist eine gute Idee, wenn die Namen der Komponenten eines Windows eindeutig sind.
   * @param name
   */
  public void setName(String name);
  /**
   * Liefert den XML-Element-Tag der Komponente ("Form", "Text", "Panel", ...).
   */
  public String getTag();
  public Component getAwtComponent();
}