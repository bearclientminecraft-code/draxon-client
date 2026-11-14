package client.gui.components;

import client.gui.animation.Animation;
import net.minecraft.client.gui.GuiGraphics;

/** Базовый элемент интерфейса: прямоугольник с обработкой мыши, клавиатуры и hover-анимацией. */
public abstract class Component {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected final Animation hover = new Animation(0f, 0.25f);

    /** Высота строки внутри панели (включая промежуток). */
    public int rowHeight() {
        return 30;
    }

    /** Отступ слева для вложенных строк. */
    public int indent() {
        return 0;
    }

    /** Строка, по которой элемент находится поиском. null — элемент не участвует в поиске. */
    public String searchKey() {
        return null;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /** Обновляет hover-анимацию и возвращает её значение (0..1). */
    protected float updateHover(int mouseX, int mouseY) {
        hover.setTarget(isHovered(mouseX, mouseY) ? 1f : 0f);
        return hover.update();
    }

    /** @param alpha общая прозрачность (используется для анимации открытия/закрытия) */
    public abstract void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha);

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    public boolean keyPressed(int key, int scancode, int modifiers) {
        return false;
    }

    public boolean charTyped(int codePoint) {
        return false;
    }
}
