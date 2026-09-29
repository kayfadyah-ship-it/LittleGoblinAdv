
//  Entity.Boss.java — Caveman Entity.Boss Sprite Edition (Trimmed Whitespace)
// ============================================================
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import javax.imageio.ImageIO;

public class Boss extends Entity {
    private int hp, maxHp;
    private float speed = 2.5f;
    private float velY = 0;
    private boolean isDead = false;
    private int hitTimer = 0;

    // Ukuran render boss — bisa diperbesar sesuai selera
    private static final int WIDTH = 58, HEIGHT = 96;
    private TileMap tileMap;

    // ── AI Menembak ──
    private int attackTimer = 0;
    private static final int SHOOT_INTERVAL = 90;

    // ── Animasi "shoot" visual (berapa frame tampil anim attacking) ──
    private int shootAnimTimer = 0;
    private static final int SHOOT_ANIM_DURATION = 30;

    // ── Animasi Hurt ──
    private int hurtAnimTimer = 0;
    private static final int HURT_ANIM_DURATION = 20;

    // ────────────────────────────────────────────────────────
    // SPRITE LOADING
    // ────────────────────────────────────────────────────────
    private static final String SPRITE_ROOT = "D:\\UGM\\SEM 2\\PBO & PPBO\\KITA PAKAI KODE YANG INI GAMENYA\\LittleGoblinSimpel2\\craftpix-net-907874-free-top-down-boss-character-4-direction-pack\\Caveman Boss\\PNG\\PNG Sequences";

    // Kanan
    private static final BufferedImage[] IDLE_R = loadBossSprites("Right - Idle");
    private static final BufferedImage[] WALK_R = loadBossSprites("Right - Walking");
    private static final BufferedImage[] ATTACK_R = loadBossSprites("Right - Attacking");
    private static final BufferedImage[] HURT_R = loadBossSprites("Right - Hurt");
    // Kiri (pakai sprite Left)
    private static final BufferedImage[] IDLE_L = loadBossSprites("Left - Idle");
    private static final BufferedImage[] WALK_L = loadBossSprites("Left - Walking");
    private static final BufferedImage[] ATTACK_L = loadBossSprites("Left - Attacking");
    private static final BufferedImage[] HURT_L = loadBossSprites("Left - Hurt");
    // Dying (hanya satu arah)
    private static final BufferedImage[] DYING = loadBossSprites("Dying");

    private static final boolean HAS_SPRITES = (IDLE_R != null && IDLE_R.length > 0) ||
            (WALK_R != null && WALK_R.length > 0);

    // Frame timer untuk animasi dying (tidak loop)
    private int dyingFrame = 0;
    private static final int DYING_FRAME_DELAY = 6; // ticks per frame
    private int dyingFrameTick = 0;

    // ────────────────────────────────────────────────────────
    // METODE PERBAIKAN: Menghilangkan Whitespace saat Load
    // ────────────────────────────────────────────────────────
    private static BufferedImage[] loadBossSprites(String subfolder) {
        File dir = new File(SPRITE_ROOT, subfolder);
        if (!dir.exists() || !dir.isDirectory())
            return null;
        File[] files = dir.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0)
            return null;
        Arrays.sort(files, Comparator.comparing(File::getName));
        try {
            BufferedImage[] frames = new BufferedImage[files.length];
            for (int i = 0; i < files.length; i++) {
                // ── PERBAIKAN: Baca gambar lalu trim whitespace-nya ──
                BufferedImage img = ImageIO.read(files[i]);
                frames[i] = trimWhitespace(img); 
            }
            return frames;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── TAMBAHAN: Fungsi Pemotong Area Transparan (Agar tidak melayang) ──
    private static BufferedImage trimWhitespace(BufferedImage img) {
        int left = img.getWidth();
        int right = 0;
        int top = img.getHeight();
        int bottom = 0;

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int alpha = (img.getRGB(x, y) >> 24) & 0xff;
                if (alpha != 0) {
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                }
            }
        }

        if (right < left || bottom < top) {
            return img;
        }

        return img.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }

    // ────────────────────────────────────────────────────────
    public Boss(float startX, float startY, TileMap map) {
        super(startX, startY, WIDTH, HEIGHT);
        this.tileMap = map;
        this.maxHp = 100;
        this.hp = maxHp;
    }

    // ────────────────────────────────────────────────────────
    @Override
    public void update() {
        // Update anim dying bahkan setelah mati
        if (isDead) {
            updateDyingAnim();
            return;
        }
        if (hitTimer > 0)
            hitTimer--;
        if (hurtAnimTimer > 0)
            hurtAnimTimer--;
        if (shootAnimTimer > 0)
            shootAnimTimer--;

        attackTimer++;

        // 1. Gravitasi
        velY += 0.5f;
        setY(getY() + velY);

        int bottomRow = (int) ((getY() + getHeight()) / TileMap.TILE_SIZE);
        int leftCol = (int) (getX() / TileMap.TILE_SIZE);
        int rightCol = (int) ((getX() + getWidth() - 1) / TileMap.TILE_SIZE);

        boolean onGround = false;
        if (tileMap.isSolid(leftCol, bottomRow) || tileMap.isSolid(rightCol, bottomRow)) {
            setY(bottomRow * TileMap.TILE_SIZE - getHeight());
            velY = 0;
            onGround = true;
        }

        // 2. Patroli — berbalik di ujung jurang/tembok
        if (onGround) {
            setX(getX() + speed);
            int curColLeft = (int) (getX() / TileMap.TILE_SIZE);
            int curColRight = (int) ((getX() + getWidth()) / TileMap.TILE_SIZE);
            int midRow = (int) ((getY() + getHeight() / 2) / TileMap.TILE_SIZE);
            int feetRow = (int) ((getY() + getHeight() + 2) / TileMap.TILE_SIZE);

            if (speed > 0) {
                boolean wall = tileMap.isSolid(curColRight, midRow);
                boolean cliff = !tileMap.isSolid(curColRight, feetRow) && !tileMap.isPlatform(curColRight, feetRow);
                if (wall || cliff) {
                    if (wall)
                        setX(curColRight * TileMap.TILE_SIZE - getWidth() - 1);
                    speed = -speed;
                }
            } else if (speed < 0) {
                boolean wall = tileMap.isSolid(curColLeft, midRow);
                boolean cliff = !tileMap.isSolid(curColLeft, feetRow) && !tileMap.isPlatform(curColLeft, feetRow);
                if (wall || cliff) {
                    if (wall)
                        setX((curColLeft + 1) * TileMap.TILE_SIZE + 1);
                    speed = -speed;
                }
            }
        }
    }

    private void updateDyingAnim() {
        if (DYING == null || DYING.length == 0)
            return;
        dyingFrameTick++;
        if (dyingFrameTick >= DYING_FRAME_DELAY) {
            dyingFrameTick = 0;
            if (dyingFrame < DYING.length - 1)
                dyingFrame++;
        }
    }

    // ── AI Menembak ──
    public boolean readyToShoot() {
        if (isDead)
            return false;
        if (attackTimer >= SHOOT_INTERVAL) {
            attackTimer = 0;
            shootAnimTimer = SHOOT_ANIM_DURATION;
            return true;
        }
        return false;
    }

    public void takeDamage(int amount) {
        if (hitTimer > 0 || isDead)
            return;
        hp -= amount;
        hitTimer = 15;
        hurtAnimTimer = HURT_ANIM_DURATION;
        if (hp <= 0) {
            hp = 0;
            isDead = true;
            dyingFrame = 0;
            dyingFrameTick = 0;
        }
    }

    // ────────────────────────────────────────────────────────
    // DRAW
    // ────────────────────────────────────────────────────────
    @Override
    public void draw(Graphics2D g2, Camera camera) {
        int sx = camera.worldToScreenX((int) getX());
        int sy = camera.worldToScreenY((int) getY());
        int dw = getWidth();
        int dh = getHeight();

        if (HAS_SPRITES) {
            BufferedImage frame = selectFrame();
            if (frame != null) {
                // Efek kedip saat kena hit
                if (hitTimer > 0 && (hitTimer / 3) % 2 == 0) {
                    // Tint putih: gambar normal lalu overlay putih semi-transparan
                    g2.drawImage(frame, sx, sy, dw, dh, null);
                    Composite old = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
                    g2.setColor(Color.WHITE);
                    g2.fillRect(sx, sy, dw, dh);
                    g2.setComposite(old);
                } else {
                    g2.drawImage(frame, sx, sy, dw, dh, null);
                }
                return;
            }
        }

        // ── Fallback: Geometric placeholder ──
        if (isDead)
            return;
        if (hitTimer > 0 && (hitTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect(sx, sy, dw, dh);
        } else {
            g2.setColor(new Color(60, 10, 80));
            g2.fillRoundRect(sx, sy, dw, dh, 10, 10);
            g2.setColor(Color.RED);
            if (speed > 0) {
                g2.fillRect(sx + 35, sy + 15, 12, 8);
                g2.fillRect(sx + 50, sy + 15, 12, 8);
            } else {
                g2.fillRect(sx + 5, sy + 15, 12, 8);
                g2.fillRect(sx + 20, sy + 15, 12, 8);
            }
            g2.setColor(Color.WHITE);
            for (int i = 0; i < 3; i++) {
                int[] px = { sx + 20 + (i * 10), sx + 25 + (i * 10), sx + 30 + (i * 10) };
                int[] py = { sy + 45, sy + 55, sy + 45 };
                g2.fillPolygon(px, py, 3);
            }
        }
    }

    /** Pilih frame sprite yang sesuai dengan state saat ini. */
    private BufferedImage selectFrame() {
        boolean facingRight = (speed > 0);

        // ── Dying ──
        if (isDead) {
            if (DYING == null || DYING.length == 0)
                return null;
            return DYING[Math.min(dyingFrame, DYING.length - 1)];
        }

        // ── Hurt ──
        if (hurtAnimTimer > 0) {
            BufferedImage[] hurtSeq = facingRight ? HURT_R : HURT_L;
            if (hurtSeq != null && hurtSeq.length > 0) {
                int elapsed = HURT_ANIM_DURATION - hurtAnimTimer;
                int idx = (elapsed * hurtSeq.length) / HURT_ANIM_DURATION;
                return hurtSeq[Math.min(idx, hurtSeq.length - 1)];
            }
        }

        // ── Attacking ──
        if (shootAnimTimer > 0) {
            BufferedImage[] atkSeq = facingRight ? ATTACK_R : ATTACK_L;
            if (atkSeq != null && atkSeq.length > 0) {
                int elapsed = SHOOT_ANIM_DURATION - shootAnimTimer;
                int idx = (elapsed * atkSeq.length) / SHOOT_ANIM_DURATION;
                return atkSeq[Math.min(idx, atkSeq.length - 1)];
            }
        }

        // ── Walking / Idle ──
        long now = System.currentTimeMillis();
        BufferedImage[] walkSeq = facingRight ? WALK_R : WALK_L;
        BufferedImage[] idleSeq = facingRight ? IDLE_R : IDLE_L;

        if (Math.abs(speed) > 0.1f && walkSeq != null && walkSeq.length > 0) {
            int idx = (int) ((now / 80) % walkSeq.length);
            return walkSeq[idx];
        }
        if (idleSeq != null && idleSeq.length > 0) {
            int idx = (int) ((now / 100) % idleSeq.length);
            return idleSeq[idx];
        }
        return null;
    }

    // ── Getters ──
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public boolean isDead() { return isDead; }
    public boolean isFacingRight() { return speed > 0; }
}