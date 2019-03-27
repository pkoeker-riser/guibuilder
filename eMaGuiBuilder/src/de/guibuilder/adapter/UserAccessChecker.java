package de.guibuilder.adapter;

public interface UserAccessChecker {
   public boolean checkAccess(String controllerClass, String methodName);
}
