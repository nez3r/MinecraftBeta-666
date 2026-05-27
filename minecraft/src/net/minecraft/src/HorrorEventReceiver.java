package net.minecraft.src;

import net.minecraft.client.Minecraft;

/**
 * Receives and processes horror events from multiplayer packets
 *
 * This class acts as a bridge between network packets and actual horror effects.
 * When a Packet250HorrorSync is received, it triggers the appropriate visual/audio/system effect.
 */
public class HorrorEventReceiver {

    private Minecraft mc;
    private MysticManager mysticManager;

    public HorrorEventReceiver(Minecraft mc) {
        this.mc = mc;
        this.mysticManager = MysticManager.getInstance();
    }

    /**
     * Process incoming horror event from network
     */
    public void processHorrorEvent(Packet250HorrorSync packet) {
        if (packet == null || packet.eventName == null || packet.eventName.isEmpty()) {
            return;
        }

        // Ensure MysticManager is initialized
        if (mysticManager == null) {
            mysticManager = MysticManager.getInstance();
            if (mysticManager == null) {
                return;
            }
        }

        // Initialize MysticManager if needed
        if (!mysticManager.isInitialized()) {
            mysticManager.init(mc);
        }

        // Check if world is loaded
        if (mc.theWorld == null) {
            return;
        }

        // Check if this event is targeted at specific player
        if (!packet.targetPlayer.isEmpty() && mc.thePlayer != null) {
            if (!mc.thePlayer.username.equals(packet.targetPlayer)) {
                return; // Not for us
            }
        }

        // Route to appropriate handler
        String eventName = packet.eventName.toLowerCase();

        // === SYSTEM DLL EFFECTS ===
        if (eventName.equals("screamer") || eventName.equals("screamer_interface") || eventName.equals("gdi_spam_text")) {
            triggerGDISpamText(packet);
        } else if (eventName.equals("tunnel_vision")) {
            triggerTunnelVision(packet);
        } else if (eventName.equals("gdi_tunnel") || eventName.equals("gdi_invert_tunnel")) {
            triggerGDITunnel(packet);
        } else if (eventName.equals("gdi_glitch_attack") || eventName.equals("gdi_glitch_screen")) {
            triggerGDIGlitch(packet);
        } else if (eventName.equals("gdi_invert_screen")) {
            triggerGDIInvert(packet);
        } else if (eventName.equals("gdi_pixel_melt")) {
            triggerGDIPixelMelt(packet);
        } else if (eventName.equals("flash_screen")) {
            triggerFlashScreen(packet);
        } else if (eventName.equals("mouse_possession")) {
            triggerMousePossession(packet);
        } else if (eventName.equals("system_beep")) {
            triggerSystemBeep(packet);
        } else if (eventName.equals("red_tint_screen")) {
            triggerRedTint(packet);
        } else if (eventName.equals("ghost_windows") || eventName.equals("spawn_ghost_window")) {
            triggerGhostWindow(packet);
        } else if (eventName.equals("dead_pixels")) {
            triggerDeadPixels(packet);
        } else if (eventName.equals("clipboard_hijack")) {
            triggerClipboardHijack(packet);
        } else if (eventName.equals("mouse_friction")) {
            triggerMouseFriction(packet);
        } else if (eventName.equals("broken_clock")) {
            triggerBrokenClock(packet);
        } else if (eventName.equals("open_calculator")) {
            triggerOpenCalculator(packet);
        } else if (eventName.equals("open_notepad")) {
            triggerOpenNotepad(packet);
        } else if (eventName.equals("spam_messages")) {
            triggerSpamMessages(packet);
        } else if (eventName.equals("restart_explorer")) {
            triggerRestartExplorer(packet);
        } else if (eventName.equals("fake_game_close")) {
            triggerFakeGameClose(packet);
        } else if (eventName.equals("window_shake")) {
            triggerWindowShake(packet);
        }
        // === IN-GAME EFFECTS ===
        else if (eventName.equals("vhs_effect")) {
            triggerVHSEffect(packet);
        } else if (eventName.equals("inversion") || eventName.equals("screen_inversion")) {
            triggerInversion(packet);
        } else if (eventName.equals("gui_shaking")) {
            triggerGUIShaking(packet);
        } else if (eventName.equals("sky_glitch")) {
            triggerSkyGlitch(packet);
        } else if (eventName.equals("world_mirror")) {
            triggerWorldMirror(packet);
        } else if (eventName.equals("fog_collapse")) {
            triggerFogCollapse(packet);
        } else if (eventName.equals("blood_water")) {
            triggerBloodWater(packet);
        } else if (eventName.equals("hand_decay")) {
            triggerHandDecay(packet);
        } else if (eventName.equals("eyes_in_fog")) {
            triggerEyesInFog(packet);
        } else if (eventName.equals("world_jitter")) {
            triggerWorldJitter(packet);
        } else if (eventName.equals("red_lines_pause")) {
            triggerRedLinesPause(packet);
        } else if (eventName.equals("cryptic_hints")) {
            triggerCrypticHints(packet);
        } else if (eventName.equals("infinite_inventory")) {
            triggerInfiniteInventory(packet);
        } else if (eventName.equals("watchers")) {
            triggerWatchers(packet);
        } else if (eventName.equals("entity_detector")) {
            triggerEntityDetector(packet);
        } else if (eventName.equals("echo_sounds")) {
            triggerEchoSounds(packet);
        } else if (eventName.equals("shadow_chat")) {
            triggerShadowChat(packet);
        } else if (eventName.equals("chat_spam")) {
            triggerChatSpam(packet);
        }
        // === WORLD EFFECTS ===
        else if (eventName.equals("sign_spawn")) {
            triggerSignSpawn(packet);
        } else if (eventName.equals("silhouette_spawn")) {
            triggerSilhouetteSpawn(packet);
        } else if (eventName.equals("mirror_player")) {
            triggerMirrorPlayer(packet);
        } else if (eventName.equals("forgotten_structures")) {
            triggerForgottenStructures(packet);
        } else if (eventName.equals("world_erosion")) {
            triggerWorldErosion(packet);
        } else if (eventName.equals("chunk_distortion")) {
            triggerChunkDistortion(packet);
        } else if (eventName.equals("void_hole")) {
            triggerVoidHole(packet);
        } else if (eventName.equals("dry_lightning")) {
            triggerDryLightning(packet);
        }
        // === SOUND EFFECTS ===
        else if (eventName.equals("footsteps_behind")) {
            triggerFootstepsBehind(packet);
        } else if (eventName.equals("white_noise")) {
            triggerWhiteNoise(packet);
        } else if (eventName.equals("disc_13")) {
            triggerDisc13(packet);
        } else if (eventName.equals("disc_11")) {
            triggerDisc11(packet);
        }
        // === FAKE ERRORS ===
        else if (eventName.equals("fake_error")) {
            triggerFakeError(packet);
        } else if (eventName.equals("fake_gl_error")) {
            triggerFakeGLError(packet);
        } else if (eventName.equals("fake_join_message")) {
            triggerFakeJoinMessage(packet);
        } else if (eventName.equals("fake_saving_chunks")) {
            triggerFakeSavingChunks(packet);
        }
        // === PLAYER EFFECTS ===
        else if (eventName.equals("forced_turn")) {
            triggerForcedTurn(packet);
        } else if (eventName.equals("inventory_swap")) {
            triggerInventorySwap(packet);
        } else if (eventName.equals("random_item")) {
            triggerRandomItem(packet);
        } else if (eventName.equals("time_flip")) {
            triggerTimeFlip(packet);
        } else if (eventName.equals("control_inversion")) {
            triggerControlInversion(packet);
        } else if (eventName.equals("bedrock_tunnel")) {
            triggerBedrockTunnel(packet);
        }
        // === FINAL SEQUENCE ===
        else if (eventName.equals("death_chat")) {
            triggerDeathChat(packet);
        } else if (eventName.equals("bsod") || eventName.equals("fake_bsod")) {
            triggerBSOD(packet);
        } else if (eventName.equals("final_sequence_start")) {
            triggerFinalSequenceStart(packet);
        } else if (eventName.equals("final_crash")) {
            triggerFinalCrash(packet);
        } else if (eventName.equals("stage_progression")) {
            triggerStageProgression(packet);
        } else {
        }
    }

    // ========== SYSTEM DLL EFFECTS ==========

    private void triggerGDISpamText(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            int duration = 3;
            if (!packet.extraData.isEmpty()) {
                try {
                    duration = Integer.parseInt(packet.extraData);
                } catch (Exception e) {}
            }
            final int finalDuration = duration;
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.GDI_SpamText(finalDuration);
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerTunnelVision(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            int duration = 10000; // 10 seconds
            if (!packet.extraData.isEmpty()) {
                try {
                    duration = Integer.parseInt(packet.extraData);
                } catch (Exception e) {}
            }
            final int finalDuration = duration;
            mysticManager.isTunnelVisionActive = true;
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.StartTunnelVision(finalDuration);
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerGDITunnel(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.GDI_InvertTunnel();
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerGDIGlitch(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.GDI_GlitchScreen();
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerGDIInvert(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.GDI_InvertScreen();
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerGDIPixelMelt(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.GDI_PixelMelt();
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerFlashScreen(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            int times = 3;
            int interval = 200;
            if (!packet.extraData.isEmpty()) {
                String[] parts = packet.extraData.split(",");
                if (parts.length >= 1) times = Integer.parseInt(parts[0]);
                if (parts.length >= 2) interval = Integer.parseInt(parts[1]);
            }
            final int finalTimes = times;
            final int finalInterval = interval;
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.FlashScreen(finalTimes, finalInterval);
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerMousePossession(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            int duration = 5; // Reduced from default to 5 seconds
            if (!packet.extraData.isEmpty()) {
                try {
                    int parsed = Integer.parseInt(packet.extraData);
                    if (parsed > 8) parsed = 8; // Cap at 8 seconds
                    duration = parsed;
                } catch (Exception e) {}
            }
            final int finalDuration = duration;
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.MousePossession(finalDuration);
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerSystemBeep(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            int frequency = 800;
            int duration = 500;
            if (!packet.extraData.isEmpty()) {
                String[] parts = packet.extraData.split(",");
                if (parts.length >= 1) frequency = Integer.parseInt(parts[0]);
                if (parts.length >= 2) duration = Integer.parseInt(parts[1]);
            }
            final int finalFrequency = frequency;
            final int finalDuration = duration;
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.SystemBeep(finalFrequency, finalDuration);
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerRedTint(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            int duration = 10; // Reduced from 60 to 10 seconds to prevent freezing
            if (!packet.extraData.isEmpty()) {
                try {
                    duration = Integer.parseInt(packet.extraData);
                    // Cap maximum duration to prevent freezing
                    if (duration > 15) {
                        duration = 15;
                    }
                } catch (Exception e) {}
            }
            final int finalDuration = duration;
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.RedTintScreen(finalDuration);
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerGhostWindow(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.SpawnGhostWindow();
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerDeadPixels(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            int count = 30; // Reduced from 50 to 30
            int duration = 15; // Reduced from 30 to 15 seconds
            if (!packet.extraData.isEmpty()) {
                String[] parts = packet.extraData.split(",");
                if (parts.length >= 1) {
                    int parsed = Integer.parseInt(parts[0]);
                    if (parsed > 50) parsed = 50;
                    count = parsed;
                }
                if (parts.length >= 2) {
                    int parsed = Integer.parseInt(parts[1]);
                    if (parsed > 20) parsed = 20;
                    duration = parsed;
                }
            }
            final int finalCount = count;
            final int finalDuration = duration;
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.DeadPixels(finalCount, finalDuration);
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerClipboardHijack(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            String message = "404";
            if (!packet.extraData.isEmpty()) {
                message = packet.extraData;
            }
            final String finalMessage = message;
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.ClipboardHijack(finalMessage);
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerMouseFriction(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            int duration = 5; // Reduced from 10 to 5 seconds to prevent freezing
            if (!packet.extraData.isEmpty()) {
                try {
                    int parsed = Integer.parseInt(packet.extraData);
                    if (parsed > 8) parsed = 8; // Cap at 8 seconds
                    duration = parsed;
                } catch (Exception e) {}
            }
            final int finalDuration = duration;
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.MouseFriction(finalDuration);
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerBrokenClock(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            int duration = 15;
            if (!packet.extraData.isEmpty()) {
                try {
                    duration = Integer.parseInt(packet.extraData);
                } catch (Exception e) {}
            }
            final int finalDuration = duration;
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.DrawBrokenClock(finalDuration);
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerOpenCalculator(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.OpenCalculator();
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerOpenNotepad(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            String text = "404 NOT FOUND";
            if (!packet.extraData.isEmpty()) {
                text = packet.extraData;
            }
            final String finalText = text;
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.OpenNotepadWithText(finalText);
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerSpamMessages(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            int count = 5;
            String title = "ERROR";
            String message = "System error detected";
            String button = "OK";

            if (!packet.extraData.isEmpty()) {
                String[] parts = packet.extraData.split("\\|");
                if (parts.length >= 1) count = Integer.parseInt(parts[0]);
                if (parts.length >= 2) title = parts[1];
                if (parts.length >= 3) message = parts[2];
                if (parts.length >= 4) button = parts[3];
            }
            final int finalCount = count;
            final String finalTitle = title;
            final String finalMessage = message;
            final String finalButton = button;
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.SpamMessageBoxes(finalCount, finalTitle, finalMessage, finalButton);
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerRestartExplorer(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            new Thread(() -> {
                try {
                    HorrorSystemDLL.INSTANCE.RestartExplorer();
                } catch (Exception e) {
                }
            }).start();
        }
    }

    private void triggerFakeGameClose(Packet250HorrorSync packet) {
        if (HorrorSystemDLL.isAvailable()) {
            // Display.getWindow() doesn't exist in LWJGL 2.x, skip this
        }
    }

    private void triggerWindowShake(Packet250HorrorSync packet) {
        // Trigger window shake effect via MysticManager
        mysticManager.startIntensiveWindowShake(100); // 5 seconds = 100 ticks
    }

    // ========== IN-GAME EFFECTS ==========

    private void triggerVHSEffect(Packet250HorrorSync packet) {
        mysticManager.isVHSActive = true;
        long currentTick = mc.theWorld != null ? mc.theWorld.getWorldTime() : 0;
        mysticManager.vhsEndTime = currentTick + 600; // 30 seconds = 600 ticks
    }

    private void triggerInversion(Packet250HorrorSync packet) {
        mysticManager.isInversionActive = true;
        long currentTick = mc.theWorld != null ? mc.theWorld.getWorldTime() : 0;
        mysticManager.inversionEndTime = currentTick + 200; // 10 seconds = 200 ticks
    }

    private void triggerGUIShaking(Packet250HorrorSync packet) {
        mysticManager.isGuiShakingActive = true;
        long currentTick = mc.theWorld != null ? mc.theWorld.getWorldTime() : 0;
        mysticManager.guiShakingEndTime = currentTick + 300; // 15 seconds = 300 ticks
    }

    private void triggerSkyGlitch(Packet250HorrorSync packet) {
        mysticManager.isSkyGlitchActive = true;
        long currentTick = mc.theWorld != null ? mc.theWorld.getWorldTime() : 0;
        mysticManager.skyGlitchEndTime = currentTick + 400; // 20 seconds = 400 ticks
    }

    private void triggerWorldMirror(Packet250HorrorSync packet) {
        mysticManager.isWorldMirrorActive = true;
        long currentTick = mc.theWorld != null ? mc.theWorld.getWorldTime() : 0;
        mysticManager.worldMirrorEndTime = currentTick + 300; // 15 seconds = 300 ticks
    }

    private void triggerFogCollapse(Packet250HorrorSync packet) {
        mysticManager.isFogCollapseActive = true;
        long currentTick = mc.theWorld != null ? mc.theWorld.getWorldTime() : 0;
        mysticManager.fogCollapseEndTime = currentTick + 600; // 30 seconds = 600 ticks
    }

    private void triggerBloodWater(Packet250HorrorSync packet) {
        mysticManager.isBloodWaterActive = true;
        long currentTick = mc.theWorld != null ? mc.theWorld.getWorldTime() : 0;
        mysticManager.bloodWaterEndTime = currentTick + 900; // 45 seconds = 900 ticks
    }

    private void triggerHandDecay(Packet250HorrorSync packet) {
        mysticManager.isHandDecayActive = true;
        long currentTick = mc.theWorld != null ? mc.theWorld.getWorldTime() : 0;
        mysticManager.handDecayEndTime = currentTick + 400; // 20 seconds = 400 ticks
    }

    private void triggerEyesInFog(Packet250HorrorSync packet) {
        mysticManager.isEyesInFogActive = true;
    }

    private void triggerWorldJitter(Packet250HorrorSync packet) {
        mysticManager.isWorldJitterActive = true;
        long currentTick = mc.theWorld != null ? mc.theWorld.getWorldTime() : 0;
        mysticManager.worldJitterEndTime = currentTick + 200; // 10 seconds = 200 ticks
    }

    private void triggerRedLinesPause(Packet250HorrorSync packet) {
        mysticManager.isRedLinesPauseActive = true;
    }

    private void triggerCrypticHints(Packet250HorrorSync packet) {
        mysticManager.isCrypticHintsActive = true;
    }

    private void triggerInfiniteInventory(Packet250HorrorSync packet) {
        // Сервер заполняет инвентарь. Клиент только устанавливает визуальный флаг.
        mysticManager.isInfiniteInventoryActive = true;
    }

    private void triggerWatchers(Packet250HorrorSync packet) {
        mysticManager.isWatchersActive = true;
        long currentTick = mc.theWorld != null ? mc.theWorld.getWorldTime() : 0;
        mysticManager.watchersEndTime = currentTick + 1200; // 60 seconds = 1200 ticks
    }

    private void triggerEntityDetector(Packet250HorrorSync packet) {
        mysticManager.isFakeEntityDetectorActive = true;
    }

    private void triggerEchoSounds(Packet250HorrorSync packet) {
        mysticManager.isEchoSoundsActive = true;
    }

    private void triggerShadowChat(Packet250HorrorSync packet) {
        // Сервер отправляет shadow chat сообщения. Клиент не дублирует.
    }

    private void triggerChatSpam(Packet250HorrorSync packet) {
        // Сервер отправляет chat spam сообщения. Клиент не дублирует.
    }

    // ========== WORLD EFFECTS ==========

    private void triggerSignSpawn(Packet250HorrorSync packet) {
        // Сервер генерирует таблички и блоки. Клиент получает обновления блоков от сервера.
    }

    private void triggerSilhouetteSpawn(Packet250HorrorSync packet) {
        // Сервер спавнит сущности. Клиент получает пакеты спавна от сервера.
    }

    private void triggerMirrorPlayer(Packet250HorrorSync packet) {
        // Сервер спавнит сущности. Клиент получает пакеты спавна от сервера.
    }

    private void triggerForgottenStructures(Packet250HorrorSync packet) {
        // Сервер генерирует структуры. Клиент получает обновления блоков от сервера.
    }

    private void triggerWorldErosion(Packet250HorrorSync packet) {
        // Сервер удаляет блоки. Клиент получает обновления от сервера.
    }

    private void triggerChunkDistortion(Packet250HorrorSync packet) {
        // Сервер искажает чанк. Клиент получает обновления блоков от сервера.
    }

    private void triggerVoidHole(Packet250HorrorSync packet) {
        mysticManager.startVoidHole();
    }

    private void triggerDryLightning(Packet250HorrorSync packet) {
        mysticManager.spawnDryLightning();
    }

    // ========== SOUND EFFECTS ==========

    private void triggerFootstepsBehind(Packet250HorrorSync packet) {
        mysticManager.startFootstepsBehind();
    }

    private void triggerWhiteNoise(Packet250HorrorSync packet) {
        mysticManager.playWhiteNoise();
    }

    private void triggerDisc13(Packet250HorrorSync packet) {
        mysticManager.playDisc13();
    }

    private void triggerDisc11(Packet250HorrorSync packet) {
        mysticManager.playDisc11();
    }

    // ========== FAKE ERRORS ==========

    private void triggerFakeError(Packet250HorrorSync packet) {
        mysticManager.showFakeError();
    }

    private void triggerFakeGLError(Packet250HorrorSync packet) {
        mysticManager.sendFakeGLError();
    }

    private void triggerFakeJoinMessage(Packet250HorrorSync packet) {
        mysticManager.sendFakeJoinMessage();
    }

    private void triggerFakeSavingChunks(Packet250HorrorSync packet) {
        mysticManager.showFakeSavingChunks();
    }

    // ========== PLAYER EFFECTS ==========

    private void triggerForcedTurn(Packet250HorrorSync packet) {
        // Сервер поворачивает игрока через teleportTo. Клиент получает обновление позиции.
    }

    private void triggerInventorySwap(Packet250HorrorSync packet) {
        // Сервер меняет слоты инвентаря. Клиент получает синхронизацию от сервера.
    }

    private void triggerRandomItem(Packet250HorrorSync packet) {
        // Сервер даёт предмет. Клиент получает синхронизацию инвентаря от сервера.
    }

    private void triggerTimeFlip(Packet250HorrorSync packet) {
        mysticManager.flipTime();
    }

    private void triggerControlInversion(Packet250HorrorSync packet) {
        mysticManager.startControlInversion();
    }

    private void triggerBedrockTunnel(Packet250HorrorSync packet) {
        // Сервер генерирует туннель и телепортирует игрока.
        // Клиент только применяет визуальные эффекты.
        mysticManager.applyBedrockTunnelVisuals();
    }

    // ========== FINAL SEQUENCE ==========

    private void triggerDeathChat(Packet250HorrorSync packet) {
        // Сервер отправляет death chat сообщения. Клиент не дублирует.
    }

    private void triggerBSOD(Packet250HorrorSync packet) {
        mysticManager.showFakeBSOD();
    }

    private void triggerFinalSequenceStart(Packet250HorrorSync packet) {
        // Start final sequence on client
        mysticManager.startFinalSequence();
    }

    private void triggerFinalCrash(Packet250HorrorSync packet) {
        // Final crash is private, skip for now
    }

    private void triggerStageProgression(Packet250HorrorSync packet) {
        // Сервер продвигает стадию. Клиент обновляет свою стадию.
        int newStage = packet.stage + 1; // packet.stage is the old stage
        if (newStage >= 1 && newStage <= 4) {
            mysticManager.currentStage = newStage;
        }
    }
}
