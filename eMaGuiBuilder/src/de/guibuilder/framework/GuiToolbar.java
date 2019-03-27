package de.guibuilder.framework;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JToolBar;

/**
 * Implementierung einer Toolbar. <br>
 * Default-Einstellungen:
 * <ul>
 * <li>Nicht verschiebbar
 * <li>Horizontal
 * <li>Kein Tabstop
 * <li>Kein rollover
 * <li>Mit Rahmen
 * </ul>
 */
@SuppressWarnings("serial")
public class GuiToolbar extends JToolBar implements MemberAble {
    // Attributes
    /**
     * Kennzeichen, ob die Komponente mit "tab" erreichbar ist. Default ist
     * "false".
     * @see #setTabstop
     */
    private boolean tabstop = false; // Default von true auf false geändert!
                                     // 8.8.2004 PKÖ
    private String label;
    private ArrayList<GuiElement> tools = new ArrayList<GuiElement>();

    // Constructor
    /**
     * Erzeugt eine horizontale Toolbar die nicht beweglich ist, kein Tabstop und einen Rahmen hat.
     */
    public GuiToolbar(String label) {
        super();
        if (label == null || label.length() == 0) {
            label = "toolbar";
        }
        this.label = label;
        this.setName(GuiUtil.labelToName(label));
        this.setFloatable(false); // Toolbar nicht verschiebbar
        this.setBorderPainted(true); // ein Rahmen
    }

    // Methods
    public final String getTag() {
        return "Toolbar";
    }

    /**
     * Es wird der Toolbar ein Button am Ende hinzugefügt. Der Button "erbt"
     * hierbei die TabStop-Eigenschaften der Toolbar. (Default ist false)
     */
    public final void addGuiTool(GuiElement tool) {
        tool.setTabstop(this.isFocusable());
        super.addImpl(tool.getJComponent(), null, this.getComponentCount());
        tools.add(tool);
    }

    /**
     * Wird "false" übergeben, ist die Komponente nicht mehr mit "Tab"
     * erreichbar.
     */
    public final void setTabstop(boolean b) {
        tabstop = b;
    }

    public final boolean isFocusable() {
        if (tabstop == false) {
            return tabstop;
        }
        return super.isFocusable();
    }

    public Component getAwtComponent() {
        return this;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }
}