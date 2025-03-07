package info.preva1l.fadah.utils.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.utils.CommandMapUtil;
import info.preva1l.fadah.utils.TaskManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class Command {
    public Fadah plugin;
    private CommandExecutor executor;
    private CommandArgs assigned;
    private boolean senderHasPermission = false;
    private final List<String> aliases;

    public Command(Fadah plugin, List<String> aliases) {
        this.plugin = plugin;
        this.aliases = aliases;
        this.register();
    }

    public void register() {
        this.assigned = Arrays.stream(this.getClass().getMethods()).filter(method -> method.getAnnotation(CommandArgs.class) != null).map(method -> method.getAnnotation(CommandArgs.class)).findFirst().orElse(null);

        if (assigned != null) {
            this.executor = new CommandExecutor(assigned.name(), assigned, this.aliases);
            CommandMapUtil.getCommandMap(plugin.getServer());
            CommandMapUtil.getCommandMap(plugin.getServer()).register(plugin.getDescription().getName().toLowerCase(), executor);
        }
    }

    public abstract void execute(@NotNull CommandArguments command);

    public List<String> onTabComplete(CommandArguments command) {
        return new ArrayList<>();
    }

    //This is default tab complete which should return a list of online players
    public List<String> getDefaultTabComplete(CommandArguments command) {
        List<String> completors = new ArrayList<>();

        List<String> values = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList();

        String[] args = command.args();

        if (args.length == 0) return new ArrayList<>();

        if (!args[args.length - 1].equalsIgnoreCase("")) {
            values.forEach(value -> {
                if (value.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                    completors.add(value);
                }
            });
        } else {
            completors.addAll(values);
        }
        return completors;
    }

    public List<String> subCommandsTabCompleter(CommandArguments command, List<SubCommand> subCommands) {
        List<String> completions = new ArrayList<>();
        List<String> ret = new ArrayList<>();
        if (command.args().length != 1) return completions;
        for (SubCommand subCommand : subCommands) {
            if (!command.sender().hasPermission(subCommand.getAssigned().permission())) continue;
            ret.add(subCommand.getAssigned().name());
            ret.addAll(subCommand.getAliases());
        }
        StringUtil.copyPartialMatches(command.args()[0], ret, completions);
        return completions;
    }

    public boolean subCommandExecutor(@NotNull CommandArguments command, List<SubCommand> subCommands2) {
        for (SubCommand subCommand : subCommands2) {
            if (command.args()[0].equalsIgnoreCase(subCommand.getAssigned().name()) || subCommand.getAliases().contains(command.args()[0])) {
                subCommand.executor(command.sender(), command.label(), command.args());
                return true;
            }
        }
        return false;
    }

    public class CommandExecutor extends BukkitCommand {
        private final boolean inGameOnly;
        private final boolean async;
        private CommandArguments executeArguments;

        public CommandExecutor(String name, CommandArgs assigned, List<String> aliases) {
            super(name);
            this.setAliases(aliases);
            this.setPermission(assigned.permission());
            this.inGameOnly = assigned.inGameOnly();
            this.async = assigned.async();
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            if (this.inGameOnly && sender instanceof ConsoleCommandSender) {
                Lang.sendMessage(sender, Lang.i().getPrefix() + Lang.i().getErrors().getMustBePlayer());
                return false;
            }
            if (this.getPermission() != null && !sender.hasPermission(this.getPermission())) {
                senderHasPermission = false;
                Lang.sendMessage(sender, Lang.i().getPrefix() + Lang.i().getErrors().getNoPermission());
                return false;
            }

            this.executeArguments = new CommandArguments(sender, label, args);

            if (this.async) {
                TaskManager.Async.run(plugin, () -> Command.this.execute(executeArguments));
            } else {
                Command.this.execute(executeArguments);
            }
            return false;
        }

        @NotNull
        @Override
        public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
            List<String> completors = onTabComplete(new CommandArguments(sender, null, args));

            if (completors.isEmpty()) {
                completors.addAll(getDefaultTabComplete(new CommandArguments(sender, null, args)));
            }
            return completors;
        }
    }
}