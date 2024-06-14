package Client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ClientRunner {
    public static final String HOST = "localhost";

    public static void main(String[] args) throws IOException {
        Player player = new Player();
        Scanner input = new Scanner(System.in);
        File name = new File("name.txt");
        name.createNewFile();
        Scanner scanner = new Scanner(name);
        if (scanner.hasNextLine()){
            /////////////////////////////////////////////////////////////send name to server
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
                            System.out.println("showing game...");


                        case 4:
                            System.out.println("insert your name : ");
                            String name1 = input.nextLine();
                            FileWriter writer = new FileWriter("name.txt");
                            writer.write(name1);
                            writer.close();
                        case 5:
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
        else{
            try {
                System.out.println("insert your name : ");
                String name1 = input.nextLine();
                FileWriter writer = new FileWriter("name.txt");
                writer.write(name1);
                writer.close();
            }
                catch (Exception e) {

            }
        }
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
                        System.out.println("showing game...");


                    case 4:
                        System.out.println("insert your name : ");
                        String name1 = input.nextLine();
                        FileWriter writer = new FileWriter("name.txt");
                        writer.write(name1);
                        writer.close();
                    case 5:
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
        System.out.println("3. Show Games");
        System.out.println("4. Change Name");
        System.out.println("5. Exit");
        System.out.println();
        System.out.print(">>> ");
    }
}