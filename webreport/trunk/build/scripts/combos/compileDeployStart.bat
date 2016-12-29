call compile.bat
IF errorlevel 1 GOTO :eof

call deploy.bat
IF errorlevel 1 GOTO :eof

call startserver.bat
