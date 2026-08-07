@echo off
echo ========================================
echo   Java GUI - Cafeteria Management System
echo ========================================
echo.

cd /d "%~dp0"

echo Compiling Java files...
javac CafeteriaGUI.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Running Java GUI...
echo ========================================
echo.
echo IMPORTANT: Make sure C++ backend is also running!
echo.

java CafeteriaGUI

pause