package client.gui.theme;

import client.config.ConfigManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Цветовая тема ClickGUI. Чтобы добавить тему, создайте экземпляр через конструктор
 * и зарегистрируйте его в {@link #register(Theme)}; выбор хранится в настройках.
 */
public final class Theme {
    private static final Map<String, Theme> PRESETS = new LinkedHashMap<>();

    /** Тёмно-синяя тема, близкая к референсу. */
    public static final Theme MIDNIGHT = register(new Theme(
            "midnight",
            0xA0030812, // затемнение фона
            0xD00C1422, // фон панелей
            0x402B4A7A, // обводка панелей
            0xFF3B82F6, // акцент
            0xFF60A5FA, // акцент при наведении
            0xFFE6EEFF, // основной текст
            0xFF7D8DAA, // приглушённый текст
            0x80000000, // тень
            0x66101B2E  // фон полей ввода
    ));

    /** Нейтральная серая тема. */
    public static final Theme GRAPHITE = register(new Theme(
            "graphite",
            0xA0000000,
            0xD0151515,
            0x40FFFFFF,
            0xFF9CA3AF,
            0xFFD1D5DB,
            0xFFF3F4F6,
            0xFF8A8A8A,
            0x80000000,
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

    /** Текущая тема из настроек. */
    public static Theme current() {
        return get(ConfigManager.settings().theme);
    }
}
