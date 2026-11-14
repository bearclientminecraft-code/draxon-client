package client.module;

import client.category.Category;
import client.config.ConfigManager;
import client.gui.components.Component;
import client.gui.components.OptionRow;
import client.lang.LanguageManager;

import java.util.ArrayList;
import java.util.List;

/** Armor HUD: вертикальный столбик брони. Сторона выбирается в настройках модуля. */
public final class ArmorHudModule extends Module {
    public ArmorHudModule(Category category) {
        super("Armor HUD", "Vertical armor display", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = new ArrayList<>();
        rows.add(new OptionRow(LanguageManager.tr("option.left", "Left"), "left",
                () -> ConfigManager.settings().armorHudSide, value -> apply(value)));
        rows.add(new OptionRow(LanguageManager.tr("option.right", "Right"), "right",
                () -> ConfigManager.settings().armorHudSide, value -> apply(value)));
        return rows;
    }

    private static void apply(String side) {
        if (side.equals(ConfigManager.settings().armorHudSide)) {
            return;
        }
        ConfigManager.settings().armorHudSide = side;
        ConfigManager.save();
    }
}
