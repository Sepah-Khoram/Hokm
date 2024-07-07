package Server;

import Utilities.Card;
import Utilities.GameType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Player extends Client implements Runnable {
    private static final Logger logger = LoggerManager.getLogger();
    private final UUID Id;
    private List<Card> cards = new ArrayList<>();
    private String name;
    private boolean connected;
    private int playerNumber;
    private Game game;
    private Card onTableCard;
    private boolean isRuler;

    public Player(Client client) throws IOException {
        super(client);
        this.connected = true;
        this.Id = UUID.randomUUID();
        sendData(Id);
    }

    @Override
    public void run() {
        try {
            sendInt(playerNumber); // send player number to player
            logger.info("Player number " + playerNumber + " sent to player.");

            // give player name
            Object data = getInput().readObject();
            process(data);

            if (game.getGameType() == GameType.Private) {
                sendData(game.getToken());
                sendInt(game.getNumberOfPlayers());
            }

            game.getBarrier().await();
            logger.info("Player " + name + " is ready.");

            while (connected) {
                data = getInput().readObject();
                logger.info("Received data from player " + name + ": " + data);
                process(data); // processing data
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error handling player " + name + ": " + e.getMessage(), e);
        } catch (BrokenBarrierException e) {
            logger.log(Level.SEVERE, "Error waiting at barrier for player " + name + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Thread interrupted for player " + name + ": " + e.getMessage(), e);
            Thread.currentThread().interrupt();
        } finally {
            closeConnection();
            logger.info("Connection closed with player " + name);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    private void process(Object data) {
        // processing data
        if (data.toString().startsWith("name:")) {
            name = data.toString().substring(5).trim();
            logger.info("Player name set to: " + name);
        } else if (data.toString().startsWith("rule:")) {
            game.setRule(Card.Suit.valueOf(data.toString().substring(5)));
            logger.info("Game rule set by player " + name + ": " + data.toString().substring(5));
        } else if (data instanceof Card) {
            onTableCard = (Card) data;
            logger.info("Card received from player " + name + ": " + onTableCard);
        } else {
            logger.warning("Invalid data received from player " + name + ": " + data);
        }
    }

    synchronized Card playCard() {
        while (onTableCard == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Thread interrupted while waiting for card from player " + name, e);
                Thread.currentThread().interrupt();
            }
        }
        // remove onTableCard from cards and send it
        cards.remove(onTableCard);
        Card copy = onTableCard;
        onTableCard = null;
        logger.info("Player " + name + " played card: " + copy);
        return copy;
    }

    void divideCards(Card[] cards) {
        // prompt user to dividing card
        sendData("divide cards");
        // send first five cards
        ArrayList<Card> card = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            card.add(cards[i]);
        }
        sendData(card);

        // get rule from ruler
        if (isRuler) {
            try {
                String rule = (String) this.getInput().readObject();
                game.setRule(getSuit(rule));
                this.addCards((Card) this.getInput().readObject());
                this.addCards((Card) this.getInput().readObject());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            // choosing rule
            sendData(cards[26]);
            try {
                boolean choose = (boolean) this.getInput().readObject();
                if (choose) {
                    this.addCards(cards[26]);
                } else {
                    this.addCards(cards[27]);
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            try {
                this.addCards((Card) this.getInput().readObject());
                this.addCards((Card) this.getInput().readObject());
                this.addCards((Card) this.getInput().readObject());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        for (int i = 5; i < 25; i += 2) {
            try {
                //  choose between cards
                sendData(cards[i]);
                boolean choose = (boolean) this.getInput().readObject();
                if (choose) {
                    this.addCards(cards[i]);
                } else {
                    this.addCards(cards[i + 1]);
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void setCards(List<Card> cards) {
        this.cards = cards;
        sendData("cards:");
        sendData(new ArrayList<>(cards));
    }

    void addCards(Card... cards) {
        this.cards.addAll(Arrays.asList(cards));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    void setGame(Game game) {
        this.game = game;
    }

    UUID getId() {
        return Id;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public void setRuler(boolean ruler) {
        isRuler = ruler;
    }
    public Card.Suit getSuit(String str){
        String rule = str.substring(5);
        if (rule.equals(Card.Suit.Clubs.name())){
            return Card.Suit.Clubs;
        } else if (rule.equals(Card.Suit.Spades.name())) {
            return Card.Suit.Spades;
        } else if (rule.equals(Card.Suit.Diamonds.name())) {
            return Card.Suit.Diamonds;
        } else if (rule.equals(Card.Suit.Hearts.name())) {
            return Card.Suit.Hearts;
        }
        else {
            return null;
        }

    }
}
