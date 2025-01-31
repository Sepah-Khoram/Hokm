package Client;

import Utilities.GameType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ClientRunner {
    public static final String HOST = "localhost";
    private static final String FILE_NAME = ".Hokm.txt";
    private static final Scanner inputString = new Scanner(System.in); // input String
    private static final Scanner inputInt = new Scanner(System.in); // input int
    private static Client client;
    private static String nameOfPlayer;

    public static void main(String[] args) {
        setupAppFile(); // setup applecation files
        client = new Client(); // new player

        // show and handle menu
        while (true) {
            showMenu();
            handleMenu();
        }
    }

    private static void showMenu() {
        System.out.println("Please enter your choice:");
        System.out.println("1. Create a new public game");
        System.out.println("2. Create a new private game");
        System.out.println("3. Join a random game");
        System.out.println("4. Join the game via token");
        System.out.println("5. Show current games");
        System.out.println("6. Rename");
        System.out.println("7. Exit");
        System.out.print(">>> ");
    }

    private static void handleMenu() {
        while (true) {
            int choice; // for save choice of the user

            // give a choice of the user
            try {
                choice = inputInt.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Please enter a number. Try agaain.");
                System.out.print(">>> ");
                inputInt.nextLine();
                continue;
            }

            // response to the selected choice
            switch (choice) {
                case 1:
                    createGame(GameType.Public);
                    break;
                case 2:
                    createGame(GameType.Private);
                    break;
                case 3:
                    joinGame();
                    break;
                case 4:
                    Scanner scanner = new Scanner(System.in);
                    System.out.print("Enter the token of the game: ");
                    String gameToken = scanner.nextLine().trim();
                    joinGame(gameToken);
                    break;
                case 5:
                    showCurrentGames();
                    break;
                case 6:
                    rename();
                    break;
                case 7:
                    System.out.println("Bye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
                    System.out.print(">>> ");
                    continue;
            } // end of switch

            break;
        } // end of while
    }

    private static void createGame(GameType type) {
        for (int i = 0; i < 3; i++) {
            try {
                System.out.print("Enter number of players: ");
                if (client.createGame(HOST, inputInt.nextInt(), type)) {
                    Player player = new Player(nameOfPlayer, client, type);
                    player.run();
                }
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (InputMismatchException e) {
                System.out.println("Please write a number(2 or 4).");
                inputInt.nextLine();
            }
            // cancel proccess
            if (i == 2)
                System.out.println("3 incorrect attempts. Canceling...");
        }
    }

    private static void joinGame() {
        for (int i = 0; i < 3; i++) {
            try {
                System.out.print("Enter number of players: ");
                if (client.joinGame(HOST, inputInt.nextInt())) {
                    Player player = new Player(nameOfPlayer, client, GameType.Public);
                    player.run();
                    return;
                }
                return;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            } catch (InputMismatchException e) {
                System.out.println("Please write a number(2 or 4).");
                inputInt.nextLine();
            }
            // cancel proccess
            if (i == 2)
                System.out.println("3 incorrect attempts. Canceling...");
        }
    }

    private static void joinGame(String token) {
        try {
            UUID gameUUID = UUID.fromString(token);
            if (client.joinGame(HOST, gameUUID)) {
                Player player = new Player(nameOfPlayer, client, GameType.Private);
                player.run();
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void showCurrentGames() {
        // get games from server
        ArrayList<Game> currentGames = client.showCurrentGames(HOST);

        if (currentGames == null) {
            System.out.println("No current game in server. You can create a new game!");
            return;
        }

        System.out.printf("row %26s    %22s    %s%n", "token", "players", "connected players");
        int rowCount = 0;
        for (Game game : currentGames) {
            System.out.printf("%3d %s%n", ++rowCount, game);
        }
    }

    private static void rename() {
        // give a number from user
        String oldName = nameOfPlayer; // save old name for cancel
        for (int i = 0; i < 3; i++) {
            System.out.print("Enter your new name(x for cancel): ");
            nameOfPlayer = inputString.nextLine();
            // cancel operation
            if (nameOfPlayer.equals("x")) {
                System.out.println("Canceling...");
                return;
            }
            // verify to name is not empty
            if (!nameOfPlayer.isEmpty())
                break;
        }

        if (nameOfPlayer.isEmpty()) {
            System.out.println("3 incorrect attempt. Rename was canceled!");
            nameOfPlayer = oldName;
            return;
        }

        // write a number in the file
        try (Formatter outputFile = new Formatter(FILE_NAME)) {
            outputFile.format("%s", nameOfPlayer); // write in the file
            hideFile(); // hide the file
            System.out.println("Your name changed successfully."); // prompt
        } catch (FormatterClosedException | SecurityException | FileNotFoundException e) {
            System.err.println("Error writing to file. Terminating.");
            System.exit(1);
        }
    }

    private static void setupAppFile() {
        if (Files.notExists(Paths.get(FILE_NAME))) {
            System.out.println("Welcome to Hokm game!"); // welcome message

            // get the name
            do {
                System.out.print("Please enter your name: ");
                nameOfPlayer = inputString.nextLine();
            } while (nameOfPlayer.isEmpty());

            // write name in the file
            try (Formatter outputFile = new Formatter(FILE_NAME)) {
                outputFile.format("%s", nameOfPlayer);
                hideFile(); // hide file in windows
            } catch (FormatterClosedException | SecurityException | FileNotFoundException e) {
                System.err.println("Error writing to file. Terminating.");
                System.exit(1);
            }
        } else {
            try (Scanner inputFile = new Scanner(Paths.get(FILE_NAME))) {
                nameOfPlayer = inputFile.nextLine(); // get a name of client
                System.out.println("Welcome " + nameOfPlayer + "!"); // print welcome mesage
            } catch (SecurityException | IOException e) {
                System.out.println("Error reading from file. Terminating...");
                System.exit(1);
            } catch (NoSuchElementException e) {
                new File(FILE_NAME).delete();
                System.out.println("Again launch application. Terminating...");
                System.exit(1);
            }
        } // end of if-else
    }

    private static void hideFile() {
        // to hide file in windows
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
                    "attrib +h " + FILE_NAME);
            try {
                Process process = builder.start();
                process.waitFor();
            } catch (IOException | InterruptedException ignored) {
            }
        }
    }
}