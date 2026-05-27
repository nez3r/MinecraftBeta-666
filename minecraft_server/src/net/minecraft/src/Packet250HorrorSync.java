package net.minecraft.src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Packet for synchronizing horror events across multiplayer
 *
 * When a player triggers a mystic event, this packet is sent to the server,
 * which then broadcasts it to all other players to create synchronized horror experience.
 */
public class Packet250HorrorSync extends Packet {

    // Event identifier (e.g., "screamer", "tunnel_vision", "gdi_glitch")
    public String eventName;

    // Stage of the event (1-4)
    public int stage;

    // Intensity multiplier (0.0 - 2.0, default 1.0)
    public float intensity;

    // Target player username (empty string = broadcast to all)
    public String targetPlayer;

    // Additional data (for complex events)
    public String extraData;

    // Sender username (who triggered the event)
    public String senderName;

    public Packet250HorrorSync() {
        this.eventName = "";
        this.stage = 1;
        this.intensity = 1.0f;
        this.targetPlayer = "";
        this.extraData = "";
        this.senderName = "";
    }

    public Packet250HorrorSync(String eventName, int stage) {
        this.eventName = eventName;
        this.stage = stage;
        this.intensity = 1.0f;
        this.targetPlayer = "";
        this.extraData = "";
        this.senderName = "";
    }

    public Packet250HorrorSync(String eventName, int stage, float intensity) {
        this.eventName = eventName;
        this.stage = stage;
        this.intensity = intensity;
        this.targetPlayer = "";
        this.extraData = "";
        this.senderName = "";
    }

    public Packet250HorrorSync(String eventName, int stage, float intensity, String targetPlayer) {
        this.eventName = eventName;
        this.stage = stage;
        this.intensity = intensity;
        this.targetPlayer = targetPlayer;
        this.extraData = "";
        this.senderName = "";
    }

    public void readPacketData(DataInputStream input) throws IOException {
        this.eventName = readString(input, 64);
        this.stage = input.readInt();
        this.intensity = input.readFloat();
        this.targetPlayer = readString(input, 32);
        this.extraData = readString(input, 256);
        this.senderName = readString(input, 32);
    }

    public void writePacketData(DataOutputStream output) throws IOException {
        writeString(this.eventName, output);
        output.writeInt(this.stage);
        output.writeFloat(this.intensity);
        writeString(this.targetPlayer, output);
        writeString(this.extraData, output);
        writeString(this.senderName, output);
    }

    public void processPacket(NetHandler handler) {
        handler.handleHorrorSync(this);
    }

    public int getPacketSize() {
        return 2 + this.eventName.length() + 4 + 4 + 2 + this.targetPlayer.length() +
               2 + this.extraData.length() + 2 + this.senderName.length();
    }
}
