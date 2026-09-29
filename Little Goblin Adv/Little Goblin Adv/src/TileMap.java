import java.awt.*;

public class TileMap {
    public static final int AIR           = 0;
    public static final int GROUND        = 1;
    public static final int PLATFORM      = 2;
    public static final int WALL          = 3;
    public static final int DANGER        = 4;
    public static final int BOSS_TRIGGER  = 5; // (Sudah tidak dipakai, diganti Pintu)
    public static final int DECORATION    = 6;
    public static final int CHECKPOINT    = 7; // TILE BARU: Bendera
    public static final int DOOR          = 8; // TILE BARU: Pintu Entity.Boss

    public static final int TILE_SIZE = 32;

    // LEVEL 1: Ujung kanannya (Kolom 75-76) sekarang ada Bendera (7) dan Pintu (8)
    public static final int[][] LEVEL_1 = {
            // Baris 0 - 9: Langit
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // 0
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // 1
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // 2
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // 3
            {0,0,0,0,0,0,0,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,2,0,0,0,0,0,0,0,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // 4
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // 5
            {0,0,0,0,0,0,0,0,0,0,0,0,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // 6
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // 7
            {0,0,0,0,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,0,0,0,0,0,0,0,2,2,2,0,0,0,0,0,0,0,0,0,0,0,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // 8
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // 9

            // Baris 10: Bendera Entity.Boss (7)
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,0,0,0,0}, // 10

            // Baris 11: DURI (4) di jurang ke-2, Tiang Bendera (7), Pintu Bos (8)
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,8,0,0,0}, // 11

            // Baris 12: RUMPUT HIJAU (1). Jurang 1 dibikin bolong (0), Jurang 2 dikasih pijakan tanah (3) di bawah durinya
            {1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,3,3,3,3,1,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,3}, // 12

            // Baris 13-19: TANAH DALAM (3). Jurang 1 bolong tembus bawah (0) untuk mati instan
            {3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3}, // 13
            {3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3}, // 14
            {3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3}, // 15
            {3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3}, // 16
            {3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3}, // 17
            {3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3}, // 18
            {3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,0,0,0,3,3,3,3,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3}  // 19
    };

    // LEVEL BARU: ARENA BOSS (Satu Layar Penuh Tertutup Tembok)
    public static final int[][] LEVEL_BOSS = {
            {3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3},
            {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
            {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
            {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
            {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
            {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
            {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
            {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
            {3,0,0,0,0,2,2,0,0,0,0,0,0,0,0,0,0,0,2,2,0,0,0,0,3}, // Platform lompatan di arena
            {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
            {3,8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3}, // Pintu masuk di kiri
            {3,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,3}, // Lantai arena
            {3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3},
            {3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3},
            {3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3}
    };

    private int[][] mapData;
    private int rows;
    private int cols;

    // ... (Kode warna biarkan tetap sama)
    private static final Color COLOR_GROUND    = new Color(101, 67, 33);
    private static final Color COLOR_GROUND_TOP= new Color(76, 153, 0);
    private static final Color COLOR_PLATFORM  = new Color(139, 90, 43);
    private static final Color COLOR_WALL      = new Color(80, 80, 80);
    private static final Color COLOR_DANGER    = new Color(220, 50, 50);

    public TileMap() { loadLevel(LEVEL_1); }

    public void loadLevel(int[][] data) {
        this.mapData = data;
        this.rows    = data.length;
        this.cols    = data[0].length;
    }

    public int getTile(int col, int row) {
        if (col < 0 || col >= cols || row < 0) return WALL;
        if (row >= rows) return AIR;
        return mapData[row][col];
    }

    public boolean isSolid(int col, int row) {
        int t = getTile(col, row);
        return t == GROUND || t == WALL || t == PLATFORM;
    }
    public boolean isPlatform(int col, int row) { return getTile(col, row) == PLATFORM; }
    public boolean isDanger(int col, int row) { return getTile(col, row) == DANGER; }

    public int pixelToTile(int pixel) { return pixel / TILE_SIZE; }
    public int tileToPixel(int tile) { return tile * TILE_SIZE; }
    public int getMapWidthPixels()  { return cols * TILE_SIZE; }
    public int getMapHeightPixels() { return rows * TILE_SIZE; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }

    public void render(Graphics2D g2d, int cameraX, int cameraY, int screenW, int screenH) {
        int startCol = Math.max(0, cameraX / TILE_SIZE);
        int endCol   = Math.min(cols - 1, (cameraX + screenW) / TILE_SIZE + 1);
        int startRow = Math.max(0, cameraY / TILE_SIZE);
        int endRow   = Math.min(rows - 1, (cameraY + screenH) / TILE_SIZE + 1);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                int tileType = mapData[row][col];
                if (tileType == AIR) continue;
                int drawX = col * TILE_SIZE - cameraX;
                int drawY = row * TILE_SIZE - cameraY;
                drawTile(g2d, tileType, drawX, drawY);
            }
        }
    }

    private void drawTile(Graphics2D g2d, int type, int x, int y) {
        int S = TILE_SIZE;
        switch (type) {
            case GROUND -> {
                g2d.setColor(COLOR_GROUND); g2d.fillRect(x, y, S, S);
                g2d.setColor(COLOR_GROUND_TOP); g2d.fillRect(x, y, S, 6);
                g2d.setColor(new Color(0, 0, 0, 40)); g2d.drawRect(x, y, S, S);
            }
            case PLATFORM -> {
                g2d.setColor(COLOR_PLATFORM); g2d.fillRect(x, y, S, S);
                g2d.setColor(new Color(255, 255, 255, 60)); g2d.fillRect(x, y, S, 5);
                g2d.setColor(new Color(0, 0, 0, 30));
                for (int i = 8; i < S; i += 10) g2d.drawLine(x + i, y, x + i, y + S);
                g2d.setColor(new Color(0, 0, 0, 60)); g2d.drawRect(x, y, S, S);
            }
            case WALL -> {
                g2d.setColor(COLOR_WALL); g2d.fillRect(x, y, S, S);
                g2d.setColor(new Color(100, 100, 100));
                int halfS = S / 2;
                g2d.drawRect(x, y, S, halfS); g2d.drawRect(x, y + halfS, S, halfS);
                g2d.drawLine(x + halfS, y, x + halfS, y + halfS);
                g2d.drawLine(x + S / 4, y + halfS, x + S / 4, y + S);
                g2d.drawLine(x + 3 * S / 4, y + halfS, x + 3 * S / 4, y + S);
            }
            case DANGER -> {
                g2d.setColor(new Color(60, 60, 60)); g2d.fillRect(x, y + S / 2, S, S / 2);
                g2d.setColor(COLOR_DANGER);
                int[] xp1 = {x + 4, x + 10, x + 7}; int[] yp1 = {y + S, y + S, y + S / 3};
                int[] xp2 = {x + S/2-3, x + S/2+3, x + S/2}; int[] yp2 = {y + S, y + S, y + S / 3};
                int[] xp3 = {x + S-10, x + S - 4, x + S - 7}; int[] yp3 = {y + S, y + S, y + S / 3};
                g2d.fillPolygon(xp1, yp1, 3); g2d.fillPolygon(xp2, yp2, 3); g2d.fillPolygon(xp3, yp3, 3);
            }
            case CHECKPOINT -> {
                // Tiang
                g2d.setColor(Color.LIGHT_GRAY); g2d.fillRect(x + 14, y, 4, S);
                // Bendera Segitiga Merah
                g2d.setColor(Color.RED);
                int[] fx = {x + 18, x + 30, x + 18};
                int[] fy = {y + 2, y + 10, y + 18};
                g2d.fillPolygon(fx, fy, 3);
            }
            case DOOR -> {
                // Pintu Kayu
                g2d.setColor(new Color(90, 50, 20)); g2d.fillRect(x + 4, y + 4, S - 8, S - 4);
                // Gagang Pintu
                g2d.setColor(Color.YELLOW); g2d.fillOval(x + 22, y + S / 2, 5, 5);
                g2d.setColor(Color.BLACK); g2d.drawRect(x + 4, y + 4, S - 8, S - 4);
            }
        }
    }
}