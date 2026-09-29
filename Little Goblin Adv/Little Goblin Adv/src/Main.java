// ============================================================
//  Main.java
//  Entry point aplikasi. Membuat JFrame dan menjalankan game.
// ============================================================
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;

final class GameState {
    public static final int MAIN_MENU = 0;   // Layar judul dan menu utama
    public static final int PLAYING = 1;     // State gameplay normal
    public static final int BOSS_ROOM = 2;   // State boss fight (Entity.Boss sudah aktif)
    public static final int PAUSED = 3;      // Game dijeda
    public static final int GAME_OVER = 4;   // Nyawa habis
    public static final int WIN = 5;         // Goblin berhasil mengalahkan Entity.Boss

    private GameState() {
        // Class ini tidak bisa diinstansiasi
    }
}

public class Main {
    public static void main(String[] args) {
        // Jalankan di Event Dispatch Thread (aturan Swing)
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Little Goblin Adventure");

            GamePanel gamePanel = new GamePanel();

            frame.add(gamePanel);
            frame.pack();  // Sesuaikan ukuran frame dengan preferredSize GamePanel
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null); // Tengah layar
            frame.setVisible(true);

            // Minta fokus agar keyboard input langsung diterima
            gamePanel.requestFocusInWindow();

            // Mulai game loop SETELAH frame terlihat
            gamePanel.startGameLoop();
        });
    }
}

class GamePanel extends JPanel implements Runnable, KeyListener {

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 480;
    private static final int TARGET_FPS = 60;
    private static final long FRAME_TIME_NS = 1_000_000_000L / TARGET_FPS;

    private int currentState = GameState.MAIN_MENU;

    private TileMap tileMap;
    private Camera camera;
    private HUD hud;

    private Goblin goblin;
    private List<Creep> creeps;
    private List<PowerUp> powerUps;
    private Weapon weapon;
    private Boss boss;

    // ── INI DIA YANG BIKIN MERAH TADI (List Bola Api) ──
    private List<Fireball> fireballs;

    private List<FloatText> floatTexts = new ArrayList<>();

    private static class FloatText {
        String text;
        float x, y;
        int life;
        Color color;

        FloatText(String t, float x, float y, Color c) {
            text = t;
            this.x = x;
            this.y = y;
            life = 60;
            color = c;
        }
    }

    private boolean bossActive = false;
    private int bossHp = 100;
    private int bossMaxHp = 100;
    private boolean gameOverFromBoss = false; // Flag untuk tracking game over dari boss

    public boolean[] keys = new boolean[256];
    private boolean[] prevKeys = new boolean[256];

    private BufferedImage offscreenBuffer;
    private Graphics2D offscreenG2d;

    private Thread gameThread;
    private boolean running = false;

    private int fpsCounter = 0;
    private int displayedFps = 0;
    private long lastFpsTime = System.nanoTime();

    private int deathDelayTimer = 0;
    private static final int DEATH_DELAY = 90;

    // Hutan Background
    private BufferedImage treeBgImage;

    // Awan
    private BufferedImage[] cloudImages;

    private static class Cloud {
        float x, y, speed;
        int type;

        Cloud(float x, float y, float speed, int type) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.type = type;
        }
    }

    private List<Cloud> clouds;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        tileMap = new TileMap();
        camera = new Camera(SCREEN_WIDTH, SCREEN_HEIGHT);
        hud = new HUD();
        camera.setWorldSize(tileMap.getMapWidthPixels(), tileMap.getMapHeightPixels());

        offscreenBuffer = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        offscreenG2d = offscreenBuffer.createGraphics();
        offscreenG2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        loadClouds();
        loadTrees();
    }

    private void loadTrees() {
        treeBgImage = loadImage(
                "D:\\1- KULIAH TRPL\\Semester 2\\Z- Tugas Akhir PBO\\Utama\\craftpix-net-168228-free-tree-pixel-art-asset-pack\\PNG\\middle_lane_tree3.png");
    }

    private static BufferedImage loadImage(String path) { //Static Function
        try {
            File file = new File(path);
            if (file.exists()) {
                return ImageIO.read(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadClouds() {
        cloudImages = new BufferedImage[8];
        clouds = new ArrayList<>();
        try {
            String basePath = "D:\\1- KULIAH TRPL\\Semester 2\\Z- Tugas Akhir PBO\\Utama\\craftpix-net-801184-free-clouds-pixel-art-asset-pack\\PNG\\Clouds_gray\\";
            for (int i = 1; i <= 8; i++) {
                cloudImages[i - 1] = loadImage(basePath + "Shape" + i + "\\cloud_shape" + i + "_1.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        java.util.Random rnd = new java.util.Random();
        int mapWidth = tileMap.getMapWidthPixels();
        for (int i = 0; i < 25; i++) {
            float cx = rnd.nextInt(mapWidth > 0 ? mapWidth : 3000);
            float cy = 10 + rnd.nextInt(200); // Ketinggian awan di langit atas
            float cs = 0.1f + rnd.nextFloat() * 0.4f;
            int ct = rnd.nextInt(8);
            clouds.add(new Cloud(cx, cy, cs, ct));
        }
    }

    public void startGameLoop() {
        if (running)
            return;
        running = true;
        gameThread = new Thread(this);
        gameThread.setDaemon(true);
        gameThread.start();
    }

    @Override
    public void run() {
        long previousTime = System.nanoTime();
        long lag = 0;
        while (running) {
            long currentTime = System.nanoTime();
            long elapsed = currentTime - previousTime;
            previousTime = currentTime;
            lag += elapsed;
            while (lag >= FRAME_TIME_NS) {
                update();
                lag -= FRAME_TIME_NS;
            }
            render();
            fpsCounter++;
            if (System.nanoTime() - lastFpsTime >= 1_000_000_000L) {
                displayedFps = fpsCounter;
                fpsCounter = 0;
                lastFpsTime = System.nanoTime();
            }
            long sleepTime = FRAME_TIME_NS - (System.nanoTime() - currentTime);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1_000_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void update() {
        switch (currentState) {
            case GameState.MAIN_MENU:
                if (justPressed(KeyEvent.VK_ENTER))
                    startNewGame();
                break;
            case GameState.PLAYING:
                updatePlaying();
                break;
            case GameState.BOSS_ROOM:
                updateBossRoom();
                break;
            case GameState.PAUSED:
                if (justPressed(KeyEvent.VK_P) || justPressed(KeyEvent.VK_ESCAPE))
                    currentState = bossActive ? GameState.BOSS_ROOM : GameState.PLAYING;
                break;
            case GameState.GAME_OVER:
                if (justPressed(KeyEvent.VK_ENTER)) {
                    if (gameOverFromBoss) {
                        restartToLevel1();
                        gameOverFromBoss = false;
                    } else {
                        currentState = GameState.MAIN_MENU;
                    }
                }
                break;
            case GameState.WIN:
                if (justPressed(KeyEvent.VK_ENTER))
                    currentState = GameState.MAIN_MENU;
                break;
        }
        System.arraycopy(keys, 0, prevKeys, 0, keys.length);
    }

    private boolean justPressed(int k) {
        return keys[k] && !prevKeys[k];
    }

    private void startNewGame() {
        goblin = new Goblin(50f, 300f, tileMap);
        bossActive = false;
        deathDelayTimer = 0;
        gameOverFromBoss = false;
        floatTexts.clear();
        camera.reset();

        creeps = new ArrayList<>();
        powerUps = new ArrayList<>();
        fireballs = new ArrayList<>(); // Inisialisasi Api

        creeps.add(new Creep(300, 300, tileMap));
        creeps.add(new Creep(700, 300, tileMap));
        creeps.add(new Creep(1100, 300, tileMap));
        creeps.add(new Creep(1500, 300, tileMap));
        powerUps.add(new PowerUp(250, 100));
        powerUps.add(new PowerUp(1000, 150));

        weapon = new Weapon(2300, 300);
        boss = null;

        currentState = GameState.PLAYING;
    }

    private void restartToLevel1() {
        // Restart ke Level 1 ketika goblin mati di Entity.Boss Room
        tileMap.loadLevel(TileMap.LEVEL_1);
        camera.setWorldSize(tileMap.getMapWidthPixels(), tileMap.getMapHeightPixels());

        goblin = new Goblin(50f, 300f, tileMap);
        bossActive = false;
        deathDelayTimer = 0;
        gameOverFromBoss = false;
        floatTexts.clear();
        camera.reset();

        creeps = new ArrayList<>();
        powerUps = new ArrayList<>();
        fireballs = new ArrayList<>();

        creeps.add(new Creep(300, 300, tileMap));
        creeps.add(new Creep(700, 300, tileMap));
        creeps.add(new Creep(1100, 300, tileMap));
        creeps.add(new Creep(1500, 300, tileMap));
        powerUps.add(new PowerUp(250, 100));
        powerUps.add(new PowerUp(1000, 150));

        weapon = new Weapon(2300, 300);
        boss = null;

        currentState = GameState.PLAYING;
    }

    private void updatePlaying() {
        if (justPressed(KeyEvent.VK_P)) {
            currentState = GameState.PAUSED;
            return;
        }

        goblin.handleInput(keys);
        goblin.update();
        camera.follow(goblin.getCenterX(), goblin.getCenterY());

        // Goblin Nembak Api
        if (goblin.canShoot(keys)) {
            float sx = goblin.isFacingRight() ? goblin.getX() + goblin.getWidth() : goblin.getX() - 10;
            fireballs.add(new Fireball(sx, goblin.getY() + 10, goblin.isFacingRight(), true, tileMap));
            goblin.resetShootCooldown();
        }

        updateFireballs(); // Perbarui gerakan api

        // Masuk Pintu Entity.Boss
        if (goblin.isOnDoor() && !bossActive) {
            tileMap.loadLevel(TileMap.LEVEL_BOSS);
            camera.setWorldSize(tileMap.getMapWidthPixels(), tileMap.getMapHeightPixels());

            goblin.setX(50);
            goblin.setY(tileMap.tileToPixel(11) - goblin.getHeight());
            goblin.alignToGround();

            boss = new Boss(600, 250, tileMap);
            bossHp = boss.getMaxHp();
            bossMaxHp = boss.getMaxHp();

            creeps.clear();
            powerUps.clear();
            fireballs.clear(); // Hapus sisa api di map luar
            weapon = null;

            bossActive = true;
            currentState = GameState.BOSS_ROOM;
        }

        if (goblin.isDead()) {
            if (++deathDelayTimer >= DEATH_DELAY) {
                currentState = GameState.GAME_OVER;
                deathDelayTimer = 1;
            }
        }

        updateFloatTexts();
        updatePowerUps();
        updateCreeps();

        // Ambil Senjata
        if (weapon != null && !weapon.isCollected()) {
            if (goblin.collidesWith(weapon)) {
                weapon.collect();
                goblin.pickupWeapon();
                goblin.addScore(200);
                spawnScoreText(200, weapon.getX(), weapon.getY() - 10);
            }
        }
    }

    private void updateBossRoom() {
        if (justPressed(KeyEvent.VK_P)) {
            currentState = GameState.PAUSED;
            return;
        }

        goblin.handleInput(keys);
        goblin.update();
        camera.follow(goblin.getCenterX(), goblin.getCenterY());

        if (boss != null && !boss.isDead()) {
            boss.update();
            bossHp = boss.getHp();

            // Goblin Nembak Api di Entity.Boss Room
            if (goblin.canShoot(keys)) {
                float sx = goblin.isFacingRight() ? goblin.getX() + goblin.getWidth() : goblin.getX() - 10;
                fireballs.add(new Fireball(sx, goblin.getY() + 10, goblin.isFacingRight(), true, tileMap));
                goblin.resetShootCooldown();
            }

            // Entity.Boss Nembak Api
            if (boss.readyToShoot()) {
                float bx = boss.isFacingRight() ? boss.getX() + boss.getWidth() : boss.getX() - 20;
                fireballs.add(new Fireball(bx, boss.getY() + 20, boss.isFacingRight(), false, tileMap));
            }

            if (goblin.collidesWith(boss)) {
                if (justPressed(KeyEvent.VK_Z) && goblin.hasWeapon()) {
                    boss.takeDamage(20);
                    spawnScoreText(150, boss.getX(), boss.getY());
                    goblin.setVelX(goblin.isFacingRight() ? -5 : 5);

                    if (boss.isDead()) {
                        goblin.addScore(1000);
                        currentState = GameState.WIN;
                    }
                } else {
                    goblin.takeDamage();
                }
            }
        }

        updateFireballs(); // Perbarui gerakan api di Entity.Boss Room

        if (goblin.isDead()) {
            if (++deathDelayTimer >= DEATH_DELAY) {
                // Tampilkan Game Over screen sebelum restart
                currentState = GameState.GAME_OVER;
                gameOverFromBoss = true;
                deathDelayTimer = 0;
            }
        }
        updateFloatTexts();
    }

    private void updateFireballs() {
        Iterator<Fireball> it = fireballs.iterator();
        while (it.hasNext()) {
            Fireball f = it.next();
            f.update();

            if (!f.isAlive()) {
                it.remove();
                continue;
            }

            if (f.isFromPlayer()) {
                if (bossActive && boss != null && !boss.isDead() && f.collidesWith(boss)) {
                    boss.takeDamage(10);
                    spawnScoreText(50, boss.getX(), boss.getY() - 10);
                    f.alive = false;
                    if (boss.isDead()) {
                        goblin.addScore(1000);
                        currentState = GameState.WIN;
                    }
                }
                for (Creep c : creeps) {
                    if (!c.isDead() && f.collidesWith(c)) {
                        c.die();
                        goblin.addScore(100);
                        spawnScoreText(100, c.getX(), c.getY());
                        f.alive = false;
                        break;
                    }
                }
            } else {
                if (!goblin.isDead() && f.collidesWith(goblin)) {
                    goblin.takeDamage();
                    f.alive = false;
                }
            }
        }
    }

    private void updatePowerUps() {
        Iterator<PowerUp> it = powerUps.iterator();
        while (it.hasNext()) {
            PowerUp p = it.next();
            p.update();
            if (!p.isCollected() && goblin.collidesWith(p)) {
                p.collect();
                goblin.activateSuper();
                goblin.addScore(50);
                spawnScoreText(50, p.getX(), p.getY() - 10);
            }
            if (p.isCollected())
                it.remove();
        }
    }

    private void updateCreeps() {
        Iterator<Creep> it = creeps.iterator();
        while (it.hasNext()) {
            Creep c = it.next();
            c.update();
            if (c.isReadyToRemove()) {
                it.remove();
                continue;
            }
            if (!c.isDead() && !goblin.isDead() && goblin.collidesWith(c)) {
                if (goblin.getVelY() > 0 && goblin.getY() + goblin.getHeight() < c.getY() + c.getHeight() / 2 + 5) {
                    c.die();
                    goblin.setVelY(-10);
                    goblin.addScore(100);
                    spawnScoreText(100, c.getX(), c.getY());
                } else {
                    goblin.takeDamage();
                }
            }
        }
    }

    private void updateFloatTexts() {
        Iterator<FloatText> it = floatTexts.iterator();
        while (it.hasNext()) {
            FloatText ft = it.next();
            ft.y -= 0.8f;
            if (--ft.life <= 0)
                it.remove();
        }
    }

    public void spawnScoreText(int points, float worldX, float worldY) {
        Color c = points >= 100 ? new Color(255, 200, 50) : points >= 50 ? new Color(180, 255, 100) : Color.WHITE;
        floatTexts.add(new FloatText("+" + points, worldX, worldY, c));
    }

    private void render() {
        Color sky = (currentState == GameState.BOSS_ROOM || bossActive) ? new Color(20, 5, 25) : new Color(45, 60, 40);
        offscreenG2d.setColor(sky);
        offscreenG2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        switch (currentState) {
            case GameState.MAIN_MENU:
                hud.drawMainMenu(offscreenG2d, SCREEN_WIDTH, SCREEN_HEIGHT);
                break;
            case GameState.PLAYING:
                renderWorldAndHUD();
                break;
            case GameState.BOSS_ROOM:
                renderWorldAndHUD();
                break;
            case GameState.PAUSED:
                renderWorldAndHUD();
                hud.drawPaused(offscreenG2d, SCREEN_WIDTH, SCREEN_HEIGHT);
                break;
            case GameState.GAME_OVER:
                renderWorldAndHUD();
                hud.drawGameOver(offscreenG2d, SCREEN_WIDTH, SCREEN_HEIGHT, safeScore());
                break;
            case GameState.WIN:
                renderWorldAndHUD();
                hud.drawWin(offscreenG2d, SCREEN_WIDTH, SCREEN_HEIGHT, safeScore());
                break;
        }

        renderFpsCounter();
        Graphics g = getGraphics();
        if (g != null) {
            g.drawImage(offscreenBuffer, 0, 0, null);
            g.dispose();
        }
    }

    private int safeScore() {
        return goblin != null ? goblin.getScore() : 0;
    }

    private void renderWorldAndHUD() {
        if (goblin == null)
            return;
        int camX = camera.getOffsetX();
        int camY = camera.getOffsetY();

        if (currentState != GameState.BOSS_ROOM)
            renderClouds(camX / 3, camY / 5);
        tileMap.render(offscreenG2d, camX, camY, SCREEN_WIDTH, SCREEN_HEIGHT);

        for (PowerUp p : powerUps)
            p.draw(offscreenG2d, camera);
        for (Creep c : creeps)
            c.draw(offscreenG2d, camera);

        if (weapon != null)
            weapon.draw(offscreenG2d, camera);

        if (fireballs != null) {
            for (Fireball f : fireballs)
                f.draw(offscreenG2d, camera);
        }

        if (bossActive && boss != null)
            boss.draw(offscreenG2d, camera);

        goblin.draw(offscreenG2d, camera);
        renderFloatTexts(camX, camY);

        hud.render(offscreenG2d, SCREEN_WIDTH, SCREEN_HEIGHT, goblin.getHp(), goblin.getScore(),
                bossActive, bossHp, bossMaxHp, goblin.isSuper(), goblin.hasWeapon());
    }

    private void renderFloatTexts(int camX, int camY) {
        offscreenG2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        for (FloatText ft : floatTexts) {
            int sx = (int) ft.x - camX;
            int sy = (int) ft.y - camY;
            int alpha = Math.min(255, ft.life * 13);
            Color c = new Color(ft.color.getRed(), ft.color.getGreen(), ft.color.getBlue(), alpha);
            offscreenG2d.setColor(new Color(0, 0, 0, alpha / 2));
            offscreenG2d.drawString(ft.text, sx + 1, sy + 1);
            offscreenG2d.setColor(c);
            offscreenG2d.drawString(ft.text, sx, sy);
        }
    }

    private void renderClouds(int parallaxOffsetX, int parallaxOffsetY) {
        offscreenG2d.setColor(new Color(200, 200, 100, 180));
        int moonX = 700 - (parallaxOffsetX / 8);
        int moonY = 50 - (parallaxOffsetY / 8);
        offscreenG2d.fillOval(moonX, moonY, 70, 70);

        if (clouds != null) {
            int mapWidth = tileMap.getMapWidthPixels();
            if (mapWidth <= 0)
                mapWidth = 3000;

            for (Cloud c : clouds) {
                c.x += c.speed;
                if (c.x > mapWidth)
                    c.x = -200;

                int screenX = (int) (c.x - (parallaxOffsetX / 4));
                int screenY = (int) (c.y - (parallaxOffsetY / 8)) - 50;

                screenX = screenX % mapWidth;
                if (screenX < -200)
                    screenX += mapWidth;
                else if (screenX > mapWidth)
                    screenX -= mapWidth;

                if (cloudImages != null && cloudImages[c.type] != null) {
                    offscreenG2d.drawImage(cloudImages[c.type], screenX, screenY, null);
                }
            }
        }

        if (treeBgImage != null) {
            int scale = 2;
            int tw = treeBgImage.getWidth() * scale;
            int th = treeBgImage.getHeight() * scale;

            int mapCenter = tileMap.getMapWidthPixels() / 2;
            int worldX = mapCenter - (tw / 2);
            int startX = worldX - parallaxOffsetX;
            int groundWorldY = 15 * TileMap.TILE_SIZE;
            int cy = groundWorldY - th - parallaxOffsetY;

            offscreenG2d.drawImage(treeBgImage, startX, cy, tw, th, null);
        }
    }

    void renderFpsCounter() {
        offscreenG2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
        offscreenG2d.drawString("FPS: " + displayedFps, 6, SCREEN_HEIGHT - 6);
    }

    public static boolean aabbCollides(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k >= 0 && k < keys.length)
            keys[k] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k >= 0 && k < keys.length)
            keys[k] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (offscreenBuffer != null)
            g.drawImage(offscreenBuffer, 0, 0, null);
    }

    public int getCurrentState() {
        return currentState;
    }

    public TileMap getTileMap() {
        return tileMap;
    }

    public Camera getCamera() {
        return camera;
    }

    public Goblin getGoblin() {
        return goblin;
    }

    public boolean[] getKeys() {
        return keys;
    }

    public void setCurrentState(int s) {
        currentState = s;
    }

    public void setBossActive(boolean b, int hp, int max) {
        bossActive = b;
        bossHp = hp;
        bossMaxHp = max;
    }

    public void updateBossHp(int hp) {
        bossHp = hp;
    }

    public void triggerWin() {
        currentState = GameState.WIN;
    }
}