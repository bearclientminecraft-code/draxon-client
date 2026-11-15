package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Safe Anchor: ставит glowstone-покрытие между вами и заряженным/поставленным
 * якорем, чтобы уменьшить урон от собственного взрыва.
 *
 * <p>Это самостоятельная версия подфункции AutoAnchor: работает с чужими и
 * своими якорями рядом с целью, не запускает зарядку и детонацию сама.</p>
 */
public final class SafeAnchorModule extends CombatModule {
    public SafeAnchorModule(Category category) {
        super("Safe Anchor", "Glowstone cover between you and anchor", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(intSlider("range", 40, 10, 60, 5, "", "combat.option.range"));
        rows.add(toggleRow("aimAssist", true, "combat.option.aim_assist"));
        rows.add(intSlider("aimSpeed", 30, 5, 90, 5, "", "combat.option.aim_speed"));
        addSlotRow(rows, "combat.option.glowstone_slot");
        return rows;
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready() || !macroHeld() || !gateOpen()) {
            return;
        }
        Player target = CombatApi.nearestEnemy(options.clamped("range", 40, 10, 60) / 10.0);
        if (target == null) {
            return;
        }
        List<BlockPos> anchors = ExplosiveApi.anchorsNear(target, 4.0);
        if (anchors.isEmpty()) {
            // Якорь ещё не поставлен — покрытие ставим относительно самой цели.
            anchors = List.of(target.blockPosition());
        }
        for (BlockPos anchor : anchors) {
            Vec3 center = Vec3.atCenterOf(anchor);
            if (!CombatApi.isPathBlocked(CombatApi.player().getEyePosition(), center)) {
                continue;
            }
            BlockPos cover = ExplosiveApi.safeCover(center);
            if (cover == null) {
                continue;
            }
            if (options.getBoolean("aimAssist", true)) {
                aim(Vec3.atCenterOf(cover.above()), "aimSpeed", 30);
            }
            if (ExplosiveApi.placeCover(cover)) {
                markAction();
            }
            return;
        }
    }
}
