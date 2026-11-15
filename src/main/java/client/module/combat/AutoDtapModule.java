package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Auto DTAP (One Tap / Double Tap): после вашего попадания по противнику ставит
 * обсидиан рядом с ним, затем кристалл и детонирует его.
 *
 * <p>Режим Double Tap ставит и подрывает второй кристалл, чтобы добить цель.
 * Нажатие Macro Key не требуется — модуль реагирует на факт попадания.</p>
 */
public final class AutoDtapModule extends CombatModule {
    private enum Stage { IDLE, OBSIDIAN, CRYSTAL, DETONATE, SECOND }

    private Stage stage = Stage.IDLE;
    private int ticks;
    private Player target;
    private BlockPos base;
    private float lastScale = 1f;

    public AutoDtapModule(Category category) {
        super("Auto DTAP", "One/double tap after your hit", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(choiceRow("mode", "double", "tap.mode.", "one", "double"));
        rows.add(toggleRow("placeObsidian", true, "combat.option.auto_obsidian"));
        rows.add(intSlider("range", 45, 20, 60, 5, "", "combat.option.range"));
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
        if (hit && stage == Stage.IDLE) {
            start(client);
        }
        if (stage == Stage.IDLE || !gateOpen()) {
            return;
        }
        if (ticks > 0) {
            ticks--;
            return;
        }
        step(client);
    }

    private void start(Minecraft client) {
        if (!macroHeld()) {
            return;
        }
        if (options.getBoolean("limitToItems", false) && !holdingBoundSlot()) {
            return;
        }
        Player nearest = null;
        if (client.crosshairPickEntity instanceof Player hovered && hovered != client.player) {
            nearest = hovered;
        } else {
            nearest = CombatApi.nearestEnemy(options.clamped("range", 45, 20, 60) / 10.0);
        }
        if (nearest == null || !nearest.isAlive()) {
            return;
        }
        target = nearest;
        base = ExplosiveApi.bestCrystalBase(nearest, 4.5, 0f, 20f);
        stage = base == null && options.getBoolean("placeObsidian", true) ? Stage.OBSIDIAN : Stage.CRYSTAL;
        ticks = 1;
    }

    private void step(Minecraft client) {
        switch (stage) {
            case OBSIDIAN -> {
                if (ExplosiveApi.placeObsidian(target)) {
                    markAction();
                }
                base = ExplosiveApi.bestCrystalBase(target, 4.5, 0f, 20f);
                stage = Stage.CRYSTAL;
                ticks = 1;
            }
            case CRYSTAL -> {
                if (base == null) {
                    base = ExplosiveApi.bestCrystalBase(target, 4.5, 0f, 20f);
                }
                if (base == null) {
                    stage = Stage.IDLE;
                    return;
                }
                aim(Vec3.atCenterOf(base.above()), "aimSpeed", 45);
                if (ExplosiveApi.placeCrystal(base)) {
                    markAction();
                }
                stage = Stage.DETONATE;
                ticks = 2;
            }
            case DETONATE -> {
                EndCrystal crystal = ExplosiveApi.crystalNear(target, 5.0);
                if (crystal != null) {
                    ExplosiveApi.detonate(crystal);
                    markAction();
                }
                stage = "double".equals(options.get("mode", "double")) ? Stage.SECOND : Stage.IDLE;
                ticks = 2;
            }
            case SECOND -> {
                base = ExplosiveApi.bestCrystalBase(target, 4.5, 0f, 20f);
                if (base != null && ExplosiveApi.placeCrystal(base)) {
                    markAction();
                    EndCrystal crystal = ExplosiveApi.crystalNear(target, 5.0);
                    if (crystal != null) {
                        ExplosiveApi.detonate(crystal);
                    }
                }
                stage = Stage.IDLE;
                base = null;
            }
            default -> stage = Stage.IDLE;
        }
    }

    @Override
    protected void onDisable() {
        stage = Stage.IDLE;
        base = null;
    }
}
