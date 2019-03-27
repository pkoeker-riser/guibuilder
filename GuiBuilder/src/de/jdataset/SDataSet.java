package de.jdataset;

import java.io.Serializable;

/**
 * Ein DataSet wird hier in ein ByteArray verwandelt.<p>
 * Der Sinn dieser Übung besteht darin, die Serialisierung
 * (z.B. für SOAP oder HTTP) zu vereinfachen.<br>
 * Diese Art der Serialisierung spart gegenüber SOAP ca. 85% Bandbreite.<p>
 * Eine Komprimierung der Daten reduziert den Durchsatz noch einmal um
 * mehr als 95%; diese Komprimierung lohnt sich besonders bei größeren Datenmengen.<p>
 * Wenn keine Encoding angegeben, wird UTF-8 verwendet.
 * @author peter
 */
public class SDataSet implements Serializable {
   private static final long serialVersionUID = -720995201957465176L;

   private byte[] myDataset;
	private boolean compressed;
	private String encoding = "UTF-8";	

	/**
	 * @deprecated For serialization purpose only
	 */
	public SDataSet() {}
	
	/**
	 * Ohne Komprimierung; encoding = "UTF-8"
	 * @param bytes
	 */
	public SDataSet(byte[] bytes) {
		this(bytes, false);
	}
	/**
	 * Erzeugt einen serialisierten DataSet
	 * @param ds
	 * @param compressed Um Bandbreite zu sparen werden die Daten mit gzip komprimiert.
	 */
	public SDataSet(byte[] bytes, boolean compressed) {
		this.compressed = compressed;
		this.myDataset = bytes;
	}
	/**
	 * Erzeugt einen serialisierten DataSet, bei dem das angegebene Encoding verwendet wurde
	 * @param bytes
	 * @param compressed
	 * @param encoding
	 */
  public SDataSet(byte[] bytes, boolean compressed, String encoding) {
    this.compressed = compressed;
    this.myDataset = bytes;
    this.encoding = encoding;
  }
	
	public byte[] getBytes() {
		return this.myDataset;
	}
	/**
	 * @deprecated For serialization purpose only
	 */
  public void setBytes(byte[] bytes) {
      this.myDataset = bytes;
  }

	/**
	 * @deprecated For serialization purpose only
	 */
  public void setCompressed(boolean compressed) {
      this.compressed = compressed;
  }
	public boolean isCompressed() {
		return this.compressed;
	}
	
	public void setEncoding(String encoding) {
	  this.encoding = encoding;
	}
	
	public String getEncoding() {
	  return encoding;
	}
	
	/**
	 * Gibt die Länge des internen byte-Arrays an.
	 * @return
	 */
	public int getSize() {
		if (myDataset != null) {
			return this.myDataset.length;
		} else {
			return 0;
		}
	}
}
