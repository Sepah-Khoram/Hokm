package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Player implements Runnable {
    private Socket connection;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public Player(Socket connection) throws IOException {
        this.connection = connection;

        output = new ObjectOutputStream(connection.getOutputStream());
        input = new ObjectInputStream(connection.getInputStream());
    }

    @Override
    public void run() {

    }
}
