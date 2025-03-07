package info.preva1l.fadah.utils.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.utils.TaskManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class SubCommand {
    private final SubCommandArgs assigned;
    public Fadah plugin;
    private SubCommandArguments executeArguments;
    private boolean senderHasPermission = false;
    private final List<String> aliases;
    private final String description;

    public SubCommand(Fadah plugin, List<String> aliases, String description) {
        this.plugin = plugin;
        this.description = description;
        this.aliases = aliases;
        this.assigned = Arrays.stream(this.getClass().getMethods()).filter(method -> method.getAnnotation(SubCommandArgs.class) != null).map(method -> method.getAnnotation(SubCommandArgs.class)).findFirst().orElse(null);
    }

    public abstract void execute(@NotNull SubCommandArguments command);

    public final void executor(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (this.assigned.inGameOnly() && sender instanceof ConsoleCommandSender) {
            Lang.sendMessage(sender, Lang.i().getPrefix() + Lang.i().getErrors().getMustBePlayer());
            return;
        }
        if (this.assigned.permission() != null && !sender.hasPermission(this.assigned.permission())) {
            senderHasPermission = false;
            Lang.sendMessage(sender, Lang.i().getPrefix() + Lang.i().getErrors().getNoPermission());
            return;
        }

        if (args.length == 0) {
            this.executeArguments = new SubCommandArguments(sender, label, new String[0], this.assigned.async(), this.assigned.inGameOnly());
        } else {
            this.executeArguments = new SubCommandArguments(sender, label, Arrays.copyOfRange(args, 1, args.length), this.assigned.async(), this.assigned.inGameOnly());
        }

        if (this.assigned.async()) {
            TaskManager.Async.run(plugin, () -> this.execute(executeArguments));
        } else {
            if (sender instanceof Player p) {
                TaskManager.Sync.run(plugin, p, () -> this.execute(executeArguments));
            } else {
                TaskManager.Sync.run(plugin, () -> this.execute(executeArguments));
            }
        }
    }

    public List<String> onTabComplete(SubCommandArguments command) {
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
}
