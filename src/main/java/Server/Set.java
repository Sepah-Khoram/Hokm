package Server;

import Utilities.Card;
import Utilities.GameService;

import java.security.SecureRandom;
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

    private final Player[] players;
    private final int numberOfPlayers;
    private final Player ruler;
    private Card.Suit rule;

    private final ArrayList<Team> teams;
    private final int[] scoresOfTeams;
    private final int[] scoresOfPlayers;
    private int round;
    private final ArrayList<Card> onTableCards = new ArrayList<>();

    private final Lock setLock = new ReentrantLock();
    private final Condition ruleSelected = setLock.newCondition();
    private final Condition turnCondition = setLock.newCondition();
    private int currentPlayerIndex;

    private Team winner;

    public Team getWinner() {
        return winner;
    }

    Set(Player[] players, ArrayList<Team> teams) {
        this.players = players;
        this.numberOfPlayers = players.length;
        this.teams = teams;

        this.scoresOfTeams = new int[teams.size()];
        Arrays.fill(scoresOfTeams, 0);

        this.scoresOfPlayers = new int[numberOfPlayers];
        Arrays.fill(scoresOfPlayers, 0);

        int indexOfRuler = new SecureRandom().nextInt(0, numberOfPlayers);
        this.ruler = players[indexOfRuler];

        Collections.rotate(Arrays.asList(players), 4 - indexOfRuler);
        logger.info("Set created with ruler: " + ruler.getName());
    }

    @Override
    public void run() {
        sendData("ruler:" + ruler.getId());
        divideCards();

        while (scoresOfTeams[0] < 7 && scoresOfTeams[1] < 7) {
            sendData("round:" + ++round);
            playRound();
        }

        if (scoresOfTeams[0] == 7) {
            winner = teams.get(0);
            sendData("winner set:team1");
        } else {
            winner = teams.get(1);
            sendData("winner set:team2");
        }
    }

    private void divideCards() {
        Card[][] cards = GameService.divideCards(numberOfPlayers);

        if (numberOfPlayers == 4) {
            for (int i = 1; i < numberOfPlayers; i++) {
                players[i].setCards(Arrays.asList(cards[i]));
            }

            ruler.setCards(Arrays.asList(cards[0]).subList(0, 5));

            setLock.lock();
            try {
                while (rule == null) {
                    ruleSelected.await();
                }
                ruler.setCards(Arrays.asList(cards[0]));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "Thread interrupted while waiting for rule selection: " + e.getMessage());
            } finally {
                setLock.unlock();
            }

            sendData("rule:" + rule);
        } else if (players.length == 2) {
            sendData("divide cards");
            for (int i = 0; i < 2; i++) {
                Player currentPlayer = players[i];
                for (int j = 0; j < 5; j++) {
                    sendData(cards[i][j], currentPlayer);
                }
                if (currentPlayer.equals(ruler)) {
                    sendData("Choose games rule");
                    try {
                        currentPlayer.getInput().readObject();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error reading rule selection: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void playRound() {
        for (int i = 0; i < numberOfPlayers; i++) {
            setLock.lock();
            try {
                currentPlayerIndex = i;
                turnCondition.signalAll();

                sendData("turn" + (i + 1) + ":" + players[currentPlayerIndex].getId());
                Card playedCard = players[i].playCard();
                logger.info("Player " + players[i].getName() + " played: " + playedCard);
                onTableCards.add(playedCard);

                sendData("on table card:" + players[currentPlayerIndex].getId());
                sendData(playedCard);

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

        Card winCard = GameService.topCard(onTableCards, rule);
        int indexOfWinner = onTableCards.indexOf(winCard);
        Player winner = players[indexOfWinner];

        scoresOfPlayers[indexOfWinner]++;
        if (teams.get(0).getPlayers().contains(winner)) {
            scoresOfTeams[0]++;
        } else {
            scoresOfTeams[1]++;
        }

        sendData("winner round:" + winner.getId().toString());

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
