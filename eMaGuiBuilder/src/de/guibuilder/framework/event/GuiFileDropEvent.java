package de.guibuilder.framework.event;

import java.io.File;

import de.guibuilder.framework.GuiMember;
import de.guibuilder.framework.GuiWindow;

public class GuiFileDropEvent extends GuiUserEvent {
  public File[] files;

  public GuiFileDropEvent(GuiWindow win, GuiMember mem, File[] files) {
    super(win, mem);
    this.files = files;
  }

  @Override
  public int getEventType() {
    return FILEDROP;
  }

}
