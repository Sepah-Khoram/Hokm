package Server;

import java.util.Scanner;

public class ServerManager implements Runnable{

    @Override
    public void run() {
        Scanner scanner =new Scanner(System.in);
        System.out.println("1. Show all games \n2. Show details of the selected game \n3. Loging the information" );
        System.out.println("insert your Choose :");
        switch (scanner.nextInt()){
            case 1:
                //
            case 2:
                //
            case 3:
                //
            default:
        }
    }
}
