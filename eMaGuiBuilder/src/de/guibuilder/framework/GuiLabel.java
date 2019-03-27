package de.guibuilder.framework;

import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import de.pkjs.util.Convert;

/**
 * Implementierung eines Labels.
 * <p>
 * Die Ausrichtung des Textes ist rechtsbündig. <BR>
 * Enthält die Bezeichnung des Labels das Zeichen "%" wird der nächste Buchstabe
 * als Accelerator für die diesem Label zugeordnete Komponente verwendet. Soll
 * das Zeichen "%" in der Bezeichnung enthalten sein, ist "%%" anzugeben.
 */
public class GuiLabel extends GuiComponent implements TableColumnAble {
    // Attributes
    protected JLabel component;

    // Constructors
    /**
     * Konstruktor für GuiStatusBar
     */
    public GuiLabel() {
        super();
    }

    /**
     * Erzeugt ein Label mit dem übergebenen Text, der gleichzeitig als Name der
     * Komponente verwendet wird.
     */
    public GuiLabel(String label) {
        super(label);
        guiInit(label);
    }

    /**
     * Constructor mit der Angabe einer Komponente zu diesem Label
     * (JLabel.setLabelFor).
     */
    public GuiLabel(GuiComponent comp, String title) {
        super(title);
        this.getJLabel().setLabelFor(comp.getJComponent());
        guiInit(title);
    }

    /**
     * setText, setMnemonic (wenn "%"), setHorizontalAlignment, setName
     */
    private final void guiInit(String title) {
        this.setText(title);
        setLabel(title);
        char mnemo;
        int p = title.indexOf("%");
        if (p != -1 && p + 1 < title.length()) {
            mnemo = title.charAt(p + 1);
            title = title.substring(0, p) + title.substring(p + 1);
            this.setText(title);
            if (mnemo != '%') {
                this.setMnemonic(mnemo);
                this.component.setDisplayedMnemonicIndex(p);
            }
        }
        String name = GuiUtil.labelToName(title);
        this.setHorizontalAlignment(SwingConstants.RIGHT);
        this.setName("guiLabel_" + name);
    }

    public final String getTag() {
        return "Label";
    }

    /**
     * Liefert JLabel
     */
    public final JComponent getJComponent() {
        return this.getJLabel();
    }

    public JLabel getJLabel() {
        if (component == null)
            component = new JLabel();
        return component;
    }

    public final int getDataType() {
        return STRING;
    }
    
    public void click(MouseEvent e) {
       if (this.actionClick != null) {
           GuiRootPane root = this.getRootPane();
           if (root != null) {
               root.obj_Click(this, this.actionClick, this.getValue(), e);
           }
       }
    }
    
    /**
     * Macht hier naturgemäß nix.
     */
    public final void reset() {
    }

    /**
     * @see #setText
     */
    public final void setValue(Object val) {
        if (val instanceof String) {
            setText((String) val);
        } else {
           this.setText(Convert.toString(val));
        }
    }

    public Object getUnformatedValue() {
        return component.getText();
    }

    /**
     * @see #getText
     */
    public final Object getValue() {
        return component.getText();
    }

    /**
     * Delegation an JLabel#setText
     */
    public final void setText(String s) {
        if (component != null) {
           component.setText(s);
        }
    }
    
    /**
     * @see #setText
     */
    public void setLabel(String s) {
       super.setLabel(s);
       setText(s);
     }

    /**
     * @see #getText
     */
    public String getLabel() {
       return this.getText();
    }

    /**
     * Delegation an JLabel
     */
    public final String getText() {
        return component.getText();
    }
    /**
     * LEFT | RIGHT
     * @param posi
     */
    public void setHorizontalTextPosition(int posi) {
      component.setHorizontalTextPosition(posi);
    }

    /**
     * Delegation an JLabel
     */
    public final void setHorizontalAlignment(int i) {
        component.setHorizontalAlignment(i);
    }

    /**
     * Delegation an JLabel
     */
    final int getHorizontalAlignment() {
        return component.getHorizontalAlignment();
    }

    /**
     * Delegation an JLabel setDisplayedMnemonic
     */
    public final void setMnemonic(char c) {
        component.setDisplayedMnemonic(c);
    }

    /**
     * Delegation an JLabel
     */
    public final void setIcon(Icon icon) {
        component.setIcon(icon);
    }

    /**
     * Delegation an JLabel
     */
    final Icon getIcon() {
        return component.getIcon();
    }

    public Class getValueClass() {
        return String.class;
    }
}