package de.guibuilder.framework;

import java.util.*;

/**
 * Diese Klasse hält ein Keyword aus einer Spezifikation.
 * Das Keyword hält eine Liste der spezifizierten Attribute.
 * Diese Daten werden durch GuiFactory.makeKeywordList(String source)
 * erzeugt.
 * Sie werden für das Erstellen der Oberfläche und den Codegenerator verwendet.
 */
public final class CurrentKeyword {
  /**
   * Klartext des Schlüsselwortes (z.b. "Date" oder "Kalender")
   */
  public String sKeyword;
  /**
   * Kennzeichen, ob das ein Plugin ist (z.B. bei "Kalender")
   */
  boolean isPlugin;
  /**
   * Key des Schlüsselwortes in der HashMap (z.B. keyDATE)
   */
  Integer iKeyword;
  /**
   * Titel der Komponente oder leer, wenn kein Titel.
   */
  public String title = "";
  /**
   * Kennzeichen, ob das die letzte Komponente in einer Spezifikationszeile ist.
   * Wenn ja, dann muß die nächste Komponete am linken Rand unterhalb gesetzt werden,
   * also y um die Höhe weiterzählen und x auf 0 setzen.
   */
  public boolean eol = true;
  /**
   * Enthält den Kommentar zu dem Keyword.
   * <BR>
   * Dieser wird bei der XML-Generierung aus GDL übernommen.
   */
  public String comment = null;
  /**
   * ArrayList der spezifizierten Attribute zu diesem Schlüsselwort.
   * @see CurrentAttrib
   */
  public ArrayList<CurrentAttrib> vAttrib = new ArrayList<CurrentAttrib>();
  /**
   * Mit dieser HashMap wird festgestellt, ob ein Attribut zu
   * diesem Keyword bereits gesetzt ist.
   * Beim erneuten setzen eines Attributes wird das alte
   * überschrieben.
   * @see #add
   */
  HashMap<String, Integer> hash = new HashMap<String, Integer>();
  /**
   *  Eine HashMap von CurrentKeywords mit den Defaulteinstellungen der Komponenten.
   *  <BR>
   *  Über den Namen der Komponente kann auf CurrentKeyword zugegriffen werden.
   *  Wird eingelesen aus "Repository.txt"
   *  @see GuiFactory#fillDefKeys
   */
  private static HashMap<String, CurrentKeyword> defKeys;
  /**
  Information über Version (Siehe Attribute case="Berlin|Köln")
  */
  public String version;
  /**
   * Attribute OnCreate="[ActionCommand]" true / false
   */
  public boolean onCreate;
  // Constructor
  /**
   *  Erzeugt ein Schlüsselwort mit einem bestimmten Namen (XML-Notation).
   *  @param name Das Schlüsselwort.
   */
  CurrentKeyword (String name) {
    this.setKeyword(name);
  }
  /**
   * Erzeugt ein noch unspezifiziertes Keyword (alte Notation)
   * @see #setKeyword
   */
  CurrentKeyword() {
  }
  /**
   * Setzt das Keyword und die Default-Einstellungen.
   */
  void setKeyword(String s) {
    this.sKeyword = s;
    CurrentKeyword defaultKey;
    CurrentAttrib curAtt;

    if (defKeys != null) {
      defaultKey = defKeys.get(this.sKeyword);
      if (defaultKey != null) {
        vAttrib = (ArrayList)defaultKey.vAttrib.clone();
        for (int i = 0; i < defaultKey.vAttrib.size() ; i++) {
          curAtt=defaultKey.vAttrib.get(i);
          hash.put(curAtt.sKeyword, new Integer(i));
        }
        this.title = defaultKey.title;
      }
    }
  }

  /**
   * Fügt dem Keyword ein Attribut hinzu.
   * Sollte das Attribut bereits gesetzt sein, wird es überschrieben
   */
  public void add(CurrentAttrib att) {
    Integer posi = hash.get(att.sKeyword);
		if (att.iKeyword != null && att.iKeyword.intValue() == GuiFactory.msgCREATE) {
			onCreate = true;
		}
    if (posi == null) {
      vAttrib.add(att);
    }
    else {
      try {
        vAttrib.set(posi.intValue(), att);
      }
      catch (Exception ex) {
        GuiUtil.showEx(ex);
      }
    }
  }
  public String toString() {
     StringBuffer buff = new StringBuffer();
     buff.append(this.sKeyword);
     buff.append(": ");
     if (vAttrib != null) {
         for (Iterator<CurrentAttrib> it = vAttrib.iterator(); it.hasNext();) {
             CurrentAttrib at = it.next();
             buff.append(at.sKeyword);
             buff.append(at.sValue);
             buff.append(' ');
         }
     }

     return buff.toString();
  }
  /**
   * Setzt die statische HashMap mit den Defaulteigenschaften der Komponenten.
   */
  static void setDefKeys(HashMap<String, CurrentKeyword> h) {
    defKeys = h;
  }
}