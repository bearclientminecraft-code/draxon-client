package client.hud;

import client.module.Module;
import client.module.ModuleRegistry;
import client.notification.NotificationManager;
import net.minecraft.client.gui.GuiGraphics;

/** HUD-слой уведомлений: рисует стек, пока включён модуль Notification. */
public final class NotificationHUD {
    private NotificationHUD() {
    }

    public static void render(GuiGraphics graphics) {
        boolean enabled = ModuleRegistry.byName("Notification")
                .map(Module::isEnabled)
                .orElse(true);
        if (enabled) {
            NotificationManager.render(graphics);
        }
    }
}
