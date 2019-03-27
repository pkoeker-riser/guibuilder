package de.guibuilder.framework;

import java.util.Iterator;

/*
Anforderungen:
Script compilieren, wenn geändert
Compiliertes Script nutzen, wenn vorhanden
Name der Klasse aus xml-File oder aus pnut-File (wenn src=)
Packages für Scripte
Compilierte Scripts im JAR-File (Je Projekt/Package? eines)
Einsatz im Applet
*/

/**
 * Diese Funktion wurde aus GuiFactory ausgelagert, damit wenn
 * Pnuts oder BeanShell nicht installiert ist, es zu keiner ClassNotFound
 * Exception kommt.
 */
final class GuiFactoryHelper {
  static void perfScript(CurrentKeyword curKey, GuiFactory.CurContext c, String filename) {
    CurrentAttrib curAtt = null;
    Iterator<CurrentAttrib> a = null;
    String source = null;
    String language = "Pnuts";
    for (a = curKey.vAttrib.iterator() ; a.hasNext() ;) {
      curAtt = a.next();
      switch (curAtt.iKeyword.intValue()) {
        case GuiFactory.attLANGUAGE:
          language = curAtt.sValue;
          break;
        case GuiFactory.attSRC: source = GuiUtil.fileToString(curAtt.sValue);
          filename = curAtt.sValue;
          break;
        // Wird von der Factory hier eingetragen!
        case GuiFactory.attITEMS: source = curAtt.sValue; break;
      } // End Switch
    } // End For
    if (language.equals("Pnuts")) {
      GuiScripting context = new GuiScriptingPnuts(source);
			c.cpPar.p.setContext(context);
    } else if (language.equals("BeanShell")) {
			GuiScripting context = new GuiScriptingBeanShell(source, filename);
			c.cpPar.p.setContext(context);
//    } else if (language.equals("Groovy")) {
//      GuiScripting context = new GuiScriptingGroovy(source, filename);
//      c.cpPar.p.setContext(context);
    }
  }
}
