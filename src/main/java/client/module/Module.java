package client.module;

import client.category.Category;
import client.gui.components.Component;

import java.util.List;

/**
 * Базовый класс будущих модулей. Сам по себе ничего не делает:
 * конкретные модули наследуются от него и регистрируются в {@link ModuleRegistry}.
 */
public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final Category getCategory() {
        return category;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean value) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    /** Может ли модуль включаться левым кликом. Для модулей-настроек выключено. */
    public boolean isToggleable() {
        return true;
    }

    /** Строки, которые раскрываются под модулем по правой кнопке. По умолчанию пусто. */
    public List<Component> createSettingRows() {
        return List.of();
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }
}
