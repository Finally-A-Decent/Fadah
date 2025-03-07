package info.preva1l.fadah.metrics;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.records.listing.Listing;

public interface MetricsProvider {
    int METRICS_ID = 21651;

    void setMetrics(Metrics metrics);

    Metrics getMetrics();

    default void setupMetrics(Fadah plugin) {
        Fadah.getConsole().info("Starting Metrics...");

        setMetrics(new Metrics(plugin, METRICS_ID));
        // todo:  this will show inflated numbers on a multi instance server
        getMetrics().addCustomChart(new Metrics.SingleLineChart("items_listed", () -> CacheAccess.getAll(Listing.class).size()));
        getMetrics().addCustomChart(new Metrics.SimplePie("database_type", () -> Config.i().getDatabase().getType().getFriendlyName()));
        getMetrics().addCustomChart(new Metrics.SimplePie(
                "multi_server",
                () -> Config.i().getBroker().isEnabled() ? Config.i().getBroker().getType().getDisplayName() : "None"
                ));

        Fadah.getConsole().info("Metrics Logging Started!");
    }

    default void shutdownMetrics() {
        if (getMetrics() != null) {
            getMetrics().shutdown();
            Fadah.getConsole().info("Metrics Logging Stopped!");
        }
    }
}
