package de.jdataset;

/**
 * Bildet Unicode-Zeichen auf einfache Zeichensätze ab (für Reportwriter)
 * @author pkoeker
 */
public interface EncodingFilter {
   public enum Encoding {UTF8_TO_ASCII, UTF8_TO_ISO, UTF8_TO_SUCHFORM}
   /**
    * @formatter:off
    * @param source
    * @param type 
    * @return
    * @formatter:on
    */
   public String encode(String source, Encoding e);
   /**
    * @formatter:off
    * Konvertiert alle Sonderzeichen mit Ausnahme der deutschen Umlaute nach ASCII é --> e
    * @param source
    * @return
    * @formatter:on
    */   
   public String utf8ToAsciiGerman(String source);
   /**
    * @formatter:off
    * Bildes Unicode-Sonderzeichen auf ISO-8859-1 ab Ặ --> A
    * @param source
    * @return
    * @formatter:on
    */
   public String utf8ToIso(String source);
   public String utf8ToSuchform(String source);
}
