package de.guibuilder.framework;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * Implementierung einer Texteingabe. <br>
 * Führende und folgende Blancs werden abgeschnitten.
 * <p>
 * Die minimumSize und die preferredSize sind 40,24
 */
public class GuiText extends GuiComponent implements TableTextColumn {
	static final long serialVersionUID = -576102460375051666L;

	// Attributes
	protected JTextField component;

	/**
	 * Voriger Wert der Komponente vor Tastatureingaben.
	 * 
	 * @see GuiRootPane#setModified
	 */
	protected String oldValue = "";

	/**
	 * Regulärer Ausdruck für Eingabe-Prüfung
	 */
	private Pattern regexp;
	private String msgKeyTyped;
	private boolean hasKeyListener;

	// Constructors
	/**
	 * Erzeugt ein Textfeld mit dem Defaultnamen "text". <br>
	 * Es wird ein Focus- und ein MouseListener eingerichtet.
	 * 
	 * @see GuiFocusListener
	 * @see GuiMouseListener
	 */
	public GuiText() {
		this("text");
	}

	/**
	 * Erzeugt ein Textfeld mit einem Label, das als Name verwendet wird.
	 */
	public GuiText(String label) {
		super(label);
		this.guiInit();
	}

	// Methods
	private void guiInit() {
		final int MIN_SIZE = 40;
		this.setMinimumSize(new Dimension(MIN_SIZE,
				this.getMinimumSize().height));
		if (this.getPreferredSize().width < MIN_SIZE) {
			this.setPreferredSize(new Dimension(MIN_SIZE, this
					.getPreferredSize().height));
		}
		this.setMaxlen(2000);
		// Listener
		this.addFocusListener(new GuiFocusListener(this));
		this.addMouseListener(new GuiMouseListener(this));
	}

	public String getTag() {
		return "Text";
	}
	/**
	 * @return JTextField
	 */
	public JComponent getJComponent() {
		return this.getTextField();
	}
	/**
	 * Liefert die zugrundeliegende Swing-Komponente
	 * @return
	 */
	public JTextField getTextField() {
		if (component == null)
			component = new JTextField();
		return component;
	}

	/**
	 * Liefert STRING; wird von Date,Time,Money,Number überschrieben. From
	 * GuiComponent
	 */
	public int getDataType() {
		return STRING;
	}

	/**
	 * Gibt den Parameter unverändert wieder zurück.
	 * <p>
	 * Wird von Date, Time, Money, Number überschrieben. <br>
	 * Dort wird aus einem unformatierten String ein gemäß des gesetzten
	 * Formates formatierter String erzeugt.
	 * 
	 * @see GuiTable.GuiTableFormatRenderer
	 */
	public String makeFormat(String value) throws ParseException {
		postProc();
		return value;
	}
    public String makeFormat(Number value) throws ParseException {
        postProc();
        if (value == null) return null;
        return value.toString();
    }

	public final boolean isEnabled() {
		return component.isEditable();
	}

	/**
	 * Setzt die Komponente auch "nur anzeigen" wenn "true" übergeben wird:
	 * Hintergrundfarbe auf hellgrau setzen, nicht editierbar, kein TabStop.
     * <p>
     * Wenn die Zwischenablage auch bei enable="false" genutzt werden soll,
     * dann nachträglich tabstop="true" setzen.
	 */
	public void setEnabled(boolean b) {
		component.setEditable(b);
		if (b == false) {
			this.setBackground(GuiUtil.getDisabledColor()); 
			this.setForeground(Color.black);
		} else { // enabled = true
			if (this.notnull) {
				this.setBackground(GuiUtil.getNNColor());
			} else {
				this.setBackground(Color.white);
			}
		}
		this.setTabstop(b);
	}
	/**
	 * Wird überschrieben, wegen dem "merkwürdigen" Verhalten beim
	 * Einsatz von Winlaf.
	 */
	void gotFocus(FocusEvent e) {
		super.gotFocus(e);
		if (this.getParentTable() != null) {
//			System.out.println("gotFocus: "+this.getValue());
//      	System.out.println(component.getSelectionStart()+"/"+
//      	component.getSelectionEnd()+"-"+component.getCaretPosition());
			component.setSelectionStart(component.getSelectionEnd());
		}
	}
	/**
	 * <ul>
	 * <li>GuiComponent#postProc
	 * <li>Update LinkTable wenn vorhanden (linkCol=).
	 * <li>Setzt RootPane auf isModified, wenn Inhalt verändert wurde.
	 * <li>Setzt GuiChangeEvent ab wenn spezifiziert (OnChange=)
	 * </ul>
	 */
	public final void lostFocus(FocusEvent e) {
		// Set modified am Anfang!
		if (this.getText().equals(oldValue) == false) {
			this.setModified(true);
		}
		super.lostFocus(e); // GuiComponent (OnLostFocus); GuiElement
							// (Statuszeilentext, stopCellEditing)
		if (e == null || e.isTemporary() == false) {
			// PostProcessor
			this.postProc();
			// OnChange
			if (this.isModified()) {
			  this.changed();
			}
			this.oldValue = this.getText();
		}
	}
	/**
	 * Löst das OnChange-Ereignis aus
	 * @return true, wenn OnChange-Eigenschaft definiert
	 */
	boolean changed() {	  
    if (actionChange != null && getRootPane() != null) {
      this.getRootPane().obj_ItemChanged(this, this.actionChange, this.getValue());
      return true;
    }
    return false;
	}

	/**
	 * Setzt einen regulären Ausdruck zur Eingabeüberprüfung. Es wird eine
	 * Fehlermeldung ausgegeben, wenn der Ausdruck keine korrekte Syntax hat.
	 * <p>
	 * Bei übergabe von null wird der regläre Ausdruck gelöscht.
	 */
	public final void setRegexp(String s) {
		try {
			if (s == null) {
				this.regexp = null;
			} else {
				this.regexp = Pattern.compile(s);
				this.setInputVerifier();
			}
		} catch (PatternSyntaxException ex) {
			GuiUtil.showEx(ex);
		}
	}

	/**
	 * Liefert den Regulären Ausdruck zur Eingabeprüfung oder null, wenn keine
	 * Eingabeprüfung mit RE vorgenommen wird.
	 * 
	 * @see #setRegexp
	 */
	public final Pattern getRegexp() {
		return regexp;
	}

	/**
	 * Installiert einen KeyListener.
	 * <p>
	 * Siehe Attribut OnKeyTyped= <br>
	 * Wenn als ActionCommand null übergeben wird, wird der KeyListener wieder
	 * deinstalliert.
	 */
	public final void setMsgKeyTyped(String msg) {
		this.msgKeyTyped = msg;
		if (hasKeyListener == false && msg != null) {
			component.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					//##getRootPane().obj_KeyEvent(GuiText.this, msgKeyTyped, getValue(), e);
				}

				public void keyReleased(KeyEvent e) {
					getRootPane().obj_KeyEvent(GuiText.this, msgKeyTyped, getValue(), e);
				}

				public void keyTyped(KeyEvent e) {
					//##getRootPane().obj_KeyEvent(GuiText.this, msgKeyTyped, getValue(), e);
				}
			});
			hasKeyListener = true;
		} else if (hasKeyListener == true && msg == null) {
			KeyListener[] els = (KeyListener[]) component
					.getListeners(KeyListener.class);
			for (int i = 0; i < els.length; i++) {
				component.removeKeyListener(els[i]);
			}
			hasKeyListener = false;
		}
	}

	/**
	 * Setzt den Inhalt der Componente. muß ein String sein; "null" wird zu
	 * Leerstring. Der Zustand <code>modified</code> wird auf "false" gesetzt.
	 */
	@Override
  public void setValue(Object val) {
		if (val == null) {
			this.setText((String)val);
		} else {
			this.setValue(val.toString());
		}
		super.setValue(val);
		oldValue = this.getText();
		component.setCaretPosition(0); // 9.3.2011 Damit zu große Werte linksbündig angezeigt werden
	}
	public void setValue(String val) {
		this.setText(val);
	}

	@Override
  public Object getUnformatedValue() {
		return this.getText();
	}

	/**
	 * Liefert den Inhalt der Componente als String. Schneidet hinten und vorne
	 * Blancs ab.
	 * <p>
	 * Eine leere Eingabe ist ein Leerstring; also nie null. <br>
	 * Fraglich ist, ob dieses Verhalten schlau ist; denn wird hier mit
	 * setValue() null zugewiesen, wird mit getValue ein Leerstring geliefert.
	 * <br>
	 * Das ganze liegt an setText und getText von JTextField.
	 */
	@Override
  public Object getValue() {
		try {
			String value = this.getText();
			if (value != null) {
			   if (value.startsWith(" ") || value.endsWith(" ")) {			
			      value = value.trim();
			      this.setText(value);
			   }
			   while (value.endsWith("\t") || value.endsWith("\r") || value.endsWith("\n") || value.endsWith(" ")) {
			      value = value.substring(0, value.length()-1);
                  this.setText(value);
			   }
			}
			return makeFormat(value);
		} catch (ParseException ex) {
			return null;
		}
	}

	public boolean isValid() {
		InputVerifier verifier = getJComponent().getInputVerifier();
		if (verifier != null) {
			return verifier.verify(this.getJComponent());
		} else {
			return true;
		}
	}

	// see GuiMember
	public final void reset() {
		this.setValue(null);
		this.setModified(false);
	}

	/**
	 * Ist dieses Objekt eine Tabellenspalte, wird diese Nachricht auch an die
	 * Tabelle weitergeleitet.
	 * 
	 * @see GuiRootPane#obj_DblClick(GuiComponent, String, Object, MouseEvent)
	 */
	public final void d_click(MouseEvent e) {
		if (this.getParentTable() != null) {
			this.getParentTable().d_click(e);
		}
		if (actionDblClick != null && getRootPane() != null) {
			getRootPane()
					.obj_DblClick(this, actionDblClick, this.getValue(), e);
		}
	} // End of d_click()

	/**
	 * see JTextField
	 */
	public final String getText() {
		return component.getText();
	}

	/**
	 * see JTextField
	 */
	public void setText(String s) {
		component.setText(s);
		//updateLinkedColumn(); // Führt zu Endlosschleife!
	}

	/**
	 * see JTextField
	 */
	public final void setHorizontalAlignment(int i) {
		component.setHorizontalAlignment(i);
	}

	/**
	 * see JTextField
	 */
	public final int getHorizontalAlignment() {
		return component.getHorizontalAlignment();
	}

	/**
	 * see JTextField
	 */
	public final void setColumns(int i) {
		if (component != null) {
			component.setColumns(i);
		}
	}
	/**
	 * @return String.class
	 */
	public Class getValueClass() {
		return String.class;
	}
}