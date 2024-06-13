package Server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket; // server socket to connect with clients
    private final ArrayList<Socket> clients; // clients that connected to server
    private final ArrayList<Game> games; // games that play in the server
    private final ExecutorService executorService; // will run games in threads

    // constructor
    public Server() {
        try
        {
            // set up ServerSocket
            serverSocket = new ServerSocket(5482, 4);
        }
        catch (IOException ioException)
        {
            System.out.println("Can't create server");
            System.exit(1);
        }

        clients = new ArrayList<>();
        executorService = Executors.newCachedThreadPool(); // create thread pool
        games = new ArrayList<>();
    } // end constructor

    public void execute() {
        // scanner and formatter for shake hands
        ObjectInputStream input;
        ObjectOutputStream output;

        Socket connection = null; // new socket to get a new connection
        String command = null; // command of clients

        while (true) {
            try {
                // create new socket
                connection = serverSocket.accept();

                // add socket to array list
                clients.add(connection);

                output = new ObjectOutputStream(connection.getOutputStream());
                output.writeObject("Accept");
                output.flush();

                input = new ObjectInputStream(connection.getInputStream());
                command = (String) input.readObject();
            } catch (IOException e) {
                System.out.println("Problem to load streams for client " + connection);
                closeConnection(connection);
                continue;
            } catch (ClassNotFoundException e) {
                System.out.println("Illegal object. Terminating connection " +
                        connection);
                closeConnection(connection);
            }

            if (command.startsWith("create")) {
                int number = Integer.parseInt(command.substring(6));
                createNewGame(connection, number); // create new game
            } else if (command.startsWith("join")) {

            } else
                closeConnection(connection);
        }
    } // end method execute

    public void createNewGame(Socket connection, int numberOfPlayers) {
        Player player = null; // create player to create new game

        try {
            player = new Player(connection);
        } catch (SocketException e) {
            System.out.println(e.getMessage());
            closeConnection(connection); // close connection
            return;
        } catch (IOException e) {
            System.out.println("Problem to load streams for client " + connection);
            closeConnection(connection); // close connection
            return;
        }

        Game game = new Game(player, numberOfPlayers); // create new game
        games.add(game); // add a game to arraylist

        executorService.execute(game); // assign new thread to this game and execute it
    }

    private void closeConnection (Socket connection){
        clients.remove(connection); // remove connection from arraylist

        try {
            connection.close(); // close connection
        } catch (IOException e) {
            System.out.println("Problem to close connection " + connection);
        }
    }
}
