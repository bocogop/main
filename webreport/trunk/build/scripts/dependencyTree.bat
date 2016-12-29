@echo off

@rem save current directory
@set OLDDIR=%CD%

echo Building WR dependency tree from %WR_PROJECT_HOME%...
cd %WR_PROJECT_HOME%
call mvn dependency:tree -s %MAVEN_SETTINGS_FILE% > %OLDDIR%\dependencies.txt

echo Wrote dependency tree to %OLDDIR%\dependencies.txt.
@set MYERROR=%ERRORLEVEL%

chdir /d %OLDDIR%
EXIT /B %MYERROR%