package Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Logger;

public class ServerManager implements Runnable {
    private static final Logger logger = LoggerManager.getLogger();
    private final Server server;
    private final Scanner input;

    ServerManager(Server server) {
        this.server = server;
        this.input = new Scanner(System.in);
    }

    @Override
    public void run() {
//            showMenu();
        JFrame frame = new JFrame("Server manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLayout(new GridLayout(7, 1));
        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayout(1, 3));
        JLabel label1 = new JLabel("Please enter your choice: ");
        JTextField textField = new JTextField(10);
        JButton button = new JButton("Enter");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice;
                choice = Integer.parseInt(textField.getText());
                switch (choice) {
                    case 1:
                        showGames();
                        break;
                    case 2:
                        // determine whether there is a game
                        if (!showGames())
//                            continue;

                            System.out.println("Which game do you want to see the detail of?");
                        System.out.print(">>> ");

                        try {
                            int gameIndex = input.nextInt() - 1;
                            server.showGameDetail(gameIndex);
                        } catch (InputMismatchException | IndexOutOfBoundsException e1) {
                            logger.warning("Invalid choice for game detail!");
                            input.nextLine(); // to clear the buffer
                        }

                        break;
                    case 3:
                        if (!showGames())
//                            continue;

                            System.out.println("Which game do you want to send a message to?");
                        System.out.print(">>> ");

                        try {
                            int gameIndex = input.nextInt() - 1;
                            input.nextLine(); // consume newline
                            if (gameIndex >= 0 && gameIndex < server.getPublicGames().size()) {
                                System.out.print("Enter your message: ");
                                String message = input.nextLine();
                                server.sendMessage(message, gameIndex);
                            } else {
                                logger.warning("Invalid game choice for sending message!");
                            }
                        } catch (InputMismatchException e2) {
                            logger.warning("Invalid choice for sending message!");
                            input.nextLine(); // to clear the buffer
                        }

                        // check the range
                        if (choice > server.getPublicGames().size()) {
                            System.out.println("Your choice is out of range!");
//                            continue;
                        }

                        // get message and send
                        System.out.print("Enter your massage: ");
                        server.sendMessage(input.nextLine(), choice - 1);
                        break;
                    case 4:
                        System.out.print("Enter your message: ");
                        String globalMessage = input.nextLine();
                        server.sendGlobalMessage(globalMessage);
                        logger.info("Sent message to all games: " + globalMessage);
                        break;
                    case 5:
                        System.out.print("Enter your message: ");
                        String privateMessage = input.nextLine();
                        server.sendGlobalMessageToPrivateGames(privateMessage);
                        logger.info("Sent message to all private games: " + privateMessage);
                        break;
                    case 6:
//                            logger.info("Exiting ServerManager...");
//                            return;
                        System.exit(0);
                    default:
                        logger.warning("Invalid menu choice!");
                        System.out.println("Invalid menu choice.");

                }
            }
        });
        panel1.add(label1);
        panel1.add(textField);
        panel1.add(button);
        frame.add(panel1);
        JPanel panel2 = new JPanel();
        JLabel label2 = new JLabel("1. Show all games");
        panel2.add(label2);
        frame.add(panel2);
        JPanel panel3 = new JPanel();
        JLabel label3 = new JLabel("2. Show details of the selected game");
        panel3.add(label3);
        frame.add(panel3);
        JPanel panel4 = new JPanel();
        JLabel label4 = new JLabel("3. Send a message to a specific game");
        panel4.add(label4);
        frame.add(panel4);
        JPanel panel5 = new JPanel();
        JLabel label5 = new JLabel("4. Send a message to all public games");
        panel5.add(label5);
        frame.add(panel5);
        JPanel panel6 = new JPanel();
        JLabel label6 = new JLabel("5. Send a message to all private games");
        panel6.add(label6);
        frame.add(panel6);
        JPanel panel7 = new JPanel();
        JLabel label7 = new JLabel("6. Exit");
        panel7.add(label7);
        frame.add(panel7);
        frame.setVisible(true);


    }

    private void showMenu() {
        System.out.println("1. Show all games");
        System.out.println("2. Show details of the selected game");
        System.out.println("3. Send a message to a specific game");
        System.out.println("4. Send a message to all public games");
        System.out.println("5. Send a message to all private games");
        System.out.println("6. Exit");
        System.out.print("Please enter your choice: ");
        System.out.print(">>> ");
    }

    private boolean showGames() {
        // check if the game is not null
        if (server.getPublicGames().isEmpty()) {
            System.out.println("No game found!");
            logger.info("No games found!");
            return false;
        }

        // show list of the public game
        System.out.println("Public Games:");
        for (int i = 0; i < server.getPublicGames().size(); i++) {
            System.out.println((i + 1) + ". " + server.getPublicGames().get(i));
        }
        // show list of the private game
        System.out.println("Private Games:");
        for (int i = 0; i < server.getPrivateGames().size(); i++) {
            System.out.println((i + 1 + server.getPublicGames().size()) + ". " + server.getPrivateGames().get(i));
        }
        logger.info("Displayed list of games.");
        return true;
    }
}
