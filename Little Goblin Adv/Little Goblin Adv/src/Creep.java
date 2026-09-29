import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Creep extends Entity {
    private float speed = 1.2f;
    private float velY = 0;
    private boolean isDead = false;
    private int deathTimer = 0;

    private static final int WIDTH = 68; // Sesuaikan dengan ukuran sprite Anda
    private static final int HEIGHT = 54;

    private TileMap tileMap;

    // Assets
    private BufferedImage[] idleFrames;
    private BufferedImage[] walkFrames;
    private BufferedImage[] dyingFrames;
    private int frameIndex = 0;
    private int animationTick = 0;
    private boolean facingRight = true;

    public Creep(float startX, float startY, TileMap map) {
        super(startX, startY, WIDTH, HEIGHT);
        this.tileMap = map;
        loadSprites();
    }

    private void loadSprites() {
        try {
            String basePath = "D:\\UGM\\SEM 2\\PBO & PPBO\\KITA PAKAI KODE YANG INI GAMENYA\\LittleGoblinSimpel2\\craftpix-net-563568-free-wraith-tiny-style-2d-sprites\\PNG\\Wraith_02\\PNG Sequences\\";

            idleFrames = new BufferedImage[12];
            for (int i = 0; i < 12; i++) {
                idleFrames[i] = ImageIO
                        .read(new File(basePath + "Idle\\Wraith_02_Idle_" + String.format("%03d", i) + ".png"));
            }

            walkFrames = new BufferedImage[12];
            for (int i = 0; i < 12; i++) {
                walkFrames[i] = ImageIO.read(
                        new File(basePath + "Walking\\Wraith_02_Moving Forward_" + String.format("%03d", i) + ".png"));
            }

            dyingFrames = new BufferedImage[15];
            for (int i = 0; i < 15; i++) {
                dyingFrames[i] = ImageIO
                        .read(new File(basePath + "Dying\\Wraith_02_Dying_" + String.format("%03d", i) + ".png"));
            }
        } catch (Exception e) {
            System.out.println("Gagal memuat asset Wraith: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        if (isDead) {
            deathTimer++;
            animationTick++;
            if (animationTick >= 10) {
                animationTick = 0;
                if (dyingFrames != null && frameIndex < dyingFrames.length - 1) {
                    frameIndex++; // Berhenti di frame terakhir saat mati
                }
            }
            return;
        }

        if (speed > 0)
            facingRight = true;
        else if (speed < 0)
            facingRight = false;

        // Update Animasi
        animationTick++;
        if (animationTick >= 10) { // Kecepatan ganti frame
            animationTick = 0;
            frameIndex++;
            int maxFrames = (speed == 0) ? (idleFrames != null ? idleFrames.length : 1)
                    : (walkFrames != null ? walkFrames.length : 1);
            if (frameIndex >= maxFrames)
                frameIndex = 0;
        }

        // 1. Gravitasi
        velY += 0.5f;
        if (velY > 8f)
            velY = 8f;
        setY(getY() + velY);

        int bottomTileY = (int) ((getY() + getHeight()) / TileMap.TILE_SIZE);
        int leftTileX = (int) (getX() / TileMap.TILE_SIZE);
        int rightTileX = (int) ((getX() + getWidth() - 1) / TileMap.TILE_SIZE);

        boolean onGround = false;
        if (tileMap.isSolid(leftTileX, bottomTileY) || tileMap.isSolid(rightTileX, bottomTileY)) {
            setY(bottomTileY * TileMap.TILE_SIZE - getHeight());
            velY = 0;
            onGround = true;
        }

        // 2. Pergerakan X (Patroli)
        if (onGround) {
            setX(getX() + speed);

            int nextLeftX = (int) (getX() / TileMap.TILE_SIZE);
            int nextRightX = (int) ((getX() + getWidth()) / TileMap.TILE_SIZE);
            int midY = (int) ((getY() + getHeight() / 2) / TileMap.TILE_SIZE);
            int feetTileY = (int) ((getY() + getHeight() + 2) / TileMap.TILE_SIZE);

            if (speed > 0) {
                boolean hitWall = tileMap.isSolid(nextRightX, midY);
                boolean isCliff = !tileMap.isSolid(nextRightX, feetTileY) && !tileMap.isPlatform(nextRightX, feetTileY);
                if (hitWall || isCliff) {
                    if (hitWall)
                        setX(nextRightX * TileMap.TILE_SIZE - getWidth() - 1);
                    speed = -speed;
                }
            } else if (speed < 0) {
                boolean hitWall = tileMap.isSolid(nextLeftX, midY);
                boolean isCliff = !tileMap.isSolid(nextLeftX, feetTileY) && !tileMap.isPlatform(nextLeftX, feetTileY);
                if (hitWall || isCliff) {
                    if (hitWall)
                        setX((nextLeftX + 1) * TileMap.TILE_SIZE + 1);
                    speed = -speed;
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2, Camera camera) {
        if (deathTimer > 150)
            return;

        int screenX = camera.worldToScreenX((int) getX());
        int screenY = camera.worldToScreenY((int) getY());

        drawSprite(g2, screenX, screenY);
    }

    private void drawSprite(Graphics2D g2, int x, int y) {
        BufferedImage[] currentAnim = idleFrames;
        if (isDead) {
            currentAnim = dyingFrames;
        } else if (speed != 0) {
            currentAnim = walkFrames;
        }

        if (currentAnim != null && frameIndex < currentAnim.length && currentAnim[frameIndex] != null) {
            BufferedImage currentFrame = currentAnim[frameIndex];

            // Membalik gambar jika menghadap kiri
            if (!facingRight) {
                g2.drawImage(currentFrame, x + WIDTH, y, -WIDTH, HEIGHT, null);
            } else {
                g2.drawImage(currentFrame, x, y, WIDTH, HEIGHT, null);
            }
        } else {
            // Fallback jika gambar gagal dimuat
            g2.setColor(Color.RED);
            g2.fillRect(x, y, (int) getWidth(), (int) getHeight());
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isReadyToRemove() {
        return deathTimer > 150;
    }

    public void die() {
        if (!isDead) {
            isDead = true;
            speed = 0;
            frameIndex = 0;
            animationTick = 0;
        }
    }
}