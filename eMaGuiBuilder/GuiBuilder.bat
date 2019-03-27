@echo off

set CP=.;.\bin

for %%i in (lib\*.jar) do call cp.bat %%i
set CP=%CP%;lib\icons.zip
rem echo Using classpath:  %CP%

rem Get command line arguments and save them in the CMD_LINE_ARGS
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs


java -cp %CP% de.guibuilder.design.GuiMain %CMD_LINE_ARGS%



REM GuiBuilder aus jar-File starten
rem java -ms4M -jar lib/guibuilder.jar %1
REM pause