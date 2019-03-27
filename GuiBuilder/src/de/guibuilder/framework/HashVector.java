package de.guibuilder.framework;

import java.util.*;

/**
 * Menge der Daten haltenden Komponenten zu einem Container. <br>
 * Die Komponenten können sowohl über ihren Namen als auch in der Reihenfolge
 * ihrer Anordnung angesprochen werden.
 * 
 * @see GuiContainer
 */
final class HashVector {
  // Attribute
   private LinkedHashMap<String, GuiMember> members = new LinkedHashMap<String, GuiMember>();
   private String parent;
   // Constructor
   HashVector(String parent) {
     this.parent = parent;
   }
   // Methods
   void addMember(GuiMember member) {
     // prüfung auf fehlende Namen
      if (GuiUtil.isAPI() == true) {
         if (member.getName().length() == 0) {
           String msg = "addMember() Missing memberName: " + this.parent +"/"+ member.getClass().getName();
           System.err.println(msg);
         }
      }
      // prüfen auf doppelte Namen
      if (members.put(member.getName(), member) != null) {
         System.err.println("Duplicate memberName: " + this.parent+"/"+member.getName() 
               + "[" + member.getLabel() + ":" + member.getTag() + "]");
      }
   }

   /**
    * @return true, wenn erfolgreich gelöscht.
    */
   boolean removeMember(GuiMember member) {
      boolean ret = true;
      Object o = members.remove(member.getName());
      if (o == null)
         ret = false;

      return ret;
   }

   /**
    * Liefert einen Member unter den angegebene Name; wirft eine
    * IllegalArgumentException, wenn Member fehlt.
    */
   GuiMember getMember(String name, GuiContainer cont, boolean action) {
      GuiMember comp = null;
      GuiContainer container = cont;
      int cnt = 0;
      if (name.indexOf(".") == -1) {
         // keine Punkte drin: Aus HashMap entnehmen.
         comp = members.get(name);
         if (comp == null) {
            throw new IllegalArgumentException("Missing Member: " + name);
         }
         return comp;
      }
      // Sind Punkte drin: Container abarbeiten
      StringTokenizer tokens = new StringTokenizer(name, ".");
      int anz = tokens.countTokens(); // Anzahl Tokens
      String tok;
      while (tokens.hasMoreTokens()) {
         tok = tokens.nextToken();
         cnt++;
         if (cnt < anz) {
            // Rekursiv Container-Schachtelung abarbeiten.
            container = container.getContainer(tok);
         } else {
            if (action) {
               comp = container.getAction(tok);
            } else {
               comp = container.getMember(tok);
            }
            return comp;
         }
      }
      return comp;
   }

   Iterator<GuiMember> members() {
      return members.values().iterator();
   }

   Iterator<String> memberNames() {
      return members.keySet().iterator();
   }

   LinkedHashMap<String, GuiMember> getMembers() {
      return members;
   }

   /*
    * ArrayList getMemberNames() { return memberNames; }
    */
   int size() {
      return members.size();
   }
   public String toString() {
     if (this.members == null) {
       return "[null]";
     } else {
       return members.toString();
     }
   }
}

