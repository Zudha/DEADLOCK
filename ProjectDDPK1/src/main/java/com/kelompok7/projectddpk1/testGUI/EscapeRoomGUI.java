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
    int state = 0;

    public EscapeRoomGUI() {
        setTitle("COMDEV: Commit of Development");
        setSize(700, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Setup Display Area
        display = new JTextArea();
        display.setEditable(false);
        display.setBackground(Color.BLACK);
        display.setForeground(new Color(50, 255, 50));
        display.setFont(new Font("Monospaced", Font.PLAIN, 14));
        display.setMargin(new Insets(15, 15, 15, 15));

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

        mazePanel = new MazeGUI(); 

        input.addActionListener(e -> {
            String userInput = input.getText();
            input.setText("");
            handleInput(userInput);
        });

        intro();
        
        setLocationRelativeTo(null);
        setVisible(true);
        SwingUtilities.invokeLater(() -> input.requestFocusInWindow());
    }

    void print(String text) {
        display.append(text + "\n");
        display.setCaretPosition(display.getDocument().getLength());
    }

    void clear() { display.setText(""); }
    
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

    void playMorse(String morse) {
        int unit = 225;
        input.setEnabled(false);
        Timer timer = new Timer(unit, null);
        final int[] i = {0};
        final boolean[] isOn = {false};
        
        timer.addActionListener(e -> {
            if (i[0] >= morse.length()) {
                timer.stop();
                lamp.setBackground(Color.BLACK);
                print("\n[SYSTEM]: Masukkan kode hasil dekripsi:");
                state = 7;
                input.setEnabled(true);
                return;
            }
            char c = morse.charAt(i[0]);
            if (c == ' ') {
                lamp.setBackground(Color.BLACK);
                timer.setDelay(unit * 3);
                i[0]++;
            } else if (!isOn[0]) {
                lamp.setBackground(Color.WHITE);
                timer.setDelay(c == '.' ? unit : unit * 3);
                isOn[0] = true;
            } else {
                lamp.setBackground(Color.BLACK);
                timer.setDelay(unit);
                isOn[0] = false;
                i[0]++;
            }
            lamp.repaint();
        });
        timer.start();
    }

    void showMazeMode() {
        centerPanel.removeAll();
        centerPanel.add(mazePanel, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // --- ALUR CERITA COMDEV ---

    void intro() {
        showTextMode();
        print("=== COMDEV: Commit of Development ===");
        print("Status: Menjalankan hidup.exe...");
        print("\nKamu berdiri di persimpangan jalan.");
        print("Mau langsung kerja cari uang, atau lanjut kuliah?");
        print("Dua-duanya kelihatan benar, tapi mana yang buatmu tenang?");
        print("\nIngat: Setiap pilihan adalah COMMIT. Tak bisa di-Undo.");
        print("\n(Tekan Enter untuk mulai)");
    }

    void room1() {
        clear();
        print("--- ROOM 1: SALAH KODE ---");
        print("Pikiranmu lagi ruwet, kayak kode yang banyak Error-nya.");
        print("Kamu butuh angka 'kunci' supaya sistem ini terbuka:");
        print("\n  Target  : 2026");
        print("  Sekarang: 1000");
        print("\nBerapa angka 'Kunci' yang kurang?");
    }

    void room2() {
        clear();
        print("--- ROOM 2: WAKTU TERBATAS ---");
        print("Kamu ngerasa tertinggal dari teman-temanmu.");
        print("Ada yang sudah sukses, ada yang masih berjuang.");
        print("\nJangan panik. Ikuti polanya:");
        print("01:00 -> 03:30 -> 06:00 -> ...");
        print("\nJam berapa selanjutnya? (Format 0000)");
    }

    void room3() {
        clear();
        print("--- ROOM 3: SUARA BERBISIK ---");
        print("Banyak orang bilang: 'Buat apa sekolah tinggi kalau ujungnya kerja?'");
        print("Tapi hatimu bilang: 'Aku butuh ilmu lebih banyak.'");
        print("\nMana yang kamu pilih?");
        print("1. Menyerah saja.");
        print("2. Terus belajar (Update diri).");
        print("3. Ikut-ikutan orang lain."); 
    }

    void roomMazeIntro() {
        showMazeMode();
        print("--- ROOM 4: JALAN BERLIKU ---");
        print("Dunia luar itu luas dan membingungkan.");
        print("Cari jalan keluarmu di sini sebelum waktumu habis.");
        print("\n(Gunakan W/A/S/D untuk jalan)");
    }

    void room4() {
        showTextMode();
        clear();
        input.setEnabled(false);
        print("--- ROOM 5: JANGAN LUPA ---");
        print("Seringkali kita lupa tujuan awal karena terlalu sibuk.");
        print("Simpan angka ini baik-baik di kepalamu...");
        String kode = "8821";
        print("\nINGAT KODE INI: " + kode);
        Timer t = new Timer(4000, e -> {
            clear();
            print("Masukkan kembali kode memori tadi:");
            state = 6;
            input.setEnabled(true);
        });
        t.setRepeats(false);
        t.start();
    }

    void room5() {
        showLampMode();
        clear();
        print("--- ROOM 6: SINYAL HARAPAN ---");
        print("Di tempat gelap, selalu ada cahaya.");
        print("Perhatikan lampu itu, dia membisikkan sebuah kode.");
        print("Tulis kodenya di sini...");
        playMorse("--... ----. ..--- ....."); // 7925
    }

    void room6() {
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

    void room7() {
        showTextMode();
        clear();
        print("--- ROOM 7: RENCANA MASA DEPAN ---");
        print("Hidupmu yang berantakan harus disusun kembali.");
        print("Urutkan kepingan ini dari yang terkecil.");
        print("Kalau sudah rapi, pintu akan terbuka.");
    }

    void ending() {
        clear();
        print("--- SISTEM BERHASIL DI-UPDATE ---");
        print("Klik. Pintu masa depan terbuka.");
        print("Ternyata sukses itu bukan balapan sama orang lain,");
        print("tapi soal seberapa berani kamu melangkah.");
        print("\nSELAMAT! KAMU BERHASIL COMMIT.");
        input.setEnabled(false);
    }

    void gameOver() {
        clear();
        print("--- SYSTEM CRASH ---");
        print("Kamu gagal melakukan commit. Brain Memory leak berlebihan.");
        print("GAME OVER");
        input.setEnabled(false);
    }

    void handleInput(String inputUser) {
        if (inputUser.trim().isEmpty()) return;
        switch (state) {
            case 0: state = 1; room1(); break;
            case 1: if (inputUser.equals("1026")) { state = 2; room2(); } else gameOver(); break;
            case 2: if (inputUser.equals("0830")) { state = 3; room3(); } else gameOver(); break;
            case 3: if (inputUser.equals("2")) { state = 4; roomMazeIntro(); } else gameOver(); break;
            case 4: 
                mazePanel.movePlayer(inputUser.toUpperCase().charAt(0));
                if (mazePanel.isExitReached()) { state = 5; room4(); }
                break;
            case 6: if (inputUser.equals("8821")) { state = 7; room5(); } else gameOver(); break;
            case 7: if (inputUser.equals("7925")) { state = 8; room6(); } else gameOver(); break;
            case 8: if (inputUser.equals("13206")) { state = 9; ending(); } else gameOver(); break;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EscapeRoomGUI());
    }
}