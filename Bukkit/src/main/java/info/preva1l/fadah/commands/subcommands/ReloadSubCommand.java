package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.jetbrains.annotations.NotNull;

public class ReloadSubCommand extends SubCommand {
    public ReloadSubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getReload().getAliases(), Lang.i().getCommands().getReload().getDescription());
    }

    @SubCommandArgs(name = "reload", permission = "fadah.reload", inGameOnly = false)
    public void execute(@NotNull SubCommandArguments command) {
        if (Broker.getInstance().isConnected()) {
            Message.builder()
                    .type(Message.Type.RELOAD)
                    .build().send(Broker.getInstance());
            return;
        }
        try {
            plugin.reload();
            command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getReload().getSuccess());
        } catch (Exception e) {
            command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getReload().getFail());
            throw new RuntimeException(e);
        }
    }
}
