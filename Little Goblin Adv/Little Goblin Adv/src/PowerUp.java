import java.awt.*;

public class PowerUp extends Entity {
    private boolean isCollected = false;
    private static final int SIZE = 20;

    public PowerUp(float startX, float startY) {
        super(startX, startY, SIZE, SIZE);
    }

    @Override
    public void update() {
        // Item diam saja, tidak perlu update logika fisik yang rumit
    }

    @Override
    public void draw(Graphics2D g2, Camera camera) {
        if (isCollected)
            return;

        int screenX = camera.worldToScreenX((int) getX());
        int screenY = camera.worldToScreenY((int) getY());

        // Gambar Jamur/Item (Kuning dengan titik-titik merah)
        g2.setColor(Color.YELLOW);
        g2.fillOval(screenX, screenY, getWidth(), getHeight());

        g2.setColor(Color.RED);
        g2.fillOval(screenX + 4, screenY + 4, 4, 4);
        g2.fillOval(screenX + 12, screenY + 10, 4, 4);
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void collect() {
        isCollected = true;
    }
}