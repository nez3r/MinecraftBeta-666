# Horror System DLL - Compilation Guide

## Требования

### Windows:
- **MinGW-w64** (рекомендуется) или **MSVC**
- Make (входит в MinGW)

### Установка MinGW-w64:
1. Скачай: https://www.mingw-w64.org/downloads/
2. Или через MSYS2: `pacman -S mingw-w64-x86_64-gcc`
3. Добавь в PATH: `C:\mingw64\bin`

## Компиляция

### Вариант 1: Через Make (рекомендуется)
```bash
cd dll
make
```

### Вариант 2: Вручную
```bash
cd dll
g++ -std=c++11 -O2 -shared -o horror_system.dll main.cpp -lgdi32 -luser32 -lshell32 -lkernel32 -static-libgcc -static-libstdc++
```

## Установка

### Автоматическая установка:
```bash
make install
```

### Ручная установка:
Скопируй `horror_system.dll` в `libraries/natives/`

## Проверка

После компиляции проверь:
```bash
# Проверка зависимостей
dumpbin /dependents horror_system.dll

# Проверка экспортируемых функций
dumpbin /exports horror_system.dll
```

## Экспортируемые функции

- `StartTunnelVision(int durationMs)` - Туннельный эффект на весь экран
- `StopTunnelVision()` - Остановка туннельного эффекта
- `ShowErrorMessage(const char* title, const char* message)` - Системное сообщение
- `FlashScreen(int times, int intervalMs)` - Мигание экрана
- `OpenNotepadWithText(const char* text)` - Открытие Notepad с текстом
- `ChangeWallpaperTemporary(const char* path, int durationMs)` - Временная смена обоев
- `RestoreAll()` - Восстановление всех изменений

## Интеграция с Java

### 1. Добавь JNA библиотеку
Скачай `jna.jar` и `jna-platform.jar` в `libraries/`

### 2. Используй HorrorSystemDLL.java
```java
if (HorrorSystemDLL.isAvailable()) {
    HorrorSystemDLL.INSTANCE.StartTunnelVision(10000); // 10 секунд
}
```

## Troubleshooting

### Ошибка: "g++ not found"
- Установи MinGW-w64
- Добавь в PATH: `C:\mingw64\bin`

### Ошибка: "cannot find -lgdi32"
- Переустанови MinGW-w64 с полным набором библиотек

### DLL не загружается в Java
- Проверь, что DLL в `libraries/natives/`
- Проверь архитектуру: 64-bit Java требует 64-bit DLL
- Проверь зависимости: `dumpbin /dependents horror_system.dll`

### Туннельный эффект не работает
- Проверь права администратора (для overlay окна)
- Проверь антивирус (может блокировать overlay)

## Безопасность

✅ **Безопасные эффекты:**
- Туннельное зрение (временное overlay окно)
- MessageBox сообщения
- Мигание экрана (инверсия цветов)
- Открытие Notepad
- Временная смена обоев (с автовосстановлением)

❌ **НЕ реализовано (небезопасно):**
- Удаление файлов
- Изменение реестра
- Блокировка системы
- Кража данных

## Лицензия

Этот код предназначен для хоррор-мода Minecraft 666.
Все эффекты обратимы и не наносят вреда системе.

## Дата создания
2026-05-10
