@echo off
rem Minimal mvnw.cmd that bootstraps the Takari maven-wrapper jar if missing
set SCRIPT_DIR=%~dp0
set WRAPPER_DIR=%SCRIPT_DIR%.mvn\wrapper
set JAR=%WRAPPER_DIR%\maven-wrapper.jar

if not exist "%JAR%" (
  mkdir "%WRAPPER_DIR%" >nul 2>&1
  powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar' -OutFile '%JAR%'"
  if %ERRORLEVEL% NEQ 0 (
    echo Failed to download maven-wrapper.jar
    exit /b 1
  )
)

java -jar "%JAR%" %*
