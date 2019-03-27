package de.guibuilder.adapter;

import java.util.Vector;

/**
 * Dieses Interface ist dafür vorgesehen, über SOAP die Dienste von GuiAPI
 * auch anderen Anwendungen auf dem Client (z.B. Office) anzubieten.
 */
public interface GuiAPIF {
  // Attributes
  // Methods
  /**
  Setzt die Codebase für alle Klassen.
  <BR>
  Die DocumentBase wird gleichzeitig auf diesen Wert gesetzt, wenn sie nicht null ist.
  <BR>
  Als Protokoll ist "http" oder "file" erlaubt.
  @see #setDocumentBase
  */
  public void setCodeBase(String url);
  /**
  Setzt die DocumentBase relativ zur CodeBase.
  <BR>
  Es handelt sich hierbei um dasjenige Verzeichnis, aus dem die Factory ihre
  Sourcen (Spezifikation, Images) bezieht, wenn keine absolute Adresse angegeben.
  <BR>
  Ist üblicherweise ein Unterverzeichnis der CodeBase;
  die CodeBase muß zuvor gesetzt werden.
  @see de.guibuilder.framework.GuiUtil#setDocumentBase
  @see #setCodeBase
  */
  public void setDocumentBase(String documentBase);
  /**
  Setzt den Debug-Modus für die Factory.
  @see de.guibuilder.framework.GuiUtil#setDebug
  */
  public void setDebug(boolean b);
  /**
  Setzt die Version für die Factory.
  <BR>
  Ist das Argument null; wird die Version auf "default" gesetzt.
  @see de.guibuilder.framework.GuiUtil#setVersion
  */
  public void setVersion(String version);
  /**
  Setzt den UI Manager.
  <BR>
  Erlaubte Werte sind "windows", "metal", "motif".
  <BR>
  Gro�- und Kleinschreibung ist egal.
  <BR>
  Es darf auch null übergeben werden, dann passiert aber auch n�scht.
  */
  public void setUiManager(String ui);
  /**
  Sanduhr einschalten.
  */
  public void start();
  /**
  Sanduhr ausschalten.
  */
  public void end();
  /**
  Anwendung beenden.
  <p>
  Bei einer Application wird System.exit aufgerufen; bei einem Applet werden alle
  Fenster geschlossen und der Pinger beendet.
  */
  public void exit();
  /**
  Neues Fenster erstellen und anzeigen.
  <BR>
  Das Fenster erhält den angegebenen Namen und die Id.
  Die WindowId wird von der Dialogsteuerung vergeben und muß je Client-Session
  eindeutig sein;
  hierüber kann der Client auch veranla�t werden,
  das selbe Fenster mehrfach zu öffnen
  (etwa wenn aus der Personenauskunft heraus die Auskunft
  über eine andere Person aufgerufen werden soll).
  <BR>
  Die WindowId dient sowohl dem GuiBuilder als auch der Dialogsteuerung zur
  Identifikation eines Fensters.
  Bei allen Benutzeraktionen wird die hier vergebene WindowId gemeldet.
  <p>
  Es wird nur angegeben, welche Spezifikationsdatei angefordert werden soll.
  Der eigentliche Transport der Spezifikationen muß über einen anderen Kanal
  (z.B. HTTP) erfolgen.
  <BR>
  Dieses erm�glicht es dem Client,
  die Gui-Spezifikationen - oder noch besser das ganze Fenster -
  in einem Cache für den wiederholten Gebrauch vorzuhalten.
  <BR>
  Die Dialogsteuerung selbst kann mit den Methoden hideWindow und showWindow ein Fenster
  verstecken bzw. ein verstecktes Fenster wieder anzeigen.
  Hierüber kann die Performanz der Anwendung - bei entsprechendem Speicherbedarf -
  gesteigert werden.
  @param windowName Ein beliebiger Name für das Fenster.
  @param windowId Eine eindeutige Id für dieses Fenster.
  @param fileName Dateiname eine Gui-Spezifikation relativ zur DocumentBase.
  @see #hideWindow
  @see #showWindow
  @see #createWindow
  @see #setDocumentBase
  */
  public void openWindow(String windowName, String windowId, String fileName);
  /**
  Erzeugt das Fenster wie <code>openWindow</code>, aber zeigt es nicht an.
  Das Fenster kann anschlie�end mit <code>showWindow</code> sichtbar gemacht werden.
  <br>
  Es wird der Default.Controller gesetzt.
  @see #openWindow
  @see #showWindow
  */
  public void createWindow(String windowName, String windowId, String fileName);
  /**
  Erzeugt ein Fenster wie createWindow.
  <BR>
  Statt eines Dateinamens wird hier ein String erwartet, der ein XML-Dokument
  enthält.
  Das Fenster kann anschlie�end mit <code>showWindow</code> sichtbar gemacht werden.
  @see #openWindow
  @see #showWindow
  */
  public void createWindowXml(String windowName, String windowId, String doc);
  /**
  Das Fenster mit der angegebenen WindowId ist zu schlie�en (dispose).
  <BR>
  Es wird eine Exception geworfen, wenn diese WindowId bisher nicht ge�ffnet wurde.
  <P>
  PENDING: Was passiert mit currentWindow?
  */
  public void closeWindow(String windowId);
  /**
  Macht ein Fenster unsichtbar.
  Achtung! Der Fensterinhalt bleibt dabei erhalten!
  @see #showWindow
  @see #resetWindow
  */
  public void hideWindow(String windowId);
  /**
  Zeigt ein mit <code>createWindow</code> erzeugtes
  oder mit <code>hideWindow</code> verstecktes Fenster an.
  @see #hideWindow
  @see #resetWindow
  */
  public void showWindow(String windowId);
  /**
   * Zeigt einen modalen Dialog.<br>
   * @see de.guibuilder.framework.GuiDialog#zeige
   */
  public boolean showModalDialog(String windowId);
  /**
  Es wird ein anderes Fenster aktiviert, wenn mehrere gleichzeitig offen sind;
  bewirkt einen Focuswechsel zwischen verschiedenen Fenstern.
  <BR>
  Es wird eine Exception geworfen, wenn diese WindowId bisher nicht ge�ffnet wurde.
  <BR>
  Als Folge dieser Anweisung wird von dem Fenster die Nachricht "windowActivated"
  gesendet.
  */
  public void activateWindow(String windowId);
  /**
  Alle Felder der Fensters leeren, alle Registerkarten, Menüeintr�ge und Buttons
  werden enabled.
  */
  public void resetWindow(String windowId);
  /**
  Leert die Felder eines einzelnen Panels (zumeist eine Registerkarte).
  */
  public void resetPanel(String windowId, String name);
  /**
  Selektiert oder deselectiert alle TextComponents des Panels.
  <br>
  Wenn name null, dann MainPanel.
  */
  //##public void selectPanel(String windowId, String name, boolean b);
  /**
  Setzt die Titelzeile des Fensters neu.
  */
  public void setWindowTitle(String windowId, String value);
  /**
  Es soll eine andere Registerkarte aktiviert werden.
  */
  public void activateTab(String windowId, String tabName);
  /**
  Die ganze Registerkarte wird für Benutzeraktionen gesperrt (false),
  d.h. daß sie auch nicht mehr angeklickt werden kann; bzw wieder aktiviert (true).
  */
  public void enableTab(String windowId, String tabName, boolean b);
  /**
  Setzt den Wert der angegebenen Komponente auf den angegebenen Wert.
  <P>
  Diese Methode kann auch für geschachtelte Container verwendet werden,
  wenn der Name der Componente in Punkt-Notation angegeben wird:
  <BR>
  Beispiel: <code>
  setValue("myWindowId", "myTab.myPanel.myComponent", "neuer Wert");</code>
  */
  public void setValue(String windowId, String name, String value);
  /**
  Setzt den Wert der angegebenen Komponente
  - die auf der angegebenen Registerkarte liegt - auf den angegebenen Wert.
  */
  public void setValue(String windowId, String tabName, String name, String value);
  /**
  Liefert den Wert der angegebenen Komponente.
  */
  public String getValue(String windowId, String name);
  /**
  Liefert den Wert der angegebenen Komponente,
  die auf der angegebenen Registerkarte liegt.
  @param windowId des Fensters
  @param tabName Name einer Registerkarte (Panel oder Group) dieses Fensters.
  @param name Name der Komponente auf der Registerkarte.
  @return Inhalt der Komponente.
  */
  public String getValue(String windowId, String tabName, String name);
  /**
  Aktiviert oder deaktiviert Menüeintr�ge und Buttons (auch Toolbar).
  */
  public void enable(String windowId, String name, boolean b);
  /**
  Aktiviert oder deaktiviert Eingabekomponenten wie Text, Combo usw.(keine Buttons oder Menüeintrage)
  */
  public void enableComp(String windowId, String name, boolean b);
  /**
  Aktiviert oder deaktiviert Eingabekomponenten wie Text, Combo usw.
  die auf einer Registerkarte liegt.
  (keine Buttons oder Menüeintrage)
  */
  public void enableComp(String windowId, String tabName, String name, boolean b);
  //##public void enableComp(String windowId, HashSet components, boolean b);
  /**
  Setzt den Focus auf die angegebene Komponente.
  */
  public void setFocus(String windowId, String name);
  /**
  Setzt den Focus auf die angegebene Komponente.
  */
  public void setFocus(String windowId, String tabName, String name);
  /**
  Selektiert die angegebene Zeile und Spalte der Tabelle.
  */
  public void setFocus(String windowId, String tableName, int row, int col);
  /**
  Selektiert die angegebene Zeile und Spalte der Tabelle.
  */
  public void setFocus(String windowId, String tabName, String tableName, int row, int col);
  /**
  Setzt das Minimum und das Maximum bei Scrollbar und Slider neu.
  PENDING: Bei Scrollbar immer max um 10 gr��er angeben !!!??? (Bug in Swing?)
  @see de.guibuilder.framework.GuiMinMax
  */
  public void setMinMaxValue(String windowId, String name, int min, int max);
  /**
  Setzt das Minimum und das Maximum bei Scrollbar und Slider neu.
  @see de.guibuilder.framework.GuiMinMax
  */
  public void setMinMaxValue(String windowId, String tabName, String name, int min, int max);
  /**
  Setzt das �nderungskennzeichen einer Komponente.
  @see de.guibuilder.framework.GuiComponent#setModified
  */
  public void setModified(String windowId, String name, boolean b);
  public void setModified(String windowId, String tabName, String name, boolean b);
  /**
  F�llt eine List- oder Combobox neu mit Werten.
  */
  public void setItems(String windowId, String name, Vector items);
  /**
  F�llt eine List- oder Combobox neu mit Werten.
  */
  public void setItems(String windowId, String tabName, String name, Vector items);
  /**
  F�llt eine Combobox, die Spalte einer Tabelle ist, neu mit Werten.
  <br>
  tabName darf auch null sein.
  */
  public void setItems(String windowId, String tabName, String tblName, int colIndex, Vector items);
  /**
  F�gt der List- oder Combobox einen Eintrag am Ende hinzu.
  */
  public void addItem(String windowId, String name, String item);
  /**
  F�gt der List- oder Combobox einen Eintrag am Ende hinzu.
  */
  public void addItem(String windowId, String tabName, String name, String item);
  /**
  F�gt einen Eintrag an einem bestimmten Index ein.
  */
  public void addItem(String windowId, String name, String item, int index);
  /**
  F�gt einen Eintrag an einem bestimmten Index ein.
  */
  public void addItem(String windowId, String tabName, String name, String item, int index);
  /**
  L�scht den angegebenen Eintrag aus der List- oder Combobox.
  */
  public void removeItem(String windowId, String name, String item);
  /**
  L�scht den angegebenen Eintrag aus der List- oder Combobox.
  */
  public void removeItem(String windowId, String tabName, String name, String item);
  /**
  L�scht alle Eintr�ge aus der List- oder Combobox.
  */
  public void removeAll(String windowId, String name);
  /**
  L�scht alle Eintr�ge aus der List- oder Combobox.
  */
  public void removeAll(String windowId, String tabName, String name);
  /**
  F�gt dem Menü einen neuen Eintrag mit einem Label und einem ActionCommand
  hinzu.
  @param windowId Eine eindeutige Id für dieses Fenster.
  @param menuName Der Name des Menüs, dem der Menüeintrag hinzuzuf�gen ist.
  @param label Beschriftung des Menüeintrags.
  @param name Name des Menüeintrags; wenn null wird das Label als Name verwendet.
  @param cmd ActionCommand, welches beim Bet�tigen dieses Menüeintrags
  übergeben wird; wenn null, wird das Label als ActionCommand verwendet.
  */
  public void addMenuItem(String windowId, String menuName, String label, String name, String cmd);
  /**
  F�gt dem Menü einen neuen Eintrag mit einem Label und einem ActionCommand
  hinzu.
  @param windowId Eine eindeutige Id für dieses Fenster.
  @param menuName Der Name des Menüs, dem der Menüeintrag hinzuzuf�gen ist.
  @param label Beschriftung des Menüeintrags.
  @param name Name des Menüeintrags; wenn null wird das Label als Name verwendet.
  @param cmd ActionCommand, welches beim Bet�tigen dieses Menüeintrags
  übergeben wird; wenn null, wird das Label als ActionCommand verwendet.
  @param type Typ des Menüeintrags: NORMAL, OPTION, CHECK.
  */
  public void addMenuItem(String windowId, String menuName, String label, String name, String cmd, String type);
  public String[] getTableColumnValues(String windowId, String tableName, int col);
  /**
   * Liefert den Inhalt der Tabelle als einen Vector von TableRows
   * @see de.guibuilder.framework.GuiTableRow
   */
  public Vector getTableValues(String windowId, String tableName);
  /**
   * Liefert den Inhalt der angegebenen Spalte aus der selektierten Zeile.
   */
  public String getCellValue(String windowId, String tableName, int col);
  /**
   * Liefert den Inhalt der angegebenen Spalte aus der selektierten Zeile.
   */
  public String getCellValue(String windowId, String tabName, String tableName, int col);
  /**
   * Setzt den Wert eine Tabellenzelle neu.
   */
  public void setCellValue(String windowId, String tableName, String value, int row, int col);
  /**
   * Setzt den Wert eine Tabellenzelle neu.
   */
  public void setCellValue(String windowId, String tabName, String tableName, String value, int row, int col);
  /**
   * überschreibt den Inhalt einer Tabellenzeile mit neuen Werten.
   */
  public void setRowValues(String windowId, String tableName, int row, Vector values);
  /**
   * überschreibt den Inhalt einer Tabellenzeile mit neuen Werten.
   */
  public void setRowValues(String windowId, String tabName, String tableName, int row, Vector values);
  /**
   * F�gt eine Zeile der Tabelle am Ende hinzu.
   */
  public void insertRow(String windowId, String tableName, Vector values);
  /**
   * F�gt eine Zeile in die Tabelle an der angegebenen Zeile ein.
   */
  public void insertRow(String windowId, String tableName, int row, Vector values);
  /**
   * F�gt eine Zeile der Tabelle am Ende hinzu.
   */
  public void insertRow(String windowId, String tabName, String tableName, Vector values);
  /**
   * F�gt eine leere Zeile in die Tabelle ein.
   */
  public void insertRow(String windowId, String tableName);
  /**
   * F�gt eine Zeile in die Tabelle an der angegebenen Zeile ein.
   */
  public void insertRow(String windowId, String tabName, String tableName, int row, Vector values);
  /**
   * L�scht die selektierte Zeile aus der Tabelle.
   */
  public void deleteRow(String windowId, String tableName);
  /**
   * L�scht die angegebene Zeile aus der Tabelle.
   */
  public void deleteRow(String windowId, String tableName, int row);
  /**
   * L�scht die angegebene Zeile aus der Tabelle.
   */
  public void deleteRow(String windowId, String tabName, String tableName, int row);
  /**
   * L�scht die selektierte Zeile aus der Tabelle.
   */
  public void deleteRow(String windowId, String tabName, String tableName);
  /**
   * Liefert die in dieser Sitzung gel�schten Zeilen, die vor der Sitzung
   * vorhanden waren; also nicht die neu eingef�gten und gleich wieder
   * gel�schten Zeilen.
   * @return Vector von GuiTableRows
   * @see de.guibuilder.framework.GuiTableRow
   */
  public Vector getDeletedRows(String windowId, String tableName);
  /**
  F�llt eine Tabelle mit neuen Daten (Vector von Vectoren).
  <B>Achtung!</B>
  <BR>
  Der übergebene Vector wird hier mit clone() an die Tabelle weitergereicht!
  */
  //##public void setTableValues(String windowId, String tableName, Vector values);
  /**
  F�llt eine Tabelle mit neuen Daten (Vector von Vectoren).
  <B>Achtung!</B>
  <BR>
  Der übergebene Vector wird hier mit clone() an die Tabelle weitergereicht!
  */
  //##public void setTableValues(String windowId, String tabName, String tableName, Vector values);
  /**
  Einfache Benachrichtigung an den Benutzer (nur Button OK).
  @param title Titel der Nachricht
  @param type Art der Nachricht: Error, Info, Warn, Ask
  @param message Text der Nachricht.
  */
  public void showMessage(String title, String type, String message);
  /**
  Wahl einer Option durch den Benutzer mit Ausl�sen einer entsprechenden Nachricht.
  <P>
  Es wird eine MessageBox angezeigt, die die mit "buttons" definierten Optionen zur
  Auswahl anbietet.
  <BR>
  In "actionCommands" ist eine gleich gro�e Anzahl von eindeutigen Nachrichten zu hinterlegen,
  die den buttons entsprechen.
  <BR>
  Es wird ein Event "doAction" generiert. Das ActionCommand dieser Nachricht entspricht
  der vom Benutzer gew�hlten Aktion.
  <BR>
  Wurde keine windowId angegeben (null), wird bei "doAction" als Fenstername "MessageBox" und
  als FensterId "-1" geliefert.
  <BR>
  Wenn der Benutzer die ControlBox angeklickt, wird bei "doAction"
  als ActionCommand "CANCEL" geliefert.
  @param windowId des Parent Windows oder null, wenn kein Parent.
  @param msgName Name der Messagebox
  @param title Titel der Nachricht
  @param type Art der Nachricht: Error, Info, Warn, Ask
  @param message Text der Nachricht.
  @param buttons Menge der Wahlm�glichkeiten durch den Benutzer
  @param actionCommands ActionCommands für die Buttons
  @return actionCommand des vom Benutzer gedr�ckten Button oder CANCEL,
  wenn ControlBox angeklickt wurde.
  */
  public String showMessage(String windowId, String msgName, String title,
      String type, String message, String[] buttons, String[] actionCommands);
  /**
   * @see de.guibuilder.framework.GuiUtil#fileOpenDialog
   */
  public String[] fileOpenDialog(String parentWindowId, String dialogTitle,
      String directoryName, String fileName);
  /**
   * @see de.guibuilder.framework.GuiUtil#fileSaveDialog
   */
  public String[] fileSaveDialog(String parentWindowId, String dialogTitle,
      String directoryName, String fileName);
  /**
  Liefert ein Window (Form oder Dialog).
  <BR>
  Auf diese Art k�nnen die Methoden des Fensters aufgerufen werden.
  */
  //##public GuiWindow getWindow(String windowId);
  /**
  Liefert eine Tabelle die auf einer Registerkarte liegt.
  */
  //public GuiTable getTable(String windowId, String tabName, String tableName);
  /**
  Liefert eine Tabelle, die direkt auf einem Fenster liegt.
  */
  //public GuiTable getTable(String windowId, String tableName);
  /**
  Liefert die Tree-Komponente eines Fensters, oder null, wenn keine
  vorhanden.
  <BR>
  Es wird davon ausgegangen, daß ein Fenster nur eine Tree-Komponente enthält!
  */
  //public GuiTree getTree(String windowId);
  /**
  F�gt dem Tree des Fensters einen Knoten hinzu. Der Knoten wird
  dem zuletzt aktivierten Knoten hinzugef�gt.
  */
  public void addTreeNode(String windowId, String title, String filename);
  /**
  F�gt dem Tree des Fensters einen Knoten mit einem speziellen Namen hinzu. Der Knoten wird
  dem zuletzt aktivierten Knoten hinzugef�gt.
  */
  public void addTreeNode(String windowId, String title, String name, String filename);
  /**
  Setzt den selektierten Konten im Baum.
  @param path Pfad zum Knoten in Punkt-Notation: "root.myFolder.myNode".
  */
  public void setSelectedNode(String windowId, String path);
  /**
  L�scht den selektierten Knoten des Baums; es wird die Knoten selekiert, der auf den
  gel�schten folgt.
  */
  public void removeTreeNode(String windowId);
  /**
  Liefert eine Objektreferenz auf die Komponente mit dem angegebenen Namen.
  */
  //##public GuiMember getMember(String windowId, String name);
  /**
  Liefert eine Objektreferenz auf die Komponente mit dem angegebenen Namen.
  */
  //public GuiMember getMember(String windowId, String tabName, String name);
  /**
  Liefert eine Objektreferenz auf die Komponente mit dem angegebenen Namen.
  */
  //public GuiComponent getComponent(String windowId, String name);
  /**
  Liefert eine Objektreferenz auf die Komponente mit dem angegebenen Namen.
  */
  //public GuiComponent getComponent(String windowId, String tabName, String name);
  /**
  Liefert eine Objektreferenz auf einen Conatainer mit dem angegebenen Namen.
  */
  //public GuiContainer getContainer(String windowId, String name);
  public void setPingInterval(String interval);
  /**
   * Setzt das ResourceBundle mit dem angegebenen Namen.
   */
  public void setDefaultResourceBundle(String name);

}