package net.minecraft.src;

import java.io.*;

/**
 * Packet for sending mystic commands from client to server
 * Commands: /event, /next, /mst, /mlvl
 */
public class Packet251MysticCommand extends Packet {
    public String command;
    public String[] args;
    public String senderName;

    public Packet251MysticCommand() {
    }

    public Packet251MysticCommand(String command, String[] args, String senderName) {
        this.command = command;
        this.args = args;
        this.senderName = senderName;
    }

    public void readPacketData(DataInputStream input) throws IOException {
        this.command = readString(input, 32);
        this.senderName = readString(input, 16);

        int argCount = input.readByte();
        this.args = new String[argCount];
        for (int i = 0; i < argCount; i++) {
            this.args[i] = readString(input, 64);
        }
    }

    public void writePacketData(DataOutputStream output) throws IOException {
        writeString(this.command, output);
        writeString(this.senderName, output);

        output.writeByte(this.args.length);
        for (int i = 0; i < this.args.length; i++) {
            writeString(this.args[i], output);
        }
    }

    public void processPacket(NetHandler handler) {
        handler.handleMysticCommand(this);
    }

    public int getPacketSize() {
        int size = 2 + this.command.length() + 2 + this.senderName.length() + 1;
        for (int i = 0; i < this.args.length; i++) {
            size += 2 + this.args[i].length();
        }
        return size;
    }
}
