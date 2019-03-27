/*
 * Created on 20.01.2004
 */
package de.guibuilder.framework.event;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiSession;
import de.guibuilder.framework.GuiWindow;

/**
 * @author pkoeker
 */
public class GuiDropEvent extends GuiUserEvent {
	public DropTargetDropEvent event;
	public GuiMember dragSource;
	public String value;
	// Constructor
   public GuiDropEvent(GuiWindow win, GuiMember member, DropTargetDropEvent event) {
      super(win, member);
      this.event = event;
      // TODO: Wie kommt man an die DropDaten?
      Transferable tr = event.getTransferable();
      DataFlavor[] dfs = tr.getTransferDataFlavors();
      Object[] os = new Object[dfs.length];
      StringBuilder sb = new StringBuilder();
      for(int i = 0; i < dfs.length; i++) {
         DataFlavor df = dfs[i];
         try {
            Object o = tr.getTransferData(df);
            if (o != null) {
            }
            os[i] = o;
            if (o instanceof String) {
               sb.append(os[i] + "\n");
            } else if (o instanceof InputStream) {
               InputStream in = (InputStream)os[i];
               int s = 0;
               do {
                  byte[] b = new byte[256];
                  s = in.read(b);
                  sb.append(new String(b));
               } while (s != -1);
               in.close();
            } else if (o instanceof CharBuffer) {
               CharBuffer cb = (CharBuffer)o;               
               sb.append(cb);
            } else if (o instanceof InputStreamReader) {
               InputStreamReader in = (InputStreamReader)os[i];
               int s = 0;
               do {
                  char[] c = new char[256];
                  s = in.read(c);
                  sb.append(new String(c));
               } while (s != -1);
               in.close();
            } else if (o instanceof StringReader) {
               StringReader in = (StringReader)os[i];
               int s = 0;
               do {
                  char[] c = new char[256];
                  s = in.read(c);
                  sb.append(new String(c));
               } while (s != -1);
               in.close();
            } else if (o instanceof ByteBuffer) {
               ByteBuffer bb = (ByteBuffer)os[i];
               sb.append(bb);
            } else {
               System.out.println(i + ": " +o.getClass().getName());
            }
         }
         catch(UnsupportedFlavorException e) {
            System.err.println(e.getMessage());
         }
         catch(IOException e) {
            System.err.println(e.getMessage());
         }
      }
      this.value = sb.toString();
      this.dragSource = (GuiMember)GuiSession.getInstance().getProperty("LastDragSource");
   }

	// From GuiUserEvent
	public int getEventType() {
		return DROP;
	}
	public GuiMember getDragSource() {
	   return this.dragSource;
	}
}
