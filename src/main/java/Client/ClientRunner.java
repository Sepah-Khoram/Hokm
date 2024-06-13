package Client;

public class ClientRunner {
    public static void main(String[] args) {
        Player player = new Player();
        player.createGame("localhost", 2);
    }
}
