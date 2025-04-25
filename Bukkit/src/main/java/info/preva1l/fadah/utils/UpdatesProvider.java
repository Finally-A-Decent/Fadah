package info.preva1l.fadah.utils;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.trashcan.plugin.UpdateChecker;
import info.preva1l.trashcan.plugin.annotations.PluginEnable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface UpdatesProvider {
    @PluginEnable
    static void checkForUpdates() {
        final UpdateChecker checker = UpdateChecker.builder()
                .currentVersion(Fadah.getInstance().getCurrentVersion())
                .endpoint(UpdateChecker.Endpoint.SPIGOT)
                .resource(Integer.toString(UpdatesHolder.SPIGOT_ID))
                .build();
        checker.check().thenAccept(checked -> UpdatesHolder.completed = checked);

        TaskManager.Sync.runLater(Fadah.getInstance(), () -> notifyUpdate(Bukkit.getConsoleSender()), 60L);
    }

    static void notifyUpdate(@NotNull CommandSender recipient) {
        if (!recipient.hasPermission("fadah.manage.profile")) return;
        var checked = UpdatesHolder.completed;
        if (checked.isUpToDate()) return;
        boolean critical = isCritical(checked);
        if (recipient instanceof Player && !Config.i().isUpdateChecker() && !critical) return;

        recipient.sendMessage(Text.text(
                "&7[Fadah]&f Fadah is &#D63C3COUTDATED&f! &7Current: &#D63C3C%s &7Latest: &#18D53A%s %s"
                        .formatted(checked.getCurrentVersion(),
                                checked.getLatestVersion(),
                                critical
                                        ? "\n&#D63C3C&lThis update is marked as critical. Update as soon as possible."
                                        : ""
                        )
        ));
    }

    private static boolean isCritical(UpdateChecker.Completed checked) {
        return !checked.isUpToDate() &&
                (checked.getLatestVersion().getMetadata().equalsIgnoreCase("hotfix") ||
                        checked.getLatestVersion().getMajor() > checked.getCurrentVersion().getMajor() ||
                        checked.getLatestVersion().getMinor() > checked.getCurrentVersion().getMinor() + 5);
    }

    class UpdatesHolder {
        private static final int SPIGOT_ID = 116157;

        private static UpdateChecker.Completed completed;

        private UpdatesHolder() {}
    }
}
