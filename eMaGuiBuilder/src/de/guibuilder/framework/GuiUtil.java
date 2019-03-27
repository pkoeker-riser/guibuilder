package de.guibuilder.framework;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import de.guibuilder.framework.GuiFactory.CurContext;
import de.jdataset.JDataSet;
import de.pkjs.util.Convert;

import electric.xml.Attribute;
import electric.xml.Attributes;
import electric.xml.CData;
import electric.xml.Child;
import electric.xml.Comment;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.Node;
import electric.xml.ParseException;
import electric.xml.XPath;

// TODO :Die Sache mit der Initialisierung von Codebase und Documentbase ist extrem furchtbar!
// Unbedingt ändern, aber so, daß es zuverlässig für Applet und Application funktioniert!
/**
 * Hilfsklasse für diverse Berechnungen und globale Zustände.
 * <P>
 * Für verschiedene Methoden muß die interne CodeBase zuvor gesetzt werden.
 * 
 * @see #setCodeBase
 * 
 */
public final class GuiUtil {
	
   // Attributes
   private static org.apache.log4j.Logger logger;

   /**
    * Datei und Pfad zum ErrorIcon, dass immer dann angezeigt wird,
    * wenn ein spezifizertes Image nicht gefunden wird.
    */
	private final static String ERROR_ICON_FILE = "broken.gif";
	private final static String ERROR_ICON_PATH = "sysimages";
   
   
   /**
    * Properties mit der Liste der gültigen Attribute je Schlüsselwort.
    */
   private static Properties attProps;

   /**
    * CodeBase der Anwendung. <BR>
    * Üblicherweise das Directory, in dem die Anwendung installiert ist.
    */
   private static URL codebase;

   /**
    * Documentbase für Spezifikationsfiles.
    */
   private static URL documentBase;
   private static String docbaseOrig;

   /**
    * Dieser Icon wird angezeigt, wenn kein Image gefunden wurde.
    */
   private static Icon errorIcon;

   /**
    * @see #setHttpUsage
    */
   public static final int NONE = 1;

   /**
    * @see #setHttpUsage
    */
   public static final int GET = 2;

   /**
    * @see #setHttpUsage
    */
   public static final int PUT = 3;

   /**
    * Dateiname der Properties für den GuiBuilder in der CodeBase:
    * "GuiBuilderConfig.xml".
    */
   public static final String GUIBUILDER_CONFIG = "GuiBuilderConfig.xml";

   private static JDataSet mdlConfig;

   /**
    * Liefert das Model, welches die Konfiguration des GuiBuilder hält. Siehe
    * GuiBuilderConfig.xml
    * 
    * @return JDataSet
    */
   public static JDataSet getConfig() {
      return mdlConfig;
   }

   /**
    * HTTP-Policy; default ist "GET"
    */
   private static int httpUsage = GET;

   /**
    * Liefert das Applet, wenn Anwendung als Applet läuft; sonst null.
    */
   private static GuiApplet applet;

   /**
    * Der zuletzt mit fileToString gelesene File
    */
   private static String currentFile;

   private static String tempDir;

   /**
    * Inhalt der gewählten Version des GuiBuilders. <BR>
    * Default ist "default".
    */
   private static String version = "default";

   /**
    * Debug-Status des GuiBuilders
    */
   private static boolean debug = false;

   /**
    * Wenn true, dann GuiBuilder als API verwendet; wenn false, dann für
    * Spezifikation.
    */
   private static boolean api = false;

   /**
    * Image Cache
    */
   private static Hashtable<URL, SoftReference<Icon>> imageCache = new Hashtable<URL, SoftReference<Icon>>();

   /**
    * Number of Image Cache Hits
    */
   private static int cacheHits;

   /**
    * Hintergrundfarbe für Attribut nn="true".
    */
   private static Color nnColor;

   /**
    * Zeichen für die Ergänzung des Labels von Feldern, für die das Attribut
    * nn="true" gesetzt wurde.
    */
   private static Character nnChar;

   private static boolean checkNN;

   // Resources
   private static ResourceBundle defaultBundle;

   private static String defaultBundleName;

   // Scripting
   private static boolean scripting;

   // für createDocument
   private static long lastFileNumber = 0;

   private static LinkedHashMap<String, Class<?>> parameterClasses;

   // Methods
   /**
    * Liefert eine HashSet mit erlaubten Namen von Attributen zu dem
    * übergebenen Schlüsselwort aus KeywordAttributes.properties. <br>
    * für PropertyEditor
    */
   public static HashSet<String> getKeywordAttributes(String keyword) throws Exception {
      HashSet<String> ret = null;
      if (attProps == null) {
         try {
            attProps = loadProperties("KeywordAttributes.properties");
         } catch (Exception ex) {
            throw ex;
         }
      }
      String s = attProps.getProperty(keyword);
      if (s != null) {
         StringTokenizer toks = new StringTokenizer(s);
         ret = new HashSet<String>();
         while (toks.hasMoreTokens()) {
            String tok = toks.nextToken();
            ret.add(tok);
         }
      }
      return ret;
   }

   /**
    * Lädt die Konfiguration des GuiBuilders neu.
    */
   public static void loadGuiPropXml() {
      Document doc = null;
      String strFile = null;
      try {
      strFile = GuiUtil.getLocalDir() + System.getProperty("file.separator") + GUIBUILDER_CONFIG;
      File file = new File(strFile);
      if (file.exists()) {
         doc = new Document(file);
         System.out.println(GUIBUILDER_CONFIG + " loaded from " + strFile + ".");
      } else {
         //File f = new File(GUIBUILDER_CONFIG);
         String sDoc = fileToString(GUIBUILDER_CONFIG);
         doc = new Document(sDoc);
         System.out.println(GUIBUILDER_CONFIG + " loaded from filesystem.");
      }
      } catch (Exception ex) {
         // Wenn File fehlt, dann aus JAR, wenn irgend möglich?
         ClassLoader cl = GuiFactory.class.getClassLoader();
         InputStream inp = cl.getResourceAsStream(GUIBUILDER_CONFIG);
         try {
            doc = new Document(inp);
            System.out.println(GUIBUILDER_CONFIG + " loaded from JAR.");
            inp.close();
         } catch (Exception pex) {
            System.err.println("Missing File: " + GUIBUILDER_CONFIG + " "
                  + pex.getMessage());
         }
      }
      try {
         mdlConfig = new JDataSet(doc);
         mdlConfig.commitChanges(); // vorsichtshalber
         setDocumentBase(mdlConfig.getValuePath("@DocumentBase"));
         setRepository(mdlConfig.getValuePath("@RepositoryFilename"));
         try {
             setMethodMap(mdlConfig.getValuePath("@MethodmapFilename"));
         } catch (Exception ex) {
            System.err.println("Missing @MethodmapFilename in "
                  + GUIBUILDER_CONFIG);
         }
         //setHttpUsage(mdlConfig.getValue("@UseProtocol"));
      String l_nnColor = mdlConfig.getValuePath("@BackgroundColor");
      String l_nnChar = mdlConfig.getValuePath("@NnChar");
      String l_nnCheck = mdlConfig.getValuePath("@CheckNotNull");
         setNotNull(l_nnColor,l_nnChar,l_nnCheck);
         try {
            String language = mdlConfig.getValuePath(".Locale@Language"); // default
            // "de"
            String country = mdlConfig.getValuePath(".Locale@Country"); // default
            // "DE"
            if (country.length() != 2)
               country = "";
            if (language.length() == 2) {
               Locale.setDefault(new Locale(language, country));
            }
         } catch (Exception ex) {
            System.err.println(ex.getMessage());
         }
         GuiDate.setDefaultFormat(mdlConfig
               .getValuePath(".Locale@DateFormat")); // default
         // "dd.MM.yyyy"
         GuiTime.setDefaultFormat(mdlConfig
               .getValuePath(".Locale@TimeFormat")); // default
         // "HH:mm"
         String adapter = mdlConfig.getValuePath("@ApplicationAdapter");
         if (adapter != null) {
            try {
               Class.forName(adapter);
            } catch (Throwable ex) {
               System.err.println(ex.getMessage());
            }
         }
      } catch (Exception ex) {
         System.err.println("Error reading " + GuiUtil.GUIBUILDER_CONFIG
               + ": " + ex.getMessage());
         ex.printStackTrace();
      }
   }

   static void setNotNull(String color, String check) {
      setNNColor(color);
      setCheckNN(check);
   }

   /**
    * Setzt die Farbe und das Zeichen zur Ergänzung des Labels bei
    * Pflichtfeldern (nn="true") mit der übergebenen Farbe und dem ersten
    * Zeichen des übergebenen String. Setzt das Kennzeichen, ob die überprüfung
    * automatisch erfolgen soll.
    * 
    * @param p_color
    *            Farbe für die Kennzeichnung von Pflichtfeldern
    * @param p_char
    *            erstes Zeichen des übergebenen String wird für die Ergänzung
    *            des Labels von Pflichtfeldern verwendet
    * @param p_check
    *            Kennzeichen, ob automatische überprüfung stattfinden soll
    * @autor thomas
    */
   static void setNotNull(String p_color, String p_char, String p_check) {
      setNNColor(p_color);
      setNNChar(p_char);
      setCheckNN(p_check);
   }

   static void setNNColor(String color) {
      if (color != null) {
         nnColor = Convert.toColor(color);
      }
   }


   /**
    * Setzt das Zeichen zur Ergänzung des Labels bei Pflichtfeldern (nn="true")
    * mit dem ersten Zeichen des übergebenen String.
    * 
    * @param p_char
    *            erstes Zeichen des übergebenen String wird für die Ergänzung
    *            des Labels von Pflichtfeldern verwendet
    * @autor thomas
    */
   static void setNNChar(String p_char) {
      if (p_char != null) {
         nnChar = new Character(p_char.charAt(0));
      } else {
         nnChar = null;
      }
   }

   /**
    * Setzt das Zeichen zur Ergänzung des Labels bei Pflichtfeldern (nn="true")
    * mit dem übergebenen Zeichen.
    * 
    * @param p_char
    *            Zeichen für die Ergänzung des Labels von Pflichtfeldern
    *            verwendet
    * @autor thomas
    */
   static void setNNChar(Character p_char) {
      if (p_char != null) {
         nnChar = p_char;
      } else
         nnChar = null;
   }
   
   /**
    * Setzt das Zeichen zur Ergänzung des Labels bei Pflichtfeldern (nn="true")
    * mit dem übergebenen Zeichen.
    * 
    * @param p_char
    *            Zeichen für die Ergänzung des Labels von Pflichtfeldern
    *            verwendet
    * @autor thomas
    */
   static void setNNChar(char p_char) {
      nnChar = new Character(p_char);
   }   

   /**
    * Setzt die Hintergrundfarbe für NotNull-Felder.
    */
   public static void setNNColor(Color c) {
      nnColor = c;
   }

   /**
    * Liefert die Hintergrundfarbe für NotNull-Felder. Default ist 243,243,192
    */
   public static Color getNNColor() {
      if (nnColor == null) {
         nnColor = new Color(243,243,192);
      }
     return nnColor;
   }

   /**
    * Liefert ein Zeichen zur Ergänzung des Labels von NotNull-Felder. Default
    * ist das leere Zeichen.
    * 
    */
   public static String getNNChar() {
      String retChar = null;
      if (nnChar != null){
         retChar = nnChar.toString();
      }
      return retChar;
   }

   /**
    * Legt fest, ob Felder mit dem Attribut notnull=true wirklich geprüft
    * werden sollen. <br>
    * Siehe GuiBuilderConfig.xml "checkNotNull"
    */
   static void setCheckNN(String val) {
      try {
         setCheckNN(Boolean.valueOf(val).booleanValue());
      } catch (Exception ex) {
         System.err.println("GuiUtil#setCheckNN; Illegal boolean value: "
               + val);
      }
   }
   /**
    * Legt fest, ob Felder mit dem Attribut notnull='true' wirklich geprüft
    * werden sollen. <br>
    * In der Entwicklungsumgebung üblicherweise auf 'false' setzen; zur
    * Laufzeitumgebung auf 'true'.
    * 
    * @param b
    */
   public static void setCheckNN(boolean b) {
      checkNN = b;
   }
   /**
    * @see #setCheckNN(boolean)
    * @return
    */
   public static boolean isCheckNN() {
      return checkNN;
   }

   private static Color disabledColor = new Color(230, 230, 230); // TODO:

   // Property

   public static Color getDisabledColor() {
      return disabledColor;
   }
   /**
    * Speichert die Properties des GuiBuilder in "GuiBuilderConfig.xml".
    */
   public static void saveGuiPropXml() {
      if (mdlConfig.hasChanges()) {
         Document doc = mdlConfig.getXml();
         try {
            doc.write(new File(GuiUtil.GUIBUILDER_CONFIG));
            mdlConfig.commitChanges();
         } catch (Exception ex) {
            GuiUtil.showEx(ex);
         }
      }
   }

   /**
    * Setzt die Codebase für alle Klassen. <BR>
    * Die DocumentBase wird gleichzeitig auf diesen Wert gesetzt, wenn sie null
    * ist.
    */
   public static void setCodeBase(URL url) {
      String s = url.toString();
      int p = s.indexOf("\\");
      if (p != -1) {
         s = replaceFileSeparator(s);
         try {
            url = new URL(s);
         } catch (Exception ex) {
            logger.error(ex);
         }
      }
      codebase = url;
      System.out.println("GuiUtil#setCodeBase " + codebase);
      if (documentBase == null) {
         try {
            documentBase = new URL(url.toString());
         } catch (Exception ex) {
            GuiUtil.showEx(ex);
         }
      }
   }

   /**
    * Setzt die DocumentBase absolut unter Angabe einer URL.
    */
   public static void setDocumentBase(URL url) {
      String s = url.toString();
      /*
       * Das Problem ist, daß eine URL mit "\" zwar von der Klasse URL
       * akzeptiert wird, am Ende aber nicht richtig funktioniert. Dashalb muß
       * sichergestelt werden, daß hier keine "\" übergeben werden (Windows
       * File-System).
       */
      int p = s.indexOf("\\");
      if (p != -1) {
         s = replaceFileSeparator(s);
         try {
            url = new URL(s);
         } catch (Exception ex) {
            logger.error(ex);
         }
      }
      documentBase = url;
   }

   /**
    * Liefert die gesetzte Codebase.
    * <P>
    * Wenn die Codebase nicht gesetzt ist, wird das current Directory geliefert
    * (aus der SystemProperty "user.dir").
    */
   public static URL getCodeBase() {
      if (codebase != null) {
         return codebase;
      }
      try {
          // Vorsicht! Wenn Applet und Policy nicht gesetzt, gibts
          // AccessControlEx!
          String dir = System.getProperty("user.dir");
          dir = replaceFileSeparator(dir); // "\" zu "/"
          if (dir.endsWith("/") == false) {
             dir = dir + "/";
          }
          codebase = new URL("file", null, 0, dir);
      } catch (AccessControlException aex) {
         System.err.println("GuiUtil#getCodeBase: (Error) "
               + aex.getMessage());
       } catch (Exception ex) {
          GuiUtil.showEx(ex);
       }
       if (codebase != null) {
          System.out.println("GuiUtil#getCodeBase: " + codebase);
       }
       return codebase;
   }

   /**
    * Setzt die DocumentBase relativ zur CodeBase. <BR>
    * Ist üblicherweise ein Unterverzeichnis der CodeBase; die CodeBase muß
    * zuvor gesetzt werden.
    * <p>
    */
   public static void setDocumentBase(String offset) {
      docbaseOrig = offset;
      if (offset != null && offset.length() > 0) {
         offset = replaceFileSeparator(offset);
         try {
            // jar ?
            if (getCodeBase() != null
                  && getCodeBase().toString().startsWith("jar:")) {
               if (offset.startsWith("/") || offset.startsWith("\\")) {
                  offset = offset.substring(2);
               }
               documentBase = new URL("jar:"
                     + GuiUtil.getCodeBase().toString() + offset + "!/");
            }
            // nix jar
            else {
               documentBase = new URL(getCodeBase(), offset);
            }
         } catch (Exception ex) {
            GuiUtil.showEx(ex);
         }
         System.out.println("New Documentbase: " + documentBase);
      }
   }

   /**
    * Setzt die DocumentBase auf den Wert der CodeBase.
    */
   public static void resetDocumentBase() {
      try {
         documentBase = new URL(getCodeBase().toString());
      } catch (Exception ex) {
         GuiUtil.showEx(ex);
      }
   }

   /**
    * Liefert die DocumentBase. <BR>
    * Wenn die DocumentBase nicht gesetzt ist, wird die CodeBase geliefert.
    */
   public static URL getDocumentBase() {
      if (documentBase == null) {
         documentBase = getCodeBase();
      }
      return documentBase;
   }
   /**
    * Liefert die Documentbase relativ zur Codebase
    * 
    * @return
    */
   public static String getRelativeDocumentBase() {
     return docbaseOrig;
   }

   /**
    * Macht aus \myDir\myFile --> /myDir/myFile und aus \my1stDir\my2ndDir -->
    * /my1stDir/my2ndDir/
    * 
    * @param val
    * @return
    */
   public static String replaceFileSeparator(String val) {

      int p = val.indexOf("\\");
      while (p != -1) {
         val = val.substring(0, p) + "/" + val.substring(p + 1);
         p = val.indexOf("\\");
      }
      /*
       * Wenn der Verzeichnisname nicht mit / endet werden Icons nicht richtig
       * geladen. Deshalb wird hier der Verzeichnisname geprüft und angepasst.
       * Wenn am Ende des Pfades eine Datei steht, muss kein / angehängt
       * werden. Es wird versucht, das anhand des . im String zu erkennen.
       * Funktioniert also nicht bei Verzeichnissen mit einem . im Namen
       */
      
      /*
       * TODO Es wäre besser, den . erst nach dem letzten / zu suchen, weil
       * jeder . vorher zu einem Verzeichnis gehören muss.
       */      
      if ((!(val.endsWith("/")) && (!(val.contains("."))))) {
         val = val + "/";
      }
      return val;
   }

   /**
    * Setzt den Dateinamen des Repository. "NONE" bedeutet, daß die eingebauten
    * Standardeigenschaften der Factory verwendet werden sollen.
    */
   public static void setRepository(String s) {
      GuiFactory.getInstance().fillDefKeys(s);
   }
   public static void setMethodMap(String filename) {
       String xs = fileToString(filename);
       xs = xs.replaceAll("\r", "");
       if (xs == null) {
         return;
       }
       StringTokenizer toksLine = new StringTokenizer(xs, "\n");
       while (toksLine.hasMoreTokens()) {
           String line = toksLine.nextToken();
         if (line.length() == 0 || line.startsWith("#"))
            continue;
           StringTokenizer toks = new StringTokenizer(line, ",");
           int cnt = 0;
           String className = null;
           String attribName = null;
           //String getter = null;
           String setter = null;
           String attribType = null;
           while (toks.hasMoreTokens()) {
               String tok = toks.nextToken();
               switch (cnt) {
               case 0:
                   className = "de.guibuilder.framework."+tok;
                   break;
               case 1:
                   attribName = tok;
                   break;
               case 2:
                   //getter = tok;
                   break;
               case 3:
                   setter = tok;
                   break;
               case 4:
                   attribType = tok;
                   break;
               }
               cnt++;
           }
           try {
              Class clazz = Class.forName(className);
              Class[] types = {String.class, Method.class};
              Method am = getMethod(clazz, "addSetter", types);
              Class[] type = {getParameterClass(attribType)};
              Method m = getMethod(clazz, setter, type);
              if (m != null) {
                  Object[] args = {attribName, m}; 
                  am.invoke(null, args);
              } else {
               System.err.println("Missing Method: " + className + "#"
                     + setter);
              }
           } catch (Throwable ex) {
               // TODO
               System.err.println("GuiUtil#setMethodMap: "+ex.getMessage());
           }
       }
       
   }
   /**
    * Liefert zu dem angegebenen ParameterTypNamen die entsprechende Klasse
    * z.B. "int" --> Integer
    * 
    * @param parameterTypeName
    * @return
    */
   public static Class<?> getParameterClass(String parameterTypeName) {
       // Parameter Classes
       if (parameterClasses == null) {
           parameterClasses = new LinkedHashMap<String, Class<?>>();
           parameterClasses.put("boolean", Boolean.TYPE);
           //parameterClasses.put("!boolean", Boolean.TYPE);
           parameterClasses.put("byte", Byte.TYPE);
           parameterClasses.put("short", Short.TYPE);
           parameterClasses.put("int", Integer.TYPE);
           parameterClasses.put("long", Long.TYPE);
           parameterClasses.put("float", Float.TYPE);
           parameterClasses.put("double", java.lang.Double.TYPE);
           parameterClasses.put("char", Character.TYPE);
           parameterClasses.put("String", String.class);
           parameterClasses.put("BigDecimal", BigDecimal.class);
           parameterClasses.put("Date", Date.class);
           parameterClasses.put("Time", Time.class);
           parameterClasses.put("Timestamp", Timestamp.class);
       }

       Class clazz = (Class)parameterClasses.get(parameterTypeName);
       return clazz;
   }
   /**
    * Versucht zu der angegebenen Klasse die angegebene Methode mit den
    * angegebenen Parameter-Typen zu finden. Sucht auch nach default, protected
    * und vererbte Methoden.
    * <p>
    * Wirft eine NoSuchMethod-Exception wenn Methode fehlt Achtung!<br>
    * Es werden zwar auch !public Methoden geliefert, aber erst zur Laufzeit
    * stellt sich heraus, ob man das Recht hat sie aufzurufen!
    * 
    * @param clazz
    * @param name
    * @param types
    * @return null, wenn keine Methode zu finden
    */
   public static Method getMethod(Class clazz, String name, Class[] types) 
       throws NoSuchMethodException {
       Method m = null;
       try {
           m = clazz.getMethod(name, types);
       } catch (NoSuchMethodException ex) {
           try {
               m = clazz.getDeclaredMethod(name, types);
           } catch (NoSuchMethodException mex) {
               // superclasses
               Class sup = clazz.getSuperclass();
               if (sup != null) {
                   m = getMethod(sup, name, types);
               } else {
               System.err.println("Missing Method: " + clazz.getName()
                     + "#" + name);
                   throw mex;
               }
           }
       }
       return m;
   }
   /**
    * Liefert "file" oder "http" je nach Codebase. <BR>
    * Wenn die CodeBase nicht gesetzt wurde, wird gleichfalls "file"
    * zurückgegeben.
    */
   public static String getProtocol() {
      URL cb = getCodeBase();
      if (cb == null) {
         return "file";
      }
      return cb.getProtocol();
   }

   /**
    * Liefert das Temp-Directory; bei Windows c:\temp, bei Unix /temp.
    * <p>
    * Im .java.policy-File sollte der Nutzer hier Schreibberechtigung haben.
    */
   public static String getTempDir() {
      if (tempDir != null) {
         return tempDir;
      }
      if (System.getProperty("os.name").startsWith("Windows")) {
          tempDir = "c:\\temp";
       } else { // Unix
          tempDir = "/temp";
       }
       return tempDir;
   }

   /**
    * Liefert das Directory der DocumentBase oder "c:\temp" bei HTTP-Protocol.
    */
   public static String getCurrentDir() {
      String currentDir = getTempDir();
      if (getProtocol().equals("file")) {
         if (getDocumentBase() == null) {
            return null;
         }
         currentDir = getDocumentBase().getFile();
         String sep = System.getProperty("file.separator");
         if (sep.equals("\\")) {
            currentDir = currentDir.replace('/', '\\');
         }
         if (currentDir.endsWith("\\") || currentDir.endsWith("/")) {
            currentDir = currentDir.substring(0, currentDir.length() - 1);
         }
         String os = System.getProperty("os.name");
         if (os.startsWith("Linux") == false
               && (currentDir.startsWith("\\") || currentDir
                     .startsWith("/"))) {
            currentDir = currentDir.substring(1);
         }
      }
      return currentDir;
   }

   /**
    * Liefert das Directory der CodeBase oder "c:\temp" bei HTTP-Protocol.
    */
   public static String getUserDir() {
      String currentDir = getTempDir();
      if (getProtocol().equals("file")) {
         if (getCodeBase() == null) {
            return null;
         }
         currentDir = getCodeBase().getFile();
         String sep = System.getProperty("file.separator");
         if (sep.equals("\\")) {
            currentDir = currentDir.replace('/', '\\');
         }
         if (currentDir.endsWith("\\")) {
            currentDir = currentDir.substring(0, currentDir.length() - 1);
         }
         if (currentDir.startsWith("\\")) {
            currentDir = currentDir.substring(1);
         }
      }
      return currentDir;
   }
   /**
    * System.getProperty("user.dir") + ".guibuilder"
    * 
    * @return
    */
   public static String getLocalDir() {
      String dir = null;
      try {
             dir = System.getProperty("user.home");
          File f = new File(dir, ".guibuilder");
          if (f.canRead() == false) {
             f.mkdir();
          }
          dir = f.getAbsolutePath();
       } catch (Exception ex) {
          logger.error(dir, ex);
       }
       return dir;
   }

   /**
    * Setzt die HTTP-Policy für HTTP-Protocol. <BR>
    * Erlaubt sind NONE, GET und PUT.
    */
   public static void setHttpUsage(String s) {
      if (s != null) {
         if (s.equals("NONE")) {
            httpUsage = NONE;
         } else if (s.equals("GET")) {
            httpUsage = GET;
         } else if (s.equals("PUT")) {
            httpUsage = PUT;
         } else {
            throw new IllegalArgumentException(
                  "HttpUsage must be NONE, GET or PUT!");
         }
      }
   }

   /**
    * Liefert die HTTP-Policy. Default ist "GET".
    */
   public static int getHttpUsage() {
      return httpUsage;
   }

   /**
    * Liefert das Applet des GuiBuilders oder null.
    */
   public static GuiApplet getApplet() {
      return applet;
   }

   /**
    * Legt fest, daß der GuiBuilder als Applet läuft.
    * <p>
    * Die Codebase wird aus dem Applet übernommen.
    * 
    * @see #setCodeBase
    */
   public static void setApplet(GuiApplet a) {
      applet = a;
      setCodeBase(applet.getApplet().getCodeBase());
   }

   /**
    * Liefert das Kennzeichen, ob die Anwendung als Applet oder Application
    * läuft.
    */
   public static boolean isApplet() {
      if (applet == null) {
         return false;
      }
      return true;
   }

   /**
    * Liefert die eingestellte Version des GuiBuilders; default ist "default". <BR>
    * Die Menge der verfügbaren Versionen für den GuiBuilder wird im File
    * "Version.lst" eingestellt.
    */
   public static String getVersion() {
      return version;
   }

   /**
    * Setzt die Version für die Factory. <BR>
    * Ist das Argument null; wird die Version auf "default" gesetzt.
    */
   public static void setVersion(String v) {
      if (v == null) {
         version = "default";
      } else {
         version = v;
      }
   }

   /**
    * Setzt den Debug-Modus für die Factory.
    */
   public static void setDebug(boolean b) {
      debug = b;
   }

   /**
    * Setzt den Debug-Modus für die Factory.
    */
   public static void setDebug(String s) {
      debug = Boolean.valueOf(s).booleanValue();
   }

   /**
    * Liefert den Debug-Modus. Default ist "false";
    */
   public static boolean getDebug() {
      return debug;
   }

   /**
    * Liefert einen Vector der installierten LookAndFeels.
    * <p>
    * Leider nur die fest installierten...
    */
   /*
    * public static Vector getInstalledLAF() { UIManager.LookAndFeelInfo[]
    * m_infos; Vector ret = new Vector(); m_infos =
    * UIManager.getInstalledLookAndFeels(); String[] lafNames = new
    * String[m_infos.length]; for(int i=0; i < m_infos.length; i++) {
    * ret.add(m_infos[i].getName()); } return ret; }
    */
   /**
    * Setzt den UI Manager. <BR>
    * Erlaubte Werte sind "windows", "metal", "motif", "gtk", "kunststoff". <BR>
    * Groß- und Kleinschreibung ist egal. <BR>
    * Es darf auch null übergeben werden, dann passiert aber auch nüscht.
    */
   public static void setUiManager(String ui) {
      if (ui == null)
         return;
      String val = ui.toLowerCase();

      try {
        if (val.equals("windows")) {
               try {
               UIManager.setLookAndFeel(UIManager
                     .getSystemLookAndFeelClassName());
               } catch ( Exception ex ) {
                   logger.error(ex.getMessage(), ex);
               UIManager.setLookAndFeel(UIManager
                     .getSystemLookAndFeelClassName());
               }       
       } else if (val.equals("system")) {
            UIManager.setLookAndFeel(UIManager
                  .getSystemLookAndFeelClassName());
       } else if (val.equals("metal")) {
            UIManager
                  .setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
       } else if (val.equals("cross_platform")) {
            UIManager.setLookAndFeel(UIManager
                  .getCrossPlatformLookAndFeelClassName());
       } else if (val.equals("motif")) {
          UIManager
                .setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
       } else if (val.equals("gtk")) {
          UIManager
                .setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
       } else if (val.equals("3d")) {
          UIManager
                .setLookAndFeel("swing.addon.plaf.threeD.ThreeDLookAndFeel");
       } else if (val.equals("oyoaha")) {
          UIManager
                .setLookAndFeel("com.oyoaha.swing.plaf.oyoaha.OyoahaLookAndFeel");
       } else if (val.equals("skin")) {
          UIManager
                .setLookAndFeel("com.l2fprod.gui.plaf.skin.SkinLookAndFeel");
       } else if (val.equals("kunststoff")) {
            UIManager
                  .setLookAndFeel("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
       } else if (val.equals("plastic")) {
            UIManager
                  .setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
       } else if (val.equals("winlaf")) {
            UIManager
                  .setLookAndFeel("net.java.plaf.windows.WindowsLookAndFeel");
       } else if (val.equalsIgnoreCase("Nimbus")) {
            UIManager
                  .setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
       } else {
          throw new IllegalArgumentException("Illegal UI Manager: " + val);
       }
      } catch (Exception ex) {
         logger.error("Error: In GuiUtil#setUiManager failed with exception: ", ex);
         System.out.println("Error: In GuiUtil#setUiManager failed with exception: " + ex.getMessage());
      }
  }
  public static Document getDocument(String fileName) {
     // 1. Resource Loader
    if (GuiFactory.getResourceLoader() != null) {
      // 1.1 Wurzelverzeichnis
      String s1 = "/" + fileName;
      try {
         Document doc = new Document(GuiFactory.getResourceLoader()
                  .getResourceAsStream(s1));
        return doc;
      } catch (Exception ex) {
        // System.err.println("1.1 " + ex.getMessage());
        if (getDocumentBase() != null) {
          try {
            // 1.2 DocumentBase
            String s2 = "/" + getRelativeDocumentBase() + fileName;
                  Document doc = new Document(GuiFactory
                        .getResourceLoader().getResourceAsStream(s2));
            return doc;
          } catch (Exception ex2) {
            // System.err.println("1.2 " + ex.getMessage());
          }
        }
      }
    }
     // 2. HTTP / Connection to Web Server
     if (getDocumentBase() != null
           && getDocumentBase().getProtocol().startsWith("http")
           && fileName.indexOf(":") != 1) {
        // 2.1 InputStream
        InputStream in = null;
        try {
           // 2.2 URL-Connection
           URL url = new URL(GuiUtil.getDocumentBase(), fileName);
            HttpURLConnection con = (HttpURLConnection) url
                  .openConnection();
           con.setAllowUserInteraction(true);
           con.setDoInput(true);
           con.setUseCaches(false);
           con.setRequestMethod("GET");
           con.setRequestProperty("Accept-Encoding", "gzip");
           con.connect();
           System.out.println(con.getResponseCode() + " "
                 + con.getResponseMessage() + " " + fileName);
           if (con.getResponseCode() == 404) {
              return null;
           }
           // 2.3 GZIP?
           String gzip = con.getContentEncoding();
           
           if (gzip != null && gzip.equals("gzip")) {
              in = new BufferedInputStream(new GZIPInputStream(con
                    .getInputStream()));
           } else {
              in = new BufferedInputStream(con.getInputStream());
           }
           Document doc = new Document(in);

           in.close();
           currentFile = fileName;
           return doc;
        } catch (Exception ioe) {
           GuiUtil.showEx(ioe);
           return null;
        } finally {
           if (in != null) {
              try {
                 in.close();
              } catch (Exception ex) {
              }
           }
        }
     }
     // 3. nix Web Server: jar
     else if (getDocumentBase() != null
           && getDocumentBase().toString().startsWith("jar:")) {
        // 3.1 Inputstream
        BufferedInputStream in = null;
        try {
           // 3.2 URL-Connection
           URL url = new URL(getDocumentBase(), fileName);
           JarURLConnection con = (JarURLConnection) url.openConnection();
           con.setAllowUserInteraction(true);
           con.setDoInput(true);
           con.connect();
           in = new BufferedInputStream(con.getInputStream());
           // 3.3 read
           Document doc = new Document(in);
           currentFile = fileName;
           return doc;
        } catch (Exception ioe) {
           GuiUtil.showEx(ioe);
           return null;
        } finally {
           if (in != null) {
              try {
                 in.close();
              } catch (Exception ex) {
              }
           }
        }
     }
     // 4. Weder http noch jar: file
     else {
        File f = null;
        // 4.1 InputStream
        InputStream fin = null;
        // 4.2 Absoluter Pfad?
         if (fileName.indexOf(":") == 1 || fileName.indexOf("file:") != -1 // Laufwerksbuchstabe
               // ?
               // neu für Linux!?
               || fileName
                     .startsWith(System.getProperty("file.separator"))) {
           if (fileName.startsWith("file:")) {
              fileName = fileName.substring(5);
           }
           f = new File(fileName);
           try {
              fin = new FileInputStream(f);
           } catch (FileNotFoundException ffe) {
              // ClassLoader
              try {
                 return docFromJar(fileName);
              } catch (Exception ioex) {
                 GuiUtil.showEx(ioex);
                 return null;
              }
           } catch (Exception exc) {
              GuiUtil.showEx(exc);
              return null;
           }
        } else { // 4.3 relative Pfadangabe
           f = new File(GuiUtil.getCurrentDir(), fileName);
           try {
              fin = new FileInputStream(f);
           } catch (FileNotFoundException ffe) {
              // ClassLoader
              try {
                 return docFromJar(fileName);
              } catch (Exception ioex) {
                 GuiUtil.showEx(ioex);
                 return null;
              }
           } catch (AccessControlException ace) {
              // ClassLoader
              try {
                 return docFromJar(fileName);
              } catch (Exception ioex) {
                 GuiUtil.showEx(ioex);
                 return null;
              }
           } catch (Exception exc) {
              GuiUtil.showEx(exc);
              return null;
           }
        }
        try {
           currentFile = f.getName();
           Document doc = new Document(fin);
          return doc;    
        } catch (Exception ex) {
           GuiUtil.showEx(ex);
           return null;
        } finally {
           if (fin != null) {
              try {
                 fin.close();
               } catch (Exception ex) {
               }
           }
        }       
     }
   }
   
   // Aus guibuilder.jar
   private static Document docFromJar(String fileName) throws Exception {
      ClassLoader cl = GuiFactory.class.getClassLoader();
      InputStream inp = cl.getResourceAsStream(fileName);
      if (inp != null) {
         Document doc = new Document(inp);
         return doc;
      }
      return null;
   }

   /**
    * Konvertiert den Inhalt eines Files unter Angabe seines Namens zu einem
    * String. Linefeeds bleiben erhalten. <BR>
    * Das Dokument wird per HTTP geladen wenn die Codebase auf "http"
    * eingestellt ist.
    * 
    * @see #getProtocol
    */
   public static String fileToString(String fileName) {
      StringBuilder sb = new StringBuilder();
      String s = null;
      // 1. Resource Loader
      if (GuiFactory.getResourceLoader() != null) {
         if (fileName.toLowerCase().endsWith(".xml")) {
            try {
               Document guiDoc = new Document(GuiFactory
                     .getResourceLoader().getResourceAsStream(
                           "/" + fileName));
              s = guiDoc.toString();
              return s;
            } catch (Exception ex) {
               // logger.error("XML-Document Resource-Loader: "+ fileName,
               // ex);
            }
         } else {
				try {
					InputStream in = GuiFactory.getResourceLoader().getResourceAsStream("/" + fileName);
					int i;
					if (in != null) {
						byte inBytes[] = new byte[1024];
						// 2.4 lesen
						while ((i = in.read(inBytes)) != -1) {
							sb = sb.append(new String(inBytes, 0, i));
						}
						in.close();
						s = sb.toString();
						return s;
					}
				} catch (Exception ex) {
					logger.error("File Resource-Loader: " + fileName, ex);
				}
			}
      }
      // 2. HTTP / Connection to Web Server
      if (getDocumentBase() != null
            && getDocumentBase().getProtocol().startsWith("http")
            && fileName.indexOf(":") != 1) {
         // 2.1 InputStream
         BufferedInputStream in = null;
         try {
            // 2.2 URL-Connection
            URL url = new URL(GuiUtil.getDocumentBase(), fileName);
            HttpURLConnection con = (HttpURLConnection) url
                  .openConnection();
            con.setAllowUserInteraction(true);
            con.setDoInput(true);
            con.setUseCaches(false); // neu 6.9.2007
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept-Encoding", "gzip");
            con.connect();
            System.out.println(con.getResponseCode() + " "
                  + con.getResponseMessage() + " " + fileName);
            if (con.getResponseCode() == 404) {
               return null;
            }
            // 2.3 GZIP?
            String gzip = con.getContentEncoding();
            if (gzip != null && gzip.equals("gzip")) {
               in = new BufferedInputStream(new GZIPInputStream(con
                     .getInputStream()));
            } else {
               in = new BufferedInputStream(con.getInputStream());
            }
            int i;
            byte inBytes[] = new byte[1024];
            // 2.4 lesen
            while ((i = in.read(inBytes)) != -1) {
               sb = sb.append(new String(inBytes, 0, i));
            }

            in.close();
            //con.disconnect(); // !! falsch !!
            s = sb.toString();
            //s = s.replace('\r', ' ');
            s = s.replaceAll("\r", ""); // jdk 1.4
            currentFile = fileName;
         } catch (Exception ioe) {
            GuiUtil.showEx(ioe);
            return null;
         } finally {
            if (in != null) {
               try {
                  in.close();
               } catch (Exception ex) {
               }
            }
         }
      }
      // 3. nix Web Server: jar
      else if (getDocumentBase() != null
            && getDocumentBase().toString().startsWith("jar:")) {
         // 3.1 Inputstream
         BufferedInputStream in = null;
         try {
            // 3.2 URL-Connection
            URL url = new URL(getDocumentBase(), fileName);
            JarURLConnection con = (JarURLConnection) url.openConnection();
            con.setAllowUserInteraction(true);
            con.setDoInput(true);
            con.connect();
            in = new BufferedInputStream(con.getInputStream());
            // 3.3 read
            int i;
            byte inBytes[] = new byte[1024];
            while ((i = in.read(inBytes)) != -1) {
               sb = sb.append(new String(inBytes, 0, i));
            }
            in.close();
            s = sb.toString();
            s = s.replaceAll("\r", ""); 
            currentFile = fileName;
         } catch (Exception ioe) {
            GuiUtil.showEx(ioe);
            return null;
         } finally {
            if (in != null) {
               try {
                  in.close();
               } catch (Exception ex) {
               }
            }
         }
      }
      // 4. Weder http noch jar: file
      else {
         File f = null;
         // 4.1 InputStream
         InputStream fin = null;
         int filesize = -1;
         // 4.2 Absoluter Pfad?
         if (fileName.indexOf(":") == 1 || fileName.indexOf("file:") != -1 // Laufwerksbuchstabe
               // ?
               // neu für Linux!?
               || fileName
                     .startsWith(System.getProperty("file.separator"))) {
            if (fileName.startsWith("file:")) {
               fileName = fileName.substring(5);
            }
            f = new File(fileName);
            try {
               fin = new FileInputStream(f);
               filesize = (int) f.length();
            } catch (FileNotFoundException ffe) {
               // ClassLoader
               try {
                  return fileFromJar(fileName);
               } catch (Exception ioex) {
                  GuiUtil.showEx(ioex);
                  return null;
               }
            } catch (Exception exc) {
               GuiUtil.showEx(exc);
               return null;
            }
         } else { // 4.3 relative Pfadangabe
            f = new File(GuiUtil.getCurrentDir(), fileName);
            try {
               fin = new FileInputStream(f);
               filesize = (int) f.length();
            } catch (FileNotFoundException ffe) {
               // ClassLoader
               try {
                  return fileFromJar(fileName);
               } catch (Exception ioex) {
                  GuiUtil.showEx(ioex);
                  return null;
               }
            } catch (AccessControlException ace) {
               // ClassLoader
               try {
                  return fileFromJar(fileName);
               } catch (Exception ioex) {
                  GuiUtil.showEx(ioex);
                  return null;
               }
            } catch (Exception exc) {
               GuiUtil.showEx(exc);
               return null;
            }
         }
         if (fileName.toLowerCase().endsWith(".xml")) {
            try {
               Document guiDoc = new Document(fin);
               s = guiDoc.toString();
               fin.close();
               return s;
            } catch (Exception ex) {
               logger.debug(ex.getMessage(), ex);
            }
         }
         try {
            byte[] data = new byte[filesize];
            int n = fin.read(data, 0, filesize);
            if (n != filesize) {
               String msg = "File: " + fileName + " Size:" + filesize
                     + " != read:" + n;
               logger.warn(msg);
            }
            fin.close();
            s = new String(data);
            // CR ersetzen
            s = s.replaceAll("\r", ""); 
            currentFile = f.getName();
         } catch (Exception ex) {
            GuiUtil.showEx(ex);
            return null;
         } finally {
            if (fin != null) {
               try {
                  fin.close();
               } catch (Exception ex) {
               }
            }
         }       
      }
      return s;
   } // End of method fileToString

   // Aus guibuilder.jar
   private static String fileFromJar(String fileName) throws Exception {
      StringBuffer sb = new StringBuffer(1000);
      ClassLoader cl = GuiFactory.class.getClassLoader();
      InputStream inp = cl.getResourceAsStream(fileName);
      if (inp != null) {
         byte[] buffer = new byte[4096];
         int bytes_read;
         while ((bytes_read = inp.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, bytes_read));
         }
         inp.close();
         System.out.println("Loaded from JAR: " + fileName);
         return sb.toString();
      }
      return null;
   }

   /**
    * Liefert ein XmlDocument unter Angabe eines Dateinamens; relativ zur
    * DokumentBase.
    * 
    * @see GuiFactory#perfBeginForm(CurrentKeyword, CurContext)
    */
   public static Document fileToXml(String filename) {
    Document doc = null;
    try {
      // Absolut
      File f = new File(filename);
      try {
        if (f.exists()) {
          doc = new Document(f);
          return doc;
        }
      } catch (Exception ex) { // access denied
        logger.debug(ex);
      }
      // relativ
      f = new File(GuiUtil.getCurrentDir(), filename);
      try {
        if (f.exists()) {
          doc = new Document(f);
          return doc;
        }
      } catch (Exception ex) { // access denied
        logger.debug(ex);
      }
      URL url = GuiUtil.class.getClassLoader().getResource(filename);
      if (url != null) {
        doc = new Document(url.openStream());
      } else {
        // letzte Rettung:
        String data = fileToString(filename);
        if (data != null) {
          doc = new Document(data);
        }
      }
    } catch (ParseException ex) {
      GuiUtil.showEx(ex);
    } catch (IOException ex) {
      GuiUtil.showEx(ex);
    }
    return doc;
  }

   /**
     * Erzeugt einen Vector von Vectoren aus einer Textdatei; Spaltentrenner ist
     * "|".
     * 
     * @see GuiFactory#perfBeginTable(CurrentKeyword, CurContext)
     */
   public static Vector<Vector<Object>> fileToTableData(String filename) {
      Vector<Vector<Object>> ret = new Vector<Vector<Object>>();
      String data = fileToString(filename);
      if (data == null)
         return null;
      StringTokenizer lines = new StringTokenizer(data, "\n");
      StringTokenizer cols = null;
      String line = null;
      while (lines.hasMoreTokens()) {
         line = lines.nextToken();
         String col = null;
         cols = new StringTokenizer(line, "|");
         Vector<Object> row = new Vector<Object>();
         while (cols.hasMoreTokens()) {
            col = cols.nextToken().trim();
            if (col.equals("false") || col.equals("true")) {
               row.add(Boolean.valueOf(col));
            } else {
               row.add(col);
            }
         }
         ret.add(row);
      }
      return ret;
   }

   /**
    * Liefert den XML-Kommentar, der vor diesem Element steht. Oder null, wenn
    * kein Kommentar.
    */
   public static String getGuiDocComment(Element ele) {
      Comment comment = null;
      Child c = ele.getPreviousSiblingChild();
      try {
         if (c.getNodeType() == Node.COMMENT_NODE) {
            comment = (Comment) c;
         }
      } catch (Exception ex) {
         // nix
      }
      if (comment != null) {
         return comment.getString();
      }
      return null;
   }

   /**
    * Erzeugt ein Office-Dokument aus einem Template und gibt den Dateiname
    * zurück. Das alles geschieht in dem Verzeichnis, was in
    * guibuilder.properties unter "office" definiert ist.
    * 
    * @param template
    *           existierende Dokument-Vorlage
    * @param name
    *           Namensprefix für die erzeuge Datei
    * @return der erzeugte Dateiname
    */
   public static String createDocument(String template, String name)
         throws Exception {
      String ext = null;
      int p = template.indexOf(".");
      if (p == -1) {
         throw new IllegalArgumentException("Missing File Extension: "
               + template);
      }
      ext = template.substring(p);
      String dir = null;
      dir = getConfig().getValuePath(".Document@Office");
      if (dir.endsWith("/") == false) {
         dir = dir + "/";
      }
      String ret = null;
      File f = null;
      do {
         lastFileNumber++;
         ret = dir + name + Long.toString(lastFileNumber) + ext;
         f = new File(ret);
      } while (f.exists() == true);
      copyFile(dir + template, ret);
      return ret;
   }

   /**
    * Wandelt die Eingaben im Property-Editor in ein Element für den GuiBuilder
    * um.
    */
   public static Element exportGuiElement(Document doc) {
      Element ret = null;
      //System.out.println(ret);
      final Element root = doc.getRoot();
      final Elements comps = root.getElements();
      while (comps.hasMoreElements()) {
         Element rele = comps.next();
         String att = rele.getAttributeValue("name");
         String rval = rele.getTextString();
         // Widget
         if (att.equals("class")) {
            ret = new Element(rval);
         } else {
            // Default-Werte nicht setzen
            if (att.equals("tabstop") && rval.equals("true")) {
               continue;
            } else if (att.equals("visible") && rval.equals("true")) {
               continue;
            } else if (att.equals("eol") && rval.equals("true")) {
               continue;
            } else if (att.equals("do") && rval.equals("false")) {
               continue;
            } else if (att.equals("nn") && rval.equals("false")) {
               continue;
            } else if (att.equals("search") && rval.equals("false")) {
               continue;
            } else if (att.equals("invert") && rval.equals("false")) {
               continue;
            } else if (att.equals("nodeTitle") && rval.equals("false")) {
               continue;
            } else if (att.equals("comment")) {
               Comment comment = new Comment(rval);
               ret.addChild(comment);
               continue;
            }
            ret.setAttribute(att, rval);
         }
      }
      return ret;
   }

   public static Document importGuiScript(Document doc) {
      Document ret = new Document();
      ret.setEncoding("UTF-8");
      Element retRoot = ret.setRoot("Root");
      Element root = doc.getRoot(); // GDL
      Elements eles = root.getElements();
      while (eles.hasMoreElements()) {
         Element ele = eles.next();
         Element tele = transformGuiScript(ele);
         retRoot.addElement(tele);
      }
      return ret;
   }

   private static Element transformGuiScript(Element ele) {
      String comp = ele.getName();
      Element ret = new Element("Node");
      ret.setAttribute("title", comp);
      ret.setAttribute("name", comp);
      ret.setAttribute("element", comp);
      Element panel = ret.addElement("Panel");
      panel.setAttribute("name", comp);
      Element classEle = panel.addElement("Component");
      classEle.setAttribute("name", "class");
      classEle.setText(comp);
      // Attributes
      Attributes map = ele.getAttributes(new XPath("@*"));
      while (map.hasMoreElements()) {
         Attribute att = map.next();
         Element aele = panel.addElement("Component");
         aele.setAttribute("name", att.getName());
         aele.setText(att.getValue());
      }
      String comment = GuiUtil.getGuiDocComment(ele);
      if (comment != null) {
         Element cele = panel.addElement("Component");
         cele.setAttribute("name", "comment");
         cele.addChild(new CData(comment));
      }
      Elements eles = ele.getElements();
      while (eles.hasMoreElements()) {
         Element nele = eles.next();
         Element tele = transformGuiScript(nele);
         ret.addElement(tele);
      }
      return ret;
   }

   /**
    * This example is from the book _Java in a Nutshell_ by David Flanagan.
    * Written by David Flanagan. Copyright (c) 1996 O'Reilly & Associates. You
    * may study, use, modify, and distribute this example for any purpose. This
    * example is provided WITHOUT WARRANTY either expressed or implied.
    */
   private static void copyFile(String source_name, String dest_name)
         throws Exception {
      File source_file = new File(source_name);
      File destination_file = new File(dest_name);
      FileReader source = null; // SAS: changed for proper text io
      FileWriter destination = null;
      char[] buffer;
      int bytes_read;

      try {
         // First make sure the specified source file
         // exists, is a file, and is readable.
         if (!source_file.exists() || !source_file.isFile())
            throw new IllegalStateException(
                  "FileCopy: no such source file: " + source_name);
         if (!source_file.canRead())
            throw new IllegalStateException("FileCopy: source file "
                  + "is unreadable: " + source_name);

         // If the destination exists, make sure it is a writeable file
         // and ask before overwriting it. If the destination doesn't
         // exist, make sure the directory exists and is writeable.
         if (destination_file.exists()) {
            if (destination_file.isFile()) {
               //String response;
               if (!destination_file.canWrite())
                  throw new IllegalStateException(
                        "FileCopy: destination "
                        + "file is unwriteable: " + dest_name);
            } else {
               throw new IllegalStateException("FileCopy: destination "
                     + "is not a file: " + dest_name);
            }
         } else {
            File parentdir = parent(destination_file);
            if (!parentdir.exists())
               throw new IllegalStateException("FileCopy: destination "
                     + "directory doesn't exist: " + dest_name);
            if (!parentdir.canWrite())
               throw new IllegalStateException("FileCopy: destination "
                     + "directory is unwriteable: " + dest_name);
         }

         // If we've gotten this far, then everything is okay; we can
         // copy the file.
         source = new FileReader(source_file);
         destination = new FileWriter(destination_file);
         buffer = new char[1024];
         while (true) {
            bytes_read = source.read(buffer, 0, 1024);
            if (bytes_read == -1)
               break;
            destination.write(buffer, 0, bytes_read);
         }
      }
      // No matter what happens, always close any streams we've opened.
      finally {
         if (source != null) {
            try {
               source.close();
            } catch (IOException e) {
            }
         }
         if (destination != null) {
            try {
               destination.close();
            } catch (IOException e) {
            }
         }
      }
   }

   // File.getParent() can return null when the file is specified without
   // a directory or is in the root directory.
   // This method handles those cases.
   private static File parent(File f) {
      String dirname = f.getParent();
      if (dirname == null) {
         if (f.isAbsolute()) {
            return new File(File.separator);
         }
        File ff = null;
        try {
           ff = new File(System.getProperty("user.dir"));
        } catch (Exception ex) {
        }
        return ff;
      }
      return new File(dirname);
   }

   /**
    * Liefert den von der Methode fileToString zuletzt gelesenen File (wenn
    * FileProtocol).
    */
   public static String getCurrentFile() {
      return currentFile;
   }

   /**
    * Liefert einen Satz von Properties unter Angabe ihres Namens. Wird auch
    * vom Generator verwendet! <BR>
    * Als erstes wird versucht, die Properties aus guibuilder.jar zu entnehmen.
    * Die Daten werden je nach Codebase mit http- oder file-Protocol gelesen.
    * Als letztes wird versucht, die Properties aus guibuilder.jar zu
    * entnehmen.
    */
   public static Properties loadProperties(String name) throws Exception {
      Properties prop = new Properties();
      Exception ex = null;
      // HTTP
      if (getProtocol().startsWith("http")) {
         try {
            URL url = new URL(getCodeBase(), name);

            HttpURLConnection con = (HttpURLConnection) url
                  .openConnection();
            con.setAllowUserInteraction(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.connect();
            InputStream in = con.getInputStream();
            prop.load(in);
            in.close();
            return prop;
         } catch (Exception ioe) {
            ex = ioe;
         }
      }
      // jar
      else if (getProtocol().equals("jar")) {
         try {
            JarFile jar = new JarFile("GuiBuilder.jar");
            ZipEntry zip = jar.getEntry(name);
            InputStream in = jar.getInputStream(zip);
            prop.load(in);
            return prop;
         } catch (Exception jex) {
            ex = jex;
         }
      }
      // file
      else {
         FileInputStream propFile = null;
         try {
            URL url = new URL(getCodeBase(), name);
            propFile = new FileInputStream(url.getFile());
            prop.load(propFile);
            propFile.close();
            return prop;
         } catch (Exception exe) {
            ex = exe;
         } finally {
            if (propFile != null) {
               try {
                  propFile.close();
               } catch (Exception exc) {
               }
            }
         }
      }
      URL res = GuiUtil.class.getResource("/" + name);
      if (res != null) {
         InputStream in = res.openStream();
         prop.load(in);
         in.close();
         return prop;
      }
      throw ex;
   }

   /**
    * Speichert Properties in der angegebenen Datei. <BR>
    * Nur bei file-Protocol.
    * 
    * @param prop
    *           Zu speichernde Properties
    * @param fileName
    *           Name der Datei unterhalb von CodeBase
    */
   public static void saveProperties(Properties p, String fileName) {
      File f = new File(GuiUtil.getUserDir(), fileName);
      FileOutputStream fos = null;
      try {
         fos = new FileOutputStream(f);
         p.store(fos, "GuiUtil.saveProperties");
      } catch (FileNotFoundException exc) {
         GuiUtil.showEx(exc);
      } catch (IOException exc) {
         GuiUtil.showEx(exc);
      } finally {
         if (fos != null) {
            try {
               fos.close();
            } catch (Exception ex) {
            }
         }
      }
   }

   /**
    * Liefert einen Swing-Icon auf Basis des Namens einer Resource:
    * <p>
    * <ul>
    * <li>Resouren im Classpath ("/icons/New24.gif")
    * <li>Resouren im Classpath ("/" + [DocumentBase] + "icons/New24.gif")
    * <li>Absolute URL ("http://www.myHomepage.com/images/myIcon.png")
    * <li>Relative URL zur DocumentBase ("images/myIcon.png")
    * <li>Namen einer Klasse, die das Interface "Icon" implementiert und im
    * Classpath enthalten ist ("javax.swing.plaf.metal.MetalComboBoxIcon")
    * </ul>
    * 
   * @see #setDocumentBase
   */
  public static Icon makeIcon(String name) {
    if (name == null || name.length() == 0) return getErrorIcon();
    URL url = null;
    Icon icon = null; 
    // 1. Als erstes Resourceloader aus GuiFactory probieren (wie bei GDL-Files)
    Class<?> resLoader = GuiFactory.getResourceLoader();
    if ( resLoader != null) {
      if (name.startsWith("/")) {        
        url = resLoader.getResource(name);
      } else {
        url = resLoader.getResource("/" + name);
      }
      icon = getIcon(url);
      if (icon != null) return icon;
    }
    // 2. Classloader
    ClassLoader loader = null;
    try {
        loader = ClassLoader.getSystemClassLoader();
        url = loader.getResource(name);
        if (url == null) { // nix gefunden: probieren mit "bla" und "/bla" falls "/bla" oder "bla"
          if (name.startsWith("/")) {        
            String s = name.substring(1);
            url = loader.getResource(s);
          } else {
            String s = "/" + name;
            url = loader.getResource(s);
          }
        }
        icon = getIcon(url);
        if (icon != null) return icon;
    } catch (Exception ex) {
      logger.warn(ex);
    }
    try {
        // 3. File
        int p = name.indexOf(":");
        if (p != -1) { // absolute URL
          if (p == 1) { // Laufwerksbuchstabe?
            name = "file:" + name;
          }
          url = new URL(name);
        } else { // relative URL
          // Wenn name nicht mit "/" anfängt, dann prüfen ob unter
          // "/" + [DocBase] + name eine Resource vorliegt
          if (name.startsWith("/") == false && loader != null) {
            url = loader.getResource("/" + getRelativeDocumentBase() + name);
            icon = getIcon(url);
            if (icon != null) return icon;
          }
          url = new URL(getDocumentBase() + name);          
        }
        icon = getIcon(url);
        if (icon != null) return icon;
    } catch (Exception ex) {
      logger.warn(ex);
    }
 
    if (icon == null) {
      return getErrorIcon();
    }
    return icon;
  } // End of method makeIcon

  private static Icon getIcon(URL url) {
    if (url == null) return null;
    //System.out.print(url);
    SoftReference<Icon> sr = imageCache.get(url);
    if (sr != null) {
      cacheHits++;
      if (sr.get() != null) {
        //System.out.println(" cache");
        return sr.get();
      }
    }

    ImageIcon icon = new ImageIcon(url);
    if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
      SoftReference<Icon> nsr = new SoftReference<Icon>(icon);
      imageCache.put(url, nsr);
      //System.out.println(" new");
      return icon;
    } else {
      //System.out.println(" null");
      return null;
    }
  }
  
  /**
   * Lädt das Icon, das angezeigt wird, wenn die in der Gui spezifizierten
   * Icons nicht gefunden werden.
   *  
   * @return Fehler-Icon
   * 	
   */
   public static Icon getErrorIcon() {
	  // Diese Methode sollte getImageIcon() nicht verwenden, um das ErrorIcon zu laden
	  // weil bei Fehlern in getImageIcon() dann Entlosschleifen entstehen können
	   
	  URL errorIconUrl = null; 
	  
	  // Auf Nummer "Sicher":
      if (errorIcon == null) {
         try {
        	 
        	// Versuche als SystemResource zu laden...
        	errorIconUrl = ClassLoader.getSystemResource(ERROR_ICON_FILE); 
        	
        	if (errorIconUrl == null) {
        		errorIconUrl = ClassLoader.getSystemResource("/" + ERROR_ICON_FILE);
        	}
        	
        	if (errorIconUrl == null) {
        		errorIconUrl = ClassLoader.getSystemResource(ERROR_ICON_PATH + "/"+ ERROR_ICON_FILE);
        	}

        	if (errorIconUrl == null) {
        		errorIconUrl = ClassLoader.getSystemResource("/" + ERROR_ICON_PATH + "/"+ ERROR_ICON_FILE);
        	}
        	
        	// Versuche als Resource zu laden
        	if (errorIconUrl == null) {
        		errorIconUrl = GuiUtil.class.getResource(ERROR_ICON_FILE);
        	}
        	
        	if (errorIconUrl == null) {
        		errorIconUrl = GuiUtil.class.getResource("/" + ERROR_ICON_FILE);
        	}
        	
        	if (errorIconUrl == null) {
        		errorIconUrl = GuiUtil.class.getResource(ERROR_ICON_PATH + "/"+ ERROR_ICON_FILE);
        	}

        	if (errorIconUrl == null) {
        		errorIconUrl = GuiUtil.class.getResource("/" + ERROR_ICON_PATH + "/"+ ERROR_ICON_FILE);
        	}        	
        	
        	if (errorIconUrl != null) {
				errorIcon = new ImageIcon(errorIconUrl);
				// Toolkit tk = Toolkit.getDefaultToolkit();				
				// errorIcon = tk.createImage((ImageProducer) errorIconUrl.getContent());        		
        	}
            // errorIcon = makeIcon("/sysimages/broken.gif");
            // errorIcon = makeIcon("sysimages/broken.gif");
         } catch (Exception ex) {
            System.err.println("Missing ErrorIcon /sysimages/broken.gif");
         }
      }
      return errorIcon;
   }

   /**
    * Liefert ein Window aus dem Cache oder erzeugt es, wenn nicht im Cache und
    * trägt das neue Fenster in den Cache ein. #deprecated
    */
   /*
    * public static GuiWindow getWindow(String name) { GuiWindow ret = null;
    * SoftReference sr = null; // Check Property "Cache_Windows" try { if
    * (getGuiProp("Cache_Windows").equals("true")) { sr =
    * (SoftReference)windowCache.get(name); if (sr != null) { ret =
    * (GuiWindow)sr.get(); } } // End If Cache_Windows } catch (Exception ex) {
    * // nix: Property fehlt } if (ret == null) { ret =
    * GuiFactory.getInstance().createWindow(name); // Insert Window into Cache?
    * try { if (getGuiProp("Cache_Windows").equals("true")) { sr = new
    * SoftReference(ret); windowCache.put(name, sr); } } catch (Exception ex) {
    * // nix: Property fehlt } } else { try { if
    * (getGuiProp("Reset_Window").equals("true")) ret.reset();
    * System.out.println("Window from Cache: "+name); } catch (Exception ex) {
    * // nix: Property fehlt } } return ret; }
    */

   /**
    * Zeigt die Größe des Image-Caches und die Anzahl Treffer.
    */
   public static void showImageCacheHits() {
      if (imageCache.size() != 0) {
         System.out.println("Image Cache Statistic: Size: "
               + imageCache.size() + " Hits: " + cacheHits);
      }
   }

   /**
    * Liefert ein awt.Image auf Basis eines Dateinamens. <BR>
    * Zuvor muß die DocumentBase gesetzt werden. <br>
    * Siehe Attribut img= bei Form, Applet unf InternalFrame.
    * 
    * @see #setDocumentBase
    */
   public static Image makeAwtImage(String fileName) {
      if (fileName == null || fileName.length() == 0) return null;
      URL url = null;
      Image icon = null;
      try {
        url = new URL(getDocumentBase(), fileName);
        icon = Toolkit.getDefaultToolkit().getImage(url);
        if (icon == null) {
          try {
            icon = Toolkit.getDefaultToolkit().getImage(
                GuiFactory.getInstance().getClass().getResource(fileName));
          } catch (Exception ex) {
            System.err.println("GuiUtil#makeAwtImage (" + fileName + "): " + ex.getMessage());
          }
        }
        return icon;
      } catch (MalformedURLException e) {
        GuiUtil.showEx(e);
        return null;
      }
   } // End of method getImage

   /**
     * Wandelt ein Label in einen legalen Bezeichner um. Es werden alle Zeichen
     * weggelassen, die nicht Buchstabe oder Zahl sind. <BR>
     * Buchstaben, die nach einem Leerzeichen stehen, werden in Großbuchstaben
     * verwandelt. <BR>
     * Deutsche Umlaute werden konvertiert. Der erste Buchstabe wird zu einem
     * Kleinbuchstaben.
     */
   public static String labelToName(String label) {
      StringBuilder buf = new StringBuilder();
      char c;
      boolean nextUpcase = false;
      boolean firstChar = true;
      for (int i = 0; i < label.length(); i++) {
         c = label.charAt(i);
         if (Character.isLetterOrDigit(c)) {
            if (firstChar == true) {
               c = Character.toLowerCase(c);
               firstChar = false;
            }
            if (nextUpcase == true) {
               c = Character.toUpperCase(c);
               nextUpcase = false;
            }
            switch (c) {
            case 'ä':
               buf.append("ae");
               break;
            case 'ö':
               buf.append("oe");
               break;
            case 'ü':
               buf.append("ue");
               break;
            case 'Ä':
               buf.append("Ae");
               break;
            case 'Ö':
               buf.append("Oe");
               break;
            case 'Ü':
               buf.append("Ue");
               break;
            case 'ß':
               buf.append("ss");
               break;
            case 'á':
               buf.append('a');
               break;
            case 'à':
               buf.append('a');
               break;
            case 'â':
               buf.append('a');
               break;
            case 'é':
               buf.append('e');
               break;
            case 'è':
               buf.append('e');
               break;
            case 'ê':
               buf.append('e');
               break;
            case 'ô':
               buf.append('o');
               break;
            default:
               buf.append(c);
            }
         } // End If LetterOrDigit
         else {
            switch (c) {
            case ' ':
               nextUpcase = true;
               break;
            case '-':
               buf.append('_');
               break;
            }
         }
      }
      return buf.toString();
   }

   /**
    * Wenn true, dann wird der GuiBuilder zur Laufzeit verwendet; <br>
    * wenn false, dann für Spezifikation.
    * <p>
    * Wenn true, werden
    * <ul>
    * <li>Feldnamen auf Eindeutigkeit geprüft (siehe Attribut name=).
    * <li>Eingebaute Methoden nicht ausgeführt; z.B. file="Close()".
    * </ul>
    */
   public static boolean isAPI() {
      return api;
   }

   /**
    * Wenn true, dann wird der GuiBuilder zur Laufzeit verwendet; <br>
    * wenn false, dann für Spezifikation.
    * <p>
    * Wenn true, werden
    * <ul>
    * <li>Feldnamen auf Eindeutigkeit geprüft (siehe Attribut name=).
    * <li>Eingebaute Methoden nicht ausgeführt; z.B. file="Close()".
    * </ul>
    */
   public static void setAPI(boolean b) {
      api = b;
   }
   public static void showEx(Throwable ex) {
     showEx(null, ex);
   } 
   /**
    * Diese Methode wird im Fehlerfall (catch) aufgerufen und zeigt die
    * Fehlermeldung an.
    */
   public static void showEx(String title, Throwable ex) {
      logger.error("showEx", ex);
      String[] buttons = { "OK", "Details" };
      String msg = ex.getLocalizedMessage();

      /*
       * KKN 24.05.2004 Wenn keine Message zur Exception vorhanden ist, dann
       * die Message von Cause ausgeben (wenn vorhanden).
       */
      if (msg == null) {
         Throwable ex2 = ex.getCause();
         if (ex2 != null)
            msg = ex2.getLocalizedMessage();
      }

      if (msg == null) {
         msg = "(No Message)";
      }
      Component parent = null;
      try {
         parent = GuiSession.getInstance().getCurrentWindow().getComponent();
      } catch (Exception pex) {
         // Null Pointer wenn Applet ??
      }
      if (msg.length() > 100) {
         msg = umbrechen(msg);
      }
      if (title != null) {
        title = title + " / " + ex.getClass().getName();
      } else {
        title = ex.getClass().getName();
      }
      int ret = JOptionPane.showOptionDialog(parent, msg, title, 
          0, JOptionPane.ERROR_MESSAGE, null, buttons, null);
      if (ret == 1) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         ex.printStackTrace(pw);
         String strace = sw.toString();
         if (strace.length() > 100) {
            strace = umbrechen(strace);
         }
         String dmsg = msg + "\n\n" + strace;
         JOptionPane.showMessageDialog(parent, dmsg,
               ex.getClass().getName(), JOptionPane.ERROR_MESSAGE);
      }
   }
   public static String umbrechen(String s) {
      if (s == null)
         return null;
      StringBuffer ret = new StringBuffer();
      StringTokenizer toks = new StringTokenizer(s, " ,;\n", true);
      StringBuffer line = new StringBuffer();
      while (toks.hasMoreTokens()) {
         String tok = toks.nextToken();
         if (line.length() > 100 && tok.length() > 1) {
            ret.append(line);
            ret.append('\n');
            line = new StringBuffer();
         }
         if (tok.equals("\n") == false) {
            line.append(tok);
         } else {
           ret.append(line);
         ret.append('\n');
         line = new StringBuffer();
         }
      }
      ret.append(line);
      return ret.toString();
   }

   /**
    * Zeigt das Hilfe-Fenster.
    * <p>
    * Wenn die Anwendung als Applet läuft, wird der Browser zur Anzeige der
    * Hilfe verwendet. <br>
    * Ansonsten wird unter Windows die mit der Extension "html" verknüpfte
    * Anwendung gestartet, bei anderen Betriebsystemen die Klasse GuiEditor.
    * 
    * @see #setApplet
    * @see GuiEditor
    */
   public static void showHelp(URL url, String target) {
      if (applet != null) { // Applet
         applet.getApplet().getAppletContext().showDocument(url, target);
      } else { // kein Applet
         String file = url.getFile();
         if (System.getProperty("os.name").startsWith("Windows")) { // Windows
            try {
               Runtime.getRuntime()
                     .exec(
                           "rundll32 "
                                 + "url.dll,FileProtocolHandler "
                                 + file);
            } catch (Exception ex) {
               showEx(ex);
            }
         } else { // nix Applet, nix Windows
            GuiForm dia = new GuiForm();
            dia.setTitle(target);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int w = 800;
            int h = Math.min(900, screen.height);
            dia.getComponent().setSize(w, h);
            GuiEditor edi = new GuiEditor();
            edi.getEditor().setEditable(false);
            try {
               edi.getEditor().setPage(url);
               dia.getForm().getContentPane().add(new GuiScrollBox(edi));
               dia.getComponent().setLocation((screen.width - w), 0);
               dia.show();
            } catch (Exception ex) {
               GuiUtil.showEx(ex);
            }
         } // End If Windows
      }
   }

   public static void showDocument(String fileName) {
      if (System.getProperty("os.name").startsWith("Windows")) { // Windows
         try {
            Runtime.getRuntime()
                  .exec(
                        "rundll32 " + "url.dll,FileProtocolHandler "
                              + fileName);
         } catch (Exception ex) {
            showEx(ex);
         }
      }
   }

   /**
    * Einfache Benachrichtigung an den Benutzer (nur Button OK).
    * 
    * @param title
    *           Titel der Nachricht
    * @param type
    *           Art der Nachricht: Error, Info, Warn, Ask
    * @param message
    *           Text der Nachricht.
    */
   public static void showMessage(GuiWindow parent, String title, String type,
         String message) {
      Component pc = null;
      if (parent != null) {
         parent.cursorDefault();
         pc = parent.getComponent();
      }
      int msg_type = JOptionPane.PLAIN_MESSAGE;
      if (type.equalsIgnoreCase("Error")) {
         msg_type = JOptionPane.ERROR_MESSAGE;
      } else if (type.equalsIgnoreCase("Info")) {
         msg_type = JOptionPane.INFORMATION_MESSAGE;
      } else if (type.equalsIgnoreCase("Warn")) {
         msg_type = JOptionPane.WARNING_MESSAGE;
      } else if (type.equalsIgnoreCase("Ask")) {
         msg_type = JOptionPane.QUESTION_MESSAGE;
      }
      JOptionPane.showMessageDialog(pc, message, title, msg_type);
   }

   public static boolean yesNoMessage(GuiWindow parent, String title,
         String message) {
      Component pc = null;
      if (parent != null) {
         parent.cursorDefault();
         pc = parent.getComponent();
      }
      if (JOptionPane.showConfirmDialog(pc, message, title,
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
         return true;
      }
      return false;
   }

   public static boolean okCancelMessage(GuiWindow parent, String title,
         String message) {
      Container pc = null;
      GuiWindow win = null;
      if (parent != null) {
         parent.cursorDefault();
         pc = parent.getComponent();
      } else {
    	 // Erst prüfen, ob es ein aktuelles Fenster gibt (z.B. bei Aufruf über Kommandozeile wäre das nicht der Fall)
    	 win = GuiSession.getInstance().getCurrentWindow();
    	 if (win != null) {
             win.cursorDefault();
    		 pc = win.getComponent();
    	 } else {
    		 // Kein Fenster, dann loggen und den Click auf Abbrechen simulieren
    		 logger.error("Failed to show okCancelMessage(GuiWindow parent = null,  String title = " + title + ", String message = " + message + ") due to GuiSession returning null as current window.");
    		 return false;
    	 }
      }
      if (JOptionPane.showConfirmDialog(pc, message, title,
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
         return true;
      }
      return false;
   }

   /**
    * Startet einen (Plattform-spezifischen) Datei-Öffnen-Dialog.
    * 
    * @param parent
    *           ParentWindow oder null
    * @param dialogTitle
    *           Beschriftung der Titelzeile des Dialoges.
    * @param directoryName
    *           das Das Directory, in dem der Dialog per default stehen soll.
    * @param fileName
    *           default für Filename; hier kann unter Windows auch ein
    *           Dateifilter z.B. im Format "*.xml;*.txt" angegeben werden.
    * @return ein StringArray mit drei Elementen: 0: den vollen Dateinamen (mit
    *         Pfad) <br>
    *         1: Das gewählte Directory <br>
    *         2: Der Filename ohne Pfad <br>
    *         oder null, wenn der Benutzer den Dialog abgebrochen hat.
    */
   public static String[] fileOpenDialog(GuiWindow parent, String dialogTitle,
         String directoryName, String fileName) {
      JFrame dummy = null;
      if (parent == null || parent.getGuiType() != GuiWindow.FORM) {
         dummy = new JFrame();
      } else {
         dummy = ((GuiForm) parent).getForm();
      }

      FileDialog fileDialog = new FileDialog(dummy, dialogTitle,
            FileDialog.LOAD);
      if (fileName != null) {
         fileDialog.setFile(fileName);
      }
      fileDialog.setDirectory(directoryName);
      // ***********************
      fileDialog.show();
      // ***********************
      if (fileDialog.getFile() != null) {
         return new String[] {
               fileDialog.getDirectory() + fileDialog.getFile(),
               fileDialog.getDirectory(), fileDialog.getFile() };
      }
      return null;
   }

   /**
    * Startet einen (Plattform-spezifischen) Datei-Speichern-Dialog.
    * 
    * @param parent
    *           ParentWindow oder null
    * @param dialogTitle
    *           Beschriftung der Titelzeile des Dialoges.
    * @param directoryName
    *           das Das Directory, in dem der Dialog per default stehen soll.
    * @param fileName
    *           default für Filename; hier kann unter Windows auch ein
    *           Dateifilter z.B. im Format "*.xml;*.txt" angegeben werden.
    * @return ein StringArray mit drei Elementen: 0: den vollen Dateinamen (mit
    *         Pfad) <br>
    *         1: Das gewählte Directory <br>
    *         2: Der Filename ohne Pfad <br>
    *         oder null, wenn der Benutzer den Dialog abgebrochen hat.
    */
   public static String[] fileSaveDialog(GuiWindow parent, String dialogTitle,
         String directoryName, String fileName) {
      JFrame dummy = null;
      if (parent == null || parent.getGuiType() != GuiWindow.FORM) {
         dummy = new JFrame();
      } else {
         dummy = ((GuiForm) parent).getForm();
      }
      FileDialog fileDialog = new FileDialog(dummy, dialogTitle,
            FileDialog.SAVE);
      fileDialog.setDirectory(directoryName);
      if (fileName != null) {
         fileDialog.setFile(fileName);
      }
      // ***********************
      fileDialog.show();
      // ***********************
      if (fileDialog.getFile() != null) {
         return new String[] {
               fileDialog.getDirectory() + fileDialog.getFile(),
               fileDialog.getDirectory(), fileDialog.getFile() };
      }
      return null;
   }

   public static ResourceBundle getDefaultResourceBundle() {
      return defaultBundle;
   }

   public static void setDefaultResourceBundle(String name) {
      if (name == null) {
         defaultBundle = null;
         defaultBundleName = null;
      } else {
         try {
            defaultBundle = ResourceBundle.getBundle(name);
            defaultBundleName = name;
         } catch (Exception ex) {
            logger.error(name, ex);
         }
      }
   }

   /**
    * Hiermit kann das Default-Locale geändert werden.
    */
   public static void setLocale(String language, String country) {
      try {
         Locale.setDefault(new Locale(language, country));
         if (getDefaultResourceBundle() != null) {
            setDefaultResourceBundle(defaultBundleName);
         }
      } catch (Exception ex) {
         System.out.println(ex.getMessage());
      }
   }

   public static void setLocale(String loc) {
      int p = loc.indexOf("_");
      if (p != -1) {
         setLocale(loc.substring(0, p), loc.substring(p + 1));
      } else {
         setLocale(loc, "");
      }
   }

   public static Locale getLocale() {
      return Locale.getDefault();
   }

   /**
    * Diese Methode prüft, ob Pnuts oder BeanShell installiert ist.
    * <p>
    * Hierzu wird versucht die Klasse pnuts.lang.Jump bzw, bsh.InterpreterError
    * zu laden.
    */
   private static void checkScripting() {
      scripting = false;
      // BeanShell
      try {
         // Lädt (hoffentlich) nur eine Klasse
         Class.forName("bsh.InterpreterError");
         scripting = true;
         System.out.println("Installed Scripting Language: BeanShell");
      } catch (Throwable ex) {
      }
      // Groovy
      try {
         // Lädt (hoffentlich) nur eine Klasse
         Class.forName("groovy.swing.impl.Startable");
         scripting = true;
         System.out.println("Installed Scripting Language: Groovy");
      } catch (Throwable ex) {
      }
      // Pnuts
      try {
         // Lädt nur eine Klasse!
         Class.forName("pnuts.lang.Jump");
         scripting = true;
         System.out.println("Installed Scripting Language: Pnuts");
      } catch (Exception ex) {
      }
      if (scripting == false) {
         System.out.println("No Scripting Language Installed");
      }
   }

   /**
    * Zeigt an, ob Scripting mit BeanShell, Groovy oder Pnuts verfügbar ist.
    * <p>
    * Scripting funktioniert nicht wenn GuiBuilder als Applet läuft, oder wenn
    * Pnuts nicht im Classpath enthalten ist.
    */
   public static boolean hasScripting() {
      return scripting;
   }

   static Attribute createAtt(String name, int value) {
      return new Attribute(name, Integer.toString(value));
   }

   static Attribute createAtt(String name, boolean value) {
      return new Attribute(name, Convert.toString(value));
   }

//   static Attribute createAtt(String name, double value) {
//      return new Attribute(name, Double.toString(value));
//   }

   /**
    * Gibt das Basisverzeichnis von clazz resp. des Jars, in dem diese Klasse
    * sich befindet zurück. Holt hierfür die URL der Klass und unterscheidet
    * folgende Fälle: jar:file://netzlaufwerk/pfad/MyApp.jar
    * file://netzlaufwerk/pfad/MyApp.class jar:file:/X:/pfad/MyApp.jar
    * file://X:/pfad/MyApp.class
    * 
    * @return
    */
   public static String getBasePath(Class clazz) {   
       String raw=getClassPath(clazz);
       if(raw == null) {
           return ".";
       }
       String found=null;
      Pattern p = Pattern
            .compile(".*?file:(\\/{0,2})(.+?)[^\\\\\\/]+\\.(jar|class).*");
       Matcher m=p.matcher(raw);
       if(m.matches()){
           found=m.group(2);
           if(found.matches("[a-zA-Z]:.+")){
               return found;
           }
           return m.group(1)+found;
       }
       return found;
   }
   public static String getClassPath( Class clazz ) {    
      ClassLoader loader = clazz.getClassLoader();
      if (loader == null) {
         return null;
      }
      URL url = loader.getResource(clazz.getName().replace('.','/')
                                    + ".class");
      //System.out.println("URL: "+url);
      return ( url != null ) ? url.toString() : null;
  }
   
   
   /**
    * @deprecated
    */
   static Element createElementXY(Element ele, JComponent comp) {
      // Element ele = new Element("Label");
      // ele.setAttribute("label", comp.getText());
      // ele.setAttribute("name", comp.getName());
      ele.setAttribute(GuiUtil.createAtt("x", comp.getLocation().x));
      ele.setAttribute(GuiUtil.createAtt("y", comp.getLocation().y));
      ele.setAttribute(GuiUtil.createAtt("w", comp.getSize().width));
      ele.setAttribute(GuiUtil.createAtt("h", comp.getSize().height));
      // GridBagConstraints
      GridBagLayout gbl = GuiMember.getGridBagLayout(comp);
      if (gbl != null) {
         GridBagConstraints c = gbl.getConstraints(comp);
         // ele.setAttribute(GuiUtil.createAtt("wx", c.weightx));
         // ele.setAttribute(GuiUtil.createAtt("wy", c.weighty));
         ele.setAttribute(GuiUtil.createAtt("an", c.anchor));
         ele.setAttribute(GuiUtil.createAtt("fill", c.fill));
      }
      return ele;
   }

   /**
    * Default-Initialisierung
    */
   static {
    // Try-catch erscheint sinnvoll bei statischer Initialisierung
    try {
      nnColor = new Color(243, 243, 192); // Default NotNull BG-Color
      try {
        logger = org.apache.log4j.Logger.getLogger(GuiUtil.class);
      } catch (Exception ex) {
        // No logger
      }
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception ex) {
        if (logger != null) {
          logger.error("Error: In static init of GuiUtil failed to setLookAndFeel: ", ex);
        } else {
          System.err.println("Error: In static init of GuiUtil failed to setLookAndFeel: " + ex.getMessage());
        }
      }
      try {
        setDefaultResourceBundle("GuiBundle");
      } catch (Exception ex) {
        if (logger != null) {
          logger.error("Error: In static init of GuiUtil failed to setREsourceBundle: ", ex);
        } else {
          System.err.println("Error: In static init of GuiUtil failed to setResourceBundle: "
              + ex.getMessage());
        }
      }
      loadGuiPropXml(); // Oh weh! der braucht schon Codebase!
      checkScripting();
    } catch (Throwable ex) {
      if (logger != null) {
        logger.error("Error: In static init of GuiUtil failed for reason: ", ex);
      } else {
        System.out.println("Error: In static init of GuiUtil failed for reason: " + ex.getMessage());
      }
    }
  }
}
