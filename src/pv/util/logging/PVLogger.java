package pv.util.logging;

import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PVLogger {
    private final Logger logger;
    private final String name;

    public PVLogger(String name) {
        this.logger = LoggerFactory.createLogger(name, Level.ALL);
        this.name = name;
    }

    public void info(String msg) {
        logger.log(Level.INFO, msg + "\n");
        System.out.println("\u001B[34m[INFO/" + name + "]\u001B[0m " + msg);
    }

    public void error(String msg) {
        logger.log(Level.SEVERE, msg + "\n");
        System.out.println("\u001B[31m[ERROR/" + name + "]\u001B[0m " + msg);
    }

    public void close() {
        // For every handler in the logger, close it
        Arrays.stream(logger.getHandlers()).forEach(Handler::close);
    }
}