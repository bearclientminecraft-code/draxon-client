package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Auto Hit Crystal: сразу после вашего попадания по противнику детонирует
 * кристалл рядом с ним — так урон кристалла складывается с ударом.
 *
 * <p>Если готового кристалла нет, модуль (по настройке) ставит его на ближайшее
 * основание и подрывает на следующем шаге.</p>
 */
public final class AutoHitCrystalModule extends CombatModule {
    private float lastScale = 1f;
    private Player target;
    private int pendingTicks;

    public AutoHitCrystalModule(Category category) {
        super("Auto Hit Crystal", "Detonate crystals on your hit", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(toggleRow("placeIfMissing", true, "combat.option.place_if_missing"));
        rows.add(intSlider("range", 45, 20, 60, 5, "", "combat.option.range"));
        rows.add(intSlider("minDamage", 4, 0, 20, 1, "", "combat.option.min_damage"));
        rows.add(toggleRow("limitToItems", false, "combat.option.limit_to_items"));
        addSlotRow(rows, "combat.option.crystal_slot");
        return rows;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready()) {
            return;
        }
        float scale = client.player.getAttackStrengthScale(0.5f);
        boolean hit = lastScale > 0.85f && scale < 0.3f;
        lastScale = scale;
        if (hit && pendingTicks == 0 && macroHeld()) {
            if (options.getBoolean("limitToItems", false) && !holdingBoundSlot()) {
                return;
            }
            Player hovered = client.crosshairPickEntity instanceof Player player && player != client.player
                    ? player : CombatApi.nearestEnemy(options.clamped("range", 45, 20, 60) / 10.0);
            if (hovered != null && hovered.isAlive()) {
                target = hovered;
                pendingTicks = 1;
            }
        }
        if (pendingTicks > 0) {
            pendingTicks--;
            if (pendingTicks == 0) {
                detonate(client);
            }
        }
    }

    private void detonate(Minecraft client) {
        if (target == null || !target.isAlive() || !gateOpen()) {
            return;
        }
        float minDamage = options.clamped("minDamage", 4, 0, 20);
        EndCrystal crystal = ExplosiveApi.crystalNear(target, 4.5);
        if (crystal != null && CombatApi.crystalDamage(target, crystal.position()) >= minDamage) {
            aim(crystal, "aimSpeed", 50);
            ExplosiveApi.detonate(crystal);
            markAction();
            return;
        }
        if (options.getBoolean("placeIfMissing", true)) {
            net.minecraft.core.BlockPos base = ExplosiveApi.bestCrystalBase(target, 4.5, 0f, 20f);
            if (base != null) {
                aim(net.minecraft.world.phys.Vec3.atCenterOf(base.above()), "aimSpeed", 50);
                ExplosiveApi.placeCrystal(base);
                markAction();
                pendingTicks = 2;
            }
        }
    }
}
