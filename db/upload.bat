@echo off

if "%1"=="" goto blank
if "%2"=="" goto blank

set server=bocogop.database.windows.net
set db=bocogop
set user=bcgop

For /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
For /f "tokens=1-2 delims=/:" %%a in ("%TIME%") do (set mytime=%%a%%b)
set staging_table=stg_%mydate%T%mytime%_%~n1
echo Importing to new table "%staging_table%"...

@rem Create the staging table on the server (dropping any existing with the same name if necessary)
sqlcmd.exe -S %server% -d %db% -U %user% -b -P %2 -I -v TableName="%staging_table%" -i scripts/create-staging-table.sql
IF ERRORLEVEL 1 goto err_handler

@rem Copy the CSV rows into the staging table
@rem add this flag to only process the first N rows: -L 5000
bcp %staging_table% in "%1" -S %server% -d %db% -U %user% -P %2 -F 2 -f vr.fmt -e errors.txt -q
IF ERRORLEVEL 1 goto err_handler

@rem Clean up and merge the staged data into the target tables (primarily dbo.Voter)
sqlcmd.exe -S %server% -d %db% -U %user% -b -P %2 -I -v TableName="%staging_table%" -i scripts/transform-data.sql
IF ERRORLEVEL 1 goto err_handler

EXIT /B 0

:blank
echo Please specify both the CSV to import and the database password, e.g.
echo upload.bat VR011_20161206.csv ThePassword$123
EXIT /B 0

:err_handler
echo An error occurred during the execution, please check errors.txt for details.
EXIT /B 1