package Client;

import Utilities.Card;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

public class Player extends Client implements Runnable {
    private String name;
    private List<Card> cards;
    private Map<String, String> playerInGame; // <Name, Id>
    boolean isRuler;

    public Player(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        try {
            // check player number and prompt
            int countPlayers = (Integer) getInput().readObject();
            if (countPlayers == 404) {
                System.out.println("Not found any game!");
                return;
            }
            System.out.println("You are player number " + countPlayers);

            // send name of the player to server
            sendData("name: " + name);

            // prompt to wait for others
            if (getNumberOfPlayers() != countPlayers) {
                if (getNumberOfPlayers() == 2 || countPlayers == 3)
                    System.out.println("Please wait for other player...");
                else
                    System.out.println("Please wait for other players...");
            }

            while (true) {
                // get server messages
                String serverMessage = (String) getInput().readObject();

                if (serverMessage.equals("players:")) {
                    // save id and name of players in the game
                    for (int i = 0; i < getNumberOfPlayers(); i++) {
                        String message = (String) getInput().readObject();
                        String[] temp = message.split(":");
                        playerInGame.put(temp[0], temp[1]);
                    }
                } else if (serverMessage.equals("cards:")) {
                    getCards();
                } else if (serverMessage.startsWith("ruler:")) {
                    String rulerId = serverMessage.substring(6);
                    if (getId().equals(rulerId)) {
                        System.out.println("You are ruler!");
                        isRuler = true;
                    } else {
                        System.out.println(playerInGame.get(rulerId) + " is ruler.");
                        isRuler = false;
                    }
                } else if (serverMessage.startsWith("set:")) {
                    int numberOfSet = Integer.parseInt(serverMessage.substring(4));
                    System.out.println("Set " + numberOfSet + " has started.");
                }
            }
        } catch (IOException|ClassNotFoundException e) {
            if (!getConnection().isConnected())
                return;
            e.printStackTrace();
        }
    }

    private void getCards() throws IOException, ClassNotFoundException {
        // get cards from server
        cards = (List<Card>) getInput().readObject();

        if (isRuler) {
            // show 5 first cards of the player
            System.out.println("Your first five cards: ");
            for(int i = 0; i < 5; i++) {
                System.out.printf("%d. %s%n", i + 1, cards.get(i));
            }

            // prompt to user to select rule
            Scanner input = new Scanner(System.in);
            System.out.println("please choose rule : ");
            System.out.println("1-->> Hearts");
            System.out.println("2-->> Diamonds");
            System.out.println("3-->> Clubs");
            System.out.println("4-->> Spades");
            System.out.println();
            System.out.print(">>> ");

            // get rule from user
            Card.Suit rule = null;
            for (int i = 0; i < 3; i++) {
                try {
                    int choice = input.nextInt();
                    if (1 <= choice  && choice <= 4) {
                        rule = Card.Suit.values()[choice - 1]; // obtain rule
                        System.out.println("Ok. Hokm is " + rule + "."); // print rule
                        break;
                    } else
                        throw new InputMismatchException();
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input! please enter a number between 1 to 4.");
                } // end of try-catch
            } // end of if

            // if user didn't choose it correctly
            if (rule == null) {
                System.out.println("3 incorrect input. Rule will choose randomly.");
                rule = Card.Suit.values()[new SecureRandom().nextInt(0, 4)];
            }

            sendData("rule:" + rule.name()); // send rule to server
        }
        // print cards
        System.out.println("Cards in your hand:");
        int count = 0;
        for (Card card : cards)
            System.out.printf("%d. %s%n", ++count, card);
    }

    public static void showWinSets() {

    }

    public Card putCard(Card card) {
        if (cards.contains(card)) {
            try {
                getOutput().writeObject("put" + card);
                getOutput().flush();
                String response = (String) getInput().readObject();
                System.out.println("Server responded: " + response);

                cards.remove(card);
                System.out.println("Put Successful");

                return card;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("You have not put a card");
            return null;
        }
    }

    public void showCards() {
        try {
            getOutput().writeObject("showCards");
            getOutput().flush();

            List<Card> response = (List<Card>) getInput().readObject();
            System.out.println("Cards in your hand:");
            for (Card card : response) {
                System.out.println(card);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void recommendCard() {
        // recommand card should handle in client
    }
}