package info.preva1l.fadah.utils.logging;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;

public interface LoggingProvider {
    Fadah getPlugin();

    default void initLogger() {
        Fadah.getConsole().info("Initialising transaction logger...");

        if (!Config.i().isLogToFile()) return;
        try {
            File logsFolder = new File(getPlugin().getDataFolder(), "logs");
            if (!logsFolder.exists()) {
                if (!logsFolder.mkdirs()) {
                    Fadah.getConsole().warning("Failed to create logs folder!");
                    return;
                }
            }

            File logFile = new File(logsFolder, "transactions.log");

            String archivedLogNameFormat = "transactions_%s%s";
            if (logFile.exists()) {
                String date = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        .format(Instant.now());

                String newFileName = archivedLogNameFormat.formatted(date, "");

                int i = 0;
                while (new File(logsFolder, newFileName + ".log").exists()) {
                    newFileName = archivedLogNameFormat.formatted(date, "-" + ++i);
                }

                File renamedFile = new File(logsFolder, newFileName + ".log");

                if (!logFile.renameTo(renamedFile)) {
                    Fadah.getConsole().warning("Could not rename logfile!");
                }
            }

            FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath());
            fileHandler.setFormatter(new TransactionLogFormatter());
            getPlugin().getTransactionLogger().setUseParentHandlers(false);
            getPlugin().getTransactionLogger().addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Fadah.getConsole().info("Logger Started!");
    }
}
