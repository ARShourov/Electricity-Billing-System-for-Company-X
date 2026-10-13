@echo off
rem Compile and start the app (Windows). Needs JDK 17+ on PATH.
cd /d "%~dp0"
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
if errorlevel 1 (
  echo.
  echo Compile failed. Is the JDK installed?  Check:  javac -version
  del sources.txt
  pause
  exit /b 1
)
del sources.txt
java -cp "bin;src" electricity.billing.system.Splash
