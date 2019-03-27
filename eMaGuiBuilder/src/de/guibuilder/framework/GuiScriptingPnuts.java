package de.guibuilder.framework;

import java.util.HashSet;

//import pnuts.lang.PnutsException;

import de.guibuilder.framework.event.GuiUserEvent;

/**
 * Ein Object dieser Klasse hält ein Pnuts-Script
 */
final class GuiScriptingPnuts extends GuiScripting {
	private pnuts.lang.Context context;
	/**
	 * Pnuts-interne Funktionen nicht aufrufen. Siehe unter static
	 */
	private static HashSet<String> pnutsIntern;
	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(GuiScriptingPnuts.class);

	// Constructor
	/**
	 * Erzeugt ein neues Script aus dem übergebenen Sourcecode.
	 */
	GuiScriptingPnuts(String source) {
		this.initContext(source);
	}

	private void initContext(String source) {
		this.context = new pnuts.lang.Context();
		try {
			context.setImplementation(new pnuts.lang.PnutsImpl());
			pnuts.lang.Pnuts.load(new java.io.StringReader(source), context);
		} catch (Exception ex) {
			GuiUtil.showEx(ex);
		}
	}

	GuiInvokationResult invokeScripting(String cmd, GuiUserEvent event) {
		GuiInvokationResult result = new GuiInvokationResult("Pnuts: "+ cmd);
		try {
			// prüfen, ob Pnuts-interner Funktionsname vorliegt
			if (pnutsIntern.contains(cmd)) {
				result = new GuiInvokationResult(new IllegalArgumentException(
						"Warning! Do not use Pnuts build-in Functions: " + cmd));
				logger.error(result.exception);
				return result;
			}
			Object ret = pnuts.lang.PnutsFunction.call(cmd, new Object[] { event }, context);
			result.done = true;
			result.returnValue = ret;
			return result;
		} catch (pnuts.lang.PnutsException ex) {
			result = new GuiInvokationResult(ex);
			logger.error(ex);
			logger.error(ex.getMessage() + " (" + ex.getLine() + ") -> "
					+ ex.getThrowable().getClass().getName() + " "
					+ ex.getThrowable().getMessage());
			return result;
		}
	}

	static {
		// Namen der Pnuts-Funktionen, die nicht aufgerufen werden dürfen.
		pnutsIntern = new HashSet<String>();
		pnutsIntern.add("import");
		pnutsIntern.add("package");
		pnutsIntern.add("getContext");
		pnutsIntern.add("throw");
		pnutsIntern.add("catch");
		pnutsIntern.add("use");
		pnutsIntern.add("load");
		pnutsIntern.add("require");
		pnutsIntern.add("loadFile");
		pnutsIntern.add("eval");
		pnutsIntern.add("defined");
		pnutsIntern.add("class");
		pnutsIntern.add("quit");
		pnutsIntern.add("autoload");
	}
}