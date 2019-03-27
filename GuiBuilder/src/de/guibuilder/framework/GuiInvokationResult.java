package de.guibuilder.framework;

/**
 * Rückgabewert eine vom Framework ausgeführten Methode 
 * @author peter
 */
class GuiInvokationResult {
	
		enum ReturnStatus {
			UNKNOWN,
			OK,
			CANCEL,
			ERROR
		}
		String label;
		boolean done; // Wenn true, dann wurde Methode erfolgreich ausgeführt; wenn false, dann Fehler
		boolean isSync = true; // false wenn mit SwingWorker aufgerufen
		ReturnStatus status = ReturnStatus.UNKNOWN;
		Object returnValue;
		Exception exception;

    GuiInvokationResult(String label, ReturnStatus status) {
      this.label = label;
      this.status = status;
      if (status != ReturnStatus.ERROR) {
        done = true;
      }
    }
		GuiInvokationResult(String label) {
			this.label = label;
		}
		/**
		 * Erzeugt einen Result mit einem Fehler
		 * @param ex
		 */
		GuiInvokationResult(Exception ex) {
			this.exception = ex;
			this.status = ReturnStatus.ERROR;
			//this.done = true;
		}
}

