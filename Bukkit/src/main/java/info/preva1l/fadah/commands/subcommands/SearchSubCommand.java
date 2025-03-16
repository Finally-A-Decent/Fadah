package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.MainMenu;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.jetbrains.annotations.NotNull;

public class SearchSubCommand extends SubCommand {
    public SearchSubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getSearch().getAliases(), Lang.i().getCommands().getSearch().getDescription());
    }

    @SubCommandArgs(name = "search", permission = "fadah.search")
    public void execute(@NotNull SubCommandArguments command) {
        assert command.getPlayer() != null;
        if (!Config.i().isEnabled()) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getDisabled());
            return;
        }
        if (command.args().length == 0) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getInvalidUsage()
                    .replace("%command%", Lang.i().getCommands().getSearch().getUsage()));
            return;
        }
        new MainMenu(null, command.getPlayer(), String.join(" ", command.args()), null, null).open(command.getPlayer());
    }
}
