/*
 * Created on 26.11.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package de.pkjs.pl.gui;

import de.pkjs.pl.PL;

import org.apache.log4j.xml.DOMConfigurator;
/**
 * @author peter
 */
public class GuiMain {
	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GuiMain.class);

	public static void main(String[] args) {
		DOMConfigurator.configure("Log4J_PL_GUI.xml");
		logger.info("GuiMain gestartet #################");
		ControlTree ctrl = ControlTree.getInstance();
		try {
			/*
			File f = new File("c:/temp/ds2.xml");
			Document doc = new Document(f);
			JDataSet ds = new JDataSet(doc);
			System.out.println(ds.getXml());
			PL pl = new PL(ds);
			JDataSet dsMeta = pl.getMetaDataSet();
			System.out.println(ds.getXml());
			*/
			// Geht nicht! static MetaDataTable!
			//PL plCopy = new PL(ds);
			//JDataSet dsCopy = plCopy.getMetaDataSet();
			//System.out.println(dsCopy.getXml());
			PL pl = new PL();
			pl.setDebug(true);
			ctrl.setPL(pl);
			/*
			try {
				Document doc = new Document(new File("MetaConfig.xml"));
				PL metaPL = new PL(doc);
				metaPL.setDebug(true);
				metaPL.setDataset(ds);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
//		ctrl.show();
	}
}
