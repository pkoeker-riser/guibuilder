/*
 * Created on 18.06.2003
 */
import de.pkjs.pl.PL;
import de.jdataset.*;

import de.guibuilder.adapter.PersistenceAdapter;
import de.guibuilder.framework.*;

import electric.xml.*;
/**
 * @author peter
 */
public class TestConnection {

	public static void main(String[] args) {
		try {
			PL pl = new PL();
			System.out.println("*** Getting Database Meta Data ***");
			Document doc = pl.getDatabaseMetaDataDoc();
			System.out.println(doc);
			System.out.println("*** Getting Dataset ***");
			JDataSet ds = pl.getAll("AnzahlAdressen");
			System.out.println(ds.getXml());
			int anzahl = ds.getValueIntPath("@Anzahl");
			System.out.println("Anzahl: "+anzahl);
			System.out.println("*** End ***");
			// Adapter
			PersistenceAdapter ada = PersistenceAdapter.getInstance();
			ada.setDatabase(pl);
			GuiSession.getInstance().setAdapter(ada);
			// Start 
			GuiUtil.setDocumentBase("example/dataset/forms/");
			GuiWindow win = GuiFactory.getInstance().createWindow("PLAdresse.xml");
			win.show();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
