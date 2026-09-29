import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Fireball extends Entity {
    private float speed;
    private float spinAngle = 0f;
    private static final float SPIN_SPEED = 0.25f; // radians per frame
    private static final String PLAYER_FIREBALL_IMAGE_PATH = "D:\\UGM\\SEM 2\\PBO & PPBO\\KITA PAKAI KODE YANG INI GAMENYA\\LittleGoblinSimpel2\\craftpix-net-974061-free-rocks-and-stones-top-down-pixel-art\\PNG\\Objects_separately\\Rock1_5.png";
    private static final String BOSS_FIREBALL_IMAGE_PATH = "D:\\UGM\\SEM 2\\PBO & PPBO\\KITA PAKAI KODE YANG INI GAMENYA\\LittleGoblinSimpel2\\craftpix-net-974061-free-rocks-and-stones-top-down-pixel-art\\PNG\\Objects_separately\\Rock2_2.png";
    private static final BufferedImage PLAYER_FIREBALL_IMAGE = loadFireballImage(PLAYER_FIREBALL_IMAGE_PATH);
    private static final BufferedImage BOSS_FIREBALL_IMAGE = loadFireballImage(BOSS_FIREBALL_IMAGE_PATH);
    private int life; // Berapa lama tembakan bertahan sebelum hilang
    public boolean alive = true;
    private boolean fromPlayer; // Mengecek siapa yang nembak
    private TileMap tileMap;

    public Fireball(float startX, float startY, boolean facingRight, boolean fromPlayer, TileMap map) {
        // Pemain: kecil & cepat. Entity.Boss: besar & lambat.
        super(startX, startY, fromPlayer ? 10 : 20, fromPlayer ? 10 : 20);
        this.fromPlayer = fromPlayer;
        this.tileMap = map;
        this.speed = fromPlayer ? 7.0f : 4.0f; // Kecepatan
        this.life = fromPlayer ? 60 : 120; // Umur (frame)

        if (!facingRight) this.speed = -speed; // Balik arah jika hadap kiri
    }

    private static BufferedImage loadFireballImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Gagal memuat asset fireball: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void update() {
        if (!alive) return;

        setX(getX() + speed); // Gerak horizontal
        spinAngle += SPIN_SPEED;
        if (spinAngle >= Math.PI * 2)
            spinAngle -= Math.PI * 2;
        life--;
        if (life <= 0) alive = false; // Mati jika umur habis

        // Cek tabrakan dinding
        int tileX = (int)(getX() / TileMap.TILE_SIZE);
        int tileY = (int)(getY() / TileMap.TILE_SIZE);
        if (tileMap.isSolid(tileX, tileY)) {
            alive = false; // Hancur jika nabrak tembok
        }
    }

    @Override
    public void draw(Graphics2D g2, Camera camera) {
        if (!alive) return;
        int sx = camera.worldToScreenX((int) getX());
        int sy = camera.worldToScreenY((int) getY());

        // Gambar bola api
        if (fromPlayer && PLAYER_FIREBALL_IMAGE != null) {
            AffineTransform oldTransform = g2.getTransform();
            g2.rotate(spinAngle, sx + getWidth() / 2.0, sy + getHeight() / 2.0);
            g2.drawImage(PLAYER_FIREBALL_IMAGE, sx, sy, getWidth(), getHeight(), null);
            g2.setTransform(oldTransform);
        } else {
            AffineTransform oldTransform = g2.getTransform();
            g2.rotate(spinAngle, sx + getWidth() / 2.0, sy + getHeight() / 2.0);
            int drawH = 30;
            int drawW = 30;
            g2.drawImage(BOSS_FIREBALL_IMAGE, sx, sy, drawW, drawH, null);
            g2.setTransform(oldTransform);
        }
    }

    public boolean isAlive() { return alive; }
    public boolean isFromPlayer() { return fromPlayer; }
}