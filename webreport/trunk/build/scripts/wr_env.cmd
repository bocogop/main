@echo off
@rem Dir containing other shared scripts
@set CURRENT_DIR=%~dp0

@rem @set WR_INCLUDE_BUILD_NUMBER=true

@set WR_VERSION=1.0

@set BCGOP_ROOT=C:\dev\code\bocogop\main
@set SOFTWARE_ROOT=%BCGOP_ROOT%\software

@set WR_CC_PATH=%BCGOP_ROOT%\webreport\trunk

@set SERVER_HOME=%SOFTWARE_ROOT%\apache-tomcat-8.5.9\
@set DEPLOYMENTS_FOLDER=%SERVER_BASE_DIR%\webapps

@set JAVA_HOME=%SOFTWARE_ROOT%\jdk1.8.0

@set M2_HOME=%SOFTWARE_ROOT%\apache-maven-3.3.9
@set MAVEN_REPO_LOC=%BCGOP_ROOT%\maven-repo

@set MAVEN_SETTINGS_FILE=%WR_CC_PATH%\build\settings_build.xml
@set MAVEN_OPTS=-Xmx512m

@set PATH=%SERVER_HOME%\bin;%SOFTWARE_ROOT%\bin;%CURRENT_DIR%;%CURRENT_DIR%/combos;%JAVA_HOME%\bin;%M2_HOME%\bin;%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem

@cd %WR_CC_PATH%\parent

title WR