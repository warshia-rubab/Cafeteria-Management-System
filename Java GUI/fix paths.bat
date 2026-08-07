@echo off
echo ========================================
echo    FIXING FILE LOCATIONS
echo ========================================
echo.

echo 1. Creating DATA folder...
mkdir DATA 2>nul

echo 2. Copying files from C++_Backend to DATA...
copy "C++_Backend\menu.csv" "DATA\" 2>nul
copy "C++_Backend\sales.csv" "DATA\" 2>nul
copy "C++_Backend\orders.csv" "DATA\" 2>nul

echo 3. If files don't exist, creating them...
if not exist "DATA\menu.csv" (
    echo ID,Name,Price,Stock,Sold > DATA\menu.csv
    echo 101,Burger,5.99,50,0 >> DATA\menu.csv
    echo 102,Pizza,8.99,30,0 >> DATA\menu.csv
    echo 103,Fries,2.99,100,0 >> DATA\menu.csv
    echo 104,Coffee,1.99,200,0 >> DATA\menu.csv
)

if not exist "DATA\sales.csv" (
    echo OrderID,Items,Total,Status,Time > DATA\sales.csv
)

if not exist "DATA\orders.csv" (
    echo. > DATA\orders.csv
)

echo.
echo ========================================
echo    FILES ARE NOW IN: %cd%\DATA\
echo ========================================
echo.
dir DATA
echo.
pause