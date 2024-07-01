package Client;

import Server.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class ClientRunner {
    public static final String HOST = "localhost";
    private static final String FILE_NAME = ".Hokm.txt";
    private static final Scanner input = new Scanner(System.in); // input String
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
//        JFrame frame = new JFrame("Hokm");
//        JLabel label = new JLabel("Wecome to Hokm game ");
//        JTextField name = new JTextField("");
//        JMenu menu = new JMenu("4 Player Game");
//        JMenu menu1 = new JMenu("2 Player Game");
//        JMenuBar m = new JMenuBar();
//        JMenuItem first = new JMenuItem("Creat New Game");
//        JMenuItem second = new JMenuItem("Join Game");
//        menu.add (first);
//        menu.add(second);
//        menu1.add(first);
//        menu1.add(second);
//        first.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                createGame();
//            }
//        });
//        second.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                joinGame();
//            }
//        });
//        m.add(menu);
//        m.add(menu1);
//        frame.setJMenuBar(m);
//        frame.setLayout(new GridBagLayout());
//        frame.pack();
//        frame.setVisible(true);
    }

    private static void showMenu() {
        System.out.println("Enter your choice:");
        System.out.println("1. Create a new game");
        System.out.println("2. Join to a random game");
        System.out.println("3. Show current games");
        System.out.println("4. rename");
        System.out.println("5. Exit");
        System.out.print(">>> ");
    }

    private static void handleMenu() {
        while (true) {
            int choice; // for save choice of the user

            // give a choice of the user
            try {
                choice = inputInt.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid choice. Try agaain.");
                System.out.print(">>> ");
                continue;
            }

            // response to the selected choice
            switch (choice) {
                case 1:
                    createGame();
                    break;
                case 2:
                    joinGame();
                    break;
                case 3:
                    showCurrentGames();
                    break;
                case 4:
                    rename();
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
        } // end of while
    }

    private static void createGame() {
        for (int i = 0; i < 3; i++) {
            try {
                System.out.print("Enter number of players: ");
                if (client.createGame(HOST, input.nextInt())) {
                    Player player = new Player(nameOfPlayer, client);
                    player.run();
                }
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (InputMismatchException e) {
                System.out.println("Please write a number(2 or 4).");
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
                if (client.joinGame(HOST, input.nextInt())) {
                    Player player = new Player(nameOfPlayer, client);
                    player.run();
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (InputMismatchException e) {
                System.out.println("Please write a number(2 or 4).");
            }
            // cancel proccess
            if (i == 2)
                System.out.println("3 incorrect attempts. Canceling...");
        }
    }

    private static void showCurrentGames() {
        // get games from server
        ArrayList<Game> currentGames = client.showCurrentGames(HOST);

        if (currentGames == null) {
            System.out.println("No current game in server. You can create a new game!");
            return;
        }

        // for count games
        int count = 0;

        // prompt and show games
        System.out.println("Choose one of the game to join(enter 0 for cancel proccess):");
        for (Game game : currentGames) {
            System.out.printf("%d. %s", ++count, game);
        }

        // join to the game that user select
        for (int i = 0; i < 3; i++) {
            try {
                // give the selected game of the user
                int gameNumber = inputInt.nextInt() - 1;
                Game selectGame = currentGames.get(gameNumber);

                // add number of player and join to the game
                client.setNumberOfPlayer(selectGame.getNumberOfPlayers());
                if (client.joinGame(HOST, currentGames.get(gameNumber).getToken())) {
                    Player player = new Player(nameOfPlayer, client);
                    player.run();
                }
            } catch (InputMismatchException e) {
                System.out.println("Please write a number.");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Please write a correct number in the range.");
            }
            // cancel proccess
            if (i == 2) {
                System.out.println("3 incorrect attempts. Canceling...");
            }
        }
    }

    private static void rename() {
        // give a number from user
        String oldName = nameOfPlayer; // save old name for cancel
        for (int i = 0; i < 3; i++) {
            System.out.print("Insert new name : ");
            nameOfPlayer = input.nextLine();
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
                nameOfPlayer = input.nextLine();
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