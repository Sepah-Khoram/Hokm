package Client;

import Utilities.Card;
import Utilities.GameService;
import Utilities.GameType;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

public class Player extends Client implements Runnable {
    private final String name;
    private final Map<String, String> playersInGame; // <Id, Name>
    private final Map<String, Integer> scoresOfPlayers; // <Id, Score>
    private final String[] opponentId;
    private final int[] scoresOfTeams;
    private final ArrayList<String> players;
    private final ArrayList<Card> onTableCards = new ArrayList<>();
    private final GameType gameType;
    private boolean isRuler;
    private Card.Suit rule;
    private ArrayList<Card> cards;
    private int playerNumber;
    private String teammateId;
    private Card teammateCard;

    public Player(String name, Client client, GameType gameType) {
        super(client);
        this.gameType = gameType;
        this.name = name;
        playersInGame = new HashMap<>();
        scoresOfPlayers = new HashMap<>();
        players = new ArrayList<>();
        opponentId = new String[getNumberOfPlayers() / 2];
        scoresOfTeams = new int[getNumberOfPlayers() / 2];
        Arrays.fill(scoresOfTeams, 0);
    }

    @Override
    public void run() {
        try {
            // check player number and prompt
            playerNumber = getInput().readInt();
            if (playerNumber == 404) {
                System.out.println("Not found any game!");
                return;
            }
            System.out.println("You are player number " + playerNumber);

            // send name of the player to server
            sendData("name:" + name);

            // get token of private game
            if (gameType == GameType.Private) {
                try {
                    System.out.println("Token of the game: " + getInput().readObject());
                    setNumberOfPlayers(getInput().readInt());
                } catch (IOException e) {
                    System.out.println("Problem in loading token.");
                    return;
                }
            } else if (getNumberOfPlayers() != playerNumber) {
                if (getNumberOfPlayers() == 2 || playerNumber == 3)
                    System.out.println("Please wait for other player...");
                else
                    System.out.println("Please wait for other players...");
            }

            while (true) {
                // get server messages
                String serverMessage = (String) getInput().readObject();

                if (serverMessage.equals("players:")) {
                    showPlayers();
                } else if (serverMessage.startsWith("new player")) {
                    String newPlayerName = serverMessage.substring(11);
                    System.out.println(newPlayerName + " joined the game!");
                } else if (serverMessage.equals("cards:")) {
                    getCards();
                } else if (serverMessage.startsWith("ruler")) {
                    String rulerId = serverMessage.substring(6);
                    if (getId().equals(rulerId)) {
                        System.out.println("You are ruler!");
                        isRuler = true;
                    } else {
                        System.out.println(playersInGame.get(rulerId).trim() + " is ruler.");
                        isRuler = false;
                    }
                } else if (serverMessage.startsWith("set")) {
                    int numberOfSet = Integer.parseInt(serverMessage.substring(4));
                    System.out.println("Set " + numberOfSet + " has started.");
                } else if (serverMessage.startsWith("team")) {
                    String[] team = serverMessage.substring(6).split(",");
                    if (team[0].equals(getId()) || team[1].equals(getId())) {
                        // get id of teammate
                        if (team[0].equals(getId()))
                            teammateId = team[1];
                        else
                            teammateId = team[0];

                        System.out.println("Your team: " + playersInGame.get(team[0]) +
                                ", " + playersInGame.get(team[1]));
                    } else {
                        // save oponent ids
                        opponentId[0] = team[0];
                        opponentId[1] = team[1];
                        System.out.println("Other team: " + playersInGame.get(team[0]) +
                                ", " + playersInGame.get(team[1]));
                    }
                } else if (serverMessage.startsWith("rule")) {
                    if (!isRuler) {
                        rule = Card.Suit.valueOf(serverMessage.substring(5));
                        System.out.println("Rule is : " + rule);
                    }
                } else if (serverMessage.startsWith("round")) {
                    int round = Integer.parseInt(serverMessage.substring(6));
                    System.out.println("Round " + round + " has started.");
                    onTableCards.clear();
                } else if (serverMessage.startsWith("on table card")) {
                    // get player's id
                    String playerId = serverMessage.substring(14);

                    // get card
                    onTableCards.add((Card) getInput().readObject());
                    if (!playerId.equals(getId())) {
                        System.out.println(playersInGame.get(playerId) + " played " +
                                onTableCards.getLast());
                    }

                    // set teammate card
                    if (playerId.equals(teammateId))
                        teammateCard = onTableCards.getLast();
                } else if (serverMessage.startsWith("turn")) {
                    playerNumber = Integer.parseInt(serverMessage.substring(4, 5));
                    String id = serverMessage.substring(6);
                    if (id.equals(getId()))
                        putCard();
                    else
                        System.out.println("Now it's " + playersInGame.get(id) + "'s turn.");
                } else if (serverMessage.startsWith("server massage: ")) {
                    System.out.println(serverMessage);
                } else if (serverMessage.startsWith("winner round:")) {
                    String winnerID = serverMessage.substring(13);
                    int previousScore = scoresOfPlayers.get(winnerID);
                    scoresOfPlayers.replace(winnerID, ++previousScore);

                    showPlayerScores();
                    showWinner(winnerID);
                } else if (serverMessage.startsWith("winner set")) {
                    String winnerID = serverMessage.substring(11);

                    if (winnerID.equals(teammateId) || winnerID.equals(getId())) {
                        System.out.println("Congratulation! You winned this set!");
                        scoresOfTeams[0]++;
                    } else {
                        System.out.println("Unfortunatly you lost this set!");
                        scoresOfTeams[1]++;
                    }

                    // print scores of teams
                    System.out.println("Scores of teams:");
                    System.out.print("Your team: " + scoresOfTeams[0]);
                    System.out.println("Other team: " + scoresOfTeams[1]);
                } else if (serverMessage.equals("divide cards")) {
                    divideCards();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!getConnection().isConnected())
                return;
            e.printStackTrace();
        }
    }

    private void showWinner(String winnerID) {
        if (winnerID.equals(getId()) || winnerID.equals(teammateId)) {
            System.out.print("You ");
            if (getNumberOfPlayers() == 4)
                System.out.printf("and %s ", playersInGame.get(teammateId));
            System.out.println("winned this round.");

            scoresOfTeams[0]++;
        } else {
            System.out.println(playersInGame.get(winnerID));
            if (getNumberOfPlayers() == 4) {
                String anotherOpponent = playersInGame.get(opponentId[0]).equals(winnerID) ?
                        playersInGame.get(opponentId[1]) : playersInGame.get(opponentId[0]);
                System.out.printf(" and %s", anotherOpponent);
            }
            System.out.println(" winned this round.");

            scoresOfTeams[1]++;
        }

        teammateCard = null; // delete card of teammate
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
            getInput().readObject(); // for shake hands
            cards = (ArrayList<Card>) getInput().readObject();
            return;
        }

        // print cards
        Collections.sort(cards);
        showCards();
        System.out.println("Wait for ruler to select the rule.");
    }

    private void getRule() {
        // prompt to user to select rule
        Scanner input = new Scanner(System.in);
        System.out.println("Please choose rule : ");
        System.out.println("1-->> Clubs");
        System.out.println("2-->> Diamonds");
        System.out.println("3-->> Hearts");
        System.out.println("4-->> Spades");
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

    private void showPlayerScores() {
        System.out.println("Scores of players:");
        for (int i = 0; i < getNumberOfPlayers(); i++) {
            String id = players.get(i);
            System.out.printf("%d.%s %d%n", i + 1, playersInGame.get(id), scoresOfPlayers.get(id));
        }
    }

    private void showPlayers() throws IOException, ClassNotFoundException {
        System.out.println("Players in game: ");
        // save id and name of players in the game
        for (int i = 0; i < getNumberOfPlayers(); i++) {
            String message = (String) getInput().readObject();
            String[] temp = message.split(":");

            players.add(temp[0]);
            playersInGame.put(temp[0], temp[1]);
            scoresOfPlayers.put(temp[0], 0);

            // print in the output
            System.out.print((i + 1) + "." + temp[1] + " ");
            if (temp[0].equals(getId()))
                System.out.print("(You)");
            else if (getNumberOfPlayers() == 2) {
                opponentId[0] = temp[0];
            }
            System.out.println();
        }
    }

    private void putCard() {
        showCards();
        System.out.println("Your turn!");

        // check if we can show suggested card
        if (playerNumber == 1)
            System.out.println("Choose one of them:");
        else
            System.out.println("Choose one of them(suggested card is " +
                    GameService.suggestedCard(onTableCards, cards, teammateCard, rule) + "):");

        Scanner input = new Scanner(System.in);
        int choice = -1;

        for (int i = 0; i < 3; i++) {
            System.out.print(">>> ");

            try {
                choice = input.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Please enter a number!");
                input.nextLine();
            }

            if (choice > cards.size() || choice <= 0) {
                System.out.println("Your choice is out of range!");
            } else if (GameService.validCard(onTableCards, cards, cards.get(--choice), rule)) {
                break;
            } else {
                System.out.println("Your choice is invalid. please try again.");
                i--;
            }
        }

        // send card and remove it
        sendData(cards.get(choice));
        cards.remove(cards.get(choice));
        System.out.println("Successful");
    }

    private void divideCards() {
        Scanner scanner = new Scanner(System.in);
        try {
            cards = (ArrayList<Card>) this.getInput().readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if(isRuler){
            showCards();
            getRule();
            System.out.print("Choose your first card :");
            try {
                sendData(cards.get(scanner.nextInt()));
                System.out.print("Choose your second card :");
                sendData(cards.get(scanner.nextInt()));
            } catch (InputMismatchException e) {
                throw new RuntimeException(e);
            }
            try {
                Card card = (Card) getInput().readObject();
                while (true){
                    System.out.println("Do you want this card ? (yes/no)");
                    System.out.println(card);
                    String entry = scanner.nextLine();
                    if (Objects.equals(entry, "yes")){
                        sendData(true);
                        break;
                    } else if (Objects.equals(entry, "no")) {
                        sendData(false);
                        break;
                    }
                    else {
                        System.out.println("wrong entry");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        }
        else {
            showCards();
            System.out.print("Choose your first card :");
            try {
                sendData(cards.get(scanner.nextInt()));
                System.out.print("Choose your second card :");
                sendData(cards.get(scanner.nextInt()));
                System.out.print("Choose your third card :");
                sendData(cards.get(scanner.nextInt()));
            } catch (InputMismatchException e) {
                throw new RuntimeException(e);
            }
        }
            for (int i = 5; i < 25 ; i+=2) {
                try {
                    Card card = (Card) getInput().readObject();
                    while (true){
                        System.out.println("Do you want this card ? (yes/no)");
                        System.out.println(card);
                        String entry = scanner.nextLine();
                        if (Objects.equals(entry, "yes")){
                            sendData(true);
                            break;
                        } else if (Objects.equals(entry, "no")) {
                            sendData(false);
                            break;
                        }
                        else {
                            System.out.println("wrong entry");
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

    }
}
