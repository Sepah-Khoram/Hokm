package Server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.UUID;
public class Game implements Runnable {
    private final Player[] players;
    private UUID gameId;
    private String token;
    private final ExecutorService executorService;
    private final CopyOnWriteArrayList<Player> connectedPlayers;
    private boolean gameStarted;

    public Game(Player player, int numberOfPlayers) {
        players = new Player[numberOfPlayers];
        gameId = UUID.randomUUID();
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
                startGame();
            }
        }
    }

    private void startGame() {
        gameStarted = true;
        // شروع بازی و انجام تنظیمات اولیه
        System.out.println("Game started with " + players.length + " players.");
    }

    @Override
    public void run() {
        while (!gameStarted) {

        }

        // منطق بازی که باید در حین اجرای بازی انجام شود
    }

    public int getNumberOfPlayers() {
        return players.length;
    }

    public boolean isStarted() {
        return gameStarted;
    }
    public UUID getGameId() {
        return gameId;
    }
    public String getToken() {
        return token;
    }
}
