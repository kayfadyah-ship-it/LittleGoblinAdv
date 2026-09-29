// ============================================================
//  Entity.java
//  Abstract class induk semua objek di dunia game.
//
//  OOP yang diterapkan:
//    - ENCAPSULATION : semua field private, akses via getter/setter
//    - ABSTRACT CLASS: tidak bisa di-instansiasi langsung
//    - INTERFACE     : wajib implement Drawable, Updatable, Collidable
// ============================================================
import java.awt.*;

// ── Interfaces ──────────────────────────────────────────────
interface Drawable   { void draw(Graphics2D g2d, Camera cam); }
interface Updatable  { void update(); }
interface Collidable { Rectangle getBounds(); }

// ── Abstract Class ───────────────────────────────────────────
public abstract class Entity implements Drawable, Updatable, Collidable {

    // ── Encapsulation: semua field PRIVATE ──────────────────
    private float x, y;           // Posisi dunia (pixel, float untuk presisi sub-pixel)
    private float velX, velY;     // Kecepatan per frame
    private int   width, height;  // Ukuran hitbox
    private boolean active;       // false = objek dihapus dari dunia

    // ── Constructor ─────────────────────────────────────────
    protected Entity(float x, float y, int width, int height) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
        this.active = true;
        this.velX   = 0;
        this.velY   = 0;
    }

    // ── Abstract methods wajib di-override ──────────────────
    @Override public abstract void draw(Graphics2D g2d, Camera cam);
    @Override public abstract void update();

    // ── Collidable: kembalikan hitbox sebagai Rectangle ─────
    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }

    /** Apakah dua entity ini saling bertabrakan (AABB)? */
    public boolean collidesWith(Entity other) {
        return getBounds().intersects(other.getBounds());
    }

    /** Apakah entity ini bertabrakan dengan Rectangle tertentu? */
    public boolean collidesWith(Rectangle r) {
        return getBounds().intersects(r);
    }

    // ── Getters & Setters (Encapsulation) ───────────────────
    public float getX()      { return x; }
    public float getY()      { return y; }
    public int   getWidth()  { return width; }
    public int   getHeight() { return height; }
    public float getVelX()   { return velX; }
    public float getVelY()   { return velY; }
    public boolean isActive(){ return active; }

    public void setX(float x)         { this.x = x; }
    public void setY(float y)         { this.y = y; }
    public void setVelX(float vx)     { this.velX = vx; }
    public void setVelY(float vy)     { this.velY = vy; }
    public void setWidth(int w)       { this.width = w; }
    public void setHeight(int h)      { this.height = h; }
    public void setActive(boolean a)  { this.active = a; }

    /** Titik tengah X entity (berguna untuk kamera). */
    public int getCenterX() { return (int)x + width  / 2; }
    /** Titik tengah Y entity. */
    public int getCenterY() { return (int)y + height / 2; }
}