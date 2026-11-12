package client.category;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Реестр категорий. Новые категории регистрируются через {@link #register(Category)}
 * и автоматически появляются в ClickGUI.
 */
public final class CategoryRegistry {
    private static final Map<String, Category> CATEGORIES = new LinkedHashMap<>();

    static {
        register(new Category("combat", "Combat", CategoryIcon.SWORDS));
        register(new Category("movement", "Movement", CategoryIcon.ARROW));
        register(new Category("render", "Render", CategoryIcon.MONITOR));
        register(new Category("player", "Player", CategoryIcon.PERSON));
        register(new Category("misc", "Misc", CategoryIcon.GEAR));
    }

    private CategoryRegistry() {
    }

    /** Принудительно загружает реестр (вызывается при старте клиента). */
    public static void init() {
    }

    public static void register(Category category) {
        if (CATEGORIES.containsKey(category.id())) {
            throw new IllegalArgumentException("Категория уже зарегистрирована: " + category.id());
        }
        CATEGORIES.put(category.id(), category);
    }

    public static Collection<Category> all() {
        return Collections.unmodifiableCollection(CATEGORIES.values());
    }

    public static Optional<Category> get(String id) {
        return Optional.ofNullable(CATEGORIES.get(id));
    }
}
