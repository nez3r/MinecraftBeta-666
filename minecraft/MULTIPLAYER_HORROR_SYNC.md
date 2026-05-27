# Multiplayer Horror Synchronization System

**Дата:** 2026-05-10  
**Версия:** 1.0  
**Статус:** ✅ Готово к тестированию

---

## 📋 Обзор

Система синхронизации хоррор-эффектов в мультиплеере позволяет передавать мистические события между игроками в реальном времени. Когда у одного игрока происходит хоррор-событие, оно автоматически транслируется всем остальным игрокам на сервере.

### Принцип работы

```
Игрок A → Событие триггерится → Пакет отправляется на сервер
                                         ↓
                    Сервер получает пакет и рассылает всем
                                         ↓
                    Игрок B, C, D получают и воспроизводят эффект
```

---

## 🏗️ Архитектура

### Компоненты системы

1. **Packet250HorrorSync** - Сетевой пакет для передачи событий
2. **HorrorEventReceiver** - Обработчик входящих событий на клиенте
3. **HorrorSyncServerHandler** - Серверный ретранслятор пакетов
4. **MysticManager** - Интеграция с системой мистики

### Поток данных

```
┌─────────────────┐
│  MysticManager  │ Триггер события
│   (Client A)    │
└────────┬────────┘
         │ broadcastHorrorEvent()
         ↓
┌─────────────────┐
│ Packet250Horror │ Создание пакета
│      Sync       │
└────────┬────────┘
         │ NetworkManager.addToSendQueue()
         ↓
┌─────────────────┐
│     Server      │ Получение пакета
│  NetHandler     │
└────────┬────────┘
         │ handleHorrorSync()
         ↓
┌─────────────────┐
│ HorrorSyncServer│ Ретрансляция
│     Handler     │
└────────┬────────┘
         │ Broadcast to all except sender
         ↓
┌─────────────────┐
│  Client B, C, D │ Получение пакета
│  NetClientHandler│
└────────┬────────┘
         │ handleHorrorSync()
         ↓
┌─────────────────┐
│ HorrorEvent     │ Обработка события
│    Receiver     │
└────────┬────────┘
         │ processHorrorEvent()
         ↓
┌─────────────────┐
│  Воспроизведение│ Эффект активируется
│     эффекта     │
└─────────────────┘
```

---

## 📦 Packet250HorrorSync

### Структура пакета

```java
public class Packet250HorrorSync extends Packet {
    public String eventName;      // Название события (например, "screamer")
    public int stage;             // Этап мистики (1-4)
    public float intensity;       // Интенсивность (0.0 - 2.0)
    public String targetPlayer;   // Целевой игрок (пусто = всем)
    public String extraData;      // Дополнительные данные
    public String senderName;     // Отправитель
}
```

### Регистрация пакета

В `Packet.java` добавлена регистрация:

```java
addIdClassMapping(250, true, true, Packet250HorrorSync.class);
```

- **ID:** 250
- **Client → Server:** ✅ Да
- **Server → Client:** ✅ Да

---

## 🎮 Типы событий

### Системные DLL эффекты

| Событие | Описание | Параметры |
|---------|----------|-----------|
| `screamer` / `gdi_spam_text` | Спам страшных текстов по экрану | `extraData`: длительность (сек) |
| `tunnel_vision` | Затемнение краёв экрана | `extraData`: длительность (мс) |
| `gdi_tunnel` | GDI туннельный эффект | - |
| `gdi_glitch_screen` | Глитч экрана | - |
| `gdi_invert_screen` | Инверсия цветов | - |
| `gdi_pixel_melt` | Тающие пиксели | - |
| `flash_screen` | Мигание экрана | `extraData`: "times,interval" |
| `mouse_possession` | Дрожание курсора | `extraData`: длительность (сек) |
| `system_beep` | Системный писк | `extraData`: "frequency,duration" |
| `red_tint_screen` | Красный оттенок | `extraData`: длительность (сек) |
| `spawn_ghost_window` | Призрачное окно | - |
| `dead_pixels` | Битые пиксели | `extraData`: "count,duration" |
| `clipboard_hijack` | Подмена буфера обмена | `extraData`: текст |
| `mouse_friction` | Сопротивление мыши | `extraData`: длительность (сек) |
| `broken_clock` | Сломанные часы | `extraData`: длительность (сек) |
| `open_calculator` | Открытие калькулятора | - |
| `open_notepad` | Открытие блокнота | `extraData`: текст |
| `spam_messages` | Спам MessageBox | `extraData`: "count\|title\|message\|button" |
| `restart_explorer` | Перезапуск Explorer.exe | - |
| `fake_game_close` | Обманчивое закрытие игры | - |

### Игровые эффекты

| Событие | Описание | Длительность |
|---------|----------|--------------|
| `vhs_effect` | VHS искажение | 30 сек |
| `inversion` | Инверсия экрана | 10 сек |
| `gui_shaking` | Тряска GUI | 15 сек |
| `sky_glitch` | Глитч неба | 20 сек |
| `world_mirror` | Зеркальный мир | 15 сек |
| `fog_collapse` | Коллапс тумана | 30 сек |
| `blood_water` | Кровавая вода | 45 сек |
| `hand_decay` | Распад руки | 20 сек |
| `eyes_in_fog` | Глаза в тумане | Постоянно |
| `world_jitter` | Вибрация мира | 10 сек |
| `red_lines_pause` | Красные линии в паузе | Постоянно |
| `cryptic_hints` | Криптичные подсказки | Постоянно |
| `infinite_inventory` | Бесконечный инвентарь | Постоянно |
| `watchers` | Мобы смотрят на игрока | 60 сек |
| `entity_detector` | Детектор сущностей в F3 | Постоянно |
| `echo_sounds` | Эхо звуков | Постоянно |
| `shadow_chat` | Теневой чат | `extraData`: сообщение |
| `chat_spam` | Спам в чате | 100 сообщений |

### Мировые эффекты

| Событие | Описание |
|---------|----------|
| `sign_spawn` | Спавн знака с "404" |
| `silhouette_spawn` | Спавн силуэта |
| `mirror_player` | Спавн зеркального игрока |
| `forgotten_structures` | Забытые структуры |
| `world_erosion` | Эрозия мира (замена листвы) |
| `chunk_distortion` | Искажение чанков |
| `void_hole` | Дыра в пустоту |
| `dry_lightning` | Сухая молния |

### Звуковые эффекты

| Событие | Описание |
|---------|----------|
| `footsteps_behind` | Шаги за спиной |
| `white_noise` | Белый шум |
| `disc_13` | Проигрывание диска 13 |
| `disc_11` | Проигрывание диска 11 |

### Фейковые ошибки

| Событие | Описание |
|---------|----------|
| `fake_error` | Фейковая Java ошибка |
| `fake_gl_error` | Фейковая OpenGL ошибка |
| `fake_join_message` | Фейковое сообщение о входе |
| `fake_saving_chunks` | Фейковое сохранение чанков |

### Эффекты игрока

| Событие | Описание |
|---------|----------|
| `forced_turn` | Принудительный поворот на 180° |
| `inventory_swap` | Перемешивание инвентаря |
| `random_item` | Случайный предмет в инвентаре |
| `time_flip` | Инверсия времени |
| `control_inversion` | Инверсия управления (10 сек) |

### Финальная последовательность

| Событие | Описание |
|---------|----------|
| `death_chat` | Спам сообщений о смерти (50 шт) |
| `bsod` | Фейковый синий экран смерти |
| `final_crash` | Финальный краш с locked.dat |

---

## 💻 Использование

### Автоматическая синхронизация

События автоматически синхронизируются при триггере в `MysticManager`:

```java
private void triggerNextEvent() {
    // ... выбор события ...
    
    executeEvent(event);
    
    // Автоматическая отправка в мультиплеер
    broadcastHorrorEvent(event.name, currentStage, 1.0f);
}
```

### Ручная отправка события

```java
MysticManager manager = MysticManager.getInstance(mc);

// Отправка всем игрокам
manager.broadcastHorrorEvent("screamer", 3, 1.5f);

// Отправка конкретному игроку
manager.broadcastHorrorEvent("tunnel_vision", 2, 1.0f, "Player404");

// Отправка с дополнительными данными
manager.broadcastHorrorEventWithData("open_notepad", 3, 1.0f, "YOU ARE BEING WATCHED");
```

### Серверная отправка

```java
// Отправка события от сервера всем игрокам
HorrorSyncServerHandler.broadcastServerEvent("gdi_glitch_screen", 4, 2.0f, serverInstance);

// Отправка конкретному игроку
HorrorSyncServerHandler.broadcastToPlayer("fake_error", 2, 1.0f, "Steve", serverInstance);
```

---

## 🔧 Интеграция

### Клиентская часть

1. **NetClientHandler** - При подключении к серверу:
```java
public void handleLogin(Packet1Login var1) {
    // ... стандартная логика ...
    
    // Инициализация мультиплеера
    MysticManager mysticManager = MysticManager.getInstance(this.mc);
    mysticManager.setNetworkManager(this.netManager);
}
```

2. **NetClientHandler** - Обработка входящих пакетов:
```java
public void handleHorrorSync(Packet250HorrorSync var1) {
    HorrorEventReceiver receiver = new HorrorEventReceiver(this.mc);
    receiver.processHorrorEvent(var1);
}
```

### Серверная часть

**NetServerHandler** (требуется создать) - Обработка пакетов от клиентов:

```java
public void handleHorrorSync(Packet250HorrorSync packet) {
    // Получаем игрока-отправителя
    EntityPlayerMP sender = this.playerEntity;
    
    // Ретранслируем всем остальным
    HorrorSyncServerHandler.handleClientHorrorSync(packet, sender, this.mcServer);
}
```

---

## 🎯 Примеры сценариев

### Сценарий 1: Синхронный скример

Игрок A находит знак с "404" и кликает по нему:

1. У игрока A активируется инверсия экрана + сообщения
2. `MysticManager` отправляет пакет `Packet250HorrorSync("screamer", 1, 1.0f)`
3. Сервер получает пакет и рассылает игрокам B, C, D
4. У всех игроков одновременно запускается `GDI_SpamText(3)`
5. Все видят страшные тексты на экране в течение 3 секунд

### Сценарий 2: Целевой эффект

Игрок A хочет напугать конкретного игрока B:

```java
manager.broadcastHorrorEvent("tunnel_vision", 3, 1.5f, "PlayerB");
```

1. Пакет отправляется с `targetPlayer = "PlayerB"`
2. Сервер рассылает всем
3. Игрок B получает пакет и проверяет `targetPlayer`
4. Только у игрока B активируется туннельный эффект
5. Остальные игроки игнорируют пакет

### Сценарий 3: Серверное событие

Сервер решает напугать всех игроков одновременно:

```java
HorrorSyncServerHandler.broadcastServerEvent("gdi_glitch_screen", 4, 2.0f, server);
```

1. Сервер создаёт пакет с `senderName = "SERVER"`
2. Рассылает всем подключенным игрокам
3. У всех одновременно глитчит экран
4. Интенсивность 2.0 = усиленный эффект

### Сценарий 4: Мировые эффекты

Игрок A триггерит спавн зеркального игрока:

1. `MysticManager` спавнит `EntityMirrorPlayer` в мире игрока A
2. Отправляет пакет `Packet250HorrorSync("mirror_player", 3, 1.0f)`
3. Игроки B, C, D получают пакет
4. У каждого спавнится свой зеркальный игрок
5. Все видят своих "двойников" одновременно

---

## 🐛 Отладка

### Логирование

Все компоненты выводят отладочную информацию:

```
[MysticManager] Multiplayer mode: true
[MysticManager] Broadcasted event: screamer (stage 3)
[HorrorSync] Received event: screamer (stage 3, intensity 1.0, from PlayerA)
[HorrorSync Server] Received event 'screamer' from PlayerA (stage 3)
[HorrorSync Server] Broadcasted 'screamer' to 3 players
```

### Проверка подключения

```java
MysticManager manager = MysticManager.getInstance(mc);
if (manager.isMultiplayer()) {
    System.out.println("Multiplayer mode active");
} else {
    System.out.println("Singleplayer mode");
}
```

### Тестирование

1. Запустите локальный сервер
2. Подключите 2+ клиента
3. На одном клиенте выполните команду: `/event screamer`
4. Проверьте, что эффект появился у всех игроков
5. Проверьте логи на наличие сообщений о broadcast

---

## ⚠️ Известные ограничения

1. **DLL эффекты работают только на Windows** - Системные эффекты через `HorrorSystemDLL` требуют Windows
2. **Нет серверной валидации** - Клиент может отправлять любые события (можно добавить whitelist)
3. **Нет rate limiting** - Клиент может спамить пакетами (можно добавить throttling)
4. **Reflection для player list** - `HorrorSyncServerHandler` использует reflection для получения списка игроков
5. **Нет конфигурации** - Система всегда включена (можно добавить server.properties опцию)

---

## 🚀 Будущие улучшения

### Приоритет 1 (Критично)
- [ ] Добавить серверную валидацию событий
- [ ] Реализовать rate limiting (макс. 1 событие в 5 секунд на игрока)
- [ ] Добавить server.properties опцию `enable-horror-sync=true`

### Приоритет 2 (Важно)
- [ ] Синхронизация состояния мистики при подключении (stage, flags)
- [ ] Команды для админов: `/horror broadcast <event>`, `/horror target <player> <event>`
- [ ] Blacklist событий на сервере (запретить определённые эффекты)

### Приоритет 3 (Желательно)
- [ ] Статистика событий (кто сколько раз триггерил)
- [ ] Cooldown между событиями для каждого игрока
- [ ] Синхронизация мировых структур (знаки, силуэты)
- [ ] Replay система (запись и воспроизведение последовательности событий)

---

## 📚 API Reference

### MysticManager

```java
// Установить network manager (вызывается автоматически при подключении)
public void setNetworkManager(NetworkManager netManager)

// Проверка мультиплеера
public boolean isMultiplayer()

// Отправка события всем
public void broadcastHorrorEvent(String eventName, int stage, float intensity)

// Отправка события конкретному игроку
public void broadcastHorrorEvent(String eventName, int stage, float intensity, String targetPlayer)

// Отправка события с дополнительными данными
public void broadcastHorrorEventWithData(String eventName, int stage, float intensity, String extraData)
```

### HorrorEventReceiver

```java
// Обработка входящего события
public void processHorrorEvent(Packet250HorrorSync packet)
```

### HorrorSyncServerHandler

```java
// Обработка пакета от клиента и рассылка
public static void handleClientHorrorSync(Packet250HorrorSync packet, EntityPlayerMP sender, Object server)

// Отправка серверного события всем
public static void broadcastServerEvent(String eventName, int stage, float intensity, Object server)

// Отправка конкретному игроку
public static void broadcastToPlayer(String eventName, int stage, float intensity, String targetUsername, Object server)

// Проверка включения системы
public static boolean isHorrorSyncEnabled()
```

---

## 📞 Troubleshooting

### Проблема: События не синхронизируются

**Решение:**
1. Проверьте логи на наличие `[MysticManager] Multiplayer mode: true`
2. Убедитесь, что пакет зарегистрирован: `addIdClassMapping(250, true, true, Packet250HorrorSync.class)`
3. Проверьте, что `handleHorrorSync()` добавлен в `NetHandler` и `NetClientHandler`

### Проблема: DLL эффекты не работают у других игроков

**Решение:**
1. Убедитесь, что у всех игроков установлена `error404.dll` в `natives/`
2. Проверьте логи на `JNA loaded successfully!` и `horror_system.dll loaded successfully!`
3. DLL эффекты работают только на Windows

### Проблема: Сервер не рассылает пакеты

**Решение:**
1. Убедитесь, что серверная часть реализована (NetServerHandler)
2. Проверьте, что `HorrorSyncServerHandler.handleClientHorrorSync()` вызывается
3. Проверьте логи на `[HorrorSync Server] Broadcasted to X players`

---

## ✅ Чек-лист готовности

- [x] Packet250HorrorSync создан
- [x] Пакет зарегистрирован в Packet.java (ID 250)
- [x] handleHorrorSync() добавлен в NetHandler
- [x] handleHorrorSync() реализован в NetClientHandler
- [x] HorrorEventReceiver создан (обработка 60+ событий)
- [x] HorrorSyncServerHandler создан
- [x] MysticManager интегрирован с NetworkManager
- [x] broadcastHorrorEvent() вызывается при триггере событий
- [ ] Серверная часть (NetServerHandler) реализована
- [ ] Протестировано в мультиплеере (2+ игрока)
- [ ] Документация создана

---

## 👤 Информация

**Автор:** Claude (Anthropic)  
**Дата:** 2026-05-10  
**Версия:** 1.0  
**Проект:** Minecraft 666 Horror Mod - Multiplayer Sync

---

**Статус:** ✅ Готово к тестированию  
**Приоритет:** Высокий  
**Сложность:** Высокая  
**Время на реализацию:** ~2 часа
