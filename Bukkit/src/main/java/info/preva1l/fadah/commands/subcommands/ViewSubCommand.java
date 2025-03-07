package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.ViewListingsMenu;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ViewSubCommand extends SubCommand {
    public ViewSubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getView().getAliases(),Lang.i().getCommands().getView().getDescription());
    }

    @SubCommandArgs(name = "view", permission = "fadah.view")
    public void execute(@NotNull SubCommandArguments command) {
        assert command.getPlayer() != null;
        if (!Config.i().isEnabled()) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getDisabled());
            return;
        }
        if (command.args().length == 0) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getInvalidUsage()
                    .replace("%command%", Lang.i().getCommands().getView().getUsage()));
            return;
        }
        OfflinePlayer owner = Bukkit.getOfflinePlayerIfCached(command.args()[0]);
        if (owner == null) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getPlayerNotFound()
                    .replace("%player%", command.args()[0]));
            return;
        }
        new ViewListingsMenu(command.getPlayer(), owner).open(command.getPlayer());
    }
}
