package client.notification;

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
 * Собственная система уведомлений Draxon: стек плашек в правом верхнем углу,
 * появление с выездом, автоматическое исчезновение и мягкое затухание.
 * Ширина плашки подстраивается под текст, стандартные тосты Minecraft не используются.
 */
public final class NotificationManager {
    private static final List<Notification> ITEMS = new ArrayList<>();
    private static final int HEIGHT = 24;
    private static final int GAP = 6;
    private static final int MARGIN = 8;
    private static final int MIN_WIDTH = 120;
    private static final int MAX_WIDTH = 260;

    private NotificationManager() {
    }

    public static void push(String text, Notification.Type type) {
        ITEMS.add(0, new Notification(text, type));
        while (ITEMS.size() > 6) {
            ITEMS.remove(ITEMS.size() - 1);
        }
    }

    public static void moduleToggled(client.module.Module module, boolean enabled) {
        String state = LanguageManager.tr(enabled ? "notification.on" : "notification.off",
                enabled ? "ON" : "OFF");
        push(LanguageManager.moduleName(module) + " \u2014 " + state,
                enabled ? Notification.Type.ENABLED : Notification.Type.DISABLED);
    }

    /** Рисует стек уведомлений в правом верхнем углу. */
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
        float y = MARGIN;
        for (Notification notification : ITEMS) {
            float appear = notification.appear();
            float fade = notification.fade();
            float alpha = Math.max(0f, Math.min(1f, appear * fade));

            String label = TextRenderer.clip(notification.text(), MAX_WIDTH - 34,
                    TextRenderer.Weight.MEDIUM);
            int boxWidth = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH,
                    TextRenderer.width(label, TextRenderer.Weight.MEDIUM) + 34));

            int offset = Math.round((1f - appear) * 26f);
            int x = screenWidth - MARGIN - boxWidth + offset;
            int top = Math.round(y);

            RenderUtils.drawShadow(graphics, x, top, boxWidth, HEIGHT, 8, 4, theme.shadow, alpha * 0.9f);
            RenderUtils.fillRounded(graphics, x, top, boxWidth, HEIGHT, 8,
                    ColorUtils.mulAlpha(theme.notification, alpha * 0.94f));
            RenderUtils.fillRounded(graphics, x, top, boxWidth, HEIGHT, 8,
                    ColorUtils.mulAlpha(theme.panelBorder, alpha * 0.25f));

            int indicator = switch (notification.type()) {
                case ENABLED -> theme.accent;
                case DISABLED -> theme.textMuted;
                case ERROR -> 0xFFD95C5C;
                case INFO -> theme.accentSoft;
            };
            RenderUtils.fillCircle(graphics, x + 14, top + HEIGHT / 2.0, 3.0,
                    ColorUtils.mulAlpha(indicator, alpha));

            int textY = top + (HEIGHT - TextRenderer.lineHeight()) / 2;
            TextRenderer.draw(graphics, label, x + 24, textY,
                    ColorUtils.mulAlpha(theme.text, alpha), TextRenderer.Weight.MEDIUM);

            y += (HEIGHT + GAP) * Math.max(0.08f, appear);
        }

        ITEMS.removeIf(Notification::expired);
    }

    public static void clear() {
        ITEMS.clear();
    }
}
