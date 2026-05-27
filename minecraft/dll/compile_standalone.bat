@echo off
echo ========================================
echo   Compiling STANDALONE error404.dll (x64)
echo ========================================
echo.

cd /d "%~dp0"

echo Compiling with static linking...
g++ -std=c++11 -O2 -Wall -m64 ^
    -shared ^
    -static-libgcc ^
    -static-libstdc++ ^
    -static ^
    -o error404.dll main.cpp ^
    -lgdi32 -luser32 -lshell32 -lkernel32 ^
    -Wl,--subsystem,windows

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   SUCCESS! Standalone DLL compiled
    echo ========================================
    echo.
    echo File size:
    dir error404.dll | find "error404.dll"
    echo.
    echo Copying to all locations...
    copy /Y error404.dll ..\..\libraries\natives\error404.dll
    copy /Y error404.dll ..\..\jars\error404.dll
    copy /Y error404.dll ..\..\error404.dll
	copy /Y error404.dll "C:\Users\nez3r\Desktop\666\natives\error404.dll"
    echo.
    echo ========================================
    echo   Done! Testing DLL...
    echo ========================================
    echo.
    echo If MessageBox appears, DLL works!
    rundll32 error404.dll,ShowWarning
) else (
    echo.
    echo ========================================
    echo   COMPILATION FAILED!
    echo ========================================
    echo.
    echo Make sure MinGW-w64 is installed and in PATH
)

pause
