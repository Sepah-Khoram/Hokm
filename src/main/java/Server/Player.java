package Server;

import Utilities.Card;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;

public class Player extends Client implements Runnable {
    private List<Card> cards = new ArrayList<>();
    private String name;
    private final UUID Id;
    private boolean connected;
    private int playerNumber;
    private Game game;

    public Player(Client client) throws IOException {
        super(client);
        this.connected = true;
        this.Id = UUID.randomUUID();
        sendData(Id);
    }

    @Override
    public void run() {
        try {
            sendData(playerNumber); // send player number to player
            // give player name
            Object data = getInput().readObject();
            process(data);
            game.getBarrier().await();

            while (connected) {
                data = getInput().readObject();
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

    private void process(@NotNull Object data) {
        // proccesing datas
        if (data.toString().startsWith("name:")) {
            name = data.toString().substring(5);
        } else if (data.toString().startsWith("rule:")) {
            game.setRule(Card.Suit.valueOf(data.toString().substring(5)));
        }

        System.out.println("Received data: " + data);
    }

    @Override
    public synchronized void sendData(Object data) {
        try {
            getOutput().writeObject(data);
            getOutput().flush();
        } catch (IOException e) {
            System.err.println("Error sending data: " + e.getMessage());
        }
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
        sendData("cards:");
        sendData(cards);
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public UUID getId() {
        return Id;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }
}
