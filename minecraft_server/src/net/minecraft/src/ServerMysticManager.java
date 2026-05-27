package net.minecraft.src;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;

/**
 * Server-side MysticManager that generates horror events and broadcasts them to all players
 *
 * This manager runs on the server and is responsible for:
 * - Generating horror events at appropriate intervals
 * - Managing stage progress
 * ion
 * - Broadcasting events to all connected players via Packet250HorrorSync
 * - Saving/loading mystic state
 * - Pausing when no players online
 */
public class ServerMysticManager {

    private static ServerMysticManager instance;
    private MinecraftServer server;
    private boolean initialized = false;

    // Stage progression
    public int currentStage = 1;

    // Timing (using ticks instead of milliseconds)
    private long gameStartTick = 0;
    private long lastEventTick = 0;
    private long nextEventDelayTicks = 0;
    private long pausedTick = 0; // When paused due to no players
    private boolean isPaused = false;

    // Speed multiplier
    private float speedMultiplier = 2.0f;

    // Death chat state
    private boolean deathChatActive = false;
    private int deathChatCounter = 0;
    private long deathChatLastTick = 0;

    // Chat spam state
    private boolean chatSpamActive = false;
    private int chatSpamCounter = 0;
    private long chatSpamLastTick = 0;

    // Shadow chat state
    private boolean shadowChatActive = false;
    private String shadowChatMessage = "";
    private long shadowChatTick = 0;

    // Event lists for each stage
    private List<MysticEvent> stage1Events = new ArrayList<MysticEvent>();
    private List<MysticEvent> stage2Events = new ArrayList<MysticEvent>();
    private List<MysticEvent> stage3Events = new ArrayList<MysticEvent>();
    private List<MysticEvent> stage4Events = new ArrayList<MysticEvent>();

    public Random random = new Random();

    // Final sequence
    public boolean finalSequenceActive = false;
    private long finalSequenceStartTick = 0;
    private int finalEventCounter = 0;

    // Save file
    private static final String SAVE_FILE = "mystic_state.dat";

    private ServerMysticManager(MinecraftServer server) {
        this.server = server;
    }

    public static ServerMysticManager getInstance(MinecraftServer server) {
        if (instance == null) {
            instance = new ServerMysticManager(server);
        }
        return instance;
    }

    /**
     * Initialize the horror event system
     */
    public void initialize() {
        if (initialized) {
            return;
        }


        initializeEvents();
        loadState(); // Load saved state
        initialized = true;

    }

    private void initializeEvents() {
        // Stage 1 Events (50-80 seconds intervals) - из старой версии
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

        // Stage 2 Events (60-90 seconds intervals) - из старой версии
        stage2Events.add(new MysticEvent("silhouette_spawn", 2, true)); // повторяющееся
        stage2Events.add(new MysticEvent("time_flip", 2, true)); // теперь повторяющееся
        stage2Events.add(new MysticEvent("footsteps_behind", 2, true)); // теперь повторяющееся
        stage2Events.add(new MysticEvent("inventory_swap", 2, true)); // повторяющееся
        stage2Events.add(new MysticEvent("eyes_in_fog", 2, true)); // теперь повторяющееся
        stage2Events.add(new MysticEvent("disc_11", 2, true)); // теперь повторяющееся
        stage2Events.add(new MysticEvent("gdi_tunnel", 2, true)); // теперь повторяющееся
        stage2Events.add(new MysticEvent("world_erosion", 2, true)); // повторяющееся

        // Stage 3 Events (30-50 seconds intervals) - из старой версии
        stage3Events.add(new MysticEvent("fake_join_message", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("entity_detector", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("fake_saving_chunks", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("hand_decay", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("fake_gl_error", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("tunnel_vision", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("open_calculator", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("spam_messages", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("open_notepad", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("gdi_glitch_attack", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("gdi_spam_text", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("mouse_possession", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("red_tint_screen", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("ghost_windows", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("dead_pixels", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("clipboard_hijack", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("mouse_friction", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("system_beep", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("fake_game_close", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("broken_clock", 3, true)); // теперь повторяющееся
        stage3Events.add(new MysticEvent("mirror_player", 3, true)); // повторяющееся
        stage3Events.add(new MysticEvent("forgotten_structures", 3, true)); // повторяющееся
        stage3Events.add(new MysticEvent("echo_sounds", 3, true)); // теперь повторяющееся

        // Stage 4 Events (40-70 seconds intervals) - из старой версии
        stage4Events.add(new MysticEvent("chunk_distortion", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("blood_water", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("forced_turn", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("fog_collapse", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("sky_glitch", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("infinite_inventory", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("restart_explorer", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("death_chat", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("white_noise", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("screamer_interface", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("fake_bsod", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("void_hole", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("chat_spam", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("control_inversion", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("shadow_chat", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("world_jitter", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("bedrock_tunnel", 4, true)); // теперь повторяющееся
        stage4Events.add(new MysticEvent("final_crash", 4)); // только один раз
    }

    /**
     * Main update method - call this every server tick
     */
    public void update() {
        if (!initialized) {
            return;
        }

        // Check if any players are online
        int playerCount = server.configManager.playerEntities.size();
        if (playerCount == 0) {
            if (!isPaused) {
                pausedTick = server.worldMngr[0].getWorldTime();
                isPaused = true;
            }
            return;
        } else if (isPaused) {
            // Resume from pause
            if (pausedTick > 0) {
                long pauseDuration = server.worldMngr[0].getWorldTime() - pausedTick;
                gameStartTick += pauseDuration;
                lastEventTick += pauseDuration;
            }

            isPaused = false;
        }

        long currentTick = server.worldMngr[0].getWorldTime();

        // Start the horror sequence if not started
        if (gameStartTick == 0) {
            gameStartTick = currentTick;
            lastEventTick = gameStartTick;
            scheduleNextEvent();
        }

        // Periodic save (every 5 minutes = 6000 ticks)
        if (currentTick % 6000 == 0 && currentTick > gameStartTick) {
            saveState();
        }

        // Handle final sequence
        if (finalSequenceActive) {
            handleFinalSequence();
            return;
        }

        // Time-based stage progression removed - progression now happens only through event exhaustion
        // This matches single-player behavior where stages progress naturally as events are used up

        // Process death_chat messages (50 messages every 2 ticks)
        if (deathChatActive) {
            if (currentTick - deathChatLastTick >= 2 && deathChatCounter < 50) {
                String[] messages = {
                    "\u00A7cDeleting C:/Users/Documents...",
                    "\u00A7cAccess denied: Desktop",
                    "\u00A7cSystem32 corrupted",
                    "\u00A7cPlayer404: I see you",
                    "\u00A7cERROR: Memory leak detected",
                    "\u00A7cjava.lang.NullPointerException at World.class:404",
                    "\u00A7cFATAL: Heap space exceeded",
                    "\u00A7c[SYSTEM] Formatting C:\\ drive...",
                    "\u00A7cPlayer404: Did you miss me?",
                    "\u00A7cERROR: File not found: reality.dll"
                };
                String msg = messages[random.nextInt(messages.length)];
                server.configManager.sendPacketToAllPlayers(new Packet3Chat(msg));
                deathChatCounter++;
                deathChatLastTick = currentTick;
            } else if (deathChatCounter >= 50) {
                deathChatActive = false;
            }
        }

        // Process chat_spam (100 messages every tick)
        if (chatSpamActive) {
            if (currentTick - chatSpamLastTick >= 1 && chatSpamCounter < 100) {
                StringBuilder spam = new StringBuilder("\u00A7c");
                for (int j = 0; j < 20; j++) {
                    spam.append((char)(random.nextInt(94) + 33));
                }
                server.configManager.sendPacketToAllPlayers(new Packet3Chat(spam.toString()));
                chatSpamCounter++;
                chatSpamLastTick = currentTick;
            } else if (chatSpamCounter >= 100) {
                chatSpamActive = false;
            }
        }

        // Process shadow_chat
        if (shadowChatActive && currentTick >= shadowChatTick) {
            StringBuilder distorted = new StringBuilder();
            for (char c : shadowChatMessage.toCharArray()) {
                if (random.nextFloat() < 0.3f) {
                    distorted.append((char)(random.nextInt(94) + 33));
                } else {
                    distorted.append(c);
                }
            }
            server.configManager.sendPacketToAllPlayers(new Packet3Chat("\u00A77<Player404> " + distorted.toString()));
            shadowChatMessage = "";
            shadowChatActive = false;
        }

        // Check if it's time for next event
        if (currentTick >= lastEventTick + nextEventDelayTicks) {
            triggerNextEvent();
        }
    }

    private void scheduleNextEvent() {
        int minDelayTicks = 0;
        int maxDelayTicks = 0;

        // 20 ticks = 1 second, 1200 ticks = 1 minute
        // Рассчитано для ~45 минут прохождения при x2 скорости
        switch (currentStage) {
            case 1:
                minDelayTicks = 40 * 20; // 40 seconds
                maxDelayTicks = 70 * 20; // 70 seconds
                break;
            case 2:
                minDelayTicks = 35 * 20; // 35 seconds
                maxDelayTicks = 60 * 20; // 60 seconds
                break;
            case 3:
                minDelayTicks = 20 * 20; // 20 seconds
                maxDelayTicks = 40 * 20; // 40 seconds
                break;
            case 4:
                minDelayTicks = 15 * 20; // 15 seconds
                maxDelayTicks = 30 * 20; // 30 seconds
                break;
        }

        // Apply speed multiplier
        minDelayTicks = (int)(minDelayTicks / speedMultiplier);
        maxDelayTicks = (int)(maxDelayTicks / speedMultiplier);

        // Ensure minimum delay of at least 1 tick
        if (minDelayTicks < 1) {
            minDelayTicks = 1;
        }

        // Prevent division by zero and ensure range
        if (maxDelayTicks <= minDelayTicks) {
            maxDelayTicks = minDelayTicks + 1;
        }

        nextEventDelayTicks = minDelayTicks + random.nextInt(maxDelayTicks - minDelayTicks);

    }

    public void triggerNextEvent() {

        List<MysticEvent> availableEvents = getCurrentStageEvents();
        if (availableEvents.isEmpty()) {
            return;
        }


        // Find events that can still be triggered (matching client logic)
        List<MysticEvent> validEvents = new ArrayList<MysticEvent>();
        for (MysticEvent event : availableEvents) {
            if (event.canTrigger()) {
                validEvents.add(event);
            }
        }


        if (validEvents.isEmpty()) {
            // No valid events, progress to next stage
            if (currentStage < 4) {
                currentStage++;
                broadcastEventToAllPlayers("stage_progression", currentStage - 1, 1.0f);
                scheduleNextEvent();
                return;
            } else {
                // Start final sequence
                startFinalSequence();
                return;
            }
        }

        // Select random event
        MysticEvent selectedEvent = validEvents.get(random.nextInt(validEvents.size()));

        // Increment trigger counter
        selectedEvent.trigger();


        // Broadcast event to all players
        broadcastEventToAllPlayers(selectedEvent.name, currentStage, 1.0f);

        // Update timing
        lastEventTick = server.worldMngr[0].getWorldTime();
        scheduleNextEvent();
    }

    private List<MysticEvent> getCurrentStageEvents() {
        switch (currentStage) {
            case 1: return stage1Events;
            case 2: return stage2Events;
            case 3: return stage3Events;
            case 4: return stage4Events;
            default: return new ArrayList<MysticEvent>();
        }
    }

    private void startFinalSequence() {
        finalSequenceActive = true;
        finalSequenceStartTick = server.worldMngr[0].getWorldTime();
        finalEventCounter = 0;


        // Broadcast initial final sequence events
        broadcastEventToAllPlayers("gdi_glitch_screen", 4, 2.0f);
        broadcastEventToAllPlayers("final_sequence_start", 4, 2.0f);
    }

    private void handleFinalSequence() {
        long ticksSinceStart = server.worldMngr[0].getWorldTime() - finalSequenceStartTick;

        // Trigger events at specific intervals
        if (ticksSinceStart == 100) { // 5 seconds
            broadcastEventToAllPlayers("red_tint_screen", 4, 3.0f);
        } else if (ticksSinceStart == 200) { // 10 seconds
            broadcastEventToAllPlayers("screamer", 4, 3.0f);
        } else if (ticksSinceStart == 400) { // 20 seconds
            broadcastEventToAllPlayers("bsod", 4, 3.0f);
        }

        // After 2 minutes (2400 ticks), trigger final crash
        if (ticksSinceStart >= 2400) {
            broadcastEventToAllPlayers("final_crash", 4, 3.0f);
            finalSequenceActive = false;
        }
    }

    /**
     * Broadcast horror event to all players
     */
    public void broadcastEventToAllPlayers(String eventName, int stage, float intensity) {
        if (server.configManager == null || server.configManager.playerEntities.isEmpty()) {
            return;
        }


        // Handle server-side effects for events that need server authority
        handleServerSideEffects(eventName, stage, intensity);

        // Create and send packet to all players
        Packet250HorrorSync packet = new Packet250HorrorSync();
        packet.eventName = eventName;
        packet.stage = stage;
        packet.intensity = intensity;
        packet.senderName = "SERVER";
        packet.targetPlayer = ""; // Broadcast to all

        server.configManager.sendPacketToAllPlayers(packet);
    }

    /**
     * Handle server-side effects for events that need server authority
     */
    private void handleServerSideEffects(String eventName, int stage, float intensity) {
        List<EntityPlayerMP> players = server.configManager.playerEntities;
        if (players.isEmpty()) {
            return;
        }
        
        // Inventory-related events
        if (eventName.equals("random_item")) {
            for (EntityPlayerMP player : players) {
                giveRandomItem(player);
            }
        } else if (eventName.equals("inventory_swap")) {
            for (EntityPlayerMP player : players) {
                swapInventorySlots(player);
            }
        } else if (eventName.equals("infinite_inventory")) {
            for (EntityPlayerMP player : players) {
                fillInventoryRandom(player);
            }
        }
        
        // World modification events
        else if (eventName.equals("bedrock_tunnel")) {
            EntityPlayerMP targetPlayer = players.get(random.nextInt(players.size()));
            teleportToBedrockTunnel(targetPlayer);
        } else if (eventName.equals("chunk_distortion")) {
            for (EntityPlayerMP player : players) {
                applyChunkDistortion(player);
            }
        } else if (eventName.equals("world_erosion")) {
            for (EntityPlayerMP player : players) {
                applyWorldErosion(player);
            }
        } else if (eventName.equals("void_hole")) {
            EntityPlayerMP targetPlayer = players.get(random.nextInt(players.size()));
            createVoidHole(targetPlayer);
        } else if (eventName.equals("world_jitter")) {
            for (EntityPlayerMP player : players) {
                applyWorldJitter(player);
            }
        } else if (eventName.equals("blood_water")) {
            applyBloodWater(players);
        } else if (eventName.equals("fog_collapse")) {
            for (EntityPlayerMP player : players) {
                applyFogCollapse(player);
            }
        }
        
        // Sign/structure events
        else if (eventName.equals("sign_spawn")) {
            for (EntityPlayerMP player : players) {
                spawnMysticSigns(player);
            }
        } else if (eventName.equals("forgotten_structures")) {
            for (EntityPlayerMP player : players) {
                spawnForgottenStructures(player);
            }
        }
        
        // Player manipulation events
        else if (eventName.equals("forced_turn")) {
            for (EntityPlayerMP player : players) {
                forceTurn180(player);
            }
        }
        
        // Chat events (server sends messages directly)
        else if (eventName.equals("death_chat")) {
            deathChatActive = true;
            deathChatCounter = 0;
            deathChatLastTick = 0;
        } else if (eventName.equals("chat_spam")) {
            chatSpamActive = true;
            chatSpamCounter = 0;
            chatSpamLastTick = 0;
        } else if (eventName.equals("shadow_chat")) {
            String[] messages = {"help me", "I can see you", "404", "don't look behind you", "it's too late"};
            shadowChatMessage = messages[random.nextInt(messages.length)];
            shadowChatTick = server.worldMngr[0].getWorldTime() + 100; // 5 second delay
            shadowChatActive = true;
        }
        
        // Entity spawn events
        else if (eventName.equals("silhouette_spawn")) {
            for (EntityPlayerMP player : players) {
                spawnSilhouette(player);
            }
        } else if (eventName.equals("mirror_player")) {
            for (EntityPlayerMP player : players) {
                spawnMirrorPlayer(player);
            }
        }
    }
    
    /**
     * Swap inventory slots for player
     */
    private void swapInventorySlots(EntityPlayerMP player) {
        if (player.inventory == null || player.inventory.mainInventory.length < 2) {
            return;
        }
        
        try {
            // Swap random slots in main inventory
            int slot1 = random.nextInt(player.inventory.mainInventory.length);
            int slot2 = random.nextInt(player.inventory.mainInventory.length);
            
            if (slot1 != slot2) {
                ItemStack temp = player.inventory.mainInventory[slot1];
                player.inventory.mainInventory[slot1] = player.inventory.mainInventory[slot2];
                player.inventory.mainInventory[slot2] = temp;
                
                // Sync inventory changes
                syncPlayerInventory(player);
            }
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error swapping inventory: " + e.getMessage());
        }
    }
    
    /**
     * Apply chunk distortion effects
     */
    private void applyChunkDistortion(EntityPlayerMP player) {
        try {
            WorldServer world = (WorldServer)player.worldObj;
            int playerX = (int)player.posX;
            int playerZ = (int)player.posZ;
            
            // Apply random block changes in small radius around player
            for (int i = 0; i < 10; i++) {
                int x = playerX + random.nextInt(20) - 10;
                int z = playerZ + random.nextInt(20) - 10;
                int y = (int)player.posY + random.nextInt(10) - 5;
                
                if (y > 0 && y < 128) {
                    int currentBlock = world.getBlockId(x, y, z);
                    if (currentBlock != 0 && currentBlock != Block.bedrock.blockID) {
                        // Replace with random block
                        int randomBlock = random.nextInt(5) + 1; // Stone, dirt, etc.
                        world.setBlockWithNotify(x, y, z, randomBlock);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error applying chunk distortion: " + e.getMessage());
        }
    }
    
    /**
     * Apply world erosion effects
     */
    private void applyWorldErosion(EntityPlayerMP player) {
        try {
            WorldServer world = (WorldServer)player.worldObj;
            int playerX = (int)player.posX;
            int playerZ = (int)player.posZ;
            
            // Remove random surface blocks around player
            for (int i = 0; i < 15; i++) {
                int x = playerX + random.nextInt(30) - 15;
                int z = playerZ + random.nextInt(30) - 15;
                int y = world.getHeightValue(x, z);
                
                if (y > 0 && y < 128) {
                    int block = world.getBlockId(x, y, z);
                    if (block != Block.bedrock.blockID && block != Block.waterMoving.blockID && block != Block.waterStill.blockID) {
                        world.setBlockWithNotify(x, y, z, 0); // Air
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error applying world erosion: " + e.getMessage());
        }
    }
    
    /**
     * Create void hole under player
     */
    private void createVoidHole(EntityPlayerMP player) {
        try {
            WorldServer world = (WorldServer)player.worldObj;
            int holeX = (int)player.posX;
            int holeZ = (int)player.posZ;
            
            // Create 5x5 void hole down to bedrock
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    for (int y = (int)player.posY - 5; y >= 0; y--) {
                        int block = world.getBlockId(holeX + x, y, holeZ + z);
                        if (block != Block.bedrock.blockID) {
                            world.setBlockWithNotify(holeX + x, y, holeZ + z, 0); // Air
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error creating void hole: " + e.getMessage());
        }
    }
    
    /**
     * Apply world jitter effects
     */
    private void applyWorldJitter(EntityPlayerMP player) {
        try {
            WorldServer world = (WorldServer)player.worldObj;
            int playerX = (int)player.posX;
            int playerZ = (int)player.posZ;
            
            // Randomly change blocks around player
            for (int i = 0; i < 20; i++) {
                int x = playerX + random.nextInt(15) - 7;
                int z = playerZ + random.nextInt(15) - 7;
                int y = (int)player.posY + random.nextInt(8) - 4;
                
                if (y > 0 && y < 128) {
                    int currentBlock = world.getBlockId(x, y, z);
                    if (currentBlock != 0 && currentBlock != Block.bedrock.blockID) {
                        // Randomly replace or remove
                        if (random.nextBoolean()) {
                            world.setBlockWithNotify(x, y, z, 0); // Remove
                        } else {
                            world.setBlockWithNotify(x, y, z, random.nextInt(10) + 1); // Replace
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error applying world jitter: " + e.getMessage());
        }
    }
    
    /**
     * Turn water to blood for all players
     */
    private void applyBloodWater(List<EntityPlayerMP> players) {
        try {
            for (EntityPlayerMP player : players) {
                WorldServer world = (WorldServer)player.worldObj;
                int playerX = (int)player.posX;
                int playerZ = (int)player.posZ;
                
                // Turn water to lava in radius around player
                for (int x = playerX - 20; x <= playerX + 20; x++) {
                    for (int z = playerZ - 20; z <= playerZ + 20; z++) {
                        for (int y = 0; y < 128; y++) {
                            int block = world.getBlockId(x, y, z);
                            if (block == Block.waterMoving.blockID || block == Block.waterStill.blockID) {
                                world.setBlockWithNotify(x, y, z, Block.lavaMoving.blockID);
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error applying blood water: " + e.getMessage());
        }
    }
    
    /**
     * Apply fog collapse effects
     */
    private void applyFogCollapse(EntityPlayerMP player) {
        try {
            // This is mainly a client-side effect, but we can add some server-side atmosphere
            WorldServer world = (WorldServer)player.worldObj;
            int playerX = (int)player.posX;
            int playerZ = (int)player.posZ;
            
            // Add some atmosphere blocks around player
            for (int i = 0; i < 5; i++) {
                int x = playerX + random.nextInt(10) - 5;
                int z = playerZ + random.nextInt(10) - 5;
                int y = world.getHeightValue(x, z);
                
                if (y > 0 && y < 128) {
                    // Place some cobwebs or other atmospheric blocks
                    if (random.nextBoolean()) {
                        world.setBlockWithNotify(x, y + 1, z, Block.web.blockID);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error applying fog collapse: " + e.getMessage());
        }
    }
    
    /**
     * Fill inventory with random items (infinite_inventory event)
     */
    private void fillInventoryRandom(EntityPlayerMP player) {
        if (player.inventory == null || player.inventory.mainInventory == null) {
            return;
        }

        int[] itemIds = {1, 2, 3, 4, 5, 12, 13, 17, 18, 20, 35, 45, 46, 49, 79, 80, 82, 87, 88, 89};
        ItemStack[] inv = player.inventory.mainInventory;

        for (int i = 0; i < inv.length; i++) {
            int randomId = itemIds[random.nextInt(itemIds.length)];
            int randomCount = 1 + random.nextInt(64);
            inv[i] = new ItemStack(randomId, randomCount, 0);
        }

        syncPlayerInventory(player);
    }
    
    /**
     * Spawn mystic signs near player (matching client logic)
     */
    private void spawnMysticSigns(EntityPlayerMP player) {
        try {
            WorldServer world = (WorldServer)player.worldObj;
            int playerX = (int)player.posX;
            int playerY = (int)player.posY;
            int playerZ = (int)player.posZ;

            // Спавн таблички "404" + бедроковый крест + блоки в воздухе + дерево без листвы (как в клиенте)
            // 1. Табличка "404"
            int signX = playerX + random.nextInt(20) - 10;
            int signZ = playerZ + random.nextInt(20) - 10;
            int signY = world.getHeightValue(signX, signZ);

            world.setBlockAndMetadataWithNotify(signX, signY, signZ, Block.signPost.blockID, random.nextInt(16));
            TileEntitySign sign = (TileEntitySign)world.getBlockTileEntity(signX, signY, signZ);
            if (sign != null) {
                sign.signText[0] = "404";
                sign.signText[1] = "";
                sign.signText[2] = "";
                sign.signText[3] = "";
            }

            // 2. Бедроковый крест рядом
            int crossX = playerX + random.nextInt(16) - 8;
            int crossZ = playerZ + random.nextInt(16) - 8;
            int crossY = world.getHeightValue(crossX, crossZ);
            world.setBlockWithNotify(crossX, crossY, crossZ, Block.bedrock.blockID);
            world.setBlockWithNotify(crossX, crossY + 1, crossZ, Block.bedrock.blockID);
            world.setBlockWithNotify(crossX - 1, crossY + 1, crossZ, Block.bedrock.blockID);
            world.setBlockWithNotify(crossX + 1, crossY + 1, crossZ, Block.bedrock.blockID);
            world.setBlockWithNotify(crossX, crossY + 2, crossZ, Block.bedrock.blockID);

            // 3. Блоки в воздухе
            for (int i = 0; i < 5; i++) {
                int bx = playerX + random.nextInt(30) - 15;
                int bz = playerZ + random.nextInt(30) - 15;
                int by = playerY + 10 + random.nextInt(20);
                world.setBlockWithNotify(bx, by, bz, random.nextInt(3) + 1); // Stone, dirt, etc.
            }

        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error spawning mystic signs: " + e.getMessage());
        }
    }
    
    /**
     * Spawn forgotten structures near player (matching client logic)
     */
    private void spawnForgottenStructures(EntityPlayerMP player) {
        try {
            WorldServer world = (WorldServer)player.worldObj;

            // Одинокая дверь (50%) или круг заборов (50%) за спиной игрока (15 блоков)
            double yaw = Math.toRadians(player.rotationYaw + 180);
            int behindX = (int)(player.posX + Math.sin(yaw) * 15);
            int behindZ = (int)(player.posZ - Math.cos(yaw) * 15);
            int baseY = world.getHeightValue(behindX, behindZ);

            if (random.nextBoolean()) {
                // Одинокая дверь
                world.setBlockWithNotify(behindX, baseY, behindZ, Block.doorWood.blockID);
                world.setBlockWithNotify(behindX, baseY + 1, behindZ, Block.doorWood.blockID);
            } else {
                // Круг заборов
                for (int i = 0; i < 8; i++) {
                    double angle = (i / 8.0) * Math.PI * 2;
                    int fx = behindX + (int)(Math.cos(angle) * 3);
                    int fz = behindZ + (int)(Math.sin(angle) * 3);
                    int fy = world.getHeightValue(fx, fz);
                    world.setBlockWithNotify(fx, fy, fz, Block.fence.blockID);
                }
            }

        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error spawning forgotten structure: " + e.getMessage());
        }
    }
    
    /**
     * Force player to turn 180 degrees
     */
    private void forceTurn180(EntityPlayerMP player) {
        try {
            float newYaw = player.rotationYaw + 180.0F;
            if (newYaw >= 360.0F) newYaw -= 360.0F;
            if (newYaw < 0.0F) newYaw += 360.0F;

            if (player.playerNetServerHandler != null) {
                player.playerNetServerHandler.teleportTo(player.posX, player.posY, player.posZ, newYaw, player.rotationPitch);
            }

        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error forcing turn: " + e.getMessage());
        }
    }
    
    /**
     * Spawn silhouette entity near player
     */
    private void spawnSilhouette(EntityPlayerMP player) {
        try {
            WorldServer world = (WorldServer)player.worldObj;

            // Spawn a zombie with no AI behavior as a silhouette
            double yaw = Math.toRadians(player.rotationYaw + 180);
            double spawnX = player.posX + Math.sin(yaw) * 10;
            double spawnZ = player.posZ - Math.cos(yaw) * 10;
            double spawnY = player.posY;

            EntityZombie silhouette = new EntityZombie(world);
            silhouette.setLocationAndAngles(spawnX, spawnY, spawnZ, player.rotationYaw + 180, 0.0F);
            world.entityJoinedWorld(silhouette);

        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error spawning silhouette: " + e.getMessage());
        }
    }
    
    /**
     * Spawn mirror player (zombie facing player) near player
     */
    private void spawnMirrorPlayer(EntityPlayerMP player) {
        try {
            WorldServer world = (WorldServer)player.worldObj;

            double yaw = Math.toRadians(player.rotationYaw + 180);
            double spawnX = player.posX + Math.sin(yaw) * 5;
            double spawnZ = player.posZ - Math.cos(yaw) * 5;
            double spawnY = player.posY;

            EntityZombie mirror = new EntityZombie(world);
            mirror.setLocationAndAngles(spawnX, spawnY, spawnZ, player.rotationYaw, player.rotationPitch);
            world.entityJoinedWorld(mirror);

        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error spawning mirror player: " + e.getMessage());
        }
    }

    /**
     * Set current stage (used by NetServerHandler)
     */
    public void setStage(int stage) {
        this.currentStage = stage;
    }

    /**
     * Process client mystic commands to maintain compatibility with single-player logic
     */
    public void processClientCommand(EntityPlayerMP player, String command) {
        
        // Handle mystic commands that need server authority
        if (command.startsWith("/mst")) {
            // Show mystic status
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a76=== Mystic Status ==="));
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7eCurrent stage: " + currentStage + "/4"));
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7eSpeed: x" + speedMultiplier));

            List<MysticEvent> currentEvents = getCurrentStageEvents();
            int availableCount = 0;
            for (MysticEvent event : currentEvents) {
                if (event.canTrigger()) {
                    availableCount++;
                }
            }

            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7eAvailable events: " + availableCount + "/" + currentEvents.size()));
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a77Next event in ~" + (nextEventDelayTicks / 20) + "s"));

        } else if (command.startsWith("/next")) {
            // Force next stage progression
            if (currentStage < 4) {
                currentStage++;
                scheduleNextEvent();
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aProgressed to Stage " + currentStage));
            } else {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cAlready at maximum stage!"));
            }

        } else if (command.startsWith("/mlvl")) {
            // Set specific mystic level/stage
            String[] parts = command.split(" ");
            if (parts.length < 2) {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cUsage: /mlvl <1-4>"));
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7eCurrent stage: " + currentStage));
                return;
            }

            try {
                int level = Integer.parseInt(parts[1]);
                if (level >= 1 && level <= 4) {
                    int oldStage = currentStage;
                    currentStage = level;
                    scheduleNextEvent();
                    player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aStage set to " + level));
                } else {
                    player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cLevel must be 1-4"));
                }
            } catch (NumberFormatException e) {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cInvalid format. Usage: /mlvl <1-4>"));
            }

        } else if (command.startsWith("/x")) {
            // Custom speed multiplier: /x2, /x3, /x5, /x10 etc.
            String multiplierStr = command.substring(2).trim();


            if (multiplierStr.isEmpty()) {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cUsage: /x<number> (e.g., /x2, /x3, /x5, /x10)"));
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7eCurrent speed: x" + speedMultiplier));
                return;
            }

            try {
                int multiplier = Integer.parseInt(multiplierStr);
                if (multiplier < 1 || multiplier > 100) {
                    player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cMultiplier must be between 1 and 100"));
                    return;
                }

                float oldMultiplier = speedMultiplier;
                speedMultiplier = (float)multiplier;
                scheduleNextEvent();

                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aEvent speed set to x" + multiplier));
            } catch (NumberFormatException e) {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cInvalid format. Use /x2, /x3, /x5, /x10 etc."));
            }
            
        } else if (command.startsWith("/horror_info")) {
            // Show current horror status
            String info = "\u00a76Horror Status:\n" +
                         "\u00a7eStage: " + currentStage + "/4\n" +
                         "\u00a7eSpeed: " + speedMultiplier + "x\n" +
                         "\u00a7ePlayers: " + server.configManager.playerEntities.size();
            player.playerNetServerHandler.sendPacket(new Packet3Chat(info));
            
        } else if (command.startsWith("/give ")) {
            // Handle standard give command
            handleGiveCommand(player, command);
        } else if (command.startsWith("/tp ")) {
            // Handle standard tp command
            handleTpCommand(player, command);
        } else if (command.equals("/event item px-all")) {
            // Give pickaxeAll to player
            givePickaxeAll(player);
        } else if (command.startsWith("/event item ")) {
            // Handle /event item command
            handleEventItemCommand(player, command);
        } else if (command.startsWith("/event ")) {
            // Trigger specific event by name
            String eventName = command.substring(7).trim();
            broadcastEventToAllPlayers(eventName, currentStage, 1.0f);
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aTriggered event: " + eventName));
        } else if (command.startsWith("/horror_reset")) {
            // Reset horror system (admin only)
            if (server.configManager.isOp(player.username)) {
                currentStage = 1;
                gameStartTick = 0;
                lastEventTick = 0;
                speedMultiplier = 2.0f;
                finalSequenceActive = false;
                // Reset all event trigger counts
                resetAllEvents();
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aHorror system reset!"));
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7eStage: 1, Speed: x2.0"));
            } else {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cYou don't have permission to use this command!"));
            }
        }
    }
    
    /**
     * Handle standard give command
     */
    private void handleGiveCommand(EntityPlayerMP player, String command) {
        String[] parts = command.split(" ");
        
        if (parts.length < 3) {
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cUsage: /give <player> <item_id> [amount]"));
            return;
        }
        
        String targetUsername = parts[1];
        String itemIdStr = parts[2];
        String amountStr = parts.length > 3 ? parts[3] : "1";
        
        try {
            int itemId = Integer.parseInt(itemIdStr);
            int amount = Integer.parseInt(amountStr);
            
            // Find target player
            EntityPlayerMP targetPlayer = server.configManager.getPlayerEntity(targetUsername);
            if (targetPlayer == null) {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cPlayer '" + targetUsername + "' not found!"));
                return;
            }
            
            // Give item to target player
            giveItemToPlayer(targetPlayer, itemId, amount);
            
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aGave " + amount + " of item " + itemId + " to " + targetUsername));
            targetPlayer.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aYou received " + amount + " of item " + itemId + " from " + player.username));
            
            
        } catch (NumberFormatException e) {
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cInvalid item ID or amount: " + itemIdStr + " " + amountStr));
        }
    }
    
    /**
     * Handle standard tp command
     */
    private void handleTpCommand(EntityPlayerMP player, String command) {
        String[] parts = command.split(" ");
        
        if (parts.length < 2) {
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cUsage: /tp <player> or /tp <x> <y> <z> or /tp <player1> <player2>"));
            return;
        }
        
        if (parts.length == 2) {
            // Teleport self to another player
            String targetUsername = parts[1];
            EntityPlayerMP targetPlayer = server.configManager.getPlayerEntity(targetUsername);
            
            if (targetPlayer == null) {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cPlayer '" + targetUsername + "' not found!"));
                return;
            }
            
            teleportPlayer(player, targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ);
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aTeleported to " + targetUsername));
            
        } else if (parts.length == 3) {
            // Teleport player1 to player2
            String player1Username = parts[1];
            String player2Username = parts[2];
            
            EntityPlayerMP player1 = server.configManager.getPlayerEntity(player1Username);
            EntityPlayerMP player2 = server.configManager.getPlayerEntity(player2Username);
            
            if (player1 == null) {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cPlayer '" + player1Username + "' not found!"));
                return;
            }
            
            if (player2 == null) {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cPlayer '" + player2Username + "' not found!"));
                return;
            }
            
            teleportPlayer(player1, player2.posX, player2.posY, player2.posZ);
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aTeleported " + player1Username + " to " + player2Username));
            player1.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aYou were teleported to " + player2Username));
            
        } else if (parts.length == 4) {
            // Teleport to coordinates: /tp <x> <y> <z>
            try {
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                
                teleportPlayer(player, x, y, z);
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aTeleported to " + (int)x + " " + (int)y + " " + (int)z));
            } catch (NumberFormatException e) {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cInvalid coordinates! Usage: /tp <x> <y> <z>"));
            }
        }
    }
    
    /**
     * Give item to player
     */
    private void giveItemToPlayer(EntityPlayerMP player, int itemId, int amount) {
        if (player.inventory == null) {
            return;
        }

        try {
            ItemStack stack = new ItemStack(itemId, amount, 0);
            boolean added = player.inventory.addItemStackToInventory(stack);

            if (added) {
            } else {
                // If inventory full, drop item
                player.dropPlayerItem(stack);
            }
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error giving item: " + e.getMessage());
        }
    }
    
    /**
     * Teleport player to coordinates
     */
    private void teleportPlayer(EntityPlayerMP player, double x, double y, double z) {
        if (player.playerNetServerHandler != null) {
            player.playerNetServerHandler.teleportTo(x, y, z, player.rotationYaw, player.rotationPitch);
            player.motionX = 0.0;
            player.motionY = 0.0;
            player.motionZ = 0.0;
            player.fallDistance = 0.0F;
        }
    }
    
    /**
     * Handle /event item command
     * Supports: /event item <id>, /event item <player> <id>, /event item px-all
     */
    private void handleEventItemCommand(EntityPlayerMP player, String command) {
        String[] parts = command.split(" ");
        // parts[0] = "/event", parts[1] = "item", parts[2] = <id> or <player>, parts[3] = <id> (optional)
        
        if (parts.length < 3) {
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cUsage: /event item <id> or /event item <player> <id> or /event item px-all"));
            return;
        }
        
        // Check if parts[2] is a number (item ID for self) or a player name
        if (parts.length == 3) {
            // /event item <id> - give item to self
            String itemIdStr = parts[2];
            try {
                int itemId = Integer.parseInt(itemIdStr);
                giveItemToPlayer(player, itemId, 1);
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aGave you item " + itemId));
            } catch (NumberFormatException e) {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cInvalid item ID: " + itemIdStr));
            }
        } else if (parts.length >= 4) {
            // /event item <player> <id> - give item to specified player
            String targetUsername = parts[2];
            String itemIdStr = parts[3];
            
            try {
                int itemId = Integer.parseInt(itemIdStr);
                
                EntityPlayerMP targetPlayer = server.configManager.getPlayerEntity(targetUsername);
                if (targetPlayer == null) {
                    player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cPlayer '" + targetUsername + "' not found!"));
                    return;
                }
                
                giveItemToPlayer(targetPlayer, itemId, 1);
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aGave item " + itemId + " to " + targetUsername));
                targetPlayer.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aYou received item " + itemId + " from " + player.username));
                
            } catch (NumberFormatException e) {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7cInvalid item ID: " + itemIdStr));
            }
        }
    }
    
    /**
     * Give pickaxeAll to player
     */
    private void givePickaxeAll(EntityPlayerMP player) {
        if (player.inventory == null) {
            return;
        }

        try {
            ItemStack pickaxe = new ItemStack(Item.pickaxeAll);
            boolean added = player.inventory.addItemStackToInventory(pickaxe);

            if (added) {
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aGiven: \u00a7fPickaxe (Breaks Everything)"));
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a77Can break bedrock and any block instantly"));
                syncPlayerInventory(player);
            } else {
                player.dropPlayerItem(pickaxe);
                player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00a7aDropped Pickaxe at your feet (inventory full)"));
            }

        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error giving pickaxeAll: " + e.getMessage());
        }
    }
    
    /**
     * Reset all event trigger counts
     */
    private void resetAllEvents() {
        resetEventList(stage1Events);
        resetEventList(stage2Events);
        resetEventList(stage3Events);
        resetEventList(stage4Events);
    }
    
    private void resetEventList(List<MysticEvent> events) {
        for (MysticEvent event : events) {
            event.triggered = false;
            event.triggerCount = 0;
        }
    }
    
    /**
     * Give specific item to player
     */
    private void giveRandomItem(EntityPlayerMP player, int specificItemId) {
        if (player.inventory == null) {
            return;
        }

        try {
            ItemStack stack = new ItemStack(specificItemId, 1, 0);
            boolean added = player.inventory.addItemStackToInventory(stack);

            if (added) {
            } else {
                // If inventory full, drop item
                player.dropPlayerItem(stack);
            }
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error giving item: " + e.getMessage());
        }
    }
    
    /**
     * Teleport player to bedrock tunnel - underground near player
     */
    private void teleportToBedrockTunnel(EntityPlayerMP player) {
        // Туннель под землёй рядом с игроком - на минусовых координатах по Y
        // Используем смещение от позиции игрока, чтобы не грузить далёкие чанки
        int startX = (int)player.posX + 20;
        int startY = 5; // Под землёй, под слоем бедрока (Y=0-4)
        int startZ = (int)player.posZ + 20;
        
        double teleportX = startX + 0.5;
        double teleportY = startY + 1.0;
        double teleportZ = startZ + 1.5;
        float yaw = 0.0F;
        float pitch = 0.0F;


        // Generate the tunnel structure first
        generateBedrockTunnel((WorldServer)player.worldObj, startX, startY, startZ);

        if (player.playerNetServerHandler != null) {
            // Teleport player
            player.playerNetServerHandler.teleportTo(teleportX, teleportY, teleportZ, yaw, pitch);
            
            // Reset player motion
            player.motionX = 0.0;
            player.motionY = 0.0;
            player.motionZ = 0.0;
            player.fallDistance = 0.0F;
            player.onGround = true;
            
            // Force inventory sync
            syncPlayerInventory(player);
            
            // Send chat message
            player.playerNetServerHandler.sendPacket(new Packet3Chat("\u00A74your world is fucked"));
        }
    }
    
    /**
     * Sync player inventory efficiently
     */
    private void syncPlayerInventory(EntityPlayerMP player) {
        if (player.inventory == null) return;
        
        // Send inventory update packets for all slots
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack item = player.inventory.mainInventory[i];
            player.playerNetServerHandler.sendPacket(new Packet5PlayerInventory(player.entityId, i, item));
        }
        // Sync armor slots
        for (int i = 0; i < player.inventory.armorInventory.length; i++) {
            ItemStack item = player.inventory.armorInventory[i];
            player.playerNetServerHandler.sendPacket(new Packet5PlayerInventory(player.entityId, 100 + i, item));
        }
    }
    

    /**
     * Generate bedrock tunnel structure underground at specified coordinates
     */
    private void generateBedrockTunnel(WorldServer world, int centerX, int centerY, int centerZ) {

        int startX = centerX;
        int startY = centerY;
        int startZ = centerZ;

        // Бедроковая оболочка вокруг туннеля (чтобы не было утечек в пещеры)
        for (int x = -3; x <= 3; x++) {
            for (int z = -2; z <= 52; z++) {
                for (int y = -2; y <= 6; y++) {
                    int bx = startX + x;
                    int by = startY + y;
                    int bz = startZ + z;
                    if (by >= 0 && by < 128) {
                        world.setBlockWithNotify(bx, by, bz, Block.bedrock.blockID);
                    }
                }
            }
        }

        // Туннель 3x4 внутри (x=-1..1, y=0..3) длиной 50 блоков
        for (int length = 0; length < 50; length++) {
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 3; y++) {
                    int blockX = startX + x;
                    int blockY = startY + y;
                    int blockZ = startZ + length;

                    if (blockY >= 0 && blockY < 128) {
                        // Внутри туннеля - воздух
                        world.setBlockWithNotify(blockX, blockY, blockZ, 0);
                    }
                }
            }

            // Факелы на стенах каждые 10 блоков
            if (length % 10 == 0 && length > 0) {
                if (startY + 1 < 128) {
                    world.setBlockWithNotify(startX - 1, startY + 1, startZ + length, Block.torchWood.blockID);
                    world.setBlockWithNotify(startX + 1, startY + 1, startZ + length, Block.torchWood.blockID);
                }
            }
        }

        // Табличка в конце туннеля на стене
        int signX = startX;
        int signY = startY + 1;
        int signZ = startZ + 48;

        if (signY >= 0 && signY < 128) {
            world.setBlockAndMetadataWithNotify(signX, signY, signZ, Block.signWall.blockID, 2);
            TileEntitySign sign = (TileEntitySign)world.getBlockTileEntity(signX, signY, signZ);
            if (sign != null) {
                sign.signText[0] = "";
                sign.signText[1] = "You shouldn't";
                sign.signText[2] = "have done that";
                sign.signText[3] = "";
            }
        }

    }

    /**
     * Give random item - EXACT COPY from client MysticManager
     */
    private void giveRandomItem(EntityPlayerMP player) {
        if (player.inventory == null) {
            return;
        }

        // Список предметов для выдачи (включая редкие)
        int[] commonItems = {1, 2, 3, 4, 5, 12, 17, 18, 20, 21, 26, 45, 49, 50, 54, 58, 59, 67, 73, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 91, 102};
        int[] rareItems = {264, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288};
        int[] specialItems = {49, 51, 52, 69, 71, 90, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 103, 110, 322, 323, 324, 325, 326, 327, 328, 329, 330, 331, 332, 333, 334, 335, 336, 337, 338, 339, 340, 341, 342, 343, 344, 345, 346};

        // Выбор редкости
        int rarity = random.nextInt(100);
        int[] selectedItems;
        int maxAmount;

        if (rarity < 70) {
            // 70% - обычные предметы
            selectedItems = commonItems;
            maxAmount = 64;
        } else if (rarity < 95) {
            // 25% - редкие предметы
            selectedItems = rareItems;
            maxAmount = 1;
        } else {
            // 5% - особые предметы
            selectedItems = specialItems;
            maxAmount = 1;
        }

        // Выбор предмета
        int itemId = selectedItems[random.nextInt(selectedItems.length)];
        int amount = Math.min(random.nextInt(maxAmount) + 1, maxAmount);

        // Попытка добавить предмет в инвентарь
        try {
            ItemStack stack = new ItemStack(itemId, amount, 0);
            boolean added = player.inventory.addItemStackToInventory(stack);

            if (added) {
                return; // Предмет добавлен, выходим
            }
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error giving item: " + e.getMessage());
        }

        // Если не получилось добавить в инвентарь, даем алмаз как запасной вариант
        try {
            ItemStack fallback = new ItemStack(Item.diamond.shiftedIndex, 1, 0);
            player.inventory.addItemStackToInventory(fallback);
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Error giving fallback diamond: " + e.getMessage());
        }
    }

    /**
     * Save mystic state to file
     */
    private void saveState() {
        try {
            File saveDir = new File("saves");
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            File saveFile = new File(saveDir, SAVE_FILE);
            DataOutputStream out = new DataOutputStream(new FileOutputStream(saveFile));

            // Write basic state
            out.writeInt(currentStage);
            out.writeLong(gameStartTick);
            out.writeLong(lastEventTick);
            out.writeFloat(speedMultiplier);
            out.writeBoolean(finalSequenceActive);
            out.writeLong(finalSequenceStartTick);

            // Write event trigger states
            writeEventStates(out, stage1Events);
            writeEventStates(out, stage2Events);
            writeEventStates(out, stage3Events);
            writeEventStates(out, stage4Events);

            out.close();
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Failed to save state: " + e.getMessage());
        }
    }

    private void writeEventStates(DataOutputStream out, List<MysticEvent> events) throws IOException {
        out.writeInt(events.size());
        for (MysticEvent event : events) {
            out.writeUTF(event.name);
            out.writeBoolean(event.triggered);
            out.writeInt(event.triggerCount);
        }
    }

    /**
     * Load mystic state from file
     */
    private void loadState() {
        try {
            File saveFile = new File("saves", SAVE_FILE);
            if (!saveFile.exists()) {
                return;
            }

            DataInputStream in = new DataInputStream(new FileInputStream(saveFile));

            // Read basic state
            currentStage = in.readInt();
            gameStartTick = in.readLong();
            lastEventTick = in.readLong();
            speedMultiplier = in.readFloat();
            finalSequenceActive = in.readBoolean();
            finalSequenceStartTick = in.readLong();

            // Read event trigger states
            readEventStates(in, stage1Events);
            readEventStates(in, stage2Events);
            readEventStates(in, stage3Events);
            readEventStates(in, stage4Events);

            in.close();
        } catch (Exception e) {
            System.err.println("[ServerMysticManager] Failed to load state: " + e.getMessage());
        }
    }

    private void readEventStates(DataInputStream in, List<MysticEvent> events) throws IOException {
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            String name = in.readUTF();
            boolean triggered = in.readBoolean();
            int triggerCount = in.readInt();

            // Find corresponding event and update its state
            for (MysticEvent event : events) {
                if (event.name.equals(name)) {
                    event.triggered = triggered;
                    event.triggerCount = triggerCount;
                    break;
                }
            }
        }
    }

    /**
     * Cleanup method - call when server shuts down
     */
    public void shutdown() {
        if (initialized) {
            saveState();
        }
    }

    /**
     * Mystic Event class
     */
    public static class MysticEvent {
        public String name;
        public int stage;
        public boolean repeatable;
        public boolean triggered;
        public int triggerCount;
        public int maxRepeats;

        public MysticEvent(String name, int stage) {
            this(name, stage, false);
        }

        public MysticEvent(String name, int stage, boolean repeatable) {
            this.name = name;
            this.stage = stage;
            this.repeatable = repeatable;
            this.triggered = false;
            this.triggerCount = 0;
            this.maxRepeats = repeatable ? 3 : 1; // Повторяющиеся события - 3 раза, неповторяющиеся - 1 раз
        }

        public boolean canTrigger() {
            if (!repeatable) {
                return triggerCount == 0;
            }
            return triggerCount < maxRepeats;
        }

        public void trigger() {
            triggered = true;
            triggerCount++;
        }
    }
}
