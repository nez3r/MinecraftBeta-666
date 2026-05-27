# Сама версия:

## Debug команды:
```batch
  1. /x<число> - Установить множитель скорости событий                                                                      - Пример: /x2, /x5, /x10
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
    - Удаляет файл locked.dat
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