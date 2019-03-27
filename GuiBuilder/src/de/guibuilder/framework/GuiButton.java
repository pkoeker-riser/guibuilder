package de.guibuilder.framework;

import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Implementierung eines PushButtons.
 * <p>
 * Nur die Buttons mit der Beschriftung oder dem ActionCommand "OK" reagieren
 * auf die Return-Taste. <br>
 * Es wird setDefaultCapable(false) gesetzt, damit sich der Button das Verhalten
 * eines Default-Buttons nicht "einfängt".
 */
public final class GuiButton extends GuiAction {
   // Attributes
   /**
    * @see #getJComponent
    * @see #getButton
    */
   private JButton component;
   /**
    * Verweis auf die Tabelle, die zuvor von der Factory erzeugt wurde. für
    * Standardmethoden wie InsertRow(), DeleteRow().
    */
   private transient GuiTable myTable;

   // Constructors
   /**
    * Erzeugt einen Button ohne Beschriftung; z.B. für Toolbar, wenn später eine
    * Grafik hinzugefügt wird.
    */
   public GuiButton() {
      super();
      this.setName("NoNameButton");
      this.setActionCommand("NoActionButton");
      this.guiInit();
   }

   /**
    * Erzeugt einen beschrifteten Button. Das Label wird gleichzeitig als Name
    * des Buttons und als ActionCommand verwendet. <BR>
    * Lautet das Label "OK" wird ein Default-Button erzeugt.
    */
   public GuiButton(String label) {
      super(label);
      this.setText(label);
      if (label == null || label.length() == 0) {
         this.setName("NoNameButton");
         component.setActionCommand("NoActionButton");
      } else {
         setActionCommand(getName());
         char mnemo;
         int p = label.indexOf("%");
         if (p != -1 && p + 1 < label.length()) {
            mnemo = label.charAt(p + 1);
            label = label.substring(0, p) + label.substring(p + 1);
            this.setText(label);
            if (mnemo != '%') {
               this.setMnemonic(mnemo);
               this.component.setDisplayedMnemonicIndex(p);
            }
         }
      }
      this.guiInit();
   }

   // Methods
   /**
    * Es wird der ActionListener gesetzt, und setDefaultCapable(false).
    * 
    * @see GuiActionListener
    */
   private void guiInit() {
   	GuiActionListener al = new GuiActionListener(this);
   
      component.addActionListener(al);
      // Endlich verstanden!
      component.setDefaultCapable(false);
   }

   // From GuiMember
   @Override
	public final String getTag() {
      return "Button";
   }
   // From GuiMember
   @Override
	public JComponent getJComponent() {
      return this.getButton();
   }

   /**
    * @see #getJComponent
    */
   public JButton getButton() {
      if (component == null)
         component = new JButton();
      return component;
   }
   // From GuiAction
   @Override
	public AbstractButton getAbstractButton() {
      return this.getButton();
   }

   /**
    * Wenn "OK", dann default-Button. Von GuiAction überschrieben
    */
   @Override
	public void setActionCommand(String cmd) {
      super.setActionCommand(cmd);
      if (cmd.toLowerCase().equals("ok")) {
         getButton().setDefaultCapable(true); // Kann weg?
         if (this.getRootPane() != null) {
            this.getRootPane().setDefaultButton(component);
         }
      }
   }
   
   public void setAccelerator(KeyStroke key) {
   	ActionListener[] als = component.getActionListeners();
   	if (als.length == 1) {
   		component.registerKeyboardAction(als[0], key, JComponent.WHEN_IN_FOCUSED_WINDOW);
   	}
   }

   /**
    * Beim Erhalt des Focus wird der Statuszeilentext gesetzt. <br>
    * CurrentTable wird in RootPane gesetzt wenn der Button mit einer Tabelle
    * verknüpft ist.
    */
   @Override
	public void gotFocus(FocusEvent e) {
      super.gotFocus(e);
      if (myTable != null && this.getRootPane() != null) {
         this.getRootPane().setCurrentTable(myTable);
      }
   }

   /**
    * Liefert die Tabelle zu diesem Button oder null, wenn keine gesetzt. <BR>
    * für InsertRow() usw.
    */
   public final GuiTable getTable() {
      return myTable;
   }

   /**
    * Setzt die Tabelle zu diesem Button. <BR>
    * für InsertRow() usw.
    */
   public final void setTable(GuiTable tbl) {
      this.myTable = tbl;
   }

   /**
    * Macht hier nüscht.
    */
   @Override
	public final void reset() {
   }

   @Override
	void dispose() {
      myTable = null;
      component = null;
      this.setGuiParent(null);
   }
}