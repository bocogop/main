@echo off

@rem save current directory
@set OLDDIR=%CD%

cd %WR_PROJECT_HOME%
call mvn clean -s %MAVEN_SETTINGS_FILE%
@set MYERROR=%ERRORLEVEL%

chdir /d %OLDDIR%
EXIT /B %MYERROR%