/*
 * Created on 14.06.2003
 */
package de.guibuilder.framework;

import com.jgoodies.forms.layout.*;

import java.awt.*;
/**
 * @author peter
 */
final class FormLayoutHelper extends AbstractLayoutHelper {
	// Attributes
	private FormLayout layout;
	private String colSpec;
	private String rowSpec;
	private int currentX;
	private int currentY;
	// Constructor
	FormLayoutHelper(String col, String row) {
		this.colSpec = col;
		this.rowSpec = row;
		if (colSpec != null && rowSpec != null) {
			layout = new FormLayout(colSpec, rowSpec);					
		} else if (colSpec != null) {
			layout = new FormLayout(colSpec);								
		} else {
			this.colSpec = GuiUtil.getConfig().getValuePath(".FormLayout@ColSpec");
			this.rowSpec = GuiUtil.getConfig().getValuePath(".FormLayout@RowSpec");
			if (rowSpec != null) {
				layout = new FormLayout(colSpec, rowSpec);
			} else {
				layout = new FormLayout(colSpec);
			}			
		}
	}	
	// Methods
	LayoutManager getLayoutManager() {
		return layout;
	}
	void reset() {
		currentX = 0;
		currentY = 0;
	}
	Object addAbsolut(GridBagConstraints c) {
		// Alignment
		CellConstraints.Alignment hAlign = CellConstraints.DEFAULT;
		CellConstraints.Alignment vAlign = CellConstraints.DEFAULT;
				
		switch (c.anchor) {
			case GridBagConstraints.NORTHEAST :
				hAlign = CellConstraints.RIGHT;
				vAlign = CellConstraints.TOP;
			break;
			case GridBagConstraints.EAST :
				hAlign = CellConstraints.RIGHT;
				vAlign = CellConstraints.CENTER;
			break;
			case GridBagConstraints.SOUTHEAST :
				hAlign = CellConstraints.RIGHT;
				vAlign = CellConstraints.BOTTOM;
			break;
			case GridBagConstraints.SOUTH :
				hAlign = CellConstraints.CENTER;
				vAlign = CellConstraints.BOTTOM;
			break;
			case GridBagConstraints.SOUTHWEST :
				hAlign = CellConstraints.LEFT;
				vAlign = CellConstraints.BOTTOM;
			break;
			case GridBagConstraints.WEST :
				hAlign = CellConstraints.LEFT;
				vAlign = CellConstraints.CENTER;
			break;
			case GridBagConstraints.NORTHWEST :
				hAlign = CellConstraints.LEFT;
				vAlign = CellConstraints.TOP;
			break;
			case GridBagConstraints.NORTH :
				hAlign = CellConstraints.CENTER;
				vAlign = CellConstraints.TOP;
			break;
			case GridBagConstraints.CENTER :
				hAlign = CellConstraints.CENTER;
				vAlign = CellConstraints.CENTER;
			break;
		}
		if (c.weightx != 0) {
			hAlign = CellConstraints.FILL;
		}
		if (c.weighty != 0) {
			vAlign = CellConstraints.FILL;
		}
		// umrechen Posi
		// 1-relativ! um Himmels Willen!
		// überall eine Spalte "Durchschuß"
		int x = c.gridx * 2 + 2;
		int y = c.gridy * 2 + 2;
		int w = c.gridwidth * 2 - 1;
		int h = c.gridheight * 2 - 1;
		// Create Cellconstr
		/*
		CellConstraints cc = 
			new CellConstraints(
				x, y, w, h,
				hAlign, vAlign, c.insets);
		*/
		CellConstraints cc = 
			new CellConstraints(
				x, y, w, h,
				hAlign, vAlign);
		return cc;   	
		
	}
	Object addBehind(GridBagConstraints c) {
		c.gridx = currentX;
		Object ret = this.addAbsolut(c);
		currentX = currentX + c.gridwidth;
		return ret;
	}
	Object addBelow(GridBagConstraints c) {
		currentX = 0;
		c.gridx = 0;
		c.gridy = currentY;
		Object ret = this.addAbsolut(c);		
		currentY = currentY + c.gridheight;
		currentX = currentX + c.gridwidth;
		return ret;
	}
}
