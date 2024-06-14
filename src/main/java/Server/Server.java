package Server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server {
    // logger for logging
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private ServerSocket serverSocket; // server socket to connect with clients
    private final List<Socket> clients; // clients that connected to server
    private final List<Game> games; // games that play in the server
    private final ExecutorService gameExecutor; // will run games in threads

    // constructor
    public Server() {
        try {
            FileHandler fileHandler = new FileHandler("server.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            serverSocket = new ServerSocket(5482);
        }
        catch (IOException ioException)
        {
            System.err.println("Can't create server");
            System.exit(1);
        }

        clients = new CopyOnWriteArrayList<>();
        gameExecutor = Executors.newCachedThreadPool(); // create thread pool
        games = new CopyOnWriteArrayList<>();
    } // end constructor

    public void execute() {
        while (true) {
            Socket connection = null;
            try {
                // create new socket
                connection = serverSocket.accept();

                // add socket to array list
                clients.add(connection);

                // send  a message for shake hands
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeObject(UUID.randomUUID());
                output.flush();

                // get a command of the client
                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                String command = (String) input.readObject();

                if (command.startsWith("create")) {
                    int number = Integer.parseInt(command.substring(6));
                    createNewGame(connection, number); // create new game
                } else if (command.startsWith("join")) {
                    int number = Integer.parseInt(command.substring(4));
                    if (!joinGame(connection, number)) {
                        System.out.println("Game for player " + connection + " Not Found!");
                        output.writeObject("Not Found!");
                        output.flush();
                        closeConnection(connection);
                    }
                } else
                    closeConnection(connection);
            } catch (IOException e) {
                logger.warning("Problem to load streams for client " +
                        connection + ": " + e.getMessage());
                e.printStackTrace();
                closeConnection(connection);
            } catch (ClassNotFoundException e) {
                logger.warning("Illegal object received from client " +
                        connection + ": " + e.getMessage());
                e.printStackTrace();
                closeConnection(connection);
            } catch (Exception e) {
                logger.severe("Unexpected error occurred with client " +
                        connection + ": " + e.getMessage());
                e.printStackTrace();
                if (connection != null) {
                    closeConnection(connection);
                }
            }
        }
    } // end method execute

    public void createNewGame(Socket connection, int numberOfPlayers) {
        try {
            Player player = new Player(connection);
            Game game = new Game(player, numberOfPlayers); // create new game
            games.add(game); // add a game to arraylist
            gameExecutor.execute(game); // assign new thread to this game and execute it

            // information about logging
            logger.info("Created a new game with " + numberOfPlayers + " players."); //
        } catch (SocketException e) {
            logger.warning("SocketException in createNewGame: " + e.getMessage());
            closeConnection(connection); // close connection
        } catch (IOException e) {
            logger.warning("IOException in createNewGame: " + e.getMessage());
            closeConnection(connection); // close connection
        } catch (Exception e) {
            logger.severe("Unexpected error in createNewGame: " + e.getMessage());
            if (connection != null) {
                closeConnection(connection);
            }
        }
    }

    public boolean joinGame(Socket connection, int numberOfPlayers) {
        try {
            Player player = new Player(connection);

            // find a game
            for (Game game : games) {
                if (numberOfPlayers == game.getNumberOfPlayers() && game.isStarted()) {
                    game.addPlayer(player);
                    return true;
                }
            }
        } catch (SocketException e) {
            logger.warning("SocketException in createNewGame: " + e.getMessage());
            closeConnection(connection); // close connection
        } catch (IOException e) {
            logger.warning("IOException in createNewGame: " + e.getMessage());
            closeConnection(connection); // close connection
        } catch (Exception e) {
            logger.severe("Unexpected error in createNewGame: " + e.getMessage());
            if (connection != null) {
                closeConnection(connection);
            }
        }
        return false;
    }

    private void closeConnection (Socket connection) {
        clients.remove(connection); // remove connection from arraylist
        try {
            connection.close(); // close connection
            logger.info("Closed connection with client: " + connection);
        } catch (IOException e) {
            logger.warning("IOException when closing connection: " + e.getMessage());
            System.err.println("Problem to close connection " + connection);
        }
    }
}
