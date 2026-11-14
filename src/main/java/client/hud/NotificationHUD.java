package client.hud;

import client.config.ConfigManager;
import client.notification.NotificationManager;
import net.minecraft.client.gui.GuiGraphics;

/** Лёгкий HUD-слой для собственных уведомлений, гаджет виден в режиме HUD. */
public final class NotificationHUD {
    private static boolean visible;

    public static void setVisible(boolean value) {
        visible = value;
    }

    public static void render(GuiGraphics graphics) {
        if (!visible) {
            return;
        }
        NotificationManager.render(graphics);
    }
}
