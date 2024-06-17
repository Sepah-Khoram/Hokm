package Server;

import Utilities.Card;
import Utilities.GameService;
import Utilities.Set;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game implements Runnable {
    private final Player[] players;
    private final UUID token;
    private static final Logger logger = LoggerManager.getLogger(); // logger for logging
    private final CyclicBarrier gameStartBarrier; // for wait threads for the game to start
    private final CopyOnWriteArrayList<Player> connectedPlayers;
    private final ExecutorService executorService; // manage threads
    private final List<Set> sets;
    private Set currentSet;
    private boolean isGameStarted;

    public Game(Player player, int numberOfPlayers) {
        players = new Player[numberOfPlayers];
        token = UUID.randomUUID();
        connectedPlayers = new CopyOnWriteArrayList<>();
        sets = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(numberOfPlayers);
        gameStartBarrier = new CyclicBarrier(numberOfPlayers, this::startGame);
        isGameStarted = false;

        logger.info("Game created with token " + token + " for " +
                numberOfPlayers + " players."); // logging create game

        addPlayer(player); // add player 1
    }

    public synchronized void addPlayer(Player player) {
        if (connectedPlayers.size() < players.length) {
            players[connectedPlayers.size()] = player;
            connectedPlayers.add(player);

            player.setGame(this); // set game
            player.setPlayerNumber(connectedPlayers.size()); // set player number
            executorService.execute(player);

            logger.info("Player " + player.getId() + " added to the game. Player number: "
                    + player.getPlayerNumber());

            if (connectedPlayers.size() == players.length) {
                logger.info("All players have joined the game.");
            }
        } else {
            logger.warning("Attempted to add a player to a full game.");
        }
    }
    private void checkPlayerNames() {

        ArrayList<String> uniquePlayerNames = new ArrayList<>();
        for (Player player : players) {
            String enteredName = player.getName();
            String newName = enteredName;

            int counter = 1;
            while (uniquePlayerNames.contains(newName)) {
                newName = enteredName + counter;
                counter++;
            }
            uniquePlayerNames.add(newName);
            player.setName(newName);
        }
    }
    private void startGame() {
        logger.info("All players have joined. Game is starting.");
        /*
         implement check name
        */
        // send id and name of players in the game
        sendData("players:");
        for (Player player : players) {
            sendData(player.getId() + ":" + player.getName());
        }

        System.out.println("Game " + token + " started with " + players.length + " players.");
        isGameStarted = true;
    }

    private synchronized void sendData(Object data) {
        for (Player player : players) {
            player.sendData(data);
        }
        logger.info("Data sent to all players: " + data);
    }

    @Override
    public void run() {
        while (!isGameStarted) {
            try {
                Thread.sleep(100); // Wait for a short period before checking again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "Game thread interrupted while waiting to start", e);
            }
        }

        while (!isGameOver()) {
            // new set
            sets.add(new Set(players));
            currentSet = sets.getLast();

            sendData("set:" + sets.size()); // send set number to the clients
            sendData("ruler:" + currentSet.getRuler().getId()); // get ruler of this set

            logger.info("New set started. Set number: " + sets.size());
            logger.info("Ruler of the set: " + currentSet.getRuler().getId());

            // devide cards btw users and send them to users
            Card[][] cards = GameService.divideCards(getNumberOfPlayers());
            for (int i = 0; i < players.length; i++) {
                players[i].setCards(Arrays.asList(cards[i]));
                logger.info("Cards dealt to player " +
                        players[i].getPlayerNumber() + ": " + Arrays.toString(cards[i]));
            }
        }
    }

    @Override
    public String toString() {
        return players.length + " players: " + connectedPlayers.toString();
    }

    private boolean isGameOver() {
        // codition to check if game is game overed
        return false;
    }

    public int getNumberOfPlayers() {
        return players.length;
    }

    public boolean isStarted() {
        return isGameStarted;
    }

    public UUID getToken() {
        return token;
    }

    public void setRule(Card.Suit rule) {
        currentSet.setRule(rule);
        logger.info("Rule set for the current set: " + rule);
    }

    public CyclicBarrier getBarrier() {
        return gameStartBarrier;
    }
}
