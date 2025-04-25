package info.preva1l.fadah.commands;

import dev.triumphteam.cmd.bukkit.BukkitCommand;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.BukkitSubCommand;
import dev.triumphteam.cmd.bukkit.CommandPermission;
import dev.triumphteam.cmd.bukkit.message.NoPermissionMessageContext;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.message.MessageKey;
import dev.triumphteam.cmd.core.message.context.DefaultMessageContext;
import dev.triumphteam.cmd.core.message.context.InvalidArgumentContext;
import dev.triumphteam.cmd.core.message.context.MessageContext;
import dev.triumphteam.cmd.core.requirement.RequirementKey;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.commands.subcommands.SubCommandInfo;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.migrator.MigrationProvider;
import info.preva1l.fadah.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

public interface CommandProvider {
    Fadah getPlugin();

    default void loadCommands() {
        loadCommandManager();
        registerCommands();
        loadCommandCache();
    }

    default List<SubCommandInfo> getSubCommands(String command) {
        return CommandManagerHolder.subCommands.get(command);
    }

    private void registerCommands() {
        getPlugin().getLogger().info("Registering Commands...");
        Stream.of(
                new AuctionHouseCommand(getPlugin()),
                new MigrateCommand()
        ).forEach(CommandManagerHolder.commandManager::registerCommand);
        getPlugin().getLogger().info("Commands Registered!");
    }

    private void loadCommandManager() {
        getPlugin().getLogger().info("Loading CommandManager...");
        CommandManagerHolder.commandManager = BukkitCommandManager.create(getPlugin());
        registerMessages();
        registerArguments();

        CommandManagerHolder.commandManager.registerRequirement(
                RequirementKey.of("enabled"),
                sender -> {
                    boolean enabled = Config.i().isEnabled();
                    if (!enabled) {
                        send(sender, Lang.i().getPrefix() + Lang.i().getErrors().getDisabled());
                    }
                    return enabled;
                }
        );
        getPlugin().getLogger().info("CommandManager Registered!");
    }

    private void registerMessages() {
        CommandManagerHolder.commandManager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, this::badArgs);
        CommandManagerHolder.commandManager.registerMessage(MessageKey.INVALID_ARGUMENT, this::badArgs);
        CommandManagerHolder.commandManager.registerMessage(
                MessageKey.UNKNOWN_COMMAND,
                (user, context) -> send(user, Lang.i().getErrors().getCommandNotFound())
        );
        CommandManagerHolder.commandManager.registerMessage(
                MessageKey.of("NO_PERMISSION", NoPermissionMessageContext.class),
                (user, context) -> send(user, Lang.i().getErrors().getNoPermission())
        );
        CommandManagerHolder.commandManager.registerMessage(
                MessageKey.of("PLAYER_ONLY", DefaultMessageContext.class),
                (user, context) -> send(user, Lang.i().getErrors().getMustBePlayer())
        );
    }

    private void registerArguments() {
        CommandManagerHolder.commandManager.registerArgument(
                OfflinePlayer.class,
                (sender, argument) -> Bukkit.getOfflinePlayerIfCached(argument)
        );
        CommandManagerHolder.commandManager.registerArgument(
                UUID.class,
                (sender, argument) -> {
                    try {
                        return UUID.fromString(argument);
                    } catch (IllegalArgumentException ignored) {
                        return null;
                    }
                }
        );
        CommandManagerHolder.commandManager.registerArgument(
                Double.TYPE,
                (sender, argument) -> {
                    try {
                        return Text.getAmountFromString(argument);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                }
        );
        // Plugin Migrators
        CommandManagerHolder.commandManager.registerArgument(
                Plugin.class,
                (sender, argument) -> {
                    Plugin plugin = Bukkit.getPluginManager().getPlugin(argument);
                    if (plugin == null || !plugin.isEnabled()) {
                        return null;
                    }
                    return plugin;
                }
        );
        CommandManagerHolder.commandManager.registerSuggestion(
                Plugin.class,
                (sender, context) -> MigrationProvider.getMigratorNames()
        );
    }

    private <T extends MessageContext> void badArgs(CommandSender sender, T context) {
        if (context instanceof InvalidArgumentContext invalid) {
            if (invalid.getArgumentType().isAssignableFrom(OfflinePlayer.class)) {
                send(sender, Lang.i().getErrors().getPlayerNotFound().replace("%player%", invalid.getTypedArgument()));
                return;
            }

            if (invalid.getArgumentType().isAssignableFrom(Double.TYPE)) {
                send(sender, Lang.i().getCommands().getSell().getMustBeNumber());
                return;
            }

            send(sender, Lang.i().getErrors().getInvalidArgument()
                    .replace("%arg%", invalid.getTypedArgument())
                    .replace("%type%", invalid.getArgumentType().getSimpleName())
            );
            return;
        }

        send(sender, Lang.i().getErrors().getInvalidUsage().replace("%command%",
                switch (context.getSubCommand()) {
                    case "sell" -> Lang.i().getCommands().getSell().getUsage();
                    case "view" -> Lang.i().getCommands().getView().getUsage();
                    case "search" -> Lang.i().getCommands().getSearch().getUsage();
                    case "view-listing" -> Lang.i().getCommands().getViewListing().getUsage();
                    case Default.DEFAULT_CMD_NAME -> "&cUsage: &f/fadah-migrate <plugin>";
                    default -> "Unkown Command Arguments!";
                }));
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(Text.text(sender instanceof Player p ? p : null, message));
    }

    private void loadCommandCache() {
        try {
            CommandMap commandMap = getCommandMap();
            for (Command bukkitCommand : commandMap.getKnownCommands().values()) {
                if (!(bukkitCommand instanceof BukkitCommand<?>)) continue;
                BukkitCommand<CommandSender> triumphCommand = (BukkitCommand<CommandSender>) bukkitCommand;

                CommandManagerHolder.commands.put(bukkitCommand.getName(), triumphCommand);
                CommandManagerHolder.subCommands.put(triumphCommand.getName(), extractSubCommands(triumphCommand));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private CommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        Field commandMapField = BukkitCommandManager.class.getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        return (CommandMap) commandMapField.get(CommandManagerHolder.commandManager);
    }

    @SuppressWarnings("unchecked")
    private List<SubCommandInfo> extractSubCommands(BukkitCommand<CommandSender> command)
            throws NoSuchFieldException, IllegalAccessException {
        Field subCommandsField = BukkitCommand.class.getDeclaredField("subCommands");
        subCommandsField.setAccessible(true);

        Map<String, BukkitSubCommand<CommandSender>> subCommands =
                (Map<String, BukkitSubCommand<CommandSender>>) subCommandsField.get(command);

        List<SubCommandInfo> infos = new ArrayList<>();
        for (BukkitSubCommand<CommandSender> cmd : subCommands.values()) {
            Tuple<List<String>, String> descriptionAndAliases = getCommandInfo(cmd.getName());
            descriptionAndAliases.first.forEach(alias -> command.addSubCommandAlias(alias, cmd));
            CommandManagerHolder.commands.put(command.getName(), command);

            CommandPermission permission = cmd.getPermission() != null
                    ? cmd.getPermission()
                    : new CommandPermission(List.of(), "Default Permission Handle", PermissionDefault.TRUE);

            infos.add(new SubCommandInfo(cmd.getName(), descriptionAndAliases.second, permission));
        }
        return infos;
    }

    private Tuple<List<String>, String> getCommandInfo(String commandName) {
        return switch (commandName) {
            case "help" -> Tuple.of(Lang.i().getCommands().getHelp().getAliases(), Lang.i().getCommands().getHelp().getDescription());
            case "view" -> Tuple.of(Lang.i().getCommands().getView().getAliases(), Lang.i().getCommands().getView().getDescription());
            case "active-listings" -> Tuple.of(Lang.i().getCommands().getActiveListings().getAliases(), Lang.i().getCommands().getActiveListings().getDescription());
            case "search" -> Tuple.of(Lang.i().getCommands().getSearch().getAliases(), Lang.i().getCommands().getSearch().getDescription());
            case "sell" -> Tuple.of(Lang.i().getCommands().getSell().getAliases(), Lang.i().getCommands().getSell().getDescription());
            case "view-listing" -> Tuple.of(Lang.i().getCommands().getViewListing().getAliases(), Lang.i().getCommands().getViewListing().getDescription());
            case "history" -> Tuple.of(Lang.i().getCommands().getHistory().getAliases(), Lang.i().getCommands().getHistory().getDescription());
            case "collection-box" -> Tuple.of(Lang.i().getCommands().getCollectionBox().getAliases(), Lang.i().getCommands().getCollectionBox().getDescription());
            case "about" -> Tuple.of(Lang.i().getCommands().getAbout().getAliases(), Lang.i().getCommands().getAbout().getDescription());
            case "expired-items" -> Tuple.of(Lang.i().getCommands().getExpiredItems().getAliases(), Lang.i().getCommands().getExpiredItems().getDescription());
            case "profile" -> Tuple.of(Lang.i().getCommands().getProfile().getAliases(), Lang.i().getCommands().getProfile().getDescription());
            case "watch" -> Tuple.of(Lang.i().getCommands().getWatch().getAliases(), Lang.i().getCommands().getWatch().getDescription());
            case "reload" -> Tuple.of(Lang.i().getCommands().getReload().getAliases(), Lang.i().getCommands().getReload().getDescription());
            case "toggle" -> Tuple.of(Lang.i().getCommands().getToggle().getAliases(), Lang.i().getCommands().getToggle().getDescription());
            default -> Tuple.of(List.of("bla"), "Unknown");
        };
    }

    class CommandManagerHolder {
        public static BukkitCommandManager<CommandSender> commandManager;
        private static final Map<String, BukkitCommand<CommandSender>> commands = new HashMap<>();
        private static final Map<String, List<SubCommandInfo>> subCommands = new HashMap<>();
    }
}
