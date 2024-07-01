package Client;

import Utilities.Card;
import Utilities.GameService;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

public class Player extends Client implements Runnable {
    private final String name;
    private final Map<String, String> playerInGame; // <Id, Name>
    private final ArrayList<Card> onTableCards = new ArrayList<>();
    private boolean isRuler;
    private Card.Suit rule;
    private ArrayList<Card> cards;

    public Player(String name, Client client) {
        super(client);
        this.name = name;
        playerInGame = new HashMap<>();
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
            sendData("name:" + name);

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

                if (serverMessage.equals("players")) {
                    showPlayers();
                } else if (serverMessage.equals("cards")) {
                    getCards();
                } else if (serverMessage.startsWith("ruler")) {
                    String rulerId = serverMessage.substring(6);
                    if (getId().equals(rulerId)) {
                        System.out.println("You are ruler!");
                        isRuler = true;
                    } else {
                        System.out.println(playerInGame.get(rulerId).trim() + " is ruler.");
                        isRuler = false;
                    }
                } else if (serverMessage.startsWith("set")) {
                    int numberOfSet = Integer.parseInt(serverMessage.substring(4));
                    System.out.println("Set " + numberOfSet + " has started.");
                } else if (serverMessage.startsWith("team")) {
                    String[] team = serverMessage.substring(6).split(",");
                    if (team[1].equals(getId()) || team[2].equals(getId())) {
                        System.out.println("Your team: " + playerInGame.get(team[1]) +
                                ", " + playerInGame.get(team[2]));
                    } else {
                        System.out.println("Other team: " + playerInGame.get(team[1]) +
                                ", " + playerInGame.get(team[2]));
                    }
                } else if (serverMessage.startsWith("rule")) {
                    if (!isRuler) {
                        rule = Card.Suit.valueOf(serverMessage.substring(5));
                        System.out.println("Rule is : " + rule);
                    }
                } else if (serverMessage.startsWith("round")) {
                    int round = Integer.parseInt(serverMessage.substring(6));
                    System.out.println("Round " + round + " is started.");
                    onTableCards.clear();
                } else if (serverMessage.startsWith("on table card")) {
                    // get player's id
                    String playerId = serverMessage.substring(14);
                    // get card
                    onTableCards.add((Card) getInput().readObject());
                    if (!playerId.equals(getId())) {
                        System.out.println(playerInGame.get(playerId) + " played " +
                                onTableCards.getLast());
                    }
                } else if (serverMessage.startsWith("turn")) {
                    showCards();
                    System.out.println("Your turn!");
                    System.out.println("Choose one of them(suggested card is " +
                            GameService.suggestedCard(onTableCards, cards, rule) + "):");
                    System.out.println(">>> ");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!getConnection().isConnected())
                return;
            e.printStackTrace();
        }
    }

    private void getCards() throws IOException, ClassNotFoundException {
        // get cards from server
        cards = (ArrayList<Card>) getInput().readObject();

        if (isRuler) {
            // show 5 first cards of the player
            System.out.println("Your first five cards: ");
            showCards();
            getRule();

            // get complete cards from server
            cards = (ArrayList<Card>) getInput().readObject();
        }

        // print cards
        showCards();
        if (!isRuler)
            System.out.println("Wait for ruler to select the rule.");
    }

    private void getRule() {
        // prompt to user to select rule
        Scanner input = new Scanner(System.in);
        System.out.println("Please choose rule : ");
        System.out.println("1-->> Hearts");
        System.out.println("2-->> Diamonds");
        System.out.println("3-->> Clubs");
        System.out.println("4-->> Spades");
        System.out.println();
        System.out.print(">>> ");

        // get rule from user
        for (int i = 0; i < 3; i++) {
            try {
                int choice = input.nextInt();
                if (1 <= choice && choice <= 4) {
                    rule = Card.Suit.values()[choice - 1]; // obtain rule
                    System.out.println("Ok. Rule is " + rule + "."); // print rule
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

    private void showCards() {
        System.out.println("Cards in your hand:");
        int count = 0;
        for (Card card : cards)
            System.out.printf("%2d. %s%n", ++count, card);
    }

    private void showPlayers() throws IOException, ClassNotFoundException {
        System.out.println("Player in games: ");
        // save id and name of players in the game
        for (int i = 0; i < getNumberOfPlayers(); i++) {
            String message = (String) getInput().readObject();
            String[] temp = message.split(":");
            playerInGame.put(temp[0], temp[1]);

            // print in the output
            System.out.print((i + 1) + "." + temp[1] + " ");
            if (temp[0].equals(getId()))
                System.out.print("(You)");
            System.out.println();
        }
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
}