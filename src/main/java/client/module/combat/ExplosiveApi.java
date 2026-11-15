package client.module.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Кристаллы и respawn anchor: поиск места, установка, зарядка и детонация. */
public final class ExplosiveApi {
    private ExplosiveApi() {
    }

    /** Основания под кристалл вокруг цели: обсидиан/бедрок с воздухом сверху. */
    public static List<BlockPos> crystalBases(Player target, double maxDistance) {
        List<BlockPos> bases = new ArrayList<>();
        if (target == null || CombatApi.player() == null) {
            return bases;
        }
        BlockPos center = target.blockPosition();
        Vec3 eye = CombatApi.player().getEyePosition();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos base = center.offset(dx, dy, dz);
                    if (!CombatApi.isSolidSupport(CombatApi.mc().level.getBlockState(base))) {
                        continue;
                    }
                    if (!CombatApi.isAir(base.above())) {
                        continue;
                    }
                    if (eye.distanceTo(Vec3.atCenterOf(base)) > maxDistance) {
                        continue;
                    }
                    bases.add(base);
                }
            }
        }
        return bases;
    }

    /** Лучшее основание: максимум урона по цели при минимуме урона по себе. */
    public static BlockPos bestCrystalBase(Player target, double maxDistance, float minEfficiency,
                                           float maxSelfDamage) {
        BlockPos best = null;
        float bestScore = -1f;
        for (BlockPos base : crystalBases(target, maxDistance)) {
            Vec3 center = Vec3.atCenterOf(base.above());
            float damage = CombatApi.crystalDamage(target, center);
            if (damage <= 0f) {
                continue;
            }
            float efficiency = Math.min(100f, damage / 12f * 100f);
            if (efficiency < minEfficiency) {
                continue;
            }
            float self = CombatApi.selfDamage(center);
            if (self > maxSelfDamage) {
                continue;
            }
            float score = damage - self * 1.5f;
            if (score > bestScore) {
                bestScore = score;
                best = base;
            }
        }
        return best;
    }

    /** Готовый кристалл, который можно детонировать рядом с целью. */
    public static EndCrystal crystalNear(Player target, double radius) {
        if (target == null) {
            return null;
        }
        List<EndCrystal> crystals = CombatApi.mc().level.getEntitiesOfClass(EndCrystal.class,
                target.getBoundingBox().inflate(radius), EndCrystal::isAlive);
        EndCrystal best = null;
        float bestScore = -1f;
        for (EndCrystal crystal : crystals) {
            float damage = CombatApi.crystalDamage(target, crystal.position());
            if (damage <= 0f) {
                continue;
            }
            if (CombatApi.selfDamage(crystal.position()) > 12f) {
                continue;
            }
            if (damage > bestScore) {
                bestScore = damage;
                best = crystal;
            }
        }
        return best;
    }

    /** Ставит кристалл на основание: нужен предмет в руке и задержка у вызывающего. */
    public static boolean placeCrystal(BlockPos base) {
        int slot = CombatApi.hotbarSlot(Items.END_CRYSTAL);
        if (slot < 0) {
            return false;
        }
        if (!CombatApi.swapToSlot(slot)) {
            return false;
        }
        return CombatApi.useOn(base, Direction.UP);
    }

    public static boolean detonate(EndCrystal crystal) {
        return CombatApi.attack(crystal);
    }

    /** Ставит обсидиан на ближайший твёрдый блок у цели (Auto Obsidian). */
    public static boolean placeObsidian(Player target) {
        int slot = CombatApi.hotbarSlot(Items.OBSIDIAN);
        if (slot < 0) {
            return false;
        }
        if (!CombatApi.swapToSlot(slot)) {
            return false;
        }
        if (!(CombatApi.mc().hitResult instanceof net.minecraft.world.phys.BlockHitResult hit)) {
            return false;
        }
        return CombatApi.useOn(hit.getBlockPos(), hit.getDirection());
    }

    // ------------------------------------------------------------- anchor

    /** Основание под якорь: твёрдый блок с воздухом сверху. */
    public static BlockPos anchorBase(Player target, double maxDistance) {
        List<BlockPos> bases = new ArrayList<>();
        if (target == null) {
            return null;
        }
        BlockPos center = target.blockPosition();
        Vec3 eye = CombatApi.player() == null ? Vec3.ZERO : CombatApi.player().getEyePosition();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos base = center.offset(dx, dy, dz);
                    var state = CombatApi.mc().level.getBlockState(base);
                    if (state.isAir() || !CombatApi.isAir(base.above())) {
                        continue;
                    }
                    if (state.getBlock() == Blocks.RESPAWN_ANCHOR) {
                        continue;
                    }
                    if (eye.distanceTo(Vec3.atCenterOf(base)) > maxDistance) {
                        continue;
                    }
                    bases.add(base);
                }
            }
        }
        if (bases.isEmpty()) {
            return null;
        }
        bases.sort(java.util.Comparator.comparingDouble(base -> CombatApi.distanceToVec(Vec3.atCenterOf(base))));
        return bases.get(0);
    }

    public static boolean placeAnchor(BlockPos base) {
        int slot = CombatApi.hotbarSlot(Items.RESPAWN_ANCHOR);
        if (slot < 0 || !CombatApi.swapToSlot(slot)) {
            return false;
        }
        return CombatApi.useOn(base, Direction.UP);
    }

    public static boolean chargeAnchor(BlockPos anchorPos) {
        int slot = CombatApi.hotbarSlot(Items.GLOWSTONE);
        if (slot < 0 || !CombatApi.swapToSlot(slot)) {
            return false;
        }
        return CombatApi.useOn(anchorPos, Direction.UP);
    }

    /** Детонация заряженного якоря: правый клик по блоку. */
    public static boolean detonateAnchor(BlockPos anchorPos) {
        return CombatApi.useOn(anchorPos, Direction.UP);
    }

    public static List<BlockPos> anchorsNear(Player target, double radius) {
        List<BlockPos> result = new ArrayList<>();
        if (target == null) {
            return result;
        }
        BlockPos center = target.blockPosition();
        int r = (int) Math.ceil(radius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (CombatApi.mc().level.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR) {
                        result.add(pos);
                    }
                }
            }
        }
        return result;
    }

    /** Ищет блок-покрытие (glowstone) между игроком и центром взрыва. */
    public static BlockPos safeCover(Vec3 center) {
        if (CombatApi.player() == null) {
            return null;
        }
        BlockPos origin = BlockPos.containing(center);
        Vec3 eye = CombatApi.player().getEyePosition();
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos base = origin.offset(dx, dy, dz);
                    var state = CombatApi.mc().level.getBlockState(base);
                    if (state.isAir() || !CombatApi.isAir(base.above())) {
                        continue;
                    }
                    Vec3 spot = Vec3.atCenterOf(base.above());
                    double distanceToLine = distanceToSegment(spot, eye, center);
                    if (distanceToLine > 1.2) {
                        continue;
                    }
                    double score = spot.distanceTo(eye);
                    if (score < bestScore) {
                        bestScore = score;
                        best = base;
                    }
                }
            }
        }
        return best;
    }

    /** Ставит блок покрытия (glowstone) на найденное основание. */
    public static boolean placeCover(BlockPos base) {
        int slot = CombatApi.hotbarSlot(Items.GLOWSTONE);
        if (slot < 0 || !CombatApi.swapToSlot(slot)) {
            return false;
        }
        return CombatApi.useOn(base, Direction.UP);
    }

    /** Предмет из whitelist'а (слот 1..9) для удержания в руке при взрыве. */
    public static boolean holdExplosionItem(int slot) {
        if (slot <= 0 || slot > 9) {
            return false;
        }
        ItemStack stack = CombatApi.player() == null ? ItemStack.EMPTY
                : CombatApi.player().getInventory().getItem(slot - 1);
        return !stack.isEmpty() && CombatApi.swapToSlot(slot);
    }

    public static Predicate<ItemStack> notEmpty() {
        return stack -> !stack.isEmpty();
    }

    private static double distanceToSegment(Vec3 point, Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        double lengthSquared = delta.lengthSqr();
        if (lengthSquared < 0.0001) {
            return point.distanceTo(from);
        }
        double t = Math.max(0, Math.min(1, point.subtract(from).dot(delta) / lengthSquared));
        return point.distanceTo(from.add(delta.scale(t)));
    }
}
