@echo off
set TNS_ADMIN=\\yemaya\Apps\ora102\NETWORK\ADMIN
REM PATH=C:\path\to\putty\directory;%PATH%
REM **
REM **
REM **      Developer: Jennifer Agana
REM **           Date: 09-20-2012
REM **
REM **    NOTE: For 64 bit Windows OS, change editv32 to editv64.
REM **
REM ** This process reads an Application Manager text file and copies the files to the 
REM ** appropriate unix folders for use by CM Support. Once this process is complete
REM ** CM Support will need to log into the appropriate AM instance and run the 
REM ** COM_PROC.... process flow to automatically import objects. 
REM **
REM ** setlocal is essential to make variables available over all funtcions..
setLOCAL ENABLEDELAYEDEXPANSION
goto begin
REM **
REM ** sub program builds the data file for sftp
REM **
:createObjectsFile
for /f "tokens=1,2,3,4 delims=: " %%a in ( "%current_line%" ) do (

 if "%%a" == "version" (
    if "%%d" == "end" (
     set _versioncopy=end      
     echo quit >>  %jiraissue%_sftp.txt        
     echo quit >>  %jiraissue%_wh_sftp.txt
    )
 ) 

 if "!_versioncopy!" == "start" (    
    for /f "tokens=1,2,3,4 delims=\" %%g in ( "%current_line%" ) do ( 
       echo put dvlp\%%a %%h              >>  %jiraissue%_sftp.txt  
       echo chmod 777 %%h                 >>  %jiraissue%_sftp.txt        
       echo put dvlp\%%a %%h              >>  %jiraissue%_wh_sftp.txt  
       echo chmod 777 %%h                 >>  %jiraissue%_wh_sftp.txt              
    )
 )

 if "%%a" == "version" (
   if "%%d" == "start" (
       set _versioncopy=start
       echo rmdir /home/appworx-wd/%_dbinstance%/common/tmp/jira/%jiraissue%  >> %jiraissue%_sftp.txt 
       echo rmdir /home/appworx-wd/%_dbinstance%/common/tmp/jira/%jiraissue%  >> %jiraissue%_wh_sftp.txt       
       
       echo mkdir /home/appworx-wd/%_dbinstance%/common/tmp/jira/%jiraissue%  >> %jiraissue%_sftp.txt  
       echo mkdir /home/appworx-wd/%_dbinstance%/common/tmp/jira/%jiraissue%  >> %jiraissue%_wh_sftp.txt    
       
       echo chmod 777 /home/appworx-wd/%_dbinstance%/common/tmp/jira/%jiraissue%  >> %jiraissue%_sftp.txt   
       echo chmod 777 /home/appworx-wd/%_dbinstance%/common/tmp/jira/%jiraissue%  >> %jiraissue%_wh_sftp.txt       
       
       echo cd /home/appworx-wd/%_dbinstance%/common/tmp/jira/%jiraissue%  >> %jiraissue%_sftp.txt    
       echo cd /home/appworx-wd/%_dbinstance%/common/tmp/jira/%jiraissue%  >> %jiraissue%_wh_sftp.txt  
       
       echo put %jiraissue%.txt %jiraissue%.txt  >>  %jiraissue%_sftp.txt                
       echo put %jiraissue%.txt %jiraissue%.txt  >>  %jiraissue%_wh_sftp.txt  
       
       echo chmod 777 %jiraissue%.txt %jiraissue%.txt >>  %jiraissue%_sftp.txt
       echo chmod 777 %jiraissue%.txt %jiraissue%.txt >>  %jiraissue%_wh_sftp.txt
   )
 )

)
goto :EOF
:begin
REM **
REM ** Main processing starts here
REM ** Initialize variables
REM **
set _password=
set _userid=
set _verpath=
set _filename=
set _dbinstance=
set _drive=
set _versioncopy=
set yninput=
set jiraissue=
set working_directory=
set copy_to_server=
REM set gemini_app=tas;gems;fast
REM set versionCtrlEnv=
REM **
REM ** Get User Inputs
REM **
echo.*****************************
echo.enter all input in lower case
echo.*****************************
REM **
REM ** set the version path
REM ** 
set /p yninput=is version control folder c:\cmversioncontrol (y/n)?
if "%yninput%" == "y" ( 
    ( c:
      set _verpath=c:\cmversioncontrol)
) else set /p _verpath=enter path of version control folder(Like x:\cmversioncontrol)
for /f "tokens=1 delims=\ " %%a in ( "%_verpath%" ) do ( set _drive=%%a)
%_drive%
cd %_verpath%
set /p _dbinstance=enter name of am instance(dvlp/dvlpu/uat/uatu/prod)
if "%_dbinstance%" == "dvlp"  ( set copy_to_server=trusty)
if "%_dbinstance%" == "dvlpu" ( set copy_to_server=trusty)
if "%_dbinstance%" == "uat"   ( set copy_to_server=truly) 
if "%_dbinstance%" == "uatu"  ( set copy_to_server=truly)
if "%_dbinstance%" == "prod"  ( set copy_to_server=thor)
set /p jiraissue=enter name of jira issue(Like rfturner-12)
set /p _userid=enter your %copy_to_server% user id:
echo enter password
editv32 -m _password
REM **
REM ** set the AM working directory and get into the directory
REM ** 
for /f "tokens=1 delims=- " %%a in ( "%jiraissue%" ) do ( set _projpath=%%a)
if "%_projpath%" == "absfam"    ( set working_directory=fast)
if "%_projpath%" == "absgam"    ( set working_directory=gems)
if "%_projpath%" == "aisdw"  ( set /p working_directory="Enter name of AIS app(tas/dwhouse)" )
if "%_projpath%" == "faam"      ( set working_directory=urpt)
if "%_projpath%" == "itam"      ( set working_directory=common)
if "%_projpath%" == "oasam"     ( set working_directory=oasis)
if "%_projpath%" == "oasbdmsam" ( set working_directory=oasis)
if "%_projpath%" == "roam"      ( set working_directory=repo)
cd %_projpath%
REM **
REM ** production applications for tas, fast and gems use the gemini server
REM **
if "%_dbinstance%" == "prod" (
  if "%working_directory%" == "fast" (set copy_to_server=gemini)
  if "%working_directory%" == "gems" (set copy_to_server=gemini)
  if "%working_directory%" == "tas"  (set copy_to_server=gemini)  
)
REM **
REM ** These set commands convert text from lower to upper text
REM **
set ujiraissue=%jiraissue%
IF %ujiraissue%==' ' goto endupper
set ujiraissue=%ujiraissue:a=A%
set ujiraissue=%ujiraissue:b=B%
set ujiraissue=%ujiraissue:c=C%
set ujiraissue=%ujiraissue:d=D%
set ujiraissue=%ujiraissue:e=E%
set ujiraissue=%ujiraissue:f=F%
set ujiraissue=%ujiraissue:g=G%
set ujiraissue=%ujiraissue:h=H%
set ujiraissue=%ujiraissue:i=I%
set ujiraissue=%ujiraissue:j=J%
set ujiraissue=%ujiraissue:k=K%
set ujiraissue=%ujiraissue:l=L%
set ujiraissue=%ujiraissue:m=M%
set ujiraissue=%ujiraissue:n=N%
set ujiraissue=%ujiraissue:o=O%
set ujiraissue=%ujiraissue:p=P%
set ujiraissue=%ujiraissue:q=Q%
set ujiraissue=%ujiraissue:r=R%
set ujiraissue=%ujiraissue:s=S%
set ujiraissue=%ujiraissue:t=T%
set ujiraissue=%ujiraissue:u=U%
set ujiraissue=%ujiraissue:v=V%
set ujiraissue=%ujiraissue:w=W%
set ujiraissue=%ujiraissue:x=X%
set ujiraissue=%ujiraissue:y=Y%
set ujiraissue=%ujiraissue:z=Z%
:endupper
REM **
REM ** Give an error if the issue text file does not exist
REM **
if not exist %ujiraissue%.txt (
 echo.Could not find %ujiraissue%.txt in %cd%
 goto theend
)
REM **
REM ** Delete log files
REM **
if exist %jiraissue%_sftp.txt (
 echo.deleting sftp file, %jiraissue%_sftp.txt
 del %jiraissue%_sftp.txt
)
if exist %jiraissue%_wh_sftp.txt (
 echo.deleting sftp file, %jiraissue%_wh_sftp.txt
 del %jiraissue%_wh_sftp.txt
)
REM **
REM ** Read through the text file and create the sftp objectfiles
REM **
for /f "tokens=*" %%i in (%ujiraissue%.txt) do ( 
  set current_line=
  set current_line=%%i
  call:createObjectsFile 
)
REM **
REM ** send code to the appropriate server
REM **
psftp -b %_verpath%\%_projpath%\%jiraissue%_sftp.txt -be -l %_userid% -pw %_password% %copy_to_server%.cfr.usf.edu
REM **
REM ** UAT code also goes to wh5000
REM **
if "%copy_to_server%" == "truly" (
 set copy_to_server=wh5000
 psftp -b %_verpath%\%_projpath%\%jiraissue%_wh_sftp.txt -be -l %_userid% -pw %_password% %copy_to_server%.cfr.usf.edu 
)
REM **
REM ** Delete log files
REM **
REM if exist %jiraissue%_sftp.txt (
REM  echo.deleting sftp file, %jiraissue%_sftp.txt
REM  del %jiraissue%_sftp.txt
REM )
REM if exist %jiraissue%_wh_sftp.txt (
REM  echo.deleting sftp file, %jiraissue%_wh_sftp.txt
REM  del %jiraissue%_wh_sftp.txt
REM )
:theend
set _password=
set _userid=
set _verpath=
set _filename=
set _dbinstance=
set _drive=
set _versioncopy=
set yninput=
set jiraissue=
set working_directory=
set copy_to_server=