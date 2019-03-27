package de.guibuilder.framework;

import javax.swing.JSplitPane;

@SuppressWarnings("serial")
public class GuiSplitImpl extends JSplitPane {
  private int fixedLocation = -1;
  private boolean valid = true;
  
  public GuiSplitImpl(int orientation) {
    super(orientation);
  }
  
  void setFixedDividerLocation(int location) {
     //System.out.println(this.getName() + " setFix=" + location);
     this.fixedLocation = location;
     setDividerLocationImpl(location);
     //this.setEnabled(false); // Dann kann man den Divider nicht mehr verschieben!
  }
  
  void setDividerLocationImpl(int location) {
     //System.out.println(this.getName() + " locImpl=" + location +"\n");
     super.setDividerLocation(location);
  }
  
  public void setDividerLocation(int location) {
     int prev = this.getDividerLocation();
     //System.out.println(this.getName() + " prev=" + prev + " loc=" + location + " fix=" + fixedLocation);
     if (location == 1)
        return;
    if (fixedLocation != -1 && valid && prev != fixedLocation) {
       //System.out.println("fixed: " + fixedLocation);
       super.setDividerLocation(fixedLocation);      
    } else if (!valid && prev != location) {
       //System.out.println("loc: " + location);
       super.setDividerLocation(location);      
    }
    //System.out.println("endSetDiv\n");
  }
  
  public void validateTree() {
    super.validateTree();
    valid = false;
  }
}
