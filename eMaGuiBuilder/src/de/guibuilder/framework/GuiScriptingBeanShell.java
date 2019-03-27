package de.guibuilder.framework;

import bsh.Interpreter;
import bsh.NameSpace;
import de.guibuilder.framework.event.GuiUserEvent;

/**
 * Diese Klasse ist für das Scripting mit BeanShell zuständig.
 */
final class GuiScriptingBeanShell extends GuiScripting {
   // Attribute
   private Interpreter context = new Interpreter();
   private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GuiScriptingBeanShell.class);

   // Constructor
   /**
    * Erzeugt ein neues Script aus dem übergebenen Sourcecode 
    * oder Dateiname.
    */
   GuiScriptingBeanShell(String source, String filename) {
      this.initContext(source, filename);
   }

   // Methods
   private void initContext(String source, String filename) {
      try {
         if(source != null && source.length() > 3) {
            context.eval(source);
         }
         else {
            context.source(filename);
         }
      }
      catch(Exception ex) {
         ex.printStackTrace();
         logger.error(ex);
      }
   }

   // From GuiScripting
   GuiInvokationResult invokeScripting(String cmd, GuiUserEvent event) {
      GuiInvokationResult result = new GuiInvokationResult("BeanShell: " + cmd);
      try {
         NameSpace ns = context.getNameSpace();
         Object ret = ns.invokeMethod(cmd, new Object[] {event}, context);
         result.done = true;
         result.returnValue = ret;
         return result;
      }
      catch(Exception ex) {
         logger.error(ex);
         result = new GuiInvokationResult(ex);
         return result;
      }
   }
}
