package info.preva1l.fadah.utils.logging;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.trashcan.plugin.annotations.PluginLoad;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;

public interface LoggingProvider {
    @PluginLoad
    static void initLogger() {
        Fadah.getConsole().info("Initialising transaction logger...");

        if (!Config.i().isLogToFile()) return;
        try {
            File logsFolder = new File(Fadah.getInstance().getDataFolder(), "logs");
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
                        .withZone(ZoneId.systemDefault())
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
            Fadah.getInstance().getTransactionLogger().setUseParentHandlers(false);
            for (Handler handler : Fadah.getInstance().getTransactionLogger().getHandlers()) {
                Fadah.getInstance().getTransactionLogger().removeHandler(handler);
            }
            Fadah.getInstance().getTransactionLogger().addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Fadah.getConsole().info("Logger Started!");
    }
}
