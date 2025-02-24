package info.preva1l.fadah.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.migrator.Migrator;
import info.preva1l.fadah.utils.commands.Command;
import info.preva1l.fadah.utils.commands.CommandArgs;
import info.preva1l.fadah.utils.commands.CommandArguments;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MigrateCommand extends Command {
    public MigrateCommand(Fadah plugin) {
        super(plugin, List.of("ah-migrate"));
    }

    @CommandArgs(name = "fadah-migrate", permission = "fadah.migrate", inGameOnly = false)
    public void execute(@NotNull CommandArguments command) {
        if (command.args().length == 0) {
            command.reply(Lang.i().getPrefix() + "&cUsage: &f/fadah-migrate <plugin>");
            return;
        }

        if (!plugin.getMigrationManager().migratorExists(command.args()[0])) {
            command.reply(Lang.i().getPrefix() + "&cMigrator does not exist!");
            return;
        }

        Migrator migrator = plugin.getMigrationManager().getMigrator(command.args()[0]);
        assert migrator != null;

        long start = Instant.now().toEpochMilli();
        command.reply(Lang.i().getPrefix() + "&fStarting migration from %s...".formatted(migrator.getMigratorName()));
        migrator.startMigration(plugin).thenRun(() -> {
            command.reply(Lang.i().getPrefix() + "&aMigration from %s complete! &7(Took: %sms)"
                    .formatted(migrator.getMigratorName(), Instant.now().toEpochMilli() - start));
        });
    }

    @Override
    public List<String> onTabComplete(CommandArguments command) {
        List<String> completions = new ArrayList<>();
        if (command.args().length != 1) return completions;
        StringUtil.copyPartialMatches(command.args()[0], plugin.getMigrationManager().getMigratorNames(), completions);
        return completions;
    }
}
