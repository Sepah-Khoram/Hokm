package Client;

import Utilities.Card;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Player extends Client implements Runnable {
    private String name;
    private List<Card> cards;
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
            if (getNumberOfPlayers() != countPlayers)
                System.out.println("Please wait for other players...");

            while (true) {
                // get server messages
                String serverMessage = (String) getInput().readObject();

                if (serverMessage.equals("cards:")) {
                    // get cards from server
                    cards = (List<Card>) getInput().readObject();

                    if (isRuler) {
                        for(int i = 0; i<5 && i<cards.size(); i++){
                            System.out.printf("%d. %s%n", i + 1, cards.get(i));
                        }
                        Scanner scanner = new Scanner(System.in);
                        System.out.println("Ple>ase choose your hokm : ");
                        System.out.println("1-->> Hearts");
                        System.out.println("2-->> Diamonds");
                        System.out.println("3-->> Clubs");
                        System.out.println("4-->> Spades");
                        String hokmChoice = scanner.nextLine();
                        int choice = scanner.nextInt();
                        switch (choice) {
                            case 1:
                                hokmChoice = "Hearts";
                                break;
                            case 2:
                                hokmChoice = "Diamonds";
                                break;
                            case 3:
                                hokmChoice = "Clubs";
                            case 4:
                                hokmChoice = "Spades";
                                break;
                            default:
                                System.out.println("Invalid choice!");
                                 hokmChoice = cards.get(2).getSuit().name();
                                try {
                                    getOutput().writeObject("Hokm: " + hokmChoice);
                                    getOutput().flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                    // print cards
                    System.out.println("Cards in your hand:");
                    int count = 0;
                    for (Card card : cards)
                        System.out.printf("%d. %s%n", ++count, card);
                } else if (serverMessage.equals("dealer")) {
                    isRuler = true;
                    System.out.println("You are dealer!");
                }
            }
        } catch (IOException|ClassNotFoundException e) {
            if (!getConnection().isConnected())
                return;
            e.printStackTrace();
        }
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