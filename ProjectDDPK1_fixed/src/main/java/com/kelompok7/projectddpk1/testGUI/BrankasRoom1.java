package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class BrankasRoom1 extends JPanel {
    private BufferedImage bgBrankas;
    private EscapeRoomGUI parent;
    private int selectedCode = 0; // kode yang dipilih pemain (13, 26, atau 67)

    // ── Proporsi tombol relatif terhadap ukuran panel ─────────────────
    // Dikalibrasi langsung dari Brankas.png (1152x921px).
    // Ubah BTN_BORDER_PAINTED = true untuk debug jika perlu kalibrasi ulang.
    private static final boolean BTN_BORDER_PAINTED = false;

    private static final double BTN_Y   = 0.4343;
    private static final double BTN_H   = 0.0651;
    private static final double COL13_X = 0.4149;
    private static final double COL26_X = 0.4861;
    private static final double COL67_X = 0.5582;
    private static final double BTN_W   = 0.0564;

    // Tombol OK
    private static final double OK_X = 0.4887;
    private static final double OK_Y = 0.5190;
    private static final double OK_W = 0.0608;
    private static final double OK_H = 0.0489;

    private JButton btn13, btn26, btn67, btnOK;

    public BrankasRoom1(EscapeRoomGUI controller) {
        this.parent = controller;
        setLayout(null);
        setOpaque(false);
        loadBrankasImage();

        btn13 = createInvisibleButton();
        btn26 = createInvisibleButton();
        btn67 = createInvisibleButton();
        btnOK = createInvisibleButton();

        btn13.addActionListener(e -> parent.gameOver());
        btn26.addActionListener(e -> parent.gameOver());
        btn67.addActionListener(e -> {
            selectedCode = 67;
            repaint();
        });
        btnOK.addActionListener(e -> {
            if (selectedCode == 67) {
                System.out.println("Kode 67 Benar!");
                showGoldenKeyNote();
            } else if (selectedCode != 0) {
                parent.gameOver();
            }
        });

        add(btn13);
        add(btn26);
        add(btn67);
        add(btnOK);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionButtons();
            }
            @Override
            public void componentShown(ComponentEvent e) {
                requestFocusInWindow();
            }
        });
    }

    /** Hitung ulang posisi semua tombol proporsional setiap panel di-resize. */
    private void repositionButtons() {
        int W = getWidth(), H = getHeight();
        if (W == 0 || H == 0) return;
        int bw = (int)(W * BTN_W);
        int bh = (int)(H * BTN_H);
        int by = (int)(H * BTN_Y);
        btn13.setBounds((int)(W * COL13_X), by, bw, bh);
        btn26.setBounds((int)(W * COL26_X), by, bw, bh);
        btn67.setBounds((int)(W * COL67_X), by, bw, bh);
        btnOK.setBounds((int)(W * OK_X), (int)(H * OK_Y), (int)(W * OK_W), (int)(H * OK_H));
    }

    private void loadBrankasImage() {
        try {
            bgBrankas = ImageIO.read(getClass().getResourceAsStream("/asset/bg/Brankas.png"));
        } catch (Exception e) {
            System.err.println("Gagal load brankas: " + e.getMessage());
        }
    }

    private JButton createInvisibleButton() {
        JButton btn = new JButton();
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(BTN_BORDER_PAINTED);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    

    private void showGoldenKeyNote() {
        // Brankas berisi kunci emas — BUKAN PIN.
        // PIN harus ditemukan sendiri dari clue di laptop (teka-teki jam).
        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.setSize(360, 230);
        dialog.setLocationRelativeTo(this);
        dialog.setModal(true);

        JPanel notePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Latar gelap brankas
                g2.setColor(new Color(20, 14, 5));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Kotak utama warna emas redup
                g2.setColor(new Color(60, 45, 10));
                g2.fillRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 14, 14);
                g2.setColor(new Color(200, 160, 40));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 14, 14);

                // Ikon kunci emas — sederhana
                g2.setColor(new Color(255, 210, 50));
                g2.setFont(new Font("SansSerif", Font.BOLD, 28));
                g2.drawString("🗝", 18, 52);

                // Judul
                g2.setFont(new Font("Monospaced", Font.BOLD, 14));
                g2.setColor(new Color(255, 210, 50));
                g2.drawString("BRANKAS TERBUKA!", 60, 46);

                // Garis pemisah
                g2.setColor(new Color(160, 120, 30, 120));
                g2.drawLine(18, 60, getWidth() - 18, 60);

                // Isi pesan
                g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
                g2.setColor(new Color(220, 200, 140));
                g2.drawString("Di dalam brankas hanya ada satu benda:", 18, 82);

                g2.setFont(new Font("Monospaced", Font.BOLD, 16));
                g2.setColor(new Color(255, 220, 60));
                g2.drawString("✦  KUNCI EMAS", 90, 112);

                g2.setFont(new Font("Monospaced", Font.ITALIC, 11));
                g2.setColor(new Color(160, 140, 80));
                g2.drawString("Kunci ini untuk mengakses pintu keluar.", 18, 138);
                g2.drawString("PIN-nya? Kamu harus ingat sendiri.", 18, 155);
                g2.drawString("(Petunjuknya ada di laptop tadi...)", 18, 172);
            }
        };
        notePanel.setBackground(new Color(20, 14, 5));
        notePanel.setLayout(null);

        JButton btnOk = new JButton("Ambil Kunci & Lanjut");
        btnOk.setBounds(90, 185, 180, 32);
        btnOk.setBackground(new Color(60, 45, 10));
        btnOk.setForeground(new Color(255, 210, 50));
        btnOk.setFont(new Font("Monospaced", Font.BOLD, 12));
        btnOk.setFocusPainted(false);
        btnOk.setBorderPainted(true);
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOk.addActionListener(ev -> {
            dialog.dispose();
            parent.showPintu(); // Kunci emas diambil → ke pintu untuk input PIN
        });
        notePanel.add(btnOk);

        dialog.setContentPane(notePanel);
        dialog.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgBrankas != null) {
            g.drawImage(bgBrankas, 0, 0, getWidth(), getHeight(), this);
        }
        // Posisikan tombol saat pertama kali render
        // Posisikan tombol saat pertama kali render
        if (btn13.getWidth() == 0) repositionButtons();
    }
}