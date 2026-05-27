#include <windows.h>
#include <thread>
#include <atomic>
#include <cmath>
#include <cstdio>
#include <cstring>

// Global variables
HWND g_overlayWindow = NULL;
std::atomic<bool> g_tunnelActive(false);
std::thread* g_tunnelThread = nullptr;
char g_originalWallpaper[MAX_PATH] = {0};
bool g_wallpaperSaved = false;

// Transparent window for tunnel effect
LRESULT CALLBACK OverlayWndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    switch (msg) {
        case WM_PAINT: {
            PAINTSTRUCT ps;
            HDC hdc = BeginPaint(hwnd, &ps);

            // Get screen dimensions
            int screenWidth = GetSystemMetrics(SM_CXSCREEN);
            int screenHeight = GetSystemMetrics(SM_CYSCREEN);

            // Create gradient from edges to center
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;
            int maxRadius = (int)sqrt(centerX * centerX + centerY * centerY);

            // Draw darkening from edges
            for (int radius = maxRadius; radius > maxRadius / 3; radius -= 10) {
                int alpha = (int)(255.0 * (1.0 - (double)radius / maxRadius));
                if (alpha > 200) alpha = 200; // Maximum darkening

                HBRUSH brush = CreateSolidBrush(RGB(0, 0, 0));
                HPEN pen = CreatePen(PS_SOLID, 20, RGB(0, 0, 0));
                SelectObject(hdc, brush);
                SelectObject(hdc, pen);

                // Draw ring
                Ellipse(hdc,
                    centerX - radius, centerY - radius,
                    centerX + radius, centerY + radius);

                DeleteObject(brush);
                DeleteObject(pen);
            }

            EndPaint(hwnd, &ps);
            return 0;
        }
        case WM_DESTROY:
            PostQuitMessage(0);
            return 0;
    }
    return DefWindowProc(hwnd, msg, wParam, lParam);
}

// Create transparent overlay window
void CreateOverlayWindow() {
    WNDCLASSEX wc = {0};
    wc.cbSize = sizeof(WNDCLASSEX);
    wc.lpfnWndProc = OverlayWndProc;
    wc.hInstance = GetModuleHandle(NULL);
    wc.lpszClassName = "TunnelVisionOverlay";
    wc.hbrBackground = (HBRUSH)GetStockObject(BLACK_BRUSH);

    RegisterClassEx(&wc);

    int screenWidth = GetSystemMetrics(SM_CXSCREEN);
    int screenHeight = GetSystemMetrics(SM_CYSCREEN);

    // Create fullscreen window with transparency
    g_overlayWindow = CreateWindowEx(
        WS_EX_LAYERED | WS_EX_TRANSPARENT | WS_EX_TOPMOST | WS_EX_TOOLWINDOW,
        "TunnelVisionOverlay",
        "",
        WS_POPUP,
        0, 0, screenWidth, screenHeight,
        NULL, NULL, GetModuleHandle(NULL), NULL
    );

    // Set transparency (80% opacity for darkening)
    SetLayeredWindowAttributes(g_overlayWindow, RGB(0, 0, 0), 200, LWA_ALPHA);

    ShowWindow(g_overlayWindow, SW_SHOW);
    UpdateWindow(g_overlayWindow);
}

// Thread for tunnel effect
void TunnelVisionThread(int durationMs) {
    CreateOverlayWindow();

    // Process window messages
    MSG msg;
    DWORD startTime = GetTickCount();

    while (g_tunnelActive && (GetTickCount() - startTime) < (DWORD)durationMs) {
        if (PeekMessage(&msg, NULL, 0, 0, PM_REMOVE)) {
            TranslateMessage(&msg);
            DispatchMessage(&msg);
        }

        // Update window for pulsation effect
        InvalidateRect(g_overlayWindow, NULL, TRUE);
        Sleep(50);
    }

    // Close window
    if (g_overlayWindow) {
        DestroyWindow(g_overlayWindow);
        g_overlayWindow = NULL;
    }

    g_tunnelActive = false;
}

// ========== EXPORTED FUNCTIONS ==========

extern "C" {

__declspec(dllexport) void StartTunnelVision(int durationMs) {
    if (g_tunnelActive) {
        return; // Already active
    }

    g_tunnelActive = true;

    // Start in separate thread
    if (g_tunnelThread) {
        delete g_tunnelThread;
    }

    g_tunnelThread = new std::thread(TunnelVisionThread, durationMs);
    g_tunnelThread->detach();
}

__declspec(dllexport) void StopTunnelVision() {
    g_tunnelActive = false;

    if (g_overlayWindow) {
        DestroyWindow(g_overlayWindow);
        g_overlayWindow = NULL;
    }
}

__declspec(dllexport) void ShowErrorMessage(const char* title, const char* message) {
    MessageBoxA(NULL, message, title, MB_ICONERROR | MB_SYSTEMMODAL);
}

__declspec(dllexport) void ShowWarning() {
    MessageBoxA(NULL, "404", "?????", MB_OK | MB_ICONWARNING);
}

__declspec(dllexport) void GDI_InvertTunnel() {
    HDC hdc = GetDC(NULL);
    int w = GetSystemMetrics(SM_CXSCREEN);
    int h = GetSystemMetrics(SM_CYSCREEN);

    // 3 seconds effect = 100 iterations x 30ms
    for (int i = 0; i < 100; i++) {
        // Draw inverted rectangle that narrows to center
        int offset = i * 5;
        PatBlt(hdc, offset, offset, w - (offset * 2), h - (offset * 2), DSTINVERT);
        Sleep(30);
    }

    ReleaseDC(NULL, hdc);

    // Force screen update to remove artifacts
    InvalidateRect(NULL, NULL, TRUE);
    UpdateWindow(GetDesktopWindow());
}

__declspec(dllexport) void GDI_TunnelEffect(int iterations) {
    HDC hdc = GetDC(NULL);
    int screenWidth = GetSystemMetrics(SM_CXSCREEN);
    int screenHeight = GetSystemMetrics(SM_CYSCREEN);

    for (int i = 0; i < iterations; i++) {
        // Copy screen with inward offset - creates corridor effect
        StretchBlt(hdc, 10, 10, screenWidth - 20, screenHeight - 20,
                   hdc, 0, 0, screenWidth, screenHeight, SRCCOPY);

        Sleep(50);
    }

    ReleaseDC(NULL, hdc);
}

__declspec(dllexport) void FlashScreen(int times, int intervalMs) {
    HDC hdc = GetDC(NULL);
    int width = GetSystemMetrics(SM_CXSCREEN);
    int height = GetSystemMetrics(SM_CYSCREEN);

    for (int i = 0; i < times; i++) {
        // Invert colors
        BitBlt(hdc, 0, 0, width, height, hdc, 0, 0, NOTSRCCOPY);
        Sleep(intervalMs);
        BitBlt(hdc, 0, 0, width, height, hdc, 0, 0, NOTSRCCOPY);
        Sleep(intervalMs);
    }

    ReleaseDC(NULL, hdc);
}

__declspec(dllexport) void OpenNotepadWithText(const char* text) {
    // Create temporary file
    char tempPath[MAX_PATH];
    GetTempPathA(MAX_PATH, tempPath);
    strcat(tempPath, "minecraft666.txt");

    FILE* f = fopen(tempPath, "w");
    if (f) {
        fprintf(f, "%s", text);
        fclose(f);

        // Open in Notepad
        ShellExecuteA(NULL, "open", "notepad.exe", tempPath, NULL, SW_SHOW);
    }
}

__declspec(dllexport) void OpenCalculator() {
    // Open Windows Calculator
    ShellExecuteA(NULL, "open", "calc.exe", NULL, NULL, SW_SHOW);
}

__declspec(dllexport) void RestartExplorer() {
    // Close explorer.exe
    system("taskkill /F /IM explorer.exe");

    // Wait 5 seconds
    Sleep(5000);

    // Start explorer.exe again
    ShellExecuteA(NULL, "open", "explorer.exe", NULL, NULL, SW_SHOW);
}

__declspec(dllexport) void SpamMessageBoxes(int count, const char* title, const char* message, const char* buttonText) {
    // Spam MessageBox (buttonText ignored, as MessageBox doesn't allow custom button text)
    for (int i = 0; i < count; i++) {
        MessageBoxA(NULL, message, title, MB_OK | MB_ICONWARNING | MB_SYSTEMMODAL);
    }
}

__declspec(dllexport) void ChangeWallpaperTemporary(const char* imagePath, int durationMs) {
    // Save original wallpaper
    if (!g_wallpaperSaved) {
        SystemParametersInfoA(SPI_GETDESKWALLPAPER, MAX_PATH, g_originalWallpaper, 0);
        g_wallpaperSaved = true;
    }

    // Change wallpaper
    SystemParametersInfoA(SPI_SETDESKWALLPAPER, 0, (void*)imagePath, 0);

    // Start timer for restoration
    std::thread([durationMs]() {
        Sleep(durationMs);
        if (g_wallpaperSaved) {
            SystemParametersInfoA(SPI_SETDESKWALLPAPER, 0, g_originalWallpaper, SPIF_UPDATEINIFILE);
        }
    }).detach();
}

__declspec(dllexport) void RestoreAll() {
    // Stop tunnel effect
    StopTunnelVision();

    // Restore wallpaper
    if (g_wallpaperSaved) {
        SystemParametersInfoA(SPI_SETDESKWALLPAPER, 0, g_originalWallpaper, SPIF_UPDATEINIFILE);
        g_wallpaperSaved = false;
    }
}

// ========== NEW GDI GLITCH EFFECTS ==========

__declspec(dllexport) void GDI_PixelMelt() {
    HDC hdc = GetDC(NULL);
    int w = GetSystemMetrics(SM_CXSCREEN);
    int h = GetSystemMetrics(SM_CYSCREEN);

    // Выбираем случайную координату по X
    int x = rand() % w;
    // Выбираем случайную ширину полоски
    int width = rand() % 50;
    // Сдвигаем вертикальный кусок экрана на 2-5 пикселей вниз
    BitBlt(hdc, x, rand() % 5, width, h, hdc, x, 0, SRCCOPY);

    ReleaseDC(NULL, hdc);
}

__declspec(dllexport) void GDI_GlitchScreen() {
    HDC hdc = GetDC(NULL);
    int w = GetSystemMetrics(SM_CXSCREEN);
    int h = GetSystemMetrics(SM_CYSCREEN);

    for (int i = 0; i < 20; i++) {
        int x1 = rand() % w;
        int y1 = rand() % h;
        int x2 = rand() % w;
        int y2 = rand() % h;
        int width = rand() % 200;
        int height = rand() % 200;

        // Копируем кусок экрана (x1, y1) в место (x2, y2)
        BitBlt(hdc, x1, y1, width, height, hdc, x2, y2, SRCCOPY);
    }

    ReleaseDC(NULL, hdc);
}

__declspec(dllexport) void GDI_InvertScreen() {
    HDC hdc = GetDC(NULL);
    int w = GetSystemMetrics(SM_CXSCREEN);
    int h = GetSystemMetrics(SM_CYSCREEN);

    // Флаг NOTSRCCOPY инвертирует цвета при копировании
    BitBlt(hdc, 0, 0, w, h, hdc, 0, 0, NOTSRCCOPY);

    ReleaseDC(NULL, hdc);
}

__declspec(dllexport) void GDI_SpamText(int durationSeconds) {
    HDC hdc = GetDC(NULL);
    int w = GetSystemMetrics(SM_CXSCREEN);
    int h = GetSystemMetrics(SM_CYSCREEN);

    // Список пугающих фраз
    const char* phrases[] = {
        "RUN", "I SEE YOU", "NOT ALONE", "LOOK BEHIND",
        "ERROR", "HELP ME", "VOID", "666", "SYSTEM FAILURE",
        "404", "Player404", "YOU SHOULDN'T BE HERE", "GET OUT",
        "NO ESCAPE", "CORRUPTED", "FATAL ERROR", "WATCHING YOU",
        "TOO LATE", "GAME OVER", "DELETE", "CRASH"
    };
    int phraseCount = sizeof(phrases) / sizeof(phrases[0]);

    DWORD startTime = GetTickCount();
    while (GetTickCount() - startTime < (DWORD)(durationSeconds * 1000)) {
        int x = rand() % w;
        int y = rand() % h;
        const char* text = phrases[rand() % phraseCount];

        // Настройка цвета (красный или белый на черном фоне)
        SetTextColor(hdc, RGB(255 - rand() % 50, 0, 0)); // Красные оттенки
        SetBkMode(hdc, TRANSPARENT);

        // Можно даже менять размер шрифта для большего хаоса
        HFONT hFont = CreateFontA(rand() % 100 + 20, 0, 0, 0, FW_BOLD,
                                  FALSE, FALSE, FALSE, ANSI_CHARSET,
                                  OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS,
                                  DEFAULT_QUALITY, DEFAULT_PITCH | FF_SWISS, "Arial");
        SelectObject(hdc, hFont);

        TextOutA(hdc, x, y, text, strlen(text));

        DeleteObject(hFont);
        Sleep(10); // Скорость появления надписей
    }

    ReleaseDC(NULL, hdc);
    // Принудительно обновляем экран, чтобы убрать надписи
    InvalidateRect(NULL, NULL, TRUE);
}

// ========== NEW ADVANCED HORROR EFFECTS ==========

__declspec(dllexport) void MousePossession(int durationSeconds) {
    POINT originalPos;
    GetCursorPos(&originalPos);

    DWORD startTime = GetTickCount();
    while (GetTickCount() - startTime < (DWORD)(durationSeconds * 1000)) {
        POINT currentPos;
        GetCursorPos(&currentPos);

        // Дрожание курсора
        int offsetX = (rand() % 10) - 5;
        int offsetY = (rand() % 10) - 5;
        SetCursorPos(currentPos.x + offsetX, currentPos.y + offsetY);

        Sleep(50);
    }
}

__declspec(dllexport) void SystemBeep(int frequency, int durationMs) {
    Beep(frequency, durationMs);
}

__declspec(dllexport) void RedTintScreen(int durationSeconds) {
    HDC hdc = GetDC(NULL);
    int w = GetSystemMetrics(SM_CXSCREEN);
    int h = GetSystemMetrics(SM_CYSCREEN);

    DWORD startTime = GetTickCount();
    int intensity = 0;

    while (GetTickCount() - startTime < (DWORD)(durationSeconds * 1000)) {
        // Постепенное усиление красного оттенка (0 -> 150)
        if (intensity < 150) {
            intensity += 1;
        }

        // Рисуем полупрозрачные красные пиксели по всему экрану
        for (int i = 0; i < intensity / 10; i++) {
            for (int y = 0; y < h; y += 50) {
                for (int x = 0; x < w; x += 50) {
                    SetPixel(hdc, x, y, RGB(255, 0, 0));
                }
            }
        }

        Sleep(100);
    }

    ReleaseDC(NULL, hdc);
    InvalidateRect(NULL, NULL, TRUE);
}

__declspec(dllexport) void SpawnGhostWindow() {
    int screenW = GetSystemMetrics(SM_CXSCREEN);
    int screenH = GetSystemMetrics(SM_CYSCREEN);

    // Случайная позиция на краю экрана
    int x = (rand() % 2 == 0) ? 0 : screenW - 100;
    int y = rand() % screenH;

    HWND ghost = CreateWindowA("STATIC", "", WS_POPUP | WS_VISIBLE,
                 x, y, 100, 100, NULL, NULL, NULL, NULL);

    if (ghost) {
        SetWindowLong(ghost, GWL_EXSTYLE, GetWindowLong(ghost, GWL_EXSTYLE) | WS_EX_LAYERED);
        SetLayeredWindowAttributes(ghost, 0, 150, LWA_ALPHA);

        // Заполняем черным цветом
        HDC hdc = GetDC(ghost);
        RECT r = {0, 0, 100, 100};
        FillRect(hdc, &r, (HBRUSH)GetStockObject(BLACK_BRUSH));
        ReleaseDC(ghost, hdc);

        Sleep(200);
        DestroyWindow(ghost);
    }
}

__declspec(dllexport) void DeadPixels(int count, int durationSeconds) {
    HDC hdc = GetDC(NULL);

    // Сохраняем позиции "битых пикселей"
    POINT* pixels = new POINT[count];
    for (int i = 0; i < count; i++) {
        pixels[i].x = rand() % GetSystemMetrics(SM_CXSCREEN);
        pixels[i].y = rand() % GetSystemMetrics(SM_CYSCREEN);
    }

    DWORD startTime = GetTickCount();
    while (GetTickCount() - startTime < (DWORD)(durationSeconds * 1000)) {
        for (int i = 0; i < count; i++) {
            // Рисуем битый пиксель (черный или ярко-зеленый)
            COLORREF color = (rand() % 2 == 0) ? RGB(0, 0, 0) : RGB(0, 255, 0);
            SetPixel(hdc, pixels[i].x, pixels[i].y, color);

            // Рисуем квадратик 3x3 для видимости
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    SetPixel(hdc, pixels[i].x + dx, pixels[i].y + dy, color);
                }
            }
        }
        Sleep(100);
    }

    delete[] pixels;
    ReleaseDC(NULL, hdc);
    InvalidateRect(NULL, NULL, TRUE);
}

__declspec(dllexport) void ClipboardHijack(const char* message) {
    if (OpenClipboard(NULL)) {
        EmptyClipboard();

        size_t len = strlen(message) + 1;
        HGLOBAL hMem = GlobalAlloc(GMEM_MOVEABLE, len);
        if (hMem) {
            memcpy(GlobalLock(hMem), message, len);
            GlobalUnlock(hMem);
            SetClipboardData(CF_TEXT, hMem);
        }

        CloseClipboard();
    }
}

__declspec(dllexport) void MouseFriction(int durationSeconds) {
    POINT lastPos;
    GetCursorPos(&lastPos);

    DWORD startTime = GetTickCount();
    while (GetTickCount() - startTime < (DWORD)(durationSeconds * 1000)) {
        POINT currentPos;
        GetCursorPos(&currentPos);

        // Если мышь двигается, тянем её назад
        if (currentPos.x != lastPos.x || currentPos.y != lastPos.y) {
            int pullX = lastPos.x + (currentPos.x - lastPos.x) / 3;
            int pullY = lastPos.y + (currentPos.y - lastPos.y) / 3;
            SetCursorPos(pullX, pullY);

            lastPos.x = pullX;
            lastPos.y = pullY;
        } else {
            lastPos = currentPos;
        }

        Sleep(10);
    }
}

__declspec(dllexport) void FakeGameClose(long gameWindowHandle) {
    HWND gameWindow = (HWND)gameWindowHandle;

    if (!gameWindow || !IsWindow(gameWindow)) {
        // Пытаемся найти окно Minecraft по разным вариантам названия
        gameWindow = FindWindowA(NULL, "Minecraft Beta 1.6.6");

        if (!gameWindow) {
            gameWindow = FindWindowA(NULL, "Minecraft");
        }

        if (!gameWindow) {
            // Ищем окно LWJGL
            gameWindow = FindWindowA("LWJGL", NULL);
        }
    }

    if (gameWindow && IsWindow(gameWindow)) {
        // Скрываем окно
        ShowWindow(gameWindow, SW_HIDE);

        // Ждем 2 секунды
        Sleep(2000);

        // Меняем заголовок окна
        SetWindowTextA(gameWindow, "YOU CAN'T LEAVE");

        // Показываем окно обратно
        ShowWindow(gameWindow, SW_SHOW);
        SetForegroundWindow(gameWindow);

        // Через 3 секунды возвращаем нормальный заголовок
        Sleep(3000);
        SetWindowTextA(gameWindow, "Minecraft Beta 1.6.6");
    }
}

__declspec(dllexport) bool CheckWindowFocus(HWND gameWindow) {
    if (!gameWindow) {
        gameWindow = FindWindowA(NULL, "Minecraft Beta 1.6.6");
    }

    if (gameWindow) {
        HWND foreground = GetForegroundWindow();
        return (foreground == gameWindow);
    }

    return false;
}

__declspec(dllexport) void TimeDisplacement(int durationSeconds) {
    SYSTEMTIME originalTime;
    GetLocalTime(&originalTime);

    // Устанавливаем время на 00:00:00
    SYSTEMTIME fakeTime = originalTime;
    fakeTime.wHour = 0;
    fakeTime.wMinute = 0;
    fakeTime.wSecond = 0;

    // Меняем системное время (требует прав администратора)
    SetLocalTime(&fakeTime);

    // Ждем указанное время
    Sleep(durationSeconds * 1000);

    // Возвращаем оригинальное время
    SetLocalTime(&originalTime);
}

__declspec(dllexport) void DrawBrokenClock(int durationSeconds) {
    HDC hdc = GetDC(NULL);
    int w = GetSystemMetrics(SM_CXSCREEN);

    DWORD startTime = GetTickCount();
    while (GetTickCount() - startTime < (DWORD)(durationSeconds * 1000)) {
        // Рисуем "сломанные" часы в правом верхнем углу
        SetTextColor(hdc, RGB(255, 0, 0));
        SetBkMode(hdc, TRANSPARENT);

        HFONT hFont = CreateFontA(40, 0, 0, 0, FW_BOLD,
                                  FALSE, FALSE, FALSE, ANSI_CHARSET,
                                  OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS,
                                  DEFAULT_QUALITY, DEFAULT_PITCH | FF_SWISS, "Arial");
        HFONT oldFont = (HFONT)SelectObject(hdc, hFont);

        // Случайное время или 00:00
        char timeStr[20];
        if (rand() % 2 == 0) {
            sprintf(timeStr, "00:00:00");
        } else {
            sprintf(timeStr, "%02d:%02d:%02d", rand() % 24, rand() % 60, rand() % 60);
        }

        TextOutA(hdc, w - 200, 20, timeStr, strlen(timeStr));

        SelectObject(hdc, oldFont);
        DeleteObject(hFont);

        Sleep(1000);
    }

    ReleaseDC(NULL, hdc);
    InvalidateRect(NULL, NULL, TRUE);
}

} // extern "C"

// DLL Entry Point
BOOL APIENTRY DllMain(HMODULE hModule, DWORD ul_reason_for_call, LPVOID lpReserved) {
    switch (ul_reason_for_call) {
        case DLL_PROCESS_ATTACH:
            break;
        case DLL_PROCESS_DETACH:
            RestoreAll();
            break;
    }
    return TRUE;
}
