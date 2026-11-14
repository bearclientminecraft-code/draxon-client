package client.module;

import client.category.Category;

/** Модуль без логики: заполняет списки ClickGUI, пока у категории нет своей реализации. */
public final class SimpleModule extends Module {
    public SimpleModule(String name, Category category, boolean enabledByDefault) {
        super(name, "", category);
        setEnabledSilently(enabledByDefault);
    }
}
