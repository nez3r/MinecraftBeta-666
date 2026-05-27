package net.minecraft.src;

import java.util.List;

/**
 * Server-side handler for horror event synchronization
 *
 * When a client sends a horror event packet, the server receives it and broadcasts
 * it to all other connected players (except the sender).
 *
 * This creates a synchronized horror experience across all players in multiplayer.
 */
public class HorrorSyncServerHandler {

    /**
     * Process incoming horror sync packet from a client and broadcast to others
     *
     * @param packet The horror sync packet from client
     * @param sender The player who sent the packet
     * @param server The server instance (for accessing player list)
     */
    public static void handleClientHorrorSync(Packet250HorrorSync packet, EntityPlayerMP sender, Object server) {
        if (packet == null || sender == null) {
            return;
        }

        System.out.println("[HorrorSync Server] Received event '" + packet.eventName +
                          "' from " + sender.username + " (stage " + packet.stage + ")");

        // Handle server-side effects for certain events BEFORE broadcasting
        handleServerSideEffects(packet, sender, server);

        // Get all players from server
        List playerList = getPlayerList(server);
        if (playerList == null || playerList.isEmpty()) {
            return;
        }

        // Broadcast to all players
        int broadcastCount = 0;
        for (Object playerObj : playerList) {
            if (!(playerObj instanceof EntityPlayerMP)) {
                continue;
            }

            EntityPlayerMP player = (EntityPlayerMP) playerObj;

            // Skip sender (they already triggered the event locally)
            if (player.username.equals(sender.username)) {
                continue;
            }

            // Check if this event is targeted at specific player
            if (!packet.targetPlayer.isEmpty() && !player.username.equals(packet.targetPlayer)) {
                continue; // Not for this player
            }

            // Send packet to this player
            try {
                if (player.playerNetServerHandler != null) {
                    player.playerNetServerHandler.sendPacket(packet);
                    broadcastCount++;
                }
            } catch (Exception e) {
                System.err.println("[HorrorSync Server] Failed to send to " + player.username + ": " + e.getMessage());
            }
        }

        System.out.println("[HorrorSync Server] Broadcasted '" + packet.eventName +
                          "' to " + broadcastCount + " players");
    }

    /**
     * Broadcast a server-initiated horror event to all players
     *
     * @param eventName Event identifier
     * @param stage Stage number (1-4)
     * @param intensity Intensity multiplier
     * @param server The server instance
     */
    public static void broadcastServerEvent(String eventName, int stage, float intensity, Object server) {
        List playerList = getPlayerList(server);
        if (playerList == null || playerList.isEmpty()) {
            return;
        }

        Packet250HorrorSync packet = new Packet250HorrorSync(eventName, stage, intensity);
        packet.senderName = "SERVER";

        System.out.println("[HorrorSync Server] Broadcasting server event: " + eventName);

        int broadcastCount = 0;
        for (Object playerObj : playerList) {
            if (!(playerObj instanceof EntityPlayerMP)) {
                continue;
            }

            EntityPlayerMP player = (EntityPlayerMP) playerObj;

            try {
                if (player.playerNetServerHandler != null) {
                    player.playerNetServerHandler.sendPacket(packet);
                    broadcastCount++;
                }
            } catch (Exception e) {
                System.err.println("[HorrorSync Server] Failed to send to " + player.username + ": " + e.getMessage());
            }
        }

        System.out.println("[HorrorSync Server] Broadcasted to " + broadcastCount + " players");
    }

    /**
     * Broadcast horror event to specific player only
     *
     * @param eventName Event identifier
     * @param stage Stage number
     * @param intensity Intensity multiplier
     * @param targetUsername Target player username
     * @param server The server instance
     */
    public static void broadcastToPlayer(String eventName, int stage, float intensity,
                                         String targetUsername, Object server) {
        List playerList = getPlayerList(server);
        if (playerList == null || playerList.isEmpty()) {
            return;
        }

        Packet250HorrorSync packet = new Packet250HorrorSync(eventName, stage, intensity, targetUsername);
        packet.senderName = "SERVER";

        for (Object playerObj : playerList) {
            if (!(playerObj instanceof EntityPlayerMP)) {
                continue;
            }

            EntityPlayerMP player = (EntityPlayerMP) playerObj;

            if (player.username.equals(targetUsername)) {
                try {
                    if (player.playerNetServerHandler != null) {
                        player.playerNetServerHandler.sendPacket(packet);
                        System.out.println("[HorrorSync Server] Sent '" + eventName + "' to " + targetUsername);
                    }
                } catch (Exception e) {
                    System.err.println("[HorrorSync Server] Failed to send to " + targetUsername + ": " + e.getMessage());
                }
                break;
            }
        }
    }

    /**
     * Get player list from server instance
     * Uses reflection to support different server implementations
     */
    private static List getPlayerList(Object server) {
        if (server == null) {
            return null;
        }

        try {
            // Try to get player list via reflection
            // This works for both vanilla and modded servers
            java.lang.reflect.Field[] fields = server.getClass().getDeclaredFields();

            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(server);

                // Look for List containing EntityPlayerMP
                if (value instanceof List) {
                    List list = (List) value;
                    if (!list.isEmpty() && list.get(0) instanceof EntityPlayerMP) {
                        return list;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[HorrorSync Server] Failed to get player list: " + e.getMessage());
        }

        return null;
    }

    /**
     * Handle server-side effects for events that need server authority
     * This prevents anticheat kicks and ensures proper synchronization
     */
    private static void handleServerSideEffects(Packet250HorrorSync packet, EntityPlayerMP sender, Object server) {
        String eventName = packet.eventName;

        // Bedrock tunnel - teleport player on server to prevent anticheat kick
        if (eventName.equals("bedrock_tunnel")) {
            handleBedrockTunnelTeleport(sender);
        }
        // Random item - give item on server to sync inventory
        else if (eventName.equals("random_item")) {
            handleRandomItem(sender);
        }
        // Inventory swap - swap on server to sync inventory
        else if (eventName.equals("inventory_swap")) {
            handleInventorySwap(sender);
        }
        // Infinite inventory - handle on server
        else if (eventName.equals("infinite_inventory")) {
            handleInfiniteInventory(sender);
        }
    }

    /**
     * Teleport player for bedrock tunnel event
     * Tunnel is generated underground near the player by ServerMysticManager
     */
    private static void handleBedrockTunnelTeleport(EntityPlayerMP player) {
        // ServerMysticManager handles the actual tunnel generation and teleport
        // This is now delegated to ServerMysticManager.teleportToBedrockTunnel()
        ServerMysticManager manager = ServerMysticManager.getInstance(player.mcServer);
        if (manager != null) {
            // The manager will generate the tunnel and teleport the player
            System.out.println("[HorrorSync Server] Delegating bedrock tunnel to ServerMysticManager for " + player.username);
        }
    }

    /**
     * Give random item to player (server-side)
     */
    private static void handleRandomItem(EntityPlayerMP player) {
        if (player.inventory == null) {
            return;
        }

        // Random item IDs (safe items that won't crash)
        int[] itemIds = {1, 2, 3, 4, 5, 12, 13, 17, 18, 20, 35, 45, 46, 49, 79, 80, 82, 87, 88, 89};
        int randomId = itemIds[player.worldObj.rand.nextInt(itemIds.length)];
        int randomCount = 1 + player.worldObj.rand.nextInt(16);

        ItemStack randomItem = new ItemStack(randomId, randomCount, 0);
        player.inventory.addItemStackToInventory(randomItem);

        System.out.println("[HorrorSync Server] Gave " + player.username + " random item: " + randomId + " x" + randomCount);
    }

    /**
     * Swap two random inventory slots (server-side)
     */
    private static void handleInventorySwap(EntityPlayerMP player) {
        if (player.inventory == null || player.inventory.mainInventory == null) {
            return;
        }

        ItemStack[] inv = player.inventory.mainInventory;
        int slot1 = player.worldObj.rand.nextInt(inv.length);
        int slot2 = player.worldObj.rand.nextInt(inv.length);

        // Swap
        ItemStack temp = inv[slot1];
        inv[slot1] = inv[slot2];
        inv[slot2] = temp;

        System.out.println("[HorrorSync Server] Swapped inventory slots " + slot1 + " and " + slot2 + " for " + player.username);
    }

    /**
     * Fill inventory with random items (server-side)
     */
    private static void handleInfiniteInventory(EntityPlayerMP player) {
        if (player.inventory == null || player.inventory.mainInventory == null) {
            return;
        }

        int[] itemIds = {1, 2, 3, 4, 5, 12, 13, 17, 18, 20, 35, 45, 46, 49, 79, 80, 82, 87, 88, 89};
        ItemStack[] inv = player.inventory.mainInventory;

        for (int i = 0; i < inv.length; i++) {
            int randomId = itemIds[player.worldObj.rand.nextInt(itemIds.length)];
            int randomCount = 1 + player.worldObj.rand.nextInt(64);
            inv[i] = new ItemStack(randomId, randomCount, 0);
        }

        System.out.println("[HorrorSync Server] Filled inventory for " + player.username);
    }

    /**
     * Check if horror sync is enabled on server
     * Can be used for server configuration
     */
    public static boolean isHorrorSyncEnabled() {
        // TODO: Add server configuration option
        return true;
    }
}
