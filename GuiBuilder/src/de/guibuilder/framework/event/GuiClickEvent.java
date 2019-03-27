/*
 * Created on 01.06.2005
 */
package de.guibuilder.framework.event;

import java.awt.event.MouseEvent;

import de.guibuilder.framework.GuiComponent;
import de.guibuilder.framework.GuiWindow;

/**
 * @author PKOEKER
 */
public class GuiClickEvent extends GuiChangeEvent {
    public MouseEvent mouseEvent;

    public GuiClickEvent(GuiWindow win, GuiComponent comp, Object val,
            MouseEvent me) {
        super(win, comp, val);
        this.mouseEvent = me;
    }

    public final int getEventType() {
        return CLICK;
    }
}