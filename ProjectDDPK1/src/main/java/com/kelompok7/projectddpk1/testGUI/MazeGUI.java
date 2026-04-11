package com.kelompok7.projectddpk1.testGUI;

import javax.swing.*;
import java.awt.*;

public class MazeGUI extends JPanel {

    private char[][] grid;
    private int playerX;
    private int playerY;

    public MazeGUI() {
        grid = new char[][] {
            {'|','|','|','|','|','|','|','|','|','|','|','|','|','|','|'},
            {'|',' ',' ',' ','|',' ',' ',' ',' ',' ','|',' ',' ',' ','|'},
            {'|',' ','|',' ','|',' ','|','|','|',' ','|',' ','|',' ','|'},
            {'|',' ','|',' ',' ',' ','|',' ','|',' ',' ',' ','|',' ','|'},
            {'|',' ','|','|','|',' ','|',' ','|','|','|',' ','|',' ','|'},
            {'|',' ',' ',' ','|',' ',' ',' ',' ',' ','|',' ','|',' ','|'},
            {'|','|','|',' ','|','|','|','|','|',' ','|',' ','|',' ','|'},
            {'|',' ',' ',' ',' ',' ',' ',' ','|',' ','|',' ',' ',' ','|'},
            {'|',' ','|','|','|','|','|',' ','|',' ','|','|','|',' ','|'},
            {'|',' ','|',' ',' ',' ','|',' ',' ',' ',' ',' ','|',' ','|'},
            {'|',' ','|',' ','|',' ','|','|','|','|','|',' ','|',' ','|'},
            {'|',' ',' ',' ','|',' ',' ',' ',' ',' ','|',' ',' ',' ','|'},
            {'|','|','|','|','|','|','|','|','|',' ','|','|','|','E','|'},
            {'|',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ','|'},
            {'|','|','|','|','|','|','|','|','|','|','|','|','|','|','|'}
        };

        playerX = 1;
        playerY = 1;

        setPreferredSize(new Dimension(400, 400));
        setBackground(Color.DARK_GRAY);
    }

    public void movePlayer(char direction) {
        int newX = playerX;
        int newY = playerY;

        char d = Character.toUpperCase(direction);
        if (d == 'W') newY--;
        else if (d == 'S') newY++;
        else if (d == 'A') newX--;
        else if (d == 'D') newX++;

        if (newY >= 0 && newY < grid.length &&
            newX >= 0 && newX < grid[0].length &&
            grid[newY][newX] != '|') {
            playerX = newX;
            playerY = newY;
        }
        repaint();
    }

    public boolean isExitReached() {
        return grid[playerY][playerX] == 'E';
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int tileSize = 25;

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x] == '|') {
                    g.setColor(Color.BLACK);
                } else if (grid[y][x] == 'E') {
                    g.setColor(Color.RED);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }

        // Render Player
        g.setColor(Color.BLUE);
        g.fillOval(playerX * tileSize + 2, playerY * tileSize + 2, tileSize - 4, tileSize - 4);
    }
}