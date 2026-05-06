package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Panel Pintu Keluar — Puzzle 3d (akhir dari rangkaian puzzle).
 *
 * Pemain harus:
 * 1. Sudah memiliki kunci emas (didapat dari brankas, state otomatis diteruskan)
 * 2. Memasukkan PIN = jawaban puzzle jam (0830)
 *
 * Jika benar → lanjut ke room3() / tahap berikutnya dalam game.
 */
public class PintuRoom1 extends JPanel {

    private EscapeRoomGUI parent;
    private BufferedImage bgRoom;

    private StringBuilder pinBuffer = new StringBuilder();
    private String feedbackMsg = "";
    private Color feedbackColor = Color.WHITE;
    private boolean cursorVisible = true;
    private Timer cursorTimer;

    // State: apakah pemain sudah "menginput kunci"
    private boolean keyInserted = false;
    private float keyGlow = 0f;
    private Timer keyGlowTimer;

    public PintuRoom1(EscapeRoomGUI parent) {
        this.parent = parent;
        setLayout(null);
        setBackground(Color.BLACK);
        setFocusable(true);
        loadBackground();

        // Cursor blink
        cursorTimer = new Timer(500, e -> { cursorVisible = !cursorVisible; repaint(); });
        cursorTimer.start();

        // Tombol "Masukkan Kunci Emas"
        JButton btnKey = new JButton("🗝  Masukkan Kunci Emas");
        btnKey.setBounds(235, 200, 230, 44);
        btnKey.setBackground(new Color(60, 50, 10));
        btnKey.setForeground(new Color(255, 220, 50));
        btnKey.setFont(new Font("Monospaced", Font.BOLD, 13));
        btnKey.setFocusPainted(false);
        btnKey.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKey.addActionListener(e -> {
            if (!keyInserted) {
                keyInserted = true;
                feedbackMsg = "";
                startKeyGlow();
                repaint();
            }
        });
        add(btnKey);

        // Keyboard input untuk PIN
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!keyInserted) {
                    feedbackMsg = "Masukkan kunci emas terlebih dahulu!";
                    feedbackColor = new Color(255, 150, 50);
                    repaint();
                    return;
                }
                handlePinInput(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { requestFocusInWindow(); }
        });
    }

    private void loadBackground() {
        try {
            var stream = getClass().getResourceAsStream("/asset/bg/room1.png");
            if (stream != null) bgRoom = ImageIO.read(stream);
        } catch (Exception e) {
            System.err.println("[PintuRoom1] Gagal load background: " + e.getMessage());
        }
    }

    private void startKeyGlow() {
        keyGlowTimer = new Timer(30, null);
        keyGlowTimer.addActionListener(e -> {
            keyGlow = Math.min(1f, keyGlow + 0.06f);
            repaint();
            if (keyGlow >= 1f) keyGlowTimer.stop();
        });
        keyGlowTimer.start();
    }

    private void handlePinInput(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9) {
            if (pinBuffer.length() < 4) {
                pinBuffer.append((char) e.getKeyChar());
                feedbackMsg = "";
            }
        } else if (code == KeyEvent.VK_BACK_SPACE) {
            if (pinBuffer.length() > 0)
                pinBuffer.deleteCharAt(pinBuffer.length() - 1);
            feedbackMsg = "";
        } else if (code == KeyEvent.VK_ENTER) {
            String input = pinBuffer.toString();
            if (input.equals("0830")) {
                cursorTimer.stop();
                feedbackMsg = "✓ PINTU TERBUKA!";
                feedbackColor = new Color(50, 255, 50);
                repaint();
                Timer t = new Timer(1000, ev -> {
                    parent.doorOpened();
                });
                t.setRepeats(false);
                t.start();
            } else {
                feedbackMsg = "✗ PIN SALAH — Ingat jawaban teka-teki jam!";
                feedbackColor = new Color(255, 80, 80);
                pinBuffer.setLength(0);
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int W = getWidth(), H = getHeight();

        // Background ruangan
        if (bgRoom != null) g2.drawImage(bgRoom, 0, 0, W, H, this);
        else { g2.setColor(Color.BLACK); g2.fillRect(0, 0, W, H); }

        // Overlay gelap semi-transparan
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, W, H);

        // ── Panel utama di tengah ──
        int panelW = 420, panelH = 340;
        int panelX = (W - panelW) / 2;
        int panelY = (H - panelH) / 2 - 20;

        g2.setColor(new Color(8, 8, 20));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 16, 16);
        g2.setColor(new Color(50, 255, 50, 100));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 16, 16);

        int cx = panelX + panelW / 2;

        // Judul
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.setColor(new Color(50, 255, 50));
        String title = "— PINTU KELUAR —";
        g2.drawString(title, cx - g2.getFontMetrics().stringWidth(title) / 2, panelY + 30);

        g2.setColor(new Color(50, 255, 50, 80));
        g2.drawLine(panelX + 15, panelY + 38, panelX + panelW - 15, panelY + 38);

        // Status kunci
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        if (!keyInserted) {
            g2.setColor(new Color(180, 180, 100));
            g2.drawString("Status: Kunci emas belum dimasukkan", panelX + 20, panelY + 60);
            g2.drawString("Kamu memiliki kunci emas dari brankas.", panelX + 20, panelY + 78);
            g2.drawString("Tekan tombol di bawah untuk menggunakannya.", panelX + 20, panelY + 95);
        } else {
            // Kunci sudah dimasukkan — glow emas
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, keyGlow));
            g2.setColor(new Color(255, 200, 0, 60));
            g2.fillRoundRect(panelX + 10, panelY + 48, panelW - 20, 55, 8, 8);
            g2.setComposite(old);

            g2.setColor(new Color(255, 220, 50));
            g2.setFont(new Font("Monospaced", Font.BOLD, 13));
            g2.drawString("✦ Kunci Emas: TERPASANG", panelX + 20, panelY + 70);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g2.setColor(new Color(180, 180, 180));
            g2.drawString("Sekarang masukkan PIN rahasia untuk membuka pintu.", panelX + 20, panelY + 90);
        }

        // Hint clue PIN
        g2.setFont(new Font("Monospaced", Font.ITALIC, 11));
        g2.setColor(new Color(120, 120, 120));
        g2.drawString("(Ingat: PIN adalah jawaban tebakan jam dari laptop)", panelX + 20, panelY + 116);

        // Kotak PIN Input
        int boxW = 200, boxH = 42;
        int boxX = cx - boxW / 2;
        int boxY = panelY + 135;

        boolean pinActive = keyInserted;
        g2.setColor(pinActive ? new Color(20, 20, 40) : new Color(15, 15, 25));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        Color borderColor = pinActive ? new Color(50, 255, 50, 180) : new Color(80, 80, 80);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        // PIN text
        g2.setFont(new Font("Monospaced", Font.BOLD, 28));
        StringBuilder displayPin = new StringBuilder();
        for (char c : pinBuffer.toString().toCharArray()) displayPin.append(c);
        if (pinActive && cursorVisible) displayPin.append("|");
        g2.setColor(pinActive ? new Color(50, 255, 50) : new Color(60, 60, 60));
        g2.drawString(displayPin.toString(), boxX + 14, boxY + 30);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(new Color(100, 100, 100));
        g2.drawString(pinBuffer.length() + "/4 digit  [ENTER] konfirmasi", boxX, boxY + boxH + 14);

        // Feedback
        if (!feedbackMsg.isEmpty()) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 13));
            g2.setColor(feedbackColor);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(feedbackMsg, cx - fm.stringWidth(feedbackMsg) / 2, boxY + boxH + 36);
        }

        // Footer
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(new Color(70, 70, 70));
        g2.drawString("[ Klik di area ini untuk fokus keyboard ]", cx - 130, panelY + panelH - 12);
    }
}
