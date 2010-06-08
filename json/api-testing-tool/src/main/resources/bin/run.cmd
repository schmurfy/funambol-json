@echo off

setlocal

rem
rem Copyright (C) 2007 Funambol, Inc.  All rights reserved.
rem
set CMD_HOME="%~d0%~p0.."

cd %CMD_HOME%\lib
set CLASSPATH=config
for %%i in (*.jar) do call :append %%i
goto okClasspath
:append
set CLASSPATH=%CLASSPATH%;lib/%*
goto :eof

:okClasspath

cd ..
java -cp "%CLASSPATH%" com.funambol.json.api.JsonTestAPI

endlocal
