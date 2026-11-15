package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import client.gui.components.SlotBindRow;
import client.lang.LanguageManager;
import net.minecraft.client.Minecraft;

import java.util.List;

/** Один ПКМ — один ЛКМ, слот кристалла держится всегда; задержки из Macro Key/Delay здесь. */
public final class AutoHitCrystalModule extends CombatModule {
    public AutoHitCrystalModule(Category category) {
        super("Auto Hit Crystal", "ЛКМ по ПКМ", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(new SlotBindRow(LanguageManager.tr("combat.option.crystal_slot", "Crystal slot"),
                () -> options.clamped("crystalSlot", 0, 0, 9),
                v -> options.setInt("crystalSlot", v)));
        rows.add(toggleRow("holdRepeat", true, "combat.option.repeat_while_held"));
        return rows;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready() || !macroHeld()) {
            return;
        }
        int slot = options.clamped("crystalSlot", 0, 0, 9);
        if (slot > 0 && CombatApi.mainHandSlot() != slot) {
            CombatApi.swapToSlot(slot);
        }
        boolean useDown = client.options.keyUse.isDown();
        boolean justPressed = useDown && !useWasDown;
        useWasDown = useDown;
        boolean holdRepeat = options.getBoolean("holdRepeat", true);
        if (!useDown || (!justPressed && !holdRepeat)) {
            return;
        }
        if (!gateOpen()) {
            useWasDown = false;
            return;
        }
        CombatApi.leftClick(null);
        markAction();
    }

    @Override
    public void onEnable() {
        useWasDown = false;
    }

    @Override
    public void onDisable() {
        useWasDown = false;
    }

    private boolean useWasDown;
}
