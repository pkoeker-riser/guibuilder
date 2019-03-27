package de.guibuilder.framework;

import java.awt.Component;
import javax.swing.JMenuBar;
import javax.swing.JMenu;

/**
 * Implementierung einer Menubar.
 */
public class GuiMenuBar extends JMenuBar implements MenuAble {
	// Attributes
	/**
	 * Button Group für GuiMenuItemOption
	 */
	private transient GuiOptionGroup optionGroup;

	// Constructor
	/**
	 * Erzeugt eine MenuBar für Formulare.
	 * 
	 * @see GuiForm
	 */
	public GuiMenuBar() {
		super();
	}

	// Methods
	public final String getTag() {
		return "Menubar";
	}

	/**
	 * Fügt dem Menü einen Menüeintrag hinzu.
	 * <p>
	 * Setzt bei dem Menüeintrag auch den Parent.
	 */
	public final void add(MenuItemAble item) {
		super.add(item.getJComponent());
		item.setGuiMenu(this);
	}

	/**
	 * Fügt der Menubar ein Menü hinzu.
	 */
	public final void add(GuiMenu menu) {
		super.add(menu);
		menu.setParentMenu(this);
	}

	/**
	 * Fügt der Menubar ein Menü mit dem angegebenen Text hinzu und liefert eine
	 * Referenz auf das erzeugte Menü.
	 */
	public final GuiMenu addMenu(String label) {
		GuiMenu menu = new GuiMenu(label);
		this.add(menu);
		return menu;
	}

	/**
	 * Fügt dem Menü einen Radiobutton hinzu.<BR>
	 * Je Menü kann nur eine Gruppe von RadioButtons definiert werden.
	 */
	public final void addOption(OptionAble opt) {
		if (optionGroup == null) {
			optionGroup = new GuiOptionGroup(opt.getName());
			opt.setSelected(true);
		}
		optionGroup.add(opt);
		opt.setOptionGroup(optionGroup);
	}

	/**
	 * Liefert die OptionGroup zu diesem Menu oder null, wenn dieses Menu keine
	 * Optionen hält.
	 * 
	 * @see GuiMenuItemOption
	 * @return
	 */
	public final GuiOptionGroup getOptionGroup() {
		return this.optionGroup;
	}

	/**
	 * From MenuAble.<br>
	 * Das ist hier nicht erlaubt.
	 * 
	 * @throws IllegalArgumentException
	 */
	public final void addSeparator() {
		throw new IllegalArgumentException("MenuBar: No Separator allowed!");
	}

	/**
	 * Liefert das Menü mit dem angegebenen Namen.<br>
	 * Es wird nur nach den Menüs gesucht, die direkt der Menubar zugeordnet
	 * sind, also keine UnterMenüs.
	 * 
	 * @throws IllegalArgumentException
	 *             Wenn es ein derartiges Menu nicht gibt.
	 */
	public final GuiMenu getGuiMenu(String name) {
		for (int i = 0; i < this.getMenuCount(); i++) {
			GuiMenu menu = (GuiMenu) this.getMenu(i);
			if (menu.getName().equals(name)) {
				return menu;
			}
		}
		throw new IllegalArgumentException("Missing Menu: " + name);
	}

	public Component getAwtComponent() {
		return this;
	}

	/**
	 * Vernichtet alle Menüs.
	 */
	public final void dispose() {
		for (int i = 0; i < this.getMenuCount(); i++) {
			/*
			 * Der ursprüngliche Code hier geht so nicht, denn im Menubar kann
			 * auch etwas anderes stecken, als nur ein GuiMenu. Denkbar - wenn
			 * auch nicht schön - ist ein GuiMenuItem direkt im Menubar. Etwa
			 * so: 
			 * 	<Menubar> 
			 * 		<Menu label="Datei"> 
			 * 			<Menu label="öffnen..."/>
			 * 			<Menu label="Speichern"/> 
			 * 		</Menu> 
			 * 		<Item label="Hilfe"/>
			 * </Menubar>
			 * 
			 * für diese Elemente (z.B. JMenuItem, wenn GuiMenuItem spezifiziert
			 * ist) liefert this.getMenu() null zurück und nicht ein GuiMenu.
			 * Deshalb fällt man so, wie ursprünglich implementiert auf die
			 * Nase:
			 */

			// GuiMenu menu = (GuiMenu)this.getMenu(i);
			// menu.removeAll();
			/*
			 * Also mindestens so, wobei das Dispose dann für andere Elemente
			 * als GuiMenu nicht durchgeführt wird, sondern ein Warnung
			 * ausgegeben wird:
			 */

			Object o = this.getMenu(i);

			if (o != null) {
				if (o instanceof JMenu) {
					((GuiMenu) o).removeAll();
				}
			} else {
				System.out
						.println("Warning: Dispose of Menubar failed in GuiMenubar#dispose() at menu count position '"
								+ i
								+ "' because Menubar contains other elements than GuiMenu, witch can not be disposed.");
			}

			/*
			 * TODO Besser wäre aber eine Implementierung wie folgt, wobei dazu
			 * noch eine gemeinsame Oberklasse MenubarElement für GuiMenu und
			 * GuiMenuItem zu imlementieren. Das erforder größere Anpassungen am
			 * Framework. Oder vielleicht kann man auch das Interface MemberAble
			 * verwenden?
			 */

			// MenuBarElement mbe = this.getMenuBarElement(i);
			// if (mbe != null) {
			// 	if (mbe instanceof JMenu) {
			// 		((GuiMenu)mbe).removeAll();
			// 		} else if (o instanceof JMenuItem) {
			// 					((GuiMenuItem)mbe).dispose();
			// 			} else {
			// 				System.out.println("Warning: Dispose of Menubar failed in
			// 						GuiMenubar#dispose at menu count position '" + i + "' because
			// 						Menubar contains other elements than Menu and Item, witch can not
			// 						be disposed.");
			// 			}
			// 		}
		}
		this.removeAll();
	}
}