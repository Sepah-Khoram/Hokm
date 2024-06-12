package Client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    public static void joinGame() throws IOException {
        try {
            Socket socket1 = new Socket("localhost", 3737);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket1.getOutputStream());
            objectOutputStream.writeObject("player join the game");
            objectOutputStream.flush();
        } catch (IOException e) {
        }
    }


    public static void createGame() {
        try {
            Socket socket1 = new Socket("localhost", 3737);
            // It is the responsibility of the server to check the matters related to making the game
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket1.getOutputStream());
            objectOutputStream.writeObject("player join the game");
            objectOutputStream.flush();
        } catch (IOException e) {
        }
    }
}
