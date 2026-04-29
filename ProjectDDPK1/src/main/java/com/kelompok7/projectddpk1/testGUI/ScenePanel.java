package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScenePanel extends JPanel {

    // -------------------------------------------------------
    // Data dialog
    // -------------------------------------------------------
    private Runnable onDialogShown;
    public void setOnDialogShown(Runnable callback) {
    this.onDialogShown = callback;
    }
    private BufferedImage background;
    private String characterName = "";
    private List<String> lines = new ArrayList<>();

    // Dialog tersimpan — muncul saat E ditekan di dekat meja
    private String   pendingDialogName  = "";
    private String[] pendingDialogLines = {};
    private boolean  dialogShown        = false;

    // -------------------------------------------------------
    // Styling kotak dialog
    // -------------------------------------------------------
    private static final int   BOX_HEIGHT  = 160;
    private static final int   BOX_PADDING = 20;
    private static final Color BOX_BG      = new Color(0, 0, 0, 180);
    private static final Color BOX_BORDER  = new Color(50, 255, 50, 130);
    private static final Color TEXT_COLOR  = new Color(230, 230, 230);
    private static final Color NAME_COLOR  = new Color(50, 255, 50);
    private static final Font  TEXT_FONT   = new Font("Monospaced", Font.PLAIN, 14);
    private static final Font  NAME_FONT   = new Font("Monospaced", Font.BOLD, 13);

    // -------------------------------------------------------
    // Spritesheet MC
    //   Row 0 : idle/jalan depan    — 4 frame (col 0-3)
    //   Row 1 : jalan diagonal/back — 6 frame (2 grup x 3)
    //   Row 2 : jalan samping       — 6 frame (kiri col 0-2, kanan col 3-5)
    // -------------------------------------------------------
    private static final String MC_PATH    = "/asset/bg/mc2.png";
    private static final int    MC_COLS    = 6;
    private static final int    MC_ROWS    = 3;
    private static final int    CHAR_SCALE = 1; // 1x = dikecilkan dari sebelumnya (2x)

    private static final int DIR_DOWN  = 0;
    private static final int DIR_UP    = 1;
    private static final int DIR_LEFT  = 2;
    private static final int DIR_RIGHT = 3;

    private static final int[][] DIR_DEF = {
        {0, 0, 4},  // DOWN
        {1, 3, 3},  // UP
        {2, 0, 3},  // LEFT
        {2, 3, 3},  // RIGHT
    };

    private BufferedImage[][] mcFrames;
    private int mcFrameW, mcFrameH;

    // -------------------------------------------------------
    // State karakter
    // -------------------------------------------------------
    private float charX = 300, charY = 200;
    private int   charDir   = DIR_DOWN;
    private int   charFrame = 0;
    private int   frameTick = 0;
    private static final int FRAME_DELAY = 8;
    private static final int MOVE_SPEED  = 3;

    // -------------------------------------------------------
    // Posisi meja koran & radius interaksi
    // Ubah via setDeskPosition() setelah lihat in-game
    // -------------------------------------------------------
    private int deskX = 150, deskY = 150, deskW = 80, deskH = 80;
    private static final int INTERACT_RADIUS = 80;

    // -------------------------------------------------------
    // Input
    // -------------------------------------------------------
    private boolean keyW, keyA, keyS, keyD;

    // -------------------------------------------------------
    // Timer
    // -------------------------------------------------------
    private Timer gameLoop;

    // -------------------------------------------------------
    public ScenePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        loadMCSprite();
        setupKeyBindings();
        startLoop();
    }

    // -------------------------------------------------------
    // API: atur posisi zona interaksi meja koran
    // Panggil dari EscapeRoomGUI setelah koordinat diketahui
    // Contoh: scenePanel.setDeskPosition(200, 180, 80, 60);
    // -------------------------------------------------------
    public void setDeskPosition(int x, int y, int w, int h) {
        deskX = x; deskY = y; deskW = w; deskH = h;
    }

    // -------------------------------------------------------
    // API: simpan dialog untuk trigger E
    // Panggil ini di room1() GANTIKAN setDialog() biasa
    // -------------------------------------------------------
    public void setInteractDialog(String name, String... textLines) {
        pendingDialogName  = name;
        pendingDialogLines = textLines;
        dialogShown        = false;
        clearDialog(); // dialog sembunyi dulu sampai E ditekan
    }

    // -------------------------------------------------------
    // Load & slice spritesheet
    // -------------------------------------------------------
    private void loadMCSprite() {
        try {
            var stream = getClass().getResourceAsStream(MC_PATH);
            if (stream == null) { System.err.println("[ScenePanel] mc2.png tidak ditemukan."); return; }
            BufferedImage sheet = makeBlackTransparent(ImageIO.read(stream), 40);

            mcFrameW = sheet.getWidth()  / MC_COLS;
            mcFrameH = sheet.getHeight() / MC_ROWS;

            mcFrames = new BufferedImage[4][];
            for (int d = 0; d < 4; d++) {
                int row = DIR_DEF[d][0], col = DIR_DEF[d][1], n = DIR_DEF[d][2];
                mcFrames[d] = new BufferedImage[n];
                for (int f = 0; f < n; f++)
                    mcFrames[d][f] = sheet.getSubimage((col + f) * mcFrameW, row * mcFrameH, mcFrameW, mcFrameH);
            }
        } catch (IOException e) {
            System.err.println("[ScenePanel] Gagal load MC: " + e.getMessage());
        }
    }

    private BufferedImage makeBlackTransparent(BufferedImage src, int threshold) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++)
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
                out.setRGB(x, y, (r < threshold && g < threshold && b < threshold) ? 0x00000000 : argb);
            }
        return out;
    }

    // -------------------------------------------------------
    // Key Bindings WASD + E
    // -------------------------------------------------------
    private void setupKeyBindings() {
        InputMap  im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        bindKey(im, am, "W", KeyEvent.VK_W, true,  () -> keyW = true);
        bindKey(im, am, "A", KeyEvent.VK_A, true,  () -> keyA = true);
        bindKey(im, am, "S", KeyEvent.VK_S, true,  () -> keyS = true);
        bindKey(im, am, "D", KeyEvent.VK_D, true,  () -> keyD = true);
        bindKey(im, am, "W", KeyEvent.VK_W, false, () -> keyW = false);
        bindKey(im, am, "A", KeyEvent.VK_A, false, () -> keyA = false);
        bindKey(im, am, "S", KeyEvent.VK_S, false, () -> keyS = false);
        bindKey(im, am, "D", KeyEvent.VK_D, false, () -> keyD = false);

        // E = interaksi
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0, false), "E_P");
        am.put("E_P", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { tryInteract(); }
        });
    }

    private void bindKey(InputMap im, ActionMap am, String key, int vk, boolean pressed, Runnable action) {
        String id = key + (pressed ? "_P" : "_R");
        im.put(KeyStroke.getKeyStroke(vk, 0, !pressed), id);
        am.put(id, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    // -------------------------------------------------------
    // Logika interaksi E
    // -------------------------------------------------------
    private void tryInteract() {
    if (pendingDialogLines.length == 0) return;
    if (isNearDesk()) {
        setDialog(pendingDialogName, pendingDialogLines);
        dialogShown = true;
        if (onDialogShown != null) onDialogShown.run(); // ← tambahkan ini
    }
}

    private boolean isNearDesk() {
        int cx = (int) charX + (mcFrameW * CHAR_SCALE) / 2;
        int cy = (int) charY + (mcFrameH * CHAR_SCALE) / 2;
        int mx = deskX + deskW / 2;
        int my = deskY + deskH / 2;
        return Math.hypot(cx - mx, cy - my) <= INTERACT_RADIUS;
    }

    // -------------------------------------------------------
    // Game loop
    // -------------------------------------------------------
    private void startLoop() {
        gameLoop = new Timer(16, e -> update());
        gameLoop.start();
    }

    public void activateCharacter()   { gameLoop.start(); }
    public void deactivateCharacter() { gameLoop.stop(); keyW = keyA = keyS = keyD = false; }

    private void update() {
        boolean moving = false;
        float nx = charX, ny = charY;
        
        if (dialogShown) { repaint(); return; }
        if (keyW) { ny -= MOVE_SPEED; charDir = DIR_UP;    moving = true; }
        if (keyS) { ny += MOVE_SPEED; charDir = DIR_DOWN;  moving = true; }
        if (keyA) { nx -= MOVE_SPEED; charDir = DIR_LEFT;  moving = true; }
        if (keyD) { nx += MOVE_SPEED; charDir = DIR_RIGHT; moving = true; }

        int sprW = mcFrameW * CHAR_SCALE;
        int sprH = mcFrameH * CHAR_SCALE;
        int maxX = getWidth() - sprW;
        int maxY = (lines.isEmpty() ? getHeight() : getHeight() - BOX_HEIGHT - 40) - sprH;

        charX = Math.max(0, Math.min(nx, maxX));
        charY = Math.max(0, Math.min(ny, maxY));

        if (moving) {
            frameTick++;
            if (frameTick >= FRAME_DELAY) {
                frameTick = 0;
                int max = mcFrames != null ? mcFrames[charDir].length : 1;
                charFrame = (charFrame + 1) % max;
            }
        } else {
            charFrame = 0;
            frameTick = 0;
        }

        repaint();
    }

    // -------------------------------------------------------
    // API dari EscapeRoomGUI
    // -------------------------------------------------------
    public void setBackground(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) { background = null; repaint(); return; }
        try {
            var stream = getClass().getResourceAsStream(imagePath);
            if (stream != null) background = ImageIO.read(stream);
            else System.out.println("Background tidak ditemukan: " + imagePath);
        } catch (IOException e) {
            System.out.println("Error load background: " + e.getMessage());
        }
        repaint();
    }

    public void setDialog(String name, String... textLines) {
        characterName = name;
        lines.clear();
        for (String line : textLines) lines.add(line);
        repaint();
    }

    public void addLine(String line) { lines.add(line); repaint(); }

    public void clearDialog() {
        lines.clear();
        characterName = "";
        repaint();
    }

    public void clearInteractDialog() {
        pendingDialogName  = "";
        pendingDialogLines = new String[]{};
        dialogShown        = false;
        clearDialog();
    }
    
    public void resetCharPos() {
        charX = 300; charY = 200;
        charDir = DIR_DOWN; charFrame = 0;
        dialogShown = false;
        clearDialog();
        clearInteractDialog();
    }

    // -------------------------------------------------------
    // Rendering
    // -------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int W = getWidth(), H = getHeight();

        // 1. Background
        if (background != null) g2.drawImage(background, 0, 0, W, H, this);
        else { g2.setColor(Color.BLACK); g2.fillRect(0, 0, W, H); }

        // 2. Karakter Raka
        if (mcFrames != null) {
            g2.drawImage(mcFrames[charDir][charFrame],
                (int) charX, (int) charY,
                mcFrameW * CHAR_SCALE, mcFrameH * CHAR_SCALE, this);
        }

        // 3. Hint "[ E ] Baca Koran" saat dekat meja & dialog belum tampil
        if (isNearDesk() && !dialogShown && pendingDialogLines.length > 0) {
            String hint = "[ E ] Baca Koran";
            g2.setFont(new Font("Monospaced", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            int hw = fm.stringWidth(hint) + 16;
            int hx = deskX + deskW / 2 - hw / 2;
            int hy = deskY - 10;
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(hx, hy - fm.getAscent() - 4, hw, fm.getHeight() + 8, 8, 8);
            g2.setColor(new Color(50, 255, 50));
            g2.drawString(hint, hx + 8, hy);
        }

        // 4. Dialog box
        if (lines.isEmpty() && characterName.isEmpty()) return;

        int boxY = H - BOX_HEIGHT - 10;
        int boxX = 10, boxW = W - 20;

        g2.setColor(BOX_BG);
        g2.fillRoundRect(boxX, boxY, boxW, BOX_HEIGHT, 12, 12);
        g2.setColor(BOX_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(boxX, boxY, boxW, BOX_HEIGHT, 12, 12);

        if (!characterName.isEmpty()) {
            g2.setFont(NAME_FONT);
            FontMetrics fm   = g2.getFontMetrics();
            int nameW    = fm.stringWidth(characterName) + 20;
            int nameBoxY = boxY - 24;
            g2.setColor(BOX_BG);    g2.fillRoundRect(boxX, nameBoxY, nameW, 22, 6, 6);
            g2.setColor(BOX_BORDER); g2.drawRoundRect(boxX, nameBoxY, nameW, 22, 6, 6);
            g2.setColor(NAME_COLOR); g2.drawString(characterName, boxX + 10, nameBoxY + 15);
        }

        g2.setFont(TEXT_FONT);
        g2.setColor(TEXT_COLOR);
        FontMetrics fm = g2.getFontMetrics();
        int textY  = boxY + BOX_PADDING + fm.getAscent();
        int maxLns = (BOX_HEIGHT - BOX_PADDING * 2) / fm.getHeight();
        int start  = Math.max(0, lines.size() - maxLns);
        for (int i = start; i < lines.size(); i++) {
            g2.drawString(lines.get(i), boxX + BOX_PADDING, textY);
            textY += fm.getHeight() + 2;
        }
    }
}