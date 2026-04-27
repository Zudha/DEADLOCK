package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScenePanel extends JPanel {

    // --- Data ---
    private BufferedImage background;   // gambar background
    private String characterName = "RAKA";  // nama yang muncul di kotak atas dialog
    private List<String> lines = new ArrayList<>(); // baris teks dialog

    // --- Styling kotak dialog ---
    private static final int BOX_HEIGHT   = 160;
    private static final int BOX_PADDING  = 20;
    private static final Color BOX_BG     = new Color(0, 0, 0, 180);   // hitam transparan
    private static final Color BOX_BORDER = new Color(50, 255, 50, 130);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);
    private static final Color NAME_COLOR = new Color(50, 255, 50);
    private static final Font  TEXT_FONT  = new Font("Monospaced", Font.PLAIN, 14);
    private static final Font  NAME_FONT  = new Font("Monospaced", Font.BOLD, 13);

    public ScenePanel() {
        setBackground(Color.BLACK);
    }

    // -------------------------------------------------------
    // API yang kamu pakai dari EscapeRoomGUI
    // -------------------------------------------------------

    /** Ganti background. Path dari folder resources, contoh: "/assets/bg/room1.png" */
    public void setBackground(String imagePath) {
        try {
            var stream = getClass().getResourceAsStream(imagePath);
            if (stream != null) background = ImageIO.read(stream);
            else System.out.println("Background tidak ditemukan: " + imagePath);
        } catch (IOException e) {
            System.out.println("Error load background: " + e.getMessage());
        }
        repaint();
    }

    /** Set teks dialog sekaligus nama karakter */
    public void setDialog(String name, String... textLines) {
        characterName = name;
        lines.clear();
        for (String line : textLines) lines.add(line);
        repaint();
    }

    /** Tambah satu baris ke dialog yang sudah ada */
    public void addLine(String line) {
        lines.add(line);
        repaint();
    }

    /** Hapus semua teks dialog */
    public void clearDialog() {
        lines.clear();
        characterName = "";
        repaint();
    }

    // -------------------------------------------------------
    // Rendering
    // -------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        // 1. Gambar background
        if (background != null) {
            g2.drawImage(background, 0, 0, W, H, this);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, W, H);
        }

        // 2. Kalau tidak ada teks, tidak perlu gambar kotak dialog
        if (lines.isEmpty() && characterName.isEmpty()) return;

        // 3. Gambar kotak dialog di bawah
        int boxY = H - BOX_HEIGHT - 10;
        int boxX = 10, boxW = W - 20;

        g2.setColor(BOX_BG);
        g2.fillRoundRect(boxX, boxY, boxW, BOX_HEIGHT, 12, 12);
        g2.setColor(BOX_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(boxX, boxY, boxW, BOX_HEIGHT, 12, 12);

        // 4. Kotak nama karakter (kalau ada)
        if (!characterName.isEmpty()) {
            g2.setFont(NAME_FONT);
            FontMetrics fm = g2.getFontMetrics();
            int nameW = fm.stringWidth(characterName) + 20;
            int nameBoxY = boxY - 24;

            g2.setColor(BOX_BG);
            g2.fillRoundRect(boxX, nameBoxY, nameW, 22, 6, 6);
            g2.setColor(BOX_BORDER);
            g2.drawRoundRect(boxX, nameBoxY, nameW, 22, 6, 6);
            g2.setColor(NAME_COLOR);
            g2.drawString(characterName, boxX + 10, nameBoxY + 15);
        }

        // 5. Teks dialog
        g2.setFont(TEXT_FONT);
        g2.setColor(TEXT_COLOR);
        FontMetrics fm = g2.getFontMetrics();
        int textY = boxY + BOX_PADDING + fm.getAscent();
        int maxLines = (BOX_HEIGHT - BOX_PADDING * 2) / fm.getHeight();
        int start = Math.max(0, lines.size() - maxLines); // scroll otomatis ke bawah

        for (int i = start; i < lines.size(); i++) {
            g2.drawString(lines.get(i), boxX + BOX_PADDING, textY);
            textY += fm.getHeight() + 2;
        }
    }
}