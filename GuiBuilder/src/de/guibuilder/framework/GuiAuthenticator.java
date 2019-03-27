package de.guibuilder.framework;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Zuständig für die Eingabe von Usernamen und Password, wenn eine URL
 * hiermit geschützt ist. Besonders für HTTP-PUT.
 */
public final class GuiAuthenticator extends Authenticator {
  /**
   * User Name; default = guest
   */
  private String username = "guest";
  /**
   * Password; default = guest
   */
  private String password = "guest";
  // Constructor
  public GuiAuthenticator() {
    super();
  }
  // Methods
  /**
   * Ruft einen modalen Dialog zur Eingabe von Usernamen und Password auf.
   */
  public PasswordAuthentication getPasswordAuthentication() {
    GuiFactory fact = GuiFactory.getInstance();
    GuiDialog dia = null;
    String prompt = this.getRequestingPrompt();
    if (prompt == null) {
   	 prompt = "[Realm]";
    }
    try {
      dia = (GuiDialog)fact.createWindowXml(
      		"<?xml version='1.0' encoding='UTF-8'?>"
      		+ "<!DOCTYPE GDL SYSTEM 'gdl.dtd'>"
      		+ "<GDL>"
      		+ "<Dialog label='Username and Password required' type='MODAL' w='340' h='220'>"
      		+ "<Label label='" + prompt + "' w='2' />"
      		+ "<Text label='Username:' w='1' it='10' ref='*' />"
      		+ "<Password label='Password:' w='1' it='10' ref='*' />"
      		+ "<Panel wy='0' w='2' it='20' >"
      			+ "<Button label='OK' eol='false' /> " 
      			+ "<Button label='Cancel' />"
      		+ "</Panel>"
      		+ "</Dialog>"
      		+ "</GDL>");
      if (dia.showDialog()) {
        username = (String)dia.getValue("username");
        password = (String)dia.getValue("password");
        return new PasswordAuthentication(username, password.toCharArray());
      }
      else {
        return null; // geht nicht!
      }
    } catch (GDLParseException ex) {
      GuiUtil.showEx(ex);
      return null;
    }
  }
}