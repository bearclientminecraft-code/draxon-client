package client.module.combat;

import client.module.Module;
import client.module.ModuleRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Центральный опрос Macro Key.
 *
 * <p>Клавиши опрашиваются один раз за тик до всех модулей, поэтому фронт нажатия
 * не теряется, если модуль в этом тике рано вышел (нет цели, не то состояние).
 * Нажатие живёт в окне {@link #PRESS_WINDOW_MS} и «съедается» тем модулем,
 * который его обработал, поэтому цепочка не запускается дважды.</p>
 */
public final class MacroKeys {
    /** Сколько миллисекунд нажатие считается доступным для обработки. */
    public static final long PRESS_WINDOW_MS = 600;
    /** Сколько миллисекунд макрос остаётся активным после нажатия (тап вместо удержания). */
    public static final long ENGAGE_WINDOW_MS = 1500;

    private static final Map<Integer, Boolean> DOWN = new HashMap<>();
    private static final Map<Integer, Long> PRESSED = new HashMap<>();

    private MacroKeys() {
    }

    /** Вызывается из {@link ModuleRegistry#tick} до модулей. */
    public static void tick() {
        Minecraft client = Minecraft.getInstance();
        if (client.getWindow() == null || client.player == null) {
            return;
        }
        // Пока открыт любой экран (ClickGUI, инвентарь), клавиши не считаются макросом:
        // иначе назначение клавиши в настройках сразу запускало бы макрос.
        if (client.screen != null) {
            DOWN.replaceAll((key, state) -> false);
            return;
        }
        Set<Integer> keys = new HashSet<>();
        for (Module module : ModuleRegistry.all()) {
            if (module instanceof CombatModule combat) {
                int key = combat.macroKey();
                if (key > 0) {
                    keys.add(key);
                }
            }
        }
        long now = System.currentTimeMillis();
        for (int key : keys) {
            boolean down = InputConstants.isKeyDown(client.getWindow(), key);
            boolean was = Boolean.TRUE.equals(DOWN.get(key));
            if (down && !was) {
                PRESSED.put(key, now);
                // Отклик на нажатие не зависит от условий модуля — так видно, что клавиша назначена.
                for (Module module : ModuleRegistry.all()) {
                    if (module.isEnabled() && module instanceof CombatModule combat
                            && combat.macroKey() == key) {
                        combat.onMacroPressed();
                    }
                }
            }
            DOWN.put(key, down);
        }
    }

    public static boolean isDown(int key) {
        return Boolean.TRUE.equals(DOWN.get(key));
    }

    public static long lastPress(int key) {
        Long time = PRESSED.get(key);
        return time == null ? 0L : time;
    }

    /** Нажатие было недавно и ещё не обработано. */
    public static boolean wasPressed(int key, long windowMs) {
        long time = lastPress(key);
        return time > 0 && System.currentTimeMillis() - time <= windowMs;
    }

    /** Забирает нажатие: следующий вызов вернёт false, пока клавишу не нажмут снова. */
    public static boolean consumePress(int key, long windowMs) {
        if (wasPressed(key, windowMs)) {
            PRESSED.remove(key);
            return true;
        }
        return false;
    }
}
