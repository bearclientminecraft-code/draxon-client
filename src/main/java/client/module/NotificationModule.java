package client.module;

import client.category.Category;
import client.gui.animation.Animation;
import client.config.ConfigManager;
import client.hud.NotificationHUD;
import client.notification.NotificationManager;
import net.minecraft.client.Minecraft;

/** Разворачивается в Misc — управление собственными уведомлениями (стек справа сверху). */
public final class NotificationModule extends Module {
    private final Animation state = new Animation(0f, 0.25f);

    public NotificationModule(Category category) {
        super("Notification", "Own notifications stack", category);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    @Override
    public void onTick(Minecraft client) {
        // Включение/выключение стека уведомлений управляется модулем (Misc -> Notification).
        NotificationHUD.setVisible(ConfigManager.settings().notificationVisible);
    }
}
