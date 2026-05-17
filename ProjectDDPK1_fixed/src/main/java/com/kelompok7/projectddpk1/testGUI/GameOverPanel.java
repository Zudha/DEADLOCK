package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * GameOverPanel — Layar Game Over berbasis gambar.
 * Menampilkan gambar gameover.png sebagai background penuh.
 * Tombol PLAY AGAIN dan EXIT sudah ada di dalam gambar.
 * Dua JButton invisible diletakkan tepat di atas tombol gambar.
 */
public class GameOverPanel extends JPanel {

    private static final String GAMEOVER_IMG = "/asset/bg/gameover.png";

    private BufferedImage gameoverImage;
    private EscapeRoomGUI parent;

    private JButton btnPlay;
    private JButton btnExit;

    // Animasi fade-in
    private float alpha = 0f;
    private Timer fadeTimer;

    public GameOverPanel(EscapeRoomGUI parent) {
        this.parent = parent;
        setBackground(Color.BLACK);
        setLayout(null);
        loadGameoverImage();
        createInvisibleButtons();
        startFadeIn();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionButtons();
            }
        });
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

    private void createInvisibleButtons() {
        btnPlay = makeInvisible();
        btnPlay.addActionListener(e -> parent.restartGame());

        btnExit = makeInvisible();
        btnExit.addActionListener(e -> System.exit(0));

        add(btnPlay);
        add(btnExit);
    }

    private JButton makeInvisible() {
        JButton btn = new JButton();
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Sesuaikan posisi tombol invisible dengan posisi tombol di gambar gameover.png.
     * Proporsi dari gambar asli:
     *   PLAY AGAIN : Y ~54%-63%
     *   EXIT       : Y ~67%-76%
     *   Keduanya   : X tengah ±16% dari center, lebar ~32%
     */
    private void repositionButtons() {
        int W = getWidth(), H = getHeight();
        int btnW = (int)(W * 0.32);
        int btnH = (int)(H * 0.09);
        int btnX = (W - btnW) / 2;

        btnPlay.setBounds(btnX, (int)(H * 0.54), btnW, btnH);
        btnExit.setBounds(btnX, (int)(H * 0.67), btnW, btnH);
    }

    private void startFadeIn() {
        fadeTimer = new Timer(30, e -> {
            alpha = Math.min(1f, alpha + 0.05f);
            repaint();
            if (alpha >= 1f) ((Timer) e.getSource()).stop();
        });
        fadeTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int W = getWidth(), H = getHeight();

        // Fade-in composite
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (gameoverImage != null) {
            g2.drawImage(gameoverImage, 0, 0, W, H, this);
        } else {
            // Fallback kalau gambar tidak ditemukan
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, W, H);
            GradientPaint gp = new GradientPaint(
                W / 2f, 0, new Color(180, 60, 0, 220),
                W / 2f, H * 0.6f, Color.BLACK
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, W, H);
            g2.setFont(new Font("Monospaced", Font.BOLD, 64));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            String go = "GAME OVER";
            g2.drawString(go, (W - fm.stringWidth(go)) / 2, (int)(H * 0.35));
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Posisikan tombol saat pertama kali render
        if (btnPlay.getWidth() == 0) repositionButtons();
    }
}