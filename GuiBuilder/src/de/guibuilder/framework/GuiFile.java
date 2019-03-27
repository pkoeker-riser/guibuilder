/*
 * Created on 22.05.2004
 *
 * This file has been created as part of the project: JXMLGuiBuilder.
 * 
 * KKN 08.06.2004
 * überall dort, wo rscsLoader verwendet wurde habe ich   
 * GuiFactory.getInstance().getResourceLoader() eingebaut, weil
 * damit z.B. der Vergleich auf null funktioniert!
 */
package de.guibuilder.framework;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.zip.GZIPInputStream;

import electric.xml.Document;
import electric.xml.ParseException;

/**
 * GuiFile Objekte dienen als Schnittstelle zwischen interner und externer
 * Repr&auml;sentation einer GDL-Beschreibung. Die assoziierte Datei kann mit
 * verschiedenen Methoden gelesen werden. <br>
 * <i>GuiFile objects works as interface between internal and external
 * representation of a GDL description. The associated file can be read using
 * several methods </i> <br>
 * Z. Zt. werden folgende Methoden unterst&uuml;tzt: <br>
 * <i>The following methods are actually supported: </i>
 * <ul>
 * <li><b>RSC </b>, ein spezieller ResourceLoader wurde in der GuiFactory
 * angegeben, der das Lesen aus der Datei erledigt. Wurde ein ResourceLoader
 * gesetzt, so wird keine andere Methode zum Lesen der Datei versucht. <br>
 * <i>A special resource loader has been set in the GuiFactory If a resource
 * loader has been set, no other method will be tried to read the file. </i>
 * </li>
 * <li><b>HTTP </b>, der Dateiname beginnt mit http: und es wurde kein
 * ResourceLoader in der GuiFactory gesetzt. <br>
 * <i>The filename starts with http: and no ResourceLoader has been set in the
 * GuiFactory. </i></td>
 * </li>
 * <li><b>JAR </b>, der Dateiname beginnt mit jar: und es wurde kein
 * ResourceLoader in der GuiFactory gesetzt. <br>
 * <i>The filename starts with jar: and no ResourceLoader has been set in the
 * GuiFactory. </i></td>
 * </li>
 * <li><b>FILE </b>, jeder andere Fall, es sei denn, ein ResourceLoader wurde
 * in der GuiFactory gesetzt. <br>
 * <i>Every other case except that a resource loader has been set in the
 * GuiFactory. </i></li>
 * </ul>
 * 
 * @author bubi
 *  
 */
public class GuiFile {
   private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger
         .getLogger(GuiFile.class);

   /**
    * Standard-Buchstabenkodierung f&uuml;r GuiFile Objekte. Die
    * Standard-Buchstabenkodierung wird immer dann angenommen, wenn keine
    * Buchstabenkodierung f&uuml;r ein GuiFile Objekt angegeben wurde, oder
    * keine Kodierung direkt aus der Datei gelesen werden kann (XML). Die
    * Standard-Standard-Buchstabenkodierung ist "ISO-8859-1". <br>
    * <i>Default character encoding (charset) for GuiFile objects. The default
    * character encoding is taken if no charset is given at the creation of the
    * GuiFile object and no encoding information can be read out of the file it
    * self (as it is in XML). The default-default character encoding (charset)
    * is "ISO-8859-1". </i>
    */
   private static String defaultEncoding = "ISO-8859-1";

   /**
    * Buchstabenkodierung des GuiFile Objektes. <br>
    * <i>Character encoding (charset) of the GuiFile object. </i>
    * 
    * @see #defaultEncoding
    */
   private String encoding;

   /**
    * Name der Datei, so wie er vom Benutzer angegeben wurde. <br>
    * <i>Name of the file like requested. </i>
    */
   private String filename;

   /**
    * Inhalt der Datei als electric.xml.Document (falls m&ouml;glich). <br>
    * <i>Content of the file as an electric.xml.Document (if XML) </i>
    * 
    * @see electric.xml.Document
    */
   private Document document;

   /**
    * Inhalt der Datei als Byte. <br>
    * <i>Content of the file as byte </i>
    */
   private byte[] buffer;

   /**
    * Basisverzeichnis f&uuml;r Dateien. <br>
    * <i>Document base out of GuiUtil </i>
    * 
    * @see GuiUtil#documentBase
    */
   private URL documentBase;

   /**
    * URL der Datei. <br>
    * <i>URL of the file. </i>
    */
   private URL url = null;

   /**
    * Connection zur Datei. <br>
    * <i>Connection to the file. </i>
    */
   private URLConnection connection = null;

   /**
    * InputStream zur Datei. <br>
    * <i>InputStream to the file. </i>
    */
   private InputStream inputStream = null;

   /**
    * Erzeugt ein neues GuiFile Objekt aus der Datei, die durch <b>filename </b>
    * bezeichnet ist. <br>
    * <i>Creates a new GuiFile Object from the Resource specified by <b>filename
    * </b>. </i>
    * 
    * @param myFilename
    *           Der Name der Datei. <i>the name of the file. </i>
    * @throws IOException
    *            Wenn URL unkorrekt (MalformedUrlException), wenn Datei nicht
    *            gefunden (FileNotFoundException) oder wenn sonstige E/A Fehler
    *            auftreten (IOException). <i>If URL malformed
    *            (MalformedUrlException), if file not found
    *            (FileNotFoundException) or other I/O errors occur
    *            (IOException). </i>
    */
   public GuiFile(String myFilename) throws IOException {
      this.encoding = defaultEncoding;
      open(myFilename);
   }

   /**
    * Erzeugt ein neues GuiFile Objekt aus der Datei, die durch <b>filename </b>
    * bezeichnet ist. Es wird die Buchstabenkodierung <b>encoding </b>
    * angenommen. Encoding muss eine unterst&uuml;tzte Buchstabenkodierung sein.
    * <br>
    * <i>Creates a new GuiFile Object from the Resource specified by <b>filename
    * </b>. </i>
    * 
    * @param myFilename
    *           Der Name der Datei. <i>the name of the file. </i>
    * @param encoding
    *           Die Buchstabenkodierung der Datei. <i>The charset encoding </i>
    * @throws IOException
    *            Wenn URL unkorrekt (MalformedUrlException), wenn Datei nicht
    *            gefunden (FileNotFoundException) oder wenn sonstige E/A Fehler
    *            auftreten (IOException). <i>If URL malformed
    *            (MalformedUrlException), if file not found
    *            (FileNotFoundException) or other I/O errors occur
    *            (IOException). </i>
    * @throws IllegalCharsetNameException
    *            Wenn <b>encoding </b> keine unterst&uuml;tzte
    *            Buchstabenkodierung ist. <br>
    *            <i>If <b>encoding </b> is a not supported charset </i>
    *  
    */
   public GuiFile(String myFilename, String encoding)
         throws IllegalCharsetNameException, IOException {
      this.encoding = encoding;
      open(myFilename);
   }

   /**
    * Antwortet mit der aktuellen Buchstabenkodierung des GuiFile Objektes. <br>
    * <i>returns the actual character encoding for this GuiFile object. </i>
    * 
    * @return Die Buchstabenkodierung. <i>Returns the encoding. </i>
    */
   public String getEncoding() {
      return encoding;
   }

   /**
    * Setzt die Buchstabenkodierung f&uml;r ein konkretes GuiFile Objekt. Dies
    * kann notwendig werden, wenn die Buchstabenkodierung nicht aus der Datei
    * selbst (XML) gelesen werden kann. <br>
    * <i>sets the character encoding (the charset) for the concret GuiFile
    * object manually. This may be necessary, if the encoding is not given
    * within the file it self (as it is in XML). </i>
    * 
    * @param encoding
    *           Name der Buchstabenkodierung. <i>The encoding to set. </i>
    * @throws IllegalCharsetNameException
    * @see java.nio.charset.Charset
    * @see java.nio.charset.IllegalCharsetNameException
    */
   public void setEncoding(String encoding) throws IllegalCharsetNameException {
      if (!Charset.isSupported(encoding))
         throw new IllegalCharsetNameException(encoding);
      this.encoding = encoding;
   }

   /**
    * Setzt das Basisverzeichnis f&uml;r zu lesende Dateien. Ist ein Dateiname
    * relativ, so wird der Dateiname aus dem Basisverzeichnis und dem Dateinamen
    * gebildet. <i>Sets the base directory for files. If a requested filename is
    * relative, die filename is set to the base directory concatenated with the
    * filename. </i>
    * 
    * @param myUrl
    *           Basisverzeichnis f&uuml;r zu lesende Dateien. <i>Base part of
    *           relative filenames. <i>
    */
   private void setDocumentBase(URL myUrl) {
      this.documentBase = myUrl;
   }

   /**
    * Antwortet mit der aktuellen Buchstabenkodierung. <br>
    * <i>returns the actual character encoding for this GuiFile object. </i>
    * 
    * @return Die Buchstabenkodierung. <br>
    *         <i>Returns the encoding. </i>
    */
   public static String getDefaultEncoding() {
      return defaultEncoding;
   }

   /**
    * Setzt die Standard-Buchstabenkodierung f&uml;r GuiFile Objekte. <br>
    * <i>sets the default character encoding (the charset) for the GuiFile
    * objects. </i>
    * 
    * @param myEncoding
    *           Name der Buchstabenkodierung. <br>
    *           <i>encoding The encoding to set. </i>
    * @throws IllegalCharsetNameException
    * @see java.nio.charset.Charset
    * @see java.nio.charset.IllegalCharsetNameException
    */
   public static void setDefaultEncoding(String myEncoding)
         throws IllegalCharsetNameException {
      if (!Charset.isSupported(myEncoding)) {
         throw new IllegalCharsetNameException(myEncoding);
      }
      GuiFile.defaultEncoding = myEncoding;
   }

   /**
    * &Ouml;ffnet die Datei. <br>
    * <i>Opens the file </i>
    * 
    * @param filename
    *           Name der Datei. <i>Name of the file </i>
    * @throws IOException
    */
   private void open(String _filename) throws IOException {
      this.setDocumentBase(GuiUtil.getDocumentBase());
      if (System.getProperty("file.separator").equals("\\")) // DOSe
      {
         _filename = GuiUtil.replaceFileSeparator(_filename);
      }
      this.filename = _filename;
      this.url = getURL();
      this.connection = url.openConnection();
      if (GuiFactory.getResourceLoader() != null)
         this.inputStream = GuiFactory.getResourceLoader()
               .getResourceAsStream(filename);
      else
         this.inputStream = getInputStream(connection);
   }

   /**
    * Schliesst die Datei. <br>
    * <i>Closes the file </i>
    * 
    * @throws IOException
    */
   public void close() throws IOException {
      if (inputStream != null) {
         inputStream.close();
      }
   }

   /**
    * Erzeugt eine URL aus einem Dateinamen und einer documentBase.
    * 
    * @return Die URL, die aus dem Dateinamen und der documentBase
    *         zusammengebaut wurde.
    * @throws MalformedURLException
    * @see java.net.URL#URL(URL, String)
    */
   private URL getURL() throws MalformedURLException {
      URL myUrl = null;
      if (documentBase != null) {
         myUrl = new URL(documentBase, filename);
      } else {
         myUrl = new URL(filename);
      }
      logger.info("opening URL: " + myUrl);
      System.out.println(myUrl);
      return myUrl;
   }

   /**
    * Erstellt eine Verbindung zu der Datei. Konnte die Verbindung nicht
    * hergestellt werden weil die Datei an dem angegebenen Ort nicht existiert,
    * wird versucht eine Verbindung zu der Datei unter der Annahme herzustellen,
    * dass es sich um eine Resource des Programms handelt (Bspw. aus der
    * JAR-Datei, aus der das Programm gestartet wurde). <br>
    * <i>Creates a connection to the file resource. If it is not possible to
    * create a connection because the file does not exist at the specified
    * location, it is presumed that the file may be a resource of the programm.
    * and a connection is made using the ClassLoader. </i>
    * 
    * @return InputStream Verbindung zu der Datei. <i>A connection to the file
    *         resource </i>.
    * @throws IOException
    *            Wenn die Datei an dem angegebenen Ort nicht existiert und auch
    *            nicht als Programmresource geladen werden kann. <i>If the file
    *            does not exist at the specified location and even cannot be
    *            loaded as a program resource using the ClassLoader. </i>
    */
   private InputStream getInputStream(URLConnection con) throws IOException {
      InputStream is = null;
      if (this.inputStream == null) {
         try {
            logger.debug("Connecting to... ");
            con.setDoInput(true);
            con.setRequestProperty("Accept-Encoding", "gzip");
            //Map p = con.getRequestProperties();
            con.connect();
            is = con.getInputStream();

            String myEncoding = con.getContentEncoding();
            if (myEncoding != null && myEncoding.equals("gzip")) { // macht das die
                                                               // URLConnection
                                                               // nicht
                                                               // automatisch?
               is = new GZIPInputStream(con.getInputStream());
               logger.debug("gzipped...");
            }
         } catch (FileNotFoundException ffe) { // ClassLoader
            ClassLoader cl = GuiFactory.class.getClassLoader();
            is = cl.getResourceAsStream(url.getFile());
            logger.debug("resourced (" + url.getFile() + ")");
         }
      }
      //logger.debug("InputStream: "+is);
      return is;
   }

   /**
    * Liesst den Inhalt aus der Datei. <br>
    * <i>Reads the content of the file. </i>
    * 
    * @return byte[] Inhalt der Datei. <i>Content of the file. </i>
    */
   private byte[] read() throws IOException {
      byte[] myBuffer = null;
      int contentLength = connection.getContentLength();
      if (contentLength != -1) {
         myBuffer = new byte[contentLength];
         inputStream.read(myBuffer);
      } else { // ContentLength not known
         StringBuffer sb = new StringBuffer(1024);
         int i;
         InputStream in = connection.getInputStream();
         byte inBytes[] = new byte[1024];
         while ((i = in.read(inBytes)) != -1) {
            sb = sb.append(new String(inBytes, 0, i));
         }
         myBuffer = sb.toString().getBytes();
      }
      return myBuffer;
   }

   /**
    * Antwortet mit der String Repr&auml;sentation des GuiFile Objektes. <br>
    * <i>returns the String representation of the GuiFile object. </i>
    * 
    * @return String-Repr&auml;sentation des GuiFile Objektes. <br>
    *         <i>String, which represents the content of the file. </i>
    */
   public String toString() {
      String s = null;
      if (buffer == null) { // buffer nur einmal einlesen
         if (GuiFactory.getResourceLoader() == null) {
            try {
               buffer = read();
               toDocument(); // hole ggf. die encoding information.
            } catch (ParseException e) { // keine XML Datei - macht nichts.
            } catch (IOException e) { // schon schlimmer...
               logger.error(e.getMessage(), e);
               //GuiUtil.showEx(e);
            }
         } else { // vom ResourceLoader
            try {
               document = new Document(GuiFactory.getResourceLoader().getResourceAsStream(filename));
               buffer = document.getBytes();
               encoding = document.getEncoding();
            } catch (ParseException ex) {
               GuiUtil.showEx(ex);
            } catch (UnsupportedEncodingException ex) {
               GuiUtil.showEx(ex);
            }
         }
      }
      try {
         s = new String(buffer, encoding);
         s = s.replaceAll("\r", ""); // DOSe
      } catch (UnsupportedEncodingException e1) {
         logger.error(e1);
      }
      return s;
   }

   /**
    * Antwortet mit der <b>electric.xml.Dokument </b> Repr&auml;sentation des
    * GuiFile Objektes. <br>
    * <i>returns the electric.xml.Document </b> representation for the GuiFile
    * object. </i>
    * 
    * @return Dokument, welches das GuiFile Objekt Repr&auml;sentiert. <br>
    *         <i>Document which represents the content of the file. </i>
    */
   public Document toDocument() throws ParseException, IOException {
      if (buffer == null) {
         buffer = read();
      }
      Document d = new Document(buffer);
      encoding = d.getEncoding();
      return d;
   }

   /**
    * Schliesst die Datei, falls vergessen. <br>
    * <i>Closes the file if forgotten. </i>
    */
   protected void finalize() {
      try {
      	super.finalize();
         inputStream.close();
      } catch (Throwable e) {
         logger.error("finalize", e);
      }
   }
}