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
    // player
    private final Player[] players;
    private final int numberOfPlayers;

    // property of ruler
    private final Player ruler;
    private final int indexOfRuler;
    private Card.Suit rule;

    // teams
    private final ArrayList<Team> teams;
    private final int[] scoresOfTeams;
    private final int[] scoresOfPlayers;
    private int round;
    private final ArrayList<Card> onTableCards = new ArrayList<>();

    // concurrency
    private final Lock setLock = new ReentrantLock();
    private final Condition ruleSelected = setLock.newCondition();
    private final Condition[] otherPlayerTurn;
    private int currentPlayerIndex;

    // result of the game
    private String result;
    private Team winner;

    public  Team getWinner() {
        return winner;
    }

    Set(Player @NotNull [] players, @NotNull ArrayList<Team> teams) {
        this.players = players;
        this.numberOfPlayers = players.length;
        this.teams = teams;

        this.scoresOfTeams = new int[teams.size()];
        for (int i = 0; i < teams.size(); i++) {
            scoresOfTeams[i] = 0;
        }

        this.scoresOfPlayers = new int[numberOfPlayers];
        for (int i = 0; i < numberOfPlayers; i++) {
            scoresOfPlayers[i] = 0;
        }

        // determine ruler
        this.indexOfRuler = new SecureRandom().nextInt(0, numberOfPlayers);
        this.ruler = players[indexOfRuler];

        // determine conditions
        this.otherPlayerTurn = new Condition[numberOfPlayers];
        for (int i = 0; i < numberOfPlayers; i++) {
            otherPlayerTurn[i] = setLock.newCondition();
        }
    }

    @Override
    public void run() {
        // specify ruler
        sendData("ruler:" + ruler.getId());

        divideCards();

        while (scoresOfTeams[0] < 7 && scoresOfTeams[1] < 7) {
            // start the round
            sendData("round:" + ++round);

            for (int i = 0; i < numberOfPlayers; i++) {
                setLock.lock();
                try {
                    while (currentPlayerIndex != i) {
                        otherPlayerTurn[i].await();
                    }
                    // get cards from the current player
                    sendData("turn", players[currentPlayerIndex]);
                    Card playedCard = players[i].playCard();
                    System.out.println("Player " + players[i].getId() + " played: " + playedCard);
                    onTableCards.add(playedCard);

                    // send played card
                    sendData("on table card:" + players[currentPlayerIndex].getId());
                    sendData(playedCard);

                    // change the turn
                    currentPlayerIndex++;
                    otherPlayerTurn[currentPlayerIndex].signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Thread interrupted while waiting for player's turn: " + e.getMessage());
                } finally {
                    setLock.unlock();
                }
            }

            // determine the winner of the round
            Card winCard = GameService.topCard(onTableCards, rule);
            int indexOfWinner = onTableCards.indexOf(winCard);
            Player winner = players[indexOfWinner];

            // increase score of the winner and winner team
            scoresOfPlayers[indexOfWinner]++;
            if (teams.getFirst().getPlayers().contains(winner))
                scoresOfTeams[0]++;
            else
                scoresOfTeams[1]++;

            // send the winner to client
            sendData("winner:");
            sendData(winner.getId());

            // initial again
            onTableCards.clear();
            currentPlayerIndex = 0;
        }

        // determine the winner of the set and send it
        if (scoresOfTeams[0] == 7) {
            winner = teams.getFirst();
            sendData("winner set:team1");
        } else {
            winner = teams.getLast();
            sendData("winner set:team2");
        }
    }

    private void divideCards() {
        // devide cards btw users and send them to users
        Card[][] cards = GameService.divideCards(numberOfPlayers);

        if (numberOfPlayers == 4) {
            for (int i = 0; i < numberOfPlayers; i++) {
                if (i != indexOfRuler)
                    players[i].setCards(Arrays.asList(cards[i]));
            }

            // set 5 first 5 cards of ruler
            ruler.setCards(Arrays.asList(cards[indexOfRuler]).subList(0, 5));

            // Wait for ruler to select the rule
            setLock.lock();
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
                setLock.unlock();
            }

            // send the rule of the game
            sendData("rule:" + rule);
        } else {

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
        setLock.lock();
        try {
            this.rule = rule;
            ruleSelected.signalAll();
        } finally {
            setLock.unlock();
        }
    }
}
