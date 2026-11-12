package client.gui.animation;

/**
 * Плавное изменение значения к цели. Скорость не зависит от частоты кадров:
 * speed — доля пути, проходимая за кадр при 60 FPS.
 */
public final class Animation {
    private final float speed;
    private float value;
    private float target;
    private long lastNanos = System.nanoTime();

    public Animation(float initial, float speed) {
        this.value = initial;
        this.target = initial;
        this.speed = speed;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public float getTarget() {
        return target;
    }

    public float get() {
        return value;
    }

    public void snap(float value) {
        this.value = value;
        this.target = value;
    }

    public boolean isFinished() {
        return value == target;
    }

    /** Продвигает анимацию на время, прошедшее с прошлого вызова, и возвращает текущее значение. */
    public float update() {
        long now = System.nanoTime();
        float deltaSeconds = Math.min((now - lastNanos) / 1_000_000_000f, 0.1f);
        lastNanos = now;

        float factor = 1f - (float) Math.pow(1f - speed, deltaSeconds * 60f);
        value += (target - value) * factor;
        if (Math.abs(target - value) < 0.001f) {
            value = target;
        }
        return value;
    }
}
