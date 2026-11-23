@echo off
title Smart College Management & Student Administration System - RISU
color 0B

echo.
echo  ========================================================================
echo   SMART COLLEGE MANAGEMENT AND STUDENT ADMINISTRATION SYSTEM
echo   Applied DSA (HashMap, Min-Heap, FIFO Queue, LIFO Stack)
echo   Rungta International Skills University (RISU), Bhilai
echo  ========================================================================
echo.

:: Detect Java JDK
set "JAVAC_EXE="
set "JAVA_EXE="

if exist "C:\Program Files\Microsoft\jdk-21.0.13.21-hotspot\bin\javac.exe" (
    set "JAVAC_EXE=C:\Program Files\Microsoft\jdk-21.0.13.21-hotspot\bin\javac.exe"
    set "JAVA_EXE=C:\Program Files\Microsoft\jdk-21.0.13.21-hotspot\bin\java.exe"
    echo  [OK] Found Microsoft OpenJDK 21
    goto :compile
)

where javac >nul 2>&1
if %errorlevel% == 0 (
    set "JAVAC_EXE=javac"
    set "JAVA_EXE=java"
    echo  [OK] Found Java in system PATH
    goto :compile
)

for /d %%i in ("C:\Program Files\Microsoft\jdk-*") do (
    if exist "%%i\bin\javac.exe" (
        set "JAVAC_EXE=%%i\bin\javac.exe"
        set "JAVA_EXE=%%i\bin\java.exe"
        echo  [OK] Found JDK at: %%i
        goto :compile
    )
)

for /d %%i in ("C:\Program Files\Java\jdk-*") do (
    if exist "%%i\bin\javac.exe" (
        set "JAVAC_EXE=%%i\bin\javac.exe"
        set "JAVA_EXE=%%i\bin\java.exe"
        echo  [OK] Found JDK at: %%i
        goto :compile
    )
)

echo  [ERROR] Java JDK not found!
pause
exit /b 1

:compile
echo.
echo  [1/2] Compiling Java classes...
cd /d "%~dp0src"

"%JAVAC_EXE%" -d . models/*.java services/*.java server/*.java Main.java
if %errorlevel% neq 0 (
    echo.
    echo  [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo  [OK] Compilation successful!
echo.
echo  [2/2] Starting College Management Web Server on port 8080...
echo.
echo  ========================================================================
echo   The Interactive Web Dashboard will automatically open in your browser!
echo   Dashboard URL: http://localhost:8080/
echo  ========================================================================
echo.

"%JAVA_EXE%" Main
pause

