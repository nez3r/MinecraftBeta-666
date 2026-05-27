# Добавление JNA в проект

## Что такое JNA?

JNA (Java Native Access) - библиотека для вызова нативных функций из DLL без написания JNI кода.

## Скачивание JNA

### Вариант 1: Maven Central
Скачай последние версии:
- https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar
- https://repo1.maven.org/maven2/net/java/dev/jna/jna-platform/5.13.0/jna-platform-5.13.0.jar

### Вариант 2: Прямые ссылки
```
https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar
https://repo1.maven.org/maven2/net/java/dev/jna/jna-platform/5.13.0/jna-platform-5.13.0.jar
```

## Установка

### 1. Создай структуру директорий
```
libraries/
└── net/
    └── java/
        └── dev/
            └── jna/
                ├── jna/
                │   └── 5.13.0/
                │       └── jna-5.13.0.jar
                └── jna-platform/
                    └── 5.13.0/
                        └── jna-platform-5.13.0.jar
```

### 2. Скопируй JAR файлы
```bash
# Создай директории
mkdir -p libraries/net/java/dev/jna/jna/5.13.0
mkdir -p libraries/net/java/dev/jna/jna-platform/5.13.0

# Скопируй JAR файлы
copy jna-5.13.0.jar libraries/net/java/dev/jna/jna/5.13.0/
copy jna-platform-5.13.0.jar libraries/net/java/dev/jna/jna-platform/5.13.0/
```

## Обновление START.bat

Добавь JNA в classpath:

```batch
@echo off
title Minecraft 666 - Horror Edition

cd game

java -cp ^
../minecraft.jar;^
../libraries/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar;^
../libraries/net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar;^
../libraries/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar;^
../libraries/org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar;^
../libraries/com/paulscode/codecjorbis/20230120/codecjorbis-20230120.jar;^
../libraries/com/paulscode/codecwav/20101023/codecwav-20101023.jar;^
../libraries/com/paulscode/libraryjavasound/20101123/libraryjavasound-20101123.jar;^
../libraries/com/paulscode/librarylwjglopenal/20100824/librarylwjglopenal-20100824.jar;^
../libraries/com/paulscode/soundsystem/20120107/soundsystem-20120107.jar;^
../libraries/org/mcphackers/launchwrapper/1.2.4/launchwrapper-1.2.4.jar;^
../libraries/org/json/json/20230311/json-20230311.jar;^
../libraries/org/ow2/asm/asm/9.9/asm-9.9.jar;^
../libraries/org/ow2/asm/asm-tree/9.9/asm-tree-9.9.jar;^
../libraries/org/ow2/asm/asm-commons/9.9/asm-commons-9.9.jar;^
../libraries/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar;^
../libraries/net/java/dev/jna/jna-platform/5.13.0/jna-platform-5.13.0.jar ^
-Djava.library.path=../libraries/natives ^
org.mcphackers.launchwrapper.Launch ^
--username Player ^
--version b1.6.6 ^
--gameDir . ^
--assetsDir ./assets ^
--assetIndex b1.5

pause
```

## Проверка

### Тест загрузки JNA:
```java
public class TestJNA {
    public static void main(String[] args) {
        try {
            Class.forName("com.sun.jna.Native");
            System.out.println("JNA loaded successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("JNA not found!");
        }
    }
}
```

### Тест загрузки DLL:
```java
if (HorrorSystemDLL.isAvailable()) {
    System.out.println("horror_system.dll loaded successfully!");
} else {
    System.err.println("horror_system.dll not found!");
}
```

## Troubleshooting

### Ошибка: "ClassNotFoundException: com.sun.jna.Native"
- Проверь, что JNA JAR файлы в правильных директориях
- Проверь classpath в START.bat

### Ошибка: "UnsatisfiedLinkError: Unable to load library 'horror_system'"
- Проверь, что horror_system.dll в libraries/natives/
- Проверь архитектуру: 64-bit Java требует 64-bit DLL
- Проверь имя: должно быть точно "horror_system.dll"

### DLL загружается, но функции не работают
- Проверь экспорт функций: `dumpbin /exports horror_system.dll`
- Проверь сигнатуры функций в HorrorSystemDLL.java

## Альтернатива: Упрощённая структура

Если не хочешь создавать сложную структуру директорий, можно положить JAR файлы в корень:

```
libraries/
├── jna-5.13.0.jar
└── jna-platform-5.13.0.jar
```

И изменить classpath:
```batch
java -cp ../minecraft.jar;../libraries/jna-5.13.0.jar;../libraries/jna-platform-5.13.0.jar;...
```

## Финальная структура проекта

```
Minecraft666/
├── game/
│   ├── assets/
│   └── resources/
├── libraries/
│   ├── natives/
│   │   └── horror_system.dll  ← Скомпилированная DLL
│   └── net/java/dev/jna/
│       ├── jna/5.13.0/
│       │   └── jna-5.13.0.jar
│       └── jna-platform/5.13.0/
│           └── jna-platform-5.13.0.jar
├── minecraft.jar
├── START.bat
└── README.txt
```

## Дата создания
2026-05-10
