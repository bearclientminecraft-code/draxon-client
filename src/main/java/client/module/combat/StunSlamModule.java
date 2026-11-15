package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Stun Slam: при атаке на игрока со щитом бьёт топором, ломая щит, и сразу
 * добивает mace'ом. Отдельный модуль повторяет цепочку Stun Slam из HitSwap и
 * AutoMace, но запускается самостоятельно по Macro Key.
 */
public final class StunSlamModule extends CombatModule {
    private enum Stage { IDLE, AXE, MACE, RETURN }

    private Stage stage = Stage.IDLE;
    private int ticks;
    private int originalSlot;
    private Player target;

    public StunSlamModule(Category category) {
        super("Stun Slam", "Axe into mace combo", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(intSlider("chance", 50, 0, 100, 5, "%", "combat.option.stun_chance"));
        rows.add(intSlider("swapDelay", 1, 0, 6, 1, " t", "combat.option.swap_delay"));
        rows.add(toggleRow("requireShield", true, "combat.option.require_shield"));
        addSlotRow(rows, "combat.option.axe_slot");
        return rows;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready() || !macroHeld()) {
            stage = Stage.IDLE;
            return;
        }
        if (stage == Stage.IDLE) {
            tryStart(client);
            return;
        }
        if (--ticks > 0) {
            return;
        }
        switch (stage) {
            case AXE -> {
                if (target != null && target.isAlive()) {
                    CombatApi.attack(target);
                }
                int mace = maceSlot();
                if (mace > 0 && CombatApi.swapToSlot(mace)) {
                    stage = Stage.MACE;
                    ticks = Math.max(1, options.clamped("swapDelay", 1, 0, 6));
                } else {
                    stage = Stage.RETURN;
                    ticks = 1;
                }
            }
            case MACE -> {
                if (target != null && target.isAlive()) {
                    CombatApi.attack(target);
                }
                stage = Stage.RETURN;
                ticks = 2;
            }
            case RETURN -> {
                if (originalSlot >= 1) {
                    CombatApi.swapToSlot(originalSlot);
                }
                stage = Stage.IDLE;
                target = null;
            }
            default -> {
            }
        }
    }

    private void tryStart(Minecraft client) {
        if (!(client.crosshairPickEntity instanceof Player hovered) || hovered == client.player
                || !hovered.isAlive()) {
            return;
        }
        if (options.getBoolean("requireShield", true) && !hovered.isBlocking()) {
            return;
        }
        int chance = options.clamped("chance", 50, 0, 100);
        if (chance < 100 && Math.random() * 100 >= chance) {
            return;
        }
        int axe = CombatApi.hotbarSlot(CombatApi::isAxe);
        if (axe <= 0 || maceSlot() <= 0) {
            return;
        }
        originalSlot = CombatApi.mainHandSlot();
        if (!CombatApi.swapToSlot(axe)) {
            return;
        }
        target = hovered;
        stage = Stage.AXE;
        ticks = Math.max(1, options.clamped("swapDelay", 1, 0, 6));
        markAction();
    }

    private int maceSlot() {
        int bound = boundSlot();
        if (bound > 0 && CombatApi.player().getInventory().getItem(bound - 1).is(Items.MACE)) {
            return bound;
        }
        // Отдельный слот mace: boundSlot занят топором, поэтому ищем по инвентарю.
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = CombatApi.player().getInventory().getItem(slot);
            if (stack.is(Items.MACE)) {
                return slot + 1;
            }
        }
        return -1;
    }

    @Override
    protected void onDisable() {
        stage = Stage.IDLE;
        target = null;
    }
}
