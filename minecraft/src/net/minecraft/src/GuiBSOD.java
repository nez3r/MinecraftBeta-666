package net.minecraft.src;

import org.lwjgl.opengl.GL11;

public class GuiBSOD extends GuiScreen {
    private long startTime;
    private boolean canExit = false;

    public GuiBSOD() {
        this.startTime = System.currentTimeMillis();
    }

    public void updateScreen() {
        long elapsed = System.currentTimeMillis() - startTime;

        // Через 10 секунд можно выйти
        if (elapsed > 10000) {
            canExit = true;
        }
    }

    public void drawScreen(int var1, int var2, float var3) {
        // Синий фон (BSOD цвет)
        this.drawRect(0, 0, this.width, this.height, 0xFF0000AA);

        // Текст BSOD
        int centerX = this.width / 2;
        int startY = this.height / 4;

        this.drawCenteredString(this.fontRenderer, "A problem has been detected and Windows has been shut down to prevent damage", centerX, startY, 0xFFFFFFFF);
        this.drawCenteredString(this.fontRenderer, "to your computer.", centerX, startY + 20, 0xFFFFFFFF);

        this.drawCenteredString(this.fontRenderer, "DRIVER_IRQL_NOT_LESS_OR_EQUAL", centerX, startY + 50, 0xFFFFFFFF);

        this.drawCenteredString(this.fontRenderer, "If this is the first time you've seen this Stop error screen,", centerX, startY + 80, 0xFFFFFFFF);
        this.drawCenteredString(this.fontRenderer, "restart your computer. If this screen appears again, follow", centerX, startY + 100, 0xFFFFFFFF);
        this.drawCenteredString(this.fontRenderer, "these steps:", centerX, startY + 120, 0xFFFFFFFF);

        this.drawCenteredString(this.fontRenderer, "Check to make sure any new hardware or software is properly installed.", centerX, startY + 150, 0xFFFFFFFF);

        this.drawCenteredString(this.fontRenderer, "Technical information:", centerX, startY + 190, 0xFFFFFFFF);
        this.drawCenteredString(this.fontRenderer, "*** STOP: 0x000000D1 (0x00000000, 0x00000002, 0x00000000, 0xF86B5A89)", centerX, startY + 210, 0xFFFFFFFF);

        if (canExit) {
            this.drawCenteredString(this.fontRenderer, "Press ESC to continue", centerX, this.height - 30, 0xFFFFFF00);
        }
    }

    protected void keyTyped(char var1, int var2) {
        if (var2 == 1 && canExit) {
            // ESC - выходим
            this.mc.displayGuiScreen((GuiScreen)null);
        }
    }
}
