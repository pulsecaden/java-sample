@echo off
setlocal

set ROOT_DIR=%~dp0
set MAVEN_VERSION=3.9.16
set DISTRIBUTION_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip
set DIST_DIR=%ROOT_DIR%.mvn\wrapper\dists\apache-maven-%MAVEN_VERSION%
set MAVEN_HOME=%DIST_DIR%\apache-maven-%MAVEN_VERSION%
set ARCHIVE=%DIST_DIR%\apache-maven-%MAVEN_VERSION%-bin.zip

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
    if not exist "%ARCHIVE%" (
        powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%ARCHIVE%'"
    )
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%ARCHIVE%' -DestinationPath '%DIST_DIR%' -Force"
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal
