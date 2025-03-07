package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.CollectionBoxMenu;
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
        assert command.getPlayer() != null;
        if (!Config.i().isEnabled()) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getDisabled());
            return;
        }
        OfflinePlayer owner = command.getPlayer();

        if (command.args().length >= 1 && command.sender().hasPermission("fadah.manage.collection-box")) {
            owner = Bukkit.getOfflinePlayerIfCached(command.args()[0]);
            if (owner == null) {
                command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getPlayerNotFound()
                        .replace("%player%", command.args()[0]));
                return;
            }
            Fadah.getINSTANCE().loadPlayerData(owner.getUniqueId()).join();
        }

        new CollectionBoxMenu(command.getPlayer(), owner).open(command.getPlayer());
    }
}
