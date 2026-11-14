package client.notification;

import client.gui.animation.Animation;

/**
 * Одно уведомление Draxon: текст, тип (вкл/выкл/информация/ошибка), время жизни
 * и анимация появления. Экземпляры создаёт {@link NotificationManager}.
 */
public final class Notification {
    public static final long LIFETIME_NANOS = 3_200_000_000L;
    private static final long FADE_OUT_NANOS = 380_000_000L;

    public enum Type {
        ENABLED,
        DISABLED,
        INFO,
        ERROR
    }

    private final String text;
    private final Type type;
    private final long createdAt = System.nanoTime();
    private final Animation appear = new Animation(0f, 0.32f);

    Notification(String text, Type type) {
        this.text = text;
        this.type = type;
        this.appear.setTarget(1f);
    }

    public String text() {
        return text;
    }

    public Type type() {
        return type;
    }

    /** 0..1 — насколько уведомление «раскрыто» по горизонтали. */
    public float appear() {
        return appear.update();
    }

    /** Доля оставшегося времени жизни; после нуля уведомление удаляется. */
    public float remaining() {
        long age = System.nanoTime() - createdAt;
        return 1f - (float) age / LIFETIME_NANOS;
    }

    /** Прозрачность на затухании в конце жизни. */
    public float fade() {
        long left = LIFETIME_NANOS - (System.nanoTime() - createdAt);
        if (left >= FADE_OUT_NANOS) {
            return 1f;
        }
        return Math.max(0f, (float) left / FADE_OUT_NANOS);
    }

    public boolean expired() {
        return remaining() <= 0f;
    }
}
