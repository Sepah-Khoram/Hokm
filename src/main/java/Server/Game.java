package Server;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Game implements Runnable {
    private final Player[] players;
    private Executor executorService;

    public Game(Player player, int numberOfPlayers) {
        players = new Player[numberOfPlayers];
        players[0] = player;

        executorService = Executors.newFixedThreadPool(numberOfPlayers);

        executorService.execute(player);
    }

    @Override
    public void run() {

    }
}
