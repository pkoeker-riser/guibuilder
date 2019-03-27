package de.guibuilder.framework;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;


/**
 * Implementierung einer mehrzeiligen Texteingabe.
 */
public class GuiMemo extends GuiMultiLine {
   // Attributes
  private JTextArea component = new JTextArea();
  private final UndoManager undo = new UndoManager();

  // Constructors
  /**
   * Erzeugt eine Notizfeld mit dem Default-Namen "memo".<br>
   * Die maximale Eingabelänge beträgt 64KB.
   */
  public GuiMemo() {
    super();
    this.setName("memo");
    this.guiInit();
  }
  // Methods
  /**
   * Es wird ein Key-Listener eingerichtet zwecks prüfung, ob sich der Inhalt der
   * Komponente geändert hat.<br>
   * Wird an RootPane weiter gereicht.
   * @see GuiRootPane#setModified
   */
  private void guiInit() {
    this.setMaxlen(64000);
    this.component.setTabSize(3); // Tabstops alle drei Zeichen
    this.component.setLineWrap(true);
    this.component.setWrapStyleWord(true); // WordWrap
    //component.setDoubleBuffered(true);
    // Focuslistener
    this.addFocusListener(new GuiFocusListener(this));
    // MouseListener für DblClick
    this.addMouseListener(new GuiMouseListener(this));
    // KeyListener für modified
    this.component.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
      }
      public void keyReleased(KeyEvent e) {
      }
      public void keyTyped(KeyEvent e) {
        if (isModified() == false) {
          setModified(true);
          getRootPane().setModified(true);
        }
      }
    });
    // UNDO,REDO
    Document doc = this.component.getDocument();

    doc.addUndoableEditListener(new UndoableEditListener() {
      public void undoableEditHappened(UndoableEditEvent evt) {
        undo.addEdit(evt.getEdit());
      }
    });

    component.getActionMap().put("Undo", new AbstractAction("Undo") {
      public void actionPerformed(ActionEvent evt) {
        try {
          if (undo.canUndo()) {
            undo.undo();
          }
        } catch (CannotUndoException e) {
        }
      }
    });
    component.getActionMap().put("Redo", new AbstractAction("Redo") {
       public void actionPerformed(ActionEvent evt) {
         try {
           if (undo.canRedo()) {
             undo.redo();
           }
         } catch (CannotUndoException e) {
         }
       }
     });

    component.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
    component.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
  }
  public final String getTag() {
    return "Memo";
  }
  /**
   * Liefert eine JTextArea
   * @see #getMemo
   */
  public JComponent getJComponent() {
    return component;
  }
  /**
   * Delegation to JTextArea
   */
  public String getText() {
    return component.getText();
  }
  /**
   * Setzt den Cursor an das Ende der angegebenen Zeile.
   */
  public void setLine(int line) {
    int cnt = 0;
    int posi = 0;
    String ll = null;
    final StringTokenizer lines = new StringTokenizer(this.getText(), "\n\r");
    while (lines.hasMoreTokens() && cnt < line) {
      cnt++;
      ll = lines.nextToken();
      posi = posi + ll.length() + 1;
    }
    component.requestFocus();
    component.setCaretPosition(posi-1);
  }
  /**
   * Setzt die Komponente auch "nur anzeigen" wenn true:<br>
   * Hintergrundfarbe auf Hellgrau, nicht editierbar, kein TabStop.
   */
  public void setEnabled(boolean b) {
    component.setEditable(b);
    if (b == false) {
      this.setBackground(new Color(230, 230, 230)); // TODO: Property!
    }
    else {
			if (this.isNotnull()) {
				final Color bg = GuiUtil.getNNColor();
				if (bg != null) {
					this.setBackground(bg);
				}
			} else {
				component.setBackground(Color.white);
			}
    }
    this.setTabstop(b);
  }
  public boolean isEnabled() {
     return component.isEditable();
  }
  public final void lostFocus(FocusEvent e) {
    super.lostFocus(e); // GuiComponent (OnLostFocus, LinkTable)--> GuiElement (Statuszeile)
    if (e.isTemporary() == false) {
    	this.postProc();
      if (isModified()) {
        if (actionChange != null && getRootPane() != null) {
          getRootPane().obj_ItemChanged(this, actionChange, this.getValue());
        }
      }
    }
  }

  /**
   * Setzt den Inhalt der Komponente als String.
   * @see GuiComponent
   */
  public void setValue(Object val) {
    this.setText((String)val);
    this.setModified(false);
    component.repaint();
  }
  
  public Object getUnformatedValue()
  {
	return this.getText();	
  }
  
  /**
   * Liefert den Inhalt der Komponente als String.
   */
  public Object getValue() {
    return this.getText();
  }
  // From GuiMember
  public void reset() {
    this.setText(null);
    this.setModified(false);
  }
  /**
   * Weiterleiten von "OnDblClick" an Controller
   * @see GuiRootPane#obj_DblClick
   */
  public void d_click(MouseEvent e) {
    if (actionDblClick != null) {
      getRootPane().obj_DblClick(this, actionDblClick, this.getValue(), e);
    }
  }
  /**
   * Delegation to JTextArea.
   * setCaretPosition(0).
   */
  public void setText(String s) {
    component.setText(s);
    component.setCaretPosition(0);
    setModified(false);
  }
  /**
   * Delegation to JTextArea
   */
  void setColumns(int i) {
    component.setColumns(i);
  }
  /**
   * @see #getJComponent
   */
  public JTextArea getMemo() {
    return component;
  }
  public boolean canUndo() {
	  return this.undo.canUndo();
  }
  public boolean canRedo() {
	  return this.undo.canRedo();
  }
}
