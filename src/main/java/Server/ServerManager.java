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
        Scanner input = new Scanner(System.in);

        while (true) {
            int choice;
            showMenu();

            try {
                choice = input.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid entery. Please write a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    showGames();
                    break;
                case 2:
                    showGames();
                    System.out.println("Which game do you want to see the detail of?");
                    System.out.println(">>> ");

                    try {
                        server.showGamedetail(input.nextInt() - 1);
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid choice!");
                        continue;
                    }

                    break;
                case 3:
                    System.out.println("wich game do you want to send a massage ? ");
                    choice = input.nextInt();
                    if (choice >= server.getGames().size() ){
                        System.out.println("you insert out of range!");
                        continue;
                    }
                    System.out.println("insert your massage :");
                    server.getGames().get(choice).massageToAll(input.nextLine(),choice,server.getGames());
                case 4:
                    System.out.println("insert your massage :");
                    String massage = input.nextLine();
                    for (int i=0;i<server.getGames().size();i++){
                        server.getGames().get(i).massageToAll(massage,i,server.getGames());
                    }
                default:
            }
        }
    }

    private void showMenu() {
        System.out.println("1. Show all games");
        System.out.println("2. Show details of the selected game");
        System.out.println("3. Send a message to the specified game");
        System.out.println("4. Send a massage to all games");
        System.out.println("Please enter your Choice :");
        System.out.print(">>> ");
    }

    private void showGames() {
        // check if the game is not null
        if (server.getGames() == null) {
            System.out.println("No game found!");
            return;
        }

        // show list of the game
        System.out.println("Games:");
        for (int i = 1; i <= server.getGames().size(); i++)
            System.out.println(i + ". " + server.getGames().get(i));
    }
}
