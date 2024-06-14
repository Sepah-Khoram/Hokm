package Server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;

public class Game implements Runnable {
    private final Player[] players;
    private final ExecutorService executorService;
    private final CopyOnWriteArrayList<Player> connectedPlayers;
    private boolean gameStarted;
    private int joinedPlayers;

    public Game(Player player, int numberOfPlayers) {
        players = new Player[numberOfPlayers];
        connectedPlayers = new CopyOnWriteArrayList<>();
        executorService = Executors.newFixedThreadPool(numberOfPlayers);
        gameStarted = false;
        joinedPlayers = 0;

        addPlayer(player); // add player 1
    }

    public synchronized void addPlayer(Player player) {
        if (connectedPlayers.size() < players.length) {
            players[joinedPlayers++] = player;
            connectedPlayers.add(player);

            player.setPlayerNumber(joinedPlayers); // set player number
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
}
