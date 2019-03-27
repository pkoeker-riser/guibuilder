package de.guibuilder.framework;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import layout.TableLayout;
import layout.TableLayoutConstants;

/**
 * Implementierung eines unsichtbaren Containers.
 */
public class GuiPanel extends GuiContainer {
  // Attributes
  private int layoutManager = GRIDBAG;
  /**
   * Delegation
   */
  protected JPanel component;
  /**
   * DesktopPane
   */
  private GuiDesktop desktop;
  /**
   * Dieses Attribut wird von GuiTree.addPanel gesetzt.
   * Der Sinn besteht darin, daß Navigator-Panels, die nicht
   * angezeigt werden, bei getRootPane eine spezielle
   * Behandlung bedürfen.
   * @see #getRootPane
   * @see GuiTree#addPanel
   */
  private GuiTree myTree;

  // Constructors
  /**
   * Erzeugt ein Panel mit GridBagLayout mit dem Default-Namen "panel".
   */
  public GuiPanel() {
    super();
    this.setName("panel");
    this.guiInit();
  }
  /**
   * Erzeugt ein Panel mit einem angegebenen Name.
   */
  public GuiPanel(String label) {
    super(label);
    this.guiInit();
  }
  /**
   * setName; setDoubleBuffered(false); setLayout(new GridBagLayout())
   */
  protected void guiInit() {
    component.setDoubleBuffered(false);
    component.setLayout(new GridBagLayout());
  }
  // Methods
  @Override
	public String getTag() {
    return "Panel";
  }
  /**
   * @see GuiTree#addPanel
   */
  void setMyTree(GuiTree tree) {
    myTree = tree;
  }
  GuiTree getMyTree() {
    return myTree;
  }
  /**
   * @see #setLayoutManager
   */
  @Override
	public int getLayoutManager() {
    return layoutManager;
  }
  /**
   * Es wird festgelegt, welchen LayoutManager dieses Panel einsetzen soll.
   * Erlaubte Werte sind die Konstanten aus GuiContainer:
   * <ul>
   * <li>GRIDBAG
   * <li>GRID
   * <li>FLOW
   * <li>BORDER
   * <li>FORM
   * <li>NULL
   * </ul>
   */
  @Override
	public void setLayoutManager(int lm) {
    layoutManager = lm;
    switch (lm) {
      case NULL:
         if (component != null)
            component.setLayout(null);
        break;
      case GRID:
        component.setLayout(new GridLayout(0, 2, 5, 5)); // Rows, Cols, gap
        break;
      case FLOW:
        component.setLayout(new FlowLayout()); // gap
        break;
      case BORDER:
        component.setLayout(new BorderLayout());
        break;
      case SPRING:
      	component.setLayout(new SpringLayout());
      	break;
      case FORM:
      	//component.setLayout(new FormLayout()); // colSpan, rowSpan?
      	break;
      case TABLE:
        double size[][] =
        {{5, TableLayoutConstants.FILL,5, TableLayoutConstants.FILL, 5, TableLayoutConstants.FILL, 5, TableLayoutConstants.FILL, 5, TableLayoutConstants.FILL, 5, TableLayoutConstants.FILL, 5},
         {5, TableLayoutConstants.FILL,5, TableLayoutConstants.FILL, 5, TableLayoutConstants.FILL, 5, TableLayoutConstants.FILL, 5, TableLayoutConstants.FILL, 5, TableLayoutConstants.FILL, 5}};
        component.setLayout(new TableLayout(size));
        break;
      default:
      	throw new IllegalArgumentException("GuiPanel#setLayoutManager: Index out of Range "+lm);
    }
  }
  /**
   * Setzt die Anzahl Spalten für das GridLayout.
   * Wirft eine IllegalStateException, wenn dem Panel kein
   * GridLayout als LayoutManager zugewiesen wurde.
   */
  public void setGridColumns(int cols) {
    if (this.getJComponent().getLayout() instanceof GridLayout) {
      GridLayout grid = (GridLayout)this.getJComponent().getLayout();
      grid.setColumns(cols);
    } else {
      throw new IllegalStateException("LayoutManager isn't GridLayout!");
    }
  }
  /**
   * Liefert JPanel
   */
  @Override
	public JComponent getJComponent() {
    if (component == null) {
      if (GuiUtil.isAPI() == true) {
        component = new JPanel();
      } else {
        component = new GuiPanelImpl(this);
      }
    }
    return component;
  }
  @Override
	final void removeAll() {
     if (component != null) {
        component.removeAll();
     }
    if (desktop != null) {
      desktop.dispose();
      desktop = null;
    }
  }
  /**
   * Selektiert den Inhalt aller Textkomponenten dieses Panels
   * @deprecated
   */
  public final void selectTextMembers(boolean b) {
    for (Iterator<Map.Entry<String, GuiMember>> i = getMembers().entrySet().iterator(); i.hasNext();) {
      GuiMember member = (GuiMember)i.next();
      if (member instanceof GuiText) {
        GuiText txt = (GuiText)member;
        if (b == true) {
          txt.getTextField().setSelectionStart(0);
          txt.getTextField().setSelectionEnd(txt.getText().length());
        } else {
          txt.getTextField().setSelectionEnd(0);
        }
      /*
      } else if (member.getGuiType() == GuiMember.GUI_CONTAINER) {
        ((GuiPanel)member).selectTextMembers(b);
      */
      }
    }
  }
  // für Desktop
  /**
   * Liefert den Desktop für InternalFrames oder null.
   */
  public final GuiDesktop getDesktop() {
    return desktop;
  }
  /**
   * Setzt den Desktop für Internal Frames.
   */
  public final void setDesktop(GuiDesktop d) {
    this.desktop = d;
    component.setLayout(new BorderLayout());
    this.add(d);
  }
}