package client.gui.theme;

import client.config.ConfigManager;
import client.render.ColorUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Цветовая тема ClickGUI. Каждая тема задаётся базовыми цветами (фон, панель, акцент,
 * текст), опционально — цветом плашки и вторым акцентом, и разворачивает из них полную
 * палитру из девяти цветов: фон, панель, плашка модуля, акцент, текст, вторичный текст,
 * граница, hover и уведомления.
 */
public final class Theme {
    private static final Map<String, Theme> PRESETS = new LinkedHashMap<>();

    /** Базовая тема Draxon: нейтральное тёмное стекло с холодным акцентом. */
    public static final Theme DRAXON = register("draxon", "Draxon",
            0x05070B, 0x15181E, 0x9AA7C7, 0xF4F6FA);
    public static final Theme MIDNIGHT_BLUE = register("midnight_blue", "Midnight Blue",
            0x080D18, 0x111B2B, 0x3978E8, 0xEAF1FF);
    public static final Theme OBSIDIAN_PURPLE = register("obsidian_purple", "Obsidian Purple",
            0x100D18, 0x1C1728, 0x9365E8, 0xF1EAFE);
    public static final Theme ARCTIC = register("arctic", "Arctic",
            0x0B141B, 0x14232D, 0x50BDEB, 0xE7F8FF);
    public static final Theme EMERALD = register("emerald", "Emerald",
            0x0B1512, 0x14251D, 0x35B77A, 0xE7FFF1);
    public static final Theme ROSE_QUARTZ = register("rose_quartz", "Rose Quartz",
            0x180F17, 0x281824, 0xDB78B2, 0xFFF0F8);
    public static final Theme GRAPHITE = register("graphite", "Graphite",
            0x101010, 0x1C1C1C, 0xA0A0A0, 0xF5F5F5);
    public static final Theme SUNSET = register("sunset", "Sunset",
            0x171016, 0x281B22, 0xE58A55, 0xFFF0E6);
    public static final Theme OCEAN = register("ocean", "Ocean",
            0x081619, 0x10262B, 0x32BFC1, 0xE6FFFF);
    public static final Theme CRIMSON = register("crimson", "Crimson",
            0x170D10, 0x281519, 0xD94B62, 0xFFECEF);
    public static final Theme CHAMPAGNE = register("champagne", "Champagne",
            0x15130F, 0x252119, 0xC7A66A, 0xFFF8E9);
    /** Halloween: тыквенный оранжевый и фиолетовый на почти чёрном фоне. */
    public static final Theme HALLOWEEN = register("halloween", "Halloween",
            0x120B18, 0x211329, 0xFF7518, 0xFFF0E6, 0x2B1933, 0xA64DFF);
    /** Cyberpunk: неоновый бирюзовый с маджентовым вторым акцентом. */
    public static final Theme CYBERPUNK = register("cyberpunk", "Cyberpunk",
            0x080B16, 0x11182A, 0x00F5D4, 0xEAFBFF, 0x19243A, 0xFF3CAC);

    public final String id;
    public final String displayName;
    /** Четыре базовых цвета для компактной плашки: фон, поверхность, акцент, текст. */
    public final int[] swatches;
    public final int background;
    public final int panel;
    public final int module;
    public final int panelBorder;
    public final int accent;
    public final int accentSoft;
    public final int text;
    public final int textMuted;
    public final int hover;
    public final int notification;
    public final int shadow;
    public final int field;

    private Theme(String id, String displayName, int background, int panel, int accent, int text) {
        this(id, displayName, background, panel, accent, text, 0, 0);
    }

    /**
     * @param card      отдельный цвет плашки модуля; 0 — выводится из панели
     * @param secondary второй акцент (наведение, мягкий акцент); 0 — выводится из основного
     */
    private Theme(String id, String displayName, int background, int panel, int accent, int text,
                  int card, int secondary) {
        this.id = id;
        this.displayName = displayName;
        int bg = opaque(background);
        int surface = opaque(panel);
        int accentColor = opaque(accent);
        int textColor = opaque(text);

        this.swatches = new int[]{bg, surface, accentColor, textColor};
        this.background = withAlpha(bg, 0x99);
        this.panel = withAlpha(surface, 0xE8);
        this.module = card != 0
                ? withAlpha(opaque(card), 0x66)
                : withAlpha(ColorUtils.mix(surface, textColor, 0.05f) & 0xFFFFFF, 0x55);
        this.panelBorder = ColorUtils.mix(surface, textColor, 0.10f);
        this.accent = accentColor;
        this.accentSoft = secondary != 0 ? opaque(secondary) : ColorUtils.mix(accentColor, textColor, 0.35f);
        this.text = textColor;
        this.textMuted = ColorUtils.mix(textColor, surface, 0.45f);
        this.hover = withAlpha(ColorUtils.mix(surface, secondary != 0 ? opaque(secondary) : accentColor,
                secondary != 0 ? 0.28f : 0.20f) & 0xFFFFFF, 0xA6);
        this.notification = withAlpha(surface & 0xFFFFFF, 0xF0);
        this.shadow = 0x66000000;
        this.field = withAlpha(surface & 0xFFFFFF, 0x99);
    }

    private static Theme register(String id, String displayName, int background, int panel, int accent, int text) {
        Theme theme = new Theme(id, displayName, background, panel, accent, text);
        PRESETS.put(id, theme);
        return theme;
    }

    private static Theme register(String id, String displayName, int background, int panel, int accent, int text,
                                  int card, int secondary) {
        Theme theme = new Theme(id, displayName, background, panel, accent, text, card, secondary);
        PRESETS.put(id, theme);
        return theme;
    }

    private static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    private static int withAlpha(int argb, int alpha) {
        return (alpha << 24) | (argb & 0xFFFFFF);
    }

    /** Полный набор цветов темы для предпросмотра. */
    public int[] palette() {
        return new int[]{background, panel, module, accent, text, textMuted, panelBorder, hover, notification};
    }

    public static Theme get(String id) {
        return PRESETS.getOrDefault(id, DRAXON);
    }

    public static List<Theme> all() {
        return new ArrayList<>(PRESETS.values());
    }

    /** Текущая тема из настроек. */
    public static Theme current() {
        return get(ConfigManager.settings().theme);
    }
}
