package Server;

import Utilities.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ServerManager implements Runnable{
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
                //
            default:
        }
        }
    }
}
