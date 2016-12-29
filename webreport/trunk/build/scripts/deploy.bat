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
rd /S /Q properties-kiosk
echo Creating new properties dir
mkdir properties
copy %WR_CC_PATH%\properties\sqa_hp\*.properties properties
copy %WR_CC_PATH%\web\src\main\resources\messages_en.properties properties
copy %WR_CC_PATH%\web\src\main\webapp\WEB-INF\log4j\log4j2.xml properties

mkdir properties-kiosk
copy %WR_CC_PATH%\properties\sqa_hp\*.properties properties-kiosk
copy %WR_CC_PATH%\kiosk\src\main\resources\messages_en.properties properties-kiosk
copy %WR_CC_PATH%\kiosk\src\main\webapp\WEB-INF\log4j\log4j2.xml properties-kiosk

echo|set /p="web.version=5.1.0_" > properties\version.properties
echo|set /p="web.version=5.1.0_" > properties-kiosk\version.properties

svnversion -n %WR_CC_PATH% >> properties\version.properties
svnversion -n %WR_CC_PATH% >> properties-kiosk\version.properties

@rem restore current directory
chdir /D %OLDDIR%

echo Deployment complete.