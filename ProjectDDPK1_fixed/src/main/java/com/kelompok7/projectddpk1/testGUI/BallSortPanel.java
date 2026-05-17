package com.kelompok7.projectddpk1.testGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

public class BallSortPanel extends JPanel {
    private Stack<Color>[] tubes = new Stack[4];
    private Color selectedColor = null;
    private int selectedTube = -1;
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

        tubes[0].push(Color.RED);
        tubes[0].push(Color.BLUE);
        tubes[0].push(Color.GREEN);
        tubes[0].push(Color.RED);

        tubes[1].push(Color.BLUE);
        tubes[1].push(Color.GREEN);
        tubes[1].push(Color.RED);
        tubes[1].push(Color.BLUE);

        tubes[2].push(Color.GREEN);
        tubes[2].push(Color.RED);
        tubes[2].push(Color.BLUE);
        tubes[2].push(Color.GREEN);

        // Tabung 3: KOSONG

        movesLabel = new JLabel("Moves: 0/" + MAX_MOVES, JLabel.CENTER);
        movesLabel.setForeground(Color.WHITE);
        movesLabel.setOpaque(false);
        add(movesLabel, BorderLayout.NORTH);

        puzzleTimer = new Timer(1000, e -> {
            if (moves >= MAX_MOVES && !checkWinLogic()) {
                puzzleTimer.stop();
                parent.gameOver();
            }
        });
        puzzleTimer.start();

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
            if (!tubes[index].isEmpty()) {
                selectedColor = tubes[index].pop();
                selectedTube = index;
            }
        } else {
            if (tubes[index].size() < MAX_SIZE) {
                tubes[index].push(selectedColor);
                selectedColor = null;
                selectedTube = -1;
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
        boolean won = checkWinLogic();
        if (won) {
            puzzleTimer.stop();
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

        int W = getWidth();
        int H = getHeight();

        int numTubes = 4;
        int tubeWidth = W / numTubes;

        int tubeW   = (int)(tubeWidth * 0.55);
        int tubeH   = (int)(H * 0.70);
        int yOffset = (int)(H * 0.20);
        int ballW   = (int)(tubeWidth * 0.50);
        int ballH   = (int)(tubeH / MAX_SIZE) - 4;
        int xPad    = (tubeWidth - tubeW) / 2;

        for (int i = 0; i < 4; i++) {
            int xBase = i * tubeWidth + xPad;

            // Highlight border tabung yang dipilih
            if (i == selectedTube) {
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(xBase - 2, yOffset - 2, tubeW + 4, tubeH + 4, 20, 20);
            }

            // Frame tabung
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(xBase, yOffset, tubeW, tubeH, 20, 20);

            // Isi bola
            for (int j = 0; j < tubes[i].size(); j++) {
                int ballX = xBase + (tubeW - ballW) / 2;
                int ballY = yOffset + tubeH - (j + 1) * (ballH + 4);

                g2.setColor(tubes[i].get(j));
                g2.fillOval(ballX, ballY, ballW, ballH);

                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillOval(ballX + (int)(ballW * 0.15), ballY + (int)(ballH * 0.1),
                            (int)(ballW * 0.25), (int)(ballH * 0.25));
            }
        }

        // Bola melayang di atas tabung yang diklik
        if (selectedColor != null && selectedTube >= 0) {
            int xBase = selectedTube * tubeWidth + xPad;
            int ballX = xBase + (tubeW - ballW) / 2;
            int ballY = yOffset - ballH - 8;

            // Bayangan
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillOval(ballX + 4, ballY + ballH - 4, ballW, 10);

            // Bola
            g2.setColor(selectedColor);
            g2.fillOval(ballX, ballY, ballW, ballH);

            // Efek cahaya
            g2.setColor(new Color(255, 255, 255, 80));
            g2.fillOval(ballX + (int)(ballW * 0.15), ballY + (int)(ballH * 0.1),
                        (int)(ballW * 0.25), (int)(ballH * 0.25));
        }
    }
}