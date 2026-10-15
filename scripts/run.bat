@echo off
REM Compile and run DineSync on Windows. Usage: scripts\run.bat [--demo] [--fast]
cd /d "%~dp0..\src"
javac *.java || exit /b 1
java Main %*
