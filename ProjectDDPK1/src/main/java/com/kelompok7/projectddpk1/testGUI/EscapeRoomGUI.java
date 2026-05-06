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

        centerPanel = new JPanel(new BorderLayout());
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

        CutscenePanel cutscene = new CutscenePanel(this);
        centerPanel.removeAll();
        centerPanel.add(cutscene, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();

        setLocationRelativeTo(null);
        setVisible(true);
        SwingUtilities.invokeLater(() -> {
            input.requestFocusInWindow();
            input.setEnabled(false);
            cutscene.play();
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

    // ================================================================
    // INTRO
    // ================================================================

    void intro() {
        showTextMode();
        clear();
        print("=== COMDEV: Commit of Development ===");
        print("Siang yang biasa. Lapangan ramai, tawa anak-anak memenuhi udara.");
        print("Raka berjalan menjauh dari kelompoknya...");
        print("Matanya tertangkap sebuah mobil gelap di pinggir jalan.");
        print("Di balik kaca — jajanan kesukaannya tersusun rapi,");
        print("seperti sengaja dipajang.");
        print("");
        print("Dia mendekat. Pintu terbuka sendiri.");
        print("...");
        print("Klik. Terkunci.");
        print("Gas menyebar. Mata Raka berat.");
        print("Gelap.");
        print("");
        print("(Tekan Enter untuk melanjutkan...)");
        input.setEnabled(true);
        input.requestFocusInWindow();
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
        walls.add(new Rectangle(324, 355, 49, 48));
        walls.add(new Rectangle(448, 334, 57, 98));
        walls.add(new Rectangle(200, 332, 55, 110));
        walls.add(new Rectangle(12, 376, 78, 146));
        walls.add(new Rectangle(12, 80, 102, 131));
        walls.add(new Rectangle(617, 93, 59, 56));
        walls.add(new Rectangle(4, 6, 672, 64));

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
        walls.add(new Rectangle(324, 355, 49, 48));
        walls.add(new Rectangle(448, 334, 57, 98));
        walls.add(new Rectangle(200, 332, 55, 110));
        walls.add(new Rectangle(12, 376, 78, 146));
        walls.add(new Rectangle(12, 80, 102, 131));
        walls.add(new Rectangle(617, 93, 59, 56));
        walls.add(new Rectangle(4, 6, 672, 64));

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
        scenePanel.setDeskPosition(49, 396, 150, 150);
        scenePanel.setInteractHint("[ E ] Buka Brankas");

        scenePanel.enableCollision(true);
        List<Rectangle> walls = new ArrayList<>();
        scenePanel.setCollisionRects(walls);
        walls.add(new Rectangle(324, 355, 49, 48));
        walls.add(new Rectangle(448, 334, 57, 98));
        walls.add(new Rectangle(200, 332, 55, 110));
        walls.add(new Rectangle(12, 376, 78, 146));
        walls.add(new Rectangle(12, 80, 102, 131));
        walls.add(new Rectangle(617, 93, 59, 56));
        walls.add(new Rectangle(4, 6, 672, 64));

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

    // ── Panel pintu — butuh kunci emas + PIN 0830 ─────────────────
    void showPintu() {
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
        room2();
    }

    // ================================================================
    // ROOM 2 — Suara dari balik dinding (setelah pintu terbuka)
    // ================================================================

    void room2() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/room1.png");
        scenePanel.setDialog("NARRATOR",
            "--- ROOM 2: SUARA DARI BALIK DINDING ---",
            "Raka berhasil keluar dari room pertama!",
            "Ruangan berikutnya penuh foto-foto anak.",
            "Di sudut, rekaman tua berputar...",
            "  'Diam. Jalan terus. Minta sampai dapat.'",
            "Di cermin retak, bayangan berbisik.",
            "  1. Menyerah dan tunggu dijemput.",
            "  2. Terus cari jalan keluar.",
            "  3. Ikut saja apa kata mereka.",
            "Mana pilihanmu?"
        );
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
        showTextMode();
        clear();
        input.setEnabled(false);
        print("--- ROOM 5: MEMORI ---");
        print("Seringkali kita lupa tujuan awal karena terlalu sibuk.");
        print("Simpan angka ini baik-baik di kepalamu...");
        String kode = "8821";
        print("\nINGAT KODE INI: " + kode);
        Timer t = new Timer(2000, e -> {
            clear();
            print("Masukkan kembali kode memori tadi:");
            state = 6;
            input.setEnabled(true);
        });
        t.setRepeats(false);
        t.start();
    }

    // ================================================================
    // ROOM 5 — Morse
    // ================================================================

    void room5() {
        showLampMode();
        clear();
        print("--- ROOM 6: SINYAL DARI LUAR ---");
        print("Di tempat gelap, selalu ada cahaya.");
        print("Perhatikan lampu itu, dia membisikkan sebuah kode.");
        print("Tulis kodenya di sini...");
        playMorse("--... ----. ..--- .....");
    }

    // ================================================================
    // ROOM 6 — Ball Sort
    // ================================================================

    void room6() {
        showTextMode();
        clear();
        print("--- ROOM EXTRA: SORTIR JIWA ---");
        print("Kumpulkan warna yang sama dalam satu wadah.");
        print("Klik tabung untuk mengambil, klik tabung lain untuk menaruh.");
        input.setEnabled(false);
        centerPanel.removeAll();
        centerPanel.add(new BallSortPanel(this), BorderLayout.CENTER);
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
        showTextMode();
        clear();
        print("--- ROOM 7: FINAL COMMIT ---");
        print("Susun puzzle ini untuk menyelesaikan development dirimu.");
        input.setEnabled(false);
        centerPanel.removeAll();
        centerPanel.add(new SlidingPuzzlePanel(this), BorderLayout.CENTER);
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
            walls.add(new Rectangle(0, 3, 51, 299));
            walls.add(new Rectangle(96, 74, 41, 81));
            walls.add(new Rectangle(135, 139, 84, 17));
            walls.add(new Rectangle(177, 154, 42, 66));
            walls.add(new Rectangle(221, 208, 170, 16));
            walls.add(new Rectangle(348, 224, 41, 68));
            walls.add(new Rectangle(51, 213, 87, 13));
            walls.add(new Rectangle(182, 352, 38, 79));
            walls.add(new Rectangle(133, 286, 133, 10));
            walls.add(new Rectangle(263, 287, 38, 79));
            walls.add(new Rectangle(301, 353, 132, 12));
            walls.add(new Rectangle(433, 353, 36, 77));
            walls.add(new Rectangle(159, 492, 81, 35));
            walls.add(new Rectangle(239, 488, 26, 11));
            walls.add(new Rectangle(331, 501, 16, 28));
            walls.add(new Rectangle(455, 498, 31, 30));
            walls.add(new Rectangle(534, 488, 25, 42));
            walls.add(new Rectangle(625, 446, 52, 6));
            walls.add(new Rectangle(434, 280, 77, 14));
            walls.add(new Rectangle(431, 142, 39, 140));
            walls.add(new Rectangle(466, 212, 92, 11));
            walls.add(new Rectangle(591, 282, 47, 13));
            walls.add(new Rectangle(513, 71, 42, 85));
            walls.add(new Rectangle(553, 117, 17, 35));
            walls.add(new Rectangle(611, 117, 62, 36));
            walls.add(new Rectangle(618, 15, 52, 104));
            walls.add(new Rectangle(430, 8, 39, 78));
            walls.add(new Rectangle(470, 4, 148, 12));
            walls.add(new Rectangle(49, 5, 424, 15));
            walls.add(new Rectangle(177, 14, 40, 68));
            walls.add(new Rectangle(260, 71, 41, 87));
            walls.add(new Rectangle(304, 72, 42, 14));
            walls.add(new Rectangle(346, 73, 39, 82));
            walls.add(new Rectangle(385, 142, 50, 10));
            walls.add(new Rectangle(58, 488, 35, 39));
            walls.add(new Rectangle(94, 489, 23, 8));
            walls.add(new Rectangle(510, 282, 49, 82));
            walls.add(new Rectangle(595, 429, 24, 56));
            walls.add(new Rectangle(620, 448, 10, 35));
            walls.add(new Rectangle(596, 354, 40, 9));
            walls.add(new Rectangle(597, 215, 72, 6));
            walls.add(new Rectangle(101, 287, 32, 73));
            walls.add(new Rectangle(470, 424, 82, 6));
            walls.add(new Rectangle(40, 357, 12, 118));
            walls.add(new Rectangle(56, 424, 124, 6));
            walls.add(new Rectangle(263, 427, 7, 71));
            walls.add(new Rectangle(386, 486, 104, 4));
            walls.add(new Rectangle(305, 425, 78, 4));
            walls.add(new Rectangle(302, 490, 45, 7));
            walls.add(new Rectangle(300, 436, 6, 50));

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
        showSceneMode();
        scenePanel.setBackground("/asset/bg/SuperMarket.jpg");
        scenePanel.setDialog("--- KAMU BERHASIL KABUR ---",
            "Raka berlari keluar rumah itu.",
            "Napasnya terengah, tapi dia tidak berhenti.",
            "Di kejauhan, jalan raya ramai terlihat.",
            "",
            "Penjahat hanya berdiri di pagar.",
            "Dia tidak berani keluar.",
            "",
            "Raka selamat."
        );
        input.setEnabled(false);
    }

    void gameOver() {
        showSceneMode();
        scenePanel.setBackground("");
        scenePanel.setDialog("SISTEM",
            "--- SYSTEM CRASH ---",
            "Kamu gagal melakukan commit.",
            "Brain Memory leak berlebihan.",
            "",
            "GAME OVER",
            "",
            "Apakah kamu ingin mengulang? (Y/T)"
        );
        state = 99;
        input.setEnabled(true);
        input.requestFocusInWindow();
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

        // ── Game Over restart ──
        if (state == 99) {
            if (inputUser.trim().equalsIgnoreCase("Y")) {
                state = 1;
                scenePanel.resetCharPos();
                scenePanel.clearInteractDialog();
                room1();
            } else {
                System.exit(0);
            }
            return;
        }

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

            // Room 4: input memori
            case 6 -> {
                if (inputUser.equals("8821")) { state = 7; room5(); }
                else gameOver();
            }

            // Room 5: input morse
            case 7 -> {
                if (inputUser.equals("7925")) { state = 8; room6(); }
                else gameOver();
            }

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