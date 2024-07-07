package Server;

import Utilities.GameType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {
    // Using LoggerManager to get the logger
    private static final Logger logger = LoggerManager.getLogger();

    private Socket connection;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private final Server server;

    public Client(Socket connection, Server server) {
        this.server = server;
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

    protected Client(Client client) {
        connection = client.getConnection();
        output = client.getOutput();
        input = client.getInput();
        this.server = client.getServer();
    }

    @Override
    public String toString() {
        return "Client connected: " + connection.isConnected() +
                ", Address: " + connection.getInetAddress().getHostAddress() +
                ", Port: " + connection.getPort();
    }

    @Override
    public void run() {
        try {
            // send user id for handshake
            sendData(UUID.randomUUID());

            // get a command of the client
            String command = (String) input.readObject();

            // handle user command
            handleCommand(command);
        } catch (ClassNotFoundException e) {
            logger.warning("Illegal object received from client: " + e.getMessage());
            logger.log(Level.WARNING, "ClassNotFoundException", e);
            closeConnection();
        } catch (IOException e) {
            logger.severe("I/O error occurred with client: " + e.getMessage());
            logger.log(Level.SEVERE, "IOException", e);
            closeConnection();
        } catch (Exception e) {
            logger.severe("Unexpected error occurred with client: " + e.getMessage());
            logger.log(Level.SEVERE, "Exception", e);
            closeConnection();
        }
    }

    private void handleCommand(String command) {
        try {
            if (command.startsWith("create:")) {
                int number = Integer.parseInt(command.substring(7, 8));
                if (command.substring(8).equals("Private")) {
                    server.createNewGame(this, number, GameType.Private); // create new game
                } else {
                    server.createNewGame(this, number, GameType.Public); // create new game
                }
            } else if (command.startsWith("join random:")) {
                int number = Integer.parseInt(command.substring(12));
                if (!server.joinGame(this, number)) {
                    logger.warning("Game for player not found!");
                    // send data to the client
                    sendInt(404);
                    closeConnection();
                }
            } else if (command.startsWith("join token:")) {
                UUID token = UUID.fromString(command.substring(11));
                if (server.joinGame(this, token)) {
                    logger.warning("Game " + token + " not found or has started!");
                    // send data to the client
                    sendInt(404);
                    closeConnection();
                }
            } else if (command.equals("getGames")) {
                logger.info("Sending game details to client");

                sendInt(server.getPublicGames().size());
                for (Game game : server.getPublicGames()) {
                    // format of the games to send:
                    // token, number of players, connected Players
                    sendData(game.getToken());
                    sendInt(game.getNumberOfPlayers());
                    sendInt(game.getconnectedPlayer());
                }
            } else {
                logger.warning("Invalid command received from client: " + command);
                closeConnection();
            }
        } catch (NumberFormatException e) {
            logger.warning("Invalid command format: " + command);
            closeConnection();
        }
    }

    public synchronized void sendInt(int number) {
        try {
            output.writeInt(number);
            output.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending integer: " + number, e);
            closeConnection();
        }
    }

    public synchronized void sendData(Object data) {
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

    public Server getServer() {
        return server;
    }
}
