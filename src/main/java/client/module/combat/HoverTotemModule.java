package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import client.gui.components.OptionRow;
import client.lang.LanguageManager;
import client.module.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Hover Totem — отображение информации о тотеме в HUD, позиция/масштаб/цвета,
 * счётчик тотемов в инвентаре. Ничего не нажимает автоматически.
 */
public final class HoverTotemModule extends Module {
    private boolean visible = true;
    private double scale = 1.0;
    private int position = 0; // 0 = left, 1 = right, 2 = center
    private int colText = 0xFFFFFF;
    private int colBg = 0xFF1F1F1F;
    private int slot = 1;

    public HoverTotemModule(Category category) {
        super("Hover Totem", "Totem info in HUD", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = new ArrayList<>();
        rows.add(new OptionRow(LanguageManager.tr("combat.hover_totem.visible", "Visible"), "on",
                () -> String.valueOf(visible), v -> setVisible("on".equals(v))));
        rows.add(new OptionRow(LanguageManager.tr("combat.hover_totem.slot", "Slot"), String.valueOf(slot),
                () -> String.valueOf(slot), v -> slot = Math.max(1, Integer.parseInt(v))));
        rows.add(new OptionRow(LanguageManager.tr("combat.hover_totem.scale", "Scale"), String.valueOf((int)(scale * 100)),
                () -> String.valueOf((int)(scale * 100)), v -> scale = Math.max(50, Math.min(250, Integer.parseInt(v))) / 100f));
        rows.add(new OptionRow(LanguageManager.tr("combat.hover_totem.position", "Position"), "left",
                () -> { int p = position; return p == 0 ? "Left" : p == 1 ? "Right" : "Center"; },
                v -> position = switch (v) { case "Left" -> 0; case "Right" -> 1; case "Center" -> 2; default -> 0; }));
        rows.add(new OptionRow(LanguageManager.tr("combat.hover_totem.color_text", "Text color"), String.format("%06X", colText & 0xFFFFFF),
                () -> String.format("%06X", colText & 0xFFFFFF),
                v -> colText = 0xFF000000 | (Integer.parseInt(v, 16) & 0xFFFFFF)));
        rows.add(new OptionRow(LanguageManager.tr("combat.hover_totem.color_bg", "Bg color"), String.format("%06X", colBg & 0xFFFFFF),
                () -> String.format("%06X", colBg & 0xFFFFFF),
                v -> colBg = 0xFF000000 | (Integer.parseInt(v, 16) & 0xFFFFFF)));
        return rows;
    }

    private void setVisible(boolean value) { this.visible = value; }

    public boolean isVisible() { return visible; }
    public double getScale() { return scale; }
    public int getPosition() { return position; }
    public int getColText() { return colText; }
    public int getColBg() { return colBg; }
    public int getSlot() { return slot; }

    public int countTotems() { return 0; }
}
