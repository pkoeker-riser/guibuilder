package de.guibuilder.framework;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.Timer;

import de.guibuilder.framework.event.GuiTimerEvent;
import de.guibuilder.framework.event.GuiUserEvent;

public class GuiTimer {
   private GuiWindow win;
   private int delay;
   private String message;
   private String methodName;
   private Timer t;
   private boolean running;
   private boolean enabled;

   public GuiTimer(final GuiWindow win, final String message, int delay, String method) {
      this.win = win;
      this.delay = delay;
      this.message = message;
      this.methodName = method;
   }

   private void start() {
      if (!enabled) {
         return;
      }
      if(t == null) {
         ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
               Object controller = win.getController();
               if(controller != null) {
                  try {
                     Method m = controller.getClass().getMethod(methodName, GuiUserEvent.class);
                     GuiTimerEvent event = new GuiTimerEvent(win, null, message);
                     m.invoke(controller, event);
                  }
                  catch(Exception ex) {
                     System.err.println(ex.getMessage());
                  }
               }
            }
         };
         t = new Timer(delay, taskPerformer);
         t.setInitialDelay(delay);
         t.setActionCommand(methodName);
      }
      t.start();
   }

   /**
    * Beim schließen des Container Timer stoppen
    */
   private void stop() {
      if(t != null)
         t.stop();
   }

   public void setActive(boolean b) {
      if(b) {
         this.start();
      }
      else {
         this.stop();
      }
   }

   public boolean isActive() {
      if(t == null) {
         return false;
      }
      else {
         return t.isRunning();
      }
   }
   
   public void setEnabled(boolean b) {
      this.enabled = b;
   }
   
   public boolean isEnabled() {
      return enabled;
   }

   /**
    * @formatter:off
    * Window (de)iconified
    * @param b
    * @formatter:on
    */
   void setTempDisabled(boolean b) {
      if(!b && running) {
         this.start();
         //System.out.println("Started!");
      }
      else if(b) {
         running = this.isActive();
         this.stop();
         //System.out.println("Stoped!");
      }
   }

   public String getActionCommand() {
      if(t != null) {
         return t.getActionCommand();
      }
      else {
         return this.methodName;
      }
   }

   public void setActionCommand(String s) {
      this.methodName = s;
      if(t != null) {
         t.setActionCommand(s);
         t.restart();
      }
   }

   public void setDelay(int d) {
      this.delay = d;
      if(t != null) {
         t.setDelay(d);
         t.setInitialDelay(delay);
         t.restart();
      }
   }

   public int getDelay() {
      return this.delay;
   }
}
