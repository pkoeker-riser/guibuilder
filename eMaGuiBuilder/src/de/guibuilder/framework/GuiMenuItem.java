package de.guibuilder.framework;


import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Implementierung eins "normalen" Menüeintrags.
 * 
 * @see GuiMenuItemCheckBox
 * @see GuiMenuItemOption
 */
public final class GuiMenuItem extends GuiAction implements MenuItemAble {
	// Attributes
	private GuiMenuItemImpl component;	

	/**
	 * Das Menü oder MenuBar, in dem dieser Menüeintrag steckt, wird für den Zugriff auf
	 * RootPane benötigt.
	 */
	private transient MenuAble guiMenu;

	// Constructor
	/**
	 * Erzeugt einen beschrifteten Menüeintrag.
	 * <p>
	 * Enthält das Label das Zeichen "%" wird das folgende Zeichen als mnemonic Char
	 * verwendet. Mit "%%" kann ein "%"-Zeichen ausgegeben werden. <br>
	 * Der übergebene Text wird gleichzeitig als Name der Komponente verwendet. <br>
	 * Es wird der ActionListener gesetzt.
	 * 
	 * @see GuiActionListener
	 */
	public GuiMenuItem(String label) {
		super(label);
		this.setText(label);
		this.setActionCommand(getName());
		this.component.addActionListener(new GuiActionListener(this));

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

	/**
	 * Erzeugt einen Menüeintrag, der dem übergebenen Menü am Ende hinzugefügt wird.
	 * <p>
	 * Dieses ist die einfachste Methode, einen MenüEintrag zu erzeugen, und ihn gleich dem
	 * Menü hinzuzufügen; erspart menu.add().
	 */
	public GuiMenuItem(String label, MenuAble menu) {
		this(label);
		menu.add(this);
	}

	// Methods	
	public char getEnabledWhen() {
		return this.component.getEnabledWhen();
	}

	public void setEnabledWhen(char c) {
		this.component.setEnabledWhen(c);
	}

	public final String getTag() {
		return "Item";
	}

	/**
	 * Liefert JMenuItem
	 */
	public JComponent getJComponent() {
		return this.getMenuItem();
	}

	/**
	 * Liefert das Swing JMenuItem
	 * 
	 * @return
	 */
	public GuiMenuItemImpl getMenuItem() {
		if (component == null) {
			component = new GuiMenuItemImpl();
		}
		return component;
	}

	public AbstractButton getAbstractButton() {
		return this.getMenuItem();
	}

	/**
	 * Wegen Bug in Swing wird GuiMember.getRootPane hier überschrieben.
	 */
	public GuiRootPane getRootPane() {
		if (guiMenu == null) {
			throw new IllegalStateException(
					"GuiMenuItem: Parent Menu not set! See setGuiMenu()");
		}
		GuiRootPane rootPane = (GuiRootPane) guiMenu.getRootPane();
		return rootPane;
	}

	/**
	 * From GuiMember. <br>
	 * Macht hier nichts.
	 */
	public final void reset() {
	}

	/**
	 * Setzt den Short-Cut; z.B. Ctrl+N
	 */
	public void setAccelerator(KeyStroke key) {
		component.setAccelerator(key);
	}

	/**
	 * Liefert das Menü zu diesem Menüeintrag.
	 */
	public MenuAble getGuiMenu() {
		return guiMenu;
	}

	/**
	 * Setzt die Assoziation mit dem Menü dieses Eintrags; wird für getRootPane benötigt.
	 */
	public void setGuiMenu(MenuAble menu) {
		guiMenu = menu;
	}

	void dispose() {
		guiMenu = null;
		component = null;
		this.setGuiParent(null);
	}
}