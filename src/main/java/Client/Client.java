package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private Socket connection; // connection to server
    private ObjectOutputStream output; // output to server
    private ObjectInputStream input; // input from server

    public ObjectOutputStream getOutput() {
        return output;
    }

    public ObjectInputStream getInput() {
        return input;
    }


    public void joinGame(String host, int numberOfPlayers) {
        try {
            getConnection(host);
            output.writeObject("join" + numberOfPlayers);
            output.flush();
        } catch (IOException e) {
            System.out.println("Connection to server failed!");
        }
    }


    public void createGame(String host, int numberOfPlayers) {
        try {
            getConnection(host);
            output.writeObject("create" + numberOfPlayers);
            output.flush();
        } catch (IOException e) {
            System.out.println("Connection to server failed!");
        }
    }

    private void getConnection(String host) throws IOException {
        connection = new Socket(host, 5482);
        output = new ObjectOutputStream(connection.getOutputStream());
        input = new ObjectInputStream(connection.getInputStream());

        input.readUTF(); // for shake hands
    }
}
