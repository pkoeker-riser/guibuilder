package de.guibuilder.framework;

import java.awt.Component;
import java.awt.PageAttributes;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.Icon;
/**
 * Abstrakte Basisklasse für Oberfächenelemente die Aktionen auslösen können: 
 * Button und MenuItem.<p>
 * Alle Methoden bis auf setActionCommand sind final.
 * 
 * @since 0.9.1
 */
public abstract class GuiAction extends GuiElement {
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GuiAction.class);

	/**
	 * OK-Action: Key "Return" typed
	 */
	public static final int OK = 1;
	/**
	 * Cancel-Action: Key "Esc" typed
	 */
	public static final int CANCEL = 2;
	/**
	 * Help-Action: Key "F1" typed
	 */
	public static final int HELP = 3;
	/**
	 * Context-help-Action: Key "Shift-F1" typed
	 */
	public static final int CONTEXT_HELP = 4;
	public static final int NEW = 10;
	public static final int SAVE = 11;
	public static final int DELETE = 12;
	public static final int FIND = 13;
	public static final int RELOAD = 14;
	public static final int DETAIL = 15;
	public static final int PRINT = 16; // Drucker-Auswahl-Dialog
	public static final int EXIT = 99;
	protected int type;
	
	private int detailRestoreHight;

	// Constructor
	/**
	 * Default Constructor; ist nur bei Buttons ohne Beschriftung aber mit Grafiken
	 * wirklich sinnvoll.
	 */
	GuiAction() {
		// tut nix
		super();
	}

	/**
	 * Setzt das übergebene Label als default ActionCommand.
	 */
	GuiAction(String label) {
		super(label);
	}

	// Methods
	/**
	 * Liefert GUI_ACTION
	 */
	@Override
	public final int getGuiType() {
		return GUI_ACTION;
	}

	/**
	 * Liefert den AbstractButton der zugrundeliegenden Implementierung; JButton, JMenuItem
	 * 
	 * @return
	 */
	public abstract AbstractButton getAbstractButton();

	/**
	 * Wird von GuiButton überschieben.
	 * <p>
	 * From swing.AbstractButton
	 */
	public void setActionCommand(String a) {
		this.getAbstractButton().setActionCommand(a);
		if (this.getName() == null || this.getName().length() == 0) {
			this.setName(a);
		}
	}

	/**
	 * Wird vom Action-Listener aufgerufen wenn diese Action
	 * aktiviert wird.<p>
	 * für das Auslösen von Ereignissen in Scripten oder
	 * Controllern wird RootPane aktiviert.
	 * @see GuiActionListener
	 */
  GuiInvokationResult obj_actionPerformed(ActionEvent e) {
		if (this.getType() == DETAIL) {
			this.perfDetail();
			return new GuiInvokationResult("DETAIL", GuiInvokationResult.ReturnStatus.OK);
		} else if (this.getType() == PRINT) {
			this.perfPrint(e);
			return new GuiInvokationResult("PRINT", GuiInvokationResult.ReturnStatus.OK);
		} else if (this.getType() == HELP) {
			GuiSession.getInstance().showHelp(this);
			return new GuiInvokationResult("HELP", GuiInvokationResult.ReturnStatus.OK);
		} else if (this.getType() == CONTEXT_HELP) { // Nix machen, wenn Context-Help
			return new GuiInvokationResult("CONTEXT_HELP", GuiInvokationResult.ReturnStatus.OK);
		}
		GuiRootPane root = this.getRootPane();
		if (root == null) {
			logger.warn(this.getName() + " Missing RootPane!");
			return new GuiInvokationResult("Missing RootPane!", GuiInvokationResult.ReturnStatus.ERROR);
		}
		GuiInvokationResult result = root.obj_ActionPerformed(this, e);
		return result;
	}
	private void perfDetail() {
		GuiRootPane root = this.getRootPane();
		GuiPanel mainPanel = root.getMainPanel();
		String s = this.getFileName();
		StringTokenizer toks = new StringTokenizer(s,",;");
		int h = 0;
		while(toks.hasMoreTokens()) {
			String tok = toks.nextToken();
			GuiContainer mem = mainPanel.getContainer(tok);
			if (mem != null) {
				boolean visible = mem.isVisible();
				if (visible) {
					h = h + mem.getJComponent().getHeight();
				}
				mem.setVisible(!visible);				
			}
		}
		GuiWindow win = root.getParentWindow();
		Component comp = win.getComponent();
		if (h != 0) {
			this.detailRestoreHight = comp.getHeight();
			comp.setSize(comp.getWidth(), comp.getHeight() - h);
		} else {
			comp.setSize(comp.getWidth(), this.detailRestoreHight);
		}
		comp.validate();
		//comp.repaint();
	}
	
	private void perfPrint(ActionEvent e) {
		try {
			GuiRootPane root = this.getRootPane();
			GuiWindow win = root.getParentWindow();
			PageAttributes atts = new PageAttributes();
			atts.setOrigin(PageAttributes.OriginType.PRINTABLE);
      PrinterJob pj = PrinterJob.getPrinterJob();
			if (pj == null) return; // Abbrechen
      PageFormat pfUse = pj.defaultPage();
      pfUse = pj.pageDialog( pfUse );
      if (pj.printDialog()) {
        pj.setPrintable(win, pfUse);
        pj.print();
      }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * From swing.AbstractButton
	 */
	public final String getActionCommand() {
		if (this.getAbstractButton() != null) {
			return this.getAbstractButton().getActionCommand();
		} else {
			return null;
		}
	}

	/**
	 * From swing.AbstractButton
	 */
	public final void setHorizontalAlignment(int i) {
		this.getAbstractButton().setHorizontalAlignment(i);
	}

	/**
	 * From swing.AbstractButton
	 */
	public final void setIcon(Icon icon) {
		this.getAbstractButton().setIcon(icon);
	}

	/**
	 * From swing.AbstractButton
	 */
	public final void setMnemonic(char c) {
		this.getAbstractButton().setMnemonic(c);
	}

	/**
	 * From swing.AbstractButton
	 */
	public final String getText() {
		return this.getAbstractButton().getText();
	}

	/**
	 * From swing.AbstractButton
	 */
	public final void setText(String s) {
		this.getAbstractButton().setText(s);
		setLabel(s);
	}

	/**
	 * Die Action wird aktiviert, so als wäre sie vom Benutzer angeklickt worden.
	 */
	public final void click() {
		this.getAbstractButton().doClick();
	}
	/**
	 * prüft ob der Name leer ist, und versucht anhand des Typs bzw. des ActionCommands einen
	 * default-Namen zu vergeben. 
	 *
	 */
	void checkEmptyName() {
	   String name = this.getName();
	   if (name == null || name.length() == 0 || name.equals("NoNameButton")) {
	   	String s = GuiAction.getTypeName(this.getType()).toLowerCase();
	   	String cmd = this.getActionCommand();
	   	if (cmd != null && cmd.length() > 0 ) {
	   		s = cmd;
	   	}
	      String pref = "a_";
	      if (this instanceof GuiButton) {
	          pref = "pb_";
	      } else if (this instanceof GuiMenuItem) {
	          pref = "i_";
	      }
	      this.setName(pref + s);
	   }
	}
	/**
	 * Setzt den Typ der Action (OK, Cancel, HELP, CONTEXT_HELP)
	 * 
	 * @param type
	 */
	public void setType(int type) {
		this.type = type;
			switch (type) {
			case OK:
				break;
			case CANCEL:
				break;
			case HELP:
				break;
			case CONTEXT_HELP:
				GuiSession.getInstance().enableContextHelp(this);
			break;
		}
	}

	/**
	 * Liefert den Typ der Action.
	 * 
	 * @return
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * Setzt den Typ der Action: 
	 * OK|CANCEL|HELP|CONTEXT_HELP|NEW|SAVE|FIND|RELOAD|DELETE|DETAIL|EXIT
	 * 
	 * @param type
	 */
	public void setType(String type) {
		if (type.equalsIgnoreCase("HELP")) {
			this.setType(HELP);
		} else if (type.equalsIgnoreCase("CONTEXT_HELP")) {
			this.setType(CONTEXT_HELP);
		} else if (type.equalsIgnoreCase("OK")) {
			this.setType(OK);
		} else if (type.equalsIgnoreCase("CANCEL")) {
			this.setType(CANCEL);
		} else if (type.equalsIgnoreCase("NEW")) {
			this.setType(NEW);
		} else if (type.equalsIgnoreCase("SAVE")) {
			this.setType(SAVE);
		} else if (type.equalsIgnoreCase("FIND")) {
			this.setType(FIND);
		} else if (type.equalsIgnoreCase("RELOAD")) {
			this.setType(RELOAD);
		} else if (type.equalsIgnoreCase("DELETE")) {
			this.setType(DELETE);
		} else if (type.equalsIgnoreCase("DETAIL")) {
			this.setType(DETAIL);
		} else if (type.equalsIgnoreCase("PRINT")) {
			this.setType(PRINT);
		} else if (type.equalsIgnoreCase("EXIT")) {
			this.setType(EXIT);
		}
	}
	/**
	 * Liefert den Namen der Actiontype:
	 * OK|CANCEL|HELP|CONTEXT_HELP|NEW|SAVE|FIND|RELOAD|DELETE|EXIT
	 * oder UNKNOWN
	 * @param type
	 * @return
	 */
	public static String getTypeName(int type) {
	   String s = "UNKNOWN";
	   switch (type) {
	   	case HELP:
	   	    s = "HELP";
	   	    break;
	   	case CONTEXT_HELP:
	   	    s = "CONTEXT_HELP";
	   	    break;
	   	case OK:
	   	    s = "OK";
	   	    break;
	   	case CANCEL:
	   	    s = "CANCEL";
	   	    break;
	   	case NEW:
	   	    s = "NEW";
	   	    break;
	   	case SAVE:
	   	    s = "SAVE";
	   	    break;
	   	case FIND:
	   	    s = "FIND";
	   	    break;
	   	case RELOAD:
	   	    s = "RELOAD";
	   	    break;
	   	case DELETE:
	   	    s = "DELETE";
	   	    break;
	   	case DETAIL:
   	    s = "DETAIL";
   	    break;
	   	case PRINT:
   	    s = "PRINT";
   	    break;
	   	case EXIT:
	   	    s = "EXIT";
	   	    break;
	   }
	   
	   return s;
	}
	/**
	 * Aktiviert oder deaktiviert diese Action in Abhängigkeit vom
	 * übergebenen Action-Type
	 * @see GuiWindow#STATE_NEW
	 * @param actionType
	 */
	public void setEnabled(int actionState) {
	    switch (actionState) {
	    	case GuiWindow.STATE_EMPTY:
	    	    switch (this.getType()) {
	    	    	case NEW:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    	case SAVE:
	    	    	    this.setEnabled(false);
	    	    	    break;
	    	    	case FIND:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    	case DELETE:
	    	    	    this.setEnabled(false);
	    	    	    break;
	    	    	case RELOAD:
	    	    	    this.setEnabled(false);
	    	    	    break;
	    	    }
	    	 break;   
	    	case GuiWindow.STATE_NEW:
	    	    switch (this.getType()) {
	    	    	case NEW:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    	case SAVE:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    	case FIND:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    	case DELETE:
	    	    	    this.setEnabled(false);
	    	    	    break;
	    	    	case RELOAD:
	    	    	    this.setEnabled(false);
	    	    	    break;
	    	    }
		    	 break;   
	    	case GuiWindow.STATE_OLD:
	    	    switch (this.getType()) {
	    	    	case NEW:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    	case SAVE:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    	case FIND:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    	case DELETE:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    	case RELOAD:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    }
		    	 break;   
	    	case GuiWindow.STATE_SAVED:
	    	    switch (this.getType()) {
	    	    	case NEW:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    	case SAVE:
	    	    	    this.setEnabled(false);
	    	    	    break;
	    	    	case FIND:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    	case DELETE:
	    	    	    this.setEnabled(false);
	    	    	    break;
	    	    	case RELOAD:
	    	    	    this.setEnabled(true);
	    	    	    break;
	    	    }
		    	 break;   
	    }
	}
	abstract void dispose();

	/**
	 * Leere Implementierung wegen abstrakter Methode in GuiMember
	 */
	@Override
	public final void verify(boolean checkNN) {
	}
	
	public String toString() {
		return "label="+ this.getLabel() + " name=" + this.getName() + " cmd=" + this.getActionCommand();
	}
}