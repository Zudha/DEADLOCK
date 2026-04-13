
package com.kelompok7.projectddpk1.game;

/**
 *
 * @author valdis
 */

// Labirin request Fajar
public class Maze {
    private char[][] grid;
    private int playerX;
    private int playerY;
    
   public Maze(){
       grid = new char[][] {
    {'-','-','-','-','-','-','-','-','-','-','-','-','-','-','-'},
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
    {'-','-','-','-','-','-','-','-','-','-','-','-','-','-','-'}
};
    playerX = 1;
    playerY = 1;
   }
   
   public void displayMaze() { // ini buat nampilin mazenya
       for (int i = 0; i < grid.length; i++){
           for (int j = 0; j < grid[i].length; j++){
               if (i == playerY && j == playerX) { // ngecek posisi player
                   System.out.print("P");
               }
               else {
                   System.out.print(grid[i][j]);
               }
           }
                  System.out.println();
       }
   }
   
   // Ngegerakin Player
   public void movePlayer(char direction){
       int newX = playerX;
       int newY = playerY;
       
       if (direction == 'W' || direction == 'w'){
           newY--;
       }
       else if (direction == 'S' || direction == 's'){
           newY++;
       }
       else if (direction == 'D' || direction == 'd'){
           newX++;
       }
       else if (direction == 'A' || direction == 'a'){
           newX--;
       }
       if (grid[newY][newX] != '|') { // mencegah player nembus tembok
           playerX = newX;
           playerY = newY;
       }
   }
   
   public boolean isExitReached() {
       return grid[playerY][playerX] == 'E';
   }
}

