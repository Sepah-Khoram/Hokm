package Server;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    // Using LoggerManager to get the logger
    private static final Logger logger = LoggerManager.getLogger();

    private Socket connection;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public Client(@NotNull Socket connection) {
        try {
            this.connection = connection;
            this.output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            this.input = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error initializing Client", e);
            closeConnection();
        }
    }

    protected Client(@NotNull Client client) {
        connection = client.getConnection();
        output = client.getOutput();
        input = client.getInput();
    }

    public void sendInt(int number) {
        try {
            output.writeInt(number);
            output.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending integer: " + number, e);
            closeConnection();
        }
    }

    public void sendData(Object data) {
        try {
            output.writeObject(data);
            output.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending data: " + data, e);
            closeConnection();
        }
    }

    public void closeConnection() {
        try {
            if (!connection.isConnected())
                connection.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error closing connection", ex);
        }
    }

    public Socket getConnection() {
        return connection;
    }

    public ObjectOutputStream getOutput() {
        return output;
    }

    public ObjectInputStream getInput() {
        return input;
    }

    @Override
    public String toString() {
        return "Client connected: " + connection.isConnected() +
                ", Address: " + connection.getInetAddress().getHostAddress() +
                ", Port: " + connection.getPort();
    }
}
