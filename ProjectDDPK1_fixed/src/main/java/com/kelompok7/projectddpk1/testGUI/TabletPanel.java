package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * TabletPanel — menampilkan konten di dalam gambar tablet.
 * Mendukung dua mode:
 *   - NORMAL  : layar kecil di tengah tablet (untuk teks/morse)
 *   - FULLSCREEN_PUZZLE : layar besar menutupi hampir seluruh tablet (untuk BallSort/SlidingPuzzle)
 */
public class TabletPanel extends JLayeredPane {

    public enum Mode { NORMAL, FULLSCREEN_PUZZLE }

    private static final String TABLET_IMG = "/asset/bg/Tablet.png";

    // Koordinat layar NORMAL (konten teks kecil)
    private static final int SCREEN_X = 212;
    private static final int SCREEN_Y = 168;
    private static final int SCREEN_W = 265;
    private static final int SCREEN_H = 185;

    // Koordinat layar FULLSCREEN_PUZZLE (lebih besar, cocok untuk game)
    private static final int FULL_X = 100;
    private static final int FULL_Y = 80;
    private static final int FULL_W = 490;
    private static final int FULL_H = 420;

    private BufferedImage tabletImg;
    private JPanel screenArea;

    /** Constructor default — mode NORMAL */
    public TabletPanel(JPanel contentToShow) {
        this(contentToShow, Mode.NORMAL);
    }

    /** Constructor dengan mode eksplisit */
    public TabletPanel(JPanel contentToShow, Mode mode) {
        setPreferredSize(new Dimension(700, 600));
        setOpaque(false);

        // Load gambar tablet
        try {
            var stream = getClass().getResourceAsStream(TABLET_IMG);
            if (stream != null) tabletImg = ImageIO.read(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Background tablet (gambar)
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (tabletImg != null)
                    g.drawImage(tabletImg, 0, 0, getWidth(), getHeight(), this);
                else {
                    // Fallback: gambar outline tablet sederhana
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(40, 40, 40));
                    g2.fillRoundRect(80, 40, 530, 510, 30, 30);
                    g2.setColor(new Color(80, 80, 80));
                    g2.setStroke(new java.awt.BasicStroke(4));
                    g2.drawRoundRect(80, 40, 530, 510, 30, 30);
                }
            }
        };
        bg.setOpaque(true);
        bg.setBackground(Color.BLACK);
        bg.setBounds(0, 0, 700, 600);
        add(bg, Integer.valueOf(0));

        // Pilih ukuran layar sesuai mode
        int sx, sy, sw, sh;
        if (mode == Mode.FULLSCREEN_PUZZLE) {
            sx = FULL_X; sy = FULL_Y; sw = FULL_W; sh = FULL_H;
        } else {
            sx = SCREEN_X; sy = SCREEN_Y; sw = SCREEN_W; sh = SCREEN_H;
        }

        // Area konten puzzle di dalam layar
        screenArea = new JPanel(new BorderLayout());
        screenArea.setOpaque(true);
        screenArea.setBackground(Color.BLACK);
        screenArea.setBounds(sx, sy, sw, sh);
        screenArea.add(contentToShow, BorderLayout.CENTER);
        add(screenArea, Integer.valueOf(1));
    }
}