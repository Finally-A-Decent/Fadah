package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import info.preva1l.fadah.utils.guis.FastInvManager;
import org.jetbrains.annotations.NotNull;

public class ToggleSubCommand extends SubCommand {
    public ToggleSubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getToggle().getAliases(), Lang.i().getCommands().getToggle().getDescription());
    }

    @SubCommandArgs(name = "toggle", inGameOnly = false, permission = "fadah.toggle-status")
    public void execute(@NotNull SubCommandArguments command) {
        if (Broker.getInstance().isConnected()) {
            Message.builder()
                    .type(Message.Type.TOGGLE)
                    .build().send(Broker.getInstance());
        }
        FastInvManager.closeAll(plugin);
        boolean enabled = Config.i().isEnabled();
        Config.i().setEnabled(!enabled);

        Lang.Commands.Toggle conf = Lang.i().getCommands().getToggle();
        String toggle = enabled ? conf.getDisabled() : conf.getEnabled();
        command.reply(Lang.i().getPrefix() + conf.getMessage().replace("%status%", toggle));
    }
}
