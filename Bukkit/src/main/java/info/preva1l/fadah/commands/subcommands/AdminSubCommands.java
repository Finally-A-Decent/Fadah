package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.guis.FastInvManager;
import org.bukkit.command.CommandSender;

/**
 * Created on 31/03/2025
 *
 * @author Preva1l
 */
public interface AdminSubCommands {
    default void toggle(CommandSender sender, Fadah plugin) {
        if (Broker.getInstance().isConnected()) {
            Message.builder()
                    .type(Message.Type.TOGGLE)
                    .build().send(Broker.getInstance());
        }

        FastInvManager.closeAll();
        boolean enabled = !Config.i().isEnabled();
        Config.i().setEnabled(enabled);

        Lang.Commands.Toggle conf = Lang.i().getCommands().getToggle();
        String toggle = enabled ? conf.getEnabled() : conf.getDisabled();
        sender.sendMessage(Text.text(Lang.i().getPrefix() + conf.getMessage().replace("%status%", toggle)));
    }

    default void reload(CommandSender sender, Fadah plugin) {
        if (Broker.getInstance().isConnected()) {
            Message.builder()
                    .type(Message.Type.RELOAD)
                    .build().send(Broker.getInstance());
        }

        try {
            plugin.reload();
            sender.sendMessage(Text.text(Lang.i().getPrefix() + Lang.i().getCommands().getReload().getSuccess()));
        } catch (Exception e) {
            sender.sendMessage(Text.text(Lang.i().getPrefix() + Lang.i().getCommands().getReload().getFail()));
            throw new RuntimeException(e);
        }
    }
}
