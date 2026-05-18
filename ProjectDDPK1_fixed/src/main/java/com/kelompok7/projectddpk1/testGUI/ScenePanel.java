package com.kelompok7.projectddpk1.testGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    // debug untuk melihat koordinat
    private int debugX = 0, debugY = 0;
    private boolean showDebug = false;

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
    private static final int RENDER_H = 100;

    private static final int DIR_DOWN  = 0;
    private static final int DIR_UP    = 1;
    private static final int DIR_LEFT  = 2;
    private static final int DIR_RIGHT = 3;

    private BufferedImage[][] mcFrames;
    private int[] renderW = new int[4];

    // fog
    private boolean fogEnabled = false;

    // ── Karakter state ──────────────────────────────────────────
    private float charX = 300, charY = 200;
    private int   charDir   = DIR_DOWN;
    private int   charFrame = 0;
    private int   frameTick = 0;
    private int   prevDir   = DIR_DOWN;
    private static final int FRAME_DELAY = 10;
    private static final int MOVE_SPEED  = 3;
    private float  renderScale = 1.0f;
    private double layoutScale = 1.0;
    public void setLayoutScale(double scale) { this.layoutScale = scale; }

    // ── Zona interaksi ──────────────────────────────────────────
    private int deskX = 150, deskY = 150, deskW = 80, deskH = 80;
    private static final int INTERACT_RADIUS = 60;
    private String interactHint = "[ E ] Interaksi";

    private boolean collisionEnabled = false;

    // ── Input & Loop ────────────────────────────────────────────
    private boolean keyW, keyA, keyS, keyD;
    private Timer gameLoop;

    // ── Ending GIF ──────────────────────────────────────────────
    private ImageIcon endingGif       = null;
    private boolean   showingEndingGif = false;

    // ───────────────────────────────────────────────────────────
    public ScenePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        loadSprite();
        setupKeys();
        startLoop();
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
            mcFrames[DIR_DOWN]  = sliceRow(sheet, 1, 0, 3);
            mcFrames[DIR_UP]    = sliceRow(sheet, 1, 3, 3);
            mcFrames[DIR_LEFT]  = sliceSideWalk(sheet, 0, 3);
            mcFrames[DIR_RIGHT] = sliceSideWalk(sheet, 3, 3);

            for (int d = 0; d < 4; d++) {
                if (mcFrames[d] != null && mcFrames[d].length > 0) {
                    BufferedImage f = mcFrames[d][0];
                    renderW[d] = (int)(((double) f.getWidth() / f.getHeight()) * RENDER_H);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

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

    public void enableFog(boolean enabled) {
        this.fogEnabled = enabled;
    }

    private BufferedImage[] sliceSideWalk(BufferedImage sheet, int colStart, int count) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            int x = (colStart + i) * FRAME_W;
            int y = 2 * FRAME_H;
            int h = Math.min(FRAME_H * 2, sheet.getHeight() - y);
            BufferedImage raw = sheet.getSubimage(x, y, FRAME_W, h);
            frames[i] = cropToContent(raw);
        }
        return frames;
    }

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
        if (maxX < 0 || maxY < 0) return src;

        int pad = 5;
        minX = Math.max(0, minX - pad);
        minY = Math.max(0, minY - pad);
        maxX = Math.min(src.getWidth()  - 1, maxX + pad);
        maxY = Math.min(src.getHeight() - 1, maxY + pad);
        return src.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private BufferedImage makeBlackTransparent(BufferedImage src, int threshold) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++)
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int r = (argb >> 16) & 0xFF, g2 = (argb >> 8) & 0xFF, b = argb & 0xFF;
                out.setRGB(x, y, (r < threshold && g2 < threshold && b < threshold) ? 0 : argb);
            }
        return out;
    }

    public void setCollisionRects(List<Rectangle> rects) {
        this.collisionRects = rects;
    }

    // ── Key bindings ────────────────────────────────────────────
    private void setupKeys() {
        InputMap  im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        bindKey(im, am, "WP", KeyEvent.VK_W, true,  () -> keyW = true);
        bindKey(im, am, "AP", KeyEvent.VK_A, true,  () -> keyA = true);
        bindKey(im, am, "SP", KeyEvent.VK_S, true,  () -> keyS = true);
        bindKey(im, am, "DP", KeyEvent.VK_D, true,  () -> keyD = true);
        bindKey(im, am, "WR", KeyEvent.VK_W, false, () -> keyW = false);
        bindKey(im, am, "AR", KeyEvent.VK_A, false, () -> keyA = false);
        bindKey(im, am, "SR", KeyEvent.VK_S, false, () -> keyS = false);
        bindKey(im, am, "DR", KeyEvent.VK_D, false, () -> keyD = false);
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
        am.put(id, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { action.run(); }
        });
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
        int cx = (int) charX + (int)(renderW[charDir] * renderScale) / 2;
        int cy = (int) charY + (int)(RENDER_H * renderScale) / 2;
        return Math.hypot(cx - (deskX + deskW / 2), cy - (deskY + deskH / 2)) <= INTERACT_RADIUS;
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

    public void deactivateCharacter() { gameLoop.stop(); keyW = keyA = keyS = keyD = false; }

    private void update() {
        if (showingEndingGif) { repaint(); return; }
        if (dialogShown) { repaint(); return; }

        boolean moving = false;
        float nx = charX, ny = charY;
        if (keyW)      { ny -= MOVE_SPEED; charDir = DIR_UP;    moving = true; }
        else if (keyS) { ny += MOVE_SPEED; charDir = DIR_DOWN;  moving = true; }
        else if (keyA) { nx -= MOVE_SPEED; charDir = DIR_LEFT;  moving = true; }
        else if (keyD) { nx += MOVE_SPEED; charDir = DIR_RIGHT; moving = true; }

        int rw       = renderW[charDir];
        int scaledRW = (int)(rw * renderScale);
        int rh       = (int)(RENDER_H * renderScale);

        int maxX = 700 - scaledRW;
        int maxY = (lines.isEmpty() ? 560 : 560 - BOX_HEIGHT - 40) - rh;
        nx = Math.max(0, Math.min(nx, maxX));
        ny = Math.max(0, Math.min(ny, maxY));

        Rectangle nextBounds = new Rectangle((int) nx, (int) ny, scaledRW, rh);
        boolean collide = false;
        for (Rectangle r : collisionRects) {
            if (nextBounds.intersects(r)) { collide = true; break; }
        }
        if (!collide) { charX = nx; charY = ny; }

        if (charDir != prevDir) {
            charFrame = 0; frameTick = 0; prevDir = charDir;
        } else if (moving) {
            if (++frameTick >= FRAME_DELAY) {
                frameTick = 0;
                if (mcFrames != null && mcFrames[charDir] != null)
                    charFrame = (charFrame + 1) % mcFrames[charDir].length;
            }
        } else {
            charFrame = 0; frameTick = 0;
        }
        repaint();
    }

    // ── Public setters ──────────────────────────────────────────
    public void setBackground(String path) {
        if (path == null || path.isEmpty()) { background = null; repaint(); return; }
        try {
            var s = getClass().getResourceAsStream(path);
            if (s != null) background = ImageIO.read(s);
        } catch (IOException e) { System.out.println("[ScenePanel] BG error: " + e.getMessage()); }
        repaint();
    }

    public void setDialog(String name, String... textLines) {
        characterName = name;
        lines.clear();
        for (String l : textLines) lines.add(l);
        repaint();
    }

    public void addLine(String line) { lines.add(line); repaint(); }

    public void clearDialog() { lines.clear(); characterName = ""; repaint(); }

    public void clearInteractDialog() {
        pendingDialogName  = "";
        pendingDialogLines = new String[]{};
        dialogShown        = false;
        clearDialog();
    }

    public void resetCharPos() {
        charX = 300; charY = 200; charDir = DIR_DOWN; charFrame = 0;
        dialogShown = false;
        clearDialog();
        clearInteractDialog();
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

    // ── Ending GIF ──────────────────────────────────────────────

    /**
     * Putar GIF ending penuh layar di dalam ScenePanel.
     * Game loop dihentikan selama GIF diputar supaya karakter tidak bergerak.
     * Setelah durationMs berlalu, onFinish dipanggil dan GIF dihapus.
     *
     * @param gifPath    path resource GIF, misal "/asset/bg/theend.gif"
     * @param durationMs durasi tampil dalam milidetik (sesuaikan dengan panjang GIF)
     * @param onFinish   callback yang dipanggil setelah GIF selesai
     */
    public void playEndingGif(String gifPath, int durationMs, Runnable onFinish) {
        // Load GIF
        try {
            var stream = getClass().getResourceAsStream(gifPath);
            if (stream != null) {
                byte[] gifBytes = stream.readAllBytes();
                endingGif = new ImageIcon(gifBytes);
                endingGif.setImageObserver(this);
            } else {
                System.err.println("[ScenePanel] GIF tidak ditemukan: " + gifPath);
            }
        } catch (Exception e) {
            System.err.println("[ScenePanel] Gagal load GIF: " + e.getMessage());
        }

        // Hentikan game loop & tampilkan GIF
        gameLoop.stop();
        keyW = keyA = keyS = keyD = false;
        showingEndingGif = true;
        repaint();

        // Timer untuk akhiri GIF setelah durationMs
        Timer gifTimer = new Timer(durationMs, e -> {
            showingEndingGif = false;
            endingGif        = null;
            repaint();
            if (onFinish != null) onFinish.run();
        });
        gifTimer.setRepeats(false);
        gifTimer.start();
    }

    // ── Render ──────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // ── Ending GIF: tampilkan penuh layar, skip render scene biasa ──
        if (showingEndingGif) {
            int W = getWidth(), H = getHeight();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, W, H);
            if (endingGif != null) {
                g.drawImage(endingGif.getImage(), 0, 0, W, H, this);
            }
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Apply scale — semua koordinat tetap 700x560
        g2.scale(layoutScale, layoutScale);

        int W = 700, H = 560;

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

        // 3. Fog effect
        if (fogEnabled) {
            int fogCenterX = (int)(charX + (renderW[charDir] * renderScale) / 2);
            int fogCenterY = (int)(charY + (RENDER_H * renderScale) / 2);
            int fogRadius  = 80;
            RadialGradientPaint fog = new RadialGradientPaint(
                fogCenterX, fogCenterY, fogRadius,
                new float[]{ 0.0f, 0.6f, 1.0f },
                new Color[]{
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 200),
                    new Color(0, 0, 0, 255)
                }
            );
            g2.setPaint(fog);
            g2.fillRect(0, 0, W, H);
        }

        // 4. Hint interaksi
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

        // 5. Dialog box
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
            FontMetrics fm    = g2.getFontMetrics();
            int nameW         = fm.stringWidth(characterName) + 20;
            int nameBoxY      = boxY - 24;
            g2.setColor(BOX_BG);     g2.fillRoundRect(boxX, nameBoxY, nameW, 22, 6, 6);
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