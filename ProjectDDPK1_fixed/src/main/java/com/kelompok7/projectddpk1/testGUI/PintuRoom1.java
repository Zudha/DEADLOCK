package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Panel Pintu Keluar — menggunakan gambar PinRoom1.png sebagai keypad.
 *
 * Gambar berisi keypad angka 1-9, *, 0, # dengan display hitam di atas.
 * Invisible JButton diletakkan tepat di atas setiap tombol di gambar.
 *
 * PIN yang benar: 0830 → lanjut via parent.doorOpened()
 * Tombol * = hapus (backspace), # = konfirmasi
 * Auto-confirm setelah 4 digit dimasukkan.
 *
 * ── Cara kalibrasi tombol ──────────────────────────────────────────────
 * Ubah BTN_BORDER_PAINTED = true untuk melihat area tombol saat testing.
 * Sesuaikan konstanta COL*_X, ROW*_Y, BTN_W, BTN_H sampai kotak merah
 * tepat menimpa tombol di gambar, lalu kembalikan ke false.
 */
public class PintuRoom1 extends JPanel {

    private EscapeRoomGUI parent;
    private BufferedImage bgImage;

    private StringBuilder pinBuffer = new StringBuilder();
    private String feedbackMsg = "";
    private Color feedbackColor = Color.WHITE;

    // ── Ubah ke true saat kalibrasi untuk melihat area tombol ────────
    private static final boolean BTN_BORDER_PAINTED = false;

    // ── Proporsi tombol — dikalibrasi dari pixel scan PinRoom1.png ───
    // Gambar asli 1152×921px, diverifikasi dengan debug overlay image.
    private static final double COL1_X = 0.425; // kolom kiri  (1, 4, 7, *)
    private static final double COL2_X = 0.475; // kolom tengah(2, 5, 8, 0)
    private static final double COL3_X = 0.524; // kolom kanan (3, 6, 9, #)
    private static final double BTN_W  = 0.047; // lebar tiap tombol

    private static final double ROW1_Y = 0.320; // baris 1: 1,2,3
    private static final double ROW2_Y = 0.391; // baris 2: 4,5,6
    private static final double ROW3_Y = 0.461; // baris 3: 7,8,9
    private static final double ROW4_Y = 0.532; // baris 4: *,0,#
    private static final double BTN_H  = 0.060; // tinggi tiap tombol

    // ── Area display (layar hitam di atas keypad) ────────────────────
    private static final double DISP_X = 0.425;
    private static final double DISP_Y = 0.215;
    private static final double DISP_W = 0.148;
    private static final double DISP_H = 0.087;

    // Mapping tombol [baris][kolom]
    private static final char[][] KEY_MAP = {
        {'1', '2', '3'},
        {'4', '5', '6'},
        {'7', '8', '9'},
        {'*', '0', '#'}
    };

    private static final double[] ROW_Y_ARR = {ROW1_Y, ROW2_Y, ROW3_Y, ROW4_Y};

    private JButton[] allButtons = new JButton[12];

    public PintuRoom1(EscapeRoomGUI parent) {
        this.parent = parent;
        setLayout(null);
        setBackground(Color.BLACK);
        loadBackground();
        createKeypadButtons();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionButtons();
            }
        });
    }

    private void loadBackground() {
        try {
            // Nama file: PinRoom1.png (sesuaikan path asset jika berbeda)
            var stream = getClass().getResourceAsStream("/asset/bg/PinRoom1.png");
            if (stream != null) {
                bgImage = ImageIO.read(stream);
            } else {
                System.err.println("[PintuRoom1] PinRoom1.png tidak ditemukan di /asset/bg/");
            }
        } catch (Exception e) {
            System.err.println("[PintuRoom1] Gagal load: " + e.getMessage());
        }
    }

    private void createKeypadButtons() {
        int idx = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                final char key = KEY_MAP[row][col];
                JButton btn = new JButton();
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(BTN_BORDER_PAINTED);
                btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btn.addActionListener(e -> handleKeyPress(key));
                add(btn);
                allButtons[idx++] = btn;
            }
        }
    }

    /** Hitung ulang posisi & ukuran semua tombol proporsional */
    private void repositionButtons() {
        int W = getWidth(), H = getHeight();
        if (W == 0 || H == 0) return;

        int bw = (int)(W * BTN_W);
        int bh = (int)(H * BTN_H);

        double[] colX = {COL1_X, COL2_X, COL3_X};
        int idx = 0;
        for (int row = 0; row < 4; row++) {
            int by = (int)(H * ROW_Y_ARR[row]);
            for (int col = 0; col < 3; col++) {
                int bx = (int)(W * colX[col]);
                allButtons[idx].setBounds(bx, by, bw, bh);
                idx++;
            }
        }
    }

    private void handleKeyPress(char key) {
        if (key == '*') {
            // * = hapus satu digit (backspace)
            if (pinBuffer.length() > 0) {
                pinBuffer.deleteCharAt(pinBuffer.length() - 1);
                feedbackMsg = "";
            }
        } else if (key == '#') {
            // # = konfirmasi (enter)
            if (pinBuffer.length() > 0) confirmPin();
        } else {
            // Digit 0-9: tambahkan jika belum 4 digit
            if (pinBuffer.length() < 4) {
                pinBuffer.append(key);
                feedbackMsg = "";
            }
            // Auto-confirm setelah 4 digit
            if (pinBuffer.length() == 4) {
                confirmPin();
                return; // repaint sudah dipanggil di dalam confirmPin
            }
        }
        repaint();
    }

    private void confirmPin() {
        String input = pinBuffer.toString();
        if (input.equals("0830")) {
            feedbackColor = new Color(50, 255, 50);
            repaint();
            for (JButton btn : allButtons) btn.setEnabled(false);
            Timer t = new Timer(1000, ev -> parent.doorOpened());
            t.setRepeats(false);
            t.start();
        } else {
           
            feedbackColor = new Color(255, 80, 80);
            pinBuffer.setLength(0);
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int W = getWidth(), H = getHeight();

        // ── Gambar background PinRoom1.png penuh ──────────────────────
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, W, H, this);
        } else {
            // Fallback sederhana kalau gambar tidak ada
            g2.setColor(new Color(30, 30, 50));
            g2.fillRect(0, 0, W, H);
            g2.setColor(new Color(80, 80, 120));
            g2.fillRoundRect(W/3, H/5, W/3, H*3/5, 20, 20);
            g2.setColor(new Color(50, 255, 50));
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));
            g2.drawString("[ PinRoom1.png tidak ada ]", W/3 + 10, H/2);
        }

        // ── Teks PIN langsung di atas gambar display (tanpa overlay hitam) ──
        int dx = (int)(W * DISP_X);
        int dy = (int)(H * DISP_Y);
        int dw = (int)(W * DISP_W);
        int dh = (int)(H * DISP_H);

        // Teks: digit yang sudah diinput + underscore untuk slot kosong
        StringBuilder displayText = new StringBuilder();
        for (int i = 0; i < pinBuffer.length(); i++) displayText.append(pinBuffer.charAt(i));
        for (int i = pinBuffer.length(); i < 4; i++) displayText.append('_');

        int fontSize = Math.max(12, (int)(dh * 0.55));
        g2.setFont(new Font("Monospaced", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int tx = dx + (dw - fm.stringWidth(displayText.toString())) / 2;
        int ty = dy + (dh + fm.getAscent()) / 2 - 4;

        // Shadow tipis agar teks tetap terbaca di atas gambar apapun
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(displayText.toString(), tx + 1, ty + 1);
        // Teks utama hijau
        g2.setColor(new Color(50, 255, 50));
        g2.drawString(displayText.toString(), tx, ty);

        // ── Feedback pesan (di bawah display) ────────────────────────
        if (!feedbackMsg.isEmpty()) {
            int fbFont = Math.max(11, (int)(H * 0.025));
            g2.setFont(new Font("Monospaced", Font.BOLD, fbFont));
            FontMetrics fmFb = g2.getFontMetrics();
            int fbX = (W - fmFb.stringWidth(feedbackMsg)) / 2;
            int fbY = dy + dh + 60;
            // Shadow
            g2.setColor(new Color(0, 0, 0, 200));
            g2.drawString(feedbackMsg, fbX + 1, fbY + 1);
            g2.setColor(feedbackColor);
            g2.drawString(feedbackMsg, fbX, fbY);
        }

        // Posisikan tombol saat pertama kali render
        if (allButtons[0].getWidth() == 0) repositionButtons();
    }
}