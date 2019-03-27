SET classpath=.;..\lib\GLUE.jar;..\lib\pnuts.zip;..\lib\bsh.jar;..\lib\jdataset.jar
REM javadoc.exe -source 1.4 -use -author -version -private -windowtitle "GuiBuilder" -d "C:\GuiBuilder\docs" de.guibuilder.adapter de.guibuilder.design de.guibuilder.framework de.guibuilder.framework.event de.guibuilder.server
javadoc.exe -source 1.4 -use -author -version -windowtitle "GuiBuilder" -d "C:\GuiBuilder\docs" de.guibuilder.adapter de.guibuilder.design de.guibuilder.framework de.guibuilder.framework.event de.guibuilder.server
pause
