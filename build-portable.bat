@echo off
REM Builds a portable folder (no installer, no WiX needed): target\dist\PDFFixNow\PDFFixNow.exe
setlocal
call mvn -q clean package
if errorlevel 1 ( echo Maven build failed & exit /b 1 )
if exist target\input rmdir /s /q target\input
mkdir target\input
copy /y target\pdffixnow.jar target\input\ >nul
if exist target\dist rmdir /s /q target\dist
jpackage --type app-image --name PDFFixNow --app-version 1.0.0 --input target\input ^
  --main-jar pdffixnow.jar --main-class com.pdffixnow.desktop.Launcher --dest target\dist ^
  --icon installer\icon.ico ^
  --add-modules java.base,java.desktop,java.logging,java.xml,java.naming,java.sql,java.prefs,java.management,java.net.http,jdk.unsupported,jdk.xml.dom,jdk.crypto.ec,jdk.localedata ^
  --java-options "-Xmx1g"
echo Done. Run target\dist\PDFFixNow\PDFFixNow.exe
pause
endlocal
