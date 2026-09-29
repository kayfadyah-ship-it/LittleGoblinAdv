import java.awt.*;

public class Weapon extends Entity {
    private boolean isCollected = false;

    public Weapon(float startX, float startY) {
        super(startX, startY, 24, 24); // Hitbox senjata
    }

    @Override
    public void update() {
        // Senjata mengambang diam
    }

    @Override
    public void draw(Graphics2D g2, Camera camera) {
        if (isCollected) return;

        int screenX = camera.worldToScreenX((int) getX());
        int screenY = camera.worldToScreenY((int) getY());

        // Gambar Pedang Tertancap
        int s = 3;
        // Gagang
        g2.setColor(new Color(120, 70, 20));
        g2.fillRect(screenX + 10, screenY + 12, s, 10);
        // Crossguard (Penahan tangan)
        g2.setColor(new Color(200, 170, 50));
        g2.fillRect(screenX + 6, screenY + 12, s * 4, 4);
        // Bilah pedang
        g2.setColor(new Color(200, 200, 220));
        g2.fillRect(screenX + 10, screenY, s, 12);
        // Ujung lancip
        int[] px = {screenX + 10, screenX + 10 + s/2, screenX + 10 + s};
        int[] py = {screenY, screenY - 4, screenY};
        g2.fillPolygon(px, py, 3);

        // Aura emas
        g2.setColor(new Color(255, 200, 50, 100));
        g2.drawOval(screenX, screenY - 5, 24, 30);
    }

    public boolean isCollected() { return isCollected; }
    public void collect() { isCollected = true; }
}