package info.preva1l.fadah.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.Requirement;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.migrator.MigrationProvider;
import info.preva1l.fadah.utils.Text;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.util.List;

@Command("fadah-migrate")
@Permission("fadah.migrate")
public class MigrateCommand extends BaseCommand {
    public MigrateCommand() {
        super(List.of("ah-migrate"));
    }

    @Default
    @Requirement("enabled")
    public void execute(Player player, Plugin plugin) {
        MigrationProvider.getMigrator(plugin.getName())
                .ifPresentOrElse(migrator -> {
                    long start = Instant.now().toEpochMilli();
                    player.sendMessage(Text.text(Lang.i().getPrefix() + "&fStarting migration from %s..."
                            .formatted(migrator.getMigratorName())));

                    migrator.startMigration().thenRun(() ->
                            player.sendMessage(Text.text(Lang.i().getPrefix() + "&aMigration from %s complete! &7(Took: %sms)"
                                    .formatted(migrator.getMigratorName(), Instant.now().toEpochMilli() - start))));
                }, () -> player.sendMessage(Text.text(Lang.i().getPrefix() + "&cMigrator does not exist!")));
    }
}
