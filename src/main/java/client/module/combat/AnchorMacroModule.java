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
 * Anchor Macro — ручная настройка якоря/светокамня/тотема с переключением.
 * Ни каких автоматических действий.
 */
public final class AnchorMacroModule extends Module {
    private int anchorSlot = 1;
    private int glowstoneSlot = 2;
    private int totemSlot = 1;
    private int keybind = 5;

    public AnchorMacroModule(Category category) {
        super("Anchor Macro", "Manual slot switch", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = new ArrayList<>();
        rows.add(new OptionRow(LanguageManager.tr("combat.anchor_macro.anchor_slot", "Anchor slot"), String.valueOf(anchorSlot),
                () -> String.valueOf(anchorSlot), v -> anchorSlot = Math.max(1, Integer.parseInt(v))));
        rows.add(new OptionRow(LanguageManager.tr("combat.anchor_macro.glowstone_slot", "Glowstone slot"), String.valueOf(glowstoneSlot),
                () -> String.valueOf(glowstoneSlot), v -> glowstoneSlot = Math.max(1, Integer.parseInt(v))));
        rows.add(new OptionRow(LanguageManager.tr("combat.anchor_macro.totem_slot", "Totem slot"), String.valueOf(totemSlot),
                () -> String.valueOf(totemSlot), v -> totemSlot = Math.max(1, Integer.parseInt(v))));
        rows.add(new OptionRow(LanguageManager.tr("combat.anchor_macro.keybind", "Keybind"), String.valueOf(keybind),
                () -> String.valueOf(keybind), v -> keybind = Math.max(0, Integer.parseInt(v))));
        return rows;
    }

    public int getAnchorSlot() { return anchorSlot; }
    public int getGlowstoneSlot() { return glowstoneSlot; }
    public int getTotemSlot() { return totemSlot; }
    public int getKeybind() { return keybind; }
}
