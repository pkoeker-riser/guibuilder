/*
 * Created on 14.06.2003
 */
package de.guibuilder.framework;

import java.awt.*;

/**
 * @author peter
 */
abstract class AbstractLayoutHelper {
	abstract Object addAbsolut(GridBagConstraints c);
	abstract Object addBehind(GridBagConstraints c);
	abstract Object addBelow(GridBagConstraints c);
	abstract LayoutManager getLayoutManager();
	abstract void reset();
}

