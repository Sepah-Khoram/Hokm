package Server;

import Utilities.Card;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;

public class Player extends Client implements Runnable {
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
            // give player name
            Object data = getInput().readObject();
            process(data);
            game.getBarrier().await();

            while (connected) {
                data = getInput().readObject();
                System.out.println(Thread.currentThread().getName() + " has ricived data");
                process(data); // proccessing datas
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling player: " + e.getMessage());
        } catch (BrokenBarrierException e) {
            System.err.println("Error waiting at barrier: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Error thread interrupted: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    private void process(Object data) {
        // proccesing datas
        if (data.toString().startsWith("name:")) {
            name = data.toString().substring(5).trim();
        } else if (data.toString().startsWith("rule:")) {
            game.setRule(Card.Suit.valueOf(data.toString().substring(5)));
        } else if (data instanceof Card) {
            onTableCard = (Card) data;
        }

        System.out.println("Received data: " + data);
    }

    synchronized Card playCard() {
        while (onTableCard == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // remove onTableCard from cards and send it
        cards.remove(onTableCard);
        Card copy = onTableCard;
        onTableCard = null;
        return copy;
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

    void divideCards(Card[] cards) {
        // prompt user to dividing card
        sendData("divide cards");
        // send first five cards
        for (int i = 0; i < 5; i++) {
            sendData(cards[i]);
        }

        // get rule from ruler
        if (isRuler) {
            try {
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
        } else {
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

    public void setRuler(boolean ruler) {
        isRuler = ruler;
    }
}
