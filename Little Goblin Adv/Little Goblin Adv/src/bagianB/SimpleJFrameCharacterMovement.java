package bagianB;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimpleJFrameCharacterMovement extends JFrame {

    public SimpleJFrameCharacterMovement() {
        setTitle("Little Goblin Adventure");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        add(new MovePanel());
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimpleJFrameCharacterMovement::new);
    }
}

class MovePanel extends JPanel implements ActionListener, KeyListener {

    int x = 80;
    int y = 370;
    int speed = 5;

    boolean left = false, right = false;

    Timer timer = new Timer(16, this);

    public MovePanel() {
        setFocusable(true);
        addKeyListener(this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // Background langit gelap
        g2.setColor(new Color(35, 60, 35));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Bulan
        g2.setColor(new Color(180, 180, 90));
        g2.fillOval(930, 110, 110, 110);

        // Gunung belakang
        g2.setColor(new Color(15, 45, 15));
        int[] x1 = {200, 380, 560};
        int[] y1 = {420, 200, 420};
        g2.fillPolygon(x1, y1, 3);

        int[] x2 = {650, 820, 980};
        int[] y2 = {420, 170, 420};
        g2.fillPolygon(x2, y2, 3);

        // Platform kiri
        drawGround(g2, 0, 420, 300, 300);

        // Lubang tengah

        // Platform kanan
        drawGround(g2, 450, 420, 730, 300);

        // Platform ujung kanan
        drawGround(g2, 1180, 420, 100, 300);

        // Kotak kayu
        drawBox(g2, 220, 230);
        drawBox(g2, 600, 140);
        drawBox(g2, 1080, 230);
        drawBox(g2, 350, 50);

        // Spike
        g2.setColor(Color.RED);
        for (int i = 0; i < 5; i++) {
            int sx = 1140 + (i * 18);
            int[] px = {sx, sx + 8, sx + 16};
            int[] py = {420, 380, 420};
            g2.fillPolygon(px, py, 3);
        }

        // Karakter goblin sederhana
        drawGoblin(g2, x, y);

        // Tulisan
        g2.setColor(new Color(255, 210, 50));
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        g2.drawString("SCORE: 0", 1120, 50);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("A / LEFT = Kiri   D / RIGHT = Kanan", 20, 30);
    }

    public void drawGround(Graphics2D g2, int x, int y, int w, int h) {

        // rumput
        g2.setColor(new Color(70, 170, 0));
        g2.fillRect(x, y, w, 10);

        // tanah
        g2.setColor(new Color(120, 80, 35));
        g2.fillRect(x, y + 10, w, 40);

        // batu bawah
        g2.setColor(new Color(90, 90, 90));
        g2.fillRect(x, y + 50, w, h - 50);

        // garis batu
        g2.setColor(new Color(120, 120, 120));
        for (int i = x; i < x + w; i += 25) {
            for (int j = y + 50; j < y + h; j += 25) {
                g2.drawRect(i, j, 25, 25);
            }
        }
    }

    public void drawBox(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(150, 100, 40));
        g2.fillRect(x, y, 100, 50);

        g2.setColor(new Color(110, 70, 25));
        for (int i = 0; i < 5; i++) {
            g2.drawLine(x + (i * 20), y, x + (i * 20), y + 50);
        }
    }

    public void drawGoblin(Graphics2D g2, int x, int y) {

        // badan
        g2.setColor(new Color(80, 200, 60));
        g2.fillRect(x, y, 40, 45);

        // topi
        g2.setColor(new Color(140, 70, 180));
        int[] px = {x - 2, x + 20, x + 42};
        int[] py = {y, y - 20, y};
        g2.fillPolygon(px, py, 3);

        // mata
        g2.setColor(Color.WHITE);
        g2.fillOval(x + 8, y + 12, 10, 10);
        g2.fillOval(x + 22, y + 12, 10, 10);

        g2.setColor(Color.RED);
        g2.fillOval(x + 11, y + 15, 4, 4);
        g2.fillOval(x + 25, y + 15, 4, 4);

        // kaki
        g2.setColor(new Color(130, 90, 20));
        g2.fillRect(x + 8, y + 45, 8, 10);
        g2.fillRect(x + 24, y + 45, 8, 10);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (left && x > 0)
            x -= speed;

        if (right && x < getWidth() - 40)
            x += speed;

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
            left = true;

        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
            right = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
            left = false;

        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
            right = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}