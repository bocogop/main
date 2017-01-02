@echo off

@rem save current directory
@set OLDDIR=%CD%

cd %DEPLOYMENTS_FOLDER%\..
echo Removing deployments dir
rd /S /Q deployments
del deployments
echo Creating new deployments dir
mkdir deployments
copy %MAVEN_REPO_LOC%\gov\va\wr\wr_ear\%WR_VERSION%\wr_ear-%WR_VERSION%.ear deployments

echo Removing properties dir
rd /S /Q properties
rd /S /Q properties-event
echo Creating new properties dir
mkdir properties
copy %WR_CC_PATH%\properties\sqa_hp\*.properties properties
copy %WR_CC_PATH%\web\src\main\resources\messages_en.properties properties
copy %WR_CC_PATH%\web\src\main\webapp\WEB-INF\log4j\log4j2.xml properties

mkdir properties-event
copy %WR_CC_PATH%\properties\sqa_hp\*.properties properties-event
copy %WR_CC_PATH%\event\src\main\resources\messages_en.properties properties-event
copy %WR_CC_PATH%\event\src\main\webapp\WEB-INF\log4j\log4j2.xml properties-event

echo|set /p="web.version=5.1.0_" > properties\version.properties
echo|set /p="web.version=5.1.0_" > properties-event\version.properties

svnversion -n %WR_CC_PATH% >> properties\version.properties
svnversion -n %WR_CC_PATH% >> properties-event\version.properties

@rem restore current directory
chdir /D %OLDDIR%

echo Deployment complete.