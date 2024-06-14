package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

public class Player implements Runnable {
    private UUID playerId;
    private Socket connection;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean connected;
    private int playerNumber;

    public Player(Socket connection) throws IOException {
        this.connection = connection;
        this.output = new ObjectOutputStream(connection.getOutputStream());
        this.input = new ObjectInputStream(connection.getInputStream());
        this.connected = true;
        this.playerId = UUID.randomUUID();
    }

    @Override
    public void run() {
        try {
            while (connected) {
                Object data = input.readObject();
                // پردازش داده‌ها
                process(data);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling player: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void process(Object data) {
        // پردازش داده‌ها
        System.out.println("Received data: " + data);
    }

    public void sendData(Object data) {
        try {
            output.writeObject(data);
            output.flush();
        } catch (IOException e) {
            System.err.println("Error sending data: " + e.getMessage());
        }
    }

    private void closeConnection() {
        connected = false;
        try {
            connection.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }
    public UUID getPlayerId() {
        return playerId;
    }
}
