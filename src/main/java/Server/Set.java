package Server;

import Utilities.Card;
import Utilities.GameService;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Set implements Runnable {
    // stream of the game
    private final Player[] players;
    private final int numberOfPlayers;

    private final Player ruler;
    private final int indexOfRuler;
    private Card.Suit rule;

    private final ArrayList<Team> teams;
    private final Lock lock = new ReentrantLock();
    private final Condition ruleSelected = lock.newCondition();
    private int round;
    // result of the game
    private String result;
    private Team winner;

    public static Server.Team getWinner() {
        return winner;
    }

    Set(Player @NotNull [] players, ArrayList<Team> teams) {
        this.players = players;
        this.numberOfPlayers = players.length;
        this.teams = teams;

        // determine ruler
        this.indexOfRuler = new SecureRandom().nextInt(0, numberOfPlayers);
        this.ruler = players[indexOfRuler];
    }

    Set(Player[] players) {
        this(players, null);
    }

    @Override
    public void run() {
        // specify ruler
        sendData("ruler:" + ruler.getId());

        divideCards();
    }

    private void divideCards() {
        if (numberOfPlayers == 4) {
            // devide cards btw users and send them to users
            Card[][] cards = GameService.divideCards(numberOfPlayers);

            for (int i = 0; i < numberOfPlayers; i++) {
                if (i != indexOfRuler)
                    players[i].setCards(Arrays.asList(cards[i]));
            }

            // set 5 first 5 cards of ruler
            ruler.setCards(Arrays.asList(cards[indexOfRuler]).subList(0, 5));

            // Wait for ruler to select the rule
            lock.lock();
            try {
                while (rule == null) {
                    ruleSelected.await();
                }
                // After the rule is selected, send all the cards to the ruler
                ruler.setCards(Arrays.asList(cards[indexOfRuler]));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted while waiting for rule selection: " +
                        e.getMessage());
            } finally {
                lock.unlock();
            }

            // send the rule of the game
            sendData("rule:" + rule);
        }
    }

    private synchronized void sendData(Object data, Player @NotNull ... players) {
        for (Player player : players) {
            player.sendData(data);
        }
    }

    private synchronized void sendData(Object data) {
        sendData(data, players);
    }

    public void setRule(Card.Suit rule) {
        lock.lock();
        try {
            this.rule = rule;
            ruleSelected.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
