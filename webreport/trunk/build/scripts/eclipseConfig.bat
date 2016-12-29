@echo off

@rem save current directory
@set OLDDIR=%CD%

echo Generating Eclipse configuration files...
cd %WR_CC_PATH%\parent
call mvn eclipse:clean eclipse:eclipse -U -s %MAVEN_SETTINGS_FILE%
@set MYERROR=%ERRORLEVEL%

chdir /d %OLDDIR%
EXIT /B %MYERROR%