package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * SlidingPuzzlePanel  —  Cable Routing Puzzle (Update: Smooth Pipes)
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class SlidingPuzzlePanel extends JPanel {

    // ── Konstanta grid ─────────────────────────────────────────────────
    private static final int GRID      = 3;
    private static final int TILES     = GRID * GRID;   
    private static final int EMPTY     = 8;
    private static final int MAX_MOVES = 50;

    // ── Sprite sheet & Margin Correction ──────────────────────────────
    private static final String SPRITE_PATH  = "/asset/bg/Puzzle_Slide_Cable.png";
    private static final int    SHEET_COLS   = 3;
    private static final int    SHEET_ROWS   = 2;
    private static final int    SHEET_TILE_W = 155;
    private static final int    SHEET_TILE_H = 160;

    /**
     * Margin tiap tile (Left, Right, Top, Bottom) untuk membuang area kosong
     * agar ujung pipa menempel ke tepi kotak.
     */
    private static final int[][] MARGINS = {
        {16, 21, 12, 0},  // Tile 0: bend kanan-bawah
        {13, 27, 11, 0},  // Tile 1: bend kiri-bawah
        {0,  40, 10, 0},  // Tile 2: lurus horizontal
        {16, 25, 0, 67},  // Tile 3: bend atas-kanan
        {12, 28, 0, 66},  // Tile 4: bend atas-kiri
        {0,  40, 0, 68}   // Tile 5: lurus vertikal
    };

    @SuppressWarnings("unchecked")
    private static final Set<String>[] CONN = new Set[]{
        Set.of("R", "B"),      // 0
        Set.of("L", "B"),      // 1
        Set.of("L", "R"),      // 2
        Set.of("T", "R"),      // 3
        Set.of("T", "L"),      // 4
        Set.of("T", "B"),      // 5
        Set.of("R"),           // 6 START
        Set.of("L"),           // 7 END
        Set.of()               // 8 EMPTY
    };

    private static final int[] SOLVED = { 6, 1, 2,  4, 5, 0,  EMPTY, 3, 7 };
    private static final int   WIN_START = 0;
    private static final int   WIN_END   = 8;

    private final int[]           board    = new int[TILES];
    private final BufferedImage[] tileImgs = new BufferedImage[9];

    private int     moves     = 0;
    private int     hoverCell = -1;
    private boolean wonGame   = false;
    private float   winGlow   = 0f;

    private boolean animating    = false;
    private int     animFrom     = -1;
    private int     animTo       = -1;
    private float   animProgress = 0f;

    private Timer  puzzleTimer;
    private Timer  animTimer;
    private Timer  winTimer;
    private JLabel movesLabel;

    private final EscapeRoomGUI parent;

    private static final Color C_BG       = new Color(8,  10,  16);
    private static final Color C_PANEL    = new Color(10, 16,  28);
    private static final Color C_ACCENT   = new Color(0,  210, 255);
    private static final Color C_GREEN    = new Color(0,  255, 180);
    private static final Color C_TILE     = new Color(14,  22,  40);
    private static final Color C_TILE_HOV = new Color(20,  50,  80);
    private static final Color C_EMPTY    = new Color(5,   7,  12);
    private static final Color C_WARN     = new Color(255, 180, 50);

    public SlidingPuzzlePanel(EscapeRoomGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(C_BG);

        loadSprites();
        initBoard();
        buildUI();
        initTimers();

        Timer introDelay = new Timer(350, e -> showIntroDialog());
        introDelay.setRepeats(false);
        introDelay.start();
    }

    private void loadSprites() {
        try {
            var stream = getClass().getResourceAsStream(SPRITE_PATH);
            if (stream == null) {
                System.err.println("[SlidingPuzzle] Sprite tidak ditemukan: " + SPRITE_PATH);
                return;
            }
            BufferedImage sheet = ImageIO.read(stream);

            int idx = 0;
            for (int r = 0; r < SHEET_ROWS; r++) {
                for (int c = 0; c < SHEET_COLS; c++) {
                    int sx = c * SHEET_TILE_W;
                    int sy = r * SHEET_TILE_H;

                    // Implementasi koreksi margin (Crop area kosong)
                    int ml = MARGINS[idx][0];
                    int mr = MARGINS[idx][1];
                    int mt = MARGINS[idx][2];
                    int mb = MARGINS[idx][3];

                    int cropX = sx + ml;
                    int cropY = sy + mt;
                    int cropW = SHEET_TILE_W - ml - mr;
                    int cropH = SHEET_TILE_H - mt - mb;

                    // Safety bounds check
                    cropW = Math.min(cropW, sheet.getWidth() - cropX);
                    cropH = Math.min(cropH, sheet.getHeight() - cropY);

                    if (cropW > 0 && cropH > 0) {
                        tileImgs[idx] = sheet.getSubimage(cropX, cropY, cropW, cropH);
                    }
                    idx++;
                }
            }
            System.out.println("[SlidingPuzzle] Sprite berhasil dimuat dengan koreksi margin.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initBoard() {
        System.arraycopy(SOLVED, 0, board, 0, TILES);
        Random rnd = new Random();
        int ep = emptyPos();
        for (int k = 0; k < 300; k++) {
            List<Integer> nb = getMovable(ep);
            int pick = nb.get(rnd.nextInt(nb.size()));
            swap(ep, pick);
            ep = pick;
        }
    }

    private void swap(int a, int b) { int t=board[a]; board[a]=board[b]; board[b]=t; }

    private List<Integer> getMovable(int ep) {
        List<Integer> list = new ArrayList<>();
        int r = ep/GRID, c = ep%GRID;
        if (r > 0)      list.add((r-1)*GRID+c);
        if (r < GRID-1) list.add((r+1)*GRID+c);
        if (c > 0)      list.add(r*GRID+(c-1));
        if (c < GRID-1) list.add(r*GRID+(c+1));
        return list;
    }

    private int emptyPos() {
        for (int i=0; i<TILES; i++) if (board[i]==EMPTY) return i;
        return -1;
    }

    private boolean isSolved() {
        boolean[] vis = new boolean[TILES];
        Queue<Integer> q = new ArrayDeque<>();
        q.add(WIN_START); vis[WIN_START] = true;
        while (!q.isEmpty()) {
            int pos = q.poll();
            if (pos == WIN_END) return true;
            for (int nb : connectedNeighbors(pos))
                if (!vis[nb]) { vis[nb]=true; q.add(nb); }
        }
        return false;
    }

    private List<Integer> connectedNeighbors(int pos) {
        List<Integer> res = new ArrayList<>();
        int tile = board[pos];
        if (tile == EMPTY) return res;
        Set<String> c = CONN[tile];
        int r = pos/GRID, col = pos%GRID;

        if (c.contains("T") && r>0)        pipeCheck(res, pos-GRID, "B");
        if (c.contains("B") && r<GRID-1)   pipeCheck(res, pos+GRID, "T");
        if (c.contains("L") && col>0)      pipeCheck(res, pos-1,    "R");
        if (c.contains("R") && col<GRID-1) pipeCheck(res, pos+1,    "L");
        return res;
    }

    private void pipeCheck(List<Integer> res, int nb, String port) {
        int t = board[nb];
        if (t != EMPTY && CONN[t].contains(port)) res.add(nb);
    }

    private void initTimers() {
        puzzleTimer = new Timer(400, e -> {
            if (!animating && !wonGame && moves >= MAX_MOVES && !isSolved()) {
                puzzleTimer.stop();
                parent.gameOver();
            }
        });
        puzzleTimer.start();

        animTimer = new Timer(14, e -> {
            animProgress += 0.16f;
            if (animProgress >= 1f) {
                animProgress = 1f;
                swap(animFrom, animTo);
                animating = false; animFrom = animTo = -1;
                ((Timer)e.getSource()).stop();
                repaint();
                if (!wonGame && isSolved()) triggerWin();
            }
            repaint();
        });

        winTimer = new Timer(30, e -> { winGlow += 0.04f; repaint(); });
    }

    private void triggerWin() {
        wonGame = true;
        puzzleTimer.stop();
        winTimer.start();
        Timer delay = new Timer(1800, e -> {
            winTimer.stop();
            JOptionPane.showMessageDialog(this, "Kabel Terhubung!", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            parent.nextFromSlidingPuzzle();
        });
        delay.setRepeats(false);
        delay.start();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(10, 36, 10, 36));

        JLabel title = new JLabel("◈  CABLE ROUTING PUZZLE");
        title.setFont(new Font("Monospaced", Font.BOLD, 16));
        title.setForeground(C_ACCENT);
        header.add(title, BorderLayout.WEST);

        movesLabel = new JLabel("Moves: 0 / " + MAX_MOVES);
        movesLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        movesLabel.setForeground(Color.WHITE);
        header.add(movesLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel canvas = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintPuzzle((Graphics2D) g);
            }
        };
        canvas.setBackground(C_BG);
        canvas.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleClick(e, canvas); }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) { 
                int[] gd = gridDim(canvas);
                hoverCell = cellAt(e.getX(), e.getY(), gd);
                canvas.repaint();
            }
        });
        add(canvas, BorderLayout.CENTER);
    }

    private void handleClick(MouseEvent e, JPanel canvas) {
        if (animating || wonGame) return;
        int[] gd   = gridDim(canvas);
        int   cell = cellAt(e.getX(), e.getY(), gd);
        if (cell < 0) return;
        int ep = emptyPos();
        if (getMovable(ep).contains(cell)) {
            animFrom=cell; animTo=ep; animProgress=0f; animating=true;
            moves++;
            movesLabel.setText("Moves: " + moves + " / " + MAX_MOVES);
            animTimer.restart();
        }
    }

    private int[] gridDim(JPanel canvas) {
        int gs = Math.min(canvas.getWidth() - 200, canvas.getHeight() - 40);
        int cw = gs / GRID, ch = gs / GRID;
        int offX = (canvas.getWidth() - gs) / 2, offY = (canvas.getHeight() - (ch * GRID)) / 2;
        return new int[]{offX, offY, cw, ch, gs};
    }

    private int cellAt(int mx, int my, int[] gd) {
        if (mx<gd[0]||mx>gd[0]+gd[4]||my<gd[1]||my>gd[1]+gd[4]) return -1;
        return ((my-gd[1])/gd[3])*GRID + ((mx-gd[0])/gd[2]);
    }

    private void paintPuzzle(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int[] gd = gridDim(this);
        int offX=gd[0], offY=gd[1], cw=gd[2], ch=gd[3];

        for (int i = 0; i < TILES; i++) {
            int tile = board[i];
            int x = offX + (i % GRID) * cw;
            int y = offY + (i / GRID) * ch;

            if (tile == EMPTY && !(animating && animTo == i)) {
                drawEmptySlot(g2, x, y, cw, ch);
                continue;
            }

            if (animating && i == animFrom) {
                float t = easeInOut(animProgress);
                int destX = offX + (animTo % GRID) * cw;
                int destY = offY + (animTo / GRID) * ch;
                x = Math.round(x + (destX - x) * t);
                y = Math.round(y + (destY - y) * t);
            }
            drawTile(g2, tile, i, emptyPos(), x, y, cw, ch);
        }
        
        drawConnectionGlow(g2, offX, offY, cw, ch);
        if (wonGame) drawWinOverlay(g2, offX, offY, gd[4], gd[4]);
    }

    private void drawTile(Graphics2D g2, int tile, int pos, int empty, int x, int y, int cw, int ch) {
        boolean hov = (pos == hoverCell) && getMovable(empty).contains(pos);
        g2.setColor(hov ? C_TILE_HOV : C_TILE);
        g2.fillRoundRect(x+2, y+2, cw-4, ch-4, 10, 10);

        if (tile == 6) drawEndpointStart(g2, x, y, cw, ch, hov);
        else if (tile == 7) drawEndpointEnd(g2, x, y, cw, ch, hov);
        else if (tileImgs[tile] != null) {
            // Karena margin sudah di-crop, gambar akan otomatis ditarik (stretch) ke tepi cell
            g2.drawImage(tileImgs[tile], x, y, cw, ch, null);
        }

        if (hov) {
            g2.setColor(C_ACCENT);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x+2, y+2, cw-4, ch-4, 10, 10);
        }
    }

    // Helper draw lainnya tetap sama namun disingkat
    private void drawEndpointStart(Graphics2D g, int x, int y, int cw, int ch, boolean h) {
        g.setColor(C_GREEN); g.setStroke(new BasicStroke(9));
        g.drawLine(x+cw/2, y+ch/2, x+cw, y+ch/2);
        g.fillOval(x+cw/2-10, y+ch/2-10, 20, 20);
    }

    private void drawEndpointEnd(Graphics2D g, int x, int y, int cw, int ch, boolean h) {
        g.setColor(C_ACCENT); g.setStroke(new BasicStroke(9));
        g.drawLine(x, y+ch/2, x+cw/2, y+ch/2);
        g.fillOval(x+cw/2-10, y+ch/2-10, 20, 20);
    }

    private void drawEmptySlot(Graphics2D g, int x, int y, int cw, int ch) {
        g.setColor(C_EMPTY); g.fillRoundRect(x+2, y+2, cw-4, ch-4, 10, 10);
    }

    private void drawConnectionGlow(Graphics2D g2, int offX, int offY, int cw, int ch) {
        int a = Math.round(200 * (wonGame ? (float)(0.5+0.5*Math.sin(winGlow*Math.PI*2)) : 1f));
        for (int i=0; i<TILES; i++) {
            if (board[i]==EMPTY) continue;
            if ((i%GRID)<GRID-1 && CONN[board[i]].contains("R") && board[i+1]!=EMPTY && CONN[board[i+1]].contains("L"))
                glowJ(g2, offX+(i%GRID+1)*cw, offY+(i/GRID)*ch+ch/2, true, a);
            if ((i/GRID)<GRID-1 && CONN[board[i]].contains("B") && board[i+GRID]!=EMPTY && CONN[board[i+GRID]].contains("T"))
                glowJ(g2, offX+(i%GRID)*cw+cw/2, offY+(i/GRID+1)*ch, false, a);
        }
    }

    private void glowJ(Graphics2D g, int x, int y, boolean h, int a) {
        g.setColor(new Color(0, 255, 180, a)); g.setStroke(new BasicStroke(3));
        if (h) g.drawLine(x-5, y, x+5, y); else g.drawLine(x, y-5, x, y+5);
    }

    private void drawWinOverlay(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(0, 255, 180, 40)); g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(Color.WHITE); g.setFont(new Font("Monospaced", Font.BOLD, 30));
        g.drawString("CONNECTED", x + w/2 - 80, y + h/2);
    }

    private void showIntroDialog() { /* Sama seperti kode asli */ }
    private float easeInOut(float t) { return t < .5f ? 2*t*t : -1 + (4-2*t)*t; }
}