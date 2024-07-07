package Server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerRunner {
    private static final Logger logger = LoggerManager.getLogger();

    public static void main(String[] args) {
        Server application = new Server();
        ServerManager manager = new ServerManager(application);
        ExecutorService mainExecutor = Executors.newFixedThreadPool(2);

        try {
            logger.info("Starting server application and manager.");
            mainExecutor.execute(application);
            mainExecutor.execute(manager);

            mainExecutor.shutdown();
            logger.info("Main executor shut down.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred in ServerRunner", e);
        } finally {
            if (!mainExecutor.isTerminated()) {
                mainExecutor.shutdownNow();
                logger.warning("Main executor was forcefully shut down.");
            }
        }
    }
}
