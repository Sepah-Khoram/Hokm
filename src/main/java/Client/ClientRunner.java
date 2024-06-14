package Client;

import java.util.InputMismatchException;
import java.util.Scanner;

public class ClientRunner {
    public static final String HOST = "localhost";

    public static void main(String[] args) {
        Player player = new Player();
        Scanner input = new Scanner(System.in);

        while (true) {
            showMenu();

            while (true) {
                int choice = 0; // for save choice of the user

                try {
                    choice = input.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Invalid choice. Try agaain.");
                    System.out.print(">>> ");
                }

                switch (choice) {
                    case 1:
                        System.out.println("creating game...");
                        player.createGame(HOST, 2);
                        break;
                    case 2:
                        System.out.println("joining game....");
                        player.joinGame(HOST, 2);
                        break;
                    case 3:
                        System.out.println("Bye!");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                        System.out.print(">>> ");
                        continue;
                }

                break;
            }
        }
    }

    private static void showMenu() {
        System.out.println("Welcome to Hokm game!");
        System.out.println("Enter your choice:");
        System.out.println("1. Create a Game");
        System.out.println("2. Join to a Game");
        System.out.println("3. Exit");
        System.out.println();
        System.out.print(">>> ");
    }
}