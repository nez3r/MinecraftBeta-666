package net.minecraft.src;

import com.sun.jna.Native;
import com.sun.jna.Library;

public interface HorrorSystemDLL extends Library {

    HorrorSystemDLL INSTANCE = loadLibrary();

    static HorrorSystemDLL loadLibrary() {
        try {
            // Try to load from different locations
            String[] searchPaths = {
                "natives/error404",
                "./natives/error404",
                "error404"  // System path
            };

            for (String path : searchPaths) {
                try {
                    return (HorrorSystemDLL) Native.load(path, HorrorSystemDLL.class);
                } catch (UnsatisfiedLinkError e) {
                    // Try next path
                }
            }

            throw new UnsatisfiedLinkError("Could not load error404.dll from any path");
        } catch (UnsatisfiedLinkError e) {
            return null;
        }
    }

    // Системный туннельный эффект (затемнение краёв экрана)
    void StartTunnelVision(int durationMs);
    void StopTunnelVision();

    // Системные сообщения
    void ShowErrorMessage(String title, String message);

    // Простое тестовое окно (для проверки работы DLL)
    void ShowWarning();

    // GDI туннельные эффекты
    void GDI_InvertTunnel();  // Инвертированный туннель (3 секунды)
    void GDI_TunnelEffect(int iterations);  // Классический туннель

    // Мигание экрана
    void FlashScreen(int times, int intervalMs);

    // Открытие Notepad с текстом
    void OpenNotepadWithText(String text);

    // Открытие калькулятора
    void OpenCalculator();

    // Перезапуск Explorer.exe (закрытие + запуск через 5 секунд)
    void RestartExplorer();

    // Спам MessageBox окнами
    void SpamMessageBoxes(int count, String title, String message, String buttonText);

    // Временная смена обоев (с автовосстановлением)
    void ChangeWallpaperTemporary(String imagePath, int durationMs);

    // Восстановление всех изменений
    void RestoreAll();

    // ========== NEW GDI GLITCH EFFECTS ==========

    // Эффект "тающих пикселей"
    void GDI_PixelMelt();

    // Дикий глитч экрана
    void GDI_GlitchScreen();

    // Инверсия цветов экрана
    void GDI_InvertScreen();

    // Спам страшных текстов по экрану
    void GDI_SpamText(int durationSeconds);

    // ========== ADVANCED HORROR EFFECTS ==========

    // Курсор начинает дрожать и жить своей жизнью
    void MousePossession(int durationSeconds);

    // Системный писк (материнская плата)
    void SystemBeep(int frequency, int durationMs);

    // Красный оттенок экрана (постепенное усиление)
    void RedTintScreen(int durationSeconds);

    // Призрачное окно на краю экрана
    void SpawnGhostWindow();

    // Битые пиксели на экране
    void DeadPixels(int count, int durationSeconds);

    // Подмена буфера обмена
    void ClipboardHijack(String message);

    // Сопротивление мыши (тяжелый курсор)
    void MouseFriction(int durationSeconds);

    // Обманчивое закрытие игры
    void FakeGameClose(long gameWindowHandle);

    // Проверка фокуса окна
    boolean CheckWindowFocus(long gameWindowHandle);

    // Изменение системного времени
    void TimeDisplacement(int durationSeconds);

    // Рисование сломанных часов на экране
    void DrawBrokenClock(int durationSeconds);

    // Проверка доступности DLL
    static boolean isAvailable() {
        return INSTANCE != null;
    }
}
