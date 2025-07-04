package info.preva1l.fadah.utils;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.trashcan.Version;
import info.preva1l.trashcan.extension.UpdateChecker;
import info.preva1l.trashcan.flavor.annotations.Configure;
import info.preva1l.trashcan.flavor.annotations.Service;
import info.preva1l.trashcan.flavor.annotations.inject.Inject;
import info.preva1l.trashcan.flavor.annotations.inject.condition.Named;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Service
public class UpdateService {
    public static final UpdateService instance = new UpdateService();

    private static final int SPIGOT_ID = 116157;

    @Inject private Fadah plugin;
    @Inject @Named("plugin:version") private Version currentVersion;

    private UpdateChecker.Completed completed;

    @Configure
    public void checkForUpdates() {
        final UpdateChecker checker = UpdateChecker.builder()
                .currentVersion(currentVersion)
                .endpoint(UpdateChecker.Endpoint.SPIGOT)
                .resource(Integer.toString(SPIGOT_ID))
                .build();
        checker.check().thenAccept(checked -> completed = checked);

        Tasks.syncDelayed(plugin, () -> notifyUpdate(Bukkit.getConsoleSender()), 60L);
    }

    public void notifyUpdate(@NotNull CommandSender recipient) {
        if (!recipient.hasPermission("fadah.manage.profile")) return;
        if (completed.isUpToDate()) return;
        boolean critical = isCritical(completed);
        if (recipient instanceof Player && !Config.i().isUpdateChecker() && !critical) return;

        recipient.sendMessage(Text.text(
                "&7[Fadah]&f Fadah is &#D63C3COUTDATED&f! &7Current: &#D63C3C%s &7Latest: &#18D53A%s %s"
                        .formatted(completed.getCurrentVersion(),
                                completed.getLatestVersion(),
                                critical
                                        ? "\n&#D63C3C&lThis update is marked as critical. Update as soon as possible."
                                        : ""
                        )
        ));
    }

    private boolean isCritical(UpdateChecker.Completed checked) {
        return !checked.isUpToDate() &&
                (checked.getLatestVersion().getMetadata().equalsIgnoreCase("hotfix") ||
                        checked.getLatestVersion().getMajor() > checked.getCurrentVersion().getMajor() ||
                        checked.getLatestVersion().getMinor() > checked.getCurrentVersion().getMinor() + 5);
    }
}
