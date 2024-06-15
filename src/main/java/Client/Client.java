package Client;

import Server.Game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

public class Client {
    private Socket connection; // connection to server
    private ObjectOutputStream output; // output to server
    private ObjectInputStream input; // input from server
    private String id;
    private int numberOfPlayers;

    public ObjectOutputStream getOutput() {
        return output;
    }

    public ObjectInputStream getInput() {
        return input;
    }

    public ArrayList<Game> showCurrentGames(String host) {
        ArrayList<Game> currentGames = null;

        try {
            connectionTo(host);
            sendData("getGames"); // send request to server

            currentGames = (ArrayList<Game>) input.readObject(); // get games from server
        } catch (IOException e) {
            System.err.println("Connection to server failed!");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

         return currentGames;
    }

    public boolean joinGame(String host, int numberOfPlayers) throws IllegalArgumentException {
        setNumberOfPlayer(numberOfPlayers); // set number of players

        System.out.println("joining game..."); // prompt

        try {
            connectionTo(host);
            sendData("join random:" + numberOfPlayers);

            // get player id
            id = input.readObject().toString();
        } catch (IOException e) {
            System.err.println("Connection to server failed!");
            return false;
        } catch (ClassNotFoundException e) {
            System.err.println("Error in get data from server!");
        }
        return true;
    }

    public boolean joinGame(String host, UUID token) {
        System.out.println("joining game..."); // prompt

        try {
            connectionTo(host);
            sendData("join token:" + token.toString());

            // get player id
            id = input.readObject().toString();
        } catch (IOException e) {
            System.err.println("Connection to server failed!");
            return false;
        } catch (ClassNotFoundException e) {
            System.err.println("Error in get data from server!");
        }
        return true;
    }

    public boolean createGame(String host, int numberOfPlayers) throws IllegalArgumentException {
        setNumberOfPlayer(numberOfPlayers); // set number of players

        System.out.println("Creating game..."); // prompt

        try {
            connectionTo(host);
            sendData("create:" + numberOfPlayers);

            // get player id
            id = input.readObject().toString();
        } catch (IOException e) {
            System.err.println("Connection to server failed!");
            return false;
        } catch (ClassNotFoundException e) {
            System.err.println("Error in get data from server!");
        }
        return true;
    }

    public void sendData(Object data) {
        try {
            output.writeObject(data);
            output.flush();
        } catch (IOException e) {
            System.err.println("Error sending data: " + e.getMessage());
        }
    }

    private void connectionTo(String host) throws IOException {
        connection = new Socket(host, 5482);
        output = new ObjectOutputStream(connection.getOutputStream());
        input = new ObjectInputStream(connection.getInputStream());

        try {
            id = input.readObject().toString(); // client id for shake hands
        } catch (ClassNotFoundException e) {
            System.out.println("Unexpected error. Terminating...");
            System.exit(1);
        }
    }

    public Socket getConnection() {
        return connection;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayer(int numberOfPlayer) throws IllegalArgumentException {
        if (numberOfPlayer != 2 && numberOfPlayer != 4)
            throw new IllegalArgumentException("Sorry. We support just game with 2 or 4 players. " +
                    "Try again.");

        this.numberOfPlayers = numberOfPlayer;
    }
}
