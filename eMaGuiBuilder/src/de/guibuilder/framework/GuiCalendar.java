/*
 * Created on 06.05.2005
 */
package de.guibuilder.framework;

import java.util.Date;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;

/**
 * @author PKOEKER
 */
public class GuiCalendar extends GuiDate {
    protected JCalendar cal;
    
    public GuiCalendar() {
       this("calendar");
    }
    public GuiCalendar(String label) {
        super(label);
    }
    protected void guiInit() {
    	cal.addCalendarListener(new JCalendar.CalendarListener() {
    		public void dateChanged(ChangeEvent e) {
    			Date d = cal.getDate();
    		   component.setText(format.format(d));
    			if (getText().equals(oldValue) == false) {
    				setModified(true);
    			}
   			// PostProcessor
   			postProc();
   			// OnChange
   			if (isModified()) {
   				if (actionChange != null && getRootPane() != null) {
   					getRootPane().obj_ItemChanged(GuiCalendar.this, actionChange,
   							getValue());
   				}
   			}
   			oldValue = getText(); 
    		}
    	});
      this.format = defaultFormat;
      this.format.setLenient(false);
    }
    public JComponent getJComponent() {
    	super.getJComponent();
        if (this.cal == null) {
            this.cal = new JCalendar();
        }
        return this.cal;
    }
    public final void setValue(Object o) {
    	super.setValue(o);
    	if (o != null) {
	    	try {
	    		Date d = format.parse(o.toString());
	    		this.setValue(d);
	    	} catch (Exception ex) {
	    		System.err.println("GuiCalendar#setValue: "+ex.getMessage());
	    	}
    	}
    }
    public final void setValue(Date d) {
    	super.setValue(d);
      cal.setDate(d);
    }
    public final void setValue(long d) {
    	super.setValue(d);
      cal.setDate(new Date(d));
    }
    public final void setValue(Long d) {
    	super.setValue(d);
      cal.setDate(new Date(d.longValue()));
    }
}
