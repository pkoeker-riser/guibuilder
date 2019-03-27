package de.guibuilder.framework;

import java.awt.Container;

import javax.swing.JPanel;


/**
 * Wird nur zur Design Time verwendet.
 */
public final class GuiPanelImpl extends JPanel {
  // Attributes
  //private static boolean showGrid;
  //public static MemberPropAble memberPropController;
  private transient GuiPanel myGuiPanel;
  // Constructor
  public GuiPanelImpl(GuiPanel panel) {
    super();
    myGuiPanel = panel;
  }
  // Methods
  /**
   * Liefert auch dann einen Parent, wenn Navigator-Panel.<p>
   * @see GuiComponent#lostFocus(FocusEvent)
   */
  public final Container getParent() {
    Container c = super.getParent();
    if (c != null) {
      return c;
    }
    if (myGuiPanel != null && myGuiPanel.getMyTree() != null) {
      c = myGuiPanel.getMyTree().getJComponent();
    }
    return c;
  }
  final GuiPanel getGuiParent() {
    return myGuiPanel;
  }
//  public static void setShowGrid(boolean b) {
//    showGrid = b;
//  }
//  public static boolean getShowGrid() {
//    return showGrid;
//  }
  /*
  public final void paint (Graphics g) {
    super.paint (g);
    if (showGrid == true && ((GuiRootPane)this.getRootPane()).hasDesignMode() == true) {
        g.setColor(Color.red);
        {
        for (int i = 0; i < this.getComponentCount(); i++) {
          Component comp = this.getComponent(i);
          Rectangle r = comp.getBounds();
          if (comp instanceof JComponent) {
            JComponent jcomp = (JComponent)comp;
            Insets ins = jcomp.getInsets();
            if (ins != null) {
              r = new Rectangle((int)comp.getLocation().getX() + ins.left,
                (int)comp.getLocation().getY() + ins.top,
                comp.getWidth() - ins.left - ins.right,
                comp.getHeight() - ins.top - ins.bottom);
            }
          }
          g.drawRect(r.x, r.y, r.width, r.height);
        }
      }

      // Gitternetz?
      GridBagLayout grid = (GridBagLayout)this.getLayout();
      int d[][] = grid.getLayoutDimensions();
      Point origin = grid.getLayoutOrigin(); // Anfangspunkt des Layouts!!!
      //System.out.println(origin.x+":"+origin.y);
      //System.out.println(this.getName());
      // Spalten *****************
      int c = origin.x;
      for (int j = 0; j < d[0].length; j++) {
        int off = d[0][j];
        c+=off;
        //System.out.print(off+":"+c+" ");
        if (off != 0) {
          g.drawLine(c, 0, c, this.getBounds().height);
        }
      }
      //System.out.println(":");
      // Zeilen ******************
      c = origin.y;
      for (int j = 0; j < d[1].length; j++) {
        int off = d[1][j];
        c+=off;
        //System.out.print(off+":"+c+" ");
        if (off != 0) {
          g.drawLine(0, c, this.getBounds().width ,c);
        }
      }
      //System.out.println(":");
    }
  }
  */
}