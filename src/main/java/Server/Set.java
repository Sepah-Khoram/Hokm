package Server;

import Utilities.Card;
import Utilities.GameService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Set implements Runnable {
    private static final Logger logger = Logger.getLogger(Set.class.getName());

    // player
    private final Player[] players;
    private final int numberOfPlayers;

    // property of ruler
    private final Player ruler;
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
    private final Condition turnCondition = setLock.newCondition();

    // result of the game
    private Team winner;

    public Team getWinner() {
        return winner;
    }

    Set(Player[] players, ArrayList<Team> teams, Player ruler) {
        this.players = players;
        this.ruler = ruler;
        this.numberOfPlayers = players.length;
        this.teams = teams;

        this.scoresOfTeams = new int[teams.size()];
        Arrays.fill(scoresOfTeams, 0);

        this.scoresOfPlayers = new int[numberOfPlayers];
        Arrays.fill(scoresOfPlayers, 0);
    }

    @Override
    public void run() {
        // divide cards
        divideCards();

        while (scoresOfTeams[0] < 7 && scoresOfTeams[1] < 7) {
            // start the round
            sendData("round:" + ++round);
            playRound();
        }

        // determine the winner of the set and send it
        if (scoresOfTeams[0] == 7) {
            winner = teams.getFirst();
        } else {
            winner = teams.get(1);
        }

        // send winner of the set to the clients
        sendData("winner set:" + winner.getPlayers().getFirst().getId());
    }

    private void divideCards() {
        // devide cards btw users and send them to users
        Card[][] cards = GameService.divideCards(numberOfPlayers);

        if (numberOfPlayers == 4) {
            for (int i = 1; i < numberOfPlayers; i++) {
                players[i].setCards(Arrays.asList(cards[i]));
            }

            // set 5 first 5 cards of ruler
            ruler.setCards(Arrays.asList(cards[0]).subList(0, 5));

            // Wait for ruler to select the rule
            setLock.lock();
            try {
                while (rule == null) {
                    ruleSelected.await();
                }
                // After the rule is selected, send all the cards to the ruler
                ruler.setCards(Arrays.asList(cards[0]));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "Thread interrupted while waiting for rule selection: " + e.getMessage());
            } finally {
                setLock.unlock();
            }

            // send the rule of the game
            sendData("rule:" + rule);
        } else if (players.length == 2) {
            // determine player that is not ruler
            Player notRuler = (players[0] == ruler) ? players[1] : players[0];

            Thread player1 = new Thread(() -> ruler.divideCards(cards[0]));
            Thread player2 = new Thread(() -> notRuler.divideCards(cards[1]));

            player1.start();
            player2.start();
        }
    }

    private void playRound() {
        // they should play cards
        int currentPlayerIndex;
        for (int i = 0; i < numberOfPlayers; i++) {
            setLock.lock();
            try {
                currentPlayerIndex = i;
                turnCondition.signalAll();

                // get cards from the current player
                sendData("turn" + (i + 1) + ":" + players[currentPlayerIndex].getId());
                Card playedCard = players[i].playCard();
                logger.info("Player " + players[i].getName() + " played: " + playedCard);
                onTableCards.add(playedCard);

                // send played card
                sendData("on table card:" + players[currentPlayerIndex].getId());
                sendData(playedCard);

                // change the turn
                currentPlayerIndex++;
                while (currentPlayerIndex == i) {
                    turnCondition.await();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "Thread interrupted during playRound: " + e.getMessage());
            } finally {
                setLock.unlock();
            }
        }

        // determine the winner of the round
        Card winCard = GameService.topCard(onTableCards, rule);
        int indexOfWinner = onTableCards.indexOf(winCard);
        Player winner = players[indexOfWinner];
        Collections.rotate(Arrays.asList(players),4 - indexOfWinner);

        // increase score of the winner and winner team
        scoresOfPlayers[indexOfWinner]++;
        if (teams.getFirst().getPlayers().contains(winner)) {
            scoresOfTeams[0]++;
        } else {
            scoresOfTeams[1]++;
        }

        // send the winner to client
        sendData("winner round:" + winner.getId().toString());

        // initial again
        onTableCards.clear();
        currentPlayerIndex = 0;
    }

    private synchronized void sendData(Object data, Player... players) {
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

    public int getRound() {
        return round;
    }
}
