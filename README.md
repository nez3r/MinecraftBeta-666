# Minecraft Beta 1.6.6 - RetroMCP Project

Проект для декомпиляции, модификации и сборки клиента и сервера Minecraft Beta 1.6.6 с использованием RetroMCP.

## Требования

- **Операционная система**: Windows Vista и выше
- **Java**: Java 17 или выше
- **Оперативная память**: минимум 1 ГБ ОЗУ
- **Дисковое пространство**: минимум 500 МБ свободного места

## Структура проекта

```
├── conf/                    # Конфигурационные файлы RetroMCP
│   ├── exceptions.exc       # Исключения для деобфускации
│   ├── mappings.tiny        # Маппинги имен
│   └── version.json         # Информация о версии игры
├── minecraft/               # Клиентская часть
│   ├── src/                 # Декомпилированные исходные коды
│   ├── src_original/        # Оригинальные исходные коды
│   ├── output/              # Выходные файлы компиляции
│   ├── dll/                 # Нативные библиотеки
│   ├── Client.launch        # Конфигурация запуска для Eclipse
│   └── options.cfg          # Настройки клиента
├── minecraft_server/        # Серверная часть
│   ├── src/                 # Декомпилированные исходные коды
│   ├── src_original/        # Оригинальные исходные коды
│   ├── Server.launch        # Конфигурация запуска для Eclipse
│   └── options.cfg          # Настройки сервера
├── recompile.bat            # Скрипт автоматической пересборки
└── options.cfg              # Основные настройки проекта
```

## Установка

1. Убедитесь, что установлена Java 17 или выше:
   ```
   java -version
   ```

2. Скачайте и установите RetroMCP-Java-CLI.jar или RetroMCP-Java-GUI.jar

3. Поместите JAR-файл RetroMCP в корневую директорию проекта

4. При необходимости настройте параметры в файле `options.cfg`:
   - `workingDir` - путь к рабочей директории
   - `runargs` - аргументы запуска JVM (по умолчанию -Xms1024M -Xmx1024M)

## Сборка проекта

### Использование командной строки

Запустите скрипт пересборки:
```batch
recompile.bat
```

### Использование RetroMCP GUI

1. Запустите `RetroMCP-Java-GUI.jar`
2. Откройте проект через меню File -> Open
3. Нажмите кнопку "Build" для компиляции
4. Используйте "Repack" для упаковки в JAR

### Параметры сборки

В файле `options.cfg` доступны следующие опции:
- `side=ANY` - сторона сборки (CLIENT, SERVER, ANY)
- `fullbuild=false` - полная пересборка всех классов
- `stripgenerics=false` - удаление дженериков из исходного кода
- `outputsrc=true` - выводить исходные коды после деобфускации

## Запуск

### Клиент

Для запуска клиента используйте конфигурацию `Client.launch` в Eclipse или выполните:
```batch
java -cp build/minecraft.zip org.mcphackers.launchwrapper.Launch --username Player --uuid - --session - --version b1.6.6 --gameDir . --assetsDir .\assets --assetIndex b1.5 --accessToken - --userProperties {} --userType legacy --versionType release --skinProxy pre-b1.9-pre4
```

### Сервер

Для запуска сервера используйте конфигурацию `Server.launch` в Eclipse или выполните:
```batch
java -cp build/minecraft_server.zip net.minecraft.server.MinecraftServer
```

## Настройка IDE (Eclipse)

1. Импортируйте проекты из папок `minecraft/` и `minecraft_server/`
2. Убедитесь, что подключены все необходимые библиотеки из папки `libraries/`
3. Используйте файлы `.launch` для отладки и запуска

## Конфигурация

Основные параметры настраиваются в файлах:
- `options.cfg` (корень) - общие настройки проекта
- `minecraft/options.cfg` - настройки клиента
- `minecraft_server/options.cfg` - настройки сервера

Ключевые параметры:
- `lang=ENGLISH` - язык интерфейса
- `theme=com.formdev.flatlaf.FlatDarkLaf` - тема оформления GUI
- `versionUrl` - URL файла версий
- `javahome` - путь к JDK (оставьте пустым для использования системной)

## Примечания

- Проект использует маппинги из файла `conf/mappings.tiny`
- Для корректной работы требуются нативные библиотеки LWJGL
- Исходные коды расположены в соответствии со структурой пакетов Minecraft Beta 1.6.6
- Файлы игровых миров и конфиги сохраняются в папке `game/`

## Лицензия

Исходные коды Minecraft являются собственностью Mojang Studios. Данный проект предназначен исключительно для образовательных целей и изучения механизмов работы игры.

## Поддержка

Версия игры: Minecraft Beta 1.6.6  
Дата релиза: 31 мая 2011 года  
Схема конфигурации: https://meta.omniarchive.uk/schemas/v1/client.schema.json
