package client.gui.theme;

import client.config.ConfigManager;
import client.render.ColorUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Цветовая тема ClickGUI. Задаётся четырьмя цветами: фон, поверхность, акцент, текст.
 * Остальные цвета (обводка, приглушённый текст, поля) вычисляются из них.
 */
public final class Theme {
    private static final Map<String, Theme> PRESETS = new LinkedHashMap<>();

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
            0x15130F, 0x252119, 0xC7A66A, 0xFFF8E0);

    public final String id;
    public final String displayName;
    /** Цвета для плашки: фон, поверхность, акцент, текст. */
    public final int[] swatches;
    public final int background;
    public final int panel;
    public final int panelBorder;
    public final int accent;
    public final int accentSoft;
    public final int text;
    public final int textMuted;
    public final int shadow;
    public final int field;

    private Theme(String id, String displayName, int background, int panel, int accent, int text) {
        this.id = id;
        this.displayName = displayName;
        int bg = opaque(background);
        int surface = opaque(panel);
        int accentColor = opaque(accent);
        int textColor = opaque(text);

        this.swatches = new int[]{bg, surface, accentColor, textColor};
        this.background = withAlpha(bg, 0x7A);
        this.panel = withAlpha(surface, 0xCC);
        this.panelBorder = ColorUtils.mix(surface, textColor, 0.22f);
        this.accent = accentColor;
        this.accentSoft = ColorUtils.mix(accentColor, textColor, 0.35f);
        this.text = textColor;
        this.textMuted = ColorUtils.mix(textColor, surface, 0.45f);
        this.shadow = 0x66000000;
        this.field = withAlpha(surface, 0x66);
    }

    private static Theme register(String id, String displayName, int background, int panel, int accent, int text) {
        Theme theme = new Theme(id, displayName, background, panel, accent, text);
        PRESETS.put(id, theme);
        return theme;
    }

    private static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    private static int withAlpha(int argb, int alpha) {
        return (alpha << 24) | (argb & 0xFFFFFF);
    }

    public static Theme get(String id) {
        return PRESETS.getOrDefault(id, MIDNIGHT_BLUE);
    }

    public static List<Theme> all() {
        return new ArrayList<>(PRESETS.values());
    }

    /** Текущая тема из настроек. */
    public static Theme current() {
        return get(ConfigManager.settings().theme);
    }
}
