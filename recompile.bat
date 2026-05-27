@echo off
echo Building...
java -jar RetroMCP-Java-CLI.jar build
timeout /t 3 /NOBREAK >nul 2>&1
echo Repacking...

set root=C:\Users\nez3r\Desktop\vers\minecraftb666
set build=%root%\build
set dest=C:\Users\nez3r\Desktop\vers\minecraftb666\build\666
set tmp=%build%\temp_move
set z=%root%\7z.exe

:: 1. Клиент
if exist "%build%\minecraft.zip" (
    %z% x "%build%\minecraft.zip" -o"%tmp%" -y >nul
    %z% a "%dest%\Minecraft.jar" "%tmp%\*" -sdel >nul
    if %ERRORLEVEL% EQU 0 (
        del "%build%\minecraft.zip"
        echo [OK] Minecraft.jar обновлен.
    )
    rd /s /q "%tmp%" 2>nul
)

:: 2. Сервер
if exist "%build%\minecraft_server.zip" (
    %z% x "%build%\minecraft_server.zip" -o"%tmp%" -y >nul
    %z% a "%dest%\server\server.jar" "%tmp%\*" -sdel >nul
    if %ERRORLEVEL% EQU 0 (
        del "%build%\minecraft_server.zip"
        echo [OK] server.jar обновлен.
    ) else (
        echo [ERROR] Не удалось обновить server.jar. Проверьте, не запущен ли сервер.
    )
    rd /s /q "%tmp%" 2>nul
)

echo -----------------
echo Done
pause

exit