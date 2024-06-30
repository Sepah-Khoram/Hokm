package Server;

import java.util.Scanner;

public class ServerManager implements Runnable{
    public ServerManager(Server server1){
        server=server1;
    }
    Server server = new Server();

    @Override
    public void run() {
        while(true){
        Scanner scanner =new Scanner(System.in);
        System.out.println("1. Show all games \n2. Show details of the selected game \n3. Loging the information" );
        System.out.println("insert your Choose :");
        switch (scanner.nextInt()){
            case 1:
                System.out.println("games list :");
                for (int i=0 ; i < server.getGames().size();i++){
                    System.out.println(String.valueOf(i+1)+ ". "+ server.getGames().get(i).toString());
                }
            case 2:
                System.out.println("wich game do you want to see the detail ? ");
                server.showGamedetail(server.getGames(),scanner.nextInt());

            default:
        }
        }
    }
}
