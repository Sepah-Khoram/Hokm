package Server;

import Utilities.Card;
import Utilities.GameService;
import Utilities.GameType;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game implements Runnable {
    // for manage the multithreading and logging
    private static final Logger logger = LoggerManager.getLogger(); // logger for logging
    private final CyclicBarrier gameStartBarrier; // for wait threads for the game to start
    private final ExecutorService executorService; // manage threads

    // for manage the game
    private final Player[] players;
    private final ArrayList<Team> teams = new ArrayList<>();
    private final UUID token;
    private final List<Set> sets;
    private final GameType gameType;
    private Set currentSet;
    private boolean isGameStarted;
    private int connectedPlayers; // no one added
    private int winTeam1 = 0;
    private int winTeam2 = 0;

    Game(Player player, int numberOfPlayers, GameType gameType) {
        this.gameType = gameType;
        players = new Player[numberOfPlayers];
        token = UUID.randomUUID();
        sets = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(numberOfPlayers);
        gameStartBarrier = new CyclicBarrier(numberOfPlayers, this::startGame);
        isGameStarted = false;

        logger.info("Game created with token " + token + " for " +
                numberOfPlayers + " players."); // logging create game

        addPlayer(player); // add player 1
    }

    synchronized void addPlayer(Player player) {
        if (connectedPlayers < players.length) {
            players[connectedPlayers++] = player;

            player.setGame(this); // set game
            player.setPlayerNumber(connectedPlayers); // set player number
            executorService.execute(player);

            // send information about new player to previous player
            if (connectedPlayers > 1) {
                try {
                    while (player.getName() == null)
                        Thread.sleep(10);
                    // send the notification to previous players
                    Player[] previousPlayers = Arrays.copyOfRange(players, 0, connectedPlayers - 1);
                    sendData("new player:" + player.getName(), previousPlayers);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            logger.info("Player " + player.getId() + " added to the game. Player number: "
                    + player.getPlayerNumber());

            if (connectedPlayers == players.length) {
                logger.info("All players have joined the game.");
            }
        } else {
            logger.warning("Attempted to add a player to a full game.");
        }
    }

    private void startGame() {
        logger.info("All players have joined. Game is starting.");

        // if name of someone is identical change it
        {
            // save names of players in an array
            String[] names = new String[getNumberOfPlayers()];
            for (int i = 0; i < getNumberOfPlayers(); i++) {
                names[i] = players[i].getName();
            }

            String[] newNames = GameService.processNames(names);

            // set new names
            for (int i = 0; i < getNumberOfPlayers(); i++) {
                players[i].setName(newNames[i]);
            }
        }

        // send id and name of players in the game
        sendData("players:");
        for (Player player : players) {
            sendData(player.getId() + ":" + player.getName());
        }

        // make two treams
        if (getNumberOfPlayers() == 4) {
            makeTwoTeams();
            sendData("team1:" + teams.getFirst().toString());
            sendData("team2:" + teams.getLast().toString());
        } else {
            // simulate player1 with team1
            ArrayList<Player> team1 = new ArrayList<>();
            team1.add(players[0]);
            teams.add(new Team(team1));

            // simulate player2 with team2
            ArrayList<Player> team2 = new ArrayList<>();
            team2.add(players[1]);
            teams.add(new Team(team2));
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
            sets.add(new Set(players, teams));
            currentSet = sets.getLast();
            teams.getFirst().addSet(currentSet);
            teams.getLast().addSet(currentSet);

            sendData("set:" + sets.size()); // send set number to the clients
            logger.info("New set started. Set number: " + sets.size());

            // start the set
            currentSet.run();
            if (teams.getFirst() == currentSet.getWinner()) {
                winTeam1++;
            } else {
                winTeam2++;
            }
        }
    }

    private void makeTwoTeams() {
        // create team1 and team2
        ArrayList<Player> team1 = new ArrayList<>(2);
        ArrayList<Player> team2 = new ArrayList<>(2);

        team1.add(players[0]);
        team1.add(players[1]);

        team2.add(players[2]);
        team2.add(players[3]);

        // add team to game
        teams.add(new Team(team1));
        teams.add(new Team(team2));
    }

    private boolean isGameOver() {
        // codition to check if game is game overed
        return (winTeam1 == 7 || winTeam2 == 7);
    }

    private synchronized void sendData(Object data, Player... players) {
        for (Player player : players)
            player.sendData(data);
    }

     synchronized void sendData(Object data) {
        sendData(data, players);
    }

    synchronized void sendMessage(String message) {
        for (int i = 0; i < connectedPlayers; i++) {
            sendData(message, players[i]);
        }
    }

    @Override
    public String toString() {
        return players.length + " players: " + Arrays.toString(players);
    }

    public int getNumberOfPlayers() {
        return players.length;
    }

    boolean hasNotStarted() {
        return !isGameStarted;
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

    int getWinTeam1() {
        return winTeam1;
    }

    int getWinTeam2() {
        return winTeam2;
    }

    int getCurrentSet() {
        return sets.size();
    }

    int getCurrentRound() {
        if (currentSet != null)
            return currentSet.getRound();
        else
            return 0;
    }

    public int getconnectedPlayer() {
        return connectedPlayers;
    }

    public GameType getGameType() {
        return gameType;
    }
}
