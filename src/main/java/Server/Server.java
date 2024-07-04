package Server;

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
    private final List<Game> games; // games that play in the server
    private final ExecutorService gameExecutor; // will run games in threads
    private ServerSocket serverSocket; // server socket to connect with clients

    public List<Game> getGames() {
        return games;
    }

    // constructor
    public Server() {
        try {
            serverSocket = new ServerSocket(5482);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating server socket", e);
            System.exit(1);
        }

        clients = new CopyOnWriteArrayList<>();
        games = new CopyOnWriteArrayList<>();
        gameExecutor = Executors.newCachedThreadPool(); // create thread pool
    } // end constructor

    @Override
    public void run() {
        while (true) {
            Client newClient = null;
            try {
                // create new Client
                newClient = new Client(serverSocket.accept());

                // add socket to array list
                clients.add(newClient);

                // send user id for shake hands
                newClient.sendData(UUID.randomUUID());

                // get a command of the client
                ObjectInputStream input = newClient.getInput();
                String command = (String) input.readObject();

                // handle user command
                handleCommand(command, newClient);
            } catch (ClassNotFoundException e) {
                logger.warning("Illegal object received from client " + newClient + ": " +
                        e.getMessage());
                logger.log(Level.WARNING, "ClassNotFoundException", e);
                closeConnection(newClient);
            } catch (Exception e) {
                logger.severe("Unexpected error occurred with client " + newClient + ": " +
                        e.getMessage());
                logger.log(Level.SEVERE, "Exception", e);
                closeConnection(newClient);
            }
        }
    } // end method execute

    private void handleCommand(String command, Client client) {
        try {
            if (command.startsWith("create:")) {
                int number = Integer.parseInt(command.substring(7));
                createNewGame(client, number); // create new game
            } else if (command.startsWith("join random:")) {
                int number = Integer.parseInt(command.substring(12));
                if (!joinGame(client, number)) {
                    System.out.println("Game for player " + client + " Not Found!");
                    // send data to the client
                    client.sendInt(404);
                    closeConnection(client);
                }
            } else if (command.startsWith("join token:")) {
                UUID token = UUID.fromString(command.substring(11));
                if (!joinGame(client, token)) {
                    System.out.println("Game " + token + " Not Found or has started!");
                    // send data to the client
                    client.sendInt(404);
                    closeConnection(client);
                }
            } else if (command.equals("getGames")) {
                // get Games
               // games.forEach();
                for (Game game : games) {
                    int i = game.getNumberOfPlayers();
                    int j = game.getCurrentRound();
                    int k = game.getCurrentSet();
                    game.sendData(j);
                    game.sendData(i);
                    game.sendData(k);
                }
            } else
                closeConnection(client);
        } catch (NumberFormatException e) {
            logger.warning("Invalid command format: " + command);
            closeConnection(client);
        }
    }

    public void createNewGame(Client client, int numberOfPlayers) {
        try {
            Player player = new Player(client);
            Game game = new Game(player, numberOfPlayers); // create new game
            games.add(game); // add a game to arraylist
            gameExecutor.execute(game); // assign new thread to this game and execute it
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
            for (Game game : games) {
                if (game.getToken().equals(token)) {
                    game.addPlayer(player);
                    return true;
                }
            }
        } catch (SocketException e) {
            logger.warning("SocketException in createNewGame: " + e.getMessage());
            closeConnection(client); // close connection
        } catch (IOException e) {
            logger.warning("IOException in createNewGame: " + e.getMessage());
            closeConnection(client); // close connection
        } catch (Exception e) {
            logger.severe("Unexpected error in createNewGame: " + e.getMessage());
            closeConnection(client);
        }
        return false;
    }

    public boolean joinGame(Client client, int numberOfPlayers) {
        try {
            Player player = new Player(client);

            // find a game
            for (Game game : games) {
                if (numberOfPlayers == game.getNumberOfPlayers() && game.hasNotStarted()) {
                    game.addPlayer(player);
                    return true;
                }
            }
        } catch (SocketException e) {
            logger.warning("SocketException in createNewGame: " + e.getMessage());
            closeConnection(client); // close connection
        } catch (IOException e) {
            logger.warning("IOException in createNewGame: " + e.getMessage());
            closeConnection(client); // close connection
        } catch (Exception e) {
            logger.severe("Unexpected error in createNewGame: " + e.getMessage());
            closeConnection(client);
        }
        return false;
    }

    private void closeConnection(Client client) {
        clients.remove(client); // remove connection from arraylist
        client.closeConnection();
    }

    public void showGamedetail(int choosenGame){
        if(choosenGame < games.size()) {
            Game game = games.get(choosenGame);

            if (game.hasNotStarted()) {
                System.out.println("Game has not started yet.");
                return;
            }

            System.out.println("This game have this information :");
            System.out.println("First team win " + game.getWinTeam1() + " sets");
            System.out.println("Second team win " + game.getWinTeam2() + " sets");
            System.out.println("Now game is in set " + game.getCurrentSet() + " and round " +
                    game.getCurrentRound());
        } else {
            System.out.println("Invalid choice!");
        }
    }

    public void massageToGame(String message, int gameNumber) {
        games.get(gameNumber).sendMessage("server massage: " + message);
    }

    public void massageToGame(String message) {
        for (Game game : games)
            game.sendMessage("server massage: " + message);
    }
}
