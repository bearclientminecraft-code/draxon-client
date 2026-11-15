package client.module;

import client.category.Category;
import client.config.ConfigManager;
import client.gui.components.Component;
import client.gui.components.OptionRow;
import client.lang.LanguageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Уведомления Draxon: включаются/выключаются кликом, смещение стека от прицела
 * настраивается. Сама отрисовка живёт в {@link client.notification.NotificationManager}.
 */
public final class NotificationModule extends Module {
    private static final int[] OFFSETS = {20, 26, 34, 44, 56};

    public NotificationModule(Category category) {
        super("Notification", "Own notification stack", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = new ArrayList<>();
        for (int offset : OFFSETS) {
            rows.add(new OptionRow(LanguageManager.tr("notification.offset", "Offset") + " " + offset,
                    String.valueOf(offset),
                    () -> String.valueOf(ConfigManager.settings().notificationOffset),
                    value -> {
                        ConfigManager.settings().notificationOffset = Integer.parseInt(value);
                        ConfigManager.save();
                    }));
        }
        return rows;
    }
}
