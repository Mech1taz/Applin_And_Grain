@REM Maven wrapper batch script pointing to local Maven installation
@echo off
set "MAVEN_CMD=C:\Users\co-alumno\.local\apache-maven-3.9.9\bin\mvn.cmd"
if exist "%MAVEN_CMD%" (
    "%MAVEN_CMD%" %*
) else (
    mvn %*
)
