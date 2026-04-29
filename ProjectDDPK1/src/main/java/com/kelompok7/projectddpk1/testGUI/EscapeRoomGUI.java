package com.kelompok7.projectddpk1.testGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;

public class EscapeRoomGUI extends JFrame {

    JPanel lamp;
    JTextArea display;
    JTextField input;
    JPanel centerPanel;
    MazeGUI mazePanel;
    ScenePanel scenePanel;  // panel baru untuk background + dialog
    int state = 0;

    public EscapeRoomGUI() {
        setTitle("COMDEV: Commit of Development");
        setSize(700, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // Setup Display Area (masih dipakai untuk mode morse & maze)
        display = new JTextArea();
        display.setEditable(false);
        display.setBackground(Color.BLACK);
        display.setForeground(new Color(50, 255, 50));
        display.setFont(new Font("Monospaced", Font.PLAIN, 14));
        display.setMargin(new Insets(15, 15, 15, 15));
        display.setLineWrap(true);
        display.setWrapStyleWord(true);

        // Setup Input Field
        input = new JTextField();
        input.setBackground(new Color(30, 30, 30));
        input.setForeground(Color.WHITE);
        input.setCaretColor(Color.WHITE);
        input.setFont(new Font("Consolas", Font.BOLD, 16));

        centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(display), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        add(input, BorderLayout.SOUTH);

        // Setup Lampu Morse
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

        // Inisialisasi panel-panel
        mazePanel = new MazeGUI();
        scenePanel = new ScenePanel();
        scenePanel.setOnDialogShown(() -> {
        input.setEnabled(true);
        input.requestFocusInWindow();
    });

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
        cutscene.play();
});
    }

    // ================================================================
    // HELPER: PRINT & CLEAR
    // (print() hanya bekerja saat mode teks/lamp aktif)
    // ================================================================

    void print(String text) {
        display.append(text + "\n");
        display.setCaretPosition(display.getDocument().getLength());
    }

    void clear() {
        display.setText("");
    }

    // ================================================================
    // HELPER: GANTI MODE TAMPILAN
    // Pilih salah satu sesuai kebutuhan per room:
    //   showSceneMode() -> background gambar + kotak dialog (room biasa)
    //   showTextMode()  -> layar hitam + teks hijau (fallback / morse)
    //   showLampMode()  -> lampu morse + teks di bawah
    //   showMazeMode()  -> labirin
    // ================================================================

    // Mode baru: background gambar + kotak dialog visual novel
    void showSceneMode() {
        centerPanel.removeAll();
        centerPanel.add(scenePanel, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
        scenePanel.resetCharPos();
        input.setEnabled(false); 
        scenePanel.requestFocusInWindow(); 
        SwingUtilities.invokeLater(() -> scenePanel.requestFocusInWindow());  
    }

    // Mode lama: teks hijau di layar hitam
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

    void showMazeMode() {
        centerPanel.removeAll();
        centerPanel.add(mazePanel, BorderLayout.CENTER);
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
                    if (c == '-') {
                        timer.setDelay(unit * 3);
                    } else {
                        timer.setDelay(unit * 1);
                    }
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
    // STORY — SEMUA ROOM
    //
    // Cara pakai ScenePanel di setiap room:
    //   1. showSceneMode()                        <- aktifkan mode scene
    //   2. scenePanel.setBackground("path")       <- pasang gambar background
    //   3. scenePanel.setDialog("NAMA", "teks..") <- set teks dialog
    //
    // Kalau gambar belum ada, cukup tulis "" untuk path-nya.
    // Nanti tinggal ganti path-nya saja tanpa ubah yang lain.
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
    }

    void room1() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/room1.png");
        scenePanel.resetCharPos();
        scenePanel.setDeskPosition(300, 290, 80, 60);
        scenePanel.setInteractDialog("NARRATOR",
            "--- ROOM 1: KORAN LUSUH ---",
            "Raka terbangun. Ruangan pengap, cahaya redup.",
            "Di lantai, secarik koran lusuh tergeletak.",
            "  'Sindikat pengemis anak beroperasi di kota...'",
            "  'Korban dipilih berdasarkan kecerdasan dan usia...'",
            "Di pojok koran, ada angka yang dilingkari tangan seseorang.",
            "  Target: 2026  |  Sekarang: 1000",
            "  Gunakan digit pertama target, sisanya digit sekarang.",
            "Berapa angka yang kurang?"
        );
    }

    void room2() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/room1.png");
        scenePanel.setDialog("NARRATOR",
            "--- ROOM 2: SUARA DARI BALIK DINDING ---",
            "Raka melangkah masuk ke ruangan berikutnya.",
            "Di dinding — foto-foto anak. Banyak. Dengan tanggal di bawahnya.",
            "Di pojok setiap foto, selalu ada satu sosok yang sama. Pria bertopeng.",
            "Dari sudut ruangan, rekaman tua berputar sendiri...",
            "  'Diam. Jalan terus. Minta sampai dapat.'",
            "Di jam dinding yang rusak, jarum bergerak dalam pola aneh.",
            "  01:00 -> 03:30 -> 06:00 -> ...",
            "Jam berapa selanjutnya? (Format: 0000)"
        );
        input.setEnabled(true);
        input.requestFocusInWindow();
    }

    void room3() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/room1.png");
        scenePanel.setDialog("NARRATOR",
            "--- ROOM 3: NAMAMU ADA DI SINI ---",
            "Di meja, sebuah buku catatan terbuka.",
            "Daftar nama anak-anak. Puluhan. Semua sudah dicoret — kecuali satu.",
            "Paling bawah. Nama Raka. Belum dicoret.",
            "Di balik buku, ada cermin retak. Bayangan berbisik:",
            "  1. Menyerah dan tunggu dijemput.",
            "  2. Terus cari jalan keluar.",
            "  3. Ikut saja apa kata mereka.",
            "Mana pilihanmu?"
        );
        input.setEnabled(true);
        input.requestFocusInWindow();
    }

    void roomMazeIntro() {
        showTextMode();
        clear();
        print("--- KONFRONTASI ---");
        print("Raka berhasil membuka pintu room 3. Udara segar dari lorong menyambutnya.");
        print("Tapi —");
        print("Sebuah sosok berdiri di ujung lorong. Topeng putih. Tubuh besar. Diam.");
        print("Lalu dia berbicara, pelan tapi dingin:");
        print("");
        print("  \"Kamu pintar. Anak pintar... harganya mahal.\"");
        print("");
        print("Raka tidak menunggu kalimat berikutnya. Dia berlari.");
        print("");
        Timer t = new Timer(3000, e -> {
            showMazeMode();
            print("--- KABUR! ---");
            print("Langkah kaki berat terdengar di belakang.");
            print("Lorong gelap. Banyak belokan. Raka tidak punya waktu.");
            print("(Gunakan W/A/S/D untuk kabur!)");
        });
        t.setRepeats(false);
        t.start();
    }

    void room4() {
        // Room memori — pakai text mode karena ada efek timer (teks hilang lalu muncul lagi)
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

    void room5() {
        // Room morse — pakai lamp mode
        showLampMode();
        clear();
        print("--- ROOM 6: SINYAL DARI LUAR ---");
        print("Di tempat gelap, selalu ada cahaya.");
        print("Perhatikan lampu itu, dia membisikkan sebuah kode.");
        print("Tulis kodenya di sini...");
        playMorse("--... ----. ..--- .....");
    }

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

    void room8() {
        showSceneMode();
        // Ganti "" dengan path gambar nanti, contoh: "/assets/bg/room8.png"
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

    void ending() {
        showSceneMode();
        // Ganti "" dengan path gambar ending nanti
        scenePanel.setBackground("/asset/bg/BG-Scene-Penculikan.jpg");
        scenePanel.setDialog("SISTEM",
            "--- SISTEM BERHASIL DI-UPDATE ---",
            "Klik. Pintu masa depan terbuka.",
            "Ternyata sukses itu bukan balapan sama orang lain,",
            "tapi soal seberapa berani kamu melangkah.",
            "",
            "SELAMAT! KAMU BERHASIL COMMIT."
        );
        input.setEnabled(false);
    }

    void gameOver() {
        showSceneMode();
        // Ganti "" dengan path gambar game over nanti
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
    // HANDLE INPUT & STATE MACHINE
    // ================================================================

    void handleInput(String inputUser) {
        if (inputUser.startsWith("/")) {
            String room = inputUser.toLowerCase();
            if (room.equals("/room1")) { state = 1; room1(); return; }
            if (room.equals("/room2")) { state = 2; room2(); return; }
            if (room.equals("/room3")) { state = 3; room3(); return; }
            if (room.equals("/room4")) { state = 4; roomMazeIntro(); return; }
            if (room.equals("/room5")) { state = 5; room4(); return; }
            if (room.equals("/room6")) { state = 7; room5(); return; }
            if (room.equals("/room7")) { state = 8; room6(); return; }
            if (room.equals("/room8")) { state = 9; room8(); return; }
            if (room.equals("/ending")) { state = 9; ending(); return; }
        }
         if (state == 99) {
            if (inputUser.trim().equalsIgnoreCase("Y")) {
                state = 1;
                room1();
            }   
            else {
                System.exit(0);
            }
            return;
        }
        
        switch (state) {
            case 0: state = 1; room1(); break;
            case 1: if (inputUser.equals("1026"))      { state = 2; room2(); } else gameOver(); break;
            case 2: if (inputUser.equals("0830"))      { state = 3; room3(); } else gameOver(); break;
            case 3: if (inputUser.equals("2"))         { state = 4; roomMazeIntro(); } else gameOver(); break;
            case 4:
                mazePanel.movePlayer(inputUser.toUpperCase().charAt(0));
                if (inputUser.equalsIgnoreCase("MJ"))  { state = 5; room4(); }
                if (mazePanel.isExitReached())          { state = 5; room4(); }
                break;
            case 6: if (inputUser.equals("8821"))      { state = 7; room5(); } else gameOver(); break;
            case 7: if (inputUser.equals("7925"))      { state = 8; room6(); } else gameOver(); break;
            case 8: if (inputUser.equals("9999")) { state = 9; ending(); } else gameOver(); break;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EscapeRoomGUI());
    }
}