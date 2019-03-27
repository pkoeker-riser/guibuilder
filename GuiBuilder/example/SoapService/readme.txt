WSDL-Service
============

Dieses Beispiel soll demonstrieren, wie eine Client-Application
mit einem WSDL-Service kommuniziert.

Vorgehen:
- "StartServer.bat" aufrufen...
- und anschlie�end "StartClient.bat"

Es erscheint ein kleines Fenster mit verschiedenen Buttons.
- Dr�cken Sie zuerst den Button "Bind";
dieses bewirkt, da� der Client an den vom Server publizierten
Dienst gebunden wird.
- Anschlie�end (auch wiederholt) den Button "DateTime" bet�tigen.
In dem Textfeld darunter wird das aktuelle Datum und die Uhrzeit ausgegeben.
Die Methode "getDateTime" wird auf dem Server ausgef�hrt.

Der Button "Restart" veranla�t einen ShutDown mit anschlie�endem Restart
des Servers.
Auf diese Art kann der Server vom Client aus neu gestartet werden
(dieses ist dann hilfreich, wenn der Server in einem Rechenzentrum
steht, zu dem man nicht problemlos Zugang hat, aber z.B. per FTP
eine neue Version des Servers eingespielt hat).
Achtung!
Nach einem "Restart" ist kein erneutes "Bind" erforderlich!

Der Button "Endlos" veranla�t den Server eine Endlosschleife aufzurufen.
(Man erkennt es daran, da� die CPU zu 100% ausgelastet ist)
Der Client ist jetzt blockiert, da er keinen Response vom Server mehr
erh�lt.
Um den Server neu zu starten, wird einfach ein weiterer Client
aufgerufen, "Bind" und anschlie�end "Restart" gedr�ckt.
Jetzt wird auch der blockierte Client wieder "befreit" 
und der Button "DateTime" leifert wieder Daten.

Wie funktioniert das?
=====================
Der Server besteht der Klasse "Main" und dem Interface "IService".
In der Klasse "Main" sind die Methoden aus "IService" implementiert
und dieses Objekt wird als WSDL-Service publiziert.

Der Client besteht aus der Oberfl�chen-Spezifikation "Client.xml",
welche den Controler "SoapControler" verwendet.
Die Methode "bind" fordert vom Server ein Remote Object an,
welches auf "IService" ge-cast-et wird.
Die Benutzeraktionen (das Dr�cken der Buttons) werden vom GuiBuilder
an diesen Controler weiter gereicht, der seinerseits die Servermethoden
aufruft.

Wenn der Server auf einem anderen Rechner installiert ist,
mu� naturgem�� beim SoapControler die Methode "bind"
entsprechend angepa�t werden:
Statt "localhost" die IP-Adresse oder der Name des Servers.

Hinweis:
========
Um den vom Server publizierten WSDL-File einzusehen im Browser diese URL eingeben:
http://localhost:8004/serv/urn:service.wsdl

Achtung!
Einen Browser verwenden, der XML-Dokumente darstellen kann (Mozilla, Firefox, IE).
