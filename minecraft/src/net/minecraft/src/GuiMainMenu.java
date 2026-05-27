package net.minecraft.src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import org.lwjgl.opengl.GL11;

public class GuiMainMenu extends GuiScreen {
	private static final Random rand = new Random();
	private float updateCounter = 0.0F;
	private String splashText = "missingno";
	private GuiButton multiplayerButton;

	public GuiMainMenu() {
		try {
			ArrayList var1 = new ArrayList();
			BufferedReader var2 = new BufferedReader(new InputStreamReader(GuiMainMenu.class.getResourceAsStream("/title/splashes.txt"), Charset.forName("UTF-8")));
			String var3 = "";

			while(true) {
				var3 = var2.readLine();
				if(var3 == null) {
					this.splashText = (String)var1.get(rand.nextInt(var1.size()));
					break;
				}

				var3 = var3.trim();
				if(var3.length() > 0) {
					var1.add(var3);
				}
			}
		} catch (Exception var4) {
		}

	}

	public void updateScreen() {
		++this.updateCounter;

		// Тряска окна на главном меню
		MysticManager.getInstance().updateWindowShake();
	}

	protected void keyTyped(char var1, int var2) {
	}

	public void initGui() {
		Calendar var1 = Calendar.getInstance();
		var1.setTime(new Date());
		if(var1.get(2) + 1 == 11 && var1.get(5) == 9) {
			this.splashText = "Happy birthday, ez!";
		} else if(var1.get(2) + 1 == 6 && var1.get(5) == 1) {
			this.splashText = "Happy birthday, Notch!";
		} else if(var1.get(2) + 1 == 12 && var1.get(5) == 24) {
			this.splashText = "Merry X-mas!";
		} else if(var1.get(2) + 1 == 1 && var1.get(5) == 1) {
			this.splashText = "Happy new year!";
		}

		StringTranslate var2 = StringTranslate.getInstance();
		int var4 = this.height / 4 + 48;

		// Проверка на End Mode
		MysticManager manager = MysticManager.getInstance();
		if(manager.isEndMode) {
			this.controlList.add(new GuiButton(1, this.width / 2 - 100, var4, "E N D"));
			this.controlList.add(this.multiplayerButton = new GuiButton(2, this.width / 2 - 100, var4 + 24, "E N D"));
			this.controlList.add(new GuiButton(3, this.width / 2 - 100, var4 + 48, "E N D"));
			if(this.mc.hideQuitButton) {
				this.controlList.add(new GuiButton(0, this.width / 2 - 100, var4 + 72, "E N D"));
			} else {
				this.controlList.add(new GuiButton(0, this.width / 2 - 100, var4 + 72 + 12, 98, 20, "E N D"));
				this.controlList.add(new GuiButton(4, this.width / 2 + 2, var4 + 72 + 12, 98, 20, "E N D"));
			}
		} else {
			// Глитч текста на кнопках
			this.controlList.add(new GuiButton(1, this.width / 2 - 100, var4, manager.glitchButtonText(var2.translateKey("menu.singleplayer"))));
			this.controlList.add(this.multiplayerButton = new GuiButton(2, this.width / 2 - 100, var4 + 24, manager.glitchButtonText(var2.translateKey("menu.multiplayer"))));
			this.controlList.add(new GuiButton(3, this.width / 2 - 100, var4 + 48, manager.glitchButtonText(var2.translateKey("menu.mods"))));
			if(this.mc.hideQuitButton) {
				this.controlList.add(new GuiButton(0, this.width / 2 - 100, var4 + 72, manager.glitchButtonText(var2.translateKey("menu.options"))));
			} else {
				this.controlList.add(new GuiButton(0, this.width / 2 - 100, var4 + 72 + 12, 98, 20, manager.glitchButtonText(var2.translateKey("menu.options"))));
				this.controlList.add(new GuiButton(4, this.width / 2 + 2, var4 + 72 + 12, 98, 20, manager.glitchButtonText(var2.translateKey("menu.quit"))));
			}
		}

		if(this.mc.session == null) {
			this.multiplayerButton.enabled = false;
		}

		// Тестовая кнопка для проверки DLL (в правом нижнем углу)
		this.controlList.add(new GuiButton(99, this.width - 110, this.height - 30, 100, 20, "???"));

	}

	protected void actionPerformed(GuiButton var1) {
		if(var1.id == 0) {
			this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
		}

		if(var1.id == 1) {
			this.mc.displayGuiScreen(new GuiSelectWorld(this));
		}

		if(var1.id == 2) {
			this.mc.displayGuiScreen(new GuiMultiplayer(this));
		}

		if(var1.id == 3) {
			this.mc.displayGuiScreen(new GuiTexturePacks(this));
		}

		if(var1.id == 4) {
			this.mc.shutdown();
		}

		// Тестовая кнопка DLL
		if(var1.id == 99) {
			if(HorrorSystemDLL.isAvailable()) {
				HorrorSystemDLL.INSTANCE.ShowWarning();
			}
		}

	}

	public void drawScreen(int var1, int var2, float var3) {
		this.drawDefaultBackground();
		Tessellator var4 = Tessellator.instance;
		short var5 = 274;
		int var6 = this.width / 2 - var5 / 2;
		byte var7 = 30;

		MysticManager manager = MysticManager.getInstance();

		// Глитчи на главном экране
		if(manager.isEndMode) {
			// В End Mode черный фон
			this.drawRect(0, 0, this.width, this.height, 0xFF000000);
		} else {
			// Постоянные интенсивные глитчи
			Random glitchRand = new Random();
			// Рисуем 5-10 случайных глитч-прямоугольников каждый кадр
			int glitchCount = 5 + glitchRand.nextInt(6);
			for (int i = 0; i < glitchCount; i++) {
				int glitchX = glitchRand.nextInt(this.width);
				int glitchY = glitchRand.nextInt(this.height);
				int glitchW = glitchRand.nextInt(100) + 20;
				int glitchH = glitchRand.nextInt(100) + 20;
				int glitchColor = glitchRand.nextInt(0xFFFFFF);
				int alpha = 50 + glitchRand.nextInt(100); // Полупрозрачность
				this.drawRect(glitchX, glitchY, glitchX + glitchW, glitchY + glitchH, (alpha << 24) | glitchColor);
			}
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/title/mclogo.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(var6 + 0, var7 + 0, 0, 0, 155, 44);
		this.drawTexturedModalRect(var6 + 155, var7 + 0, 0, 45, 155, 44);
		var4.setColorOpaque_I(16777215);
		GL11.glPushMatrix();
		GL11.glTranslatef((float)(this.width / 2 + 90), 70.0F, 0.0F);
		GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
		float var8 = 1.8F - MathHelper.abs(MathHelper.sin((float)(System.currentTimeMillis() % 1000L) / 1000.0F * (float)Math.PI * 2.0F) * 0.1F);
		var8 = var8 * 100.0F / (float)(this.fontRenderer.getStringWidth(this.splashText) + 32);
		GL11.glScalef(var8, var8, var8);
		this.drawCenteredString(this.fontRenderer, this.splashText, 0, -8, 16776960);
		GL11.glPopMatrix();

		String versionText = manager.isEndMode ? manager.generateGlitchedString("Minecraft Beta 1.6.6") : "Minecraft Beta 1.6.6";
		this.drawString(this.fontRenderer, versionText, 2, 2, 5263440);

		String var9 = "Copyright Mojang AB. Do not distribute.";
		this.drawString(this.fontRenderer, var9, this.width - this.fontRenderer.getStringWidth(var9) - 2, this.height - 10, 16777215);
		super.drawScreen(var1, var2, var3);
	}
}
