@echo off

REM ---------------------------------------------------------------------------
REM  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
REM
REM  WSO2 Inc. licenses this file to you under the Apache License,
REM  Version 2.0 (the "License"); you may not use this file except
REM  in compliance with the License.
REM  You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM  Unless required by applicable law or agreed to in writing,
REM  software distributed under the License is distributed on an
REM  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM  KIND, either express or implied. See the License for the
REM  specific language governing permissions and limitations
REM  under the License.
REM ---------------------------------------------------------------------------

SetLocal EnableDelayedExpansion
set CURRENT_PATH=%~sdp0
set dist=false
set update=false
set build=false
set RUN_BALLERINA=true
set FILE_PATH=%CURRENT_PATH%..\distributions\ballerina-version
set JAVA_CMD=java
if "%1" == "dist" set dist=true
if "%~2" == "dist" set dist=true
if "%1" == "update" set dist=true
if "%~2" == "update" (
    if "%1" == "dist" (
        set dist=true
    )
)
if "%1" == "build" set dist=true
if "%1" == "update" set update=true
if "%1" == "build" set build=true
SetLocal EnableDelayedExpansion

if exist %CURRENT_PATH%..\dependencies\jdk-21.0.5+11-jre (
   set JAVA_CMD=%CURRENT_PATH%..\dependencies\jdk-21.0.5+11-jre\bin\java
) else (
    if exist %CURRENT_PATH%..\dependencies\jdk-11.0.18+10-jre (
       set JAVA_CMD=%CURRENT_PATH%..\dependencies\jdk-11.0.18+10-jre\bin\java
    ) else (
        if exist %CURRENT_PATH%..\dependencies\jdk-11.0.18+10-jre (
           set JAVA_CMD=%CURRENT_PATH%..\dependencies\jdk-11.0.18+10-jre\bin\java
        ) else (
            if exist %CURRENT_PATH%..\dependencies\jdk-11.0.15+10-jre (
               set JAVA_CMD=%CURRENT_PATH%..\dependencies\jdk-11.0.15+10-jre\bin\java
            ) else (
                if exist %CURRENT_PATH%..\dependencies\jdk-11.0.8+10-jre (
                   set JAVA_CMD=%CURRENT_PATH%..\dependencies\jdk-11.0.8+10-jre\bin\java
                ) else (
                    if exist %CURRENT_PATH%..\dependencies\jdk8u332-b09-jre (
                       set JAVA_CMD=%CURRENT_PATH%..\dependencies\jdk8u332-b09-jre\bin\java
                    ) else (
                        if exist %CURRENT_PATH%..\dependencies\jdk8u265-b01-jre (
                           set JAVA_CMD=%CURRENT_PATH%..\dependencies\jdk8u265-b01-jre\bin\java
                        ) else (
                            if exist %CURRENT_PATH%..\dependencies\jdk8u202-b08-jre (
                               set JAVA_CMD=%CURRENT_PATH%..\dependencies\jdk8u202-b08-jre\bin\java
                            ) else (
                                if not exist "%JAVA_HOME%" (
                                   echo Compatible JRE not found. Please follow the instructions in ^<BALLERINA_HOME^>\INSTALL.txt to install and setup Ballerina.
                                   exit /b
                                )
                            )
                        )
                    )
                )
            )
        )
    )
)

if "%dist%" == "true" (
   if not "%build%" == "true" (
       set RUN_BALLERINA=false;
   )
   if "%build%" == "true" (
        %JAVA_CMD% -jar %CURRENT_PATH%..\lib\ballerina-command-@version@.jar build
   ) else (
        %JAVA_CMD% -jar %CURRENT_PATH%..\lib\ballerina-command-@version@.jar %*
   )
   if "%update%" == "true" if exist  %CURRENT_PATH%..\ballerina-command-tmp (
        call %CURRENT_PATH%\..\ballerina-command-tmp\install.bat
        if %errorlevel% neq 0 (
            echo Update failed due to errors
            rd /s /q %CURRENT_PATH%\..\ballerina-command-tmp
            exit /b %errorlevel%
        )
        rd /s /q %CURRENT_PATH%\..\ballerina-command-tmp
        echo Update successfully completed
        echo.
        echo "If you want to update the Ballerina distribution, use 'bal dist update'"
        exit /b
   )
)

if "%RUN_BALLERINA%" == "true" (
    set BALLERINA_HOME=
    for /f %%a in (%CURRENT_PATH%\..\distributions\ballerina-version) do (
        set BALLERINA_HOME=%%a
    )
    if exist "%userprofile%\.ballerina\ballerina-version" (
        set "FILE_PATH=%userprofile%\.ballerina\ballerina-version"
    )

    SetLocal EnableDelayedExpansion
    set CURRENT_VERSION=
    for /F "tokens=* delims=" %%a in ('Type "!FILE_PATH!"') do (
        set CURRENT_VERSION=%%a
    )
    if exist %CURRENT_PATH%..\distributions\%CURRENT_VERSION% (
        set BALLERINA_HOME=!CURRENT_VERSION!
    )

    if exist %CURRENT_PATH%..\distributions\!BALLERINA_HOME!\bin\bal.bat (
        call %CURRENT_PATH%..\distributions\!BALLERINA_HOME!\bin\bal.bat %*
    ) else (
        call %CURRENT_PATH%..\distributions\!BALLERINA_HOME!\bin\ballerina.bat %*
    )
)
set merge=false
if "%1" == "help" (
	if "%~2" == "" set merge=true
)
if "%1" == "-h" set merge=true
if "%1" == "--help" set merge=true
if "%1" == "" set merge=true
if "%1" == "version" set merge=true
if "%1" == "-v" set merge=true
if "%1" == "--version" set merge=true

if "%merge%" == "true" (
    %JAVA_CMD% -jar %CURRENT_PATH%..\lib\ballerina-command-@version@.jar %*
)

exit /b
