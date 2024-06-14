package Server;

import Utilities.GameService;
import Utilities.Card;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.UUID;

public class Game implements Runnable {
    private final Player[] players;
    private UUID token;
    private final ExecutorService executorService;
    private final CopyOnWriteArrayList<Player> connectedPlayers;
    private boolean gameStarted;

    public Game(Player player, int numberOfPlayers) {
        players = new Player[numberOfPlayers];
        token = UUID.randomUUID();
        connectedPlayers = new CopyOnWriteArrayList<>();
        executorService = Executors.newFixedThreadPool(numberOfPlayers);
        gameStarted = false;

        addPlayer(player); // add player 1
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    public synchronized void addPlayer(Player player) {
        if (connectedPlayers.size() < players.length) {
            players[connectedPlayers.size()] = player;
            connectedPlayers.add(player);

            player.setPlayerNumber(connectedPlayers.size()); // set player number
            executorService.execute(player);

            if (connectedPlayers.size() == players.length) {
                executorService.shutdown();
                startGame();
            }
        }
    }

    private void startGame() {
        gameStarted = true;

        Card[][] cards = GameService.divideCards(players.length); // devide cards btw players
        for (int i = 0; i < players.length; i++) {
            try {
                players[i].setCards(new ArrayList<>(Arrays.asList(cards[i]))); // set cards of every player
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Game started with " + players.length + " players.");
    }

    @Override
    public void run() {
        while (!gameStarted) {
            try {
                Thread.sleep(100); // Wait for a short period before checking again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // منطق بازی که باید در حین اجرای بازی انجام شود
    }

    public int getNumberOfPlayers() {
        return players.length;
    }

    public boolean isStarted() {
        return gameStarted;
    }

    public UUID getToken() {
        return token;
    }
}
