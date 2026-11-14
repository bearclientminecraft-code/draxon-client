package client.hud;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Проверяет, есть ли у игрока активные босс-бары. Ванильный оверлей не отдаёт это
 * состояние публично, поэтому читаем его карту событий через отражение. Имена
 * разрешаются через {@link MappingResolver}, так что код одинаково работает в dev
 * окружении (named) и в собранном моде (intermediary). Любая ошибка — считаем,
 * что босс-бара нет: водяной знак просто останется на месте.
 */
public final class BossBarProbe {
    private static final String NAMED_OWNER = "net.minecraft.client.gui.components.BossHealthOverlay";

    private static boolean resolved;
    private static Field eventsField;

    private BossBarProbe() {
    }

    public static boolean visible() {
        Field field = field();
        if (field == null) {
            return false;
        }
        try {
            Object overlay = Minecraft.getInstance().gui.getBossOverlay();
            Object value = field.get(overlay);
            return value instanceof Map<?, ?> events && !events.isEmpty();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }

    private static Field field() {
        if (resolved) {
            return eventsField;
        }
        resolved = true;
        try {
            MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
            Class<?> owner = Class.forName(resolver.unmapClassName("named", NAMED_OWNER));
            // Поле ищем по типу, а не по имени: так код не зависит от маппингов.
            for (Field candidate : owner.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(candidate.getType())) {
                    candidate.setAccessible(true);
                    eventsField = candidate;
                    break;
                }
            }
        } catch (ClassNotFoundException | RuntimeException exception) {
            eventsField = null;
        }
        return eventsField;
    }
}
