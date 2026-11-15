package client.module.combat;

import client.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Настройки боевого модуля. Значения хранятся строками в общем словаре конфигурации
 * ({@code Settings.combat}), поэтому назначенные слоты, Macro Key, задержки и
 * режимы переживают перезапуск без отдельных полей на каждый модуль.
 *
 * <p>Карта берётся из {@link ConfigManager#settings()} при каждом чтении: загрузка
 * конфигурации заменяет объект настроек целиком, и закэшированная ссылка на карту
 * теряла бы все сохранённые значения.</p>
 */
public final class CombatOptions {
    private final String id;

    private CombatOptions(String id) {
        this.id = id;
    }

    /** Возвращает набор настроек модуля. */
    public static CombatOptions of(String id) {
        return new CombatOptions(id);
    }

    private Map<String, String> values() {
        Map<String, Map<String, String>> all = ConfigManager.settings().combat;
        if (all == null) {
            all = new HashMap<>();
            ConfigManager.settings().combat = all;
        }
        return all.computeIfAbsent(id, key -> new HashMap<>());
    }

    public String id() {
        return id;
    }

    public String get(String key, String fallback) {
        String value = values().get(key);
        return value == null ? fallback : value;
    }

    public void set(String key, String value) {
        values().put(key, value);
        ConfigManager.save();
    }

    public int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key, String.valueOf(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public void setInt(String key, int value) {
        set(key, String.valueOf(value));
    }

    public boolean getBoolean(String key, boolean fallback) {
        return Boolean.parseBoolean(get(key, String.valueOf(fallback)));
    }

    public void setBoolean(String key, boolean value) {
        set(key, String.valueOf(value));
    }

    /** Ограничивает int-настройку диапазоном (для слайдеров и шагов). */
    public int clamped(String key, int fallback, int min, int max) {
        return Math.max(min, Math.min(max, getInt(key, fallback)));
    }
}
