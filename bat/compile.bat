@echo off
REM ** 
REM **
REM ** Developer: Venu Bangalore
REM **
REM Developer: Venu Bangalroe
set GRAILS_HOME=Z:\grails\grails-1.3.4
PATH=%GRAILS_HOME%\BIN;%PATH%;Z:\svn\requiredfiles\CM;
SETLOCAL ENABLEDELAYEDEXPANSION
set home_drive=%cd%
cd ..\apps\svnproj
set /p env=Enter environment(Like devl/test/prod)
if "%env%" == "devl" (
  grails dev war
  echo %cd%
  copy %cd%\target\svnProj.war %cd%\..\..\..\svnProjDev.war
  del /s  /q %cd%\target\svnProj.war
  rmdir /s  /q %cd%\target\classes
)

if "%env%" == "test" (
  grails test war
  echo %cd%
  copy %cd%\target\svnProj.war %cd%\..\..\..\svnProjTest.war
  del /s  /q %cd%\target\svnProj.war
  rmdir /s  /q %cd%\target\classes
)

if "%env%" == "prod" (
  grails prod war
  echo %cd%
  copy %cd%\target\svnProj.war %cd%\..\..\..\svnProj.war
  del /s  /q %cd%\target\svnProj.war
  rmdir /s  /q %cd%\target\classes
)
cd %home_drive%

