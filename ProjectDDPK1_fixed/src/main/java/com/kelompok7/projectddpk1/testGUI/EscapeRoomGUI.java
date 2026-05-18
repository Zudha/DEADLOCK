package com.kelompok7.projectddpk1.testGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;
import java.util.ArrayList;
import java.awt.Rectangle;
import java.util.List;


public class EscapeRoomGUI extends JFrame {

    JPanel lamp;
    JTextArea display;
    JTextField input;
    JPanel centerPanel;
    ScenePanel scenePanel;
    int state = 0;
    boolean BacaKoran = false;
    private static final int GAME_W = 700; 
    private static final int GAME_H = 560;
    

    // ── STATE MACHINE (tambahan untuk puzzle baru) ───────────────
    // state 0  = intro
    // state 1  = room1 (scene — jelajah, koran di meja)
    // state 1K = (panel koran terbuka, tidak ada text input)
    // state 1L = laptop lockscreen (input 04011937)
    // state 1J = laptop clue jam (input 0830)
    // state 1B = laptop clue brankas (tampilkan clue 67)
    // state 1P = panel pintu (input pin 0830 setelah kunci emas)
    // state 2  = room2
    // state 3  = room3
    // state 4  = room4 (memori)
    // state 6  = room5 (morse answer)
    // state 7  = room6 (ball sort)
    // state 8  = room7/room8 (sliding puzzle / next)
    // state 99 = game over
    //
    // Alur puzzle Room1:
    //   room1() → [E dekat koran] → showKoran() → afterKoran() → room1Laptop(LOCK)
    //   → [input 04011937] → openLaptopClueJam() (CLUE_JAM)
    //   → [input 0830] → openLaptopClueBrankas() (CLUE_BRANKAS)
    //   → [kembali ke room] → backToRoom1AfterBrankas() → roomAsset1Brankas()
    //   → [klik 67] → brankasOpened() → showPintu()
    //   → [masukkan kunci + pin 0830] → doorOpened() → room2()

    public EscapeRoomGUI() {
        setTitle("COMDEV: Commit of Development");
        setSize(700, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setLayout(new BorderLayout());

        display = new JTextArea();
        display.setEditable(false);
        display.setBackground(Color.BLACK);
        display.setForeground(new Color(50, 255, 50));
        display.setFont(new Font("Monospaced", Font.PLAIN, 14));
        display.setMargin(new Insets(15, 15, 15, 15));
        display.setLineWrap(true);
        display.setWrapStyleWord(true);

        input = new JTextField();
        input.setBackground(new Color(30, 30, 30));
        input.setForeground(Color.WHITE);
        input.setCaretColor(Color.WHITE);
        input.setFont(new Font("Consolas", Font.BOLD, 16));
        

        centerPanel = new JPanel(new BorderLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }

        @Override
        public void doLayout() {
            int W = getWidth(), H = getHeight();
            double scale = Math.min((double) W / GAME_W, (double) H / GAME_H);
            int scaledW = (int)(GAME_W * scale);
            int scaledH = (int)(GAME_H * scale);
            int offsetX = (W - scaledW) / 2;
            int offsetY = (H - scaledH) / 2;
            for (Component c : getComponents()) {
                c.setBounds(offsetX, offsetY, scaledW, scaledH);
                if (c instanceof ScenePanel sp) {
                    sp.setLayoutScale(scale);
                }
            }
        }
    };  // ← tutup anonymous class di sini
    centerPanel.setBackground(Color.BLACK);
    centerPanel.add(new JScrollPane(display), BorderLayout.CENTER);

    add(centerPanel, BorderLayout.CENTER);
    add(input, BorderLayout.SOUTH);

    lamp = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(getBackground());
            g.fillOval(10, 10, 100, 100);
        }
    };
    centerPanel.setBackground(Color.BLACK);
        centerPanel.add(new JScrollPane(display), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        add(input, BorderLayout.SOUTH);

        lamp = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(getBackground());
                g.fillOval(10, 10, 100, 100);
            }
        };
        lamp.setPreferredSize(new Dimension(120, 120));
        lamp.setBackground(Color.BLACK);

        scenePanel = new ScenePanel();

        input.addActionListener(e -> {
            String userInput = input.getText();
            input.setText("");
            handleInput(userInput);
        });

        setLocationRelativeTo(null);
        setVisible(true);
        SwingUtilities.invokeLater(() -> {
            input.setEnabled(false);
            showMainMenu();
        });
    }

    // ================================================================
    // UTIL
    // ================================================================

    void print(String text) {
        display.append(text + "\n");
        display.setCaretPosition(display.getDocument().getLength());
    }

    void clear() { display.setText(""); }

    // ================================================================
    // MODE HELPERS
    // ================================================================

    void showSceneMode() {
        centerPanel.removeAll();
        centerPanel.add(scenePanel, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
        scenePanel.resetCharPos();
        input.setEnabled(false);
        scenePanel.requestFocusInWindow();
        SwingUtilities.invokeLater(() -> scenePanel.requestFocusInWindow());
        scenePanel.enableCollision(false);
        scenePanel.setRenderScale(1.0f);
        scenePanel.clearOnDialogShown();
        scenePanel.enableFog(false);
    }

    void showTextMode() {
        centerPanel.removeAll();
        centerPanel.add(new JScrollPane(display), BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    void showLampMode() {
        centerPanel.removeAll();
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Color.BLACK);
        lamp.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.add(Box.createVerticalGlue());
        wrapper.add(lamp);
        wrapper.add(Box.createVerticalStrut(20));
        wrapper.add(new JScrollPane(display));
        wrapper.add(Box.createVerticalGlue());
        centerPanel.add(wrapper, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // ================================================================
    // MORSE
    // ================================================================

    void playMorse(String morse) {
        int unit = 250;
        input.setEnabled(false);
        Timer timer = new Timer(unit, null);
        final int[] i = {0};
        final boolean[] isOn = {false};
        final boolean[] isGap = {false};

        timer.addActionListener(e -> {
            if (i[0] >= morse.length()) {
                timer.stop();
                lamp.setBackground(Color.BLACK);
                print("\n[SYSTEM]: Sinyal selesai. Masukkan kode:");
                state = 7;
                input.setEnabled(true);
                return;
            }
            char c = morse.charAt(i[0]);
            if (c == ' ') {
                if (!isGap[0]) {
                    lamp.setBackground(Color.BLACK);
                    timer.setDelay(unit * 5);
                    isGap[0] = true;
                } else {
                    lamp.setBackground(Color.BLACK);
                    timer.setDelay(unit * 5);
                    isGap[0] = false;
                    i[0]++;
                }
            } else {
                if (!isOn[0]) {
                    lamp.setBackground(Color.WHITE);
                    timer.setDelay(c == '-' ? unit * 3 : unit * 1);
                    isOn[0] = true;
                } else {
                    lamp.setBackground(Color.BLACK);
                    timer.setDelay(unit * 2);
                    isOn[0] = false;
                    i[0]++;
                }
            }
            lamp.repaint();
        });
        timer.start();
    }
    
    // Tambah method ini di EscapeRoomGUI.java
    public void nextFromSlidingPuzzle() {
        showTextMode();
        clear();
        print("--- SISTEM TERHUBUNG ---");
        print("");
        print("Klik. Klik. Klik.");
        print("Pintu besi di ujung ruangan bergetar.");
        print("");
        print("Raka menelan ludah. Di balik pintu itu...");
        print("entah apa yang menunggu.");
        print("");
        print("Tapi tidak ada pilihan lain.");
        print("Dia melangkah maju.");

        Timer t = new Timer(4000, e -> { state = 9; roomMazeIntro(); });
        t.setRepeats(false);
        t.start();
    }

    // ================================================================
    // MAIN MENU (image-based with click listener)
    // ================================================================

    void showMainMenu() {
        input.setEnabled(false);
        MainMenuPanel menu = new MainMenuPanel(this);
        centerPanel.removeAll();
        centerPanel.add(menu, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // Called from MainMenuPanel when START is clicked
    void startGame() {
        CutscenePanel cutscene = new CutscenePanel(this);
        centerPanel.removeAll();
        centerPanel.add(cutscene, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
        cutscene.play();
    }

    // ================================================================
    // INTRO
    // ================================================================

    void intro() {
        // After cutscene ends, go directly to room1
        state = 1;
        room1();
    }

    // ================================================================
    // ROOM 1  ── Jelajah ruangan, koran di meja tengah
    // ================================================================

    void room1() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/room1.png");
        scenePanel.resetCharPos();
        scenePanel.clearOnDialogShown();

        // Koran ada di meja tengah ruangan
        scenePanel.setDeskPosition(354, 379, 80, 60);
        scenePanel.setInteractHint("[ E ] Baca Koran");
        scenePanel.setInteractDialog("NARRATOR",
            "--- ROOM 1: KORAN LUSUH ---",
            "Raka menemukan koran tua di atas meja.",
            "Koran itu tampak lusuh. Ada sesuatu yang penting di sini...",
            "Perhatikan tanggal penerbitannya!"
        );

        scenePanel.enableCollision(true);
        List<Rectangle> walls = new ArrayList<>();
        scenePanel.setCollisionRects(walls);
        walls.add(new Rectangle(9, 386, 94, 166));
        walls.add(new Rectangle(201, 349, 56, 107));
        walls.add(new Rectangle(334, 368, 52, 58));
        walls.add(new Rectangle(460, 358, 55, 83));
        walls.add(new Rectangle(8, 86, 101, 132));
        walls.add(new Rectangle(5, 16, 694, 70));

        // Setelah dialog → tampilkan gambar koran zoom
        scenePanel.setOnDialogShown(() -> {
            Timer t = new Timer(1500, e -> showKoran());
            t.setRepeats(false);
            t.start();
        });

        input.setEnabled(false);
        scenePanel.requestFocusInWindow();
    }

    // ── Tampilkan panel koran zoom ───────────────────────────────
    void showKoran() {
        centerPanel.removeAll();
        centerPanel.add(new KoranPanel(this), BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
        input.setEnabled(false);
    }

    // ── Setelah koran ditutup → laptop lockscreen ────────────────
    void afterKoran() {
        BacaKoran = true;
        backToRoom1WithLaptop();
    }

    // ── Kembali ke room1 setelah baca koran — laptop jadi target interact ──
    void backToRoom1WithLaptop() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/room1.png");
        scenePanel.resetCharPos();
        scenePanel.clearOnDialogShown();

        // Posisi laptop di ruangan — sesuaikan koordinat dengan asset room1.png
        scenePanel.setDeskPosition(318, 106, 80, 60); // ← sesuaikan koordinat laptop
        scenePanel.setInteractHint("[ E ] Buka Laptop");
        scenePanel.setInteractDialog("RAKA",
            "Laptop ini terkunci.",
            "Mungkin tanggal di koran tadi adalah passwordnya..."
        );

        scenePanel.enableCollision(true);
        List<Rectangle> walls = new ArrayList<>();
        scenePanel.setCollisionRects(walls);
        walls.add(new Rectangle(9, 386, 94, 166));
        walls.add(new Rectangle(201, 349, 56, 107));
        walls.add(new Rectangle(334, 368, 52, 58));
        walls.add(new Rectangle(460, 358, 55, 83));
        walls.add(new Rectangle(8, 86, 101, 132));
        walls.add(new Rectangle(5, 16, 694, 70));

        // Setelah dialog laptop → buka LaptopLockPanel
        scenePanel.setOnDialogShown(() -> {
            Timer t = new Timer(800, e -> room1Laptop(LaptopLockPanel.Mode.LOCK));
            t.setRepeats(false);
            t.start();
        });

        input.setEnabled(false);
        scenePanel.requestFocusInWindow();
    }

    void room1Laptop(LaptopLockPanel.Mode mode) {
        centerPanel.removeAll();
        LaptopLockPanel laptopPanel = new LaptopLockPanel(this, mode);
        centerPanel.add(laptopPanel, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
        input.setEnabled(false);
        if (!BacaKoran) {
            JOptionPane.showMessageDialog(this, "Kamu Kurang Teliti");
            return;
        }

        SwingUtilities.invokeLater(() -> laptopPanel.requestFocusInWindow());
    }

    // ── Dipanggil dari LaptopLockPanel saat password koran benar ─
    public void openLaptopClueJam() {
        room1Laptop(LaptopLockPanel.Mode.CLUE_JAM);
    }

    // ── Dipanggil dari LaptopLockPanel saat jawaban jam benar ────
    public void openLaptopClueBrankas() {
        room1Laptop(LaptopLockPanel.Mode.CLUE_BRANKAS);
    }

    // ── Kembali ke room1 scene (setelah baca clue brankas) ───────
    public void backToRoom1() {
        // Cek di mode mana kita sekarang — kalau sudah di CLUE_BRANKAS berarti
        // pemain perlu cari brankas
        showSceneMode();
        scenePanel.setBackground("/asset/bg/room1.png");
        scenePanel.resetCharPos();
        scenePanel.clearOnDialogShown();

        // Sekarang koran sudah dibaca & laptop sudah dibuka.
        // Brankas ada di pojok kiri bawah (sesuai roomAsset1 lama)
        scenePanel.setDeskPosition(39, 387, 150, 150);
        scenePanel.setInteractHint("[ E ] Buka Brankas");



        scenePanel.setInteractDialog("RAKA",
            "Ini... brankas.",
            "Di laptop tadi ada clue: angka trending 2025.",
            "Pasti salah satu dari tiga tombol ini adalah kodenya!"
        );

        scenePanel.setOnDialogShown(() -> {
            Timer t = new Timer(1500, e -> roomBrankas());
            t.setRepeats(false);
            t.start();
        });

        input.setEnabled(false);
        scenePanel.requestFocusInWindow();
    }

    // ── Panel brankas interaktif ─────────────────────────────────
    void roomBrankas() {
        centerPanel.removeAll();
        centerPanel.add(new BrankasRoom1(this), BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
        input.setEnabled(false);
    }

    // ── Dipanggil dari BrankasRoom1 saat kode 67 dipilih (benar) ─
    public void brankasOpened() {
        // Tampilkan panel pintu
        showPintu();
    }

    // ── Setelah brankas dibuka → kembali ke scene room1, player
    //    harus jalan ke pintu dan tekan E untuk interact
    void showPintu() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/room1.png");
        scenePanel.resetCharPos();
        scenePanel.clearOnDialogShown();

        // Pintu ada di dekat jam 06:00 (pojok kiri atas gambar room1)
        scenePanel.setDeskPosition(148, 100, 80, 80);
        scenePanel.setInteractHint("[ E ] Gunakan Kunci Emas");
        scenePanel.setInteractDialog("RAKA",
            "Ini pintu keluarnya!",
            "Aku punya kunci emasnya dari brankas tadi.",
            "Tapi... ada keypad PIN di sini.",
            "Aku harus ingat jawaban teka-teki jam dari laptop."
        );

        scenePanel.enableCollision(true);
        java.util.List<java.awt.Rectangle> walls = new java.util.ArrayList<>();
        scenePanel.setCollisionRects(walls);
        walls.add(new java.awt.Rectangle(9, 386, 94, 166));
        walls.add(new java.awt.Rectangle(201, 349, 56, 107));
        walls.add(new java.awt.Rectangle(334, 368, 52, 58));
        walls.add(new java.awt.Rectangle(460, 358, 55, 83));
        walls.add(new java.awt.Rectangle(8, 86, 101, 132));
        walls.add(new java.awt.Rectangle(5, 16, 694, 70));

        scenePanel.setOnDialogShown(() -> {
            Timer t = new Timer(1200, e -> openPintuPanel());
            t.setRepeats(false);
            t.start();
        });

        input.setEnabled(false);
        scenePanel.requestFocusInWindow();
    }

    // ── Buka panel input PIN (setelah interact pintu di scene) ───
    void openPintuPanel() {
        centerPanel.removeAll();
        PintuRoom1 pintuPanel = new PintuRoom1(this);
        centerPanel.add(pintuPanel, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
        input.setEnabled(false);
        SwingUtilities.invokeLater(() -> pintuPanel.requestFocusInWindow());
    }

    // ── Dipanggil dari PintuRoom1 saat PIN 0830 benar ────────────
    public void doorOpened() {
        // Pintu terbuka! Lanjut ke room2 (atau room berikutnya dalam alur)
        state = 2;
        room4();
    }

    // ================================================================
    // ROOM 2 — Suara dari balik dinding (setelah pintu terbuka)
    // ================================================================

    void room2() {
        showTextMode();
        clear();
        print("--- ROOM 2: SUARA DARI BALIK DINDING ---");
        print("Raka berhasil keluar dari room pertama!");
        print("Ruangan berikutnya penuh foto-foto anak.");
        print("Di sudut, rekaman tua berputar...");
        print("  'Diam. Jalan terus. Minta sampai dapat.'");
        print("Di cermin retak, bayangan berbisik.");
        print("  1. Menyerah dan tunggu dijemput.");
        print("  2. Terus cari jalan keluar.");
        print("  3. Ikut saja apa kata mereka.");
        print("Mana pilihanmu?");
        input.setEnabled(true);
        input.requestFocusInWindow();
    }

    // ================================================================
    // ROOM 3 — Nama di daftar
    // ================================================================

    void room3() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/room1.png");
        scenePanel.setDialog("NARRATOR",
            "--- ROOM 3: NAMAMU ADA DI SINI ---",
            "Di meja, sebuah buku catatan terbuka.",
            "Daftar nama anak-anak. Puluhan. Semua sudah dicoret — kecuali satu.",
            "Paling bawah. Nama Raka. Belum dicoret.",
            "Tekad Raka makin kuat. Dia harus keluar."
        );
        input.setEnabled(true);
        input.requestFocusInWindow();
    }

    // ================================================================
    // ROOM 4 — Memori
    // ================================================================

    void room4() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/Ruangan_2.png");
        scenePanel.resetCharPos();
        scenePanel.clearOnDialogShown();

        scenePanel.setDeskPosition(40, 255, 60, 60);
        scenePanel.setInteractHint("[ E ] Lihat Tablet");
        scenePanel.setInteractDialog("NARRATOR",
            "--- PUZZLE 4: TABLET MISTERIUS ---",
            "Raka menemukan tablet yang menyala di sudut ruangan.",
            "Layarnya menampilkan deretan angka yang berkedip...",
            "Ingat urutannya dengan baik!"
        );

        scenePanel.enableCollision(true);
        List<Rectangle> walls = new ArrayList<>();
        scenePanel.setCollisionRects(walls);
        walls.add(new Rectangle(15, 236, 47, 75));
        walls.add(new Rectangle(3, 0, 697, 148));
        walls.add(new Rectangle(225, 412, 20, 21));
        walls.add(new Rectangle(478, 415, 17, 17));
        walls.add(new Rectangle(245, 272, 21, 15));
        walls.add(new Rectangle(461, 271, 20, 13));
        walls.add(new Rectangle(338, 388, 41, 23));

        scenePanel.setOnDialogShown(() -> {
            Timer t = new Timer(1500, e -> showMemoriPuzzle());
            t.setRepeats(false);
            t.start();
        });

        input.setEnabled(false);
        scenePanel.requestFocusInWindow();
    }

    void showMemoriPuzzle() {
        input.setEnabled(false);
        clear();

        // === Panel tablet yang menampilkan kode LALU input langsung di tablet ===
        JPanel tabletScreen = new JPanel(new BorderLayout());
        tabletScreen.setBackground(Color.BLACK);

        // Area teks atas (tampilkan kode)
        JTextArea tabletDisplay = new JTextArea();
        tabletDisplay.setEditable(false);
        tabletDisplay.setBackground(Color.BLACK);
        tabletDisplay.setForeground(new Color(50, 255, 50));
        tabletDisplay.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tabletDisplay.setMargin(new Insets(8, 10, 8, 10));
        tabletDisplay.setLineWrap(true);
        tabletDisplay.setWrapStyleWord(true);
        tabletDisplay.append("[ TABLET ] Layar menyala...\n");
        tabletDisplay.append("Hafalkan kode berikut!\n");

        // Area input bawah (di dalam tablet)
        JPanel inputArea = new JPanel(new BorderLayout());
        inputArea.setBackground(new Color(10, 10, 30));
        inputArea.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(50, 255, 50), 1));

        JLabel inputLabel = new JLabel("Masukkan kode: ", JLabel.RIGHT);
        inputLabel.setForeground(new Color(50, 255, 50));
        inputLabel.setFont(new Font("Monospaced", Font.BOLD, 12));

        JTextField tabletInput = new JTextField();
        tabletInput.setBackground(new Color(5, 5, 20));
        tabletInput.setForeground(Color.WHITE);
        tabletInput.setCaretColor(Color.WHITE);
        tabletInput.setFont(new Font("Monospaced", Font.BOLD, 13));
        tabletInput.setEnabled(false); // aktif setelah kode disembunyikan

        inputArea.add(inputLabel, BorderLayout.WEST);
        inputArea.add(tabletInput, BorderLayout.CENTER);

        tabletScreen.add(new JScrollPane(tabletDisplay), BorderLayout.CENTER);
        tabletScreen.add(inputArea, BorderLayout.SOUTH);

        TabletPanel tablet = new TabletPanel(tabletScreen, TabletPanel.Mode.FULLSCREEN_PUZZLE);
        centerPanel.removeAll();
        centerPanel.add(tablet, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();

        String kode = "8821";

        // Tampilkan kode setelah 800ms
        Timer showKode = new Timer(800, e -> {
            tabletDisplay.append("\n>>> KODE: " + kode + " <<<\n");
            tabletDisplay.append("(Kode akan disembunyikan...)\n");
        });
        showKode.setRepeats(false);
        showKode.start();

        // Sembunyikan kode, aktifkan input di tablet setelah 2800ms
        Timer hideKode = new Timer(2800, e -> {
            tabletDisplay.setText("");
            tabletDisplay.append("[ TABLET ] Kode telah disembunyikan.\n");
            tabletDisplay.append("Masukkan kode yang tadi kamu lihat:\n");
            tabletInput.setEnabled(true);
            SwingUtilities.invokeLater(() -> tabletInput.requestFocusInWindow());
            state = 6;

            tabletInput.addActionListener(ev -> {
                String val = tabletInput.getText().trim();
                tabletInput.setText("");
                if (val.equals("8821")) {
                    tabletDisplay.append("\n✓ KODE BENAR!\n");
                    tabletInput.setEnabled(false);
                    Timer next = new Timer(800, ex -> { state = 7; room5(); });
                    next.setRepeats(false);
                    next.start();
                } else {
                    tabletDisplay.append("✗ Salah! Coba lagi.\n");
                }
            });
        });
        hideKode.setRepeats(false);
        hideKode.start();
    }

    // ================================================================
    // ROOM 5 — Morse
    // ================================================================

    void room5() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/Ruangan_2.png");
        scenePanel.resetCharPos();
        scenePanel.clearOnDialogShown();

        scenePanel.setDeskPosition(40, 255, 60, 60);
        scenePanel.setInteractHint("[ E ] Lihat Tablet");
        scenePanel.setInteractDialog("NARRATOR",
            "--- PUZZLE 5: SINYAL MORSE ---",
            "Tablet menyala lagi. Kali ini beda...",
            "Layarnya berkedip dalam pola aneh.",
            "Perhatikan cahayanya. Itu pesan tersembunyi!"
        );

        scenePanel.enableCollision(false);

        scenePanel.setOnDialogShown(() -> {
            Timer t = new Timer(1500, e -> showMorsePuzzle());
            t.setRepeats(false);
            t.start();
        });

        input.setEnabled(false);
        scenePanel.requestFocusInWindow();
    }

        void showMorsePuzzle() {
        // ── Layout tablet: lampu morse + teks + input field (semua di dalam tablet) ──
        JPanel tabletScreen = new JPanel(new BorderLayout());
        tabletScreen.setBackground(Color.BLACK);

        // Panel atas: lampu + teks
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.BLACK);

        // Lampu morse — bulat, bisa nyala/mati
        JPanel tabletLamp = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                int s = Math.min(getWidth(), getHeight()) - 10;
                g2.fillOval((getWidth()-s)/2, (getHeight()-s)/2, s, s);
                g2.setColor(new Color(80, 80, 80));
                g2.setStroke(new BasicStroke(2));
                g2.drawOval((getWidth()-s)/2, (getHeight()-s)/2, s, s);
            }
        };
        tabletLamp.setBackground(Color.BLACK);
        tabletLamp.setPreferredSize(new Dimension(60, 60));
        tabletLamp.setOpaque(false);

        JPanel lampWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        lampWrapper.setBackground(Color.BLACK);
        lampWrapper.add(tabletLamp);
        lampWrapper.setPreferredSize(new Dimension(100, 70));

        JTextArea tabletText = new JTextArea();
        tabletText.setEditable(false);
        tabletText.setBackground(Color.BLACK);
        tabletText.setForeground(new Color(50, 255, 50));
        tabletText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        tabletText.setMargin(new Insets(5, 8, 5, 8));
        tabletText.setLineWrap(true);
        tabletText.setWrapStyleWord(true);
        tabletText.append("[ TABLET ] Sinyal Morse\n");
        tabletText.append("Perhatikan lampu berkedip!\n");
        tabletText.append("Catat pola: titik (.) dan garis (-)\n");
        tabletText.append("lalu terjemahkan ke angka.\n");

        topPanel.add(lampWrapper, BorderLayout.WEST);
        topPanel.add(new JScrollPane(tabletText), BorderLayout.CENTER);

        // Panel bawah: input + tombol ulangi
        JPanel inputArea = new JPanel(new BorderLayout());
        inputArea.setBackground(new Color(10, 10, 30));
        inputArea.setBorder(BorderFactory.createLineBorder(new Color(50, 255, 50), 1));

        JLabel inputLabel = new JLabel(" Jawaban: ", JLabel.CENTER);
        inputLabel.setForeground(new Color(50, 255, 50));
        inputLabel.setFont(new Font("Monospaced", Font.BOLD, 12));

        JTextField morseInput = new JTextField();
        morseInput.setBackground(new Color(5, 5, 20));
        morseInput.setForeground(Color.WHITE);
        morseInput.setCaretColor(Color.WHITE);
        morseInput.setFont(new Font("Monospaced", Font.BOLD, 14));
        morseInput.setEnabled(false);

        // ── Tombol Ulangi Morse ──────────────────────────────────────
        JButton btnUlangi = new JButton("↺ Ulangi Morse");
        btnUlangi.setBackground(new Color(20, 20, 50));
        btnUlangi.setForeground(new Color(50, 255, 50));
        btnUlangi.setFont(new Font("Monospaced", Font.BOLD, 11));
        btnUlangi.setFocusPainted(false);
        btnUlangi.setBorderPainted(true);
        btnUlangi.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUlangi.setEnabled(false); // aktif setelah morse selesai pertama kali

        inputArea.add(inputLabel, BorderLayout.WEST);
        inputArea.add(morseInput, BorderLayout.CENTER);
        inputArea.add(btnUlangi, BorderLayout.EAST);

        tabletScreen.add(topPanel, BorderLayout.CENTER);
        tabletScreen.add(inputArea, BorderLayout.SOUTH);

        TabletPanel tablet = new TabletPanel(tabletScreen, TabletPanel.Mode.FULLSCREEN_PUZZLE);
        centerPanel.removeAll();
        centerPanel.add(tablet, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();

        // ── Logika animasi morse dibungkus dalam Runnable agar bisa diulang ──
        String morse = "--... ----. ..--- .....";
        int unit = 250;
        input.setEnabled(false);

        // Pakai array agar bisa direferensikan dari lambda
        final Timer[] timerHolder = {null};

        Runnable runMorse = () -> {
            // Stop timer lama jika masih jalan
            if (timerHolder[0] != null && timerHolder[0].isRunning()) {
                timerHolder[0].stop();
            }

            morseInput.setEnabled(false);
            btnUlangi.setEnabled(false);
            tabletLamp.setBackground(Color.BLACK);
            tabletLamp.repaint();
            tabletText.append("\n--- Memutar sinyal morse... ---\n");

            final int[] idx   = {0};
            final boolean[] isOn  = {false};
            final boolean[] isGap = {false};

            Timer timer = new Timer(unit, null);
            timerHolder[0] = timer;

            timer.addActionListener(e -> {
                if (idx[0] >= morse.length()) {
                    timer.stop();
                    tabletLamp.setBackground(Color.BLACK);
                    tabletLamp.repaint();
                    tabletText.append("--- Selesai! Masukkan jawabanmu ---\n");
                    morseInput.setEnabled(true);
                    btnUlangi.setEnabled(true); // aktifkan tombol ulangi
                    SwingUtilities.invokeLater(() -> morseInput.requestFocusInWindow());
                    state = 7;
                    return;
                }
                char c = morse.charAt(idx[0]);
                if (c == ' ') {
                    if (!isGap[0]) {
                        tabletLamp.setBackground(Color.BLACK);
                        timer.setDelay(unit * 5);
                        isGap[0] = true;
                    } else {
                        tabletLamp.setBackground(Color.BLACK);
                        timer.setDelay(unit * 5);
                        isGap[0] = false;
                        idx[0]++;
                    }
                } else {
                    if (!isOn[0]) {
                        tabletLamp.setBackground(Color.WHITE);
                        timer.setDelay(c == '-' ? unit * 3 : unit * 1);
                        isOn[0] = true;
                    } else {
                        tabletLamp.setBackground(Color.BLACK);
                        timer.setDelay(unit * 2);
                        isOn[0] = false;
                        idx[0]++;
                    }
                }
                tabletLamp.repaint();
            });
            timer.start();
        };

        // Tombol ulangi: stop timer lama, reset, putar ulang
        btnUlangi.addActionListener(e -> {
            morseInput.setText("");
            runMorse.run();
        });

        // Input jawaban
        morseInput.addActionListener(ev -> {
            String val = morseInput.getText().trim();
            morseInput.setText("");
            if (val.equals("7925")) {
                tabletText.append("BENAR! Kode morse terpecahkan.\n");
                morseInput.setEnabled(false);
                btnUlangi.setEnabled(false);
                if (timerHolder[0] != null) timerHolder[0].stop();
                Timer next = new Timer(800, ex -> { state = 8; room6(); });
                next.setRepeats(false);
                next.start();
            } else {
                tabletText.append("Salah. Coba lagi!\n");
            }
        });

        // Mulai morse pertama kali
        runMorse.run();
    }

    // ================================================================
    // ROOM 6 — Ball Sort
    // ================================================================

    void room6() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/Ruangan_2.png");
        scenePanel.resetCharPos();
        scenePanel.clearOnDialogShown();

        scenePanel.setDeskPosition(40, 255, 60, 60);
        scenePanel.setInteractHint("[ E ] Lihat Tablet");
        scenePanel.setInteractDialog("NARRATOR",
            "--- PUZZLE 6: SORTIR BOLA ---",
            "Tablet nyala lagi. Ada mini-game di dalamnya.",
            "Pisahkan bola berdasarkan warnanya ke tabung masing-masing.",
            "Jangan sampai tercampur!"
        );

        scenePanel.enableCollision(false);

        scenePanel.setOnDialogShown(() -> {
            Timer t = new Timer(1500, e -> showBallSortPuzzle());
            t.setRepeats(false);
            t.start();
        });

        input.setEnabled(false);
        scenePanel.requestFocusInWindow();
    }

    void showBallSortPuzzle() {
        input.setEnabled(false);
        centerPanel.removeAll();
        BallSortPanel ballSort = new BallSortPanel(this);
        TabletPanel tablet = new TabletPanel(ballSort, TabletPanel.Mode.FULLSCREEN_PUZZLE);
        centerPanel.add(tablet, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    public void nextFromBallSort() {
        state = 8;
        room7();
    }

    // ================================================================
    // ROOM 7 — Sliding Puzzle
    // ================================================================

    void room7() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/Ruangan_2.png");
        scenePanel.resetCharPos();
        scenePanel.clearOnDialogShown();

        scenePanel.setDeskPosition(40, 255, 60, 60);
        scenePanel.setInteractHint("[ E ] Lihat Tablet");
        scenePanel.setInteractDialog("NARRATOR",
            "--- PUZZLE 7: BALL SORT ---",
            "Tablet menyala untuk terakhir kalinya.",
            "Bola-Bola acak memenuhi layar.",
            "Masukkan semua bola hingga menjadi 1 warna yang sama!"
        );

        scenePanel.enableCollision(false);

        scenePanel.setOnDialogShown(() -> {
            Timer t = new Timer(1500, e -> showSlidingPuzzle());
            t.setRepeats(false);
            t.start();
        });

        input.setEnabled(false);
        scenePanel.requestFocusInWindow();
    }

    void showSlidingPuzzle() {
        input.setEnabled(false);
        centerPanel.removeAll();
        SlidingPuzzlePanel sliding = new SlidingPuzzlePanel(this);
        TabletPanel tablet = new TabletPanel(sliding, TabletPanel.Mode.FULLSCREEN_PUZZLE);
        centerPanel.add(tablet, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // ================================================================
    // ROOM 8
    // ================================================================

    void room8() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/room2.jpeg");
        scenePanel.setDialog("NARRATOR",
            "--- ROOM 8: RENCANA MASA DEPAN ---",
            "Hidupmu yang berantakan harus disusun kembali.",
            "Urutkan kepingan ini dari yang terkecil.",
            "Kalau sudah rapi, pintu akan terbuka."
        );
        input.setEnabled(true);
        input.requestFocusInWindow();
    }

    // ================================================================
    // MAZE INTRO
    // ================================================================

    void roomMazeIntro() {
        showTextMode();
        clear();
        print("--- KONFRONTASI ---");
        print("Raka berhasil membuka pintu room 8. Udara segar dari lorong menyambutnya.");
        print("Tapi —");
        print("Sebuah sosok berdiri di ujung lorong. Topeng putih. Tubuh besar. Diam.");
        print("Lalu dia berbicara, pelan tapi dingin:");
        print("");
        print("  \"Kamu pintar. Anak pintar... harganya mahal.\"");
        print("");
        print("Raka tidak menunggu kalimat berikutnya. Dia berlari.");
        print("");
        Timer t = new Timer(3000, e -> {
            centerPanel.removeAll();
            centerPanel.add(scenePanel, BorderLayout.CENTER);
            centerPanel.revalidate();
            centerPanel.repaint();
            input.setEnabled(false);
            scenePanel.requestFocusInWindow();
            scenePanel.enableFog(true);
            scenePanel.setBackground("/asset/bg/maze.png");
            scenePanel.setCharPos(62, 83);
            scenePanel.setRenderScale(0.5f);
            scenePanel.enableCollision(true);

            List<Rectangle> walls = new ArrayList<>();
            scenePanel.setCollisionRects(walls);
            walls.add(new Rectangle(399, 516, 74, 3));
            walls.add(new Rectangle(614, 452, 22, 54));
            walls.add(new Rectangle(2, 2, 51, 313));
            walls.add(new Rectangle(56, 226, 80, 5));
            walls.add(new Rectangle(44, 378, 6, 137));
            walls.add(new Rectangle(50, 445, 139, 5));
            walls.add(new Rectangle(191, 374, 35, 78));
            walls.add(new Rectangle(106, 302, 31, 76));
            walls.add(new Rectangle(138, 301, 135, 6));
            walls.add(new Rectangle(275, 302, 32, 75));
            walls.add(new Rectangle(308, 374, 141, 4));
            walls.add(new Rectangle(449, 374, 32, 79));
            walls.add(new Rectangle(481, 447, 90, 4));
            walls.add(new Rectangle(172, 518, 71, 39));
            walls.add(new Rectangle(245, 516, 34, 5));
            walls.add(new Rectangle(271, 443, 4, 73));
            walls.add(new Rectangle(310, 444, 3, 73));
            walls.add(new Rectangle(310, 516, 49, 5));
            walls.add(new Rectangle(344, 522, 11, 34));
            walls.add(new Rectangle(314, 448, 84, 4));
            walls.add(new Rectangle(470, 518, 31, 38));
            walls.add(new Rectangle(554, 516, 18, 39));
            walls.add(new Rectangle(530, 302, 41, 77));
            walls.add(new Rectangle(447, 300, 79, 5));
            walls.add(new Rectangle(443, 150, 38, 148));
            walls.add(new Rectangle(481, 224, 94, 8));
            walls.add(new Rectangle(615, 226, 73, 6));
            walls.add(new Rectangle(614, 299, 44, 7));
            walls.add(new Rectangle(0, 316, 4, 244));
            walls.add(new Rectangle(614, 372, 44, 9));
            walls.add(new Rectangle(46, 4, 654, 25));
            walls.add(new Rectangle(101, 80, 36, 77));
            walls.add(new Rectangle(137, 150, 50, 7));
            walls.add(new Rectangle(185, 153, 38, 79));
            walls.add(new Rectangle(225, 224, 135, 8));
            walls.add(new Rectangle(358, 225, 35, 80));
            walls.add(new Rectangle(273, 81, 35, 80));
            walls.add(new Rectangle(357, 84, 39, 71));
            walls.add(new Rectangle(182, 29, 42, 62));
            walls.add(new Rectangle(451, 30, 29, 58));
            walls.add(new Rectangle(525, 84, 37, 73));
            walls.add(new Rectangle(571, 124, 12, 32));
            walls.add(new Rectangle(564, 81, 10, 39));
            walls.add(new Rectangle(637, 34, 55, 121));
            walls.add(new Rectangle(629, 121, 5, 36));
            walls.add(new Rectangle(638, 468, 62, 6));
            walls.add(new Rectangle(396, 151, 46, 6));
            walls.add(new Rectangle(53, 516, 73, 3));
walls.add(new Rectangle(312, 82, 44, 6));

            scenePanel.setDeskPosition(657, 484, 60, 60);
            scenePanel.setInteractHint("[ E ] Kabur!");
            scenePanel.setInteractDialog("RAKA", "Pintunya! Harus kabur sekarang!");
            scenePanel.setOnDialogShown(() -> {
                Timer t2 = new Timer(1500, ev -> ending());
                t2.setRepeats(false);
                t2.start();
            });
        });
        t.setRepeats(false);
        t.start();
    }

    // ================================================================
    // ENDING & GAME OVER
    // ================================================================

    void ending() {
        // Urutan ending:
        //   1. ending.gif       — durasi sesuai panjang GIF
        //   2. showEndingText() — tampil selama 4 detik
        //   3. theend.gif       — tampil selama 8 detik
        //   4. kembali ke main menu
        centerPanel.removeAll();
        centerPanel.add(scenePanel, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
        input.setEnabled(false);
        scenePanel.enableFog(false);

        // Langkah 1: putar ending.gif — sesuaikan durasinya dengan panjang GIF kamu
        int endingGifDurationMs = 9000; // ganti sesuai durasi ending.gif
        scenePanel.playEndingGif("/asset/bg/ending.gif", endingGifDurationMs, () -> {
            // Langkah 2: tampilkan teks ending selama 4 detik
            showEndingText();
        });
    }

    /**
     * Teks penutup setelah ending.gif selesai.
     * Ditampilkan selama 4 detik, lalu putar theend.gif 8 detik, kemudian main menu.
     */
    void showEndingText() {
        showTextMode();
        clear();
        print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        print("        KAMU BERHASIL KABUR!          ");
        print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        print("");
        print("Raka berlari keluar rumah itu.");
        print("Napasnya terengah, tapi dia tidak berhenti.");
        print("Di kejauhan, jalan raya ramai terlihat.");
        print("");
        print("Penjahat hanya berdiri di pagar.");
        print("Dia tidak berani keluar.");
        print("");
        print(" Aku akan pulang. Aku pasti baik-baik saja.");
        print("");
        print("Raka selamat.");
        print("");
        print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        print("      TERIMA KASIH SUDAH BERMAIN!");
        print("           — Kelompok 7 —");
        print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        input.setEnabled(false);

        // Langkah 3: setelah 4 detik tampilkan theend.gif selama 8 detik lalu ke main menu
        Timer t = new Timer(4000, e -> {
            centerPanel.removeAll();
            centerPanel.add(scenePanel, BorderLayout.CENTER);
            centerPanel.revalidate();
            centerPanel.repaint();
            scenePanel.playEndingGif("/asset/bg/theend.gif", 15000, () -> {
                state = 0;
                BacaKoran = false;
                scenePanel.resetCharPos();
                scenePanel.clearInteractDialog();
                showMainMenu();
            });
        });
        t.setRepeats(false);
        t.start();
    }

    void gameOver() {
        state = 99;
        input.setEnabled(false);
        GameOverPanel goPanel = new GameOverPanel(this);
        centerPanel.removeAll();
        centerPanel.add(goPanel, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // Called from GameOverPanel Play Again button
    void restartGame() {
        state = 0;
        scenePanel.resetCharPos();
        scenePanel.clearInteractDialog();
        BacaKoran = false;
        showMainMenu();
    }

    // ================================================================
    // INPUT HANDLER & STATE MACHINE
    // (Puzzle 1-3 kini ditangani oleh panel masing-masing,
    //  state machine di sini untuk room 2+ dan debug commands)
    // ================================================================

    void handleInput(String inputUser) {
        // ── Debug commands ──
        if (inputUser.startsWith("/")) {
            String room = inputUser.toLowerCase();
            if (room.equals("/room1"))   { state = 1; room1();        return; }
            if (room.equals("/room2"))   { state = 2; room2();        return; }
            if (room.equals("/room3"))   { state = 3; room3();        return; }
            if (room.equals("/room4"))   { state = 4; room4();        return; }
            if (room.equals("/room5"))   { state = 5; room5();        return; }
            if (room.equals("/room6"))   { state = 7; room6();        return; }
            if (room.equals("/room7"))   { state = 8; room8();        return; }
            if (room.equals("/room8"))   { state = 9; roomMazeIntro(); return; }
            if (room.equals("/ending"))  { state = 9; ending();       return; }
            if (room.equals("/koran"))   { showKoran();               return; }
            if (room.equals("/laptop"))  { room1Laptop(LaptopLockPanel.Mode.LOCK); if (!BacaKoran) return; }
            if (room.equals("/brankas")) { roomBrankas();             return; }
            if (room.equals("/pintu"))   { showPintu();               return; }
        }

        // ── Game Over — ditangani GameOverPanel, bukan text input ──
        if (state == 99) return;

        // ── State machine (room 2 dst.) ──
        switch (state) {
            case 0 -> { state = 1; room1(); }

            // state 1 = jelajah room1 (dikontrol ScenePanel / KoranPanel / LaptopLockPanel)
            // Tidak ada text input di state ini — semua dikontrol via panel

            // Room 2: pilihan cermin
            case 2 -> {
                if (inputUser.equals("2")) { state = 3; room4(); }
                else gameOver();
            }

            // Room 3 → lanjut ke room4 (memori)
            case 3 -> { state = 4; room4(); }

            // Room 4: input memori ditangani langsung di TabletPanel (state 6)
            // case 6 — handled inside showMemoriPuzzle tablet input

            // Room 5: input morse ditangani di dalam TabletPanel (morseInput)
            // case 7 — handled inside showMorsePuzzle tablet input

            // Room 7/8: input sliding puzzle → maze
            case 8 -> {
                if (inputUser.equals("9999")) { state = 9; roomMazeIntro(); }
                else gameOver();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EscapeRoomGUI());
    }
}