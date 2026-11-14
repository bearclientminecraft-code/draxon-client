package client.hud;

import client.config.ConfigManager;
import client.hud.Watermark;
import net.minecraft.client.gui.GuiGraphics;

/** Управление водяным знаком: включается/выключается через Misc -> Watermark. */
public final class WatermarkHUD {
    private static boolean visible;

    public static void setVisible(boolean value) {
        visible = value;
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void render(GuiGraphics graphics) {
        if (!visible) {
            return;
        }
        Watermark.render(graphics);
    }
}
