import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import javax.imageio.ImageIO;

public class Goblin extends Entity {
    // ── PERBAIKAN: Ukuran Goblin diperbesar lagi supaya lebih hadir di layar ──
    private static final int NORMAL_W = 36, NORMAL_H = 42;
    private static final int SUPER_W = 42, SUPER_H = 50;

    // ── PERBAIKAN: "Obat Kuat" Fisika Lompatan ──
    private float velX = 0, velY = 0;
    private float moveSpeed = 4.0f; // Jaga kecepatan lari tetap 4
    // UBAH jumpPower menjadi -12.0f (sebelumnya mungkin -10 atau -11).
    // Angka negatif lebih besar = lompatan lebih tinggi.
    private float jumpPower = -12.0f;
    private float gravity = 0.6f;

    // ... (State variables lain di bawahnya biarkan saja)
    private boolean onGround = false;
    private boolean facingRight = true;
    private boolean isSuper = false;
    private boolean hasWeapon = false;
    private boolean isDead = false;
    private boolean isAttacking = false;
    private int attackTimer = 0;
    private int airTime = 0;

    // Stat & Combat
    private int hp = 3, maxHp = 3;
    private int score = 0;
    private int invincibleTimer = 0;
    private static final int INV_FRAMES = 60; // 1 detik kebal

    // Cooldown Tembak Api
    private int shootCooldown = 0;
    private static final int SHOOT_DELAY = 20;

    // Animasi Melempar Senjata
    private boolean isThrowing = false;
    private int throwTimer = 0;

    // Getters & Setters (Encapsulation)
    public static int getNormalWidth() {
        return NORMAL_W;
    }

    public static int getNormalHeight() {
        return NORMAL_H;
    }

    public static int getSuperWidth() {
        return SUPER_W;
    }

    public static int getSuperHeight() {
        return SUPER_H;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getScore() {
        return score;
    }

    public boolean isSuper() {
        return isSuper;
    }

    public boolean hasWeapon() {
        return hasWeapon;
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void addScore(int p) {
        if (!isDead)
            score += p;
    }

    public void setVelX(float v) {
        velX = v;
    }

    public void setVelY(float v) {
        velY = v;
    }

    public float getVelX() {
        return velX;
    }

    public float getVelY() {
        return velY;
    }

    private static final String PROJECT_SPRITE_ROOT = "assets/goblin";
    private static final String EXTERNAL_SPRITE_ROOT = "D:\\UGM\\SEM 2\\PBO & PPBO\\KITA PAKAI KODE YANG INI GAMENYA\\LittleGoblinSimpel2\\craftpix-064112-free-orc-ogre-and-goblin-chibi-2d-game-sprites\\Goblin\\PNG\\PNG Sequences";
    private static final BufferedImage[] IDLE_SPRITES = loadGoblinSprites("Idle");
    private static final BufferedImage[] WALK_SPRITES = loadGoblinSprites("Walking");
    private static final BufferedImage[] JUMP_START_SPRITES = loadGoblinSprites("Jump Start");
    private static final BufferedImage[] JUMP_LOOP_SPRITES = loadGoblinSprites("Jump Loop");
    private static final BufferedImage[] THROWING_SPRITES = loadGoblinSprites("Throwing");
    private static final BufferedImage[] DYING_SPRITES = loadGoblinSprites("Dying");
    private static final boolean HAS_IDLE_SPRITES = IDLE_SPRITES != null && IDLE_SPRITES.length > 0;
    private static final boolean HAS_WALK_SPRITES = WALK_SPRITES != null && WALK_SPRITES.length > 0;
    private static final boolean HAS_JUMP_START_SPRITES = JUMP_START_SPRITES != null && JUMP_START_SPRITES.length > 0;
    private static final boolean HAS_JUMP_LOOP_SPRITES = JUMP_LOOP_SPRITES != null && JUMP_LOOP_SPRITES.length > 0;
    private static final boolean HAS_THROWING_SPRITES = THROWING_SPRITES != null && THROWING_SPRITES.length > 0;
    private static final boolean HAS_DYING_SPRITES = DYING_SPRITES != null && DYING_SPRITES.length > 0;
    private static final boolean HAS_SPRITES = HAS_IDLE_SPRITES || HAS_WALK_SPRITES || HAS_JUMP_START_SPRITES || HAS_JUMP_LOOP_SPRITES || HAS_THROWING_SPRITES || HAS_DYING_SPRITES;
    private static final int ATTACK_DURATION = 25;
    private int dyingFrame = 0;
    private static final int DYING_FRAME_DELAY = 8;
    private int dyingFrameTick = 0;
    // 12 frame × 6 ticks per frame @ 60fps ≈ 1.2 detik, cukup untuk 1x putaran penuh

    private static final int WEAPON_THROW_DURATION = HAS_THROWING_SPRITES ? Math.max(72, THROWING_SPRITES.length * 6): 72;

    private static BufferedImage[] loadGoblinSprites(String subfolder) {
        String[] candidates = { PROJECT_SPRITE_ROOT, EXTERNAL_SPRITE_ROOT };
        for (String rootPath : candidates) {
            File dir = new File(rootPath, subfolder);
            if (!dir.exists() || !dir.isDirectory())
                continue;
            File[] files = dir.listFiles(file -> file.isFile() && file.getName().toLowerCase().endsWith(".png"));
            if (files == null || files.length == 0)
                continue;
            Arrays.sort(files, Comparator.comparing(File::getName));
            BufferedImage[] frames = new BufferedImage[files.length];
            try {
                for (int i = 0; i < files.length; i++) {
                    BufferedImage img = ImageIO.read(files[i]);
                    frames[i] = trimWhitespace(img);
                }
                return frames;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Goblin tidak melayang saat di ingame
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

    private BufferedImage getCurrentSprite() {
        if (!HAS_SPRITES)
            return null;
        if (isDead && HAS_DYING_SPRITES) {
            return DYING_SPRITES[Math.min(dyingFrame, DYING_SPRITES.length - 1)];
        }
        BufferedImage[] sequence = null;
        if (isThrowing && HAS_THROWING_SPRITES) {
            // Hitung frame berdasarkan progres (play sekali, tidak loop)
            sequence = THROWING_SPRITES;
        } else if (attackTimer > 0) {
            if (sequence == null || sequence.length == 0) {
                sequence = onGround ? JUMP_START_SPRITES : JUMP_LOOP_SPRITES;
            }
        } else if (!onGround) {
            sequence = HAS_JUMP_START_SPRITES && airTime < JUMP_START_SPRITES.length * 10 ? JUMP_START_SPRITES
                    : JUMP_LOOP_SPRITES;
            if (sequence == null || sequence.length == 0) {
                sequence = HAS_WALK_SPRITES ? WALK_SPRITES : IDLE_SPRITES;
            }
        } else if (Math.abs(velX) > 0.1f) {
            sequence = WALK_SPRITES;
        } else {
            sequence = IDLE_SPRITES;
        }

        if (sequence == null || sequence.length == 0) {
            sequence = HAS_IDLE_SPRITES ? IDLE_SPRITES : HAS_WALK_SPRITES ? WALK_SPRITES : null;
        }
        if (sequence == null || sequence.length == 0)
            return null;

        int frameIndex = (int) ((System.currentTimeMillis() / 70) % sequence.length);
        return sequence[frameIndex];
    }

    private TileMap tileMap;

    public Goblin(float startX, float startY, TileMap map) {
        super(startX, startY, NORMAL_W, NORMAL_H);
        this.tileMap = map;
        alignToGround();
    }

    public void alignToGround() {
        int S = TileMap.TILE_SIZE;
        int leftCol = (int) getX() / S;
        int rightCol = (int) (getX() + getWidth() - 1) / S;
        int row = (int) ((getY() + getHeight() - 1) / S);
        while (row < tileMap.getRows()) {
            boolean supported = false;
            for (int col = leftCol; col <= rightCol; col++) {
                if (tileMap.isSolid(col, row)) {
                    supported = true;
                    break;
                }
            }
            if (supported)
                break;
            row++;
        }
        if (row < tileMap.getRows()) {
            setY(row * S - getHeight());
            onGround = true;
            setVelY(0);
        }
    }

    public void handleInput(boolean[] keys) {
        if (isDead)
            return;

        velX = 0;
        // Sekarang bisa pakai Panah KIRI/KANAN atau tombol A/D
        if (keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_A]) {
            velX = -moveSpeed;
            facingRight = false;
        }
        if (keys[KeyEvent.VK_RIGHT] || keys[KeyEvent.VK_D]) {
            velX = moveSpeed;
            facingRight = true;
        }

        // Serangan ketika ada senjata dan tombol Z ditekan
        if (keys[KeyEvent.VK_Z] && hasWeapon && attackTimer == 0 && !isDead) {
            attackTimer = ATTACK_DURATION;
            isAttacking = true;
        }

        // Lompat bisa pakai SPASI, W, atau Panah ATAS
        if ((keys[KeyEvent.VK_SPACE] || keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]) && onGround) {
            velY = jumpPower;
            onGround = false;
        }

        // Sprint / Super Jump (Tombol Shift)
        if (isSuper) {
            moveSpeed = keys[KeyEvent.VK_SHIFT] ? 6.0f : 4.0f;
            if (keys[KeyEvent.VK_SHIFT] && (keys[KeyEvent.VK_SPACE] || keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP])
                    && onGround) {
                velY = -14.5f;
                onGround = false;
            }
        } else {
            moveSpeed = 4.0f;
        }
    }

    public boolean canShoot(boolean[] keys) {
        if (shootCooldown > 0)
            return false;
        return hasWeapon && keys[KeyEvent.VK_X] && !isDead;
    }

    public void resetShootCooldown() {
        this.shootCooldown = SHOOT_DELAY;
        // Trigger animasi throwing saat X ditekan
        if (HAS_THROWING_SPRITES) {
            isThrowing = true;
            throwTimer = WEAPON_THROW_DURATION;
        }
    }

    @Override
    public void update() {
        if (isDead) {
            updateDyingAnim();
            velY += gravity;
            if (velY > 10.0f)
                velY = 10.0f;
            setX(getX() + velX);
            setY(getY() + velY);
            return;
        }
        if (invincibleTimer > 0)
            invincibleTimer--;
        if (shootCooldown > 0)
            shootCooldown--;
        if (attackTimer > 0) {
            attackTimer--;
            if (attackTimer == 0)
                isAttacking = false;
        }
        if (isThrowing) {
            throwTimer--;
            if (throwTimer == 0)
                isThrowing = false;
        }
        if (!onGround) {
            airTime++;
        } else {
            airTime = 0;
        }

        // 1. Gravitasi
        velY += gravity;
        if (velY > 10.0f)
            velY = 10.0f; // Terminal velocity

        // 2. Terapkan Kecepatan
        setX(getX() + velX);
        setY(getY() + velY);

        // 3. Resolusi Tabrakan
        checkCollisions();
        // --- TAMBAHAN: CEK KENA DURI ---
        int S = TileMap.TILE_SIZE;
        int leftCol = (int) getX() / S;
        int rightCol = (int) (getX() + getWidth() - 1) / S;
        int bottomRow = (int) (getY() + getHeight() - 1) / S;

        // Kalau kaki kiri atau kaki kanan nyentuh DANGER (Duri), Goblin kena damage!
        if (tileMap.isDanger(leftCol, bottomRow) || tileMap.isDanger(rightCol, bottomRow)) {
            takeDamage();
        }

        // 4. Batas Dunia (World Bounds)
        int mapW = tileMap.getMapWidthPixels();
        int mapH = tileMap.getMapHeightPixels();

        if (getX() < 0)
            setX(0);
        if (getX() + getWidth() > mapW)
            setX(mapW - getWidth());

        // Jatuh ke bawah dunia = INSTANT DEATH
        if (getY() > mapH && !isDead) {
            hp = 0;
            isDead = true;
            dyingFrame = 0;
            dyingFrameTick = 0;
            setVelX(0);
            setVelY(0);
        }
    }

    private void updateDyingAnim() {
        if (!HAS_DYING_SPRITES)
            return;
        dyingFrameTick++;
        if (dyingFrameTick >= DYING_FRAME_DELAY) {
            dyingFrameTick = 0;
            if (dyingFrame < DYING_SPRITES.length - 1)
                dyingFrame++;
        }
    }

    private void checkCollisions() {
        int S = TileMap.TILE_SIZE;
        float curX = getX(), curY = getY(), w = getWidth(), h = getHeight();

        // Tabrakan Vertikal (Y)
        int bottomRow = (int) (curY + h) / S;
        int topRow = (int) curY / S;
        int leftCol = (int) curX / S;
        int rightCol = (int) (curX + w - 1) / S;

        onGround = false;
        if (velY >= 0) {
            boolean hitFloor = false;
            for (int col = leftCol; col <= rightCol; col++) {
                if (tileMap.isSolid(col, bottomRow)) {
                    hitFloor = true;
                    break;
                }
            }
            if (hitFloor) {
                setY(bottomRow * S - h);
                velY = 0;
                onGround = true;
            }
        } else if (velY < 0) {
            boolean hitCeiling = false;
            for (int col = leftCol; col <= rightCol; col++) {
                if (tileMap.isSolid(col, topRow)) {
                    hitCeiling = true;
                    break;
                }
            }
            if (hitCeiling) {
                setY((topRow + 1) * S);
                velY = 0;
            }
        }

        // Tabrakan Horizontal (X)
        curX = getX();
        curY = getY();
        topRow = (int) curY / S;
        bottomRow = (int) (curY + h - 1) / S;

        if (velX > 0) {
            int rightC = (int) (curX + w) / S;
            boolean hitWall = false;
            for (int row = topRow; row <= bottomRow; row++) {
                if (tileMap.isSolid(rightC, row)) {
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) {
                setX(rightC * S - w);
                velX = 0;
            }
        } else if (velX < 0) {
            int leftC = (int) curX / S;
            boolean hitWall = false;
            for (int row = topRow; row <= bottomRow; row++) {
                if (tileMap.isSolid(leftC, row)) {
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) {
                setX((leftC + 1) * S);
                velX = 0;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2, Camera camera) {
        if (invincibleTimer > 0 && (invincibleTimer / 4) % 2 == 0)
            return; // Efek kedip pas kena damage

        int screenX = camera.worldToScreenX((int) getX());
        int screenY = camera.worldToScreenY((int) getY());

        if (HAS_SPRITES) {
            BufferedImage sprite = getCurrentSprite();
            if (sprite != null) {
                int drawW = getWidth();
                int drawH = getHeight();
                if (facingRight) {
                    g2.drawImage(sprite, screenX, screenY, drawW, drawH, null);
                } else {
                    g2.drawImage(sprite, screenX + drawW, screenY, -drawW, drawH, null);
                }
                if (isDead && !HAS_DYING_SPRITES) {
                    g2.setColor(Color.RED);
                    g2.setFont(new Font("Arial", Font.BOLD, 12));
                    g2.drawString("X_X", screenX, screenY - 15);
                }
                return;
            }
        }

        // 5. Pedang
        if (hasWeapon && !isDead) {
            g2.setColor(new Color(200, 200, 220));
            if (facingRight) {
                g2.fillRect(screenX + getWidth(), screenY + 10, 4, 16);
                g2.fillRect(screenX + getWidth() - 2, screenY + 16, 8, 4);
            } else {
                g2.fillRect(screenX - 4, screenY + 10, 4, 16);
                g2.fillRect(screenX - 6, screenY + 16, 8, 4);
            }
        }

        // ── PERBAIKAN: Posisi Teks Mati biar gak nempel jidat ──
        if (isDead) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            // Ubah koordinat Y-nya dari (screenY - 5) jadi (screenY - 15)
            g2.drawString("X_X", screenX, screenY - 15);
        }
    }

    // Power-Up Mechanics
    public void activateSuper() {
        if (!isSuper) {
            isSuper = true;
            float oldY = getY();
            setWidth(SUPER_W);
            setHeight(SUPER_H);
            setY(oldY - (SUPER_H - NORMAL_H));
            maxHp = 5;
        }
        if (hp < maxHp)
            hp++;
    }

    public void deactivateSuper() {
        if (!isSuper)
            return;
        isSuper = false;
        float oldY = getY();
        setWidth(NORMAL_W);
        setHeight(NORMAL_H);
        setY(oldY + (SUPER_H - NORMAL_H));
        maxHp = 3;
        hp = Math.min(hp, maxHp);
    }

    // Combat Mechanics
    public void takeDamage() {
        if (invincibleTimer > 0 || isDead)
            return;
        hp--;
        invincibleTimer = INV_FRAMES;

        if (isSuper && hp <= 3) {
            deactivateSuper();
        }

        if (hp <= 0) {
            hp = 0;
            isDead = true;
            dyingFrame = 0;
            dyingFrameTick = 0;
            setVelX(0);
            setVelY(-8);
        }
    }

    public void pickupWeapon() {
        this.hasWeapon = true;
        // Trigger throwing animation
        isThrowing = true;
        throwTimer = WEAPON_THROW_DURATION;
    }



    public boolean isOnDoor() {
        int S = TileMap.TILE_SIZE;
        int col = getCenterX() / S;
        int row = getCenterY() / S;
        return tileMap.getTile(col, row) == 8;
    }
}