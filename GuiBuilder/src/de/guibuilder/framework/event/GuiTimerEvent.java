package de.guibuilder.framework.event;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiWindow;

public class GuiTimerEvent extends GuiUserEvent {
   public String message;
   
   public GuiTimerEvent(GuiWindow win, GuiMember mem, String message) {
      super(win, mem);
      this.message = message;
   }

   @Override
   public int getEventType() {
      return TIMER;
   }
}
