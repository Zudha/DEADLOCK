package com.kelompok7.projectddpk1.game;

import java.util.Scanner;

public class PuzzleRooms {
    private Scanner input;

    public PuzzleRooms(Scanner input) {
        this.input = input;
    }

    public void room1() {
        System.out.println("\n--- ROOM 1: WHISPER CODE ---");

        System.out.println("K A M U");
        System.out.println("A M U K");
        System.out.println("M U K A");
        System.out.println("U K A M");

        System.out.println("\n\"Baca seperti aku mengawasi...\"");

        // Fake clue
        System.out.println("\nDi sudut ruangan tertulis:");
        System.out.println("\"SEMUA JAWABAN ADALAH KEBALIKAN\"");
        System.out.println("Angka di dinding: 9999");
        System.out.println("Huruf = posisi alfabet");
        
        System.out.print("\nMasukkan kode: ");
        String jawab = input.nextLine();

        if (jawab.equals("1111321")) {
            System.out.println("Benar... tapi sesuatu memperhatikanmu.");
        } else {
            gameOver();
        }
    }
    
    public void room2() {
        System.out.println("\n--- ROOM 2: TIME IS BROKEN ---");

        System.out.println("Jam menunjukkan:");
        System.out.println("03:15");
        System.out.println("06:30");
        System.out.println("09:45");
        System.out.println("?");

        System.out.println("\"Waktu di sini berulang...\"");

        // Fake clue
        System.out.println("Suara berbisik:");
        System.out.println("\"Jangan percaya pola...\"");

        System.out.print("\nMasukkan waktu: ");
        String jawab = input.nextLine();

        if (jawab.equals("1300")) {
            System.out.println("Jam berdetak lagi...");
        } else {
            gameOver();
        }
    }

    public void room3() {
        System.out.println("\n--- ROOM 3: VOICES ---");

        System.out.println("1. Aku selalu bohong");
        System.out.println("2. Dia berkata jujur");
        System.out.println("3. Kami semua bohong");

        System.out.print("\nPilih jawaban benar (1/2/3): ");
        String jawab = input.nextLine();

        if (jawab.equals("2")) {
            System.out.println("Salah satu suara menirukanmu...");
        } else {
            gameOver();
        }
    }

    public void room4() {
        System.out.println("\n--- ROOM 4: REMEMBER---");
        
        System.out.println("Kamu Memperhatikan sebuah tanda.....");
        System.out.println("Yang muncul sesaat...");
        
        String Reminder = "3602";
        try {
            for (int i = 0; i < 1; i++){
                System.out.print(Reminder);
                Thread.sleep(1500);
                
                System.out.print("\b\b\b\b"); // ini untuk menghapus output yang di cetak
            }
            } catch (InterruptedException e) {
                    e.printStackTrace();
        }
        System.out.print("\nMasukkan kode akhir: ");
        
        String jawab = input.nextLine();
         
        if (jawab.equals("3602")) {
            System.out.println("Pintu terbuka...");
        } else {
            gameOver();
        }
        
    }

    public void room5() {
        System.out.println("\n--- ROOM 5: FINAL CODE ---");

        System.out.println("Gabungkan semua angka...");
        System.out.println("Buang yang sama... ambil yang tersisa...");

        System.out.print("\nMasukkan kode akhir: ");
        String jawab = input.nextLine();

        if (jawab.equals("13206")) {
            System.out.println("Pintu terbuka...");
        } else {
            gameOver();
        }
    }
    
     public void room6() {
        System.out.println("\n--- ROOM 6: SLIDING PUZZLE ---");
        System.out.println("Geser kotak sampai jadi urutan 1-8 (0 = kosong)");
        
        // Target: 1 2 3
        //          4 5 6  
        //          7 8 0
        int[][] target = {{1,2,3},{4,5,6},{7,8,0}};
        int[][] puzzle = {{1,2,3},{4,0,6},{7,5,8}}; // Puzzle awal
        
        printPuzzle(puzzle);
        
        int moves = 0;
        while (moves < 10) { // Max 10 moves
            System.out.print("Gerakkan 0 ke (atas/bawah/kiri/kanan): ");
            String direction = input.nextLine().toLowerCase();
            
            if (movePuzzle(puzzle, direction)) {
                moves++;
                System.out.println("Move " + moves + "/10");
                printPuzzle(puzzle);
                
                if (isSolved(puzzle, target)) {
                    System.out.println("PUZZLE TERPECAHKAN!");
                    return;
                }
            } else {
                System.out.println("Gerakan tidak valid!");
            }
        }
        gameOver();
    }

    private void printPuzzle(int[][] puzzle) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.printf("%2d ", puzzle[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    private boolean movePuzzle(int[][] puzzle, String direction) {
        int[] zeroPos = findZero(puzzle);
        
        int newX = zeroPos[0], newY = zeroPos[1];
        
        if (direction.equals("atas") && zeroPos[0] > 0) newX--;
        else if (direction.equals("bawah") && zeroPos[0] < 2) newX++;
        else if (direction.equals("kiri") && zeroPos[1] > 0) newY--;
        else if (direction.equals("kanan") && zeroPos[1] < 2) newY++;
        else return false;
        
        // Swap
        swap(puzzle, zeroPos[0], zeroPos[1], newX, newY);
        return true;
    }

    private int[] findZero(int[][] puzzle) {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (puzzle[i][j] == 0) return new int[]{i,j};
        return new int[]{-1,-1};
    }

    private void swap(int[][] puzzle, int x1, int y1, int x2, int y2) {
        int temp = puzzle[x1][y1];
        puzzle[x1][y1] = puzzle[x2][y2];
        puzzle[x2][y2] = temp;
    }

    private boolean isSolved(int[][] puzzle, int[][] target) {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (puzzle[i][j] != target[i][j]) return false;
        return true;
    }

    private void gameOver() {
        System.out.println("\nLampu mati...");
        System.out.println("Suara mendekat...");
        System.out.println("GAME OVER");
        System.exit(0);
    }
}

    