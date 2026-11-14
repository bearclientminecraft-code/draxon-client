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
 * Auto Crystal — только визуальный и информационный компонент: слот кристаллов,
 * счётчик, позиция/масштаб HUD, цвета. Автоматическая установка/разрушение НЕ
 * реализована.
 */
public final class AutoCrystalModule extends Module {
    private int crystalSlot = 1;
    private int hudSlot = 1;
    private int slot = 1;
    private double scale = 1.0;
    private int colorText = 0xFFFFFF;
    private int colorBg = 0xFF1F1F1F;

    public AutoCrystalModule(Category category) {
        super("Auto Crystal", "Crystal slot info in HUD", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = new ArrayList<>();
        rows.add(new OptionRow(LanguageManager.tr("combat.auto_crystal.crystal_slot", "Crystal slot"), String.valueOf(crystalSlot),
                () -> String.valueOf(crystalSlot), v -> crystalSlot = Math.max(1, Integer.parseInt(v))));
        rows.add(new OptionRow(LanguageManager.tr("combat.auto_crystal.hud_slot", "HUD slot"), String.valueOf(hudSlot),
                () -> String.valueOf(hudSlot), v -> hudSlot = Math.max(1, Integer.parseInt(v))));
        rows.add(new OptionRow(LanguageManager.tr("combat.auto_crystal.slot", "Slot"), String.valueOf(slot),
                () -> String.valueOf(slot), v -> slot = Math.max(1, Integer.parseInt(v))));
        rows.add(new OptionRow(LanguageManager.tr("combat.auto_crystal.scale", "Scale"), String.valueOf((int)(scale * 100)),
                () -> String.valueOf((int)(scale * 100)), v -> scale = Math.max(50, Math.min(250, Integer.parseInt(v))) / 100f));
        return rows;
    }

    public int getCrystalSlot() { return crystalSlot; }
    public int getHudSlot() { return hudSlot; }
    public int getSlot() { return slot; }
    public double getScale() { return scale; }
    public int getColorText() { return colorText; }
    public int getColorBg() { return colorBg; }
}
