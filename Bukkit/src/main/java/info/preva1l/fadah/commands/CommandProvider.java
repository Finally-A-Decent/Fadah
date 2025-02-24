package info.preva1l.fadah.commands;

import info.preva1l.fadah.Fadah;

public interface CommandProvider {
    default void loadCommands(Fadah plugin) {
        Fadah.getConsole().info("Loading commands...");
        new AuctionHouseCommand(plugin);
        new MigrateCommand(plugin);
        Fadah.getConsole().info("Commands Loaded!");
    }
}
