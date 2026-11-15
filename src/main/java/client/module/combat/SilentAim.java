package client.module.combat;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

/**
 * Silent Aim: наводит удар/установку на цель только для сервера.
 *
 * <p>Поворот игрока подменяется на время действия, сервер получает пакет ротации
 * с нужным углом и следующие за ним пакеты взаимодействия, после чего угол
 * возвращается. Камера и модель игрока не дёргаются, потому что откат происходит
 * до отрисовки кадра, а обычная ротация уйдёт на сервер следующим тиком.</p>
 */
public final class SilentAim {
    private static boolean active;
    private static float savedYaw;
    private static float savedPitch;

    private SilentAim() {
    }

    /** Резко наводит на точку и отправляет серверу подменённую ротацию. */
    public static boolean begin(Vec3 point) {
        LocalPlayer player = CombatApi.player();
        if (player == null || point == null) {
            return false;
        }
        end();
        savedYaw = player.getYRot();
        savedPitch = player.getXRot();
        float yaw = CombatApi.yawTo(point);
        float pitch = Math.max(-90f, Math.min(90f, CombatApi.pitchTo(point)));
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch,
                player.onGround(), false));
        active = true;
        return true;
    }

    public static void begin(net.minecraft.world.entity.Entity entity) {
        if (entity != null) {
            begin(entity.position().add(0, entity.getBbHeight() * 0.5, 0));
        }
    }

    /** Возвращает исходный поворот: визуально игрок ничего не замечает. */
    public static void end() {
        if (!active) {
            return;
        }
        active = false;
        LocalPlayer player = CombatApi.player();
        if (player == null) {
            return;
        }
        player.setYRot(savedYaw);
        player.setXRot(savedPitch);
    }

    public static boolean isActive() {
        return active;
    }
}
