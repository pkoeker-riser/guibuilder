package de.jdataset;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import electric.xml.Document;
/**
 * Hilfsklasse für die Konvertierung der verschiedenen
 * DataSets.
 * @author peter
 */
public class DataSetFactory {
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DataSetFactory.class);
  private static String ENCODING = "UTF-8";
  
  /**
   * Abweichendes Encoding statt UTF-8 setzen
   * @param encoding
   */
  public static void setDefaultEncoding(String encoding) {
    ENCODING = encoding;
  }

	/**
	 * Erzeugt einen serialisierten DataSet aus einem "normalen" DataSet 
	 * (ohne Komprimierung); Encoding = UTF-8.
	 * @param ds
	 * @return
	 */
	public static SDataSet getSDataSet(JDataSet ds) {
		return getSDataSet(ds, true, ENCODING);
	}
	/**
   * Erzeugt einen serialisierten DataSet aus einem "normalen" DataSet 
   * Encoding = UTF-8.
	 * @param ds
	 * @param compressed
	 * @return
	 */
  public static SDataSet getSDataSet(JDataSet ds, boolean compressed) {
    return getSDataSet(ds, compressed, ENCODING);
  }
	/**
   * Erzeugt einen serialisierten DataSet aus einem "normalen" DataSet 
   * (ohne Komprimierung).
	 * @param ds
	 * @param encoding
	 * @return
	 */
  public static SDataSet getSDataSet(JDataSet ds, String encoding) {
    return getSDataSet(ds, true, encoding);
  }
	/**
	 * Erzeugt einen (comprimierten) serialisierten DataSet
	 * @param ds
	 * @param compressed Um Bandbreite zu sparen können die Daten mit
	 * gzip komprimiert werden.
	 */
	public static SDataSet getSDataSet(JDataSet ds, boolean compressed, String encoding) {
    long start = System.currentTimeMillis();
    String s = ds.toString(encoding);
    long duras = System.currentTimeMillis() - start;
		byte[] bytes;
		try {
		   if (compressed) {
  				//System.out.println("Input:" + s.length());
  				ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
  				GZIPOutputStream zipout = new GZIPOutputStream(baos);
  				zipout.write(s.getBytes(encoding));
  				zipout.close();
  				bytes = baos.toByteArray();
  				//System.out.println("Gzip : " + myDataset.length);
  		   } else {
  		      bytes = s.getBytes(encoding);
  		   }
		} catch (Exception ex) {
		  throw new IllegalStateException(ex.getMessage());
		}
		SDataSet sds = new SDataSet(bytes, compressed, encoding);
		long dura = System.currentTimeMillis() - start;
		logger.debug("#Bytes: " + s.length() + "  Duration: " + duras+"/"+dura);
		return sds;
	}
	
	/**
	 * Verwandelt einen serialisierten Dataset in einen "normalen"
	 * @param sds
	 * @return
	 */
	public static JDataSet getDataSet(SDataSet sds) {
    long start = System.currentTimeMillis();
		try {
			String s = null;
			if (sds.isCompressed()) {
				ByteArrayInputStream bais = new ByteArrayInputStream(sds.getBytes());
				GZIPInputStream zipin = new GZIPInputStream (bais);
				ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
				int length = 0;
				int blockSize = 1024;
				byte buffer[] = new byte[blockSize];
				while ( (length = zipin.read(buffer, 0, blockSize)) != -1 ) {
					baos.write( buffer, 0, length );
				}
				baos.close();
				zipin.close();
				s = new String(baos.toByteArray(), sds.getEncoding());
			} else {
				s = new String(sds.getBytes(), sds.getEncoding());
			}
			Document doc = new Document(s);
			JDataSet ds = new JDataSet(doc);
			long dura = System.currentTimeMillis() - start;
			logger.debug("#Bytes: " + s.length() + "  Duration: " + dura);
			return ds;
		} catch (Exception ex) {
			throw new IllegalStateException(ex.getMessage());
		}
	}
}
