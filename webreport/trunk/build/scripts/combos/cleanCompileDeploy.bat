call clean.bat
IF errorlevel 1 GOTO :eof

call compile.bat
IF errorlevel 1 GOTO :eof

call deploy.bat