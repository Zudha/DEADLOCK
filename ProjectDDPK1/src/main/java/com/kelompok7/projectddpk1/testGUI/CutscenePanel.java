package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * CutscenePanel — Animasi penculikan sebelum game dimulai.
 *
 * Alur:
 *   1. Van masuk dari kiri  (Row 1 spritesheet = jalan normal, 5 frame)
 *   2. Van berhenti di tengah layar (diam ~1.5 detik)
 *   3. Van kabur ke kanan   (Row 2 spritesheet = ngebut, 4 frame)
 *   4. Callback ke EscapeRoomGUI.intro() otomatis setelah selesai
 *
 * Asset yang dipakai:
 *   /asset/bg/BG-Scene-Penculikan.jpg   <- background supermarket
 *   /asset/bg/animasi-van.png           <- spritesheet van
 *
 * Cara pakai di EscapeRoomGUI:
 *   - Ganti isi konstruktor / pemanggilan intro() menjadi:
 *       CutscenePanel cutscene = new CutscenePanel(this);
 *       centerPanel.add(cutscene, BorderLayout.CENTER);
 *       centerPanel.revalidate();
 *       cutscene.play();
 */
public class CutscenePanel extends JPanel {

    // ── Referensi parent ──────────────────────────────────────────────
    private final EscapeRoomGUI parent;

    // ── Asset ─────────────────────────────────────────────────────────
    private BufferedImage background;
    private BufferedImage spritesheet;

    // ── Sprite config ─────────────────────────────────────────────────
    // Sesuaikan jika ukuran tiap frame di spritesheet berbeda
    private static final int SPRITE_COLS   = 5;   // frame per baris (row 1 punya 5)
    private static final int SPRITE_ROWS   = 3;   // total baris yang dipakai
    private static final int ROW_WALK      = 0;   // row 1 = jalan normal (index 0)
    private static final int ROW_FAST      = 1;   // row 2 = ngebut       (index 1)
    private static final int FAST_FRAMES   = 4;   // row 2 cuma 4 frame (kolom ke-5 kosong)

    private int frameW, frameH;                   // ukuran 1 frame (dihitung otomatis)
    private BufferedImage[] framesWalk;            // 5 frame row 1
    private BufferedImage[] framesFast;            // 4 frame row 2

    // ── State animasi (statis) ────────────────────────────────────────
    private int  currentFrame   = 0;
    private int  frameCounter   = 0;
    private static final int FRAME_DELAY   = 6;   // tick per frame sprite
    private static final int SHOW_DURATION = 180; // tick sebelum lanjut (~3 detik)
    private int  totalTicks     = 0;

    // Posisi van — diam di tengah layar
    private static final int VAN_X = 270;
    private static final int VAN_Y = 370;

    // ── Timer utama ───────────────────────────────────────────────────
    private Timer gameLoop;
    private static final int FPS = 16;

    // ─────────────────────────────────────────────────────────────────
    public CutscenePanel(EscapeRoomGUI parent) {
        this.parent = parent;
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(700, 600));

        loadAssets();
    }

    // ── Load gambar ───────────────────────────────────────────────────
    private void loadAssets() {
        try {
            var bgStream = getClass().getResourceAsStream("/asset/bg/BG-Scene-Penculikan.jpg");
            if (bgStream != null) background = ImageIO.read(bgStream);
            else System.err.println("[CutscenePanel] BG tidak ditemukan.");
        } catch (IOException e) {
            System.err.println("[CutscenePanel] Gagal load BG: " + e.getMessage());
        }

        try {
            var spStream = getClass().getResourceAsStream("/asset/bg/animasi-van.png");
            if (spStream != null) {
                spritesheet = ImageIO.read(spStream);
                sliceSprites();
            } else {
                System.err.println("[CutscenePanel] Spritesheet tidak ditemukan.");
            }
        } catch (IOException e) {
            System.err.println("[CutscenePanel] Gagal load spritesheet: " + e.getMessage());
        }
    }

    // ── Potong spritesheet jadi frame-frame ───────────────────────────
    private void sliceSprites() {
        frameW = spritesheet.getWidth()  / SPRITE_COLS;
        frameH = spritesheet.getHeight() / SPRITE_ROWS;

        framesWalk = new BufferedImage[SPRITE_COLS];
        for (int i = 0; i < SPRITE_COLS; i++) {
            framesWalk[i] = spritesheet.getSubimage(i * frameW, ROW_WALK * frameH, frameW, frameH);
        }

        framesFast = new BufferedImage[FAST_FRAMES];
        for (int i = 0; i < FAST_FRAMES; i++) {
            framesFast[i] = spritesheet.getSubimage(i * frameW, ROW_FAST * frameH, frameW, frameH);
        }
    }

    // ── Mulai animasi (panggil dari EscapeRoomGUI) ────────────────────
    public void play() {
        currentFrame = 0;
        frameCounter = 0;
        totalTicks   = 0;

        gameLoop = new Timer(FPS, e -> tick());
        gameLoop.start();
    }

    // ── Loop utama ────────────────────────────────────────────────────
    private void tick() {
        totalTicks++;

        // Animasi frame sprite tetap jalan
        frameCounter++;
        if (frameCounter >= FRAME_DELAY) {
            frameCounter = 0;
            currentFrame = (currentFrame + 1) % framesWalk.length;
        }

        // Setelah SHOW_DURATION tick, lanjut ke intro
        if (totalTicks >= SHOW_DURATION) {
            gameLoop.stop();
            onCutsceneDone();
        }

        repaint();
    }

    // ── Callback setelah cutscene selesai ─────────────────────────────
    private void onCutsceneDone() {
        // Tunggu sebentar lalu pindah ke intro
        Timer delay = new Timer(400, e -> {
            parent.showTextMode();
            parent.intro();
        });
        delay.setRepeats(false);
        delay.start();
    }

    // ── Render ────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int W = getWidth(), H = getHeight();

        // 1. Background
        if (background != null) {
            g2.drawImage(background, 0, 0, W, H, this);
        } else {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, W, H);
        }

        // 2. Van statis di tengah
        if (spritesheet != null && framesWalk != null) {
            BufferedImage frame = framesWalk[currentFrame];
            if (frame != null) {
                g2.drawImage(frame, VAN_X, VAN_Y, frameW, frameH, this);
            }
        } else {
            // Fallback kotak putih kalau asset belum ada
            g2.setColor(Color.WHITE);
            g2.fillRect(VAN_X, VAN_Y, 150, 80);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("VAN", VAN_X + 55, VAN_Y + 45);
        }
    }

}