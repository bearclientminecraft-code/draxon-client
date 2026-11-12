package client.module;

import client.category.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Реестр модулей. Пока модули не добавлены, все запросы возвращают пустые списки:
 * поиск не выдаёт фейковых результатов.
 */
public final class ModuleRegistry {
    private static final List<Module> MODULES = new ArrayList<>();

    private ModuleRegistry() {
    }

    /** Принудительно загружает реестр (вызывается при старте клиента). */
    public static void init() {
    }

    public static void register(Module module) {
        boolean duplicate = MODULES.stream()
                .anyMatch(existing -> existing.getName().equalsIgnoreCase(module.getName()));
        if (duplicate) {
            throw new IllegalArgumentException("Модуль уже зарегистрирован: " + module.getName());
        }
        MODULES.add(module);
    }

    public static List<Module> all() {
        return List.copyOf(MODULES);
    }

    public static List<Module> byCategory(Category category) {
        return MODULES.stream()
                .filter(module -> module.getCategory().equals(category))
                .toList();
    }

    /** Поиск по названию. Пустой запрос возвращает пустой список. */
    public static List<Module> search(String query) {
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return List.of();
        }
        return MODULES.stream()
                .filter(module -> module.getName().toLowerCase(Locale.ROOT).contains(normalized))
                .toList();
    }
}
