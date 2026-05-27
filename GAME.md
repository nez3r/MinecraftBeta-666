# Сама версия:


## Список событий:

## Этап 1
- Sign Spawn 404 — появление таблички с надписью "404"
- Fake Java Error — фейковая ошибка Java
- Red Lines in Pause — красные линии в меню паузы
- Disc 13 — проигрывание пластинки C418 - 13
- VHS Effect — эффект VHS
- Random Item — случайный предмет в инвентаре
- GUI Shaking — тряска интерфейса
- Dry Lightning — сухие удары молнии без дождя
- Cryptic Hints — загадочные подсказки в чате
- The Watchers — наблюдатели вдали
- Window Shake — тряска окна игры

## Этап 2
- Silhouette Spawn — появление силуэта
- Time Flip — временной переворот времени
- Footsteps Behind — шаги за спиной
- Inventory Swap — перемешивание инвентаря
- Eyes in Fog — глаза в тумане
- Disc 11 — проигрывание пластинки C418 - 11
- GDI Tunnel — туннельный эффект через GDI (3 секунды)
- World Erosion — эрозия мира

## Этап 3
- Fake Join Message — фейковое сообщение о присоединении игрока
- Entity Detector — детектор сущностей
- Fake Saving Chunks — фейковое сохранение чанков
- Hand Decay — распад руки игрока
- Fake GL Error — фейковая ошибка OpenGL
- Tunnel Vision — туннельное зрение
- Open Calculator — открытие калькулятора Windows
- Spam Messages — спам всплывающими окнами (5 окон)
- Open Notepad — открытие Notepad с текстом
- GDI Glitch Attack — GDI глитч-атака (2 секунды)
- GDI Spam Text — спам страшных текстов на экране (3 секунды)
- Mouse Possession — дрожание курсора мыши (5 секунд)
- Red Tint Screen — красный оттенок экрана (10 секунд)
- Ghost Windows — призрачные окна (60 секунд)
- Dead Pixels — битые пиксели (30 секунд)
- Clipboard Hijack — подмена содержимого буфера обмена
- Mouse Friction — сопротивление движению мыши (10 секунд)
- System Beep — системный звуковой сигнал
- Fake Game Close — обманчивое закрытие игры
- Broken Clock — сломанные часы (15 секунд)
- Mirror Player — зеркальный игрок
- Forgotten Structures — забытые структуры в мире
- Echo Sounds — эхо-звуки

## Этап 4
- Chunk Distortion — искажение чанков
- Blood Water — кровавая вода
- Forced Turn — принудительный разворот на 180 градусов
- Fog Collapse — схлопывание тумана
- Sky Glitch — глитч неба
- Infinite Inventory — бесконечный инвентарь
- Restart Explorer — перезапуск проводника Windows
- Death Chat — сообщения о смерти в чате
- White Noise — белый шум
- Screamer Interface — скример через интерфейс
- Fake BSOD — фейковый синий экран смерти (BSOD)
- Void Hole — дыра в пустоту
- Chat Spam — спам в чате
- Control Inversion — инверсия управления
- Shadow Chat — теневой чат
- World Jitter — дрожание мира
- Bedrock Tunnel — бедроковый туннель
- Final Crash — финальный краш игры (однократное событие)

## DLL функции (системные эффекты)
- StartTunnelVision / StopTunnelVision — управление эффектом туннельного зрения
- ShowErrorMessage — показ системного окна ошибки
- ShowWarning — показ предупреждения "404"
- GDI_InvertTunnel — инвертированный туннельный эффект
- GDI_TunnelEffect — туннельный эффект через GDI
- FlashScreen — мигание экрана
- OpenNotepadWithText — открытие Notepad с заданным текстом
- OpenCalculator — открытие калькулятора
- RestartExplorer — перезапуск explorer.exe
- SpamMessageBoxes — спам окнами сообщений
- ChangeWallpaperTemporary — временная смена обоев рабочего стола
- RestoreAll — восстановление всех системных изменений
- GDI_PixelMelt — эффект "плавления" пикселей
- GDI_GlitchScreen — глитч экрана через GDI
- GDI_InvertScreen — инверсия цветов экрана
- GDI_SpamText — спам текстом на экране
- MousePossession — одержимость мышью (дрожание курсора)
- SystemBeep — системный звуковой сигнал Beep
- RedTintScreen — красный оттенок экрана
- SpawnGhostWindow — создание призрачного окна
- DeadPixels — эффект битых пикселей
- ClipboardHijack — перехват буфера обмена
- MouseFriction — сопротивление мыши
- FakeGameClose — фейковое закрытие игры


## Debug команды:
```batch
  1. /x<число> - Установить множитель скорости событий.
    - Ускоряет появление мистических событий
  2. /next - Принудительно запустить следующее случайное событие
    - Выбирает доступное событие из текущего этапа
    - Показывает информацию о повторениях для повторяющихся событий
  3. /mlvl [1-4] - Установить/показать уровень мистики
    - /mlvl - показать текущий уровень
    - /mlvl 1-4 - установить уровень (1-4)
    - Каждый уровень меняет искажение звука:
        - Уровень 1: 1.0x
      - Уровень 2: 1.3x
      - Уровень 3: 1.7x
      - Уровень 4: 2.2x
  4. /mst - Показать статус мистической системы
    - Текущий этап
    - Скорость событий
    - Искажение звука
    - Количество событий (одноразовых и повторяющихся)
    - Статус End
  5. /mreset - Полный сброс мистического состояния
    - Удаляет файл locked.dat из финального краша
    - Сбрасывает все флаги и эффекты
    - Возвращает на Stage 1
    - Сбрасывает все таймеры

  Команды управления предметами:

  8. /event item <item_id> - Выдать предмет себе по ID
    - Пример: /event item 1 (камень)
  9. /event item <player> <item_id> - Выдать предмет игроку
    - Пример: /event item Steve 1
  10. /event item_set <set_name> - Выдать набор предметов
    - Доступные наборы определены в коде
  11. /event item clear - Очистить инвентарь
  12. /event item px-all - Выдать кирку, ломающую всё

  Команды управления временем:

  13. /event time set day - Установить день (время 1000)
  14. /event time set night - Установить ночь (время 13000)

  Команда запуска событий:

  15. /event <event_name> - Запустить конкретное событие по имени

  Доступные события для /event:

  Stage 1:
  - sign_spawn, fake_error, red_lines_pause, disc_13, vhs_effect, random_item, gui_shaking, dry_lightning,
  cryptic_hints, watchers, window_shake, silhouette_spawn, time_flip, footsteps_behind, inventory_swap, eyes_in_fog,
  disc_11, gdi_tunnel, world_erosion

  Stage 2:
  - fake_join_message, entity_detector, fake_saving_chunks, hand_decay, fake_gl_error, tunnel_vision, open_calculator,
  spam_messages, open_notepad, gdi_glitch_attack, gdi_spam_text, mouse_possession, red_tint_screen, ghost_windows,
  dead_pixels, clipboard_hijack, mouse_friction, system_beep, fake_game_close, broken_clock, mirror_player,
  forgotten_structures, echo_sounds

  Stage 3:
  - chunk_distortion, blood_water, forced_turn, fog_collapse, sky_glitch, infinite_inventory, restart_explorer,
  world_mirror, death_chat, white_noise, screamer_interface, fake_bsod, void_hole, chat_spam, control_inversion,
  shadow_chat, world_jitter, bedrock_tunnel

  Stage 4:
  - final_crash
```

#### Особенности:
  - В мультиплеере команды /next, /mst, /mlvl, /event, /x