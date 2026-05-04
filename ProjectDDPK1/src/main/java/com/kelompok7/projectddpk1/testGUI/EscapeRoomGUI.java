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
        //mazePanel = new MazeGUI(this);
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
        scenePanel.enableCollision(false); 
        scenePanel.setRenderScale(1.0f);
        scenePanel.clearOnDialogShown(); 
        scenePanel.enableFog(false);
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
        input.setEnabled(false);
        mazePanel.requestFocusInWindow();
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

    void room1() {
        showSceneMode();
        scenePanel.setBackground("/asset/bg/room1.png");
        scenePanel.resetCharPos();
        scenePanel.clearOnDialogShown(); 
        scenePanel.setDeskPosition(354,379, 80, 60);
        scenePanel.setInteractHint("[ E ] Baca Koran");
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
        
        scenePanel.enableCollision(true);
        List<Rectangle> walls = new ArrayList<>();
        scenePanel.setCollisionRects(walls);
        walls.add(new Rectangle(6, 87, 100, 124));
        walls.add(new Rectangle(104, 83, 22, 41));
        walls.add(new Rectangle(0, -1, 674, 81));
        walls.add(new Rectangle(4, 378, 110, 144));
        walls.add(new Rectangle(108, 443, 37, 37));
        walls.add(new Rectangle(202, 345, 49, 81));
        walls.add(new Rectangle(452, 355, 49, 62));
        walls.add(new Rectangle(322, 366, 51, 45));
        walls.add(new Rectangle(619, 89, 57, 60));  
        scenePanel.setOnDialogShown(() -> {
        input.setEnabled(true);
        input.requestFocusInWindow();
    });
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
    
        void roomAsset1() {
            showSceneMode();
            scenePanel.setBackground("/asset/bg/room1.png");
            scenePanel.clearOnDialogShown(); 
            scenePanel.setDeskPosition(49, 396, 150, 150);
            scenePanel.setInteractHint("[ E ] Buka Brankas");
            
            scenePanel.enableCollision(true);
            List<Rectangle> walls = new ArrayList<>();
            scenePanel.setCollisionRects(walls);
            walls.add(new Rectangle(6, 87, 100, 124));
        walls.add(new Rectangle(104, 83, 22, 41));
        walls.add(new Rectangle(0, -1, 674, 81));
        walls.add(new Rectangle(202, 345, 49, 81));
        walls.add(new Rectangle(452, 355, 49, 62));
        walls.add(new Rectangle(322, 366, 51, 45));
        walls.add(new Rectangle(619, 89, 57, 60));
        walls.add(new Rectangle(6, 407, 69, 121));
    
            // Hint & dialog saat dekat brankas
            scenePanel.setInteractDialog("RAKA",
            "Ini... brankas.",
            "Ada tiga tombol di sini.",
            "Kode yang benar pasti bisa membukanya."
            );
    
        // Setelah dialog interact muncul, tampilkan gambar brankas zoom
           scenePanel.setOnDialogShown(() -> {
            // Tunggu sebentar biar dialog kebaca, lalu tampilkan brankas
            Timer t = new Timer(1500, e -> roomBrankas());
            t.setRepeats(false);
            t.start();
            });
    
            input.setEnabled(false);
            scenePanel.requestFocusInWindow();
}
     void roomBrankas() {
        // Tampilkan panel brankas interaktif
        centerPanel.removeAll();
        centerPanel.add(new BrankasRoom1(this), BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
        input.setEnabled(false); // input dinonaktifkan, kontrol via klik mouse
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
        // 5. Set interaksi pintu keluar
        scenePanel.setDeskPosition(657, 484, 60, 60);
        scenePanel.setInteractHint("[ E ] Kabur!");
        scenePanel.setInteractDialog("RAKA", "Pintunya! Harus kabur sekarang!");
        scenePanel.setOnDialogShown(() -> {
            Timer t2 = new Timer(1500, ev -> {
                state = 5;
                room4();
            });
            t2.setRepeats(false);
            t2.start();
        });
    });
    t.setRepeats(false);
    t.start();
    }

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
        scenePanel.setBackground("/asset/bg/SuperMarket.jpg");
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
                scenePanel.resetCharPos();
                scenePanel.clearInteractDialog();
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
            case 2: if (inputUser.equals("0830"))      { state = 3; roomAsset1(); } else gameOver(); break;
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