package pv.util.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.*;

public class LoggerFactory {
    private static final Formatter formatter = new Formatter() {
        @Override
        public String format(LogRecord record) {
            return record.getMessage();
        }
    };

    public static Logger createLogger(String name, Level level) {
        Logger logger = Logger.getLogger(name);

        try {
            String path = "logs/%s.log".formatted(name);
            Files.createDirectories(Path.of("logs"));
            // Clear the file before writing to it
            Files.write(Path.of(path), new byte[0]);
            FileHandler fileHandler = new FileHandler(path, true);
            fileHandler.setFormatter(formatter);
            logger.setUseParentHandlers(false);
            logger.addHandler(fileHandler);
            logger.setLevel(level);
            return logger;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}