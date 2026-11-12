package client.gui.theme;

import client.config.ConfigManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Цветовая тема ClickGUI. Чтобы добавить тему, создайте её в этом классе
 * и зарегистрируйте через {@link #register(Theme)}; выбор хранится в настройках.
 */
public final class Theme {
    private static final Map<String, Theme> PRESETS = new LinkedHashMap<>();

    /** Тёмное синее стекло. */
    public static final Theme MIDNIGHT = register(new Theme(
            "midnight",
            0x7A02060F, // затемнение фона
            0xCC131D31, // поверхность стекла (темнее)
            0x26FFFFFF, // обводка (тоньше)
            0xFF4F8CFF, // акцент
            0xFF8DB8FF, // акцент при наведении
            0xFFF2F6FF, // основной текст
            0xFF8EA0BE, // приглушённый текст
            0x66000000, // тень
            0x66101B2E  // поля ввода
    ));

    /** Нейтральное серое стекло. */
    public static final Theme GRAPHITE = register(new Theme(
            "graphite",
            0x7A000000,
            0xA8161616,
            0x3DFFFFFF,
            0xFF9CA3AF,
            0xFFD1D5DB,
            0xFFF3F4F6,
            0xFF8A8A8A,
            0x66000000,
            0x66202020
    ));

    public final String name;
    public final int background;
    public final int panel;
    public final int panelBorder;
    public final int accent;
    public final int accentSoft;
    public final int text;
    public final int textMuted;
    public final int shadow;
    public final int field;

    private Theme(String name, int background, int panel, int panelBorder, int accent, int accentSoft,
                  int text, int textMuted, int shadow, int field) {
        this.name = name;
        this.background = background;
        this.panel = panel;
        this.panelBorder = panelBorder;
        this.accent = accent;
        this.accentSoft = accentSoft;
        this.text = text;
        this.textMuted = textMuted;
        this.shadow = shadow;
        this.field = field;
    }

    private static Theme register(Theme theme) {
        PRESETS.put(theme.name, theme);
        return theme;
    }

    public static Theme get(String name) {
        return PRESETS.getOrDefault(name, MIDNIGHT);
    }

    /** Имена всех тем в порядке регистрации. */
    public static List<String> names() {
        return new ArrayList<>(PRESETS.keySet());
    }

    /** Текущая тема из настроек. */
    public static Theme current() {
        return get(ConfigManager.settings().theme);
    }
}
