package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class BrankasRoom1 extends JPanel {
    private BufferedImage bgBrankas;
    private EscapeRoomGUI parent;

    public BrankasRoom1(EscapeRoomGUI controller) {
        this.parent = controller;
        setLayout(null);
        setOpaque(false);
        loadBrankasImage();

        // Tombol Transparan (Koordinat sesuaikan dengan gambar Brankas_Zoom)
        JButton btn13 = createInvisibleButton(285, 240, 40, 40);
        JButton btn26 = createInvisibleButton(335, 240, 40, 40);
        JButton btn67 = createInvisibleButton(385, 240, 40, 40);

        btn13.addActionListener(e -> parent.gameOver());
        btn26.addActionListener(e -> parent.gameOver());
        btn67.addActionListener(e -> {
            System.out.println("Kode 67 Benar!");
            parent.room4();
        });

        add(btn13);
        add(btn26);
        add(btn67);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private void loadBrankasImage() {
        try {
            bgBrankas = ImageIO.read(getClass().getResourceAsStream("/asset/bg/Brankas.png"));
        } catch (Exception e) {
            System.err.println("Gagal load brankas: " + e.getMessage());
        }
    }

    private JButton createInvisibleButton(int x, int y, int w, int h) {
        JButton btn = new JButton();
        btn.setBounds(x, y, w, h);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); // Set true untuk melihat kotak tombol saat testing
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));  
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgBrankas != null) {
            g.drawImage(bgBrankas, 0, 0, getWidth(), getHeight(), this);
        }
    }
}