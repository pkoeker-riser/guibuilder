rem Dient dem Erstellen von SerialUID für serialisierbare Klassen.


@echo off

set CP=.;bin

for %%i in (lib\*.jar) do call appendCP %%i

rem echo Using classpath:  %CP%

rem Get command line arguments and save them in the CMD_LINE_ARGS
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

serialver -classpath %CP% -show  %CMD_LINE_ARGS%

pause
