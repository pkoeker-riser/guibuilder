package de.guibuilder.framework;

import java.awt.event.FocusEvent;

/**
 * Abstracte Klasse für die "Einzelteile" einer Oberfäche im Unterschied
 * zu den "Baugruppen".
 * @see GuiContainer
 * @see GuiMember
 * @since 0.9.1
*/
public abstract class GuiElement extends GuiMember {
  // Attibutes
  /**
   * Kennzeichen, ob die Komponente mit der Taste tab erreichbar ist.
   */
  private boolean tabstop;
  /**
   * Statuszeilentext
   */
  private String hint;
  /**
   * ParentTable wenn Spalte einer Tabelle
   */
  private transient GuiTable parentTable;
  /**
   * Das ActionCommand, welches beim LostFocus-Ereignis geliefert wird.
   */
  private String actionLostFocus;
  /**
   * für GuiLabel
   */
  protected String actionClick;
  /**
   * Das ActionCommand, welches beim DoppelKlick-Ereignis geliefert wird.
   */
  protected String actionDblClick;
  /**
   * Das ActionCommand, welches beim Change-Ereignis geliefert wird.
   * Wenn das die Komponente nicht unterstützt, dann bei LostFocus.
   */
  protected String actionChange;
  /**
   * Inhalt des Attributes file=[fileName.xml]; Verweis auf einen zu startenden Folgedialog
   */
  private String fileName;
  // Constructor
  GuiElement() {
    super();
  }
  GuiElement(String label) {
    super(label);
  }

  // Methods
  /**
   * Wird hier überschrieben wegen Bug in Swing:<br>
   * Wenn die Komponente Spalte in einer Tabelle ist, dann
   * funktioniert JComponent#getRootPane nicht.
   * @see GuiMember#getRootPane
   */
  public GuiRootPane getRootPane() {
    if (parentTable != null) {
      return parentTable.getRootPane();
    } else {
      return super.getRootPane();
    }
  }
  /**
   * Liefert das Kennzeichen, ob die Komponente per Tabstop erreichbar ist.
   */
  public final boolean hasTabstop() {
    if (tabstop == false) {
      return tabstop;
    }
    else {
      return getJComponent().isFocusable();
    }
  }
  /**
   * Setzt das Kennzeichen, ob die Komponente per Tabstop erreichbar ist.
   */
  public final void setTabstop(boolean b) {
    tabstop = b;
    if (this.getJComponent() != null) {
    	this.getJComponent().setFocusable(b);
    }
  }
  /**
   * From awt.Component
   */
  public void requestFocus() {
    if (this.getJComponent() != null) {
			this.getJComponent().requestFocus();
    }
	}
  /**
   * From awt.Component<p>
   * Wird von GuiText überschrieben (Editable)
   */
  public boolean isEnabled() {
    return getJComponent() != null ? getJComponent().isEnabled() : false;
  }
  /**
   * From awt.Component
   * Wird von Text und Memo überschieben
   */
  public void setEnabled(boolean b) {
    if (getJComponent() != null) getJComponent().setEnabled(b);
  }
  /**
   * Liefert den Statuszeilentext.
   */
  public final String getHint() {
    return hint;
  }
  /**
   * Setzt den Statuszeilentext.
   */
  public final void setHint(String s) {
    hint = s;
  }
  /**
   * Setzt den Statuszeilentext.
   * Wird vom FocusListener aufgerufen.
   * Diese Methode wird bei vielen Klassen überschrieben.
   * @see GuiFocusListener
   * @see GuiRootPane#setHint
   */
  void gotFocus(FocusEvent e) {
    if (this.getRootPane() != null) {
      this.getRootPane().setHint(this.getHint());
    }
  }
  /**
   * Löscht den Statuszeilentext.<p>
   * Wenn Spalte einer Tabelle, dann stopCellEditing() aufrufen.<br>
   * Wird vom FocusListener aufgerufen.
   * Diese Methode wird bei einigen Klassen überschrieben.
   * @see GuiFocusListener
   * @see GuiRootPane#setHint
   * @see GuiRootPane#obj_LostFocus
   * @see GuiTable#stopCellEditing
   */
  void lostFocus(FocusEvent e) {
	  if (e == null || e.isTemporary() == false) {
      GuiRootPane pane = this.getRootPane();
      // Ab jdk 1.4 kommt hier null???
      if (pane != null) {
        pane.setHint(" ");
      }
      // Stop Cell Editing
      if (parentTable != null) {
        parentTable.stopCellEditing();
      }
    }
  }
  /**
   * Liefert die Tabelle, wenn die Komponente eine Tabellen-Spalte ist.
   * @see TableColumnAble
   */
  public final GuiTable getParentTable() {
    return parentTable;
  }
  /**
   * @see TableColumnAble
   */
  public final void setParentTable(GuiTable tbl) {
    parentTable = tbl;
  }
  /**
   * Teilt der Komponente mit, daß sie ein LostFocus-Ereignis an den Controller
   * weiterreichen soll. Bei übergaben von null wird die Weiterleitung abgeschaltet.
   * @param cmd ActionCommand dieser Message
   */
  public final void setMsgLostFocus(String cmd) {
    this.actionLostFocus = cmd;
  }
  /**
   * Liefert das ActionCommand zum LostFocus-Ereignis oder null.
   */
  final String getMsgLostFocus() {
    return this.actionLostFocus;
  }
  public final void setMsgClick(String cmd) {
      this.actionClick = cmd;
      if (hasMouseListener == false) {
        this.addMouseListener(new GuiMouseListener(this));
      }
    }
    public final String getMsgClick() {
      return this.actionClick;
    }
  /**
   * Teilt der Komponente mit, daß sie ein DoppelKlick-Ereignis an den Controller
   * weiterreichen soll. Bei übergaben von null wird die Weiterleitung abgeschaltet.
   * @param cmd ActionCommand dieser Message
   */
  public final void setMsgDblClick(String cmd) {
    this.actionDblClick = cmd;
    if (hasMouseListener == false) {
      this.addMouseListener(new GuiMouseListener(this));
    }
  }
  public final String getMsgDblClick() {
    return this.actionDblClick;
  }
  /**
   * Setzt das ActionCommand, welches ausgelöst wird, wenn sich der Inhalt der Komponente
   * geändert hat.
   */
  public final void setMsgChange(String cmd) {
    this.actionChange = cmd;
  }
  /**
   * Liefert das ActionCommand, welches ausgelöst wird, wenn sich der Inhalt der Komponente
   * geändert hat.
   */
  public final String getMsgChange() {
    return this.actionChange;
  }
  /**
   * Siehe Attribut file=
   */
  public final String getFileName() {
    return this.fileName;
  }
  /**
   * Siehe Attribut file=
   */
  final void setFileName(String f) {
    this.fileName = f;
  }
}