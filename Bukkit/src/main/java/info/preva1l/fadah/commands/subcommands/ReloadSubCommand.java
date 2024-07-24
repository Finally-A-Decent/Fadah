package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.jetbrains.annotations.NotNull;

public class ReloadSubCommand extends SubCommand {
    public ReloadSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "reload", aliases = {"rl"}, permission = "fadah.reload", inGameOnly = false, description = "Reloads the plugin!")
    public void execute(@NotNull SubCommandArguments command) {
        if (Fadah.getINSTANCE().getCacheSync() != null) {
            CacheSync.send(CacheSync.CacheType.RELOAD);
            return;
        }
        plugin.reload();
        command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + "&aConfig reloaded!"));
    }
}
