@echo off

@rem save current directory
@set OLDDIR=%CD%

echo Building WR dependency tree from %WR_CC_PATH%\parent...
cd %WR_CC_PATH%\parent
call mvn dependency:tree -s %MAVEN_SETTINGS_FILE% > %OLDDIR%\dependencies.txt

echo Wrote dependency tree to %OLDDIR%\dependencies.txt.
@set MYERROR=%ERRORLEVEL%

chdir /d %OLDDIR%
EXIT /B %MYERROR%