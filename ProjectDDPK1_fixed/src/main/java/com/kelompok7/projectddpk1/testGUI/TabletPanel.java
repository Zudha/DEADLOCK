package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class TabletPanel extends JLayeredPane {

    private static final String TABLET_IMG = "/asset/bg/Tablet.png";

    // Area layar hitam di dalam gambar tablet (koordinat di panel 700x600)
    private static final int SCREEN_X = 212;
    private static final int SCREEN_Y = 168;
    private static final int SCREEN_W = 265;
    private static final int SCREEN_H = 185;

    private BufferedImage tabletImg;
    private JPanel screenArea;

    public TabletPanel(JPanel contentToShow) {
        setPreferredSize(new Dimension(700, 600));
        setOpaque(false);

        // Load gambar tablet
        try {
            var stream = getClass().getResourceAsStream(TABLET_IMG);
            if (stream != null) tabletImg = ImageIO.read(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Background tablet (gambar)
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (tabletImg != null)
                    g.drawImage(tabletImg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        bg.setOpaque(true);
        bg.setBackground(Color.BLACK);
        bg.setBounds(0, 0, 700, 600);
        add(bg, Integer.valueOf(0));

        // Area konten puzzle di dalam layar hitam
        screenArea = new JPanel(new BorderLayout());
        screenArea.setOpaque(true);
        screenArea.setBackground(Color.BLACK);
        screenArea.setBounds(SCREEN_X, SCREEN_Y, SCREEN_W, SCREEN_H);
        screenArea.add(contentToShow, BorderLayout.CENTER);
        add(screenArea, Integer.valueOf(1));
    }
}