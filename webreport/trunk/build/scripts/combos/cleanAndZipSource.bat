@echo off
setlocal

call clean.bat
IF errorlevel 1 GOTO :eof

for %%I in ("%WR_CC_PATH%\..") do set "WR_CC_PARENT=%%~fI"
for %%f in (%WR_CC_PATH%) do set foldername=%%~nxf

del /F /Q %WR_CC_PARENT%\%foldername%.zip

rd /S /Q %WR_CC_PATH%\deploy\wildfly\standalone\data
rd /S /Q %WR_CC_PATH%\deploy\wildfly\standalone\deployments
rd /S /Q %WR_CC_PATH%\deploy\wildfly\standalone\log
rd /S /Q %WR_CC_PATH%\deploy\wildfly\standalone\tmp

7z a ^
-r %WR_CC_PARENT%\%foldername%.zip %WR_CC_PATH% ^
-xr!*.fpr ^
-xr!*.log ^
-xr!target ^
-xr!target-eclipse

echo Zip file %WR_CC_PARENT%\%foldername%.zip created.
