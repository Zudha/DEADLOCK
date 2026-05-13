package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;

/**
 * GameOverPanel — Layar Game Over berbasis gambar.
 * Menampilkan gambar gameover.png sebagai background.
 * Dua tombol overlay: PLAY AGAIN dan EXIT.
 */
public class GameOverPanel extends JPanel {

    private static final String GAMEOVER_IMG = "/asset/bg/gameover.png";

    private BufferedImage gameoverImage;
    private EscapeRoomGUI parent;

    private boolean hoverPlay = false;
    private boolean hoverExit = false;

    private Rectangle playRect = new Rectangle();
    private Rectangle exitRect = new Rectangle();

    // Animasi fade-in
    private float alpha = 0f;
    private Timer fadeTimer;

    public GameOverPanel(EscapeRoomGUI parent) {
        this.parent = parent;
        setBackground(Color.BLACK);
        setLayout(null);
        loadGameoverImage();
        setupMouseListeners();
        startFadeIn();
    }

    private void loadGameoverImage() {
        try {
            var stream = getClass().getResourceAsStream(GAMEOVER_IMG);
            if (stream != null) {
                gameoverImage = ImageIO.read(stream);
            } else {
                System.err.println("[GameOverPanel] gameover.png tidak ditemukan di /asset/bg/");
            }
        } catch (Exception e) {
            System.err.println("[GameOverPanel] Gagal load: " + e.getMessage());
        }
    }

    private void startFadeIn() {
        fadeTimer = new Timer(30, e -> {
            alpha = Math.min(1f, alpha + 0.05f);
            repaint();
            if (alpha >= 1f) ((Timer)e.getSource()).stop();
        });
        fadeTimer.start();
    }

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean hp = playRect.contains(e.getPoint());
                boolean he = exitRect.contains(e.getPoint());
                if (hp != hoverPlay || he != hoverExit) {
                    hoverPlay = hp;
                    hoverExit = he;
                    setCursor(new Cursor(hp || he ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (playRect.contains(e.getPoint())) {
                    parent.restartGame();
                } else if (exitRect.contains(e.getPoint())) {
                    System.exit(0);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int W = getWidth(), H = getHeight();

        // Fade-in composite
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // ── Background gameover ──────────────────────────────────
        if (gameoverImage != null) {
            g2.drawImage(gameoverImage, 0, 0, W, H, this);
        } else {
            // Fallback kalau gambar tidak ada
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, W, H);

            // Gradasi oranye-merah di bagian atas (mirip gameover.png)
            GradientPaint gp = new GradientPaint(
                W / 2f, 0, new Color(180, 60, 0, 220),
                W / 2f, H * 0.6f, Color.BLACK
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, W, H);

            // Teks GAME OVER
            g2.setFont(new Font("Monospaced", Font.BOLD, 64));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            String go = "GAME OVER";
            g2.drawString(go, (W - fm.stringWidth(go)) / 2, (int)(H * 0.35));
        }

        // ── Tombol PLAY AGAIN ────────────────────────────────────
        int btnW = 240, btnH = 52;
        int btnX = (W - btnW) / 2;
        int playY = (int)(H * 0.60);
        playRect.setBounds(btnX, playY, btnW, btnH);
        drawButton(g2, playRect, "✦ PLAY AGAIN", hoverPlay, new Color(255, 165, 30));

        // ── Tombol EXIT ──────────────────────────────────────────
        int exitY = playY + btnH + 18;
        exitRect.setBounds(btnX, exitY, btnW, btnH);
        drawButton(g2, exitRect, "EXIT", hoverExit, new Color(180, 60, 60));

        // Reset composite
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void drawButton(Graphics2D g2, Rectangle r, String label,
                            boolean hover, Color accent) {
        Color bg = hover
            ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 210)
            : new Color(0, 0, 0, 170);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 12, 12));

        g2.setColor(hover ? accent.brighter() : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160));
        g2.setStroke(new BasicStroke(hover ? 2.5f : 1.5f));
        g2.draw(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 12, 12));

        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + (r.width  - fm.stringWidth(label)) / 2;
        int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(hover ? Color.WHITE : accent);
        g2.drawString(label, tx, ty);
    }
}
