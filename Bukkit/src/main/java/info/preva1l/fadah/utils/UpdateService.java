package info.preva1l.fadah.utils;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;

public class UpdateService {
    public static final UpdateService instance = new UpdateService();

    private static final int SPIGOT_ID = 116157;

    private Fadah plugin;
    private String currentVersion;

    private CheckedUpdate completed;

    public UpdateService init(Fadah plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getCurrentVersion();
        return this;
    }

    public void checkForUpdates() {
        URI endpoint = URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + SPIGOT_ID);
        HttpRequest request = HttpRequest.newBuilder(endpoint).GET().build();
        HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(String::trim)
                .thenAccept(latest -> completed = new CheckedUpdate(currentVersion, latest))
                .exceptionally(throwable -> {
                    plugin.getLogger().log(Level.WARNING, "Failed to check for updates", throwable);
                    return null;
                });

        Tasks.syncDelayed(plugin, () -> notifyUpdate(Bukkit.getConsoleSender()), 60L);
    }

    public void notifyUpdate(@NotNull CommandSender recipient) {
        if (completed == null) return;
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

    private boolean isCritical(CheckedUpdate checked) {
        return !checked.isUpToDate() &&
                (checked.latestHasMetadata("hotfix") ||
                        checked.latestMajor() > checked.currentMajor() ||
                        checked.latestMinor() > checked.currentMinor() + 5);
    }

    private record CheckedUpdate(String currentVersion, String latestVersion) {
        boolean isUpToDate() {
            return currentVersion.equalsIgnoreCase(latestVersion);
        }

        String getCurrentVersion() {
            return currentVersion;
        }

        String getLatestVersion() {
            return latestVersion;
        }

        boolean latestHasMetadata(String metadata) {
            String[] parts = latestVersion.split("-", 2);
            return parts.length == 2 && parts[1].equalsIgnoreCase(metadata);
        }

        int currentMajor() {
            return versionPart(currentVersion, 0);
        }

        int currentMinor() {
            return versionPart(currentVersion, 1);
        }

        int latestMajor() {
            return versionPart(latestVersion, 0);
        }

        int latestMinor() {
            return versionPart(latestVersion, 1);
        }

        private int versionPart(String version, int index) {
            String cleanVersion = version.split("-", 2)[0];
            String[] parts = cleanVersion.split("\\.");
            if (index >= parts.length) return 0;
            try {
                return Integer.parseInt(parts[index]);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
    }
}
