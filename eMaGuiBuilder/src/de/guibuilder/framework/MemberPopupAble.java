/*
 * Created on 10.11.2003
 */
package de.guibuilder.framework;

/**
 * Erweitert MemberAble um die Fähigkeit ein Kontext-Menü zu haben.
 */
public interface MemberPopupAble extends MemberAble {
	public void setPopupMenu(GuiPopupMenu m);
	public GuiPopupMenu getPopupMenu();
	public void showPopupMenu(int x, int y);
	public void setMsgPopup(String msgPopup);
	public String getMsgPopup();
}
