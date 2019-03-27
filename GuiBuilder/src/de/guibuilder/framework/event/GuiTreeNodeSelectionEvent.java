package de.guibuilder.framework.event;

import de.guibuilder.framework.GuiTree;
import de.guibuilder.framework.GuiTreeNode;
import de.guibuilder.framework.GuiWindow;

public final class GuiTreeNodeSelectionEvent extends GuiUserEvent {
  public transient GuiTree component;
  public transient GuiTreeNode node;

  public GuiTreeNodeSelectionEvent(GuiWindow win, GuiTree comp, GuiTreeNode node) {
    super(win, comp);
    component = comp;
    this.node = node;
  }
  public final int getEventType() {
    return TREE;
  }
  public GuiTree getComponent() {
     return this.component;
  }
  public GuiTreeNode getNode() {
     return this.node;
  }
}