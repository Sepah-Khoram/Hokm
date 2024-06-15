package Server;

import Utilities.Card;
import Utilities.GameService;
import Utilities.Set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.UUID;

public class Game implements Runnable {
    private final Player[] players;
    private final UUID token;
    private final ExecutorService executorService;
    private final CopyOnWriteArrayList<Player> connectedPlayers;
    private final ArrayList<Set> sets;
    private Set currentSet;
    private boolean isGameStarted;

    public Game(Player player, int numberOfPlayers) {
        players = new Player[numberOfPlayers];
        token = UUID.randomUUID();
        connectedPlayers = new CopyOnWriteArrayList<>();
        sets = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(numberOfPlayers);
        isGameStarted = false;

        addPlayer(player); // add player 1
    }

    public synchronized void addPlayer(Player player) {
        if (connectedPlayers.size() < players.length) {
            players[connectedPlayers.size()] = player;
            connectedPlayers.add(player);

            player.setPlayerNumber(connectedPlayers.size()); // set player number
            executorService.execute(player);

            if (connectedPlayers.size() == getNumberOfPlayers()) {
                executorService.shutdown();
                startGame();
            }
        }
    }

    private void startGame() {
        for(int i =0 ;i<4;i++){
            for (int j =i+1;j<3;j++){
                if(players[i].getName().equals(players[j].getName())){
                    players[j].setName(players[j].getName()+"1");
                }
            }
        }
        /*
         implement check name

        */
        System.out.println("Game " + token + " started with " + players.length + " players.");
        isGameStarted = true;
    }

    private synchronized void sendData(Object data) {
        for (Player player : players) {
            player.sendData(data);
        }
    }

    @Override
    public void run() {
        while (!isGameStarted) {
            try {
                Thread.sleep(100); // Wait for a short period before checking again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        while (!isGameOver()) {
            // new set
            sets.add(new Set(players));
            currentSet = sets.getLast();

            sendData("set: " + sets.size()); // send set number to the clients
            sendData("ruler: " + currentSet.getRuler().getName()); // get ruler of this set

            // devide cards btw users and send them to users
            Card[][] cards = GameService.divideCards(getNumberOfPlayers());
            for (int i = 0; i < players.length; i++) {
                players[i].setCards(Arrays.asList(cards[i]));
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
}
