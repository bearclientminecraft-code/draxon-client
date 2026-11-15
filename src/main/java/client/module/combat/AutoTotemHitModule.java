package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * Auto Totem Hit: наносит удар сразу после срабатывания тотема.
 *
 * <p>Момент срабатывания определяется по исчезновению тотема из offhand при
 * полученном уроне. Удар идёт по цели в крестике, иначе по ближайшему
 * противнику в радиусе.</p>
 */
public final class AutoTotemHitModule extends CombatModule {
    private boolean hadTotem = true;
    private long poppedAt;

    public AutoTotemHitModule(Category category) {
        super("Auto Totem Hit", "Hit right after totem pop", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(intSlider("window", 250, 50, 1000, 50, " ms", "combat.option.window"));
        rows.add(intSlider("range", 40, 10, 60, 5, "", "combat.option.range"));
        addSlotRow(rows, "combat.option.weapon_slot");
        return rows;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready()) {
            return;
        }
        boolean hasTotem = CombatApi.hasTotemInOffhand();
        if (hadTotem && !hasTotem && client.player.hurtTime > 0) {
            poppedAt = System.currentTimeMillis();
        }
        hadTotem = hasTotem;
        if (poppedAt == 0 || !macroHeld() || !gateOpen()) {
            return;
        }
        long window = options.clamped("window", 250, 50, 1000);
        if (System.currentTimeMillis() - poppedAt > window) {
            poppedAt = 0;
            return;
        }
        LivingEntity target = client.crosshairPickEntity instanceof LivingEntity hovered && hovered != client.player
                && hovered.isAlive() ? hovered
                : CombatApi.nearestEnemy(options.clamped("range", 40, 10, 60) / 10.0);
        if (target == null) {
            return;
        }
        int slot = boundSlot();
        if (slot > 0 && CombatApi.mainHandSlot() != slot) {
            CombatApi.swapToSlot(slot);
        }
        if (CombatApi.attack(target)) {
            poppedAt = 0;
            markAction();
        }
    }
}
