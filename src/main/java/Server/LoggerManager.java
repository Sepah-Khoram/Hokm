package Server;

import java.io.IOException;
import java.util.logging.*;

public class LoggerManager {
    private static final Logger logger = Logger.getLogger(LoggerManager.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("server.log", true); // true for append mode
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);


            logger.setUseParentHandlers(false);

            logger.setLevel(Level.INFO);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up logger", e);
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
