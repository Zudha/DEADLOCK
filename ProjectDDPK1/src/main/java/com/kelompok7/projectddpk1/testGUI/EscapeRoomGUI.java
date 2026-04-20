package com.kelompok7.projectddpk1.testGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;  // ← 
import java.awt.event.ActionListener; // ← TAMBAH
import javax.swing.Timer;  // ← TAMBAH
import java.awt.event.*;  // ← TAMBAH

public class EscapeRoomGUI extends JFrame {

    JPanel lamp;
    JTextArea display;
    JTextField input;
    JPanel centerPanel;
    MazeGUI mazePanel; 
    int state = 0;

    public EscapeRoomGUI() {
        setTitle("ECHOES OF THE LOCKED MIND");
        setSize(700, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        

        // Setup Display Area (Area Narasi)
        display = new JTextArea();
        display.setEditable(false);
        display.setBackground(Color.BLACK);
        display.setForeground(new Color(0, 255, 0)); // Hijau Matrix/Terminal
        display.setFont(new Font("Monospaced", Font.PLAIN, 14));
        display.setMargin(new Insets(15, 15, 15, 15));

        // Setup Input Field
        input = new JTextField();
        input.setBackground(Color.DARK_GRAY);
        input.setForeground(Color.WHITE);
        input.setCaretColor(Color.WHITE);
        input.setFont(new Font("SansSerif", Font.BOLD, 14));

        centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(display), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        add(input, BorderLayout.SOUTH);
        
        lamp = new JPanel();
        lamp.setBackground(Color.BLACK); // lampu mati

        // Inisialisasi Maze
        mazePanel = new MazeGUI(); 

        // Listener Enter
        input.addActionListener(e -> {
            String userInput = input.getText();
            input.setText("");
            handleInput(userInput);
        });
        
        lamp = new JPanel() {
        protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(getBackground());
        g.fillOval(0, 0, getWidth(), getHeight());
    }
};
        lamp.setPreferredSize(new Dimension(120, 120));
        intro();
        
        setLocationRelativeTo(null);
        setVisible(true);
        SwingUtilities.invokeLater(() -> input.requestFocusInWindow());
    }

    void print(String text) {
        display.append(text + "\n");
        display.setCaretPosition(display.getDocument().getLength());
    }

    void clear() {
        display.setText("");
    }
    
     void showTextMode() {
        centerPanel.removeAll();
        centerPanel.add(new JScrollPane(display), BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
        input.requestFocusInWindow();
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

        //Kode agar Morse Berjalan
    void playMorse(String morse){
        int unit = 225; // durasi
        input.setEnabled(false);
        
        Timer timer = new Timer(unit,null);
        final int[] i = {0};
        final boolean[] isOn = {false};
        final int[] delay = {unit};
        
        
        
    timer.addActionListener(e -> {
        if (i[0] >= morse.length()) {
            timer.stop();
            lampOff();
            print("\nMasukkan kode:");
            state = 7;
            input.setEnabled(true);
            return;
        }

        char c = morse.charAt(i[0]);

        if (c == ' ') {
        lampOff();
        isOn[0] = false; // 🔥 PENTING: paksa mati state
        timer.setDelay(unit * 3); // delay antar karakter
        i[0]++;
        return;
        }

        if (!isOn[0]) {
            lampOn();

            if (c == '.') timer.setDelay(unit);
            else if (c == '-') timer.setDelay(unit * 3);

            isOn[0] = true;

        } else {
            lampOff();
            timer.setDelay(unit);
            isOn[0] = false;
            i[0]++;
        }
    });
        timer.start();
    }
        
         // Menampilkan Labirin
    void showMazeMode() {
        centerPanel.removeAll();
        centerPanel.add(mazePanel, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
        input.requestFocusInWindow();
    }
    
    void lampOn() {
        lamp.setBackground(Color.WHITE);
        lamp.repaint();
    }
    
    void lampOff(){
        lamp.setBackground(Color.BLACK);
        lamp.repaint();
    }

    // --- LOGIKA NARASI SESUAI KODE TEKS ---

    void intro() {
        showTextMode();
        print("=== ECHOES OF THE LOCKED MIND ===\n");
        print("Kamu terbangun di tempat gelap...");
        print("Suara berbisik terdengar...");
        print("\"Kamu tidak seharusnya di sini...\"\n");
        print("(Ketik apapun untuk melanjutkan)");
    }

    void room1() {
        clear();
        print("--- ROOM 1: WHISPER CODE ---");
        print("K A M U\nA M U K\nM U K A\nU K A M");
        print("\n\"Baca seperti aku mengawasi...\"");
        print("\nDi sudut ruangan tertulis:");
        print("\"SEMUA JAWABAN ADALAH KEBALIKAN\"");
        print("Angka di dinding: 9999");
        print("Huruf = posisi alfabet");
        print("\nMasukkan kode:");
    }

    void room2() {
        clear();
        print("--- ROOM 2: TIME IS BROKEN ---");
        print("Benar... tapi sesuatu memperhatikanmu.");
        print("\nJam menunjukkan:");
        print("03:15 -> 06:30 -> 09:45 -> ?");
        print("\n\"Waktu di sini berulang...\"");
        print("Suara berbisik: \"Jangan percaya pola...\"");
        print("\nMasukkan waktu (format 0000):");
    }

    void room3() {
        clear();
        print("--- ROOM 3: VOICES ---");
        print("Jam berdetak lagi...");
        print("\n1. Aku selalu bohong");
        print("2. Dia berkata jujur");
        print("3. Kami semua bohong");
        print("\nPilih jawaban benar (1/2/3):");
    }

    void roomMazeIntro() {
        showMazeMode();
        print("\n--- ROOM MAZE ---");
        print("Kamu melangkah masuk...");
        print("Dinding di sekitarmu terasa... hidup.");
        print("Bisikan muncul: \"Jangan percaya arahmu sendiri...\"");
        print("Gunakan W/A/S/D lalu Enter.");
    }
    
void room4() {
    showTextMode();
    clear();

    input.setEnabled(false); //  disable input dulu

    print("--- ROOM 4: REMEMBER ---");
    print("Kamu memperhatikan sebuah tanda...");
    print("Yang muncul sesaat...\n");

    String kode = "3602";
    print(kode);

    Timer t = new Timer(750, e -> {
        clear();
        print("--- ROOM 4: REMEMBER ---");
        print("Tanda itu menghilang...");
        print("Kamu merasa harus mengingat sesuatu...\n");
        
        print("Masukkan Kode: ");

        state = 6;     // pindah ke state input
        input.setEnabled(true); // input di enabled lagi
    });

    t.setRepeats(false);
    t.start();
}
    void room5() {
        lampOff();
        showLampMode();
        clear();
        print("---ROOM 5: MORSE ---");
        print("Kamu Melihat Lampu berkedip di ruangan yang kosong.... ");
        print("Kamu Mencoba untuk memecahkan apa tanda yang dimaksud?!?");
        
        String morse = "--... ----. ..--- .....";
        
        playMorse(morse);
    }
    
    void room6() {
    showTextMode();
    clear();
    print("--- ROOM 6: SLIDING PUZZLE ---");
    print("Kamu menemukan panel misterius...");
    print("9 kotak bergeser... 1 kosong.");
    print("\"Susun sampai benar... atau mati di sini.\"");
    print("\nKlik kotak untuk memecahkan...");
    
    // Disable input text sementara
    input.setEnabled(false);
    
    // Show puzzle
    centerPanel.removeAll();
    centerPanel.add(new SlidingPuzzlePanel(this), BorderLayout.CENTER);
    centerPanel.revalidate();
    centerPanel.repaint();
}
    
    void room7() {
        showTextMode();
        clear();
        print("--- ROOM 7: FINAL CODE ---");
        print("Kamu melihat sebuah celah... Cahaya... akhirnya.");
        print("\nSuara kembali terdengar:");
        print("\"KAMU... TIDAK PERNAH KELUAR DARI SINI.\"");
        print("\nGabungkan semua angka...");
        print("Hilangkan yang berulang... ambil yang tersisa...");
        print("Urutkan Untuk menemukan jalan keluar.....");
        print("\nMasukkan kode akhir:");
    }
    


    void ending() {
        clear();
        print("--- ENDING ---");
        print("Pintu terbuka...");
        print("Kamu keluar... Tapi...");
        print("Semua orang melihatmu aneh...");
        print("Seolah-olah kamu bukan dirimu sendiri...");
        print("\nYOU ESCAPED...?");
        input.setEnabled(false);
    }

    // Tambah di EscapeRoomGUI
void showPuzzleMode(JPanel puzzlePanel) {
    centerPanel.removeAll();
    centerPanel.add(puzzlePanel, BorderLayout.CENTER);
    centerPanel.revalidate();
    centerPanel.repaint();
}
    
    void gameOver() {
        clear();
        print("\nLampu mati...");
        print("Suara mendekat...");
        print("GAME OVER");
        input.setEnabled(false);
    }

    void handleInput(String inputUser) {
        if (inputUser.trim().isEmpty()) return;

        switch (state) {
            case 0:
                state = 1;
                room1();
                break;
            case 1:
                if (inputUser.equals("1111321")) { state = 2; room2(); } 
                else gameOver();
                break;
            case 2:
                if (inputUser.equals("1300")) { state = 3; room3(); } 
                else gameOver();
                break;
            case 3:
                if (inputUser.equals("2")) { 
                    state = 4; 
                    roomMazeIntro(); 
                } else gameOver();
                break;
            case 4:
                if (inputUser.equalsIgnoreCase("MJ")) {
                    state = 5;
                    room4();
                } else {
                    mazePanel.movePlayer(inputUser.charAt(0));
                    if (mazePanel.isExitReached()) {
                        state = 5;
                        room4();
                    }
                }
                break;
            case 5:
                
                break;
            case 6:
                if (inputUser.equals("3602")) {
                    state = 8;
                    room5();
                }
                else gameOver();
                break;
            case 7:
                if (inputUser.equals("7925")) {
                    state = 9;
                    room6();
                }
                else gameOver();
                break;
            case 8:
                if (inputUser.equals("13206")){
                    state = 9;
                    ending();
                }
                else gameOver();
                break;
        }
        input.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EscapeRoomGUI());
    }
}

