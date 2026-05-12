package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;

public class LaptopLockPanel extends JPanel {

    public enum Mode { LOCK, CLUE_JAM, CLUE_BRANKAS }

    private Mode currentMode;
    private EscapeRoomGUI parent;
    private BufferedImage laptopImage;

    private StringBuilder pinBuffer = new StringBuilder();
    private String feedbackMsg = "";
    private Color feedbackColor = Color.WHITE;

    private boolean cursorVisible = true;
    private Timer cursorTimer;

    public LaptopLockPanel(EscapeRoomGUI parent, Mode mode) {
        this.parent = parent;
        this.currentMode = mode;
        setLayout(null);
        setBackground(Color.BLACK);
        setFocusable(true);
        loadLaptopImage();
        setupKeyInput();

        cursorTimer = new Timer(500, e -> {
            cursorVisible = !cursorVisible;
            repaint();
        });
        cursorTimer.start();

        JButton btnBack = new JButton("← Kembali");
        btnBack.setBounds(10, 10, 110, 30);
        btnBack.setBackground(new Color(30, 30, 30));
        btnBack.setForeground(new Color(50, 255, 50));
        btnBack.setFont(new Font("Monospaced", Font.BOLD, 12));
        btnBack.setFocusPainted(false);
        btnBack.setBorderPainted(true);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
        if (currentMode == Mode.CLUE_BRANKAS) {
            parent.backToRoom1();
            }   else {
                parent.room1();
            }
        });
        add(btnBack);
    }

    private void loadLaptopImage() {
        try {
            var stream = getClass().getResourceAsStream("/asset/bg/laptoproom1.png");
            if (stream != null) {
                laptopImage = ImageIO.read(stream);
            } else {
                System.err.println("[LaptopLockPanel] laptoproom1.png tidak ditemukan.");
            }
        } catch (Exception e) {
            System.err.println("[LaptopLockPanel] Gagal load laptop image: " + e.getMessage());
        }
    }

    private void setupKeyInput() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyInput(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private void handleKeyInput(KeyEvent e) {
        if (currentMode == Mode.LOCK) handleLockInput(e);
        else if (currentMode == Mode.CLUE_JAM) handleJamInput(e);
    }

    private void handleLockInput(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9) {
            if (pinBuffer.length() < 5) { pinBuffer.append((char) e.getKeyChar()); feedbackMsg = ""; }
        } else if (code == KeyEvent.VK_BACK_SPACE) {
            if (pinBuffer.length() > 0) pinBuffer.deleteCharAt(pinBuffer.length() - 1);
            feedbackMsg = "";
        } else if (code == KeyEvent.VK_ENTER) {
            if (pinBuffer.toString().equals("41937")) {
                cursorTimer.stop();
                feedbackMsg = "✓ AKSES DITERIMA";
                feedbackColor = new Color(50, 255, 50);
                repaint();
                Timer t = new Timer(800, ev -> parent.openLaptopClueJam());
                t.setRepeats(false); t.start();
            } else {
                feedbackMsg = "✗ PASSWORD SALAH — COBA LAGI";
                feedbackColor = new Color(255, 80, 80);
                pinBuffer.setLength(0);
            }
        }
        repaint();
    }

    private void handleJamInput(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9) {
            if (pinBuffer.length() < 4) { pinBuffer.append((char) e.getKeyChar()); feedbackMsg = ""; }
        } else if (code == KeyEvent.VK_BACK_SPACE) {
            if (pinBuffer.length() > 0) pinBuffer.deleteCharAt(pinBuffer.length() - 1);
            feedbackMsg = "";
        } else if (code == KeyEvent.VK_ENTER) {
            if (pinBuffer.toString().equals("0830")) {
                cursorTimer.stop();
                feedbackMsg = "✓ JAM BERIKUTNYA BENAR!";
                feedbackColor = new Color(50, 255, 50);
                repaint();
                Timer t = new Timer(900, ev -> parent.openLaptopClueBrankas());
                t.setRepeats(false); t.start();
            } else {
                feedbackMsg = "✗ JAWABAN SALAH — PERHATIKAN POLA JAM";
                feedbackColor = new Color(255, 80, 80);
                pinBuffer.setLength(0);
            }
        }
        repaint();
    }

    // ── RENDER ──────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int W = getWidth(), H = getHeight();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, W, H);

        int imgX = 0, imgY = 0, imgW = W, imgH = H;
        if (laptopImage != null) {
            double imgRatio = (double) laptopImage.getWidth() / laptopImage.getHeight();
            double panelRatio = (double) W / H;
            if (imgRatio > panelRatio) {
                imgW = W;
                imgH = (int)(W / imgRatio);
            } else {
                imgH = H;
                imgW = (int)(H * imgRatio);
            }
            imgX = (W - imgW) / 2;
            imgY = (H - imgH) / 2;
            g2.drawImage(laptopImage, imgX, imgY, imgW, imgH, this);
        }

        int screenX = imgX + (int)(imgW * 0.30);
        int screenY = imgY + (int)(imgH * 0.23);
        int screenW = (int)(imgW * 0.39);
        int screenH = (int)(imgH * 0.28);

        Shape oldClip = g2.getClip();
        g2.setClip(screenX, screenY, screenW, screenH);

        switch (currentMode) {
            case LOCK         -> drawLockScreen(g2, screenX, screenY, screenW, screenH);
            case CLUE_JAM     -> drawClueJamScreen(g2, screenX, screenY, screenW, screenH);
            case CLUE_BRANKAS -> drawClueBrankasScreen(g2, screenX, screenY, screenW, screenH);
        }

        g2.setClip(oldClip);
    }

    // ── LOCK SCREEN ──────────────────────────────────────────────
    private void drawLockScreen(Graphics2D g2, int sx, int sy, int sw, int sh) {
        g2.setColor(new Color(5, 5, 20));
        g2.fillRect(sx, sy, sw, sh);

        int cx = sx + sw / 2;

        drawLockIcon(g2, cx, sy + (int)(sh * 0.05));

        g2.setFont(new Font("Monospaced", Font.BOLD, (int)(sh * 0.09)));
        g2.setColor(new Color(50, 255, 50));
        String title = "SISTEM TERKUNCI";
        g2.drawString(title, cx - g2.getFontMetrics().stringWidth(title) / 2, sy + (int)(sh * 0.45));

        g2.setFont(new Font("Monospaced", Font.PLAIN, (int)(sh * 0.07)));
        g2.setColor(new Color(170, 170, 170));
        String inst1 = "Tanggal koran (DDMMYYYY),";
        String inst2 = "Tiadakan & jangan ulang angka sama.";
        g2.drawString(inst1, cx - g2.getFontMetrics().stringWidth(inst1) / 2, sy + (int)(sh * 0.55));
        g2.drawString(inst2, cx - g2.getFontMetrics().stringWidth(inst2) / 2, sy + (int)(sh * 0.55) + g2.getFontMetrics().getHeight());
        int boxW = (int)(sw * 0.75), boxH = (int)(sh * 0.18);
        int boxX = cx - boxW / 2;
        int boxY = sy + (int)(sh * 0.62);
        g2.setColor(new Color(20, 20, 40));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g2.setColor(new Color(50, 255, 50, 150));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        g2.setFont(new Font("Monospaced", Font.BOLD, (int)(sh * 0.13)));
        g2.setColor(new Color(50, 255, 50));
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < pinBuffer.length(); i++) display.append("●");
        if (cursorVisible) display.append("|");
        g2.drawString(display.toString(), boxX + 10, boxY + (int)(boxH * 0.75));

        g2.setFont(new Font("Monospaced", Font.PLAIN, (int)(sh * 0.06)));
        g2.setColor(new Color(120, 120, 120));
        g2.drawString(pinBuffer.length() + "/5  [ENTER] konfirmasi", boxX, boxY + boxH + (int)(sh * 0.07));
        g2.drawString(pinBuffer.length() + "/5  [ENTER] konfirmasi", boxX, boxY + boxH + (int)(sh * 0.07));

        if (!feedbackMsg.isEmpty()) {
            g2.setFont(new Font("Monospaced", Font.BOLD, (int)(sh * 0.07)));
            g2.setColor(feedbackColor);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(feedbackMsg, cx - fm.stringWidth(feedbackMsg) / 2, boxY + boxH + (int)(sh * 0.15));
        }
    }

    // ── CLUE JAM SCREEN ─────────────────────────────────────────
    private void drawClueJamScreen(Graphics2D g2, int sx, int sy, int sw, int sh) {
        g2.setColor(new Color(5, 5, 20));
        g2.fillRect(sx, sy, sw, sh);

        int cx = sx + sw / 2;
        int fs = Math.max(6, (int)(sh * 0.07));

        g2.setFont(new Font("Monospaced", Font.BOLD, (int)(sh * 0.09)));
        g2.setColor(new Color(50, 255, 50));
        String hdr = "AKSES DITERIMA";
        g2.drawString(hdr, cx - g2.getFontMetrics().stringWidth(hdr) / 2, sy + (int)(sh * 0.12));

        g2.setColor(new Color(50, 255, 50, 80));
        g2.drawLine(sx + 10, sy + (int)(sh * 0.16), sx + sw - 10, sy + (int)(sh * 0.16));

        g2.setFont(new Font("Monospaced", Font.PLAIN, fs));
        g2.setColor(new Color(200, 200, 200));
        String[] narasi = { "Ada tiga jam di ruangan ini. Urutkan sesuai dengan MHB, huruf awal setiap warna.", "Perhatikan polanya:" };
        int ny = sy + (int)(sh * 0.25);
        for (String line : narasi) {
            g2.drawString(line, sx + 10, ny);
            ny += (int)(sh * 0.10);
        }

        g2.setFont(new Font("Monospaced", Font.PLAIN, fs));
        g2.setColor(new Color(170, 170, 170));
        String inst = "Jawab dalam 4 digit (HHMM):";
        int instY = sy + (int)(sh * 0.58);
        g2.drawString(inst, cx - g2.getFontMetrics().stringWidth(inst) / 2, instY);

        int boxW = (int)(sw * 0.55), boxH = (int)(sh * 0.16);
        int boxX = cx - boxW / 2;
        int boxY = sy + (int)(sh * 0.63);
        g2.setColor(new Color(20, 20, 40));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g2.setColor(new Color(255, 220, 50, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        g2.setFont(new Font("Monospaced", Font.BOLD, (int)(sh * 0.13)));
        g2.setColor(new Color(255, 220, 50));
        StringBuilder display = new StringBuilder();
        for (char c : pinBuffer.toString().toCharArray()) display.append(c);
        if (cursorVisible) display.append("|");
        g2.drawString(display.toString(), boxX + 10, boxY + (int)(boxH * 0.75));

        g2.setFont(new Font("Monospaced", Font.PLAIN, (int)(sh * 0.06)));
        g2.setColor(new Color(120, 120, 120));
        g2.drawString("[ENTER] konfirmasi", boxX, boxY + boxH + (int)(sh * 0.07));

        if (!feedbackMsg.isEmpty()) {
            g2.setFont(new Font("Monospaced", Font.BOLD, (int)(sh * 0.07)));
            g2.setColor(feedbackColor);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(feedbackMsg, cx - fm.stringWidth(feedbackMsg) / 2, boxY + boxH + (int)(sh * 0.15));
        }
    }

    // ── CLUE BRANKAS SCREEN ──────────────────────────────────────
    private void drawClueBrankasScreen(Graphics2D g2, int sx, int sy, int sw, int sh) {
        g2.setColor(new Color(5, 5, 20));
        g2.fillRect(sx, sy, sw, sh);

        int cx = sx + sw / 2;
        int fs = Math.max(6, (int)(sh * 0.07));

        g2.setFont(new Font("Monospaced", Font.BOLD, (int)(sh * 0.09)));
        g2.setColor(cursorVisible ? new Color(50, 255, 50) : new Color(30, 180, 30));
        String hdr = "DEKRIPSI BERHASIL";
        g2.drawString(hdr, cx - g2.getFontMetrics().stringWidth(hdr) / 2, sy + (int)(sh * 0.12));

        g2.setColor(new Color(50, 255, 50, 80));
        g2.drawLine(sx + 10, sy + (int)(sh * 0.16), sx + sw - 10, sy + (int)(sh * 0.16));

        g2.setFont(new Font("Monospaced", Font.PLAIN, fs));
        g2.setColor(new Color(200, 200, 200));
        String[] lines = {
            "Bagus. Kamu menemukan polanya.",
            "",
            "Di tahun 2025, ada hal trending yang berhubungan dengan angka",
            "di media sosial.",
            "",
            "Angka itu adalah kunci brankas.",
        };
        int ty = sy + (int)(sh * 0.25);
        for (String line : lines) {
            g2.drawString(line, sx + 10, ty);
            ty += (int)(sh * 0.10);
        }

        g2.setFont(new Font("Monospaced", Font.PLAIN, fs));
        g2.setColor(new Color(120, 120, 120));
        String hint = "← Kembali untuk jelajah ruangan";
        g2.drawString(hint, cx - g2.getFontMetrics().stringWidth(hint) / 2, sy + (int)(sh * 0.90));
    }

    // ── LOCK ICON ────────────────────────────────────────────────
    private void drawLockIcon(Graphics2D g2, int cx, int topY) {
        g2.setColor(new Color(50, 255, 50, 150));
        g2.setStroke(new BasicStroke(3));
        g2.drawArc(cx - 14, topY, 28, 24, 0, 180);
        g2.setColor(new Color(50, 255, 50, 200));
        g2.fillRoundRect(cx - 18, topY + 18, 36, 28, 6, 6);
        g2.setColor(new Color(5, 5, 20));
        g2.fillOval(cx - 5, topY + 24, 10, 10);
        g2.fillRect(cx - 3, topY + 30, 6, 8);
    }
}