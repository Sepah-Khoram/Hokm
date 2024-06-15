package Server;

import java.io.IOException;
import java.util.logging.*;

public class LoggerManager {
    private static final Logger logger = Logger.getLogger(LoggerManager.class.getName());

    static {
        try {
            // Setting up file handler for logging
            FileHandler fileHandler = new FileHandler("server.log", true); // true for append mode
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Setting up console handler for logging
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

            // Set logger level
            logger.setLevel(Level.INFO); // You can adjust the logging level as needed
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up logger", e);
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
