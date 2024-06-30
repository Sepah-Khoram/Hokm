package Server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerRunner {
    private final ExecutorService mainExecutor;
    Server server = new Server();
    ServerManager serverManager = new ServerManager(server);

    public ServerRunner(ExecutorService mainExecutor) {
        this.mainExecutor = mainExecutor;
        mainExecutor = Executors.newFixedThreadPool(2);
    mainExecutor.execute(server);
    mainExecutor.execute(serverManager);
    mainExecutor.shutdown();

    }


    public static void main(String[] args) {
        Server application = new Server();
        application.execute();
    }
}
