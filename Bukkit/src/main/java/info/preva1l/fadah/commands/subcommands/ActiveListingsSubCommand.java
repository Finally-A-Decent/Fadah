package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.ActiveListingsMenu;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ActiveListingsSubCommand extends SubCommand {
    public ActiveListingsSubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getActiveListings().getAliases(), Lang.i().getCommands().getActiveListings().getDescription());
    }

    @SubCommandArgs(name = "active-listings", permission = "fadah.active-listings")
    public void execute(@NotNull SubCommandArguments command) {
        if (!Config.i().isEnabled()) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getDisabled());
            return;
        }
        assert command.getPlayer() != null;
        OfflinePlayer owner = command.getPlayer();
        if (command.args().length >= 1 && command.sender().hasPermission("fadah.manage.listings")) {
            owner = Bukkit.getOfflinePlayer(command.args()[0]);
        }
        if (owner.getUniqueId() != command.getPlayer().getUniqueId() && !HistoricItemsCache.playerExists(owner.getUniqueId())) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getPlayerNotFound()
                    .replace("%player%", command.args()[0]));
            return;
        }
        new ActiveListingsMenu(command.getPlayer(), owner).open(command.getPlayer());
    }
}