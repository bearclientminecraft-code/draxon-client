package client.module.combat;

import client.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Настройки боевого модуля. Значения хранятся строками в общем словаре конфигурации
 * ({@code Settings.combat}), поэтому назначенные слоты, Macro Key, задержки и
 * режимы переживают перезапуск без отдельных полей на каждый модуль.
 */
public final class CombatOptions {
    private final String id;
    private final Map<String, String> values;

    private CombatOptions(String id, Map<String, String> values) {
        this.id = id;
        this.values = values;
    }

    /** Возвращает (и при необходимости создаёт) набор настроек модуля. */
    public static CombatOptions of(String id) {
        Map<String, Map<String, String>> all = ConfigManager.settings().combat;
        if (all == null) {
            all = new HashMap<>();
            ConfigManager.settings().combat = all;
        }
        Map<String, String> values = all.computeIfAbsent(id, key -> new HashMap<>());
        return new CombatOptions(id, values);
    }

    public String id() {
        return id;
    }

    public String get(String key, String fallback) {
        String value = values.get(key);
        return value == null ? fallback : value;
    }

    public void set(String key, String value) {
        values.put(key, value);
        save();
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

    private void save() {
        ConfigManager.save();
    }
}
