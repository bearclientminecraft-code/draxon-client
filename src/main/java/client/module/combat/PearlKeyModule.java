package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import client.gui.components.OptionRow;
import client.lang.LanguageManager;
import client.module.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Pearl Key — ручная система бинда для эндер-жемчуга.
 * Жемчуг используем ТОЛЬКО по явному действию игрока (без автоматических бросков).
 */
public final class PearlKeyModule extends Module {
    private int pearlSlot = 1;
    private int totemSlot = 1;
    private int pearlKey = 3;
    private int throwKey = 4;
    private boolean rightMouseButton = false;

    public PearlKeyModule(Category category) {
        super("Pearl Key", "Manual pearl bind", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = new ArrayList<>();
        rows.add(new OptionRow(LanguageManager.tr("combat.pearl_key.pearl_slot", "Pearl slot"), String.valueOf(pearlSlot),
                () -> String.valueOf(pearlSlot), v -> pearlSlot = Math.max(1, Integer.parseInt(v))));
        rows.add(new OptionRow(LanguageManager.tr("combat.pearl_key.totem_slot", "Totem slot"), String.valueOf(totemSlot),
                () -> String.valueOf(totemSlot), v -> totemSlot = Math.max(1, Integer.parseInt(v))));
        rows.add(new OptionRow(LanguageManager.tr("combat.pearl_key.pearl_key", "Pearl key"), String.valueOf(pearlKey),
                () -> String.valueOf(pearlKey), v -> pearlKey = Math.max(0, Integer.parseInt(v))));
        rows.add(new OptionRow(LanguageManager.tr("combat.pearl_key.throw_key", "Throw key"), String.valueOf(throwKey),
                () -> String.valueOf(throwKey), v -> throwKey = Math.max(0, Integer.parseInt(v))));
        rows.add(new OptionRow(LanguageManager.tr("combat.pearl_key.right_mouse", "Right mouse"), rightMouseButton ? "ON" : "OFF",
                () -> rightMouseButton ? "ON" : "OFF", v -> rightMouseButton = "ON".equals(v)));
        return rows;
    }

    public int getPearlSlot() { return pearlSlot; }
    public int getTotemSlot() { return totemSlot; }
    public int getPearlKey() { return pearlKey; }
    public int getThrowKey() { return throwKey; }
    public boolean isRightMouse() { return rightMouseButton; }
}
