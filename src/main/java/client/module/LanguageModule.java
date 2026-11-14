package client.module;

import client.category.Category;
import client.gui.components.Component;
import client.gui.components.LanguageRow;
import client.lang.LanguageManager;

import java.util.ArrayList;
import java.util.List;

/** Модуль-настройка: раскрывается правой кнопкой и показывает четыре языка. */
public final class LanguageModule extends Module {
    public LanguageModule(Category category) {
        super("Language", "Interface language", category);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = new ArrayList<>();
        for (LanguageManager.Language language : LanguageManager.Language.values()) {
            rows.add(new LanguageRow(language));
        }
        return rows;
    }
}
