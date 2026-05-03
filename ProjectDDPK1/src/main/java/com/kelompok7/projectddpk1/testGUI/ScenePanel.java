package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.awt.Rectangle;


public class ScenePanel extends JPanel {

    // ── Dialog ─────────────────────────────────────────────────
    private Runnable onDialogShown;
    public void setOnDialogShown(Runnable cb) { this.onDialogShown = cb; }

    private BufferedImage background;
    private String characterName = "";
    private List<String> lines = new ArrayList<>();
    private String   pendingDialogName  = "";
    private String[] pendingDialogLines = {};
    private boolean  dialogShown        = false;

    private static final int   BOX_HEIGHT  = 160;
    private static final int   BOX_PADDING = 20;
    private static final Color BOX_BG      = new Color(0, 0, 0, 180);
    private static final Color BOX_BORDER  = new Color(50, 255, 50, 130);
    private static final Color TEXT_COLOR  = new Color(230, 230, 230);
    private static final Color NAME_COLOR  = new Color(50, 255, 50);
    private static final Font  TEXT_FONT   = new Font("Monospaced", Font.PLAIN, 14);
    private static final Font  NAME_FONT   = new Font("Monospaced", Font.BOLD, 13);
    private List<Rectangle> collisionRects = new ArrayList<>();

    // ── Sprite ─────────────────────────────────────────────────
    private static final String SPRITE_PATH = "/asset/bg/mc2.png";
    private static final int FRAME_W = 150;
    private static final int FRAME_H = 150;
    // Tinggi render karakter di layar (aspek rasio terjaga)
    private static final int RENDER_H = 100;

    private static final int DIR_DOWN  = 0;
    private static final int DIR_UP    = 1;
    private static final int DIR_LEFT  = 2;
    private static final int DIR_RIGHT = 3;

    private BufferedImage[][] mcFrames; // [arah][frame_index]
    private int[] renderW = new int[4]; // lebar render per arah

    // ── Karakter state ──────────────────────────────────────────
    private float charX = 300, charY = 200;
    private int   charDir   = DIR_DOWN;
    private int   charFrame = 0;
    private int   frameTick = 0;
    private int prevDir = DIR_DOWN;
    private static final int FRAME_DELAY = 10;
    private static final int MOVE_SPEED  = 3;
    private float renderScale = 1.0f; // default normal

    // ── Zona interaksi ──────────────────────────────────────────
    private int deskX = 150, deskY = 150, deskW = 80, deskH = 80;
    private static final int INTERACT_RADIUS = 80;
    private String interactHint = "[ E ] Interaksi";
    
    //debug untuk melihat kordinat
    private int debugX = 0, debugY = 0;
    private boolean showDebug = true;
    private boolean collisionEnabled = false;

    // ── Input & Loop ────────────────────────────────────────────
    private boolean keyW, keyA, keyS, keyD;
    private Timer gameLoop;

    // ───────────────────────────────────────────────────────────
    public ScenePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        loadSprite();
        setupKeys();
        startLoop();
        
        //kode untuk melihat kordinat
        addMouseListener(new java.awt.event.MouseAdapter() {
    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        debugX = e.getX();
        debugY = e.getY();
        repaint();
        System.out.println("Klik: X=" + debugX + " Y=" + debugY);
        }
        });
    }

    // ── API ─────────────────────────────────────────────────────
    public void setDeskPosition(int x, int y, int w, int h) {
        deskX = x; deskY = y; deskW = w; deskH = h;
    }
    
    public void setInteractHint(String hint) {
        this.interactHint = hint;
    }

    public void setInteractDialog(String name, String... textLines) {
        pendingDialogName  = name;
        pendingDialogLines = textLines;
        dialogShown        = false;
        clearDialog();
    }

    // ── Sprite loading ──────────────────────────────────────────
    private void loadSprite() {
        try {
            var stream = getClass().getResourceAsStream(SPRITE_PATH);
            if (stream == null) return;
            BufferedImage sheet = makeBlackTransparent(ImageIO.read(stream), 40);

            mcFrames = new BufferedImage[4][];

            // DOWN: Row 1 (y=125), FRAME_H=125. Pastikan ambil tepat 125px.
            mcFrames[DIR_DOWN] = sliceRow(sheet, 1, 0, 3);
        
            // UP: Row 1 (y=125), kolom 3-5.
            mcFrames[DIR_UP]   = sliceRow(sheet, 1, 3, 3);

            // SIDE (LEFT/RIGHT): Masalahnya ada di sini. 
            // Kamu harus memastikan y dimulai dari 250 dan mengambil tinggi 250 (Row 2 + 3).
            mcFrames[DIR_LEFT] = sliceSideWalk(sheet, 0, 3);
            mcFrames[DIR_RIGHT] = sliceSideWalk(sheet, 3, 3);

            // RE-CALCULATE RENDER WIDTH: Agar ukuran karakter konsisten di semua arah
            for (int d = 0; d < 4; d++) {
                if (mcFrames[d] != null && mcFrames[d].length > 0) {
                    BufferedImage f = mcFrames[d][0];
                    // Menggunakan double agar pembagian presisi sebelum dikali RENDER_H
                    renderW[d] = (int)(((double)f.getWidth() / f.getHeight()) * RENDER_H);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
}

    /** Ambil n frame dari satu row spritesheet, lalu crop ke konten. */
    private BufferedImage[] sliceRow(BufferedImage sheet, int row, int colStart, int count) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            int x = (colStart + i) * FRAME_W;
            int y = row * FRAME_H;
            BufferedImage raw = sheet.getSubimage(x, y, FRAME_W, FRAME_H);
            frames[i] = cropToContent(raw);
        }
        return frames;
    }

    public void setRenderScale(float scale) {
        this.renderScale = scale;
    }
    
    private BufferedImage[] sliceSideWalk(BufferedImage sheet, int colStart, int count) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            int x = (colStart + i) * FRAME_W;
            int y = 2 * FRAME_H; // y=250
            int h = Math.min(FRAME_H * 2, sheet.getHeight() - y); // 250 atau sisa
            BufferedImage raw = sheet.getSubimage(x, y, FRAME_W, h);
            frames[i] = cropToContent(raw);
        }
        return frames;
    }

    /** Crop gambar ke bounding box pixel non-transparan. */
    private BufferedImage cropToContent(BufferedImage src) {
        int minX = src.getWidth(), maxX = -1, minY = src.getHeight(), maxY = -1;
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int a = (src.getRGB(x, y) >> 24) & 0xFF;
                if (a > 30) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < 0 || maxY < 0) return src; // tidak ada konten

        int pad = 5;
        minX = Math.max(0, minX - pad);
        minY = Math.max(0, minY - pad);
        maxX = Math.min(src.getWidth() - 1, maxX + pad);
        maxY = Math.min(src.getHeight() - 1, maxY + pad);
        maxX = Math.min(src.getWidth()  - 1, maxX + pad);
        maxY = Math.min(src.getHeight() - 1, maxY + pad);
        return src.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }


    private BufferedImage[] flipHoriz(BufferedImage[] src) {
        if (src == null) return new BufferedImage[0];
        BufferedImage[] dst = new BufferedImage[src.length];
        for (int i = 0; i < src.length; i++) {
            BufferedImage s = src[i];
            BufferedImage d = new BufferedImage(s.getWidth(), s.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = d.createGraphics();
            g.drawImage(s, s.getWidth(), 0, -s.getWidth(), s.getHeight(), null);
            g.dispose();
            dst[i] = d;
        }
        return dst;
    }

    private BufferedImage makeBlackTransparent(BufferedImage src, int threshold) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++)
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
                out.setRGB(x, y, (r < threshold && g < threshold && b < threshold) ? 0 : argb);
            }
        return out;
    }

    public void setCollisionRects(List<Rectangle> rects) {
        this.collisionRects = rects;
    }

    public void clearCollisionRects() {
        this.collisionRects.clear();
    }
    // ── Key bindings ────────────────────────────────────────────
    private void setupKeys() {
        InputMap  im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        bindKey(im, am, "WP",  KeyEvent.VK_W, true,  () -> keyW = true);
        bindKey(im, am, "AP",  KeyEvent.VK_A, true,  () -> keyA = true);
        bindKey(im, am, "SP",  KeyEvent.VK_S, true,  () -> keyS = true);
        bindKey(im, am, "DP",  KeyEvent.VK_D, true,  () -> keyD = true);
        bindKey(im, am, "WR",  KeyEvent.VK_W, false, () -> keyW = false);
        bindKey(im, am, "AR",  KeyEvent.VK_A, false, () -> keyA = false);
        bindKey(im, am, "SR",  KeyEvent.VK_S, false, () -> keyS = false);
        bindKey(im, am, "DR",  KeyEvent.VK_D, false, () -> keyD = false);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0, false), "EP");
        am.put("EP", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { tryInteract(); }
        });
    }
    
    public void clearOnDialogShown() {
    this.onDialogShown = null;
    }
    
    private void bindKey(InputMap im, ActionMap am, String id, int vk, boolean pressed, Runnable action) {
        im.put(KeyStroke.getKeyStroke(vk, 0, !pressed), id);
        am.put(id, new AbstractAction() { public void actionPerformed(ActionEvent e) { action.run(); } });
    }

    private void tryInteract() {
        if (pendingDialogLines.length == 0) return;
        if (isNearDesk()) {
            setDialog(pendingDialogName, pendingDialogLines);
            dialogShown = true;
            if (onDialogShown != null) onDialogShown.run();
        }
    }

    private boolean isNearDesk() {
        int cx = (int) charX + renderW[charDir] / 2;
        int cy = (int) charY + RENDER_H / 2;
        return Math.hypot(cx - (deskX + deskW/2), cy - (deskY + deskH/2)) <= INTERACT_RADIUS;
    }

    public void setCharPos(float x, float y) {
        charX = x;
        charY = y;
    }
    // ── Loop & update ───────────────────────────────────────────
    private void startLoop() {
        gameLoop = new Timer(16, e -> update());
        gameLoop.start();
    }

    public void activateCharacter()   { gameLoop.start(); }
    public void deactivateCharacter() { gameLoop.stop(); keyW = keyA = keyS = keyD = false; }

    private void update() {
        if (dialogShown) { repaint(); return; }

            
        boolean moving = false;
        float nx = charX, ny = charY;
        if (keyW) { ny -= MOVE_SPEED; charDir = DIR_UP;    moving = true; }
        else if (keyS) { ny += MOVE_SPEED; charDir = DIR_DOWN;  moving = true; }
        else if (keyA) { nx -= MOVE_SPEED; charDir = DIR_LEFT;  moving = true; }
        else if (keyD) { nx += MOVE_SPEED; charDir = DIR_RIGHT; moving = true; }

        int rw = renderW[charDir];
        int maxX = getWidth()  - rw;
        int maxY = (lines.isEmpty() ? getHeight() : getHeight() - BOX_HEIGHT - 40) - RENDER_H;
        nx = Math.max(0, Math.min(nx, maxX));
        ny = Math.max(0, Math.min(ny, maxY));

        
        float margin = rw * 0.35f; 
        float scaledH  = RENDER_H * renderScale;
        float footY    = ny    + scaledH - 5;
        float footYcur = charY + scaledH - 5;
        float footXL   = nx    + margin;
        float footXR   = nx    + rw - margin;
        float footXC   = nx    + rw / 2f;
        float footXLcur = charX + margin;
        float footXRcur = charX + rw - margin;
        float footXCcur = charX + rw / 2f;
        
        boolean canMoveX = !isWall(footXL, footYcur) && !isWall(footXR, footYcur) && !isWall(footXC, footYcur);
        boolean canMoveY = !isWall(footXLcur, footY) && !isWall(footXRcur, footY) && !isWall(footXCcur, footY);

        charX = canMoveX ? nx : charX;
        charY = canMoveY ? ny : charY;
        
        if (charDir != prevDir) {
            charFrame = 0;
            frameTick = 0;
            prevDir = charDir;
            }
        else if (moving) {
            if (++frameTick >= FRAME_DELAY) {
                frameTick = 0;
                if (mcFrames != null && mcFrames[charDir] != null)
                    charFrame = (charFrame + 1) % mcFrames[charDir].length;
            }
        } else {
            charFrame = 0;
            frameTick = 0;
        }
        repaint();
    }

    // ── Public setters ──────────────────────────────────────────
    public void setBackground(String path) {
        if (path == null || path.isEmpty()) { background = null; repaint(); return; }
        try {
            var s = getClass().getResourceAsStream(path);
            if (s != null) background = ImageIO.read(s);
            else System.out.println("[ScenePanel] BG tidak ditemukan: " + path);
        } catch (IOException e) { System.out.println("[ScenePanel] BG error: " + e.getMessage()); }
        repaint();
    }

    public void setDialog(String name, String... textLines) {
        characterName = name; lines.clear();
        for (String l : textLines) lines.add(l);
        repaint();
    }

    public void addLine(String line) { lines.add(line); repaint(); }

    public void clearDialog() { lines.clear(); characterName = ""; repaint(); }

    public void clearInteractDialog() {
        pendingDialogName = ""; pendingDialogLines = new String[]{};
        dialogShown = false; clearDialog();
    }

    public void resetCharPos() {
        charX = 300; charY = 200; charDir = DIR_DOWN; charFrame = 0;
        dialogShown = false; clearDialog(); clearInteractDialog();
    }
    
    public void enableCollision(boolean enabled) {
        this.collisionEnabled = enabled;
    }
    
    private boolean isWall(float x, float y) {
    if (!collisionEnabled) return false;
    for (Rectangle r : collisionRects) {
        if (r.contains(x, y)) return true;
    }
    return false;
}

    // ── Render ──────────────────────────────────────────────────
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

        // 2. Karakter
        if (mcFrames != null && mcFrames[charDir] != null
                && charFrame < mcFrames[charDir].length
                && mcFrames[charDir][charFrame] != null) {
            BufferedImage frame = mcFrames[charDir][charFrame];
            int rw = (int)(renderW[charDir] * renderScale);
            int rh = (int)(RENDER_H * renderScale);
            g2.drawImage(frame, (int) charX, (int) charY, rw, rh, this);
        }

        // 3. Hint interaksi
        if (isNearDesk() && !dialogShown && pendingDialogLines.length > 0) {
            String hint = interactHint;
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
            FontMetrics fm = g2.getFontMetrics();
            int nameW = fm.stringWidth(characterName) + 20, nameBoxY = boxY - 24;
            g2.setColor(BOX_BG);     g2.fillRoundRect(boxX, nameBoxY, nameW, 22, 6, 6);
            g2.setColor(BOX_BORDER); g2.drawRoundRect(boxX, nameBoxY, nameW, 22, 6, 6);
            g2.setColor(NAME_COLOR); g2.drawString(characterName, boxX + 10, nameBoxY + 15);
        }
        
        if (showDebug && collisionEnabled) {
        g2.setColor(new Color(255, 0, 0, 80));
        for (Rectangle r : collisionRects) {
        g2.fillRect(r.x, r.y, r.width, r.height);
        }
        g2.setColor(new Color(255, 0, 0, 180));
        for (Rectangle r : collisionRects) {
        g2.drawRect(r.x, r.y, r.width, r.height);
            }
        }
        
        if (showDebug) {
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Monospaced", Font.BOLD, 13));
            g2.drawString("X: " + debugX + "  Y: " + debugY, 10, 20);
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