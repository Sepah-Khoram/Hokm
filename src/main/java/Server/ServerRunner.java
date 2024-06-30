package Server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerRunner {

    public static void main(String[] args) {
        Server application = new Server();
        ServerManager manager = new ServerManager(application);
        ExecutorService mainExecutor = Executors.newFixedThreadPool(2);

        mainExecutor.execute(application);
        mainExecutor.execute(manager);

        mainExecutor.shutdown();
    }
}
