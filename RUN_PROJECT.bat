@echo off
title Cafeteria Management System
color 0A

echo ========================================
echo   CAFETERIA MANAGEMENT SYSTEM
echo   Complete Project Runner
echo ========================================
echo.

echo PROJECT STRUCTURE:
echo - C++ Backend: C++_Backend\cafeteria.cpp (Open in Dev-C++)
echo - Java GUI: Java_GUI\CafeteriaGUI.java (Open in VS Code)
echo - Shared Data: DATA\ folder
echo.

echo INSTRUCTIONS:
echo 1. First, run C++ Backend in Dev-C++
echo    - Open C++_Backend\cafeteria.cpp
echo    - Press F11 to compile and run
echo    - Keep it running
echo.
echo 2. Then run Java GUI
echo    - Right-click run.bat in Java_GUI folder
echo    - Select "Run as administrator"
echo.
echo 3. Both programs will share data through CSV files
echo.

echo Current folder: %cd%
echo.
echo Press any key to open project folders...
pause > nul

explorer "C++_Backend"
explorer "Java_GUI"
explorer "DATA"

echo.
echo Folders opened! Follow instructions above.
pause