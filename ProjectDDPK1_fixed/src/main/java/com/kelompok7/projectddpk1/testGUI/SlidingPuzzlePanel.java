package com.kelompok7.projectddpk1.testGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SlidingPuzzlePanel extends JPanel implements ActionListener {
    private int[][] puzzle = {{4, 1, 3}, {7, 2, 5}, {0, 8, 6}};
    private int[][] target = {{1, 2, 3}, {4, 5, 6}, {7, 8, 0}};
    private JButton[][] tiles = new JButton[3][3];
    private int moves = 0;
    private JLabel movesLabel;
    private EscapeRoomGUI parent;
    private Timer puzzleTimer;

    public SlidingPuzzlePanel(EscapeRoomGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 50));

        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        gridPanel.setOpaque(false);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tiles[i][j] = new JButton(puzzle[i][j] == 0 ? "" : "" + puzzle[i][j]);
                tiles[i][j].setFont(new Font("Arial", Font.BOLD, 24));
                tiles[i][j].addActionListener(this);
                gridPanel.add(tiles[i][j]);
            }
        }

        movesLabel = new JLabel("Moves: 0/12", JLabel.CENTER);
        movesLabel.setForeground(Color.WHITE);
        
        add(gridPanel, BorderLayout.CENTER);
        add(movesLabel, BorderLayout.NORTH);

        puzzleTimer = new Timer(1000, e -> {
            if (moves >= 12 && !isSolved()) {
                puzzleTimer.stop();
                parent.gameOver();
            }
        });
        puzzleTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (e.getSource() == tiles[i][j]) {
                    checkMove(i, j);
                }
            }
        }
    }

    private void checkMove(int r, int c) {
        int r0 = -1, c0 = -1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (puzzle[i][j] == 0) { r0 = i; c0 = j; }
            }
        }

        if (Math.abs(r - r0) + Math.abs(c - c0) == 1) {
            puzzle[r0][c0] = puzzle[r][c];
            puzzle[r][c] = 0;
            moves++;
            movesLabel.setText("Moves: " + moves + "/12");
            updateButtons();
            
            if (isSolved()) {
                puzzleTimer.stop();
                parent.print("\n>>> MEKANISME TERKUNCI! PUZZLE SELESAI.");
    
    
               parent.state = 8; 
    
               parent.input.setEnabled(true); 
               parent.showTextMode();         
               parent.nextFromSlidingPuzzle();                
}
        }
    }

    private void updateButtons() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tiles[i][j].setText(puzzle[i][j] == 0 ? "" : "" + puzzle[i][j]);
            }
        }
    }

    private boolean isSolved() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (puzzle[i][j] != target[i][j]) return false;
            }
        }
        return true;
    }
}