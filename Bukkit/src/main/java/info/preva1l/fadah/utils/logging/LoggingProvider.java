package info.preva1l.fadah.utils.logging;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public interface LoggingProvider {
    Logger getTransactionLogger();

    default void initLogger(Fadah plugin) {
        Fadah.getConsole().info("Initialising transaction logger...");

        if (!Config.i().isLogToFile()) {
            return;
        }
        try {
            File logsFolder = new File(plugin.getDataFolder(), "logs");
            if (!logsFolder.exists()) {
                if (!logsFolder.mkdirs()) {
                    Fadah.getConsole().warning("Failed to create logs folder!");
                    return;
                }
            }

            File logFile = new File(logsFolder, "transaction-log.log");
            if (logFile.exists()) {
                long epochMillis = System.currentTimeMillis();
                String newFileName = "transaction-log_" + epochMillis + ".log";
                File renamedFile = new File(logsFolder, newFileName);
                if (!logFile.renameTo(renamedFile)) {
                    Fadah.getConsole().warning("Could not rename logfile!");
                }
            }

            FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath());
            fileHandler.setFormatter(new TransactionLogFormatter());
            getTransactionLogger().setUseParentHandlers(false);
            getTransactionLogger().addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Fadah.getConsole().info("Logger Started!");
    }
}
