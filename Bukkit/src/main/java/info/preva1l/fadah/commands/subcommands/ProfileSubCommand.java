package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.ProfileMenu;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ProfileSubCommand extends SubCommand {
    public ProfileSubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getProfile().getAliases(), Lang.i().getCommands().getProfile().getDescription());
    }

    @SubCommandArgs(name = "profile", permission = "fadah.profile", async = true)
    public void execute(@NotNull SubCommandArguments command) {
        assert command.getPlayer() != null;
        if (!Config.i().isEnabled()) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getDisabled());
            return;
        }
        OfflinePlayer owner = command.getPlayer();

        if (command.args().length >= 1 && command.sender().hasPermission("fadah.manage.profiles")) {
            owner = Bukkit.getOfflinePlayerIfCached(command.args()[0]);
            if (owner == null) {
                command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getPlayerNotFound()
                        .replace("%player%", command.args()[0]));
                return;
            }
            Fadah.getINSTANCE().loadPlayerData(owner.getUniqueId()).join();
        }

        new ProfileMenu(command.getPlayer(), owner).open(command.getPlayer());
    }
}
