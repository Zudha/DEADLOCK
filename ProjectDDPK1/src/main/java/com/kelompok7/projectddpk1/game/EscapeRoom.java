package com.kelompok7.projectddpk1.game;

import java.util.Scanner;

public class EscapeRoom {

    static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        
        intro();
        room1();
        room2();
        room3();
        roommaze();
        room4();
        room5();
        ending();
    }

    static void intro() {
        System.out.println("=== ECHOES OF THE LOCKED MIND ===\n");
        System.out.println("Kamu terbangun di tempat gelap...");
        System.out.println("Suara berbisik terdengar...");
        System.out.println("\"Kamu tidak seharusnya di sini...\"\n");
    }

    static void room1() {
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

    static void room2() {
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

    static void room3() {
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
    
    static void roommaze() {
        
        System.out.println("\n--- ROOM MAZE ---\n");

        System.out.println("Kamu melangkah masuk...");
        System.out.println("Dinding di sekitarmu terasa... hidup.");
        System.out.println("Setiap langkahmu bergema, tapi...");
        System.out.println("...suara itu bukan cuma milikmu.\n");

        System.out.println("Sebuah bisikan muncul:");
        System.out.println("\"Jangan percaya arahmu sendiri...\"\n");
         // masukin maze ke game
        Maze m = new Maze();
        while (!m.isExitReached()) {
            m.displayMaze();
            System.out.print("Keybind W/A/S/D: ");
            String inputUser = input.nextLine();
            //shortcut maze wkwk
            if (inputUser.equalsIgnoreCase("MJ")) {
                System.out.println("Shortcut cuk");
                break;
            }
            
            if (inputUser.length() > 0) { // pemain cuman bisa input W A S D
                char move = inputUser.charAt(0);
                m.movePlayer(move);
            }
    }
        System.out.println("\nKamu melihat sebuah celah...");
        System.out.println("Cahaya... akhirnya.\n");

        System.out.println("Saat kamu melangkah keluar...");
        System.out.println("Suara itu kembali terdengar:");

        System.out.println("\n\"KAMU...\"");
        System.out.println("\"TIDAK PERNAH KELUAR DARI SINI.\"");
}
    static void room4() {
        System.out.println("\n--- ROOM 4: REMEMBER---");
        
        System.out.println("Kamu Memperhatikan sebuah tanda.....");
        System.out.println("Yang muncul sesaat...");
        
        String Reminder = "3602";
        try {
            for (int i = 0; i < 2; i++){
                System.out.println(Reminder);
                Thread.sleep(500);
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
    
    static void room5() {
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
    
    

    static void ending() {
        System.out.println("\nKamu keluar...");
        System.out.println("Tapi...");
        System.out.println("Semua orang melihatmu aneh...");
        System.out.println("Seolah-olah kamu bukan dirimu sendiri...");
        System.out.println("\nYOU ESCAPED...?");
    }

    static void gameOver() {
        System.out.println("\nLampu mati...");
        System.out.println("Suara mendekat...");
        System.out.println("GAME OVER");
        System.exit(0);
    }
}