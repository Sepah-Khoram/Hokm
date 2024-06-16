package Server;

import Utilities.Card;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player extends Client implements Runnable {
    private List<Card> cards = new ArrayList<>();
    private String name;
    private final UUID Id;
    private boolean connected;
    private int playerNumber;

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
            while (connected) {
                Object data = getInput().readObject();
                process(data); // proccessing datas
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling player: " + e.getMessage());
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
            name = data.toString().substring(6);
        }

        System.out.println("Received data: " + data);
    }

    public void setName(String name) {
        this.name = name;
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

    public UUID getId() {
        return Id;
    }
}
