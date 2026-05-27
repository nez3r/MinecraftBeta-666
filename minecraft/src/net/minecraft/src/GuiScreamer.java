package net.minecraft.src;

import org.lwjgl.opengl.GL11;

public class GuiScreamer extends GuiScreen {
    private long startTime;
    private boolean crashed = false;

    public GuiScreamer() {
        this.startTime = System.currentTimeMillis();

        // Воспроизводим звук джампскера
        try {
            if (this.mc != null && this.mc.sndManager != null) {
                this.mc.sndManager.playSoundFX("glitches.jumpscare", 1.0f, 1.0f);
            }
        } catch (Exception e) {
            System.out.println("[SCREAMER] Failed to play jumpscare sound: " + e.getMessage());
        }
    }

    public void updateScreen() {
        long elapsed = System.currentTimeMillis() - startTime;

        // Через 500мс крашим игру
        if (elapsed > 500 && !crashed) {
            crashed = true;
            // Сохраняем мир перед крашем
            if (this.mc.theWorld != null) {
                this.mc.theWorld.saveWorld(true, null);
            }
            // Возвращаемся в главное меню
            this.mc.changeWorld1((World)null);
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
    }

    public void drawScreen(int var1, int var2, float var3) {
        // Рисуем черный фон
        this.drawRect(0, 0, this.width, this.height, 0xFF000000);

        // Пытаемся загрузить текстуру error404.png
        try {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/mob/error404.png"));
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            // Рисуем на весь экран
            Tessellator tess = Tessellator.instance;
            tess.startDrawingQuads();
            tess.addVertexWithUV(0, this.height, 0.0D, 0.0D, 1.0D);
            tess.addVertexWithUV(this.width, this.height, 0.0D, 1.0D, 1.0D);
            tess.addVertexWithUV(this.width, 0, 0.0D, 1.0D, 0.0D);
            tess.addVertexWithUV(0, 0, 0.0D, 0.0D, 0.0D);
            tess.draw();
        } catch (Exception e) {
            // Если текстуры нет, просто красный экран
            this.drawRect(0, 0, this.width, this.height, 0xFFFF0000);
            this.drawCenteredString(this.fontRenderer, "404", this.width / 2, this.height / 2, 0xFFFFFFFF);
        }
    }

    protected void keyTyped(char var1, int var2) {
        // Блокируем ESC
    }
}
