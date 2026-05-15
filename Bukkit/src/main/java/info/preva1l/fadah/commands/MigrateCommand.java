package info.preva1l.fadah.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.migrator.MigrationService;
import info.preva1l.fadah.migrator.Migrator;
import info.preva1l.fadah.utils.Tasks;
import info.preva1l.fadah.utils.Text;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.parser.Parser;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandInput;

import java.time.Instant;
import java.util.List;

public class MigrateCommand {
    @Command("fadah-migrate <migrator>")
    @Permission("fadah.migrate")
    public void execute(
            Player player,
            Migrator migrator
    ) {
        long start = Instant.now().toEpochMilli();
        player.sendMessage(Text.text(Lang.i().getPrefix() + "&fStarting migration from %s..."
                .formatted(migrator.getMigratorName())));

        migrator.startMigration().thenRun(() ->
                Tasks.sync(Fadah.getInstance(), player,
                        () -> player.sendMessage(Text.text(Lang.i().getPrefix() + "&aMigration from %s complete! &7(Took: %sms)"
                                .formatted(migrator.getMigratorName(), Instant.now().toEpochMilli() - start))),
                        () -> {}));
    }

    @Parser
    public Migrator getMigrator(CommandInput input) {
        String migratorName = input.readString();
        return MigrationService.instance.getMigrator(migratorName)
                .orElseThrow(() -> new UnknownMigratorException(migratorName));
    }

    @Suggestions("migrator")
    public List<String> suggestions() {
        return MigrationService.instance.getMigratorNames();
    }

    public static class UnknownMigratorException extends RuntimeException {
        public UnknownMigratorException(String migratorName) {
            super("migrator '" + migratorName + "' not found");
        }
    }
}
