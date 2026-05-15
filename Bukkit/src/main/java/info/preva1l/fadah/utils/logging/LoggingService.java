package info.preva1l.fadah.utils.logging;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class LoggingService {
    public static final LoggingService instance = new LoggingService();

    private Fadah plugin;
    private Logger logger;
    
    final Logger transactionLogger = Logger.getLogger("AuctionHouse-Transactions");

    public LoggingService init(Fadah plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        return this;
    }

    public void initLogger() {
        if (!Config.i().isLogToFile()) return;
        try {
            File logsFolder = new File(plugin.getDataFolder(), "logs");
            if (!logsFolder.exists()) {
                if (!logsFolder.mkdirs()) {
                    logger.warning("Failed to create logs folder!");
                    return;
                }
            }

            File logFile = new File(logsFolder, "transactions.log");

            String archivedLogNameFormat = "transactions_%s%s";
            if (logFile.exists()) {
                String date = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.now());

                String newFileName = archivedLogNameFormat.formatted(date, "");

                int i = 0;
                while (new File(logsFolder, newFileName + ".log").exists()) {
                    newFileName = archivedLogNameFormat.formatted(date, "-" + ++i);
                }

                File renamedFile = new File(logsFolder, newFileName + ".log");

                if (!logFile.renameTo(renamedFile)) {
                    logger.warning("Could not rename logfile!");
                }
            }

            FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath());
            fileHandler.setFormatter(new TransactionLogFormatter());
            transactionLogger.setUseParentHandlers(false);
            for (Handler handler : transactionLogger.getHandlers()) {
                transactionLogger.removeHandler(handler);
            }
            transactionLogger.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
