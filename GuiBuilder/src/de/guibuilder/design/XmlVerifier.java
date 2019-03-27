package de.guibuilder.design;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import de.guibuilder.framework.GuiUtil;
/**
 Validiert eine Oberflächenspezifikation gegen gdl.dtd.
 */
public final class XmlVerifier {
  private static int errLine = -1;
  private static XmlVerifier me = new XmlVerifier();

  /**
   * Private Constuktor; setzt die GuiFactory und die CodeBase
   * für die zu lesenden XML-Dokumente aus GuiUtil.
   * @see GuiUtil#getCurrentDir
   */
  private XmlVerifier() {
  }
  /**
  Liefert die Instanz dieser Klasse
  */
  public static XmlVerifier getInstance() {
    return me;
  }
  /**
   Liefert die Zeile mit dem Fehler oder -1 wenn kein Fehler
   */
  public static int getErrorLine() {
    return errLine;
  }
  /**
   Liefert die Zeile mit dem Fehler oder -1 wenn kein Fehler.
   */
  public final int verifyFromString(String source) {
    boolean validate = false;
    // TODO : Schema
    // Wenn ein DOCTYPE angegeben ist, dann validieren.
    if (source.indexOf("<!DOCTYPE") != -1) {
      validate = true;
    }
    byte[] buf = source.getBytes();
    ByteArrayInputStream bin = new ByteArrayInputStream(buf);
    return verify(bin, validate);
  }
  private int verify(InputStream in, boolean validate) {
    errLine = -1;

    InputSource input = new InputSource(in);
    String cb = GuiUtil.getCodeBase().toString();
    cb = cb.replace('\\', '/'); // für Application!
    input.setSystemId(cb);
    try {
      // Dauert nur beim ersten Mal lange vermutlich wegen ClassLoader
      // sonst 20-30ms
      //long lastTime = System.currentTimeMillis(); // Zeit messen
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setValidating(validate);
      DocumentBuilder db = dbf.newDocumentBuilder();
      db.setErrorHandler(new MyErrorHandler());
      @SuppressWarnings("unused")
      Document doc = db.parse(input);
      //System.out.println(System.currentTimeMillis()-lastTime);
    } catch (Exception ex) {
      GuiUtil.showEx(ex);
    }
    return errLine;
  }
  private static class MyErrorHandler implements ErrorHandler {
    public void error(SAXParseException ex) {
      showMessage(ex, "Error");
    }
    public void fatalError(SAXParseException ex) {
      showMessage(ex, "Fatal");
    }
    public void warning(SAXParseException ex) {
      showMessage(ex, "Warning");
    }
    private void showMessage(SAXParseException ex, String caption) {
      errLine = ex.getLineNumber();
      JOptionPane.showMessageDialog(null, ex.getMessage()
      +"\nLine: "+Integer.toString(ex.getLineNumber())
      +"\nColumn: "+Integer.toString(ex.getColumnNumber()),
      caption, JOptionPane.ERROR_MESSAGE);
    }
  }
}