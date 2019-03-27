package de.guibuilder.framework;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.MenuElement;

import de.guibuilder.framework.GuiTable.GuiTableColumn;

/**
 * Implementierung eines PopupMenüs.
 * <p>
 * PopupMenus können im Prinzip allen Members zugeordnet werden, aber nur bei Table, Tree,
 * Memo, Editor und List ist dies wirklich sinnvoll.
 * <p>
 */
public class GuiPopupMenu extends JPopupMenu implements MenuAble {	
   private static final long serialVersionUID = 1L;
	// Attributes
	private transient GuiOptionGroup optionGroup;
	/**
	 * Die Komponente, die das PopupMenu anzeigt.
	 */
	private transient GuiMember invoker;
	private String tableColumns; // Namen der anklickbaren Tabellenspalten (Komma-getrennt) bei denen dieses PopupMenü aktiviert sein soll

	// Constructor
	/**
	 * Erzeugt ein PopupMenu mit dem angegebenen Label.<p> 
	 * Dieses Label wird nur bei Motif L&F angezeigt.
	 */
	public GuiPopupMenu(String title) {
		super(title);
		super.setName(GuiUtil.labelToName(title));
	}

	// Methods
	public final String getTag() {
		return "Popup";
	}

	// From Interface MenuAble
	public final void add(MenuItemAble item) {
		super.add(item.getJComponent());
		item.setGuiMenu(this);
	}

	// From Interface MenuAble
	public final void add(GuiMenu menu) {
		super.add(menu);
	    menu.setParentMenu(this);
	}
	  /**
	   * Liefert das Menü mit dem angegebenen Namen.<br>
	   * Es wird nur nach den Menüs gesucht, die UnterMenüs zu
	   * diesem Menü sind.
	   * @since 30.7.2012 / PKÖ
	   * @throws IllegalArgumentException Wenn es ein derartiges Menu nicht gibt.
	   */
	public final GuiMenu getGuiMenu(String name) {
	    if (name == null) return null;
	    for (int i=0; i<this.getComponentCount(); i++) {
	      Component comp = this.getComponent(i);
	      if (name.equals(comp.getName()) && comp instanceof GuiMenu) {
	        return (GuiMenu)comp;
	      }
	    }
	    throw new IllegalArgumentException("Missing Menu: "+name);
	}

	// From Interface MenuAble
	public final void addOption(OptionAble opt) {
		if (optionGroup == null) {
			optionGroup = new GuiOptionGroup(opt.getName());
			optionGroup.setMsgChange(opt.getMsgChange());
			opt.setSelected(true);
		}
		optionGroup.add(opt);
	}
    /**
     * Liefert die OptionGroup zu diesem Menu oder null, 
     * wenn dieses Menu keine Optionen hält.
     * @see GuiMenuItemOption
     * @return
     */
    public final GuiOptionGroup getOptionGroup() {
       return this.optionGroup;
    }

	// From Interface MenuAble
	// Trick17: RootPane wird vom Invoker beschafft!
	public final JRootPane getRootPane() {
		if (invoker == null) {
			return null;
		} else {
			return invoker.getRootPane();
		}
	}

	GuiRootPane getGuiRootPane() {
		return (GuiRootPane) getRootPane();
	}
	public Component getAwtComponent() {
		return this;
	}

	/**
	 * Wird aufgerufen wenn PopupMenu angezeigt wird.
	 */
	final void setGuiInvoker(GuiMember member) {
		if (invoker == null) {
			this.invoker = member;
			if (optionGroup != null) {
				// Hack: Member an invoker hängen
				GuiContainer memberContainer = invoker.getGuiParent();
				if (invoker instanceof GuiContainer) {
					if (((GuiContainer) invoker).isParentContainer()) {
						memberContainer = (GuiContainer) invoker;
					}
				}
				if (memberContainer != null) {
					memberContainer.addMember(optionGroup);
				}
			}
		}
	}

	/**
	 * Liefert die Komponente, der dieses PopupMenu zugeordnet ist, oder null, wenn das
	 * PopupMenu noch nicht angezeigt wurde.
	 * 
	 * @see de.guibuilder.framework.event.GuiActionEvent
	 */
	public final GuiMember getGuiInvoker() {
		return invoker;
	}
	
	public MenuElement[] getSubElements() {
	   MenuElement result[];
	   Vector<MenuElement> tmp = new Vector<MenuElement>();
	   int c = getComponentCount();
	   int i;
	   for(i=0; i < c; i++) {
	      Component m = getComponent(i);
		  if(m instanceof MenuElement)
		     tmp.addElement((MenuElement) m);
	   }
		
	   result = new MenuElement[tmp.size()];
	   for(i=0,c=tmp.size(); i < c; i++) {
	      result[i] = tmp.elementAt(i);
	   }
	   return result;
    }
	
	public void setTableComlumnNames(String s) {
		this.tableColumns = s;
	}

	public void setEnabledWhen(GuiTableColumn col) {
	   String colName = col.getName();
	   if (colName == null || colName.length() == 0 || this.tableColumns == null)
	      return;
	   boolean enabled = tableColumns.indexOf(colName) != -1;
	   this.setEnabled(enabled);		
	}
	
	public void setEnabledWhen(int cnt) {
		MenuElement[] elements = this.getSubElements();
		for(int i = 0; i < elements.length; i++) {
			MenuElement ele = elements[i];
			if (ele instanceof GuiMenuItemImpl) {
				GuiMenuItemImpl mi = (GuiMenuItemImpl)ele;
				char c = mi.getEnabledWhen();
				if (c != 0) {
      				switch (cnt) {
      				case 0: // nix selektiert
      					switch (c) {
      					case 'M':
      					case 'S':
      					case 'X':
      						mi.setEnabled(false);
      						break;
      					default:
      						mi.setEnabled(true);
      						break;
      					}
      					break;
      				case 1: // Einfache Selektion
      					switch (c) {
      					case 'M':
      					case 'N':
      						mi.setEnabled(false);
      						break;
      					default:
      						mi.setEnabled(true);
      						break;
      				}
      					break;
      				default: // Mehrfachselektion
      					switch (c) {
      					case 'N':
      					case 'S':
      						mi.setEnabled(false);
      						break;
      					default:
      						mi.setEnabled(true);
      						break;
      				}
				}
				}
			}
		}
	}
}