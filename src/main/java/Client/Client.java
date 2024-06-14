package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private Socket connection; // connection to server
    private ObjectOutputStream output; // output to server
    private ObjectInputStream input; // input from server
    private String id;

    public ObjectOutputStream getOutput() {
        return output;
    }

    public ObjectInputStream getInput() {
        return input;
    }

    public void joinGame(String host, int numberOfPlayers) throws IllegalArgumentException {
        if (numberOfPlayers != 2 && numberOfPlayers != 4)
            throw new IllegalArgumentException("Sorry. We support just game with 2 or 4 players. " +
                    "Try again.");

        System.out.println("joining game..."); // prompt

        try {
            getConnection(host);
            output.writeObject("join" + numberOfPlayers);
            output.flush();
        } catch (IOException e) {
            System.err.println("Connection to server failed!");
        }
    }

    public void createGame(String host, int numberOfPlayers) throws IllegalArgumentException {
        if (numberOfPlayers != 2 && numberOfPlayers != 4)
            throw new IllegalArgumentException("Sorry. We support just game with 2 or 4 players. " +
                    "Try again.");

        System.out.println("Creating game..."); // prompt

        try {
            getConnection(host);
            output.writeObject("create" + numberOfPlayers);
            output.flush();
        } catch (IOException e) {
            System.err.println("Connection to server failed!");
        }
    }

    private void getConnection(String host) throws IOException {
        connection = new Socket(host, 5482);
        output = new ObjectOutputStream(connection.getOutputStream());
        input = new ObjectInputStream(connection.getInputStream());

        try {
            id = input.readObject().toString(); // for shake hands
        } catch (ClassNotFoundException e) {
            System.out.println("Unexpected error. Terminating...");
            System.exit(1);
        }
    }
}
