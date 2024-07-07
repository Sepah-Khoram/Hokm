package Server;

import Utilities.GameType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {
    private static final Logger logger = LoggerManager.getLogger();
    private final List<Client> clients; // clients that connected to server
    private final List<Game> publicGames; // public games that play in the server
    private final List<Game> privateGames; // private games that play in the server
    private final ExecutorService gameExecutor; // will run games in threads
    private final ExecutorService clientHandler;
    private ServerSocket serverSocket; // server socket to connect with clients

    // constructor
    public Server() {
        try {
            serverSocket = new ServerSocket(5482);
            logger.info("Server socket created successfully on port 5482.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating server socket", e);
            System.exit(1);
        }

        clients = new CopyOnWriteArrayList<>();
        publicGames = new CopyOnWriteArrayList<>();
        privateGames = new CopyOnWriteArrayList<>();
        gameExecutor = Executors.newCachedThreadPool();
        clientHandler = Executors.newCachedThreadPool();
    } // end constructor

    @Override
    public void run() {
        while (true) {
            try {
                // create new Client
                Client newClient = new Client(serverSocket.accept(), this);
                logger.info("New client connected: " + newClient);

                // add socket to array list
                clients.add(newClient);

                // handle client in a new thread
                clientHandler.execute(newClient);
            } catch (IOException e) {
                logger.severe("I/O error occurred while accepting client: " + e.getMessage());
                logger.log(Level.SEVERE, "IOException", e);
            }
        }
    }

    public void createNewGame(Client client, int numberOfPlayers, GameType gameType) {
        try {
            Player player = new Player(client);
            Game game = new Game(player, numberOfPlayers, gameType); // create new game

            // add a game to arraylist
            if (game.getGameType() == GameType.Private)
                privateGames.add(game);
            else
                publicGames.add(game);

            gameExecutor.execute(game); // assign new thread to this game and execute it
            logger.info("New game created with " + numberOfPlayers + " players.");
        } catch (SocketException e) {
            logger.warning("SocketException in createNewGame: " + e.getMessage());
            closeConnection(client); // close client
        } catch (IOException e) {
            logger.warning("IOException in createNewGame: " + e.getMessage());
            closeConnection(client); // close client
        } catch (Exception e) {
            logger.severe("Unexpected error in createNewGame: " + e.getMessage());
            if (client != null) {
                closeConnection(client);
            }
        }
    }

    public boolean joinGame(Client client, UUID token) {
        try {
            Player player = new Player(client);
            for (Game game : privateGames) {
                if (game.getToken().equals(token)) {
                    game.addPlayer(player);
                    logger.info("Player " + client + " joined game " + token);
                    return false;
                }
            }
        } catch (SocketException e) {
            logger.warning("SocketException in joinGame: " + e.getMessage());
            closeConnection(client); // close connection
        } catch (IOException e) {
            logger.warning("IOException in joinGame: " + e.getMessage());
            closeConnection(client); // close connection
        } catch (Exception e) {
            logger.severe("Unexpected error in joinGame: " + e.getMessage());
            closeConnection(client);
        }
        return true;
    }

    public boolean joinGame(Client client, int numberOfPlayers) {
        try {
            Player player = new Player(client);

            // find a game
            for (Game game : publicGames) {
                if (numberOfPlayers == game.getNumberOfPlayers() && game.hasNotStarted()) {
                    game.addPlayer(player);
                    logger.info("Player " + client + " joined a random game with " + numberOfPlayers + " players.");
                    return true;
                }
            }
        } catch (SocketException e) {
            logger.warning("SocketException in joinGame: " + e.getMessage());
            closeConnection(client); // close connection
        } catch (IOException e) {
            logger.warning("IOException in joinGame: " + e.getMessage());
            closeConnection(client); // close connection
        } catch (Exception e) {
            logger.severe("Unexpected error in joinGame: " + e.getMessage());
            closeConnection(client);
        }
        return false;
    }

    private void closeConnection(Client client) {
            clients.remove(client); // remove connection from arraylist
            client.closeConnection();
            logger.info("Connection closed with client: " + client);
    }

    public void showGameDetail(int choosenGame){
        if(choosenGame < publicGames.size()) {
            Game game = publicGames.get(choosenGame);

            if (game.hasNotStarted()) {
                logger.info("Game has not started yet.");
                return;
            }

            logger.info("Game details for game " + choosenGame + ":");
            logger.info("First team win " + game.getWinTeam1() + " sets");
            logger.info("Second team win " + game.getWinTeam2() + " sets");
            logger.info("Now game is in set " + game.getCurrentSet() + " and round " + game.getCurrentRound());
        } else {
            logger.warning("Invalid game choice: " + choosenGame);
        }
    }

    public void sendMessage(String message, int gameNumber) {
        publicGames.get(gameNumber).sendMessage("server massage: " + message);
        logger.info("Message sent to game " + gameNumber + ": " + message);
    }

    public void sendGlobalMessage(String message) {
        for (Game game : publicGames) {
            game.sendMessage("server message: " + message);
        }
        logger.info("Message sent to all games: " + message);
    }

    public List<Game> getPublicGames() {
        return publicGames;
    }
}
