package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import client.gui.components.OptionRow;
import client.lang.LanguageManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Утилиты для боевых модулей: слот/клавиша строк. */
public final class CombatModuleUtils {
    private CombatModuleUtils() {}

    public static OptionRow slotRow(String langKey, int defaultSlot, Supplier<Integer> get, Consumer<Integer> set) {
        return new OptionRow(LanguageManager.tr(langKey, "Slot " + defaultSlot),
                String.valueOf(defaultSlot),
                () -> { int v = get.get(); return v > 0 ? String.valueOf(v) : "1"; },
                v -> set.accept(Math.max(1, Integer.parseInt(v))));
    }

    public static OptionRow bindRow(String langKey, Supplier<Integer> get, Consumer<Integer> set) {
        int key = get.get();
        return new OptionRow(LanguageManager.tr(langKey, "Bind"),
                String.valueOf(key > 0 ? key : 0),
                () -> String.valueOf(key),
                v -> set.accept(Integer.parseInt(v)));
    }
}
