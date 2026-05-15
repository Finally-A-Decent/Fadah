package info.preva1l.fadah.migrator;

import info.preva1l.fadah.migrator.impl.AkarianAuctionHouseMigrator;
import info.preva1l.fadah.migrator.impl.AuctionHouseMigrator;
import info.preva1l.fadah.migrator.impl.zAuctionHouseMigrator;
import info.preva1l.fadah.Fadah;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public final class MigrationService {
    public static final MigrationService instance = new MigrationService();

    public Logger logger;

    private static final Map<String, Migrator> migrators = new HashMap<>();

    public MigrationService init(Fadah plugin) {
        this.logger = plugin.getLogger();
        return this;
    }

    public void loadMigrators() {
        logger.info("Loading migrators...");

        if (Bukkit.getServer().getPluginManager().getPlugin("zAuctionHouseV3") != null) {
            registerMigrator(new zAuctionHouseMigrator());
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("AuctionHouse") != null) {
            registerMigrator(new AuctionHouseMigrator());
            registerMigrator(new AkarianAuctionHouseMigrator());
        }

        logger.info("%s Migrators Loaded!".formatted(migrators.size()));
    }

    public Optional<Migrator> getMigrator(String migratorName) {
        return Optional.ofNullable(migrators.get(migratorName));
    }

    public List<String> getMigratorNames() {
        return migrators.keySet().stream().toList();
    }

    private void registerMigrator(Migrator migrator) {
        migrators.put(migrator.getMigratorName(), migrator);
    }
}
