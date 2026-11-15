package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * ShieldDisabler: при атаке на игрока с поднятым щитом меняет предмет на топор,
 * бьёт им и возвращает исходный слот горячей панели.
 *
 * <p>Swap Delay — пауза между свапом и ударом (в тиках), Swap Back Delay — пауза
 * перед возвратом слота. Double Click наносит второй удар сразу после снятия щита.</p>
 */
public final class ShieldBreakerModule extends CombatModule {
    private enum Stage { IDLE, SWAP, ATTACK, RETURN }

    private Stage stage = Stage.IDLE;
    private int ticks;
    private int originalSlot;
    private LivingEntity target;
    private boolean doublePending;

    public ShieldBreakerModule(Category category) {
        super("ShieldDisabler", "Axe swap for shield breaks", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(intSlider("swapDelay", 1, 0, 10, 1, " t", "combat.option.swap_delay"));
        rows.add(intSlider("swapBackDelay", 2, 0, 10, 1, " t", "combat.option.swap_back_delay"));
        rows.add(toggleRow("doubleClick", true, "combat.option.double_click"));
        rows.add(toggleRow("limitToItems", false, "combat.option.limit_to_items"));
        rows.add(toggleRow("requireAttack", true, "combat.option.require_mouse"));
        addSlotRow(rows, "combat.option.axe_slot");
        return rows;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready() || !macroHeld()) {
            stage = Stage.IDLE;
            return;
        }
        if (options.getBoolean("limitToItems", false) && !holdingBoundSlot()) {
            return;
        }
        switch (stage) {
            case IDLE -> tryStart(client);
            case SWAP -> {
                if (--ticks <= 0) {
                    if (target != null && target.isAlive()) {
                        CombatApi.attack(target);
                    }
                    doublePending = options.getBoolean("doubleClick", true);
                    stage = Stage.ATTACK;
                    ticks = 1;
                }
            }
            case ATTACK -> {
                if (--ticks <= 0) {
                    if (doublePending && target != null && target.isAlive()) {
                        doublePending = false;
                        CombatApi.attack(target);
                        ticks = 1;
                        return;
                    }
                    swapBack(client);
                }
            }
            case RETURN -> {
                if (--ticks <= 0) {
                    swapBack(client);
                }
            }
        }
    }

    private void tryStart(Minecraft client) {
        if (options.getBoolean("requireAttack", true) && !client.options.keyAttack.isDown()) {
            return;
        }
        if (!(client.crosshairPickEntity instanceof Player hovered) || hovered == client.player) {
            return;
        }
        if (!hovered.isBlocking()) {
            return;
        }
        int axeSlot = options.clamped("slot", 0, 0, 9);
        int slot = axeSlot > 0 ? axeSlot : CombatApi.hotbarSlot(CombatApi::isAxe);
        if (slot <= 0) {
            return;
        }
        originalSlot = CombatApi.mainHandSlot();
        if (!CombatApi.swapToSlot(slot)) {
            return;
        }
        target = hovered;
        stage = Stage.SWAP;
        ticks = Math.max(1, options.clamped("swapDelay", 1, 0, 10));
        markAction();
    }

    private void swapBack(Minecraft client) {
        if (originalSlot >= 1 && originalSlot <= 9) {
            CombatApi.swapToSlot(originalSlot);
        }
        target = null;
        stage = Stage.IDLE;
        markAction();
    }

    @Override
    protected void onDisable() {
        stage = Stage.IDLE;
        target = null;
    }
}
