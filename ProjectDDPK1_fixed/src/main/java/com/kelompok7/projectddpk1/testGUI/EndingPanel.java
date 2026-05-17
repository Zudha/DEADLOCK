package com.kelompok7.projectddpk1.testGUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * EndingPanel — Layar ending berbasis GIF animasi.
 *
 * Mirip dengan CutscenePanel (intro), panel ini memutar GIF ending
 * setelah pemain berhasil kabur dari maze. Setelah GIF selesai,
 * tidak ada transisi lanjutan — game berakhir di sini.
 *
 * Letakkan file GIF ending di:
 *   src/main/resources/asset/bg/SceneEnding.gif
 *
 * Untuk mengubah durasi tayangan, sesuaikan GIF_DURATION_MS
 * dengan panjang GIF kamu (dalam milidetik).
 */
public class EndingPanel extends JPanel {

    private final EscapeRoomGUI parent;
    private ImageIcon gifAnimation;

    // Durasi ending GIF dalam milidetik — sesuaikan dengan panjang GIF kamu
    private static final int GIF_DURATION_MS = 10000; // default 10 detik

    // Path GIF ending (sesuaikan jika berbeda)
    private static final String GIF_PATH = "/asset/bg/ending.gif";

    public EndingPanel(EscapeRoomGUI parent) {
        this.parent = parent;
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(700, 600));
        loadGif();
    }

    private void loadGif() {
        try {
            var stream = getClass().getResourceAsStream(GIF_PATH);
            if (stream != null) {
                byte[] gifBytes = stream.readAllBytes();
                gifAnimation = new ImageIcon(gifBytes);
                gifAnimation.setImageObserver(this);
            } else {
                System.err.println("[EndingPanel] " + GIF_PATH + " tidak ditemukan.");
            }
        } catch (IOException e) {
            System.err.println("[EndingPanel] Gagal load GIF ending: " + e.getMessage());
        }
    }

    /**
     * Mulai putar GIF ending.
     * Setelah GIF_DURATION_MS berlalu, tampilkan layar kredit / selesai.
     */
    public void play() {
        Timer delay = new Timer(GIF_DURATION_MS, e -> showCredits());
        delay.setRepeats(false);
        delay.start();
    }

    /**
     * Tampilkan layar kredit sederhana setelah GIF selesai.
     * Pemain bisa klik tombol untuk kembali ke menu utama atau keluar.
     */
    private void showCredits() {
        parent.showCreditsScreen();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int W = getWidth(), H = getHeight();

        if (gifAnimation != null) {
            g2.drawImage(gifAnimation.getImage(), 0, 0, W, H, this);
        } else {
            // Fallback kalau GIF tidak ditemukan
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, W, H);

            // Pesan fallback
            g2.setColor(new Color(50, 255, 50));
            g2.setFont(new Font("Monospaced", Font.BOLD, 22));
            String line1 = "RAKA SELAMAT.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(line1, (W - fm.stringWidth(line1)) / 2, H / 2 - 40);

            g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2.setColor(new Color(180, 255, 180));
            String line2 = "[ Letakkan SceneEnding.gif di /asset/bg/ ]";
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(line2, (W - fm2.stringWidth(line2)) / 2, H / 2 + 10);
        }
    }
}