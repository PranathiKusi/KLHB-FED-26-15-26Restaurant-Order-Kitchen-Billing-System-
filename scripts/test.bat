@echo off
REM Compile and run all automated tests on Windows.
cd /d "%~dp0..\src"
javac *.java || exit /b 1
java DineSyncTests
