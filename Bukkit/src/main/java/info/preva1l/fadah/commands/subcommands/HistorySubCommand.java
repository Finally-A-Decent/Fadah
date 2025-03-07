package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.HistoryMenu;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class HistorySubCommand extends SubCommand {
    public HistorySubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getHistory().getAliases(), Lang.i().getCommands().getHistory().getDescription());
    }

    @SubCommandArgs(name = "history", permission = "fadah.history")
    public void execute(@NotNull SubCommandArguments command) {
        assert command.getPlayer() != null;
        if (!Config.i().isEnabled()) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getDisabled());
            return;
        }
        OfflinePlayer owner = command.getPlayer();

        if (command.args().length >= 1 && command.sender().hasPermission("fadah.manage.history")) {
            owner = Bukkit.getOfflinePlayerIfCached(command.args()[0]);
            if (owner == null) {
                command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getPlayerNotFound()
                        .replace("%player%", command.args()[0]));
                return;
            }
            Fadah.getINSTANCE().loadPlayerData(owner.getUniqueId()).join();
        }

        new HistoryMenu(command.getPlayer(), owner, null).open(command.getPlayer());
    }
}
