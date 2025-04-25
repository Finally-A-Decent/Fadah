package info.preva1l.fadah.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.utils.Text;
import info.preva1l.trashcan.commands.bukkit.annotation.Permission;
import info.preva1l.trashcan.commands.core.BaseCommand;
import info.preva1l.trashcan.commands.core.annotation.Command;
import info.preva1l.trashcan.commands.core.annotation.Default;
import info.preva1l.trashcan.commands.core.annotation.Requirement;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.util.List;

@Command("fadah-migrate")
@Permission("fadah.migrate")
public class MigrateCommand extends BaseCommand {
    private final Fadah fadah;

    public MigrateCommand(Fadah fadah) {
        super(List.of("ah-migrate"));
        this.fadah = fadah;
    }

    @Default
    @Requirement("enabled")
    public void execute(Player player, Plugin plugin) {
        fadah.getMigrator(plugin.getName())
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
