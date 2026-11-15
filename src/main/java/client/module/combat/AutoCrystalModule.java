package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import client.notification.NotificationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Auto Crystal (CrystalAura, Auto mode): ищет цель, ставит энд-кристаллы на
 * обсидиан/бедрок рядом с ней и детонирует их с задержкой.
 *
 * <p>Режимы: None (только выбор цели), Rapid fire (ставит и сразу детонирует
 * кристаллы с задержкой Delay), Predict (детонирует кристалл на следующем тике
 * после установки, чтобы не ждать полного появления).</p>
 */
public final class AutoCrystalModule extends CombatModule {
    private Player target;
    private String lastTargetName = "";

    public AutoCrystalModule(Category category) {
        super("Auto Crystal", "CrystalAura auto mode", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(choiceRow("mode", "rapid", "crystal.mode.", "none", "rapid", "predict"));
        rows.add(choiceRow("target", "distance", "target.mode.", "distance", "yaw", "armor", "threat", "health"));
        rows.add(intSlider("range", 45, 20, 80, 5, "", "combat.option.range"));
        rows.add(intSlider("angle", 180, 30, 360, 10, "°", "combat.option.max_angle"));
        rows.add(intSlider("aimSpeed", 25, 5, 90, 5, "", "combat.option.aim_speed"));
        rows.add(intSlider("minEfficiency", 25, 0, 100, 5, "%", "combat.option.min_efficiency"));
        rows.add(intSlider("maxSelfDamage", 8, 0, 20, 1, "", "combat.option.max_self_damage"));
        rows.add(toggleRow("autoObsidian", false, "combat.option.auto_obsidian"));
        rows.add(toggleRow("antiSuicide", true, "combat.option.anti_suicide"));
        rows.add(toggleRow("showTarget", true, "combat.option.show_target"));
        addSlotRow(rows, "combat.option.crystal_slot");
        return rows;
    }

    private double range() {
        return options.clamped("range", 45, 20, 80) / 10.0;
    }

    private String mode() {
        return options.get("mode", "rapid");
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready() || !macroHeld()) {
            return;
        }
        // Своя щит-блокировка: не подрываем себя, пока держим щит.
        if (client.player.isBlocking()) {
            return;
        }
        acquireTarget();
        if (target == null) {
            return;
        }
        if ("none".equals(mode())) {
            return;
        }
        if (!gateOpen()) {
            return;
        }

        float minEfficiency = options.clamped("minEfficiency", 25, 0, 100);
        float maxSelf = options.clamped("maxSelfDamage", 8, 0, 20);
        boolean antiSuicide = options.getBoolean("antiSuicide", true);

        // 1. Детонируем готовый кристалл рядом с целью, если он выгоден.
        EndCrystal existing = ExplosiveApi.crystalNear(target, 4.0);
        if (existing != null) {
            float damage = CombatApi.crystalDamage(target, existing.position());
            float self = CombatApi.selfDamage(existing.position());
            if (damage / 12f * 100f >= minEfficiency && (!antiSuicide || self < maxSelf)) {
                aimAndSwing(existing);
                ExplosiveApi.detonate(existing);
                markAction();
                return;
            }
        }

        // 2. Ставим кристалл на лучшее основание.
        BlockPos base = ExplosiveApi.bestCrystalBase(target, range(), minEfficiency,
                antiSuicide ? maxSelf : 40f);
        if (base == null) {
            if (options.getBoolean("autoObsidian", false)) {
                ExplosiveApi.placeObsidian(target);
                markAction();
            }
            return;
        }
        aim(net.minecraft.world.phys.Vec3.atCenterOf(base.above()), "aimSpeed", 25);
        if (ExplosiveApi.placeCrystal(base)) {
            markAction();
        }
    }

    private void aimAndSwing(EndCrystal crystal) {
        aim(crystal, "aimSpeed", 25);
    }

    private void acquireTarget() {
        double angle = options.clamped("angle", 180, 30, 360);
        Player candidate = CombatApi.bestEnemy(range(), options.get("target", "distance"));
        if (candidate != null) {
            float yaw = CombatApi.yawTo(candidate.position().add(0, candidate.getBbHeight() * 0.5, 0));
            if (Math.abs(CombatApi.angleDelta(yaw - CombatApi.player().getYRot())) > angle) {
                candidate = null;
            }
        }
        if (candidate != target) {
            target = candidate;
            if (target != null && options.getBoolean("showTarget", true)
                    && !target.getName().getString().equals(lastTargetName)) {
                lastTargetName = target.getName().getString();
                NotificationManager.push(target.getName().getString(), client.notification.Notification.Type.INFO);
            }
        }
    }

    public boolean hasTarget() {
        return target != null && target.isAlive();
    }
}
