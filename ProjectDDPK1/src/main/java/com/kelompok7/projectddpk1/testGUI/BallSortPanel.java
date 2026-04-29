package com.kelompok7.projectddpk1.testGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

public class BallSortPanel extends JPanel {
    private Stack<Color>[] tubes = new Stack[4];
    private Color selectedColor = null;
    private final int MAX_SIZE = 4;
    private EscapeRoomGUI parent;

    private int moves = 0;
    private final int MAX_MOVES = 20;
    private JLabel movesLabel;
    private Timer puzzleTimer;
    
    
    public BallSortPanel(EscapeRoomGUI parent) {
        this.parent = parent;
        this.setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        
        for (int i = 0; i < 4; i++) {
            tubes[i] = new Stack<>();
        }

        // --- SETUP BOLA (3 WARNA, 1 TABUNG KOSONG) ---
        // Tabung 0: Campuran 1
        tubes[0].push(Color.RED); 
        tubes[0].push(Color.BLUE); 
        tubes[0].push(Color.GREEN); 
        tubes[0].push(Color.RED);

        // Tabung 1: Campuran 2
        tubes[1].push(Color.BLUE); 
        tubes[1].push(Color.GREEN); 
        tubes[1].push(Color.RED); 
        tubes[1].push(Color.BLUE);

        // Tabung 2: Campuran 3
        tubes[2].push(Color.GREEN); 
        tubes[2].push(Color.RED); 
        tubes[2].push(Color.BLUE); 
        tubes[2].push(Color.GREEN);
        
        movesLabel = new JLabel("Moves: 0/" + MAX_MOVES, JLabel.CENTER);
        movesLabel.setForeground(Color.WHITE);
        
        add(movesLabel, BorderLayout.NORTH);

        puzzleTimer = new Timer(1000, e -> {
        if (moves >= MAX_MOVES && !checkWinLogic()) {
            puzzleTimer.stop();
            parent.gameOver();
        }
        });
        puzzleTimer.start();

        // Tabung 3: KOSONG (Wajib kosong agar bisa dimainkan)

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int tubeWidth = getWidth() / 4;
                int clickedTube = e.getX() / tubeWidth;
                if (clickedTube >= 0 && clickedTube < 4) {
                    handleTubeClick(clickedTube);
                }
            }
        });
    }
    

    private void handleTubeClick(int index) {
        if (selectedColor == null) {
            // Ambil bola
            if (!tubes[index].isEmpty()) {
                selectedColor = tubes[index].pop();
            }
        } else {
            // Taruh bola
            if (tubes[index].size() < MAX_SIZE) {
                tubes[index].push(selectedColor);
                selectedColor = null;
                
                moves++;
                movesLabel.setText("Moves: " + moves + "/" + MAX_MOVES);
                checkWin();
            }
        }
        repaint();
    }

    private boolean checkWinLogic() {
        int solvedTubes = 0;
        for (Stack<Color> tube : tubes) {
            if (tube.isEmpty()) {
                solvedTubes++; 
            } else if (tube.size() == MAX_SIZE) {
                Color first = tube.get(0);
                boolean allSame = true;
                for (Color c : tube) {
                    if (!c.equals(first)) { allSame = false; break; }
                }
                if (allSame) solvedTubes++;
            }
        }
        return solvedTubes == 4;
    }
    
     private boolean checkWin() {
        // Panggil logika yang sudah ada
        boolean won = checkWinLogic();
    
        if (won) {
            puzzleTimer.stop(); // Bagus kalau distop di sini juga
            JOptionPane.showMessageDialog(this, "Warna Terpola! Sistem Berhasil Diurutkan.");
            parent.nextFromBallSort(); 
        }
    return won;
}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int tubeWidth = getWidth() / 4;
        int yOffset = 200; // Jarak dari atas (Tabung ditaruh lebih bawah)

        for (int i = 0; i < 4; i++) {
            // Gambar Frame Tabung
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(4));
            g2.drawRoundRect(i * tubeWidth + 40, yOffset, 80, 220, 20, 20);
            
            // Gambar Isi Bola
            for (int j = 0; j < tubes[i].size(); j++) {
                g2.setColor(tubes[i].get(j));
                // Jarak antar bola disesuaikan (yOffset + bawah - urutan_bola)
                g2.fillOval(i * tubeWidth + 45, (yOffset + 165) - (j * 52), 70, 50);
                
                // Efek cahaya biar estetik
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillOval(i * tubeWidth + 55, (yOffset + 170) - (j * 52), 15, 10);
            }
        }

        // Tampilan bola yang sedang dibawa (Data Temp)
        if (selectedColor != null) {
            g2.setColor(selectedColor);
            g2.fillOval(getWidth() / 2 - 35, 50, 70, 50);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("WARNA DI TANGAN", getWidth() / 2 - 55, 120);
        }
    }
}