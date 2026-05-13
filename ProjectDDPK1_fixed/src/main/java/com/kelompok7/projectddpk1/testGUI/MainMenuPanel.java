package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * MainMenuPanel — Layar utama game.
 * Gambar main-menu.png sudah berisi tombol START dan EXIT.
 * Menggunakan invisible JButton yang ditimpa tepat di atas tombol gambar.
 */
public class MainMenuPanel extends JPanel {

    private static final String MENU_IMG = "/asset/bg/main-menu.png";

    private BufferedImage menuImage;
    private EscapeRoomGUI parent;

    // Invisible buttons — posisi dalam proporsi (0.0–1.0) terhadap ukuran panel
    // Sesuaikan nilai ini dengan posisi tombol di gambar main-menu.png kamu
    private static final double BTN_X_RATIO  = 0.36;  // mulai X (36% dari kiri)
    private static final double BTN_W_RATIO  = 0.28;  // lebar (28% panel)
    private static final double BTN_H_RATIO  = 0.08;  // tinggi (8% panel)
    private static final double START_Y_RATIO = 0.41; // START mulai Y (41% dari atas)
    private static final double EXIT_Y_RATIO  = 0.54; // EXIT mulai Y (54% dari atas)

    private JButton btnStart;
    private JButton btnExit;

    public MainMenuPanel(EscapeRoomGUI parent) {
        this.parent = parent;
        setBackground(Color.BLACK);
        setLayout(null); // absolute layout agar tombol bisa diposisikan bebas
        loadMenuImage();
        createInvisibleButtons();
    }

    private void loadMenuImage() {
        try {
            var stream = getClass().getResourceAsStream(MENU_IMG);
            if (stream != null) {
                menuImage = ImageIO.read(stream);
            } else {
                System.err.println("[MainMenuPanel] main-menu.png tidak ditemukan di /asset/bg/");
            }
        } catch (Exception e) {
            System.err.println("[MainMenuPanel] Gagal load: " + e.getMessage());
        }
    }

    private void createInvisibleButtons() {
        btnStart = makeInvisibleButton();
        btnStart.addActionListener(e -> parent.startGame());
        add(btnStart);

        btnExit = makeInvisibleButton();
        btnExit.addActionListener(e -> System.exit(0));
        add(btnExit);
    }

    private JButton makeInvisibleButton() {
        JButton btn = new JButton();
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    @Override
    public void doLayout() {
        // Hitung ulang posisi & ukuran tombol setiap kali panel di-resize
        int W = getWidth(), H = getHeight();
        if (W == 0 || H == 0) return;

        int bw = (int)(W * BTN_W_RATIO);
        int bh = (int)(H * BTN_H_RATIO);
        int bx = (int)(W * BTN_X_RATIO);

        btnStart.setBounds(bx, (int)(H * START_Y_RATIO), bw, bh);
        btnExit .setBounds(bx, (int)(H * EXIT_Y_RATIO),  bw, bh);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int W = getWidth(), H = getHeight();

        if (menuImage != null) {
            // Gambar penuh — tidak ada apapun yang ditimpa
            g2.drawImage(menuImage, 0, 0, W, H, this);
        } else {
            // Fallback teks kalau gambar tidak ditemukan
            g2.setColor(new Color(15, 15, 30));
            g2.fillRect(0, 0, W, H);
            g2.setFont(new Font("Monospaced", Font.BOLD, 42));
            g2.setColor(new Color(50, 255, 50));
            String title = "DEADLOCK";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, (W - fm.stringWidth(title)) / 2, H / 3);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
            g2.setColor(Color.WHITE);
            g2.drawString("[ START ]", (W - 80) / 2, (int)(H * 0.45));
            g2.drawString("[ EXIT ]",  (W - 70) / 2, (int)(H * 0.56));
        }
    }
}