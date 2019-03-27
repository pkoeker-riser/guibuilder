package de.guibuilder.framework;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * Text- oder HTML-Editor.<p>
 * Die maximale Eingabelänge wird auf 128K gesetzt.<br>
 * Der HyperlinkListener funktioniert nur, wenn der Editor auf nicht editierbar gesetzt wird.
 */
public final class GuiEditor extends GuiMultiLine {
  // Attributes
  private JEditorPane component = new JEditorPane();
  private ArrayList<URL> pages;
  private int pointer = -1;
  // Constructor
  /**
   * Der Name wird standardmäßig auf "editor" gesetzt.<br>
   * Es wird ein HyperlinkListener eingerichtet.<br>
   * Die maximale Eingabelänge auf 128K
   */
  public GuiEditor() {
    super();
    this.setName("editor");
    setMaxlen(128000);
    // Nested KeyListener
    component.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
      }
      public void keyReleased(KeyEvent e) {
      }
      public void keyTyped(KeyEvent e) {
        if (isModified() == false && getRootPane() != null) {
          setModified(true);
          getRootPane().setModified(true);
        }
      }
    });
    // Nested HyperlinkListener
    component.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          JEditorPane pane = (JEditorPane) e.getSource();
          if (e instanceof HTMLFrameHyperlinkEvent) {
            HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
            HTMLDocument doc = (HTMLDocument)pane.getDocument();
            doc.processHTMLFrameHyperlinkEvent(evt);
          }
          else {
            try {
              pane.setPage(e.getURL());
              pages.add(e.getURL());
              pointer++;
            } catch (Exception ex) {
              GuiUtil.showEx(ex);
            }
          }
        }
      }
    }); // End Of addHyperlinkListener
  }
  // Methods
  public final String getTag() {
    return "Editor";
  }
  public JComponent getJComponent() {
    return component;
  }
  JEditorPane getEditor() {
    return component;
  }
  /**
   * Setzt den Inhalt des Editors neu. Es muß ein String übergeben werden.
   */
  public void setValue(Object val) {
    if (val == null) {
      this.setText("");
    } else {
      this.setText((String)val);
    }
    this.setModified(false);
  }
  
  public Object getUnformatedValue()
  {
	return this.getText();	
  }
  
  /**
   * Liefert den Inhalt des Editors als String.
   */
  public Object getValue() {
    return this.getText();
  }
  
  public void reset() {
    this.setText(null);
    this.setModified(false);
  }
  // Browser

  public void resetPages() {

  	if( pages != null )
		pages = new ArrayList<URL>();
  }
  public void setPage(String surl) throws IOException {
    URL url = new URL(surl);
    this.setPage(url);
  }
  /**
   * Setzt den Inhalt der Komponente auf die übergebene URL.
   */
  public void setPage(URL url) throws IOException {
    component.setPage(url);
    
    InputStream in = url.openStream();
    InputStreamReader isr = new InputStreamReader(in);
    StringBuffer buff = new StringBuffer();

    int len;
    while ((len = isr.read()) != -1) {
        buff.append((char) len);
    }
    in.close();
    isr.close();
    setText(buff.toString()); 
    if (pages == null) {
      pages = new ArrayList<URL>();
    }
    boolean bFound = false;
    for(int i=0; i < pages.size(); i++) {
    	URL aktUrl = pages.get(i);
    	if( aktUrl.toString().equalsIgnoreCase(url.toString()) )
    		bFound = true;
    }
    if( !bFound )
    {
   		pages.add(url);
    	pointer++;
    }
  }
  /**
   * Browser-Funktionalität bei HTML
   */
  public void back() {
    if (pointer > 0) {
      try {
        pointer--;
        this.setPage(pages.get(pointer));
      } catch (Exception ex) {
        GuiUtil.showEx(ex);
      }
    }
  }
  /**
   * Browser-Funktionalität bei HTML
   */
  public void forward() {
    if (pointer < pages.size() -1) {
      try {
        pointer++;
        this.setPage(pages.get(pointer));
      } catch (Exception ex) {
        GuiUtil.showEx(ex);
      }
    }
  }
  /**
   * Browser-Funktionalität bei HTML
   */
  public void home() {
    pointer = 0;
    try {
      this.setPage(pages.get(pointer));
    } catch (Exception ex) {
      GuiUtil.showEx(ex);
    }
  }
  //From GuiMuliLine
  public String getText() {
  	try {
  		return component.getText();
  	}
  	catch(Exception ex) {
  		return "";
  	}
  }
  /**
   * Setzt den Text auf Leerstring wenn dieser null; null würde leider zum Fehler führen.
   */
  public void setText(String s) {
    if (s == null) {
      component.setText("");
    } else {
      component.setText(s);
    }
  }
  /**
   * Setzt den Content-Type auf das übergebene Mime-Format; z.B. "text/html".
   */
  public void setContentType(String s) {
    s = s.replace('_', '/');
    component.setContentType(s);
  }
  /**
   * Wird auf setEditable gemapt.
   */
  public void setEnabled(boolean b) {
    component.setEditable(b);
  }
  /**
   * Setzt den Font auf BOLD oder auf !BOLD
   */
  public void setBold(boolean b) {
    Font font = component.getFont();
    if (b) {
      if (font.isBold() == false) {
        font = new Font(font.getFontName(), font.getStyle() | Font.BOLD, font.getSize());
        component.setFont(font);
      }
    } else {
      if (font.isBold() == true) {
        font = new Font(font.getFontName(), font.getStyle() ^ Font.BOLD, font.getSize());
        component.setFont(font);
      }
    }
  }
	/**
	 * Setzt den Font auf ITALIC oder auf !ITALIC
	 */
	public void setItalic(boolean b) {
		Font font = component.getFont();
		if (b) {
			if (font.isItalic() == false) {
				font = new Font(font.getFontName(), font.getStyle() | Font.ITALIC, font.getSize());
				component.setFont(font);
			}
		} else {
			if (font.isItalic() == true) {
				font = new Font(font.getFontName(), font.getStyle() ^ Font.ITALIC, font.getSize());
				component.setFont(font);
			}
		}
	}
}