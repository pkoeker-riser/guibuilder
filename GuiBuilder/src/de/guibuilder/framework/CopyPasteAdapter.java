package de.guibuilder.framework;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import de.guibuilder.framework.GuiTable.GuiTableColumn;

/**
 * 
 * Adapter copies and pastes from the clipboard in tab delimited format.
 * Compatible with Excel
 * 
 */
public class CopyPasteAdapter implements ActionListener {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CopyPasteAdapter.class);

	private static final String LINE_BREAK = "\n";
	private static final String CELL_BREAK = "\t";
	private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

	private GuiTable gt;
	private final JTable table;
	
	private int[] disabledColumnsForPasting;
	
	
	public CopyPasteAdapter(GuiTable gt) {
		this.gt = gt;
		this.table = (JTable) gt.getJComponent();
		try {
			KeyStroke copy, cut, paste, pasteInsert;
			copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false);
			cut = KeyStroke.getKeyStroke(KeyEvent.VK_X,ActionEvent.CTRL_MASK,false);
			paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK,false);
			pasteInsert = KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK,false); // Ctrl+Shift-V
			table.registerKeyboardAction(this,"Copy",copy,JComponent.WHEN_FOCUSED);
			table.registerKeyboardAction(this,"Cut",cut,JComponent.WHEN_FOCUSED);
			table.registerKeyboardAction(this,"Paste",paste,JComponent.WHEN_FOCUSED);
			table.registerKeyboardAction(this,"PasteInsert",pasteInsert,JComponent.WHEN_FOCUSED);
		} catch (Exception ex) {
			GuiUtil.showEx(ex);
		}
	}

	private void pasteFromClipboard(boolean insert) {
		int startRow = 0;
		int startCol = 0;
		int selectedRows[] = table.getSelectedRows();
		int selectedCols[] = table.getSelectedColumns();
		if (selectedRows.length == 0) {
			gt.insertRow();
		} else {
			startRow = selectedRows[0];
		}
		if (selectedCols.length > 0) {
			startCol = selectedCols[0];
		}
		try {
			String trstring = (String)(CLIPBOARD.getContents(this).getTransferData(DataFlavor.stringFlavor));
			if (insert) { // zuvor Platz schaffen
				String[] lines = trstring.split("\n"); 
				startRow++;
				for (int i = 0; i < lines.length; i++) {
					gt.insertRow(startRow + i);
				}
			}
			
			StringTokenizer st1 = new StringTokenizer(trstring, "\n", true); 
			int line = 0;
			while (st1.hasMoreTokens()) {
				if (startRow + line +1 > gt.getRowCount()) {
					gt.insertRow();
				}
				String rowstring = st1.nextToken();
				if ("\n".equals(rowstring)) {
					line++;
					continue;
				}
				StringTokenizer st2 = new StringTokenizer(rowstring, "\t", true);
				int col = 0;
				while (st2.hasMoreTokens()) {
					Object value = st2.nextToken();
					if ("\t".equals(value)) {
						col++;
						continue;
					}
					if (startRow + line < gt.getRowCount() 
							&& startCol + col < table.getColumnCount()) {
						if (isColumnForPastingDisabled(startCol + col))
							continue;
						// Wenn Combobox, dann prüfen ob DisplayMember angegeben und ggf. auf ValueMember abbilden
						GuiTableColumn gtcol = gt.getColumn(startCol + col);
						GuiComponent comp = gtcol.getGuiComponent();
						if (comp instanceof GuiCombo) {
							GuiCombo combo = (GuiCombo)comp;
							if (combo.getMap() != null) {
								Object mapVal = combo.getValueMemberValue(value);
								if (mapVal != null) {
									value = mapVal;
								}
							}
						}
						gt.setValueAt(value, startRow + line, startCol + col);
					}
				}
			}
			if (gt.hasPasteCallbackAdapter()) {
				gt.getPasteCallbackAdapter().callback();
			}
			table.repaint();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	private String escape(Object cell) {
		if (cell == null) {
			return "";
		} else {
			return cell.toString().replace(LINE_BREAK, " ").replace(CELL_BREAK, " ");
		}
	}
	
	private void copyTableSelection(boolean isCut) {
		StringBuilder sbf = new StringBuilder();
		int numcols = table.getSelectedColumnCount();
		int numrows = table.getSelectedRowCount();
		int[] rowsselected = table.getSelectedRows();
		int[] colsselected = table.getSelectedColumns();
		
		int startCol = 0;
		if (colsselected.length > 0) {
			startCol = colsselected[0];
		}
		
		for (int i = 0; i < numrows; i++) {
			for (int j = 0; j < numcols; j++) {
				Object val = table.getValueAt(rowsselected[i], colsselected[j]);
				val = this.escape(val);
				sbf.append(val);
				if (j < numcols - 1) {
					sbf.append("\t");
				}
				if (isCut) {
					if (!isColumnForPastingDisabled(startCol + j)) { // deaktivierte Spalten nicht löschen
						table.setValueAt(null, rowsselected[i], colsselected[j]);
					}
				}
			}
			sbf.append("\n");
		}
		String s = sbf.toString();
		if (!"\n".equals(s)) { // not empty Selection
			StringSelection stsel = new StringSelection(sbf.toString());
			CLIPBOARD.setContents(stsel, stsel);
		}
	}

	private boolean isColumnForPastingDisabled(int col) {
		if (disabledColumnsForPasting == null)
			return false;
		for (int i = 0; i < disabledColumnsForPasting.length; i++) {
			if (disabledColumnsForPasting[i] == col) {
				return true;
			}
		}
		return false;
	}

	void setDisabledColumnsForPasting(int[] p_disabledColumnsForPasting) {
		disabledColumnsForPasting = p_disabledColumnsForPasting;
	}

	/**
	 * This method is activated on the Keystrokes we are listening to
	 * in this implementation. Here it listens for Copy and Paste ActionCommands.
	 * Selections comprising non-adjacent cells result in invalid selection and
	 * then copy action cannot be performed.
	 * Paste is done by aligning the upper left corner of the selection with the
	 * 1st element in the current selection of the JTable.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().compareTo("Copy")==0 || e.getActionCommand().compareTo("Cut")==0){
			copyTableSelection(e.getActionCommand().compareTo("Cut")==0);
		}
		if (e.getActionCommand().compareTo("PasteInsert")==0){
			pasteFromClipboard(true);
		} else if (e.getActionCommand().compareTo("Paste")==0){
			pasteFromClipboard(false);
		}
	}
}