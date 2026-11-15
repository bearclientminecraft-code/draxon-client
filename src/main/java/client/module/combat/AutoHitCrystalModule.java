package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

import java.util.List;

/**
 * Auto Hit Crystal — простой макрос: держит слот кристалла и по нажатию правой
 * кнопки мыши сам нажимает левую.
 *
 * <p>Удар уходит по кристаллу в крестике (или ближайшему кристаллу рядом), иначе
 * по сущности под прицелом. Задержка между автокликами — общий Delay ± Random
 * Range, чтобы не долбить каждый тик.</p>
 */
public final class AutoHitCrystalModule extends CombatModule {
    public AutoHitCrystalModule(Category category) {
        super("Auto Hit Crystal", "Crystal slot and auto left click", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        addSlotRow(rows, "combat.option.crystal_slot");
        rows.add(toggleRow("requireRightClick", true, "combat.option.require_right_click"));
        return rows;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready() || !macroHeld() || !gateOpen()) {
            return;
        }
        boolean rightClick = client.options.keyUse.isDown();
        if (options.getBoolean("requireRightClick", true) && !rightClick) {
            return;
        }
        int slot = boundSlot();
        if (slot > 0 && CombatApi.mainHandSlot() != slot && !CombatApi.swapToSlot(slot)) {
            return;
        }
        Entity target = crystalTarget(client);
        if (target != null) {
            CombatApi.attack(target);
            markAction();
        }
    }

    /** Кристалл в крестике, ближайший кристалл рядом или сущность под прицелом. */
    private Entity crystalTarget(Minecraft client) {
        if (client.crosshairPickEntity instanceof EndCrystal crystal && crystal.isAlive()) {
            return crystal;
        }
        if (client.crosshairPickEntity instanceof net.minecraft.world.entity.LivingEntity living
                && living != client.player && living.isAlive()) {
            return living;
        }
        EndCrystal nearest = null;
        double best = 5.0;
        for (EndCrystal crystal : client.level.getEntitiesOfClass(EndCrystal.class,
                client.player.getBoundingBox().inflate(5.0), EndCrystal::isAlive)) {
            double distance = CombatApi.distanceTo(crystal);
            if (distance < best) {
                best = distance;
                nearest = crystal;
            }
        }
        return nearest;
    }
}
