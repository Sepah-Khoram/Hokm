package Server;

import java.util.Scanner;

public class ServerManager implements Runnable {
    private final Server server;

    public ServerManager(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("1.Show all games");
            System.out.println("2.Show details of the selected game");
            System.out.println("insert your Choose :");
            switch (input.nextInt()) {
                case 1:
                    System.out.println("games list :");
                    for (int i = 0; i < server.getGames().size(); i++) {
                        System.out.println(i + 1 + ". " + server.getGames().get(i).toString());
                    }
                    break;
                case 2:
                    System.out.println("wich game do you want to see the detail ? ");
                    server.showGamedetail(server.getGames(), input.nextInt());
                    break;
                default:
            }
        }
    }
}
