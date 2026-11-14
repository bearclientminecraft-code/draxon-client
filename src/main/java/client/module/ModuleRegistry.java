package client.module;

import client.category.Category;
import client.category.CategoryRegistry;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Реестр модулей. Сейчас содержит только реально реализованные модули Misc;
 * остальные категории остаются пустыми, пока в них не появятся свои реализации.
 */
public final class ModuleRegistry {
    private static final List<Module> MODULES = new ArrayList<>();

    private ModuleRegistry() {
    }

    /** Заполняет реестр модулями (вызывается при старте клиента). */
    public static void init() {
        if (!MODULES.isEmpty()) {
            return;
        }
        Category misc = category("misc");
        register(new AutoRespawnModule(misc));
        register(new ThemesModule(misc));
        register(new LanguageModule(misc));
        register(new OptimizationModule(misc));

        Category miscHud = category("misc");
        register(new WatermarkModule(miscHud));
        register(new NotificationModule(miscHud));

        Category render = category("render");
        register(new ArmorHudModule(render));

        Category combat = category("combat");
        client.module.combat.CombatModules.register(combat);
    }

    private static Category category(String id) {
        return CategoryRegistry.get(id).orElseThrow(() -> new IllegalStateException("Нет категории " + id));
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

    public static Optional<Module> byName(String name) {
        return MODULES.stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /** Тик клиента: включённые модули получают управление. */
    public static void tick(Minecraft client) {
        for (Module module : MODULES) {
            if (module.isEnabled()) {
                module.onTick(client);
            }
        }
    }

    /** Поиск по названию и описанию. Пустой запрос возвращает пустой список. */
    public static List<Module> search(String query) {
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return List.of();
        }
        return MODULES.stream()
                .filter(module -> module.getName().toLowerCase(Locale.ROOT).contains(normalized)
                        || module.getDescription().toLowerCase(Locale.ROOT).contains(normalized))
                .toList();
    }
}
