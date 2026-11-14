package client.module;

import client.category.Category;
import client.gui.components.Component;
import client.notification.NotificationManager;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * Базовый класс модулей. Конкретные модули наследуются от него и регистрируются
 * в {@link ModuleRegistry}; состояние сохраняется в конфигурации Draxon.
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
        apply(value, true);
    }

    /** Включает/выключает модуль без уведомления: старт клиента и загрузка конфигурации. */
    public final void setEnabledSilently(boolean value) {
        apply(value, false);
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

    /** Вызывается каждый клиентский тик, пока модуль включён. */
    public void onTick(Minecraft client) {
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    private void apply(boolean value, boolean notify) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
        if (notify) {
            NotificationManager.moduleToggled(this, value);
        }
    }
}
