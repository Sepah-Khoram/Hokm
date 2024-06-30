package Server;

import Utilities.Card;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game implements Runnable {
    // for manage the multithreading and logging
    private static final Logger logger = LoggerManager.getLogger(); // logger for logging
    private final CyclicBarrier gameStartBarrier; // for wait threads for the game to start
    private final CopyOnWriteArrayList<Player> connectedPlayers;
    private final ExecutorService executorService; // manage threads

    // for manage the game
    private final Player[] players;
    private final ArrayList<Team> teams = new ArrayList<>();
    private final UUID token;
    private final List<Set> sets;
    private Set currentSet;
    private boolean isGameStarted;
    private int winTeam1=0;
    private int winTeam2 = 0;

    Game(Player player, int numberOfPlayers) {
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

    synchronized void addPlayer(Player player) {
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

        // make two treams
        if (getNumberOfPlayers() == 4) {
            makeTwoTeams();
            sendData("team1:" + teams.getFirst());
            sendData("team2:" + teams.getLast());
        }

        System.out.println("Game " + token + " started with " + players.length + " players.");
        isGameStarted = true;
    }

    @Override
    public void run() {
        while (!isGameStarted) {
            try {
                Thread.sleep(1000); // Wait for a short period before checking again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "Game thread interrupted while waiting to start", e);
            }
        }

        while (!isGameOver()) {
            // new set
            if (getNumberOfPlayers() == 4) {
                sets.add(new Set(players, teams));
                currentSet = sets.getLast();
                teams.getFirst().addSet(currentSet);
                teams.getLast().addSet(currentSet);
            } else {
                sets.add(new Set(players));
                currentSet = sets.getLast();
            }

            sendData("set:" + sets.size()); // send set number to the clients
            logger.info("New set started. Set number: " + sets.size());

            // start the set
            currentSet.run();
            if (teams.getFirst() == currentSet.getWinner()) {
                winTeam1++;
            }
            else{
                winTeam2++;
            }
        }
    }

    private void makeTwoTeams() {
        // create team1 and team2
        ArrayList<Player> team1 = new ArrayList<>(2);
        team1.add(players[4]);

        ArrayList<Player> team2 = new ArrayList<>(2);

        // find the teammate for the last player
        int teammateIndex = new SecureRandom().nextInt(4);
        team1.add(players[teammateIndex]);

        // make team2
        for (int i = 0; i < 3; i++) {
            if (i != teammateIndex)
                team2.add(players[i]);
        }

        // add team to game
        teams.add(new Team(team1));
        teams.add(new Team(team2));
    }

    private boolean isGameOver() {
        // codition to check if game is game overed
        if(sets.size()<7){
            return false;
        }
        else{
            if(winTeam1>=7){
                return true;
            }
            else if (wintTeam2>=7) {
                return true;
            }
            else{
                return false;
            }
        }
    }

    private synchronized void sendData(Object data) {
        for (Player player : players) {
            player.sendData(data);
        }
    }

    @Override
    public String toString() {
        return players.length + " players: " + connectedPlayers.toString();
    }

    public int getNumberOfPlayers() {
        return players.length;
    }

    boolean isStarted() {
        return isGameStarted;
    }

    public UUID getToken() {
        return token;
    }

    void setRule(Card.Suit rule) {
        currentSet.setRule(rule);
        logger.info("Rule set for the current set: " + rule);
    }

    CyclicBarrier getBarrier() {
        return gameStartBarrier;
    }
}
