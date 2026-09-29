
// ============================================================
//  HUD.java
//  Heads-Up Display: semua UI yang digambar di ATAS dunia.
//  HUD tidak ikut digeser oleh kamera — selalu di posisi tetap.
//
//  Yang digambar:
//    - Ikon hati (HP) Goblin
//    - Skor saat ini
//    - Entity.Boss health bar (hanya di state BOSS_ROOM)
//    - Indikator power-up aktif (SUPER / WEAPON)
// ============================================================
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class HUD {

    // ──────────────────────────────────────────────
    // Konstanta visual
    // ──────────────────────────────────────────────
    private static final int MAX_HEARTS = 5;
    private static final int HEART_SIZE = 22;
    private static final int HEART_GAP = 6;
    private static final int HUD_PAD = 14;

    private static final Color COLOR_HEART_FULL = new Color(230, 50, 70);
    private static final Color COLOR_HEART_EMPTY = new Color(80, 30, 30, 120);
    private static final Color COLOR_SCORE = new Color(255, 220, 60);
    private static final Color COLOR_SCORE_SHADOW = new Color(0, 0, 0, 160);
    private static final Color COLOR_BOSS_BG = new Color(20, 0, 0, 200);
    private static final Color COLOR_BOSS_BAR = new Color(220, 0, 50);
    private static final Color COLOR_BOSS_SHINE = new Color(255, 80, 80, 80);
    private static final Color COLOR_SUPER_BADGE = new Color(50, 200, 100, 200);
    private static final Color COLOR_WEAPON_BADGE = new Color(200, 150, 50, 200);

    // ──────────────────────────────────────────────
    // Render utama HUD (dipanggil setiap frame)
    // ──────────────────────────────────────────────
    public void render(Graphics2D g2d, int screenWidth, int screenHeight,
            int currentHp, int score, boolean isBossActive,
            int bossHp, int bossMaxHp,
            boolean isSuper, boolean hasWeapon) {

        // Simpan state antialiasing, restore nanti
        Object oldAA = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawHearts(g2d, currentHp);
        drawScore(g2d, score, screenWidth);

        if (isBossActive) {
            drawBossBar(g2d, bossHp, bossMaxHp, screenWidth, screenHeight);
        }

        drawPowerUpBadges(g2d, isSuper, hasWeapon, screenHeight);

        // Restore state
        if (oldAA != null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
        }
    }

    // ──────────────────────────────────────────────
    // Gambar ikon hati HP
    // ──────────────────────────────────────────────
    private void drawHearts(Graphics2D g2d, int currentHp) {
        for (int i = 0; i < MAX_HEARTS; i++) {
            int x = HUD_PAD + i * (HEART_SIZE + HEART_GAP);
            int y = HUD_PAD;
            boolean full = i < currentHp;
            drawHeart(g2d, x, y, HEART_SIZE, full);
        }
    }

    /**
     * Menggambar ikon hati menggunakan dua lingkaran + segitiga (pixel art style).
     * full = hati merah penuh; false = hati abu-abu/kosong.
     */
    private void drawHeart(Graphics2D g2d, int x, int y, int size, boolean full) {
        Color fill = full ? COLOR_HEART_FULL : COLOR_HEART_EMPTY;
        Color shadow = full ? new Color(150, 20, 40) : new Color(40, 10, 10, 80);

        int half = size / 2;
        int qrt = size / 4;

        // Bayangan sedikit di bawah
        g2d.setColor(new Color(0, 0, 0, 60));
        drawHeartShape(g2d, x + 1, y + 2, size);

        // Isi hati
        g2d.setColor(fill);
        drawHeartShape(g2d, x, y, size);

        // Highlight kecil di kiri atas
        if (full) {
            g2d.setColor(new Color(255, 180, 180, 150));
            g2d.fillOval(x + qrt - 1, y + qrt - 1, qrt, qrt - 1);
        }
    }

    /** Menggambar bentuk hati menggunakan filled polygon. */
    private void drawHeartShape(Graphics2D g2d, int x, int y, int size) {
        // Titik poligon bentuk hati (pixel art approximation)
        int[] px = {
                x, x + size / 4, x + size / 2, x + 3 * size / 4, x + size,
                x + size, x + 3 * size / 4, x + size / 2, x + size / 4, x
        };
        int[] py = {
                y + size / 3, y, y + size / 4, y, y + size / 3,
                y + size * 2 / 3, y + size, y + 3 * size / 4, y + size, y + size * 2 / 3
        };

        // Cara mudah: pakai dua oval + satu poligon segitiga
        int half = size / 2;
        int qrt = size / 4;
        // Dua lingkaran di atas
        g2d.fillOval(x, y, half, half);
        g2d.fillOval(x + half, y, half, half);
        // Segitiga menunjuk ke bawah
        int[] tx = { x, x + size, x + half };
        int[] ty = { y + qrt, y + qrt, y + size };
        g2d.fillPolygon(tx, ty, 3);
    }

    // ──────────────────────────────────────────────
    // Gambar skor
    // ──────────────────────────────────────────────
    private void drawScore(Graphics2D g2d, int score, int screenWidth) {
        String text = "SCORE: " + score;
        Font font = new Font("Monospaced", Font.BOLD, 16);
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(text);
        int tx = screenWidth - tw - HUD_PAD;
        int ty = HUD_PAD + 16;

        // Shadow
        g2d.setColor(COLOR_SCORE_SHADOW);
        g2d.drawString(text, tx + 2, ty + 2);
        // Teks utama
        g2d.setColor(COLOR_SCORE);
        g2d.drawString(text, tx, ty);
    }

    // ──────────────────────────────────────────────
    // Gambar boss health bar di bagian bawah layar
    // ──────────────────────────────────────────────
    private void drawBossBar(Graphics2D g2d, int bossHp, int bossMaxHp,
            int screenWidth, int screenHeight) {
        int barWidth = screenWidth - 80;
        int barHeight = 18;
        int barX = 40;
        int barY = screenHeight - 40;

        // Background gelap
        g2d.setColor(COLOR_BOSS_BG);
        g2d.fillRoundRect(barX - 4, barY - 4, barWidth + 8, barHeight + 8, 6, 6);

        // Bar merah (HP saat ini)
        float ratio = (float) Math.max(0, bossHp) / bossMaxHp;
        int fillW = (int) (barWidth * ratio);

        if (fillW > 0) {
            g2d.setColor(COLOR_BOSS_BAR);
            g2d.fillRoundRect(barX, barY, fillW, barHeight, 4, 4);

            // Shine effect di atas bar
            g2d.setColor(COLOR_BOSS_SHINE);
            g2d.fillRoundRect(barX, barY, fillW, barHeight / 2, 4, 4);
        }

        // Border bar
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.drawRoundRect(barX, barY, barWidth, barHeight, 4, 4);

        // Label "BOSS"
        g2d.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2d.setColor(new Color(255, 120, 120));
        g2d.drawString("BOSS", barX, barY - 6);

        // Persentase HP
        String pct = (int) (ratio * 100) + "%";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);
        g2d.drawString(pct, barX + barWidth - fm.stringWidth(pct), barY - 6);
    }

    // ──────────────────────────────────────────────
    // Badge power-up aktif
    // ──────────────────────────────────────────────
    private void drawPowerUpBadges(Graphics2D g2d, boolean isSuper,
            boolean hasWeapon, int screenHeight) {
        int badgeY = screenHeight - 70;
        int badgeX = 14;

        if (isSuper) {
            drawBadge(g2d, badgeX, badgeY, "SUPER!", COLOR_SUPER_BADGE);
            badgeX += 80;
        }
        if (hasWeapon) {
            drawBadge(g2d, badgeX, badgeY, "ARMED", COLOR_WEAPON_BADGE);
        }
    }

    private void drawBadge(Graphics2D g2d, int x, int y, String label, Color bg) {
        g2d.setFont(new Font("Monospaced", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(label);
        int pad = 6;

        g2d.setColor(bg);
        g2d.fillRoundRect(x, y, tw + pad * 2, 18, 6, 6);
        g2d.setColor(Color.WHITE);
        g2d.drawString(label, x + pad, y + 13);
    }

    // ──────────────────────────────────────────────
    // Layar Main Menu
    // ──────────────────────────────────────────────
    public void drawMainMenu(Graphics2D g2d, int screenWidth, int screenHeight) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Background gradien manual (dua rect)
        g2d.setColor(new Color(15, 10, 30));
        g2d.fillRect(0, 0, screenWidth, screenHeight);
        g2d.setColor(new Color(30, 15, 50, 200));
        g2d.fillRect(0, screenHeight / 2, screenWidth, screenHeight / 2);

        // Bintang dekoratif (titik-titik kecil)
        g2d.setColor(new Color(255, 255, 255, 60));
        int[][] stars = { { 50, 30 }, { 120, 80 }, { 200, 20 }, { 300, 60 }, { 400, 15 },
                { 480, 90 }, { 550, 40 }, { 620, 70 }, { 700, 25 } };
        for (int[] s : stars) {
            if (s[0] < screenWidth) {
                g2d.fillOval(s[0], s[1], 2, 2);
            }
        }

        // Judul game
        g2d.setFont(new Font("Monospaced", Font.BOLD, 36));
        drawCenteredShadowText(g2d, "LITTLE GOBLIN", screenWidth, 140,
                new Color(120, 220, 80), new Color(0, 80, 0));

        g2d.setFont(new Font("Monospaced", Font.BOLD, 36));
        drawCenteredShadowText(g2d, "ADVENTURE", screenWidth, 185,
                new Color(120, 220, 80), new Color(0, 80, 0));

        // Goblin kecil dekoratif di tengah
        drawMenuGoblin(g2d, screenWidth / 2 - 75, 165);

        // Instruksi
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.setColor(new Color(200, 200, 150));
        drawCenteredText(g2d, "Arrow Keys / WASD  =  Move & Jump", screenWidth, 310);
        drawCenteredText(g2d, "SHIFT  =  Super Jump   |   X  =  Attack", screenWidth, 332);
        drawCenteredText(g2d, "P  =  Pause", screenWidth, 354);

        // Tombol START (berkedip)
        long t = System.currentTimeMillis();
        boolean blink = (t / 500) % 2 == 0;
        if (blink) {
            g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
            drawCenteredShadowText(g2d, "[ PRESS ENTER TO START ]", screenWidth, 410,
                    new Color(255, 220, 60), new Color(120, 80, 0));
        }

        // Versi kecil
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2d.setColor(new Color(100, 100, 100));
        g2d.drawString("v1.0  -  Java OOP Demo", 10, screenHeight - 10);
    }

    // ──────────────────────────────────────────────
    // Layar Game Over
    // ──────────────────────────────────────────────
    public void drawGameOver(Graphics2D g2d, int screenWidth, int screenHeight,
            int finalScore) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Overlay gelap
        g2d.setColor(new Color(0, 0, 0, 190));
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 42));
        drawCenteredShadowText(g2d, "GAME OVER", screenWidth, screenHeight / 2 - 50,
                new Color(220, 50, 50), new Color(80, 0, 0));

        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        drawCenteredText(g2d, "Final Score: " + finalScore,
                screenWidth, screenHeight / 2 + 10, new Color(255, 220, 60));

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 15));
        g2d.setColor(new Color(200, 200, 200));
        drawCenteredText(g2d, "Press ENTER to return to menu",
                screenWidth, screenHeight / 2 + 50);
    }

    // ──────────────────────────────────────────────
    // Layar WIN
    // ──────────────────────────────────────────────
    public void drawWin(Graphics2D g2d, int screenWidth, int screenHeight,
            int finalScore) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 38));
        drawCenteredShadowText(g2d, "YOU WIN!", screenWidth, screenHeight / 2 - 60,
                new Color(100, 230, 80), new Color(0, 80, 0));

        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        drawCenteredText(g2d, "Entity.Boss defeated! Final Score: " + finalScore,
                screenWidth, screenHeight / 2, new Color(255, 220, 60));

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 15));
        g2d.setColor(new Color(200, 200, 200));
        drawCenteredText(g2d, "Press ENTER to return to menu",
                screenWidth, screenHeight / 2 + 45);
    }

    // ──────────────────────────────────────────────
    // Overlay PAUSED
    // ──────────────────────────────────────────────
    public void drawPaused(Graphics2D g2d, int screenWidth, int screenHeight) {
        g2d.setColor(new Color(0, 0, 0, 130));
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 32));
        drawCenteredShadowText(g2d, "PAUSED", screenWidth, screenHeight / 2,
                new Color(200, 200, 200), new Color(60, 60, 60));

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.setColor(new Color(160, 160, 160));
        drawCenteredText(g2d, "Press P to resume", screenWidth, screenHeight / 2 + 30);
    }

    // ──────────────────────────────────────────────
    // ──────────────────────────────────────────────
    // Helper: Goblin pixel art kecil untuk menu
    // ──────────────────────────────────────────────
    private static final String MENU_SPRITE_ROOT = "D:\\UGM\\SEM 2\\PBO & PPBO\\KITA PAKAI KODE YANG INI GAMENYA\\LittleGoblinSimpel2\\craftpix-064112-free-orc-ogre-and-goblin-chibi-2d-game-sprites\\Goblin\\PNG\\PNG Sequences";

    private static final BufferedImage[] MENU_IDLE_SPRITES = loadMenuSprites("Idle Blinking");

    private static BufferedImage[] loadMenuSprites(String subfolder) {
        File dir = new File(MENU_SPRITE_ROOT, subfolder);
        if (!dir.exists() || !dir.isDirectory()) {
            return new BufferedImage[0];
        }
        File[] files = dir.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) {
            return new BufferedImage[0];
        }
        java.util.Arrays.sort(files, java.util.Comparator.comparing(File::getName));
        BufferedImage[] frames = new BufferedImage[files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                frames[i] = ImageIO.read(files[i]);
            } catch (IOException e) {
                frames[i] = null;
            }
        }
        return frames;
    }

    private void drawMenuGoblin(Graphics2D g2d, int x, int y) {
        if (MENU_IDLE_SPRITES != null && MENU_IDLE_SPRITES.length > 0) {
            int frameIndex = (int) ((System.currentTimeMillis() / 150) % MENU_IDLE_SPRITES.length);
            if (MENU_IDLE_SPRITES[frameIndex] != null) {
                g2d.drawImage(MENU_IDLE_SPRITES[frameIndex], x, y, 150, 150, null);
                return;
            }
        }
        // Fallback: simple green rectangle if sprites not loaded
        g2d.setColor(new Color(70, 160, 50));
        g2d.fillRoundRect(x + 5, y + 14, 20, 22, 6, 6);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + 2, y, 26, 22);
    }

    // ──────────────────────────────────────────────
    // Helper: Text utilities
    // ──────────────────────────────────────────────
    private void drawCenteredText(Graphics2D g2d, String text,
            int screenWidth, int y) {
        drawCenteredText(g2d, text, screenWidth, y, null);
    }

    private void drawCenteredText(Graphics2D g2d, String text,
            int screenWidth, int y, Color color) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (screenWidth - fm.stringWidth(text)) / 2;
        if (color != null)
            g2d.setColor(color);
        g2d.drawString(text, x, y);
    }

    private void drawCenteredShadowText(Graphics2D g2d, String text,
            int screenWidth, int y,
            Color mainColor, Color shadowColor) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (screenWidth - fm.stringWidth(text)) / 2;
        g2d.setColor(shadowColor);
        g2d.drawString(text, x + 3, y + 3);
        g2d.setColor(mainColor);
        g2d.drawString(text, x, y);
    }
}