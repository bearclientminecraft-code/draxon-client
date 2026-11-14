package client.module;

import client.category.Category;
import client.gui.components.Component;
import client.gui.components.ThemeRow;
import client.gui.theme.Theme;

import java.util.ArrayList;
import java.util.List;

/** Модуль-настройка: раскрывается правой кнопкой и показывает плашки всех тем. */
public final class ThemesModule extends Module {
    public ThemesModule(Category category) {
        super("Themes", "Interface color theme", category);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = new ArrayList<>();
        for (Theme theme : Theme.all()) {
            rows.add(new ThemeRow(theme));
        }
        return rows;
    }
}
