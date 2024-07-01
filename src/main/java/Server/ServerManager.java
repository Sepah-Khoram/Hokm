package Server;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class ServerManager implements Runnable{
    public ServerManager(Server server) {
        this.server = server;
    }
    Server server = new Server();

    @Override
    public void run() {
        Scanner scanner =new Scanner(System.in);
        int entry;
        while(true){

        System.out.println("1. Show all games \n2. Show details of the selected game \n3. Send a message to the specified game \n4. Send a massage to all games" );
        System.out.println("insert your Choose :");
        try{
            entry = scanner.nextInt();
        }catch (InputMismatchException e){
            System.out.println("wrong entry!");
            continue;
        }

        switch (entry){
            case 1:
                System.out.println("games list :");
                for (int i=0 ; i < server.getGames().size();i++){
                    System.out.println(String.valueOf(i+1)+ ". "+ server.getGames().get(i).toString());
                }
            case 2:
                System.out.println("wich game do you want to see the detail ? ");
                try{
                    server.showGamedetail(server.getGames(),scanner.nextInt());
                }
                catch (InputMismatchException e){
                    System.out.println("wrong entry!");
                    continue;
                }
            case 3:
                System.out.println("wich game do you want to send a massage ? ");
                entry =scanner.nextInt();
                if (entry >= server.getGames().size() ){
                    System.out.println("you insert out of range!");
                    continue;
                }
                System.out.println("insert your massage :");
                server.getGames().get(entry).massageToAll(scanner.nextLine(),entry,server.getGames());
            case 4:
                System.out.println("insert your massage :");
                String massage = scanner.nextLine();
                for (int i=0;i<server.getGames().size();i++){
                    server.getGames().get(i).massageToAll(massage,i,server.getGames());
                }
            default:
        }
        }
    }

}
