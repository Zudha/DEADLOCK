package com.kelompok7.projectddpk1.testGUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class CutscenePanel extends JPanel {

    private final EscapeRoomGUI parent;
    private ImageIcon gifAnimation;

    // Durasi cutscene dalam milidetik — sesuaikan dengan panjang GIF kamu
    private static final int GIF_DURATION_MS = 11000; // 5 detik

    public CutscenePanel(EscapeRoomGUI parent) {
        this.parent = parent;
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(700, 600));
        loadGif();
        
    }

    private void loadGif() {
        try {
            var stream = getClass().getResourceAsStream("/asset/bg/SceneCulik.gif");
            if (stream != null) {
                byte[] gifBytes = stream.readAllBytes();
                gifAnimation = new ImageIcon(gifBytes);
                gifAnimation.setImageObserver(this);
            } else {
                System.err.println("[CutscenePanel] Scene_1.gif tidak ditemukan.");
            }
        } catch (IOException e) {
            System.err.println("[CutscenePanel] Gagal load GIF: " + e.getMessage());
        }
    }

    public void play() {
        Timer delay = new Timer(GIF_DURATION_MS, e -> {
            parent.showTextMode();
            parent.intro();
        });
        delay.setRepeats(false);
        delay.start();
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
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, W, H);
            g2.setColor(new Color(50, 255, 50));
            g2.setFont(new Font("Monospaced", Font.BOLD, 16));
            g2.drawString("[ Loading Scene... ]", W / 2 - 90, H / 2);
        }
    }
}