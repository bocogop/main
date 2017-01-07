@echo off

@rem save current directory
@set OLDDIR=%CD%

echo Building Core (including test jar)...
cd %WR_CC_PATH%\core
call mvn install -s %MAVEN_SETTINGS_FILE% -U

echo Building WR from %WR_CC_PATH%\parent...
cd %WR_CC_PATH%\parent
call mvn install -Dmaven.test.skip=true -s %MAVEN_SETTINGS_FILE%
@rem mvn dependency:sources -s %MAVEN_SETTINGS_FILE%
@rem mvn dependency:resolve -Dclassifier=javadoc -s %MAVEN_SETTINGS_FILE%

@set MYERROR=%ERRORLEVEL%

chdir /d %OLDDIR%
EXIT /B %MYERROR%