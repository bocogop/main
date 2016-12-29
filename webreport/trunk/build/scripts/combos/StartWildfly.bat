

@rem save current directory
@rem set OLDDIR=%CD%

call %~dp0\..\wr_env_sqahp.cmd

@rem svn up %WR_ROOT%

net stop Apache2.4
net start Apache2.4

@rem call %~dp0\cleancompile.bat

taskkill -im java.exe /F
taskkill -im java.exe /F
TIMEOUT /T 10
@rem call %~dp0\..\deploy.bat

call %~dp0\..\startserver.bat

@rem restore current directory
@rem chdir /D %OLDDIR%

echo Deployment complete.