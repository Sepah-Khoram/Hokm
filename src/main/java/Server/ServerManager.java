package Server;

import java.util.InputMismatchException;
import java.util.Scanner;

public class ServerManager implements Runnable{
    private final Server server;

    ServerManager(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        // scanner for input
        Scanner inputString = new Scanner(System.in);
        Scanner inputInt = new Scanner(System.in);

        while (true) {
            int choice;
            showMenu();

            try {
                choice = inputInt.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid entery. Please write a number.");
                inputInt.nextLine(); // to ignore shit words
                continue;
            }

            switch (choice) {
                case 1:
                    showGames();
                    break;
                case 2:
                    // determine whether there is a game
                    if (!showGames())
                        continue;

                    System.out.println("Which game do you want to see the detail of?");
                    System.out.print(">>> ");

                    try {
                        server.showGamedetail(inputInt.nextInt() - 1);
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid choice!");
                        inputInt.nextLine();
                        continue;
                    }

                    break;
                case 3:
                    if (!showGames())
                        continue;

                    System.out.println("Which game do you want to send a massage?");
                    System.out.print(">>> ");

                    try {
                        choice = inputInt.nextInt();
                    } catch (InputMismatchException e) {
                        System.out.println("Please enter a number");
                        inputInt.nextLine();
                        continue;
                    }

                    // check the range
                    if (choice > server.getGames().size() || choice <= 0) {
                        System.out.println("Your choice is out of range!");
                        continue;
                    }

                    // get message and send
                    System.out.print("Enter your massage: ");
                    server.massageToGame(inputString.nextLine(), choice - 1);
                    break;
                case 4:
                    // determine whether there is a game
                    if (server.getGames().isEmpty())
                        continue;

                    // get message and send
                    System.out.print("Enter your massage: ");
                    String massage = inputString.nextLine();
                    server.massageToGame(massage);
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private void showMenu() {
        System.out.println("1. Show all games");
        System.out.println("2. Show details of the selected game");
        System.out.println("3. Send a message to the specified game");
        System.out.println("4. Send a massage to all games");
        System.out.println("Please enter your choice :");
        System.out.print(">>> ");
    }

    private boolean showGames() {
        // check if the game is not null
        if (server.getGames().isEmpty()) {
            System.out.println("No game found!");
            return false;
        }

        // show list of the game
        System.out.println("Games:");
        for (int i = 0; i < server.getGames().size(); i++)
            System.out.println((i + 1) + ". " + server.getGames().get(i));
        return true;
    }
}
