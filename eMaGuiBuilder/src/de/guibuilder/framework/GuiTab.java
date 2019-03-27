package de.guibuilder.framework;

import javax.swing.Icon;

/**
 * Implementierung einer Karte für einen Registerkartensatz.<p>
 * Die Methode setIcon muß vor dem Hinzufügen zu einem Tabset aufgerufen werden.
 * @see GuiTabset#addTab
 */
public class GuiTab extends GuiPanel {
  // Attributes
  /**
   * Rückverkettung auf das Tabset, in dem diese Registerkarte eingesetzt wird.
   */
  private GuiTabset myTabset;
  /**
   * Der Tabindex dieser Registerkarte in myTabset
   */
  private int myTabIndex = -1;
  /**
   * Icon der Registerkarte; default ist null.
   */
  private Icon icon;
  /**
   * für Ereignis OnActive bei Tab
   */
  private String msgActive;
  // Constructors
  /**
   * Erzeugt einer Registerkarte ohne Beschriftung.<P>
   * Die Beschriftung muß anschließend mit
   * der Methode setTitle(String) gesetzt werden.<BR>
   * Erst dann kann die Karte einem Tabset mit addTab() zugewiesen werden.
   * @see #setTitle
   * @see GuiTabset#addTab
   */
  public GuiTab () {
    super("tab");
    this.guiInit("tab");
  }
  /**
   * Erzeugt einer Registerkarte mit einer Beschriftung.<BR>
   * Die Beschriftung wird gleichzeitig als Name übernommen.
   * @param label Beschriftung der Registerkarte.
   * @see GuiTabset#addTab
   */
  public GuiTab (String label) {
    super(label);
    this.guiInit(label);
  }
  /**
   * Erzeugt eine Registerkarte zu einem Registerkartensatz.<p>
   * Die Verknüpfung Tabset <--> Tab wird mit erledigt.<br>
   * Dieser Constructor wird von der Factory verwendet. 
   * @param parent
   * @param label
   */
  public GuiTab (GuiTabset parent, String label) {
  	super(label);
  	this.myTabset = parent;
    parent.addTab(this);
    this.guiInit(label);
  }
  // Methods
  /**
   * Setzt ref="*"
   */
  private void guiInit(String title) {
    this.setRef("*");
    // Shortcut
    char mnemo;
    int p = title.indexOf("%");
    if (p != -1 && p+1 < title.length()) {
      mnemo = title.charAt(p+1);
      title = title.substring(0,p) + title.substring(p+1);
      this.setTitle(title);
      if (mnemo != '%') {
        this.setMnemonic(mnemo, p);
      }
    }
  }
  public void setMnemonic(char mnemo, int index) {
  	this.getGuiTabset().setMnemonicAt(this.myTabIndex, mnemo);
  	this.getGuiTabset().setDisplayedMnemonicIndexAt(this.myTabIndex, index);
  }
  // From Interface MemberAble
  public final String getTag() {
    return "Tab";
  }
  /**
   * Liefert den Registerkartensatz, in dem dieser Karte liegt
   * oder null, wenn diese Karte noch nicht einem Tabset zugewiesen wurde.
   * @see GuiTabset#addTab
   */
  public final GuiTabset getGuiTabset() {
    return myTabset;
  }
  /**
   * Setzt Rückwärtsverkettung Tab --> Tabset.
   * @see GuiTabset#addTab
   */
  final void setGuiTabset(GuiTabset tabset, int tabIndex) {
    myTabset = tabset;
    myTabIndex = tabIndex;
  }
  /**
   * Liefert den TabIndex dieser Registerkarte im Tabset.<BR>
   * Der ist -1, wenn die Karte noch keinem Tabset zugeordnet wurde.
   */
  public final int getTabIndex() {
    return myTabIndex;
  }
  /**
   * Liefert den Icon dieser Registerkarte oder null wenn keiner gesetzt.
   */
  public final Icon getIcon() {
    return this.icon;
  }
  /**
   * Setzt den Icon dieser Registerkarte.
   * muß vor dem Hinzufügen zu einem Tabset aufgerufen werden.
   * @param icon Ein ImageIcon
   * @see GuiUtil#makeIcon
   * @see GuiTabset#addTab
   */
  public final void setIcon(Icon icon) {
    this.icon = icon;
  }
  /**
   * Setzt die Beschriftung der Registerkarte zur Laufzeit neu.
   * Der Name der Karte wird dabei nicht geändert.
   */
  public final void setTitle(String label) {
    setLabel(label);
    if (myTabset != null) {
    	myTabset.setTitleAt(myTabIndex, label);
    }
  }
  /**
   * Liefert den Tabset zu dieser Registerkarte
   * @return
   */
  public final GuiTabset getParentTabset() {
  	return this.myTabset;
  }
  /**
   * Holt diesen Tab in den Vordergrund
   *
   */
  public final void setSelected() {
  	this.myTabset.setSelectedIndex(this.myTabIndex);
  }
  /**
   * Diese Methode ist hier überschrieben wegen der
   * Verknüpfung der Registerkarten mit dem Tabset.
   */
  public void setName(String newName) {
     String oldName = this.getName();
     super.setName(newName);
     if (myTabset != null) {
        myTabset.renameTab(oldName, newName);
     }
  }
  /**
   * Setzt den Feldinhalt aller Komponenten dieser Panel auf leer (null).
   * Die erste Registerkarte wird selektiert.<p>
   * TODO : Das funktioniert nicht, wenn das Attribut ref="-" ist.
   */
  public final void reset() {
    super.reset();
    if (myTabIndex == 0 && myTabset != null) {
      myTabset.setSelectedIndex(0);
    }
  }
	public String getMsgActive() {
		return msgActive;
	}
	void setMsgActive(String msgActive) {
		this.msgActive = msgActive;
	}
}