package client.module;

import client.category.Category;
import client.config.ConfigManager;
import client.gui.components.Component;
import client.gui.components.OptionRow;
import client.gui.components.TextOptionRow;
import client.lang.LanguageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Watermark как HUD-модуль категории Render: включение/выключение, текст,
 * масштаб, позиция, стиль и цвет из текущей темы. Настройки читаются при
 * отрисовке напрямую, поэтому изменения применяются сразу.
 */
public final class WatermarkModule extends Module {
    public WatermarkModule(Category category) {
        super("Watermark", "HUD watermark", category);
    }

    @Override
    public List<Component> createSettingRows() {
        ConfigManager.Settings settings = ConfigManager.settings();
        List<Component> rows = new ArrayList<>();
        rows.add(new TextOptionRow(LanguageManager.tr("watermark.text", "Text"), settings.watermarkText, 16,
                value -> apply(s -> s.watermarkText = value)));

        for (int percent : new int[]{75, 100, 125, 150, 200}) {
            rows.add(new OptionRow(LanguageManager.tr("watermark.scale", "Scale") + " " + percent + "%",
                    String.valueOf(percent),
                    () -> String.valueOf(ConfigManager.settings().watermarkScale),
                    value -> apply(s -> s.watermarkScale = Integer.parseInt(value))));
        }

        rows.add(new OptionRow(LanguageManager.tr("option.left", "Left"), "left",
                () -> ConfigManager.settings().watermarkPosition,
                value -> apply(s -> s.watermarkPosition = value)));
        rows.add(new OptionRow(LanguageManager.tr("option.center", "Center"), "center",
                () -> ConfigManager.settings().watermarkPosition,
                value -> apply(s -> s.watermarkPosition = value)));
        rows.add(new OptionRow(LanguageManager.tr("option.right", "Right"), "right",
                () -> ConfigManager.settings().watermarkPosition,
                value -> apply(s -> s.watermarkPosition = value)));

        rows.add(new OptionRow(LanguageManager.tr("style.glass", "Glass"), "glass",
                () -> ConfigManager.settings().watermarkStyle,
                value -> apply(s -> s.watermarkStyle = value)));
        rows.add(new OptionRow(LanguageManager.tr("style.minimal", "Minimal"), "minimal",
                () -> ConfigManager.settings().watermarkStyle,
                value -> apply(s -> s.watermarkStyle = value)));
        rows.add(new OptionRow(LanguageManager.tr("style.accent", "Accent"), "accent",
                () -> ConfigManager.settings().watermarkStyle,
                value -> apply(s -> s.watermarkStyle = value)));

        rows.add(new OptionRow(LanguageManager.tr("watermark.color", "Theme color") + ": "
                + LanguageManager.tr("option.on", "ON"), "on",
                () -> ConfigManager.settings().watermarkThemeColor ? "on" : "off",
                value -> apply(s -> s.watermarkThemeColor = "on".equals(value))));
        rows.add(new OptionRow(LanguageManager.tr("watermark.color", "Theme color") + ": "
                + LanguageManager.tr("option.off", "OFF"), "off",
                () -> ConfigManager.settings().watermarkThemeColor ? "on" : "off",
                value -> apply(s -> s.watermarkThemeColor = "off".equals(value))));
        return rows;
    }

    /** Применяет настройку и сразу сохраняет её: изменения видны в HUD без перезапуска. */
    private static void apply(java.util.function.Consumer<ConfigManager.Settings> change) {
        change.accept(ConfigManager.settings());
        ConfigManager.save();
    }
}
