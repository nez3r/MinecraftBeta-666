package net.minecraft.src;

import org.lwjgl.input.Keyboard;
import java.util.ArrayList;
import java.util.List;

public class GuiChat extends GuiScreen {
	protected String message = "";
	private int updateCounter = 0;
	private static final String field_20082_i = ChatAllowedCharacters.allowedCharacters;
	
	// Chat history for arrow key navigation
	private static final List<String> chatHistory = new ArrayList<String>();
	private int historyIndex = -1;
	private String currentMessage = "";

	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		// Reset history navigation when opening chat
		historyIndex = -1;
		currentMessage = "";
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		// Reset history navigation when closing chat
		historyIndex = -1;
		currentMessage = "";
	}

	public void updateScreen() {
		++this.updateCounter;
	}

	protected void keyTyped(char var1, int var2) {
		if(var2 == 1) {
			this.mc.displayGuiScreen((GuiScreen)null);
		} else if(var2 == 28) {
			String var3 = this.message.trim();
			if(var3.length() > 0) {
				String var4 = this.message.trim();

				// Check if multiplayer and handle mystic commands
				if(this.mc.theWorld != null && this.mc.theWorld.multiplayerWorld) {
					// In multiplayer - send commands to server
					if(var4.equals("/next") || var4.equals("/mst") || var4.startsWith("/mlvl") ||
					   var4.startsWith("/event") || var4.startsWith("/x") || var4.equals("/horror_reset")) {
						this.sendMysticCommandToServer(var4);
						// НЕ очищаем сообщение и НЕ закрываем чат для команд
						return;
					}
				} else {
					// In singleplayer - handle locally
					if(var4.startsWith("/x") || var4.equals("/next") || var4.equals("/mst") ||
					   var4.startsWith("/mlvl") || var4.equals("/mreset") ||
					   var4.equals("/wav") || var4.equals("/ogg") || var4.startsWith("/event")) {
						MysticManager.getInstance().handleCommand(var4);
						// НЕ очищаем сообщение и НЕ закрываем чат для команд
						return;
					}
				}

				if(!this.mc.lineIsCommand(var4)) {
					this.mc.thePlayer.sendChatMessage(var4);

					// Add to chat history (avoid duplicates)
					addToChatHistory(var4);

					// Shadow chat - capture message
					MysticManager manager = MysticManager.getInstance();
					if (manager.isShadowChatActive) {
						manager.shadowChatMessage = var4;
						manager.shadowChatTime = System.currentTimeMillis() + 5000;
					}
				} else {
					// Also add commands to history
					addToChatHistory(var4);
				}

				this.message = "";
				// Reset history navigation after sending
				historyIndex = -1;
				currentMessage = "";
			}

			this.mc.displayGuiScreen((GuiScreen)null);
		} else {
			// Handle arrow keys for chat history navigation
			if(var2 == 200) { // Up arrow
				navigateHistory(-1);
			} else if(var2 == 208) { // Down arrow
				navigateHistory(1);
			} else if(var2 == 14 && this.message.length() > 0) { // Backspace
				this.message = this.message.substring(0, this.message.length() - 1);
				// Reset history navigation when typing
				if(historyIndex != -1) {
					historyIndex = -1;
					currentMessage = "";
				}
			} else if(field_20082_i.indexOf(var1) >= 0 && this.message.length() < 100) {
				this.message = this.message + var1;
				// Reset history navigation when typing
				if(historyIndex != -1) {
					historyIndex = -1;
					currentMessage = "";
				}
			}

		}
	}

	private void sendMysticCommandToServer(String command) {
		// Parse command and arguments
		String[] parts = command.split(" ");
		String cmd = parts[0].substring(1); // Remove '/'
		String[] args = new String[parts.length - 1];
		System.arraycopy(parts, 1, args, 0, args.length);

		// Create and send packet
		Packet251MysticCommand packet = new Packet251MysticCommand(cmd, args, this.mc.thePlayer.username);
		((EntityClientPlayerMP)this.mc.thePlayer).sendQueue.addToSendQueue(packet);

		System.out.println("[GuiChat] Sent mystic command to server: " + cmd);
	}

	public void drawScreen(int var1, int var2, float var3) {
		this.drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
		this.drawString(this.fontRenderer, "> " + this.message + (this.updateCounter / 6 % 2 == 0 ? "_" : ""), 4, this.height - 12, 14737632);
		super.drawScreen(var1, var2, var3);
	}

	protected void mouseClicked(int var1, int var2, int var3) {
		if(var3 == 0) {
			if(this.mc.ingameGUI.field_933_a != null) {
				if(this.message.length() > 0 && !this.message.endsWith(" ")) {
					this.message = this.message + " ";
				}

				this.message = this.message + this.mc.ingameGUI.field_933_a;
				byte var4 = 100;
				if(this.message.length() > var4) {
					this.message = this.message.substring(0, var4);
				}
			} else {
				super.mouseClicked(var1, var2, var3);
			}
		}

	}
	
	/**
	 * Add message to chat history
	 */
	private void addToChatHistory(String message) {
		if (message == null || message.trim().isEmpty()) {
			return;
		}
		
		// Remove from history if it already exists (to avoid duplicates)
		chatHistory.remove(message);
		
		// Add to beginning of history
		chatHistory.add(0, message);
		
		// Limit history size to 50 messages
		while (chatHistory.size() > 50) {
			chatHistory.remove(chatHistory.size() - 1);
		}
	}
	
	/**
	 * Navigate through chat history with arrow keys
	 */
	private void navigateHistory(int direction) {
		// Save current message if not already in history
		if (historyIndex == -1 && !message.trim().isEmpty()) {
			currentMessage = message;
		}
		
		if (direction == -1) { // Up arrow - go to older messages
			if (historyIndex < chatHistory.size() - 1) {
				historyIndex++;
				message = chatHistory.get(historyIndex);
			}
		} else { // Down arrow - go to newer messages
			if (historyIndex > -1) {
				historyIndex--;
				if (historyIndex == -1) {
					message = currentMessage;
				} else {
					message = chatHistory.get(historyIndex);
				}
			}
		}
	}
}
