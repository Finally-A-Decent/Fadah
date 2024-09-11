package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.old.Lang;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import info.preva1l.fadah.utils.guis.FastInvManager;
import org.jetbrains.annotations.NotNull;

public class ToggleSubCommand extends SubCommand {
    public ToggleSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "toggle", inGameOnly = false, permission = "fadah.toggle-status", description = "Toggles the auction house on or off.")
    public void execute(@NotNull SubCommandArguments command) {
        if (Fadah.getINSTANCE().getBroker() != null) {
            Message.builder().type(Message.Type.TOGGLE).build().send(Fadah.getINSTANCE().getBroker());
            return;
        }
        FastInvManager.closeAll(plugin);
        boolean enabled = Config.i().isEnabled();
        Config.i().setEnabled(!enabled);

        String toggle = enabled ? Lang.ADMIN_TOGGLE_DISABLED.toFormattedString() : Lang.ADMIN_TOGGLE_ENABLED.toFormattedString();
        command.sender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.ADMIN_TOGGLE_MESSAGE.toFormattedString(toggle));
    }
}
