package client.module.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Общий слой боевых операций: поиск целей, наведение, атака, смена слота,
 * установка блоков и приблизительная оценка урона взрыва.
 *
 * <p>Оценка урона намеренно упрощена: она нужна только для отсечения заведомо
 * невыгодных кристаллов/якорей (Min Efficiency, Anti-Suicide), а не для точного
 * расчёта как в серверной математике.</p>
 */
public final class CombatApi {
    public static final double CRYSTAL_RANGE = 4.5;

    private CombatApi() {
    }

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /** Клиент в мире и никакой экран не перехватывает ввод. */
    public static boolean ready() {
        Minecraft mc = mc();
        return mc.player != null && mc.level != null && mc.gameMode != null && mc.screen == null;
    }

    public static LocalPlayer player() {
        return mc().player;
    }

    public static double distanceTo(Entity entity) {
        return player().distanceTo(entity);
    }

    /** Расстояние от глаз игрока до точки. */
    public static double distanceToVec(Vec3 point) {
        return player() == null ? Double.MAX_VALUE : player().getEyePosition().distanceTo(point);
    }

    /** Живые игроки в радиусе, кроме себя. */
    public static List<Player> enemies(double range) {
        List<Player> result = new ArrayList<>();
        if (!ready()) {
            return result;
        }
        for (Player candidate : mc().level.players()) {
            if (candidate == player() || !candidate.isAlive() || candidate.isSpectator()) {
                continue;
            }
            if (distanceTo(candidate) <= range) {
                result.add(candidate);
            }
        }
        return result;
    }

    public static Player nearestEnemy(double range) {
        List<Player> list = enemies(range);
        list.sort(Comparator.comparingDouble(CombatApi::distanceTo));
        return list.isEmpty() ? null : list.get(0);
    }

    /** Цель по режиму приоритета: distance, health, armor, threat, yaw. */
    public static Player bestEnemy(double range, String mode) {
        List<Player> list = enemies(range);
        if (list.isEmpty()) {
            return null;
        }
        Comparator<Player> comparator = switch (mode == null ? "distance" : mode) {
            case "health" -> Comparator.comparingDouble(LivingEntity::getHealth);
            case "armor" -> Comparator.comparingInt(LivingEntity::getArmorValue);
            case "yaw" -> Comparator.comparingDouble(p -> Math.abs(angleDelta(
                    yawTo(p.position().add(0, p.getBbHeight() * 0.5, 0)) - player().getYRot())));
            case "threat" -> Comparator.comparingDouble(p -> -(p.getHealth() / Math.max(1.0, distanceTo(p))));
            default -> Comparator.comparingDouble(CombatApi::distanceTo);
        };
        list.sort(comparator);
        return list.get(0);
    }

    public static float yawTo(Vec3 target) {
        Vec3 eye = player().getEyePosition();
        double dx = target.x - eye.x;
        double dz = target.z - eye.z;
        return (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
    }

    public static float pitchTo(Vec3 target) {
        Vec3 eye = player().getEyePosition();
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        return (float) (-Math.toDegrees(Math.atan2(dy, horizontal)));
    }

    /** Плавно доворачивает камеру к точке (видимый поворот, без пакетной подмены). */
    public static void aimAt(Vec3 target, float speed) {
        if (player() == null) {
            return;
        }
        float targetYaw = yawTo(target);
        float targetPitch = pitchTo(target);
        float deltaYaw = wrapDegrees(targetYaw - player().getYRot());
        float deltaPitch = targetPitch - player().getXRot();
        float step = Math.max(0.5f, speed);
        player().setYRot(player().getYRot() + clamp(deltaYaw, -step, step));
        player().setXRot(clamp(player().getXRot() + clamp(deltaPitch, -step, step), -90f, 90f));
    }

    public static void aimAt(Entity entity, float speed) {
        if (entity != null) {
            aimAt(entity.position().add(0, entity.getBbHeight() * 0.5, 0), speed);
        }
    }

    /** Готовность обычной атаки (кулдаун оружия). */
    public static boolean attackReady() {
        return player() != null && player().getAttackStrengthScale(0.5f) >= 0.92f;
    }

    public static boolean attack(Entity target) {
        if (!ready() || target == null) {
            return false;
        }
        mc().gameMode.attack(player(), target);
        player().swing(InteractionHand.MAIN_HAND);
        return true;
    }

    /** Наведение крестика на сущность (для TriggerBot). */
    public static boolean crosshairOn(Entity entity) {
        return entity != null && mc().crosshairPickEntity == entity;
    }

    public static boolean shieldUp(LivingEntity entity) {
        return entity != null && entity.isBlocking();
    }

    // ---------------------------------------------------------------- слоты

    public static boolean hasTotemInOffhand() {
        return player() != null && player().getInventory().getItem(40).is(Items.TOTEM_OF_UNDYING);
    }

    public static int mainHandSlot() {
        return player() == null ? 0 : player().getInventory().getSelectedSlot() + 1;
    }

    public static ItemStack heldItem() {
        return player() == null ? ItemStack.EMPTY : player().getMainHandItem();
    }

    /** Ищет предмет на горячей панели; возвращает слот 1..9 или -1. */
    public static int hotbarSlot(Predicate<ItemStack> predicate) {
        if (player() == null) {
            return -1;
        }
        for (int slot = 0; slot < 9; slot++) {
            if (predicate.test(player().getInventory().getItem(slot))) {
                return slot + 1;
            }
        }
        return -1;
    }

    /** Ищет предмет в инвентаре; возвращает индекс 0..35 или -1. */
    public static int inventorySlot(Predicate<ItemStack> predicate) {
        if (player() == null) {
            return -1;
        }
        for (int slot = 0; slot < 36; slot++) {
            if (predicate.test(player().getInventory().getItem(slot))) {
                return slot;
            }
        }
        return -1;
    }

    public static int hotbarSlot(Item item) {
        return hotbarSlot(stack -> stack.is(item));
    }

    public static boolean swapToSlot(int slot1based) {
        if (!ready() || slot1based < 1 || slot1based > 9) {
            return false;
        }
        if (player().getInventory().getSelectedSlot() == slot1based - 1) {
            return true;
        }
        player().getInventory().setSelectedSlot(slot1based - 1);
        player().connection.send(new ServerboundSetCarriedItemPacket(slot1based - 1));
        return true;
    }

    public static int countTotems() {
        if (player() == null) {
            return 0;
        }
        int count = 0;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player().getInventory().getItem(slot);
            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /** Открывает инвентарь так же, как ванильная клавиша (с пакетом серверу). */
    public static void openInventory() {
        if (!ready() || player().containerMenu != player().inventoryMenu) {
            return;
        }
        player().connection.send(new ServerboundPlayerCommandPacket(player(),
                ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY));
        mc().setScreen(new InventoryScreen(player()));
    }

    public static boolean inventoryOpen() {
        return player() != null && player().containerMenu == player().inventoryMenu
                && mc().screen instanceof InventoryScreen;
    }

    /**
     * Перекладывает предмет из слота инвентаря (0..35) в offhand.
     *
     * <p>Клик идёт в меню инвентаря: его слоты 9..35 — основной инвентарь,
     * 36..44 — горячая панель, 45 — offhand. Кнопка 40 в SWAP-клике означает
     * обмен с offhand.</p>
     */
    public static boolean moveToOffhand(int inventoryIndex) {
        if (player() == null || player().containerMenu != player().inventoryMenu) {
            return false;
        }
        if (inventoryIndex < 0 || inventoryIndex > 35) {
            return false;
        }
        int menuSlot = inventoryIndex >= 9 ? inventoryIndex : inventoryIndex + 36;
        mc().gameMode.handleInventoryMouseClick(player().inventoryMenu.containerId,
                menuSlot, 40, ClickType.SWAP, player());
        return true;
    }

    /** Надевает нагрудник/элитры из горячей панели в слот брони (6 = chest). */
    public static boolean equipChestFromHotbar(int hotbarIndex) {
        if (player() == null || player().containerMenu != player().inventoryMenu) {
            return false;
        }
        if (hotbarIndex < 0 || hotbarIndex > 8) {
            return false;
        }
        mc().gameMode.handleInventoryMouseClick(player().inventoryMenu.containerId,
                6, hotbarIndex, ClickType.SWAP, player());
        return true;
    }

    // ---------------------------------------------------------------- блоки

    public static boolean useOn(BlockPos pos, Direction face) {
        if (!ready()) {
            return false;
        }
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false);
        return mc().gameMode.useItemOn(player(), InteractionHand.MAIN_HAND, hit).consumesAction();
    }

    public static boolean useItem() {
        return ready() && mc().gameMode.useItem(player(), InteractionHand.MAIN_HAND).consumesAction();
    }

    /**
     * Настоящая правая кнопка мыши «куда смотрю»: по блоку — useItemOn с попаданием
     * прицела, по сущности — interactAt/interact, иначе ПКМ по воздуху.
     *
     * <p>Именно этого не хватало макросам: {@link #useItem()} — это только ПКМ
     * по воздуху, поэтому якорь не ставился и светокамень не заряжал блок.</p>
     */
    public static boolean rightClick() {
        if (!ready()) {
            return false;
        }
        if (mc().hitResult instanceof BlockHitResult blockHit) {
            return mc().gameMode.useItemOn(player(), InteractionHand.MAIN_HAND, blockHit).consumesAction();
        }
        if (mc().hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHit) {
            return mc().gameMode.interactAt(player(), entityHit.getEntity(), entityHit,
                    InteractionHand.MAIN_HAND).consumesAction()
                    || mc().gameMode.interact(player(), entityHit.getEntity(),
                    InteractionHand.MAIN_HAND).consumesAction();
        }
        return mc().gameMode.useItem(player(), InteractionHand.MAIN_HAND).consumesAction();
    }

    /** Левая кнопка мыши по цели под прицелом (или по ближайшему кристаллу). */
    public static boolean leftClick(Entity target) {
        if (!ready()) {
            return false;
        }
        Entity real = target;
        if (real == null) {
            real = mc().crosshairPickEntity;
        }
        if (real != null && real.isAlive()) {
            return attack(real);
        }
        return false;
    }

    public static boolean interact(Entity entity) {
        return ready() && mc().gameMode.interact(player(), entity, InteractionHand.MAIN_HAND).consumesAction();
    }

    public static boolean isSolidSupport(BlockState state) {
        return state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.BEDROCK;
    }

    public static boolean isAir(BlockPos pos) {
        return mc().level.getBlockState(pos).isAir();
    }

    // ---------------------------------------------------------------- взрывы

    /** Приблизительный урон взрыва по сущности с учётом расстояния и перекрытий. */
    public static float explosionDamage(Vec3 center, LivingEntity target, float power) {
        Vec3 hit = target.position().add(0, target.getBbHeight() * 0.5, 0);
        double distance = center.distanceTo(hit);
        double reach = power * 2.0;
        if (distance > reach) {
            return 0f;
        }
        float raw = (float) (power * 12.0 * Math.exp(-distance / 4.0));
        if (isPathBlocked(center, hit)) {
            raw *= 0.35f;
        }
        float armor = target.getArmorValue();
        float reduction = Math.min(0.72f, armor * 0.04f);
        if (target.isBlocking()) {
            reduction = Math.min(0.95f, reduction + 0.35f);
        }
        return Math.max(0f, raw * (1f - reduction));
    }

    /** Есть ли непроходимый блок на отрезке между точками. */
    public static boolean isPathBlocked(Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 0.01) {
            return false;
        }
        for (double travelled = 0.25; travelled < length; travelled += 0.25) {
            Vec3 point = from.add(delta.scale(travelled / length));
            BlockPos pos = BlockPos.containing(point.x, point.y, point.z);
            if (!isAir(pos)) {
                return true;
            }
        }
        return false;
    }

    /** Кристаллы в радиусе от позиции. */
    public static List<EndCrystal> crystalsNear(BlockPos base, double radius) {
        AABB box = new AABB(base).inflate(radius);
        return mc().level.getEntitiesOfClass(EndCrystal.class, box, EndCrystal::isAlive);
    }

    /** Оценка урона по цели от кристалла в данной точке. */
    public static float crystalDamage(Player target, Vec3 center) {
        return explosionDamage(center, target, 6f);
    }

    /** Оценка урона по себе от кристалла/якоря. */
    public static float selfDamage(Vec3 center) {
        return explosionDamage(center, player(), 6f);
    }

    // ---------------------------------------------------------------- утилиты

    public static boolean isAxe(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.NETHERITE_AXE || item == Items.DIAMOND_AXE || item == Items.IRON_AXE
                || item == Items.STONE_AXE || item == Items.WOODEN_AXE || item == Items.GOLDEN_AXE;
    }

    public static boolean isSword(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.NETHERITE_SWORD || item == Items.DIAMOND_SWORD || item == Items.IRON_SWORD
                || item == Items.STONE_SWORD || item == Items.WOODEN_SWORD || item == Items.GOLDEN_SWORD;
    }

    public static boolean isMace(ItemStack stack) {
        return stack.is(Items.MACE);
    }

    public static boolean isChestplate(ItemStack stack) {
        return stack.getItem() == Items.NETHERITE_CHESTPLATE || stack.getItem() == Items.DIAMOND_CHESTPLATE
                || stack.getItem() == Items.IRON_CHESTPLATE || stack.getItem() == Items.CHAINMAIL_CHESTPLATE
                || stack.getItem() == Items.GOLDEN_CHESTPLATE || stack.getItem() == Items.LEATHER_CHESTPLATE;
    }

    public static boolean hasEnchant(ItemStack stack, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        for (net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> holder : stack.getEnchantments().keySet()) {
            if (holder.is(key)) {
                return true;
            }
        }
        return false;
    }

    public static float angleDelta(float degrees) {
        float wrapped = degrees % 360f;
        if (wrapped >= 180f) {
            wrapped -= 360f;
        }
        if (wrapped < -180f) {
            wrapped += 360f;
        }
        return wrapped;
    }

    public static float wrapDegrees(float degrees) {
        return angleDelta(degrees);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
