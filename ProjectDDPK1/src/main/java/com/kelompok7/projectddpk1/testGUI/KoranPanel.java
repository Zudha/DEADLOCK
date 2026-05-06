package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Panel yang menampilkan gambar koran zoom-in.
 * Pemain harus mencatat tanggal penerbitan: 04/01/1937
 * Klik tombol "Tutup" untuk kembali ke room dan lanjut ke laptop.
 */
public class KoranPanel extends JPanel {

    private BufferedImage koranImage;
    private EscapeRoomGUI parent;

    public KoranPanel(EscapeRoomGUI parent) {
        this.parent = parent;
        setLayout(null);
        setBackground(new Color(10, 10, 10));
        loadKoranImage();

        // Tombol tutup di pojok kanan atas
        JButton btnClose = new JButton("✕  Tutup Koran");
        btnClose.setBounds(540, 20, 140, 36);
        btnClose.setBackground(new Color(50, 50, 50));
        btnClose.setForeground(new Color(50, 255, 50));
        btnClose.setFont(new Font("Monospaced", Font.BOLD, 13));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(true);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> parent.afterKoran());
        add(btnClose);

        // Label petunjuk bawah
        JLabel hint = new JLabel("Perhatikan tanggal penerbitan koran ini...", JLabel.CENTER);
        hint.setBounds(0, 540, 700, 30);
        hint.setForeground(new Color(200, 200, 100));
        hint.setFont(new Font("Monospaced", Font.ITALIC, 13));
        add(hint);
    }

    private void loadKoranImage() {
        try {
            var stream = getClass().getResourceAsStream("/asset/bg/koran.png");
            if (stream != null) {
                koranImage = ImageIO.read(stream);
            } else {
                System.err.println("[KoranPanel] koran.png tidak ditemukan di /asset/bg/");
            }
        } catch (Exception e) {
            System.err.println("[KoranPanel] Gagal load koran: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int W = getWidth(), H = getHeight();

        // Overlay gelap
        g2.setColor(new Color(10, 10, 10));
        g2.fillRect(0, 0, W, H);

        if (koranImage != null) {
            g2.drawImage(koranImage, 0, 0, W, H, this);
        } else {
            // Fallback kalau gambar tidak ada
            g2.setColor(new Color(50, 255, 50));
            g2.setFont(new Font("Monospaced", Font.BOLD, 20));
            g2.drawString("[ KORAN TIDAK DITEMUKAN ]", W / 2 - 160, H / 2 - 30);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 15));
            g2.setColor(new Color(200, 200, 100));
            g2.drawString("Letakkan koran.png di /asset/bg/koran.png", W / 2 - 200, H / 2 + 10);
            g2.drawString("TANGGAL PENERBITAN: 04/01/1937", W / 2 - 180, H / 2 + 50);
        }
    }
}
