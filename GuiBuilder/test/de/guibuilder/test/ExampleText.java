/*
 * Created on 02.11.2003
 */
package de.guibuilder.test;

import de.guibuilder.framework.GuiText;
import java.awt.Color;
/**
 * @author peter
 */
public class ExampleText extends GuiText {
	public void setSelectionColor(Color c) {
		this.getTextField().setSelectionColor(c);
	}
}
