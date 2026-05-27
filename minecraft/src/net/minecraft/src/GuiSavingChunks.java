package net.minecraft.src;

public class GuiSavingChunks extends GuiScreen {
    private long startTime;
    private boolean shouldClose = false;
    private boolean weatherChanged = false;

    public GuiSavingChunks() {
        this.startTime = System.currentTimeMillis();
    }

    public void initGui() {
        super.initGui();
        // Устанавливаем skipRenderWorld сразу после инициализации
        if (this.mc != null) {
            this.mc.skipRenderWorld = true;
        }
    }

    public void updateScreen() {
        super.updateScreen();
        long elapsed = System.currentTimeMillis() - startTime;

        // Меняем погоду сразу при открытии
        if (!weatherChanged && this.mc != null && this.mc.theWorld != null) {
            weatherChanged = true;
            if (this.mc.theWorld.rand.nextBoolean()) {
                // Ночь
                this.mc.theWorld.setWorldTime(13000);
            } else {
                // Гроза
                this.mc.theWorld.worldInfo.setRaining(true);
                this.mc.theWorld.worldInfo.setThundering(true);
            }
        }

        // Через 3 секунды закрываем экран
        if (elapsed > 3000 && !shouldClose) {
            shouldClose = true;
            // Восстанавливаем skipRenderWorld перед закрытием
            if (this.mc != null) {
                this.mc.skipRenderWorld = false;
            }
            this.mc.displayGuiScreen((GuiScreen)null);
        }
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        // Восстанавливаем skipRenderWorld при закрытии
        if (this.mc != null) {
            this.mc.skipRenderWorld = false;
        }
    }

    public void drawScreen(int var1, int var2, float var3) {
        // Черный фон
        this.drawRect(0, 0, this.width, this.height, 0xFF000000);

        // Текст "Saving chunks"
        String text = "Saving chunks";
        if (this.fontRenderer != null) {
            int textWidth = this.fontRenderer.getStringWidth(text);
            this.drawString(this.fontRenderer, text, (this.width - textWidth) / 2, this.height / 2 - 10, 0xFFFFFFFF);
        }

        super.drawScreen(var1, var2, var3);
    }

    protected void keyTyped(char var1, int var2) {
        // Блокируем все клавиши включая ESC
        if (var2 == 1) {
            // ESC - не закрываем
            return;
        }
    }

    public boolean doesGuiPauseGame() {
        return true;
    }
}
