package client.lang;

import client.DraxonClient;
import client.config.ConfigManager;
import client.module.Module;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Менеджер языков Draxon. Строки лежат в assets/draxon/lang/*.json и читаются
 * из ресурсов мода, поэтому переключение не требует перезапуска и не обращается
 * к сети: при смене языка кэш просто перечитывается.
 */
public final class LanguageManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DraxonClient.NAME);
    private static final Map<String, Map<String, String>> CACHE = new HashMap<>();

    private LanguageManager() {
    }

    /** Доступные языки интерфейса. Названия даны на самом языке. */
    public enum Language {
        RU("ru_ru", "\u0420\u0443\u0441\u0441\u043a\u0438\u0439"),
        EN("en_us", "English"),
        DE("de_de", "Deutsch"),
        ES("es_es", "Espa\u00f1ol");

        private final String code;
        private final String displayName;

        Language(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String code() {
            return code;
        }

        public String displayName() {
            return displayName;
        }

        public static Language fromCode(String code) {
            for (Language language : values()) {
                if (language.code.equalsIgnoreCase(code)) {
                    return language;
                }
            }
            return EN;
        }
    }

    public static Language current() {
        return Language.fromCode(ConfigManager.settings().language);
    }

    public static void setLanguage(Language language) {
        if (language == null) {
            return;
        }
        ConfigManager.settings().language = language.code();
        CACHE.clear();
        ConfigManager.save();
    }

    /** Сбрасывает кэш строк: нужно после загрузки конфигурации с другим языком. */
    public static void refresh() {
        CACHE.clear();
    }

    /** Перевод по ключу с запасным значением. */
    public static String tr(String key, String fallback) {
        String value = strings(current().code()).get(key);
        return value != null ? value : fallback;
    }

    public static String tr(String key) {
        return tr(key, key);
    }

    public static String category(String id, String fallback) {
        return tr("category." + id, fallback);
    }

    public static String moduleName(Module module) {
        return tr(moduleKey(module.getName()), module.getName());
    }

    public static String moduleDescription(Module module) {
        String key = moduleKey(module.getName()) + ".desc";
        return tr(key, module.getDescription());
    }

    /** Ключ перевода для названия модуля: "Auto Respawn" -> "module.auto_respawn". */
    public static String moduleKey(String name) {
        StringBuilder builder = new StringBuilder("module.");
        for (char symbol : name.toLowerCase(Locale.ROOT).toCharArray()) {
            if (Character.isLetterOrDigit(symbol)) {
                builder.append(symbol);
            } else if (builder.charAt(builder.length() - 1) != '_') {
                builder.append('_');
            }
        }
        while (builder.charAt(builder.length() - 1) == '_') {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

    private static Map<String, String> strings(String code) {
        return CACHE.computeIfAbsent(code, LanguageManager::load);
    }

    private static Map<String, String> load(String code) {
        Map<String, String> result = new HashMap<>();
        Identifier id = Identifier.fromNamespaceAndPath(DraxonClient.MOD_ID, "lang/" + code + ".json");
        try {
            List<Resource> resources = Minecraft.getInstance().getResourceManager().getResourceStack(id);
            if (resources.isEmpty()) {
                LOGGER.warn("\u041d\u0435\u0442 \u0444\u0430\u0439\u043b\u0430 \u044f\u0437\u044b\u043a\u0430 {}", id);
                return result;
            }
            Resource resource = resources.get(resources.size() - 1);
            try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                for (String key : json.keySet()) {
                    result.put(key, json.get(key).getAsString());
                }
            }
        } catch (Exception exception) {
            LOGGER.error("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u044f\u0437\u044b\u043a {}", id, exception);
        }
        return result;
    }
}
