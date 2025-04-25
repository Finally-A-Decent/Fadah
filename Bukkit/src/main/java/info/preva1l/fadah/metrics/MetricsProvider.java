package info.preva1l.fadah.metrics;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.trashcan.plugin.annotations.PluginDisable;
import info.preva1l.trashcan.plugin.annotations.PluginEnable;

public interface MetricsProvider {
    int METRICS_ID = 21651;

    @PluginEnable
    static void setupMetrics() {
        Fadah.getConsole().info("Starting Metrics...");

        MetricsHolder.metrics = new Metrics(Fadah.getInstance(), METRICS_ID);
        MetricsHolder.metrics.addCustomChart(new Metrics.SingleLineChart("items_listed", () -> CacheAccess.size(Listing.class)));
        MetricsHolder.metrics.addCustomChart(new Metrics.SimplePie("database_type", () -> Config.i().getDatabase().getType().getFriendlyName()));
        MetricsHolder.metrics.addCustomChart(new Metrics.SimplePie(
                "multi_server",
                () -> Config.i().getBroker().isEnabled() ? Config.i().getBroker().getType().getDisplayName() : "None"
        ));


        Fadah.getConsole().info("Metrics Logging Started!");
    }

    @PluginDisable
    static void shutdownMetrics() {
        if (MetricsHolder.metrics != null) {
            MetricsHolder.metrics.shutdown();
        }
    }

    class MetricsHolder {
        private static Metrics metrics;

        private MetricsHolder() {}
    }
}
