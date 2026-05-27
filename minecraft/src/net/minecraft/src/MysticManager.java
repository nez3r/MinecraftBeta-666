package net.minecraft.src;

import java.io.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

public class MysticManager {

    private static MysticManager instance;
    private Minecraft mc;
    private boolean initialized = false;

    // Этапы мистики
    public int currentStage = 1;

    // Таймеры (используем тики вместо миллисекунд)
    private long gameStartTick = 0;
    private long lastEventTick = 0;
    private long nextEventDelayTicks = 0;

    // Множители скорости
    private float speedMultiplier = 2.0f;

    // Списки событий для каждого этапа
    private List<MysticEvent> stage1Events = new ArrayList<MysticEvent>();
    private List<MysticEvent> stage2Events = new ArrayList<MysticEvent>();
    private List<MysticEvent> stage3Events = new ArrayList<MysticEvent>();
    private List<MysticEvent> stage4Events = new ArrayList<MysticEvent>();

    private int currentEventIndex = 0;
    public Random random = new Random();

    // Multiplayer support
    private boolean isMultiplayer = false;
    private NetworkManager networkManager = null;

    // Флаги состояния
    public boolean isEndMode = false;
    public boolean isGlitchMode = false;
    public boolean isInversionActive = false;
    public boolean isVHSActive = false;
    public boolean isSkyGlitchActive = false;
    public boolean isWorldMirrorActive = false;
    public boolean isFogCollapseActive = false;
    public boolean isInfiniteInventoryActive = false;
    public boolean isRedLinesPauseActive = false;
    public boolean isGuiShakingActive = false;
    public boolean isCrypticHintsActive = false;
    public boolean isWatchersActive = false;
    public boolean isFakeEntityDetectorActive = false;
    public boolean isHandDecayActive = false;
    public boolean isEchoSoundsActive = false;
    public boolean isBloodWaterActive = false;
    public boolean isChatSpamActive = false;
    public boolean isEyesInFogActive = false;
    public boolean isWorldJitterActive = false;
    public boolean isTunnelVisionActive = false;

    // Финальная последовательность
    private boolean finalSequenceActive = false;
    private long finalSequenceStartTime = 0;
    private int finalGlitchCounter = 0;

    // Бедроковый туннель
    private boolean bedrockTunnelActive = false;
    private int tunnelSignZ = 0;

    // Тряска окна
    private boolean intensiveWindowShakeActive = false;
    private long intensiveWindowShakeEndTime = 0;

    // Для shadow chat
    public boolean isShadowChatActive = false;
    public String shadowChatMessage = "";
    public long shadowChatTime = 0;

    // Для echo sounds
    private java.util.Queue<EchoSound> echoSoundQueue = new java.util.LinkedList<EchoSound>();

    private static class EchoSound {
        String sound;
        long playTime;

        EchoSound(String sound, long playTime) {
            this.sound = sound;
            this.playTime = playTime;
        }
    }

    // Таймеры для временных эффектов (в тиках)
    public long vhsEndTime = 0;
    public long inversionEndTime = 0;
    public long skyGlitchEndTime = 0;
    public long worldMirrorEndTime = 0;
    public long fogCollapseEndTime = 0;
    public long watchersEndTime = 0;
    public long handDecayEndTime = 0;
    public long bloodWaterEndTime = 0;
    public long guiShakingEndTime = 0;
    public long worldJitterEndTime = 0;

    // Для тряски окна
    private long lastWindowShakeTime = 0;
    public int originalX = 0;
    public int originalY = 0;
    private boolean windowShakeInitialized = false;

    // Для изменения названия окна
    private long lastTitleChangeTime = 0;
    private String originalTitle = "Minecraft Beta 1.6.6";

    // Для искажения звуков
    public float soundDistortion = 1.0f;

    // Переменные для отложенных действий
    private long timeFlipRevertTick = 0;
    private long timeFlipOriginalTime = 0;
    private boolean timeFlipPending = false;
    private boolean timeFlipCrashPending = false;
    private long timeFlipCrashTick = 0;

    // Переменные для footsteps
    private long footstepsStartTick = 0;
    private boolean footstepsActive = false;
    private int footstepsCounter = 0;

    // Переменные для fake join message
    private long fakeJoinMessageTick1 = 0;
    private long fakeJoinMessageTick2 = 0;
    private boolean fakeJoinMessage1Pending = false;
    private boolean fakeJoinMessage2Pending = false;

    // Переменные для distortChunk
    private boolean distortChunkActive = false;
    private long distortChunkEndTick = 0;
    private int distortChunkX = 0;
    private int distortChunkZ = 0;
    private int[][][] distortChunkOriginalBlocks = null;

    // Переменные для deathChat
    private boolean deathChatActive = false;
    private int deathChatCounter = 0;
    private long deathChatLastTick = 0;

    // Переменные для worldEater
    private boolean worldEaterActive = false;
    private int worldEaterRadius = 5;
    private long worldEaterLastTick = 0;
    private int worldEaterPlayerX = 0;
    private int worldEaterPlayerY = 0;
    private int worldEaterPlayerZ = 0;

    // Переменные для chatSpam
    private boolean chatSpamActive = false;
    private int chatSpamCounter = 0;
    private long chatSpamLastTick = 0;

    // Флаг регистрации кастомных звуков
    private boolean customSoundsRegistered = false;

    // Для 404 скримера
    private boolean screamer404Active = false;
    private int screamer404Counter = 0;
    private long screamer404StartTime = 0;
    private boolean screamer404ShouldCrash = false; // Флаг для краша после скримера

    // Для моргания инверсии при клике по табличке
    private boolean signInversionActive = false;
    private long signInversionStartTime = 0;
    private static final long SIGN_INVERSION_DURATION = 5000; // 5 секунд
    private static final int FLASH_INTERVAL = 100; // Моргание каждые 100мс

    // Для агрессивных мобов
    private Map<EntityLiving, Long> aggressiveMobs = new HashMap<EntityLiving, Long>();

    private MysticManager() {}

    public static MysticManager getInstance() {
        if (instance == null) {
            instance = new MysticManager();
        }
        return instance;
    }

    public Minecraft getMinecraft() {
        return mc;
    }

    public boolean isInitialized() {
        return initialized && mc != null;
    }

    public void init(Minecraft minecraft) {
        this.mc = minecraft;
        this.gameStartTick = 0; // Будет установлен в первом update()
        this.lastEventTick = 0;

        // Загружаем данные мистики из мира
        loadFromWorld();

        // Проверка на locked.dat при запуске
        checkLockFile();

        // НЕ регистрируем звуки здесь - они будут зарегистрированы в update()
        // после того, как ванильные звуки загрузятся

        // Инициализация событий
        initializeEvents();

        // Установка первого интервала
        scheduleNextEvent();

        // Помечаем как инициализированный
        this.initialized = true;
    }

    private boolean isMultiplayer() {
        return mc != null && mc.theWorld != null && mc.theWorld.multiplayerWorld;
    }

    public void trigger404Screamer() {
        trigger404Screamer(false);
    }

    public void trigger404Screamer(boolean shouldCrash) {
        screamer404Active = true;
        screamer404Counter = 0;
        screamer404StartTime = System.currentTimeMillis();
        screamer404ShouldCrash = shouldCrash;
    }

    private String getEventNameRussian(String eventName) {
        switch (eventName) {
            // Stage 1
            case "sign_spawn": return "Sign Spawn 404";
            case "fake_error": return "Fake Java Error";
            case "red_lines_pause": return "Red Lines in Pause";
            case "disc_13": return "Disc 13";
            case "vhs_effect": return "VHS Effect";
            case "random_item": return "Random Item";
            case "gui_shaking": return "GUI Shaking";
            case "dry_lightning": return "Dry Lightning";
            case "cryptic_hints": return "Cryptic Hints";
            case "watchers": return "The Watchers";
            case "window_shake": return "Window Shake";
            // Stage 2
            case "silhouette_spawn": return "Silhouette Spawn";
            case "time_flip": return "Time Flip";
            case "footsteps_behind": return "Footsteps Behind";
            case "inventory_swap": return "Inventory Swap";
            case "eyes_in_fog": return "Eyes in Fog";
            case "disc_11": return "Disc 11";
            case "gdi_tunnel": return "GDI Tunnel";
            case "world_erosion": return "World Erosion";
            // Stage 3
            case "fake_join_message": return "Fake Join Message";
            case "entity_detector": return "Entity Detector";
            case "fake_saving_chunks": return "Fake Saving Chunks";
            case "hand_decay": return "Hand Decay";
            case "fake_gl_error": return "Fake GL Error";
            case "tunnel_vision": return "Tunnel Vision";
            case "open_calculator": return "Open Calculator";
            case "spam_messages": return "Spam Messages";
            case "open_notepad": return "Open Notepad";
            case "gdi_glitch_attack": return "GDI Glitch Attack";
            case "gdi_spam_text": return "GDI Spam Text";
            case "mouse_possession": return "Mouse Possession";
            case "red_tint_screen": return "Red Tint Screen";
            case "ghost_windows": return "Ghost Windows";
            case "dead_pixels": return "Dead Pixels";
            case "clipboard_hijack": return "Clipboard Hijack";
            case "mouse_friction": return "Mouse Friction";
            case "system_beep": return "System Beep";
            case "fake_game_close": return "Fake Game Close";
            case "broken_clock": return "Broken Clock";
            case "mirror_player": return "Mirror Player";
            case "forgotten_structures": return "Forgotten Structures";
            case "echo_sounds": return "Echo Sounds";
            // Stage 4
            case "chunk_distortion": return "Chunk Distortion";
            case "blood_water": return "Blood Water";
            case "forced_turn": return "Forced Turn 180";
            case "fog_collapse": return "Fog Collapse";
            case "sky_glitch": return "Sky Glitch";
            case "infinite_inventory": return "Infinite Inventory";
            case "restart_explorer": return "Restart Explorer";
            case "world_mirror": return "World Mirror";
            case "death_chat": return "Death Chat";
            case "white_noise": return "White Noise";
            case "screamer_interface": return "Screamer Interface";
            case "fake_bsod": return "Fake BSOD";
            case "void_hole": return "Void Hole";
            case "chat_spam": return "Chat Spam";
            case "control_inversion": return "Control Inversion";
            case "shadow_chat": return "Shadow Chat";
            case "world_jitter": return "World Jitter";
            case "bedrock_tunnel": return "Bedrock Tunnel";
            case "final_crash": return "Final Crash";
            default: return eventName;
        }
    }

    private void registerCustomSounds() {
        try {
            // Рабочая директория - game/, поэтому ищем относительно неё
            File resourcesDir = new File("resources/newsound/glitches");

            if (!resourcesDir.exists()) {
                // Пробуем абсолютный путь через Minecraft dir
                File minecraftDir = Minecraft.getMinecraftDir();
                resourcesDir = new File(minecraftDir, "resources/newsound/glitches");
            }

            if (!resourcesDir.exists()) {
                // Пробуем путь относительно родительской директории
                resourcesDir = new File("../game/resources/newsound/glitches");
            }

            if (resourcesDir.exists() && resourcesDir.isDirectory()) {
                File[] soundFiles = resourcesDir.listFiles();

                if (soundFiles != null) {
                    for (File soundFile : soundFiles) {
                        if (!soundFile.isFile()) continue;

                        String name = soundFile.getName();
                        String nameLower = name.toLowerCase();

                        // Поддержка только .wav и .ogg
                        if (!nameLower.endsWith(".wav") && !nameLower.endsWith(".ogg")) {
                            continue;
                        }

                        // Регистрируем звук - SoundPool автоматически обрежет расширение
                        // Поэтому передаем полное имя файла с расширением
                        String soundPath = "glitches/" + name;
                        mc.sndManager.addSound(soundPath, soundFile);
                    }
                }
            }

            // Регистрируем музыку из resources/music
            File musicDir = new File("resources/music");
            if (!musicDir.exists()) {
                File minecraftDir = Minecraft.getMinecraftDir();
                musicDir = new File(minecraftDir, "resources/music");
            }
            if (!musicDir.exists()) {
                musicDir = new File("../game/resources/music");
            }

            if (musicDir.exists() && musicDir.isDirectory()) {
                File[] musicFiles = musicDir.listFiles();

                if (musicFiles != null) {
                    for (File musicFile : musicFiles) {
                        if (!musicFile.isFile()) continue;

                        String name = musicFile.getName();
                        if (name.endsWith(".ogg")) {
                            // Передаем оригинальное имя файла
                            mc.sndManager.addMusic(name, musicFile);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkLockFile() {
        File lockFile = new File("game/locked.dat");
        if (lockFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(lockFile));
                int attempts = Integer.parseInt(reader.readLine().trim());
                reader.close();

                if (attempts < 5) {
                    attempts++;
                    PrintWriter writer = new PrintWriter(new FileWriter(lockFile));
                    writer.println(attempts);
                    writer.close();

                    // Разные сообщения в зависимости от попытки
                    if (attempts == 2) {
                        // Фейковая переустановка System32
                        showFakeSystem32Deletion();
                    } else if (attempts == 3) {
                        // Окно "File Not Found"
                        javax.swing.JOptionPane.showMessageDialog(null,
                            "The file 'minecraft.exe' could not be found.",
                            "File Not Found",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    } else {
                        // Обычное сообщение
                        javax.swing.JOptionPane.showMessageDialog(null,
                            "Stay away.",
                            "again 5?",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    }

                    System.exit(0);
                } else {
                    this.isEndMode = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showFakeSystem32Deletion() {
        try {
            // Создаем окно с имитацией удаления System32
            javax.swing.JFrame frame = new javax.swing.JFrame("mnmnmnmn");
            frame.setSize(500, 300);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);

            javax.swing.JTextArea textArea = new javax.swing.JTextArea();
            textArea.setEditable(false);
            textArea.setBackground(java.awt.Color.BLACK);
            textArea.setForeground(java.awt.Color.WHITE);
            textArea.setFont(new java.awt.Font("Console", java.awt.Font.PLAIN, 12));

            frame.add(new javax.swing.JScrollPane(textArea));
            frame.setVisible(true);

            String[] messages = {
                "You need to log in to the program five times to gain access.",
                "Don't go in there, I regret it."
            };

            for (String msg : messages) {
                textArea.append(msg + "\n");
                Thread.sleep(1000);
            }

            Thread.sleep(5000);
            frame.dispose();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeEvents() {
        // Этап 1 события (5-7 минут)
        stage1Events.add(new MysticEvent("sign_spawn", 1, true)); // повторяющееся
        stage1Events.add(new MysticEvent("fake_error", 1, true)); // теперь повторяющееся
        stage1Events.add(new MysticEvent("red_lines_pause", 1, true)); // теперь повторяющееся
        stage1Events.add(new MysticEvent("disc_13", 1, true)); // теперь повторяющееся
        stage1Events.add(new MysticEvent("vhs_effect", 1, true)); // теперь повторяющееся
        stage1Events.add(new MysticEvent("random_item", 1, true)); // теперь повторяющееся
        stage1Events.add(new MysticEvent("gui_shaking", 1, true)); // теперь повторяющееся
        stage1Events.add(new MysticEvent("dry_lightning", 1, true)); // теперь повторяющееся
        stage1Events.add(new MysticEvent("cryptic_hints", 1, true)); // теперь повторяющееся
        stage1Events.add(new MysticEvent("watchers", 1, true)); // теперь повторяющееся
        stage1Events.add(new MysticEvent("window_shake", 1, true)); // теперь повторяющееся

        // Этап 2 события (7-9 минут)
        stage2Events.add(new MysticEvent("silhouette_spawn", 2, true)); // повторяющееся
        stage2Events.add(new MysticEvent("time_flip", 2, true)); // теперь повторяющееся
        stage2Events.add(new MysticEvent("footsteps_behind", 2, true)); // теперь повторяющееся
        stage2Events.add(new MysticEvent("inventory_swap", 2, true)); // теперь повторяющееся
        stage2Events.add(new MysticEvent("eyes_in_fog", 2, true)); // теперь повторяющееся
        stage2Events.add(new MysticEvent("disc_11", 2, true)); // теперь повторяющееся
        stage2Events.add(new MysticEvent("gdi_tunnel", 2, true)); // Туннельный эффект 3 секунды, теперь повторяющееся
        stage2Events.add(new MysticEvent("world_erosion", 2, true)); // повторяющееся

        // Этап 3 события (13-16 минут)
        stage3Events.add(new MysticEvent("fake_join_message", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("entity_detector", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("fake_saving_chunks", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("hand_decay", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("fake_gl_error", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("tunnel_vision", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("open_calculator", 3, true)); // Открытие калькулятора, теперь повторяющееся
        stage3Events.add(new MysticEvent("spam_messages", 3, true)); // Спам MessageBox (5 окон), теперь повторяющееся
        stage3Events.add(new MysticEvent("open_notepad", 3, true)); // Открытие Notepad с текстом, теперь повторяющееся
        stage3Events.add(new MysticEvent("gdi_glitch_attack", 3, true)); // GDI глитч атака (2 секунды), теперь повторяющееся
        stage3Events.add(new MysticEvent("gdi_spam_text", 3, true)); // Спам страшных текстов (3 секунды), теперь повторяющееся
        stage3Events.add(new MysticEvent("mouse_possession", 3, true)); // Курсор дрожит (5 секунд), теперь повторяющееся
        stage3Events.add(new MysticEvent("red_tint_screen", 3, true)); // Красный экран (10 секунд), теперь повторяющееся
        stage3Events.add(new MysticEvent("ghost_windows", 3, true)); // Призрачные окна (60 секунд), теперь повторяющееся
        stage3Events.add(new MysticEvent("dead_pixels", 3, true)); // Битые пиксели (30 секунд), теперь повторяющееся
        stage3Events.add(new MysticEvent("clipboard_hijack", 3, true)); // Подмена буфера обмена, теперь повторяющееся
        stage3Events.add(new MysticEvent("mouse_friction", 3, true)); // Сопротивление мыши (10 секунд), теперь повторяющееся
        stage3Events.add(new MysticEvent("system_beep", 3, true)); // Системный писк, теперь повторяющееся
        stage3Events.add(new MysticEvent("fake_game_close", 3, true)); // Обманчивое закрытие игры, теперь повторяющееся
        stage3Events.add(new MysticEvent("broken_clock", 3, true)); // Сломанные часы (15 секунд), теперь повторяющееся
        stage3Events.add(new MysticEvent("mirror_player", 3, true)); // повторяющееся
        stage3Events.add(new MysticEvent("forgotten_structures", 3, true)); // повторяющееся
        stage3Events.add(new MysticEvent("echo_sounds", 3, true)); // теперь повторяющееся

        // Этап 4 события (18-20 минут)
        stage4Events.add(new MysticEvent("chunk_distortion", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("blood_water", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("forced_turn", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("fog_collapse", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("sky_glitch", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("infinite_inventory", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("restart_explorer", 4, true)); // Перезапуск Explorer.exe, теперь повторяющееся
        stage4Events.add(new MysticEvent("death_chat", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("white_noise", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("screamer_interface", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("fake_bsod", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("void_hole", 4, true)); // повторяющееся
        stage4Events.add(new MysticEvent("chat_spam", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("control_inversion", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("shadow_chat", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("world_jitter", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("bedrock_tunnel", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("final_crash", 4)); // только один раз
    }

    public void update() {
        // Обработка 404 скримера ВСЕГДА (даже без мира)
        if (screamer404Active) {
            updateScreamer404();
        }

        // Обработка моргания инверсии от таблички - вызываем для обновления состояния
        if (signInversionActive) {
            isSignInversionActive(); // Обновляет isInversionActive
        }

        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        // Инициализация gameStartTick при первом вызове
        if (gameStartTick == 0) {
            gameStartTick = mc.theWorld.getWorldTime();
            lastEventTick = gameStartTick;
            scheduleNextEvent();
            saveToWorld(); // Сохраняем начальное состояние
        }

        long currentTick = mc.theWorld.getWorldTime();

        // Периодическое сохранение (каждые 5 минут = 6000 тиков)
        if (currentTick % 6000 == 0) {
            saveToWorld();
        }

        // Регистрация кастомных звуков после небольшой задержки (100 тиков = 5 секунд)
        // Это гарантирует, что ванильные звуки уже загрузились
        if (!customSoundsRegistered && (currentTick - gameStartTick) > 100) {
            registerCustomSounds();
            customSoundsRegistered = true;
        }

        // Обработка финальной последовательности
        if (finalSequenceActive) {
            updateFinalSequence();
            return; // Не обрабатываем остальное во время финала
        }

        // Постоянные эффекты
        updateWindowShake();
        updateWindowTitle();
        updateAggressiveMobs();
        updateSoundDistortion();

        // Проверка временных эффектов (используем тики: 20 тиков = 1 секунда)
        if (isVHSActive && currentTick > vhsEndTime) {
            isVHSActive = false;
        }
        if (isInversionActive && currentTick > inversionEndTime) {
            isInversionActive = false;
        }
        if (isSkyGlitchActive && currentTick > skyGlitchEndTime) {
            isSkyGlitchActive = false;
        }
        if (isWorldMirrorActive && currentTick > worldMirrorEndTime) {
            isWorldMirrorActive = false;
        }
        if (isFogCollapseActive && currentTick > fogCollapseEndTime) {
            isFogCollapseActive = false;
        }
        if (isWatchersActive && currentTick > watchersEndTime) {
            isWatchersActive = false;
            isEyesInFogActive = false;
        }
        if (isHandDecayActive && currentTick > handDecayEndTime) {
            isHandDecayActive = false;
        }
        if (isBloodWaterActive && currentTick > bloodWaterEndTime) {
            isBloodWaterActive = false;
        }
        if (isGuiShakingActive && currentTick > guiShakingEndTime) {
            isGuiShakingActive = false;
        }
        if (isWorldJitterActive && currentTick > worldJitterEndTime) {
            isWorldJitterActive = false;
        }

        // Обработка отложенных действий
        if (timeFlipPending && currentTick >= timeFlipRevertTick) {
            if (mc.theWorld != null) {
                mc.theWorld.setWorldTime(timeFlipOriginalTime);
            }
            timeFlipPending = false;
        }
        if (timeFlipCrashPending && currentTick >= timeFlipCrashTick) {
            if (mc.theWorld != null) {
                mc.theWorld.saveWorld(true, null);
                mc.changeWorld1(null);
                mc.displayGuiScreen(new GuiMainMenu());
            }
            timeFlipCrashPending = false;
        }

        // Обработка footsteps
        if (footstepsActive) {
            // Каждые 10 тиков (0.5 сек) воспроизводим звук
            if (currentTick % 10 == 0 && mc.thePlayer != null && mc.theWorld != null) {
                double distance = 3.0 + random.nextDouble() * 2.0;
                double yaw = Math.toRadians(mc.thePlayer.rotationYaw + 180);
                double x = mc.thePlayer.posX + Math.sin(yaw) * distance;
                double y = mc.thePlayer.posY;
                double z = mc.thePlayer.posZ - Math.cos(yaw) * distance;

                if (mc.thePlayer.motionX != 0 || mc.thePlayer.motionZ != 0) {
                    mc.theWorld.playSoundEffect(x, y, z, "step.grass", 0.8f, 1.0f);
                }

                footstepsCounter++;
                if (footstepsCounter >= 30) { // 15 секунд (30 * 0.5 сек)
                    footstepsActive = false;
                    footstepsCounter = 0;
                }
            }
        }

        // Обработка fake join messages
        if (fakeJoinMessage1Pending && currentTick >= fakeJoinMessageTick1) {
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage("\u00A7fPlayer404: Hello?");
            }
            fakeJoinMessage1Pending = false;
        }
        if (fakeJoinMessage2Pending && currentTick >= fakeJoinMessageTick2) {
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage("\u00A7ePlayer404 left the game");
            }
            fakeJoinMessage2Pending = false;
        }

        // Обработка distortChunk
        if (distortChunkActive && currentTick >= distortChunkEndTick) {
            // Возвращаем блоки обратно - используем setBlock для временного восстановления
            if (mc.theWorld != null && distortChunkOriginalBlocks != null) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 128; y++) {
                        for (int z = 0; z < 16; z++) {
                            mc.theWorld.setBlock(distortChunkX + x, y, distortChunkZ + z, distortChunkOriginalBlocks[x][y][z]);
                        }
                    }
                }
            }
            distortChunkActive = false;
            distortChunkOriginalBlocks = null;
        }

        // Обработка deathChat (50 сообщений каждые 2 тика)
        if (deathChatActive) {
            if (currentTick - deathChatLastTick >= 2) {
                if (mc.thePlayer != null && deathChatCounter < 50) {
                    String username = System.getProperty("user.name");
                    String[] messages = {
                        "\u00A7cDeleting C:/Users/" + username + "/Documents...",
                        "\u00A7cDeleting C:/Users/" + username + "/AppData...",
                        "\u00A7cAccess denied: C:/Users/" + username + "/Desktop",
                        "\u00A7cSystem32 corrupted",
                        "\u00A7cPlayer404: I see you, " + username,
                        "\u00A7cPlayer404: Hello " + username,
                        "\u00A7cERROR: Memory leak detected",
                        "\u00A7cjava.lang.NullPointerException at World.class:404",
                        "\u00A7cat net.minecraft.src.EntityPlayer.onDeath(Unknown Source)",
                        "\u00A7cat net.minecraft.src.World.removeEntity(Unknown Source)",
                        "\u00A7cFATAL: Heap space exceeded",
                        "\u00A7cWARNING: Unauthorized access from " + username,
                        "\u00A7c[SYSTEM] Formatting C:\\ drive...",
                        "\u00A7cPlayer404: Did you miss me?",
                        "\u00A7cERROR: File not found: reality.dll"
                    };
                    mc.thePlayer.addChatMessage(messages[random.nextInt(messages.length)]);
                    deathChatCounter++;
                    deathChatLastTick = currentTick;
                } else {
                    deathChatActive = false;
                    isChatSpamActive = false;
                }
            }
        }

        // Обработка worldEater (удаление блоков каждые 10 тиков)
        if (worldEaterActive) {
            if (currentTick - worldEaterLastTick >= 10 && worldEaterRadius < 25) {
                for (int x = worldEaterPlayerX - worldEaterRadius; x <= worldEaterPlayerX + worldEaterRadius; x++) {
                    for (int z = worldEaterPlayerZ - worldEaterRadius; z <= worldEaterPlayerZ + worldEaterRadius; z++) {
                        for (int y = worldEaterPlayerY - 5; y <= worldEaterPlayerY + 10; y++) {
                            if (mc.theWorld != null) {
                                mc.theWorld.setBlockWithNotify(x, y, z, 0);
                            }
                        }
                    }
                }
                worldEaterRadius++;
                worldEaterLastTick = currentTick;
            } else if (worldEaterRadius >= 25) {
                worldEaterActive = false;
            }
        }

        // Обработка chatSpam (100 сообщений каждый тик)
        if (chatSpamActive) {
            if (currentTick - chatSpamLastTick >= 1 && chatSpamCounter < 100) {
                if (mc.thePlayer != null) {
                    StringBuilder spam = new StringBuilder("\u00A7c");
                    for (int j = 0; j < 20; j++) {
                        spam.append((char)(random.nextInt(94) + 33));
                    }
                    mc.thePlayer.addChatMessage(spam.toString());
                    chatSpamCounter++;
                    chatSpamLastTick = currentTick;
                }
            } else if (chatSpamCounter >= 100) {
                chatSpamActive = false;
            }
        }

        // Обработка shadow chat
        if (isShadowChatActive && shadowChatTime > 0 && System.currentTimeMillis() >= shadowChatTime) {
            if (mc.thePlayer != null && !shadowChatMessage.isEmpty()) {
                // Искажаем сообщение
                StringBuilder distorted = new StringBuilder();
                for (char c : shadowChatMessage.toCharArray()) {
                    if (random.nextFloat() < 0.3f) {
                        distorted.append((char)(random.nextInt(94) + 33));
                    } else {
                        distorted.append(c);
                    }
                }
                mc.thePlayer.addChatMessage("\u00A77<Player404> " + distorted.toString());
            }
            shadowChatMessage = "";
            shadowChatTime = 0;
            isShadowChatActive = false;
        }

        // Обработка echo sounds
        if (isEchoSoundsActive && !echoSoundQueue.isEmpty()) {
            EchoSound nextEcho = echoSoundQueue.peek();
            if (nextEcho != null && System.currentTimeMillis() >= nextEcho.playTime) {
                echoSoundQueue.poll();
                if (mc.sndManager != null) {
                    mc.sndManager.playSoundFX(nextEcho.sound, 0.5f, 0.8f);
                }
            }
        }

        // Проверка приближения к табличке в туннеле
        if (bedrockTunnelActive && mc.thePlayer != null) {
            double distanceToSign = Math.abs(mc.thePlayer.posZ - tunnelSignZ);
            if (distanceToSign < 3.0) {
                // Игрок близко к табличке - запускаем краш с locked.dat
                bedrockTunnelActive = false;
                triggerFinalCrashWithLock();
            }
        }

        // Проверка на следующее событие (ТОЛЬКО В ОДИНОЧНОЙ ИГРЕ)
        if (!isMultiplayer) {
            long ticksSinceLastEvent = currentTick - lastEventTick;

            if (ticksSinceLastEvent >= nextEventDelayTicks) {
                triggerNextEvent();
                lastEventTick = currentTick;
                scheduleNextEvent();
            }

            // Прогрессия этапов
            updateStageProgression();
        }

        // В мультиплеере временные эффекты продолжают работать, но новые события не генерируются
        return;
    }

    // Продолжение для одиночной игры (после return выше для мультиплеера)
    private void updateSingleplayerEffects() {
        // Эта функция больше не нужна, логика перенесена в update()
    }

    private void updateStageProgression() {
        // Прогрессия этапов теперь управляется только через исчерпание событий
        // Автоматическая прогрессия по времени отключена
    }

    private void scheduleNextEvent() {
        int minDelayTicks = 0;
        int maxDelayTicks = 0;

        // 20 тиков = 1 секунда, 1200 тиков = 1 минута
        // Рассчитано для ~45 минут прохождения при x2 скорости
        switch (currentStage) {
            case 1:
                minDelayTicks = 40 * 20; // 40 секунд
                maxDelayTicks = 70 * 20; // 70 секунд
                break;
            case 2:
                minDelayTicks = 35 * 20; // 35 секунд
                maxDelayTicks = 60 * 20; // 60 секунд
                break;
            case 3:
                minDelayTicks = 20 * 20; // 20 секунд
                maxDelayTicks = 40 * 20; // 40 секунд
                break;
            case 4:
                minDelayTicks = 15 * 20; // 15 секунд
                maxDelayTicks = 30 * 20; // 30 секунд
                break;
        }

        // Применяем множитель скорости
        minDelayTicks = (int)(minDelayTicks / speedMultiplier);
        maxDelayTicks = (int)(maxDelayTicks / speedMultiplier);

        // Защита от деления на ноль
        if (maxDelayTicks <= minDelayTicks) {
            maxDelayTicks = minDelayTicks + 1;
        }

        nextEventDelayTicks = minDelayTicks + random.nextInt(maxDelayTicks - minDelayTicks);
    }

    private void triggerNextEvent() {
        List<MysticEvent> currentEvents = getCurrentStageEvents();

        if (currentEvents.isEmpty()) {
            return;
        }

        // Фильтруем события, которые еще можно активировать
        List<MysticEvent> availableEvents = new ArrayList<MysticEvent>();
        for (MysticEvent event : currentEvents) {
            if (event.canTrigger()) {
                availableEvents.add(event);
            }
        }


        // Если нет доступных событий, переходим на следующий этап
        if (availableEvents.isEmpty() && currentStage < 4) {
            currentStage++;

            // Обновляем искажение звука
            if (currentStage == 2) {
                soundDistortion = 1.3f;
            } else if (currentStage == 3) {
                soundDistortion = 1.7f;
            } else if (currentStage == 4) {
                soundDistortion = 2.2f;
            }

            // Планируем следующее событие
            scheduleNextEvent();
            return;
        }

        // Если нет доступных событий и мы на 4 этапе - запускаем финальную последовательность
        if (availableEvents.isEmpty() && currentStage == 4) {
            startFinalSequence();
            return;
        }

        // Выбираем случайное событие из доступных
        int index = random.nextInt(availableEvents.size());
        MysticEvent event = availableEvents.get(index);

        // Увеличиваем счетчик повторений
        event.trigger();

        executeEvent(event);

        // Broadcast event to multiplayer if connected
        broadcastHorrorEvent(event.name, currentStage, 1.0f);
    }

    private List<MysticEvent> getCurrentStageEvents() {
        switch (currentStage) {
            case 1: return stage1Events;
            case 2: return stage2Events;
            case 3: return stage3Events;
            case 4: return stage4Events;
            default: return stage1Events;
        }
    }

    private void executeEvent(MysticEvent event) {
        try {
        // Этап 1 события
        if (event.name.equals("sign_spawn")) {
            spawnMysticSigns();
        } else if (event.name.equals("fake_error")) {
            showFakeError();
        } else if (event.name.equals("red_lines_pause")) {
            // Флаг для GuiIngameMenu
            isRedLinesPauseActive = true;
        } else if (event.name.equals("disc_13")) {
            playDisc13();
        } else if (event.name.equals("disc_11")) {
            playDisc11();
        } else if (event.name.equals("vhs_effect")) {
            isVHSActive = true;
            long currentTick = mc.theWorld.getWorldTime();
            vhsEndTime = currentTick + 600; // 30 секунд = 600 тиков
        } else if (event.name.equals("random_item")) {
            giveRandomItem();
        } else if (event.name.equals("gui_shaking")) {
            isGuiShakingActive = true;
            // Тряска GUI длится 30 секунд (600 тиков)
            long currentTick = mc.theWorld.getWorldTime();
            long guiShakingEndTime = currentTick + 600;
            // Сохраняем время окончания для проверки в update()
            this.guiShakingEndTime = guiShakingEndTime;
        } else if (event.name.equals("dry_lightning")) {
            spawnDryLightning();
        } else if (event.name.equals("cryptic_hints")) {
            isCrypticHintsActive = true;
        } else if (event.name.equals("watchers")) {
            isWatchersActive = true;
            long currentTick = mc.theWorld.getWorldTime();
            watchersEndTime = currentTick + 600; // 30 секунд = 600 тиков
        } else if (event.name.equals("window_shake")) {
            startWindowShake();
        }

        // Этап 2 события
        else if (event.name.equals("silhouette_spawn")) {
            spawnSilhouette();
        } else if (event.name.equals("time_flip")) {
            flipTime();
        } else if (event.name.equals("footsteps_behind")) {
            startFootstepsBehind();
        } else if (event.name.equals("inventory_swap")) {
            swapInventoryItems();
        } else if (event.name.equals("eyes_in_fog")) {
            spawnEyesInFog();
        } else if (event.name.equals("gdi_tunnel")) {
            // GDI tunnel effect (3 seconds)
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    HorrorSystemDLL.INSTANCE.GDI_InvertTunnel();
                }).start();
            }
        } else if (event.name.equals("world_erosion")) {
            erodeWorld();
        }

        // Этап 3 события
        else if (event.name.equals("fake_join_message")) {
            sendFakeJoinMessage();
        } else if (event.name.equals("entity_detector")) {
            isFakeEntityDetectorActive = true;
        } else if (event.name.equals("fake_saving_chunks")) {
            showFakeSavingChunks();
        } else if (event.name.equals("hand_decay")) {
            isHandDecayActive = true;
            handDecayEndTime = System.currentTimeMillis() + 45000;
        } else if (event.name.equals("fake_gl_error")) {
            sendFakeGLError();
        } else if (event.name.equals("mirror_player")) {
            spawnMirrorPlayer();
        } else if (event.name.equals("forgotten_structures")) {
            spawnForgottenStructures();
        } else if (event.name.equals("echo_sounds")) {
            isEchoSoundsActive = true;
        } else if (event.name.equals("open_calculator")) {
            // Open Windows Calculator
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    HorrorSystemDLL.INSTANCE.OpenCalculator();
                }).start();
            }
        } else if (event.name.equals("spam_messages")) {
            // Spam MessageBox windows (5 times)
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    HorrorSystemDLL.INSTANCE.SpamMessageBoxes(5, "ohh", "what is this?", "death");
                }).start();
            }
        } else if (event.name.equals("open_notepad")) {
            // Open Notepad with creepy text
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    String[] messages = {
                        "You shouldn't have done that...",
                        "I can see you through the screen",
                        "404 404 404 404 404 404",
                        "Player404 is watching you",
                        "There is no escape",
                        "Your world is corrupted",
                        "Did you think this was just a game?",
                        "I know where you live"
                    };
                    String randomMessage = messages[random.nextInt(messages.length)];
                    HorrorSystemDLL.INSTANCE.OpenNotepadWithText(randomMessage);
                }).start();
            }
        } else if (event.name.equals("gdi_glitch_attack")) {
            // GDI Glitch Attack (2 seconds)
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    long startTime = System.currentTimeMillis();
                    int iterations = 0;

                    // 2 секунды глитча (40 итераций по 50мс)
                    while (System.currentTimeMillis() - startTime < 2000) {
                        // Основной глитч экрана
                        HorrorSystemDLL.INSTANCE.GDI_GlitchScreen();

                        // Каждые 10 итераций - инверсия цветов
                        if (iterations % 10 == 0) {
                            HorrorSystemDLL.INSTANCE.GDI_InvertScreen();
                        }

                        // Каждые 5 итераций - эффект тающих пикселей
                        if (iterations % 5 == 0) {
                            HorrorSystemDLL.INSTANCE.GDI_PixelMelt();
                        }

                        iterations++;

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    // После глитча восстанавливаем экран инверсией (если был нечетный)
                    if (iterations / 10 % 2 == 1) {
                        HorrorSystemDLL.INSTANCE.GDI_InvertScreen();
                    }
                }).start();
            }
        } else if (event.name.equals("gdi_spam_text")) {
            // GDI Spam Text (3 seconds)
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    // Опционально: инверсия перед спамом для усиления эффекта
                    if (random.nextBoolean()) {
                        HorrorSystemDLL.INSTANCE.GDI_InvertScreen();
                    }

                    // Воспроизводим звук глитча
                    try {
                        mc.sndManager.playSoundFX("glitches.error", 1.0f, 0.8f);
                    } catch (Exception e) {}

                    // Запускаем спам текстов на 3 секунды
                    HorrorSystemDLL.INSTANCE.GDI_SpamText(3);

                    // Восстанавливаем инверсию если была
                    if (random.nextBoolean()) {
                        HorrorSystemDLL.INSTANCE.GDI_InvertScreen();
                    }
                }).start();
            }
        } else if (event.name.equals("mouse_possession")) {
            // Mouse Possession (5 seconds)
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    HorrorSystemDLL.INSTANCE.MousePossession(5);
                }).start();
            }
        } else if (event.name.equals("red_tint_screen")) {
            // Red Tint Screen (10 seconds - reduced to prevent freezing)
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    try {
                        HorrorSystemDLL.INSTANCE.RedTintScreen(10); // Reduced from 60 to 10
                    } catch (Exception e) {
                    }
                }).start();
            }
        } else if (event.name.equals("ghost_windows")) {
            // Ghost Windows (spawn multiple over 60 seconds)
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    for (int i = 0; i < 20; i++) {
                        HorrorSystemDLL.INSTANCE.SpawnGhostWindow();
                        try {
                            Thread.sleep(3000); // Каждые 3 секунды
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }).start();
            }
        } else if (event.name.equals("dead_pixels")) {
            // Dead Pixels (30 seconds, 10 pixels)
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    HorrorSystemDLL.INSTANCE.DeadPixels(10, 30);
                }).start();
            }
        } else if (event.name.equals("clipboard_hijack")) {
            // Clipboard Hijack
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    String[] messages = {
                        "WHY ARE YOU CALLING FOR HELP?",
                        "THERE IS NO ESCAPE",
                        "I AM WATCHING YOU",
                        "YOU SHOULDN'T HAVE DONE THAT",
                        "404 ERROR: REALITY NOT FOUND",
                        "PLAYER404 IS HERE"
                    };
                    String randomMessage = messages[random.nextInt(messages.length)];
                    HorrorSystemDLL.INSTANCE.ClipboardHijack(randomMessage);
                }).start();
            }
        } else if (event.name.equals("mouse_friction")) {
            // Mouse Friction (10 seconds)
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    HorrorSystemDLL.INSTANCE.MouseFriction(10);
                }).start();
            }
        } else if (event.name.equals("system_beep")) {
            // System Beep (random frequencies)
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    int[] frequencies = {400, 600, 800, 1000, 1200};
                    for (int i = 0; i < 5; i++) {
                        int freq = frequencies[random.nextInt(frequencies.length)];
                        HorrorSystemDLL.INSTANCE.SystemBeep(freq, 200);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }).start();
            }
        } else if (event.name.equals("fake_game_close")) {
            // Fake Game Close
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    // Передаем 0 как handle, DLL сама найдет окно
                    HorrorSystemDLL.INSTANCE.FakeGameClose(0);
                }).start();
            }
        } else if (event.name.equals("broken_clock")) {
            // Broken Clock (15 seconds)
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    HorrorSystemDLL.INSTANCE.DrawBrokenClock(15);
                }).start();
            }
        } else if (event.name.equals("tunnel_vision")) {
            // Системный туннельный эффект через DLL
            if (HorrorSystemDLL.isAvailable()) {
                isTunnelVisionActive = true;
                HorrorSystemDLL.INSTANCE.StartTunnelVision(10000); // 10 секунд
            }
        }

        // Этап 4 события
        else if (event.name.equals("chunk_distortion")) {
            distortChunk();
        } else if (event.name.equals("blood_water")) {
            isBloodWaterActive = true;
            bloodWaterEndTime = System.currentTimeMillis() + 45000;
        } else if (event.name.equals("forced_turn")) {
            forceTurn180();
        } else if (event.name.equals("fog_collapse")) {
            isFogCollapseActive = true;
            fogCollapseEndTime = System.currentTimeMillis() + 30000;
        } else if (event.name.equals("sky_glitch")) {
            isSkyGlitchActive = true;
            skyGlitchEndTime = System.currentTimeMillis() + 60000;
        } else if (event.name.equals("infinite_inventory")) {
            isInfiniteInventoryActive = true;
        } else if (event.name.equals("restart_explorer")) {
            // Restart Explorer.exe (close + start after 5 seconds)
            if (HorrorSystemDLL.isAvailable()) {
                new Thread(() -> {
                    HorrorSystemDLL.INSTANCE.RestartExplorer();
                }).start();
            }
        } else if (event.name.equals("world_mirror")) {
            isWorldMirrorActive = true;
            worldMirrorEndTime = System.currentTimeMillis() + 15000;
        } else if (event.name.equals("death_chat")) {
            startDeathChat();
        } else if (event.name.equals("white_noise")) {
            playWhiteNoise();
        } else if (event.name.equals("screamer_interface")) {
            showScreamerInterface();
        } else if (event.name.equals("fake_bsod")) {
            showFakeBSOD();
        } else if (event.name.equals("void_hole")) {
            startVoidHole();
        } else if (event.name.equals("chat_spam")) {
            startChatSpam();
        } else if (event.name.equals("control_inversion")) {
            startControlInversion();
        } else if (event.name.equals("shadow_chat")) {
            startShadowChat();
        } else if (event.name.equals("world_jitter")) {
            startWorldJitter();
        } else if (event.name.equals("bedrock_tunnel")) {
            createBedrockTunnel();
        } else if (event.name.equals("final_crash")) {
            triggerFinalCrash();
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateScreamer404() {
        long elapsed = System.currentTimeMillis() - screamer404StartTime;

        // Инициализация позиции окна если еще не сделано
        if (!windowShakeInitialized) {
            try {
                originalX = org.lwjgl.opengl.Display.getX();
                originalY = org.lwjgl.opengl.Display.getY();
                windowShakeInitialized = true;
            } catch (Exception e) {}
        }

        // Моргание инверсии каждые 200мс
        if (elapsed < 5000) {
            boolean shouldShow = (elapsed / 200) % 2 == 0;
            isInversionActive = shouldShow;
        }

        // Тряска окна каждые 50мс в течение 5 секунд
        if (elapsed < 5000 && screamer404Counter < 100) {
            if (elapsed / 50 > screamer404Counter) {
                int shakeX = random.nextInt(40) - 20;
                int shakeY = random.nextInt(40) - 20;
                try {
                    org.lwjgl.opengl.Display.setLocation(
                        originalX + shakeX,
                        originalY + shakeY
                    );
                } catch (Exception e) {}
                screamer404Counter++;
            }
        } else if (elapsed >= 5000) {
            // После 5 секунд - отключаем скример
            screamer404Active = false;

            // Возвращаем окно на место
            try {
                org.lwjgl.opengl.Display.setLocation(originalX, originalY);
            } catch (Exception e) {}

            // Отключаем инверсию
            isInversionActive = false;

            // Полный краш из игры БЕЗ locked.dat
            throw new RuntimeException("404");
        }
    }

    private void updateSignInversion() {
        // Проверка не нужна - логика перенесена в isSignInversionActive()
    }

    public void triggerSignInversion() {
        signInversionActive = true;
        signInversionStartTime = System.currentTimeMillis();
        isInversionActive = true; // Включаем сразу
    }

    /**
     * Проверяет, активна ли инверсия от таблички и должна ли она показываться
     * Вызывается при рендеринге
     */
    public boolean isSignInversionActive() {
        if (signInversionActive) {
            long elapsed = System.currentTimeMillis() - signInversionStartTime;
            if (elapsed >= SIGN_INVERSION_DURATION) {
                signInversionActive = false;
                isInversionActive = false;
                return false;
            }
            // Моргание вкл/выкл каждые FLASH_INTERVAL мс
            boolean shouldShow = (elapsed / FLASH_INTERVAL) % 2 == 0;
            isInversionActive = shouldShow;
            return shouldShow;
        }
        return false;
    }

    public void updateWindowShake() {
        // Интенсивная тряска окна во время события
        if (intensiveWindowShakeActive) {
            long currentTime = System.currentTimeMillis();
            if (currentTime < intensiveWindowShakeEndTime) {
                // Сильная тряска каждые 50мс
                if (currentTime - lastWindowShakeTime > 50) {
                    int shakeX = random.nextInt(30) - 15;
                    int shakeY = random.nextInt(30) - 15;
                    try {
                        Display.setLocation(originalX + shakeX, originalY + shakeY);
                    } catch (Exception e) {}
                    lastWindowShakeTime = currentTime;
                }
            } else {
                // Время вышло - отключаем тряску и возвращаем окно на место
                intensiveWindowShakeActive = false;
                try {
                    Display.setLocation(originalX, originalY);
                } catch (Exception e) {}
            }
            return; // Не выполняем обычную тряску меню
        }

        // Тряска окна только в главном меню, не в игре
        if (mc.currentScreen != null && mc.currentScreen instanceof GuiMainMenu) {
            if (!windowShakeInitialized) {
                try {
                    originalX = Display.getX();
                    originalY = Display.getY();
                    windowShakeInitialized = true;
                } catch (Exception e) {}
            }

            // Уменьшаем частоту с 50мс до 200мс для предотвращения потери фокуса
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastWindowShakeTime > 200) {
                int shakeX = random.nextInt(5) - 2;
                int shakeY = random.nextInt(5) - 2;
                try {
                    Display.setLocation(originalX + shakeX, originalY + shakeY);
                } catch (Exception e) {}
                lastWindowShakeTime = currentTime;
            }
        }
    }

    private void updateWindowTitle() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTitleChangeTime > 50) {
            String glitchedTitle = generateGlitchedString(originalTitle);
            Display.setTitle(glitchedTitle);
            lastTitleChangeTime = currentTime;
        }
    }

    public String generateGlitchedString(String input) {
        if (isEndMode) {
            // В режиме End все буквы заменяются на цифры
            StringBuilder result = new StringBuilder();
            for (char c : input.toCharArray()) {
                if (Character.isLetter(c)) {
                    result.append(random.nextInt(10));
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        } else {
            // Обычный глитч - случайные символы
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < input.length(); i++) {
                if (random.nextFloat() < 0.3f) {
                    result.append((char)('a' + random.nextInt(26)));
                } else {
                    result.append(input.charAt(i));
                }
            }
            return result.toString();
        }
    }

    public String glitchButtonText(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c) && random.nextFloat() < 0.2f) {
                result.append(random.nextInt(10));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private void updateAggressiveMobs() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<EntityLiving, Long>> iterator = aggressiveMobs.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<EntityLiving, Long> entry = iterator.next();
            if (currentTime > entry.getValue()) {
                iterator.remove();
            }
        }
    }

    private void updateSoundDistortion() {
        // Постепенно увеличиваем искажение звука с прогрессией этапов
        if (currentStage == 1) {
            soundDistortion = 1.0f + (random.nextFloat() * 0.1f - 0.05f); // ±5%
        } else if (currentStage == 2) {
            soundDistortion = 1.0f + (random.nextFloat() * 0.2f - 0.1f); // ±10%
        } else if (currentStage == 3) {
            soundDistortion = 1.0f + (random.nextFloat() * 0.4f - 0.2f); // ±20%
        } else if (currentStage == 4) {
            soundDistortion = 1.0f + (random.nextFloat() * 0.6f - 0.3f); // ±30%
        }

        // В END режиме - максимальное искажение
        if (isEndMode) {
            soundDistortion = 0.5f + random.nextFloat() * 1.0f; // 0.5 - 1.5
        }
    }

    public void makeAggressiveTemporarily(EntityLiving entity) {
        long endTime = System.currentTimeMillis() + 10000; // 10 секунд
        aggressiveMobs.put(entity, endTime);
    }

    public boolean isTemporarilyAggressive(EntityLiving entity) {
        return aggressiveMobs.containsKey(entity);
    }

    // Команды управления
    public void handleCommand(String command) {
        if (command.startsWith("/x")) {
            String multiplierStr = command.substring(2);
            try {
                int multiplier = Integer.parseInt(multiplierStr);
                float oldMultiplier = speedMultiplier;
                speedMultiplier = multiplier;
                mc.thePlayer.addChatMessage("Speed multiplier set to x" + multiplier);


                // Пересчитываем интервал для текущего события
                scheduleNextEvent();


                // Сбрасываем lastEventTick так, чтобы следующее событие произошло через новый интервал
                if (mc.theWorld != null) {
                    long currentTick = mc.theWorld.getWorldTime();
                    // Устанавливаем lastEventTick = (текущее время - новый интервал)
                    // Это заставит событие сработать в следующем update()
                    lastEventTick = currentTick - nextEventDelayTicks + 20; // +20 тиков = 1 секунда запас
                }

                long seconds = nextEventDelayTicks / 20;
                mc.thePlayer.addChatMessage("\u00A77Next event in ~" + seconds + "s (interval: " + (nextEventDelayTicks / 1200) + "m)");
            } catch (NumberFormatException e) {
                mc.thePlayer.addChatMessage("\u00A7cInvalid format");
            }
        } else if (command.equals("/next")) {
            if (!initialized || mc.theWorld == null) {
                if (mc.thePlayer != null) {
                    mc.thePlayer.addChatMessage("\u00A7cMystic system not initialized");
                }
                return;
            }

            List<MysticEvent> currentEvents = getCurrentStageEvents();

            // Фильтруем события, которые еще можно активировать
            List<MysticEvent> availableEvents = new ArrayList<MysticEvent>();
            for (MysticEvent event : currentEvents) {
                if (event.canTrigger()) {
                    availableEvents.add(event);
                }
            }

            if (!availableEvents.isEmpty()) {
                MysticEvent nextEvent = availableEvents.get(random.nextInt(availableEvents.size()));
                String eventNameRu = getEventNameRussian(nextEvent.name);

                // Показываем информацию о повторениях
                if (nextEvent.repeatable) {
                    mc.thePlayer.addChatMessage("Triggering: " + eventNameRu + " (" + (nextEvent.repeatCount + 1) + "/" + nextEvent.maxRepeats + ")");
                } else {
                    mc.thePlayer.addChatMessage("Triggering: " + eventNameRu);
                }

                // Увеличиваем счетчик повторений
                nextEvent.trigger();

                executeEvent(nextEvent);
            } else {
                // Если нет доступных событий и мы на 4 этапе - запускаем финальную последовательность
                if (currentStage == 4) {
                    mc.thePlayer.addChatMessage("\u00A74All events exhausted. Starting final sequence...");
                    startFinalSequence();
                } else {
                    mc.thePlayer.addChatMessage("\u00A7cNo available events for stage " + currentStage);
                    mc.thePlayer.addChatMessage("\u00A77All events exhausted");
                }
            }
        } else if (command.startsWith("/mlvl")) {
            String[] parts = command.split(" ");
            if (parts.length == 1) {
                mc.thePlayer.addChatMessage("Current mystic level: " + currentStage);
                mc.thePlayer.addChatMessage("Usage: /mlvl <1-4>");
            } else if (parts.length == 2) {
                try {
                    int newStage = Integer.parseInt(parts[1]);
                    if (newStage >= 1 && newStage <= 4) {
                        currentStage = newStage;
                        saveToWorld();
                        mc.thePlayer.addChatMessage("Mystic level set to: " + currentStage);

                        // Обновляем искажение звука в зависимости от этапа
                        if (currentStage == 1) {
                            soundDistortion = 1.0f;
                        } else if (currentStage == 2) {
                            soundDistortion = 1.3f;
                        } else if (currentStage == 3) {
                            soundDistortion = 1.7f;
                        } else if (currentStage == 4) {
                            soundDistortion = 2.2f;
                        }

                        mc.thePlayer.addChatMessage("\u00A77Sound distortion: " + soundDistortion);
                    } else {
                        mc.thePlayer.addChatMessage("\u00A7cLevel must be 1-4");
                    }
                } catch (NumberFormatException e) {
                    mc.thePlayer.addChatMessage("\u00A7cInvalid format. Usage: /mlvl <1-4>");
                }
            }
        } else if (command.equals("/mst")) {
            // Показать текущий этап мистики
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage("Current stage: " + currentStage);
                mc.thePlayer.addChatMessage("Speed: x" + speedMultiplier);
                mc.thePlayer.addChatMessage("Sound distortion: " + soundDistortion + "x");

                List<MysticEvent> currentEvents = getCurrentStageEvents();
                int nonRepeatableCount = 0;
                int repeatableCount = 0;
                for (MysticEvent event : currentEvents) {
                    if (event.repeatable) {
                        repeatableCount++;
                    } else {
                        nonRepeatableCount++;
                    }
                }
                mc.thePlayer.addChatMessage("Events: " + nonRepeatableCount + " one-time, " + repeatableCount + " repeatable");

                if (isEndMode) {
                    mc.thePlayer.addChatMessage("\u00A7c[END MODE ACTIVE]");
                }
            }
        } else if (command.equals("/mreset")) {
            // Сброс мистического состояния
            try {
                File lockFile = new File("locked.dat");
                if (lockFile.exists()) {
                    lockFile.delete();
                    mc.thePlayer.addChatMessage("\u00A7aFile locked.dat deleted");
                } else {
                    mc.thePlayer.addChatMessage("\u00A77File locked.dat not found");
                }

                // Сброс всех флагов и состояний
                isEndMode = false;
                isGlitchMode = false;
                currentStage = 1;
                soundDistortion = 1.0f;
                speedMultiplier = 1.0f;

                // Сброс gameStartTick
                gameStartTick = mc.theWorld.getWorldTime();

                // Сохраняем сброшенное состояние
                saveToWorld();

                // Сброс всех активных эффектов
                isInversionActive = false;
                isVHSActive = false;
                isSkyGlitchActive = false;
                isWorldMirrorActive = false;
                isFogCollapseActive = false;
                isInfiniteInventoryActive = false;
                isRedLinesPauseActive = false;
                isGuiShakingActive = false;
                isCrypticHintsActive = false;
                isWatchersActive = false;
                isEyesInFogActive = false;
                isFakeEntityDetectorActive = false;
                isHandDecayActive = false;
                isEchoSoundsActive = false;
                isBloodWaterActive = false;
                isChatSpamActive = false;

                // Сброс таймеров
                gameStartTick = mc.theWorld.getWorldTime();
                lastEventTick = gameStartTick;

                mc.thePlayer.addChatMessage("\u00A7aMystic state reset to Stage 1");
                mc.thePlayer.addChatMessage("\u00A77Sound distortion: 1.0");
            } catch (Exception e) {
                mc.thePlayer.addChatMessage("\u00A7cReset error: " + e.getMessage());
            }
        } else if (command.equals("/wav")) {
            // Воспроизвести случайный .wav звук из glitches
            playRandomGlitchSound();
        } else if (command.equals("/ogg")) {
            // Воспроизвести случайный .ogg звук из glitches
            playRandomGlitchSoundOgg();
        } else if (command.startsWith("/event item_set ")) {
            // Выдать набор предметов
            String setName = command.substring(16).trim();
            giveItemSet(setName);
        } else if (command.startsWith("/event item clear")) {
            // Очистить инвентарь
            clearInventory();
        } else if (command.equals("/event item px-all")) {
            // Выдать кирку, ломающую всё
            givePickaxeAll();
        } else if (command.startsWith("/event item ")) {
            // Выдать предмет по ID с поддержкой указания игрока
            String[] parts = command.split(" ");
            
            if (parts.length >= 4) {
                // Формат: /event item <ник> <айди>
                String targetUsername = parts[2];
                String itemIdStr = parts[3];
                
                try {
                    int itemId = Integer.parseInt(itemIdStr);
                    giveItemToPlayer(targetUsername, itemId);
                } catch (NumberFormatException e) {
                    mc.thePlayer.addChatMessage("\u00A7cInvalid item ID: " + itemIdStr);
                }
            } else if (parts.length == 3) {
                // Формат: /event item <айди> - выдать себе
                String itemIdStr = parts[2];
                try {
                    int itemId = Integer.parseInt(itemIdStr);
                    giveItem(itemId);
                } catch (NumberFormatException e) {
                    mc.thePlayer.addChatMessage("\u00A7cInvalid item ID: " + itemIdStr);
                }
            } else {
                mc.thePlayer.addChatMessage("\u00A7cUsage: /event item <player> <item_id> or /event item <item_id>");
            }
        } else if (command.startsWith("/event time set ")) {
            // Установить время суток
            String timeOfDay = command.substring(16).trim().toLowerCase();
            if (mc.theWorld != null) {
                if (timeOfDay.equals("day")) {
                    mc.theWorld.setWorldTime(1000); // День
                    mc.thePlayer.addChatMessage("\u00A7aTime set to day");
                } else if (timeOfDay.equals("night")) {
                    mc.theWorld.setWorldTime(13000); // Ночь
                    mc.thePlayer.addChatMessage("\u00A7aTime set to night");
                } else {
                    mc.thePlayer.addChatMessage("\u00A7cUsage: /event time set <day|night>");
                }
            }
        } else if (command.startsWith("/event ")) {
            // Запустить конкретное событие по имени
            String eventName = command.substring(7).trim();
            executeEventByName(eventName);
        }
    }

    private void executeEventByName(String eventName) {
        if (mc.thePlayer == null) {
            return;
        }

        // Алиасы для удобства
        if (eventName.equalsIgnoreCase("InvertTunnel")) {
            eventName = "gdi_tunnel";
        }
        if (eventName.equalsIgnoreCase("Calculator") || eventName.equalsIgnoreCase("Calc")) {
            eventName = "open_calculator";
        }
        if (eventName.equalsIgnoreCase("SpamMessages") || eventName.equalsIgnoreCase("Spam")) {
            eventName = "spam_messages";
        }
        if (eventName.equalsIgnoreCase("RestartExplorer") || eventName.equalsIgnoreCase("Explorer")) {
            eventName = "restart_explorer";
        }

        // Ищем событие во всех этапах
        MysticEvent foundEvent = null;

        // Проверяем Stage 1
        for (MysticEvent event : stage1Events) {
            if (event.name.equals(eventName)) {
                foundEvent = event;
                break;
            }
        }

        // Проверяем Stage 2
        if (foundEvent == null) {
            for (MysticEvent event : stage2Events) {
                if (event.name.equals(eventName)) {
                    foundEvent = event;
                    break;
                }
            }
        }

        // Проверяем Stage 3
        if (foundEvent == null) {
            for (MysticEvent event : stage3Events) {
                if (event.name.equals(eventName)) {
                    foundEvent = event;
                    break;
                }
            }
        }

        // Проверяем Stage 4
        if (foundEvent == null) {
            for (MysticEvent event : stage4Events) {
                if (event.name.equals(eventName)) {
                    foundEvent = event;
                    break;
                }
            }
        }

        if (foundEvent != null) {
            String eventNameDisplay = getEventNameRussian(foundEvent.name);
            mc.thePlayer.addChatMessage("\u00A7aExecuting event: \u00A7f" + eventNameDisplay);
            executeEvent(foundEvent);
        } else {
            mc.thePlayer.addChatMessage("\u00A7cEvent not found: " + eventName);
            mc.thePlayer.addChatMessage("\u00A77Usage: /event <event_name>");
            mc.thePlayer.addChatMessage("\u00A77Examples:");
            mc.thePlayer.addChatMessage("\u00A77  /event gdi_tunnel (or /event InvertTunnel)");
            mc.thePlayer.addChatMessage("\u00A77  /event open_calculator (or /event Calc)");
            mc.thePlayer.addChatMessage("\u00A77  /event spam_messages (or /event Spam)");
            mc.thePlayer.addChatMessage("\u00A77  /event restart_explorer (or /event Explorer)");
            mc.thePlayer.addChatMessage("\u00A77  /event time set day");
            mc.thePlayer.addChatMessage("\u00A77  /event time set night");
        }
    }

    public void triggerFinalLock() {
        try {
            // Показываем финальное окно "Did you have fun?"
            showFinalExitDialog();

            // Портим настройки игры
            corruptGameSettings();

            // Создаем locked.dat в директории game
            File lockFile = new File("game/locked.dat");
            if (!lockFile.exists()) {
                // Создаем директорию если не существует
                lockFile.getParentFile().mkdirs();

                PrintWriter writer = new PrintWriter(new FileWriter(lockFile));
                writer.println("0");
                writer.close();
            }
        } catch (IOException e) {
            // Игнорируем ошибки
        }
        throw new RuntimeException("your world is fucked");
    }

    private void corruptGameSettings() {
        try {
            File optionsFile = new File("game/options.txt");
            if (optionsFile.exists()) {
                // Читаем текущие настройки
                BufferedReader reader = new BufferedReader(new FileReader(optionsFile));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    // Изменяем настройки
                    if (line.startsWith("mouseSensitivity:")) {
                        content.append("mouseSensitivity:0.0\n");
                    } else if (line.startsWith("soundVolume:")) {
                        content.append("soundVolume:1.0\n");
                    } else if (line.startsWith("musicVolume:")) {
                        content.append("musicVolume:1.0\n");
                    } else if (line.startsWith("lang:")) {
                        // Случайные символы вместо языка
                        String glitched = "";
                        for (int i = 0; i < 8; i++) {
                            glitched += (char)('a' + random.nextInt(26));
                        }
                        content.append("lang:" + glitched + "\n");
                    } else {
                        content.append(line + "\n");
                    }
                }
                reader.close();

                // Записываем испорченные настройки
                PrintWriter writer = new PrintWriter(new FileWriter(optionsFile));
                writer.print(content.toString());
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFinalExitDialog() {
        try {
            // Создаем отдельный поток для диалога
            new Thread(new Runnable() {
                public void run() {
                    try {
                        javax.swing.JFrame frame = new javax.swing.JFrame();
                        frame.setAlwaysOnTop(true);
                        frame.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);

                        // Показываем диалог
                        javax.swing.JOptionPane pane = new javax.swing.JOptionPane(
                            "Did you have fun?",
                            javax.swing.JOptionPane.QUESTION_MESSAGE,
                            javax.swing.JOptionPane.YES_NO_OPTION
                        );
                        javax.swing.JDialog dialog = pane.createDialog(frame, "...");

                        // Показываем диалог
                        dialog.setVisible(true);

                        // После нажатия кнопки - трясем окно 5 секунд
                        long startTime = System.currentTimeMillis();
                        int originalX = dialog.getX();
                        int originalY = dialog.getY();

                        while (System.currentTimeMillis() - startTime < 5000) {
                            int shakeX = random.nextInt(20) - 10;
                            int shakeY = random.nextInt(20) - 10;
                            dialog.setLocation(originalX + shakeX, originalY + shakeY);
                            Thread.sleep(50);
                        }

                        dialog.dispose();
                        frame.dispose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // Ждем 6 секунд (1 секунда на ответ + 5 секунд тряски)
            Thread.sleep(6000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void giveItem(int itemId) {
        giveItem(itemId, 1);
    }

    private void giveItem(int itemId, int amount) {
        if (mc.thePlayer == null) {
            return;
        }

        try {
            ItemStack itemStack = new ItemStack(itemId, amount, 0);
            mc.thePlayer.inventory.addItemStackToInventory(itemStack);
            mc.thePlayer.addChatMessage("\u00A7aGiven item: \u00A7f" + itemId + " x" + amount);
        } catch (Exception e) {
            mc.thePlayer.addChatMessage("\u00A7cFailed to give item: " + itemId);
        }
    }

    private void giveItemToPlayer(String targetUsername, int itemId) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        
        // В одиночной игре не можем выдать предмет другому игроку
        // Показываем сообщение что это работает только в мультиплеере
        mc.thePlayer.addChatMessage("\u00a7cThis command only works in multiplayer!");
        mc.thePlayer.addChatMessage("\u00a77Usage: /event item <player> <item_id>");
    }
    
    private void giveItemSet(String setName) {
        if (mc.thePlayer == null) {
            return;
        }

        if (setName.equalsIgnoreCase("tnt")) {
            // Fire (Огонь): 51
            giveItem(51, 64);
            // Redstone Torch Active (Редстоуновый факел активный): 76
            giveItem(76, 64);
            // Redstone Torch Inactive (Редстоуновый факел выключенный): 75
            giveItem(75, 64);
            // Water Standing (Блок стоячей воды): 9
            giveItem(9, 64);
            // Water Flowing (Блок текущей воды): 8
            giveItem(8, 64);
            // Redstone Dust (Редстоуновая пыль): 331
            giveItem(331, 64);
            // TNT (Взрывчатка): 46
            giveItem(46, 64);
            // Flint and Steel (Огниво): 259
            giveItem(259, 1);

            mc.thePlayer.addChatMessage("\u00A7aGiven TNT item set");
        } else {
            mc.thePlayer.addChatMessage("\u00A7cUnknown item set: " + setName);
            mc.thePlayer.addChatMessage("\u00A77Available sets: tnt");
        }
    }

    private void clearInventory() {
        if (mc.thePlayer == null) {
            return;
        }

        for (int i = 0; i < mc.thePlayer.inventory.mainInventory.length; i++) {
            mc.thePlayer.inventory.mainInventory[i] = null;
        }

        mc.thePlayer.addChatMessage("\u00A7aInventory cleared");
    }

    private void givePickaxeAll() {
        if (mc.thePlayer == null) {
            return;
        }

        try {
            ItemStack pickaxe = new ItemStack(Item.pickaxeAll);
            mc.thePlayer.inventory.addItemStackToInventory(pickaxe);
            mc.thePlayer.addChatMessage("\u00A7aGiven: \u00A7fPickaxe (Breaks Everything)");
            mc.thePlayer.addChatMessage("\u00A77Can break bedrock and any block instantly");
        } catch (Exception e) {
            mc.thePlayer.addChatMessage("\u00A7cFailed to give pickaxe: " + e.getMessage());
        }
    }

    // Внутренний класс для событий
    private class MysticEvent {
        String name;
        int stage;
        boolean repeatable;
        int repeatCount;
        int maxRepeats;

        MysticEvent(String name, int stage) {
            this.name = name;
            this.stage = stage;
            this.repeatable = false;
            this.repeatCount = 0;
            this.maxRepeats = 0;
        }

        MysticEvent(String name, int stage, boolean repeatable) {
            this.name = name;
            this.stage = stage;
            this.repeatable = repeatable;
            this.repeatCount = 0;
            this.maxRepeats = repeatable ? 3 : 1; // Повторяющиеся события - 3 раза, неповторяющиеся - 1 раз
        }

        boolean canTrigger() {
            if (!repeatable) {
                return repeatCount == 0;
            }
            return repeatCount < maxRepeats;
        }

        void trigger() {
            repeatCount++;
        }
    }

    // ========== РЕАЛИЗАЦИЯ СОБЫТИЙ ЭТАПА 1 ==========

    public void spawnMysticSigns() {
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        int playerX = (int)mc.thePlayer.posX;
        int playerY = (int)mc.thePlayer.posY;
        int playerZ = (int)mc.thePlayer.posZ;

        // Спавн таблички с надписью 404
        int signX = playerX + random.nextInt(40) - 20;
        int signZ = playerZ + random.nextInt(40) - 20;
        int signY = mc.theWorld.getHeightValue(signX, signZ);

        if (signY > 0) {
            mc.theWorld.setBlockWithNotify(signX, signY, signZ, Block.signPost.blockID);
            TileEntitySign sign = (TileEntitySign)mc.theWorld.getBlockTileEntity(signX, signY, signZ);
            if (sign != null) {
                sign.signText[1] = "404";
            }
        }

        // Спавн бедрокового креста
        int crossX = playerX + random.nextInt(30) - 15;
        int crossZ = playerZ + random.nextInt(30) - 15;
        int crossY = mc.theWorld.getHeightValue(crossX, crossZ);

        if (crossY > 0) {
            // Вертикальная часть креста
            mc.theWorld.setBlockWithNotify(crossX, crossY + 1, crossZ, Block.bedrock.blockID);
            mc.theWorld.setBlockWithNotify(crossX, crossY + 2, crossZ, Block.bedrock.blockID);
            mc.theWorld.setBlockWithNotify(crossX, crossY + 3, crossZ, Block.bedrock.blockID);
            // Горизонтальная часть
            mc.theWorld.setBlockWithNotify(crossX - 1, crossY + 2, crossZ, Block.bedrock.blockID);
            mc.theWorld.setBlockWithNotify(crossX + 1, crossY + 2, crossZ, Block.bedrock.blockID);
        }

        // Блоки на воздухе
        int airBlockX = playerX + random.nextInt(20) - 10;
        int airBlockY = playerY + 10 + random.nextInt(20);
        int airBlockZ = playerZ + random.nextInt(20) - 10;
        mc.theWorld.setBlockWithNotify(airBlockX, airBlockY, airBlockZ, Block.stone.blockID);

        // Деревья без листвы
        int treeX = playerX + random.nextInt(40) - 20;
        int treeZ = playerZ + random.nextInt(40) - 20;
        int treeY = mc.theWorld.getHeightValue(treeX, treeZ);

        if (treeY > 0) {
            // Ствол дерева (5-7 блоков высотой)
            int height = 5 + random.nextInt(3);
            for (int i = 0; i < height; i++) {
                mc.theWorld.setBlockWithNotify(treeX, treeY + i, treeZ, Block.wood.blockID);
            }
            // Несколько веток без листвы
            mc.theWorld.setBlockWithNotify(treeX + 1, treeY + height - 2, treeZ, Block.wood.blockID);
            mc.theWorld.setBlockWithNotify(treeX - 1, treeY + height - 2, treeZ, Block.wood.blockID);
            mc.theWorld.setBlockWithNotify(treeX, treeY + height - 2, treeZ + 1, Block.wood.blockID);
            mc.theWorld.setBlockWithNotify(treeX, treeY + height - 2, treeZ - 1, Block.wood.blockID);
        }
    }

    public void showFakeError() {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage("\u00A7cjava.lang.StackOverflow.error");
            // Воспроизводим звук ошибки
            try {
                mc.sndManager.playSoundFX("glitches.error", 0.8f, 1.0f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void playDisc13() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Воспроизводим пластинку 13 через мир (как обычная пластинка)
        int x = (int)mc.thePlayer.posX;
        int y = (int)mc.thePlayer.posY;
        int z = (int)mc.thePlayer.posZ;

        // Используем метод воспроизведения записи
        mc.theWorld.playRecord("13", x, y, z);
    }

    public void playDisc11() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Воспроизводим пластинку 11 через мир (как обычная пластинка)
        int x = (int)mc.thePlayer.posX;
        int y = (int)mc.thePlayer.posY;
        int z = (int)mc.thePlayer.posZ;

        // Используем метод воспроизведения записи
        mc.theWorld.playRecord("11", x, y, z);
    }

    public void giveRandomItem() {
        if (mc.thePlayer == null) return;

        // Пытаемся найти валидный предмет
        for (int attempt = 0; attempt < 100; attempt++) {
            int itemId = random.nextInt(256);

            // Проверка что предмет существует
            if (Item.itemsList[itemId] == null) {
                continue;
            }

            try {
                Item item = Item.itemsList[itemId];
                int amount = 1;

                // Проверка на стакающиеся предметы
                if (item.getItemStackLimit() > 1) {
                    amount = 1 + random.nextInt(Math.min(66, item.getItemStackLimit()));
                } else {
                    amount = 1 + random.nextInt(3);
                }

                ItemStack stack = new ItemStack(itemId, amount, 0);

                // Добавляем в инвентарь
                boolean added = mc.thePlayer.inventory.addItemStackToInventory(stack);

                if (added) {
                    return; // Предмет добавлен, выходим
                }
            } catch (Exception e) {
                // Пропускаем этот предмет и пробуем следующий
                continue;
            }
        }

        // Если не удалось выдать предмет, выдаем алмаз
        try {
            ItemStack fallback = new ItemStack(Item.diamond.shiftedIndex, 1, 0);
            mc.thePlayer.inventory.addItemStackToInventory(fallback);
        } catch (Exception e) {
        }
    }

    public void spawnDryLightning() {
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;

        EntityLightningBolt lightning = new EntityLightningBolt(mc.theWorld, x, y, z);
        mc.theWorld.addWeatherEffect(lightning);
    }

    // ========== РЕАЛИЗАЦИЯ СОБЫТИЙ ЭТАПА 2 ==========

    public void spawnSilhouette() {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        EntitySilhouette silhouette = new EntitySilhouette(mc.theWorld, mc.thePlayer);
        mc.theWorld.entityJoinedWorld(silhouette);

        // Воспроизводим тёмный звук
        try {
            mc.sndManager.playSoundFX("glitches.dark", 0.6f, 0.8f);
        } catch (Exception e) {
        }
    }

    public void flipTime() {
        if (mc.theWorld == null) return;

        long currentTime = mc.theWorld.getWorldTime();
        long newTime = (currentTime + 12000) % 24000;
        mc.theWorld.setWorldTime(newTime);

        // Сохраняем оригинальное время и планируем возврат через 5 секунд (100 тиков)
        timeFlipOriginalTime = currentTime;
        timeFlipRevertTick = mc.theWorld.getWorldTime() + 100;
        timeFlipPending = true;

        // 5% шанс краша через 5 секунд
        if (random.nextFloat() < 0.05f) {
            timeFlipCrashTick = mc.theWorld.getWorldTime() + 100;
            timeFlipCrashPending = true;
        }
    }

    public void startFootstepsBehind() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Активируем footsteps, которые будут обрабатываться в update()
        footstepsActive = true;
        footstepsCounter = 0;
    }

    public void swapInventoryItems() {
        if (mc.thePlayer == null) return;

        int slot1 = random.nextInt(9);
        int slot2 = random.nextInt(9);

        if (slot1 != slot2) {
            ItemStack temp = mc.thePlayer.inventory.mainInventory[slot1];
            mc.thePlayer.inventory.mainInventory[slot1] = mc.thePlayer.inventory.mainInventory[slot2];
            mc.thePlayer.inventory.mainInventory[slot2] = temp;
        }
    }

    public void spawnEyesInFog() {
        // Активируем флаг для рендеринга глаз
        // Реализация будет в RenderGlobal
        isEyesInFogActive = true;
        isWatchersActive = true;
        watchersEndTime = System.currentTimeMillis() + 30000;
    }

    public void erodeWorld() {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        int playerX = (int)mc.thePlayer.posX;
        int playerY = (int)mc.thePlayer.posY;
        int playerZ = (int)mc.thePlayer.posZ;

        // Ищем листву в радиусе 20 блоков
        for (int i = 0; i < 5; i++) {
            int x = playerX + random.nextInt(40) - 20;
            int y = playerY + random.nextInt(20) - 10;
            int z = playerZ + random.nextInt(40) - 20;

            if (mc.theWorld.getBlockId(x, y, z) == Block.leaves.blockID) {
                int newBlock = random.nextInt(3);
                if (newBlock == 0) {
                    mc.theWorld.setBlockWithNotify(x, y, z, Block.glass.blockID);
                } else if (newBlock == 1) {
                    mc.theWorld.setBlockWithNotify(x, y, z, Block.bedrock.blockID);
                } else {
                    mc.theWorld.setBlockWithNotify(x, y, z, 0);
                }
                break;
            }
        }
    }

    // ========== РЕАЛИЗАЦИЯ СОБЫТИЙ ЭТАПА 3 ==========

    public void sendFakeJoinMessage() {
        if (mc.thePlayer == null) return;

        mc.thePlayer.addChatMessage("\u00A7ePlayer404 joined the game");

        // Планируем сообщения через 2 минуты (2400 тиков) и 2:10 (2600 тиков)
        long currentTick = mc.theWorld.getWorldTime();
        fakeJoinMessageTick1 = currentTick + 2400;
        fakeJoinMessageTick2 = currentTick + 2600;
        fakeJoinMessage1Pending = true;
        fakeJoinMessage2Pending = true;
    }

    public void showFakeSavingChunks() {
        // Показываем GUI только если игрок в игре
        if (mc != null && mc.theWorld != null && mc.thePlayer != null) {
            GuiSavingChunks gui = new GuiSavingChunks();
            mc.displayGuiScreen(gui);
        }
    }

    public void sendFakeGLError() {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage("\u00A7eWarning: GL11 is corrupted.");
            // Воспроизводим звук глитча
            try {
                mc.sndManager.playSoundFX("glitches.gl", 0.7f, 1.0f);
            } catch (Exception e) {
            }
        }
    }

    public void spawnMirrorPlayer() {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        EntityMirrorPlayer mirror = new EntityMirrorPlayer(mc.theWorld, mc.thePlayer);
        mc.theWorld.entityJoinedWorld(mirror);

        // Воспроизводим звук глитча
        try {
            mc.sndManager.playSoundFX("glitches.glitch", 0.7f, 0.9f);
        } catch (Exception e) {
        }
    }

    public void spawnForgottenStructures() {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        int playerX = (int)mc.thePlayer.posX;
        int playerY = (int)mc.thePlayer.posY;
        int playerZ = (int)mc.thePlayer.posZ;

        // Определяем направление взгляда игрока
        float yaw = mc.thePlayer.rotationYaw;
        double lookX = -Math.sin(Math.toRadians(yaw));
        double lookZ = Math.cos(Math.toRadians(yaw));

        // Спавним за спиной (противоположное направление)
        int backX = playerX - (int)(lookX * 15);
        int backZ = playerZ - (int)(lookZ * 15);

        if (random.nextBoolean()) {
            // Одинокая дверь
            int backY = mc.theWorld.getHeightValue(backX, backZ);
            mc.theWorld.setBlockWithNotify(backX, backY, backZ, Block.doorWood.blockID);
            mc.theWorld.setBlockWithNotify(backX, backY + 1, backZ, Block.doorWood.blockID);
        } else {
            // Круг из заборов (радиус 3 блока)
            int centerY = mc.theWorld.getHeightValue(backX, backZ);
            int radius = 3;

            for (int angle = 0; angle < 360; angle += 30) {
                double rad = Math.toRadians(angle);
                int x = backX + (int)(Math.cos(rad) * radius);
                int z = backZ + (int)(Math.sin(rad) * radius);
                int y = mc.theWorld.getHeightValue(x, z);

                mc.theWorld.setBlockWithNotify(x, y, z, Block.fence.blockID);
            }
        }
    }

    // ========== РЕАЛИЗАЦИЯ СОБЫТИЙ ЭТАПА 4 ==========

    public void distortChunk() {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        int playerX = (int)mc.thePlayer.posX;
        int playerZ = (int)mc.thePlayer.posZ;

        // Находим чанк рядом с игроком
        distortChunkX = (playerX >> 4) << 4;
        distortChunkZ = (playerZ >> 4) << 4;

        // Сохраняем оригинальные блоки
        distortChunkOriginalBlocks = new int[16][128][16];
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 128; y++) {
                for (int z = 0; z < 16; z++) {
                    distortChunkOriginalBlocks[x][y][z] = mc.theWorld.getBlockId(distortChunkX + x, y, distortChunkZ + z);
                }
            }
        }

        // Вырезаем половину чанка (делаем воздухом) - только визуально
        // Вырезаем нижнюю половину по Y (0-64)
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 64; y++) {
                for (int z = 0; z < 16; z++) {
                    // Используем setBlock вместо setBlockWithNotify для временного изменения
                    mc.theWorld.setBlock(distortChunkX + x, y, distortChunkZ + z, 0);
                }
            }
        }

        // Воспроизводим звук искажения
        try {
            mc.sndManager.playSoundFX("glitches.error", 0.8f, 0.7f);
        } catch (Exception e) {
        }

        // Планируем возврат через 10 секунд (200 тиков)
        distortChunkActive = true;
        distortChunkEndTick = mc.theWorld.getWorldTime() + 200;
    }

    private void forceTurn180() {
        if (mc.thePlayer != null) {
            mc.thePlayer.rotationYaw += 180.0f;
            mc.sndManager.playSoundFX("random.break", 1.0f, 1.0f);
        }
    }

    public void startDeathChat() {
        isChatSpamActive = true;
        deathChatActive = true;
        deathChatCounter = 0;
        deathChatLastTick = mc.theWorld.getWorldTime();
    }

    public void playWhiteNoise() {
        if (mc.sndManager != null) {
            try {
                mc.sndManager.playSoundFX("glitches.white_noise", 1.0f, 1.0f);
            } catch (Exception e) {
            }
        }
    }

    public void showScreamerInterface() {
        // Показываем скример всегда (работает и в меню, и в игре)
        if (mc != null) {
            // Воспроизводим звук перед показом GUI
            if (mc.sndManager != null) {
                try {
                    mc.sndManager.playSoundFX("glitches.white_noise", 1.0f, 1.0f);
                } catch (Exception e) {
                }
            }
            mc.displayGuiScreen(new GuiScreamer());
        }
    }

    public void showFakeBSOD() {
        // Показываем BSOD всегда (работает и в меню, и в игре)
        if (mc != null) {
            // Воспроизводим звук шума
            try {
                mc.sndManager.playSoundFX("glitches.noise0_small", 0.6f, 1.0f);
            } catch (Exception e) {
            }
            mc.displayGuiScreen(new GuiBSOD());
        }
    }

    public void startVoidHole() {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        worldEaterPlayerX = (int)mc.thePlayer.posX;
        worldEaterPlayerY = (int)mc.thePlayer.posY;
        worldEaterPlayerZ = (int)mc.thePlayer.posZ;
        worldEaterRadius = 5;
        worldEaterActive = true;
        worldEaterLastTick = mc.theWorld.getWorldTime();

        // Воспроизводим звук шума
        try {
            mc.sndManager.playSoundFX("glitches.noise0_small", 0.5f, 0.8f);
        } catch (Exception e) {
        }
    }

    public void startChatSpam() {
        if (mc == null || mc.theWorld == null) {
            return;
        }
        isChatSpamActive = true;
        chatSpamActive = true;
        chatSpamCounter = 0;
        chatSpamLastTick = mc.theWorld.getWorldTime();
    }

    public void startControlInversion() {
        if (mc.thePlayer == null) return;

        // Устанавливаем флаг инверсии управления на 10 секунд
        mc.thePlayer.addChatMessage("\u00A7c[SYSTEM] Input device error detected");

        // Активируем инверсию в EntityPlayerSP
        if (mc.thePlayer instanceof EntityPlayerSP) {
            EntityPlayerSP player = (EntityPlayerSP)mc.thePlayer;
            player.controlsInverted = true;
            player.controlsInvertedEndTime = System.currentTimeMillis() + 10000; // 10 секунд
        }
    }

    private void startShadowChat() {
        if (mc.thePlayer == null) return;

        // Активируем shadow chat на 60 секунд
        isShadowChatActive = true;
        mc.thePlayer.addChatMessage("\u00A77[SHADOW] Listening...");
    }

    private void startWorldJitter() {
        if (mc.thePlayer == null) return;

        // Активируем дрожание мира на 20 секунд
        isWorldJitterActive = true;
        long currentTick = mc.theWorld.getWorldTime();
        worldJitterEndTime = currentTick + 400; // 20 секунд = 400 тиков
        mc.thePlayer.addChatMessage("\u00A7cThe world begins to shake...");
    }

    private void startWindowShake() {
        // Инициализируем позицию окна если еще не сделано
        if (!windowShakeInitialized) {
            try {
                originalX = Display.getX();
                originalY = Display.getY();
                windowShakeInitialized = true;
            } catch (Exception e) {}
        }

        // Активируем интенсивную тряску на 15 секунд
        intensiveWindowShakeActive = true;
        intensiveWindowShakeEndTime = System.currentTimeMillis() + 15000; // 15 секунд
    }

    /**
     * Start intensive window shake effect (for multiplayer)
     */
    public void startIntensiveWindowShake(int durationTicks) {
        intensiveWindowShakeActive = true;
        long durationMs = durationTicks * 50; // Convert ticks to milliseconds (1 tick = 50ms)
        intensiveWindowShakeEndTime = System.currentTimeMillis() + durationMs;
    }

    /**
     * Apply only visual effects for bedrock tunnel event (used in multiplayer).
     * Server handles tunnel generation and teleportation.
     */
    public void applyBedrockTunnelVisuals() {
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        // Активируем инверсию цветов на 10 секунд
        isInversionActive = true;
        long currentTick = mc.theWorld.getWorldTime();
        inversionEndTime = currentTick + 200; // 10 секунд = 200 тиков

        // Активируем флаг проверки приближения к табличке
        bedrockTunnelActive = true;

    }

    public void createBedrockTunnel() {
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }


        // Туннель под землёй рядом с игроком - на минусовых координатах по Y
        int startX = (int)mc.thePlayer.posX + 20;
        int startY = 5; // Под землёй, под слоем бедрока
        int startZ = (int)mc.thePlayer.posZ + 20;

        // Бедроковая оболочка вокруг туннеля (чтобы не было утечек в пещеры)
        for (int x = -3; x <= 3; x++) {
            for (int z = -2; z <= 52; z++) {
                for (int y = -2; y <= 6; y++) {
                    int bx = startX + x;
                    int by = startY + y;
                    int bz = startZ + z;
                    if (by >= 0 && by < 128) {
                        mc.theWorld.setBlockWithNotify(bx, by, bz, Block.bedrock.blockID);
                    }
                }
            }
        }

        // Туннель 3x4 внутри длиной 50 блоков
        for (int length = 0; length < 50; length++) {
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 3; y++) {
                    int blockX = startX + x;
                    int blockY = startY + y;
                    int blockZ = startZ + length;

                    if (blockY >= 0 && blockY < 128) {
                        mc.theWorld.setBlockWithNotify(blockX, blockY, blockZ, 0);
                    }
                }
            }

            // Факелы на стенах каждые 10 блоков
            if (length % 10 == 0 && length > 0) {
                if (startY + 1 < 128) {
                    mc.theWorld.setBlockWithNotify(startX - 1, startY + 1, startZ + length, Block.torchWood.blockID);
                    mc.theWorld.setBlockWithNotify(startX + 1, startY + 1, startZ + length, Block.torchWood.blockID);
                }
            }
        }

        // Табличка в конце туннеля на стене
        int signX = startX;
        int signY = startY + 1;
        int signZ = startZ + 48;

        // Сохраняем координату таблички для проверки приближения
        tunnelSignZ = signZ;

        if (signY >= 0 && signY < 128) {
            mc.theWorld.setBlockAndMetadataWithNotify(signX, signY, signZ, Block.signWall.blockID, 2);
            TileEntitySign sign = (TileEntitySign)mc.theWorld.getBlockTileEntity(signX, signY, signZ);
            if (sign != null) {
                sign.signText[0] = "";
                sign.signText[1] = "You shouldn't";
                sign.signText[2] = "have done that";
                sign.signText[3] = "";
            }
        }

        // Телепортируем игрока в начало туннеля
        mc.thePlayer.setPositionAndRotation(startX + 0.5, startY + 1.0, startZ + 1.5, 0.0F, 0.0F);

        // Обнуляем скорость и ставим на землю
        mc.thePlayer.motionX = 0.0;
        mc.thePlayer.motionY = 0.0;
        mc.thePlayer.motionZ = 0.0;
        mc.thePlayer.fallDistance = 0.0F;
        mc.thePlayer.onGround = true;

        // Сообщение в чат
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage("\u00A74your world is fucked");
        }

        // Активируем инверсию цветов на 10 секунд
        isInversionActive = true;
        long currentTick = mc.theWorld.getWorldTime();
        inversionEndTime = currentTick + 200; // 10 секунд = 200 тиков

        // Активируем флаг проверки приближения к табличке
        bedrockTunnelActive = true;
    }

    public void queueEchoSound(String sound) {
        if (!isEchoSoundsActive) return;

        // Добавляем звук с задержкой 2-5 секунд
        long delay = 2000 + random.nextInt(3000);
        echoSoundQueue.add(new EchoSound(sound, System.currentTimeMillis() + delay));
    }

    private void triggerFinalCrash() {
        // Все крши теперь создают locked.dat
        triggerFinalLock();
    }

    private void triggerFinalCrashWithLock() {
        // Метод больше не нужен, но оставляем для совместимости
        triggerFinalLock();
    }

    /**
     * Воспроизводит случайный звук из директории glitches
     */
    public void playRandomGlitchSound() {
        if (mc == null || mc.thePlayer == null) {
            return;
        }

        // Список звуков БЕЗ цифр в конце (SoundPool обрезает цифры!)
        // ВАЖНО: используем точку вместо слэша, т.к. SoundPool заменяет / на .
        // ИСКЛЮЧЕНИЕ: noise0_small - цифра перед _ НЕ обрезается!
        String[] glitchSounds = {
            "glitches.dark",
            "glitches.error",
            "glitches.gl",        // было gl0, но SoundPool обрежет 0
            "glitches.glitch",    // было glitch0, но SoundPool обрежет 0
            "glitches.jumpscare",
            "glitches.noise0_small", // цифра перед _ НЕ обрезается!
            "glitches.white_noise"
        };

        // Выбираем случайный звук
        String selectedSound = glitchSounds[random.nextInt(glitchSounds.length)];

        try {
            // Воспроизводим звук
            mc.sndManager.playSoundFX(selectedSound, 1.0f, 1.0f);

            // Сообщаем игроку
            String soundName = selectedSound.substring(selectedSound.lastIndexOf(".") + 1);
            mc.thePlayer.addChatMessage("\u00A7aPlaying: \u00A7f" + soundName);
        } catch (Exception e) {
            mc.thePlayer.addChatMessage("\u00A7cFailed to play sound: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Воспроизводит случайный OGG звук из директории glitches
     */
    public void playRandomGlitchSoundOgg() {
        if (mc == null || mc.thePlayer == null) {
            return;
        }

        // Те же звуки, но явно указываем что это OGG
        String[] glitchSounds = {
            "glitches.dark",
            "glitches.error",
            "glitches.gl",
            "glitches.glitch",
            "glitches.jumpscare",
            "glitches.noise0_small",  // цифра перед _ НЕ обрезается!
            "glitches.white_noise"
        };

        // Выбираем случайный звук
        String selectedSound = glitchSounds[random.nextInt(glitchSounds.length)];

        try {
            // Воспроизводим звук
            mc.sndManager.playSoundFX(selectedSound, 1.0f, 1.0f);

            // Сообщаем игроку
            String soundName = selectedSound.substring(selectedSound.lastIndexOf(".") + 1);
            mc.thePlayer.addChatMessage("\u00A7b[OGG] Playing: \u00A7f" + soundName);
        } catch (Exception e) {
            mc.thePlayer.addChatMessage("\u00A7cFailed to play sound: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== ФИНАЛЬНАЯ ПОСЛЕДОВАТЕЛЬНОСТЬ ==========

    public void startFinalSequence() {
        finalSequenceActive = true;
        finalSequenceStartTime = System.currentTimeMillis();
        finalGlitchCounter = 0;

        // Активируем все визуальные эффекты одновременно
        isVHSActive = true;
        isSkyGlitchActive = true;
        isWorldMirrorActive = true;
        isFogCollapseActive = true;
        isGuiShakingActive = true;
        isBloodWaterActive = true;
        isWorldJitterActive = true;

        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage("\u00A74[SYSTEM ERROR]");
        }
    }

    private void updateFinalSequence() {
        long elapsed = System.currentTimeMillis() - finalSequenceStartTime;

        // В течение 5 секунд - интенсивные глитчи
        if (elapsed < 5000) {
            // Каждые 100мс новый глитч-эффект
            if (elapsed / 100 > finalGlitchCounter) {
                finalGlitchCounter++;

                // Случайные глитч-звуки
                String[] glitchSounds = {
                    "glitches.glitch",
                    "glitches.error",
                    "glitches.noise0_small",
                    "glitches.white_noise"
                };
                try {
                    String sound = glitchSounds[random.nextInt(glitchSounds.length)];
                    mc.sndManager.playSoundFX(sound, 1.0f, random.nextFloat() * 0.5f + 0.5f);
                } catch (Exception e) {}

                // Случайные сообщения в чат
                if (mc.thePlayer != null && random.nextFloat() < 0.3f) {
                    String[] messages = {
                        "\u00A7c[ERROR] Memory corruption detected",
                        "\u00A74[FATAL] Segmentation fault",
                        "\u00A7c[ERROR] Stack overflow",
                        "\u00A74[CRITICAL] System failure",
                        "\u00A7c404 404 404 404 404",
                        "\u00A74YOU SHOULDN'T BE HERE",
                        "\u00A7cGET OUT GET OUT GET OUT"
                    };
                    mc.thePlayer.addChatMessage(messages[random.nextInt(messages.length)]);
                }

                // Интенсивная тряска окна
                try {
                    int shakeX = random.nextInt(30) - 15;
                    int shakeY = random.nextInt(30) - 15;
                    Display.setLocation(originalX + shakeX, originalY + shakeY);
                } catch (Exception e) {}
            }
        } else {
            // После 5 секунд - краш
            triggerFinalCrash();
        }
    }

    // ========== СОХРАНЕНИЕ И ЗАГРУЗКА ==========

    /**
     * Загружает данные мистики из файла мира
     */
    public void loadFromWorld() {
        if (mc == null || mc.theWorld == null) {
            return;
        }

        try {
            // Получаем имя мира
            String worldName = mc.theWorld.worldInfo.getWorldName();
            File saveDir = new File(Minecraft.getMinecraftDir(), "saves");
            File worldDir = new File(saveDir, worldName);
            File mysticFile = new File(worldDir, "level666.dat");

            if (mysticFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(mysticFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Пропускаем комментарии
                    if (line.startsWith("#")) continue;

                    String[] parts = line.split(":");
                    if (parts.length >= 2) {
                        if (parts[0].equals("stage")) {
                            this.currentStage = Integer.parseInt(parts[1]);
                        } else if (parts[0].equals("gameStartTick")) {
                            this.gameStartTick = Long.parseLong(parts[1]);
                        } else if (parts[0].equals("speedMultiplier")) {
                            this.speedMultiplier = Float.parseFloat(parts[1]);
                        } else if (parts[0].equals("isEndMode")) {
                            this.isEndMode = Boolean.parseBoolean(parts[1]);
                        } else if (parts[0].equals("soundDistortion")) {
                            this.soundDistortion = Float.parseFloat(parts[1]);
                        } else if (parts[0].equals("initialized")) {
                            // Файл существует = инициализирован
                        } else if (parts[0].equals("event1") && parts.length >= 3) {
                            // Восстанавливаем счетчики событий Stage 1
                            String eventName = parts[1];
                            int repeatCount = Integer.parseInt(parts[2]);
                            for (MysticEvent event : stage1Events) {
                                if (event.name.equals(eventName)) {
                                    event.repeatCount = repeatCount;
                                    break;
                                }
                            }
                        } else if (parts[0].equals("event2") && parts.length >= 3) {
                            // Восстанавливаем счетчики событий Stage 2
                            String eventName = parts[1];
                            int repeatCount = Integer.parseInt(parts[2]);
                            for (MysticEvent event : stage2Events) {
                                if (event.name.equals(eventName)) {
                                    event.repeatCount = repeatCount;
                                    break;
                                }
                            }
                        } else if (parts[0].equals("event3") && parts.length >= 3) {
                            // Восстанавливаем счетчики событий Stage 3
                            String eventName = parts[1];
                            int repeatCount = Integer.parseInt(parts[2]);
                            for (MysticEvent event : stage3Events) {
                                if (event.name.equals(eventName)) {
                                    event.repeatCount = repeatCount;
                                    break;
                                }
                            }
                        } else if (parts[0].equals("event4") && parts.length >= 3) {
                            // Восстанавливаем счетчики событий Stage 4
                            String eventName = parts[1];
                            int repeatCount = Integer.parseInt(parts[2]);
                            for (MysticEvent event : stage4Events) {
                                if (event.name.equals(eventName)) {
                                    event.repeatCount = repeatCount;
                                    break;
                                }
                            }
                        }
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
            // Тихо игнорируем ошибки загрузки
        }
    }

    /**
     * Сохраняет данные мистики в файл мира
     */
    public void saveToWorld() {
        if (mc == null || mc.theWorld == null) {
            return;
        }

        try {
            // Получаем имя мира
            String worldName = mc.theWorld.worldInfo.getWorldName();
            File saveDir = new File(Minecraft.getMinecraftDir(), "saves");
            File worldDir = new File(saveDir, worldName);

            // Создаем директорию если не существует
            if (!worldDir.exists()) {
                worldDir.mkdirs();
            }

            File mysticFile = new File(worldDir, "level666.dat");
            PrintWriter writer = new PrintWriter(new FileWriter(mysticFile));

            // Основные параметры
            writer.println("stage:" + this.currentStage);
            writer.println("gameStartTick:" + this.gameStartTick);
            writer.println("speedMultiplier:" + this.speedMultiplier);
            writer.println("isEndMode:" + this.isEndMode);
            writer.println("soundDistortion:" + this.soundDistortion);
            writer.println("initialized:true");

            // Сохраняем счетчики событий для каждого этапа
            for (MysticEvent event : stage1Events) {
                writer.println("event1:" + event.name + ":" + event.repeatCount);
            }

            for (MysticEvent event : stage2Events) {
                writer.println("event2:" + event.name + ":" + event.repeatCount);
            }

            for (MysticEvent event : stage3Events) {
                writer.println("event3:" + event.name + ":" + event.repeatCount);
            }

            for (MysticEvent event : stage4Events) {
                writer.println("event4:" + event.name + ":" + event.repeatCount);
            }

            writer.close();
        } catch (Exception e) {
            // Тихо игнорируем ошибки сохранения
        }
    }

    // ========== MULTIPLAYER SUPPORT ==========

    /**
     * Set network manager for multiplayer synchronization
     */
    public void setNetworkManager(NetworkManager netManager) {
        this.networkManager = netManager;
        this.isMultiplayer = (netManager != null);
    }

    /**
     * Broadcast horror event to all players in multiplayer
     */
    public void broadcastHorrorEvent(String eventName, int stage, float intensity) {
        if (!isMultiplayer || networkManager == null) {
            return; // Singleplayer mode, no broadcast needed
        }

        try {
            // Create packet
            Packet250HorrorSync packet = new Packet250HorrorSync(eventName, stage, intensity);

            // Set sender name
            if (mc.thePlayer != null) {
                packet.senderName = mc.thePlayer.username;
            }

            // Send to server (server will broadcast to all other players)
            networkManager.addToSendQueue(packet);

        } catch (Exception e) {
            System.err.println("[MysticManager] Failed to broadcast horror event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Broadcast horror event with target player
     */
    public void broadcastHorrorEvent(String eventName, int stage, float intensity, String targetPlayer) {
        if (!isMultiplayer || networkManager == null) {
            return;
        }

        try {
            Packet250HorrorSync packet = new Packet250HorrorSync(eventName, stage, intensity, targetPlayer);

            if (mc.thePlayer != null) {
                packet.senderName = mc.thePlayer.username;
            }

            networkManager.addToSendQueue(packet);

        } catch (Exception e) {
            System.err.println("[MysticManager] Failed to broadcast targeted horror event: " + e.getMessage());
        }
    }

    /**
     * Broadcast horror event with extra data
     */
    public void broadcastHorrorEventWithData(String eventName, int stage, float intensity, String extraData) {
        if (!isMultiplayer || networkManager == null) {
            return;
        }

        try {
            Packet250HorrorSync packet = new Packet250HorrorSync(eventName, stage, intensity);
            packet.extraData = extraData;

            if (mc.thePlayer != null) {
                packet.senderName = mc.thePlayer.username;
            }

            networkManager.addToSendQueue(packet);

        } catch (Exception e) {
            System.err.println("[MysticManager] Failed to broadcast horror event with data: " + e.getMessage());
        }
    }

    /**
     * Check if in multiplayer mode (public accessor)
     */
    public boolean getIsMultiplayer() {
        return isMultiplayer;
    }
}