package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import client.gui.components.SlotBindRow;
import client.lang.LanguageManager;
import net.minecraft.client.Minecraft;

import java.util.List;

/** Слоты и ПКМ: anchor -> glowstone -> totem, дальше автоматически при зажатии. */
public final class AnchorMacroModule extends CombatModule {
    public AnchorMacroModule(Category category) {
        super("Anchor Macro", "Anchor -> glowstone -> totem macro", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(new SlotBindRow(LanguageManager.tr("combat.option.anchor_slot", "Anchor slot"),
                () -> options.clamped("anchorSlot", 0, 0, 9),
                v -> options.setInt("anchorSlot", v)));
        rows.add(new SlotBindRow(LanguageManager.tr("combat.option.glowstone_slot", "Glowstone slot"),
                () -> options.clamped("glowstoneSlot", 0, 0, 9),
                v -> options.setInt("glowstoneSlot", v)));
        rows.add(new SlotBindRow(LanguageManager.tr("combat.option.totem_slot", "Totem slot"),
                () -> options.clamped("totemSlot", 0, 0, 9),
                v -> options.setInt("totemSlot", v)));
        return rows;
    }

    private int step;
    private long stepDeadline;
    private int originalSlot;
    private boolean running;

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready() || !macroHeld() || !gateOpen()) {
            step = 0;
            running = false;
            return;
        }
        if (!running) {
            if (!macroJustPressed()) {
                return;
            }
            start();
            return;
        }
        doStep();
    }

    private void start() {
        originalSlot = CombatApi.mainHandSlot();
        step = 0;
        running = true;
        stepDeadline = System.currentTimeMillis() + 150;
        markAction();
    }

    private void doStep() {
        int slot = switch (step) {
            case 0 -> options.clamped("anchorSlot", 0, 0, 9);
            case 1 -> options.clamped("glowstoneSlot", 0, 0, 9);
            case 2 -> options.clamped("totemSlot", 0, 0, 9);
            default -> -1;
        };
        if (slot <= 0 || slot > 9) {
            step = 0;
            running = false;
            return;
        }
        CombatApi.swapToSlot(slot);
        CombatApi.rightClick();
        step++;
        stepDeadline = System.currentTimeMillis() + 150;
        markAction();
        if (step == 3) {
            finish();
        }
    }

    @Override
    public void onDisable() {
        step = 0;
        running = false;
    }

    @Override
    public void afterTick(Minecraft client) {
        if (!macroHeld() && running) {
            finish();
        }
    }

    private void finish() {
        if (originalSlot >= 1 && originalSlot <= 9) {
            CombatApi.swapToSlot(originalSlot);
        }
        running = false;
        step = 0;
    }
}
