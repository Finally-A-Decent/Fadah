package info.preva1l.fadah.migrator;

import info.preva1l.fadah.Fadah;
import org.bukkit.Bukkit;

public interface MigrationProvider {
    MigratorManager getMigrationManager();
    void setMigrationManager(MigratorManager migratorManager);

    default void loadMigrators() {
        Fadah.getConsole().info("Loading migrators...");

        setMigrationManager(new MigratorManager());

        if (Bukkit.getServer().getPluginManager().getPlugin("zAuctionHouseV3") != null) {
            getMigrationManager().loadMigrator(new zAuctionHouseMigrator());
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("AuctionHouse") != null) {
            getMigrationManager().loadMigrator(new AuctionHouseMigrator());
        }

        Fadah.getConsole().info("%s Migrators Loaded!".formatted(getMigrationManager().getMigratorNames().size()));
    }
}
