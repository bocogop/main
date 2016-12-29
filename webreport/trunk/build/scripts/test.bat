@echo off

@rem save current directory
@set OLDDIR=%CD%

cd %WR_CC_PATH%\parent
call mvn test site -s %MAVEN_SETTINGS_FILE%

@set MYERROR=%ERRORLEVEL%

chdir /d %OLDDIR%
EXIT /B %MYERROR%