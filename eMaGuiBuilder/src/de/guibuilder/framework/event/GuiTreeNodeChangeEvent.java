package de.guibuilder.framework.event;

import de.guibuilder.framework.GuiTree;
import de.guibuilder.framework.GuiTreeNode;
import de.guibuilder.framework.GuiWindow;

public final class GuiTreeNodeChangeEvent extends GuiUserEvent {
  public transient GuiTree component;
  public transient GuiTreeNode node;
  public String value;

  public GuiTreeNodeChangeEvent(GuiWindow win, GuiTree comp, GuiTreeNode node) {
    super(win, comp);
    this.component = comp;
    this.node = node;
    this.value = node.getTitle();
  }
  public final int getEventType() {
    return NODE_CHANGE;
  }
  public GuiTree getComponent() {
     return this.component;
  }
  public GuiTreeNode getNode() {
     return this.node;
  }
  public String getValue() {
     return this.value;
  }
}