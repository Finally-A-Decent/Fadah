package info.preva1l.fadah.migrator;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.migrator.impl.AkarianAuctionHouseMigrator;
import info.preva1l.fadah.migrator.impl.AuctionHouseMigrator;
import info.preva1l.fadah.migrator.impl.zAuctionHouseMigrator;
import info.preva1l.trashcan.plugin.annotations.PluginEnable;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MigrationProvider {
    @PluginEnable
    static void loadMigrators() {
        Fadah.getConsole().info("Loading migrators...");

        if (Bukkit.getServer().getPluginManager().getPlugin("zAuctionHouseV3") != null) {
            registerMigrator(new zAuctionHouseMigrator());
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("AuctionHouse") != null) {
            registerMigrator(new AuctionHouseMigrator());
            registerMigrator(new AkarianAuctionHouseMigrator());
        }

        Fadah.getConsole().info("%s Migrators Loaded!".formatted(MigratorHolder.migrators.size()));
    }

    static Optional<Migrator> getMigrator(String migratorName) {
        return Optional.ofNullable(MigratorHolder.migrators.get(migratorName));
    }

    static List<String> getMigratorNames() {
        return MigratorHolder.migrators.keySet().stream().toList();
    }

    private static void registerMigrator(Migrator migrator) {
        MigratorHolder.migrators.put(migrator.getMigratorName(), migrator);
    }

    class MigratorHolder {
        private static final Map<String, Migrator> migrators = new HashMap<>();
    }
}
