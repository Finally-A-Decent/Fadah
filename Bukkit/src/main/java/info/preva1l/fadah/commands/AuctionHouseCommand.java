package info.preva1l.fadah.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.commands.subcommands.*;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.MainMenu;
import info.preva1l.fadah.utils.commands.Command;
import info.preva1l.fadah.utils.commands.CommandArgs;
import info.preva1l.fadah.utils.commands.CommandArguments;
import info.preva1l.fadah.utils.commands.SubCommand;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AuctionHouseCommand extends Command {
    @Getter
    private static final List<SubCommand> subCommands = new ArrayList<>();

    public AuctionHouseCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getMain().getAliases());
        subCommands.add(new ReloadSubCommand(plugin));
        subCommands.add(new SellSubCommand(plugin));
        subCommands.add(new ProfileSubCommand(plugin));
        subCommands.add(new CollectionBoxSubCommand(plugin));
        subCommands.add(new ToggleSubCommand(plugin));
        subCommands.add(new ExpiredItemsSubCommand(plugin));
        subCommands.add(new HelpSubCommand(plugin));
        subCommands.add(new ActiveListingsSubCommand(plugin));
        subCommands.add(new HistorySubCommand(plugin));
        subCommands.add(new ViewListingCommand(plugin));
        subCommands.add(new AboutSubCommand(plugin));
        subCommands.add(new ViewSubCommand(plugin));
        subCommands.add(new WatchSubCommand(plugin));
        subCommands.add(new SearchSubCommand(plugin));
    }

    @CommandArgs(name = "fadah", inGameOnly = false, permission = "fadah.use")
    public void execute(@NotNull CommandArguments command) {
        if (command.args().length >= 1) {
            if (subCommandExecutor(command, subCommands)) return;
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getCommandNotFound());
            return;
        }
        if (!Config.i().isEnabled()) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getDisabled());
            return;
        }
        if (command.getPlayer() == null) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getMustBePlayer());
            return;
        }

        new MainMenu(null, command.getPlayer(), null, null, null).open(command.getPlayer());
    }

    @Override
    public List<String> onTabComplete(CommandArguments command) {
        return subCommandsTabCompleter(command, subCommands);
    }
}
