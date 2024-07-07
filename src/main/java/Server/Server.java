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
        gameExecutor = Executors.newCachedThreadPool(); // create thread pool
    } // end constructor

    @Override
    public void run() {
        while (true) {
            Client newClient = null;
            try {
                // create new Client
                newClient = new Client(serverSocket.accept());
                logger.info("New client connected: " + newClient);

                // add socket to array list
                clients.add(newClient);

                // send user id for handshake
                newClient.sendData(UUID.randomUUID());

                // get a command of the client
                ObjectInputStream input = newClient.getInput();
                String command = (String) input.readObject();

                // handle user command
                handleCommand(command, newClient);
            } catch (ClassNotFoundException e) {
                logger.warning("Illegal object received from client " + newClient + ": " + e.getMessage());
                logger.log(Level.WARNING, "ClassNotFoundException", e);
                closeConnection(newClient);
            } catch (IOException e) {
                logger.severe("I/O error occurred with client " + newClient + ": " + e.getMessage());
                logger.log(Level.SEVERE, "IOException", e);
                closeConnection(newClient);
            } catch (Exception e) {
                logger.severe("Unexpected error occurred with client " + newClient + ": " + e.getMessage());
                logger.log(Level.SEVERE, "Exception", e);
                closeConnection(newClient);
            }
        }
    } // end method execute

    private void handleCommand(String command, Client client) {
        try {
            if (command.startsWith("create:")) {
                int number = Integer.parseInt(command.substring(7, 8));
                if (command.substring(8).equals("Private")){
                    createNewGame(client, number, GameType.Private); // create new game
                } else {
                    createNewGame(client, number, GameType.Public); // create new game
                }
            } else if (command.startsWith("join random:")) {
                int number = Integer.parseInt(command.substring(12));
                if (joinGame(client, number)) {
                    logger.warning("Game for player " + client + " Not Found!");
                    // send data to the client
                    client.sendInt(404);
                    closeConnection(client);
                }
            } else if (command.startsWith("join token:")) {
                UUID token = UUID.fromString(command.substring(11));
                if (joinGame(client, token)) {
                    logger.warning("Game " + token + " Not Found or has started!");
                    // send data to the client
                    client.sendInt(404);
                    closeConnection(client);
                }
            } else if (command.equals("getGames")) {
                logger.info("Sending game details to client: " + client);
                for (Game game : publicGames) {
                    int numberOfPlayers = game.getNumberOfPlayers();
                    int currentRound = game.getCurrentRound();
                    int currentSet = game.getCurrentSet();
                    game.sendData(currentRound);
                    game.sendData(numberOfPlayers);
                    game.sendData(currentSet);
                }
            } else {
                logger.warning("Invalid command received from client: " + command);
                closeConnection(client);
            }
        } catch (NumberFormatException e) {
            logger.warning("Invalid command format: " + command);
            closeConnection(client);
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
