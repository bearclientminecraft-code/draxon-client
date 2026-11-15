package client.notification;

import client.config.ConfigManager;
import client.gui.theme.Theme;
import client.lang.LanguageManager;
import client.render.ColorUtils;
import client.render.RenderUtils;
import client.render.TextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Собственная система уведомлений Draxon: компактный вертикальный стек строго по
 * центру экрана под прицелом, плавное появление и затухание. Ширина плашки
 * подстраивается под текст, стандартные тосты Minecraft не используются.
 *
 * <p>Новые уведомления встают сверху стека — ближе к прицелу — и расталкивают
 * предыдущие вниз, поэтому плашки никогда не перекрывают друг друга.</p>
 */
public final class NotificationManager {
    private static final List<Notification> ITEMS = new ArrayList<>();
    private static final int HEIGHT = 20;
    private static final int GAP = 4;
    private static final int MIN_WIDTH = 104;
    private static final int MAX_WIDTH = 236;
    private static final int MAX_VISIBLE = 6;

    private NotificationManager() {
    }

    public static void push(String text, Notification.Type type) {
        ITEMS.add(0, new Notification(text, type));
        while (ITEMS.size() > MAX_VISIBLE) {
            ITEMS.remove(ITEMS.size() - 1);
        }
    }

    public static void moduleToggled(client.module.Module module, boolean enabled) {
        String key = enabled ? "notification.module_enabled" : "notification.module_disabled";
        String fallback = enabled ? "Module enabled" : "Module disabled";
        push(LanguageManager.tr(key, fallback) + ": " + LanguageManager.moduleName(module),
                enabled ? Notification.Type.ENABLED : Notification.Type.DISABLED);
    }

    /** Рисует стек уведомлений по центру экрана под прицелом. */
    public static void render(GuiGraphics graphics) {
        if (ITEMS.isEmpty()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }

        Theme theme = Theme.current();
        int centerX = screenWidth / 2;
        int offset = Math.max(0, Math.min(120, ConfigManager.settings().notificationOffset));
        float y = screenHeight / 2.0f + offset;

        for (Notification notification : ITEMS) {
            float appear = notification.appear();
            float fade = notification.fade();
            float alpha = Math.max(0f, Math.min(1f, appear * fade));

            String label = TextRenderer.clip(notification.text(), MAX_WIDTH - 30,
                    TextRenderer.Weight.MEDIUM);
            int boxWidth = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH,
                    TextRenderer.width(label, TextRenderer.Weight.MEDIUM) + 30));

            // Появление: плашка чуть поднимается и проявляется, без рывков.
            int slide = Math.round((1f - appear) * 5f);
            int x = centerX - boxWidth / 2;
            int top = Math.round(y + slide);

            RenderUtils.drawShadow(graphics, x, top, boxWidth, HEIGHT, 6, 3, theme.shadow, alpha * 0.85f);
            RenderUtils.fillRounded(graphics, x, top, boxWidth, HEIGHT, 6,
                    ColorUtils.mulAlpha(theme.notification, alpha * 0.94f));
            RenderUtils.fillRounded(graphics, x, top, boxWidth, HEIGHT, 6,
                    ColorUtils.mulAlpha(theme.panelBorder, alpha * 0.35f));

            int indicator = switch (notification.type()) {
                case ENABLED -> theme.accent;
                case DISABLED -> theme.textMuted;
                case ERROR -> 0xFFD95C5C;
                case INFO -> theme.accentSoft;
            };
            RenderUtils.fillCircle(graphics, x + 10, top + HEIGHT / 2.0, 2.4,
                    ColorUtils.mulAlpha(indicator, alpha));

            int textY = top + (HEIGHT - TextRenderer.lineHeight()) / 2;
            TextRenderer.draw(graphics, label, x + 18, textY,
                    ColorUtils.mulAlpha(theme.text, alpha), TextRenderer.Weight.MEDIUM);

            y += (HEIGHT + GAP) * Math.max(0.06f, appear);
        }

        ITEMS.removeIf(Notification::expired);
    }

    public static void clear() {
        ITEMS.clear();
    }
}
