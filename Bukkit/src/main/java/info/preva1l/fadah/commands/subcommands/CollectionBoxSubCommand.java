package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.guis.CollectionBoxMenu;
import info.preva1l.fadah.records.CollectionBox;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class CollectionBoxSubCommand extends SubCommand {
    public CollectionBoxSubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getCollectionBox().getAliases(), Lang.i().getCommands().getCollectionBox().getDescription());
    }

    @SubCommandArgs(name = "collection-box", permission = "fadah.collection-box")
    public void execute(@NotNull SubCommandArguments command) {
        if (!Config.i().isEnabled()) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getDisabled());
            return;
        }
        assert command.getPlayer() != null;
        OfflinePlayer owner = command.getPlayer();
        if (command.args().length >= 1 && command.sender().hasPermission("fadah.manage.collection-box")) {
            owner = Bukkit.getOfflinePlayer(command.args()[0]);
            final OfflinePlayer finalOwner = owner;
            DatabaseManager.getInstance().get(CollectionBox.class, finalOwner.getUniqueId())
                    .thenAccept(var1 -> var1.ifPresent(list -> CacheAccess.add(CollectionBox.class, list)));
        }
        if (owner.getUniqueId() != command.getPlayer().getUniqueId() && !HistoricItemsCache.playerExists(owner.getUniqueId())) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getPlayerNotFound()
                    .replace("%player%", command.args()[0]));
            return;
        }
        new CollectionBoxMenu(command.getPlayer(), owner).open(command.getPlayer());
    }
}
