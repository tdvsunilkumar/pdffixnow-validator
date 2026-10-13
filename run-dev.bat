@echo off
REM Builds and launches the app. The window stays open so you can read any error.
cd /d "%~dp0"
echo Building... (first time downloads dependencies, this can take a few minutes)
call mvn clean package > build.log 2>&1
if errorlevel 1 (
  echo.
  echo ===== BUILD FAILED. Last lines of build.log: =====
  powershell -NoProfile -Command "Get-Content build.log -Tail 40"
  echo.
  echo Full log saved in: %~dp0build.log
  pause
  exit /b 1
)
echo Build OK. Starting PDFFixNow...
java -jar target\pdffixnow.jar > run.log 2>&1
if errorlevel 1 (
  echo.
  echo ===== APP CRASHED. Output: =====
  type run.log
  echo.
  echo Full log saved in: %~dp0run.log
)
pause
