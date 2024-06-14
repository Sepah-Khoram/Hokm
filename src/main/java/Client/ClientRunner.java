package Client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class ClientRunner {
    public static final String HOST = "localhost";
    private static final String FILE_NAME = ".Hokm.txt";

    public static void main(String[] args) throws IOException {
        Player player;
        String name = null;
        Scanner input = new Scanner(System.in); // input String
        Scanner inputInt = new Scanner(System.in); // input int

        if (Files.notExists(Paths.get(FILE_NAME))) {
            System.out.println("Welcome to Hokm game!"); // welcome message

            // get the name
            System.out.print("Please enter your name: ");
            name = input.nextLine();

            // write name in the file
            try (Formatter outputFile = new Formatter(FILE_NAME)) {
                outputFile.format("%s", name);
            } catch (FormatterClosedException|SecurityException e) {
                System.err.println("Error writing to file. Terminating.");
                System.exit(1);
            }
        } else {
            try (Scanner inputFile = new Scanner(Paths.get(FILE_NAME))) {
                name = inputFile.nextLine(); // get a name of client
                System.out.println("Welcome " + name + "!"); // print welcome mesage
            } catch (SecurityException e) {
                System.out.println("Error reading from file. Terminating...");
                System.exit(1);
            } catch (NoSuchElementException e) {
                new File(FILE_NAME).delete();
                System.out.println("Again launch application. Terminating...");
                System.exit(1);
            }
        } // end of if-else

        player = new Player(name);

        while (true) {
            showMenu();
            while (true) {
                int choice = 0; // for save choice of the user

                try {
                    choice = inputInt.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Invalid choice. Try agaain.");
                    System.out.print(">>> ");
                }

                switch (choice) {
                    case 1:
                        for (int i = 0; i < 3; i++) {
                            try {
                                System.out.print("Enter number of players: ");
                                player.createGame(HOST, input.nextInt());
                                break;
                            } catch (IllegalArgumentException e) {
                                System.out.println(e.getMessage());
                            } catch (InputMismatchException e) {
                                System.out.println("Please write a number(2 or 4).");
                            }

                            if (i == 2)
                                System.out.println("3 incorrect attempts. Canceling...");
                        }
                        break;
                    case 2:
                        for (int i = 0; i < 3; i++) {
                            try {
                                System.out.print("Enter number of players: ");
                                player.joinGame(HOST, input.nextInt());
                                break;
                            } catch (IllegalArgumentException e) {
                                System.out.println(e.getMessage());
                            } catch (InputMismatchException e) {
                                System.out.println("Please write a number(2 or 4).");
                            }

                            if (i == 2)
                                System.out.println("3 incorrect attempts. Canceling...");
                        }
                        break;
                    case 3:
                        System.out.println("showing game...");
                        // show games
                        break;
                    case 4:
                        System.out.print("Insert new name : ");
                        name = input.nextLine();
                        try (Formatter outputFile = new Formatter(FILE_NAME)){
                            outputFile.format("%s", name);
                            System.out.println("Your name changed successfully.");
                        } catch (FormatterClosedException|SecurityException e) {
                            System.err.println("Error writing to file. Terminating.");
                            System.exit(1);
                        }

                        player = new Player(name); // new player with new name
                        break;
                    case 5:
                        System.out.println("Bye!");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                        System.out.print(">>> ");
                        continue;
                } // end of switch

                break;
            } // end of inner while
        } // end of outer while
    }

    private static void showMenu() {
        System.out.println("Enter your choice:");
        System.out.println("1. Create a new game");
        System.out.println("2. Join to a random game");
        System.out.println("3. Show current games");
        System.out.println("4. rename");
        System.out.println("5. Exit");
        System.out.println();
        System.out.print(">>> ");
    }
}