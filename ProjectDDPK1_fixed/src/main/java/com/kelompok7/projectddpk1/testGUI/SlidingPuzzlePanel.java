package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * SlidingPuzzlePanel — Rotation pipe/cable puzzle.
 *
 * Pemain klik tile untuk rotate 90°. Tujuan: semua kabel nyambung
 * membentuk jalur U-shape yang tersambung penuh.
 *
 * Tile dari Puzzle_Slide_Cable.png (sprite sheet 780x320, 6 tile):
 *   [0][0] = Elbow KANAN-BAWAH  → koneksi: E, S
 *   [0][1] = Elbow KIRI-BAWAH   → koneksi: W, S
 *   [0][2] = Straight HORIZONTAL → koneksi: W, E
 *   [1][0] = Elbow KANAN-ATAS   → koneksi: E, N
 *   [1][1] = Elbow KIRI-ATAS    → koneksi: W, N
 *   [1][2] = Straight VERTIKAL  → koneksi: N, S
 */
public class SlidingPuzzlePanel extends JPanel {

    // ── Tile type constants ───────────────────────────────────────────
    private static final int ELBOW_ES   = 0; // kanan-bawah
    private static final int ELBOW_WS   = 1; // kiri-bawah
    private static final int STRAIGHT_H = 2; // horizontal
    private static final int ELBOW_EN   = 3; // kanan-atas
    private static final int ELBOW_WN   = 4; // kiri-atas
    private static final int STRAIGHT_V = 5; // vertikal

    // Koneksi base (N=0, E=1, S=2, W=3)
    private static final boolean[][] BASE_CONNECTIONS = {
        {false, true,  true,  false}, // ELBOW_ES
        {false, false, true,  true }, // ELBOW_WS
        {false, true,  false, true }, // STRAIGHT_H
        {true,  true,  false, false}, // ELBOW_EN
        {true,  false, false, true }, // ELBOW_WN
        {true,  false, true,  false}, // STRAIGHT_V
    };

    private static final int GRID = 3;
    private static final String SPRITE_PATH = "/asset/bg/Puzzle_Slide_Cable.png";
    private static final int SPRITE_TILE_W = 140;
    private static final int SPRITE_TILE_H = 130;

    // ── State ─────────────────────────────────────────────────────────
    private final int[][] tileType = new int[GRID][GRID];
    private final int[][] rotation = new int[GRID][GRID]; // 0-3
    private final int[][] solRot   = new int[GRID][GRID]; // target rotasi
    private BufferedImage[] tileImgs = new BufferedImage[6];

    private int moves = 0;
    private final int MAX_MOVES = 12;
    private JLabel movesLabel;
    private Timer puzzleTimer;
    private final EscapeRoomGUI parent;

    public SlidingPuzzlePanel(EscapeRoomGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        loadSprites();
        initPuzzle();

        movesLabel = new JLabel("Moves: 0/" + MAX_MOVES, JLabel.CENTER);
        movesLabel.setForeground(Color.WHITE);
        movesLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        add(movesLabel, BorderLayout.NORTH);

        puzzleTimer = new Timer(1000, e -> {
            if (moves >= MAX_MOVES && !checkWin()) {
                puzzleTimer.stop();
                parent.gameOver();
            }
        });
        puzzleTimer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    // ── Sprite loading ────────────────────────────────────────────────
    private void loadSprites() {
        try {
            var stream = getClass().getResourceAsStream(SPRITE_PATH);
            if (stream == null) {
                System.err.println("[SlidingPuzzlePanel] Sprite tidak ditemukan: " + SPRITE_PATH);
                return;
            }
            BufferedImage sheet = ImageIO.read(stream);

            // Ukuran tile penuh (termasuk padding hitam)
            int tileW = 140, tileH = 130;
            // Padding konten di dalam tiap tile
            int padL = 19, padT = 12, padR = 7, padB = 8;
            int contentW = tileW - padL - padR; // 114px
            int contentH = tileH - padT - padB; // 110px

            int idx = 0;
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 3; col++) {
                    tileImgs[idx++] = sheet.getSubimage(
                        col * tileW + padL,
                        row * tileH + padT,
                        contentW,
                        contentH
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Puzzle init ───────────────────────────────────────────────────
    private void initPuzzle() {
        // Layout jalur U-shape:
        //  (0,0) ES → (0,1) H → (0,2) WS
        //  (1,0) V              (1,2) V
        //  (2,0) EN → (2,1) H → (2,2) WN
        int[][] layout = {
            {ELBOW_ES,   STRAIGHT_H, ELBOW_WS  },
            {STRAIGHT_V, ELBOW_ES,   STRAIGHT_V}, // (1,1) filler dekoratif
            {ELBOW_EN,   STRAIGHT_H, ELBOW_WN  },
        };
        int[][] targetRot = {
            {0, 0, 0},
            {0, 1, 0},
            {0, 0, 0},
        };

        Random rnd = new Random();
        for (int r = 0; r < GRID; r++) {
            for (int c = 0; c < GRID; c++) {
                tileType[r][c] = layout[r][c];
                solRot[r][c]   = targetRot[r][c];
                // Acak rotasi, pastikan tidak langsung solved
                int randRot;
                do {
                    randRot = rnd.nextInt(4);
                } while (randRot == targetRot[r][c] && rnd.nextBoolean());
                rotation[r][c] = randRot;
            }
        }
    }

    // ── Click handler ─────────────────────────────────────────────────
    private void handleClick(int mx, int my) {
        int labelH = movesLabel.getHeight();
        int cellW  = getWidth() / GRID;
        int cellH  = (getHeight() - labelH) / GRID;

        int col = mx / cellW;
        int row = (my - labelH) / cellH;
        if (row < 0 || row >= GRID || col < 0 || col >= GRID) return;

        rotation[row][col] = (rotation[row][col] + 1) % 4;
        moves++;
        movesLabel.setText("Moves: " + moves + "/" + MAX_MOVES);

        if (checkWin()) {
            puzzleTimer.stop();
            JOptionPane.showMessageDialog(this, "Kabel Terhubung! Sistem Aktif.");
            parent.nextFromSlidingPuzzle();
        }
        repaint();
    }

    // ── Win check ────────────────────────────────────────────────────
    private boolean checkWin() {
        for (int r = 0; r < GRID; r++)
            for (int c = 0; c < GRID; c++)
                if (rotation[r][c] != solRot[r][c]) return false;
        return true;
    }

    // ── Koneksi tile setelah rotate ───────────────────────────────────
    private boolean hasConnection(int row, int col, int dir) {
        int baseDir = (dir - rotation[row][col] + 4) % 4;
        return BASE_CONNECTIONS[tileType[row][col]][baseDir];
    }

    // ── Paint ────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int W      = getWidth();
        int H      = getHeight();
        int labelH = movesLabel.getHeight();
        int gridH  = H - labelH;
        int cellW  = W / GRID;
        int cellH  = gridH / GRID;

        for (int r = 0; r < GRID; r++) {
            for (int c = 0; c < GRID; c++) {
                int x = c * cellW;
                int y = labelH + r * cellH;

                BufferedImage img = tileImgs[tileType[r][c]];
                if (img != null) {
                    Graphics2D tg = (Graphics2D) g2.create();
                    tg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    tg.translate(x + cellW / 2.0, y + cellH / 2.0);
                    tg.rotate(Math.toRadians(rotation[r][c] * 90));
                    tg.translate(-cellW / 2.0, -cellH / 2.0);
                    tg.drawImage(img, 0, 0, cellW, cellH, null);
                    tg.dispose();
                } else {
                    // Fallback jika sprite tidak ada
                    g2.setColor(new Color(30, 30, 50));
                    g2.fillRect(x, y, cellW, cellH);
                    g2.setColor(Color.CYAN);
                    g2.setFont(new Font("Monospaced", Font.BOLD, 11));
                    g2.drawString("T" + tileType[r][c] + "R" + rotation[r][c],
                                  x + 10, y + cellH / 2);
                }

                // Indikator koneksi
                highlightConnections(g2, r, c, x, y, cellW, cellH);

                // Border grid
                g2.setColor(new Color(60, 60, 80));
                g2.setStroke(new BasicStroke(1));
                g2.drawRect(x, y, cellW, cellH);
            }
        }
    }

    private void highlightConnections(Graphics2D g2, int r, int c,
                                      int x, int y, int cw, int ch) {
        int[][] info = {
            {-1, 0, cw/2, 2     }, // N
            { 0, 1, cw-2, ch/2  }, // E
            { 1, 0, cw/2, ch-2  }, // S
            { 0,-1, 2,    ch/2  }, // W
        };
        for (int d = 0; d < 4; d++) {
            if (!hasConnection(r, c, d)) continue;
            int nr = r + info[d][0];
            int nc = c + info[d][1];
            boolean connected = (nr >= 0 && nr < GRID && nc >= 0 && nc < GRID)
                                 && hasConnection(nr, nc, (d + 2) % 4);
            g2.setColor(connected ? new Color(0, 255, 120, 200)
                                  : new Color(255, 80, 80, 180));
            g2.fillOval(x + info[d][2] - 4, y + info[d][3] - 4, 8, 8);
        }
    }
}