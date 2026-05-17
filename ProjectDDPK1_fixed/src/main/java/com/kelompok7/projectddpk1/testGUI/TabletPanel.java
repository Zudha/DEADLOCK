package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * TabletPanel — menampilkan konten di dalam gambar tablet.
 *
 * Semua bounds dihitung proporsional di doLayout() sehingga
 * selalu tepat di resolusi/skala apapun — tidak pernah hardcoded.
 *
 * Mode:
 *   NORMAL            — area konten menyesuaikan layar hitam di gambar tablet
 *   FULLSCREEN_PUZZLE — area konten lebih besar (isi hampir seluruh tablet)
 *
 * ── Cara kalibrasi ────────────────────────────────────────────────────
 * Ubah SHOW_DEBUG_BORDER = true untuk melihat border area konten saat runtime.
 * Sesuaikan konstanta N_X/N_Y/N_W/N_H sampai area merah pas di atas layar
 * hitam tablet, lalu kembalikan ke false.
 *
 * Dari gambar Tablet.png (1030×879 px):
 *   Layar hitam mulai kira-kira di X=280, Y=195, lebar=690, tinggi=410
 *   → proporsi: X≈0.272, Y≈0.222, W≈0.670, H≈0.466
 */
public class TabletPanel extends JLayeredPane {

    public enum Mode { NORMAL, FULLSCREEN_PUZZLE }

    private static final String TABLET_IMG = "/asset/bg/Tablet.png";

    // ── Ubah ke true saat kalibrasi ──────────────────────────────────
    private static final boolean SHOW_DEBUG_BORDER = false;

    // ── Proporsi NORMAL: area layar tablet (X, Y, Lebar, Tinggi) ─────
    // Sesuaikan dengan posisi layar hitam di gambar Tablet.png
    private static final double N_X = 0.2998;
    private static final double N_Y = 0.2787;
    private static final double N_W = 0.3818;
    private static final double N_H = 0.3081;


    // ── Proporsi FULLSCREEN_PUZZLE: hampir isi seluruh area tablet ────
    // Sedikit lebih kecil dari tepi gambar tablet
    private static final double F_X = 0.2998;
    private static final double F_Y = 0.2787;
    private static final double F_W = 0.3818;
    private static final double F_H = 0.3081;

    private final Mode mode;
    private BufferedImage tabletImg;
    private final JPanel bgPanel;
    private final JPanel screenArea;

    public TabletPanel(JPanel contentToShow) {
        this(contentToShow, Mode.NORMAL);
        
    }

    public TabletPanel(JPanel contentToShow, Mode mode) {
        this.mode = mode;

        try {
            var stream = getClass().getResourceAsStream(TABLET_IMG);
            if (stream != null) tabletImg = ImageIO.read(stream);
            else System.err.println("[TabletPanel] Tablet.png tidak ditemukan di /asset/bg/");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Layer 0: background (gambar tablet penuh)
        bgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                if (tabletImg != null) {
                    g2.drawImage(tabletImg, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback jika gambar tidak ada
                    g2.setColor(new Color(40, 40, 40));
                    g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 30, 30);
                    g2.setColor(new Color(80, 80, 80));
                    g2.setStroke(new BasicStroke(4));
                    g2.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 30, 30);
                }

                // Debug border merah: tampilkan area layar agar mudah kalibrasi
                if (SHOW_DEBUG_BORDER) {
                    int W = getWidth(), H = getHeight();
                    double rx = (mode == Mode.FULLSCREEN_PUZZLE) ? F_X : N_X;
                    double ry = (mode == Mode.FULLSCREEN_PUZZLE) ? F_Y : N_Y;
                    double rw = (mode == Mode.FULLSCREEN_PUZZLE) ? F_W : N_W;
                    double rh = (mode == Mode.FULLSCREEN_PUZZLE) ? F_H : N_H;
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRect((int)(W * rx), (int)(H * ry),
                                (int)(W * rw), (int)(H * rh));
                }
            }
        };
        bgPanel.setOpaque(true);
        bgPanel.setBackground(Color.BLACK);
        add(bgPanel, Integer.valueOf(0));

        // Layer 1: area konten puzzle (di atas gambar tablet)
        screenArea = new JPanel(new BorderLayout());
        screenArea.setOpaque(true);
        screenArea.setBackground(Color.BLACK);
        if (contentToShow != null) {
            screenArea.add(contentToShow, BorderLayout.CENTER);
        }
        add(screenArea, Integer.valueOf(1));
    }

    /**
     * Dipanggil otomatis setiap kali panel di-resize.
     * Bounds selalu dihitung proporsional — tidak pernah hardcoded.
     */
    @Override
    public void doLayout() {
        int W = getWidth(), H = getHeight();
        if (W == 0 || H == 0) return;

        // Background penuh
        bgPanel.setBounds(0, 0, W, H);

        // Area konten sesuai mode
        double rx = (mode == Mode.FULLSCREEN_PUZZLE) ? F_X : N_X;
        double ry = (mode == Mode.FULLSCREEN_PUZZLE) ? F_Y : N_Y;
        double rw = (mode == Mode.FULLSCREEN_PUZZLE) ? F_W : N_W;
        double rh = (mode == Mode.FULLSCREEN_PUZZLE) ? F_H : N_H;

        screenArea.setBounds((int)(W * rx), (int)(H * ry),
                             (int)(W * rw), (int)(H * rh));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(700, 560);
    }
}