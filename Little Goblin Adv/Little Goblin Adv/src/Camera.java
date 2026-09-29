// ============================================================
//  Camera.java
//  Mengatur viewport (jendela pandang) yang mengikuti Goblin.
//
//  Cara kerja:
//    - Camera menyimpan offsetX & offsetY (posisi sudut
//      kiri-atas dunia yang sedang terlihat di layar).
//    - follow() menghitung offset baru agar target selalu
//      berada di tengah layar, dibatasi oleh batas dunia.
//    - Semua objek digambar di koordinat layar:
//        screenX = worldX - cameraOffsetX
//        screenY = worldY - cameraOffsetY
// ============================================================
public class Camera {

    // ──────────────────────────────────────────────
    // Fields
    // ──────────────────────────────────────────────
    private int offsetX;
    private int offsetY;

    private final int screenWidth;
    private final int screenHeight;

    private int worldWidth;
    private int worldHeight;

    // Lerp (linear interpolation) speed untuk gerakan kamera halus
    // Nilai 1.0 = kamera instan, 0.1 = kamera sangat lambat/halus
    private static final float LERP = 0.12f;

    // Target posisi kamera (sebelum lerp diterapkan)
    private float targetX;
    private float targetY;

    // ──────────────────────────────────────────────
    // Constructor
    // ──────────────────────────────────────────────
    public Camera(int screenWidth, int screenHeight) {
        this.screenWidth  = screenWidth;
        this.screenHeight = screenHeight;
        this.offsetX = 0;
        this.offsetY = 0;
        this.targetX = 0;
        this.targetY = 0;
    }

    // ──────────────────────────────────────────────
    // Setup batas dunia
    // ──────────────────────────────────────────────
    public void setWorldSize(int worldWidth, int worldHeight) {
        this.worldWidth  = worldWidth;
        this.worldHeight = worldHeight;
    }

    // ──────────────────────────────────────────────
    // Update kamera agar mengikuti target
    // targetWorldX / targetWorldY = koordinat DUNIA objek target
    // (biasanya titik tengah Goblin)
    // ──────────────────────────────────────────────
    public void follow(int targetWorldX, int targetWorldY) {
        // Kita ingin target berada di tengah layar
        targetX = targetWorldX - (float) screenWidth  / 2;
        targetY = targetWorldY - (float) screenHeight / 2;

        // Klem agar kamera tidak keluar batas dunia
        targetX = Math.max(0, Math.min(targetX, worldWidth  - screenWidth));
        targetY = Math.max(0, Math.min(targetY, worldHeight - screenHeight));

        // Lerp untuk transisi halus
        offsetX += (int) ((targetX - offsetX) * LERP);
        offsetY += (int) ((targetY - offsetY) * LERP);
    }

    // ──────────────────────────────────────────────
    // Konversi koordinat
    // ──────────────────────────────────────────────

    /** Ubah koordinat dunia ke koordinat layar. */
    public int worldToScreenX(int worldX) { return worldX - offsetX; }
    public int worldToScreenY(int worldY) { return worldY - offsetY; }

    /** Ubah koordinat layar ke koordinat dunia. */
    public int screenToWorldX(int screenX) { return screenX + offsetX; }
    public int screenToWorldY(int screenY) { return screenY + offsetY; }

    // ──────────────────────────────────────────────
    // Visibility Check
    // ──────────────────────────────────────────────

    /**
     * Cek apakah sebuah objek (dalam koordinat dunia) terlihat
     * di dalam viewport saat ini. Gunakan ini untuk culling:
     * jangan update/render objek yang tidak terlihat.
     */
    public boolean isVisible(int worldX, int worldY, int w, int h) {
        return worldX + w >= offsetX
                && worldX    <= offsetX + screenWidth
                && worldY + h >= offsetY
                && worldY    <= offsetY + screenHeight;
    }

    // ──────────────────────────────────────────────
    // Getters
    // ──────────────────────────────────────────────
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }

    /** Reset kamera ke posisi awal. */
    public void reset() {
        offsetX = 0;
        offsetY = 0;
        targetX = 0;
        targetY = 0;
    }
}