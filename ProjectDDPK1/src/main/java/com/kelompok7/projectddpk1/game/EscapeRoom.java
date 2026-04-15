package com.kelompok7.projectddpk1.game;

import java.util.Scanner;

public class EscapeRoom {

    static Scanner input = new Scanner(System.in);
    static PuzzleRooms puzzles;

    public static void main(String[] args) {
        puzzles = new PuzzleRooms(input);
         System.out.print("Masukkan 'DEBUG' untuk mode test (kosongkan untuk normal): ");
        String debugInput = input.nextLine().trim();
        
        if (debugInput.equalsIgnoreCase("DEBUG")) {
            debugMode();
        } else {
            normalMode();
        }
    }
        
    static void normalMode(){
        intro();
        room1();
        room2();
        room3();
        roommaze();
        room4();
        room5();
        ending();
    }
    
    static void debugMode() {
        System.out.println("🔧 DEBUG MODE AKTIF! 🔧");
        System.out.println("Ketik nama room (room1/room2/room3/roommaze/room4/room5/ending/exit):");
        
        while (true) {
            System.out.print("\n> ");
            String command = input.nextLine().toLowerCase().trim();
            
            switch (command) {
                case "room1":
                    room1();
                    break;
                case "room2":
                    room2();
                    break;
                case "room3":
                    room3();
                    break;
                case "roommaze":
                case "maze":
                    roommaze();
                    break;
                case "room4":
                    room4();
                    break;
                case "room5":
                    room5();
                    break;
                case "ending":
                case "end":
                    ending();
                    break;
                case "intro":
                    intro();
                    break;
                case "exit":
                case "quit":
                    System.out.println("Keluar dari DEBUG MODE. Bye! 👋");
                    return;
                default:
                    System.out.println("Room ga ada! Ketik: room1/room2/room3/roommaze/room4/room5/ending/exit");
            }
        }
    }

    static void intro() {
        System.out.println("=== ECHOES OF THE LOCKED MIND ===\n");
        System.out.println("Kamu terbangun di tempat gelap...");
        System.out.println("Suara berbisik terdengar...");
        System.out.println("\"Kamu tidak seharusnya di sini...\"\n");
    }
    
    static void room1() {
        puzzles.room1();
    }
    
    static void room2() {
        puzzles.room2();
    }
    
    static void room3() {
        puzzles.room3();
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
        puzzles.room4();
    }
    
    static void room5() {
        puzzles.room5();
    }
    
    static void ending() {
        System.out.println("\nKamu keluar...");
        System.out.println("Tapi...");
        System.out.println("Semua orang melihatmu aneh...");
        System.out.println("Seolah-olah kamu bukan dirimu sendiri...");
        System.out.println("\nYOU ESCAPED...?");
    }
}