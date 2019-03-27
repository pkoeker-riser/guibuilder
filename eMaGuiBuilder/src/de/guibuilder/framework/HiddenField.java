package de.guibuilder.framework;

import javax.swing.JComponent;

/**
 * Implementierung eines "versteckten" Feldes.
 * <p>
 * Dieses dient vor allem dazu, zusätzliche Statusinformation in die Oberfläche
 * mit aufzunehmen, die dem Benutzer aber nicht angezeigt werden soll.
 * <p>
 * Die Komponente kann auch als unsichtbare Spalte in Tabellen aufgenommen
 * werden.
 * <p>
 * isVisible() und isModified() liefern immer false.
 */
public final class HiddenField extends GuiComponent implements TableColumnAble {
   // Attributes
   /**
    * Da diesem Feld kein JComponent zugeordnet ist, muß es selbst das Attribut
    * "name" halten.
    */
   private String name;

   private Object value;

   // Constructor
   public HiddenField(String name) {
      super(name);
   }

   // Methods from GuiMember
   public final String getTag() {
      return "Hidden";
   }

   /**
    * Achtung! Liefert null!
    */
   public JComponent getJComponent() {
      return null;
   }

   /**
    * Liefert STRING
    */
   public final int getDataType() {
      return STRING;
   }

   public String getName() {
      return name;
   }

   /**
    * From awt.Component
    */
   public void setName(String name) {
      this.name = name;
   }

   public void reset() {
      this.value = null;
      setModified(false);
   }

   /**
    * RootPane wird von GuiParent geliefert.
    */
   public GuiRootPane getRootPane() {
      return getGuiParent().getRootPane();
   }

   // From GuiComponent
   public void setValue(Object value) {
      this.value = (String) value;
      setModified(false);
   }

   public Object getUnformatedValue() {
      return value;
   }

   public Object getValue() {
      return value;
   }

   public Class getValueClass() {
      return String.class;
   }
}