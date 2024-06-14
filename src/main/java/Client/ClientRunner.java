package Client;
import jdk.dynalink.beans.StaticClass;

import java.util.Scanner;

public class ClientRunner {
    private static Player player;
    public static void main(String[] args) {
        player = new Player();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            while (true) {
                showMenu();
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        createGame();
                        break;
                    case 2:
                        joinGame();
                        break;
                    case 3:
                        leaveGame();
                        break;
                    default:
                    System.out.println("Invalid choice");
                }
            }
        }
    }
    private static void showMenu() {
        System.out.println("Welcome to Client");
        System.out.println("1. Create Game");
        System.out.println("2. Join Game");
        System.out.println("3. Leave Game");
    }

    private static void createGame() {
        System.out.println("creating game....");
        player.createGame("localhost", 2);
    }
    private static void joinGame() {
        System.out.println("joining game....");
        player.joinGame("localhost", 2);
    }


private static void leaveGame() {
    System.out.println("leaving game....");
}
}