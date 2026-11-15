package client.module.combat;

import client.category.Category;
import client.gui.components.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Anchor Macro (AutoAnchor): авто-постановка respawn anchor, зарядка glowstone
 * и детонация.
 *
 * <p>Режим «On bind» запускает всю цепочку с Macro Key, «On place» подхватывает
 * якорь, который игрок поставил вручную. Double Anchor сразу начинает вторую
 * цепочку, Safe Anchor ставит glowstone-покрытие между игроком и якорем.</p>
 */
public final class AnchorMacroModule extends CombatModule {
    private enum Stage { IDLE, PLACE, CHARGE, DETONATE }

    private Stage stage = Stage.IDLE;
    private BlockPos anchorPos;
    private Player target;
    private final Set<BlockPos> knownAnchors = new HashSet<>();

    public AnchorMacroModule(Category category) {
        super("Anchor Macro", "AutoAnchor sequence", category);
    }

    @Override
    public List<Component> createSettingRows() {
        List<Component> rows = baseRows();
        rows.add(choiceRow("mode", "bind", "anchor.mode.", "bind", "place"));
        rows.add(toggleRow("doubleAnchor", false, "combat.option.double_anchor"));
        rows.add(choiceRow("safeAnchor", "off", "option.", "off", "on"));
        rows.add(toggleRow("aimAssist", true, "combat.option.aim_assist"));
        rows.add(intSlider("aimSpeed", 30, 5, 90, 5, "", "combat.option.aim_speed"));
        rows.add(intSlider("range", 45, 20, 60, 5, "", "combat.option.range"));
        addSlotRow(rows, "combat.option.anchor_slot");
        rows.add(new client.gui.components.SlotBindRow(t("combat.option.explosion_item", "Explosion item"),
                () -> options.clamped("explosionSlot", 0, 0, 9),
                value -> options.setInt("explosionSlot", value)));
        return rows;
    }

    private double range() {
        return options.clamped("range", 45, 20, 60) / 10.0;
    }

    private boolean safeAnchor() {
        return "on".equals(options.get("safeAnchor", "off"));
    }

    @Override
    public void onTick(Minecraft client) {
        if (!CombatApi.ready()) {
            return;
        }
        Player nearest = CombatApi.nearestEnemy(range());
        if (nearest == null) {
            stage = Stage.IDLE;
            return;
        }
        target = nearest;

        if ("place".equals(options.get("mode", "bind"))) {
            handleManualPlacement();
            return;
        }
        if (!macroHeld()) {
            stage = Stage.IDLE;
            return;
        }
        if (stage == Stage.IDLE && macroJustPressed()) {
            stage = Stage.PLACE;
        }
        if (stage == Stage.IDLE || !gateOpen()) {
            return;
        }
        step();
    }

    private void step() {
        switch (stage) {
            case PLACE -> {
                if (safeAnchor()) {
                    BlockPos cover = ExplosiveApi.safeCover(target.position().add(0, 1, 0));
                    if (cover != null && ExplosiveApi.placeCover(cover)) {
                        markAction();
                        return;
                    }
                }
                BlockPos base = ExplosiveApi.anchorBase(target, range());
                if (base == null) {
                    stage = Stage.IDLE;
                    return;
                }
                if (options.getBoolean("aimAssist", true)) {
                    aim(Vec3.atCenterOf(base.above()), "aimSpeed", 30);
                }
                if (ExplosiveApi.placeAnchor(base)) {
                    anchorPos = base.above();
                    knownAnchors.add(anchorPos);
                    stage = Stage.CHARGE;
                }
                markAction();
            }
            case CHARGE -> {
                if (anchorPos == null) {
                    stage = Stage.IDLE;
                    return;
                }
                if (options.getBoolean("aimAssist", true)) {
                    aim(Vec3.atCenterOf(anchorPos), "aimSpeed", 30);
                }
                if (ExplosiveApi.chargeAnchor(anchorPos)) {
                    stage = Stage.DETONATE;
                }
                markAction();
            }
            case DETONATE -> {
                if (anchorPos == null) {
                    stage = Stage.IDLE;
                    return;
                }
                int whitelistSlot = options.clamped("explosionSlot", 0, 0, 9);
                if (whitelistSlot > 0) {
                    ExplosiveApi.holdExplosionItem(whitelistSlot);
                }
                ExplosiveApi.detonateAnchor(anchorPos);
                knownAnchors.remove(anchorPos);
                anchorPos = null;
                stage = options.getBoolean("doubleAnchor", false) ? Stage.PLACE : Stage.IDLE;
                markAction();
            }
            default -> {
            }
        }
    }

    /** Режим On place: замечает новый якорь рядом с целью и завершает цепочку. */
    private void handleManualPlacement() {
        List<BlockPos> anchors = ExplosiveApi.anchorsNear(target, range());
        for (BlockPos pos : anchors) {
            if (!knownAnchors.contains(pos)) {
                knownAnchors.add(pos);
                anchorPos = pos;
                stage = Stage.CHARGE;
            }
        }
        knownAnchors.retainAll(anchors);
        if (stage == Stage.IDLE || anchorPos == null || !gateOpen()) {
            return;
        }
        if (stage == Stage.CHARGE && ExplosiveApi.chargeAnchor(anchorPos)) {
            stage = Stage.DETONATE;
            markAction();
        } else if (stage == Stage.DETONATE) {
            ExplosiveApi.detonateAnchor(anchorPos);
            knownAnchors.remove(anchorPos);
            anchorPos = null;
            stage = Stage.IDLE;
            markAction();
        }
    }

    @Override
    protected void onDisable() {
        stage = Stage.IDLE;
        anchorPos = null;
    }
}
