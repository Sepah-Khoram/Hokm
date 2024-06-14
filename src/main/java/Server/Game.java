package Server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;

public class Game implements Runnable {
    private final Player[] players;
    private final ExecutorService executorService;
    private final CopyOnWriteArrayList<Player> connectedPlayers;
    private boolean gameStarted;

    public Game(Player player, int numberOfPlayers) {
        players = new Player[numberOfPlayers];
        players[0] = player;
        connectedPlayers = new CopyOnWriteArrayList<>();
        connectedPlayers.add(player);

        executorService = Executors.newFixedThreadPool(numberOfPlayers);
        executorService.execute(player);

        gameStarted = false;
    }

    public synchronized void addPlayer(Player player) {
        if (connectedPlayers.size() < players.length) {
            connectedPlayers.add(player);
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
            // انتظار برای شروع بازی
        }

        // منطق بازی که باید در حین اجرای بازی انجام شود
    }
}
