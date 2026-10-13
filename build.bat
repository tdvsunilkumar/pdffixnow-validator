@echo off
REM ============================================================
REM  PDFFixNow - build the Windows installer
REM  Run on a Windows PC with: JDK 21+, Maven, and WiX Toolset 3.x
REM  (WiX is only needed for the .exe installer; see README)
REM ============================================================
setlocal
set APP_VERSION=1.0.0
set "INSTALLER_DIR=%~dp0installer"

echo [1/3] Building jar with Maven...
call mvn -q clean package
if errorlevel 1 ( echo Maven build failed & exit /b 1 )

echo [2/3] Preparing input folder...
if exist target\input rmdir /s /q target\input
mkdir target\input
copy /y target\pdffixnow.jar target\input\ >nul

set ICON_OPT=
if exist installer\icon.ico set ICON_OPT=--icon installer\icon.ico

if exist target\dist rmdir /s /q target\dist

echo [3/3] Creating installer with jpackage...
jpackage ^
  --type exe ^
  --name PDFFixNow ^
  --app-version %APP_VERSION% ^
  --vendor "PDFFixNow" ^
  --description "Offline PDF/A validator" ^
  --copyright "Copyright PDFFixNow" ^
  --input target\input ^
  --main-jar pdffixnow.jar ^
  --main-class com.pdffixnow.desktop.Launcher ^
  --dest target\dist ^
  --license-file installer\LICENSE.txt ^
  --resource-dir installer ^
  --win-menu --win-menu-group "PDFFixNow" ^
  --win-shortcut --win-dir-chooser ^
  --win-upgrade-uuid 6c1d2a5e-3b7f-4e0a-9d52-8f41c7a0b1e3 ^
  --add-modules java.base,java.desktop,java.logging,java.xml,java.naming,java.sql,java.prefs,java.management,java.net.http,jdk.unsupported,jdk.xml.dom,jdk.crypto.ec,jdk.localedata ^
  --java-options "-Xmx1g" ^
  %ICON_OPT%
if errorlevel 1 ( echo jpackage failed & exit /b 1 )

echo.
echo Done. Installer is in target\dist
pause
endlocal
